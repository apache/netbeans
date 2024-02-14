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

package org.openide.windows;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Window;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Manages window system.
 * Allows the work with window system components, i.e. <code>Mode</code>s, <code>TopComponentGroup</code>s
 * and provides handling of operations provided over <code>TopComponent</code>s.
 * <p>
 * <b><font color="red"><em>Important note: Do not provide implementation of this abstract class unless you are window system provider!</em></font></b>
 *
 * @author Jaroslav Tulach
 */
public abstract class WindowManager extends Object implements Serializable {
    /** property change of workspaces.
     * @deprecated Do not use. Workspaces are not supported anymore. */
    @Deprecated
    public static final String PROP_WORKSPACES = "workspaces"; // NOI18N

    /** property change of current workspace.
     * @deprecated Do not use. Workspaces are not supported anymore.
     */
    @Deprecated
    public static final String PROP_CURRENT_WORKSPACE = "currentWorkspace"; // NOI18N

    /** Name of property for modes in the workspace.
     * @since 4.13 */
    public static final String PROP_MODES = "modes"; // NOI18N

    /** Instance of dummy window manager. */
    private static WindowManager dummyInstance;
    static final long serialVersionUID = -4133918059009277602L;

    /** The top component which is currently active */
    private Reference<TopComponent> activeComponent = new WeakReference<TopComponent>(null);

    /** the registry */
    private TopComponent.Registry registry;
    /** handle {@link OnShowing} */
    private final OnShowingHandler onShowing = new OnShowingHandler(null, this);

    /** Singleton instance accessor method for window manager. Provides entry
     * point for further work with window system API of the system.
     *
     * @return instance of window manager installed in the system
     * @since 2.10
     */
    public static final WindowManager getDefault() {
        WindowManager wmInstance = Lookup.getDefault().lookup(WindowManager.class);

        return (wmInstance != null) ? wmInstance : getDummyInstance();
    }

    private static synchronized WindowManager getDummyInstance() {
        if (dummyInstance == null) {
            dummyInstance = new DummyWindowManager();
        }

        return dummyInstance;
    }

    /** Finds mode of specified name.
     * @return <code>Mode</code> whith the specified name is or <code>null</code>
     *          if there does not exist such <code>Mode</code> inside window system.
     * @since 4.13 */
    public abstract Mode findMode(String name);

    /** Finds mode which contains specified <code>TopComponent</code>.
     * @return <code>Mode</code> which contains specified <code>TopComponent</code> or <code>null</code>
     *          if the <code>TopComponent</code> is not added into any <code>Mode</code> inside window system.
     * @since 4.13 */
    public abstract Mode findMode(TopComponent tc);

    /** Gets set of all <code>Mode</code>S added into window system.
     * @since 4.13 */
    public abstract Set<? extends Mode> getModes();

    /**
     * Gets the NetBeans Main Window.
    * This should ONLY be used for:
    * <UL>
    *   <LI>using the Main Window as the parent for dialogs</LI>
    *   <LI>using the Main Window's position for preplacement of windows</LI>
    * </UL>
     * Since version 6.36 the default implementation in org.netbeans.core.windows
     * module first checks already opened Frames (see Frame.getFrames()) and if
     * there is a Frame named 'NbMainWindow' then it is reused as NetBeans main
     * window. Otherwise a new Frame is created instead.
    * @return the Main Window
    */
    public abstract Frame getMainWindow();

    /** Called after a Look&amp;Feel change to update the NetBeans UI.
    * Should call {@link javax.swing.JComponent#updateUI} on all opened windows.
    */
    public abstract void updateUI();

    /** Create a component manager for the given top component.
    * @param c the component
    * @return the manager to handle opening, closing and selecting the component
    */
    protected abstract WindowManager.Component createTopComponentManager(TopComponent c);

    /** Access method for registry of all components in the system.
    * @return the registry
    */
    protected TopComponent.Registry componentRegistry() {
        return Lookup.getDefault().lookup(TopComponent.Registry.class);
    }

