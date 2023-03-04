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
import org.netbeans.jellytools.actions.FilesViewAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Operator handling Files TopComponent.<p>
 * Functionality related to Projects tree is delegated to JTreeOperator (method
 * tree()) and nodes (method getProjectNode()).<p>
 *
 * Example:<p>
 * <pre>
 *      FilesTabOperator fto = new FilesTabOperator();
 *      // or when Files pane is not already opened
 *      FilesTabOperator fto = FilesTabOperator.invoke();
 *      
 *      // get the tree if needed
 *      JTreeOperator tree = fto.tree();
 *      // work with nodes
 *      Node projectNode = fto.getProjectNode("SampleProject").select();
 *      Node node = new Node(projectNode, "subnode|sub subnode");
 * </pre> 
 *
 * @see FilesViewAction
 */
public class FilesTabOperator extends TopComponentOperator {
    
    static final String FILES_CAPTION = Bundle.getStringTrimmed(
                                            "org.netbeans.modules.project.ui.Bundle", 
                                            "LBL_projectTab_tc");
    private static final FilesViewAction viewAction = new FilesViewAction();
    
    private JTreeOperator _tree;
    
    /** Search for Files TopComponent within all IDE. */
    public FilesTabOperator() {
        super(waitTopComponent(null, FILES_CAPTION, 0, new FilesTabSubchooser()));
    }

    /** invokes Files and returns new instance of FilesTabOperator
     * @return new instance of FilesTabOperator */
    public static FilesTabOperator invoke() {
        viewAction.perform();
        return new FilesTabOperator();
    }
    
    /** Getter for Files JTreeOperator
     * @return JTreeOperator of Projects tree */    
    public JTreeOperator tree() {
        makeComponentVisible();
        if(_tree==null) {
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

    /** Gets node representing a project.
     * @param projectName display name of project
     * @return Node instance representing the project specified by name
     */
    public Node getProjectNode(String projectName) {
        return new Node(tree(), projectName);
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        tree();
    }
    
    /** SubChooser to determine TopComponent is instance of 
     * org.netbeans.modules.project.ui.ProjectTab
     * Used in constructor.
     */
    private static final class FilesTabSubchooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("ProjectTab");
        }
        
        public String getDescription() {
            return "org.netbeans.modules.project.ui.ProjectTab";
        }
    }
}
