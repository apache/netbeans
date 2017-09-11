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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.netbeans.swing.tabcontrol.WinsysInfoForTabbedContainer;
import org.netbeans.swing.tabcontrol.event.ComplexListDataEvent;
import org.openide.windows.TopComponent;

/**
 * Base class for tab displayer UIs which use cell renderers to display tabs.
 * This class does not contain much logic itself, but rather acts to connect events
 * and data from various objects relating to the tab displayer, which it creates and
 * installs.  Basically, the things that are involved are:
 * <ul>
 * <li>A layout model ({@link TabLayoutModel}) - A data model providing the positions and sizes of tabs</li>
 * <li>A state model ({@link TabState}) - A data model which tracks state data (selected, pressed, etc.)
 *     for each tab, and can be queried when a tab is painted to determine how that should be done.</li>
 * <li>A selection model ({@link javax.swing.SingleSelectionModel}) - Which tracks which tab is selected</li>
 * <li>The {@link TabDisplayer} component itself</li>
 * <li>The {@link TabDisplayer}'s data model, which contains the list of tab names, their icons and
 *     tooltips and the user object (or {@link java.awt.Component}) they identify</li>
 * <li>Assorted listeners on the component and data models, specifically
 *       <ul><li>A mouse listener that tells the state model when a state-affecting event
 *               has happened, such as the mouse entering a tab</li>
 *           <li>A change listener that repaints appropriately when the selection changes</li>
 *           <li>A property change listener to trigger any repaints needed due to property
 *               changes on the displayer component</li>
 *           <li>A component listener to attach and detach listeners when the component is shown/
 *               hidden, and if neccessary, notify the layout model when the component is resized</li>
 *           <li>A default {@link TabCellRenderer}, which is what will actually paint the tabs, and which
 *               is also responsible for providing some miscellaneous data such as the number of
 *               pixels the layout model should add to tab widths to make room for decorations,
 *               etc.</li>
 *       </ul>
 * </ul>
 * The usage pattern of this class is similar to other {@link javax.swing.plaf.ComponentUI} subclasses -
 * {@link javax.swing.plaf.ComponentUI#installUI}
 * is called via {@link JComponent#updateUI}.  <code>installUI</code> initializes protected fields which
 * subclasses will need, in a well defined way; abstract methods are provided for subclasses to
 * create these objects (such as the things listed above), and convenience implementations of some
 * are provided. <strong>Under no circumstances</strong> should subclasses modify these protected fields -
 * due to the circuitousness of the way Swing installs UIs, they cannot be declared final, but should
 * be treated as read-only.
 * <p>
 * The goal of this class is to make it quite easy to implement new appearances
 * for tabs:  To create a new appearance, implement a {@link TabCellRenderer} that can 
 * paint individual tabs as desired.  This is made even easier via the 
 * {@link TabPainter} interface - simply create the painting logic needed there.  Then
 * subclass <code>BasicTabDisplayerUI</code> and include any painting logic for the background,
 * scroll buttons, etc. needed.  A good example is {@link AquaEditorTabDisplayerUI}.
 *
 */
public abstract class BasicTabDisplayerUI extends AbstractTabDisplayerUI {
    protected TabState tabState = null;
    private static final boolean swingpainting = Boolean.getBoolean(
            "nb.tabs.swingpainting"); //NOI18N

    protected TabCellRenderer defaultRenderer = null;
    protected int repaintPolicy = 0;

    //A couple rectangles for calculation purposes
    private Rectangle scratch = new Rectangle();
    private Rectangle scratch2 = new Rectangle();
    private Rectangle scratch3 = new Rectangle();

    private Point lastKnownMouseLocation = new Point();

    int pixelsToAdd = 0;   
    
