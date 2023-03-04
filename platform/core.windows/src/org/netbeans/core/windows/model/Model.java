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

package org.netbeans.core.windows.model;


import java.awt.Rectangle;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.SplitConstraint;
import org.netbeans.core.windows.TopComponentGroupImpl;
import org.netbeans.core.windows.WindowSystemSnapshot;
import org.openide.windows.TopComponent;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Represents model of this window system implementation.
 *
 * @author  Peter Zavadsky
 */
public interface Model {

    ////////////////////////////////
    // Global (the highest) level >>
    /////////////////////////////
    // Mutators (global level) >>
    /** Sets visibility status. */
    public void setVisible(boolean visible);
    /** Sets main window bounds when the editor area is in joined(tiled) state. */
    public void setMainWindowBoundsJoined(Rectangle bounds);
    /** Sets main window bounds when the editor area is in separated state. */
    public void setMainWindowBoundsSeparated(Rectangle bounds);
    /** Sets frame state of main window when editor area is in tiled(joined) state. */
    public void setMainWindowFrameStateJoined(int frameState);
    /** Sets frame state of main window when editor area is in separated state. */
    public void setMainWindowFrameStateSeparated(int frameState);
    /** Sets editor area state. */
    public void setEditorAreaState(int editorAreaState);
    /** Sets editor area frame state when in separate state. */
    public void setEditorAreaFrameState(int frameState);
    /** Sets editor area bounds. */
    public void setEditorAreaBounds(Rectangle editorAreaBounds);
    /** Sets editor area constraints. */
    public void setEditorAreaConstraints(SplitConstraint[] editorAreaConstraints);
    /** Sets toolbar configuration name. */
    public void setToolbarConfigName(String toolbarConfigName);
    /** Sets active mode. */
    public void setActiveMode(ModeImpl mode);
    /** Sets editor mode that is currenlty maximized */
    public void setEditorMaximizedMode(ModeImpl maximizedMode);
    /** Sets view mode that is currenlty maximized */
    public void setViewMaximizedMode(ModeImpl maximizedMode);
    /** Adds mode. */ 
    public void addMode(ModeImpl mode, SplitConstraint[] constraints);
    /** Adds mode. */
    // XXX
    public void addModeToSide(ModeImpl mode, ModeImpl attachMode, String side);
    // XXX
    /** Adds mode around (attaches from side). */
    public void addModeAround(ModeImpl mode, String side);
    // XXX
    /** Adds mode around editor area (attaches from side). */
    public void addModeAroundEditor(ModeImpl mode, String side);
    /** Removes mode. */
    public void removeMode(ModeImpl mode);
    /** Renames a mode */
    public void setModeName(ModeImpl mode, String name);
    /** Sets mode constraints. */
    public void setModeConstraints(ModeImpl mode, SplitConstraint[] constraints);
    /** Adds top component group. */
    public void addTopComponentGroup(TopComponentGroupImpl tcGroup);
    /** Removes top component group. */
    public void removeTopComponentGroup(TopComponentGroupImpl tcGroup);
    /** Adds sliding mode into specific side */ 
    public void addSlidingMode(ModeImpl mode, String side, Map<String,Integer> slideInSizes);
    /** Resets the model to an initial state. */
    public void reset();
    /** Set the size (width or height of the given TopComponent when it is slided in */
    public void setSlideInSize(String side, TopComponent tc, int size);
    /**
     * Set whether the given TopComponent is maximized when it is slided-in.
     */
    public void setTopComponentMaximizedWhenSlidedIn( String tcid, boolean maximized );
    
    // Mutators (global level) <<
    /////////////////////////////

