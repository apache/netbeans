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
package org.netbeans.test.php;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.openide.util.Utilities;

/**
 *
 * @author michaelnazarov@netbeans.org
 */
public class GeneralPHP extends JellyTestCase {

    // Okey, this is hack and should be removed later
    protected boolean bRandomCheck = false;
    static final String PHP_CATEGORY_NAME = "PHP";
    static final String PHP_PROJECT_NAME = "PHP Application";
    static final String SAMPLES = "Samples";
    static final String PROJECT_RentSymfony = "Rent a Flat - Symfony Framework Sample Application";
    static final String PROJECT_RentZend = "Rent a Flat - Zend Framework Sample Application";
    static final String PROJECT_TodoList = "TodoList - PHP Sample Application";
    protected static final int COMPLETION_LIST_THRESHOLD = 5000;
    protected static final String PHP_EXTENSION = ".php";

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

    protected class CompletionInfo {

        public CompletionJListOperator listItself;
        public List listItems;

        public int size() {
            return listItems.size();
        }

        public void hideAll() {
            listItself.hideAll();
        }
    }

    public GeneralPHP(String arg0) {
        super(arg0);
    }

    public void Dummy() {
        startTest();
        System.out.println("=== DUMMY ===");
        endTest();
    }

    protected String GetWorkDir() {
        return getDataDir().getPath() + File.separator;
    }

    protected void Sleep(int iTime) {
        try {
            Thread.sleep(iTime);
        } catch (InterruptedException ex) {
            System.out.println("=== Interrupted sleep ===");
        }
    }

    protected String CreateSamplePHPApplication(String type) {
        NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke();

        opNewProjectWizard.selectCategory(SAMPLES + "|" + PHP_CATEGORY_NAME);
//        opNewProjectWizard.selectCategory(PHP_CATEGORY_NAME);

        if ("RentZend".equals(type)) {
            opNewProjectWizard.selectProject(PROJECT_RentZend);
        } else if ("TodoList".equals(type)) {
            opNewProjectWizard.selectProject(PROJECT_TodoList);
        } else if ("RentSymfony".equals(type)) {
            opNewProjectWizard.selectProject(PROJECT_RentSymfony);
        }

        opNewProjectWizard.next();

        JDialogOperator jdNew = new JDialogOperator("New PHP Sample Project");
        JTextComponentOperator jtName = new JTextComponentOperator(jdNew, 0);

        String sResult = jtName.getText();

        opNewProjectWizard.finish();
        return sResult;

    }

    // All defaults including name
    protected String CreatePHPApplicationInternal(int iPort) {
        // Create PHP application

        // Workaround for MacOS platform
        // TODO : check platform
        // TODO : remove after normal issue fix
        //NewProjectWizardOperator.invoke().cancel();

        NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke();
        opNewProjectWizard.selectCategory(PHP_CATEGORY_NAME);
        opNewProjectWizard.selectProject(PHP_PROJECT_NAME);

        opNewProjectWizard.next();

        JDialogOperator jdNew = new JDialogOperator("New PHP Project");

        JTextComponentOperator jtName = new JTextComponentOperator(jdNew, 0);

        String sResult = jtName.getText();

        /*
         * JComboBoxOperator jcPath = new JComboBoxOperator( jdNew, 0 );
         *
         * Timeouts t = jcPath.getTimeouts( ); long lBack = t.getTimeout(
         * "JTextComponentOperator.TypeTextTimeout" ); t.setTimeout(
         * "JTextComponentOperator.TypeTextTimeout", 30000 );
         * jcPath.setTimeouts( t );
         *
         * jcPath.enterText( sProjectPath );
         *
         * t.setTimeout( "JTextComponentOperator.TypeTextTimeout", lBack );
         * jcPath.setTimeouts( t );
         */

        //NewProjectNameLocationStepOperator opNewProjectNameLocationStep = new NewProjectNameLocationStepOperator( );
        //opNewProjectNameLocationStep.txtProjectLocation( ).setText( GetWorkDir( ) );

        opNewProjectWizard.next();

        //opNewProjectNameLocationStep.txtProjectName( ).setText( sName );

        if (-1 != iPort) {
            //opNewProjectWizard.next( );

            // Set new port based URL here
            jdNew = new JDialogOperator("New PHP Project");
            JTextComponentOperator jtUrl = new JTextComponentOperator(jdNew, 0);
            String sUrl = jtUrl.getText();
            System.out.println("== Original: " + sUrl);
            sUrl = sUrl.replace("localhost", "localhost:" + iPort);
            System.out.println("== Fixed: " + sUrl);
            jtUrl.setText(sUrl);
        }

        opNewProjectWizard.finish();

        // Wait for warnings
        Sleep(5000);
        try {
            JDialogOperator jdWarning = new JDialogOperator("Warning");
            JButtonOperator jbCancel = new JButtonOperator(jdWarning, "Cancel");
            jbCancel.push();
            jdWarning.waitClosed();
        } catch (JemmyException ex) {
            // No warning? Nice to know.
        }

        return sResult;
    }

