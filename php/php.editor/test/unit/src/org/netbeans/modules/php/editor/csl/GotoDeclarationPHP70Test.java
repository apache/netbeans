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

public class GotoDeclarationPHP70Test extends GotoDeclarationTestBase {

    public GotoDeclarationPHP70Test(String testName) {
        super(testName);
    }

    // #262141
    public void testContextSensitiveLexer_01() throws Exception {
        checkDeclaration(getTestPath(), "self::inter^face;", "const ^interface = \"interface\";");
    }

    public void testContextSensitiveLexer_02() throws Exception {
        checkDeclaration(getTestPath(), "self::GO^TO[0];", "const ^GOTO = [1, 2], IF = 2;");
    }

    public void testContextSensitiveLexer_03() throws Exception {
        checkDeclaration(getTestPath(), "static::E^CHO;", "const ^ECHO = \"ECHO\", FOR = 1;");
    }

    public void testContextSensitiveLexer_04() throws Exception {
        checkDeclaration(getTestPath(), "parent::C^ONST;", "const ^CONST = \"CONST\";");
    }

    public void testContextSensitiveLexer_05() throws Exception {
        checkDeclaration(getTestPath(), "$this->impl^ements();", "public function ^implements() {");
    }

    public void testContextSensitiveLexer_06() throws Exception {
        checkDeclaration(getTestPath(), "MyInterface::inter^face;", "const ^interface = \"interface\";");
    }

    public void testContextSensitiveLexer_07() throws Exception {
        checkDeclaration(getTestPath(), "$parent::CON^ST;", "const ^CONST = \"CONST\";");
    }

    public void testContextSensitiveLexer_08() throws Exception {
        checkDeclaration(getTestPath(), "ParentClass::CO^NST;", "const ^CONST = \"CONST\";");
    }

    public void testContextSensitiveLexer_09() throws Exception {
        checkDeclaration(getTestPath(), "$child::GOT^O[0];", "const ^GOTO = [1, 2], IF = 2;");
    }

    public void testContextSensitiveLexer_10() throws Exception {
        checkDeclaration(getTestPath(), "ChildClass::FO^R;", "const ECHO = \"ECHO\", ^FOR = 1;");
    }

    public void testContextSensitiveLexer_11() throws Exception {
        checkDeclaration(getTestPath(), "ChildClass::fo^r();", "public static function ^for() {");
    }

    public void testContextSensitiveLexer_12() throws Exception {
        checkDeclaration(getTestPath(), "ChildClass::ne^w(\"test\");", "public static function ^new($new) {");
    }

    public void testContextSensitiveLexer_13() throws Exception {
        checkDeclaration(getTestPath(), "$child->forea^ch(\"test\");", "public function ^foreach($test) {");
    }

    public void testContextSensitiveLexer_14() throws Exception {
        checkDeclaration(getTestPath(), "$child->trai^t(\"trait\");", "public function ^trait($a) {");
    }

    public void testContextSensitiveLexer_15() throws Exception {
        checkDeclaration(getTestPath(), "        ChildClass::I^F;", "    const GOTO = [1, 2], ^IF = 2;");
    }

    public void testUniformVariableSyntaxNestedStaticFieldAccess_01() throws Exception {
        checkDeclaration(getTestPath(), "UV^S1::$INSTANCE2::$INSTANCE3::MAX;", "class ^UVS1");
    }

    public void testUniformVariableSyntaxNestedStaticFieldAccess_02() throws Exception {
        checkDeclaration(getTestPath(), "UVS1::$I^NSTANCE2::$INSTANCE3::MAX;", "    static $^INSTANCE2;");
    }

    public void testUniformVariableSyntaxNestedStaticFieldAccess_03() throws Exception {
        checkDeclaration(getTestPath(), "UVS1::$INSTANCE2::$INS^TANCE3::MAX;", "    static $^INSTANCE3;");
    }

    public void testUniformVariableSyntaxNestedStaticFieldAccess_04() throws Exception {
        checkDeclaration(getTestPath(), "UVS1::$INSTANCE2::$INSTANCE3::M^AX;", "    const ^MAX = 101; // UVS3");
    }

    public void testUniformVariableSyntaxNestedStaticFieldAccess_05() throws Exception {
        checkDeclaration(getTestPath(), "tes^t()::$INSTANCE1::$INSTANCE2::MAX;", "function ^test() { // func");
    }

    public void testUniformVariableSyntaxNestedStaticFieldAccess_06() throws Exception {
        checkDeclaration(getTestPath(), "test()::$IN^STANCE1::$INSTANCE2::MAX;", "    static $^INSTANCE1;");
    }

    public void testUniformVariableSyntaxNestedStaticFieldAccess_07() throws Exception {
        checkDeclaration(getTestPath(), "test()::$INSTANCE1::$INSTAN^CE2::MAX;", "    static $^INSTANCE2;");
    }

    public void testUniformVariableSyntaxNestedStaticFieldAccess_08() throws Exception {
        checkDeclaration(getTestPath(), "test()::$INSTANCE1::$INSTANCE2::M^AX;", "    const ^MAX = 100; // UVS2");
    }
}
