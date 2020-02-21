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


import java.io.IOException;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;


import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * CsmReturnStatement implementation
 */
public final class ReturnStatementImpl extends StatementBase implements CsmReturnStatement {
    
    private ExpressionBase returnExpr;

    private ReturnStatementImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file, scope);
    }
    
    private ReturnStatementImpl(CsmScope scope, CsmFile file, int start, int end) {
        super(file, start, end, scope);
    }    

    public static ReturnStatementImpl create(AST ast, CsmFile file, CsmScope scope) {
        ReturnStatementImpl result = new ReturnStatementImpl(ast, file, scope);
        AST returnExprAST = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_EXPRESSION);
        
        boolean shouldCreateReturnExpression = false;
                
        if (returnExprAST != null) {
            // Lambda function
            shouldCreateReturnExpression |= AstUtil.findChildOfType(returnExprAST, CPPTokenTypes.CSM_DECLARATION_STATEMENT) != null;
            
            if (!shouldCreateReturnExpression) {
                CsmScope current = scope;
                while (CsmKindUtilities.isScopeElement(current) && !CsmKindUtilities.isFunction(current)) {
                    current = ((CsmScopeElement) current).getScope();
                }
                if (CsmKindUtilities.isFunction(current)) {
                    CsmFunction func = (CsmFunction) current;
                    if (CsmUtilities.isAutoType(func.getReturnType())) {
                        shouldCreateReturnExpression = true;
                    } else if (CsmUtilities.isDecltypeAutoType(func.getReturnType())) {
                        shouldCreateReturnExpression = true;
                    }
                }
            }
            
            // TODO: check if scope is a function and it is annotated with constexpr.
            // In such case we should store return expression too.            
        }
        
        if (shouldCreateReturnExpression) {
            result.returnExpr = ExpressionsFactory.create(returnExprAST, file, scope);
        }
        return result;
    }
    
    @Override
    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.RETURN;
    } 

    @Override
    public CsmExpression getReturnExpression() {
        return returnExpr;
    }
    
    public static class ReturnStatementBuilder extends StatementBuilder {

        @Override
        public ReturnStatementImpl create() {
            ReturnStatementImpl stmt = new ReturnStatementImpl(getScope(), getFile(), getStartOffset(), getEndOffset());
            return stmt;
        }
    }       
   
    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeExpression(returnExpr, output);
    }

    public ReturnStatementImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.returnExpr = (ExpressionBase) PersistentUtils.readExpression(input);
    }      
}
