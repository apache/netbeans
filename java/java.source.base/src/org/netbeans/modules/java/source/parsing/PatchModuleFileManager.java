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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.util.Iterators;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 */
final class PatchModuleFileManager implements JavaFileManager {
    private static final Logger LOG = Logger.getLogger(PatchModuleFileManager.class.getName());
    private final JavaFileManager binDelegate;
    private final JavaFileManager srcDelegate;
    private final ClassPath src;
    private final Map<String,List<URL>> patches;
    private final Map<URL, String> roots;
    private Set<PatchLocation> moduleLocations;
    private String overrideModuleName;

    PatchModuleFileManager(
            @NonNull final JavaFileManager binDelegate,
            @NonNull final JavaFileManager srcDelegate,
            @NonNull ClassPath src) {
        this.binDelegate = binDelegate;
        this.srcDelegate = srcDelegate;
        this.src = src;
        this.patches = new HashMap<>();
        this.roots = new HashMap<>();
    }

    @Override
    public Location getLocationForModule(Location location, String moduleName) throws IOException {
        if (location == StandardLocation.PATCH_MODULE_PATH) {
            return moduleLocations(location).stream()
                    .filter((ml) -> moduleName != null && moduleName.equals(ml.getModuleName()))
                    .findFirst()
                    .orElse(null);
        } else if (location == StandardLocation.CLASS_OUTPUT) {
            return moduleLocations(StandardLocation.PATCH_MODULE_PATH).stream()
                    .filter((pl) -> moduleName != null && moduleName.equals(pl.getModuleName()))
                    .findFirst()
                    .map((pl) -> {
                        final List<URL> cacheRoots = pl.getSrc() == null ?
                                Collections.emptyList() :
                                pl.getSrc().getModuleRoots().stream()
                                        .map((url) -> {
                                            try {
                                                return BaseUtilities.toURI(JavaIndex.getClassFolder(url, false, false)).toURL();
                                            } catch (IOException ioe) {
                                                LOG.log(Level.WARNING, "Cannot determine the cache URL for: {0}", url); //NOI18N
                                                return null;
                                            }
                                        })
                                        .filter((url) -> url != null)
                                        .collect(Collectors.toList());
                        return cacheRoots.isEmpty() ?
                                null :
                                new PatchLocation(
                                    StandardLocation.PATCH_MODULE_PATH,
                                    cacheRoots,
                                    Collections.emptyList(),
                                    pl.getModuleName());
                    })
                    .orElse(null);
        } else {
            return null;
        }
    }

    @Override
    public Location getLocationForModule(Location location, JavaFileObject fo) throws IOException {
        if (location == StandardLocation.PATCH_MODULE_PATH) {
            final URL url = fo.toUri().toURL();
            for (Map.Entry<URL,String> root : roots.entrySet()) {
                if (FileObjects.isParentOf(root.getKey(), url)) {
                    String modName = root.getValue();
                    return moduleLocations(location).stream()
                            .filter((ml) -> modName.equals(ml.getModuleName()))
                            .findFirst()
                            .orElse(null);
                }
            }
        }
        return null;
    }

