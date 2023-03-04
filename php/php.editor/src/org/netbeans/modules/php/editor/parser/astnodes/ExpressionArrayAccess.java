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
 * <pre>e.g.
 * [1, 2, 3][0];
 * "String"[1];
 * CONSTANT[1][2]; MyClass::CONSTANT[1];
 * \Foo\CONSTANT[0]; namespace\CONSTANT[0];
 * </pre>
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ExpressionArrayAccess extends VariableBase {
    private final Expression expression;
    private final ArrayDimension dimension;

    public ExpressionArrayAccess(int start, int end, Expression expression, ArrayDimension dimension) {
        super(start, end);
        this.expression = expression;
        this.dimension = dimension;
    }

    public Expression getExpression() {
        return expression;
    }

    public ArrayDimension getDimension() {
        return dimension;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "" + getExpression() + getDimension(); //NOI18N
    }

}
