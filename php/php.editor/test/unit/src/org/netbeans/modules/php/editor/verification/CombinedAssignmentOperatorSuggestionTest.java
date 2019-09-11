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

public class CombinedAssignmentOperatorSuggestionTest extends PHPHintsTestBase {

    public CombinedAssignmentOperatorSuggestionTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestDirectory() {
        return TEST_DIRECTORY + "CombinedAssignmentOperatorSuggestion/";
    }

    public void testNullCoalesce_01() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(), "testNullCoalesce.php", "$test = $tes^t ?? \"test\";");
    }

    public void testNullCoalesce_01_php73() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(PhpVersion.PHP_73), "testNullCoalesce.php", "$test = $tes^t ?? \"test\";");
    }

    public void testNullCoalesce_02() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(), "testNullCoalesce.php", "$test = $test ?? getDefau^ltValue();");
    }

    public void testNullCoalesce_02_php72() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(PhpVersion.PHP_72), "testNullCoalesce.php", "$test = $test ?? getDefau^ltValue();");
    }

    public void testNullCoalesce_03() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(), "testNullCoalesce.php", "$object->request['test']['id'] = $object->request['test']['id'] ??^ \"test\";");
    }

    public void testNullCoalesce_03_php71() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(PhpVersion.PHP_71), "testNullCoalesce.php", "$object->request['test']['id'] = $object->request['test']['id'] ??^ \"test\";");
    }

    public void testPlus_01() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(), "testPlus.php", "$x = $x + ^10;");
    }

    public void testPlus_01_php73() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(PhpVersion.PHP_73), "testPlus.php", "$x = $x + ^10;");
    }

    public void testPlus_02() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(), "testPlus.php", "$x = ^$x + $y;");
    }

    public void testPlus_03() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(), "testPlus.php", "$x = $x^ + (int)$y;");
    }

    public void testPlus_04() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(), "testPlus.php", "$x = $x + getNumbe^r();");
    }

    public void testPlus_05() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(), "testPlus.php", "$x^ = $x + $object->request['messages']['id'];");
    }

    public void testMinus_01() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(), "testMinus.php", "$x = $x -^ 10;");
    }

    public void testMul_01() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(), "testMul.php", "$x = $x * ^10;");
    }

    public void testDiv_01() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(), "testDiv.php", "$x = ^$x / 10;");
    }

    public void testConcat_01() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(), "testConcat.php", "$x = $x . 10^;");
    }

    public void testMod_01() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(), "testMod.php", "$x = $x % 1^0;");
    }

    public void testSl_01() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(), "testSl.php", "$x = $x << 1^0;");
    }

    public void testSr_01() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(), "testSr.php", "$x^ = $x >> 10;");
    }

    public void testAnd_01() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(), "testAnd.php", "$x = $x & ^10;");
    }

    public void testOr_01() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(), "testOr.php", "$x = ^$x | 10;");
    }

    public void testXor_01() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(), "testXor.php", "$x = $x ^^ 10;");
    }

    public void testPow_01() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(), "testPow.php", "$x = $x ** 10;^");
    }

    public void testNoHints_01() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(), "testNoHints.php", "$x = $x + $y +^ 100;");
    }

    public void testNoHints_02() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(), "testNoHints.php", "$x = $x + $y * 100^;");
    }

    public void testNoHints_03() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(), "testNoHints.php", "$x = ^($x + $y) * 100;");
    }

    public void testNoHints_04() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(), "testNoHints.php", "$x = $x / ($y ^* 100);");
    }

    public void testNoHints_05() throws Exception {
        checkHints(new CombinedAssignmentOperatorSuggestionStub(), "testNoHints.php", "$x^ = $x + getNumber() * 100;");
    }

    // Fixes
    public void testNullCoalesceFix_01() throws Exception {
        applyHint(new CombinedAssignmentOperatorSuggestionStub(), "testNullCoalesce.php", "$test = $tes^t ?? \"test\";", "Use Combined Assignment Operator");
    }

    public void testNullCoalesceFix_02() throws Exception {
        applyHint(new CombinedAssignmentOperatorSuggestionStub(), "testNullCoalesce.php", "$test = $test ?? getDefau^ltValue();", "Use Combined Assignment Operator");
    }

    public void testNullCoalesceFix_03() throws Exception {
        applyHint(new CombinedAssignmentOperatorSuggestionStub(), "testNullCoalesce.php", "$object->request['test']['id'] = $object->request['test']['id'] ??^ \"test\";", "Use Combined Assignment Operator");
    }

    public void testPlusFix_01() throws Exception {
        applyHint(new CombinedAssignmentOperatorSuggestionStub(), "testPlus.php", "$x = $x + ^10;", "Use Combined Assignment Operator");
    }

    public void testPlusFix_02() throws Exception {
        applyHint(new CombinedAssignmentOperatorSuggestionStub(), "testPlus.php", "$x = ^$x + $y;", "Use Combined Assignment Operator");
    }

    public void testPlusFix_03() throws Exception {
        applyHint(new CombinedAssignmentOperatorSuggestionStub(), "testPlus.php", "$x = $x^ + (int)$y;", "Use Combined Assignment Operator");
    }

    public void testPlusFix_04() throws Exception {
        applyHint(new CombinedAssignmentOperatorSuggestionStub(), "testPlus.php", "$x = $x + getNumbe^r();", "Use Combined Assignment Operator");
    }

    public void testPlusFix_05() throws Exception {
        applyHint(new CombinedAssignmentOperatorSuggestionStub(), "testPlus.php", "$x^ = $x + $object->request['messages']['id'];", "Use Combined Assignment Operator");
    }

    public void testMinusFix_01() throws Exception {
        applyHint(new CombinedAssignmentOperatorSuggestionStub(), "testMinus.php", "$x = $x -^ 10;", "Use Combined Assignment Operator");
    }

    public void testMulFix_01() throws Exception {
        applyHint(new CombinedAssignmentOperatorSuggestionStub(), "testMul.php", "$x = $x * ^10;", "Use Combined Assignment Operator");
    }

    public void testDivFix_01() throws Exception {
        applyHint(new CombinedAssignmentOperatorSuggestionStub(), "testDiv.php", "$x = ^$x / 10;", "Use Combined Assignment Operator");
    }

    public void testConcatFix_01() throws Exception {
        applyHint(new CombinedAssignmentOperatorSuggestionStub(), "testConcat.php", "$x = $x . 10^;", "Use Combined Assignment Operator");
    }

    public void testModFix_01() throws Exception {
        applyHint(new CombinedAssignmentOperatorSuggestionStub(), "testMod.php", "$x = $x % 1^0;", "Use Combined Assignment Operator");
    }

    public void testSlFix_01() throws Exception {
        applyHint(new CombinedAssignmentOperatorSuggestionStub(), "testSl.php", "$x = $x << 1^0;", "Use Combined Assignment Operator");
    }

    public void testSrFix_01() throws Exception {
        applyHint(new CombinedAssignmentOperatorSuggestionStub(), "testSr.php", "$x^ = $x >> 10;", "Use Combined Assignment Operator");
    }

    public void testAndFix_01() throws Exception {
        applyHint(new CombinedAssignmentOperatorSuggestionStub(), "testAnd.php", "$x = $x & ^10;", "Use Combined Assignment Operator");
    }

    public void testOrFix_01() throws Exception {
        applyHint(new CombinedAssignmentOperatorSuggestionStub(), "testOr.php", "$x = ^$x | 10;", "Use Combined Assignment Operator");
    }

    public void testXorFix_01() throws Exception {
        applyHint(new CombinedAssignmentOperatorSuggestionStub(), "testXor.php", "$x = $x ^^ 10;", "Use Combined Assignment Operator");
    }

    public void testPowFix_01() throws Exception {
        applyHint(new CombinedAssignmentOperatorSuggestionStub(), "testPow.php", "$x = $x ** 10;^", "Use Combined Assignment Operator");
    }

    private static final class CombinedAssignmentOperatorSuggestionStub extends CombinedAssignmentOperatorSuggestion {

        private final PhpVersion phpVersion;

        public CombinedAssignmentOperatorSuggestionStub() {
            this.phpVersion = PhpVersion.getDefault();
        }

        public CombinedAssignmentOperatorSuggestionStub(PhpVersion phpVersion) {
            this.phpVersion = phpVersion;
        }

        @Override
        protected PhpVersion getPhpVersion(FileObject fileObject) {
            return phpVersion;
        }
    }
}
