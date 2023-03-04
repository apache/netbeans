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

import java.util.Collections;
import java.util.List;

/**
 * Represents an attribute.
 *
 * NETBEANS-4443 PHP 8.0 Support
 *
 * https://wiki.php.net/rfc/attributes_v2
 * https://wiki.php.net/rfc/shorter_attribute_syntax
 * https://wiki.php.net/rfc/shorter_attribute_syntax_change
 *
 * <pre>e.g.
 * #[A],
 * #[A(0)],
 * #[A('foo', 'bar')],
 * #[A1(1), A2(2),], // grouping
 * </pre>
 */
public class Attribute extends Expression {

    private final List<AttributeDeclaration> attributeDeclarations;

    public Attribute(int start, int end, List<AttributeDeclaration> attributeDeclarations) {
        super(start, end);
        this.attributeDeclarations = attributeDeclarations;
    }

    /**
     * Get the attribute declarations.
     *
     * <pre>e.g.
     * A(0),
     * A1(1), A2(2)
     * </pre>
     * @return the attribute declarations
     */
    public List<AttributeDeclaration> getAttributeDeclarations() {
        return Collections.unmodifiableList(attributeDeclarations);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (AttributeDeclaration attributeDeclaration : getAttributeDeclarations()) {
            if (sb.length() > 0) {
                sb.append(", "); // NOI18N
            }
            sb.append(attributeDeclaration);
        }
        return "#[" + sb.toString() +  "]"; // NOI18N
    }
}