    //////////////////////////////   
    // Accessors (global level) >>
    /** Gets visibility status. */
    public boolean isVisible();
    /** Gets main window bounds for the joined (tiled) editor area state. */
    public Rectangle getMainWindowBoundsJoined(); 
    /** Gets main window bounds for the separated editor area state. */
    public Rectangle getMainWindowBoundsSeparated(); 
    /** Gets frame state of main window when editor area is in tiled(joined) state. */
    public int getMainWindowFrameStateJoined();
    /** Gets frame state of main window when editor area is in separated state. */
    public int getMainWindowFrameStateSeparated();
    /** Gets main window bounds for separated state (helper initial value). */
    public Rectangle getMainWindowBoundsSeparatedHelp();
    /** Gets editor area state. */
    public int getEditorAreaState();
    /** Gets editor area frame state when in serparate state. */
    public int getEditorAreaFrameState();
    /** Gets editor area bounds. */
    public Rectangle getEditorAreaBounds();
    /** Gets editor area bounds for separated state (helper initial value). */
    public Rectangle getEditorAreaBoundsHelp();
    /** Gets editor area constraints. */
    public SplitConstraint[] getEditorAreaConstraints();
    /** Gets toolbar configuration name. */
    public String getToolbarConfigName();
    /** Gets active mode. */
    public ModeImpl getActiveMode();
    /** Gets last active editor mode. */
    public ModeImpl getLastActiveEditorMode();
    /** Gets editor maximized mode. */
    public ModeImpl getEditorMaximizedMode();
    /** Gets view maximized mode. */
    public ModeImpl getViewMaximizedMode();
    /** Gets set of modes. */
    public Set<ModeImpl> getModes();
    /** Gets mode constraints. */
    public SplitConstraint[] getModeConstraints(ModeImpl mode);
    // XXX
    /** Gets model element constraints. */
    public SplitConstraint[] getModelElementConstraints(ModelElement element);
    /** Gets constraints (its side) for sliding mode */
    public String getSlidingModeConstraints(ModeImpl mode);
    /** Gets constraints (its side) for sliding mode */
    public ModeImpl getSlidingMode(String side);
    /** 
     * Gets the sizes (width or height) of TopComponents in the given sliding 
     * side, the key in the Map is TopComponent's ID 
     */
    public Map<String,Integer> getSlideInSizes(String side);
    /**
     * @return The docking status (docked/slided) of TopComponents before the window system
     * switched to maximized mode.
     */
    public DockingStatus getDefaultDockingStatus();
    /**
     * @return The docking status (docked/slided) of TopComponents in maximized editor mode.
     */
    public DockingStatus getMaximizedDockingStatus();
    /**
     * Find the side (LEFT/RIGHT/BOTTOM) where the TopComponent from the given
     * mode should slide to.
     * 
     * @param mode Mode
     * @return The slide side for TopComponents from the given mode.
     */
    public String getSlideSideForMode( ModeImpl mode );
    /**
     * @return True if the given TopComponent is maximized when it is slided-in.
     */
    public boolean isTopComponentMaximizedWhenSlidedIn( String tcid );

    // Accessors (global level) >>
    //////////////////////////////   
    // Global (the highest) level <<
    ////////////////////////////////

    
    ////////////////
    // Mode level >>
    ///////////////////////////
    // Mutators (mode level) >>
    /** Sets state. */
    public void setModeState(ModeImpl mode, int state);
    /** Sets bounds. */
    public void setModeBounds(ModeImpl mode, Rectangle bounds);
    /** Sets frame state. */
    public void setModeFrameState(ModeImpl mode, int frameState);
    /** Sets selected TopComponent. */
    public void setModeSelectedTopComponent(ModeImpl mode, TopComponent selected);
    /** Remember which top component was the selected one before switching to/from maximized mode */
    public void setModePreviousSelectedTopComponentID(ModeImpl mode, String prevSelectedId);
    /** Adds opened TopComponent. */
    public void addModeOpenedTopComponent(ModeImpl mode, TopComponent tc);
    /** Inserts opened TopComponent. */
    public void insertModeOpenedTopComponent(ModeImpl mode, TopComponent tc, int index);
    /** Adds closed TopComponent. */
    public void addModeClosedTopComponent(ModeImpl mode, TopComponent tc);
    // XXX
    /** Adds unloaded TopComponent. */
    public void addModeUnloadedTopComponent(ModeImpl mode, String tcID, int index);
    // XXX
    public void setModeUnloadedSelectedTopComponent(ModeImpl mode, String tcID);
    /** Remember which top component was the selected one before switching to/from maximized mode */
    public void setModeUnloadedPreviousSelectedTopComponent(ModeImpl mode, String tcID);
    /** */
    /** */
    public void removeModeTopComponent(ModeImpl mode, TopComponent tc, TopComponent recentTc);
    // XXX
    public void removeModeClosedTopComponentID(ModeImpl mode, String tcID);

