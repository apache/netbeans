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

package org.netbeans.core.output2;

import java.util.Arrays;

/**
 * A collections-like lineStartList of primitive integers.
 */
final class IntListSimple {
    private int[] array;
    private int used = 0;

    /** Creates a new instance of IntMap */
    IntListSimple(int capacity) {
        array = new int[capacity];
    }
    
    public synchronized void add (int value) {
        if (used >= array.length) {
            growArray();
        }
        array[used++] = value;
    }
    
    public synchronized int get(int index) {
        if (index >= used) {
            throw new ArrayIndexOutOfBoundsException("List contains " + used 
                + " items, but tried to fetch item " + index);
        }
        return array[index];
    }
    
    public synchronized int size() {
        return used;
    }
    
    public void set(int index, int value) {
        if (index >= used) {
            throw new IndexOutOfBoundsException();
        } else {
            array[index] = value;
        }
    }

    public void shorten(int newSize) {
        if (newSize > used) {
            throw new IllegalArgumentException();
        } else {
            used = newSize;
        }
    }

    private void growArray() {
        int[] old = array;
        array = new int[Math.round(array.length * 2)];
        System.arraycopy(old, 0, array, 0, old.length);
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder ("IntListSimple [");
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
     * @param increment The value each item should be decremented by.
     */
    public synchronized void compact(int shift, int decrement) {
        if (shift < 0 || shift > used) {
            throw new IllegalArgumentException();
        }
        for (int i = shift; i < used; i++) {
            array[i - shift] = array[i] - decrement;
        }
        Arrays.fill(array, used - shift, used, Integer.MAX_VALUE);
        used -= shift;
    }
}
