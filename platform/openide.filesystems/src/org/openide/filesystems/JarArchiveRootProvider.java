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
package org.openide.filesystems;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.spi.ArchiveRootProvider;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
final class JarArchiveRootProvider implements ArchiveRootProvider {
    private static final String PROTOCOL = "jar";   //NOI18N
    /** Normal header for ZIP files. */
    private static byte[] ZIP_HEADER_1 = {0x50, 0x4b, 0x03, 0x04};
    /** Also seems to be used at least in apisupport/project/test/unit/data/example-external-projects/suite3/nbplatform/random/modules/ext/stuff.jar; not known why */
    private static byte[] ZIP_HEADER_2 = {0x50, 0x4b, 0x05, 0x06};
    private static final Logger LOG = Logger.getLogger(JarArchiveRootProvider.class.getName());
    /** Cache for {@link #isArchiveFile(FileObject)}. */
    private static final Map</*@GuardedBy("archiveFileCache")*/FileObject, Boolean> archiveFileCache = Collections.synchronizedMap(new WeakHashMap<FileObject,Boolean>());

    private static final Set<String> KNOWN_ZIP_EXTENSIONS = new HashSet<>(Arrays.asList("jar", "war", "zip", "ear", "sar", "rar")); //NOI18N

    @Override
    public boolean isArchiveFile(URL url, boolean strict) {
        if (PROTOCOL.equals(url.getProtocol())) { //NOI18N
            //Already inside archive, return false
            return false;
        }
        FileObject fo = URLMapper.findFileObject(url);
        if ((fo != null) && !fo.isVirtual()) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "isArchiveFile_FILE_RESOLVED", fo); //NOI18N, used by FileUtilTest.testIsArchiveFileRace
            }
            return isArchiveFile(fo, strict);
        } else {
            return isArchiveFile(url.getPath());
        }
    }

    @Override
    public boolean isArchiveFile(FileObject fo, boolean strict) {
        if (!fo.isValid()) {
            return isArchiveFile(fo.getPath());
        }
        // XXX Special handling of virtual file objects: try to determine it using its name, but don't cache the
        // result; when the file is checked out the more correct method can be used
        if (fo.isVirtual()) {
            return isArchiveFile(fo.getPath());
        }

        if (fo.isFolder()) {
            return false;
        }

        // First check the cache.
        Boolean b = archiveFileCache.get(fo);
        if (b == null) {
            // Need to check it.
            try {
                InputStream in = fo.getInputStream();

                try {
                    byte[] buffer = new byte[4];
                    int len = in.read(buffer, 0, 4);

                    if (len == 4) {
                        // Got a header, see if it is a ZIP file.
                        b = Boolean.valueOf(Arrays.equals(ZIP_HEADER_1, buffer) || Arrays.equals(ZIP_HEADER_2, buffer));
                    } else {
                        //If the length is less than 4, it can be either
                        //broken (empty) archive file or other empty file.
                        //Return false and don't cache it, when the archive
                        //file will be written and closed its length will change
                        return !strict;
                    }
                } finally {
                    in.close();
                }
            } catch (IOException ioe) {
                // #160507 - ignore exception (e.g. permission denied)
                LOG.log(Level.FINE, null, ioe);
            }
            if (b == null) {
                b = isArchiveFile(fo.getPath());
            }
            archiveFileCache.put(fo, b);
        }
        return b.booleanValue();
    }


    @Override
    public boolean isArchiveArtifact(URL url) {
        return PROTOCOL.equals(url.getProtocol());
    }

    @Override
    public URL getArchiveFile(URL url) {
        String protocol = url.getProtocol();

        if (PROTOCOL.equals(protocol)) { //NOI18N

            String path = url.getPath();
            int index = path.indexOf("!/"); //NOI18N

            if (index >= 0) {
                String jarPath = null;
                try {
                    jarPath = path.substring(0, index);
                    if (jarPath.indexOf("file://") > -1 && jarPath.indexOf("file:////") == -1) {  //NOI18N
                        /* Replace because JDK application classloader wrongly recognizes UNC paths. */
                        jarPath = jarPath.replaceFirst("file://", "file:////");  //NOI18N
                    }
                    return new URL(jarPath);

                } catch (MalformedURLException mue) {
                    LOG.log(
                        Level.WARNING,
                        "Invalid URL ({0}): {1}, jarPath: {2}", //NOI18N
                        new Object[] {
                            mue.getMessage(),
                            url.toExternalForm(),
                            jarPath
                        });
                }
            }
        }
        return null;
    }

    @Override
    public FileObject getArchiveFile(FileObject fo) {
        try {
            final FileSystem fs = fo.getFileSystem();
            if (fs instanceof JarFileSystem) {
                final File jarFile = ((JarFileSystem) fs).getJarFile();
                return FileUtil.toFileObject(jarFile);
            }
        } catch (FileStateInvalidException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }

    @Override
    public URL getArchiveRoot(final URL url) {
        try {
            // XXX TBD whether the url should ever be escaped...
            return new URL("jar:" + url + "!/"); // NOI18N
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Tests if a non existent path represents a file.
     * @param path to be tested, separated by '/'.
     * @return true if the file has '.' after last '/' and the text after 
     *         the '.' is a known zip extension.
     */
    private static boolean isArchiveFile (final String path) {
        int dot = path.lastIndexOf('.');   //NOI18N
        int slash = path.lastIndexOf('/'); //NOI18N
        return (dot != -1) && (dot > slash + 1) && KNOWN_ZIP_EXTENSIONS.contains(path.substring(dot + 1));
    }

}
