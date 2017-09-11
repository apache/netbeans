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
package org.netbeans.modules.java.source.save;

/**
 * Represents a difference, as used in <code>Diff</code>. A difference consists
 * of two pairs of starting and ending points, each pair representing either the
 * "from" or the "to" collection passed to <code>Diff</code>. If an ending point
 * is -1, then the difference was either a deletion or an addition. For example,
 * if <code>getDeletedEnd()</code> returns -1, then the difference represents an
 * addition.
 */
class Difference {
    public static final int NONE = -1;
    
    /**
     * The point at which the deletion starts.
     */
    private int delStart = NONE;
    
    /**
     * The point at which the deletion ends.
     */
    private int delEnd = NONE;
    
    /**
     * The point at which the addition starts.
     */
    private int addStart = NONE;
    
    /**
     * The point at which the addition ends.
     */
    private int addEnd = NONE;
    
    /**
     * Creates the difference for the given start and end points for the
     * deletion and addition.
     */
    public Difference(int delStart, int delEnd, int addStart, int addEnd) {
        this.delStart = delStart;
        this.delEnd   = delEnd;
        this.addStart = addStart;
        this.addEnd   = addEnd;
    }
    
    /**
     * The point at which the deletion starts, if any. A value equal to
     * <code>NONE</code> means this is an addition.
     */
    public int getDeletedStart() {
        return delStart;
    }
    
    /**
     * The point at which the deletion ends, if any. A value equal to
     * <code>NONE</code> means this is an addition.
     */
    public int getDeletedEnd() {
        return delEnd;
    }
    
    /**
     * The point at which the addition starts, if any. A value equal to
     * <code>NONE</code> means this must be an addition.
     */
    public int getAddedStart() {
        return addStart;
    }
    
    /**
     * The point at which the addition ends, if any. A value equal to
     * <code>NONE</code> means this must be an addition.
     */
    public int getAddedEnd() {
        return addEnd;
    }
    
    /**
     * Sets the point as deleted. The start and end points will be modified to
     * include the given line.
     */
    public void setDeleted(int line) {
        delStart = Math.min(line, delStart);
        delEnd   = Math.max(line, delEnd);
    }
    
    /**
     * Sets the point as added. The start and end points will be modified to
     * include the given line.
     */
    public void setAdded(int line) {
        addStart = Math.min(line, addStart);
        addEnd   = Math.max(line, addEnd);
    }
    
    /**
     * Compares this object to the other for equality. Both objects must be of
     * type Difference, with the same starting and ending points.
     */
    public boolean equals(Object obj) {
        if (obj instanceof Difference) {
            Difference other = (Difference)obj;
            
            return (delStart == other.delStart &&
                    delEnd   == other.delEnd &&
                    addStart == other.addStart &&
                    addEnd   == other.addEnd);
        } else {
            return false;
        }
    }
    
    /**
     * Returns a string representation of this difference.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("del: [" + delStart + ", " + delEnd + "]");
        buf.append(" ");
        buf.append("add: [" + addStart + ", " + addEnd + "]");
        return buf.toString();
    }
    
}
