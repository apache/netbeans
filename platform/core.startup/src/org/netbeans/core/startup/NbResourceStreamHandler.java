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

package org.netbeans.core.startup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.URLStreamHandlerRegistration;

    /** Stream handler for internal resource-based URLs.
     * @author Jesse Glick
     */
    @URLStreamHandlerRegistration(protocol={NbResourceStreamHandler.PROTOCOL_SYSTEM_RESOURCE, NbResourceStreamHandler.PROTOCOL_LOCALIZED_SYSTEM_RESOURCE})
    public final class NbResourceStreamHandler extends URLStreamHandler {
        
        public static final String PROTOCOL_SYSTEM_RESOURCE = "nbres"; // NOI18N
        public static final String PROTOCOL_LOCALIZED_SYSTEM_RESOURCE = "nbresloc"; // NOI18N
        
        public URLConnection openConnection(URL u) throws IOException {
            if (u.getProtocol().equals(PROTOCOL_SYSTEM_RESOURCE)) {
                return new Connection(u, false);
            } else if (u.getProtocol().equals(PROTOCOL_LOCALIZED_SYSTEM_RESOURCE)) {
                return new Connection(u, true);
            } else {
                throw new IOException("Bad protocol: " + u.getProtocol()); // NOI18N
            }
        }
        
        private static class Connection extends URLConnection {
            
            private final boolean localized;
            
            // A real connection to delegate to. Non-null if successfully connected.
            private URLConnection real;
            
            private IOException exception = null;
            
            public Connection(URL u, boolean localized) {
                super(u);
                this.localized = localized;
            }
            
            /** Tries to get a URL from this resource from the proper classloader,
             * localizing first if requested.
             * Also opens the URL to make a connection; this connection, <code>real</code>,
             * will be delegated to for all operations.
             */
            public synchronized void connect() throws IOException {
                if (exception != null) {
                    // See tryToConnect().
                    IOException e = exception;
                    exception = null;
                    throw e;
                }
                if (! connected) {
                    String resource = url.getPath();
                    if (resource.length() > 0 && resource.charAt(0) == '/') { // NOI18N
                        resource = resource.substring(1);
                    } else {
                        Logger.getLogger(NbResourceStreamHandler.class.getName()).log(Level.WARNING, "URL path should begin with a slash: " + url);
                    }
                    ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
                    URL target;
                    URL t1 = loader.getResource(resource);
                    if (localized) {
                        // Find the suffix insertion point.
                        // XXX #29580: should have a shared API for this
                        int dotIndex = resource.lastIndexOf('.');
                        if (dotIndex < resource.lastIndexOf('/')) {
                            dotIndex = -1;
                        }
                        String base, ext;
                        if (dotIndex != -1) {
                            base = resource.substring(0, dotIndex);
                            ext = resource.substring(dotIndex);
                        } else {
                            base = resource;
                            ext = "";
                        }
                        target = null;
                        Iterator<String> suffixes = NbBundle.getLocalizingSuffixes();
                        while (suffixes.hasNext()) {
                            String suffix = suffixes.next();
                            target = "".equals(suffix)? t1: loader.getResource(base + suffix + ext);
                            if (target != null) {
                                break;
                            }
                        }
                    } else {
                        target = t1;
                    }
                    if (target == null) {
                        throw new IOException(NbBundle.getMessage(NbResourceStreamHandler.class, "EXC_nbres_cannot_connect", url));
                    }
                    real = target.openConnection();
                    real.connect();
                    connected = true;
                }
            }
            
            /** Try to connect; but if it does not work, oh well.
             * Ideally this would be quite unnecessary.
             * Unfortunately much code, inclduing the Swing editor kits,
             * gets header fields and so on without ever calling connect().
             * These methods cannot even throw exceptions so it is a mess.
             * E.g. if you display a nbres: URL in the ICE browser, it is fine:
             * it calls connect() according to the specification, then
             * getContentType() produces text/html as expected.
             * But using the SwingBrowser default implementation, it goes
             * ahead and calls getContentType() immediately. So we have
             * to try to connect and get the right content type then too.
             * This complicated the timing of error reporting.
             */
            private void tryToConnect() {
                if (connected || exception != null) return;
                try {
                    connect();
                } catch (IOException ioe) {
                    exception = ioe;
                }
            }
            
            @Override
            public String getHeaderField(int n) {
                tryToConnect();
                if (connected)
                    return real.getHeaderField(n);
                else
                    return null;
            }
            
            @Override
            public String getHeaderFieldKey(int n) {
                tryToConnect();
                if (connected)
                    return real.getHeaderFieldKey(n);
                else
                    return null;
            }
            
            @Override
            public String getHeaderField(String key) {
                tryToConnect();
                if (connected) {
                    return real.getHeaderField(key);
                }
                return null;
            }
            
            @Override
            public InputStream getInputStream() throws IOException {
                connect();
                return real.getInputStream();
            }
            
            @Override
            public OutputStream getOutputStream() throws IOException {
                connect();
                return real.getOutputStream();
            }
            
            // Should not be required, but they are:
            
            @Override
            public String getContentType() {
                tryToConnect();
                if (connected)
                    return real.getContentType();
                else
                    return "application/octet-stream"; // NOI18N
            }
            
            @Override
            public int getContentLength() {
                tryToConnect();
                if (connected)
                    return real.getContentLength();
                else
                    return 0;
            }

            public @Override long getLastModified() {
                if (connected && /* #96928 */ !real.getURL().toExternalForm().contains("http:")) { // NOI18N
                    return real.getLastModified();
                }
                return 0L;
            }

            // [PENDING] might be some more methods it would be useful to delegate, possibly
            
        }
        
    }
