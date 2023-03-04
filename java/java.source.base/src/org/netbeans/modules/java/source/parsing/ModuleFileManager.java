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
package org.netbeans.modules.java.source.parsing;

import com.sun.tools.javac.code.Source;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.NullUnknown;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.java.source.base.SourceLevelUtils;
import org.netbeans.modules.java.source.util.Iterators;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
final class ModuleFileManager implements JavaFileManager {

    private static final Logger LOG = Logger.getLogger(ModuleFileManager.class.getName());

    private final CachingArchiveProvider cap;
    private final ClassPath modulePath;
    private final Function<URL,Collection<? extends URL>> peers;
    private final Source sourceLevel;
    private final boolean cacheFile;
    private final Location forLocation;
    private Set<ModuleLocation> moduleLocations;


    public ModuleFileManager(
            @NonNull final CachingArchiveProvider cap,
            @NonNull final ClassPath modulePath,
            @NonNull final Function<URL,Collection<? extends URL>> peers,
            @NullAllowed final Source sourceLevel,
            @NonNull final Location forLocation,
            final boolean cacheFile) {
        assert cap != null;
        assert modulePath != null;
        assert peers != null;
        assert forLocation != null;
        this.cap = cap;
        this.modulePath = modulePath;
        this.peers = peers;
        this.sourceLevel = sourceLevel;
        this.forLocation = forLocation;
        this.cacheFile = cacheFile;
    }

    // FileManager implementation ----------------------------------------------

    @Override
    public Iterable<JavaFileObject> list(
            @NonNull final Location l,
            @NonNull final String packageName,
            @NonNull final Set<JavaFileObject.Kind> kinds,
            final boolean recursive ) {
        final ModuleLocation ml = ModuleLocation.cast(l);
        final String folderName = FileObjects.convertPackage2Folder(packageName);
        try {
            final List<Iterable<JavaFileObject>> res = new ArrayList<>();
            List<? extends String> prefixes = null;
            final boolean supportsMultiRelease = sourceLevel != null && sourceLevel.compareTo(SourceLevelUtils.JDK1_9) >= 0;
            for (URL root : ml.getModuleRoots()) {
                final Archive archive = cap.getArchive(root, cacheFile);
                if (archive != null) {
                    final Iterable<JavaFileObject> entries;
                    // multi-release code here duplicated in CachingFileManager
                    // fixes should be ported across, or ideally this logic abstracted
                    if (supportsMultiRelease && archive.isMultiRelease()) {
                        if (prefixes == null) {
                            prefixes = multiReleaseRelocations();
                        }
                        final java.util.Map<String,JavaFileObject> fqn2f = new HashMap<>();
                        final Set<String> seenPackages = new HashSet<>();
                        for (String prefix : prefixes) {
                            Iterable<JavaFileObject> fos = archive.getFiles(
                                    join(prefix, folderName),
                                    null,
                                    kinds,
                                    null,
                                    recursive);
                            for (JavaFileObject fo : fos) {
                                final boolean base = prefix.isEmpty();
                                if (!base) {
                                    fo = new MultiReleaseJarFileObject((InferableJavaFileObject)fo, prefix);    //Always inferable in this branch
                                }
                                final String fqn = inferBinaryName(l, fo);
                                final String pkg = FileObjects.getPackageAndName(fqn)[0];
                                final String name = pkg + "/" + FileObjects.getName(fo, false);
                                if (base) {
                                    seenPackages.add(pkg);
                                    fqn2f.put(name, fo);
                                } else if (seenPackages.contains(pkg)) {
                                    fqn2f.put(name, fo);
                                }
                            }
                        }
                        entries = fqn2f.values();
                    } else {
                        entries = archive.getFiles(folderName, null, kinds, null, recursive);
                    }
                    res.add(entries);
                    if (LOG.isLoggable(Level.FINEST)) {
                        logListedFiles(l,packageName, kinds, entries);
                    }
                } else if (LOG.isLoggable(Level.FINEST)) {
                    LOG.log(
                        Level.FINEST,
                        "No archive for: {0}",               //NOI18N
                        ml.getModuleRoots());
                }
            }
            return Iterators.chained(res);
        } catch (final IOException e) {
            Exceptions.printStackTrace(e);
        }
        return Collections.emptySet();
    }

