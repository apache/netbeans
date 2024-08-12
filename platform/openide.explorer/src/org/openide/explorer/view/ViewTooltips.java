/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.explorer.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import org.openide.util.Utilities;

/**
 * Displays pseudo-tooltips for tree and list views which don't have enough
 * space.  This class is not NB specific, and can be used with any
 * JTree or JList.
 *
 * @author Tim Boudreau
 */
final class ViewTooltips extends MouseAdapter implements MouseMotionListener {
    private static final Object KEY = new Object();
    
    /** A reference count for number of comps listened to */
    private int refcount = 0;
    /** The last known component we were invoked against, nulled on hide() */
    private JComponent inner = null;
    /** The last row we were invoked against */
    private int row = -1;
    /** An array of currently visible popups */
    private Popup[] popups = new Popup[2];
    /** A component we'll reuse to paint into the popups */
    private ImgComp painter = new ImgComp();
    /** Nobody should instantiate this */
    private ViewTooltips() {
    }
    
    /**
     * Register a child of a JScrollPane (only JList or JTree supported 
     * for now) which should show helper tooltips.  Should be called
     * from the component's addNotify() method.
     */
    static synchronized void register (JComponent comp) {
        ViewTooltips vtt = (ViewTooltips) comp.getClientProperty(KEY);
        assert vtt == null : "There already is " + vtt + " for " + comp;
        comp.putClientProperty(KEY, vtt = new ViewTooltips());
        vtt.attachTo (comp);
    }
    
    /**
     * Unregister a child of a JScrollPane (only JList or JTree supported 
     * for now) which should show helper tooltips. Should be called
     * from the component's removeNotify() method.
     */
    static synchronized void unregister (JComponent comp) {
        ViewTooltips inst = (ViewTooltips) comp.getClientProperty(KEY);
        if (inst == null) {
            return;
        }
        int zero = inst.detachFrom(comp);
        assert zero == 0 : "The " + inst + " should no longer be needed, was: " + zero;
        inst.hide();
        comp.putClientProperty(KEY, null);
    }

    /** Start listening to mouse motion on the passed component */
    private void attachTo (JComponent comp) {
        assert comp instanceof JTree || comp instanceof JList;
        comp.addMouseListener (this);
        comp.addMouseMotionListener (this);
        refcount++;
    }
    
    /** Stop listening to mouse motion on the passed component */
    private int detachFrom (JComponent comp) {
        assert comp instanceof JTree || comp instanceof JList;
        comp.removeMouseMotionListener (this);
        comp.removeMouseListener (this);
        return --refcount;
    }
    
    public void mouseMoved(MouseEvent e) {
        Point p = e.getPoint();
        JComponent comp = (JComponent) e.getSource();
        JScrollPane jsp = (JScrollPane) 
            SwingUtilities.getAncestorOfClass(JScrollPane.class, comp);
        if (jsp != null) {
            p = SwingUtilities.convertPoint (comp, p, jsp);
            show(jsp, p);
        }
    }

    public void mouseDragged(MouseEvent e) {
        hide();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        hide();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        hide();
    }
    
    /** Shows the appropriate popups given the state of the scroll pane and
     * its view. 
     * @param view The scroll pane owning the component the event happened on
     * @param pt The point at which the mouse event happened, in the coordinate
     *  space of the scroll pane.
     */
    void show (JScrollPane view, Point pt) {
        if (view.getViewport().getView() instanceof JTree) {
            showJTree (view, pt);
        } else if (view.getViewport().getView() instanceof JList) {
            showJList (view, pt);
        } else {
            assert false : "Bad component type registered: " + view.getViewport().getView();
        }
    }
    
