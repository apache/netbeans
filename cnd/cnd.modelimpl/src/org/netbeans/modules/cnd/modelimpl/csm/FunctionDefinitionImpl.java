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
import java.util.Map;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionParameterListImpl.FunctionParameterListBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.Disposable;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.csm.deep.CompoundStatementImpl.CompoundStatementBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.StatementBase.StatementBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.StatementBase.StatementBuilderContainer;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.util.CharSequences;

/**
 */
public class FunctionDefinitionImpl<T> extends FunctionImplEx<T> implements CsmFunctionDefinition {

    private CsmUID<CsmFunction> declarationUID;
    private CsmCompoundStatement body;
    private int parseCount;

    protected FunctionDefinitionImpl(CharSequence name, CharSequence rawName, CsmScope scope, boolean _static, FunctionImpl.CV_RL _const, CsmFile file, int startOffset, int endOffset, boolean global) {
        super(name, rawName, scope, _static, _const, file, startOffset, endOffset, global);
    }
     
    public static<T> FunctionDefinitionImpl<T> create(AST ast, CsmFile file, FileContent fileContent, CsmScope scope, boolean global, Map<Integer, CsmObject> objects) throws AstRendererException {
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

        scope = AstRenderer.FunctionRenderer.getScope(scope, file, _static, true);

        FunctionDefinitionImpl<T> functionDefinitionImpl = new FunctionDefinitionImpl<>(name, rawName, scope, _static, _const, file, startOffset, endOffset, global);        
        
        temporaryRepositoryRegistration(ast, global, functionDefinitionImpl);
        
        StringBuilder clsTemplateSuffix = new StringBuilder();
        TemplateDescriptor templateDescriptor = createTemplateDescriptor(ast, file, functionDefinitionImpl, clsTemplateSuffix, global);
        CharSequence classTemplateSuffix = NameCache.getManager().getString(clsTemplateSuffix);
        
        functionDefinitionImpl.setTemplateDescriptor(templateDescriptor, classTemplateSuffix);
        functionDefinitionImpl.setReturnType(AstRenderer.FunctionRenderer.createReturnType(ast, functionDefinitionImpl, file, objects));
        functionDefinitionImpl.setParameters(AstRenderer.FunctionRenderer.createParameters(ast, functionDefinitionImpl, file, fileContent), 
                AstRenderer.FunctionRenderer.isVoidParameter(ast));        
        
        CharSequence[] classOrNspNames = CastUtils.isCast(ast) ?
            getClassOrNspNames(ast) :
            functionDefinitionImpl.initClassOrNspNames(ast);
        functionDefinitionImpl.setClassOrNspNames(classOrNspNames);        

        CsmCompoundStatement body = AstRenderer.findCompoundStatement(ast, file, functionDefinitionImpl);
        if (body == null) {
            throw AstRendererException.throwAstRendererException((FileImpl)file, ast, startOffset,
                    "Null body in method definition."); // NOI18N
        }        
        functionDefinitionImpl.setCompoundStatement(body);
        
        postObjectCreateRegistration(global, functionDefinitionImpl);
        postFunctionImpExCreateRegistration(fileContent, global, functionDefinitionImpl);
        nameHolder.addReference(fileContent, functionDefinitionImpl);
        return functionDefinitionImpl;
    }

    protected void setCompoundStatement(CsmCompoundStatement body) {
        this.body = body;
    }    
    
    @Override
    public DefinitionKind getDefinitionKind() {
        return DefinitionKind.REGULAR;
    }    

    @Override
    public void dispose() {
        super.dispose();
        if (body instanceof Disposable) {
            ((Disposable) body).dispose();
        }
    }

    @Override
    public CsmCompoundStatement getBody() {
        return body;
    }

    @Override
    public CsmFunction getDeclaration() {
        CsmFunction declaration = _getDeclaration();
        if (declaration == null || FunctionImplEx.isFakeFunction(declaration) || !CsmBaseUtilities.isValid(declaration)) {
            int newCount = FileImpl.getParseCount();
            if (newCount == parseCount) {
                return declaration;
            }
            _setDeclaration(null);
            declaration = findDeclaration();
            _setDeclaration(declaration);
            parseCount = newCount;
        }
        return declaration;
    }

    private CsmFunction _getDeclaration() {
        CsmFunction decl = UIDCsmConverter.UIDtoDeclaration(this.declarationUID);
        // null object is OK here, because of changed cached reference
        return decl;
    }

