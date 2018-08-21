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

import javax.lang.model.SourceVersion;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.parsing.JavacParser;

/**
 *
 * @author arusinha
 */
public class JavaCompletionTask111FeaturesTest extends CompletionTestBase {

    //Todo: Verification is pending on nb-javac of JDK11
    public JavaCompletionTask111FeaturesTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        try {
            SourceVersion.valueOf("RELEASE_11");
            suite.addTestSuite(JavaCompletionTask111FeaturesTest.class);
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_11, skip tests
            suite.addTest(new JavaCompletionTask111FeaturesTest("noop"));
        }
        return suite;
    }

    // JDK-1.11 lambda expressions var param type support tests ---------------------------------------
    public void testEmptyFileAfterTypingLambdaParam() throws Exception {
        performTest("SimpleLambdaExpression2Start", 1060, "t.test((s ", "empty.pass", "1.11");
    }

    public void testEmptyFileAfterTypingTypeOfLambdaParam() throws Exception {
        performTest("SimpleLambdaExpression2Start", 1060, "t.test((String ", "stringVarName.pass", "1.11");
    }

    public void testFirstLambdaParam() throws Exception {
        performTest("SimpleLambdaExpression2Start", 1066, "t.test((", "lambdaParameterTypesIncludingVar.pass", "1.11");
    }

    public void testSecondLambdaParam1() throws Exception {
        performTest("SimpleLambdaExpression2Start", 1060, "t.test((s,", "empty.pass", "1.11");
    }

    public void testSecondLambdaParam2() throws Exception {
        performTest("SimpleLambdaExpression2Start", 1060, "t.test((String s,", "lambdaParameterTypesExcludingVar.pass", "1.11");
    }

    public void testSecondLambdaParam3() throws Exception {
        performTest("SimpleLambdaExpression2Start", 1060, "t.test((var s,", "var.pass", "1.11");
    }

    public void testSecondLambdaParam4() throws Exception {
        performTest("SimpleLambdaExpression2Start", 1068, "t.test2( \"hello\",2,( var s,", "var.pass", "1.11");
    }

    public void testSecondLambdaParam5() throws Exception {
        performTest("SimpleLambdaExpression2Start", 1068, "t.test2( \"hello\",2,( String s,", "lambdaParameterTypesExcludingVar.pass", "1.11");
    }

    public void noop() {
    }

    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
}
