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
package org.netbeans.modules.javascript2.editor.qaf;

import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.*;
import org.openide.util.Exceptions;

/**
 *
 * @author vriha
 */
public class GeneralJavaScript extends JellyTestCase {

    public static final String PHP_CATEGORY_NAME = "PHP";
    public static final String PHP_PROJECT_NAME = "PHP Application";
    public static final String OTHER_CATEGORY_NAME = "Other";
    protected EventTool evt;
    public static String currentFile = "";
    public final String TEST_BASE_NAME = "js2_";
    public static int NAME_ITERATOR = 0;
    public static int currentLine = -1;

    public GeneralJavaScript(String arg0) {
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

    public void doTest(EditorOperator eo, int lineNumber) {
        waitScanFinished();
        String rawLine = eo.getText(lineNumber);
        int start = rawLine.indexOf("//cc;");
        String rawConfig = rawLine.substring(start + 2);
        String[] config = rawConfig.split(";");
        eo.setCaretPositionToEndOfLine(Integer.parseInt(config[1]));
        type(eo, config[2]);

        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, config[3].split(","));
        completion.listItself.hideAll();
        eo.setCaretPositionToEndOfLine(eo.getLineNumber());
        String l = eo.getText(eo.getLineNumber());
        for (int i = 0; i < l.length() - 1; i++) {
            eo.pressKey(KeyEvent.VK_BACK_SPACE);
        }
    }

    public void openFile(String fileName, String projectName) {
        if (projectName == null) {
            throw new IllegalStateException("YOU MUST OPEN PROJECT FIRST");
        }
        Logger.getLogger(GeneralJavaScript.class.getName()).log(Level.INFO, "Opening file {0}", fileName);
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        Node node = new Node(rootNode, "Source Files|" + fileName);
        node.select();
        node.performPopupAction("Open");
    }

    protected void createPhpApplication(String projectName) {
        // Create PHP application

        NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke();
        opNewProjectWizard.selectCategory(PHP_CATEGORY_NAME);
        opNewProjectWizard.selectProject(PHP_PROJECT_NAME);

        opNewProjectWizard.next();

        JDialogOperator jdNew = new JDialogOperator("New PHP Project");

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

//        Timeouts t = jcPath.getTimeouts();
//        long lBack = t.getTimeout("JTextComponentOperator.TypeTextTimeout");
//        t.setTimeout("JTextComponentOperator.TypeTextTimeout", 30000);
//        jcPath.setTimeouts(t);
        evt.waitNoEvent(1000);
        jcPath.getTextField().setText(sProjectPath);

//        t.setTimeout("JTextComponentOperator.TypeTextTimeout", lBack);
//        jcPath.setTimeouts(t);
        opNewProjectWizard.finish();
        waitScanFinished();

    }

    protected EditorOperator createWebFile(String fileName, String project, String fileType) {

        NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke();
        opNewFileWizard.selectCategory(OTHER_CATEGORY_NAME);
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

    public void testCompletion(EditorOperator eo, int lineNumber) throws Exception {
        waitScanFinished();
        GeneralJavaScript.currentLine = lineNumber + 1;
        String rawLine = eo.getText(lineNumber);
        int start = rawLine.indexOf("//cc;");
        String rawConfig = rawLine.substring(start + 2);
        String[] config = rawConfig.split(";");
        eo.setCaretPosition(lineNumber + 1, Integer.parseInt(config[1]));
        type(eo, config[2]);
        evt.waitNoEvent(500);
        eo.pressKey(KeyEvent.VK_ESCAPE);
        int back = Integer.parseInt(config[3]);
        for (int i = 0; i < back; i++) {
            eo.pressKey(KeyEvent.VK_LEFT);
        }

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, config[4].split(","));
        completion.listItself.hideAll();

        eo.pressKey(KeyEvent.VK_ESCAPE);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        completion = getCompletion();
        cjo = completion.listItself;
        String negResult = findNonmatchingItems(cjo, config[7].split(","));

        eo.pressKey(KeyEvent.VK_ESCAPE);
        if (config[5].length() > 0 && config[6].length() > 0) {
            String prefix = Character.toString(config[5].charAt(0));
            type(eo, prefix);
            evt.waitNoEvent(50);
            eo.typeKey(' ', InputEvent.CTRL_MASK);
            evt.waitNoEvent(20);
            if (!isSingleOption(prefix, cjo)) {
                completion = getCompletion();
                cjo = completion.listItself;
                checkCompletionMatchesPrefix(cjo.getCompletionItems(), prefix);
                evt.waitNoEvent(500);
                cjo.clickOnItem(config[5]);
                eo.pressKey(KeyEvent.VK_ENTER);
            }

            assertTrue("Wrong completion result: '" + eo.getText(lineNumber + 1) + "'", eo.getText(lineNumber + 1).contains(config[6].replaceAll("\\|", "")));
            completion.listItself.hideAll();
        }

        if (negResult.length() > 0) {
            fail("Completion list contains invalid items: " + negResult);
        }

    }

