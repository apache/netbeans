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


package org.netbeans.core.windows.actions;


import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import org.netbeans.core.windows.*;
import org.netbeans.core.windows.view.ui.slides.SlideController;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.cookies.SaveCookie;
import org.openide.util.*;
import org.openide.util.actions.Presenter;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;


/**
 * Utility class for creating contextual actions for window system
 * and window action handlers.
 *
 * @author  Peter Zavadsky
 */
public abstract class ActionUtils {
    
    private static HashMap<Object, Object> sharedAccelerators = new HashMap<Object, Object>();

    private ActionUtils() {}
    
    public static Action[] createDefaultPopupActions(TopComponent tc) {
        ModeImpl mode = findMode(tc);
        int kind = mode != null ? mode.getKind() : Constants.MODE_KIND_EDITOR;
        TopComponentTracker tcTracker = TopComponentTracker.getDefault();
        boolean isEditor = tcTracker.isEditorTopComponent( tc );
        
        List<Action> actions = new ArrayList<Action>();
        if(kind == Constants.MODE_KIND_EDITOR) {
            //close window
            if( Switches.isClosingEnabled(tc)) {
                if( (isEditor && Switches.isEditorTopComponentClosingEnabled()) 
                        || (!isEditor && Switches.isViewTopComponentClosingEnabled()) ) {
                    actions.add(new CloseWindowAction(tc));
                }
            }
            if( Switches.isEditorTopComponentClosingEnabled() ) {
                //close all
                actions.add(new CloseAllDocumentsAction(true));
                //close all but this
                CloseAllButThisAction allBut = new CloseAllButThisAction(tc, true);
                if (mode != null && mode.getOpenedTopComponents().size() == 1) {
                    allBut.setEnabled(false);
                }
                actions.add(allBut);
            }
            
            actions.add(null); // Separator
            
            //maximize window
            if( Switches.isTopComponentMaximizationEnabled() && Switches.isMaximizationEnabled(tc)) {
                actions.add(new MaximizeWindowAction(tc));
            }
            //undock window
            if( Switches.isTopComponentUndockingEnabled() && Switches.isUndockingEnabled(tc)) {
                actions.add(new UndockWindowAction(tc));
            }
            //undock group
            if( Switches.isEditorModeUndockingEnabled() && isEditor )
                actions.add( new UndockModeAction( mode) );
            //dock window
            if( Switches.isTopComponentUndockingEnabled() && Switches.isUndockingEnabled(tc)) {
                actions.add(new DockWindowAction(tc));
            }
            //dock group
            if( Switches.isEditorModeUndockingEnabled() && isEditor )
                actions.add( new DockModeAction( mode, null ) );

            //move window left
            actions.add( MoveWindowWithinModeAction.createMoveLeft(tc) );
            //move window right
            actions.add( MoveWindowWithinModeAction.createMoveRight(tc));

            if( isEditor ) {
                actions.add( null ); // Separator

                actions.add(new CloneDocumentAction(tc));

                actions.add(new NewTabGroupAction(tc));
                actions.add( new CollapseTabGroupAction( mode ) );
            }
        } else if (kind == Constants.MODE_KIND_VIEW) {
            //close window
            if( Switches.isClosingEnabled(tc)) {
                if( (isEditor && Switches.isEditorTopComponentClosingEnabled()) 
                        || (!isEditor && Switches.isViewTopComponentClosingEnabled()) ) {
                    actions.add(new CloseWindowAction(tc));
                }
            }
            //close group
            if( Switches.isModeClosingEnabled() ) {
                actions.add(new CloseModeAction(mode));
            }
            
            actions.add( null ); //separator
            
            //maximize window
            if (Switches.isTopComponentMaximizationEnabled()
                    && Switches.isMaximizationEnabled(tc)) {
                actions.add(new MaximizeWindowAction(tc));
            }
            
            //minimize window
            if( Switches.isTopComponentSlidingEnabled() && Switches.isSlidingEnabled( tc ) )
                actions.add( createMinimizeWindowAction( tc ) );
            
            //minimize group
            if( Switches.isModeSlidingEnabled() )
                actions.add( new MinimizeModeAction( mode) );

            //undock window
            if( Switches.isTopComponentUndockingEnabled() && Switches.isUndockingEnabled(tc)) {
                actions.add(new UndockWindowAction(tc));
            }
            //undock group
            if( Switches.isViewModeUndockingEnabled() )
                actions.add( new UndockModeAction( mode) );
        
            //dock window
            if( Switches.isTopComponentUndockingEnabled() && Switches.isUndockingEnabled(tc)) {
                actions.add(new DockWindowAction(tc));
            }
            //dock group
            if( Switches.isViewModeUndockingEnabled() )
                actions.add( new DockModeAction( mode, null ) );
            
            actions.add( null ); // Separator

            //move window
            actions.add( new MoveWindowAction( tc ) );

            //move window left
            actions.add( MoveWindowWithinModeAction.createMoveLeft(tc) );
            //move window right
            actions.add( MoveWindowWithinModeAction.createMoveRight(tc));
            
            //move group
            actions.add( new MoveModeAction( mode) );
            
            //size group
            actions.add( new ResizeModeAction( mode) );
            
            if( isEditor ) {
                actions.add( null ); // Separator
                actions.add(new CloneDocumentAction(tc));
            }

        } else if (kind == Constants.MODE_KIND_SLIDING) {
            //close window
            if( Switches.isClosingEnabled(tc)) {
                if( (isEditor && Switches.isEditorTopComponentClosingEnabled()) 
                        || (!isEditor && Switches.isViewTopComponentClosingEnabled()) ) {
                    actions.add(new CloseWindowAction(tc));
                }
            }
            //close group
            if( Switches.isModeClosingEnabled() ) {
                actions.add(new CloseModeAction(mode));
            }
            
            actions.add( null ); //separator
            
            //maximize window
            if (Switches.isTopComponentMaximizationEnabled()
                    && Switches.isMaximizationEnabled(tc)) {
                actions.add(new MaximizeWindowAction(tc));
            }
            
            //minimize window
            if( Switches.isTopComponentSlidingEnabled() && Switches.isSlidingEnabled( tc ) )
                actions.add(createDisabledAction("CTL_MinimizeWindowAction"));
            
            //minimize group
            if( Switches.isModeSlidingEnabled() )
                actions.add(createDisabledAction("CTL_MinimizeModeAction"));

            //undock window
            if( Switches.isTopComponentUndockingEnabled() && Switches.isUndockingEnabled(tc)) {
                actions.add(new UndockWindowAction(tc));
            }
            //undock group
            if( Switches.isViewModeUndockingEnabled() )
                actions.add(createDisabledAction("CTL_UndockModeAction"));
        
            //dock window
            if( Switches.isTopComponentUndockingEnabled() && Switches.isUndockingEnabled(tc)) {
                actions.add(new DockWindowAction(tc));
            }
            //dock group
            if( Switches.isViewModeUndockingEnabled() || Switches.isModeSlidingEnabled() )
                actions.add( new DockModeAction( findPreviousMode( tc, mode ), mode) );

            actions.add( null ); // Separator
            
            //move window
            actions.add(createDisabledAction("CTL_MoveWindowAction"));
            
            //move group
            actions.add(createDisabledAction("CTL_MoveModeAction"));
            
            //size group
            actions.add(createDisabledAction("CTL_ResizeModeAction"));

            if( isEditor ) {
                actions.add( null ); // Separator

                actions.add(new CloneDocumentAction(tc));
            }
        }
        
        Action[] res = actions.toArray(new Action[0]);
        for( ActionsFactory factory : Lookup.getDefault().lookupAll( ActionsFactory.class ) ) {
            res = factory.createPopupActions( tc, res );
        }
        return res;
    }
    
