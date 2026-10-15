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

public class OccurrencesFinderImplPHP80Test extends OccurrencesFinderImplTestBase {

    public OccurrencesFinderImplPHP80Test(String testName) {
        super(testName);
    }

    @Override
    protected String getBaseTestFolderPath() {
        return super.getBaseTestFolderPath() + "php80/";
    }

    public void testNonCapturingCatches_01() throws Exception {
        checkOccurrences(getTestPath(), "class Exc^eptionA extends Exception", true);
    }

    public void testNonCapturingCatches_02() throws Exception {
        checkOccurrences(getTestPath(), "} catch (Exceptio^nA) {", true);
    }

    public void testNonCapturingCatches_03() throws Exception {
        checkOccurrences(getTestPath(), "} catch (Ex^ceptionA | ExceptionB)", true);
    }

    public void testNonCapturingCatches_04() throws Exception {
        checkOccurrences(getTestPath(), "class Except^ionB extends Exception", true);
    }

    public void testNonCapturingCatches_05() throws Exception {
        checkOccurrences(getTestPath(), "} catch (ExceptionA | E^xceptionB)", true);
    }

    public void testMatchExpression_01a() throws Exception {
        checkOccurrences(getTestPath(), "class Ma^tchExpression", true);
    }

    public void testMatchExpression_01b() throws Exception {
        checkOccurrences(getTestPath(), "            MatchExpr^ession::START => self::$start,", true);
    }

    public void testMatchExpression_01c() throws Exception {
        checkOccurrences(getTestPath(), "            MatchExpressi^on::SUSPEND => $this->suspend,", true);
    }

    public void testMatchExpression_01d() throws Exception {
        checkOccurrences(getTestPath(), "            MatchExpress^ion::STOP => $this->stopState(),", true);
    }

    public void testMatchExpression_01e() throws Exception {
        checkOccurrences(getTestPath(), "            default =>  Ma^tchExpression::default() . MatchExpression::match . $this->match(),", true);
    }

    public void testMatchExpression_01f() throws Exception {
        checkOccurrences(getTestPath(), "            default =>  MatchExpression::default() . ^MatchExpression::match . $this->match(),", true);
    }

    public void testMatchExpression_01g() throws Exception {
        checkOccurrences(getTestPath(), "$instance = new MatchExpr^ession();", true);
    }

    public void testMatchExpression_01h() throws Exception {
        checkOccurrences(getTestPath(), "    default => MatchExpressi^on::default(),", true);
    }

    public void testMatchExpression_02a() throws Exception {
        checkOccurrences(getTestPath(), "        $sta^te = self::STOP;", true);
    }

    public void testMatchExpression_02b() throws Exception {
        checkOccurrences(getTestPath(), "        return match ($s^tate) {", true);
    }

    public void testMatchExpression_03a() throws Exception {
        checkOccurrences(getTestPath(), "    public const ST^ART = \"start\";", true);
    }

    public void testMatchExpression_03b() throws Exception {
        checkOccurrences(getTestPath(), "            MatchExpression::STA^RT => self::$start,", true);
    }

    public void testMatchExpression_04a() throws Exception {
        checkOccurrences(getTestPath(), "    private static $sta^rt = \"start state\";", true);
    }

    public void testMatchExpression_04b() throws Exception {
        checkOccurrences(getTestPath(), "            MatchExpression::START => self::$st^art,", true);
    }

    public void testMatchExpression_05a() throws Exception {
        checkOccurrences(getTestPath(), "    private $susp^end = \"suspend state\";", true);
    }

    public void testMatchExpression_05b() throws Exception {
        checkOccurrences(getTestPath(), "            MatchExpression::SUSPEND => $this->susp^end,", true);
    }

    public void testMatchExpression_06a() throws Exception {
        checkOccurrences(getTestPath(), "            MatchExpression::STOP => $this->stop^State(),", true);
    }

    public void testMatchExpression_06b() throws Exception {
        checkOccurrences(getTestPath(), "    public function stopSt^ate(): string {", true);
    }

    public void testMatchExpression_07a() throws Exception {
        checkOccurrences(getTestPath(), "            default =>  MatchExpression::defau^lt() . MatchExpression::match . $this->match(),", true);
    }

    public void testMatchExpression_07b() throws Exception {
        checkOccurrences(getTestPath(), "    public static function defau^lt(): string {", true);
    }

    public void testMatchExpression_07c() throws Exception {
        checkOccurrences(getTestPath(), "    default => MatchExpression::defa^ult(),", true);
    }

    public void testMatchExpression_08a() throws Exception {
        checkOccurrences(getTestPath(), "    private const matc^h = \"match\"; // context sensitive lexer", true);
    }

    public void testMatchExpression_08b() throws Exception {
        checkOccurrences(getTestPath(), "            default =>  MatchExpression::default() . MatchExpression::mat^ch . $this->match(),", true);
    }

