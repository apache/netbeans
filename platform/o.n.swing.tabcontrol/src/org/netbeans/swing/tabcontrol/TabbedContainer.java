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
import org.netbeans.swing.tabcontrol.event.TabActionEvent;
import org.netbeans.swing.tabcontrol.plaf.DefaultTabbedContainerUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.accessibility.AccessibleRole;
import javax.swing.JComponent.AccessibleJComponent;
import org.openide.util.NbBundle;


/**
 * A tabbed container similar to a JTabbedPane.  The tabbed container is a
 * simple container which contains two components - the tabs displayer, and the
 * content displayer.  The tabs displayer is the thing that actually draws the
 * tabs; the content displayer contains the components that are being shown.
 * <p>
 * The first difference from a JTabbedPane is that it is entirely model driven -
 * the tab contents are in a data model owned by the displayer.  It is not
 * strictly necessary for the contained components to even be installed in the
 * AWT hierarchy when not displayed.
 * <p>
 * Other differences are more flexibility in the way tabs are displayed by
 * completely separating the implementation and UI for that from that of
 * displaying the contents.
 * <p>
 * Other interesting aspects are the ability of TabDataModel to deliver complex,
 * granular events in a single pass with no information loss.  Generally, great
 * effort has been gone to to conflate nothing - that is, adding a component
 * does not equal selecting it does not equal changing focus, et. cetera,
 * leaving these decisions more in the hands of the user of the control.
 * <p>
 * It is possible to implement a subclass which provides the API of JTabbedPane,
 * making it a drop-in replacement.
 * <p>
 * There are several UI styles a <code>TabbedContainer</code> can have.  The type
 * is passed as an argument to the constructor (support for changing these on the
 * fly may be added in the future, but such a change is a very heavyweight operation,
 * and is only desirable to enable use of this component in its various permutations
 * inside GUI designers).  The following styles are supported:
 * <ul>
 * <li><b>TYPE_VIEW</b> - These are tabs such as the Explorer window has in NetBeans -
 * all tabs are always displayed, with the available space equally divided between them.</li>
 * <li><b>TYPE_EDITOR</b> - Scrolling tabs, coupled with control buttons and mouse wheel
 * support for scrolling the visible tabs, and a popup which displays a list of tabs.</li>
 * <li><b>TYPE_SLIDING</b> - Tabs which are displayed as buttons, and may provide a
 * fade or sliding effect when displayed.  For this style, a second click on the selected
 * tab will hide the selected tab (setting the selection model's selected index to -1).</li></ul>
 * <p>
 * <h4>Customizing the appearance of tabs</h4>
 * Tabs are customized by providing a different UI delegate for the tab displayer component,
 * via UIManager, in the same manner as any standard Swing component; for <code>TYPE_SLIDING</code>
 * tabs, simply implementing an alternate UI delegate for the buttons used to represent tabs
 * is all that is needed.
 *
 * <h4>Managing user events on tabs</h4>
 * When a user clicks a tab, the TabbedContainer will fire an action event to all of its listeners.
 * This action event will always be an instance of <code>TabActionEvent</code>, which can provide
 * the index of the tab that was pressed, and the command name of the action that was performed.
 * A client which wants to handle the event itself (for example, the asking a user if they want
 * to save data, and possibly vetoing the closing of a tab) may veto (or take full responsibility
 * for performing) the action by consuming the TabActionEvent.
 *
 *<h4>Indication of focus and the &quot;activated&quot; state</h4>
 * The property <code>active</code> is provided to allow a tabbed container to indicate that it
 * contains the currently focused component. However, no effort is made to track focus on the
 * part of the tabbed control - there is too much variability possible (for example, if
 * a component inside a tab opens a modal dialog, is the tab active or not?).  In fact, using
 * keyboard focus at all to manage the activated state of the component turns out to be a potent
 * source of hard-to-fix, hard-to-reproduce bugs (especially when components are being added
 * and removed, or hidden and shown or components which do not reliably produce focus events).
 * What NetBeans does to solve the problem in a reliable way is the following:
 * <ol>
 * <li>Use an AWT even listener to track mouse clicks, and when the mouse is clicked,
 *     <ul>
 *     <li>Find the ancestor that is a tabbed container (if any)</li>
 *     <li>Set the activated state appropriately on it and the previously active container</li>
 *     <li>Ensure that keyboard focus moves into that container</li>
 *     </ul>
 * <li>Block ctrl-tab style keyboard based focus traversal out of tabbed containers</li>
 * <li>Provide keyboard actions, with menu items, which will change to a different container,
 *     activating it</li>
 * </ol>
 * This may seem complicated, and it probably is overkill for a small application (as is this
 * tabbed control - it wasn't designed for a small application).  It's primary advantage is
 * that it works.
 *
 * @see TabDisplayer
 * @author Tim Boudreau, Dafe Simonek
 */
