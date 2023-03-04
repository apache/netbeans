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

/**
 * @author Radek Matous
 */
public class OccurrencesFinderImplTest extends OccurrencesFinderImplTestBase {

    public OccurrencesFinderImplTest(String testName) {
        super(testName);
    }

    public void testGotoLabel() throws Exception {
        checkOccurrences(getTestPath(), "goto en^d;", true);
    }

    public void testOccurrencesInstanceMethod() throws Exception {
        checkOccurrences(getTestPath(), "$this->na^me();", true);
    }

    public void testOccurrencesDefines() throws Exception {
        checkOccurrences(getTestPath(), "echo \"fff\".t^est.\"ddddd\";", true);
    }

    public void testOccurrencesInstanceVarParam() throws Exception {
        checkOccurrences(getTestPath(), "$this->name = $na^me;", true);
    }
    public void testOccurrencesInstanceVarParam_1() throws Exception {
        checkOccurrences(getTestPath(), "$this->na^me = $name;", true);
    }

    public void testOccurrencesClassHeader() throws Exception {
        checkOccurrences(getTestPath(), "class fo^o", true);
    }

    public void testOccurrences1() throws Exception {
        checkOccurrences(getTestPath(), "function fo^o", true);
    }

    public void testOccurrences2() throws Exception {
        checkOccurrences(getTestPath(), "echo $na^me;", true);
    }

    public void testOccurrences3() throws Exception {
        checkOccurrences(getTestPath(), "echo $na^me;", true);
    }
    public void testOccurrences4() throws Exception {
        checkOccurrences(getTestPath(), "echo \"$na^me\";", true);
    }

    public void testGotoConstructTest() throws Exception {
        checkOccurrences(getTestPath(), "$a = new MyCla^ssConstr(", true);
    }

    public void testGotoConstructTest_2() throws Exception {
        checkOccurrences(getTestPath(), "$b = new MyClass^Constr2(", true);
    }

    public void testParamVarPropInPhpDocTest() throws Exception {
        checkOccurrences(getTestPath(), "* @param Book $he^llo", true);
    }

    public void testMarkReturnsOnConstructorTest() throws Exception {
        checkOccurrences(getTestPath(), "funct^ion __construct() {}//Auth", true);
    }

    public void testMarkReturnsOnConstructorTest_2() throws Exception {
        checkOccurrences(getTestPath(), "funct^ion __construct() {}//Bo", true);
    }

    public void testClsVarPropInPhpDocTest() throws Exception {
        checkOccurrences(getTestPath(), "* @return Aut^hor", true);
    }

    public void testIfaceTest() throws Exception {
        checkOccurrences(getTestPath(), "class mycls implements my^face", true);
    }

    public void testIfaceTest_2() throws Exception {
        checkOccurrences(getTestPath(), "const REC^OVER_ORIG = ", true);
    }

    public void testIfaceTest_3() throws Exception {
        checkOccurrences(getTestPath(), "class my^cls implements myface", true);
    }

    public void testIfaceTest_4() throws Exception {
        checkOccurrences(getTestPath(), "const RECOV^ER_ORIG = ", true);
    }

    public void testMarkClsIface() throws Exception {
        checkOccurrences(getTestPath(), "class clsDecla^ration implements ifaceDeclaration ", true);
    }

    public void testMarkClsIface_2() throws Exception {
        checkOccurrences(getTestPath(), "class clsDeclaration3 extends clsDec^laration ", true);
    }

    public void testMarkClsIface_3() throws Exception {
        checkOccurrences(getTestPath(), "interface ifaceDec^laration ", true);
    }

    public void testMarkClsIface_4() throws Exception {
        checkOccurrences(getTestPath(), "interface ifaceDeclaration2 extends ifaceDecl^aration  ", true);
    }

    public void testMarkClsIface_5() throws Exception {
        checkOccurrences(getTestPath(), "class clsDeclaration implements ifaceDeclara^tion ", true);
    }

    public void testMarkClsIface_6() throws Exception {
        checkOccurrences(getTestPath(), "class clsDeclaration2 implements ifaceDecla^ration, ifaceDeclaration2 ", true);
    }

    public void testMarkClsIface_7() throws Exception {
        checkOccurrences(getTestPath(), "interface ifaceDecl^aration2 extends ifaceDeclaration  ", true);
    }

    public void testMarkClsIface_8() throws Exception {
        checkOccurrences(getTestPath(), "class clsDeclaration2 implements ifaceDeclaration, ifaceDecl^aration2 ", true);
    }

    public void testMarkClsIface_9() throws Exception {
        checkOccurrences(getTestPath(), "$ifaceDec^laration = ", true);
    }

    public void testMarkClsIface_10() throws Exception {
        checkOccurrences(getTestPath(), "$ifaceDeclarati^on2 = ", true);
    }

    public void testMarkClsIface_11() throws Exception {
        checkOccurrences(getTestPath(), "$iface^Declaration4 = ", true);
    }

    public void testMarkClsIface_12() throws Exception {
        checkOccurrences(getTestPath(), "$clsDec^laration  = ", true);
    }

    public void testMarkClsIface_13() throws Exception {
        checkOccurrences(getTestPath(), "$clsDec^laration2 = ", true);
    }

    public void testMarkClsIface_14() throws Exception {
        checkOccurrences(getTestPath(), "$clsDec^laration4 = ", true);
    }

    public void testMarkClsIface_15() throws Exception {
        checkOccurrences(getTestPath(), "$clsDeclar^ation3 = ", true);
    }

    public void testMarkClsIface_16() throws Exception {
        checkOccurrences(getTestPath(), "function ifaceDe^claration()", true);
    }

    public void testMarkClsIface_17() throws Exception {
        checkOccurrences(getTestPath(), "function ifaceDe^claration2() ", true);
    }

    public void testMarkClsIface_18() throws Exception {
        checkOccurrences(getTestPath(), "function ifaceDe^claration4() ", true);
    }

    public void testMarkClsIface_19() throws Exception {
        checkOccurrences(getTestPath(), "function clsDecla^ration() ", true);
    }

    public void testMarkClsIface_20() throws Exception {
        checkOccurrences(getTestPath(), "function clsDecla^ration2() ", true);
    }

    public void testMarkClsIface_21() throws Exception {
        checkOccurrences(getTestPath(), "function clsDecla^ration3() ", true);
    }

    public void testMarkClsIface_22() throws Exception {
        checkOccurrences(getTestPath(), "function clsDecla^ration4() ", true);
    }

    public void testMarkArray() throws Exception {
        checkOccurrences(getTestPath(), "private static $stat^ic_array = array('', 'thousand ', 'million ', 'billion '", true);
    }

    public void testMarkArray_2() throws Exception {
        checkOccurrences(getTestPath(), "$result .= self::$st^atic_array[$instance_array[$idx]", true);
    }

    public void testMarkArray_3() throws Exception {
        checkOccurrences(getTestPath(), "private $fi^eld_array = array('', 'thousand ', 'million ', 'billion '", true);
    }

    public void testMarkArray_4() throws Exception {
        checkOccurrences(getTestPath(), "$result .= $this->fiel^d_array[$instance_array[$idx]", true);
    }

    public void testMarkArray_5() throws Exception {
        checkOccurrences(getTestPath(), "$instance_a^rray = array('', 'thousand ', 'million ', 'billion '", true);
    }

    public void testMarkArray_6() throws Exception {
        checkOccurrences(getTestPath(), "$result .= self::$static_array[$instanc^e_array[$idx]", true);
    }

    public void testMarkArray_7() throws Exception {
        checkOccurrences(getTestPath(), "$i^dx = ", true);
    }

    public void testMarkArray_8() throws Exception {
        checkOccurrences(getTestPath(), "$instance_array[$i^dx", true);
    }

    public void testMarkArray_9() throws Exception {
        checkOccurrences(getTestPath(), "$i^dx2 = ", true);
    }

    public void testMarkArray_10() throws Exception {
        checkOccurrences(getTestPath(), "$instance_array2[$id^x2", true);
    }

    public void testMarkArray_11() throws Exception {
        checkOccurrences(getTestPath(), "$i^dx3 = ", true);
    }

    public void testMarkArray_12() throws Exception {
        checkOccurrences(getTestPath(), "$instance_array3[$id^x3", true);
    }

    public void testMarkArray_13() throws Exception {
        checkOccurrences(getTestPath(), "$instan^ce_array2 = array('', 'thousand ', 'million ', 'billion '", true);
    }

    public void testMarkArray_14() throws Exception {
        checkOccurrences(getTestPath(), "$instan^ce_array2[$idx2", true);
    }

    public void testMarkArray_15() throws Exception {
        checkOccurrences(getTestPath(), "$instan^ce_array3 = array('', 'thousand ', 'million ', 'billion '", true);
    }

    public void testVardoc166660() throws Exception {
        checkOccurrences(getTestPath(), "@var $testClass Test^Class", true);
    }
    public void testVardoc166660_1() throws Exception {
        checkOccurrences(getTestPath(), "@var $test^Class TestClass", true);
    }

