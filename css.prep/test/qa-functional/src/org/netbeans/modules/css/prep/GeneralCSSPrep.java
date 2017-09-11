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

package org.netbeans.modules.css.prep;

import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.Operator;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Riha (vriha)
 */
public class GeneralCSSPrep extends JellyTestCase{

    public static final String HTML5_CATEGORY_NAME = "HTML5";
    public static final String HTML5_PROJECT_NAME = "HTML5 Application";
    protected EventTool evt;
    public static String currentFile = "";
    public final String TEST_BASE_NAME = "prepcc_";
    public static int NAME_ITERATOR = 0;

    public GeneralCSSPrep(String arg0) {
        super(arg0);
        this.evt = new EventTool();
    }

    protected class CompletionInfo {

        public CompletionJListOperator listItself;
        public List listItems;

        public int size() {
            return listItems.size();
        }

        public void hideAll() {
            CompletionJListOperator.hideAll();
        }
    }

    protected String GetWorkDir() {
        return getDataDir().getPath() + File.separator;
    }

    protected void waitCompletionScanning() {

        CompletionJListOperator comp;
        while (true) {
            try {
                comp = new CompletionJListOperator();
                if (null == comp) {
                    return;
                }
                try {
                    Object o = comp.getCompletionItems().get(0);
                    if (!o.toString().contains("No suggestions")
                            && !o.toString().contains("Scanning in progress...")) {
                        return;
                    }
                    evt.waitNoEvent(100);
                } catch (java.lang.Exception ex) {
                    return;
                }
            } catch (JemmyException ex) {
                return;
            }
        }
    }

     public void openFile(String fileName, String projectName) {
        if (projectName == null) {
            throw new IllegalStateException("YOU MUST OPEN PROJECT FIRST");
        }
        Logger.getLogger(GeneralCSSPrep.class.getName()).log(Level.INFO, "Opening file {0}", fileName);
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        Node node = new Node(rootNode, "Site Root|" + fileName);
        node.select();
        node.performPopupAction("Open");
    }
    
    protected void createHTML5Application(String projectName) {

        NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke();
        opNewProjectWizard.selectCategory(HTML5_CATEGORY_NAME);
        opNewProjectWizard.selectProject(HTML5_PROJECT_NAME);

        opNewProjectWizard.next();

        JDialogOperator jdNew = new JDialogOperator("New ");

        JTextComponentOperator jtName = new JTextComponentOperator(jdNew, 0);
        jtName.setText(projectName);

        String sProjectPath = GetWorkDir() + File.separator + jtName.getText();
        if (sProjectPath.contains(File.separator + File.separator)) {
            sProjectPath = GetWorkDir() + jtName.getText();
        }

        JComboBoxOperator jcPath = new JComboBoxOperator(jdNew, 1);

        int iSleeps = 0;
        while (!jcPath.isEnabled()) {
            if (60 <= ++iSleeps) {
                fail("Project path disabled during too long time.");
            }
            evt.waitNoEvent(200);
        }

        evt.waitNoEvent(1000);
        jcPath.getTextField().setText(sProjectPath);
        opNewProjectWizard.finish();
        waitScanFinished();

    }

    protected EditorOperator createNewFile(String fileName, String project, String category, String fileType) {

        NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke();
        opNewFileWizard.selectCategory(category);
        opNewFileWizard.selectFileType(fileType);
        opNewFileWizard.next();

        JDialogOperator jdNew = new JDialogOperator("New " + fileType);
        JTextComponentOperator jt = new JTextComponentOperator(jdNew, 0);
        if (null != fileName) {
            jt.setText(fileName);
        } else {
            fileName = jt.getText();
        }

        opNewFileWizard.finish();
        return new EditorOperator(fileName);
    }

    protected void type(EditorOperator edit, String code) {
        int iLimit = code.length();
        for (int i = 0; i < iLimit; i++) {
            edit.typeKey(code.charAt(i));
        }
        evt.waitNoEvent(100);
    }

    private class DummyClick implements Runnable {

        private JListOperator list;
        private int index, count;

        public DummyClick(JListOperator l, int i, int j) {
            list = l;
            index = i;
            count = j;
        }

        public void run() {
            list.clickOnItem(index, count);
        }
    }

    protected void clickListItemNoBlock(JListOperator jlList, int iIndex, int iCount) {
        (new Thread(new DummyClick(jlList, iIndex, iCount))).start();
    }

