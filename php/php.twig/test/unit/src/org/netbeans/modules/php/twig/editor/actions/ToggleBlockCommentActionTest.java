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
package org.netbeans.modules.php.twig.editor.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.php.twig.editor.ui.options.TwigOptions;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ToggleBlockCommentActionTest extends TwigActionsTestBase {

    public ToggleBlockCommentActionTest(String testName) {
        super(testName);
    }

    public void testWholeLine_01() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.AS_TWIG_EVERYWHERE);
        testInFile("testfiles/actions/toggleComment/testWholeLine_01.twig");
    }

    public void testWholeLine_02() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.AS_TWIG_EVERYWHERE);
        testInFile("testfiles/actions/toggleComment/testWholeLine_02.twig");
    }

    public void testWholeLine_03() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.AS_TWIG_EVERYWHERE);
        testInFile("testfiles/actions/toggleComment/testWholeLine_03.twig");
    }

    public void testWholeLine_04() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.AS_TWIG_EVERYWHERE);
        testInFile("testfiles/actions/toggleComment/testWholeLine_04.twig");
    }

    public void testWholeLine_05() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.AS_TWIG_EVERYWHERE);
        testInFile("testfiles/actions/toggleComment/testWholeLine_05.twig");
    }

    public void testTwigPart_01() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.LANGUAGE_SENSITIVE);
        testInFile("testfiles/actions/toggleComment/testTwigPart_01.twig");
    }

    public void testTwigPart_02() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.LANGUAGE_SENSITIVE);
        testInFile("testfiles/actions/toggleComment/testTwigPart_02.twig");
    }

    public void testTwigPart_03() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.LANGUAGE_SENSITIVE);
        testInFile("testfiles/actions/toggleComment/testTwigPart_03.twig");
    }

    public void testTwigPart_04() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.LANGUAGE_SENSITIVE);
        testInFile("testfiles/actions/toggleComment/testTwigPart_04.twig");
    }

    public void testTwigPart_05() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.LANGUAGE_SENSITIVE);
        testInFile("testfiles/actions/toggleComment/testTwigPart_05.twig");
    }

    public void testUncommentWholeLine_01() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.AS_TWIG_EVERYWHERE);
        testInFile("testfiles/actions/toggleComment/testUncommentWholeLine_01.twig");
    }

    public void testUncommentWholeLine_02() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.AS_TWIG_EVERYWHERE);
        testInFile("testfiles/actions/toggleComment/testUncommentWholeLine_02.twig");
    }

    public void testUncommentWholeLine_03() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.AS_TWIG_EVERYWHERE);
        testInFile("testfiles/actions/toggleComment/testUncommentWholeLine_03.twig");
    }

    public void testUncommentWholeLine_04() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.AS_TWIG_EVERYWHERE);
        testInFile("testfiles/actions/toggleComment/testUncommentWholeLine_04.twig");
    }

    public void testUncommentWholeLine_05() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.AS_TWIG_EVERYWHERE);
        testInFile("testfiles/actions/toggleComment/testUncommentWholeLine_05.twig");
    }

    public void testUncommentWholeLine_06() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.AS_TWIG_EVERYWHERE);
        testInFile("testfiles/actions/toggleComment/testUncommentWholeLine_06.twig");
    }

    public void testUncommentTwigPart_01() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.LANGUAGE_SENSITIVE);
        testInFile("testfiles/actions/toggleComment/testUncommentTwigPart_01.twig");
    }

    public void testUncommentTwigPart_02() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.LANGUAGE_SENSITIVE);
        testInFile("testfiles/actions/toggleComment/testUncommentTwigPart_02.twig");
    }

    public void testUncommentTwigPart_03() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.LANGUAGE_SENSITIVE);
        testInFile("testfiles/actions/toggleComment/testUncommentTwigPart_03.twig");
    }

    public void testUncommentTwigPart_04() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.LANGUAGE_SENSITIVE);
        testInFile("testfiles/actions/toggleComment/testUncommentTwigPart_04.twig");
    }

    public void testUncommentTwigPart_05() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.LANGUAGE_SENSITIVE);
        testInFile("testfiles/actions/toggleComment/testUncommentTwigPart_05.twig");
    }

    public void testCommentPartStart() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.LANGUAGE_SENSITIVE);
        testInFile("testfiles/actions/toggleComment/testCommentPartStart.twig");
    }

    public void testCommentPartEnd() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.LANGUAGE_SENSITIVE);
        testInFile("testfiles/actions/toggleComment/testCommentPartEnd.twig");
    }

    public void testUncommentPartStart() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.LANGUAGE_SENSITIVE);
        testInFile("testfiles/actions/toggleComment/testUncommentPartStart.twig");
    }

    public void testUncommentPartEnd() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.LANGUAGE_SENSITIVE);
        testInFile("testfiles/actions/toggleComment/testUncommentPartEnd.twig");
    }

    public void testIssue234563_01() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.AS_TWIG_EVERYWHERE);
        testSelectionInFile("testfiles/actions/toggleComment/testIssue234563_01.twig");
    }

    public void testIssue234563_02() throws Exception {
        TwigOptions.getInstance().setToggleCommentType(ToggleBlockCommentAction.ToggleCommentType.AS_TWIG_EVERYWHERE);
        testSelectionInFile("testfiles/actions/toggleComment/testIssue234563_02.twig");
    }

    protected void testInFile(String file) throws Exception {
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
        assertNotNull(sourcePosStart);
        int sourcePosEnd = source.lastIndexOf('^');
        assertNotNull(sourcePosEnd);
        String sourceWithoutMarkers = source.substring(0, sourcePosStart) + source.substring(sourcePosStart + 1, sourcePosEnd) + source.substring(sourcePosEnd + 1);

        JEditorPane ta = getPane(sourceWithoutMarkers);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePosStart);
        ta.setSelectionStart(sourcePosStart);
        ta.setSelectionEnd(sourcePosEnd - 1);
        BaseDocument doc = (BaseDocument) ta.getDocument();

        Action a = new ToggleBlockCommentAction();
        a.actionPerformed(new ActionEvent(ta, 0, null));

        doc.getText(0, doc.getLength());
        doc.insertString(caret.getDot(), "^", null);

        String target = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, target, false, ".toggleComment");
    }

}
