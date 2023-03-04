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
