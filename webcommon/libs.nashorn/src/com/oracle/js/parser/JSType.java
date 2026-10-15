/*
 * Copyright (c) 2010, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.oracle.js.parser;


// @formatter:off
/**
 * Representation for ECMAScript types - this maps directly to the ECMA script standard
 */
public final class JSType {
    private JSType() {
    }

    /** Max value for an uint32 in JavaScript */
    private static final long MAX_UINT = 0xFFFF_FFFFL;

    private static final double INT32_LIMIT = 4294967296.0;

    /**
     * Returns true if double number can be represented as an int
     *
     * @param number a long to inspect
     *
     * @return true for int representable longs
     */
    public static boolean isRepresentableAsInt(final long number) {
        return (int) number == number;
    }

    /**
     * Returns true if double number can be represented as an int. Note that it returns true for negative
     * zero. If you need to exclude negative zero, use {@link #isStrictlyRepresentableAsInt(double)}.
     *
     * @param number a double to inspect
     *
     * @return true for int representable doubles
     */
    public static boolean isRepresentableAsInt(final double number) {
        return (int) number == number;
    }

    /**
     * Returns true if double number can be represented as an int. Note that it returns false for negative
     * zero. If you don't need to distinguish negative zero, use {@link #isRepresentableAsInt(double)}.
     *
     * @param number a double to inspect
     *
     * @return true for int representable doubles
     */
    public static boolean isStrictlyRepresentableAsInt(final double number) {
        return isRepresentableAsInt(number) && isNotNegativeZero(number);
    }

    /**
     * Returns true if Object can be represented as an int
     *
     * @param obj an object to inspect
     *
     * @return true for int representable objects
     */
    public static boolean isRepresentableAsInt(final Object obj) {
        if (obj instanceof Number) {
            return isRepresentableAsInt(((Number) obj).doubleValue());
        }
        return false;
    }

    /**
     * Returns true if double number can be represented as a long. Note that it returns true for negative
     * zero. If you need to exclude negative zero, use {@link #isStrictlyRepresentableAsLong(double)}.
     *
     * @param number a double to inspect
     * @return true for long representable doubles
     */
    public static boolean isRepresentableAsLong(final double number) {
        return (long) number == number;
    }

    /**
     * Returns true if double number can be represented as a long. Note that it returns false for negative
     * zero. If you don't need to distinguish negative zero, use {@link #isRepresentableAsLong(double)}.
     *
     * @param number a double to inspect
     *
     * @return true for long representable doubles
     */
    public static boolean isStrictlyRepresentableAsLong(final double number) {
        return isRepresentableAsLong(number) && isNotNegativeZero(number);
    }

    /**
     * Returns true if Object can be represented as a long
     *
     * @param obj an object to inspect
     *
     * @return true for long representable objects
     */
    public static boolean isRepresentableAsLong(final Object obj) {
        if (obj instanceof Number) {
            return isRepresentableAsLong(((Number) obj).doubleValue());
        }
        return false;
    }

    /**
     * Returns true if the number is the negative zero ({@code -0.0d}).
     *
     * @param number the number to test
     * @return true if it is the negative zero, false otherwise.
     */
    public static boolean isNegativeZero(final double number) {
        return number == 0.0d && Double.doubleToRawLongBits(number) == 0x8000000000000000L;
    }

    /**
     * Returns true if the number is not the negative zero ({@code -0.0d}).
     * @param number the number to test
     * @return true if it is not the negative zero, false otherwise.
     */
    private static boolean isNotNegativeZero(final double number) {
        return Double.doubleToRawLongBits(number) != 0x8000000000000000L;
    }

    /**
     * JavaScript compliant conversion of number to boolean
     *
     * @param num a number
     *
     * @return a boolean
     */
    public static boolean toBoolean(final double num) {
        return num != 0 && !Double.isNaN(num);
    }

    /**
     * JavaScript compliant conversion of Object to boolean See ECMA 9.2 ToBoolean
     *
     * @param obj an object
     *
     * @return a boolean
     */
    public static boolean toBoolean(final Object obj) {
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }

        if (obj == null) {
            return false;
        }

        if (obj instanceof Number) {
            final double num = ((Number) obj).doubleValue();
            return num != 0 && !Double.isNaN(num);
        }

        if (obj instanceof String) {
            return ((String) obj).length() > 0;
        }

