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
/*
 * VeryComplexListDataEvent.java
 *
 * Created on November 6, 2003, 10:28 AM
 */

package org.netbeans.swing.tabcontrol.event;

import org.netbeans.swing.tabcontrol.TabData;

import java.util.Arrays;

/**
 * Event which provides granular data on setTabs() events which may contain
 * arbitrary differences, moves, additions and removals of array contents (or no
 * changes at all).
 * <p>
 * This event class is used in the case of calls to TabDataModel.setTabs(),
 * where one array of TabData objects is replaced with a different array of
 * TabData objects, which may contain additions, removals, deletions or moves.
 * The heavy lifting is done by <code>ArrayDiff</code>, which provides lists of
 * the affected indices for those things that are added/removed/changed/
 * deleted.
 * <p>
 * Note that this class should eventually be merged with ComplexListDataEvent, along with
 * some normalization of how things are done - it was written for expedience, not beauty.
 *
 * @author Tim Boudreau
 * @see org.netbeans.swing.tabcontrol.event.ArrayDiff
 */
public final class VeryComplexListDataEvent extends ComplexListDataEvent {
    TabData[] old, nue;
    
    //XXX, probably the structure of ComplexListDataEvent should eventually
    //be modified to work more like this; the question is if it's killing
    //a mosquito with a sledgehammer for simple changes - most changes will
    //be simple, after all.
    
    /**
     * Creates a new instance of VeryComplexListDataEvent
     */
    public VeryComplexListDataEvent(Object source, TabData[] old,
                                    TabData[] nue) {
        super(source, ITEMS_CHANGED, -1, -1);
        this.old = old;
        this.nue = nue;
    }

    /**
     * Returns an ArrayDiff object if the two arrays this event was created with
     * are not identical
     */
    public ArrayDiff getDiff() {
        return ArrayDiff.createDiff(old, nue);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("VeryComplexListEvent - old array: ");
        sb.append(Arrays.asList(old));
        sb.append(" new array: ");
        sb.append(Arrays.asList(nue));
        sb.append(" diff: ");
        sb.append(getDiff());
        return sb.toString();
    }

    private static final void arr2str(Object[] o, StringBuffer sb) {
        for (int i = 0; i < o.length; i++) {
            sb.append(o[i]);
            if (i != o.length - 1) {
                sb.append(","); //NOI18N
            }
        }
    }

}
