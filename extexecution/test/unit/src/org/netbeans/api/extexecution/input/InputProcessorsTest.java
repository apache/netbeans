/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.api.extexecution.input;

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
        assertClosedConditions(bridge);
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
        assertClosedConditions(proxy);

        assertTrue(processor1.isClosed());
        assertTrue(processor2.isClosed());
    }

    public void testPrinting() throws IOException {
        TestInputWriter writer = new TestInputWriter(new PrintWriter(new ByteArrayOutputStream()));
        InputProcessor processor = InputProcessors.printing(writer, true);

        processor.processInput("pre".toCharArray());
        assertEquals("pre", writer.getPrintedRaw());
        processor.processInput("test1\n".toCharArray());
        assertEquals("pretest1\n", writer.getPrintedRaw());
        processor.processInput("test2\n".toCharArray());
        assertEquals("pretest1\ntest2\n", writer.getPrintedRaw());
        processor.processInput("test3".toCharArray());
        assertEquals("pretest1\ntest2\ntest3", writer.getPrintedRaw());

        assertEquals(0, writer.getResetsProcessed());
        processor.reset();
        assertEquals(1, writer.getResetsProcessed());

        processor.processInput("\n".toCharArray());

        processor.close();
        assertClosedConditions(processor);
    }

    public void testPrintingCloseOrdering() throws IOException {
        final TestInputWriter writer = new TestInputWriter(new PrintWriter(new ByteArrayOutputStream()));
        final InputProcessor delegate = InputProcessors.printing(writer, false);

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
        assertClosedConditions(processor);
    }

    private static <T> void assertEquals(List<T> expected, List<T> value) {
        assertEquals(expected.size(), value.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), value.get(i));
        }
    }

    private static void assertClosedConditions(InputProcessor inputProcessor) throws IOException {
        try {
            inputProcessor.processInput(new char[] {'0'});
            fail("Does not throw IllegalStateException after close");
        } catch (IllegalStateException ex) {
            // expected
        }

        try {
            inputProcessor.reset();
            fail("Does not throw IllegalStateException after close");
        } catch (IllegalStateException ex) {
            // expected
        }
    }
}
