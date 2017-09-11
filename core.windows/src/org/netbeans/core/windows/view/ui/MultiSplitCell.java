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
