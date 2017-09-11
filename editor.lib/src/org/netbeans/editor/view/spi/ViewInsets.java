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
