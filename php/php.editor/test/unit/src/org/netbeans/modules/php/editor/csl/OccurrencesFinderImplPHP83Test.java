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

public class OccurrencesFinderImplPHP83Test extends OccurrencesFinderImplTestBase {

    public OccurrencesFinderImplPHP83Test(String testName) {
        super(testName);
    }

    @Override
    protected String getBaseTestFolderPath() {
        return super.getBaseTestFolderPath() + "php83/";
    }

    public void testDynamicClassConstantFetch_01a() throws Exception {
        checkOccurrences(getTestPath(), "class Te^st {", true);
    }

    public void testDynamicClassConstantFetch_01b() throws Exception {
        checkOccurrences(getTestPath(), "Te^st::{\"TEST\"};", true);
    }

    public void testDynamicClassConstantFetch_01c() throws Exception {
        checkOccurrences(getTestPath(), "T^est::{$variable};", true);
    }

    public void testDynamicClassConstantFetch_01d() throws Exception {
        checkOccurrences(getTestPath(), "Tes^t::{$variable . $e};", true);
    }

    public void testDynamicClassConstantFetch_01e() throws Exception {
        checkOccurrences(getTestPath(), "Tes^t::{strtoupper(\"test\")};", true);
    }

    public void testDynamicClassConstantFetch_01f() throws Exception {
        checkOccurrences(getTestPath(), "^Test::{'$variablee'};", true);
    }

    public void testDynamicClassConstantFetch_01g() throws Exception {
        checkOccurrences(getTestPath(), "T^est::{strtolower(\"CLASS\")};", true);
    }

    public void testDynamicClassConstantFetch_01h() throws Exception {
        checkOccurrences(getTestPath(), "Tes^t::{1000};", true);
    }

    public void testDynamicClassConstantFetch_01i() throws Exception {
        checkOccurrences(getTestPath(), "Tes^t::{Test::method()}::{test($variable)};", true);
    }

    public void testDynamicClassConstantFetch_01j() throws Exception {
        checkOccurrences(getTestPath(), "Test::{T^est::method()}::{test($variable)};", true);
    }

    public void testDynamicClassConstantFetch_01k() throws Exception {
        checkOccurrences(getTestPath(), "Te^st::{test('test1')}::{test('test2')};", true);
    }

    public void testDynamicClassConstantFetch_01l() throws Exception {
        checkOccurrences(getTestPath(), "$test = new Te^st();", true);
    }

    public void testDynamicClassConstantFetch_02a() throws Exception {
        checkOccurrences(getTestPath(), "enum EnumT^est: string {", true);
    }

    public void testDynamicClassConstantFetch_02b() throws Exception {
        checkOccurrences(getTestPath(), "EnumTe^st::{[]};", true);
    }

    public void testDynamicClassConstantFetch_03a() throws Exception {
        checkOccurrences(getTestPath(), "    public const TE^S = 'TES';", true);
    }

    public void testDynamicClassConstantFetch_03b() throws Exception {
        checkOccurrences(getTestPath(), "    public const TEST3 = self::{self::T^ES . self::T};", true);
    }

    public void testDynamicClassConstantFetch_04a() throws Exception {
        checkOccurrences(getTestPath(), "    public const ^T = 'T';", true);
    }

    public void testDynamicClassConstantFetch_04b() throws Exception {
        checkOccurrences(getTestPath(), "    public const TEST3 = self::{self::TES . self::^T};", true);
    }

    public void testDynamicClassConstantFetch_05a() throws Exception {
        checkOccurrences(getTestPath(), "    public static function metho^d(): void {}", true);
    }

    public void testDynamicClassConstantFetch_05b() throws Exception {
        checkOccurrences(getTestPath(), "Test::{Test::met^hod()}::{test($variable)};", true);
    }

    public void testDynamicClassConstantFetch_06a() throws Exception {
        checkOccurrences(getTestPath(), "    case TE^S = 'TES';", true);
    }

    public void testDynamicClassConstantFetch_06b() throws Exception {
        checkOccurrences(getTestPath(), "    case TEST3 = self::{self::TE^S . self::T};", true);
    }

    public void testDynamicClassConstantFetch_07a() throws Exception {
        checkOccurrences(getTestPath(), "    case ^T = 'T';", true);
    }

