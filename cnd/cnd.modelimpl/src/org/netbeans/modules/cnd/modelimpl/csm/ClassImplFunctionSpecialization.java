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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.InheritanceImpl.InheritanceBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.impl.services.InstantiationProviderImpl;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.util.CharSequences;
import org.openide.util.Exceptions;

/**
 * Template function specialization container.
 *
 */
public final class ClassImplFunctionSpecialization extends ClassImplSpecialization implements CsmTemplate {

    Collection<CsmInheritance> baseClasses = null;
    Collection<CsmMember> members = null;
    
    private ClassImplFunctionSpecialization(AST ast, NameHolder name, CsmFile file) {
        super(ast, name, file, getStartOffset(ast), getStartOffset(ast));
        // This instantiation is placed before method decl or def, so 
        // getStartOffset is called two times.
    }

    private ClassImplFunctionSpecialization(NameHolder name, CsmDeclaration.Kind kind, CsmFile file, int start, int end) {
        super(name, kind, file, start, end);
    }
    
    public static ClassImplFunctionSpecialization create(AST ast, CsmScope scope, CsmFile file, String language, String languageFlavor, FileContent fileContent, boolean register, MutableDeclarationsContainer container) throws AstRendererException {
        assert !APTLanguageSupport.getInstance().isLanguageC(language) : "Function specialization is not allowed in C"; // NOI18N
        
        NameHolder nameHolder = NameHolder.createName(getClassName(ast));
        ClassImplFunctionSpecialization impl = new ClassImplFunctionSpecialization(ast, nameHolder, file);
        impl.initQualifiedName(ast, scope, false, file);
        ClassImplFunctionSpecialization clsImpl = findExistingClassImplClassImplFunctionSpecializationInProject(file, impl);
        if (clsImpl != null) {
            impl = clsImpl;
        } else {
            impl.init(scope, ast, file, fileContent, language, languageFlavor, register, container);
            container.addDeclaration(impl);
        }
        nameHolder.addReference(fileContent, impl);
        return impl;
    }

    private static ClassImplFunctionSpecialization findExistingClassImplClassImplFunctionSpecializationInProject(CsmFile file, ClassImplFunctionSpecialization spec) {
        ClassImplFunctionSpecialization out = null;
        if (file != null) {
            CsmClassifier existing = file.getProject().findClassifier(spec.getQualifiedName());
            if (existing instanceof ClassImplFunctionSpecialization) {
                out = (ClassImplFunctionSpecialization) existing;
            }
        }
        return out;
    }

    private ClassImpl findBaseClassImplInProject() {
        ClassImpl out = null;
        CsmFile file = getContainingFile();
        if (file != null) {
            CsmClassifier base = file.getProject().findClassifier(getQualifiedNameWithoutSuffix());
            if (base instanceof ClassImpl) {
                out = (ClassImpl) base;
            }
        }
        return out;
    }

    @Override
    public void addMember(CsmMember member, boolean global) {
        String name = member.getQualifiedName().toString();
        for (CsmMember m : super.getMembers()) {
            if(name.equals(m.getQualifiedName().toString())) {
                return;
            }
        }
        super.addMember(member, global);
    }

    @Override
    public Collection<CsmMember> getMembers() {
        if(members == null) {
            members = _getMembers();
        }
        return members;        
    }
    
