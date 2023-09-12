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


public class IncorrectNamedArgumentsHintErrorTest extends PHPHintsTestBase {

    public IncorrectNamedArgumentsHintErrorTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestDirectory() {
        return TEST_DIRECTORY + "IncorrectNamedArgumentsHintError/";
    }

    public void testDuplicateNames() throws Exception {
        checkHints(new IncorrectNamedArgumentsHintError(), "testDuplicateNames.php");
    }

    public void testDuplicateNamesInAttributeArgList() throws Exception {
        checkHints(new IncorrectNamedArgumentsHintError(), "testDuplicateNamesInAttributeArgList.php");
    }

    public void testPositionalAfterNamed() throws Exception {
        checkHints(new IncorrectNamedArgumentsHintError(), "testPositionalAfterNamed.php");
    }

    public void testPositionalAfterNamedInAttributeArgList() throws Exception {
        checkHints(new IncorrectNamedArgumentsHintError(), "testPositionalAfterNamedInAttributeArgList.php");
    }

    public void testUnpackingAfterNamed() throws Exception {
        checkHints(new IncorrectNamedArgumentsHintError(), "testUnpackingAfterNamed.php");
    }

}
