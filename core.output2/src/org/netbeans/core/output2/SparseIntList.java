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

package org.netbeans.core.output2;

import java.util.Arrays;

/**
 * A sparsely populated list of integers, internally implemented as two
 * arrays of integers - one containing sequential indices that have been entered,
 * and one the values associated with those integers.  Calls to get() for values
 * which have not been added will return the value of the nearest lower added 
 * entry that has been added plus the interval between that entry and the index
 * requested.  So, if you have such a list, it works as follows:
 * <p>
 * Entries are added with an associated index. get(idx) returns either value
 * corresponding to idx (if exists) or value corresponding to nearest lower index
 * + difference between requested index and nearest index of existing entry.
 * <p>
 * E.g. if you call add(10, 20), then get(0) == 1, get(9) == 10, get(10) == 20,
 * get(11) == 21, and so forth.
 * <p>
 * This is used to handle caching of logical line lengths in OutWriter -
 * if we have a 400000 line file, most lines will typically not need to be
 * word wrapped.  So we don't want to create a 400000 element int[] if most
 * of the time the number of wrapped lines will turn out to be 1 - instead,
 * only lines that are actually wrapped will have a line count added to a
 * SparseIntList; its get() behavior takes care of returning correct values
 * for the non-wrapped lines in between. 
 * <p>
 * SparseIntList contains entry (key, value) for each line which needs wrapping,
 * <i>key</i> is zero-based index of such line and <i>value</i> is the number
 * of logical (after wrapping) lines that correspond to physical (unwrapped) 
 * lines [0, key].
 *
 * @author  Tim Boudreau
 */
final class SparseIntList {
    private int[] keys;
    private int[] values;
    private int used = 0;
    private int lastAdded = Integer.MIN_VALUE;
    private int lastIndex = Integer.MIN_VALUE;
    
    /** Creates a new instance of IntMap */
    SparseIntList(int capacity) {
        allocArrays (capacity);
    }
    
    /** Add an integer to the list. The value must be > than the last value passed
     * to this methodd, and the index must be > than the last index passed to
     * this method.
     */
    public synchronized void add(int idx, int value) {
        if (value < lastAdded) {
            throw new IllegalArgumentException ("Contents must be presorted - " + //NOI18N
                "added value " + value + " is less than preceding " + //NOI18N
                "value " + lastAdded); //NOI18N
        }
        if (idx <= lastIndex) {
            throw new IllegalArgumentException ("Contents must be presorted - " + //NOI18N
                "added index " + idx + " is less than preceding " + //NOI18N
                "index " + lastIndex); //NOI18N
        }
        if (used >= keys.length) {
            growArrays();
        }
        values[used] = value;
        keys[used++] = idx;
        lastAdded = value;
        lastIndex = idx;
        
        // clear cached result
        lastGetIndex = lastGetResult = -1;
        lastGetNextKeyValue = lastGetNextKeyResult = -1;
    }
    
    public synchronized void updateLast(int idx, int value) {
        if (lastIndex != idx) {
            throw new IllegalArgumentException("Last index: " + lastIndex + " idx: " + idx); //NOI18N
        }
        values[used - 1] = value;
        lastAdded = value;
        lastGetIndex = lastGetResult = -1;
        lastGetNextKeyValue = lastGetNextKeyResult = -1;
    }

    public synchronized void removeLast() {
        if (used < 1) {
            throw new IllegalStateException("Cannot remove last, list is empty"); //NOI18N
        }
        used--;
        if (used > 0) {
            lastAdded = values[used - 1];
            lastIndex = keys[used - 1];
        } else {
            lastAdded = lastIndex = Integer.MIN_VALUE;
        }
        lastGetIndex = lastGetResult = -1;
        lastGetNextKeyValue = lastGetNextKeyResult = -1;
    }
    
    int lastAdded() {
        return lastAdded;
    }
    
    int lastIndex() {
        return lastIndex;
    }
    
    private void allocArrays (int size) {
        keys = new int[size];
        values = new int[size];
        //Fill it with Integer.MAX_VALUE so binarySearch works properly (must
        //be sorted, cannot have 0's after the actual data
        Arrays.fill(keys, Integer.MAX_VALUE);
        Arrays.fill(values, Integer.MAX_VALUE);
    }


