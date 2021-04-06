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
package org.netbeans.modules.remotefs.versioning.api;

import java.nio.charset.StandardCharsets;

/**
 * Scrambles/descrambles user passwords with the following algorithm:
 * http://www.cvsnt.org/cvsclient/Password-scrambling.html .
 * This is a clean-room implementation of the Scrambler.java source in
 * NetBeans versions before the Apache transition.
 * Since the algorithm is not very clear, we built a forward and reverse
 * maps of all bytes [-128,127] and computed the equivalences.
 */
public final class Scrambler {

    /**
     * Forward scrambling map.
     * Each element in the array is its scrambled counterpart.
     */
    private final int FORWARD[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
        13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
        31, 114, 120, 53, 79, 96, 109, 72, 108, 70, 64, 76, 67, 116, 74, 68,
        87, 111, 52, 75, 119, 49, 34, 82, 81, 95, 65, 112, 86, 118, 110, 122,
        105, 41, 57, 83, 43, 46, 102, 40, 89, 38, 103, 45, 50, 42, 123, 91, 35,
        125, 55, 54, 66, 124, 126, 59, 47, 92, 71, 115, 78, 88, 107, 106, 56,
        36, 121, 117, 104, 101, 100, 69, 73, 99, 63, 94, 93, 39, 37, 61, 48,
        58, 113, 32, 90, 44, 98, 60, 51, 33, 97, 62, 77, 84, 80, 85, -33, -31,
        -40, -69, -90, -27, -67, -34, -68, -115, -7, -108, -56, -72, -120, -8,
        -66, -57, -86, -75, -52, -118, -24, -38, -73, -1, -22, -36, -9, -43,
        -53, -30, -63, -82, -84, -28, -4, -39, -55, -125, -26, -59, -45, -111,
        -18, -95, -77, -96, -44, -49, -35, -2, -83, -54, -110, -32, -105, -116,
        -60, -51, -126, -121, -123, -113, -10, -64, -97, -12, -17, -71, -88,
        -41, -112, -117, -91, -76, -99, -109, -70, -42, -80, -29, -25, -37,
        -87, -81, -100, -50, -58, -127, -92, -106, -46, -102, -79, -122, 127,
        -74, -128, -98, -48, -94, -124, -89, -47, -107, -15, -103, -5, -19,
        -20, -85, -61, -13, -23, -3, -16, -62, -6, -65, -101, -114, -119, -11,
        -21, -93, -14, -78, -104,};

    /**
     * Backward descrambling map.
     * Each element in the array is its descrambled counterpart.
     */
    private final int BACKWARD[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
        13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
        31, 114, 120, 53, 79, 96, 109, 72, 108, 70, 64, 76, 67, 116, 74, 68,
        87, 111, 52, 75, 119, 49, 34, 82, 81, 95, 65, 112, 86, 118, 110, 122,
        105, 41, 57, 83, 43, 46, 102, 40, 89, 38, 103, 45, 50, 42, 123, 91, 35,
        125, 55, 54, 66, 124, 126, 59, 47, 92, 71, 115, 78, 88, 107, 106, 56,
        36, 121, 117, 104, 101, 100, 69, 73, 99, 63, 94, 93, 39, 37, 61, 48,
        58, 113, 32, 90, 44, 98, 60, 51, 33, 97, 62, 77, 84, 80, 85, -33, -31,
        -40, -69, -90, -27, -67, -34, -68, -115, -7, -108, -56, -72, -120, -8,
        -66, -57, -86, -75, -52, -118, -24, -38, -73, -1, -22, -36, -9, -43,
        -53, -30, -63, -82, -84, -28, -4, -39, -55, -125, -26, -59, -45, -111,
        -18, -95, -77, -96, -44, -49, -35, -2, -83, -54, -110, -32, -105, -116,
        -60, -51, -126, -121, -123, -113, -10, -64, -97, -12, -17, -71, -88,
        -41, -112, -117, -91, -76, -99, -109, -70, -42, -80, -29, -25, -37,
        -87, -81, -100, -50, -58, -127, -92, -106, -46, -102, -79, -122, 127,
        -74, -128, -98, -48, -94, -124, -89, -47, -107, -15, -103, -5, -19,
        -20, -85, -61, -13, -23, -3, -16, -62, -6, -65, -101, -114, -119, -11,
        -21, -93, -14, -78, -104,};

    private static Scrambler INSTANCE;

    /**
     * Returns an instance of a Scrambler.
     * @return a thread safe instance of a Scrambler.
     */
    public static synchronized Scrambler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Scrambler();
        }
        return INSTANCE;
    }

    /**
     * Given a scrambled password, this method descrambles it.
     *
     * @param scrambled the scrambled password.
     * @return The descrambled password.
     * @throws IllegalArgumentException if the input string contains invalid
     * characters.
     */
    public String descramble(String scrambled) {
        byte[] bytes = scrambled.getBytes(StandardCharsets.US_ASCII);
        if (bytes[0] != 'A') {
            throw new IllegalArgumentException("scrambled passwords must start with 'A'");
        }
        byte[] descrambled = new byte[bytes.length - 1];
        for (int i = 1; i < bytes.length; i++) {
            descrambled[i - 1] = (byte) BACKWARD[Byte.toUnsignedInt(bytes[i])];
        }
        return new String(descrambled, 0, descrambled.length, StandardCharsets.US_ASCII);
    }

    /**
     * Givem a descrambled password (with US-ASCII charset) this method
     * scrambles it.
     *
     * @param clear The descrambled password.
     * @return The scrambled password.
     */
    public String scramble(String clear) {
        byte[] bytes = clear.getBytes(StandardCharsets.US_ASCII);
        byte[] scrambled = new byte[bytes.length + 1];
        scrambled[0] = 'A';
        for (int i = 0; i < bytes.length; i++) {
            scrambled[i + 1] = (byte) FORWARD[Byte.toUnsignedInt(bytes[i])];
        }
        return new String(scrambled, 0, scrambled.length, StandardCharsets.US_ASCII);
    }

}
