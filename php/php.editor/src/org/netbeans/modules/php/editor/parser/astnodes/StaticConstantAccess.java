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
 * Represents a constant class access.
 *
 * e.g.
 * <pre>
 * MyClass::CONST;
 * MyClass::CONSTANT[0];
 * MyClass::{$expr}; // PHP 8.3 Dynamic class constant fetch
 * $myClass::{$expr}; // PHP 8.3 Dynamic class constant fetch
 * </pre>
 */
public class StaticConstantAccess extends StaticDispatch {

    private final Expression constant;
    private final boolean isDynamicName;

    public StaticConstantAccess(int start, int end, Expression className, Expression constant, boolean isDynamicName) {
        super(start, end, className);
        this.constant = constant;
        this.isDynamicName = isDynamicName;
    }

    public StaticConstantAccess(int start, int end, Expression className, Expression constant) {
        this(start, end, className, constant, false);
    }

    public StaticConstantAccess(int start, int end, Identifier name) {
        this(start, end, null, name);
    }

    /**
     * Constant name of this static dispatch
     *
     * @return constant name of this static dispatch
     */
    public Expression getConstant() {
        return constant;
    }

    public Identifier getConstantName() {
        Expression expression = constant;
        while (expression instanceof ExpressionArrayAccess) {
            expression = ((ExpressionArrayAccess) expression).getExpression();
        }
        // can use dynamic constant names since PHP 8.3
        // e.g. Foo::{$bar};
        assert expression != null;
        if (isDynamicName) {
            // create an identifier to avoid NPE (can improve this?)
            if (expression instanceof ReflectionVariable) {
                expression = ((ReflectionVariable) expression).getName();
            }
            expression = new Identifier(constant.getStartOffset(), constant.getEndOffset(), "{" + expression.toString() + "}"); // NOI18N
        }
        assert expression instanceof Identifier : "Expected Identifier but got: " + expression.getClass().getName(); // NOI18N
        return (Identifier) expression;
    }

    public boolean isDynamicName() {
        return isDynamicName;
    }

    @Override
    public ASTNode getMember() {
        return getConstant();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
