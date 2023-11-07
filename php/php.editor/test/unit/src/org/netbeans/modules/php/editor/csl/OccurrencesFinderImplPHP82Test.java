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

public class OccurrencesFinderImplPHP82Test extends OccurrencesFinderImplTestBase {

    public OccurrencesFinderImplPHP82Test(String testName) {
        super(testName);
    }

    @Override
    protected String getBaseTestFolderPath() {
        return super.getBaseTestFolderPath() + "php82/";
    }

    public void testConstantsInTraits_01a() throws Exception {
        checkOccurrences(getTestPath(), "    const IMPLICIT^_PUBLIC_TRAIT = 'ExampleTrait implicit public';", true);
    }

    public void testConstantsInTraits_01b() throws Exception {
        checkOccurrences(getTestPath(), "       echo self::IMPLICIT_PUBLIC^_TRAIT . PHP_EOL;", true);
    }

    public void testConstantsInTraits_01c() throws Exception {
        checkOccurrences(getTestPath(), "        echo self::IMPLICIT_P^UBLIC_TRAIT . PHP_EOL; // use", true);
    }

    public void testConstantsInTraits_01d() throws Exception {
        checkOccurrences(getTestPath(), "        echo self::IMPLICIT_P^UBLIC_TRAIT . PHP_EOL; // class", true);
    }

    public void testConstantsInTraits_01e() throws Exception {
        checkOccurrences(getTestPath(), "        echo self::IMPLI^CIT_PUBLIC_TRAIT . PHP_EOL; // child", true);
    }

    public void testConstantsInTraits_01f() throws Exception {
        checkOccurrences(getTestPath(), "echo ExampleClass::IMPLI^CIT_PUBLIC_TRAIT . PHP_EOL;", true);
    }

    public void testConstantsInTraits_02a() throws Exception {
        checkOccurrences(getTestPath(), "    private const PR^IVATE_TRAIT = 'ExampleTrait private';", true);
    }

    public void testConstantsInTraits_02b() throws Exception {
        checkOccurrences(getTestPath(), "        echo static::PRIV^ATE_TRAIT . PHP_EOL;", true);
    }

    public void testConstantsInTraits_02c() throws Exception {
        checkOccurrences(getTestPath(), "        echo static::PRIV^ATE_TRAIT . PHP_EOL; // use", true);
    }

    public void testConstantsInTraits_02d() throws Exception {
        checkOccurrences(getTestPath(), "        echo static::PR^IVATE_TRAIT . PHP_EOL; // class", true);
    }

    public void testConstantsInTraits_03a() throws Exception {
        checkOccurrences(getTestPath(), "    protected const PROTECTED_TRA^IT = 'ExampleTrait protected';", true);
    }

    public void testConstantsInTraits_03b() throws Exception {
        checkOccurrences(getTestPath(), "        echo $this::PROT^ECTED_TRAIT . PHP_EOL;", true);
    }

    public void testConstantsInTraits_03c() throws Exception {
        checkOccurrences(getTestPath(), "        echo $this::PROTE^CTED_TRAIT . PHP_EOL; // use", true);
    }

    public void testConstantsInTraits_03d() throws Exception {
        checkOccurrences(getTestPath(), "        echo $this::PROTECT^ED_TRAIT . PHP_EOL; // class", true);
    }

    public void testConstantsInTraits_04a() throws Exception {
        checkOccurrences(getTestPath(), "    public const PUBLIC^_TRAIT = 'ExampleTrait public';", true);
    }

    public void testConstantsInTraits_04b() throws Exception {
        checkOccurrences(getTestPath(), "        echo parent::PU^BLIC_TRAIT . PHP_EOL; // child", true);
    }

    public void testDNFType_a01() throws Exception {
        checkOccurrences(getTestPath(), "class F^oo {", true);
    }

    public void testDNFType_a02() throws Exception {
        checkOccurrences(getTestPath(), "    public (Fo^o&Bar)|Foo $fieldFoo;", true);
    }

    public void testDNFType_a03() throws Exception {
        checkOccurrences(getTestPath(), "    public (Foo&Bar)|F^oo $fieldFoo;", true);
    }

    public void testDNFType_a04() throws Exception {
        checkOccurrences(getTestPath(), "    public static (F^oo&Bar)|Bar $staticFieldFoo;", true);
    }

    public void testDNFType_a05() throws Exception {
        checkOccurrences(getTestPath(), "    public function methodFoo(): (Fo^o&Bar)|(Bar&Baz) {}", true);
    }

    public void testDNFType_a06() throws Exception {
        checkOccurrences(getTestPath(), "    public static function staticMethodFoo(F^oo|Bar $param): void {}", true);
    }

