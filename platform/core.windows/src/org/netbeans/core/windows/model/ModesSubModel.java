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

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.ModeStructureSnapshot;
import org.netbeans.core.windows.SplitConstraint;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.core.windows.Switches;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.windows.TopComponent;


/**
 * Sub-model which keeps modes data strucute, i.e. as split and also separate
 * ones. Note, it keeps editor ones in another field of split model, for better manipulation.
 * See UI spec about editor area and editor/view component types.
 * Note: this instances aren't thread safe, thus they has to be properly synchronized.
 * The client has to synchronize it.
 * (see in DefaultModel, where it is only place supposed to be used.)
 *
 * @author  Peter Zavadsky
 */
final class ModesSubModel {

    /** Associated parent model. */
    private final Model parentModel;
    
    /** Set of modes. */
    private final Set<ModeImpl> modes = new HashSet<ModeImpl>(10);

    /** Represents split model of modes, also contains special editor area. */
    private final EditorSplitSubModel editorSplitSubModel;
   
    /** Sliding modes model, <ModeImpl, String> mapping 
     of mode and side of presence */
    private final HashMap<ModeImpl, String> slidingModes2Sides = new HashMap<ModeImpl, String>(5);
    private final HashMap<String, ModeImpl> slidingSides2Modes = new HashMap<String, ModeImpl>(5);

    /** Active mode. */
    private ModeImpl activeMode;
    /** Last editor active mode. */
    private ModeImpl lastActiveEditorMode;
    /** Maximized mode. */
    private ModeImpl editorMaximizedMode;
    private ModeImpl viewMaximizedMode;
    
    // (sliding side + TopComponent ID) -> size in pixels (width or height 
    //depending on the sliding side)
    private final Map<String,Integer> slideInSizes = new HashMap<String,Integer>(15);
     
    
    /** Creates a new instance of ModesModel */
    public ModesSubModel(Model parentModel) {
        this.parentModel = parentModel;

        this.editorSplitSubModel = new EditorSplitSubModel(parentModel, new SplitSubModel(parentModel));
    }

    
    public void setEditorAreaConstraints(SplitConstraint[] editorAreaConstraints) {
        editorSplitSubModel.setEditorNodeConstraints(editorAreaConstraints);
    }
    
    public SplitConstraint[] getModelElementConstraints(ModelElement element) {
        return editorSplitSubModel.getModelElementConstraints(element);
    }
    
    public SplitConstraint[] getEditorAreaConstraints() {
        return editorSplitSubModel.getEditorNodeConstraints();
    }
    
    public SplitConstraint[] getModeConstraints(ModeImpl mode) {
        if(mode.getKind() == Constants.MODE_KIND_EDITOR) {
            return editorSplitSubModel.getEditorArea().getModeConstraints(mode);
        } else {
            return editorSplitSubModel.getModeConstraints(mode);
        }
    }
    
    /**
     * Find the side (LEFT/RIGHT/BOTTOM) where the TopComponent from the given
     * mode should slide to.
     * 
     * @param mode Mode
     * @return The slide side for TopComponents from the given mode.
     */
    public String getSlideSideForMode( ModeImpl mode ) {
        return editorSplitSubModel.getSlideSideForMode( mode );
    }
    
    public String getSlidingModeConstraints(ModeImpl mode) {
        return slidingModes2Sides.get(mode);
    }
    
    public ModeImpl getSlidingMode(String side) {
        return slidingSides2Modes.get(side);
    }
    
    public Set<ModeImpl> getSlidingModes() {
        return Collections.unmodifiableSet(slidingModes2Sides.keySet());
    }

    public boolean addMode(ModeImpl mode, SplitConstraint[] constraints) {
        if(modes.contains(mode)) {
            return false;
        }

        boolean result;
        if(mode.getKind() == Constants.MODE_KIND_EDITOR &&
           mode.getState() == Constants.MODE_STATE_JOINED) {
            result = editorSplitSubModel.getEditorArea().addMode(mode, constraints);
        } else {
            result = editorSplitSubModel.addMode(mode, constraints);
        }

        if(result) {
            modes.add(mode);
        }
        return result;
    }
    
