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
package org.netbeans.modules.java.completion;

import javax.lang.model.SourceVersion;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.parsing.JavacParser;

/**
 *
 * @author arusinha
 */
public class JavaCompletionTask111FeaturesTest extends CompletionTestBase {

    private static String SOURCE_LEVEL = "1.11"; //NOI18N

    //Todo: Verification is pending on nb-javac of JDK11
    public JavaCompletionTask111FeaturesTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        try {
            SourceVersion.valueOf("RELEASE_11"); //NOI18N
            suite.addTestSuite(JavaCompletionTask111FeaturesTest.class);
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_11, skip tests
            suite.addTest(new JavaCompletionTask111FeaturesTest("noop")); //NOI18N
        }
        return suite;
    }

    // JDK-1.11 lambda expressions var param type support tests ---------------------------------------
    public void testEmptyFileAfterTypingLambdaParam() throws Exception {
        performTest("SimpleLambdaExpression2Start", 1131, "t.test((s ", "empty.pass", SOURCE_LEVEL);
    }

    public void testEmptyFileAfterTypingTypeOfLambdaParam() throws Exception {
        performTest("SimpleLambdaExpression2Start", 1131, "t.test((String ", "stringVarName.pass", SOURCE_LEVEL);
    }

    public void testFirstLambdaParam() throws Exception {
        performTest("SimpleLambdaExpression2Start", 1131, "Foo obj = (final ", "lambdaParameterTypesIncludingVar1.pass", SOURCE_LEVEL);
    }

    public void testSecondLambdaParam() throws Exception {
        performTest("SimpleLambdaExpression2Start", 1131, "Foo obj = (@TestAnnotation var x,", "var.pass", SOURCE_LEVEL);
    }

    public void testThirdLambdaParam() throws Exception {
        performTest("SimpleLambdaExpression2Start", 1131, "Foo obj = (@TestAnnotation var x,var y,", "var.pass", SOURCE_LEVEL);
    }

    public void testFirstLambdaParam2() throws Exception {
        performTest("SimpleLambdaExpression2Start", 1131, "t.test((", "lambdaParameterTypesIncludingVar.pass", SOURCE_LEVEL);
    }

    public void testSecondLambdaParam1() throws Exception {
        performTest("SimpleLambdaExpression2Start", 1131, "t.test((s,", "empty.pass", SOURCE_LEVEL);
    }

    public void testSecondLambdaParam2() throws Exception {
        performTest("SimpleLambdaExpression2Start", 1131, "t.test((String s,", "lambdaParameterTypesExcludingVar.pass", SOURCE_LEVEL);
    }

    public void testSecondLambdaParam3() throws Exception {
        performTest("SimpleLambdaExpression2Start", 1131, "t.test2( \"hello\",2,( String s,", "lambdaParameterTypesExcludingVar.pass", SOURCE_LEVEL);
    }

    public void testFirstLambdaParamWithAnnotation() throws Exception {
        performTest("SimpleLambdaExpression2Start", 1131, "t.test((@TestAnnotation ", "lambdaParameterTypesIncludingVar.pass", SOURCE_LEVEL);
    }

    public void testThirdLambdaParamWithAnnotation() throws Exception {
        performTest("SimpleLambdaExpression2Start", 1131, "t.test((@TestAnnotation var x,@TestAnnotation var y,", "var.pass", SOURCE_LEVEL);
    }

    public void testThirdLambdaParamWithAnnotation1() throws Exception {
        performTest("SimpleLambdaExpression2Start", 1131, "t.test((@TestAnnotation var x, var y,", "var.pass", SOURCE_LEVEL);
    }

    public void testSecondLambdaParam4() throws Exception {
        if (shouldDisableForNETBEANS_1808()) return ;
        performTest("SimpleLambdaExpression2Start", 1131, "t.test((var s,", "var.pass", SOURCE_LEVEL);
    }

    public void testSecondLambdaParam5() throws Exception {
        if (shouldDisableForNETBEANS_1808()) return ;
        performTest("SimpleLambdaExpression2Start", 1131, "t.test2( \"hello\",2,( var s,", "var.pass", SOURCE_LEVEL);
    }

    public void testSecondLambdaParamWithAnnotation() throws Exception {
        if (shouldDisableForNETBEANS_1808()) return ;
        performTest("SimpleLambdaExpression2Start", 1131, "t.test((@TestAnnotation var x, ", "var.pass", SOURCE_LEVEL);
    }

    public void noop() {
    }

    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }

    private boolean shouldDisableForNETBEANS_1808() {
        try {
            Class.forName("com.sun.tools.javac.model.LazyTreeLoader");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
