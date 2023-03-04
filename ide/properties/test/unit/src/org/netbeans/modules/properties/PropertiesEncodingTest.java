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

package org.netbeans.modules.properties;

import java.nio.charset.CharacterCodingException;
import java.util.Arrays;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.properties.PropertiesEncoding.PropCharset;
import org.netbeans.modules.properties.PropertiesEncoding.PropCharsetEncoder;
import org.netbeans.modules.properties.PropertiesEncoding.PropCharsetDecoder;

/**
 * 
 * @author  Marian Petras
 */
public class PropertiesEncodingTest extends NbTestCase {
    
    public PropertiesEncodingTest() {
        super("Encoding test");
    }
    
    public void testEncodingOfSingleChar() {
        final PropCharsetEncoder encoder
                = new PropCharsetEncoder(new PropCharset());
        compare(encoder.encodeCharForTests((char) 0x5c),    /* backslash */
                new byte[] {'\\'});
        compare(encoder.encodeCharForTests((char) 0x09),    /* tab */
                new byte[] {0x09});
        compare(encoder.encodeCharForTests((char) 0x0c),    /* FF */
                new byte[] {0x0c});
        compare(encoder.encodeCharForTests((char) 0x0a),    /* NL */
                new byte[] {0x0a});
        compare(encoder.encodeCharForTests((char) 0x0d),    /* CR */
                new byte[] {0x0d});
        compare(encoder.encodeCharForTests((char) 0x00),
                new byte[] {'\\', 'u', '0', '0', '0', '0'});
        compare(encoder.encodeCharForTests((char) 0x01),
                new byte[] {'\\', 'u', '0', '0', '0', '1'});
        compare(encoder.encodeCharForTests((char) 0x0b),
                new byte[] {'\\', 'u', '0', '0', '0', 'B'});
        compare(encoder.encodeCharForTests((char) 0x19),
                new byte[] {'\\', 'u', '0', '0', '1', '9'});
        compare(encoder.encodeCharForTests((char) 0x20),
                new byte[] {' '});
        compare(encoder.encodeCharForTests((char) 0x21),
                new byte[] {'!'});
        compare(encoder.encodeCharForTests((char) 0x32),
                new byte[] {'2'});
        compare(encoder.encodeCharForTests((char) 0x43),
                new byte[] {'C'});
        compare(encoder.encodeCharForTests((char) 0x54),
                new byte[] {'T'});
        compare(encoder.encodeCharForTests((char) 0x65),
                new byte[] {'e'});
        compare(encoder.encodeCharForTests((char) 0x78),
                new byte[] {'x'});
        compare(encoder.encodeCharForTests((char) 0x7d),
                new byte[] {'}'});
        compare(encoder.encodeCharForTests((char) 0x7e),
                new byte[] {'~'});
        compare(encoder.encodeCharForTests((char) 0x7f),
                new byte[] {'\\', 'u', '0', '0', '7', 'F'});
        compare(encoder.encodeCharForTests((char) 0x81),
                new byte[] {'\\', 'u', '0', '0', '8', '1'});
        compare(encoder.encodeCharForTests((char) 0x89),
                new byte[] {'\\', 'u', '0', '0', '8', '9'});
        compare(encoder.encodeCharForTests((char) 0x8f),
                new byte[] {'\\', 'u', '0', '0', '8', 'F'});
        compare(encoder.encodeCharForTests((char) 0x90),
                new byte[] {'\\', 'u', '0', '0', '9', '0'});
        compare(encoder.encodeCharForTests((char) 0xa0),
                new byte[] {'\\', 'u', '0', '0', 'A', '0'});
        compare(encoder.encodeCharForTests((char) 0xad),
                new byte[] {'\\', 'u', '0', '0', 'A', 'D'});
        compare(encoder.encodeCharForTests((char) 0xcb),
                new byte[] {'\\', 'u', '0', '0', 'C', 'B'});
        compare(encoder.encodeCharForTests((char) 0xd6),
                new byte[] {'\\', 'u', '0', '0', 'D', '6'});
        compare(encoder.encodeCharForTests((char) 0xec),
                new byte[] {'\\', 'u', '0', '0', 'E', 'C'});
        compare(encoder.encodeCharForTests((char) 0xf0),
                new byte[] {'\\', 'u', '0', '0', 'F', '0'});
        compare(encoder.encodeCharForTests((char) 0xfd),
                new byte[] {'\\', 'u', '0', '0', 'F', 'D'});
        compare(encoder.encodeCharForTests((char) 0xfe),
                new byte[] {'\\', 'u', '0', '0', 'F', 'E'});
        compare(encoder.encodeCharForTests((char) 0xff),
                new byte[] {'\\', 'u', '0', '0', 'F', 'F'});
        compare(encoder.encodeCharForTests((char) 0x0100),
                new byte[] {'\\', 'u', '0', '1', '0', '0'});
        compare(encoder.encodeCharForTests((char) 0x0101),
                new byte[] {'\\', 'u', '0', '1', '0', '1'});
        compare(encoder.encodeCharForTests((char) 0x016e),
                new byte[] {'\\', 'u', '0', '1', '6', 'E'});
        compare(encoder.encodeCharForTests((char) 0x0777),
                new byte[] {'\\', 'u', '0', '7', '7', '7'});
        compare(encoder.encodeCharForTests((char) 0x0778),
                new byte[] {'\\', 'u', '0', '7', '7', '8'});
        compare(encoder.encodeCharForTests((char) 0x0877),
                new byte[] {'\\', 'u', '0', '8', '7', '7'});
        compare(encoder.encodeCharForTests((char) 0x0a46),
                new byte[] {'\\', 'u', '0', 'A', '4', '6'});
        compare(encoder.encodeCharForTests((char) 0x774d),
                new byte[] {'\\', 'u', '7', '7', '4', 'D'});
        compare(encoder.encodeCharForTests((char) 0x8000),
                new byte[] {'\\', 'u', '8', '0', '0', '0'});
        compare(encoder.encodeCharForTests((char) 0x8800),
                new byte[] {'\\', 'u', '8', '8', '0', '0'});
        compare(encoder.encodeCharForTests((char) 0xabcd),
                new byte[] {'\\', 'u', 'A', 'B', 'C', 'D'});
        compare(encoder.encodeCharForTests((char) 0xfffe),
                new byte[] {'\\', 'u', 'F', 'F', 'F', 'E'});
        compare(encoder.encodeCharForTests((char) 0xffff),
                new byte[] {'\\', 'u', 'F', 'F', 'F', 'F'});
    }
    
