/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.qa.form;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import org.netbeans.jellytools.DocumentsDialogOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.CompileJavaAction;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 * Class with helpers for easy creating jemmy/jelly tests
 *
 * @author Jiri Vagner
 */
public abstract class ExtJellyTestCase extends JellyTestCase {

    private static int MY_WAIT_MOMENT = 500;
    public String _testProjectName = "SampleProject"; // NOI18N
    private String _testPackageName = "data"; // NOI18N
    public static String DELETE_OBJECT_CONFIRM = "Confirm Object Deletion"; // NOI18N
    /* Skip file (JFrame,Frame, JDialog, ...) delete in the end of each test */
    public Boolean DELETE_FILES = true;
    public static String OS = System.getProperty("os.name").toLowerCase();
    private static String handler="";

    public String getTestProjectName() {
        return _testProjectName;
    }

    public String getTestPackageName() {
        return _testPackageName;
    }

    public void setTestProjectName(String newValue) {
        _testProjectName = newValue;
    }

    public void setTestPackageName(String newValue) {
        _testPackageName = newValue;
    }

    /** Constructor required by JUnit */
    public ExtJellyTestCase(String testName) {
        super(testName);
    }

    /** Called before every test case. */
    @Override
    public void setUp() throws IOException {
        openDataProjects(_testProjectName);
        //workdirpath = getWorkDir().getParentFile().getAbsolutePath();
        System.out.println("########  " + getName() + "  #######");
    }

    /**
     * Simple console println
     */
    public void p(String msg) {
        System.out.println(msg);
    }

    /**
     * Simple console println
     */
    public void p(Boolean msg) {
        p(String.valueOf(msg));
    }

    /**
     * Simple console println
     */
    public void p(int msg) {
        p(String.valueOf(msg));
    }

    /**
     * Creates new file using NB New File Wizzard
     * @return name of a new file
     * @param project name of project to create file in
     * @param packageName package for a new file
     * @param category category from first step of new file wizzard
     * @param fileType filetype from first step of new file wizzard
     * @param name name prefix of a new file, timestamp will be added to avoid name clash
     */
    private String createFile(String project, String packageName, String category, String fileType, String name) {
        return createFile(project, packageName, category, fileType, name, null);
    }

    public String getTimeStamp() {
        return String.valueOf(new Date().getTime());
    }

    private String createFile(String project, String packageName, String category, String fileType, String name, String beanName) {
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(project);
        nfwo.selectCategory(category);
        nfwo.selectFileType(fileType);
        nfwo.next();

        String fileName = name + String.valueOf(new Date().getTime());

        if (beanName == null) {
            NewJavaFileNameLocationStepOperator nfnlso = new NewJavaFileNameLocationStepOperator();
            nfnlso.txtObjectName().clearText();
            nfnlso.txtObjectName().typeText(fileName);
            nfnlso.setPackage(packageName);
            nfnlso.finish();
        } else {
            NewBeanFormOperator nbfOp = new NewBeanFormOperator();
            nbfOp.txtClassName().clearText();
            nbfOp.txtClassName().typeText(fileName);

            nbfOp.cboPackage().clearText();
            nbfOp.typePackage(packageName);

            nbfOp.next();

            NewBeanFormSuperclassOperator superOp = new NewBeanFormSuperclassOperator();
            superOp.setSuperclass(beanName);
            superOp.finish();
        }

        // following code avoids issue nr. 60418
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(project);
        prn.select();
        Node formnode = new Node(prn, "Source Packages|" + packageName + "|" + fileName); // NOI18N
        //formnode.select();

        OpenAction openAction = new OpenAction();
        openAction.perform(formnode);
        // end of issue code
        return fileName;
    }

    /**
     * Removes file from actual project and actual test package
     */
    public void removeFile(String fileName) {
        if (DELETE_FILES) {
            ProjectsTabOperator project = new ProjectsTabOperator();
            Node node = new Node(project.tree(), _testProjectName + "|Source Packages|" + _testPackageName + "|" + fileName + ".java"); // NOI18N
            DeleteAction act = new DeleteAction();
            act.performPopup(node);

            //new NbDialogOperator(DELETE_OBJECT_CONFIRM).yes();
            NbDialogOperator op = new NbDialogOperator("Delete");
            new JButtonOperator(op, "Ok").clickMouse();
        }
    }

