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
import org.openide.filesystems.FileObject;

public class ReturnTypeHintErrorTest extends PHPHintsTestBase {

    public ReturnTypeHintErrorTest(String testName) {
        super(testName);
    }

    public void testVoidReturnTypeHint_01() throws Exception {
        checkHints(new ReturnTypeHintErrorStub(PhpVersion.PHP_70), "testVoidReturnTypeHint.php");
    }

    public void testVoidReturnTypeHint_02() throws Exception {
        checkHints(new ReturnTypeHintErrorStub(PhpVersion.PHP_71), "testVoidReturnTypeHint.php");
    }

    public void testNeverReturnTypeHint_01() throws Exception {
        checkHints(new ReturnTypeHintErrorStub(PhpVersion.PHP_80), "testNeverReturnTypeHint.php");
    }

    public void testNeverReturnTypeHint_02() throws Exception {
        checkHints(new ReturnTypeHintErrorStub(PhpVersion.PHP_81), "testNeverReturnTypeHint.php");
    }

    public void testReturnStatementWithoutValueHint_01() throws Exception {
        checkHints(new ReturnTypeHintErrorStub(PhpVersion.PHP_56), "testReturnStatementWithoutValueHintError.php");
    }

    public void testReturnStatementWithoutValueHint_02() throws Exception {
        checkHints(new ReturnTypeHintErrorStub(PhpVersion.PHP_70), "testReturnStatementWithoutValueHintError.php");
    }

    public void testReturnStatementWithoutValueHint_03() throws Exception {
        checkHints(new ReturnTypeHintErrorStub(PhpVersion.PHP_71), "testReturnStatementWithoutValueHintError.php");
    } 

    public void testReturnStatementWithoutValueHint_04() throws Exception {
        checkHints(new ReturnTypeHintErrorStub(PhpVersion.PHP_81), "testReturnStatementWithoutValueHintError.php");
    }

    @Override
    protected String getTestDirectory() {
        return TEST_DIRECTORY + "ReturnTypeHintError/";
    }

    private static final class ReturnTypeHintErrorStub extends ReturnTypeHintError {

        private final PhpVersion phpVersion;

        public ReturnTypeHintErrorStub(PhpVersion phpVersion) {
            this.phpVersion = phpVersion;
        }

        @Override
        protected PhpVersion getPhpVersion(FileObject file) {
            return phpVersion;
        }

    }

}
