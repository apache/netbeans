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


package org.netbeans.modules.cnd.modelimpl.fsm;

import org.netbeans.modules.cnd.modelimpl.csm.*;
import org.netbeans.modules.cnd.api.model.*;
import java.io.IOException;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * Implements CsmParameter
 */
public final class DummyParameterImpl extends VariableImpl<CsmParameter> implements CsmParameter {

    private DummyParameterImpl(CsmFile file, int startOffset, int endOffset, String name, CsmType type, CsmScope scope) {
        super(file, startOffset, endOffset, type, name, scope, false, false);
    }

    public static DummyParameterImpl create(CsmFile file, int startOffset, int endOffset, String name, CsmScope scope) {
        DummyParameterImpl dummyParameterImpl = new DummyParameterImpl(
                file, 
                startOffset, 
                endOffset, 
                name, 
                TypeFactory.createBuiltinType("int", null, 0,  null/*getAst().getFirstChild()*/, file), // NOI18N
                scope
        );
        
        postObjectCreateRegistration(false, dummyParameterImpl);
        
        return dummyParameterImpl;
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

    @Override
    public CharSequence getDisplayText() {
        return super.getName();
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

    public DummyParameterImpl(RepositoryDataInput input, CsmScope scope) throws IOException {
        super(input, scope);
    } 
}