    public void testDNFType_a07() throws Exception {
        checkOccurrences(getTestPath(), "    public (Fo^o&Bar)|(Baz&Foo) $fieldBar;", true);
    }

    public void testDNFType_a08() throws Exception {
        checkOccurrences(getTestPath(), "    public (Foo&Bar)|(Baz&Fo^o) $fieldBar;", true);
    }

    public void testDNFType_a09() throws Exception {
        checkOccurrences(getTestPath(), "    public static (F^oo&Bar)|Bar $staticFieldBar;", true);
    }

    public void testDNFType_a10() throws Exception {
        checkOccurrences(getTestPath(), "    public function methodBar(): (F^oo&Bar)|Baz {}", true);
    }

    public void testDNFType_a11() throws Exception {
        checkOccurrences(getTestPath(), "    public static function staticMethodBar(Fo^o|Bar $param): void {}", true);
    }

    public void testDNFType_a12() throws Exception {
        checkOccurrences(getTestPath(), "    public (Fo^o&Bar)|(Baz&Foo) $fieldBaz;", true);
    }

    public void testDNFType_a13() throws Exception {
        checkOccurrences(getTestPath(), "    public (Foo&Bar)|(Baz&Fo^o) $fieldBaz;", true);
    }

    public void testDNFType_a14() throws Exception {
        checkOccurrences(getTestPath(), "    public static (Fo^o&Bar)|Bar $staticFieldBaz;", true);
    }

    public void testDNFType_a15() throws Exception {
        checkOccurrences(getTestPath(), "    public function methodBaz(): Fo^o|(Bar&Baz) {}", true);
    }

    public void testDNFType_a16() throws Exception {
        checkOccurrences(getTestPath(), "    public static function staticMethodBaz(Fo^o&Bar $param): void {}", true);
    }

    public void testDNFType_a17() throws Exception {
        checkOccurrences(getTestPath(), "function paramType((Fo^o&Bar)|Baz $param1, Baz|(Foo&Bar) $param2, (Foo&Bar)|(Bar&Baz) $param3): void {", true);
    }

    public void testDNFType_a18() throws Exception {
        checkOccurrences(getTestPath(), "function paramType((Foo&Bar)|Baz $param1, Baz|(Fo^o&Bar) $param2, (Foo&Bar)|(Bar&Baz) $param3): void {", true);
    }

    public void testDNFType_a19() throws Exception {
        checkOccurrences(getTestPath(), "function paramType((Foo&Bar)|Baz $param1, Baz|(Foo&Bar) $param2, (F^oo&Bar)|(Bar&Baz) $param3): void {", true);
    }

    public void testDNFType_a20() throws Exception {
        checkOccurrences(getTestPath(), "function returnType1(): (Fo^o&Bar)|Baz {}", true);
    }

    public void testDNFType_a21() throws Exception {
        checkOccurrences(getTestPath(), "function returnType2(): Baz|(F^oo&Bar) {}", true);
    }

    public void testDNFType_a22() throws Exception {
        checkOccurrences(getTestPath(), "function returnType3(): Baz|(Fo^o&Bar)|Foo {}", true);
    }

    public void testDNFType_a23() throws Exception {
        checkOccurrences(getTestPath(), "function returnType3(): Baz|(Foo&Bar)|^Foo {}", true);
    }

    public void testDNFType_a24() throws Exception {
        checkOccurrences(getTestPath(), "function returnType4(): (Fo^o&Bar)|(Foo&Baz) {}", true);
    }

    public void testDNFType_a25() throws Exception {
        checkOccurrences(getTestPath(), "function returnType4(): (Foo&Bar)|(Fo^o&Baz) {}", true);
    }

    public void testDNFType_a26() throws Exception {
        checkOccurrences(getTestPath(), " * @method (Fo^o&Bar)|(Bar&Baz) methodTag((Foo&Bar)|Bar $param1, Foo|(Bar&Baz) $param2) Description", true);
    }

    public void testDNFType_a27() throws Exception {
        checkOccurrences(getTestPath(), " * @method (Foo&Bar)|(Bar&Baz) methodTag((Fo^o&Bar)|Bar $param1, Foo|(Bar&Baz) $param2) Description", true);
    }

    public void testDNFType_a28() throws Exception {
        checkOccurrences(getTestPath(), " * @method (Foo&Bar)|(Bar&Baz) methodTag((Foo&Bar)|Bar $param1, F^oo|(Bar&Baz) $param2) Description", true);
    }

    public void testDNFType_a29() throws Exception {
        checkOccurrences(getTestPath(), " * @method static (F^oo&Bar)|Baz staticMethodTag(Bar|(Foo&Bar) $param1, (Foo&Bar)|(Bar&Baz) $param2) Description", true);
    }

