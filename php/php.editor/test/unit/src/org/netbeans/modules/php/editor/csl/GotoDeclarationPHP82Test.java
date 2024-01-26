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

public class GotoDeclarationPHP82Test extends GotoDeclarationTestBase {

    public GotoDeclarationPHP82Test(String testName) {
        super(testName);
    }

    @Override
    protected String getBaseTestFolderPath() {
        return super.getBaseTestFolderPath() + "php82/";
    }

    public void testConstantsInTraits_01a() throws Exception {
        checkDeclaration(getTestPath(), "        echo self::IMPLIC^IT_PUBLIC_TRAIT . PHP_EOL;", "    const ^IMPLICIT_PUBLIC_TRAIT = 'ExampleTrait implicit public';");
    }

    public void testConstantsInTraits_01b() throws Exception {
        checkDeclaration(getTestPath(), "        echo self::IMPL^ICIT_PUBLIC_TRAIT . PHP_EOL; // use", "    const ^IMPLICIT_PUBLIC_TRAIT = 'ExampleTrait implicit public';");
    }

    public void testConstantsInTraits_01c() throws Exception {
        checkDeclaration(getTestPath(), "        echo self::IMPL^ICIT_PUBLIC_TRAIT . PHP_EOL; // class", "    const ^IMPLICIT_PUBLIC_TRAIT = 'ExampleTrait implicit public';");
    }

    public void testConstantsInTraits_01d() throws Exception {
        checkDeclaration(getTestPath(), "        echo self::IMPLICIT_P^UBLIC_TRAIT . PHP_EOL; // child", "    const ^IMPLICIT_PUBLIC_TRAIT = 'ExampleTrait implicit public';");
    }

    public void testConstantsInTraits_01e() throws Exception {
        checkDeclaration(getTestPath(), "echo ExampleClass::I^MPLICIT_PUBLIC_TRAIT . PHP_EOL;", "    const ^IMPLICIT_PUBLIC_TRAIT = 'ExampleTrait implicit public';");
    }

    public void testConstantsInTraits_02a() throws Exception {
        checkDeclaration(getTestPath(), "        echo static::PRIV^ATE_TRAIT . PHP_EOL;", "    private const ^PRIVATE_TRAIT = 'ExampleTrait private';");
    }

    public void testConstantsInTraits_02b() throws Exception {
        checkDeclaration(getTestPath(), "        echo static::PRIVATE_T^RAIT . PHP_EOL; // use", "    private const ^PRIVATE_TRAIT = 'ExampleTrait private';");
    }

    public void testConstantsInTraits_02c() throws Exception {
        checkDeclaration(getTestPath(), "        echo static::PRIVATE_TRA^IT . PHP_EOL; // class", "    private const ^PRIVATE_TRAIT = 'ExampleTrait private';");
    }

    public void testConstantsInTraits_03a() throws Exception {
        checkDeclaration(getTestPath(), "        echo $this::PROTECTED_TRA^IT . PHP_EOL;", "    protected const ^PROTECTED_TRAIT = 'ExampleTrait protected';");
    }

    public void testConstantsInTraits_03b() throws Exception {
        checkDeclaration(getTestPath(), "        echo $this::P^ROTECTED_TRAIT . PHP_EOL; // use", "    protected const ^PROTECTED_TRAIT = 'ExampleTrait protected';");
    }

    public void testConstantsInTraits_03c() throws Exception {
        checkDeclaration(getTestPath(), "        echo $this::P^ROTECTED_TRAIT . PHP_EOL; // class", "    protected const ^PROTECTED_TRAIT = 'ExampleTrait protected';");
    }

    public void testConstantsInTraits_04a() throws Exception {
        checkDeclaration(getTestPath(), "        echo parent::PUBLI^C_TRAIT . PHP_EOL; // child", "    public const ^PUBLIC_TRAIT = 'ExampleTrait public';");
    }

