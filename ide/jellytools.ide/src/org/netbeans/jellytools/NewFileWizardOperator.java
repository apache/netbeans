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
package org.netbeans.jellytools;

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.actions.NewFileAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;

/**
 * Handle NetBeans New File wizard.
 * It is invoked either from main menu File -> New File...
 * <code>NewFileAction.performMenu();</code>
   or from popup menu on folder <code>NewFileAction.performPopup();</code><br>
 * Usage:
 *
 * <pre>
 * NewFileWizardOperator op = NewFileWizardOperator.invoke();
 * op.selectCategory("Java Classes");
 * op.selectFileType("Java Class");
 * </pre>
 *
 * @author tb115823
 */
public class NewFileWizardOperator extends WizardOperator {

    private JLabelOperator      _lblProject;
    private JLabelOperator      _lblCategories;
    private JLabelOperator      _lblFileTypes;
    private JTreeOperator       _treeCategories;
    private JListOperator       _lstFileTypes;
    private JLabelOperator      _lblDescription;
    private JEditorPaneOperator _txtDescription;
    private JComboBoxOperator   _cboProject;
    
    
    /** Creates new NewFileWizardOperator that can handle it.
     */
    public NewFileWizardOperator() {
        super(Bundle.getString("org.netbeans.modules.project.ui.Bundle", "LBL_NewFileWizard_Subtitle"));
    }

    /** Waits for wizard with given title.
     * @param title title of wizard
     */
    public NewFileWizardOperator(String title) {
        super(title);
    }
    
    /** Invokes new wizard and returns instance of NewFileWizardOperator.
     * @return  instance of NewFileWizardOperator
     */
    public static NewFileWizardOperator invoke() {
        new NewFileAction().perform();
        return new NewFileWizardOperator();
    }

    /** Invokes new wizard and returns instance of NewFileWizardOperator.
     * @param title initial wizard title
     * @return  instance of NewFileWizardOperator
     */
    public static NewFileWizardOperator invoke(String title) {
        new NewFileAction().perform();
        return new NewFileWizardOperator(title);
    }

    /** Selects specified node and invokes new file wizard by default action.
     * In "Choose File Type" wizard's page it selects given category and filetype.
     * It returns instance of NewFileWizardOperator representing "Name and Location"
     * page of the wizard.
     * @param node node which should be selected before new file wizard is invoked
     * @param category category to be selected
     * @param filetype file type to be selected (exact name - not substring)
     * @return instance of NewFileWizardOperator
     */
    public static NewFileWizardOperator invoke(Node node, String category, String filetype) {
        new NewFileAction().perform(node);
        String wizardTitle = Bundle.getString("org.netbeans.modules.project.ui.Bundle",
                                              "LBL_NewFileWizard_Title");
        NewFileWizardOperator nfwo = new NewFileWizardOperator(wizardTitle);
        nfwo.selectCategory(category);
        nfwo.selectFileType(filetype);
        nfwo.next();
        return new NewFileWizardOperator();
    }     
    
    /** Select given project in combobox of projects
     *  @param project name of project
     */
    public void selectProject(String project) {
        cboProject().selectItem(project);
    }
    
    
    /** Selects given project category
     * @param category name of the category to select
     */
    public void selectCategory(String category) {
        // we need to wait until some node is selected because 'please, wait' node
        // is shown before tree is initialized. Then we can change selection.
        try {
            new Waiter(new Waitable() {

                @Override
                public Object actionProduced(Object param) {
                    return treeCategories().isSelectionEmpty() ? null : Boolean.TRUE;
                }

                @Override
                public String getDescription() {
                    return ("Wait node is selected");
                }
            }).waitAction(null);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        }
        // wait for UI is refreshed to prevent wrong selection
        new EventTool().waitNoEvent(500);
        new Node(treeCategories(), category).select();
    }
    
