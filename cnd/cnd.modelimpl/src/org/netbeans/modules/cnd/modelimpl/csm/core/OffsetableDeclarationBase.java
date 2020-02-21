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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.BuiltinTypes;
import org.netbeans.modules.cnd.modelimpl.csm.CsmObjectBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceDefinitionImpl.NamespaceBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.TemplateDescriptor;
import org.netbeans.modules.cnd.modelimpl.csm.TemplateUtils;
import org.netbeans.modules.cnd.modelimpl.csm.TypeFactory;
import org.netbeans.modules.cnd.modelimpl.csm.TypeFactory.TypeBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.CompoundStatementImpl;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase.ExpressionBuilder;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.CharSequences;

/**
 *
 */
public abstract class OffsetableDeclarationBase<T> extends OffsetableIdentifiableBase<T> implements CsmOffsetableDeclaration {
    
    public static final char UNIQUE_NAME_SEPARATOR = ':';
    
    // Postifix in internal name of UID of the declaration if it was added via 
    // #include directive into the class or namespace
    protected static final String INCLUDED_DECLARATION = "ID$"; // NOI18N
    
    /**
     * Checks whether declaration was included with #include directive into other C++ declaration.
     *  
     * Example: all uids inside AAA structure will be "included"
     * 
     * struct AAA {
     *  #include "body.inc"
     * };
     * 
     * @param uid
     * @return true if included, false otherwise
     */
    public static boolean isIncludedDeclaration(CsmUID<?> uid) {
        CharSequence internalName = UIDUtilities.getName(uid, true);
        int internalIndex = CharSequenceUtilities.indexOf(internalName, KeyUtilities.UID_INTERNAL_DATA_PREFIX);
        if (internalIndex > 0) {
            // INCLUDED_DECLARATION is right after UID_INTERNAL_DATA_PREFIX
            return CharSequenceUtilities.indexOf(
                    internalName, 
                    INCLUDED_DECLARATION, 
                    internalIndex
            ) > 0; 
        }
        return false;
    }
    
    protected OffsetableDeclarationBase(CsmFile file, int startOffset, int endOffset) {
        super(file, startOffset, endOffset);
    }

    @Override
    public CharSequence getUniqueName() {
        return CharSequences.create(CharSequenceUtils.concatenate(Utils.getCsmDeclarationKindkey(getKind()), UNIQUE_NAME_SEPARATOR, getUniqueNameWithoutPrefix())); //NOI18N
    }
    
    public CharSequence getUniqueNameWithoutPrefix() {
        return getQualifiedName();
    }
    
    protected final CsmProject getProject() {
        CsmFile file = this.getContainingFile();
        if (file == null) {
            CndUtils.assertUnconditional("Null containing file"); //NOI18N
            return null;
        } else {
            return file.getProject();
        }
    }    
    
    public CharSequence getQualifiedNamePostfix() {
        if (TraceFlags.SET_UNNAMED_QUALIFIED_NAME && (getName().length() == 0)) {
            return getOffsetBasedName();
        } else {
            return getName();
        }
    }

    protected CharSequence toStringName() {
        CharSequence name;
        if (CsmKindUtilities.isTemplate(this)) {
            name = ((CsmTemplate)this).getDisplayName();
        } else {
            name = getName();
        }
        if (this instanceof RawNamable) {
            StringBuilder out = new StringBuilder(name);
            out.append('(');
            boolean first = true;
            for (CharSequence part : ((RawNamable)this).getRawName()) {
                if (first) {
                    first = false;
                } else {
                    out.append("::"); //NOI18N
                }
                out.append(part);
            }
            out.append(')');
            name = out;
        }
        return name;
    }
    
    private String getOffsetBasedName() {
        return "[" + this.getContainingFile().getName() + ":" + this.getStartOffset() + "-" + this.getEndOffset() + "]"; // NOI18N
    }   

    @Override
    protected CsmUID<? extends CsmOffsetableDeclaration> createUID() {
        return UIDUtilities.<CsmOffsetableDeclaration>createDeclarationUID(this);
    }

