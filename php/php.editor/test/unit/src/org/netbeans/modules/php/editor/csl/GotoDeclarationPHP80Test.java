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

public class GotoDeclarationPHP80Test extends GotoDeclarationTestBase {

    public GotoDeclarationPHP80Test(String testName) {
        super(testName);
    }

    @Override
    protected String getBaseTestFolderPath() {
        return super.getBaseTestFolderPath() + "php80/";
    }

    public void testNonCapturingCatches_01() throws Exception {
        checkDeclaration(getTestPath(), "} catch (Exc^eptionA) {", "class ^ExceptionA extends Exception");
    }

    public void testNonCapturingCatches_02() throws Exception {
        checkDeclaration(getTestPath(), "} catch (E^xceptionA | ExceptionB) {", "class ^ExceptionA extends Exception");
    }

    public void testNonCapturingCatches_03() throws Exception {
        checkDeclaration(getTestPath(), "} catch (ExceptionA | Exception^B) {", "class ^ExceptionB extends Exception");
    }

    public void testMatchExpression_01() throws Exception {
        checkDeclaration(getTestPath(), "            MatchEx^pression::START => self::$start,", "class ^MatchExpression");
    }

    public void testMatchExpression_02() throws Exception {
        checkDeclaration(getTestPath(), "            Match^Expression::SUSPEND => $this->suspend,", "class ^MatchExpression");
    }

    public void testMatchExpression_03() throws Exception {
        checkDeclaration(getTestPath(), "            MatchExpre^ssion::STOP => $this->stopState(),", "class ^MatchExpression");
    }

    public void testMatchExpression_04() throws Exception {
        checkDeclaration(getTestPath(), "            default =>  Matc^hExpression::default() . MatchExpression::match . $this->match(),", "class ^MatchExpression");
    }

    public void testMatchExpression_05() throws Exception {
        checkDeclaration(getTestPath(), "            default =>  MatchExpression::default() . MatchExpressi^on::match . $this->match(),", "class ^MatchExpression");
    }

    public void testMatchExpression_06() throws Exception {
        checkDeclaration(getTestPath(), "$instance = new MatchExp^ression();", "class ^MatchExpression");
    }

    public void testMatchExpression_07() throws Exception {
        checkDeclaration(getTestPath(), "    default => MatchExp^ression::default(),", "class ^MatchExpression");
    }

    public void testMatchExpression_08() throws Exception {
        checkDeclaration(getTestPath(), "        return match ($st^ate) {", "        $^state = self::STOP;");
    }

    public void testMatchExpression_09() throws Exception {
        checkDeclaration(getTestPath(), "            MatchExpression::STA^RT => self::$start,", "    public const ^START = \"start\";");
    }

    public void testMatchExpression_10() throws Exception {
        checkDeclaration(getTestPath(), "            MatchExpression::START => self::$s^tart,", "    private static $^start = \"start state\";");
    }

    public void testMatchExpression_11() throws Exception {
        checkDeclaration(getTestPath(), "            MatchExpression::SUSPE^ND => $this->suspend,", "    public const ^SUSPEND = \"suspend\";");
    }

    public void testMatchExpression_12() throws Exception {
        checkDeclaration(getTestPath(), "            MatchExpression::SUSPEND => $this->sus^pend,", "    private $^suspend = \"suspend state\";");
    }

    public void testMatchExpression_13() throws Exception {
        checkDeclaration(getTestPath(), "            MatchExpression::STOP => $this->stop^State(),", "    public function ^stopState(): string {");
    }

    public void testMatchExpression_14() throws Exception {
        checkDeclaration(getTestPath(), "            default =>  MatchExpression::default() . MatchExpression::ma^tch . $this->match(),", "    private const ^match = \"match\"; // context sensitive lexer");
    }

    public void testMatchExpression_15() throws Exception {
        checkDeclaration(getTestPath(), "            default =>  MatchExpression::default() . MatchExpression::match . $this->m^atch(),", "    public function ^match(): string {");
    }

    public void testMatchExpression_16() throws Exception {
        checkDeclaration(getTestPath(), "    default => MatchExpression::defau^lt(),", "    public static function ^default(): string {");
    }

    public void testUnionTypes_01a() throws Exception {
        checkDeclaration(getTestPath(), "    private ParentCl^ass|ChildClass $field;", "class ^ParentClass");
    }

    public void testUnionTypes_01b() throws Exception {
        checkDeclaration(getTestPath(), "    public function testMethod(ParentC^lass|ChildClass|null $param): ChildClass|\\Union\\Types1\\ParentClass {", "class ^ParentClass");
    }

