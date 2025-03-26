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

package org.netbeans.core.startup.layers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;
import org.openide.util.BaseUtilities;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=URLMapper.class)
public class ArchiveURLMapper extends URLMapper {
    private static final Logger LOG = Logger.getLogger(ArchiveURLMapper.class.getName());

    private static final String JAR_PROTOCOL = "jar";   //NOI18N

    private static final Map<File,SoftReference<JarFileSystem>> mountRoots = new ConcurrentHashMap<File,SoftReference<JarFileSystem>>();

    public @Override URL getURL(FileObject fo, int type) {
        assert fo != null;
        if (type == URLMapper.EXTERNAL || type == URLMapper.INTERNAL) {
            if (fo.isValid()) {
                File archiveFile = null;
                try {
                    FileSystem fs = fo.getFileSystem();
                    if (fs instanceof JarFileSystem) {
                        JarFileSystem jfs = (JarFileSystem) fs;
                        archiveFile = jfs.getJarFile();
                        if (isRoot(archiveFile)) {
                            try {
                                return new URL("jar:" + BaseUtilities.toURI(archiveFile) + "!/" +
                                    new URI(null, fo.getPath(), null).getRawSchemeSpecificPart() +
                                    (fo.isFolder() && !fo.isRoot() ? "/" : "")); // NOI18N
                            } catch (URISyntaxException syntax) {
                                final String path = new URI(null, fo.getPath(), null).toString();
                                return new URL("jar:" + BaseUtilities.toURI(archiveFile) + "!/" + path
                                        + ((fo.isFolder() && !fo.isRoot()) ? "/" : "")); // NOI18N
                            }
                        }
                    }
                } catch (/*IO,URISyntax*/Exception e) {
                    LOG.log(Level.INFO, "fo: " + fo + " archiveFile: " + archiveFile, e);
                }
            }
        }
        return null;
    }

    public @Override FileObject[] getFileObjects(URL url) {
        assert url != null;
        String protocol  = url.getProtocol ();
        if (JAR_PROTOCOL.equals (protocol)) {
            String path = url.getPath();
            int index = path.lastIndexOf ('!');
            if (index>=0) {
                try {
                    URI archiveFileURI = new URI(path.substring(0,index));
                    URL archiveFileURL;
                    try {
                        archiveFileURL = archiveFileURI.toURL();
                    } catch (IllegalArgumentException x) {
                        LOG.log(Level.INFO, "checking " + archiveFileURI, x);
                        return null;
                    }
                    FileObject fo = URLMapper.findFileObject (archiveFileURL);
                    if (fo == null || fo.isVirtual()) {
                        return null;
                    }
                    File archiveFile = FileUtil.toFile (fo);
                    if (archiveFile == null) {
                        archiveFile = copyJAR(fo, archiveFileURI, false);
                    }
                    // XXX new URI("substring").getPath() might be better?
                    String offset = path.length()>index+2 ? URLDecoder.decode(path.substring(index+2),"UTF-8"): "";   //NOI18N
                    JarFileSystem fs = getFileSystem(archiveFile);
                    FileObject resource = fs.findResource(offset);
                    if (resource != null) {
                        return new FileObject[] {resource};
                    }
                } catch (IOException e) {                    
                    LOG.log(Level.INFO, "checking " + url, e);
                } catch (URISyntaxException e) {
                    LOG.log(Level.INFO, "Can't get fo for " + url, e);
                }
            }
        }
        return null;
    }

    /** #177052 - not necessary to be synchronized. */
    private static boolean isRoot (File file) {
        return mountRoots.containsKey(file);
    }

    private static JarFileSystem getFileSystem (File file) throws IOException {
        synchronized (mountRoots) {
            Reference<JarFileSystem> reference = mountRoots.get(file);
            JarFileSystem jfs = null;
            if (reference == null || (jfs = reference.get()) == null) {
                jfs = findJarFileSystemInRepository(file);
                if (jfs == null) {
                    File aRoot = FileUtil.normalizeFile(file);
                    jfs = new JarFileSystem(aRoot);
                }
                mountRoots.put(file, new JFSReference(jfs));
            }
            return jfs;
        }
    }

