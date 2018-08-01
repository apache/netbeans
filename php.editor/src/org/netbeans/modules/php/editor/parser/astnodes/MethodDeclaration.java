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
 * Represents a function declaration in a class
 * Holds the function modifier
 * @see {@link FunctionDeclaration}
 */
public class MethodDeclaration extends BodyDeclaration {

    private FunctionDeclaration function;

    public MethodDeclaration(int start, int end, int modifier, FunctionDeclaration function, boolean shouldComplete) {
        super(start, end, modifier, shouldComplete);

        if (function == null) {
            throw new IllegalArgumentException();
        }
        this.function = function;
    }

    public MethodDeclaration(int start, int end, int modifier, FunctionDeclaration function) {
        this(start, end, modifier, function, false);
    }

    /**
     * The function declaration component of this method
     *
     * @return function declaration component of this method
     */
    public FunctionDeclaration getFunction() {
        return function;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "" + getFunction(); //NOI18N
    }

}