    public void testUnionTypes_01c() throws Exception {
        checkDeclaration(getTestPath(), "    public function testMethod(ParentClass|ChildClass|null $param): ChildClass|\\Union\\Types1\\Par^entClass {", "class ^ParentClass");
    }

    public void testUnionTypes_01d() throws Exception {
        checkDeclaration(getTestPath(), "    public static function testStaticMethod(TestClass1|\\Union\\Types2\\TestClass2|null $param1, ChildClass|null $param2): TestClass2|Pare^ntClass|null {", "class ^ParentClass");
    }

    public void testUnionTypes_02a() throws Exception {
        checkDeclaration(getTestPath(), "    private ParentClass|ChildCl^ass $field;", "class ^ChildClass");
    }

    public void testUnionTypes_02b() throws Exception {
        checkDeclaration(getTestPath(), "    public function testMethod(ParentClass|ChildC^lass|null $param): ChildClass|\\Union\\Types1\\ParentClass {", "class ^ChildClass");
    }

    public void testUnionTypes_02c() throws Exception {
        checkDeclaration(getTestPath(), "    public function testMethod(ParentClass|ChildClass|null $param): Chil^dClass|\\Union\\Types1\\ParentClass {", "class ^ChildClass");
    }

    public void testUnionTypes_02d() throws Exception {
        checkDeclaration(getTestPath(), "    public static function testStaticMethod(TestClass1|\\Union\\Types2\\TestClass2|null $param1, Child^Class|null $param2): TestClass2|ParentClass|null {", "class ^ChildClass");
    }

    public void testUnionTypes_03a() throws Exception {
        checkDeclaration(getTestPath(), "    private static \\Union\\Types2\\Test^Class1|TestClass2 $staticField;", "class ^TestClass1");
    }

    public void testUnionTypes_03b() throws Exception {
        checkDeclaration(getTestPath(), "    public static function testStaticMethod(TestC^lass1|\\Union\\Types2\\TestClass2|null $param1, ChildClass|null $param2): TestClass2|ParentClass|null {", "class ^TestClass1");
    }

    public void testUnionTypes_03c() throws Exception {
        checkDeclaration(getTestPath(), "    public function traitMethod(Te^stClass1|TestClass2 $param): TestClass1|TestClass2|null {", "class ^TestClass1");
    }

    public void testUnionTypes_03d() throws Exception {
        checkDeclaration(getTestPath(), "    public function traitMethod(TestClass1|TestClass2 $param): ^TestClass1|TestClass2|null {", "class ^TestClass1");
    }

    public void testUnionTypes_04a() throws Exception {
        checkDeclaration(getTestPath(), "    private static \\Union\\Types2\\TestClass1|Test^Class2 $staticField;", "class ^TestClass2");
    }

    public void testUnionTypes_04b() throws Exception {
        checkDeclaration(getTestPath(), "    public static function testStaticMethod(TestClass1|\\Union\\Types2\\TestCl^ass2|null $param1, ChildClass|null $param2): TestClass2|ParentClass|null {", "class ^TestClass2");
    }

    public void testUnionTypes_04c() throws Exception {
        checkDeclaration(getTestPath(), "    public static function testStaticMethod(TestClass1|\\Union\\Types2\\TestClass2|null $param1, ChildClass|null $param2): TestClas^s2|ParentClass|null {", "class ^TestClass2");
    }

    public void testUnionTypes_04d() throws Exception {
        checkDeclaration(getTestPath(), "    public function traitMethod(TestClass1|TestClas^s2 $param): TestClass1|TestClass2|null {", "class ^TestClass2");
    }

    public void testUnionTypes_04e() throws Exception {
        checkDeclaration(getTestPath(), "    public function traitMethod(TestClass1|TestClass2 $param): TestClass1|Tes^tClass2|null {", "class ^TestClass2");
    }

    public void testNullsafeOperator_01a() throws Exception {
        checkDeclaration(getTestPath(), "       $test = $this?->addr^ess?->country;", "    private ?Address $^address;");
    }

    public void testNullsafeOperator_01b() throws Exception {
        checkDeclaration(getTestPath(), "        return $this?->a^ddress;", "    private ?Address $^address;");
    }

    public void testNullsafeOperator_02a() throws Exception {
        checkDeclaration(getTestPath(), "$country = $sess^ion?->user?->getAddress()?->country;", "$^session = new Session(new User(\"test\"));");
    }

    public void testNullsafeOperator_03a() throws Exception {
        checkDeclaration(getTestPath(), "        $test = $this?->address?->cou^ntry;", "    public Country $^country;");
    }

    public void testNullsafeOperator_03b() throws Exception {
        checkDeclaration(getTestPath(), "$country = $session?->user?->getAddress()?->count^ry;", "    public Country $^country;");
    }

