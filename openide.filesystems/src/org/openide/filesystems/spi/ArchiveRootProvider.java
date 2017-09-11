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
package org.openide.filesystems.spi;

import java.net.URL;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 * A possibility to plug a support for java archives into FileUtil.
 * The interface is used by {@link FileUtil.isArchiveArtifact}, {@link FileUtil.isArchiveFile},
 * {@link FileUtil.getArchiveRoot}, {@link FileUtil.getArchiveFile}.
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
     * Clients may need to first call {@link #isArchiveFile(URL)} to determine if the URL
     * refers to an archive file.
     * @param url of an java archive file
     * @return the archive-protocol URL of the root of the archive
     */
    URL getArchiveRoot(URL url);

    /**
     * Returns a FileObject representing the root folder of an archive.
     * Clients may need to first call {@link #isArchiveFile(FileObject)} to determine
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
