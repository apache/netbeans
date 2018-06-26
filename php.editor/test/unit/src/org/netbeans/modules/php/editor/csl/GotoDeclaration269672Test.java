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

public class GotoDeclaration269672Test extends GotoDeclarationTestBase {

    public GotoDeclaration269672Test(String testName) {
        super(testName);
    }

    public void testIssue269672_01() throws Exception {
        checkDeclaration(getTestPath(), "$anon = function() use ($te^sts) {", "$^tests = [];");
    }

    public void testIssue269672_02() throws Exception {
        checkDeclaration(getTestPath(), "$te^sts[0] = [1, 2, 3];", "$^tests = [];");
    }

    public void testIssue269672_03() throws Exception {
        checkDeclaration(getTestPath(), "$^tests[0] = [1, 2, 3];", "$^tests = [];");
    }

    public void testIssue269672_04() throws Exception {
        checkDeclaration(getTestPath(), "var_dump($tes^ts);", "$^tests = [];");
    }

    public void testIssue269672_05() throws Exception {
        checkDeclaration(getTestPath(), "$anon2 = function() use (&$refere^nces) {", "$^references = [];");
    }

    public void testIssue269672_06() throws Exception {
        checkDeclaration(getTestPath(), "$^references[0] = [1, 2, 3];", "$^references = [];");
    }

    public void testIssue269672_07() throws Exception {
        checkDeclaration(getTestPath(), "$refer^ences[0] = [1, 2, 3];", "$^references = [];");
    }

    public void testIssue269672_08() throws Exception {
        checkDeclaration(getTestPath(), "var_dump($reference^s);", "$^references = [];");
    }

    public void testIssue269672_09() throws Exception {
        checkDeclaration(getTestPath(), "$anon3 = function() use (&$f^oo) {", "$^foo = new Foo();");
    }

    public void testIssue269672_10() throws Exception {
        checkDeclaration(getTestPath(), "if($a instanceof $^foo) {", "$^foo = new Foo();");
    }

    public void testIssue269672_11() throws Exception {
        checkDeclaration(getTestPath(), "if($a instanceof $fo^o) {", "$^foo = new Foo();");
    }

    public void testIssue269672_12() throws Exception {
        checkDeclaration(getTestPath(), "echo get_class($f^oo) . PHP_EOL;", "$^foo = new Foo();");
    }

}