    public void testDNFType_Param_a01() throws Exception {
        checkDeclaration(getTestPath(), "function paramType((F^oo&Bar)|Baz $param1, Baz|(Foo&Bar) $param2, (Foo&Bar)|(Bar&Baz) $param3): void {", "class ^Foo {");
    }

    public void testDNFType_Param_a02() throws Exception {
        checkDeclaration(getTestPath(), "function paramType((Foo&Bar)|Baz $param1, Baz|(Fo^o&Bar) $param2, (Foo&Bar)|(Bar&Baz) $param3): void {", "class ^Foo {");
    }

    public void testDNFType_Param_a03() throws Exception {
        checkDeclaration(getTestPath(), "function paramType((Foo&Bar)|Baz $param1, Baz|(Foo&Bar) $param2, (F^oo&Bar)|(Bar&Baz) $param3): void {", "class ^Foo {");
    }

    public void testDNFType_Param_a04() throws Exception {
        checkDeclaration(getTestPath(), "    public function paramType((F^oo&Baz)|(Foo&Bar)|Baz $test): void { // class", "class ^Foo {");
    }

    public void testDNFType_Param_a05() throws Exception {
        checkDeclaration(getTestPath(), "    public function paramType((Foo&Baz)|(Fo^o&Bar)|Baz $test): void { // class", "class ^Foo {");
    }

    public void testDNFType_Param_a06() throws Exception {
        checkDeclaration(getTestPath(), "    public function paramType((F^oo&Bar)|(Bar&Baz) $test1, Foo|(Foo&Bar) $test2): void { // trait", "class ^Foo {");
    }

    public void testDNFType_Param_a07() throws Exception {
        checkDeclaration(getTestPath(), "    public function paramType((Foo&Bar)|(Bar&Baz) $test1, Fo^o|(Foo&Bar) $test2): void { // trait", "class ^Foo {");
    }

    public void testDNFType_Param_a08() throws Exception {
        checkDeclaration(getTestPath(), "    public function paramType((Foo&Bar)|(Bar&Baz) $test1, Foo|(F^oo&Bar) $test2): void { // trait", "class ^Foo {");
    }

    public void testDNFType_Param_a09() throws Exception {
        checkDeclaration(getTestPath(), "    public function paramType(F^oo|(Foo&Bar)|null $test);", "class ^Foo {");
    }

    public void testDNFType_Param_a10() throws Exception {
        checkDeclaration(getTestPath(), "    public function paramType(Foo|(Fo^o&Bar)|null $test);", "class ^Foo {");
    }

    public void testDNFType_Param_a11() throws Exception {
        checkDeclaration(getTestPath(), "$closure = function(Fo^o|(Foo&Bar)|(Bar&Baz) $test1, $test2): void {};", "class ^Foo {");
    }

    public void testDNFType_Param_a12() throws Exception {
        checkDeclaration(getTestPath(), "$closure = function(Foo|(Fo^o&Bar)|(Bar&Baz) $test1, $test2): void {};", "class ^Foo {");
    }

    public void testDNFType_Param_a13() throws Exception {
        checkDeclaration(getTestPath(), "$arrow = fn(F^oo|Bar|(Foo&Bar) $test) => $test;", "class ^Foo {");
    }

    public void testDNFType_Param_a14() throws Exception {
        checkDeclaration(getTestPath(), "$arrow = fn(Foo|Bar|(Fo^o&Bar) $test) => $test;", "class ^Foo {");
    }

    public void testDNFType_Param_a15() throws Exception {
        checkDeclaration(getTestPath(), "$arrow = fn((F^oo&Bar)|null $test): Foo|(Foo&Bar) => $test;", "class ^Foo {");
    }

    public void testDNFType_Param_a16() throws Exception {
        checkDeclaration(getTestPath(), "$arrow = fn((F^oo&Bar)|null $test): Foo|(Foo&Bar) => $test;", "class ^Foo {");
    }

    public void testDNFType_Param_a17() throws Exception {
        checkDeclaration(getTestPath(), " * @method (Foo&Bar)|(Bar&Baz) methodTag((Fo^o&Bar)|Bar $param1, Foo|(Bar&Baz) $param2) Description", "class ^Foo {");
    }

