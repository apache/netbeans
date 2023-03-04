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

import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Represent an enum case statement.
 *
 * <pre>
 * e.g.
 * case Foo;
 * case Apple = 1; // Backed Case
 * case Banana = 2;
 * #[A1]
 * case Orange = 3;
 * </pre>
 *
 * @see https://wiki.php.net/rfc/enumerations
 */
public class CaseDeclaration extends BodyDeclaration {

    private final Identifier name;
    @NullAllowed
    private final Expression initializer;

    public CaseDeclaration(int start, int end, @NonNull Identifier name, @NullAllowed Expression initializer) {
        super(start, end, 0);
        this.name = name;
        this.initializer = initializer;
    }

    private CaseDeclaration(int start, int end, int modifier, @NonNull Identifier name, @NullAllowed Expression initializer, List<Attribute> attributes) {
        super(start, end, modifier, false, attributes);
        this.name = name;
        this.initializer = initializer;
    }

    public static CaseDeclaration create(CaseDeclaration declaration, List<Attribute> attributes) {
        assert attributes != null;
        int start = attributes.isEmpty() ? declaration.getStartOffset() : attributes.get(0).getStartOffset();
        return new CaseDeclaration(
                start,
                declaration.getEndOffset(),
                declaration.getModifier(),
                declaration.getName(),
                declaration.getInitializer(),
                attributes
        );
    }

    public Identifier getName() {
        return name;
    }

    @CheckForNull
    public Expression getInitializer() {
        return initializer;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sbAttributes = new StringBuilder();
        getAttributes().forEach(attribute -> sbAttributes.append(attribute).append(" ")); // NOI18N
        return sbAttributes.toString() + "case" + name + initializer == null ? "" : " = " + initializer; // NOI18N
    }

}
