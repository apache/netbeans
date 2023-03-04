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
package org.netbeans.modules.javascript2.editor;

/**
 *
 * @author Petr Pisl
 */
public class JsCodeCompletionTest extends JsCodeCompletionBase {

    public JsCodeCompletionTest(String testName) {
        super(testName);
    }

    public void testPrefix1() throws Exception {
        checkPrefix("testfiles/completion/cc-prefix1.js");
    }

    public void testPrefix2() throws Exception {
        checkPrefix("testfiles/completion/cc-prefix2.js");
    }

    public void testPrefix3() throws Exception {
        checkPrefix("testfiles/completion/cc-prefix3.js");
    }

    public void testTypeInferenceNew01() throws Exception {
        checkCompletion("testfiles/completion/typeInferenceNew.js", "^formatter.println(\"Car:\");", false);
    }

    public void testTypeInferenceNew02() throws Exception {
        checkCompletion("testfiles/completion/typeInferenceNew.js", "formatter.println(\"color: \" + object.^getColor());", false);
    }

    public void testTypeInferenceNew03() throws Exception {
        checkCompletion("testfiles/completion/typeInferenceNew.js", "formatter.println(\"town: \" + object.^town);", false);
    }

    public void testMethodsOfUndefinedObjects() throws Exception {
        checkCompletion("testfiles/completion/typeInferenceNew.js", "formatter.^println(\"town: \" + object.town);", false);
    }

    public void test129036() throws Exception {
        // needs to be displayed also return types
        checkCompletion("testfiles/completion/test129036.js", "my^ //Foo", false);
    }

    public void testSimpleObject01() throws Exception {
        checkCompletion("testfiles/model/simpleObject.js", "this.called = this.^called + 1;", false);
    }

    public void testSimpleObject02() throws Exception {
        checkCompletion("testfiles/model/simpleObject.js", "this.called = this.cal^led + 1;", false);
    }

    public void testSimpleObject03() throws Exception {
        checkCompletion("testfiles/model/simpleObject.js", "if (this.^color === \"red\") {", false);
    }

    public void testSimpleObject04() throws Exception {
        checkCompletion("testfiles/model/simpleObject.js", "formatter.println(Carrot.isVegi^table());", false);
    }

    public void testGetterSettterInObjectLiteral() throws Exception {
        checkCompletion("testfiles/model/getterSettterInObjectLiteral.js", "formatter.println(\"The dos is old \" + Dog.^years + \" years.\");", false);
    }
    
    public void testPrototype01() throws Exception {
        checkCompletion("testfiles/model/person.js", "gk.s^howLegs(); lk.showLegs();", false);
    }
    
    public void testPrototype02() throws Exception {
        checkCompletion("testfiles/model/person.js", "gk.showLegs(); lk.^showLegs();", false);
    }
    
    public void testPrototype03() throws Exception {
        checkCompletion("testfiles/model/person.js", "Person.p^rototype.shower=function(){ ", false);
    }
    
    public void testPrototype04() throws Exception {
        checkCompletion("testfiles/model/person.js", "Person.prototype.beCool = function(){ this.c^lothing=\"khakis and black shirt\" } ", false);
    }

    public void testPrototype05() throws Exception {
        checkCompletion("testfiles/model/person.js", "Person.prototype.shower = function(){ this.^dirtFactor=2 }", false);
    }

    public void testPrototype06() throws Exception {
        checkCompletion("testfiles/model/person.js", "Person.prototype.amputate = function(){ this.l^egs-- } ", false);
    }
    
    public void testSingletonCloser01() throws Exception {
        checkCompletion("testfiles/completion/patterns/SingletonCloser.js", "_^arr[1] = 10;", false);
    }
    
    public void testSingletonCloser02() throws Exception {
        checkCompletion("testfiles/completion/patterns/SingletonCloser.js", "SingletonClousure.^getArr();", false);
    }

    public void testSingletonCloser03() throws Exception {
        checkCompletion("testfiles/completion/patterns/SingletonCloser.js", "test.^createArr(); // OK: SHOW properties", false);
    }
   
    public void testResolvingThis01() throws Exception {
        checkCompletion("testfiles/completion/resolvingThis.js", "me.^fnc1();", false);
    }

    public void testIssue218631() throws Exception {
        checkCompletion("testfiles/completion/issue218631.html", "this.name = p^", false);
    }