    public void testNullsafeOperator_03c() throws Exception {
        checkDeclaration(getTestPath(), "$country = User::create(\"test\")?->getAddress()?->c^ountry;", "    public Country $^country;");
    }

    public void testNullsafeOperator_03d() throws Exception {
        checkDeclaration(getTestPath(), "$country = $session->getUser()::create(\"test\")?->getAddress()->cou^ntry;", "    public Country $^country;");
    }

    public void testNullsafeOperator_03e() throws Exception {
        checkDeclaration(getTestPath(), "$country = (new User(\"test\"))?->getAddress()->co^untry;", "    public Country $^country;");
    }

    public void testNullsafeOperator_04a() throws Exception {
        checkDeclaration(getTestPath(), "$country = $session?->us^er?->getAddress()?->country;", "    public ?User $^user;");
    }

    public void testNullsafeOperator_04b() throws Exception {
        checkDeclaration(getTestPath(), "$country = $session?->us^er::$test;", "    public ?User $^user;");
    }

    public void testNullsafeOperator_0c() throws Exception {
        checkDeclaration(getTestPath(), "$country = $session?->^user::test();", "    public ?User $^user;");
    }

    public void testNullsafeOperator_04d() throws Exception {
        checkDeclaration(getTestPath(), "$country = $session?->us^er->id;", "    public ?User $^user;");
    }

    public void testNullsafeOperator_04e() throws Exception {
        checkDeclaration(getTestPath(), "$country = $session?->us^er?->getAddress()::ID;", "    public ?User $^user;");
    }

    public void testNullsafeOperator_05a() throws Exception {
        checkDeclaration(getTestPath(), "$country = $session?->user?->getAdd^ress()?->country;", "    public function ^getAddress(): ?Address {");
    }

    public void testNullsafeOperator_05b() throws Exception {
        checkDeclaration(getTestPath(), "$country = $session?->user?->getAdd^ress()::ID;", "    public function ^getAddress(): ?Address {");
    }

    public void testNullsafeOperator_05c() throws Exception {
        checkDeclaration(getTestPath(), "$country = User::create(\"test\")?->getA^ddress()?->country;", "    public function ^getAddress(): ?Address {");
    }

    public void testNullsafeOperator_05d() throws Exception {
        checkDeclaration(getTestPath(), "$country = $session->getUser()::create(\"test\")?->getAd^dress()->country;", "    public function ^getAddress(): ?Address {");
    }

    public void testNullsafeOperator_05e() throws Exception {
        checkDeclaration(getTestPath(), "$country = (new User(\"test\"))?->getAd^dress()->country;", "    public function ^getAddress(): ?Address {");
    }

    public void testNullsafeOperator_06a() throws Exception {
        checkDeclaration(getTestPath(), "$country = $session?->user::$te^st;", "    public static string $^test = \"test\";");
    }

    public void testNullsafeOperator_07a() throws Exception {
        checkDeclaration(getTestPath(), "$country = $session?->user::tes^t();", "    public static function ^test(): string {");
    }

    public void testNullsafeOperator_08a() throws Exception {
        checkDeclaration(getTestPath(), "$country = $session?->user->i^d;", "    public int $^id = 1;");
    }

    public void testNullsafeOperator_09a() throws Exception {
        checkDeclaration(getTestPath(), "$country = $session?->user?->getAddress()::I^D;", "    public const ^ID = \"Adress\";");
    }

    public void testNullsafeOperator_10a() throws Exception {
        checkDeclaration(getTestPath(), "$country = User::crea^te(\"test\")?->getAddress()?->country;", "    public static function ^create(string $name): User {");
    }

    public void testNullsafeOperator_10b() throws Exception {
        checkDeclaration(getTestPath(), "$country = $session->getUser()::cre^ate(\"test\")?->getAddress()->country;", "    public static function ^create(string $name): User {");
    }

    public void testNullsafeOperator_11a() throws Exception {
        checkDeclaration(getTestPath(), "$country = $session->ge^tUser()::create(\"test\")?->getAddress()->country;", "    public function ^getUser(): ?User {");
    }

    public void testConstructorPropertyPromotion_01a() throws Exception {
        checkDeclaration(getTestPath(), "$this->fie^ld1;", "        private int $^field1,");
    }

    public void testConstructorPropertyPromotion_01b() throws Exception {
        checkDeclaration(getTestPath(), "var_dump($this->fie^ld1);", "        private int $^field1,");
    }

    public void testConstructorPropertyPromotion_01c() throws Exception {
        checkDeclaration(getTestPath(), "echo $fiel^d1;", "        private int $^field1,");
    }

    public void testConstructorPropertyPromotion_02a() throws Exception {
        checkDeclaration(getTestPath(), "$this->fie^ld2;", "            protected int|string|\\Test2\\Foo $^field2,");
    }

