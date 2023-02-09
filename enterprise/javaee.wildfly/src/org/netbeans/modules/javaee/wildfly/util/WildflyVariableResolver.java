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
package org.netbeans.modules.javaee.wildfly.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A resolver for variables from the wildfly configuration (standalone.xml).
 */
public class WildflyVariableResolver {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("^\\$\\{([\\w|\\.|-]+)(?>:?)(\\S+)?\\}$");
    private static final int VARIABLE_GROUP = 1;
    private static final int FALLBACK_GROUP = 2;

    /**
     * Resolves the given (possibly variable) expression.
     * <li>If the expression does not match the variable layout ("${}"), the expression itself is
     * returned.</li>
     * <li>If the expression matches the variable layout, an attempt to resolve the variable is made:</li>
     * <ul>
     * <li>If the variable could be resolved from a system property, the value of that property is
     * assumed.</li>
     * <li>If the variable could not be resolved from a system property, but from an environment variable, the
     * value of that variable is assumed.</li>
     * <li>If the variable could not be resolved from a system property or environment variable, the fallback
     * value of the expression is assumed (the value after the ":" in the expression).</li>
     * <li>If no fallback was defined in the expression, {@code null} is assumed.</li>
     * </ul>
     *
     * @param expression The expression to resolve, might be a variable expression containing a fallback or
     * not.
     * @return An {@link Optional} containing the resolved value which might be {@code null},
     */
    public static Optional<String> resolve(String expression) {
        Matcher matcher = VARIABLE_PATTERN.matcher(expression);
        String result = expression;
        if (matcher.matches()) {
            String variableKey = matcher.group(VARIABLE_GROUP);
            String variableValue = lookupProperty(variableKey);
            if (variableValue == null) {
                result = matcher.group(FALLBACK_GROUP);
            } else {
                result = variableValue;
            }
        }
        return Optional.ofNullable(result);
    }

    private static String lookupProperty(String key) {
        if (System.getProperty(key) != null) {
            return System.getProperty(key);
        }
        if (System.getenv(key) != null) {
            return System.getenv(key);
        }
        return null;
    }
}