    public static Action[] createDefaultPopupActions(ModeImpl mode) {
        int kind = mode != null ? mode.getKind() : Constants.MODE_KIND_EDITOR;
        
        List<Action> actions = new ArrayList<Action>();
        if(kind == Constants.MODE_KIND_EDITOR) {
            if( Switches.isEditorTopComponentClosingEnabled() ) {
                actions.add(createDisabledAction("CTL_CloseWindowAction"));
                actions.add(new CloseAllDocumentsAction(true));
                actions.add(createDisabledAction("CTL_CloseAllButThisAction")); //NOI18N
                actions.add(null); // Separator
            }
            
            actions.add(null); // Separator

            //maximize window
            if( Switches.isTopComponentMaximizationEnabled() ) {
                actions.add(createDisabledAction("CTL_MaximizeWindowAction"));
            }
            //float window
            if( Switches.isTopComponentUndockingEnabled()) {
                actions.add(createDisabledAction("CTL_UndockWindowAction"));
            }
            //float group
            if( Switches.isEditorModeUndockingEnabled()) {
                actions.add(new UndockModeAction(mode));
            }
            //dock window
            if( Switches.isTopComponentUndockingEnabled()) {
                actions.add(createDisabledAction("CTL_UndockWindowAction_Dock"));
            }
            //dock group
            if( Switches.isEditorModeUndockingEnabled() )
                actions.add( new DockModeAction( mode, null ) );

            actions.add(null); // Separator
            
            actions.add(createDisabledAction("CTL_CloneDocumentAction"));
            actions.add(createDisabledAction("CTL_NewTabGroupAction"));
            actions.add( new CollapseTabGroupAction( mode ) );
        } else if (kind == Constants.MODE_KIND_VIEW) {
            //close window
            if( Switches.isViewTopComponentClosingEnabled() ) {
                actions.add(createDisabledAction("CTL_CloseWindowAction"));
            }
            //close group
            if( Switches.isModeClosingEnabled() ) {
                actions.add(new CloseModeAction(mode));
            }
            
            actions.add( null ); //separator
            
            // maximize window
            if (Switches.isTopComponentMaximizationEnabled() ) {
                actions.add(createDisabledAction("CTL_MaximizeWindowAction"));
            }
            //minimize window
            if (Switches.isTopComponentSlidingEnabled() ) {
                actions.add(createDisabledAction("LBL_AutoHideWindowAction"));
            }
            //minimize group
            if( Switches.isModeSlidingEnabled() )
                actions.add( new MinimizeModeAction( mode) );
            //float window
            if( Switches.isTopComponentUndockingEnabled() ) {
                actions.add(createDisabledAction("CTL_UndockWindowAction"));
            }
            //float group
            if( Switches.isViewModeUndockingEnabled() )
                actions.add( new UndockModeAction( mode) );
            //dock window
            if( Switches.isTopComponentUndockingEnabled()) {
                actions.add(createDisabledAction("CTL_UndockWindowAction_Dock"));
            }
            //dock group
            if( Switches.isViewModeUndockingEnabled() )
                actions.add( new DockModeAction( mode, null ) );
            
            actions.add( null );
            
            //move window
            actions.add(createDisabledAction("CTL_MoveWindowAction"));
            
            //move group
            actions.add( new MoveModeAction( mode ) );
            
            //size group
            actions.add( new ResizeModeAction( mode ) );
            
        } else if (kind == Constants.MODE_KIND_SLIDING) {
            if( Switches.isViewTopComponentClosingEnabled() ) {
                actions.add(createDisabledAction("CTL_CloseWindowAction"));
                actions.add(new CloseModeAction(mode));
            }
            if (mode.getState() == Constants.MODE_STATE_JOINED
                    && Switches.isTopComponentMaximizationEnabled()) {
                actions.add(createDisabledAction("CTL_MaximizeWindowAction"));
            }
            if( Switches.isTopComponentUndockingEnabled() ) {
                actions.add(createDisabledAction("CTL_UndockWindowAction"));
            }
        }
        
        Action[] res = actions.toArray(new Action[0]);
        for( ActionsFactory factory : Lookup.getDefault().lookupAll( ActionsFactory.class ) ) {
            res = factory.createPopupActions( mode, res );
        }
        return res;
    }

