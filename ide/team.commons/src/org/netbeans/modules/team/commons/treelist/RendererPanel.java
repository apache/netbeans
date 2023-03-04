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
package org.netbeans.modules.team.commons.treelist;

import org.netbeans.modules.team.commons.ColorManager;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 * Wrapper for node renderers. Defines appropriate foreground/background colors,
 * borders. Provides expansion button.
 *
 * @author S. Aubrecht
 */
final class RendererPanel extends JPanel {

    private static final ColorManager colorManager = ColorManager.getDefault();
    private static final Border NO_FOCUS_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ColorManager.getDefault().getDefaultBackground()),
            BorderFactory.createEmptyBorder(1, 1, 0, 1));
    private static Border INNER_BORDER;
    private static Color expandableRootBackground = null;
    private static Color expandableRootForeground = null;
    private static Color expandableRootSelectedBackground = null;
    private static Color expandableRootSelectedForeground = null;
    private static final Icon EMPTY_ICON = new EmptyIcon();
    private final boolean isRoot;
    private final TreeListNode node;
    ;
    private JButton expander;
    private int depth = 0;

    public RendererPanel(final TreeListNode node) {
        super(new BorderLayout());

        if (null == expandableRootBackground) {
            deriveColorsAndMargin();
        }

        this.node = node;
        isRoot = node.getParent() == null;
        setOpaque(!isRoot || !colorManager.isAqua() || !node.isExpandable() || node.getType().equals(TreeListNode.Type.TITLE) );
        if (node.isExpandable()) {
            expander = new LinkButton(EMPTY_ICON, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!node.isLoaded()) {
                        return;
                    }
                    node.setExpanded(!node.isExpanded());
                }
            });

            add(expander, BorderLayout.WEST);
        } else if (!isRoot || node.getType().equals(TreeListNode.Type.CLOSED)) {
            // Leaf nodes might need additional empty space to line up with expandable nodes
            // that have the expand icon, but in all known situations the leaf nodes are alone
            // (not mixed with expandable ones) and the empty space just makes the indent too big.
            // So commented out the line below.
//            add(new JLabel(new EmptyIcon()), BorderLayout.WEST);
        }
        depth = getDepth();
    }
    
    private int getDepth() {
        int d = 1;
        TreeListNode parent = node;
        while (parent.getParent() != null) {
            parent = parent.getParent();
            d++;
        }

        return d;
    }

    public void configure(Color foreground, Color background, boolean isSelected, boolean hasFocus, int nestingDepth, int rowHeight, int rowWidth) {
        if (isRoot && node.isExpandable() || node.getType().equals(TreeListNode.Type.CLOSED)) {
            foreground = isSelected ? expandableRootSelectedForeground : expandableRootForeground;
            background = isSelected ? expandableRootSelectedBackground : expandableRootBackground;
        } else if (node.getType().equals(TreeListNode.Type.TITLE)) {
            foreground = isSelected ? expandableRootSelectedForeground : colorManager.getDefaultBackground();
            background = isSelected ? colorManager.getTitleSelectedBackground() : colorManager.getTitleBackground();
        }
        int maxWidth = rowWidth - depth * EMPTY_ICON.getIconWidth() - (TreeList.INSETS_LEFT + nestingDepth * rowHeight / 2) - TreeList.INSETS_RIGHT;
        if (expander == null) {
            maxWidth += EMPTY_ICON.getIconWidth();
        }
        JComponent inner = node.getComponent(foreground, background, isSelected, hasFocus, maxWidth > 0 ? maxWidth : 0);
        if (node.isExpandable() || !isRoot || node.getType().equals(TreeListNode.Type.CLOSED)) {
            inner.setBorder(INNER_BORDER);
        }
        add(inner, BorderLayout.CENTER);

        setBackground(background);
        setForeground(foreground);
        
        if (null != expander) {
            expander.setEnabled(node.isLoaded());
            expander.setIcon(node.isLoaded() ? node.isExpanded() ? getExpandedIcon() : getCollapsedIcon() : EMPTY_ICON);
            expander.setPressedIcon(expander.getIcon());
        }
        Border border = null;
        if (hasFocus) {
            if (isSelected) {
                border = UIManager.getBorder("List.focusSelectedCellHighlightBorder"); // NOI18N
            }
            if (border == null) {
                border = UIManager.getBorder("List.focusCellHighlightBorder"); // NOI18N
            }
        }
        if (null == border) {
            border = NO_FOCUS_BORDER;
        }
        border = BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(TreeList.INSETS_TOP, TreeList.INSETS_LEFT + nestingDepth * rowHeight / 2,
                TreeList.INSETS_BOTTOM, TreeList.INSETS_RIGHT));

        try {
            setBorder(border);
        } catch (NullPointerException npe) {
            //workaround for 175940
            Logger.getLogger(RendererPanel.class.getName()).log(Level.INFO, "Bug #175940", npe);
        }

        configureAccessibility(this, true);
    }

    /**
     * Sets accessibility name and description on renderer component. They are derived from visible
     * labels and buttons contained in the components hierarchy provided by the node. It's a little
     * bit rough, but a general solution covering all types of nodes and their renderer composition.
     * @param combineAll true if accessible name should be combined from all components with visible text,
     *                   false to use just the first one
     */
    static void configureAccessibility(Component rendererComp, boolean combineAll) {
        StringBuilder accNameBuf = new StringBuilder();
        String accDesc = null;
        List<Component> comps = new LinkedList<>();
        comps.add(rendererComp);
        do {
            Component comp = comps.remove(0);
            if (comp.isVisible()) {
                if (comp instanceof JPanel) { // JPanel is the only type of container we use in the rendering components hierarchy
                    Component[] subComps = ((JPanel)comp).getComponents();
                    for (int i=0; i < subComps.length; i++) {
                        comps.add(i, subComps[i]);
                    }
                } else {
                    if (accNameBuf.length() == 0 || combineAll) {
                        String compAccName = comp.getAccessibleContext().getAccessibleName();
                        if (compAccName != null && compAccName.length() > 0) {
                            if (accNameBuf.length() > 0 && !compAccName.startsWith(" ")) { // NOI18N
                                accNameBuf.append(" "); // NOI18N
                            }
                            accNameBuf.append(compAccName);
                        }
                    }
                    if (accDesc == null) {
                        String compAccDesc = comp.getAccessibleContext().getAccessibleDescription();
                        if (compAccDesc != null && compAccDesc.length() > 0) {
                            accDesc = compAccDesc;
                        }
                    }
                }
            }
        } while (!comps.isEmpty() && (combineAll || accNameBuf.length() == 0 || accDesc == null));

        String accName = accNameBuf.toString();
        if (accName.length() > 0) {
            accName = accName.replace("<html>", "").replace("( ", "(").replace(" | ", ", ").replace(" )", ")").replace("...", "").replace("</html>", ""); // NOI18N
            rendererComp.getAccessibleContext().setAccessibleName(accName);
        }
        if (accDesc != null) {
            accDesc = accDesc.replace("<html>", "").replace("</html>", ""); // NOI18N
        } else {
            accDesc = accName;
        }
        if (accDesc.length() > 0) {
            rendererComp.getAccessibleContext().setAccessibleDescription(accDesc);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        if (isRoot && colorManager.isAqua() && node.isExpandable() && node.isRenderedWithGradient()) {
            Graphics2D g2d = (Graphics2D) g;
            Paint oldPaint = g2d.getPaint();
            g2d.setPaint(new GradientPaint(0, 0, Color.white, 0, getHeight() / 2, getBackground()));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setPaint(oldPaint);
        } else {
            super.paintComponent(g);
        }
    }
    
    @Override
    public String getToolTipText(MouseEvent event) {
        Component c = SwingUtilities.getDeepestComponentAt(this, event.getX(), event.getY());
        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            String tooltip = jc.getToolTipText();
            if (null != tooltip) {
                return tooltip;
            }
        }
        return super.getToolTipText(event);
    }

    /**
     * Initialize the various colors we will be using. (copied from
     * org.openide.explorer.propertysheet.PropUtils)
     */
    private static void deriveColorsAndMargin() {
        expandableRootBackground = colorManager.getExpandableRootBackground();
        expandableRootForeground = colorManager.getExpandableRootForeground();
        expandableRootSelectedBackground = colorManager.getExpandableRootSelectedBackground();
        expandableRootSelectedForeground = colorManager.getExpandableRootSelectedForeground();

        Integer i = (Integer) UIManager.get("netbeans.ps.iconmargin"); //NOI18N

        int iconMargin = 0;
        if (i != null) {
            iconMargin = i.intValue();
        } else {
            if (colorManager.isWindows()) {
                iconMargin = 4;
            } else {
                iconMargin = 0;
            }
        }
        INNER_BORDER = BorderFactory.createEmptyBorder(0, iconMargin, 0, 0);
    }

    /**
     * Get the icon displayed by an expanded set. Typically this is just the
     * same icon the look and feel supplies for trees
     */
    static Icon getExpandedIcon() {
        Icon expandedIcon = UIManager.getIcon(colorManager.isGtk() ? "Tree.gtk_expandedIcon" : "Tree.expandedIcon"); //NOI18N
        assert expandedIcon != null : "no Tree.expandedIcon found"; //NOI18N
        return expandedIcon;
    }

    /**
     * Get the icon displayed by a collapsed set. Typically this is just the
     * icon the look and feel supplies for trees
     */
    static Icon getCollapsedIcon() {
        Icon collapsedIcon = UIManager.getIcon(colorManager.isGtk() ? "Tree.gtk_collapsedIcon" : "Tree.collapsedIcon"); //NOI18N
        assert collapsedIcon != null : "no Tree.collapsedIcon found"; //NOI18N
        return collapsedIcon;
    }

    private static class EmptyIcon implements Icon {

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
        }

        @Override
        public int getIconWidth() {
            return getExpandedIcon().getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return getExpandedIcon().getIconHeight();
        }
    }
}