    public void testDNFType_a30() throws Exception {
        checkOccurrences(getTestPath(), " * @method static (Foo&Bar)|Baz staticMethodTag(Bar|(F^oo&Bar) $param1, (Foo&Bar)|(Bar&Baz) $param2) Description", true);
    }

    public void testDNFType_a31() throws Exception {
        checkOccurrences(getTestPath(), " * @method static (Foo&Bar)|Baz staticMethodTag(Bar|(Foo&Bar) $param1, (^Foo&Bar)|(Bar&Baz) $param2) Description", true);
    }

    public void testDNFType_a32() throws Exception {
        checkOccurrences(getTestPath(), " * @property Fo^o|(Bar&Baz) $propertyTag Description", true);
    }

    public void testDNFType_a33() throws Exception {
        checkOccurrences(getTestPath(), "    private (Fo^o&Bar)|Baz $fieldClass; // class", true);
    }

    public void testDNFType_a34() throws Exception {
        checkOccurrences(getTestPath(), "    public function paramType((F^oo&Baz)|(Foo&Bar)|Baz $test): void { // class", true);
    }

    public void testDNFType_a35() throws Exception {
        checkOccurrences(getTestPath(), "    public function paramType((Foo&Baz)|(F^oo&Bar)|Baz $test): void { // class", true);
    }

    public void testDNFType_a36() throws Exception {
        checkOccurrences(getTestPath(), "    public function returnType(): (Fo^o&Bar)|Baz { // class", true);
    }

    public void testDNFType_a37() throws Exception {
        checkOccurrences(getTestPath(), "    private (Fo^o&Bar)|Baz $test; // trait", true);
    }

    public void testDNFType_a38() throws Exception {
        checkOccurrences(getTestPath(), "    public function paramType((Fo^o&Bar)|(Bar&Baz) $test1, Foo|(Foo&Bar) $test2): void { // trait", true);
    }

    public void testDNFType_a39() throws Exception {
        checkOccurrences(getTestPath(), "    public function paramType((Foo&Bar)|(Bar&Baz) $test1, F^oo|(Foo&Bar) $test2): void { // trait", true);
    }

    public void testDNFType_a40() throws Exception {
        checkOccurrences(getTestPath(), "    public function paramType((Foo&Bar)|(Bar&Baz) $test1, Foo|(Fo^o&Bar) $test2): void { // trait", true);
    }

    public void testDNFType_a41() throws Exception {
        checkOccurrences(getTestPath(), "    public function returnType(): Fo^o|(Foo&Bar) { // trait", true);
    }

    public void testDNFType_a42() throws Exception {
        checkOccurrences(getTestPath(), "    public function returnType(): Foo|(Fo^o&Bar) { // trait", true);
    }

    public void testDNFType_a43() throws Exception {
        checkOccurrences(getTestPath(), "    public function paramType(Fo^o|(Foo&Bar)|null $test);", true);
    }

    public void testDNFType_a44() throws Exception {
        checkOccurrences(getTestPath(), "    public function paramType(Foo|(Fo^o&Bar)|null $test);", true);
    }

    public void testDNFType_a45() throws Exception {
        checkOccurrences(getTestPath(), "    public function returnType(): (Fo^o&Bar)|(Bar&Baz);", true);
    }

    public void testDNFType_a46() throws Exception {
        checkOccurrences(getTestPath(), "$closure = function(Fo^o|(Foo&Bar)|(Bar&Baz) $test1, $test2): void {};", true);
    }

    public void testDNFType_a47() throws Exception {
        checkOccurrences(getTestPath(), "$closure = function(Foo|(Fo^o&Bar)|(Bar&Baz) $test1, $test2): void {};", true);
    }

    public void testDNFType_a48() throws Exception {
        checkOccurrences(getTestPath(), "$closure = function(int $test): (Fo^o&Bar)|null {};", true);
    }

    public void testDNFType_a49() throws Exception {
        checkOccurrences(getTestPath(), "$arrow = fn(F^oo|Bar|(Foo&Bar) $test) => $test;", true);
    }

    public void testDNFType_a50() throws Exception {
        checkOccurrences(getTestPath(), "$arrow = fn(Foo|Bar|(F^oo&Bar) $test) => $test;", true);
    }

    public void testDNFType_a51() throws Exception {
        checkOccurrences(getTestPath(), "$arrow = fn((F^oo&Bar)|null $test): Foo|(Foo&Bar) => $test;", true);
    }

    public void testDNFType_a52() throws Exception {
        checkOccurrences(getTestPath(), "$arrow = fn((Foo&Bar)|null $test): Fo^o|(Foo&Bar) => $test;", true);
    }

