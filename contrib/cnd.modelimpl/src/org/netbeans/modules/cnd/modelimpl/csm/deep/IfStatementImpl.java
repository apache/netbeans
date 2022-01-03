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
 * CsmIfStatement implementation
 */
public final class IfStatementImpl extends StatementBase implements CsmIfStatement {
    
    private CsmCondition condition;
    private CsmStatement thenStmt;
    private CsmStatement elseStmt;
    
    private IfStatementImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file, scope);
    }
    
    private IfStatementImpl(CsmScope scope, CsmFile file, int start, int end) {
        super(file, start, end, scope);
    }    

    public static IfStatementImpl create(AST ast, CsmFile file, CsmScope scope) {
        IfStatementImpl stmt = new IfStatementImpl(ast, file, scope);
        stmt.init(ast);
        return stmt;
    }
    
    private void init(AST ast) {
        render(ast);
    }
    
    @Override
    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.IF;
    }

    @Override
    public CsmCondition getCondition() {
        return condition;
    }

    @Override
    public CsmStatement getThen() {
        return thenStmt;
    }

    @Override
    public CsmStatement getElse() {
        return elseStmt;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (condition instanceof Disposable) {
            ((Disposable) condition).dispose();
        }
        if (thenStmt instanceof Disposable) {
            ((Disposable) thenStmt).dispose();
        }
        if (elseStmt instanceof Disposable) {
            ((Disposable) elseStmt).dispose();
        }
    }

    private void render(AST ast) {
        AstRenderer renderer = new AstRenderer((FileImpl) getContainingFile());
        boolean inElse = false;
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.CSM_CONDITION:
                    condition = renderer.renderCondition(token, this);
                    break;
                case CPPTokenTypes.LITERAL_else:
                    inElse = true;
                    break;
                default:
                    //if( AstRenderer.isStatement(token) ) {
                    CsmStatement stmt = AstRenderer.renderStatement(token, getContainingFile(), this);
                    if( stmt != null ) {
                        if( inElse ) {
                            elseStmt = stmt;
                        }
                        else {
                            thenStmt = stmt;
                        }
                    }
                    //}
            }
        }
    }

    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        return DeepUtil.merge(getCondition(), getThen(), getElse());
    }
    
    
    public static class IfStatementBuilder extends StatementBuilder implements StatementBuilderContainer {

        ConditionDeclarationBuilder conditionDeclaration;
        ConditionExpressionBuilder conditionExpression;
        StatementBuilder thenStatement;
        StatementBuilder elseStatement;

        public void setThenStatement(StatementBuilder thenStatement) {
            this.thenStatement = thenStatement;
        }

        public void setElseStatement(StatementBuilder elseStatement) {
            this.elseStatement = elseStatement;
        }

        public void setConditionExpression(ConditionExpressionBuilder conditionExpression) {
            this.conditionExpression = conditionExpression;
        }

        public void setConditionDeclaration(ConditionDeclarationBuilder conditionDeclaration) {
            this.conditionDeclaration = conditionDeclaration;
        }

        @Override
        public IfStatementImpl create() {
            IfStatementImpl stmt = new IfStatementImpl(getScope(), getFile(), getStartOffset(), getEndOffset());
            if (thenStatement != null) {
                thenStatement.setScope(stmt);
                stmt.thenStmt = thenStatement.create();
            }
            if(conditionDeclaration != null) {
                conditionDeclaration.setScope(stmt);
                stmt.condition = conditionDeclaration.create();
            } else if(conditionExpression != null) {
                conditionExpression.setScope(stmt);
                stmt.condition = conditionExpression.create();
            }
            if(elseStatement != null) {
                elseStatement.setScope(stmt);
                stmt.elseStmt = elseStatement.create();
            }
            return stmt;
        }

        @Override
        public void addStatementBuilder(StatementBuilder builder) {
            if(thenStatement == null) {
                thenStatement = builder;
            } else {
                elseStatement = builder;
            }
        }
    }  
    
}
