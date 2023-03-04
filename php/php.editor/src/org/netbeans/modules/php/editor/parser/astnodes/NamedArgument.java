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
package org.netbeans.modules.php.editor.parser.astnodes;

/**
 * Represents a named argument.
 *
 * [NETBEANS-4443] PHP 8.0 Support
 *
 * @see https://wiki.php.net/rfc/named_params
 *
 * <pre>e.g.
 * functionName(parameterName: $value);
 * #[A(name: "attribute")]
 * class Example {}
 * </pre>
 */
public class NamedArgument extends Expression {

    private final Identifier parameterName;
    private final Expression expression;

    public NamedArgument(int start, int end, Identifier parameterName, Expression expression) {
        super(start, end);
        this.parameterName = parameterName;
        this.expression = expression;
    }

    /**
     * Get a parameter name.
     *
     * @return a parameter name
     */
    public Identifier getParameterName() {
        return parameterName;
    }

    /**
     * Get an expression of an argument.
     *
     * @return an expression of an argument
     */
    public Expression getExpression() {
        return expression;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getParameterName().getName() + ": " + getExpression(); // NOI18N
    }
}