    protected static TemplateDescriptor createTemplateDescriptor(AST node, CsmFile file, CsmScope scope, StringBuilder classTemplateSuffix, boolean global) {
        boolean _template = false, specialization = false;
        switch(node.getType()) {
            case CPPTokenTypes.CSM_VARIABLE_TEMPLATE_DECLARATION:
            case CPPTokenTypes.CSM_FUNCTION_LIKE_VARIABLE_TEMPLATE_DECLARATION:
            case CPPTokenTypes.CSM_FUNCTION_TEMPLATE_DECLARATION: 
            case CPPTokenTypes.CSM_FUNCTION_TEMPLATE_DEFINITION: 
            case CPPTokenTypes.CSM_CTOR_TEMPLATE_DECLARATION: 
            case CPPTokenTypes.CSM_CTOR_TEMPLATE_DEFINITION:  
            case CPPTokenTypes.CSM_TEMPL_FWD_CL_OR_STAT_MEM:
            case CPPTokenTypes.CSM_USER_TYPE_CAST_TEMPLATE_DECLARATION:
            case CPPTokenTypes.CSM_USER_TYPE_CAST_TEMPLATE_DEFINITION:
                _template = true;
                break;
            case CPPTokenTypes.CSM_TEMPLATE_FUNCTION_DEFINITION_EXPLICIT_SPECIALIZATION:
            case CPPTokenTypes.CSM_USER_TYPE_CAST_DEFINITION_EXPLICIT_SPECIALIZATION:
            case CPPTokenTypes.CSM_TEMPLATE_CTOR_DEFINITION_EXPLICIT_SPECIALIZATION:
            case CPPTokenTypes.CSM_TEMPLATE_DTOR_DEFINITION_EXPLICIT_SPECIALIZATION:
            case CPPTokenTypes.CSM_TEMPLATE_EXPLICIT_SPECIALIZATION:
                _template = true;
                specialization = true;
                break;
        }
        if (_template) {
            List<CsmTemplateParameter> templateParams = null;
            AST templateNode = node.getFirstChild();                        
            if (templateNode == null || templateNode.getType() != CPPTokenTypes.LITERAL_template) {
                return null;
            }
            
            List<AST> templateClassNodes = new ArrayList<>();
            
            // 0. our grammar can't yet differ template-class's method from template-method
            // so we need to check here if we has template-class or not
            AST qIdToken = AstUtil.findChildOfType(node, CPPTokenTypes.CSM_QUALIFIED_ID);
            // 1. check for definition of template class's method
            // like template<class A> C<A>:C() {}
            AST startTemplateSign = qIdToken != null ? AstUtil.findChildOfType(qIdToken, CPPTokenTypes.LESSTHAN) : null;
            while (templateNode != null && startTemplateSign != null) {
                // TODO: fix parsing of inline definition of template operator <
                // like template<class T, class P> bool operator<(T x, P y) {return x<y};
                // workaround is next validation
                AST endTemplateSign = null;//( startTemplateSign.getNextSibling() != null ? startTemplateSign.getNextSibling().getNextSibling() : null);
                for( AST sibling = startTemplateSign.getNextSibling(); sibling != null; sibling = sibling.getNextSibling() ) {
                    if( sibling.getType() == CPPTokenTypes.GREATERTHAN ) {
                        endTemplateSign = sibling;
                        break;
                    }
                }
                if (endTemplateSign != null) {
                    AST scopeSign = endTemplateSign.getNextSibling();
                    if (scopeSign != null && scopeSign.getType() == CPPTokenTypes.SCOPE) {
                        _template = false; // assume that we do not have template method at all (if we have, it will be set later)
                        
                        // 2. we have template class, we need to determine, is it specialization definition or not
                        if (specialization && classTemplateSuffix != null) { 
                            // we need to initialize classTemplateSuffix in this case
                            // to avoid mixing different specialization (IZ92138)
                            classTemplateSuffix.append(TemplateUtils.getSpecializationSuffix(qIdToken, null));
                        }        
                        templateClassNodes.add(templateNode);
                        templateNode = AstUtil.findSiblingOfType(templateNode.getNextSibling(), CPPTokenTypes.LITERAL_template);
                        startTemplateSign = AstUtil.findSiblingOfType(endTemplateSign, CPPTokenTypes.LESSTHAN);
                    } else {
                        break;
                    }                    
                } else {
                    break;
                }
            }
            
            if (false) {
              // Both templateNode and startTemplateSign should be null
              if (templateNode != startTemplateSign) {
                // TODO: maybe notify here about error: unbalanced "template <>" nodes and specialization suffixes "<smth>"
                // Example: template<> void A<short>::f<int>()
                return null;
              }
            }
            
            if (!_template) {
                // template method without template arrows
                // e.g.: template<class A> template<class B> C<A>::C(B b) {}
                if (templateNode != null && templateNode.getType() == CPPTokenTypes.LITERAL_template ) {
                    // it is template-method of template-class
                    _template = true;
                }                
            }
            
            int inheritedTemplateParametersNumber = 0;
            if (!templateClassNodes.isEmpty()){
                for (AST templateClassNode : templateClassNodes) {
                    if (templateParams == null) {
                        templateParams = TemplateUtils.getTemplateParameters(templateClassNode, file, scope, global);
                    } else {
                        templateParams.addAll(TemplateUtils.getTemplateParameters(templateClassNode, file, scope, global));
                    }
                    inheritedTemplateParametersNumber = templateParams.size();
                }
            }
            if (_template) {                
                CharSequence templateSuffix;
                // 3. We are sure now what we have template-method, 
                // let's check is it specialization template or not
                if (specialization) {
                    // 3a. specialization
                    if (qIdToken == null) {
                        // malformed template specification
                        templateSuffix = "<>"; //NOI18N
                    } else {
                        templateSuffix = TemplateUtils.getSpecializationSuffix(qIdToken, null);
                    }
                } else {
                    // 3b. no specialization, plain and simple template-method
                    StringBuilder sb  = new StringBuilder();
                    TemplateUtils.addSpecializationSuffix(templateNode.getFirstChild(), sb, null);
                    templateSuffix = CharSequenceUtils.concatenate("<", sb, ">"); //NOI18N
                }                
                if(templateParams != null) {
                    templateParams.addAll(TemplateUtils.getTemplateParameters(templateNode,
                        file, scope, global));
                } else {
                    templateParams = TemplateUtils.getTemplateParameters(templateNode,
                        file, scope, global);
                }
                return new TemplateDescriptor(templateParams, templateSuffix, inheritedTemplateParametersNumber, specialization, global);
            } else {
                return new TemplateDescriptor(templateParams, "", inheritedTemplateParametersNumber, specialization, global);
            }
        }
        return null;
    }
    