    public void testConstructorPropertyPromotion_02b() throws Exception {
        checkDeclaration(getTestPath(), "var_dump($this->fie^ld2);", "        protected int|string|\\Test2\\Foo $^field2,");
    }

    public void testConstructorPropertyPromotion_02c() throws Exception {
        checkDeclaration(getTestPath(), "echo $fiel^d2;", "        protected int|string|\\Test2\\Foo $^field2,");
    }

    public void testConstructorPropertyPromotion_03a() throws Exception {
        checkDeclaration(getTestPath(), "$this->field^3;", "        public ?string $^field3 = \"default value\",");
    }

    public void testConstructorPropertyPromotion_03b() throws Exception {
        checkDeclaration(getTestPath(), "var_dump($this->f^ield3);", "        public ?string $^field3 = \"default value\",");
    }

    public void testConstructorPropertyPromotion_03c() throws Exception {
        checkDeclaration(getTestPath(), "echo $fie^ld3;", "        public ?string $^field3 = \"default value\",");
    }

    public void testConstructorPropertyPromotion_03d() throws Exception {
        checkDeclaration(getTestPath(), "$instance->fie^ld3;", "        public ?string $^field3 = \"default value\",");
    }

    public void testConstructorPropertyPromotion_04a() throws Exception {
        checkDeclaration(getTestPath(), "protected int|string|\\Test2\\F^oo $field2,", "    class ^Foo {}");
    }

    public void testAttributes_a01() throws Exception {
        checkDeclaration(getTestPath(), "#[AttributeCl^ass1(1, self::CONSTANT_CLASS)]", "class ^AttributeClass1 {");
    }

    public void testAttributes_a02() throws Exception {
        checkDeclaration(getTestPath(), "    #[AttributeClas^s1(2, \"class const\")]", "class ^AttributeClass1 {");
    }

    public void testAttributes_a03() throws Exception {
        checkDeclaration(getTestPath(), "    #[AttributeCla^ss1(3, \"class field\")]", "class ^AttributeClass1 {");
    }

    public void testAttributes_a04() throws Exception {
        checkDeclaration(getTestPath(), "    #[AttributeCl^ass1(4, \"class static field\"), AttributeClass2(4, \"class static field\", new \\Attributes\\Example)] // group", "class ^AttributeClass1 {");
    }

    public void testAttributes_a05() throws Exception {
        checkDeclaration(getTestPath(), "    #[AttributeCla^ss1(5, \"class method\")]", "class ^AttributeClass1 {");
    }

    public void testAttributes_a06() throws Exception {
        checkDeclaration(getTestPath(), "    public function method(#[AttributeC^lass1(5, \"class method param\")] $param1, #[AttributeClass1(5, 'class method param')] int $pram2) {}", "class ^AttributeClass1 {");
    }

    public void testAttributes_a07() throws Exception {
        checkDeclaration(getTestPath(), "    public function method(#[AttributeClass1(5, \"class method param\")] $param1, #[AttributeCla^ss1(5, 'class method param')] int $pram2) {}", "class ^AttributeClass1 {");
    }

    public void testAttributes_a08() throws Exception {
        checkDeclaration(getTestPath(), "    #[\\Attributes\\AttributeClas^s1(6, \"class static method\")]", "class ^AttributeClass1 {");
    }

    public void testAttributes_a09() throws Exception {
        checkDeclaration(getTestPath(), "    public static function staticMethod(#[\\Attributes\\Attrib^uteClass1(6, \"class static method param\")] int|string $param1): bool|int {", "class ^AttributeClass1 {");
    }

    public void testAttributes_a10() throws Exception {
        checkDeclaration(getTestPath(), "#[AttributeC^lass1(1, \"class child\")]", "class ^AttributeClass1 {");
    }

    public void testAttributes_a11() throws Exception {
        checkDeclaration(getTestPath(), "#[Attribute^Class1(1, \"trait\")]", "class ^AttributeClass1 {");
    }

    public void testAttributes_a12() throws Exception {
        checkDeclaration(getTestPath(), "    #[AttributeCl^ass1(3, \"trait field\")]", "class ^AttributeClass1 {");
    }

    public void testAttributes_a13() throws Exception {
        checkDeclaration(getTestPath(), "    #[AttributeCl^ass1(4, \"trait static field\")]", "class ^AttributeClass1 {");
    }

    public void testAttributes_a14() throws Exception {
        checkDeclaration(getTestPath(), "    #[AttributeCla^ss1(5, \"trait method\")]", "class ^AttributeClass1 {");
    }

    public void testAttributes_a15() throws Exception {
        checkDeclaration(getTestPath(), "    public function traitMethod(#[AttributeCl^ass1(5, \"trait method param\")] $param1) {}", "class ^AttributeClass1 {");
    }