    public void testGoToDeclaration(EditorOperator eo, int lineNumber) throws Exception {
        waitScanFinished();
        String rawLine = eo.getText(lineNumber);
        int start = rawLine.indexOf("//gt;");
        String rawConfig = rawLine.substring(start + 2);
        String[] config = rawConfig.split(";");
        eo.setCaretPosition(lineNumber, Integer.parseInt(config[1]));

        evt.waitNoEvent(200);
        new org.netbeans.jellytools.actions.Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_B, 2)).performShortcut(eo);
        evt.waitNoEvent(500);
        long defaultTimeout = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        try {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
            EditorOperator ed = new EditorOperator(config[2]);
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", defaultTimeout);
            int position = ed.txtEditorPane().getCaretPosition();
            ed.setCaretPosition(Integer.valueOf(config[3]), Integer.valueOf(config[4].trim()));
            int expectedPosition = ed.txtEditorPane().getCaretPosition();
            assertTrue("Incorrect caret position. Expected position " + expectedPosition + " but was " + position, position == expectedPosition);

            if (!config[2].equals(GeneralJavaScript.currentFile)) {
                ed.close(false);
            }

        } catch (Exception e) {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", defaultTimeout);
            fail(e.getMessage());
        }

    }

    protected void checkCompletionMatchesPrefix(List list, String prefix) {
        StringBuilder sb = new StringBuilder();
        prefix = prefix.toLowerCase();
        String item = "";
        for (int i = 0; i < list.size(); i++) {
            item = list.get(i).toString().toLowerCase();
            if (!item.startsWith(prefix)) {
                sb.append(item).append("\n");
            }
        }

        if (sb.toString().length() > 1) {
            fail("Completion contains nonmatching items for prefix " + prefix + ". Completion list is " + sb.toString());
        }
    }

    protected boolean isSingleOption(String pattern, CompletionJListOperator jList) {
        try {
            pattern = pattern.toLowerCase();
            List items = jList.getCompletionItems();
            Object item;
            int matches = 0;
            for (int i = 0; i < items.size(); i++) {
                item = items.get(i);
                if (item.toString().toLowerCase().startsWith(pattern)) {
                    matches++;
                }
            }
            return matches == 1;
        } catch (Exception ex) {
            return false;
        }
    }

    protected String findNonmatchingItems(CompletionJListOperator jlist, String[] invalidList) {
        StringBuilder sb = new StringBuilder();
        for (String sCode : invalidList) {
            int iIndex = jlist.findItemIndex(sCode, new CFulltextStringComparator());
            if (-1 != iIndex) {
                sb.append(sCode).append(",");

            }
        }
        return sb.toString();
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
        evt.waitNoEvent(1000);
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

    protected void cleanFile(EditorOperator eo) {
        eo.typeKey('a', InputEvent.CTRL_MASK);
        eo.pressKey(java.awt.event.KeyEvent.VK_DELETE);
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
