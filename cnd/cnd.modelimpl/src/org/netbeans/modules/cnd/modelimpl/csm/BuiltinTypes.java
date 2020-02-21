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

import org.netbeans.modules.cnd.modelimpl.csm.core.CsmIdentifiable;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelimpl.accessors.CsmPackageAccessor;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.ObjectBasedUID;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.CharSequences;

/**
 * Implementation for built-in types
 */
public class BuiltinTypes {
    static {
        CsmPackageAccessor.register(new AccessorImpl());
    }
    
    private BuiltinTypes() {
    }

    private static class BuiltinImpl implements CsmBuiltIn, CsmIdentifiable {

        private final CharSequence name;
        private final CsmUID<CsmBuiltIn> uid;
        
        private BuiltinImpl(CharSequence name) {
            this.name =name;
            this.uid = new BuiltInUID(this);
        }
        
        @Override
        public CharSequence getQualifiedName() {
            return getName();
        }

        @Override
        public CharSequence getUniqueName() {
            return CharSequences.create(CharSequenceUtils.concatenate(Utils.getCsmDeclarationKindkey(getKind()), OffsetableDeclarationBase.UNIQUE_NAME_SEPARATOR,  getQualifiedName()));
        }
        
        @Override
        public CharSequence getName() {
            assert name != null && name.length() > 0;
            return name;
        }

        @Override
        public CsmDeclaration.Kind getKind() {
            return CsmDeclaration.Kind.BUILT_IN;
        }

        @Override
        public CsmScope getScope() {
            // TODO: builtins shouldn't be declarations! snd thus shouldn't be ScopeElements!
            return null;
        }
        
        @Override
        public CsmUID<CsmBuiltIn> getUID() {
            return uid;
        }
        
        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }            
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            BuiltinImpl other = (BuiltinImpl)obj;
            return name.equals(other.name);
        }

        @Override
        public String toString() {
            return "" + getKind() + " " +  getQualifiedName(); // NOI18N
        }

        @Override
        public boolean isValid() {
            return true;
        }
    }
    
    private static final ConcurrentMap<CharSequence, CsmBuiltIn> types = new ConcurrentHashMap<>();
    
    public static CsmBuiltIn getBuiltIn(AST ast) {
        assert ast.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN;
        StringBuilder sb = new StringBuilder();
        // TODO: take synonims into account!!!
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            if( sb.length() > 0 ) {
                sb.append(' ');
            }
            sb.append(AstUtil.getText(token));
        }
        assert sb.length() > 0 : "no name in " + AstUtil.toString(ast);
        return getBuiltIn(sb);
    }
    
    public static CsmBuiltIn getBuiltIn(CharSequence text) {
        assert text != null && text.length() > 0;
        text = QualifiedNameCache.getManager().getString(text);
        CsmBuiltIn builtIn = types.get(text);
        if( builtIn == null ) {
            builtIn = new BuiltinImpl(text);
            CsmBuiltIn old = types.putIfAbsent(text, builtIn);
            if (old != null) {
                builtIn = old;
            }
        }
        return builtIn;
    }

    public static ObjectBasedUID<CsmBuiltIn> readUID(RepositoryDataInput aStream) throws IOException {
        CharSequence name = PersistentUtils.readUTF(aStream, QualifiedNameCache.getManager()); // no need for text manager
        CsmBuiltIn builtIn = BuiltinTypes.getBuiltIn(name);
        CsmUID<CsmBuiltIn> anUID = UIDs.<CsmBuiltIn>get(builtIn);
        assert anUID != null;
        return (ObjectBasedUID<CsmBuiltIn>) anUID;
    }

    /**
     * UID for CsmBuiltIn
     */    
    public static final class BuiltInUID extends ObjectBasedUID<CsmBuiltIn> {
        private BuiltInUID(CsmBuiltIn decl) {
            super(decl);
        }
        
        @Override
        public String toString() {
            String retValue = "<BUILT-IN UID> " + super.toString(); // NOI18N
            return retValue;
        } 

        @Override
        public void write(RepositoryDataOutput output) throws IOException {
            BuiltinImpl ref = (BuiltinImpl) getObject();
            assert ref != null;
            assert ref.getName() != null;
            PersistentUtils.writeUTF(ref.getName(), output);
        }
    }

}
