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
package org.openide.text;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;

import java.text.AttributedCharacterIterator;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/** The class is a support for all classes that implement PrintCookie.
* Allows creating of attributed texts.
*
* @author Ales Novak
*/
public class AttributedCharacters extends Object {
    /** Characters to iterate. */
    protected char[] chars;

    /** Font for each character. */
    protected Font[] fonts;

    /** Color for each character. */
    protected Color[] colors;

    /** Start indices of continuous blocks of text with the same font. */
    protected int[] runStart;

    /** Limit indices of continous ... */
    protected int[] runLimit;

    /** Current. */
    protected int current;

    public AttributedCharacters() {
        chars = new char[10];
        fonts = new Font[10];
        colors = new Color[10];
        runStart = new int[10];
        runLimit = new int[10];
        current = -1;
    }

    /** Append a character with specified font.
     * @param c character to append
     * @param f a Font
     * @param color a Color
     */
    public void append(char c, Font f, Color color) {
        if (f == null) {
            return;
        }

        if (++current == chars.length) {
            char[] ctmp = new char[2 * chars.length];
            Font[] ftmp = new Font[2 * chars.length];
            Color[] cotmp = new Color[2 * chars.length];
            int[] rstmp = new int[2 * chars.length];
            int[] rltmp = new int[2 * chars.length];
            System.arraycopy(chars, 0, ctmp, 0, chars.length);
            System.arraycopy(fonts, 0, ftmp, 0, chars.length);
            System.arraycopy(colors, 0, cotmp, 0, chars.length);
            System.arraycopy(runStart, 0, rstmp, 0, chars.length);
            System.arraycopy(runLimit, 0, rltmp, 0, chars.length);
            chars = ctmp;
            fonts = ftmp;
            colors = cotmp;
            runStart = rstmp;
            runLimit = rltmp;
        }

        chars[current] = c;
        fonts[current] = f;
        colors[current] = color;

        if (current != 0) {
            int prev = current - 1;

            if (fonts[prev].equals(f) && colors[prev].equals(color)) {
                runLimit[runStart[current] = runStart[prev]] = current;
            } else {
                runLimit[current] = current;
                runStart[current] = current;
            }
        }
    }

    /** Append a character array with a font.
     * @param a characters to append
     * @param f a font to use
     * @param color a color to use
     */
    public void append(char[] a, Font f, Color color) {
        if ((a == null) || (a.length == 0) || (f == null) || (color == null)) {
            return;
        }

        // increase buffers
        if ((++current + a.length) >= chars.length) {
            int size = Math.max(current + a.length, 2 * chars.length);
            char[] ctmp = new char[size];
            Font[] ftmp = new Font[size];
            Color[] cotmp = new Color[size];
            int[] rstmp = new int[size];
            int[] rltmp = new int[size];
            System.arraycopy(chars, 0, ctmp, 0, chars.length);
            System.arraycopy(fonts, 0, ftmp, 0, fonts.length);
            System.arraycopy(colors, 0, cotmp, 0, chars.length);
            System.arraycopy(runStart, 0, rstmp, 0, chars.length);
            System.arraycopy(runLimit, 0, rltmp, 0, chars.length);
            chars = ctmp;
            fonts = ftmp;
            colors = cotmp;
            runStart = rstmp;
            runLimit = rltmp;
        }

        // fill buffers
        System.arraycopy(a, 0, chars, current, a.length);

        for (int i = 0; i < a.length; i++) {
            fonts[i + current] = f;
            colors[i + current] = color;
        }

        // update last member to be the runLimit for the first one in the block
        int prev = current - 1;
        int pseudo = (current + a.length) - 1;

        if (prev < 0) { // start?
            runLimit[0] = pseudo;
        } else {
            int replace;

            if (fonts[prev].equals(f) && colors[prev].equals(color)) { // increase old block
                runLimit[runStart[prev]] = pseudo;
                replace = runStart[prev];
            } else { // new block
                runLimit[current] = pseudo;
                replace = current;
            }

            // init items in the block - update runStart
            for (int i = current; i <= pseudo; i++) {
                runStart[i] = replace;
            }
        }

        current = pseudo;
    }

    /** Produce an appropriate character iterator.
     * @return an iterator
     */
    public AttributedCharacterIterator iterator() {
        int size = current + 1;
        char[] cs = new char[size];
        Font[] fs = new Font[size];
        Color[] colos = new Color[size];
        int[] rstmp = new int[size];
        int[] rltmp = new int[size];
        System.arraycopy(runStart, 0, rstmp, 0, size);
        System.arraycopy(runLimit, 0, rltmp, 0, size);
        System.arraycopy(chars, 0, cs, 0, size);
        System.arraycopy(fonts, 0, fs, 0, size);
        System.arraycopy(colors, 0, colos, 0, size);

        AttributedCharacterIterator ret = new AttributedCharacterIteratorImpl(cs, fs, colos, rstmp, rltmp);

        return ret;
    }

    /** Implementation of AttributedCharacterIterator interface. */
    public static class AttributedCharacterIteratorImpl implements AttributedCharacterIterator {
        /** Current position. */
        protected int current;

