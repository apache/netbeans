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

package org.netbeans.editor.view.spi;

import java.io.Serializable;

/**
 * Immutable analogy of the {@link java.awt.Insets} based
 * on floats.
 * <br>
 * Any view implementation may benefit from presence
 * of this class.
 *
 * <p>
 * As the views of a same type should possibly use the same
 * insets there is a potential for sharing
 * of <code>ViewInsets</code> instances.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class ViewInsets implements Serializable {

    public static final ViewInsets ZERO_INSETS = new ViewInsets(0, 0, 0, 0);
    
    private float top;
    
    private float left;
    
    private float bottom;
    
    private float right;
    
    /**
     * Creates and initializes a new <code>ViewInsets</code> object with the
     * specified top, left, bottom, and right insets.
     * @param       top   the inset from the top.
     * @param       left   the inset from the left.
     * @param       bottom   the inset from the bottom.
     * @param       right   the inset from the right.
     */
    public ViewInsets(float top, float left, float bottom, float right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    public float getTop() {
        return top;
    }
    
    public float getLeft() {
        return left;
    }
    
    public float getBottom() {
        return bottom;
    }
    
    public float getRight() {
        return right;
    }

    public float getLeftRight() {
        return left + right;
    }
    
    public float getTopBottom() {
        return top + bottom;
    }

    /**
     * Checks whether two float insets objects are equal. Two instances
     * of <code>ViewInsets</code> are equal if the four float values
     * of the fields <code>top</code>, <code>left</code>,
     * <code>bottom</code>, and <code>right</code> are all equal.
     * @return      <code>true</code> if the two float insets are equal;
     *                          otherwise <code>false</code>.
     */
    public boolean equals(Object obj) {
        if (obj instanceof ViewInsets) {
            ViewInsets insets = (ViewInsets)obj;
            return ((top == insets.top) && (left == insets.left) &&
            (bottom == insets.bottom) && (right == insets.right));
        }
        return false;
    }
    
    /**
     * Returns the hash code for this Insets.
     *
     * @return    a hash code for this Insets.
     */
    public int hashCode() {
        float sum1 = left + bottom;
        float sum2 = right + top;
        float val1 = sum1 * (sum1 + 1)/2 + left;
        float val2 = sum2 * (sum2 + 1)/2 + top;
        int sum3 = (int)(val1 + val2);
        return sum3 * (sum3 + 1)/2 + (int)val2;
    }
    
    /**
     * Returns a string representation of this <code>Insets</code> object.
     * This method is intended to be used only for debugging purposes, and
     * the content and format of the returned string may vary between
     * implementations. The returned string may be empty but may not be
     * <code>null</code>.
     *
     * @return  a string representation of this <code>Insets</code> object.
     */
    public String toString() {
        return getClass().getName()
            + "[top="  + top + ",left=" + left // NOI18N
            + ",bottom=" + bottom + ",right=" + right + "]"; // NOI18N
    }
    
}