    protected void clickForTextPopup(EditorOperator eo, String menu) {
        JEditorPaneOperator txt = eo.txtEditorPane();
        JEditorPane epane = (JEditorPane) txt.getSource();
        try {
            Rectangle rct = epane.modelToView(epane.getCaretPosition());
            txt.clickForPopup(rct.x, rct.y);
            JPopupMenuOperator popup = new JPopupMenuOperator();
            popup.pushMenu(menu);
        } catch (BadLocationException ex) {
            System.out.println("=== Bad location");
        }
    }

    protected void checkResult(EditorOperator eo, String sCheck) {
        checkResult(eo, sCheck, 0);
    }

    protected void checkResult(EditorOperator eo, String sCheck, int iOffset) {
        String sText = eo.getText(eo.getLineNumber() + iOffset);
        if (-1 == sText.indexOf(sCheck)) {
            log("Trace wrong completion:");
            String text = eo.getText(eo.getLineNumber() + iOffset).replace("\r\n", "").replace("\n", "");
            int count = 0;
            while (!text.isEmpty() && count < 20) {
                eo.pushKey(KeyEvent.VK_Z, KeyEvent.CTRL_MASK);
                text = eo.getText(eo.getLineNumber() + iOffset).replace("\r\n", "").replace("\n", "");;
                log(">>" + text + "<<");
                count++;
            }
            fail("Invalid completion: \"" + sText + "\", should be: \"" + sCheck + "\"");

        }
    }

    protected CompletionInfo getCompletion() {
        CompletionInfo result = new CompletionInfo();
        result.listItself = null;
        int iRedo = 10;
        while (true) {
            try {
                result.listItself = new CompletionJListOperator();
                try {
                    result.listItems = result.listItself.getCompletionItems();
                    Object o = result.listItems.get(0);
                    if (!o.toString().contains("Scanning in progress...")) {
                        return result;
                    }
                    evt.waitNoEvent(1000);
                } catch (java.lang.Exception ex) {
                    return null;
                }
            } catch (JemmyException ex) {
                System.out.println("Wait completion timeout.");
                ex.printStackTrace();
                if (0 == --iRedo) {
                    return null;
                }
            }
        }
    }

    protected Object[] getAnnotations(EditorOperator eOp, int limit) {
        eOp.makeComponentVisible();
        try {
            final EditorOperator eo = new EditorOperator(eOp.getName());
            final int _limit = limit;
            new Waiter(new Waitable() {
                @Override
                public Object actionProduced(Object oper) {
                    return eo.getAnnotations().length > _limit ? Boolean.TRUE : null;
                }

                @Override
                public String getDescription() {
                    return ("Wait parser annotations."); // NOI18N
                }
            }).waitAction(null);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        Object[] anns = eOp.getAnnotations();
        return anns;
    }

    protected void checkCompletionItems(CompletionJListOperator jlist, String[] asIdeal) {
        String completionList = "";
        StringBuilder sb = new StringBuilder(":");
        for (String sCode : asIdeal) {
            sCode = sCode.trim();
            int iIndex = jlist.findItemIndex(sCode, new CFulltextStringComparator());
            if (-1 == iIndex) {
                sb.append(sCode).append(",");
                if (completionList.length() < 1) {
                    try {
                        List list = jlist.getCompletionItems();
                        for (int i = 0; i < list.size(); i++) {

                            completionList += list.get(i) + "\n";
                        }
                    } catch (java.lang.Exception ex) {
                        System.out.println("#" + ex.getMessage());
                    }
                }
            }
        }
        if (sb.toString().length() > 1) {
            fail("Unable to find items " + sb.toString() + ". Completion list is " + completionList);
        }
    }

    protected void checkCompletionDoesntContainItems(CompletionJListOperator jlist, String[] invalidList) {
        for (String sCode : invalidList) {
            int iIndex = jlist.findItemIndex(sCode, new CFulltextStringComparator());
            if (-1 != iIndex) {
                fail("Completion list contains invalid item:" + sCode);
            }
        }
    }

    protected void cleanFile(EditorOperator eo){
        eo.typeKey('a', InputEvent.CTRL_MASK);
        eo.pressKey(java.awt.event.KeyEvent.VK_DELETE);
        eo.save();
    }

    protected void checkCompletionItems(
            CompletionInfo jlist,
            String[] asIdeal) {
        checkCompletionItems(jlist.listItself, asIdeal);
    }

    public class CFulltextStringComparator implements Operator.StringComparator {

        public boolean equals(java.lang.String caption, java.lang.String match) {
            return caption.equals(match);
        }
    }

    public class CStartsStringComparator implements Operator.StringComparator {

        public boolean equals(java.lang.String caption, java.lang.String match) {
            return caption.startsWith(match);
        }
    }
    
}
