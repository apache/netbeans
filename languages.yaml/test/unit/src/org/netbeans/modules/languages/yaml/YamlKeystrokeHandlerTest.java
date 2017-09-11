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
