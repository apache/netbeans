/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

}