    public void testMagicMethod171249() throws Exception {
        checkOccurrences(getTestPath(), "class OldC^lass {", true);
    }

    public void testInstanceof198909_01() throws Exception {
        checkOccurrences(getTestPath(), "$mExpectedE^xception = null", true);
    }

    public void testInstanceof198909_02() throws Exception {
        checkOccurrences(getTestPath(), "} catch (Exception $^e) {", true);
    }

    public void testInstanceof198909_03() throws Exception {
        checkOccurrences(getTestPath(), "$e instanceof $mExpect^edException", true);
    }

    public void testIssue198449_01() throws Exception {
        checkOccurrences(getTestPath(), "$cl^ass = 'StdClass';", true);
    }

    public void testIssue201429_01() throws Exception {
        checkOccurrences(getTestPath(), "protected static function test($keyC^losure)", true);
    }

    public void testIssue200399_01() throws Exception {
        checkOccurrences(getTestPath(), "function functionName(\\Character\\Ma^nager", true);
    }

    public void testIssue201671() throws Exception {
        checkOccurrences(getTestPath(), "$array as $my^Key", true);
    }

    public void testIssue133465_01() throws Exception {
        checkOccurrences(getTestPath(), "private $U^RL;", true);
    }

    public void testIssue133465_02() throws Exception {
        checkOccurrences(getTestPath(), "st $this->$U^RL", true);
    }

    public void testIssue133465_03() throws Exception {
        checkOccurrences(getTestPath(), "return $this->$U^RL;", true);
    }

    public void testIssue197283_01() throws Exception {
        checkOccurrences(getTestPath(), "$fu^nc = 'someFunc';", true);
    }

    public void testIssue197283_02() throws Exception {
        checkOccurrences(getTestPath(), "$fu^nc();", true);
    }

    public void testIssue197283_03() throws Exception {
        checkOccurrences(getTestPath(), "$o^bj = 'MyObj';", true);
    }

    public void testIssue197283_04() throws Exception {
        checkOccurrences(getTestPath(), "$x = new $o^bj;", true);
    }

    public void testIssue197283_05() throws Exception {
        checkOccurrences(getTestPath(), "$another^Obj = 'AnotherObj';", true);
    }

    public void testIssue197283_06() throws Exception {
        checkOccurrences(getTestPath(), "$y = new $another^Obj();", true);
    }

    public void testIssue203419_01() throws Exception {
        checkOccurrences(getTestPath(), "class MyClass20^3419", true);
    }

    public void testIssue203419_02() throws Exception {
        checkOccurrences(getTestPath(), "* @var \\test\\sub\\MyClass203^419", true);
    }

    public void testIssue203419_03() throws Exception {
        checkOccurrences(getTestPath(), "public function test2(MyClass^203419 $param) {", true);
    }

    public void testIssue203419_04() throws Exception {
        checkOccurrences(getTestPath(), "$v1 = new \\test\\sub\\MyClass20^3419();", true);
    }

    public void testIssue203419_05() throws Exception {
        checkOccurrences(getTestPath(), "$v2 = new MyClass203^419();", true);
    }

    public void testIssue203419_06() throws Exception {
        checkOccurrences(getTestPath(), "$v3 = new sub\\MyClass20^3419();", true);
    }

    public void testIssue203419_07() throws Exception {
        checkOccurrences(getTestPath(), "$v4 = new baf\\MyClass203^419();", true);
    }

    public void testIssue204433_01() throws Exception {
        checkOccurrences(getTestPath(), "$form = new Edit^Form();", true);
    }

    public void testIssue204433_02() throws Exception {
        checkOccurrences(getTestPath(), "$form = new E^F()", true);
    }

    public void testIssue204433_03() throws Exception {
        checkOccurrences(getTestPath(), "$fr = new Edit^Form();", true);
    }

    public void testArrayDereferencing_01() throws Exception {
        checkOccurrences(getTestPath(), "$myCl^ass->field[0]->getArray()[][]->foo();", true);
    }

    public void testArrayDereferencing_02() throws Exception {
        checkOccurrences(getTestPath(), "$myClass->fie^ld[0]->getArray()[][]->foo();", true);
    }

    public void testArrayDereferencing_03() throws Exception {
        checkOccurrences(getTestPath(), "$myClass->field[0]->getA^rray()[][]->foo();", true);
    }

    public void testArrayDereferencing_04() throws Exception {
        checkOccurrences(getTestPath(), "$myClass->field[0]->getArray()[][]->fo^o();", true);
    }

    public void testArrayDereferencing_05() throws Exception {
        checkOccurrences(getTestPath(), "$myC^lass->getArray()[0][]->foo();", true);
    }

    public void testArrayDereferencing_06() throws Exception {
        checkOccurrences(getTestPath(), "$myClass->getA^rray()[0][]->foo();", true);
    }

    public void testArrayDereferencing_07() throws Exception {
        checkOccurrences(getTestPath(), "$myClass->getArray()[0][]->fo^o();", true);
    }

    public void testArrayDereferencing_08() throws Exception {
        checkOccurrences(getTestPath(), "function^Name()[0]->foo();", true);
    }

    public void testArrayDereferencing_09() throws Exception {
        checkOccurrences(getTestPath(), "functionName()[0]->fo^o();", true);
    }

    public void testVariableAsAClassName() throws Exception {
        checkOccurrences(getTestPath(), "$static_clas^sname::$static_property;", true);
    }

    public void testStaticMethodCall() throws Exception {
        checkOccurrences(getTestPath(), "Presenter::staticFun^ctionName($param);", true);
    }

    public void testIssue209187_01() throws Exception {
        checkOccurrences(getTestPath(), "class Class^Name {", true);
    }

    public void testIssue209187_02() throws Exception {
        checkOccurrences(getTestPath(), "use \\Foo\\Bar\\Class^Name;", true);
    }

    public void testIssue209187_03() throws Exception {
        checkOccurrences(getTestPath(), "new Class^Name();", true);
    }

    public void testIssue208826_01() throws Exception {
        checkOccurrences(getTestPath(), "class Mo^del {}", true);
    }

    public void testIssue208826_02() throws Exception {
        checkOccurrences(getTestPath(), "* @var \\Mo^del", true);
    }

    public void testIssue208826_03() throws Exception {
        checkOccurrences(getTestPath(), "* @return \\Mo^del", true);
    }

    public void testIssue208826_04() throws Exception {
        checkOccurrences(getTestPath(), "class B^ag {}", true);
    }

    public void testIssue208826_05() throws Exception {
        checkOccurrences(getTestPath(), "* @param B\\B^ag $param", true);
    }

    public void testIssue208826_06() throws Exception {
        checkOccurrences(getTestPath(), "function functionName1(B\\B^ag $param) {", true);
    }

    public void testIssue200596_01() throws Exception {
        checkOccurrences(getTestPath(), "class Class^Name {", true);
    }

    public void testIssue200596_02() throws Exception {
        checkOccurrences(getTestPath(), "class Aliased^ClassName {", true);
    }

    public void testIssue200596_03() throws Exception {
        checkOccurrences(getTestPath(), "use \\Foo\\Bar as O^mg;", true);
    }

    public void testIssue200596_04() throws Exception {
        checkOccurrences(getTestPath(), "use \\Foo\\Bar\\Aliased^ClassName as Cls;", true);
    }

    public void testIssue200596_05() throws Exception {
        checkOccurrences(getTestPath(), "use \\Foo\\Bar\\AliasedClassName as C^ls;", true);
    }

    public void testIssue200596_06() throws Exception {
        checkOccurrences(getTestPath(), "use \\Foo\\Bar\\Class^Name;", true);
    }

    public void testIssue200596_07() throws Exception {
        checkOccurrences(getTestPath(), "(new O^mg\\AliasedClassName())->bar();", true);
    }

    public void testIssue200596_08() throws Exception {
        checkOccurrences(getTestPath(), "(new Omg\\Aliased^ClassName())->bar();", true);
    }

    public void testIssue200596_09() throws Exception {
        checkOccurrences(getTestPath(), "(new C^ls())->bar();", true);
    }

    public void testIssue200596_10() throws Exception {
        checkOccurrences(getTestPath(), "(new Class^Name())->bar();", true);
    }

    public void testIssue200596_11() throws Exception {
        checkOccurrences(getTestPath(), "new O^mg\\AliasedClassName();", true);
    }

    public void testIssue200596_12() throws Exception {
        checkOccurrences(getTestPath(), "new Omg\\Aliased^ClassName();", true);
    }

    public void testIssue200596_13() throws Exception {
        checkOccurrences(getTestPath(), "new C^ls();", true);
    }

    public void testIssue200596_14() throws Exception {
        checkOccurrences(getTestPath(), "new Class^Name();", true);
    }

    public void testIssue200596_15() throws Exception {
        checkOccurrences(getTestPath(), "O^mg\\AliasedClassName::foo();", true);
    }

