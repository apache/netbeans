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
 * Represents a case statement.
 * A case statement is part of switch statement
 * <pre>e.g.<pre>
 * case expr:
 *   statement1;
 *   break;,
 *
 * default:
 *   statement2;
 */
public class SwitchCase extends Statement {

    private Expression value;
    private ArrayList<Statement> actions = new ArrayList<>();
    private boolean isDefault;

    public SwitchCase(int start, int end, Expression value, Statement[] actions, boolean isDefault) {
        super(start, end);

        if (actions == null) {
            throw new IllegalArgumentException();
        }

        this.value = value;
        this.actions.addAll(Arrays.asList(actions));
        this.isDefault = isDefault;
    }

    public SwitchCase(int start, int end, Expression value, List<Statement> actions, boolean isDefault) {
        this(start, end, value,
                actions == null ? null : actions.toArray(new Statement[0]), isDefault);
    }

    /**
     * The actions of this case statement
     * @return List of actions of this case statement
     */
    public List<Statement> getActions() {
        return this.actions;
    }

    /**
     * True if this is a default case statement
     */
    public boolean isDefault() {
        return isDefault;
    }

    /**
     * The value (expression) of this case statement
     * @return value (expression) of this case statement
     */
    public Expression getValue() {
        return value;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Statement statement : getActions()) {
            sb.append(statement).append(";"); //NOI18N
        }
        return "case " + getValue() + ":" + sb.toString(); //NOI18N
    }

}
