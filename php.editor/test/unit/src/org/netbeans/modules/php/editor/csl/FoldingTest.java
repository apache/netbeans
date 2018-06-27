/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.csl;

import org.netbeans.modules.php.editor.PHPTestBase;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class FoldingTest extends PHPTestBase {

    public FoldingTest(String testName) {
        super(testName);
    }

    public void testFoldingMethod() throws Exception {
        checkFolds("testfiles/parser/foldingMethod.php");
    }

    public void testFoldingConditionalStatements() throws Exception {
        checkFolds("testfiles/parser/foldingConditionalStatements.php");
    }

    public void testFoldingCycles() throws Exception {
        checkFolds("testfiles/parser/foldingCycles.php");
    }

    public void testFoldingMethod_1() throws Exception {
        checkFolds("testfiles/parser/foldingMethod_1.php");
    }

    public void testFoldingConditionalStatements_1() throws Exception {
        checkFolds("testfiles/parser/foldingConditionalStatements_1.php");
    }

    public void testFoldingCycles_1() throws Exception {
        checkFolds("testfiles/parser/foldingCycles_1.php");
    }

    public void testIssue213616() throws Exception {
        checkFolds("testfiles/parser/issue213616.php");
    }

    public void testIssue216088() throws Exception {
        checkFolds("testfiles/parser/issue216088.php");
    }

    public void testIssue232884() throws Exception {
        checkFolds("testfiles/parser/issue232884.php");
    }

    public void testFinally_01() throws Exception {
        checkFolds("testfiles/parser/finally_01.php");
    }

    public void testFinally_02() throws Exception {
        checkFolds("testfiles/parser/finally_02.php");
    }

    public void testAnonymousClass01() throws Exception {
        checkFolds("testfiles/parser/anonymousClass_01.php");
    }

    // #262471
    public void testArrays() throws Exception {
        checkFolds("testfiles/parser/foldingArrays.php");
    }

    // #254432
    public void testUses() throws Exception {
        checkFolds("testfiles/parser/foldingUses.php");
    }

    // #232600
    public void testPHPTags() throws Exception {
        checkFolds("testfiles/parser/foldingPHPTags.php");
    }

}