    private void showJList (JScrollPane view, Point pt) {
        JList list = (JList) view.getViewport().getView();
        Point p = SwingUtilities.convertPoint(view, pt.x, pt.y, list);
        int row = list.locationToIndex(p);
        if (row == -1) {
            hide();
            return;
        }
        Rectangle bds = list.getCellBounds(row, 
                row);
        //GetCellBounds returns a width that is the
        //full component width;  we want only what
        //the renderer really needs.
        ListCellRenderer ren = list.getCellRenderer();
        Dimension rendererSize = 
                ren.getListCellRendererComponent(list, 
                list.getModel().getElementAt(row), 
                row, false, false).getPreferredSize();
        
        bds.width = rendererSize.width;
        if (bds == null || !bds.contains(p)) {
            hide();
            return;
        }
        if (setCompAndRow (list, row)) {
            Rectangle visible = getShowingRect (view);
            Rectangle[] rects = getRects (bds, visible);
            if (rects.length > 0) {
                ensureOldPopupsHidden();
                painter.configure(
                        list.getModel().getElementAt(row), 
                        view, list, row);
                showPopups (rects, bds, visible, list, view);
            } else {
                hide();
            }
        }
    }
    
    private void showJTree (JScrollPane view, Point pt) {
        JTree tree = (JTree) view.getViewport().getView();
        Point p = SwingUtilities.convertPoint(view, 
                pt.x, pt.y, tree);
        
        int row = tree.getClosestRowForLocation(
                p.x, p.y);
        
        TreePath path = 
                tree.getClosestPathForLocation(p.x, 
                p.y);
        
        Rectangle bds = tree.getPathBounds(path);
        if (bds == null || !bds.contains(p)) {
            hide();
            return;
        }
        if (setCompAndRow (tree, row)) {
            Rectangle visible = getShowingRect (view);
            Rectangle[] rects = getRects (bds, visible);
            if (rects.length > 0) {
                ensureOldPopupsHidden();
                painter.configure(
                        path.getLastPathComponent(), 
                        view, tree, path, row);
                showPopups (rects, bds, visible, tree, view);
            } else {
                hide();
            }
        }
    }
    
    /**
     * Set the currently shown component and row, returning true if they are
     * not the same as the last known values.
     */
    private boolean setCompAndRow (JComponent inner, int row) {
        boolean rowChanged = row != this.row;
        boolean compChanged = inner != this.inner;
        this.inner = inner;
        this.row = row;
        return (rowChanged || compChanged);
    }
    
    /**
     * Hide all popups and discard any references to the components the
     * popups were showing for.
     */
    void hide() {
        ensureOldPopupsHidden();
        if (painter != null) {
            painter.clear();
        }
        setHideComponent (null, null);
        inner = null;
        row = -1;
    }
    
    private void ensureOldPopupsHidden() {
        for (int i=0; i < popups.length; i++) {
            if (popups[i] != null) {
                popups[i].hide();
                popups[i] = null;
            }
        }
    }

    /**
     * Gets the sub-rectangle of a JScrollPane's area that
     * is actually showing the view
     */
    private Rectangle getShowingRect (JScrollPane pane) {
        Insets ins1 = pane.getViewport().getInsets();
        Border inner = pane.getViewportBorder();
        Insets ins2;
        if (inner != null) {
            ins2 = inner.getBorderInsets(pane);
        } else {
            ins2 = new Insets (0,0,0,0);
        }
        Insets ins3 = new Insets(0,0,0,0);
        if (pane.getBorder() != null) {
            ins3 = pane.getBorder().getBorderInsets(pane);
        }
        
        Rectangle r = pane.getViewportBorderBounds();
        r.translate(-r.x, -r.y);
        r.width -= ins1.left + ins1.right;
        r.width -= ins2.left + ins2.right;
        r.height -= ins1.top + ins1.bottom;
        r.height -= ins2.top + ins2.bottom;
        r.x -= ins2.left;
        r.x -= ins3.left;
        Point p = pane.getViewport().getViewPosition();
        r.translate (p.x, p.y);
        r = SwingUtilities.convertRectangle(pane.getViewport(), r, pane);
        return r;
    }
    
