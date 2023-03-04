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

package org.netbeans.editor;

import java.util.Map;
import java.util.HashMap;

/** Support for comparing part of char array
* to hash map with strings as keys.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class StringMap extends java.util.HashMap {

    char[] testChars;

    int testOffset;

    int testLen;

    static final long serialVersionUID =967608225972123714L;
    public StringMap() {
        super();
    }

    public StringMap(int initialCapacity) {
        super(initialCapacity);
    }

    public StringMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public StringMap(Map t) {
        super(t);
    }

    public Object get(char[] chars, int offset, int len) {
        testChars = chars;
        testOffset = offset;
        testLen = len;
        Object o = get(this);
        testChars = null; // enable possible GC
        return o;
    }

    public boolean containsKey(char[] chars, int offset, int len) {
        testChars = chars;
        testOffset = offset;
        testLen = len;
        boolean b = containsKey(this);
        testChars = null; // enable possible GC
        return b;
    }

    public Object remove(char[] chars, int offset, int len) {
        testChars = chars;
        testOffset = offset;
        testLen = len;
        Object o = remove(this);
        testChars = null;
        return o;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof String) {
            String s = (String)o;
            if (testLen == s.length()) {
                for (int i = testLen - 1; i >= 0; i--) {
                    if (testChars[testOffset + i] != s.charAt(i)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        if (o instanceof char[]) {
            char[] chars = (char[])o;
            if (testLen == chars.length) {
                for (int i = testLen - 1; i >= 0; i--) {
                    if (testChars[testOffset + i] != chars[i]) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        return false;
    }

    public int hashCode() {
        int h = 0;
        char[] chars = testChars;
        int off = testOffset;

        for (int i = testLen; i > 0; i--) {
            h = 31 * h + chars[off++];
        }

        return h;
    }

}
