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
 * </pre>
 */
public class FieldsDeclaration extends BodyDeclaration {

    private final List<SingleFieldDeclaration> fields = new ArrayList<>();
    private final Expression fieldType;

    private FieldsDeclaration(int start, int end, int modifier, Expression fieldType, List<SingleFieldDeclaration> fields, List<Attribute> attributes) {
        super(start, end, modifier, false, attributes);
        this.fieldType = fieldType;
        this.fields.addAll(fields);
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
    }

    public static FieldsDeclaration create(FieldsDeclaration declaration, List<Attribute> attributes) {
        assert attributes != null;
        int start = attributes.isEmpty() ? declaration.getStartOffset() : attributes.get(0).getStartOffset();
        return new FieldsDeclaration(
                start,
                declaration.getEndOffset(),
                declaration.getModifier(),
                declaration.getFieldType(),
                declaration.getFields(),
                attributes
        );
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
        if (!BodyDeclaration.Modifier.isVisibilityModifier(parameter.getModifier())) {
            return null;
        }
        Variable variable = null;
        Expression expression = parameter.getParameterName();
        if (expression instanceof Reference) {
            expression = ((Reference) expression).getExpression();
        }
        if (expression instanceof Variadic) {
            // just check because the parser accepts it
            // although can't be declared variadic promoted property
            expression = ((Variadic) expression).getExpression();
        }
        if (expression instanceof Variable) {
            variable = (Variable) expression;
        }
        assert variable != null;
        int start = variable.getStartOffset();
        Expression type = parameter.getParameterType();
        int end = variable.getEndOffset();
        Expression value = parameter.getDefaultValue();
        if (value != null) {
            end = value.getEndOffset();
        }
        SingleFieldDeclaration singleFieldDeclaration = new SingleFieldDeclaration(start, end, variable, value, type);
        return FieldsDeclaration.create(
                new FieldsDeclaration(
                        parameter.getStartOffset(),
                        parameter.getEndOffset(),
                        parameter.getModifier(),
                        parameter.getParameterType(),
                        Collections.singletonList(singleFieldDeclaration)
                ),
                parameter.getAttributes()
        );
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
        return Collections.unmodifiableList(this.fields);
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

}
