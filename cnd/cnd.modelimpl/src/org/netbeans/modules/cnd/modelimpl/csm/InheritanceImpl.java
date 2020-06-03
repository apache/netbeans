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
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.TypeFactory.TypeBuilder;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase.ScopedDeclarationBuilder;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDProviderIml;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * CsmInheritance implementation
 */
public final class InheritanceImpl extends OffsetableIdentifiableBase<CsmInheritance> implements CsmInheritance {

    private CsmVisibility visibility;
    private boolean virtual;
    
    //private CsmUID<CsmClass> resolvedAncestorClassCacheUID;
    
    //private CsmUID<CsmClassifier> classifierCacheUID;
    
    private CsmType ancestorType;
    private CsmClassifier resolvedClassifier;
    private final CsmUID<CsmScope> scope;
    
    private InheritanceImpl(AST ast, CsmFile file, CsmScope scope) {
        super(file, getStartOffset(ast), getEndOffset(ast));
        this.scope = UIDCsmConverter.scopeToUID(scope);
        visibility = ((CsmDeclaration)scope).getKind() == CsmDeclaration.Kind.STRUCT?
                CsmVisibility.PUBLIC: CsmVisibility.PRIVATE;
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.LITERAL_private:
                    visibility = CsmVisibility.PRIVATE;
                    break;
                case CPPTokenTypes.LITERAL_public:
                    visibility = CsmVisibility.PUBLIC;
                    break;
                case CPPTokenTypes.LITERAL_protected:
                    visibility = CsmVisibility.PROTECTED;
                    break;
                case CPPTokenTypes.LITERAL_virtual:
                    virtual = true;
                    break;
                case CPPTokenTypes.CSM_TYPE_DECLTYPE:
                case CPPTokenTypes.IDENT:
                    this.ancestorType = TemplateUtils.checkTemplateType(TypeFactory.createType(token, getContainingFile(), null, 0, scope), scope);
                    return; // it's definitely the last!; besides otherwise we get NPE in for
            }
        }
    }

    private InheritanceImpl(CsmType type, CsmVisibility visibility, boolean virtual, CsmScope scope, CsmFile file, int startOffset, int endOffset) {
        super(file, startOffset, endOffset);
        this.scope = UIDCsmConverter.scopeToUID(scope);
        this.visibility = visibility;
        this.virtual = virtual;
        this.ancestorType = TemplateUtils.checkTemplateType(type, scope);
    }
    
    public static InheritanceImpl create(AST ast, CsmFile file, CsmScope scope, boolean isGlobal) {
        InheritanceImpl inheritanceImpl = new InheritanceImpl(ast, file, scope);
        if (!isGlobal) {
            Utils.setSelfUID(inheritanceImpl);
        }
        return inheritanceImpl;
    }

    // constructor for LWM factory
    private InheritanceImpl(CsmFile file, CsmScope scope, CsmType ancestorType, CsmVisibility visibility, boolean virtual, int startOffset, int endOffset) {
        super(file, startOffset, endOffset);
        this.scope = UIDCsmConverter.scopeToUID(scope);
        if (visibility == null) {
            this.visibility = ((CsmDeclaration)scope).getKind() == CsmDeclaration.Kind.STRUCT ? CsmVisibility.PUBLIC: CsmVisibility.PRIVATE;
        } else {
            this.visibility = visibility;
        }
        this.virtual = virtual;
        this.ancestorType = ancestorType;
    }

    public static InheritanceImpl create(CsmFile file, CsmScope scope, CsmType ancestorType, CsmVisibility visibility, boolean virtual, int startOffset, int endOffset) {
        return new InheritanceImpl(file, scope, ancestorType, visibility, virtual, startOffset, endOffset);
    }

    @Override
    public boolean isVirtual() {
        return virtual;
    }

    @Override
    public CsmVisibility getVisibility() {
        return visibility;
    }

    @Override
    public CsmType getAncestorType() {
        return ancestorType;
    }

    @Override
    public CsmClassifier getClassifier() {
        if (!CsmBaseUtilities.isValid(resolvedClassifier)) {
            resolvedClassifier = getAncestorType().getClassifier();
        }
        return resolvedClassifier;
    }

    @Override
    protected CsmUID<CsmInheritance> createUID() {
        return UIDUtilities.createInheritanceUID(this);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 11 * hash + this.visibility.hashCode();
        hash = 11 * hash + (this.virtual ? 1 : 0);
        hash = 11 * hash + (this.ancestorType != null ? this.ancestorType.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof InheritanceImpl)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final InheritanceImpl other = (InheritanceImpl) obj;
        if (this.visibility != other.visibility) {
            return false;
        }
        if (this.virtual != other.virtual) {
            return false;
        }
        if (this.ancestorType != other.ancestorType && (this.ancestorType == null || !this.ancestorType.equals(other.ancestorType))) {
            return false;
        }
        return true;
    }
    
    
    public static class InheritanceBuilder extends ScopedDeclarationBuilder {

        private CsmVisibility visibility = CsmVisibility.PUBLIC;
        private boolean _virtual = false;
        private TypeBuilder typeBuilder;

        public void setTypeBuilder(TypeBuilder type) {
            this.typeBuilder = type;
        }

        public CsmVisibility getVisibility() {
            return visibility;
        }

        public void setVisibility(CsmVisibility visibility) {
            this.visibility = visibility;
        }

        public boolean isVirtual() {
            return _virtual;
        }

        public void setVirtual() {
            this._virtual = true;
        }
        
        public InheritanceImpl create() {
            InheritanceImpl impl = new InheritanceImpl(getType(), getVisibility(), _virtual, getScope(), getFile(), getStartOffset(), getEndOffset());
            return impl;
        }
        
        private CsmType getType() {
            CsmType type = null;
            if (typeBuilder != null) {
                typeBuilder.setScope(getScope());
                type = typeBuilder.create();
            }
            if (type == null) {
                type = TypeFactory.createSimpleType(BuiltinTypes.getBuiltIn("int"), getFile(), getStartOffset(), getStartOffset()); // NOI18N
            }
            return type;
        }         
    }     

    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeVisibility(this.visibility, output);
        output.writeBoolean(this.virtual);
        PersistentUtils.writeType(ancestorType, output);

        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.writeUID(scope, output);
        // save cache
        CsmUID<CsmClassifier> toSave = UIDCsmConverter.objectToUID(resolvedClassifier);
        if (!UIDProviderIml.isPersistable(toSave)) {
            toSave = null;
        }
        factory.writeUID(toSave, output);
    }

    public InheritanceImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.visibility = PersistentUtils.readVisibility(input);
        this.virtual = input.readBoolean();
        this.ancestorType = PersistentUtils.readType(input);

        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        this.scope = factory.readUID(input);

        // restore cached value
        CsmUID<CsmClassifier> uid = factory.readUID(input);
        this.resolvedClassifier = UIDCsmConverter.UIDtoIdentifiable(uid);
    }    

    @Override
    public String toString() {
        return "INHERITANCE " + visibility + " " + (isVirtual() ? "virtual " : "") + ancestorType.getText() + getOffsetString(); // NOI18N
    }

    @Override
    public CsmScope getScope() {
        return UIDCsmConverter.UIDtoScope(scope);
    }
}
