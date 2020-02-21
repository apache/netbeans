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
 * CsmSwitchStatement implementation
 */
public final class SwitchStatementImpl extends StatementBase implements CsmSwitchStatement {

    private CsmCondition condition;
    private StatementBase body;

    private SwitchStatementImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file, scope);
    }

    private SwitchStatementImpl(CsmScope scope, CsmFile file, int start, int end) {
        super(file, start, end, scope);
    }

    public static SwitchStatementImpl create(AST ast, CsmFile file, CsmScope scope) {
        SwitchStatementImpl stmt = new SwitchStatementImpl(ast, file, scope);
        stmt.init(ast);
        return stmt;
    }

    private void init(AST ast) {
        AST token = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_CONDITION);
        if( token != null ) {
            condition = new AstRenderer((FileImpl) getContainingFile()).renderCondition(token, this);
        }

        for( AST token2 = ast.getFirstChild(); token2 != null; token2 = token2.getNextSibling() ) {
            if( AstRenderer.isStatement(token2) ) {
                body = AstRenderer.renderStatement(token2, getContainingFile(), this);
                break;
            }
        }
    }

    @Override
    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.SWITCH;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (condition instanceof Disposable) {
            ((Disposable)condition).dispose();
        }
        if (body != null) {
            body.dispose();
        }
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
    public Collection<CsmScopeElement> getScopeElements() {
        return DeepUtil.merge(getCondition(), getBody());
    }

    public static class SwitchStatementBuilder extends StatementBuilder implements StatementBuilderContainer {

        ConditionExpressionBuilder conditionExpression;
        ConditionDeclarationBuilder conditionDeclaration;
        StatementBuilder body;

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
        public SwitchStatementImpl create() {
            SwitchStatementImpl stmt = new SwitchStatementImpl(getScope(), getFile(), getStartOffset(), getEndOffset());
            if (body != null) {
                body.setScope(stmt);
                stmt.body = body.create();
            }
            if(conditionDeclaration != null) {
                conditionDeclaration.setScope(stmt);
                stmt.condition = conditionDeclaration.create();
            } else if (conditionExpression != null) {
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
