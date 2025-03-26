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
 * The AST root node for PHP program (meaning a PHP file). The program holds
 * array of statements such as Class, Function and evaluation statement. The
 * program also holds the PHP file comments.
 *
 */
public class Program extends ASTNode {

    private final ArrayList<Statement> statements = new ArrayList<>();
    /**
     * Comments array of the php program
     */
    private final ArrayList<Comment> comments = new ArrayList<>();

    private Program(int start, int end, Statement[] statements, List<Comment> commentsList) {
        super(start, end);
        this.statements.addAll(Arrays.asList(statements));
        for (Comment comment : commentsList) {
            this.comments.add(comment);
        }

    }

    public Program(int start, int end, List<Statement> statements, List<Comment> commentsList) {
        this(start, end, statements.toArray(new Statement[0]), commentsList);
    }

    public List<Comment> getComments() {
        return comments;
    }

    /**
     * Retrieves the statement list of this program.
     *
     * @return statement parts of this program
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
        StringBuilder sbComments = new StringBuilder();
        for (Comment comment : getComments()) {
            sbComments.append(comment).append(" "); //NOI18N
        }
        StringBuilder sbStatements = new StringBuilder();
        for (Statement statement : getStatements()) {
            sbStatements.append(statement).append(" "); //NOI18N
        }
        return sbComments.toString() + " *** " + sbStatements.toString(); //NOI18N
    }
}
