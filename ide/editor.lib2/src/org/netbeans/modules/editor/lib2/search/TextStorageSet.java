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
package org.netbeans.modules.editor.lib2.search;

import java.util.HashMap;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.CharSubSequence;

/**
 * Set of character sequences allowing to compare a part of character sequence.
 * <br>
 * Note: Character sequences stored must be comparable by their equals() method.
 *
 * @author Miloslav Metelka
 */

public final class TextStorageSet {
    
    private final CharSequencesMap textMap = new CharSequencesMap();

    public CharSequence add(CharSequence text) {
        return textMap.put(text, text);
    }

    public CharSequence get(CharSequence text) {
        return get(text, 0, text.length());
    }

    public CharSequence get(CharSequence text, int startIndex, int endIndex) {
        return textMap.get(text, startIndex, endIndex);
    }

    public CharSequence remove(CharSequence text) {
        return remove(text, 0, text.length());
    }

    public CharSequence remove(CharSequence text, int startIndex, int endIndex) {
        return textMap.remove(text, startIndex, endIndex);
    }
    
    public int size() {
        return textMap.size();
    }

    public void clear() {
        textMap.clear();
    }

    private static final class CharSequencesMap extends HashMap<CharSequence,CharSequence> implements CharSequence {

        CharSequence compareText;

        int compareIndex;

        int compareLength;

        static final long serialVersionUID = 0L;

        public CharSequence get(CharSequence text, int startIndex, int endIndex) {
            compareText = text;
            compareIndex = startIndex;
            compareLength = endIndex - startIndex;
            CharSequence ret = get(this);
            compareText = null; // enable possible GC
            return ret;
        }

        public boolean containsKey(CharSequence text, int startIndex, int endIndex) {
            compareText = text;
            compareIndex = startIndex;
            compareLength = endIndex - startIndex;
            boolean ret = containsKey(this);
            compareText = null; // enable possible GC
            return ret;
        }

        public CharSequence remove(CharSequence text, int startIndex, int endIndex) {
            compareText = text;
            compareIndex = startIndex;
            compareLength = endIndex - startIndex;
            CharSequence ret = remove(this);
            compareText = null;
            return ret;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o instanceof CharSequence) {
                CharSequence text = (CharSequence) o;
                if (compareLength == text.length()) {
                    for (int index = compareLength - 1; index >= 0; index--) {
                        if (compareText.charAt(compareIndex + index) != text.charAt(index)) {
                            return false;
                        }
                    }
                    return true;
                }
                return false;
            }

            return false;
        }

        @Override
        public int hashCode() {
            int h = 0;
            CharSequence text = compareText;
            int endIndex = compareIndex + compareLength;

            for (int i = compareIndex; i < endIndex; i++) {
                h = 31 * h + text.charAt(i);
            }

            return h;
        }

        @Override
        public int length() {
            return compareLength;
        }

        @Override
        public char charAt(int index) {
            CharSequenceUtilities.checkIndexValid(index, length());
            return compareText.charAt(compareIndex + index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return new CharSubSequence.StringLike(this, start, end);
        }
        
    }

}
