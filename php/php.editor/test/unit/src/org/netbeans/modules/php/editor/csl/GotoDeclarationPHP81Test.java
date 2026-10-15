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

public class GotoDeclarationPHP81Test extends GotoDeclarationTestBase {

    public GotoDeclarationPHP81Test(String testName) {
        super(testName);
    }

    @Override
    protected String getBaseTestFolderPath() {
        return super.getBaseTestFolderPath() + "php81/";
    }

    public void testNewInInitializersWithStaticVariable_01() throws Exception {
        checkDeclaration(getTestPath(), "static $staticVariable = new StaticVari^able;", "class ^StaticVariable {}");
    }

    public void testNewInInitializersWithStaticVariable_02() throws Exception {
        checkDeclaration(getTestPath(), "static $staticVariable = new StaticVari^able();", "class ^StaticVariable {}");
    }

    public void testNewInInitializersWithStaticVariable_03() throws Exception {
        checkDeclaration(getTestPath(), "static $staticVariable = new StaticVariab^le(1);", "class ^StaticVariable {}");
    }

    public void testNewInInitializersWithStaticVariable_04() throws Exception {
        checkDeclaration(getTestPath(), "static $staticVariable = new Stati^cVariable(x: 1);", "class ^StaticVariable {}");
    }

    public void testNewInInitializersWithConstant_01() throws Exception {
        checkDeclaration(getTestPath(), "const CONSTANT = new Co^nstant;", "class ^Constant {}");
    }

    public void testNewInInitializersWithConstant_02() throws Exception {
        checkDeclaration(getTestPath(), "const CONSTANT = new Const^ant();", "class ^Constant {}");
    }

    public void testNewInInitializersWithConstant_03() throws Exception {
        checkDeclaration(getTestPath(), "const CONSTANT = new Const^ant(\"test\", \"constant\");", "class ^Constant {}");
    }

    public void testNewInInitializersWithConstant_04() throws Exception {
        checkDeclaration(getTestPath(), "const CONSTANT = new Constan^t(test: \"test\", constant: \"constant\");", "class ^Constant {}");
    }

    public void testNewInInitializersWithFunc_01() throws Exception {
        checkDeclaration(getTestPath(), "function func1($param = new F^unc) {}", "class ^Func {}");
    }

    public void testNewInInitializersWithFunc_02() throws Exception {
        checkDeclaration(getTestPath(), "function func2($param = new Fun^c()) {}", "class ^Func {}");
    }

    public void testNewInInitializersWithFunc_03() throws Exception {
        checkDeclaration(getTestPath(), "function func3($param = new Fu^nc(1)) {}", "class ^Func {}");
    }

    public void testNewInInitializersWithFunc_04() throws Exception {
        checkDeclaration(getTestPath(), "function func4($param = new Fu^nc(test: 1)) {}", "class ^Func {}");
    }

    public void testNewInInitializersWithMethod_01() throws Exception {
        checkDeclaration(getTestPath(), "public $prop1 = new M^ethod,", "class ^Method {}");
    }

    public void testNewInInitializersWithMethod_02() throws Exception {
        checkDeclaration(getTestPath(), "public $prop2 = new Me^thod(),", "class ^Method {}");
    }

    public void testNewInInitializersWithMethod_03() throws Exception {
        checkDeclaration(getTestPath(), "public $prop3 = new Meth^od(\"test\"),", "class ^Method {}");
    }

    public void testNewInInitializersWithMethod_04() throws Exception {
        checkDeclaration(getTestPath(), "public $prop4 = new Metho^d(test: \"test\"),", "class ^Method {}");
    }

    public void testNewInInitializersWithMethod_05() throws Exception {
        checkDeclaration(getTestPath(), "$param = new Me^thod,", "class ^Method {}");
    }

    public void testNewInInitializersWithMethod_06() throws Exception {
        checkDeclaration(getTestPath(), "$param = new Metho^d(),", "class ^Method {}");
    }

