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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.NullUnknown;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Pair;

/**
 *
 * @author Dusan Balek
 */
final class ModuleSourceFileManager implements JavaFileManager {

    private final boolean ignoreExcludes;
    private final ClassPath srcPath;
    private final ClassPath moduleSrcPath;
    private final Map<URL,String> patches;
    private Set<ModuleLocation.WithExcludes> sourceModuleLocations;

    public ModuleSourceFileManager(
            @NonNull final ClassPath srcPath,
            @NonNull final ClassPath moduleSrcPath,
            final boolean ignoreExcludes) {
        assert srcPath != null;
        assert moduleSrcPath != null;
        this.srcPath = srcPath;
        this.moduleSrcPath = moduleSrcPath;
        this.ignoreExcludes = ignoreExcludes;
        this.patches = new HashMap<>();
    }

    // FileManager implementation ----------------------------------------------

    @Override
    public Iterable<JavaFileObject> list(
            @NonNull final Location l,
            @NonNull final String packageName,
            @NonNull final Set<JavaFileObject.Kind> kinds,
            final boolean recursive ) {
        final List<JavaFileObject> result = new ArrayList<> ();
        String folderName = FileObjects.convertPackage2Folder(packageName);
        if (folderName.length() > 0) {
            folderName += FileObjects.NBFS_SEPARATOR_CHAR;
        }
        final ModuleLocation.WithExcludes ml = asSourceModuleLocation(l);
        for (ClassPath.Entry entry : ml.getModuleEntries()) {
            if (ignoreExcludes || entry.includes(folderName)) {
                FileObject root = entry.getRoot();
                if (root != null) {
                    FileObject tmpFile = root.getFileObject(folderName);
                    if (tmpFile != null && tmpFile.isFolder()) {
                        Enumeration<? extends FileObject> files = tmpFile.getChildren (recursive);
                        while (files.hasMoreElements()) {
                            FileObject file = files.nextElement();
                            if (ignoreExcludes || entry.includes(file)) {
                                final JavaFileObject.Kind kind = FileObjects.getKind(file.getExt());                                
                                if (kinds.contains(kind)) {                        
                                    result.add (FileObjects.sourceFileObject(file, root));
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public javax.tools.FileObject getFileForInput(
            @NonNull final Location l,
            @NonNull final String pkgName,
            @NonNull final String relativeName ) {
        final String rp = FileObjects.resolveRelativePath (pkgName, relativeName);
        final FileObject[] fileRootPair = findFile(asSourceModuleLocation(l), rp);
        return fileRootPair == null ? null : FileObjects.sourceFileObject(fileRootPair[0], fileRootPair[1]);
    }

    @Override
    public JavaFileObject getJavaFileForInput (
            @NonNull final Location l,
            @NonNull final String className,
            @NonNull final JavaFileObject.Kind kind) {
        final String[] namePair = FileObjects.getParentRelativePathAndName (className);
        final String ext = kind == JavaFileObject.Kind.CLASS ? FileObjects.SIG : kind.extension.substring(1);   //tzezula: Clearly wrong in compile on save, but "class" is also wrong
        final ModuleLocation.WithExcludes ml = asSourceModuleLocation(l);
        for (ClassPath.Entry entry : ml.getModuleEntries()) {
            FileObject root = entry.getRoot();
            if (root != null) {
                FileObject parent = root.getFileObject(namePair[0]);
                if (parent != null) {
                    FileObject[] children = parent.getChildren();
                    for (FileObject child : children) {
                        if (namePair[1].equals(child.getName()) && ext.equalsIgnoreCase(child.getExt()) && (ignoreExcludes || entry.includes(child))) {
                            return FileObjects.sourceFileObject(child, root);
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public javax.tools.FileObject getFileForOutput(
            @NonNull final Location l,
            @NonNull final String pkgName,
            @NonNull final String relativeName,
            @NullAllowed final javax.tools.FileObject sibling ) throws IOException {
        throw new UnsupportedOperationException("Output is unsupported.");  //NOI18N
    }

    @Override
    public JavaFileObject getJavaFileForOutput( Location l, String className, JavaFileObject.Kind kind, javax.tools.FileObject sibling )
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
        if (JavacParser.OPTION_PATCH_MODULE.equals(head)) {
            final Pair<String,List<URL>> modulePatches = FileObjects.parseModulePatches(tail);
            if (modulePatches != null) {
                final String moduleName = modulePatches.first();
                final List<URL> patchURLs = modulePatches.second();
                for (URL patchURL : patchURLs) {
                    patches.put(patchURL, moduleName);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasLocation(Location location) {
        return location == StandardLocation.MODULE_SOURCE_PATH;
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
    public boolean isSameFile(javax.tools.FileObject a, javax.tools.FileObject b) {
        return
            a instanceof AbstractSourceFileObject  &&
            b instanceof AbstractSourceFileObject &&
            ((AbstractSourceFileObject)a).getHandle().file != null &&
            ((AbstractSourceFileObject)a).getHandle().file.equals(
                ((AbstractSourceFileObject)b).getHandle().file);
    }

    @Override
    @NonNull
    public Iterable<Set<Location>> listLocationsForModules(@NonNull final Location location) throws IOException {
        if (location != StandardLocation.MODULE_SOURCE_PATH) {
            throw new IllegalStateException(String.format("Unsupported location: %s", location));
        }
        return sourceModuleLocationsRemovedPatches().stream().map(
                loc -> Collections.singleton((Location)loc)
        ).collect(Collectors.toSet());
    }

    @Override
    @NullUnknown
    public String inferModuleName(@NonNull final Location location) throws IOException {
        final ModuleLocation ml = asSourceModuleLocation(location);
        return ml.getModuleName();
    }

    @Override
    @CheckForNull
    public Location getLocationForModule(Location location, JavaFileObject jfo) throws IOException {
        if (location != StandardLocation.MODULE_SOURCE_PATH) {
            throw new IllegalStateException(String.format("Unsupported location: %s", location));
        }
        final FileObject fo = URLMapper.findFileObject(jfo.toUri().toURL());
        if (fo != null) {
            for (ModuleLocation.WithExcludes moduleLocation : sourceModuleLocationsRemovedPatches()) {
                for (ClassPath.Entry moduleEntry : moduleLocation.getModuleEntries()) {
                    final FileObject root = moduleEntry.getRoot();
                    if (root != null && FileUtil.isParentOf(root, fo)) {
                        return moduleLocation;
                    }
                }
            }
        }
        return null;
    }

    @Override
    @CheckForNull
    public Location getLocationForModule(Location location, String moduleName) throws IOException {
        if (location != StandardLocation.MODULE_SOURCE_PATH) {
            throw new IllegalStateException(String.format("Unsupported location: %s", location));
        }
        for (ModuleLocation.WithExcludes moduleLocation : sourceModuleLocationsRemovedPatches()) {
            if (Objects.equals(moduleName, moduleLocation.getModuleName())) {
                return moduleLocation;
            }
        }
        return null;
    }

    private FileObject[] findFile (final ModuleLocation.WithExcludes location, final String relativePath) {
        for (ClassPath.Entry entry : location.getModuleEntries()) {
            if (ignoreExcludes || entry.includes(relativePath)) {
                FileObject root = entry.getRoot();
                if (root != null) {
                    FileObject file = root.getFileObject(relativePath);
                    if (file != null) {
                        return new FileObject[] {file, root};
                    }
                }
            }
        }
        return null;
    }

    @NonNull
    private Set<ModuleLocation.WithExcludes> sourceModuleLocationsRemovedPatches() {
        final Set<ModuleLocation.WithExcludes> all = sourceModuleLocations();
        if (patches.isEmpty()) {
            return all;
        } else {
            return all.stream()
                    .map((l) -> {
                        final Collection<? extends ClassPath.Entry> origEntries = l.getModuleEntries();
                        final List<? extends ClassPath.Entry> entries = origEntries.stream()
                                .filter((e) -> !patches.containsKey(e.getURL()))
                                .collect(Collectors.toList());
                        if (entries.isEmpty()) {
                            return null;
                        } else if (origEntries.size() == entries.size()) {
                            return l;
                        } else {
                            return ModuleLocation.WithExcludes.createExcludes(
                                    l.getBaseLocation(),
                                    entries,
                                    l.getModuleName());
                        }
                    })
                    .filter((l) -> l != null)
                    .collect(Collectors.toSet());
        }
    }

    Set<ModuleLocation.WithExcludes> sourceModuleLocations() {
        if (sourceModuleLocations == null) {
            final Map<String, List<ClassPath.Entry>> moduleRoots = new HashMap<>();
            final Set<URL> seen = new HashSet<>();
            srcPath.entries().forEach((srcEntry) -> {
                final URL srcURL = srcEntry.getURL();
                if (!seen.contains(srcURL)) {
                    final String src = srcURL.toExternalForm();
                    moduleSrcPath.entries().forEach((moduleEntry) -> {
                        final URL moduleURL = moduleEntry.getURL();
                        if (src.startsWith(moduleURL.toExternalForm())) {
                            try {
                                String relative = FileObjects.getRelativePath(moduleURL, srcURL);
                                int idx = relative.indexOf(FileObjects.NBFS_SEPARATOR_CHAR);
                                if (idx >= 0) {
                                    relative = relative.substring(0, idx);
                                }
                                List<ClassPath.Entry> roots = moduleRoots.get(relative);
                                if (roots == null) {
                                    roots = new ArrayList<>();
                                    moduleRoots.put(relative, roots);
                                }
                                roots.add(srcEntry);
                                seen.add(srcURL);
                            } catch (URISyntaxException ex) {}
                        }
                    });
                }
            });
            sourceModuleLocations = moduleRoots.entrySet().stream().map(
                    moduleRoot -> ModuleLocation.WithExcludes.createExcludes(StandardLocation.MODULE_SOURCE_PATH, moduleRoot.getValue(), moduleRoot.getKey())
            ).collect(Collectors.toSet());
        }
        return sourceModuleLocations;
    }

    @NonNull
    private static ModuleLocation.WithExcludes asSourceModuleLocation (@NonNull final Location l) {
        if (l.getClass() != ModuleLocation.WithExcludes.class) {
            throw new IllegalArgumentException (String.valueOf(l));
        }
        return (ModuleLocation.WithExcludes) l;
    }    
}