    public void testIssue200596_16() throws Exception {
        checkOccurrences(getTestPath(), "Omg\\Aliased^ClassName::foo();", true);
    }

    public void testIssue200596_17() throws Exception {
        checkOccurrences(getTestPath(), "C^ls::foo();", true);
    }

    public void testIssue200596_18() throws Exception {
        checkOccurrences(getTestPath(), "Class^Name::bar();", true);
    }

    public void testIssue200596_19() throws Exception {
        checkOccurrences(getTestPath(), "O^mg\\AliasedClassName::FOO;", true);
    }

    public void testIssue200596_20() throws Exception {
        checkOccurrences(getTestPath(), "Omg\\Aliased^ClassName::FOO;", true);
    }

    public void testIssue200596_21() throws Exception {
        checkOccurrences(getTestPath(), "C^ls::FOO;", true);
    }

    public void testIssue200596_22() throws Exception {
        checkOccurrences(getTestPath(), "Class^Name::BAR;", true);
    }

    public void testIssue200596_23() throws Exception {
        checkOccurrences(getTestPath(), "O^mg\\AliasedClassName::$foo;", true);
    }

    public void testIssue200596_24() throws Exception {
        checkOccurrences(getTestPath(), "Omg\\Aliased^ClassName::$foo;", true);
    }

    public void testIssue200596_25() throws Exception {
        checkOccurrences(getTestPath(), "C^ls::$foo;", true);
    }

    public void testIssue200596_26() throws Exception {
        checkOccurrences(getTestPath(), "Class^Name::$bar;", true);
    }

    public void testIssue200596_27() throws Exception {
        checkOccurrences(getTestPath(), "if ($x instanceof O^mg\\AliasedClassName) {}", true);
    }

    public void testIssue200596_28() throws Exception {
        checkOccurrences(getTestPath(), "if ($x instanceof Omg\\Aliased^ClassName) {}", true);
    }

    public void testIssue200596_29() throws Exception {
        checkOccurrences(getTestPath(), "if ($x instanceof C^ls) {}", true);
    }

    public void testIssue200596_30() throws Exception {
        checkOccurrences(getTestPath(), "if ($x instanceof Class^Name) {}", true);
    }

    public void testFieldAccessInInstanceOf_01() throws Exception {
        checkOccurrences(getTestPath(), "if ($a instanceof $this->bb^bbb) {}", true);
    }

    public void testFieldAccessInInstanceOf_02() throws Exception {
        checkOccurrences(getTestPath(), "public $bb^bbb;", true);
    }

    public void testIssue209309_01() throws Exception {
        checkOccurrences(getTestPath(), "class Aliased^ClassName {}", true);
    }

    public void testIssue209309_02() throws Exception {
        checkOccurrences(getTestPath(), "use \\Foo\\Bar as O^mg;", true);
    }

    public void testIssue209309_03() throws Exception {
        checkOccurrences(getTestPath(), "use \\Foo\\Bar\\Aliased^ClassName as Cls;", true);
    }

    public void testIssue209309_04() throws Exception {
        checkOccurrences(getTestPath(), "use \\Foo\\Bar\\AliasedClassName as C^ls;", true);
    }

    public void testIssue209309_05() throws Exception {
        checkOccurrences(getTestPath(), "function bar(O^mg\\AliasedClassName $p, Cls $a) {}", true);
    }

    public void testIssue209309_06() throws Exception {
        checkOccurrences(getTestPath(), "function bar(Omg\\Aliased^ClassName $p, Cls $a) {}", true);
    }

    public void testIssue209309_07() throws Exception {
        checkOccurrences(getTestPath(), "function bar(Omg\\AliasedClassName $p, C^ls $a) {}", true);
    }

    public void testIssue209308_01() throws Exception {
        checkOccurrences(getTestPath(), "use \\Foo\\Bar as Om^g;", true);
    }

    public void testIssue209308_02() throws Exception {
        checkOccurrences(getTestPath(), "use \\Foo\\Bar\\AliasedClassName as Cl^s;", true);
    }

    public void testIssue209308_03() throws Exception {
        checkOccurrences(getTestPath(), "/** @var Cl^s */", true);
    }

    public void testIssue209308_04() throws Exception {
        checkOccurrences(getTestPath(), "/** @var Om^g\\AliasedClassName */", true);
    }

    public void testIssue209308_05() throws Exception {
        checkOccurrences(getTestPath(), "* @return Om^g\\AliasedClassName", true);
    }

    public void testIssue209308_08() throws Exception {
        checkOccurrences(getTestPath(), "* @param Om^g\\AliasedClassName $p", true);
    }

    public void testIssue209308_09() throws Exception {
        checkOccurrences(getTestPath(), "* @param Cl^s $a", true);
    }

    public void testIssue209308_010() throws Exception {
        checkOccurrences(getTestPath(), "* @return Cl^s", true);
    }

    public void testIssue209308_011() throws Exception {
        checkOccurrences(getTestPath(), "function bar(Om^g\\AliasedClassName $p, Cls $a, \\Foo\\Bar\\AliasedClassName $name) {}", true);
    }

    public void testIssue209308_012() throws Exception {
        checkOccurrences(getTestPath(), "function bar(Omg\\AliasedClassName $p, Cl^s $a, \\Foo\\Bar\\AliasedClassName $name) {}", true);
    }

    public void testIssue209308_013() throws Exception {
        checkOccurrences(getTestPath(), "class Aliased^ClassName {}", true);
    }

    public void testIssue209308_014() throws Exception {
        checkOccurrences(getTestPath(), "use \\Foo\\Bar\\Aliased^ClassName as Cls;", true);
    }

    public void testIssue209308_015() throws Exception {
        checkOccurrences(getTestPath(), "/** @var Omg\\Aliased^ClassName */", true);
    }

    public void testIssue209308_016() throws Exception {
        checkOccurrences(getTestPath(), "* @return Omg\\Aliased^ClassName", true);
    }

    public void testIssue209308_018() throws Exception {
        checkOccurrences(getTestPath(), "* @param Omg\\Aliased^ClassName $p", true);
    }

    public void testIssue209308_019() throws Exception {
        checkOccurrences(getTestPath(), "* @param \\Foo\\Bar\\Aliased^ClassName $name Description", true);
    }

    public void testIssue209308_020() throws Exception {
        checkOccurrences(getTestPath(), "function bar(Omg\\Aliased^ClassName $p, Cls $a, \\Foo\\Bar\\AliasedClassName $name) {}", true);
    }

    public void testIssue209308_021() throws Exception {
        checkOccurrences(getTestPath(), "function bar(Omg\\AliasedClassName $p, Cls $a, \\Foo\\Bar\\Aliased^ClassName $name) {}", true);
    }

    public void testStaticAccessWithNs_01() throws Exception {
        checkOccurrences(getTestPath(), "const B^AR = 2;", true);
    }

    public void testStaticAccessWithNs_02() throws Exception {
        checkOccurrences(getTestPath(), "public static $b^ar;", true);
    }

    public void testStaticAccessWithNs_03() throws Exception {
        checkOccurrences(getTestPath(), "static function b^ar() {}", true);
    }

    public void testStaticAccessWithNs_04() throws Exception {
        checkOccurrences(getTestPath(), "const F^OO = 1;", true);
    }

    public void testStaticAccessWithNs_05() throws Exception {
        checkOccurrences(getTestPath(), "public static $f^oo;", true);
    }

    public void testStaticAccessWithNs_06() throws Exception {
        checkOccurrences(getTestPath(), "static function f^oo() {}", true);
    }

    public void testStaticAccessWithNs_07() throws Exception {
        checkOccurrences(getTestPath(), "Omg\\AliasedClassName::f^oo();", true);
    }

    public void testStaticAccessWithNs_08() throws Exception {
        checkOccurrences(getTestPath(), "Cls::f^oo();", true);
    }

    public void testStaticAccessWithNs_09() throws Exception {
        checkOccurrences(getTestPath(), "ClassName::b^ar();", true);
    }

    public void testStaticAccessWithNs_10() throws Exception {
        checkOccurrences(getTestPath(), "Omg\\AliasedClassName::F^OO;", true);
    }

    public void testStaticAccessWithNs_11() throws Exception {
        checkOccurrences(getTestPath(), "Cls::F^OO;", true);
    }

    public void testStaticAccessWithNs_12() throws Exception {
        checkOccurrences(getTestPath(), "ClassName::B^AR;", true);
    }

    public void testStaticAccessWithNs_13() throws Exception {
        checkOccurrences(getTestPath(), "Omg\\AliasedClassName::$f^oo;", true);
    }

    public void testStaticAccessWithNs_14() throws Exception {
        checkOccurrences(getTestPath(), "Cls::$f^oo;", true);
    }

    public void testStaticAccessWithNs_15() throws Exception {
        checkOccurrences(getTestPath(), "ClassName::$b^ar;", true);
    }

    public void testStaticAccessWithNs_16() throws Exception {
        checkOccurrences(getTestPath(), "\\Foo\\Bar\\ClassName::$b^ar;", true);
    }