    public void testMatchExpression_09a() throws Exception {
        checkOccurrences(getTestPath(), "            default =>  MatchExpression::default() . MatchExpression::match . $this->mat^ch(),", true);
    }

    public void testMatchExpression_09b() throws Exception {
        checkOccurrences(getTestPath(), "    public function ma^tch(): string {", true);
    }

    public void testMatchExpression_09c() throws Exception {
        checkOccurrences(getTestPath(), "echo $instance->mat^ch();", true);
    }

    public void testUnionTypes_01a() throws Exception {
        checkOccurrences(getTestPath(), "    private Paren^tClass|ChildClass $field;", true);
    }

    public void testUnionTypes_01b() throws Exception {
        checkOccurrences(getTestPath(), "    public function testMethod(Parent^Class|ChildClass|null $param): ChildClass|\\Union\\Types1\\ParentClass {", true);
    }

    public void testUnionTypes_01c() throws Exception {
        checkOccurrences(getTestPath(), "    public function testMethod(ParentClass|ChildClass|null $param): ChildClass|\\Union\\Types1\\Pare^ntClass {", true);
    }

    public void testUnionTypes_01d() throws Exception {
        checkOccurrences(getTestPath(), "    public static function testStaticMethod(TestClass1|\\Union\\Types2\\TestClass2|null $param1, ChildClass|null $param2): TestClass2|ParentClass^|null {", true);
    }

    public void testUnionTypes_02a() throws Exception {
        checkOccurrences(getTestPath(), "    private ParentClass|ChildCl^ass $field;", true);
    }
    public void testUnionTypes_02b() throws Exception {
        checkOccurrences(getTestPath(), "    public function testMethod(ParentClass|ChildC^lass|null $param): ChildClass|\\Union\\Types1\\ParentClass {", true);
    }

    public void testUnionTypes_02c() throws Exception {
        checkOccurrences(getTestPath(), "    public function testMethod(ParentClass|ChildClass|null $param): Chi^ldClass|\\Union\\Types1\\ParentClass {", true);
    }

    public void testUnionTypes_02d() throws Exception {
        checkOccurrences(getTestPath(), "    public static function testStaticMethod(TestClass1|\\Union\\Types2\\TestClass2|null $param1, ChildCl^ass|null $param2): TestClass2|ParentClass|null {", true);
    }

    public void testUnionTypes_03a() throws Exception {
        checkOccurrences(getTestPath(), "    private static \\Union\\Types2\\TestCla^ss1|TestClass2 $staticField;", true);
    }

    public void testUnionTypes_03b() throws Exception {
        checkOccurrences(getTestPath(), "    public static function testStaticMethod(Te^stClass1|\\Union\\Types2\\TestClass2|null $param1, ChildClass|null $param2): TestClass2|ParentClass|null {", true);
    }

    public void testUnionTypes_03c() throws Exception {
        checkOccurrences(getTestPath(), "    public function traitMethod(TestCla^ss1|TestClass2 $param): TestClass1|TestClass2|null {", true);
    }

    public void testUnionTypes_03d() throws Exception {
        checkOccurrences(getTestPath(), "    public function traitMethod(TestClass1|TestClass2 $param): Test^Class1|TestClass2|null {", true);
    }

    public void testUnionTypes_04a() throws Exception {
        checkOccurrences(getTestPath(), "    private static \\Union\\Types2\\TestClass1|TestCla^ss2 $staticField;", true);
    }

    public void testUnionTypes_04b() throws Exception {
        checkOccurrences(getTestPath(), "    public static function testStaticMethod(TestClass1|\\Union\\Types2\\TestCl^ass2|null $param1, ChildClass|null $param2): TestClass2|ParentClass|null {", true);
    }

    public void testUnionTypes_04c() throws Exception {
        checkOccurrences(getTestPath(), "    public static function testStaticMethod(TestClass1|\\Union\\Types2\\TestClass2|null $param1, ChildClass|null $param2): TestClas^s2|ParentClass|null {", true);
    }

    public void testUnionTypes_04d() throws Exception {
        checkOccurrences(getTestPath(), "    public function traitMethod(TestClass1|TestCla^ss2 $param): TestClass1|TestClass2|null {", true);
    }

    public void testUnionTypes_04e() throws Exception {
        checkOccurrences(getTestPath(), "    public function traitMethod(TestClass1|TestClass2 $param): TestClass1|TestCl^ass2|null {", true);
    }

    public void testNullsafeOperator_01a() throws Exception {
        checkOccurrences(getTestPath(), "    public ?User $^user;", true);
    }

    public void testNullsafeOperator_01b() throws Exception {
        checkOccurrences(getTestPath(), "$country = $session?->use^r?->getAddress()?->country;", true);
    }

    public void testNullsafeOperator_01c() throws Exception {
        checkOccurrences(getTestPath(), "$country = $session?->us^er::$test;", true);
    }

