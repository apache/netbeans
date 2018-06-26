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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ide.ergonomics.fod;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import org.openide.util.NbBundle;
import org.openide.util.URLStreamHandlerRegistration;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@URLStreamHandlerRegistration(protocol="ergoloc")
public class FoDURLStreamHandler extends URLStreamHandler {

        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            URL orig = new URL("nbresloc", u.getHost(), u.getPort(), u.getFile()); // NOI18N
            final URLConnection connection = orig.openConnection();
            if (!u.getFile().endsWith(".html")) {
                return connection;
            }

            InputStream is = connection.getInputStream();
            byte[] arr = new byte[4096];
            int len = is.read(arr);
            if (len == -1) {
                throw new IOException();
            }
            String head = new String(arr, 0, len, "UTF-8"); // NOI18N
            String newHead = head.replaceFirst("<[bB][oO][dD][yY]>", NbBundle.getMessage(FoDURLStreamHandler.class, "MSG_NotEnabled")); // NOI18N
            ByteArrayInputStream headIS = new ByteArrayInputStream(newHead.getBytes("UTF-8")); // NOI18N

            final SequenceInputStream seq = new SequenceInputStream(headIS, is);

            return new URLConnection(u) {
                @Override
                public void connect() throws IOException {
                    connection.connect();
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return seq;
                }
            };
        }

}