    // XXX
    public boolean addModeToSide(ModeImpl mode, ModeImpl attachMode, String side) {
        if(modes.contains(mode)) {
            return false;
        }

        boolean result;
        // XXX PENDING
        if(mode.getKind() == Constants.MODE_KIND_EDITOR) {
            result = editorSplitSubModel.getEditorArea().addModeToSide(mode, attachMode, side);
        } else {
            result = editorSplitSubModel.addModeToSide(mode, attachMode, side);
        }

        if(result) {
            modes.add(mode);
        }
        return result;
    }
    
    // XXX
    public boolean addModeAround(ModeImpl mode, String side) {
        if(modes.contains(mode)) {
            return false;
        }

        boolean result;
        // XXX PENDING
        if(mode.getKind() == Constants.MODE_KIND_EDITOR) {
            result = false;
        } else {
            result = editorSplitSubModel.addModeAround(mode, side);
        }

        if(result) {
            modes.add(mode);
        }
        return result;
    }
    
    // XXX
    public boolean addModeAroundEditor(ModeImpl mode, String side) {
        if(modes.contains(mode)) {
            return false;
        }

        boolean result;
        // XXX PENDING
        if(mode.getKind() == Constants.MODE_KIND_EDITOR) {
            result = editorSplitSubModel.getEditorArea().addModeToSideRoot(mode, side);
        } else {
            result = editorSplitSubModel.addModeAroundEditor(mode, side);
        }

        if(result) {
            modes.add(mode);
        }
        return result;
    }
    
    public boolean addModeSliding(ModeImpl mode, String side, Map<String,Integer> slideInSizes) {
        if(modes.contains(mode) || (mode.getKind() != Constants.MODE_KIND_SLIDING)) {
            return false;
        }

        slidingModes2Sides.put(mode, side);
        slidingSides2Modes.put(side, mode);
        
        modes.add(mode);
        
        if( null != slideInSizes ) {
            for( Iterator<String> i=slideInSizes.keySet().iterator(); i.hasNext(); ) {
                String tcId = i.next();
                this.slideInSizes.put( side+tcId, slideInSizes.get( tcId ) );
            }
        }
        
        return true;
    }

    public Map<String, Integer> getSlideInSizes(String side) {
        Map<String,Integer> res = new HashMap<String,Integer>( 5 );
        for( Iterator<String> i=slideInSizes.keySet().iterator(); i.hasNext(); ) {
            String key = i.next();
            if( key.startsWith( side ) ) {
                String tcId = key.substring( side.length() );
                Integer size = slideInSizes.get( key );
                res.put( tcId, size );
            }
        }
        return res;
    }
    
    public Map<TopComponent, Integer> getSlideInSizes(ModeImpl mode) {
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        TopComponent[] tcs = mode.getTopComponents();
        Map<TopComponent,Integer> res = new HashMap<TopComponent,Integer>( tcs.length );
        for( TopComponent tc : tcs ) {
            String tcId = wm.findTopComponentID( tc );
            Integer size = slideInSizes.get( mode.getSide() + tcId );
            if( null != size ) {
                res.put( tc, size );
            }
        }
        return res;
    }
    
    public void setSlideInSize(String side, TopComponent tc, int size) {
        if( null != tc && null != side ) {
            String tcId = WindowManagerImpl.getInstance().findTopComponentID(tc);
            slideInSizes.put( side+tcId, new Integer(size) );
        }
    }
    
    public boolean removeMode(ModeImpl mode) {
        int kind = mode.getKind();
        if (kind == Constants.MODE_KIND_SLIDING) {
            return true;
            // don't remove the sliding modes, to make dnd easier..
//            slidingSides2Modes.remove(side);
//            return slidingModes2Sides.remove(mode) != null;
        }
        modes.remove(mode);
        if (mode.equals(lastActiveEditorMode)) {
            lastActiveEditorMode = null;
        }
        if(mode.getKind() == Constants.MODE_KIND_EDITOR) {
            return editorSplitSubModel.getEditorArea().removeMode(mode);
        } else {
            return editorSplitSubModel.removeMode(mode);
        }
    }
    
    /** Sets active mode. */
    public boolean setActiveMode(ModeImpl activeMode) {
        if(activeMode == null || modes.contains(activeMode)) {
            this.activeMode = activeMode;
            if ((activeMode != null) && (activeMode.getKind() == Constants.MODE_KIND_EDITOR)) {
                if( activeMode.getState() != Constants.MODE_STATE_SEPARATED 
                        || !Switches.isOpenNewEditorsDocked() ) {
                    lastActiveEditorMode = activeMode;
                }
            }
            return true;
        }
        
        return false;
    }
    
