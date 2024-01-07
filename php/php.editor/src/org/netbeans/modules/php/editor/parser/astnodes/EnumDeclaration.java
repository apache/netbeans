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

import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Represents an enum declaration.
 *
 * <pre>
 * e.g.
 * enum Foo {}
 * enum Foo {
 *     case A;
 *     case B;
 * }
 * enum Foo : int { // Backed Enum
 *     case A = 1; // Backed Case
 *     case B = 2;
 * }
 * enum Foo : string {
 *     case A = "a";
 *     case B = "b";
 *     const = BAR = "bar";
 * }
 * #[A1]
 * enum Foo : string implements Iface {
 *     case A = "a";
 *     case B = "b";
 *     const = BAR = "bar";
 *     public function implementMethod() {
 *     }
 * }
 * </pre>
 *
 * @see https://wiki.php.net/rfc/enumerations
 */
public class EnumDeclaration extends TypeDeclaration {

    @NullAllowed
    private final Expression backingType;

    private EnumDeclaration(int start, int end, Identifier className, Expression backingType, Expression[] interfaces, Block body, List<Attribute> attributes) {
        super(start, end, className, interfaces, body, attributes);
        this.backingType = backingType;
    }

    private EnumDeclaration(int start, int end, Identifier className, Expression backingType, List<Expression> interfaces, Block body, List<Attribute> attributes) {
        this(start, end, className, backingType, interfaces == null ? null : interfaces.toArray(new Expression[0]), body, attributes);
    }

    public EnumDeclaration(int start, int end, Identifier name, @NullAllowed Expression backingType, List<Expression> interfaces, Block body) {
        this(start, end, name, backingType, interfaces, body, Collections.emptyList());
    }

    public static EnumDeclaration create(EnumDeclaration declaration, List<Attribute> attributes) {
        assert attributes != null;
        int start = attributes.isEmpty() ? declaration.getStartOffset() : attributes.get(0).getStartOffset();
        return new EnumDeclaration(
                start,
                declaration.getEndOffset(),
                declaration.getName(),
                declaration.getBackingType(),
                declaration.getInterfaces(),
                declaration.getBody(),
                attributes
        );
    }

    @CheckForNull
    public Expression getBackingType() {
        return backingType;
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
        for (Expression expression : getInterfaces()) {
            sb.append(expression).append(","); // NOI18N
        }
        return sbAttributes.toString() + "enum " + getName() + backingType == null ? "" : ": " + backingType + " implements " + sb + getBody(); // NOI18N
    }
}
