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
package org.netbeans.modules.php.editor.csl;

public class OccurrencesFinderImplPHP81Test extends OccurrencesFinderImplTestBase {

    public OccurrencesFinderImplPHP81Test(String testName) {
        super(testName);
    }

    @Override
    protected String getBaseTestFolderPath() {
        return super.getBaseTestFolderPath() + "php81/";
    }

    public void testNewInInitializersWithStaticVariable_01() throws Exception {
        checkOccurrences(getTestPath(), "class Static^Variable {}", true);
    }

    public void testNewInInitializersWithStaticVariable_02() throws Exception {
        checkOccurrences(getTestPath(), "static $staticVariable = new StaticVaria^ble;", true);
    }

    public void testNewInInitializersWithStaticVariable_03() throws Exception {
        checkOccurrences(getTestPath(), "static $staticVariable = new StaticVar^iable();", true);
    }

    public void testNewInInitializersWithStaticVariable_04() throws Exception {
        checkOccurrences(getTestPath(), "static $staticVariable = new StaticVari^able(1);", true);
    }

    public void testNewInInitializersWithStaticVariable_05() throws Exception {
        checkOccurrences(getTestPath(), "static $staticVariable = new StaticVariab^le(x: 1);", true);
    }

    public void testNewInInitializersWithConstant_01() throws Exception {
        checkOccurrences(getTestPath(), "class Const^ant {}", true);
    }

    public void testNewInInitializersWithConstant_02() throws Exception {
        checkOccurrences(getTestPath(), "const CONSTANT = new C^onstant;", true);
    }

    public void testNewInInitializersWithConstant_03() throws Exception {
        checkOccurrences(getTestPath(), "const CONSTANT = new Con^stant();", true);
    }

    public void testNewInInitializersWithConstant_04() throws Exception {
        checkOccurrences(getTestPath(), "const CONSTANT = new Cons^tant(\"test\", \"constant\");", true);
    }

    public void testNewInInitializersWithConstant_05() throws Exception {
        checkOccurrences(getTestPath(), "const CONSTANT = new Consta^nt(test: \"test\", constant: \"constant\");", true);
    }

    public void testNewInInitializersWithFunc_01() throws Exception {
        checkOccurrences(getTestPath(), "class Fun^c {}", true);
    }

    public void testNewInInitializersWithFunc_02() throws Exception {
        checkOccurrences(getTestPath(), "function func1($param = new Fu^nc) {}", true);
    }

    public void testNewInInitializersWithFunc_03() throws Exception {
        checkOccurrences(getTestPath(), "function func2($param = new Fun^c()) {}", true);
    }

    public void testNewInInitializersWithFunc_04() throws Exception {
        checkOccurrences(getTestPath(), "function func3($param = new F^unc(1)) {}", true);
    }

    public void testNewInInitializersWithFunc_05() throws Exception {
        checkOccurrences(getTestPath(), "function func4($param = new Fun^c(test: 1)) {}", true);
    }

    public void testNewInInitializersWithMethod_01() throws Exception {
        checkOccurrences(getTestPath(), "class Metho^d {}", true);
    }

    public void testNewInInitializersWithMethod_02() throws Exception {
        checkOccurrences(getTestPath(), "public $prop1 = new Meth^od,", true);
    }

    public void testNewInInitializersWithMethod_03() throws Exception {
        checkOccurrences(getTestPath(), "public $prop2 = new Meth^od(),", true);
    }

    public void testNewInInitializersWithMethod_04() throws Exception {
        checkOccurrences(getTestPath(), "public $prop3 = new Meth^od(\"test\"),", true);
    }

    public void testNewInInitializersWithMethod_05() throws Exception {
        checkOccurrences(getTestPath(), "public $prop4 = new M^ethod(test: \"test\"),", true);
    }

    public void testNewInInitializersWithMethod_06() throws Exception {
        checkOccurrences(getTestPath(), "$param = new Meth^od,", true);
    }

    public void testNewInInitializersWithMethod_07() throws Exception {
        checkOccurrences(getTestPath(), "$param = new M^ethod(),", true);
    }

    public void testNewInInitializersWithMethod_08() throws Exception {
        checkOccurrences(getTestPath(), "$param = new Metho^d(\"test\"),", true);
    }

    public void testNewInInitializersWithMethod_09() throws Exception {
        checkOccurrences(getTestPath(), "$param = new Met^hod(test: \"test\"),", true);
    }

    public void testNewInInitializersWithAttribute_01() throws Exception {
        checkOccurrences(getTestPath(), "class Fo^o {}", true);
    }