    public void testAttributes_a16() throws Exception {
        checkDeclaration(getTestPath(), "    #[Attri^buteClass1(6, \"trait static method\")]", "class ^AttributeClass1 {");
    }

    public void testAttributes_a17() throws Exception {
        checkDeclaration(getTestPath(), "$anon = new #[Attribut^eClass1(1, \"anonymous class\")] class () {};", "class ^AttributeClass1 {");
    }

    public void testAttributes_a18() throws Exception {
        checkDeclaration(getTestPath(), "    #[AttributeClas^s1(int: 7, string: \"anonymous class static method\")]", "class ^AttributeClass1 {");
    }

    public void testAttributes_a19() throws Exception {
        checkDeclaration(getTestPath(), "#[Attr^ibuteClass1(1, \"interface\")]", "class ^AttributeClass1 {");
    }

    public void testAttributes_a20() throws Exception {
        checkDeclaration(getTestPath(), "#[\\Attributes\\AttributeClass3(1, \"enum\"), Attrib^uteClass1]", "class ^AttributeClass1 {");
    }

    public void testAttributes_a21() throws Exception {
        checkDeclaration(getTestPath(), "    #[AttributeCl^ass1(2, \"enum const\")]", "class ^AttributeClass1 {");
    }

    public void testAttributes_a22() throws Exception {
        checkDeclaration(getTestPath(), "    #[AttributeC^lass1(3, \"enum case\")]", "class ^AttributeClass1 {");
    }

    public void testAttributes_a23() throws Exception {
        checkDeclaration(getTestPath(), "    #[AttributeCl^ass1(4, \"enum method\")] #[AttributeClass3()]", "class ^AttributeClass1 {");
    }

    public void testAttributes_a24() throws Exception {
        checkDeclaration(getTestPath(), "    #[AttributeCl^ass1(int: 5, string: \"enum static method\")]", "class ^AttributeClass1 {");
    }

    public void testAttributes_a25() throws Exception {
        checkDeclaration(getTestPath(), "    AttributeCla^ss1(1, \"function\"),", "class ^AttributeClass1 {");
    }

    public void testAttributes_a26() throws Exception {
        checkDeclaration(getTestPath(), "$labmda1 = #[Attribut^eClass1(1, \"closure\")] function() {};", "class ^AttributeClass1 {");
    }

    public void testAttributes_a27() throws Exception {
        checkDeclaration(getTestPath(), "$arrow1 = #[AttributeCla^ss1(1, \"arrow\")] fn() => 100;", "class ^AttributeClass1 {");
    }

    public void testAttributes_a28() throws Exception {
        checkDeclaration(getTestPath(), "$arrow2 = #[AttributeCla^ss1(2, \"arrow\"), AttributeClass2(2, \"arrow\")] fn(#[AttributeClass1(\\Attributes\\CONST_1 / 5, \"arrow param\" . Example::class)] $test): int|string => 100;", "class ^AttributeClass1 {");
    }

    public void testAttributes_a29() throws Exception {
        checkDeclaration(getTestPath(), "$arrow2 = #[AttributeClass1(2, \"arrow\"), AttributeClass2(2, \"arrow\")] fn(#[AttributeC^lass1(\\Attributes\\CONST_1 / 5, \"arrow param\" . Example::class)] $test): int|string => 100;", "class ^AttributeClass1 {");
    }

    public void testAttributes_a30() throws Exception {
        checkDeclaration(getTestPath(), "$arrow3 = #[Attribute^Class1(3, string: Example::CONST_EXAMPLE . \"arrow\")] static fn(): int|string => 100;", "class ^AttributeClass1 {");
    }

    public void testAttributes_b01() throws Exception {
        checkDeclaration(getTestPath(), "#[AttributeCl^ass2(1, \"class\")]", "class ^AttributeClass2 {");
    }

    public void testAttributes_b02() throws Exception {
        checkDeclaration(getTestPath(), "    #[AttributeCla^ss2(2, \"class const\", new Example())]", "class ^AttributeClass2 {");
    }

    public void testAttributes_b03() throws Exception {
        checkDeclaration(getTestPath(), "    #[AttributeClass1(4, \"class static field\"), Attribut^eClass2(4, \"class static field\", new \\Attributes\\Example)] // group", "class ^AttributeClass2 {");
    }

    public void testAttributes_b04() throws Exception {
        checkDeclaration(getTestPath(), "#[AttributeClas^s2(1, \"trait\")]", "class ^AttributeClass2 {");
    }

    public void testAttributes_b05() throws Exception {
        checkDeclaration(getTestPath(), "    #[A^ttributeClass2(2, \"trait const\")]", "class ^AttributeClass2 {");
    }

