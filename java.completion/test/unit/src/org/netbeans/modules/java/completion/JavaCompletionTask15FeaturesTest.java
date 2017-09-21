/**
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
        performTest("GenericsStart", 32, "<", "empty.pass");
    }

    public void testBeforeTypingFirstTypeParam() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<", "empty.pass");
    }
    
    public void testBeforeFirstTypeParam() throws Exception {
        performTest("Generics", 33, null, "empty.pass");
    }
    
    public void testEmptyFileTypingFirstTypeParam() throws Exception {
        performTest("GenericsStart", 32, "<X", "empty.pass");
    }
    
    public void testTypingFirstTypeParam() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<X", "empty.pass");
    }
    
    public void testOnFirstTypeParam() throws Exception {
        performTest("Generics", 34, null, "empty.pass");
    }

    public void testEmptyFileAfterTypingFirstTypeParamAndSpace() throws Exception {
        performTest("GenericsStart", 32, "<X ", "extendsKeyword.pass");
    }
    
    public void testAfterTypingFirstTypeParamAndSpace() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<X ", "extendsKeyword.pass");
    }
    
    public void testAfterFirstTypeParamAndSpace() throws Exception {
        performTest("Generics", 35, null, "extendsKeyword.pass");
    }

    public void testEmptyFileTypingExtendsInFirstTypeParam() throws Exception {
        performTest("GenericsStart", 32, "<X e", "extendsKeyword.pass");
    }
    
    public void testTypingExtendsInFirstTypeParam() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<X e", "extendsKeyword.pass");
    }
    
    public void testOnExtendsInFirstTypeParam() throws Exception {
        performTest("Generics", 36, null, "extendsKeyword.pass");
    }

    public void testEmptyFileAfterTypingExtendsInFirstTypeParam() throws Exception {
        performTest("GenericsStart", 32, "<X extends", "extendsKeyword.pass");
    }
    
    public void testAfterTypingExtendsInFirstTypeParam() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<X extends", "extendsKeyword.pass");
    }
    
    public void testAfterExtendsInFirstTypeParam() throws Exception {
        performTest("Generics", 42, null, "extendsKeyword.pass");
    }

    public void testEmptyFileAfterTypingExtendsAndSpaceInFirstTypeParam() throws Exception {
        performTest("GenericsStart", 32, "<X extends ", "javaLangContentAndTestClass.pass");
    }
    
    public void testAfterTypingExtendsAndSpaceInFirstTypeParam() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<X extends ", "javaLangContentAndTestClass.pass");
    }
    
    public void testAfterExtendsAndSpaceInFirstTypeParam() throws Exception {
        performTest("Generics", 43, null, "javaLangContentAndTestClass.pass");
    }

    public void testEmptyFileAfterTypingBoundedFirstTypeParamAndSpace() throws Exception {
        performTest("GenericsStart", 32, "<X extends Number ", "empty.pass");
    }
    
    public void testAfterTypingBoundedFirstTypeParamAndSpace() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<X extends Number ", "empty.pass");
    }
    
    public void testAfterBoundedFirstTypeParamAndSpace() throws Exception {
        performTest("Generics", 49, " ", "empty.pass");
    }
    
    public void testEmptyFileAfterTypingFirstTypeParam() throws Exception {
        performTest("GenericsStart", 32, "<X extends Number,", "empty.pass");
    }
    
    public void testAfterTypingFirstTypeParam() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<X extends Number,", "empty.pass");
    }
    
    public void testAfterFirstTypeParam() throws Exception {
        performTest("Generics", 50, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingTypeParams() throws Exception {
        performTest("GenericsStart", 32, "<X extends Number, Y extends RuntimeException>", "extendsAndImplementsKeywords.pass");
    }
    
    public void testAfterTypingTypeParams() throws Exception {
        performTest("GenericsNoTypeParams", 32, "<X extends Number, Y extends RuntimeException>", "extendsAndImplementsKeywords.pass");
    }
    
    public void testAfterTypeParams() throws Exception {
        performTest("Generics", 78, null, "extendsAndImplementsKeywords.pass");
    }

    public void testEmptyFileBeforeTypingFirstMethodTypeParam() throws Exception {
        performTest("MethodStart", 40, "public <", "empty.pass");
    }

    public void testBeforeTypingFirstMethodTypeParam() throws Exception {
        performTest("GenericsMethodNoTypeParams", 47, "<", "empty.pass");
    }
    
    public void testBeforeFirstMethodTypeParam() throws Exception {
        performTest("GenericsMethod", 48, null, "empty.pass");
    }
    
    public void testEmptyFileTypingFirstMethodTypeParam() throws Exception {
        performTest("MethodStart", 40, "public <X", "empty.pass");
    }
    
    public void testTypingFirstMethodTypeParam() throws Exception {
        performTest("GenericsMethodNoTypeParams", 47, "<X", "empty.pass");
    }
    
    public void testOnFirstMethodTypeParam() throws Exception {
        performTest("GenericsMethod", 49, null, "empty.pass");
    }

    public void testEmptyFileAfterTypingFirstMethodTypeParamAndSpace() throws Exception {
        performTest("MethodStart", 40, "public <X ", "extendsKeyword.pass");
    }
    
    public void testAfterTypingFirstMethodTypeParamAndSpace() throws Exception {
        performTest("GenericsMethodNoTypeParams", 47, "<X ", "extendsKeyword.pass");
    }
    
    public void testAfterFirstMethodTypeParamAndSpace() throws Exception {
        performTest("GenericsMethod", 50, null, "extendsKeyword.pass");
    }

    public void testEmptyFileTypingExtendsInFirstMethodTypeParam() throws Exception {
        performTest("MethodStart", 40, "public <X e", "extendsKeyword.pass");
    }
    
    public void testTypingExtendsInFirstMethodTypeParam() throws Exception {
        performTest("GenericsMethodNoTypeParams", 47, "<X e", "extendsKeyword.pass");
    }
    
    public void testOnExtendsInFirstMethodTypeParam() throws Exception {
        performTest("GenericsMethod", 51, null, "extendsKeyword.pass");
    }

    public void testEmptyFileAfterTypingExtendsInFirstMethodTypeParam() throws Exception {
        performTest("MethodStart", 40, "public <X extends", "extendsKeyword.pass");
    }
    
    public void testAfterTypingExtendsInFirstMethodTypeParam() throws Exception {
        performTest("GenericsMethodNoTypeParams", 47, "<X extends", "extendsKeyword.pass");
    }
    
    public void testAfterExtendsInFirstMethodTypeParam() throws Exception {
        performTest("GenericsMethod", 57, null, "extendsKeyword.pass");
    }

    public void testEmptyFileAfterTypingExtendsAndSpaceInFirstMethodTypeParam() throws Exception {
        performTest("MethodStart", 40, "public <X extends ", "javaLangContentAndTestClass.pass");
    }
    
    public void testAfterTypingExtendsAndSpaceInFirstMethodTypeParam() throws Exception {
        performTest("GenericsMethodNoTypeParams", 47, "<X extends ", "javaLangContentAndTestClass.pass");
    }
    
    public void testAfterExtendsAndSpaceInFirstMethodTypeParam() throws Exception {
        performTest("GenericsMethod", 58, null, "javaLangContentAndTestClass.pass");
    }

    public void testEmptyFileAfterTypingBoundedFirstMethodTypeParamAndSpace() throws Exception {
        performTest("MethodStart", 40, "public <X extends Number ", "empty.pass");
    }
    
    public void testAfterTypingBoundedFirstMethodTypeParamAndSpace() throws Exception {
        performTest("GenericsMethodNoTypeParams", 47, "<X extends Number ", "empty.pass");
    }
    
    public void testAfterBoundedFirstMethodTypeParamAndSpace() throws Exception {
        performTest("Generics", 64, " ", "empty.pass");
    }
    
    public void testEmptyFileAfterTypingFirstMethodTypeParam() throws Exception {
        performTest("MethodStart", 40, "public <X extends Number,", "empty.pass");
    }
    
    public void testAfterTypingFirstMethodTypeParam() throws Exception {
        performTest("GenericsMethodNoTypeParams", 47, "<X extends Number,", "empty.pass");
    }
    
    public void testAfterFirstMethodTypeParam() throws Exception {
        performTest("GenericsMethod", 65, null, "empty.pass");
    }
    
    public void testEmptyFileAfterTypingMethodTypeParams() throws Exception {
        performTest("MethodStart", 40, "public <X extends Number, Y extends RuntimeException>", "returnTypes.pass");
    }
    
    public void testAfterTypingMethodTypeParams() throws Exception {
        performTest("GenericsMethodNoTypeParams", 47, "<X extends Number, Y extends RuntimeException>", "returnTypes.pass");
    }
    
    public void testAfterMethodTypeParams() throws Exception {
        performTest("GenericsMethod", 93, null, "returnTypes.pass");
    }
}