    public BasicTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
    }

    /**
     * Overridden to initialize the <code>tabState</code> and <code>defaultRenderer</code>.
     */
    @Override
    protected void install() {
        super.install();
        tabState = createTabState();
        defaultRenderer = createDefaultRenderer();
        if( null != displayer.getContainerWinsysInfo() ) {
            defaultRenderer.setShowCloseButton( displayer.getContainerWinsysInfo().isTopComponentClosingEnabled() );
        }
        layoutModel.setPadding (defaultRenderer.getPadding());
        pixelsToAdd = defaultRenderer.getPixelsToAddToSelection();
        repaintPolicy = createRepaintPolicy();
        if (displayer.getSelectionModel().getSelectedIndex() != -1) {
            tabState.setSelected(displayer.getSelectionModel().getSelectedIndex());
            tabState.setActive(displayer.isActive());
        }
    }

    @Override
    protected void uninstall() {
        tabState = null;
        defaultRenderer = null;
        super.uninstall();
    }
    
    /** Used by unit tests */
    TabState getTabState() {
        return tabState;
    }

    /**
     * Create a TabState instance.  TabState manages the state of tabs - that is, which one
     * contains the mouse, which one is pressed, and so forth, providing methods such as
     * <code>setMouseInTab(int tab)</code>.  Its getState() method returns a bitmask of
     * states a tab may have which affect the way it is painted.
     * <p>
     * <b>Usage:</b> It is expected that UIs will subclass TabState, to implement the
     * repaint methods, and possibly override <code>getState(int tab)</code> to mix
     * additional state bits into the bitmask.  For example, scrollable tabs have the
     * possible states CLIP_LEFT and CLIP_RIGHT; BasicScrollingTabDisplayerUI's
     * implementation of this determines these states by consulting its layout model, and
     * adds them in when appropriate.
     *
     * @return An implementation of TabState
     * @see BasicTabDisplayerUI.BasicTabState
     * @see BasicScrollingTabDisplayerUI.ScrollingTabState
     */
    protected TabState createTabState() {
        return new BasicTabState();
    }

    /**
     * Create the default cell renderer for this control.  If it is desirable to
     * have more than one renderer, override getTabCellRenderer()
     */
    protected abstract TabCellRenderer createDefaultRenderer();

    /**
     * Return a set of insets defining the margins into which tabs should not be
     * painted.  Subclasses that want to paint some controls to the right of the
     * tabs should include space for those controls in these insets.  If a
     * bottom margin under the tabs is to be painted, include that as well.
     */
    public abstract Insets getTabAreaInsets();

    /**
     * Get the cell renderer for a given tab.  The default implementation simply
     * returns the renderer created by createDefaultRenderer().
     * @param tab
     * @return 
     */
    public TabCellRenderer getTabCellRenderer(int tab) {
        defaultRenderer.setShowCloseButton(displayer.isShowCloseButton());
        if( tab >=0 && tab < displayer.getModel().size() ) {
            TabData data = displayer.getModel().getTab(tab);
            boolean closingEnabled = true;
            if( data.getComponent() instanceof TopComponent ) {
                closingEnabled = displayer.getContainerWinsysInfo().isTopComponentClosingEnabled( (TopComponent)data.getComponent() );
            }

            defaultRenderer.setShowCloseButton(displayer.isShowCloseButton() && closingEnabled);
        }
        return defaultRenderer;
    }

    /**
     * Set the passed rectangle's bounds to the recangle in which tabs will be
     * painted; if your look and feel reserves some part of the tab area for its
     * own painting.  The rectangle is determined by what is returned by
     * getTabAreaInsets() - this is simply a convenience method for finding the
     * rectange into which tabs will be painted.
     */
    protected final void getTabsVisibleArea(Rectangle rect) {
        Insets ins = getTabAreaInsets();
        rect.x = ins.left;
        rect.y = ins.top;
        rect.width = displayer.getWidth() - ins.right - ins.left;
        rect.height = displayer.getHeight() - ins.bottom - ins.top;
    }

    @Override
    protected MouseListener createMouseListener() {
        return new BasicDisplayerMouseListener();
    }

    @Override
    protected PropertyChangeListener createPropertyChangeListener() {
        return new BasicDisplayerPropertyChangeListener();
    }

    @Override
    public Polygon getExactTabIndication(int index) {
        Rectangle r = getTabRect(index, scratch);
        return getTabCellRenderer(index).getTabShape(tabState.getState(index), r);
    }

    @Override
    public Polygon getInsertTabIndication(int index) {
        Polygon p;
        if (index == getLastVisibleTab() + 1) {
            p = (Polygon) getExactTabIndication (index-1);
            Rectangle r = getTabRect(index-1, scratch);
            p.translate(r.width/2, 0);
        } else {
            p = (Polygon) getExactTabIndication (index);
            Rectangle r = getTabRect(index, scratch);
            p.translate(-(r.width/2), 0);
        }
        return p;
    }

    @Override
    public int tabForCoordinate(Point p) {
        if (displayer.getModel().size() == 0) {
            return -1;
        }
        getTabsVisibleArea(scratch);
        if (!scratch.contains(p)) {
            return -1;
        }
        int tabIndex = layoutModel.indexOfPoint(p.x, p.y);
        if( tabIndex >= displayer.getModel().size() )
            tabIndex = -1;
        return tabIndex;
    }

    @Override
    public Rectangle getTabRect(int idx, Rectangle rect) {
        if (rect == null) {
            rect = new Rectangle();
        }
        if (idx < 0 || idx >= displayer.getModel().size()) {
            rect.x = rect.y = rect.width = rect.height = 0;
            return rect;
        }
        rect.x = layoutModel.getX(idx);
        rect.y = layoutModel.getY(idx);
        rect.width = layoutModel.getW(idx);
        getTabsVisibleArea(scratch3);
        //XXX for R->L component orientation cannot assume x = 0
        int maxPos = scratch.x + scratch3.width;
        if (rect.x > maxPos) {
            rect.width = 0;
        } else if (rect.x + rect.width > maxPos) {
            rect.width = (maxPos - rect.x);
        }
        rect.height = layoutModel.getH(idx);
        getTabsVisibleArea(scratch2);
        if (rect.y + rect.height > scratch2.y + scratch2.height) {
            rect.height = (scratch2.y + scratch2.height) - rect.y;
        }
        if (rect.x + rect.width > scratch2.x + scratch2.width) {
            rect.width = (scratch2.x + scratch2.width) - rect.x;
        }
        return rect;
    }

    @Override
    public Image createImageOfTab(int index) {
        TabData td = displayer.getModel().getTab(index);
        
        JLabel lbl = new JLabel(td.getText());
        int width = lbl.getFontMetrics(lbl.getFont()).stringWidth(td.getText());
        int height = lbl.getFontMetrics(lbl.getFont()).getHeight();
        width = width + td.getIcon().getIconWidth() + 6;
        height = Math.max(height, td.getIcon().getIconHeight()) + 5;
        
        GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                        .getDefaultScreenDevice().getDefaultConfiguration();
        
        BufferedImage image = config.createCompatibleImage(width, height);
        Graphics2D g = image.createGraphics();
        g.setColor(lbl.getForeground());
        g.setFont(lbl.getFont());
        td.getIcon().paintIcon(lbl, g, 0, 0);
        g.drawString(td.getText(), 18, height / 2);
        
        
        return image;
    }

    @Override
    public int dropIndexOfPoint(Point p) {
        Point p2 = toDropPoint(p);
        int start = getFirstVisibleTab();
        int end = getLastVisibleTab();
        int target;
        for (target = start; target <= end; target ++) {
            getTabRect (target, scratch);
            if (scratch.contains(p2)) {
                if (target == end) {
                    Object orientation = displayer.getClientProperty (TabDisplayer.PROP_ORIENTATION);
                    boolean flip = displayer.getType() == TabDisplayer.TYPE_SLIDING && (
                            orientation == TabDisplayer.ORIENTATION_EAST ||
                            orientation == TabDisplayer.ORIENTATION_WEST);

                    if (flip) {
                        if (p2.y > scratch.y + (scratch.height / 2)) {
                            return target+1;
                        }
                    } else {
                        if (p2.x > scratch.x + (scratch.width / 2)) {
                            return target+1;
                        }
                    }
                }
                return target;
            }
        }
        return -1;
    }

    protected boolean isAntialiased() {
        return ColorUtil.shouldAntialias();
    }

    /**
     * Paints the tab control.  Calls paintBackground(), then paints the tabs using
     * their cell renderers,
     * then calls paintAfterTabs
     */
    @Override
    public final void paint(Graphics g, JComponent c) {
        assert c == displayer;
        
        ColorUtil.setupAntialiasing(g);
        
        paintBackground(g);
        int start = getFirstVisibleTab();
        if (start == -1 || !displayer.isShowing()) {
            return;
        }
        //Possible to have a repaint called by a mouse-clicked event if close on mouse press
        int stop = Math.min(getLastVisibleTab(), displayer.getModel().size() - 1);
        getTabsVisibleArea(scratch);
        
//System.err.println("paint, clip bounds: " + g.getClipBounds() + " first visible: " + start + " last: " + stop);

        if (g.hitClip(scratch.x, scratch.y, scratch.width, scratch.height)) {
            Shape s = g.getClip();
            try {
                //Ensure that we will never paint an icon into the controls area
                //by setting the clipping bounds
                if (s != null) {
                    //Okay, some clip area is already set.  Get the intersection.
                    Area a = new Area(s);
                    a.intersect(new Area(scratch.getBounds2D()));
                    g.setClip(a);
                } else {
                    //Clip was not set (it's a normal call to repaint() or something
                    //like that).  Just set the bounds.
                    g.setClip(scratch.x, scratch.y, scratch.width,
                              scratch.height);
                }


                for (int i = start; i <= stop; i++) {
                    getTabRect(i, scratch);
                    if (g.hitClip(scratch.x, scratch.y, scratch.width + 1,
                                  scratch.height + 1)) {
                                      
                        int state = tabState.getState(i);
                        
                        if ((state & TabState.NOT_ONSCREEN) == 0) {
                            TabCellRenderer ren = getTabCellRenderer(i);
                            
                            TabData data = displayer.getModel().getTab(i);

                            if( isTabBusy( i ) ) {
                                state |= TabState.BUSY;
                            }
                            
                            JComponent renderer = ren.getRendererComponent(
                                    data, scratch, state);
                            
                            renderer.setFont(displayer.getFont());
                            //prepareRenderer ( renderer, data, ren.getLastKnownState () );
                            if (swingpainting) {
                                //Conceivable that some L&F may need this, but it generates
                                //lots of useless events - better to do direct painting where
                                //possible
                                SwingUtilities.paintComponent(g, renderer,
                                                              displayer,
                                                              scratch);
                            } else {
                                try {
                                    g.translate(scratch.x, scratch.y);
                                    renderer.setBounds(scratch);
                                    renderer.paint(g);
                                } finally {
                                    g.translate(-scratch.x, -scratch.y);
                                }
                            }
                        }
                    }
                }
            } finally {
                g.setClip(s);
            }
        }
        paintAfterTabs(g);
    }

    /**
     * Fill in the background of the component prior to painting the tabs.  The default
     * implementation does nothing.  If it's just a matter of filling in a background color,
     * setOpaque (true) on the displayer, and ComponentUI.update() will take care of the rest.
     */
    protected void paintBackground(Graphics g) {

    }

    /**
     * Override this method to provide painting of areas outside the tabs
     * rectangle, such as margins and controls
     */
    protected void paintAfterTabs(Graphics g) {
        //do nothing
    }

    /**
     * Scrollable implementations will override this method to provide the first
     * visible (even if clipped) tab.  The default implementation returns 0 if
     * there is at least one tab in the data model, or -1 to indicate the model
     * is completely empty
     */
    protected int getFirstVisibleTab() {
        return displayer.getModel().size() > 0 ? 0 : -1;
    }

    /**
     * Scrollable implementations will override this method to provide the last
     * visible (even if clipped) tab.  The default implementation returns 0 if
     * there is at least one tab in the data model, or -1 to indicate the model
     * is completely empty
     */
    protected int getLastVisibleTab() {
        return displayer.getModel().size() - 1;
    }

    @Override
    protected ChangeListener createSelectionListener() {
        return new BasicSelectionListener();
    }

    protected final Point getLastKnownMouseLocation() {
        return lastKnownMouseLocation;
    }

    /**
     * Convenience method to override for handling mouse wheel events. The
     * defualt implementation does nothing.
     */
    protected void processMouseWheelEvent(MouseWheelEvent e) {
        //do nothing
    }
    
    @Override
    protected final void requestAttention (int tab) {
        tabState.addAlarmTab(tab);
    }

    @Override
    protected final void cancelRequestAttention (int tab) {
        tabState.removeAlarmTab(tab);
    }

    @Override
    protected final void setAttentionHighlight (int tab, boolean highlight) {
        if( highlight ) {
            tabState.addHighlightTab(tab);
        } else {
            tabState.removeHighlightTab(tab);
        }
    }

    @Override
    protected void modelChanged() {
        tabState.clearTransientStates();
        //DefaultTabSelectionModel automatically updates its selected index when things
        //are added/removed from the model, so just make sure our state machine stays in
        //sync
        int idx = selectionModel.getSelectedIndex();
        tabState.setSelected(idx);
        tabState.pruneTabs(displayer.getModel().size());
        super.modelChanged();
    }

    /**
     * Create the policy that will determine what types of events trigger a repaint of one or more tabs.
     * This is a bitmask composed of constants defined in TabState. The default value is
     * <pre>
     *  TabState.REPAINT_SELECTION_ON_ACTIVATION_CHANGE
                | TabState.REPAINT_ON_SELECTION_CHANGE
                | TabState.REPAINT_ON_MOUSE_ENTER_TAB
                | TabState.REPAINT_ON_MOUSE_ENTER_CLOSE_BUTTON
                | TabState.REPAINT_ON_MOUSE_PRESSED;
     *</pre>
     *
     *
     * @return  The repaint policy that should be used in conjunction with mouse events to determine when a
     *          repaint is needed.
     */
    protected int createRepaintPolicy () {
        return TabState.REPAINT_SELECTION_ON_ACTIVATION_CHANGE
                | TabState.REPAINT_ON_SELECTION_CHANGE
                | TabState.REPAINT_ON_MOUSE_ENTER_TAB
                | TabState.REPAINT_ON_MOUSE_ENTER_CLOSE_BUTTON
                | TabState.REPAINT_ON_MOUSE_PRESSED;
    }

    /**
     * @return Rectangle of the tab to be repainted
     */ 
    protected Rectangle getTabRectForRepaint( int tab, Rectangle rect ) {
        return getTabRect( tab, rect );
    }

    protected class BasicTabState extends TabState {

        @Override
        public int getState(int tab) {
            if (displayer.getModel().size() == 0) {
                return TabState.NOT_ONSCREEN;
            }
            int result = super.getState(tab);
            if (tab == 0) {
                result |= TabState.LEFTMOST;
            }
            if (tab == displayer.getModel().size() - 1) {
                result |= TabState.RIGHTMOST;
            }
            return result;
        }

        @Override
        protected void repaintAllTabs() {
            //XXX would be nicer to just repaint the tabs area,
            //but we also need to repaint below all the tabs in the
            //event of activated/deactivated.  No actual reason to
            //repaint the buttons here.
            displayer.repaint();
        }

        @Override
        public int getRepaintPolicy(int tab) {
            //Defined in createRepaintPolicy()
            return repaintPolicy;
        }

        @Override
        protected void repaintTab(int tab) {
            if (tab == -1 || tab > displayer.getModel().size()) {
                return;
            }
            getTabRectForRepaint(tab, scratch);
            scratch.y = 0;
            scratch.height = displayer.getHeight();
            displayer.repaint(scratch.x, scratch.y, scratch.width,
                              scratch.height);
        }

        @Override
        boolean isDisplayable() {
            return displayer.isDisplayable();
        }
    }
    
    @Override
    protected ModelListener createModelListener() {
        return new BasicModelListener();
    }    

    private class BasicDisplayerPropertyChangeListener
            extends DisplayerPropertyChangeListener {

        @Override
        protected void activationChanged() {
            tabState.setActive(displayer.isActive());
        }
    }

    protected class BasicDisplayerMouseListener implements MouseListener,
            MouseMotionListener, MouseWheelListener {
        private int updateMouseLocation(MouseEvent e) {
            lastKnownMouseLocation.x = e.getX();
            lastKnownMouseLocation.y = e.getY();
            return tabForCoordinate(lastKnownMouseLocation);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            int idx = updateMouseLocation(e);
            if (idx == -1) {
                return;
            }

            TabCellRenderer tcr = getTabCellRenderer(idx);
            getTabRect(idx, scratch);
            int state = tabState.getState(idx);

            potentialCommand (idx, e, state, tcr, scratch);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            mouseMoved (e);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            int idx = updateMouseLocation(e);
            tabState.setMouseInTabsArea(true);
            tabState.setContainsMouse(idx);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            updateMouseLocation(e);
            tabState.setMouseInTabsArea(false);
            tabState.setContainsMouse(-1);
            tabState.setCloseButtonContainsMouse(-1);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            int idx = updateMouseLocation(e);
            tabState.setMouseInTabsArea(true);
            tabState.setContainsMouse(idx);
            if (idx != -1) {
                TabCellRenderer tcr = getTabCellRenderer(idx);
                getTabRect(idx, scratch);
                int state = tabState.getState(idx);

                String s = tcr.getCommandAtPoint(e.getPoint(), state, scratch);
                if (TabDisplayer.COMMAND_CLOSE == s) {
                    tabState.setCloseButtonContainsMouse(idx);
                } else {
                    tabState.setCloseButtonContainsMouse(-1);
                }
            } else {
                tabState.setContainsMouse(-1);
            }
        }

        private int lastPressedTab = -1;
        private long pressTime = -1;
        @Override
        public void mousePressed(MouseEvent e) {
            int idx = updateMouseLocation(e);
            tabState.setPressed(idx);

            //One a double click, preserve the tab that was initially clicked, in case
            //a re-layout happened.  We'll pass that to the action.
            long time = e.getWhen();
            if (time - pressTime > 200) {
                lastPressedTab = idx;
            }
            pressTime = time;
            lastPressedTab = idx;
            if (idx != -1) {
                TabCellRenderer tcr = getTabCellRenderer(idx);
                getTabRect(idx, scratch);
                int state = tabState.getState(idx);

                //First find the command for the location with the default button -
                //TabState may trigger a repaint
                String command = tcr.getCommandAtPoint (e.getPoint(), state, scratch);
                if (TabDisplayer.COMMAND_CLOSE == command) {
                    tabState.setCloseButtonContainsMouse(idx);
                    tabState.setMousePressedInCloseButton(idx);

                    //We're closing, don't try to maximize this tab if it turns out to be
                    //a double click
                    pressTime = -1;
                    lastPressedTab = -1;
                }

                potentialCommand (idx, e, state, tcr, scratch);
            } else {
                tabState.setMousePressedInCloseButton(-1); //just in case
                if( e.isPopupTrigger() ) {
                    displayer.repaint();
                    performCommand (TabDisplayer.COMMAND_POPUP_REQUEST, -1, e);
                }
            }
        }

        private void potentialCommand (int idx, MouseEvent e, int state, TabCellRenderer tcr, Rectangle bounds) {
            String command = tcr.getCommandAtPoint (e.getPoint(), state, bounds,
                    e.getButton(), e.getID(), e.getModifiersEx());
            if (command == null || TabDisplayer.COMMAND_SELECT == command) {
                if (e.isPopupTrigger()) {
                    displayer.repaint();
                    performCommand (TabDisplayer.COMMAND_POPUP_REQUEST, idx, e);
                    return;
                } else if (e.getID() == MouseEvent.MOUSE_CLICKED && e.getClickCount() >= 2 && e.getButton() == MouseEvent.BUTTON1 ) {
                    performCommand (TabDisplayer.COMMAND_MAXIMIZE, idx, e);
                    return;
                }
            }

            if (command != null) {
                performCommand (command, lastPressedTab == -1 || lastPressedTab >=
                    displayer.getModel().size() ? idx : lastPressedTab, e);
            }
        }

        private void performCommand (String command, int idx, MouseEvent evt) {
            evt.consume();
            if (TabDisplayer.COMMAND_SELECT == command) {
                if (idx != displayer.getSelectionModel().getSelectedIndex()) {
                    boolean go = shouldPerformAction (command, idx, evt);
                    if (go) {
                        selectionModel.setSelectedIndex (idx);
                    }
                }
            } else {
                boolean should = shouldPerformAction (command, idx, evt) && displayer.isShowCloseButton();
                if (should) {
                    if (TabDisplayer.COMMAND_CLOSE == command) {
                        displayer.getModel().removeTab(idx);
                    } else if (TabDisplayer.COMMAND_CLOSE_ALL == command) {
                        displayer.getModel().removeTabs (0, displayer.getModel().size());
                    } else if (TabDisplayer.COMMAND_CLOSE_ALL_BUT_THIS == command) {
                        int start;
                        int end;
                        if (idx != displayer.getModel().size()-1) {
                            start = idx+1;
                            end = displayer.getModel().size();
                            displayer.getModel().removeTabs(start, end);
                        }
                        if (idx != 0) {
                            start = 0;
                            end = idx;
                            displayer.getModel().removeTabs(start, end);
                        }
                    }
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            int idx = updateMouseLocation(e);
            if (idx != -1) {
                TabCellRenderer tcr = getTabCellRenderer(idx);
                getTabRect(idx, scratch);
                int state = tabState.getState(idx);
                if ((state & TabState.PRESSED) != 0 && ((state & TabState.CLIP_LEFT) != 0) || (state & TabState.CLIP_RIGHT) != 0) {
                    makeTabVisible(idx);
                }
                potentialCommand (idx, e, state, tcr, scratch);
            } else {
                if( e.isPopupTrigger() ) {
                    displayer.repaint();
                    performCommand (TabDisplayer.COMMAND_POPUP_REQUEST, -1, e);
                }
            }
            tabState.setMouseInTabsArea(idx != -1);
            tabState.setPressed(-1);
            tabState.setMousePressedInCloseButton(-1);
        }

        @Override
        public final void mouseWheelMoved(MouseWheelEvent e) {
            updateMouseLocation(e);
            processMouseWheelEvent(e);
        }
    }

    /** A simple selection listener implementation which updates the TabState model
     * with the new selected index from the selection model when it changes.
     */
    protected class BasicSelectionListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            assert e.getSource() == selectionModel : "Unknown event source: "
                    + e.getSource();
            int idx = selectionModel.getSelectedIndex();
            tabState.setSelected(idx >= 0 ? idx : -1);
            if (idx >= 0) {
                makeTabVisible (selectionModel.getSelectedIndex());
            }
        }
    }
    
    /**
     * Listener on data model which will pass modified indices to the
     * TabState object, so it can update which tab indices are flashing in
     * "attention" mode, if any.
     */
    protected class BasicModelListener extends AbstractTabDisplayerUI.ModelListener {
        @Override
        public void contentsChanged(ListDataEvent e) {
            super.contentsChanged(e);
            tabState.contentsChanged(e);
        }

        @Override
        public void indicesAdded(ComplexListDataEvent e) {
            super.indicesAdded(e);
            tabState.indicesAdded(e);
        }

        @Override
        public void indicesChanged(ComplexListDataEvent e) {
            tabState.indicesChanged(e);
        }

        @Override
        public void indicesRemoved(ComplexListDataEvent e) {
            tabState.indicesRemoved(e);
        }

        @Override
        public void intervalAdded(ListDataEvent e) {
            tabState.intervalAdded(e);
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            tabState.intervalRemoved(e);
        }
    }    
}
