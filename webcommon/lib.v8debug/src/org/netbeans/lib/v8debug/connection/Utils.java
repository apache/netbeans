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
package org.netbeans.lib.v8debug.connection;

/**
 *
 * @author Martin Entlicher
 */
final class Utils {
    
    /**
     * Find an index of the pattern in an array.
     * 
     * @param pattern
     * @param array
     * @param from
     * @param to
     * @return The index, or -1.
     */
    public static int indexOf(byte[] pattern, byte[] array, int from, int to) {
        byte first = pattern[0];
        for (int i = from; i < to; i++) {
            if (array[i] == first) {
                boolean match = true;
                for (int j = i+1; j < to && (j-i) < pattern.length; j++) {
                    if (array[j] != pattern[j-i]) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    return i;
                }
            }
        }
        return -1;
    }

    static byte[] joinArrays(byte[] array1, byte[] array2, int from, int length) {
        int n = array1.length + length;
        byte[] array = new byte[n];
        if (array1.length == 0) {
            System.arraycopy(array2, from, array, 0, length);
        } else {
            System.arraycopy(array1, 0, array, 0, array1.length);
            System.arraycopy(array2, from, array, array1.length, length);
        }
        return array;
    }
    
}
