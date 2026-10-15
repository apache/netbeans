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
package org.netbeans.modules.docker;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

/**
 *
 * @author Petr Hejl
 */
public class ChunkedOutputStream extends FilterOutputStream {

    public ChunkedOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write((Integer.toHexString(len) + "\r\n").getBytes(ISO_8859_1));
        out.write(b, off, len);
        out.write("\r\n".getBytes(ISO_8859_1));
    }

    @Override
    public void write(int b) throws IOException {
        out.write("1\r\n".getBytes(ISO_8859_1));
        out.write(b);
        out.write("\r\n".getBytes(ISO_8859_1));
    }

    public void finish() throws IOException {
        out.write("0\r\n\r\n".getBytes(ISO_8859_1));
    }
}