    public void testNewInInitializersWithMethod_07() throws Exception {
        checkDeclaration(getTestPath(), "$param = new M^ethod(\"test\"),", "class ^Method {}");
    }

    public void testNewInInitializersWithMethod_08() throws Exception {
        checkDeclaration(getTestPath(), "$param = new Met^hod(test: \"test\"),", "class ^Method {}");
    }

    public void testNewInInitializersWithAttribute_01() throws Exception {
        checkDeclaration(getTestPath(), "#[AnAttribute(new F^oo)]", "class ^Foo {}");
    }

    public void testNewInInitializersWithAttribute_02() throws Exception {
        checkDeclaration(getTestPath(), "#[AnAttribute(new F^oo())]", "class ^Foo {}");
    }

    public void testNewInInitializersWithAttribute_03() throws Exception {
        checkDeclaration(getTestPath(), "#[AnAttribute(new Fo^o(1))]", "class ^Foo {}");
    }

    public void testNewInInitializersWithAttribute_04() throws Exception {
        checkDeclaration(getTestPath(), "#[AnAttribute(new Fo^o(x: 1))]", "class ^Foo {}");
    }

    public void testPureIntersectionType_01() throws Exception {
        checkDeclaration(getTestPath(), "function paramType(Fo^o&Bar $test): void {", "class ^Foo {}");
    }

    public void testPureIntersectionType_02() throws Exception {
        checkDeclaration(getTestPath(), "function returnType(): F^oo&Bar {", "class ^Foo {}");
    }

    public void testPureIntersectionType_03() throws Exception {
        checkDeclaration(getTestPath(), "private Fo^o&Bar $test; // class", "class ^Foo {}");
    }

    public void testPureIntersectionType_04() throws Exception {
        checkDeclaration(getTestPath(), "public function paramType(Fo^o&Bar $test): void { // class", "class ^Foo {}");
    }

    public void testPureIntersectionType_05() throws Exception {
        checkDeclaration(getTestPath(), "public function returnType(): Fo^o&Bar { // class", "class ^Foo {}");
    }

    public void testPureIntersectionType_06() throws Exception {
        checkDeclaration(getTestPath(), "private Fo^o&Bar $test; // trait", "class ^Foo {}");
    }

    public void testPureIntersectionType_07() throws Exception {
        checkDeclaration(getTestPath(), "public function paramType(F^oo&Bar $test1, Foo&Bar $test2): void { // trait", "class ^Foo {}");
    }

    public void testPureIntersectionType_08() throws Exception {
        checkDeclaration(getTestPath(), "public function paramType(Foo&Bar $test1, Fo^o&Bar $test2): void { // trait", "class ^Foo {}");
    }

    public void testPureIntersectionType_09() throws Exception {
        checkDeclaration(getTestPath(), "public function returnType(): F^oo&Bar { // trait", "class ^Foo {}");
    }

    public void testPureIntersectionType_10() throws Exception {
        checkDeclaration(getTestPath(), "public function paramType(Fo^o&Bar $test);", "class ^Foo {}");
    }

    public void testPureIntersectionType_11() throws Exception {
        checkDeclaration(getTestPath(), "public function returnType(): F^oo&Bar;", "class ^Foo {}");
    }

    public void testPureIntersectionType_12() throws Exception {
        checkDeclaration(getTestPath(), "$closure = function(Fo^o&Bar $test1, $test2): void {};", "class ^Foo {}");
    }

    public void testPureIntersectionType_13() throws Exception {
        checkDeclaration(getTestPath(), "$closure = function(int $test): F^oo&Bar {};", "class ^Foo {}");
    }

    public void testPureIntersectionType_14() throws Exception {
        checkDeclaration(getTestPath(), "$arrow = fn(Fo^o&Bar $test) => $test;", "class ^Foo {}");
    }

