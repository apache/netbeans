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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.parser.astnodes;

import java.util.ArrayList;

/**
 * Represents namespace declaration:
 * <pre>e.g.<pre>namespace MyNamespace;
 *namespace MyProject\Sub\Level;
 */
public class NamespaceDeclaration extends Statement {

    private NamespaceName name;
    private Block body;
    private boolean bracketed = true;

    public NamespaceDeclaration(int start, int end, NamespaceName name, Block body, boolean bracketed) {
        super(start, end);

        if (!bracketed && name == null) {
            throw new IllegalArgumentException("Not bracketed statement must contain namespace name");
        }

        this.bracketed = bracketed;

        if (body == null) {
            body = new Block(end, end, new ArrayList());
        }

        this.name = name;
        this.body = body;
    }

    /**
     * Returns whether this namespace declaration has a bracketed syntax
     * @return
     */
    public boolean isBracketed() {
        return bracketed;
    }

    public void addStatement(Statement statement) {
        body.getStatements().add(statement);

        int statementEnd = statement.getEndOffset();
        int bodyStart = body.getStartOffset();
        body.setSourceRange(bodyStart, statementEnd);

        int namespaceStart = getStartOffset();
        setSourceRange(namespaceStart, statementEnd);
    }

    /**
     * The body component of this namespace declaration node
     * @return body component of this namespace declaration node
     */
    public Block getBody() {
        return body;
    }

    /**
     * The name component of this namespace declaration node
     * @return name component of this namespace declaration node
     */
    public NamespaceName getName() {
        return name;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "" + getName() + getBody(); //NOI18N
    }

}
