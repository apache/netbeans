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
package org.netbeans.modules.php.smarty.editor.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.php.smarty.SmartyFramework;
import org.netbeans.modules.php.smarty.TplTestBase;
import org.netbeans.modules.php.smarty.ui.options.SmartyOptions;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ToggleBlockCommentActionTest extends TplTestBase {

    public ToggleBlockCommentActionTest(String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testSmartyOptionCursor1()throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.SMARTY);
        testCursorInFile("testfiles/toggleComment/smarty_cursor1.tpl");
    }

    public void testSmartyOptionCursor2()throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.SMARTY);
        testCursorInFile("testfiles/toggleComment/smarty_cursor2.tpl");
    }

    public void testSmartyOptionCursor3()throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.SMARTY);
        testCursorInFile("testfiles/toggleComment/smarty_cursor3.tpl");
    }

    public void testSmartyOptionCursor4()throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.SMARTY);
        testCursorInFile("testfiles/toggleComment/smarty_cursor4.tpl");
    }

    public void testSmartyOptionSelection1()throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.SMARTY);
        testSelectionInFile("testfiles/toggleComment/smarty_selection1.tpl");
    }

    public void testSmartyOptionSelection2()throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.SMARTY);
        testSelectionInFile("testfiles/toggleComment/smarty_selection2.tpl");
    }

    public void testSmartyOptionSelection3()throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.SMARTY);
        testSelectionInFile("testfiles/toggleComment/smarty_selection3.tpl");
    }

    public void testSmartyOptionSelection4()throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.SMARTY);
        testSelectionInFile("testfiles/toggleComment/smarty_selection4.tpl");
    }

    public void testSmartyOptionSelection5()throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.SMARTY);
        testSelectionInFile("testfiles/toggleComment/smarty_selection5.tpl");
    }

    public void testSmartyOptionCursorUncomment1()throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.SMARTY);
        testCursorInFile("testfiles/toggleComment/smarty_cursor_uncomment1.tpl");
    }

    public void testSmartyOptionCursorUncomment2()throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.SMARTY);
        testCursorInFile("testfiles/toggleComment/smarty_cursor_uncomment2.tpl");
    }

    public void testSmartyOptionCursorUncomment3()throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.SMARTY);
        testCursorInFile("testfiles/toggleComment/smarty_cursor_uncomment3.tpl");
    }

    public void testSmartyOptionCursorUncomment4()throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.SMARTY);
        testCursorInFile("testfiles/toggleComment/smarty_cursor_uncomment4.tpl");
    }

    public void testSmartyOptionCursorUncomment5()throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.SMARTY);
        testCursorInFile("testfiles/toggleComment/smarty_cursor_uncomment5.tpl");
    }

    public void testContextOptionCursor1()throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.CONTEXT);
        testCursorInFile("testfiles/toggleComment/context_cursor1.tpl");
    }

    public void testContextOptionCursor2()throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.CONTEXT);
        testCursorInFile("testfiles/toggleComment/context_cursor2.tpl");
    }

    public void testContextOptionCursor3()throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.CONTEXT);
        testCursorInFile("testfiles/toggleComment/context_cursor3.tpl");
    }

    public void testContextOptionCursor4()throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.CONTEXT);
        testCursorInFile("testfiles/toggleComment/context_cursor4.tpl");
    }

    public void testContextOptionCursorUncomment1() throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.CONTEXT);
        testCursorInFile("testfiles/toggleComment/context_cursor_uncomment1.tpl");
    }

    public void testContextOptionCursorUncomment2() throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.CONTEXT);
        testCursorInFile("testfiles/toggleComment/context_cursor_uncomment2.tpl");
    }

    public void testContextOptionCursorUncomment3() throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.CONTEXT);
        testCursorInFile("testfiles/toggleComment/context_cursor_uncomment3.tpl");
    }

    public void testContextOptionCursorUncomment4() throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.CONTEXT);
        testCursorInFile("testfiles/toggleComment/context_cursor_uncomment4.tpl");
    }

    public void testContextOptionCursorUncomment5() throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.CONTEXT);
        testCursorInFile("testfiles/toggleComment/context_cursor_uncomment5.tpl");
    }

    public void testContextOptionCursorUncomment6() throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.CONTEXT);
        testCursorInFile("testfiles/toggleComment/context_cursor_uncomment6.tpl");
    }

    public void testContextOptionCursorUncomment7() throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.CONTEXT);
        testCursorInFile("testfiles/toggleComment/context_cursor_uncomment7.tpl");
    }

    public void testIssue224353() throws Exception {
        SmartyOptions.getInstance().setToggleCommentOption(SmartyFramework.ToggleCommentOption.SMARTY);
        testCursorInFile("testfiles/toggleComment/issue224353.tpl");
    }

    protected void testCursorInFile(String file) throws Exception {
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

    protected void testSelectionInFile(String file) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);
        String source = readFile(fo);

        int sourcePosStart = source.indexOf('^');
        int sourcePosEnd = source.lastIndexOf('^') - 1;
        assertNotNull(sourcePosStart);
        String sourceWithoutMarker = source.substring(0, sourcePosStart) + source.substring(sourcePosStart+1);
        sourceWithoutMarker = sourceWithoutMarker.substring(0, sourcePosEnd) + sourceWithoutMarker.substring(sourcePosEnd+1);

        JEditorPane ta = getPane(sourceWithoutMarker);
        ta.setSelectionStart(sourcePosStart);
        ta.setSelectionEnd(sourcePosEnd);
        BaseDocument doc = (BaseDocument) ta.getDocument();

        Action a = new ToggleBlockCommentAction();
        a.actionPerformed(new ActionEvent(ta, 0, null));

        doc.getText(0, doc.getLength());

        String target = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, target, false, ".toggleComment");
    }
}
