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
package org.netbeans.modules.db.dataview.util;

import org.netbeans.modules.db.dataview.meta.DBException;
import org.openide.util.NbBundle;

public class BinaryToStringConverter {

    static class ConversionConstants {

        int radix; // the base radix
        int width; // number of chars used to represent byte

        ConversionConstants(int w, int r) {
            width = w;
            radix = r;
        }
    }
    public static final int BINARY = 2;
    public static final int DECIMAL = 10;
    public static final int HEX = 16;
    public static final int OCTAL = 8;
    static ConversionConstants decimal = new ConversionConstants(3, 10);
    static ConversionConstants hex = new ConversionConstants(2, 16);
    private static ConversionConstants binary = new ConversionConstants(8, 2);
    private static ConversionConstants octal = new ConversionConstants(3, 8);
    /**
     * List of characters considered "printable".
     */
    private static String printable = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ`~!@#$%^&*()-_=+[{]}\\|;:'\",<.>/? "; // NOI18N

    /**
     * Convert from an array of Bytes into a string.
     */
    public static String convertToString(byte[] data, int base, boolean showAscii) {

        if (data == null) {
            return null;
        }

        StringBuilder buf = new StringBuilder(20);
        ConversionConstants convConst = getConstants(base);

        // Convert each byte and put into string buffer
        for (int i = 0; i < data.length; i++) {
            int value = data[i];
            String s = null;

            // if user wants to see ASCII chars as characters,
            // see if this is one that should be displayed that way
            if (showAscii) {
                char c = (char) value;
                if (printable.indexOf(c) > -1) {
                    s = Character.toString(c) + "          ".substring(10 - (convConst.width - 1)); // NOI18N
                }
            }

            // if user is not looking for ASCII chars, or if this one is one that
            // is not printable, then convert it into numeric form
            if (s == null) {
                switch (base) {
                    case DECIMAL:
                        // convert signed to unsigned
                        if (value < 0) {
                            value = 256 + value;
                        }
                        s = Integer.toString(value);
                        break;
                    case OCTAL:
                        s = Integer.toOctalString(value);
                        break;
                    case BINARY:
                        s = Integer.toBinaryString(value);
                        break;
                    case HEX: // fall through to default

                    default:
                        s = Integer.toHexString(value);
                }
                // some formats (e.g. hex & octal) extend a negative number to multiple
                // places (e.g. FC becomes FFFC), so chop off extra stuff in front
                if (s.length() > convConst.width) {
                    s = s.substring(s.length() - convConst.width);

                // front pad with zeros and add to output
                }
                if (s.length() < convConst.width) {
                    buf.append("00000000".substring(8 - (convConst.width - s.length()))); // NOI18N
                }
            }
            buf.append(showAscii ? s.trim() : s);
        //buf.append("  "); // always add spaces at end for consistancy

        }
        return buf.toString();
    }

    private static ConversionConstants getConstants(int base) {
        switch (base) {
            case DECIMAL:
                return decimal;
            case OCTAL:
                return octal;
            case BINARY:
                return binary;
            case HEX: // default to hex if unknown base passed in

            default:
                return hex;
        }
    }

    public static byte[] convertBitStringToBytes(String s) throws DBException {
        int shtBits = s.length() % 8;
        s = (shtBits > 0 ? "00000000".substring(0, 8 - shtBits) + s : s); // NOI18N

        byte[] buf = new byte[s.length() / 8];

        int bit = 0, index = 0;
        for (int i = 0; i < s.length(); i++) {
            if ('1' == s.charAt(i)) { // NOI18N
                int b = 1 << (7 - bit);
                buf[index] |= b;
            } else if ('0' != s.charAt(i)) { // NOI18N
                throw new DBException(NbBundle.getMessage(BinaryToStringConverter.class, "BinaryToStringConverter_InvalidBitFormat", s.charAt(i), i)); // NOI18N
            }
            bit++;
            if (bit > 7) {
                bit = 0;
                index++;
            }
        }
        return buf;
    }

    private BinaryToStringConverter() {
    }
}

