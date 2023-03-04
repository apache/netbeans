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


package org.netbeans.core.windows.view.ui;


import org.netbeans.swing.tabcontrol.customtabs.Tabbed;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.actions.ActionUtils;
import org.netbeans.core.windows.actions.MaximizeWindowAction;
import org.netbeans.core.windows.view.ModeView;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.ui.slides.SlideOperation;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;
import org.openide.windows.TopComponent;
import org.openide.util.Utilities;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.AWTEventListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.Switches;
import org.netbeans.core.windows.view.ui.slides.SlideBar;
import org.netbeans.core.windows.view.ui.slides.SlideBarActionEvent;
import org.netbeans.core.windows.view.ui.slides.SlideOperationFactory;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;


/** Helper class which handles <code>Tabbed</code> component inside
 * <code>ModeComponent</code>.
 *
 * @author  Peter Zavadsky
 */
public final class TabbedHandler implements ChangeListener, ActionListener {

    /** Associated mode container. */
    private final ModeView modeView;
    
    /** Component which plays tabbed. */
    private final Tabbed tabbed;
    /** kind of the mode view we are handling tabs for */
    private final int kind;

    /** Ignore own changes. */
    private boolean ignoreChange = false;

    private static ActivationManager activationManager = null;

    /** Creates new SimpleContainerImpl */
    public TabbedHandler(ModeView modeView, int kind, Tabbed tbd) {
        this.modeView = modeView;
        this.kind = kind;

        synchronized (TabbedHandler.class) {
            if (activationManager == null) {
                activationManager = new ActivationManager();
                Toolkit.getDefaultToolkit().addAWTEventListener(
                    activationManager, AWTEvent.MOUSE_EVENT_MASK);
                KeyboardFocusManager.getCurrentKeyboardFocusManager()
                    .addPropertyChangeListener("focusOwner", activationManager);
            }
        }
        tabbed = tbd;
        tabbed.addChangeListener(this);
        tabbed.addActionListener(this);
//        tabbed = createTabbedComponent(kind);

        // E.g. when switching tabs in mode.
        ((Container)tabbed.getComponent()).setFocusCycleRoot(true);
    }

    
//    /** Gets tabbed container on supplied position */
//    private Tabbed createTabbedComponent(int kind) {
//        Tabbed tabbed;
//
//        if(kind == Constants.MODE_KIND_EDITOR) {
//            tabbed = new TabbedAdapter(Constants.MODE_KIND_EDITOR);
//        } else if (kind == Constants.MODE_KIND_SLIDING) {
//            tabbed = new TabbedSlideAdapter(((SlidingView)modeView).getSide());
//        } else {
//            tabbed = new TabbedAdapter(Constants.MODE_KIND_VIEW);
//        }
//        
//        tabbed.addChangeListener(this);
//        tabbed.addActionListener(this);
//
//        return tabbed;
//    }
    
    public void requestAttention (TopComponent tc) {
        tabbed.requestAttention(tc);
    }

    public void cancelRequestAttention (TopComponent tc) {
        tabbed.cancelRequestAttention(tc);
    }

    public void setAttentionHighlight (TopComponent tc, boolean highlight) {
        tabbed.setAttentionHighlight(tc, highlight);
    }

    public void makeBusy( TopComponent tc, boolean busy ) {
        tabbed.makeBusy( tc, busy );
    }
    
    public Component getComponent() {
        return tabbed.getComponent();
    }

    /** Adds given top component to this container. */
    public void addTopComponent(TopComponent tc, int kind) {
        addTCIntoTab(tc, kind);
    }
    

    public void setTopComponents(TopComponent[] tcs, TopComponent selected) {
        ignoreChange = true;
        try {
            tabbed.setTopComponents(tcs, selected);
        } finally {
            ignoreChange = false;
        }
    }
    
    /** Adds TopComponent into specified tab. */
    private void addTCIntoTab(TopComponent tc, int kind) {
        
        if(containsTC(tabbed, tc)) {
            return;
        }

        Image icon = tc.getIcon();
        
        try {
            ignoreChange = true;
            String title = WindowManagerImpl.getInstance().getTopComponentDisplayName(tc);
            if(title == null) {
                title = ""; // NOI18N
            }
            tabbed.addTopComponent(
                title,
                icon == null ? null : ImageUtilities.image2Icon(icon),
                tc, tc.getToolTipText());
        } finally {
            ignoreChange = false;
        }
    }