    public void testNullsafeOperator_01d() throws Exception {
        checkOccurrences(getTestPath(), "$country = $session?->use^r::test();", true);
    }

    public void testNullsafeOperator_01e() throws Exception {
        checkOccurrences(getTestPath(), "$country = $session?->^user->id;", true);
    }

    public void testNullsafeOperator_01f() throws Exception {
        checkOccurrences(getTestPath(), "$country = $session?->use^r?->getAddress()::ID;", true);
    }

    public void testNullsafeOperator_02a() throws Exception {
        checkOccurrences(getTestPath(), "    public function getAdd^ress(): ?Address {", true);
    }

    public void testNullsafeOperator_02b() throws Exception {
        checkOccurrences(getTestPath(), "$country = $session?->user?->getAdd^ress()?->country;", true);
    }

    public void testNullsafeOperator_02c() throws Exception {
        checkOccurrences(getTestPath(), "$country = $session?->user?->getAd^dress()::ID;", true);
    }

    public void testNullsafeOperator_02d() throws Exception {
        checkOccurrences(getTestPath(), "$country = User::create(\"test\")?->getAddre^ss()?->country;", true);
    }

    public void testNullsafeOperator_02e() throws Exception {
        checkOccurrences(getTestPath(), "$country = $session->getUser()::create(\"test\")?->getAd^dress()->country;", true);
    }

    public void testNullsafeOperator_02f() throws Exception {
        checkOccurrences(getTestPath(), "$country = (new User(\"test\"))?->getAd^dress()->country;", true);
    }

    public void testNullsafeOperator_03a() throws Exception {
        checkOccurrences(getTestPath(), "    public const I^D = \"Adress\";", true);
    }

    public void testNullsafeOperator_03b() throws Exception {
        checkOccurrences(getTestPath(), "$country = $session?->user?->getAddress()::I^D;", true);
    }

    public void testNullsafeOperator_04a() throws Exception {
        checkOccurrences(getTestPath(), "        $test = $this?->address?->count^ry;", true);
    }

    public void testNullsafeOperator_04b() throws Exception {
        checkOccurrences(getTestPath(), "    public Country $cou^ntry;", true);
    }

    public void testNullsafeOperator_04c() throws Exception {
        checkOccurrences(getTestPath(), "        $this->co^untry = new Country();", true);
    }

    public void testNullsafeOperator_04d() throws Exception {
        checkOccurrences(getTestPath(), "$country = $session?->user?->getAddress()?->coun^try;", true);
    }

    public void testNullsafeOperator_04e() throws Exception {
        checkOccurrences(getTestPath(), "$country = User::create(\"test\")?->getAddress()?->count^ry;", true);
    }

    public void testNullsafeOperator_04f() throws Exception {
        checkOccurrences(getTestPath(), "$country = $session->getUser()::create(\"test\")?->getAddress()->count^ry;", true);
    }

    public void testNullsafeOperator_04g() throws Exception {
        checkOccurrences(getTestPath(), "$country = (new User(\"test\"))?->getAddress()->countr^y;", true);
    }

    public void testNullsafeOperator_05a() throws Exception {
        checkOccurrences(getTestPath(), "    public int $i^d = 1;", true);
    }

    public void testNullsafeOperator_05b() throws Exception {
        checkOccurrences(getTestPath(), "$country = $session?->user->i^d;", true);
    }

    public void testNullsafeOperator_06a() throws Exception {
        checkOccurrences(getTestPath(), "    public static string $t^est = \"test\";", true);
    }

    public void testNullsafeOperator_06b() throws Exception {
        checkOccurrences(getTestPath(), "        return self::$^test;", true);
    }

    public void testNullsafeOperator_06c() throws Exception {
        checkOccurrences(getTestPath(), "$country = $session?->user::$te^st;", true);
    }

    public void testNullsafeOperator_07a() throws Exception {
        checkOccurrences(getTestPath(), "    public static function tes^t(): string {", true);
    }

    public void testNullsafeOperator_07b() throws Exception {
        checkOccurrences(getTestPath(), "$country = $session?->user::te^st();", true);
    }

    public void testNullsafeOperator_08a() throws Exception {
        checkOccurrences(getTestPath(), "    public static function creat^e(string $name): User {", true);
    }

    public void testNullsafeOperator_08b() throws Exception {
        checkOccurrences(getTestPath(), "$country = User::crea^te(\"test\")?->getAddress()?->country;", true);
    }

    public void testNullsafeOperator_08c() throws Exception {
        checkOccurrences(getTestPath(), "$country = $session->getUser()::cre^ate(\"test\")?->getAddress()->country;", true);
    }

    public void testNullsafeOperator_09a() throws Exception {
        checkOccurrences(getTestPath(), "    public function getUse^r(): ?User {", true);
    }

