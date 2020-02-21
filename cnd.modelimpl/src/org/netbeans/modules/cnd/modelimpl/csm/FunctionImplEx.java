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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.services.CsmExpressionResolver;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.services.CsmSymbolResolver;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.ResolverFactory;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.APTStringManager;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.Pair;

/**
 * A class that 
 * 1) represents function template specilaization declaration (without body)
 * 2) acts as a base class for FunctionDefinitionImpl.
 * In other words, it corresponds to function that has a double colon "::" in its name
 * @param T 
 */
public class FunctionImplEx<T>  extends FunctionImpl<T> {

    private CharSequence qualifiedName;
    private static final short FAKE_QUALIFIED_NAME = 1 << (FunctionImpl.LAST_USED_FLAG_INDEX+1);
    private static final short FUNC_LIKE_VARIABLE  = 1 << (FunctionImpl.LAST_USED_FLAG_INDEX+2);
    protected static final int LAST_USED_FLAG_INDEX_EX = FunctionImpl.LAST_USED_FLAG_INDEX+2;
    
    private CharSequence[] classOrNspNames;   
    
    protected FunctionImplEx(CharSequence name, CharSequence rawName, CsmScope scope, boolean _static, FunctionImpl.CV_RL _const, CsmFile file, int startOffset, int endOffset, boolean global) {
        super(name, rawName, scope, _static, _const, file, startOffset, endOffset, global);
    }

    public static<T> FunctionImplEx<T> create(AST ast, CsmFile file, FileContent fileContent, CsmScope scope, boolean register, boolean global, Map<Integer, CsmObject> objects) throws AstRendererException {
        int startOffset = getStartOffset(ast);
        int endOffset = getEndOffset(ast);
        
        NameHolder nameHolder = NameHolder.createFunctionName(ast);
        CharSequence name = QualifiedNameCache.getManager().getString(nameHolder.getName());
        if (name.length() == 0) {
            AstRendererException.throwAstRendererException((FileImpl) file, ast, startOffset, "Empty function name."); // NOI18N
        }
        CharSequence rawName = initRawName(ast);
        
        boolean _static = AstRenderer.FunctionRenderer.isStatic(ast, file, fileContent, name);
        FunctionImpl.CV_RL _const = AstRenderer.FunctionRenderer.isConst(ast);

        scope = AstRenderer.FunctionRenderer.getScope(scope, file, _static, false);

        FunctionImplEx<T> functionImplEx = new FunctionImplEx<>(name, rawName, scope, _static, _const, file, startOffset, endOffset, global);        
        functionImplEx.setFlags(FUNC_LIKE_VARIABLE, ast.getType() == CPPTokenTypes.CSM_FUNCTION_LIKE_VARIABLE_DECLARATION 
                || ast.getType() == CPPTokenTypes.CSM_FUNCTION_LIKE_VARIABLE_TEMPLATE_DECLARATION);
        temporaryRepositoryRegistration(ast, global, functionImplEx);
        
        StringBuilder clsTemplateSuffix = new StringBuilder();
        TemplateDescriptor templateDescriptor = createTemplateDescriptor(ast, file, functionImplEx, clsTemplateSuffix, global);
        CharSequence classTemplateSuffix = NameCache.getManager().getString(clsTemplateSuffix);
        
        functionImplEx.setTemplateDescriptor(templateDescriptor, classTemplateSuffix);
        functionImplEx.setReturnType(AstRenderer.FunctionRenderer.createReturnType(ast, functionImplEx, file, objects));
        functionImplEx.setParameters(AstRenderer.FunctionRenderer.createParameters(ast, functionImplEx, file, fileContent), 
                AstRenderer.FunctionRenderer.isVoidParameter(ast));        
        
        CharSequence[] classOrNspNames = CastUtils.isCast(ast) ?
            getClassOrNspNames(ast) :
            functionImplEx.initClassOrNspNames(ast);
        functionImplEx.setClassOrNspNames(classOrNspNames);        
        
        // IZ#237907 initialize FQN
        functionImplEx.getUniqueName();
        if (register) {
            postObjectCreateRegistration(register, functionImplEx);
        } else {
            RepositoryUtils.put(functionImplEx);
        }
        postFunctionImpExCreateRegistration(fileContent, register, functionImplEx);
        nameHolder.addReference(fileContent, functionImplEx);
        return functionImplEx;
    }

