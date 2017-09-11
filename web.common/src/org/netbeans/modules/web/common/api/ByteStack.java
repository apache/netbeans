/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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

        if (obj == null || !(obj instanceof ByteStack)) {
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
