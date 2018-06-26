/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.lexer;

/**
 *
 * @author Petr Pisl
 */
public class PHP53FeaturesTest extends PHPLexerTestBase {

    public PHP53FeaturesTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGoto_01() throws Exception {
        performTest("lexer/jump01");
    }

    public void testGoto_02() throws Exception {
        performTest("lexer/jump02");
    }

    public void testGoto_03() throws Exception {
        performTest("lexer/jump03");
    }

    public void testGoto_04() throws Exception {
        performTest("lexer/jump04");
    }

    public void testGoto_05() throws Exception {
        performTest("lexer/jump05");
    }

    public void testGoto_06() throws Exception {
        performTest("lexer/jump06");
    }

    public void testGoto_07() throws Exception {
        performTest("lexer/jump07");
    }

    public void testGoto_08() throws Exception {
        performTest("lexer/jump08");
    }

    public void testGoto_09() throws Exception {
        performTest("lexer/jump09");
    }

    public void testGoto_10() throws Exception {
        performTest("lexer/jump10");
    }

    public void testGoto_11() throws Exception {
        performTest("lexer/jump11");
    }

    public void testGoto_12() throws Exception {
        performTest("lexer/jump12");
    }

    public void testGoto_13() throws Exception {
        performTest("lexer/jump13");
    }

    public void testNowDoc_00() throws Exception {
        performTest("lexer/nowdoc_000");
    }

    public void testNowDoc_01() throws Exception {
        performTest("lexer/nowdoc_001");
    }

    public void testNowDoc_02() throws Exception {
        performTest("lexer/nowdoc_002");
    }

    public void testNowDoc_03() throws Exception {
        performTest("lexer/nowdoc_003");
    }

    public void testNowDoc_04() throws Exception {
        performTest("lexer/nowdoc_004");
    }

    public void testNowDoc_05() throws Exception {
        performTest("lexer/nowdoc_005");
    }

    public void testNowDoc_06() throws Exception {
        performTest("lexer/nowdoc_006");
    }

    public void testNowDoc_07() throws Exception {
        performTest("lexer/nowdoc_007");
    }

    public void testNowDoc_08() throws Exception {
        performTest("lexer/nowdoc_008");
    }

    public void testNowDoc_09() throws Exception {
        performTest("lexer/nowdoc_009");
    }

    public void testNowDoc_10() throws Exception {
        performTest("lexer/nowdoc_010");
    }

    public void testNowDoc_11() throws Exception {
        performTest("lexer/nowdoc_011");
    }

    public void testNowDoc_12() throws Exception {
        performTest("lexer/nowdoc_012");
    }

    public void testNowDoc_13() throws Exception {
        performTest("lexer/nowdoc_013");
    }

    public void testNowDoc_14() throws Exception {
        performTest("lexer/nowdoc_014");
    }

    public void testNowDoc_15() throws Exception {
        performTest("lexer/nowdoc_015");
    }

    public void testHereDoc53_01() throws Exception {
        performTest("lexer/heredoc_001");
    }

    public void testHereDoc53_02() throws Exception {
        performTest("lexer/heredoc_002");
    }

    public void testHereDoc53_03() throws Exception {
        performTest("lexer/heredoc_003");
    }

    public void testHereDoc53_04() throws Exception {
        performTest("lexer/heredoc_004");
    }

    public void testHereDoc53_05() throws Exception {
        performTest("lexer/heredoc_005");
    }

    public void testHereDoc53_06() throws Exception {
        performTest("lexer/heredoc_006");
    }

    public void testHereDoc53_07() throws Exception {
        performTest("lexer/heredoc_007");
    }

    public void testHereDoc53_08() throws Exception {
        performTest("lexer/heredoc_008");
    }

    public void testHereDoc53_09() throws Exception {
        performTest("lexer/heredoc_009");
    }

    public void testHereDoc53_10() throws Exception {
        performTest("lexer/heredoc_010");
    }

    public void testHereDoc53_11() throws Exception {
        performTest("lexer/heredoc_011");
    }

    public void testHereDoc53_12() throws Exception {
        performTest("lexer/heredoc_012");
    }

    public void testHereDoc53_13() throws Exception {
        performTest("lexer/heredoc_013");
    }

    public void testHereDoc53_14() throws Exception {
        performTest("lexer/heredoc_014");
    }

    public void testHereDoc53_15() throws Exception {
        performTest("lexer/heredoc_015");
    }

    public void testElvis01() throws Exception {
        performTest("lexer/elvis_01");
    }

    public void testElvis02() throws Exception {
        performTest("lexer/elvis_02");
    }

    public void testElvis03() throws Exception {
        performTest("lexer/elvis_03");
    }

    public void testElvis04() throws Exception {
        performTest("lexer/elvis_04");
    }

    public void testElvis05() throws Exception {
        performTest("lexer/elvis_05");
    }

    public void testElvis06() throws Exception {
        performTest("lexer/elvis_06");
    }

    public void testIssue225549() throws Exception {
        performTest("lexer/issue225549");
    }
}
