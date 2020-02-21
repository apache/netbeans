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
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriendClass;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelimpl.csm.SpecializationDescriptor.SpecializationDescriptorBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.ResolverFactory;
import org.netbeans.modules.cnd.modelimpl.parser.FakeAST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.CharSequences;

/**
 *
 */
public final class FriendClassImpl extends OffsetableDeclarationBase<CsmFriendClass> implements CsmFriendClass, CsmTemplate {
    private final CharSequence name;
    private final CharSequence[] nameParts;
    private final CsmUID<CsmClass> parentUID;
    private final CsmUID<CsmClassForwardDeclaration> classForwardUID;
    private CsmUID<CsmClass> friendUID;
    private TemplateDescriptor templateDescriptor = null;
    private SpecializationDescriptor specializationDesctiptor;
    private int lastParseCount = -1;    
    private int lastFileID = -1;
    
    private FriendClassImpl(AST ast, AST qid, CsmClassForwardDeclaration cfd, FileImpl file, CsmClass parent, int startOffset, int endOffset, boolean register) throws AstRendererException {
        super(file, startOffset, endOffset);
        this.parentUID = UIDs.get(parent);
        qid = (qid != null) ? qid : AstUtil.findSiblingOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
        if (qid == null) {
            throw AstRendererException.throwAstRendererException(file, ast, startOffset, "Invalid friend class declaration."); // NOI18N
        }
        name = QualifiedNameCache.getManager().getString(AstRenderer.getQualifiedName(qid));
        nameParts = initNameParts(qid);
        classForwardUID = UIDCsmConverter.declarationToUID(cfd);
        AST templateParams = AstUtil.findSiblingOfType(ast, CPPTokenTypes.LITERAL_template);
        if (templateParams != null) {
            List<CsmTemplateParameter> params = TemplateUtils.getTemplateParameters(templateParams, file, parent, register);
            final CharSequence classSpecializationSuffix = TemplateUtils.getClassSpecializationSuffix(templateParams, null);
            CharSequence fullName = CharSequenceUtils.concatenate("<", classSpecializationSuffix, ">"); // NOI18N
            setTemplateDescriptor(params, fullName, classSpecializationSuffix.length() > 0, register);
        }
        specializationDesctiptor = SpecializationDescriptor.createIfNeeded(ast, getContainingFile(), parent, register);
    }

    private FriendClassImpl(CharSequence name, CsmClassForwardDeclaration cfd, CsmClass parent, CsmFile file, int startOffset, int endOffset) {
        super(file, startOffset, endOffset);
        this.parentUID = UIDs.get(parent);
        this.name = name;
        nameParts = Utils.splitQualifiedName(name.toString());
        classForwardUID = UIDCsmConverter.declarationToUID(cfd);
    }
    
    public static FriendClassImpl create(AST ast, AST qid, CsmClassForwardDeclaration cfd, FileImpl file, CsmClass parent, boolean register) throws AstRendererException {
        AST fakeParent = new FakeAST();
        fakeParent.setType(CPPTokenTypes.CSM_CLASS_DECLARATION);
        fakeParent.addChild(ast);
        int startOffset = ClassForwardDeclarationImpl.getClassForwardStartOffset(fakeParent);
        int endOffset = ClassForwardDeclarationImpl.getClassForwardEndOffset(fakeParent);
        FriendClassImpl friendClassImpl = new FriendClassImpl(ast, qid, cfd, file, parent, startOffset, endOffset, register);
        postObjectCreateRegistration(register, friendClassImpl);
        return friendClassImpl;
    }

    @Override
    protected boolean registerInProject() {
        CsmProject project = getContainingFile().getProject();
        if( project instanceof ProjectBase ) {
            return ((ProjectBase) project).registerDeclaration(this);
        }
        return false;
    }

    @Override
    public CsmClass getContainingClass() {
        return parentUID.getObject();
    }

    @Override
    public CsmScope getScope() {
        return getContainingClass();
    }

    @Override
    public CharSequence getName() {
        return name;
    }

    @Override
    public CharSequence getQualifiedName() {
        CsmClass cls = getContainingClass();
        CharSequence clsQName = cls.getQualifiedName();
	if( clsQName != null && clsQName.length() > 0 ) {
            return CharSequences.create(CharSequenceUtils.concatenate(clsQName, "::", getQualifiedNamePostfix())); // NOI18N
	}
        return getName();
    }

    @Override
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.CLASS_FRIEND_DECLARATION;
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
    
