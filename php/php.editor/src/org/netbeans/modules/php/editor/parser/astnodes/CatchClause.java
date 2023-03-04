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

import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Represents a catch clause (as part of a try statement).
 *
 * <pre>e.g.
 * catch (ExceptionClassName $variable) { body; },
 * catch (ExceptionA | ExceptionB $ex) { body; } // PHP7.1
 * catch (Exception) { echo "message"; } // PHP8.0
 * catch (ExceptionA | ExceptionB) { echo "message"; } // PHP8.0
 * </pre>
 */
public class CatchClause extends Statement {

    private final List<Expression> classNames;
    @NullAllowed
    private final Variable variable;
    private final Block body;

    public CatchClause(int start, int end, List<Expression> classNames, @NullAllowed Variable variable, Block statement) {
        super(start, end);

        assert !classNames.isEmpty() && statement != null;
        this.classNames = classNames;
        this.variable = variable;
        this.body = statement;
//        className.setParent(this);
//        variable.setParent(this);
//        statement.setParent(this);
    }

    /**
     * Returns the class names of this catch clause.
     *
     * @return the exception class names
     */
    public List<Expression> getClassNames() {
        return Collections.unmodifiableList(this.classNames);
    }

    /**
     * Returns the exception variable declaration of this catch clause.
     *
     * @return the exception variable declaration node or {@code null} if it's
     * non-capturing catches
     */
    @CheckForNull
    public Variable getVariable() {
        return this.variable;
    }

    /**
     * Returns the body of this catch clause.
     *
     * @return the catch clause body
     */
    public Block getBody() {
        return this.body;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        getClassNames().forEach((name) -> {
            if (!first) {
                sb.append(" | "); // NOI18N
            }
            sb.append(name);
        });
        if (variable != null) {
            sb.append(" ").append(getVariable()); // NOI18N
        }
        return "catch (" + sb.toString() + ")" + getBody(); //NOI18N
    }

}