    /**
     * Adds new bean into palette
     *
     * @param beanFileName
     */
    public void addBean(String beanFileName) {
        Node fileNode = openFile(beanFileName);
        waitAMoment();

        new ActionNoBlock("Tools|Add To Palette...", null).perform(); // NOI18N
        SelectPaletteCategoryOperator op = new SelectPaletteCategoryOperator();
        op.lstPaletteCategories().selectItem(SelectPaletteCategoryOperator.ITEM_SWINGCONTROLS);
        op.lstPaletteCategories().selectItem(SelectPaletteCategoryOperator.ITEM_BEANS);
        op.ok();

        CompileJavaAction compAct = new CompileJavaAction();
        compAct.perform(fileNode);
        waitAMoment();
    }

    /**
     * Open default project
     * @param projectName
     */
    public void openProject(String projectName) throws IOException {
        this._testProjectName = projectName;

        //File projectPath = new File(this.getDataDir() + "/projects/" + _testProjectName);

        //Check if project is not already opened
        ProjectsTabOperator pto = new ProjectsTabOperator().invoke();
        int nodeCount = pto.tree().getChildCount(pto.tree().getRoot());

        for (int i = 0; i < nodeCount; i++) {
            String testNode = pto.tree().getChild(pto.tree().getRoot(), i).toString();

            if (testNode.equals(_testProjectName)) {
                log("Project " + _testProjectName + " has been already opened but should not be");
                return;
            }
        }

        //Open project
        this.openDataProjects(projectName);
        log("Project " + projectName + "was opened");

        //Check if project was opened
        pto.invoke();
        nodeCount = pto.tree().getChildCount(pto.tree().getRoot());
        for (int i = 0; i < nodeCount; i++) {
            String str = pto.tree().getChild(pto.tree().getRoot(), i).toString();
            if (str.equals(projectName)) {
                log("Project " + _testProjectName + " is open. (Ok)");
                return;
            }
        }
        log("Project " + _testProjectName + " is not open, but should be!");
        fail("Project is not open");

    }

    /**
     * Opens file into nb editor
     * @param fileName
     * @return node
     */
    public Node openFile(String fileName) {
        return getProjectFileNode(fileName, true, false);
    }

    /**
     * Gets file node from nb project tree
     * @param fileName
     * @return node
     */
    private Node getProjectFileNode(String filePath, boolean openFileInEditor, boolean containsFilePathPackage) {
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(getTestProjectName());
        prn.select();

        String path = "Source Packages|";
        if (!containsFilePathPackage) {
            path += _testPackageName + "|"; // NOI18N
        }
        path += filePath;

        //p(path);
        Node formnode = new Node(prn, path); // NOI18N
        formnode.setComparator(new Operator.DefaultStringComparator(true, false));
        formnode.select();

        if (openFileInEditor) {
            OpenAction openAction = new OpenAction();
            openAction.perform(formnode);
        }

        return formnode;
    }

    public Node getProjectFileNode(String fileName) {
        return getProjectFileNode(fileName, false, false);
    }

    public Node getProjectFileNode(String fileName, boolean containsFilePathPackage) {
        return getProjectFileNode(fileName, false, containsFilePathPackage);
    }

    public Node getProjectFileNode(String fileName, String packageName) {
        return getProjectFileNode(packageName + "|" + fileName, false, true);
    }

    public String createBeanFormFile(String beanClassName) {
        return createFile(getTestProjectName(), getTestPackageName(), "Swing GUI Forms", "Bean Form", "MyBeanForm", beanClassName); // NOI18N
    }

    /**
     * Creates new JDialog file in project
     * @return new file name
     */
    public String createJDialogFile() {
        return createFile(getTestProjectName(), getTestPackageName(), "Swing GUI Forms", "JDialog Form", "MyJDialog"); // NOI18N
    }