    public void testDNFType_a53() throws Exception {
        checkOccurrences(getTestPath(), "$arrow = fn((Foo&Bar)|null $test): Foo|(F^oo&Bar) => $test;", true);
    }

    public void testDNFType_a54() throws Exception {
        checkOccurrences(getTestPath(), "/** @var (Fo^o&Bar)|Foo|(Bar&Baz&Foo) $vardoc1 */", true);
    }

    public void testDNFType_a55() throws Exception {
        checkOccurrences(getTestPath(), "/** @var (Foo&Bar)|F^oo|(Bar&Baz&Foo) $vardoc1 */", true);
    }

    public void testDNFType_a56() throws Exception {
        checkOccurrences(getTestPath(), "/** @var (Foo&Bar)|Foo|(Bar&Baz&Fo^o) $vardoc1 */", true);
    }

    public void testDNFType_a57() throws Exception {
        checkOccurrences(getTestPath(), "/* @var $vardoc2 (Fo^o&Bar)|Baz */", true);
    }

    public void testDNFType_a58() throws Exception {
        checkOccurrences(getTestPath(), "/** @var Bar|Baz|F^oo $unionType */", true);
    }

    public void testDNFType_a59() throws Exception {
        checkOccurrences(getTestPath(), "/** @var Bar&Baz&Fo^o $intersectionType */", true);
    }

    public void testDNFType_a60() throws Exception {
        checkOccurrences(getTestPath(), "/** @var ?Fo^o $nullableType */", true);
    }

    public void testDNFType_b01() throws Exception {
        checkOccurrences(getTestPath(), "    public (Foo&Ba^r)|Foo $fieldFoo;", true);
    }

    public void testDNFType_b02() throws Exception {
        checkOccurrences(getTestPath(), "    public static (Foo&Ba^r)|Bar $staticFieldFoo;", true);
    }

    public void testDNFType_b03() throws Exception {
        checkOccurrences(getTestPath(), "    public static (Foo&Bar)|B^ar $staticFieldFoo;", true);
    }

    public void testDNFType_b04() throws Exception {
        checkOccurrences(getTestPath(), "    public function methodFoo(): (Foo&Ba^r)|(Bar&Baz) {}", true);
    }

    public void testDNFType_b05() throws Exception {
        checkOccurrences(getTestPath(), "    public function methodFoo(): (Foo&Bar)|(Bar^&Baz) {}", true);
    }

    public void testDNFType_b06() throws Exception {
        checkOccurrences(getTestPath(), "    public static function staticMethodFoo(Foo|Ba^r $param): void {}", true);
    }

    public void testDNFType_b07() throws Exception {
        checkOccurrences(getTestPath(), "class B^ar {", true);
    }

    public void testDNFType_b08() throws Exception {
        checkOccurrences(getTestPath(), "    public (Foo&B^ar)|(Baz&Foo) $fieldBar;", true);
    }

    public void testDNFType_b09() throws Exception {
        checkOccurrences(getTestPath(), "    public static (Foo&Ba^r)|Bar $staticFieldBar;", true);
    }

    public void testDNFType_b10() throws Exception {
        checkOccurrences(getTestPath(), "    public static (Foo&Bar)|B^ar $staticFieldBar;", true);
    }

    public void testDNFType_b11() throws Exception {
        checkOccurrences(getTestPath(), "    public function methodBar(): (Foo&Ba^r)|Baz {}", true);
    }

    public void testDNFType_b12() throws Exception {
        checkOccurrences(getTestPath(), "    public static function staticMethodBar(Foo|^Bar $param): void {}", true);
    }

    public void testDNFType_b13() throws Exception {
        checkOccurrences(getTestPath(), "    public (Foo&Ba^r)|(Baz&Foo) $fieldBaz;", true);
    }

    public void testDNFType_b14() throws Exception {
        checkOccurrences(getTestPath(), "    public static (Foo&Ba^r)|Bar $staticFieldBaz;", true);
    }

    public void testDNFType_b15() throws Exception {
        checkOccurrences(getTestPath(), "    public static (Foo&Bar)|Ba^r $staticFieldBaz;", true);
    }

    public void testDNFType_b16() throws Exception {
        checkOccurrences(getTestPath(), "    public function methodBaz(): Foo|(Ba^r&Baz) {}", true);
    }

    public void testDNFType_b17() throws Exception {
        checkOccurrences(getTestPath(), "    public static function staticMethodBaz(Foo&Ba^r $param): void {}", true);
    }

    public void testDNFType_b18() throws Exception {
        checkOccurrences(getTestPath(), "function paramType((Foo&B^ar)|Baz $param1, Baz|(Foo&Bar) $param2, (Foo&Bar)|(Bar&Baz) $param3): void {", true);
    }