    public void testNullsafeOperator_09b() throws Exception {
        checkOccurrences(getTestPath(), "$country = $session->getUse^r()::create(\"test\")?->getAddress()->country;", true);
    }

    public void testNullsafeOperator_10a() throws Exception {
        checkOccurrences(getTestPath(), "private ?Address $add^ress;", true);
    }

    public void testNullsafeOperator_10b() throws Exception {
        checkOccurrences(getTestPath(), "        $this->a^ddress = new Address();", true);
    }

    public void testNullsafeOperator_10c() throws Exception {
        checkOccurrences(getTestPath(), "        $test = $this?->addre^ss?->country;", true);
    }

    public void testNullsafeOperator_10d() throws Exception {
        checkOccurrences(getTestPath(), "        return $this?->addr^ess;", true);
    }

    public void testConstructorPropertyPromotion_01a() throws Exception {
        checkOccurrences(getTestPath(), "private int $fie^ld1,", true);
    }

    public void testConstructorPropertyPromotion_01b() throws Exception {
        checkOccurrences(getTestPath(), "$this->fiel^d1;", true);
    }

    public void testConstructorPropertyPromotion_01c() throws Exception {
        checkOccurrences(getTestPath(), "var_dump($this->fi^eld1);", true);
    }

    public void testConstructorPropertyPromotion_01d() throws Exception {
        checkOccurrences(getTestPath(), "echo $fiel^d1;", true);
    }

    public void testConstructorPropertyPromotion_02a() throws Exception {
        checkOccurrences(getTestPath(), "protected int|string|\\Test2\\Foo $fiel^d2,", true);
    }

    public void testConstructorPropertyPromotion_02b() throws Exception {
        checkOccurrences(getTestPath(), "$this->fiel^d2;", true);
    }

    public void testConstructorPropertyPromotion_02c() throws Exception {
        checkOccurrences(getTestPath(), "var_dump($this->fi^eld2);", true);
    }

    public void testConstructorPropertyPromotion_02d() throws Exception {
        checkOccurrences(getTestPath(), "echo $fiel^d2;", true);
    }

    public void testConstructorPropertyPromotion_03a() throws Exception {
        checkOccurrences(getTestPath(), "public ?string $field^3 = \"default value\",", true);
    }

    public void testConstructorPropertyPromotion_03b() throws Exception {
        checkOccurrences(getTestPath(), "$this->fiel^d3;", true);
    }

    public void testConstructorPropertyPromotion_03c() throws Exception {
        checkOccurrences(getTestPath(), "var_dump($this->fi^eld3);", true);
    }

    public void testConstructorPropertyPromotion_03d() throws Exception {
        checkOccurrences(getTestPath(), "$instance->fiel^d3;", true);
    }

    public void testConstructorPropertyPromotion_03e() throws Exception {
        checkOccurrences(getTestPath(), "echo $fiel^d3;", true);
    }

    public void testConstructorPropertyPromotion_04a() throws Exception {
        checkOccurrences(getTestPath(), "protected int|string|\\Test2\\Fo^o $field2,", true);
    }

    public void testConstructorPropertyPromotion_04b() throws Exception {
        checkOccurrences(getTestPath(), "class F^oo {}", true);
    }

    public void testAttributes_a01() throws Exception {
        checkOccurrences(getTestPath(), "class AttributeCla^ss1 {", true);
    }

    public void testAttributes_a02() throws Exception {
        checkOccurrences(getTestPath(), "use Attributes\\AttributeCla^ss1;", true);
    }

    public void testAttributes_a03() throws Exception {
        checkOccurrences(getTestPath(), "#[Attribut^eClass1(1, self::CONSTANT_CLASS)]", true);
    }

    public void testAttributes_a04() throws Exception {
        checkOccurrences(getTestPath(), "    #[AttributeCl^ass1(2, \"class const\")]", true);
    }

    public void testAttributes_a05() throws Exception {
        checkOccurrences(getTestPath(), "    #[AttributeCl^ass1(3, \"class field\")]", true);
    }

    public void testAttributes_a06() throws Exception {
        checkOccurrences(getTestPath(), "    #[AttributeCl^ass1(4, \"class static field\"), AttributeClass2(4, \"class static field\", new \\Attributes\\Example)] // group", true);
    }

    public void testAttributes_a07() throws Exception {
        checkOccurrences(getTestPath(), "    #[AttributeCla^ss1(5, \"class method\")]", true);
    }

    public void testAttributes_a08() throws Exception {
        checkOccurrences(getTestPath(), "    public function method(#[AttributeCl^ass1(5, \"class method param\")] $param1, #[AttributeClass1(5, 'class method param')] int $pram2) {}", true);
    }

    public void testAttributes_a09() throws Exception {
        checkOccurrences(getTestPath(), "    public function method(#[AttributeClass1(5, \"class method param\")] $param1, #[AttributeCla^ss1(5, 'class method param')] int $pram2) {}", true);
    }

