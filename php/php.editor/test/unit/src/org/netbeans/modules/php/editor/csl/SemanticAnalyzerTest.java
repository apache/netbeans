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

package org.netbeans.modules.php.editor.csl;

/**
 *
 * @author Petr Pisl
 */
public class SemanticAnalyzerTest extends SemanticAnalysisTestBase {

    public SemanticAnalyzerTest(String testName) {
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

    public void testAnalysisFields() throws Exception {
        checkSemantic("testfiles/semantic/class001.php");
    }

    public void testAnalysisStatic() throws Exception {
        checkSemantic("testfiles/semantic/class002.php");
    }

    public void testAnalysisUnusedPrivateConstant() throws Exception {
        checkSemantic("testfiles/semantic/unusedPrivateConst.php");
    }

    public void testAnalysisUnusedPrivateField() throws Exception {
        checkSemantic("testfiles/semantic/class003.php");
    }

    public void testAnalysisUnusedPrivateMethod() throws Exception {
        checkSemantic("testfiles/semantic/class004.php");
    }

    public void testAnalysisAll() throws Exception {
        checkSemantic("testfiles/semantic/class005.php");
    }

    public void testAnalysisDeclarationAfterUsage() throws Exception {
        checkSemantic("testfiles/semantic/class006.php");
    }

    public void testIssue142005() throws Exception {
        checkSemantic("testfiles/semantic/class007.php");
    }

    // issue #139813
    public void testAbstract() throws Exception {
        checkSemantic("testfiles/semantic/abstract01.php");
    }

    public void testIssue142644() throws Exception {
        checkSemantic("testfiles/semantic/issue142644.php");
    }

    public void testIssue141041() throws Exception {
        checkSemantic("testfiles/semantic/issue141041.php");
    }

    public void testIssue146193() throws Exception {
        checkSemantic("testfiles/semantic/issue146193.php");
    }

    public void testIssue146197() throws Exception {
        checkSemantic("testfiles/semantic/issue146197.php");
    }

    public void testIssue145694() throws Exception {
        checkSemantic("testfiles/semantic/issue145694.php");
    }

    public void testIssue144195() throws Exception {
        checkSemantic("testfiles/semantic/issue144195.php");
    }

    public void testIssue154876() throws Exception {
        checkSemantic("testfiles/semantic/issue154876.php");
    }

    public void testVarComment01() throws Exception {
        checkSemantic("testfiles/semantic/simple01.php");
    }

    public void testVarComment02() throws Exception {
        checkSemantic("testfiles/semantic/mixed01.php");
    }

    public void testIssue194535() throws Exception {
        checkSemantic("testfiles/semantic/issue194535.php");
    }

    public void testTraits01() throws Exception {
        checkSemantic("testfiles/semantic/traits_01.php");
    }

    public void testTraits02() throws Exception {
        checkSemantic("testfiles/semantic/traits_02.php");
    }

    public void testTraits03() throws Exception {
        checkSemantic("testfiles/semantic/traits_03.php");
    }

    public void testTraits04() throws Exception {
        checkSemantic("testfiles/semantic/traits_04.php");
    }

    public void testTraits05() throws Exception {
        checkSemantic("testfiles/semantic/traits_05.php");
    }

    public void testTraits06() throws Exception {
        checkSemantic("testfiles/semantic/traits_06.php");
    }

    public void testConstantsColoring() throws Exception {
        checkSemantic("testfiles/semantic/constantsColoring.php");
    }

    public void testConstAccessInFiledDeclaration() throws Exception {
        checkSemantic("testfiles/semantic/constantsInFiledsDeclColoring.php");
    }

    public void testAnonymousClass01() throws Exception {
        checkSemantic("testfiles/semantic/anonymousClass01.php");
    }

    public void testIssue213105() throws Exception {
        checkSemantic("testfiles/semantic/issue213105.php");
    }

    public void testIssue213533() throws Exception {
        checkSemantic("testfiles/semantic/issue213533.php");
    }

    public void testIssue217239() throws Exception {
        checkSemantic("testfiles/semantic/issue217239.php");
    }

    public void testIssue216840() throws Exception {
        checkSemantic("testfiles/semantic/issue216840.php");
    }

    public void testIssue216840_02() throws Exception {
        checkSemantic("testfiles/semantic/issue216840_02.php");
    }

    public void testIssue245230() throws Exception {
        checkSemantic("testfiles/semantic/issue245230.php");
    }

    public void testIssue247411() throws Exception {
        // doesn't check unused private fields and methods for trait
        // fixed in #257985
        checkSemantic("testfiles/semantic/issue247411.php");
    }

    public void testIssue258676() throws Exception {
        checkSemantic("testfiles/semantic/issue258676.php");
    }

    public void testUniformVariableSyntax_01() throws Exception {
        checkSemantic("testfiles/semantic/uniformVariableSyntax/uniformVariableSyntax_01.php");
    }

    public void testUniformVariableSyntax_02() throws Exception {
        checkSemantic("testfiles/semantic/uniformVariableSyntax/uniformVariableSyntax_02.php");
    }

    public void testConstrucotorPropertyPromotion() throws Exception {
        checkSemantic("testfiles/semantic/constructorPropertyPromotion.php");
    }

    public void testNamedArgumentsColoring() throws Exception {
        checkSemantic("testfiles/semantic/namedArgumentsColoring.php");
    }

    public void testNETBEANS5719_01() throws Exception {
        checkSemantic("testfiles/semantic/netbeans5719_01.php");
    }

    public void testNETBEANS5719_02() throws Exception {
        checkSemantic("testfiles/semantic/netbeans5719_02.php");
    }

    public void testEnumerations() throws Exception {
        checkSemantic("testfiles/semantic/enumerations.php");
    }

    public void testEnumerationsWithPrivateConst() throws Exception {
        checkSemantic("testfiles/semantic/enumerationsWithPrivateConst.php");
    }

    public void testConstantsInTraits() throws Exception {
        checkSemantic("testfiles/semantic/constantsInTraits.php");
    }

    public void testGH5551_01() throws Exception {
        checkSemantic("testfiles/semantic/gh5551_01.php");
    }

    public void testGH5551_02() throws Exception {
        checkSemantic("testfiles/semantic/gh5551_02.php");
    }

    public void testDynamicClassConstantFetch_01() throws Exception {
        checkSemantic("testfiles/semantic/dynamicClassConstantFetch_01.php");
    }
}
