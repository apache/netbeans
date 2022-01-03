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
