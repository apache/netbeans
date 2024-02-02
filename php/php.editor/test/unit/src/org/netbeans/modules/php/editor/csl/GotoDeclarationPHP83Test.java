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

public class GotoDeclarationPHP83Test extends GotoDeclarationTestBase {

    public GotoDeclarationPHP83Test(String testName) {
        super(testName);
    }

    @Override
    protected String getBaseTestFolderPath() {
        return super.getBaseTestFolderPath() + "php83/";
    }

    public void testDynamicClassConstantFetch_01a() throws Exception {
        checkDeclaration(getTestPath(), "    public const TEST3 = self::{self::T^ES . self::T};", "    public const ^TES = 'TES';");
    }

    public void testDynamicClassConstantFetch_02a() throws Exception {
        checkDeclaration(getTestPath(), "    public const TEST3 = self::{self::TES . self::^T};", "    public const ^T = 'T';");
    }

    public void testDynamicClassConstantFetch_03a() throws Exception {
        checkDeclaration(getTestPath(), "    case TEST3 = self::{self::TE^S . self::T};", "    case ^TES = 'TES';");
    }

    public void testDynamicClassConstantFetch_04a() throws Exception {
        checkDeclaration(getTestPath(), "    case TEST3 = self::{self::TES . self::^T};", "    case ^T = 'T';");
    }

    public void testDynamicClassConstantFetch_05a() throws Exception {
        checkDeclaration(getTestPath(), "Tes^t::{\"TEST\"};", "class ^Test {");
    }

    public void testDynamicClassConstantFetch_06a() throws Exception {
        checkDeclaration(getTestPath(), "$te^st::{\"TEST\"};", "$^test = \"Test\";");
    }

    public void testDynamicClassConstantFetch_07a() throws Exception {
        checkDeclaration(getTestPath(), "Test::{$vari^able};", "$^variable = 'TEST';");
    }

    public void testDynamicClassConstantFetch_07b() throws Exception {
        checkDeclaration(getTestPath(), "$test::{$vari^able};", "$^variable = 'TEST';");
    }

    public void testDynamicClassConstantFetch_07c() throws Exception {
        checkDeclaration(getTestPath(), "Test::{$vari^able . $e};", "$^variable = 'TEST';");
    }

    public void testDynamicClassConstantFetch_07d() throws Exception {
        checkDeclaration(getTestPath(), "$test::{$vari^able . $e};", "$^variable = 'TEST';");
    }

    public void testDynamicClassConstantFetch_07e() throws Exception {
        checkDeclaration(getTestPath(), "Test::{test()}::{test($varia^ble)};", "$^variable = 'TEST';");
    }

    public void testDynamicClassConstantFetch_08a() throws Exception {
        checkDeclaration(getTestPath(), "Test::{$variable . $^e};", "$^e = 'e';");
    }

    public void testDynamicClassConstantFetch_08b() throws Exception {
        checkDeclaration(getTestPath(), "$test::{$variable . $^e};", "$^e = 'e';");
    }

    public void testDynamicClassConstantFetch_09a() throws Exception {
        checkDeclaration(getTestPath(), "Test::{tes^t()}::{test($variable)};", "function ^test(string $param = \"test\"): void {");
    }

    public void testDynamicClassConstantFetch_09b() throws Exception {
        checkDeclaration(getTestPath(), "Test::{test()}::{te^st($variable)};", "function ^test(string $param = \"test\"): void {");
    }

    public void testDynamicClassConstantFetch_09c() throws Exception {
        checkDeclaration(getTestPath(), "Test::{te^st('test1')}::{test('test2')};", "function ^test(string $param = \"test\"): void {");
    }

    public void testDynamicClassConstantFetch_09d() throws Exception {
        checkDeclaration(getTestPath(), "Test::{test('test1')}::{tes^t('test2')};", "function ^test(string $param = \"test\"): void {");
    }

    public void testDynamicClassConstantFetch_10a() throws Exception {
        checkDeclaration(getTestPath(), "EnumT^est::{[]};", "enum ^EnumTest: string {");
    }

    public void testTypedClassConstants_01a() throws Exception {
        checkDeclaration(getTestPath(), "    public const ?^A NULLABLE = null;", "class ^A implements Stringable {");
    }

    public void testTypedClassConstants_01b() throws Exception {
        checkDeclaration(getTestPath(), "    private const ^A|B UNION = A;", "class ^A implements Stringable {");
    }