    public void testDNFType_b19() throws Exception {
        checkOccurrences(getTestPath(), "function paramType((Foo&Bar)|Baz $param1, Baz|(Foo&B^ar) $param2, (Foo&Bar)|(Bar&Baz) $param3): void {", true);
    }

    public void testDNFType_b20() throws Exception {
        checkOccurrences(getTestPath(), "function paramType((Foo&Bar)|Baz $param1, Baz|(Foo&Bar) $param2, (Foo&Ba^r)|(Bar&Baz) $param3): void {", true);
    }

    public void testDNFType_b21() throws Exception {
        checkOccurrences(getTestPath(), "function paramType((Foo&Bar)|Baz $param1, Baz|(Foo&Bar) $param2, (Foo&Bar)|(Ba^r&Baz) $param3): void {", true);
    }

    public void testDNFType_b22() throws Exception {
        checkOccurrences(getTestPath(), "function returnType1(): (Foo&B^ar)|Baz {}", true);
    }

    public void testDNFType_b23() throws Exception {
        checkOccurrences(getTestPath(), "function returnType2(): Baz|(Foo&B^ar) {}", true);
    }

    public void testDNFType_b24() throws Exception {
        checkOccurrences(getTestPath(), "function returnType3(): Baz|(Foo&Ba^r)|Foo {}", true);
    }

    public void testDNFType_b25() throws Exception {
        checkOccurrences(getTestPath(), "function returnType4(): (Foo&B^ar)|(Foo&Baz) {}", true);
    }

    public void testDNFType_b26() throws Exception {
        checkOccurrences(getTestPath(), " * @method (Foo&B^ar)|(Bar&Baz) methodTag((Foo&Bar)|Bar $param1, Foo|(Bar&Baz) $param2) Description", true);
    }

    public void testDNFType_b27() throws Exception {
        checkOccurrences(getTestPath(), " * @method (Foo&Bar)|(Ba^r&Baz) methodTag((Foo&Bar)|Bar $param1, Foo|(Bar&Baz) $param2) Description", true);
    }

    public void testDNFType_b28() throws Exception {
        checkOccurrences(getTestPath(), " * @method (Foo&Bar)|(Bar&Baz) methodTag((Foo&B^ar)|Bar $param1, Foo|(Bar&Baz) $param2) Description", true);
    }

    public void testDNFType_b29() throws Exception {
        checkOccurrences(getTestPath(), " * @method (Foo&Bar)|(Bar&Baz) methodTag((Foo&Bar)|Ba^r $param1, Foo|(Bar&Baz) $param2) Description", true);
    }

    public void testDNFType_b30() throws Exception {
        checkOccurrences(getTestPath(), " * @method (Foo&Bar)|(Bar&Baz) methodTag((Foo&Bar)|Bar $param1, Foo|(^Bar&Baz) $param2) Description", true);
    }

    public void testDNFType_b31() throws Exception {
        checkOccurrences(getTestPath(), " * @method static (Foo&Ba^r)|Baz staticMethodTag(Bar|(Foo&Bar) $param1, (Foo&Bar)|(Bar&Baz) $param2) Description", true);
    }

    public void testDNFType_b32() throws Exception {
        checkOccurrences(getTestPath(), " * @method static (Foo&Bar)|Baz staticMethodTag(B^ar|(Foo&Bar) $param1, (Foo&Bar)|(Bar&Baz) $param2) Description", true);
    }

    public void testDNFType_b33() throws Exception {
        checkOccurrences(getTestPath(), " * @method static (Foo&Bar)|Baz staticMethodTag(Bar|(Foo&B^ar) $param1, (Foo&Bar)|(Bar&Baz) $param2) Description", true);
    }

    public void testDNFType_b34() throws Exception {
        checkOccurrences(getTestPath(), " * @method static (Foo&Bar)|Baz staticMethodTag(Bar|(Foo&Bar) $param1, (Foo&^Bar)|(Bar&Baz) $param2) Description", true);
    }

    public void testDNFType_b35() throws Exception {
        checkOccurrences(getTestPath(), " * @method static (Foo&Bar)|Baz staticMethodTag(Bar|(Foo&Bar) $param1, (Foo&Bar)|(Ba^r&Baz) $param2) Description", true);
    }

    public void testDNFType_b36() throws Exception {
        checkOccurrences(getTestPath(), " * @property Foo|(B^ar&Baz) $propertyTag Description", true);
    }

    public void testDNFType_b37() throws Exception {
        checkOccurrences(getTestPath(), "    private (Foo&B^ar)|Baz $fieldClass; // class", true);
    }

