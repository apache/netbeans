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

package org.netbeans.modules.websvc.wsitconf.ui;

import javax.swing.JPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import java.awt.BorderLayout;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.NbBundle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.project.Project;
import org.openide.nodes.Children;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import java.util.HashSet;
import org.openide.nodes.AbstractNode;

/**
 * @author Martin Grebac
 */
public class SelectClassPanel extends JPanel implements ExplorerManager.Provider {
    
    private ExplorerManager manager;
    private Node[] selectedNodes;
    private JPanel panel;
    private Project project;
    
    /**
     * Creates a new instance of SelectClassPanel
     */
    public SelectClassPanel(Project project) {
        initComponents();
        this.project = project;
        manager = new ExplorerManager();
        manager.addPropertyChangeListener(
        new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                selectedNodes = manager.getSelectedNodes();
            }
        });
        populateTree();
    }
    
    public Node[] getSelectedNodes(){
        return selectedNodes.clone();
    }
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    private void populateTree(){
        LogicalViewProvider lvp = project.getLookup().lookup(LogicalViewProvider.class);
        manager.setRootContext(lvp.createLogicalView());
    }
    
    private void initComponents() {
        panel = new JPanel();
        setLayout(new BorderLayout());
        panel.setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        
        BeanTreeView btv = new BeanTreeView();
        btv.getAccessibleContext().
        setAccessibleName(NbBundle.getMessage(SelectClassPanel.class,"LBL_Class_Tree"));    //NOI18N
        btv.getAccessibleContext().setAccessibleDescription
        (NbBundle.getMessage(SelectClassPanel.class,"TTL_SelectClass"));    //NOI18N
        panel.add(btv, "Center");   //NOI18N
        panel.validate();
        validate();
    }
}
