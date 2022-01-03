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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifierBasedTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmIdentifiable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypeBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.TypeBasedSpecializationParameterImpl.TypeBasedSpecializationParameterBuilder;
import static org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer.getClosestNamespaceInfo;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionsFactory;
import org.netbeans.modules.cnd.modelimpl.parser.FakeAST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.MutableObject;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.CharSequences;

/**
 *
 */
public final class TemplateParameterImpl<T> extends OffsetableDeclarationBase<T> implements CsmClassifierBasedTemplateParameter, CsmTemplate, SelfPersistent {
    private final CharSequence name;
    private final CsmUID<CsmScope> scope;
    private final TemplateDescriptor templateDescriptor;
    private final boolean typeBased;
        
    private CsmSpecializationParameter defaultValue;

    public TemplateParameterImpl(AST ast, CharSequence name, CsmFile file, CsmScope scope, boolean variadic, boolean global) {
        this(ast, name, getStartOffset(ast), getEndOffset(ast), file, scope, variadic, global);
    }
        
    public TemplateParameterImpl(AST ast, CharSequence name, int startOffset, int endOffset, CsmFile file, CsmScope scope, boolean variadic, boolean global) {
        super(file, startOffset, endOffset);
                
        CsmSpecializationParameter value = null;
        if (checkExplicitType(ast)) {
            this.typeBased = false;
            AST expressionAst = AstUtil.findSiblingOfType(ast.getFirstChild(), CPPTokenTypes.CSM_EXPRESSION, AstUtil.findSiblingOfType(ast, CPPTokenTypes.COMMA));
            if (expressionAst != null) {
                CsmExpression expr = ExpressionsFactory.create(expressionAst, file, scope);
                value = ExpressionBasedSpecializationParameterImpl.create(expr.getText(), scope, file, expr.getStartOffset(), expr.getEndOffset(), true);
            }
        } else {
            this.typeBased = true;
            AST assign = AstUtil.findSiblingOfType(ast, CPPTokenTypes.ASSIGNEQUAL, AstUtil.findSiblingOfType(ast, CPPTokenTypes.COMMA));
            value = createDefaultValue(ast, assign, file, scope, global);
        }
        
        this.name = NameCache.getManager().getString(name);
        templateDescriptor = TemplateDescriptor.createIfNeeded(ast, file, scope, global);
        if ((scope instanceof CsmIdentifiable)) {
            this.scope = UIDCsmConverter.scopeToUID(scope);
        } else {
            this.scope = null;
        }
        this.defaultValue = variadic ? VARIADIC : value;
    }

    private TemplateParameterImpl(CharSequence name, TemplateDescriptor templateDescriptor, TypeBasedSpecializationParameterImpl defaultValue,  boolean variadic, CsmScope scope, CsmFile file, int startOffset, int endOffset, boolean typeBased) {
        super(file, startOffset, endOffset);
        this.name = NameCache.getManager().getString(name);
        this.templateDescriptor = templateDescriptor;
        this.typeBased = typeBased;
        if ((scope instanceof CsmIdentifiable)) {
            this.scope = UIDCsmConverter.scopeToUID(scope);
        } else {
            this.scope = null;
        }
        this.defaultValue = variadic ? VARIADIC : defaultValue;
    }    
    
    @Override
    public CharSequence getName() {
        return name;
    }

    @Override
    public CsmSpecializationParameter getDefaultValue() {
        return defaultValue == VARIADIC ? null : defaultValue;
    }

