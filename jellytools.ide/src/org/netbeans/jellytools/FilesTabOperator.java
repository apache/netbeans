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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
