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
 *
 * @author Radek Matous
 */
public class GotoDeclarationTest extends GotoDeclarationTestBase {

    public GotoDeclarationTest(String testName) {
        super(testName);
    }

    public void testConstAccesInFldDecl() throws Exception {
        checkDeclaration(getTestPath(), "public $fetchMode = self::FETCH_E^AGER;", "const ^FETCH_EAGER = 3;");
    }

    public void testParamVarPropInPhpDocTest_2() throws Exception {
        checkDeclaration(getTestPath(), "$tmp = $hel^lo;", "function test($^hello) {//method");
    }

    public void testClsVarPropInPhpDocTest() throws Exception {
        checkDeclaration(getTestPath(), " * @property Au^thor $author hello this is doc", "class ^Author {");
    }

    public void testClsVarPropInPhpDocTest_2() throws Exception {
        checkDeclaration(getTestPath(), "$this->auth^or;", " * @property Author $^author hello this is doc");
    }

    public void testGotoConstructTest() throws Exception {
        checkDeclaration(getTestPath(), "$a = new MyCla^ssConstr();", "public function ^__construct() {//MyClassConstr");
    }

    public void testGotoConstructTest_2() throws Exception {
        checkDeclaration(getTestPath(), "$b = new MyCla^ssConstr2();", "class ^MyClassConstr2 extends MyClassConstr  {}//MyClassConstr2");
    }

    public void testIfaceTest() throws Exception {
        checkDeclaration(getTestPath(), "myf^ace::RECOVER_ORIG;", "interface ^myface {");
    }

    public void testIfaceTest_2() throws Exception {
        checkDeclaration(getTestPath(), "myface::REC^OVER_ORIG;", "const ^RECOVER_ORIG = 2;");
    }

    public void testIfaceTest_3() throws Exception {
        checkDeclaration(getTestPath(), "myc^ls::RECOVER_ORIG;", "class ^mycls implements myface {");
    }

    public void testIfaceTest_4() throws Exception {
        checkDeclaration(getTestPath(), "mycls::REC^OVER_ORIG;", "const ^RECOVER_ORIG = 1;");
    }

    public void testIfaceTest_5() throws Exception {
        checkDeclaration(getTestPath(), "$a->mf^nc();//mycls", "function ^mfnc() {}//mycls");
    }

    public void testIfaceTest_6() throws Exception {
        checkDeclaration(getTestPath(), "$a->mfn^c();//myface", "function ^mfnc();//myface");
    }

    public void testGotoTypeClsIface() throws Exception {
        checkDeclaration(getTestPath(), "interface ifaceDeclaration2 extends ifaceDec^laration  {}", "interface ^ifaceDeclaration {}");
    }

    public void testGotoTypeClsIface_2() throws Exception {
        checkDeclaration(getTestPath(), "class clsDeclaration implements ifaceDecl^aration {}", "interface ^ifaceDeclaration {}");
    }

    public void testGotoTypeClsIface_3() throws Exception {
        checkDeclaration(getTestPath(), "class clsDeclaration2 implements ifaceDec^laration, ifaceDeclaration2 {}", "interface ^ifaceDeclaration {}");
    }

    public void testGotoTypeClsIface_4() throws Exception {
        checkDeclaration(getTestPath(), "class clsDeclaration3 extends clsDeclarat^ion {}", "class ^clsDeclaration implements ifaceDeclaration {}");
    }

    public void testGotoTypeClsIface_5() throws Exception {
        checkDeclaration(getTestPath(), "class clsDeclaration2 implements ifaceDeclaration, ifaceDecla^ration2 {}", "interface ^ifaceDeclaration2 extends ifaceDeclaration  {}");
    }

    public void testGotoTypeClsIfaceFromalParam() throws Exception {
        checkDeclaration(getTestPath(), "ifaceD^eclaration $ifaceDeclarationVar,", "interface ^ifaceDeclaration {}");
    }

    public void testGotoTypeClsIfaceFromalParam_2() throws Exception {
        checkDeclaration(getTestPath(), "ifaceD^eclaration2 $ifaceDeclaration2Var,", "interface ^ifaceDeclaration2 extends ifaceDeclaration  {}");
    }

    public void testGotoTypeClsIfaceFromalParam_4() throws Exception {
        checkDeclaration(getTestPath(), "clsD^eclaration  $clsDeclarationVar,", "class ^clsDeclaration implements ifaceDeclaration {}");
    }

    public void testGotoTypeClsIfaceFromalParam_5() throws Exception {
        checkDeclaration(getTestPath(), "clsDeclara^tion2 $clsDeclaration2Var,", "class ^clsDeclaration2 implements ifaceDeclaration, ifaceDeclaration2 {}");
    }

    public void testGotoTypeClsIfaceFromalParam_6() throws Exception {
        checkDeclaration(getTestPath(), "clsDe^claration3 $clsDeclaration3Var,", "class ^clsDeclaration3 extends clsDeclaration {}");
    }

    public void testGotoTypeClsIfaceFromalParam_7() throws Exception {
        checkDeclaration(getTestPath(), "clsDeclar^ation4 $clsDeclaration4Var", "class ^clsDeclaration4 extends clsDeclaration3 implements ifaceDeclaration4 {}");
    }

    public void testGotoTypeClsIfaceCatch() throws Exception {
        checkDeclaration(getTestPath(), "} catch (clsDecla^ration $cex) {", "class ^clsDeclaration implements ifaceDeclaration {}");
    }

    public void testGotoTypeClsIfaceInstanceof() throws Exception {
        checkDeclaration(getTestPath(), "if ($cex instanceof clsDecl^aration) {", "class ^clsDeclaration implements ifaceDeclaration {}");
    }

    public void testGotoTypeClsIfaceInstanceof_2() throws Exception {
        checkDeclaration(getTestPath(), "if ($c^ex instanceof clsDeclaration) {", "} catch (clsDeclaration $^cex) {");
    }

    public void testGotoTypeArrays() throws Exception {
        checkDeclaration(getTestPath(), "$result .= self::$static_a^rray[$idx++];", "private static $^static_array = array('', 'thousand ', 'million ', 'billion ');");
    }

    public void testGotoTypeArrays_2() throws Exception {
        checkDeclaration(getTestPath(), "$result .= self::$static^_array[$instance_array[$idx]];", "private static $^static_array = array('', 'thousand ', 'million ', 'billion ');");
    }

    public void testGotoTypeArrays_3() throws Exception {
        checkDeclaration(getTestPath(), "$result .= $this->field_a^rray[$idx++];", "private $^field_array = array('', 'thousand ', 'million ', 'billion ');");
    }

    public void testGotoTypeArrays_4() throws Exception {
        checkDeclaration(getTestPath(), "$result .= $this->field_^array[$instance_array[$idx]];", "private $^field_array = array('', 'thousand ', 'million ', 'billion ');");
    }

    public void testGotoTypeArrays_5() throws Exception {
        checkDeclaration(getTestPath(), "$instan^ce_array[$idx];", "$^instance_array = array('', 'thousand ', 'million ', 'billion ');");
    }

    public void testGotoTypeArrays_6() throws Exception {
        checkDeclaration(getTestPath(), "$result .= self::$static_array[$instance_^array[$idx]];", "$^instance_array = array('', 'thousand ', 'million ', 'billion ');");
    }

    public void testGotoTypeArrays_7() throws Exception {
        checkDeclaration(getTestPath(), "$result .= $this->field_array[$instan^ce_array[$idx]];", "$^instance_array = array('', 'thousand ', 'million ', 'billion ');");
    }

    public void testGotoTypeArrays_8() throws Exception {
        checkDeclaration(getTestPath(), "$result .= self::$static_array[$id^x++];", "$^idx = 1;");
    }

    public void testGotoTypeArrays_9() throws Exception {
        checkDeclaration(getTestPath(), "$result .= $this->field_array[$i^dx++];", "$^idx = 1;");
    }

    public void testGotoTypeArrays_10() throws Exception {
        checkDeclaration(getTestPath(), "$result .= self::$static_array[$instance_array[$id^x]];", "$^idx = 1;");
    }

    public void testGotoTypeArrays_11() throws Exception {
        checkDeclaration(getTestPath(), "$result .= $this->field_array[$instance_array[$id^x]];", "$^idx = 1;");
    }

    public void testGotoTypeArrays_12() throws Exception {
        checkDeclaration(getTestPath(), "$instance_array2[$idx^2];", "$^idx2 = 1;");
    }

    public void testGotoTypeArrays_13() throws Exception {
        checkDeclaration(getTestPath(), "$instance_a^rray2[$idx2];", "$^instance_array2 = array('', 'thousand ', 'million ', 'billion ');");
    }

    public void testGotoTypeArrays_14() throws Exception {
        checkDeclaration(getTestPath(), "$instance_array3[$id^x3];", "$^idx3 = 1;");
    }

    public void testGotoTypeArrays_15() throws Exception {
        checkDeclaration(getTestPath(), "$instance_ar^ray3[$idx3];", "$^instance_array3 = array('', 'thousand ', 'million ', 'billion ');");
    }

    public void testFuncParamAsReference() throws Exception {
        checkDeclaration(getTestPath(), "$par^am++;", "function funcWithRefParam(&$^param) {");
    }

    public void testStaticFieldAccess() throws Exception {
        checkDeclaration(getTestPath(), "Animal::$cou^nt;", "public static $^count = 0, $animal;");
    }

    public void testStaticFieldAccess_ArrayIndex() throws Exception {
        checkDeclaration(getTestPath(), "$species = self::$animalSpec^ies;", "static $^animalSpecies = array();");
    }

    public void testStaticFieldAccess_ArrayIndex2() throws Exception {
        checkDeclaration(getTestPath(), "$first = self::$animalSpec^ies[0];", "static $^animalSpecies = array();");
    }

    public void testStaticFieldAccess_2() throws Exception {
        checkDeclaration(getTestPath(), "Cat::$cou^nt;", "public static $^count = 0, $cat;");
    }

    public void testStaticFieldAccess_OutsideClass() throws Exception {
        checkDeclaration(getTestPath(), "Animal::$co^unt--;", "public static $^count = 0, $animal;");
    }

    public void testStaticFieldAccess_OutsideClass2() throws Exception {
        checkDeclaration(getTestPath(), "Cat::$co^unt--;", "public static $^count = 0, $cat;");
    }

    public void testStaticFieldAccess_OutsideClassDeclaredInSuperClass() throws Exception {
        checkDeclaration(getTestPath(), "Mammal::$co^unt--;", "public static $^count = 0, $animal;");
    }

    public void testStaticFieldAccess_Self() throws Exception {
        checkDeclaration(getTestPath(), "self::$ani^mal = $this;", "public static $count = 0, $^animal;");
    }

    public void testStaticFieldAccess_SelfDeclaredInSuperClass() throws Exception {
        checkDeclaration(getTestPath(), "echo self::$cou^nt;", "public static $^count = 0, $animal;");
    }

    public void testStaticFieldAccess_ParentDeclaredInSuperClass() throws Exception {
        checkDeclaration(getTestPath(), "echo parent::$cou^nt;", "public static $^count = 0, $animal;");
    }

