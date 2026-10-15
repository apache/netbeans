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
import java.util.LinkedList;
import java.util.List;

/**
 * Represents the static statement.
 *
 * e.g.
 * <pre>
 * static $a
 * static $a, $b=5;
 * </pre>
 */
public class StaticStatement extends Statement {

    private final ArrayList<Expression> expressions = new ArrayList<>();

    private StaticStatement(int start, int end, Expression[] expressions) {
        super(start, end);
        if (expressions == null) {
            throw new IllegalArgumentException();
        }
        this.expressions.addAll(Arrays.asList(expressions));
    }

    public StaticStatement(int start, int end, List<Exception> expressions) {
        this(start, end, expressions == null ? null : expressions.toArray(new Expression[0]));
    }

    /**
     * @return the variables that participate in the static call
     */
    public Variable[] getVariables() {
        List<Variable> vars = new LinkedList<>();
        for (Expression node : this.expressions) {
            if (node instanceof Variable) {
                vars.add((Variable) node);
            } else {
                assert node instanceof Assignment;
                Assignment ass = (Assignment) node;
                vars.add((Variable) ass.getLeftHandSide());
            }
        }
        return vars.toArray(new Variable[0]);
    }

    /**
     * @return expression list of the static statement
     */
    public List<Expression> getExpressions() {
        return Collections.unmodifiableList(this.expressions);
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
        return "static " + sb.toString(); //NOI18N
    }

}
