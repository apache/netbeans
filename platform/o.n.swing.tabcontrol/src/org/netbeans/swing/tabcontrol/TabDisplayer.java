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

package org.netbeans.swing.tabcontrol;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleRole;
import javax.swing.event.ChangeEvent;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;
import org.netbeans.swing.tabcontrol.plaf.AquaEditorTabDisplayerUI;
import org.netbeans.swing.tabcontrol.plaf.AquaViewTabDisplayerUI;
import org.netbeans.swing.tabcontrol.plaf.BasicSlidingTabDisplayerUI;
import org.netbeans.swing.tabcontrol.plaf.WinClassicEditorTabDisplayerUI;
import org.netbeans.swing.tabcontrol.plaf.WinClassicViewTabDisplayerUI;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.dnd.Autoscroll;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleSelection;
import javax.swing.event.ChangeListener;
import org.netbeans.swing.tabcontrol.plaf.AbstractTabCellRenderer;
import org.netbeans.swing.tabcontrol.plaf.BasicScrollingTabDisplayerUI;
import org.netbeans.swing.tabcontrol.plaf.BasicTabDisplayerUI;
import org.netbeans.swing.tabcontrol.plaf.TabCellRenderer;
import org.netbeans.swing.tabcontrol.plaf.TabState;
import org.netbeans.swing.tabcontrol.plaf.ToolbarTabDisplayerUI;
import org.netbeans.swing.tabcontrol.plaf.WinXPEditorTabDisplayerUI;
import org.netbeans.swing.tabcontrol.plaf.WinXPViewTabDisplayerUI;


/**
 * A Component which displays tabs supplied by a TabDataModel.  This is 
 * essentially the upper (or lower) portion of a tabbed pane, without the
 * part that displays components.  It can be used to provide tab-like
 * selection over a data model containing anything, not just components.
 * <p>
 * It has a three display modes (more fully described in the overview for
 * <a href="TabbedContainer.html">TabbedContainer</a>), to provide different
 * styles of tab display, such as scrolling tabs and others.
 * <p>
 * TabDisplayer is completely model driven - the class itself is little more
 * than an aggregation point for a data model, a selection model, and so forth.
 * The logic that allows it to operate is implemented in the UI delegates,
 * which are installed by (and can be replaced via) the standard Swing 
 * UIManager mechanisms.
 * <p>
 * Some TabDisplayer UI's support an <i>orientation</i> property, which is provided
 * via the client property <code>PROP_ORIENTATION</code>.
 *
 * @author Tim Boudreau
 */
public final class TabDisplayer extends JComponent implements Accessible, Autoscroll {
    
    private boolean initialized = false;
    private TabDataModel model;
    private SingleSelectionModel sel = null;
    private boolean active;
    private final int type;

    /**
     * Displayer type for view tabs, which do not scroll and simply divide the
     * available space between themselves.  The value of this field is mapped to
     * TabbedContainer.TYPE_VIEW
     */
    public static final int TYPE_VIEW = TabbedContainer.TYPE_VIEW;
    /**
     * Displayer type for editor tabs, which scroll (typically - depends on what
     * the UI does).  The value of this field is mapped to
     * TabbedContainer.TYPE_EDITOR
     */
    public static final int TYPE_EDITOR = TabbedContainer.TYPE_EDITOR;
    
    public static final int TYPE_SLIDING = TabbedContainer.TYPE_SLIDING;
    
    public static final int TYPE_TOOLBAR = TabbedContainer.TYPE_TOOLBAR;
    
    /**
     * Property indicating the tab displayer should be painted as
     * &quot;active&quot;. This is typically used to indicate keyboard focus.
     * The valud of this field is mapped to TabbedContainer.PROP_ACTIVE
     */
    public static final String PROP_ACTIVE = TabbedContainer.PROP_ACTIVE;


    /**
     * Action command indicating that the action event signifies the user
     * clicking the Close button on a tab.
     */
    public static final String COMMAND_CLOSE = TabbedContainer.COMMAND_CLOSE;

    /**
     * Action command indicating that the action event fired signifies the user
     * selecting a tab
     */
    public static final String COMMAND_SELECT = TabbedContainer.COMMAND_SELECT;

    /**
     * Action command indicating that the action event fired signifies the user
     * requesting a popup menu over a tab
     */
    public static final String COMMAND_POPUP_REQUEST = TabbedContainer.COMMAND_POPUP_REQUEST;

