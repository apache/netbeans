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