    public void testPureIntersectionType_15() throws Exception {
        checkDeclaration(getTestPath(), "$arrow = fn(F^oo&Bar $test): Foo&Bar => $test;", "class ^Foo {}");
    }

    public void testPureIntersectionType_16() throws Exception {
        checkDeclaration(getTestPath(), "$arrow = fn(Foo&Bar $test): Fo^o&Bar => $test;", "class ^Foo {}");
    }

    public void testPureIntersectionType_17() throws Exception {
        checkDeclaration(getTestPath(), "function paramType(Foo&Ba^r $test): void {", "class ^Bar {}");
    }

    public void testPureIntersectionType_18() throws Exception {
        checkDeclaration(getTestPath(), "function returnType(): Foo&B^ar {", "class ^Bar {}");
    }

    public void testPureIntersectionType_19() throws Exception {
        checkDeclaration(getTestPath(), "private Foo&Ba^r $test; // class", "class ^Bar {}");
    }

    public void testPureIntersectionType_20() throws Exception {
        checkDeclaration(getTestPath(), "public function paramType(Foo&B^ar $test): void { // class", "class ^Bar {}");
    }

    public void testPureIntersectionType_21() throws Exception {
        checkDeclaration(getTestPath(), "public function returnType(): Foo&Ba^r { // class", "class ^Bar {}");
    }

    public void testPureIntersectionType_22() throws Exception {
        checkDeclaration(getTestPath(), "private Foo&B^ar $test; // trait", "class ^Bar {}");
    }

    public void testPureIntersectionType_23() throws Exception {
        checkDeclaration(getTestPath(), "public function paramType(Foo&B^ar $test1, Foo&Bar $test2): void { // trait", "class ^Bar {}");
    }

    public void testPureIntersectionType_24() throws Exception {
        checkDeclaration(getTestPath(), "public function paramType(Foo&Bar $test1, Foo&Ba^r $test2): void { // trait", "class ^Bar {}");
    }

    public void testPureIntersectionType_25() throws Exception {
        checkDeclaration(getTestPath(), "public function returnType(): Foo&B^ar { // trait", "class ^Bar {}");
    }

    public void testPureIntersectionType_26() throws Exception {
        checkDeclaration(getTestPath(), "public function paramType(Foo&Ba^r $test);", "class ^Bar {}");
    }

    public void testPureIntersectionType_27() throws Exception {
        checkDeclaration(getTestPath(), "public function returnType(): Foo&Ba^r;", "class ^Bar {}");
    }

    public void testPureIntersectionType_28() throws Exception {
        checkDeclaration(getTestPath(), "$closure = function(Foo&Ba^r $test1, $test2): void {};", "class ^Bar {}");
    }

    public void testPureIntersectionType_29() throws Exception {
        checkDeclaration(getTestPath(), "$closure = function(int $test): Foo&B^ar {};", "class ^Bar {}");
    }

    public void testPureIntersectionType_30() throws Exception {
        checkDeclaration(getTestPath(), "$arrow = fn(Foo&B^ar $test) => $test;", "class ^Bar {}");
    }

    public void testPureIntersectionType_31() throws Exception {
        checkDeclaration(getTestPath(), "$arrow = fn(Foo&B^ar $test): Foo&Bar => $test;", "class ^Bar {}");
    }

    public void testPureIntersectionType_32() throws Exception {
        checkDeclaration(getTestPath(), "$arrow = fn(Foo&Bar $test): Foo&B^ar => $test;", "class ^Bar {}");
    }

    public void testEnumerations_01() throws Exception {
        checkDeclaration(getTestPath(), "        Simp^le::CASE2;", "enum ^Simple {");
    }

    public void testEnumerations_02() throws Exception {
        checkDeclaration(getTestPath(), "        Simp^le::CONSTANT1;", "enum ^Simple {");
    }