    public void testAttributes_b06() throws Exception {
        checkDeclaration(getTestPath(), "    #[Attr^ibuteClass2(6, \"trait static method\")]", "class ^AttributeClass2 {");
    }

    public void testAttributes_b07() throws Exception {
        checkDeclaration(getTestPath(), "$anon2 = new #[At^tributeClass2(1, \"anonymous class\")] class ($test) {", "class ^AttributeClass2 {");
    }

    public void testAttributes_b08() throws Exception {
        checkDeclaration(getTestPath(), "    #[AttributeC^lass2(2, \"anonymous class const\")]", "class ^AttributeClass2 {");
    }

    public void testAttributes_b09() throws Exception {
        checkDeclaration(getTestPath(), "    #[Att^ributeClass2(3, \"anonymous class field\")]", "class ^AttributeClass2 {");
    }

    public void testAttributes_b10() throws Exception {
        checkDeclaration(getTestPath(), "    #[At^tributeClass2(4, \"anonymous class static field\")]", "class ^AttributeClass2 {");
    }

    public void testAttributes_b11() throws Exception {
        checkDeclaration(getTestPath(), "    #[Att^ributeClass2(5, \"anonymous class constructor\")]", "class ^AttributeClass2 {");
    }

    public void testAttributes_b12() throws Exception {
        checkDeclaration(getTestPath(), "    public function __construct(#[Attr^ibuteClass2(5, \"anonymous class\")] $param1) {", "class ^AttributeClass2 {");
    }

    public void testAttributes_b13() throws Exception {
        checkDeclaration(getTestPath(), "    #[A^ttributeClass2(6, \"anonymous class method\")] #[AttributeClass3()]", "class ^AttributeClass2 {");
    }

    public void testAttributes_b14() throws Exception {
        checkDeclaration(getTestPath(), "    public function method(#[Attribute^Class2(6, \"anonymous class method param\")] $param1): int|string {", "class ^AttributeClass2 {");
    }

    public void testAttributes_b15() throws Exception {
        checkDeclaration(getTestPath(), "    private static function staticMethod(#[AttributeClas^s2(7, \"anonymous class static method param\")] int|bool $pram1): int|string {", "class ^AttributeClass2 {");
    }

    public void testAttributes_b16() throws Exception {
        checkDeclaration(getTestPath(), "    #[Attribute^Class2(2, \"interface const\")]", "class ^AttributeClass2 {");
    }

    public void testAttributes_b17() throws Exception {
        checkDeclaration(getTestPath(), "    #[Attribut^eClass2(self::CONSTANT_INTERFACE, \"interface method\")] #[AttributeClass3()]", "class ^AttributeClass2 {");
    }

    public void testAttributes_b18() throws Exception {
        checkDeclaration(getTestPath(), "    public function interfaceMethod(#[AttributeC^lass2(AttributedInterface::CONSTANT_INTERFACE, \"interface method param\")] $param1): int|string;", "class ^AttributeClass2 {");
    }

    public void testAttributes_b19() throws Exception {
        checkDeclaration(getTestPath(), "    #[Att^ributeClass2(4, \"interface static method\")] #[AttributeClass3()]", "class ^AttributeClass2 {");
    }

    public void testAttributes_b20() throws Exception {
        checkDeclaration(getTestPath(), "    public static function staticInterfaceMethod(#[Attribu^teClass2(4, \"interface static method param\" . Example::CONST_EXAMPLE)] $param1): int|string;", "class ^AttributeClass2 {");
    }

    public void testAttributes_b21() throws Exception {
        checkDeclaration(getTestPath(), "#[\\Attributes\\AttributeCla^ss2(1, \"enum\", new Example)]", "class ^AttributeClass2 {");
    }

    public void testAttributes_b22() throws Exception {
        checkDeclaration(getTestPath(), "    public function method(#[Attrib^uteClass2(4, \"enum method param\")] $param1): int|string {", "class ^AttributeClass2 {");
    }

    public void testAttributes_b23() throws Exception {
        checkDeclaration(getTestPath(), "#[Attribut^eClass2(1, \"function\")]", "class ^AttributeClass2 {");
    }

    public void testAttributes_b24() throws Exception {
        checkDeclaration(getTestPath(), "function func2(#[AttributeC^lass2(1 + \\Attributes\\CONST_1 + 1, Example::CONST_EXAMPLE . \"function param\")] int|string $param): void {}", "class ^AttributeClass2 {");
    }

    public void testAttributes_b25() throws Exception {
        checkDeclaration(getTestPath(), "$labmda2 = #[Att^ributeClass2(2, \"closure\")] #[AttributeClass3(2, \"closurr\")] function(#[AttributeClass2(int: 2 * \\Attributes\\CONST_1, string: \"closure param\" . Example::CONST_EXAMPLE)] $test): void {};", "class ^AttributeClass2 {");
    }

