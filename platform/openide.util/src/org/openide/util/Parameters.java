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
 * Utilities for checking the values of method parameters.
 *
 * Methods in this class generally take the name of
 * the parameter to check and its value and throw exceptions
 * with messages according to the method name or just return. For example,
 * if you have a <code>myMethod()</code> method taking a <code>myParam</code>
 * parameter whose value must be a Java identifier, you usually check that
 * by doing:
 *
 * <pre>
 * public void myMethod(String myParam) {
 *     if (!Utilities.isJavaIdentifier(myParam)) {
 *         throw new IllegalArgumentException("The myParam parameter is not a valid Java identifier");
 *     }
 * }
 * </pre>
 *
 * Using this class you can do the same in a simpler way:
 *
 * <pre>
 * public void myMethod(String myParam) {
 *     Parameters.javaIdentifier("myParam", myParam);
 * }
 * </pre>
 *
 * @author Andrei Badea
 * @since org.openide.util 7.6
 */
public class Parameters {

    private Parameters() {}

    /**
     * Asserts the parameter value is not <code>null</code>.
     * <p>Use {@code java.util.Objects.requireNonNull} in JDK 7.
     * @param  name the parameter name.
     * @param  value the parameter value.
     * @throws NullPointerException if the parameter value is <code>null</code>.
     */
    public static void notNull(CharSequence name, Object value) {
        if (value == null) {
            throw new NullPointerException("The " + name + " parameter cannot be null"); // NOI18N
        }
    }

    /**
     * Asserts the parameter value is neither <code>null</code> nor an empty
     * character sequence.
     *
     * @param  name the parameter name.
     * @param  value the parameter value.
     * @throws NullPointerException if the parameter value is <code>null</code>.
     * @throws IllegalArgumentException if the parameter value is an empty
     *         character sequence.
     */
    public static void notEmpty(CharSequence name, CharSequence value) {
        notNull(name, value);
        if (value.length() == 0) {
            throw new IllegalArgumentException("The " + name + " parameter cannot be an empty character sequence"); // NOI18N
        }
    }

    /**
     * Asserts the parameter value is not <code>null</code> and it contains
     * at least one non-whitespace character. Whitespace is defined as by
     * {@link String#trim}.
     *
     * @param  name the parameter name.
     * @param  value the parameter value.
     * @throws NullPointerException if the parameter value is <code>null</code>.
     * @throws IllegalArgumentException if the parameter value does not
     *         contain at least one non-whitespace character.
     */
    public static void notWhitespace(CharSequence name, CharSequence value) {
        notNull(name, value);
        if (value.toString().trim().length() == 0) {
            throw new IllegalArgumentException("The " + name + " parameter must contain at least one non-whitespace character"); // NOI18N
        }
    }

    /**
     * Asserts the parameter value is not <code>null</code> and it is
     * a Java identifier.
     *
     * @param  name the parameter name.
     * @param  value the parameter value.
     * @throws NullPointerException if the parameter value is <code>null</code>.
     * @throws IllegalArgumentException if the parameter value is not
     *         a Java identifier.
     */
    public static void javaIdentifier(CharSequence name, CharSequence value) {
        notNull(name, value);
        javaIdentifierOrNull(name, value);
    }

    /**
     * Asserts the parameter value is either <code>null</code> or a Java
     * identifier.
     *
     * @param  name the parameter name.
     * @param  value the parameter value.
     * @throws IllegalArgumentException if the parameter value is neither
     *         <code>null</code> nor a Java identifier.
     */
    public static void javaIdentifierOrNull(CharSequence name, CharSequence value) {
        if (value != null && !BaseUtilities.isJavaIdentifier(value.toString())) {
            throw new IllegalArgumentException("The " + name + " parameter ('" + value + "') is not a valid Java identifier"); // NOI18N
        }
    }
}
