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

package org.netbeans.jellytools.nodes;

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Node representing Class file */
public class ClassNode extends Node {

    /** creates new ClassNode
     * @param treeOperator JTreeOperator tree
     * @param treePath String tree path */
    public ClassNode(JTreeOperator treeOperator, String treePath) {
       super(treeOperator, treePath);
    }

    /** creates new ClassNode
     * @param parent parent Node
     * @param treeSubPath String tree path from parent node */    
    public ClassNode(Node parent, String treeSubPath) {
       super(parent, treeSubPath);
    }

    /** creates new ClassNode
     * @param treeOperator JTreeOperator tree
     * @param path TreePath */    
    public ClassNode(JTreeOperator treeOperator, TreePath path) {
       super(treeOperator, path);
    }

    static final PropertiesAction propertiesAction = new PropertiesAction();
    
   
    /** tests popup menu items for presence */    
    public void verifyPopup() {
        verifyPopup(new Action[]{
            propertiesAction
        });
    }

    /** performs PropertiesAction with this node */    
    public void properties() {
        propertiesAction.perform(this);
    }
}
