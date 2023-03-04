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

package org.netbeans.editor.ext;

import java.text.CharacterIterator;

/**
* Character-iterator that operates on the array of characters.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class CharacterArrayIterator implements CharacterIterator {

    char[] chars;

    int beginIndex;

    int endIndex;

    int index;

    public CharacterArrayIterator(char[] chars, int beginIndex, int endIndex) {
        this.chars = chars;
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
        index = beginIndex;
    }

    private char currentChar() {
        return (index >= beginIndex && index < endIndex) ? chars[index] : DONE;
    }

    public char first() {
        index = beginIndex;
        return currentChar();
    }

    public char last() {
        index = endIndex - 1;
        return currentChar();
    }

    public char current() {
        return currentChar();
    }

    public char next() {
        index = Math.min(index + 1, endIndex);
        return currentChar();
    }

    public char previous() {
        if (index <= beginIndex) {
            return DONE;
        } else {
            return chars[--index];
        }
    }

    public char setIndex(int position) {
        if (position < beginIndex || position >= endIndex) {
            throw new IllegalArgumentException();
        }
        index = position;
        return currentChar();
    }

    public int getBeginIndex() {
        return beginIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public int getIndex() {
        return index;
    }

    public Object clone() {
        return new CharacterArrayIterator(chars, beginIndex, endIndex);
    }

}