    static Action createMinimizeWindowAction( TopComponent tc ) {
        SlideController slideController = ( SlideController ) SwingUtilities.getAncestorOfClass( SlideController.class, tc );
        ModeImpl mode = findMode( tc );
        int tabIndex = null == mode ? -1 : mode.getOpenedTopComponents().indexOf( tc );
        boolean initialState = WindowManagerImpl.getInstance().isTopComponentMinimized( tc );
        Action res = new AutoHideWindowAction( slideController, tabIndex, initialState );
        res.setEnabled( null != mode && mode.getState() == Constants.MODE_STATE_JOINED );
        return res;
    }
    
    /** Auto-hide toggle action */
    public static final class AutoHideWindowAction extends AbstractAction implements Presenter.Popup {
        
        private final SlideController slideController;
        
        private final int tabIndex;
        
        private boolean state;
        
        private JCheckBoxMenuItem menuItem;
        
        public AutoHideWindowAction(SlideController slideController, int tabIndex, boolean initialState) {
            super();
            this.slideController = slideController;
            this.tabIndex = tabIndex;
            this.state = initialState;
            putValue(Action.NAME, NbBundle.getMessage(ActionUtils.class, "LBL_AutoHideWindowAction"));
        }
        
        public HelpCtx getHelpCtx() {
            return null;
        }
        
