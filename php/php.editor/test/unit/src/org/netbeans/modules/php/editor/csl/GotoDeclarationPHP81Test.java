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

public class GotoDeclarationPHP81Test extends GotoDeclarationTestBase {

    public GotoDeclarationPHP81Test(String testName) {
        super(testName);
    }

    @Override
    protected String getBaseTestFolderPath() {
        return super.getBaseTestFolderPath() + "php81/";
    }

    public void testNewInInitializersWithStaticVariable_01() throws Exception {
        checkDeclaration(getTestPath(), "static $staticVariable = new StaticVari^able;", "class ^StaticVariable {}");
    }

    public void testNewInInitializersWithStaticVariable_02() throws Exception {
        checkDeclaration(getTestPath(), "static $staticVariable = new StaticVari^able();", "class ^StaticVariable {}");
    }

    public void testNewInInitializersWithStaticVariable_03() throws Exception {
        checkDeclaration(getTestPath(), "static $staticVariable = new StaticVariab^le(1);", "class ^StaticVariable {}");
    }

    public void testNewInInitializersWithStaticVariable_04() throws Exception {
        checkDeclaration(getTestPath(), "static $staticVariable = new Stati^cVariable(x: 1);", "class ^StaticVariable {}");
    }

    public void testNewInInitializersWithConstant_01() throws Exception {
        checkDeclaration(getTestPath(), "const CONSTANT = new Co^nstant;", "class ^Constant {}");
    }

    public void testNewInInitializersWithConstant_02() throws Exception {
        checkDeclaration(getTestPath(), "const CONSTANT = new Const^ant();", "class ^Constant {}");
    }

    public void testNewInInitializersWithConstant_03() throws Exception {
        checkDeclaration(getTestPath(), "const CONSTANT = new Const^ant(\"test\", \"constant\");", "class ^Constant {}");
    }

    public void testNewInInitializersWithConstant_04() throws Exception {
        checkDeclaration(getTestPath(), "const CONSTANT = new Constan^t(test: \"test\", constant: \"constant\");", "class ^Constant {}");
    }

    public void testNewInInitializersWithFunc_01() throws Exception {
        checkDeclaration(getTestPath(), "function func1($param = new F^unc) {}", "class ^Func {}");
    }

    public void testNewInInitializersWithFunc_02() throws Exception {
        checkDeclaration(getTestPath(), "function func2($param = new Fun^c()) {}", "class ^Func {}");
    }

    public void testNewInInitializersWithFunc_03() throws Exception {
        checkDeclaration(getTestPath(), "function func3($param = new Fu^nc(1)) {}", "class ^Func {}");
    }

    public void testNewInInitializersWithFunc_04() throws Exception {
        checkDeclaration(getTestPath(), "function func4($param = new Fu^nc(test: 1)) {}", "class ^Func {}");
    }

    public void testNewInInitializersWithMethod_01() throws Exception {
        checkDeclaration(getTestPath(), "public $prop1 = new M^ethod,", "class ^Method {}");
    }

    public void testNewInInitializersWithMethod_02() throws Exception {
        checkDeclaration(getTestPath(), "public $prop2 = new Me^thod(),", "class ^Method {}");
    }

    public void testNewInInitializersWithMethod_03() throws Exception {
        checkDeclaration(getTestPath(), "public $prop3 = new Meth^od(\"test\"),", "class ^Method {}");
    }

    public void testNewInInitializersWithMethod_04() throws Exception {
        checkDeclaration(getTestPath(), "public $prop4 = new Metho^d(test: \"test\"),", "class ^Method {}");
    }

    public void testNewInInitializersWithMethod_05() throws Exception {
        checkDeclaration(getTestPath(), "$param = new Me^thod,", "class ^Method {}");
    }

    public void testNewInInitializersWithMethod_06() throws Exception {
        checkDeclaration(getTestPath(), "$param = new Metho^d(),", "class ^Method {}");
    }

    public void testNewInInitializersWithMethod_07() throws Exception {
        checkDeclaration(getTestPath(), "$param = new M^ethod(\"test\"),", "class ^Method {}");
    }

    public void testNewInInitializersWithMethod_08() throws Exception {
        checkDeclaration(getTestPath(), "$param = new Met^hod(test: \"test\"),", "class ^Method {}");
    }

    public void testNewInInitializersWithAttribute_01() throws Exception {
        checkDeclaration(getTestPath(), "#[AnAttribute(new F^oo)]", "class ^Foo {}");
    }

    public void testNewInInitializersWithAttribute_02() throws Exception {
        checkDeclaration(getTestPath(), "#[AnAttribute(new F^oo())]", "class ^Foo {}");
    }

    public void testNewInInitializersWithAttribute_03() throws Exception {
        checkDeclaration(getTestPath(), "#[AnAttribute(new Fo^o(1))]", "class ^Foo {}");
    }

    public void testNewInInitializersWithAttribute_04() throws Exception {
        checkDeclaration(getTestPath(), "#[AnAttribute(new Fo^o(x: 1))]", "class ^Foo {}");
    }

}
