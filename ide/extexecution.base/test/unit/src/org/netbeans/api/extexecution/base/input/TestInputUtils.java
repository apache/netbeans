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

package org.netbeans.api.extexecution.base.input;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Random;

/**
 *
 * @author Petr Hejl
 */
public final class TestInputUtils {

    private TestInputUtils() {
        super();
    }

    public static InputStream prepareInputStream(String[] lines, String separator,
            Charset charset, boolean terminate) {

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < lines.length; i++) {
            buffer.append(lines[i]);
            if (terminate || i < (lines.length - 1)) {
                buffer.append(separator);
            }
        }

        ByteBuffer byteBuffer = charset.encode(buffer.toString());
        int length = byteBuffer.limit();
        byte[] byteArray = new byte[length];
        byteBuffer.position(0);
        byteBuffer.get(byteArray);

        return prepareInputStream(byteArray);
    }

    public static InputStream prepareInputStream(char[] chars, Charset charset) {
        CharBuffer wrapped = CharBuffer.wrap(chars);
        ByteBuffer buffer = charset.encode(wrapped);
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        return prepareInputStream(bytes);
    }
    
    private static InputStream prepareInputStream(byte[] bytes) {
        return new ByteArrayInputStream(bytes.clone());
    }

    public static File prepareFile(String name, File workDir,
            String[] lines, String separator, Charset charset, boolean terminate) throws IOException {

        File file = new File(workDir, name);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
        try {
            for (int i = 0; i < lines.length; i++) {
            writer.write(lines[i]);
            if (terminate || i < (lines.length - 1)) {
                writer.write(separator);
            }
            }
        } finally {
            writer.close();
        }
        return file;
    }

    public static File prepareFile(String name, File workDir, char[] chars,
            Charset charset) throws IOException {
        
        File file = new File(workDir, name);
        Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), charset);
        try {
            writer.write(chars);
        } finally {
            writer.close();
        }
        return file;
    }

    public static class EndlessAsciiInputStream extends InputStream {

        private final Random random = new Random();

        @Override
        public int read() throws IOException {
            return random.nextInt(256);
        }

        @Override
        public int available() throws IOException {
            return 1;
        }

    }

}
