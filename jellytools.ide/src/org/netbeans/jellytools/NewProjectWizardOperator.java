/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
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
