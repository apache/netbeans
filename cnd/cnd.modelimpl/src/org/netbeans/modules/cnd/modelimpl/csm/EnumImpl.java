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

import java.util.* ;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl.MemberBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.EnumeratorImpl.EnumeratorBuilder;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.impl.services.SelectImpl;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 * Implements CsmEnum
 */
public class EnumImpl extends ClassEnumBase<CsmEnum> implements CsmEnum, SelectImpl.FilterableEnumerators {
    private final boolean stronglyTyped;
    private final List<CsmUID<CsmEnumerator>> enumerators;
    
    private EnumImpl(AST ast, NameHolder name, CsmFile file) {
        super(name, file, ast);
        this.stronglyTyped = isStronglyTypedEnum(ast);
        enumerators = new ArrayList<>();
    }

    protected EnumImpl(CharSequence name, CharSequence qName, boolean stronglyTyped, CsmFile file, int startOffset, int endOffset) {
        super(name, qName, file, startOffset, endOffset);
        this.stronglyTyped = stronglyTyped;
        enumerators = new ArrayList<>();
    }
    
    public void init(CsmScope scope, AST ast, final CsmFile file, boolean register) {
	initScope(scope);
//        initEnumeratorList(ast, file, register);
        if (register) {
            register(scope, true);
        }
    }
    
    public static EnumImpl create(AST ast, CsmScope scope, final CsmFile file, FileContent fileContent, boolean register) {
        NameHolder nameHolder = NameHolder.createEnumName(ast);
	EnumImpl impl = new EnumImpl(ast, nameHolder, file);
	impl.init2(scope, ast, file, fileContent, register);
        nameHolder.addReference(fileContent, impl);
	return impl;
    }
    
    void init2(CsmScope scope, AST ast, final CsmFile file, FileContent fileContent, boolean register) {
	initScope(scope);
        temporaryRepositoryRegistration(register, this);
        initEnumDefinition(scope);
        initEnumeratorList(ast, file, fileContent, register);
        if (register) {
            register(scope, true);
        }
    }    

    private void initEnumDefinition(CsmScope scope) {
        ClassImpl.MemberForwardDeclaration mfd = findMemberForwardDeclaration(scope);
        if (mfd instanceof ClassImpl.EnumMemberForwardDeclaration && CsmKindUtilities.isEnum(this)) {
            ClassImpl.EnumMemberForwardDeclaration fd = (ClassImpl.EnumMemberForwardDeclaration) mfd;
            fd.setCsmEnum((CsmEnum) this);
            CsmClass containingClass = fd.getContainingClass();
            if (containingClass != null) {
                // this is our real scope, not current namespace
                initScope(containingClass);
            }
        }
    }

    void addEnumerator(String name, int startOffset, int endOffset, boolean register) {
        EnumeratorImpl ei = EnumeratorImpl.create(this, name, startOffset, endOffset, register);
        CsmUID<CsmEnumerator> uid = UIDCsmConverter.<CsmEnumerator>objectToUID(ei);
        synchronized(enumerators) {
            enumerators.add(uid);
        }
    }

    void addEnumerator(EnumeratorImpl ei) {
        CsmUID<CsmEnumerator> uid = UIDCsmConverter.<CsmEnumerator>objectToUID(ei);
        synchronized(enumerators) {
            enumerators.add(uid);
        }
    }

    @Override
    public String toString() {
        if (stronglyTyped) {
            return "[Strongly Typed]" + super.toString(); // NOI18N
        } else {
            return super.toString();
        }
    }

    public final void fixFakeRender(FileContent fileContent, AST ast, boolean localClass) {
        initEnumeratorList(ast, fileContent.getFile(), fileContent, !localClass);
    }
    
