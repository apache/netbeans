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
 * Represents if statement
 * <pre>e.g.<pre>
 * if ($a > $b) {
 *   echo "a is bigger than b";
 * } elseif ($a == $b) {
 *   echo "a is equal to b";
 * } else {
 *   echo "a is smaller than b";
 * },
 *
 * if ($a):
 *   echo "a is bigger than b";
 *   echo "a is NOT bigger than b";
 * endif;
 */
public class IfStatement extends Statement {

    private Expression condition;
    private Statement trueStatement;
    private Statement falseStatement;

    public IfStatement(int start, int end, Expression condition, Statement trueStatement, Statement falseStatement) {
        super(start, end);

        this.condition = condition;
        this.trueStatement = trueStatement;
        this.falseStatement = falseStatement;
    }

    /**
     * Returns the expression of this if statement.
     *
     * @return the expression node
     */
    public Expression getCondition() {
        return this.condition;
    }

    /**
     * Returns the "then" part of this if statement.
     *
     * @return the "then" statement node
     */
    public Statement getTrueStatement() {
        return this.trueStatement;
    }

    /**
     * Returns the "else" part of this if statement, or <code>null</code> if
     * this if statement has <b>no</b> "else" part.
     * <p>
     * Note that there is a subtle difference between having no else
     * statement and having an empty statement ("{}") or null statement (";").
     * </p>
     *
     * @return the "else" statement node, or <code>null</code> if none
     */
    public Statement getFalseStatement() {
        return this.falseStatement;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "if (" + getCondition() + ")" + getTrueStatement() + (getFalseStatement() == null ? "" : " else " + getFalseStatement()); //NOI18N
    }

}