    private void _setDeclaration(CsmFunction decl) {
        CsmUID<CsmFunction> uid = UIDCsmConverter.declarationToUID(decl);
        this.declarationUID = uid;
        // check local variable in assert
        // field can be already cleared by another _setDeclaration(null)
        assert uid != null || decl == null : "unexpected " + uid + " for " + decl;
    }

    // method try to find declaration in case class have exactly one cast operator with desired name
    private CsmDeclaration fixCastOperator(CsmClass owner) {
        CsmDeclaration candidate = null;
        CsmClassifier ourClassifier = null;
        String s1 = getName().toString();
        int i1 = s1.lastIndexOf(APTUtils.SCOPE); 
        if (i1 > 0) {
            s1 = OPERATOR + " " + s1.substring(i1 + 2); // NOI18N
        }
        Iterator<CsmMember> it = CsmSelect.getClassMembers(owner,
                CsmSelect.getFilterBuilder().createNameFilter(OPERATOR, false, true, false));
        while (it.hasNext()) {
            CsmMember m = it.next();
            String s2 = m.getName().toString();
            int i2 = s2.lastIndexOf(APTUtils.SCOPE); 
            if (i2 > 0) {
                s2 = OPERATOR + " " + s2.substring(i2 + 2); // NOI18N
            }
            boolean candidateMatch = s1.equals(s2);
            if (!candidateMatch) {              
                if (CsmKindUtilities.isFunctionDeclaration(m) && CsmKindUtilities.isCastOperator(m)) {
                    // First initialize our type
                    if (ourClassifier == null) {
                        ourClassifier = getCastOperatorCastEntity(this);
                        if (!checkResolvedClassifier(ourClassifier)) {
                            break;
                        }
                    }
                    CsmClassifier memberClassifier = getCastOperatorCastEntity((CsmFunction) m);
                    if (checkResolvedClassifier(memberClassifier)) {
                        if (ourClassifier.getQualifiedName().toString().equals(memberClassifier.getQualifiedName().toString())) {
                            candidateMatch = true;
                        } else if (CsmKindUtilities.isTemplateParameter(ourClassifier) && CsmKindUtilities.isTemplateParameter(memberClassifier)) {
                            candidateMatch = true;
                        }
                    }
                }
            }
            if (candidateMatch) {
                if (candidate == null) {
                    candidate = m;
                } else {
                    candidate = null;
                    break;
                }
            }
        }
        return candidate;
    }

