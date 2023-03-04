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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Represents a lambda function declaration.
 *
 * <pre>e.g.
 * function & (parameters) use (lexical vars) { body }
 * function & (parameters) use (lexical vars): return_type { body }
 * #[A(1)] function () {}; // [NETBEANS-4443] PHP 8.0
 * </pre>
 *
 * @see http://wiki.php.net/rfc/closures
 */
public class LambdaFunctionDeclaration extends Expression implements Attributed {

    private final boolean isReference;
    private final boolean isStatic;
    private final List<FormalParameter> formalParameters = new ArrayList<>();
    @NullAllowed
    private final Expression returnType;
    private final List<Expression> lexicalVariables = new ArrayList<>();
    private final Block body;
    private final List<Attribute> attributes = new ArrayList<>();

    public LambdaFunctionDeclaration(int start, int end, List formalParameters, Expression returnType, List lexicalVars, Block body, boolean isReference, boolean isStatic) {
        this(start, end, formalParameters, returnType, lexicalVars, body, isReference, isStatic, Collections.emptyList());
    }

    private LambdaFunctionDeclaration(int start, int end, List<FormalParameter> formalParameters, Expression returnType, List<Expression> lexicalVars, Block body, boolean isReference, boolean isStatic, List<Attribute> attributes) {
        super(start, end);

        this.attributes.addAll(attributes);
        if (formalParameters == null) {
            throw new IllegalArgumentException();
        }
        this.isReference = isReference;
        this.isStatic = isStatic;
        this.formalParameters.addAll(formalParameters);
        this.returnType = returnType;
        if (lexicalVars != null) {
            this.lexicalVariables.addAll(lexicalVars);
        }
        this.body = body;
    }

    public static LambdaFunctionDeclaration create(LambdaFunctionDeclaration declaration, List<Attribute> attributes) {
        assert attributes != null;
        int start = attributes.isEmpty() ? declaration.getStartOffset() : attributes.get(0).getStartOffset();
        return new LambdaFunctionDeclaration(
                start,
                declaration.getEndOffset(),
                declaration.getFormalParameters(),
                declaration.getReturnType(),
                declaration.getLexicalVariables(),
                declaration.getBody(),
                declaration.isReference(),
                declaration.isStatic(),
                attributes
        );
    }

    /**
     * Body of this function declaration
     *
     * @return Body of this function declaration
     */
    public Block getBody() {
        return body;
    }

    /**
     * List of the formal parameters of this function declaration
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
     * List of the lexical variables of this lambda function declaration
     *
     * @return the lexical variables of this declaration
     */
    public List<Expression> getLexicalVariables() {
        return Collections.unmodifiableList(this.lexicalVariables);
    }

    /**
     * True if this function's return variable will be referenced
     *
     * @return True if this function's return variable will be referenced
     */
    public boolean isReference() {
        return isReference;
    }

    /**
     * Check whether fn() is static. e.g.{@code $fnc = static function() {};}
     *
     * @return {@code true} if it is static, otherwise {@code false}
     */
    public boolean isStatic() {
        return isStatic;
    }

    public synchronized void addAttributes(List<Attribute> attributes) {
        this.attributes.addAll(attributes);
    }

    /**
     * Get the attributes of this.
     *
     * e.g. {@code $fn = #[A(1)] function () {};}
     *
     * @return the attributes
     */
    @Override
    public synchronized List<Attribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    @Override
    public boolean isAttributed() {
        return !attributes.isEmpty();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sbAttributes = new StringBuilder();
        getAttributes().forEach(attribute -> sbAttributes.append(attribute).append(" ")); // NOI18N
        StringBuilder sbParams = new StringBuilder();
        for (FormalParameter formalParameter : getFormalParameters()) {
            sbParams.append(formalParameter).append(","); //NOI18N
        }
        StringBuilder sbLex = new StringBuilder();
        for (Expression expression : getLexicalVariables()) {
            sbLex.append(expression).append(","); //NOI18N
        }
        return sbAttributes.toString()
                + (isStatic() ? "static " : "") // NOI18N
                + "function" + (isReference() ? " & " : "") + "(" + sbParams.toString() + ")" // NOI18N
                + (sbLex.length() > 0 ? " use (" + sbLex.toString() + ")" : "") // NOI18N
                + (getReturnType() != null ? ": " + getReturnType() : "") // NOI18N
                + getBody(); //NOI18N
    }

}