public class TabbedContainer extends JComponent implements Accessible {
    /**
     * UIManager key for the UI Delegate to be used by tabbed containers.
     */
    public static final String TABBED_CONTAINER_UI_CLASS_ID = "TabbedContainerUI"; //NOI18N

    /**
     * Creates a &quot;view&quot; style displayer; typically this will have a
     * fixed width and a single row of tabs which get smaller as more tabs are
     * added, as seen in NetBeans&rsquo; Explorer window.
     */
    public static final int TYPE_VIEW = 0;
    /**
     * Creates a &quot;editor&quot; style displayer; typically this uses a
     * scrolling tabs UI for the tab displayer.  This is the most scalable of the available
     * UI styles - it can handle a very large number of tabs with minimal overhead, and
     * the standard UI implementations of it use a cell-renderer model for painting.
     */
    public static final int TYPE_EDITOR = 1;
    
    /** Creates a &quot;sliding&quot; view, typically with tabs rendered as
     * buttons along the left, bottom or right edge, with no scrolling behavior for tabs.
     * Significant about this UI style is that re-clicking the selected tab will
     * cause the component displayed to be hidden.
     * <p>
     * This is the least scalable of the available UI types, and is intended primarily for
     * use with a small, fixed set of tabs.  By default, the position of the tab displayer
     * will be determined based on the proximity of the container to the edges of its
     * parent window.  This can be turned off by setting the client property
     * PROP_MANAGE_TAB_POSITION to Boolean.FALSE.
     */
    public static final int TYPE_SLIDING = 2;
    
    /**
     * Creates a Toolbar-style displayer (the style used by the NetBeans Form Editor's
     * Component Inspector and a few other places in NetBeans).
     */
    public static final int TYPE_TOOLBAR = 3;

    /**
     * Property fired when <code>setActive()</code> is called
     */
    public static final String PROP_ACTIVE = "active"; //NOI18N
    
    /** Client property applicable only to TYPE_SLIDING tabs.  If set to 
     * Boolean.FALSE, the UI will not automatically try to determine a 
     * correct position for the tab displayer.
     */
    public static final String PROP_MANAGE_TAB_POSITION = "manageTabPosition";

    /**
     * Action command indicating that the action event signifies the user
     * clicking the Close button on a tab.
     */
    public static final String COMMAND_CLOSE = "close"; //NOI18N

    /**
     * Action command indicating that the action event fired signifies the user
     * selecting a tab
     */
    public static final String COMMAND_SELECT = "select"; //NOI18N

    /** Action command indicating that a popup menu should be shown */
    public static final String COMMAND_POPUP_REQUEST = "popup"; //NOI18N

    /** Command indicating a maximize (double-click) request */
    public static final String COMMAND_MAXIMIZE = "maximize"; //NOI18N

    public static final String COMMAND_CLOSE_ALL = "closeAll"; //NOI18N

    public static final String COMMAND_CLOSE_ALL_BUT_THIS = "closeAllButThis"; //NOI18N

    public static final String COMMAND_ENABLE_AUTO_HIDE = "enableAutoHide"; //NOI18N

    public static final String COMMAND_DISABLE_AUTO_HIDE = "disableAutoHide"; //NOI18N
    
