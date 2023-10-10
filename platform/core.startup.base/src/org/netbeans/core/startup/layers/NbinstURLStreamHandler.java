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

import java.util.Objects;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.UnknownServiceException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.URLStreamHandlerRegistration;

/**
 * URLStreamHandler for nbinst protocol
 */
@URLStreamHandlerRegistration(protocol=NbinstURLMapper.PROTOCOL)
public class NbinstURLStreamHandler extends URLStreamHandler {

    protected URLConnection openConnection(URL u) throws IOException {
        return new NbinstURLConnection(u);
    }

    @Override
    protected InetAddress getHostAddress(URL u) {
        return null;
    }

    @Override
    protected int hashCode(URL u) {
        int h = 0;

        // Generate the host part.
        String host = u.getHost();
        if (host != null)
            h += host.toLowerCase().hashCode();

        // Generate the file part.
        String file = u.getFile();
        if (file != null)
            h += file.hashCode();

        return h;        
    }

    @Override
    protected boolean sameFile(URL u1, URL u2) {
        // Compare the protocols.
        if (!((u1.getProtocol() == u2.getProtocol()) ||
              (u1.getProtocol() != null &&
               u1.getProtocol().equalsIgnoreCase(u2.getProtocol()))))
            return false;

        // Compare the files.
        if (!Objects.equals(u1.getFile(), u2.getFile()))
            return false;

        // Compare the hosts.
        if (!hostsEqual(u1, u2))
            return false;

        return true;
    }

    @Override
    protected boolean hostsEqual(URL u1, URL u2) {
        final String host1 = u1.getHost();
        final String host2 = u2.getHost();
        
        if (host1 != null && host2 != null)
            return host1.equalsIgnoreCase(host2);
         else
            return host1 == null && host2 == null;
    }

    /** URLConnection for URL with nbinst protocol.
     *
     */
    private static class NbinstURLConnection extends URLConnection {

        private File f;
        private InputStream iStream;

        /**
         * Creates new URLConnection
         * @param url the parameter for which the connection should be
         * created
         */
        public NbinstURLConnection (URL url) {
            super (url);
        }


        public void connect() throws IOException {
            if (f == null) {
                f = NbinstURLMapper.decodeURL(url);
                if (f == null) {
                    throw new FileNotFoundException("Cannot find: " + url); // NOI18N
                }
            }
            if (!f.isFile()) {
                throw new UnknownServiceException();
            }
        }

        @Override
        public int getContentLength() {
            try {
                this.connect();
                return (int) f.length();     //May cause overflow long->int
            } catch (IOException e) {
                return -1;
            }
        }


        @Override
        public InputStream getInputStream() throws IOException {
            this.connect();
            if (iStream == null) {
                iStream = new FileInputStream(f);
            }
            return iStream;
        }


        @Override
        public String getHeaderField (String name) {
            if ("content-type".equals(name)) {                  //NOI18N
                try {
                    this.connect();
                    FileObject fo = FileUtil.toFileObject(f);
                    if (fo != null) {
                        return fo.getMIMEType();
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
            return super.getHeaderField(name);
        }
    }
}