    public void testTypedClassConstants_01c() throws Exception {
        checkDeclaration(getTestPath(), "    protected const ^A&B INTERSECTION = B;", "class ^A implements Stringable {");
    }

    public void testTypedClassConstants_01d() throws Exception {
        checkDeclaration(getTestPath(), "    public const (^A&B)|C DNF = C;", "class ^A implements Stringable {");
    }

    public void testTypedClassConstants_01e() throws Exception {
        checkDeclaration(getTestPath(), "    public const ^A|B UNION = A; // interface", "class ^A implements Stringable {");
    }

    public void testTypedClassConstants_01f() throws Exception {
        checkDeclaration(getTestPath(), "    public const ^A&B INTERSECTION = B; // interface", "class ^A implements Stringable {");
    }

    public void testTypedClassConstants_01g() throws Exception {
        checkDeclaration(getTestPath(), "    public const (^A&B)|C DNF = C; // interface", "class ^A implements Stringable {");
    }

    public void testTypedClassConstants_01h() throws Exception {
        checkDeclaration(getTestPath(), "    private const ^A|B UNION = A; // trait", "class ^A implements Stringable {");
    }

    public void testTypedClassConstants_01i() throws Exception {
        checkDeclaration(getTestPath(), "    protected const ^A&B INTERSECTION = B; // trait", "class ^A implements Stringable {");
    }

    public void testTypedClassConstants_01j() throws Exception {
        checkDeclaration(getTestPath(), "    public const (^A&B)|C DNF = C; // trait", "class ^A implements Stringable {");
    }

    public void testTypedClassConstants_01k() throws Exception {
        checkDeclaration(getTestPath(), "    private const ^A|B UNION = A; // enum", "class ^A implements Stringable {");
    }

    public void testTypedClassConstants_01l() throws Exception {
        checkDeclaration(getTestPath(), "    protected const ^A&B INTERSECTION = B; // enum", "class ^A implements Stringable {");
    }

    public void testTypedClassConstants_01m() throws Exception {
        checkDeclaration(getTestPath(), "    public const (^A&B)|(A&C) DNF = C; // enum", "class ^A implements Stringable {");
    }

    public void testTypedClassConstants_01o() throws Exception {
        checkDeclaration(getTestPath(), "    public const (A&B)|(^A&C) DNF = C; // enum", "class ^A implements Stringable {");
    }

    public void testTypedClassConstants_01p() throws Exception {
        checkDeclaration(getTestPath(), "define(\"A\", new ^A());", "class ^A implements Stringable {");
    }

    public void testTypedClassConstants_02a() throws Exception {
        checkDeclaration(getTestPath(), "    private const A|^B UNION = A;", "class ^B extends A {}");
    }

    public void testTypedClassConstants_02b() throws Exception {
        checkDeclaration(getTestPath(), "    protected const A&^B INTERSECTION = B;", "class ^B extends A {}");
    }

    public void testTypedClassConstants_02c() throws Exception {
        checkDeclaration(getTestPath(), "    public const (A&^B)|C DNF = C;", "class ^B extends A {}");
    }

    public void testTypedClassConstants_02d() throws Exception {
        checkDeclaration(getTestPath(), "    public const A|^B UNION = A; // interface", "class ^B extends A {}");
    }

    public void testTypedClassConstants_02e() throws Exception {
        checkDeclaration(getTestPath(), "    public const A&^B INTERSECTION = B; // interface", "class ^B extends A {}");
    }

    public void testTypedClassConstants_02f() throws Exception {
        checkDeclaration(getTestPath(), "    public const (A&^B)|C DNF = C; // interface", "class ^B extends A {}");
    }

    public void testTypedClassConstants_02g() throws Exception {
        checkDeclaration(getTestPath(), "    private const A|^B UNION = A; // trait", "class ^B extends A {}");
    }

    public void testTypedClassConstants_02h() throws Exception {
        checkDeclaration(getTestPath(), "    protected const A&^B INTERSECTION = B; // trait", "class ^B extends A {}");
    }

    public void testTypedClassConstants_02i() throws Exception {
        checkDeclaration(getTestPath(), "    public const (A&^B)|C DNF = C; // trait", "class ^B extends A {}");
    }

    public void testTypedClassConstants_02j() throws Exception {
        checkDeclaration(getTestPath(), "    private const A|^B UNION = A; // enum", "class ^B extends A {}");
    }

