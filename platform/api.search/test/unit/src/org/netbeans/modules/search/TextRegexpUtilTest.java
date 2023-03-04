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

package org.netbeans.modules.search;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.api.search.SearchPattern.MatchType;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author mp
 */
public class TextRegexpUtilTest extends NbTestCase {

    public TextRegexpUtilTest() {
        super("SimpleRegexpParserTest");
    }

    private static String getClassField(String name) throws Exception {
        Field field = TextRegexpUtil.class.getDeclaredField(name);
        field.setAccessible(true);
        return (String) field.get(null);
    }

    public void testMakeRegexp() throws Exception {

        /* basics: */
        assertEquals("", makeRegexp(""));
        assertEquals("a", makeRegexp("a"));
        assertEquals("ab", makeRegexp("ab"));
        assertEquals("abc", makeRegexp("abc"));

        /* special chars in the middle: */
        assertEquals("a.*?b.c", makeRegexp("a*b?c"));
        assertEquals("a..+?b", makeRegexp("a?*?b"));
        assertEquals("a.+?b", makeRegexp("a*?*b"));

        /* ignore stars in the begining: */
        assertEquals("a", makeRegexp("*a"));
        assertEquals(".a", makeRegexp("?a"));
        assertEquals("a", makeRegexp("**a"));
        assertEquals(".a", makeRegexp("*?a"));
        assertEquals(".a", makeRegexp("?*a"));
        assertEquals("..a", makeRegexp("??a"));

        /* ignore stars at the end: */
        assertEquals("a", makeRegexp("a*"));
        assertEquals("a.", makeRegexp("a?"));
        assertEquals("a", makeRegexp("a**"));
        assertEquals("a.", makeRegexp("a*?"));
        assertEquals("a.", makeRegexp("a?*"));
        assertEquals("a..", makeRegexp("a??"));

        /* other usage of '*' and '?': */
        assertEquals(" .*?a", makeRegexp(" *a"));
        assertEquals(" .a", makeRegexp(" ?a"));
        assertEquals(" a", makeRegexp("* a"));
        assertEquals(". a", makeRegexp("? a"));
        assertEquals("\\,a", makeRegexp("*,a"));
        assertEquals(".\\,a", makeRegexp("?,a"));
        assertEquals("a.*? ", makeRegexp("a* "));
        assertEquals("a. ", makeRegexp("a? "));
        assertEquals("a ", makeRegexp("a *"));
        assertEquals("a .", makeRegexp("a ?"));
        assertEquals("a\\,", makeRegexp("a,*"));
        assertEquals("a\\,.", makeRegexp("a,?"));

        /* whole words: */

        final String wordCharsExpr = getClassField("wordCharsExpr");
        final String checkNotAfterWordChar = getClassField("checkNotAfterWordChar");
        final String checkNotBeforeWordChar = getClassField("checkNotBeforeWordChar");

        assertEquals("", makeRegexp("", true));
        assertEquals(checkNotAfterWordChar + "a" + checkNotBeforeWordChar,
                     makeRegexp("a", true));
        assertEquals(checkNotAfterWordChar
                     + "a" + wordCharsExpr + "*?b" + wordCharsExpr + "c"
                     + checkNotBeforeWordChar,
                     makeRegexp("a*b?c", true));
        assertEquals(checkNotAfterWordChar
                     + "a" + wordCharsExpr + "{2,}?b"
                     + checkNotBeforeWordChar,
                     makeRegexp("a?*?b", true));
        assertEquals(checkNotAfterWordChar
                     + "a" + wordCharsExpr + "+?b"
                     + checkNotBeforeWordChar,
                     makeRegexp("a*?*b", true));

        assertEquals(wordCharsExpr + "*a" + checkNotBeforeWordChar,
                     makeRegexp("*a", true));
        assertEquals(checkNotAfterWordChar + wordCharsExpr + "a" + checkNotBeforeWordChar,
                     makeRegexp("?a", true));
        assertEquals(wordCharsExpr + "*a" + checkNotBeforeWordChar,
                     makeRegexp("**a", true));
        assertEquals(wordCharsExpr + "+a" + checkNotBeforeWordChar,
                     makeRegexp("*?a", true));
        assertEquals(wordCharsExpr + "+a" + checkNotBeforeWordChar,
                     makeRegexp("?*a", true));
        assertEquals(checkNotAfterWordChar + wordCharsExpr + "{2}a" + checkNotBeforeWordChar,
                     makeRegexp("??a", true));

        assertEquals(checkNotAfterWordChar + "a" + wordCharsExpr + "*",
                     makeRegexp("a*", true));
        assertEquals(checkNotAfterWordChar + "a" + wordCharsExpr + checkNotBeforeWordChar,
                     makeRegexp("a?", true));
        assertEquals(checkNotAfterWordChar + "a" + wordCharsExpr + "*",
                     makeRegexp("a**", true));
        assertEquals(checkNotAfterWordChar + "a" + wordCharsExpr + "+",
                     makeRegexp("a*?", true));
        assertEquals(checkNotAfterWordChar + "a" + wordCharsExpr + "+",
                     makeRegexp("a?*", true));
        assertEquals(checkNotAfterWordChar + "a" + wordCharsExpr + "{2}" + checkNotBeforeWordChar,
                     makeRegexp("a??", true));

        assertEquals(" " + wordCharsExpr + "*?a" + checkNotBeforeWordChar,
                     makeRegexp(" *a", true));
        assertEquals(" " + wordCharsExpr + "a" + checkNotBeforeWordChar,
                     makeRegexp(" ?a", true));
        assertEquals(wordCharsExpr + "* a" + checkNotBeforeWordChar,
                     makeRegexp("* a", true));
        assertEquals(checkNotAfterWordChar + wordCharsExpr + " a" + checkNotBeforeWordChar,
                     makeRegexp("? a", true));
        assertEquals(wordCharsExpr + "*\\,a" + checkNotBeforeWordChar,
                     makeRegexp("*,a", true));
        assertEquals(checkNotAfterWordChar + wordCharsExpr + "\\,a" + checkNotBeforeWordChar,
                     makeRegexp("?,a", true));
        assertEquals(checkNotAfterWordChar + "a" + wordCharsExpr + "*? ",
                     makeRegexp("a* ", true));
        assertEquals(checkNotAfterWordChar + "a" + wordCharsExpr + " ",
                     makeRegexp("a? ", true));
        assertEquals(checkNotAfterWordChar + "a " + wordCharsExpr + "*",
                     makeRegexp("a *", true));
        assertEquals(checkNotAfterWordChar + "a " + wordCharsExpr + checkNotBeforeWordChar,
                     makeRegexp("a ?", true));
        assertEquals(checkNotAfterWordChar + "a\\," + wordCharsExpr + "*",
                     makeRegexp("a,*", true));
        assertEquals(checkNotAfterWordChar + "a\\," + wordCharsExpr + checkNotBeforeWordChar,
                     makeRegexp("a,?", true));

        assertEquals("a b", makeRegexp("a b"));
        assertEquals("a\\!b", makeRegexp("a!b"));
        assertEquals("a\\\"b", makeRegexp("a\"b"));
        assertEquals("a\\#b", makeRegexp("a#b"));
        assertEquals("a\\$b", makeRegexp("a$b"));
        assertEquals("a\\%b", makeRegexp("a%b"));
        assertEquals("a\\&b", makeRegexp("a&b"));
        assertEquals("a\\'b", makeRegexp("a'b"));
        assertEquals("a\\(b", makeRegexp("a(b"));
        assertEquals("a\\)b", makeRegexp("a)b"));
        assertEquals("a\\+b", makeRegexp("a+b"));
        assertEquals("a\\,b", makeRegexp("a,b"));
        assertEquals("a\\-b", makeRegexp("a-b"));
        assertEquals("a\\.b", makeRegexp("a.b"));
        assertEquals("a\\/b", makeRegexp("a/b"));
        
        assertEquals("a0b", makeRegexp("a0b"));
        assertEquals("a1b", makeRegexp("a1b"));
        assertEquals("a2b", makeRegexp("a2b"));
        assertEquals("a3b", makeRegexp("a3b"));
        assertEquals("a4b", makeRegexp("a4b"));
        assertEquals("a5b", makeRegexp("a5b"));
        assertEquals("a6b", makeRegexp("a6b"));
        assertEquals("a7b", makeRegexp("a7b"));
        assertEquals("a8b", makeRegexp("a8b"));
        assertEquals("a9b", makeRegexp("a9b"));
        
        assertEquals("a\\:b", makeRegexp("a:b"));
        assertEquals("a\\;b", makeRegexp("a;b"));
        assertEquals("a\\<b", makeRegexp("a<b"));
        assertEquals("a\\=b", makeRegexp("a=b"));
        assertEquals("a\\>b", makeRegexp("a>b"));
        assertEquals("a\\@b", makeRegexp("a@b"));
        assertEquals("a\\[b", makeRegexp("a[b"));
        assertEquals("a\\\\a", makeRegexp("a\\a"));
        assertEquals("a\\\\b", makeRegexp("a\\b"));
        assertEquals("a\\\\c", makeRegexp("a\\c"));
        assertEquals("a\\\\d", makeRegexp("a\\d"));
        assertEquals("a\\\\e", makeRegexp("a\\e"));
        assertEquals("a\\\\f", makeRegexp("a\\f"));
        assertEquals("a\\\\g", makeRegexp("a\\g"));
        assertEquals("a\\\\h", makeRegexp("a\\h"));
        assertEquals("a\\\\i", makeRegexp("a\\i"));
        assertEquals("a\\\\j", makeRegexp("a\\j"));
        assertEquals("a\\\\k", makeRegexp("a\\k"));
        assertEquals("a\\\\l", makeRegexp("a\\l"));
        assertEquals("a\\\\m", makeRegexp("a\\m"));
        assertEquals("a\\\\n", makeRegexp("a\\n"));
        assertEquals("a\\\\o", makeRegexp("a\\o"));
        assertEquals("a\\\\p", makeRegexp("a\\p"));
        assertEquals("a\\\\q", makeRegexp("a\\q"));
        assertEquals("a\\\\r", makeRegexp("a\\r"));
        assertEquals("a\\\\s", makeRegexp("a\\s"));
        assertEquals("a\\\\t", makeRegexp("a\\t"));
        assertEquals("a\\\\u", makeRegexp("a\\u"));
        assertEquals("a\\\\v", makeRegexp("a\\v"));
        assertEquals("a\\\\w", makeRegexp("a\\w"));
        assertEquals("a\\\\x", makeRegexp("a\\x"));
        assertEquals("a\\\\y", makeRegexp("a\\y"));
        assertEquals("a\\\\z", makeRegexp("a\\z"));
        assertEquals("a\\]b", makeRegexp("a]b"));
        assertEquals("a\\^b", makeRegexp("a^b"));
        assertEquals("a\\_b", makeRegexp("a_b"));
        assertEquals("a\\`b", makeRegexp("a`b"));
        assertEquals("a\\{b", makeRegexp("a{b"));
        assertEquals("a\\|b", makeRegexp("a|b"));
        assertEquals("a\\}b", makeRegexp("a}b"));
        assertEquals("a\\~b", makeRegexp("a~b"));
        assertEquals("a\\\u007fb", makeRegexp("a\u007fb"));
        
        assertEquals("a\u0080b", makeRegexp("a\u0080b"));
        assertEquals("a\u00c1b", makeRegexp("a\u00c1b"));
        
        assertEquals("abc\\\\", makeRegexp("abc\\"));
        assertEquals("\\\\\\\"", makeRegexp("\\\""));
        assertEquals("\\\\", makeRegexp("\\"));
        assertEquals("\\<h3 style\\=\\\\\\\"color\\: green\\;\\\\\\\"\\>\\<\\/h3\\>", 
                makeRegexp("<h3 style=\\\"color: green;\\\"></h3>"));
        
    }

