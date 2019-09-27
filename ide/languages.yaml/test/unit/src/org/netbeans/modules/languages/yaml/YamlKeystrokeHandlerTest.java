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
package org.netbeans.modules.languages.yaml;

import javax.swing.text.BadLocationException;

/**
 * Unit tests for YAML keystroke handling
 *
 * @author Tor Norbye
 */
public class YamlKeystrokeHandlerTest extends YamlTestBase {

    public YamlKeystrokeHandlerTest(String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ() {
        // Must run in AWT thread (BaseKit.install() checks for that)
        return true;
    }

    private void insertChar(String original, char insertText, String expected) throws Exception {
        insertChar(original, insertText, expected, null);
    }

    private void insertChar(String original, char insertText, String expected, String selection) throws Exception {
        insertChar(original, insertText, expected, selection, false);
    }

    public void testBreak1() throws Exception {
        insertBreak("test1:^", "test1:\n    ^");
    }

    public void testBreak2() throws Exception {
        insertBreak("test1:^\n", "test1:\n    ^\n");
    }

    public void testBreak3() throws Exception {
        insertBreak("test1:\n  foo^", "test1:\n  foo\n  ^");
    }

    public void testBreak4() throws Exception {
        insertBreak("test1:\n  foo^\n", "test1:\n  foo\n  ^\n");
    }

    public void testBreak5() throws Exception {
        insertBreak("  test1: ^   foo", "  test1: \n  ^foo");
    }

    public void testInsertTag() throws Exception {
        insertChar("<^", '%', "<%^%>");
    }

    public void testInsertTag2a() throws Exception {
        insertChar("<^\n", '%', "<%^%>\n");
    }

    public void testInsertTag2b() throws Exception {
        insertChar("<%^", '%', "<%%^");
    }

    public void testInsertTag2c() throws Exception {
        insertChar("<^f\n", '%', "<%^f\n");
    }

    public void testInsertTag3() throws Exception {
        insertChar("<%%^", '>', "<%%>^");
    }

    public void testInsertTag4() throws Exception {
        insertChar("<%^ ", '%', "<%%^ ");
    }

    public void testInsertTag5b() throws Exception {
        insertChar("<%#%^ ", '>', "<%#%>^ ");
    }

    public void testInsertTag6() throws Exception {
        insertChar("<%^% ", '%', "<%%^% ");
    }

    public void testInsertTag8() throws Exception {
        insertChar("<%^%>", '%', "<%%^>");
    }

    public void testInsertTag9() throws Exception {
        insertChar("<%%^>", '>', "<%%>^");
    }

    public void testInsertTag10() throws Exception {
        insertChar("<%%^> ", '>', "<%%>^ ");
    }

    public void testInsertTag11() throws Exception {
        insertChar("<%foo^%>", '%', "<%foo%^>");
    }

    public void testInsertTag12() throws Exception {
        insertChar("<%foo%^>", '>', "<%foo%>^");
    }

    public void testInsertTag13() throws Exception {
        insertChar("<%foo%^> ", '>', "<%foo%>^ ");
    }

    public void testDeleteTag() throws Exception {
        deleteChar("<%^%>", "<^");
    }

    public void testDeleteTag2() throws Exception {
        deleteChar("<%^%> ", "<^ ");
    }

    public void testDeleteTag3() throws Exception {
        deleteChar("<%^%><div>", "<^<div>");
    }

    public void testInsertX() throws Exception {
        insertChar("c^ass", 'l', "cl^ass");
    }

    public void testDeleteX() throws Exception {
        deleteChar("cl^ass", "c^ass");
    }

    public void testInsertX2() throws Exception {
        insertChar("clas^", 's', "class^");
    }
}