    public void testAttributes_b26() throws Exception {
        checkDeclaration(getTestPath(), "$labmda2 = #[AttributeClass2(2, \"closure\")] #[AttributeClass3(2, \"closurr\")] function(#[AttributeCla^ss2(int: 2 * \\Attributes\\CONST_1, string: \"closure param\" . Example::CONST_EXAMPLE)] $test): void {};", "class ^AttributeClass2 {");
    }

    public void testAttributes_b27() throws Exception {
        checkDeclaration(getTestPath(), "$arrow2 = #[AttributeClass1(2, \"arrow\"), Attri^buteClass2(2, \"arrow\")] fn(#[AttributeClass1(\\Attributes\\CONST_1 / 5, \"arrow param\" . Example::class)] $test): int|string => 100;", "class ^AttributeClass2 {");
    }

    public void testAttributes_c01() throws Exception {
        checkDeclaration(getTestPath(), "    #[AttributeClas^s3(6, \"trait static method\")]", "class ^AttributeClass3 {");
    }

    public void testAttributes_c02() throws Exception {
        checkDeclaration(getTestPath(), "    public static function staticTraitMethod(#[At^tributeClass3] int|string $param1): bool|int {", "class ^AttributeClass3 {");
    }

    public void testAttributes_c03() throws Exception {
        checkDeclaration(getTestPath(), "    #[At^tributeClass3(3, \"anonymous class field\")]", "class ^AttributeClass3 {");
    }

    public void testAttributes_c04() throws Exception {
        checkDeclaration(getTestPath(), "    #[AttributeClass2(6, \"anonymous class method\")] #[A^ttributeClass3()]", "class ^AttributeClass3 {");
    }

    public void testAttributes_c05() throws Exception {
        checkDeclaration(getTestPath(), "    #[AttributeClass2(self::CONSTANT_INTERFACE, \"interface method\")] #[Attribu^teClass3()]", "class ^AttributeClass3 {");
    }

    public void testAttributes_c06() throws Exception {
        checkDeclaration(getTestPath(), "    #[AttributeClass2(4, \"interface static method\")] #[Attribut^eClass3()]", "class ^AttributeClass3 {");
    }

    public void testAttributes_c07() throws Exception {
        checkDeclaration(getTestPath(), "#[\\Attributes\\Attribute^Class3(1, \"enum\"), AttributeClass1]", "class ^AttributeClass3 {");
    }

    public void testAttributes_c08() throws Exception {
        checkDeclaration(getTestPath(), "    #[AttributeClass1(4, \"enum method\")] #[Att^ributeClass3()]", "class ^AttributeClass3 {");
    }

    public void testAttributes_c09() throws Exception {
        checkDeclaration(getTestPath(), "    public static function staticMethod(#[AttributeC^lass3(5, \"enum static method param\")] int|bool $pram1): int|string {", "class ^AttributeClass3 {");
    }

    public void testAttributes_c10() throws Exception {
        checkDeclaration(getTestPath(), "    Att^ributeClass3(int: CONST_1, string: \"function\" . Example::class)", "class ^AttributeClass3 {");
    }

    public void testAttributes_c11() throws Exception {
        checkDeclaration(getTestPath(), "$labmda2 = #[AttributeClass2(2, \"closure\")] #[Attr^ibuteClass3(2, \"closurr\")] function(#[AttributeClass2(int: 2 * \\Attributes\\CONST_1, string: \"closure param\" . Example::CONST_EXAMPLE)] $test): void {};", "class ^AttributeClass3 {");
    }

    public void testAttributes_c12() throws Exception {
        checkDeclaration(getTestPath(), "$labmda3 = #[Attribute^Class3(3, \"closure\")] static function(): void {};", "class ^AttributeClass3 {");
    }

    public void testAttributes_d01() throws Exception {
        checkDeclaration(getTestPath(), "    AttributeClass3(int: CONS^T_1, string: \"function\" . Example::class)", "const ^CONST_1 = 1;");
    }

    public void testAttributes_d02() throws Exception {
        checkDeclaration(getTestPath(), "function func2(#[AttributeClass2(1 + \\Attributes\\CONS^T_1 + 1, Example::CONST_EXAMPLE . \"function param\")] int|string $param): void {}", "const ^CONST_1 = 1;");
    }

    public void testAttributes_d03() throws Exception {
        checkDeclaration(getTestPath(), "$labmda2 = #[AttributeClass2(2, \"closure\")] #[AttributeClass3(2, \"closurr\")] function(#[AttributeClass2(int: 2 * \\Attributes\\CO^NST_1, string: \"closure param\" . Example::CONST_EXAMPLE)] $test): void {};", "const ^CONST_1 = 1;");
    }

