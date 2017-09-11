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
public class YamlScannerTest extends YamlTestBase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public YamlScannerTest(String testName) {
        super(testName);
    }

    public void testStructureRails() throws Exception {
        checkStructure("testfiles/database.yml");
    }

    public void testStructure1() throws Exception {
        checkStructure("testfiles/test1.yaml");
    }

    public void testStructure2() throws Exception {
        checkStructure("testfiles/test2.yaml");
    }

    public void testStructure3() throws Exception {
        checkStructure("testfiles/test3.yaml");
    }

    public void testStructure4() throws Exception {
        checkStructure("testfiles/test4.yaml");
    }

    public void testStructure5() throws Exception {
        checkStructure("testfiles/test5.yaml");
    }

    public void testStructure6() throws Exception {
        checkStructure("testfiles/test6.yaml");
    }

    public void testStructure7() throws Exception {
        checkStructure("testfiles/test7.yaml");
    }

    public void testStructure8() throws Exception {
        checkStructure("testfiles/test8.yaml");
    }

    public void testStructure10() throws Exception {
        checkStructure("testfiles/test10.yaml");
    }

    public void testStructure11() throws Exception {
        checkStructure("testfiles/test11.yaml");
    }

    public void testStructureOmap() throws Exception {
        checkStructure("testfiles/ordered.yaml");
    }

    public void testErb1() throws Exception {
        checkStructure("testfiles/fixture.yml");
    }

    public void testErb2() throws Exception {
        checkStructure("testfiles/fixture2.yml");
    }

    public void testErb3() throws Exception {
        checkStructure("testfiles/fixture3.yml");
    }

    public void test143747a() throws Exception {
        checkStructure("testfiles/unicode.yml");
    }

    public void testFolds1() throws Exception {
        checkFolds("testfiles/test1.yaml");
    }

    public void testFolds2() throws Exception {
        checkFolds("testfiles/test2.yaml");
    }

    public void testFolds3() throws Exception {
        checkFolds("testfiles/test3.yaml");
    }

    public void testFolds4() throws Exception {
        checkFolds("testfiles/test4.yaml");
    }

    public void testFolds5() throws Exception {
        checkFolds("testfiles/test5.yaml");
    }

    public void testFolds6() throws Exception {
        checkFolds("testfiles/test6.yaml");
    }

    public void testFolds7() throws Exception {
        checkFolds("testfiles/test7.yaml");
    }

    public void testFolds8() throws Exception {
        checkFolds("testfiles/test8.yaml");
    }

    public void testFolds9() throws Exception {
        checkFolds("testfiles/test9.yaml");
    }

    public void testFolds10() throws Exception {
        checkFolds("testfiles/test10.yaml");
    }

    public void testFolds11() throws Exception {
        checkFolds("testfiles/test11.yaml");
    }

    public void testFoldsOmap() throws Exception {
        checkFolds("testfiles/ordered.yaml");
    }

    public void testErb1Folds() throws Exception {
        checkFolds("testfiles/fixture.yml");
    }

    public void testErb2Folds() throws Exception {
        checkFolds("testfiles/fixture2.yml");
    }

    public void testErb3Folds() throws Exception {
        checkFolds("testfiles/fixture3.yml");
    }

    public void test143747b() throws Exception {
        checkFolds("testfiles/unicode.yml");
    }

    public void testUnicodePositions() throws Exception {
        checkFolds("testfiles/unicode2.yml");
    }

    public void testIssue173769() throws Exception {
        checkStructure("testfiles/issue173769.yaml");
    }
}
