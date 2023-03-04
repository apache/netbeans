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
 * Represents a type casting expression
 * <pre>e.g.<pre> (int) $a,
 * (string) $b->foo()
 */
public class CastExpression extends Expression {

    public enum Type {
        INT, // 'int'
        REAL, // 'real'
        STRING, // 'string'
    	ARRAY, // 'array'
    	OBJECT, // 'object'
    	BOOL, // 'bool'
    	UNSET// 'unset'
    }

    private Expression expression;
    private CastExpression.Type castingType;

    public CastExpression(int start, int end, Expression expr, CastExpression.Type castType) {
        super(start, end);

        if (expr == null) {
            throw new IllegalArgumentException();
        }
        this.expression = expr;
        this.castingType = castType;
//        expr.setParent(this);
    }


    /**
     * Returns the type of this cast expression.
     *
     * @return the cast type
     */
    public CastExpression.Type getCastingType() {
        return this.castingType;
    }

    /**
     * Returns the left hand side of this assignment expression.
     *
     * @return the left hand side node
     */
    public Expression getExpression() {
        return this.expression;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "(" + getCastingType() + ") " + getExpression(); //NOI18N
    }

}
