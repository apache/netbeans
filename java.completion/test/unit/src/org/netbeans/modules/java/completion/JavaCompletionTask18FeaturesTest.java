/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.java.completion;

import org.netbeans.modules.java.source.parsing.JavacParser;

/**
 *
 * @author Dusan Balek
 */
public class JavaCompletionTask18FeaturesTest extends CompletionTestBase {

    public JavaCompletionTask18FeaturesTest(String testName) {
        super(testName);
    }

    // Java 1.8 default method tests -------------------------------------------
    
    public void testEmptyFileBeforeTypingDefaultModifier() throws Exception {
        performTest("Empty", 0, "package test;\ninterface Test {", "interfaceMemberModifiersAndTypes.pass", "1.8");
    }

    public void testBeforeTypingDefaultModifier() throws Exception {
        performTest("SimpleInterfaceNoExtends", 31, null, "interfaceMemberModifiersAndTypes.pass", "1.8");
    }

    public void testBeforeDefaultModifier() throws Exception {
        performTest("Interface", 37, null, "interfaceMemberModifiersAndTypes.pass", "1.8");
    }

    public void testEmptyFileTypingDefaultModifier() throws Exception {
        performTest("Empty", 0, "package test;\ninterface Test {\nde", "defaultKeyword.pass", "1.8");
    }

    public void testTypingDefaultModifier() throws Exception {
        performTest("SimpleInterfaceNoExtends", 31, "\nde", "defaultKeyword.pass", "1.8");
    }

    public void testOnDefaultModifier() throws Exception {
        performTest("Interface", 39, null, "defaultKeyword.pass", "1.8");
    }

    public void testEmptyFileAfterTypingDefaultModifier() throws Exception {
        performTest("Empty", 0, "package test;\ninterface Test {\ndefault ", "interfaceMemberModifiersAndTypesWithoutDefaultAbstractAndStatic.pass", "1.8");
    }

    public void testAfterTypingDefaultModifier() throws Exception {
        performTest("SimpleInterfaceNoExtends", 31, "\ndefault ", "interfaceMemberModifiersAndTypesWithoutDefaultAbstractAndStatic.pass", "1.8");
    }

    public void testAfterDefaultModifier() throws Exception {
        performTest("Interface", 45, null, "interfaceMemberModifiersAndTypesWithoutDefaultAbstractAndStatic.pass", "1.8");
    }

    public void testEmptyFileAfterTypingStaticModifier() throws Exception {
        performTest("Empty", 0, "package test;\ninterface Test {\nstatic ", "memberModifiersAndTypesWithoutStatic.pass", "1.8");
    }

    public void testAfterTypingStaticModifier() throws Exception {
        performTest("SimpleInterfaceNoExtends", 31, "\nstatic ", "memberModifiersAndTypesWithoutStatic.pass", "1.8");
    }

    public void testAfterStaticModifier() throws Exception {
        performTest("Interface", 102, null, "memberModifiersAndTypesWithoutStatic.pass", "1.8");
    }

    public void testEmptyFileTypingDefaultMethodBody() throws Exception {
        performTest("Empty", 0, "package test;\ninterface Test {\ndefault String name() {", "typesInterfaceMembersAndVars1.pass", "1.8");
    }

    public void testTypingDefaultMethodBody() throws Exception {
        performTest("SimpleInterfaceNoExtends", 31, "\ndefault String name() {", "typesInterfaceMembersAndVars1.pass", "1.8");
    }

    public void testInsideDefaultMethodBody() throws Exception {
        performTest("Interface", 69, null, "typesInterfaceMembersAndVars.pass", "1.8");
    }

    public void testEmptyFileTypingStaticMethodBody() throws Exception {
        performTest("Empty", 0, "package test;\ninterface Test {\nstatic int length(String s) {", "typesStaticInterfaceMembersAndVars.pass", "1.8");
    }

    public void testTypingStaticMethodBody() throws Exception {
        performTest("SimpleInterfaceNoExtends", 31, "\nstatic int length(String s) {", "typesStaticInterfaceMembersAndVars.pass", "1.8");
    }

    public void testInsideStaticMethodBody() throws Exception {
        performTest("Interface", 133, null, "typesStaticInterfaceMembersAndVars.pass", "1.8");
    }

    // Java 1.8 lambda expressions tests ---------------------------------------
    
    public void testEmptyFileAfterTypingLambdaParam() throws Exception {
        performTest("SimpleLambdaExpressionStart", 195, "t.test(s ", "empty.pass", "1.8");
    }