    public void testDNFType_Param_a18() throws Exception {
        checkDeclaration(getTestPath(), " * @method (Foo&Bar)|(Bar&Baz) methodTag((Foo&Bar)|Bar $param1, Fo^o|(Bar&Baz) $param2) Description", "class ^Foo {");
    }

    public void testDNFType_Param_a19() throws Exception {
        checkDeclaration(getTestPath(), " * @method static (Foo&Bar)|Baz staticMethodTag(Bar|(F^oo&Bar) $param1, (Foo&Bar)|(Bar&Baz) $param2) Description", "class ^Foo {");
    }

    public void testDNFType_Param_a20() throws Exception {
        checkDeclaration(getTestPath(), " * @method static (Foo&Bar)|Baz staticMethodTag(Bar|(Foo&Bar) $param1, (F^oo&Bar)|(Bar&Baz) $param2) Description", "class ^Foo {");
    }

    public void testDNFType_Param_b01() throws Exception {
        checkDeclaration(getTestPath(), "function paramType((Foo&B^ar)|Baz $param1, Baz|(Foo&Bar) $param2, (Foo&Bar)|(Bar&Baz) $param3): void {", "class ^Bar {");
    }

    public void testDNFType_Param_b02() throws Exception {
        checkDeclaration(getTestPath(), "function paramType((Foo&Bar)|Baz $param1, Baz|(Foo&Ba^r) $param2, (Foo&Bar)|(Bar&Baz) $param3): void {", "class ^Bar {");
    }

    public void testDNFType_Param_b03() throws Exception {
        checkDeclaration(getTestPath(), "function paramType((Foo&Bar)|Baz $param1, Baz|(Foo&Bar) $param2, (Foo&B^ar)|(Bar&Baz) $param3): void {", "class ^Bar {");
    }

    public void testDNFType_Param_b04() throws Exception {
        checkDeclaration(getTestPath(), "function paramType((Foo&Bar)|Baz $param1, Baz|(Foo&Bar) $param2, (Foo&Bar)|(Ba^r&Baz) $param3): void {", "class ^Bar {");
    }

    public void testDNFType_Param_b05() throws Exception {
        checkDeclaration(getTestPath(), "    public function paramType((Foo&Baz)|(Foo&B^ar)|Baz $test): void { // class", "class ^Bar {");
    }

    public void testDNFType_Param_b06() throws Exception {
        checkDeclaration(getTestPath(), "    public function paramType((Foo&Ba^r)|(Bar&Baz) $test1, Foo|(Foo&Bar) $test2): void { // trait", "class ^Bar {");
    }

    public void testDNFType_Param_b07() throws Exception {
        checkDeclaration(getTestPath(), "    public function paramType((Foo&Bar)|(Ba^r&Baz) $test1, Foo|(Foo&Bar) $test2): void { // trait", "class ^Bar {");
    }

    public void testDNFType_Param_b08() throws Exception {
        checkDeclaration(getTestPath(), "    public function paramType((Foo&Bar)|(Bar&Baz) $test1, Foo|(Foo&Ba^r) $test2): void { // trait", "class ^Bar {");
    }

    public void testDNFType_Param_b09() throws Exception {
        checkDeclaration(getTestPath(), "    public function paramType(Foo|(Foo&^Bar)|null $test);", "class ^Bar {");
    }

    public void testDNFType_Param_b10() throws Exception {
        checkDeclaration(getTestPath(), "$closure = function(Foo|(Foo&B^ar)|(Bar&Baz) $test1, $test2): void {};", "class ^Bar {");
    }

    public void testDNFType_Param_b11() throws Exception {
        checkDeclaration(getTestPath(), "$closure = function(Foo|(Foo&Bar)|(Ba^r&Baz) $test1, $test2): void {};", "class ^Bar {");
    }

    public void testDNFType_Param_b12() throws Exception {
        checkDeclaration(getTestPath(), "$arrow = fn(Foo|B^ar|(Foo&Bar) $test) => $test;", "class ^Bar {");
    }

