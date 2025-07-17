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
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;

/**
 * Represents a fields declaration.
 * <pre>
 * e.g.
 * var $a, $b;
 * public $a = 3;
 * private static final $var;
 * protected ?int $int = 0; // PHP 7.4
 * public private(set) string $hooked = "property hook" { // PHP 8.4
 *     get => "hooked: ". $this->hooked;
 *     set($value) {
 *         echo "set: " . $value;
 *     }
 * }
 * </pre>
 */
public class FieldsDeclaration extends BodyDeclaration {

    private final List<SingleFieldDeclaration> fields = new ArrayList<>();
    private final Expression fieldType;
    private final boolean isHooked;

    private FieldsDeclaration(Builder builder) {
        super(builder.start, builder.end, builder.modifier, false, builder.attributes);
        this.fieldType = builder.fieldType;
        boolean isHookedField = false;
        for (SingleFieldDeclaration field : builder.fields) {
            if ((fieldType == null && field.getFieldType() == null)
                    || (fieldType != null && fieldType.equals(field.getFieldType()))) {
                this.fields.addAll(builder.fields);
                isHookedField = field.isHooked();
                break;
            }
            SingleFieldDeclaration singleField = new SingleFieldDeclaration.Builder(field.getStartOffset(), field.getEndOffset(), field.getName())
                    .fieldType(fieldType) // add type
                    .value(field.getValue())
                    .propertyHooks(field.getPropertyHooks())
                    .build();
            this.fields.add(singleField);
            if (field.isHooked()) {
                assert builder.fields.size() == 1 : "FieldsDeclaration can have only one hooked property, but fields size: " + builder.fields.size(); // NOI18N
                isHookedField = true;
                break;
            }
        }
        this.isHooked = isHookedField;
    }

    public FieldsDeclaration(int start, int end, int modifier, Expression fieldType, List variablesAndDefaults) {
        super(start, end, modifier);

        if (variablesAndDefaults == null || variablesAndDefaults.isEmpty()) {
            throw new IllegalArgumentException();
        }

        for (Iterator iter = variablesAndDefaults.iterator(); iter.hasNext();) {
            final Object next = iter.next();
            if (next instanceof SingleFieldDeclaration) {
                this.fields.add((SingleFieldDeclaration) next);
            } else {
                ASTNode[] element = (ASTNode[]) next;
                SingleFieldDeclaration field = createField((Variable) element[0], (Expression) element[1], fieldType);
                this.fields.add(field);
            }
        }
        this.fieldType = fieldType;
        this.isHooked = false;
    }

    public static FieldsDeclaration create(FieldsDeclaration declaration, List<Attribute> attributes) {
        assert attributes != null;
        int start = attributes.isEmpty() ? declaration.getStartOffset() : attributes.get(0).getStartOffset();
        return new Builder(start, declaration.getEndOffset(), declaration.getModifier())
                .fieldType(declaration.getFieldType())
                .fields(declaration.getFields())
                .attributes(attributes)
                .build();
    }

    /**
     * Convert from FormalParameter to FieldsDeclaration.
     *
     * @param parameter
     * @return the FieldsDeclaration, can be {@code null} if the parameter
     * doesn't have a visibility modifier.
     */
    @CheckForNull
    public static FieldsDeclaration create(FormalParameter parameter) {
        // [NETBEANS-4443] PHP 8.0 Constructor Property Promotion
        // convert from FormalParameter of constructor to FieldsDeclaration
        if (!BodyDeclaration.Modifier.isVisibilityModifier(parameter.getModifier())
                && !BodyDeclaration.Modifier.isSetVisibilityModifier(parameter.getModifier())) {
            return null;
        }
        Variable variable = null;
        Expression expression = parameter.getParameterName();
        if (expression instanceof Reference reference) {
            expression = reference.getExpression();
        }
        if (expression instanceof Variadic variadic) {
            // just check because the parser accepts it
            // although can't be declared variadic promoted property
            expression = variadic.getExpression();
        }
        if (expression instanceof Variable var) {
            variable = var;
        }
        assert variable != null;
        int start = variable.getStartOffset();
        Expression type = parameter.getParameterType();
        int end = variable.getEndOffset();
        Expression value = parameter.getDefaultValue();
        if (value != null) {
            end = value.getEndOffset();
        }
        SingleFieldDeclaration singleFieldDeclaration = new SingleFieldDeclaration.Builder(start, end, variable)
                .value(value)
                .fieldType(type)
                .propertyHooks(parameter.getPropertyHooks())
                .build();
        return new Builder(parameter.getStartOffset(), parameter.getEndOffset(), parameter.getModifier())
                .fieldType(parameter.getParameterType())
                .fields(List.of(singleFieldDeclaration))
                .attributes(parameter.getAttributes())
                .build();
    }

    private SingleFieldDeclaration createField(Variable name, Expression value, Expression fieldType) {
        int start = name.getStartOffset();
        int end = value == null ? name.getEndOffset() : value.getEndOffset();
        final SingleFieldDeclaration result = new SingleFieldDeclaration(start, end, name, value, fieldType);
        return result;
    }

    /**
     * The list of single fields that are declared
     *
     * @return List of single fields
     */
    public List<SingleFieldDeclaration> getFields() {
        return List.copyOf(fields);
    }

    public Expression[] getInitialValues() {
        Expression[] result = new Expression[this.fields.size()];
        int i = 0;
        for (SingleFieldDeclaration field : this.fields) {
            result[i++] = field.getValue();
        }
        return result;
    }

    public Variable[] getVariableNames() {
        Variable[] result = new Variable[this.fields.size()];
        int i = 0;
        for (SingleFieldDeclaration field : this.fields) {
            result[i++] = field.getName();
        }
        return result;
    }

    @CheckForNull
    public Expression getFieldType() {
        return fieldType;
    }

    /**
     * Check whether this is a hooked field(property).
     *
     * @return {@code true} if this is hooked field, {@code false} otherwise
     * @since 2.45.0
     */
    public boolean isHooked() {
        return isHooked;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sbAttributes = new StringBuilder();
        getAttributes().forEach(attribute -> sbAttributes.append(attribute).append(" ")); // NOI18N
        StringBuilder sb = new StringBuilder();
        for (SingleFieldDeclaration singleFieldDeclaration : getFields()) {
            sb.append(singleFieldDeclaration).append(" "); //NOI18N
        }
        String modifierString = getModifierString();
        if (modifierString != null && modifierString.isEmpty()) {
            modifierString += " "; // NOI18N
        }
        return sbAttributes.toString()
                + modifierString + " " // NOI18N
                + sb.toString();
    }

    //~ Inner class
    public static class Builder {

        private final int start;
        private final int end;
        private final int modifier;
        private List<SingleFieldDeclaration> fields = List.of();
        private List<Attribute> attributes = List.of();
        private Expression fieldType = null;

        public Builder(int start, int end, Integer modifier) {
            this.start = start;
            this.end = end;
            this.modifier = modifier == null ? 0 : modifier;
        }

        public Builder fieldType(Expression fieldType) {
            this.fieldType = fieldType;
            return this;
        }

        public Builder fields(List<SingleFieldDeclaration> fields) {
            this.fields = List.copyOf(fields);
            return this;
        }

        public Builder attributes(List<Attribute> attributes) {
            this.attributes = List.copyOf(attributes);
            return this;
        }

        public FieldsDeclaration build() {
            return new FieldsDeclaration(this);
        }
    }
}
