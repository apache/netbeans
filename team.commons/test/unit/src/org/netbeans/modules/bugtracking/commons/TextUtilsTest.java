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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