    /** Getter for component registry.
    * @return the registry
    */
    public synchronized TopComponent.Registry getRegistry() {
        if (registry != null) {
            return registry;
        }
        onShowing.initialize();
        return registry = componentRegistry();
    }

    /** Creates new workspace.
     * @param name the name of the workspace
     * @return new workspace
     * @deprecated Do not use. Workspaces are not supported anymore. */
    @Deprecated
    public final Workspace createWorkspace(String name) {
        return createWorkspace(name, name);
    }

    /** Creates new workspace with I18N support.
     * Note that it will not be displayed until {@link #setWorkspaces} is called
     * with an array containing the new workspace.
     * @param name the code name (used for internal purposes)
     * @param displayName the display name
     * @return the new workspace
     * @deprecated Do not use. Workspaces are not supported anymore. */
    @Deprecated
    public abstract Workspace createWorkspace(String name, String displayName);

    /** Finds workspace given its name.
     * @param name the (code) name of workspace to find
     * @return workspace or null if not found
     * @deprecated Do not use. Workspaces are not supported anymore. */
    @Deprecated
    public abstract Workspace findWorkspace(String name);

    /**
     * Gets a list of all workspaces.
     * @return an array of all known workspaces
     * @deprecated Do not use. Workspaces are not supported anymore. */
    @Deprecated
    public abstract Workspace[] getWorkspaces();

    /** Sets new array of workspaces.
     * In conjunction with {@link #getWorkspaces}, this may be used to reorder
     * workspaces, or add or remove workspaces.
     * @param workspaces An array consisting of new workspaces.
     * @deprecated Do not use. Workspaces are not supported anymore. */
    @Deprecated
    public abstract void setWorkspaces(Workspace[] workspaces);

    /**
     * Gets the current workspace.
     * @return the currently active workspace
     * @see Workspace#activate
     * @deprecated Do not use. Workspaces are not supported anymore. */
    @Deprecated
    public abstract Workspace getCurrentWorkspace();

    /** Finds <code>TopComponentGroup</code> of given name.
     * @return instance of TopComponetnGroup or null
     * @since 4.13 */
    public abstract TopComponentGroup findTopComponentGroup(String name);

    //
    // You can add implementation to this class (+firePropertyChange), or implement it in subclass
    // Do as you want.
    //

    /**
     * Attaches a listener for changes in workspaces.
     * @param l the new listener
     */
    public abstract void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Removes a listener for changes in workspaces.
     * @param l the listener to remove
     */
    public abstract void removePropertyChangeListener(PropertyChangeListener l);

    /** Finds top component manager for given top component.
     * @param tc top component to find manager for.
     * @return component manager for given top component.
     * @deprecated Do not use anymore.
     * See {@link WindowManager.Component} deprecation.
     */
    @Deprecated
    protected static final Component findComponentManager(TopComponent tc) {
        return null;
    }

    /** Activate a component. The top component containers should inform
    * the top component that it is active via a call to this method through
    * derived window manager implementation.
    * @param tc the top component to activate;
    * or <code>null</code> to deactivate all top components
    */
    protected void activateComponent(TopComponent tc) {
        // check
        if (getActiveComponent() == tc) {
            return;
        }

        TopComponent old = getActiveComponent();
        // deactivate old if possible
        if (old != null) {
            try {
                old.componentDeactivated();
            } catch (Throwable th) {
                logThrowable(th, "[Winsys] TopComponent " + old.getClass().getName() // NOI18N
                         +" throws runtime exception from its componentDeactivated() method.\nPlease repair it!"); // NOI18N
            }
        }

        setActiveComponent(tc);
        TopComponent newTC = getActiveComponent();

        if (newTC != null) {
            try {
                newTC.componentActivated();
            } catch (Throwable th) {
                logThrowable(th, "[Winsys] TopComponent " + newTC.getClass().getName() // NOI18N
                         +" throws runtime exception from its componentActivated() method.\nPlease repair it!"); // NOI18N
            }
        }
    }

