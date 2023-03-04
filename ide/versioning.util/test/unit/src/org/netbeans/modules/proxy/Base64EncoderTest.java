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
package org.netbeans.modules.proxy;

import java.nio.charset.StandardCharsets;
import org.junit.Test;
import static org.junit.Assert.*;

public class Base64EncoderTest {

    /**
     * Test of encode method, of class Base64Encoder.
     */
    @Test
    public void testEncode_byteArr() {
        final byte[] data = ("Encode this string to Base64")
                .getBytes(StandardCharsets.UTF_8);
        final String expected = "RW5jb2RlIHRoaXMgc3RyaW5nIHRvIEJhc2U2NA==";

        assertEquals(expected, Base64Encoder.encode(data));
    }

    /**
     * Test of encode method, of class Base64Encoder.
     */
    @Test
    public void testEncode_byteArr_boolean_nowrap() {
        final byte[] data = ("Encode this string to Base64")
                .getBytes(StandardCharsets.UTF_8);
        final String expected = "RW5jb2RlIHRoaXMgc3RyaW5nIHRvIEJhc2U2NA==";

        assertEquals(expected, Base64Encoder.encode(data, false));
    }

    /**
     * Test of encode method, of class Base64Encoder.
     */
    @Test
    public void testEncode_byteArr_boolean_wrap() {
        final byte[] data = ("Encode this string to Base64, then wrap it in lines of"
                + " no more than 60 chars using platform line separator")
                .getBytes(StandardCharsets.UTF_8);
        final String expected = "RW5jb2RlIHRoaXMgc3RyaW5nIHRvIEJhc2U2NCwgdGhlbiB3cmFwIGl0IGlu" + System.getProperty("line.separator")
                + "IGxpbmVzIG9mIG5vIG1vcmUgdGhhbiA2MCBjaGFycyB1c2luZyBwbGF0Zm9y" + System.getProperty("line.separator")
                + "bSBsaW5lIHNlcGFyYXRvcg==";

        assertEquals(expected, Base64Encoder.encode(data, true));
    }

    /**
     * Test of decode method, of class Base64Encoder.
     */
    @Test
    public void testDecode() {
        final String encoded = "VGhpcyBzdHJpbmcgaGFzIGJlZW4gZGVjb2RlZCBmcm9tIEJhc2U2NA==";
        final byte[] expected = ("This string has been decoded from Base64")
                .getBytes(StandardCharsets.UTF_8);

        assertArrayEquals(expected, Base64Encoder.decode(encoded));
    }

        /**
     * Test of decode method, of class Base64Encoder.
     */
    @Test
    public void testDecode_nonPrintable() {
        final String encoded = "\tVGhpcyBzdHJpbmcg\n"
                + "    aGFzIGJlZW4gZGVj\r"
                + "b2RlZCBmcm9tIEJh"
                + "c2U2NA==   ";
        final byte[] expected = ("This string has been decoded from Base64")
                .getBytes(StandardCharsets.UTF_8);

        assertArrayEquals(expected, Base64Encoder.decode(encoded));
    }
    
}
