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
package org.netbeans.modules.java.lsp.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConnectionSpecTest {
    public ConnectionSpecTest() {
    }

    private static void copy(InputStream is, OutputStream os) {
        try {
            for (;;) {
                int ch = is.read();
                if (ch == -1) {
                    break;
                }
                os.write(ch);
            }
        } catch (IOException ex) {
            throw new AssertionError("copying error", ex);
        }
    }

    @Test
    public void testParseNull() throws Exception {
        ConnectionSpec conn = ConnectionSpec.parse(null);
        final byte[] bytes = "Hello".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        conn.prepare("testParseNull", in, os, ConnectionSpecTest::copy);

        assertArrayEquals("Copied properly", bytes, os.toByteArray());
    }

    @Test
    public void testParseStdio() throws Exception {
        try (ConnectionSpec conn = ConnectionSpec.parse("stdio")) {
            final byte[] bytes = "Hello".getBytes();
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            conn.prepare("testParseNull", in, os, ConnectionSpecTest::copy);

            assertArrayEquals("Copied properly", bytes, os.toByteArray());
        }
    }

    @Test
    public void testParseListenAndConnect() throws Exception {
        try (ConnectionSpec conn = ConnectionSpec.parse("listen:0")) {
            final byte[] bytes = "Hello".getBytes();
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            conn.prepare("Pipe server", in, os, ConnectionSpecTest::copy);
            String reply = os.toString("UTF-8");
            String exp = "Pipe server listening at port ";
            assertTrue(reply, reply.startsWith(exp));
            int port = Integer.parseInt(reply.substring(exp.length()));
            assertTrue("port is specified: " + port, port >= 1024);
            try (ConnectionSpec second = ConnectionSpec.parse("connect:" + port)) {
                second.prepare("Pipe client", in, os, ConnectionSpecTest::copy);
            }
        }
    }


}
