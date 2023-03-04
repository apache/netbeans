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
 * Represents include, include_once, require and require_once expressions
 * <pre>e.g.<pre> include('myFile.php'),
 * include_once($myFile),
 * require($myClass->getFileName()),
 * require_once(A::FILE_NAME)
 */
public class Include extends Expression {

    public enum Type {
        REQUIRE,
        REQUIRE_ONCE,
        INCLUDE,
        INCLUDE_ONCE
    }

    private Expression expression;
    private Include.Type includeType;

    public Include(int start, int end, Expression expr, Include.Type type) {
        super(start, end);

        if (expr == null) {
            throw new IllegalArgumentException();
        }
        this.expression = expr;
        this.includeType = type;
    }


    /**
     * Returns the expression of this include.
     *
     * @return the expression node
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * the include type one of the following {@link #IT_INCLUDE_ONCE}, {@link #IT_INCLUDE},
     * 	{@link #IT_REQUIRE_ONCE}, {@link #IT_REQUIRE}
     * @return include type
     */
    public Include.Type getIncludeType() {
        return this.includeType;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getIncludeType() + " " + getExpression(); //NOI18N
    }

}