    public void testDynamicClassConstantFetch_07b() throws Exception {
        checkOccurrences(getTestPath(), "    case TEST3 = self::{self::TES . self::^T};", true);
    }

    public void testDynamicClassConstantFetch_08a() throws Exception {
        checkOccurrences(getTestPath(), "function t^est(string $param = \"test\"): void {", true);
    }

    public void testDynamicClassConstantFetch_08b() throws Exception {
        checkOccurrences(getTestPath(), "Test::{Test::method()}::{tes^t($variable)};", true);
    }

    public void testDynamicClassConstantFetch_08c() throws Exception {
        checkOccurrences(getTestPath(), "Test::{te^st('test1')}::{test('test2')};", true);
    }

    public void testDynamicClassConstantFetch_08d() throws Exception {
        checkOccurrences(getTestPath(), "Test::{test('test1')}::{te^st('test2')};", true);
    }

    public void testDynamicClassConstantFetch_09a() throws Exception {
        checkOccurrences(getTestPath(), "$vari^able = 'TEST';", true);
    }

    public void testDynamicClassConstantFetch_09b() throws Exception {
        checkOccurrences(getTestPath(), "Test::{$var^iable};", true);
    }

    public void testDynamicClassConstantFetch_09c() throws Exception {
        checkOccurrences(getTestPath(), "$test::{$v^ariable};", true);
    }

    public void testDynamicClassConstantFetch_09d() throws Exception {
        checkOccurrences(getTestPath(), "Test::{$vari^able . $e};", true);
    }

    public void testDynamicClassConstantFetch_09e() throws Exception {
        checkOccurrences(getTestPath(), "$test::{$varia^ble . $e};", true);
    }

    public void testDynamicClassConstantFetch_09f() throws Exception {
        checkOccurrences(getTestPath(), "Test::{Test::method()}::{test($variab^le)};", true);
    }

    public void testDynamicClassConstantFetch_10a() throws Exception {
        checkOccurrences(getTestPath(), "$^e = 'e';", true);
    }

    public void testDynamicClassConstantFetch_10b() throws Exception {
        checkOccurrences(getTestPath(), "Test::{$variable . $^e};", true);
    }

    public void testDynamicClassConstantFetch_10c() throws Exception {
        checkOccurrences(getTestPath(), "$test::{$variable . $^e};", true);
    }

    public void testDynamicClassConstantFetch_11a() throws Exception {
        checkOccurrences(getTestPath(), "$t^est = \"Test\";", true);
    }

    public void testDynamicClassConstantFetch_11b() throws Exception {
        checkOccurrences(getTestPath(), "$t^est::{\"TEST\"};", true);
    }

    public void testDynamicClassConstantFetch_11c() throws Exception {
        checkOccurrences(getTestPath(), "$t^est::{$variable};", true);
    }

    public void testDynamicClassConstantFetch_11d() throws Exception {
        checkOccurrences(getTestPath(), "$tes^t::{$variable . $e};", true);
    }

    public void testDynamicClassConstantFetch_11e() throws Exception {
        checkOccurrences(getTestPath(), "$te^st::{strtoupper(\"test\")};", true);
    }

    public void testDynamicClassConstantFetch_11f() throws Exception {
        checkOccurrences(getTestPath(), "$t^est::{'$variablee'};", true);
    }

    public void testDynamicClassConstantFetch_11g() throws Exception {
        checkOccurrences(getTestPath(), "$t^est::{strtolower(\"CLASS\")};", true);
    }

    public void testDynamicClassConstantFetch_11h() throws Exception {
        checkOccurrences(getTestPath(), "$te^st::{1000};", true);
    }

    public void testDynamicClassConstantFetch_11i() throws Exception {
        checkOccurrences(getTestPath(), "$tes^t::{[]};", true);
    }

    public void testDynamicClassConstantFetch_11j() throws Exception {
        checkOccurrences(getTestPath(), "$tes^t = new Test();", true);
    }

    public void testTypedClassConstants_01a() throws Exception {
        checkOccurrences(getTestPath(), "class ^A implements Stringable {", true);
    }

    public void testTypedClassConstants_01b() throws Exception {
        checkOccurrences(getTestPath(), "class B extends ^A {}", true);
    }

    public void testTypedClassConstants_01c() throws Exception {
        checkOccurrences(getTestPath(), "class C extends ^A {}", true);
    }

