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
package org.netbeans.swing.outline;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.CellRendererPane;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.TreePath;

/** An outline-aware TableCellRenderer which knows how to paint expansion
 * handles and indent child nodes an appropriate amount. 
 *
 * @author  Tim Boudreau
 */
public class DefaultOutlineCellRenderer extends DefaultTableCellRenderer {
    private static int expansionHandleWidth = 0;
    private static int expansionHandleHeight = 0;
    private boolean expanded = false;
    private boolean leaf = true;
    private boolean showHandle = true;
    private int nestingDepth = 0;
    private int labelTextGap = 0;
    private final JCheckBox theCheckBox;
    private final CellRendererPane fakeCellRendererPane;
    private JCheckBox checkBox;
    private Reference<RenderDataProvider> lastRendererRef = new WeakReference<RenderDataProvider>(null); // Used by lazy tooltip
    private Reference<Object> lastRenderedValueRef = new WeakReference<Object>(null);                    // Used by lazy tooltip
    private static final Border expansionBorder = new ExpansionHandleBorder();
    private static final Class htmlRendererClass = useSwingHtmlRendering() ? null : HtmlRenderer.getDelegate();
    private final HtmlRenderer.Renderer htmlRenderer = (htmlRendererClass != null) ? HtmlRenderer.createRenderer(htmlRendererClass) : null;
    private final boolean swingRendering = htmlRenderer == null;
    
    private static boolean useSwingHtmlRendering() {
        try {
            return Boolean.getBoolean("nb.useSwingHtmlRendering");              // NOI18N
        } catch (SecurityException se) {
            return false;
        }
    }
    
    /** Creates a new instance of DefaultOutlineTreeCellRenderer */
    public DefaultOutlineCellRenderer() {
        theCheckBox = createCheckBox();
        // In order to paint the check-box correctly, following condition must be true:
        // SwingUtilities.getAncestorOfClass(CellRendererPane.class, theCheckBox) != null
        // (See e.g.: paintSkin() method in com/sun/java/swing/plaf/windows/XPStyle.java)
        fakeCellRendererPane = new CellRendererPane();
        fakeCellRendererPane.add(theCheckBox);
    }
    
    final JCheckBox createCheckBox() {
        JCheckBox cb = new JCheckBox();
        cb.setSize(cb.getPreferredSize());
        cb.setBorderPainted(false);
        cb.setOpaque(false);
        return cb;
    }
    
    /** Overridden to combine the expansion border (whose insets determine how
     * much a child tree node is shifted to the right relative to the ancestor
     * root node) with whatever border is set, as a CompoundBorder.  The expansion
     * border is also responsible for drawing the expansion icon.
     * @param b the border to be rendered for this component
     */
    @Override
    public final void setBorder (Border b) {
        b = new RestrictedInsetsBorder(b);
        if (!swingRendering) {
            super.setBorder(b);
            return ;
        }
        if (b == expansionBorder) {
            super.setBorder(b);
        } else {
            super.setBorder(BorderFactory.createCompoundBorder (b, expansionBorder));
        }
    }

    @Override
    protected void setValue(Object value) {
        if (swingRendering) {
            super.setValue(value);
        }
    }

    private static Icon getDefaultOpenIcon() {
	return UIManager.getIcon("Tree.openIcon"); //NOI18N
    }

    private static Icon getDefaultClosedIcon() {
	return UIManager.getIcon("Tree.closedIcon"); //NOI18N
    }

    private static Icon getDefaultLeafIcon() {
	return UIManager.getIcon("Tree.leafIcon"); //NOI18N
    }
    
    static Icon getExpandedIcon() {
        return UIManager.getIcon ("Tree.expandedIcon"); //NOI18N
    }
    
    static Icon getCollapsedIcon() {
        return UIManager.getIcon ("Tree.collapsedIcon"); //NOI18N
    }
    
    static int getNestingWidth() {
        return getExpansionHandleWidth();
    }