    public void testStaticFieldAccess_Parent() throws Exception {
        checkDeclaration(getTestPath(), "echo parent::$cou^nt;", "public static $^count = 0, $animal;");
    }

    public void testStaticFieldAccess_ClassName() throws Exception {
        checkDeclaration(getTestPath(), "Ani^mal::$count;", "abstract class ^Animal {");
    }

    public void testStaticFieldAccess_2ClassName() throws Exception {
        checkDeclaration(getTestPath(), "Ca^t::$count;", "class ^Cat extends Mammal {");
    }

    public void testStaticFieldAccess_OutsideClass_ClassName() throws Exception {
        checkDeclaration(getTestPath(), "Ani^mal::$count--;", "abstract class ^Animal {");
    }

    public void testStaticFieldAccess_OutsideClass2_ClassName() throws Exception {
        checkDeclaration(getTestPath(), "Ca^t::$count--;", "class ^Cat extends Mammal {");
    }

    public void testStaticFieldAccess_OutsideClassDeclaredInSuperClass_ClassName() throws Exception {
        checkDeclaration(getTestPath(), "Mam^mal::$count--;", "class ^Mammal extends Animal {");
    }

    public void testClassInstantiation() throws Exception {
        checkDeclaration(getTestPath(), "$mammal = new Mamm^al;", "function ^__construct() {//Mammal");
    }

    public void testClassInstantiation_2() throws Exception {
        checkDeclaration(getTestPath(), "class Mammal extends Animal^ {", "abstract class ^Animal");
    }

    public void testSuperClasses() throws Exception {
        checkDeclaration(getTestPath(), "class Cat extends Mamm^al {", "class ^Mammal extends Animal {");
    }

    public void testMethodInvocation_Parent() throws Exception {
        checkDeclaration(getTestPath(), "echo parent::getC^ount(\"calling animal's getCount 1\");", "public function ^getCount($animalLogging) {");
    }

    public void testMethodInvocation_Parent2() throws Exception {
        checkDeclaration(getTestPath(), "echo parent::getC^ount(\"calling animal's getCount 2\");", "public function ^getCount($animalLogging) {");
    }

    public void testMethodInvocation() throws Exception {
        checkDeclaration(getTestPath(), "$mammal->get^Count(\"calling animal's getCount 3\");", "public function ^getCount($animalLogging) {");
    }

    public void testMethodInvocation_Constructor() throws Exception {
        checkDeclaration(getTestPath(), "parent::__constr^uct", "function ^__construct() {");
    }

    public void testMethodInvocation_2() throws Exception {
        checkDeclaration(getTestPath(), "$cat->getCo^unt(\"calling cat's getCount 1\");", "public function ^getCount($catLogging) {");
    }

    public void testMethodInvocation_ParentThis() throws Exception {
        checkDeclaration(getTestPath(), "echo $this->getCou^nt(\"calling cat's getCount\");", "public function ^getCount($catLogging) {");
    }

    public void testMethodInvocation_Self() throws Exception {
        checkDeclaration(getTestPath(), "self::get^Count(\"calling animal's getCount 0\");", "public function ^getCount($animalLogging) {");
    }

    public void testConstantAccess_2() throws Exception {
        checkDeclaration(getTestPath(), "$isMe = (self::KI^ND == $mammalKind);", "const ^KIND=1;");
    }

    public void testConstantAccess_2_1() throws Exception {
        checkDeclaration(getTestPath(), "$isParentAnimal = (parent::KI^ND == $animalKind);", "const ^KIND=1;");
    }

    public void testConstantAccess_3() throws Exception {
        checkDeclaration(getTestPath(), "$mammalKind = Mammal::KIN^D;", "const ^KIND=1;");
    }

    public void testConstantAccess_3_1() throws Exception {
        checkDeclaration(getTestPath(), "$mammalKind = Mam^mal::KIND;", "class ^Mammal extends Animal {");
    }

    public void testConstantAccess_4() throws Exception {
        checkDeclaration(getTestPath(), "$animalKind = Animal::KI^ND;", "const ^KIND=1;");
    }

    public void testConstantAccess_4_1() throws Exception {
        checkDeclaration(getTestPath(), "$animalKind = Ani^mal::KIND;", "abstract class ^Animal {");
    }

    public void testConstantAccess_5() throws Exception {
        checkDeclaration(getTestPath(), "$catKind = self::KIN^D;", "const ^KIND=3;");
    }

    public void testConstantAccess_6() throws Exception {
        checkDeclaration(getTestPath(), "echo Animal::KIN^D;", "const ^KIND=1;");
    }

    public void testConstantAccess_6_1() throws Exception {
        checkDeclaration(getTestPath(), "echo Ani^mal::KIND;", "abstract class ^Animal {");
    }

    public void testConstantAccess_7() throws Exception {
        checkDeclaration(getTestPath(), "echo Mammal::KI^ND;", "const ^KIND=1;");
    }

    public void testConstantAccess_7_1() throws Exception {
        checkDeclaration(getTestPath(), "echo Mamm^al::KIND;", "class ^Mammal extends Animal {");
    }

    public void testConstantAccess_8() throws Exception {
        checkDeclaration(getTestPath(), "echo Cat::KI^ND;", "const ^KIND=3;");
    }

    public void testConstantAccess_8_1() throws Exception {
        checkDeclaration(getTestPath(), "echo Ca^t::KIND;", "class ^Cat extends Mammal {");
    }

    public void testConstantAccess_9() throws Exception {
        checkDeclaration(getTestPath(), "print Animal::KI^ND;", "const ^KIND=1;");
    }

    public void testConstantAccess_9_1() throws Exception {
        checkDeclaration(getTestPath(), "print Ani^mal::KIND;", "abstract class ^Animal {");
    }

    public void testConstantAccess_10() throws Exception {
        checkDeclaration(getTestPath(), "print Mammal::KIN^D;", "const ^KIND=1;");
    }

    public void testConstantAccess_10_1() throws Exception {
        checkDeclaration(getTestPath(), "print Mam^mal::KIND;", "class ^Mammal extends Animal {");
    }

    public void testConstantAccess_11() throws Exception {
        checkDeclaration(getTestPath(), "print Cat::KI^ND;", "const ^KIND=3;");
    }

    public void testConstantAccess_11_1() throws Exception {
        checkDeclaration(getTestPath(), "print Ca^t::KIND;", "class ^Cat extends Mammal {");
    }

    // #250579
    public void testConstantArrayAccess_01() throws Exception {
        checkDeclaration(getTestPath(), "const GLOBAL_CONSTANT2 = GLOBAL_CON^STANT1[0];", "const ^GLOBAL_CONSTANT1 = [0, 1];");
    }

    public void testConstantArrayAccess_02() throws Exception {
        checkDeclaration(getTestPath(), "const GLOBAL_CONSTANT3 = GLOBAL_CONST^ANT1[GLOBAL_CONSTANT1[0] + GLOBAL_CONSTANT1[0]];", "const ^GLOBAL_CONSTANT1 = [0, 1];");
    }

    public void testConstantArrayAccess_03() throws Exception {
        checkDeclaration(getTestPath(), "const GLOBAL_CONSTANT3 = GLOBAL_CONSTANT1[GLOBAL_CO^NSTANT1[0] + GLOBAL_CONSTANT1[0]];", "const ^GLOBAL_CONSTANT1 = [0, 1];");
    }

    public void testConstantArrayAccess_04() throws Exception {
        checkDeclaration(getTestPath(), "const GLOBAL_CONSTANT3 = GLOBAL_CONSTANT1[GLOBAL_CONSTANT1[0] + GLOBAL_CONST^ANT1[0]];", "const ^GLOBAL_CONSTANT1 = [0, 1];");
    }

    public void testConstantArrayAccess_05() throws Exception {
        checkDeclaration(getTestPath(), "const GLOBAL_CONSTANT5 = GLOBAL_CONST^ANT4[\"b\"][GLOBAL_CONSTANT1[1]];", "const ^GLOBAL_CONSTANT4 = [\"a\" => [0, 1], \"b\" => [\"c\", \"d\"]];");
    }

    public void testConstantArrayAccess_06() throws Exception {
        checkDeclaration(getTestPath(), "const GLOBAL_CONSTANT5 = GLOBAL_CONSTANT4[\"b\"][GLOBAL_CONS^TANT1[1]];", "const ^GLOBAL_CONSTANT1 = [0, 1];");
    }

    public void testConstantArrayAccess_07() throws Exception {
        checkDeclaration(getTestPath(), "GLOBAL_CO^NSTANT1[$index];", "const ^GLOBAL_CONSTANT1 = [0, 1];");
    }

    public void testConstantArrayAccess_08() throws Exception {
        checkDeclaration(getTestPath(), "[1][GLOBAL_CONSTA^NT1[0]];", "const ^GLOBAL_CONSTANT1 = [0, 1];");
    }

    public void testConstantArrayAccess_09() throws Exception {
        checkDeclaration(getTestPath(), "echo GLOBAL_CONSTANT4[\"a\"][GLOBAL_CONST^ANT1[$index]];", "const ^GLOBAL_CONSTANT1 = [0, 1];");
    }

    public void testConstantArrayAccess_10() throws Exception {
        checkDeclaration(getTestPath(), "const CLASS_CONSTANT2 = self::CLA^SS_CONSTANT1[0];", "const ^CLASS_CONSTANT1 = [\"a\", \"b\"];");
    }

    public void testConstantArrayAccess_11() throws Exception {
        checkDeclaration(getTestPath(), "const CLASS_CONSTANT3 = GLOBAL_CONS^TANT1[0] + GLOBAL_CONSTANT1[1];", "const ^GLOBAL_CONSTANT1 = [0, 1];");
    }

    public void testConstantArrayAccess_12() throws Exception {
        checkDeclaration(getTestPath(), "self::CLASS_CONST^ANT1[GLOBAL_CONSTANT1[1]];", "const ^CLASS_CONSTANT1 = [\"a\", \"b\"];");
    }

    public void testConstantArrayAccess_13() throws Exception {
        checkDeclaration(getTestPath(), "self::CLASS_CONSTANT1[GLOBAL_CO^NSTANT1[1]];", "const ^GLOBAL_CONSTANT1 = [0, 1];");
    }

    public void testConstantArrayAccess_14() throws Exception {
        checkDeclaration(getTestPath(), "self::CLASS_CON^STANT1[ConstantClass::CLASS_CONSTANT4[self::CLASS_CONSTANT4[0]]];", "const ^CLASS_CONSTANT1 = [\"a\", \"b\"];");
    }

    public void testConstantArrayAccess_15() throws Exception {
        checkDeclaration(getTestPath(), "self::CLASS_CONSTANT1[C^onstantClass::CLASS_CONSTANT4[self::CLASS_CONSTANT4[0]]];", "class ^ConstantClass implements ConstantInterface {");
    }

    public void testConstantArrayAccess_16() throws Exception {
        checkDeclaration(getTestPath(), "self::CLASS_CONSTANT1[ConstantClass::CLASS_CO^NSTANT4[self::CLASS_CONSTANT4[0]]];", "const ^CLASS_CONSTANT4 = [0, 1];");
    }

