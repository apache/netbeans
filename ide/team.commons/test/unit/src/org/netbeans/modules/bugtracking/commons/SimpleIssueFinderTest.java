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

package org.netbeans.modules.bugtracking.commons;

import org.junit.After;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import static org.junit.Assert.*;

/**
 *
 * @author  Marian Petras
 */
public class SimpleIssueFinderTest extends NbTestCase {

    private SimpleIssueFinder issueFinder;

    public SimpleIssueFinderTest(String name) {
        super(name);
    }

    @After
    @Override
    public void tearDown() {
        issueFinder = null;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
    }



    @Test
    public void testGetIssueSpans() {
        issueFinder = SimpleIssueFinder.getTestInstance();

        checkNoIssueSpansFound("");
        checkNoIssueSpansFound("bug");
        checkNoIssueSpansFound("bug ");
        checkNoIssueSpansFound("bug #");
        checkNoIssueSpansFound("bug#");
        checkNoIssueSpansFound("bug# ");

        checkNoIssueSpansFound("bug##123456");
        checkNoIssueSpansFound("bug ##123456");
        checkNoIssueSpansFound("bug## 123456");
        checkNoIssueSpansFound("bug###123456");

        checkNoIssueSpansFound("sbug 123456");
        checkNoIssueSpansFound("sbug#123456");

        checkNoIssueSpansFound("123456");
        checkNoIssueSpansFound(" 123456");
        checkNoIssueSpansFound("  123456");
        checkNoIssueSpansFound("   123456");
        checkNoIssueSpansFound("bug123456"); 
        checkNoIssueSpansFound("bug#123456");
        checkNoIssueSpansFound("bug# 123456");
        checkNoIssueSpansFound("bug#  123456");

        checkIssueSpans("// #123456", "#123456");
        checkIssueSpans("/* #123456", "#123456");
        checkIssueSpans("//whatever #123456", "#123456");
        checkIssueSpans("/*#123456", "#123456");
        checkIssueSpans("//#123456", "#123456");
        
        checkIssueSpans("// #123456*78", "#123456");
        checkIssueSpans("/* #123456*78", "#123456");
        checkIssueSpans("#123456*78", "#123456");
        checkIssueSpans("#123456/78", "#123456");
        checkIssueSpans("#123456//78", "#123456");
        checkIssueSpans("#123456//", "#123456");
        checkIssueSpans("#123456*/", "#123456");
        
        checkIssueSpans("#123456", "#123456");
        checkIssueSpans("# 123456", "# 123456");
        checkIssueSpans(" #123456", "#123456");
        checkIssueSpans("  #123456", "#123456");
        checkIssueSpans(" # 123456", "# 123456");
        checkIssueSpans("#  123456", "#  123456");
        checkIssueSpans("bug 123456", "bug 123456");
        checkIssueSpans("bug  123456", "bug  123456");
        checkIssueSpans("bug #123456", "bug #123456");
        checkIssueSpans("bug   123456", "bug   123456");
        checkIssueSpans("bug  #123456", "bug  #123456");
        checkIssueSpans("bug # 123456", "bug # 123456");

        checkIssueSpans("Bug 123456", "Bug 123456");
        checkIssueSpans("BUG 123456", "BUG 123456");
        checkIssueSpans("Issue 123456", "Issue 123456");
        checkIssueSpans("ISSUE 123456", "ISSUE 123456");

        checkIssueSpans("// Bug #123456", "Bug #123456");
        checkIssueSpans("/* Bug #123456", "Bug #123456");
        checkIssueSpans("//Bug #123456", "Bug #123456");
        checkIssueSpans("/*Bug #123456", "Bug #123456");
        checkIssueSpans("Bug #123456//", "Bug #123456");
        checkIssueSpans("Bug #123456*/", "Bug #123456");
        
        checkIssueSpans("Bug #123456", "Bug #123456");
        checkIssueSpans("BUG #123456", "BUG #123456");
        checkIssueSpans("Issue #123456", "Issue #123456");
        checkIssueSpans("ISSUE #123456", "ISSUE #123456");

        checkIssueSpans("bug# #123456", "#123456");
        checkIssueSpans("sbug #123456", "#123456");

        checkIssueSpans("#67888 and #73573", "#67888", "#73573");
        checkIssueSpans("bugs #67888 and #73573", "#67888", "#73573");
        checkIssueSpans("issues #67888 and #73573", "#67888", "#73573");
        checkIssueSpans("bugs #67888, #12345 and #73573", "#67888", "#12345", "#73573");

        checkNoIssueSpansFound("#123cdE");
        checkNoIssueSpansFound("#123CDe");

        checkNoIssueSpansFound("#123cd G");
        checkNoIssueSpansFound("#123cd g");
        checkNoIssueSpansFound("#123cdG");
        checkNoIssueSpansFound("#123cdg");

        checkIssueSpans("bug\n123456", "bug\n123456");
        checkIssueSpans("* bug\n123456", "bug\n123456");
        checkIssueSpans("* bug\n 123456", "bug\n 123456");
        checkIssueSpans("* bug\n* 123456", "bug\n* 123456");
        checkIssueSpans("* bug\n * 123456", "bug\n * 123456");
        checkIssueSpans("* bug \n * 123456", "bug \n * 123456");
        checkIssueSpans("bug\n#123456", "bug\n#123456");
        checkIssueSpans("* bug\n#123456", "bug\n#123456");
        checkIssueSpans("* bug\n #123456", "bug\n #123456");
        checkIssueSpans("* bug\n* #123456", "bug\n* #123456");
        checkIssueSpans("* bug\n * #123456", "bug\n * #123456");

        checkIssueSpans("bug\n\n123456", "bug\n\n123456");
        checkIssueSpans("* bug\n\n123456", "bug\n\n123456");
        checkIssueSpans("* bug\n\n 123456", "bug\n\n 123456");
        checkIssueSpans("* bug\n\n* 123456", "bug\n\n* 123456");
        checkIssueSpans("* bug\n\n * 123456", "bug\n\n * 123456");
        checkIssueSpans("* bug \n\n * 123456", "bug \n\n * 123456");
        checkIssueSpans("bug\n\n#123456", "bug\n\n#123456");
        checkIssueSpans("* bug\n\n#123456", "bug\n\n#123456");
        checkIssueSpans("* bug\n\n #123456", "bug\n\n #123456");
        checkIssueSpans("* bug\n\n* #123456", "bug\n\n* #123456");
        checkIssueSpans("* bug\n\n * #123456", "bug\n\n * #123456");

        checkIssueSpans("bug\n* \n123456", "bug\n* \n123456");
        checkIssueSpans("bug\n * \n123456", "bug\n * \n123456");

        checkNoIssueSpansFound("* bug\n *123456");
        checkNoIssueSpansFound("* bug\n *#123456");
        checkNoIssueSpansFound("* bug\n *# 123456");
        checkNoIssueSpansFound("* bug\n *#  123456");

        checkNoIssueSpansFound("bug\n ** \n123456");

        checkIssueSpans("bug #123456\n", "bug #123456");

        checkIssueSpans("bug bug #123456", "bug #123456");
        checkIssueSpans("bug issue #123456", "issue #123456");
        checkIssueSpans("issue bug #123456", "bug #123456");
        checkIssueSpans("issue issue #123456", "issue #123456");

        checkIssueSpans("task-id: 123456", "task-id: 123456");
        checkIssueSpans("Task-ID: 123456", "Task-ID: 123456");
        checkIssueSpans("TaSk-Id: 123456", "TaSk-Id: 123456");
        checkIssueSpans("Task-Id: 123456", "Task-Id: 123456");
        checkIssueSpans("Task-Id 123456", "Task-Id 123456");
        
        /* -------- tests for special phrase "duplicate of" -------- */

        checkIssueSpans("duplicate of 123456", "123456");
        checkIssueSpans("duplicate of #123456", "#123456");
        checkIssueSpans("duplicate of # 123456", "# 123456");
        checkIssueSpans("duplicate of bug 123456", "bug 123456");
        checkIssueSpans("duplicate of issue 123456", "issue 123456");
        checkIssueSpans("duplicate duplicate of 123456", "123456");

        checkIssueSpans("DUPLICATE OF 123456", "123456");

        checkNoIssueSpansFound("duplicate of");
        checkNoIssueSpansFound("duplicate of ");
        checkNoIssueSpansFound("duplicate of #");
        checkNoIssueSpansFound("duplicate of bug");
        checkNoIssueSpansFound("duplicate of \n");
        checkNoIssueSpansFound("duplicate of \nbug");
        checkNoIssueSpansFound("of duplicate 123456");

        checkIssueSpans("duplicate of\n123456", "123456");
        checkIssueSpans("duplicate\nof 123456", "123456");
        checkIssueSpans("duplicate\nof\n123456", "123456");
        checkIssueSpans("duplicate\n* of 123456", "123456");
        checkIssueSpans("duplicate\n * of 123456", "123456");
        checkIssueSpans("duplicate\n  * of 123456", "123456");
        checkIssueSpans("duplicate \n* of 123456", "123456");
        checkIssueSpans("duplicate \n * of 123456", "123456");
        checkIssueSpans("duplicate \n  * of 123456", "123456");
        checkIssueSpans("duplicate  \n* of 123456", "123456");
        checkIssueSpans("duplicate  \n * of 123456", "123456");
        checkIssueSpans("duplicate  \n  * of 123456", "123456");
        checkIssueSpans("duplicate  \n  * of #123456", "#123456");
        checkIssueSpans("duplicate  \n  * of # 123456", "# 123456");
        checkIssueSpans("duplicate  \n  * of\n* \n # 123456", "# 123456");
        checkIssueSpans("duplicate  \n  * of bug 123456", "bug 123456");
        checkIssueSpans("duplicate  \n  * of issue 123456", "issue 123456");
    }

