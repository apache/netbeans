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

import org.netbeans.modules.cnd.modelimpl.csm.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.csm.ParameterImpl.ParameterBuilder;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

/**
 * Common ancestor for all ... statements
 */
public final class ExceptionHandlerImpl extends CompoundStatementImpl implements CsmExceptionHandler {

    private ParameterImpl parameter;

    public ExceptionHandlerImpl(AST ast,  CsmFile file, CsmScope scope, boolean global) {
        super(ast, file, scope);
    }

    protected ExceptionHandlerImpl(CsmScope scope, CsmFile file, int start, int end) {
        super(scope, file, start, end);
    }

    public static ExceptionHandlerImpl create(AST ast,  CsmFile file, CsmScope scope, boolean global) {
        ExceptionHandlerImpl stmt = new ExceptionHandlerImpl(ast, file, scope, global);
        stmt.init(ast);
        return stmt;
    }

    private void init(AST ast) {
        AST ast2 = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_PARAMETER_DECLARATION);
        if( ast2 != null ) {
            List<ParameterImpl> params = AstRenderer.renderParameter(ast2, getContainingFile(), null, this);
            if( params != null && ! params.isEmpty() ) {
                        parameter = params.get(0);
            }
        }

        AST ast3 = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_COMPOUND_STATEMENT);
        renderStatements(ast3);
    }

    @Override
    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.CATCH;
    }

    @Override
    public boolean isCatchAll() {
	CsmParameter aParameter = getParameter();
        return aParameter == null || aParameter.isVarArgs();
    }

    @Override
    public CsmParameter getParameter() {
        return parameter;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (parameter != null) {
            parameter.dispose();
        }
    }

    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        return DeepUtil.merge(getParameter(), getStatements());
    }

    public static class ExceptionHandlerBuilder extends CompoundStatementBuilder {

        ParameterBuilder parameter;

        @Override
        public ExceptionHandlerImpl create() {
            ExceptionHandlerImpl stmt = new ExceptionHandlerImpl(getScope(), getFile(), getStartOffset(), getEndOffset());
            List<CsmStatement> stmts = new ArrayList<>();
            for (StatementBuilder statementBuilder : getStatements()) {
                statementBuilder.setScope(stmt);
                stmts.add(statementBuilder.create());
            }
            if(stmts.isEmpty()) {
                stmt.setStatements(Collections.<CsmStatement>emptyList());
            } else {
                stmt.setStatements(stmts);
            }
            if(parameter != null) {
                parameter.setScope(stmt);
                stmt.parameter = parameter.create();
            }
            return stmt;
        }
    }
}
