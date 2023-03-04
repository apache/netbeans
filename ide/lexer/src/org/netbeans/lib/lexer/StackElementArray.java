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
package org.netbeans.lib.lexer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Array of stack trace elements may be used for logging of unique stack traces only
 * (for frequent problematic code invocations).
 *
 * @author Miloslav Metelka
 */
public class StackElementArray {

    public static Set<StackElementArray> createSet() {
        return Collections.synchronizedSet(new HashSet<StackElementArray>());
    }

    public static boolean addStackIfNew(Set<StackElementArray> stacks, int stackCompareSize) {
        StackTraceElement[] stackElems = new Exception().getStackTrace();
        int startIndex = 2; // For faster comparison cut of first two (this method and the caller's place)
        int endIndex = Math.min(stackElems.length, startIndex + stackCompareSize);
        StackTraceElement[] compareElems = new StackTraceElement[endIndex - startIndex];
        System.arraycopy(stackElems, startIndex, compareElems, 0, endIndex - startIndex);
        StackElementArray stackElementArray = new StackElementArray(compareElems);
        if (!stacks.contains(stackElementArray)) {
            stacks.add(stackElementArray);
            return true;
        }
        return false;
    }

    private final StackTraceElement[] stackTrace;

    private final int hashCode;

    private StackElementArray(StackTraceElement[] stackTrace) {
        this.stackTrace = stackTrace;
        int hc = 0;
        for (int i = 0; i < stackTrace.length; i++) {
            hc ^= stackTrace[i].hashCode();
        }
        hashCode = hc;
    }

    int length() {
        return stackTrace.length;
    }

    StackTraceElement element(int i) {
        return stackTrace[i];
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof StackElementArray)) {
            return false;
        }
        StackElementArray sea = (StackElementArray) obj;
        if (sea.length() != length()) {
            return false;
        }
        for (int i = 0; i < stackTrace.length; i++) {
            if (!element(i).equals(sea.element(i))) {
                return false;
            }
        }
        return true;
    }

}
