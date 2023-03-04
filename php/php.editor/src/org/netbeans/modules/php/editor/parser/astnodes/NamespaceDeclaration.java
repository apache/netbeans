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
