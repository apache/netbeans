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
import java.util.List;

/**
 * Represents a global statement
 * <pre>e.g.<pre> global $a
 * global $a, $b
 * global ${foo()->bar()},
 * global $$a
 */
public class GlobalStatement extends Statement {

    private final ArrayList<Variable> variables = new ArrayList<>();

    private GlobalStatement(int start, int end, Variable[] variables) {
        super(start, end);

        if (variables == null) {
            throw new IllegalArgumentException();
        }
        this.variables.addAll(Arrays.asList(variables));
    }

    public GlobalStatement(int start, int end, List<Variable> variables) {
        this(start, end, variables == null ? null : variables.toArray(new Variable[0]));
    }

    /**
     * @return the variables component of the global statement
     */
    public List<Variable> getVariables() {
        return variables;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Variable variable : getVariables()) {
            sb.append(variable).append(","); //NOI18N
        }
        return "global " + sb.toString(); //NOI18N
    }

}
