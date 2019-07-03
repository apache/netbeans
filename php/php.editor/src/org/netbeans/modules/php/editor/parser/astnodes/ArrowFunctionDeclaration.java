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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Represents an arrow function declaration.
 *
 * e.g.
 * <pre>
 * fn(parameters) => expr; // basic form
 *
 * fn(array $array) => $array; // parameter type
 * fn(int $x): int => $x; // return type
 * fn($x = 100) => $x; // default value
 * fn(&$x) => $x; // reference
 * fn&($x) => $x; // reference
 * fn($x, ...$y) => $y; // variadics
 * static fn() => isset($this); // static
 *
 * fn() => yield 100; // this works
 * </pre>
 *
 * PHP 7.4 Arrow Functions 2.0
 *
 * @see https://wiki.php.net/rfc/arrow_functions_v2
 */
public class ArrowFunctionDeclaration extends Expression {

    private final boolean isReference;
    private final boolean isStatic;
    private final List<FormalParameter> formalParameters = new ArrayList<>();
    @NullAllowed
    private final Expression returnType;
    private final Expression expression;

    public ArrowFunctionDeclaration(int start, int end, List formalParameters, Expression returnType, Expression expression, boolean isReference, boolean isStatic) {
        super(start, end);
        if (formalParameters == null) {
            throw new IllegalArgumentException();
        }
        this.isReference = isReference;
        this.isStatic = isStatic;
        this.formalParameters.addAll(formalParameters);
        this.returnType = returnType;
        this.expression = expression;
    }

    /**
     * Expression of this function declaration.
     *
     * @return Expression of this function declaration
     */
    public Expression getExpression() {
        return this.expression;
    }

    /**
     * List of the formal parameters of this function declaration.
     *
     * @return the parameters of this declaration
     */
    public List<FormalParameter> getFormalParameters() {
        return Collections.unmodifiableList(this.formalParameters);
    }

    /**
     * Return type of this function declaration, can be {@code null}.
     *
     * @return return type of this function declaration, can be {@code null}
     */
    @CheckForNull
    public Expression getReturnType() {
        return returnType;
    }

    /**
     * Check whether this function's return variable will be referenced.
     *
     * @return {@code true} if this function's return variable will be
     * referenced, otherwise {@code false}
     */
    public boolean isReference() {
        return isReference;
    }

    /**
     * Check whether fn() is static. e.g. {@code static fn() => isset($this);}
     *
     * @return {@code true} if it is static, otherwise {@code false}
     */
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sbParams = new StringBuilder();
        getFormalParameters().forEach((param) -> sbParams.append(param).append(",")); // NOI18N
        return (isStatic() ? "static " : " ") // NOI18N
                + "fn" + (isReference() ? " & " : "") + "(" + sbParams.toString() + ")" // NOI18N
                + (getReturnType() != null ? ": " + getReturnType() : "") // NOI18N
                + " => " // NOI18N
                + getExpression();
    }
}
