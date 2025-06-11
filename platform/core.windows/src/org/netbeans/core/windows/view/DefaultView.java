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

package org.netbeans.core.windows.view;


import java.util.List;
import java.util.logging.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.SwingUtilities;

import org.openide.awt.ToolbarPool; // Why is this in open API?
import org.openide.windows.TopComponent;

import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.Debug;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.view.dnd.WindowDnDManager;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.WindowSystemSnapshot;
import org.netbeans.core.windows.view.dnd.TopComponentDraggable;
import org.netbeans.core.windows.view.ui.slides.SlideOperation;

/**
 * Class which handles view requests, i.e. updates GUI accordingly (ViewHierarchy)
 * and also handles changes to GUI made by user, informs controller handler.
 *
 * @author  Peter Zavadsky
 */
class DefaultView implements View, Controller, WindowDnDManager.ViewAccessor {
    
    
    private final ViewHierarchy hierarchy = new ViewHierarchy(this, new WindowDnDManager(this));
    
    private final ControllerHandler controllerHandler;
    
    private final Set<TopComponent> showingTopComponents = Collections.newSetFromMap(new WeakHashMap<>(10));

    /** Debugging flag. */
    private static final boolean DEBUG = Debug.isLoggable(DefaultView.class);
    
    public DefaultView(ControllerHandler controllerHandler) {
        this.controllerHandler = controllerHandler;
    }
    

    // XXX
    @Override
    public boolean isDragInProgress() {
        return hierarchy.isDragInProgress();
    }
    
    // XXX
    @Override
    public Frame getMainWindow() {
        return hierarchy.getMainWindow().getFrame();
    }
    
    @Override
    public Component getEditorAreaComponent() {
        return hierarchy.getEditorAreaComponent();
    }
    
    @Override
    public String guessSlideSide (TopComponent comp) {
        String toReturn = Constants.LEFT;
        if (hierarchy.getMaximizedModeView() != null) {
			//issue #58562
            toReturn = (String)comp.getClientProperty("lastSlideSide");
            if (toReturn == null) {
                //TODO? now how does one figure on startup with maximazed mode where the editor is?
                toReturn = Constants.LEFT;
            }
        } else {
            Rectangle editorb = hierarchy.getPureEditorAreaBounds();
            Point leftTop = new Point(0, 0);
            SwingUtilities.convertPointToScreen(leftTop, comp);
            if (editorb.x > leftTop.x) {
                toReturn = Constants.LEFT;
                comp.putClientProperty("lastSlideSide", toReturn);
            }
            if ((editorb.x + editorb.width) < leftTop.x) {
                toReturn = Constants.RIGHT;
                comp.putClientProperty("lastSlideSide", toReturn);
            }
            if ((editorb.y + editorb.height) < leftTop.y) {
                toReturn = Constants.BOTTOM;
                comp.putClientProperty("lastSlideSide", toReturn);
            }
        }
        return toReturn;
    }
    
