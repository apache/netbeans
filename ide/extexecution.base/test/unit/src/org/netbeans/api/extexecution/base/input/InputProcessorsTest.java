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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class InputProcessorsTest extends NbTestCase {

    private static final char[] PROXY_CHARS_CHUNK1 = "abcdefghij".toCharArray();

    private static final char[] PROXY_CHARS_CHUNK2 = "jihgfedcba".toCharArray();

    private static final char[][] PROXY_TEST_CHARS = new char[][] {
        PROXY_CHARS_CHUNK1, PROXY_CHARS_CHUNK2
    };

    private static final List<String> BRIDGE_TEST_LINES = new ArrayList<String>();

    private static final char[][] BRIDGE_TEST_CHARS;

    static {
        Collections.addAll(BRIDGE_TEST_LINES, "test1", "test2");

        BRIDGE_TEST_CHARS = new char[BRIDGE_TEST_LINES.size()][];
        for (int i = 0; i < BRIDGE_TEST_LINES.size(); i++) {
            BRIDGE_TEST_CHARS[i] = (BRIDGE_TEST_LINES.get(i) + "\n").toCharArray();
        }
    }

    public InputProcessorsTest(String name) {
        super(name);
    }

    public void testBridge() throws IOException {
        TestLineProcessor processor = new TestLineProcessor(false);
        InputProcessor bridge = InputProcessors.bridge(processor);

        for (char[] chunk : BRIDGE_TEST_CHARS) {
            bridge.processInput(chunk);
        }

        assertEquals(0, processor.getResetCount());
        assertEquals(BRIDGE_TEST_LINES, processor.getLinesProcessed());

        bridge.reset();
        assertEquals(1, processor.getResetCount());

        bridge.close();
        assertClosedConditions(bridge, true);
        assertTrue(processor.isClosed());
    }

    public void testProxy() throws IOException {
        TestInputProcessor processor1 = new TestInputProcessor(false);
        TestInputProcessor processor2 = new TestInputProcessor(false);

        InputProcessor proxy = InputProcessors.proxy(processor1, processor2);
        int size = 0;
        for (char[] chunk : PROXY_TEST_CHARS) {
            proxy.processInput(chunk);
            size += chunk.length;
        }

        char[] expected = new char[size];
        int position = 0;
        for (char[] chunk : PROXY_TEST_CHARS) {
            System.arraycopy(chunk, 0, expected, position, chunk.length);
            position += chunk.length;
        }

        assertEquals(0, processor1.getResetCount());
        assertEquals(0, processor2.getResetCount());

        assertTrue(Arrays.equals(expected, processor1.getCharsProcessed()));
        assertTrue(Arrays.equals(expected, processor2.getCharsProcessed()));

        proxy.reset();

        assertEquals(1, processor1.getResetCount());
        assertEquals(1, processor2.getResetCount());

        proxy.close();
        assertClosedConditions(proxy, true);

        assertTrue(processor1.isClosed());
        assertTrue(processor2.isClosed());
    }

    public void testPrinting() throws IOException {
        TestInputWriter writer = new TestInputWriter(new PrintWriter(new ByteArrayOutputStream()));
        InputProcessor processor = InputProcessors.printing(writer);

        processor.processInput("pre".toCharArray());
        assertEquals("pre", writer.getPrintedRaw());
        processor.processInput("test1\n".toCharArray());
        assertEquals("pretest1\n", writer.getPrintedRaw());
        processor.processInput("test2\n".toCharArray());
        assertEquals("pretest1\ntest2\n", writer.getPrintedRaw());
        processor.processInput("test3".toCharArray());
        assertEquals("pretest1\ntest2\ntest3", writer.getPrintedRaw());

        processor.processInput("\n".toCharArray());

        processor.close();
        assertClosedConditions(processor, false);
    }

    public void testPrintingCloseOrdering() throws IOException {
        final TestInputWriter writer = new TestInputWriter(new PrintWriter(new ByteArrayOutputStream()));
        final InputProcessor delegate = InputProcessors.printing(writer);

        InputProcessor processor = new InputProcessor() {

            public void processInput(char[] chars) throws IOException {
                delegate.processInput(chars);
            }

            public void reset() throws IOException {
                delegate.reset();
            }

            public void close() throws IOException {
                delegate.processInput("closing mark".toCharArray());
                delegate.close();
            }
        };


        processor.processInput("first".toCharArray());
        assertEquals("first", writer.getPrintedRaw());
        processor.processInput("second\n".toCharArray());
        assertEquals("firstsecond\n", writer.getPrintedRaw());

        processor.close();
        assertEquals("firstsecond\nclosing mark", writer.getPrintedRaw());
        assertClosedConditions(processor, false);
    }

    private static <T> void assertEquals(List<T> expected, List<T> value) {
        assertEquals(expected.size(), value.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), value.get(i));
        }
    }

    private static void assertClosedConditions(InputProcessor inputProcessor,
            boolean reset) throws IOException {

        try {
            inputProcessor.processInput(new char[] {'0'});
            fail("Does not throw IllegalStateException after close");
        } catch (IllegalStateException ex) {
            // expected
        }

        if (reset) {
            try {
                inputProcessor.reset();
                fail("Does not throw IllegalStateException after close");
            } catch (IllegalStateException ex) {
                // expected
            }
        }
    }
}
