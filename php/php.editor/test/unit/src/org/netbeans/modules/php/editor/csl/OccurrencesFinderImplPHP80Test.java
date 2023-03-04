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

}
