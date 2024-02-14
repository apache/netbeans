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
 * Represents a block of statements
 * <pre>e.g.<pre>
 * {
 *   statement1;
 *   statement2;
 * },
 * :
 *   statement1;
 *   statement2;
 * ,
 */
public class Block extends Statement {

    private final ArrayList<Statement> statements = new ArrayList<>();
    private boolean isCurly;

    private Block(int start, int end, Statement[] statements, boolean isCurly) {
        super(start, end);
        this.isCurly = isCurly;
        this.statements.addAll(Arrays.asList(statements));
    }

    public Block(int start, int end, List<Statement> statements, boolean isCurly) {
        this(start, end, statements == null ? new Statement[0] : (Statement[]) statements.toArray(new Statement[0]), isCurly);
    }

    public Block(int start, int end, List<Statement> statements) {
        this(start, end, statements, true);
    }

    public boolean isCurly() {
        return isCurly;
    }

    /**
     * Retrieves the statement parts of this block
     * @return statement parts of this block
     */
    public List<Statement> getStatements() {
        return this.statements;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Statement statement : getStatements()) {
            sb.append(statement).append(" "); //NOI18N
        }
        return isCurly() ? "{" + sb + "}" : sb.toString(); //NOI18N
    }

}

