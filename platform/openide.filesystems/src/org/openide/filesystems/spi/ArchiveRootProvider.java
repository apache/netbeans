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
package org.openide.filesystems.spi;

import java.net.URL;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 * A possibility to plug a support for java archives into FileUtil.
 * The interface is used by {@link FileUtil#isArchiveArtifact}, {@link FileUtil#isArchiveFile},
 * {@link FileUtil#getArchiveRoot}, {@link FileUtil#getArchiveFile}.
 * The implementations are registered in global lookup.
 * @author Tomas Zezula
 * @since 9.10
 */
public interface ArchiveRootProvider {

    /**
     * Tests if a file represents an java archive.
     * @param url the file to be tested
     * @param strict when false the detection may not be precise, for example
     * an empty archive missing the archive header is treated as an archive
     * @return true if the file looks like an archive
     */
    boolean isArchiveFile(URL url, boolean strict);

    /**
     * Tests if a file represents an java archive.
     * The default implementation delegates to {@link ArchiveRootProvider#isArchiveFile(URL, boolean)},
     * it can be overridden by an implementation in more efficient way.
     * @param fo the file to be tested
     * @param strict when false the detection may not be precise, for example
     * an empty archive missing the archive header is treated as an archive
     * @return true if the file looks like an archive
     */
    default boolean isArchiveFile(FileObject fo, boolean strict) {
        final URL url = URLMapper.findURL(fo, URLMapper.EXTERNAL);
        return url == null ? false : isArchiveFile(url, strict);
    }

    /**
     * Tests if an {@link URL} denotes a file inside an archive.
     * @param url the url to be tested
     * @return true if the url points inside an archive
     */
    boolean isArchiveArtifact(URL url);

    /**
     * Tests if an file is inside an archive.
     * The default implementation delegates to {@link ArchiveRootProvider#isArchiveArtifact(URL)},
     * it can be overridden by an implementation in more efficient way.
     * @param fo the file to be tested
     * @return true if the file is inside an archive
     */
    default boolean isArchiveArtifact(FileObject fo) {
        final URL url = URLMapper.findURL(fo, URLMapper.EXTERNAL);
        return url == null ? false : isArchiveArtifact(url);
    }

    /**
     * Returns the URL of the archive file containing the file
     * referred to by a archive-protocol URL.
     * <strong>Remember</strong> that any path within the archive is discarded
     * so you may need to check for non-root entries.
     * @param url a URL
     * @return the embedded archive URL, or null if the URL is not an
     *         archive-protocol URL containing <code>!/</code>
     */
    URL getArchiveFile(URL url);

    /**
     * Returns a FileObject representing an archive file containing the
     * FileObject given by the parameter.
     * <strong>Remember</strong> that any path within the archive is discarded
     * so you may need to check for non-root entries.
     * The default implementation delegates to {@link ArchiveRootProvider#getArchiveFile(URL)},
     * it can be overridden by an implementation in more efficient way.
     * @param fo a file in a archive filesystem
     * @return the file corresponding to the archive itself,
     *         or null if <code>fo</code> is not an archive entry
     */
    default FileObject getArchiveFile(FileObject fo) {
        final URL rootURL = URLMapper.findURL(fo, URLMapper.EXTERNAL);
        if (rootURL == null) {
            return null;
        }
        return URLMapper.findFileObject(FileUtil.getArchiveFile(rootURL));
    }

    /**
     * Returns an URL representing the root of an archive.
     * Clients may need to first call {@link #isArchiveFile(URL,boolean)} to determine if the URL
     * refers to an archive file.
     * @param url of an java archive file
     * @return the archive-protocol URL of the root of the archive
     */
    URL getArchiveRoot(URL url);

    /**
     * Returns a FileObject representing the root folder of an archive.
     * Clients may need to first call {@link #isArchiveFile(FileObject,boolean)} to determine
     * if the file object refers to an archive file.
     * The default implementation delegates to {@link ArchiveRootProvider#getArchiveRoot(URL)},
     * it can be overridden by an implementation in more efficient way.
     * @param fo an java archive file
     * @return a virtual archive root folder, or null if the file is not actually an archive
     */
    default FileObject getArchiveRoot(FileObject fo) {
        final URL archiveURL = URLMapper.findURL(fo, URLMapper.EXTERNAL);
        if (archiveURL == null) {
            return null;
        }
        return URLMapper.findFileObject(FileUtil.getArchiveRoot(archiveURL));
    }
}