    static int getExpansionHandleWidth() {
        if (expansionHandleWidth == 0) {
            expansionHandleWidth = getExpandedIcon ().getIconWidth ();
        }
        return expansionHandleWidth;
    }

    static int getExpansionHandleHeight() {
        if (expansionHandleHeight == 0) {
            expansionHandleHeight = getExpandedIcon ().getIconHeight ();
        }
        return expansionHandleHeight;
    }
    
    private void setNestingDepth (int i) {
        nestingDepth = i;
    }
    
    private void setExpanded (boolean val) {
        expanded = val;
    }
    
    private void setLeaf (boolean val) {
        leaf = val;
    }
    
    private void setShowHandle (boolean val) {
        showHandle = val;
    }

    private void setCheckBox(JCheckBox checkBox) {
        this.checkBox = checkBox;
    }
    
    private boolean isLeaf () {
        return leaf;
    }
    
    private boolean isExpanded () {
        return expanded;
    }
    
    private boolean isShowHandle() {
        return showHandle;
    }
    
    private void setLabelTextGap(int labelTextGap) {
        this.labelTextGap = labelTextGap;
    }
    
    private int getLabelTextGap() {
        return labelTextGap;
    }
    
    /** Set the nesting depth - the number of path elements below the root.
     * This is set in getTableCellEditorComponent(), and retrieved by the
     * expansion border to determine how far to the right to indent the current
     * node. */
    private int getNestingDepth() {
        return nestingDepth;
    }

    private JCheckBox getCheckBox() {
        return checkBox;
    }
    
    final JCheckBox setUpCheckBox(CheckRenderDataProvider crendata, Object value, JCheckBox cb) {
        Boolean chSelected = crendata.isSelected(value);
        cb.setEnabled(true);
        cb.setSelected(!Boolean.FALSE.equals(chSelected));
        // Third state is "selected armed" to be consistent with org.openide.explorer.propertysheet.ButtonModel3Way
        cb.getModel().setArmed(chSelected == null);
        cb.getModel().setPressed(chSelected == null);
        cb.setEnabled(crendata.isCheckEnabled(value));
        cb.setBackground(getBackground());
        return cb;
    }

    int getTheCheckBoxWidth() {
        return theCheckBox.getSize().width;
    }
    
