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

import org.netbeans.api.annotations.common.CheckForNull;

/**
 * Represents a fields declaration.
 * <pre>
 * e.g.
 * var $a, $b;
 * public $a = 3;
 * private static final $var;
 * private int $int = 20; // PHP 7.4
 * private int $int = 20 {get => $int + 10; set;} // PHP 8.4
 * </pre>
 */
public class SingleFieldDeclaration extends ASTNode {

    private final Variable name;
    private final Expression value;
    private final Expression fieldType;
    private final Block propertyHooks;

    private SingleFieldDeclaration(Builder builder) {
        super(builder.start, builder.end);
        this.name = builder.name;
        this.value = builder.value;
        this.fieldType = builder.fieldType;
        this.propertyHooks = builder.propertyHooks;
    }

    // Use Builder instead
    public SingleFieldDeclaration(int start, int end, Variable name, Expression value, Expression fieldType) {
        super(start, end);
        this.name = name;
        this.value = value;
        this.fieldType = fieldType;
        this.propertyHooks = null;
    }

    /**
     * @return the name of the field
     */
    public Variable getName() {
        return this.name;
    }

    /**
     * @return the initial value of this field, null if none
     */
    public Expression getValue() {
        return this.value;
    }

    public Expression getFieldType() {
        return fieldType;
    }

    /**
     * Get property(field) hooks. e.g. {get => $this->a + 100; set;}
     *
     * @return property hooks
     * @since 2.45.0
     */
    @CheckForNull
    public Block getPropertyHooks() {
        return propertyHooks;
    }

    /**
     * Check whether this is hooked property(field).
     *
     * @return {@code true} if it's hooked property, {@code false} otherwise
     * @since 2.45.0
     */
    public boolean isHooked() {
        return propertyHooks != null;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (getFieldType() != null) {
            sb.append(getFieldType()).append(" "); // NOI18N
        }
        sb.append(getName());
        if (getValue() != null) {
            sb.append(" = ").append(getValue()); // NOI18N
        }
        if (getPropertyHooks() != null) {
            sb.append(getPropertyHooks());
        }
        return sb.toString();
    }

    //~ Inner class
    public static class Builder {

        private final int start;
        private final int end;
        private final Variable name;
        private Expression value = null;
        private Expression fieldType = null;
        private Block propertyHooks = null;

        public Builder(int start, int end, Variable name) {
            this.start = start;
            this.end = end;
            this.name = name;
        }

        public Builder value(Expression value) {
            this.value = value;
            return this;
        }

        public Builder fieldType(Expression fieldType) {
            this.fieldType = fieldType;
            return this;
        }

        public Builder propertyHooks(Block propertyHooks) {
            this.propertyHooks = propertyHooks;
            return this;
        }

        public SingleFieldDeclaration build() {
            return new SingleFieldDeclaration(this);
        }
    }
}
