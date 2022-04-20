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

}
