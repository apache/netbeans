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
package org.openide.util;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Efficiently store {@link CharSequence strings} in memory. This
 * class provides useful {@code static} methods to create and
 * work with memory efficient {@link CharSequence}
 * implementations for strings using just a subsets of UTF characters.
 * <p>
 * Often the strings we deal with are based on simple English alphabet. Keeping
 * them in memory as {@code char[]} isn't really efficient as they may fit
 * in simple {@code byte[]}. This utility class does such <em>compression</em>
 * behind the scene. Use the here-in provided methods and your strings will be
 * stored as efficiently as possible. As can be seen from the following example,
 * many languages benefit from the <em>compaction</em>:
 * </p>
 * {@snippet  file="org/openide/util/CharSequencesTest.java" region="createSample"}
 * <p>
 * To compare two sequences use dedicated {@link CharSequences#comparator()}
 * which understands the compacted representation and uses it, prior to falling
 * back to {@code char} by {@code char} comparision:
 * </p>
 * {@snippet  file="org/openide/util/CharSequencesTest.java" region="compareStrings"}
 * <p>
 * Use {@link CharSequences#indexOf(java.lang.CharSequence, java.lang.CharSequence)} method
 * to search the compacted strings efficiently:
 * </p>
 * {@snippet  file="org/openide/util/CharSequencesTest.java" region="indexOfSample"}
 * <p>
 * This <a target="_blank" href="https://search.maven.org/artifact/org.netbeans.api/org-openide-util/RELEASE110/jar">
 * library is available on Maven central</a>. Use it with following co-ordinates:
 * </p>
 * {@snippet  file="org/openide/util/CharSequencesTest.xml" region="CharSequencesPomDependency"}
 *
 * @since 8.3
 * @author Alexander Simon
 * @author Vladimir Voskresensky
 */
public final class CharSequences {

    /**
     * Creates new {@link CharSequence} instance representing the chars
     * in the array. The sequence contains its own copy of the array content
     * represented in an efficient (e.g. {@code byte[]}) way.
     *
     * @param buf buffer to copy the characters from
     * @param start starting offset in the {@code buf} array
     * @param count number of characters to copy
     * @return immutable char sequence
     */
    public static CharSequence create(char buf[], int start, int count) {
        if (start < 0) {
            throw new StringIndexOutOfBoundsException(start);
        }
        if (count < 0) {
            throw new StringIndexOutOfBoundsException(count);
        }
        // Note: offset or count might be near -1>>>1.
        if (start > buf.length - count) {
            throw new StringIndexOutOfBoundsException(start + count);
        }
        int n = count;
        if (n == 0) {
            // special constant shared among all empty char sequences
            return EMPTY;
        }
        byte[] b = new byte[n];
        boolean bytes = true;
        int o;
        int d;
        boolean id = true;
        // check 2 bytes vs 1 byte chars
        for (int i = 0; i < n; i++) {
            o = buf[start + i];
            d = o & 0xFF;
            if (d != o) {
                // can not compact this char sequence
                bytes = false;
                break;
            }
            if (id) {
                id = is6BitChar(d);
            }
            b[i] = (byte) o;
        }
        if (bytes) {
            return createFromBytes(b, n, id);
        }
        char[] v = new char[count];
        System.arraycopy(buf, start, v, 0, count);
        return new CharBasedSequence(v);
    }

    /**
     * Creates new {@link CharSequence} instance representing the content
     * of another sequence or {@link String} efficiently.
     * 
     * {@snippet  file="org/openide/util/CharSequencesTest.java" region="createSample"}
     * 
     * @param s existing string or sequence of chars
     * @return immutable char sequence efficiently representing the data
     */
    public static CharSequence create(CharSequence s) {
        if (s == null) {
            return null;
        }
        // already compact instance
        if (s instanceof CompactCharSequence) {
            return s;
        }
        int n = s.length();
        if (n == 0) {
            // special constant shared among all empty char sequences
            return EMPTY;
        }
        byte[] b = new byte[n];
        boolean bytes = true;
        int o;
        int d;
        boolean id = true;
        for (int i = 0; i < n; i++) {
            o = s.charAt(i);
            d = o & 0xFF;
            if (d != o) {
                bytes = false;
                break;
            }
            if (id) {
                id = is6BitChar(d);
            }
            b[i] = (byte) o;
        }
        if (bytes) {
            return createFromBytes(b, n, id);
        }
        char[] v = new char[n];
        for (int i = 0; i < n; i++) {
            v[i] = s.charAt(i);
        }
        return new CharBasedSequence(v);
    }

    /**
     * Provides optimized char sequences comparator.
     *
     * {@snippet  file="org/openide/util/CharSequencesTest.java" region="compareStrings"}
     *
     * @return comparator for {@link CharSequence} objects
     */
    public static Comparator<CharSequence> comparator() {
        return Comparator;
    }

    /**
     * Returns object to represent empty sequence {@code ""}.
     * @return char sequence to represent empty sequence
     */
    public static CharSequence empty() {
        return EMPTY;
    }

    /**
     * Predicate to check if provides char sequence is based on compact implementation.
     *
     * @param cs char sequence object to check
     * @return {@code true} if compact implementation, {@code false} otherwise
     */
    public static boolean isCompact(CharSequence cs) {
        return cs instanceof CompactCharSequence;
    }

    /**
     * Implementation of {@link String#indexOf(String)} for character sequences.
     *
     * {@snippet  file="org/openide/util/CharSequencesTest.java" region="indexOfSample"}
     *
     * @param text the text to search
     * @param seq the sequence to find in the {@code text}
     * @return the index of the first occurrence of the specified substring, or
     *      {@code -1} if there is no such occurrence
     */
    public static int indexOf(CharSequence text, CharSequence seq) {
        return indexOf(text, seq, 0);
    }

    /**
     * Implementation of {@link String#indexOf(String,int)} for character sequences.
     *
     * @param text the text to search
     * @param seq the sequence to find in the {@code text}
     * @param fromIndex the index to start searching from
     * @return the index of the first occurrence of the specified substring, or
     *      {@code -1} if there is no such occurrence
     */
    public static int indexOf(CharSequence text, CharSequence seq, int fromIndex) {
        int textLength = text.length();
        int seqLength = seq.length();
        if (fromIndex >= textLength) {
            return (seqLength == 0 ? textLength : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (seqLength == 0) {
            return fromIndex;
        }

        char first = seq.charAt(0);
        int max = textLength - seqLength;

        for (int i = fromIndex; i <= max; i++) {
            // look for first character
            if (text.charAt(i) != first) {
                while (++i <= max && text.charAt(i) != first) {
                }
            }

            // found first character, now look at the rest of seq
            if (i <= max) {
                int j = i + 1;
                int end = j + seqLength - 1;
                for (int k = 1; j < end && text.charAt(j) == seq.charAt(k); j++, k++) {
                }
                if (j == end) {
                    // found whole sequence
                    return i;
                }
            }
        }
        return -1;
    }

    private static CompactCharSequence createFromBytes(byte[] b, int n, boolean id) {
        if (id && n > 0) {
            if (n <= 10) {
                return new Fixed6Bit_1_10(b, n);
            } else if (n <= 20) {
                return new Fixed6Bit_11_20(b, n);
            } else if (n <= 30) {
                return new Fixed6Bit_21_30(b, n);
            }
        }
        if (n < 8) {
            return new Fixed_0_7(b, n);
        } else if (n < 16) {
            return new Fixed_8_15(b, n);
        } else if (n < 24) {
            return new Fixed_16_23(b, n);
        }
        return new ByteBasedSequence(b);
    }

    /**
     * Provides compact char sequence object like {@link String#String(char[], int, int)}
     */
    private static CharSequence create(byte buf[], int start, int count) {
        if (start < 0) {
            throw new StringIndexOutOfBoundsException(start);
        }
        if (count < 0) {
            throw new StringIndexOutOfBoundsException(count);
        }
        // Note: offset or count might be near -1>>>1.
        if (start > buf.length - count) {
            throw new StringIndexOutOfBoundsException(start + count);
        }
        int n = count;
        if (n == 0) {
            // special constant shared among all empty char sequences
            return EMPTY;
        }
        byte[] b = new byte[n];
        System.arraycopy(buf, start, b, 0, count);
        boolean id = true;
        for (int i = 0; i < n; i++) {
            if (id) {
                id = is6BitChar(b[i]);
            } else {
                break;
            }
        }
        return createFromBytes(b, n, id);
    }

    // Memory efficient implementations of CharSequence
    // Comparision between Fixed and String memory consumption:
    // 32-bit JVM
    //String    String CharSequence  ASCII' CharSequence
    //Length     Size    Size          Size
    //1..2        40      16            16
    //3..6        48      16            16
    //7..7        56      16            16
    //8..10       56      24            16
    //11..14      64      24            24
    //15..15      72      24            24
    //16..18      72      32            24
    //19..20      80      32            24
    //21..22      80      32            32
    //23..23      88      32            32
    //24..26      88      56            32
    //27..28      96      56            32
    //29..30      96      64            32
    //31..34     104      64            64
    //35..36     112      64            64
    //37..38     112      72            72
    //39..42     120      72            72
    //....................................
    //79..82     200     112           112
    //
    // 64-bit JVM
    //1           72      24
    //2           72      24
    //3           80      24
    //4           80      24
    //5           80      24
    //6           80      24
    //7           88      24
    //8           88      32
    //9           88      32
    //11          96      32
    //13          96      32
    //15         104      32
    //16         104      40
    //17         104      40
    //18         112      40
    //19         112      40
    //20         112      40
    //22         120      40
    //23         120      40
    //24         120      80
    //25         120      88
    //26         128      88
    //60         192     120
    //100        272     160

    //<editor-fold defaultstate="collapsed" desc="Private Classes">

    /**
     * compact char sequence implementation for strings in range 0-7 characters
     * 8 + 2*4 = 16 bytes for all strings vs String impl occupying
     */
    private static final class Fixed_0_7 implements CompactCharSequence, Comparable<CharSequence> {

        private final int i1;
        private final int i2;

        @SuppressWarnings("fallthrough")
        private Fixed_0_7(byte[] b, int n) {
            int a1 = n;
            int a2 = 0;
            switch (n) {
                case 7:
                    a2 += (b[6] & 0xFF) << 24;
                case 6:
                    a2 += (b[5] & 0xFF) << 16;
                case 5:
                    a2 += (b[4] & 0xFF) << 8;
                case 4:
                    a2 += b[3] & 0xFF;
                case 3:
                    a1 += (b[2] & 0xFF) << 24;
                case 2:
                    a1 += (b[1] & 0xFF) << 16;
                case 1:
                    a1 += (b[0] & 0xFF) << 8;
                case 0:
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            i1 = a1;
            i2 = a2;
        }

        @Override
        public int length() {
            return i1 & 0xFF;
        }

        @Override
        public char charAt(int index) {
            int r = 0;
            switch (index) {
                case 0:
                    r = (i1 & 0xFF00) >> 8;
                    break;
                case 1:
                    r = (i1 & 0xFF0000) >> 16;
                    break;
                case 2:
                    r = (i1 >> 24) & 0xFF;
                    break;
                case 3:
                    r = i2 & 0xFF;
                    break;
                case 4:
                    r = (i2 & 0xFF00) >> 8;
                    break;
                case 5:
                    r = (i2 & 0xFF0000) >> 16;
                    break;
                case 6:
                    r = (i2 >> 24) & 0xFF;
                    break;
            }
            return (char) r;
        }

        @Override
        public String toString() {
            int n = length();
            char[] r = new char[n];
            for (int i = 0; i < n; i++) {
                r[i] = charAt(i);
            }
            return new String(r);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof Fixed_0_7) {
                Fixed_0_7 otherString = (Fixed_0_7) object;
                return i1 == otherString.i1 && i2 == otherString.i2;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 19*i1 + 37*i2;
            hash += (hash <<  15) ^ 0xffffcd7d;
            hash ^= (hash >>> 10);
            hash += (hash <<   3);
            hash ^= (hash >>>  6);
            hash += (hash <<   2) + (hash << 14);
            return hash ^ (hash >>> 16);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return CharSequences.create(toString().substring(start, end));
        }

        @Override
        public int compareTo(CharSequence o) {
            return Comparator.compare(this, o);
        }
    }

    private static final long[] encodeTable = new long[] {
           -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
           -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
           -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1,
            0,  1,  2,  3,  4,  5,  6,  7,  8,  9, -1, -1, -1, -1, -1, -1,
           -1, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
           25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, -1, -1, -1, -1, 63,
           -1, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50,
           51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1
    };

    private static final char[] decodeTable = new char[] {
          '0','1','2','3','4','5','6','7','8','9',
              'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O',
          'P','Q','R','S','T','U','V','W','X','Y','Z',
              'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o',
          'p','q','r','s','t','u','v','w','x','y','z',
                                                                  '.',     // for 'file.ext' names
                                                                      '_'
    };

    private static boolean is6BitChar(int d) {
        return d < 128 && encodeTable[d] >= 0;
    }

    private static long encode6BitChar(int d) {
        return encodeTable[d];
    }

    private static char decode6BitChar(int d) {
        return decodeTable[d];
    }

    private static final class Fixed6Bit_1_10 implements CompactCharSequence, Comparable<CharSequence> {

        // Length is in lower 4bits
        // then 6bits per symbol
        private final long i;

        @SuppressWarnings("fallthrough")
        private Fixed6Bit_1_10(byte[] b, int n) {
            long a = n;
            switch (n) {
                case 10:
                    a |= encode6BitChar(b[9]) << 58;
                case 9:
                    a |= encode6BitChar(b[8]) << 52;
                case 8:
                    a |= encode6BitChar(b[7]) << 46;
                case 7:
                    a |= encode6BitChar(b[6]) << 40;
                case 6:
                    a |= encode6BitChar(b[5]) << 34;
                case 5:
                    a |= encode6BitChar(b[4]) << 28;
                case 4:
                    a |= encode6BitChar(b[3]) << 22;
                case 3:
                    a |= encode6BitChar(b[2]) << 16;
                case 2:
                    a |= encode6BitChar(b[1]) << 10;
                case 1:
                    a |= encode6BitChar(b[0]) << 4;
                    break;
                case 0:
                default:
                    throw new IllegalArgumentException();
            }
            i = a;
        }

        @Override
        public int length() {
            return (int) (i & 0x0FL);
        }

        @Override
        public char charAt(int index) {
            int r = 0;
            switch (index) {
                case 0:
                    r = (int) ((i >> 4) & 0x3FL);
                    break;
                case 1:
                    r = (int) ((i >> 10) & 0x3FL);
                    break;
                case 2:
                    r = (int) ((i >> 16) & 0x3FL);
                    break;
                case 3:
                    r = (int) ((i >> 22) & 0x3FL);
                    break;
                case 4:
                    r = (int) ((i >> 28) & 0x3FL);
                    break;
                case 5:
                    r = (int) ((i >> 34) & 0x3FL);
                    break;
                case 6:
                    r = (int) ((i >> 40) & 0x3FL);
                    break;
                case 7:
                    r = (int) ((i >> 46) & 0x3FL);
                    break;
                case 8:
                    r = (int) ((i >> 52) & 0x3FL);
                    break;
                case 9:
                    r = (int) ((i >> 58) & 0x3FL);
                    break;
            }
            return decode6BitChar(r);
        }

        @Override
        public String toString() {
            int n = length();
            char[] r = new char[n];
            for (int j = 0; j < n; j++) {
                r[j] = charAt(j);
            }
            return new String(r);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof Fixed6Bit_1_10) {
                Fixed6Bit_1_10 otherString = (Fixed6Bit_1_10) object;
                return i == otherString.i;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = (int)((i + (i>>32)) & 0xFFFFFFFFL);
            hash += (hash <<  15) ^ 0xffffcd7d;
            hash ^= (hash >>> 10);
            hash += (hash <<   3);
            hash ^= (hash >>>  6);
            hash += (hash <<   2) + (hash << 14);
            return hash ^ (hash >>> 16);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return CharSequences.create(toString().substring(start, end));
        }

        @Override
        public int compareTo(CharSequence o) {
            return Comparator.compare(this, o);
        }
    }

    private static final class Fixed6Bit_11_20 implements CompactCharSequence, Comparable<CharSequence> {

        // Length is in lower 4bits of i1 and l2
        // then 6 bits per character
        private final long i1;
        private final long i2;

        @SuppressWarnings("fallthrough")
        private Fixed6Bit_11_20(byte[] b, int n) {
            long a1 = n & 0x0F;
            long a2 = (n >> 4) & 0x0F;
            switch (n) {
                case 20:
                    a2 |= encode6BitChar(b[19]) << 58;
                case 19:
                    a2 |= encode6BitChar(b[18]) << 52;
                case 18:
                    a2 |= encode6BitChar(b[17]) << 46;
                case 17:
                    a2 |= encode6BitChar(b[16]) << 40;
                case 16:
                    a2 |= encode6BitChar(b[15]) << 34;
                case 15:
                    a2 |= encode6BitChar(b[14]) << 28;
                case 14:
                    a2 |= encode6BitChar(b[13]) << 22;
                case 13:
                    a2 |= encode6BitChar(b[12]) << 16;
                case 12:
                    a2 |= encode6BitChar(b[11]) << 10;
                case 11:
                    a2 |= encode6BitChar(b[10]) << 4;
                case 10:
                    a1 |= encode6BitChar(b[9]) << 58;
                case 9:
                    a1 |= encode6BitChar(b[8]) << 52;
                case 8:
                    a1 |= encode6BitChar(b[7]) << 46;
                case 7:
                    a1 |= encode6BitChar(b[6]) << 40;
                case 6:
                    a1 |= encode6BitChar(b[5]) << 34;
                case 5:
                    a1 |= encode6BitChar(b[4]) << 28;
                case 4:
                    a1 |= encode6BitChar(b[3]) << 22;
                case 3:
                    a1 |= encode6BitChar(b[2]) << 16;
                case 2:
                    a1 |= encode6BitChar(b[1]) << 10;
                case 1:
                    a1 |= encode6BitChar(b[0]) << 4;
                    break;
                case 0:
                default:
                    throw new IllegalArgumentException();
            }
            i1 = a1;
            i2 = a2;
        }

        @Override
        public int length() {
            return (int) ((i1 & 0x0FL) + ((i2 & 0x0FL) << 4));
        }

        @Override
        public char charAt(int index) {
            int r = 0;
            switch (index) {
                case 0:
                    r = (int) ((i1 >> 4) & 0x3FL);
                    break;
                case 1:
                    r = (int) ((i1 >> 10) & 0x3FL);
                    break;
                case 2:
                    r = (int) ((i1 >> 16) & 0x3FL);
                    break;
                case 3:
                    r = (int) ((i1 >> 22) & 0x3FL);
                    break;
                case 4:
                    r = (int) ((i1 >> 28) & 0x3FL);
                    break;
                case 5:
                    r = (int) ((i1 >> 34) & 0x3FL);
                    break;
                case 6:
                    r = (int) ((i1 >> 40) & 0x3FL);
                    break;
                case 7:
                    r = (int) ((i1 >> 46) & 0x3FL);
                    break;
                case 8:
                    r = (int) ((i1 >> 52) & 0x3FL);
                    break;
                case 9:
                    r = (int) ((i1 >> 58) & 0x3FL);
                    break;
                case 10:
                    r = (int) ((i2 >> 4) & 0x3FL);
                    break;
                case 11:
                    r = (int) ((i2 >> 10) & 0x3FL);
                    break;
                case 12:
                    r = (int) ((i2 >> 16) & 0x3FL);
                    break;
                case 13:
                    r = (int) ((i2 >> 22) & 0x3FL);
                    break;
                case 14:
                    r = (int) ((i2 >> 28) & 0x3FL);
                    break;
                case 15:
                    r = (int) ((i2 >> 34) & 0x3FL);
                    break;
                case 16:
                    r = (int) ((i2 >> 40) & 0x3FL);
                    break;
                case 17:
                    r = (int) ((i2 >> 46) & 0x3FL);
                    break;
                case 18:
                    r = (int) ((i2 >> 52) & 0x3FL);
                    break;
                case 19:
                    r = (int) ((i2 >> 58) & 0x3FL);
                    break;
            }
            return decode6BitChar(r);
        }

        @Override
        public String toString() {
            int n = length();
            char[] r = new char[n];
            for (int j = 0; j < n; j++) {
                r[j] = charAt(j);
            }
            return new String(r);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof Fixed6Bit_11_20) {
                Fixed6Bit_11_20 otherString = (Fixed6Bit_11_20) object;
                return i1 == otherString.i1 && i2 == otherString.i2;
            }
            return false;
        }

        @Override
        public int hashCode() {
            long res = i1 + 31 * i2;
            res = (res + (res >> 32)) & 0xFFFFFFFFL;
            return (int) res;
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return CharSequences.create(toString().substring(start, end));
        }

        @Override
        public int compareTo(CharSequence o) {
            return Comparator.compare(this, o);
        }
    }

    private static final class Fixed6Bit_21_30 implements CompactCharSequence, Comparable<CharSequence> {

        // Length is in lower 4bits of i1 and l2
        // then 6 bits per character in i1, i2 and i3
        private final long i1;
        private final long i2;
        private final long i3;

        @SuppressWarnings("fallthrough")
        private Fixed6Bit_21_30(byte[] b, int n) {
            long a1 = n & 0x0F;
            long a2 = (n >> 4) & 0x0F;
            long a3 = 0;
            switch (n) {
                case 30:
                    a3 |= encode6BitChar(b[29]) << 58;
                case 29:
                    a3 |= encode6BitChar(b[28]) << 52;
                case 28:
                    a3 |= encode6BitChar(b[27]) << 46;
                case 27:
                    a3 |= encode6BitChar(b[26]) << 40;
                case 26:
                    a3 |= encode6BitChar(b[25]) << 34;
                case 25:
                    a3 |= encode6BitChar(b[24]) << 28;
                case 24:
                    a3 |= encode6BitChar(b[23]) << 22;
                case 23:
                    a3 |= encode6BitChar(b[22]) << 16;
                case 22:
                    a3 |= encode6BitChar(b[21]) << 10;
                case 21:
                    a3 |= encode6BitChar(b[20]) << 4;
                case 20:
                    a2 |= encode6BitChar(b[19]) << 58;
                case 19:
                    a2 |= encode6BitChar(b[18]) << 52;
                case 18:
                    a2 |= encode6BitChar(b[17]) << 46;
                case 17:
                    a2 |= encode6BitChar(b[16]) << 40;
                case 16:
                    a2 |= encode6BitChar(b[15]) << 34;
                case 15:
                    a2 |= encode6BitChar(b[14]) << 28;
                case 14:
                    a2 |= encode6BitChar(b[13]) << 22;
                case 13:
                    a2 |= encode6BitChar(b[12]) << 16;
                case 12:
                    a2 |= encode6BitChar(b[11]) << 10;
                case 11:
                    a2 |= encode6BitChar(b[10]) << 4;
                case 10:
                    a1 |= encode6BitChar(b[9]) << 58;
                case 9:
                    a1 |= encode6BitChar(b[8]) << 52;
                case 8:
                    a1 |= encode6BitChar(b[7]) << 46;
                case 7:
                    a1 |= encode6BitChar(b[6]) << 40;
                case 6:
                    a1 |= encode6BitChar(b[5]) << 34;
                case 5:
                    a1 |= encode6BitChar(b[4]) << 28;
                case 4:
                    a1 |= encode6BitChar(b[3]) << 22;
                case 3:
                    a1 |= encode6BitChar(b[2]) << 16;
                case 2:
                    a1 |= encode6BitChar(b[1]) << 10;
                case 1:
                    a1 |= encode6BitChar(b[0]) << 4;
                    break;
                case 0:
                default:
                    throw new IllegalArgumentException();
            }
            i1 = a1;
            i2 = a2;
            i3 = a3;
        }

        @Override
        public int length() {
            return (int) ((i1 & 0x0FL) + ((i2 & 0x0FL) << 4));
        }

        @Override
        public char charAt(int index) {
            int r = 0;
            switch (index) {
                case 0:
                    r = (int) ((i1 >> 4) & 0x3FL);
                    break;
                case 1:
                    r = (int) ((i1 >> 10) & 0x3FL);
                    break;
                case 2:
                    r = (int) ((i1 >> 16) & 0x3FL);
                    break;
                case 3:
                    r = (int) ((i1 >> 22) & 0x3FL);
                    break;
                case 4:
                    r = (int) ((i1 >> 28) & 0x3FL);
                    break;
                case 5:
                    r = (int) ((i1 >> 34) & 0x3FL);
                    break;
                case 6:
                    r = (int) ((i1 >> 40) & 0x3FL);
                    break;
                case 7:
                    r = (int) ((i1 >> 46) & 0x3FL);
                    break;
                case 8:
                    r = (int) ((i1 >> 52) & 0x3FL);
                    break;
                case 9:
                    r = (int) ((i1 >> 58) & 0x3FL);
                    break;
                case 10:
                    r = (int) ((i2 >> 4) & 0x3FL);
                    break;
                case 11:
                    r = (int) ((i2 >> 10) & 0x3FL);
                    break;
                case 12:
                    r = (int) ((i2 >> 16) & 0x3FL);
                    break;
                case 13:
                    r = (int) ((i2 >> 22) & 0x3FL);
                    break;
                case 14:
                    r = (int) ((i2 >> 28) & 0x3FL);
                    break;
                case 15:
                    r = (int) ((i2 >> 34) & 0x3FL);
                    break;
                case 16:
                    r = (int) ((i2 >> 40) & 0x3FL);
                    break;
                case 17:
                    r = (int) ((i2 >> 46) & 0x3FL);
                    break;
                case 18:
                    r = (int) ((i2 >> 52) & 0x3FL);
                    break;
                case 19:
                    r = (int) ((i2 >> 58) & 0x3FL);
                    break;
                case 20:
                    r = (int) ((i3 >> 4) & 0x3FL);
                    break;
                case 21:
                    r = (int) ((i3 >> 10) & 0x3FL);
                    break;
                case 22:
                    r = (int) ((i3 >> 16) & 0x3FL);
                    break;
                case 23:
                    r = (int) ((i3 >> 22) & 0x3FL);
                    break;
                case 24:
                    r = (int) ((i3 >> 28) & 0x3FL);
                    break;
                case 25:
                    r = (int) ((i3 >> 34) & 0x3FL);
                    break;
                case 26:
                    r = (int) ((i3 >> 40) & 0x3FL);
                    break;
                case 27:
                    r = (int) ((i3 >> 46) & 0x3FL);
                    break;
                case 28:
                    r = (int) ((i3 >> 52) & 0x3FL);
                    break;
                case 29:
                    r = (int) ((i3 >> 58) & 0x3FL);
                    break;
            }
            return decode6BitChar(r);
        }

        @Override
        public String toString() {
            int n = length();
            char[] r = new char[n];
            for (int j = 0; j < n; j++) {
                r[j] = charAt(j);
            }
            return new String(r);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof Fixed6Bit_21_30) {
                Fixed6Bit_21_30 otherString = (Fixed6Bit_21_30) object;
                return i1 == otherString.i1 && i2 == otherString.i2 && i3 == otherString.i3;
            }
            return false;
        }

        @Override
        public int hashCode() {
            long res = i1 + 31 * (i2 + i3 * 31);
            res = (res + (res >> 32)) & 0xFFFFFFFFL;
            return (int) res;
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return CharSequences.create(toString().substring(start, end));
        }

        @Override
        public int compareTo(CharSequence o) {
            return Comparator.compare(this, o);
        }
    }

    /**
     * compact char sequence implementation for strings in range 8-15 characters
     * size: 8 + 4*4 = 24 bytes for all strings vs String impl occupying
     */
    private static final class Fixed_8_15 implements CompactCharSequence, Comparable<CharSequence> {

        private final int i1;
        private final int i2;
        private final int i3;
        private final int i4;

        @SuppressWarnings("fallthrough")
        private Fixed_8_15(byte[] b, int n) {
            int a1 = n;
            int a2 = 0;
            int a3 = 0;
            int a4 = 0;
            switch (n) {
                case 15:
                    a4 += (b[14] & 0xFF) << 24;
                case 14:
                    a4 += (b[13] & 0xFF) << 16;
                case 13:
                    a4 += (b[12] & 0xFF) << 8;
                case 12:
                    a4 += b[11] & 0xFF;
                case 11:
                    a3 += (b[10] & 0xFF) << 24;
                case 10:
                    a3 += (b[9] & 0xFF) << 16;
                case 9:
                    a3 += (b[8] & 0xFF) << 8;
                case 8:
                    a3 += b[7] & 0xFF;
                case 7:
                    a2 += (b[6] & 0xFF) << 24;
                case 6:
                    a2 += (b[5] & 0xFF) << 16;
                case 5:
                    a2 += (b[4] & 0xFF) << 8;
                case 4:
                    a2 += b[3] & 0xFF;
                case 3:
                    a1 += (b[2] & 0xFF) << 24;
                case 2:
                    a1 += (b[1] & 0xFF) << 16;
                case 1:
                    a1 += (b[0] & 0xFF) << 8;
                case 0:
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            i1 = a1;
            i2 = a2;
            i3 = a3;
            i4 = a4;
        }

        @Override
        public int length() {
            return i1 & 0xFF;
        }

        @Override
        public char charAt(int index) {
            int r = 0;
            switch (index) {
                case 0:
                    r = (i1 & 0xFF00) >> 8;
                    break;
                case 1:
                    r = (i1 & 0xFF0000) >> 16;
                    break;
                case 2:
                    r = (i1 >> 24) & 0xFF;
                    break;
                case 3:
                    r = i2 & 0xFF;
                    break;
                case 4:
                    r = (i2 & 0xFF00) >> 8;
                    break;
                case 5:
                    r = (i2 & 0xFF0000) >> 16;
                    break;
                case 6:
                    r = (i2 >> 24) & 0xFF;
                    break;
                case 7:
                    r = i3 & 0xFF;
                    break;
                case 8:
                    r = (i3 & 0xFF00) >> 8;
                    break;
                case 9:
                    r = (i3 & 0xFF0000) >> 16;
                    break;
                case 10:
                    r = (i3 >> 24) & 0xFF;
                    break;
                case 11:
                    r = i4 & 0xFF;
                    break;
                case 12:
                    r = (i4 & 0xFF00) >> 8;
                    break;
                case 13:
                    r = (i4 & 0xFF0000) >> 16;
                    break;
                case 14:
                    r = (i4 >> 24) & 0xFF;
                    break;
            }
            return (char) r;
        }

        @Override
        public String toString() {
            int n = length();
            char[] r = new char[n];
            for (int i = 0; i < n; i++) {
                r[i] = charAt(i);
            }
            return new String(r);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof Fixed_8_15) {
                Fixed_8_15 otherString = (Fixed_8_15) object;
                return i1 == otherString.i1 && i2 == otherString.i2 && i3 == otherString.i3 && i4 == otherString.i4;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return i1 + 31 * (i2 + 31 * (i3 + 31 * i4));
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return CharSequences.create(toString().substring(start, end));
        }

        @Override
        public int compareTo(CharSequence o) {
            return Comparator.compare(this, o);
        }
    }

    /**
     * compact char sequence implementation for strings in range 16-23 characters
     * size: 8 + 3*8 = 32 bytes for all strings vs String impl occupying
     */
    private static final class Fixed_16_23 implements CompactCharSequence, Comparable<CharSequence> {

        private final long i1;
        private final long i2;
        private final long i3;

        @SuppressWarnings("fallthrough")
        private Fixed_16_23(byte[] b, int n) {
            long a1 = 0;
            long a2 = 0;
            long a3 = 0;
            switch (n) {
                case 23:
                    a3 += (b[22] & 0xFFL) << 24;
                case 22:
                    a3 += (b[21] & 0xFF) << 16;
                case 21:
                    a3 += (b[20] & 0xFF) << 8;
                case 20:
                    a3 += (b[19] & 0xFF);
                    a3 <<= 32;
                case 19:
                    a3 += (b[18] & 0xFFL) << 24;
                case 18:
                    a3 += (b[17] & 0xFF) << 16;
                case 17:
                    a3 += (b[16] & 0xFF) << 8;
                case 16:
                    a3 += b[15] & 0xFF;
                case 15:
                    a2 += (b[14] & 0xFFL) << 24;
                case 14:
                    a2 += (b[13] & 0xFF) << 16;
                case 13:
                    a2 += (b[12] & 0xFF) << 8;
                case 12:
                    a2 += (b[11] & 0xFF);
                    a2 <<= 32;
                case 11:
                    a2 += (b[10] & 0xFFL) << 24;
                case 10:
                    a2 += (b[9] & 0xFF) << 16;
                case 9:
                    a2 += (b[8] & 0xFF) << 8;
                case 8:
                    a2 += b[7] & 0xFF;
                case 7:
                    a1 += (b[6] & 0xFFL) << 24;
                case 6:
                    a1 += (b[5] & 0xFF) << 16;
                case 5:
                    a1 += (b[4] & 0xFF) << 8;
                case 4:
                    a1 += (b[3] & 0xFF);
                    a1 <<= 32;
                case 3:
                    a1 += (b[2] & 0xFFL) << 24;
                case 2:
                    a1 += (b[1] & 0xFF) << 16;
                case 1:
                    a1 += (b[0] & 0xFF) << 8;
                case 0:
                    a1 += n;
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            i1 = a1;
            i2 = a2;
            i3 = a3;
        }

        @Override
        public int length() {
            return (int) (i1 & 0xFF);
        }

        @Override
        public char charAt(int index) {
            int r = 0;
            switch (index) {
                case 0:
                    r = (int) ((i1 >> 8) & 0xFFL);
                    break;
                case 1:
                    r = (int) ((i1 >> 16) & 0xFFL);
                    break;
                case 2:
                    r = (int) ((i1 >> 24) & 0xFFL);
                    break;
                case 3:
                    r = (int) ((i1 >> 32) & 0xFFL);
                    break;
                case 4:
                    r = (int) ((i1 >> 40) & 0xFFL);
                    break;
                case 5:
                    r = (int) ((i1 >> 48) & 0xFFL);
                    break;
                case 6:
                    r = (int) ((i1 >> 56) & 0xFFL);
                    break;
                case 7:
                    r = (int) (i2 & 0xFFL);
                    break;
                case 8:
                    r = (int) ((i2 >> 8) & 0xFFL);
                    break;
                case 9:
                    r = (int) ((i2 >> 16) & 0xFFL);
                    break;
                case 10:
                    r = (int) ((i2 >> 24) & 0xFFL);
                    break;
                case 11:
                    r = (int) ((i2 >> 32) & 0xFFL);
                    break;
                case 12:
                    r = (int) ((i2 >> 40) & 0xFFL);
                    break;
                case 13:
                    r = (int) ((i2 >> 48) & 0xFFL);
                    break;
                case 14:
                    r = (int) ((i2 >> 56) & 0xFFL);
                    break;
                case 15:
                    r = (int) (i3 & 0xFFL);
                    break;
                case 16:
                    r = (int) ((i3 >> 8) & 0xFFL);
                    break;
                case 17:
                    r = (int) ((i3 >> 16) & 0xFFL);
                    break;
                case 18:
                    r = (int) ((i3 >> 24) & 0xFFL);
                    break;
                case 19:
                    r = (int) ((i3 >> 32) & 0xFFL);
                    break;
                case 20:
                    r = (int) ((i3 >> 40) & 0xFFL);
                    break;
                case 21:
                    r = (int) ((i3 >> 48) & 0xFFL);
                    break;
                case 22:
                    r = (int) ((i3 >> 56) & 0xFFL);
                    break;
            }
            return (char) r;
        }

        @Override
        public String toString() {
            int n = length();
            char[] r = new char[n];
            for (int i = 0; i < n; i++) {
                r[i] = charAt(i);
            }
            return new String(r);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof Fixed_16_23) {
                Fixed_16_23 otherString = (Fixed_16_23) object;
                return i1 == otherString.i1 && i2 == otherString.i2 && i3 == otherString.i3;
            }
            return false;
        }

        @Override
        public int hashCode() {
            long res = i1 + 31 * (i2 + 31 * i3);
            res = (res + (res >> 32)) & 0xFFFFFFFFL;
            return (int) res;
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return CharSequences.create(toString().substring(start, end));
        }

        @Override
        public int compareTo(CharSequence o) {
            return Comparator.compare(this, o);
        }
    }

    /**
     * compact char sequence implementation based on char[] array
     * size: 8 + 4 + 4 (= 16 bytes) + sizeof ('value')
     * it is still more efficient than String, because string stores length in field
     * and it costs 20 bytes aligned into 24
     */
    private static final class CharBasedSequence implements CompactCharSequence, Comparable<CharSequence> {

        private final char[] value;
        private int hash;

        private CharBasedSequence(char[] v) {
            value = v;
        }

        @Override
        public int length() {
            return value.length;
        }

        @Override
        public char charAt(int index) {
            return value[index];
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof CharBasedSequence) {
                CharBasedSequence otherString = (CharBasedSequence) object;
                if (hash != 0 && otherString.hash != 0) {
                    if (hash != otherString.hash) {
                        return false;
                    }
                }
                return Arrays.equals(value, otherString.value);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int h = hash;
            if (h == 0) {
                int n = value.length;
                for (int i = 0; i < n; i++) {
                    h = 31 * h + value[i];
                }
                hash = h;
            }
            return h;
        }

        @Override
        public CharSequence subSequence(int beginIndex, int endIndex) {
            return CharSequences.create(value, beginIndex, endIndex-beginIndex);
        }

        @Override
        public String toString() {
            return new String(value);
        }

        @Override
        public int compareTo(CharSequence o) {
            return CharSequenceComparator.compareCharBasedWithOther(this, o);
        }
    }

    /**
     * compact char sequence implementation based on byte[]
     * size: 8 + 4 + 4 (= 16 bytes) + sizeof ('value')
     */
    private static final class ByteBasedSequence implements CompactCharSequence, Comparable<CharSequence> {

        private final byte[] value;
        private int hash;

        private ByteBasedSequence(byte[] b) {
            value = b;
        }

        @Override
        public int length() {
            return value.length;
        }

        @Override
        public char charAt(int index) {
            int r = value[index] & 0xFF;
            return (char) r;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof ByteBasedSequence) {
                ByteBasedSequence otherString = (ByteBasedSequence) object;
                if (hash != 0 && otherString.hash != 0) {
                    if (hash != otherString.hash) {
                        return false;
                    }
                }
                return Arrays.equals(value, otherString.value);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int h = hash;
            if (h == 0) {
                int n = value.length;
                for (int i = 0; i < n; i++) {
                    h = 31 * h + value[i];
                }
                hash = h;
            }
            return h;
        }

        @Override
        public CharSequence subSequence(int beginIndex, int endIndex) {
            return create(value, beginIndex, endIndex-beginIndex);
        }

        @Override
        public String toString() {
            char[] r = toChars();
            return new String(r);
        }

        private char[] toChars() {
            int n = value.length;
            char[] r = new char[n];
            for (int i = 0; i < n; i++) {
                int c = value[i] & 0xFF;
                r[i] = (char) c;
            }
            return r;
        }

        @Override
        public int compareTo(CharSequence o) {
            return CharSequenceComparator.compareByteBasedWithOther(this, o);
        }
    }

    private static final CompactCharSequence EMPTY = new Fixed_0_7(new byte[0], 0);
    private static final CharSequenceComparator Comparator = new CharSequenceComparator();

    /**
     * performance tuned comparator to prevent charAt calls when possible
     */
    private static class CharSequenceComparator implements Comparator<CharSequence> {

        @Override
        public int compare(CharSequence o1, CharSequence o2) {
            if (o1 instanceof ByteBasedSequence) {
                return compareByteBasedWithOther((ByteBasedSequence)o1, o2);
            } else if (o2 instanceof ByteBasedSequence) {
                return -compareByteBasedWithOther((ByteBasedSequence) o2, o1);
            } else if (o1 instanceof CharBasedSequence) {
                return compareCharBasedWithOther((CharBasedSequence)o1, o2);
            } else if (o2 instanceof CharBasedSequence) {
                return -compareCharBasedWithOther((CharBasedSequence)o2, o1);
            }
            int len1 = o1.length();
            int len2 = o2.length();
            int n = Math.min(len1, len2);
            int k = 0;
            while (k < n) {
                char c1 = o1.charAt(k);
                char c2 = o2.charAt(k);
                if (c1 != c2) {
                    return c1 - c2;
                }
                k++;
            }
            return len1 - len2;
        }

        //<editor-fold defaultstate="collapsed" desc="Private methods">
        private static int compareByteBased(ByteBasedSequence bbs1, ByteBasedSequence bbs2) {
            int len1 = bbs1.value.length;
            int len2 = bbs2.value.length;
            int n = Math.min(len1, len2);
            int k = 0;
            while (k < n) {
                if (bbs1.value[k] != bbs2.value[k]) {
                    return (bbs1.value[k] & 0xFF) - (bbs2.value[k] & 0xFF);
                }
                k++;
            }
            return len1 - len2;
        }

        private static int compareCharBased(CharBasedSequence cbs1, CharBasedSequence cbs2) {
            int len1 = cbs1.value.length;
            int len2 = cbs2.value.length;
            int n = Math.min(len1, len2);
            int k = 0;
            while (k < n) {
                if (cbs1.value[k] != cbs2.value[k]) {
                    return cbs1.value[k] - cbs2.value[k];
                }
                k++;
            }
            return len1 - len2;
        }

        private static int compareByteBasedWithCharBased(ByteBasedSequence bbs1, CharBasedSequence cbs2) {
            int len1 = bbs1.value.length;
            int len2 = cbs2.value.length;
            int n = Math.min(len1, len2);
            int k = 0;
            while (k < n) {
                int c1 = bbs1.value[k] & 0xFF;
                int c2 = cbs2.value[k];
                if (c1 != c2) {
                    return c1 - c2;
                }
                k++;
            }
            return len1 - len2;
        }

        private static int compareByteBasedWithOther(ByteBasedSequence bbs1, CharSequence o2) {
            if (o2 instanceof ByteBasedSequence) {
                return compareByteBased(bbs1, (ByteBasedSequence) o2);
            } else if (o2 instanceof CharBasedSequence) {
                return compareByteBasedWithCharBased(bbs1, (CharBasedSequence) o2);
            }
            int len1 = bbs1.value.length;
            int len2 = o2.length();
            int n = Math.min(len1, len2);
            int k = 0;
            int c1, c2;
            while (k < n) {
                c1 = bbs1.value[k] & 0xFF;
                c2 = o2.charAt(k);
                if (c1 != c2) {
                    return c1 - c2;
                }
                k++;
            }
            return len1 - len2;
        }

        private static int compareCharBasedWithOther(CharBasedSequence cbs1, CharSequence o2) {
            if (o2 instanceof CharBasedSequence) {
                return compareCharBased(cbs1, (CharBasedSequence) o2);
            } else if (o2 instanceof ByteBasedSequence) {
                return -compareByteBasedWithCharBased((ByteBasedSequence) o2, cbs1);
            }
            int len1 = cbs1.value.length;
            int len2 = o2.length();
            int n = Math.min(len1, len2);
            int k = 0;
            int c1, c2;
            while (k < n) {
                c1 = cbs1.value[k];
                c2 = o2.charAt(k);
                if (c1 != c2) {
                    return c1 - c2;
                }
                k++;
            }
            return len1 - len2;
        }
        //</editor-fold>
    }

    /**
     * marker interface for compact char sequence implementations
     */
    private interface CompactCharSequence extends CharSequence {
    }

    /**
     * private constructor for utilities class
     */
    private CharSequences() {
    }

    //</editor-fold>
}