    @Override
    public void changeGUI(ViewEvent[] viewEvents, WindowSystemSnapshot snapshot) {

        // Change to view understandable-convenient structure.
        WindowSystemAccessor wsa = ViewHelper.createWindowSystemAccessor(snapshot);
        
        if(DEBUG) {
            debugLog("CHANGEGUI()"); // NOI18N
            debugLog("Structure=" + wsa); // NOI18N
            debugLog(""); // NOI18N
        }

        // Update view hierarchy from accessors info
        if(wsa != null) { // wsa == null during hiding.
            hierarchy.updateViewHierarchy(wsa.getModeStructureAccessor());
        }

        // Update showing TopComponents.
        Set<TopComponent> oldShowing = new HashSet<TopComponent>(showingTopComponents);
        Set<TopComponent> newShowing = hierarchy.getShowingTopComponents();
        showingTopComponents.clear();
        showingTopComponents.addAll(newShowing);
        
        Set<TopComponent> toShow = new HashSet<TopComponent>(newShowing);
        toShow.removeAll(oldShowing);
        for(TopComponent tc: toShow) {
            WindowManagerImpl.getInstance().componentShowing(tc);
        }
        if(DEBUG) {
            debugLog("ChangeGUI: Checking view events...") ; // NOI18N
        }
        
        // PENDING Find main event first.
        for(int i = 0; i < viewEvents.length; i++) {
            ViewEvent viewEvent = viewEvents[i];
            int changeType = viewEvent.getType();
            if(DEBUG) {
                debugLog("ViewEvent=" + viewEvent) ; // NOI18N
            }
            
            if(changeType == CHANGE_VISIBILITY_CHANGED) {
                if(DEBUG) {
                    debugLog("Winsys visibility changed, visible=" + viewEvent.getNewValue()) ; // NOI18N
                }
                
                windowSystemVisibilityChanged(((Boolean)viewEvent.getNewValue()).booleanValue(), wsa);
                // PENDING this should be processed separatelly, there is nothing to coallesce.

                return;
            }
        }

        // Process all event types.
        for(int i = 0; i < viewEvents.length; i++) {
            ViewEvent viewEvent = viewEvents[i];
            int changeType = viewEvent.getType();
            
            // The other types.
            if(changeType == CHANGE_MAIN_WINDOW_BOUNDS_JOINED_CHANGED) {
                if(DEBUG) {
                    debugLog("Main window bounds joined changed"); // NOI18N
                }

                if(wsa.getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
                    Rectangle bounds = (Rectangle)viewEvent.getNewValue();
                    if(bounds != null) {
                        hierarchy.getMainWindow().setBounds(bounds);
                    }
                }
            } else if(changeType == CHANGE_MAIN_WINDOW_BOUNDS_SEPARATED_CHANGED) {
                if(DEBUG) {
                    debugLog("Main window bounds separated changed"); // NOI18N
                }

                if(wsa.getEditorAreaState() == Constants.EDITOR_AREA_SEPARATED) {
                    Rectangle bounds = (Rectangle)viewEvent.getNewValue();
                    if(bounds != null) {
                        hierarchy.getMainWindow().setBounds(bounds);
                    }
                }
            } else if(changeType == CHANGE_MAIN_WINDOW_FRAME_STATE_JOINED_CHANGED) {
                if(DEBUG) {
                    debugLog("Main window frame state joined changed"); // NOI18N
                }

                if(wsa.getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
                    hierarchy.getMainWindow().setExtendedState(wsa.getMainWindowFrameStateJoined());
                }
            } else if(changeType == CHANGE_MAIN_WINDOW_FRAME_STATE_SEPARATED_CHANGED) {
                if(DEBUG) {
                    debugLog("Main window frame state separated changed"); // NOI18N
                }

                if(wsa.getEditorAreaState() == Constants.EDITOR_AREA_SEPARATED) {
                    hierarchy.getMainWindow().setExtendedState(wsa.getMainWindowFrameStateSeparated());
                }
            } else if(changeType == CHANGE_EDITOR_AREA_STATE_CHANGED) {
                if(DEBUG) {
                    debugLog("Editor area state changed"); // NOI18N
                }
                //#45832 updating the main windo bounds goes first. need to have the correct bounds when updating desktop.
                hierarchy.updateMainWindowBounds(wsa);
                hierarchy.updateDesktop(wsa);
                hierarchy.setSeparateModesVisible(true);
            } else if(changeType == CHANGE_EDITOR_AREA_FRAME_STATE_CHANGED) {
                if(DEBUG) {
                    debugLog("Editor area frame state changed"); // NOI18N
                }
                hierarchy.updateEditorAreaFrameState(wsa.getEditorAreaFrameState());
            } else if(changeType == CHANGE_EDITOR_AREA_BOUNDS_CHANGED) {
                if(DEBUG) {
                    debugLog("Editor area bounds changed"); // NOI18N
                }

                hierarchy.updateEditorAreaBounds((Rectangle)viewEvent.getNewValue());
            } else if(changeType == CHANGE_EDITOR_AREA_CONSTRAINTS_CHANGED) {
                if(DEBUG) {
                    debugLog("Editor area constraints changed"); // NOI18N
                }

                hierarchy.updateDesktop(wsa);
            } else if(changeType == CHANGE_ACTIVE_MODE_CHANGED) {
                if(DEBUG) {
                    debugLog("Active mode changed, mode=" + viewEvent.getNewValue()); // NOI18N
                }
                hierarchy.updateDesktop(wsa);
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_TOOLBAR_CONFIGURATION_CHANGED) {
                if(DEBUG) {
                    debugLog("Toolbar config name changed"); // NOI18N
                }

                ToolbarPool.getDefault().setConfiguration(wsa.getToolbarConfigurationName());
            } else if(changeType == CHANGE_MAXIMIZED_MODE_CHANGED) {
                if(DEBUG) {
                    debugLog("Maximized mode changed"); // NOI18N
                }

                hierarchy.setMaximizedModeView(hierarchy.getModeViewForAccessor(wsa.getMaximizedModeAccessor()));
                hierarchy.updateDesktop(wsa);
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_MODE_ADDED) {
                if(DEBUG) {
                    debugLog("Mode added"); // NOI18N
                }

                hierarchy.updateDesktop(wsa);
            } else if(changeType == CHANGE_MODE_REMOVED) {
                if(DEBUG) {
                    debugLog("Mode removed"); // NOI18N
                }

                hierarchy.updateDesktop(wsa);
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_MODE_CONSTRAINTS_CHANGED) {
                if(DEBUG) {
                    debugLog("Mode constraints changed"); // NOI18N
                }

                hierarchy.updateDesktop(wsa);
            } else if(changeType == CHANGE_MODE_BOUNDS_CHANGED) {
                if(DEBUG) {
                    debugLog("Mode bounds changed"); // NOI18N
                }

                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if(modeView != null) {
                    modeView.getComponent().setBounds((Rectangle)viewEvent.getNewValue());
                }
            } else if(changeType == CHANGE_MODE_FRAME_STATE_CHANGED) {
                if(DEBUG) {
                    debugLog("Mode state changed"); // NOI18N
                }

                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if(modeView != null) {
                    modeView.setFrameState(((Integer)viewEvent.getNewValue()).intValue());
                    modeView.updateFrameState();
                }
            } else if(changeType == CHANGE_MODE_SELECTED_TOPCOMPONENT_CHANGED) {
                if(DEBUG) {
                    debugLog("Selected topcomponent changed, tc=" + viewEvent.getNewValue()); // NOI18N
                }

                // XXX PENDING see TopComponent.requestFocus (it's wrongly overriden).
                hierarchy.updateDesktop(wsa);
//                // XXX if the selection is changed in the active mode reactivate it.
//                ModeAccessor ma = wsa.getActiveModeAccessor();
//                if(ma == wsa.getActiveModeAccessor()) {
                hierarchy.activateMode(wsa.getActiveModeAccessor());
//                }
            } else if(changeType == CHANGE_MODE_TOPCOMPONENT_ADDED) {
                if(DEBUG) {
                    debugLog("TopComponent added"); // NOI18N
                }

                hierarchy.updateDesktop(wsa);
                hierarchy.setSeparateModesVisible(true);
                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if (modeView != null) {
                    // #39755 - seems to require to call the updateframestate() in order to have a closed mode to show in the last framestate.
                    // not 100% sure this is the correct location for the call, for editorarea the relevant change resides in ViewHierarchy.updateDesktop,
                    // prefer not to call hierarchy.updateframestates() because it's only needed for the currently opened mode..
                    modeView.updateFrameState();
                }
            } else if(changeType == CHANGE_MODE_TOPCOMPONENT_REMOVED) {
                if(DEBUG) {
                    debugLog("TopComponent removed"); // NOI18N
                }

                hierarchy.setMaximizedModeView(hierarchy.getModeViewForAccessor(wsa.getMaximizedModeAccessor()));
                hierarchy.updateDesktop(wsa);
                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if(modeView != null) {
                    modeView.removeTopComponent((TopComponent)viewEvent.getNewValue());
                }
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_TOPCOMPONENT_DISPLAY_NAME_CHANGED) {
                if(DEBUG) {
                    debugLog("TopComponent display name changed, tc=" + viewEvent.getNewValue()); // NOI18N
                }

                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if(modeView != null) { // PENDING investigate
                    modeView.updateName((TopComponent)viewEvent.getNewValue());
                }
            } else if(changeType == CHANGE_TOPCOMPONENT_DISPLAY_NAME_ANNOTATION_CHANGED) {
                if(DEBUG) {
                    debugLog("TopComponent display name annotation changed, tc=" + viewEvent.getNewValue()); // NOI18N
                }

                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if(modeView != null) { // PENDING investigate
                    modeView.updateName((TopComponent)viewEvent.getNewValue());
                }
            } else if(changeType == CHANGE_TOPCOMPONENT_TOOLTIP_CHANGED) {
                if(DEBUG) {
                    debugLog("TopComponent tooltip changed, tc=" + viewEvent.getNewValue()); // NOI18N
                }

                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if(modeView != null) { // PENDING investigate
                    modeView.updateToolTip((TopComponent)viewEvent.getNewValue());
                }
            } else if(changeType == CHANGE_TOPCOMPONENT_ICON_CHANGED) {
                if(DEBUG) {
                    debugLog("TopComponent icon changed"); // NOI18N
                }

                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if(modeView != null) { // PENDING investigate
                    modeView.updateIcon((TopComponent)viewEvent.getNewValue());
                }
            } else if(changeType == CHANGE_TOPCOMPONENT_ATTACHED) {
                if(DEBUG) {
                    debugLog("TopComponent attached"); // NOI18N
                }                

                hierarchy.updateDesktop(wsa);
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_TOPCOMPONENT_ARRAY_ADDED) {
                if(DEBUG) {
                    debugLog("TopComponent array added:" // NOI18N
                        + Arrays.asList((TopComponent[])viewEvent.getNewValue()));
                }
                hierarchy.updateDesktop(wsa);
//                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_TOPCOMPONENT_ARRAY_REMOVED) {
                if(DEBUG) {
                    debugLog("TopComponent array removed:" // NOI18N
                        + Arrays.asList((TopComponent[])viewEvent.getNewValue()));
                }

                hierarchy.updateDesktop(wsa);
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_TOPCOMPONENT_ACTIVATED) {
                if(DEBUG) {
                    debugLog("TopComponent activated, tc=" + viewEvent.getNewValue()); // NOI18N
                }
                
                hierarchy.updateDesktop(wsa);
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_MODE_CLOSED) {
                if(DEBUG) {
                    debugLog("Mode closed, mode=" + viewEvent.getSource()); // NOI18N
                }
                
                hierarchy.updateDesktop();
            } else if(changeType == CHANGE_DND_PERFORMED) {
                if(DEBUG) {
                    debugLog("DnD performed"); // NOI18N
                }

                hierarchy.setMaximizedModeView(hierarchy.getModeViewForAccessor(wsa.getMaximizedModeAccessor()));
                hierarchy.updateDesktop();
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_UI_UPDATE) {
                if(DEBUG) {
                    debugLog("UI update"); // NOI18N
                }

                hierarchy.updateUI();
            } else if(changeType == CHANGE_TOPCOMPONENT_AUTO_HIDE_ENABLED ||
                      changeType == CHANGE_TOPCOMPONENT_AUTO_HIDE_DISABLED) {
                if(DEBUG) {
                    debugLog("Top Component Auto Hide changed"); // NOI18N
                }
                hierarchy.setMaximizedModeView(hierarchy.getModeViewForAccessor(wsa.getMaximizedModeAccessor()));
                hierarchy.updateDesktop(wsa);
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if (changeType == View.TOPCOMPONENT_REQUEST_ATTENTION) {
                if (DEBUG) {
                    debugLog("Top component request attention");
                }
                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if (modeView != null) {
                   TopComponent tc = (TopComponent) viewEvent.getNewValue();
                   if (tc == null) {
                       throw new NullPointerException ("Top component is null for attention request"); //NOI18N
                   }
                   modeView.requestAttention (tc); 
                } else {
                    Logger.getLogger(DefaultView.class.getName()).fine(
                        "Could not find mode " + viewEvent.getSource());
                }
            } else if (changeType == View.TOPCOMPONENT_CANCEL_REQUEST_ATTENTION) {
                if (DEBUG) {
                    debugLog("Top component cancel request attention"); //NOI18N
                }
                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if (modeView != null) {
                    TopComponent tc = (TopComponent) viewEvent.getNewValue();
                    if (tc == null) {
                        throw new NullPointerException ("Top component is null for attention cancellation request"); //NOI18N
                    }
                    //make sure the TC is still opened in the given mode container
                    if( modeView.getTopComponents().contains( tc ) ) {
                        modeView.cancelRequestAttention (tc);
                    }
                } else {
                    Logger.getLogger(DefaultView.class.getName()).fine(
                        "Could not find mode " + viewEvent.getSource());
                }
            } else if (changeType == View.TOPCOMPONENT_ATTENTION_HIGHLIGHT_ON
                    || changeType == View.TOPCOMPONENT_ATTENTION_HIGHLIGHT_OFF) {
                if (DEBUG) {
                    debugLog("Top component attention highlight"); //NOI18N
                }
                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if (modeView != null) {
                    TopComponent tc = (TopComponent) viewEvent.getNewValue();
                    if (tc == null) {
                        throw new NullPointerException ("Top component is null for attention cancellation request"); //NOI18N
                    }
                    //make sure the TC is still opened in the given mode container
                    if( modeView.getTopComponents().contains( tc ) ) {
                        modeView.setAttentionHighlight(tc, changeType == View.TOPCOMPONENT_ATTENTION_HIGHLIGHT_ON);
                    }
                } else {
                    Logger.getLogger(DefaultView.class.getName()).fine(
                        "Could not find mode " + viewEvent.getSource());
                }
            } else if (changeType == View.TOPCOMPONENT_SHOW_BUSY || changeType == View.TOPCOMPONENT_HIDE_BUSY ) {
                if (DEBUG) {
                    debugLog("Top component show/hide busy"); //NOI18N
                }
                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if (modeView != null) {
                    TopComponent tc = (TopComponent) viewEvent.getNewValue();
                    if (tc == null) {
                        throw new NullPointerException ("Top component is null for make busy request"); //NOI18N
                    }
                    //make sure the TC is still opened in the given mode container
                    if( modeView.getTopComponents().contains( tc ) ) {
                        modeView.makeBusy(tc, changeType == View.TOPCOMPONENT_SHOW_BUSY);
                    }
                } else {
                    Logger.getLogger(DefaultView.class.getName()).fine(
                        "Could not find mode " + viewEvent.getSource());
                }
            } else if (changeType == View.CHANGE_MAXIMIZE_TOPCOMPONENT_SLIDE_IN) {
                if (DEBUG) {
                    debugLog("Slided-in top component toggle maximize"); //NOI18N
                }
                TopComponent tc = (TopComponent)viewEvent.getSource();
                String side = (String)viewEvent.getNewValue();
                hierarchy.performSlideToggleMaximize( tc, side );
            }
        }
        
        Set<TopComponent> toHide = new HashSet<TopComponent>(oldShowing);
        toHide.removeAll(newShowing);
        for(TopComponent tc: toHide) {
            WindowManagerImpl.getInstance().componentHidden(tc);
        }
    }
    
    /** Whether the window system should show or hide its GUI. */
    private void windowSystemVisibilityChanged(boolean visible, WindowSystemAccessor wsa) {
        if(visible) {
            showWindowSystem(wsa);
        } else {
            hideWindowSystem();
        }
    }
    

    private void showWindowSystem(final WindowSystemAccessor wsa) {
        long start = System.currentTimeMillis();
        if(DEBUG) {
            debugLog("ShowWindowSystem--"); // NOI18N
        }
        
        hierarchy.getMainWindow().initializeComponents();

        JFrame frame = hierarchy.getMainWindow().getFrame();
        // Init toolbar.
        ToolbarPool.getDefault().setConfiguration(wsa.getToolbarConfigurationName());
        
        if(DEBUG) {
            debugLog(wsa.getModeStructureAccessor().toString());
        }
        // Prepare main window (pack and set bounds).
        hierarchy.getMainWindow().prepareWindow();

        if(DEBUG) {
            debugLog("Init view 4="+(System.currentTimeMillis() - start) + " ms"); // NOI18N
        }


        if(DEBUG) {
            debugLog("Init view 2="+(System.currentTimeMillis() - start) + " ms"); // NOI18N
        }
        
        hierarchy.setSplitModesVisible(true);

        if(DEBUG) {
            debugLog("Init view 3="+(System.currentTimeMillis() - start) + " ms"); // NOI18N
        }
        
        if(wsa.getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
            hierarchy.getMainWindow().setExtendedState(wsa.getMainWindowFrameStateJoined());
        } else {
            hierarchy.getMainWindow().setExtendedState(wsa.getMainWindowFrameStateSeparated());
        }

        // Shows main window
        hierarchy.getMainWindow().setVisible(true);
        
        hierarchy.setMaximizedModeView(hierarchy.getModeViewForAccessor(wsa.getMaximizedModeAccessor()));

        // Init desktop.
        hierarchy.updateDesktop(wsa);
        
        // Show separate modes.
        hierarchy.setSeparateModesVisible(true);

        hierarchy.updateEditorAreaFrameState(wsa.getEditorAreaFrameState());
        
        // Updates frame states of separate modes.
        hierarchy.updateFrameStates();
        
        // XXX PENDING
        if(wsa.getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
            // Ignore when main window is maximized.
            if(frame.getExtendedState() != Frame.MAXIMIZED_BOTH) {
                if (DEBUG) {
                    debugLog("do updateMainWindowBoundsSeparatedHelp");
                }
                updateMainWindowBoundsSeparatedHelp();
                updateEditorAreaBoundsHelp();
//                updateSeparateBoundsForView(hierarchy.getSplitRootElement());
            }
        }
        
        // setting activate mode had to be done in Swing.invokeLater() because of split recalculations. (#40501)
        // since the JSplitPane.resetToPrefferedSizes() rewrite, it's no longer necessary
        // also should fix 
        hierarchy.activateMode(wsa.getActiveModeAccessor());

        //#39238 in maximazed mode swing posts a lot of stuff to Awt thread using SwingUtilities.invokeLater
        // for that reason the installation of window listeners and the update of splits kicked in too early when
        // the window was not maximazed yet -> resulted in wrong calculation of splits and also bad reactions from the listeners
        // which considered the automated change to maximazed mode to be issued by the user.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (DEBUG) {
                    debugLog("Installing main window listeners.");
                }
                hierarchy.installMainWindowListeners();
            }
        });
        
        if(DEBUG) {
            debugLog("Init view 5="+(System.currentTimeMillis() - start) + " ms"); // NOI18N
        }
    }
    
    private void hideWindowSystem() {
        hierarchy.uninstallMainWindowListeners();
        
        hierarchy.setSeparateModesVisible(false);
        hierarchy.getMainWindow().setVisible(false);
        // Release all.
        hierarchy.releaseAll();
    }
    
    // Controller >>
    @Override
    public void userActivatedModeView(ModeView modeView) {
        if(DEBUG) {
            debugLog("User activated mode view, mode=" + modeView); // NOI18N
        }
        
        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userActivatedMode(mode);
    }
    
    @Override
    public void userActivatedModeWindow(ModeView modeView) {
        if(DEBUG) {
            debugLog("User activated mode window, mode=" + modeView); // NOI18N
        }
        
        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userActivatedModeWindow(mode);
    }
    
    @Override
    public void userActivatedEditorWindow() {
        if(DEBUG) {
            debugLog("User activated editor window"); // NOI18N
        }
        
        controllerHandler.userActivatedEditorWindow();
    }
    
    @Override
    public void userSelectedTab(ModeView modeView, TopComponent selected) {
        if(DEBUG) {
            debugLog("User selected tab, tc=" + WindowManagerImpl.getInstance().getTopComponentDisplayName(selected)); // NOI18N
        }

        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userActivatedTopComponent(mode, selected);
    }
    
    @Override
    public void userClosingMode(ModeView modeView) {
        if(DEBUG) {
            debugLog("User closing mode="+modeView); // NOI18N
        }

        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userClosedMode(mode);
    }
    
    @Override
    public void userResizedMainWindow(Rectangle bounds) {
        if(DEBUG) {
            debugLog("User resized main window"); // NOI18N
        }

        // Ignore when main window is maximized.
        if(hierarchy.getMainWindow().getExtendedState() != Frame.MAXIMIZED_BOTH) {
            controllerHandler.userResizedMainWindow(bounds);
        } 

        // Ignore when main window is maximized.
        if(hierarchy.getMainWindow().getExtendedState() != Frame.MAXIMIZED_BOTH) {
            // XXX PENDING
            updateMainWindowBoundsSeparatedHelp();
            updateEditorAreaBoundsHelp();
            updateSeparateBoundsForView(hierarchy.getSplitRootElement());
        }
    }
    
    @Override
    public void userMovedMainWindow(Rectangle bounds) {
        if(DEBUG) {
            debugLog("User moved main window"); // NOI18N
        }

        // Ignore when main window is maximized.
        if (hierarchy.getMainWindow().getExtendedState() != Frame.MAXIMIZED_BOTH) {
            controllerHandler.userResizedMainWindow(bounds);
        }
    }
    
    @Override
    public void userResizedEditorArea(Rectangle bounds) {
        if(DEBUG) {
            debugLog("User resized editor area"); // NOI18N
        }

        controllerHandler.userResizedEditorArea(bounds);
    }
    
    @Override
    public void userResizedModeBounds(ModeView modeView, Rectangle bounds) {
        if(DEBUG) {
            debugLog("User resized mode"); // NOI18N
        }

        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        // XXX PENDING #39083 Investigate how it could happen.
        if(modeAccessor != null) {
            ModeImpl mode = getModeForModeAccessor(modeAccessor);
            controllerHandler.userResizedModeBounds(mode, bounds);
        }
    }
    
    @Override
    public void userMovedSplit(SplitView splitView, ViewElement[] childrenViews, double[] splitWeights) {
        if(DEBUG) {
            debugLog("User moved split"); // NOI18N
//            Debug.dumpStack(DefaultView.class);
        }

        SplitAccessor splitAccessor = (SplitAccessor)hierarchy.getAccessorForView(splitView);
        // XXX PENDING #257467 Investigate how it could happen.
        if (splitAccessor != null) {
            ElementAccessor[] childrenAccessors = new ElementAccessor[childrenViews.length];
            for( int i=0; i<childrenViews.length; i++ ) {
                childrenAccessors[i] = hierarchy.getAccessorForView( childrenViews[i] );
            }
            ViewHelper.setSplitWeights(splitAccessor, childrenAccessors, splitWeights, controllerHandler);
        }
        // XXX PENDING
//        updateSeparateBoundsForView(splitView);
    }
    
    @Override
    public void userClosedTopComponent(ModeView modeView, TopComponent tc) {
        if(DEBUG) {
            debugLog("User closed topComponent=" + tc); // NOI18N
        }

        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userClosedTopComponent(mode, tc);
    }
    
    @Override
    public void userChangedFrameStateMainWindow(int frameState) {
        if(DEBUG) {
            debugLog("User changed frame state main window"); // NOI18N
        }
        
        controllerHandler.userChangedFrameStateMainWindow(frameState);
    }
    
    @Override
    public void userChangedFrameStateEditorArea(int frameState) {
        if(DEBUG) {
            debugLog("User changed frame state editor area"); // NOI18N
        }
        controllerHandler.userChangedFrameStateEditorArea(frameState);
    }
    
    @Override
    public void userChangedFrameStateMode(ModeView modeView, int frameState) {
        if(DEBUG) {
            debugLog("User changed frame state mode"); // NOI18N
        }

        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userChangedFrameStateMode(mode, frameState);
    }
    
    // DnD
    @Override
    public void userDroppedTopComponents(ModeView modeView, TopComponentDraggable draggable) {
        if(DEBUG) {
            debugLog("User dropped TopComponent's"); // NOI18N
        }

        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userDroppedTopComponents(mode, draggable);
    }
    
    @Override
    public void userDroppedTopComponents(ModeView modeView, TopComponentDraggable draggable, int index) {
        if(DEBUG) {
            debugLog("User dropped TopComponent's to index=" + index); // NOI18N
        }
        
        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        
        // #37127 Refine the index if the TC is moving inside the mode.
        if( draggable.isTopComponentTransfer() ) {
            int position = Arrays.asList(modeAccessor.getOpenedTopComponents()).indexOf(draggable.getTopComponent());
            if(position > -1 && position <= index) {
                index--;
            }
        }
                
        controllerHandler.userDroppedTopComponents(mode, draggable, index);
    }
    
    @Override
    public void userDroppedTopComponents(ModeView modeView, TopComponentDraggable draggable, String side) {
        if(DEBUG) {
            debugLog("User dropped TopComponent's to side=" + side); // NOI18N
        }
        
        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userDroppedTopComponents(mode, draggable, side);
    }

    @Override
    public void userDroppedTopComponentsIntoEmptyEditor(TopComponentDraggable draggable) {
        if(DEBUG) {
            debugLog("User dropped TopComponent's into empty editor"); // NOI18N
        }
        
        controllerHandler.userDroppedTopComponentsIntoEmptyEditor(draggable);
    }
    
    @Override
    public void userDroppedTopComponentsAround(TopComponentDraggable draggable, String side) {
        if(DEBUG) {
            debugLog("User dropped TopComponent's around, side=" + side); // NOI18N
        }
        
        controllerHandler.userDroppedTopComponentsAround(draggable, side);
    }
    
    @Override
    public void userDroppedTopComponentsAroundEditor(TopComponentDraggable draggable, String side) {
        if(DEBUG) {
            debugLog("User dropped TopComponent's around editor, side=" + side); // NOI18N
        }
        
        controllerHandler.userDroppedTopComponentsAroundEditor(draggable, side);
    }
    
    @Override
    public void userDroppedTopComponentsIntoFreeArea(TopComponentDraggable draggable, Rectangle bounds) {
        if(DEBUG) {
            debugLog("User dropped TopComponent's into free area, bounds=" + bounds); // NOI18N
        }
        
        controllerHandler.userDroppedTopComponentsIntoFreeArea(draggable, bounds);
    }

    // Sliding

    @Override
    public void userDisabledAutoHide(ModeView modeView, TopComponent tc) {
        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userDisabledAutoHide(tc, mode);
    }    

    @Override
    public void userEnabledAutoHide(ModeView modeView, TopComponent tc) {
        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        String side = guessSlideSide(tc);
        controllerHandler.userEnabledAutoHide(tc, mode, side);
    }
    
    @Override
    public void userTriggeredSlideIn(ModeView modeView, SlideOperation operation) {
        hierarchy.performSlideIn(operation);
    }    
    
    @Override
    public void userTriggeredSlideOut(ModeView modeView, SlideOperation operation) {
        hierarchy.performSlideOut(operation);
        // restore focus if needed
        if (operation.requestsActivation()) {
            ModeView lastNonSlidingActive = hierarchy.getLastNonSlidingActiveModeView();
            ModeImpl mode = null;
            if (lastNonSlidingActive != null) {
                mode = getModeForModeAccessor((ModeAccessor)hierarchy.getAccessorForView(lastNonSlidingActive));
            }
            if (mode != null) {
                controllerHandler.userActivatedMode(mode);
            } else {
                // no appropriate mode exists - select editor as last resort
                controllerHandler.userActivatedEditorWindow();
            }
        }
    }    
    
    @Override
    public void userTriggeredSlideIntoDesktop(ModeView modeView, SlideOperation operation) {
        hierarchy.performSlideIntoDesktop(operation);
    }    
    
    @Override
    public void userTriggeredSlideIntoEdge(ModeView modeView, SlideOperation operation) {
        hierarchy.performSlideIntoEdge(operation);
    }
    
    @Override
    public void userResizedSlidingWindow(ModeView modeView, SlideOperation operation) {
        ((SlidingView)modeView).setSlideBounds(modeView.getSelectedTopComponent().getBounds());
        hierarchy.performSlideResize(operation);
        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userResizedSlidingMode(mode, operation.getFinishBounds());
    }
    
    
    private static ModeImpl getModeForModeAccessor(ModeAccessor accessor) {
        return accessor == null ? null : accessor.getMode();
    }
    // Controller <<
    // XXX
    private void updateMainWindowBoundsSeparatedHelp() {
        controllerHandler.userResizedMainWindowBoundsSeparatedHelp(
                hierarchy.getMainWindow().getPureMainWindowBounds());
    }
    
    // XXX
    private void updateEditorAreaBoundsHelp() {
        Rectangle bounds = hierarchy.getPureEditorAreaBounds();
        controllerHandler.userResizedEditorAreaBoundsHelp(bounds);
    }
    
    // XXX PENDING This is just for the cases split modes doesn't have a separated
    // opposite ones, so they keep the bounds for them. Revise.
    void updateSeparateBoundsForView(ViewElement view) {
        if (view.getComponent() instanceof JComponent) {
            // when updating the views after resizing the split, do
            // set the preffered size accordingly to prevent jumping splits
            // in SplitView.updateAwtHierarchy() 
            // is basically a workaround, the problem should be fixed by reimplementing the 
            // splits without using the JSplitPane
            // #45186 for more details
            JComponent comp = (JComponent)view.getComponent();
            Dimension dim = new Dimension(comp.getSize());
            comp.setPreferredSize(dim);
            comp.putClientProperty("lastAvailableSpace", dim); //NOI18N
        }
        if(view instanceof ModeView) {
            ModeView mv = (ModeView)view;
            ModeAccessor ma = (ModeAccessor)hierarchy.getAccessorForView(mv);
            if(ma != null) {
                Component comp = mv.getComponent();
                Rectangle bounds = comp.getBounds();
                Point point = new Point(0, 0);
                SwingUtilities.convertPointToScreen(point, comp);
                bounds.setLocation(point);
                
                ModeImpl mode = getModeForModeAccessor(ma);
                // XXX ControllerHandler
                controllerHandler.userResizedModeBoundsSeparatedHelp(mode, bounds);
            }
        } else if(view instanceof SplitView) {
            SplitView sv = (SplitView)view;
            List children = sv.getChildren();
            for( Iterator i=children.iterator(); i.hasNext(); ) {
                ViewElement child = (ViewElement)i.next();
                updateSeparateBoundsForView( child );
            }
        } else if(view instanceof EditorView) {
            updateEditorAreaBoundsHelp();
            // Editor area content isn't needed to remember.
        }
    }

    // ViewAccessor
    @Override
    public Set<Component> getModeComponents() {
        return hierarchy.getModeComponents();
    }
    
    @Override
    public Set<Component> getSeparateModeFrames() {
        return hierarchy.getSeparateModeFrames();
    }
    
    @Override
    public Controller getController() {
        return this;
    }
    
    @Override
    public Component getSlidingModeComponent(String side) {
        return hierarchy.getSlidingModeComponent(side);
    }
    // ViewAccessor
    private static void debugLog(String message) {
        Debug.log(DefaultView.class, message);
    }

    @Override
    public void userStartedKeyboardDragAndDrop( TopComponentDraggable draggable ) {
        hierarchy.userStartedKeyboardDragAndDrop( draggable );
    }
    
}

