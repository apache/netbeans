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


package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * Implements CsmParameter
 */
public class ParameterImpl extends VariableImpl<CsmParameter> implements CsmParameter {

    protected ParameterImpl(AST ast, CsmFile file, CsmType type, NameHolder name, CsmScope scope) {
        super(ast, file, type, name, scope, false, false);
    }

    protected ParameterImpl(CsmType type, CharSequence name, CsmScope scope, ExpressionBase initExpr, CsmFile file, int startOffset, int endOffset) {
        super(type, name, scope, false, false, initExpr, file, startOffset, endOffset);
    }
    
    public static ParameterImpl create(AST ast, CsmFile file, CsmType type, NameHolder name, CsmScope scope) {
        ParameterImpl parameterImpl = new ParameterImpl(ast, file, type, name, scope);
        return parameterImpl;
    }

    @Override
    protected CsmUID<? extends CsmOffsetableDeclaration> createUID() {
        assert false;
        return super.createUID();
    }

    @Override
    protected boolean registerInProject() {
        return false;
    }

    @Override
    protected boolean unregisterInProject() {
        return false;
    }

    @Override
    public boolean isVarArgs() {
        return false;
    }
    
    
    public static class ParameterBuilder extends SimpleDeclarationBuilder {

        @Override
        public ParameterImpl create() {
            ParameterImpl param = new ParameterImpl(getType(), getName(), getScope(), null, getFile(), getStartOffset(), getEndOffset());
            return param;
        }
    }      
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);      
    }

    @Override
    protected boolean isScopePersistent() {
        return false;
    }

    public ParameterImpl(RepositoryDataInput input, CsmScope scope) throws IOException {
        super(input, scope);
    } 
}
