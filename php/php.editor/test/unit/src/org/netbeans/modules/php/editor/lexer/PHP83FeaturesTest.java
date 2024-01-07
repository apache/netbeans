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

public class PHP83FeaturesTest extends PHPLexerTestBase {

    public PHP83FeaturesTest(String testName) {
        super(testName);
    }

    public void testTypedClassConstantsContextSensitiveLexerClass() throws Exception {
        performTest("lexer/php83/context_sensitive_lexer_typed_const_class");
    }

    public void testTypedClassConstantsContextSensitiveLexerInterface() throws Exception {
        performTest("lexer/php83/context_sensitive_lexer_typed_const_interface");
    }

    public void testTypedClassConstantsContextSensitiveLexerTrait() throws Exception {
        performTest("lexer/php83/context_sensitive_lexer_typed_const_trait");
    }

    public void testTypedClassConstantsContextSensitiveLexerEnum() throws Exception {
        performTest("lexer/php83/context_sensitive_lexer_typed_const_enum");
    }
}
