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
