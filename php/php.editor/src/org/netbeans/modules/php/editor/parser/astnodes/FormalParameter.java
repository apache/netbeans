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
 * public protected(set) int $x = 0 {get => $this->x; set{}}, // [GH-8035] PHP8.4 Property hooks with Constructor Property Promotion
 * </pre>
 */
public class FormalParameter extends ASTNode implements Attributed {

    private int modifier;
    private Expression parameterType;
    private Expression parameterName;
    private Expression defaultValue;
    private Block propertyHooks;
    private final List<Attribute> attributes = new ArrayList<>();

    // Use Builder instead
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
        this(start, end, type, parameterName, null);
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

    private FormalParameter(Builder builder) {
        super(builder.start, builder.end);
        modifier = builder.modifier;
        parameterType = builder.parameterType;
        parameterName = builder.parameterName;
        defaultValue = builder.defaultValue;
        propertyHooks = builder.propertyHooks;
        attributes.addAll(builder.attributes);
    }

    public static FormalParameter create(FormalParameter parameter, List<Attribute> attributes) {
        assert attributes != null;
        int start = attributes.isEmpty() ? parameter.getStartOffset() : attributes.get(0).getStartOffset();
        return new Builder(start, parameter.getEndOffset())
                .modifier(parameter.getModifier())
                .parameterType(parameter.getParameterType())
                .parameterName(parameter.getParameterName())
                .defaultValue(parameter.getDefaultValue())
                .propertyHooks(parameter.getPropertyHooks())
                .attributes(attributes)
                .build();
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
        return List.copyOf(attributes);
    }

    @Override
    public boolean isAttributed() {
        return !attributes.isEmpty();
    }

    public Block getPropertyHooks() {
        return propertyHooks;
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

    //~ Inner class
    public static class Builder {

        private final int start;
        private final int end;
        private int modifier = 0;
        private Expression parameterType = null;
        private Expression parameterName = null;
        private Expression defaultValue = null;
        private Block propertyHooks = null;
        private List<Attribute> attributes = List.of();

        public Builder(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public Builder modifier(Integer modifier) {
            this.modifier = modifier == null ? 0 : modifier;
            return this;
        }

        public Builder parameterType(Expression parameterType) {
            this.parameterType = parameterType;
            return this;
        }

        public Builder parameterName(Expression parameterName) {
            this.parameterName = parameterName;
            return this;
        }

        public Builder defaultValue(Expression defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder propertyHooks(Block propertyHooks) {
            this.propertyHooks = propertyHooks;
            return this;
        }

        public Builder attributes(List<Attribute> attributes) {
            this.attributes = List.copyOf(attributes);
            return this;
        }

        public FormalParameter build() {
            return new FormalParameter(this);
        }
    }
}
