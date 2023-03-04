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

package org.netbeans.modules.ide.ergonomics.fod;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
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
            String head = new String(arr, 0, len, StandardCharsets.UTF_8);
            String newHead = head.replaceFirst("<[bB][oO][dD][yY]>", NbBundle.getMessage(FoDURLStreamHandler.class, "MSG_NotEnabled")); // NOI18N
            ByteArrayInputStream headIS = new ByteArrayInputStream(newHead.getBytes(StandardCharsets.UTF_8));

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
