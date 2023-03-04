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
package org.netbeans.modules.editor.lib2.view;

import java.text.CharacterIterator;

/* This class was written from scratch. I considered using org.apache.pivot.text.CharacterIterator
from the Apache Pivot project, but it had bugs in the next() and previous() methods (trying to use
CharacterIterator.DONE = 65535 as an index). */
/**
 * Adapter class for providing a {@link CharacterIterator} over a {@link CharSequence} without
 * making a copy of the entire underlying string.
 *
 * @author Eirik Bakke (ebakke@ultorg.com)
 */
class CharSequenceCharacterIterator implements CharacterIterator {
    private final CharSequence charSequence;
    private int index;

    public CharSequenceCharacterIterator(CharSequence charSequence) {
        if (charSequence == null) {
            throw new NullPointerException();
        }
        this.charSequence = charSequence;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public char setIndex(int position) {
        if (position < getBeginIndex() || position > getEndIndex()) {
            throw new IllegalArgumentException();
        }
        this.index = position;
        return current();
    }

    @Override
    public char current() {
        return index == getEndIndex() ? CharacterIterator.DONE : charSequence.charAt(index);
    }

    @Override
    public char first() {
        return setIndex(getBeginIndex());
    }

    @Override
    public char last() {
        final int endIndex = getEndIndex();
        return setIndex(charSequence.length() == 0 ? endIndex : (endIndex - 1));
    }

    @Override
    public char next() {
        if (index < getEndIndex()) {
            index++;
        }
        return current();
    }

    @Override
    public char previous() {
        if (index > getBeginIndex()) {
            index--;
            return current();
        } else {
            return CharacterIterator.DONE;
        }
    }

    @Override
    public int getBeginIndex() {
        return 0;
    }

    @Override
    public int getEndIndex() {
        return charSequence.length();
    }

    @Override
    public Object clone() {
        CharacterIterator ret = new CharSequenceCharacterIterator(charSequence);
        ret.setIndex(index);
        return ret;
    }
}