    public void testAttributes_d04() throws Exception {
        checkDeclaration(getTestPath(), "$arrow2 = #[AttributeClass1(2, \"arrow\"), AttributeClass2(2, \"arrow\")] fn(#[AttributeClass1(\\Attributes\\C^ONST_1 / 5, \"arrow param\" . Example::class)] $test): int|string => 100;", "const ^CONST_1 = 1;");
    }

    public void testAttributes_e01() throws Exception {
        checkDeclaration(getTestPath(), "    #[AttributeClass2(2, \"class const\", new Exa^mple())]", "class ^Example {");
    }

    public void testAttributes_e02() throws Exception {
        checkDeclaration(getTestPath(), "    #[AttributeClass1(4, \"class static field\"), AttributeClass2(4, \"class static field\", new \\Attributes\\E^xample)] // group", "class ^Example {");
    }

    public void testAttributes_e03() throws Exception {
        checkDeclaration(getTestPath(), "    public static function staticInterfaceMethod(#[AttributeClass2(4, \"interface static method param\" . Ex^ample::CONST_EXAMPLE)] $param1): int|string;", "class ^Example {");
    }

    public void testAttributes_e04() throws Exception {
        checkDeclaration(getTestPath(), "#[\\Attributes\\AttributeClass2(1, \"enum\", new E^xample)]", "class ^Example {");
    }

    public void testAttributes_e05() throws Exception {
        checkDeclaration(getTestPath(), "    AttributeClass3(int: CONST_1, string: \"function\" . Exam^ple::class)", "class ^Example {");
    }

    public void testAttributes_e06() throws Exception {
        checkDeclaration(getTestPath(), "function func2(#[AttributeClass2(1 + \\Attributes\\CONST_1 + 1, Ex^ample::CONST_EXAMPLE . \"function param\")] int|string $param): void {}", "class ^Example {");
    }

    public void testAttributes_e07() throws Exception {
        checkDeclaration(getTestPath(), "$labmda2 = #[AttributeClass2(2, \"closure\")] #[AttributeClass3(2, \"closurr\")] function(#[AttributeClass2(int: 2 * \\Attributes\\CONST_1, string: \"closure param\" . E^xample::CONST_EXAMPLE)] $test): void {};", "class ^Example {");
    }

    public void testAttributes_e08() throws Exception {
        checkDeclaration(getTestPath(), "$arrow2 = #[AttributeClass1(2, \"arrow\"), AttributeClass2(2, \"arrow\")] fn(#[AttributeClass1(\\Attributes\\CONST_1 / 5, \"arrow param\" . Exa^mple::class)] $test): int|string => 100;", "class ^Example {");
    }

    public void testAttributes_e09() throws Exception {
        checkDeclaration(getTestPath(), "$arrow3 = #[AttributeClass1(3, string: E^xample::CONST_EXAMPLE . \"arrow\")] static fn(): int|string => 100;", "class ^Example {");
    }

    public void testAttributes_f01() throws Exception {
        checkDeclaration(getTestPath(), "    public static function staticInterfaceMethod(#[AttributeClass2(4, \"interface static method param\" . Example::CONST_EXA^MPLE)] $param1): int|string;", "    public const string ^CONST_EXAMPLE = \"example\";");
    }

    public void testAttributes_f02() throws Exception {
        checkDeclaration(getTestPath(), "function func2(#[AttributeClass2(1 + \\Attributes\\CONST_1 + 1, Example::CONS^T_EXAMPLE . \"function param\")] int|string $param): void {}", "    public const string ^CONST_EXAMPLE = \"example\";");
    }

    public void testAttributes_f03() throws Exception {
        checkDeclaration(getTestPath(), "$labmda2 = #[AttributeClass2(2, \"closure\")] #[AttributeClass3(2, \"closurr\")] function(#[AttributeClass2(int: 2 * \\Attributes\\CONST_1, string: \"closure param\" . Example::CO^NST_EXAMPLE)] $test): void {};", "    public const string ^CONST_EXAMPLE = \"example\";");
    }

    public void testAttributes_f04() throws Exception {
        checkDeclaration(getTestPath(), "$arrow3 = #[AttributeClass1(3, string: Example::CONST_EXAM^PLE . \"arrow\")] static fn(): int|string => 100;", "    public const string ^CONST_EXAMPLE = \"example\";");
    }

    public void testAttributes_g01() throws Exception {
        checkDeclaration(getTestPath(), "#[AttributeClass1(1, self::CONSTA^NT_CLASS)]", "    public const ^CONSTANT_CLASS = 'constant';");
    }

}
