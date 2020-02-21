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

import java.util.*;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;

import org.netbeans.modules.cnd.modelimpl.csm.core.*;

import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ConditionDeclarationImpl.ConditionDeclarationBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ConditionExpressionImpl.ConditionExpressionBuilder;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

/**
 * Common base for for/while statements implementation
 */
public final class LoopStatementImpl extends StatementBase implements CsmLoopStatement {
    
    private CsmCondition condition;
    private CsmStatement body;
    private final boolean postCheck;
    
    private LoopStatementImpl(AST ast, CsmFile file, boolean postCheck, CsmScope scope) {
        super(ast, file, scope);
        this.postCheck = postCheck;
    }
    
    private LoopStatementImpl(boolean postCheck, CsmScope scope, CsmFile file, int start, int end) {
        super(file, start, end, scope);
        this.postCheck = postCheck;
    }    

    public static LoopStatementImpl create(AST ast, CsmFile file, boolean postCheck, CsmScope scope) {
        LoopStatementImpl stmt = new LoopStatementImpl(ast, file, postCheck, scope);
        stmt.init(ast);
        return stmt;
    }
   
    private void init(AST ast) {
        render(ast);
    }
    
    @Override
    public CsmCondition getCondition() {
        return condition;
    }
    
    @Override
    public CsmStatement getBody() {
        return body;
    }

    @Override
    public boolean isPostCheck() {
        return postCheck;
    }

    @Override
    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.WHILE;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (condition instanceof Disposable) {
            ((Disposable) condition).dispose();
        }
        if (body instanceof Disposable) {
            ((Disposable) body).dispose();
        }
    }

    private void render(AST ast) {
        AstRenderer renderer = new AstRenderer((FileImpl) getContainingFile());
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            int type = token.getType();
            if( type == CPPTokenTypes.CSM_CONDITION ) {
                condition = renderer.renderCondition(token, this);
            }
            else if( AstRenderer.isExpression(type) ) {
                condition = ConditionExpressionImpl.create(token, getContainingFile(), this);
            }
            else if( AstRenderer.isStatement(type) ) {
                body = AstRenderer.renderStatement(token, getContainingFile(), this);
            }
        }
    }

    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        return DeepUtil.merge(getCondition(), getBody());
    }
 
    public static class LoopStatementBuilder extends StatementBuilder implements StatementBuilderContainer {

        boolean postCheck;
        ConditionExpressionBuilder conditionExpression;
        ConditionDeclarationBuilder conditionDeclaration;
        StatementBuilder body;

        public void setPostCheck() {
            this.postCheck = true;
        }

        public void setConditionExpression(ConditionExpressionBuilder conditionExpression) {
            this.conditionExpression = conditionExpression;
        }

        public void setConditionDeclaration(ConditionDeclarationBuilder conditionDeclaration) {
            this.conditionDeclaration = conditionDeclaration;
        }

        public void setBody(StatementBuilder body) {
            this.body = body;
        }
        
        @Override
        public LoopStatementImpl create() {
            LoopStatementImpl stmt = new LoopStatementImpl(postCheck, getScope(), getFile(), getStartOffset(), getEndOffset());
            if (body != null) {
                body.setScope(stmt);
                stmt.body = body.create();
            }
            if(conditionDeclaration != null) {
                conditionDeclaration.setScope(stmt);
                stmt.condition = conditionDeclaration.create();
            } else if(conditionExpression != null) {
                conditionExpression.setScope(stmt);
                stmt.condition = conditionExpression.create();
            }
            return stmt;
        }

        @Override
        public void addStatementBuilder(StatementBuilder builder) {
            body = builder;
        }
    }       
}
