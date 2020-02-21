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

import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;

import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.openide.util.CharSequences;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;

/**
 * CsmEnumerator implementation
 */
public final class EnumeratorImpl extends OffsetableDeclarationBase<CsmEnumerator> implements CsmEnumerator {
    private final CharSequence name;
    
    // only one of enumerationRef/enumerationUID must be used (USE_UID_TO_CONTAINER)    
    private /*final*/ CsmEnum enumerationRef;// can be set in onDispose or contstructor only
    private final CsmUID<CsmEnum> enumerationUID;

    private EnumeratorImpl(CsmFile file, AST ast, NameHolder name, EnumImpl enumeration) {
        super(file, getStartOffset(ast), getEndOffset(ast));
        assert enumeration != null;
        this.name = NameCache.getManager().getString(name.getName());
        // set parent enum, do it in constructor to have final fields
        this.enumerationUID = UIDCsmConverter.declarationToUID((CsmEnum)enumeration);
        assert this.enumerationUID != null;
        this.enumerationRef = null;
    }

    public static EnumeratorImpl create(AST ast, final CsmFile file, FileContent fileContent, EnumImpl enumeration, boolean global) {
        NameHolder holder = NameHolder.createSimpleName(ast);
        EnumeratorImpl ei = new EnumeratorImpl(file, ast, holder, enumeration);
        postObjectCreateRegistration(global, ei);
        holder.addReference(fileContent, ei);
        return ei;
    }

    private EnumeratorImpl(EnumImpl enumeration, CharSequence name, CsmFile file, int startOffset, int endOffset) {
        super(file, startOffset, endOffset);
        assert enumeration != null;
        this.name = NameCache.getManager().getString(name);
        // set parent enum, do it in constructor to have final fields
        this.enumerationUID = UIDCsmConverter.declarationToUID((CsmEnum)enumeration);
        assert this.enumerationUID != null;
        this.enumerationRef = null;
    }
    
    public static EnumeratorImpl create(EnumImpl enumeration, String name, int startOffset, int endOffset, boolean global) {
        EnumeratorImpl ei = new EnumeratorImpl(enumeration, name, enumeration.getContainingFile(), startOffset, endOffset);
        postObjectCreateRegistration(global, ei);
        return ei;
    }

    @Override
    public CharSequence getName() {
        return name;
    }

    @Override
    public CsmExpression getExplicitValue() {
        return null;
    }

    @Override
    public CsmEnum getEnumeration() {
        return _getEnumeration();
    }
    
    @Override
    public CsmScope getScope() {
        return getEnumeration();
    }

    @Override
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.ENUMERATOR;
    }

    @Override
    public CharSequence getQualifiedName() {
        CsmEnum e = _getEnumeration();
        if (e == null) {
            return CharSequences.create(getQualifiedNamePostfix());
        } else {
            return CharSequences.create(CharSequenceUtils.concatenate(e.getQualifiedName(), "::", getQualifiedNamePostfix())); // NOI18N
        }
    }

    private synchronized CsmEnum _getEnumeration() {
        CsmEnum enumeration = this.enumerationRef;
        if (enumeration == null) {
            enumeration = UIDCsmConverter.UIDtoDeclaration(this.enumerationUID);
            assert (enumeration != null || this.enumerationUID == null) : "null object for UID " + this.enumerationUID;
        }
        return enumeration;
    }    

    @Override
    public void dispose() {
        super.dispose();
        onDispose();
    } 
    
    private synchronized void onDispose() {
        if (enumerationRef == null) {
            // restore container from it's UID
            this.enumerationRef = UIDCsmConverter.UIDtoDeclaration(this.enumerationUID);
            assert this.enumerationRef != null || this.enumerationUID == null : "no object for UID " + this.enumerationUID;
        }
    }    
    
    public static class EnumeratorBuilder {
        
        private CharSequence name;
        private CsmFile file;
        private int startOffset;
        private int endOffset;
        private EnumImpl enumeration;
        private final FileContent fileContent;
        public EnumeratorBuilder(FileContent fileContent) {
            this.fileContent = fileContent;
        }

        public void setName(CharSequence name) {
            this.name = name;
        }

        public void setFile(CsmFile file) {
            this.file = file;
        }
        
        public void setEndOffset(int endOffset) {
            this.endOffset = endOffset;
        }

        public void setStartOffset(int startOffset) {
            this.startOffset = startOffset;
        }

        public void setEnum(EnumImpl enumeration) {
            this.enumeration = enumeration;
        }
        
        public EnumeratorImpl create(boolean register) {
            if(name != null) {
                NameHolder nameHolder = NameHolder.createName(name, startOffset, endOffset);
                EnumeratorImpl impl = new EnumeratorImpl(enumeration, name, file, startOffset, endOffset);
                postObjectCreateRegistration(register, impl);
                nameHolder.addReference(fileContent, impl);
                return impl;
            }
            return null;
        }
    
    }    
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        PersistentUtils.writeUTF(name, output);
    
        // not null UID
        assert this.enumerationUID != null;
        UIDObjectFactory.getDefaultFactory().writeUID(this.enumerationUID, output);
    }
    
    public EnumeratorImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.name = PersistentUtils.readUTF(input, NameCache.getManager());
        assert this.name != null;
        this.enumerationUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        // not null UID
        assert this.enumerationUID != null;
        this.enumerationRef = null;
    }
}