    public void testDNFType_Param_b13() throws Exception {
        checkDeclaration(getTestPath(), "$arrow = fn(Foo|Bar|(Foo&Ba^r) $test) => $test;", "class ^Bar {");
    }

    public void testDNFType_Param_b14() throws Exception {
        checkDeclaration(getTestPath(), "$arrow = fn((Foo&Ba^r)|null $test): Foo|(Foo&Bar) => $test;", "class ^Bar {");
    }

    public void testDNFType_Param_b15() throws Exception {
        checkDeclaration(getTestPath(), " * @method (Foo&Bar)|(Bar&Baz) methodTag((Foo&B^ar)|Bar $param1, Foo|(Bar&Baz) $param2) Description", "class ^Bar {");
    }

    public void testDNFType_Param_b16() throws Exception {
        checkDeclaration(getTestPath(), " * @method (Foo&Bar)|(Bar&Baz) methodTag((Foo&Bar)|Ba^r $param1, Foo|(Bar&Baz) $param2) Description", "class ^Bar {");
    }

    public void testDNFType_Param_b17() throws Exception {
        checkDeclaration(getTestPath(), " * @method (Foo&Bar)|(Bar&Baz) methodTag((Foo&Bar)|Bar $param1, Foo|(B^ar&Baz) $param2) Description", "class ^Bar {");
    }

    public void testDNFType_Param_b18() throws Exception {
        checkDeclaration(getTestPath(), " * @method static (Foo&Bar)|Baz staticMethodTag(B^ar|(Foo&Bar) $param1, (Foo&Bar)|(Bar&Baz) $param2) Description", "class ^Bar {");
    }

    public void testDNFType_Param_b19() throws Exception {
        checkDeclaration(getTestPath(), " * @method static (Foo&Bar)|Baz staticMethodTag(Bar|(Foo&Ba^r) $param1, (Foo&Bar)|(Bar&Baz) $param2) Description", "class ^Bar {");
    }

    public void testDNFType_Param_b20() throws Exception {
        checkDeclaration(getTestPath(), " * @method static (Foo&Bar)|Baz staticMethodTag(Bar|(Foo&Bar) $param1, (Foo&Ba^r)|(Bar&Baz) $param2) Description", "class ^Bar {");
    }

    public void testDNFType_Param_b21() throws Exception {
        checkDeclaration(getTestPath(), " * @method static (Foo&Bar)|Baz staticMethodTag(Bar|(Foo&Bar) $param1, (Foo&Bar)|(Ba^r&Baz) $param2) Description", "class ^Bar {");
    }

    public void testDNFType_Param_c01() throws Exception {
        checkDeclaration(getTestPath(), "function paramType((Foo&Bar)|Ba^z $param1, Baz|(Foo&Bar) $param2, (Foo&Bar)|(Bar&Baz) $param3): void {", "class ^Baz {");
    }

    public void testDNFType_Param_c02() throws Exception {
        checkDeclaration(getTestPath(), "function paramType((Foo&Bar)|Baz $param1, Ba^z|(Foo&Bar) $param2, (Foo&Bar)|(Bar&Baz) $param3): void {", "class ^Baz {");
    }

    public void testDNFType_Param_c03() throws Exception {
        checkDeclaration(getTestPath(), "function paramType((Foo&Bar)|Baz $param1, Baz|(Foo&Bar) $param2, (Foo&Bar)|(Bar&Ba^z) $param3): void {", "class ^Baz {");
    }

    public void testDNFType_Param_c04() throws Exception {
        checkDeclaration(getTestPath(), "    public function paramType((Foo&Ba^z)|(Foo&Bar)|Baz $test): void { // class", "class ^Baz {");
    }

    public void testDNFType_Param_c05() throws Exception {
        checkDeclaration(getTestPath(), "    public function paramType((Foo&Baz)|(Foo&Bar)|^Baz $test): void { // class", "class ^Baz {");
    }