        /** Chnage boolean state and delegate event to winsys through
         * SlideController (implemented by SlideBar component)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            // update state and menu item
            state = !state;
            getMenuItem().setSelected(state);
            // send event to winsys
            slideController.userToggledAutoHide(tabIndex, state);
        }

        @Override
        public JMenuItem getPopupPresenter() {
            return getMenuItem();
        }
        
        private JCheckBoxMenuItem getMenuItem() {
            if (menuItem == null) {
                menuItem = new JCheckBoxMenuItem("", state);
                Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
                //#45940 - hardwiring the shortcut UI since the actual shortcut processignb is also
                // hardwired in AbstractTabViewDisplayerUI class.
                // later this should be probably made customizable?
                // -> how to get rid of the parameters passed to the action here then?
                menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.CTRL_DOWN_MASK));
                menuItem.addActionListener(this);
                menuItem.setEnabled(isEnabled());
            }
            return menuItem;
        }
        
    } // End of class AutoHideWindowAction

    /**
     * Toggle transparency of slided-in window
     */
    public static final class ToggleWindowTransparencyAction extends AbstractAction {
        
        public ToggleWindowTransparencyAction(SlideController slideController, int tabIndex, boolean initialState) {
            super();
            putValue(Action.NAME, NbBundle.getMessage(ActionUtils.class, "LBL_ToggleWindowTransparencyAction")); //NOI18N
        }
        
        public HelpCtx getHelpCtx() {
            return null;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(
                    NbBundle.getMessage(ActionUtils.class, "LBL_WindowTransparencyHint"), NotifyDescriptor.INFORMATION_MESSAGE)); //NOI18N
        }
    } // End of class ToggleWindowTransparencyAction

    private static class CloneDocumentAction extends AbstractAction {
        private final TopComponent tc;
        public CloneDocumentAction(TopComponent tc) {
            this.tc = tc;
            putValue(Action.NAME, NbBundle.getMessage(ActionUtils.class, "LBL_CloneDocumentAction"));
            //hack to insert extra actions into JDev's popup menu
            putValue("_nb_action_id_", "clone"); //NOI18N
            setEnabled(tc instanceof TopComponent.Cloneable);
        }
        
        @Override
        public void actionPerformed(ActionEvent evt) {
            cloneWindow(tc);
        }
        
    } // End of class CloneDocumentAction.
    
    // Utility methods >>
    /** Closes all documents, based on isContext flag
     * 
     * @param isContext when true, closes all documents in active mode only,
     * otherwise closes all documents in the system
     */
    public static void closeAllDocuments (boolean isContext) {
        /* Historically, the closeAll method wrapped calls to TopComponent.close() in an
        invokeLater, so keep doing that. This is probably a good idea e.g. for cases where the
        caller is a button press handler and where TopComponent.close() ends up showing a modal
        dialog (which could perhaps delay the update of the button's visual state). */
        SwingUtilities.invokeLater(() -> {
            closeAll(isContext
                ? getOpened(TopComponent.getRegistry().getActivated())
                : Arrays.asList(WindowManagerImpl.getInstance().getEditorTopComponents()));
        });
    }

