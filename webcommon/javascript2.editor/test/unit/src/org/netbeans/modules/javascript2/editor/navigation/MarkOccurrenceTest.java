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
package org.netbeans.modules.javascript2.editor.navigation;

import java.io.IOException;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class MarkOccurrenceTest extends JsTestBase {

    public MarkOccurrenceTest(String testName) {
        super(testName);
    }

    @Override
    protected int timeOut() {
        return 5000;
    }
    
    
    public void testArrowFunction01() throws Exception {
        checkOccurrences("testfiles/model/arrowFunction.js", "var ^a = 1;", true);
    }
    
    public void testArrowFunction02() throws Exception {
        checkOccurrences("testfiles/model/arrowFunction.js", "(^a, b) => {", true);
    }
    
    public void testArrowFunction03() throws Exception {
        checkOccurrences("testfiles/model/arrowFunction.js", "var ^b = 2;", true);
    }

    public void testArrowFunction04() throws Exception {
        checkOccurrences("testfiles/model/arrowFunction.js", "(a, ^b) => {", true);
    }
    
    public void testArrowFunction05() throws Exception {
        checkOccurrences("testfiles/model/arrowFunction.js", "var ^x = 10;", true);
    }
    
    public void testArrowFunction06() throws Exception {
        checkOccurrences("testfiles/model/arrowFunction.js", "var y = ^x => x + 5;", true);
    }
    
    public void testDefaultParameters01() throws Exception {
        checkOccurrences("testfiles/model/defaultParameters.js", "function singularAutoPlural(^singular, plural = singular+\"s\",", true);
    }
    
    public void testDefaultParameters02() throws Exception {
        checkOccurrences("testfiles/model/defaultParameters.js", "function singularAutoPlural(singular, ^plural = singular+\"s\",", true);
    }
    
    public void testSimpleObject01() throws Exception {
        checkOccurrences("testfiles/model/simpleObject.js", "var Car^rot = {", true);
    }
    
    public void testSimpleObject02() throws Exception {
        checkOccurrences("testfiles/model/simpleObject.js", "col^or: \"red\",", true);
    }
    
    public void testSimpleObject03() throws Exception {
        checkOccurrences("testfiles/model/simpleObject.js", "this.call^ed = this.called + 1;", true);
    }
    
    public void testSimpleObject04() throws Exception {
        checkOccurrences("testfiles/model/simpleObject.js", "getCo^lor: function () {", true);
    }
    
    public void testAssignments01() throws Exception {
        checkOccurrences("testfiles/model/parameters01.js", "var he^ad = \"head\";", true);
    }
    
    public void testAssignments02() throws Exception {
        checkOccurrences("testfiles/model/parameters01.js", "head = bo^dy;", true);
    }
    
    public void testAssignments03() throws Exception {
        checkOccurrences("testfiles/model/returnTypes02.js", "zi^p = 15000;", true);
    }
    
    public void testFunctionParameters01() throws Exception {
        checkOccurrences("testfiles/model/parameters01.js", "function Joke (name, autor, descri^ption) {", true);
    }
    
    public void testFunctionParameters02() throws Exception {
        checkOccurrences("testfiles/model/parameters01.js", "this.name = na^me;", true);
    }
    
    public void testFunctionParameters03() throws Exception {
        checkOccurrences("testfiles/model/parameters01.js", "formatter.println(\"Author: \" + au^tor);", true);
    }
    
    public void testFunctionParameters04() throws Exception {
        checkOccurrences("testfiles/model/returnTypes02.js", "zip = zi^pp;", true);
    }
    
    public void testMethod01() throws Exception {
        checkOccurrences("testfiles/model/parameters01.js", "formatter.println(\"Name: \" + this.getNa^me());", true);
    }

    public void testUndefinedMethods01() throws Exception {
        checkOccurrences("testfiles/completion/undefinedMethods.js", "dvorek.getPrasatko().udelejChro(dvo^rek.dejDefault(), \"afdafa\");", true);
    }

    public void testUndefinedMethods02() throws Exception {
        checkOccurrences("testfiles/completion/undefinedMethods.js", "dvorek.getPra^satko().udelejChro(dvorek.dejDefault(), \"afdafa\");", true);
    }

    public void testUndefinedMethods03() throws Exception {
        checkOccurrences("testfiles/completion/undefinedMethods.js", "dvorek.getPrasatko().udelejC^hro(dvorek.dejDefault(), \"afdafa\");", true);
    }

    public void testUndefinedMethods04() throws Exception {
        checkOccurrences("testfiles/completion/undefinedMethods.js", "dvorek.getKo^cicku().udelejMau();", true);
    }

    public void testFunctionParameters05() throws Exception {
        checkOccurrences("testfiles/coloring/czechChars.js", "jQuery(function($^){", true);
    }
    
    public void testProperty01() throws Exception {
        checkOccurrences("testfiles/coloring/czechChars.js", "    $.timepic^ker.regional[\"cs\"] = {", true);
    }

    public void testProperty02() throws Exception {
        checkOccurrences("testfiles/coloring/czechChars.js", "    $.timepicker.region^al[\"cs\"] = {", true);
    }
    
    public void testProperty03() throws Exception {
        checkOccurrences("testfiles/coloring/czechChars.js", "    $.timepicker.regional[\"c^s\"] = {", true);
    }

    public void testProperty04() throws Exception {
        checkOccurrences("testfiles/coloring/czechChars.js", "    te^st.anotherProperty = test.myProperty;", true);
    }
    
    public void testProperty05() throws Exception {
        checkOccurrences("testfiles/coloring/czechChars.js", "    test.anotherProperty = test.myPrope^rty;", true);
    }
    
//    public void testGetterSetterInObjectLiteral01() throws Exception {
//        checkOccurrences("testfiles/model/getterSettterInObjectLiteral.js", "set yea^rs(count){this.old = count + 1;},", true);
//    }
//
//    public void testGetterSetterInObjectLiteral02() throws Exception {
//        checkOccurrences("testfiles/model/getterSettterInObjectLiteral.js", "Dog.yea^rs = 10;", true);
//    }
    
    public void testFunctionInGlobalSpace01() throws Exception {
        checkOccurrences("testfiles/model/functionInGlobal.js", "this.printSometh^ing();", true);
    }

    public void testFunctionInGlobalSpace02() throws Exception {
        checkOccurrences("testfiles/model/functionInGlobal.js", "this.anotherFunct^ion();", true);
    }
     
    public void testIssue209717_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue209717_01.js", "foobar = (typeof foo == \"undefined\") ? bar : f^oo;", true);
    }

    public void testIssue209717_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue209717_01.js", "foobar = (typeof foo == \"undefined\") ? b^ar : foo;", true);
    }
    
    public void testIssue209717_03() throws Exception {
        checkOccurrences("testfiles/coloring/issue209717_02.js", "foobar = (typeof foo^22 == \"undefined\") ? bar : foo;", true);
    }
    
    public void testIssue209717_04() throws Exception {
        checkOccurrences("testfiles/coloring/issue209717_03.js", "foobar = (typeof foo^22 == \"undefined\") ? bar : foo;", true);
    }
    
    public void testIssue209717_05() throws Exception {
        checkOccurrences("testfiles/coloring/issue209717_04.js", "fo^o22 = \"fasfdas\";", true);
    }
    
    public void testIssue209941_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue209941.js", "this.globalNot^ify();", true);
    }
    
    public void testIssue198032_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue198032.js", "function leve^l0() {", true);
    }

    public void testIssue198032_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue198032.js", "function level^1_1(){", true);
    }

    public void testIssue198032_03() throws Exception {
        checkOccurrences("testfiles/coloring/issue198032.js", "formatter.println(\"calling level1_2(): \" + lev^el1_2());", true);
    }
    
    public void testIssue198032_04() throws Exception {
        checkOccurrences("testfiles/coloring/issue198032.js", "var fir^st = \"defined in level1_2\";", true);
    }
    
    public void testIssue198032_05() throws Exception {
        checkOccurrences("testfiles/coloring/issue198032.js", "this.lev^el2_1 = function(){", true);
    }
    
    public void testIssue198032_06() throws Exception {
        checkOccurrences("testfiles/coloring/issue198032.js", "var fir^st = \"defined in level0\";// Try rename refactor from here", true);
    }
    
    public void testIssue215554() throws Exception {
        checkOccurrences("testfiles/coloring/issue215554.js", "model: B^ug", true);
    }
    
    public void testIssue215756_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue215756.js", "var lay^out;", true);
    }
    
    public void testIssue215756_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue215756.js", "TEST.te^st();", true);
    }
    
    public void testIssue215756_03() throws Exception {
        checkOccurrences("testfiles/coloring/issue215756.js", "TE^ST.test();", true);
    }
    
    public void testConstructor_1() throws Exception {
        checkOccurrences(getTestPath(), "function Ad^dress (street, town, country) {", true);
    }

    public void testConstructor_2() throws Exception {
        checkOccurrences(getTestPath(), "object = new ^Address(\"V Parku\", \"Prague\", \"Czech Republic\");", true);
    }

    public void testConstructor_3() throws Exception {
        checkOccurrences(getTestPath(), "function C^ar (color, maker) {", true);
    }

    public void testMethodIdent_1() throws Exception {
        checkOccurrences(getTestPath(), "this.color = col^or;", true);
    }

    public void testMethodIdent_2() throws Exception {
        checkOccurrences(getTestPath(), "this.town = t^own;", true);
    }

    public void testGlobalTypes_1() throws Exception {
        checkOccurrences(getTestPath(), "var mujString = new St^ring(\"mujString\");", true);
    }

    public void testDocumentation_1() throws Exception {
        checkOccurrences(getTestPath(), "* @param {Color} co^lor car color", true);
    }

    public void testDocumentation_2() throws Exception {
        checkOccurrences(getTestPath(), "* @param {Co^lor} color car color", true);
    }

    public void testDocumentation_3() throws Exception {
        checkOccurrences(getTestPath(), " * @type Ca^r", true);
    }

    public void testDocumentation_4() throws Exception {
        checkOccurrences(getTestPath(), " * @param {St^ring} street", true);
    }

    public void testDocumentation_5() throws Exception {
        checkOccurrences(getTestPath(), " * @param {String} str^eet", true);
    }

    public void testDocumentation_6() throws Exception {
        checkOccurrences(getTestPath(), "* @return {Addre^ss} address", true);
    }

    public void testDocumentation_7() throws Exception {
        // should return name occurences only from associated method and its comment
        checkOccurrences(getTestPath(), "this.street = stre^et; //another line", true);
    }

    public void testDocumentation_8() throws Exception {
        // should return name occurences only from associated method and its comment
        checkOccurrences(getTestPath(), " * @param {String} co^untry my country", true);
    }
    
    public void testDocumentation_9() throws Exception {
        // return types
        checkOccurrences(getTestPath(), " * @return {Add^ress} address", true);
    }
    
    public void testDocumentation_10() throws Exception {
        // return types
        checkOccurrences(getTestPath(), "function Add^ress (street, town, country) {", true);
    }

    public void testCorrectPrototype_1() throws Exception {
        checkOccurrences(getTestPath(), "Car.pr^ototype.a = 5;", true);
    }

    public void testCorrectPrototype_2() throws Exception {
        checkOccurrences(getTestPath(), "Car.prototype^.b = 8;", true);
    }

    public void testCorrectPrototype_3() throws Exception {
        checkOccurrences(getTestPath(), "Pislik.pro^totype.human = false;", true);
    }

    public void testCorrectPrototype_4() throws Exception {
        checkOccurrences(getTestPath(), "Hejlik.^prototype.human = false;", true);
    }

    public void testCorrectPrototype_5() throws Exception {
        checkOccurrences(getTestPath(), "Pislik.prototype.hum^an = false;", true);
    }

    public void testIssue217770_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue217770.js", "t.r^un();", true);
    }
    
    public void testIssue176581_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue176581.js", "    someElement.onfocus = fo^o;", true);
    }
    
    public void testIssue218070_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue218070_01.js", "Martin^Fousek.E;", true);
    }
    
    public void testIssue218070_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue218070_01.js", "MartinFousek.E^;", true);
    }
    
    public void testIssue218090_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue218090.js", "        text : pro^m,", true);
    }
    
    public void testIssue218261() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue218261.js", "var a = new Num^ber();", true);
    }

    public void testIssue218090_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue218090.js", "    var ag^e = 10;", true);
    }
    
    public void testIssue218090_03() throws Exception {
        checkOccurrences("testfiles/coloring/issue218090.js", "        period: peri^od", true);
    }

    public void testIssue218090_04() throws Exception {
        checkOccurrences("testfiles/coloring/issue218090.js", "        mon_yr: mo^n_yr,", true);
    }

    private String getTestFolderPath() {
        return "testfiles/markoccurences/" + getTestName();//NOI18N
    }

    public void testIssue218231_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue218231.js", "    return displa^yname;", true);
    }

    public void testIssue218231_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue218231.js", "var stylizeDisplayName = function(display^name, column, record) {", true);
    }

    public void testIssue137317_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue137317.js", "        u^rl: url", true);
    }

    public void testIssue137317_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue137317.js", "        url: u^rl", true);
    }

    public void testIssue156832() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue156832.js", "CSSClass.remove = function(p^aram, c)", true);
    }

    public void testIssue198431() throws Exception {
        checkOccurrences("testfiles/coloring/issue198431.js", "    this.doitPublic = do^it;", true);
    }

    public void testIssue218652_01() throws Exception {
        checkOccurrences("testfiles/model/getterSettterInObjectLiteral.js", "    set years(count){this.old = coun^t + 1;},", true);
    }

    public void testIssue218652_02() throws Exception {
        checkOccurrences("testfiles/model/getterSettterInObjectLiteral.js", "    set c(x^) {this.a = x / 2;}", true);
    }

    public void testIssue218561_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue218561.js", "        test: function(pa^r1) {", true);
    }

    public void testIssue218561_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue218561.js", "                par1: pa^r1 // par1 after : is marked green as member variable", true);
    }

    public void testIssue219067() throws Exception {
        checkOccurrences("testfiles/coloring/issue219027.html", "                        product = generate^Product(element);", true);
    }

    public void testIssue219634_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue219634.js", "    var mon^_yr = getInputValue(document.form1.mon_yr),", true);
    }

    public void testIssue219634_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue219634.js", "    if (!disallowBlank(document.form1.mo^n_yr, 'Pls. select Month /Year.'))", true);
    }

    public void testIssue219634_03() throws Exception {
        checkOccurrences("testfiles/coloring/issue219634.js", "    var mon_yr = getInputValue(document.for^m1.mon_yr),", true);
    }

    public void testIssue219634_04() throws Exception {
        checkOccurrences("testfiles/coloring/issue219634.js", "    $.getJSON('json_txt.php', {mon^_yr: mon_yr, period: period},", true);
    }

    public void testIssue220102() throws Exception {
        checkOccurrences("testfiles/coloring/issue220102.js", "        role: da^ta.role,", true);
    }

    public void testIssue218525_01() throws Exception {
        checkOccurrences("testfiles/completion/general/issue218525.html", "<li style=\"cursor: pointer\" onclick=\"operator.r^emoveMe(this);\">Remove me (breakpoint on node removal + breakpoint on nonDOM line)</li>", true);
    }

    public void testIssue218525_02() throws Exception {
        checkOccurrences("testfiles/completion/general/issue218525.html", "<li style=\"cursor: pointer\" onclick=\"ope^rator.removeMe(this);\">Remove me (breakpoint on node removal + breakpoint on nonDOM line)</li>", true);
    }

    public void testIssue217155_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue217155.js", "pe.h^i();", true);
    }

    public void testIssue217155_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue217155.js", "p^e.hi();", true);
    }

    public void testIssue220891() throws Exception {
        checkOccurrences("testfiles/coloring/issue220891.js", "        hiddenCom^ponents = false;", true);
    }

    public void testIssue221228_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "    var ms^g = \"private\"; // private variable", true);
    }
    
    public void testIssue221228_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "        formatter.println(m^sg); // uses private var", true);
    }
    
    public void testIssue221228_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "    function h^i() { // private function", true);
    }
    
    public void testIssue221228_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "        h^i();           // uses private function", true);
    }
    
    public void testIssue221228_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "Greetings.prototype.say^Ahoj = function () {", true);
    }
    
    public void testIssue221228_06() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "a221228.h^i();                     // the function is not accessible here", true);
    }
    
    public void testIssue221228_07() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "a221228.poz^drav();                // rename hi here", true);
    }

    public void testIssue221228_08() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "a221228.m^sg = \"Hi public\";        // creates new property of object a/", true);
    }

    public void testIssue221228_09() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "formatter.println(a221228.ms^g);", true);
    }
    
    public void testIssue221228_10() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "a221228.sayA^hoj();", true);
    }
    
    public void testIssue221228_11() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "formatter.println(b221228.ms^g);", true);
    }
    
    public void testIssue221228_12() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "b221228.ms^g = \"from b\";           // create new property of object b", true);
    }
    
    public void testIssue221228_13() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "b221228.h^i();", true);
    }
    
    public void testIssue221228_14() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "b221228.sayA^hoj();", true);
    }
    
    public void testIssue221228_15() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "b221228.pozdr^av();", true);
    }
    
    public void testIssue222250_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222250.js", "       this.x^hr = xhr;", true);
    }

    public void testIssue222250_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222250.js", "       this.xhr = x^hr;", true);
    }
    
    public void testIssue222373_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222373.js", "c.test.na^me = \"c\";", true);
    }
    
    public void testIssue222373_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222373.js", "c.te^st.name = \"c\";", true);
    }
    
    public void testIssue222373_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222373.js", "a.test.na^me = \"B\";", true);
    }
    
    public void testIssue222373_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222373.js", "a.te^st.name = \"B\";", true);
    }
    
    public void testIssue222373_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222373.js", "a^.test.name = \"B\";", true);
    }

    public void testIssue222373_06() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222373.js", "    nam^e : \"A\"", true);
    }
    
    public void testIssue222373_07() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222373.js", "  this.te^st = {", true);
    }
    
    public void testIssue222373_08() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222373.js", "function Per^son(){", true);
    }
    
    public void testIssue222507_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222507.js", "this._addPreset^Button = document.getElementById('addPreset');", true);
    }
    
    public void testIssue222507_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222507.js", "NetBeans_PresetCustomizer._addPresetB^utton = null;", true);
    }
    
    public void testIssue222698_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222698.js", "    data: js^on,", true);
    }
    
    public void testIssue222767_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222767.js", "        var js^on = \"data=\" + angular.toJson($scope.servos);", true);
    }

    public void testIssue222767_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222767.js", "            data: j^son,", true);
    }
    
    public void testIssue222498_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222498.js", "    return this.query({parent: this.getIdentity(obje^ct)});", true);
    }
  
    public void testIssue218191_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue218191.js", "    var RE^GEXP = /[+]?\\d{1,20}$/; // REGEXP marked as unused", true);
    }

    public void testIssue218191_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue218191.js", "            alert(REGE^XP.test(value));", true);
    }
    
    public void testIssue218191_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue218191.js", "            alert(REGEXP.te^st(value));", true);
    }

    public void testIssue218191_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue218191.js", "            alert(REGEXP.test(val^ue));", true);
    }
    
    public void testIssue218191_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue218191.js", "            switch (field^Type) {", true);
    }
    
    public void testIssue218136_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue218136.js", "p1.set^Static(100);", true);
    }

    public void testIssue218136_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue218136.js", "    Player.prototype.setS^tatic = function(v){ static_int = v; };", true);
    }
    
    public void testIssue218136_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue218136.js", "var stat^ic_int = 0;", true);
    }
    
    public void testIssue218041_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue218041.js", "    return b^ar;", true);
    }
    
    public void testIssue218041_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue218041.js", "ba^r = 1;", true);
    }
    
    public void testIssue217935_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue217935.js", " * @param {Dat^e} what", true);
    }
    
    public void testIssue217935_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue217935.js", " * @param {Dat^es} what", true);
    }

    public void testIssue217935_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue217935.js", " * @returns {Da^tes} description", true);
    }
    
    public void testIssue217935_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue217935.js", " * @param {Dates} wh^at", true);
    }
    
    public void testIssue222904_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue217935.js", "   what.mart^in();", true);
    }
    
    public void testIssue217086_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue217086.js", "        this.clo^thing=\"tinfoil\";", true);
    }
    
    public void testIssue217086_02() throws Exception {
        checkOccurrences("testfiles/model/person.js", "	gk.clot^hing=\"Pimp Outfit\";                    //clothing is a public variable that can be updated to any funky value ", true);
    }
    
    public void testIssue223074_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue223074.js", "        a.url = cont^ainer.name;", true);
    }

    public void testIssue223074_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue223074.js", "        a.url = container.na^me;", true);
    }
    
    public void testIssue223465() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue223465.js", "var so^me = {", true);
    }
   
    public void testIssue223699_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue223699.js","        this[bug].init( a, b^ug, this );", true);
    }

    public void testIssue223699_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue223699.js","        this[bug].init( a^, bug, this );", true);
    }
    
    public void testIssue223823_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue223823.js","var watch = function(scope, attr, name, defau^ltVal) {", true);
    }
    
    public void testIssue223823_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue223823.js","            scope[name] = v^al;", true);
    }
    
    public void testIssue223891_01() throws Exception {
        checkOccurrences("testfiles/structure/issue223891/issue223891.js"," * @param {Date} a^a", true);
    }
     
    public void testIssue223891_02() throws Exception {
        checkOccurrences("testfiles/structure/issue223891/issue223891.js"," * @returns {Utils22^3891}", true);
    }
    
    public void testIssue223891_03() throws Exception {
        checkOccurrences("testfiles/structure/issue223891/issue223891.js","    this.t^est = aa.getDay();", true);
    }
    
    public void testIssue217938_01() throws Exception {
        checkOccurrences("testfiles/structure/issue217938.js","    this.par1 = pa^r1; // this one is not in navigator", true);
    }
    
    public void testIssue217938_02() throws Exception {
        checkOccurrences("testfiles/structure/issue217938.js","    this.pa^r1 = par1; // this one is not in navigator", true);
    }
    
    public void testIssue210136() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue210136.js","va^lue = 1;", true);
    }

    public void testIssue223952() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue223952.js","function UserToConnectio^ns(ahoj) {", true);
    }
    
    public void testIssue224215_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue224215.js","    var A^ = 1;", true);
    }
    
    public void testIssue224215_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue224215.js","    window.A = A^;", true);
    }
    
    public void testIssue224215_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue224215.js","    window.A^ = A;", true);
    }
    
    public void testIssue224462_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue224462.js","    formatter.print(err224^462);", true);
    }
    
    public void testIssue224462_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue224462.js","        formatter.println(err22^4462);", true);
    }
    
    public void testIssue224462_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue224462.js","        formatter.say(err2^24462);", true);
    }
    
    public void testIssue224462_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue224462.js","        formatter.println(prom22^4462);", true);
    }
    
    public void testIssue224462_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue224462.js","        formatter.println(prom^224462_1);", true);
    }
    
    public void testIssue224520() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue224520.js","        var te^am = data[i+offset]; // mark occurrences or rename|refactor team", true);
    }
    
    public void testIssue225399_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue225399.js","        vers^ion = (version.length > 0) ? {\"version\": version} : {petr : 10};", true);
    }
    
    public void testIssue225399_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue225399.js","        version.ver^sion = 10;", true);
    }
    
    public void testIssue228634_01() throws Exception {
        checkOccurrences("testfiles/completion/issue228634/issue228634.js","    var ret2 = co^n.x();", true);
    }
    
    public void testIssue228634_02() throws Exception {
        checkOccurrences("testfiles/completion/issue228634/issue228634.js","    var ret2 = con.x^();", true);
    }
    
    public void testIssue228634_03() throws Exception {
        checkOccurrences("testfiles/completion/issue228634/issue228634.js","    return re^t;", true);
    }

    public void testIssue229717_01() throws Exception {
        checkOccurrences("testfiles/model/issue229717.js","test.typ^es;", true);
    }
    
    public void testIssue229363_01() throws Exception {
        checkOccurrences("testfiles/completion/general/issue218689.html","var A^ = function() {", true);
    }
    
    public void testIssue229363_02() throws Exception {
        checkOccurrences("testfiles/completion/general/issue218689.html","var b = new B^();", true);
    }
    
    public void testIssue231530_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231530.js","                return this.f^1(); // ctr+click does not work on f1", true);
    }
    
    public void testIssue231530_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231530.js","expect(obj.f^2()).toEqual('f1'); // here it works", true);
    }
    
    public void testIssue231531_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231531.js","expect(cat.ro^ar()).toEqual('rrrr'); // ctr+click does not work on roar", true);
    }
    
    public void testIssue231531_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231531.js","var cat = new C^at();", true);
    }
    
    public void testIssue231531_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231531.js","Cat.prototype = new An^imal();", true);
    }
    
    public void testIssue231533_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231533.js","expect(animal.ro^ar()).toEqual('rrrr'); // ctr+click does not work on roar", true);
    }
    
    public void testIssue231533_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231533.js","var animal = new bea^sties.Animal();", true);
    }
    
    public void testIssue231533_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231533.js","var animal = new beasties.An^imal();", true);
    }
    
    public void testIssue231782_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231782.js","console.log(Test.options.deb^ug);", true);
    }
    
    public void testIssue231782_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231782.js","console.log(Test.opt^ions.debug);", true);
    }

    public void testIssue231782_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231782.js","console.log(T^est.options.debug);", true);
    }
    
    public void testIssue231782_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231782.js","Test.debu^g();", true);
    }

    public void testIssue231913() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231913.js","return recurs^ion();", true);
    }
    
    public void testIssue232570_01() throws Exception {
        checkOccurrences("testfiles/completion/issue232570.js", "Test.modules.moduleA.na^me;", true);
    }
    
    public void testIssue232570_02() throws Exception {
        checkOccurrences("testfiles/completion/issue232570.js", "Test.modules.modul^eA.name;", true);
    }
    
    public void testIssue232570_03() throws Exception {
        checkOccurrences("testfiles/completion/issue232570.js", "Test.modul^es.moduleA.name;", true);
    }
    
    public void testIssue232570_04() throws Exception {
        checkOccurrences("testfiles/completion/issue232570.js", "Te^st.modules.moduleA.name;", true);
    }
    
    private String getTestPath() {
        return getTestFolderPath() + "/" + getTestName() + ".js";//NOI18N
    }

    public void testIssue232595_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232595.js","   for (var loc2 = 30, loc3 = {}; lo^c2 < 100; loc2++) {", true);
    }
    
    public void testIssue232595_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232595.js","      myl^oc1 += loc2;", true);
    }

    public void testIssue232920_01() throws Exception {
        checkOccurrences("testfiles/structure/issue232920.js","var aaa = new MyCtx.A^uto();", true);
    }
    
    public void testIssue232920_02() throws Exception {
        checkOccurrences("testfiles/structure/issue232920.js","console.log(aaa.descri^ption.name);", true);
    }
    
    public void testIssue232920_03() throws Exception {
        checkOccurrences("testfiles/structure/issue232920.js","console.log(aaa.description.na^me);", true);
    }
    
    public void testIssue232993_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue215757.js","   this.document = win^dow.document;", true);
    }
    
    public void testIssue232993_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue215757.js","   this.docume^nt = window.document;", true);
    }
    
    public void testIssue232993_03() throws Exception {
        checkOccurrences("testfiles/coloring/issue215757.js","this.browser = brow^ser.browser;", true);
    }
    
    public void testIssue232993_04() throws Exception {
        checkOccurrences("testfiles/coloring/issue215757.js","this.browser = browser.brow^ser;", true);
    }
    
    public void testIssue217769_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue217769.js","a.i^n();", true);
    }

    public void testIssue233236_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233236.js","var firstName = firs^tName;", true);
    }
    
    public void testIssue233236_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233236.js","var firstN^ame = firstName;", true);
    }
    
    public void testIssue233578_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233578.js","this.name = n^ame;", true);
    }
    
    public void testIssue233578_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233578.js","this.nam^e = name;", true);
    }
    
    public void testIssue233578_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233578.js","this.age = ag^e;", true);
    }
    
    public void testIssue233578_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233578.js","this.ag^e = age;", true);
    }
    
    public void testIssue233738_01() throws Exception {
        checkOccurrences("testfiles/structure/issue233738.js","var myhelp = window['somep^rom'];", true);
    }
    
    public void testIssue233738_02() throws Exception {
        checkOccurrences("testfiles/structure/issue233738.js","var myhelp = win^dow['someprom'];", true);
    }
    
    public void testIssue233787_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233787.js", "ondra.address.str^eet = \"Piseckeho\";", true); 
    }
    
    public void testIssue233787_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233787.js", "ondra.addre^ss.street = \"Piseckeho\";", true); 
    }
    
    public void testIssue233787_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233787.js", "console.log(addr^ess.street);", true); 
    }
    
    public void testIssue233787_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233787.js", "console.log(address.stree^t);", true); 
    }
    
    public void testIssue233720_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233720.js", "var indexOfBeta = getIndexOfLatestBe^ta();", true); 
    }
    
    public void testIssue233720_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233720.js", "var indexOfB^eta = getIndexOfLatestBeta();", true); 
    }

    public void testIssue233720_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233720.js", "return this.myMet^hod2();", true); 
    }
    
    public void testIssue222964_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222964/issue222964.js", "console.log(window.store.address.stree^t);", true); 
    }
    
    public void testIssue222964_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222964/issue222964.js", "console.log(window.store.addre^ss.street);", true); 
    }
    
    public void testIssue222964_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222964/issue222964.js", "console.log(window.st^ore.address.street);", true); 
    }
    
    public void testIssue222964_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222964/issue222964.js", "console.log(wind^ow.store.address.street);", true); 
    }
    
    public void testIssue222964_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222964/issue222964.js", "popul^ate: function() {", true); 
    }
    
    public void testIssue234392_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue234392.js", "window.console.lo^g(\"text\");", true); 
    }
    
    public void testIssue234392_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue234392.js", "window.co^nsole.log(\"text\");", true); 
    }
    
    public void testIssue234512_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue234512.js", "'Last-Modified': mtim^e", true); 
    }
    
    public void testIssue234512_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue234512.js", "'Content-Type': contentT^ype,", true); 
    }
    
    public void testIssue223057_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue223057.js", "a.url = container.nam^e;", true); 
    }
    
    public void testIssue223057_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue223057.js", "a.url = contain^er.name;", true); 
    }
    
    public void testIssue238693_01() throws Exception {
        checkOccurrences("testfiles/model/issue238693.js", "this.lo^gger.printLevel = 1; // here is this purple as variable", true); 
    }
    
    public void testIssue239967_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue239967.js", "var po^ol = []; // purple", true); 
    }
    
    public void testIssue239967_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue239967.js", "var poo^l2 = [];", true); 
    }
    
    public void testIssue239967_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue239967.js", "po^ol[i] = i;", true); 
    }
    
    public void testIssue239967_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue239967.js", "po^ol2[i] = i;", true); 
    }
    
    public void testIssue241171_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue241171.js", "this.pager.on('empty', this.welco^me, this);", true); 
    }
    
    public void testIssue241171_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue241171.js", "this.pag^er.on('empty', this.welcome, this);", true); 
    }
    
    public void testIssue241171_03() throws Exception {
        checkOccurrences("testfiles/model/person.js", "Person.prototype.amputate = function(){ this.le^gs-- } ", true); 
    }
    
    public void testIssue238499_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue238499.js", "return new_l^ink;", true);
    }
    
    public void testIssue230974_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue230974.js", "WidgetManager.prototype.nextP2 = data[0].p2^limit; // place cursor inside p2limit", true); 
    }
    
    public void testIssue230974_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue230974.js", "WidgetManager.prototype.nextP2 = da^ta[0].p2limit; // place cursor inside p2limit", true); 
    }
    
    public void testIssue242408_01() throws Exception {
        checkOccurrences("testfiles/model/issue242408.js", "function te^st(name) {", true);
    }
    
    public void testIssue242408_02() throws Exception {
        checkOccurrences("testfiles/model/issue242408.js", "var f^oo = {};", true);
    }
    
    public void testIssue242408_03() throws Exception {
        checkOccurrences("testfiles/model/issue242408.js", "foo.b^ar();", true);
    }
    
    public void testIssue242408_04() throws Exception {
        checkOccurrences("testfiles/model/issue242408.js", "Cub^e.prototype.foo = test;", true);
    }
    
    public void testIssue242408_05() throws Exception {
        checkOccurrences("testfiles/model/issue242408.js", "Cube.prototype.f^oo = test;", true);
    }
    
    public void testIssue242454_01() throws Exception {
        checkOccurrences("testfiles/model/issue242454.js", "this.publicInnerFunc = PublicAndPriv^ateUsageFunc;", true);
    }
    
    public void testIssue242421_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue242421.js", "var myF = function MyLib_Function (pa^th, ref, pfx, options) {", true); 
    }

    public void testIssue242421_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue242421.js", "var myF = function MyLib_Function (path, r^ef, pfx, options) {", true); 
    }

    public void testIssue242421_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue242421.js", "var myF = function MyLib_Function (path, ref, pf^x, options) {", true); 
    }

    public void testIssue242421_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue242421.js", "var myF = function MyLib_Function (path, ref, pfx, opt^ions) {", true); 
    }

    public void testIssue242421_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue242421.js", "var v^ = expand(path, ref, pfx, options);", true); 
    }
    
    public void testIssue243449_01() throws Exception {
        checkOccurrences("testfiles/model/issue243449.js", "p.x = f^3;", true);
    }

    public void testIssue243449_02() throws Exception {
        checkOccurrences("testfiles/model/issue243449.js", "p.x = f^2;", true);
    }
    
    public void testIssue243449_03() throws Exception {
        checkOccurrences("testfiles/model/issue243449.js", "p.x = f^1;", true);
    }
    
    public void testIssue244964_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue244964.js", "b^ar = {};", true); 
    }
    
    public void testIssue244964_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue244964.js", "f^oo = {", true); 
    }
    
    public void testIssue244973A_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue244973A.js", "this._implement^ation = null;", true); 
    }
    
    public void testIssue244973A_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue244973A.js", "throw Pages^Manager.NO_IMPLEMENTATION_ERROR;", true); 
    }
    
    public void testIssue244973B_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue244973B.js", "PagesManager.prototype._implem^entation = null;", true); 
    }
    
    public void testIssue244973B_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue244973B.js", "throw Pages^Manager.NO_IMPLEMENTATION_ERROR;", true); 
    }
    
    public void testIssue244989_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue244989.js", "return fo^o().length;", true); 
    }
    
    public void testIssue244861_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue244861.js", "var sel^f = this; // in MyClass", true); 
    }
    
    public void testIssue244861_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue244861.js", "this.myFi^eld = \"something else\";", true); 
    }
    
    public void testIssue244861_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue244861.js", "sel^f.myField.doSomething(); // click on myField", true); 
    }
    
    public void testIssue244861_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue244861.js", "self.myField.do^Again(); // in myMethod", true); 
    }
    
    public void testIssue244861_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue244861.js", "this.my^Field = \"foo\"; // click on myField", true); 
    }
    
    public void testIssue244344_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue244344.js", "SeLiteMisc.isInstance= function isPrdca( ob^ject, classes, className, message ) {", true); 
    }
    
    public void testIssue244344_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue244344.js", "SeLiteMisc.isInstance= function isPrdca( object, cla^sses, className, message ) {", true); 
    }
    
    public void testIssue244344_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue244344.js", "SeLiteMisc.isInstance= function isPrdca( object, classes, class^Name, message ) {", true); 
    }
    
    public void testIssue244344_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue244344.js", "SeLiteMisc.isInstance= function isPrdca( object, classes, className, messa^ge ) {", true); 
    }
    
    public void testIssue245445_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue245445.js", "r = r^ + array[i];", true); 
    }
    
    public void testIssue245445_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue245445.js", "r = r + ar^ray[i];", true); 
    }
    
    public void testIssue245445_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue245445.js", "r = r + array[i^];", true); 
    }
    
    public void testIssue245445_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue245445.js", "this.myMeth^od = function MyLib_myMethod(array) {", true); 
    }
    
    public void testIssue245445_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue245445.js", "this.myMethod = function MyLib_myMet^hod(array) {", true); 
    }
    
    public void testIssue245445_06() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue245445.js", "var myLib = new My^Lib();", true); 
    }
    
    public void testIssue245445_07() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue245445.js", "var myL^ib = new MyLib();", true); 
    }
    
    public void testIssue190645_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue190645.js", "config.defaults.treasure.g^old = 1;", true); 
    }
    
    public void testIssue190645_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue190645.js", "config.defaults.trea^sure.gold = 1;", true); 
    }
    
    public void testIssue190645_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue190645.js", "config.def^aults.treasure.gold = 1;", true); 
    }
    
    public void testIssue190645_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue190645.js", "conf^ig.defaults.treasure.gold = 1;", true); 
    }
    
    public void testIssue248960_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue248960.js", "var gotDepartment = gotDepartm^ent;", true); 
    }
    
    public void testIssue249006() throws Exception {
        checkOccurrences("testfiles/coloring/issue249006.js", "LoginSe^rvice.test = \"google\";", true);
    }
    
    public void testIssue249119_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue249119.js", "^f();", true);
    }
    
    public void testIssue249119_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue249119.js", "f^2();", true);
    }
    
    public void testIssue249119_03() throws Exception {
        checkOccurrences("testfiles/coloring/issue249119.js", "var  a^ = param.a || 1;", true);
    }
    
    public void testIssue249119_04() throws Exception {
        checkOccurrences("testfiles/coloring/issue249119.js", "var  a = par^am.a || 1;", true);
    }
    
    public void testIssue249119_05() throws Exception {
        checkOccurrences("testfiles/coloring/issue249119.js", "^g();", true);
    }
    
    public void testIssue249119_06() throws Exception {
        checkOccurrences("testfiles/coloring/issue249119.js", "var  a^2 = param.a || 1;", true);
    }
    
    public void testIssue249119_07() throws Exception {
        checkOccurrences("testfiles/coloring/issue249119.js", "var  a2 = par^am.a || 1;", true);
    }
    
    public void testIssue249119_08() throws Exception {
        checkOccurrences("testfiles/coloring/issue249119.js", "^g2();", true);
    }
    
    public void testIssue250099_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue250099.js", "* @param {MyC^ontext.CarDescription} carDescription", true); 
    }
    
    public void testIssue250099_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue250099.js", "* @param {MyContext.CarDesc^ription} carDescription", true); 
    }
    
    public void testIssue250099_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue250099.js", "* @param {Na^me} name", true); 
    }
    
    public void testIssue250112_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue250112.js", "* @typedef {Object} MyCont^ext.Address description", true); 
    }
    
    public void testIssue250112_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue250112.js", "* @typedef {Object} MyContext.Add^ress description", true); 
    }
     
    public void testIssue250110_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue250110.js", "* @typedef {Object} MyCont^ext~Address description", true); 
    }
    
    public void testIssue250110_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue250110.js", "* @typedef {Object} MyContext~Add^ress description", true); 
    }
    
    public void testIssue250121_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue250121.js", "* @param {MyCon^text~Address} addressDescription", true); 
    }
    
    public void testIssue250121_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue250121.js", "* @param {MyContext~Add^ress} addressDescription", true); 
    }
    
    public void testIssue250121_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue250121.js", "* @param {MyContext~Address} add^ressDescription", true); 
    }
    
    public void testIssue249619_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue249619.js", "console.log(er^r.stack);", true); 
    }
    
    public void testCallBackDeclaration_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/callbackDeclaration1.js", "* @param {Requ^ester~requestCallback} cb - The callback that handles the response.", true); 
    }
    
    public void testCallBackDeclaration_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/callbackDeclaration1.js", "* @param {Requester~reque^stCallback} cb - The callback that handles the response.", true); 
    }
    
    public void testCallBackDeclaration_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/callbackDeclaration1.js", "* @param {Requester~requestCallback} c^b - The callback that handles the response.", true); 
    }
    
    public void testCallBackDeclaration_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/callbackDeclaration2.js", " * @param {requ^estCallback} cb - The callback that handles the response.", true); 
    }
    
    public void testCallBackDeclaration_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/callbackDeclaration2.js", " * @param {requestCallback} c^b - The callback that handles the response.", true); 
    }
    
    public void testIssue251794_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue251794.js", "function Mod^el () {};", true); 
    }
    
    public void testIssue251794_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue251794.js", "Model.comp^ile = function compile () {", true); 
    }
    
    public void testIssue251794_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue251794.js", "Model.compile = function comp^ile () {", true); 
    }
    
    public void testIssue251794_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue251794.js", "model.__pro^to__ = Model;", true); 
    }
    
    public void testIssue251794_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue251794.js", "mod^el.__proto__ = Model;", true); 
    }
    
    public void testIssue251853_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue251853.js", "return new MyLib.Ob^jB(); ", true); 
    }
    
    public void testIssue251853_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue251853.js", "return new My^Lib.ObjB(); ", true); 
    }
    
    public void testIssue251824_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue251824.js", "var b1 = MyLib.obj^B();", true); 
    }
    
    public void testIssue251823_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue251823.js", "MyLib.Obj^B = function (arg) {", true); 
    }
    
    public void testIssue251892_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue251823.js", "MyL^ib.ObjB = function (arg) {", true); 
    }
    
    public void testIssue251883_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue251823.js", "return new MyLib.O^bjB(); ", true); 
    }
    
    public void testIssue250434_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue250434.js", "function TripDayFormCtrl($scope, TripMo^del, TripHandler) { // lineA", true); 
    }
    
    public void testIssue251911_01() throws Exception {
        checkOccurrences("testfiles/model/issue251911.js", "var _my^Lib = this;", true); 
    }
    
    public void testIssue251911_02() throws Exception {
        checkOccurrences("testfiles/model/issue251911.js", "var TR^UE   = !0,", true); 
    }
    
    public void testIssue251911_03() throws Exception {
        checkOccurrences("testfiles/model/issue251911.js", "FAL^SE  = !1;", true); 
    }
    
    public void testIssue251911_04() throws Exception {
        checkOccurrences("testfiles/model/issue251911.js", "function _myLi^b_in(item, container) {", true); 
    }
    
    public void testIssue251911_05() throws Exception {
        checkOccurrences("testfiles/model/issue251911.js", "function _myLib_in(it^em, container) {", true); 
    }
    
    public void testIssue251911_06() throws Exception {
        checkOccurrences("testfiles/model/issue251911.js", "function _myLib_in(item, con^tainer) {", true); 
    }
    
    public void testIssue251911_07() throws Exception {
        checkOccurrences("testfiles/model/issue251911.js", "for( var k^ey in container ) {", true); 
    }
    
    public void testIssue242454B_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue242454B.js", "this.y1 = f^1;", true); 
    }
    
    public void testIssue242454B_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue242454B.js", "this.y2 = f^2;", true); 
    }
    
    public void testIssue251984_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue251984.js", "var myL^ib = new function MyLib() {", true); 
    }
    
    public void testIssue251984_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue251984.js", "var myLib = new function My^Lib() {", true); 
    }
    
    public void testIssue251984_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue251984.js", "function f^1(f1arg) {", true); 
    }
    
    public void testIssue251984_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue251984.js", "function f^2(f2arg) {", true); 
    }
    
    public void testIssue252022_01() throws Exception {
        checkOccurrences("testfiles/hints/issue252022.js", "function fn^2() {", true); 
    }
    
    public void testIssue252022_02() throws Exception {
        checkOccurrences("testfiles/hints/issue252022.js", "fn^2().toLowerCase();  // test", true); 
    }
    
    public void testIssue252022_03() throws Exception {
        checkOccurrences("testfiles/hints/issue252022.js", "fn2().toLower^Case();  // test", true); 
    }
    
    public void testIssue249487_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue249487.js", "console.log(this.iA^rg);", true); 
    }
    
    public void testIssue249487_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue249487.js", "console.log(test.iA^rg);", true); 
    }
    
    public void testIssue249487_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue249487.js", "console.log(te^st.iArg);", true); 
    }
    
    public void testIssue249487_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue249487.js", "this.myEv^ent = new MyApi.Event(myEventArgs);", true); 
    }
    
    public void testIssue249487_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue249487.js", "this.myEvent = new MyA^pi.Event(myEventArgs);", true); 
    }
    
    public void testIssue249487_06() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue249487.js", "this.myEvent = new MyApi.Ev^ent(myEventArgs);", true); 
    }
    
    public void testIssue249487_07() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue249487.js", "this.myEvent = new MyApi.Event(myEv^entArgs);", true); 
    }
    
    public void testIssue249487_08() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue249487.js", "function myEventArgs(iAr^g) {", true); 
    }
    
    public void testIssue249487_09() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue249487.js", "this.iA^rg = iArg;", true); 
    }
    
    public void testIssue252375_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue252375.js", "this.xxx = x^xx;", true); 
    }
    
    public void testIssue252375_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue252375.js", "this.xx^x = xxx;", true); 
    }
    
    public void testIssue252375_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue252375.js", "fn^1: function (data) {", true); 
    }
    
    public void testIssue252375_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue252375.js", "fn1: function (d^ata) {", true); 
    }
    
    public void testIssue252135_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue252135.js", "var a^a = function bb () {", true); 
    }
    
    public void testIssue252135_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue252135.js", "var aa = function b^b () {", true); 
    }
    
    public void testIssue226977_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue226977_01.js", "var us^er = {};", true); 
    }
    
    public void testIssue226977_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue226977_02.js", "var se^lf = this;", true); 
    }
    
    public void testIssue226977_03() throws Exception {
        checkOccurrences("testfiles/coloring/issue226977_02.js", "self.onL^oading();", true); 
    }
    
    public void testIssue252469_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue252469.js", "_eve^nts[evtId] = handlers = [];", true);
    }
    
    public void testIssue252469_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue252469.js", "_events[ev^tId] = handlers = [];", true);
    }
    
    public void testIssue252469_03() throws Exception {
        checkOccurrences("testfiles/coloring/issue252469.js", "_events[evtId] = hand^lers = [];", true);
    }
    
    public void testIssue252019_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue252019.js", "var f^n = function FnName(){", true);
    }
    
    public void testIssue250376_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue250376.js", "* @property  {SomeW^idget} yet", true);
    }
    
    public void testIssue250376_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue250376.js", "* @property  {Anothe^rOne} [label]", true);
    }
    
    public void testIssue252655_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue252655.js", "(wi^ndow[varName].q = window[varName].q || []);", true);
    }
    
    public void testIssue252655_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue252655.js", "(window[varN^ame].q = window[varName].q || []);", true);
    }
    
    public void testIssue252655_03() throws Exception {
        checkOccurrences("testfiles/coloring/issue252655.js", "(window[varName].q^ = window[varName].q || []);", true);
    }
    
    public void testIssue252656_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue252656.js", "u^ga.q = [];", true);
    }
    
    public void testIssue252656_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue252656.js", "uga.q^ = [];", true);
    }
    
    public void testIssue252656_03() throws Exception {
        checkOccurrences("testfiles/coloring/issue252656.js", "wi^ndow[ugaVarName] = uga;", true);
    }
    
    public void testIssue252656_04() throws Exception {
        checkOccurrences("testfiles/coloring/issue252656.js", "window[ugaV^arName] = uga;", true);
    }
    
    public void testIssue252656_05() throws Exception {
        checkOccurrences("testfiles/coloring/issue252656.js", "window[ugaVarName] = u^ga;", true);
    }
    
    public void testIssue243566_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue243566.js", "this.x = x^;", true);
    }
    
    public void testIssue243566_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue243566.js", "this.x^ = x;", true);
    }
    
    public void testIssue252873_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/jsdoc/issue252873.js", " * @param {*} var^iable", true);
    }
    
    public void testIssue252873_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/jsdoc/issue252873.js", "return !Breeze.isUndefined(variable) && var^iable !== null;", true);
    }
    
    public void testIssue237914_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue237914.js", "j^ob.a = 3;", true);
    }
    
    public void testIssue237914_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue237914.js", "return jo^b;", true);
    }
    
    public void testIssue237421_01() throws Exception {
        checkOccurrences("testfiles/completion/general/issue237421.js", "var foo2 = this.getT^est().m;", true);
    }
    
    public void testIssue253129_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue253129.js", "var f^1 = f2 = function () {", true);
    }
    
    public void testIssue253129_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue253129.js", "var f1 = f^2 = function () {", true);
    }
    
    public void testIssue253129_03() throws Exception {
        checkOccurrences("testfiles/coloring/issue253129.js", "var f3, f4, f5 = f3 = f^4 = function (){", true);
    }
    
    //TODO this test is failing, but when the golden file is created, then it's created correctly. Probably problem with CSL testing
