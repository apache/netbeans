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
package org.netbeans.junit;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.netbeans.junit.AssertLinesEqualHelpers.StringsCompareMode.EXACT;

/**
 *
 * @author homberghp
 */
public class AssertLinesEqualHelpers {

    public enum StringsCompareMode {
        EXACT, IGNORE_DUP_SPACES, IGNORE_INDENTATION, IGNORE_WHITESPACE_DIFF
    }
    private static StringsCompareMode stringsCompareMode = EXACT;
    private static Function<String, String> linePreprocessor = s -> s;
    public static boolean showOutputOnPass = false;

    /**
     * Sets the string compare mode.
     * <ul>
     * <li>EXACT: exact line by line, including empty lines.
     * <li>IGNORE_DUP_SPACES: ignore duplicate (inner) spacing
     * </li>INGNORE_INDENTATION ignore difference in leading and trailing white
     * space
     * </li>INGNORE_WHITE_SPACE_DIFF ignore difference in any white space
     * </ul>
     *
     * @param mode
     */
    public static void setStringCompareMode(StringsCompareMode mode) {
        stringsCompareMode = mode;
        linePreprocessor = switch (mode) {
            case EXACT ->
                s -> s;
            case IGNORE_DUP_SPACES ->
                s -> s.replaceAll("\\s{2,}", " ");
            case IGNORE_INDENTATION ->
                s -> s.trim();
            case IGNORE_WHITESPACE_DIFF ->
                s -> s.trim().replaceAll("\\s{2,}", " ");
        };

    }

    /**
     * Prints a source by splitting on the line breaks and prefixing with name
     * and line number.
     *
     * @param out the stream to print to
     * @param name the name as prefix to each line
     * @param source the source code to print to the out stream.
     */
    public static void printNumbered(final PrintStream out, final String name, String source) {
        AtomicInteger c = new AtomicInteger(1);
        source.trim().lines().forEach(l -> out.println("%s [%4d] %s".formatted(name, c.getAndIncrement(), l)));
    }

    /**
     * Compare strings by replacing all multiples of white space([ \t\n\r]) with
     * a space.
     *
     * The test programmer chooses this to make it easier to write the input and
     * the expected strings.
     *
     * @param expected to compare
     * @param actual to compare
     */
    public static void assertLinesEqual1(String name, String expected, String actual) {
        try {
            assertEquals(name, expected.replaceAll("[ \t\r\n\n]+", " "), actual.replaceAll("[ \t\r\n\n]+", " "));
        } catch (Throwable t) {
            System.err.println("expected:");
            System.err.println(expected);
            System.err.println("actual:");
            System.err.println(actual);
            throw t;
        }
    }

    /**
     * Compare strings by splitting them into lines, remove empty lines, and
     * trim white space.Only when any of the lines differ, all lines are printed
     * with the unequal lines flagged.
     *
     * Before the lines are compared, they are trimmed and the white space is
     * normalized by collapsing multiple white space characters into one. This
     * should make the tests less brittle.
     *
     * If any of the compared lines are unequal, this test fails and the
     * comparison result is shown on stderr in a simplified diff format.
     *
     * @param testName to print before comparison result.
     * @param fileName to print before each compared line.
     * @param expected to compare
     * @param actual to compare
     */
    public static void assertLinesEqual2(String testName,String fileName, String expected, String actual) {
        if (stringsCompareMode != EXACT) {
            expected = expected.trim().replaceAll("([\t\r\n])\\1+", "$1");
            actual = actual.trim().replaceAll("([\t\r\n])\\1+", "$1");
        }
        String[] linesExpected;
        String[] linesActual;
        linesExpected = expected.lines().toArray(String[]::new);
        linesActual = actual.lines().toArray(String[]::new);
        int limit = Math.max(linesExpected.length, linesActual.length);
        StringBuilder sb = new StringBuilder();
        boolean equals = true;
        for (int i = 0; i < limit; i++) {
            String oe = (i < linesExpected.length ? linesExpected[i] : "");
            String oa = (i < linesActual.length ? linesActual[i] : "");
            String e = linePreprocessor.apply(oe);
            String a = linePreprocessor.apply(oa);
            // somehow my user is inserted, so avoid to test those lines.
            if (e.contains("@author") && a.contains("@author")) {
                e = a = "* @author goes here";
                oa = oe;
            }
            boolean same = e.equals(a);
            String sep = same ? "   " : " | ";
            equals &= same;
            sb.append(String.format(fileName + " [%3d] %-80s%s%-80s%n", i, oe, sep, oa));
        }
        if (!equals || showOutputOnPass) {
            System.err.println("test " + testName + (equals ? " PASSED" : " FAILED") + " with compare mode = " + stringsCompareMode);
            System.err.print(String.format(fileName + "       %-80s%s%-80s%n", "expected", " + ", "actual"));
            System.err.println(sb.toString());
            System.err.flush();
            if (!equals) {
                fail("lines differ, see stderr for more details.");
            }
        }
    }

}
