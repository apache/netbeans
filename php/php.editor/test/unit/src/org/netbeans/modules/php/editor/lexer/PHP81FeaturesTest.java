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

public class PHP81FeaturesTest extends PHPLexerTestBase {

    public PHP81FeaturesTest(String testName) {
        super(testName);
    }

    public void testNeverType_01() throws Exception {
        performTest("lexer/php81/neverType_01");
    }

    public void testReadonlyProperties_01() throws Exception {
        performTest("lexer/php81/readonlyProperties_01");
    }

    public void testEnumerations_01() throws Exception {
        performTest("lexer/php81/enumerations_01");
    }

    public void testEnumerations_02() throws Exception {
        performTest("lexer/php81/enumerations_02");
    }

    public void testExplicitOctalIntegerLiteralNotation_01() throws Exception {
        performTest("lexer/php81/explicitOctalIntegerLiteralNotation_01");
    }

    public void testEnumAsTypeName() throws Exception {
        performTest("lexer/php81/enumAsTypeName");
    }

}