    private void initEnumeratorList(AST ast, final CsmFile file, FileContent fileContent, boolean global){
        //enum A { a, b, c };
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            if( token.getType() == CPPTokenTypes.CSM_ENUMERATOR_LIST ) {
                addList(token, file, fileContent, global);
                return;
            }
        }
        AST token = ast.getNextSibling();
        if( token != null) {
            AST enumList = null;
            if (token.getType() == CPPTokenTypes.IDENT) {
                //typedef enum C { a2, b2, c2 } D;
                token = token.getNextSibling();
            }
            if (token.getType() == CPPTokenTypes.LCURLY ) {
                //typedef enum { a1, b1, c1 } B;
                enumList = token.getNextSibling();
            }
            if (enumList != null && enumList.getType() == CPPTokenTypes.CSM_ENUMERATOR_LIST) {
                addList(enumList, file, fileContent, global);
            }
        }
    }
    
    private void addList(AST token, final CsmFile file, FileContent fileContent, boolean global){
        for( AST t = token.getFirstChild(); t != null; t = t.getNextSibling() ) {
            if( t.getType() == CPPTokenTypes.IDENT ) {
                EnumeratorImpl ei = EnumeratorImpl.create(t, file, fileContent, this, global);
                CsmUID<CsmEnumerator> uid = UIDCsmConverter.<CsmEnumerator>objectToUID(ei);
                synchronized(enumerators) {
                    enumerators.add(uid);
                }
            }
        }
    }

    @Override
    public boolean isStronglyTyped() {
        return stronglyTyped;
    }

    @Override
    public Collection<CsmEnumerator> getEnumerators() {
        synchronized(enumerators) {
            return UIDCsmConverter.UIDsToDeclarations(enumerators);
        }
    }
    
    @Override
    public Iterator<CsmEnumerator> getEnumerators(CsmSelect.CsmFilter filter) {
        Collection<CsmUID<CsmEnumerator>> uids = new ArrayList<>();
        synchronized (enumerators) {
            uids.addAll(enumerators);
        }
        return UIDCsmConverter.UIDsToDeclarations(uids, filter);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        return (Collection)getEnumerators();
    }
    
    @Override
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.ENUM;
    }
    
    @Override
    public void dispose() {
        super.dispose();
        _clearEnumerators();
    }
    
    private void _clearEnumerators() {
        synchronized(enumerators) {
            Collection<CsmEnumerator> enumers = getEnumerators();
            Utils.disposeAll(enumers);
            RepositoryUtils.remove(enumerators);
        }
    }

    static boolean isStronglyTypedEnum(AST ast) {
        assert ast.getType() == CPPTokenTypes.CSM_ENUM_DECLARATION ||
               ast.getType() == CPPTokenTypes.CSM_ENUM_FWD_DECLARATION ||
                ast.getType() == CPPTokenTypes.LITERAL_enum : ast;
        if (ast.getType() == CPPTokenTypes.CSM_ENUM_DECLARATION ||
            ast.getType() == CPPTokenTypes.CSM_ENUM_FWD_DECLARATION) {
            AST child = ast.getFirstChild();
            if (child == null) {
                CndUtils.assertTrueInConsole(false, "incomplete enum ", ast);
                return false;
            }
            ast = child;
        }
        while (ast.getType() != CPPTokenTypes.LITERAL_enum) {
            AST sibling = ast.getNextSibling();
            if (sibling == null) {
                CndUtils.assertTrueInConsole(false, "incomplete enum ", ast);
                return false;
            }
            ast = sibling;
        }
        assert ast.getType() == CPPTokenTypes.LITERAL_enum : ast;
        if (ast.getType() == CPPTokenTypes.LITERAL_enum) {
            AST nextSibling = ast.getNextSibling();
            if (nextSibling != null) {
                switch (nextSibling.getType()) {
                    case CPPTokenTypes.LITERAL_struct:
                    case CPPTokenTypes.LITERAL_class:
                        return true;
                }
            }
        }
        return false;
    }

    public static class EnumBuilder extends SimpleDeclarationBuilder implements MemberBuilder {
        
        private boolean stronglyTyped = false;
        private final List<EnumeratorBuilder> enumeratorBuilders = new ArrayList<>();
        
        private EnumImpl instance;
        private CsmVisibility visibility = CsmVisibility.PUBLIC;
        
        
        public void setStronglyTyped() {
            this.stronglyTyped = true;
        }

        public void addEnumerator(EnumeratorBuilder eb) {
            enumeratorBuilders.add(eb);
        }
        
        @Override
        public void setVisibility(CsmVisibility visibility) {
            this.visibility = visibility;
        }
        
        public EnumImpl getEnumDefinitionInstance() {
            if(instance != null) {
                return instance;
            }
            MutableDeclarationsContainer container = null;
            if (getParent() == null) {
                container = getFileContent();
            } else {
                if(getParent() instanceof NamespaceDefinitionImpl.NamespaceBuilder) {
                    container = ((NamespaceDefinitionImpl.NamespaceBuilder)getParent()).getNamespaceDefinitionInstance();
                }
            }
            if(container != null && getName() != null) {
                CsmOffsetableDeclaration decl = container.findExistingDeclaration(getStartOffset(), getName(), Kind.ENUM);
                if (decl != null && EnumImpl.class.equals(decl.getClass())) {
                    instance = (EnumImpl) decl;
                }
            }
            return instance;
        }

        @Override
        public CharSequence getName() {
            return super.getName() == null ? getNameHolder().getName() : super.getName();
        }
        
        @Override
        public EnumImpl create(CsmParserProvider.ParserErrorDelegate delegate) {
            EnumImpl impl = getEnumDefinitionInstance();
            if(impl == null) {
                impl = new EnumImpl(getName(), getName(), stronglyTyped, getFile(), getStartOffset(), getEndOffset());
                impl.setVisibility(visibility);
                impl.initScope(getScope());
                impl.register(getScope(), true);
                getNameHolder().addReference(getFileContent(), impl);
                OffsetableDeclarationBase.temporaryRepositoryRegistration(true, impl);
                
                for (EnumeratorBuilder enumeratorBuilder : enumeratorBuilders) {
                    enumeratorBuilder.setEnum(impl);
                    EnumeratorImpl ei = enumeratorBuilder.create(true);
                    impl.addEnumerator(ei);
                }
                
                addDeclaration(impl);
            }
            return impl;
        }
    }
    
////////////////////////////////////////////////////////////////////////////
// impl of SelfPersistent
    
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        output.writeBoolean(stronglyTyped);
        UIDObjectFactory.getDefaultFactory().writeUIDCollection(this.enumerators, output, true);
    }
    
    public EnumImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.stronglyTyped = input.readBoolean();
        int collSize = input.readInt();
        if (collSize < 0) {
            enumerators = new ArrayList<>(0);
        } else {
            enumerators = new ArrayList<>(collSize);
        }
        UIDObjectFactory.getDefaultFactory().readUIDCollection(this.enumerators, input, collSize);
    }
}