    public void testDNFType_b38() throws Exception {
        checkOccurrences(getTestPath(), "    private static B^ar|(Bar&Baz) $staticFieldClass; // class", true);
    }

    public void testDNFType_b39() throws Exception {
        checkOccurrences(getTestPath(), "    private static Bar|(Ba^r&Baz) $staticFieldClass; // class", true);
    }

    public void testDNFType_b40() throws Exception {
        checkOccurrences(getTestPath(), "    public function paramType((Foo&Baz)|(Foo&B^ar)|Baz $test): void { // class", true);
    }

    public void testDNFType_b41() throws Exception {
        checkOccurrences(getTestPath(), "    public function returnType(): (Foo&B^ar)|Baz { // class", true);
    }

    public void testDNFType_b42() throws Exception {
        checkOccurrences(getTestPath(), "    private (Foo&B^ar)|Baz $test; // trait", true);
    }

    public void testDNFType_b43() throws Exception {
        checkOccurrences(getTestPath(), "    public function paramType((Foo&Ba^r)|(Bar&Baz) $test1, Foo|(Foo&Bar) $test2): void { // trait", true);
    }

    public void testDNFType_b44() throws Exception {
        checkOccurrences(getTestPath(), "    public function paramType((Foo&Bar)|(B^ar&Baz) $test1, Foo|(Foo&Bar) $test2): void { // trait", true);
    }

    public void testDNFType_b45() throws Exception {
        checkOccurrences(getTestPath(), "    public function paramType((Foo&Bar)|(Bar&Baz) $test1, Foo|(Foo&^Bar) $test2): void { // trait", true);
    }

    public void testDNFType_b46() throws Exception {
        checkOccurrences(getTestPath(), "    public function returnType(): Foo|(Foo&B^ar) { // trait", true);
    }

    public void testDNFType_b47() throws Exception {
        checkOccurrences(getTestPath(), "    public function paramType(Foo|(Foo&B^ar)|null $test);", true);
    }

    public void testDNFType_b48() throws Exception {
        checkOccurrences(getTestPath(), "    public function returnType(): (Foo&Bar^)|(Bar&Baz);", true);
    }

    public void testDNFType_b49() throws Exception {
        checkOccurrences(getTestPath(), "    public function returnType(): (Foo&Bar)|(B^ar&Baz);", true);
    }

    public void testDNFType_b50() throws Exception {
        checkOccurrences(getTestPath(), "$closure = function(Foo|(Foo&B^ar)|(Bar&Baz) $test1, $test2): void {};", true);
    }

    public void testDNFType_b51() throws Exception {
        checkOccurrences(getTestPath(), "$closure = function(Foo|(Foo&Bar)|(B^ar&Baz) $test1, $test2): void {};", true);
    }

    public void testDNFType_b52() throws Exception {
        checkOccurrences(getTestPath(), "$closure = function(int $test): (Foo&B^ar)|null {};", true);
    }

    public void testDNFType_b53() throws Exception {
        checkOccurrences(getTestPath(), "$arrow = fn(Foo|B^ar|(Foo&Bar) $test) => $test;", true);
    }

    public void testDNFType_b54() throws Exception {
        checkOccurrences(getTestPath(), "$arrow = fn(Foo|Bar|(Foo&B^ar) $test) => $test;", true);
    }

    public void testDNFType_b55() throws Exception {
        checkOccurrences(getTestPath(), "$arrow = fn((Foo&B^ar)|null $test): Foo|(Foo&Bar) => $test;", true);
    }

    public void testDNFType_b56() throws Exception {
        checkOccurrences(getTestPath(), "$arrow = fn((Foo&Bar)|null $test): Foo|(Foo&Ba^r) => $test;", true);
    }

    public void testDNFType_b57() throws Exception {
        checkOccurrences(getTestPath(), "/** @var (Foo&^Bar)|Foo|(Bar&Baz&Foo) $vardoc1 */", true);
    }

    public void testDNFType_b58() throws Exception {
        checkOccurrences(getTestPath(), "/** @var (Foo&Bar)|Foo|(Ba^r&Baz&Foo) $vardoc1 */", true);
    }

    public void testDNFType_b59() throws Exception {
        checkOccurrences(getTestPath(), "/* @var $vardoc2 (Foo&Ba^r)|Baz */", true);
    }

    public void testDNFType_b60() throws Exception {
        checkOccurrences(getTestPath(), "/** @var Ba^r|Baz|Foo $unionType */", true);
    }

    public void testDNFType_b61() throws Exception {
        checkOccurrences(getTestPath(), "/** @var B^ar&Baz&Foo $intersectionType */", true);
    }