    public void testConstantArrayAccess_17() throws Exception {
        checkDeclaration(getTestPath(), "self::CLASS_CONSTANT1[ConstantClass::CLASS_CONSTANT4[self::CLASS^_CONSTANT4[0]]];", "const ^CLASS_CONSTANT4 = [0, 1];");
    }

    public void testConstantArrayAccess_18() throws Exception {
        checkDeclaration(getTestPath(), "\"String\"[ConstantClass::CLASS_CONST^ANT4[0]];", "const ^CLASS_CONSTANT4 = [0, 1];");
    }

    public void testConstantArrayAccess_19() throws Exception {
        checkDeclaration(getTestPath(), "const INTERFACE_CONSTANT2 = self::INT^ERFACE_CONSTANT1[0];", "const ^INTERFACE_CONSTANT1 = [\"a\", \"b\"];");
    }

    public void testConstantArrayAccess_20() throws Exception {
        checkDeclaration(getTestPath(), "const INTERFACE_CONSTANT3 = ConstantInterface::INTERFACE^_CONSTANT1[0] . GLOBAL_CONSTANT1[1];", "const ^INTERFACE_CONSTANT1 = [\"a\", \"b\"];");
    }

    public void testConstantArrayAccess_21() throws Exception {
        checkDeclaration(getTestPath(), "const INTERFACE_CONSTANT3 = ConstantInterface::INTERFACE_CONSTANT1[0] . GLOBAL_CON^STANT1[1];", "const ^GLOBAL_CONSTANT1 = [0, 1];");
    }

    public void testConstantArrayAccess_22() throws Exception {
        checkDeclaration(getTestPath(), "const INTERFACE_CONSTANT3 = Constant^Interface::INTERFACE_CONSTANT1[0] . GLOBAL_CONSTANT1[1];", "interface ^ConstantInterface {");
    }

    public void testConstantArrayAccess_23() throws Exception {
        checkDeclaration(getTestPath(), "ConstantInterface::INTERFACE_CONS^TANT1[GLOBAL_CONSTANT1[1]];", "const ^INTERFACE_CONSTANT1 = [\"a\", \"b\"];");
    }

    public void testConstantArrayAccess_24() throws Exception {
        checkDeclaration(getTestPath(), "ConstantInterface::INTERFACE_CONSTANT1[GLOBAL_CON^STANT1[1]];", "const ^GLOBAL_CONSTANT1 = [0, 1];");
    }

    // constant array access with namespace
    public void testIssue268712_01() throws Exception {
        checkDeclaration(getTestPath(), "const TEST3 = \\TES^T1[0];", "    const ^TEST1 = [0, 1];");
    }

    public void testIssue268712_02() throws Exception {
        checkDeclaration(getTestPath(), "\\Issue268712_A\\TEST^2[0][1];", "    const ^TEST2 = [[0, 1], 1];");
    }

    public void testIssue268712_03() throws Exception {
        checkDeclaration(getTestPath(), "$const1 = \\TE^ST1[$test];", "    const ^TEST1 = [0, 1];");
    }

    public void testIssue268712_04() throws Exception {
        checkDeclaration(getTestPath(), "$const2 = \\Issue268712_A\\TEST2^[1];", "    const ^TEST2 = [[0, 1], 1];");
    }

    public void testIssue268712_05() throws Exception {
        checkDeclaration(getTestPath(), "echo \\Issue268712_B\\TES^T6[\"test\"] . PHP_EOL;", "    const ^TEST6 = [\"test\" => \"test\"];");
    }

    public void testIssue268712_06() throws Exception {
        checkDeclaration(getTestPath(), "echo Sub\\S^UB[\"sub\"] . PHP_EOL;", "    const ^SUB = [\"sub\" => \"sub\"];");
    }

    public void testIssue268712_07() throws Exception {
        checkDeclaration(getTestPath(), "echo namespace\\TEST^6[\"test\"] . PHP_EOL;", "    const ^TEST6 = [\"test\" => \"test\"];");
    }

    public void testStaticMethodInvocation_First() throws Exception {
        checkDeclaration(getTestPath(), "echo Mammal::$co^unt;", "public static $^count = 0, $animal;");
    }

    public void testStaticMethodInvocation() throws Exception {
        checkDeclaration(getTestPath(), "echo Animal::kindIn^fo();", "public static function ^kindInfo() {return \"animal is ...\";}");
    }

    public void testStaticMethodInvocation_1_2() throws Exception {
        checkDeclaration(getTestPath(), "echo Mammal::kindI^nfo();", "public static function ^kindInfo() {return \"animal is ...\";}");
    }

    public void testStaticMethodInvocation_1_1() throws Exception {
        checkDeclaration(getTestPath(), "echo Mam^mal::kindInfo();", "class ^Mammal extends Animal {");
    }

    public void testStaticMethodInvocation_2() throws Exception {
        checkDeclaration(getTestPath(), "echo Cat::kindIn^fo();", "public static function ^kindInfo() {return \"cat is ...\";}");
    }

    public void testStaticMethodInvocation_2_1() throws Exception {
        checkDeclaration(getTestPath(), "echo C^at::kindInfo();", "class ^Cat extends Mammal {");
    }

    public void testStaticMethodInvocation_3() throws Exception {
        checkDeclaration(getTestPath(), "echo self::kindIn^fo();", "public static function ^kindInfo() {return \"cat is ...\";}");
    }

    public void testStaticMethodInvocation_4() throws Exception {
        checkDeclaration(getTestPath(), "echo parent::kindIn^fo();", "public static function ^kindInfo() {return \"animal is ...\";}");
    }

    public void testStaticMethodInvocation_5_1() throws Exception {
        checkDeclaration(getTestPath(), "print Anim^al::kindInfo();", "abstract class ^Animal {");
    }

    public void testStaticMethodInvocation_6() throws Exception {
        checkDeclaration(getTestPath(), "print Mammal::kindIn^fo();", "public static function ^kindInfo() {return \"animal is ...\";}");
    }

    public void testStaticMethodInvocation_6_1() throws Exception {
        checkDeclaration(getTestPath(), "print Mam^mal::kindInfo();", "class ^Mammal extends Animal {");
    }

    public void testStaticMethodInvocation_7() throws Exception {
        checkDeclaration(getTestPath(), "print Cat::kindIn^fo();", "public static function ^kindInfo() {return \"cat is ...\";}");
    }

    public void testStaticMethodInvocation_7_1() throws Exception {
        checkDeclaration(getTestPath(), "print Ca^t::kindInfo();", "class ^Cat extends Mammal {");
    }

    public void testStaticMethodInvocation_Issue_200700_01() throws Exception {
        checkDeclaration(getTestPath(), "echo static::kin^dInfo();", "public static function ^kindInfo() {return \"cat is ...\";}");
    }

    public void testStaticMethodInvocation_Issue_200700_02() throws Exception {
        checkDeclaration(getTestPath(), "echo static::getCla^ssDesc(); // navigate to parent", "public static function ^getClassDesc() {return \"Mammal class\";}");
    }

    public void testStaticMethodInvocation_Issue_200700_03() throws Exception {
        checkDeclaration(getTestPath(), "echo static::get^Animal(); // navigate to parent", "public static function ^getAnimal() {");
    }

    public void testVardoc166660() throws Exception {
        checkDeclaration(getTestPath(), "@var $testClass Test^Class", "class ^TestClass {}");
    }
    public void testVardoc166660_1() throws Exception {
        checkDeclaration(getTestPath(), "@var $test^Class TestClass", "$^testClass = new TestClass();");
    }

    public void testStaticConstant197239_01() throws Exception {
        checkDeclaration(getTestPath(), "echo static::LET^TER22;", "const ^LETTER22 = 'a';");
    }

    public void testStaticConstant197239_02() throws Exception {
        checkDeclaration(getTestPath(), "echo self::LETT^ER22;", "const ^LETTER22 = 'a';");
    }

    public void testStaticConstant197239_03() throws Exception {
        checkDeclaration(getTestPath(), "echo AA::LETT^ER22;", "const ^LETTER22 = 'a';");
    }

    public void testMixedTypes200156_01() throws Exception {
        checkDeclaration(getTestPath(), "* @property F^oo|Bar $property", "class ^Foo {");
    }

    public void testMixedTypes200156_02() throws Exception {
        checkDeclaration(getTestPath(), "* @property Foo|B^ar $property", "class ^Bar {");
    }

    public void testMixedTypes200156_03() throws Exception {
        checkDeclaration(getTestPath(), "     * @var Fo^o|Bar", "class ^Foo {");
    }

    public void testMixedTypes200156_04() throws Exception {
        checkDeclaration(getTestPath(), "     * @var Foo|Ba^r", "class ^Bar {");
    }

    // uncomment when issue #200161 will be fixed
//    public void testMixedTypes200156_05() throws Exception {
//        checkDeclaration(getTestPath(), "* @method Fo^o|Bar m1() m1(Foo|Bar $param) a magic method declaration", "class ^Foo {");
//    }
//
//    public void testMixedTypes200156_06() throws Exception {
//        checkDeclaration(getTestPath(), "* @method Foo|B^ar m1() m1(Foo|Bar $param) a magic method declaration", "class ^Bar {");
//    }

    public void testMixedTypes200156_07() throws Exception {
        checkDeclaration(getTestPath(), "* @method Foo|Bar m1() m1(F^oo|Bar $param) a magic method declaration", "class ^Foo {");
    }

    public void testMixedTypes200156_08() throws Exception {
        checkDeclaration(getTestPath(), "* @method Foo|Bar m1() m1(Foo|B^ar $param) a magic method declaration", "class ^Bar {");
    }

    public void testClassInUseStatement209187() throws Exception {
        checkDeclaration(getTestPath(), "use \\Foo\\Bar\\Class^Name;", "class ^ClassName {");
    }

    public void testQualifiedClassInPhpDoc_01() throws Exception {
        checkDeclaration(getTestPath(), "* @param B\\B^ag $param", "class ^Bag {}");
    }

    public void testQualifiedClassInPhpDoc_02() throws Exception {
        checkDeclaration(getTestPath(), "function functionName1(B\\B^ag $param) {", "class ^Bag {}");
    }

    public void testQualifiedClassInPhpDoc_03() throws Exception {
        checkDeclaration(getTestPath(), "* @return B\\B^ag", "class ^Bag {}");
    }

    public void testQualifiedClassInPhpDoc_04() throws Exception {
        checkDeclaration(getTestPath(), "return new B\\B^ag();", "class ^Bag {}");
    }

    public void testIssue200596_01() throws Exception {
        checkDeclaration(getTestPath(), "(new O^mg\\AliasedClassName())->bar();", "use \\Foo\\Bar as ^Omg;");
    }

    public void testIssue200596_02() throws Exception {
        checkDeclaration(getTestPath(), "new O^mg\\AliasedClassName();", "use \\Foo\\Bar as ^Omg;");
    }