    public void testAttributes_a10() throws Exception {
        checkOccurrences(getTestPath(), "    #[\\Attributes\\AttributeCla^ss1(6, \"class static method\")]", true);
    }

    public void testAttributes_a11() throws Exception {
        checkOccurrences(getTestPath(), "    public static function staticMethod(#[\\Attributes\\AttributeCl^ass1(6, \"class static method param\")] int|string $param1): bool|int {", true);
    }

    public void testAttributes_a12() throws Exception {
        checkOccurrences(getTestPath(), "#[AttributeC^lass1(1, \"class child\")]", true);
    }

    public void testAttributes_a13() throws Exception {
        checkOccurrences(getTestPath(), "#[AttributeC^lass1(1, \"trait\")]", true);
    }

    public void testAttributes_a14() throws Exception {
        checkOccurrences(getTestPath(), "    #[AttributeC^lass1(3, \"trait field\")]", true);
    }

    public void testAttributes_a15() throws Exception {
        checkOccurrences(getTestPath(), "    #[AttributeCla^ss1(4, \"trait static field\")]", true);
    }

    public void testAttributes_a16() throws Exception {
        checkOccurrences(getTestPath(), "    #[Attribut^eClass1(5, \"trait method\")]", true);
    }

    public void testAttributes_a17() throws Exception {
        checkOccurrences(getTestPath(), "    public function traitMethod(#[Attribute^Class1(5, \"trait method param\")] $param1) {}", true);
    }

    public void testAttributes_a18() throws Exception {
        checkOccurrences(getTestPath(), "    #[Att^ributeClass1(6, \"trait static method\")]", true);
    }

    public void testAttributes_a19() throws Exception {
        checkOccurrences(getTestPath(), "$anon = new #[Attri^buteClass1(1, \"anonymous class\")] class () {};", true);
    }

    public void testAttributes_a20() throws Exception {
        checkOccurrences(getTestPath(), "    #[AttributeCl^ass1(int: 7, string: \"anonymous class static method\")]", true);
    }

    public void testAttributes_a21() throws Exception {
        checkOccurrences(getTestPath(), "#[AttributeClas^s1(1, \"interface\")]", true);
    }

    public void testAttributes_a22() throws Exception {
        checkOccurrences(getTestPath(), "#[\\Attributes\\AttributeClass3(1, \"enum\"), AttributeC^lass1]", true);
    }

    public void testAttributes_a23() throws Exception {
        checkOccurrences(getTestPath(), "    #[AttributeCla^ss1(2, \"enum const\")]", true);
    }

    public void testAttributes_a24() throws Exception {
        checkOccurrences(getTestPath(), "    #[AttributeCla^ss1(3, \"enum case\")]", true);
    }

    public void testAttributes_a25() throws Exception {
        checkOccurrences(getTestPath(), "    #[Attr^ibuteClass1(4, \"enum method\")] #[AttributeClass3()]", true);
    }

    public void testAttributes_a26() throws Exception {
        checkOccurrences(getTestPath(), "    #[Attribute^Class1(int: 5, string: \"enum static method\")]", true);
    }

    public void testAttributes_a27() throws Exception {
        checkOccurrences(getTestPath(), "    Attri^buteClass1(1, \"function\"),", true);
    }

    public void testAttributes_a28() throws Exception {
        checkOccurrences(getTestPath(), "$labmda1 = #[Attribut^eClass1(1, \"closure\")] function() {};", true);
    }

    public void testAttributes_a29() throws Exception {
        checkOccurrences(getTestPath(), "$arrow1 = #[AttributeCl^ass1(1, \"arrow\")] fn() => 100;", true);
    }

    public void testAttributes_a30() throws Exception {
        checkOccurrences(getTestPath(), "$arrow2 = #[AttributeCl^ass1(2, \"arrow\"), AttributeClass2(2, \"arrow\")] fn(#[AttributeClass1(\\Attributes\\CONST_1 / 5, \"arrow param\" . Example::class)] $test): int|string => 100;", true);
    }

    public void testAttributes_a31() throws Exception {
        checkOccurrences(getTestPath(), "$arrow2 = #[AttributeClass1(2, \"arrow\"), AttributeClass2(2, \"arrow\")] fn(#[Attribute^Class1(\\Attributes\\CONST_1 / 5, \"arrow param\" . Example::class)] $test): int|string => 100;", true);
    }

    public void testAttributes_a32() throws Exception {
        checkOccurrences(getTestPath(), "$arrow3 = #[Attr^ibuteClass1(3, string: Example::CONST_EXAMPLE . \"arrow\")] static fn(): int|string => 100;", true);
    }

    public void testAttributes_b01() throws Exception {
        checkOccurrences(getTestPath(), "class AttributeClass^2 {", true);
    }

    public void testAttributes_b02() throws Exception {
        checkOccurrences(getTestPath(), "use Attributes\\Attribut^eClass2;", true);
    }

