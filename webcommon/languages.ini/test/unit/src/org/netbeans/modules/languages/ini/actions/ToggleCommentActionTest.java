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
package org.netbeans.modules.languages.ini.actions;

import org.netbeans.modules.csl.core.CslEditorKit;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ToggleCommentActionTest extends IniActionsTestBase {

    public ToggleCommentActionTest(String testName) {
        super(testName);
    }

    public void testToggleComment_01() throws Exception {
        testInFile("testfiles/actions/toggleComment/toggle_01.ini");
    }

    public void testToggleComment_02() throws Exception {
        testInFile("testfiles/actions/toggleComment/toggle_02.ini");
    }

    public void testToggleComment_03() throws Exception {
        testInFile("testfiles/actions/toggleComment/toggle_03.ini");
    }

    public void testToggleComment_04() throws Exception {
        testInFile("testfiles/actions/toggleComment/toggle_04.ini");
    }

    protected void testInFile(String file) throws Exception {
        testInFile(file, CslEditorKit.toggleCommentAction);
    }

    @Override
    protected String goldenFileExtension() {
        return ".toggleComment";
    }

}
