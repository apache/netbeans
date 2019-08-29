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


public class OccurrencesFinderImplPHP74Test extends OccurrencesFinderImplTestBase {

    public OccurrencesFinderImplPHP74Test(String testName) {
        super(testName);
    }

    // class
    public void testTypedProperties20Class_01() throws Exception {
        checkOccurrences(getTestPath(), "use Bar\\My^Class;", true);
    }

    public void testTypedProperties20Class_02() throws Exception {
        checkOccurrences(getTestPath(), "    public My^Class $myClass;", true);
    }

    public void testTypedProperties20Class_03() throws Exception {
        checkOccurrences(getTestPath(), "    public ?MyCla^ss $myClass2;", true);
    }

    public void testTypedProperties20Class_04() throws Exception {
        checkOccurrences(getTestPath(), "    public \\Bar\\MyCla^ss $myClass3;", true);
    }

    public void testTypedProperties20Class_05() throws Exception {
        checkOccurrences(getTestPath(), "    public ?\\Bar\\^MyClass $myClass4;", true);
    }

    public void testTypedProperties20Class_06() throws Exception {
        checkOccurrences(getTestPath(), "class MyCl^ass {", true);
    }

    public void testTypedProperties20Class_07() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass->publicTe^stMethod();", true);
    }

    public void testTypedProperties20Class_08() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass2->pu^blicTestMethod();", true);
    }

    public void testTypedProperties20Class_09() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass3->p^ublicTestMethod();", true);
    }

    public void testTypedProperties20Class_10() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass4->^publicTestMethod();", true);
    }

    public void testTypedProperties20Class_11() throws Exception {
        checkOccurrences(getTestPath(), "    public function publicTestMe^thod(): void {", true);
    }

    public void testTypedProperties20Class_12() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass::publicStati^cTestMethod();", true);
    }

    public void testTypedProperties20Class_13() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass2::p^ublicStaticTestMethod();", true);
    }

    public void testTypedProperties20Class_14() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass3::^publicStaticTestMethod();", true);
    }

    public void testTypedProperties20Class_15() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass4::^publicStaticTestMethod();", true);
    }

    public void testTypedProperties20Class_16() throws Exception {
        checkOccurrences(getTestPath(), "    public static function publicStaticTestMe^thod(): void {", true);
    }

    // trait
    public void testTypedProperties20Trait_01() throws Exception {
        checkOccurrences(getTestPath(), "use Bar\\My^Class;", true);
    }

    public void testTypedProperties20Trait_02() throws Exception {
        checkOccurrences(getTestPath(), "    public My^Class $myClass;", true);
    }

    public void testTypedProperties20Trait_03() throws Exception {
        checkOccurrences(getTestPath(), "    public ?MyCla^ss $myClass2;", true);
    }

    public void testTypedProperties20Trait_04() throws Exception {
        checkOccurrences(getTestPath(), "    public \\Bar\\MyCla^ss $myClass3;", true);
    }

    public void testTypedProperties20Trait_05() throws Exception {
        checkOccurrences(getTestPath(), "    public ?\\Bar\\^MyClass $myClass4;", true);
    }

    public void testTypedProperties20Trait_06() throws Exception {
        checkOccurrences(getTestPath(), "class MyCl^ass {", true);
    }

    public void testTypedProperties20Trait_07() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass->publicTe^stMethod();", true);
    }

    public void testTypedProperties20Trait_08() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass2->pu^blicTestMethod();", true);
    }

    public void testTypedProperties20Trait_09() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass3->p^ublicTestMethod();", true);
    }

    public void testTypedProperties20Trait_10() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass4->^publicTestMethod();", true);
    }

    public void testTypedProperties20Trait_11() throws Exception {
        checkOccurrences(getTestPath(), "    public function publicTestMe^thod(): void {", true);
    }

    public void testTypedProperties20Trait_12() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass::publicStati^cTestMethod();", true);
    }

    public void testTypedProperties20Trait_13() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass2::p^ublicStaticTestMethod();", true);
    }

    public void testTypedProperties20Trait_14() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass3::^publicStaticTestMethod();", true);
    }

    public void testTypedProperties20Trait_15() throws Exception {
        checkOccurrences(getTestPath(), "        $this->myClass4::^publicStaticTestMethod();", true);
    }

    public void testTypedProperties20Trait_16() throws Exception {
        checkOccurrences(getTestPath(), "    public static function publicStaticTestMe^thod(): void {", true);
    }

    public void testSpreadOperatorInArrayExpression_01() throws Exception {
        checkOccurrences(getTestPath(), "$a^rray1 = [1, 2, 3];", true);
    }

    public void testSpreadOperatorInArrayExpression_02() throws Exception {
        checkOccurrences(getTestPath(), "$array2 = [...$array^1];", true);
    }

    public void testSpreadOperatorInArrayExpression_03() throws Exception {
        checkOccurrences(getTestPath(), "$array3 = [0, ...$arra^y1];", true);
    }

    public void testSpreadOperatorInArrayExpression_04() throws Exception {
        checkOccurrences(getTestPath(), "$array4 = array(...$array^1, ...$array2, 111);", true);
    }

    public void testSpreadOperatorInArrayExpression_05() throws Exception {
        checkOccurrences(getTestPath(), "$array5 = [...$^array1, ...$array1];", true);
    }

    public void testSpreadOperatorInArrayExpression_06() throws Exception {
        checkOccurrences(getTestPath(), "$array5 = [...$array1, ...$ar^ray1];", true);
    }

    public void testSpreadOperatorInArrayExpression_07() throws Exception {
        checkOccurrences(getTestPath(), "$arr^ay2 = [...$array1];", true);
    }

    public void testSpreadOperatorInArrayExpression_08() throws Exception {
        checkOccurrences(getTestPath(), "$array4 = array(...$array1, ...$arra^y2, 111);", true);
    }

    public void testSpreadOperatorInArrayExpression_09() throws Exception {
        checkOccurrences(getTestPath(), "function getArr^ay() {", true);
    }

    public void testSpreadOperatorInArrayExpression_10() throws Exception {
        checkOccurrences(getTestPath(), "$array6 = [...getAr^ray()];", true);
    }

    public void testSpreadOperatorInArrayExpression_11() throws Exception {
        checkOccurrences(getTestPath(), "function arrayGenerat^or() {", true);
    }

    public void testSpreadOperatorInArrayExpression_12() throws Exception {
        checkOccurrences(getTestPath(), "$array8 = [...arrayGenera^tor()];", true);
    }

    public void testSpreadOperatorInArrayExpression_GlobalConst_01() throws Exception {
        checkOccurrences(getTestPath(), "const CONST^ANT = [0, 1, 2, 3];", true);
    }

    public void testSpreadOperatorInArrayExpression_GlobalConst_02() throws Exception {
        checkOccurrences(getTestPath(), "const CONSTANT1 = [...^CONSTANT];", true);
    }

    public void testSpreadOperatorInArrayExpression_GlobalConst_03() throws Exception {
        checkOccurrences(getTestPath(), "const CONSTANT2 = [100, ...CONSTANT^, ...CONSTANT1,];", true);
    }

    public void testSpreadOperatorInArrayExpression_GlobalConst_04() throws Exception {
        checkOccurrences(getTestPath(), "const CONSTANT3 = [...CONSTANT2, 100 => 0, ...CON^STANT];", true);
    }

    public void testSpreadOperatorInArrayExpression_GlobalConst_05() throws Exception {
        checkOccurrences(getTestPath(), "const CONST^ANT1 = [...CONSTANT];", true);
    }

    public void testSpreadOperatorInArrayExpression_GlobalConst_06() throws Exception {
        checkOccurrences(getTestPath(), "const CONSTANT2 = [100, ...CONSTANT, ...CON^STANT1,];", true);
    }

    public void testSpreadOperatorInArrayExpression_GlobalConst_07() throws Exception {
        checkOccurrences(getTestPath(), "const CONST^ANT2 = [100, ...CONSTANT, ...CONSTANT1,];", true);
    }

    public void testSpreadOperatorInArrayExpression_GlobalConst_08() throws Exception {
        checkOccurrences(getTestPath(), "const CONSTANT3 = [...CONSTAN^T2, 100 => 0, ...CONSTANT];", true);
    }

    public void testSpreadOperatorInArrayExpression_GlobalConst_09() throws Exception {
        checkOccurrences(getTestPath(), "const CONSTANT4 = [...CON^STANT2, 100 => 0, ...\\Bar\\BAR_CONSTANT];", true);
    }

    public void testSpreadOperatorInArrayExpression_GlobalConst_10() throws Exception {
        checkOccurrences(getTestPath(), "const CONSTANT5 = [...CONSTANT^2, 100 => 0, ...\\Bar\\Bar::BAR_CONSTANT];", true);
    }

    public void testSpreadOperatorInArrayExpression_GlobalConst_11() throws Exception {
        checkOccurrences(getTestPath(), "const BAR_CO^NSTANT = [];", true);
    }

    public void testSpreadOperatorInArrayExpression_GlobalConst_12() throws Exception {
        checkOccurrences(getTestPath(), "const CONSTANT4 = [...CONSTANT2, 100 => 0, ...\\Bar\\BAR_CO^NSTANT];", true);
    }

    public void testSpreadOperatorInArrayExpression_GlobalConst_13() throws Exception {
        checkOccurrences(getTestPath(), "    public const BAR_C^ONSTANT = \"test\";", true);
    }

    public void testSpreadOperatorInArrayExpression_GlobalConst_14() throws Exception {
        checkOccurrences(getTestPath(), "const CONSTANT5 = [...CONSTANT2, 100 => 0, ...\\Bar\\Bar::BAR^_CONSTANT];", true);
    }

    public void testSpreadOperatorInClassConst_01a() throws Exception {
        checkOccurrences(getTestPath(), "const F_C^ONST = \"test\";", true);
    }

    public void testSpreadOperatorInClassConst_01b() throws Exception {
        checkOccurrences(getTestPath(), "    private const CONST4 = [...F_CON^ST];", true);
    }

    public void testSpreadOperatorInClassConst_01c() throws Exception {
        checkOccurrences(getTestPath(), "use const \\Foo\\F_CON^ST;", true);
    }

    public void testSpreadOperatorInClassConst_02a() throws Exception {
        checkOccurrences(getTestPath(), "    public const ^CONSTANT = [\"1\", \"2\", \"3\"];", true);
    }

    public void testSpreadOperatorInClassConst_02b() throws Exception {
        checkOccurrences(getTestPath(), "    public const CONSTANT1 = self::C^ONSTANT;", true);
    }

    public void testSpreadOperatorInClassConst_02c() throws Exception {
        checkOccurrences(getTestPath(), "    public const CONSTANT2 = [...self::CONSTA^NT];", true);
    }

    public void testSpreadOperatorInClassConst_02d() throws Exception {
        checkOccurrences(getTestPath(), "    public const CONSTANT3 = [...self::CONST^ANT, \"4\"];", true);
    }

    public void testSpreadOperatorInClassConst_02e() throws Exception {
        checkOccurrences(getTestPath(), "    public const CONSTANT4 = [\"0\", ...self::CONST^ANT, \"4\"];", true);
    }

    public void testSpreadOperatorInClassConst_02f() throws Exception {
        checkOccurrences(getTestPath(), "    public const CONSTANT5 = [\"0\", ...self::CONSTA^NT, \"4\", self::CONSTANT1];", true);
    }

    public void testSpreadOperatorInClassConst_02g() throws Exception {
        checkOccurrences(getTestPath(), "    public const CHILD_CONSTANT = [\"0\", ...parent::CONSTA^NT, ];", true);
    }

    public void testSpreadOperatorInClassConst_02h() throws Exception {
        checkOccurrences(getTestPath(), "    private const CONST1 = [...UnpackClass::CONSTA^NT];", true);
    }

    public void testSpreadOperatorInClassConst_02i() throws Exception {
        checkOccurrences(getTestPath(), "    private const CONST2 = [1, ...UnpackClass::CONSTA^NT];", true);
    }

    public void testSpreadOperatorInClassConst_02j() throws Exception {
        checkOccurrences(getTestPath(), "    private const CONST3 = [1, ...\\Foo\\UnpackClass::C^ONSTANT, 4];", true);
    }

    public void testArrowFunctions_01a() throws Exception {
        checkOccurrences(getTestPath(), "const CONST^ANT_INT = 1000;", true);
    }

    public void testArrowFunctions_01b() throws Exception {
        checkOccurrences(getTestPath(), "fn(): int => CONST^ANT_INT;", true);
    }

    public void testArrowFunctions_02a() throws Exception {
        checkOccurrences(getTestPath(), "$^y = 5;", true);
    }

    public void testArrowFunctions_02b() throws Exception {
        checkOccurrences(getTestPath(), "$fn1a = fn(int $x) => $x + $^y;", true);
    }

    public void testArrowFunctions_02c() throws Exception {
        checkOccurrences(getTestPath(), "$fn1b = function ($x) use ($^y) {", true);
    }

    public void testArrowFunctions_02d() throws Exception {
        checkOccurrences(getTestPath(), "    return $x + $^y;", true);
    }

    public void testArrowFunctions_02e() throws Exception {
        checkOccurrences(getTestPath(), "$fn2 = fn(int $a, ArrowFunctions $b) => $a + $b->getNumber() * $^y;", true);
    }

    public void testArrowFunctions_02f() throws Exception {
        checkOccurrences(getTestPath(), "$af = fn() => fn() => $^y;", true);
    }

    public void testArrowFunctions_02g() throws Exception {
        checkOccurrences(getTestPath(), "(fn() => fn() => $^y)()();", true);
    }

    public void testArrowFunctions_02h() throws Exception {
        checkOccurrences(getTestPath(), "(fn() => function() use ($^y) {return $y;})()();", true);
    }

    public void testArrowFunctions_02i() throws Exception {
        checkOccurrences(getTestPath(), "(fn() => function() use ($y) {return $^y;})()();", true);
    }

    public void testArrowFunctions_02j() throws Exception {
        checkOccurrences(getTestPath(), "$af = fn(int $x): callable => fn(int $z): int => $x + $^y * $z;", true);
    }

    public void testArrowFunctions_02k() throws Exception {
        checkOccurrences(getTestPath(), "$fn3 = function ($x) use ($^y) {", true);
    }

    public void testArrowFunctions_02l() throws Exception {
        checkOccurrences(getTestPath(), "    return fn() => $x + $^y;", true);
    }

    public void testArrowFunctions_02m() throws Exception {
        checkOccurrences(getTestPath(), "$fn4 = function ($x) use ($^y) {", true);
    }

    public void testArrowFunctions_02n() throws Exception {
        checkOccurrences(getTestPath(), "    return fn($x) => $x + $^y;", true);
    }

    public void testArrowFunctions_03a() throws Exception {
        checkOccurrences(getTestPath(), "$fn1a = fn(int $^x) => $x + $y;", true);
    }

    public void testArrowFunctions_03b() throws Exception {
        checkOccurrences(getTestPath(), "$fn1a = fn(int $x) => $^x + $y;", true);
    }

    public void testArrowFunctions_04a() throws Exception {
        checkOccurrences(getTestPath(), "$fn1b = function ($^x) use ($y) {", true);
    }

    public void testArrowFunctions_04b() throws Exception {
        checkOccurrences(getTestPath(), "    return $^x + $y;", true);
    }

    public void testArrowFunctions_05a() throws Exception {
        checkOccurrences(getTestPath(), "$fn2 = fn(int $a, ArrowFunc^tions $b) => $a + $b->getNumber() * $y;", true);
    }

    public void testArrowFunctions_05b() throws Exception {
        checkOccurrences(getTestPath(), "fn(): ArrowFunction^s => ArrowFunctions::new();", true);
    }

    public void testArrowFunctions_05c() throws Exception {
        checkOccurrences(getTestPath(), "fn(): ArrowFunctions => Arr^owFunctions::new();", true);
    }

    public void testArrowFunctions_05d() throws Exception {
        checkOccurrences(getTestPath(), "function (): ArrowFunc^tions {", true);
    }

    public void testArrowFunctions_05e() throws Exception {
        checkOccurrences(getTestPath(), "    return ArrowFunctio^ns::new();", true);
    }

    public void testArrowFunctions_05f() throws Exception {
        checkOccurrences(getTestPath(), "$af = fn(ArrowFunc^tions $x): callable => fn(?ArrowFunctions $z): ?ArrowFunctions => new ArrowFunctions();", true);
    }

    public void testArrowFunctions_05g() throws Exception {
        checkOccurrences(getTestPath(), "$af = fn(ArrowFunctions $x): callable => fn(?Arro^wFunctions $z): ?ArrowFunctions => new ArrowFunctions();", true);
    }

    public void testArrowFunctions_05h() throws Exception {
        checkOccurrences(getTestPath(), "$af = fn(ArrowFunctions $x): callable => fn(?ArrowFunctions $z): ?ArrowFunctio^ns => new ArrowFunctions();", true);
    }

    public void testArrowFunctions_05i() throws Exception {
        checkOccurrences(getTestPath(), "$af = fn(ArrowFunctions $x): callable => fn(?ArrowFunctions $z): ?ArrowFunctions => new ArrowF^unctions();", true);
    }

    public void testArrowFunctions_05j() throws Exception {
        checkOccurrences(getTestPath(), "class ArrowFunct^ions", true);
    }

    public void testArrowFunctions_05k() throws Exception {
        checkOccurrences(getTestPath(), "        $af = fn(): ?ArrowFunctio^ns => $this;", true);
    }

    public void testArrowFunctions_05l() throws Exception {
        checkOccurrences(getTestPath(), "        return new ArrowFunctio^ns();", true);
    }

    public void testArrowFunctions_06a() throws Exception {
        checkOccurrences(getTestPath(), "$fn2 = fn(int $^a, ArrowFunctions $b) => $a + $b->getNumber() * $y;", true);
    }

    public void testArrowFunctions_06b() throws Exception {
        checkOccurrences(getTestPath(), "$fn2 = fn(int $a, ArrowFunctions $b) => $^a + $b->getNumber() * $y;", true);
    }

    public void testArrowFunctions_07a() throws Exception {
        checkOccurrences(getTestPath(), "fn (array $^x) => $x; // parameter type", true);
    }

    public void testArrowFunctions_07b() throws Exception {
        checkOccurrences(getTestPath(), "fn (array $x) => $^x; // parameter type", true);
    }

    public void testArrowFunctions_08a() throws Exception {
        checkOccurrences(getTestPath(), "fn(int $^x): int => $x; // return type", true);
    }

    public void testArrowFunctions_08b() throws Exception {
        checkOccurrences(getTestPath(), "fn(int $x): int => $^x; // return type", true);
    }

    public void testArrowFunctions_09a() throws Exception {
        checkOccurrences(getTestPath(), "fn(?array $^z, int $x): ?int => $x + count($z); // parameter and return type", true);
    }

    public void testArrowFunctions_09b() throws Exception {
        checkOccurrences(getTestPath(), "fn(?array $z, int $x): ?int => $x + count($^z); // parameter and return type", true);
    }

    public void testArrowFunctions_10a() throws Exception {
        checkOccurrences(getTestPath(), "fn(?array $z, int $^x): ?int => $x + count($z); // parameter and return type", true);
    }

    public void testArrowFunctions_10b() throws Exception {
        checkOccurrences(getTestPath(), "fn(?array $z, int $x): ?int => $^x + count($z); // parameter and return type", true);
    }

    public void testArrowFunctions_11a() throws Exception {
        checkOccurrences(getTestPath(), "fn($^x = 100) => $x; // default value", true);
    }

    public void testArrowFunctions_11b() throws Exception {
        checkOccurrences(getTestPath(), "fn($x = 100) => $^x; // default value", true);
    }

    public void testArrowFunctions_12a() throws Exception {
        checkOccurrences(getTestPath(), "fn(&$^x) => $x; // reference", true);
    }

    public void testArrowFunctions_12b() throws Exception {
        checkOccurrences(getTestPath(), "fn(&$x) => $^x; // reference", true);
    }

    public void testArrowFunctions_13a() throws Exception {
        checkOccurrences(getTestPath(), "fn&($^x) => $x; // reference", true);
    }

    public void testArrowFunctions_13b() throws Exception {
        checkOccurrences(getTestPath(), "fn&($x) => $^x; // reference", true);
    }

    public void testArrowFunctions_14a() throws Exception {
        checkOccurrences(getTestPath(), "fn&(&$^x) => $x; // reference", true);
    }

    public void testArrowFunctions_14b() throws Exception {
        checkOccurrences(getTestPath(), "fn&(&$x) => $^x; // reference", true);
    }

    public void testArrowFunctions_15a() throws Exception {
        checkOccurrences(getTestPath(), "fn($x, ...$re^set) => $reset; // variadics", true);
    }

    public void testArrowFunctions_15b() throws Exception {
        checkOccurrences(getTestPath(), "fn($x, ...$reset) => $r^eset; // variadics", true);
    }

    public void testArrowFunctions_16a() throws Exception {
        checkOccurrences(getTestPath(), "fn($x, &...$re^set) => $reset; // reference variadics", true);
    }

    public void testArrowFunctions_16b() throws Exception {
        checkOccurrences(getTestPath(), "fn($x, &...$reset) => $res^et; // reference variadics", true);
    }

    public void testArrowFunctions_17a() throws Exception {
        checkOccurrences(getTestPath(), "$af = fn(int $^x): callable => fn(int $z): int => $x + $y * $z;", true);
    }

    public void testArrowFunctions_17b() throws Exception {
        checkOccurrences(getTestPath(), "$af = fn(int $x): callable => fn(int $z): int => $^x + $y * $z;", true);
    }

    public void testArrowFunctions_18a() throws Exception {
        checkOccurrences(getTestPath(), "$af = fn(int $x): callable => fn(int $^z): int => $x + $y * $z;", true);
    }

    public void testArrowFunctions_18b() throws Exception {
        checkOccurrences(getTestPath(), "$af = fn(int $x): callable => fn(int $z): int => $x + $y * $^z;", true);
    }

    public void testArrowFunctions_19a() throws Exception {
        checkOccurrences(getTestPath(), "$fn3 = function ($^x) use ($y) {", true);
    }

    public void testArrowFunctions_19b() throws Exception {
        checkOccurrences(getTestPath(), "    return fn() => $^x + $y;", true);
    }

    public void testArrowFunctions_20a() throws Exception {
        checkOccurrences(getTestPath(), "$fn4 = function ($^x) use ($y) {", true);
    }

    public void testArrowFunctions_21a() throws Exception {
        checkOccurrences(getTestPath(), "    return fn($^x) => $x + $y;", true);
    }

    public void testArrowFunctions_21b() throws Exception {
        checkOccurrences(getTestPath(), "    return fn($x) => $^x + $y;", true);
    }

    // in class
    public void testArrowFunctions_22a() throws Exception {
        checkOccurrences(getTestPath(), "        $^y = 100;", true);
    }

    public void testArrowFunctions_22b() throws Exception {
        checkOccurrences(getTestPath(), "        $af = fn($x): ?int => $x + $^y;", true);
    }

    public void testArrowFunctions_23a() throws Exception {
        checkOccurrences(getTestPath(), "        $af = fn($^x): ?int => $x + $y;", true);
    }

    public void testArrowFunctions_23b() throws Exception {
        checkOccurrences(getTestPath(), "        $af = fn($x): ?int => $^x + $y;", true);
    }

    public void testArrowFunctions_24a() throws Exception {
        checkOccurrences(getTestPath(), "function test(callable $^f) {", true);
    }

    public void testArrowFunctions_24b() throws Exception {
        checkOccurrences(getTestPath(), "    return fn(...$args) => !$^f(...$args);", true);
    }

    public void testArrowFunctions_25a() throws Exception {
        checkOccurrences(getTestPath(), "    return fn(...$a^rgs) => !$f(...$args);", true);
    }

    public void testArrowFunctions_25b() throws Exception {
        checkOccurrences(getTestPath(), "    return fn(...$args) => !$f(...$ar^gs);", true);
    }

}