    public void testAttributes_b03() throws Exception {
        checkOccurrences(getTestPath(), "#[Attribu^teClass2(1, \"class\")]", true);
    }

    public void testAttributes_b04() throws Exception {
        checkOccurrences(getTestPath(), "    #[Attribute^Class2(2, \"class const\", new Example())]", true);
    }

    public void testAttributes_b05() throws Exception {
        checkOccurrences(getTestPath(), "    #[AttributeClass1(4, \"class static field\"), Attr^ibuteClass2(4, \"class static field\", new \\Attributes\\Example)] // group", true);
    }

    public void testAttributes_b06() throws Exception {
        checkOccurrences(getTestPath(), "#[AttributeCl^ass2(1, \"trait\")]", true);
    }

    public void testAttributes_b07() throws Exception {
        checkOccurrences(getTestPath(), "    #[Attribut^eClass2(2, \"trait const\")]", true);
    }

    public void testAttributes_b08() throws Exception {
        checkOccurrences(getTestPath(), "    #[Attribute^Class2(6, \"trait static method\")]", true);
    }

    public void testAttributes_b09() throws Exception {
        checkOccurrences(getTestPath(), "$anon2 = new #[Attr^ibuteClass2(1, \"anonymous class\")] class ($test) {", true);
    }

    public void testAttributes_b10() throws Exception {
        checkOccurrences(getTestPath(), "    #[Attribute^Class2(2, \"anonymous class const\")]", true);
    }

    public void testAttributes_b11() throws Exception {
        checkOccurrences(getTestPath(), "    #[At^tributeClass2(3, \"anonymous class field\")]", true);
    }

    public void testAttributes_b12() throws Exception {
        checkOccurrences(getTestPath(), "    #[Att^ributeClass2(4, \"anonymous class static field\")]", true);
    }

    public void testAttributes_b13() throws Exception {
        checkOccurrences(getTestPath(), "    #[Att^ributeClass2(5, \"anonymous class constructor\")]", true);
    }

    public void testAttributes_b14() throws Exception {
        checkOccurrences(getTestPath(), "    public function __construct(#[AttributeClas^s2(5, \"anonymous class\")] $param1) {", true);
    }

    public void testAttributes_b15() throws Exception {
        checkOccurrences(getTestPath(), "    #[Attribute^Class2(6, \"anonymous class method\")] #[AttributeClass3()]", true);
    }

    public void testAttributes_b16() throws Exception {
        checkOccurrences(getTestPath(), "    public function method(#[Attrib^uteClass2(6, \"anonymous class method param\")] $param1): int|string {", true);
    }

    public void testAttributes_b17() throws Exception {
        checkOccurrences(getTestPath(), "    private static function staticMethod(#[Attribu^teClass2(7, \"anonymous class static method param\")] int|bool $pram1): int|string {", true);
    }

    public void testAttributes_b18() throws Exception {
        checkOccurrences(getTestPath(), "    #[Attribut^eClass2(2, \"interface const\")]", true);
    }

    public void testAttributes_b19() throws Exception {
        checkOccurrences(getTestPath(), "    #[AttributeC^lass2(self::CONSTANT_INTERFACE, \"interface method\")] #[AttributeClass3()]", true);
    }

    public void testAttributes_b20() throws Exception {
        checkOccurrences(getTestPath(), "    public function interfaceMethod(#[Attribute^Class2(AttributedInterface::CONSTANT_INTERFACE, \"interface method param\")] $param1): int|string;", true);
    }

    public void testAttributes_b21() throws Exception {
        checkOccurrences(getTestPath(), "    #[Attribut^eClass2(4, \"interface static method\")] #[AttributeClass3()]", true);
    }

    public void testAttributes_b22() throws Exception {
        checkOccurrences(getTestPath(), "    public static function staticInterfaceMethod(#[Attrib^uteClass2(4, \"interface static method param\" . Example::CONST_EXAMPLE)] $param1): int|string;", true);
    }

    public void testAttributes_b23() throws Exception {
        checkOccurrences(getTestPath(), "#[\\Attributes\\Attrib^uteClass2(1, \"enum\", new Example)]", true);
    }

    public void testAttributes_b24() throws Exception {
        checkOccurrences(getTestPath(), "    public function method(#[Attribu^teClass2(4, \"enum method param\")] $param1): int|string {", true);
    }

    public void testAttributes_b25() throws Exception {
        checkOccurrences(getTestPath(), "#[Attribut^eClass2(1, \"function\")]", true);
    }

    public void testAttributes_b26() throws Exception {
        checkOccurrences(getTestPath(), "function func2(#[Att^ributeClass2(1 + \\Attributes\\CONST_1 + 1, Example::CONST_EXAMPLE . \"function param\")] int|string $param): void {}", true);
    }

