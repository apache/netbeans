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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.source.classpath.AptCacheForSourceQuery;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public class AptSourceFileManager extends SourceFileManager {

    public static final String ORIGIN_FILE = "apt-origin";    //NOI18N
    public static final String ORIGIN_SOURCE_ELEMENT_URL = "apt-source-element";   //NOI18N
    public static final String ORIGIN_RESOURCE_ELEMENT_URL = "apt-resource-element";  //NOI18N

    private final ClassPath userRoots;
    private final SiblingProvider siblings;
    /**
     * Transactional support, makes files visible at the end of scanning
     */
    private final FileManagerTransaction fileTx;
    private final ModuleSourceFileManager moduleSourceFileManager;

    private Iterable<Set<Location>> cachedModuleLocations;

    public AptSourceFileManager (
            final @NonNull ClassPath userRoots,
            final @NonNull ClassPath aptRoots,
            final @NonNull SiblingProvider siblings,
            final @NonNull FileManagerTransaction fileTx,
            final @NullAllowed ModuleSourceFileManager moduleSFileManager) {
        super(aptRoots, true);
        assert userRoots != null;
        assert siblings != null;
        this.userRoots = userRoots;
        this.siblings = siblings;
        this.fileTx = fileTx;
        this.moduleSourceFileManager = moduleSFileManager;
    }

    @Override
    public Iterable<JavaFileObject> list(Location l, String packageName, Set<Kind> kinds, boolean recursive) {
        return fileTx.filter(l, packageName, super.list(l, packageName, kinds, recursive));
    }

    @Override
    public javax.tools.FileObject getFileForOutput(Location l, String pkgName, String relativeName, javax.tools.FileObject sibling)
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        URL aptRoot = getAptRoot(sibling);
        if (ModuleLocation.isInstance(l)) {
            ModuleLocation mloc = ModuleLocation.cast(l);
            l = mloc.getBaseLocation();
            if (aptRoot == null) {
                final Iterator<? extends URL> it = mloc.getModuleRoots().iterator();
                aptRoot = it.hasNext() ? it.next() : null;
            } else if (!mloc.getModuleRoots().contains(aptRoot)) {
                throw new UnsupportedOperationException("ModuleLocation's APT root differs from the sibling's APT root");
            }
        }
        final Location location = l;
        if (StandardLocation.SOURCE_OUTPUT != location) {
            throw new UnsupportedOperationException("Only apt output is supported."); // NOI18N
        }
        if (aptRoot == null) {
            throw new UnsupportedOperationException(noAptRootDebug(sibling));
        }
        final String nameStr = pkgName.length() == 0 ?
            relativeName :
            pkgName.replace('.', File.separatorChar) + File.separatorChar + relativeName;    //NOI18N
        //Always on master fs -> file is save.
        return Optional.ofNullable(URLMapper.findFileObject(aptRoot))
                .map(fo -> {
                    File f = FileUtil.toFile(fo);
                    return fileTx.createFileObject(location, new File(f, nameStr), f, null, null);
                }).get();
    }


    @Override
    public JavaFileObject getJavaFileForOutput (Location l, String className, JavaFileObject.Kind kind, javax.tools.FileObject sibling)
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        URL aptRoot = getAptRoot(sibling);
        if (ModuleLocation.isInstance(l)) {
            ModuleLocation mloc = ModuleLocation.cast(l);
            l = mloc.getBaseLocation();
            if (aptRoot == null) {
                final Iterator<? extends URL> it = mloc.getModuleRoots().iterator();
                aptRoot = it.hasNext() ? it.next() : null;
            } else if (!mloc.getModuleRoots().contains(aptRoot)) {
                throw new UnsupportedOperationException("ModuleLocation's APT root differs from the sibling's APT root");
            }
        }
        final Location location = l;
        if (StandardLocation.SOURCE_OUTPUT != location) {
            throw new UnsupportedOperationException("Only apt output is supported."); // NOI18N
        }
        if (aptRoot == null) {
            throw new UnsupportedOperationException(noAptRootDebug(sibling));
        }
        final String nameStr = className.replace('.', File.separatorChar) + kind.extension;    //NOI18N
        //Always on master fs -> file is save.
        return Optional.ofNullable(URLMapper.findFileObject(aptRoot))
                .map(fo -> {
                    File f = FileUtil.toFile(fo);
                    return fileTx.createFileObject(location, new File(f, nameStr), f, null, null);
                }).get();
    }

    @Override
    public boolean handleOption(String head, Iterator<String> tail) {
        return super.handleOption(head, tail);
    }

    @Override
    public Iterable<Set<Location>> listLocationsForModules(Location location) throws IOException {
        if (location != StandardLocation.SOURCE_OUTPUT) {
            throw new UnsupportedOperationException("Only apt output is supported."); // NOI18N
        }
        if (cachedModuleLocations == null) {
            if (moduleSourceFileManager != null) {
                final Set<URL> entriesUrl = new HashSet<>();
                sourceRoots.entries().forEach((e) -> entriesUrl.add(e.getURL()));
                cachedModuleLocations = moduleSourceFileManager.sourceModuleLocations().stream()
                        .map((ml) -> {
                            ModuleLocation oml = ModuleLocation.create(
                                    StandardLocation.SOURCE_OUTPUT,
                                    ml.getModuleRoots().stream()
                                            .map((src) -> {
                                                try {
                                                    return BaseUtilities.toURI(JavaIndex.getAptFolder(src, false)).toURL();
                                                } catch (IOException ioe) {
                                                    Exceptions.printStackTrace(ioe);
                                                    return null;
                                                }
                                            })
                                            .filter((cacheEntry) -> cacheEntry != null && entriesUrl.contains(cacheEntry))
                                            .collect(Collectors.toSet()),
                                    ml.getModuleName());
                            return oml.getModuleRoots().isEmpty() ? null : Collections.singleton((Location)oml);
                        })
                        .filter(locations -> locations != null)
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

    private URL getAptRoot (final javax.tools.FileObject sibling) {
        final URL ownerRoot = getOwnerRoot (sibling);
        if (ownerRoot == null) {
            return null;
        }
        return AptCacheForSourceQuery.getAptFolder(ownerRoot);
    }

    private URL getOwnerRoot (final javax.tools.FileObject sibling) {
        try {
            return siblings.hasSibling() ? getOwnerRootSib(siblings.getSibling()) :
                (sibling == null ? getOwnerRootNoSib() : getOwnerRootSib(sibling.toUri().toURL()));
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    private URL getOwnerRootSib (final URL sibling) throws MalformedURLException {
        assert sibling != null;
        for (ClassPath.Entry entry : userRoots.entries()) {
            final URL rootURL = entry.getURL();
            if (FileObjects.isParentOf(rootURL, sibling)) {
                return rootURL;
            }
        }
        for (ClassPath.Entry entry : sourceRoots.entries()) {
            final URL rootURL = entry.getURL();
            if (FileObjects.isParentOf(rootURL, sibling)) {
                return rootURL;
            }
        }
        return null;
    }

    private URL getOwnerRootNoSib () {
        //todo: fix me, now supports just 1 src root
        final List<ClassPath.Entry> entries = userRoots.entries();
        return entries.size() == 1 ? entries.get(0).getURL() : null;
    }

    private String noAptRootDebug(final javax.tools.FileObject sibling) {
        final StringBuilder sb = new StringBuilder("No apt root for source root: ");    //NOI18N
        sb.append(getOwnerRoot(sibling));
        sb.append(" sibling: ");    //NOI18N
        if (siblings.hasSibling()) {
            sb.append(siblings.getSibling());
        } else if (sibling != null) {
            sb.append(sibling.toUri());
        }
        else {
            sb.append("none");  //NOI18N
        }
        return sb.toString();
    }

}
