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

package org.netbeans.modules.java.source.parsing;



import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.BaseUtilities;


/** Global cache for Archives (zip files and folders).
 *
 * XXX-Perf Add swapping for lower memory usage
 *
 * @author Petr Hrebejk
 */
public final class CachingArchiveProvider {

    private static final String NAME_RT_JAR = "rt.jar";         //NOI18N
    private static final String NAME_INNER_RT_JAR = NAME_RT_JAR + "!/"; //NOI18N
    private static final String PATH_CT_SYM = "lib/ct.sym";     //NOI18N
    private static final String PATH_RT_JAR_IN_CT_SYM = "META-INF/sym/rt.jar/"; //NOI18N
    private static final boolean USE_CT_SYM = !Boolean.getBoolean("CachingArchiveProvider.disableCtSym");   //NOI18N
    private static final Pair<Archive,URI> EMPTY = Pair.of(null, null);
    //@GuardedBy("CachingArchiveProvider.class")
    private static CachingArchiveProvider instance;

    /** Names to caching zip files.
     *
     */
    //@GuardedBy("this")
    private final Map<URI,Archive> archives;
    //@GuardedBy("this")
    private final Map<URI,URI> ctSymToJar;

    public static synchronized CachingArchiveProvider getDefault () {
        if (instance == null) {
            instance = new CachingArchiveProvider ();
        }
        return instance;
    }

    /**
     * Creates new instance of {@link CachingArchiveProvider} for unit test purposes.
     * @return new <b>unshared</b> {@link CachingArchiveProvider}
     */
    static CachingArchiveProvider newInstance() {
        return new CachingArchiveProvider();
    }
        
    /** Creates a new instance CachingArchiveProvider 
     *  Can be called only from UnitTests or {@link CachingArchiveProvider#getDefault} !!!!!
     */
    private CachingArchiveProvider() {
        archives = new HashMap<>();
        ctSymToJar = new HashMap<>();
    }
    
    /** Gets archive for given file.
     * @param root the root to get archive for
     * @param cacheFile if true the jar archive keeps the jar file opened.
     * @return new {@link Archive}
     */
    @CheckForNull
    public Archive getArchive(@NonNull final URL root, final boolean cacheFile)  {
        final URI rootURI = toURI(root);
        Archive archive;

        synchronized (this) {
            archive = archives.get(rootURI);
        }
        if (archive == null) {
            final Pair<Archive,URI> becomesArchive = create(Pair.of(root, rootURI), cacheFile);
            archive = becomesArchive.first();
            URI uriToCtSym = becomesArchive.second();
            if (archive != null) {
                synchronized (this) {
                    // optimize for no collision
                    Archive oldArchive = archives.put(rootURI, archive);
                    if (oldArchive != null) {
                        archive = oldArchive;
                        archives.put(rootURI, archive);
                    } else if (uriToCtSym != null) {
                        ctSymToJar.put(uriToCtSym, rootURI);
                    }
                }
            }
        }
        return archive;
    }

    /**
     * Frees the {@link Archive} for given root.
     * @param root the root for which the {@link Archive} should be freed.
     */
    public synchronized void removeArchive (@NonNull final URL root) {
        final URI rootURI = toURI(root);
        final Archive archive = archives.remove(rootURI);
        for (Iterator<Map.Entry<URI,URI>> it = ctSymToJar.entrySet().iterator(); it.hasNext();) {
            final Map.Entry<URI,URI> e = it.next();
            if (e.getValue().equals(rootURI)) {
                it.remove();
                break;
            }
        }
        if (archive != null) {
            archive.clear();
        }
    }

    /**
     * Clears the in memory cached {@link Archive} data for given root.
     * @param root the root for which the {@link Archive} data should be cleared.
     * @see Archive#clear()
     */
    public synchronized void clearArchive (@NonNull final URL root) {
        Archive archive = archives.get(toURI(root));
        if (archive != null) {
            archive.clear();
        }
    }