    public void testStaticAccessWithNs_17() throws Exception {
        checkOccurrences(getTestPath(), "\\Foo\\Bar\\ClassName::b^ar();", true);
    }

    public void testStaticAccessWithNs_18() throws Exception {
        checkOccurrences(getTestPath(), "\\Foo\\Bar\\ClassName::B^AR;", true);
    }

    public void testIssue207971_01() throws Exception {
        checkOccurrences(getTestPath(), "private $fie^ld1;", true);
    }

    public void testIssue207971_02() throws Exception {
        checkOccurrences(getTestPath(), "private $fie^ld3;", true);
    }

    public void testIssue207971_03() throws Exception {
        checkOccurrences(getTestPath(), "private $obj^ect2;", true);
    }

    public void testIssue207971_04() throws Exception {
        checkOccurrences(getTestPath(), "$sql = \" {$this->fie^ld1} {$this->object2->xxx} {$this->field3['array1']} \";", true);
    }

    public void testIssue207971_05() throws Exception {
        checkOccurrences(getTestPath(), "$sql = \" {$this->field1} {$this->obj^ect2->xxx} {$this->field3['array1']} \";", true);
    }

    public void testIssue207971_06() throws Exception {
        checkOccurrences(getTestPath(), "$sql = \" {$this->field1} {$this->object2->xxx} {$this->fie^ld3['array1']} \";", true);
    }

    public void testQualifiedUseStatement_01() throws Exception {
        checkOccurrences(getTestPath(), "class Kit^chen {", true);
    }

    public void testQualifiedUseStatement_02() throws Exception {
        checkOccurrences(getTestPath(), "use pl\\dagguh\\someproject\\rooms\\Kit^chen;", true);
    }

    public void testQualifiedUseStatement_03() throws Exception {
        checkOccurrences(getTestPath(), "Kit^chen::DEFAULT_SIZE;", true);
    }

    public void testQualifiedUseStatement_04() throws Exception {
        checkOccurrences(getTestPath(), "use pl\\dagguh\\someproject\\rooms\\Kit^chen as Alias;", true);
    }

    public void testIssue208245_01() throws Exception {
        checkOccurrences(getTestPath(), "$glob^Var = \"\";", true);
    }

    public void testIssue208245_02() throws Exception {
        checkOccurrences(getTestPath(), "function() use($glob^Var) {", true);
    }

    public void testIssue208245_03() throws Exception {
        checkOccurrences(getTestPath(), "echo $glob^Var;", true);
    }

    public void testIssue208245_04() throws Exception {
        checkOccurrences(getTestPath(), "$v^ar = \"\";", true);
    }

    public void testIssue208245_05() throws Exception {
        checkOccurrences(getTestPath(), "function() use($v^ar) {", true);
    }

    public void testIssue208245_06() throws Exception {
        checkOccurrences(getTestPath(), "echo $v^ar;", true);
    }

    public void testIssue203073_01() throws Exception {
        checkOccurrences(getTestPath(), "class First^Parent {", true);
    }

    public void testIssue203073_02() throws Exception {
        checkOccurrences(getTestPath(), "use Full\\Name\\Space\\First^Parent as SecondParent;", true);
    }

    public void testIssue203073_03() throws Exception {
        checkOccurrences(getTestPath(), "use Full\\Name\\Space\\First^Parent;", true);
    }

    public void testIssue203073_04() throws Exception {
        checkOccurrences(getTestPath(), "class Yours1 extends First^Parent {", true);
    }

    public void testIssue203073_05() throws Exception {
        checkOccurrences(getTestPath(), "use Full\\Name\\Space\\FirstParent as Second^Parent;", true);
    }

    public void testIssue203073_06() throws Exception {
        checkOccurrences(getTestPath(), "class Yours extends Second^Parent {", true);
    }

    public void testIssue203814_01() throws Exception {
        checkOccurrences(getTestPath(), "public function fMe^thod()", true);
    }

    public void testIssue203814_02() throws Exception {
        checkOccurrences(getTestPath(), "self::$first->fMe^thod();", true);
    }

    public void testIssue203814_03() throws Exception {
        checkOccurrences(getTestPath(), "static::$first->fMe^thod();", true);
    }

    public void testIssue203814_04() throws Exception {
        checkOccurrences(getTestPath(), "Second::$first->fMe^thod();", true);
    }

    public void testIssue207346_01() throws Exception {
        checkOccurrences(getTestPath(), "public $invalid^LinkMode;", true);
    }

    public void testIssue207346_02() throws Exception {
        checkOccurrences(getTestPath(), "$this->invalid^LinkMode = 10;", true);
    }

    public void testIssue207346_03() throws Exception {
        checkOccurrences(getTestPath(), "$this->invalid^LinkMode;", true);
    }

    public void testIssue207615_01() throws Exception {
        checkOccurrences(getTestPath(), "protected static $_v^ar = true;", true);
    }

    public void testIssue207615_02() throws Exception {
        checkOccurrences(getTestPath(), "self::$_v^ar;", true);
    }

    public void testIssue207615_03() throws Exception {
        checkOccurrences(getTestPath(), "return static::$_v^ar;", true);
    }

    public void testConstants_01() throws Exception {
        checkOccurrences(getTestPath(), "const C^ON = 1;", true);
    }

    public void testConstants_02() throws Exception {
        checkOccurrences(getTestPath(), "parent::C^ON;", true);
    }

    public void testConstants_03() throws Exception {
        checkOccurrences(getTestPath(), "self::C^ON;", true);
    }

    public void testConstants_04() throws Exception {
        checkOccurrences(getTestPath(), "static::C^ON;", true);
    }

    // #250579
    public void testConstantArrayAccess_01() throws Exception {
        checkOccurrences(getTestPath(), "const GLOBAL_C^ONSTANT1 = [0, 1];", true);
    }

    public void testConstantArrayAccess_02() throws Exception {
        checkOccurrences(getTestPath(), "const GLOBAL_CONSTANT2 = GL^OBAL_CONSTANT1[0];", true);
    }

    public void testConstantArrayAccess_03() throws Exception {
        checkOccurrences(getTestPath(), "const GLOBAL_CONSTANT3 = GLOBAL_^CONSTANT1[GLOBAL_CONSTANT1[0] + GLOBAL_CONSTANT1[0]];", true);
    }

    public void testConstantArrayAccess_04() throws Exception {
        checkOccurrences(getTestPath(), "const GLOBAL_CONSTANT3 = GLOBAL_CONSTANT1[GL^OBAL_CONSTANT1[0] + GLOBAL_CONSTANT1[0]];", true);
    }

    public void testConstantArrayAccess_05() throws Exception {
        checkOccurrences(getTestPath(), "const GLOBAL_CONSTANT3 = GLOBAL_CONSTANT1[GLOBAL_CONSTANT1[0] + GL^OBAL_CONSTANT1[0]];", true);
    }

    public void testConstantArrayAccess_06() throws Exception {
        checkOccurrences(getTestPath(), "const G^LOBAL_CONSTANT4 = [\"a\" => [0, 1], \"b\" => [\"c\", \"d\"]];", true);
    }

    public void testConstantArrayAccess_07() throws Exception {
        checkOccurrences(getTestPath(), "const GLOBAL_CONSTANT5 = GLOBAL_CO^NSTANT4[\"b\"][GLOBAL_CONSTANT1[1]];", true);
    }

    public void testConstantArrayAccess_08() throws Exception {
        checkOccurrences(getTestPath(), "const GLOBAL_CONSTANT5 = GLOBAL_CONSTANT4[\"b\"][GLOBAL_CONS^TANT1[1]];", true);
    }

    public void testConstantArrayAccess_09() throws Exception {
        checkOccurrences(getTestPath(), "GLOBAL_CONST^ANT1[$index];", true);
    }

    public void testConstantArrayAccess_10() throws Exception {
        checkOccurrences(getTestPath(), "[1][GLOBAL_CO^NSTANT1[0]];", true);
    }

    public void testConstantArrayAccess_11() throws Exception {
        checkOccurrences(getTestPath(), "echo GLOBAL_C^ONSTANT4[\"a\"][GLOBAL_CONSTANT1[$index]];", true);
    }

    public void testConstantArrayAccess_12() throws Exception {
        checkOccurrences(getTestPath(), "echo GLOBAL_CONSTANT4[\"a\"][GLOB^AL_CONSTANT1[$index]];", true);
    }

    public void testConstantArrayAccess_13() throws Exception {
        checkOccurrences(getTestPath(), "const CLASS_CO^NSTANT1 = [\"a\", \"b\"];", true);
    }

    public void testConstantArrayAccess_14() throws Exception {
        checkOccurrences(getTestPath(), "const CLASS_CONSTANT2 = self::CL^ASS_CONSTANT1[0];", true);
    }

    public void testConstantArrayAccess_15() throws Exception {
        checkOccurrences(getTestPath(), "const CLASS_CONSTANT3 = GLOB^AL_CONSTANT1[0] + GLOBAL_CONSTANT1[1];", true);
    }