    public static final String COMMAND_TOGGLE_TRANSPARENCY = "toggleTransparency"; //NOI18N
    /**
     * @since 1.27
     */
    public static final String COMMAND_MINIMIZE_GROUP = "minimizeGroup"; //NOI18N
    
    /**
     * @since 1.27
     */
    public static final String COMMAND_RESTORE_GROUP = "restoreGroup"; //NOI18N
    
    /**
     * @since 1.27
     */
    public static final String COMMAND_CLOSE_GROUP = "closeGroup"; //NOI18N
    
    //XXX support supressing close buttons
    
    /**
     * The data model which contains information about the tabs, such as the
     * corresponding component, the icon and the tooltip.  Currently this is
     * assigned in the constructor and cannot be modified later, though this
     * could be supported in the future (with substantial effort).
     *
     * @see TabData
     * @see TabDataModel
     */
    private TabDataModel model;

    /**
     * The type of this container, which determines what UI delegate is used for
     * the tab displayer
     */
    private final int type;

    /**
     * Holds the value of the active property, determining if the displayer
     * should be painted with the focused or unfocused colors
     */
    private boolean active = false;

    /**
     * Flag used to block the call to updateUI() from the superclass constructor
     * - at that time, none of our instance fields are set, so the UI can't yet
     * set up the tab displayer correctly
     */
    private boolean initialized = false;

    /**
     * Utility field holding list of ActionListeners.
     */
    private transient List<ActionListener> actionListenerList;

    /**
     * Content policy in which all components contained in the data model should immediately
     * be added to the AWT hierarchy at the time they appear in the data model.
     *
     * @see #setContentPolicy
     */
    public static final int CONTENT_POLICY_ADD_ALL = 1;
    /**
     * Content policy by which components contained in the data model are added to the AWT
     * hierarchy the first time they are shown, and remain their thereafter unless removed
     * from the data model.
     *
     * @see #setContentPolicy
     */
    public static final int CONTENT_POLICY_ADD_ON_FIRST_USE = 2;
    /**
     * Content policy by which components contained in the data model are added to the AWT
     * hierarchy the when they are shown, and removed immediately when the user changes tabs.
     */
    public static final int CONTENT_POLICY_ADD_ONLY_SELECTED = 3;

    private int contentPolicy = DEFAULT_CONTENT_POLICY ;

    /** The default content policy, currently CONTENT_POLICY_ADD_ALL.  To facilitate experimentation with
     * different settings application-wide, set the system property &quot;nb.tabcontrol.contentpolicy&quot;
     * to 1, 2 or 3 for ADD_ALL, ADD_ON_FIRST_USE or ADD_ONLY_SELECTED, respectively (note other values
     * will throw an <code>Error</code>).  Do not manipulate this value at runtime, it will likely become
     * a final field in a future release.  It is a protected field only to ensure its inclusion in documentation.
     *
     * @see #setContentPolicy
     */
    protected static int DEFAULT_CONTENT_POLICY = CONTENT_POLICY_ADD_ALL;
    
    /** The component converter which will tranlate TabData's from the model into
     * components. */
    private ComponentConverter converter = null;
    
    /** Winsys info needed for tab control or null if not available */
    private WinsysInfoForTabbed winsysInfo = null;
    
    /** Winsys info needed for tab control or null if not available */
    private WinsysInfoForTabbedContainer containerWinsysInfo = null;

    @Deprecated
    private LocationInformer locationInformer = null;

    /**
     * Create a new pane with the default model and tabs displayer
     */
    public TabbedContainer() {
        this(null, TYPE_VIEW);
    }

    /**
     * Create a new pane with asociated model and the default tabs displayer
     */
    public TabbedContainer(TabDataModel model) {
        this(model, TYPE_VIEW);
    }
    
    public TabbedContainer(int type) {
        this (null, type);
    }
    
    /**
     * Create a new pane with the specified model and displayer type
     *
     * @param model The model
     */
    public TabbedContainer(TabDataModel model, int type) {
        this (model, type, (WinsysInfoForTabbed)null);
    }

