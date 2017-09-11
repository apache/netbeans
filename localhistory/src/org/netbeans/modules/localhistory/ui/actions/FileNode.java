/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.localhistory.ui.actions;

import java.io.File;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import org.netbeans.modules.localhistory.store.StoreEntry;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * @author Tomas Stupka
 */
public abstract class FileNode extends DefaultMutableTreeNode {

    private boolean selected = true;

    public FileNode(Object userObject) {
        super(userObject, true);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        setSelected(selected, true);
    }

    private void setSelected(boolean selected, boolean completeTree) {
        this.selected = selected;
        if(completeTree) {
            TreeNode[] path = getPath();
            for (int i = path.length - 2; i >= 0; i--) {
                TreeNode tn = path[i];
                if(tn instanceof FileNode) {
                    FileNode fn = (FileNode) tn;
                    if(containsSelected(fn) && !selected) {
                        break;
                    } 
                    fn.setSelected(selected, false);
                }
            }
            selectChildren(this, selected);
        }
    }
    
    private void selectChildren(FileNode node, boolean selected) {
        int count = node.getChildCount();
        for (int i = 0; i < count; i++) {
            FileNode child = (FileNode) node.getChildAt(i);
            child.setSelected(selected);
            selectChildren(child, selected);
        }
    }

    abstract VCSFileProxy getFile();
    abstract String getText();
    abstract String getTooltip();

    private boolean containsSelected(FileNode fn) {
        for (int i = 0; i < fn.getChildCount(); i++) {
            TreeNode child = fn.getChildAt(i);
            if(child instanceof FileNode) {
                if( ((FileNode)child).isSelected() ) {
                    return true;
                }
            }
        }
        return false;
    }

    static class StoreEntryNode extends FileNode {

        public StoreEntryNode(StoreEntry storeEntry) {
            super(storeEntry);
        }
        
        @Override
        VCSFileProxy getFile() {
            return getStoreEntry().getFile();
        }
        
        StoreEntry getStoreEntry() {
            return ((StoreEntry)userObject);
        }

        @Override
        String getText() {
            return getFile().getName();
        }

        @Override
        String getTooltip() {
            return "Revert " + getFile();
        }
    }
    
    static class PlainFileNode extends FileNode {
        public PlainFileNode(VCSFileProxy root) {
            super(root);
        }

        @Override
        VCSFileProxy getFile() {
            return (VCSFileProxy)userObject;
        }

        @Override
        String getText() {
            return getFile().getName();
        }

        @Override
        String getTooltip() {
            return getFile().getName();
        }
    }
    
}
