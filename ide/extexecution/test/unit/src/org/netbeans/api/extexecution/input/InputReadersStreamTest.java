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
import org.netbeans.api.extexecution.input.InputReaders;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class InputReadersStreamTest extends NbTestCase {

    private static final char[] TEST_CHARS = "abcdefghij".toCharArray();

    private static final int MAX_RETRIES = TEST_CHARS.length * 2;

    private static final Charset TEST_CHARSET = StandardCharsets.UTF_8;

    public InputReadersStreamTest(String name) {
        super(name);
    }

    public void testReadInput() throws IOException {
        InputReader reader = InputReaders.forStream(TestInputUtils.prepareInputStream(
                TEST_CHARS, TEST_CHARSET), TEST_CHARSET);
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

    public void testFactory() {
        try {
            InputReaders.forStream(null, TEST_CHARSET);
            fail("Accepts null stream"); // NOI18N
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testClose() throws IOException {
        InputReader reader = InputReaders.forStream(TestInputUtils.prepareInputStream(
                TEST_CHARS, TEST_CHARSET), TEST_CHARSET);
        reader.close();

        try {
            reader.readInput(null);
            fail("Reader not throw exception on read after closing it"); // NOI18N
        } catch (IllegalStateException ex) {
            // expected
        }
    }
}
