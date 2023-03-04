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

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * TreePanelView.java
 *
 * Created on May 26, 2005
 * @author mkuchtiak
 */
public class TreePanelView extends PanelView {

    HashMap<String, TreePanel> map;
    JPanel cardPanel;
    CardLayout cardLayout;
    public TreePanelView() {
        super();
    }

    public void initComponents() {
        setLayout(new BorderLayout());
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        map = new HashMap<>();
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(cardPanel);
        add (scrollPane, BorderLayout.CENTER);
    }

    public void showSelection(org.openide.nodes.Node[] nodes) {
        if (nodes.length>0 && nodes[0] instanceof TreeNode) {
            TreeNode node = (TreeNode)nodes[0];
            showPanel(node);
        }
    }
    
    protected void showPanel(TreeNode node) {
        String panelId = node.getPanelId();
        TreePanel treePanel = map.get(panelId);
        if (treePanel==null) {
            treePanel = node.getPanel();
            map.put(panelId,treePanel);
            cardPanel.add((JPanel)treePanel,panelId);
        } 
        cardLayout.show(cardPanel, panelId);
        treePanel.setModel(node);
    }

    protected org.netbeans.modules.xml.multiview.Error validateView() {
        return null;
    }

}