//    public void testIssue253129_04() throws Exception {
//        checkOccurrences("testfiles/coloring/issue253129.js", "f^3();", true);
//    }
    
    public void testIssue253736_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue253736.js", "var addre^ss = {baseUrl:'', id:0};", true);
    }
     
    public void testIssue253736_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue253736.js", "var address = {bas^eUrl:'', id:0};", true);
    }
    
    public void testIssue253736_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue253736.js", "var address = {baseUrl:'', i^d:0};", true);
    }
    
    public void testIssue253736_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue253736.js", "syste^mUri.replace(myPattern, function(a, protocol, server, id) {", true);
    }
    
    public void testIssue253736_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue253736.js", "systemUri.replace(myPat^tern, function(a, protocol, server, id) {", true);
    }
    
    public void testIssue253736_06() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue253736.js", "systemUri.replace(myPattern, function(a, prot^ocol, server, id) {", true);
    }
    
    public void testIssue253736_07() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue253736.js", "systemUri.replace(myPattern, function(a, protocol, se^rver, id) {", true);
    }
    
    public void testIssue253736_08() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue253736.js", "systemUri.replace(myPattern, function(a, protocol, server, i^d) {", true);
    }
    
    public void testIssue253129_05() throws Exception {
        checkOccurrences("testfiles/coloring/issue253129.js", "var f3, f4, f^5 = f3 = f4 = function (){", true);
    }
    
    public void testClass01_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class01.js", "var p^ = new Polygon (10,20);", true);
    }
    
    public void testClass01_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class01.js", "var p = new Pol^ygon (10,20);", true);
    }
    
    public void testClass01_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class01.js", "console.log(p.wid^th);", true);
    }
    
    public void testClass01_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class01.js", "console.log(p.hei^ght);", true);
    }
    
    public void testClass01_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class01.js", "this.height = hei^ght;", true);
    }
    
    public void testClass01_06() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class01.js", "const^ructor(height, width) {", true);
    }
    
    public void testClass02_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class02.js", "var p^ = new Polygon (10,20);", true);
    }
    
    public void testClass02_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class02.js", "var p = new Pol^ygon (10,20);", true);
    }
    
    public void testClass02_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class02.js", "console.log(p.wid^th);", true);
    }
    
    public void testClass02_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class02.js", "console.log(p.hei^ght);", true);
    }
    
    public void testClass02_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class02.js", "this.height = hei^ght;", true);
    }
    
    public void testClass02_06() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class02.js", "const^ructor(height, width) {", true);
    }
    
    public void testClass03_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class03.js", "var p^ = new Polygon (10, 20);", true);
    }
    
    public void testClass03_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class03.js", "var p = new Poly^gon (10, 20);", true);
    }
    
    public void testClass03_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class03.js", "console.log(p.wid^th);", true);
    }
    
    public void testClass03_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class03.js", "console.log(p.hei^ght);", true);
    }
    
    public void testClass03_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class03.js", "this.height = hei^ght;", true);
    }
    
    public void testClass03_06() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class03.js", "const^ructor(height, width) {", true);
    }
    
    public void testClass03_07() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class03.js", "var p = new Polygo^n2(1,2);", true);
    }
    
    public void testClass04_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class04.js", "const p1 = new Po^int(5, 5);", true);
    }
    
    public void testClass04_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class04.js", "const p^1 = new Point(5, 5);", true);
    }
    
    public void testClass04_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class04.js", "console.log(Point.distan^ce(p1, p2)); ", true);
    }
    
    public void testClass04_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class04.js", "const d^x = a.x - b.x;", true);
    }
    
    public void testClass04_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class04.js", "const dx = a^.x - b.x;", true);
    }
    
    public void testClass04_06() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class04.js", "const dx = a.x^ - b.x;", true);
    }
    
    public void testClass04_07() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class04.js", "const dy = a.y - b.y^;", true);
    }
    
    public void testClass04_08() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class04.js", "this.x^ = x;", true);
    }
    
    public void testClass04_09() throws Exception {
        checkOccurrences("testfiles/markoccurences/classes/class04.js", "this.x = x^;", true);
    }
    
    public void testFunctionDeclaration01_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/functionDeclaration/functionDeclaration01.js", "f^1(true);", true);
    }
    
    public void testFunctionDeclaration01_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/functionDeclaration/functionDeclaration01.js", "if (co^nt) {", true);
    }
    
    public void testFunctionDeclaration02_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/functionDeclaration/functionDeclaration02.js", "f^1(true);", true);
    }
    
    public void testFunctionDeclaration02_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/functionDeclaration/functionDeclaration02.js", "if (co^nt) {", true);
    }
    
    public void testFunctionDeclaration02_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/functionDeclaration/functionDeclaration02.js", "f^1(false); // inner f1 is called", true);
    }
    
    public void testFunctionDeclaration03_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/functionDeclaration/functionDeclaration03.js", "f^1(true);", true);
    }
    
    public void testFunctionDeclaration03_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/functionDeclaration/functionDeclaration03.js", "if (co^nt) {", true);
    }
    
    public void testFunctionDeclaration03_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/functionDeclaration/functionDeclaration03.js", "f^1(false); // inner f1 is called", true);
    }
    
    public void testFunctionDeclaration04_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/functionDeclaration/functionDeclaration04.js", "f^1(true); // outer f1 is called", true);
    }
    
    public void testFunctionDeclaration04_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/functionDeclaration/functionDeclaration04.js", "f^1(true); // inner f1 is called", true);
    }
    
    public void testFunctionDeclaration04_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/functionDeclaration/functionDeclaration04.js", "f^1(false);  // the f1 private name is called", true);
    }
    
    public void testFunctionDeclaration04_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/functionDeclaration/functionDeclaration04.js", "var f1 = function f1(c^ont) {", true);
    }
    
    public void testFunctionDeclaration04_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/functionDeclaration/functionDeclaration04.js", "function f1(c^ont) {", true);
    }
    
    public void testFunctionDeclaration05_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/functionDeclaration/functionDeclaration05.js", "f^1(true); // inner f1 is called", true);
    }
    
    public void testFunctionDeclaration05_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/functionDeclaration/functionDeclaration05.js", "function f^1(cont) {", true);
    }
    
    public void testFunctionDeclaration05_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/functionDeclaration/functionDeclaration05.js", "var f1 = function f^2(cont) {", true);
    }
    
    public void testFunctionDeclaration06_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/functionDeclaration/functionDeclaration06.js", "f^1(false);", true);
    }
    
    public void testFunctionDeclaration06_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/functionDeclaration/functionDeclaration06.js", "A.f^1(true);", true);
    }
    
    public void testFunctionDeclaration06_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/functionDeclaration/functionDeclaration06.js", "var b = A^;", true);
    }

    public void testGenerator01_01() throws Exception {
        checkOccurrences("testfiles/ecmascript6/generators/generator01.js", "var g = gen^01();", true);
    }
    
    public void testGenerator01_02() throws Exception {
        checkOccurrences("testfiles/ecmascript6/generators/generator01.js", "var g^ = gen01();", true);
    }
    
    public void testGenerator02_01() throws Exception {
        checkOccurrences("testfiles/ecmascript6/generators/generator02.js", "console.log(Ut^ils.values().next().value);", true);
    }
    
    public void testGenerator02_02() throws Exception {
        checkOccurrences("testfiles/ecmascript6/generators/generator02.js", "console.log(Utils.val^ues().next().value);", true);
    }
    
    public void testGenerator02_03() throws Exception {
        checkOccurrences("testfiles/ecmascript6/generators/generator02.js", "console.log(Utils.values().n^ext().value);", true);
    }
    
    public void testGenerator02_04() throws Exception {
        checkOccurrences("testfiles/ecmascript6/generators/generator02.js", "console.log(Utils.values().next().val^ue);", true);
    }
    
    public void testGenerator03_01() throws Exception {
        checkOccurrences("testfiles/ecmascript6/generators/generator03.js", "console.log(n.get^Name());", true);
    }
    
    public void testGenerator04_01() throws Exception {
        checkOccurrences("testfiles/ecmascript6/generators/generator04.js", "console.log(keyboard.ke^ys().next());", true);
    }
    
    public void testShorthandPropertyNames01() throws Exception {
        checkOccurrences("testfiles/ecmascript6/shorthands/shorthandPropertyNames.js", "console.log(c^.sayHello());  // Hello", true);
    }
    
    public void testShorthandPropertyNames02() throws Exception {
        checkOccurrences("testfiles/ecmascript6/shorthands/shorthandPropertyNames.js", "console.log(c.sa^yHello());  // Hello", true);
    }
    
    public void testShorthandPropertyNames03() throws Exception {
        checkOccurrences("testfiles/ecmascript6/shorthands/shorthandPropertyNames.js", "console.log(c.t^hird);       // 3th", true);
    }
    
    public void testShorthandPropertyNames04() throws Exception {
        checkOccurrences("testfiles/ecmascript6/shorthands/shorthandPropertyNames.js", "console.log(o.c.sec^ond);    // 2th", true);
    }
    
    public void testShorthandPropertyNames05() throws Exception {
        checkOccurrences("testfiles/ecmascript6/shorthands/shorthandPropertyNames.js", "console.log(o.b^);           // 42", true);
    }
    
    public void testShorthandMethodNames01() throws Exception {
        checkOccurrences("testfiles/ecmascript6/shorthands/shorthandMethodNames.js", "console.log(o.pro^perty);", true);
    }
    
    public void testShorthandMethodNames02() throws Exception {
        checkOccurrences("testfiles/ecmascript6/shorthands/shorthandMethodNames.js", "console.log(prop^erty);", true);
    }
    
    public void testShorthandMethodNames03() throws Exception {
        checkOccurrences("testfiles/ecmascript6/shorthands/shorthandMethodNames.js", "console.log(o.met^hod());", true);
    }
    
    public void testShorthandMethodNames04() throws Exception {
        checkOccurrences("testfiles/ecmascript6/shorthands/shorthandMethodNames.js", "prope^rty = value;  // global property", true);
    }
    
    public void testShorthandMethodNames05() throws Exception {
        checkOccurrences("testfiles/ecmascript6/shorthands/shorthandMethodNames.js", "property = val^ue;  // global property", true);
    }
    
    public void testComputedPropertyNames01() throws Exception {
        checkOccurrences("testfiles/ecmascript6/shorthands/computedPropertyNames.js", "var pro^p = \"foo\";", true);
    }
    
    public void testComputedPropertyNames02() throws Exception {
        checkOccurrences("testfiles/ecmascript6/shorthands/computedPropertyNames.js", "pr^op : \"test\"", true);
    }
    
    public void testNumberLiterals() throws Exception {
        checkOccurrences("testfiles/completion/general/numberLiterals01.js", "console.log(0o676.toF^ixed(3));", true);
    }
    
    public void testArrayDestructuring01() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/arrayDestructuring01.js", "[a^,b, c, d] = [1, 2, \"testik\", d+d];", true);
    }
    
    public void testArrayDestructuring02() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/arrayDestructuring01.js", "[a,b^, c, d] = [1, 2, \"testik\", d+d];", true);
    }
    
    public void testArrayDestructuring03() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/arrayDestructuring01.js", "[a,b, c^, d] = [1, 2, \"testik\", d+d];", true);
    }
    
    public void testArrayDestructuring04() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/arrayDestructuring01.js", "[a,b, c, d^] = [1, 2, \"testik\", d+d];", true);
    }
    
    public void testArrayDestructuring02_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/arrayDestructuring02.js", "var [o^ne, two, three] = foo;", true);
    }
    
    public void testArrayDestructuring02_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/arrayDestructuring02.js", "var [one, tw^o, three] = foo;", true);
    }
    
    public void testArrayDestructuring02_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/arrayDestructuring02.js", "var [one, two, thr^ee] = foo;", true);
    }
    
    public void testArrayDestructuring02_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/arrayDestructuring02.js", "var [one, two, three] = fo^o;", true);
    }
    
    public void testArrayDestructuring03_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/arrayDestructuring03.js", "[a^, b] = f();", true);
    }
    
    public void testArrayDestructuring03_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/arrayDestructuring03.js", "[a, b^] = f();", true);
    }
    
    public void testArrayDestructuring03_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/arrayDestructuring03.js", "[a, b] = f^();", true);
    }
    
    public void testArrayDestructuring01_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/arrayDestructuring04.js", "var [a^, , b] = f();", true);
    }
    
    public void testArrayDestructuring01_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/arrayDestructuring04.js", "var [a, , b^] = f();", true);
    }
    
    public void testArrayDestructuring01_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/arrayDestructuring04.js", "var [a, , b] = f^();", true);
    }
    
    public void testRegExpDestructuring01_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/regExpDestructuring01.js", "var [, pr^otocol, fullhost, fullpath] = parsedURL;", true);
    }
    
    public void testRegExpDestructuring01_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/regExpDestructuring01.js", "var [, protocol, ful^lhost, fullpath] = parsedURL;", true);
    }
    
    public void testRegExpDestructuring01_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/regExpDestructuring01.js", "var [, protocol, fullhost, fullp^ath] = parsedURL;", true);
    }
    
    public void testRegExpDestructuring01_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/regExpDestructuring01.js", "var [, protocol, fullhost, fullpath] = par^sedURL;", true);
    }
    
    public void testObjectDestructuring01_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/objectDestructuring01.js", "var {p^, q} = o;", true);
    }
    
    public void testObjectDestructuring01_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/objectDestructuring01.js", "var {p, q^} = o;", true);
    }
    
    public void testObjectDestructuring01_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/objectDestructuring01.js", "var {p, q} = o^;", true);
    }
    
    public void testObjectDestructuring02_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/objectDestructuring02.js", "({c^, a, b} = {b:1, a:2});", true);
    }
    
    public void testObjectDestructuring02_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/objectDestructuring02.js", "({c, a^, b} = {b:1, a:2});", true);
    }
    
    public void testObjectDestructuring02_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/objectDestructuring02.js", "({c, a, b^} = {b:1, a:2});", true);
    }
    
    public void testObjectDestructuring02_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/objectDestructuring02.js", "({c, a, b} = {b^:1, a:2});", true);
    }
    
    public void testObjectDestructuring02_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/objectDestructuring02.js", "({c, a, b} = {b:1, a^:2});", true);
    }
    
    public void testObjectDestructuring03_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/objectDestructuring03.js", "console.log(c^); // undefined ", true);
    }
    
    public void testObjectDestructuring04_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/objectDestructuring04.js", "var {p^: foo, q: bar, w: abc} = {p: 5, q:7};", true);
    }
    
    public void testObjectDestructuring04_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/objectDestructuring04.js", "var {p: foo^, q: bar, w: abc} = {p: 5, q:7};", true);
    }
    
    public void testObjectDestructuring04_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/objectDestructuring04.js", "var {p: foo, q: b^ar, w: abc} = {p: 5, q:7};", true);
    }
    
    public void testObjectDestructuring04_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/objectDestructuring04.js", "var {p: foo, q^: bar, w: abc} = {p: 5, q:7};", true);
    }
    
    public void testObjectDestructuring04_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/objectDestructuring04.js", "var {p: foo, q: bar, w: ab^c} = {p: 5, q:7};", true);
    }
    
    public void testObjectDestructuring05_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/objectDestructuring05.js", "var {p^: foo, q: bar} = o;", true);
    }
    
    public void testObjectDestructuring05_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/objectDestructuring05.js", "var {p: fo^o, q: bar} = o;", true);
    }
    
    public void testObjectDestructuring05_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/objectDestructuring05.js", "var {p: foo, q^: bar} = o;", true);
    }
    
    public void testObjectDestructuring05_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/objectDestructuring05.js", "var {p: foo, q: ba^r} = o;", true);
    }
    
    public void testObjectDestructuring05_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/objectDestructuring05.js", "var {p: foo, q: bar} = o^;", true);
    }
    
    public void testObjectDestructuring06_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/objectDestructuring06.js", "var {a^:ab=10, bb=5} = {a: 3}; ", true);
    }
    
    public void testObjectDestructuring06_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/objectDestructuring06.js", "var {a:a^b=10, bb=5} = {a: 3}; ", true);
    }
    
    public void testObjectDestructuring06_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/objectDestructuring06.js", "var {a:ab=10, b^b=5} = {a: 3}; ", true);
    }
    
    public void testExample01_01() throws Exception {
        // testing when the destructuring assignment is used as parameter definition
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/example01.js", "function drawE^S6Chart({size = 'big', cords = { x: 0, y: 0 , z: 0}, radius = 25} = {}) {", true);
    }
    
    public void testExample01_02() throws Exception {
        // testing when the destructuring assignment is used as parameter definition
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/example01.js", "function drawES6Chart({si^ze = 'big', cords = { x: 0, y: 0 , z: 0}, radius = 25} = {}) {", true);
    }
    
    public void testExample01_03() throws Exception {
        // testing when the destructuring assignment is used as parameter definition
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/example01.js", "function drawES6Chart({size = 'big', co^rds = { x: 0, y: 0 , z: 0}, radius = 25} = {}) {", true);
    }
    
    public void testExample01_04() throws Exception {
        // testing when the destructuring assignment is used as parameter definition
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/example01.js", "function drawES6Chart({size = 'big', cords = { x^: 0, y: 0 , z: 0}, radius = 25} = {}) {", true);
    }
    
    public void testExample01_05() throws Exception {
        // testing when the destructuring assignment is used as parameter definition
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/example01.js", "function drawES6Chart({size = 'big', cords = { x: 0, y^: 0 , z: 0}, radius = 25} = {}) {", true);
    }
    
    public void testExample01_06() throws Exception {
        // testing when the destructuring assignment is used as parameter definition
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/example01.js", "function drawES6Chart({size = 'big', cords = { x: 0, y: 0 , z^: 0}, radius = 25} = {}) {", true);
    }
    
    public void testExample01_07() throws Exception {
        // testing when the destructuring assignment is used as parameter definition
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/example01.js", "function drawES6Chart({size = 'big', cords = { x: 0, y: 0 , z: 0}, rad^ius = 25} = {}) {", true);
    }
    
    public void testExample01_08() throws Exception {
        // testing when the destructuring assignment is used as parameter definition
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/example01.js", "function drawES6Chart({size = 'big', cords = { x: 0, y: 0 , z: 0}, radius = ^25} = {}) {", true);
    }
    
    public void testExample01_09() throws Exception {
        // testing when the destructuring assignment is used as parameter definition
        checkOccurrences("testfiles/markoccurences/destructuringAssignments/example01.js", "function drawES6Chart({size = 'big', cords = { x: 0, y: 0 , z: 0}, radius = 25} = {^}) {", true);
    }
    
    public void testObjectPropertyAssignment01_01() throws Exception {
        checkOccurrences("testfiles/ecmascript6/parser/other/objectPropertyAssignment.js", "console.log(target.proper^ty1);", true);
    }
    
//    public void testObjectPropertyAssignment01_02() throws Exception {
//        checkOccurrences("testfiles/ecmascript6/parser/other/objectPropertyAssignment.js", "console.log(target.prop^erty2);", true);
//    }
    
    public void testBlockScope01_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/blockscope/scope01.js", "tes^t();", true);
    }
    
    public void testBlockScope01_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/blockscope/scope01.js", "var te^st = 10;", true);
    }
    
    public void testBlockScope01_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/blockscope/scope01.js", "let te^st = 20;", true);
    }
    
    public void testBlockScope02_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/blockscope/scope02.js", "return getNum^ber();", true);
    }
    
    public void testBlockScope02_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/blockscope/scope02.js", "console.log(getN^umber()); // 2", true);
    }
    
    public void testBlockScope02_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/blockscope/scope02.js", "console.log(getNu^mber()); // 3", true);
    }
    
    public void testBlockScope03_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/blockscope/scope03.js", "console.log(i^);  // object again", true);
    }
    
    public void testBlockScope03_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/blockscope/scope03.js", "console.log(i^); // number", true);
    }
    
    public void testBlockScope04_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/blockscope/scope04.js", "console.log(f^oo());         // 0", true);
    }
    
    public void testBlockScope04_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/blockscope/scope04.js", "console.log(fo^o());     // 1", true);
    }
    
    public void testBlockScope04_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/blockscope/scope04.js", "console.log(f^oo()); // 2", true);
    }
    
    public void testBlockScope05_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/blockscope/scope05.js", "console.log(\"i: \" + i^);", true);
    }
    
    public void testBlockScope05_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/blockscope/scope05.js", "console.log(\"day: \" + d^ay);", true);
    }
    
    public void testBlockScope05_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/blockscope/scope05.js", "console.log(\"index: \" + i^ndex);", true);
    }
    
    public void testBlockScope05_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/blockscope/scope05.js", "console.log(\"action: \" + ac^tion);", true);
    }
    
    public void testArrayLiteralInBlockScope01_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/blockscope/arrayLiteral01.js", "matches = _union(matc^hes, match);", true);
    }
    
    public void testArrayLiteralInBlockScope01_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/blockscope/arrayLiteral01.js", "matc^hes = _union(matches, match);", true);
    }
    
    public void testArrayLiteralInBlockScope01_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/blockscope/arrayLiteral01.js", "matches = _union(matches, ma^tch);", true);
    }
    
    public void testStrangeMethodNames_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/strangeMethodName.js", "dependencies[\"jqu^ery1.6+\"]();", true);
    }
    
    public void testIssue262469_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue262469.js", "test(servi^ce);", true);
    }
    
    public void testDecorators8_01() throws Exception {
        checkOccurrences("testfiles/parser/decorators/decorators8.js", "function annot^ation(target) {", true);
    }
    
    public void testIssue267974_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue267974.js", "var onreadystatec^hange;", true);
    }
    
    public void testIssue267974_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue267974_01.js", "      metho^d", true);
    }
    
    public void testIssue254189_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue254189.js", "this.methodB = metho^dB;", true);
    }
    
    public void testIssue246239_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue246239.js", "var type = ct^l.options.selectedIndex,", true);
    }
    
    public void testIssue246239_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue246239.js", "c^tl.value = type;", true);
    }
    
    public void testIssue267694_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue267694.js", "var bu^f = this.buf;", true);
    }
    
    public void testIssue267694_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue267694.js", "var buf = this.bu^f;", true);
    }
    
    public void testIssue252755_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue252755_01.js", "var bu^f = this.buf,", true);
    }
    
    public void testIssue252755_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue252755_01.js", "var buf = this.b^uf,", true);
    }
    
    public void testIssue252755_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue252755_02.js", "var coo^rd;", true);
    }
    
    public void testIssue223970_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue223970.js", "wrapper.pers.ru^n(); // place cursor here to inside run", true);
    }
    
    public void testIssue223970_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue223970.js", "wrapper.pe^rs.run(); // place cursor here to inside run", true);
    }
    
    public void testIssue231627_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231627.js", "var Argum^ents = {};", true);
    }
    
    public void testIssue258724_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue258724.js", "return this.nam^e;", true);
    }
    
    public void testIssue258724_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue258724.js", "return this.ag^e;", true);
    }

    public void testIssueGH5184_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issueGH5184_01.js", "export {te^st2};", true);
    }

    public void testIssueGH4376() throws Exception {
        checkStructure("testfiles/markoccurences/issueGH4376.js");
    }

    public void testIssueGH4376_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issueGH4376.js", "		this.b^ar = {};  //Line 5", true);
    }

    public void testIssueGH4376_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issueGH4376.js", "			this.ba^r[val] = 1;  //Line 7", true);
    }

    private String getTestName() {
        String name = getName();
        int indexOf = name.indexOf("_");
        if (indexOf != -1) {
            name = name.substring(0, indexOf);
        }
        return name;
    }
        
    @Override
    protected void assertDescriptionMatches(FileObject fileObject,
            String description, boolean includeTestName, String ext, boolean goldenFileInTestFileDir) throws IOException {
        super.assertDescriptionMatches(fileObject, description, includeTestName, ext, true);
    }
}