    /**
     * Deprecated, please use constructor with WinsysInfoForTabbed instead.
     */
    @Deprecated
    public TabbedContainer(TabDataModel model, int type, LocationInformer locationInformer) {
        this (model, type, (WinsysInfoForTabbed)null);
        this.locationInformer = locationInformer;
    }
        
    /**
     * Deprecated, please use constructor with WinsysInfoForTabbed instead.
     */
    @Deprecated
    public TabbedContainer(TabDataModel model, int type, WinsysInfoForTabbed winsysInfo) {
        this( model, type, WinsysInfoForTabbedContainer.getDefault( winsysInfo ) );
    }
        
    /**
     * Create a new pane with the specified model, displayer type and extra
     * information from winsys
     */
    public TabbedContainer(TabDataModel model, int type, WinsysInfoForTabbedContainer winsysInfo) {
        switch (type) {
            case TYPE_VIEW:
            case TYPE_EDITOR:
            case TYPE_SLIDING:
            case TYPE_TOOLBAR:
                break;
            default :
                throw new IllegalArgumentException("Unknown UI type: " + type); //NOI18N
        }
        if (model == null) {
            model = new DefaultTabDataModel();
        }
        this.model = model;
        this.type = Boolean.getBoolean("nb.tabcontrol.alltoolbar") ? TYPE_TOOLBAR : type;
        this.winsysInfo = winsysInfo;
        this.containerWinsysInfo = winsysInfo;
        initialized = true;
        updateUI();
        //A few borders and such will check this
        //@see org.netbeans.swing.plaf.gtk.AdaptiveMatteBorder
        putClientProperty ("viewType", new Integer(type)); //NOI18N
    }
    
    /**
     * Overridden as follows:  When called by the superclass constructor (before
     * the <code>type</code> field is set), it will simply return; the  
     * TabbedContainer constructor will call updateUI() explicitly later.
     * <p>
     * Will first search UIManager for a matching UI class.  If non-null
     * (by default it is set in the core/swing/plaf library), it will compare
     * the found class name with the current UI.  If they are a match, it
     * will call TabbedContainerUI.shouldReplaceUI() to decide whether to
     * actually do anything or not (in most cases it would just replace an
     * instance of DefaultTabbedContainerUI with another one; but this call
     * allows DefaultTabbedContainerUI.uichange() to update the tab displayer
     * as needed).
     * <p>
     * If no UIManager UI class is defined, this method will silently use an
     * instance of DefaultTabbedContainerUI.
     */
    @Override
    public void updateUI() {
        if (!initialized) {
            //Block the superclass call to updateUI(), which comes before the
            //field is set to tell if we are a view tab control or an editor
            //tab control - the UI won't be able to set up the tab displayer
            //correctly until this is set.
            return;
        }
        TabbedContainerUI ui = null;
        String UIClass = (String) UIManager.get(getUIClassID());
        if (getUI() != null && (getUI().getClass().getName().equals(UIClass) | UIClass == null)) {
            if (!getUI().shouldReplaceUI()) {
                return;
            }
        }
        
        if (UIClass != null) { //Avoid a stack trace
            try {
                ui = (TabbedContainerUI) UIManager.getUI(this);
            } catch (Error e) {
                //do nothing
            }
        }
        if (ui != null) {
            setUI(ui);
        } else {
            setUI(DefaultTabbedContainerUI.createUI(this));
        }
    }
    
    /**
     * Get the type of this displayer - it is either TYPE_EDITOR or TYPE_VIEW.
     * This property is set in the constructor and is immutable
     */
    public final int getType() {
        return type;
    }

    /**
     * Returns <code>TabbedContainer.TABBED_CONTAINER_UI_CLASS_ID</code>
     */
    @Override
    public String getUIClassID() {
        return TABBED_CONTAINER_UI_CLASS_ID;
    }

    /** Get the ui delegate for this component */
    public TabbedContainerUI getUI() {
        return (TabbedContainerUI) ui;
    }
    
