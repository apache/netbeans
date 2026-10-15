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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents base class for class declaration and interface declaration.
 */
public abstract class TypeDeclaration extends Statement implements Attributed {

    private Identifier name;
    private final ArrayList<Expression> interfaces = new ArrayList<>();
    private Block body;
    private final List<Attribute> attributes;

    public TypeDeclaration(int start, int end, final Identifier name, final Expression[] interfaces, final Block body) {
        this(start, end, name, interfaces, body, Collections.emptyList());
    }

    public TypeDeclaration(int start, int end, final Identifier name, final Expression[] interfaces, final Block body, List<Attribute> attributes) {
        super(start, end);

        if (name == null || body == null) {
            throw new IllegalArgumentException();
        }

        this.name = name;
        this.body = body;

        if (interfaces != null) {
            this.interfaces.addAll(Arrays.asList(interfaces));
        }
        this.attributes = attributes;
    }

    /**
     * The body component of this type declaration node.
     *
     * @return body component of this type declaration node
     */
    public Block getBody() {
        return body;
    }

    /**
     * The name of the type declaration node.
     *
     * @return name of the type declaration node
     */
    public Identifier getName() {
        return this.name;
    }

    /**
     * List of interfaces that this type implements / extends.
     *
     * @return interfaces
     * @deprecated instead, use {@link #getInterfaces()}
     */
    @Deprecated
    public List<Expression> getInterfaes() {
        return getInterfaces();
    }

    /**
     * List of interfaces that this type implements / extends.
     *
     * @return interfaces
     * @since 2.34.0
     */
    public List<Expression> getInterfaces() {
        return Collections.unmodifiableList(interfaces);
    }

    /**
     * Get attributes(#[A('param')]).
     *
     * e.g.
     * <pre>
     * #[A1(1)]
     * #[A2(2)]
     * #[A3(3)]
     * class Foo {}
     *
     * #[A1]
     * interface MyInterface {}
     * </pre>
     *
     * @return attributes
     */
    @Override
    public List<Attribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    @Override
    public boolean isAttributed() {
        return !attributes.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sbAttributes = new StringBuilder();
        getAttributes().forEach(attribute -> sbAttributes.append(attribute).append(" ")); // NOI18N
        StringBuilder sb = new StringBuilder();
        for (Expression expression : getInterfaces()) {
            sb.append(expression).append(","); //NOI18N
        }
        return sbAttributes.toString() + getName() + (sb.length() > 0 ? " " + sb.toString() : " ") + getBody(); //NOI18N
    }

}
