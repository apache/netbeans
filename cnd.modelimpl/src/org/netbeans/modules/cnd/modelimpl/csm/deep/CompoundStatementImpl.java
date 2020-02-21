/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
