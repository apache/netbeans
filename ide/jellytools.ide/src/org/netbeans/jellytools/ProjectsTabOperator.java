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

import java.awt.Component;
import java.awt.Container;
import javax.swing.JLabel;
import org.netbeans.jellytools.actions.ProjectViewAction;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 * Operator handling Projects TopComponent.<p>
 * Functionality related to Projects tree is delegated to JTreeOperator (method
 * tree()) and nodes (method getRootNode()).<p>
 *
 * Example:<p>
 * <pre>
 *      ProjectsTabOperator pto = new ProjectsTabOperator();
 *      // or when Projects pane is not already opened
 *      //ProjectsTabOperator pto = ProjectsTabOperator.invoke();
 *
 *      // get the tree if needed
 *      JTreeOperator tree = pto.tree();
 *      // work with nodes
 *      ProjectRootNode prn = pto.getProjectRootNode("SampleProject").select();
 *      Node node = new Node(prn, "subnode|sub subnode");
 * </pre>
 *
 * @see ProjectViewAction
 * @see ProjectRootNode
 */
public class ProjectsTabOperator extends TopComponentOperator {

    static final String PROJECT_CAPTION = Bundle.getStringTrimmed(
            "org.netbeans.modules.project.ui.Bundle",
            "LBL_projectTabLogical_tc");
    protected static final ProjectViewAction viewAction = new ProjectViewAction();
    private JTreeOperator _tree;

    /**
     * Search for Projects TopComponent within all IDE.
     */
    public ProjectsTabOperator() {
        super(waitTopComponent(null, PROJECT_CAPTION, 0, new ProjectsTabSubchooser()));
    }

    /**
     * invokes Projects and returns new instance of ProjectsTabOperator
     *
     * @return new instance of ProjectsTabOperator
     */
    public static ProjectsTabOperator invoke() {
        viewAction.perform();
        return new ProjectsTabOperator();
    }

    /**
     * Getter for Projects JTreeOperator
     *
     * @return JTreeOperator of Projects tree
     */
    public JTreeOperator tree() {
        makeComponentVisible();
        if (_tree == null) {
            _tree = new JTreeOperator(this);
        }
        return _tree;
    }
    
    /**
     * Collapse all nodes.
     */
    public void collapseAll() {
        JTreeOperator tree = tree();
        for (int i = tree.getRowCount() - 1; i >= 0; i--) {
            tree.collapseRow(i);
        }
    }

    /**
     * Gets ProjectRootNode. Wait if Opening Projects label is in main window
     * progress bar.
     *
     * @param projectName display name of project
     * @return ProjectsRootNode
     */
    public ProjectRootNode getProjectRootNode(String projectName) {
        final String openingProjectsLabel = "Opening Projects";
        Object lblOpening = JLabelOperator.findJLabel(
                (Container) MainWindowOperator.getDefault().getSource(),
                openingProjectsLabel, false, false);
        if (lblOpening != null) {
            JLabelOperator lblOper = new JLabelOperator((JLabel) lblOpening);
            lblOper.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 180000);
            lblOper.waitState(new ComponentChooser() {
                @Override
                public boolean checkComponent(Component comp) {
                    String text = ((JLabel) comp).getText();
                    return text == null || !text.startsWith(openingProjectsLabel) || !comp.isShowing();
                }

                @Override
                public String getDescription() {
                    return openingProjectsLabel + " label disappears";
                }
            });
        }
        return new ProjectRootNode(tree(), projectName);
    }

    /**
     * Performs verification by accessing all sub-components
     */
    public void verify() {
        tree();
    }

    /**
     * SubChooser to determine TopComponent is instance of
     * org.netbeans.modules.projects.ui.ProjectTab Used in constructor.
     */
    private static final class ProjectsTabSubchooser implements ComponentChooser {

        @Override
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("ProjectTab");
        }

        @Override
        public String getDescription() {
            return "org.netbeans.modules.projects.ui.ProjectTab";
        }
    }
}