    /** Checks whether the tabbedPane already contains the component. */
    private static boolean containsTC(Tabbed tabbed, TopComponent tc) {
        return tabbed.indexOf(tc) != -1;
    }
    
    /** Removes TopComponent from this container. */
    public void removeTopComponent(TopComponent tc) {
        removeTCFromTab(tc); 
    }

    /** Removes TC from tab. */
    private void removeTCFromTab (TopComponent tc) {
        if(tabbed.indexOf(tc) != -1) {
            try {
                ignoreChange = true;
                tabbed.removeComponent(tc);
            } finally {
                ignoreChange = false;
            }

            //Bugfix #27644: Remove reference from TopComponent's accessible context
            //to our tabbed pane.
            tc.getAccessibleContext().setAccessibleParent(null);
        }
    }
    
    /** Called when icon of some component in this multi frame has changed  */
    public void topComponentIconChanged(TopComponent tc) {
        int index = tabbed.indexOf(tc);
        if (index < 0) {
            return;
        }

        Image icon = tc.getIcon();
        if( null != icon ) {
            tabbed.setIconAt(index, ImageUtilities.image2Icon(tc.getIcon()));
        } else {
            Logger.getLogger(TabbedHandler.class.getName()).log(Level.INFO, "TopComponent has no icon: " + tc);
            tabbed.setIconAt(index, null);
        }
    }
    
    /** Called when the name of some component has changed  */
    public void topComponentNameChanged(TopComponent tc, int kind) {
        int index = tabbed.indexOf(tc);
        if (index < 0) {
            return;
        }
        
        String title = WindowManagerImpl.getInstance().getTopComponentDisplayName(tc);
        if(title == null) {
            title = ""; // NOI18N
        }
        tabbed.setTitleAt (index, title);
    }
    
    public void topComponentToolTipChanged(TopComponent tc) {
        int index = tabbed.indexOf(tc);
        if (index < 0) {
            return;
        }
        
        tabbed.setToolTipTextAt(index, tc.getToolTipText());
    }
    
    /** Sets selected <code>TopComponent</code>.
     * Ensures GUI components to set accordingly. */
    public void setSelectedTopComponent(TopComponent tc) {
        if(tc == getSelectedTopComponent()) {
            return;
        }
        if (tc == null && !isNullSelectionAllowed()) {
            return;
        }
        
        if(tabbed.indexOf(tc) >= 0 || (isNullSelectionAllowed() && tc == null)) {
            try {
                ignoreChange = true;
                tabbed.setSelectedComponent(tc);
            } finally {
                ignoreChange = false;
            }
        }
    }
    
    private boolean isNullSelectionAllowed() {
        return kind == Constants.MODE_KIND_SLIDING;
    }
    
    public TopComponent getSelectedTopComponent() {
        return tabbed.getSelectedTopComponent();
    }

    public TopComponent[] getTopComponents() {
        return tabbed.getTopComponents();
    }

    public void setActive(boolean active) {
        tabbed.setActive(active);
    }
    
    ///////////////////
    // ChangeListener
    @Override
    public void stateChanged(ChangeEvent evt) {
        if(ignoreChange || evt.getSource() != tabbed) {
            return;
        }
        TopComponent selected = tabbed.getSelectedTopComponent();
        modeView.getController().userSelectedTab(modeView, (TopComponent)selected);
    }
    
    // DnD>>
    public Shape getIndicationForLocation(Point location, TopComponent startingTransfer,
    Point startingPoint, boolean attachingPossible) {
        return tabbed.getIndicationForLocation(location, startingTransfer,
                                            startingPoint, attachingPossible);
    }
    
    public Object getConstraintForLocation(Point location, boolean attachingPossible) {
        return tabbed.getConstraintForLocation(location, attachingPossible);
    }
    