    @Test
    public void testGetIssueNumber() {
        issueFinder = SimpleIssueFinder.getTestInstance();

        testGetIssueNumber("#123456", "123456");
        testGetIssueNumber("# 123456", "123456");
        testGetIssueNumber(" #123456", "123456");
        testGetIssueNumber("  #123456", "123456");
        testGetIssueNumber(" # 123456", "123456");
        testGetIssueNumber("#  123456", "123456");
        testGetIssueNumber("bug 123456", "123456");
        testGetIssueNumber("bug  123456", "123456");
        testGetIssueNumber("bug #123456", "123456");
        testGetIssueNumber("bug   123456", "123456");
        testGetIssueNumber("bug  #123456", "123456");
        testGetIssueNumber("bug # 123456", "123456");

        testGetIssueNumber("bug# #123456", "123456");
        testGetIssueNumber("sbug #123456", "123456");

        checkNoIssueSpansFound("bug #abcdef");
        checkNoIssueSpansFound("bug #ABCDEF");

        checkNoIssueSpansFound("bug # abcdef");
        checkNoIssueSpansFound("# abcdef");

        testGetIssueNumber("Bug 123456", "123456");
        testGetIssueNumber("BUG 123456", "123456");
        testGetIssueNumber("Issue 123456", "123456");
        testGetIssueNumber("ISSUE 123456", "123456");
        testGetIssueNumber("Bug #123456", "123456");
        testGetIssueNumber("BUG #123456", "123456");
        testGetIssueNumber("Issue #123456", "123456");
        testGetIssueNumber("ISSUE #123456", "123456");
        
        testGetIssueNumber("duplicate of 123456", "123456");
        testGetIssueNumber("duplicate of #123456", "123456");
        testGetIssueNumber("duplicate of # 123456", "123456");
        testGetIssueNumber("duplicate of bug 123456", "123456");
        testGetIssueNumber("duplicate of issue 123456", "123456");

        testGetIssueNumber("DUPLICATE OF 123456", "123456");

        testGetIssueNumber("duplicate of\n123456", "123456");
        testGetIssueNumber("duplicate\nof 123456", "123456");
        testGetIssueNumber("duplicate\nof\n123456", "123456");
        testGetIssueNumber("duplicate\n* of 123456", "123456");
        testGetIssueNumber("duplicate\n * of 123456", "123456");
        testGetIssueNumber("duplicate\n  * of 123456", "123456");
        testGetIssueNumber("duplicate \n* of 123456", "123456");
        testGetIssueNumber("duplicate \n * of 123456", "123456");
        testGetIssueNumber("duplicate \n  * of 123456", "123456");
        testGetIssueNumber("duplicate  \n* of 123456", "123456");
        testGetIssueNumber("duplicate  \n * of 123456", "123456");
        testGetIssueNumber("duplicate  \n  * of 123456", "123456");
        testGetIssueNumber("duplicate  \n  * of #123456", "123456");
        testGetIssueNumber("duplicate  \n  * of # 123456", "123456");
        testGetIssueNumber("duplicate  \n  * of bug 123456", "123456");
        testGetIssueNumber("duplicate  \n  * of issue 123456", "123456");
    }