    public void testTypedClassConstants_01d() throws Exception {
        checkOccurrences(getTestPath(), "    public const ?^A NULLABLE = null;", true);
    }

    public void testTypedClassConstants_01e() throws Exception {
        checkOccurrences(getTestPath(), "    private const ^A|B UNION = A;", true);
    }

    public void testTypedClassConstants_01f() throws Exception {
        checkOccurrences(getTestPath(), "    protected const ^A&B INTERSECTION = B;", true);
    }

    public void testTypedClassConstants_01g() throws Exception {
        checkOccurrences(getTestPath(), "    public const (^A&B)|C DNF = C;", true);
    }

    public void testTypedClassConstants_01h() throws Exception {
        checkOccurrences(getTestPath(), "    public const ^A|B UNION = A; // interface", true);
    }

    public void testTypedClassConstants_01i() throws Exception {
        checkOccurrences(getTestPath(), "    public const ^A&B INTERSECTION = B; // interface", true);
    }

    public void testTypedClassConstants_01j() throws Exception {
        checkOccurrences(getTestPath(), "    public const (^A&B)|C DNF = C; // interface", true);
    }

    public void testTypedClassConstants_01k() throws Exception {
        checkOccurrences(getTestPath(), "    private const ^A|B UNION = A; // trait", true);
    }

    public void testTypedClassConstants_01l() throws Exception {
        checkOccurrences(getTestPath(), "    protected const ^A&B INTERSECTION = B; // trait", true);
    }

    public void testTypedClassConstants_01m() throws Exception {
        checkOccurrences(getTestPath(), "    public const (^A&B)|C DNF = C; // trait", true);
    }

    public void testTypedClassConstants_01n() throws Exception {
        checkOccurrences(getTestPath(), "    private const ^A|B UNION = A; // enum", true);
    }

    public void testTypedClassConstants_01o() throws Exception {
        checkOccurrences(getTestPath(), "    protected const ^A&B INTERSECTION = B; // enum", true);
    }

    public void testTypedClassConstants_01p() throws Exception {
        checkOccurrences(getTestPath(), "    public const (^A&B)|(A&C) DNF = C; // enum", true);
    }

    public void testTypedClassConstants_01q() throws Exception {
        checkOccurrences(getTestPath(), "    public const (A&B)|(^A&C) DNF = C; // enum", true);
    }

    public void testTypedClassConstants_01r() throws Exception {
        checkOccurrences(getTestPath(), "define(\"A\", new ^A());", true);
    }

    public void testTypedClassConstants_02a() throws Exception {
        checkOccurrences(getTestPath(), "class ^B extends A {}", true);
    }

    public void testTypedClassConstants_02b() throws Exception {
        checkOccurrences(getTestPath(), "    private const A|^B UNION = A;", true);
    }

    public void testTypedClassConstants_02c() throws Exception {
        checkOccurrences(getTestPath(), "    protected const A&^B INTERSECTION = B;", true);
    }

    public void testTypedClassConstants_02d() throws Exception {
        checkOccurrences(getTestPath(), "    public const (A&^B)|C DNF = C;", true);
    }

    public void testTypedClassConstants_02e() throws Exception {
        checkOccurrences(getTestPath(), "    public const A|^B UNION = A; // interface", true);
    }

    public void testTypedClassConstants_02f() throws Exception {
        checkOccurrences(getTestPath(), "    public const A&^B INTERSECTION = B; // interface", true);
    }

    public void testTypedClassConstants_02g() throws Exception {
        checkOccurrences(getTestPath(), "    public const (A&^B)|C DNF = C; // interface", true);
    }

    public void testTypedClassConstants_02h() throws Exception {
        checkOccurrences(getTestPath(), "    private const A|^B UNION = A; // trait", true);
    }

    public void testTypedClassConstants_02i() throws Exception {
        checkOccurrences(getTestPath(), "    protected const A&^B INTERSECTION = B; // trait", true);
    }

    public void testTypedClassConstants_02j() throws Exception {
        checkOccurrences(getTestPath(), "    public const (A&^B)|C DNF = C; // trait", true);
    }

    public void testTypedClassConstants_02k() throws Exception {
        checkOccurrences(getTestPath(), "    private const A|^B UNION = A; // enum", true);
    }

