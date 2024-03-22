/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.source.usages;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.classfile.Access;
import org.netbeans.modules.classfile.Annotation;
import org.netbeans.modules.classfile.AnnotationComponent;
import org.netbeans.modules.classfile.ArrayElementValue;
import org.netbeans.modules.classfile.CPClassInfo;
import org.netbeans.modules.classfile.CPFieldInfo;
import org.netbeans.modules.classfile.CPInterfaceMethodInfo;
import org.netbeans.modules.classfile.CPMethodInfo;
import org.netbeans.modules.classfile.ClassElementValue;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;
import org.netbeans.modules.classfile.Code;
import org.netbeans.modules.classfile.ConstantPool;
import org.netbeans.modules.classfile.ElementValue;
import org.netbeans.modules.classfile.EnclosingMethod;
import org.netbeans.modules.classfile.EnumElementValue;
import org.netbeans.modules.classfile.Field;
import org.netbeans.modules.classfile.InnerClass;
import org.netbeans.modules.classfile.InvalidClassFormatException;
import org.netbeans.modules.classfile.LocalVariableTableEntry;
import org.netbeans.modules.classfile.LocalVariableTypeTableEntry;
import org.netbeans.modules.classfile.Method;
import org.netbeans.modules.classfile.NestedElementValue;
import org.netbeans.modules.classfile.Variable;
import org.netbeans.modules.classfile.Parameter;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.parsing.impl.indexing.SPIAccessor;
import org.netbeans.modules.parsing.impl.indexing.SuspendSupport;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.LowMemoryWatcher;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;




/**TODO: the full index does not handle invokeDynamicInfo, MethodHandleInfo and MethodTypeInfo.
 *
 * @author Petr Hrebejk, Tomas Zezula
 */
public class BinaryAnalyser {


    public static final class Changes {

        private static final Changes UP_TO_DATE = new Changes(
                true,
                Collections.<ElementHandle<TypeElement>>emptyList(),
                Collections.<ElementHandle<TypeElement>>emptyList(),
                Collections.<ElementHandle<TypeElement>>emptyList(),
                false);

        private static final Changes FAILURE = new Changes(
                false,
                Collections.<ElementHandle<TypeElement>>emptyList(),
                Collections.<ElementHandle<TypeElement>>emptyList(),
                Collections.<ElementHandle<TypeElement>>emptyList(),
                false);

        public final List<ElementHandle<TypeElement>> added;
        public final List<ElementHandle<TypeElement>> removed;
        public final List<ElementHandle<TypeElement>> changed;
        public final boolean preBuildArgs;
        public final boolean done;

        private Changes (
                final boolean done,
                final List<ElementHandle<TypeElement>> added,
                final List<ElementHandle<TypeElement>> removed,
                final List<ElementHandle<TypeElement>> changed,
                final boolean preBuildArgs) {
            this.done = done;
            this.added = added;
            this.removed = removed;
            this.changed = changed;
            this.preBuildArgs = preBuildArgs;
        }

    }

    public abstract static class Config {

        public enum UsagesLevel {
            BASIC("basic"), //NOI18N
            EXEC_VAR_REFS("refs"), //NOI18N
            ALL("all"); //NOI18N

            private static final Map<String,UsagesLevel> byName = new HashMap<>();
            static {
                for (UsagesLevel lvl : values()) {
                    byName.put(lvl.getName(), lvl);
                }
            }
            private final String name;
            UsagesLevel(@NonNull final String name) {
                this.name = name;
            }
            @NonNull
            public final String getName() {
                return this.name;
            }
            @CheckForNull
            public static UsagesLevel forName(@NullAllowed final String name) {
                return name == null ?
                    null :
                    byName.get(name);
            }
        }

        public enum IdentLevel {
            NONE ("none") { //NOI18N
                @Override
                boolean accepts(final Field f) {
                    return false;
                }
            },
            VISIBLE("exported") { //NOI18N
                @Override
                boolean accepts(final Field f) {
                    return !f.isPrivate();
                }
            },
            ALL("all") {    //NOI18N
                @Override
                boolean accepts(final Field f) {
                    return true;
                }
            };

            private static final Map<String,IdentLevel> byName = new HashMap<>();
            static {
                for (IdentLevel lvl : values()) {
                    byName.put(lvl.getName(), lvl);
                }
            }
            private final String name;
            IdentLevel(@NonNull final String name) {
                this.name = name;
            }
            @NonNull
            public final String getName() {
                return name;
            }
            @CheckForNull
            public static IdentLevel forName(@NullAllowed final String name) {
                return name == null ?
                    null :
                    byName.get(name);
            }
            abstract boolean accepts(@NonNull Field f);
        }

        @NonNull
        protected abstract UsagesLevel getUsagesLevel();

        @NonNull
        protected abstract IdentLevel getIdentLevel();

        @NonNull
        final ClassFileProcessor createProcessor(@NonNull final ClassFile cf) {
            final UsagesLevel ul = getUsagesLevel();
            switch (ul) {
                case BASIC:
                    return new ClassSignatureProcessor(cf, getIdentLevel());
                case EXEC_VAR_REFS:
                    return new ExecVarRefsProcessor(cf, getIdentLevel());
                case ALL:
                    return new FullIndexProcessor(cf, getIdentLevel());
                default:
                    throw new IllegalStateException(String.valueOf(ul));
            }
        }

        @NonNull
        static Config getDefault() {
            Config res = Lookup.getDefault().lookup(Config.class);
            if (res == null) {
                res = new DefaultConfig();
            }
            return res;
        }

        private static final class DefaultConfig extends Config {
            private static final String PROP_FULL_INDEX = "org.netbeans.modules.java.source.usages.BinaryAnalyser.fullIndex";   //NOI18N
            private static final String PROP_USG_LVL = "org.netbeans.modules.java.source.usages.BinaryAnalyser.usages"; //NOI18N
            private static final String PROP_ID_LVL = "org.netbeans.modules.java.source.usages.BinaryAnalyser.idents"; //NOI18N

            private static final UsagesLevel DEFAULT_USAGES_LEVEL = UsagesLevel.EXEC_VAR_REFS;
            private static final IdentLevel DEFAULT_IDENT_LEVEL = IdentLevel.VISIBLE;

            private final UsagesLevel usgLvl;
            private final IdentLevel idLvl;

            public DefaultConfig() {
                usgLvl = resolveUsagesLevel();
                idLvl = resolveIdentLevel();
            }

            @Override
            @NonNull
            public UsagesLevel getUsagesLevel() {
                return usgLvl;
            }