    protected String CreatePHPApplicationInternal() {
        return CreatePHPApplicationInternal(-1);
    }

    // All defaults including name
    protected void CreatePHPApplicationInternal(String sProjectName, int iPort) {
        // Create PHP application

        // Workaround for MacOS platform
        // TODO : check platform
        // TODO : remove after normal issue fix
        NewProjectWizardOperator.invoke().cancel();
        Sleep(1000);

        NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke();
        opNewProjectWizard.selectCategory(PHP_CATEGORY_NAME);
        opNewProjectWizard.selectProject(PHP_PROJECT_NAME);

        opNewProjectWizard.next();

        JDialogOperator jdNew = new JDialogOperator("New PHP Project");

        JTextComponentOperator jtName = new JTextComponentOperator(jdNew, 0);

        if (null != sProjectName) {
            int iSleeps = 0;
            while (!jtName.isEnabled()) {
                if (60 <= ++iSleeps) {
                    fail("Project name disabled during too long time.");
                }
                Sleep(1000);
            }
            jtName.setText(sProjectName);
        }

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
            Sleep(1000);
        }

        Timeouts t = jcPath.getTimeouts();
        long lBack = t.getTimeout("JTextComponentOperator.TypeTextTimeout");
        t.setTimeout("JTextComponentOperator.TypeTextTimeout", 30000);
        jcPath.setTimeouts(t);

        jcPath.getTextField().setText(sProjectPath);

        t.setTimeout("JTextComponentOperator.TypeTextTimeout", lBack);
        jcPath.setTimeouts(t);

        //NewProjectNameLocationStepOperator opNewProjectNameLocationStep = new NewProjectNameLocationStepOperator( );
        //opNewProjectNameLocationStep.txtProjectLocation( ).setText( GetWorkDir( ) );

        if (-1 != iPort) {
            //opNewProjectWizard.next( );

            // Set new port based URL here
            jdNew = new JDialogOperator("New PHP Project");
            JTextComponentOperator jtUrl = new JTextComponentOperator(jdNew, 1);
            String sUrl = jtUrl.getText();
            System.out.println("== Original: " + sUrl);
            sUrl = sUrl.replace("localhost", "localhost:" + iPort);
            System.out.println("== Fixed: " + sUrl);
            jtUrl.setText(sUrl);
        }

