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
package org.netbeans.modules.php.editor.verification;

import org.netbeans.modules.php.api.PhpVersion;

public class IncorrectDeprecatedAttributeHintErrorTest extends PHPHintsTestBase {

    public IncorrectDeprecatedAttributeHintErrorTest(String testName) {
        super(testName);
    }

    private void checkHints(String fileName, PhpVersion phpVersion) throws Exception {
        checkHints(new IncorrectDeprecatedAttributeHintErrorStub(phpVersion), fileName);
    }

    @Override
    protected String getTestDirectory() {
        return TEST_DIRECTORY + "IncorrectDeprecatedAttributeHintError/";
    }

    public void testFieldsInGlobal_PHP83() throws Exception {
        checkHints("testFieldsInGlobal.php", PhpVersion.PHP_83);
    }

    public void testFieldsInGlobal_PHP84() throws Exception {
        checkHints("testFieldsInGlobal.php", PhpVersion.PHP_84);
    }

    public void testFieldsInNamespace_PHP83() throws Exception {
        checkHints("testFieldsInNamespace.php", PhpVersion.PHP_83);
    }

    public void testFieldsInNamespace_PHP84() throws Exception {
        checkHints("testFieldsInNamespace.php", PhpVersion.PHP_84);
    }

    public void testFieldsInNamespaceWithUse_PHP83() throws Exception {
        checkHints("testFieldsInNamespaceWithUse.php", PhpVersion.PHP_83);
    }

    public void testFieldsInNamespaceWithUse_PHP84() throws Exception {
        checkHints("testFieldsInNamespaceWithUse.php", PhpVersion.PHP_84);
    }

    public void testTypesInGlobal_PHP83() throws Exception {
        checkHints("testTypesInGlobal.php", PhpVersion.PHP_83);
    }

    public void testTypesInGlobal_PHP84() throws Exception {
        checkHints("testTypesInGlobal.php", PhpVersion.PHP_84);
    }

    public void testTypesInNamespace_PHP83() throws Exception {
        checkHints("testTypesInNamespace.php", PhpVersion.PHP_83);
    }

    public void testTypesInNamespace_PHP84() throws Exception {
        checkHints("testTypesInNamespace.php", PhpVersion.PHP_84);
    }

    public void testTypesInNamespaceWithUse_PHP83() throws Exception {
        checkHints("testTypesInNamespaceWithUse.php", PhpVersion.PHP_83);
    }

    public void testTypesInNamespaceWithUse_PHP84() throws Exception {
        checkHints("testTypesInNamespaceWithUse.php", PhpVersion.PHP_84);
    }

    private static class IncorrectDeprecatedAttributeHintErrorStub extends IncorrectDeprecatedAttributeHintError {

        private final PhpVersion PhpVersion;

        public IncorrectDeprecatedAttributeHintErrorStub(PhpVersion PhpVersion) {
            this.PhpVersion = PhpVersion;
        }

        @Override
        protected PhpVersion getPhpVersion() {
            return PhpVersion;
        }

    }

}
