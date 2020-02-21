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