    public void testIssue200596_03() throws Exception {
        checkDeclaration(getTestPath(), "O^mg\\AliasedClassName::foo();", "use \\Foo\\Bar as ^Omg;");
    }

    public void testIssue200596_04() throws Exception {
        checkDeclaration(getTestPath(), "O^mg\\AliasedClassName::FOO;", "use \\Foo\\Bar as ^Omg;");
    }

    public void testIssue200596_05() throws Exception {
        checkDeclaration(getTestPath(), "O^mg\\AliasedClassName::$foo;", "use \\Foo\\Bar as ^Omg;");
    }

    public void testIssue200596_06() throws Exception {
        checkDeclaration(getTestPath(), "if ($x instanceof O^mg\\AliasedClassName) {}", "use \\Foo\\Bar as ^Omg;");
    }

    public void testIssue200596_07() throws Exception {
        checkDeclaration(getTestPath(), "(new C^ls())->bar();", "use \\Foo\\Bar\\AliasedClassName as ^Cls;");
    }

    public void testIssue200596_08() throws Exception {
        checkDeclaration(getTestPath(), "new C^ls();", "use \\Foo\\Bar\\AliasedClassName as ^Cls;");
    }

    public void testIssue200596_09() throws Exception {
        checkDeclaration(getTestPath(), "C^ls::foo();", "use \\Foo\\Bar\\AliasedClassName as ^Cls;");
    }

    public void testIssue200596_10() throws Exception {
        checkDeclaration(getTestPath(), "C^ls::FOO;", "use \\Foo\\Bar\\AliasedClassName as ^Cls;");
    }

    public void testIssue200596_11() throws Exception {
        checkDeclaration(getTestPath(), "C^ls::$foo;", "use \\Foo\\Bar\\AliasedClassName as ^Cls;");
    }

    public void testIssue200596_12() throws Exception {
        checkDeclaration(getTestPath(), "if ($x instanceof C^ls) {}", "use \\Foo\\Bar\\AliasedClassName as ^Cls;");
    }

    public void testFieldAccessInInstanceOf() throws Exception {
        checkDeclaration(getTestPath(), "if ($a instanceof $this->bb^bbb) {}", "public $^bbbbb;");
    }

    public void testIssue209309_01() throws Exception {
        checkDeclaration(getTestPath(), "function bar(O^mg\\AliasedClassName $p, Cls $a, \\Foo\\Bar\\AliasedClassName $name) {}", "use \\Foo\\Bar as ^Omg;");
    }

    public void testIssue209309_02() throws Exception {
        checkDeclaration(getTestPath(), "function bar(Omg\\Aliased^ClassName $p, Cls $a, \\Foo\\Bar\\AliasedClassName $name) {}", "class ^AliasedClassName {}");
    }

    public void testIssue209309_03() throws Exception {
        checkDeclaration(getTestPath(), "function bar(Omg\\AliasedClassName $p, C^ls $a, \\Foo\\Bar\\AliasedClassName $name) {}", "use \\Foo\\Bar\\AliasedClassName as ^Cls;");
    }

    public void testIssue209309_04() throws Exception {
        checkDeclaration(getTestPath(), "function bar(Omg\\AliasedClassName $p, Cls $a, \\Foo\\Bar\\Aliased^ClassName $name) {}", "class ^AliasedClassName {}");
    }

    public void testIssue209308_01() throws Exception {
        checkDeclaration(getTestPath(), "/** @var Om^g\\AliasedClassName */", "use \\Foo\\Bar as ^Omg;");
    }

    public void testIssue209308_02() throws Exception {
        checkDeclaration(getTestPath(), "* @return Om^g\\AliasedClassName", "use \\Foo\\Bar as ^Omg;");
    }

    public void testIssue209308_04() throws Exception {
        checkDeclaration(getTestPath(), "* @param Om^g\\AliasedClassName $p", "use \\Foo\\Bar as ^Omg;");
    }

    public void testIssue209308_05() throws Exception {
        checkDeclaration(getTestPath(), "/** @var Cl^s */", "use \\Foo\\Bar\\AliasedClassName as ^Cls;");
    }

    public void testIssue209308_07() throws Exception {
        checkDeclaration(getTestPath(), "* @param Cl^s $a", "use \\Foo\\Bar\\AliasedClassName as ^Cls;");
    }

    public void testIssue209308_08() throws Exception {
        checkDeclaration(getTestPath(), "* @return Cl^s", "use \\Foo\\Bar\\AliasedClassName as ^Cls;");
    }

    public void testIssue209308_09() throws Exception {
        checkDeclaration(getTestPath(), "/** @var Omg\\Aliased^ClassName */", "class ^AliasedClassName {}");
    }

    public void testIssue209308_10() throws Exception {
        checkDeclaration(getTestPath(), "* @return Omg\\Aliased^ClassName", "class ^AliasedClassName {}");
    }

    public void testIssue209308_12() throws Exception {
        checkDeclaration(getTestPath(), "* @param Omg\\Aliased^ClassName $p", "class ^AliasedClassName {}");
    }

    public void testIssue209308_13() throws Exception {
        checkDeclaration(getTestPath(), "* @param \\Foo\\Bar\\Aliased^ClassName $name Description", "class ^AliasedClassName {}");
    }

    public void testStaticAccessWithNs_01() throws Exception {
        checkDeclaration(getTestPath(), "ClassName::B^AR;", "const ^BAR = 2;");
    }

    public void testStaticAccessWithNs_02() throws Exception {
        checkDeclaration(getTestPath(), "\\Foo\\Bar\\ClassName::B^AR;", "const ^BAR = 2;");
    }

    public void testStaticAccessWithNs_03() throws Exception {
        checkDeclaration(getTestPath(), "ClassName::$b^ar;", "public static $^bar;");
    }

    public void testStaticAccessWithNs_04() throws Exception {
        checkDeclaration(getTestPath(), "\\Foo\\Bar\\ClassName::$b^ar;", "public static $^bar;");
    }

    public void testStaticAccessWithNs_05() throws Exception {
        checkDeclaration(getTestPath(), "ClassName::b^ar();", "static function ^bar() {}");
    }

    public void testStaticAccessWithNs_06() throws Exception {
        checkDeclaration(getTestPath(), "\\Foo\\Bar\\ClassName::b^ar();", "static function ^bar() {}");
    }

    public void testStaticAccessWithNs_07() throws Exception {
        checkDeclaration(getTestPath(), "Omg\\AliasedClassName::F^OO;", "const ^FOO = 1;");
    }

    public void testStaticAccessWithNs_08() throws Exception {
        checkDeclaration(getTestPath(), "Cls::F^OO;", "const ^FOO = 1;");
    }

    public void testStaticAccessWithNs_09() throws Exception {
        checkDeclaration(getTestPath(), "Omg\\AliasedClassName::$f^oo;", "public static $^foo;");
    }

    public void testStaticAccessWithNs_10() throws Exception {
        checkDeclaration(getTestPath(), "Cls::$f^oo;", "public static $^foo;");
    }

    public void testStaticAccessWithNs_11() throws Exception {
        checkDeclaration(getTestPath(), "Omg\\AliasedClassName::f^oo();", "static function ^foo() {}");
    }

    public void testStaticAccessWithNs_12() throws Exception {
        checkDeclaration(getTestPath(), "Cls::f^oo();", "static function ^foo() {}");
    }

    public void testIssue207971_01() throws Exception {
        checkDeclaration(getTestPath(), "$sql = \" {$this->fie^ld1} {$this->object2->xxx} {$this->field3['array1']} \";", "private $^field1;");
    }

    public void testIssue207971_02() throws Exception {
        checkDeclaration(getTestPath(), "$sql = \" {$this->field1} {$this->obj^ect2->xxx} {$this->field3['array1']} \";", "private $^object2;");
    }

    public void testIssue207971_03() throws Exception {
        checkDeclaration(getTestPath(), "$sql = \" {$this->field1} {$this->object2->xxx} {$this->fie^ld3['array1']} \";", "private $^field3;");
    }

    public void testImplementsInterface() throws Exception {
        checkDeclaration(getTestPath(), "class Man implements Pe^rson {", "interface ^Person {");
    }

    public void testExtendsClass() throws Exception {
        checkDeclaration(getTestPath(), "class User extends M^an {", "class ^Man implements Person {");
    }

    public void testIssue209888_01() throws Exception {
        checkDeclaration(getTestPath(), "$this->type = Types::B^AR;", "const ^BAR = 2;");
    }

    public void testIssue209888_02() throws Exception {
        checkDeclaration(getTestPath(), "private static $foo = array(self::CS^S_CLASS => \"\");", "const ^CSS_CLASS = 'datepicker';");
    }

    public void testIssue209888_03() throws Exception {
        checkDeclaration(getTestPath(), "$this->controlPrototype->class(self::CS^S_CLASS);", "const ^CSS_CLASS = 'datepicker';");
    }

    public void testIssue147517_01() throws Exception {
        checkDeclaration(getTestPath(), "require_once 'driv^er.php';", "^<?php//driver");
    }

    public void testIssue147517_02() throws Exception {
        checkDeclaration(getTestPath(), "require ('man^ager.php');", "^<?php//manager");
    }

    public void testIssue147517_03() throws Exception {
        checkDeclaration(getTestPath(), "include 'facto^ry.php';", "^<?php//factory");
    }

    public void testIssue147517_04() throws Exception {
        checkDeclaration(getTestPath(), "include_once ( 'con^tainer.php');", "^<?php//container");
    }

    public void testIssue203073_01() throws Exception {
        checkDeclaration(getTestPath(), "class Yours extends Second^Parent {", "use Full\\Name\\Space\\FirstParent as ^SecondParent;");
    }

    public void testIssue203073_02() throws Exception {
        checkDeclaration(getTestPath(), "class Yours1 extends First^Parent {", "class ^FirstParent {");
    }

    public void testIssue203814_01() throws Exception {
        checkDeclaration(getTestPath(), "self::$first->fMe^thod();", "public function ^fMethod()");
    }

    public void testIssue203814_02() throws Exception {
        checkDeclaration(getTestPath(), "static::$first->fMe^thod();", "public function ^fMethod()");
    }

    public void testIssue203814_03() throws Exception {
        checkDeclaration(getTestPath(), "Second::$first->fMe^thod();", "public function ^fMethod()");
    }

    public void testIssue207346_01() throws Exception {
        checkDeclaration(getTestPath(), "$this->invalid^LinkMode = 10;", "public $^invalidLinkMode;");
    }

    public void testIssue207346_02() throws Exception {
        checkDeclaration(getTestPath(), "$this->invalid^LinkMode;", "public $^invalidLinkMode;");
    }

    public void testIssue208851() throws Exception {
        checkDeclaration(getTestPath(), "parent::some^Func();", "function ^someFunc() {}");
    }

    public void testIssue207615_01() throws Exception {
        checkDeclaration(getTestPath(), "self::$_v^ar;", "protected static $^_var = true;");
    }

    public void testIssue207615_02() throws Exception {
        checkDeclaration(getTestPath(), "return static::$_v^ar;", "protected static $^_var = true;");
    }

