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
package org.netbeans.modules.javascript2.requirejs;

import org.netbeans.jellytools.JellyTestCase;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Riha
 */
public class GeneralRequire extends JellyTestCase {

    protected EventTool evt;
    public static final String HTML_CATEGORY_NAME = "HTML5";
    public static final String HTML_PROJECT_NAME = "HTML5 Application";
    public static String currentFile;
    public static String currentProject;
    public final String TEST_BASE_NAME = "jsreq_";
    public static int NAME_ITERATOR = 0;

    public GeneralRequire(String arg0) {
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

    public EditorOperator openFile(String filePath, String projectName) {
        if (projectName == null) {
            throw new IllegalStateException("YOU MUST OPEN PROJECT FIRST");
        }
        String requestedFileName = filePath.contains("|") ? filePath.substring(filePath.lastIndexOf("|") + 1) : filePath;

        long defaultTimeout = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        try {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
            EditorOperator ed = new EditorOperator(requestedFileName);
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", defaultTimeout);
            GeneralRequire.currentFile = requestedFileName;
            return ed;
        } catch (Exception e) {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", defaultTimeout);
        }

        Logger.getLogger(GeneralRequire.class.getName()).log(Level.INFO, "Opening file {0}", filePath);
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        Node node = new Node(rootNode, "Site Root|" + filePath);
        node.select();
        node.performPopupAction("Open");
        GeneralRequire.currentFile = requestedFileName;

        return new EditorOperator(GeneralRequire.currentFile);
    }

    protected void createHTML5Application(String projectName) {

        NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke();
        opNewProjectWizard.selectCategory(HTML_CATEGORY_NAME);
        opNewProjectWizard.selectProject(HTML_PROJECT_NAME);

        opNewProjectWizard.next();

        JDialogOperator jdNew = new JDialogOperator("New HTML5 Application");

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

    protected EditorOperator createFile(String fileName, String project, String fileType, String category) {

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
                text = eo.getText(eo.getLineNumber() + iOffset).replace("\r\n", "").replace("\n", "");
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

    protected void checkCompletionHtmlAttrItems(CompletionJListOperator jlist, String[] asIdeal) {
        try {
            List items = jlist.getCompletionItems();
            HashSet<String> completion = new HashSet<>();
            for (Object item : items) {
                completion.add(((HtmlCompletionItem) item).getItemText());
            }
            checkCompletionItems(completion, asIdeal);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
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

    protected void checkCompletionItems(HashSet<String> data, String[] asIdeal) {
        String completionList = "";
        StringBuilder sb = new StringBuilder(":");
        for (String sCode : asIdeal) {
            if (!data.contains(sCode)) {
                sb.append(sCode).append(",");
                if (completionList.length() < 1) {
                    try {
                        Iterator<String> it = data.iterator();
                        while (it.hasNext()) {
                            completionList += it.next() + "\n";
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

    protected void checkCompletionDoesntContainItems(CompletionJListOperator jlist, String[] invalidList) {
        for (String sCode : invalidList) {
            int iIndex = jlist.findItemIndex(sCode, new CFulltextStringComparator());
            if (-1 != iIndex) {
                fail("Completion list contains invalid item:" + sCode);
            }
        }
    }

    protected void checkCompletionDoesntContainHtmlAttrItems(CompletionJListOperator jlist, String[] invalidList) {
        try {
            List items = jlist.getCompletionItems();
            HashSet<String> completion = new HashSet<>();
            for (Object item : items) {
                completion.add(((HtmlCompletionItem) item).getItemText());
            }
            for (String sCode : invalidList) {
                if (completion.contains(sCode)) {
                    fail("Completion list contains invalid item:" + sCode);
                }
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected void checkCompletionItems(
            CompletionInfo jlist,
            String[] asIdeal) {
        checkCompletionItems(jlist.listItself, asIdeal);
    }

    public void testCompletion(EditorOperator eo, int lineNumber) throws Exception {
        //cc;20;ironman3.;0;iteration,date,exec,config;date;da|te;test2,hello2,dog,ironman
        //   0  1   2       3    4                       5    6    7         
        //[0] -  
        //[1] go to line 19
        //[2] type dog2.
        //[3] go 0 times left
        //[4] check completion for items
        //[5] type and check cc to contain matching items and press Enter to select it from cc
        //[6] check line contains [6]
        //[7] check cc does not contain [7] in step 4
        waitScanFinished();
        String rawLine = eo.getText(lineNumber);
        int start = rawLine.indexOf("//cc;");
        String rawConfig = rawLine.substring(start + 2);
        String[] config = rawConfig.split(";");

        String[] position = config[1].split("/");
        int line = Integer.parseInt(position[0], 10);
        eo.setCaretPosition(line, Integer.parseInt(position[1], 10));
        type(eo, config[2]);
        eo.pressKey(KeyEvent.VK_ESCAPE);
        int back = Integer.parseInt(config[3]);
        for (int i = 0; i < back; i++) {
            eo.pressKey(KeyEvent.VK_LEFT);
        }
        eo.pressKey(KeyEvent.VK_ESCAPE);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, config[4].split(","));
        completion.listItself.hideAll();

        eo.pressKey(KeyEvent.VK_ESCAPE);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        completion = getCompletion();
        cjo = completion.listItself;
        checkCompletionDoesntContainItems(cjo, config[7].split(","));

        eo.pressKey(KeyEvent.VK_ESCAPE);
        if (config[5].length() > 0 && config[6].length() > 0) {
            String prefix = Character.toString(config[5].charAt(0));
            type(eo, prefix);
            eo.typeKey(' ', InputEvent.CTRL_MASK);
            completion = getCompletion();
            cjo = completion.listItself;
            checkCompletionMatchesPrefix(cjo.getCompletionItems(), prefix);
            evt.waitNoEvent(500);
            cjo.clickOnItem(config[5]);
            eo.pressKey(KeyEvent.VK_ENTER);
            assertTrue("Wrong completion result", eo.getText(lineNumber).contains(config[6].replaceAll("|", "")));
            completion.listItself.hideAll();
        }

        clearCurrentLine(eo);

    }

    public final void clearCurrentLine(EditorOperator eo) {
        eo.setCaretPositionToEndOfLine(eo.getLineNumber());
        String l = eo.getText(eo.getLineNumber());
        for (int i = 0; i < l.length() - 1; i++) {
            eo.pressKey(KeyEvent.VK_BACK_SPACE);
        }
    }

    public void testGoToDeclaration(EditorOperator eo, int lineNumber) throws Exception {
        waitScanFinished();
        String rawLine = eo.getText(lineNumber);
        int start = rawLine.indexOf("//gt;");
        String rawConfig = rawLine.substring(start + 2);
        String[] config = rawConfig.split(";");
        eo.setCaretPosition(lineNumber, Integer.parseInt(config[1]));
        eo.insert(config[2]);
        eo.pressKey(KeyEvent.VK_ESCAPE);
        int back = Integer.parseInt(config[3]);
        for (int i = 0; i < back; i++) {
            eo.pressKey(KeyEvent.VK_LEFT);
        }
        evt.waitNoEvent(1000);
        new org.netbeans.jellytools.actions.Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_B, 2)).performShortcut(eo);
        evt.waitNoEvent(500);
        try {
            EditorOperator ed = new EditorOperator(config[4]);
            int position = ed.txtEditorPane().getCaretPosition();
            ed.setCaretPosition(Integer.valueOf(config[5]), Integer.valueOf(config[6]));
            int expectedPosition = ed.txtEditorPane().getCaretPosition();
            assertTrue("Incorrect caret position. Expected position " + expectedPosition + " but was " + position, position == expectedPosition);
            ed.close(false);
        } catch (Exception e) {
            fail(e.getMessage());
        }

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