    /**
     * Set the converter that converts user objects in the data model into
     * components to display.  If set to null (the default), the user object
     * at the selected index in the data model will be cast as an instance
     * of JComponent when searching for what to show for a given tab.  
     * <p>
     * For use cases where a single component is to be displayed for more
     * than one tab, just reconfigured when the selection changes, simply
     * supply a ComponentConverter.Fixed with the component that should be
     * used for all tabs.
     */
    public final void setComponentConverter (ComponentConverter cc) {
        ComponentConverter old = converter;
        converter = cc;
        if (old instanceof ComponentConverter.Fixed && cc instanceof ComponentConverter.Fixed) {
            List<TabData> l = getModel().getTabs();
            if (!l.isEmpty()) {
                TabData[] td = l.toArray (new TabData[0]);
                getModel().setTabs (new TabData[0]);
                getModel().setTabs(td);
            }
        }
    }
    
    /** Get the component converter which is used to fetch a component
     * corresponding to an element in the data model.  If the value has
     * not been set, it will use ComponentConverter.DEFAULT, which simply
     * delegates to TabData.getComponent(). 
     */
    public final ComponentConverter getComponentConverter() {
        if (converter != null) {
            return converter;
        }
        return ComponentConverter.DEFAULT;
    }

    /** Experimental property - alter the policy by which the components in
     * the model are added to the container.  This may not remain suppported.
     * If used, it should be called before populating the data model.
     */
    public final void setContentPolicy(int i) {
        switch (i) {
            case CONTENT_POLICY_ADD_ALL :
            case CONTENT_POLICY_ADD_ON_FIRST_USE :
            case CONTENT_POLICY_ADD_ONLY_SELECTED :
                break;
            default :
                throw new IllegalArgumentException ("Unknown content policy: " 
                    + i);
        }
        
        if (i != contentPolicy) {
            int old = contentPolicy;
            contentPolicy = i;
            firePropertyChange ("contentPolicy", old, i); //NOI18N
        }
    }

    /** Determine the policy by which components are added to the container.
     * There are various pros and cons to each:
     * <ul>
     * <li>CONTENT_POLICY_ADD_ALL - All components in the data model are
     * automatically added to the container, and whenever the model changes,
     * components are added and removed as need be.  This is less scalable,
     * but absolutely reliable</li>
     * <li>CONTENT_POLICY_ADD_ON_FIRST_USE - Components are not added to the
     * container until the first time they are used, and then they remain in
     * the AWT hierarchy until their TabData elements are removed from the 
     * model.  This is more scalable, and probably has some startup time 
     * benefits</li>
     * <li>CONTENT_POLICY_ADD_ONLY_SELECTED - The only component that will
     * ever be in the AWT hierarchy is the one that is being displayed.  This
     * is safest in the case that heavyweight AWT components may be used </li>
     * </ul>
     */
    public int getContentPolicy() {
        return contentPolicy;
    }
    
    @Override
    public boolean isValidateRoot() {
        return true;
    }

    @Override
    public boolean isPaintingOrigin() {
        return true;
    }
    

    public void setToolTipTextAt(int index, String toolTip) {
        //Do this quietly - no notification is needed
        TabData tabData = getModel().getTab(index);
        if (tabData != null) {
            tabData.tip = toolTip;
        }
    }

    /**
     * Get the data model that represents the tabs this component has.  All
     * programmatic manipulation of tabs should be done via the data model.
     *
     * @return The model
     */
    public final TabDataModel getModel() {
        return model;
    }

    /**
     * Get the selection model.  The selection model tracks the index of the
     * selected component, modifying this index appropriately when tabs are
     * added or removed.
     *
     * @return The model
     */
    public final SingleSelectionModel getSelectionModel() {
        return getUI().getSelectionModel();
    }
    
    /**
     * Fetch the rectangle of the tab for a given index, in the coordinate space
     * of this component, by reconfiguring the passed rectangle object
     */
    public final Rectangle getTabRect(int index, final Rectangle r) {
        return getUI().getTabRect(index, r);
    }

