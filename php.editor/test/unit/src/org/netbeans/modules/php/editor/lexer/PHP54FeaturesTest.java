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
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PHP54FeaturesTest extends PHPLexerTestBase {

    public PHP54FeaturesTest(String testName) {
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

    public void testTraits_01() throws Exception {
        performTest("lexer/traits_01");
    }

    public void testTraits_02() throws Exception {
        performTest("lexer/traits_02");
    }

    public void testTraits_03() throws Exception {
        performTest("lexer/traits_03");
    }

    public void testTraits_04() throws Exception {
        performTest("lexer/traits_04");
    }

    public void testTraits_05() throws Exception {
        performTest("lexer/traits_05");
    }

    public void testTraits_06() throws Exception {
        performTest("lexer/traits_06");
    }

    public void testShortArrays_01() throws Exception {
        performTest("lexer/shortArrays_01");
    }

    public void testShortArrays_02() throws Exception {
        performTest("lexer/shortArrays_02");
    }

    public void testShortArraysStaticScalar_01() throws Exception {
        performTest("lexer/shortArraysStaticScalar_01");
    }

    public void testShortArraysStaticScalar_02() throws Exception {
        performTest("lexer/shortArraysStaticScalar_02");
    }

    public void testBinaryNotation_01() throws Exception {
        performTest("lexer/binaryNotation_01");
    }

    public void testBinaryNotation_02() throws Exception {
        performTest("lexer/binaryNotation_02");
    }

    public void testBinaryNotation_03() throws Exception {
        performTest("lexer/binaryNotation_03");
    }

    public void testBinaryNotation_04() throws Exception {
        performTest("lexer/binaryNotation_04");
    }

    // #268496
    public void testCallableKeyword() throws Exception {
        performTest("lexer/callableKeyword");
    }

    public void testTraitConst() throws Exception {
        performTest("lexer/traitConst");
    }
}
