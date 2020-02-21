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