    public void testTypedClassConstants_02l() throws Exception {
        checkOccurrences(getTestPath(), "    protected const A&^B INTERSECTION = B; // enum", true);
    }

    public void testTypedClassConstants_02m() throws Exception {
        checkOccurrences(getTestPath(), "    public const (A&^B)|(A&C) DNF = C; // enum", true);
    }

    public void testTypedClassConstants_02n() throws Exception {
        checkOccurrences(getTestPath(), "define(\"B\", new ^B());", true);
    }

    public void testTypedClassConstants_03a() throws Exception {
        checkOccurrences(getTestPath(), "class ^C extends A {}", true);
    }

    public void testTypedClassConstants_03b() throws Exception {
        checkOccurrences(getTestPath(), "    public const (A&B)|^C DNF = C;", true);
    }

    public void testTypedClassConstants_03c() throws Exception {
        checkOccurrences(getTestPath(), "    public const (A&B)|^C DNF = C; // interface", true);
    }

    public void testTypedClassConstants_03d() throws Exception {
        checkOccurrences(getTestPath(), "    public const (A&B)|^C DNF = C; // trait", true);
    }

    public void testTypedClassConstants_03e() throws Exception {
        checkOccurrences(getTestPath(), "    public const (A&B)|(A&^C) DNF = C; // enum", true);
    }

    public void testTypedClassConstants_03f() throws Exception {
        checkOccurrences(getTestPath(), "define(\"C\", new ^C());", true);
    }

    public void testTypedClassConstants_04a() throws Exception {
        checkOccurrences(getTestPath(), "class Class^Test {", true);
    }