        return true;
    }

    /**
     * JavaScript compliant conversion of number to String
     *
     * @param num a number
     * @param radix a radix for the conversion
     *
     * @return a string
     */
    public static String toString(final double num, final int radix) {
        assert radix >= 2 && radix <= 36 : "invalid radix";

        if (isRepresentableAsInt(num)) {
            return Integer.toString((int) num, radix);
        }

        if (num == Double.POSITIVE_INFINITY) {
            return "Infinity";
        }

        if (num == Double.NEGATIVE_INFINITY) {
            return "-Infinity";
        }

        if (Double.isNaN(num)) {
            return "NaN";
        }

        if (num == 0.0) {
            return "0";
        }

        final String chars = "0123456789abcdefghijklmnopqrstuvwxyz";
        final StringBuilder sb = new StringBuilder();

        final boolean negative = num < 0.0;
        final double signedNum = negative ? -num : num;

        double intPart = Math.floor(signedNum);
        double decPart = signedNum - intPart;

        // encode integer part from least significant digit, then reverse
        do {
            final double remainder = intPart % radix;
            sb.append(chars.charAt((int) remainder));
            intPart -= remainder;
            intPart /= radix;
        } while (intPart >= 1.0);

        if (negative) {
            sb.append('-');
        }
        sb.reverse();

        // encode decimal part
        if (decPart > 0.0) {
            final int dot = sb.length();
            sb.append('.');
            do {
                decPart *= radix;
                final double d = Math.floor(decPart);
                sb.append(chars.charAt((int) d));
                decPart -= d;
            } while (decPart > 0.0 && sb.length() - dot < 1100);
            // somewhat arbitrarily use same limit as V8
        }

        return sb.toString();
    }

    /**
     * JavaScript compliant conversion of Object to number See ECMA 9.3 ToNumber
     *
     * @param obj an object
     *
     * @return a number
     */
    public static double toNumber(final Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        return toNumberGeneric(obj);
    }

    /**
     * Digit representation for a character
     *
     * @param ch a character
     * @param radix radix
     *
     * @return the digit for this character
     */
    public static int digit(final char ch, final int radix) {
        return digit(ch, radix, false);
    }

    /**
     * Digit representation for a character
     *
     * @param ch a character
     * @param radix radix
     * @param onlyIsoLatin1 iso latin conversion only
     *
     * @return the digit for this character
     */
    public static int digit(final char ch, final int radix, final boolean onlyIsoLatin1) {
        final char maxInRadix = (char) ('a' + (radix - 1) - 10);
        final char c = Character.toLowerCase(ch);

        if (c >= 'a' && c <= maxInRadix) {
            return Character.digit(ch, radix);
        }

        if (Character.isDigit(ch)) {
            if (!onlyIsoLatin1 || ch >= '0' && ch <= '9') {
                return Character.digit(ch, radix);
            }
        }

        return -1;
    }

    /**
     * JavaScript compliant String to number conversion
     *
     * @param str a string
     *
     * @return a number
     */
    public static double toNumber(final String str) {
        int end = str.length();
        if (end == 0) {
            return 0.0; // Empty string
        }

        int start = 0;
        char f = str.charAt(0);

        while (Lexer.isJSWhitespace(f)) {
            if (++start == end) {
                return 0.0d; // All whitespace string
            }
            f = str.charAt(start);
        }

        // Guaranteed to terminate even without start >= end check, as the previous loop found at
        // least one non-whitespace character.
        while (Lexer.isJSWhitespace(str.charAt(end - 1))) {
            end--;
        }

        final boolean negative;
        if (f == '-') {
            if (++start == end) {
                return Double.NaN; // Single-char "-" string
            }
            f = str.charAt(start);
            negative = true;
        } else {
            if (f == '+') {
                if (++start == end) {
                    return Double.NaN; // Single-char "+" string
                }
                f = str.charAt(start);
            }
            negative = false;
        }

        final double value;
        if (start + 1 < end && f == '0' && Character.toLowerCase(str.charAt(start + 1)) == 'x') {
            // decode hex string
            value = parseRadix(str.toCharArray(), start + 2, end, 16);
        } else if (f == 'I' && end - start == 8 && str.regionMatches(start, "Infinity", 0, 8)) {
            return negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        } else {
            // Fast (no NumberFormatException) path to NaN for non-numeric strings.
            for (int i = start; i < end; i++) {
                f = str.charAt(i);
                if ((f < '0' || f > '9') && f != '.' && f != 'e' && f != 'E' && f != '+' && f != '-') {
                    return Double.NaN;
                }
            }
            try {
                value = Double.parseDouble(str.substring(start, end));
            } catch (final NumberFormatException e) {
                return Double.NaN;
            }
        }

        return negative ? -value : value;
    }

    /**
     * Converts an Object to long.
     *
     * <p>
     * Note that this returns {@link java.lang.Long#MAX_VALUE} or {@link java.lang.Long#MIN_VALUE}
     * for double values that exceed the long range, including positive and negative Infinity. It is
     * the caller's responsibility to handle such values correctly.
     * </p>
     *
     * @param obj an object
     * @return a long
     */
    public static long toLong(final Object obj) {
        return obj instanceof Long ? ((Long)obj) : toLong(toNumber(obj));
    }

    /**
     * Converts a double to long.
     *
     * @param num the double to convert
     * @return the converted long value
     */
    public static long toLong(final double num) {
        return (long) num;
    }

    /**
     * JavaScript compliant Object to int32 conversion See ECMA 9.5 ToInt32
     *
     * @param obj an object
     * @return an int32
     */
    public static int toInt32(final Object obj) {
        return toInt32(toNumber(obj));
    }

    // Minimum and maximum range between which every long value can be precisely represented as a double.
    private static final long MAX_PRECISE_DOUBLE = 1L << 53;
    private static final long MIN_PRECISE_DOUBLE = -MAX_PRECISE_DOUBLE;

    /**
     * JavaScript compliant long to int32 conversion
     *
     * @param num a long
     * @return an int32
     */
    public static int toInt32(final long num) {
        return (int) (num >= MIN_PRECISE_DOUBLE && num <= MAX_PRECISE_DOUBLE ? num : (long) (num % INT32_LIMIT));
    }

    /**
     * JavaScript compliant number to int32 conversion
     *
     * @param num a number
     * @return an int32
     */
    public static int toInt32(final double num) {
        return (int) doubleToInt32(num);
    }

    /**
     * JavaScript compliant Object to uint32 conversion
     *
     * @param obj an object
     * @return a uint32
     */
    public static long toUint32(final Object obj) {
        return toUint32(toNumber(obj));
    }

    /**
     * JavaScript compliant number to uint32 conversion
     *
     * @param num a number
     * @return a uint32
     */
    public static long toUint32(final double num) {
        return doubleToInt32(num) & MAX_UINT;
    }

    /**
     * JavaScript compliant Object to uint16 conversion ECMA 9.7 ToUint16: (Unsigned 16 Bit Integer)
     *
     * @param obj an object
     * @return a uint16
     */
    public static int toUint16(final Object obj) {
        return toUint16(toNumber(obj));
    }

    /**
     * JavaScript compliant number to uint16 conversion
     *
     * @param num a number
     * @return a uint16
     */
    public static int toUint16(final int num) {
        return num & 0xffff;
    }

    /**
     * JavaScript compliant number to uint16 conversion
     *
     * @param num a number
     * @return a uint16
     */
    public static int toUint16(final long num) {
        return (int) num & 0xffff;
    }

    /**
     * JavaScript compliant number to uint16 conversion
     *
     * @param num a number
     * @return a uint16
     */
    public static int toUint16(final double num) {
        return (int) doubleToInt32(num) & 0xffff;
    }

    private static long doubleToInt32(final double num) {
        final int exponent = Math.getExponent(num);
        if (exponent < 31) {
            return (long) num;  // Fits into 32 bits
        }
        if (exponent >= 84) {
            // Either infinite or NaN or so large that shift / modulo will produce 0
            // (52 bit mantissa + 32 bit target width).
            return 0;
        }
        // This is rather slow and could probably be sped up using bit-fiddling.
        final double d = num >= 0 ? Math.floor(num) : Math.ceil(num);
        return (long) (d % INT32_LIMIT);
    }

    /**
     * Check whether a number is finite
     *
     * @param num a number
     * @return true if finite
     */
    public static boolean isFinite(final double num) {
        return !Double.isInfinite(num) && !Double.isNaN(num);
    }

    private static double parseRadix(final char[] chars, final int start, final int length, final int radix) {
        int pos = 0;

        for (int i = start; i < length; i++) {
            if (digit(chars[i], radix) == -1) {
                return Double.NaN;
            }
            pos++;
        }

        if (pos == 0) {
            return Double.NaN;
        }

        double value = 0.0;
        for (int i = start; i < start + pos; i++) {
            value *= radix;
            value += digit(chars[i], radix);
        }

        return value;
    }

    private static double toNumberGeneric(final Object obj) {
        if (obj == null) {
            return +0.0;
        }

        if (obj instanceof String) {
            return toNumber((String) obj);
        }

        if (obj instanceof Boolean) {
            return (Boolean) obj ? 1 : +0.0;
        }

        return Double.NaN;
    }
}