    public void testNewInInitializersWithAttribute_02() throws Exception {
        checkOccurrences(getTestPath(), "#[AnAttribute(new F^oo)]", true);
    }

    public void testNewInInitializersWithAttribute_03() throws Exception {
        checkOccurrences(getTestPath(), "#[AnAttribute(new Fo^o())]", true);
    }

    public void testNewInInitializersWithAttribute_04() throws Exception {
        checkOccurrences(getTestPath(), "#[AnAttribute(new Fo^o(1))]", true);
    }

    public void testNewInInitializersWithAttribute_05() throws Exception {
        checkOccurrences(getTestPath(), "#[AnAttribute(new F^oo(x: 1))]", true);
    }

    public void testPureIntersectionType_01a() throws Exception {
        checkOccurrences(getTestPath(), "class Fo^o {}", true);
    }

    public void testPureIntersectionType_02a() throws Exception {
        checkOccurrences(getTestPath(), "function paramType(F^oo&Bar $test): void {", true);
    }

    public void testPureIntersectionType_03a() throws Exception {
        checkOccurrences(getTestPath(), "function returnType(): Fo^o&Bar {", true);
    }

    public void testPureIntersectionType_04a() throws Exception {
        checkOccurrences(getTestPath(), "private Fo^o&Bar $test; // class", true);
    }

    public void testPureIntersectionType_05a() throws Exception {
        checkOccurrences(getTestPath(), "public function paramType(F^oo&Bar $test): void { // class", true);
    }

    public void testPureIntersectionType_06a() throws Exception {
        checkOccurrences(getTestPath(), "public function returnType(): Fo^o&Bar { // class", true);
    }

    public void testPureIntersectionType_07a() throws Exception {
        checkOccurrences(getTestPath(), "private F^oo&Bar $test; // trait", true);
    }

    public void testPureIntersectionType_08a() throws Exception {
        checkOccurrences(getTestPath(), "public function paramType(Fo^o&Bar $test1, Foo&Bar $test2): void { // trait", true);
    }

    public void testPureIntersectionType_09a() throws Exception {
        checkOccurrences(getTestPath(), "public function paramType(Foo&Bar $test1, Fo^o&Bar $test2): void { // trait", true);
    }

    public void testPureIntersectionType_10a() throws Exception {
        checkOccurrences(getTestPath(), "public function returnType(): F^oo&Bar { // trait", true);
    }

    public void testPureIntersectionType_11a() throws Exception {
        checkOccurrences(getTestPath(), "public function paramType(F^oo&Bar $test);", true);
    }

    public void testPureIntersectionType_12a() throws Exception {
        checkOccurrences(getTestPath(), "public function returnType(): Fo^o&Bar;", true);
    }

    public void testPureIntersectionType_13a() throws Exception {
        checkOccurrences(getTestPath(), "$closure = function(F^oo&Bar $test1, $test2): void {};", true);
    }

    public void testPureIntersectionType_14a() throws Exception {
        checkOccurrences(getTestPath(), "$closure = function(int $test): Fo^o&Bar {};", true);
    }

    public void testPureIntersectionType_15a() throws Exception {
        checkOccurrences(getTestPath(), "$arrow = fn(F^oo&Bar $test) => $test;", true);
    }

    public void testPureIntersectionType_16a() throws Exception {
        checkOccurrences(getTestPath(), "$arrow = fn(Fo^o&Bar $test): Foo&Bar => $test;", true);
    }

    public void testPureIntersectionType_17a() throws Exception {
        checkOccurrences(getTestPath(), "$arrow = fn(Foo&Bar $test): Fo^o&Bar => $test;", true);
    }

    public void testPureIntersectionType_01b() throws Exception {
        checkOccurrences(getTestPath(), "class B^ar {}", true);
    }

    public void testPureIntersectionType_02b() throws Exception {
        checkOccurrences(getTestPath(), "function paramType(Foo&Ba^r $test): void {", true);
    }

    public void testPureIntersectionType_03b() throws Exception {
        checkOccurrences(getTestPath(), "function returnType(): Foo&B^ar {", true);
    }

    public void testPureIntersectionType_04b() throws Exception {
        checkOccurrences(getTestPath(), "private Foo&Ba^r $test; // class", true);
    }

    public void testPureIntersectionType_05b() throws Exception {
        checkOccurrences(getTestPath(), "public function paramType(Foo&Ba^r $test): void { // class", true);
    }