    public Collection<CsmMember> _getMembers() {
        Collection<CsmMember> members = new ArrayList<>();
        members.addAll(super.getMembers());        
        if (isValid()) {
            ClassImpl base = findBaseClassImplInProject();
            if(base != null && base != this) {
                CsmInstantiationProvider p = CsmInstantiationProvider.getDefault();
                if(p instanceof InstantiationProviderImpl) {
                    CsmObject baseInst = ((InstantiationProviderImpl)p).instantiate(base, this.getSpecializationParameters(), false);
                    if(CsmKindUtilities.isClass(baseInst)) {
                        members.addAll(((CsmClass)baseInst).getMembers());
                    }
                }
            }
        }
        return members;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<CsmMember> getMembers(CsmFilter filter) {
        ClassImpl base = findBaseClassImplInProject();
        if(base != null && base != this) {
            CsmInstantiationProvider p = CsmInstantiationProvider.getDefault();
            if(p instanceof InstantiationProviderImpl) {
                CsmObject baseInst = ((InstantiationProviderImpl)p).instantiate(base, this.getSpecializationParameters(), false);
                if(baseInst instanceof ClassImpl) {
                    return new MultiIterator<>(super.getMembers(filter), ((ClassImpl)baseInst).getMembers(filter));
                } else if(baseInst instanceof Instantiation.Class) {
                    return new MultiIterator<>(super.getMembers(filter), ((Instantiation.Class)baseInst).getMembers(filter));
                }

            }
        }
        return super.getMembers(filter);
    }

    @Override
    public Collection<CsmInheritance> getBaseClasses() {
        if(baseClasses == null) {
            baseClasses = _getBaseClasses();
        }
        return baseClasses;
    }
    
    public Collection<CsmInheritance> _getBaseClasses() {
        ClassImpl base = findBaseClassImplInProject();
        if(base != null && base != this) {
            return base.getBaseClasses();
            // we store inheritances in ClassifierContainer:218
            // and Instantiations are not persistable
//            CsmInstantiationProvider p = CsmInstantiationProvider.getDefault();
//            if(p instanceof InstantiationProviderImpl) {
//                CsmObject baseInst = ((InstantiationProviderImpl)p).instantiate(base, this.getSpecializationParameters(), getContainingFile(), getStartOffset(), false);
//                if(CsmKindUtilities.isClass(baseInst)) {
//                    return ((CsmClass)baseInst).getBaseClasses();
//                }
//            }
        }
        return Collections.<CsmInheritance>emptyList();
    }

    @Override
    public boolean isExplicitSpecialization() {
        return true;
    }

    private static CharSequence getClassName(AST ast) {
        CharSequence funName;
        if (CastUtils.isCast(ast)) {
            funName = CharSequences.create(CastUtils.getFunctionRawName(ast, APTUtils.SCOPE));
        } else {
            funName = CharSequences.create(AstUtil.findId(ast, CPPTokenTypes.RCURLY, true));
        }
        return getClassNameFromFunctionSpecialicationName(funName);
    }

    private static CharSequence getClassNameFromFunctionSpecialicationName(CharSequence functionName) {
        CharSequence[] nameParts = Utils.splitQualifiedName(functionName.toString());
        StringBuilder className = new StringBuilder("");
        for(int i = 0; i < nameParts.length - 1; i++) {
            if (FunctionImpl.OPERATOR.equals(nameParts[i].toString())) {
                break;
            }
            className.append(nameParts[i]);
        }
        return className;
    }

// #############################################################################
// # Commented because these specializations should not be inside declarations
// # and definitions of methods. 
// #############################################################################
//    public static int getStartOffset(AST node) {
//        AST id = AstUtil.findChildOfType(node, CPPTokenTypes.CSM_QUALIFIED_ID);
//        node = (id != null) ? id : node;
//        if( node != null ) {
//            OffsetableAST csmAst = AstUtil.getFirstOffsetableAST(node);
//            if( csmAst != null ) {
//                return csmAst.getOffset();
//            }
//        }
//        return 0;
//    }
//
//    public static int getEndOffset(AST node) {
//        AST id = AstUtil.findChildOfType(node, CPPTokenTypes.CSM_QUALIFIED_ID);
//        node = (id != null) ? id : node;
//        if( node != null ) {
//            AST child = node.getFirstChild();
//            if(child != null) {
//                AST gt = AstUtil.findLastSiblingOfType(child, CPPTokenTypes.GREATERTHAN);
//                if( gt instanceof CsmAST ) {
//                    return ((CsmAST) gt).getEndOffset();
//                }
//            }
//            AST lastChild = AstUtil.getLastChildRecursively(node);
//            if( lastChild instanceof CsmAST ) {
//                return ((CsmAST) lastChild).getEndOffset();
//            }
//        }
//        return 0;
//    }

    
    public static class ClassFunctionSpecializationBuilder extends ClassSpecializationBuilder {

        private ClassImplFunctionSpecialization instance;
        
        private ClassImplFunctionSpecialization getInstance() {
            if(instance != null) {
                return instance;
            }
            
            CsmClassifier cls = getFile().getProject().findClassifier(getName());
            if (cls instanceof ClassImplFunctionSpecialization) {
                instance = (ClassImplFunctionSpecialization) cls;
            }
            return instance;
        }
        
        @Override
        public ClassImpl create(CsmParserProvider.ParserErrorDelegate delegate) {
            ClassImplFunctionSpecialization impl = getInstance();
            if (impl == null) {
                instance = impl = new ClassImplFunctionSpecialization(getNameHolder(), getKind(), getFile(), getStartOffset(), getEndOffset());
                try {
                    impl.init2(getSpecializationDescriptor(), NameCache.getManager().getString(CharSequences.create("")), getScope(), getFile(), getFileContent(), isGlobal()); // NOI18N
                } catch (AstRendererException ex) {
                    Exceptions.printStackTrace(ex);
                }
                
                if(getTemplateDescriptorBuilder() != null) {
                    impl.setTemplateDescriptor(getTemplateDescriptor());
                }
                for (InheritanceBuilder inheritanceBuilder : getInheritanceBuilders()) {
                    inheritanceBuilder.setScope(impl);
                    impl.addInheritance(inheritanceBuilder.create(), isGlobal());
                }
                for (MemberBuilder builder : getMemberBuilders()) {
                    builder.setScope(impl);
                    final CsmMember member = builder.create(delegate);
                    if (member != null) {
                        impl.addMember(member, isGlobal());
                    } else {
                        CsmParserProvider.registerParserError(delegate, "Skip unrecognized member for builder '"+builder, getFile(), getStartOffsetImpl(builder, this)); //NOI18N
                    }
                }                
                addDeclaration(impl);
            }
            getNameHolder().addReference(getFileContent(), impl);
            return impl;
        }
        
    }    
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
    }

    public ClassImplFunctionSpecialization(RepositoryDataInput input) throws IOException {
        super(input);
    }

    private static class MultiIterator<T> implements Iterator<T> {

        private int currentIterator;
        private final Iterator<T> iterators[];

        public MultiIterator(Iterator<T>... iterators) {
            this.iterators = iterators;
            this.currentIterator = 0;
        }

        @Override
        public T next() {
            while (currentIterator < iterators.length && !iterators[currentIterator].hasNext()) {
                currentIterator++;
            }
            return iterators[currentIterator].next();
        }

        @Override
        public boolean hasNext() {
            while (currentIterator < iterators.length && !iterators[currentIterator].hasNext()) {
                currentIterator++;
            }
            return currentIterator < iterators.length;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }
    }

}
