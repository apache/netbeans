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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.netbeans.api.extexecution.base.input.LineProcessor;
import org.netbeans.api.extexecution.input.TestInputWriter;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class LineProcessorsTest extends NbTestCase {

    private static final List<String> PRINTING_TEST_LINES = new ArrayList<String>(5);

    static {

        Collections.addAll(PRINTING_TEST_LINES,
                "the first test line",
                "the second test line",
                "the third test line",
                "the fourth test line",
                "the fifth test line");
    }

    private ExecutorService executor;

    public LineProcessorsTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        executor = Executors.newCachedThreadPool();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        executor.shutdownNow();
    }

    public void testPrinting() {
        TestInputWriter writer = new TestInputWriter(new PrintWriter(new ByteArrayOutputStream()));
        LineProcessor lineProcessor = LineProcessors.printing(writer, true);
        for (String line : PRINTING_TEST_LINES) {
            lineProcessor.processLine(line);
        }
        assertEquals(PRINTING_TEST_LINES, writer.getPrinted());

        lineProcessor.reset();
        assertEquals(1, writer.getResetsProcessed());

        for (String line : PRINTING_TEST_LINES) {
            lineProcessor.processLine(line);
        }
        assertEquals(PRINTING_TEST_LINES, writer.getPrinted());

        writer = new TestInputWriter(new PrintWriter(new ByteArrayOutputStream()));
        lineProcessor = LineProcessors.printing(writer, false);
        lineProcessor.reset();
        assertEquals(0, writer.getResetsProcessed());

        lineProcessor.close();
        assertClosedConditions(lineProcessor);
    }

    public void testPrintingCloseOrdering() {
        final TestInputWriter writer = new TestInputWriter(new PrintWriter(new ByteArrayOutputStream()));
        final LineProcessor delegate = LineProcessors.printing(writer, false);

        LineProcessor lineProcessor = new LineProcessor() {

            public void processLine(String line) {
                delegate.processLine(line);
            }

            public void reset() {
                delegate.reset();
            }

            public void close() {
                delegate.processLine("closing mark");
                delegate.close();
            }
        };

        for (String line : PRINTING_TEST_LINES) {
            lineProcessor.processLine(line);
        }
        assertEquals(PRINTING_TEST_LINES, writer.getPrinted());

        lineProcessor.close();
        List<String> printed = new ArrayList<String>(PRINTING_TEST_LINES);
        printed.add("closing mark");
        assertEquals(printed, writer.getPrinted());
        assertClosedConditions(lineProcessor);
    }

    private static <T> void assertEquals(List<T> expected, List<T> value) {
        assertEquals(expected.size(), value.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), value.get(i));
        }
    }

    private static void assertClosedConditions(LineProcessor lineProcessor) {
        try {
            lineProcessor.processLine("something");
            fail("Does not throw IllegalStateException after close");
        } catch (IllegalStateException ex) {
            // expected
        }

        try {
            lineProcessor.reset();
            fail("Does not throw IllegalStateException after close");
        } catch (IllegalStateException ex) {
            // expected
        }
    }
}
