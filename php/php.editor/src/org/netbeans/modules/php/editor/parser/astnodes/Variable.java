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
 * Holds a variable. note that the variable name can be expression.
 * <pre>
 * e.g.
 * $a
 * </pre> Subclasses: {@link ArrayAccess}, {@link ReflectionVariable}, {@link StaticFieldAccess},
 * {@link ConstantVariable}
 */
public class Variable extends VariableBase {

    private Expression name;
    private boolean isDollared;

    protected Variable(int start, int end, Expression variableName, boolean isDollared) {
        super(start, end);
        this.name = variableName;
        this.isDollared = isDollared;
    }

    protected Variable(int start, int end, Expression variableName) {
        this(start, end, variableName, false);
    }

    /**
     * A simple variable (like $a) can be constructed with a string
     * The string is warped by an identifier
     * @param start
     * @param end
     * @param variableName
     */
    public Variable(int start, int end, String variableName) {
        this(start, end, createIdentifier(start, end, variableName), checkIsDollared(variableName));
    }

    private static Identifier createIdentifier(int start, int end, String idName) {
        if (checkIsDollared(idName)) {
            idName = idName.substring(1);
            // the start position move after the the dollar mark
            start++;
        }
        return new Identifier(start, end, idName);
    }

    private static boolean checkIsDollared(String variableName) {
        return variableName.indexOf('$') == 0;
    }

    /**
     * Returns the name (expression) of this variable
     *
     * @return the expression name node
     */
    public Expression getName() {
        return name;
    }

    /**
     * True this variable node is dollared
     *
     * @return True if this variable node is dollared
     */
    public boolean isDollared() {
        return isDollared;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return (isDollared() ? "$" : "") + getName(); //NOI18N
    }
}
