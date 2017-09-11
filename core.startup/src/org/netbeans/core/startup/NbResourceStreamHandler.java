/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
