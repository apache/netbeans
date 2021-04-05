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
package org.netbeans.modules.php.editor.parser.astnodes;

/**
 * Represents a function formal parameter
 * <pre>e.g.<pre> $a,
 * MyClass $a,
 * $a = 3,
 * int $a = 3
 */
public class FormalParameter extends ASTNode {

    private Expression parameterType;
    private Expression parameterName;
    private Expression defaultValue;

    public FormalParameter(int start, int end, Expression type, final Expression parameterName, Expression defaultValue) {
        super(start, end);

        this.parameterName = parameterName;
        this.parameterType = type;
        this.defaultValue = defaultValue;
    }

    public FormalParameter(int start, int end, Expression type, final Reference parameterName, Expression defaultValue) {
        this(start, end, type, (Expression) parameterName, defaultValue);
    }

    public FormalParameter(int start, int end, Expression type, final Expression parameterName) {
        this(start, end, type, (Expression) parameterName, null);
    }

    public FormalParameter(int start, int end, Expression type, final Reference parameterName) {
        this(start, end, type, (Expression) parameterName, null);
    }

    public Expression getDefaultValue() {
        return defaultValue;
    }

    public boolean isMandatory() {
        return getDefaultValue() == null && !isVariadic();
    }

    public boolean isOptional() {
        return !isMandatory();
    }

    public boolean isVariadic() {
        if (isReference()) {
            return ((Reference)getParameterName()).getExpression() instanceof Variadic;
        }
        return getParameterName() instanceof Variadic;
    }

    public boolean isReference() {
        return getParameterName() instanceof Reference;
    }

    public boolean isNullableType() {
        return getParameterType() instanceof NullableType;
    }

    public Expression getParameterName() {
        return parameterName;
    }

    public Expression getParameterType() {
        return parameterType;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getParameterType() + " " + getParameterName() + (isMandatory() ? "" : " = " + getDefaultValue()); //NOI18N
    }

}
