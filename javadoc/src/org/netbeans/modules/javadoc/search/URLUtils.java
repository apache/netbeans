/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javadoc.search;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 * Convenience methods for looking up available URLs such as Javadoc HTML pages.
 */
class URLUtils {

    private URLUtils() {}

    private static final Logger LOG = Logger.getLogger(URLUtils.class.getName());

    /**
     * Like {@link URL#openStream} but uses the platform's user JAR cache ({@code ArchiveURLMapper}) when available.
     * @param url a url to open
     * @return its input stream
     * @throws IOException for the usual reasons
     */
    public static InputStream openStream(URL url) throws IOException {
        if (url.getProtocol().equals("jar")) { // NOI18N
            FileObject f = URLMapper.findFileObject(url);
            if (f != null) {
                return f.getInputStream();
            }
        }
        if (url.getProtocol().startsWith("http")) { // NOI18N
            LOG.log(Level.FINE, "opening network stream: {0}", url);
        }
        return url.openStream();
    }

    /**
     * Attempts to {@linkplain URL#openStream open} some URLs in turn.
     * If opening a URL fails, it is skipped.
     * It is the caller's responsibility to close any non-null result.
     * @param base a base URL
     * @param specs relative paths
     * @return a stream from one of the constructed URLs, or null if none could be opened
     */
    public static InputStream open(URL base, String... specs) {
        for (String spec : specs) {
            try {
                URL url = new URL(base, spec);
                try {
                    return openStream(url);
                } catch (IOException x) {
                    LOG.log(Level.FINE, "Could not open " + url, x);
                }
            } catch (MalformedURLException x) {
                LOG.log(Level.INFO, "Could not construct " + base + spec, x);
            }
        }
        return null;
    }

    /**
     * Like {@link #open} but just returns the URL that worked.
     * Any opened stream is closed again.
     * @param base a base URL
     * @param specs relative paths
     * @return the constructed URL, or null if none cannot be opened
     */
    public static URL findOpenable(URL base, String... specs) {
        for (String spec : specs) {
            try {
                URL url = new URL(base, spec);
                try {
                    openStream(url).close();
                    return url;
                } catch (IOException x) {
                    LOG.log(Level.FINE, "Could not open " + url, x);
                } catch (Exception x) {
                    LOG.log(Level.INFO, "Could not open " + url, x);
                }
            } catch (MalformedURLException x) {
                LOG.log(Level.INFO, "Could not construct " + base + spec, x);
            }
        }
        return null;
    }

    /**
     * Produces a display name appropriate to a URL.
     * @param url a URL
     * @return some display name, e.g. file path
     * @see FileUtil#getFileDisplayName
     */
    public static String getDisplayName(URL url) {
        FileObject fo = URLMapper.findFileObject(url);
        return fo != null ? FileUtil.getFileDisplayName(fo) : url.toString();
    }

}
