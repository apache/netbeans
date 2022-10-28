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
public class JavaCompletionTask113FeaturesTest extends CompletionTestBase {

    private static String SOURCE_LEVEL = "1.13"; //NOI18N

    public JavaCompletionTask113FeaturesTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        try {
            SourceVersion.valueOf("RELEASE_13"); //NOI18N
            suite.addTestSuite(JavaCompletionTask113FeaturesTest.class);
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_13, skip tests
            suite.addTest(new JavaCompletionTask113FeaturesTest("noop")); //NOI18N
        }
        return suite;
    }

    public void testSwitchExprAutoCompleteYieldValue() throws Exception {
        performTest("SwitchExprForYieldWithValue", 1019, "yi", "SwitchExprYieldAutoCompletion.pass", SOURCE_LEVEL);
    }

    public void testSwitchExprAutoCompleteYieldValue2() throws Exception {
        performTest("SwitchExprForYieldWithValue2", 1023, "yi", "SwitchExprYieldAutoCompletion.pass", SOURCE_LEVEL);
    }

    public void testSwitchExprAutoCompleteAfterYield() throws Exception {
        performTest("SwitchExprForYieldWithValue", 1019, "yield ", "SwitchExprAfterYieldAutoCompletion.pass", SOURCE_LEVEL);
    }

    public void testSwitchExprAutoCompleteAfterYield2() throws Exception {
        performTest("SwitchExprForYieldWithValue2", 1023, "yield ", "SwitchExprAfterYieldAutoCompletion.pass", SOURCE_LEVEL);
    }

    public void noop() {
    }

    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
}