    public static abstract class ScopedDeclarationBuilder extends OffsetableIdentifiableBuilder {

        private boolean global = true;
        private CsmObjectBuilder parent;
        private CsmScope scope;

        public ScopedDeclarationBuilder() {
        }
        
        protected ScopedDeclarationBuilder(ScopedDeclarationBuilder builder) {
            super(builder);
            global = builder.global;
            parent = builder.parent;
            scope = builder.scope;
        }        
        
        public void setScope(CsmScope scope) {
            this.scope = scope;
        }
        
        public void setParent(CsmObjectBuilder parent) {
            this.parent = parent;
        }        

        public CsmObjectBuilder getParent() {
            return parent;
        }
        
        public CsmScope getScope() {
            if(scope != null) {
                return scope;
            }
            if(parent instanceof NamespaceDefinitionImpl.NamespaceBuilder) {
                scope = ((NamespaceDefinitionImpl.NamespaceBuilder)parent).getNamespace();
            } else {
                scope = (NamespaceImpl) getFile().getProject().getGlobalNamespace();
            }
            return scope;
        }
        
        protected void addDeclaration(CsmOffsetableDeclaration decl) {
            if(parent != null) {
                if(parent instanceof NamespaceBuilder) {
                    ((NamespaceBuilder)parent).addDeclaration(decl);
                }          
            } else {
                getFileContent().addDeclaration(decl);
            }                
            if(getScope() instanceof CsmNamespace) {
                ((NamespaceImpl)getScope()).addDeclaration(decl);
            }
        }        

        public boolean isGlobal() {
            return global && !(scope instanceof CompoundStatementImpl);
        }

        public void setLocal() {
            this.global = false;
        }
        
        @Override
        public CharSequence getName() {
            String[] split = super.getName().toString().split("::"); // NOI18N
            return NameCache.getManager().getString(split[split.length - 1]);
        }
        
        public CharSequence[] getScopeNames() {
            if(super.getName() != null) {
                String[] split = super.getName().toString().split("::"); // NOI18N
                CharSequence[] res = new CharSequence[split.length - 1];
                for (int i = 0; i < res.length; i++) {
                    res[i] =  NameCache.getManager().getString(split[i]);
                }
                return res;
            } else {
                return new CharSequence[0];
            }
        }