    /**
     * Action command indicating that the action event fired signifies the user
     * has double clicked a tab
     */
    public static final String COMMAND_MAXIMIZE = TabbedContainer.COMMAND_MAXIMIZE;

    /**
     * Action command indicating that the action event fired signifies the user
     * has shift-clicked the close button on a tab
     */
    public static final String COMMAND_CLOSE_ALL = TabbedContainer.COMMAND_CLOSE_ALL; //NOI18N

    /**
     * Action command indicating that the action event fired signifies the user
     * has alt-clicked the close button on a tab
     */
    public static final String COMMAND_CLOSE_ALL_BUT_THIS = TabbedContainer.COMMAND_CLOSE_ALL_BUT_THIS; //NOI18N

    /**
     * Action command indicating that the action event signifies the user
     * clicking the Pin button on a tab.
     */
    public static final String COMMAND_ENABLE_AUTO_HIDE = TabbedContainer.COMMAND_ENABLE_AUTO_HIDE; //NOI18N
    
    /**
     * Action command to slide out the whole window group.
     * @since 1.27
     */
    public static final String COMMAND_MINIMIZE_GROUP = TabbedContainer.COMMAND_MINIMIZE_GROUP;
    
    /**
     * Action command to restore the whole slided-out window group.
     * @since 1.27
     */
    public static final String COMMAND_RESTORE_GROUP = TabbedContainer.COMMAND_RESTORE_GROUP;
    
    /**
     * Action command to close the whole window group.
     * @since 1.27
     */
    public static final String COMMAND_CLOSE_GROUP = TabbedContainer.COMMAND_CLOSE_GROUP;

    /**
     * Action command indicating that the action event signifies the user
     * clicking the Pin button on a tab.
     */
    public static final String COMMAND_DISABLE_AUTO_HIDE = TabbedContainer.COMMAND_DISABLE_AUTO_HIDE; //NOI18N

    /**
     * UIManager key for the UI Delegate to be used for &quot;editor&quot; style TabbedContainers
     */
    public static final String EDITOR_TAB_DISPLAYER_UI_CLASS_ID = "EditorTabDisplayerUI"; //NOI18N

    /**
     * UIManager key for the UI Delegate to be used for &quot;view&quot; style TabbedContainers
     */
    public static final String VIEW_TAB_DISPLAYER_UI_CLASS_ID = "ViewTabDisplayerUI"; //NOI18N
    
    /**
     * UIManager key for the UI delegate to be used in &quot;sliding&quot; style
     * containers */
    public static final String SLIDING_TAB_DISPLAYER_UI_CLASS_ID = "SlidingTabDisplayerUI"; //NOI18N

    /**
     * UIManager key for the UI delegate to be used for toolbar style tabs
     */
    public static final String TOOLBAR_TAB_DISPLAYER_UI_CLASS_ID = "ToolbarTabDisplayerUI"; //NOI18N
    
    /** Client property to indicate the orientation, which determines what
     * side the tabs are displayed on.  Currently this is only honored by
     * the sliding tabs ui delegate. */
    public static final String PROP_ORIENTATION = "orientation"; //NOI18N

    /** Client property value to display tabs on the left side of the control.
     */
    public static final Object ORIENTATION_EAST = "east"; //NOI18N
    /** Client property value to display tabs on the right side of the control 
     */
    public static final Object ORIENTATION_WEST = "west"; //NOI18N
    /** Client property value to display tabs on the top edge of the control 
     */
    public static final Object ORIENTATION_NORTH = "north"; //NOI18N
    /** Client property value to display tabs on the bottom edge of the control 
     */
    public static final Object ORIENTATION_SOUTH = "south"; //NOI18N
    /** Client property value for pin button to have neutral orientation 
     */
    public static final Object ORIENTATION_CENTER = "center"; //NOI18N
    
    /** Client property value for pin button to be invisible 
     */
    public static final Object ORIENTATION_INVISIBLE = "invisible"; //NOI18N
    
    
    /**
     * Utility field holding list of ActionListeners.
     */
    private transient List<ActionListener> actionListenerList;
    
    private WinsysInfoForTabbed winsysInfo = null;
    private WinsysInfoForTabbedContainer containerWinsysInfo = null;
    
    @Deprecated
    private LocationInformer locationInformer = null;

    private boolean showClose = !Boolean.getBoolean(
        "nb.tabs.suppressCloseButton"); //NOI18N
    
    public TabDisplayer () {
        this (new DefaultTabDataModel(), TYPE_VIEW);
    }
    
