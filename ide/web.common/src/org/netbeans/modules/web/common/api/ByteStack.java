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
package org.netbeans.modules.web.common.api;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stack using byte array.
 * <p>
 * This class is not thread-safe.
 * @since 1.97
 */
public final class ByteStack {

    private static final Logger LOGGER = Logger.getLogger(ByteStack.class.getName());

    /**
     * Default initial size of the stack.
     */
    public static final byte DEFAULT_SIZE = 5;

    private byte[] data;
    private int index;


    /**
     * Creates a new stack with the default initial size.
     * @see #ByteStack(int)
     */
    public ByteStack() {
        this(DEFAULT_SIZE);
    }

    /**
     * Creates a new stack with the given initial size.
     * @param stackSize initial stack size
     * @see #ByteStack()
     */
    public ByteStack(int stackSize) {
        data = new byte[stackSize];
        index = -1;
    }

    /**
     * Returns {@code true} if this stack contains no elements.
     * @return {@code true} if this stack contains no elements
     */
    public boolean isEmpty() {
        return index == -1;
    }

    /**
     * Retrieves, but does not remove, the head of this stack.
     * @return the head of this stack
     * @throws ArrayIndexOutOfBoundsException if this stack is {@link #isEmpty() empty}
     */
    public int peek() {
        return data[index];
    }

    /**
     * Pops an element from this stack. In other words, removes and returns the first element of this stack.
     * @return the element at the front of this stack (which is the top of this stack)
     * @throws ArrayIndexOutOfBoundsException if this stack is {@link #isEmpty() empty}
     */
    public int pop() {
        int item = peek();
        index--;
        return item;
    }

    /**
     * Pushes an element onto this stack. In other words, inserts the element at the front of this stack.
     * @param element the element to push
     */
    public void push(int element) {
        index++;
        if (index == data.length) {
            ensureCapacity();
        }
        data[index] = (byte) element;
    }

    /**
     * Removes all of the elements from this stack.
     * The stack will be empty after this call returns.
     */
    public void clear() {
        index = -1;
    }

    /**
     * Returns the number of elements in this stack.
     * @return the number of elements in this stack
     */
    public int size() {
        return index + 1;
    }

    /**
     * Returns {@code true} if this stack contains the specified element.
     * @param element element to be checked for containment in this stack
     * @return {@code true} if this stack contains the specified element
     */
    public boolean contains(int element) {
        for (int i = 0; i <= index; i++) {
            if (data[i] == element) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a copy of this stack.
     * @return a copy of this stack
     */
    public ByteStack copyOf() {
        ByteStack copy = new ByteStack(size());
        copy.copyFrom(this);
        return copy;
    }

    /**
     * Copies data from the given stack to this stack.
     * @param stack stack to be copied data from
     */
    public void copyFrom(ByteStack stack) {
        while (stack.index >= data.length) {
            ensureCapacity();
        }
        index = stack.index;
        for (int i = 0; i <= stack.index; i++) {
            data[i] = stack.data[i];
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(50);
        for (int i = 0; i <= index; i++) {
            sb.append(" stack[").append(i).append("]= ").append(data[i]); // NOI18N
        }
        return sb.toString();
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ByteStack)) {
            return false;
        }

        ByteStack other = (ByteStack) obj;
        if (index != other.index) {
            return false;
        }

        for (int i = index; i >= 0; i--) {
            if (data[i] != other.data[i]) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + index;
        for (int i = index; i >= 0; i--) {
            hash = 31 * hash + data[i];
        }
        return hash;
    }

    private void ensureCapacity() {
        int length = data.length;
        LOGGER.log(Level.FINE, "ByteStack - increasing size: {0} by {1}", new Object[]{length, DEFAULT_SIZE});
        data = Arrays.copyOf(data, length + DEFAULT_SIZE);
    }

}
