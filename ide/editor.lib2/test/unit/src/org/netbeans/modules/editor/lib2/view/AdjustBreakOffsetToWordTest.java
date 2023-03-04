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
package org.netbeans.modules.editor.lib2.view;

import java.util.Objects;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link HighlightsViewUtils#adjustBreakOffsetToWord(CharSequence,int,boolean)}.
 */
public class AdjustBreakOffsetToWordTest {
    private void testOne(String original, String expected, boolean allowWhitespaceBeyondEnd) {
        TextWithCaret originalTwC = TextWithCaret.fromEncoded(original);
        TextWithCaret expectedTwC = TextWithCaret.fromEncoded(expected);
        TextWithCaret actualTwC = new TextWithCaret(originalTwC.text,
                HighlightsViewUtils.adjustBreakOffsetToWord(originalTwC.text, originalTwC.caret,
                allowWhitespaceBeyondEnd));
        Assert.assertEquals(expectedTwC, actualTwC);
    }

    private void testOne(String original, String expected) {
        testOne(original, expected, false);
        testOne(original, expected, true);
    }

    private void testOne(String original, String
            expectedDisallowWhitespaceBeyondEnd, String expectedAllowWhitespaceBeyondEnd)
    {
        testOne(original, expectedDisallowWhitespaceBeyondEnd, false);
        testOne(original, expectedAllowWhitespaceBeyondEnd, true);
    }

    @Test
    public void testLongWord() {
        // Forward-skipping is necessary.
        testOne("|this is a test", "this| is a test", "this |is a test");
        testOne("t|his is a test", "this| is a test", "this |is a test");
        testOne("th|is is a test", "this| is a test", "this |is a test");
        testOne("thi|s is a test", "this| is a test", "this |is a test");
        testOne("this| is a test", "this| is a test", "this |is a test");
    }

    @Test
    public void testBasic() {
        // Forward-skipping is not necessary.
        testOne("this |is a test", "this |is a test");
        testOne("this i|s a test", "this |is a test");
        /* If !allowWhitespaceBeyondEnd, break must backtrack to before "is" to avoid the new line
        starting with a whitespace. */
        testOne("this is| a test", "this |is a test", "this is |a test");
        testOne("this is |a test", "this is |a test");
        testOne("this is a| test", "this is |a test", "this is a |test");
        testOne("this is a |test", "this is a |test");
        testOne("this is a t|est", "this is a |test");
        testOne("this is a te|st", "this is a |test");
        testOne("this is a tes|t", "this is a |test");
        testOne("this is a test|", "this is a test|");
    }

    @Test
    public void testDashInFirstWord() {
        testOne("|foo-test bar is", "foo-|test bar is");
        testOne("f|oo-test bar is", "foo-|test bar is");
        testOne("fo|o-test bar is", "foo-|test bar is");
        testOne("foo|-test bar is", "foo-|test bar is");
        testOne("foo-|test bar is", "foo-|test bar is");
        testOne("foo-t|est bar is", "foo-|test bar is");
        testOne("foo-te|st bar is", "foo-|test bar is");
        testOne("foo-tes|t bar is", "foo-|test bar is");
        testOne("foo-test| bar is", "foo-|test bar is", "foo-test |bar is");
        testOne("foo-test |bar is", "foo-test |bar is");
        testOne("foo-test b|ar is", "foo-test |bar is");
    }

    @Test
    public void testDashInNonFirstWord() {
        testOne("this is| foo-test bar", "this |is foo-test bar", "this is |foo-test bar");
        testOne("this is |foo-test bar", "this is |foo-test bar");
        testOne("this is f|oo-test bar", "this is |foo-test bar");
        testOne("this is fo|o-test bar", "this is |foo-test bar");
        testOne("this is foo|-test bar", "this is |foo-test bar");
        testOne("this is foo-|test bar", "this is foo-|test bar");
        testOne("this is foo-t|est bar", "this is foo-|test bar");
        testOne("this is foo-te|st bar", "this is foo-|test bar");
        testOne("this is foo-tes|t bar", "this is foo-|test bar");
        testOne("this is foo-test| bar", "this is foo-|test bar", "this is foo-test |bar");
        testOne("this is foo-test |bar", "this is foo-test |bar");
        testOne("this is foo-test b|ar", "this is foo-test |bar");
    }

