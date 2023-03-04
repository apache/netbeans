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
/*
 * IntMap.java
 *
 * Created on March 29, 2004, 6:40 PM
 */

package org.netbeans.core.output2;

import java.util.Arrays;
import org.openide.util.Exceptions;

/**
 * Sparse array integer keyed map.  Similar to a standard Collections map,
 * but considerably more efficient for this purpose, it simply an array
 * if integer indices that have values and an array of objects mapped to
 * those indices.  Entries may be added only in ascending order, enabling
 * use of Arrays.binarySearch() to quickly locate the relevant entry.
 * <p>
 * Used to maintain the mapping between the (relatively few) OutputListeners
 * and their associated getLine numbers.
 *
 * @author  Tim Boudreau
 */
final class IntMap {
    private int[] keys = new int[] { Integer.MAX_VALUE, Integer.MAX_VALUE, 
        Integer.MAX_VALUE, Integer.MAX_VALUE};
        
    private Object[] vals = new Object[4];
    private int last = -1;
    
    /** Creates a new instance of IntMap */
    IntMap() {
    }
    
    public int first() {
        return isEmpty() ? -1 : keys[0];
    }
    
    public int nearest (int line, boolean backward) {
        if (isEmpty()) {
            return -1;
        }
        if (last == 0) {
            return keys[last];
        }
        if (line < keys[0]) {
            return backward ? keys[last] : keys[0];
        }
        if (line > keys[last]) {
            return backward ? keys[last] : keys[0];
        }
        int idx = Arrays.binarySearch(keys, line);
        if (idx < 0) {
            idx = -idx + (backward ? -2 :- 1);
            if (idx > last) {
                idx = backward ? last : 0;
            } else if (idx < 0) {
                idx = backward ? last : 0;
            }
        }
        return keys[idx];
    }

    public int[] getKeys () {
        if (last == -1) {
            return new int[0];
        }
        if (last == keys.length -1) {
            growArrays();
        }
        int[] result = new int[last+1];
        try {
            System.arraycopy (keys, 0, result, 0, last+1);
            return result;
        } catch (ArrayIndexOutOfBoundsException aioobe) { //XXX temp diagnostics
            ArrayIndexOutOfBoundsException e = new ArrayIndexOutOfBoundsException (
                "AIOOBE in IntMap.getKeys() - last = " + last + " keys: " + 
                i2s(keys) + " vals: " + Arrays.asList(vals) + " result length "
                + result.length);
            Exceptions.printStackTrace(e);
            return new int[0];
        }
    }

    /** Some temporary diagnostics re issue 48608 */
    private static String i2s (int[] arr) {
        StringBuffer sb = new StringBuffer(arr.length * 3);
        sb.append ('[');
        for (int i=0; i < arr.length; i++) {
            if (arr[i] != Integer.MAX_VALUE) {
                sb.append (arr[i]);
                sb.append (',');
            }
        }
        sb.append (']');
        return sb.toString();
    }
    
    public Object get (int key) {
        int idx = Arrays.binarySearch (keys, key);
        if (idx > -1 && idx <= last) {
            return vals[idx];
        }
        return null;
    }
    
    public void put (int key, Object val) {
        if (last >= 0) {
            if (keys[last] == key && vals[last] == val) {
                return;
            }
            assert key > keys[last]: "key=" + key + " last=" + keys[last];
        }
        if (last == keys.length - 1) {
            growArrays();
        }
        last++;
        keys[last] = key;
        vals[last] = val;
    }
    
    private void growArrays() {
        int newSize = keys.length * 2;
        int[] newKeys = new int[newSize];
        Object[] newVals = new Object[newSize];
        Arrays.fill (newKeys, Integer.MAX_VALUE); //So binarySearch works
        System.arraycopy (keys, 0, newKeys, 0, keys.length);
        System.arraycopy (vals, 0, newVals, 0, vals.length);
        keys = newKeys;
        vals = newVals;
    }
    
    /**
     * Get the key which follows the passed key, or -1.  Will wrap around 0.
     */
    public int nextEntry (int entry) {
        int result = -1;
        if (!isEmpty()) {
            int idx = Arrays.binarySearch (keys, entry);
            if (idx >= 0) {
                result = idx == keys.length -1 ? keys[0] : keys[idx+1];
            }
        }
        return result;
    }
    
    /**
     * Get the key which precedes the passed key, or -1.  Will wrap around 0.
     */
    public int prevEntry (int entry) {
        int result = -1;
        if (!isEmpty()) {
            int idx = Arrays.binarySearch (keys, entry);
            if (idx >= 0) {
                result = idx == 0 -1 ? keys[keys.length-1] : keys[idx-1];
            }
        }
        return result;
    }
    
    
    public boolean isEmpty() {
        return last == -1;
    }
    
    public int size() {
        return last + 1;
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("IntMap@") //NOI18N
                .append(System.identityHashCode(this));
        
        for (int i=0; i < size(); i++) {
            sb.append ("["); //NOI18N
            sb.append (keys[i]);
            sb.append (":"); //NOI18N
            sb.append (vals[i]);
            sb.append ("]"); //NOI18N
        }
        if (size() == 0) {
            sb.append ("empty"); //NOI18N
        }
        return sb.toString();
    }

    /**
     * Decrement keys in the map. Entries with negative keys will be removed.
     *
     * @param decrement Value the keys should be decremented by. Must be zero or
     * higher.
     */
    public void decrementKeys(int decrement) {

        if (decrement < 0) {
            throw new IllegalArgumentException();
        }

        int shift = Arrays.binarySearch(keys, decrement);
        if (shift < 0) {
            shift = -shift - 1;
        }

        for (int i = shift; i <= last; i++) {
            keys[i - shift] = keys[i] - decrement;
            vals[i - shift] = vals[i];
        }

        Arrays.fill(keys, last - shift + 1, last + 1, Integer.MAX_VALUE);
        last = last - shift;
    }
}