    protected void setClassOrNspNames(CharSequence[] classOrNspNames) {
        this.classOrNspNames = classOrNspNames;
    }
    
    /** @return either class or namespace */
    public CsmObject findOwner() {
        return findOwner(false);
    }
    
    public CsmObject findOwner(boolean isProjectParsed) {
	CharSequence[] cnn = classOrNspNames;
	if( cnn != null && cnn.length > 0) {
            Resolver resolver = ResolverFactory.createResolver(this);
            CsmObject obj = null;
            try {
                obj = resolver.resolve(cnn, Resolver.CLASSIFIER | Resolver.NAMESPACE);
                if (CsmKindUtilities.isClassifier(obj)) {
                    CsmClassifier cls = resolver.getOriginalClassifier((CsmClassifier)obj);
                    if (cls != null) {
                        obj = cls;
                    }
                    if (CsmKindUtilities.isClass(obj)) {
                        return obj;
                    }
                } else if(CsmKindUtilities.isNamespace(obj)) {
                    return obj;
                }
            } finally {
                ResolverFactory.releaseResolver(resolver);
            }
            // 1) Check that resolver found nothing. It is unlikely that 
            //    further resolving will return different object.
            // 2) Check that length of qualified name is more than 1. 
            //    Seems that it makes no sense to resolve qualified name
            //    with one element any further.
            if (isProjectParsed && obj == null && cnn.length > 1) {
                StringBuilder sb = new StringBuilder(cnn[0]);
                for (int i = 1; i < cnn.length; ++i) {
                    sb.append(APTUtils.SCOPE).append(cnn[i]);
                }
                int startOffset = getStartOffset();
                Collection<CsmObject> resolved = CsmExpressionResolver.resolveObjects(sb.toString(), getContainingFile(), startOffset, null);
                if (resolved != null && !resolved.isEmpty()) {
                    for (CsmObject candidate : resolved) {
                        if (CsmKindUtilities.isClass(candidate) || CsmKindUtilities.isNamespace(candidate)) {
                            return candidate;
                        }
                    }
                }
            }
	}
	return null;
    }    


    protected static CharSequence[] getClassOrNspNames(AST ast) {
	assert CastUtils.isCast(ast);
	AST child = ast.getFirstChild();
        while (child != null && child.getType() == CPPTokenTypes.LITERAL_template) {
            child = AstRenderer.skipTemplateSibling(child);
        }
        child = AstRenderer.getFirstSiblingSkipInline(child);
        child = AstRenderer.getFirstSiblingSkipQualifiers(child);
	if( child != null && child.getType() == CPPTokenTypes.IDENT ) {
	    AST next = child.getNextSibling();
	    if( next != null && next.getType() == CPPTokenTypes.LESSTHAN ) {
                next = AstRenderer.skipTemplateParameters(next);
	    }
	    if( next != null && next.getType() == CPPTokenTypes.SCOPE ) {
		List<CharSequence> l = new ArrayList<>();
                APTStringManager manager = NameCache.getManager();
		l.add(manager.getString(AstUtil.getText(child)));
		begin:
		for( next = next.getNextSibling(); next != null; next = next.getNextSibling() ) {
		    switch( next.getType() ) {
			case CPPTokenTypes.IDENT:
			    l.add(manager.getString(AstUtil.getText(next)));
                            break;
			case CPPTokenTypes.SCOPE:
			    break; // do nothing
			default:
			    break begin;
		    }
		}
		return  l.toArray(new CharSequence[l.size()]);
	    }
	}
	return null;
    }

    protected CharSequence[] initClassOrNspNames(AST node) {
        //qualified id
        AST qid = AstUtil.findMethodName(node);
        if( qid == null ) {
            return null;
        }
        return AstRenderer.renderQualifiedId(qid, !getInheritedTemplateParameters().isEmpty() ? getInheritedTemplateParameters() : getTemplateParameters());
    }
    