    @Override
    public FileObject getFileForInput(
            @NonNull final Location l,
            @NonNull final String pkgName,
            @NonNull final String relativeName ) {
        return findFile(ModuleLocation.cast(l), pkgName, relativeName);
    }

    @Override
    public JavaFileObject getJavaFileForInput (
            @NonNull final Location l,
            @NonNull final String className,
            @NonNull final JavaFileObject.Kind kind) {
        final ModuleLocation ml = ModuleLocation.cast(l);
        final String[] namePair = FileObjects.getParentRelativePathAndName(className);
        final boolean supportsMultiRelease = sourceLevel != null && sourceLevel.compareTo(SourceLevelUtils.JDK1_9) >= 0;
        List<? extends String> reloc = null;
        for (URL root : ml.getModuleRoots()) {
            try {
                final Archive  archive = cap.getArchive (root, cacheFile);
                if (archive != null) {
                    final List<? extends String> prefixes;
                    if (supportsMultiRelease && archive.isMultiRelease()) {
                        if (reloc == null) {
                            reloc = multiReleaseRelocations();
                        }
                        prefixes = reloc;
                    } else {
                        prefixes = Collections.singletonList("");   //NOI18N
                    }
                    for (int i = prefixes.size() - 1; i >=0; i--) {
                        final String prefix = prefixes.get(i);
                        Iterable<JavaFileObject> files = archive.getFiles(
                                join(prefix,namePair[0]),
                                null,
                                null,
                                null,
                                false);
                        for (JavaFileObject e : files) {
                            final String ename = e.getName();
                            if (namePair[1].equals(FileObjects.stripExtension(ename)) &&
                                kind == FileObjects.getKind(FileObjects.getExtension(ename))) {
                                return prefix.isEmpty() ?
                                        e :
                                        new MultiReleaseJarFileObject((InferableJavaFileObject)e, prefix);  //Always inferable
                            }
                        }
                    }
                }
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return null;
    }


    @Override
    public FileObject getFileForOutput(
            @NonNull final Location l,
            @NonNull final String pkgName,
            @NonNull final String relativeName,
            @NullAllowed final FileObject sibling ) throws IOException {
        throw new UnsupportedOperationException("Output is unsupported.");  //NOI18N
    }

    @Override
    public JavaFileObject getJavaFileForOutput( Location l, String className, JavaFileObject.Kind kind, FileObject sibling )
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        throw new UnsupportedOperationException ("Output is unsupported.");
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public int isSupportedOption(String string) {
        return -1;
    }

    @Override
    public boolean handleOption (final String head, final Iterator<String> tail) {
        return false;
    }

    @Override
    public boolean hasLocation(Location location) {
        return true;
    }

    @Override
    public ClassLoader getClassLoader (final Location l) {
        return null;
    }

    @Override
    public String inferBinaryName (Location l, JavaFileObject javaFileObject) {
        if (javaFileObject instanceof InferableJavaFileObject) {
            return ((InferableJavaFileObject)javaFileObject).inferBinaryName();
        }
        return null;
    }

    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
        return a instanceof FileObjects.FileBase
               && b instanceof FileObjects.FileBase
               && ((FileObjects.FileBase)a).getFile().equals(((FileObjects.FileBase)b).getFile());
    }

    @Override
    @NonNull
    public Iterable<Set<Location>> listLocationsForModules(Location location) throws IOException {
        return moduleLocations(location).stream()
                .map((ml) -> Collections.<Location>singleton(ml))
                .collect(Collectors.toList());
    }

    @Override
    @NullUnknown
    public String inferModuleName(@NonNull final Location location) throws IOException {
        final ModuleLocation ml = ModuleLocation.cast(location);
        return ml.getModuleName();
    }

    @Override
    @CheckForNull
    public Location getLocationForModule(Location location, JavaFileObject fo) throws IOException {
        //todo: Only for Source Module Path & Output Path
        return null;
    }

    @Override
    @CheckForNull
    public Location getLocationForModule(Location location, String moduleName) throws IOException {
        return moduleLocations(location).stream()
                .filter((ml) -> moduleName != null && moduleName.equals(ml.getModuleName()))
                .findFirst()
                .orElse(null);
    }

    private Set<ModuleLocation> moduleLocations(final Location baseLocation) {
        if (!forLocation.equals(baseLocation)) {
            throw new IllegalStateException(String.format(
                    "Locations computed for: %s, but queried for: %s",  //NOI18N
                    forLocation,
                    baseLocation));
        }
        if (moduleLocations == null) {
            final Set<ModuleLocation> moduleRoots = new HashSet<>();
            final Set<URL> seen = new HashSet<>();
            for (ClassPath.Entry e : modulePath.entries()) {
                final URL root = e.getURL();
                if (!seen.contains(root)) {
                    final String moduleName = SourceUtils.getModuleName(root);
                    if (moduleName != null) {
                        Collection<? extends URL> p = peers.apply(root);
                        moduleRoots.add(ModuleLocation.create(baseLocation, p, moduleName));
                        seen.addAll(p);
                    }
                }
            }
            moduleLocations = moduleRoots;
        }
        return moduleLocations;
    }

    private JavaFileObject findFile(
            @NonNull final ModuleLocation ml,
            @NonNull final String pkgName,
            @NonNull final String relativeName) {
        assert ml != null;
        assert pkgName != null;
        assert relativeName != null;
        final String resourceName = FileObjects.resolveRelativePath(pkgName,relativeName);
        final boolean supportsMultiRelease = sourceLevel != null && sourceLevel.compareTo(SourceLevelUtils.JDK1_9) >= 0;
        List<? extends String> reloc = null;
        for (URL root : ml.getModuleRoots()) {
            try {
                final Archive  archive = cap.getArchive (root, cacheFile);
                if (archive != null) {
                    final List<? extends String> prefixes;
                    if (supportsMultiRelease && archive.isMultiRelease()) {
                        if (reloc == null) {
                            reloc = multiReleaseRelocations();
                        }
                        prefixes = reloc;
                    } else {
                        prefixes = Collections.singletonList("");   //NOI18N
                    }
                    for (int i = prefixes.size() - 1; i >= 0; i--) {
                        final String prefix = prefixes.get(i);
                        final JavaFileObject file = archive.getFile(join(prefix, resourceName));
                        if (file != null) {
                            return prefix.isEmpty() ?
                                    file :
                                    new MultiReleaseJarFileObject((InferableJavaFileObject)file, prefix);   //Always inferable
                        }
                    }
                }
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return null;
    }

    private static void logListedFiles(
            @NonNull final Location l,
            @NonNull final String packageName,
            @NullAllowed final Set<? extends JavaFileObject.Kind> kinds,
            @NonNull final Iterable<? extends JavaFileObject> entries) {
        final StringBuilder urls = new StringBuilder ();
        for (JavaFileObject jfo : entries) {
            urls.append(jfo.toUri().toString());
            urls.append(", ");  //NOI18N
        }
        LOG.log(
            Level.FINEST,
            "Filesfor {0} package: {1} type: {2} files: [{3}]",   //NOI18N
            new Object[] {
                l,
                packageName,
                kinds,
                urls
            });
    }

    @NonNull
    private List<? extends String> multiReleaseRelocations() {
        final List<String> prefixes = new ArrayList<>();
        prefixes.add("");   //NOI18N
        final Source[] sources = Source.values();
        for (int i=0; i< sources.length; i++) {
            if (sources[i].compareTo(SourceLevelUtils.JDK1_9) >=0 && sources[i].compareTo(sourceLevel) <=0) {
                prefixes.add(String.format(
                        "META-INF/versions/%s",    //NOI18N
                        normalizeSourceLevel(sources[i].name)));
            }
        }
        return prefixes;
    }

    @NonNull
    private static String normalizeSourceLevel(@NonNull final String sl) {
        final int index = sl.indexOf('.');  //NOI18N
        return index < 0 ?
                sl :
                sl.substring(index+1);
    }

    @NonNull
    private static String join(
            @NonNull final String prefix,
            @NonNull final String path) {
        return prefix.isEmpty() ?
            path:
            path.isEmpty() ?
                prefix :
                prefix + FileObjects.NBFS_SEPARATOR_CHAR + path;
    }
}
