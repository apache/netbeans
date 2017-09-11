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


package org.netbeans.core.windows.actions;


import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import org.netbeans.core.windows.*;
import org.netbeans.core.windows.view.ui.slides.SlideController;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Actions;
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
        
        Action[] res = actions.toArray(new Action[actions.size()]);
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
        
        Action[] res = actions.toArray(new Action[actions.size()]);
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
        if (isContext) {
            TopComponent activeTC = TopComponent.getRegistry().getActivated();
            List<TopComponent> tcs = getOpened(activeTC);

            closeAll( tcs.toArray(new TopComponent[tcs.size()]) );
        } else {
            TopComponent[] tcs = WindowManagerImpl.getInstance().getEditorTopComponents();
            closeAll( tcs );
        }
    }

    private static void closeAll( TopComponent[] tcs ) {
        for( TopComponent tc: tcs ) {
            if( !Switches.isClosingEnabled(tc) )
                continue;
            final TopComponent toBeClosed = tc;
            SwingUtilities.invokeLater( new Runnable() {
                @Override
                public void run() {
                    toBeClosed.putClientProperty("inCloseAll", Boolean.TRUE);
                    toBeClosed.close();
                }
            });
        }
    }

    /** Closes all documents except given param, according to isContext flag
     * 
     * @param isContext when true, closes all documents except given 
     * in active mode only, otherwise closes all documents in the system except
     * given
     */
    public static void closeAllExcept (TopComponent tc, boolean isContext) {
        if (isContext) {
            List<TopComponent> tcs = getOpened(tc);

            for(TopComponent curTC: tcs) {
                if( !Switches.isClosingEnabled(curTC) )
                    continue;
                if (curTC != tc) {
                    curTC.putClientProperty("inCloseAll", Boolean.TRUE);
                    curTC.close();
                }
            }
        } else {
            TopComponent[] tcs = WindowManagerImpl.getInstance().getEditorTopComponents();

            for(TopComponent curTC: tcs) {
                if( !Switches.isClosingEnabled(curTC) )
                    continue;
                if (curTC != tc) {
                    curTC.putClientProperty("inCloseAll", Boolean.TRUE);
                    curTC.close();
                }
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