    @Test
    public void testJustWhitespace() {
        testOne("|", "|");
        testOne("| ", " |");
        testOne(" |", " |");
        testOne("|  ", " | ");
        testOne(" | ", " | ", "  |");
        testOne("  |", "  |");
        testOne("|   ", " |  ");
        testOne(" |  ", " |  ", "  | ");
        testOne("  | ", "  | ",  "   |");
        testOne("   |", "   |");
    }

    @Test
    public void testTrailingNewline() {
        // A newline character should just be treated like any other whitespace here.
        testOne("|this is a test\n", "this| is a test\n", "this |is a test\n");
        testOne("t|his is a test\n", "this| is a test\n", "this |is a test\n");
        testOne("th|is is a test\n", "this| is a test\n", "this |is a test\n");
        testOne("thi|s is a test\n", "this| is a test\n", "this |is a test\n");
        testOne("this| is a test\n", "this| is a test\n", "this |is a test\n");
        testOne("this |is a test\n", "this |is a test\n");
        testOne("this i|s a test\n", "this |is a test\n");
        testOne("this is| a test\n", "this |is a test\n", "this is |a test\n");
        testOne("this is |a test\n", "this is |a test\n");
        testOne("this is a| test\n", "this is |a test\n", "this is a |test\n");
        testOne("this is a |test\n", "this is a |test\n");
        testOne("this is a t|est\n", "this is a |test\n");
        testOne("this is a te|st\n", "this is a |test\n");
        testOne("this is a tes|t\n", "this is a |test\n");
        testOne("this is a test|\n", "this is a |test\n", "this is a test\n|");
        testOne("this is a test\n|", "this is a test\n|");
    }

    @Test
    public void testSpacesBetween() {
        // Multiple whitespace characters between first and second word.
        testOne("th|is   is a test", "this|   is a test", "this |  is a test");
        testOne("thi|s   is a test", "this|   is a test", "this |  is a test");
        testOne("this|   is a test", "this|   is a test", "this |  is a test");
        testOne("this |  is a test", "this |  is a test", "this  | is a test");
        testOne("this  | is a test", "this  | is a test", "this   |is a test");
        testOne("this   |is a test", "this   |is a test");
        testOne("this   i|s a test", "this   |is a test");
        testOne("this   is| a test", "this   |is a test", "this   is |a test");
        testOne("this   is |a test", "this   is |a test");
        // Multiple whitespace characters between words not including the first word.
        testOne("this |is   aaaa test", "this |is   aaaa test");
        testOne("this i|s   aaaa test", "this |is   aaaa test");
        testOne("this is|   aaaa test", "this |is   aaaa test", "this is |  aaaa test");
        testOne("this is |  aaaa test", "this |is   aaaa test", "this is  | aaaa test");
        testOne("this is  | aaaa test", "this |is   aaaa test", "this is   |aaaa test");
        testOne("this is   |aaaa test", "this is   |aaaa test");
        testOne("this is   a|aaa test", "this is   |aaaa test");
        testOne("this is   aa|aa test", "this is   |aaaa test");
        testOne("this is   aaa|a test", "this is   |aaaa test");
        testOne("this is   aaaa| test", "this is   |aaaa test", "this is   aaaa |test");
        testOne("this is   aaaa |test", "this is   aaaa |test");
    }