    public void testTypedClassConstants_04b() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(Class^Test::WITHOUT_TYPE);", true);
    }

    public void testTypedClassConstants_04c() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(Clas^sTest::NULLABLE);", true);
    }

    public void testTypedClassConstants_04d() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(Cla^ssTest::UNION);", true);
    }

    public void testTypedClassConstants_04e() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(Class^Test::INTERSECTION);", true);
    }

    public void testTypedClassConstants_04f() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(ClassTes^t::DNF);", true);
    }

    public void testTypedClassConstants_04g() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(C^lassTest::STRING);", true);
    }

    public void testTypedClassConstants_04h() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(Class^Test::INT);", true);
    }

    public void testTypedClassConstants_04i() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(Clas^sTest::FLOAT);", true);
    }

    public void testTypedClassConstants_04j() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(Cla^ssTest::BOOL);", true);
    }

    public void testTypedClassConstants_04k() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(Cla^ssTest::ARRAY);", true);
    }

    public void testTypedClassConstants_04l() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(Cla^ssTest::ITERABLE);", true);
    }

    public void testTypedClassConstants_04m() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(Clas^sTest::MIXED);", true);
    }

    public void testTypedClassConstants_04n() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(Clas^sTest::OBJECT);", true);
    }

    public void testTypedClassConstants_04o() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(Cla^ssTest::UNION2);", true);
    }

    public void testTypedClassConstants_04p() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(ClassTe^st::UNION3);", true);
    }

    public void testTypedClassConstants_05a() throws Exception {
        checkOccurrences(getTestPath(), "    public const string|array UNION2 = 'a' . Inter^faceTest::STRING;", true);
    }

    public void testTypedClassConstants_05b() throws Exception {
        checkOccurrences(getTestPath(), "interface Interf^aceTest {", true);
    }

    public void testTypedClassConstants_06a() throws Exception {
        checkOccurrences(getTestPath(), "enum EnumT^est {", true);
    }

    public void testTypedClassConstants_06b() throws Exception {
        checkOccurrences(getTestPath(), "    public const static A = Enum^Test::Test; // enum", true);
    }

    public void testTypedClassConstants_07a() throws Exception {
        checkOccurrences(getTestPath(), "    public const WITHOUT^_TYPE = 1;", true);
    }

    public void testTypedClassConstants_07b() throws Exception {
        checkOccurrences(getTestPath(), "    public const mixed MIXED = 1 + self::WITHOUT_^TYPE;", true);
    }

    public void testTypedClassConstants_07c() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(ClassTest::WITH^OUT_TYPE);", true);
    }

    public void testTypedClassConstants_08a() throws Exception {
        checkOccurrences(getTestPath(), "    public const ?A NULLA^BLE = null;", true);
    }

    public void testTypedClassConstants_08b() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(ClassTest::NUL^LABLE);", true);
    }

    public void testTypedClassConstants_09a() throws Exception {
        checkOccurrences(getTestPath(), "    private const A|B UNI^ON = A;", true);
    }

    public void testTypedClassConstants_09b() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(ClassTest::UN^ION);", true);
    }

    public void testTypedClassConstants_10a() throws Exception {
        checkOccurrences(getTestPath(), "    protected const A&B INTER^SECTION = B;", true);
    }

    public void testTypedClassConstants_10b() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(ClassTest::INTE^RSECTION);", true);
    }

    public void testTypedClassConstants_11a() throws Exception {
        checkOccurrences(getTestPath(), "    public const (A&B)|C D^NF = C;", true);
    }

    public void testTypedClassConstants_11b() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(ClassTest::D^NF);", true);
    }

    public void testTypedClassConstants_12a() throws Exception {
        checkOccurrences(getTestPath(), "    public const string STR^ING = 'a';", true);
    }

    public void testTypedClassConstants_12b() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(ClassTest::ST^RING);", true);
    }

    public void testTypedClassConstants_13a() throws Exception {
        checkOccurrences(getTestPath(), "    public const int I^NT = 1;", true);
    }

    public void testTypedClassConstants_13b() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(ClassTest::I^NT);", true);
    }

    public void testTypedClassConstants_14a() throws Exception {
        checkOccurrences(getTestPath(), "    public const float FL^OAT = 1.5;", true);
    }

    public void testTypedClassConstants_14b() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(ClassTest::FL^OAT);", true);
    }

    public void testTypedClassConstants_15a() throws Exception {
        checkOccurrences(getTestPath(), "    public const bool B^OOL = true;", true);
    }

    public void testTypedClassConstants_15b() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(ClassTest::B^OOL);", true);
    }

    public void testTypedClassConstants_16a() throws Exception {
        checkOccurrences(getTestPath(), "    public const array A^RRAY = ['t', 'e', 's', 't'];", true);
    }

    public void testTypedClassConstants_16b() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(ClassTest::AR^RAY);", true);
    }

    public void testTypedClassConstants_17a() throws Exception {
        checkOccurrences(getTestPath(), "    public const iterable ITER^ABLE = ['a', 'b', 'c'];", true);
    }

    public void testTypedClassConstants_17b() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(ClassTest::ITE^RABLE);", true);
    }

    public void testTypedClassConstants_18a() throws Exception {
        checkOccurrences(getTestPath(), "    public const mixed MIX^ED = 1 + self::WITHOUT_TYPE;", true);
    }

    public void testTypedClassConstants_18b() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(ClassTest::MI^XED);", true);
    }

    public void testTypedClassConstants_19a() throws Exception {
        checkOccurrences(getTestPath(), "    public const object OB^JECT = A;", true);
    }

    public void testTypedClassConstants_19b() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(ClassTest::OBJ^ECT);", true);
    }

    public void testTypedClassConstants_20a() throws Exception {
        checkOccurrences(getTestPath(), "    public const string|array UNI^ON2 = 'a' . InterfaceTest::STRING;", true);
    }

    public void testTypedClassConstants_20b() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(ClassTest::UNIO^N2);", true);
    }

    public void testTypedClassConstants_21a() throws Exception {
        checkOccurrences(getTestPath(), "    public const int|null UNI^ON3 = null;", true);
    }

    public void testTypedClassConstants_21b() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(ClassTest::UNIO^N3);", true);
    }

    public void testTypedClassConstants_22a() throws Exception {
        checkOccurrences(getTestPath(), "    public const string|array UNION2 = 'a' . InterfaceTest::STR^ING;", true);
    }

    public void testTypedClassConstants_22b() throws Exception {
        checkOccurrences(getTestPath(), "    const string STR^ING = \"string\"; // interface", true);
    }

    public void testTypedClassConstants_23a() throws Exception {
        checkOccurrences(getTestPath(), "    public const static A = EnumTest::Tes^t; // enum", true);
    }

    public void testTypedClassConstants_23b() throws Exception {
        checkOccurrences(getTestPath(), "    case T^est;", true);
    }
}
