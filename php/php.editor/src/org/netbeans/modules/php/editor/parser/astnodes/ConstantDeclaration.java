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

/**
 * Represents a class or namespace constant declaration.
 *
 * <pre>e.g.
 * const MY_CONST = 5;
 * const MY_CONST = 5, YOUR_CONSTANT = 8;
 * const CONSTANT = [0, 1];
 * private const CONSTANT = 1; // PHP7.1
 * #[A("attribute")]
 * const MY_CONST = "const"; // PHP8.0
 * </pre>
 */
public class ConstantDeclaration extends BodyDeclaration {

    private final ArrayList<Identifier> names = new ArrayList<>();
    private final ArrayList<Expression> initializers = new ArrayList<>();
    private final boolean isGlobal;

    // XXX remove?
    private ConstantDeclaration(int start, int end, List<Identifier> names, List<Expression> initializers, boolean isGlobal) {
        super(start, end, BodyDeclaration.Modifier.IMPLICIT_PUBLIC);

        if (names == null || initializers == null || names.size() != initializers.size()) {
            throw new IllegalArgumentException();
        }

        Iterator<Identifier> iteratorNames = names.iterator();
        Iterator<Expression> iteratorInitializers = initializers.iterator();
        Identifier identifier;
        while (iteratorNames.hasNext()) {
            identifier = iteratorNames.next();
            this.names.add(identifier);
            Expression initializer = iteratorInitializers.next();
            this.initializers.add(initializer);
        }
        this.isGlobal = isGlobal;
    }

    private ConstantDeclaration(int start, int end, int modifier, List<Identifier> names, List<Expression> initializers, boolean isGlobal, List<Attribute> attributes) {
        super(start, end, modifier, false, attributes);
        this.names.addAll(names);
        this.initializers.addAll(initializers);
        this.isGlobal = isGlobal;
    }

    public ConstantDeclaration(int start, int end, int modifier, List variablesAndDefaults, boolean isGlobal) {
        super(start, end, modifier);
        if (variablesAndDefaults == null || variablesAndDefaults.isEmpty()) {
            throw new IllegalArgumentException();
        }

        for (Iterator iter = variablesAndDefaults.iterator(); iter.hasNext();) {
            ASTNode[] element = (ASTNode[]) iter.next();
            assert element != null && element.length == 2 && element[0] != null && element[1] != null;

            this.names.add((Identifier) element[0]);
            this.initializers.add((Expression) element[1]);
        }
        this.isGlobal = isGlobal;
    }

    public static ConstantDeclaration create(ConstantDeclaration declaration, List<Attribute> attributes) {
        assert attributes != null;
        int start = attributes.isEmpty() ? declaration.getStartOffset() : attributes.get(0).getStartOffset();
        return new ConstantDeclaration(
                start,
                declaration.getEndOffset(),
                declaration.getModifier(),
                declaration.getNames(),
                declaration.getInitializers(),
                declaration.isGlobal(),
                attributes
        );
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    /**
     * @return constant initializers expressions
     */
    public List<Expression> getInitializers() {
        return Collections.unmodifiableList(this.initializers);
    }

    /**
     * @return the constant names
     */
    public List<Identifier> getNames() {
        return Collections.unmodifiableList(this.names);
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
        for (Expression expression : getInitializers()) {
            sb.append(expression).append(","); //NOI18N
        }
        return sbAttributes.toString() + getModifierString() + "const " + sb; //NOI18N
    }

}