    /**
     * Maps ct.sym back to the base boot classpath root.
     * @param archiveOrCtSym the root or ct.sym folder to transform.
     * @return the boot classpath root corresponding to the ct.sym folder
     * or the given boot classpath root.
     */
    @NonNull
    public synchronized URL mapCtSymToJar (@NonNull final URL archiveOrCtSym) {
        final URI result = ctSymToJar.get(toURI(archiveOrCtSym));
        if (result != null) {
            try {
                return result.toURL();
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return archiveOrCtSym;
    }

    @NonNull
    public synchronized URL[] ctSymRootsFor(@NonNull final ClassPath cp) {
        final List<URL> res = new ArrayList<>();
        for (ClassPath.Entry entry : cp.entries()) {
            final URL root = entry.getURL();
            if (!root.getPath().endsWith(NAME_INNER_RT_JAR)) {
                continue;
            }
            try {
                for (Map.Entry<URI,URI> e : ctSymToJar.entrySet()) {
                    if (e.getValue().equals(root.toURI())) {
                        res.add(e.getKey().toURL());
                        break;
                    }
                }
            } catch (URISyntaxException | MalformedURLException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return res.toArray(new URL[0]);
    }

    /**
     * Checks if the given boot classpath root has the the ct.sym equivalent.
     * @param root the root to check
     * @return true if there is a ct.sym folder corresponding to given boot classpath
     * root
     */
    public boolean hasCtSym (@NonNull final URL root) {
        final URL fileURL = FileUtil.getArchiveFile(root);
        if (fileURL == null) {
            return false;
        }
        final File f;
        try {
            f = BaseUtilities.toFile(fileURL.toURI());
        } catch (URISyntaxException ex) {
            return false;
        }
        if (f == null || !f.exists()) {
            return false;
        }
        final Pair<File, String> res = mapJarToCtSym(f);
        return res.second() != null;

    }

    /**
     * Frees all the archives.
     */
    synchronized void clear() {
        archives.clear();
        ctSymToJar.clear();
    }
        
    // Private methods ---------------------------------------------------------
    
    /** Creates proper archive for given file.
     */
    @NonNull
    private Pair<Archive,URI> create( Pair<URL,URI> root, boolean cacheFile ) {
        String protocol = root.first().getProtocol();
        if ("file".equals(protocol)) {      //NOI18N
            File f = BaseUtilities.toFile(root.second());
            if (JavaIndex.isCacheFolder(f)) {
                return Pair.<Archive,URI>of(new CacheFolderArchive(f), null);
            } else if (f.isDirectory()) {
                return Pair.<Archive,URI>of(new FolderArchive (f), null);
            }
            else {
                return EMPTY;
            }
        }
        if ("jar".equals(protocol) &&      //NOI18N
            root.first().getPath().endsWith("!/")) { //CachingArchive does not handle paths inside the archive - skip and use FileObjectArchive      //NOI18N
            URL inner = FileUtil.getArchiveFile(root.first());
            protocol = inner.getProtocol();
            if ("file".equals(protocol)) {  //NOI18N
                File f = BaseUtilities.toFile(URI.create(inner.toExternalForm()));
                if (f.isFile()) {
                    final Pair<File,String> resolved = mapJarToCtSym(f);
                    if (resolved.second() == null) {
                        return Pair.<Archive,URI>of(
                            new CachingArchive(
                                resolved.first(),
                                resolved.second(),
                                cacheFile),
                            null);
                    } else {
                        URI uriInCtSym;
                        try {
                            uriInCtSym = toURI(resolved);
                        } catch (IllegalArgumentException e) {
                            uriInCtSym = null;
                        }
                        return Pair.<Archive,URI>of(
                            new CTSymArchive(
                                f,
                                null,
                                resolved.first(),
                                resolved.second()),
                            uriInCtSym
                        );
                    }
                } else {
                    return EMPTY;
                }
            }
        }
        final FileObject fo = URLMapper.findFileObject(root.first());
        if (fo != null) {
            final Object attr = fo.getAttribute(Path.class.getName());
            if (attr instanceof Path) {
                return Pair.<Archive,URI>of(
                        cacheFile ?
                            new CachingPathArchive((Path)attr, root.second()) :
                            new PathArchive((Path)attr, root.second()),
                        null);
            } else {
                //Slow
                return Pair.<Archive,URI>of(new FileObjectArchive (fo), null);
            }
        } else {
            return EMPTY;
        }
    }
    
    @NonNull
    private Pair<File,String> mapJarToCtSym(
        @NonNull final File file) {
        if (USE_CT_SYM && NAME_RT_JAR.equals(file.getName())) {
            final FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                for (JavaPlatform jp : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
                    for (FileObject jdkFolder : jp.getInstallFolders()) {
                        if (FileUtil.isParentOf(jdkFolder, fo)) {
                            final FileObject ctSym = jdkFolder.getFileObject(PATH_CT_SYM);
                            File ctSymFile;
                            if (ctSym != null && (ctSymFile = FileUtil.toFile(ctSym)) != null) {
                                return Pair.<File,String>of(ctSymFile,PATH_RT_JAR_IN_CT_SYM);
                            }
                        }
                    }
                }
            }
        }
        return Pair.<File,String>of(file, null);
    }

    @NonNull
    private static URI toURI (@NonNull final URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @NonNull
    private static URI toURI(@NonNull final Pair<File,String> path) {
        try {
            final URL root = FileUtil.getArchiveRoot(BaseUtilities.toURI(path.first()).toURL());
            return new URI(String.format(
                "%s%s", //NOI18N
                root.toExternalForm(),
                PATH_RT_JAR_IN_CT_SYM));
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
