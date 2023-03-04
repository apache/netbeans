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

package org.netbeans.lib.editor.util;

/**
 * Abstract implementation of character sequence
 * with {@link String}-like implementation
 * of <CODE>hashCode()</CODE> and <CODE>equals()</CODE>.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class AbstractCharSequence implements CharSequence {

    /**
     * Returns the length of this character sequence.  The length is the number
     * of 16-bit Unicode characters in the sequence. </p>
     *
     * @return  the number of characters in this sequence
     */
    public abstract int length();

    /**
     * Returns the character at the specified index.  An index ranges from zero
     * to <tt>length() - 1</tt>.  The first character of the sequence is at
     * index zero, the next at index one, and so on, as for array
     * indexing. </p>
     *
     * @param   index   the index of the character to be returned
     *
     * @return  the specified character
     *
     * @throws  IndexOutOfBoundsException
     *          if the <tt>index</tt> argument is negative or not less than
     *          <tt>length()</tt>
     */
    public abstract char charAt(int index);


    private String toString(int start, int end) {
        return CharSequenceUtilities.toString(this, start, end);
    }

    /**
     * Return subsequence of this character sequence.
     * The returned character sequence is only as stable as is this character
     * sequence.
     *
     * @param start &gt;=0 starting index of the subsequence within this
     *  character sequence.
     * @param end &gt;=0 ending index of the subsequence within this
     *  character sequence.
     */
    public CharSequence subSequence(int start, int end) {
        return new CharSubSequence(this, start, end);
    }

    public String toString() {
        return toString(0, length());
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
    public abstract static class StringLike extends AbstractCharSequence {

        public int hashCode() {
            return CharSequenceUtilities.stringLikeHashCode(this);
        }

        public boolean equals(Object o) {
            return CharSequenceUtilities.equals(this, o);
        }
        
        public CharSequence subSequence(int start, int end) {
            return new CharSubSequence.StringLike(this, start, end);
        }
        
    }

}
