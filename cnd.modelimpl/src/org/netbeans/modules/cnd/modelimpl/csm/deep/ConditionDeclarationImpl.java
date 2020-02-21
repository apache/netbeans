/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.modelimpl.csm.deep;


import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;

import org.netbeans.modules.cnd.modelimpl.csm.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.csm.VariableImpl.VariableBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase.ScopedDeclarationBuilder;

/**
 * Implements condition of kind CsmCondition.Kind.DECLARATION
 */
public final class ConditionDeclarationImpl extends OffsetableBase implements CsmCondition {
    
    private VariableImpl<?> declaration;
    
    private ConditionDeclarationImpl(AST ast, CsmFile file, CsmScope scope) {
        super(file, getStartOffset(ast), getEndOffset(ast));
        initDeclaration(ast, scope);
    }

    private ConditionDeclarationImpl(VariableImpl<?> declaration, CsmScope scope, CsmFile file, int start, int end) {
        super(file, start, end);
        this.declaration = declaration;
    }
    
    public static ConditionDeclarationImpl create(AST ast, CsmFile file, CsmScope scope) {
        return new ConditionDeclarationImpl(ast, file, scope);
    }

    @Override
    public CsmCondition.Kind getKind() {
        return CsmCondition.Kind.DECLARATION;
    }
    
    
    private void initDeclaration(AST node, final CsmScope scope) {
        AstRenderer renderer = new AstRenderer((FileImpl) getContainingFile()) {
            @Override
            protected VariableImpl createVariable(AST offsetAst, AST templateAst, CsmFile file, CsmType type, NameHolder name, boolean _static, boolean _extern,
		    MutableDeclarationsContainer container1, MutableDeclarationsContainer container2, CsmScope passedScope) {
		
                ConditionDeclarationImpl.this.declaration = super.createVariable(offsetAst, templateAst,
                        file, type, name, _static, _extern,
			container1, container2, scope);
                return declaration;
            }

            @Override
            protected boolean isRenderingLocalContext() {
                return true;
            }

        };
        renderer.renderVariable(node, null, null, null, false);
    }

    @Override
    public CsmVariable getDeclaration() {
        return declaration;
    }

    @Override
    public CsmExpression getExpression() {
        return null;
    }

    @Override
    public CsmScope getScope() {
        return (declaration == null) ? getContainingFile() : declaration.getScope();
    }
    
    public static class ConditionDeclarationBuilder extends ScopedDeclarationBuilder {

        VariableBuilder declaration;

        public void setDeclarationBuilder(VariableBuilder builder) {
            this.declaration = builder;
        }
        
        public ConditionDeclarationImpl create() {
            declaration.setScope(getScope());
            ConditionDeclarationImpl decl = new ConditionDeclarationImpl(declaration.create(), getScope(), getFile(), getStartOffset(), getEndOffset());
            return decl;
        }
    }         
    
}
