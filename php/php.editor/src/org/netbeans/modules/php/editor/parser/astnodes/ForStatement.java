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
 * Represents a for statement
 * <pre>e.g.<pre>
 * for (expr1; expr2; expr3)
 * 	 statement;
 *
 * for (expr1; expr2; expr3):
 * 	 statement
 * 	 ...
 * endfor;
 */
public class ForStatement extends Statement {

    private final ArrayList<Expression> initializers = new ArrayList<>();
    private final ArrayList<Expression> conditions = new ArrayList<>();
    private final ArrayList<Expression> updaters = new ArrayList<>();
    private Statement body;

    private ForStatement(int start, int end, Expression[] initializations, Expression[] conditions, Expression[] increasements, Statement action) {
        super(start, end);

        if (initializations == null || conditions == null || increasements == null || action == null) {
            throw new IllegalArgumentException();
        }
        this.initializers.addAll(Arrays.asList(initializations));
        this.conditions.addAll(Arrays.asList(conditions));
        this.updaters.addAll(Arrays.asList(increasements));
        this.body = action;
    }

    public ForStatement(int start, int end, List<Expression> initializations, List<Expression> conditions, List<Expression> increasements, Statement action) {
        this(start, end,
                initializations == null ? null : initializations.toArray(new Expression[0]),
                conditions == null ? null : conditions.toArray(new Expression[0]),
                increasements == null ? null : increasements.toArray(new Expression[0]),
                action);
    }

    /**
     * Returns the live ordered list of initializer expressions in this for
     * statement.
     * <p>
     * The list should consist of either a list of so called statement
     * expressions (JLS2, 14.8), or a single <code>VariableDeclarationExpression</code>.
     * Otherwise, the for statement would have no Java source equivalent.
     * </p>
     *
     * @return the live list of initializer expressions
     *    (element type: <code>Expression</code>)
     */
    public List<Expression> getInitializers() {
        return this.initializers;
    }

    /**
     * Returns the condition expression of this for statement, or
     * <code>null</code> if there is none.
     *
     * @return the condition expression node, or <code>null</code> if
     *     there is none
     */
    public List<Expression> getConditions() {
        return this.conditions;
    }

    /**
     * Returns the live ordered list of update expressions in this for
     * statement.
     * <p>
     * The list should consist of so called statement expressions. Otherwise,
     * the for statement would have no Java source equivalent.
     * </p>
     *
     * @return the live list of update expressions
     *    (element type: <code>Expression</code>)
     */
    public List<Expression> getUpdaters() {
        return this.updaters;
    }

    /**
     * Returns the body of this for statement.
     *
     * @return the body statement node
     */
    public Statement getBody() {
        return this.body;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sbInit = new StringBuilder();
        for (Expression expression : getInitializers()) {
            sbInit.append(expression).append(","); //NOI18N
        }
        StringBuilder sbCond = new StringBuilder();
        for (Expression expression : getConditions()) {
            sbCond.append(expression).append(","); //NOI18N
        }
        StringBuilder sbUpd = new StringBuilder();
        for (Expression expression : getUpdaters()) {
            sbUpd.append(expression).append(","); //NOI18N
        }
        return "for (" + sbInit + ";" + sbCond + ";" + sbUpd + ")" + getBody(); //NOI18N
    }

}