    /** Notifies component that it was opened (and wasn't opened on any
     * workspace before). Top component manager that implements Component
     * inner interface of this class should send open notifications via
     * calling this method
     * @param tc the top component to be notified
     */
    protected void componentOpenNotify(TopComponent tc) {
        try {
            tc.componentOpened();
        } catch (Throwable th) {
            logThrowable(th, "[Winsys] TopComponent " + tc.getClass().getName() // NOI18N
                 +" throws exception/error from its componentOpened() method.\nPlease repair it!"); // NOI18N
        }
    }

    /** Notifies component that it was closed (and is not opened on any
     * workspace anymore). Top component manager that implements Component
     * inner interface of this class should send close notifications via
     * calling this method
     * @param tc the top component to be notified
     */
    protected void componentCloseNotify(TopComponent tc) {
        try {
            tc.componentClosed();
        } catch (Throwable th) {
            logThrowable(th, "[Winsys] TopComponent " + tc.getClass().getName() // NOI18N
                     +" throws exception/error from its componentClosed() method.\nPlease repair it!"); // NOI18N
        }

        if (tc == getActiveComponent()) {
            activateComponent(null);
        }
    }

    /** Notifies <code>TopComponent</code> it is about to be shown.
     * @param tc <code>TopComponent</code> to be notified
     * @see TopComponent#componentShowing
     * @since 2.18 */
    protected void componentShowing(TopComponent tc) {
        try {
            tc.componentShowing();
        } catch (Throwable th) {
            logThrowable(th, "[Winsys] TopComponent " + tc.getClass().getName() // NOI18N
                    +" throws runtime exception from its componentShowing() method.\nPlease repair it!"); // NOI18N
        }
    }

    /** Notifies <code>TopComponent</code> it was hidden.
     * @param tc <code>TopComponent</code> to be notified
     * @see TopComponent#componentHidden
     * @since 2.18 */
    protected void componentHidden(TopComponent tc) {
        try {
            tc.componentHidden();
        } catch (Throwable th) {
            logThrowable(th, "[Winsys] TopComponent " + tc.getClass().getName() // NOI18N
                    +" throws runtime exception from its componentHidden() method.\nPlease repair it!"); // NOI18N
        }
    }
    
    /** #113158: even errors may come
     * from TopComponent.componentOpened or componentClosed.
     */
    private static void logThrowable (Throwable th, String message) {
        if (th instanceof ThreadDeath || th instanceof OutOfMemoryError) {
            // let us R.I.P. :-)
            throw (Error) th;
        }
        Logger.getLogger(WindowManager.class.getName()).log(Level.WARNING, message, th);
    }

    /** Provides opening of specified <code>TopComponent</code>.
     * @param tc <code>TopComponent</code> to open
     * @since 4.13 */
    protected abstract void topComponentOpen(TopComponent tc);
    
    /** Opens given TopComponent at given position in the mode. TopComponent is inserted at given
     * position, positions of already opened TopComponents in the same mode are
     * incremented.
     * 
     * <ul>
     *    <li>Does no operation if TopComponent is already opened.</li>
     *    <li>For position value less then 0, TopComponent is opened at position 0, the very first one.</li>
     *    <li>For position value greater then count of opened TopComponents in the mode,
     *          TopComponent is opened at last position</li>
     * </ul>
     * 
     * @param tc TopComponent which is opened.  
     * @param position Index of the requested position.
     * @since 6.15
     */
    protected void topComponentOpenAtTabPosition(TopComponent tc, int position) {
        topComponentOpen(tc);
    }
    
    /** Gives position index of given TopComponent in the mode. Result is
     * undefined for closed TopComponents.
     * 
     * @param tc TopComponent for which position is returned. 
     * @return Index of position.
     * @since 6.15
     */
    protected int topComponentGetTabPosition(TopComponent tc) {
        Mode mode = findMode(tc);
        if (mode == null || !topComponentIsOpened(tc)) {
            return -1;
        }
        
        TopComponent[] tcs = mode.getTopComponents();
        for (int i = 0; i < tcs.length; i++) {
            if (tcs[i] == tc) {
                return i;
            }
        }

        return -1;
    }