    public void testEnumerations_03() throws Exception {
        checkDeclaration(getTestPath(), "        Simp^le::publicStaticMethod();", "enum ^Simple {");
    }

    public void testEnumerations_04() throws Exception {
        checkDeclaration(getTestPath(), "        S^imple::CASE1->publicMethod();", "enum ^Simple {");
    }

    public void testEnumerations_05() throws Exception {
        checkDeclaration(getTestPath(), "        Sim^ple::CASE1::publicStaticMethod();", "enum ^Simple {");
    }

    public void testEnumerations_06() throws Exception {
        checkDeclaration(getTestPath(), "use Enum1\\S^imple;", "enum ^Simple {");
    }

    public void testEnumerations_07() throws Exception {
        checkDeclaration(getTestPath(), "Simp^le::CASE1::CONSTANT1;", "enum ^Simple {");
    }

    public void testEnumerations_08() throws Exception {
        checkDeclaration(getTestPath(), "Sim^ple::CASE1::CASE2;", "enum ^Simple {");
    }

    public void testEnumerations_09() throws Exception {
        checkDeclaration(getTestPath(), "S^imple::CASE2->publicMethod();", "enum ^Simple {");
    }

    public void testEnumerations_10() throws Exception {
        checkDeclaration(getTestPath(), "Sim^ple::CASE2::publicStaticMethod();", "enum ^Simple {");
    }

    public void testEnumerations_11() throws Exception {
        checkDeclaration(getTestPath(), "Sim^ple::publicStaticMethod(); // 2", "enum ^Simple {");
    }

    public void testEnumerations_12() throws Exception {
        checkDeclaration(getTestPath(), "Simp^le::staticTest();", "enum ^Simple {");
    }

    public void testEnumerations_13() throws Exception {
        checkDeclaration(getTestPath(), "$i = Sim^ple::CASE2;", "enum ^Simple {");
    }

    public void testEnumerations_14() throws Exception {
        checkDeclaration(getTestPath(), "            static::CA^SE1 => 'Case1',", "    case ^CASE1;");
    }

    public void testEnumerations_15() throws Exception {
        checkDeclaration(getTestPath(), "        Simple::CAS^E1->publicMethod();", "    case ^CASE1;");
    }

    public void testEnumerations_16() throws Exception {
        checkDeclaration(getTestPath(), "        Simple::C^ASE1::publicStaticMethod();", "    case ^CASE1;");
    }

    public void testEnumerations_17() throws Exception {
        checkDeclaration(getTestPath(), "        self::CAS^E1;", "    case ^CASE1;");
    }

    public void testEnumerations_18() throws Exception {
        checkDeclaration(getTestPath(), "        self::CASE^1->publicMethod();", "    case ^CASE1;");
    }

    public void testEnumerations_19() throws Exception {
        checkDeclaration(getTestPath(), "        static::CAS^E1;", "    case ^CASE1;");
    }

    public void testEnumerations_20() throws Exception {
        checkDeclaration(getTestPath(), "        static::CAS^E1->publicMethod();", "    case ^CASE1;");
    }

    public void testEnumerations_21() throws Exception {
        checkDeclaration(getTestPath(), "Simple::CAS^E1::CONSTANT1;", "    case ^CASE1;");
    }

    public void testEnumerations_22() throws Exception {
        checkDeclaration(getTestPath(), "Simple::CA^SE1::CASE2;", "    case ^CASE1;");
    }

    public void testEnumerations_23() throws Exception {
        checkDeclaration(getTestPath(), "$i::CAS^E1;", "    case ^CASE1;");
    }

    public void testEnumerations_24() throws Exception {
        checkDeclaration(getTestPath(), "    const CONSTANT2 = self::CAS^E2;", "    case ^CASE2;");
    }

    public void testEnumerations_25() throws Exception {
        checkDeclaration(getTestPath(), "            static::CA^SE2 => 'Case2',", "    case ^CASE2;");
    }

