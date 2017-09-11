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

package org.netbeans.core.startup.layers;

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
        if (!(u1.getFile() == u2.getFile() ||
              (u1.getFile() != null && u1.getFile().equals(u2.getFile()))))
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

        public int getContentLength() {
            try {
                this.connect();
                return (int) f.length();     //May cause overflow long->int
            } catch (IOException e) {
                return -1;
            }
        }


        public InputStream getInputStream() throws IOException {
            this.connect();
            if (iStream == null) {
                iStream = new FileInputStream(f);
            }
            return iStream;
        }


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
