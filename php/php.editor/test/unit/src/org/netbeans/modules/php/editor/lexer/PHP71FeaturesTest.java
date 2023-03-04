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
package org.netbeans.modules.php.editor.lexer;

public class PHP71FeaturesTest extends PHPLexerTestBase {

    public PHP71FeaturesTest(String testName) {
        super(testName);
    }

    public void testVoidReturnType01() throws Exception {
        performTest("lexer/void_return_type_01");
    }

    public void testVoidReturnType02() throws Exception {
        performTest("lexer/void_return_type_02");
    }

    public void testVoidReturnType03() throws Exception {
        performTest("lexer/void_return_type_03");
    }

    public void testIterable01() throws Exception {
        performTest("lexer/iterable_01");
    }

    // #262141
    public void testContextSensitiveLexerWithConstVisibility01() throws Exception {
        performTest("lexer/context_sensitive_lexer_with_const_visibility_01");
    }

}