    @Override
    public CharSequence getQualifiedName() {
        if( qualifiedName == null ) {
            qualifiedName = QualifiedNameCache.getManager().getString(findQualifiedName(false));
        }
        return qualifiedName;
    }

    protected CharSequence findQualifiedName(boolean isProjectParsed) {
        CsmObject owner = findOwner(isProjectParsed);
        // check if owner is real or fake
        if(CsmKindUtilities.isQualified(owner)) {
            setFlags(FAKE_QUALIFIED_NAME, false);
            if (!CsmKindUtilities.isSpecialization(owner)) {
                return CharSequenceUtils.concatenate(((CsmQualifiedNamedElement) owner).getQualifiedName(), getScopeSuffix(), "::", getQualifiedNamePostfix()); // NOI18N
            } else {
                return CharSequenceUtils.concatenate(((CsmQualifiedNamedElement) owner).getQualifiedName(),  "::", getQualifiedNamePostfix()); // NOI18N
            }
        }
        setFlags(FAKE_QUALIFIED_NAME, true);
        CharSequence[] cnn = classOrNspNames;
        CsmNamespaceDefinition nsd = findNamespaceDefinition();
        StringBuilder sb = new StringBuilder();
        if( nsd != null ) {
            sb.append(nsd.getQualifiedName());
        }
        if( cnn != null ) {
            for (int i = 0; i < cnn.length; i++) {
                if( sb.length() > 0 ) {
                    sb.append("::"); // NOI18N
                }
                sb.append(cnn[i]);
            }
        }
        if( sb.length() == 0 ) {
            sb.append("unknown>"); // NOI18N
        }
        sb.append("::"); // NOI18N
        sb.append(getQualifiedNamePostfix());
        return sb;
    }

    protected static <T> void postFunctionImpExCreateRegistration(FileContent fileContent, boolean global, FunctionImplEx<T> obj) {
        if (global) {
            if (obj.isFakeQualifiedName()) {
                fileContent.onFakeRegisration(obj, null);
            }
        }
    }
//    
//    @Override
//    protected boolean registerInProject() {
//        boolean out = super.registerInProject();
//        // if this funtion couldn't find it's FQN => register it as fake and
//        // come back later on for registration (see fixFakeRegistration with null ast)
//        if (hasFlags(FAKE_QUALIFIED_NAME)) {
//            int i = 0;
//        }
//        return out;
//    }
    
