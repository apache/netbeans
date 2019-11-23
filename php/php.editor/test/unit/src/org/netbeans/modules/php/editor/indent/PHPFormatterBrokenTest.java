/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.editor.indent;

import java.util.HashMap;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PHPFormatterBrokenTest extends PHPFormatterTestBase {

    public PHPFormatterBrokenTest(String testName) {
        super(testName);
    }

    public void testIssue197074_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        reformatFileContents("testfiles/formatting/broken/issue197074_01.php", options);
    }

    public void testIssue197074_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        reformatFileContents("testfiles/formatting/broken/issue197074_02.php", options);
    }

    public void testIssue197074_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        reformatFileContents("testfiles/formatting/broken/issue197074_03.php", options);
    }

    public void testIssue197074_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        reformatFileContents("testfiles/formatting/broken/issue197074_04.php", options);
    }

    public void testNetBeans3103SelectedSelf_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/broken/netbeans3103_self_01.php", options);
    }

    public void testNetBeans3103SelectedSelf_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/broken/netbeans3103_self_02.php", options);
    }

    public void testNetBeans3103SelectedParent_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/broken/netbeans3103_parent_01.php", options);
    }

    public void testNetBeans3103SelectedParent_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/broken/netbeans3103_parent_02.php", options);
    }
}