    public void testConstants_01() throws Exception {
        checkDeclaration(getTestPath(), "parent::C^ON;", "const ^CON = 1;");
    }

    public void testConstants_02() throws Exception {
        checkDeclaration(getTestPath(), "self::C^ON;", "const ^CON = 1;");
    }

    public void testConstants_03() throws Exception {
        checkDeclaration(getTestPath(), "static::C^ON;", "const ^CON = 1;");
    }

    public void testStaticAccessWithNsAlias_01() throws Exception {
        checkDeclaration(getTestPath(), "parent::O^MG;", "const ^OMG = 1;");
    }

    public void testStaticAccessWithNsAlias_02() throws Exception {
        checkDeclaration(getTestPath(), "self::O^MG;", "const ^OMG = 1;");
    }

    public void testStaticAccessWithNsAlias_03() throws Exception {
        checkDeclaration(getTestPath(), "static::O^MG;", "const ^OMG = 1;");
    }

    public void testStaticAccessWithNsAlias_04() throws Exception {
        checkDeclaration(getTestPath(), "parent::$static^Field;", "public static $^staticField = 2;");
    }

    public void testStaticAccessWithNsAlias_05() throws Exception {
        checkDeclaration(getTestPath(), "self::$static^Field;", "public static $^staticField = 2;");
    }

    public void testStaticAccessWithNsAlias_06() throws Exception {
        checkDeclaration(getTestPath(), "static::$static^Field;", "public static $^staticField = 2;");
    }

    public void testStaticAccessWithNsAlias_07() throws Exception {
        checkDeclaration(getTestPath(), "parent::some^Func();", "static function ^someFunc() {");
    }

    public void testIssue211230_01() throws Exception {
        checkDeclaration(getTestPath(), " * @method F^oo|Bar method() This is my cool magic method description.", "class ^Foo {");
    }

    public void testIssue211230_02() throws Exception {
        checkDeclaration(getTestPath(), " * @method Foo|B^ar method() This is my cool magic method description.", "class ^Bar {");
    }

    public void testIssue186553_01() throws Exception {
        checkDeclaration(getTestPath(), "$object1->do^Something();", "public function ^doSomething() {} //obj1");
    }

    public void testIssue186553_02() throws Exception {
        checkDeclaration(getTestPath(), "$this->do^Something();", "public function ^doSomething() //so");
    }

    public void testIssue213133_01() throws Exception {
        checkDeclaration(getTestPath(), "echo $test->{Te^st::$CHECK};", "class ^Test {");
    }

    public void testIssue213133_02() throws Exception {
        checkDeclaration(getTestPath(), "echo $test->{Test::$CH^ECK};", "    public static $^CHECK = \"check\";");
    }

    public void testIssue213584_01() throws Exception {
        checkDeclaration(getTestPath(), "use A^A, BB, CC, DD {", "trait ^AA {");
    }

    public void testIssue213584_02() throws Exception {
        checkDeclaration(getTestPath(), "use AA, B^B, CC, DD {", "trait ^BB {");
    }

    public void testIssue213584_03() throws Exception {
        checkDeclaration(getTestPath(), "use AA, BB, C^C, DD {", "trait ^CC {");
    }

    public void testIssue213584_04() throws Exception {
        checkDeclaration(getTestPath(), "use AA, BB, CC, D^D {", "trait ^DD {");
    }

    public void testIssue213584_05() throws Exception {
        checkDeclaration(getTestPath(), "C^C::bar insteadof AA, BB;", "trait ^CC {");
    }

    public void testIssue213584_06() throws Exception {
        checkDeclaration(getTestPath(), "CC::bar insteadof A^A, BB;", "trait ^AA {");
    }

    public void testIssue213584_07() throws Exception {
        checkDeclaration(getTestPath(), "CC::bar insteadof AA, B^B;", "trait ^BB {");
    }

    public void testIssue213584_08() throws Exception {
        checkDeclaration(getTestPath(), "D^D::bar as foo;", "trait ^DD {");
    }

    public void testIssue218487() throws Exception {
        checkDeclaration(getTestPath(), "class AbstractController implements Dispatch^able2 {", "use Zend\\Stdlib2\\DispatchableInterface2 as ^Dispatchable2;");
    }

    public void testIssue217360_01() throws Exception {
        checkDeclaration(getTestPath(), "$two = $this->getT^wo();", "private function ^getTwo() //One");
    }

    public void testIssue217360_02() throws Exception {
        checkDeclaration(getTestPath(), "return $two->getT^wo();", "public function ^getTwo() //Two");
    }

    public void testIssue217360_03() throws Exception {
        checkDeclaration(getTestPath(), "(new Two)->getT^wo();", "public function ^getTwo() //Two");
    }

    public void testUseFuncAndConst_01() throws Exception {
        checkDeclaration(getTestPath(), "use const Name\\Space\\F^OO;", "const ^FOO = 42;");
    }

    public void testUseFuncAndConst_02() throws Exception {
        checkDeclaration(getTestPath(), "use const Name\\Space\\F^OO as FOO2;", "const ^FOO = 42;");
    }

    public void testUseFuncAndConst_03() throws Exception {
        checkDeclaration(getTestPath(), "use const Name\\Space\\FOO as F^OO2;", "use const Name\\Space\\FOO as ^FOO2;");
    }

    public void testUseFuncAndConst_04() throws Exception {
        checkDeclaration(getTestPath(), "use function Name\\Space\\f^nc;", "function ^fnc() {}");
    }

    public void testUseFuncAndConst_05() throws Exception {
        checkDeclaration(getTestPath(), "use function Name\\Space\\f^nc as fnc2;", "function ^fnc() {}");
    }

    public void testUseFuncAndConst_06() throws Exception {
        checkDeclaration(getTestPath(), "use function Name\\Space\\fnc as f^nc2;", "use function Name\\Space\\fnc as ^fnc2;");
    }

    public void testUseFuncAndConst_07() throws Exception {
        checkDeclaration(getTestPath(), "echo F^OO;", "const ^FOO = 42;");
    }

    public void testUseFuncAndConst_08() throws Exception {
        checkDeclaration(getTestPath(), "echo F^OO2;", "use const Name\\Space\\FOO as ^FOO2;");
    }

    public void testUseFuncAndConst_09() throws Exception {
        checkDeclaration(getTestPath(), "f^nc();", "function ^fnc() {}");
    }

    public void testUseFuncAndConst_10() throws Exception {
        checkDeclaration(getTestPath(), "f^nc2();", "use function Name\\Space\\fnc as ^fnc2;");
    }

    public void testIssue244317_01() throws Exception {
        checkDeclaration(getTestPath(), "$variable = self::testCon^stant;", "const ^testConstant = \"test\";");
    }

    public void testIssue244317_02() throws Exception {
        checkDeclaration(getTestPath(), "echo self::testCon^stant;", "const ^testConstant = \"test\";");
    }

    public void testReturnType_01() throws Exception {
        checkDeclaration(getTestPath(), "    public function getLogger(): Lo^gger {", "interface ^Logger {");
    }

    public void testReturnType_02() throws Exception {
        checkDeclaration(getTestPath(), "function foo(): Log^ger {", "interface ^Logger {");
    }

    public void testAnonymousClass01_01() throws Exception {
        checkDeclaration(getTestPath(), "$anonCls = new class implements Lo^gger {", "interface ^Logger {");
    }

    public void testAnonymousClass01_02() throws Exception {
        checkDeclaration(getTestPath(), "        echo $m^sg; // 1", "    public function log(string $^msg) { // 1");
    }

    public void testAnonymousClass01_03() throws Exception {
        checkDeclaration(getTestPath(), "var_dump($ano^nCls);", "$^anonCls = new class implements Logger {");
    }

    public void testAnonymousClass01_04() throws Exception {
        checkDeclaration(getTestPath(), "$app->setLogger(new class implements Lo^gger {", "interface ^Logger {");
    }

    public void testAnonymousClass01_05() throws Exception {
        checkDeclaration(getTestPath(), "        echo $m^sg; // 2", "    public function log(string $^msg) { // 2");
    }

    public void testAnonymousClass01_06() throws Exception {
        checkDeclaration(getTestPath(), "(new class implements Logg^er {", "interface ^Logger {");
    }

    public void testAnonymousClass01_07() throws Exception {
        checkDeclaration(getTestPath(), "        echo $ms^g; // 3", "    public function log(string $^msg) { // 3");
    }

    public void testAnonymousClass01_08() throws Exception {
        checkDeclaration(getTestPath(), "})->l^og('hello world');", "    public function ^log(string $msg) { // 3");
    }

    public void testAnonymousClass02() throws Exception {
        checkDeclaration(getTestPath(), "        $this->tes^tB();", "    private function ^testB() {");
    }

    public void testAnonymousClass03_01() throws Exception {
        checkDeclaration(getTestPath(), "        return new class($this->pr^op) extends Outer {", "    private $^prop = 1;");
    }

    public void testAnonymousClass03_02() throws Exception {
        checkDeclaration(getTestPath(), "        return new class($this->prop) extends Ou^ter {", "class ^Outer {");
    }

    public void testAnonymousClass03_03() throws Exception {
        checkDeclaration(getTestPath(), "                $this->pr^op3 = $prop;", "            private $^prop3;");
    }

    // XXX
//    public void testAnonymousClass03_04() throws Exception {
//        checkDeclaration(getTestPath(), "                $this->prop3 = $p^rop;", "            public function __construct($^prop) {");
//    }

    public void testAnonymousClass03_05() throws Exception {
        checkDeclaration(getTestPath(), "                return $this->pr^op2 + $this->prop3 + $this->func1();", "    protected $^prop2 = 2;");
    }

    public void testAnonymousClass03_06() throws Exception {
        checkDeclaration(getTestPath(), "                return $this->prop2 + $this->prop^3 + $this->func1();", "            private $^prop3;");
    }

    public void testAnonymousClass03_07() throws Exception {
        checkDeclaration(getTestPath(), "                return $this->prop2 + $this->prop3 + $this->fun^c1();", "    protected function ^func1() {");
    }

    public void testAnonymousClass03_08() throws Exception {
        checkDeclaration(getTestPath(), "echo (new Ou^ter)->func2()->func3() . PHP_EOL;", "class ^Outer {");
    }

    public void testAnonymousClass03_09() throws Exception {
        checkDeclaration(getTestPath(), "echo (new Outer)->fun^c2()->func3() . PHP_EOL;", "    public function ^func2() {");
    }

    public void testAnonymousClass03_10() throws Exception {
        checkDeclaration(getTestPath(), "echo (new Outer)->func2()->fun^c3() . PHP_EOL;", "            public function ^func3() {");
    }

    public void testAnonymousClass04_01() throws Exception {
        checkDeclaration(getTestPath(), "var_dump(new class(10) extends Som^eClass implements SomeInterface {", "class ^SomeClass {");
    }

    public void testAnonymousClass04_02() throws Exception {
        checkDeclaration(getTestPath(), "var_dump(new class(10) extends SomeClass implements SomeInt^erface {", "interface ^SomeInterface {");
    }