    public final boolean fixFakeRegistration(FileContent fileContent, boolean projectParsedMode, Pair<AST, MutableDeclarationsContainer> fakeData) {
        boolean fixed = false;
        if (fakeData != null) {
            final AST fixFakeRegistrationAst = fakeData.first();
            final MutableDeclarationsContainer container = fakeData.second();
            CsmObject owner = findOwner(projectParsedMode);
            if (CsmKindUtilities.isClass(owner)) {
                CsmClass cls = (CsmClass) owner;
                boolean _static = AstUtil.hasChildOfType(fixFakeRegistrationAst, CPPTokenTypes.LITERAL_static);
                boolean _extern = AstUtil.hasChildOfType(fixFakeRegistrationAst, CPPTokenTypes.LITERAL_extern);
                for (CsmMember member : cls.getMembers()) {
                    if (member.isStatic() && member.getName().equals(getName())) {
                        FileImpl aFile = (FileImpl) getContainingFile();
                        aFile.getProjectImpl(true).unregisterDeclaration(this);
                        fileContent.removeDeclaration(this);
                        NameHolder nameHolder = NameHolder.createFunctionName(fixFakeRegistrationAst);
                        VariableDefinitionImpl var = VariableDefinitionImpl.create(fixFakeRegistrationAst, getContainingFile(), getReturnType(), nameHolder, _static, _extern);
                        fileContent.addDeclaration(var);
                        nameHolder.addReference(fileContent, var); // TODO: move into VariableImpl.create()
                        return true;
                    }
                }
            } else if (CsmKindUtilities.isNamespace(owner)) {
                boolean _static = AstUtil.hasChildOfType(fixFakeRegistrationAst, CPPTokenTypes.LITERAL_static);
                boolean _extern = AstUtil.hasChildOfType(fixFakeRegistrationAst, CPPTokenTypes.LITERAL_extern);
                CsmFilter filter = CsmSelect.getFilterBuilder().createCompoundFilter(
                         CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.VARIABLE, CsmDeclaration.Kind.VARIABLE_DEFINITION),
                         CsmSelect.getFilterBuilder().createNameFilter(getName(), true, true, false));
                Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDeclarations(((CsmNamespace)owner), filter);
                while (it.hasNext()) {
                    CsmDeclaration decl = it.next();
                    if (CsmKindUtilities.isExternVariable(decl) && decl.getName().equals(getName())) {
                        FileImpl aFile = (FileImpl) getContainingFile();
                        aFile.getProjectImpl(true).unregisterDeclaration(this);
                        fileContent.removeDeclaration(this);
                        NameHolder nameHolder = NameHolder.createFunctionName(fixFakeRegistrationAst);
                        VariableDefinitionImpl var = VariableDefinitionImpl.create(fixFakeRegistrationAst, getContainingFile(), getReturnType(), nameHolder, _static, _extern);
                        fileContent.addDeclaration(var);
                        nameHolder.addReference(fileContent, var); // TODO: move into VariableImpl.create()
                        return true;
                    }
                }
            }            
            if (projectParsedMode) {
                try {                    
                    FileImpl aFile = (FileImpl) getContainingFile();
                    fileContent.removeDeclaration(this);  
                    this.dispose();
                    RepositoryUtils.remove(this.getUID(), this);
                    CsmOffsetableDeclaration decl;
                    boolean isGloballyVisibleInNamespace;
                    if (new AstRenderer(aFile).isFuncLikeVariable(fixFakeRegistrationAst, true, true)) {
                        boolean _static = AstUtil.hasChildOfType(fixFakeRegistrationAst, CPPTokenTypes.LITERAL_static);
                        boolean _extern = AstUtil.hasChildOfType(fixFakeRegistrationAst, CPPTokenTypes.LITERAL_extern);                        
                        NameHolder nameHolder = NameHolder.createFunctionName(fixFakeRegistrationAst);
                        VariableImpl var = VariableImpl.create(
                                fixFakeRegistrationAst, 
                                getContainingFile(), 
                                getReturnType(), 
                                nameHolder, 
                                this.getScope(), 
                                _static, 
                                _extern, 
                                true
                        );
                        nameHolder.addReference(fileContent, var); // TODO: move into VariableImpl.create()
                        CndUtils.assertTrueInConsole(!CsmKindUtilities.isClass(this.getScope()), "Cannot be class!"); // NOI18N
                        isGloballyVisibleInNamespace = NamespaceImpl.isNamespaceScope(var, CsmKindUtilities.isFile(getScope())) && CsmKindUtilities.isNamespace(this.getScope());
                        decl = var;
                    } else {
                        FunctionImpl<T> fi = FunctionImpl.create(fixFakeRegistrationAst, getContainingFile(), fileContent, null, this.getScope(), true, null);
                        fi.registerInProject();
                        CndUtils.assertTrueInConsole(!CsmKindUtilities.isClass(this.getScope()), "Cannot be class!"); // NOI18N
                        isGloballyVisibleInNamespace = NamespaceImpl.isNamespaceScope(fi) && CsmKindUtilities.isNamespace(this.getScope());
                        decl = fi;
                    }
                    if (isGloballyVisibleInNamespace) {
                        ((NamespaceImpl) getScope()).addDeclaration(decl);
                        if (CsmKindUtilities.isNamespaceDefinition(container)) {
                            container.addDeclaration(decl);
                        } else if (((NamespaceImpl) getScope()).isGlobal()) {
                            fileContent.addDeclaration(decl);
                        }
                    } else {
                        if (CsmKindUtilities.isNamespaceDefinition(container)) {
                            container.addDeclaration(decl);
                        } else {
                            fileContent.addDeclaration(decl);
                        }
                    }
                    fixed = true;
                } catch (AstRendererException e) {
                    DiagnosticExceptoins.register(e);
                }
            }
        } else {
            CharSequence newQname = QualifiedNameCache.getManager().getString(findQualifiedName(projectParsedMode));
            if (!newQname.equals(qualifiedName)) {
                ProjectBase aProject = ((FileImpl)getContainingFile()).getProjectImpl(true);
                aProject.unregisterDeclaration(this);
                this.cleanUID();
                //TODO: need to create the new FunctionImpl instead
                qualifiedName = newQname;
                registerInProject();
                postFunctionImpExCreateRegistration(fileContent, true, this);
                fixed = true;
                RepositoryUtils.put(this);
            }
        }
        return fixed;
    }
    
    private CsmNamespaceDefinition findNamespaceDefinition() {
        CsmFilter filter = CsmSelect.getFilterBuilder().createKindFilter(Kind.NAMESPACE_DEFINITION);
        return findNamespaceDefinition(CsmSelect.getDeclarations(getContainingFile(), filter), filter);
    }
    
    private CsmNamespaceDefinition findNamespaceDefinition(Iterator<CsmOffsetableDeclaration> it, CsmFilter filter) {
        while(it.hasNext()) {
            CsmOffsetableDeclaration decl = it.next();
            if (decl.getStartOffset() > this.getStartOffset()) {
                break;
            }
            if (decl.getKind() == CsmDeclaration.Kind.NAMESPACE_DEFINITION) {
                if (this.getEndOffset() < decl.getEndOffset()) {
                    CsmNamespaceDefinition nsdef = (CsmNamespaceDefinition) decl;
                    CsmNamespaceDefinition inner = findNamespaceDefinition(CsmSelect.getDeclarations(nsdef, filter), filter);
                    return (inner == null) ? nsdef : inner;
                }
            }
        }
        return null;
    }    
    
    private boolean isFakeQualifiedName() {
        return hasFlags(FAKE_QUALIFIED_NAME);
    }
    
    public static boolean isFakeFunction(CsmObject declaration) {
        if (declaration instanceof FunctionImplEx<?>) {
            // TODO: remove usage of non-final FAKE_QUALIFIED_NAME flag
            return FunctionImplEx.class.equals(declaration.getClass()) &&
                    (((FunctionImplEx)declaration).hasFlags(FUNC_LIKE_VARIABLE) || ((FunctionImplEx)declaration).hasFlags(FAKE_QUALIFIED_NAME));
        } else {
            return false;
        }
    }

    public static class FunctionExBuilder extends FunctionBuilder {
    
        @Override
        public FunctionImplEx create() {
            FunctionImplEx fun = new FunctionImplEx(getName(), getRawName(), getScope(), isStatic(), FunctionImpl.CV_RL.isConst(isConst()), getFile(), getStartOffset(), getEndOffset(), isGlobal());
            init(fun);
            return fun;
        }
        
        protected void init(FunctionImplEx fun) {
            temporaryRepositoryRegistration(isGlobal(), fun);

            setTemplateDescriptor(fun);
            setReturnType(fun);
            setParameters(fun);
            setScopeNames(fun);

            postObjectCreateRegistration(isGlobal(), fun);
            postFunctionImpExCreateRegistration(getFileContent(), isGlobal(), fun);
            addReference(fun);
            
            addDeclaration(fun);
        }
        
        protected void setScopeNames(FunctionImplEx fun) {
            fun.setClassOrNspNames(getScopeNames());
        }
        
    }    
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        // can be null
        PersistentUtils.writeUTF(this.qualifiedName, output);
        PersistentUtils.writeStrings(this.classOrNspNames, output);
    }
    
    public FunctionImplEx(RepositoryDataInput input) throws IOException {
	super(input);
        // can be null
        this.qualifiedName = PersistentUtils.readUTF(input, QualifiedNameCache.getManager());
        this.classOrNspNames = PersistentUtils.readStrings(input, NameCache.getManager());
    }
}