    public void testIssue221022() throws Exception {
        checkCompletion("testfiles/completion/issue221022.js", "    $scope.^     //here", false);
    }

    public void testIssue222955() throws Exception {
        checkCompletion("testfiles/completion/issue222955.js", "po^ //here CC", false);
    }

    public void testIssue224458() throws Exception {
        checkCompletion("testfiles/completion/issue224458.js", "U^ //CC here", false);
    }
    
    public void testIssue226559() throws Exception {
        checkCompletion("testfiles/structure/issue226559.js", "test226559.met^hod1();", false);
    }
    
    public void testIssue226532() throws Exception {
        checkCompletion("testfiles/completion/issue226532.js", "that.^ppp();", false);
    }
    
    public void testIssue228564() throws Exception {
        checkCompletion("testfiles/completion/issue228564.js", "y(\"d\").^", false);
    }
    
    public void testIssue229717_01() throws Exception {
        checkCompletion("testfiles/model/issue229717.js", "test.^types", false);
    }
    
    public void testIssue229717_02() throws Exception {
        checkCompletion("testfiles/model/issue229717.js", "this.^types;", false);
    }
    
    public void testIssue229717_03() throws Exception {
        checkCompletion("testfiles/model/issue229717.js", "self.^types.push(type);", false);
    }
    
    public void testIssue224071_01() throws Exception {
        checkCompletion("testfiles/completion/issue224071.js", "a.^t", false);
    }

    public void testIssue225307() throws Exception {
        checkCompletion("testfiles/completion/issue225307.js", "/*HERE*/ d^", false);
    }
    
    public void testIssue226650() throws Exception {
        checkCompletion("testfiles/completion/issue226650.html", "                <script>^", false);
    }
    
    public void testIssue226563() throws Exception {
        checkCompletion("testfiles/completion/issue226563.js", "        this.^pppp();", false);
    }
    
    public void testIssue223933() throws Exception {
        checkCompletion("testfiles/completion/issue223933.js", "test22393^3()", false);
    }
    
    public void testIssue229204_01() throws Exception {
        checkCompletion("testfiles/completion/issue229204.js", "(new Test()).r^", false);
    }
    
    public void testIssue231293_01() throws Exception {
        checkCompletion("testfiles/completion/issue231293.js", "_self.form.e^", false);
    }
    
    public void testIssue232570_01() throws Exception {
        checkCompletion("testfiles/completion/issue232570.js", "Test.modules.moduleA.n^ame;", false);
    }
    
    public void testIssue232178_01() throws Exception {
        checkCompletion("testfiles/completion/issue232178.js", "               ^ // here", false);
    }
    
    public void testIssue238986() throws Exception {
        checkCompletion("testfiles/completion/issue228986.js", "var a = $^.parent(); ", false);
    }
    
    public void testIssue224650() throws Exception {
        checkCompletion("testfiles/completion/issue224650.js", "this.^ // cc here", false);
    }
    
    public void testIssue223593() throws Exception {
        checkCompletion("testfiles/completion/issue223593.js", "obj.^propertyC = \"B\";", false);
    }
    
    public void testIssue233719_01() throws Exception {
        checkCompletion("testfiles/structure/issue233719.js", "console.log(p^rom02);", false);
    }
    
    public void testIssue233719_02() throws Exception {
        checkCompletion("testfiles/structure/issue233719.js", "console.log(man.^prop2);", false);
    }
    
    public void testIssue23277() throws Exception {
        checkCompletion("testfiles/markoccurences/issue232792.js", "A.B.^getName();", false);
    }
    
    public void testCCinWith01() throws Exception {
        checkCompletion("testfiles/markoccurences/issue232792.js", "    ^ // try here", false);
    }
    
    public void testIssue232798() throws Exception {
        checkCompletion("testfiles/markoccurences/issue232792.js", "        this.^c // issue 232798", false);
    }
    
    public void testIssue238310_01() throws Exception {
        checkCompletion("testfiles/completion/issue238310_01.js", "var x = {a238310:{oneA:\"test\", twoA:2}}.a^;", false);
    }
    
    public void testIssue238310_02() throws Exception {
        checkCompletion("testfiles/completion/issue238310_02.js", "var x = {a238310:{oneA:\"test\", twoA:2}}.a238310.^;", false);
    }
    
    public void testIssue238310_03() throws Exception {
        checkCompletion("testfiles/completion/issue238310_03.js", "x.^", false);
    }
    