    @Override
    public CsmClass getReferencedClass() {
        CsmClass cls = UIDCsmConverter.UIDtoClass(friendUID);
        int newParseCount = FileImpl.getParseCount();
        Resolver currentResolver = ResolverFactory.getCurrentResolver();
        if (needRecount(newParseCount, currentResolver)) {
            if(!CsmBaseUtilities.isValid(cls)|| ForwardClass.isForwardClass(cls)) {
                cls = null;
                CsmClassForwardDeclaration cfd = UIDCsmConverter.UIDtoCsmObject(classForwardUID);
                if(CsmBaseUtilities.isValid(cfd)) {
                    cls = cfd.getCsmClass();
                }
                friendUID = UIDCsmConverter.declarationToUID(cls);
            }
            if (!CsmBaseUtilities.isValid(cls) || ForwardClass.isForwardClass(cls)) {
                CsmObject o = resolve();
                if (CsmKindUtilities.isClass(o)) {
                    cls = (CsmClass) o;
                    friendUID = UIDCsmConverter.objectToUID(cls);
                }
            }
            if(CsmKindUtilities.isTemplate(cls) && specializationDesctiptor != null) {
                CsmInstantiationProvider instProvider = CsmInstantiationProvider.getDefault();            
                CsmObject o = instProvider.instantiate((CsmTemplate)cls, specializationDesctiptor.getSpecializationParameters());
                while(CsmKindUtilities.isInstantiation(o) ) {
                    o = ((CsmInstantiation)o).getTemplateDeclaration();
                }
                if(CsmKindUtilities.isClass(o)) {
                    cls = (CsmClass) o;
                    friendUID = UIDCsmConverter.objectToUID(cls);
                }
            }            
            updateCache(newParseCount, currentResolver);
        //} else {
        //    System.err.println("cache hit FriendClassImpl");
        }
        return cls;
    }
    
    private CharSequence[] initNameParts(AST qid) {
        if( qid != null ) {
            return AstRenderer.getNameTokens(qid);
        }
        return new CharSequence[0];
    }
    
    private CsmObject resolve() {
        CsmObject result = null;
        if (!isValid()) {
            return result;
        }
        Resolver aResolver = ResolverFactory.createResolver(this);
        try {
            result = aResolver.resolve(nameParts, Resolver.CLASS);
        } finally {
            ResolverFactory.releaseResolver(aResolver);
        }
        if (result == null) {
            result = ProjectBase.getDummyForUnresolved(nameParts, this);
        }
        return result;
    }

    @Override
    public void dispose() {
        super.dispose();
        unregisterInProject();
    }

    private void unregisterInProject() {
        CsmClassForwardDeclaration cfd = UIDCsmConverter.UIDtoCsmObject(classForwardUID);
        if (cfd instanceof ClassForwardDeclarationImpl) {
            ((ClassForwardDeclarationImpl) cfd).dispose();
        }
        ((ProjectBase) getContainingFile().getProject()).unregisterDeclaration(this);
        this.cleanUID();
    }

    private void setTemplateDescriptor(List<CsmTemplateParameter> params, CharSequence name, boolean specialization, boolean global) {
        templateDescriptor = new TemplateDescriptor(params, name, specialization, global);
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
        return (templateDescriptor != null) ? CharSequences.create(CharSequenceUtils.concatenate(getName(), templateDescriptor.getTemplateSuffix())) : getName();
    }

    
    public static class FriendClassBuilder extends SimpleDeclarationBuilder {
        
        private SpecializationDescriptorBuilder specializationDescriptorBuilder;

        public FriendClassBuilder() {
        }
        
        public FriendClassBuilder(SimpleDeclarationBuilder builder) {
            super(builder);
        }
        
        public void setSpecializationDescriptorBuilder(SpecializationDescriptorBuilder specializationDescriptorBuilder) {
            this.specializationDescriptorBuilder = specializationDescriptorBuilder;
        }

        public SpecializationDescriptor getSpecializationDescriptor() {
            if(specializationDescriptorBuilder != null) {
                specializationDescriptorBuilder.setScope(getScope());
                return specializationDescriptorBuilder.create();
            }
            return null;
        }
        
        @Override
        public FriendClassImpl create() {
            FriendClassImpl impl = new FriendClassImpl(getName(), null, (CsmClass)getScope(), getFile(), getStartOffset(), getEndOffset());
            if(getTemplateDescriptorBuilder() != null) {
                impl.templateDescriptor = getTemplateDescriptor();
            }
            if(specializationDescriptorBuilder != null) {
                impl.specializationDesctiptor = getSpecializationDescriptor();
            }
            postObjectCreateRegistration(isGlobal(), impl);
            return impl;
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
        UIDObjectFactory.getDefaultFactory().writeUID(this.parentUID, output);    
        UIDObjectFactory.getDefaultFactory().writeUID(this.friendUID, output);
        PersistentUtils.writeTemplateDescriptor(templateDescriptor, output);
        UIDObjectFactory.getDefaultFactory().writeUID(this.classForwardUID, output);
        PersistentUtils.writeSpecializationDescriptor(specializationDesctiptor, output);
    }


    public FriendClassImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.name = PersistentUtils.readUTF(input, QualifiedNameCache.getManager());
        assert this.name != null;
        this.nameParts = PersistentUtils.readStrings(input, NameCache.getManager());
        this.parentUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        this.friendUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        this.templateDescriptor = PersistentUtils.readTemplateDescriptor(input);
        this.classForwardUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        this.specializationDesctiptor = PersistentUtils.readSpecializationDescriptor(input);
    }
}
