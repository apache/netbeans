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
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase.ScopedDeclarationBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase.ExpressionBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase.ExpressionBuilderContainer;

/**
 * Implements condition of kind CsmCondition.Kind.EXPRESSION
 */
public final class ConditionExpressionImpl extends OffsetableBase implements CsmCondition {
    
    private final CsmExpression expression;
    
    private ConditionExpressionImpl(AST ast, CsmFile file, CsmScope scope) {
        super(file, getStartOffset(ast), getEndOffset(ast));
        expression = new AstRenderer((FileImpl)getContainingFile()).renderExpression(ast, scope);
    }

    private ConditionExpressionImpl(CsmExpression expression, CsmScope scope, CsmFile file, int start, int end) {
        super(file, start, end);
        this.expression = expression;
    }
    
    public static ConditionExpressionImpl create(AST ast, CsmFile file, CsmScope scope) {
        return new ConditionExpressionImpl(ast, file, scope);
    }

    @Override
    public CsmCondition.Kind getKind() {
        return CsmCondition.Kind.EXPRESSION;
    }

    @Override
    public CsmVariable getDeclaration() {
        return null;
    }

    @Override
    public CsmExpression getExpression() {
        return expression;
    }

    @Override
    public CsmScope getScope() {
        return expression.getScope();
    }
    
    public static class ConditionExpressionBuilder extends ScopedDeclarationBuilder implements ExpressionBuilderContainer {

        private ExpressionBuilder expressionBuilder;

        @Override
        public void addExpressionBuilder(ExpressionBuilder expression) {
            this.expressionBuilder = expression;
        }
        
        public ConditionExpressionImpl create() {
            ExpressionBase expression = null;
            if(expressionBuilder != null) {
                expressionBuilder.setScope(getScope());
                expression = expressionBuilder.create();
            }
            ConditionExpressionImpl expr = new ConditionExpressionImpl(expression, getScope(), getFile(), getStartOffset(), getEndOffset());
            return expr;
        }
    }         
    
    
}
