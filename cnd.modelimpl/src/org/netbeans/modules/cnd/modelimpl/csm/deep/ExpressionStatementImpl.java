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


import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase.ExpressionBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase.ExpressionBuilderContainer;

/**
 * Implements CsmExpressionStatement
 */
public final class ExpressionStatementImpl extends StatementBase implements CsmExpressionStatement {
    
    private final CsmExpression expr;
    
    private ExpressionStatementImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file, scope);
        expr = ExpressionsFactory.create(ast.getFirstChild(), file, scope);
    }

    private ExpressionStatementImpl(CsmExpression expression, CsmScope scope, CsmFile file, int start, int end) {
        super(file, start, end, scope);
        this.expr = expression;
    }
    
    public static ExpressionStatementImpl create(AST ast, CsmFile file, CsmScope scope) {
        return new ExpressionStatementImpl(ast, file, scope);
    }
    
    @Override
    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.EXPRESSION;
    }

    @Override
    public CsmExpression getExpression() {
        return expr;
    }

    public static class ExpressionStatementBuilder extends StatementBuilder implements ExpressionBuilderContainer {

        private ExpressionBuilder expressionBuilder;

        @Override
        public void addExpressionBuilder(ExpressionBuilder expression) {
            this.expressionBuilder = expression;
        }
        
        @Override
        public ExpressionStatementImpl create() {
            ExpressionBase expression = null;
            if(expressionBuilder != null) {
                expressionBuilder.setScope(getScope());
                expression = expressionBuilder.create();
            }
            ExpressionStatementImpl stmt = new ExpressionStatementImpl(expression, getScope(), getFile(), getStartOffset(), getEndOffset());
            return stmt;
        }
    }
    
}
