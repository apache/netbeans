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