    /** Get a component that can render cells in an Outline.  If 
     * <code>((Outline) table).isTreeColumnIndex(column)</code> is true,
     * it will paint as indented and with an expansion handle if the 
     * Outline's model returns false from <code>isLeaf</code> for the
     * passed value. 
     * <p>
     * If the column is not the tree column, its behavior is the same as
     * DefaultTableCellRenderer.
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                          boolean isSelected, boolean hasFocus, int row, 
                          int column) {

        setForeground(null);
        setBackground(null);
        setLabelTextGap(0);
        super.getTableCellRendererComponent(
                  table, value, isSelected, hasFocus, row, column);
        JLabel label = null;
        if (!swingRendering) {
            htmlRenderer.setColors(getForeground(), getBackground());
            label = (JLabel) htmlRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
        Outline tbl = (Outline) table;
        if (tbl.isTreeColumnIndex(column)) {
            AbstractLayoutCache layout = tbl.getLayoutCache();
            row = tbl.convertRowIndexToModel(row);
            boolean isleaf = tbl.getOutlineModel().isLeaf(value);
            setLeaf(isleaf);
            setShowHandle(true);
            TreePath path = layout.getPathForRow(row);
            boolean isExpanded = layout.isExpanded(path);
            setExpanded (isExpanded);
            int nd = path == null ? 0 : path.getPathCount() - (tbl.isRootVisible() ? 1 : 2);
            if (nd < 0) {
                nd = 0;
            }
            setNestingDepth (nd );
            RenderDataProvider rendata = tbl.getRenderDataProvider();
            Icon icon = null;
            if (rendata != null && value != null) {
                String displayName = rendata.getDisplayName(value);
                if (displayName != null) {
                    if (rendata.isHtmlDisplayName(value) && !(displayName.startsWith("<html") || displayName.startsWith("<HTML"))) {
                        if (swingRendering) {
                            setText("<html>" + displayName.replace(" ", "&nbsp;") + "</html>"); // NOI18N
                        } else {
                            label.setText("<html>" + displayName.replace(" ", "&nbsp;") + "</html>"); // NOI18N
                        }
                    } else {
                        if (swingRendering) {
                            setText (displayName);
                        } else {
                            label.setText (displayName);
                        }
                    }
                }
                lastRendererRef = new WeakReference<RenderDataProvider>(rendata);
                lastRenderedValueRef = new WeakReference<Object>(value);
                Color bg = rendata.getBackground(value);
                Color fg = rendata.getForeground(value);
                if (bg != null && !isSelected) {
                    if (swingRendering) {
                        setBackground (bg);
                    } else {
                        label.setBackground (bg);
                    }
                } else {
                    if (!swingRendering) {
                        label.setBackground(getBackground());
                    }
                }
                if (fg != null && !isSelected) {
                    if (swingRendering) {
                        setForeground (fg);
                    } else {
                        label.setForeground (fg);
                    }
                } else {
                    if (!swingRendering) {
                        label.setForeground(getForeground());
                    }
                }
                icon = rendata.getIcon(value);

                JCheckBox cb = null;
                if (rendata instanceof CheckRenderDataProvider) {
                    CheckRenderDataProvider crendata = (CheckRenderDataProvider) rendata;
                    if (crendata.isCheckable(value)) {
                        cb = setUpCheckBox(crendata, value, theCheckBox);
                    }
                }
                setCheckBox(cb);
            } else {
                setCheckBox(null);
            }
            if (icon == null) {
                if (!isleaf) {
                    if (isExpanded) {
                        icon = getDefaultOpenIcon();
                    } else { // ! expanded
                        icon = getDefaultClosedIcon();
                    }
                } else { // leaf
                    icon = getDefaultLeafIcon();
                }
            }
            if (swingRendering) {
                setIcon(icon);
            } else {
                label.setIcon(icon);
            }
            if (icon == null || icon.getIconWidth() == 0) {
                setLabelTextGap(getIconTextGap());
            }
        } else { // ! tbl.isTreeColumnIndex(column)
            setCheckBox(null);
            if (swingRendering) {
                setIcon(null);
            } else {
                label.setIcon(null);
            }
            setShowHandle(false);
            lastRendererRef = new WeakReference<RenderDataProvider>(null);
            lastRenderedValueRef = new WeakReference<Object>(null);
        }

        if (swingRendering) {
            return this;
        } else {
            Border b = getBorder();
            if (b == null) {
                label.setBorder(expansionBorder);
            } else {
                label.setBorder(BorderFactory.createCompoundBorder (b, expansionBorder));
            }
            label.setOpaque(true);

            label.putClientProperty(DefaultOutlineCellRenderer.class, this);
            return label;
        }
    }

    @Override
    public String getToolTipText() {
        // Retrieve the tooltip only when someone asks for it...
        RenderDataProvider rendata = lastRendererRef.get();
        Object value = lastRenderedValueRef.get();
        if (rendata != null && value != null) {
            String toolT = rendata.getTooltipText(value);
            if (toolT != null && (toolT = toolT.trim ()).length () > 0) {
                return toolT;
            }
        }
        return super.getToolTipText();
    }

    private static class ExpansionHandleBorder implements Border {

        private static final boolean isGtk = "GTK".equals (UIManager.getLookAndFeel ().getID ()); //NOI18N
        private static final boolean isNimbus = "Nimbus".equals (UIManager.getLookAndFeel ().getID ()); //NOI18N

        private Insets insets = new Insets(0,0,0,0);
        private static JLabel lExpandedIcon = null;
        private static JLabel lCollapsedIcon = null;

        {
            if (isGtk) {
                lExpandedIcon = new JLabel (getExpandedIcon (), SwingUtilities.TRAILING);
                lCollapsedIcon = new JLabel (getCollapsedIcon (), SwingUtilities.TRAILING);
            }
        }

        @Override
        public Insets getBorderInsets(Component c) {
            DefaultOutlineCellRenderer ren = (DefaultOutlineCellRenderer)
                    ((JComponent) c).getClientProperty(DefaultOutlineCellRenderer.class);
            if (ren == null) {
                ren = (DefaultOutlineCellRenderer) c;
            }
            if (ren.isShowHandle()) {
                insets.left = getExpansionHandleWidth() + (ren.getNestingDepth() *
                    getNestingWidth()) + ren.getLabelTextGap();
                //Defensively adjust all the insets fields
                insets.top = 1;
                insets.right = 1;
                insets.bottom = 1;
            } else {
                //Defensively adjust all the insets fields
                insets.left = 1;
                insets.top = 1;
                insets.right = 1;
                insets.bottom = 1;
            }
            if (ren.getCheckBox() != null) {
                insets.left += ren.getCheckBox().getSize().width;
            }
            return insets;
        }
        
        @Override
        public boolean isBorderOpaque() {
            return false;
        }
        
        @Override
        public void paintBorder(Component c, java.awt.Graphics g, int x, int y, int width, int height) {
            DefaultOutlineCellRenderer ren = (DefaultOutlineCellRenderer)
                    ((JComponent) c).getClientProperty(DefaultOutlineCellRenderer.class);
            if (ren == null) {
                ren = (DefaultOutlineCellRenderer) c;
            }
            if (ren.isShowHandle() && !ren.isLeaf()) {
                Icon icon = ren.isExpanded() ? getExpandedIcon() : getCollapsedIcon();
                int iconY;
                int iconX = ren.getNestingDepth() * getNestingWidth();
                if (icon.getIconHeight() < height) {
                    iconY = (height / 2) - (icon.getIconHeight() / 2);
                } else {
                    iconY = 0;
                }
                if (isNimbus) {
                    iconX += icon.getIconWidth()/3; // To look good
                }
                if (isGtk) {
                    JLabel lbl = ren.isExpanded () ? lExpandedIcon : lCollapsedIcon;
                    lbl.setSize (Math.max (getExpansionHandleWidth (), iconX + getExpansionHandleWidth ()), height);
                    lbl.paint (g);
                } else {
                    icon.paintIcon(c, g, iconX, iconY);
                }
            }
            JCheckBox chBox = ren.getCheckBox();
            if (chBox != null) {
                int chBoxX = getExpansionHandleWidth() + ren.getNestingDepth() * getNestingWidth();
                Rectangle bounds = chBox.getBounds();
                int chBoxY;
                if (bounds.getHeight() < height) {
                    chBoxY = (height / 2) - (((int) bounds.getHeight()) / 2);
                } else {
                    if (isNimbus) {
                        chBoxY = 1;
                    } else {
                        chBoxY = 0;
                    }
                }
                Dimension chDim = chBox.getSize();
                java.awt.Graphics gch = g.create(chBoxX, chBoxY, chDim.width, chDim.height);
                chBox.paint(gch);
            }
        }
    }
    
    /**
     * Use reflection to access org.openide.awt.HtmlRenderer class
     * so that we do not have to have a dependency on org.openide.awt module.
     */
    private static final class HtmlRenderer {
        