    public void testAttributes_b27() throws Exception {
        checkOccurrences(getTestPath(), "$labmda2 = #[Attribute^Class2(2, \"closure\")] #[AttributeClass3(2, \"closurr\")] function(#[AttributeClass2(int: 2 * \\Attributes\\CONST_1, string: \"closure param\" . Example::CONST_EXAMPLE)] $test): void {};", true);
    }

    public void testAttributes_b28() throws Exception {
        checkOccurrences(getTestPath(), "$labmda2 = #[AttributeClass2(2, \"closure\")] #[AttributeClass3(2, \"closurr\")] function(#[Attrib^uteClass2(int: 2 * \\Attributes\\CONST_1, string: \"closure param\" . Example::CONST_EXAMPLE)] $test): void {};", true);
    }

    public void testAttributes_b29() throws Exception {
        checkOccurrences(getTestPath(), "$arrow2 = #[AttributeClass1(2, \"arrow\"), Attribut^eClass2(2, \"arrow\")] fn(#[AttributeClass1(\\Attributes\\CONST_1 / 5, \"arrow param\" . Example::class)] $test): int|string => 100;", true);
    }

    public void testAttributes_c01() throws Exception {
        checkOccurrences(getTestPath(), "class Attribute^Class3 {", true);
    }

    public void testAttributes_c02() throws Exception {
        checkOccurrences(getTestPath(), "use Attributes\\Attribu^teClass3;", true);
    }

    public void testAttributes_c03() throws Exception {
        checkOccurrences(getTestPath(), "    #[Attribut^eClass3(6, \"trait static method\")]", true);
    }

    public void testAttributes_c04() throws Exception {
        checkOccurrences(getTestPath(), "    public static function staticTraitMethod(#[Attribut^eClass3] int|string $param1): bool|int {", true);
    }

    public void testAttributes_c05() throws Exception {
        checkOccurrences(getTestPath(), "    #[Attrib^uteClass3(3, \"anonymous class field\")]", true);
    }

    public void testAttributes_c06() throws Exception {
        checkOccurrences(getTestPath(), "    #[AttributeClass2(6, \"anonymous class method\")] #[Attribu^teClass3()]", true);
    }

    public void testAttributes_c07() throws Exception {
        checkOccurrences(getTestPath(), "    #[AttributeClass2(self::CONSTANT_INTERFACE, \"interface method\")] #[Attribu^teClass3()]", true);
    }

    public void testAttributes_c08() throws Exception {
        checkOccurrences(getTestPath(), "    #[AttributeClass2(4, \"interface static method\")] #[Attri^buteClass3()]", true);
    }

    public void testAttributes_c09() throws Exception {
        checkOccurrences(getTestPath(), "#[\\Attributes\\Attribut^eClass3(1, \"enum\"), AttributeClass1]", true);
    }

    public void testAttributes_c10() throws Exception {
        checkOccurrences(getTestPath(), "    #[AttributeClass1(4, \"enum method\")] #[AttributeCla^ss3()]", true);
    }

    public void testAttributes_c11() throws Exception {
        checkOccurrences(getTestPath(), "    public static function staticMethod(#[Attrib^uteClass3(5, \"enum static method param\")] int|bool $pram1): int|string {", true);
    }

    public void testAttributes_c12() throws Exception {
        checkOccurrences(getTestPath(), "    Attribu^teClass3(int: CONST_1, string: \"function\" . Example::class)", true);
    }

    public void testAttributes_c13() throws Exception {
        checkOccurrences(getTestPath(), "$labmda2 = #[AttributeClass2(2, \"closure\")] #[Attri^buteClass3(2, \"closurr\")] function(#[AttributeClass2(int: 2 * \\Attributes\\CONST_1, string: \"closure param\" . Example::CONST_EXAMPLE)] $test): void {};", true);
    }

    public void testAttributes_c14() throws Exception {
        checkOccurrences(getTestPath(), "$labmda3 = #[Attribut^eClass3(3, \"closure\")] static function(): void {};", true);
    }

    public void testAttributes_d01() throws Exception {
        checkOccurrences(getTestPath(), "const CONS^T_1 = 1;", true);
    }

    public void testAttributes_d02() throws Exception {
        checkOccurrences(getTestPath(), "use const Attributes\\CO^NST_1;", true);
    }

    public void testAttributes_d03() throws Exception {
        checkOccurrences(getTestPath(), "    AttributeClass3(int: CONST_^1, string: \"function\" . Example::class)", true);
    }

    public void testAttributes_d04() throws Exception {
        checkOccurrences(getTestPath(), "function func2(#[AttributeClass2(1 + \\Attributes\\CON^ST_1 + 1, Example::CONST_EXAMPLE . \"function param\")] int|string $param): void {}", true);
    }

    public void testAttributes_d05() throws Exception {
        checkOccurrences(getTestPath(), "$labmda2 = #[AttributeClass2(2, \"closure\")] #[AttributeClass3(2, \"closurr\")] function(#[AttributeClass2(int: 2 * \\Attributes\\CON^ST_1, string: \"closure param\" . Example::CONST_EXAMPLE)] $test): void {};", true);
    }

