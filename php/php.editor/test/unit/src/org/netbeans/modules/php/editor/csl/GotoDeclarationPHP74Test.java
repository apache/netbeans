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
package org.netbeans.modules.php.editor.csl;

public class GotoDeclarationPHP74Test extends GotoDeclarationTestBase {

    public GotoDeclarationPHP74Test(String testName) {
        super(testName);
    }

    // class
    public void testTypedProperties20Class_01() throws Exception {
        checkDeclaration(getTestPath(), "use Bar\\My^Class;", "class ^MyClass {");
    }

    public void testTypedProperties20Class_02() throws Exception {
        checkDeclaration(getTestPath(), "    public My^Class $myClass;", "class ^MyClass {");
    }

    public void testTypedProperties20Class_03() throws Exception {
        checkDeclaration(getTestPath(), "    public ?^MyClass $myClass2;", "class ^MyClass {");
    }

    public void testTypedProperties20Class_04() throws Exception {
        checkDeclaration(getTestPath(), "    public \\Bar\\MyCl^ass $myClass3;", "class ^MyClass {");
    }

    public void testTypedProperties20Class_05() throws Exception {
        checkDeclaration(getTestPath(), "    public ?\\Bar\\My^Class $myClass4;", "class ^MyClass {");
    }

    public void testTypedProperties20Class_06() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass->pub^licTestMethod();", "    public function ^publicTestMethod(): void {");
    }

    public void testTypedProperties20Class_07() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass2->publicTest^Method();", "    public function ^publicTestMethod(): void {");
    }

    public void testTypedProperties20Class_08() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass3->p^ublicTestMethod();", "    public function ^publicTestMethod(): void {");
    }

    public void testTypedProperties20Class_09() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass4->publicTestMe^thod();", "    public function ^publicTestMethod(): void {");
    }

    public void testTypedProperties20Class_10() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass::publicS^taticTestMethod();", "    public static function ^publicStaticTestMethod(): void {");
    }

    public void testTypedProperties20Class_11() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass2::publ^icStaticTestMethod();", "    public static function ^publicStaticTestMethod(): void {");
    }

    public void testTypedProperties20Class_12() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass3::publicSt^aticTestMethod();", "    public static function ^publicStaticTestMethod(): void {");
    }

    public void testTypedProperties20Class_13() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass4::^publicStaticTestMethod();", "    public static function ^publicStaticTestMethod(): void {");
    }

    // trait
    public void testTypedProperties20Trait_01() throws Exception {
        checkDeclaration(getTestPath(), "use Bar\\My^Class;", "class ^MyClass {");
    }

    public void testTypedProperties20Trait_02() throws Exception {
        checkDeclaration(getTestPath(), "    public My^Class $myClass;", "class ^MyClass {");
    }

    public void testTypedProperties20Trait_03() throws Exception {
        checkDeclaration(getTestPath(), "    public ?^MyClass $myClass2;", "class ^MyClass {");
    }

    public void testTypedProperties20Trait_04() throws Exception {
        checkDeclaration(getTestPath(), "    public \\Bar\\MyCl^ass $myClass3;", "class ^MyClass {");
    }

    public void testTypedProperties20Trait_05() throws Exception {
        checkDeclaration(getTestPath(), "    public ?\\Bar\\My^Class $myClass4;", "class ^MyClass {");
    }

    public void testTypedProperties20Trait_06() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass->pub^licTestMethod();", "    public function ^publicTestMethod(): void {");
    }

    public void testTypedProperties20Trait_07() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass2->publicTest^Method();", "    public function ^publicTestMethod(): void {");
    }

    public void testTypedProperties20Trait_08() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass3->p^ublicTestMethod();", "    public function ^publicTestMethod(): void {");
    }

    public void testTypedProperties20Trait_09() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass4->publicTestMe^thod();", "    public function ^publicTestMethod(): void {");
    }

    public void testTypedProperties20Trait_10() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass::publicS^taticTestMethod();", "    public static function ^publicStaticTestMethod(): void {");
    }

