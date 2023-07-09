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

package org.netbeans.lib.editor.util;

/**
 * Subsequence of the given character sequence. The backing sequence
 * is considered to be stable i.e. does not change length or content over time.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class CharSubSequence extends AbstractCharSequence {

    /**
     * Ensure that the given start and end parameters are valid indices
     * of the given text.
     * @throws IndexOutOfBoundsException if the start or end are not within bounds
     *  of the given text.
     * @deprecated use {@link CharSequenceUtilities#checkIndexesValid(CharSequence, int, int)}
     */
    @Deprecated
    public static void checkIndexesValid(CharSequence text, int start, int end) {
        CharSequenceUtilities.checkIndexesValid(text, start, end);
    }
    
    private int length;
    
    private int start;

    private CharSequence backingSequence;
    
    /**
     * Construct character subsequence with the given backing character sequence.
     *
     * @param backingSequence non-null backing character sequence. It is considered
     * to be stable and not to change over time.
     * @param start &gt;=0 starting index of the subsequence within
     *  the backing character sequence.
     * @param end &gt;= ending index of the subsequence within
     *  the backing character sequence.
     * @throws IndexOutOfBoundsException if the start or end are not within bounds
     *  of backingSequence.
     */
    public CharSubSequence(CharSequence backingSequence, int start, int end) {
        checkIndexesValid(backingSequence, start, end);
        this.backingSequence = backingSequence;
        this.start = start;
        this.length = end - start;
    }
    
    protected CharSequence backingSequence() {
        return backingSequence;
    }
    
    protected int start() {
        return start;
    }

    public int length() {
        return length;
    }

    public char charAt(int index) {
        CharSequenceUtilities.checkIndexValid(index, length);
        return backingSequence.charAt(start() + index);
    }

    /**
     * Subclass providing string-like implementation
     * of <code>hashCode()</code> and <code>equals()</code>
     * method accepting strings with the same content
     * like charsequence has.
     * <br>
     * This makes the class suitable for matching to strings
     * e.g. in maps.
     * <br>
     * <b>NOTE</b>: Matching is just uni-directional
     * i.e. charsequence.equals(string) works
     * but string.equals(charsequence) does not.
     */
    public static class StringLike extends CharSubSequence {

        public StringLike(CharSequence backingSequence, int start, int end) {
            super(backingSequence, start, end);
        }
    
        public int hashCode() {
            return CharSequenceUtilities.stringLikeHashCode(this);
        }

        public boolean equals(Object o) {
            return CharSequenceUtilities.equals(this, o);
        }
        
    }

}