    /**
     * Fetches an array or rectangles representing the non-overlapping
     * portions of a cell rect against the visible portion of the component.
     * @bds The cell's bounds, in the coordinate space of the tree or list
     * @vis The visible area of the tree or list, in the tree or list's coordinate space
     */
    private static final Rectangle[] getRects(final Rectangle bds, final Rectangle vis) {
        Rectangle[] result;
        if (vis.contains(bds)) {
            result = new Rectangle[0];
        } else {
            if (bds.x < vis.x && bds.x + bds.width > vis.x + vis.width) {
                Rectangle a = new Rectangle (bds.x, bds.y, vis.x - bds.x, bds.height);
                Rectangle b = new Rectangle (vis.x + vis.width, bds.y, (bds.x + bds.width) - (vis.x + vis.width), bds.height);
                result = new Rectangle[] {a, b};
            } else if (bds.x < vis.x) {
                result = new Rectangle[] {
                    new Rectangle (bds.x, bds.y, vis.x - bds.x, bds.height)
                };
            } else if (bds.x + bds.width > vis.x + vis.width) {
                result = new Rectangle[] {
                    new Rectangle (vis.x + vis.width, bds.y, (bds.x + bds.width) - (vis.x + vis.width), bds.height)
                };
            } else {
                result = new Rectangle[0];
            }
        }
        return result;
    }

    /**
     * Show popups for each rectangle, using the now configured painter.
     */
    private void showPopups(Rectangle[] rects, Rectangle bds, Rectangle visible, JComponent comp, JScrollPane view) {
        boolean shown = false;
        for (int i=0; i < rects.length; i++) {
            Rectangle sect = rects[i];
            sect.translate (-bds.x, -bds.y);
            ImgComp part = painter.getPartial(sect, bds.x + rects[i].x < visible.x);
            Point pos = new Point (bds.x + rects[i].x, bds.y + rects[i].y);
            SwingUtilities.convertPointToScreen(pos, comp);
            if (comp instanceof JList) {
                //XXX off by one somewhere, only with JLists - where?
                pos.y--;
            }
            if (pos.x > 0) { //Mac OS will reposition off-screen popups to x=0,
                //so don't try to show them
                popups[i] = getPopupFactory().getPopup(view, 
                        part, pos.x, pos.y);
                popups[i].show();
                shown = true;
            }
        }
        if (shown) {
            setHideComponent (comp, view);
        } else {
            setHideComponent (null, null); //clear references
        }
    }
    
    private static PopupFactory popupFactory;
    private static PopupFactory getPopupFactory() {
        if (popupFactory == null) popupFactory = new CustomPopupFactory();
        return popupFactory;
    }
    
    private Hider hider = null;
    /**
     * Set a component (JList or JTree) which should be listened to, such that if
     * a model, selection or scroll event occurs, all currently open popups
     * should be hidden.
     */
    private void setHideComponent (JComponent comp, JScrollPane parent) {
        if (hider != null) {
            if (hider.isListeningTo(comp)) {
                return;
            }
        }
        if (hider != null) {
            hider.detach();
        }
        if (comp != null) {
            hider = new Hider (comp, parent);
        } else {
            hider = null;
        }
    }
    
    /**
     * A JComponent which creates a BufferedImage of a cell renderer and can
     * produce clones of itself that display subrectangles of that cell
     * renderer.
     */
    private static final class ImgComp extends JComponent {
        private Image img;
        private Dimension d = null;
        
        private Color bg = Color.WHITE;
        private JScrollPane comp = null;
        
        private Object node = null;
        
        private AffineTransform at = AffineTransform.getTranslateInstance(0d, 0d);
        boolean isRight = false;
        
        ImgComp() {}
        
        /**
         * Create a clone with a specified backing image
         */
        ImgComp (Image img, Rectangle off, boolean right) {
            this.img = img;
            at = AffineTransform.getTranslateInstance(-off.x, 0);
            d = new Dimension (off.width, off.height);
            isRight = right;
        }
        
        public ImgComp getPartial (Rectangle bds, boolean right) {
            assert img != null;
            return new ImgComp (img, bds, right);
        }        
        
