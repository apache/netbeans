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
package org.netbeans.modules.php.latte.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import org.netbeans.editor.BaseDocument;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ToggleBlockCommentActionTest extends LatteActionsTestBase {

    public ToggleBlockCommentActionTest(String testName) {
        super(testName);
    }

    public void testHtml() throws Exception {
        testInFile("testfiles/actions/toggleComment/testHtml.latte");
    }

    public void testIssue230261_01() throws Exception {
        testInFile("testfiles/actions/toggleComment/testIssue230261_01.latte");
    }

    public void testIssue230261_02() throws Exception {
        testInFile("testfiles/actions/toggleComment/testIssue230261_02.latte");
    }

    public void testIssue230261_03() throws Exception {
        testInFile("testfiles/actions/toggleComment/testIssue230261_03.latte");
    }

    public void testIssue230261_04() throws Exception {
        testInFile("testfiles/actions/toggleComment/testIssue230261_04.latte");
    }

    public void testIssue230261_05() throws Exception {
        testInFile("testfiles/actions/toggleComment/testIssue230261_05.latte");
    }

    public void testIssue230261_06() throws Exception {
        testInFile("testfiles/actions/toggleComment/testIssue230261_06.latte");
    }

    public void testIssue230261_07() throws Exception {
        testInFile("testfiles/actions/toggleComment/testIssue230261_07.latte");
    }

    public void testIssue230261_08() throws Exception {
        testInFile("testfiles/actions/toggleComment/testIssue230261_08.latte");
    }

    public void testIssue230261_09() throws Exception {
        testInFile("testfiles/actions/toggleComment/testIssue230261_09.latte");
    }

    public void testIssue230261_10() throws Exception {
        testInFile("testfiles/actions/toggleComment/testIssue230261_10.latte");
    }

    public void testIssue230261_11() throws Exception {
        testInFile("testfiles/actions/toggleComment/testIssue230261_11.latte");
    }

    public void testIssue230261_12() throws Exception {
        testInFile("testfiles/actions/toggleComment/testIssue230261_12.latte");
    }

    public void testIssue230261_13() throws Exception {
        testInFile("testfiles/actions/toggleComment/testIssue230261_13.latte");
    }

    public void testIssue230261_14() throws Exception {
        testInFile("testfiles/actions/toggleComment/testIssue230261_14.latte");
    }

    public void testIssue230377() throws Exception {
        testInFile("testfiles/actions/toggleComment/testIssue230377.latte");
    }

    private void testInFile(String file) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);
        String source = readFile(fo);

        int sourcePos = source.indexOf('^');
        assertNotNull(sourcePos);
        String sourceWithoutMarker = source.substring(0, sourcePos) + source.substring(sourcePos+1);

        JEditorPane ta = getPane(sourceWithoutMarker);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePos);
        BaseDocument doc = (BaseDocument) ta.getDocument();

        Action a = new ToggleBlockCommentAction();
        a.actionPerformed(new ActionEvent(ta, 0, null));

        doc.getText(0, doc.getLength());
        doc.insertString(caret.getDot(), "^", null);

        String target = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, target, false, ".toggleComment");
    }

}
