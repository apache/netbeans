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
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl.MemberBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.util.CharSequences;

/**
 * CsmVariable + CsmMember implementation
 */
public final class FieldImpl extends VariableImpl<CsmField> implements CsmField {

    private final CsmVisibility visibility;

    private FieldImpl(AST ast, CsmFile file, CsmType type, NameHolder name, ClassImpl cls, CsmVisibility visibility) {
        super(ast, file, type, name, cls, AstUtil.hasChildOfType(ast, CPPTokenTypes.LITERAL_static), AstUtil.hasChildOfType(ast, CPPTokenTypes.LITERAL_extern));
        this.visibility = visibility;
    }

    public static FieldImpl create(AST ast, CsmFile file, FileContent fileContent, CsmType type, AST templateAst, NameHolder name, ClassImpl cls, CsmVisibility visibility, boolean register) {
        TemplateDescriptor tplDescr = TemplateDescriptor.createIfNeededDirect(templateAst, file, cls, register);
        type = TemplateUtils.checkTemplateType(type, cls, tplDescr);
        FieldImpl fieldImpl = new FieldImpl(ast, file, type, name, cls, visibility);
        fieldImpl.setTemplateDescriptor(tplDescr);
        postObjectCreateRegistration(register, fieldImpl);
        name.addReference(fileContent, fieldImpl);
        return fieldImpl;
    }
    
    public static FieldImpl create(AST ast, CsmFile file, FileContent fileContent, CsmType type, AST templateAst, NameHolder name, ClassImpl cls, CsmVisibility visibility, boolean _static, boolean _extern, boolean register) {
        TemplateDescriptor tplDescr = TemplateDescriptor.createIfNeededDirect(templateAst, file, cls, register);
        type = TemplateUtils.checkTemplateType(type, cls, tplDescr);
        FieldImpl fieldImpl = new FieldImpl(ast, file, type, name, cls, visibility, _static, _extern);
        fieldImpl.setTemplateDescriptor(tplDescr);
        postObjectCreateRegistration(register, fieldImpl);
        name.addReference(fileContent, fieldImpl);
        return fieldImpl;
    }

    private FieldImpl(AST ast, CsmFile file, CsmType type, NameHolder name, ClassImpl cls, CsmVisibility visibility, boolean _static, boolean _extern) {
        super(ast, file, type, name, cls, _static, _extern);
        this.visibility = visibility;
    }

    private FieldImpl(CsmType type, CharSequence name, CsmScope cls, CsmVisibility visibility, boolean _static, boolean _extern, ExpressionBase initExpr, CsmFile file, int startOffset, int endOffset) {
        super(type, name, cls, _static, _extern, initExpr, file, startOffset, endOffset);
        this.visibility = visibility;
    }
    
    @Override
    public CsmClass getContainingClass() {
        return (CsmClass) getScope();
    }

    @Override
    public CsmVisibility getVisibility() {
        return visibility;
    }

    @Override
    public String toString() {
        return "FIELD " + super.toString(); // NOI18N
    }
    
    public static class FieldBuilder extends SimpleDeclarationBuilder implements MemberBuilder {
        
        private CharSequence name;// = CharSequences.empty();
        private CsmDeclaration.Kind kind = CsmDeclaration.Kind.CLASS;
        private CsmVisibility visibility = CsmVisibility.PUBLIC;
        private final FileContent fileContent;

        private CsmScope scope;
        private FieldImpl instance;

        public FieldBuilder(SimpleDeclarationBuilder builder, FileContent fileContent) {
            super(builder);
            assert fileContent != null;
            this.fileContent = fileContent;
        }
        
        public void setKind(Kind kind) {
            this.kind = kind;
        }
        
        @Override
        public void setName(CharSequence name) {
            if(this.name == null) {
                this.name = name;
            }
        }
        
        @Override
        public CharSequence getName() {
            return name;
        }
        
        @Override
        public CharSequence getRawName() {
            return NameCache.getManager().getString(CharSequences.create(name.toString().replace("::", "."))); //NOI18N
        }
        
        private FieldImpl getVariableInstance() {
            return instance;
        }

        @Override
        public void setVisibility(CsmVisibility visibility) {
            this.visibility = visibility;
        }
        
        @Override
        public void setScope(CsmScope scope) {
            assert scope != null;
            this.scope = scope;
        }
        
        @Override
        public CsmScope getScope() {
            if(scope != null) {
                return scope;
            }
            if (getParent() == null) {
                scope = (NamespaceImpl) getFile().getProject().getGlobalNamespace();
            } else {
                if(getParent() instanceof NamespaceDefinitionImpl.NamespaceBuilder) {
                    scope = ((NamespaceDefinitionImpl.NamespaceBuilder)getParent()).getNamespace();
                }
            }
            return scope;
        }
        
        @Override
        public FieldImpl create(CsmParserProvider.ParserErrorDelegate delegate) {
            FieldImpl field = getVariableInstance();
            CsmScope s = getScope();
            if (field == null && s != null && name != null && getScope() != null) {
                CsmType type = null;
                if(getTypeBuilder() != null) {
                    getTypeBuilder().setScope(s);
                    type = getTypeBuilder().create();
                }
                if(type == null) {
                    type = TypeFactory.createSimpleType(BuiltinTypes.getBuiltIn("int"), getFile(), getStartOffset(), getEndOffset()); // NOI18N
                }
                ExpressionBase initializer = null;
                if(getInitializerBuilder() != null) {
                    getInitializerBuilder().setScope(getScope());
                    initializer = getInitializerBuilder().create();
                }
                field = new FieldImpl(type, name, scope, visibility, isStatic(), isExtern(), initializer, getFile(), getStartOffset(), getEndOffset());
                
                postObjectCreateRegistration(true, field);
                NameHolder.createName(name).addReference(fileContent, field);
                //name.addReference(fileContent, field);
                
            }
            return field;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeVisibility(this.visibility, output);
    }

    public FieldImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.visibility = PersistentUtils.readVisibility(input);
    }
}