    public void testPureIntersectionType_06b() throws Exception {
        checkOccurrences(getTestPath(), "public function returnType(): Foo&B^ar { // class", true);
    }

    public void testPureIntersectionType_07b() throws Exception {
        checkOccurrences(getTestPath(), "private Foo&B^ar $test; // trait", true);
    }

    public void testPureIntersectionType_08b() throws Exception {
        checkOccurrences(getTestPath(), "public function paramType(Foo&B^ar $test1, Foo&Bar $test2): void { // trait", true);
    }

    public void testPureIntersectionType_09b() throws Exception {
        checkOccurrences(getTestPath(), "public function paramType(Foo&Bar $test1, Foo&B^ar $test2): void { // trait", true);
    }

    public void testPureIntersectionType_10b() throws Exception {
        checkOccurrences(getTestPath(), "public function returnType(): Foo&Ba^r { // trait", true);
    }

    public void testPureIntersectionType_11b() throws Exception {
        checkOccurrences(getTestPath(), "public function paramType(Foo&B^ar $test);", true);
    }

    public void testPureIntersectionType_12b() throws Exception {
        checkOccurrences(getTestPath(), "public function returnType(): Foo&B^ar;", true);
    }

    public void testPureIntersectionType_13b() throws Exception {
        checkOccurrences(getTestPath(), "$closure = function(Foo&Ba^r $test1, $test2): void {};", true);
    }

    public void testPureIntersectionType_14b() throws Exception {
        checkOccurrences(getTestPath(), "$closure = function(int $test): Foo&B^ar {};", true);
    }

    public void testPureIntersectionType_15b() throws Exception {
        checkOccurrences(getTestPath(), "$arrow = fn(Foo&B^ar $test) => $test;", true);
    }

    public void testPureIntersectionType_16b() throws Exception {
        checkOccurrences(getTestPath(), "$arrow = fn(Foo&B^ar $test): Foo&Bar => $test;", true);
    }

    public void testPureIntersectionType_17b() throws Exception {
        checkOccurrences(getTestPath(), "$arrow = fn(Foo&Bar $test): Foo&B^ar => $test;", true);
    }

    public void testEnumerations_a01() throws Exception {
        checkOccurrences(getTestPath(), "enum Simp^le {", true);
    }

    public void testEnumerations_a02() throws Exception {
        checkOccurrences(getTestPath(), "        Si^mple::CASE2;", true);
    }

    public void testEnumerations_a03() throws Exception {
        checkOccurrences(getTestPath(), "        Simpl^e::CONSTANT1;", true);
    }

    public void testEnumerations_a04() throws Exception {
        checkOccurrences(getTestPath(), "        Simp^le::publicStaticMethod();", true);
    }

    public void testEnumerations_a05() throws Exception {
        checkOccurrences(getTestPath(), "        Sim^ple::CASE1->publicMethod();", true);
    }

    public void testEnumerations_a06() throws Exception {
        checkOccurrences(getTestPath(), "        Si^mple::CASE1::publicStaticMethod();", true);
    }

    public void testEnumerations_a07() throws Exception {
        checkOccurrences(getTestPath(), "use Enum1\\Simp^le;", true);
    }

    public void testEnumerations_a08() throws Exception {
        checkOccurrences(getTestPath(), "Sim^ple::CASE1::CONSTANT1;", true);
    }

    public void testEnumerations_a09() throws Exception {
        checkOccurrences(getTestPath(), "S^imple::CASE1::CASE2;", true);
    }

    public void testEnumerations_a10() throws Exception {
        checkOccurrences(getTestPath(), "Simpl^e::CASE2->publicMethod();", true);
    }

    public void testEnumerations_a11() throws Exception {
        checkOccurrences(getTestPath(), "S^imple::CASE2::publicStaticMethod();", true);
    }

    public void testEnumerations_a12() throws Exception {
        checkOccurrences(getTestPath(), "Simpl^e::publicStaticMethod(); // 2", true);
    }

    public void testEnumerations_a13() throws Exception {
        checkOccurrences(getTestPath(), "Sim^ple::staticTest();", true);
    }

    public void testEnumerations_a14() throws Exception {
        checkOccurrences(getTestPath(), "$i = Simp^le::CASE2;", true);
    }

    public void testEnumerations_b01() throws Exception {
        checkOccurrences(getTestPath(), "    case CAS^E1;", true);
    }

    public void testEnumerations_b02() throws Exception {
        checkOccurrences(getTestPath(), "            static::CA^SE1 => 'Case1',", true);
    }

