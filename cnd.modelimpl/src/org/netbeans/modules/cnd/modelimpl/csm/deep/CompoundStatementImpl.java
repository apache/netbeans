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
import java.io.IOException;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * Common ancestor for all ... statements
 */
public class CompoundStatementImpl extends StatementBase implements CsmCompoundStatement {

    private volatile List<CsmStatement> statements;

    protected CompoundStatementImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file, scope);
    }

    protected CompoundStatementImpl(CsmScope scope, CsmFile file, int start, int end) {
        super(file, start, end, scope);        
    }
    
    public static CompoundStatementImpl create(AST ast, CsmFile file, CsmScope scope) {
        CompoundStatementImpl stmt = new CompoundStatementImpl(ast, file, scope);
        stmt.init(ast);
        return stmt;
    }

    private void init(AST ast) {
        renderStatements(ast);
    }
    
    @Override
    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.COMPOUND;
    }

    @Override
    public final List<CsmStatement> getStatements() {
        return statements;
    }

    public void setStatements(List<CsmStatement> statements) {
        this.statements = statements;
    }
    
    @Override
    public void dispose() {
        super.dispose();
        if (statements != null) {
            Utils.disposeAll(statements);
        }
    }

    protected void renderStatements(AST ast) {
        // to prevent re-rendering recursions initialize with empty list
        // and replace with real content at the end
        // we do not use extra sync here, because it's ok in case of real 
        // call of this functions from different threads to render twice;
        // key point is non-null value of 'statements' field to prevent recursions
        statements = Collections.emptyList();
        List<CsmStatement> out = new ArrayList<>();
        if (ast != null) {
            for (AST token = ast.getFirstChild(); token != null; token = token.getNextSibling()) {
                CsmStatement stmt = AstRenderer.renderStatement(token, getContainingFile(), this);
                if (stmt != null) {
                    out.add(stmt);
                }
            }
        }
        statements = out;
    }

    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        @SuppressWarnings("unchecked")
        Collection<CsmScopeElement> out = (Collection<CsmScopeElement>) (List<?>) getStatements();
        return out;
    }

    public static class CompoundStatementBuilder extends StatementBuilder implements StatementBuilderContainer {

        private final List<StatementBuilder> statements = new ArrayList<>();
        
        @Override
        public void addStatementBuilder(StatementBuilder statement) {
            statements.add(statement);
        }

        protected List<StatementBuilder> getStatements() {
            return statements;
        }
        
        @Override
        public CompoundStatementImpl create() {
            CompoundStatementImpl stmt = new CompoundStatementImpl(getScope(), getFile(), getStartOffset(), getEndOffset());
            List<CsmStatement> stmts = new ArrayList<>();
            for (StatementBuilder statementBuilder : statements) {
                statementBuilder.setScope(stmt);
                stmts.add(statementBuilder.create());
            }
            if(stmts.isEmpty()) {
                stmt.statements = Collections.<CsmStatement>emptyList();
            } else {
                stmt.statements = stmts;
            }
            return stmt;
        }
    }      
    
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        // HAVE TO BE ONLY DELEGATION INTO SUPER
        // because it is deserialized as lazy
        super.write(output);
    }

    public CompoundStatementImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.statements = null;
    }
}