    public void testAnonymousClass04_03() throws Exception {
        checkDeclaration(getTestPath(), "    use Some^Trait;", "trait ^SomeTrait {");
    }

    public void testAnonymousClass05() throws Exception {
        checkDeclaration(getTestPath(), "$anon = new class($int, fo^o()) {", "function ^foo() {");
    }

    public void testAnonymousClass06_01() throws Exception {
        checkDeclaration(getTestPath(), "        $this->used^Field = 10;", "    private $^usedField;");
    }

    public void testAnonymousClass06_02() throws Exception {
        checkDeclaration(getTestPath(), "        self::$usedS^taticField = 20;", "    private static $^usedStaticField;");
    }

    public void testAnonymousClass06_03() throws Exception {
        checkDeclaration(getTestPath(), "        $this->usedPrivat^eMethod();", "    private function ^usedPrivateMethod() {");
    }

    public void testAnonymousClass06_04() throws Exception {
        checkDeclaration(getTestPath(), "        self::usedStaticP^rivateMethod();", "    private static function ^usedStaticPrivateMethod() {");
    }

    public void testAnonymousClass07_01() throws Exception {
        checkDeclaration(getTestPath(), "$x = new class implements MyL^ogger {", "use api\\Logger as ^MyLogger;");
    }

    public void testAnonymousClass07_02() throws Exception {
        checkDeclaration(getTestPath(), "$x->l^og('');", "    public function ^log($msg) {");
    }

    public void testGroupUse_01() throws Exception {
        checkDeclaration(getTestPath(), "    B\\C\\Cls^ABC2 AS MyCls", "class ^ClsABC2 {");
    }

    public void testGroupUse_02() throws Exception {
        checkDeclaration(getTestPath(), "$a = new Cl^sA();", "class ^ClsA {");
    }

    public void testGroupUse_03() throws Exception {
        checkDeclaration(getTestPath(), "$a->te^st();", "    public function ^test() { // ClsA");
    }

    public void testGroupUse_04() throws Exception {
        checkDeclaration(getTestPath(), "$ab = new Cl^sAB();", "class ^ClsAB {");
    }

    public void testGroupUse_05() throws Exception {
        checkDeclaration(getTestPath(), "$ab->te^st();", "    public function ^test() { // ClsAB");
    }

    public void testGroupUse_06() throws Exception {
        checkDeclaration(getTestPath(), "$abc = new Cls^ABC();", "class ^ClsABC {");
    }

    public void testGroupUse_07() throws Exception {
        checkDeclaration(getTestPath(), "$abc->tes^t();", "    public function ^test() { // ClsABC");
    }

    public void testGroupUse_08() throws Exception {
        checkDeclaration(getTestPath(), "$mycls = new MyC^ls();", "    B\\C\\ClsABC2 AS ^MyCls");
    }

    public void testGroupUse_09() throws Exception {
        checkDeclaration(getTestPath(), "$mycls->te^st();", "    public function ^test() { // ClsABC2");
    }

    public void testGroupUseConst_01() throws Exception {
        checkDeclaration(getTestPath(), "    B\\C\\CA^BC AS MyCABC", "const ^CABC = 'CABC';");
    }

    public void testGroupUseConst_02() throws Exception {
        checkDeclaration(getTestPath(), "echo C^A . PHP_EOL;", "const ^CA = 'CA';");
    }

    public void testGroupUseConst_03() throws Exception {
        checkDeclaration(getTestPath(), "echo C^AB . PHP_EOL;", "const ^CAB = 'CAB';");
    }

    public void testGroupUseConst_04() throws Exception {
        checkDeclaration(getTestPath(), "echo CA^BC . PHP_EOL;", "const ^CABC = 'CABC';");
    }

    public void testGroupUseConst_05() throws Exception {
        checkDeclaration(getTestPath(), "echo MyC^ABC . PHP_EOL;", "    B\\C\\CABC AS ^MyCABC");
    }

    public void testGroupUseFunc_01() throws Exception {
        checkDeclaration(getTestPath(), "    B\\C\\f^abc AS MyFabc", "function ^fabc() {");
    }

    public void testGroupUseFunc_02() throws Exception {
        checkDeclaration(getTestPath(), "echo f^a();", "function ^fa() {");
    }

    public void testGroupUseFunc_03() throws Exception {
        checkDeclaration(getTestPath(), "echo fa^b();", "function ^fab() {");
    }

    public void testGroupUseFunc_04() throws Exception {
        checkDeclaration(getTestPath(), "echo fa^bc();", "function ^fabc() {");
    }

    public void testGroupUseFunc_05() throws Exception {
        checkDeclaration(getTestPath(), "echo MyFa^bc();", "    B\\C\\fabc AS ^MyFabc");
    }

    public void testGroupUseMixed_01() throws Exception {
        checkDeclaration(getTestPath(), "    My^A,", "class ^MyA {}");
    }

    public void testGroupUseMixed_02() throws Exception {
        checkDeclaration(getTestPath(), "new M^yA();", "class ^MyA {}");
    }

    public void testGroupUseMixed_03() throws Exception {
        checkDeclaration(getTestPath(), "    const CONST^ANT,", "const ^CONSTANT = \"CONSTANT\";");
    }

    public void testGroupUseMixed_04() throws Exception {
        checkDeclaration(getTestPath(), "echo CON^STANT; // CONSTANT", "const ^CONSTANT = \"CONSTANT\";");
    }

    public void testGroupUseMixed_05() throws Exception {
        checkDeclaration(getTestPath(), "    function te^st,", "function ^test() {");
    }

    public void testGroupUseMixed_06() throws Exception {
        checkDeclaration(getTestPath(), "    function te^st AS mytest", "function ^test() {");
    }

    public void testGroupUseMixed_07() throws Exception {
        checkDeclaration(getTestPath(), "t^est(); // test", "function ^test() {");
    }

    public void testGroupUseMixed_08() throws Exception {
        checkDeclaration(getTestPath(), "my^test(); // test", "    function test AS ^mytest");
    }

    public void testUniformVariableSyntax_01() throws Exception {
        checkDeclaration(getTestPath(), "UV^S3::myStatic3()::myStatic2();", "class ^UVS3 {");
    }

    public void testUniformVariableSyntax_02() throws Exception {
        checkDeclaration(getTestPath(), "UVS3::mySt^atic3()::myStatic2();", "    public static function ^myStatic3(): UVS2 {");
    }

    public void testUniformVariableSyntax_03() throws Exception {
        checkDeclaration(getTestPath(), "UVS3::myStatic3()::mySt^atic2();", "    public static function ^myStatic2(): UVS1 {");
    }

    public void testUniformVariableSyntax_04() throws Exception {
        checkDeclaration(getTestPath(), "UVS3::myStatic3()::myStatic2()::mySt^atic1()::MAX;", "    public static function ^myStatic1() {");
    }

    public void testUniformVariableSyntax_05() throws Exception {
        checkDeclaration(getTestPath(), "UVS3::myStatic3()::myStatic2()::myStatic1()::M^AX;", "    const ^MAX = 99;");
    }

    public void testUniformVariableSyntax_06() throws Exception {
        checkDeclaration(getTestPath(), "UVS3::myStatic3()::myStatic2()::myStatic1()::A^VG;", "    const ^AVG = 50;");
    }

    public void testUniformVariableSyntax_07() throws Exception {
        checkDeclaration(getTestPath(), "UVS3::myStatic3()::myStatic2()::myStatic1()::$M^IN;", "    static $^MIN = \"MIN\";");
    }

    public void testUniformVariableSyntax_08() throws Exception {
        checkDeclaration(getTestPath(), "UVS3::myStatic3()::myStatic2()::myStatic1()::$TO^TAL;", "    static $^TOTAL = \"TOTAL\";");
    }

    public void testUniformVariableSyntax_09() throws Exception {
        checkDeclaration(getTestPath(), "UVS3::myStatic3()::myStatic2()::myStatic1()::$INST^ANCE::myStatic1();", "    static $^INSTANCE;");
    }

    public void testUniformVariableSyntax_10() throws Exception {
        checkDeclaration(getTestPath(), "UVS3::myStatic3()::myStatic2()::myStatic1()::$INSTANCE::myStati^c1();", "    public static function ^myStatic1() {");
    }

    public void testUniformVariableSyntax_11() throws Exception {
        checkDeclaration(getTestPath(), "UVS3::myStatic3()::myStatic2()::myStatic1()::mySt^atic2();", "    public static function ^myStatic2() { // UVS1");
    }

    public void testUniformVariableSyntax_12() throws Exception {
        checkDeclaration(getTestPath(), "UVS3::myStatic3()->myStatic2()::myStatic1()->mySt^atic2();", "    public static function ^myStatic2() { // UVS1");
    }

    public void testUniformVariableSyntax_13() throws Exception {
        checkDeclaration(getTestPath(), "UVS3::myStatic3()->myStatic2()::myStatic1()->te^st;", "    public $^test = null;");
    }

    public void testUniformVariableSyntax_14() throws Exception {
        checkDeclaration(getTestPath(), "f^oo()::myStatic2();", "function ^foo(): UVS2 {");
    }

    public void testUniformVariableSyntax_15() throws Exception {
        checkDeclaration(getTestPath(), "foo()::mySta^tic2();", "    public static function ^myStatic2(): UVS1 {");
    }

    public void testUniformVariableSyntax_16() throws Exception {
        checkDeclaration(getTestPath(), "foo()::myStatic2()::myStati^c2();", "    public static function ^myStatic2() { // UVS1");
    }

    public void testIssue247082_01() throws Exception {
        checkDeclaration(getTestPath(), "            echo parent::PAREN^T_CONST . PHP_EOL;", "    const ^PARENT_CONST = 'PARENT_CONST';");
    }

    public void testIssue247082_02() throws Exception {
        checkDeclaration(getTestPath(), "            echo parent::$paren^tFieldStatic . PHP_EOL;", "    public static $^parentFieldStatic = 'parentFieldStatic';");
    }

    public void testIssue247082_03() throws Exception {
        checkDeclaration(getTestPath(), "            echo parent::parent^TestInstance() . PHP_EOL;", "    public function ^parentTestInstance() {");
    }

    public void testIssue247082_04() throws Exception {
        checkDeclaration(getTestPath(), "            echo parent::parentTes^tStatic() . PHP_EOL;", "    public static function ^parentTestStatic() {");
    }

    public void testIssue247082_05() throws Exception {
        checkDeclaration(getTestPath(), "            echo $this->myFieldI^nstance . PHP_EOL;", "    public $^myFieldInstance = 'myFieldInstance';");
    }

    public void testIssue247082_06() throws Exception {
        checkDeclaration(getTestPath(), "            echo $this->myTest^Instance() . PHP_EOL;", "    public function ^myTestInstance() {");
    }

    public void testIssue247082_07() throws Exception {
        checkDeclaration(getTestPath(), "            echo self::MY_C^ONST . PHP_EOL;", "    const ^MY_CONST = 'MY_CONST';");
    }

    public void testIssue247082_08() throws Exception {
        checkDeclaration(getTestPath(), "            echo self::$myFieldS^tatic . PHP_EOL;", "    public static $^myFieldStatic = 'myFieldStatic';");
    }

