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
package org.netbeans.modules.websvc.core.dev.wizard;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JPanel;
import javax.swing.tree.TreeSelectionModel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;


/**
 *
 * @author ChrisWebster
 */
public class NodeDisplayPanel extends JPanel implements ExplorerManager.Provider {
    private PropertyChangeSupport pcs;
    private ExplorerManager manager = new ExplorerManager();
    
    /** Creates a new instance of NodeDisplayPanel */
    public NodeDisplayPanel(Node rootNode) {
        BeanTreeView btv = new BeanTreeView();
        btv.setRootVisible(false);
        btv.setDefaultActionAllowed(false);
        btv.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        manager.setRootContext(rootNode);
        Node[] rootChildren = rootNode.getChildren().getNodes();
        for (int i = 0; i < rootChildren.length; i++) {
            btv.expandNode(rootChildren[i]);
        }
        manager.addPropertyChangeListener(
        new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                firePropertyChange();
            }
        });
        setLayout(new BorderLayout());
        add(btv, BorderLayout.CENTER);
        btv.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NodeDisplayPanel.class, "ACSD_PortNodeTreeView"));
        btv.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NodeDisplayPanel.class, "ACSD_PortNodeTreeView"));
    }
    
    /** Ubuntu workaround
     */
    private PropertyChangeSupport getProperChangeSupport() {
        if(pcs == null) {
            pcs =  new PropertyChangeSupport(this);
        }
        return pcs;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        getProperChangeSupport().addPropertyChangeListener(l);
    }
    
    private void firePropertyChange() {
        getProperChangeSupport().firePropertyChange(ExplorerManager.PROP_NODE_CHANGE, null, null);
    }
    
    public Node[] getSelectedNodes() {
        return manager.getSelectedNodes();
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
}