    /** Provides closing of specified <code>TopComponent</code>.
     * @param tc <code>TopComponent</code> to close
     * @since 4.13 */
    protected abstract void topComponentClose(TopComponent tc);

    /** Provides activation of specified <code>TopComponent</code>.
     * @param tc <code>TopComponent</code> to activate
     * @since 4.13 */
    protected abstract void topComponentRequestActive(TopComponent tc);

    /** Provides selection of specfied <code>TopComponent</code>.
     * @param tc <code>TopComponent</code> to set visible (select)
     * @since 4.13 */
    protected abstract void topComponentRequestVisible(TopComponent tc);

    /** Informs about change of display name of specified <code>TopComponent</code>.
     * @param tc <code>TopComponent</code> which display name has changed
     * @param displayName newly changed display name value
     * @since 4.13 */
    protected abstract void topComponentDisplayNameChanged(TopComponent tc, String displayName);
    
    /** Informs about change of html display name of specified <code>TopComponent</code>.
     * @param tc <code>TopComponent</code> which display name has changed
     * @param htmlDisplayName newly changed html display name value
     * @since 6.4 */
    protected abstract void topComponentHtmlDisplayNameChanged(TopComponent tc, String htmlDisplayName);

    /** Informs about change of tooltip of specified <code>TopComponent</code>.
     * @param tc <code>TopComponent</code> which tooltip has changed
     * @param toolTip newly changed tooltip value
     * @since 4.13 */
    protected abstract void topComponentToolTipChanged(TopComponent tc, String toolTip);

    /** Informs about chagne of icon of specified <code>TopComponent</code>.
     * @param tc <code>TopComponent</code> which icon has changed
     * @param icon newly chaned icon value
     * @since 4.13 */
    protected abstract void topComponentIconChanged(TopComponent tc, Image icon);

    /** Informs about change of activated nodes of specified <code>TopComponent</code>.
     * @param tc <code>TopComponent</code> which activated nodes has chagned
     * @param activatedNodes newly chaged activated nodes value
     * @since 4.13 */
    protected abstract void topComponentActivatedNodesChanged(TopComponent tc, Node[] activatedNodes);

    /** Indicates whether specified <code>TopComponent</code> is opened.
     * @param tc specified <code>TopComponent</code>
     * @since 4.13 */
    protected abstract boolean topComponentIsOpened(TopComponent tc);

    /** Gets default list of actions which appear in popup menu of TopComponent.
     * The popup menu which is handled by window systsm implementation, typically at tab.
     * @param tc <code>TopComponent</code> for which the default actions to provide
     * @since 4.13 */
    protected abstract javax.swing.Action[] topComponentDefaultActions(TopComponent tc);

    /** Returns unique ID for specified <code>TopComponent</code>.
     * @param tc <code>TopComponent</code> the component for which is ID returned
     * @param preferredID first approximation used for ID
     * @return unique <code>TopComponent</code> ID
     * @since 4.13 */
    protected abstract String topComponentID(TopComponent tc, String preferredID);

    /**
     * Cause this TopComponent's tab to flash or otherwise draw the users' attention
     * to it.
     * Note to WindowManager providers: This method not abstract for backward compatibility reasons,
     * please override and provide implementation.
     * @param tc A TopComponent
     * @since 5.1 */
    protected void topComponentRequestAttention(TopComponent tc) {
    }

    /**
     * Notifies the user that some process is running in the given TopComponent,
     * for example by drawing an animated "wait" icon in TopComponent's header.<br>
     * The default implementation does nothing.
     *
     * @param tc
     * @param busy True to start 'busy' notification, false to stop it.
     *
     * @since 6.51
     */
    protected void topComponentMakeBusy( TopComponent tc, boolean busy ) {
    }

