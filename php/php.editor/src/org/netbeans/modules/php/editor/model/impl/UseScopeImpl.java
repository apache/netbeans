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
package org.netbeans.modules.php.editor.model.impl;

import java.util.Collection;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.AliasedName;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.UseAliasElement;
import org.netbeans.modules.php.editor.model.UseScope;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.model.nodes.SingleUseStatementPartInfo;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;

class UseScopeImpl extends ScopeImpl implements UseScope {

    private final boolean partOfGroupUse;
    private AliasedName aliasName;
    private final UseScope.Type type;

    UseScopeImpl(NamespaceScopeImpl inScope, SingleUseStatementPartInfo nodeInfo) {
        super(inScope, nodeInfo.getName(), inScope.getFile(), nodeInfo.getRange(), PhpElementKind.USE_STATEMENT, false);
        final Identifier alias = nodeInfo.getOriginalNode().getAlias();
        this.aliasName = alias != null ? new AliasedName(alias.getName(), QualifiedName.create(getName())) : null;
        AliasedName aliasedName = null;
        if (alias != null) {
            aliasedName = new AliasedName(alias.getName(), QualifiedName.create(getName()));
            ASTNodeInfo<Expression> aliasNodeInfo = ASTNodeInfo.create(ASTNodeInfo.Kind.USE_ALIAS, alias);
            new UseAliasElementImpl(this, aliasNodeInfo);
        }
        this.aliasName = aliasedName;
        type = CodeUtils.mapType(nodeInfo.getType());
        partOfGroupUse = nodeInfo.isPartOfGroupUse();
    }

    @Override
    public AliasedName getAliasedName() {
        return aliasName;
    }

    @Override
    public UseScope.Type getType() {
        return type;
    }

    public boolean isPartOfGroupUse() {
        return partOfGroupUse;
    }

    @CheckForNull
    @Override
    public UseAliasElement getAliasElement() {
        Collection<UseAliasElement> filteredElements = filter(getElements(), new ElementFilter() {

            @Override
            public boolean isAccepted(ModelElement element) {
                return (element instanceof UseAliasElement);
            }
        });
        return filteredElements.size() > 0 ? filteredElements.iterator().next() : null;
    }
}