        /** Configures a tree cell renderer and sets up sizing and the 
         * backing image from it */
        public boolean configure (Object nd, JScrollPane tv, JTree tree, TreePath path, int row) {
            boolean sameVn = setLastRendereredObject(nd);
            boolean sameComp = setLastRenderedScrollPane (tv);
            Component renderer = null;
            bg = tree.getBackground();
            boolean sel = tree.isSelectionEmpty() ? false :
                tree.getSelectionModel().isPathSelected(path);
            boolean exp = tree.isExpanded(path);
            boolean leaf = !exp && tree.getModel().isLeaf(nd);
            boolean lead = path.equals(tree.getSelectionModel().getLeadSelectionPath());
            renderer = tree.getCellRenderer().getTreeCellRendererComponent(tree, nd, sel, exp, leaf, row, lead);
            if (renderer != null) {
                setComponent (renderer, tree);
            }
            return true;
        }
        
        /** Configures a list cell renderer and sets up sizing and the 
         * backing image from it */
        public boolean configure (Object nd, JScrollPane tv, JList list, int row) {
            boolean sameVn = setLastRendereredObject(nd);
            boolean sameComp = setLastRenderedScrollPane (tv);
            Component renderer = null;
            bg = list.getBackground();
            boolean sel = list.isSelectionEmpty() ? false :
                list.getSelectionModel().isSelectedIndex(row);
            renderer = list.getCellRenderer().getListCellRendererComponent(list, nd, row, sel, false);
            if (renderer != null) {
                setComponent (renderer, list);
            }
            return true;
        }
        
        private boolean setLastRenderedScrollPane (JScrollPane comp) {
            boolean result = comp != this.comp;
            this.comp = comp;
            return result;
        }
        
        private boolean setLastRendereredObject (Object nd) {
            boolean result = node != nd;
            if (result) {
                node = nd;
            }
            return result;
        }
        
        void clear() {
            comp = null;
            node = null;
        }
        
        /**
         * Set the cell renderer we will proxy.
         */
        public void setComponent (Component jc, JComponent owner) {
            Dimension dd = jc.getPreferredSize();
            Rectangle currentScreenBounds = Utilities.getUsableScreenBounds();
            // get some reasonable limit for the width
            int width = Math.min(dd.width, 2 * currentScreenBounds.width);
            int height = Math.min(dd.height + 2, 2 * currentScreenBounds.height);
            Image nue = owner.createVolatileImage(width, height);
            Graphics g = nue.getGraphics();
            g.setColor (bg);
            g.fillRect (0, 0, width, dd.height + 2);
            if( jc instanceof Container && !jc.isValid() ) {
                //#214739
                jc.setSize( width, dd.height );
                jc.doLayout();
            }
            SwingUtilities.paintComponent(g, jc, this, 0, 0, width, dd.height + 2);
            g.dispose();
            setImage (nue);
        }
        
        @Override
        public Rectangle getBounds() {
            Dimension dd = getPreferredSize();
            return new Rectangle (0, 0, dd.width, dd.height);
        }
        
        private void setImage(Image img) {
            this.img = img;
            d = null;
        }
        
        @Override
        public Dimension getPreferredSize() {
            if (d == null) {
                d = new Dimension (img.getWidth(null), img.getHeight(null));
            }
            return d;
        }
        
        @Override
        public Dimension getSize() {
            return getPreferredSize();
        }
        
        @Override
        public void paint (Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage (img, at, null);
            g.setColor (Color.GRAY);
            g.drawLine (0, 0, d.width, 0);
            g.drawLine (0, d.height-1, d.width, d.height-1);
            if (isRight) {
                g.drawLine (0, 0, 0, d.height-1);
            } else {
                g.drawLine (d.width-1, 0, d.width-1, d.height-1);
            }
        }
        
        @Override
        public void firePropertyChange (String s, Object a, Object b) {}
        @Override
        public void invalidate() {}
        @Override
        public void validate() {}
        @Override
        public void revalidate() {}
    }
    
