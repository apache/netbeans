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

public class OccurrencesFinderImplPHP70Test extends OccurrencesFinderImplTestBase {

    public OccurrencesFinderImplPHP70Test(String testName) {
        super(testName);
    }

    // #262141
    public void testContextSensitiveLexer_01() throws Exception {
        checkOccurrences(getTestPath(), "const int^erface = \"interface\";", true);
    }

    public void testContextSensitiveLexer_02() throws Exception {
        checkOccurrences(getTestPath(), "self::interf^ace;", true);
    }

    public void testContextSensitiveLexer_03() throws Exception {
        checkOccurrences(getTestPath(), "MyInterface::interfa^ce;", true);
    }

    public void testContextSensitiveLexer_04() throws Exception {
        checkOccurrences(getTestPath(), "public function imple^ments();", true);
    }

    public void testContextSensitiveLexer_05() throws Exception {
        checkOccurrences(getTestPath(), "public function implemen^ts() {", true);
    }

    public void testContextSensitiveLexer_06() throws Exception {
        checkOccurrences(getTestPath(), "$this->implemen^ts();", true);
    }

    public void testContextSensitiveLexer_07() throws Exception {
        checkOccurrences(getTestPath(), "const C^ONST = \"CONST\";", true);
    }

    public void testContextSensitiveLexer_08() throws Exception {
        checkOccurrences(getTestPath(), "$parent::CON^ST;", true);
    }

    public void testContextSensitiveLexer_09() throws Exception {
        checkOccurrences(getTestPath(), "parent::^CONST;", true);
    }

    public void testContextSensitiveLexer_10() throws Exception {
        checkOccurrences(getTestPath(), "ParentClass::CONST^", true);
    }

    public void testContextSensitiveLexer_11() throws Exception {
        checkOccurrences(getTestPath(), "public static function ^new($new) {", true);
    }

    public void testContextSensitiveLexer_12() throws Exception {
        checkOccurrences(getTestPath(), "ChildClass::ne^w(\"test\");", true);
    }

    public void testContextSensitiveLexer_13() throws Exception {
        checkOccurrences(getTestPath(), "public function trai^t($a) {", true);
    }

    public void testContextSensitiveLexer_14() throws Exception {
        checkOccurrences(getTestPath(), "$child->trai^t(\"trait\");", true);
    }

    public void testContextSensitiveLexer_15() throws Exception {
        checkOccurrences(getTestPath(), "const GO^TO = [1, 2], IF = 2;", true);
    }

    public void testContextSensitiveLexer_16() throws Exception {
        checkOccurrences(getTestPath(), "self::GOT^O[0];", true);
    }

    public void testContextSensitiveLexer_17() throws Exception {
        checkOccurrences(getTestPath(), "$child::GOT^O[0];", true);
    }

    public void testContextSensitiveLexer_18() throws Exception {
        checkOccurrences(getTestPath(), "const ECH^O = \"ECHO\", FOR = 1;", true);
    }

    public void testContextSensitiveLexer_19() throws Exception {
        checkOccurrences(getTestPath(), "static::E^CHO;", true);
    }

    public void testContextSensitiveLexer_20() throws Exception {
        checkOccurrences(getTestPath(), "const ECHO = \"ECHO\", F^OR = 1;", true);
    }

    public void testContextSensitiveLexer_21() throws Exception {
        checkOccurrences(getTestPath(), "ChildClass::F^OR;", true);
    }

    public void testContextSensitiveLexer_22() throws Exception {
        checkOccurrences(getTestPath(), "public function forea^ch($test) {", true);
    }

    public void testContextSensitiveLexer_23() throws Exception {
        checkOccurrences(getTestPath(), "$child->for^each(\"test\");", true);
    }

    public void testContextSensitiveLexer_24() throws Exception {
        checkOccurrences(getTestPath(), "public static function f^or() {", true);
    }

    public void testContextSensitiveLexer_25() throws Exception {
        checkOccurrences(getTestPath(), "ChildClass::f^or();", true);
    }

    public void testContextSensitiveLexer_26() throws Exception {
        checkOccurrences(getTestPath(), "const GOTO = [1, 2], I^F = 2;", true);
    }

    public void testContextSensitiveLexer_27() throws Exception {
        checkOccurrences(getTestPath(), "ChildClass::^IF;", true);
    }

    // nested static field access
    public void testUniformVariableSyntax03_01() throws Exception {
        checkOccurrences(getTestPath(), "class U^VS1", true);
    }

    public void testUniformVariableSyntax03_02() throws Exception {
        checkOccurrences(getTestPath(), "     * @var UV^S1", true);
    }

    public void testUniformVariableSyntax03_03() throws Exception {
        checkOccurrences(getTestPath(), "UV^S1::$INSTANCE2::$INSTANCE3::MAX;", true);
    }

    public void testUniformVariableSyntax03_04() throws Exception {
        checkOccurrences(getTestPath(), "    static $INSTA^NCE2;", true);
    }

    public void testUniformVariableSyntax03_05() throws Exception {
        checkOccurrences(getTestPath(), "UVS1::$INSTAN^CE2::$INSTANCE3::MAX;", true);
    }

    public void testUniformVariableSyntax03_06() throws Exception {
        checkOccurrences(getTestPath(), "test()::$INSTANCE1::$INSTANC^E2::MAX;", true);
    }

    public void testUniformVariableSyntax03_07() throws Exception {
        checkOccurrences(getTestPath(), "    static $INSTAN^CE3;", true);
    }

    public void testUniformVariableSyntax03_08() throws Exception {
        checkOccurrences(getTestPath(), "UVS1::$INSTANCE2::$INSTANC^E3::MAX;", true);
    }

    public void testUniformVariableSyntax03_09() throws Exception {
        checkOccurrences(getTestPath(), "    const ^MAX = 101; // UVS3", true);
    }

    public void testUniformVariableSyntax03_10() throws Exception {
        checkOccurrences(getTestPath(), "UVS1::$INSTANCE2::$INSTANCE3::M^AX;", true);
    }

    public void testUniformVariableSyntax03_11() throws Exception {
        checkOccurrences(getTestPath(), "    static $INSTAN^CE1;", true);
    }

    public void testUniformVariableSyntax03_12() throws Exception {
        checkOccurrences(getTestPath(), "test()::$INSTA^NCE1::$INSTANCE2::MAX;", true);
    }

    public void testUniformVariableSyntax03_13() throws Exception {
        checkOccurrences(getTestPath(), "    const MA^X = 100; // UVS2", true);
    }

    public void testUniformVariableSyntax03_14() throws Exception {
        checkOccurrences(getTestPath(), "test()::$INSTANCE1::$INSTANCE2::MA^X;", true);
    }

}
