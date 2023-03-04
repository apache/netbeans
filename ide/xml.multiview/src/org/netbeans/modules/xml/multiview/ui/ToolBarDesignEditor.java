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

package org.netbeans.modules.xml.multiview.ui;

import org.netbeans.modules.xml.multiview.Error;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.ChoiceView;
import org.openide.explorer.view.NodeListModel;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.*;

/**
 * The ComponentPanel three pane editor. This is basically a container that implements the ExplorerManager
 * interface. It coordinates the selection of a node in the structure pane and the display of a panel by the a PanelView
 * in the content pane and the nodes properties in the properties pane. It will populate the tree view in the structure pane
 * from the root node of the supplied PanelView.
 *
 **/

public class ToolBarDesignEditor extends AbstractDesignEditor {
    
    protected JComponent designPanel;
    private ErrorPanel errorPanel;
    private Object lastActive;

    /**
     * Creates a new instance of ToolBarDesignEditor
     * @param panel The PanelView which will provide the node tree for the structure view
     *              and the set of panels the nodes map to.
     */
    public ToolBarDesignEditor(){
        super();
        createDesignPanel();
        add(java.awt.BorderLayout.CENTER,designPanel);
        add(java.awt.BorderLayout.SOUTH,createErrorPanel());
    }
    
    /**
     * Creates a new instance of ToolBarDesignEditor
     * @param panel The PanelView which will provide the node tree for the structure view
     *              and the set of panels the nodes map to.
     */
    public ToolBarDesignEditor(PanelView panel){
        super(panel);
        createDesignPanel();
        designPanel.add(panel,BorderLayout.CENTER);
        add(java.awt.BorderLayout.CENTER,designPanel);
        add(java.awt.BorderLayout.SOUTH,createErrorPanel());
        panel.attachErrorPanel(errorPanel);
    }
    
    public JComponent createDesignPanel(){
        designPanel = new JPanel(new BorderLayout());
        return designPanel;
    }
    
    public void setContentView(PanelView panel) {
        if (getContentView()!=null) {
            designPanel.remove(getContentView());
        }
        designPanel.add(panel,BorderLayout.CENTER);
        panel.attachErrorPanel(errorPanel);
        super.setContentView(panel);
    }
    
    public ErrorPanel getErrorPanel() {
        return errorPanel;
    }
    
    public Error getError() {
        return(errorPanel==null?null:errorPanel.getError());
    }
    
    private ErrorPanel createErrorPanel() {
        errorPanel = new ErrorPanel(this);
        return errorPanel;
    }
    
    /**
     * Used to create an instance of the JComponent used for the structure component. Usually a subclass of BeanTreeView.
     * @return the JComponent
     */
    
    public JComponent createStructureComponent() {
        JToolBar toolbar = new ToolBarView(getExplorerManager(),getContentView().getRoot(), helpAction);
        return toolbar;
    }
 
    private static class ToolBarView extends JToolBar implements ExplorerManager.Provider, Lookup.Provider {
        private ExplorerManager manager;
        private Lookup lookup;
        private javax.swing.Action helpAction;
        ToolBarView(final ExplorerManager manager, org.openide.nodes.Node root, javax.swing.Action helpAction) {
            super();
            this.manager=manager;
            this.helpAction=helpAction;
            // same as before...
            
            setLayout(new java.awt.GridBagLayout());
            ActionMap map = getActionMap();
            // ...and initialization of lookup variable
            lookup = ExplorerUtils.createLookup (manager, map);

            ChoiceView cView = new ChoiceView();
            ((NodeListModel)(cView.getModel())).setNode(root);
            setFloatable(false);
            ((NodeListModel)(cView.getModel())).setDepth(5);
            
            java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.insets = new java.awt.Insets(0, 2, 4, 0);
            add(cView,gridBagConstraints);
            
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.weightx = 1.0;
            JPanel filler = new JPanel();
            add(filler,gridBagConstraints);
            
            javax.swing.JButton helpButton = new javax.swing.JButton(helpAction);
            helpButton.setText("");
            helpButton.setContentAreaFilled(false);
            helpButton.setFocusPainted(false);
            helpButton.setBorderPainted(false);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 0;
            //gridBagConstraints.weightx = 1.0;
            add(helpButton,gridBagConstraints);
        }
        // ...method as before and getLookup
        public ExplorerManager getExplorerManager() {
            return manager;
        }
        
        public Lookup getLookup() {
            return lookup;
        }
        // ...methods as before, but replace componentActivated and
        // componentDeactivated with e.g.:
        
        public void addNotify() {
            //System.out.println("addNotify()");
            super.addNotify();
            ExplorerUtils.activateActions(manager, true);
        }
        public void removeNotify() {
            //System.out.println("removeNotify()");
            ExplorerUtils.activateActions(manager, false);
            //super.removeNotify();
        }
    }
    
    public Object getLastActive() {
        return lastActive;
    }
    
    public void setLastActive(Object lastActive) {
        this.lastActive=lastActive;
    }
}
