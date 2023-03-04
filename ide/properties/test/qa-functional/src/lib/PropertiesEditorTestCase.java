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

/*
 * File PropertiesEditorOperator.java
 *
 * Created on 24.9.02 18:18
 *
 * Description :
 *
 * This is operator used in autometed tests. Operator has been
 * writed in Jelly2
 *
 */
package lib;

import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.nodes.*;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.junit.ide.ProjectSupport;
import org.openide.util.Exceptions;

/** Class implementing all necessary methods for handling Property sheet in Editor window.
 * This class is used for automated tests of properties module.
 * @author Petr Felenda ( e-mail petr.felenda@sun.com )
 * @version 1
 */
public class PropertiesEditorTestCase extends JellyTestCase {

    protected static final String DEFAULT_PROJECT_NAME = "properties_test";
    public String projectName;
    private String treeSubPackagePathToFile;
    protected String TREE_SEPARATOR = "|";
    protected String menuSeparator = "#";
    public String defaultPackageDir = "src";
    public String rootPackageName = "Source Packages";
    protected String defaultPackage = "<default package>";
    protected final String WIZARD_CATEGORY = "Other";
    protected final String WIZARD_CATEGORY_FILE = "Other";
    protected final String WIZARD_FILE_TYPE = "Properties File";
    protected final String WIZARD_DEFAULT_PROPERTIES_FILE_NAME = "newproperties";    //variables for New Property dialog
    public JTextFieldOperator KEY;
    public JTextFieldOperator VALUE;
    public JTextFieldOperator COMMENTS;
    /*
     * declaration of members variables
     */
    private final String TITLE_ADD_LOCALE_DIALOG = "";//Bundle.getStringTrimmed("org.netbeans.modules.properties.Bundle", "CTL_NewLocaleTitle");    // String : New Locale
    private final String TITLE_NEW_PROPERTY_DIALOG = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.properties.Bundle", "CTL_NewPropertyTitle");
    private final String TITLE_HELP_DIALOG = "Help";
    private final String TITLE_ERROR_DIALOG = "";//Bundle.getStringTrimmed("org.openide.Bundle", "NTF_ErrorTitle");  // String : Error
    private final String TITLE_SAVE_QUESTION_DIALOG = "";//Bundle.getStringTrimmed("org.openide.text.Bundle", "LBL_SaveFile_Title");    // String : Question
    private final String TITLE_QUESTION_DIALOG = "Question";    // String : Question
    private final String TITLE_CUSTOMIZE_LOCALES_DIALOG = "";//Bundle.getStringTrimmed("org.netbeans.core.Bundle", "CTL_Customizer_dialog_title");  // String : Customizer Dialog
    private final String TITLE_CUSTOMIZE_PROPERTIES_DIALOG = "";//Bundle.getStringTrimmed("org.netbeans.core.Bundle", "CTL_Customizer_dialog_title");   // The same string as last one
    private final String TITLE_DIALOG_CONFIRM_OBJECT_DELETION = "";//Bundle.getStringTrimmed("org.openide.explorer.Bundle", "MSG_ConfirmDeleteObjectTitle");    // String : Confirm Object deletion
    private final String TITLE_DELETE_MORE_LOCALES_CONFIRMATION_DIALOG = "";//Bundle.getStringTrimmed("org.openide.explorer.Bundle", "MSG_ConfirmDeleteObjectsTitle");    // String : Confirm Multiple Object Deletion
    private final String TITLE_PROPERTIES_WINDOW_TABLE = "";//Bundle.getStringTrimmed("org.netbeans.core.Bundle", "CTL_FMT_GlobalProperties");  // String : Properties of
    private final String TITLE_PROPERTIES_WINDOW_TAB = "";//Bundle.getStringTrimmed("org.openide.nodes.Bundle", "Properties");    // String : Properties
    private final String BUTTON_NAME_NEW_PROPERTY = "";//Bundle.getStringTrimmed("org.netbeans.modules.properties.Bundle", "LBL_AddPropertyButton"); // String : New Property
    private final String BUTTON_NAME_REMOVE_PROPERTY = "";//Bundle.getStringTrimmed("org.netbeans.modules.properties.Bundle", "LBL_RemovePropertyButton");   // String : Remove Property
    private final String BUTTON_NAME_REMOVE_LOCALE = "";//Bundle.getStringTrimmed("org.netbeans.modules.properties.Bundle", "CTL_RemoveLocale");   // String : Remove Locale
    private final String BUTTON_NAME_ADD_LOCALE = "";//Bundle.getStringTrimmed("org.netbeans.modules.properties.Bundle", "CTL_AddLocale"); // String : Add Locale
    private final String BUTTON_NAME_ADD_KEY = "";//Bundle.getStringTrimmed("org.netbeans.modules.properties.Bundle", "CTL_AddKey");   // String : Add Key
    private final String BUTTON_NAME_REMOVE_KEY = "";//Bundle.getStringTrimmed("org.netbeans.modules.properties.Bundle", "CTL_RemoveKey"); // String : Remove Key
    private final String BUTTON_NAME_YES = "";//Bundle.getStringTrimmed("org.netbeans.core.Bundle", "YES_OPTION_CAPTION");   // String : Yes
    private final String BUTTON_NAME_NO = "";//Bundle.getStringTrimmed("org.netbeans.core.Bundle", "NO_OPTION_CAPTION"); // String : No
    private final String BUTTON_NAME_DISCARD = "Discard";
    private final String POPUP_MENU_ADD_LOCALE = "";//Bundle.getStringTrimmed("org.openide.actions.Bundle", "NewArg")+Bundle.getStringTrimmed("org.netbeans.modules.properties.Bundle", "LAB_NewLocaleAction");   // String : Add Locale...
    private final String POPUP_MENU_CUSTOMIZE = "";//Bundle.getStringTrimmed("org.openide.actions.Bundle", "Customize");    // String : Customize
    private final String POPUP_MENU_ADD_PROPERTY = "";//Bundle.getStringTrimmed("org.openide.actions.Bundle", "NewArg")+Bundle.getStringTrimmed("org.netbeans.modules.properties.Bundle", "LAB_NewPropertyAction");  // String : Add Property
    private final String POPUP_MENU_DELETE_LOCALE = "";//Bundle.getStringTrimmed("org.openide.actions.Bundle", "Delete");   // String : Delete
    private final String POPUP_MENU_EDIT = "";//Bundle.getStringTrimmed("org.openide.actions.Bundle", "Edit");  // String : Edit
    private final String LABEL_KEY = "";//Bundle.getStringTrimmed("org.netbeans.modules.properties.Bundle", "LBL_KeyLabel"); // String : Key
    private final String LABLE_VALUE = "";//Bundle.getStringTrimmed("org.netbeans.modules.properties.Bundle", "LBL_ValueLabel"); // String : Value
    private final String RESOURCE_BUNDLE_COMMENT = " Sample ResourceBundle properties file\n\n";
    private final String EXCEPTION_TEXT = "Text typing";
    private final String WIZARD_TREE_STRING = "";//Bundle.getStringTrimmed("org.netbeans.modules.text.Bundle", "Templates/Other")+"|"+Bundle.getStringTrimmed("org.netbeans.modules.properties.Bundle", "Templates/Other/properties.properties"); // String : "Other|Properties File";
    public EditorOperator eo;
    public static String BUTTON_NEW_PROPERTY = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.properties.Bundle", "CTL_NewPropertyTitle");
    ;

