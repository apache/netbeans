/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.actions;

import org.netbeans.modules.csl.core.CslEditorKit;

/**
 *
 * @author Petr Pisl
 */
public class ToggleBlockCommentActionTest extends PHPActionTestBase {

    public ToggleBlockCommentActionTest(String testName) {
        super(testName);
    }

    public void testIssue198269_01()throws Exception {
        testInFile("testfiles/actions/toggleComment/issue198269_01.php");
    }

    public void testIssue198269_02()throws Exception {
        testInFile("testfiles/actions/toggleComment/issue198269_02.php");
    }

    public void testIssue198269_03()throws Exception {
        testInFile("testfiles/actions/toggleComment/issue198269_03.php");
    }

    public void testIssue198269_04()throws Exception {
        testInFile("testfiles/actions/toggleComment/issue198269_04.php");
    }

    public void testIsue207153()throws Exception {
        testInFile("testfiles/actions/toggleComment/issue207153.php");
    }

    public void testIssue213706_01()throws Exception {
        testInFile("testfiles/actions/toggleComment/issue213706_01.php");
    }

    public void testIssue213706_02()throws Exception {
        testInFile("testfiles/actions/toggleComment/issue213706_02.php");
    }

    public void testIssue213706_03()throws Exception {
        testInFile("testfiles/actions/toggleComment/issue213706_03.php");
    }

    public void testIssue218830_01()throws Exception {
        testInFile("testfiles/actions/toggleComment/issue218830_01.php");
    }

    public void testIssue218830_02()throws Exception {
        testInFile("testfiles/actions/toggleComment/issue218830_02.php");
    }

    public void testIssue218830_03()throws Exception {
        testInFile("testfiles/actions/toggleComment/issue218830_03.php");
    }

    public void testIssue218830_04()throws Exception {
        testInFile("testfiles/actions/toggleComment/issue218830_04.php");
    }

    public void testIssue218830_05()throws Exception {
        testInFile("testfiles/actions/toggleComment/issue218830_05.php");
    }

    public void testIssue228768_01() throws Exception {
        testInFile("testfiles/actions/toggleComment/issue228768_01.php");
    }

    public void testIssue228768_02() throws Exception {
        testInFile("testfiles/actions/toggleComment/issue228768_02.php");
    }

    public void testIssue228768_03() throws Exception {
        testInFile("testfiles/actions/toggleComment/issue228768_03.php");
    }

    public void testIssue228768_04() throws Exception {
        testInFile("testfiles/actions/toggleComment/issue228768_04.php");
    }

    public void testIssue228768_05() throws Exception {
        testInFile("testfiles/actions/toggleComment/issue228768_05.php");
    }

    public void testIssue228768_06() throws Exception {
        testInFile("testfiles/actions/toggleComment/issue228768_06.php");
    }

    public void testIssue228731_01() throws Exception {
        testInFile("testfiles/actions/toggleComment/issue228731_01.php");
    }

    public void testIssue228731_02() throws Exception {
        testInFile("testfiles/actions/toggleComment/issue228731_02.php");
    }

    public void testIssue228731_03() throws Exception {
        testInFile("testfiles/actions/toggleComment/issue228731_03.php");
    }

    public void testIssue228731_04() throws Exception {
        testInFile("testfiles/actions/toggleComment/issue228731_04.php");
    }

    public void testIssue228731_05() throws Exception {
        testInFile("testfiles/actions/toggleComment/issue228731_05.php");
    }

    public void testIssue228731_06() throws Exception {
        testInFile("testfiles/actions/toggleComment/issue228731_06.php");
    }

    public void testIssue231715_01() throws Exception {
        testInFile("testfiles/actions/toggleComment/issue231715_01.php");
    }

    public void testIssue231715_02() throws Exception {
        testInFile("testfiles/actions/toggleComment/issue231715_02.php");
    }

    public void testIssue231715_03() throws Exception {
        testInFile("testfiles/actions/toggleComment/issue231715_03.php");
    }

    protected void testInFile(String file) throws Exception {
        testInFile(file, CslEditorKit.toggleCommentAction);
    }

    @Override
    protected String goldenFileExtension() {
        return ".toggleComment";
    }
}
