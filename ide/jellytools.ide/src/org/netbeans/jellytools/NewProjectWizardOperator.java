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

import org.netbeans.jellytools.actions.NewProjectAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.*;
import javax.swing.tree.TreePath;

/**
 * Handles NetBeans New Project wizard and its components Categories and
 * Projects. It is invoked from main menu File -> New Project... <br> Usage:
 * <pre>
 *  NewProjectWizardOperator npwop = NewProjectWizardOperator.invoke();
 *  npwop.selectCategory("Standard");
 *  npwop.selectProject("Java Application");
 *  npwop.next();
 *  npwop.getDescription();
 * </pre>
 *
 * @author tb115823
 */
public class NewProjectWizardOperator extends WizardOperator {

    private JLabelOperator _lblCategories;
    private JLabelOperator _lblProjects;
    private JTreeOperator _treeCategories;
    private JListOperator _lstProjects;
    private JLabelOperator _lblDescription;
    private JEditorPaneOperator _txtDescription;

    /** Creates new NewProjectWizardOperator that can handle it.
     */
    public NewProjectWizardOperator() {
        super(Bundle.getString("org.netbeans.modules.project.ui.Bundle", "LBL_NewProjectWizard_Subtitle"));
    }

    /** Waits for wizard with given title.
     * @param title title of wizard
     */
    public NewProjectWizardOperator(String title) {
        super(title);
    }

    /** Invokes new wizard and returns instance of NewProjectWizardOperator.
     * @return  instance of NewProjectWizardOperator
     */
    public static NewProjectWizardOperator invoke() {
        new NewProjectAction().perform();
        return new NewProjectWizardOperator();
    }

    /** Invokes new wizard and returns instance of NewProjectWizardOperator.
     * @param title initial title of New Project Wizard
     * @return  instance of NewProjectWizardOperator
     */
    public static NewProjectWizardOperator invoke(String title) {
        new NewProjectAction().perform();
        return new NewProjectWizardOperator(title);
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
        } catch (TimeoutExpiredException tee) {
            // ignore it because sometimes can happen that no category is selected by default
        }
        new Node(treeCategories(), category).select();
    }

    /** Selects given project
     * @param project name of project to select
     */
    public void selectProject(String project) {
        lstProjects().selectItem(project);
    }

    /** Tries to find "Categories:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblCategories() {
        if (_lblCategories == null) {
            _lblCategories = new JLabelOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "CTL_Categories"));
        }
        return _lblCategories;
    }

    /** Tries to find "Projects:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblProjects() {
        if (_lblProjects == null) {
            _lblProjects = new JLabelOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "CTL_Projects"));
        }
        return _lblProjects;
    }

    /** Tries to find null TreeView$ExplorerTree in this dialog.
     * @return JTreeOperator
     */
    public JTreeOperator treeCategories() {
        if (_treeCategories == null) {
            _treeCategories = new JTreeOperator(this);
        }
        return _treeCategories;
    }

    /** returns selected path in treeCategories
     * @return TreePath
     */
    public TreePath getSelectedCategory() {
        return treeCategories().getSelectionPath();
    }

    /** Tries to find null ListView$NbList in this dialog.
     * @return JListOperator
     */
    public JListOperator lstProjects() {
        if (_lstProjects == null) {
            _lstProjects = new JListOperator(this, 1);
        }
        return _lstProjects;
    }

    /** returns selected item for lstProject
     * @return selected project
     */
    public String getSelectedProject() {
        return lstProjects().getSelectedValue().toString();
    }

    /** Tries to find "Description:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblDescription() {
        if (_lblDescription == null) {
            _lblDescription = new JLabelOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "CTL_Description"));
        }
        return _lblDescription;
    }

    /** Tries to find null JEditorPane in this dialog.
     * @return JEditorPaneOperator
     */
    public JEditorPaneOperator txtDescription() {
        if (_txtDescription == null) {
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

    /** Pushes "Finish" button and waits until wizard dismisses.
     */
    @Override
    public void finish() {
        btFinish().push();
        getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 120000);
        waitClosed();
    }

    /** Performs verification of NewProjectWizardOperator by accessing all its components.
     */
    @Override
    public void verify() {
        lblCategories();
        lblProjects();
        treeCategories();
        lstProjects();
        lblDescription();
        txtDescription();
    }
}