        private static final String HTML_RENDERER_CLASS = "org.openide.awt.HtmlRenderer";   // NOI18N
        
        static Class getDelegate() {
            Class delegate;
            try {
                delegate = ClassLoader.getSystemClassLoader().loadClass(HTML_RENDERER_CLASS);
            } catch (ClassNotFoundException ex) {
                try {
                    delegate = Thread.currentThread().getContextClassLoader().loadClass(HTML_RENDERER_CLASS);
                } catch (ClassNotFoundException ex2) {
                    // We are searching for org.openide.awt.HtmlRenderer class.
                    // However, we can not find it directly from the system class loader.
                    // We need to find it via Lookup
                    try {
                        Class lookupClass = ClassLoader.getSystemClassLoader().loadClass("org.openide.util.Lookup");    // NOI18N
                        try {
                            Object defaultLookup = lookupClass.getMethod("getDefault").invoke(null);    // NOI18N
                            ClassLoader systemClassLoader = (ClassLoader) lookupClass.getMethod("lookup", Class.class).invoke(defaultLookup, ClassLoader.class);    // NOI18N
                            if (systemClassLoader == null) {
                                return null;
                            }
                            delegate = systemClassLoader.loadClass(HTML_RENDERER_CLASS);
                        } catch (NoSuchMethodException mex) {
                            Logger.getLogger(DefaultOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                            return null;
                        } catch (SecurityException mex) {
                            Logger.getLogger(DefaultOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                            return null;
                        } catch (IllegalAccessException mex) {
                            Logger.getLogger(DefaultOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                            return null;
                        } catch (IllegalArgumentException mex) {
                            Logger.getLogger(DefaultOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                            return null;
                        } catch (InvocationTargetException mex) {
                            Logger.getLogger(DefaultOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                            return null;
                        }
                    } catch (ClassNotFoundException ex3) {
                        return null;
                    } catch (SecurityException se) {
                        return null;
                    }
                } catch (SecurityException se) {
                    return null;
                }
            } catch (SecurityException se) {
                return null;
            }
            return delegate;
        }

        private static Renderer createRenderer(Class htmlRendererClass) {
            try {
                Method createRenderer = htmlRendererClass.getMethod("createRenderer");                  // NOI18N
                return new Renderer(createRenderer.invoke(null));
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(DefaultOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            } catch (SecurityException ex) {
                Logger.getLogger(DefaultOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            } catch (IllegalAccessException ex) {
                Logger.getLogger(DefaultOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(DefaultOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            } catch (InvocationTargetException ex) {
                Logger.getLogger(DefaultOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }

        private static class Renderer {
            
            private Object renderer;
            private Method getTableCellRendererComponent;

            private Renderer(Object renderer) throws NoSuchMethodException {
                this.renderer = renderer;
                this.getTableCellRendererComponent = TableCellRenderer.class.getMethod(
                        "getTableCellRendererComponent",                                        // NOI18N
                        JTable.class, Object.class, Boolean.TYPE, Boolean.TYPE, Integer.TYPE, Integer.TYPE);
            }
            
            public Component getTableCellRendererComponent(
                JTable table, Object value, boolean selected, boolean leadSelection, int row, int column
            ) {
                try {
                    return (Component) getTableCellRendererComponent.invoke(
                            renderer,
                            table, value, selected, leadSelection, row, column);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(DefaultOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                    throw new IllegalStateException(ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(DefaultOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                    throw new IllegalStateException(ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(DefaultOutlineCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
                    throw new IllegalStateException(ex);
                }
            }
            
            private void setColors(Color foreground, Color background) {
                Component c = (Component) renderer;
                c.setForeground(foreground);
                c.setBackground(background);
            }
        }
        
    }
    
    private static class RestrictedInsetsBorder implements Border {
        
        private final Border delegate;
        
        public RestrictedInsetsBorder(Border delegate) {
            this.delegate = delegate;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            delegate.paintBorder(c, g, x, y, width, height);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            Insets insets = delegate.getBorderInsets(c);
            if (insets.top > 1 || insets.left > 1 || insets.bottom > 1 || insets.right > 1) {
                insets = new Insets(Math.min(insets.top, 1),
                                    Math.min(insets.left, 1),
                                    Math.min(insets.bottom, 1),
                                    Math.min(insets.right, 1));
            }
            return insets;
        }

        @Override
        public boolean isBorderOpaque() {
            return delegate.isBorderOpaque();
        }
    }
}
