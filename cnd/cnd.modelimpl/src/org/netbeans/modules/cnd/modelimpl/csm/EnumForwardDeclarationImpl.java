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
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumForwardDeclaration;
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
public class EnumForwardDeclarationImpl extends OffsetableDeclarationBase<CsmEnumForwardDeclaration>
                                         implements CsmEnumForwardDeclaration, CsmTemplate {
    private final CharSequence name;
    private CharSequence[] nameParts;
    private int lastParseCount = -1;    
    private int lastFileID = -1;
    private CsmObject lastResult;

    private final TemplateDescriptor templateDescriptor;
    
    protected EnumForwardDeclarationImpl(AST ast, CsmFile file, boolean global) {
        super(file, getEnumForwardStartOffset(ast), getEnumForwardEndOffset(ast));
        AST qid = getNameAst(ast);
        assert qid != null;
        assert !AstRenderer.isScopedId(qid) : qid;
        name = QualifiedNameCache.getManager().getString(AstUtil.getText(qid));
        nameParts = new CharSequence[] {name};
        this.templateDescriptor = TemplateDescriptor.createIfNeeded(ast, file, null, global);
    }
    
    public static boolean isCorrectOpaqueEnumDeclaration(AST ast) {
        /*
          opaque-enum-declaration:
              enum-key attribute-specifier-seq(opt) identifier enum-base(opt);
        */
        AST qid = getNameAst(ast);
        return qid != null && !AstRenderer.isScopedId(qid);
    }

    public static EnumForwardDeclarationImpl create(AST ast, CsmFile file, CsmScope scope, MutableDeclarationsContainer container, boolean global) {
        EnumForwardDeclarationImpl cfdi = new EnumForwardDeclarationImpl(ast, file, global);
        if (container != null) {
            container.addDeclaration(cfdi);
        }
        cfdi.init(ast, scope, global);
        return cfdi;
    }

    public static ForwardEnum createForwardEnumIfNeeded(AST ast, CsmFile file, CsmScope scope, MutableDeclarationsContainer container, boolean global) {
        EnumForwardDeclarationImpl cfdi = new EnumForwardDeclarationImpl(ast, file, false);
        return cfdi.createForwardEnumIfNeed(ast, scope, global);
    }

    public static EnumForwardDeclarationImpl create(AST ast, CsmFile file, boolean global) {
         EnumForwardDeclarationImpl cfdi = new EnumForwardDeclarationImpl(ast, file, global);
         if (!global) {
              Utils.setSelfUID(cfdi);
         }
        return cfdi;
    }

    private static int getEnumForwardStartOffset(AST ast) {
        AST firstChild = ast.getFirstChild();
        if (firstChild != null && firstChild.getType() == CPPTokenTypes.LITERAL_typedef) {
            AST secondChild = firstChild.getNextSibling();
            if (secondChild != null && (secondChild.getType() == CPPTokenTypes.LITERAL_enum)) {
                return getStartOffset(secondChild);
            }
        }
        return getStartOffset(ast);        
    }
    
    private static int getEnumForwardEndOffset(AST ast) {
        AST firstChild = AstRenderer.getFirstChildSkipQualifiers(ast);
        if (firstChild != null) {
            AST qid = AstUtil.findChildOfType(ast, CPPTokenTypes.SEMICOLON);
            if(qid != null) {
                return getEndOffset(qid);
            }
        }
        return getEndOffset(ast);        
    }
    
    private static AST getNameAst(AST ast) {
        AST qid = AstUtil.findChildOfType(ast, CPPTokenTypes.IDENT);
        if (qid == null) {
            qid = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
        }
        return qid;
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
        return CsmDeclaration.Kind.ENUM_FORWARD_DECLARATION;
    }

    @Override
    public CsmEnum getCsmEnum() {
        CsmObject o = resolve();
        return (o instanceof CsmEnum) ? (CsmEnum) o : (CsmEnum) null;
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
                result = aResolver.resolve(nameParts, Resolver.CLASSIFIER);
            } finally {
                ResolverFactory.releaseResolver(aResolver);
            }
            if (result == null || ForwardEnum.isForwardEnum((CsmDeclaration)result) || CsmKindUtilities.isEnumForwardDeclaration(result)) {
                Resolver aResolver2 = ResolverFactory.createResolver(this);
                try {
                    CharSequence[] nameParts2 = new CharSequence[1];
                    nameParts2[0] = nameParts[nameParts.length - 1];
                    CsmObject result2 = aResolver2.resolve(nameParts2, Resolver.CLASSIFIER);
                    if(result == null || (result2 != null && !ForwardEnum.isForwardEnum((CsmDeclaration)result2) && !CsmKindUtilities.isEnumForwardDeclaration(result2))) {
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
        }
        // NOTE: result shouldn't be cached. 
        // class forward could mean different things depending on other includes
        return result;
    }

    public Collection<CsmScopeElement> getScopeElements() {
        // currently class forward declaration is a scope only for its template parameters,
        // but we do not return them as scope elements
        return Collections.emptyList();
    }

    public void init(AST ast, CsmScope scope, boolean registerInProject) {
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
        createForwardEnumIfNeed(ast, scope, registerInProject);
    }

    /**
     * Creates a fake class this forward declaration refers to
     */
    protected ForwardEnum createForwardEnumIfNeed(AST ast, CsmScope scope, boolean registerInProject) {
        return ForwardEnum.createIfNeeded(name, getContainingFile(), ast, this.getStartOffset(), this.getEndOffset(), scope, registerInProject);
    }

    @Override
    public void dispose() {
        super.dispose();
        // nobody disposes the fake forward class => we should take care of this
        CsmEnum cls = getCsmEnum();
        if (ForwardEnum.isForwardEnum(cls)) {
            ((ForwardEnum) cls).dispose();
        }
        CsmNamespace scope = getContainingFile().getProject().getGlobalNamespace();
        ((NamespaceImpl) scope).removeDeclaration(this);
        ((ProjectBase) getContainingFile().getProject()).unregisterDeclaration(this);
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
    
    public EnumForwardDeclarationImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.name = PersistentUtils.readUTF(input, QualifiedNameCache.getManager());
        assert this.name != null;
        this.nameParts = PersistentUtils.readStrings(input, NameCache.getManager());
        this.templateDescriptor = PersistentUtils.readTemplateDescriptor(input);
    }
}