    // More or less copied from URLMapper:
    private static JarFileSystem findJarFileSystemInRepository(File jarFile) {
        @SuppressWarnings("deprecation") // for compat only
        Enumeration<? extends FileSystem> en = Repository.getDefault().getFileSystems();
        while (en.hasMoreElements()) {
            FileSystem fs = en.nextElement();
            if (fs instanceof JarFileSystem) {
                JarFileSystem jfs = (JarFileSystem)fs;
                if (jarFile.equals(jfs.getJarFile())) {
                    return jfs;
                }
            }
        }
        return null;
    }

    /**
     * After deleting and recreating of jar file there must be properly
     * refreshed cached map "mountRoots". 
     */ 
    private static class JFSReference extends SoftReference<JarFileSystem> {
        private FileChangeListener fcl;

        public JFSReference(JarFileSystem jfs) throws IOException {
            super(jfs);
            final File root = jfs.getJarFile();
            URI nestedRootURI = null;
            FileObject rootFo = null;
            synchronized (copiedJARs) {
                if (copiedJARs.containsValue(root)) {
                    // nested jar
                    for (Map.Entry<URI, File> entry : copiedJARs.entrySet()) {
                        if (entry.getValue().equals(root)) {
                            nestedRootURI = entry.getKey();
                            rootFo = URLMapper.findFileObject(nestedRootURI.toURL());
                        }
                    }
                } else {
                    // regular jar
                    rootFo = FileUtil.toFileObject(root);
                }
            }
            final URI nestedRootURIFinal = nestedRootURI;
            if (rootFo != null) {
                fcl = new FileChangeAdapter() {
                    public @Override void fileDeleted(FileEvent fe) {
                        releaseMe(root);
                    }
                    public @Override void fileRenamed(FileRenameEvent fe) {
                        releaseMe(root);
                    }

                    @Override
                    public void fileChanged(FileEvent fe) {
                        if (nestedRootURIFinal != null) {
                            try {
                                // update copy of nested jar and re-register root
                                copyJAR(fe.getFile(), nestedRootURIFinal, true);
                                releaseMe(root);
                                // and register again
                                getFileSystem(root);
                            } catch (IOException e) {
                                LOG.log(Level.INFO, "Can't copy JAR " + fe.getFile() + " to " + nestedRootURIFinal, e);
                            }
                        }
                    }
                };
                rootFo.addFileChangeListener(FileUtil.weakFileChangeListener(fcl, rootFo));
            }
        }
        
        void releaseMe (final File root) {
            JarFileSystem jfs = get();
            if (jfs != null) {
                synchronized (mountRoots) {
                    File keyToRemove = (root != null) ? root : jfs.getJarFile();
                    mountRoots.remove(keyToRemove);                    
                }
            }
        }
    }

    private static final Map<URI,File> copiedJARs = new HashMap<URI,File>();
    private static File copyJAR(FileObject fo, URI archiveFileURI, boolean replace) throws IOException {
        synchronized (copiedJARs) {
            File copy = copiedJARs.get(archiveFileURI);
            if (copy == null || replace) {
                if (copy == null) {
                    copy = Files.createTempFile("copy", "-" + archiveFileURI.toString().replaceFirst(".+/", "")).toFile(); // NOI18N
                    copy = copy.getCanonicalFile();
                    copy.deleteOnExit();
                }
                InputStream is = fo.getInputStream();
                try {
                    OutputStream os = new FileOutputStream(copy);
                    try {
                        FileUtil.copy(is, os);
                    } finally {
                        os.close();
                    }
                } finally {
                    is.close();
                }
                copiedJARs.put(archiveFileURI, copy);
            }
            return copy;
        }
    }

}
