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
package org.netbeans.modules.gradle.execute;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author lkishalmi
 */
public class EscapeProcessingOutputStreamTest {

    private static final String COLORED_TEST = "Hello \033[31mRed Robin\033[0m.";
    private static final String UTF8_TEST = "大通西（２０～２８丁目）";

    private static class DummyEscapeProcessor implements EscapeProcessor {

        final StringBuilder output = new StringBuilder();

        @Override
        public void processCommand(String sequence, char command, int... args) {
        }

        @Override
        public void processText(String text) {
            output.append(text);
        }

        public String toString() {
            return output.toString();
        }
    }

    @Test
    public void testUTF8() {
        DummyEscapeProcessor ep = new DummyEscapeProcessor();
        try (EscapeProcessingOutputStream os = new EscapeProcessingOutputStream(ep)) {
            os.write(UTF8_TEST.getBytes(StandardCharsets.UTF_8));
            os.flush();
        } catch(IOException ex) {
            fail(ex.getMessage());
        }
        assertEquals(UTF8_TEST, ep.toString());
    }

    @Test
    public void testEscape() {
        DummyEscapeProcessor ep = new DummyEscapeProcessor();
        try (EscapeProcessingOutputStream os = new EscapeProcessingOutputStream(ep)) {
            os.write(COLORED_TEST.getBytes(StandardCharsets.UTF_8));
        } catch(IOException ex) {
            fail(ex.getMessage());
        }
        assertEquals("Hello Red Robin.", ep.toString());
    }

    @Test
    public void testMultiLine() {
        DummyEscapeProcessor ep = new DummyEscapeProcessor();
        try (PrintWriter out = new PrintWriter(new EscapeProcessingOutputStream(ep), true)) {
            out.print("Line1");
            assertEquals("", ep.toString());
            out.println();
            assertEquals("Line1\n", ep.toString());
            out.print("Line2");
        }
        assertEquals("Line1\nLine2", ep.toString());
    }

    @Test
    public void testMultiLine2() {
        DummyEscapeProcessor ep = new DummyEscapeProcessor();
        try (PrintWriter out = new PrintWriter(new EscapeProcessingOutputStream(ep), true)) {
            out.print("Line1\015\012Line2");
        }
        assertEquals("Line1\nLine2", ep.toString());
    }
}