    @Override
    public Iterable<Set<Location>> listLocationsForModules(Location location) throws IOException {
        if (location == StandardLocation.PATCH_MODULE_PATH) {
            return moduleLocations(location).stream()
                    .map((ml) -> Collections.<Location>singleton(ml))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String inferModuleName(Location location) throws IOException {
        return ModuleLocation.cast(location).getModuleName();
    }

    @Override
    public int isSupportedOption(String option) {
        return -1;
    }

    @Override
    public boolean handleOption(String head, Iterator<String> tail) {
        if (JavacParser.OPTION_PATCH_MODULE.equals(head)) {
            final Pair<String,List<URL>> modulePatches = FileObjects.parseModulePatches(tail);
            if (modulePatches != null) {
                addModulePatches(modulePatches.first(), modulePatches.second());
                return true;
            }
        } else if (head.startsWith(JavacParser.NB_X_MODULE)) {
            overrideModuleName = head.substring(JavacParser.NB_X_MODULE.length());
            addModulePatches(overrideModuleName,
                             src.entries().stream().map(e -> e.getURL()).collect(Collectors.toList()));
            return true;
        }
        return false;
    }

    private void addModulePatches(final String moduleName, final List<URL> patchURLs) {
        if (patches.putIfAbsent(moduleName, patchURLs) == null) {
            for (URL url : patchURLs) {
                roots.put(url, moduleName);
            }
        } else {
            //Don't abort compilation by Abort
            //Log error into javac Logger doe not help - no source to attach to.
            LOG.log(
                    Level.WARNING,
                    "Duplicate " +JavacParser.OPTION_PATCH_MODULE+ " option, ignoring: {0}",    //NOI18N
                    patchURLs);
        }
    }

    @Override
    public boolean hasLocation(Location location) {
        return (StandardLocation.PATCH_MODULE_PATH == location || StandardLocation.CLASS_OUTPUT == location)
                && !patches.isEmpty();
    }

    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
        return binDelegate.isSameFile(a, b) || srcDelegate.isSameFile(a, b);
    }

    @Override
    public void flush() throws IOException {
        binDelegate.flush();
        srcDelegate.flush();
    }

    @Override
    public void close() throws IOException {
        binDelegate.close();
        srcDelegate.close();
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        return null;
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
        location = fixLocation(location);

        if (PatchLocation.isInstance(location)) {
            final PatchLocation pl = PatchLocation.cast(location);
            final ModuleLocation bin = pl.getBin();
            final ModuleLocation src = pl.getSrc();
            final Set<JavaFileObject.Kind> binKinds = EnumSet.copyOf(kinds);
            binKinds.remove(JavaFileObject.Kind.SOURCE);
            if (bin == null) {
                return src == null ?
                        Collections.emptyList() :
                        srcDelegate.list(src, packageName, kinds, recurse);
            } else {
                if (src == null) {
                    return binDelegate.list(bin, packageName, binKinds, recurse);
                } else {
                    final List<Iterable<JavaFileObject>> res = new ArrayList<>(2);
                    res.add(binDelegate.list(bin, packageName, binKinds, recurse));
                    res.add(srcDelegate.list(src, packageName, kinds, recurse));
                    return Iterators.chained(res);
                }
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        if (file instanceof InferableJavaFileObject) {
            return ((InferableJavaFileObject)file).inferBinaryName();
        }
        return null;
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
        location = fixLocation(location);

        if (PatchLocation.isInstance(location)) {
            final PatchLocation pl = PatchLocation.cast(location);
            final ModuleLocation bin = pl.getBin();
            final ModuleLocation src = pl.getSrc();
            if (bin != null) {
                final JavaFileObject jfo = binDelegate.getJavaFileForInput(bin, className, kind);
                if (jfo != null) {
                    return jfo;
                }
            }
            if (src != null) {
                final JavaFileObject jfo = srcDelegate.getJavaFileForInput(src, className, kind);
                if (jfo != null) {
                    return jfo;
                }
            }
        }
        return null;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        throw new UnsupportedOperationException("Not supported by patch JavaFileManager.");
    }

    @Override
    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
        throw new UnsupportedOperationException("Not supported by patch JavaFileManager.");
    }

    @Override
    public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
        throw new UnsupportedOperationException("Not supported by patch JavaFileManager.");
    }
    //</editor-fold>

    private Location fixLocation(Location input) throws IOException {
        if (input == StandardLocation.CLASS_OUTPUT && overrideModuleName != null) {
            return moduleLocations(StandardLocation.PATCH_MODULE_PATH)
                    .stream().filter(pl -> overrideModuleName.equals(pl.getModuleName())).map(pl -> (Location) pl).findAny().orElse(input);
        } else {
            return input;
        }
    }

    private Set<PatchLocation> moduleLocations(final Location baseLocation) throws IOException {
        if (baseLocation != StandardLocation.PATCH_MODULE_PATH) {
            throw new IllegalStateException(baseLocation.toString());
        }
        if (moduleLocations == null) {
            Set<PatchLocation> res = new HashSet<>();
            for (Map.Entry<String,List<URL>> patch : patches.entrySet()) {
                res.add(createPatchLocation(patch.getKey(), patch.getValue(), patch.getKey().equals(overrideModuleName)));
            }
            moduleLocations = Collections.unmodifiableSet(res);
        }
        return moduleLocations;
    }

    @NonNull
    private static PatchLocation createPatchLocation(
            @NonNull final String modName,
            @NonNull final List<? extends URL> roots,
                     final boolean sourceOverride) throws IOException {
        Collection<URL> bin = new ArrayList<>(roots.size());
        Collection<URL> src = new ArrayList<>(roots.size());
        for (URL root : roots) {
            if (JavaIndex.hasSourceCache(root, false)) {
                src.add(root);
                bin.add(FileUtil.urlForArchiveOrDir(JavaIndex.getClassFolder(root)));
            } else {
                if (sourceOverride) {
                    bin.addAll(Arrays.asList(BinaryForSourceQuery.findBinaryRoots(root).getRoots()));
                } else {
                    bin.add(root);
                }
            }
        }
        return new PatchLocation(
                StandardLocation.PATCH_MODULE_PATH,
                bin,
                src,
                modName);
    }

    private static final class PatchLocation extends ModuleLocation {

        private final ModuleLocation bin;
        private final ModuleLocation.WithExcludes src;

        PatchLocation(
                @NonNull final Location base,
                @NonNull final Collection<? extends URL> bin,
                @NonNull final Collection<? extends URL> src,
                @NonNull final String name) {
            super(
                    base,
                    name,
                    Stream.of(bin, src)
                        .flatMap((c) -> c.stream())
                        .collect(Collectors.toList()));
            this.bin = binaryLocation(name, bin);
            this.src = sourceLocation(name, src);
        }

        @CheckForNull
        ModuleLocation getBin() {
            return bin;
        }

        @CheckForNull
        ModuleLocation.WithExcludes getSrc() {
            return src;
        }

        @CheckForNull
        private static ModuleLocation binaryLocation(
                @NonNull final String name,
                @NonNull final Collection<? extends URL> roots) {
            if (roots.isEmpty()) {
                return null;
            }
            return ModuleLocation.create(
                    StandardLocation.MODULE_PATH,
                    roots,
                    name);
        }

        @CheckForNull
        private static ModuleLocation.WithExcludes sourceLocation(
                @NonNull final String name,
                @NonNull final Collection<? extends URL> roots) {
            if (roots.isEmpty()) {
                return null;
            }
            final Collection<ClassPath.Entry> moduleEntries = roots.stream()
                    .map((root) -> {
                        org.openide.filesystems.FileObject fo = URLMapper.findFileObject(root);
                        if (fo != null) {
                            ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
                            if (cp != null) {
                                for (ClassPath.Entry e : cp.entries()) {
                                    if (root.equals(e.getURL())) {
                                        return e;
                                    }
                                }
                            }
                        }
                        return ClassPathSupport.createClassPath(root).entries().get(0);
                    })
                    .collect(Collectors.toList());
            return ModuleLocation.WithExcludes.createExcludes(StandardLocation.MODULE_SOURCE_PATH, moduleEntries, name);
        }

        static boolean isInstance(Location l) {
            return l.getClass() == PatchLocation.class;
        }

        @NonNull
        static PatchLocation cast(Location l) {
            if (isInstance(l)) {
                return (PatchLocation) l;
            } else {
                throw new IllegalArgumentException (String.valueOf(l));
            }
        }
    }
}
