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

package org.netbeans.lib.profiler.results.cpu.marking;


/**
 *
 * @author Jaroslav Bachorik
 */
public class CharStack {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private char[] data;
    private float loadFactor;
    private int maxCapacity;
    private int stackPointer;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of CharStack */
    public CharStack() {
        maxCapacity = 10;
        stackPointer = -1;
        loadFactor = 1.75f;

        data = new char[maxCapacity];
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public synchronized boolean isEmpty() {
        return stackPointer == -1;
    }

    public synchronized void clear() {
        stackPointer = -1;
    }

    public synchronized char peek() throws IllegalStateException {
        if (isEmpty()) {
            throw new IllegalStateException();
        }

        return data[stackPointer];
    }

    public synchronized char pop() throws IllegalStateException {
        if (isEmpty()) {
            throw new IllegalStateException();
        }

        return data[stackPointer--];
    }

    public synchronized void push(char chr) {
        if (stackPointer >= (maxCapacity - 1)) {
            int newCapacity = (int) ((float) maxCapacity * loadFactor);
            char[] newData = new char[newCapacity];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
            maxCapacity = newCapacity;
            newData = null;
        }

        data[++stackPointer] = chr;
    }
}
