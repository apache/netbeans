/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.extexecution.input;

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