    public void testEnumerations_b03() throws Exception {
        checkOccurrences(getTestPath(), "        Simple::CA^SE1->publicMethod();", true);
    }

    public void testEnumerations_b04() throws Exception {
        checkOccurrences(getTestPath(), "        Simple::CAS^E1::publicStaticMethod();", true);
    }

    public void testEnumerations_b05() throws Exception {
        checkOccurrences(getTestPath(), "        self::CAS^E1;", true);
    }

    public void testEnumerations_b06() throws Exception {
        checkOccurrences(getTestPath(), "        self::CASE^1->publicMethod();", true);
    }

    public void testEnumerations_b07() throws Exception {
        checkOccurrences(getTestPath(), "        static::C^ASE1;", true);
    }

    public void testEnumerations_b08() throws Exception {
        checkOccurrences(getTestPath(), "        static::CA^SE1->publicMethod();", true);
    }

    public void testEnumerations_b09() throws Exception {
        checkOccurrences(getTestPath(), "Simple::CAS^E1::CONSTANT1;", true);
    }

    public void testEnumerations_b10() throws Exception {
        checkOccurrences(getTestPath(), "Simple::CASE^1::CASE2;", true);
    }

    public void testEnumerations_b11() throws Exception {
        checkOccurrences(getTestPath(), "$i::C^ASE1;", true);
    }

    public void testEnumerations_c01() throws Exception {
        checkOccurrences(getTestPath(), "    case CA^SE2;", true);
    }

    public void testEnumerations_c02() throws Exception {
        checkOccurrences(getTestPath(), "    const CONSTANT2 = self::C^ASE2;", true);
    }

    public void testEnumerations_c03() throws Exception {
        checkOccurrences(getTestPath(), "            static::CASE^2 => 'Case2',", true);
    }

    public void testEnumerations_c04() throws Exception {
        checkOccurrences(getTestPath(), "        Simple::CAS^E2;", true);
    }

    public void testEnumerations_c05() throws Exception {
        checkOccurrences(getTestPath(), "Simple::CASE1::C^ASE2;", true);
    }

    public void testEnumerations_c06() throws Exception {
        checkOccurrences(getTestPath(), "Simple::C^ASE2->publicMethod();", true);
    }

    public void testEnumerations_c07() throws Exception {
        checkOccurrences(getTestPath(), "Simple::CAS^E2::publicStaticMethod();", true);
    }

    public void testEnumerations_c08() throws Exception {
        checkOccurrences(getTestPath(), "$i = Simple::CAS^E2;", true);
    }

    public void testEnumerations_d01() throws Exception {
        checkOccurrences(getTestPath(), "    public function publicM^ethod(): void {", true);
    }

    public void testEnumerations_d02() throws Exception {
        checkOccurrences(getTestPath(), "        Simple::CASE1->publ^icMethod();", true);
    }

    public void testEnumerations_d03() throws Exception {
        checkOccurrences(getTestPath(), "        self::CASE1->pub^licMethod();", true);
    }

    public void testEnumerations_d04() throws Exception {
        checkOccurrences(getTestPath(), "        static::CASE1->publicMet^hod();", true);
    }

    public void testEnumerations_d05() throws Exception {
        checkOccurrences(getTestPath(), "Simple::CASE2->publicMetho^d();", true);
    }

    public void testEnumerations_d06() throws Exception {
        checkOccurrences(getTestPath(), "$i->publi^cMethod();", true);
    }

    public void testEnumerations_e01() throws Exception {
        checkOccurrences(getTestPath(), "    public static function publicStati^cMethod(): void {", true);
    }

    public void testEnumerations_e02() throws Exception {
        checkOccurrences(getTestPath(), "        Simple::publicStaticMeth^od();", true);
    }

    public void testEnumerations_e03() throws Exception {
        checkOccurrences(getTestPath(), "        Simple::CASE1::publicStaticMet^hod();", true);
    }

    public void testEnumerations_e04() throws Exception {
        checkOccurrences(getTestPath(), "        self::public^StaticMethod();", true);
    }

    public void testEnumerations_e05() throws Exception {
        checkOccurrences(getTestPath(), "        static::publicStatic^Method();", true);
    }

    public void testEnumerations_e06() throws Exception {
        checkOccurrences(getTestPath(), "Simple::CASE2::publ^icStaticMethod();", true);
    }

    public void testEnumerations_e07() throws Exception {
        checkOccurrences(getTestPath(), "Simple::publicStaticMet^hod(); // 2", true);
    }

    public void testEnumerations_e08() throws Exception {
        checkOccurrences(getTestPath(), "$i::publicStaticMeth^od();", true);
    }