            @Override
            @NonNull
            public IdentLevel getIdentLevel() {
                return idLvl;
            }

            @NbBundle.Messages("USE_FULL_INDEX=false")
            @NonNull
            private static UsagesLevel resolveUsagesLevel() {
                UsagesLevel lvl = "true".equals(Bundle.USE_FULL_INDEX()) || Boolean.getBoolean(PROP_FULL_INDEX) ? //NOI18N
                        UsagesLevel.ALL:
                        null;
                if (lvl == null) {
                    lvl = UsagesLevel.forName(System.getProperty(PROP_USG_LVL));
                    if (lvl == null) {
                        lvl = DEFAULT_USAGES_LEVEL;
                    }
                }
                return lvl;
            }

            @NonNull
            private static IdentLevel resolveIdentLevel() {
                IdentLevel lvl = IdentLevel.forName(System.getProperty(PROP_ID_LVL));
                if (lvl == null) {
                    lvl = DEFAULT_IDENT_LEVEL;
                }
                return lvl;
            }

        }
    }

    private static final String INIT ="<init>"; //NOI18N
    private static final String CLINIT ="<clinit>"; //NOI18N
    private static final String OUTHER_THIS_PREFIX = "this$"; //NOI18N
    private static final String ACCESS_METHOD_PREFIX = "access$";   //NOI18N
    private static final String ASSERTIONS_DISABLED = "$assertionsDisabled";    //NOI18N
    private static final String ROOT = "/"; //NOI18N
    private static final String TIME_STAMPS = "timestamps.properties";   //NOI18N
    private static final String CRC = "crc.properties"; //NOI18N
    private static final Logger LOGGER = Logger.getLogger(BinaryAnalyser.class.getName());
    private static final String JCOMPONENT = javax.swing.JComponent.class.getName();
    static final String OBJECT = Object.class.getName();

    private final ClassIndexImpl.Writer writer;
    private final File cacheRoot;
    private final List<Pair<Pair<BinaryName,String>,Object[]>> refs = new ArrayList<>();
    private final Set<Pair<String,String>> toDelete = new HashSet<> ();
    private final LowMemoryWatcher lmListener;
    private final Config cfg;
    //@NotThreadSafe
    private Pair<LongHashMap<String>,Set<String>> timeStamps;

    BinaryAnalyser (final @NonNull ClassIndexImpl.Writer writer, final @NonNull File cacheRoot) {
       Parameters.notNull("writer", writer);   //NOI18N
       Parameters.notNull("cacheRoot", cacheRoot);  //NOI18N
       this.writer = writer;
       this.cacheRoot = cacheRoot;
       this.lmListener = LowMemoryWatcher.getInstance();
       this.cfg = Config.getDefault();
    }


    /** Analyses a classpath root.
     * @param scanning context
     */
    @NonNull
    public final Changes analyse (final @NonNull Context ctx) throws IOException, IllegalArgumentException  {
        return analyse(ctx, ctx.getRootURI());
    }
    
    /** Analyses a classpath root.
     * @param scanning context
     */
    @NonNull
    public final Changes analyse (final @NonNull Context ctx, URL processRoot, Predicate<ClassFile> accept) throws IOException, IllegalArgumentException  {
        return analyse(ctx, createProcessor(ctx, processRoot), accept);
    }

    @NonNull
    public final Changes analyse (final @NonNull Context ctx, URL processRoot) throws IOException, IllegalArgumentException  {
        return analyse(ctx, processRoot, null);
    }

    @NonNull
    public final Changes analyse (final @NonNull Context ctx, File root, Iterable<File> files) throws IOException, IllegalArgumentException  {
        return analyse(ctx, new EnumerateFilesProcessor(ctx, root, files), null);
    }

    @NonNull
    private Changes analyse (final @NonNull Context ctx, final @NonNull RootProcessor p, Predicate<ClassFile> accept) throws IOException, IllegalArgumentException  {
        Parameters.notNull("ctx", ctx); //NOI18N
        if (p.execute(accept)) {
            if (!p.hasChanges() && timeStampsEmpty()) {
                assert refs.isEmpty();
                assert toDelete.isEmpty();
                return Changes.UP_TO_DATE;
            }
            final List<Pair<ElementHandle<TypeElement>,Long>> newState = p.result();
            final List<Pair<ElementHandle<TypeElement>,Long>> oldState = loadCRCs(cacheRoot);
            final boolean preBuildArgs = p.preBuildArgs();
            store();
            storeCRCs(cacheRoot, newState);
            storeTimeStamps();
            return diff(oldState,newState, preBuildArgs);
        } else {
            writer.rollback();
            return Changes.FAILURE;
        }
    }

    /**
     *
     * @param url of root to be indexed
     * @return result of indexing
     * @throws IOException
     * @throws IllegalArgumentException
     * @deprecated Only used by unit tests, the start method is used by impl dep by tests of several modules, safer to keep it.
     */
    @Deprecated
    public final Changes analyse(@NonNull final URL url) throws IOException, IllegalArgumentException  {
        Context ctx = SPIAccessor.getInstance().createContext(
                FileUtil.createMemoryFileSystem().getRoot(),
                url,
                JavaIndex.NAME,
                JavaIndex.VERSION,
                null,
                false,
                false,
                false,
                SuspendSupport.NOP,
                null,
                null);
        return analyse(ctx, ctx.getRootURI());
    }

    //<editor-fold defaultstate="collapsed" desc="Private helper methods">
    @NonNull
    private RootProcessor createProcessor(@NonNull final Context ctx, URL root) throws IOException {
        final String mainP = root.getProtocol();
        if ("jar".equals(mainP)) {          //NOI18N
            final URL innerURL = FileUtil.getArchiveFile(root);
            if ("file".equals(innerURL.getProtocol())) {  //NOI18N
                //Fast way
                final File archive = BaseUtilities.toFile(URI.create(innerURL.toExternalForm()));
                if (archive.canRead()) {
                    if (!isUpToDate(ROOT,archive.lastModified())) {
                        try {
                            return new ArchiveProcessor (archive, ctx);
                        } catch (ZipException e) {
                            LOGGER.log(Level.WARNING, "Broken zip file: {0}", archive.getAbsolutePath());
                        }
                    }
                } else {
                    return new DeletedRootProcessor(ctx);
                }
            } else {
                final FileObject rootFo =  URLMapper.findFileObject(root);
                if (rootFo != null) {
                    if (!isUpToDate(ROOT,rootFo.lastModified().getTime())) {
                        final Object path = rootFo.getAttribute(Path.class.getName());
                        return (path instanceof Path) ?
                            new PathProcessor(root, (Path) path, ctx) :
                            new NBFSProcessor(rootFo, ctx);
                    }
                } else {
                    return new DeletedRootProcessor(ctx);
                }
            }
        } else if ("file".equals(mainP)) {    //NOI18N
            //Fast way
            final File rootFile = BaseUtilities.toFile(URI.create(root.toExternalForm()));
            if (rootFile.isDirectory()) {
                return new FolderProcessor(rootFile, ctx);
            } else if (!rootFile.exists()) {
                return new DeletedRootProcessor(ctx);
            }
        } else {
            final FileObject rootFo =  URLMapper.findFileObject(root);
            if (rootFo != null) {
                if (!isUpToDate(ROOT, rootFo.lastModified().getTime())) {
                    final Object path = rootFo.getAttribute(Path.class.getName());
                    return (path instanceof Path) ?
                        new PathProcessor(root, (Path) path, ctx) :
                        new NBFSProcessor(rootFo, ctx);
                }
            } else {
                return new DeletedRootProcessor(ctx);
            }
        }
        return RootProcessor.UP_TO_DATE;
    }

