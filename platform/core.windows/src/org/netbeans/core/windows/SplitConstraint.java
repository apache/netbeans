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

package org.netbeans.core.windows;

/**
 * Constraint class, which array designates constraints of mode in split structure.
 *
 * @author  Marek Slama
 */
public class SplitConstraint {

    /** Orientation of splitter */
    public final int orientation;

    /** Cell index. From TOP to BOTTOM or from LEFT to RIGHT respectivelly. */
    public final int index;

    /** Split weight in range from 0.0 to 1.0. It designates how much from split
     * takes this component if present in. */
    public final double splitWeight;

    /** Creates a new instance of SplitConstraint. */
    public SplitConstraint(int orientation, int index, double splitWeight) {
        this.orientation = orientation;
        this.index = index;
        this.splitWeight = splitWeight;
    }
    
    public String toString() {
        String o;
        if(orientation == Constants.VERTICAL) {
            o = "V"; // NOI18N
        } else if(orientation == Constants.HORIZONTAL) {
            o = "H"; // NOi18N
        } else {
            o = String.valueOf(orientation);
        }
        
        return "[" + o + ", " + index + ", " + splitWeight + "]"; // NOI18N
    }
    
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SplitConstraint) {
            SplitConstraint item = (SplitConstraint)obj;
            if (orientation == item.orientation
            && index == item.index
            && splitWeight == item.splitWeight) {
                return true;
            }
        }
        return false;
    }
    
    public int hashCode() {
        int hash = 17;
        hash = 37 * hash + orientation;
        hash = 37 * hash + index;
        long l = Double.doubleToLongBits(splitWeight);
        hash = 37 * hash + (int) (l ^ (l >>> 32));
        return hash;
    }
    
}

