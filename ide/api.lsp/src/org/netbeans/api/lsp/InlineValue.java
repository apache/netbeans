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
package org.netbeans.api.lsp;

/**
 * An expression whose value may be shown inline while debugging.
 *
 * @since 1.35
 */
public final class InlineValue {
    private final Range range;
    private final String expression;

    private InlineValue(Range range, String expression) {
        this.range = range;
        this.expression = expression;
    }

    /**
     * {@return Range to which the inline value applies}
     */
    public Range getRange() {
        return range;
    }

    /**
     * {@return The expression of that should be evaluated for the inline value.}
     */
    public String getExpression() {
        return expression;
    }

    /**
     * {@return a new instance of {@code InlineValue}, based on the provided information.}
     *
     * @param range range to which the inline value should apply
     * @param expression expression that should be evaluted
     */
    public static InlineValue createInlineVariable(Range range, String expression) {
        return new InlineValue(range, expression);
    }
}