    // Info about previous top component context, used by sliding kind of modes
    
    /** Sets information of previous mode top component was in. */
    public void setModeTopComponentPreviousMode(ModeImpl mode, String tcID, ModeImpl previousMode, int previousIndex);
    /** Sets information of previous constraints of mode top component was in. */
    public void setModeTopComponentPreviousConstraints(ModeImpl mode, String tcID, SplitConstraint[] constraints);
    
    
    // Mutators (mode level) <<
    ///////////////////////////

    ////////////////////////////
    // Accessors (mode level) >>
    /** Gets programatic name of mode. */
    public String getModeName(ModeImpl mode);
    /** Gets bounds. */
    public Rectangle getModeBounds(ModeImpl mode);
    /** Gets mode bounds for separated state (helper initial value). */
    public Rectangle getModeBoundsSeparatedHelp(ModeImpl mode);
    /** Gets state. */
    public int getModeState(ModeImpl mode);
    /** Gets kind. */
    public int getModeKind(ModeImpl mode);
    /** Gets side. */
    public String getModeSide(ModeImpl mode);
    /** Gets frame state. */
    public int getModeFrameState(ModeImpl mode);
    /** Gets whether it is permanent. */
    public boolean isModePermanent(ModeImpl mode);
    /** Make mode permanent
     * @since 2.30
     */
    public void makeModePermanent( ModeImpl target );
    /** */
    public boolean isModeEmpty(ModeImpl mode);
    /** Indicates whether the mode contains the TopComponent. */
    public boolean containsModeTopComponent(ModeImpl mode, TopComponent tc);
    /** Gets selected TopComponent. */
    public TopComponent getModeSelectedTopComponent(ModeImpl mode);
    /** Get the ID of top component that had been the selected one before switching to/from maximzied mode */
    public String getModePreviousSelectedTopComponentID(ModeImpl mode);
    /** Gets list of top components in this workspace. */
    public List<TopComponent> getModeTopComponents(ModeImpl mode);
    /** Gets list of top components in this workspace. */
    public List<TopComponent> getModeOpenedTopComponents(ModeImpl mode);
    // XXX
    public List<String> getModeOpenedTopComponentsIDs(ModeImpl mode);
    public List<String> getModeClosedTopComponentsIDs(ModeImpl mode);
    public List<String> getModeTopComponentsIDs(ModeImpl mode);

    /** @since 2.30 */
    public boolean isModeMinimized(ModeImpl mode);
    /** @since 2.30 */
    public void setModeMinimized(ModeImpl mode, boolean minimized);
    /** @since 2.30 */
    public Collection<String> getModeOtherNames(ModeImpl mode);
    /** @since 2.30 */
    public void addModeOtherName(ModeImpl mode, String otherModeName);
    /** @since 2.31 */
    public void dockMode( ModeImpl prevMode, ModeImpl floatingMode );
    
    // Info about previous top component context, used by sliding kind of modes
    
    public ModeImpl getModeTopComponentPreviousMode(ModeImpl mode, String tcID);
    public SplitConstraint[] getModeTopComponentPreviousConstraints(ModeImpl mode, String tcID);
    /** Gets the tab index of the given top component before it was moved to sliding/separate mode */
    public int getModeTopComponentPreviousIndex(ModeImpl mode, String tcID);
    
