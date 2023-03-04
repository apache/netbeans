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
package org.openide.util;

/**
 * A type safe pair of two object.
 * @author Tomas Zezula
 * @since 8.32
 */
public final class Pair<First,Second> {

    private final First first;
    private final Second second;


    private Pair(final First first, final Second second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Returns the first element of the {@link Pair}.
     * @return the first element.
     */
    public First first() {
        return first;
    }

    /**
     * Returns the second element of the {@link Pair}.
     * @return the second element.
     */
    public Second second() {
        return second;
    }

    @Override
    public String toString () {
        return String.format("Pair[%s,%s]", first,second);  //NOI18N
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Pair)) {
            return false;
        }
        final Pair<?,?> otherPair = (Pair<?,?>) other;
        return (first == null ? otherPair.first == null : first.equals(otherPair.first)) &&
            (second == null ? otherPair.second == null : second.equals(otherPair.second));
    }

    @Override
    public int hashCode() {
        int res = 17;
        res = res * 31 + (first == null ? 0 : first.hashCode());
        res = res * 31 + (second == null ? 0 : second.hashCode());
        return res;
    }


    /**
     * Creates a new Pair.
     * @param <First>   the type of the first element
     * @param <Second>  the type of the second element
     * @param first     the first element
     * @param second    the second element
     * @return  the new {@link Pair} of the first and second elements.
     */
    public static <First,Second> Pair<First,Second> of (final First first, final Second second) {
        return new Pair<First, Second>(first, second);
    }
}