    @Override
    public int hashCode() {
        // use cheap hashCode
        if (true) {
            return name.hashCode();
        } else {
            int hash = 5;
            hash = 19 * hash + Objects.hashCode(this.name);
            hash = 19 * hash + Objects.hashCode(this.scope);
            hash = 19 * hash + Objects.hashCode(this.defaultValue);
            hash = 19 * hash + Objects.hashCode(this.templateDescriptor);
            hash = 19 * hash + Objects.hashCode(super.hashCode());
            return hash;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }        
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TemplateParameterImpl<?> other = (TemplateParameterImpl<?>) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.scope, other.scope)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!Objects.equals(this.defaultValue, other.defaultValue)) {
            return false;
        }
        if (!Objects.equals(this.templateDescriptor, other.templateDescriptor)) {
            return false;
        }
        return true;
    }
        
    @Override
    public boolean isTemplate() {
        return templateDescriptor != null;
    }

    @Override
    public boolean isSpecialization() {
        return false;
    }

    @Override
    public boolean isExplicitSpecialization() {
        return false;
    }
    

    @Override
    public boolean isVarArgs() {
        return defaultValue == VARIADIC;
    }    

    @Override
    public List<CsmTemplateParameter> getTemplateParameters() {
        return (templateDescriptor != null) ? templateDescriptor.getTemplateParameters() : Collections.<CsmTemplateParameter>emptyList();
    }

    @Override
    public CharSequence getDisplayName() {
        return (templateDescriptor != null) ? CharSequences.create(CharSequenceUtils.concatenate(getName(), templateDescriptor.getTemplateSuffix())) : getName();
    }

    @Override
    public boolean isTypeBased() {
        return typeBased;
    }
   
    private static CsmTypeBasedSpecializationParameter createDefaultValue(AST ast, AST assign, CsmFile file, CsmScope scope, boolean global) {
        if (assign != null && assign.getType() == CPPTokenTypes.ASSIGNEQUAL) {
            if (assign.getNextSibling() != null) {
                CsmType type = null;
                AST typeAst = assign.getNextSibling();
                if (typeAst.getType() == CPPTokenTypes.LITERAL_typename && typeAst.getNextSibling() != null) {
                    typeAst = typeAst.getNextSibling();
                }
                if (typeAst.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND
                        || typeAst.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN) {
                    type = TypeFactory.createType(typeAst, file, null, 0);
                } else if (typeAst.getType() == CPPTokenTypes.LITERAL_struct
                        || typeAst.getType() == CPPTokenTypes.LITERAL_class
                        || typeAst.getType() == CPPTokenTypes.LITERAL_union) {      
                    // This is for types like DDD in the next code:
                    //  template<typename TAG = struct DDD>
                    //  struct copy {};                                  
                    type = TypeFactory.createType(typeAst, file, null, 0);
                    
                    MutableObject<CsmNamespace> targetScope = new MutableObject<>();
                    MutableObject<MutableDeclarationsContainer> targetDefinitionContainer = new MutableObject<>();
                    
                    // TODO: need fileContent here
                    getClosestNamespaceInfo(scope, file, null, OffsetableBase.getStartOffset(ast), targetScope, targetDefinitionContainer); 
                    
                    FakeAST fakeParent = new FakeAST();
                    fakeParent.setType(CPPTokenTypes.CSM_GENERIC_DECLARATION);
                    fakeParent.addChild(typeAst);                                    
                    ClassForwardDeclarationImpl.create(fakeParent, file, targetScope.value, targetDefinitionContainer.value, global);
                }
                if (type != null) {
                    return new TypeBasedSpecializationParameterImpl(type, scope);
                }
            }
        }
        return null;
    }
    
    public static class TemplateParameterBuilder extends SimpleDeclarationBuilder {

        private boolean variadic = false;
        private TypeBasedSpecializationParameterBuilder defaultValue;

        public void setDefaultValue(TypeBasedSpecializationParameterBuilder defaultValue) {
            this.defaultValue = defaultValue;
        }

        public void setVariadic() {
            this.variadic = true;
        }
        
        @Override
        public TemplateParameterImpl create() {
            TemplateDescriptor td = null;
            if(getTemplateDescriptorBuilder() != null) {
                getTemplateDescriptorBuilder().setScope(getScope());
                td = getTemplateDescriptorBuilder().create();
            }
            TypeBasedSpecializationParameterImpl value = null;
            if(defaultValue != null) {
                defaultValue.setScope(getScope());
                value = defaultValue.create();
            }
            TemplateParameterImpl param = new TemplateParameterImpl(getName(), td, value, variadic, getScope(), getFile(), getStartOffset(), getEndOffset(), true);
            return param;
        }
    }      
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output); 
        PersistentUtils.writeUTF(name, output);
        UIDObjectFactory.getDefaultFactory().writeUID(scope, output);
        boolean variadic = isVarArgs();
        output.writeBoolean(variadic);
        if(!variadic) {
            PersistentUtils.writeSpecializationParameter(defaultValue, output);
        }       
        PersistentUtils.writeTemplateDescriptor(templateDescriptor, output);
        output.writeBoolean(typeBased);
    }
    
    public TemplateParameterImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.name = PersistentUtils.readUTF(input, NameCache.getManager());
        this.scope = UIDObjectFactory.getDefaultFactory().readUID(input);
        boolean variadic = input.readBoolean();
        if(!variadic) {
            this.defaultValue = PersistentUtils.readSpecializationParameter(input);
        } else {
            this.defaultValue = VARIADIC;
        }
        this.templateDescriptor = PersistentUtils.readTemplateDescriptor(input);
        this.typeBased = input.readBoolean();
    }
    
    @Override
    public CsmScope getScope() {
        return scope == null? null : scope.getObject();
    }

    @Override
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.TEMPLATE_PARAMETER;
    }

    @Override
    public CharSequence getQualifiedName() {
        CsmScope s = getScope();
        if (CsmKindUtilities.isFunction(s)) {
            return CharSequences.create(CharSequenceUtils.concatenate(((CsmFunction)s).getQualifiedName(),"::",name)); // NOI18N
        } else if (CsmKindUtilities.isClass(s)) {
            return CharSequences.create(CharSequenceUtils.concatenate(((CsmClass)s).getQualifiedName(),"::",name)); // NOI18N
        }
        return name;
    }

    @Override
    public String toString() {
        return getQualifiedName().toString() + getPositionString();
    }

    private boolean checkExplicitType(AST ast) {
        if (ast != null) {
            if (!"class".equals(ast.getText()) &&     // NOI18N
                !"typename".equals(ast.getText()) &&  // NOI18N
                ast.getType() != CPPTokenTypes.CSM_TEMPLATE_TEMPLATE_PARAMETER) {
                return true;
            }
        }
        return false;
    }

    private static final CsmSpecializationParameter VARIADIC = new CsmSpecializationParameter() {
        @Override
        public CsmFile getContainingFile() {
            throw new UnsupportedOperationException();
        }
        @Override
        public CsmScope getScope() {
            throw new UnsupportedOperationException();
        }
        @Override
        public int getStartOffset() {
            throw new UnsupportedOperationException();
        }
        @Override
        public int getEndOffset() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Position getStartPosition() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Position getEndPosition() {
            throw new UnsupportedOperationException();
        }
        @Override
        public CharSequence getText() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return "VARIADIC";// NOI18N
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
                
    };
    
}
