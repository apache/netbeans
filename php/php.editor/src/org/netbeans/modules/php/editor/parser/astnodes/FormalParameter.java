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

/**
 * Represents a function formal parameter.
 *
 * <pre>e.g.
 * $a,
 * MyClass $a,
 * $a = 3,
 * int $a = 3,
 * #[A(1)] int $a, // [NETBEANS-4443] PHP8.0 Attribute Syntax
 * public int $x = 0, // [NETBEANS-4443] PHP8.0 Constructor Property Promotion
 * </pre>
 */
public class FormalParameter extends ASTNode implements Attributed {

    private int modifier;
    private Expression parameterType;
    private Expression parameterName;
    private Expression defaultValue;
    private final List<Attribute> attributes = new ArrayList<>();

    public FormalParameter(int start, int end, Integer modifier, Expression type, final Expression parameterName, Expression defaultValue) {
        this(start, end, modifier == null ? 0 : modifier, type, parameterName, defaultValue, Collections.emptyList());
    }

    public FormalParameter(int start, int end, Expression type, final Expression parameterName, Expression defaultValue) {
        this(start, end, 0, type, parameterName, defaultValue, Collections.emptyList());
    }

    public FormalParameter(int start, int end, Expression type, final Reference parameterName, Expression defaultValue) {
        this(start, end, type, (Expression) parameterName, defaultValue);
    }

    public FormalParameter(int start, int end, Integer modifier, Expression type, final Expression parameterName) {
        this(start, end, modifier, type, parameterName, null);
    }

    public FormalParameter(int start, int end, Expression type, final Expression parameterName) {
        this(start, end, type, (Expression) parameterName, null);
    }

    public FormalParameter(int start, int end, Expression type, final Reference parameterName) {
        this(start, end, type, (Expression) parameterName, null);
    }

    private FormalParameter(int start, int end, int modifier, Expression parameterType, Expression parameterName, Expression defaultValue, List<Attribute> attributes) {
        super(start, end);
        this.attributes.addAll(attributes);
        this.modifier = modifier;
        this.parameterType = parameterType;
        this.parameterName = parameterName;
        this.defaultValue = defaultValue;
    }

    public static FormalParameter create(FormalParameter parameter, List<Attribute> attributes) {
        assert attributes != null;
        int start = attributes.isEmpty() ? parameter.getStartOffset() : attributes.get(0).getStartOffset();
        return new FormalParameter(
                start,
                parameter.getEndOffset(),
                parameter.getModifier(),
                parameter.getParameterType(),
                parameter.getParameterName(),
                parameter.getDefaultValue(),
                attributes
        );

    }

    public int getModifier() {
        return modifier;
    }

    public String getModifierString() {
        return BodyDeclaration.Modifier.toString(modifier);
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
            return ((Reference) getParameterName()).getExpression() instanceof Variadic;
        }
        return getParameterName() instanceof Variadic;
    }

    public boolean isReference() {
        return getParameterName() instanceof Reference;
    }

    public boolean isNullableType() {
        return getParameterType() instanceof NullableType;
    }

    public boolean isUnionType() {
        return getParameterType() instanceof UnionType;
    }

    public boolean isIntersectionType() {
        return getParameterType() instanceof IntersectionType;
    }

    public Expression getParameterName() {
        return parameterName;
    }

    public Expression getParameterType() {
        return parameterType;
    }

    @Override
    public List<Attribute> getAttributes() {
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
        String modifierString = getModifierString();
        if (modifierString != null && !modifierString.isEmpty()) {
            modifierString += " "; // NOI18N
        }
        return sbAttributes.toString()
                + modifierString
                + (getParameterType() == null ? "" : getParameterType() + " ") // NOI18N
                + getParameterName()
                + (isMandatory() ? "" : " = " + getDefaultValue()); // NOI18N
    }

}