    public void testRegexpMatches() {
        checkMatch("public", "x", null);
        checkMatch("public", "li", "li");
        checkMatch("public", "*li", "li");
        checkMatch("public", "li*", "li");
        checkMatch("public", "*li*", "li");
        checkMatch("<body><h3 style=\\\"color: green;\\\"></h3></body>", 
                "<h3 style=\\\"color: green;\\\"></h3>", 
                "<h3 style=\\\"color: green;\\\"></h3>");

        checkMatchWW("public", "x", null);
        checkMatchWW("public", "li", null);
        checkMatchWW("public", "*li", null);
        checkMatchWW("public", "li*", null);
        checkMatchWW("public", "*li*", "public");
        checkMatchWW("public poklice", "*li*", "public");
        checkMatchWW("public poklice", "*lic", "public");
        checkMatchWW("poklice public", "*lic", "public");
        checkMatchWW("public", "??lic", null);
        checkMatchWW("public", "pub??", null);
        checkMatchWW("", "???", null);
        checkMatchWW("p", "???", null);
        checkMatchWW("pub", "???", "pub");
        checkMatchWW("public", "???", null);
    }

    private void checkMatch(String testString,
                            String simpleExpr,
                            String expectedMatch) {
        checkMatch(testString, simpleExpr, expectedMatch, false);
    }