    /** Caches the last requested get value. Often we will be called repeatedly 
     * for the same value - cache it */
    private int lastGetIndex = -1;
    /** Caches the last requested result for the same reasons. */
    private int lastGetResult;    
    
    /**
     * Get an entry in the list.  If the list is empty, it will simply return
     * (index+1) value; if the index is lower than the first entry entered
     * by a call to add (index, value) in the list, it will do the same.
     * <p>
     * If the index is greater than an added value's index, the return result
     * will be the value of that index + the requested index minus the added
     * index.
     */
    public synchronized int get(int index) {
        if (index < 0) {
            return 0;
        }
        
        if ((used == 0) || (used > 0 && index < keys[0])) {
            return index + 1;
        }

        if (index == lastGetIndex) {
            return lastGetResult;
        } else {
            lastGetIndex = index;
        }
        
        //First, see if we have a real entry for this index - if add() was
        //called passing this exact index as a value
        int pos = Arrays.binarySearch(keys, index);
        if (pos >= 0) {  // real entry
            return lastGetResult = values[pos];     
        } else {
            pos = -pos - 2;         // nearest element
            return lastGetResult = values[pos] + index - keys[pos];
        }
    }
    
    
    /** Caches the last requested getNextKey() value. Often we will be called 
     * repeatedly for the same value - cache it */
    private int lastGetNextKeyValue = -1;
    /** Caches the last requested result for the same reasons. */
    private int lastGetNextKeyResult;
    
    /** Finds key which is next to key corresponding to supplied value, 
     *  if value does not exit, the key is interpolated in similar manner as in get().
     *  This is used to locate physical line which corresponds to logical line
     */ 
    public synchronized int getNextKey(int val) {
        if (val < 0) {
            return 0;
        }
        if (used == 0) {
            return val;
        }        
        if (used > 0 && val < values[0]) {
            return val < keys[0] ? val : keys[0];
        }
        
        if (val == lastGetNextKeyValue) {
            return lastGetNextKeyResult;
        } else {
            lastGetNextKeyValue = val;
        }

        int pos = Arrays.binarySearch(values, val);
        if (pos < 0) {
            pos = -pos - 1;
            int key = val - values[pos - 1] + keys[pos - 1] + 1;
            return lastGetNextKeyResult = pos < used ? Math.min(key, keys[pos]) : key;
        } else {
            while (pos >= 0 && pos + 1 < values.length
                    && values[pos] == values[pos + 1]) {
                pos++;
            }
            return lastGetNextKeyResult = keys[pos] + 1;
        }
    }
    
    /** Finds key for supplied value, if value does not exit, the key is 
     *  interpolated in similar manner as in get()
     */ 
    public synchronized int getKey(int val) {
        if (val < 0) {
            return 0;
        }
        if (used == 0) {
            return val - 1;
        }
        
        if (used > 0 && val < values[0]) {
            return val < keys[0] ? val - 1 : keys[0];
        }

        int pos = Arrays.binarySearch(values, val);
        if (pos < 0) {
            pos = -pos - 1;
            int key = val - values[pos - 1] + keys[pos - 1];
            return pos < used ? Math.min(key, keys[pos]) : key;
        } else {
            return keys[pos];
        }
    }    

    /**
     * Grow the arrays we're using to store keys/values
     */
    private void growArrays() {
        int[] oldkeys = keys;
        int[] oldvals = values;
        allocArrays(Math.round(keys.length * 1.5f));
        System.arraycopy(oldkeys, 0, keys, 0, oldkeys.length);
        System.arraycopy(oldvals, 0, values, 0, oldvals.length);
    }
    
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer ("SparseIntList ["); //NOI18N
        result.append ("used="); //NOI18N
        result.append (used);
        result.append (" capacity="); //NOI18N
        result.append (keys.length);
        result.append (" keyValuePairs:"); //NOI18N
        for (int i=0; i < used; i++) {
            result.append (keys[i]);
            result.append (':'); //NOI18N
            result.append (values[i]);
            if (i != used-1) {
                result.append(','); //NOI18N
            }
        }
        result.append (']');
        return result.toString();
    }
}
