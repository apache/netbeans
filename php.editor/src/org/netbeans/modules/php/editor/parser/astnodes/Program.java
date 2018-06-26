/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
            this.comments.add((Comment) comment);
        }

    }

    public Program(int start, int end, List<Statement> statements, List<Comment> commentsList) {
        this(start, end, (Statement[]) statements.toArray(new Statement[statements.size()]), commentsList);
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
