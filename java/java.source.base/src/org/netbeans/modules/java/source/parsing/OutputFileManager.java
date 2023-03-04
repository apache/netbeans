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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.source.classpath.AptCacheForSourceQuery;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.BaseUtilities;

/**
 *
 * @author Tomas Zezula
 */
public class OutputFileManager extends CachingFileManager {

    private static final Logger LOG = Logger.getLogger(OutputFileManager.class.getName());
    /**
     * Exception used to signal that the sourcepath is broken (project is deleted)
     */
    public static class InvalidSourcePath extends IllegalStateException {
    }

    private final ClassPath scp;
    private final ClassPath apt;
    private final SiblingProvider siblings;
    private final FileManagerTransaction tx;
    private final ModuleSourceFileManager moduleSourceFileManager;
    private Pair<URI,File> cachedClassFolder;
    private Iterable<Set<Location>> cachedModuleLocations;

    /** Creates a new instance of CachingFileManager */
    public OutputFileManager(
            @NonNull final CachingArchiveProvider provider,
            @NonNull final ClassPath outputClassPath,
            @NonNull final ClassPath sourcePath,
            @NonNull final ClassPath aptPath,
            @NonNull final SiblingProvider siblings,
            @NonNull final FileManagerTransaction tx,
            @NullAllowed final ModuleSourceFileManager moduleSFileManager) {
        super (provider, outputClassPath, null, false, true);
        assert outputClassPath != null;
        assert sourcePath != null;
        assert siblings != null;
        assert tx != null;
	this.scp = sourcePath;
        this.apt = aptPath;
        this.siblings = siblings;
        this.tx = tx;
        this.moduleSourceFileManager = moduleSFileManager;
    }

    @Override
    public Iterable<JavaFileObject> list(Location l, String packageName, Set<Kind> kinds, boolean recursive) {
        final Iterable<JavaFileObject> sr;
        if (!ModuleLocation.isInstance(l)) {
            //List output
            sr =  super.list(l, packageName, kinds, recursive);
        } else {
            //List module
            final ModuleLocation.WithExcludes ml = ModuleLocation.WithExcludes.cast(l);
            sr = listImpl(l, ml.getModuleEntries(), packageName, kinds, recursive);
        }
        return tx.filter(l, packageName, sr);
    }

