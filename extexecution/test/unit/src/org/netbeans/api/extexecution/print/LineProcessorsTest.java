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
