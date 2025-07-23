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
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;

/// Represents a property(field) hook declaration.
///
/// Example:
/// ```php
/// get => $this->prop;
/// final set => strtoupper($value);
/// &get {
///     $this->prop ?? $this->default();
///     return $this->prop;
/// }
/// set (string $value) {
///     echo "something" . PHP_EOL;
///     $this->prop = $value;
/// }
/// #[Attr] get {}
/// #[Attr] set {}
/// get;
/// set;
/// ```
///
/// See: [Property hooks](https://wiki.php.net/rfc/property-hooks)
/// @since 2.45.0
public class PropertyHookDeclaration extends Statement implements Attributed {

    private final int modifier;
    private final Identifier name;
    private final List<FormalParameter> formalParameters = new ArrayList<>();
    private final Block body;
    private final boolean isReference;
    private final List<Attribute> attributes = new ArrayList<>();

    private PropertyHookDeclaration(Builder builder) {
        super(builder.start, builder.end);
        this.modifier = builder.modifier;
        this.name = builder.name;
        this.formalParameters.addAll(builder.formalParameters);
        this.body = builder.body;
        this.isReference = builder.isReference;
        this.attributes.addAll(builder.attributes);
    }

    public static PropertyHookDeclaration create(PropertyHookDeclaration propertyHook, List<Attribute> attributes) {
        assert attributes != null;
        int start = attributes.isEmpty() ? propertyHook.getStartOffset() : attributes.get(0).getStartOffset();
        return new Builder(start, propertyHook.getEndOffset(), propertyHook.getName())
                .modifier(propertyHook.getModifier())
                .parameters(propertyHook.getFormalParameters())
                .body(propertyHook.getBody())
                .isReference(propertyHook.isReference())
                .attributes(attributes)
                .build();
    }

    public int getModifier() {
        return modifier;
    }

    public String getModifierString() {
        return Modifier.toString(modifier);
    }

    /**
     * Get a property hook name. e.g. get, set
     *
     * @return a property hook name
     */
    public Identifier getName() {
        return name;
    }

    public List<FormalParameter> getFormalParameters() {
        return List.copyOf(formalParameters);
    }

    /**
     * Get a body of hook
     *
     * @return a body if hook has body, {@code null} otherwise (e.g. get; set;)
     */
    @CheckForNull
    public Block getBody() {
        return body;
    }

    /**
     * Check whether a hook is reference.
     *
     * @return {@code true} if a hook is reference(e.g. &get), {@code false}
     * otherwise
     */
    public boolean isReference() {
        return isReference;
    }

    @Override
    public List<Attribute> getAttributes() {
        return List.copyOf(attributes);
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
        for (FormalParameter parameter : getFormalParameters()) {
            if (sbParams.length() > 0) {
                sbParams.append(", "); // NOI18N
            }
            sbParams.append(parameter);
        }
        return sbAttributes.toString()
                + (isReference() ? "&" : "") + getName() // NO18N
                + (sbParams.length() > 0 ? "(" + sbParams.toString() + ")" : "") // NOI18N
                + (getBody() != null ? getBody() : ";"); // NOI18N
    }

    //~ Inner class
    public static class Builder {

        private final int start;
        private final int end;
        private final Identifier name;
        private int modifier;
        private boolean isReference = false;
        private Block body = null;
        private List<FormalParameter> formalParameters = List.of();
        private List<Attribute> attributes = List.of();

        public Builder(int start, int end, Identifier name) {
            this.start = start;
            this.end = end;
            this.name = name;
        }

        public Builder modifier(Integer modifier) {
            this.modifier = modifier == null ? 0 : modifier;
            return this;
        }

        public Builder body(Block body) {
            this.body = body;
            return this;
        }

        public Builder isReference(boolean isReference) {
            this.isReference = isReference;
            return this;
        }

        public Builder parameters(List<FormalParameter> formalParameters) {
            this.formalParameters = List.copyOf(formalParameters);
            return this;
        }

        public Builder attributes(List<Attribute> attributes) {
            this.attributes = List.copyOf(attributes);
            return this;
        }

        public PropertyHookDeclaration build() {
            return new PropertyHookDeclaration(this);
        }
    }
}