    /** Gets the index of the tab at point p, or -1 if no tab is there */
    public int tabForCoordinate (Point p) {
        return getUI().tabForCoordinate(p);
    }

    /**
     * Set the &quot;active&quot; state of this tab control - this affects the
     * way the tabs are displayed, to indicate focus.  Note that this method
     * will <i>never</i> be called automatically in stand-alone use of
     * TabbedContainer. While one would expect a component gaining keyboard
     * focus to be a good determinant, it actually turns out to be a potent
     * source of subtle and hard-to-fix bugs.
     * <p/>
     * NetBeans uses an AWTEventListener to track mouse clicks, and allows
     * components to become activated only via a mouse click or via a keyboard
     * action or menu item which activates the component.  This approach is far
     * more robust and is the recommended usage pattern.
     */
    public final void setActive(boolean active) {
        if (active != this.active) {
            this.active = active;
            firePropertyChange(PROP_ACTIVE, !active, active);
        }
    }
    
    /**
     * Cause the tab at the specified index to blink or otherwise suggest that
     * the user should click it.
     */
    public final void requestAttention (int tab) {
        getUI().requestAttention(tab);
    }
    
    public final void cancelRequestAttention (int tab) {
        getUI().cancelRequestAttention(tab);
    }

    /**
     * Turn tab highlight on/off
     * @param tab
     * @since 1.38
     */
    public final void setAttentionHighlight (int tab, boolean highlight) {
        getUI().setAttentionHighlight(tab, highlight);
    }
    
    /**
     * Cause the specified tab to blink or otherwisse suggest that the user should
     * click it.
     */
    public final boolean requestAttention (TabData data) {
        int idx = getModel().indexOf(data);
        boolean result = idx >= 0;
        if (result) {
            requestAttention (idx);
        }
        return result;
    }    

    public final void cancelRequestAttention (TabData data) {
        int idx = getModel().indexOf(data);
        if (idx != -1) {
            cancelRequestAttention(idx);
        }
    }

    /**
     *
     * @param data
     * @since 1.38
     */
    public final void setAttentionHighlight (TabData data, boolean highlight) {
        int idx = getModel().indexOf(data);
        if (idx != -1) {
            setAttentionHighlight(idx, highlight);
        }
    }

