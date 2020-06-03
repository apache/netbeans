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
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExceptionHandlerImpl.ExceptionHandlerBuilder;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

/**
 * CsmTryCatchStatement implementation
 */
public final class TryCatchStatementImpl extends StatementBase implements CsmTryCatchStatement, CsmScope {

    private StatementBase tryStatement;
    private List<CsmExceptionHandler> handlers;

    private TryCatchStatementImpl(AST ast, CsmFile file, CsmScope scope, boolean global) {
        super(ast, file, scope);
        render(ast, global);
    }

    private TryCatchStatementImpl(CsmScope scope, CsmFile file, int start, int end) {
        super(file, start, end, scope);
    }

    public static TryCatchStatementImpl create(AST ast, CsmFile file, CsmScope scope, boolean global) {
        return new TryCatchStatementImpl(ast, file, scope, global);
    }

    @Override
    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.TRY_CATCH;
    }

    @Override
    public CsmStatement getTryStatement() {
        return tryStatement;
    }

    @Override
    public List<CsmExceptionHandler> getHandlers() {
        return handlers;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (tryStatement != null) {
            tryStatement.dispose();
        }
        if (handlers != null) {
            Utils.disposeAll(handlers);
        }
    }

    private void render(AST ast, boolean global) {
        handlers = new ArrayList<>();
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.CSM_COMPOUND_STATEMENT:
                    tryStatement = AstRenderer.renderStatement(token, getContainingFile(), this);
                    break;
                case CPPTokenTypes.CSM_CATCH_CLAUSE:
                    handlers.add(ExceptionHandlerImpl.create(token, getContainingFile(), this, global));
                    break;
            }
        }
    }

    @Override
    public Collection<CsmScopeElement> getScopeElements() {
	Collection<CsmScopeElement> elements = new ArrayList<>();
        if (tryStatement != null) {
            elements.add(tryStatement);
        }
        if (handlers != null) {
            elements.addAll(handlers);
        }
	return elements;
    }

    public static class TryCatchStatementBuilder extends StatementBuilder implements StatementBuilderContainer {

        private final List<ExceptionHandlerBuilder> handlers = new ArrayList<>();
        private StatementBuilder tryStatement;

        public void addHandlerBuilder(ExceptionHandlerBuilder statement) {
            handlers.add(statement);
        }

        public void setTryStatementBuilder(StatementBuilder tryStatement) {
            this.tryStatement = tryStatement;
        }

        @Override
        public TryCatchStatementImpl create() {
            TryCatchStatementImpl stmt = new TryCatchStatementImpl(getScope(), getFile(), getStartOffset(), getEndOffset());
            List<CsmExceptionHandler> stmts = new ArrayList<>();
            for (ExceptionHandlerBuilder statementBuilder : handlers) {
                statementBuilder.setScope(stmt);
                stmts.add(statementBuilder.create());
            }
            if(stmts.isEmpty()) {
                stmt.handlers = Collections.<CsmExceptionHandler>emptyList();
            } else {
                stmt.handlers = stmts;
            }

            if(tryStatement != null) {
                tryStatement.setScope(stmt);
                stmt.tryStatement = tryStatement.create();
            }

            return stmt;
        }

        @Override
        public void addStatementBuilder(StatementBuilder builder) {
            tryStatement = builder;
        }
    }

}