    public void testDNFType_Param_c06() throws Exception {
        checkDeclaration(getTestPath(), "    public function paramType((Foo&Bar)|(Bar&Baz^) $test1, Foo|(Foo&Bar) $test2): void { // trait", "class ^Baz {");
    }

    public void testDNFType_Param_c07() throws Exception {
        checkDeclaration(getTestPath(), "$closure = function(Foo|(Foo&Bar)|(Bar&B^az) $test1, $test2): void {};", "class ^Baz {");
    }

    public void testDNFType_Param_c08() throws Exception {
        checkDeclaration(getTestPath(), " * @method (Foo&Bar)|(Bar&Baz) methodTag((Foo&Bar)|Bar $param1, Foo|(Bar&Ba^z) $param2) Description", "class ^Baz {");
    }

    public void testDNFType_Param_c09() throws Exception {
        checkDeclaration(getTestPath(), " * @method static (Foo&Bar)|Baz staticMethodTag(Bar|(Foo&Bar) $param1, (Foo&Bar)|(Bar&^Baz) $param2) Description", "class ^Baz {");
    }

    public void testDNFType_Return_a01() throws Exception {
        checkDeclaration(getTestPath(), "function returnType1(): (^Foo&Bar)|Baz {}", "class ^Foo {");
    }

    public void testDNFType_Return_a02() throws Exception {
        checkDeclaration(getTestPath(), "function returnType2(): Baz|(Fo^o&Bar) {}", "class ^Foo {");
    }

    public void testDNFType_Return_a03() throws Exception {
        checkDeclaration(getTestPath(), "function returnType3(): Baz|(Fo^o&Bar)|Foo {}", "class ^Foo {");
    }

    public void testDNFType_Return_a04() throws Exception {
        checkDeclaration(getTestPath(), "function returnType3(): Baz|(Foo&Bar)|Fo^o {}", "class ^Foo {");
    }

    public void testDNFType_Return_a05() throws Exception {
        checkDeclaration(getTestPath(), "function returnType4(): (Fo^o&Bar)|(Foo&Baz) {}", "class ^Foo {");
    }

    public void testDNFType_Return_a06() throws Exception {
        checkDeclaration(getTestPath(), "function returnType4(): (Foo&Bar)|(Fo^o&Baz) {}", "class ^Foo {");
    }

    public void testDNFType_Return_a07() throws Exception {
        checkDeclaration(getTestPath(), "    public function returnType(): (F^oo&Bar)|Baz { // class", "class ^Foo {");
    }

    public void testDNFType_Return_a08() throws Exception {
        checkDeclaration(getTestPath(), "    public function returnType(): F^oo|(Foo&Bar) { // trait", "class ^Foo {");
    }

    public void testDNFType_Return_a09() throws Exception {
        checkDeclaration(getTestPath(), "    public function returnType(): Foo|(Fo^o&Bar) { // trait", "class ^Foo {");
    }

    public void testDNFType_Return_a10() throws Exception {
        checkDeclaration(getTestPath(), "    public function returnType(): (F^oo&Bar)|(Bar&Baz);", "class ^Foo {");
    }

    public void testDNFType_Return_a11() throws Exception {
        checkDeclaration(getTestPath(), "$closure = function(int $test): (Foo^&Bar)|null {};", "class ^Foo {");
    }

    public void testDNFType_Return_a12() throws Exception {
        checkDeclaration(getTestPath(), "$arrow = fn((Foo&Bar)|null $test): Fo^o|(Foo&Bar) => $test;", "class ^Foo {");
    }

    public void testDNFType_Return_a13() throws Exception {
        checkDeclaration(getTestPath(), "$arrow = fn((Foo&Bar)|null $test): Foo|(F^oo&Bar) => $test;", "class ^Foo {");
    }

    public void testDNFType_Return_a14() throws Exception {
        checkDeclaration(getTestPath(), " * @method (F^oo&Bar)|(Bar&Baz) methodTag((Foo&Bar)|Bar $param1, Foo|(Bar&Baz) $param2) Description", "class ^Foo {");
    }

