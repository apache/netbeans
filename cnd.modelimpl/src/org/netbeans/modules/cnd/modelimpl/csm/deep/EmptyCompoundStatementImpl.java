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

import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * empty compound statement. Used for incorrect/uncompleted code 
 * to present i.e. functions body
 */
public final class EmptyCompoundStatementImpl extends StatementBase implements CsmCompoundStatement {
    
    private EmptyCompoundStatementImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file, scope);
        ast.setFirstChild(null);
    }

    public static EmptyCompoundStatementImpl create(AST ast, CsmFile file, CsmScope scope) {
        return new EmptyCompoundStatementImpl(ast, file, scope);
    }
    
    @Override
    public List<CsmStatement> getStatements() {
        return Collections.<CsmStatement>emptyList();
    }
    
    @Override
    public CsmStatement.Kind getKind() {
        return Kind.COMPOUND;
    }
    
    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        return Collections.<CsmScopeElement>emptyList();
    }
    
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
    }
    
    public EmptyCompoundStatementImpl(RepositoryDataInput input) throws IOException {
        super(input);
    }     
}
