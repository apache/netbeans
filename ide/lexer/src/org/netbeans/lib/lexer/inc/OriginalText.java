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

package org.netbeans.lib.lexer.inc;

import org.netbeans.lib.editor.util.AbstractCharSequence;

/**
 * Character sequence emulating state of a mutable input source
 * before the last modification.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class OriginalText extends AbstractCharSequence.StringLike {

    private final CharSequence currentText;

    private final int offset;

    private final int insertedTextLength;

    private final CharSequence removedText;

    private final int origLength;

    public OriginalText(CharSequence currentText, int offset, CharSequence removedText, int insertedTextLength) {
        this.currentText = currentText;
        this.offset = offset;
        this.removedText = (removedText != null) ? removedText : ""; // always non-null
        this.insertedTextLength = insertedTextLength;

        this.origLength = currentText.length() - insertedTextLength + this.removedText.length();
    }

    public int length() {
        return origLength;
    }

    public char charAt(int index) {
        if (index < offset) {
            return currentText.charAt(index);
        }
        index -= offset;
        if (index < removedText.length()) {
            return removedText.charAt(index);
        }
        return currentText.charAt(offset + index - removedText.length() + insertedTextLength);
    }

    public char[] toCharArray(int start, int end) {
        char[] chars = new char[end - start];
        int charsIndex = 0;
        if (start < offset) {
            int bound = (end < offset) ? end : offset;
            while (start < bound) {
                chars[charsIndex++] = currentText.charAt(start++);
            }
            if (end == bound) {
                return chars;
            }
        }
        start -= offset;
        end -= offset;
        int bound = removedText.length();
        if (start < bound) {
            if (end < bound) {
                bound = end;
            }
            while (start < bound) {
                chars[charsIndex++] = removedText.charAt(start++);
            }
            if (end == bound) {
                return chars;
            }
        }
        bound = offset - removedText.length() + insertedTextLength;
        start += bound;
        bound += end;
        while (start < bound) {
            chars[charsIndex++] = currentText.charAt(start++);
        }
        return chars;
    }

}