    public @Override JavaFileObject getJavaFileForOutput( Location l, String className, JavaFileObject.Kind kind, javax.tools.FileObject sibling ) 
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        if (kind != JavaFileObject.Kind.CLASS) {
            throw new IllegalArgumentException ();
        } else {
            String baseName = FileObjects.convertPackage2Folder(className);     //Todo: Use File.separatorChar and remove below baseName = ...
            File activeRoot = getClassFolderForSource(l, sibling, baseName);
            if (activeRoot == null) {
                activeRoot = getClassFolderForApt(l, sibling, baseName);
            }
            if (activeRoot == null && siblings.hasSibling()) {
                URL siblingURL = siblings.getSibling();
                activeRoot = getClassFolderForSourceImpl(siblingURL);
                if (activeRoot == null) {
                    activeRoot = getClassFolderForApt(siblingURL);
                }
            }
            if (activeRoot == null) {
                //Deleted project
                if (this.scp.getRoots().length > 0) {
                    LOG.log(
                        Level.WARNING,
                        "No output for class: {0} sibling: {1} srcRoots: {2}",    //NOI18N
                        new Object[]{
                            className,
                            sibling,
                            this.scp
                        });
                }
                throw new InvalidSourcePath ();
            }
            baseName = className.replace('.', File.separatorChar);       //NOI18N
            String nameStr = baseName + '.' + FileObjects.SIG;
            final File f = new File (activeRoot, nameStr);
            if (FileObjects.isValidFileName(className)) {
                return tx.createFileObject(l, f, activeRoot, null, null);
            } else {
                LOG.log(
                    Level.WARNING,
                    "Invalid class name: {0} sibling: {1}", //NOI18N
                    new Object[]{
                        className,
                        sibling
                    });
                return FileObjects.nullWriteFileObject(FileObjects.fileFileObject(f, activeRoot, null, null));
            }
        }
    }

    public @Override javax.tools.FileObject getFileForOutput( Location l, String pkgName, String relativeName, javax.tools.FileObject sibling )
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        assert pkgName != null;
        assert relativeName != null;
        URL siblingURL = siblings.hasSibling() ? siblings.getSibling() : sibling == null ? null : sibling.toUri().toURL();
        if (siblingURL == null) {
            throw new IllegalArgumentException ("sibling == null");
        }
        File activeRoot = getClassFolderForSourceImpl (siblingURL);
        if (activeRoot == null) {
            activeRoot = getClassFolderForApt(siblingURL);
            if (activeRoot == null) {
                //Deleted project
                throw new InvalidSourcePath ();
            }
        }
        final String path = FileObjects.resolveRelativePath(pkgName, relativeName);
        final File file = FileUtil.normalizeFile(new File (activeRoot,path.replace(FileObjects.NBFS_SEPARATOR_CHAR, File.separatorChar)));
        return tx.createFileObject(l, file, activeRoot,null,null);
    }

    @Override
    public javax.tools.FileObject getFileForInput(Location l, String pkgName, String relativeName) {
        final String[] names = FileObjects.getFolderAndBaseName(
            FileObjects.resolveRelativePath(pkgName, relativeName),
            FileObjects.NBFS_SEPARATOR_CHAR);
        javax.tools.FileObject fo = tx.readFileObject(l, names[0], names[1]);
        if (fo != null) {
            return fo;
        }
        if (!ModuleLocation.isInstance(l)) {
            //File in output
            return super.getFileForInput(l, pkgName, relativeName);
        } else {
            //File in module
            return getFileForInputImpl(
                    ModuleLocation.WithExcludes.cast(l).getModuleEntries(),
                    pkgName,
                    relativeName);
        }
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location l, String className, Kind kind) {
        if (kind == JavaFileObject.Kind.CLASS) {
            int dot = className.lastIndexOf('.');
            String dir = dot == -1 ? "" : FileObjects.convertPackage2Folder(className.substring(0, dot));
            javax.tools.FileObject fo = tx.readFileObject(l, dir, className.substring(dot + 1));
            if (fo != null) {
                return (JavaFileObject)fo;
            }
        }
        if (!ModuleLocation.isInstance(l)) {
            //File in output
            return super.getJavaFileForInput(l, className, kind);
        } else {
            //File in module
            return getJavaFileForInputImpl(
                    ModuleLocation.WithExcludes.cast(l).getModuleEntries(),
                    className,
                    kind
            );
        }
    }

    @Override
    public boolean hasLocation(Location location) {
        return location == StandardLocation.CLASS_OUTPUT;
    }

    @Override
    public Iterable<Set<Location>> listLocationsForModules(Location location) throws IOException {
        if (location != StandardLocation.CLASS_OUTPUT) {
            throw new IllegalStateException(String.format("Unsupported location: %s", location));
        }
        if (cachedModuleLocations == null) {
            if (moduleSourceFileManager != null) {
                final Map<URL,ClassPath.Entry> entriesByUrl = new HashMap<>();
                getClassPath().entries().forEach((e) -> entriesByUrl.put(e.getURL(), e));
                cachedModuleLocations = moduleSourceFileManager.sourceModuleLocations().stream()
                        .map((ml) -> {
                            Location oml = ModuleLocation.WithExcludes.createExcludes(
                                    StandardLocation.CLASS_OUTPUT,
                                    ml.getModuleRoots().stream()
                                            .map((src) -> {
                                                try {
                                                    final URL cacheRoot = BaseUtilities.toURI(JavaIndex.getClassFolder(src, false, false)).toURL();
                                                    final ClassPath.Entry cacheEntry = entriesByUrl.get(cacheRoot);
                                                    assert cacheEntry != null : String.format(
                                                            "No cache entry for cache root: %s (src root: %s), known entries: %s",  //NOI18N
                                                            cacheRoot,
                                                            src,
                                                            entriesByUrl.keySet());
                                                    return cacheEntry;
                                                } catch (IOException ioe) {
                                                    Exceptions.printStackTrace(ioe);
                                                    return null;
                                                }
                                            })
                                            .filter((cacheEntry) -> cacheEntry != null)
                                            .collect(Collectors.toSet()),
                                    ml.getModuleName());
                            return Collections.singleton(oml);
                        })
                        .collect(Collectors.toList());
            } else {
                cachedModuleLocations = Collections.emptySet();
            }
        }
        return cachedModuleLocations;
    }

    @Override
    public Location getLocationForModule(Location location, String moduleName) throws IOException {
        return StreamSupport.stream(
                listLocationsForModules(location).spliterator(),
                false)
                .flatMap((c) -> c.stream())
                .filter((l) -> moduleName.equals(ModuleLocation.cast(l).getModuleName()))
                .findAny()
                .orElse(null);
    }

    @Override
    public Location getLocationForModule(Location location, JavaFileObject fo) throws IOException {
        final URL foUrl = fo.toUri().toURL();
        for (Set<Location> s :  listLocationsForModules(location)) {
            for (Location l : s) {
                ModuleLocation ml = ModuleLocation.cast(l);
                for (URL root : ml.getModuleRoots()) {
                    if (FileObjects.isParentOf(root, foUrl)) {
                        return l;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String inferModuleName(Location location) throws IOException {
        final ModuleLocation ml = ModuleLocation.cast(location);
        return ml.getModuleName();
    }

    private File getClassFolderForSource (
            final Location l,
            final javax.tools.FileObject sibling,
            final String baseName) throws IOException {
        return sibling == null ?
                getClassFolderForSourceImpl(l, baseName) :
                getClassFolderForSourceImpl(sibling.toUri().toURL());
    }

    private File getClassFolderForSourceImpl (final URL sibling) throws IOException {
        List<ClassPath.Entry> entries = this.scp.entries();
        int eSize = entries.size();
        if ( eSize == 1) {
            return getClassFolder(entries.get(0).getURL());
        }
        if (eSize == 0) {
            return null;
        }
        try {
            for (ClassPath.Entry entry : entries) {
                URL rootUrl = entry.getURL();
                if (FileObjects.isParentOf(rootUrl, sibling)) {
                    return getClassFolder(rootUrl);
                }
            }
        } catch (IllegalArgumentException e) {
            //Logging for issue #151416
            String message = String.format("uri: %s", sibling.toString());
            throw Exceptions.attachMessage(e, message);
        }
        return null;
    }

    private File getClassFolderForSourceImpl (
            final Location l,
            final String baseName) throws IOException {
        List<ClassPath.Entry> entries = this.scp.entries();
        int eSize = entries.size();
        if (eSize == 1) {
            return getClassFolder(entries.get(0).getURL());
        }
        if (eSize == 0) {
            return null;
        }
        final String[] parentName = splitParentName(baseName);
        final Collection<? extends URL> roots = getLocationRoots(l);
        for (ClassPath.Entry entry : entries) {
            FileObject root = entry.getRoot();
            if (root != null) {
                FileObject parentFile = root.getFileObject(parentName[0]);
                if (parentFile != null) {
                    if (parentFile.getFileObject(parentName[1], FileObjects.JAVA) != null) {
                        final File cacheFolder = getClassFolder(entry.getURL());
                        if (roots.contains(BaseUtilities.toURI(cacheFolder).toURL())) {
                            return cacheFolder;
                        }
                    }
                }
            }
        }
	return null;
    }
        
    private File getClassFolderForApt(
            final Location l,
            final javax.tools.FileObject sibling,
            final String baseName) throws IOException {
        return sibling == null ?
                getClassFolderForApt(l, baseName) :
                getClassFolderForApt(sibling.toUri().toURL());
    }

    private File getClassFolderForApt(final @NonNull URL surl) {
        for (ClassPath.Entry entry : apt.entries()) {
            if (FileObjects.isParentOf(entry.getURL(), surl)) {
                final URL classFolder = AptCacheForSourceQuery.getClassFolder(entry.getURL());
                if (classFolder != null) {
                    try {
                        return BaseUtilities.toFile(classFolder.toURI());
                    } catch (URISyntaxException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        return null;
    }

    private File getClassFolderForApt(
            final Location l,
            final String baseName) {
        String[] parentName = splitParentName(baseName);
        final Collection<? extends URL> roots = getLocationRoots(l);
        for (ClassPath.Entry entry : this.apt.entries()) {
            FileObject root = entry.getRoot();
            if (root != null) {
                FileObject parentFile = root.getFileObject(parentName[0]);
                if (parentFile != null) {
                    if (parentFile.getFileObject(parentName[1], FileObjects.JAVA) != null) {
                        final URL classFolder = AptCacheForSourceQuery.getClassFolder(entry.getURL());
                        if (classFolder != null && roots.contains(classFolder)) {
                            try {
                                return BaseUtilities.toFile(classFolder.toURI());
                            } catch (URISyntaxException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
            }
        }
	return null;
    }

    private String[] splitParentName(final String baseName) {
        String name, parent = null;
	int index = baseName.lastIndexOf('/');              //NOI18N
	if (index<0) {
            parent = "";
            name = baseName;
	}
	else {
            parent = baseName.substring(0, index);
            name = baseName.substring(index+1);
	}
        index = name.indexOf('$');                          //NOI18N
	if (index > 0) {
	    name = name.substring(0,index);
	}
        return new String[] {parent, name};
    }
    
    private File getClassFolder(final URL url) throws IOException {
        final Pair<URI,File> cacheItem = cachedClassFolder;
        URI uri = null;
        try {
            uri = url.toURI();
            if (cacheItem != null && uri.equals(cacheItem.first())) {
                return cacheItem.second();
            }
        } catch (URISyntaxException e) {
            LOG.log(
                Level.FINE,
                "Not caching class folder for URL: {0}",    //NOI18N
                url);
        }
        final File result = JavaIndex.getClassFolder(url, false, false);
        assert result != null : "No class folder for source root: " + url;
        if (uri != null) {
            cachedClassFolder = Pair.<URI,File>of(
                uri,
                result);
        }
        return result;
    }

    private Collection<? extends URL> getLocationRoots(final Location l) {
        if (!ModuleLocation.isInstance(l)) {
            return getClassPath().entries().stream()
                    .map((e) -> e.getURL())
                    .collect(Collectors.toSet());
        } else {
            return ModuleLocation.cast(l).getModuleRoots();
        }
    }
}