    public void testDNFType_c01() throws Exception {
        checkOccurrences(getTestPath(), "    public function methodFoo(): (Foo&Bar)|(Bar&B^az) {}", true);
    }

    public void testDNFType_c02() throws Exception {
        checkOccurrences(getTestPath(), "    public (Foo&Bar)|(Ba^z&Foo) $fieldBar;", true);
    }

    public void testDNFType_c03() throws Exception {
        checkOccurrences(getTestPath(), "    public function methodBar(): (Foo&Bar)|B^az {}", true);
    }

    public void testDNFType_c04() throws Exception {
        checkOccurrences(getTestPath(), "class B^az {", true);
    }

    public void testDNFType_c05() throws Exception {
        checkOccurrences(getTestPath(), "    public (Foo&Bar)|(Ba^z&Foo) $fieldBaz;", true);
    }

    public void testDNFType_c06() throws Exception {
        checkOccurrences(getTestPath(), "    public function methodBaz(): Foo|(Bar&Ba^z) {}", true);
    }

    public void testDNFType_c07() throws Exception {
        checkOccurrences(getTestPath(), "function paramType((Foo&Bar)|B^az $param1, Baz|(Foo&Bar) $param2, (Foo&Bar)|(Bar&Baz) $param3): void {", true);
    }

    public void testDNFType_c08() throws Exception {
        checkOccurrences(getTestPath(), "function paramType((Foo&Bar)|Baz $param1, B^az|(Foo&Bar) $param2, (Foo&Bar)|(Bar&Baz) $param3): void {", true);
    }

    public void testDNFType_c09() throws Exception {
        checkOccurrences(getTestPath(), "function paramType((Foo&Bar)|Baz $param1, Baz|(Foo&Bar) $param2, (Foo&Bar)|(Bar&^Baz) $param3): void {", true);
    }

    public void testDNFType_c10() throws Exception {
        checkOccurrences(getTestPath(), "function returnType1(): (Foo&Bar)|B^az {}", true);
    }

    public void testDNFType_c11() throws Exception {
        checkOccurrences(getTestPath(), "function returnType2(): Ba^z|(Foo&Bar) {}", true);
    }

    public void testDNFType_c12() throws Exception {
        checkOccurrences(getTestPath(), "function returnType3(): B^az|(Foo&Bar)|Foo {}", true);
    }

    public void testDNFType_c13() throws Exception {
        checkOccurrences(getTestPath(), "function returnType4(): (Foo&Bar)|(Foo&Ba^z) {}", true);
    }

    public void testDNFType_c14() throws Exception {
        checkOccurrences(getTestPath(), " * @method (Foo&Bar)|(Bar&B^az) methodTag((Foo&Bar)|Bar $param1, Foo|(Bar&Baz) $param2) Description", true);
    }

    public void testDNFType_c15() throws Exception {
        checkOccurrences(getTestPath(), " * @method (Foo&Bar)|(Bar&Baz) methodTag((Foo&Bar)|Bar $param1, Foo|(Bar&Ba^z) $param2) Description", true);
    }

    public void testDNFType_c16() throws Exception {
        checkOccurrences(getTestPath(), " * @method static (Foo&Bar)|B^az staticMethodTag(Bar|(Foo&Bar) $param1, (Foo&Bar)|(Bar&Baz) $param2) Description", true);
    }

    public void testDNFType_c17() throws Exception {
        checkOccurrences(getTestPath(), " * @method static (Foo&Bar)|Baz staticMethodTag(Bar|(Foo&Bar) $param1, (Foo&Bar)|(Bar&^Baz) $param2) Description", true);
    }

    public void testDNFType_c18() throws Exception {
        checkOccurrences(getTestPath(), " * @property Foo|(Bar&B^az) $propertyTag Description", true);
    }

    public void testDNFType_c19() throws Exception {
        checkOccurrences(getTestPath(), "    private (Foo&Bar)|B^az $fieldClass; // class", true);
    }

    public void testDNFType_c20() throws Exception {
        checkOccurrences(getTestPath(), "    private static Bar|(Bar&Ba^z) $staticFieldClass; // class", true);
    }

    public void testDNFType_c21() throws Exception {
        checkOccurrences(getTestPath(), "    public function paramType((Foo&Ba^z)|(Foo&Bar)|Baz $test): void { // class", true);
    }

    public void testDNFType_c22() throws Exception {
        checkOccurrences(getTestPath(), "    public function paramType((Foo&Baz)|(Foo&Bar)|B^az $test): void { // class", true);
    }

    public void testDNFType_c23() throws Exception {
        checkOccurrences(getTestPath(), "    public function returnType(): (Foo&Bar)|B^az { // class", true);
    }

