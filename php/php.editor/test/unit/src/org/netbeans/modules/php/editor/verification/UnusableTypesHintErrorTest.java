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

public class UnusableTypesHintErrorTest extends PHPHintsTestBase {

    public UnusableTypesHintErrorTest(String testName) {
        super(testName);
    }

    public void testFieldTypes_01() throws Exception {
        checkHints(new UnusableTypesHintError(), "testFieldTypes_01.php");
    }

    public void testArrowFunctionReturnTypes_01() throws Exception {
        checkHints(new UnusableTypesHintError(), "testArrowFunctionReturnTypes_01.php");
    }

    public void testUnionTypes_01() throws Exception {
        checkHints(new UnusableTypesHintError(), "testUnionTypes_01.php");
    }

    public void testUnionTypes_01_PHP81() throws Exception {
        checkHints(new UnusableTypesHintErrorStub(PhpVersion.PHP_81), "testUnionTypes_01.php");
    }

    public void testStaticReturnTypes_01() throws Exception {
        checkHints(new UnusableTypesHintError(), "testStaticReturnTypes_01.php");
    }

    public void testMixedTypes_01() throws Exception {
        checkHints(new UnusableTypesHintError(), "testMixedTypes_01.php");
    }

    public void testNeverTypes_01() throws Exception {
        checkHints(new UnusableTypesHintError(), "testNeverTypes_01.php");
    }

    public void testIntersectionTypes_01() throws Exception {
        checkHints(new UnusableTypesHintError(), "testIntersectionTypes_01.php");
    }

    public void testNullableTypes_01() throws Exception {
        checkHints(new UnusableTypesHintError(), "testNullableTypes_01.php");
    }

    public void testNullableTypes_01_PHP81() throws Exception {
        checkHints(new UnusableTypesHintErrorStub(PhpVersion.PHP_81), "testNullableTypes_01.php");
    }

    public void testDnfTypes_01() throws Exception {
        checkHints(new UnusableTypesHintError(), "testDnfTypes_01.php");
    }

    public void testDuplicateTypes_01() throws Exception {
        checkHints(new UnusableTypesHintError(), "testDuplicateTypes_01.php");
    }

    public void testConstantTypes_01() throws Exception {
        checkHints(new UnusableTypesHintError(), "testConstantTypes_01.php");
    }

    @Override
    protected String getTestDirectory() {
        return TEST_DIRECTORY + "UnusableTypesHintError/";
    }

    private static final class UnusableTypesHintErrorStub extends UnusableTypesHintError {

        private PhpVersion phpVersion;

        public UnusableTypesHintErrorStub(PhpVersion phpVersion) {
            this.phpVersion = phpVersion;
        }

        @Override
        protected PhpVersion getPhpVersion(FileObject fileObject) {
            return phpVersion;
        }
    }
}
