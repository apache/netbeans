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
package org.openide.explorer.view;

import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.Icon;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.swing.outline.CheckRenderDataProvider;
import org.netbeans.swing.outline.Outline;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

/**
 *
 * @author David Strupl
 */
class NodeRenderDataProvider implements CheckRenderDataProvider {

    private Outline table;
    private boolean showIcons = true;
    private Icon emptyIcon;

    /** Creates a new instance of NodeRenderDataProvider */
    public NodeRenderDataProvider(Outline table) {
        this.table = table;
    }

    public java.awt.Color getBackground(Object o) {
        return null;
    }

    public String getDisplayName(Object o) {
        Node n = Visualizer.findNode(o);
        if (n == null) {
            throw new IllegalStateException("TreeNode must be VisualizerNode but was: " + o + " of class " + o.getClass().getName());
        }
        String text = n.getHtmlDisplayName();
        if( null == text )
            text = n.getDisplayName();
        return text;
    }

    public java.awt.Color getForeground(Object o) {
        return null;
    }
    
    public void setShowIcons(boolean showIcons) {
        this.showIcons = showIcons;
        if (!showIcons && emptyIcon == null) {
            emptyIcon = new EmptyIcon();
        }
    }
    
    public boolean isShowIcons() {
        return showIcons;
    }

    public javax.swing.Icon getIcon(Object o) {
        if (!showIcons) {
            return emptyIcon;
        }
        Node n = Visualizer.findNode(o);
        if (n == null) {
            throw new IllegalStateException("TreeNode must be VisualizerNode but was: " + o + " of class " + o.getClass().getName());
        }
        boolean expanded = false;
        if (o instanceof TreeNode) {
            TreeNode tn = (TreeNode)o;
            ArrayList<TreeNode> al = new ArrayList<TreeNode> ();
            while (tn != null) {
                al.add(tn);
                tn = tn.getParent();
            }
            Collections.reverse(al);
            TreePath tp = new TreePath(al.toArray());
            AbstractLayoutCache layout = table.getLayoutCache();
            expanded = layout.isExpanded(tp);
        }
        java.awt.Image image = null;
        if (expanded) {
            image = n.getOpenedIcon(java.beans.BeanInfo.ICON_COLOR_16x16);
        } else {
            image = n.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16);
        }
        return ImageUtilities.image2Icon(image);
    }

    public String getTooltipText(Object o) {
        Node n = Visualizer.findNode(o);
        if (n == null) {
            throw new IllegalStateException("TreeNode must be VisualizerNode but was: " + o + " of class " + o.getClass().getName());
        }
        return n.getShortDescription();
    }

    public boolean isHtmlDisplayName(Object o) {
        Node n = Visualizer.findNode(o);
        if (n == null) {
            throw new IllegalStateException("TreeNode must be VisualizerNode but was: " + o + " of class " + o.getClass().getName());
        }
        return null != n.getHtmlDisplayName();
    }

    private CheckableNode getCheckCookie(Object o) {
        Node n = Visualizer.findNode(o);
        if (n == null) {
            throw new IllegalStateException("TreeNode must be VisualizerNode but was: " + o + " of class " + o.getClass().getName());
        }
        return n.getLookup().lookup(CheckableNode.class);
    }

    public boolean isCheckable(Object o) {
        CheckableNode c = getCheckCookie(o);
        return c != null && c.isCheckable();
    }

    public boolean isCheckEnabled(Object o) {
        CheckableNode c = getCheckCookie(o);
        return c != null && c.isCheckEnabled();
    }

    public Boolean isSelected(Object o) {
        CheckableNode c = getCheckCookie(o);
        if (c != null) {
            return c.isSelected();
        } else {
            return null;
        }
    }

    public void setSelected(Object o, Boolean selected) {
        CheckableNode c = getCheckCookie(o);
        if (c != null) {
            c.setSelected(selected);
        }
    }
    
    private static final class EmptyIcon implements Icon {

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
        }

        @Override
        public int getIconWidth() {
            return 0;
        }

        @Override
        public int getIconHeight() {
            return 0;
        }
        
    }

}
