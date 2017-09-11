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

