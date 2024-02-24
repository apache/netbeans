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
import java.util.List;

/**
 * Represents back tick expression
 * <pre>e.g.<pre>
 * `.\exec.sh`
 */
public class BackTickExpression extends Expression {

    private ArrayList<Expression> expressions = new ArrayList<>();

    public BackTickExpression(int start, int end, Expression[] expressions) {
        super(start, end);

        if (expressions == null) {
            throw new IllegalArgumentException();
        }
        this.expressions.addAll(Arrays.asList(expressions));
    }

    public BackTickExpression(int start, int end, List<Expression> expressions) {
        this(start, end, expressions == null ? null : expressions.toArray(new Expression[0]));
    }

    /**
     * The expression list of the back tick command
     * @return expressions of this back tick expression
     */
    public List<Expression> getExpressions() {
        return this.expressions;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Expression expression : getExpressions()) {
            sb.append(expression).append(","); //NOI18N
        }
        return "`" + sb + "`"; //NOI18N
    }

}
