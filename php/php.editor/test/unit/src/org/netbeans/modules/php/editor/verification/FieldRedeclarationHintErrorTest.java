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
package org.netbeans.modules.php.editor.verification;


public class FieldRedeclarationHintErrorTest extends PHPHintsTestBase {

    public FieldRedeclarationHintErrorTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestDirectory() {
        return TEST_DIRECTORY + "FieldRedeclarationHintError/";
    }

    public void testFieldRedeclarationHint() throws Exception {
        checkHints(new FieldRedeclarationHintError(), "testFieldRedeclarationHint.php");
    }

    public void testIssue268557() throws Exception {
        checkHints(new FieldRedeclarationHintError(), "testIssue268557.php");
    }

    public void testFieldRedeclarationTypedProperties20Hint_01() throws Exception {
        // PHP 7.4
        checkHints(new FieldRedeclarationHintError(), "testFieldRedeclarationTypedProperties20Hint_01.php");
    }

    public void testFieldRedeclarationTypedProperties20Hint_02() throws Exception {
        // PHP 7.4
        checkHints(new FieldRedeclarationHintError(), "testFieldRedeclarationTypedProperties20Hint_02.php");
    }

    // [NETBEANS-4443] PHP 8.0
    public void testConstructorPropertyPromotion_01() throws Exception {
        checkHints(new FieldRedeclarationHintError(), "testConstructorPropertyPromotion.php");
    }

}
