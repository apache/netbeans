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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents function invocation.
 *
 * Holds the function name and the invocation parameters.
 * <pre>e.g.
 * foo(),
 * $a(),
 * foo($a, 'a', 12),
 * foo($a, 'a', 12,) // PHP 7.3
 * </pre>
 */
public class FunctionInvocation extends VariableBase {

    private FunctionName functionName;
    private final ArrayList<Expression> parameters = new ArrayList<>();

    private FunctionInvocation(int start, int end, FunctionName functionName, Expression[] parameters) {
        super(start, end);
        this.functionName = functionName;
        this.parameters.addAll(Arrays.asList(parameters));
    }

    public FunctionInvocation(int start, int end, FunctionName functionName, List<Expression> parameters) {
        this(start, end, functionName, parameters == null ? new Expression[0] : parameters.toArray(new Expression[0]));
    }

    /**
     * The function name component of this function invocation
     *
     * @return function name component of this function invocation
     */
    public FunctionName getFunctionName() {
        return functionName;
    }

    /**
     * @return the parameters component of this function invocation expression
     */
    public List<Expression> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Expression expression : getParameters()) {
            sb.append(expression).append(","); //NOI18N
        }
        return getFunctionName() + "(" + sb.toString() + ")"; //NOI18N
    }

}
