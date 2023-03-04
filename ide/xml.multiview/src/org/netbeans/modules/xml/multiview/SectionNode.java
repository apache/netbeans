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
package org.netbeans.modules.xml.multiview;

import org.netbeans.modules.xml.multiview.ui.BoxPanel;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodePanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;

import java.util.List;
import java.util.LinkedList;
import java.awt.*;

/**
 * This class represents a section node. In other words, this class represents
 * a node that in turn represents a section.
 *
 * @author pfiala
 *
 * @see org.netbeans.modules.xml.multiview.ui.SectionNodePanel
 * @see org.netbeans.modules.xml.multiview.ui.SectionNodeView
 * @see org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel
 *
 */
public class SectionNode extends AbstractNode {

    protected final Object key;
    private boolean expanded = false;
    private SectionNodePanel sectionPanel = null;
    private final String iconBase;
    private final SectionNodeView sectionNodeView;
    protected boolean helpProvider = false;

    /**
     * Create a new section node with a given child set.
     *
     * @param sectionNodeView
     * @param children the children for this node.
     * @param key the key by which this node is identified
     * @param title the title for the node
     * @param iconBase base resource for icons (without initial slash)
     */
    protected SectionNode(SectionNodeView sectionNodeView, Children children, Object key, String title,
                                                         String iconBase) {
        super(children);
        this.sectionNodeView = sectionNodeView;
        this.key = key;
        super.setDisplayName(title);
        super.setIconBaseWithExtension(iconBase + ".gif"); //NOI18N
        this.iconBase = iconBase;
        sectionNodeView.registerNode(this);
    }

    public SectionNodeView getSectionNodeView() {
        return sectionNodeView;
    }

    public Object getKey() {
        return key;
    }

    public void addChild(SectionNode node) {
        getChildren().add(new Node[]{node});
    }

    /**
     * Creates an inner panel for this and populates
     * it with the children of this node (if there are any).
     */
    public SectionNodeInnerPanel createInnerPanel() {
        Children children = getChildren();
        if (children.getNodesCount() == 0) {
            return createNodeInnerPanel();
        } else {
            BoxPanel boxPanel = new BoxPanel(sectionNodeView);
            populateBoxPanel(boxPanel);
            return boxPanel;
        }
    }

    /**
     * Populates the associated inner panel with the children
     * of this.
     */
    public void populateBoxPanel() {
        SectionInnerPanel innerPanel = getSectionNodePanel().getInnerPanel();
        if (innerPanel instanceof BoxPanel) {
            populateBoxPanel((BoxPanel) innerPanel);
        }
    }
    
    /**
     * Populates the given box panel with the children
     * of this.
     * @param boxPanel the panel to be populated
     */
    public void populateBoxPanel(BoxPanel boxPanel) {
        List<Component> nodeList = new LinkedList<>();
        SectionInnerPanel nodeInnerPanel = createNodeInnerPanel();
        if (nodeInnerPanel != null) {
            nodeList.add(nodeInnerPanel);
        }
        Node[] nodes = getChildren().getNodes();
        for (int i = 0; i < nodes.length; i++) {
            nodeList.add(((SectionNode) nodes[i]).getSectionNodePanel());
        }
        boxPanel.setComponents(nodeList.toArray(new Component[0]));
    }


    public HelpCtx getHelpCtx() {
        if (helpProvider) {
            return new HelpCtx(getClass());
        }
        final Node parentNode = getParentNode();
        if (parentNode instanceof SectionNode) {
            return ((SectionNode) parentNode).getHelpCtx();
        } else {
            return new HelpCtx(sectionNodeView.getClass());
        }
    }

    public boolean canDestroy() {
        return true;
    }

    /**
     * Creates appropriate SectionNodeInnerPanel. Override in 
     * subclasses, default implementation just returns null.
     */
    protected SectionNodeInnerPanel createNodeInnerPanel() {
        return null;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public SectionNodePanel getSectionNodePanel() {
        if (sectionPanel == null) {
            sectionPanel = createSectionNodePanel();
        }
        return sectionPanel;
    }
    
    /**
     * Creates appropriate SectionNodePanel. Override in 
     * subclasses if necessary.
     */
    protected SectionNodePanel createSectionNodePanel() {
        return new SectionNodePanel(this);
    }    

    public String getIconBase() {
        return iconBase;
    }

    public boolean equals(Object obj) {
        if (getClass() == obj.getClass()) {
            if (key.equals(((SectionNode) obj).key)) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        return key.hashCode();
    }
                 
    public final void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        if (sectionPanel != null) {
            SectionInnerPanel innerPanel = sectionPanel.getInnerPanel();
            if (innerPanel != null) {
                innerPanel.dataModelPropertyChange(source, propertyName, oldValue, newValue);
            }
        }
        Children children = getChildren();
        if (children != null) {
            Node[] nodes = children.getNodes();
            for (int i = 0; i < nodes.length; i++) {
                Node node = nodes[i];
                if (node instanceof SectionNode) {
                    ((SectionNode) node).dataModelPropertyChange(source, propertyName, oldValue, newValue);
                }
            }
        }
    }

    
    /**
     * Recursively refreshes view of the associated inner panel of this node and 
     * all its children nodes.
     */
    public void refreshSubtree() {
        if (sectionPanel != null) {
            SectionInnerPanel innerPanel = sectionPanel.getInnerPanel();
            if (innerPanel != null) {
                innerPanel.refreshView();
            }
        }
        Children children = getChildren();
        if (children != null) {
            Node[] nodes = children.getNodes();
            for (int i = 0; i < nodes.length; i++) {
                Node node = nodes[i];
                if (node instanceof SectionNode) {
                    ((SectionNode) node).refreshSubtree();
                }
            }
        }
    }

    /**
     * Gets the node associated with the given <code>element</code>. Searches
     * recursively from the children nodes. 
     * @param element the object representing the key of the node that we're looking
     * for
     */
    public SectionNode getNodeForElement(Object element) {
        if (key.equals(element)) {
            return this;
        } else {
            final Node[] nodes = getChildren().getNodes();
            for (int i = 0; i < nodes.length; i++) {
                Node node = nodes[i];
                if (node instanceof SectionNode) {
                    final SectionNode nodeForElement = ((SectionNode) node).getNodeForElement(element);
                    if (nodeForElement != null) {
                        return nodeForElement;
                    }
                }
            }
            return null;
        }
    }
}
