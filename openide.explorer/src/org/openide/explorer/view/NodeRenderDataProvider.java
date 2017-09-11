/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.openide.explorer.view;

import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.swing.outline.CheckRenderDataProvider;
import org.netbeans.swing.outline.Outline;
import org.openide.nodes.Node;

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
        return new ImageIcon(image);
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