    /**
     * Creates a new instance of TabDisplayer
     */
    public TabDisplayer(TabDataModel model, int type) {
        this (model, type, (WinsysInfoForTabbed)null);
    }
    
    /**
     * Depreacated, please use constructor with WinsysInfoForTabbed param.
     */
    @Deprecated
    public TabDisplayer(TabDataModel model, int type, LocationInformer locationInformer) {
        this (model, type, (WinsysInfoForTabbed)null);
        this.locationInformer = locationInformer;
    }
        
    /**
     * Depreacated, please use constructor with WinsysInfoForTabbedContainer param.
     */
    @Deprecated
    public TabDisplayer(TabDataModel model, int type, WinsysInfoForTabbed winsysInfo) {
        this( model, type, WinsysInfoForTabbedContainer.getDefault(winsysInfo) );
    }
    /**
     * Creates a new instance of TabDisplayer
     */
    public TabDisplayer(TabDataModel model, int type, WinsysInfoForTabbedContainer containerWinsysInfo) {
        switch (type) {
            case TYPE_VIEW:
            case TYPE_EDITOR:
            case TYPE_SLIDING:
            case TYPE_TOOLBAR:
                break;
            default :
                throw new IllegalArgumentException("Unknown UI type: " + type); //NOI18N
        }
        this.model = model;
        this.type = type;
        this.winsysInfo = containerWinsysInfo;
        this.containerWinsysInfo = containerWinsysInfo;
        showClose &= containerWinsysInfo.isTopComponentClosingEnabled();
        putClientProperty (PROP_ORIENTATION, ORIENTATION_NORTH);
        initialized = true;
        updateUI();
        setFocusable(false);
//        Color fillC = (Color)UIManager.get("nb_workplace_fill"); //NOI18N
//        if (fillC != null) setBackground (fillC);
    }

    public final TabDisplayerUI getUI() {
        return (TabDisplayerUI) ui;
    }

    /** Overridden to block the call from the superclass constructor, which
     * comes before the <code>type</code> property is initialized.  Provides
     * a reasonable fallback UI for use on unknown look and feels.
     */
    @Override
    public final void updateUI() {
        if (!initialized) {
            return;
        }
        
        if (type == TYPE_TOOLBAR) {
            setUI (new ToolbarTabDisplayerUI(this));
            return;
        } else if (type == TYPE_SLIDING) {
            setUI (new BasicSlidingTabDisplayerUI(this));
            return;
        }
        
        ComponentUI ui = null;
        if (UIManager.get(getUIClassID()) != null) { //Avoid Error stack trace
            try {
                ui = UIManager.getUI(this);
            } catch (Error error) {
                System.err.println("Could not load a UI for " + getUIClassID() + 
                    " - missing class?");
            }
        } else {
            ui = findUIStandalone();
        }
        
        if (ui == null) {
            ui = getType() == TYPE_VIEW ?
                    WinClassicViewTabDisplayerUI.createUI(this) :
                    WinClassicEditorTabDisplayerUI.createUI(this);
        }
        setUI((TabDisplayerUI) ui);
        
    }
    
    /**
     * Allows the tabcontrol to find the correct UI if the plaf library is
     * not present (no UI class defined in UIManager).
     */
    private ComponentUI findUIStandalone() {
        ComponentUI result = null;
        String lf = UIManager.getLookAndFeel().getID();
        switch (type) {
            case TYPE_VIEW :
                if ("Aqua".equals(lf)) { //NOI18N
                    result = AquaViewTabDisplayerUI.createUI(this);
                } else if ("Windows".equals(lf)) { //NOI18N
                    result = isXPLF() ? 
                        WinXPViewTabDisplayerUI.createUI(this) :
                        WinClassicViewTabDisplayerUI.createUI(this);
                }
                break;
            case TYPE_EDITOR :
                if ("Aqua".equals(lf)) { //NOI18N
                    result = AquaEditorTabDisplayerUI.createUI(this);
                } else if ("Windows".equals(lf)) { //NOI18N
                    result = isXPLF() ? 
                        WinXPEditorTabDisplayerUI.createUI(this) :
                        WinClassicEditorTabDisplayerUI.createUI(this);
                }
                break;
        }
        return result;
    }
    
