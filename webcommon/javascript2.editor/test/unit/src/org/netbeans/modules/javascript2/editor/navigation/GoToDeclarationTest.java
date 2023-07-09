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

import org.netbeans.modules.javascript2.editor.JsTestBase;

/**
 *
 * @author Petr Pisl
 */
public class GoToDeclarationTest extends JsTestBase {
    
    public GoToDeclarationTest(String testName) {
        super(testName);
    }
    
    public void testIssue209941_01() throws Exception {
        checkDeclaration("testfiles/coloring/issue209941.js", "this.globalNot^ify();", "    this.^globalNotify = function() {");
    }
            
    public void testIssue233298_0() throws Exception {        
        checkDeclaration("testfiles/coloring/issue233298.js", "address.door^No", "^doorNo: \"10\"");        
    }
        
    public void testIssue233298_1() throws Exception {        
        checkDeclaration("testfiles/coloring/issue233298_1.js", "add.door^No", "^doorNo: \"10\"");        
    }
    
    public void testIssue233298_2() throws Exception {        
        checkDeclaration("testfiles/coloring/issue233298_1.js", "addr3.door^No", "^doorNo: \"10\"");        
    }
    
    public void testIssue233298_3() throws Exception {
        checkDeclaration("testfiles/coloring/issue233298_2.js", "add.zi^p", "^zip: \"15000\"");        
    }

    public void testIssue233298_4() throws Exception { 
        checkDeclaration("testfiles/coloring/issue233298.js", "a.b^", "a.^b");
    }

    public void testIssue233298_5() throws Exception {
        checkDeclaration("testfiles/coloring/issue233298_3.js", "Foo.test2 = test^1", "var ^test1 = Foo.test2");
    }
    
    public void testBasicNavigation_01() throws Exception {
        checkDeclaration("testfiles/model/variables01.js", "var address = new A^ddress(\"Prague\", \"Czech Republic\", 15000)", "function ^Address (town, state, number) {");
    }
    
    public void testBasicNavigation_02() throws Exception {
        checkDeclaration("testfiles/model/variables01.js", "formatter.println(addr^ess.print());", "var ^address = new Address(\"Prague\", \"Czech Republic\", 15000)");
    }
    
    public void testBasicNavigation_03() throws Exception {
        checkDeclaration("testfiles/model/variables01.js", "formatter.println(\"MyApp.country: \" + MyApp.coun^try);", "    MyApp.^country = state; ");
    }

    public void testIssue176581() throws Exception {
        checkDeclaration("testfiles/coloring/issue176581.js", "    someElement.onfocus = fo^o;", "function ^foo() { }");
    }
    
    public void testIssue218090_01() throws Exception {
        checkDeclaration("testfiles/coloring/issue218090.js", "        text : pro^m,", "    var ^prom = 'test';");
    }
    
    public void testIssue223057_01() throws Exception {
        checkDeclaration("testfiles/markoccurences/issue223057.js", "a.url = container.na^me;", "container.^name = n;");
    }
    
    public void testCallbackDeclaration_01() throws Exception {
        checkDeclaration("testfiles/markoccurences/callbackDeclaration1.js", "* @param {Requester~requestC^allback} cb - The callback that handles the response.", " * @callback Requester~^requestCallback");
    }
    
    public void testCallbackDeclaration_02() throws Exception {
        checkDeclaration("testfiles/markoccurences/callbackDeclaration1.js", "* @param {Reque^ster~requestCallback} cb - The callback that handles the response.", "function ^Requester() {}");
    }
    
    public void testCallbackDeclaration_03() throws Exception {
        checkDeclaration("testfiles/markoccurences/callbackDeclaration2.js", " * @param {reques^tCallback} cb - The callback that handles the response.", " * @callback ^requestCallback");
    }
    
    public void testIssue227972_01() throws Exception {
        checkDeclaration("testfiles/navigation/issue227972.js", "a.meth^od();", "this.^method = function() { // nagigate from a.method()");
    }
    
    public void testIssue227972_02() throws Exception {
        checkDeclaration("testfiles/navigation/issue227972.js", "b.meth^od();", "this.^method = function() { // navigate from b.mehtod()");
    }
    
    public void testIssue227972_03() throws Exception {
        checkDeclaration("testfiles/navigation/issue227972.js", "this.me^thod(); // case 1", "this.^method = function() { // nagigate from a.method()");
    }
    
    public void testIssue227972_04() throws Exception {
        checkDeclaration("testfiles/navigation/issue227972.js", "this.me^thod(); // case 2", "this.^method = function() { // navigate from b.mehtod()");
    }
    
    public void testIssue227972_05() throws Exception {
        checkDeclaration("testfiles/navigation/issue227972.js", "this.me^thod(); // case 3", "this.^method = function() { // navigate from c.method()");
    }
    
    public void testIssue227972_06() throws Exception {
        checkDeclaration("testfiles/navigation/issue227972.js", "c.me^thod();", "this.^method = function() { // navigate from c.method()");
    }
    
//    public void testIssue227972_07() throws Exception {
//        checkDeclaration("testfiles/navigation/issue227972.js", "NS.test.met^hod();", "this.^method = function() { // navigate from c.method()");
//    }
    
    public void testIssue227972_08() throws Exception {
        checkDeclaration("testfiles/navigation/issue227972.js", "this.f^();", "MyClass.prototype.^f= function() {};");
    }
    
    public void testImportedFile_01() throws Exception {
        checkDeclaration("testfiles/ecmascript6/importExport/importFindDeclaration01.js", "import text from \"expo^rt01\";", "export01.js", 0);
    }
    
    public void testImportedFile_02() throws Exception {
        checkDeclaration("testfiles/ecmascript6/importExport/importFindDeclaration01.js", "import { text as text2 } from \"./lib/exp^ort02\";", "export02.js", 0);
    }
    
    public void testImportedFile_03() throws Exception {
        checkDeclaration("testfiles/ecmascript6/importExport/importFindDeclaration01.js", "import { text as text3 } from \"l^ib/export02\";", "export02.js", 0);
    }

    public void testIssueGH5184_01() throws Exception {
        checkDeclaration("testfiles/markoccurences/issueGH5184_01.js", "export {te^st2};", "class ^test2 {");
    }
}