    /**
     * A listener that listens to just about everything in the known universe
     * and hides all currently displayed popups if anything happens.
     */
    private static final class Hider implements ChangeListener, PropertyChangeListener, TreeModelListener, TreeSelectionListener, HierarchyListener, HierarchyBoundsListener, ListSelectionListener, ListDataListener, ComponentListener {
        private final JTree tree;
        
        private JScrollPane pane;
        private final JList list;
        
        public Hider (JComponent comp, JScrollPane pane) {
            if (comp instanceof JTree) {
                this.tree = (JTree) comp;
                this.list = null;
            } else {
                this.list = (JList) comp;
                this.tree = null;
            }
            assert tree != null || list != null;
            this.pane = pane;
            attach();
        }
        
        private boolean isListeningTo (JComponent comp) {
            return !detached && (comp == list || comp == tree);
        }
        
        private void attach() {
            if (tree != null) {
                tree.getModel().addTreeModelListener(this);
                tree.getSelectionModel().addTreeSelectionListener(this);
                tree.addHierarchyBoundsListener(this);
                tree.addHierarchyListener(this);
                tree.addComponentListener(this);
            } else {
                list.getSelectionModel().addListSelectionListener(this);
                list.getModel().addListDataListener(this);
                list.addHierarchyBoundsListener(this);
                list.addHierarchyListener(this);
                list.addComponentListener(this);
            }
            pane.getHorizontalScrollBar().getModel().addChangeListener(this);
            pane.getVerticalScrollBar().getModel().addChangeListener(this);
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(this);
        }
        
        private boolean detached = false;
        private void detach() {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener(this);
            if (tree != null) {
                tree.getSelectionModel().removeTreeSelectionListener(this);
                tree.getModel().removeTreeModelListener(this);
                tree.removeHierarchyBoundsListener(this);
                tree.removeHierarchyListener(this);
                tree.removeComponentListener(this);
            } else {
                list.getSelectionModel().removeListSelectionListener(this);
                list.getModel().removeListDataListener(this);
                list.removeHierarchyBoundsListener(this);
                list.removeHierarchyListener(this);
                list.removeComponentListener(this);
            }
            pane.getHorizontalScrollBar().getModel().removeChangeListener(this);
            pane.getVerticalScrollBar().getModel().removeChangeListener(this);
            detached = true;
        }
        
        private void change() {
            ViewTooltips vtt = (ViewTooltips) (tree != null ? 
                tree.getClientProperty(KEY) : list.getClientProperty(KEY));
            if (vtt != null) {
                vtt.hide();
            }
            detach();
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            change();
        }
        public void treeNodesChanged(TreeModelEvent e) {
            change();
        }
        
        public void treeNodesInserted(TreeModelEvent e) {
            change();
        }
        
        public void treeNodesRemoved(TreeModelEvent e) {
            change();
        }
        
        public void treeStructureChanged(TreeModelEvent e) {
            change();
        }
        
        public void hierarchyChanged(HierarchyEvent e) {
            change();
        }
        
        public void valueChanged(TreeSelectionEvent e) {
            change();
        }
        
        public void ancestorMoved(HierarchyEvent e) {
            change();
        }
        
        public void ancestorResized(HierarchyEvent e) {
            change();
        }

        public void stateChanged(ChangeEvent e) {
            change();
        }
        
        public void valueChanged(ListSelectionEvent e) {
            change();
        }
        
        public void intervalAdded(ListDataEvent e) {
            change();
        }
        
        public void intervalRemoved(ListDataEvent e) {
            change();
        }
        
        public void contentsChanged(ListDataEvent e) {
            change();
        }
        
        public void componentResized(ComponentEvent e) {
            change();
        }
        
        public void componentMoved(ComponentEvent e) {
            change();
        }
        
        public void componentShown(ComponentEvent e) {
            change();
        }
        
        public void componentHidden(ComponentEvent e) {
            change();
        }
    }
}
