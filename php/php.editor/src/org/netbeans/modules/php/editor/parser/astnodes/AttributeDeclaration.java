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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Represents an attribute declaration.
 *
 * [NETBEANS-4443] PHP 8.0 Support
 *
 * https://wiki.php.net/rfc/attributes_v2
 * https://wiki.php.net/rfc/shorter_attribute_syntax
 * https://wiki.php.net/rfc/shorter_attribute_syntax_change
 *
 * <pre>e.g.
 * A (#[A]),
 * A(0) (#[A(0)]),
 * A('foo', 'bar') (#[A('foo', 'bar')]),
 * </pre>
 */
public class AttributeDeclaration extends Expression {

    private final Expression attributeName;
    @NullAllowed
    private final List<Expression> parameters;

    public AttributeDeclaration(int start, int end, Expression attributeName, @NullAllowed List<Expression> parameters) {
        super(start, end);
        this.attributeName = attributeName;
        this.parameters = parameters == null ? null : new ArrayList<>(parameters);
    }

    /**
     * Get the attribute name.
     *
     * e.g. in #[MyAttribute()] case, the name is MyAttribute
     *
     * @return the attribute name
     */
    public Expression getAttributeName() {
        return attributeName;
    }

    /**
     * Get the parameters.
     *
     * e.g.
     * #[MyAttribute]: without parameters, in this case, {@code null}
     * #[MyAttribute()]: with empty parameter
     * #[MyAttribute(1, 2)]: with parameters
     *
     * @return the parameters if the attribute has them, can be {@code null}
     */
    @CheckForNull
    public List<Expression> getParameters() {
        return parameters == null ? null : Collections.unmodifiableList(parameters);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getAttributeName());
        if (parameters != null) {
            sb.append("("); // NOI18N
            parameters.forEach(parameter -> sb.append(parameter));
            sb.append(")"); // NOI18N
        }
        return sb.toString();
    }

}
