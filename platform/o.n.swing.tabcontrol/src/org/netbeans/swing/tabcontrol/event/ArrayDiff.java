/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.swing.tabcontrol.event;

import org.netbeans.swing.tabcontrol.TabData;

import java.util.*;

/*
 * ArrayDiff.java
 *
 * Created on November 5, 2003, 12:44 PM
 */

/**
 * Class representing a diff of two arrays.  Note that it is
 * <strong>not</strong> designed to work with arrays which contain the same
 * element more than one time - in that case, the results are undefined.
 * <p>Note the current implementation is unoptimized and fairly brute force.
 *
 * @author Tim Boudreau
 */
public final class ArrayDiff {
    /**
     * The old array
     */
    private TabData[] old;
    /**
     * The new array
     */
    private TabData[] nue;
    
    //XXX all of this could be implemented more efficiently with a single
    //loop to calculate all statistics and so forth.  The approach is algorithmically
    //inelegant and brute force. To do that would significantly
    //increase the complexity of the code, but it could be done later as an
    //optimization
    
    /**
     * Creates a new instance of ArrayDiff
     */
    private ArrayDiff(TabData[] old, TabData[] nue) {
        this.old = old;
        this.nue = nue;
        if (nue == null || old == null) {
            throw new NullPointerException(old == null && nue == null ?
                                           "Both arrays are null" :
                                           old == null ?
                                           "Old array is null" :
                                           "New array is null");
        }
    }

    /**
     * Get the array representing the old state
     */
    public TabData[] getOldData() {
        return old;
    }

    /**
     * Get the array representing the new state
     */
    public TabData[] getNewData() {
        return nue;
    }

    /**
     * Returns an ArrayDiff object if the two arrays are not the same, or null
     * if they are
     */
    public static ArrayDiff createDiff(TabData[] old, TabData[] nue) {
        if (!Arrays.equals(old, nue)) {
            return new ArrayDiff(old, nue);
        } else {
            return null;
        }
    }

    private Set<Integer> deleted = null;

    /**
     * Returns the indices of objects in the old array which are not present in
     * the new array.  The resulting array's size will be that of the old array
     */
    public Set<Integer> getDeletedIndices() {
        if (deleted == null) {
            HashSet<TabData> set = new HashSet<TabData>(Arrays.asList(nue));
            HashSet<Integer> results = new HashSet<Integer>(old.length);
            for (int i = 0; i < old.length; i++) {
                if (!set.contains(old[i])) {
                    results.add(i);
                }
            }
            deleted = results;
        }
        return deleted;
    }

    private Set<Integer> added = null;

    /**
     * Returns the indices of objects in the new array which are not present in
     * the old array
     */
    public Set<Integer> getAddedIndices() {
        if (added == null) {
            HashSet<TabData> set = new HashSet<TabData>(Arrays.asList(old));
            Set<Integer> results = new HashSet<Integer>(nue.length);
            for (int i = 0; i < nue.length; i++) {
                if (!set.contains(nue[i])) {
                    results.add(i);
                }
            }
            added = results;
        }
        return added;
    }

    /**
     * Returns the indices of objects which differ in any way between the new
     * and old array.  The size of the result is Math.max(old.length,
     * nue.length).
     */
    public Set<Integer> getChangedIndices() {
        //XXX can add similar caching as with deleted/added fields if it looks
        //to prove useful.  getDeletedIndices() and getAddedIndices() are called
        //more than once, and the computation can be expensive.
        int max = Math.max(nue.length, old.length);
        HashSet<Integer> results = new HashSet<Integer>(max);

        for (int i = 0; i < max; i++) {
            if (i < old.length && i < nue.length) {
                if (!old[i].equals(nue[i])) {
                    results.add(i);
                }
            } else {
                results.add(i);
            }
        }
        return results;
    }

    /**
     * Returns the indices of objects which were in the old array and are also
     * in the new array, but at a different index.  The indices returned are
     * indices into the old array.
     */
    public Set<Integer> getMovedIndices() {
        HashSet<TabData> set = new HashSet<TabData>(Arrays.asList(nue));
        HashSet<Integer> results = new HashSet<Integer>(old.length);

        for (int i = 0; i < old.length; i++) {
            boolean isPresent = set.contains(old[i]);
            if (isPresent) {
                boolean isMoved = (i < nue.length
                        && !nue[i].equals(old[i])) || i >= nue.length;
                if (isMoved) {
                    results.add(i);
                }
            }
        }
        return results;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<ArrayDiff: deleted indices: [");
        sb.append(outCol(getDeletedIndices()));
        sb.append("] added indices: [");
        sb.append(outCol(getAddedIndices()));
        sb.append("] changed indices: [");
        sb.append(outCol(getChangedIndices()));
        sb.append("] moved indices: [");
        sb.append(outCol(getChangedIndices()));
        sb.append("]>");
        return sb.toString();
    }

    private static String outCol(Collection c) {
        Iterator i = c.iterator();
        StringBuilder result = new StringBuilder();
        while (i.hasNext()) {
            Object o = i.next();
            result.append(o.toString());
            if (i.hasNext()) {
                result.append(",");
            }
        }
        return result.toString();
    }

    public boolean equals(Object o) {
        if (o instanceof ArrayDiff) {
            if (o == this) {
                return true;
            }
            TabData[] otherOld = ((ArrayDiff) o).getOldData();
            TabData[] otherNue = ((ArrayDiff) o).getNewData();
            return Arrays.equals(old, otherOld)
                    && Arrays.equals(nue, otherNue);
        }
        return false;
    }

    public int hashCode() {
        return arrayHashCode(old) ^ arrayHashCode(nue);
    }

    private static int arrayHashCode(Object[] o) {
        int result = 0;
        for (int i = 0; i < o.length; i++) {
            result += o[i].hashCode() ^ i;
        }
        return result;
    }
}
