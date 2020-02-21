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