    public void testIssue240914_01() throws Exception {
        checkCompletion("testfiles/completion/issue240914.js", "        ^name);", false);
    }
    
    public void testIssue240914_02() throws Exception {
        checkCompletion("testfiles/completion/issue240914.js", "        .^name);", false);
    }
    
    public void testIssue240914_03() throws Exception {
        checkCompletion("testfiles/completion/issue240914.js", "        .^prop1);", false);
    }
    
    public void testIssue240914_04() throws Exception {
        checkCompletion("testfiles/completion/issue240914.js", "        ^prop1);", false);
    }
    
    public void testIssue249454() throws Exception {
        checkCompletion("testfiles/model/person.js", "this.^dirtFactor++;", false);
    }
    
    public void testIssue242408_01() throws Exception {
        checkCompletion("testfiles/model/issue242408.js", "cube.^foo();", false);
    }
    
    public void testIssue242408_02() throws Exception {
        checkCompletion("testfiles/model/issue242408.js", "foo.^bar();", false);
    }
    
    public void testIssue242454_01() throws Exception {
        checkCompletion("testfiles/model/issue242454.js", "myLib.^", false);
    }
    
    public void testIssue238356_01() throws Exception {
        checkCompletion("testfiles/completion/issue238356.js", "x.^one;", false);
    }
    
    public void testConstructors_01() throws Exception {
        checkCompletion("testfiles/completion/constructors.js", "var hisObject = new ^MyObject();", false);
    }
    
    public void testConstructors_02() throws Exception {
        checkCompletion("testfiles/completion/constructors.js", "test(My^Object());", false);
    }
    
    public void testConstructors_03() throws Exception {
        checkCompletion("testfiles/completion/constructors.js", "Context.Men.A^ddress = function (street, town){};", false);
    }
    
    public void testIssue237878() throws Exception {
        checkCompletion("testfiles/completion/issue237878.js", "a.^", false);
    }
    
    public void testIssue242454A_01() throws Exception {
        checkCompletion("testfiles/completion/issue242454A.js", "myLib.f^;", false);
    }
    
    public void testIssue242454A_02() throws Exception {
        checkCompletion("testfiles/completion/issue242454A.js", "f^2(\"arg\");", false);
    }
    
    public void testIssue242472_01() throws Exception {
        checkCompletion("testfiles/completion/issue242472.js", "f^;", false);
    }
    
    public void testIssue242472_02() throws Exception {
        checkCompletion("testfiles/completion/issue242472.js", "myLib.^;", false);
    }
    
    public void testIssue242472B_01() throws Exception {
        checkCompletion("testfiles/completion/issue242472B.js", "f^;", false);
    }
    
    public void testIssue242472B_02() throws Exception {
        checkCompletion("testfiles/completion/issue242472B.js", "this.f^;", false);
    }
    
    public void testIssue242472B_03() throws Exception {
        checkCompletion("testfiles/completion/issue242472B.js", "myLib.^;", false);
    }
    
    public void testIssue246006_01() throws Exception {
        checkCompletion("testfiles/completion/issue246006_01.js", "x.^;", false);
    }
    
    public void testIssue246006_02() throws Exception {
        checkCompletion("testfiles/completion/issue246006_02.js", "x.^", false);
    }
    
    public void testIssue223111_01() throws Exception {
        checkCompletion("testfiles/completion/issue223111.js", "}.a^Property1;", false);
    }
    
    public void testShorthandPropertyNames01() throws Exception {
        checkCompletion("testfiles/ecmascript6/shorthands/shorthandPropertyNames.js", "console.log(o.c.^third);", true);
    }
    
    public void testShorthandPropertyNames02() throws Exception {
        checkCompletion("testfiles/ecmascript6/shorthands/shorthandPropertyNames.js", "console.log(c.^third);", true);
    }
    
    public void testIssue258724_01() throws Exception {
        checkCompletion("testfiles/completion/issue258724.js", "return this.n^;", false);
    }
    
    public void testIssue258724_02() throws Exception {
        checkCompletion("testfiles/completion/issue258724.js", "return this.a^;", false);
    }

    public void testCompletionPrivateMembers() throws Exception {
        checkCompletion("testfiles/completion/classPrivateMembersInside.js", "        return this.^;", false);
        checkCompletion("testfiles/completion/classPrivateMembersOutside.js", "c.^", false);
    }
}