        @Override
        public String toString() {
            return "{" + "global=" + global + ", scope=" + scope + super.toString() + '}'; //NOI18N
        }
    }
    
    public static class SimpleDeclarationBuilder extends ScopedDeclarationBuilder {
        
        private boolean typedefSpecifier = false;
        private boolean friendSpecifier = false;
        private boolean typeSpecifier = false;
        private boolean inDeclSpecifiers = false;
        private DeclaratorBuilder declaratorBuilder;
        private TypeBuilder typeBuilder;
        private CsmObjectBuilder parametersListBuilder;
        private TemplateDescriptor.TemplateDescriptorBuilder templateDescriptorBuilder;
        private ExpressionBuilder initializerBuilder;
        
        private boolean _static = false;
        private boolean _extern = false;
        private boolean _const = false;

        private boolean constructor = false;
        private boolean destructor = false;

        public SimpleDeclarationBuilder() {
        }
        
        protected SimpleDeclarationBuilder(SimpleDeclarationBuilder builder) {
            super(builder);
            typedefSpecifier = builder.typedefSpecifier;
            friendSpecifier = builder.friendSpecifier;
            typeSpecifier = builder.typeSpecifier;
            inDeclSpecifiers = builder.inDeclSpecifiers;
            
            declaratorBuilder = builder.declaratorBuilder;
            typeBuilder = builder.typeBuilder;
            parametersListBuilder = builder.parametersListBuilder;
            templateDescriptorBuilder = builder.templateDescriptorBuilder;
            initializerBuilder = builder.initializerBuilder;

            _static = builder._static;
            _extern = builder._extern;
            _const = builder._const;

            constructor = builder.constructor;
            destructor = builder.destructor;
        }
        
        public void setInitializerBuilder(ExpressionBuilder initializerBuilder) {
            this.initializerBuilder = initializerBuilder;
        }

        public ExpressionBuilder getInitializerBuilder() {
            return initializerBuilder;
        }
        
        public void setConstructor() {
            this.constructor = true;
        }

        public void setDestructor() {
            this.destructor = true;
        }

        public boolean isConstructor() {
            return constructor;
        }

        public boolean isDestructor() {
            return destructor;
        }

        public void setFriend() {
            friendSpecifier = true;
        }
                
        public boolean isFriend() {
            return friendSpecifier;
        }
        
        public void setStatic() {
            this._static = true;
        }

        public void setExtern() {
            this._extern = true;
        }

        public void setConst() {
            this._const = true;
        }

        public boolean isStatic() {
            return _static;
        }

        public boolean isExtern() {
            return _extern;
        }

        public boolean isConst() {
            return _const;
        }

        
        public void setTypedefSpecifier() {
            this.typedefSpecifier = true;
            if(typeBuilder != null) {
                typeBuilder.setTypedef();
            }            
        }

        public boolean hasTypedefSpecifier() {
            return typedefSpecifier;
        }

        public void setTypeSpecifier() {
            this.typeSpecifier = true;
        }

        public boolean hasTypeSpecifier() {
            return typeSpecifier && inDeclSpecifiers;
        }
        
        public void declSpecifiers() {
            inDeclSpecifiers = true;
        }

        public void endDeclSpecifiers() {
            inDeclSpecifiers = false;
        }
        
        public boolean isInDeclSpecifiers() {
            return inDeclSpecifiers;
        }

        public void setDeclaratorBuilder(DeclaratorBuilder declaratorBuilder) {
            this.declaratorBuilder = declaratorBuilder;
        }

        public void setTemplateDescriptorBuilder(TemplateDescriptor.TemplateDescriptorBuilder templateDescriptorBuilder) {
            this.templateDescriptorBuilder = templateDescriptorBuilder;
            if(templateDescriptorBuilder != null) {
                setStartOffset(templateDescriptorBuilder.getStartOffset());
            }
        }

        public TemplateDescriptor.TemplateDescriptorBuilder getTemplateDescriptorBuilder() {
            return templateDescriptorBuilder;
        }
        
        public TemplateDescriptor getTemplateDescriptor() {
            TemplateDescriptor td = null;
            if(getTemplateDescriptorBuilder() != null) {
                getTemplateDescriptorBuilder().setScope(getScope());
                td = getTemplateDescriptorBuilder().create();
            }
            return td;
        }
        
