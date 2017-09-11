/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.languages.yaml;

/**
 *
 * @author Tor Norbye
 */
public class YamlSemanticAnalyzerTest extends YamlTestBase {

    public YamlSemanticAnalyzerTest(String testName) {
        super(testName);
    }

    public void testSemanticRails() throws Exception {
        checkSemantic("testfiles/database.yml");
    }

    public void testSemantic1() throws Exception {
        checkSemantic("testfiles/test1.yaml");
    }

    public void testSemantic2() throws Exception {
        checkSemantic("testfiles/test2.yaml");
    }

    public void testSemantic3() throws Exception {
        checkSemantic("testfiles/test3.yaml");
    }

    public void testSemantic4() throws Exception {
        checkSemantic("testfiles/test4.yaml");
    }

    public void testSemantic5() throws Exception {
        checkSemantic("testfiles/test5.yaml");
    }

    public void testSemantic6() throws Exception {
        checkSemantic("testfiles/test6.yaml");
    }

    public void testSemantic7() throws Exception {
        checkSemantic("testfiles/test7.yaml");
    }

    public void testSemantic8() throws Exception {
        checkSemantic("testfiles/test8.yaml");
    }

    public void testSemantic9() throws Exception {
        checkSemantic("testfiles/test9.yaml");
    }

    public void testSemantic10() throws Exception {
        checkSemantic("testfiles/test10.yaml");
    }

    public void testSemantic11() throws Exception {
        checkSemantic("testfiles/test11.yaml");
    }

    public void testSemanticOmap() throws Exception {
        checkSemantic("testfiles/ordered.yaml");
    }

    public void testErb1() throws Exception {
        checkSemantic("testfiles/fixture.yml");
    }

    public void testErb2() throws Exception {
        checkSemantic("testfiles/fixture2.yml");
    }

    public void testErb3() throws Exception {
        checkSemantic("testfiles/fixture3.yml");
    }

    public void testAdvanced1() throws Exception {
        checkSemantic("testfiles/advanced1.yaml");
    }

    public void test143747() throws Exception {
        checkSemantic("testfiles/unicode.yml");
    }
}
