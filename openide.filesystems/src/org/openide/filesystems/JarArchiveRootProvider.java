/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.openide.filesystems;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
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
     * @return true if the file has '.' after last '/'.
     */
    private static boolean isArchiveFile (final String path) {
        int index = path.lastIndexOf('.');  //NOI18N
        return (index != -1) && (index > path.lastIndexOf('/') + 1);    //NOI18N
    }

}