    public void testEnumerations_f01() throws Exception {
        checkOccurrences(getTestPath(), "    const CONSTA^NT1 = \"CONSTANT1\";", true);
    }

    public void testEnumerations_f02() throws Exception {
        checkOccurrences(getTestPath(), "        Simple::CONSTA^NT1;", true);
    }

    public void testEnumerations_f03() throws Exception {
        checkOccurrences(getTestPath(), "Simple::CASE1::CONSTA^NT1;", true);
    }

    public void testEnumerations_g01() throws Exception {
        checkOccurrences(getTestPath(), "    const CONST^ANT2 = self::CASE2;", true);
    }

    public void testEnumerations_g02() throws Exception {
        checkOccurrences(getTestPath(), "        self::CONSTAN^T2;", true);
    }

    public void testEnumerations_g03() throws Exception {
        checkOccurrences(getTestPath(), "        static::CONST^ANT2;", true);
    }

    public void testEnumerationsWithBackingType_a01() throws Exception {
        checkOccurrences(getTestPath(), "    case C^ASE1 = 1;", true);
    }

    public void testEnumerationsWithBackingType_a02() throws Exception {
        checkOccurrences(getTestPath(), "            static::CAS^E1 => 'Case1',", true);
    }

    public void testEnumerationsWithBackingType_b01() throws Exception {
        checkOccurrences(getTestPath(), "    case CA^SE2 = 2;", true);
    }

    public void testEnumerationsWithBackingType_b02() throws Exception {
        checkOccurrences(getTestPath(), "    const CONSTANT2 = self::CAS^E2;", true);
    }

    public void testEnumerationsWithBackingType_b03() throws Exception {
        checkOccurrences(getTestPath(), "            static::CASE^2 => 'Case2',", true);
    }

    public void testEnumerationsWithBackingType_c01() throws Exception {
        checkOccurrences(getTestPath(), "    case CAS^E3 = 1 << 3;", true);
    }

    public void testEnumerationsWithBackingType_c02() throws Exception {
        checkOccurrences(getTestPath(), "            static::CAS^E3 => 'Case3',", true);
    }

    public void testFirstClassCallableSyntax_01a() throws Exception {
        checkOccurrences(getTestPath(), "function te^st($param1, $param2, $param3) {", true);
    }

    public void testFirstClassCallableSyntax_01b() throws Exception {
        checkOccurrences(getTestPath(), "tes^t(...); // test", true);
    }

    public void testFirstClassCallableSyntax_01c() throws Exception {
        checkOccurrences(getTestPath(), "$fn = t^est(...);", true);
    }

    public void testFirstClassCallableSyntax_02a() throws Exception {
        checkOccurrences(getTestPath(), "    public function public^Method(int $param): int {", true);
    }

    public void testFirstClassCallableSyntax_02b() throws Exception {
        checkOccurrences(getTestPath(), "$fn = $test->publicMetho^d(...);", true);
    }

    public void testFirstClassCallableSyntax_02c() throws Exception {
        checkOccurrences(getTestPath(), "$fn = (new Test)->publicM^ethod(...);", true);
    }

    public void testFirstClassCallableSyntax_03a() throws Exception {
        checkOccurrences(getTestPath(), "    public static function publicSta^ticMethod(): void {", true);
    }

    public void testFirstClassCallableSyntax_03b() throws Exception {
        checkOccurrences(getTestPath(), "$fn = $test->publicStat^icMethod(...);", true);
    }

    public void testFirstClassCallableSyntax_03c() throws Exception {
        checkOccurrences(getTestPath(), "$fn = $test::publicStatic^Method(...);", true);
    }

    public void testFirstClassCallableSyntax_03d() throws Exception {
        checkOccurrences(getTestPath(), "$fn = Test::publicS^taticMethod(...);", true);
    }

    public void testFirstClassCallableSyntax_04a() throws Exception {
        checkOccurrences(getTestPath(), "        $fn = $this->tes^t(...);", true);
    }

    public void testFirstClassCallableSyntax_04b() throws Exception {
        checkOccurrences(getTestPath(), "    private function tes^t(): void {", true);
    }

    public void testFirstClassCallableSyntax_05a() throws Exception {
        checkOccurrences(getTestPath(), "        $fn = self::testSta^tic(...);", true);
    }

    public void testFirstClassCallableSyntax_05b() throws Exception {
        checkOccurrences(getTestPath(), "    private static function testSt^atic(): void {", true);
    }

}
