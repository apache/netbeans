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
package org.netbeans.jellytools.modules.xml.catalog.nodes;

/*
 * CatalogEntryNode.java
 *
 * Created on 11/13/03 4:02 PM
 */

import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.nodes.Node;
import javax.swing.tree.TreePath;
import org.netbeans.jemmy.operators.JTreeOperator;

/** CatalogEntryNode Class
 * @author ms113234 */
public class CatalogEntryNode extends Node {
    
    private static class RemoveAction extends ActionNoBlock{
        RemoveAction(){
            super(null, "Delete Delete");
        }
    }
    
    private static class EditAction extends ActionNoBlock{
        EditAction(){
            super(null, "Edit");
        }
    }

    private static final Action editAction = new EditAction();
    private static final Action viewAction = new ViewAction();
    private static final Action propertiesAction = new PropertiesAction();
    private static final DeleteAction removeAction = new DeleteAction();
    
    /** creates new CatalogEntryNode
     * @param tree JTreeOperator of tree
     * @param treePath String tree path */
    public CatalogEntryNode(JTreeOperator tree, String treePath) {
        super(tree, treePath);
    }
    
    /** creates new CatalogEntryNode
     * @param tree JTreeOperator of tree
     * @param treePath TreePath of node */
    public CatalogEntryNode(JTreeOperator tree, TreePath treePath) {
        super(tree, treePath);
    }
    
    /** creates new CatalogEntryNode
     * @param parent parent Node
     * @param treePath String tree path from parent Node */
    public CatalogEntryNode(Node parent, String treePath) {
        super(parent, treePath);
    }
    
    /** tests popup menu items for presence */
    public void verifyPopup() {
        verifyPopup(new Action[]{
            viewAction,
            propertiesAction
        });
    }
    
    /** performs ViewAction with this node */
    public void view() {
        viewAction.perform(this);
    }
    
    public void edit(){
        editAction.perform(this);
    }
    
    /** performs PropertiesAction with this node */
    public void properties() {
        propertiesAction.perform(this);
    }
    
    public void remove() {
        removeAction.perform(this);
    }
}

