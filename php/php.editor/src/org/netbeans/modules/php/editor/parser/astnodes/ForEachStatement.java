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

/**
 * Represents a for each statement
 * <pre>e.g.<pre>
 * foreach (array_expression as $value)
 *   statement;
 *
 * foreach (array_expression as list($v1, $v2))
 *   statement;
 *
 * foreach (array_expression as $key => $value)
 *   statement;
 *
 * foreach (array_expression as $key => list($v1, $v2))
 *   statement;
 *
 * foreach (array_expression as $key => $value):
 *   statement;
 *   ...
 * endforeach;
 */
public class ForEachStatement extends Statement {

    private Expression expression;
    private Expression key;
    private Expression value;
    private Statement statement;

    public ForEachStatement(int start, int end, Expression expression, Expression key, Expression value, Statement statement) {
        super(start, end);

        if (expression == null || value == null || statement == null) {
            throw new IllegalArgumentException();
        }
        this.expression = expression;
        this.value = value;
        this.key = key;
        this.statement = statement;
    }

    public ForEachStatement(int start, int end, Expression expression, Expression value, Statement statement) {
        this(start, end, expression, null, value, statement);
    }

    /**
     * Returns the expression of this for each statement.
     *
     * @return the expression node
     */
    public Expression getExpression() {
        return this.expression;
    }

    /**
     * @return the key component of this for each statement
     */
    public Expression getKey() {
        return key;
    }

    /**
     * @return the value component of this for each statement
     */
    public Expression getValue() {
        return this.value;
    }

    /**
     * @return the statement component of this for each statement
     */
    public Statement getStatement() {
        return this.statement;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "foreach (" + getExpression() + " as " + getKey() + " => " + getValue() + ")" + getStatement(); //NOI18N
    }

}