    /** Finds if windows LF with XP theme is active.
     * @return true if windows LF and XP theme is active, false otherwise */
    private static boolean isXPLF () {
        Boolean isXP = (Boolean)Toolkit.getDefaultToolkit().
                        getDesktopProperty("win.xpstyle.themeActive"); //NOI18N
        return isXP == null ? false : isXP.booleanValue();
    }
    

    /** Returns an different UIClassID depending on the value of the <code>type</code>
     * property. */
    @Override
    public String getUIClassID() {
        switch (getType()) {
            case TYPE_VIEW : return VIEW_TAB_DISPLAYER_UI_CLASS_ID;
            case TYPE_EDITOR : return EDITOR_TAB_DISPLAYER_UI_CLASS_ID;
            case TYPE_SLIDING : return SLIDING_TAB_DISPLAYER_UI_CLASS_ID;
            case TYPE_TOOLBAR : return TOOLBAR_TAB_DISPLAYER_UI_CLASS_ID;
            default :
                throw new IllegalArgumentException ("Unknown UI type: " + 
                    getType());
        }
    }

    /**
     * Returns whether this control uses the view tab look or the scrolling
     * editor tab look.  This is set in the constructor.
     */
    public final int getType() {
        return type;
    }

    @Override
    public final Dimension getPreferredSize() {
        return getUI().getPreferredSize(this);
    }
    
    @Override
    public final Font getFont() {
        return getUI().getTxtFont();
    }

    @Override
    public final Dimension getMinimumSize() {
        return getUI().getMinimumSize(this);
    }
    
    /**
     * Cause the specified tab to flash or otherwise call attention to itself
     * without changing selection or focus.  Supported by VIEW and EDITOR type
     * UIs.
     */
    public final void requestAttention (int tab) {
        getUI().requestAttention(tab);
    }

    /**
     * Cause a tab, if blinking, to stop.
     */
    public final void cancelRequestAttention (int tab) {
        getUI().cancelRequestAttention (tab);
    }

    /**
     * Turn tab highlight on/off
     * @param tab
     * @since 1.38
     */
    public final void setAttentionHighlight (int tab, boolean highlight) {
        getUI().setAttentionHighlight (tab, highlight);
    }
    
    public final boolean requestAttention (TabData data) {
        int idx = getModel().indexOf(data);
        boolean result = idx >= 0;
        if (result) {
            requestAttention (idx);
        }
        return result;
    }

    /**
     * Accessor only for TabDisplayerUI when installing the UI
     */
    void setSelectionModel(SingleSelectionModel sel) {
        this.sel = sel;
    }

    /** Get the selection model, which determines which tab is selected.
     * To change the selection, get the selection model and call 
     * setSelectedIndex(). */
    public SingleSelectionModel getSelectionModel() {
        return sel;
    }

    /** Get the data model that defines the contents which are displayed */
    public final TabDataModel getModel() {
        return model;
    }

    /** Set the active state of the component */
    public final void setActive(boolean active) {
        if (active != this.active) {
            this.active = active;
            firePropertyChange(PROP_ACTIVE, !active, active); //NOI18N
        }
    }

    /** Gets the &quot;active&quot; state of this component.  If the component
     * is active, most UIs will paint the selected tab differently to indicate
     * that focus is somewhere in the container */
    public final boolean isActive() {
        return active;
    }

    /**
     * Gets tooltip for the tab corresponding to the mouse event, or if no
     * tab, delegates to the default implementation.
     */
    @Override
    public final String getToolTipText(MouseEvent event) {
        if (ui != null) {
            Point p = event.getPoint();
            if (event.getSource() != this) {
                Component c = (Component) event.getSource();
                p = SwingUtilities.convertPoint(c, p, this);
            }
            int index = getUI().tabForCoordinate(p);
            if (index != -1) {
                return getTooltipForTab( index );
            }
        }
        return super.getToolTipText(event);
    }

    private String getTooltipForTab( int tabIndex ) {
        if( TYPE_EDITOR == getType() ) {
           if( getUI() instanceof BasicTabDisplayerUI ) {
               BasicTabDisplayerUI basicUI = (BasicTabDisplayerUI)getUI();
               TabCellRenderer cellRenderer = basicUI.getTabCellRenderer(tabIndex);
               if( cellRenderer instanceof AbstractTabCellRenderer ) {
                   if( (((AbstractTabCellRenderer)cellRenderer).getState() & TabState.CLOSE_BUTTON_ARMED) != 0 ) {
                       return org.openide.util.NbBundle.getMessage(TabDisplayer.class, "TT_TabDisplayer_Close");
                   }
               }
           }
        }
        return getModel().getTab(tabIndex).tip;
    }