    public void testConstantArrayAccess_16() throws Exception {
        checkOccurrences(getTestPath(), "const CLASS_CONSTANT3 = GLOBAL_CONSTANT1[0] + GLOBAL_CON^STANT1[1];", true);
    }

    public void testConstantArrayAccess_17() throws Exception {
        checkOccurrences(getTestPath(), "const CLASS_C^ONSTANT4 = [0, 1];", true);
    }

    public void testConstantArrayAccess_18() throws Exception {
        checkOccurrences(getTestPath(), "self::CLASS_CON^STANT1[GLOBAL_CONSTANT1[1]];", true);
    }

    public void testConstantArrayAccess_19() throws Exception {
        checkOccurrences(getTestPath(), "self::CLASS_CONSTANT1[GL^OBAL_CONSTANT1[1]];", true);
    }

    public void testConstantArrayAccess_20() throws Exception {
        checkOccurrences(getTestPath(), "self::CLASS_CON^STANT1[ConstantClass::CLASS_CONSTANT4[self::CLASS_CONSTANT4[0]]];", true);
    }

    public void testConstantArrayAccess_21() throws Exception {
        checkOccurrences(getTestPath(), "self::CLASS_CONSTANT1[ConstantClass::CLASS_C^ONSTANT4[self::CLASS_CONSTANT4[0]]];", true);
    }

    public void testConstantArrayAccess_22() throws Exception {
        checkOccurrences(getTestPath(), "self::CLASS_CONSTANT1[ConstantClass::CLASS_CONSTANT4[self::CLASS_CONS^TANT4[0]]];", true);
    }

    public void testConstantArrayAccess_23() throws Exception {
        checkOccurrences(getTestPath(), "ConstantClass::CLASS_CO^NSTANT1[$index];", true);
    }

    public void testConstantArrayAccess_24() throws Exception {
        checkOccurrences(getTestPath(), "\"String\"[ConstantClass::CLASS_CON^STANT4[0]];", true);
    }

    public void testConstantArrayAccess_25() throws Exception {
        checkOccurrences(getTestPath(), "ConstantInterface::IN^TERFACE_CONSTANT1[GLOBAL_CONSTANT1[1]];", true);
    }

    public void testConstantArrayAccess_26() throws Exception {
        checkOccurrences(getTestPath(), "ConstantInterface::INTERFACE_CONSTANT1[GL^OBAL_CONSTANT1[1]];", true);
    }

    public void testConstantArrayAccess_27() throws Exception {
        checkOccurrences(getTestPath(), "const IN^TERFACE_CONSTANT1 = [\"a\", \"b\"];", true);
    }

    public void testConstantArrayAccess_28() throws Exception {
        checkOccurrences(getTestPath(), "const INTERFACE_CONSTANT2 = self::IN^TERFACE_CONSTANT1[0];", true);
    }

    public void testConstantArrayAccess_29() throws Exception {
        checkOccurrences(getTestPath(), "const INTERFACE_CONSTANT3 = ConstantInterface::INTERFACE_CON^STANT1[0] . GLOBAL_CONSTANT1[1];", true);
    }

    public void testConstantArrayAccess_30() throws Exception {
        checkOccurrences(getTestPath(), "const INTERFACE_CONSTANT3 = ConstantInterface::INTERFACE_CONSTANT1[0] . GLOBAL_CON^STANT1[1];", true);
    }

    public void testStaticAccessWithNsAlias_01() throws Exception {
        checkOccurrences(getTestPath(), "const O^MG = 1;", true);
    }

    public void testStaticAccessWithNsAlias_02() throws Exception {
        checkOccurrences(getTestPath(), "parent::O^MG;", true);
    }

    public void testStaticAccessWithNsAlias_03() throws Exception {
        checkOccurrences(getTestPath(), "self::O^MG;", true);
    }

    public void testStaticAccessWithNsAlias_04() throws Exception {
        checkOccurrences(getTestPath(), "static::O^MG;", true);
    }

    public void testStaticAccessWithNsAlias_05() throws Exception {
        checkOccurrences(getTestPath(), "public static $static^Field = 2;", true);
    }

    public void testStaticAccessWithNsAlias_06() throws Exception {
        checkOccurrences(getTestPath(), "parent::$static^Field;", true);
    }

    public void testStaticAccessWithNsAlias_07() throws Exception {
        checkOccurrences(getTestPath(), "self::$static^Field;", true);
    }

    public void testStaticAccessWithNsAlias_08() throws Exception {
        checkOccurrences(getTestPath(), "static::$static^Field;", true);
    }

    public void testStaticAccessWithNsAlias_09() throws Exception {
        checkOccurrences(getTestPath(), "static function some^Func() {", true);
    }

    public void testStaticAccessWithNsAlias_10() throws Exception {
        checkOccurrences(getTestPath(), "parent::some^Func();", true);
    }

    public void testIssue211230_01() throws Exception {
        checkOccurrences(getTestPath(), "class F^oo {", true);
    }

    public void testIssue211230_02() throws Exception {
        checkOccurrences(getTestPath(), " * @method F^oo|Bar method() This is my cool magic method description.", true);
    }

    public void testIssue211230_03() throws Exception {
        checkOccurrences(getTestPath(), " * @method Foo|B^ar method() This is my cool magic method description.", true);
    }

    public void testIssue211230_04() throws Exception {
        checkOccurrences(getTestPath(), "class B^ar {", true);
    }

    public void testIssue211230_05() throws Exception {
        checkOccurrences(getTestPath(), "$b = new B^ar();", true);
    }

    public void testMagicMethod_01() throws Exception {
        checkOccurrences(getTestPath(), " * @method Foo|Bar met^hod() This is my cool magic method description.", true);
    }

    public void testMagicMethod_02() throws Exception {
        checkOccurrences(getTestPath(), "$b->met^hod()->fooMethod();", true);
    }

    public void testIssue211015_01() throws Exception {
        checkOccurrences(getTestPath(), "$f^oo = \"omg\";", true);
    }

    public void testIssue211015_02() throws Exception {
        checkOccurrences(getTestPath(), "$this->$f^oo();", true);
    }

    public void testIssue211015_03() throws Exception {
        checkOccurrences(getTestPath(), "self::$f^oo();", true);
    }

    public void testIssue211015_04() throws Exception {
        checkOccurrences(getTestPath(), "static::$f^oo();", true);
    }

    public void testIssue211015_05() throws Exception {
        checkOccurrences(getTestPath(), "parent::$f^oo();", true);
    }

    public void testIssue186553_01() throws Exception {
        checkOccurrences(getTestPath(), "public function do^Something()", true);
    }

    public void testIssue186553_02() throws Exception {
        checkOccurrences(getTestPath(), "$object1->do^Something();", true);
    }

    public void testIssue186553_03() throws Exception {
        checkOccurrences(getTestPath(), "$this->do^Something();", true);
    }

    public void testIssue213133_01() throws Exception {
        checkOccurrences(getTestPath(), "class Te^st {", true);
    }

    public void testIssue213133_02() throws Exception {
        checkOccurrences(getTestPath(), "echo $test->{Te^st::$CHECK};", true);
    }

    public void testIssue213133_03() throws Exception {
        checkOccurrences(getTestPath(), "echo Te^st::$CHECK;", true);
    }

    public void testIssue213133_04() throws Exception {
        checkOccurrences(getTestPath(), "public static $CH^ECK = \"check\";", true);
    }

    public void testIssue213133_05() throws Exception {
        checkOccurrences(getTestPath(), "echo $test->{Test::$CH^ECK};", true);
    }

    public void testIssue213133_06() throws Exception {
        checkOccurrences(getTestPath(), "echo Test::$CH^ECK;", true);
    }

    public void testIssue213584_01() throws Exception {
        checkOccurrences(getTestPath(), "trait A^A {", true);
    }

    public void testIssue213584_02() throws Exception {
        checkOccurrences(getTestPath(), "trait B^B {", true);
    }

    public void testIssue213584_03() throws Exception {
        checkOccurrences(getTestPath(), "trait C^C {", true);
    }

    public void testIssue213584_04() throws Exception {
        checkOccurrences(getTestPath(), "trait D^D {", true);
    }

    public void testIssue213584_05() throws Exception {
        checkOccurrences(getTestPath(), "use A^A, BB, CC, DD {", true);
    }

    public void testIssue213584_06() throws Exception {
        checkOccurrences(getTestPath(), "use AA, B^B, CC, DD {", true);
    }

    public void testIssue213584_07() throws Exception {
        checkOccurrences(getTestPath(), "use AA, BB, C^C, DD {", true);
    }

    public void testIssue213584_08() throws Exception {
        checkOccurrences(getTestPath(), "use AA, BB, CC, D^D {", true);
    }

    public void testIssue213584_09() throws Exception {
        checkOccurrences(getTestPath(), "C^C::bar insteadof AA, BB;", true);
    }

    public void testIssue213584_10() throws Exception {
        checkOccurrences(getTestPath(), "CC::bar insteadof A^A, BB;", true);
    }

