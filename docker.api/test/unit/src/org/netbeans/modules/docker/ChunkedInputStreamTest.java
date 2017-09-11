/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
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
        Charset charset = Charset.forName("UTF-8");
        String data = "{\"status\":\"start\",\"id\":\"7ec0c471084729a05270be99fd8450d3e515587d9755f97e15e74a227b4e12a6\",\"from\":\"ubuntu:latest\",\"time\":1447673048}";

        ByteArrayInputStream bis = new ByteArrayInputStream(createChunk(createChunk(null, data, charset), "", charset));
        ChunkedInputStream is = new ChunkedInputStream(bis);
        InputStreamReader r = new InputStreamReader(is, "UTF-8");
        StringBuilder sb = new StringBuilder();
        int ch = -1;
        while ((ch = r.read()) != -1) {
            sb.append((char) ch);
        }
        assertEquals(data, sb.toString());
    }

    public void testComplex() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
        InputStreamReader r = new InputStreamReader(is, "UTF-8");
        StringBuilder sb = new StringBuilder();
        int ch = -1;
        while ((ch = r.read()) != -1) {
            sb.append((char) ch);
        }
        assertEquals(data1 + data2 + data3 + data4 + data5 + data6 + data7, sb.toString());
    }

    public void testUnfinished() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String data = "{\"status\":\"die\",\"id\":\"7ec0c471084729a05270be99fd8450d3e515587d9755f97e15e74a227b4e12a6\",\"from\":\"ubuntu:latest\",\"time\":1447666993}";

        ByteArrayInputStream bis = new ByteArrayInputStream(createChunk(null, data, charset));
        ChunkedInputStream is = new ChunkedInputStream(bis);
        InputStreamReader r = new InputStreamReader(is, "UTF-8");
        StringBuilder sb = new StringBuilder();
        int count = data.length();
        while (count > 0) {
            sb.append((char) r.read());
            count--;
        }
        assertEquals(data, sb.toString());
    }

    public void testBlocking() throws Exception {
        Charset charset = Charset.forName("UTF-8");
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
        InputStreamReader r = new InputStreamReader(is, "UTF-8");
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
        byte[] sizeBytes = size.getBytes("ISO-8859-1");
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
