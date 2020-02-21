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

import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver3;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.ResolverFactory;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl.MemberBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceDefinitionImpl.NamespaceBuilder;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.openide.util.CharSequences;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * Implements CsmUsingDeclaration
 */
public final class UsingDeclarationImpl extends OffsetableDeclarationBase<CsmUsingDeclaration>
        implements CsmUsingDeclaration, CsmMember, RawNamable, Disposable {

    private final CharSequence name;
    private final CharSequence rawName;
    // TODO: don't store declaration here since the instance might change
    private CsmUID<CsmDeclaration> referencedDeclarationUID = null;
    private WeakReference<CsmDeclaration> refDeclaration;
    private int lastParseCount = -1;
    private final CsmUID<CsmScope> scopeUID;
    private final CsmVisibility visibility;
    
    private UsingDeclarationImpl(AST ast, CsmFile file, CsmScope scope, CsmVisibility visibility) {
        this(NameCache.getManager().getString(AstUtil.getText(ast)), 
                AstUtil.getRawNameInChildren(ast), 
                scope, 
                visibility, 
                file, getUsingDeclarationStartOffset(ast), getEndOffset(ast));
    }

    private UsingDeclarationImpl(CharSequence name, CharSequence rawName, CsmScope scope, CsmVisibility visibility, CsmFile file, int startOffset, int endOffset) {
        super(file, startOffset, endOffset);
        this.scopeUID = UIDCsmConverter.scopeToUID(scope);
        this.name = name;
        this.rawName = rawName;
        this.visibility = visibility;
    }
    
    public static UsingDeclarationImpl create(AST ast, CsmFile file, CsmScope scope, boolean global, CsmVisibility visibility) {
        UsingDeclarationImpl usingDeclarationImpl = new UsingDeclarationImpl(ast, file, scope, visibility);
        if (!global) {
            Utils.setSelfUID(usingDeclarationImpl);
        }
        return usingDeclarationImpl;
    }

    private static int getUsingDeclarationStartOffset(AST ast) {
        int startOffset;
        AST child = ast.getFirstChild();
        if (child instanceof CsmAST) {
            startOffset = ((CsmAST) child).getOffset();
        } else {
            startOffset = getStartOffset(ast);
        }
        return startOffset;
    }
    
    private CsmDeclaration renderReferencedDeclaration() {
        CsmDeclaration referencedDeclaration = null;
        CharSequence[] aRawName = getRawName();
        if (aRawName != null) {
            ProjectBase prjBase = (ProjectBase)getProject();
            CsmNamespace namespace = null;
            if (aRawName.length == 1) {
                namespace = prjBase.getGlobalNamespace();
            } else if (aRawName.length > 1) {
                CharSequence[] partial = new CharSequence[aRawName.length - 1];
                System.arraycopy(aRawName, 0, partial, 0, aRawName.length - 1);
                CsmObject result = null;
                Resolver aResolver = ResolverFactory.createResolver(this);
                try {
                    if (!aResolver.isRecursionOnResolving(Resolver.INFINITE_RECURSION)) {
                        result = aResolver.resolve(partial, Resolver.NAMESPACE);
                    } else {
                        result = null;
                    }
                } finally {
                    ResolverFactory.releaseResolver(aResolver);
                }
                if (CsmKindUtilities.isNamespace(result)) {
                    namespace = (CsmNamespace)result;
                }
            }
            if (namespace != null) {
                CharSequence lastName = aRawName[aRawName.length - 1];
                CsmDeclaration bestChoice = null;
                CsmFilter filter = CsmSelect.getFilterBuilder().createNameFilter(lastName, true, true, false);

                // we should try searching not only in namespace resolved found,
                // but in numspaces with the same name in required projects
                // iz #140787 cout, endl unresolved in some Loki files
                final Collection<CsmProject> libraries;
                libraries = Resolver3.getSearchLibraries(prjBase);
                Collection<CsmNamespace> namespacesToSearch = new LinkedHashSet<>();
                namespacesToSearch.add(namespace);
                namespacesToSearch.addAll(CsmBaseUtilities.getInlinedNamespaces(namespace, libraries));
                CharSequence nspQName = namespace.getQualifiedName();
                for (CsmProject lib : libraries) {
                    CsmNamespace libNs = lib.findNamespace(nspQName);
                    if (libNs != null) {
                        namespacesToSearch.add(libNs);
                    }
                }

                outer:
                for (CsmNamespace curr : namespacesToSearch) {
                    Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDeclarations(curr, filter);
                    while (it.hasNext()) {
                        CsmDeclaration elem = it.next();
                        if (CharSequences.comparator().compare(lastName,elem.getName())==0) {
                            if (!CsmKindUtilities.isExternVariable(elem) 
                                 && !CsmKindUtilities.isClassForwardDeclaration(elem) 
                                 && !ForwardClass.isForwardClass(elem)) 
                            {
                                referencedDeclaration = elem;
                                break outer;
                            } else {
                                bestChoice = elem;
                            }
                        }
                    }
                }

                // search for enumerators
                if (referencedDeclaration == null && bestChoice == null) {
                    CsmFilter filter2 = CsmSelect.getFilterBuilder().createKindFilter(new Kind[]{Kind.ENUM});
                    outer2:
                    for (CsmNamespace curr : namespacesToSearch) {
                        Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDeclarations(curr, filter2);
                        while (it.hasNext()) {
                            CsmDeclaration elem = it.next();
                            if (CsmKindUtilities.isEnum(elem)) {
                                CsmEnum e = (CsmEnum) elem;
                                if (!e.isStronglyTyped()) {
                                    for (CsmEnumerator enumerator : e.getEnumerators()) {
                                        if(lastName.toString().equals(enumerator.getName().toString())) {
                                            referencedDeclaration = enumerator;
                                            break outer2;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                referencedDeclaration = referencedDeclaration == null ? bestChoice : referencedDeclaration;
            }
            CsmClass cls = null;
            if(namespace == null && aRawName.length > 1) {
                CharSequence[] partial = new CharSequence[aRawName.length - 1];
                System.arraycopy(aRawName, 0, partial, 0, aRawName.length - 1);
                CsmObject result = null;
                Resolver aResolver = ResolverFactory.createResolver(this);
                try {
                    // TODO: this is not correct - qualified name doesn't contain template parameters.
                    // We need to store them and use CsmExpressionResolver to resolve qualified name
                    result = aResolver.resolve(partial, Resolver.CLASSIFIER);
                    if (CsmKindUtilities.isClassifier(result)) {
                        result = aResolver.getOriginalClassifier((CsmClassifier) result);
                    }
                } finally {
                    ResolverFactory.releaseResolver(aResolver);
                }
                if (CsmKindUtilities.isClass(result)) {
                    cls = (CsmClass)result;
                }
            }
            if(cls != null && aRawName.length > 0) {
                CharSequence lastName = aRawName[aRawName.length - 1];
                CsmFilter filter = CsmSelect.getFilterBuilder().createNameFilter(lastName, true, true, false);
                Iterator<CsmMember> it = CsmSelect.getClassMembers(cls, filter);
                if (it.hasNext()) {
                    CsmMember member = it.next();
                    referencedDeclaration = member;
                }
            }

        }
        return referencedDeclaration;
    }

    @Override
    public CsmDeclaration getReferencedDeclaration() {
        // TODO: process preceding aliases
        // TODO: process non-class elements
//        if (!Boolean.getBoolean("cnd.modelimpl.resolver"))
        CsmDeclaration referencedDeclaration = _getReferencedDeclaration();
        if (referencedDeclaration == null) {
            int newParseCount = FileImpl.getParseCount();
            if (lastParseCount != newParseCount) {
                referencedDeclaration = renderReferencedDeclaration();
                synchronized (this) {
                    lastParseCount = newParseCount;
                    _setReferencedDeclaration(referencedDeclaration);
                }
            }            
        }
        return referencedDeclaration;
    }
    
    private CsmDeclaration _getReferencedDeclaration() {
        CsmDeclaration referencedDeclaration = null;
        WeakReference<CsmDeclaration> aRefDeclaration = refDeclaration;
        if (aRefDeclaration != null) {
            referencedDeclaration =((Reference<CsmDeclaration>)aRefDeclaration).get();
        }
        if (referencedDeclaration == null) {
            referencedDeclaration = UIDCsmConverter.UIDtoDeclaration(referencedDeclarationUID);
            refDeclaration = new WeakReference<>(referencedDeclaration);
        }
        if (referencedDeclarationUID == null) {
            CsmFile csmFile = getContainingFile();
            if(csmFile instanceof FileImpl) {
                FileImpl fileImpl = (FileImpl) csmFile;
                CsmReference typeReference = fileImpl.getResolvedReference(new CsmUsingReferenceImpl(this));
                if (typeReference != null) {
                    CsmObject referencedObject = typeReference.getReferencedObject();
                    if (CsmKindUtilities.isDeclaration(referencedObject)) {
                        referencedDeclaration = (CsmDeclaration) referencedObject;
                        refDeclaration = new WeakReference<>(referencedDeclaration);
                        //System.out.println("Hit "+referencedDeclaration);
                    }
                }
            }
        }
        // can be null if namespace was removed 
        return referencedDeclaration;
    }    

    private void _setReferencedDeclaration(CsmDeclaration referencedDeclaration) {
        CsmFile csmFile = getContainingFile();
        if(csmFile instanceof FileImpl) {
            FileImpl fileImpl = (FileImpl) csmFile;
            fileImpl.removeResolvedReference(new CsmUsingReferenceImpl(this));
            refDeclaration = referencedDeclaration == null ? null : new WeakReference<>(referencedDeclaration);
            this.referencedDeclarationUID = UIDCsmConverter.declarationToUID(referencedDeclaration);
            if (referencedDeclarationUID != null && referencedDeclaration != null && CsmBaseUtilities.isValid(referencedDeclaration)) {
                fileImpl.addResolvedReference(new CsmUsingReferenceImpl(this), referencedDeclaration);
            }
            assert this.referencedDeclarationUID != null || referencedDeclaration == null;
        }
    }

    @Override
    public CsmClass getContainingClass() {
        CsmScope scope = getScope();
        if(CsmKindUtilities.isClass(scope)) {
            return (CsmClass) scope;
        }
        return null;
    }

    @Override
    public CsmVisibility getVisibility() {
        return visibility;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.USING_DECLARATION;
    }
    
    @Override
    public CharSequence getName() {
        return name;
    }
    
    @Override
    public CharSequence getQualifiedName() {
        return getName();
    }
    
    @Override
    public CharSequence[] getRawName() {
        return AstUtil.toRawName(rawName);
    }
    
    @Override
    public CsmScope getScope() {
        return  UIDCsmConverter.UIDtoScope(this.scopeUID);
    }

    @Override
    public void dispose() {
        super.dispose();
        CsmScope scope = getScope();
        if( scope instanceof MutableDeclarationsContainer ) {
            ((MutableDeclarationsContainer) scope).removeDeclaration(this);
        }
    }
    
    public static class UsingDeclarationBuilder implements CsmObjectBuilder, MemberBuilder {
        
        private CharSequence name;// = CharSequences.empty();
        private int nameStartOffset;
        private int nameEndOffset;
        private CsmDeclaration.Kind kind = CsmDeclaration.Kind.CLASS;
        private CsmFile file;
        private final FileContent fileContent;
        private int startOffset;
        private int endOffset;
        CsmVisibility visibility;
        private CsmObjectBuilder parent;

        private CsmScope scope;
        private UsingDeclarationImpl instance;
        private final List<CsmOffsetableDeclaration> declarations = new ArrayList<>();

        public UsingDeclarationBuilder(FileContent fileContent) {
            assert fileContent != null;
            this.fileContent = fileContent;
        }
        
        public void setKind(Kind kind) {
            this.kind = kind;
        }
        
        public void setName(CharSequence name, int startOffset, int endOffset) {
            if(this.name == null) {
                this.name = name;
                this.nameStartOffset = startOffset;
                this.nameEndOffset = endOffset;
            }
        }

        @Override
        public void setVisibility(CsmVisibility visibility) {
            this.visibility = visibility;
        }
        
        public CharSequence getName() {
            return name;
        }
        
        public CharSequence getRawName() {
            return NameCache.getManager().getString(CharSequences.create(name.toString().replace("::", "."))); //NOI18N
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

        public void setParent(CsmObjectBuilder parent) {
            this.parent = parent;
        }

        public void addDeclaration(CsmOffsetableDeclaration decl) {
            this.declarations.add(decl);
        }
        
        private UsingDeclarationImpl getUsingDeclarationInstance() {
            if(instance != null) {
                return instance;
            }
            MutableDeclarationsContainer container = null;
            if (parent == null) {
                container = fileContent;
            } else {
                if(parent instanceof NamespaceDefinitionImpl.NamespaceBuilder) {
                    container = ((NamespaceDefinitionImpl.NamespaceBuilder)parent).getNamespaceDefinitionInstance();
                }
            }
            if(container != null && name != null) {
                CsmOffsetableDeclaration decl = container.findExistingDeclaration(startOffset, name, kind);
                if (decl != null && UsingDeclarationImpl.class.equals(decl.getClass())) {
                    instance = (UsingDeclarationImpl) decl;
                }
            }
            return instance;
        }
        
        @Override
        public void setScope(CsmScope scope) {
            assert scope != null;
            this.scope = scope;
        }
        
        public CsmScope getScope() {
            if(scope != null) {
                return scope;
            }
            if (parent == null) {
                scope = (NamespaceImpl) file.getProject().getGlobalNamespace();
            } else {
                if(parent instanceof NamespaceDefinitionImpl.NamespaceBuilder) {
                    scope = ((NamespaceDefinitionImpl.NamespaceBuilder)parent).getNamespace();
                }
            }
            return scope;
        }
        
        @Override
        public UsingDeclarationImpl create(CsmParserProvider.ParserErrorDelegate delegate) {
            UsingDeclarationImpl using = getUsingDeclarationInstance();
            CsmScope s = getScope();
            if (using == null && s != null && name != null && getScope() != null) {
                using = new UsingDeclarationImpl(name, getRawName(), scope, visibility, file, startOffset, endOffset);
                if(parent != null) {
                    if(parent instanceof NamespaceBuilder) {
                        ((NamespaceDefinitionImpl.NamespaceBuilder)parent).addDeclaration(using);
                    }
                } else {
                    fileContent.addDeclaration(using);
                }
            }
            if(getScope() instanceof CsmNamespace) {
                ((NamespaceImpl)getScope()).addDeclaration(using);
            }
            return using;
        }
    }        

    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent
    
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        PersistentUtils.writeUTF(name, output);
        PersistentUtils.writeUTF(this.rawName, output);
        
        // save cached declaration
        UIDObjectFactory.getDefaultFactory().writeUID(this.referencedDeclarationUID, output);
        UIDObjectFactory.getDefaultFactory().writeUID(this.scopeUID, output);

        PersistentUtils.writeVisibility(this.visibility, output);
    }
    
    public UsingDeclarationImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.name = PersistentUtils.readUTF(input, NameCache.getManager());
        assert this.name != null;
        this.rawName = PersistentUtils.readUTF(input, NameCache.getManager());
        
        // read cached declaration
        this.referencedDeclarationUID = UIDObjectFactory.getDefaultFactory().readUID(input);        
        this.scopeUID = UIDObjectFactory.getDefaultFactory().readUID(input);

        this.visibility = PersistentUtils.readVisibility(input);
    }
    
    private static class CsmUsingReferenceImpl implements CsmReference {
        private final UsingDeclarationImpl using;

        public CsmUsingReferenceImpl(UsingDeclarationImpl using) {
            this.using = using;
        }

        @Override
        public CsmReferenceKind getKind() {
            return CsmReferenceKind.DIRECT_USAGE;
        }

        @Override
        public CsmObject getReferencedObject() {
            return null;
        }

        @Override
        public CsmObject getOwner() {
            return null;
        }

        @Override
        public CsmObject getClosestTopLevelObject() {
            return null;
        }

        @Override
        public CsmFile getContainingFile() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getStartOffset() {
            return using.getStartOffset();
        }

        @Override
        public int getEndOffset() {
            return using.getEndOffset();
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
            return using.getName();
        }

        @Override
        public String toString() {
            return using.getName()+"["+using.getStartOffset()+","+using.getEndOffset()+"]"; //NOI18N
        }
    }
}