    /**
     * Attempts to bring the parent <code>Window</code> of the given <code>TopComponent</code>
     * to front of other windows.
     * @see java.awt.Window#toFront()
     * @since 5.8
     */
    protected void topComponentToFront(TopComponent tc) {
        Window parentWindow = SwingUtilities.getWindowAncestor(tc);

        // be defensive, although w probably will always be non-null here
        if (null != parentWindow) {
            if (parentWindow instanceof Frame) {
                Frame parentFrame = (Frame) parentWindow;
                int state = parentFrame.getExtendedState();

                if ((state & Frame.ICONIFIED) > 0) {
                    parentFrame.setExtendedState(state & ~Frame.ICONIFIED);
                }
            }

            parentWindow.toFront();
        }
    }

    /**
     * Stop this TopComponent's tab from flashing if it is flashing.
     * Note to WindowManager providers: This method not abstract for backward compatibility reasons,
     * please override and provide implementation.
     *
     * @param tc A TopComponent
     * @since 5.1 */
    protected void topComponentCancelRequestAttention(TopComponent tc) {
    }

    /**
     * Highlights the tab of the given TopComponent until user activates it.
     * @param tc
     * @param highlight True to highlight the tab, false to switch the highlight off.
     * @since 6.58
     */
    protected void topComponentAttentionHighlight(TopComponent tc, boolean highlight) {
    }

    /** Returns unique ID for specified <code>TopComponent</code>.
     * @param tc <code>TopComponent</code> the component for which is ID returned
     * @return unique <code>TopComponent</code> ID
     * @since 4.13 */
    public String findTopComponentID(TopComponent tc) {
        return topComponentID(tc, tc.preferredID());
    }

    /** Returns <code>TopComponent</code> for given unique ID.
     * @param tcID unique <code>TopComponent</code> ID
     * @return <code>TopComponent</code> instance corresponding to unique ID
     * @since 4.15 */
    public abstract TopComponent findTopComponent(String tcID);
    
    /** Provides support for executing a piece of code when UI of the window
     * system is ready. 
     * The behaviour is similar to {@link EventQueue#invokeLater}
     * moreover it is guaranteed that only one Runnable runs at given time.
     * This method can be invoked from any thread.
     *
     * <p class="non-normative">
     * The typical usecase is to call this method during startup of NetBeans
     * based application. The default manager then waits till the main window
     * is opened and then executes all the registered methods one by one.
     * </p>
     * 
     * <b>Usage:</b>
     * <pre>
     *  // some initialization method
     *  public static void init () {
     *     WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
     *        public void run() {
     *           // code to be invoked when system UI is ready
     *        }
     *     );
     *  }
     * </pre>
     * 
     * Note to WindowManager providers: This method is not abstract for backward compatibility reasons,
     * please override and provide implementation.
     * 
     * @param run the runnable that executes piece of code when UI of the system is ready
     * @since 6.8
     */
    public void invokeWhenUIReady(Runnable run) {
        EventQueue.invokeLater(run);
    }
    
    /**
     * <p>Check whether the given TopComponent will be/is docked into an 'editor' Mode.</p>
     * <p>Please note that some TopComponents may be docked into 'editor' modes as well as 
     * 'view' modes, see method isTopComponentAllowedToMoveAnywhere().</p>
     * 
     * @param tc TopComponent to check.
     * @return True if there is a Mode that the TopComponent will be/is docked to and
     * the Mode is of 'editor' kind (i.e. holds editor windows).
     * @since 6.13
     */
    public boolean isEditorTopComponent( TopComponent tc ) {
        return false;
    }
    
    /**
     * <p>Check whether the given TopComponent is opened and docked into an 'editor' Mode. 
     * It is safe to call this method outside the event dispatch thread.</p>
     * <p>Please note that some TopComponents may be docked into 'editor' modes as well as 
     * 'view' modes, see method isTopComponentAllowedToMoveAnywhere().</p>
     * 
     * @param tc TopComponent to check.
     * @return True if the TopComponent is opened and the Mode it is docked 
     * is of 'editor' kind (i.e. holds editor windows).
     * @since 6.16
     */
    public boolean isOpenedEditorTopComponent( TopComponent tc ) {
        return false;
    }