    public void testIssue247082_09() throws Exception {
        checkDeclaration(getTestPath(), "            echo self::myTe^stStatic() . PHP_EOL;", "    public static function ^myTestStatic() {");
    }

    // related to #171249
    public void testIssue270422() throws Exception {
        checkDeclaration(getTestPath(), " * @method Paren^tClass testMethod()", "class ^ParentClass {");
    }

    //TODO: these tests need to be checked, filtered , rewritten , enabled
//    public void testGotoTypeClsIface6() throws Exception {
//        String gotoTest = prepareTestFile(
//                "testfiles/gotoType2.php",
//                "interface ifaceDeclaration4 {}",
//                "interface ^ifaceDeclaration4 {}"
//                );
//        String gotoTest2 = prepareTestFile(
//                "testfiles/gotoType.php",
//                "class clsDeclaration4 extends clsDeclaration3 implements ifaceDeclaration4 {}",
//                "class clsDeclaration4 extends clsDeclaration3 implements ifaceDecla|ration4 {}"
//                );
//        performTestSimpleFindDeclaration(-1, gotoTest2, gotoTest);
//    }
//
//    public void testGotoTypeClsIfaceFromalParam3() throws Exception {
//        String gotoTest = prepareTestFile(
//                "testfiles/gotoType2.php",
//                "interface ifaceDeclaration4 {}",
//                "interface ^ifaceDeclaration4 {}"
//                );
//        String gotoTest2 = prepareTestFile(
//                "testfiles/gotoType.php",
//                "ifaceDeclaration4 $ifaceDeclaration4Var,",
//                "ifaceD|eclaration4 $ifaceDeclaration4Var,"
//                );
//        performTestSimpleFindDeclaration(-1, gotoTest2, gotoTest);
//    }
//    public void testStaticFieldAccessInOtherFile() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public static $count = 0, $animal;",
//                "public static $^count = 0, $animal;"
//                );
//        String animal2Test = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "return Animal::$count;",
//                "return Animal::$cou|nt;;"
//                );
//        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
//    }
//    public void testStaticFieldAccessInOtherFileRef() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public static $count = 0, $animal;",
//                "public static $^count = 0, $animal;"
//                );
//        String animal2Test = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "return Animal::$count;",
//                "return &Animal::$cou|nt;"
//                );
//        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
//    }
//    public void testStaticFieldAccessParentInOtherFile() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public static $count = 0, $animal;",
//                "public static $^count = 0, $animal;"
//                /*maybe a bug but not important I guess. Sometimes jumps:
//                 * 1/public static ^$count = 0, $animal;
//                 * 2/^public static $count = 0, $animal;
//                 */
//                );
//        String animal2Test = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "parent::$count;",
//                "parent::$cou|nt;"
//                );
//        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
//    }
//    public void testStaticFieldAccessParentInOtherFileRef() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public static $count = 0, $animal;",
//                "public static $^count = 0, $animal;"
//                /*maybe a bug but not important I guess. Sometimes jumps:
//                 * 1/public static ^$count = 0, $animal;
//                 * 2/^public static $count = 0, $animal;
//                 */
//                );
//        String animal2Test = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "parent::$count;",
//                "&parent::$cou|nt;"
//                );
//        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
//    }
//
//    public void testStaticFieldAccessInOtherFile_ClassName() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "abstract class Animal {",
//                "abstract class ^Animal {"
//                );
//        String animal2Test = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "return Animal::$count;",
//                "return Ani|mal::$count;;"
//                );
//        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
//    }
//    public void testStaticFieldAccessInOtherFile_ClassNameRef() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "abstract class Animal {",
//                "abstract class ^Animal {"
//                );
//        String animal2Test = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "return Animal::$count;",
//                "return &Ani|mal::$count;;"
//                );
//        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
//    }
//    public void testSuperClassesOtherClass() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "abstract class Animal {",
//                "abstract class ^Animal {"
//                );
//        String animal2Test = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "class Fish extends Animal {",
//                "class Fish extends Anim|al {"
//                );
//        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
//    }
//    public void testMethodInvocationFromOtherClassThis() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public function getCount($animalLogging) {",
//                "public function ^getCount($animalLogging) {"
//                );
//        String animal2Test = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "$this->getCount(\"\")",
//                "$this->getCo|unt(\"\")"
//                );
//        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
//    }
//    public void testMethodInvocationFromOtherClassParent() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public function getCount($animalLogging) {",
//                "public function ^getCount($animalLogging) {"
//                );
//        String animal2Test = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "parent::getCount(\"\")",
//                "parent::getCo|unt(\"\")"
//                );
//        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
//    }
//    public void testMethodInvocationFromOtherClassSelf() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public function getCount($animalLogging) {",
//                "public function ^getCount($animalLogging) {"
//                );
//        String animal2Test = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "self::getCount(\"\")",
//                "self::getCo|unt(\"\")"
//                );
//        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
//    }
//    public void testMethodInvocationFromOther2() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public function getCount($animalLogging) {",
//                "public function ^getCount($animalLogging) {"
//                );
//        String animal2Test = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "$mammal->getCount(\"\")",
//                "$mammal->getCo|unt(\"\")"
//                );
//        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
//    }
//    public void testMethodInvocationFromOther4() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public function getCount($animalLogging) {",
//                "public function ^getCount($animalLogging) {"
//                );
//        String animal2Test = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "$fish->getCount(\"\")",
//                "$fish->getCo|unt(\"\")"
//                );
//        performTestSimpleFindDeclaration(-1, animal2Test, animalTest);
//    }
//    public void testMethodInvocationFromOther5() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "public function getCount($sharkLogging) {",
//                "public function ^getCount($sharkLogging) {",
//                "$shark->getCount(\"\");",
//                "$$shark->getCou|nt(\"\");"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest);
//    }
//    public void testConstantAccess() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "const KIND=1;",
//                "const ^KIND=1;",
//                "echo self::KIND;",
//                "echo self::KIN|D;"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest);
//    }
//    public void testConstantAccess12() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "const KIND=1;",
//                "const ^KIND=1;"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo Animal::KIND;",
//                "echo Animal::KIN|D;"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//    public void testConstantAccess12_1() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "abstract class Animal {",
//                "abstract class ^Animal {"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo Animal::KIND;",
//                "echo Ani|mal::KIND;"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//    public void testConstantAccess13() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "const KIND=1;",
//                "const ^KIND=1;"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo Mammal::KIND;",
//                "echo Mammal::KI|ND;"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//    public void testConstantAccess13_1() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "class Mammal extends Animal {",
//                "class ^Mammal extends Animal {"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo Mammal::KIND;",
//                "echo Mamm|al::KIND;"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//    public void testConstantAccess14() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "const KIND=3;",
//                "const ^KIND=3;"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo Cat::KIND;",
//                "echo Cat::KI|ND;"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//    public void testConstantAccess15() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "const KIND=1;",
//                "const ^KIND=1;"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "print Animal::KIND;",
//                "print Animal::KIN|D;"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//    public void testConstantAccess15_1() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "abstract class Animal {",
//                "abstract class ^Animal {"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "print Animal::KIND;",
//                "print Anim|al::KIND;"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//    public void testConstantAccess16() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "const KIND=1;",
//                "const ^KIND=1;"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "print Mammal::KIND;",
//                "print Mammal::KIN|D;"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//    public void testConstantAccess16_1() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "class Mammal extends Animal {",
//                "class ^Mammal extends Animal {"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "print Mammal::KIND;",
//                "print Ma|mmal::KIND;"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//
//    public void testStaticMethodInvocation8() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public static function kindInfo() {return \"animal is ...\";}",
//                "public static function ^kindInfo() {return \"animal is ...\";}"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo Animal::kindInfo();",
//                "echo Animal::kindIn|fo();"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//    public void testStaticMethodInvocation8_1() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "abstract class Animal {",
//                "abstract class ^Animal {"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo Animal::kindInfo();",
//                "echo Ani|mal::kindInfo();"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//    public void testStaticMethodInvocation9() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public static function kindInfo() {return \"animal is ...\";}",
//                "public static function ^kindInfo() {return \"animal is ...\";}"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo Mammal::kindInfo();",
//                "echo Mammal::kindI|nfo();"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//    public void testStaticMethodInvocation9_1() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "class Mammal extends Animal {",
//                "class ^Mammal extends Animal {"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo Mammal::kindInfo();",
//                "echo Mam|mal::kindInfo();"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }
//    /* TODO: fails, evaluate, fix
//     public void testStaticMethodInvocation10() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public static function kindInfo() {return \"cat is ...\";}",
//                "^public static function kindInfo() {return \"cat is ...\";}"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo Cat::kindInfo();",
//                "echo Cat::kindIn|fo();"
//                );
//        performTestSimpleFindDeclaration(-1, animalTest2, animalTest);
//    }*/
//    public void testStaticMethodInvocation10_1() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "class Cat extends Mammal {",
//                "class ^Cat extends Mammal {"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo Cat::kindInfo();",
//                "echo C|at::kindInfo();"
//                );
//        performTestSimpleFindDeclaration(-1,animalTest2, animalTest);
//    }
//    public void testStaticMethodInvocation11() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public static function kindInfo() {return \"animal is ...\";}",
//                "public static function ^kindInfo() {return \"animal is ...\";}"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo self::kindInfo();",
//                "echo self::kindIn|fo();"
//                );
//        performTestSimpleFindDeclaration(-1,animalTest2, animalTest);
//    }
//    public void testStaticMethodInvocation12() throws Exception {
//        String animalTest = prepareTestFile(
//                "testfiles/animalTest.php",
//                "public static function kindInfo() {return \"animal is ...\";}",
//                "public static function ^kindInfo() {return \"animal is ...\";}"
//                );
//        String animalTest2 = prepareTestFile(
//                "testfiles/animalTest2.php",
//                "echo parent::kindInfo();",
//                "echo parent::kindIn|fo();"
//                );
//        performTestSimpleFindDeclaration(-1,animalTest2, animalTest);
//    }
//
//
//    public void testDefines2() throws Exception {
//        performTestSimpleFindDeclaration(0,
//                                         "<?php\n" +
//                                         "include \"testa.php\";\n" +
//                                         "echo \"a\".te|st.\"b\";\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "define('^test', 'test');\n" +
//                                         "?>");
//    }
//
//    public void testSimpleFindDeclaration1() throws Exception {
//        performTestSimpleFindDeclaration("<?php\n$^name = \"test\";\n echo \"$na|me\";\n?>");
//    }
//
//    public void testSimpleFindDeclaration2() throws Exception {
//        performTestSimpleFindDeclaration("<?php\n$^name = \"test\";\n$name = \"test\";\n echo \"$na|me\";\n?>");
//    }
//
//    public void testSimpleFindDeclaration3() throws Exception {
//        performTestSimpleFindDeclaration("<?php\n$^name = \"test\";\n$name = \"test\";\n echo \"$na|me\";\n$name = \"test\";\n?>");
//    }
//
//    public void testSimpleFindDeclaration4() throws Exception {
//        performTestSimpleFindDeclaration("<?php\n" +
//                                         "$name = \"test\";\n" +
//                                         "function foo($^name) {\n" +
//                                         "    echo \"$na|me\";\n" +
//                                         "}\n" +
//                                         "?>");
//    }
//
//    public void testSimpleFindDeclaration5() throws Exception {
//        performTestSimpleFindDeclaration("<?php\n" +
//                                         "$^name = \"test\";\n" +
//                                         "function foo($name) {\n" +
//                                         "}\n" +
//                                         "echo \"$na|me\";\n" +
//                                         "?>");
//    }
//
//    public void testSimpleFindDeclaration6() throws Exception {
//        performTestSimpleFindDeclaration("<?php\n" +
//                                         "$name = \"test\";\n" +
//                                         "function ^foo($name) {\n" +
//                                         "}\n" +
//                                         "fo|o($name);\n" +
//                                         "?>");
//    }
//
//    public void testSimpleFindDeclaration7() throws Exception {
//        performTestSimpleFindDeclaration("<?php\n" +
//                                         "class ^name {\n" +
//                                         "}\n" +
//                                         "$r = new na|me();\n" +
//                                         "?>");
//    }
//
//    public void testSimpleFindDeclaration8() throws Exception {
//        performTestSimpleFindDeclaration("<?php\n" +
//                                         "class name {\n" +
//                                         "    function ^test() {" +
//                                         "    }" +
//                                         "}\n" +
//                                         "$r = new name();\n" +
//                                         "$r->te|st();" +
//                                         "?>");
//    }
//
//    public void testSimpleFindDeclaration9() throws Exception {
//        performTestSimpleFindDeclaration("<?php\n" +
//                                         "$^name = \"test\";\n" +
//                                         "function foo($name) {\n" +
//                                         "}\n" +
//                                         "foo($na|me);\n" +
//                                         "?>");
//    }
//
//    public void testFindDeclarationInOtherFile1() throws Exception {
//        performTestSimpleFindDeclaration(1,
//                                         "<?php\n" +
//                                         "include \"testa.php\";\n" +
//                                         "fo|o();\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "function ^foo() {}\n" +
//                                         "?>");
//    }
//
//    public void testFindDeclarationInOtherFile2() throws Exception {
//        performTestSimpleFindDeclaration(2,
//                                         "<?php\n" +
//                                         "include \"testa.php\";\n" +
//                                         "fo|o();\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "include \"testb.php\";\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "function ^foo() {}\n" +
//                                         "?>");
//    }
//
//    public void testFindDeclarationInOtherFile3() throws Exception {
//        performTestSimpleFindDeclaration(2,
//                                         "<?php\n" +
//                                         "include \"testa.php\";\n" +
//                                         "$r = new fo|o();\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "include \"testb.php\";\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "class ^foo {}\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "class ^foo {}\n" +
//                                         "?>");
//    }
//
//    public void testFunctionsInGlobalScope1() throws Exception {
//        performTestSimpleFindDeclaration(0,
//                                         "<?php\n" +
//                                         "function ^foo() {}\n" +
//                                         "function bar() {\n" +
//                                         "    fo|o();\n" +
//                                         "}\n" +
//                                         "?>");
//    }
//
//    public void testClassInGlobalScope1() throws Exception {
//        performTestSimpleFindDeclaration(0,
//                                         "<?php\n" +
//                                         "function foo() {" +
//                                         "    class ^bar {}\n" +
//                                         "}\n" +
//                                         "$r = new b|ar();\n" +
//                                         "?>");
//    }
//
//    public void testArrayVariable() throws Exception {
//        performTestSimpleFindDeclaration(0,
//                                         "<?php\n" +
//                                         "$^foo = array();\n" +
//                                         "$f|oo['test'] = array();\n" +
//                                         "?>");
//    }
//
//    public void testResolveUseBeforeDeclaration() throws Exception {
//        performTestSimpleFindDeclaration(0,
//                                         "<?php\n" +
//                                         "fo|o();\n" +
//                                         "function ^foo() {}\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "function foo() {}\n" +
//                                         "?>");
//    }
//
//    public void testShowAllDeclarationsWhenUnknownForFunctions() throws Exception {
//        performTestSimpleFindDeclaration(0,
//                                         "<?php\n" +
//                                         "fo|o();\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "function ^foo() {}\n" +
//                                         "?>",
//                                          "<?php\n" +
//                                         "function ^foo() {}\n" +
//                                         "?>");
//    }
//
//    public void testShowAllDeclarationsWhenUnknownForClasses() throws Exception {
//        performTestSimpleFindDeclaration(0,
//                                         "<?php\n" +
//                                         "$r = new fo|o();\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "class ^foo {}\n" +
//                                         "?>",
//                                          "<?php\n" +
//                                         "class ^foo {}\n" +
//                                         "?>");
//    }
//
//    public void testDefines1() throws Exception {
//        performTestSimpleFindDeclaration(0,
//                                         "<?php\n" +
//                                         "define('^test', 'test');\n" +
//                                         "echo \"a\".te|st.\"b\";\n" +
//                                         "?>");
//    }
//
//    public void testGoToInherited() throws Exception {
//        performTestSimpleFindDeclaration(0,
//                                         "<?php\n" +
//                                         "class foo {\n" +
//                                         "    function ^test() {}\n" +
//                                         "}\n" +
//                                         "class bar extends foo {\n" +
//                                         "}\n" +
//                                         "$r = new bar();\n" +
//                                         "$r->te|st();" +
//                                         "?>");
//    }
//
//    public void testGoToInclude01() throws Exception {
//        performTestSimpleFindDeclaration(2,
//                                         "<?php\n" +
//                                         "include \"te|sta.php\";\n" +
//                                         "?>",
//                                         "^<?php\n" +
//                                         "function foo() {}\n" +
//                                         "?>");
//    }
//
//    public void testGoToInclude02() throws Exception {
//        performTestSimpleFindDeclaration(2,
//                                         "<?php\n" +
//                                         "include ('|testa.php');\n" +
//                                         "?>",
//                                         "^<?php\n" +
//                                         "function foo() {}\n" +
//                                         "?>");
//    }
//
//    public void testGoToInclude03() throws Exception {
//        performTestSimpleFindDeclaration(2,
//                                         "<?php\n" +
//                                         "require 'testa.php|';\n" +
//                                         "?>",
//                                         "^<?php\n" +
//                                         "function foo() {}\n" +
//                                         "?>");
//    }
//
//    public void testGoToInclude04() throws Exception {
//        performTestSimpleFindDeclaration(2,
//                                         "<?php\n" +
//                                         "include_once '|testa.php';\n" +
//                                         "?>",
//                                         "^<?php\n" +
//                                         "function foo() {}\n" +
//                                         "?>");
//    }
//
//    public void testGoToInclude05() throws Exception {
//        performTestSimpleFindDeclaration(2,
//                                         "<?php\n" +
//                                         "include_once ('|testa.php');\n" +
//                                         "?>",
//                                         "^<?php\n" +
//                                         "function foo() {}\n" +
//                                         "?>");
//    }
//
//    public void testGoToInclude06() throws Exception {
//        performTestSimpleFindDeclaration(2,
//                                         "<?php\n" +
//                                         "require_once '|testa.php';\n" +
//                                         "?>",
//                                         "^<?php\n" +
//                                         "function foo() {}\n" +
//                                         "?>");
//    }
//
//    public void testGoToInclude07() throws Exception {
//        performTestSimpleFindDeclaration(2,
//                                         "<?php\n" +
//                                         "require_once (\"|testa.php\");\n" +
//                                         "?>",
//                                         "^<?php\n" +
//                                         "function foo() {}\n" +
//                                         "?>");
//    }
//
//    public void testGoToInstanceVar() throws Exception {
//        performTestSimpleFindDeclaration("<?php\n" +
//                                         "class test {\n" +
//                                         "    function ftest($name) {\n" +
//                                         "        $this->na|me = $name;\n" +
//                                         "    }\n" +
//                                         "    var $^name;\n" +
//                                         "}\n" +
//                                         "?>");
//    }
//
//    public void testGoToForward() throws Exception {
//        performTestSimpleFindDeclaration("<?php\n" +
//                                         "class test {\n" +
//                                         "    function ftest($name) {\n" +
//                                         "        $this->na|me();\n" +
//                                         "    }\n" +
//                                         "    function ^name() {}\n" +
//                                         "}\n" +
//                                         "?>");
//    }
//
//    public void testMethodInOtherFile() throws Exception {
//        performTestSimpleFindDeclaration(-1,
//                                         "<?php\n" +
//                                         "include \"testa.php\";\n" +
//                                         "$r = new foo();\n" +
//                                         "$r->ffo|o();\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "include \"testb.php\";\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "class foo {\n" +
//                                         "    function ^ffoo() {\n" +
//                                         "    }\n" +
//                                         "}\n" +
//                                         "?>");
//    }
//
//    public void testMethodInOtherFileWithInheritance() throws Exception {
//        performTestSimpleFindDeclaration(-1,
//                                         "<?php\n" +
//                                         "include \"testa.php\";\n" +
//                                         "$r = new foo2();\n" +
//                                         "$r->ffo|o();\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "include \"testb.php\";\n" +
//                                         "class foo2 extends foo3 {}\n" +
//                                         "?>",
//                                         "<?php\n" +
//                                         "class foo3 {\n" +
//                                         "    function ^ffoo() {\n" +
//                                         "    }\n" +
//                                         "}\n" +
//                                         "?>");
//    }
//
//    public void testPHPDocType01() throws Exception {
//        performTestSimpleFindDeclaration(-1,
//                                         "<?php\n" +
//                                         "class Magazine {\n" +
//                                         "    public $title;\n" +
//                                         "}\n" +
//                                         "class ^Book { \n" +
//                                         "    public $author;\n" +
//                                         "}\n" +
//                                         "/**\n" +
//                                         " * @param Bo|ok $hello\n" +
//                                         " * @return Magazine test\n" +
//                                         " */\n" +
//                                         "function test($hello) {\n" +
//                                         "}\n" +
//                                         "?>\n");
//    }
//
//    public void testPHPDocType02() throws Exception {
//        performTestSimpleFindDeclaration(-1,
//                                         "<?php\n" +
//                                         "class ^Magazine {\n" +
//                                         "    public $title;\n" +
//                                         "}\n" +
//                                         "class Book { \n" +
//                                         "    public $author;\n" +
//                                         "}\n" +
//                                         "/**\n" +
//                                         " * @param Book $hello\n" +
//                                         " * @return Mag|azine test\n" +
//                                         " */\n" +
//                                         "function test($hello) {\n" +
//                                         "}\n" +
//                                         "?>\n");
//    }
//
//    public void testPHPDocParamName() throws Exception {
//        performTestSimpleFindDeclaration(-1,
//                                         "<?php\n" +
//                                         "/**\n" +
//                                         " *\n" +
//                                         " * @param  string $he|llo\n" +
//                                         " */\n" +
//                                        "function test($^hello) {\n" +
//                                         "}\n" +
//                                         "?> ");

}
