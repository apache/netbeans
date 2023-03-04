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

package org.netbeans.lib.lexer;

/**
 * Cache of java.lang.Integer instances.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class IntegerCache {

    private static final int MAX_CACHED_INTEGER = 511;

    private static final Integer[] cache = new Integer[MAX_CACHED_INTEGER + 1];

    public static Integer integer(int i) {
        Integer integer;
        if (i <= MAX_CACHED_INTEGER) {
            integer = cache[i];
            if (integer == null) {
                integer = Integer.valueOf(i); // possibly delegate to global cache
                cache[i] = integer; // may lead to multiple instances but no problem with that
            }

        } else { // cannot cache
            integer = Integer.valueOf(i);
        }
        return integer;
    }
    
}