    /** Closes all documents except given param, according to isContext flag
     * 
     * @param isContext when true, closes all documents except given 
     * in active mode only, otherwise closes all documents in the system except
     * given
     */
    public static void closeAllExcept (TopComponent tc, boolean isContext) {
        // See closeAllDocuments.
        SwingUtilities.invokeLater(() -> {
            List<TopComponent> tcs = new ArrayList<>(isContext
                ? getOpened(tc)
                : Arrays.asList(WindowManagerImpl.getInstance().getEditorTopComponents()));
            tcs.remove(tc);
            closeAll(tcs);
        });
    }

    private static void closeAll( Iterable<TopComponent> tcs ) {
        for (TopComponent curTC : tcs) {
            if( !Switches.isClosingEnabled(curTC) ) {
                continue;
            }
            curTC.putClientProperty("inCloseAll", Boolean.TRUE);
            if (!curTC.close()) {
                break;
            }
        }
    }

    /** Returns List of opened top components in mode of given TopComponent.
     */
    private static List<TopComponent> getOpened (TopComponent tc) {
        ModeImpl mode = findMode(tc);
        List<TopComponent> tcs = new ArrayList<TopComponent>();
        if (mode != null) {
                tcs.addAll(mode.getOpenedTopComponents());
        }
        return tcs;
    }

    static void closeWindow(TopComponent tc) {
        tc.close();
    }
    
    private static void saveDocument(TopComponent tc) {
        SaveCookie sc = getSaveCookie(tc);
        if(sc != null) {
            try {
                sc.save();
            } catch(IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }
    
    private static SaveCookie getSaveCookie(TopComponent tc) {
        Lookup lookup = tc.getLookup();
        Object obj = lookup.lookup(SaveCookie.class);
        if(obj instanceof SaveCookie) {
            return (SaveCookie)obj;
        }
        
        return null;
    }

    static void cloneWindow(TopComponent tc) {
        if(tc instanceof TopComponent.Cloneable) {
            TopComponent clone = ((TopComponent.Cloneable)tc).cloneComponent();
            int openIndex = -1;
            Mode m = findMode(tc);
            if( null != m ) {
                TopComponent[] tcs = m.getTopComponents();
                for( int i=0; i<tcs.length; i++ ) {
                    if( tcs[i] == tc ) {
                        openIndex = i + 1;
                        break;
                    }
                }
                if( openIndex >= tcs.length )
                    openIndex = -1;
            }
            if( openIndex >= 0 ) {
                clone.openAtTabPosition(openIndex);
            } else {
                clone.open();
            }
            clone.requestActive();
        }
    }
    
    static void putSharedAccelerator (Object key, Object value) {
        sharedAccelerators.put(key, value);
    }
    
    static Object getSharedAccelerator (Object key) {
        return sharedAccelerators.get(key);
    }
    
    private static Action createDisabledAction( String bundleKey ) {
        return new DisabledAction( NbBundle.getMessage(ActionUtils.class, bundleKey) );
    }

    private static class DisabledAction extends AbstractAction {

        private DisabledAction( String name ) {
            super( name );
        }
        @Override
        public void actionPerformed( ActionEvent e ) {
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }
    // Utility methods <<
    
    static ModeImpl findMode( TopComponent tc ) {
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        ModeImpl mode = (ModeImpl)wm.findMode(tc);
        if( null == mode ) {
            //maybe it's multiview element
            TopComponent multiviewParent = ( TopComponent ) SwingUtilities.getAncestorOfClass( TopComponent.class, tc);
            if( null != multiviewParent )
                mode = (ModeImpl)wm.findMode(multiviewParent);
        }
        return mode;
    }
    
    private static ModeImpl findPreviousMode( TopComponent tc, ModeImpl slidingMode ) {
        ModeImpl res = null;
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        String tcId = wm.findTopComponentID( tc );
        if( null != tcId ) {
            res = wm.getPreviousModeForTopComponent( tcId, slidingMode );
        }
        return res;
    }
}