    public void testDNFType_Return_a15() throws Exception {
        checkDeclaration(getTestPath(), " * @method static (^Foo&Bar)|Baz staticMethodTag(Bar|(Foo&Bar) $param1, (Foo&Bar)|(Bar&Baz) $param2) Description", "class ^Foo {");
    }

    public void testDNFType_Return_b01() throws Exception {
        checkDeclaration(getTestPath(), "function returnType1(): (Foo&B^ar)|Baz {}", "class ^Bar {");
    }

    public void testDNFType_Return_b02() throws Exception {
        checkDeclaration(getTestPath(), "function returnType2(): Baz|(Foo&^Bar) {}", "class ^Bar {");
    }

    public void testDNFType_Return_b03() throws Exception {
        checkDeclaration(getTestPath(), "function returnType3(): Baz|(Foo&B^ar)|Foo {}", "class ^Bar {");
    }

    public void testDNFType_Return_b04() throws Exception {
        checkDeclaration(getTestPath(), "function returnType4(): (Foo&Ba^r)|(Foo&Baz) {}", "class ^Bar {");
    }

    public void testDNFType_Return_b05() throws Exception {
        checkDeclaration(getTestPath(), "    public function returnType(): (Foo&Bar^)|Baz { // class", "class ^Bar {");
    }

    public void testDNFType_Return_b06() throws Exception {
        checkDeclaration(getTestPath(), "    public function returnType(): Foo|(Foo&Ba^r) { // trait", "class ^Bar {");
    }

    public void testDNFType_Return_b07() throws Exception {
        checkDeclaration(getTestPath(), "    public function returnType(): (Foo&Ba^r)|(Bar&Baz);", "class ^Bar {");
    }

    public void testDNFType_Return_b08() throws Exception {
        checkDeclaration(getTestPath(), "    public function returnType(): (Foo&Bar)|(B^ar&Baz);", "class ^Bar {");
    }

    public void testDNFType_Return_b09() throws Exception {
        checkDeclaration(getTestPath(), "$closure = function(int $test): (Foo&B^ar)|null {};", "class ^Bar {");
    }

    public void testDNFType_Return_b10() throws Exception {
        checkDeclaration(getTestPath(), "$arrow = fn((Foo&Bar)|null $test): Foo|(Foo&Ba^r) => $test;", "class ^Bar {");
    }

    public void testDNFType_Return_b11() throws Exception {
        checkDeclaration(getTestPath(), " * @method (Foo&^Bar)|(Bar&Baz) methodTag((Foo&Bar)|Bar $param1, Foo|(Bar&Baz) $param2) Description", "class ^Bar {");
    }

    public void testDNFType_Return_b12() throws Exception {
        checkDeclaration(getTestPath(), " * @method (Foo&Bar)|(Ba^r&Baz) methodTag((Foo&Bar)|Bar $param1, Foo|(Bar&Baz) $param2) Description", "class ^Bar {");
    }

    public void testDNFType_Return_b13() throws Exception {
        checkDeclaration(getTestPath(), " * @method static (Foo&B^ar)|Baz staticMethodTag(Bar|(Foo&Bar) $param1, (Foo&Bar)|(Bar&Baz) $param2) Description", "class ^Bar {");
    }

    public void testDNFType_Return_c01() throws Exception {
        checkDeclaration(getTestPath(), "function returnType1(): (Foo&Bar)|B^az {}", "class ^Baz {");
    }

    public void testDNFType_Return_c02() throws Exception {
        checkDeclaration(getTestPath(), "function returnType2(): Ba^z|(Foo&Bar) {}", "class ^Baz {");
    }

    public void testDNFType_Return_c03() throws Exception {
        checkDeclaration(getTestPath(), "function returnType3(): Baz^|(Foo&Bar)|Foo {}", "class ^Baz {");
    }

    public void testDNFType_Return_c04() throws Exception {
        checkDeclaration(getTestPath(), "function returnType4(): (Foo&Bar)|(Foo&Ba^z) {}", "class ^Baz {");
    }

