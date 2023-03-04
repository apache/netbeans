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
package org.netbeans.test.groovy;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.*;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Riha
 */
public class GeneralGroovy extends JellyTestCase {

    static final String JAVA_CATEGORY_NAME = "Java";
    static final String GROOVY_CATEGORY_NAME = "Groovy";
    static final String JAVA_PROJECT_NAME = "Java Application";
    protected static final int COMPLETION_LIST_THRESHOLD = 5000;
    protected static final String GROOVY_EXTENSION = ".groovy";
    protected static final String SAMPLES = "Samples";
    protected static final String SAMPLES_CATEGORY = "Groovy";
    protected EventTool evt;

    public GeneralGroovy(String arg0) {
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
        return getDataDir().getPath();
    }

    protected void createJavaApplication(String projectName) {
        
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 120000);
        NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke();
        opNewProjectWizard.selectCategory(JAVA_CATEGORY_NAME);
        opNewProjectWizard.selectProject(JAVA_PROJECT_NAME);

        opNewProjectWizard.next();

        JDialogOperator jdNew = new JDialogOperator("New Java Application");
        JTextComponentOperator jtName = new JTextComponentOperator(jdNew, 0);
        JTextComponentOperator jcPath = new JTextComponentOperator(jdNew, 1);
        jcPath.setText(GetWorkDir() + File.separator + projectName);


        if (null != projectName) {
            int iSleeps = 0;
            while (!jtName.isEnabled()) {
                if (60 <= ++iSleeps) {
                    fail("Project name disabled during too long time.");
                }
                evt.waitNoEvent(1000);
            }
            jtName.setText(projectName);
        }
        opNewProjectWizard.finish();
        waitScanFinished();
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
            evt.waitNoEvent(100);
        }
    }

    protected void type(EditorOperator edit, String code) {
        int iLimit = code.length();
        for (int i = 0; i < iLimit; i++) {
            edit.typeKey(code.charAt(i));
        }
        evt.waitNoEvent(100);
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

    protected void createJavaFile(String sProject, String sItem, String sName) {
        createJavaFile(sProject, sItem, sName, null);
    }

    protected void createJavaFile(String sProject, String sItem, String sName, String sPackage) {
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(sProject);
        prn.select();
        NewFileWizardOperator.invoke().cancel();

        NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke();
        opNewFileWizard.selectCategory(JAVA_CATEGORY_NAME);
        opNewFileWizard.selectFileType(sItem);
        opNewFileWizard.next();

        JDialogOperator jdNew = new JDialogOperator("New " + sItem);
        JTextComponentOperator jt = new JTextComponentOperator(jdNew, 0);
        if (null != sName) {
            jt.setText(sName);
        } else {
            sName = jt.getText();
        }

        if (sPackage != null) {
            JComboBoxOperator sd = new JComboBoxOperator(jdNew, 1);
            sd.getTextField().setText(sPackage.toLowerCase());
        }
        opNewFileWizard.finish();
        new EditorOperator(sName);
    }

    /**
     * Creates a new Groovy file
     * @param sProject name of project
     * @param sItem type of file ("Groovy Class" etc.)
     * @param sName name of file
     */
    protected void createGroovyFile(String sProject, String sItem, String sName) {
        createGroovyFile(sProject, sItem, sName, null);
    }

    protected void createGroovyFile(String sProject, String sItem, String sName, String sPackage) {
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(sProject);
        prn.select();

        NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke();
        opNewFileWizard.selectCategory(GROOVY_CATEGORY_NAME);
        opNewFileWizard.selectFileType(sItem);
        opNewFileWizard.next();

        JDialogOperator jdNew = new JDialogOperator("New " + sItem);
        JTextComponentOperator jt = new JTextComponentOperator(jdNew, 0);
        if (null != sName) {
            jt.setText(sName);
        } else {
            sName = jt.getText();
        }
        JTextComponentOperator jtp = new JTextComponentOperator(jdNew, 2);
        jtp.setText(sProject.toLowerCase());

        if (sPackage != null) {
            JComboBoxOperator sd = new JComboBoxOperator(jdNew, 1);
            sd.getTextField().setText(sPackage.toLowerCase());
        }
        opNewFileWizard.finish();
        new EditorOperator(sName);
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
        for (String sCode : asIdeal) {
            int iIndex = jlist.findItemIndex(sCode, new CFulltextStringComparator());
            if (-1 == iIndex) {
                try {
                    List list = jlist.getCompletionItems();
                    for (int i = 0; i < list.size(); i++) {

                        completionList += list.get(i) + "\n";
                    }
                } catch (java.lang.Exception ex) {
                    System.out.println("#" + ex.getMessage());
                }
                System.out.println("Unable to find " + sCode + " completion. Completion list is " + completionList);
                fail("Unable to find " + sCode + " completion. Completion list is " + completionList);
            }
        }
    }

    protected void checkCompletionItems(
            CompletionInfo jlist,
            String[] asIdeal) {
        checkCompletionItems(jlist.listItself, asIdeal);
    }

    /**
     * Creates new sample project
     *
     * @param sampleName name of sample
     * @param projectName target project name
     */
    public void createSampleProject(String sampleName, String projectName) {

        NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke();
        opNewProjectWizard.selectCategory(SAMPLES + "|" + SAMPLES_CATEGORY);
        opNewProjectWizard.selectProject(sampleName);
        opNewProjectWizard.next();

        JDialogOperator jdNew = new JDialogOperator("New");
        JTextComponentOperator jtName = new JTextComponentOperator(jdNew, sampleName.replace(" ", "").replace("-", ""));
        jtName.setText(projectName);
        opNewProjectWizard.finish();
        waitScanFinished();
    }

    /**
     * Opens file in editor
     *
     * @param pathAndFileName relative path to Site Root in Projects window,
     * e.g. for Site Root/web/index.html it would be "web|index.html" (| is path
     * separator)
     * @param projectName project name
     */
    public void openFile(String pathAndFileName, String projectName) {
        if (projectName == null) {
            throw new IllegalStateException("YOU MUST OPEN PROJECT FIRST");
        }

        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        Node node = new Node(rootNode, "Source Packages|" + pathAndFileName);
        evt.waitNoEvent(1000);

        if (node.isLeaf()) {
            node.select();
            node.performPopupAction("Open");
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
