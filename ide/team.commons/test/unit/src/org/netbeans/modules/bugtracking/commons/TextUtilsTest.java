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

import org.junit.Test;
import static org.junit.Assert.*;
import static org.netbeans.modules.bugtracking.commons.TextUtils.trimSpecial;

/**
 *
 * @author Marian Petras
 */
public class TextUtilsTest {

    public TextUtilsTest() {
    }

    @Test(expected=IllegalArgumentException.class)
    public void testShortenTextNullText() {
        shortenText(null, 5);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testShortenTextNegativeLimit() {
        shortenText("abc", -7);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testShortenTextZeroLimit() {
        shortenText("abc", 0);
    }

    @Test
    public void testShortenText() {
        assertEquals("", shortenText("", 1));
        assertEquals("", shortenText("", 2));
        assertEquals("", shortenText("", 3));
        assertEquals("", shortenText("", 4));
        assertEquals("", shortenText("", 5));

        assertEquals("", shortenText(" ", 1));
        assertEquals("", shortenText("    ", 1));
        assertEquals("", shortenText("\t\t ", 1));
        assertEquals("", shortenText(" \t  ", 1));
        assertEquals("", shortenText("\t\t\t\t", 1));
        assertEquals("", shortenText("  \t\t  ", 1));

        assertEquals("", shortenText(" ", 15));
        assertEquals("", shortenText("    ", 15));
        assertEquals("", shortenText("\t\t ", 15));
        assertEquals("", shortenText(" \t  ", 15));
        assertEquals("", shortenText("\t\t\t\t", 15));
        assertEquals("", shortenText("  \t\t  ", 15));

        assertEquals("a", shortenText("  a   ", 10));
        assertEquals("a", shortenText("\t a \t\t     \t ", 10));

        assertEquals("a", shortenText("a", 1));
        assertEquals("a...", shortenText("ab", 1));

        assertEquals("a...", shortenText("alpha beta", 1));
        assertEquals("al...", shortenText("alpha beta", 2));
        assertEquals("alp...", shortenText("alpha beta", 3));
        assertEquals("alph...", shortenText("alpha beta", 4));
        assertEquals("alpha ...", shortenText("alpha beta", 5));
        assertEquals("alpha ...", shortenText("alpha beta", 6));
        assertEquals("alpha b...", shortenText("alpha beta", 7));
        assertEquals("alpha be...", shortenText("alpha beta", 8));
        assertEquals("alpha bet...", shortenText("alpha beta", 9));
        assertEquals("alpha beta", shortenText("alpha beta", 10));
        assertEquals("alpha beta", shortenText("alpha beta", 11));

        assertEquals("a...", shortenText("alpha\tbeta", 1));
        assertEquals("al...", shortenText("alpha\tbeta", 2));
        assertEquals("alp...", shortenText("alpha\tbeta", 3));
        assertEquals("alph...", shortenText("alpha\tbeta", 4));
        assertEquals("alpha ...", shortenText("alpha\tbeta", 5));
        assertEquals("alpha ...", shortenText("alpha\tbeta", 6));
        assertEquals("alpha\tb...", shortenText("alpha\tbeta", 7));
        assertEquals("alpha\tbe...", shortenText("alpha\tbeta", 8));
        assertEquals("alpha\tbet...", shortenText("alpha\tbeta", 9));
        assertEquals("alpha\tbeta", shortenText("alpha\tbeta", 10));
        assertEquals("alpha\tbeta", shortenText("alpha\tbeta", 11));
        assertEquals("alpha\tbeta", shortenText("alpha\tbeta", 12));

        assertEquals("a...", shortenText("alpha beta gamma", 1));
        assertEquals("al...", shortenText("alpha beta gamma", 2));
        assertEquals("alp...", shortenText("alpha beta gamma", 3));
        assertEquals("alph...", shortenText("alpha beta gamma", 4));
        assertEquals("alpha ...", shortenText("alpha beta gamma", 5));
        assertEquals("alpha ...", shortenText("alpha beta gamma", 6));
        assertEquals("alpha b...", shortenText("alpha beta gamma", 7));
        assertEquals("alpha be...", shortenText("alpha beta gamma", 8));
        assertEquals("alpha bet...", shortenText("alpha beta gamma", 9));
        assertEquals("alpha beta ...", shortenText("alpha beta gamma", 10));
        assertEquals("alpha beta ...", shortenText("alpha beta gamma", 11));
        assertEquals("alpha beta ...", shortenText("alpha beta gamma", 12));
        assertEquals("alpha beta ...", shortenText("alpha beta gamma", 13));
        assertEquals("alpha beta ...", shortenText("alpha beta gamma", 14));
        assertEquals("alpha beta ...", shortenText("alpha beta gamma", 15));
        assertEquals("alpha beta gamma", shortenText("alpha beta gamma", 16));
        assertEquals("alpha beta gamma", shortenText("alpha beta gamma", 17));

        assertEquals("alpha beta ...", shortenText("alpha beta gamma", 10));
        assertEquals("alpha  bet...",  shortenText("alpha  beta gamma", 10));
        assertEquals("alpha   be...",  shortenText("alpha   beta gamma", 10));
        assertEquals("alpha    b...",  shortenText("alpha    beta gamma", 10));
        assertEquals("alpha ...",      shortenText("alpha     beta gamma", 10));
        assertEquals("alpha ...",      shortenText("alpha      beta gamma", 10));
        assertEquals("alpha ...",      shortenText("alpha       beta gamma", 10));

        assertEquals("alpha beta gamma", shortenText("alpha beta gamma", 16));
        assertEquals("alpha beta ...",   shortenText("alpha beta  gamma", 16));
        assertEquals("alpha beta ...",   shortenText("alpha beta   gamma", 16));
        assertEquals("alpha beta ...",   shortenText("alpha beta    gamma", 16));
        assertEquals("alpha beta ...",   shortenText("alpha beta     gamma", 16));
        assertEquals("alpha beta ...",   shortenText("alpha beta      gamma", 16));
        assertEquals("alpha beta ...",   shortenText("alpha beta       gamma", 16));
        assertEquals("alpha beta ...",   shortenText("alpha beta        gamma", 16));

        assertEquals("alp...",            TextUtils.shortenText("alpha beta gamma delta", 1, 3));
        assertEquals("alp...",            TextUtils.shortenText("alpha beta gamma delta", 2, 3));
        assertEquals("alp...",            TextUtils.shortenText("alpha beta gamma delta", 3, 3));
        assertEquals("alp...",            TextUtils.shortenText("alpha beta gamma delta", 4, 3));
        assertEquals("alp...",            TextUtils.shortenText("alpha beta gamma delta", 5, 3));
        assertEquals("alp...",            TextUtils.shortenText("alpha beta gamma delta", 6, 3));

        assertEquals("alpha ...",         TextUtils.shortenText("alpha beta gamma delta", 1, 8));
        assertEquals("alpha be...",       TextUtils.shortenText("alpha beta gamma delta", 2, 8));
        assertEquals("alpha be...",       TextUtils.shortenText("alpha beta gamma delta", 3, 8));
        assertEquals("alpha be...",       TextUtils.shortenText("alpha beta gamma delta", 4, 8));
        assertEquals("alpha be...",       TextUtils.shortenText("alpha beta gamma delta", 5, 8));
        assertEquals("alpha be...",       TextUtils.shortenText("alpha beta gamma delta", 6, 8));

        assertEquals("alpha beta ...",    TextUtils.shortenText("alpha beta gamma delta", 1, 14));
        assertEquals("alpha beta ...",    TextUtils.shortenText("alpha beta gamma delta", 2, 14));
        assertEquals("alpha beta gam...", TextUtils.shortenText("alpha beta gamma delta", 3, 14));
        assertEquals("alpha beta gam...", TextUtils.shortenText("alpha beta gamma delta", 4, 14));
        assertEquals("alpha beta gam...", TextUtils.shortenText("alpha beta gamma delta", 5, 14));
        assertEquals("alpha beta gam...", TextUtils.shortenText("alpha beta gamma delta", 6, 14));
    }

    private static String shortenText(String text, int limit) {
        return TextUtils.shortenText(text, 2, limit);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testTrimSpecialNullText() {
        trimSpecial(null);
    }

    @Test
    public void testTrimSpecial() {
        assertEquals("", trimSpecial(""));
        assertEquals("", trimSpecial(" "));
        assertEquals("", trimSpecial("\t"));
        assertEquals("", trimSpecial("  "));
        assertEquals("", trimSpecial(" \t"));
        assertEquals("", trimSpecial("\t "));
        assertEquals("", trimSpecial("\t\t"));

        assertEquals("a b", trimSpecial("a b"));
        assertEquals("a b", trimSpecial(" a b"));
        assertEquals("a b", trimSpecial(" a b "));
        assertEquals("a  b", trimSpecial("a  b"));
        assertEquals("a  b", trimSpecial(" a  b"));
        assertEquals("a  b", trimSpecial("  a  b"));
        assertEquals("a  b", trimSpecial("   a  b"));
        assertEquals("a  b", trimSpecial("a  b "));
        assertEquals("a  b", trimSpecial("a  b  "));
        assertEquals("a  b", trimSpecial("a  b   "));
        assertEquals("a \tb", trimSpecial("a \tb"));
        assertEquals("a\t b", trimSpecial("a\t b"));
        assertEquals("a \tb", trimSpecial(" a \tb"));
        assertEquals("a \tb", trimSpecial(" \ta \tb"));
        assertEquals("a \tb", trimSpecial("a \tb\t\t"));
    }

}
