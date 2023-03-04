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
package org.netbeans.modules.php.editor.model.nodes;

import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;

/**
 * @author Radek Matous
 */
public class NamespaceDeclarationInfo extends ASTNodeInfo<NamespaceDeclaration> {
    public static final String NAMESPACE_SEPARATOR = "\\"; //NOI18N
    public static final String DEFAULT_NAMESPACE_NAME = ""; //NOI18N

    NamespaceDeclarationInfo(NamespaceDeclaration node) {
        super(node);
    }

    public static NamespaceDeclarationInfo create(NamespaceDeclaration node) {
        return new NamespaceDeclarationInfo(node);
    }

    public boolean isDefaultNamespace() {
        return DEFAULT_NAMESPACE_NAME.equals(getName());
    }

    @Override
    public Kind getKind() {
        return Kind.NAMESPACE_DECLARATION;
    }

    @Override
    public String getName() {
        StringBuilder sb = new StringBuilder();
        NamespaceDeclaration node = getOriginalNode();
        final NamespaceName nameSpaceName = node.getName();
        if (nameSpaceName != null) {
            for (Identifier identifier : nameSpaceName.getSegments()) {
                if (sb.length() > 0) {
                    sb.append(NAMESPACE_SEPARATOR);
                }
                sb.append(identifier.getName());
            }
        } else {
            sb.append(DEFAULT_NAMESPACE_NAME);
        }
        return sb.toString();
    }

    @Override
    public QualifiedName getQualifiedName() {
        return QualifiedName.create(getName());
    }

    @Override
    public OffsetRange getRange() {
        ASTNode node = getOriginalNode();
        final NamespaceName name = ((NamespaceDeclaration) node).getName();
        if (name != null) {
            node = name;
        }
        return new OffsetRange(node.getStartOffset(), node.getEndOffset());
    }

    @Override
    public PhpElementKind getPhpElementKind() {
        return PhpElementKind.NAMESPACE_DECLARATION;
    }

}
