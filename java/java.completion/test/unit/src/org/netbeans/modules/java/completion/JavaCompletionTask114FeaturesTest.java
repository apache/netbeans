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
public class JavaCompletionTask114FeaturesTest extends CompletionTestBase {

    private static String SOURCE_LEVEL = "14"; //NOI18N

    public JavaCompletionTask114FeaturesTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        try {
            SourceVersion.valueOf("RELEASE_14"); //NOI18N
            suite.addTestSuite(JavaCompletionTask114FeaturesTest.class);
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_13, skip tests
            suite.addTest(new JavaCompletionTask114FeaturesTest("noop")); //NOI18N
        }
        return suite;
    }

    public void testBindingUse() throws Exception {
        performTest("GenericMethodInvocation", 1231, "boolean b = argO instanceof String str && st", "BindingUse.pass", SOURCE_LEVEL);
    }

    public void noop() {
    }

    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
}
