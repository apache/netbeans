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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * @author Radek Matous
 */
@SuppressWarnings("deprecation") // needs to use old Repository methods for compat
final class NbfsUtil {
    /** url separator */
    private static final char SEPARATOR = '/';

    private static final String SYSTEM_FILE_SYSTEM_NAME = "SystemFileSystem";

    /**
     * Gets URL with nbfs protocol for passes fo
     * @param fo
     * @return url with nbfs protocol
     */
    static URL getURL(final FileObject fo) {
        String fsPart;
        try {
            fsPart = encodeFsPart(fo);
        } catch (FileStateInvalidException x) {
            fsPart = "invalid";
        }
        final String foPart = encodeFoPart(fo);

        final String host = "nbhost"; //NOI18N
        final String file = combine(fsPart, foPart);

        // #13038: the URL constructor accepting a handler is a security-sensitive
        // operation. Sometimes a user class loaded internally (customized bean...),
        // which has no privileges, needs to make and use an nbfs: URL, since this
        // may be the URL used by e.g. ClassLoader.getResource for resources.
        try {
            return AccessController.doPrivileged(
                new PrivilegedExceptionAction<URL>() {
                    public URL run() throws Exception {
                        // #30397: the fsPart name cannot be null
                        return new URL(FileURL.PROTOCOL, host, -1, file, new FileURL.Handler());
                    }
                }
            );
        } catch (PrivilegedActionException pae) {
            // MalformedURLException is declared but should not happen.
            IllegalStateException ise = new IllegalStateException(pae.toString());
            ExternalUtil.annotate(ise, pae);
            throw ise;
        }
    }

    private static String combine(final String host, final String file) {
        StringBuffer sb = new StringBuffer();
        sb.append(SEPARATOR).append(host);
        sb.append(file);

        return sb.toString();
    }

    private static String[] split(URL url) {
        String file = url.getFile();
        int idx = file.indexOf("/", 1);
        String fsPart = "";
        String foPart = file;

        if (idx > 1) {
            fsPart = file.substring(1, idx);
            foPart = file.substring(idx + 1);
        }

        return new String[] { fsPart, foPart };
    }

    /**
     *  Gets FileObject for passed url.
     * @param url
     * @return appropriate FileObject. Can return null for other protocol than nbfs or
     * if such FileObject isn't reachable via Repository.
     */
    static FileObject getFileObject(URL url) {
        if (!url.getProtocol().equals(FileURL.PROTOCOL)) {
            return null;
        }

        if (isOldEncoding(url)) {
            return oldDecode(url);
        }

        String[] urlParts = split(url);

        String fsName = decodeFsPart(urlParts[0]);
        String foName = decodeFoPart(urlParts[1]);

        FileSystem fsys = fsName.equals(SYSTEM_FILE_SYSTEM_NAME) ? Repository.getDefault().getDefaultFileSystem() : Repository.getDefault().findFileSystem(fsName);

        return (fsys == null) ? null : fsys.findResource(foName);
    }

    private static String encodeFsPart(FileObject fo) throws FileStateInvalidException {
        FileSystem fs = fo.getFileSystem();
        assert fs != null : "File object " + fo + " returns null from getFileSystem()";
        String n = fs.isDefault() ? SYSTEM_FILE_SYSTEM_NAME : fs.getSystemName();
        if (n.isEmpty()) {
            n = String.format("%s.%h", fs.getClass().getName(), fs);
        }
        return encoder(n);
    }

    private static String encodeFoPart(FileObject fo) {
        String path = fo.getPath();
        if (fo.isFolder()) {
            path += "/";
        }
        if (!fo.isRoot()) {
            path = "/" + path;
        }
        try {
            return new URI(null, path, null).toString();
        } catch (URISyntaxException x) {
            return "???";
        }
    }

    private static String decodeFsPart(String encodedStr) {
        return decoder(encodedStr);
    }

    private static String decodeFoPart(String encodedStr) {
        if (encodedStr == null) {
            return ""; //NOI18N
        }
        if (encodedStr.endsWith("/")) {
            encodedStr = encodedStr.substring(0, encodedStr.length() - 1);
        }
        try {
            return new URI(encodedStr).getPath();
        } catch (URISyntaxException x) {
            return "???";
        }
    }

    private static String encoder(String elem) {
        try {
            return URLEncoder.encode(elem, "UTF-8"); // NOI18N
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    private static String decoder(String elem) {
        try {
            return URLDecoder.decode(elem, "UTF-8"); // NOI18N
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    // backward compatibility
    private static boolean isOldEncoding(URL url) {
        String host = url.getHost();

        return (host == null) || (host.length() == 0);
    }

    private static FileObject oldDecode(URL u) {
        String resourceName = u.getFile();

        if (resourceName.startsWith("/")) {
            resourceName = resourceName.substring(1); // NOI18N
        }

        // first part is FS name
        int first = resourceName.indexOf('/'); // NOI18N

        if (first == -1) {
            return null;
        }

        String fileSystemName = oldDecodeFSName(resourceName.substring(0, first));
        resourceName = resourceName.substring(first);

        FileSystem fsys = Repository.getDefault().findFileSystem(fileSystemName);

        return (fsys == null) ? null : fsys.findResource(resourceName);
    }

    /** Decodes name to FS one.
     * @param name encoded name
     * @return original name of the filesystem
     */
    private static String oldDecodeFSName(String name) {
        StringBuffer sb = new StringBuffer();
        int i = 0;
        int len = name.length();

        while (i < len) {
            char ch = name.charAt(i++);

            if ((ch == 'Q') && (i < len)) {
                switch (name.charAt(i++)) {
                case 'B':
                    sb.append('/');

                    break;

                case 'C':
                    sb.append(':');

                    break;

                case 'D':
                    sb.append('\\');

                    break;

                case 'E':
                    sb.append('#');

                    break;

                default:
                    sb.append('Q');

                    break;
                }
            } else {
                // not Q
                sb.append(ch);
            }
        }

        return sb.toString();
    }
}