    @Test
    public void testBug176091() {
        issueFinder = SimpleIssueFinder.getTestInstance();

        checkIssueSpans("See issue 1446.", "issue 1446");
    }

    @Test
    public void testBug177290() {
        issueFinder = SimpleIssueFinder.getTestInstance();

        checkIssueSpans(
                "Seems like duplicate of 149393, thank you for report anyway.\n"
                + '\n'
                + "*** This issue has been marked as a duplicate of 149393 ***",
                "149393",
                "149393");
    }

    private void checkIssueSpans(String str, String... substr) {
        checkTestValidity(str != null);
        checkTestValidity(substr != null);
        
        int fromIndex = 0;

        int[] expBounds = new int[substr.length * 2];
        for (int i = 0; i < substr.length; i++) {
            int lowBound = str.indexOf(substr[i], fromIndex);
            checkTestValidity(lowBound != -1);
            int highBound = lowBound + substr[i].length();
            expBounds[2 * i] = lowBound;
            expBounds[2 * i + 1] = highBound;
            fromIndex = highBound;
        }
        checkIssueSpans(str, expBounds);
    }

    private void checkIssueSpans(String str, int... expectedBounds) {
        if ((expectedBounds == null) || (expectedBounds.length == 0)) {
            checkNoIssueSpansFound(str);
            return;
        }

        checkTestValidity(expectedBounds.length % 2 == 0);

        int[] spans = issueFinder.getIssueSpans(str); 
        assertNotNull(spans);
        assertTrue("incorrect bounds detected: "
                       + "expected: " + printArray(expectedBounds)
                       + ", real: " + (spans.length == 0 ? "none" : printArray(spans)),
                   equals(expectedBounds, spans));
    }

