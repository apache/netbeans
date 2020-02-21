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

package org.netbeans.modules.remote.impl.fs.server;

/**
 *
 */

/*package*/ final class Buffer {
    private final CharSequence text;
    private int curr;

    public Buffer(CharSequence text) {
        this.text = text;
        curr = 0;
    }
    
    public String getString() {
        int len = getInt();
        StringBuilder sb = new StringBuilder(len);
        int limit = curr + len;
        if (limit > text.length()) {
            new IllegalStateException("Wrong buffer format: " + text).printStackTrace(System.err); // NOI18N
            limit = text.length();
        }
        while (curr < limit) {
            sb.append(text.charAt(curr++));
        }
        skipSpaces();
        return FSSUtil.unescape(sb.toString());
    }
    
    public String getRest() {
        return text.subSequence(curr, text.length()).toString();
    }

    char getChar() {
        return text.charAt(curr++);
    }

    public int getInt() {
        long result = getLong();
        if (Integer.MIN_VALUE <= result && result <= Integer.MAX_VALUE) {
            return (int) result;
        } else {
            throw new IllegalArgumentException("Too long integer " + result + " in buffer " + text); //NOI18N
        }
    }

    public long getLong() {
        skipSpaces();
        //StringBuilder sb = new StringBuilder(16);
        long result = 0;
        boolean first = true;
        boolean negative = false;
        while (curr < text.length()) {
            char c = text.charAt(curr++);
            if (c == '-' && first) {
                first = false;
                negative = true;
            } else {
                first = false;
                if (Character.isDigit(c)) {
                    result *= 10;
                    result += (int) c - (int) '0';
                } else {
                    break;
                }
            }
        }
        return negative ? -result : result;
    }

    private void skipSpaces() {
        if (curr < text.length() && Character.isSpaceChar(text.charAt(curr))) {
            curr++;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ' ' + text;
    }
}