    /** This constructor only creates operator's object and then does nothing. */
        public PropertiesEditorTestCase(String testMethodName) {
        super(testMethodName);
    }

    /** This method open project.
     * 1) It is checked if the project is open
     * before the project is opened.
     * 2) open project
     * 3) check if project is open
     * @param projectName is name of the project stored in .../editor/test/qa-functional/data/ directory.
     */
    protected void openProject(String projectName) {
        this.projectName = projectName;
        File projectPath = new File(this.getDataDir() + "/projects/", projectName);
        log("data dir = " + this.getDataDir().toString());
        log("project path = " + projectPath.toString());

        /* 1. check if project is open  */
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        log("treecount before = " + pto.tree().getChildCount(pto.tree().getRoot()));
        int childCount = pto.tree().getChildCount(pto.tree().getRoot());
        for (int i = 0; i < childCount; i++) {
            String str = pto.tree().getChild(pto.tree().getRoot(), i).toString();
            log("Found existed project in ProjectView: " + str);
            if (str.equals(projectName)) {
                log("Project " + projectName + " is open, but shoud not be!");
                return;
            }
        }
        try {
            /* 2. open project */
            openDataProjects("projects/" + projectName);
        } catch (IOException ex) {
            fail("Project is not open, probably does not exist");
        }
        log("treecount after  = " + pto.tree().getChildCount(pto.tree().getRoot()));

        /* 3. check the project name */
        ProjectsTabOperator.invoke();
        childCount = pto.tree().getChildCount(pto.tree().getRoot());
        for (int i = 0; i < childCount; i++) {
            String str = pto.tree().getChild(pto.tree().getRoot(), i).toString();
            if (str.equals(projectName)) {
                log("Project " + projectName + " is open. (Ok)");
                return;
            }
        }
        log("Project is not open, but should be!");
        fail("Project is not open");
    }

    protected void openDefaultProject() {
        openProject(DEFAULT_PROJECT_NAME);
    }

    protected Node getDefaultPackageNode() {
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        ProjectRootNode prn = pto.getProjectRootNode(projectName);
        prn.select();
        Node node = new Node(prn, defaultPackage);
        return node;
    }

    protected PropertiesNode getNode(String projectName, String pathToBundle) {
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        ProjectRootNode prn = pto.getProjectRootNode(projectName);
        PropertiesNode node = new PropertiesNode(prn, pathToBundle);
        return node;
    }

    protected PropertiesNode getNodeFilesTab(String projectName, String pathToBundle) {
        FilesTabOperator fto = FilesTabOperator.invoke();
        Node projectNode = fto.getProjectNode(projectName);
        PropertiesNode node = new PropertiesNode(projectNode, pathToBundle);
        return node;
    }

    /** This method opens file in editor. There is used popup menu in explorer.<br>
     * Usage :<br>
     * FilesystemNode filesystemNode = new FilesystemNode(<br>
     *      System.getProperty("netbeans.user") + <br>
     *       File.separator + <br>
     *       "sampledir");<br>
     * <br>
     * openExistedPropertiesFile(filesystemNode.getPath()+"|"+"myFile");<br>
     * @param filePath of existed file in explorer ( without extension )
     */
    // public void openExistedPropertiesFile(String filePath) {
    public void openExistedPropertiesFile(String treeSubPackagePathToFile, String fileName) {
        this.treeSubPackagePathToFile = "Source Packages" + this.TREE_SEPARATOR + treeSubPackagePathToFile;
        ProjectsTabOperator pto = new ProjectsTabOperator();
        pto.invoke();
        ProjectRootNode prn = pto.getProjectRootNode(projectName);
        prn.select();
        Node node = new Node(prn, treeSubPackagePathToFile + TREE_SEPARATOR + fileName);
        node.performPopupAction("Open");
    }

    /** This method opens file in editor. There is used popup menu in explorer for this action.
     * @param filesystemNode of mounted directory ( without extension )
     * @param filePath of file in explorer tree
     */
    public void openExistedPropetiesFileInClassicEditor(Node filesystemNode, String filePath) {
        new EditAction().performPopup(new Node(filesystemNode, filePath));
    }

    /** It creates new property file. There is used popup menu from explorer.
     * @param filesystemNode node of tree, where file will be created
     * @param filePath of file without extension
     */
    public void createNewPropertiesFile(Node node) {
        NewFileWizardOperator newWizard = NewFileWizardOperator.invoke(node, this.WIZARD_CATEGORY, this.WIZARD_FILE_TYPE);
        newWizard.finish();
    }

