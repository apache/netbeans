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

package org.netbeans.modules.groovy.editor.api.completion;

/**
 *
 * @author schmidtm
 */
public class MethodCCTest extends GroovyCCTestBase {

    public MethodCCTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestType() {
        return "method"; //NOI18N
    }

    public void testCompletionInsideFor1_1() throws Exception {
        checkCompletion(BASE + "CompletionInsideFor1.groovy", "for(new Date().get^", false);
    }

    public void testCompletionInsideFor1_2() throws Exception {
        checkCompletion(BASE + "CompletionInsideFor1.groovy", "for (String other in [1:\"Alice\", 2:\"Bob\"].^) {", false);
    }

    public void testMethods1_1() throws Exception {
        checkCompletion(BASE + "Methods1.groovy", "        new URL(\"http://google.com\").getPr^", false);
    }

    public void testMethods1_2() throws Exception {
        checkCompletion(BASE + "Methods1.groovy", "        new URL(\"http://google.com\").getP^r", false);
    }

    public void testMethods1_3() throws Exception {
        checkCompletion(BASE + "Methods1.groovy", "        new URL(\"http://google.com\").get^Pr", false);
    }

    public void testMethods1_4() throws Exception {
        checkCompletion(BASE + "Methods1.groovy", "        new URL(\"http://google.com\").^getPr", false);
    }

    public void testMethods2_1() throws Exception {
        checkCompletion(BASE + "Methods2.groovy", "        new Byte().^", false);
    }

    public void testMethods2_2() throws Exception {
        checkCompletion(BASE + "Methods2.groovy", "        new GroovyClass3().in^", false);
    }

    public void testCompletionInMethodCall1_1() throws Exception {
        checkCompletion(BASE + "CompletionInMethodCall1.groovy", "        new File(\"something\").ea^", false);
    }

    public void testCompletionInMethodCall2_1() throws Exception {
        checkCompletion(BASE + "CompletionInMethodCall2.groovy", "        new File(\"something\").c^", false);
    }

    public void testCompletionInMethodCall3_1() throws Exception {
        checkCompletion(BASE + "CompletionInMethodCall3.groovy", "if (new File(\"/\").is^) {", false);
    }

    public void testCompletionInArgument1_1() throws Exception {
        checkCompletion(BASE + "CompletionInArgument1.groovy", "println new URL(\"http://google.com\").getT^", false);
    }

    public void testCompletionForLiteral1_1() throws Exception {
        checkCompletion(BASE + "CompletionForLiteral1.groovy", "1.d^", false);
    }

    public void testCompletionForLiteral1_2() throws Exception {
        checkCompletion(BASE + "CompletionForLiteral1.groovy", "1.0.d^", false);
    }

    public void testCompletionForLiteral1_3() throws Exception {
        checkCompletion(BASE + "CompletionForLiteral1.groovy", "\"\".c^", false);
    }

    public void testCompletionInsideConstructor1_1() throws Exception {
        checkCompletion(BASE + "CompletionInsideConstructor1.groovy", "new File(\"/\").equals(new Date().a^", false);
    }

    public void testCompletionInsideConstructor1_2() throws Exception {
        checkCompletion(BASE + "CompletionInsideConstructor1.groovy", "new File(new Date().get^", false);
    }

    public void testCompletionInsideConstructor1_3() throws Exception {
        checkCompletion(BASE + "CompletionInsideConstructor1.groovy", "if (new File(new Date().get^", false);
    }

    public void testCompletionGeneratedAccessors1_1() throws Exception {
        checkCompletion(BASE + "CompletionGeneratedAccessors1.groovy", "        new Test().get^", false);
    }

    public void testCompletionGeneratedAccessors1_2() throws Exception {
        checkCompletion(BASE + "CompletionGeneratedAccessors1.groovy", "        new Test().set^", false);
    }
    
    public void testCompletionGeneratedAccessors1_3() throws Exception {
        checkCompletion(BASE + "CompletionGeneratedAccessors1.groovy", "        new Test().is^", false);
    }

    public void testCompletionGroovyClass1_1() throws Exception {
        checkCompletion(BASE + "CompletionGroovyClass1.groovy", "        new Test1().^", false);
    }

    public void testCompletionGroovyThis1_1() throws Exception {
        checkCompletion(BASE + "CompletionGroovyThis1.groovy", "        this.get^", false);
    }

    public void testCompletionGroovySuper1_1() throws Exception {
        checkCompletion(BASE + "CompletionGroovySuper1.groovy", "        super.^", false);
    }

    public void testCompletionNoDot1_1() throws Exception {
        checkCompletion(BASE + "CompletionNoDot1.groovy", "        no^", false);
    }

    public void testCompletionNoDot1_2() throws Exception {
        checkCompletion(BASE + "CompletionNoDot1.groovy", "        x^", false);
    }

    public void testCompletionNoDot1_3() throws Exception {
        checkCompletion(BASE + "CompletionNoDot1.groovy", "        n^", false);
    }

    public void testCompletionNoPrefixString1() throws Exception {
        checkCompletion(BASE + "CompletionNoPrefixString1.groovy", "println \"Hello $name!\".^", false);
    }

    public void testCompletionNoPrefixString2() throws Exception {
        checkCompletion(BASE + "CompletionNoPrefixString2.groovy", "def name='Petr'.^", false);
    }
}

