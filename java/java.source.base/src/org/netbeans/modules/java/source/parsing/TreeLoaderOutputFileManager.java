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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 */
final class TreeLoaderOutputFileManager implements JavaFileManager {

    private static final Logger LOG = Logger.getLogger(TreeLoaderOutputFileManager.class.getName());
    static final String OUTPUT_ROOT = "output-root";   //NOI18N
    private String outputRoot;


    private final CachingArchiveProvider provider;
    private final FileManagerTransaction tx;

    TreeLoaderOutputFileManager(
            @NonNull final CachingArchiveProvider provider,
            @NonNull final FileManagerTransaction tx) {
        assert provider != null;
        assert tx != null;
        this.provider = provider;
        this.tx = tx;
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        final Pair<Location,URL> p = baseLocation(location);
        if (!hasLocation(p.first())) {
            throw new IllegalArgumentException(String.valueOf(p.first()));
        }
        final File root = new File (outputRoot);
        assert p.second() == null || p.second().equals(BaseUtilities.toURI(root).toURL()) :
                String.format("Expected: %s, Current %s", p.second(), root);
        final String nameStr = FileObjects.convertPackage2Folder(className, File.separatorChar)  + '.' + FileObjects.SIG;
        final File file = new File (root, nameStr);
        if (FileObjects.isValidFileName(className)) {
            return tx.createFileObject(location, file, root, null, null);
        } else {
            LOG.log(
                Level.WARNING,
                "Invalid class name: {0} sibling: {1}", //NOI18N
                new Object[]{
                    className,
                    sibling
                });
            return FileObjects.nullWriteFileObject(FileObjects.fileFileObject(file, root, null, null));
        }
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
        final Pair<Location,URL> p = baseLocation(location);
        if (!hasLocation(p.first())) {
            throw new IllegalArgumentException(String.valueOf(p.first()));
        }
        final String [] names = FileObjects.getParentRelativePathAndName(className);
        if (kind == JavaFileObject.Kind.CLASS) {
            javax.tools.FileObject fo = tx.readFileObject(location, names[0], names[1]);
            if (fo != null) {
                return (JavaFileObject)fo;
            }
        }
        names[1] = names[1] + kind.extension;
        try {
            final File root = new File (outputRoot);
            assert p.second() == null || p.second().equals(BaseUtilities.toURI(root).toURL()) :
                String.format("Expected: %s, Current %s", p.second(), root);
            Archive  archive = provider.getArchive (BaseUtilities.toURI(root).toURL(), false);
            if (archive != null) {
                Iterable<JavaFileObject> files = archive.getFiles(names[0], null, null, null, false);
                for (JavaFileObject e : files) {
                    if (names[1].equals(e.getName())) {
                        return e;
                    }
                }
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }

    @Override
    public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
        final Pair<Location,URL> p = baseLocation(location);
        if (!hasLocation(p.first())) {
            throw new IllegalArgumentException(String.valueOf(p.first()));
        }
        final File root = new File(outputRoot);
        assert p.second() == null || p.second().equals(BaseUtilities.toURI(root).toURL()) :
                String.format("Expected: %s, Current %s", p.second(), root);
        final File file = FileUtil.normalizeFile(new File (
            root,
            FileObjects.resolveRelativePath(packageName, relativeName).replace(FileObjects.NBFS_SEPARATOR_CHAR, File.separatorChar)));  //NOI18N
        return tx.createFileObject(location, file, root,null,null);
    }

    @Override
    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
        final Pair<Location,URL> p = baseLocation(location);
        if (!hasLocation(p.first())) {
            throw new IllegalArgumentException(String.valueOf(p.first()));
        }
        final File root = new File(outputRoot);
        assert p.second() == null || p.second().equals(BaseUtilities.toURI(root).toURL()) :
                String.format("Expected: %s, Current %s", p.second(), root);
        final String path = FileObjects.resolveRelativePath(packageName, relativeName);
        final String[] names = FileObjects.getFolderAndBaseName(path, FileObjects.NBFS_SEPARATOR_CHAR);
        final javax.tools.FileObject jfo = tx.readFileObject(location, names[0], names[1]);
        if (jfo != null) {
            return (JavaFileObject) jfo;
        }
        final Archive archive = provider.getArchive(BaseUtilities.toURI(root).toURL(), false);
        return archive != null ?
            archive.getFile(path) :
            null;
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        if (file instanceof InferableJavaFileObject) {
            return ((InferableJavaFileObject)file).inferBinaryName();
        }
        return null;
    }

    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
        return false;
    }

    @Override
    public int isSupportedOption(String option) {
        return OUTPUT_ROOT.equals(option) ? 1 : -1;
    }

    @Override
    public boolean handleOption(String current, Iterator<String> remaining) {
        if (OUTPUT_ROOT.equals(current)) {
            if (remaining.hasNext()) {
                outputRoot = remaining.next();
                if (outputRoot.isEmpty()) {
                    outputRoot = null;
                }
            } else {
                throw new IllegalStateException("No OUTPUT_ROOT value.");   //NOI18N
            }
        }
        return false;
    }

    @Override
    public boolean hasLocation(Location location) {
        return location == StandardLocation.CLASS_OUTPUT && outputRoot != null;
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        return null;
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }

    //Modules
    @Override
    public Location getLocationForModule(Location location, String moduleName) throws IOException {
        if (!hasLocation(location)) {
            throw new IllegalArgumentException(String.valueOf(location));
        }
        final URL cacheRoot = BaseUtilities.toURI(new File(outputRoot)).toURL();
        final URL origRoot = JavaIndex.getSourceRootForClassFolder(cacheRoot);
        if (origRoot == null) {
            return null;
        }
        final String expectedModuleName = SourceUtils.getModuleName(origRoot);
        return moduleName.equals(expectedModuleName) ?
            ModuleLocation.create(
                    StandardLocation.CLASS_OUTPUT,
                    Collections.singleton(cacheRoot),
                    moduleName):
            null;
    }

    //Implementation methods
    @NonNull
    private static Pair<Location,URL> baseLocation(@NonNull final Location loc) {
        if (ModuleLocation.isInstance(loc)) {
            final ModuleLocation ml = ModuleLocation.cast(loc);
            return Pair.of(ml.getBaseLocation(), ml.getModuleRoots().iterator().next());
        } else {
            return Pair.of(loc,null);
        }
    }
}
