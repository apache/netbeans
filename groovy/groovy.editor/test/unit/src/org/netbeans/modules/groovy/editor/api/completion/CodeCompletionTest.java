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

package org.netbeans.modules.groovy.editor.api.completion;

import java.util.Collections;
import java.util.Set;

/**
 *
 * @author schmidtm
 */
public class CodeCompletionTest extends GroovyCCTestBase {

    String TEST_BASE = "testfiles/completion/";

    public CodeCompletionTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestType() {
        return ".";
    }

    @Override
    protected Set<String> additionalSourceClassPath() {
        return Collections.singleton(TEST_BASE);
    }

    public void testMethodCompletion1() throws Exception {
        checkCompletion(TEST_BASE + "MethodCompletionTestCase.groovy", "new String().toS^", false);
    }

    public void testMethodCompletion2() throws Exception {
        checkCompletion(TEST_BASE + "MethodCompletionTestCase.groovy", "new String().find^", false);
    }

    public void testScriptLong1() throws Exception {
        checkCompletion(TEST_BASE + "ScriptLong1.groovy", "l.MA^", false);
    }

    public void testScriptLong2() throws Exception {
        checkCompletion(TEST_BASE + "ScriptLong2.groovy", "l.comp^", false);
    }

    public void testScriptString1() throws Exception {
        checkCompletion(TEST_BASE + "ScriptString1.groovy", "s.val^", false);
    }

    public void testScriptString2() throws Exception {
        checkCompletion(TEST_BASE + "ScriptString2.groovy", "s.spli^", false);
    }

    public void testScriptStringConst1() throws Exception {
        checkCompletion(TEST_BASE + "ScriptStringConst1.groovy", "\" ddd \".toS^", false);
    }

    public void testClassMethodFieldString1() throws Exception {
        checkCompletion(TEST_BASE + "ClassMethodFieldString1.groovy", "stringField.toL^", false);
    }

    public void testClassMethodFieldString2() throws Exception {
        checkCompletion(TEST_BASE + "ClassMethodFieldString2.groovy", "stringField.spli^", false);
    }

    public void testClassMethodFieldLong1() throws Exception {
        checkCompletion(TEST_BASE + "ClassMethodFieldLong1.groovy", "longField.MAX^", false);
    }

    public void testClassMethodFieldLong2() throws Exception {
        checkCompletion(TEST_BASE + "ClassMethodFieldLong2.groovy", "longField.comp^", false);
    }

    public void testClassMethodLocalLong1() throws Exception {
        checkCompletion(TEST_BASE + "ClassMethodLocalLong1.groovy", "localLong.MAX^", false);
    }

    public void testClassMethodLocalLong2() throws Exception {
        checkCompletion(TEST_BASE + "ClassMethodLocalLong2.groovy", "localLong.comp^", false);
    }

    public void testClassMethodLocalString1() throws Exception {
        checkCompletion(TEST_BASE + "ClassMethodLocalString1.groovy", "localString.toL^", false);
    }

    public void testClassMethodLocalString2() throws Exception {
        checkCompletion(TEST_BASE + "ClassMethodLocalString2.groovy", "localString.get^", false);
    }

    public void testKeywordImport1() throws Exception {
        checkCompletion(TEST_BASE + "KeywordImport1.groovy", "import java.lang.Ab^", false);
    }

    public void testKeywordAboveClass1() throws Exception {
        checkCompletion(TEST_BASE + "KeywordAboveClass1.groovy", "ab^", false);
    }

    public void testKeywordAboveClass2() throws Exception {
        checkCompletion(TEST_BASE + "KeywordAboveClass2.groovy", "ab^", false);
    }

//    // proper recognition of Constructor calls and the corresponding types.
//
//    public void testConstructorCall1() throws Exception {
//        checkCompletion(TEST_BASE + "ConstructorCall1.groovy", "println new URL(\"http://google.com\").getT^", false);
//    }
//
//    // Test CamelCase constructor-proposals
//
//    public void testCamelCaseConstructor1() throws Exception {
//        checkCompletion(TEST_BASE + "CamelCaseConstructor1.groovy", "SSC^", false);
//    }
//
//
//    // Package completion could not be tested at the moment, since this statement returns nothing for "java.n|":
////    pkgSet = pathInfo.getClassIndex().getPackageNames(packageRequest.fullString, true, EnumSet.allOf(ClassIndex.SearchScope.class));
//
////    public void testKeywordImport2() throws Exception {
////        checkCompletion(TEST_BASE + "KeywordImport2.groovy", "import java.n^", false);
////        assertTrue(false);
////    }
//
//
////    Testing all completion possibilities for java.lang.String is broken
//
////    public void testClassMethodLocalStringConst1() throws Exception {
////        checkCompletion(TEST_BASE + "ClassMethodLocalStringConst1.groovy", "\" ddd \".^", false);
////    }

}
