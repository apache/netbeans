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


import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;


import org.netbeans.modules.cnd.antlr.collections.AST;

/**
 * For different kinds of statements, which doesn't return specific information - continue, break, etc
 */
public final class UniversalStatement extends StatementBase {
    
    private final CsmStatement.Kind kind;
    
    public UniversalStatement(AST ast, CsmFile file, CsmStatement.Kind kind, CsmScope scope) {
            super(ast, file, scope);
            this.kind = kind;
    }
    
    private UniversalStatement(CsmStatement.Kind kind, CsmScope scope, CsmFile file, int start, int end) {
        super(file, start, end, scope);        
        this.kind = kind;
    }        
    
    public static UniversalStatement create(AST ast, CsmFile file, CsmStatement.Kind kind, CsmScope scope) {
        return new UniversalStatement(ast, file, kind, scope);
    }
    
    @Override
    public CsmStatement.Kind getKind() {
        return kind;
    }
    
    public static class UniversalStatementBuilder extends StatementBuilder {

        private CsmStatement.Kind kind;

        public void setKind(Kind kind) {
            this.kind = kind;
        }
        
        @Override
        public UniversalStatement create() {
            UniversalStatement stmt = new UniversalStatement(kind, getScope(), getFile(), getStartOffset(), getEndOffset());
            return stmt;
        }
    }    
    
}