    /**
     * Convenience method to retrieve the list of all opened TopComponents for
     * given mode.
     * @param mode Mode to get the list of TopComponents from.
     * @return Array of TopComponents that are opened in given mode.
     * @since 6.28
     */
    public TopComponent[] getOpenedTopComponents( Mode mode ) {
        TopComponent[] allTcs = mode.getTopComponents();
        List<TopComponent> openedTcs = new ArrayList<TopComponent>(allTcs.length);
        for( TopComponent tc : allTcs ) {
            if( tc.isOpened() ) {
                openedTcs.add(tc);
            }
        }
        return openedTcs.toArray(new TopComponent[0]);
    }

    /**
     * <p>Check whether the given Mode holds editor windows.</p>
     * <p>Please note that some TopComponents may be docked into 'editor' modes as well as 
     * 'view' modes, see method isTopComponentAllowedToMoveAnywhere().</p>
     * 
     * @param mode Mode to check.
     * @return True the Mode contains editor windows.
     * @since 6.13
     */
    public boolean isEditorMode( Mode mode ) {
        return false;
    }

    private TopComponent getActiveComponent() {
        return activeComponent.get();
    }

    private void setActiveComponent(TopComponent activeComponent) {
        this.activeComponent = new WeakReference<TopComponent>(activeComponent);
    }
    
    /**
     * Register window system listener to receive notifications when the window
     * system loads or saves.
     * @since 6.43
     */
    public void addWindowSystemListener( WindowSystemListener listener ) {
    }
    
    /**
     * Remove window system listener.
     * @since 6.43
     */
    public void removeWindowSystemListener( WindowSystemListener listener ) {
    }
    
    /**
     * <p>Switches the window system to a new role (perspective).</p> 
     * <p>A role may customize the default window layout by adding/removing TopComponents,
     * changing window positions and/or sizes etc. Roles are defined in XML
     * layers in folder <code>Window2/Roles</code>. Each role has a unique name corresponding
     * to a sub-folder in <code>Window2/Roles</code>. The content of a role sub-folder has
     * the same syntax and meaning as the default window layout in <code>Windows2</code> folder.
     * The content of role sub-folder is merged with the defaults in Windows2 folder
     * when the window system loads. User's changes to the window layout are persisted
     * per role. So user's customizations in role <b>A</b> are not propagated to
     * role <b>B</b> and vice versa.</p> 
     * <p>The default implementation of this method in core.windows module does
     * the following:
     * <ol>
     * <li>Hide the main window.</li>
     * <li>Save the current window layout of the current role.</li>
     * <li>Load new window layout from the given role.</li>
     * <li>Show the main window.</li>
     * </ol>
     * The whole operation may take a few seconds to complete.
     * <p>Windows that were opened in previous role but are not opened or present 
     * in the new role will receive <code>TopComponent.componentClosed()</code> notification.
     * <p>Note: To keep the main window showing while switching the role, use the following branding:
     * <code>org.netbeans.core.windows.WinSys.Show.Hide.MainWindow.While.Switching.Role=false</code>
     * <p>Note: Action <code>org.netbeans.core.windows.actions.SwitchRoleKeepDocumentsAction</code>
     * in <code>Actions/Window</code> layer folder can change the role too but it
     * also attempts to keep documents from previous role opened in the new role as well.
     * The new role name is <code>ActionEvent.getActionCommand()</code> argument.
     * <p>If the window system has been already loaded then the method must be
     * called from EDT thread, otherwise it's safe to set the initial startup
     * role in e.g. <code>ModuleInstall.restored()</code> method.
     * @param roleName Name of the new role to switch to or null to switch
     * to the default window layout.
     * @since 6.43
     * @see #getRole() 
     */
    public void setRole( String roleName ) {
    }
    

    /**
     * Checks the minimized status of given TopComponent.
     * @param tc
     * @return True if the given TopComponent is minimized (slided-out), false
     * otherwise.
     * @since 6.57
     */
    public boolean isTopComponentMinimized( TopComponent tc ) {
        return false;
    }

    /**
     * Minimizes the given TopComponent.
     * @param tc
     * @param minimize True to minimize (slide-out) given TopComponent, false
     * to pin it back to the main window.
     * @since 6.57
     */
    public void setTopComponentMinimized(  TopComponent tc, boolean minimize ) {
    }

