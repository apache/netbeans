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
package org.netbeans.modules.docker;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class ChunkedInputStreamTest extends NbTestCase {

    public ChunkedInputStreamTest(String name) {
        super(name);
    }

    public void testSimple() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String data = "{\"status\":\"start\",\"id\":\"7ec0c471084729a05270be99fd8450d3e515587d9755f97e15e74a227b4e12a6\",\"from\":\"ubuntu:latest\",\"time\":1447673048}";

        ByteArrayInputStream bis = new ByteArrayInputStream(createChunk(createChunk(null, data, charset), "", charset));
        ChunkedInputStream is = new ChunkedInputStream(bis);
        InputStreamReader r = new InputStreamReader(is, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        int ch = -1;
        while ((ch = r.read()) != -1) {
            sb.append((char) ch);
        }
        assertEquals(data, sb.toString());
    }

    public void testComplex() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String data1 = "{\"status\":\"die\",\"id\":\"7ec0c471084729a05270be99fd8450d3e515587d9755f97e15e74a227b4e12a6\",\"from\":\"ubuntu:latest\",\"time\":1447666993}";
        String data2 = "{\"status\":\"stop\",\"id\":\"7ec0c471084729a05270be99fd8450d3e515587d9755f97e15e74a227b4e12a6\",\"from\":\"ubuntu:latest\",\"time\":1447666993}";
        String data3 = "{\"status\":\"start\",\"id\":\"7ec0c471084729a05270be99fd8450d3e515587d9755f97e15e74a227b4e12a6\",\"from\":\"ubuntu:latest\",\"time\":1447667011}";
        String data4 = "{\"status\":\"die\",\"id\":\"7ec0c471084729a05270be99fd8450d3e515587d9755f97e15e74a227b4e12a6\",\"from\":\"ubuntu:latest\",\"time\":1447667083}";
        String data5 = "{\"status\":\"stop\",\"id\":\"7ec0c471084729a05270be99fd8450d3e515587d9755f97e15e74a227b4e12a6\",\"from\":\"ubuntu:latest\",\"time\":1447667083}";
        String data6 = "{\"status\":\"start\",\"id\":\"7ec0c471084729a05270be99fd8450d3e515587d9755f97e15e74a227b4e12a6\",\"from\":\"ubuntu:latest\",\"time\":1447667146}";
        String data7 = "{\"status\":\"die\",\"id\":\"7ec0c471084729a05270be99fd8450d3e515587d9755f97e15e74a227b4e12a6\",\"from\":\"ubuntu:latest\",\"time\":1447667315}";

        ByteArrayInputStream bis = new ByteArrayInputStream(
                createChunk(createChunk(createChunk(createChunk(createChunk(createChunk(createChunk(createChunk(
                        null, data1, charset), data2, charset), data3, charset), data4, charset), data5, charset), data6, charset), data7, charset), "", charset));
        ChunkedInputStream is = new ChunkedInputStream(bis);
        InputStreamReader r = new InputStreamReader(is, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        int ch = -1;
        while ((ch = r.read()) != -1) {
            sb.append((char) ch);
        }
        assertEquals(data1 + data2 + data3 + data4 + data5 + data6 + data7, sb.toString());
    }

    public void testUnfinished() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String data = "{\"status\":\"die\",\"id\":\"7ec0c471084729a05270be99fd8450d3e515587d9755f97e15e74a227b4e12a6\",\"from\":\"ubuntu:latest\",\"time\":1447666993}";

        ByteArrayInputStream bis = new ByteArrayInputStream(createChunk(null, data, charset));
        ChunkedInputStream is = new ChunkedInputStream(bis);
        InputStreamReader r = new InputStreamReader(is, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        int count = data.length();
        while (count > 0) {
            sb.append((char) r.read());
            count--;
        }
        assertEquals(data, sb.toString());
    }

    public void testBlocking() throws Exception {
        Charset charset = StandardCharsets.UTF_8;
        String data = "{\"status\":\"die\",\"id\":\"7ec0c471084729a05270be99fd8450d3e515587d9755f97e15e74a227b4e12a6\",\"from\":\"ubuntu:latest\",\"time\":1447666993}";

        ByteArrayInputStream bis = new ByteArrayInputStream(createChunk(null, data, charset));
        FilterInputStream fis = new FilterInputStream(bis) {
            @Override
            public synchronized int read(byte[] b, int off, int len) throws IOException {
                int ret = in.read(b, off, len);
                if (ret < 0) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        throw new IOException(ex);
                    }
                }
                return ret;
            }

            @Override
            public synchronized int read() throws IOException {
                int ret = in.read();
                if (ret < 0) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        throw new IOException(ex);
                    }
                }
                return ret;
            }
        };
        ChunkedInputStream is = new ChunkedInputStream(fis);
        InputStreamReader r = new InputStreamReader(is, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        int count = data.length();
        while (count > 0) {
            sb.append((char) r.read());
            count--;
        }
        assertEquals(data, sb.toString());
    }

    private byte[] createChunk(byte[] previous, String data, Charset charset) throws UnsupportedEncodingException {
        byte[] bytes = data.getBytes(charset);
        String size = Integer.toString(bytes.length, 16) + "\r\n";
        byte[] sizeBytes = size.getBytes(StandardCharsets.ISO_8859_1);
        int arraySize = sizeBytes.length + bytes.length + 2;
        if (previous != null) {
            arraySize += previous.length;
        }
        byte[] result = new byte[arraySize];
        int start = 0;
        if (previous != null) {
            System.arraycopy(previous, 0, result, 0, previous.length);
            start = previous.length;
        }
        System.arraycopy(sizeBytes, 0, result, start, sizeBytes.length);
        System.arraycopy(bytes, 0, result, start + sizeBytes.length, bytes.length);
        result[result.length - 2] = 0x0d;
        result[result.length - 1] = 0x0a;
        return result;
    }
}
