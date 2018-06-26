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
package org.netbeans.modules.php.latte.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import static junit.framework.Assert.assertNotNull;
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
