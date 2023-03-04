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
package org.netbeans.modules.java.lsp.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import org.openide.util.Pair;

public class ConnectionSpecTest {

    private static ThreadLocal<List<Boolean>> copySessionServer = new ThreadLocal<>();

    public ConnectionSpecTest() {
    }

    @After
    public void afterTest() {
        copySessionServer.remove();
    }

    private static LspSession.ScheduledServer copy(Pair<InputStream, OutputStream> io, LspSession lspSession) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        new Thread(() -> {
            if (lspSession == null) {
                future.completeExceptionally(new AssertionError(new NullPointerException("null session")));
                return ;
            }
            try {
                for (;;) {
                    int ch = io.first().read();
                    if (ch == -1) {
                        break;
                    }
                    io.second().write(ch);
                }
                future.complete(null);
            } catch (IOException ex) {
                future.completeExceptionally(new AssertionError("copying error", ex));
            }
        }).start();
        return () -> future;
    }

    private static void setCopy(LspSession session, LspSession.ScheduledServer copyServer) {
        List<Boolean> list = copySessionServer.get();
        if (list == null) {
            list = new ArrayList<>(4);
            copySessionServer.set(list);
        }
        list.add(copyServer != null);
    }

    @Test
    public void testParseNull() throws Exception {
        ConnectionSpec conn = ConnectionSpec.parse(null);
        final byte[] bytes = "Hello".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        conn.prepare("testParseNull", in, os, new LspSession(), ConnectionSpecTest::setCopy, ConnectionSpecTest::copy);
        assertEquals("[true, false]", copySessionServer.get().toString());

        try {
            conn.prepare("testParseNull", in, os, null, ConnectionSpecTest::setCopy, ConnectionSpecTest::copy);
            fail();
        } catch (AssertionError err) {
            assertTrue(err.getCause() instanceof NullPointerException);
        }
        assertEquals("[true, false, true, false]", copySessionServer.get().toString());

        assertArrayEquals("Copied properly", bytes, os.toByteArray());
    }

    @Test
    public void testParseStdio() throws Exception {
        try (ConnectionSpec conn = ConnectionSpec.parse("stdio")) {
            final byte[] bytes = "Hello".getBytes();
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            conn.prepare("testParseNull", in, os, new LspSession(), ConnectionSpecTest::setCopy, ConnectionSpecTest::copy);
            assertEquals("[true, false]", copySessionServer.get().toString());

            assertArrayEquals("Copied properly", bytes, os.toByteArray());
        }
    }

    @Test
    public void testParseListenAndConnect() throws Exception {
        try (ConnectionSpec conn = ConnectionSpec.parse("listen:0")) {
            final byte[] bytes = "Hello".getBytes();
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            conn.prepare("Pipe server", in, os, new LspSession(), ConnectionSpecTest::setCopy, ConnectionSpecTest::copy);
            String reply = os.toString("UTF-8");
            String exp = "Pipe server listening at port ";
            assertTrue(reply, reply.startsWith(exp));
            int port = Integer.parseInt(reply.substring(exp.length()));
            assertTrue("port is specified: " + port, port >= 1024);
            try (ConnectionSpec second = ConnectionSpec.parse("connect:" + port)) {
                second.prepare("Pipe client", in, os, new LspSession(), ConnectionSpecTest::setCopy, ConnectionSpecTest::copy);
            }
        }
    }


}