    public void testDNFType_Return_c05() throws Exception {
        checkDeclaration(getTestPath(), "    public function returnType(): (Foo&Bar)|(Bar&B^az);", "class ^Baz {");
    }

    public void testDNFType_Return_c06() throws Exception {
        checkDeclaration(getTestPath(), " * @method (Foo&Bar)|(Bar&Ba^z) methodTag((Foo&Bar)|Bar $param1, Foo|(Bar&Baz) $param2) Description", "class ^Baz {");
    }

    public void testDNFType_Return_c07() throws Exception {
        checkDeclaration(getTestPath(), " * @method static (Foo&Bar)|^Baz staticMethodTag(Bar|(Foo&Bar) $param1, (Foo&Bar)|(Bar&Baz) $param2) Description", "class ^Baz {");
    }

    public void testDNFType_Field_a01() throws Exception {
        checkDeclaration(getTestPath(), "    private (^Foo&Bar)|Baz $fieldClass; // class", "class ^Foo {");
    }

    public void testDNFType_Field_a02() throws Exception {
        checkDeclaration(getTestPath(), "    private (Fo^o&Bar)|Baz $test; // trait", "class ^Foo {");
    }

    public void testDNFType_Field_a03() throws Exception {
        checkDeclaration(getTestPath(), " * @property F^oo|(Bar&Baz) $propertyTag Description", "class ^Foo {");
    }

    public void testDNFType_Field_b01() throws Exception {
        checkDeclaration(getTestPath(), "    private (Foo&Ba^r)|Baz $fieldClass; // class", "class ^Bar {");
    }

    public void testDNFType_Field_b02() throws Exception {
        checkDeclaration(getTestPath(), "    private static Ba^r|(Bar&Baz) $staticFieldClass; // class", "class ^Bar {");
    }

    public void testDNFType_Field_b03() throws Exception {
        checkDeclaration(getTestPath(), "    private static Bar|(Ba^r&Baz) $staticFieldClass; // class", "class ^Bar {");
    }

    public void testDNFType_Field_b04() throws Exception {
        checkDeclaration(getTestPath(), "    private (Foo&Ba^r)|Baz $test; // trait", "class ^Bar {");
    }

    public void testDNFType_Field_b05() throws Exception {
        checkDeclaration(getTestPath(), " * @property Foo|(Ba^r&Baz) $propertyTag Description", "class ^Bar {");
    }

    public void testDNFType_Field_c01() throws Exception {
        checkDeclaration(getTestPath(), "    private (Foo&Bar)|Ba^z $fieldClass; // class", "class ^Baz {");
    }

    public void testDNFType_Field_c02() throws Exception {
        checkDeclaration(getTestPath(), "    private static Bar|(Bar&^Baz) $staticFieldClass; // class", "class ^Baz {");
    }

    public void testDNFType_Field_c03() throws Exception {
        checkDeclaration(getTestPath(), "    private (Foo&Bar)|Ba^z $test; // trait", "class ^Baz {");
    }

    public void testDNFType_Field_c04() throws Exception {
        checkDeclaration(getTestPath(), " * @property Foo|(Bar&^Baz) $propertyTag Description", "class ^Baz {");
    }

    public void testDNFType_Vardoc_a01() throws Exception {
        checkDeclaration(getTestPath(), "/** @var (F^oo&Bar)|Foo|(Bar&Baz&Foo) $vardoc1 */", "class ^Foo {");
    }

    public void testDNFType_Vardoc_a02() throws Exception {
        checkDeclaration(getTestPath(), "/** @var (Foo&Bar)|Fo^o|(Bar&Baz&Foo) $vardoc1 */", "class ^Foo {");
    }

    public void testDNFType_Vardoc_a03() throws Exception {
        checkDeclaration(getTestPath(), "/* @var $vardoc2 (F^oo&Bar)|Baz */", "class ^Foo {");
    }

