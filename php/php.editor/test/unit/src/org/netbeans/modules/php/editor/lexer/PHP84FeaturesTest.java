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
package org.netbeans.modules.php.editor.lexer;

public class PHP84FeaturesTest extends PHPLexerTestBase {

    public PHP84FeaturesTest(String testName) {
        super(testName);
    }

    public void testAsymmetricVisibilityClass() throws Exception {
        performTest("lexer/php84/asymmetricVisibilityClass");
    }

    public void testAsymmetricVisibilityTrait() throws Exception {
        performTest("lexer/php84/asymmetricVisibilityTrait");
    }

    public void testAsymmetricVisibilityAnonClass() throws Exception {
        performTest("lexer/php84/asymmetricVisibilityAnonClass");
    }

    public void testAsymmetricVisibilityConstructorPropertyPromotion() throws Exception {
        performTest("lexer/php84/asymmetricVisibilityConstructorPropertyPromotion");
    }

    public void testFinalFieldsClass() throws Exception {
        performTest("lexer/php84/finalFieldsClass");
    }

    public void testFinalFieldsTrait() throws Exception {
        performTest("lexer/php84/finalFieldsTrait");
    }

    public void testFinalFieldsAnonClass() throws Exception {
        performTest("lexer/php84/finalFieldsAnonClass");
    }

    public void testPropertyHooks() throws Exception {
        performTest("lexer/php84/propertyHooks");
    }
}
