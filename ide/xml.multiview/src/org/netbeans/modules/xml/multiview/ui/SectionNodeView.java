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

import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.Utils;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

import java.util.HashMap;

/**
 * A section view for <code>SectionNode</code>. 
 *
 * @author pfiala
 */
public abstract class SectionNodeView extends SectionView {

    private final XmlMultiViewDataObject dataObject;
    private SectionNode rootNode = null;
    private HashMap<SectionNode, SectionNode> nodes = new HashMap<>();

    private final RequestProcessor.Task refreshTask = RequestProcessor.getDefault().create(new Runnable() {
        public void run() {
            getRootNode().refreshSubtree();
        }
    });

    private static final int REFRESH_DELAY = 20;

    /**
     * Constructs new SecionNodeView.
     * @param dataObject the associated data object.
     */
    public SectionNodeView(XmlMultiViewDataObject dataObject) {
        super();
        this.dataObject = dataObject;
    }

    /**
     * Sets the given <code>rootNode</code> as the root
     * of this view and adds its associated section node 
     * panel as a section for this.
     */
    public void setRootNode(SectionNode rootNode) {
        this.rootNode = rootNode;
        Children root = new Children.Array();
        root.add(new Node[]{rootNode});
        AbstractNode mainNode = new AbstractNode(root);
        mainNode.setDisplayName(rootNode.getDisplayName());
        mainNode.setIconBaseWithExtension(rootNode.getIconBase() + ".gif"); //NOI18N
        setRoot(mainNode);
        addSection(rootNode.getSectionNodePanel());
    }

    public XmlMultiViewDataObject getDataObject() {
        return dataObject;
    }

    /**
     * Opens the panel associated with the given <code>node</code>.
     */
    public void openSection(Node node) {
        openPanel(node);
    }

    /**
     * Opens the panel representing <code>SecctionNode</code>
     * identified by the given <code>key</code>.
     * @param key the key of <code>SectionNode</code>
     */
    public void openPanel(Object key) {
        SectionNode sectionNode = retrieveSectionNode((SectionNode) key);
        SectionNodePanel sectionNodePanel = sectionNode.getSectionNodePanel();
        sectionNodePanel.open();
        setActivePanel(sectionNodePanel);
        sectionNodePanel.setActive(true);
        selectNode(sectionNodePanel.getNode());
        Utils.scrollToVisible(sectionNodePanel);
    }

    public SectionNode getRootNode() {
        return rootNode;
    }

    public void registerNode(SectionNode node) {
        nodes.put(node, node);
    }

    public SectionNode retrieveSectionNode(SectionNode node) {
        SectionNode sectionNode = nodes.get(node);
        return sectionNode == null ? rootNode : sectionNode;
    }

    /**
     * Recursively refreshes the view starting from the root node.
     */
    public void refreshView() {
        rootNode.refreshSubtree();
    }

    /**
     * Schedules refreshing of the view with default delay.
     * @see #refreshView
     * @see #REFRESH_DELAY
     */
    public void scheduleRefreshView() {
        refreshTask.schedule(REFRESH_DELAY);
    }

    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        rootNode.dataModelPropertyChange(source, propertyName, oldValue, newValue);
    }

    public abstract XmlMultiViewDataSynchronizer getModelSynchronizer();

    ;
}
