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

import java.io.IOException;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypeAlias;
import org.netbeans.modules.cnd.modelimpl.csm.core.Disposable;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 *
 */
public class TypeAliasImpl extends TypedefImpl implements CsmTypeAlias, CsmScopeElement, Disposable {
    
    public static TypeAliasImpl create(AST ast, CsmFile file, CsmObject container, CsmType type, CharSequence aName, boolean global) {
        TypeAliasImpl typeAliasImpl = new TypeAliasImpl(ast, file, container, type, aName);
        if (!global) {
            Utils.setSelfUID(typeAliasImpl);
        }
        return typeAliasImpl;
    }    
    

    protected TypeAliasImpl(AST ast, CsmFile file, CsmObject container, CsmType type, CharSequence aName) {
        super(ast, file, container, type, aName);
    }

    protected TypeAliasImpl(CsmType type, CharSequence name, CsmObject container, CsmFile file, int startOffset, int endOffset) {
        super(type, name, container, file, startOffset, endOffset);
    }   

    @Override
    public Kind getKind() {
        return CsmDeclaration.Kind.TYPEALIAS;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
    }
    
    public TypeAliasImpl(RepositoryDataInput input) throws IOException {
        super(input);
    }
}