    public void testDNFType_Vardoc_a04() throws Exception {
        checkDeclaration(getTestPath(), "/** @var Bar|Baz|Fo^o $unionType */", "class ^Foo {");
    }

    public void testDNFType_Vardoc_a05() throws Exception {
        checkDeclaration(getTestPath(), "/** @var Bar&Baz&F^oo $intersectionType */", "class ^Foo {");
    }

    public void testDNFType_Vardoc_a06() throws Exception {
        checkDeclaration(getTestPath(), "/** @var ?F^oo $nullableType */", "class ^Foo {");
    }

    public void testDNFType_Vardoc_b01() throws Exception {
        checkDeclaration(getTestPath(), "/** @var (Foo&Ba^r)|Foo|(Bar&Baz&Foo) $vardoc1 */", "class ^Bar {");
    }

    public void testDNFType_Vardoc_b02() throws Exception {
        checkDeclaration(getTestPath(), "/** @var (Foo&Bar)|Foo|(^Bar&Baz&Foo) $vardoc1 */", "class ^Bar {");
    }

    public void testDNFType_Vardoc_b03() throws Exception {
        checkDeclaration(getTestPath(), "/* @var $vardoc2 (Foo&Ba^r)|Baz */", "class ^Bar {");
    }

    public void testDNFType_Vardoc_b04() throws Exception {
        checkDeclaration(getTestPath(), "/** @var B^ar|Baz|Foo $unionType */", "class ^Bar {");
    }

    public void testDNFType_Vardoc_b05() throws Exception {
        checkDeclaration(getTestPath(), "/** @var Ba^r&Baz&Foo $intersectionType */", "class ^Bar {");
    }

    public void testDNFType_Vardoc_c01() throws Exception {
        checkDeclaration(getTestPath(), "/** @var (Foo&Bar)|Foo|(Bar&Baz^&Foo) $vardoc1 */", "class ^Baz {");
    }

    public void testDNFType_Vardoc_c02() throws Exception {
        checkDeclaration(getTestPath(), "/* @var $vardoc2 (Foo&Bar)|B^az */", "class ^Baz {");
    }

    public void testDNFType_Vardoc_c03() throws Exception {
        checkDeclaration(getTestPath(), "/** @var Bar|Ba^z|Foo $unionType */", "class ^Baz {");
    }

    public void testDNFType_Vardoc_c04() throws Exception {
        checkDeclaration(getTestPath(), "/** @var Bar&Ba^z&Foo $intersectionType */", "class ^Baz {");
    }

    public void testDNFType_01() throws Exception {
        checkDeclaration(getTestPath(), "        $this->fieldClass::CONSTANT_FO^O;", "    public const ^CONSTANT_FOO = \"test\";");
    }

    public void testDNFType_02() throws Exception {
        checkDeclaration(getTestPath(), "        $this->fieldClass::$staticFieldB^az;", "    public static (Foo&Bar)|Bar $^staticFieldBaz;");
    }

    public void testDNFType_03() throws Exception {
        checkDeclaration(getTestPath(), "        $this->fieldClass->fieldB^ar;", "    public (Foo&Bar)|(Baz&Foo) $^fieldBar;");
    }

    public void testDNFType_04() throws Exception {
        checkDeclaration(getTestPath(), "        $test::CONSTANT_BA^Z;", "    public const ^CONSTANT_BAZ = \"test\";");
    }

    public void testDNFType_05() throws Exception {
        checkDeclaration(getTestPath(), "        $test::$staticFieldB^ar;", "    public static (Foo&Bar)|Bar $^staticFieldBar;");
    }

    public void testDNFType_06() throws Exception {
        checkDeclaration(getTestPath(), "        self::$staticFieldClass->methodB^ar();", "    public function ^methodBar(): (Foo&Bar)|Baz {}");
    }

    public void testDNFType_07() throws Exception {
        checkDeclaration(getTestPath(), "        self::$staticFieldClass::staticmethodBa^z(null);", "    public static function ^staticMethodBaz(Foo&Bar $param): void {}");
    }

}
