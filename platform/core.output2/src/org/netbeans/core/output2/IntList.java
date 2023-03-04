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
 * IntList.java
 *
 * Created on March 21, 2004, 12:18 AM
 */

package org.netbeans.core.output2;

import java.util.Arrays;

/** A collections-like lineStartList of primitive integers.  Entries may be added only
 * in ascending order.  This is used to map lines to file offsets.
 *
 * @author  Tim Boudreau
 */
final class IntList {
    private int[] array;
    private int used = 0;
    private int lastAdded = Integer.MIN_VALUE;

    /** Creates a new instance of IntMap */
    IntList(int capacity) {
        array = allocArray (capacity);
    }
    
    /** Add an integer to the lineStartList.  Must be greater than the preceding value
     * or an exception is thrown. */
    public synchronized void add (int value) {
        if (used > 0 && array[used - 1] == value) {
            return;
        }
        if (value < lastAdded) {
            throw new IllegalArgumentException ("Contents must be presorted - " + //NOI18N
                "added value " + value + " is less than preceding " + //NOI18N
                "value " + lastAdded); //NOI18N
        }
        if (used >= array.length) {
            growArray();
        }
        array[used++] = value;
        lastAdded = value;
    }
    
    private int[] allocArray (int size) {
        int[] result = new int[size];
        //Fill it with Integer.MAX_VALUE so binarySearch works properly (must
        //be sorted, cannot have 0's after the actual data
        Arrays.fill(result, Integer.MAX_VALUE);
        return result;
    }
    
    public synchronized int get(int index) {
        if (index >= used) {
            throw new ArrayIndexOutOfBoundsException("List contains " + used 
                + " items, but tried to fetch item " + index);
        }
        return array[index];
    }
    
    public boolean contains (int val) {
        return Arrays.binarySearch(array, val) >= 0;
    }
    
    /** Return the <strong>index</strong> of the value closest to but lower than
     * the passed value */
    public int findNearest (int val) {
        if (size() == 0) {
            return -1;
        }
        int pos = Arrays.binarySearch(array, val);
        if (pos < 0) {
            pos = -pos - 2; 
        }
        return pos;
    }

    public int indexOf (int val) {
        int result = Arrays.binarySearch(array, val);
        if (result < 0) {
            result = -1;
        }
        if (result >= used) {
            result = -1;
        }
        return result;
    }
    
    
    public synchronized int size() {
        return used;
    }
    
    private void growArray() {
        int[] old = array;
        array = allocArray(Math.round(array.length * 2));
        System.arraycopy(old, 0, array, 0, old.length);
    }
    
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer ("IntList [");
        for (int i=0; i < used; i++) {
            result.append (i);
            result.append (':');
            result.append (array[i]);
            if (i != used-1) {
                result.append(',');
            }
        }
        result.append (']');
        return result.toString();
    }
    
    /**
     * Shift the list (to left). First {@code shift} items will be forgotten.
     * Each item can be decremented by {@code decrement}.
     *
     * @param shift How many items should be removed. Item at index
     * {@code shift} will be at index 0 after this operation.
     * @param decrement The value each item should be decremented by.
     */
    public synchronized void compact(int shift, int decrement) {
        if (shift < 0 || shift > used) {
            throw new IllegalArgumentException();
        }
        for (int i = shift; i < used; i++) {
            array[i - shift] = array[i] - decrement;
        }
        Arrays.fill(array, used - shift, used, Integer.MAX_VALUE);
        if (used > 0) {
            used -= shift;
            lastAdded = (used == 0) ? Integer.MIN_VALUE : lastAdded - decrement;
        }
    }

    public synchronized void shorten(int newSize) {
        if (newSize > used || newSize < 0) {
            throw new IllegalArgumentException();
        } else if (newSize < used) {
            lastAdded = newSize == 0 ? Integer.MIN_VALUE : array[newSize - 1];
            Arrays.fill(array, newSize, used, Integer.MAX_VALUE);
            used = newSize;
        }
    }
}
