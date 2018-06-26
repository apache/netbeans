/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
