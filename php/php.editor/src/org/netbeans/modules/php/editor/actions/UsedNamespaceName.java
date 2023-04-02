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
package org.netbeans.modules.php.editor.actions;

import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class UsedNamespaceName {
    private final ASTNode node;
    private final NamespaceScope inScope;
    private final QualifiedName qualifiedName;

    public UsedNamespaceName(NamespaceName node, NamespaceScope inScope) {
        this.node = node;
        this.inScope = inScope;
        this.qualifiedName = QualifiedName.create(node);
    }

    public UsedNamespaceName(PHPDocTypeNode node, NamespaceScope inScope) {
        this.node = node;
        this.inScope = inScope;
        this.qualifiedName = QualifiedName.create(node.getValue());
    }

    public int getOffset() {
        return node.getStartOffset();
    }

    public NamespaceScope getInScope() {
        return inScope;
    }

    public int getReplaceLength() {
        return qualifiedName.toString().length();
    }

    public String getReplaceName() {
        return qualifiedName.getName();
    }

    public String getName() {
        return qualifiedName.toString();
    }

    @Override
    public String toString() {
        return qualifiedName.toString();
    }

}
