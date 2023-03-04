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

/**
 * Represents a match arm. PHP 8.0: https://wiki.php.net/rfc/match_expression_v2
 *
 * e.g.
 * <pre>
 * 1 => '1',
 * 2, 3 => '2, 3',
 * foo() => 'foo()',
 * default => getDefault(),
 * </pre>
 *
 * @see MatchExpression
 */
public class MatchArm extends Expression {

    private final List<Expression> conditions;
    private final Expression expression;
    private final boolean isDefault;

    public MatchArm(int start, int end, List<Expression> conditions, Expression expression, boolean isDefault) {
        super(start, end);
        this.conditions = new ArrayList<>(conditions);
        this.expression = expression;
        this.isDefault = isDefault;
    }

    public List<Expression> getConditions() {
        return Collections.unmodifiableList(conditions);
    }

    public Expression getExpression() {
        return expression;
    }

    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        conditions.forEach(condition -> {
            if (sb.length() > 0) {
                sb.append(", "); // NOI18N
            }
            sb.append(condition);
        });
        sb.append(" => "); // NOI18N
        sb.append(getExpression()).append(","); // NOI18N
        return sb.toString();
    }

}
