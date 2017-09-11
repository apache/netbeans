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

package org.netbeans.lib.editor.view;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.View;
import org.netbeans.editor.view.spi.ViewLayoutState;

/**
 * Implementation of children for {@link GapLineView}.
 *
 * <p>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

class GapLineViewChildren extends GapBoxViewChildren {

    /**
     * Value of maximal preferred ascent along the minor axis.
     */
    private float maxMinorAxisPrefAscent; // 52

    /**
     * Index of the child wrapping the view that has a maximal descent
     * along the minor axis among all the children.
     */
    private int maxMinorAxisPrefDescentChildIndex = -1; // 56
    
    /**
     * Value of maximal preferred descent along the minor axis.
     */
    private float maxMinorAxisPrefDescent; // 60


    /** List of rows that is non-null in case the line has to be wrapped */
    private RowList rowList; // 64
    

    GapLineViewChildren(GapBoxView view) {
        super(view);
    }
    
    /**
     * Compute layout information of the children along the minor axis.
     * This implementation does wrapped-line layout 
     */
    public void minorAxisLayout() {
        // Find child states with maximal prefered and minimum spans
        // along the minor axis

        int childCount = getChildCount();
        int maxMinorPrefAscentIndex = -1;
        int maxMinorPrefDescentIndex = -1;
        float maxMinorPrefAscentValue = 0f;
        float maxMinorPrefDescentValue = 0f;

        for (int i = 0; i < childCount; i++) {
            ViewLayoutState child = getChild(i);
            float span = child.getLayoutMinorAxisPreferredSpan();
            float alignment = child.getLayoutMinorAxisAlignment();
            float ascent = span * alignment;
            float descent = span - ascent;

            if (ascent > maxMinorPrefAscentValue) {
                maxMinorPrefAscentIndex = i;
                maxMinorPrefAscentValue = ascent;
            }

            if (descent > maxMinorPrefDescentValue) {
                maxMinorPrefDescentIndex = i;
                maxMinorPrefDescentValue = descent;
            }

        }

        float maxMinorPrefSpanValue = maxMinorPrefAscentValue + maxMinorPrefDescentValue;
        setMinorAxisPreferredSpan(maxMinorPrefSpanValue);
        setMaxMinorAxisPrefAscent(maxMinorPrefAscentValue);
        setMaxMinorAxisPrefDescent(maxMinorPrefDescentValue);
        setMaxMinorAxisPrefAscentChildIndex(maxMinorPrefAscentIndex);
        setMaxMinorAxisPrefDescentChildIndex(maxMinorPrefDescentIndex);
    }
    
    private float getPrefAscent(ViewLayoutState child) {
        float span = child.getLayoutMinorAxisPreferredSpan();
        float alignment = child.getLayoutMinorAxisAlignment();
        return span * alignment;
    }
    
    private float getMaxMinorAxisPrefAscent() {
        return maxMinorAxisPrefAscent;
    }
    
    private void setMaxMinorAxisPrefAscent(float maxMinorAxisPrefAscent) {
        this.maxMinorAxisPrefAscent = maxMinorAxisPrefAscent;
    }

    private int getMaxMinorAxisPrefAscentChildIndex() {
        // reuse variable for preferred span
        return super.getMaxMinorAxisPreferredSpanChildIndex();
    }
    
    private void setMaxMinorAxisPrefAscentChildIndex(int maxMinorAxisPrefAscentChildIndex) {
        // reuse variable for preferred span
        super.setMaxMinorAxisPreferredSpanChildIndex(maxMinorAxisPrefAscentChildIndex);
    }
    
    private float getPrefDescent(ViewLayoutState child) {
        float span = child.getLayoutMinorAxisPreferredSpan();
        float alignment = child.getLayoutMinorAxisAlignment();
        return span * (1 - alignment);
    }
    
    private float getMaxMinorAxisPrefDescent() {
        return maxMinorAxisPrefDescent;
    }
    
    private void setMaxMinorAxisPrefDescent(float maxMinorAxisPrefDescent) {
        this.maxMinorAxisPrefDescent = maxMinorAxisPrefDescent;
    }

    private int getMaxMinorAxisPrefDescentChildIndex() {
        return maxMinorAxisPrefDescentChildIndex;
    }
    
    private void setMaxMinorAxisPrefDescentChildIndex(int maxMinorAxisPrefDescentChildIndex) {
        this.maxMinorAxisPrefDescentChildIndex = maxMinorAxisPrefDescentChildIndex;
    }

    int getMaxMinorAxisPrefSpanChildIndex() {
        throw new IllegalStateException("Should never be called"); // NOI18N
    }
    
    void setMaxMinorAxisPrefSpanChildIndex(int maxMinorAxisPrefSpanChildIndex) {
        throw new IllegalStateException("Should never be called"); // NOI18N
    }

    protected void replaceUpdateIndexes(int index, int removeLength, int insertLength,
    int neighborIndex, int neighborIndexAfterReplace, ViewLayoutState neighbor) {
        
        boolean minorAxisChanged = false;
        int endRemoveIndex = index + removeLength;
        /* Check whether any of the removed children
         * are either maxPref, maxMin.
         * If so replace them by the neighbor where appropriate.
         * Set parent of each remove child to null.
         */
/*        int ind = getMaxMinorAxisPrefSpanChildIndex();
        if (ind >= endRemoveIndex) { // must update by diff
            setMaxMinorAxisPrefSpanChildIndex(ind + insertLength - removeLength);
        } else if (ind >= index) { // in removed area -> need to change to neighbor
            if (neighbor == null || neighbor.getLayoutMinorAxisPreferredSpan()
                < view.getMinorAxisPreferredSpan()
            ) {
                minorAxisChanged = true; // minor axis spans must be recomputed
            }
            setMaxMinorAxisPrefSpanChildIndex(neighborIndexAfterReplace);
        }
  */          
            

        int ind = getMaxMinorAxisPrefAscentChildIndex();
        if (ind >= endRemoveIndex) {
            setMaxMinorAxisPrefAscentChildIndex(ind + insertLength - removeLength);
        } else if (ind >= index) {
            float neighborAscent = getPrefAscent(neighbor);
            if (neighbor == null ||  neighborAscent < getMaxMinorAxisPrefAscent()) {
                minorAxisChanged = true;
            }
            setMaxMinorAxisPrefAscentChildIndex(neighborIndexAfterReplace);
        }
        
        ind = getMaxMinorAxisPrefDescentChildIndex();
        if (ind >= endRemoveIndex) {
            setMaxMinorAxisPrefDescentChildIndex(ind + insertLength - removeLength);
        } else if (ind >= index) {
            float neighborDescent = getPrefDescent(neighbor);
            if (neighbor == null ||  neighborDescent < getMaxMinorAxisPrefDescent()) {
                minorAxisChanged = true;
            }
            setMaxMinorAxisPrefDescentChildIndex(neighborIndexAfterReplace);
        }

        if (minorAxisChanged) {
            view.markMinorAxisPreferenceChanged();
        }
    }

    protected void minorAxisPreferenceChanged(ViewLayoutState child, int childIndex) {
        boolean minorAxisChanged = false;

        float span = child.getLayoutMinorAxisPreferredSpan();
        float alignment = child.getLayoutMinorAxisAlignment();
        float ascent = span * alignment;
        float descent = span - ascent;

        if (getMaxMinorAxisPrefAscentChildIndex() == -1
            || ascent > getMaxMinorAxisPrefAscent()
        ) {
            setMaxMinorAxisPrefAscent(ascent);
            setMaxMinorAxisPrefAscentChildIndex(childIndex);
            minorAxisChanged = true;
        }

        if (getMaxMinorAxisPrefDescentChildIndex() == -1
            || descent > getMaxMinorAxisPrefDescent()
        ) {
            setMaxMinorAxisPrefDescent(descent);
            setMaxMinorAxisPrefDescentChildIndex(childIndex);
            minorAxisChanged = true;
        }
        
        
        if (minorAxisChanged) {
            setMinorAxisPreferredSpan(getMaxMinorAxisPrefAscent()
                + getMaxMinorAxisPrefDescent());

            view.markMinorAxisPreferenceChanged();
        }
    }
    
    public float getChildMinorAxisOffset(int childIndex) {
        ViewLayoutState child = getChild(childIndex);
        float minorAxisSpan = view.getMinorAxisAssignedSpan();
        float childMinorMaxSpan = child.getLayoutMinorAxisMaximumSpan();
        if (childMinorMaxSpan < minorAxisSpan) {
            // can't make the child to fill the whole span, so align it
            float align = child.getLayoutMinorAxisAlignment();
            // Use baseline layout
            float baseline = getMaxMinorAxisPrefAscent();
            float span = child.getLayoutMinorAxisPreferredSpan();
            float alignment = child.getLayoutMinorAxisAlignment();
            float ascent = span * alignment;
            return baseline - ascent;
        }

        return 0f;
    }
    
    private int getRowCount() {
        return (rowList != null) ? rowList.size() : 0;
    }
    
    private Row getRow(int rowIndex) {
        return (Row)rowList.get(rowIndex);
    }

    /**
     * Return row index that corresponds to the child index.
     * <br>
     * Search in valid rows only.
     * @return &gt;=0 and &lt;<code>lastValidRowIndex</code>
     *  or <code>lastValidRowIndex + 1</code> in case the searched
     *  child is above the valid area.
     */
    private int getRowIndex(int childIndex) {
        // binary search
        int low = 0;
        /* can only search up to last valid row index
         * because the indexes of invalid lines have unpredictable values
         * which could confuse the binary search
         * and in worst case make infinite loop.
         */
        int high = rowList.lastValidRowIndex;
        
        while (low <= high) {
            int mid = (low + high) / 2;
            Row midRow = getRow(mid);
            
            if (midRow.endChildIndex <= childIndex) { // above the mid row
                low = mid + 1;
            } else if (midRow.startChildIndex > childIndex) { // below the mid row
                high = mid - 1;
            } else {
                // child index inside the row
                return mid;
            }
        }
        
        return low; // will be lastValidRowIndex
    }

    /**
     * Data of one visual row when line-wrapping is used.
     *
     * <p>
     * The views that have to be split are logically included
     * into the row where they start.
     */
    class Row {
        
        /**
         * Index of the first child that starts
         * on this row. If the whole row is covered
         * with Possible presence of fragment
         * of the view starting at previous row stored
         * in <code>rowStartFragment</code> has no effect on this value.
         */
        int startChildIndex;
        
        /**
         * Index of the last child included fully or partly included on this row.
         * In case the view has to be split 
         */
        int endChildIndex;
        
        /**
         * If there is a part of the view starting at one of the previous lines
         * continuing to this line then this is absolute offset along major axis
         * at which the part at the begining
         * of this line starts with.
         */
        float beforeStartChildMajorOffset;
        
        /**
         * Layout offset along the minor axis at which this row
          * is located.
         */
        float minorAxisOffset;
        
        /**
         * Span of this row along the minor axis.
         */
        float minorAxisSpan;
        
    }
    
    class RowList extends ArrayList {
        
        int lastValidRowIndex;
        
    }
    
}