    public void testDNFType_c24() throws Exception {
        checkOccurrences(getTestPath(), "    private (Foo&Bar)|B^az $test; // trait", true);
    }

    public void testDNFType_c25() throws Exception {
        checkOccurrences(getTestPath(), "    public function paramType((Foo&Bar)|(Bar&Ba^z) $test1, Foo|(Foo&Bar) $test2): void { // trait", true);
    }

    public void testDNFType_c26() throws Exception {
        checkOccurrences(getTestPath(), "    public function returnType(): (Foo&Bar)|(Bar&^Baz);", true);
    }

    public void testDNFType_c27() throws Exception {
        checkOccurrences(getTestPath(), "$closure = function(Foo|(Foo&Bar)|(Bar&B^az) $test1, $test2): void {};", true);
    }

    public void testDNFType_c28() throws Exception {
        checkOccurrences(getTestPath(), "/** @var (Foo&Bar)|Foo|(Bar&Ba^z&Foo) $vardoc1 */", true);
    }

    public void testDNFType_c29() throws Exception {
        checkOccurrences(getTestPath(), "/* @var $vardoc2 (Foo&Bar)|^Baz */", true);
    }

    public void testDNFType_c30() throws Exception {
        checkOccurrences(getTestPath(), "/** @var Bar|Ba^z|Foo $unionType */", true);
    }

    public void testDNFType_c31() throws Exception {
        checkOccurrences(getTestPath(), "/** @var Bar&B^az&Foo $intersectionType */", true);
    }

    public void testDNFType_d01() throws Exception {
        checkOccurrences(getTestPath(), "    public const CONSTAN^T_FOO = \"test\";", true);
    }

    public void testDNFType_d02() throws Exception {
        checkOccurrences(getTestPath(), "        $this->fieldClass::CONSTANT_F^OO;", true);
    }

    public void testDNFType_e01() throws Exception {
        checkOccurrences(getTestPath(), "    public static (Foo&Bar)|Bar $stat^icFieldBaz;", true);
    }

    public void testDNFType_e02() throws Exception {
        checkOccurrences(getTestPath(), "        $this->fieldClass::$staticFi^eldBaz;", true);
    }

    public void testDNFType_f01() throws Exception {
        checkOccurrences(getTestPath(), "    public (Foo&Bar)|(Baz&Foo) $field^Bar;", true);
    }

    public void testDNFType_f02() throws Exception {
        checkOccurrences(getTestPath(), "        $this->fieldClass->fieldB^ar;", true);
    }

    public void testDNFType_g01() throws Exception {
        checkOccurrences(getTestPath(), "    public const CON^STANT_BAZ = \"test\";", true);
    }

    public void testDNFType_g02() throws Exception {
        checkOccurrences(getTestPath(), "        $test::CONSTANT^_BAZ;", true);
    }

    public void testDNFType_h01() throws Exception {
        checkOccurrences(getTestPath(), "    public static (Foo&Bar)|Bar $staticF^ieldBar;", true);
    }

    public void testDNFType_h02() throws Exception {
        checkOccurrences(getTestPath(), "        $test::$staticFi^eldBar;", true);
    }

    public void testDNFType_i01() throws Exception {
        checkOccurrences(getTestPath(), "    public function metho^dBar(): (Foo&Bar)|Baz {}", true);
    }

    public void testDNFType_i02() throws Exception {
        checkOccurrences(getTestPath(), "        self::$staticFieldClass->metho^dBar();", true);
    }

    public void testDNFType_j01() throws Exception {
        checkOccurrences(getTestPath(), "    public static function staticMethodB^az(Foo&Bar $param): void {}", true);
    }

    public void testDNFType_j02() throws Exception {
        checkOccurrences(getTestPath(), "        self::$staticFieldClass::staticmethod^Baz(null);", true);
    }

    public void testDNFType_j03() throws Exception {
        checkOccurrences(getTestPath(), "$vardoc2::staticMethodB^az(null);", true);
    }

    public void testDNFType_k01() throws Exception {
        checkOccurrences(getTestPath(), "    public function method^Foo(): (Foo&Bar)|(Bar&Baz) {}", true);
    }

    public void testDNFType_k02() throws Exception {
        checkOccurrences(getTestPath(), "$vardoc1->method^Foo();", true);
    }

    public void testDNFType_k03() throws Exception {
        checkOccurrences(getTestPath(), "$unionType->methodF^oo();", true);
    }

    public void testDNFType_k04() throws Exception {
        checkOccurrences(getTestPath(), "$intersectionType->metho^dFoo();", true);
    }

    public void testDNFType_k05() throws Exception {
        checkOccurrences(getTestPath(), "$nullableType->methodF^oo();", true);
    }
}
