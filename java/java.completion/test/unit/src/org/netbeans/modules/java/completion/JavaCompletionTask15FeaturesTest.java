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

/**
 *
 * @author Dusan Balek
 */
public class JavaCompletionTask15FeaturesTest extends CompletionTestBase {

    public JavaCompletionTask15FeaturesTest(String testName) {
        super(testName);
    }

    // Java 1.5 generics tests -------------------------------------------------------

    public void testEmptyFileBeforeTypingFirstTypeParam() throws Exception {
        performTest("GenericsStart", 841, "<", "empty.pass");
    }

    public void testBeforeTypingFirstTypeParam() throws Exception {
        performTest("GenericsNoTypeParams", 841, "<", "empty.pass");
    }
    
    public void testBeforeFirstTypeParam() throws Exception {
        performTest("Generics", 842, null, "empty.pass");
    }
    
    public void testEmptyFileTypingFirstTypeParam() throws Exception {
        performTest("GenericsStart", 841, "<X", "empty.pass");
    }
    
    public void testTypingFirstTypeParam() throws Exception {
        performTest("GenericsNoTypeParams", 841, "<X", "empty.pass");
    }
    
    public void testOnFirstTypeParam() throws Exception {
        performTest("Generics", 843, null, "empty.pass");
    }

    public void testEmptyFileAfterTypingFirstTypeParamAndSpace() throws Exception {
        performTest("GenericsStart", 841, "<X ", "extendsKeyword.pass");
    }
    
    public void testAfterTypingFirstTypeParamAndSpace() throws Exception {
        performTest("GenericsNoTypeParams", 841, "<X ", "extendsKeyword.pass");
    }
    
    public void testAfterFirstTypeParamAndSpace() throws Exception {
        performTest("Generics", 844, null, "extendsKeyword.pass");
    }

    public void testEmptyFileTypingExtendsInFirstTypeParam() throws Exception {
        performTest("GenericsStart", 841, "<X e", "extendsKeyword.pass");
    }
    
    public void testTypingExtendsInFirstTypeParam() throws Exception {
        performTest("GenericsNoTypeParams", 841, "<X e", "extendsKeyword.pass");
    }
    
    public void testOnExtendsInFirstTypeParam() throws Exception {
        performTest("Generics", 845, null, "extendsKeyword.pass");
    }

    public void testEmptyFileAfterTypingExtendsInFirstTypeParam() throws Exception {
        performTest("GenericsStart", 841, "<X extends", "extendsKeyword.pass");
    }
    
    public void testAfterTypingExtendsInFirstTypeParam() throws Exception {
        performTest("GenericsNoTypeParams", 841, "<X extends", "extendsKeyword.pass");
    }
    
    public void testAfterExtendsInFirstTypeParam() throws Exception {
        performTest("Generics", 851, null, "extendsKeyword.pass");
    }

    public void testEmptyFileAfterTypingExtendsAndSpaceInFirstTypeParam() throws Exception {
        performTest("GenericsStart", 841, "<X extends ", "javaLangContentAndTestClass.pass");
    }
    
    public void testAfterTypingExtendsAndSpaceInFirstTypeParam() throws Exception {
        performTest("GenericsNoTypeParams", 841, "<X extends ", "javaLangContentAndTestClass.pass");
    }
    
    public void testAfterExtendsAndSpaceInFirstTypeParam() throws Exception {
        performTest("Generics", 852, null, "javaLangContentAndTestClass.pass");
    }

    public void testEmptyFileAfterTypingBoundedFirstTypeParamAndSpace() throws Exception {
        performTest("GenericsStart", 841, "<X extends Number ", "empty.pass");
    }
    
    public void testAfterTypingBoundedFirstTypeParamAndSpace() throws Exception {
        performTest("GenericsNoTypeParams", 841, "<X extends Number ", "empty.pass");
    }
    
    public void testAfterBoundedFirstTypeParamAndSpace() throws Exception {
        performTest("Generics", 858, " ", "empty.pass");
    }
    
    public void testEmptyFileAfterTypingFirstTypeParam() throws Exception {
        performTest("GenericsStart", 841, "<X extends Number,", "empty.pass");
    }
    
    public void testAfterTypingFirstTypeParam() throws Exception {
        performTest("GenericsNoTypeParams", 841, "<X extends Number,", "empty.pass");
    }
    