    /** Gets active mode. */
    public ModeImpl getActiveMode() {
        return this.activeMode;
    }

    /** Gets last active editor mode. */
    public ModeImpl getLastActiveEditorMode() {
        return this.lastActiveEditorMode;
    }
    
    /** Sets maximized mode for editor components. */
    public boolean setEditorMaximizedMode(ModeImpl maximizedMode) {
        if(maximizedMode == null || modes.contains(maximizedMode)) {
            this.editorMaximizedMode = maximizedMode;
            return true;
        }
        
        return false;
    }
    
    /** Gets maximized mode for editor components. */
    public ModeImpl getEditorMaximizedMode() {
        return this.editorMaximizedMode;
    }
    
    /** Sets maximized mode for non-editor components. */
    public boolean setViewMaximizedMode(ModeImpl maximizedMode) {
        if(maximizedMode == null || modes.contains(maximizedMode)) {
            this.viewMaximizedMode = maximizedMode;
            return true;
        }
        
        return false;
    }
    
    /** Gets maximized mode for non-editor components. */
    public ModeImpl getViewMaximizedMode() {
        return this.viewMaximizedMode;
    }

    public Set<ModeImpl> getModes() {
        return new HashSet<ModeImpl>(modes);
    }
    
    public void setSplitWeights( ModelElement[] snapshots, double[] splitWeights ) {
        editorSplitSubModel.setSplitWeights( snapshots, splitWeights );
    }
    
    @Override
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode()) // NOI18N
            + "\n" + editorSplitSubModel; // NOI18N
    }

    /////////////////////////////////////////////
    // used when creating snapshot of this model.
    public ModeStructureSnapshot.ElementSnapshot createSplitSnapshot() {
        return editorSplitSubModel.createSplitSnapshot();
    }

    /** Set of mode element snapshots. */
    public Set<ModeStructureSnapshot.ModeSnapshot> createSeparateModeSnapshots() {
        Set<ModeStructureSnapshot.ModeSnapshot> s = 
                new HashSet<ModeStructureSnapshot.ModeSnapshot>();
        
        s.addAll(editorSplitSubModel.createSeparateSnapshots());
        
        return s;
    }
    
    public Set<ModeStructureSnapshot.SlidingModeSnapshot> createSlidingModeSnapshots() {
        Set<ModeStructureSnapshot.SlidingModeSnapshot> result = 
                new HashSet<ModeStructureSnapshot.SlidingModeSnapshot>();
        for (Map.Entry<ModeImpl, String> curEntry: slidingModes2Sides.entrySet()) {
            final ModeImpl key = curEntry.getKey();
            AbstractMap<TopComponent, Integer> lazy = new AbstractMap<TopComponent, Integer>() {
                Map<TopComponent, Integer> delegate;
                @Override
                public Set<Entry<TopComponent, Integer>> entrySet() {
                    if (delegate == null) {
                        delegate = getSlideInSizes(key);
                    }
                    return delegate.entrySet();
                }
            };

             result.add(new ModeStructureSnapshot.SlidingModeSnapshot(
                    curEntry.getKey(), curEntry.getValue(), lazy
            ));
        }
        return result;
    }
    
    public void dock( ModeImpl prevMode, ModeImpl floatingMode ) {
        assert modes.contains( prevMode );
        assert modes.contains( floatingMode );
        SplitConstraint[] constraints = getModeConstraints( prevMode );
        boolean editorMode = prevMode.getKind() == Constants.MODE_KIND_EDITOR;
        if( editorMode ) {
            editorSplitSubModel.getEditorArea().addMode(floatingMode, constraints);
            removeMode( prevMode );
            editorSplitSubModel.removeMode( floatingMode );
        } else {
            editorSplitSubModel.removeMode(floatingMode);
            editorSplitSubModel.addMode(floatingMode, constraints);
            removeMode( prevMode );
        }
    }
    
    ////////////////////////////////////////////

    
    //////////////////////////////
    // Controller updates >>
    
    public ModeImpl getModeForOriginator(ModelElement originator) {
        ModeImpl mode = editorSplitSubModel.getModeForOriginator(originator);
        
        if(modes.contains(mode)) {
            return mode;
        } else {
            return null;
        }
    }
    
    // Controller updates <<
    ///////////////////////////////

}