        public void setTypeBuilder(TypeBuilder typeBuilder) {
            this.typeBuilder = typeBuilder;
        }

        public TypeBuilder getTypeBuilder() {
            return typeBuilder;
        }
        
        
        public DeclaratorBuilder getDeclaratorBuilder() {
            return declaratorBuilder;
        }

        public void setParametersListBuilder(CsmObjectBuilder parametersListBuilder) {
            this.parametersListBuilder = parametersListBuilder;
        }

        public CsmObjectBuilder getParametersListBuilder() {
            return parametersListBuilder;
        }
        
        public boolean isFunction() {
            return parametersListBuilder != null;
        }
        
        protected CsmType getType() {
            CsmType type = null;
            if (getTypeBuilder() != null) {
                getTypeBuilder().setScope(getScope());
                type = getTypeBuilder().create();
            }
            if (type == null) {
                type = TypeFactory.createSimpleType(BuiltinTypes.getBuiltIn("int"), getFile(), getStartOffset(), getStartOffset()); // NOI18N
            }
            return type;
        }
        
        public CsmDeclaration create() {
            throw new UnsupportedOperationException("Should not be used."); // NOI18N
        }

        @Override
        public String toString() {
            return "SimpleDeclarationBuilder{" + "typedefSpecifier=" + typedefSpecifier + ", friendSpecifier=" + friendSpecifier + //NOI18N
                    ", typeSpecifier=" + typeSpecifier + ", inDeclSpecifiers=" + inDeclSpecifiers + //NOI18N
                    ", declaratorBuilder=" + declaratorBuilder + ", typeBuilder=" + typeBuilder + //NOI18N
                    ", parametersListBuilder=" + parametersListBuilder + ", templateDescriptorBuilder=" + templateDescriptorBuilder + //NOI18N
                    ", initializerBuilder=" + initializerBuilder + ", _static=" + _static + //NOI18N
                    ", _extern=" + _extern + ", _const=" + _const + ", constructor=" + constructor + //NOI18N
                    ", destructor=" + destructor + super.toString() + '}'; //NOI18N
        }
    }
    
    public static class DeclaratorBuilder implements CsmObjectBuilder {

        private int level = 0;
        private CharSequence name;
        private NameBuilder nameBuilder;
        
        public void setName(CharSequence name) {
            assert CharSequences.isCompact(name) : "only compact strings allowed";
            this.name = name;
        }

        public CharSequence getName() {
            return name;
        }
        
        public void enterDeclarator() {
            level++;
        }
        
        public void leaveDeclarator() {
            level--;
        }
        
        public boolean isTopDeclarator() {
            return level == 0;
        }

        public NameBuilder getNameBuilder() {
            return nameBuilder;
        }

        public void setNameBuilder(NameBuilder nameBuilder) {
            this.nameBuilder = nameBuilder;
        }

        @Override
        public String toString() {
            return "DeclaratorBuilder{" + "level=" + level + ", name=" + name + ", nameBuilder=" + nameBuilder + '}'; //NOI18N
        }
    }    
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
    }  
    
    protected OffsetableDeclarationBase(RepositoryDataInput input) throws IOException {
        super(input);
    }    

    @Override
    public String toString() {
        CharSequence name = toStringName();
        return "" + getKind() + ' ' + name  + getOffsetString() + getPositionString(); // NOI18N
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)){
            return false;
        }
        final OffsetableDeclarationBase<?> other = (OffsetableDeclarationBase<?>)obj;
        if (!this.getKind().equals(other.getKind())) {
            return false;
        }
        return getName().equals(other.getName());
    }

    @Override
    public int hashCode() {
        return  31*super.hashCode() + getName().hashCode();
    }

    protected boolean registerInProject() {
        //do nothing by default
        return false;
    }

    protected static<T> void postObjectCreateRegistration(boolean register, OffsetableDeclarationBase<T> obj) {
        // IZ#237907 initialize FQN
        obj.getUniqueName();
        if (register) {
            if (!obj.registerInProject()) {
                RepositoryUtils.put(obj);
            }
        } else {
            Utils.setSelfUID(obj);
        }
    }

    protected static<T> void temporaryRepositoryRegistration(boolean global, OffsetableDeclarationBase<T> obj) {
        if (global) {
            RepositoryUtils.hang(obj); // "hang" now and then "put" in "register()"
        } else {
            Utils.setSelfUID(obj);
        }
    }
}