    public void testTypedProperties20Trait_11() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass2::publ^icStaticTestMethod();", "    public static function ^publicStaticTestMethod(): void {");
    }

    public void testTypedProperties20Trait_12() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass3::publicSt^aticTestMethod();", "    public static function ^publicStaticTestMethod(): void {");
    }

    public void testTypedProperties20Trait_13() throws Exception {
        checkDeclaration(getTestPath(), "        $this->myClass4::^publicStaticTestMethod();", "    public static function ^publicStaticTestMethod(): void {");
    }

    public void testSpreadOperatorInArrayExpression_01() throws Exception {
        checkDeclaration(getTestPath(), "$array2 = [...$a^rray1];", "$^array1 = [1, 2, 3];");
    }

    public void testSpreadOperatorInArrayExpression_02() throws Exception {
        checkDeclaration(getTestPath(), "$array3 = [0, ...$arr^ay1];", "$^array1 = [1, 2, 3];");
    }

    public void testSpreadOperatorInArrayExpression_03a() throws Exception {
        checkDeclaration(getTestPath(), "$array4 = array(...$ar^ray1, ...$array2, 111);", "$^array1 = [1, 2, 3];");
    }

    public void testSpreadOperatorInArrayExpression_03b() throws Exception {
        checkDeclaration(getTestPath(), "$array4 = array(...$array1, ...$ar^ray2, 111);", "$^array2 = [...$array1];");
    }

    public void testSpreadOperatorInArrayExpression_04a() throws Exception {
        checkDeclaration(getTestPath(), "$array5 = [...$^array1, ...$array1];", "$^array1 = [1, 2, 3];");
    }

    public void testSpreadOperatorInArrayExpression_04b() throws Exception {
        checkDeclaration(getTestPath(), "$array5 = [...$array1, ...$ar^ray1];", "$^array1 = [1, 2, 3];");
    }

    public void testSpreadOperatorInArrayExpression_05() throws Exception {
        checkDeclaration(getTestPath(), "$array6 = [...getArr^ay()];", "function ^getArray() {");
    }

    public void testSpreadOperatorInArrayExpression_06() throws Exception {
        checkDeclaration(getTestPath(), "$array8 = [...arrayGenerat^or()];", "function ^arrayGenerator() {");
    }

    // const
    public void testSpreadOperatorInArrayExpression_07() throws Exception {
        checkDeclaration(getTestPath(), "const CONSTANT1 = [...CO^NSTANT];", "const ^CONSTANT = [0, 1, 2, 3];");
    }

    public void testSpreadOperatorInArrayExpression_08() throws Exception {
        checkDeclaration(getTestPath(), "const CONSTANT2 = [100, ...CONST^ANT, ...CONSTANT1,];", "const ^CONSTANT = [0, 1, 2, 3];");
    }

    public void testSpreadOperatorInArrayExpression_09() throws Exception {
        checkDeclaration(getTestPath(), "const CONSTANT3 = [...CONSTANT2, 100 => 0, ...CONSTA^NT];", "const ^CONSTANT = [0, 1, 2, 3];");
    }

    public void testSpreadOperatorInArrayExpression_10() throws Exception {
        checkDeclaration(getTestPath(), "const CONSTANT2 = [100, ...CONSTANT, ...CON^STANT1,];", "const ^CONSTANT1 = [...CONSTANT];");
    }

    public void testSpreadOperatorInArrayExpression_11() throws Exception {
        checkDeclaration(getTestPath(), "const CONSTANT3 = [...^CONSTANT2, 100 => 0, ...CONSTANT];", "const ^CONSTANT2 = [100, ...CONSTANT, ...CONSTANT1,];");
    }

    public void testSpreadOperatorInArrayExpression_12() throws Exception {
        checkDeclaration(getTestPath(), "const CONSTANT4 = [...CONST^ANT2, 100 => 0, ...\\Bar\\BAR_CONSTANT];", "const ^CONSTANT2 = [100, ...CONSTANT, ...CONSTANT1,];");
    }