    private void checkNoIssueSpansFound(String str) {
        checkTestValidity(str != null);
        int[] spans = issueFinder.getIssueSpans(str);
        assertNotNull(spans);
        assertTrue("incorrect bounds detected for \"" + str + "\": "
                       + "no spans expected but got: " + printArray(spans),
                   spans.length == 0);
    }

    private static boolean equals(int[] expected, int[] real) {
        if ((expected == null) && (real == null)) {
            return true;
        }

        if ((expected == null) || (real == null)) {
            return false;
        }

        if (expected.length != real.length) {
            return false;
        }

        for (int i = 0; i < real.length; i++) {
            if (real[i] != expected[i]) {
                return false;
            }
        }

        return true;
    }

    private static String printArray(int... arr) {
        if (arr == null) {
            return "<null>";
        }

        if (arr.length == 0) {
            return "[]";
        }

        StringBuilder buf = new StringBuilder(arr.length * 4 + 5);
        buf.append('[');
        buf.append(arr[0]);
        for (int i = 1; i < arr.length; i++) {
            buf.append(',').append(' ').append(arr[i]);
        }
        buf.append(']');
        return buf.toString();
    }

    private void testGetIssueNumber(String hyperlinkText, String issueNumber) {
        assertEquals(issueNumber, issueFinder.getIssueId(hyperlinkText));
    }

    private static void checkTestValidity(boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException();
        }
    }

}
