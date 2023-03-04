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

}