    public void testAfterFirstTypeParam() throws Exception {
        performTest("Generics", 859, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingTypeParams() throws Exception {
        performTest("GenericsStart", 841, "<X extends Number, Y extends RuntimeException>", "extendsAndImplementsKeywords.pass");
    }
    
    public void testAfterTypingTypeParams() throws Exception {
        performTest("GenericsNoTypeParams", 841, "<X extends Number, Y extends RuntimeException>", "extendsAndImplementsKeywords.pass");
    }
    
    public void testAfterTypeParams() throws Exception {
        performTest("Generics", 887, null, "extendsAndImplementsKeywords.pass");
    }

    public void testEmptyFileBeforeTypingFirstMethodTypeParam() throws Exception {
        performTest("MethodStart", 849, "public <", "empty.pass");
    }

    public void testBeforeTypingFirstMethodTypeParam() throws Exception {
        performTest("GenericsMethodNoTypeParams", 856, "<", "empty.pass");
    }
    
    public void testBeforeFirstMethodTypeParam() throws Exception {
        performTest("GenericsMethod", 857, null, "empty.pass");
    }
    
    public void testEmptyFileTypingFirstMethodTypeParam() throws Exception {
        performTest("MethodStart", 849, "public <X", "empty.pass");
    }
    
    public void testTypingFirstMethodTypeParam() throws Exception {
        performTest("GenericsMethodNoTypeParams", 856, "<X", "empty.pass");
    }
    
    public void testOnFirstMethodTypeParam() throws Exception {
        performTest("GenericsMethod", 858, null, "empty.pass");
    }

    public void testEmptyFileAfterTypingFirstMethodTypeParamAndSpace() throws Exception {
        performTest("MethodStart", 849, "public <X ", "extendsKeyword.pass");
    }
    
    public void testAfterTypingFirstMethodTypeParamAndSpace() throws Exception {
        performTest("GenericsMethodNoTypeParams", 856, "<X ", "extendsKeyword.pass");
    }
    
    public void testAfterFirstMethodTypeParamAndSpace() throws Exception {
        performTest("GenericsMethod", 859, null, "extendsKeyword.pass");
    }

    public void testEmptyFileTypingExtendsInFirstMethodTypeParam() throws Exception {
        performTest("MethodStart", 849, "public <X e", "extendsKeyword.pass");
    }
    
    public void testTypingExtendsInFirstMethodTypeParam() throws Exception {
        performTest("GenericsMethodNoTypeParams", 856, "<X e", "extendsKeyword.pass");
    }
    
    public void testOnExtendsInFirstMethodTypeParam() throws Exception {
        performTest("GenericsMethod", 860, null, "extendsKeyword.pass");
    }

    public void testEmptyFileAfterTypingExtendsInFirstMethodTypeParam() throws Exception {
        performTest("MethodStart", 849, "public <X extends", "extendsKeyword.pass");
    }
    
    public void testAfterTypingExtendsInFirstMethodTypeParam() throws Exception {
        performTest("GenericsMethodNoTypeParams", 856, "<X extends", "extendsKeyword.pass");
    }
    
    public void testAfterExtendsInFirstMethodTypeParam() throws Exception {
        performTest("GenericsMethod", 866, null, "extendsKeyword.pass");
    }

    public void testEmptyFileAfterTypingExtendsAndSpaceInFirstMethodTypeParam() throws Exception {
        performTest("MethodStart", 849, "public <X extends ", "javaLangContentAndTestClass.pass");
    }
    
    public void testAfterTypingExtendsAndSpaceInFirstMethodTypeParam() throws Exception {
        performTest("GenericsMethodNoTypeParams", 856, "<X extends ", "javaLangContentAndTestClass.pass");
    }
    
    public void testAfterExtendsAndSpaceInFirstMethodTypeParam() throws Exception {
        performTest("GenericsMethod", 867, null, "javaLangContentAndTestClass.pass");
    }

    public void testEmptyFileAfterTypingBoundedFirstMethodTypeParamAndSpace() throws Exception {
        performTest("MethodStart", 849, "public <X extends Number ", "empty.pass");
    }
    
    public void testAfterTypingBoundedFirstMethodTypeParamAndSpace() throws Exception {
        performTest("GenericsMethodNoTypeParams", 856, "<X extends Number ", "empty.pass");
    }
    
    public void testAfterBoundedFirstMethodTypeParamAndSpace() throws Exception {
        performTest("Generics", 873, " ", "empty.pass");
    }
    
    public void testEmptyFileAfterTypingFirstMethodTypeParam() throws Exception {
        performTest("MethodStart", 849, "public <X extends Number,", "empty.pass");
    }
    
    public void testAfterTypingFirstMethodTypeParam() throws Exception {
        performTest("GenericsMethodNoTypeParams", 856, "<X extends Number,", "empty.pass");
    }
    
    public void testAfterFirstMethodTypeParam() throws Exception {
        performTest("GenericsMethod", 874, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingMethodTypeParams() throws Exception {
        performTest("MethodStart", 849, "public <X extends Number, Y extends RuntimeException>", "returnTypes.pass");
    }
    
    public void testAfterTypingMethodTypeParams() throws Exception {
        performTest("GenericsMethodNoTypeParams", 856, "<X extends Number, Y extends RuntimeException>", "returnTypes.pass");
    }
    
    public void testAfterMethodTypeParams() throws Exception {
        performTest("GenericsMethod", 902, null, "returnTypes.pass");
    }
}