    /** Make a tab visible.  In the case of scrolling UIs, a tab is not
     * always visible.  This call will make it scroll into view */
    public final void makeTabVisible(int index) { 
        getUI().makeTabVisible(index);
    }

    /** Get the rectangle that a given tab occupies */
    public final Rectangle getTabRect(int tab, Rectangle dest) {
        if (dest == null) {
            dest = new Rectangle();
        }
        getUI().getTabRect(tab, dest);
        return dest;
    }

    @Deprecated
    public final Image getDragImage(int index) {
        return null;
    }

    /**
     * Register an ActionListener.  TabbedContainer and TabDisplayer guarantee
     * that the type of event fired will always be TabActionEvent.  There are
     * two special things about TabActionEvent: <ol> <li>There are methods on
     * TabActionEvent to find the index of the tab the event was performed on,
     * and if present, retrieve the mouse event that triggered it, for clients
     * that wish to provide different handling for different mouse buttons</li>
     * <li>TabActionEvents can be consumed.  If a listener consumes the event,
     * the UI will take no action - the selection will not be changed, the tab
     * will not be closed.  Consuming the event means taking responsibility for
     * doing whatever would normally happen automatically.  This is useful for,
     * for example, showing a dialog and possibly aborting closing a tab if it
     * contains unsaved data, for instance.</li> </ol> Action events will be
     * fired <strong>before</strong> any action has been taken to alter the
     * state of the control to match the action, so that they may be vetoed or
     * modified by consuming the event.
     *
     * @param listener The listener to register.
     */
    public final synchronized void addActionListener(ActionListener listener) {
        if (actionListenerList == null) {
            actionListenerList = new ArrayList<ActionListener>();
        }
        actionListenerList.add(listener);
    }

    /**
     * Removes ActionListener from the list of listeners.
     *
     * @param listener The listener to remove.
     */
    public final synchronized void removeActionListener(ActionListener listener) {
        if (actionListenerList != null) {
            actionListenerList.remove(listener);
        }
    }

    public void registerShortcuts(JComponent comp) {
        getUI().registerShortcuts(comp);
    }
    
    public void unregisterShortcuts(JComponent comp) {
        getUI().unregisterShortcuts(comp);
    }

    /**
     * Notifies all registered listeners about the event.
     *
     * @param event The event to be fired
     */
    protected final void postActionEvent(TabActionEvent event) {
        List<ActionListener> list;
        synchronized (this) {
            if (actionListenerList == null) {
                return;
            }
            list = Collections.unmodifiableList(actionListenerList);
        }
        for (int i = 0; i < list.size(); i++) {
            list.get(i).actionPerformed(event);
        }
    }

    public int tabForCoordinate(Point p) {
        return getUI().tabForCoordinate(p);
    }
    
    @Deprecated
    public WinsysInfoForTabbed getWinsysInfo() {
        return winsysInfo;
    }
    
    public WinsysInfoForTabbedContainer getContainerWinsysInfo() {
        return containerWinsysInfo;
    }
    
