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

}
