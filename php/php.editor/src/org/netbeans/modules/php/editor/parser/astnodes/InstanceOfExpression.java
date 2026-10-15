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

/**
 * Represent instanceof expression.
 *
 * e.g.
 * <pre>
 * $a instanceof MyClass,
 * foo() instanceof $myClass,
 * $a instanceof $b->$myClass,
 * $a instansof ((string) $myClass)
 * </pre>
 */
public class InstanceOfExpression extends Expression {

    private Expression expression;
    private ClassName className;

    public InstanceOfExpression(int start, int end, Expression expr, ClassName className) {
        super(start, end);

        if (expr == null || className == null) {
            throw new IllegalArgumentException();
        }
        this.className = className;
        this.expression = expr;
    }

    /**
     * The expression of this instance of expression
     *
     * @return expression of this instance of expression
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * @return class name
     */
    public final ClassName getClassName() {
        return className;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getExpression() + " instanceof " + getClassName(); //NOI18N
    }

}
