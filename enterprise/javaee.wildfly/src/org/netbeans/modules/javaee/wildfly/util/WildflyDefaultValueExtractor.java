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
 * A default value extractor for variables from the wildfly configuration (standalone.xml).
 */
public class WildflyDefaultValueExtractor {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("^\\$\\{\\S+:(\\S+)\\}$");
    private static final int DEFAULT_VALUE_GROUP = 1;

    /**
     * Retrieve default value from the given (possibly variable) expression.
     * <p>
     * If the expression matches the variable layout "${key:default}", an optional containing the default is
     * returned. In all other cases, an empty optional is returned.
     *
     * @param expression The expression to extract the default value from, might be a variable expression
     * containing a default or not.
     * @return An {@link Optional} containing the default value or an empty optional.
     */
    public static Optional<String> extract(String expression) {
        if (expression != null) {
            Matcher matcher = VARIABLE_PATTERN.matcher(expression);
            if (matcher.matches()) {
                return Optional.of(matcher.group(DEFAULT_VALUE_GROUP));
            }
        }
        return Optional.empty();
    }
}