    public void testTypedClassConstants_02k() throws Exception {
        checkDeclaration(getTestPath(), "    protected const A&^B INTERSECTION = B; // enum", "class ^B extends A {}");
    }

    public void testTypedClassConstants_02l() throws Exception {
        checkDeclaration(getTestPath(), "    public const (A&^B)|(A&C) DNF = C; // enum", "class ^B extends A {}");
    }

    public void testTypedClassConstants_02m() throws Exception {
        checkDeclaration(getTestPath(), "define(\"B\", new ^B());", "class ^B extends A {}");
    }

    public void testTypedClassConstants_03a() throws Exception {
        checkDeclaration(getTestPath(), "    public const (A&B)|^C DNF = C;", "class ^C extends A {}");
    }

    public void testTypedClassConstants_03b() throws Exception {
        checkDeclaration(getTestPath(), "    public const (A&B)|^C DNF = C; // interface", "class ^C extends A {}");
    }

    public void testTypedClassConstants_03c() throws Exception {
        checkDeclaration(getTestPath(), "    public const (A&B)|^C DNF = C; // trait", "class ^C extends A {}");
    }

    public void testTypedClassConstants_03d() throws Exception {
        checkDeclaration(getTestPath(), "    public const (A&B)|(A&^C) DNF = C; // enum", "class ^C extends A {}");
    }

    public void testTypedClassConstants_03e() throws Exception {
        checkDeclaration(getTestPath(), "define(\"C\", new ^C());", "class ^C extends A {}");
    }

    public void testTypedClassConstants_04a() throws Exception {
        checkDeclaration(getTestPath(), "    private const A|B UNION = ^A;", "define(\"^A\", new A());");
    }

    public void testTypedClassConstants_04b() throws Exception {
        checkDeclaration(getTestPath(), "    public const object OBJECT = ^A;", "define(\"^A\", new A());");
    }

    public void testTypedClassConstants_04c() throws Exception {
        checkDeclaration(getTestPath(), "    public const A|B UNION = ^A; // interface", "define(\"^A\", new A());");
    }

    public void testTypedClassConstants_04d() throws Exception {
        checkDeclaration(getTestPath(), "    private const A|B UNION = ^A; // trait", "define(\"^A\", new A());");
    }

    public void testTypedClassConstants_04e() throws Exception {
        checkDeclaration(getTestPath(), "    private const A|B UNION = ^A; // enum", "define(\"^A\", new A());");
    }

    public void testTypedClassConstants_05a() throws Exception {
        checkDeclaration(getTestPath(), "    protected const A&B INTERSECTION = ^B;", "define(\"^B\", new B());");
    }

    public void testTypedClassConstants_05b() throws Exception {
        checkDeclaration(getTestPath(), "    public const A&B INTERSECTION = ^B; // interface", "define(\"^B\", new B());");
    }

    public void testTypedClassConstants_05c() throws Exception {
        checkDeclaration(getTestPath(), "    protected const A&B INTERSECTION = ^B; // trait", "define(\"^B\", new B());");
    }

    public void testTypedClassConstants_05d() throws Exception {
        checkDeclaration(getTestPath(), "    protected const A&B INTERSECTION = ^B; // enum", "define(\"^B\", new B());");
    }

    public void testTypedClassConstants_06a() throws Exception {
        checkDeclaration(getTestPath(), "    public const (A&B)|C DNF = ^C;", "define(\"^C\", new C());");
    }

    public void testTypedClassConstants_06b() throws Exception {
        checkDeclaration(getTestPath(), "    public const (A&B)|C DNF = ^C; // interface", "define(\"^C\", new C());");
    }

    public void testTypedClassConstants_06c() throws Exception {
        checkDeclaration(getTestPath(), "    public const (A&B)|C DNF = ^C; // trait", "define(\"^C\", new C());");
    }

    public void testTypedClassConstants_06d() throws Exception {
        checkDeclaration(getTestPath(), "    public const (A&B)|(A&C) DNF = ^C; // enum", "define(\"^C\", new C());");
    }

    public void testTypedClassConstants_07a() throws Exception {
        checkDeclaration(getTestPath(), "    public const mixed MIXED = 1 + self::WITH^OUT_TYPE;", "    public const ^WITHOUT_TYPE = 1;");
    }