    public void testIssue213584_11() throws Exception {
        checkOccurrences(getTestPath(), "CC::bar insteadof AA, B^B;", true);
    }

    public void testIssue213584_12() throws Exception {
        checkOccurrences(getTestPath(), "D^D::bar as foo;", true);
    }

    public void testIssue217357_01() throws Exception {
        checkOccurrences(getTestPath(), "class Str^ing {", true);
    }

    public void testIssue217357_02() throws Exception {
        checkOccurrences(getTestPath(), "use Abc\\Str^ing;", true);
    }

    public void testIssue217357_03() throws Exception {
        checkOccurrences(getTestPath(), "$s = new Str^ing();", true);
    }

    public void testCatchWithAlias_01() throws Exception {
        checkOccurrences(getTestPath(), "use Blah\\Sec as B^S;", true);
    }

    public void testCatchWithAlias_02() throws Exception {
        checkOccurrences(getTestPath(), "new B^S\\MyException();", true);
    }

    public void testCatchWithAlias_03() throws Exception {
        checkOccurrences(getTestPath(), "} catch (B^S\\MyException $ex) {", true);
    }

    public void testIssue216876_01() throws Exception {
        checkOccurrences(getTestPath(), "class MyNewCl^ass123 {", true);
    }

    public void testIssue216876_02() throws Exception {
        checkOccurrences(getTestPath(), "public function MyNewCl^ass123($foo) {", true);
    }

    public void testIssue216876_03() throws Exception {
        checkOccurrences(getTestPath(), "$c = new \\Foo\\MyNewCl^ass123();", true);
    }

    public void testIssue218487_01() throws Exception {
        checkOccurrences(getTestPath(), "use Zend\\Stdlib2\\DispatchableInterface2 as Dispatch^able2;", true);
    }

    public void testIssue218487_02() throws Exception {
        checkOccurrences(getTestPath(), "class AbstractController implements Dispatch^able2 {", true);
    }

    public void testIssue223076_01() throws Exception {
        checkOccurrences(getTestPath(), "func^tion functionName($param) {", true);
    }

    public void testIssue223076_02() throws Exception {
        checkOccurrences(getTestPath(), "retur^n 5;", true);
    }

    public void testIssue223076_03() throws Exception {
        checkOccurrences(getTestPath(), "retur^n 10;", true);
    }

    public void testReflectionVariableInMethodInvocation_01() throws Exception {
        checkOccurrences(getTestPath(), "private $cont^ext;", true);
    }

    public void testReflectionVariableInMethodInvocation_02() throws Exception {
        checkOccurrences(getTestPath(), "$this->cont^ext[0]", true);
    }

    public void testIssue217360_01() throws Exception {
        checkOccurrences(getTestPath(), "private function get^Two()", true);
    }

    public void testIssue217360_02() throws Exception {
        checkOccurrences(getTestPath(), "$two = $this->get^Two();", true);
    }

    public void testIssue217360_03() throws Exception {
        checkOccurrences(getTestPath(), "return $two->get^Two();", true);
    }

    public void testIssue217360_04() throws Exception {
        checkOccurrences(getTestPath(), "(new Two)->get^Two();", true);
    }

    public void testUseFuncAndConst_01() throws Exception {
        checkOccurrences(getTestPath(), "use const Name\\Space\\F^OO;", true);
    }

    public void testUseFuncAndConst_02() throws Exception {
        checkOccurrences(getTestPath(), "use const Name\\Space\\F^OO as FOO2;", true);
    }

    public void testUseFuncAndConst_03() throws Exception {
        checkOccurrences(getTestPath(), "use const Name\\Space\\FOO as F^OO2;", true);
    }

    public void testUseFuncAndConst_04() throws Exception {
        checkOccurrences(getTestPath(), "use function Name\\Space\\f^nc;", true);
    }

    public void testUseFuncAndConst_05() throws Exception {
        checkOccurrences(getTestPath(), "use function Name\\Space\\f^nc as fnc2;", true);
    }

    public void testUseFuncAndConst_06() throws Exception {
        checkOccurrences(getTestPath(), "use function Name\\Space\\fnc as f^nc2;", true);
    }

    public void testUseFuncAndConst_07() throws Exception {
        checkOccurrences(getTestPath(), "echo F^OO;", true);
    }

    public void testUseFuncAndConst_08() throws Exception {
        checkOccurrences(getTestPath(), "echo F^OO2;", true);
    }

    public void testUseFuncAndConst_09() throws Exception {
        checkOccurrences(getTestPath(), "f^nc();", true);
    }

    public void testUseFuncAndConst_10() throws Exception {
        checkOccurrences(getTestPath(), "f^nc2();", true);
    }

    public void testIssue244317_01() throws Exception {
        checkOccurrences(getTestPath(), "const test^Constant = \"test\";", true);
    }

    public void testIssue244317_02() throws Exception {
        checkOccurrences(getTestPath(), "$variable = self::test^Constant;", true);
    }

    public void testIssue244317_03() throws Exception {
        checkOccurrences(getTestPath(), "echo self::test^Constant;", true);
    }

    public void testReturnTypes01_01() throws Exception {
        checkOccurrences(getTestPath(), "interface Ifac^eA {", true);
    }

    public void testReturnTypes01_02() throws Exception {
        checkOccurrences(getTestPath(), "    static function make(): Ifa^ceA;", true);
    }

    public void testReturnTypes01_03() throws Exception {
        checkOccurrences(getTestPath(), "class ClsB implements Iface^A {", true);
    }

    public void testReturnTypes01_04() throws Exception {
        checkOccurrences(getTestPath(), "    static function make(): If^aceA {", true);
    }

    public void testReturnTypes01_05() throws Exception {
        checkOccurrences(getTestPath(), "function create(Ifa^ceA $a): ClsB {", true);
    }

    public void testReturnTypes01_06() throws Exception {
        checkOccurrences(getTestPath(), "class Cls^B implements IfaceA {", true);
    }

    public void testReturnTypes01_07() throws Exception {
        checkOccurrences(getTestPath(), "function create(IfaceA $a): C^lsB {", true);
    }

    public void testReturnTypes01_08() throws Exception {
        checkOccurrences(getTestPath(), "function create2(Cl^sB $b): array {", true);
    }

    public void testReturnTypes01_09() throws Exception {
        // no occurences for 'array'
        checkOccurrences(getTestPath(), "    function test(): a^rray {", true);
    }

    public void testReturnTypes01_10() throws Exception {
        // no occurences for 'array'
        checkOccurrences(getTestPath(), "function create2(ClsB $b): arr^ay {", true);
    }

    public void testGroupUses01_01() throws Exception {
        checkOccurrences(getTestPath(), "class C^lsA {", true);
    }

    public void testGroupUses01_02() throws Exception {
        checkOccurrences(getTestPath(), "    Cl^sA,", true);
    }

    public void testGroupUses01_03() throws Exception {
        checkOccurrences(getTestPath(), "$a = new Cls^A();", true);
    }

    public void testGroupUses01_04() throws Exception {
        checkOccurrences(getTestPath(), "class Cls^AB {", true);
    }

    public void testGroupUses01_05() throws Exception {
        checkOccurrences(getTestPath(), "    B\\ClsA^B,", true);
    }

    public void testGroupUses01_06() throws Exception {
        checkOccurrences(getTestPath(), "$ab = new C^lsAB();", true);
    }

    public void testGroupUses01_07() throws Exception {
        checkOccurrences(getTestPath(), "class C^lsABC {", true);
    }

    public void testGroupUses01_08() throws Exception {
        checkOccurrences(getTestPath(), "    B\\C\\ClsAB^C,", true);
    }

    public void testGroupUses01_09() throws Exception {
        checkOccurrences(getTestPath(), "$abc = new ClsA^BC();", true);
    }

    public void testGroupUses01_10() throws Exception {
        checkOccurrences(getTestPath(), "class ClsAB^C2 {", true);
    }

    public void testGroupUses01_11() throws Exception {
        checkOccurrences(getTestPath(), "    B\\C\\Cl^sABC2 AS MyCls", true);
    }

    public void testGroupUses01_12() throws Exception {
        checkOccurrences(getTestPath(), "    B\\C\\ClsABC2 AS MyCl^s", true);
    }

    public void testGroupUses01_13() throws Exception {
        checkOccurrences(getTestPath(), "$mycls = new MyCl^s();", true);
    }

    public void testGroupUses01_14() throws Exception {
        checkOccurrences(getTestPath(), "class Cl^sAB implements Iface {", true); // unused
    }

    public void testGroupUses01_15() throws Exception {
        checkOccurrences(getTestPath(), "class MyC^ls implements Iface {", true); // unused
    }

    public void testGroupUses02_01() throws Exception {
        checkOccurrences(getTestPath(), "const CONSTA^NT = \"CONSTANT\";", true);
    }

    public void testGroupUses02_02() throws Exception {
        checkOccurrences(getTestPath(), "    const CONST^ANT,", true);
    }

