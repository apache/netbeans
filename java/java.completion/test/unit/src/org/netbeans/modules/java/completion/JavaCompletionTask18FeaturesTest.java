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
        performTest("Empty", 808, "package test;\ninterface Test {", "interfaceMemberModifiersAndTypes.pass", "1.8");
    }

    public void testBeforeTypingDefaultModifier() throws Exception {
        performTest("SimpleInterfaceNoExtends", 840, null, "interfaceMemberModifiersAndTypes.pass", "1.8");
    }

    public void testBeforeDefaultModifier() throws Exception {
        performTest("Interface", 846, null, "interfaceMemberModifiersAndTypes.pass", "1.8");
    }

    public void testEmptyFileTypingDefaultModifier() throws Exception {
        performTest("Empty", 808, "package test;\ninterface Test {\nde", "defaultKeyword.pass", "1.8");
    }

    public void testTypingDefaultModifier() throws Exception {
        performTest("SimpleInterfaceNoExtends", 840, "\nde", "defaultKeyword.pass", "1.8");
    }

    public void testOnDefaultModifier() throws Exception {
        performTest("Interface", 848, null, "defaultKeyword.pass", "1.8");
    }

    public void testEmptyFileAfterTypingDefaultModifier() throws Exception {
        performTest("Empty", 808, "package test;\ninterface Test {\ndefault ", "interfaceMemberModifiersAndTypesWithoutDefaultAbstractAndStatic.pass", "1.8");
    }

    public void testAfterTypingDefaultModifier() throws Exception {
        performTest("SimpleInterfaceNoExtends", 840, "\ndefault ", "interfaceMemberModifiersAndTypesWithoutDefaultAbstractAndStatic.pass", "1.8");
    }

    public void testAfterDefaultModifier() throws Exception {
        performTest("Interface", 854, null, "interfaceMemberModifiersAndTypesWithoutDefaultAbstractAndStatic.pass", "1.8");
    }

    public void testEmptyFileAfterTypingStaticModifier() throws Exception {
        performTest("Empty", 808, "package test;\ninterface Test {\nstatic ", "memberModifiersAndTypesWithoutStatic.pass", "1.8");
    }

    public void testAfterTypingStaticModifier() throws Exception {
        performTest("SimpleInterfaceNoExtends", 840, "\nstatic ", "memberModifiersAndTypesWithoutStatic.pass", "1.8");
    }

    public void testAfterStaticModifier() throws Exception {
        performTest("Interface", 911, null, "memberModifiersAndTypesWithoutStatic.pass", "1.8");
    }

    public void testEmptyFileTypingDefaultMethodBody() throws Exception {
        performTest("Empty", 808, "package test;\ninterface Test {\ndefault String name() {", "typesInterfaceMembersAndVars1.pass", "1.8");
    }

    public void testTypingDefaultMethodBody() throws Exception {
        performTest("SimpleInterfaceNoExtends", 840, "\ndefault String name() {", "typesInterfaceMembersAndVars1.pass", "1.8");
    }

    public void testInsideDefaultMethodBody() throws Exception {
        performTest("Interface", 878, null, "typesInterfaceMembersAndVars.pass", "1.8");
    }

    public void testEmptyFileTypingStaticMethodBody() throws Exception {
        performTest("Empty", 808, "package test;\ninterface Test {\nstatic int length(String s) {", "typesStaticInterfaceMembersAndVars.pass", "1.8");
    }

    public void testTypingStaticMethodBody() throws Exception {
        performTest("SimpleInterfaceNoExtends", 840, "\nstatic int length(String s) {", "typesStaticInterfaceMembersAndVars.pass", "1.8");
    }

    public void testInsideStaticMethodBody() throws Exception {
        performTest("Interface", 942, null, "typesStaticInterfaceMembersAndVars.pass", "1.8");
    }

    // Java 1.8 lambda expressions tests ---------------------------------------
    
    public void testEmptyFileAfterTypingLambdaParam() throws Exception {
        performTest("SimpleLambdaExpressionStart", 1004, "t.test(s ", "empty.pass", "1.8");
    }

    public void testAfterTypingLambdaParam() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 1004, "t.test(s ", "empty.pass", "1.8");
    }

    public void testAfterLambdaParam() throws Exception {
        performTest("SimpleLambdaExpression", 1014, null, "empty.pass", "1.8");
    }

    public void testEmptyFileAfterTypingTypeOfLambdaParam() throws Exception {
        performTest("SimpleLambdaExpressionStart", 1004, "t.test((String ", "stringVarName.pass", "1.8");
    }

    public void testAfterTypingTypeOfLambdaParam() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 1004, "t.test((String ", "stringVarName.pass", "1.8");
    }

    public void testAfterTypeOfLambdaParam() throws Exception {
        performTest("SimpleLambdaExpression", 1074, null, "stringVarName.pass", "1.8");
    }

    public void testEmptyFileAfterTypingTypedLambdaParam() throws Exception {
        performTest("SimpleLambdaExpressionStart", 1004, "t.test((String s ", "empty.pass", "1.8");
    }

    public void testAfterTypingTypedLambdaParam() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 1004, "t.test((String s ", "empty.pass", "1.8");
    }

    public void testAfterTypedLambdaParam() throws Exception {
        performTest("SimpleLambdaExpression", 1075, " ", "empty.pass", "1.8");
    }

    public void testEmptyFileTypingSecondLambdaParam() throws Exception {
        performTest("SimpleLambdaExpressionStart", 1004, "t.test((s,", "empty.pass", "1.8");
    }

    public void testTypingSecondLambdaParam() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 1004, "t.test((s,", "empty.pass", "1.8");
    }

    public void testBeforeSecondLambdaParam() throws Exception {
        performTest("SimpleLambdaExpression", 1040, ",", "empty.pass", "1.8");
    }

    public void testEmptyFileTypingSecondTypedLambdaParam() throws Exception {
        performTest("SimpleLambdaExpressionStart", 1004, "t.test((String s,", "lambdaParameterTypes.pass", "1.8");
    }

    public void testTypingSecondTypedLambdaParam() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 1004, "t.test((String s,", "lambdaParameterTypes.pass", "1.8");
    }

    public void testBeforeSecondTypedLambdaParam() throws Exception {
        performTest("SimpleLambdaExpression", 1075, ",", "lambdaParameterTypes.pass", "1.8");
    }

    public void testEmptyFileAfterTypingTypedLambdaParams() throws Exception {
        performTest("SimpleLambdaExpressionStart", 1004, "t.test((String s)", "empty.pass", "1.8");
    }

    public void testAfterTypingTypedLambdaParams() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 1004, "t.test((String s)", "empty.pass", "1.8");
    }

    public void testAfterTypedLambdaParams() throws Exception {
        performTest("SimpleLambdaExpression", 1076, null, "empty.pass", "1.8");
    }

    public void testEmptyFileAfterTypingTypedLambdaParamsAndSpace() throws Exception {
        performTest("SimpleLambdaExpressionStart", 1004, "t.test((String s) ", "empty.pass", "1.8");
    }

    public void testAfterTypingTypedLambdaParamsAndSpace() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 1004, "t.test((String s) ", "empty.pass", "1.8");
    }

    public void testAfterTypedLambdaParamsAndSpace() throws Exception {
        performTest("SimpleLambdaExpression", 1077, null, "empty.pass", "1.8");
    }

    public void testEmptyFileAfterTypingLambdaArrow() throws Exception {
        performTest("SimpleLambdaExpressionStart", 1004, "t.test(s ->", "lambdaSmartInt.pass", "1.8");
    }

    public void testAfterTypingLambdaArrow() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 1004, "t.test(s ->", "lambdaSmartInt.pass", "1.8");
    }

    public void testAfterLambdaArrow() throws Exception {
        performTest("SimpleLambdaExpression", 1016, null, "lambdaSmartInt.pass", "1.8");
    }

    public void testEmptyFileAfterTypingTypedLambdaArrow() throws Exception {
        performTest("SimpleLambdaExpressionStart", 1004, "t.test((String s) ->", "lambdaSmartInt.pass", "1.8");
    }

    public void testAfterTypingTypedLambdaArrow() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 1004, "t.test((String s) ->", "lambdaSmartInt.pass", "1.8");
    }

    public void testAfterTypedLambdaArrow() throws Exception {
        performTest("SimpleLambdaExpression", 1079, null, "lambdaSmartInt.pass", "1.8");
    }

    public void testEmptyFileAfterTypingLambdaArrowAndSpace() throws Exception {
        performTest("SimpleLambdaExpressionStart", 1004, "t.test(s -> ", "lambdaSmartInt.pass", "1.8");
    }

    public void testAfterTypingLambdaArrowAndSpace() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 1004, "t.test(s -> ", "lambdaSmartInt.pass", "1.8");
    }

    public void testAfterLambdaArrowAndSpace() throws Exception {
        performTest("SimpleLambdaExpression", 1017, null, "lambdaSmartInt.pass", "1.8");
    }

    public void testEmptyFileAfterTypingTypedLambdaArrowAndSpace() throws Exception {
        performTest("SimpleLambdaExpressionStart", 1004, "t.test((String s) -> ", "lambdaSmartInt.pass", "1.8");
    }

    public void testAfterTypingTypedLambdaArrowAndSpace() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 1004, "t.test((String s) -> ", "lambdaSmartInt.pass", "1.8");
    }

    public void testAfterTypedLambdaArrowAndSpace() throws Exception {
        performTest("SimpleLambdaExpression", 1080, null, "lambdaSmartInt.pass", "1.8");
    }

    public void testEmptyFileAfterTypingLambdaExpression() throws Exception {
        performTest("SimpleLambdaExpressionStart", 1004, "t.test(s -> s.length()", "empty.pass", "1.8");
    }

    public void testAfterTypingLambdaExpression() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 1004, "t.test(s -> s.length()", "empty.pass", "1.8");
    }

    public void testAfterLambdaExpression() throws Exception {
        performTest("SimpleLambdaExpression", 1027, null, "empty.pass", "1.8");
    }
    
    public void testEmptyFileTypingLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpressionStart", 1004, "t.test(s -> {", "lambdaBodyContent.pass", "1.8");
    }

    public void testTypingLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 1004, "t.test(s -> {", "lambdaBodyContent.pass", "1.8");
    }

    public void testInsideOfLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpression", 1117, null, "lambdaBodyContent.pass", "1.8");
    }

    public void testEmptyFileTypingTypedLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpressionStart", 1004, "t.test((String s) -> {", "lambdaBodyContent.pass", "1.8");
    }

    public void testTypingTypedLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 1004, "t.test((String s) -> {", "lambdaBodyContent.pass", "1.8");
    }

    public void testInsideOfTypedLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpression", 1081, null, "lambdaBodyContent.pass", "1.8");
    }
    
    public void testEmptyFileAfterLocalVarInLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpressionStart", 1004, "t.test(s -> {int i; ", "lambdaBodyContentWithLocalVar.pass", "1.8");
    }

    public void testTypingAfterLocalVarInLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 1004, "t.test(s -> {int i; ", "lambdaBodyContentWithLocalVar.pass", "1.8");
    }

    public void testAfterLocalVarInLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpression", 1081, "int i; ", "lambdaBodyContentWithLocalVar.pass", "1.8");
    }

    public void testEmptyFileTypingReturnFromLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpressionStart", 1004, "t.test(s -> {return ", "lambdaSmartInt.pass", "1.8");
    }

    public void testTypingReturnFromLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 1004, "t.test(s -> {return ", "lambdaSmartInt.pass", "1.8");
    }

    public void testAfterReturnFromLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpression", 1124, null, "lambdaSmartInt.pass", "1.8");
    }

    public void testEmptyFileTypingReturnFromTypedLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpressionStart", 1004, "t.test((String s) -> {return ", "lambdaSmartInt.pass", "1.8");
    }

    public void testTypingReturnFromTypedLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpressionEmptyMethodBody", 1004, "t.test((String s) -> {return ", "lambdaSmartInt.pass", "1.8");
    }

    public void testAfterReturnFromTypedLambdaBlock() throws Exception {
        performTest("SimpleLambdaExpression", 1088, null, "lambdaSmartInt.pass", "1.8");
    }

    public void testMoreCandidatesEmptyFileAfterTypingLambdaArrow() throws Exception {
        performTest("LambdaExpressionStart", 1102, "t.test(s ->", "lambdaExpression.pass", "1.8");
    }

    public void testMoreCandidatesAfterTypingLambdaArrow() throws Exception {
        performTest("LambdaExpressionEmptyMethodBody", 1102, "t.test(s ->", "lambdaExpression.pass", "1.8");
    }

    public void testMoreCandidatesAfterLambdaArrow() throws Exception {
        performTest("LambdaExpression", 1122, null, "lambdaSmartInt2.pass", "1.8");
    }
    
    public void testMoreCandidatesEmptyFileTypingReturnFromLambdaBlock() throws Exception {
        performTest("LambdaExpressionStart", 1102, "t.test(s -> {return ", "lambdaExpression.pass", "1.8");
    }

    public void testMoreCandidatesTypingReturnFromLambdaBlock() throws Exception {
        performTest("LambdaExpressionEmptyMethodBody", 1102, "t.test(s -> {return ", "lambdaExpression.pass", "1.8");
    }

    public void testMoreCandidatesAfterReturnFromLambdaBlock() throws Exception {
        performTest("LambdaExpression", 1159, null, "lambdaSmartInt2.pass", "1.8");
    }
    
    public void testLambdaExpressionOutsideMethodBody() throws Exception {
        performTest("LambdaExpressionOutsideMethodBody", 959, null, "lambdaOutsideMethodBodyContent.pass", "1.8");
    }
    
    public void testNETBEANS_4259() throws Exception {
        performTest("LambdaExpression", 986, "return (String s", "stringVarName.pass", "1.8");
    }

    public void testEffectivellyFinal() throws Exception {
        performTest("Final", 1125, "var", "effectivellyFinal.pass", "1.8");
    }

    public void testDiamond1() throws Exception {
        performTest("Empty", 808,
                """
                package test;
                public class Test {
                    public static class Test<T> {
                        public Test(T value) { }
                        public Test(String name, T value) { }
                    }
                    private static void test() {
                        new Test<>
                """,
                "diamond1.pass", "1.8");
    }

    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
}