    public void testTypedClassConstants_07b() throws Exception {
        checkDeclaration(getTestPath(), "var_dump(ClassTest::WITHOUT_T^YPE);", "    public const ^WITHOUT_TYPE = 1;");
    }

    public void testTypedClassConstants_08a() throws Exception {
        checkDeclaration(getTestPath(), "var_dump(ClassTest::NULLAB^LE);", "    public const ?A ^NULLABLE = null;");
    }

    public void testTypedClassConstants_09a() throws Exception {
        checkDeclaration(getTestPath(), "var_dump(ClassTest::UNI^ON);", "    private const A|B ^UNION = A;");
    }

    public void testTypedClassConstants_10a() throws Exception {
        checkDeclaration(getTestPath(), "var_dump(ClassTest::INTERSECTI^ON);", "    protected const A&B ^INTERSECTION = B;");
    }

    public void testTypedClassConstants_11a() throws Exception {
        checkDeclaration(getTestPath(), "var_dump(ClassTest::DN^F);", "    public const (A&B)|C ^DNF = C;");
    }

    public void testTypedClassConstants_12a() throws Exception {
        checkDeclaration(getTestPath(), "var_dump(ClassTest::STRIN^G);", "    public const string ^STRING = 'a';");
    }

    public void testTypedClassConstants_13a() throws Exception {
        checkDeclaration(getTestPath(), "var_dump(ClassTest::IN^T);", "    public const int ^INT = 1;");
    }

    public void testTypedClassConstants_14a() throws Exception {
        checkDeclaration(getTestPath(), "var_dump(ClassTest::FLOA^T);", "    public const float ^FLOAT = 1.5;");
    }

    public void testTypedClassConstants_15a() throws Exception {
        checkDeclaration(getTestPath(), "var_dump(ClassTest::BO^OL);", "    public const bool ^BOOL = true;");
    }

    public void testTypedClassConstants_16a() throws Exception {
        checkDeclaration(getTestPath(), "var_dump(ClassTest::ARRA^Y);", "    public const array ^ARRAY = ['t', 'e', 's', 't'];");
    }

    public void testTypedClassConstants_17a() throws Exception {
        checkDeclaration(getTestPath(), "var_dump(ClassTest::ITERAB^LE);", "    public const iterable ^ITERABLE = ['a', 'b', 'c'];");
    }

    public void testTypedClassConstants_18a() throws Exception {
        checkDeclaration(getTestPath(), "var_dump(ClassTest::MIX^ED);", "    public const mixed ^MIXED = 1 + self::WITHOUT_TYPE;");
    }

    public void testTypedClassConstants_19a() throws Exception {
        checkDeclaration(getTestPath(), "var_dump(ClassTest::OB^JECT);", "    public const object ^OBJECT = A;");
    }

    public void testTypedClassConstants_20a() throws Exception {
        checkDeclaration(getTestPath(), "var_dump(ClassTest::UNIO^N2);", "    public const string|array ^UNION2 = 'a' . InterfaceTest::STRING;");
    }

    public void testTypedClassConstants_21a() throws Exception {
        checkDeclaration(getTestPath(), "var_dump(ClassTest::UNIO^N3);", "    public const int|null ^UNION3 = null;");
    }

    public void testTypedClassConstants_22a() throws Exception {
        checkDeclaration(getTestPath(), "    public const string|array UNION2 = 'a' . InterfaceTest::ST^RING;", "    const string ^STRING = \"string\"; // interface");
    }

    public void testTypedClassConstants_23a() throws Exception {
        checkDeclaration(getTestPath(), "    public const static A = EnumTest::Te^st; // enum", "    case ^Test;");
    }

    public void testTypedClassConstants_24a() throws Exception {
        checkDeclaration(getTestPath(), "var_dump(Class^Test::WITHOUT_TYPE);", "class ^ClassTest {");
    }

    public void testTypedClassConstants_25a() throws Exception {
        checkDeclaration(getTestPath(), "    public const string|array UNION2 = 'a' . InterfaceT^est::STRING;", "interface ^InterfaceTest {");
    }

    public void testTypedClassConstants_26a() throws Exception {
        checkDeclaration(getTestPath(), "    public const static A = EnumT^est::Test; // enum", "enum ^EnumTest {");
    }

    public void testTypedClassConstants_01() throws Exception {
//        checkDeclaration(getTestPath(), "", "");
    }
}
