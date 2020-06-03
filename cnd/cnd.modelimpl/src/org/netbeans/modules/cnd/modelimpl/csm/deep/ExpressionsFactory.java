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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

/**
 *
 */
public final class ExpressionsFactory {
    
    public static ExpressionBase create(AST ast, CsmFile file,/* CsmExpression parent,*/ CsmScope scope) {
        ExpressionBase expr;
        
        if (ExpandedExpressionBase.hasExpandedTokens(ast)) {
            expr = new ExpandedExpressionBase(ast, file, scope);
        } else {
            expr = new ExpressionBase(ast, file, scope);
        }
        if (ast != null) {
            AST token = ast.getFirstChild();
            List<CsmStatement> lambdas = new ArrayList<>();
            while (token != null) {
                if(token.getType() == CPPTokenTypes.CSM_DECLARATION_STATEMENT) {
                    lambdas.add(AstRenderer.renderStatement(token, file, scope));
                }
                token = token.getNextSibling();
            }
            if(!lambdas.isEmpty()) {
                expr.setLambdas(lambdas);
            }
        }
        return expr;
    }
    
    public static ExpressionBase create(int startOffset, int endOffset, CsmFile file,/* CsmExpression parent,*/ CsmScope scope) {
        return new ExpressionBase(startOffset, endOffset, file, scope);
    }    

    private ExpressionsFactory() {
        throw new AssertionError();
    }
}
