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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardClass.ForwardClassBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.TemplateDescriptor.TemplateDescriptorBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.ResolverFactory;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.CharSequences;

/**
 *
 */
public class ClassForwardDeclarationImpl extends OffsetableDeclarationBase<CsmClassForwardDeclaration> 
                                         implements CsmClassForwardDeclaration, CsmTemplate {
    private final CharSequence name;
    private CharSequence[] nameParts;
    private int lastParseCount = -1;    
    private int lastFileID = -1;
    private CsmObject lastResult;

    private final TemplateDescriptor templateDescriptor;
    
    protected ClassForwardDeclarationImpl(AST ast, CsmFile file, boolean global) {
        this(ast, file, global, getClassForwardStartOffset(ast), getClassForwardEndOffset(ast));
    }
    
    protected ClassForwardDeclarationImpl(AST ast, CsmFile file, boolean global, int startOffset, int endOffset) {
        super(file, startOffset, endOffset);
        AST qid = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
        assert qid != null;
        name = QualifiedNameCache.getManager().getString(AstRenderer.getQualifiedName(qid));
        nameParts = initNameParts(qid);
        this.templateDescriptor = TemplateDescriptor.createIfNeeded(ast, file, this, global);
    }

    protected ClassForwardDeclarationImpl(CharSequence name, TemplateDescriptor templateDescriptor, CsmFile file, int startOffset, int endOffset) {
        super(file, startOffset, endOffset);
        this.name = name;
        this.nameParts = Utils.splitQualifiedName(name.toString());
        this.templateDescriptor = templateDescriptor;
    }
    
    public static ClassForwardDeclarationImpl create(AST ast, CsmFile file, CsmScope scope, MutableDeclarationsContainer container, boolean global) {
        return create(ast, file, scope, container, global, false);
    }
    
    public static ClassForwardDeclarationImpl create(AST ast, CsmFile file, CsmScope scope, MutableDeclarationsContainer container, boolean global, boolean usedAsType) {
        int startOffset = getClassForwardStartOffset(ast);
        int endOffset = getClassForwardEndOffset(ast);
        
        // this is a hack to avoid possible registering one declaration inside another
        ClassForwardDeclarationImpl cfdi = new ClassForwardDeclarationImpl(ast, file, global, usedAsType ? -1 : startOffset, usedAsType ? -1 : endOffset);
        
        if (container != null) {
            container.addDeclaration(cfdi);
        }
        
        cfdi.init(ast, scope, global, startOffset, endOffset);
        return cfdi;
    }

    public static ForwardClass createForwardClassIfNeeded(AST ast, CsmFile file, CsmScope scope, MutableDeclarationsContainer container, boolean global) {
        ClassForwardDeclarationImpl cfdi = new ClassForwardDeclarationImpl(ast, file, false);
        return cfdi.createForwardClassIfNeed(ast, scope, global);
    }

    public static ClassForwardDeclarationImpl create(AST ast, CsmFile file, boolean global) {
         ClassForwardDeclarationImpl cfdi = new ClassForwardDeclarationImpl(ast, file, global);
         if (!global) {
              Utils.setSelfUID(cfdi);
         }
        return cfdi;
    }

    /*package*/ static int getClassForwardStartOffset(AST ast) {        
        AST firstChild = ast.getFirstChild();
        if (firstChild != null && firstChild.getType() == CPPTokenTypes.LITERAL_typedef) {
            AST secondChild = firstChild.getNextSibling();
            if (secondChild != null &&
                    (secondChild.getType() == CPPTokenTypes.LITERAL_struct ||
                    secondChild.getType() == CPPTokenTypes.LITERAL_union ||
                    secondChild.getType() == CPPTokenTypes.LITERAL_class)) {
                return getStartOffset(secondChild);
            }
        } else if (firstChild != null && firstChild.getType() == CPPTokenTypes.LITERAL_using) {
            AST assign = AstUtil.findSiblingOfType(firstChild, CPPTokenTypes.ASSIGNEQUAL);
            if (assign != null && assign.getNextSibling() != null) {
                switch (assign.getNextSibling().getType()) {
                    case CPPTokenTypes.LITERAL_class:
                    case CPPTokenTypes.LITERAL_struct:
                    case CPPTokenTypes.LITERAL_union:
                        return getStartOffset(assign.getNextSibling());
                }
            }
        }
        return getStartOffset(ast);        
    }
    
    /*package*/ static int getClassForwardEndOffset(AST ast) {
        AST firstChild = AstRenderer.getFirstChildSkipQualifiers(ast);
        if (firstChild != null) {
            if(firstChild.getType() == CPPTokenTypes.LITERAL_typedef) {
                AST qid = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
                if(qid != null) {
                    return getEndOffset(qid);
                }
            } else {
                if (firstChild.getType() == CPPTokenTypes.LITERAL_friend) {
                    if (firstChild.getNextSibling() != null) {
                        firstChild = firstChild.getNextSibling();
                    }
                }
                if (firstChild.getType() == CPPTokenTypes.LITERAL_class ||
                    firstChild.getType() == CPPTokenTypes.LITERAL_struct ||
                    firstChild.getType() == CPPTokenTypes.LITERAL_union) {
                    AST qid = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
                    if(qid != null) {
                        AST nextSibling = qid.getNextSibling();
                        if(nextSibling != null && nextSibling.getType() == CPPTokenTypes.SEMICOLON) {
                            return getEndOffset(nextSibling);
                        } else {
                            return getEndOffset(qid);
                        }
                    }                
                }
            }
        }
        return getEndOffset(ast);        
    }
    
    @Override
    public CsmScope getScope() {
        return getContainingFile();
    }

    @Override
    public CharSequence getName() {
        return name;
    }
    
    @Override
    public CharSequence getQualifiedName() {
        return name;
    }

//    Moved to OffsetableDeclarationBase
//    public String getUniqueName() {
//        return getQualifiedName();
//    }
    
    @Override
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION;
    }

    @Override
    public CsmClass getCsmClass() {
        CsmObject o = resolve();
        return (o instanceof CsmClass) ? (CsmClass) o : (CsmClass) null;
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
    public List<CsmTemplateParameter> getTemplateParameters() {
        return (templateDescriptor != null) ? templateDescriptor.getTemplateParameters() : Collections.<CsmTemplateParameter>emptyList();
    }

    @Override
    public CharSequence getDisplayName() {
        return (templateDescriptor != null) ? CharSequences.create(CharSequenceUtils.concatenate(getName(), templateDescriptor.getTemplateSuffix())) : getName(); // NOI18N
    }

    private CharSequence[] initNameParts(AST qid) {
        if( qid != null ) {
            return AstRenderer.getNameTokens(qid);
        }
        return new CharSequence[0];
    }
    
    private boolean needRecount(int newParseCount, Resolver currentResolver) {
        if (lastParseCount != newParseCount) {
            return true;
        }
        CsmFile startFile = null;
        if (currentResolver != null) {
            startFile = currentResolver.getStartFile();
        }
        if (startFile == null) {
            startFile = getContainingFile();
        }
        int fileID = UIDUtilities.getFileID(UIDs.get(startFile));
        if (lastFileID != fileID) {
            return true;
        }
        return false;
    }

    private void updateCache(int newParseCount, Resolver currentResolver) {
        lastParseCount = newParseCount;
        CsmFile startFile = null;
        if (currentResolver != null) {
            startFile = currentResolver.getStartFile();
        }
        if (startFile == null) {
            startFile = getContainingFile();
        }
        int fileID = UIDUtilities.getFileID(UIDs.get(startFile));
        lastFileID = fileID;
    }

    private CsmObject resolve() {
        int newParseCount = FileImpl.getParseCount();
        Resolver currentResolver = ResolverFactory.getCurrentResolver();
        CsmObject result = lastResult;
        if (!isValid()) {
            return result;
        }
        if (needRecount(newParseCount, currentResolver)) {
            Resolver aResolver = ResolverFactory.createResolver(this);
            try {
                result = aResolver.resolve(nameParts, Resolver.CLASS);
            } finally {
                ResolverFactory.releaseResolver(aResolver);
            }
            if (result == null || ForwardClass.isForwardClass((CsmDeclaration)result) || CsmKindUtilities.isClassForwardDeclaration(result)) {
                Resolver aResolver2 = ResolverFactory.createResolver(this);
                try {
                    CharSequence[] nameParts2 = new CharSequence[1];
                    nameParts2[0] = nameParts[nameParts.length - 1];
                    CsmObject result2 = aResolver2.resolve(nameParts2, Resolver.CLASS);
                    if(result == null || (result2 != null && !ForwardClass.isForwardClass((CsmDeclaration)result2) && !CsmKindUtilities.isClassForwardDeclaration(result2))) {
                        result = result2;
                    }
                } finally {
                    ResolverFactory.releaseResolver(aResolver2);
                }
            }
            if(result == null || CsmKindUtilities.isTypedef(result) || CsmKindUtilities.isTypeAlias(result)) {
                result = ((ProjectBase) getContainingFile().getProject()).findClassifier(name);
            }
            if (result == null) {
                result = ProjectBase.getDummyForUnresolved(nameParts, this);
            }
            lastResult = result;
            updateCache(newParseCount, currentResolver);
        //} else {
        //    System.err.println("cache hit ClassForwardDeclarationImpl");
        }
        // NOTE: result shouldn't be cached. 
        // class forward could mean different things depending on other includes
        return result;
    }

    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        // currently class forward declaration is a scope only for its template parameters,
        // but we do not return them as scope elements
        return Collections.emptyList();
    }
    
    public void init(AST ast, CsmScope scope, boolean registerInProject) {
        init(ast, scope, registerInProject, this.getStartOffset(), this.getEndOffset());
    }

    public void init(AST ast, CsmScope scope, boolean registerInProject, int startOffset, int endOffset) {
        // we now know the scope - let's modify nameParts accordingly
        if (CsmKindUtilities.isQualified(scope)) {
            CharSequence scopeQName = ((CsmQualifiedNamedElement) scope).getQualifiedName();
            if (scopeQName != null && scopeQName.length() > 0) {
                List<CharSequence> l = new ArrayList<>();
                for (StringTokenizer stringTokenizer = new StringTokenizer(scopeQName.toString()); stringTokenizer.hasMoreTokens();) {
                    l.add(NameCache.getManager().getString(stringTokenizer.nextToken()));
                }
                l.addAll(Arrays.asList(nameParts));
                CharSequence[] newNameParts = new CharSequence[l.size()];
                l.toArray(newNameParts);
                nameParts = newNameParts;
                if (registerInProject) {
                    RepositoryUtils.put(this);
                } else {
                    Utils.setSelfUID(this);
                }
            }
        }
        // create fake class we refer to
        createForwardClassIfNeed(ast, scope, registerInProject, startOffset, endOffset);
    }
    
    public void init2(CsmScope scope, TemplateDescriptorBuilder templateDescriptor, boolean registerInProject) {
        // we now know the scope - let's modify nameParts accordingly
        if (CsmKindUtilities.isQualified(scope)) {
            CharSequence scopeQName = ((CsmQualifiedNamedElement) scope).getQualifiedName();
            if (scopeQName != null && scopeQName.length() > 0) {
                List<CharSequence> l = new ArrayList<>();
                for (StringTokenizer stringTokenizer = new StringTokenizer(scopeQName.toString()); stringTokenizer.hasMoreTokens();) {
                    l.add(NameCache.getManager().getString(stringTokenizer.nextToken()));
                }
                l.addAll(Arrays.asList(nameParts));
                CharSequence[] newNameParts = new CharSequence[l.size()];
                l.toArray(newNameParts);
                nameParts = newNameParts;
                if (registerInProject) {
                    RepositoryUtils.put(this);
                } else {
                    Utils.setSelfUID(this);
                }
            }
        }
        
        ForwardClassBuilder builder = new ForwardClassBuilder();
        builder.setFile(getContainingFile());
        builder.setStartOffset(getStartOffset());
        builder.setEndOffset(getEndOffset());
        builder.setName(name);
        builder.setScope(scope);
        builder.setTemplateDescriptorBuilder(templateDescriptor);
        if(!registerInProject) {
            builder.setLocal();
        }
        builder.create();
    }    
    
    /**
     * Creates a fake class this forward declaration refers to
     */
    protected ForwardClass createForwardClassIfNeed(AST ast, CsmScope scope, boolean registerInProject) {
        return createForwardClassIfNeed(ast, scope, registerInProject, this.getStartOffset(), this.getEndOffset());
    }    

    /**
     * Creates a fake class this forward declaration refers to
     */
    protected ForwardClass createForwardClassIfNeed(AST ast, CsmScope scope, boolean registerInProject, int startOffset, int endOffset) {
        return ForwardClass.createIfNeeded(name, getContainingFile(), ast, startOffset, endOffset, scope, registerInProject);
    }

    @Override
    public void dispose() {
        super.dispose();
        // nobody disposes the fake forward class => we should take care of this
        CsmClass cls = getCsmClass();
        if (ForwardClass.isForwardClass(cls)) {
            ((ForwardClass) cls).dispose();
        }
        CsmNamespace scope = getContainingFile().getProject().getGlobalNamespace();
        ((NamespaceImpl) scope).removeDeclaration(this);
        ((ProjectBase) getContainingFile().getProject()).unregisterDeclaration(this);
    }

    
    public static class ClassForwardDeclarationBuilder extends SimpleDeclarationBuilder {

        public ClassForwardDeclarationBuilder() {
        }

        public ClassForwardDeclarationBuilder(SimpleDeclarationBuilder builder) {
            super(builder);
        }

        @Override
        public ClassForwardDeclarationImpl create() {
            TemplateDescriptor td = null;
            if(getTemplateDescriptorBuilder() != null) {
                getTemplateDescriptorBuilder().setScope(getScope());
                td = getTemplateDescriptorBuilder().create();
            }

            ClassForwardDeclarationImpl fc = new ClassForwardDeclarationImpl(getName(), td, getFile(), getStartOffset(), getEndOffset());
            
            addDeclaration(fc);
            
            fc.init2(getScope(), getTemplateDescriptorBuilder(), isGlobal());
            
            return fc;
        }
    }    
    
    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        PersistentUtils.writeUTF(name, output);
        PersistentUtils.writeStrings(this.nameParts, output);
        PersistentUtils.writeTemplateDescriptor(templateDescriptor, output);
    }
    
    public ClassForwardDeclarationImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.name = PersistentUtils.readUTF(input, QualifiedNameCache.getManager());
        assert this.name != null;
        this.nameParts = PersistentUtils.readStrings(input, NameCache.getManager());
        this.templateDescriptor = PersistentUtils.readTemplateDescriptor(input);
    }
}