    public void testAttributes_d06() throws Exception {
        checkOccurrences(getTestPath(), "$arrow2 = #[AttributeClass1(2, \"arrow\"), AttributeClass2(2, \"arrow\")] fn(#[AttributeClass1(\\Attributes\\CONS^T_1 / 5, \"arrow param\" . Example::class)] $test): int|string => 100;", true);
    }

    public void testAttributes_e01() throws Exception {
        checkOccurrences(getTestPath(), "class Ex^ample {", true);
    }

    public void testAttributes_e02() throws Exception {
        checkOccurrences(getTestPath(), "use Attributes\\Exa^mple;", true);
    }

    public void testAttributes_e03() throws Exception {
        checkOccurrences(getTestPath(), "    #[AttributeClass2(2, \"class const\", new Exa^mple())]", true);
    }

    public void testAttributes_e04() throws Exception {
        checkOccurrences(getTestPath(), "    #[AttributeClass1(4, \"class static field\"), AttributeClass2(4, \"class static field\", new \\Attributes\\Exa^mple)] // group", true);
    }

    public void testAttributes_e05() throws Exception {
        checkOccurrences(getTestPath(), "    public static function staticInterfaceMethod(#[AttributeClass2(4, \"interface static method param\" . Examp^le::CONST_EXAMPLE)] $param1): int|string;", true);
    }

    public void testAttributes_e06() throws Exception {
        checkOccurrences(getTestPath(), "#[\\Attributes\\AttributeClass2(1, \"enum\", new E^xample)]", true);
    }

    public void testAttributes_e07() throws Exception {
        checkOccurrences(getTestPath(), "    AttributeClass3(int: CONST_1, string: \"function\" . Exa^mple::class)", true);
    }

    public void testAttributes_e08() throws Exception {
        checkOccurrences(getTestPath(), "function func2(#[AttributeClass2(1 + \\Attributes\\CONST_1 + 1, Exam^ple::CONST_EXAMPLE . \"function param\")] int|string $param): void {}", true);
    }

    public void testAttributes_e09() throws Exception {
        checkOccurrences(getTestPath(), "$labmda2 = #[AttributeClass2(2, \"closure\")] #[AttributeClass3(2, \"closurr\")] function(#[AttributeClass2(int: 2 * \\Attributes\\CONST_1, string: \"closure param\" . Ex^ample::CONST_EXAMPLE)] $test): void {};", true);
    }

    public void testAttributes_e10() throws Exception {
        checkOccurrences(getTestPath(), "$arrow2 = #[AttributeClass1(2, \"arrow\"), AttributeClass2(2, \"arrow\")] fn(#[AttributeClass1(\\Attributes\\CONST_1 / 5, \"arrow param\" . Exam^ple::class)] $test): int|string => 100;", true);
    }

    public void testAttributes_e11() throws Exception {
        checkOccurrences(getTestPath(), "$arrow3 = #[AttributeClass1(3, string: Exa^mple::CONST_EXAMPLE . \"arrow\")] static fn(): int|string => 100;", true);
    }

    public void testAttributes_f01() throws Exception {
        checkOccurrences(getTestPath(), "    public const string CONST_EXAMP^LE = \"example\";", true);
    }

    public void testAttributes_f02() throws Exception {
        checkOccurrences(getTestPath(), "    public static function staticInterfaceMethod(#[AttributeClass2(4, \"interface static method param\" . Example::CONST^_EXAMPLE)] $param1): int|string;", true);
    }

    public void testAttributes_f03() throws Exception {
        checkOccurrences(getTestPath(), "function func2(#[AttributeClass2(1 + \\Attributes\\CONST_1 + 1, Example::CONST_EXA^MPLE . \"function param\")] int|string $param): void {}", true);
    }

    public void testAttributes_f04() throws Exception {
        checkOccurrences(getTestPath(), "$labmda2 = #[AttributeClass2(2, \"closure\")] #[AttributeClass3(2, \"closurr\")] function(#[AttributeClass2(int: 2 * \\Attributes\\CONST_1, string: \"closure param\" . Example::CO^NST_EXAMPLE)] $test): void {};", true);
    }

    public void testAttributes_f05() throws Exception {
        checkOccurrences(getTestPath(), "$arrow3 = #[AttributeClass1(3, string: Example::CONST_EX^AMPLE . \"arrow\")] static fn(): int|string => 100;", true);
    }

    public void testAttributes_g01() throws Exception {
        checkOccurrences(getTestPath(), "#[AttributeClass1(1, self::CONSTANT^_CLASS)]", true);
    }

    public void testAttributes_g02() throws Exception {
        checkOccurrences(getTestPath(), "    public const CO^NSTANT_CLASS = 'constant';", true);
    }

}