    public void testGroupUses02_03() throws Exception {
        checkOccurrences(getTestPath(), "echo CON^STANT; // CONSTANT", true);
    }

    public void testGroupUses02_04() throws Exception {
        checkOccurrences(getTestPath(), "function te^st() {", true);
    }

    public void testGroupUses02_05() throws Exception {
        checkOccurrences(getTestPath(), "    function te^st,", true);
    }

    public void testGroupUses02_06() throws Exception {
        checkOccurrences(getTestPath(), "    function tes^t AS mytest", true);
    }

    public void testGroupUses02_07() throws Exception {
        checkOccurrences(getTestPath(), "te^st(); // test", true);
    }

    public void testGroupUses02_08() throws Exception {
        checkOccurrences(getTestPath(), "    function test AS myte^st", true);
    }

    public void testGroupUses02_09() throws Exception {
        checkOccurrences(getTestPath(), "my^test(); // test", true);
    }

    public void testGroupUses02_10() throws Exception {
        checkOccurrences(getTestPath(), "class My^A {}", true);
    }

    public void testGroupUses02_11() throws Exception {
        checkOccurrences(getTestPath(), "    M^yA,", true);
    }

    public void testGroupUses02_12() throws Exception {
        checkOccurrences(getTestPath(), "new My^A();", true);
    }

    public void testAnonymousClasses01_01() throws Exception {
        checkOccurrences(getTestPath(), "interface Lo^gger {", true);
    }

    public void testAnonymousClasses01_02() throws Exception {
        checkOccurrences(getTestPath(), "    public function lo^g(string $msg);", true);
    }

    public void testAnonymousClasses01_03() throws Exception {
        checkOccurrences(getTestPath(), "$anonCls = new class implements Log^ger {", true);
    }

    public void testAnonymousClasses01_04() throws Exception {
        checkOccurrences(getTestPath(), "    public function lo^g(string $msg1) {", true);
    }

    public void testAnonymousClasses01_05() throws Exception {
        checkOccurrences(getTestPath(), "    public function log(string $ms^g1) {", true);
    }

    public void testAnonymousClasses01_06() throws Exception {
        checkOccurrences(getTestPath(), "        echo $ms^g1;", true);
    }

    public void testAnonymousClasses01_07() throws Exception {
        checkOccurrences(getTestPath(), "    public function getLogger(): Log^ger {", true);
    }

    public void testAnonymousClasses01_08() throws Exception {
        checkOccurrences(getTestPath(), "    public function setLogger(Lo^gger $logger) {", true);
    }

    public void testAnonymousClasses01_09() throws Exception {
        checkOccurrences(getTestPath(), "$app->setLogger(new class implements Lo^gger {", true);
    }

    public void testAnonymousClasses01_10() throws Exception {
        checkOccurrences(getTestPath(), "    public function l^og(string $msg2) {", true);
    }

    public void testAnonymousClasses01_11() throws Exception {
        checkOccurrences(getTestPath(), "    public function log(string $m^sg2) {", true);
    }

    public void testAnonymousClasses01_12() throws Exception {
        checkOccurrences(getTestPath(), "        echo $ms^g2;", true);
    }

    public void testAnonymousClasses01_13() throws Exception {
        checkOccurrences(getTestPath(), "(new class implements Log^ger {", true);
    }

    public void testAnonymousClasses01_14() throws Exception {
        checkOccurrences(getTestPath(), "    public function l^og(string $msg3) {", true);
    }

    public void testAnonymousClasses01_15() throws Exception {
        checkOccurrences(getTestPath(), "    public function log(string $m^sg3) {", true);
    }

    public void testAnonymousClasses01_16() throws Exception {
        checkOccurrences(getTestPath(), "        echo $ms^g3 . PHP_EOL;", true);
    }

    public void testAnonymousClasses01_17() throws Exception {
        checkOccurrences(getTestPath(), "})->lo^g('hello world');", true);
    }

    public void testAnonymousClasses02_01() throws Exception {
        checkOccurrences(getTestPath(), "class Some^Class {", true);
    }

    public void testAnonymousClasses02_02() throws Exception {
        checkOccurrences(getTestPath(), "interface Some^Interface {", true);
    }

    public void testAnonymousClasses02_03() throws Exception {
        checkOccurrences(getTestPath(), "trait Some^Trait {", true);
    }