        opNewProjectWizard.finish();
        waitScanFinished();
    }

    protected void CreatePHPApplicationInternal(String sProjectName) {
        CreatePHPApplicationInternal(sProjectName, -1);
    }

    protected void TypeCode(EditorOperator edit, String code) {
        int iLimit = code.length();
        for (int i = 0; i < iLimit; i++) {
            edit.typeKey(code.charAt(i));
            Sleep(100);
        }
    }

    protected void CheckResult(
            EditorOperator eoPHP,
            String sCheck) {
        CheckResult(eoPHP, sCheck, 0);
    }

    protected void CheckResult(
            EditorOperator eoPHP,
            String sCheck,
            int iOffset) {
        String sText = eoPHP.getText(eoPHP.getLineNumber() + iOffset);

        // Check code completion list
        if (-1 == sText.indexOf(sCheck)) {
            if (bRandomCheck) {
                fail("Invalid completion, looks like issue #153062 still here: \"" + sText + "\", should be: \"" + sCheck + "\"");
            } else {
                log("Trace wrong completion:");
                String text = eoPHP.getText(eoPHP.getLineNumber() + iOffset).replace("\r\n", "").replace("\n", "");
                int count = 0;
                while (!text.isEmpty() && count < 20) {
                    eoPHP.pushKey(KeyEvent.VK_Z, KeyEvent.CTRL_MASK);
                    text = eoPHP.getText(eoPHP.getLineNumber() + iOffset).replace("\r\n", "").replace("\n", "");
                    log(">>" + text + "<<");
                    count++;
                }
                fail("Invalid completion: \"" + sText + "\", should be: \"" + sCheck + "\"");
            }
        }
    }

    protected void CheckResultRegex(
            EditorOperator eoPHP,
            String sCheck) {
        String sText = eoPHP.getText(eoPHP.getLineNumber());

        // Check code completion list
        if (!sText.matches(sCheck)) {
            fail("Invalid completion: \"" + sText + "\", should be: \"" + sCheck + "\"");
        }
    }

    protected void TypeCodeCheckResult(
            EditorOperator eoPHP,
            String sType,
            String sCheck) {
        TypeCodeCheckResult(eoPHP, sType, sCheck, 0);
    }

    protected void TypeCodeCheckResult(
            EditorOperator eoPHP,
            String sType,
            String sCheck,
            int iOffset) {
        TypeCode(eoPHP, sType);
        CheckResult(eoPHP, sCheck, iOffset);
    }

    protected void TypeCodeCheckResultRegex(
            EditorOperator eoPHP,
            String sType,
            String sCheck) {
        TypeCode(eoPHP, sType);
        CheckResultRegex(eoPHP, sCheck);
    }

    protected void CheckResult(EditorOperator eoCode, String[] asCode, int iOffset) {
        for (int i = 0; i < asCode.length; i++) {
            CheckResult(eoCode, asCode[i], iOffset + i);
        }
    }

    private class dummyClick implements Runnable {

        private JListOperator list;
        private int index, count;

        public dummyClick(JListOperator l, int i, int j) {
            list = l;
            index = i;
            count = j;
        }

        public void run() {
            list.clickOnItem(index, count);
        }
    }

    protected void ClickListItemNoBlock(
            JListOperator jlList,
            int iIndex,
            int iCount) {
        (new Thread(new dummyClick(jlList, iIndex, iCount))).start();
    }

    protected void ClickForTextPopup(EditorOperator eo, String menu) {
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

    private void SetTagsSupport(String sTag, String sProject, boolean b) {
        // Open project properties
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(sProject);
        prn.select();
        prn.callPopup();
        JPopupMenuOperator popup = new JPopupMenuOperator();
        popup.pushMenuNoBlock("Properties");
        JDialogOperator jdProperties = new JDialogOperator("Project Properties - ");
        // Set support
        JCheckBoxOperator box = new JCheckBoxOperator(jdProperties, sTag);
        if (box.isSelected() ^ b) {
            box.clickMouse();
        }
        //Sleep( 10000 );
        // Close dialog
        JButtonOperator bOk = new JButtonOperator(jdProperties, "OK");
        bOk.push();
        jdProperties.waitClosed();
    }

    protected void SetPhpVersion(String sProject, int version) {
        // Open project properties
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(sProject);
        prn.select();
        prn.callPopup();
        JPopupMenuOperator popup = new JPopupMenuOperator();
        popup.pushMenuNoBlock("Properties");
        JDialogOperator jdProperties = new JDialogOperator("Project Properties - ");
        // Set support
        JComboBoxOperator box = new JComboBoxOperator(jdProperties, 1);
        switch (version) {
            case 2:
                box.selectItem(0);
                break;
            case 3:
                box.selectItem(1);
                break;
            case 4:
                box.selectItem(2);
                break;
            default:
                box.selectItem(2);
                break;
        }
        //Sleep( 10000 );
        // Close dialog
        JButtonOperator bOk = new JButtonOperator(jdProperties, "OK");
        bOk.push();
        jdProperties.waitClosed();
    }

    protected void SetShortTags(String sProject, boolean b) {
        SetTagsSupport("Allow short tags", sProject, b);
    }

    protected void SetAspTags(String sProject, boolean b) {
        SetTagsSupport("Allow ASP tags", sProject, b);
    }

    protected EditorOperator CreatePHPFile(
            String sProject,
            String sItem,
            String sName) {
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(sProject);
        prn.select();

        // Workaround for MacOS platform
        NewFileWizardOperator.invoke().cancel();

        NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke();
        opNewFileWizard.selectCategory("PHP");
        opNewFileWizard.selectFileType(sItem);
        opNewFileWizard.next();

        JDialogOperator jdNew = new JDialogOperator("New " + sItem);
        JTextComponentOperator jt = new JTextComponentOperator(jdNew, 0);
        if (null != sName) {
            jt.setText(sName);
        } else {
            sName = jt.getText();
        }

        opNewFileWizard.finish();

        // Check created in project tree
        String sPath = sProject + "|Source Files|" + sName;
        prn = pto.getProjectRootNode(sPath);
        prn.select();

        // Check created in editor
        return new EditorOperator(sName);
    }

    protected CompletionInfo GetCompletion() {
        CompletionInfo result = new CompletionInfo();
        result.listItself = null;
        int iRedo = 10;
        while (true) {
            try {
                result.listItself = new CompletionJListOperator();
                try {
                    result.listItems = result.listItself.getCompletionItems();
                    Object o = result.listItems.get(0);
                    if ( //!o.toString( ).contains( "No suggestions" )
                            //&&
                            !o.toString().contains("Scanning in progress...")) {
                        return result;
                    }
                    new EventTool().waitNoEvent(300);
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

    protected void CheckCompletionItems(
            CompletionJListOperator jlist,
            String[] asIdeal) {
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

    protected void CheckCompletionItems(
            CompletionInfo jlist,
            String[] asIdeal) {
        CheckCompletionItems(jlist.listItself, asIdeal);
    }

    protected void Backit(EditorOperator eoPHP, int iCount) {
        for (int i = 0; i < iCount; i++) {
            eoPHP.pushKey(KeyEvent.VK_BACK_SPACE);
        }
    }

    private String Suppress(String sFrom) {
        String sResult = sFrom.replaceAll("[\t\r\n]+", " ");
        sResult = sResult.replaceAll(" +", " ");
        sResult = sResult.replaceAll("^ *", "");
        sResult = sResult.replaceAll(" *$", "");
        sResult = sResult.replaceAll(" *[{] *", "{");
        sResult = sResult.replaceAll(" *[(] *", "(");
        sResult = sResult.replaceAll(" *[}] *", "}");
        sResult = sResult.replaceAll(" *[)] *", ")");
        sResult = sResult.replaceAll(" *= *", "=");
        sResult = sResult.replaceAll(" *, *", ",");
        sResult = sResult.replaceAll(" *; *", ";");

        return sResult;
    }

    protected void CheckFlex(
            EditorOperator eoCode,
            String sIdealCode,
            boolean bDeleteAfter) {
        //System.out.println( "===sIdealCode===" + sIdealCode + "===" );
        // Move up line by line till ideal code starts with
        int iWalkUpLine = eoCode.getLineNumber();
        String sLine;
        while (true) {
            sLine = Suppress(eoCode.getText(iWalkUpLine));
            if (!sLine.equals("")) {
                //System.out.println( "===startwith===" + sLine + "===" );
                if (sIdealCode.startsWith(sLine)) {
                    break;
                }
            }
            iWalkUpLine--;
            if (iWalkUpLine == 0) {
                fail("Unable to find start of text: " + sIdealCode);
            }
        }

        // Move down line by line till whole ideal code found
        int iWalkDownLine = iWalkUpLine + 1;
        while (true) {
            try {
                String sNext = eoCode.getText(iWalkDownLine);
                sLine = Suppress(sLine + sNext);
                //System.out.println( "===" + sLine + "===" );
                if (sIdealCode.equals(sLine)) {
                    break;
                }

                iWalkDownLine++;
            } catch (JemmyException e) {
                fail("End of file reached before ideal code found: " + sIdealCode + " instead found: " + sLine);
            }
        }

        if (bDeleteAfter) {
            for (int i = 0; i < iWalkDownLine - iWalkUpLine + 1; i++) {
                eoCode.deleteLine(iWalkUpLine);
            }
        }
    }

    public boolean DeleteFileContent(EditorOperator eoPHP) {
        eoPHP.setCaretPositionToLine(1);
        while (eoPHP.getText().length() != 0) {
            eoPHP.deleteLine(eoPHP.getLineNumber());
            Sleep(1000);
        }
        return true;
    }

    public JDialogOperator selectPHPFromEditorOptions(int mode, int platform) {

        if (platform != 4096) {
            new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Tools|Options");
        } else {
            new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner|Preferences...");
        }
        Sleep(1000);
        JDialogOperator window = new JDialogOperator("Options");
        window.pressKey(KeyEvent.VK_ESCAPE);
        if (mode == 0) {
            window.pressKey(KeyEvent.VK_RIGHT);
            Sleep(1000);

            for (int i = 0; i <= 4; i++) {
                window.pressKey(KeyEvent.VK_TAB);
                Sleep(1000);
            }
            window.pressKey(KeyEvent.VK_RIGHT);
            Sleep(1000);
            window.pressKey(KeyEvent.VK_SPACE);
            Sleep(1000);
        }
        window.pressKey(KeyEvent.VK_TAB);
        Sleep(1000);
        for (int i = 0; i <= 6; i++) { // in all bundle, PHP is 7th in the list
            window.pressKey(KeyEvent.VK_DOWN);
            Sleep(1000);
        }
       // window.pressKey(KeyEvent.VK_ENTER);
        Sleep(1000);

        return window;
    }

    public void setMethodParametersWrappingOptions(int state) {

        JDialogOperator window = selectPHPFromEditorOptions(1, getPlatform());

        //categories - check if they are all present
        JComboBoxOperator category = new JComboBoxOperator(window, 2);
        category.selectItem(5);

        JComboBoxOperator wrappingCombo = new JComboBoxOperator(window, 2);
        if (state == 0) {
            wrappingCombo.selectItem(2);
        }
        if (state == 1) {
            wrappingCombo.selectItem(0);
        }
        if (state == 2) {
            wrappingCombo.selectItem(1);
        }
        Sleep(10000);
        JButtonOperator jbOK = new JButtonOperator(window, "OK");
        jbOK.push();
        Sleep(1000);
        window.waitClosed();
    }

    public void setPHPIndentation(int initialIndentation, int contIndentation, int arrayDeclarationIndentation) {
        JDialogOperator window = selectPHPFromEditorOptions(1, getPlatform());

        //categories - check if they are all present
        JComboBoxOperator category = new JComboBoxOperator(window, 1);
        category.selectItem("Tabs And Indents");

        JTextFieldOperator initialIndentTextField = new JTextFieldOperator(window, 1);
        initialIndentTextField.clearText();
        initialIndentTextField.enterText(
                String.valueOf(initialIndentation));

        JTextFieldOperator contIndentTextField = new JTextFieldOperator(window, 2);
        contIndentTextField.clearText();
        contIndentTextField.enterText(
                String.valueOf(contIndentation));

        JTextFieldOperator arrayDeclarationIndentTextField = new JTextFieldOperator(window, 1);
        arrayDeclarationIndentTextField.clearText();
        arrayDeclarationIndentTextField.enterText(
                String.valueOf(arrayDeclarationIndentation));


    }

    protected int getPlatform() {
        return Utilities.getOperatingSystem();
    }
}
