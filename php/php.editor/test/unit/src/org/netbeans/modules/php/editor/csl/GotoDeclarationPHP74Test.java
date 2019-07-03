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

    public void testArrowFunctions_01a() throws Exception {
        checkDeclaration(getTestPath(), "$fn1a = fn(int $x) => $x + $^y;", "$^y = 5;");
    }

    public void testArrowFunctions_01b() throws Exception {
        checkDeclaration(getTestPath(), "$fn1b = function ($x) use ($^y) {", "$^y = 5;");
    }

    public void testArrowFunctions_01c() throws Exception {
        checkDeclaration(getTestPath(), "    return $x + $^y;", "$^y = 5;");
    }

    public void testArrowFunctions_01d() throws Exception {
        checkDeclaration(getTestPath(), "$fn2 = fn(int $a, ArrowFunctions $b) => $a + $b->getNumber() * $^y;", "$^y = 5;");
    }

    public void testArrowFunctions_01e() throws Exception {
        checkDeclaration(getTestPath(), "$af = fn() => fn() => $^y;", "$^y = 5;");
    }

    public void testArrowFunctions_01f() throws Exception {
        checkDeclaration(getTestPath(), "(fn() => fn() => $y^)()();", "$^y = 5;");
    }

    public void testArrowFunctions_01g() throws Exception {
        checkDeclaration(getTestPath(), "(fn() => function() use ($^y) {return $y;})()();", "$^y = 5;");
    }

    public void testArrowFunctions_01i() throws Exception {
        checkDeclaration(getTestPath(), "$af = fn(int $x): callable => fn(int $z): int => $x + $^y * $z;", "$^y = 5;");
    }

    public void testArrowFunctions_01j() throws Exception {
        checkDeclaration(getTestPath(), "    return fn() => $x + $^y;", "$^y = 5;");
    }

    public void testArrowFunctions_01k() throws Exception {
        checkDeclaration(getTestPath(), "    return fn($x) => $x + $^y;", "$^y = 5;");
    }

    public void testArrowFunctions_02() throws Exception {
        checkDeclaration(getTestPath(), "$fn1a = fn(int $x) => $^x + $y;", "$fn1a = fn(int $^x) => $x + $y;");
    }

    public void testArrowFunctions_03a() throws Exception {
        checkDeclaration(getTestPath(), "$fn2 = fn(int $a, ArrowFunctions $b) => $^a + $b->getNumber() * $y;", "$fn2 = fn(int $^a, ArrowFunctions $b) => $a + $b->getNumber() * $y;");
    }

    public void testArrowFunctions_03b() throws Exception {
        checkDeclaration(getTestPath(), "$fn2 = fn(int $a, ArrowFunctions $b) => $a + $^b->getNumber() * $y;", "$fn2 = fn(int $a, ArrowFunctions $^b) => $a + $b->getNumber() * $y;");
    }

    public void testArrowFunctions_04() throws Exception {
        checkDeclaration(getTestPath(), "fn (array $x) => $^x; // parameter type", "fn (array $^x) => $x; // parameter type");
    }

    public void testArrowFunctions_05() throws Exception {
        checkDeclaration(getTestPath(), "fn(int $x): int => $^x; // return type", "fn(int $^x): int => $x; // return type");
    }

    public void testArrowFunctions_06a() throws Exception {
        checkDeclaration(getTestPath(), "fn(?array $z, int $x): ?int => $^x + count($z);", "fn(?array $z, int $^x): ?int => $x + count($z);");
    }

    public void testArrowFunctions_06b() throws Exception {
        checkDeclaration(getTestPath(), "fn(?array $z, int $x): ?int => $x + count($^z);", "fn(?array $^z, int $x): ?int => $x + count($z);");
    }

    public void testArrowFunctions_07() throws Exception {
        checkDeclaration(getTestPath(), "fn($x = 100) => $^x; // default value", "fn($^x = 100) => $x; // default value");
    }

    public void testArrowFunctions_08() throws Exception {
        checkDeclaration(getTestPath(), "fn(&$x) => $^x; // reference", "fn(&$^x) => $x; // reference");
    }

    public void testArrowFunctions_09() throws Exception {
        checkDeclaration(getTestPath(), "fn&($x) => $^x; // reference", "fn&($^x) => $x; // reference");
    }

    public void testArrowFunctions_10() throws Exception {
        checkDeclaration(getTestPath(), "fn&(&$x) => $^x; // reference", "fn&(&$^x) => $x; // reference");
    }

    public void testArrowFunctions_11a() throws Exception {
        checkDeclaration(getTestPath(), "fn($x, ...$reset) => $re^set; // variadics", "fn($x, ...$^reset) => $reset; // variadics");
    }

    public void testArrowFunctions_11b() throws Exception {
        checkDeclaration(getTestPath(), "fn($x, &...$reset) => $^reset; // reference variadics", "fn($x, &...$^reset) => $reset; // reference variadics");
    }

    public void testArrowFunctions_12() throws Exception {
        checkDeclaration(getTestPath(), "fn(): int => CONST^ANT_INT;", "const ^CONSTANT_INT = 1000;");
    }

    public void testArrowFunctions_13a() throws Exception {
        checkDeclaration(getTestPath(), "fn(): ArrowFun^ctions => ArrowFunctions::new();", "class ^ArrowFunctions");
    }

    public void testArrowFunctions_13b() throws Exception {
        checkDeclaration(getTestPath(), "fn(): ArrowFunctions => Arrow^Functions::new();", "class ^ArrowFunctions");
    }

    public void testArrowFunctions_13c() throws Exception {
        checkDeclaration(getTestPath(), "function (): Arro^wFunctions {", "class ^ArrowFunctions");
    }

    public void testArrowFunctions_13d() throws Exception {
        checkDeclaration(getTestPath(), "    return ArrowFu^nctions::new();", "class ^ArrowFunctions");
    }

    public void testArrowFunctions_13e() throws Exception {
        checkDeclaration(getTestPath(), "$af = fn(ArrowFun^ctions $x): callable => fn(?ArrowFunctions $z): ?ArrowFunctions => new ArrowFunctions();", "class ^ArrowFunctions");
    }

    public void testArrowFunctions_13f() throws Exception {
        checkDeclaration(getTestPath(), "$af = fn(ArrowFunctions $x): callable => fn(?^ArrowFunctions $z): ?ArrowFunctions => new ArrowFunctions();", "class ^ArrowFunctions");
    }

    public void testArrowFunctions_13g() throws Exception {
        checkDeclaration(getTestPath(), "$af = fn(ArrowFunctions $x): callable => fn(?ArrowFunctions $z): ?ArrowFu^nctions => new ArrowFunctions();", "class ^ArrowFunctions");
    }

    public void testArrowFunctions_13h() throws Exception {
        checkDeclaration(getTestPath(), "$af = fn(ArrowFunctions $x): callable => fn(?ArrowFunctions $z): ?ArrowFunctions => new ArrowFu^nctions();", "class ^ArrowFunctions");
    }

    public void testArrowFunctions_13i() throws Exception {
        checkDeclaration(getTestPath(), "        $af = fn(): ?ArrowFunc^tions => $this;", "class ^ArrowFunctions");
    }

    public void testArrowFunctions_13j() throws Exception {
        checkDeclaration(getTestPath(), "$fn2 = fn(int $a, ArrowF^unctions $b) => $a + $b->getNumber() * $y;", "class ^ArrowFunctions");
    }

    public void testArrowFunctions_14() throws Exception {
        checkDeclaration(getTestPath(), "$fn2 = fn(int $a, ArrowFunctions $b) => $a + $b->getNu^mber() * $y;", "    public function ^getNumber(): int {");
    }

    public void testArrowFunctions_15() throws Exception {
        checkDeclaration(getTestPath(), "fn(): ArrowFunctions => ArrowFunctions::ne^w();", "    public static function ^new() {");
    }

    public void testArrowFunctions_16() throws Exception {
        checkDeclaration(getTestPath(), "        $af = fn($x): ?int => $^x + $y;", "        $af = fn($^x): ?int => $x + $y;");
    }

    public void testArrowFunctions_17() throws Exception {
        checkDeclaration(getTestPath(), "        $af = fn($x): ?int => $x + $^y;", "        $^y = 100;");
    }

    public void testArrowFunctions_18() throws Exception {
        checkDeclaration(getTestPath(), "    return fn() => $^x + $y;", "$fn3 = function ($^x) use ($y) {");
    }

    public void testArrowFunctions_19() throws Exception {
        checkDeclaration(getTestPath(), "    return fn($x) => $^x + $y;", "    return fn($^x) => $x + $y;");
    }

    public void testArrowFunctions_20() throws Exception {
        checkDeclaration(getTestPath(), "    return fn(...$args) => !$^f(...$args);", "function test(callable $^f) {");
    }

    public void testArrowFunctions_21() throws Exception {
        checkDeclaration(getTestPath(), "    return fn(...$args) => !$f(...$ar^gs);", "    return fn(...$^args) => !$f(...$args);");
    }

}
