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
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase.ExpressionBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase.ExpressionBuilderContainer;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

/**
 * Implements CsmForStatement statements
 */
public final class ForStatementImpl extends StatementBase implements CsmForStatement, CsmRangeForStatement {

    private StatementBase init;
    private CsmCondition condition;
    private ExpressionBase iteration;
    private StatementBase body;
    private boolean rangeBased = false;

    private ForStatementImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file, scope);
    }

    private ForStatementImpl(CsmScope scope, CsmFile file, int start, int end) {
        super(file, start, end, scope);
    }

    public static ForStatementImpl create(AST ast, CsmFile file, CsmScope scope) {
        ForStatementImpl stmt = new ForStatementImpl(ast, file, scope);
        stmt.init(ast);
        return stmt;
    }

    private void init(AST ast) {
        render(ast);
    }

    @Override
    public CsmStatement.Kind getKind() {
        return rangeBased ? CsmStatement.Kind.RANGE_FOR : CsmStatement.Kind.FOR;
    }

    @Override
    public boolean isPostCheck() {
        return true;
    }

    @Override
    public CsmExpression getIterationExpression() {
        return iteration;
    }

    @Override
    public CsmStatement getInitStatement() {
        return init;
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
    public void dispose() {
        super.dispose();
        if (condition instanceof Disposable) {
            ((Disposable) condition).dispose();
        }
        if (init != null) {
            init.dispose();
        }
        if (iteration != null) {
            iteration.dispose();
        }
        if (body instanceof Disposable) {
            ((Disposable) body).dispose();
        }
    }

    private void render(AST ast) {

        AstRenderer renderer = new AstRenderer((FileImpl) getContainingFile());

        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
            case CPPTokenTypes.CSM_FOR_INIT_STATEMENT:
                AST child = AstRenderer.getFirstChildSkipQualifiers(token);
                if( child != null ) {
                    switch( child.getType() ) {
                        case CPPTokenTypes.SEMICOLON:
                            init = null;
                            break;
                        case CPPTokenTypes.CSM_TYPE_BUILTIN:
                        case CPPTokenTypes.CSM_TYPE_ATOMIC:
                        case CPPTokenTypes.CSM_TYPE_COMPOUND:
                        case CPPTokenTypes.LITERAL_struct:
                        case CPPTokenTypes.LITERAL_class:
                        case CPPTokenTypes.LITERAL_union:
                        case CPPTokenTypes.LITERAL_enum:
                            //renderer.renderVariable(token, null, null);
                            init = DeclarationStatementImpl.create(token, getContainingFile(), ForStatementImpl.this);
                            break;
                        default:
                            if( AstRenderer.isExpression(child) ) {
                                init = ExpressionStatementImpl.create(token, getContainingFile(), ForStatementImpl.this);
                            }
                            break;
                    }
                }
                break;
            case CPPTokenTypes.CSM_CONDITION:
                condition = renderer.renderCondition(token, this);
                break;
            case CPPTokenTypes.COLON:
                rangeBased = true;
                break;
            default:
                if( AstRenderer.isStatement(token) ) {
                    body = AstRenderer.renderStatement(token, getContainingFile(), this);
                }
                else if( AstRenderer.isExpression(token) ) {
                    iteration = renderer.renderExpression(token, ForStatementImpl.this);
                }
            }
        }
    }

    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        //return DeepUtil.merge(getInitStatement(), getCondition(), getBody());
        List<CsmScopeElement> l = new ArrayList<>();
        CsmStatement stmt = getInitStatement();
        if( stmt != null ) {
            l.add(stmt);
        }
        CsmCondition cond = getCondition();
        if( cond != null ) {
            CsmVariable decl = cond.getDeclaration();
            if( decl != null ) {
                l.add(decl);
            }
        }
        stmt = getBody();
        if( stmt != null ) {
            l.add(stmt);
        }
        return l;
    }

    @Override
    public CsmDeclarationStatement getDeclaration() {
        if (init instanceof CsmDeclarationStatement) {
            assert rangeBased;
            return (CsmDeclarationStatement) init;
        } else {
            return null;
        }
    }

    @Override
    public CsmExpression getInitializer() {
        assert rangeBased;
        return iteration;
    }

    public static class ForStatementBuilder extends StatementBuilder implements StatementBuilderContainer, ExpressionBuilderContainer {

        ExpressionBuilder iteration;
        ConditionDeclarationBuilder conditionDeclaration;
        ConditionExpressionBuilder conditionExpression;
        StatementBuilder init;
        StatementBuilder body;
        boolean head = true;

        @Override
        public void addExpressionBuilder(ExpressionBuilder expression) {
            setIteration(expression);
        }

        public void setIteration(ExpressionBuilder iteration) {
            this.iteration = iteration;
        }

        public void setInit(StatementBuilder init) {
            this.init = init;
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

        public void body() {
            this.head = false;
        }

        @Override
        public ForStatementImpl create() {
            ForStatementImpl stmt = new ForStatementImpl(getScope(), getFile(), getStartOffset(), getEndOffset());
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
            if(iteration != null) {
                iteration.setScope(stmt);
                stmt.iteration = iteration.create();
            }
            if(init != null) {
                init.setScope(stmt);
                stmt.init = init.create();
            }

            return stmt;
        }

        @Override
        public void addStatementBuilder(StatementBuilder builder) {
            if(head) {
                init = builder;
            } else {
                body = builder;
            }
        }
    }

}