    /**
     * Determine if this component thinks it is &quot;active&quot;, which
     * affects how the tabs are painted - typically used to indicate that
     * keyboard focus is somewhere within the component
     */
    public final boolean isActive() {
        return active;
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
     * Remove an action listener.
     *
     * @param listener The listener to remove.
     */
    public final synchronized void removeActionListener(
            ActionListener listener) {
        if (actionListenerList != null) {
            actionListenerList.remove(listener);
            if (actionListenerList.isEmpty()) {
                actionListenerList = null;
            }
        }
    }

    /**
     * Used by the UI to post action events for selection and close operations.
     * If the event is consumed, the UI should take no action to change the
     * selection or close the tab, and will presume that the receiver of the
     * event is handling performing whatever action is appropriate.
     *
     * @param event The event to be fired
     */
    protected final void postActionEvent(TabActionEvent event) {
        List<ActionListener> list;
        synchronized (this) {
            if (actionListenerList == null)
                return;
            list = Collections.unmodifiableList(actionListenerList);
        }
        for( ActionListener l : list ) {
            l.actionPerformed(event);
        }
    }

    public void setIconAt(int index, Icon icon) {
        getModel().setIcon(index, icon);
    }

    public void setTitleAt(int index, String title) {
        getModel().setText(index, title);
    }

    /** Create an image of a single tab, suitable for use in drag and drop operations */
    public Image createImageOfTab(int idx) {
        return getUI().createImageOfTab (idx);
    }
    
    /** Get the number of tabs.  Equivalent to <code>getModel().size()</code> */
    public int getTabCount() {
        return getModel().size();
    }
    
    /**
     * Set whether or not close buttons should be shown.
     * This can be defaulted with the system property
     * <code>nb.tabs.suppressCloseButton</code>; if the system
     * property is not set, the default is true.
     */
    public final void setShowCloseButton (boolean val) {
        boolean wasShow = isShowCloseButton();
        if (val != wasShow) {
            getUI().setShowCloseButton(val);
            firePropertyChange ("showCloseButton", wasShow, val);
        }
    }
    
    /**
     * Determine whether or not close buttons are being shown. 
     */
    public final boolean isShowCloseButton () {
        return getUI().isShowCloseButton();
    }

    /** Get the index of a component */
    public int indexOf (Component comp) {
        int max = getModel().size();
        TabDataModel mdl = getModel();
        for (int i=0; i < max; i++) {
            if (getComponentConverter().getComponent(mdl.getTab(i)) == comp) {
                return i;
            }
        }
        return -1;
    }

    /** The index at which a tab should be inserted if a drop operation
     * occurs at this point.
     *
     * @param location A point anywhere on the TabbedContainer
     * @return A tab index, or -1
     */
    public int dropIndexOfPoint (Point location) {
        return getUI().dropIndexOfPoint(location);
    }

    /**
     * Get a shape appropriate for drawing on the window's glass pane to indicate
     * where a component should appear in the tab order if it is dropped here.
     *
     * @param dragged An object being dragged, or null. The object may be an instance
     *        of <code>TabData</code> or <code>Component</code>, in which case a check
     *        will be done of whether the dragged object is already in the data model,
     *        so that attempts to drop the object over the place it already is in the
     *        model will always return the exact indication of that tab's position.
     *
     * @param location A point
     * @return Drop indication drawing
     */
    public Shape getDropIndication(Object dragged, Point location) {
        int ix;
        if (dragged instanceof Component) {
            ix = indexOf((Component)dragged);
        } else if (dragged instanceof TabData) {
            ix = getModel().indexOf((TabData) dragged);
        } else {
            ix = -1;
        }

        int over = dropIndexOfPoint(location);

        if (over == ix && ix != -1) {
            return getUI().getExactTabIndication(over);
        } else {
            return getUI().getInsertTabIndication(over);
        }
    }
    
    @Deprecated
    public LocationInformer getLocationInformer() {
        return locationInformer;
    }
    
    @Deprecated
    public WinsysInfoForTabbed getWinsysInfo() {
        return winsysInfo;
    }

    public WinsysInfoForTabbedContainer getContainerWinsysInfo() {
        return containerWinsysInfo;
    }
    
    static {
        //Support for experimenting with different content policies in NetBeans
        String s = System.getProperty("nb.tabcontrol.contentpolicy"); //NOI18N
        if (s != null) {
            try {
                DEFAULT_CONTENT_POLICY = Integer.parseInt (s);
                switch (DEFAULT_CONTENT_POLICY) {
                    case CONTENT_POLICY_ADD_ALL :
                    case CONTENT_POLICY_ADD_ON_FIRST_USE :
                    case CONTENT_POLICY_ADD_ONLY_SELECTED :
                        System.err.println("Using custom content policy: " + DEFAULT_CONTENT_POLICY);
                        break;
                    default :
                        throw new Error ("Bad value for default content " +
                                "policy: " + s + " only values 1, 2 or 3" +
                                "are meaningful"); //NOI18N
                }
                System.err.println ("Default content policy is " + DEFAULT_CONTENT_POLICY);
            } catch (Exception e) {
                System.err.println ("Error parsing default content " +
                    "policy: \"" + s + "\""); //NOI18N
            }
        }
    }

    @Override
    public javax.accessibility.AccessibleContext getAccessibleContext() {
        if( null == accessibleContext ) {
            accessibleContext = new AccessibleJComponent() {
                        @Override
                        public AccessibleRole getAccessibleRole() {
                            return AccessibleRole.PAGE_TAB_LIST;
                        }
                    };
        
            accessibleContext.setAccessibleName( NbBundle.getMessage(TabbedContainer.class, "ACS_TabbedContainer") );
            accessibleContext.setAccessibleDescription( NbBundle.getMessage(TabbedContainer.class, "ACSD_TabbedContainer") );
        }
        
        return accessibleContext;
    }
    
    //+++++++++++++++++++++++++
    //Begin: Transparency support 
    //+++++++++++++++++++++++++
    private static final float ALPHA_TRESHOLD = 0.1f;
    private float currentAlpha = 1.0f;
    
    /**
     * @return True if the container is transparent, i.e. painted if Alpha set to 0.2 or less.
     */
    public boolean isTransparent() {
        return isSliding() && currentAlpha <= ALPHA_TRESHOLD;
    }
    
    /**
     * Turn container transparency on/off
     * @param transparent True to make the container transparent
     */
    public void setTransparent( boolean transparent ) {
        _setTransparent( transparent );
        inTransparentMode = transparent;
    }

    private void _setTransparent( boolean transparent ) {
        if( isSliding() ) {
            //#129444 - AWT events may be retargeted icorrectly sometimes
            float oldAlpha = currentAlpha;
            currentAlpha = transparent ? ALPHA_TRESHOLD : 1.0f;
            if( oldAlpha != currentAlpha ) {
                repaint();
            }
        }
    }

    private boolean inTransparentMode = false;
    AWTEventListener awtListener = null;
    private AWTEventListener getAWTListener() {
        if( null == awtListener ) {
            awtListener = new AWTEventListener() {
                public void eventDispatched(AWTEvent event) {
                    if( !isSliding() )
                        return;
                    if( event.getID() == MouseEvent.MOUSE_PRESSED 
                            || event.getID() == MouseEvent.MOUSE_RELEASED
                            || event.getID() == MouseEvent.MOUSE_WHEEL) {
                        //ignore mouse clicks outside this container
                        if( event.getSource() instanceof Component
                            && !SwingUtilities.isDescendingFrom((Component)event.getSource(), TabbedContainer.this) )
                            return;
                        _setTransparent( false );
                    } else if( event.getID() == KeyEvent.KEY_PRESSED ) {
                        KeyEvent ke = (KeyEvent)event;
                        //TODO make shortcut configurable
                        if( ke.getKeyCode() == KeyEvent.VK_NUMPAD0 && ke.isControlDown() && !inTransparentMode ) {
                            setTransparent( true );
                            ke.consume();
                            return;
                        }
                    } else if( event.getID() == KeyEvent.KEY_RELEASED ) {
                        setTransparent( false );
                        return;
                    }
                }
            };
        }
        return awtListener;
    }

    /**
     * @return True if the container holds slided-in window.
     */
    private boolean isSliding() {
        boolean res = false;
        if( getModel().size() == 1 ) {
            Component c = getModel().getTab(0).getComponent();
            if( c instanceof JComponent ) {
                Object val = ((JComponent)c).getClientProperty("isSliding"); //NOI18N
                res = val instanceof Boolean && ((Boolean) val).booleanValue();
            }
        }
        return res;
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        if( isSliding() ) {
            //register AWT listener in case the inner top component has its only mouse listener
            Toolkit.getDefaultToolkit().addAWTEventListener( getAWTListener(), 
                    MouseEvent.MOUSE_WHEEL_EVENT_MASK+MouseEvent.MOUSE_EVENT_MASK+KeyEvent.KEY_EVENT_MASK );
        }
    }
    
    @Override
    public void removeNotify() {
        if( null != awtListener ) {
            Toolkit.getDefaultToolkit().removeAWTEventListener( awtListener );
            awtListener = null;
        }
        super.removeNotify();
        currentAlpha = 1.0f;
    }

    @Override
    public void paint(Graphics g) {
        if( isSliding() && currentAlpha != 1.0f ) {
            Graphics2D g2d = (Graphics2D)g;
            Composite oldComposite = g2d.getComposite();
            g2d.setComposite( AlphaComposite.getInstance(AlphaComposite.SRC_OVER, currentAlpha) );
            super.paint(g);
            g2d.setComposite(oldComposite);
        } else {
            super.paint(g);
        }
    }
    //+++++++++++++++++++++++++
    //End: Transparency support 
    //+++++++++++++++++++++++++    
}
