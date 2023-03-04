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


package org.netbeans.jellytools.properties.editors;

import javax.swing.JDialog;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling TreeView Custom Editor
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0 */
public class TreeViewCustomEditorOperator extends NbDialogOperator {

    private JTreeOperator _tree;

    /** Creates new TreeViewCustomEditorOperator
     * @param title String title of custom editor */    
    public TreeViewCustomEditorOperator(String title) {
        super(title);
    }

    /** Creates new TreeViewCustomEditorOperator
     * @param wrapper JDialogOperator wrapper for custom editor */    
    public TreeViewCustomEditorOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }
    
    /** returns selected node name
     * @return String name of selected node */    
    public String getNodeValue() {
        TreePath tp=tree().getSelectionPath();
        if (tp==null) return null;
        return tp.getLastPathComponent().toString();
    }
    
    /** returns selected node path
     * @return String path of selected node */    
    public String getPathValue() {
        TreePath tp=tree().getSelectionPath();
        if (tp==null) return null;
        return new Node(tree(), tp).getPath();
    }
    
    /** sets selected node
     * @param treePath String path of node to be selected */    
    public void setPathValue(String treePath) {
        tree().selectPath(tree().findPath(treePath, "|"));
    }
    
    /** getter for JTreeOperator
     * @return JTreeOperator */    
    public JTreeOperator tree() {
        if(_tree==null) {
            _tree = new JTreeOperator(this);
        }
        return _tree;
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        tree();
    }
}