    @Test
    public void testTrailingSpaces() {
        // One trailing whitespace character.
        testOne("this is a test| ", "this is a |test ", "this is a test |");
        testOne("this is a test |", "this is a test |");
        // Two trailing whitespace characters.
        testOne("this is a tes|t  ", "this is a |test  ", "this is a |test  ");
        testOne("this is a test|  ", "this is a |test  ", "this is a test | ");
        testOne("this is a test | ", "this is a |test  ", "this is a test  |");
        testOne("this is a test  |", "this is a test  |", "this is a test  |");
        // Long line with one trailing whitespace character.
        testOne("|testtest ", "testtest| ", "testtest |");
        testOne("t|esttest ", "testtest| ", "testtest |");
        testOne("testte|st ", "testtest| ", "testtest |");
        testOne("testtes|t ", "testtest| ", "testtest |");
        testOne("testtest| ", "testtest| ", "testtest |");
        testOne("testtest |", "testtest |", "testtest |");
        // Long line with two trailing whitespace characters.
        testOne("|testtest  ", "testtest|  ", "testtest | ");
        testOne("t|esttest  ", "testtest|  ", "testtest | ");
        testOne("testte|st  ", "testtest|  ", "testtest | ");
        testOne("testtes|t  ", "testtest|  ", "testtest | ");
        testOne("testtest|  ", "testtest|  ", "testtest | ");
        testOne("testtest | ", "testtest | ", "testtest  |");
        testOne("testtest  |", "testtest  |");
    }

    @Test
    public void testAdjustBreakOffsetToWordLeadingSpace() {
        // One leading space
        testOne("| this is a test", " |this is a test");
        testOne(" |this is a test", " |this is a test");
        testOne(" t|his is a test", " |this is a test");
        testOne(" th|is is a test", " |this is a test");
        testOne(" thi|s is a test", " |this is a test");
        testOne(" this| is a test", " |this is a test", " this |is a test");
        testOne(" this |is a test", " this |is a test");
        testOne(" this i|s a test", " this |is a test");
        // Two leading spaces
        testOne("|  this is a test", " | this is a test");
        testOne(" | this is a test", " | this is a test", "  |this is a test");
        testOne("  |this is a test", "  |this is a test");
        testOne("  t|his is a test", "  |this is a test");
        testOne("  th|is is a test", "  |this is a test");
        testOne("  thi|s is a test", "  |this is a test");
        testOne("  this| is a test", "  |this is a test", "  this |is a test");
        testOne("  this |is a test", "  this |is a test");
        testOne("  this i|s a test", "  this |is a test");
    }

    private static final class TextWithCaret {
        public final String text;
        public final int caret;

        public TextWithCaret(String text, int caret) {
            this(text, caret, true);
        }

        private TextWithCaret(String text, int caret, boolean check) {
            if (text == null) {
                throw new NullPointerException();
            }
            if (caret < 0 || caret > text.length()) {
                throw new IllegalArgumentException();
            }
            this.text = text;
            this.caret = caret;

            if (check && !equals(fromEncoded(toString()))) {
                throw new AssertionError();
            }
        }

        /**
         * @param encoded string containing a single '|' character indicating the caret position
         */
        public static TextWithCaret fromEncoded(String encoded) {
            final StringBuilder psb = new StringBuilder();
            Integer caret = null;
            for (char ec : encoded.toCharArray()) {
                if (ec == '|') {
                    if (caret != null) {
                        throw new IllegalArgumentException("Multiple caret positions specified");
                    }
                    caret = psb.length();
                } else {
                    psb.append(ec);
                }
            }
            if (caret == null) {
                throw new IllegalArgumentException("No caret position specified");
            }
            return new TextWithCaret(psb.toString(), caret, false);
        }

        @Override
        public String toString() {
            return text.substring(0, caret) + '|' + text.substring(caret);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TextWithCaret)) {
                return false;
            }
            TextWithCaret other = (TextWithCaret) obj;
            return this.text.equals(other.text)
                && this.caret == other.caret;
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, caret);
        }
    }
}
