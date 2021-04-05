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
package org.netbeans.modules.payara.tooling.utils;

/**
 * Enumeration helper methods:<ul>
 * <li>Ordinal value based comparison.</li>
 * </ul>
 * @author Tomas Kraus
 */
public final class EnumUtils {
    
    /**
     * Ordinal value based comparison: <i>equals</i> {@code v1 == v2}.
     * <p/>
     * @param v1 First {@link Enum} instance to be compared.
     * @param v2 Second {@link Enum} instance to be compared.
     * @return Value of {@code true} when ordinal values of {@code v1}
     *         and {@code v2} are equal or both values are {@code null}.
     *         Value of {@code false} otherwise.
     */
    public static final boolean eq(
            final Enum<? extends Enum> v1, final Enum<? extends Enum> v2) {
        return v1 != null
                ? (v2 != null ? v1.ordinal() == v2.ordinal() : false)
                : v2 == null;
    }

    /**
     * Ordinal value based comparison: <i>not equals</i> {@code v1 != v2}.
     * <p/>
     * @param v1 First {@link Enum} instance to be compared.
     * @param v2 Second {@link Enum} instance to be compared.
     * @return Value of {@code true} when ordinal values of {@code v1}
     *         and {@code v2} are not equal or one of the values
     *         is {@code null} and second one is not {@code null}. Value
     *         of {@code false} otherwise.
     */
    public static final boolean ne(
            final Enum<? extends Enum> v1, final Enum<? extends Enum> v2) {
        return v1 != null
                ? (v2 != null ? v1.ordinal() != v2.ordinal() : true)
                : v2 != null;
    }

    /**
     * Ordinal value based comparison: <i>less than</i> {@code v1 < v2}.
     * Value of {@code null} is considered less than any ordinal value.
     * <p/>
     * @param v1 First {@link Enum} instance to be compared.
     * @param v2 Second {@link Enum} instance to be compared.
     * @return Value of {@code true} when ordinal values of {@code v1}
     *         is less than {@code v2} or {@code v1} is {@code null} 
     *         and {@code v2} is not {@code null}. Value of {@code false}
     *         otherwise. 
     */
    public static final boolean lt(
            final Enum<? extends Enum> v1, final Enum<? extends Enum> v2) {
        return v1 != null
                ? (v2 != null ? v1.ordinal() < v2.ordinal() : false)
                : v2 != null;
    }

    /**
     * Ordinal value based comparison: <i>less than or equal</i> {@code v1 <= v2}.
     * Value of {@code null} is considered less than any ordinal value.
     * <p/>
     * @param v1 First {@link Enum} instance to be compared.
     * @param v2 Second {@link Enum} instance to be compared.
     * @return Value of {@code true} when ordinal values of {@code v1}
     *         is less than or equal to {@code v2} or {@code v1} is {@code null} 
     *         and {@code v2} is not {@code null}. Value of {@code false}
     *         otherwise. 
     */
    public static final boolean le(
            final Enum<? extends Enum> v1, final Enum<? extends Enum> v2) {
        return v1 != null
                ? (v2 != null ? v1.ordinal() <= v2.ordinal() : false)
                : true;
    }

    /**
     * Ordinal value based comparison: <i>greater than</i> {@code v1 > v2}.
     * Value of {@code null} is considered less than any ordinal value.
     * <p/>
     * @param v1 First {@link Enum} instance to be compared.
     * @param v2 Second {@link Enum} instance to be compared.
     * @return Value of {@code true} when ordinal values of {@code v1}
     *         is greater than {@code v2} or {@code v1} is {@code null} 
     *         and {@code v2} is not {@code null}. Value of {@code false}
     *         otherwise. 
     */
    public static final boolean gt(
            final Enum<? extends Enum> v1, final Enum<? extends Enum> v2) {
        return v1 != null
                ? (v2 != null ? v1.ordinal() > v2.ordinal() : true)
                : false;
    }

    /**
     * Ordinal value based comparison: <i>greater than or equal</i> {@code v1 >= v2}.
     * Value of {@code null} is considered less than any ordinal value.
     * <p/>
     * @param v1 First {@link Enum} instance to be compared.
     * @param v2 Second {@link Enum} instance to be compared.
     * @return Value of {@code true} when ordinal values of {@code v1}
     *         is greater than or equal to {@code v2} or {@code v1} is not {@code null} 
     *         and {@code v2} is {@code null}. Value of {@code false}
     *         otherwise. 
     */
    public static final boolean ge(
            final Enum<? extends Enum> v1, final Enum<? extends Enum> v2) {
        return v1 != null
                ? (v2 != null ? v1.ordinal() >= v2.ordinal() : true)
                : v2 == null;
    }

}
