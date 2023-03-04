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

package org.netbeans.lib.profiler.utils;


/**
 * A Vector of ints. Implements a subset of standard java.util.Vector class
 *
 * @author Misha Dmitriev
 */
public class IntVector {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private int[] vec;
    private int size;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public IntVector() {
        this(10);
    }

    public IntVector(int capacity) {
        vec = new int[capacity];
        size = 0;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void add(int val) {
        if (size == vec.length) {
            int[] oldVec = vec;
            vec = new int[oldVec.length * 2];
            System.arraycopy(oldVec, 0, vec, 0, oldVec.length);
        }

        vec[size++] = val;
    }

    public void clear() {
        size = 0;
    }

    public int get(int idx) {
        return vec[idx];
    }

    public int size() {
        return size;
    }
}