    /**
     * Creates new JFrame file in project
     * @return new file name
     */
    public String createJFrameFile() {
        return createFile(getTestProjectName(), getTestPackageName(), "Swing GUI Forms", "JFrame Form", "MyJFrame"); // NOI18N
    }

    /**
     * Creates new AWT Frame file in project
     * @return new file name
     */
    public String createFrameFile() {
        return createFile(getTestProjectName(), getTestPackageName(), "AWT GUI Forms", "Frame Form", "MyFrame"); // NOI18N
    }

    /**
     * Runs popoup command over node
     * @param popup command, ex.: "Add|Swing|Label"
     * @param node to run action on
     */
    public void runPopupOverNode(String actionName, Node node) {
        Action act = new Action(null, actionName);
        act.setComparator(new Operator.DefaultStringComparator(false, false));
        act.perform(node);
        // p(actionName);
    }

    /**
     * Runs popoup command over node using NoBlockAction
     * @param popup command, ex.: "Add|Swing|Label"
     * @param node to run action on
     */
    public void runNoBlockPopupOverNode(String actionName, Node node) {
        Action act = new ActionNoBlock(null, actionName);
        act.setComparator(new Operator.DefaultStringComparator(false, false));
        act.perform(node);
        // p(actionName);
    }

    /**
     * Runs popup commands over node
     * @param array list of popup commands
     * @param node to run actions on
     */
    public void runPopupOverNode(ArrayList<String> actionNames, Node node, Operator.DefaultStringComparator comparator) {
        for (String actionName : actionNames) {
            Action act = new Action(null, actionName);
            act.setComparator(comparator);
            act.perform(node);
            // p(actionName);
        }
    }

    /**
     * Runs popup commands over node
     * @param array list of popup commands
     * @param node to run actions on
     */
    public void runPopupOverNode(ArrayList<String> actionNames, Node node) {
        runPopupOverNode(actionNames, node, new Operator.DefaultStringComparator(false, false));
    }

    /**
     * Find a substring in a string
     * Test fail() method is called, when code string doesnt contain stringToFind.
     * @param stringToFind string to find
     * @param string to search
     */
    private void findStringInCode(String stringToFind, String code) {
        if (!code.contains(stringToFind)) {
            fail("Missing string \"" + stringToFind + "\" in code."); // NOI18N
        }
    }

    /**
     * Find a strings in a code
     * @param lines array list of strings to find
     * @param designer operator "with text"
     */
    public void findInCode(ArrayList<String> lines, FormDesignerOperator designer) {
        EditorOperator editor = designer.editor();
        String code = editor.getText();

        for (String line : lines) {
            findStringInCode(line, code);
        }
        designer.design();
    }

    /**
     * Find a string in a code
     * @param lines array list of strings to find
     * @param designer operator "with text"
     */
    public void findInCode(String stringToFind, FormDesignerOperator designer) {
        EditorOperator editor = designer.editor();
        findStringInCode(stringToFind, editor.getText());
        designer.design();
    }

    /**
     * Miss a string in a code
     * Test fail() method is called, when code contains stringToFind string
     * @param stringToFind
     * @param designer operator "with text"
     */
    public void missInCode(String stringToFind, FormDesignerOperator designer) {
        EditorOperator editor = designer.editor();

        if (editor.getText().contains(stringToFind)) {
            fail("String \"" + stringToFind + "\" found in code."); // NOI18N
        }
        designer.design();
    }

    /**
     * Calls Jelly waitNoEvent()
     * @param quiet time (miliseconds)
     */
    public static void waitNoEvent(long waitTimeout) {
        new org.netbeans.jemmy.EventTool().waitNoEvent(waitTimeout);
    }

    /**
     * Calls Jelly waitNoEvent() with MY_WAIT_MOMENT
     */
    public static void waitAMoment() {
        waitNoEvent(MY_WAIT_MOMENT);
    }

    /** Find msg string in file
     *
     * @result boolean
     */
    public static boolean findInFile(String msg, String filePath) {
        String content = getContents(new File(filePath));
        return content.indexOf(msg) != -1;
    }