    public void createNewPropertiesFile(Node node, String packageName, String fileName) {
        NewFileWizardOperator newWizard = NewFileWizardOperator.invoke(node, this.WIZARD_CATEGORY, this.WIZARD_FILE_TYPE);
        NewJavaFileNameLocationStepOperator nfnlso = new NewJavaFileNameLocationStepOperator();
        nfnlso.setObjectName(fileName);
        JTextFieldOperator jtfo = new JTextFieldOperator(nfnlso, 2);
        jtfo.setText(packageName);

        newWizard.finish();
    }

    /** It clicks to the 'New property' button in properties editor ( table view ) */
    public void propertiesEditorClickNewPropertyButton(String fileName) {
        JButtonOperator jButtonOperator = new JButtonOperator(new TopComponentOperator(fileName), this.BUTTON_NAME_NEW_PROPERTY);
        jButtonOperator.pushNoBlock();
        new EventTool().waitNoEvent(250);

    }

    /** This pushs 'Remove property' button in properties editor form. */
    public void propertiesEditorClickRemovePropertyButton(String fileName) {
        JButtonOperator jButtonOperator = new JButtonOperator(new TopComponentOperator(fileName), this.BUTTON_NAME_REMOVE_PROPERTY);
        jButtonOperator.pushNoBlock();
    }

    /** This opens popup menu over tab in properties file and chooses close item from it. */
    public void propertiesEditorCloseFromTabPane() throws Exception {
        throw new Exception("Do not use this method. Not defined in Operator yet");
    }

    /** This deletes properties file from disk. There is used popup menu in Explorer.
     * @param filesystemNode of tree, where file is stored ( without file name )
     * @param filePath of file to delete
     */
    public void deletePropertiesFileFromExplorer(Node filesystemNode, String filePath) {
        PropertiesNode propNode = new PropertiesNode(filesystemNode, filePath);
        propNode.delete();
        new NbDialogOperator(this.TITLE_DIALOG_CONFIRM_OBJECT_DELETION).yes();

    }

    /** This closes properties file. There is used popup menu in Explorer.
     * @param filesystemNode of tree, where file is stored ( without file name )
     * @param filePath of file to delete
     */
    public boolean closePropertiesFile(String fileName) {

        try {
            EditorOperator eo = new EditorOperator(fileName);
            eo.close(false);
            return true;
        } catch (TimeoutExpiredException ex) {
            return false;
        }
    }
    //This closes properties file without back controll
    public void closePropertiesFileWithoutCheck(String fileName) {
        EditorOperator eo = new EditorOperator(fileName);
        eo.close(false);
    }

    /** This close all files in editor. This method should be called in teardown method
     */
    public void closeFiles() {
        eo.closeDiscardAll();
    }