    private List<Pair<ElementHandle<TypeElement>,Long>> loadCRCs(final File indexFolder) throws IOException {
        List<Pair<ElementHandle<TypeElement>,Long>> result = new LinkedList<Pair<ElementHandle<TypeElement>, Long>>();
        final File file = new File (indexFolder,CRC);
        if (file.canRead()) {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));

            try {
                String line;
                while ((line=in.readLine())!=null) {
                    final String[] parts = line.split("=");    //NOI18N
                    if (parts.length == 2) {
                        try {
                            final ElementHandle<TypeElement> handle = ElementHandleAccessor.getInstance().create(ElementKind.OTHER, parts[0]);
                            final Long crc = Long.parseLong(parts[1]);
                            result.add(Pair.of(handle, crc));
                        } catch (NumberFormatException e) {
                            //Log and pass
                        }
                    }
                }
            } finally {
                in.close();
            }
        }
        return result;
    }

    private void storeCRCs(final File indexFolder, final List<Pair<ElementHandle<TypeElement>,Long>> state) throws IOException {
        final File file = new File (indexFolder,CRC);
        if (state.isEmpty()) {
            file.delete();
        } else {
            final PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
            try {
                for (Pair<ElementHandle<TypeElement>,Long> pair : state) {
                    StringBuilder sb = new StringBuilder(pair.first().getBinaryName());
                    sb.append('='); //NOI18N
                    sb.append(pair.second().longValue());
                    out.println(sb.toString());
                }
            } finally {
                out.close();
            }
        }
    }

    @NonNull
    private Pair<LongHashMap<String>,Set<String>> getTimeStamps() throws IOException {
        if (timeStamps == null) {
            final LongHashMap<String> map = new LongHashMap<String>();
            final File f = new File (cacheRoot, TIME_STAMPS); //NOI18N
            if (f.exists()) {
                final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
                try {
                    String line;
                    while (null != (line = in.readLine())) {
                        int idx = line.indexOf('='); //NOI18N
                        if (idx != -1) {
                            try {
                                long ts = Long.parseLong(line.substring(idx + 1));
                                map.put(line.substring(0, idx), ts);
                            } catch (NumberFormatException nfe) {
                                LOGGER.log(Level.FINE, "Invalid timestamp: line={0}, timestamps={1}, exception={2}", new Object[] { line, f.getPath(), nfe }); //NOI18N
                            }
                        }
                    }
                } finally {
                    in.close();
                }
            }
            timeStamps = Pair.<LongHashMap<String>,Set<String>>of(map,new HashSet<String>(map.keySet()));
        }
        return timeStamps;
    }

    private void storeTimeStamps() throws IOException {
        final File f = new File (cacheRoot, TIME_STAMPS);
        if (timeStamps == null) {
            f.delete();
        } else {
            timeStamps.first().keySet().removeAll(timeStamps.second());
            final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8));
            try {
                // write data
                for(LongHashMap.Entry<String> entry : timeStamps.first().entrySet()) {
                    out.write(entry.getKey());
                    out.write('='); //NOI18N
                    out.write(Long.toString(entry.getValue()));
                    out.newLine();
                }
                out.flush();
            } finally {
                timeStamps = null;
                out.close();
            }
        }
    }

    private boolean timeStampsEmpty() {
        return timeStamps == null || timeStamps.second().isEmpty();
    }

    private boolean isUpToDate(final String resourceName, final long timeStamp) throws IOException {
        final Pair<LongHashMap<String>,Set<String>> ts = getTimeStamps();
        long oldTime = ts.first().put(resourceName,timeStamp);
        ts.second().remove(resourceName);
        return oldTime == timeStamp;
    }

    static Changes diff (
            final List<Pair<ElementHandle<TypeElement>,Long>> oldState,
            final List<Pair<ElementHandle<TypeElement>,Long>> newState,
            final boolean preBuildArgs
            ) {
        final List<ElementHandle<TypeElement>> changed = new LinkedList<ElementHandle<TypeElement>>();
        final List<ElementHandle<TypeElement>> removed = new LinkedList<ElementHandle<TypeElement>>();
        final List<ElementHandle<TypeElement>> added = new LinkedList<ElementHandle<TypeElement>>();

        final Iterator<Pair<ElementHandle<TypeElement>,Long>> oldIt = oldState.iterator();
        final Iterator<Pair<ElementHandle<TypeElement>,Long>> newIt = newState.iterator();
        Pair<ElementHandle<TypeElement>,Long> oldE = null;
        Pair<ElementHandle<TypeElement>,Long> newE = null;
        while (oldIt.hasNext() && newIt.hasNext()) {
            if (oldE == null) {
                oldE = oldIt.next();
            }
            if (newE == null) {
                newE = newIt.next();
            }
            int ni = oldE.first().getBinaryName().compareTo(newE.first().getBinaryName());
            if (ni == 0) {
                if (oldE.second().longValue() == 0 || oldE.second().longValue() != newE.second().longValue()) {
                    changed.add(oldE.first());
                }
                oldE = newE = null;
            }
            else if (ni < 0) {
                removed.add(oldE.first());
                oldE = null;
            }
            else if (ni > 0) {
                added.add(newE.first());
                newE = null;
            }
        }
        if (oldE != null) {
            removed.add(oldE.first());
        }
        while (oldIt.hasNext()) {
            removed.add(oldIt.next().first());
        }
        if (newE != null) {
            added.add(newE.first());
        }
        while (newIt.hasNext()) {
            added.add(newIt.next().first());
        }
        return new Changes(true, added, removed, changed, preBuildArgs);
    }


    private void releaseData() {
        refs.clear();
        toDelete.clear();
    }

    private void flush() throws IOException {
        try {
            if (this.refs.size()>0 || this.toDelete.size()>0) {
                this.writer.deleteAndFlush(this.refs,this.toDelete);
            }
        } finally {
            releaseData();
        }
    }

    private void store() throws IOException {
        try {
            // do unconditionally, so pending flushed changes are committed, at least.
            this.writer.deleteAndStore(this.refs,this.toDelete);
        } finally {
            releaseData();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Class file introspection">
    private void delete (@NullAllowed final String className, @NullAllowed final String fileName) throws IOException {
        assert className != null || fileName != null;
        this.toDelete.add(Pair.<String,String>of(className, fileName));
    }

    private void analyse (final InputStream inputStream, Predicate<ClassFile> accept) throws IOException {
        final ClassFile classFile = new ClassFile(inputStream);
        if (accept != null && !accept.test(classFile)) {
            return;
        }
        final ClassFileProcessor cfp = cfg.createProcessor(classFile);
        this.delete (cfp.getClassName(), cfp.getFileName());
        final UsagesData<ClassName> usages = cfp.analyse();
        final Pair<BinaryName,String> pair = Pair.of(
                BinaryName.create(
                        cfp.getClassName(),
                        getElementKind(classFile),
                        isLocal(classFile),
                        getSimpleNameIndex(classFile)
                        ),
                cfp.getFileName());
        addReferences (pair, usages);
    }

    private void addReferences (
        @NonNull final Pair<BinaryName,String> name,
        @NonNull final UsagesData<ClassName> usages) {
        assert name != null;
        assert usages != null;
        final Object[] cr = new Object[] {
            usages.usagesToStrings(),
            usages.featureIdentsToString(),
            usages.identsToString()
        };
        this.refs.add(Pair.<Pair<BinaryName,String>,Object[]>of(name, cr));
    }

    private static ElementKind getElementKind(@NonNull final ClassFile cf) {
        if (cf.isEnum()) {
            return ElementKind.ENUM;
        } else if (cf.isAnnotation()) {
            return ElementKind.ANNOTATION_TYPE;
        } else if (cf.isModule()) {
            return ElementKind.MODULE;
        } else if ((cf.getAccess() & Access.INTERFACE) == Access.INTERFACE) {
            return ElementKind.INTERFACE;
        } else {
            return ElementKind.CLASS;
        }
    }

    private static boolean isLocal(@NonNull final ClassFile cf) {
        return cf.getEnclosingMethod() != null;
    }

    private static int getSimpleNameIndex(@NonNull final ClassFile cf) {
        if (cf.isModule()) {
            return 0;
        }
        final ClassName me = cf.getName();
        final String simpleName = getInternalSimpleName(me);
        int len = simpleName.length();
        for (InnerClass ic : cf.getInnerClasses()) {
            if (me.equals(ic.getName())) {
                final String innerName = ic.getSimpleName();
                boolean found = false;
                if (innerName != null && !innerName.isEmpty()) {
                    if (simpleName.endsWith(innerName)) {
                        len = innerName.length();
                        found = true;
                    }
                } else {
                    final EnclosingMethod enclosingMethod = cf.getEnclosingMethod();
                    if (enclosingMethod != null) {
                        final String encSimpleName = getInternalSimpleName(enclosingMethod.getClassName());
                        if (simpleName.startsWith(encSimpleName)) {
                            len -= encSimpleName.length() + 1;
                            if (len < 0) {
                                len = simpleName.length();
                            }
                            found = true;
                        }
                    }
                }
                if (!found) {
                    final int sepIndex = simpleName.lastIndexOf('$');   //NOI18N
                    if (sepIndex > 0) {
                        len -= sepIndex+1;
                    }
                }
                break;
            }
        }
        return me.getInternalName().length() - len;
    }

    private static String getInternalSimpleName(@NonNull final ClassName name) {
        final String pkg = name.getPackage();
        final String internalName = name.getInternalName();
        return pkg.isEmpty() ?
                internalName :
                internalName.substring(pkg.length()+1);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="ClassFileProcessor implementations">
    private static class ClassFileProcessor {

        private static final Convertor<ClassName,String> CONVERTOR =
                (ClassName p) -> p.getInternalName().replace('/', '.'); // NOI18N

        private final ClassFile classFile;
        private final Config.IdentLevel idLvl;
        private final String className;
        private final String fileName;
        private final UsagesData<ClassName> usages = new UsagesData<>(CONVERTOR);

        ClassFileProcessor(
                @NonNull final ClassFile classFile,
                @NonNull final Config.IdentLevel idLvl) {
            this.classFile = classFile;
            final ClassName name = classFile.getName ();
            if (classFile.isModule()) {
                this.className = classFile.getModule().getName();
                final String simpleName = name.getSimpleName();
                assert FileObjects.MODULE_INFO.equals(simpleName);
                this.fileName = String.format("%s.%s", simpleName, FileObjects.CLASS);    //NOI18N
            } else {
                this.className = CONVERTOR.convert(name);
                this.fileName = null;
            }
            this.idLvl = idLvl;
        }

        @NonNull
        final String getClassName() {
            return this.className;
        }
        
        @CheckForNull
        final String getFileName() {
            return this.fileName;
        }

        @NonNull
        final Config.IdentLevel getIdentLevel() {
            return this.idLvl;
        }

        final UsagesData analyse() {
            visit(classFile);
            return usages;
        }


        void visit(@NonNull ClassFile cf) {
            for (Method method : cf.getMethods()) {
                visit(method);
            }
            for (Variable var : cf.getVariables()) {
                visit(var);
            }
        }

        void visit(@NonNull Method m) {
        }

        void visit(@NonNull Variable v) {
        }

        final void addIdent (@NonNull final CharSequence ident) {
            assert ident != null;
            usages.addFeatureIdent(ident);
        }

        final void addUsage (
                @NonNull final ClassName name,
                @NonNull final ClassIndexImpl.UsageType usage) {
            if (OBJECT.equals(name.getExternalName())) {
                return;
            }
            usages.addUsage(name, usage);
        }

        final boolean hasUsage(@NonNull final ClassName name) {
            return usages.hasUsage(name);
        }

        final void handleAnnotations(
                @NonNull final Iterable<? extends Annotation> annotations,
                final boolean onlyTopLevel) {
            for (Annotation a : annotations) {
                addUsage(a.getType(), ClassIndexImpl.UsageType.TYPE_REFERENCE);

                if (onlyTopLevel) {
                    continue;
                }

                List<ElementValue> toProcess = new LinkedList<ElementValue>();

                for (AnnotationComponent ac : a.getComponents()) {
                    toProcess.add(ac.getValue());
                }

                while (!toProcess.isEmpty()) {
                    ElementValue ev = toProcess.remove(0);
                    if (ev instanceof ArrayElementValue) {
                        toProcess.addAll(Arrays.asList(((ArrayElementValue) ev).getValues()));
                    }
                    if (ev instanceof NestedElementValue) {
                        Annotation nested = ((NestedElementValue) ev).getNestedValue();
                        addUsage(nested.getType(), ClassIndexImpl.UsageType.TYPE_REFERENCE);
                        for (AnnotationComponent ac : nested.getComponents()) {
                            toProcess.add(ac.getValue());
                        }
                    }
                    if (ev instanceof ClassElementValue) {
                        addUsage(((ClassElementValue) ev).getClassName(), ClassIndexImpl.UsageType.TYPE_REFERENCE);
                    }
                    if (ev instanceof EnumElementValue) {
                        String type = ((EnumElementValue) ev).getEnumType();
                        ClassName className = ClassFileUtil.getType(type);
                        if (className != null) {
                            addUsage(className, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                        }
                    }
                }
            }
        }
    }

    private static class ClassSignatureProcessor extends ClassFileProcessor {

        ClassSignatureProcessor(
                @NonNull final ClassFile classFile,
                @NonNull final Config.IdentLevel idLvl) {
            super(classFile, idLvl);
        }

        @Override
        void visit(@NonNull final ClassFile cf) {
            //Add type signature of this class
            final String signature = cf.getTypeSignature();
            if (signature != null) {
                try {
                    for (ClassName typeSigName : ClassFileUtil.getTypesFromClassTypeSignature(signature)) {
                        addUsage(typeSigName, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                    }
                } catch (final RuntimeException re) {
                    final StringBuilder message = new StringBuilder ("BinaryAnalyser: Cannot read type: " + signature+" cause: " + re.getLocalizedMessage() + '\n');    //NOI18N
                    final StackTraceElement[] elements = re.getStackTrace();
                    for (StackTraceElement e : elements) {
                        message.append(e.toString());
                        message.append('\n');   //NOI18N
                    }
                    LOGGER.warning(message.toString());    //NOI18N
                }
            }

            // 0. Add the superclass
            final ClassName scName = cf.getSuperClass();
            if ( scName != null ) {
                addUsage (scName, ClassIndexImpl.UsageType.SUPER_CLASS);
            }

            // 1. Add interfaces
            Collection<ClassName> interfaces = cf.getInterfaces();
            for( ClassName ifaceName : interfaces ) {
                addUsage (ifaceName, ClassIndexImpl.UsageType.SUPER_INTERFACE);
            }

            // 3. Add top-level class annotations:
            handleAnnotations(cf.getAnnotations(), true);
            super.visit(cf);
        }

        @Override
        void visit(@NonNull final Method m) {
            final String name = m.getName();
            if (getIdentLevel().accepts(m) &&
                !m.isSynthetic() &&
                !isInit(name) &&
                !isAccessorMethod(name)) {
                addIdent(name);
            }
            super.visit(m);
        }

        @Override
        void visit(@NonNull final Variable v) {
            final String name = v.getName();
            if (getIdentLevel().accepts(v) &&
                !v.isSynthetic() &&
                !isOutherThis(name) &&
                !isDisableAssertions(name)) {
                addIdent(name);
            }
            super.visit(v);
        }

        private static boolean isInit(@NonNull final String name) {
            return INIT.equals(name) || CLINIT.equals(name);
        }

        private static boolean isOutherThis(@NonNull final String name) {
            return name.startsWith(OUTHER_THIS_PREFIX);
        }

        private static boolean isAccessorMethod(@NonNull final String name) {
            return name.startsWith(ACCESS_METHOD_PREFIX);
        }

        private static boolean isDisableAssertions(@NonNull final String name) {
            return ASSERTIONS_DISABLED.equals(name);
        }
    }

    private static final class ExecVarRefsProcessor extends ClassSignatureProcessor {
        ExecVarRefsProcessor(
                @NonNull final ClassFile classFile,
                @NonNull final Config.IdentLevel idLvl) {
            super(classFile, idLvl);
        }

        @Override
        void visit(@NonNull final ClassFile cf) {
            final ConstantPool constantPool = cf.getConstantPool();
            //2. Add field usages
            for (CPFieldInfo field : constantPool.getAllConstants(CPFieldInfo.class)) {
                ClassName name = ClassFileUtil.getType(constantPool.getClass(field.getClassID()));
                if (name != null) {
                    addUsage (name, ClassIndexImpl.UsageType.FIELD_REFERENCE);
                }
            }
            //3. Add method usages
            for (CPMethodInfo method : constantPool.getAllConstants(CPMethodInfo.class)) {
                ClassName name = ClassFileUtil.getType(constantPool.getClass(method.getClassID()));
                if (name != null) {
                    addUsage (name, ClassIndexImpl.UsageType.METHOD_REFERENCE);
                }
            }
            for (CPMethodInfo method : constantPool.getAllConstants(CPInterfaceMethodInfo.class)) {
                ClassName name = ClassFileUtil.getType(constantPool.getClass(method.getClassID()));
                if (name != null) {
                    addUsage (name, ClassIndexImpl.UsageType.METHOD_REFERENCE);
                }
            }
            super.visit(cf);
        }
    }

    private static final class FullIndexProcessor extends ClassSignatureProcessor {

        FullIndexProcessor(
                @NonNull final ClassFile classFile,
                @NonNull final Config.IdentLevel idLvl) {
            super(classFile, idLvl);
        }

        @Override
        void visit(@NonNull final ClassFile cf) {
            //1. Add class annotations: including attributes
            handleAnnotations(cf.getAnnotations(), false);
            final ConstantPool constantPool = cf.getConstantPool();
            //2. Add field usages
            for (CPFieldInfo field : constantPool.getAllConstants(CPFieldInfo.class)) {
                ClassName name = ClassFileUtil.getType(constantPool.getClass(field.getClassID()));
                if (name != null) {
                    addUsage (name, ClassIndexImpl.UsageType.FIELD_REFERENCE);
                }
            }
            //3. Add method usages
            for (CPMethodInfo method : constantPool.getAllConstants(CPMethodInfo.class)) {
                ClassName name = ClassFileUtil.getType(constantPool.getClass(method.getClassID()));
                if (name != null) {
                    addUsage (name, ClassIndexImpl.UsageType.METHOD_REFERENCE);
                }
            }
            for (CPMethodInfo method : constantPool.getAllConstants(CPInterfaceMethodInfo.class)) {
                ClassName name = ClassFileUtil.getType(constantPool.getClass(method.getClassID()));
                if (name != null) {
                    addUsage (name, ClassIndexImpl.UsageType.METHOD_REFERENCE);
                }
            }
            super.visit(cf);
            //9. Remains
            for (CPClassInfo ci : constantPool.getAllConstants(CPClassInfo.class)) {
                final ClassName ciName = ClassFileUtil.getType(ci);
                if (ciName != null && !hasUsage(ciName)) {
                    addUsage(ciName, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                }
            }
        }

        @Override
        void visit(@NonNull final Method m) {
            //4, 5, 6, 8 Add method type refs (return types, param types, exception types) and local variables.
            handleAnnotations(m.getAnnotations(), false);
            String jvmTypeId = m.getReturnType();
            ClassName type = ClassFileUtil.getType (jvmTypeId);
            if (type != null) {
                addUsage(type, ClassIndexImpl.UsageType.TYPE_REFERENCE);
            }
            List<Parameter> params =  m.getParameters();
            for (Parameter param : params) {
                jvmTypeId = param.getDescriptor();
                type = ClassFileUtil.getType (jvmTypeId);
                if (type != null) {
                    addUsage(type, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                }
            }
            CPClassInfo[] classInfos = m.getExceptionClasses();
            for (CPClassInfo classInfo : classInfos) {
                type = classInfo.getClassName();
                if (type != null) {
                    addUsage(type, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                }
            }
            jvmTypeId = m.getTypeSignature();
            if (jvmTypeId != null) {
                try {
                    ClassName[] typeSigNames = ClassFileUtil.getTypesFromMethodTypeSignature (jvmTypeId);
                    for (ClassName typeSigName : typeSigNames) {
                        addUsage(typeSigName, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                    }
                } catch (IllegalStateException is) {
                    LOGGER.log(Level.WARNING, "Invalid method signature: {0}::{1} signature is:{2}",    // NOI18N
                            new Object[] {
                                getClassName(),
                                m.getName(),
                                jvmTypeId});
                }
            }
            Code code = m.getCode();
            if (code != null) {
                LocalVariableTableEntry[] vars = code.getLocalVariableTable();
                for (LocalVariableTableEntry var : vars) {
                    type = ClassFileUtil.getType (var.getDescription());
                    if (type != null) {
                        addUsage(type, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                    }
                }
                LocalVariableTypeTableEntry[] varTypes = m.getCode().getLocalVariableTypeTable();
                for (LocalVariableTypeTableEntry varType : varTypes) {
                    try {
                        ClassName[] typeSigNames = ClassFileUtil.getTypesFromFiledTypeSignature (varType.getSignature());
                        for (ClassName typeSigName : typeSigNames) {
                            addUsage(typeSigName, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                        }
                    } catch (IllegalStateException is) {
                        LOGGER.log(Level.WARNING, "Invalid local variable signature: {0}::{1}", // NOI18N
                                new Object[]{
                                    getClassName(),
                                    m.getName()});
                    }
                }
            }
            super.visit(m);
        }

        @Override
        void visit(Variable v) {
            //7. Add Filed Type References
            handleAnnotations(v.getAnnotations(), false);
            String jvmTypeId = v.getDescriptor();
            ClassName type = ClassFileUtil.getType (jvmTypeId);
            if (type != null) {
                addUsage (type, ClassIndexImpl.UsageType.TYPE_REFERENCE);
            }
            jvmTypeId = v.getTypeSignature();
            if (jvmTypeId != null) {
                try {
                    ClassName[] typeSigNames = ClassFileUtil.getTypesFromFiledTypeSignature (jvmTypeId);
                    for (ClassName typeSigName : typeSigNames) {
                        addUsage(typeSigName, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                    }
                } catch (IllegalStateException is) {
                    LOGGER.log(Level.WARNING, "Invalid field signature: {0}::{1} signature is: {2}",
                            new Object[]{
                                getClassName(),
                                v.getName(),
                                jvmTypeId});  // NOI18N
                }
            }
            super.visit(v);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="RootProcessor implementations">
    private abstract static class RootProcessor {
        private static final Comparator<Pair<ElementHandle<TypeElement>,Long>> COMPARATOR = new Comparator<Pair<ElementHandle<TypeElement>,Long>>() {
                @Override
                public int compare(
                        final Pair<ElementHandle<TypeElement>,Long> o1,
                        final Pair<ElementHandle<TypeElement>,Long> o2) {
                    return o1.first().getBinaryName().compareTo(o2.first().getBinaryName());
                }
            };

        static final RootProcessor UP_TO_DATE = new RootProcessor() {
            @Override
            @NonNull
            protected boolean executeImpl(Predicate<ClassFile> accept) throws IOException {
                return true;
            }
        };

        private final List<Pair<ElementHandle<TypeElement>,Long>> result;
        private final Context ctx;
        private boolean changed;
        private byte preBuildArgsState;

        RootProcessor(@NonNull final Context ctx) {
            assert ctx != null;
            this.ctx = ctx;
            this.result  = new ArrayList<>();
        }

        private RootProcessor() {
            this.ctx = null;
            this.result = Collections.emptyList();
        }

        @NonNull
        protected final boolean execute(Predicate<ClassFile> accept) throws IOException {
            final boolean res = executeImpl(accept);
            if (res) {
                result.sort(COMPARATOR);
            }
            return res;
        }

        protected final boolean hasChanges() {
            return changed;
        }

        protected final boolean preBuildArgs() {
            return preBuildArgsState == 3;
        }

        @NonNull
        protected final List<Pair<ElementHandle<TypeElement>,Long>> result() {
            return result;
        }

        protected final void report (final ElementHandle<TypeElement> te, final long crc) {
            this.result.add(Pair.of(te, crc));
            //On the IBM VMs the swing is in separate jar (graphics.jar) where no j.l package exists, don't prebuild such an archive.
            //The param names will be created on deamand
            final String binName = te.getBinaryName();
            if (OBJECT.equals(binName)) {
                preBuildArgsState|=1;
            } else if (JCOMPONENT.equals(binName)) {
                preBuildArgsState|=2;
            }
        }

        protected final void markChanged() {
            this.changed = true;
        }

        protected final boolean isCancelled() {
            return ctx.isCancelled();
        }

        protected final boolean accepts(String name) {
            int index = name.lastIndexOf('.');  //NOI18N
            if (index == -1 || (index+1) == name.length()) {
                return false;
            }
            return FileObjects.CLASS.equalsIgnoreCase(name.substring(index+1));
        }

        @NonNull
        protected abstract boolean executeImpl(Predicate<ClassFile> accept) throws IOException;
    }

    private final class ArchiveProcessor extends RootProcessor {

        private final ZipFile zipFile;
        private final Enumeration<? extends ZipEntry> entries;
        private boolean brokenLogged;

        ArchiveProcessor (
                final @NonNull File file,
                final @NonNull Context ctx) throws IOException {
            super(ctx);
            assert file != null;
            writer.clear();
            this.zipFile = new ZipFile(file);
            this.entries = zipFile.entries();
            markChanged();  //Always dirty, created only for dirty root
        }


        @Override
        @NonNull
        protected boolean executeImpl(Predicate<ClassFile> accept) throws IOException {
            try {
                while(entries.hasMoreElements()) {
                    final ZipEntry ze;
                    try {
                        ze = entries.nextElement();
                    } catch (InternalError err) {
                        LOGGER.log(
                                Level.INFO,
                                "Broken zip file: {0}, reason: {1}",    //NOI18N
                                new Object[] {
                                    zipFile.getName(),
                                    err.getMessage()
                                });
                        return true;
                    } catch (RuntimeException re) {
                        if (re instanceof NoSuchElementException) {
                            //Valid for Enumeration.nextElement
                            throw (NoSuchElementException) re;
                        }
                        if (!brokenLogged) {
                            LOGGER.log(
                                    Level.INFO,
                                    "Broken zip file: {0}, reason: {1}",    //NOI18N
                                    new Object[]{
                                        zipFile.getName(),
                                        re.getMessage()
                                    });
                            brokenLogged = true;
                        }
                        continue;
                    }
                    if (!ze.isDirectory()  && accepts(ze.getName()))  {
                        report (
                            ElementHandleAccessor.getInstance().create(ElementKind.OTHER, FileObjects.convertFolder2Package(FileObjects.stripExtension(ze.getName()))),
                            ze.getCrc());
                        final InputStream in = new BufferedInputStream (zipFile.getInputStream( ze ));
                        try {
                            analyse(in, accept);
                        } catch (InvalidClassFormatException | RuntimeException icf) {
                            LOGGER.log(
                                    Level.WARNING,
                                    "Invalid class file format: {0}!/{1}",      //NOI18N
                                    new Object[]{
                                        BaseUtilities.toURI(new File(zipFile.getName())),
                                        ze.getName()});
                            LOGGER.log(
                                    Level.INFO,
                                    "Class File Exception Details",             //NOI18N
                                    icf);
                        } catch (IOException x) {
                            Exceptions.attachMessage(x, "While scanning: " + ze.getName());    //NOI18N
                            throw x;
                        }
                        finally {
                            in.close();
                        }
                        if (lmListener.isLowMemory()) {
                            flush();
                        }
                    }
                    if (isCancelled()) {
                        return false;
                    }
                }
                return true;
            } finally {
                zipFile.close();
            }
        }
    }

    private final class FolderProcessor extends RootProcessor {
        private final LinkedList<File> todo;
        private final String rootPath;

        public FolderProcessor(
                final @NonNull File root,
                final @NonNull Context ctx) throws IOException {
            super(ctx);
            assert root != null;
            String path = root.getAbsolutePath ();
            if (path.charAt(path.length()-1) != File.separatorChar) {
                path = path + File.separatorChar;
            }
            this.todo = new LinkedList<File> ();
            this.rootPath = path;
            final File[] children = root.listFiles();
            if (children != null) {
                Collections.addAll (todo, children);
            }
        }

        @Override
        @NonNull
        protected boolean executeImpl(Predicate<ClassFile> accept) throws IOException {
            while (!todo.isEmpty()) {
                File file = todo.removeFirst();
                if (file.isDirectory()) {
                    File[] c = file.listFiles();
                    if (c!= null) {
                        Collections.addAll(todo, c);
                    }
                } else if (accepts(file.getName())) {
                    String filePath = file.getAbsolutePath();
                    long fileMTime = file.lastModified();
                    int dotIndex = filePath.lastIndexOf('.');
                    int slashIndex = filePath.lastIndexOf(File.separatorChar);
                    int endPos;
                    if (dotIndex>slashIndex) {
                        endPos = dotIndex;
                    } else {
                        endPos = filePath.length();
                    }
                    String relativePath = FileObjects.convertFolder2Package (filePath.substring(rootPath.length(), endPos), File.separatorChar);
                    report(
                        ElementHandleAccessor.getInstance().create(ElementKind.OTHER, relativePath),
                        fileMTime);
                    if (!isUpToDate (relativePath, fileMTime)) {
                        markChanged();
                        toDelete.add(Pair.<String,String>of (relativePath,null));
                        try {
                            InputStream in = new BufferedInputStream(new FileInputStream(file));
                            try {
                                analyse(in, accept);
                            } catch (InvalidClassFormatException | RuntimeException icf) {
                                LOGGER.log(
                                    Level.WARNING,
                                    "Invalid class file format: {0}",      //NOI18N
                                    file.getAbsolutePath());
                                    LOGGER.log(
                                        Level.INFO,
                                        "Class File Exception Details",             //NOI18N
                                        icf);
                            } finally {
                                in.close();
                            }
                        } catch (IOException ex) {
                            //unreadable file?
                            LOGGER.log(Level.WARNING, "Cannot read file: {0}", file.getAbsolutePath());      //NOI18N
                            LOGGER.log(Level.FINE, null, ex);
                        }
                        if (lmListener.isLowMemory()) {
                            flush();
                        }
                    }
                }
                if (isCancelled()) {
                    return false;
                }
            }
            for (String deleted : getTimeStamps().second()) {
                if (FileObjects.MODULE_INFO.equals(deleted)) {
                    delete(null,String.format("%s.%s", FileObjects.MODULE_INFO, FileObjects.CLASS));  //NOI18N
                } else {
                    delete(deleted, null);  //TODO
                }
                markChanged();
            }
            return true;
        }
    }

    private final class EnumerateFilesProcessor extends RootProcessor {
        private final File todoRoot;
        private final Iterable<File> todo;

        public EnumerateFilesProcessor(
                final @NonNull Context ctx,
                final @NonNull File todoRoot,
                final @NonNull Iterable<File> todo) throws IOException {
            super(ctx);
            this.todoRoot = todoRoot;
            this.todo = todo;
        }

        @Override
        @NonNull
        protected boolean executeImpl(Predicate<ClassFile> accept) throws IOException {
            for (File file : todo) {
                long fileMTime = file.lastModified();
                String relativePath = FileObjects.convertFolder2Package(FileObjects.getRelativePath(todoRoot, file), File.separatorChar);
                report(
                    ElementHandleAccessor.getInstance().create(ElementKind.OTHER, relativePath),
                    fileMTime);
                if (!isUpToDate (relativePath, fileMTime)) {
                    markChanged();
                    toDelete.add(Pair.<String,String>of (relativePath,null));
                    try {
                        InputStream in = new BufferedInputStream(new FileInputStream(file));
                        try {
                            analyse(in, accept);
                        } catch (InvalidClassFormatException | RuntimeException icf) {
                            LOGGER.log(
                                Level.WARNING,
                                "Invalid class file format: {0}",      //NOI18N
                                file.getAbsolutePath());
                                LOGGER.log(
                                    Level.INFO,
                                    "Class File Exception Details",             //NOI18N
                                    icf);
                        } finally {
                            in.close();
                        }
                    } catch (IOException ex) {
                        //unreadable file?
                        LOGGER.log(Level.WARNING, "Cannot read file: {0}", file.getAbsolutePath());      //NOI18N
                        LOGGER.log(Level.FINE, null, ex);
                    }
                    if (lmListener.isLowMemory()) {
                        flush();
                    }
                }
                if (isCancelled()) {
                    return false;
                }
            }
            for (String deleted : getTimeStamps().second()) {
                if (FileObjects.MODULE_INFO.equals(deleted)) {
                    delete(null,String.format("%s.%s", FileObjects.MODULE_INFO, FileObjects.CLASS));  //NOI18N
                } else {
                    delete(deleted, null);  //TODO
                }
                markChanged();
            }
            return true;
        }
    }

    private final class PathProcessor extends RootProcessor {

        private final URL rootURL;
        private final Path rootPath;

        PathProcessor(
            @NonNull final URL rootURL,
            @NonNull final Path rootPath,
            @NonNull final Context ctx) {
            super(ctx);
            assert rootURL != null;
            assert rootPath != null;
            this.rootURL = rootURL;
            this.rootPath = rootPath;
            markChanged();  //Always dirty, created only for dirty root
        }

        @Override
        protected boolean executeImpl(Predicate<ClassFile> accept) throws IOException {
            final boolean[] cancelled = new boolean[1];
            final char separator = rootPath.getFileSystem().getSeparator().charAt(0);
            Files.walkFileTree(rootPath, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    final String simpleName = file.getName(file.getNameCount()-1).toString();
                    if (accepts(simpleName)) {
                        final String fqn = FileObjects.convertFolder2Package (
                            FileObjects.stripExtension(rootPath.relativize(file).toString()),
                            separator);
                        report (
                            ElementHandleAccessor.getInstance().create(
                                ElementKind.OTHER,
                                fqn),
                            attrs.lastModifiedTime().toMillis());
                        try (final InputStream in = new BufferedInputStream (Files.newInputStream(file, StandardOpenOption.READ))) {
                            analyse(in, accept);
                        } catch (InvalidClassFormatException | RuntimeException icf) {
                            LOGGER.log(
                                    Level.WARNING,
                                    "Invalid class file format: {0} in: {1}",      //NOI18N
                                    new Object[]{
                                        file,
                                        rootURL,
                                        });
                            LOGGER.log(
                                    Level.INFO,
                                    "Class File Exception Details",             //NOI18N
                                    icf);
                        } catch (IOException x) {
                            Exceptions.attachMessage(
                                x,
                                String.format(
                                    "While scanning: %s in: %s",        //NOI18N
                                    file,
                                    rootURL));
                            throw x;
                        }
                        if (lmListener.isLowMemory()) {
                            flush();
                        }
                    }
                    return handleCancel();
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    LOGGER.log(
                        Level.WARNING,
                        "Cannot read file: {0}",      //NOI18N
                        file);
                    LOGGER.log(Level.FINE, null, exc);
                    return handleCancel();
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                private FileVisitResult handleCancel() {
                    if (isCancelled()) {
                        cancelled[0] = true;
                        return FileVisitResult.TERMINATE;
                    } else {
                        return FileVisitResult.CONTINUE;
                    }
                }
            });
            return !cancelled[0];
        }

    }

    private final class NBFSProcessor extends RootProcessor {

        private final Enumeration<? extends FileObject> todo;
        private final FileObject root;

        NBFSProcessor(
                final @NonNull FileObject root,
                final @NonNull Context ctx) throws IOException {
            super(ctx);
            assert root != null;
            writer.clear();
            this.root = root;
            this.todo = root.getData(true);
            markChanged();  //Always dirty, created only for dirty root
        }

        @Override
        @NonNull
        protected boolean executeImpl(Predicate<ClassFile> accept) throws IOException {
            while (todo.hasMoreElements()) {
                FileObject fo = todo.nextElement();
                if (accepts(fo.getNameExt())) {
                    final String rp = FileObjects.stripExtension(FileUtil.getRelativePath(root, fo));
                    report(
                        ElementHandleAccessor.getInstance().create(ElementKind.OTHER, FileObjects.convertFolder2Package(rp)),
                        0L);
                    final InputStream in = new BufferedInputStream (fo.getInputStream());
                    try {
                        analyse (in, accept);
                    } catch (InvalidClassFormatException icf) {
                        LOGGER.log(Level.WARNING, "Invalid class file format: {0}", FileUtil.getFileDisplayName(fo));      //NOI18N
                    }
                    finally {
                        in.close();
                    }
                    if (lmListener.isLowMemory()) {
                        flush();
                    }
                }
                if (isCancelled()) {
                    return false;
                }
            }
            return true;
        }
    }

    private final class DeletedRootProcessor extends RootProcessor {

        DeletedRootProcessor(@NonNull final Context ctx) throws IOException {
            super(ctx);
            final Pair<LongHashMap<String>, Set<String>> ts = getTimeStamps();
            if (!ts.first().isEmpty()) {
                markChanged();
            }
        }

        @Override
        @NonNull
        protected boolean executeImpl(Predicate<ClassFile> accept) throws IOException {
            if (hasChanges()) {
                writer.clear();
            }
            return true;
        }
    }
    //</editor-fold>
}
