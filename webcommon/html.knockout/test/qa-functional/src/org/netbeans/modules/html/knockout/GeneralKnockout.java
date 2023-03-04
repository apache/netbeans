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
package org.netbeans.modules.html.knockout;

import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.openide.util.Exceptions;
import javax.swing.JTextField;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;

/**
 *
 * @author Vladimir Riha (vriha)
 */
public class GeneralKnockout extends JellyTestCase {

    protected EventTool evt;
    public static final String SAMPLES = "Samples";
    public static final String SAMPLES_CATEGORY = "HTML5";
    public static String currentFile = "";
    public final String TEST_BASE_NAME = "knockout_";
    public static int NAME_ITERATOR = 0;
    public static String originalContent;

    public GeneralKnockout(String args) {
        super(args);
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

    protected HashSet<String> getBindingTypes() {
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
                        HashSet<String> results = new HashSet<>();
                        for (Object d : result.listItems) {
                            results.add(((HtmlCompletionItem) d).getItemText());
                        }
                        return results;

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

    protected void checkCompletionHtmlMatchesPrefix(List list, String prefix) {
        StringBuilder sb = new StringBuilder();
        prefix = prefix.toLowerCase();

        String itemValue;
        for (Object item : list) {

            if (item instanceof HtmlCompletionItem) {
                itemValue = ((HtmlCompletionItem) item).getItemText().toLowerCase();
            } else {
                itemValue = item.toString().toLowerCase();
            }

            if (!itemValue.startsWith(prefix)) {
                sb.append(itemValue).append("\n");
            }
        }

        if (sb.toString().length() > 1) {
            fail("Completion contains nonmatching items for prefix " + prefix + ". Completion list is " + sb.toString());
        }
    }

    protected void checkCompletionHtmlItems(CompletionJListOperator jlist, String[] asIdeal) {
        try {
            List items = jlist.getCompletionItems();
            HashSet<String> completion = new HashSet<>();
            for (Object item : items) {
                if (item instanceof HtmlCompletionItem) {
                    completion.add(((HtmlCompletionItem) item).getItemText());
                } else {
                    completion.add(item.toString());
                }
            }
            checkCompletionItems(completion, asIdeal);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected void checkCompletionDoesntContainHtmlItems(CompletionJListOperator jlist, String[] invalidList) {
        try {
            List items = jlist.getCompletionItems();
            HashSet<String> completion = new HashSet<>();
            for (Object item : items) {
                if (item instanceof HtmlCompletionItem) {
                    completion.add(((HtmlCompletionItem) item).getItemText());
                } else {
                    completion.add(item.toString());
                }
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

    protected int findItemIndex(CompletionJListOperator jlist, String searchedItem) {
        try {
            List items = jlist.getCompletionItems();
            Object item;
            for (int i = 0; i < items.size(); i++) {
                item = items.get(i);
                if (item instanceof HtmlCompletionItem) {
                    if (((HtmlCompletionItem) item).getItemText().equals(searchedItem)) {
                        return i;
                    }
                } else if (item.toString().equals(searchedItem)) {
                    return i;
                }
            }
            return -1;
        } catch (Exception ex) {
            return -1;
        }
    }

    public void navigate(String fromFile, String toFile, int fromLine, int fromColumn, int toLine, int toColumn) {
        EditorOperator eo = new EditorOperator(fromFile);
        eo.setCaretPosition(fromLine, fromColumn);
        evt.waitNoEvent(200);
        MainWindowOperator.getDefault().menuBar().pushMenu("Navigate|Go to Declaration");
        evt.waitNoEvent(500);
        try {
            EditorOperator ed = new EditorOperator(toFile);

            int position = ed.txtEditorPane().getCaretPosition();
            ed.setCaretPosition(toLine, toColumn);
            int expectedPosition = ed.txtEditorPane().getCaretPosition();
            assertTrue("Incorrect caret position. Expected position " + expectedPosition + " but was " + position, position == expectedPosition);
            if (!fromFile.equals(toFile)) {
                ed.close(false);
            }
        } catch (Exception e) {
            fail(e.getMessage());
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
                if (item instanceof HtmlCompletionItem) {
                    if (((HtmlCompletionItem) item).getItemText().toLowerCase().startsWith(pattern)) {
                        matches++;
                    }
                } else if (item.toString().toLowerCase().startsWith(pattern)) {
                    matches++;
                }
            }
            return matches == 1;
        } catch (Exception ex) {
            return false;
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

    public final void cleanLine(EditorOperator eo) {
        eo.setCaretPositionToEndOfLine(eo.getLineNumber());
        String l = eo.getText(eo.getLineNumber());
        for (int i = 0; i < l.length() - 1; i++) {
            eo.pressKey(KeyEvent.VK_BACK_SPACE);
        }
    }

    /**
     * Creates new sample project
     *
     * @param sampleName name of sample
     * @param projectName target project name
     */
    public void createSampleProject(String sampleName, String projectName) {
        setProxy();
        NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke();

        opNewProjectWizard.selectCategory(SAMPLES + "|" + SAMPLES_CATEGORY);
        opNewProjectWizard.selectProject(sampleName);
        opNewProjectWizard.next();

        JDialogOperator jdNew = new JDialogOperator("New HTML5 Sample Project");
        JTextComponentOperator jtName = new JTextComponentOperator(jdNew, 2);

        jtName.setText(projectName);
        evt.waitNoEvent(200);
        opNewProjectWizard.finish();
        evt.waitNoEvent(200);
        waitScanFinished();

    }

    public void openFile(String fileName, String projectName) {
        if (projectName == null) {
            throw new IllegalStateException("YOU MUST OPEN PROJECT FIRST");
        }
        Logger.getLogger(GeneralKnockout.class.getName()).log(Level.INFO, "Opening file {0}", fileName);
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        Node node = new Node(rootNode, "Site Root|" + fileName);
        node.select();
        node.performPopupAction("Open");
    }

    protected void setProxy() {
        OptionsOperator optionsOper = OptionsOperator.invoke();
        optionsOper.selectGeneral();
        // "Manual Proxy Setting"
        new JRadioButtonOperator(optionsOper, "Manual").push();
        // "HTTP Proxy:"
        JLabelOperator jloHost = new JLabelOperator(optionsOper, "HTTP Proxy");
        new JTextFieldOperator((JTextField) jloHost.getLabelFor()).typeText("emea-proxy.uk.oracle.com"); // NOI18N
        // "Port:"
        JLabelOperator jloPort = new JLabelOperator(optionsOper, "Port");
        new JTextFieldOperator((JTextField) jloPort.getLabelFor()).setText("80"); // NOI18N
        optionsOper.ok();
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
