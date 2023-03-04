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
package org.openide.util.lookup;

import java.util.Comparator;
import org.openide.util.lookup.AbstractLookup.Pair;


/** Implementation of comparator for AbstractLookup.Pair
 *
 * @author  Jaroslav Tulach
 */
final class ALPairComparator implements Comparator<Pair<?>> {
    public static final Comparator<Pair<?>> DEFAULT = new ALPairComparator();

    /** Creates a new instance of ALPairComparator */
    private ALPairComparator() {
    }

    /** Compares two items.
    */
    @Override
    public int compare(Pair<?> i1, Pair<?> i2) {
        int result = i1.getIndex() - i2.getIndex();

        if (result == 0) {
            if (i1 != i2) {
                throw new DuplicatedPairException(i1, i2);
            }

            return 0;
        }

        return result;
    }
    
    private static final class DuplicatedPairException extends IllegalStateException {
        private final Pair<?> i1, i2;

        public DuplicatedPairException(Pair<?> i1, Pair<?> i2) {
            this.i1 = i1;
            this.i2 = i2;
        }

        @Override
        public String getMessage() {
                java.io.ByteArrayOutputStream bs = new java.io.ByteArrayOutputStream();
            java.io.PrintStream ps = new java.io.PrintStream(bs);

            ps.println(
                    "Duplicate pair in tree" + // NOI18N
                    "Pair1: " + i1 + " pair2: " + i2 + " index1: " + i1.getIndex() + " index2: "
                    + i2.getIndex() // NOI18N
                    + " item1: " + i1.getInstance() + " item2: " + i2.getInstance() // NOI18N
                    + " id1: " + Integer.toHexString(System.identityHashCode(i1)) // NOI18N
                    + " id2: " + Integer.toHexString(System.identityHashCode(i2)) // NOI18N
                    );

            //                print (ps, false);
            ps.close();

            return bs.toString();
        }
    }
}
