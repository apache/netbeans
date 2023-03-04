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
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Node representing Folder */
public class FolderNode extends Node {

    static final ExploreFromHereAction exploreFromHereAction = new ExploreFromHereAction();
    static final FindAction findAction = new FindAction();
    // Compile Package
    static final Action compileAction = new Action(null,
                Bundle.getString("org.netbeans.spi.java.project.support.ui.Bundle", 
                                 "LBL_CompilePackage_Action"));
    static final CopyAction copyAction = new CopyAction();
    static final PasteAction pasteAction = new PasteAction();
    static final CutAction cutAction = new CutAction();
    static final DeleteAction deleteAction = new DeleteAction();
    static final RenameAction renameAction = new RenameAction();
    static final PropertiesAction propertiesAction = new PropertiesAction();
    static final NewFileAction newFileAction = new NewFileAction();
    
    /*   protected static final Action[] folderActions = new Action[] {
        copyAction
    };*/
    
    /** creates new FolderNode
     * @param tree JTreeOperator of tree
     * @param treePath String tree path */    
    public FolderNode(JTreeOperator tree, String treePath) {
        super(tree, treePath);
    }

    /** creates new FolderNode
     * @param tree JTreeOperator of tree
     * @param treePath TreePath of node */    
    public FolderNode(JTreeOperator tree, TreePath treePath) {
        super(tree, treePath);
    }
    
    /** creates new FolderNode
     * @param parent parent Node
     * @param treePath String tree path from parent Node */    
    public FolderNode(Node parent, String treePath) {
        super(parent, treePath);
    }
   
    /** tests popup menu items for presence */    
    public void verifyPopup() {
        verifyPopup(new Action[]{
            exploreFromHereAction,
            findAction,
            copyAction,
            cutAction,
            deleteAction,
            renameAction,
            propertiesAction
        });
    }
    
/*    Action[] getActions() {
	return(folderActions);
    }*/
    
    /** performs ExploreFromHereAction with this node */    
    public void exploreFromHere() {
        exploreFromHereAction.perform(this);
    }
    
    /** performs FindAction with this node */    
    public void find() {
        findAction.perform(this);
    }
    
    /** performs CompileAction with this node */    
    public void compile() {
        compileAction.perform(this);
    }

    /** performs CopyAction with this node */    
    public void copy() {
        copyAction.perform(this);
    }
    
    /** performs PasteAction with this node */    
    public void paste() {
        pasteAction.perform(this);
    }

    /** performs CutAction with this node */    
    public void cut() {
        cutAction.perform(this);
    }
    
    /** performs DeleteAction with this node */    
    public void delete() {
        deleteAction.perform(this);
    }
    
    /** performs RenameAction with this node */    
    public void rename() {
        renameAction.perform(this);
    }
    
    /** performs PropertiesAction with this node */    
    public void properties() {
        propertiesAction.perform(this);
    }
    
    /** performs NewFileAction with this node */    
    public void newFile() {
        newFileAction.perform(this);
    }

    /** performs NewFileAction with this node
     * @param templateName template name from sub menu
     */    
    public void newFile(String templateName) {
        new NewFileAction(templateName).perform(this);
    }
}