    /** Selects given file type
     * @param filetype name of file type to select (exact name - not substring)
     */
    public void selectFileType(final String filetype) {
        // need to wait for item before selecting it
        try {
            new Waiter(new Waitable() {

                @Override
                public Object actionProduced(Object param) {
                    return lstFileTypes().findItemIndex(filetype) != -1 ? Boolean.TRUE : null;
                }

                @Override
                public String getDescription() {
                    return ("Wait item available");
                }
            }).waitAction(null);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        }
        lstFileTypes().selectItem(filetype);
    }
    
    
    /** Tries to find "Project:"
     * @return JLabelOperator
     */
    public JLabelOperator lblProject() {
        if (_lblProject==null) {
            _lblProject = new JLabelOperator(this, Bundle.getString("org.netbeans.modules.project.ui.Bundle", "LBL_TargetChooser_Project_Label"));
        }
        return _lblCategories;
    }
    
    /** Tries to find "Categories:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblCategories() {
        if (_lblCategories==null) {
            _lblCategories = new JLabelOperator(this, Bundle.getString("org.netbeans.modules.project.ui.Bundle", "CTL_Categories"));
        }
        return _lblCategories;
    }

    /** Tries to find "File Types:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFileTypes() {
        if (_lblFileTypes==null) {
            _lblFileTypes = new JLabelOperator(this, Bundle.getString("org.netbeans.modules.project.ui.Bundle", "CTL_Files"));
        }
        return _lblFileTypes;
    }

    /** Tries to find JComboBox Project
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboProject() {
        if (_cboProject==null) {
            _cboProject = new JComboBoxOperator(this);
        }
        return _cboProject;
    }
    
    /** returns selected item for cboProject
     * @return selected project
     */
    public String getSelectedProject() {
        return cboProject().getSelectedItem().toString();
    }
    
    /** Tries to find null TreeView$ExplorerTree in this dialog.
     * @return JTreeOperator
     */
    public JTreeOperator treeCategories() {
        if (_treeCategories==null) {
            _treeCategories = new JTreeOperator(this);
            // comparator needs to be exact -  category can be "Java", but also "JavaServer Faces"
            _treeCategories.setComparator(new Operator.DefaultStringComparator(true, true));
        }
        return _treeCategories;
    }
    
    /** returns selected path in treeCategories
     * @return TreePath
     */
    public TreePath getSelectedCategory() {
        return treeCategories().getSelectionPath();
    }

    /** Tries to find FileTypes ListView in this dialog.
     * @return JListOperator
     */
    public JListOperator lstFileTypes() {
        if (_lstFileTypes==null) {
            _lstFileTypes = new JListOperator(this, 1);
            // set exact comparator because Java Classes has types 'Java Package Info'
            // and 'Java Package'.
            _lstFileTypes.setComparator(new Operator.DefaultStringComparator(true, true));
        }
        return _lstFileTypes;
    }

    
    /** returns selected item in lstFileType
     * @return String selected file type
     */
    public String getSelectedFileType() {
        return lstFileTypes().getSelectedValue().toString();
    }
    
    /** Tries to find "Description:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblDescription() {
        if (_lblDescription==null) {
            _lblDescription = new JLabelOperator(this, Bundle.getString("org.netbeans.modules.project.ui.Bundle", "CTL_Description"));
        }
        return _lblDescription;
    }

    
    /** Tries to find null JEditorPane in this dialog.
     * @return JEditorPaneOperator
     */
    public JEditorPaneOperator txtDescription() {
        if (_txtDescription==null) {
            _txtDescription = new JEditorPaneOperator(this);
        }
        return _txtDescription;
    }


    
    /** gets text for txtDescription
     * @return String text
     */
    public String getDescription() {
        return txtDescription().getText();
    }

    

    /** Performs verification of NewFileWizardOperator by accessing all its components.
     */
    @Override
    public void verify() {
        lblCategories();
        lblFileTypes();
        cboProject();
        treeCategories();
        lstFileTypes();
        lblDescription();
        txtDescription();
    }
}

