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

package org.netbeans.api.extexecution.print;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.input.TestInputWriter;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class InputProcessorsTest extends NbTestCase {

    public InputProcessorsTest(String name) {
        super(name);
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