    public void testAfterTypingLambdaParam() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 195, "t.test(s ", "empty.pass", "1.8");
    }

    public void testAfterLambdaParam() throws Exception {
        performTest("SimpleLambdaExpression", 205, null, "empty.pass", "1.8");
    }

    public void testEmptyFileAfterTypingTypeOfLambdaParam() throws Exception {
        performTest("SimpleLambdaExpressionStart", 195, "t.test((String ", "stringVarName.pass", "1.8");
    }

    public void testAfterTypingTypeOfLambdaParam() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 195, "t.test((String ", "stringVarName.pass", "1.8");
    }

    public void testAfterTypeOfLambdaParam() throws Exception {
        performTest("SimpleLambdaExpression", 265, null, "stringVarName.pass", "1.8");
    }

    public void testEmptyFileAfterTypingTypedLambdaParam() throws Exception {
        performTest("SimpleLambdaExpressionStart", 195, "t.test((String s ", "empty.pass", "1.8");
    }

    public void testAfterTypingTypedLambdaParam() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 195, "t.test((String s ", "empty.pass", "1.8");
    }

    public void testAfterTypedLambdaParam() throws Exception {
        performTest("SimpleLambdaExpression", 266, " ", "empty.pass", "1.8");
    }

    public void testEmptyFileTypingSecondLambdaParam() throws Exception {
        performTest("SimpleLambdaExpressionStart", 195, "t.test((s,", "empty.pass", "1.8");
    }

    public void testTypingSecondLambdaParam() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 195, "t.test((s,", "empty.pass", "1.8");
    }

    public void testBeforeSecondLambdaParam() throws Exception {
        performTest("SimpleLambdaExpression", 231, ",", "empty.pass", "1.8");
    }

    public void testEmptyFileTypingSecondTypedLambdaParam() throws Exception {
        performTest("SimpleLambdaExpressionStart", 195, "t.test((String s,", "lambdaParameterTypes.pass", "1.8");
    }

    public void testTypingSecondTypedLambdaParam() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 195, "t.test((String s,", "lambdaParameterTypes.pass", "1.8");
    }

    public void testBeforeSecondTypedLambdaParam() throws Exception {
        performTest("SimpleLambdaExpression", 266, ",", "lambdaParameterTypes.pass", "1.8");
    }

    public void testEmptyFileAfterTypingTypedLambdaParams() throws Exception {
        performTest("SimpleLambdaExpressionStart", 195, "t.test((String s)", "empty.pass", "1.8");
    }

    public void testAfterTypingTypedLambdaParams() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 195, "t.test((String s)", "empty.pass", "1.8");
    }

    public void testAfterTypedLambdaParams() throws Exception {
        performTest("SimpleLambdaExpression", 267, null, "empty.pass", "1.8");
    }

    public void testEmptyFileAfterTypingTypedLambdaParamsAndSpace() throws Exception {
        performTest("SimpleLambdaExpressionStart", 195, "t.test((String s) ", "empty.pass", "1.8");
    }

    public void testAfterTypingTypedLambdaParamsAndSpace() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 195, "t.test((String s) ", "empty.pass", "1.8");
    }

    public void testAfterTypedLambdaParamsAndSpace() throws Exception {
        performTest("SimpleLambdaExpression", 268, null, "empty.pass", "1.8");
    }

    public void testEmptyFileAfterTypingLambdaArrow() throws Exception {
        performTest("SimpleLambdaExpressionStart", 195, "t.test(s ->", "lambdaSmartInt.pass", "1.8");
    }

    public void testAfterTypingLambdaArrow() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 195, "t.test(s ->", "lambdaSmartInt.pass", "1.8");
    }

    public void testAfterLambdaArrow() throws Exception {
        performTest("SimpleLambdaExpression", 207, null, "lambdaSmartInt.pass", "1.8");
    }

    public void testEmptyFileAfterTypingTypedLambdaArrow() throws Exception {
        performTest("SimpleLambdaExpressionStart", 195, "t.test((String s) ->", "lambdaSmartInt.pass", "1.8");
    }

    public void testAfterTypingTypedLambdaArrow() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 195, "t.test((String s) ->", "lambdaSmartInt.pass", "1.8");
    }

    public void testAfterTypedLambdaArrow() throws Exception {
        performTest("SimpleLambdaExpression", 270, null, "lambdaSmartInt.pass", "1.8");
    }

    public void testEmptyFileAfterTypingLambdaArrowAndSpace() throws Exception {
        performTest("SimpleLambdaExpressionStart", 195, "t.test(s -> ", "lambdaSmartInt.pass", "1.8");
    }

    public void testAfterTypingLambdaArrowAndSpace() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 195, "t.test(s -> ", "lambdaSmartInt.pass", "1.8");
    }

    public void testAfterLambdaArrowAndSpace() throws Exception {
        performTest("SimpleLambdaExpression", 208, null, "lambdaSmartInt.pass", "1.8");
    }

    public void testEmptyFileAfterTypingTypedLambdaArrowAndSpace() throws Exception {
        performTest("SimpleLambdaExpressionStart", 195, "t.test((String s) -> ", "lambdaSmartInt.pass", "1.8");
    }

    public void testAfterTypingTypedLambdaArrowAndSpace() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 195, "t.test((String s) -> ", "lambdaSmartInt.pass", "1.8");
    }

    public void testAfterTypedLambdaArrowAndSpace() throws Exception {
        performTest("SimpleLambdaExpression", 271, null, "lambdaSmartInt.pass", "1.8");
    }

    public void testEmptyFileAfterTypingLambdaExpression() throws Exception {
        performTest("SimpleLambdaExpressionStart", 195, "t.test(s -> s.length()", "empty.pass", "1.8");
    }

    public void testAfterTypingLambdaExpression() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 195, "t.test(s -> s.length()", "empty.pass", "1.8");
    }

    public void testAfterLambdaExpression() throws Exception {
        performTest("SimpleLambdaExpression", 218, null, "empty.pass", "1.8");
    }
    
    public void testEmptyFileTypingLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpressionStart", 195, "t.test(s -> {", "lambdaBodyContent.pass", "1.8");
    }

    public void testTypingLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 195, "t.test(s -> {", "lambdaBodyContent.pass", "1.8");
    }

    public void testInsideOfLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpression", 308, null, "lambdaBodyContent.pass", "1.8");
    }

    public void testEmptyFileTypingTypedLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpressionStart", 195, "t.test((String s) -> {", "lambdaBodyContent.pass", "1.8");
    }

    public void testTypingTypedLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 195, "t.test((String s) -> {", "lambdaBodyContent.pass", "1.8");
    }

    public void testInsideOfTypedLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpression", 272, null, "lambdaBodyContent.pass", "1.8");
    }
    
    public void testEmptyFileAfterLocalVarInLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpressionStart", 195, "t.test(s -> {int i; ", "lambdaBodyContentWithLocalVar.pass", "1.8");
    }

    public void testTypingAfterLocalVarInLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 195, "t.test(s -> {int i; ", "lambdaBodyContentWithLocalVar.pass", "1.8");
    }

    public void testAfterLocalVarInLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpression", 272, "int i; ", "lambdaBodyContentWithLocalVar.pass", "1.8");
    }

    public void testEmptyFileTypingReturnFromLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpressionStart", 195, "t.test(s -> {return ", "lambdaSmartInt.pass", "1.8");
    }

    public void testTypingReturnFromLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 195, "t.test(s -> {return ", "lambdaSmartInt.pass", "1.8");
    }

    public void testAfterReturnFromLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpression", 315, null, "lambdaSmartInt.pass", "1.8");
    }

    public void testEmptyFileTypingReturnFromTypedLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpressionStart", 195, "t.test((String s) -> {return ", "lambdaSmartInt.pass", "1.8");
    }

    public void testTypingReturnFromTypedLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 195, "t.test((String s) -> {return ", "lambdaSmartInt.pass", "1.8");
    }

    public void testAfterReturnFromTypedLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpression", 279, null, "lambdaSmartInt.pass", "1.8");
    }

    public void testMoreCandidatesEmptyFileAfterTypingLambdaArrow() throws Exception {
        performTest("LambdaExpressionStart", 293, "t.test(s ->", "lambdaExpression.pass", "1.8");
    }

    public void testMoreCandidatesAfterTypingLambdaArrow() throws Exception {
        performTest("LambdaExpressionEmptyMethodBody", 293, "t.test(s ->", "lambdaExpression.pass", "1.8");
    }

    public void testMoreCandidatesAfterLambdaArrow() throws Exception {
        performTest("LambdaExpression", 313, null, "lambdaSmartInt2.pass", "1.8");
    }
    
    public void testMoreCandidatesEmptyFileTypingReturnFromLambdaBlock() throws Exception {
        performTest("LambdaExpressionStart", 293, "t.test(s -> {return ", "lambdaExpression.pass", "1.8");
    }

    public void testMoreCandidatesTypingReturnFromLambdaBlock() throws Exception {
        performTest("LambdaExpressionEmptyMethodBody", 293, "t.test(s -> {return ", "lambdaExpression.pass", "1.8");
    }

    public void testMoreCandidatesAfterReturnFromLambdaBlock() throws Exception {
        performTest("LambdaExpression", 350, null, "lambdaSmartInt2.pass", "1.8");
    }
    
    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
}