    /**
     * Fetch the entire contents of a text file, and return it in a String.
     * This style of implementation does not throw Exceptions to the caller.
     *
     * @param aFile is a file which already exists and can be read.
     */
    public static String getContents(File aFile) {
        //...checks on aFile are elided
        StringBuffer contents = new StringBuffer();

        //declared here only to make visible to finally clause
        BufferedReader input = null;
        try {
            //use buffering, reading one line at a time
            //FileReader always assumes default encoding is OK!
            input = new BufferedReader(new FileReader(aFile));
            String line = null; //not declared within while loop
            /*
             * readLine is a bit quirky :
             * it returns the content of a line MINUS the newline.
             * it returns null only for the END of the stream.
             * it returns an empty String if two newlines appear in a row.
             */
            while ((line = input.readLine()) != null) {
                contents.append(line);
                contents.append(System.getProperty("line.separator"));
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    //flush and close both "input" and its underlying FileReader
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return contents.toString();
    }

    /** Gets full path to file from "data" package from SampleProject
     * @param short file name with extension (e.g TestFrame.java);
     * @result full path to file (e.g /home/jirka/TestFrame.java)
     */
    public String getFilePathFromDataPackage(String fileName) {
        return getDataDir().getAbsolutePath() + File.separatorChar + getTestProjectName() + File.separatorChar + "src" + File.separatorChar + getTestPackageName() + File.separatorChar + fileName; // NOI18N
    }

    /** Gets text value of jlabel component
     * @return String text 
     */
    public static String getTextValueOfLabel(ComponentInspectorOperator inspector, String nodePath) {
        // invoke properties of component ...
        handler=nodePath;
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector=new ComponentInspectorOperator();
                Node actNode = new Node(inspector.treeComponents(), handler);
                
                ActionNoBlock act = new ActionNoBlock(null, "Properties");  // NOI18N
                act.perform(actNode);
            }
        });


        // get value of property
        NbDialogOperator dialogOp = new NbDialogOperator("[JLabel]");  // NOI18N
        Property prop = new Property(new PropertySheetOperator(dialogOp), "text");  // NOI18N
        String result = prop.getValue();

        // close property dialog
        new JButtonOperator(dialogOp, "Close").push();  // NOI18N
        waitAMoment();

        return result;
    }
    
    public  String getTextValueOfLabelNonStatic(ComponentInspectorOperator inspector, String nodePath) {
        // invoke properties of component ...
        handler=nodePath;
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector=new ComponentInspectorOperator();
                Node actNode = new Node(inspector.treeComponents(), handler);
                runNoBlockPopupOverNode("Properties", actNode);
            }
        });


        // get value of property
        NbDialogOperator dialogOp = new NbDialogOperator("[JLabel]");  // NOI18N
        Property prop = new Property(new PropertySheetOperator(dialogOp), "text");  // NOI18N
        String result = prop.getValue();

        // close property dialog
        new JButtonOperator(dialogOp, "Close").push();  // NOI18N
        waitAMoment();

        return result;
    }


    public void createJDABasicProject() {

        new ActionNoBlock("File|New Project", null).perform(); // NOI18N

        NewProjectWizardOperator op = new NewProjectWizardOperator();
        op.selectProject("Java Desktop Application"); // NOI18N
        op.next();
        op.next();
        NbDialogOperator newJDAOp = new NbDialogOperator("New Desktop Application"); // NOI18N
        new JTextFieldOperator(newJDAOp, 2).typeText(getTestProjectName());
        new JButtonOperator(newJDAOp, "Finish").push(); // NOI18N
    }
    // Method for checking jdk version

    public static String getJDKVersionCode() {
        String specVersion = System.getProperty("java.version");

        if (specVersion.startsWith("1.4")) {
            return "jdk14";
        }

        if (specVersion.startsWith("1.5")) {
            return "jdk15";
        }

        if (specVersion.startsWith("1.6")) {
            return "jdk16";
        }

        if (specVersion.startsWith("1.7")) {
            return "jdk17";
        }

        throw new IllegalStateException("Specification version: " + specVersion + " not recognized.");
    }
}