    /** Gets position of given top component in given mode */
    public int getModeOpenedTopComponentTabPosition(ModeImpl mode, TopComponent tc);
    
    // Accessors (mode level) <<
    ////////////////////////////
    // Mode level <<
    ////////////////


    ///////////////////////////
    // TopComponentGroup level >>
    public Set<TopComponentGroupImpl> getTopComponentGroups();

    /** Gets programatic name of goup. */
    public String getGroupName(TopComponentGroupImpl tcGroup);

    public void openGroup(TopComponentGroupImpl tcGroup, 
            Collection<TopComponent> openedTopComponents, 
            Collection<TopComponent> openedBeforeTopComponenets);
    public void closeGroup(TopComponentGroupImpl tcGroup);
    public boolean isGroupOpened(TopComponentGroupImpl tcGroup);
    
    public Set<TopComponent> getGroupTopComponents(TopComponentGroupImpl tcGroup);
    
    public Set<TopComponent> getGroupOpenedTopComponents(TopComponentGroupImpl tcGroup);
    public Set<TopComponent> getGroupOpenedBeforeTopComponents(TopComponentGroupImpl tcGroup);
    
    public Set<TopComponent> getGroupOpeningTopComponents(TopComponentGroupImpl tcGroup);
    public Set<TopComponent> getGroupClosingTopComponents(TopComponentGroupImpl tcGroup);

    public boolean addGroupUnloadedTopComponent(TopComponentGroupImpl tcGroup, String tcID);
    public boolean removeGroupUnloadedTopComponent(TopComponentGroupImpl tcGroup, String tcID);
    
    public boolean addGroupOpeningTopComponent(TopComponentGroupImpl tcGroup, TopComponent tc);
    public boolean removeGroupOpeningTopComponent(TopComponentGroupImpl tcGroup, TopComponent tc);

    public boolean addGroupUnloadedOpeningTopComponent(TopComponentGroupImpl tcGroup, String tcID);
    public boolean removeGroupUnloadedOpeningTopComponent(TopComponentGroupImpl tcGroup, String tcID);
    
    public boolean addGroupUnloadedClosingTopComponent(TopComponentGroupImpl tcGroup, String tcID);
    public boolean removeGroupUnloadedClosingTopComponent(TopComponentGroupImpl tcGroup, String tcID);
    
    // XXX
    public boolean addGroupUnloadedOpenedTopComponent(TopComponentGroupImpl tcGroup, String tcID);
    
    // XXX>>
    public Set<String> getGroupTopComponentsIDs(TopComponentGroupImpl tcGroup);
    public Set<String> getGroupOpeningSetIDs(TopComponentGroupImpl tcGroup);
    public Set<String> getGroupClosingSetIDs(TopComponentGroupImpl tcGroup);
    public Set<String> getGroupOpenedTopComponentsIDs(TopComponentGroupImpl tcGroup);
    // XXX<<
    // TopComponentGroup level <<
    ///////////////////////////

    //////////////////////////////////////    
    // Other methods, creating sub-models.
    /** Creates mode sub model. */
    public void createModeModel(ModeImpl mode, String name, int state, int kind, boolean permanent);
    /** Creates top component group subg  model. */
    public void createGroupModel(TopComponentGroupImpl tcGroup, String name, boolean opened);

    /////////////////////////
    // snapshot
    public WindowSystemSnapshot createWindowSystemSnapshot();

    
    ////////////////////////
    // controller updates >>
    // Helper values.
    public void setMainWindowBoundsUserSeparatedHelp(Rectangle bounds);
    public void setEditorAreaBoundsUserHelp(Rectangle bounds);
    public void setModeBoundsSeparatedHelp(ModeImpl mode, Rectangle bounds);
    
    public void setSplitWeights( ModelElement[] snapshots, double[] splitWeights );
    // controller updates <<
    ////////////////////////


    
}

