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

import org.netbeans.modules.cnd.modelimpl.csm.core.*;

import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase.ExpressionBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase.ExpressionBuilderContainer;

/**
 *
 */
public final class CaseStatementImpl extends StatementBase implements CsmCaseStatement {
    
    private CsmExpression expression;
    
    private CaseStatementImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file, scope);
    }

    private CaseStatementImpl(CsmExpression expression, CsmScope scope, CsmFile file, int start, int end) {
        super(file, start, end, scope);
        this.expression = expression;
    }
    
    public static CaseStatementImpl create(AST ast, CsmFile file, CsmScope scope) {
        CaseStatementImpl stmt = new CaseStatementImpl(ast, file, scope);
        stmt.init(ast);
        return stmt;
    }

    protected void init(AST ast) {
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            if( AstRenderer.isExpression(token) ) {
                expression = new AstRenderer((FileImpl) getContainingFile()).renderExpression(token, getScope());
                break;
            }
        }
    }
    
    @Override
    public CsmExpression getExpression() {
        return expression;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (expression instanceof Disposable) {
            ((Disposable)expression).dispose();
        }
    }

    @Override
    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.CASE;
    }
    
    public static class CaseStatementBuilder extends StatementBuilder implements ExpressionBuilderContainer {

        private ExpressionBuilder expressionBuilder;

        @Override
        public void addExpressionBuilder(ExpressionBuilder expression) {
            this.expressionBuilder = expression;
        }
        
        @Override
        public CaseStatementImpl create() {
            ExpressionBase expression = null;
            if(expressionBuilder != null) {
                expressionBuilder.setScope(getScope());
                expression = expressionBuilder.create();
            }
            CaseStatementImpl expr = new CaseStatementImpl(expression, getScope(), getFile(), getStartOffset(), getEndOffset());
            return expr;
        }
    }
}