    public void testEnumerations_26() throws Exception {
        checkDeclaration(getTestPath(), "        Simple::CAS^E2;", "    case ^CASE2;");
    }

    public void testEnumerations_27() throws Exception {
        checkDeclaration(getTestPath(), "Simple::CASE1::CAS^E2;", "    case ^CASE2;");
    }

    public void testEnumerations_28() throws Exception {
        checkDeclaration(getTestPath(), "Simple::C^ASE2->publicMethod();", "    case ^CASE2;");
    }

    public void testEnumerations_29() throws Exception {
        checkDeclaration(getTestPath(), "Simple::CASE^2::publicStaticMethod();", "    case ^CASE2;");
    }

    public void testEnumerations_30() throws Exception {
        checkDeclaration(getTestPath(), "$i = Simple::CA^SE2;", "    case ^CASE2;");
    }

    public void testEnumerations_31() throws Exception {
        checkDeclaration(getTestPath(), "        Simple::CONST^ANT1;", "    const ^CONSTANT1 = \"CONSTANT1\";");
    }

    public void testEnumerations_32() throws Exception {
        checkDeclaration(getTestPath(), "Simple::CASE1::CONSTAN^T1;", "    const ^CONSTANT1 = \"CONSTANT1\";");
    }

    public void testEnumerations_33() throws Exception {
        checkDeclaration(getTestPath(), "        self::CONSTAN^T2;", "    const ^CONSTANT2 = self::CASE2;");
    }

    public void testEnumerations_34 () throws Exception {
        checkDeclaration(getTestPath(), "        static::CO^NSTANT2;", "    const ^CONSTANT2 = self::CASE2;");
    }

    public void testEnumerations_35() throws Exception {
        checkDeclaration(getTestPath(), "        Simple::CASE1->publ^icMethod();", "    public function ^publicMethod(): void {");
    }

    public void testEnumerations_36() throws Exception {
        checkDeclaration(getTestPath(), "        self::CASE1->publicMe^thod();", "    public function ^publicMethod(): void {");
    }

    public void testEnumerations_37() throws Exception {
        checkDeclaration(getTestPath(), "        static::CASE1->publicMe^thod();", "    public function ^publicMethod(): void {");
    }

    public void testEnumerations_38() throws Exception {
        checkDeclaration(getTestPath(), "Simple::CASE2->pu^blicMethod();", "    public function ^publicMethod(): void {");
    }

    public void testEnumerations_39() throws Exception {
        checkDeclaration(getTestPath(), "$i->publicMeth^od();", "    public function ^publicMethod(): void {");
    }

    public void testEnumerations_40() throws Exception {
        checkDeclaration(getTestPath(), "        Simple::publicStat^icMethod();", "    public static function ^publicStaticMethod(): void {");
    }

    public void testEnumerations_41() throws Exception {
        checkDeclaration(getTestPath(), "        Simple::CASE1::publicStaticMet^hod();", "    public static function ^publicStaticMethod(): void {");
    }

    public void testEnumerations_42() throws Exception {
        checkDeclaration(getTestPath(), "        self::publicStaticMeth^od();", "    public static function ^publicStaticMethod(): void {");
    }

    public void testEnumerations_43() throws Exception {
        checkDeclaration(getTestPath(), "        static::publicS^taticMethod();", "    public static function ^publicStaticMethod(): void {");
    }

    public void testEnumerations_44() throws Exception {
        checkDeclaration(getTestPath(), "Simple::CASE2::pub^licStaticMethod();", "    public static function ^publicStaticMethod(): void {");
    }

    public void testEnumerations_45() throws Exception {
        checkDeclaration(getTestPath(), "Simple::publicStaticMet^hod(); // 2", "    public static function ^publicStaticMethod(): void {");
    }

    public void testEnumerations_46() throws Exception {
        checkDeclaration(getTestPath(), "$i::publicSt^aticMethod();", "    public static function ^publicStaticMethod(): void {");
    }

