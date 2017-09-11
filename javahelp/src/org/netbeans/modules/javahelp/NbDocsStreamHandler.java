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

package org.netbeans.modules.javahelp;

import java.io.*;
import java.net.*;
import java.util.*;

import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInfo;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.URLStreamHandlerRegistration;
import org.openide.util.Utilities;

/** Handler & connection cribbed from NbResourceStreamHandler.
 * @author Jesse Glick
 */
@URLStreamHandlerRegistration(protocol="nbdocs")
public final class NbDocsStreamHandler extends URLStreamHandler {

    /** Make a URLConnection for nbdocs: URLs.
     * @param u the URL
     * @throws IOException if the wrong protocol
     * @return the connection
     */
    protected URLConnection openConnection(URL u) throws IOException {
        if (u.getProtocol().equals("nbdocs")) { // NOI18N
            return new NbDocsURLConnection(u);
        } else {
            throw new IOException("mismatched protocol"); // NOI18N
        }
    }

    @Override
    protected synchronized InetAddress getHostAddress(URL u) {
        if (u.getProtocol().equals("nbdocs")) { // NOI18N
            return null;
        } else {
            return super.getHostAddress(u);
        }
    }

    /** A URL connection that reads from the docs classloader.
     */
    private static final class NbDocsURLConnection extends URLConnection {

        /** underlying URL connection
         */
        private URLConnection real = null;

        /** any associated exception while handling
         */
        private IOException exception = null;

        /** Make the connection.
         * @param u URL to connect to
         */
        public NbDocsURLConnection(URL u) {
            super(u);
        }

        /** Connect to the URL.
         * Actually look up and open the underlying connection.
         * @throws IOException for the usual reasons
         */
        public synchronized void connect() throws IOException {
            if (exception != null) {
                IOException e = exception;
                exception = null;
                throw e;
            }
            if (! connected) {
                String host = url.getHost();
                if (host.length() > 0) {
                    ModuleInfo moduleInfo = findModule(host);
                    if (moduleInfo != null) {
                        if (!moduleInfo.isEnabled()) {
                            URL info = new URL("nbdocs:/org/netbeans/modules/javahelp/resources/notEnabledModule.html"); // NOI18N
                            String moduleName = moduleInfo.getDisplayName();
                            real = new InfoURLConnection(info,moduleName);
                            real.connect();
                            connected = true;
                            return;
                        }
                    } else {
                        URL info = new URL("nbdocs:/org/netbeans/modules/javahelp/resources/notInstalledModule.html"); // NOI18N
                        String moduleName = ""; // NOI18N
                        try {
                            moduleName = NbBundle.getMessage(NbDocsStreamHandler.class,host);
                        } catch (MissingResourceException exc) {
                            moduleName = host;
                        }
                        real = new InfoURLConnection(info,moduleName);
                        real.connect();
                        connected = true;
                        return;
                    }
                }
                String resource = url.getFile();
                if (resource.startsWith("/")) resource = resource.substring(1); //NOI18N
                URL target;
                String ext, basename;
                int index = resource.lastIndexOf('.');
                if (index != -1 && index > resource.lastIndexOf('/')) {
                    ext = resource.substring(index + 1);
                    basename = resource.substring(0, index).replace('/', '.');
                } else {
                    ext = null;
                    basename = resource.replace('/', '.');
                }
                try {
                    target = NbBundle.getLocalizedFile(basename, ext);
                } catch (MissingResourceException mre) {
                    // OK, try file.
                    if (host.isEmpty() && resource.equals("org/netbeans/modules/usersguide/ide.css")) { // NOI18N
                        host = "org.netbeans.modules.usersguide"; // well-known resource referenced w/o host field by many files
                    }
                    File f = InstalledFileLocator.getDefault().locate("docs/" + resource, host.isEmpty() ? null : host, true); // NOI18N
                    if (f != null) {
                        target = Utilities.toURI(f).toURL();
                    } else {
                        IOException ioe = new IOException("cannot connect to " + url + ": " + mre);
                        ioe.initCause(mre);
                        Exceptions.attachLocalizedMessage(ioe,
                                                          NbBundle.getMessage(NbDocsStreamHandler.class,
                                                                              "EXC_nbdocs_cannot_connect",
                                                                              url));
                        throw ioe;
                    }
                }
                //System.err.println("loading from " + target);
                real = target.openConnection();
                real.connect();
                connected = true;
            }
        }

        /** Searches for module with given code name.
         * @param codeNameBase unique string base name of the module
         * (without release number)
         *
         * @return module info of found module or null if module is not found
         * (not installed).
         * @deprecated will be replaced by similar method in Modules Open APIs in
         * future releases
         */
        private static ModuleInfo findModule (String codeNameBase) {
            Lookup.Result<ModuleInfo> modulesResult =
                Lookup.getDefault().lookup(new Lookup.Template<ModuleInfo>(ModuleInfo.class));
            for (ModuleInfo curInfo: modulesResult.allInstances()) {
                if (curInfo.getCodeNameBase().equals(codeNameBase)) {
                    return curInfo;
                }
            }
            return null;
        }

