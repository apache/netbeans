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

}