    public void testSpreadOperatorInArrayExpression_13() throws Exception {
        checkDeclaration(getTestPath(), "const CONSTANT5 = [...CONSTANT2^, 100 => 0, ...\\Bar\\Bar::BAR_CONSTANT];", "const ^CONSTANT2 = [100, ...CONSTANT, ...CONSTANT1,];");
    }

    public void testSpreadOperatorInArrayExpression_14() throws Exception {
        checkDeclaration(getTestPath(), "const CONSTANT4 = [...CONSTANT2, 100 => 0, ...\\Bar\\BAR_CONS^TANT];", "const ^BAR_CONSTANT = [];");
    }

    public void testSpreadOperatorInArrayExpression_15() throws Exception {
        checkDeclaration(getTestPath(), "const CONSTANT5 = [...CONSTANT2, 100 => 0, ...\\Bar\\Bar::BAR_CON^STANT];", "    public const ^BAR_CONSTANT = \"test\";");
    }

    public void testSpreadOperatorInClassConst_01() throws Exception {
        checkDeclaration(getTestPath(), "    public const CONSTANT1 = self::^CONSTANT;", "    public const ^CONSTANT = [\"1\", \"2\", \"3\"];");
    }

    public void testSpreadOperatorInClassConst_02() throws Exception {
        checkDeclaration(getTestPath(), "    public const CONSTANT2 = [...self::CONS^TANT];", "    public const ^CONSTANT = [\"1\", \"2\", \"3\"];");
    }

    public void testSpreadOperatorInClassConst_03() throws Exception {
        checkDeclaration(getTestPath(), "    public const CONSTANT3 = [...self::CONSTANT^, \"4\"];", "    public const ^CONSTANT = [\"1\", \"2\", \"3\"];");
    }

    public void testSpreadOperatorInClassConst_04() throws Exception {
        checkDeclaration(getTestPath(), "    public const CONSTANT4 = [\"0\", ...self::C^ONSTANT, \"4\"];", "    public const ^CONSTANT = [\"1\", \"2\", \"3\"];");
    }

    public void testSpreadOperatorInClassConst_05() throws Exception {
        checkDeclaration(getTestPath(), "    public const CONSTANT5 = [\"0\", ...self::CONSTA^NT, \"4\", self::CONSTANT1];", "    public const ^CONSTANT = [\"1\", \"2\", \"3\"];");
    }

    public void testSpreadOperatorInClassConst_06() throws Exception {
        checkDeclaration(getTestPath(), "    public const CHILD_CONSTANT = [\"0\", ...parent::CONS^TANT, ];", "    public const ^CONSTANT = [\"1\", \"2\", \"3\"];");
    }

    public void testSpreadOperatorInClassConst_07() throws Exception {
        checkDeclaration(getTestPath(), "    private const CONST1 = [...UnpackClass::CO^NSTANT];", "    public const ^CONSTANT = [\"1\", \"2\", \"3\"];");
    }

    public void testSpreadOperatorInClassConst_08() throws Exception {
        checkDeclaration(getTestPath(), "    private const CONST2 = [1, ...UnpackClass::CONST^ANT];", "    public const ^CONSTANT = [\"1\", \"2\", \"3\"];");
    }

    public void testSpreadOperatorInClassConst_09() throws Exception {
        checkDeclaration(getTestPath(), "    private const CONST3 = [1, ...\\Foo\\UnpackClass::CO^NSTANT, 4];", "    public const ^CONSTANT = [\"1\", \"2\", \"3\"];");
    }

    public void testSpreadOperatorInClassConst_10() throws Exception {
        checkDeclaration(getTestPath(), "    public const CONSTANT5 = [\"0\", ...self::CONSTANT, \"4\", self::CONST^ANT1];", "    public const ^CONSTANT1 = self::CONSTANT;");
    }

    public void testSpreadOperatorInClassConst_11() throws Exception {
        checkDeclaration(getTestPath(), "    private const CONST4 = [...F_CO^NST];", "const ^F_CONST = \"test\";");
    }
}
