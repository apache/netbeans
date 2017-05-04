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

public class PHP70FeaturesTest extends PHPLexerTestBase {

    public PHP70FeaturesTest(String testName) {
        super(testName);
    }

    public void testSpaceship01() throws Exception {
        performTest("lexer/spaceship_01");
    }

    public void testSpaceship02() throws Exception {
        performTest("lexer/spaceship_02");
    }

    public void testCoalesce01() throws Exception {
        performTest("lexer/coalesce_01");
    }

    public void testCoalesce02() throws Exception {
        performTest("lexer/coalesce_02");
    }

    public void testCoalesce03() throws Exception {
        performTest("lexer/coalesce_03");
    }

    public void testCoalesce04() throws Exception {
        performTest("lexer/coalesce_04");
    }

    public void testCoalesce05() throws Exception {
        performTest("lexer/coalesce_05");
    }

    public void testReturnTypes01() throws Exception {
        performTest("lexer/return_types_01");
    }

    public void testReturnTypes02() throws Exception {
        performTest("lexer/return_types_02");
    }

    public void testReturnTypes03() throws Exception {
        performTest("lexer/return_types_03");
    }

    public void testYieldFrom01() throws Exception {
        performTest("lexer/yield_from_01");
    }

    public void testYieldFrom02() throws Exception {
        performTest("lexer/yield_from_02");
    }

    public void testYieldFrom03() throws Exception {
        performTest("lexer/yield_from_03");
    }

    public void testYieldFrom04() throws Exception {
        performTest("lexer/yield_from_04");
    }

    public void testYieldFrom05() throws Exception {
        performTest("lexer/yield_from_05");
    }

    public void testYieldFrom06() throws Exception {
        performTest("lexer/yield_from_06");
    }

    // #262631
    public void testScalarTypes01() throws Exception {
        performTest("lexer/scalar_types_01");
    }

    // #262141
    public void testContextSensitiveLexer_01() throws Exception {
        performTest("lexer/context_sensitive_lexer_01"); // class method
    }

    public void testContextSensitiveLexer_02() throws Exception {
        performTest("lexer/context_sensitive_lexer_02"); // trait method
    }

    public void testContextSensitiveLexer_03() throws Exception {
        performTest("lexer/context_sensitive_lexer_03"); // interface method
    }

    public void testContextSensitiveLexer_04() throws Exception {
        performTest("lexer/context_sensitive_lexer_04"); // class const
    }

    public void testContextSensitiveLexer_05() throws Exception {
        performTest("lexer/context_sensitive_lexer_05"); // interface const
    }

    // check the "function"(PHP_FUNCTION) token after the const keyword
    // it's not PHP_STRING but PHP_FUNCTION
    public void testContextSensitiveLexer_06() throws Exception {
        performTest("lexer/context_sensitive_lexer_06"); // in mixed group uses
    }

    public void testContextSensitiveLexer_07() throws Exception {
        performTest("lexer/context_sensitive_lexer_07"); // in mixed group uses
    }

    public void testContextSensitiveLexer_08() throws Exception {
        // const CONST = [1,3], GOTO = 2;
        performTest("lexer/context_sensitive_lexer_08");
    }

    public void testContextSensitiveLexer_09() throws Exception {
        // const CONST = array("test", array("foo" => 1)), GOTO = 2;
        performTest("lexer/context_sensitive_lexer_09");
    }

}