        /** Maybe connect, if not keep track of the problem.
         */
        private void tryToConnect() {
            if (connected || exception != null) return;
            try {
                connect();
            } catch (IOException ioe) {
                exception = ioe;
            }
        }

        /** Get a URL header.
         * @param n index of the header
         * @return the header value
         */
        public String getHeaderField(int n) {
            tryToConnect();
            if (connected)
                return real.getHeaderField(n);
            else
                return null;
        }

        /** Get the name of a header.
         * @param n the index
         * @return the header name
         */
        public String getHeaderFieldKey(int n) {
            tryToConnect();
            if (connected)
                return real.getHeaderFieldKey(n);
            else
                return null;
        }

        /** Get a header by name.
         * @param key the header name
         * @return the value
         */
        public String getHeaderField(String key) {
            tryToConnect();
            if (connected)
                return real.getHeaderField(key);
            else
                return null;
        }

        /** Get an input stream on the connection.
         * @throws IOException for the usual reasons
         * @return a stream to the object
         */
        public InputStream getInputStream() throws IOException {
            connect();
            return real.getInputStream();
        }

        /** Get an output stream on the object.
         * @throws IOException for the usual reasons
         * @return an output stream writing to it
         */
        public OutputStream getOutputStream() throws IOException {
            connect();
            return real.getOutputStream();
        }

        /** Get the type of the content.
         * @return the MIME type
         */
        public String getContentType() {
            tryToConnect();
            if (connected)
                return real.getContentType();
            else
                return "application/octet-stream"; // NOI18N
        }

        /** Get the length of content.
         * @return the length in bytes
         */
        public int getContentLength() {
            tryToConnect();
            if (connected)
                return real.getContentLength();
            else
                return 0;
        }

    }

    /** A URL connection that reads from the info files. It displays
     * help page when referred module is not enabled or installed.
     * It also takes module display name from bundle when available.
     * Module base name is key to retrieve module display name
     * eg.: org.netbeans.modules.web.monitor=HTTP Monitor
     */
    private static final class InfoURLConnection extends URLConnection {
        /** Provides input stream for this connection. */
        private ByteArrayInputStream stream;
        /** Module display name */
        private String moduleName;

        /** Make the connection.
         * @param u URL to connect to
         */
        public InfoURLConnection (URL u, String moduleName) {
            super(u);
            this.moduleName = moduleName;
        }

        /** Connect to the URL.
         * Actually look up and open the underlying connection.
         * @throws IOException for the usual reasons
         */
        public synchronized void connect() throws IOException {
            if (!connected) {
                //Prepare data
                InputStream is = url.openStream();
                if (is != null) {
                    byte [] arr;
                    arr = readData(is);
                    String s1 = new String(arr,"UTF-8"); // NOI18N
                    String s2 = s1.replaceAll("\\{0\\}",moduleName); // NOI18N
                    arr = s2.getBytes("UTF-8");
                    stream = new ByteArrayInputStream(arr);
                } else {
                    throw new IOException("Info file not found."); // NOI18N
                }
                connected = true;
            }
        }

        /** Reads all available data from input steram to byte array. It is workaround
         * to avoid usage of InputStream.available which might be unreliable on URL. */
        private byte [] readData (InputStream is) throws IOException {
            int step = 4096;
            byte[] buff = new byte[step];
            byte[] sum = new byte[0];
            byte[] result;
            int len = -1, readLen = 0, allocLen = 0;

            for (;;) {
                len = is.read(buff);
                if (len == -1) {
                    result = new byte[readLen];
                    System.arraycopy(sum,0,result,0,readLen);
                    return result;
                }
                if (allocLen < (readLen + len)) {
                    byte [] tmp = new byte[sum.length];
                    System.arraycopy(sum,0,tmp,0,readLen);
                    sum = new byte[allocLen + step];
                    allocLen = allocLen + step;
                    System.arraycopy(tmp,0,sum,0,readLen);
                }
                System.arraycopy(buff,0,sum,readLen,len);
                readLen = readLen + len;
            }
        }

        /** Maybe connect, if not keep track of the problem.
         */
        private void tryToConnect() {
            if (connected) {
                return;
            }
            try {
                connect();
            } catch (IOException ioe) {
            }
        }

        /** Get an input stream on the connection.
         * @throws IOException for the usual reasons
         * @return a stream to the object
         */
        public InputStream getInputStream() throws IOException {
            connect();
            return stream;
        }


        /** Get the type of the content.
         * @return the MIME type
         */
        public String getContentType() {
            return "text/html"; // NOI18N
        }

        /** Get the length of content.
         * @return the length in bytes
         */
        public int getContentLength() {
            tryToConnect();
            if (connected) {
                return stream.available();
            } else {
                return 0;
            }
        }
    }
}