    private void checkMatchWW(String testString,
                              String simpleExpr,
                              String expectedMatch) {
        checkMatch(testString, simpleExpr, expectedMatch, true);
    }

    /**
     * Checks whether the given simple expression matches the expected substring
     * of the given string.
     * 
     * @param  simpleExpr  simple search expression to be tested
     * @param  expectedMatch  substring that should be matched by the expression
     * @param  testString  test string to be searched
     * @param  wholeWords  whether to search with the <i>Whole Words</i> option
     */
    private void checkMatch(String testString,
                            String simpleExpr,
                            String expectedMatch,
                            boolean wholeWords) {
        String regexp = makeRegexp(simpleExpr, wholeWords);
        Matcher matcher = Pattern.compile(regexp).matcher(testString);

        if (expectedMatch == null) {
            assertFalse(matcher.find());
        } else {
            assertTrue(matcher.find());
            assertEquals(expectedMatch, matcher.group());
        }
    }
    
    public void testCanBeMultilinePattern() {
        assertFalse(TextRegexpUtil.isMultilineOrMatchesMultiline("a\\d\\d\\da"));
        assertFalse(TextRegexpUtil.isMultilineOrMatchesMultiline(".*"));
        assertFalse(TextRegexpUtil.isMultilineOrMatchesMultiline("(?m)^x.*y$"));
        assertTrue(TextRegexpUtil.isMultilineOrMatchesMultiline("(?ms-x)test.*test"));
        assertTrue(TextRegexpUtil.isMultilineOrMatchesMultiline("test\\ntest"));
        assertTrue(TextRegexpUtil.isMultilineOrMatchesMultiline("test\\rtest"));
        assertTrue(TextRegexpUtil.isMultilineOrMatchesMultiline("test\\r\\ntest"));
        assertTrue(TextRegexpUtil.isMultilineOrMatchesMultiline("test\\ftest"));
        assertTrue(TextRegexpUtil.isMultilineOrMatchesMultiline("test\\u000Btest"));
        assertTrue(TextRegexpUtil.isMultilineOrMatchesMultiline("test\\x85test"));
        assertTrue(TextRegexpUtil.isMultilineOrMatchesMultiline("test\\s*86test"));
        assertTrue(TextRegexpUtil.isMultilineOrMatchesMultiline("test\ntest"));
        assertTrue(TextRegexpUtil.isMultilineOrMatchesMultiline("test\rtest"));
        assertTrue(TextRegexpUtil.isMultilineOrMatchesMultiline("test\r\ntest"));
    }

    public void testLiteralMatches() {
        SearchPattern sp = SearchPattern.create("a*b", false, false,
                MatchType.LITERAL);
        Pattern p = TextRegexpUtil.makeTextPattern(sp);
        assertTrue(p.matcher("xxxa*byyy").find());
        assertFalse(p.matcher("xxxaSSbyyy").find());

        sp = sp.changeSearchExpression("a?b");
        p = TextRegexpUtil.makeTextPattern(sp);
        assertTrue(p.matcher("xxxa?byyy").find());
        assertFalse(p.matcher("xxxaSbyyy").find());

        sp = sp.changeSearchExpression("a?b*c*d?e");
        p = TextRegexpUtil.makeTextPattern(sp);
        assertTrue(p.matcher("xxxa?b*c*d?eyyy").find());
        assertFalse(p.matcher("xxxa?b*cudweyyy").find());
    }

    private String makeRegexp(String string) {
        return TextRegexpUtil.makeTextPattern(
                SearchPattern.create(string, false, false, MatchType.BASIC))
                .pattern();
    }

    private String makeRegexp(String string, boolean wholeWords) {
        return TextRegexpUtil.makeTextPattern(
                SearchPattern.create(
                string, wholeWords, false, MatchType.BASIC)).pattern();
    }
}