    /** This fill three textafields in 'New property' dialog.
     * @param key which will be filled to appeared dialog 'New property'
     * @param value of key, which will be filled to appeared dialog 'New property'
     * @param comment of key, which will be filles to appeared dialog 'New property'
     * @throws Exception is throws if appeared
     */
    public void newPropertyDialogFill(String fileName, String key, String value, String comment) throws Exception {
        new EventTool().waitNoEvent(250);
        boolean finished = false;
        do {
            NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_NEW_PROPERTY_DIALOG);
            ContainerOperator containerOperator = new ContainerOperator(nbDialogOperator);
            int limit = 0;
            try {

                JTextFieldOperator jTextFieldOperator = null;
                if (key != null) {
                    jTextFieldOperator = new JTextFieldOperator(containerOperator, 0);
                    jTextFieldOperator.typeText(key);
                }
                if (value != null) {
                    jTextFieldOperator = new JTextFieldOperator(containerOperator, 1);
                    jTextFieldOperator.typeText(value);
                }
                if (comment != null) {
                    jTextFieldOperator = new JTextFieldOperator(containerOperator, 2);
                    jTextFieldOperator.typeText(comment);
                }

                finished = true;

            } catch (TimeoutExpiredException ex) {
                //if ( ex.getMessage().equals(this.EXCEPTION_TEXT) == false )
                //    throw new Exception(ex.getMessage());
                // if  problems occurs then use this code to wake up dialog anyway
                // close the dialog and open it anyway
                newPropertyDialogClickCancelButton();
                propertiesEditorClickNewPropertyButton(fileName);
            }

            JButtonOperator bo = new JButtonOperator(containerOperator);
            bo.requestFocus();

            if (limit++ >= 3) {
                finished = true;
            }

        } while (finished == false);
    }

    /** This closes 'New property' dialog. There is used close button for this action in
     * this dialog. ( dialog must be open )
     */
    public void newPropertyDialogClickCloseButton() {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_NEW_PROPERTY_DIALOG);
        //nbDialogOperator.btOK().requestFocus();
        //nbDialogOperator.btOK().pushNoBlock();
        nbDialogOperator.ok();
        System.out.println(">> Ok button pushed");
    }

    /** This closes dialog as in previous method, but there is used no block action for closing dialog. */
    public void newPropertyDialogClickOkButtonNoBlock() {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_NEW_PROPERTY_DIALOG);
        //nbDialogOperator.btOK().pushNoBlock();
        nbDialogOperator.ok();
    }

    /** There will be found adequte key in property sheet and then will be checked value and comment. If kye will not found then method fail.
     * @param key which will be compared with key in properties file
     * @param value of key which will be compared with value in properties file
     * @param comment of key witch will be compared with comment in propertiies file
     * @param rowOfCheckedProperty is number of compared propererty in properties file (row position >=0)
     * @param localeCount is count of locales in properties file, new file has 1 locale (default)
     * @param rowCount is count of rows (properties) in properties file
     * @throws Exception if an appeared
     */
    public void checkPropertiesInSheet(String fileName, String key, String value, String comment, int rowOfCheckedProperty, int localeCount, int rowCount) throws Exception {

        if (key == null) {
            key = "";
        }
        if (value == null) {
            value = "";
        }
        if (comment == null) {
            comment = "";
        }

        // Checking texfields ( Comment and Key ) under table.
        // ==================================================

        TopComponentOperator tco = new TopComponentOperator(fileName);
        JTable jTable = JTableOperator.findJTable(tco.getContainer(ComponentSearcher.getTrueChooser("")), ComponentSearcher.getTrueChooser(""));

        if (rowOfCheckedProperty == 1) {
            // first row is not normal bacause there is comment of file in it + comment of the property
            comment = this.RESOURCE_BUNDLE_COMMENT.concat(comment);
        }

        // click to key field in table

        // WA the next two rows are work around of jemmy because the action clickOnCell+waitEmpty is not
        // WA the same as user event called from mouse, keyboard
        new JTableOperator(jTable).clickOnCell(rowOfCheckedProperty - 1, 1);
        new EventTool().waitNoEvent(500);
        // END OF WA

        new JTableOperator(jTable).clickOnCell(rowOfCheckedProperty - 1, 0);

        JTextArea jTextArea = JTextAreaOperator.findJTextArea(tco.getContainer(ComponentSearcher.getTrueChooser("")), ComponentSearcher.getTrueChooser(""), 0);

        if (jTextArea == null) {
            System.out.println("> Comment textarea is null");
        } else {
            System.out.println("> Comment textarea is found.");

            if (!jTextArea.getText().equals(comment)) {
                System.out.println("textarea == string : \"" + jTextArea.getText() + "\" == \"" + comment + "\"");
                throw new Exception("Comment is not equal to the specified string. (comparing in textarea under the table)");
            } else {
                System.out.println("> Comment is Ok in textarea under the table.");
            }
        }

        if (rowCount == 0) {
            // if label == key then compare key else comapre value
            JLabel jLabel = JLabelOperator.findJLabel(tco.getContainer(ComponentSearcher.getTrueChooser("")), ComponentSearcher.getTrueChooser(""), 1);
            String label = jLabel.getText();
            System.out.println("> label = " + label);
            if (this.LABEL_KEY.equals(label)) {
                jTextArea = JTextAreaOperator.findJTextArea(tco.getContainer(ComponentSearcher.getTrueChooser("")), ComponentSearcher.getTrueChooser(""), 1);
                if (jTextArea == null) {
                    System.out.println("> Key textarea is null");
                } else {
                    System.out.println("> Key textarea is found.");
                    if (!jTextArea.getText().equals(key)) {
                        System.out.println("textarea == string : " + jTextArea.getText() + " == " + key);
                        throw new Exception("Key is not equal to the specified string. (comparing in textarea under the table)");
                    } else {
                        System.out.println("> Key is Ok in textarea under the table.");
                    }
                }
            } else if (this.LABLE_VALUE.equals(label)) {
                jTextArea = JTextAreaOperator.findJTextArea(tco.getContainer(ComponentSearcher.getTrueChooser("")), ComponentSearcher.getTrueChooser(""), 1);
                if (jTextArea == null) {
                    System.out.println("> Value textarea is null");
                } else {
                    System.out.println("> Value textarea is found.");
                    if (!jTextArea.getText().equals(value)) {
                        System.out.println("textarea == string : " + jTextArea.getText() + " == " + value);
                        throw new Exception("Value is not equal to the specified string. (comparing in textarea under the table)");
                    } else {
                        System.out.println("> Value is Ok in textarea under the table.");
                    }
                }

            } else {
                throw new Exception("Label of Key or Value textarea is corrupted!");
            }


        } else {
            System.out.println("More rows in table (rows>0)");
            // comapre key and then compare value
            System.out.println(">Click to row" + rowOfCheckedProperty);
            // click to value field in table
            new JTableOperator(jTable).clickOnCell(rowOfCheckedProperty - 1, 1);

            jTextArea = JTextAreaOperator.findJTextArea(tco.getContainer(ComponentSearcher.getTrueChooser("")), ComponentSearcher.getTrueChooser(""), 1);
            if (jTextArea == null) {
                System.out.println("> Value textarea is null");
            } else {
                System.out.println("> Value textarea is found.");
                if (!jTextArea.getText().equals(value)) {
                    System.out.println("textarea == string : " + jTextArea.getText() + " == " + value);
                    throw new Exception("Value is not equal to the specified string. (comparing in textarea under the table)");
                } else {
                    System.out.println("> Value is Ok in textarea under the table.");
                }
            }
            // click to key fild in table
            new JTableOperator(jTable).clickOnCell(rowOfCheckedProperty - 1, 0);

            jTextArea = JTextAreaOperator.findJTextArea(tco.getContainer(ComponentSearcher.getTrueChooser("")), ComponentSearcher.getTrueChooser(""), 1);
            if (jTextArea == null) {
                System.out.println("> Key textarea is null");
            } else {
                System.out.println("> Key textarea is found.");
                if (!jTextArea.getText().equals(key)) {
                    System.out.println("textarea == string : " + jTextArea.getText() + " == " + key);
                    throw new Exception("Key is not equal to the specified string. (comparing in textarea under the table)");
                } else {
                    System.out.println("> Key is Ok in textarea under the table.");
                }
            }

        }

        // Checking table

        new EventTool().waitNoEvent(250);

        // find in table if any key which matchs to the parametr
        int index = -1;

        if (jTable != null) {

            if (jTable.getRowCount() == rowCount && rowCount == 0) {
                System.out.println("> There is no rows in table. (OK)");
                return;
            } else if (rowCount == 0) {
                System.out.println("> Table shouldn't have any row!(There is " + jTable.getRowCount() + " rows)");
            }

            if (key != null) {
                for (int ii = 0; ii < jTable.getModel().getRowCount(); ii++) {
                    // check if is the key in properties file more times
                    new EventTool().waitNoEvent(250);
                    if (index >= 0 && jTable.getModel().getValueAt(ii, 0).toString().equals(key) == true) {
                        throw new Exception("The added key is more times in properties sheet.");
                    }

                    if (index == -1 && jTable.getModel().getValueAt(ii, 0).toString().toString().equals(key) == true) {
                        index = ii;
                        // check value of property if is equal to the value
                        if (jTable.getModel().getValueAt(ii, 1).toString().toString().equals(value) == false) {
                            throw new Exception("Value doesn't match. " + ii + " - (" + value + " not equals " + jTable.getCellEditor(ii, 1).getCellEditorValue().toString());
                        }
                        // check comment
                        new JTableOperator(jTable).clickOnCell(ii, 0);
                        new EventTool().waitNoEvent(250);

                        // check comment of property if is equal to the comment
                        if (new JTextAreaOperator(new ContainerOperator(tco.getContainer(ComponentSearcher.getTrueChooser(""))), 0).getText().equals(comment) == false) {
                            throw new Exception("Comment doesn't match. " + ii + " : \n>" + comment + "< not equals with : >" + new JTextAreaOperator(new ContainerOperator(tco.getContainer(ComponentSearcher.getTrueChooser(""))), 0).getText() + "<\n");
                        }

                        if (ii + 1 != rowOfCheckedProperty) {
                            throw new Exception("Property is not in the right row in property sheet.");
                        }
                    }

                }
            }

            if (index < 0 && rowOfCheckedProperty >= 0) {
                throw new Exception("Key not found in properties sheet");
            }

            if (localeCount >= 0 && localeCount != jTable.getModel().getColumnCount() - 1) {
                int locales = jTable.getModel().getColumnCount() - 1;
                throw new Exception("Count of locales (columns-1) doesn't match. (" + localeCount + "<>" + locales + ")");
            }

            if (rowCount >= 0 && rowCount != jTable.getModel().getRowCount()) {
                throw new Exception("Count of properties (rows) doesn't match. (" + rowCount + "<>" + jTable.getModel().getRowCount() + ")");
            }

        } else {
            throw new Exception("Cann't find JTable in Editor Window");
        }
    }

    /** This method check pleces of keys, values and comments in properties file (in
     * text mode)
     * @throws Exception if an appeared
     */
    public void checkPropertiesFileIntegrityInClassicEditor(String fileName) throws Exception {
        // open properties file in classic editor and them chek spaces, comments, keys, values
        // and their positions
        EditorOperator eo = new EditorOperator(fileName);
        String content = eo.getText();
        StringTokenizer strT = new StringTokenizer(content, "\n", true);
        String row = "";
        try {
            for (int ii = 0; ii < strT.countTokens(); ii++) {
                // comment of file
                if (ii == 0) {
                    row = strT.nextToken();

                    if (row.charAt(0) != '#') {
                        throw new Exception("Structure of properties file has been corrupted");
                    }
                    // end row
                    row = strT.nextToken();

                    try {
                        // free row or end of file
                        row = strT.nextToken();
                    } catch (NoSuchElementException ex) {
                        // no properties in file
                        return;
                    }

                }

                // comment of property
                row = strT.nextToken();

                // comment of property
                if (row.charAt(0) != '#') {
                    throw new Exception("Structure of properties file has been corrupted");
                }

                // end row
                row = strT.nextToken();


                // key and value of property
                row = strT.nextToken();
                StringTokenizer strT2 = new StringTokenizer(row);
                String key = strT2.nextToken("=\n");
                String value = strT2.nextToken("=\n");
                if (key == null || value == null) {
                    throw new Exception("Structure of properties file has been corrupted");
                }
            }
        } catch (NoSuchElementException ex) {
            throw new Exception("Structure of properties file has been corrupted");
        }

    }

    /** It clicks on 'Help' button in 'New property' dialog. */
    public void newPropertyDialogClickHelpButton() {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_NEW_PROPERTY_DIALOG);
        nbDialogOperator.help();
    }

    /** Closes 'Help' window. */
    public void closeHelp() {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_HELP_DIALOG);
        nbDialogOperator.close();
    }

    /** Closes 'Error' dialog. This dialog appeared after property is added with existed key. (Key which
     * exists in properties file)
     */
    public void errorDialogClickOkButton() {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_ERROR_DIALOG);
        nbDialogOperator.ok();
    }

    /** Closes 'New property' dialog. There is used 'Cancel' button. */
    public void newPropertyDialogClickCancelButton() {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_NEW_PROPERTY_DIALOG);
        nbDialogOperator.cancel();
    }

    /** This selectes row in sheet of properties in properties editor.
     * @param rowNumber is number of row in property sheet
     * @throws Exception if an appeared
     */
    public void selectPropertiesFileItem(String fileName, int rowNumber) throws Exception {
        TopComponentOperator tco = new TopComponentOperator(fileName);

        JTable jTable = JTableOperator.findJTable(tco.getContainer(ComponentSearcher.getTrueChooser("")), ComponentSearcher.getTrueChooser(""));
        new EventTool().waitNoEvent(250);
        // find in table if any key match to the parametr

        if (jTable != null) {
            JTableOperator jTableOperator = new JTableOperator(jTable);
            jTableOperator.getTimeouts().setTimeout("JScrollBarOperator.WholeScrollTimeout", 240000);
            jTableOperator.clickOnCell(rowNumber, 1);
            return;
        }
        throw new Exception("Info : Cannot select item id properties table.");
    }

    /** There will be counted rows in property sheet in properties editor.
     * @return counts of rows in property sheet
     *
     * @throws Exception if an appeared
     */
    public int getPropertiesFileItemsCount(String fileName) throws Exception {
        TopComponentOperator tco = new TopComponentOperator(fileName);

        JTable jTable = JTableOperator.findJTable(tco.getContainer(ComponentSearcher.getTrueChooser("")), ComponentSearcher.getTrueChooser(""));
        new EventTool().waitNoEvent(250);
        // find in table if any key match to the parametr

        if (jTable != null) {
            return jTable.getModel().getRowCount();
        }
        return -1;

    }

    /** Clicks to button 'Ok' in Quiestion dialog. This dialog appeared after pushing 'Remove Property' button. */
    public void questionDialogClickOkButton() {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_SAVE_QUESTION_DIALOG);
        nbDialogOperator.ok();
    }

    public void questionDialogClickDiscardButton() {
        QuestionDialogOperator qdo = new QuestionDialogOperator(TITLE_QUESTION_DIALOG);
        JButtonOperator jbo = new JButtonOperator(qdo, BUTTON_NAME_DISCARD);
        jbo.pushNoBlock();
    }

    /** Clicks to 'Cancel' button in 'Question' dialog. This dialog appeared after pushing 'Remove Property' button in
     * properties editor.
     */
    public void questionDialogClickCancelButton() {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_SAVE_QUESTION_DIALOG);
        nbDialogOperator.cancel();
    }

    /** Method createNewPropertiesFileToClassicEditor()
     * @param filesystemNode is node of filesystem in which is file located in Explorer
     * @param fileName is name of file
     */
    public void createNewPropertiesFileAndOpenInClassicEditor(Node node, String fileName) {
        // create new file
        NewFileWizardOperator newWizard = NewFileWizardOperator.invoke(node, this.WIZARD_CATEGORY, this.WIZARD_FILE_TYPE);
        new NewJavaFileNameLocationStepOperator().setName(fileName);
        newWizard.finish();

        new EventTool().waitNoEvent(250);

        // close Properties Editor
        TopComponentOperator tco = new TopComponentOperator(fileName);
        tco.close();
        // There is problem while writing tests becasuse we must have opened some source files.
        //new EditorWindowOperator().close();
        // open classic editor from popup menu in Explorer window
        //ExplorerOperator explorerOperator = new ExplorerOperator();
        //RepositoryTabOperator eplorer = new RepositoryTabOperator();
        ProjectsTabOperator pto = new ProjectsTabOperator();
        pto.invoke();
        ProjectRootNode prn = pto.getProjectRootNode(projectName);
        prn.select();
        String packageName = node.getPath();
        new Node(prn, "Source Packages" + this.TREE_SEPARATOR + packageName + this.TREE_SEPARATOR + fileName).performPopupAction(this.POPUP_MENU_EDIT);

    }

    /** Open 'Add Locale..' dialog and do nothing
     * @param filesystemNode is node of filesystem in which is file located in Explorer
     * @param filePath is path to file in tree in Explorer
     */
    public void openAddLocaleDialogFromExplorer(Node filesystemNode, String filePath) {
        new ActionNoBlock(null, this.POPUP_MENU_ADD_LOCALE).performPopup(new Node(filesystemNode, filePath));
    }

    /** Fills values to 'Add Locale...' dialog. ( use null values if nothig to be filed )
     * @param languageCode is Language Code e.g. cs, de, en or other staff
     * @param countryCode is Country Code e.g. CZ, DE, US or other stuff
     * @param variant is variant of locale, this should be empty or should will have value e.g. EURO
     * @param filesystemNode is node of tree in explorer
     * @param fileName is name of file
     * @throws Exception if an appeared
     */
    public void addLocaleDialogFill(String languageCode, String countryCode, String variant, Node filesystemNode, String fileName) throws Exception {
        new EventTool().waitNoEvent(250);
        boolean finished = false;
        do {
            try {
                NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_ADD_LOCALE_DIALOG);
                //ContainerOperator containerOperator = new ContainerOperator(nbDialogOperator);
                JComboBoxOperator jComboBoxOperator;
                jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
                if (languageCode != null) {
                    jComboBoxOperator.typeText(languageCode);
                } else {
                    jComboBoxOperator.typeText("");
                }
                jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
                if (countryCode != null) {
                    jComboBoxOperator.typeText(countryCode);
                } else {
                    jComboBoxOperator.typeText("");
                }
                jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 2);
                if (variant != null) {
                    jComboBoxOperator.typeText(variant);
                } else {
                    jComboBoxOperator.typeText("");
                }
                finished = true;
                System.out.println("Set languge: " + new JComboBoxOperator(nbDialogOperator, 0).getTextField().getText());
                System.out.println("Set country: " + new JComboBoxOperator(nbDialogOperator, 1).getTextField().getText());
            //nbDialogOperator.ge
            } catch (TimeoutExpiredException ex) {
                if (ex.getMessage().equals(this.EXCEPTION_TEXT) == false) {
                    throw new Exception(ex.getMessage());
                }
                // if  problems occurs then use this code to wake up dialog anyway
                // close the dialog and open it anyway
                addLocaleDialogClickCancelButton();
                openAddLocaleDialogFromExplorer(filesystemNode, fileName);
            }

        } while (finished == false);
        new EventTool().waitNoEvent(250);

    }

    /** Clicks to 'Help' button in 'Add Locale...' dialog. */
    public void addLocaleDialogClickHelpButton() {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_ADD_LOCALE_DIALOG);
        nbDialogOperator.help();
    }

    /** Clicks to 'Ok' button in 'Add Locale...' dialog. */
    public void addLocaleDialogClickOkButtonNoBlock() {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_ADD_LOCALE_DIALOG);
        //nbDialogOperator.btOK().requestFocus();
        //nbDialogOperator.btOK().pushNoBlock();
        nbDialogOperator.ok();
    }

    /** Clicks to 'Cancel' button in 'Add Locale...' dialog. */
    public void addLocaleDialogClickCancelButton() {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_ADD_LOCALE_DIALOG);
        nbDialogOperator.cancel();
    }

    /** Opens Customizer dialog.
     * @param filesystemNode node of filesystem in tree in explorer
     * @param path to the object you can customize
     */
    public void openCustomizeLocalesDialogFromExplorer(Node filesystemNode, String path) {
        new ActionNoBlock(null, this.POPUP_MENU_CUSTOMIZE).performPopup(new Node(filesystemNode, path));
    }

    /** Clicks to 'Add Locale...' button in customizer. */
    public void customizeLocalesDialogClickAddLocaleButton() {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_CUSTOMIZE_LOCALES_DIALOG);
        JButtonOperator jButtonOperator = new JButtonOperator(new ContainerOperator(nbDialogOperator), this.BUTTON_NAME_ADD_LOCALE);
        jButtonOperator.pushNoBlock();
    }

    /** Clicks to 'Remove Locale' button in customizer. */
    public void customizeLocalesDialogClickRemoveLocaleButton() {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_CUSTOMIZE_LOCALES_DIALOG);
        JButtonOperator jButtonOperator = new JButtonOperator(new ContainerOperator(nbDialogOperator), this.BUTTON_NAME_REMOVE_LOCALE);
        jButtonOperator.pushNoBlock();
    }

    /** Clicks to 'Help' button in customizer. */
    public void customizeLocalesDialogClickHelpButton() {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_CUSTOMIZE_LOCALES_DIALOG);
        //nbDialogOperator.btHelp().pushNoBlock();
        nbDialogOperator.help();
    }

    /** Clicks to 'Close' button in customizer. */
    public void customizeLocalesDialogClickCloseButton() {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_CUSTOMIZE_LOCALES_DIALOG);
        //nbDialogOperator.btClose().pushNoBlock();
        nbDialogOperator.close();
    }

    /** Selects focus adequate to locale in customizer
     * @param row of locale you wish to select
     */
    public void customizeLocalesDialogSelectLocale(int row) {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_CUSTOMIZE_LOCALES_DIALOG);
        JListOperator jListOperator = new JListOperator(new ContainerOperator(nbDialogOperator));
        jListOperator.selectItem(row - 1);
    }

    /** This checks if has been selected 'Remove Locale' button.
     * @return true if locale button has been selected
     */
    public boolean customizeLocalesDialogIsEnabledRemoveLocaleButton() {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_CUSTOMIZE_LOCALES_DIALOG);
        JButtonOperator jButtonOperator = new JButtonOperator(new ContainerOperator(nbDialogOperator), this.BUTTON_NAME_REMOVE_LOCALE);
        return jButtonOperator.isEnabled();
    }

    /** Opens 'Add Property' dialog from explorer.
     * @param filesystemNode is node of tree in explorer
     * @param path of file in tree in explorer
     */
    public void openAddPropertyDialogFromExplorer(Node filesystemNode, String path) {
        new ActionNoBlock(null, this.POPUP_MENU_ADD_PROPERTY).performPopup(new Node(filesystemNode, path));
    }

    public void openCustomizePropertiesDialogFromExplorer(Node fileNode, String path) {
        new ActionNoBlock(null, this.POPUP_MENU_CUSTOMIZE).performPopup(new Node(fileNode, path));
    }

    public void customizePropertiesDialogClickHelpButton() {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_CUSTOMIZE_PROPERTIES_DIALOG);
        //nbDialogOperator.btHelp().pushNoBlock();
        nbDialogOperator.help();
    }

    public void customizePropertiesDialogClickCloseButton() {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_CUSTOMIZE_PROPERTIES_DIALOG);
        //nbDialogOperator.btClose().pushNoBlock();
        nbDialogOperator.close();
    }

    public void customizePropertiesDialogClickAddKeyButton() {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_CUSTOMIZE_PROPERTIES_DIALOG);
        JButtonOperator jButtonOperator = new JButtonOperator(new ContainerOperator(nbDialogOperator), this.BUTTON_NAME_ADD_KEY);
        jButtonOperator.pushNoBlock();
    }

    public void customizePropertiesDialogClickRemovePropertyButton() {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_CUSTOMIZE_PROPERTIES_DIALOG);
        JButtonOperator jButtonOperator = new JButtonOperator(new ContainerOperator(nbDialogOperator), this.BUTTON_NAME_REMOVE_KEY);
        jButtonOperator.requestFocus();
        jButtonOperator.pushNoBlock();
    }

    public boolean customizePropertiesDialogIsEnableRemovePropertyButton() {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_CUSTOMIZE_PROPERTIES_DIALOG);
        JButtonOperator jButtonOperator = new JButtonOperator(new ContainerOperator(nbDialogOperator), this.BUTTON_NAME_REMOVE_KEY);
        return jButtonOperator.isEnabled();

    }

    public void customizePropertiesDialogSelectProperty(int row) {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_CUSTOMIZE_PROPERTIES_DIALOG);
        JListOperator jListOperator = new JListOperator(new ContainerOperator(nbDialogOperator));
        jListOperator.selectItem(row - 1);
    }

    public void deleteLocaleFromExplorer(Node fileNode, String path) {
        new ActionNoBlock(null, this.POPUP_MENU_DELETE_LOCALE).performPopup(new Node(fileNode, path));
    }

    public void confirmationDeleteteLocaleDialogClickYes() {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_DIALOG_CONFIRM_OBJECT_DELETION);
        JButtonOperator jButtonOperator = new JButtonOperator(new ContainerOperator(nbDialogOperator), this.BUTTON_NAME_YES);
        jButtonOperator.pushNoBlock();
    }

    public void confirmationDeleteteMoreLocalesDialogClickYes() {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_DELETE_MORE_LOCALES_CONFIRMATION_DIALOG);
        JButtonOperator jButtonOperator = new JButtonOperator(new ContainerOperator(nbDialogOperator), this.BUTTON_NAME_YES);
        jButtonOperator.pushNoBlock();
    }

    public void confirmationDeleteteLocaleDialogClickNo() {
        NbDialogOperator nbDialogOperator = new NbDialogOperator(this.TITLE_DIALOG_CONFIRM_OBJECT_DELETION);
        JButtonOperator jButtonOperator = new JButtonOperator(new ContainerOperator(nbDialogOperator), this.BUTTON_NAME_NO);
        jButtonOperator.pushNoBlock();
    }

    public int checkEditorTabCount() {
        return -1;
    }

    public void deleteMoreLocalesInExplorer(String[] Languages, String[] Countres, String[] Varians, Node fileNode, String path_to_group) {

        Node[] nodes = new Node[Languages.length];
        for (int ii = 0; ii < Languages.length; ii++) {
            String path = path_to_group.concat("|").concat(Languages[ii]).concat("_").concat(Countres[ii]);
            System.out.println(">> path = " + path);
            nodes[ii] = new Node(fileNode, path);
        }
        new DeleteAction().performPopup(nodes);

    }

    public void explorerSelectPath(String treePath, Node filesystemNode) {
        Node node = new Node(filesystemNode, treePath);
        node.select();
    }

    public void propertiesWindowChangeProperty(String objectName, String propertyName, String newValue) {
        PropertySheetOperator.invoke();
        PropertySheetOperator pso = new PropertySheetOperator(this.TITLE_PROPERTIES_WINDOW_TABLE.concat(objectName));
        //PropertySheetTabOperator psto = new PropertySheetTabOperator(pso, this.TITLE_PROPERTIES_WINDOW_TAB);
        StringProperty pr = new StringProperty(pso, propertyName);
        pr.setValue(newValue);
        new EventTool().waitNoEvent(250);
    /*        StringCustomEditorOperator stringCustomizerEditorOperator = pr.setinvokeCustomizer();
    stringCustomizerEditorOperator.setStringValue(newValue);
    new EventTool().waitNoEvent(250);
    stringCustomizerEditorOperator.btOK().requestFocus();
    new EventTool().waitNoEvent(250);
    stringCustomizerEditorOperator.btOK().pushNoBlock();
    new EventTool().waitNoEvent(250);*/
    }

    public void checkLocalesInExplorer(java.lang.String[] locales, Node filesystemNode, java.lang.String fileName) throws Exception {
        new EventTool().waitNoEvent(300);
        //org.openide.filesystems.FileSystem fileSytem = org.openide.filesystems.Repository.getDefault().findFileSystem(filesystemNode.getPath());
        ProjectsTabOperator pto = new ProjectsTabOperator();
        pto.invoke();
        ProjectRootNode prn = pto.getProjectRootNode(projectName);
        prn.select();
        Node node = new Node(prn, "Source Packages" + this.TREE_SEPARATOR + fileName);
        String[] strs = node.getChildren();
        if (strs.length != locales.length) {
            throw new Exception("> There is bad count of locales in Explorer window. (" + strs.length + "<>" + locales.length + ")");
        }
        for (int ii = 0; ii < locales.length; ii++) {
            if (strs[ii].compareTo(locales[ii]) != 0) {
                throw new Exception("> There is bad locale name in Explorer window. (" + strs[ii] + "<>" + locales[ii] + ")");
            }
        }

    }

    public boolean existsFileInExplorer(String packageName, String fileName) {
        try {
            log("Testing name of file in Explorer : \"" + fileName + "\"");
            ProjectsTabOperator pto = ProjectsTabOperator.invoke();
            //ProjectRootNode prn = pto.getProjectRootNode(projectName);
            //prn.select();
            //Node node = new Node(prn,treeSubPackagePathToFile+TREE_SEPARATOR+fileName);
            SourcePackagesNode sourcePackagesNode = new SourcePackagesNode(DEFAULT_PROJECT_NAME);
            log("source node:" + sourcePackagesNode.getPath());
            Node packageNode = new Node(sourcePackagesNode, packageName);
            log("package node:" + packageNode.getPath());
            Node fileNode = new Node(packageNode, fileName);

            log("Log: Found node " + fileNode.getText());
            return true;
        } catch (TimeoutExpiredException ex) {
            return false;
        }
    }

    public boolean existsFileInFilesTab(String fileName) {
        try {
            log("Testing name of file in Files Explorer : \"" + fileName + "\"");
            FilesTabOperator fto = FilesTabOperator.invoke();
            Node projectNode = fto.getProjectNode(DEFAULT_PROJECT_NAME);
            Node propertyNode = new Node(projectNode, fileName);
            return true;
        } catch (TimeoutExpiredException ex) {
            return false;
        }
    }

    public void requestFocusEditorPane(String nameOfTab) {
        TopComponentOperator tco = new TopComponentOperator(nameOfTab);
        tco.requestFocus();
    /*EditorWindowOperator ewo = new EditorWindowOperator();
    EditorOperator eo =  ewo.getEditor(nameOfTab);
    eo.requestFocus();*/
    }

    public void tableViewClickTo(String fileName, int row, int column) {
        TopComponentOperator tco = new TopComponentOperator(fileName);
        JTable jTable = JTableOperator.findJTable(tco.getContainer(ComponentSearcher.getTrueChooser("")), ComponentSearcher.getTrueChooser(""));
        new JTableOperator(jTable).clickOnCell(row, column);

    }

    /** Check if file exists in Editor. This method compare the title name of
     * tab in editor window.
     */
    public boolean existsFileInEditor(String fileName) {
        try {
            log("Testing name of file in tab of Editor window : \"" + fileName + "\"");
            eo = new EditorOperator(fileName);
            eo.close();
            return true;
        } catch (TimeoutExpiredException ex) {
            return false;
        }

    }

    public boolean existsFileInAdvanceEditor(String fileName) {
        try {
            log("Testing name of file in tab of Editor window : \"" + fileName + "\"");
            TopComponentOperator tcp = new TopComponentOperator(fileName);
            tcp.closeWindow();
            return true;
        } catch (TimeoutExpiredException ex) {
            return false;
        }

    }
    //Fill key, value and comments in dialog New Property
    public void fillNewKeyValue(String key, String value, String comments) {
        NbDialogOperator nbdo = new NbDialogOperator(BUTTON_NEW_PROPERTY);
        KEY = new JTextFieldOperator(nbdo, 0);
        KEY.typeText(key);

        VALUE = new JTextFieldOperator(nbdo, 1);
        VALUE.typeText(value);

        COMMENTS = new JTextFieldOperator(nbdo, 2);
        COMMENTS.typeText(comments);

        new EventTool().waitNoEvent(1000);
        nbdo.ok();
    }

    public boolean checkKeysAndValues(String bundle, String key, String value, String comments) {
        StringTokenizer tokenizer = new StringTokenizer("#" + comments + "\n" + key + "=" + value, "\n");
        boolean result = true;
        int pos = -1;

        eo = new EditorOperator(bundle);
        String editortext = eo.getText();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            pos = editortext.indexOf(token, pos);
            if (pos == -1) {
                result = false;
                break;
            }
            pos += token.length();
        }

        System.out.println(result);
        return result;
    }
}
