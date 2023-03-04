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

package org.netbeans.lib.profiler.ui.swing;

import java.util.HashSet;
import java.util.Set;
import javax.swing.tree.TreeNode;

/**
 *
 * @author Jiri Sedlacek
 */
public interface ProfilerTreeTableModel {
    
    public TreeNode getRoot();
    
    public int getColumnCount();
    
    public Class getColumnClass(int column);

    public String getColumnName(int column);

    public void setValueAt(Object aValue, TreeNode node, int column);

    public Object getValueAt(TreeNode node, int column);
    
    public boolean isCellEditable(TreeNode node, int column);
    
    public void addListener(Listener listener);
    
    public void removeListener(Listener listener);
    
    
    public abstract static class Abstract implements ProfilerTreeTableModel {
        
        private TreeNode root;
        
        private Set<Listener> listeners;
        
        public Abstract(TreeNode root) {
            if (root == null) throw new NullPointerException("Root cannot be null"); // NOI18N
            this.root = root;
        }
        
        public void dataChanged() {
            fireDataChanged();
        }
        
        public void structureChanged() {
            fireStructureChanged();
        }
        
        public void childrenChanged(TreeNode node) {
            fireChildrenChanged(node);
        }
        
        public void setRoot(TreeNode newRoot) {
            TreeNode oldRoot = root;
            root = newRoot;
            fireRootChanged(oldRoot, newRoot);
        }
        
        public TreeNode getRoot() {
            return root;
        }
        
        public void addListener(Listener listener) {
            if (listeners == null) listeners = new HashSet();
            listeners.add(listener);
        }
        
        public void removeListener(Listener listener) {
            if (listeners != null) {
                listeners.remove(listener);
                if (listeners.isEmpty()) listeners = null;
            }
        }
        
        protected void fireDataChanged() {
            if (listeners != null)
                for (Listener listener : listeners)
                    listener.dataChanged();
        }
        
        protected void fireStructureChanged() {
            if (listeners != null)
                for (Listener listener : listeners)
                    listener.structureChanged();
        }
        
        protected void fireChildrenChanged(TreeNode node) {
            if (listeners != null)
                for (Listener listener : listeners)
                    listener.childrenChanged(node);
        }
        
        protected void fireRootChanged(TreeNode oldRoot, TreeNode newRoot) {
            if (listeners != null)
                for (Listener listener : listeners)
                    listener.rootChanged(oldRoot, newRoot);
        }
        
    }
    
    
    public static interface Listener {
        
        public void dataChanged();
        
        public void structureChanged();
        
        public void childrenChanged(TreeNode node);
        
        public void rootChanged(TreeNode oldRoot, TreeNode newRoot);
        
    }
    
    public static class Adapter implements Listener {
        
        public void dataChanged() {}
        
        public void structureChanged() {}
        
        public void childrenChanged(TreeNode node) {}
        
        public void rootChanged(TreeNode oldRoot, TreeNode newRoot) {}
        
    }
    
}
