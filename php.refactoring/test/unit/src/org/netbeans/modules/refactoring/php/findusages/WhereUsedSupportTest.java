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
package org.netbeans.modules.refactoring.php.findusages;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class WhereUsedSupportTest extends FindUsagesTestBase {

    public WhereUsedSupportTest(String testName) {
        super(testName);
    }

    public void testIssue213974_01() throws Exception {
        findUsages("echo Kitchen::$aStatic^Field;");
    }

    public void testIssue213974_02() throws Exception {
        findUsages("echo Kitchen::getDefault^Size();");
    }

    public void testIssue213974_03() throws Exception {
        findUsages("echo Kitchen::SI^ZE;");
    }

    public void testTraits_01() throws Exception {
        findUsages("trait F^oo {");
    }

    public void testTraits_02() throws Exception {
        findUsages("trait B^ar {");
    }

    public void testTraits_03() throws Exception {
        findUsages("trait B^az {");
    }

    public void testTraits_04() throws Exception {
        findUsages("use F^oo, Bar, Baz {");
    }

    public void testTraits_05() throws Exception {
        findUsages("use Foo, B^ar, Baz {");
    }

    public void testTraits_06() throws Exception {
        findUsages("use Foo, Bar, B^az {");
    }

    public void testTraits_07() throws Exception {
        findUsages("F^oo::fnc insteadof Bar, Baz;");
    }

    public void testTraits_08() throws Exception {
        findUsages("Foo::fnc insteadof B^ar, Baz;");
    }

    public void testTraits_09() throws Exception {
        findUsages("Foo::fnc insteadof Bar, B^az;");
    }

    public void testTraits_10() throws Exception {
        findUsages("B^ar::fnc as aliased;");
    }

    public void testClasses_01() throws Exception {
        findUsages("class F^oo {");
    }

    public void testClasses_02() throws Exception {
        findUsages("use A\\Fo^o;");
    }

    public void testClasses_03() throws Exception {
        findUsages("class B^ar extends Foo {");
    }

    public void testClasses_04() throws Exception {
        findUsages("class Bar extends F^oo {");
    }

    public void testClasses_05() throws Exception {
        findUsages("use A\\F^oo;");
    }

    public void testClasses_06() throws Exception {
        findUsages("class B^az {");
    }

    public void testClasses_07() throws Exception {
        findUsages("F^oo::stMethod();");
    }

    public void testClasses_08() throws Exception {
        findUsages("F^oo::$stField;");
    }

    public void testClasses_09() throws Exception {
        findUsages("F^oo::CON;");
    }

    public void testAliases_01() throws Exception {
        findUsages("use Foo\\Bar\\FB as A^li;");
    }

    public void testAliases_02() throws Exception {
        findUsages("* @param A^li $param");
    }

    public void testAliases_03() throws Exception {
        findUsages("function functionName(A^li $param) {");
    }

    public void testAliases_04() throws Exception {
        findUsages("A^li::CON;");
    }

    public void testFields_01() throws Exception {
        findUsages("const C^ON = 1;");
    }

    public void testFields_02() throws Exception {
        findUsages("public static $st^Field;");
    }

    public void testFields_03() throws Exception {
        findUsages("public $pub^Field;");
    }

    public void testFields_04() throws Exception {
        findUsages("Foo::$st^Field;");
    }

    public void testFields_05() throws Exception {
        findUsages("Foo::C^ON;");
    }

    public void testFields_06() throws Exception {
        findUsages("$f->pub^Field;");
    }

    public void testMethods_01() throws Exception {
        findUsages("public static function st^Method() {}");
    }

    public void testMethods_02() throws Exception {
        findUsages("public function in^Method() {}");
    }

    public void testMethods_03() throws Exception {
        findUsages("Foo::st^Method();");
    }

    public void testMethods_04() throws Exception {
        findUsages("$f->in^Method();");
    }

    public void testMethods_05() throws Exception {
        findUsages("$retFoo = $this->ret^Foo();");
    }

    public void testMethods_06() throws Exception {
        findUsages("$retFoo->in^Method();");
    }

    public void testMethods_07() throws Exception {
        findUsages("$retFoo2 = $this->ret^Foo2();");
    }

    public void testMethods_08() throws Exception {
        findUsages("$retFoo2->in^Method();");
    }

    public void testMethods_09() throws Exception {
        findUsages("private function ret^Foo() {");
    }

    public void testMethods_10() throws Exception {
        findUsages("private function ret^Foo2() {");
    }

    public void testInterfaces_01() throws Exception {
        findUsages("interface My^Face {}");
    }

    public void testInterfaces_02() throws Exception {
        findUsages("use First\\My^Face;");
    }

    public void testInterfaces_03() throws Exception {
        findUsages("interface Another^Face extends MyFace {}");
    }

    public void testInterfaces_04() throws Exception {
        findUsages("interface AnotherFace extends My^Face {}");
    }

    public void testInterfaces_05() throws Exception {
        findUsages("use Second\\Another^Face;");
    }

    public void testInterfaces_06() throws Exception {
        findUsages("class Foo implements Another^Face {}");
    }

    public void testIssue224176_01() throws Exception {
        findUsages("public function TestFunc(Test $fr^ed) {");
    }

    public void testIssue224176_02() throws Exception {
        findUsages("public function TestFunc($fr^ed) {");
    }

    public void testIssue231286() throws Exception {
        findUsages("define('ROLE_^ADMIN', 7);");
    }

    public void testIssue217360_01() throws Exception {
        findUsages("private function get^Two() //One");
    }

    public void testIssue217360_02() throws Exception {
        findUsages("$two = $this->get^Two();");
    }

    public void testIssue217360_03() throws Exception {
        findUsages("return $two->get^Two();");
    }

    public void testIssue217360_04() throws Exception {
        findUsages("(new Two)->get^Two();");
    }

}