        /** Characters to iterate. */
        protected char[] chars;

        /** Font for each character. */
        protected Font[] fonts;

        /** Color for each character. */
        protected Color[] colors;

        /** Start indices of continuous blocks of text with the same font. */
        protected int[] runStart;

        /** Limit indices of continous ... */
        protected int[] runLimit;

        /** Singleton. */
        protected Set<AttributedCharacterIterator.Attribute> singleton;

        public AttributedCharacterIteratorImpl(char[] chars, Font[] fonts, Color[] colors, int[] rs, int[] rl) {
            this.chars = chars;
            this.fonts = fonts;
            this.colors = colors;
            runStart = rs;
            runLimit = rl;
        }

        // first implement CharacterIterator

        /*
         * Clones the object.
         * @return a clone
         */
        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                // impossible to catch it
                return null;
            }
        }

        /*
         * @return current char
         */
        public char current() {
            if (current >= chars.length) {
                return DONE;
            }

            return chars[current];
        }

        /*
         * @return first char
         */
        public char first() {
            current = 0;

            if (current >= chars.length) {
                return DONE;
            }

            return chars[current];
        }

        /*
         * @return begin index
         */
        public int getBeginIndex() {
            return 0;
        }

        /*
         * @return end index
         */
        public int getEndIndex() {
            return chars.length;
        }

        /*
         * @return current index
         */
        public int getIndex() {
            return current;
        }

        /*
         * @return character at last index
         */
        public char last() {
            int end = getEndIndex();

            if (end == 0) {
                return DONE;
            }

            return chars[current = end - 1];
        }

        /*
         * @return next char
         */
        public char next() {
            if (current >= (getEndIndex() - 1)) {
                return DONE;
            }

            return chars[++current];
        }

        /*
         * @return previous char
         */
        public char previous() {
            if (current == 0) {
                return DONE;
            }

            return chars[--current];
        }

        /*
         * @param i new index
         * @return char at that position
         */
        public char setIndex(int i) {
            if (i < 0) {
                throw new IllegalArgumentException();
            }

            if (i == getEndIndex()) {
                current = getEndIndex();

                return DONE;
            }

            return chars[current = i];
        }

        // attributes

        /*
         * @return a Set with TextAttribute.FONT
         */
        public Set<AttributedCharacterIterator.Attribute> getAllAttributeKeys() {
            if (singleton == null) {
                Set<AttributedCharacterIterator.Attribute> l = new HashSet<AttributedCharacterIterator.Attribute>(4);
                l.add(TextAttribute.FONT);
                l.add(TextAttribute.FOREGROUND);
                singleton = Collections.unmodifiableSet(l);
            }

            return singleton;
        }

        /*
         * @param att an Attribute
         * @return a value for this attribute
         */
        public Object getAttribute(AttributedCharacterIterator.Attribute att) {
            if (att == TextAttribute.FONT) {
                return fonts[getIndex()];
            } else if (att == TextAttribute.FOREGROUND) {
                return colors[getIndex()];
            } else {
                return null;
            }
        }

        /*
         *  @return map with all attributes for current char
         */
        public Map<AttributedCharacterIterator.Attribute,Object> getAttributes() {
            Map<AttributedCharacterIterator.Attribute,Object> m = new HashMap<AttributedCharacterIterator.Attribute,Object>(1);
            m.put(TextAttribute.FONT, fonts[getIndex()]);
            m.put(TextAttribute.FOREGROUND, colors[getIndex()]);

            return m;
        }

        /*
         * @return
         */
        public int getRunLimit() {
            return runLimit[runStart[getIndex()]] + 1;
        }

        /*
         * @param att an Attribute
         */
        public int getRunLimit(AttributedCharacterIterator.Attribute att) {
            if ((att != TextAttribute.FONT) && (att != TextAttribute.FOREGROUND)) {
                return getEndIndex(); // undefined attribute
            }

            return getRunLimit();
        }

        /*
         * @param attributes a Set of attributes
         */
        public int getRunLimit(Set<? extends AttributedCharacterIterator.Attribute> attributes) {
            if (attributes.contains(TextAttribute.FONT) || attributes.contains(TextAttribute.FOREGROUND)) {
                return getRunLimit();
            } else {
                return getEndIndex();
            }
        }

        /*
         * @return run start for current char
         */
        public int getRunStart() {
            return runStart[getIndex()];
        }

        /*
         * @param att
         */
        public int getRunStart(AttributedCharacterIterator.Attribute att) {
            if ((att != TextAttribute.FONT) && (att != TextAttribute.FOREGROUND)) {
                return 0; // undefined attribute
            }

            return getRunStart();
        }

        /*
         * @param attributes a Set
         */
        public int getRunStart(Set<? extends AttributedCharacterIterator.Attribute> attributes) {
            if ((attributes.contains(TextAttribute.FONT)) || attributes.contains(TextAttribute.FOREGROUND)) {
                return getRunStart();
            } else {
                return 0;
            }
        }
    }
}
