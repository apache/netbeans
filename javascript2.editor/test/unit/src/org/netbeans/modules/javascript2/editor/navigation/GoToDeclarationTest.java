/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
    
}
