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

package org.netbeans.api.extexecution.input;

import org.netbeans.api.extexecution.input.InputReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.extexecution.input.InputReaders.FileInput;

/**
 *
 * @author Petr Hejl
 */
public class InputReadersFileTest extends NbTestCase {

    private static final char[] TEST_CHARS = "abcdefghij".toCharArray();

    private static final char[] TEST_CHARS_ROTATE = "jihgfedcba".toCharArray();

    private static final Charset TEST_CHARSET = StandardCharsets.UTF_8;

    private static final int MAX_RETRIES = TEST_CHARS.length * 2;

    private File byteFile;

    private File byteFileRotate;

    public InputReadersFileTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        byteFile = TestInputUtils.prepareFile(
                "testFile.txt", getWorkDir(), TEST_CHARS, TEST_CHARSET);
        byteFileRotate = TestInputUtils.prepareFile(
                "testFileRotate.txt", getWorkDir(), TEST_CHARS_ROTATE, TEST_CHARSET);
    }

    public void testReadInput() throws IOException {
        final FileInput fileInput = new FileInput(byteFile, TEST_CHARSET);
        InputReader reader = InputReaders.forFileInputProvider(new InputReaders.FileInput.Provider() {

            public FileInput getFileInput() {
                return fileInput;
            }
        });
        TestInputProcessor processor = new TestInputProcessor(false);

        int read = 0;
        int retries = 0;
        while (read < TEST_CHARS.length && retries < MAX_RETRIES) {
            read += reader.readInput(processor);
            retries++;
        }

        assertEquals(read, TEST_CHARS.length);
        assertEquals(0, processor.getResetCount());

        assertTrue(Arrays.equals(TEST_CHARS, processor.getCharsProcessed()));
    }

    public void testRotation() throws IOException {
        TestProvider provider = new TestProvider(byteFile, TEST_CHARSET);

        InputReader outputReader = InputReaders.forFileInputProvider(provider);
        TestInputProcessor processor = new TestInputProcessor(true);

        int read = 0;
        int retries = 0;
        while (read < TEST_CHARS.length && retries < MAX_RETRIES) {
            read += outputReader.readInput(processor);
            retries++;
        }

        assertEquals(read, TEST_CHARS.length);
        assertEquals(0, processor.getResetCount());

        assertTrue(Arrays.equals(TEST_CHARS, processor.getCharsProcessed()));

        // file rotation
        provider.setFile(byteFileRotate);

        read = 0;
        retries = 0;
        while (read < TEST_CHARS_ROTATE.length && retries < MAX_RETRIES) {
            read += outputReader.readInput(processor);
            retries++;
        }

        assertEquals(read, TEST_CHARS_ROTATE.length);
        assertEquals(1, processor.getResetCount());

        assertTrue(Arrays.equals(TEST_CHARS_ROTATE, processor.getCharsProcessed()));
    }

    public void testFactory() {
        try {
            InputReaders.forFile(null, null);
            fail("Accepts null file generator"); // NOI18N
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testClose() throws IOException {
        final FileInput fileInput = new FileInput(byteFile, TEST_CHARSET);
        InputReader reader = InputReaders.forFileInputProvider(new InputReaders.FileInput.Provider() {

            public FileInput getFileInput() {
                return fileInput;
            }
        });
        reader.close();

        try {
            reader.readInput(null);
            fail("Reader not throw exception on read after closing it"); // NOI18N
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    private static class TestProvider implements InputReaders.FileInput.Provider {

        private final Charset charset;

        private FileInput fileInput;

        public TestProvider(File file, Charset charset) {
            this.charset = charset;
            setFile(file);
        }

        public final FileInput getFileInput() {
            return fileInput;
        }

        public final void setFile(File file) {
            this.fileInput = new FileInput(file, charset);
        }

    }
}
