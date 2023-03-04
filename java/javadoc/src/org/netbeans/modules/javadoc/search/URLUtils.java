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
