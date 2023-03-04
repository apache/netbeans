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

public class FinalModifierHintErrorTest extends PHPHintsTestBase {

    public FinalModifierHintErrorTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestDirectory() {
        return TEST_DIRECTORY + "FinalModifierHintError/";
    }

    public void testFinalPrivateConstants() throws Exception {
        checkHints(new FinalModifierHintError(), "testFinalPrivateConstants.php");
    }

    public void testFinalPrivateConstantsWithAttributes() throws Exception {
        checkHints(new FinalModifierHintError(), "testFinalPrivateConstantsWithAttributes.php");
    }

    public void testOverridingFinalConstants() throws Exception {
        checkHints(new FinalModifierHintError(), "testOverridingFinalConstants.php");
    }

    // Fix
    public void testFinalPrivateConstantsFix_01() throws Exception {
        applyHint(new FinalModifierHintError(), "testFinalPrivateConstants.php", "fin^al private const PRIVATE_CONST = \"foo\";", "Remove \"final\"");
    }

    public void testFinalPrivateConstantsFix_02() throws Exception {
        applyHint(new FinalModifierHintError(), "testFinalPrivateConstants.php", "private fin^al const PRIVATE_CONST2 = \"foo\";", "Remove \"final\"");
    }

    public void testFinalPrivateConstantsFix_03() throws Exception {
        applyHint(new FinalModifierHintError(), "testFinalPrivateConstants.php", "fin^al private const PRIVATE_CONST3 = \"foo\", PRIVATE_CONST4 = \"foo\";", "Remove \"final\"");
    }

    public void testFinalPrivateConstantsFix_04() throws Exception {
        applyHint(new FinalModifierHintError(), "testFinalPrivateConstants.php", "private fi^nal const PRIVATE_CONST5 = \"foo\", PRIVATE_CONST6 = \"foo\";", "Remove \"final\"");
    }

    public void testFinalPrivateConstantsWithAttributesFix_01() throws Exception {
        applyHint(new FinalModifierHintError(), "testFinalPrivateConstantsWithAttributes.php", "fin^al private const PRIVATE_CONST = \"foo\";", "Remove \"final\"");
    }

    public void testFinalPrivateConstantsWithAttributesFix_02() throws Exception {
        applyHint(new FinalModifierHintError(), "testFinalPrivateConstantsWithAttributes.php", "private fin^al const PRIVATE_CONST2 = \"foo\";", "Remove \"final\"");
    }

    public void testFinalPrivateConstantsWithAttributesFix_03() throws Exception {
        applyHint(new FinalModifierHintError(), "testFinalPrivateConstantsWithAttributes.php", "fin^al private const PRIVATE_CONST3 = \"foo\", PRIVATE_CONST4 = \"foo\";", "Remove \"final\"");
    }

    public void testFinalPrivateConstantsWithAttributesFix_04() throws Exception {
        applyHint(new FinalModifierHintError(), "testFinalPrivateConstantsWithAttributes.php", "private fi^nal const PRIVATE_CONST5 = \"foo\", PRIVATE_CONST6 = \"foo\";", "Remove \"final\"");
    }
}
