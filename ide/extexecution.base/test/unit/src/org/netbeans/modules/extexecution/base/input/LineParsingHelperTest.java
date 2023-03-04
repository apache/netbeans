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

package org.netbeans.modules.extexecution.base.input;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class LineParsingHelperTest extends NbTestCase {

    private static final String[] TEST_LINES = new String[] {"line1", "line2", "line3"}; // NOI18N

    private static final String[] TEST_SEPARATORS = new String[] {"\n", "\r", "\r\n"}; // NOI18N

    private static final int EXTENDED_LENGTH = 10;

    public LineParsingHelperTest(String name) {
        super(name);
    }

    public void testParsingCharacterIterator() {
        for (String separator : TEST_SEPARATORS) {
            StringBuffer testInput = new StringBuffer();
            for (String line : TEST_LINES) {
                testInput.append(line).append(separator);
            }

            LineParsingHelper helper = new LineParsingHelper();
            String[] lines = helper.parse(testInput);
            checkParsingResults(lines, helper);
        }
    }

    public void testParsingCharacterArray() {
        for (String separator : TEST_SEPARATORS) {
            StringBuffer testInput = new StringBuffer();
            for (String line : TEST_LINES) {
                testInput.append(line).append(separator);
            }

            LineParsingHelper helper = new LineParsingHelper();
            char[] characterTestInput = new char[testInput.length()];
            testInput.getChars(0, testInput.length(), characterTestInput, 0);
            String[] lines = helper.parse(characterTestInput);
            checkParsingResults(lines, helper);

            characterTestInput = new char[testInput.length() + EXTENDED_LENGTH];
            testInput.getChars(0, testInput.length(), characterTestInput, 0);
            lines = helper.parse(characterTestInput, 0, testInput.length());
            checkParsingResults(lines, helper);
        }
    }

    public void testTrailingLine() {
        String testLine = "line1\nline2\nline3"; // NOI18N
        LineParsingHelper helper = new LineParsingHelper();
        String[] lines = helper.parse(testLine);

        assertEquals(2, lines.length);
        assertEquals("line1", lines[0]); // NOI18N
        assertEquals("line2", lines[1]); // NOI18N

        assertEquals("line3", helper.getTrailingLine(false));
        assertEquals("line3", helper.getTrailingLine(true));
        assertEquals(null, helper.getTrailingLine(true));

        testLine = "line1\nline2\nline3\n";
        helper.parse(testLine);
        assertEquals(null, helper.getTrailingLine(true));
    }

    private void checkParsingResults(String[] lines, LineParsingHelper helper) {
        assertEquals(TEST_LINES.length, lines.length);

        for (int i = 0; i < TEST_LINES.length; i++) {
            assertEquals(TEST_LINES[i], lines[i]);
        }

        assertEquals(null, helper.getTrailingLine(false));
        assertEquals(null, helper.getTrailingLine(true));
    }
}