    public void testEnumerationsWithInterface_01a() throws Exception {
        checkDeclaration(getTestPath(), "use Enum1\\ExampleInter^face1;", "interface ^ExampleInterface1 {}");
    }

    public void testEnumerationsWithInterface_01b() throws Exception {
        checkDeclaration(getTestPath(), "enum EnumImpl1 implements ExampleInterf^ace1 {", "interface ^ExampleInterface1 {}");
    }

    public void testEnumerationsWithInterface_01c() throws Exception {
        checkDeclaration(getTestPath(), "enum EnumImpl2 implements ExampleInterfa^ce1, ExampleInterface2 {", "interface ^ExampleInterface1 {}");
    }

    public void testEnumerationsWithInterface_02a() throws Exception {
        checkDeclaration(getTestPath(), "use Enum1\\ExampleInter^face2;", "interface ^ExampleInterface2 {}");
    }

    public void testEnumerationsWithInterface_02b() throws Exception {
        checkDeclaration(getTestPath(), "enum EnumImpl2 implements ExampleInterface1, ExampleInterfa^ce2 {", "interface ^ExampleInterface2 {}");
    }

    public void testEnumerationsWithBackingType_01() throws Exception {
        checkDeclaration(getTestPath(), "            static::CASE^1 => 'Case1',", "    case ^CASE1 = 1;");
    }

    public void testEnumerationsWithBackingType_02() throws Exception {
        checkDeclaration(getTestPath(), "    const CONSTANT2 = self::CAS^E2;", "    case ^CASE2 = 2;");
    }

    public void testEnumerationsWithBackingType_03() throws Exception {
        checkDeclaration(getTestPath(), "            static::CASE^2 => 'Case2',", "    case ^CASE2 = 2;");
    }

    public void testEnumerationsWithBackingType_04() throws Exception {
        checkDeclaration(getTestPath(), "            static::CAS^E3 => 'Case3',", "    case ^CASE3 = 1 << 3;");
    }

    public void testFirstClassCallableSyntax_01a() throws Exception {
        checkDeclaration(getTestPath(), "t^est(...); // test", "function ^test($param1, $param2, $param3) {");
    }

    public void testFirstClassCallableSyntax_01b() throws Exception {
        checkDeclaration(getTestPath(), "$fn = tes^t(...);", "function ^test($param1, $param2, $param3) {");
    }

    public void testFirstClassCallableSyntax_02() throws Exception {
        checkDeclaration(getTestPath(), "        $fn = $this->te^st(...);", "    private function ^test(): void {");
    }

    public void testFirstClassCallableSyntax_03() throws Exception {
        checkDeclaration(getTestPath(), "        $fn = self::testStat^ic(...);", "    private static function ^testStatic(): void {");
    }

    public void testFirstClassCallableSyntax_04a() throws Exception {
        checkDeclaration(getTestPath(), "$fn = $test->publicMetho^d(...);", "    public function ^publicMethod(int $param): int {");
    }

    public void testFirstClassCallableSyntax_4b() throws Exception {
        checkDeclaration(getTestPath(), "$fn = (new Test)->publicMet^hod(...);", "    public function ^publicMethod(int $param): int {");
    }

    public void testFirstClassCallableSyntax_05a() throws Exception {
        checkDeclaration(getTestPath(), "$fn = $test->publicStati^cMethod(...);", "    public static function ^publicStaticMethod(): void {");
    }

    public void testFirstClassCallableSyntax_05b() throws Exception {
        checkDeclaration(getTestPath(), "$fn = $test::publicStatic^Method(...);", "    public static function ^publicStaticMethod(): void {");
    }

    public void testFirstClassCallableSyntax_05c() throws Exception {
        checkDeclaration(getTestPath(), "$fn = Test::publi^cStaticMethod(...);", "    public static function ^publicStaticMethod(): void {");
    }

}