    // Sliding
    public Rectangle getTabBounds(int tabIndex) {
        return tabbed.getTabBounds(tabIndex);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e instanceof TabActionEvent) {
            TabActionEvent tae = (TabActionEvent) e;
            String cmd = tae.getActionCommand();
            if (TabbedContainer.COMMAND_SELECT.equals(cmd)) {
                return;
            }
            tae.consume();
            if (TabbedContainer.COMMAND_CLOSE == cmd) { //== test is safe here
                TopComponent tc = tabbed.getTopComponentAt(tae.getTabIndex());
                if (tc != null && modeView != null) {
                    modeView.getController().userClosedTopComponent(modeView, tc);
                } else {
                    Logger.getLogger(TabbedHandler.class.getName()).warning(
                        "TopComponent to be closed is null at index " + tae.getTabIndex());
                }
            } else if (TabbedContainer.COMMAND_POPUP_REQUEST == cmd) {
                handlePopupMenuShowing(tae.getMouseEvent(), tae.getTabIndex());
            } else if (TabbedContainer.COMMAND_MAXIMIZE == cmd) {
                handleMaximization(tae);
            } else if (TabbedContainer.COMMAND_CLOSE_ALL == cmd) {
                ActionUtils.closeAllDocuments(true);
            } else if (TabbedContainer.COMMAND_CLOSE_ALL_BUT_THIS == cmd) {
                TopComponent tc = tabbed.getTopComponentAt(tae.getTabIndex());
                ActionUtils.closeAllExcept(tc, true);
            //Pin button handling here
            } else if (TabbedContainer.COMMAND_ENABLE_AUTO_HIDE.equals(cmd)) {
                if( Switches.isTopComponentSlidingEnabled() && tabbed.getComponent().isShowing() ) {
                    TopComponent tc = tabbed.getTopComponentAt(tae.getTabIndex());
                    // prepare slide operation
                    Component tabbedComp = tabbed.getComponent();

                    String side = WindowManagerImpl.getInstance().guessSlideSide(tc);
                    SlideOperation operation = SlideOperationFactory.createSlideIntoEdge(
                        tabbedComp, side, true);
                    operation.setStartBounds(
                           new Rectangle(tabbedComp.getLocationOnScreen(), tabbedComp.getSize()));
                    operation.prepareEffect();

                    modeView.getController().userEnabledAutoHide(modeView, tc);
                    modeView.getController().userTriggeredSlideIntoEdge(modeView, operation);
                }
            } else if (TabbedContainer.COMMAND_MINIMIZE_GROUP.equals(cmd)) {
                if( Switches.isModeSlidingEnabled() ) {
                    TopComponent tc = tabbed.getTopComponentAt(0);
                    WindowManagerImpl wm = WindowManagerImpl.getInstance();
                    ModeImpl mode = ( ModeImpl ) wm.findMode( tc );
                    if( null != mode ) {
                        wm.userMinimizedMode( mode );
                    }
                }
            } else if (TabbedContainer.COMMAND_RESTORE_GROUP.equals(cmd)) {
                String nameOfModeToRestore = tae.getGroupName();
                if( null != nameOfModeToRestore ) {
                    TopComponent tc = tabbed.getTopComponentAt(0);
                    WindowManagerImpl wm = WindowManagerImpl.getInstance();
                    ModeImpl slidingMode = ( ModeImpl ) wm.findMode( tc );
                    ModeImpl modeToRestore = ( ModeImpl ) wm.findMode( nameOfModeToRestore );
                    if( null != modeToRestore && null != slidingMode ) {
                        wm.userRestoredMode( slidingMode, modeToRestore );
                    }
                }
            } else if (TabbedContainer.COMMAND_CLOSE_GROUP.equals(cmd)) {
                if( Switches.isModeClosingEnabled() ) {
                    TopComponent tc = tabbed.getTopComponentAt(0);
                    WindowManagerImpl wm = WindowManagerImpl.getInstance();
                    ModeImpl mode = ( ModeImpl ) wm.findMode( tc );
                    if( null != mode ) {
                        wm.userClosedMode( mode );
                    }
                }
            }
        } else if (e instanceof SlideBarActionEvent) {
            // slide bar commands
            SlideBarActionEvent sbe = (SlideBarActionEvent)e;
            String cmd = sbe.getActionCommand();
            if (SlideBar.COMMAND_POPUP_REQUEST.equals(cmd)) {
                handlePopupMenuShowing(sbe.getMouseEvent(), sbe.getTabIndex());
            } else if (SlideBar.COMMAND_SLIDE_IN.equals(cmd)) {
                modeView.getController().userTriggeredSlideIn(modeView, sbe.getSlideOperation());
            } else if (SlideBar.COMMAND_SLIDE_RESIZE.equals(cmd)) {
                modeView.getController().userResizedSlidingWindow(modeView, sbe.getSlideOperation());
            } else if (SlideBar.COMMAND_SLIDE_OUT.equals(cmd)) {
                // when the call comes from the change of tehmodel rather than user clicking,
                // ignore activation requests.
                // #48539
                SlideOperation op = new ProxySlideOperation(sbe.getSlideOperation(), ignoreChange);
                modeView.getController().userTriggeredSlideOut(modeView, op);
            } else if (SlideBar.COMMAND_DISABLE_AUTO_HIDE.equals(cmd)) {
                TopComponent tc = tabbed.getTopComponentAt(sbe.getTabIndex());
                modeView.getController().userDisabledAutoHide(modeView, tc);
            } else if( SlideBar.COMMAND_MAXIMIZE == cmd ) {
                TopComponent tc = tabbed.getTopComponentAt(sbe.getTabIndex());
                MaximizeWindowAction mwa = new MaximizeWindowAction(tc);
                if( mwa.isEnabled() )
                    mwa.actionPerformed(e);
            }
        }
    }

    /** Possibly invokes popup menu. */
    public static void handlePopupMenuShowing(MouseEvent e, int idx) {
        Component c = (Component) e.getSource();
        while (c != null && !(c instanceof Tabbed.Accessor))
            c = c.getParent();
        if (c == null) {
            return;
        }
        final Tabbed tab = ((Tabbed.Accessor)c).getTabbed();

        final Point p = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), c);

        final int clickTab = idx;
        Lookup context = null;
        Action[] actions = null;
        if (clickTab >= 0) {

            TopComponent tc = tab.getTopComponentAt(clickTab);
            if(tc != null) {
                // ask also tabbed to possibly alter actions
                actions = tab.getPopupActions(tc.getActions(), clickTab);
                if (actions == null) { 
                    actions = tc.getActions();
                }
                if (actions == null || actions.length == 0 )
                    return;
                context = tc.getLookup();
            }
        }
        if( null == context ) {
            actions = tab.getPopupActions(new Action[0], -1);
            if (actions == null || actions.length == 0 )
                return;
            context = Lookup.getDefault();
        }

        showPopupMenu(Utilities.actionsToPopup(actions, context), p, c);
    }

    /** Shows given popup on given coordinations and takes care about the
     * situation when menu can exceed screen limits */
    private static void showPopupMenu (JPopupMenu popup, Point p, Component comp) {
        popup.show(comp, p.x, p.y);
    }

    /** Possibly invokes the (un)maximization. */
    public static void handleMaximization(TabActionEvent tae) {
        Component c = (Component) tae.getSource();
        while (c != null && !(c instanceof Tabbed.Accessor))
            c = c.getParent();
        if (c == null) {
            return;
        }
        
        final Tabbed tab = ((Tabbed.Accessor) c).getTabbed();
        TopComponent tc = tab.getTopComponentAt(tae.getTabIndex());
        // perform action
        MaximizeWindowAction mwa = new MaximizeWindowAction(tc);
        if( mwa.isEnabled() )
            mwa.actionPerformed(tae);
    }

    /** Well, we can't totally get rid of AWT event listeners - this is what
     * keeps track of the activated mode. */
    private static class ActivationManager implements AWTEventListener, PropertyChangeListener {
        @Override
        public void eventDispatched(AWTEvent e) {
            if(e.getID() == MouseEvent.MOUSE_PRESSED) {
                handleActivation(e.getSource());
            }
        }

        /**
         * Keyboard focus change event handler. Handle situation where
         * active TopComponent was in a different window and window
         * changed without a mouse event.
         * See
         *     Editor with Keyboard focus is not active TopComponent
         *     https://github.com/apache/netbeans/issues/4437
         */
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            Frame mainWindowNB = WindowManagerImpl.getInstance().getMainWindow();
            Window activeWindowKB = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
            TopComponent currentTC = TopComponent.getRegistry().getActivated();
            // Only do something if focus to the main window and
            // active TC is not in the main window. Note that focus changes
            // to detached windows handled in DefaultSeparateContainer.
            if(mainWindowNB == activeWindowKB
                    && mainWindowNB != SwingUtilities.getRoot(currentTC)) {
                handleActivation(e.getNewValue());
            }
        }

        //
        /* XXX(-ttran) when the split container contains two TopComponents say TC1
         * and TC2.  If TC2 itself does not accept focus or the user clicks on one
         * of TC2's child compoennts which does not accept focus, then the whole
         * split container is activated.  It in turn may choose to activate TC1 not
         * TC2.  This is a very annoying problem if TC1 is an Explorer and TC2 is
         * the global property sheet.  The user clicks on the property sheet but
         * the Explorer gets activated which has a different selected node than the
         * one attached to the property sheet at that moment.  The contents of the
         * property sheet is updated immediately after the mouse click.  For more
         * details see <http://www.netbeans.org/issues/show_bug.cgi?id=11149>
         *
         * What follows here is a special hack for mouse click on _any_
         * TopComponent.  The hack will cause the nearest upper TopComponent in the
         * AWT hieararchy to be activated on MOUSE_PRESSED on any of its child
         * components.  This behavior is compatible with all window managers I can
         * imagine.
         */
        private void handleActivation(Object evtObject) {
            if (!(evtObject instanceof Component)) {
                return;
            }
            Component comp = (Component) evtObject;
            
            while (comp != null && !(comp instanceof ModeComponent)) {
                if (comp instanceof JComponent) {
                    JComponent c = (JComponent)comp;
                    // don't activate if requested
                    if (Boolean.TRUE.equals(c.getClientProperty("dontActivate"))) { //NOI18N
                        return;
                    }
                }
                if (comp instanceof TopComponent) {
                    TopComponent tc = (TopComponent)comp;
                    // special way of activation for topcomponents in sliding mode
                    if (Boolean.TRUE.equals(tc.getClientProperty("isSliding"))) { //NOI18N
                        tc.requestActive();
                        return;
                    }
                }
                comp = comp.getParent();
            }

            if (comp instanceof ModeComponent) {
                ModeComponent modeComp = (ModeComponent)comp;
                // don't activate sliding views when user clicked edge bar
                if (modeComp.getKind() != Constants.MODE_KIND_SLIDING) {
                    ModeView modeView = modeComp.getModeView();
                    modeView.getController().userActivatedModeView(modeView);
                }
            }
        }
        
    } // end of ActivationManager
    
    /**
     * proxy slide operation that disables activation reqeuest when the operation comes from the model, not really user action.
     */
    private static class ProxySlideOperation implements SlideOperation {
        
        private SlideOperation original;
        private boolean disable;
        
        public ProxySlideOperation(SlideOperation orig, boolean disableActivation) {
            original = orig;
            disable = disableActivation;
        }
        
        @Override
        public Component getComponent() {
            return original.getComponent();
        }

        @Override
        public Rectangle getFinishBounds() {
            return original.getFinishBounds();
        }

        @Override
        public String getSide() {
            return original.getSide();
        }

        @Override
        public Rectangle getStartBounds() {
            return original.getStartBounds();
        }

        @Override
        public int getType() {
            return original.getType();
        }

        @Override
        public void prepareEffect() {
            original.prepareEffect();
        }

        @Override
        public boolean requestsActivation() {
            if (disable) {
                return false;
            }
            return original.requestsActivation();
        }

        @Override
        public void run(JLayeredPane pane, Integer layer) {
            original.run(pane, layer);
        }

        @Override
        public void setFinishBounds(Rectangle bounds) {
            original.setFinishBounds(bounds);
        }

        @Override
        public void setStartBounds(Rectangle bounds) {
            original.setStartBounds(bounds);
        }
    }

}