    public void testAnonymousClasses02_04() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(new class(10) extends Some^Class implements SomeInterface {", true);
    }

    public void testAnonymousClasses02_05() throws Exception {
        checkOccurrences(getTestPath(), "var_dump(new class(10) extends SomeClass implements Some^Interface {", true);
    }

    public void testAnonymousClasses02_06() throws Exception {
        checkOccurrences(getTestPath(), "    private $n^um;", true);
    }

    public void testAnonymousClasses02_07() throws Exception {
        checkOccurrences(getTestPath(), "    public function __construct($n^um) {", true);
    }

    public void testAnonymousClasses02_08() throws Exception {
        checkOccurrences(getTestPath(), "        $this->n^um = $num;", true);
    }

    public void testAnonymousClasses02_09() throws Exception {
        checkOccurrences(getTestPath(), "        $this->num = $nu^m;", true);
    }

    public void testAnonymousClasses02_10() throws Exception {
        checkOccurrences(getTestPath(), "    use Some^Trait;", true);
    }

    public void testAnonymousClasses03_01() throws Exception {
        checkOccurrences(getTestPath(), "class Ou^ter {", true);
    }

    public void testAnonymousClasses03_02() throws Exception {
        checkOccurrences(getTestPath(), "    private $pr^op = 1;", true);
    }

    public void testAnonymousClasses03_03() throws Exception {
        checkOccurrences(getTestPath(), "    protected $pro^p2 = 2;", true);
    }

    public void testAnonymousClasses03_04() throws Exception {
        checkOccurrences(getTestPath(), "    protected function fun^c1() {", true);
    }

    public void testAnonymousClasses03_05() throws Exception {
        checkOccurrences(getTestPath(), "    public function fun^c2() {", true);
    }

    public void testAnonymousClasses03_06() throws Exception {
        checkOccurrences(getTestPath(), "        return new class($this->pr^op) extends Outer {", true);
    }

    public void testAnonymousClasses03_07() throws Exception {
        checkOccurrences(getTestPath(), "        return new class($this->prop) extends Out^er {", true);
    }

    public void testAnonymousClasses03_08() throws Exception {
        checkOccurrences(getTestPath(), "            private $pro^p3;", true);
    }

    public void testAnonymousClasses03_09() throws Exception {
        checkOccurrences(getTestPath(), "                $this->pro^p3 = $prop;", true);
    }

    public void testAnonymousClasses03_10() throws Exception {
        checkOccurrences(getTestPath(), "            public function fun^c3() {", true);
    }

    public void testAnonymousClasses03_11() throws Exception {
        checkOccurrences(getTestPath(), "                return $this->pr^op2 + $this->prop3 + $this->func1();", true);
    }

    public void testAnonymousClasses03_12() throws Exception {
        checkOccurrences(getTestPath(), "                return $this->prop2 + $this->pr^op3 + $this->func1();", true);
    }

    public void testAnonymousClasses03_13() throws Exception {
        checkOccurrences(getTestPath(), "                return $this->prop2 + $this->prop3 + $this->fun^c1();", true);
    }

    public void testAnonymousClasses03_14() throws Exception {
        checkOccurrences(getTestPath(), "echo (new Out^er)->func2()->func3() . PHP_EOL;", true);
    }

    public void testAnonymousClasses03_15() throws Exception {
        checkOccurrences(getTestPath(), "echo (new Outer)->fu^nc2()->func3() . PHP_EOL;", true);
    }

    public void testAnonymousClasses03_16() throws Exception {
        checkOccurrences(getTestPath(), "echo (new Outer)->func2()->fu^nc3() . PHP_EOL;", true);
    }

    public void testAnonymousClasses04_01() throws Exception {
        checkOccurrences(getTestPath(), "        $this->tes^tB();", true);
    }

    public void testAnonymousClasses04_02() throws Exception {
        checkOccurrences(getTestPath(), "    private function tes^tB() {", true);
    }

    public void testAnonymousClasses05_01() throws Exception {
        checkOccurrences(getTestPath(), "interface Log^ger {", true);
    }

    public void testAnonymousClasses05_02() throws Exception {
        checkOccurrences(getTestPath(), "use api\\Lo^gger as MyLogger;", true);
    }

    public void testAnonymousClasses05_03() throws Exception {
        checkOccurrences(getTestPath(), "use api\\Logger as MyLogg^er;", true);
    }

    public void testAnonymousClasses05_04() throws Exception {
        checkOccurrences(getTestPath(), "$x = new class implements MyLog^ger {", true);
    }

    public void testAnonymousClasses05_05() throws Exception {
        checkOccurrences(getTestPath(), "    public function lo^g($msg2) {", true);
    }

    public void testAnonymousClasses05_06() throws Exception {
        checkOccurrences(getTestPath(), "$x->lo^g('');", true);
    }

    public void testUniformVariableSyntax01_01() throws Exception {
        checkOccurrences(getTestPath(), "UV^S3::myStatic3()::myStatic2()::myStatic1()::MAX;", true);
    }

    public void testUniformVariableSyntax01_02() throws Exception {
        checkOccurrences(getTestPath(), "UVS3::mySta^tic3()::myStatic2()::myStatic1()::MAX;", true);
    }

    public void testUniformVariableSyntax01_03() throws Exception {
        checkOccurrences(getTestPath(), "UVS3::myStatic3()::mySt^atic2()::myStatic1()::MAX;", true);
    }

    public void testUniformVariableSyntax01_04() throws Exception {
        checkOccurrences(getTestPath(), "UVS3::myStatic3()::myStatic2()::mySta^tic1()::MAX;", true);
    }

    public void testUniformVariableSyntax01_05() throws Exception {
        checkOccurrences(getTestPath(), "UVS3::myStatic3()::myStatic2()::myStatic1()::MA^X;", true);
    }

    public void testUniformVariableSyntax01_06() throws Exception {
        checkOccurrences(getTestPath(), "UVS3::myStatic3()::myStatic2()::myStatic1()::AV^G;", true);
    }

    public void testUniformVariableSyntax01_07() throws Exception {
        checkOccurrences(getTestPath(), "UVS3::myStatic3()::myStatic2()::myStatic1()::mySt^atic2();", true);
    }

    public void testUniformVariableSyntax01_08() throws Exception {
        checkOccurrences(getTestPath(), "UVS3::myStatic3()->myStat^ic2()::myStatic1()->myStatic2();", true);
    }

    public void testUniformVariableSyntax01_09() throws Exception {
        checkOccurrences(getTestPath(), "UVS3::myStatic3()->myStatic2()::my^Static1()->myStatic2();", true);
    }

    public void testUniformVariableSyntax01_10() throws Exception {
        checkOccurrences(getTestPath(), "UVS3::myStatic3()->myStatic2()::myStatic1()->myS^tatic2();", true);
    }

    public void testUniformVariableSyntax01_11() throws Exception {
        checkOccurrences(getTestPath(), "UVS3::myStatic3()::myStatic2()::myStatic1()::$M^IN;", true);
    }

    public void testUniformVariableSyntax01_12() throws Exception {
        checkOccurrences(getTestPath(), "UVS3::myStatic3()->myStatic2()::myStatic1()->te^st;", true);
    }

    public void testUniformVariableSyntax01_13() throws Exception {
        checkOccurrences(getTestPath(), "UVS3::myStatic3()::myStatic2()::myStatic1()::$INSTANCE::mySt^atic1();", true);
    }

    public void testUniformVariableSyntax02_01() throws Exception {
        checkOccurrences(getTestPath(), "UV^S3::myStatic3()->my2()::myStatic1()->my1();", true);
    }

    public void testUniformVariableSyntax02_02() throws Exception {
        checkOccurrences(getTestPath(), "UVS3::mySt^atic3()->my2()::myStatic1()->my1();", true);
    }

    public void testUniformVariableSyntax02_03() throws Exception {
        checkOccurrences(getTestPath(), "UVS3::myStatic3()->m^y2()::myStatic1()->my1();", true);
    }

    public void testUniformVariableSyntax02_04() throws Exception {
        checkOccurrences(getTestPath(), "UVS3::myStatic3()->my2()::myS^tatic1()->my1();", true);
    }

    public void testUniformVariableSyntax02_05() throws Exception {
        checkOccurrences(getTestPath(), "UVS3::myStatic3()->my2()::myStatic1()->my^1();", true);
    }

    public void testIssue247082_01() throws Exception {
        checkOccurrences(getTestPath(), "    const PARE^NT_CONST = 'PARENT_CONST';", true);
    }

    public void testIssue247082_02() throws Exception {
        checkOccurrences(getTestPath(), "            echo parent::PAR^ENT_CONST . PHP_EOL;", true);
    }

    public void testIssue247082_03() throws Exception {
        checkOccurrences(getTestPath(), "    public static $parentF^ieldStatic = 'parentFieldStatic';", true);
    }

    public void testIssue247082_04() throws Exception {
        checkOccurrences(getTestPath(), "            echo parent::$parentFi^eldStatic . PHP_EOL;", true);
    }

    public void testIssue247082_05() throws Exception {
        checkOccurrences(getTestPath(), "    public function parentTe^stInstance() {", true);
    }

    public void testIssue247082_06() throws Exception {
        checkOccurrences(getTestPath(), "            echo parent::parentTe^stInstance() . PHP_EOL;", true);
    }

    public void testIssue247082_07() throws Exception {
        checkOccurrences(getTestPath(), "    public static function parentT^estStatic() {", true);
    }

    public void testIssue247082_08() throws Exception {
        checkOccurrences(getTestPath(), "            echo parent::parentTes^tStatic() . PHP_EOL;", true);
    }

    public void testIssue247082_09() throws Exception {
        checkOccurrences(getTestPath(), "    const MY_CO^NST = 'MY_CONST';", true);
    }

    public void testIssue247082_10() throws Exception {
        checkOccurrences(getTestPath(), "            echo self::MY_C^ONST . PHP_EOL;", true);
    }

    public void testIssue247082_11() throws Exception {
        checkOccurrences(getTestPath(), "    public static $myField^Static = 'myFieldStatic';", true);
    }

    public void testIssue247082_12() throws Exception {
        checkOccurrences(getTestPath(), "            echo self::$myFiel^dStatic . PHP_EOL;", true);
    }

    public void testIssue247082_13() throws Exception {
        checkOccurrences(getTestPath(), "    public $myFieldIn^stance = 'myFieldInstance';", true);
    }

    public void testIssue247082_14() throws Exception {
        checkOccurrences(getTestPath(), "            echo $this->myFie^ldInstance . PHP_EOL;", true);
    }

    public void testIssue247082_15() throws Exception {
        checkOccurrences(getTestPath(), "    public function myTestIns^tance() {", true);
    }

    public void testIssue247082_16() throws Exception {
        checkOccurrences(getTestPath(), "            echo $this->myTest^Instance() . PHP_EOL;", true);
    }

    public void testIssue247082_17() throws Exception {
        checkOccurrences(getTestPath(), "    public static function myTestSt^atic() {", true);
    }

    public void testIssue247082_18() throws Exception {
        checkOccurrences(getTestPath(), "            echo self::myTest^Static() . PHP_EOL;", true);
    }

    public void testIssue262438_00() throws Exception {
        checkOccurrences(getTestPath(), "interface MyIf^ace {", true);
    }

    public void testIssue262438_01() throws Exception {
        checkOccurrences(getTestPath(), "use MyVendor\\PackageTwo\\MyT^rait;", true);
    }

    public void testIssue262438_02() throws Exception {
        checkOccurrences(getTestPath(), "    use MyTr^ait;", true);
    }

    public void testIssue262438_03() throws Exception {
        checkOccurrences(getTestPath(), "trait MyTr^ait {", true);
    }

    public void testIssue268712_01() throws Exception {
        checkOccurrences(getTestPath(), "const TES^T1 = [0, 1];", true);
    }

    public void testIssue268712_02() throws Exception {
        checkOccurrences(getTestPath(), "const TE^ST2 = [[0, 1], 1];", true);
    }

    public void testIssue268712_03() throws Exception {
        checkOccurrences(getTestPath(), "const TEST^3 = \\TEST1[0];", true);
    }

    public void testIssue268712_04() throws Exception {
        checkOccurrences(getTestPath(), "const TEST4 = \\Issue268712_A\\TES^T3;", true);
    }

    public void testIssue268712_05() throws Exception {
        checkOccurrences(getTestPath(), "const TEST5 = \\Issue268712_A\\TES^T2[0][1];", true);
    }

    public void testIssue268712_06() throws Exception {
        checkOccurrences(getTestPath(), "const TES^T6 = [\"test\" => \"test\"];", true);
    }

    public void testIssue268712_07() throws Exception {
        checkOccurrences(getTestPath(), "$const1 = \\TEST^1[0];", true);
    }

    public void testIssue268712_08() throws Exception {
        checkOccurrences(getTestPath(), "$const2 = \\Issue268712_A\\TE^ST2[1];", true);
    }

    public void testIssue268712_09() throws Exception {
        checkOccurrences(getTestPath(), "echo \\Issue268712_B\\TES^T6[\"test\"] . PHP_EOL;", true);
    }

    public void testIssue268712_10() throws Exception {
        checkOccurrences(getTestPath(), "echo Sub\\S^UB[\"sub\"] . PHP_EOL;", true);
    }

    public void testIssue268712_11() throws Exception {
        checkOccurrences(getTestPath(), "echo namespace\\TEST^6[\"test\"] . PHP_EOL;", true);
    }

    public void testIssue268712_12() throws Exception {
        checkOccurrences(getTestPath(), "const S^UB = [\"sub\" => \"sub\"];", true);
    }

}
