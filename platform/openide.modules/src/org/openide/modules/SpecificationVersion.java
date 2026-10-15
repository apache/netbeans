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
package org.openide.modules;


// THIS CLASS OUGHT NOT USE NbBundle NOR org.openide CLASSES
// OUTSIDE OF openide-util.jar! UI AND FILESYSTEM/DATASYSTEM
// INTERACTIONS SHOULD GO ELSEWHERE.
import java.util.*;


/** Utility class representing a specification version.
 * @author Jesse Glick
 * @since 1.24
 */
public final class SpecificationVersion implements Comparable<SpecificationVersion> {
    // Might be a bit wasteful of memory, but many SV's are created during
    // startup, so best to not have to reparse them each time!
    // In fact sharing the int arrays might save a bit of memory overall,
    // since it is unusual for a module to be deleted.
    private static final Map<String,int[]> parseCache = new HashMap<>(200);
    private final int[] digits;

    /** Parse from string. Must be Dewey-decimal. */
    public SpecificationVersion(String version) throws NumberFormatException {
        synchronized (parseCache) {
            int[] d = parseCache.get(version);

            if (d == null) {
                d = parse(version);
                parseCache.put(version.intern(), d);
            }

            digits = d;
        }
    }

    private static int[] parse(String version) throws NumberFormatException {
        StringTokenizer tok = new StringTokenizer(version, ".", true); // NOI18N
        
        int len = tok.countTokens();
        if ((len % 2) == 0) {
            throw new NumberFormatException("Even number of pieces in a spec version: `" + version + "'"); // NOI18N
        }
        int[] digits = new int[len / 2 + 1];
        int i = 0;

        boolean expectingNumber = true;

        while (tok.hasMoreTokens()) {
            if (expectingNumber) {
                expectingNumber = false;

                int piece = Integer.parseInt(tok.nextToken());

                if (piece < 0) {
                    throw new NumberFormatException("Spec version component <0: " + piece); // NOI18N
                }

                digits[i++] = piece;
            } else {
                if (!".".equals(tok.nextToken())) { // NOI18N
                    throw new NumberFormatException("Expected dot in spec version: `" + version + "'"); // NOI18N
                }

                expectingNumber = true;
            }
        }
        return digits;
    }

    /** Perform a Dewey-decimal comparison. */
    @Override
    public int compareTo(SpecificationVersion o) {
        int[] od = o.digits;
        int len1 = digits.length;
        int len2 = od.length;
        int max = Math.max(len1, len2);

        for (int i = 0; i < max; i++) {
            int d1 = ((i < len1) ? digits[i] : 0);
            int d2 = ((i < len2) ? od[i] : 0);

            if (d1 != d2) {
                return d1 - d2;
            }
        }

        return 0;
    }

    /** Overridden to compare contents. */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SpecificationVersion)) {
            return false;
        }

        return Arrays.equals(digits, ((SpecificationVersion) o).digits);
    }

    /** Overridden to hash by contents. */
    @Override
    public int hashCode() {
        int hash = 925295;
        int len = digits.length;

        for (int i = 0; i < len; i++) {
            hash ^= (digits[i] << i);
        }

        return hash;
    }

    /** String representation (Dewey-decimal). */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder((digits.length * 3) + 1);

        for (int i = 0; i < digits.length; i++) {
            if (i > 0) {
                buf.append('.'); // NOI18N
            }

            buf.append(digits[i]);
        }

        return buf.toString();
    }
}
