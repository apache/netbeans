/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.api.java.source.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceLevelQuery.Profile;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.classfile.Annotation;
import org.netbeans.modules.classfile.AnnotationComponent;
import org.netbeans.modules.classfile.CPEntry;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;
import org.netbeans.modules.classfile.ElementValue;
import org.netbeans.modules.classfile.PrimitiveElementValue;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.parsing.Archive;
import org.netbeans.modules.java.source.parsing.CachingArchiveProvider;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.Union2;
import org.openide.util.BaseUtilities;

/**
 * Utility methods for JDK 8 Profiles.
 * @author Tomas Zezula
 * @since 0.119
 */
public class ProfileSupport {

    private static final String RES_MANIFEST = "META-INF/MANIFEST.MF";              //NOI18N
    private static final String ATTR_PROFILE = "Profile";                           //NOI18N
    private static final String ANNOTATION_PROFILE = "jdk/Profile+Annotation";      //NOI18N
    private static final String ANNOTATION_VALUE = "value";                         //NOI18N
    private static final Logger LOG = Logger.getLogger(ProfileSupport.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(ProfileSupport.class);

    private ProfileSupport() {}

    /**
     * Kind of profile validation.
     */
    public enum Validation {
        /**
         * Request to validate sources on source path.
         */
        SOURCES,

        /**
         * Request to validate binaries on compile class path by Profile attribute in Manifest.
         * The manifest based validation does not analyze class files and is significantly faster
         * compared to {@link Validation#BINARIES_BY_CLASS_FILES} but does not work for jar
         * files with no Profile attributes.
         */
        BINARIES_BY_MANIFEST,

        /**
         * Request to validate binaries on compile class path by references in class files.
         */
        BINARIES_BY_CLASS_FILES
    }

    /**
     * Violation of profile.
     * The violation can be caused either by jar file on classpath requiring
     * a higher profile in manifest or by a source or class file referring to
     * a type from higher profile.
     */
    public static class Violation {

        private final URL root;
        private final Profile profile;
        private final URL file;
        private final ElementHandle<TypeElement> type;

        private Violation(
                @NonNull final URL root,
                @NullAllowed final Profile profile,
                @NullAllowed final URL file,
                @NullAllowed final ElementHandle<TypeElement> type) {
            Parameters.notNull("root", root);   //NOI18N
            this.root = root;
            this.profile = profile;
            this.file = file;
            this.type = type;
        }

        /**
         * Returns the root which violates the tested profile.
         * @return the root {@link URL}
         */
        @NonNull
        public URL getRoot() {
            return root;
        }

        /**
         * Returns the {@link Profile} required by a root or file.
         * @return the {@link Profile} required by a root in the manifest
         * or by class (source) file under the root. May return a null if the
         * manifest contains invalid profile attribute.
         */
        @CheckForNull
        public Profile getRequiredProfile() {
            return profile;
        }

        /**
         * Returns the file which violates the tested profile.
         * @return the file referring to a type from higher profile. May return
         * null if the whole archive requires a higher profile.
         */
        @CheckForNull
        public URL getFile() {
            return file;
        }

        /**
         * Returns the type from higher {@link Profile} causing the violation.
         * @return the type causing the violation or null if the whole archive
         * requires a higher profile.
         */
        @CheckForNull
        public ElementHandle<TypeElement> getUsedType() {
            return type;
        }
    }

    /**
     * Asynchronous callback for collecting the profile {@link Violation}s.
     * For each classpath or source root a new {@link ViolationCollector} is
     * created by the {@link ViolationCollectorFactory#create}. When the validation
     * of the root is done the {@link ViolationCollector#finished} is called.
     * Threading: Validations of individual roots may run in parallel depending
     * on the {@link Executor} throughput.
     */
    public interface ViolationCollector {
        /**
         * Called to report a {@link Profile} violation.
         * @param violation the {@link Violation} to be reported.
         */
        void reportProfileViolation(@NonNull Violation violation);
        /**
         * Called when the validation of whole a root has finished.
         */
        void finished();
    }

    /**
     * Factory for {@link ViolationCollector}.
     * For each root a new {@link ViolationCollector} is created by the factory.
     * Threading: Validations of individual roots may run in parallel depending
     * on the {@link Executor} throughput.
     */
    public interface ViolationCollectorFactory {
        /**
         * Creates a new {@lni ViolationCollector} for given root.
         * @param root the root to be validated
         * @return a new {@link ViolationCollector}
         */
        @NonNull
        ViolationCollector create(@NonNull URL root);

        /**
         * Signals that the validation should be canceled.
         * @return if true the validation is canceled.
         */
        boolean isCancelled();
    }

    /**
     * Asynchronously finds the {@link Profile} violations in given source and classpath roots.
     * @param profileToCheck the {@link Profile} to be verified
     * @param bootClassPath  the boot classpath of JDK 8 platform to get the profile info from
     * @param compileClassPath the compile classpath to be validated
     * @param sourcePath the source path to be validated
     * @param check types of validation
     * @param collectorFactory the {@link Violation}s collector
     * @throws IllegalArgumentException if the bootClassPath is not a valid JDK 8 boot classpath
     */
    public static void findProfileViolations(
            @NonNull final Profile profileToCheck,
            @NonNull final Iterable<URL> bootClassPath,
            @NonNull final Iterable<URL> compileClassPath,
            @NonNull final Iterable<URL> sourcePath,
            @NonNull final Set<Validation> check,
            @NonNull final ViolationCollectorFactory collectorFactory) {
        findProfileViolations(
                profileToCheck,
                bootClassPath,
                compileClassPath,
                sourcePath,
                check,
                collectorFactory,
                RP);
    }

    /**
     * Synchronously finds the {@link Profile} violations in given source and classpath roots.
     * @param profileToCheck the {@link Profile} to be verified
     * @param bootClassPath  the boot classpath of JDK 8 platform to get the profile info from
     * @param compileClassPath the compile classpath to be validated
     * @param sourcePath the source path to be validated
     * @param check types of validation
     * @return the {@link Collection} of found {@link Violation}s
     * @throws IllegalArgumentException if the bootClassPath is not a valid JDK 8 boot classpath
     */
    @NonNull
    public static Collection<Violation> findProfileViolations(
            @NonNull final Profile profileToCheck,
            @NonNull final Iterable<URL> bootClassPath,
            @NonNull final Iterable<URL> compileClassPath,
            @NonNull final Iterable<URL> sourcePath,
            @NonNull final Set<Validation> check) {
        final DefaultProfileViolationCollector collector =
                new DefaultProfileViolationCollector();
        findProfileViolations(
                profileToCheck,
                bootClassPath,
                compileClassPath,
                sourcePath,
                check,
                collector,
                new CurrentThreadExecutor());
        return collector.getViolations();
    }

    /**
     * Asynchronously finds the {@link Profile} violations in given source and classpath roots.
     * @param profileToCheck the {@link Profile} to be verified
     * @param bootClassPath  the boot classpath of JDK 8 platform to get the profile info from
     * @param compileClassPath the compile classpath to be validated
     * @param sourcePath the source path to be validated
     * @param check types of validation
     * @param collectorFactory the {@link Violation}s collector
     * @param executor to use for the asynchronous operation, may have higher throughput
     * @throws IllegalArgumentException if the bootClassPath is not a valid JDK 8 boot classpath
     */
    public static void findProfileViolations(
            @NonNull final Profile profileToCheck,
            @NonNull final Iterable<URL> bootClassPath,
            @NonNull final Iterable<URL> compileClassPath,
            @NonNull final Iterable<URL> sourcePath,
            @NonNull final Set<Validation> check,
            @NonNull final ViolationCollectorFactory collectorFactory,
            @NonNull final Executor executor) {
        Parameters.notNull("profileToCheck", profileToCheck);   //NOI18N
        Parameters.notNull("compileClassPath", compileClassPath);   //NOI18N
        Parameters.notNull("sourcePath", sourcePath);   //NOI18N
        Parameters.notNull("check", check);     //NOI18N
        Parameters.notNull("collectorFactory", collectorFactory); //NOI18N
        Parameters.notNull("executor", executor);   //NOI18N
        final Context ctx = new Context(profileToCheck, bootClassPath, collectorFactory, check);
        if (check.contains(Validation.BINARIES_BY_MANIFEST) ||
            check.contains(Validation.BINARIES_BY_CLASS_FILES)) {
            for (final URL compileRoot : compileClassPath) {
                executor.execute(Validator.forBinary(compileRoot, ctx));
            }
        }
        if (check.contains(Validation.SOURCES)) {
            for (final URL sourceRoot : sourcePath) {
                executor.execute(Validator.forSource(sourceRoot, ctx));
            }
        }
    }

    private static final class Context {

        private final ArchiveCache archiveCache;
        private final TypeCache typeCache;
        private final Profile profileToCheck;
        private final ViolationCollectorFactory factory;
        private final Set<Validation> validations;

        Context(
                @NonNull final Profile profileToCheck,
                @NonNull final Iterable<? extends URL> bootClassPath,
                @NonNull final ViolationCollectorFactory factory,
                @NonNull final Set<Validation> validations) {
            assert profileToCheck != null;
            assert bootClassPath != null;
            assert factory != null;
            assert validations != null;
            this.archiveCache = ArchiveCache.getInstance();
            this.typeCache =
                    !bootClassPath.iterator().hasNext() &&
                    (validations.isEmpty() || validations.equals(EnumSet.of(Validation.BINARIES_BY_MANIFEST))) ?
                    null :
                    TypeCache.newInstance(bootClassPath);
            this.profileToCheck = profileToCheck;
            this.factory = factory;
            this.validations = EnumSet.copyOf(validations);
        }

        @NonNull
        ArchiveCache getArchiveCache() {
            return archiveCache;
        }

        @NonNull
        TypeCache getTypeCache() {
            if (typeCache == null) {
                throw new IllegalArgumentException("No type cache");            //NOI18N
            }
            return typeCache;
        }

        @NonNull
        Profile getRequredProfile() {
            return profileToCheck;
        }

        @NonNull
        ViolationCollector newCollector(@NonNull final URL root) {
            return factory.create(root);
        }

        boolean shouldValidate(@NonNull final Validation validation) {
            return validations.contains(validation);
        }

        boolean isCancelled() {
            return factory.isCancelled();
        }

    }

    private abstract static class Validator implements Runnable {

        protected final Context context;
        protected final URL root;
        

        Validator(            
            @NonNull final URL root,
            @NonNull final Context context) {
            assert root != null;
            assert context != null;
            this.root = root;
            this.context = context;
        }

        @Override
        public final void run() {
            final ViolationCollector collector = context.newCollector(root);
            assert collector != null;
            try {
                validate(collector);
            } finally {
                collector.finished();
            }
        }        

        protected final void validateBinaryRoot(
                @NonNull final URL root,
                @NonNull final ViolationCollector collector) {
            final FileObject rootFo = URLMapper.findFileObject(root);
            if (rootFo == null) {
                return;
            }
            final Enumeration<? extends FileObject> children = rootFo.getChildren(true);
            while (children.hasMoreElements()) {
                if (context.isCancelled()) {
                    break;
                }
                final FileObject fo = children.nextElement();
                if (isImportant(fo)) {
                    validateBinaryFile(fo, collector);
                }
            }
        }

        @CheckForNull
        protected URL map(@NonNull final FileObject fo) {
            return fo.toURL();
        }

        protected abstract void validate(@NonNull ViolationCollector collector);

        private boolean isImportant(@NonNull final FileObject file) {
            return file.isData() &&
                (FileObjects.CLASS.equals(file.getExt()) || FileObjects.SIG.equals(file.getExt()));
        }

        private void validateBinaryFile(
                @NonNull final FileObject fo,
                @NonNull final ViolationCollector collector) {
            final Profile profileToCheck = context.getRequredProfile();
            final TypeCache tc = context.getTypeCache();
            try {
                try (InputStream in = fo.getInputStream()) {
                    ClassFile cf = new ClassFile(in);
                    for (ClassName className : cf.getAllClassNames()) {
                        final Profile p = tc.profileForType(className);
                        if (p != null && profileToCheck.compareTo(p) < 0) {
                            collector.reportProfileViolation(
                                new Violation(
                                    root,
                                    p,
                                    map(fo),
                                    ElementHandleAccessor.getInstance().create(ElementKind.CLASS, className.getInternalName().replace('/', '.'))    //NOI18N
                            ));
                        }
                    }
                }
            } catch (IOException ioe) {
                LOG.log(
                    Level.INFO,
                    "Cannot validate file: {0}",    //NOI18N
                    FileUtil.getFileDisplayName(fo));
            }
        }

        static Validator forSource(
                @NonNull final URL root,
                @NonNull final Context context) {
            return new SourceValidator(root, context);
        }

        static Validator forBinary(
                @NonNull final URL root,
                @NonNull final Context context) {
            return new BinaryValidator(root, context);
        }

        private static final class BinaryValidator extends Validator {

            private BinaryValidator(
                @NonNull final URL root,
                @NonNull final Context context) {
                super(root, context);                
            }

            @Override
            protected void validate(@NonNull final ViolationCollector collector) {
                if (context.isCancelled()) {
                    return;
                }
                Profile current = null;
                if (context.shouldValidate(Validation.BINARIES_BY_MANIFEST)) {
                    final Union2<Profile,String> res = findProfileInManifest(root);
                    if (!res.hasFirst()) {
                        //Invalid value of profile in manifest of dependent jar
                        collector.reportProfileViolation(new Violation(root, null, null, null));
                        return;
                    }
                    current = res.first();
                    if (current != Profile.DEFAULT && current.compareTo(context.getRequredProfile()) > 0) {
                        //Hiher profile in manifest of dependent jar
                        collector.reportProfileViolation(new Violation(root, res.first(), null, null));
                        return;
                    }                    
                }
                if (context.shouldValidate(Validation.BINARIES_BY_CLASS_FILES)) {
                    if (current == null || current == Profile.DEFAULT) {
                        validateBinaryRoot(root, collector);
                    }
                }
            }

            @NonNull
            private Union2<Profile,String> findProfileInManifest(@NonNull URL root) {
                final ArchiveCache ac = context.getArchiveCache();
                Union2<Profile,String> res;
                final ArchiveCache.Key key = ac.createKey(root);
                if (key != null) {
                    res = ac.getProfile(key);
                    if (res != null) {
                        return res;
                    }
                }
                String profileName = null;
                final FileObject rootFo = URLMapper.findFileObject(root);
                if (rootFo != null) {
                    final FileObject manifestFile = rootFo.getFileObject(RES_MANIFEST);
                    if (manifestFile != null) {
                        try {
                            try (InputStream in = manifestFile.getInputStream()) {
                                final Manifest manifest = new Manifest(in);
                                final Attributes attrs = manifest.getMainAttributes();
                                profileName = attrs.getValue(ATTR_PROFILE);
                            }
                        } catch (IOException ioe) {
                            LOG.log(
                                Level.INFO,
                                "Cannot read Profile attribute from: {0}", //NOI18N
                                FileUtil.getFileDisplayName(manifestFile));
                        }
                    }
                }
                final Profile profile = Profile.forName(profileName);
                res = profile != null ?
                        Union2.<Profile,String>createFirst(profile) :
                        Union2.<Profile,String>createSecond(profileName);
                if (key != null) {
                    ac.putProfile(key, res);
                }
                return res;
            }

        }

        private static final class SourceValidator extends Validator {

            private final File cacheRoot;
            private final ClasspathInfo resolveCps;

            private SourceValidator(
               @NonNull final URL root,
               @NonNull final Context context) {
                super(root, context);
                File f;
                try {
                    f = JavaIndex.getClassFolder(root, true);
                } catch (IOException ioe) {
                    f = null;
                }
                cacheRoot = f;
                resolveCps = ClasspathInfo.create(
                    ClassPath.EMPTY,
                    ClassPath.EMPTY,
                    ClassPathSupport.createClassPath(root));
            }

            @Override
            protected void validate(@NonNull final ViolationCollector collector) {
                if (context.isCancelled()) {
                    return;
                }
                try {
                    if (cacheRoot != null) {
                        validateBinaryRoot(BaseUtilities.toURI(cacheRoot).toURL(), collector);
                    }
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            @Override
            @CheckForNull
            protected URL map(@NonNull final FileObject fo) {
                final String relative = FileObjects.convertFolder2Package(
                        FileObjects.stripExtension(FileObjects.getRelativePath(cacheRoot, FileUtil.toFile(fo))), File.separatorChar);
                final FileObject sourceFile = SourceUtils.getFile(
                    ElementHandleAccessor.getInstance().create(ElementKind.CLASS, relative),
                    resolveCps);
                return sourceFile == null ? null : sourceFile.toURL();
            }            
        }        
    }
    

    private static class CurrentThreadExecutor implements Executor {
        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }

    private static class DefaultProfileViolationCollector implements ViolationCollectorFactory, ViolationCollector {

        private final Queue<Violation> violations = new ArrayDeque<>();

        @Override
        public ViolationCollector create(@NonNull final URL root) {
            return this;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public void reportProfileViolation(@NonNull final Violation violation) {
            violations.offer(violation);
        }

        @Override
        public void finished() {
        }

        Collection<Violation> getViolations() {
            return Collections.unmodifiableCollection(violations);
        }
    }

    //@ThreadSafe
    private static final class ArchiveCache {

        private static final int MAX_CACHE_SIZE = Integer.getInteger(
                "ProfileSupport.ArchiveCache.size",    //NOI18N
                1<<10);

        //@GuardedBy("ArchiveCache.class")
        private static volatile ArchiveCache instance;

        //@GuardedBy("cache")
        private final Map<Key,Union2<Profile,String>> cache;

        private ArchiveCache() {
            this.cache  = Collections.synchronizedMap(new LinkedHashMap<Key,Union2<Profile,String>>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Key, Union2<Profile,String>> entry) {
                    return size() > MAX_CACHE_SIZE;
                }
            });
        }

        @NonNull
        static ArchiveCache getInstance() {
            ArchiveCache cache = instance;
            if (cache == null) {
                synchronized (ArchiveCache.class) {
                    cache = instance;
                    if (cache == null) {
                        instance = cache = new ArchiveCache();
                    }
                }
            }
            return cache;
        }

        @CheckForNull
        Union2<Profile,String> getProfile(@NonNull final Key key) {
            final Union2<Profile,String> res = cache.get(key);
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(
                    Level.FINER,
                    "cache[{0}]->{1}",  //NOI18N
                    new Object[]{
                        key,
                        res.hasFirst() ? res.first() : res.second()
                    });
            }
            return res;
        }

        void putProfile(
            @NonNull final  Key key,
            @NonNull final Union2<Profile,String> profile) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(
                    Level.FINER,
                    "cache[{0}]<-{1}",   //NOI18N
                    new Object[]{
                        key,
                        profile.hasFirst() ? profile.first() : profile.second()
                    });
            }
            cache.put(key,profile);
        }