    /**
     * Checks the floating status of given TopComponent.
     * @return True if the given TopComponent is separated from the main window.
     * @since 6.57
     */
    public boolean isTopComponentFloating( TopComponent tc ) {
        return false;
    }

    /**
     * Floats the given TopComponent or docks it back to the main window.
     * @param tc
     * @param floating True to separate the given TopComponent from the main window,
     * false to dock it back to the main window.
     * @since 6.57
     */
    public void setTopComponentFloating(  TopComponent tc, boolean floating ) {
    }

    /**
     * @return The name of the current role or null if the default window system
     * layout is being used.
     * @since 6.43
     * @see #setRole(java.lang.String) 
     */
    public String getRole() {
        return null;
    }
    
    /**
     * Given some XML, attempts to create a Mode that can
     * subsequently be used to dock a TopComponent into.
     * Usually this will be an anonymous Mode.
     * 
     * @param xml ModeConfig XML that was originally produced by {@link ModeUtilities#toXml}
     * @return an instance of Mode or null if the attempt to create the Mode failed
     * @see ModeUtilities
     */
    public Mode createModeFromXml(String xml) {
        return null;
    }

    /**
     * Before restoring a whole bunch of Modes (for example with XML that has been
     * previously saved somewhere and now loaded), it is useful to remove the
     * anonymous modes from the system.
     * 
     * @param mode the {@link Mode} to be removed
     * @return success or failure of the attempt to remove the {@link Mode}
     */
    public boolean removeMode(Mode mode) {
        return false;
    }

    /**
     * Before restoring anonymous Modes, it is useful to update whatever defined Modes
     * may exist like editor, explorer etc., so that all the Modes will eventually
     * re-appear in the desired locations.
     * 
     * @param xml ModeConfig XML that was originally produced by {@link ModeUtilities#toXml}
     * @return success or failure of the attempt to find the Mode and update it
     * @see ModeUtilities
     */
    public boolean updateModeConstraintsFromXml(String xml) {
        return false;
    }
            
    /** A manager that handles operations on top components.
     * It is always attached to a {@link TopComponent}.
     * @deprecated Do not use anymore. This interface is replaced by bunch of protected methods
     * which name starts with topComponent prefix, i.e. {@link #topComponentOpen}, {@link #topComponentClose} etc. */
    @SuppressWarnings("deprecation")
    @Deprecated
    protected interface Component extends java.io.Serializable {
        /**
         * Do not use.
         * @deprecated Only public by accident.
         */
        @Deprecated
        /* public static final */ long serialVersionUID = 0L;

        /** Open the component on current workspace */
        public void open();

        /**
         * Opens this component on a given workspace.
         * @param workspace the workspace on which to open it
         */
        public void open(Workspace workspace);

        /**
         * Closes this component on a given workspace.
         * @param workspace the workspace on which to close it
         */
        public void close(Workspace workspace);

        /** Called when the component requests focus. Moves it to be visible.
        */
        public void requestFocus();

        /** Set this component visible but not selected or focused if possible.
        * If focus is in other container (multitab) or other pane (split) in
        * the same container it makes this component only visible eg. it selects
        * tab with this component.
        * If focus is in the same container (multitab) or in the same pane (split)
        * it has the same effect as requestFocus().
        */
        public void requestVisible();

        /** Get the set of activated nodes.
        * @return currently activated nodes for this component
        */
        public Node[] getActivatedNodes();

        /** Set the set of activated nodes for this component.
        * @param nodes new set of activated nodes
        */
        public void setActivatedNodes(Node[] nodes);

        /** Called when the name of the top component changes.
        */
        public void nameChanged();

        /** Set the icon of the top component.
        * @param icon the new icon
        */
        public void setIcon(final Image icon);

        /**
         * Gets the icon associated with this component.
         * @return the icon
         */
        public Image getIcon();

        /**
         * Gets a list of workspaces where this component is currently open.
         * @return the set of workspaces where the managed component is open
         */
        public Set<Workspace> whereOpened();
    }
}