    public void testEncodingOfString() throws CharacterCodingException {
        final PropCharsetEncoder encoder
                = new PropCharsetEncoder(new PropCharset());
        compare(encoder.encodeStringForTests(""),
                new byte[] {});
        compare(encoder.encodeStringForTests("a"),
                new byte[] {'a'});
        compare(encoder.encodeStringForTests("\\"),     //pending character
                new byte[] {'\\'});
        compare(encoder.encodeStringForTests("\\\\"),
                new byte[] {'\\', '\\'});
        compare(encoder.encodeStringForTests("\\t"),
                new byte[] {'\\', 't'});
        compare(encoder.encodeStringForTests("key\t=value"),
                new byte[] {'k', 'e', 'y', '\t', '=', 'v', 'a', 'l', 'u', 'e'});
        compare(encoder.encodeStringForTests("key=\tvalue"),
                new byte[] {'k', 'e', 'y', '=', '\t', 'v', 'a', 'l', 'u', 'e'});
    }
    
    public void testDecodingOfSingleChar() throws CharacterCodingException {
        final PropCharsetDecoder decoder
                = new PropCharsetDecoder(new PropCharset());
        compare(decoder.decodeBytesForTests(new byte[] {'\\', '\\'}),   /* backslash */
                new char[] {'\\', '\\'});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 't'}),    /* tab */
                new char[] {'\\', 't'});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'f'}),    /* FF */
                new char[] {'\\', 'f'});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'n'}),    /* NL */
                new char[] {'\\', 'n'});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'r'}),    /* CR */
                new char[] {'\\', 'r'});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '0', '0'}),
                new char[] {(char) 0x00});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '0', '1'}),
                new char[] {(char) 0x01});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '0', '2'}),
                new char[] {(char) 0x02});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '0', '3'}),
                new char[] {(char) 0x03});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '0', '4'}),
                new char[] {(char) 0x04});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '0', '5'}),
                new char[] {(char) 0x05});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '0', '6'}),
                new char[] {(char) 0x06});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '0', '7'}),
                new char[] {(char) 0x07});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '0', '8'}),
                new char[] {(char) 0x08});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '0', '9'}),
                new char[] {'\\', 'u', '0', '0', '0', '9'}); //see issue #111530
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '0', 'a'}),
                new char[] {'\n'});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '0', 'b'}),
                new char[] {(char) 0x0b});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '0', 'c'}),
                new char[] {'\\', 'u', '0', '0', '0', 'c'}); //see issue #111530
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '0', 'd'}),
                new char[] {'\r'});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '0', 'e'}),
                new char[] {(char) 0x0e});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '0', 'f'}),
                new char[] {(char) 0x0f});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '1', '9'}),
                new char[] {(char) 0x19});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '2', '0'}),
                new char[] {'\\', 'u', '0', '0', '2', '0'}); //see issue #111530
        compare(decoder.decodeBytesForTests(new byte[] {' '}),
                new char[] {(char) 0x20});
        compare(decoder.decodeBytesForTests(new byte[] {'!'}),
                new char[] {(char) 0x21});
        compare(decoder.decodeBytesForTests(new byte[] {'2'}),
                new char[] {(char) 0x32});
        compare(decoder.decodeBytesForTests(new byte[] {'C'}),
                new char[] {(char) 0x43});
        compare(decoder.decodeBytesForTests(new byte[] {'T'}),
                new char[] {(char) 0x54});
        compare(decoder.decodeBytesForTests(new byte[] {'e'}),
                new char[] {(char) 0x65});
        compare(decoder.decodeBytesForTests(new byte[] {'x'}),
                new char[] {(char) 0x78});
        compare(decoder.decodeBytesForTests(new byte[] {'}'}),
                new char[] {(char) 0x7d});
        compare(decoder.decodeBytesForTests(new byte[] {'~'}),
                new char[] {(char) 0x7e});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '7', 'f'}),
                new char[] {(char) 0x7f});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '8', '1'}),
                new char[] {(char) 0x81});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '8', '9'}),
                new char[] {(char) 0x89});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '8', 'f'}),
                new char[] {(char) 0x8f});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '9', '0'}),
                new char[] {(char) 0x90});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', 'a', '0'}),
                new char[] {(char) 0xa0});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', 'a', 'd'}),
                new char[] {(char) 0xad});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', 'c', 'b'}),
                new char[] {(char) 0xcb});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', 'd', '6'}),
                new char[] {(char) 0xd6});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', 'e', 'c'}),
                new char[] {(char) 0xec});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', 'f', '0'}),
                new char[] {(char) 0xf0});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', 'f', 'd'}),
                new char[] {(char) 0xfd});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', 'f', 'e'}),
                new char[] {(char) 0xfe});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', 'f', 'f'}),
                new char[] {(char) 0xff});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '1', '0', '0'}),
                new char[] {(char) 0x0100});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '1', '0', '1'}),
                new char[] {(char) 0x0101});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '1', '6', 'e'}),
                new char[] {(char) 0x016e});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '7', '7', '7'}),
                new char[] {(char) 0x0777});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '7', '7', '8'}),
                new char[] {(char) 0x0778});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '8', '7', '7'}),
                new char[] {(char) 0x0877});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', 'a', '4', '6'}),
                new char[] {(char) 0x0a46});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '7', '7', '4', 'd'}),
                new char[] {(char) 0x774d});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '8', '0', '0', '0'}),
                new char[] {(char) 0x8000});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '8', '8', '0', '0'}),
                new char[] {(char) 0x8800});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', 'a', 'b', 'c', 'd'}),
                new char[] {(char) 0xabcd});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', 'f', 'f', 'f', 'e'}),
                new char[] {(char) 0xfffe});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', 'f', 'f', 'f', 'f'}),
                new char[] {(char) 0xffff});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', 'A', 'B', 'C', 'D'}),
                new char[] {(char) 0xabcd});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', 'F', 'F', 'F', 'E'}),
                new char[] {(char) 0xfffe});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', 'F', 'F', 'F', 'F'}),
                new char[] {(char) 0xffff});
    }
    
    public void testDecodingPendingBytes() throws CharacterCodingException {
        final PropCharsetDecoder decoder
                = new PropCharsetDecoder(new PropCharset());
        compare(decoder.decodeBytesForTests(new byte[] {'\\'}),
                new char[] {'\\'});
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u'}),
                "\\u".toCharArray());
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '1'}),
                "\\u1".toCharArray());
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '1', '2'}),
                "\\u12".toCharArray());
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '1', '2', '3'}),
                "\\u123".toCharArray());
    }
    
    public void testDecodingErrorRecovery() throws CharacterCodingException {
        final PropCharsetDecoder decoder
                = new PropCharsetDecoder(new PropCharset());
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', 'x', 'y', 'z'}),
                "\\uxyz".toCharArray());
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '1', 'x', 'y', 'z'}),
                "\\u1xyz".toCharArray());
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '1', '2', 'x', 'y', 'z'}),
                "\\u12xyz".toCharArray());
        compare(decoder.decodeBytesForTests(new byte[] {'\\', 'u', '1', '2', '3', 'x', 'y', 'z'}),
                "\\u123xyz".toCharArray());
    }
    
    private void compare(char[] actual, char[] expected) {
        if (!Arrays.equals(expected, actual)) {
            fail("char arrays do not match"
                 + " - expected: " + showCharArray(expected)
                 + ", actual: " + showCharArray(actual));
        }
    }
    
    private void compare(byte[] actual, byte[] expected) {
        if (!Arrays.equals(expected, actual)) {
            fail("byte arrays do not match"
                 + " - expected: " + showByteArray(expected)
                 + ", actual: " + showByteArray(actual));
        }
    }
    
    private static String showCharArray(final char[] arr) {
        StringBuilder buf = new StringBuilder(3 * arr.length + 5);
        buf.append('{');
        for (int i = 0; i < arr.length - 1; i++) {
            buf.append(getVisualRepresentation(arr[i]));
            buf.append(',');
        }
        if (arr.length != 0) {
            buf.append(getVisualRepresentation(arr[arr.length - 1]));
        }
        buf.append('}');
        return buf.toString();
    }
    
    private static String showByteArray(final byte[] arr) {
        StringBuilder buf = new StringBuilder(3 * arr.length + 5);
        buf.append('{');
        for (int i = 0; i < arr.length - 1; i++) {
            buf.append(getVisualRepresentation(arr[i]));
            buf.append(',');
        }
        if (arr.length != 0) {
            buf.append(getVisualRepresentation(arr[arr.length - 1]));
        }
        buf.append('}');
        return buf.toString();
    }
    
    private static char[] getVisualRepresentation(char c) {
        if (c < '\u0020') {
            char[] result = new char[4];
            int off = 0;
            result[off++] = '<';
            result[off++] = getCharForHexadec((c >>> 4) & 0x0f);
            result[off++] = getCharForHexadec(    c     & 0x0f);
            result[off++] = '>';
            return result;
        } else {
            return new char[] {c};
        }
    }
    
    private static char[] getVisualRepresentation(byte b) {
        if (b < 0x20) {
            char[] result = new char[4];
            int off = 0;
            result[off++] = '<';
            result[off++] = getCharForHexadec((b >>> 4) & 0x0f);
            result[off++] = getCharForHexadec(    b     & 0x0f);
            result[off++] = '>';
            return result;
        } else {
            return new char[] {(char) b};
        }
    }
    
    private static char getCharForHexadec(int b) {
        assert b < 0x10;
        return (b < 10) ? (char) ('0' + (b & 0x0f))
                        : (char) ('a' + ((b & 0x0f) - 10));
    }
    
}
