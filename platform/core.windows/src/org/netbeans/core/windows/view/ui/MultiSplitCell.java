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

import java.awt.Component;
import org.netbeans.core.windows.Switches;
import org.netbeans.core.windows.view.ViewElement;

/**
 * A wrapper class for a component displayed in MultiSplitPane.
 */
class MultiSplitCell {

    private ViewElement view;
    //normalized resize weight, used internally only
    private double normalizedResizeWeight = 0.0;
    private double initialSplitWeight;
    //the size (widht or height) required by this component, used when resizing all split components
    private int requiredSize = -1;
    private boolean dirty = false;
    private boolean isHorizontalSplit;
    
    private static final int MINIMUM_POSSIBLE_SIZE = 10;
    
    MultiSplitCell( ViewElement view, double initialSplitWeight, boolean isHorizontalSplit ) {
        this.view = view;
        this.initialSplitWeight = initialSplitWeight;
        this.isHorizontalSplit = isHorizontalSplit;
    }

    public boolean equals( Object o ) {
        if( o instanceof MultiSplitCell ) {
            MultiSplitCell cell = (MultiSplitCell)o;
            return getComponent().equals( cell.getComponent() );
        }
        return super.equals( o );
    }
    
    boolean isDirty() {
        return dirty;
    }
    
    void setDirty( boolean isDirty ) {
        this.dirty = isDirty;
    }
    
    void maybeResetToInitialSize( int newSize ) {
        if( -1 == requiredSize ) {
            requiredSize = getSize();
            if( requiredSize <= 0 || requiredSize >= newSize ) {
                requiredSize = (int)(newSize * initialSplitWeight + 0.5);
            }
            requiredSize = Math.max( requiredSize, getMinimumSize() );
            dirty = true;
        }
    }
    
    double getResizeWeight() {
        return view.getResizeWeight();
    }
    
    Component getComponent() {
        return view.getComponent();
    }

    /**
     * @param dividerSize The width of splitter bar.
     * @return The minimum size of this cell. If this cell is a split cell then the
     * result is a sum of minimum sizes of all children cells.
     */
    int getMinimumSize() {
        int result = MINIMUM_POSSIBLE_SIZE;
        if( Switches.isSplitterRespectMinimumSizeEnabled() ) {
            if( isHorizontalSplit )
                result = getComponent().getMinimumSize().width;
            else
                result = getComponent().getMinimumSize().height;
        }
        if( result < MINIMUM_POSSIBLE_SIZE )
            result = MINIMUM_POSSIBLE_SIZE;
        return result;
    }
    
    int getRequiredSize() {
        if( -1 == requiredSize ) {
            if( isHorizontalSplit ) 
                return getComponent().getPreferredSize().width;
            return getComponent().getPreferredSize().height;
        }
        return requiredSize;
    }
    /**
     * Adjust cell's dimensions.
     */
    void layout( int x, int y, int width, int height ) {
        if( isHorizontalSplit ) {
            dirty |= x != getLocation() || requiredSize != width;
            requiredSize = width;
        } else {
            dirty |= y != getLocation() || requiredSize != height;
            requiredSize = height;
        }
        getComponent().setBounds( x, y, width, height );
    }
    
    void setRequiredSize( int newRequiredSize ) {
        dirty |= newRequiredSize != requiredSize;
        this.requiredSize = newRequiredSize;
    }
    
    int getLocation() {
        if( isHorizontalSplit )
            return getComponent().getLocation().x;
        return getComponent().getLocation().y;
    }
    
    int getSize() {
        if( isHorizontalSplit )
            return getComponent().getSize().width;
        return getComponent().getSize().height;
    }
    
    double getNormalizedResizeWeight() {
        return normalizedResizeWeight;
    }
    
    void setNormalizedResizeWeight( double newNormalizedResizeWeight ) {
        this.normalizedResizeWeight = newNormalizedResizeWeight;
    }
    
    ViewElement getViewElement() {
        return view;
    }
}
