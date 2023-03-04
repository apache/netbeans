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
 * Holds a function name.
 * note that the function name can be expression,
 * <pre>e.g.<pre> foo() - the name is foo
 * $a() - the variable $a holds the function name
 */
public class FunctionName extends ASTNode {

    private Expression name;

    public FunctionName(int start, int end, Expression functionName) {
        super(start, end);
        this.name = functionName;
    }

    /**
     * Returns the name expression of this function name.
     *
     * @return the expression node
     */
    public Expression getName() {
        return this.name;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "" + getName(); //NOI18N
    }

}
