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
package org.netbeans.core.windows.view.dnd;

import java.awt.Component;
import java.awt.Rectangle;
import javax.swing.SwingUtilities;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.Switches;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.ui.ModeComponent;
import org.openide.windows.TopComponent;

/**
 * A common wrapper for TopComponent or the whole Mode being dragged to a new location.
 * 
 * @author S. Aubrecht
 * 
 * @since 2.30
 */
public final class TopComponentDraggable {

    private final TopComponent tc;
    private final ModeImpl mode;
    
    public TopComponentDraggable( TopComponent tc ) {
        this( tc, ( ModeImpl ) WindowManagerImpl.getInstance().findMode( tc ) );
    }
    
    TopComponentDraggable( TopComponent tc, ModeImpl mode ) {
        assert null != tc;
        assert null != mode;
        this.tc = tc;
        this.mode = mode;
    }
    
    public TopComponentDraggable( ModeImpl mode ) {
        this.tc = null;
        assert null != mode;
        this.mode = mode;
    }
    
    /**
     * @return Mode kind of the originating mode when just a single TopComponent
     * is being dragged or the kind of the mode that is being dragged.
     */
    public int getKind() {
        return mode.getKind();
    }
    
    /**
     * @return The whole mode being dragged or the originating mode when just a single
     * TopComponent is being dragged.
     */
    public ModeImpl getMode() {
        return mode;
    }
    
    /**
     * @return True if this draggable is allowed to drop anywhere regardless of
     * editor/view mode kind.
     */
    public boolean isAllowedToMoveAnywhere() {
        boolean res = Constants.SWITCH_MODE_ADD_NO_RESTRICT || Switches.isMixingOfEditorsAndViewsEnabled();
        if( isTopComponentTransfer() )
            res |= WindowManagerImpl.getInstance().isTopComponentAllowedToMoveAnywhere(tc);
        return res;
    }

    boolean isUndockingEnabled() {
        if( isModeTransfer() ) {
            if( getKind() == Constants.MODE_KIND_EDITOR )
                return Switches.isEditorModeUndockingEnabled();
            return Switches.isViewModeUndockingEnabled();
        }
        return Switches.isTopComponentUndockingEnabled() && Switches.isUndockingEnabled( tc );
    }

    boolean isSlidingEnabled() {
        if( isModeTransfer() )
            return Switches.isModeSlidingEnabled();
        return Switches.isTopComponentSlidingEnabled() && Switches.isSlidingEnabled( tc );
    }

    /**
     * @return True if the whole mode is being dragged.
     */
    public boolean isModeTransfer() {
        return null == tc;
    }

    /**
     * @return Dragged TopComponent or null if the whole mode is being dragged.
     */
    public TopComponent getTopComponent() {
        return tc;
    }

    /**
     * @return True if just a single TopComponent is being dragged.
     */
    public boolean isTopComponentTransfer() {
        return null != tc;
    }

    Rectangle getBounds() {
        Rectangle bounds = null;
        TopComponent modeTC = getTopComponent();
        if( null == modeTC ) {
            modeTC = mode.getSelectedTopComponent();
            if( null == modeTC ) {
                TopComponent[] tcs = mode.getTopComponents();
                if( null != tcs && tcs.length > 0 )
                    modeTC = tcs[0];
            }
        }
        Component modeComp = ( Component ) SwingUtilities.getAncestorOfClass(ModeComponent.class, modeTC);
        if( modeComp != null ) {
            bounds = modeComp.getBounds();
        }
        return bounds;
    }
}