    @Deprecated
    public LocationInformer getLocationInformer() {
        return locationInformer;
    }
    
    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleTabDisplayer();
        }
        return accessibleContext;
    }

    /**
     * Set whether or not the close button should be visible.
     * This can be defaulted by setting the system property
     * <code>nb.tabs.suppressCloseButton</code>.  The default is
     * true.
     */
    public final void setShowCloseButton (boolean val) {
        boolean wasShow = isShowCloseButton();
        if (wasShow != val) {
            showClose = val;
            if (isShowing()) {
                repaint();
            }
            firePropertyChange ("showCloseButton", !val, val);
        }
    }
    
    /** Find out if this displayer is set to show close buttons */
    public final boolean isShowCloseButton () {
        return showClose;
    }

    private ComponentConverter componentConverter = ComponentConverter.DEFAULT;

    public void setComponentConverter( ComponentConverter converter ) {
        this.componentConverter = converter;
    }

    public ComponentConverter getComponentConverter() {
        return componentConverter;
    }

    @Override
    public Insets getAutoscrollInsets() {
        return getUI().getAutoscrollInsets();
    }

    @Override
    public void autoscroll( Point cursorLocn ) {
        getUI().autoscroll( cursorLocn );
    }
    
    
    protected class AccessibleTabDisplayer extends AccessibleJComponent
                                           implements AccessibleSelection, ChangeListener {
                                               
         /**
         *  Constructs an AccessibleTabDisplayer
         */
        public AccessibleTabDisplayer() {
            super();
            getModel().addChangeListener(this);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            Object o = e.getSource();
            firePropertyChange(AccessibleContext.ACCESSIBLE_SELECTION_PROPERTY,
                               null, o);
        }

        
        /**
         * Get the role of this object.
         *
         * @return an instance of AccessibleRole describing the role of 
         *          the object
         */
        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.PAGE_TAB_LIST;
        }
        
        /**
         * Returns the number of accessible children in the object.
         *
         * @return the number of accessible children in the object.
         */
        @Override
        public int getAccessibleChildrenCount() {
            return getModel().size();
        }

        /**
         * Return the specified Accessible child of the object.
         *
         * @param i zero-based index of child
         * @return the Accessible child of the object
         * @exception IllegalArgumentException if index is out of bounds
         */
        @Override
        public Accessible getAccessibleChild(int i) {
            if (i < 0 || i >= getModel().size()) {
                return null;
            }
            TabData data = getModel().getTab(i);
            if (data.getComponent() instanceof Accessible) {
                return (Accessible)data.getComponent();
            }
            return null;
        }
        
        

        /**
         * Gets the <code>AccessibleSelection</code> associated with
         * this object.  In the implementation of the Java 
         * Accessibility API for this class, 
	 * returns this object, which is responsible for implementing the
         * <code>AccessibleSelection</code> interface on behalf of itself.
	 * 
	 * @return this object
         */
        @Override
        public AccessibleSelection getAccessibleSelection() {
           return this;
        }        
        
        /**
         * Returns the <code>Accessible</code> child contained at
         * the local coordinate <code>Point</code>, if one exists.
         * Otherwise returns the currently selected tab.
         *
         * @return the <code>Accessible</code> at the specified
         *    location, if it exists
         */
        @Override
        public Accessible getAccessibleAt(Point p) {
            int tab = tabForCoordinate(p);
            if (tab == -1) {
                tab = getSelectionModel().getSelectedIndex();
            }
            return getAccessibleChild(tab);
        }        
        
        /**
         * Returns the number of Accessible children currently selected.
         * If no children are selected, the return value will be 0.
         *
         * @return the number of items currently selected.
         */
        @Override
        public int getAccessibleSelectionCount() {
            return 1;
        }
        
        /**
         * Returns an Accessible representing the specified selected child
         * of the object.  If there isn't a selection, or there are
         * fewer children selected than the integer passed in, the return
         * value will be null.
         * <p>Note that the index represents the i-th selected child, which
         * is different from the i-th child.
         *
         * @param i the zero-based index of selected children
         * @return the i-th selected child
         * @see #getAccessibleSelectionCount
         */
        @Override
        public Accessible getAccessibleSelection(int i) {
            // always just one selected.. -> ignore i
            int index  = getSelectionModel().getSelectedIndex();
            return getAccessibleChild(index);
        }
        
        /**
         * Determines if the current child of this object is selected.
         *
         * @return true if the current child of this object is selected; else false.
         * @param i the zero-based index of the child in this Accessible object.
         * @see AccessibleContext#getAccessibleChild
         */
        @Override
        public boolean isAccessibleChildSelected(int i) {
            return i == getSelectionModel().getSelectedIndex();
        }
        
        /**
         * Adds the specified Accessible child of the object to the object's
         * selection.  If the object supports multiple selections,
         * the specified child is added to any existing selection, otherwise
         * it replaces any existing selection in the object.  If the
         * specified child is already selected, this method has no effect.
         *
         * @param i the zero-based index of the child
         * @see AccessibleContext#getAccessibleChild
         */
        @Override
        public void addAccessibleSelection(int i) {
            //TODO?
        }
        
        /**
         * Removes the specified child of the object from the object's
         * selection.  If the specified item isn't currently selected, this
         * method has no effect.
         *
         * @param i the zero-based index of the child
         * @see AccessibleContext#getAccessibleChild
         */
        @Override
        public void removeAccessibleSelection(int i) {
            //TODO?
        }
        
        /**
         * Clears the selection in the object, so that no children in the
         * object are selected.
         */
        @Override
        public void clearAccessibleSelection() {
            //TODO?
        }
        
        /**
         * Causes every child of the object to be selected
         * if the object supports multiple selections.
         */
        @Override
        public void selectAllAccessibleSelection() {
            //TODO?
        }
    }
    
}