        @CheckForNull
        Key createKey(@NonNull final URL rootURL) {
            final URL fileURL = FileUtil.getArchiveFile(rootURL);
            if (fileURL == null) {
                //Not an archive
                return null;
            }
            final FileObject fileFo = URLMapper.findFileObject(fileURL);
            if (fileFo == null) {
                return null;
            }
            return new Key(
                fileFo.toURI(),
                fileFo.lastModified().getTime(),
                fileFo.getSize());
        }

        private static final class Key {

            private final URI root;
            private final long mtime;
            private final long size;

            Key(
                    @NonNull final URI root,
                    final long mtime,
                    final long size) {
                this.root = root;
                this.mtime = mtime;
                this.size = size;
            }

            @Override
            public int hashCode() {
                int hash = 17;
                hash = 31 * hash + (this.root != null ? this.root.hashCode() : 0);
                hash = 31 * hash + (int) (this.mtime ^ (this.mtime >>> 32));
                hash = 31 * hash + (int) (this.size ^ (this.size >>> 32));
                return hash;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (!(obj instanceof Key)) {
                    return false;
                }
                final Key other = (Key) obj;
                return this.root.equals(other.root) &&
                    this.mtime == other.mtime &&
                    this.size == other.size;

            }

            @Override
            public String toString() {
                return String.format(
                    "Key{root: %s, mtime: %d, size: %d}",   //NOI18N
                    root,
                    mtime,
                    size);
            }
        }
    }

    //@ThreadSafe
    private static final class TypeCache {

        private final Object UNKNOWN = new Object();
        private final ConcurrentMap<String,Object> cache;
        private final Archive ctSym;

        private TypeCache(@NonNull final Archive ctSym) {
            assert ctSym != null;
            this.ctSym = ctSym;
            cache = new ConcurrentHashMap<>();
        }

        @NonNull
        static TypeCache newInstance(Iterable<? extends URL> bootClassPath) {
            Archive ctSym = null;
            final CachingArchiveProvider ap = CachingArchiveProvider.getDefault();
            for (URL root : bootClassPath) {
                if (ap.hasCtSym(root)) {
                    ctSym = ap.getArchive(root, true);
                    break;
                }
            }
            if (ctSym == null) {
                throw new IllegalArgumentException(
                    String.format(
                        "No profile info for boot classpath: %s",   //NOI18N
                        bootClassPath));
            }
            return new TypeCache(ctSym);
        }

        @CheckForNull
        Profile profileForType(@NonNull final ClassName className) {
            final String binName = className.getInternalName();
            Object res = cache.get(binName);
            if (res == null) {
                res = findProfile(binName);
                cache.put(binName, res);
            }
            return res == UNKNOWN ? null : (Profile) res;
        }

        @NonNull
        private Object findProfile(@NonNull final String binaryName) {
            Object res = UNKNOWN;
            final StringBuilder sb = new StringBuilder(binaryName);
            sb.append('.'); //NOI18N
            sb.append(FileObjects.CLASS);
            try {
                final JavaFileObject jfo = ctSym.getFile(sb.toString());
                if (jfo != null) {
                    try (InputStream in = jfo.openInputStream()) {
                        final ClassFile cf = new ClassFile(in);
                        final Annotation a = cf.getAnnotation(ClassName.getClassName(ANNOTATION_PROFILE));
                        if (a == null) {
                            res = Profile.COMPACT1;
                        } else {
                            final AnnotationComponent ac = a.getComponent(ANNOTATION_VALUE);
                            res = profileFromAnnotationComponent(ac);
                        }
                    }
                }
            } catch (IOException ioe) {
                LOG.log(
                        Level.INFO,
                        "Cannot read class: {0}, reason: {1}",  //NOI18N
                        new Object[]{
                            sb,
                            ioe.getMessage()
                        });
            }
            return res;
        }

        @NonNull
        private static Profile profileFromAnnotationComponent(@NullAllowed final AnnotationComponent ac) {
            if (ac == null) {
                return Profile.COMPACT1;
            }
            try {
                final ElementValue ev = ac.getValue();
                if (!(ev instanceof PrimitiveElementValue)) {
                    return Profile.COMPACT1;
                }
                final CPEntry cpEntry = ((PrimitiveElementValue)ev).getValue();
                if (cpEntry.getTag() != 3) {
                    return Profile.COMPACT1;
                }
                final int ordinal = (Integer) cpEntry.getValue();
                if (ordinal <= 0) {
                    return Profile.COMPACT1;
                }
                final Profile[] values = Profile.values();
                if (ordinal >= values.length) {
                    return Profile.DEFAULT;
                }
                return values[ordinal-1];
            } catch (NumberFormatException nfe) {
                return Profile.COMPACT1;
            }
        }
    }

}