    private CsmFunction findDeclaration() {
        if (!isValid()) {
            return null;
        }
        String uname = ""+Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION) + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix(); //NOI18N
        Collection<? extends CsmDeclaration> prjDecls = getContainingFile().getProject().findDeclarations(uname);
        if (prjDecls.isEmpty()) {
            uname = ""+Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_FRIEND) + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix(); //NOI18N
            prjDecls = getContainingFile().getProject().findDeclarations(uname);
        }
        Collection<CsmDeclaration> decls = new ArrayList<>(1);
        if (prjDecls.isEmpty()) {
            CsmObject owner = findOwner();
            if(owner == null) {
                owner = CsmBaseUtilities.getFunctionClassByQualifiedName(this);
            }
            if (CsmKindUtilities.isClass(owner)) {
                Iterator<CsmMember> it = CsmSelect.getClassMembers((CsmClass) owner,
                        CsmSelect.getFilterBuilder().createNameFilter(getName(), true, true, false));
                decls = findByNameAndParamsNumber(it, getName(), getParameters().size());
                if (decls.isEmpty() && CsmKindUtilities.isCastOperator(this)) {
                    CsmDeclaration cast = fixCastOperator((CsmClass)owner);
                    if (cast != null) {
                        decls.add(cast);
                    }
                }
            } else if (CsmKindUtilities.isNamespace(owner)) {
                Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDeclarations(((CsmNamespace) owner),
                        CsmSelect.getFilterBuilder().createNameFilter(getName(), true, true, false));
                decls = findByNameAndParamsNumber(it, getName(), getParameters().size());
            }
        } else {
            decls = findByNameAndParamsNumber(prjDecls.iterator(), getName(), getParameters().size());
        }
        CsmFunction decl = chooseDeclaration(decls);
        return decl;
    }

    private Collection<CsmDeclaration> findByNameAndParamsNumber(Iterator<? extends CsmObject> declarations, CharSequence name, int paramsNumber) {
        Collection<CsmDeclaration> out = new ArrayList<>(1);
        Collection<CsmDeclaration> best = new ArrayList<>(1);
        Collection<CsmDeclaration> otherVisible = new ArrayList<>(1);
        for (Iterator<? extends CsmObject> it = declarations; it.hasNext();) {
            CsmObject o = it.next();
            if (CsmKindUtilities.isFunction(o)) {
                CsmFunction decl = (CsmFunction) o;
                if (decl.getName().equals(name)) {
                    if (decl.getParameters().size() == paramsNumber) {
                        if (!FunctionImplEx.isFakeFunction(decl)) {
                            if (FunctionImpl.isObjectVisibleInFile(getContainingFile(), decl)) {
                                best.add(decl);
                                continue;
                            }
                        }
                        out.add(decl);
                    } else {
                        if (!FunctionImplEx.isFakeFunction(decl)) {
                            if (FunctionImpl.isObjectVisibleInFile(getContainingFile(), decl)) {
                                otherVisible.add(decl);
                            }
                            out.add(decl);
                        }
                    }
                }
            }
        }
        if (!best.isEmpty()) {
            out = best;
        } else if (!otherVisible.isEmpty()) {
            out = otherVisible;
        }
        return out;
    }

    @Override
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.FUNCTION_DEFINITION;
    }

    @Override
    protected CharSequence findQualifiedName(boolean isProjectParsed) {
        CsmFunction declaration = _getDeclaration();
        if (declaration != null) {
            return declaration.getQualifiedName();
        }
        return super.findQualifiedName(isProjectParsed);
    }

    @Override
    public CsmScope getScope() {
        return getContainingFile();
    }

    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        Collection<CsmScopeElement> l = super.getScopeElements();
        l.add(getBody());
        return l;
    }

    @Override
    public CsmFunctionDefinition getDefinition() {
        return this;
    }

    public static class FunctionDefinitionBuilder extends FunctionExBuilder implements StatementBuilderContainer {

        private CompoundStatementBuilder bodyBuilder;
        
        public void setBodyBuilder(CompoundStatementBuilder builder) {
            bodyBuilder = builder;
        }

        public CompoundStatementBuilder getBodyBuilder() {
            return bodyBuilder;
        }
        
        @Override
        public FunctionDefinitionImpl create() {
            final FunctionParameterListBuilder parameters = (FunctionParameterListBuilder)getParametersListBuilder();
            if (parameters == null) {
                return null;
            }
            CsmScope scope = AstRenderer.FunctionRenderer.getScope(getScope(), getFile(), isStatic(), true);

            FunctionDefinitionImpl<?> impl = new FunctionDefinitionImpl(getName(), getRawName(), scope, isStatic(), FunctionImpl.CV_RL.isConst(isConst()), getFile(), getStartOffset(), getEndOffset(), true);        
            temporaryRepositoryRegistration(true, impl);

//            StringBuilder clsTemplateSuffix = new StringBuilder();
//            TemplateDescriptor templateDescriptor = createTemplateDescriptor(ast, file, functionDDImpl, clsTemplateSuffix, global);
//            CharSequence classTemplateSuffix = NameCache.getManager().getString(clsTemplateSuffix);
//
//            functionDDImpl.setTemplateDescriptor(templateDescriptor, classTemplateSuffix);
            if(getTemplateDescriptorBuilder() != null) {
                impl.setTemplateDescriptor(getTemplateDescriptor(), NameCache.getManager().getString(CharSequences.create(""))); // NOI18N
            }
            
            impl.setReturnType(getType());
            (parameters).setScope(impl);
            impl.setParameters(parameters.create(), false);
            
            impl.setClassOrNspNames(getScopeNames());        
            
            bodyBuilder.setScope(impl);
            impl.setCompoundStatement(bodyBuilder.create());

            postObjectCreateRegistration(true, impl);
            postFunctionImpExCreateRegistration(getFileContent(), isGlobal(), impl);
            getNameHolder().addReference(getFileContent(), impl);
            
            addDeclaration(impl);
            
            return impl;
        }

        @Override
        public void addStatementBuilder(StatementBuilder builder) {
            assert builder instanceof CompoundStatementBuilder;
            setBodyBuilder((CompoundStatementBuilder)builder);
        }

        protected void setBody(FunctionDefinitionImpl fun) {
            bodyBuilder.setScope(fun);
            fun.setCompoundStatement(bodyBuilder.create());
        }        
        
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeCompoundStatement(this.body, output);

        // save cached declaration
        UIDObjectFactory.getDefaultFactory().writeUID(this.declarationUID, output);
    }

    public FunctionDefinitionImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.body = PersistentUtils.readCompoundStatement(input);

        // read cached declaration
        this.declarationUID = UIDObjectFactory.getDefaultFactory().readUID(input);
    }
}
