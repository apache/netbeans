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

package org.netbeans.lib.v8debug.connection;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Map;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;
import org.netbeans.lib.v8debug.V8Event;
import org.netbeans.lib.v8debug.V8Response;
import org.netbeans.lib.v8debug.events.BreakEventBody;

/**
 *
 * @author Martin Entlicher
 */
public class ClientConnectionTest {
    
    private static final String CHUNK1 = "{\"seq\":11,\"request_seq\":2,\"type\":\"response\",\"command\":\"source\",\"success\":false,\"message\":\"";
    private static final String CHUNK2 = "\",\"running\":true}";
    private static final int MIN_CHUNK_LENGTH = CHUNK1.length() + CHUNK2.length();
    
    private static final String EVENT1 = "{\"seq\":1,\"type\":\"event\",\"event\":\"break\",\"body\":{\"invocationText\":\"#<Object>.[anonymous]\",\"sourceLine\":1,\"sourceColumn\":0,\"sourceLineText\":\"abc('";
    private static final String EVENT2 = "');\",\"script\":{\"id\":76,\"name\":\"main.js\",\"lineOffset\":0,\"columnOffset\":0,\"lineCount\":4},\"breakpoints\":[1]}}";
    private static final String CONTINUE = "{\"seq\":18,\"request_seq\":1,\"type\":\"response\",\"command\":\"continue\",\"success\":true,\"running\":true}";
    
    @Test
    public void bigChunkTest() throws IOException, InterruptedException {
        final boolean[] success = new boolean[] { true };
        PipedOutputStream pos = createPipedConnection(success, new DummyListener());
        for (int nch = 1; nch < 10; nch++) {
            System.out.println("Num chunks: "+nch);
            for (int chs = MIN_CHUNK_LENGTH; chs < 20000; chs++) {
                pos.write(generateChunk(nch, chs));
            }
        }
        Thread.sleep(200);
        boolean retSuccess = success[0];
        pos.close();
        Assert.assertTrue(retSuccess);
    }
    
    private PipedOutputStream createPipedConnection(final boolean[] success,
                                                    final ClientConnection.Listener listener)
                                                    throws IOException {
        final boolean[] closed = new boolean[] { false };
        PipedOutputStream pos = new PipedOutputStream() {
            @Override public void close() throws IOException {
                closed[0] = true;
                super.close();
            }
        };
        PipedInputStream pis = new PipedInputStream(pos);
        OutputStream dummyOS = new OutputStream() {
            @Override
            public void write(int b) throws IOException {}
        };
        final ClientConnection cc = new ClientConnection(pis, dummyOS);
        Thread t = new Thread() {
            @Override public void run() {
                try {
                    cc.runEventLoop(listener);
                } catch (IOException ex) {
                    if (!closed[0]) {
                        ex.printStackTrace();
                        success[0] = false;
                    }
                }
            }
        };
        t.start();
        pos.write("Protocol-Version: 1\r\n".getBytes());
        return pos;
    }

    private byte[] generateChunk(int numChunks, int chunkSize) {
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < numChunks; c++) {
            sb.append(generateChunk(chunkSize));
        }
        return sb.toString().getBytes();
    }
    
    private String generateChunk(int chunkSize) {
        StringBuilder sb = new StringBuilder("Content-Length: ");
        sb.append(chunkSize);
        sb.append("\r\n\r\n");
        sb.append(CHUNK1);
        int n = chunkSize - MIN_CHUNK_LENGTH;
        Random rnd = new Random();
        for (int i = 0; i < n; i++) {
            sb.append((char) ('a' + rnd.nextInt(26)));
        }
        sb.append(CHUNK2);
        return sb.toString();
    }
    
    @Test
    public void unicodeTest() throws Exception {
        final boolean[] success = new boolean[] { true };
        LastResponseListener lrl = new LastResponseListener();
        PipedOutputStream pos = createPipedConnection(success, lrl);
        int[] codePoints = new int[] { 0 };
        for (int c = Character.MIN_CODE_POINT; c <= Character.MAX_CODE_POINT; c++) {
            if (0xD800 <= c && c <= 0xDFFF) {
                // Invalid unicode characters
                continue;
            }
            if ('\"' == c || '\\' == c) {
                continue;
            }
            codePoints[0] = c;
            String str = new String(codePoints, 0, 1);
            String msg = EVENT1 + str + EVENT2;
            byte[] messageBytes = msg.getBytes(DebuggerConnection.CHAR_SET);
            pos.write("Content-Length: ".getBytes());
            pos.write(Integer.toString(messageBytes.length).getBytes());
            pos.write(DebuggerConnection.EOL);
            pos.write(DebuggerConnection.EOL);
            pos.write(messageBytes);
            // Write something more so that we do not stuck when trying to read more bytes...
            pos.write("Content-Length: ".getBytes());
            pos.write(Integer.toString(CONTINUE.length()).getBytes());
            pos.write(DebuggerConnection.EOL);
            pos.write(DebuggerConnection.EOL);
            pos.write(CONTINUE.getBytes(DebuggerConnection.CHAR_SET));
            pos.flush();
            V8Event event = lrl.getLastEvent();
            String line = "abc('"+str+"');";
            Assert.assertTrue("Failure detected.", success[0]);
            String eventText = ((BreakEventBody) event.getBody()).getSourceLineText();
            Assert.assertEquals("Failure for code point = U+"+Integer.toHexString(c)+" = '"+str+"'"+
                                ", but event code point = "+
                                   (eventText.length() > 6 ?
                                           "U+"+Integer.toHexString(eventText.codePointAt(6)) :
                                           "none."),
                                line, eventText);
        }
    }
    
    private static class DummyListener implements ClientConnection.Listener {

        public DummyListener() {
        }

        @Override
        public void header(Map<String, String> properties) {}

        @Override
        public void response(V8Response response) {}

        @Override
        public void event(V8Event event) {}
    }
    
    private static class LastResponseListener implements ClientConnection.Listener {
        
        private final Object lastResponseLock = new Object();
        private final Object lastEventLock = new Object();
        private V8Response lastResponse;
        private V8Event lastEvent;

        @Override
        public void header(Map<String, String> properties) {}

        @Override
        public void response(V8Response response) {
            synchronized(lastResponseLock) {
                lastResponse = response;
                lastResponseLock.notifyAll();
            }
        }
        
        public V8Response getLastResponse() {
            V8Response response;
            synchronized (lastResponseLock) {
                if (lastResponse == null) {
                    try {
                        lastResponseLock.wait(1000);
                    } catch (InterruptedException ex) {}
                }
                response = lastResponse;
                lastResponse = null;
            }
            return response;
        }

        @Override
        public void event(V8Event event) {
            synchronized(lastEventLock) {
                lastEvent = event;
                lastEventLock.notifyAll();
            }
        }
        
        public V8Event getLastEvent() {
            V8Event event;
            synchronized (lastEventLock) {
                if (lastEvent == null) {
                    try {
                        lastEventLock.wait(1000);
                    } catch (InterruptedException ex) {}
                }
                event = lastEvent;
                lastEvent = null;
            }
            return event;
        }

    }
}
