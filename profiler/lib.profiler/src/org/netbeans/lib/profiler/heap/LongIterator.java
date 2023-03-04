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

package org.netbeans.lib.profiler.heap;

import java.util.NoSuchElementException;

/**
 *
 * @author Tomas Hurka
 */
abstract class LongIterator {

    static LongIterator EMPTY_ITERATOR = new Empty();

    static LongIterator singleton(long i) {
        return new Singleton(i);
    }

    abstract boolean hasNext();

    abstract long next();

    private static class Empty extends LongIterator {

        @Override
        boolean hasNext() {
            return false;
        }

        @Override
        long next() {
            throw new NoSuchElementException();
        }
    }

    private static class Singleton extends LongIterator {

        private final long item;
        private boolean skipped;

        private Singleton(long i) {
            item = i;
        }

        @Override
        boolean hasNext() {
            return !skipped;
        }

        @Override
        long next() {
            if (hasNext()) {
                skipped = true;
                return item;
            }
            throw new NoSuchElementException();
        }
    }
}
