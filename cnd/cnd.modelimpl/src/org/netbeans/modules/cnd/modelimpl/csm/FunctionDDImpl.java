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
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionParameterListImpl.FunctionParameterListBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil.ASTTokensStringizer;
import org.netbeans.modules.cnd.modelimpl.csm.core.Disposable;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.csm.deep.CompoundStatementImpl.CompoundStatementBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.StatementBase.StatementBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.StatementBase.StatementBuilderContainer;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.util.CharSequences;

/**
 * Implements both CsmFunction and CsmFunctionDefinition -
 * for those cases, when they coinside (i.e. implivit inlines)
 */
public class FunctionDDImpl<T> extends FunctionImpl<T> implements CsmFunctionDefinition {

    private CsmCompoundStatement body;

    protected FunctionDDImpl(CharSequence name, CharSequence rawName, CsmScope scope, boolean _static, FunctionImpl.CV_RL _const, CsmFile file, int startOffset, int endOffset, boolean global) {
        super(name, rawName, scope, _static, _const, file, startOffset, endOffset, global);
    }
    
    public static<T> FunctionDDImpl<T> create(AST ast, CsmFile file, FileContent fileContent, CsmScope scope, boolean global) throws AstRendererException {
        int startOffset = getStartOffset(ast);
        int endOffset = getEndOffset(ast);
        
        NameHolder nameHolder = NameHolder.createFunctionName(ast);
        CharSequence name = QualifiedNameCache.getManager().getString(nameHolder.getName());
        boolean isLambda = false;
        if (name.length() == 0 && !global) {
            name = QualifiedNameCache.getManager().getString("lambda"); // NOI18N
            isLambda = true;
        }        
        if (name.length() == 0) {
            ASTTokensStringizer stringizer = new ASTTokensStringizer(true);
            AstUtil.visitAST(stringizer, ast);
            String expanded = stringizer.getText();
            throw AstRendererException.throwAstRendererException((FileImpl) file, ast, startOffset, "Empty function name: " + expanded); // NOI18N
        }
        CharSequence rawName = initRawName(ast);
        
        boolean _static = AstRenderer.FunctionRenderer.isStatic(ast, file, fileContent, name);
        FunctionImpl.CV_RL _const = AstRenderer.FunctionRenderer.isConst(ast);

        scope = AstRenderer.FunctionRenderer.getScope(scope, file, _static, true);

        FunctionDDImpl<T> functionDDImpl;
        if (isLambda) {
            functionDDImpl = new LambdaFunction<>(name, rawName, scope, _static, _const, file, startOffset, endOffset, global);        
        } else {
            functionDDImpl = new FunctionDDImpl<>(name, rawName, scope, _static, _const, file, startOffset, endOffset, global);        
        }
        temporaryRepositoryRegistration(ast, global, functionDDImpl);
        
        StringBuilder clsTemplateSuffix = new StringBuilder();
        TemplateDescriptor templateDescriptor = createTemplateDescriptor(ast, file, functionDDImpl, clsTemplateSuffix, global);
        CharSequence classTemplateSuffix = NameCache.getManager().getString(clsTemplateSuffix);
        
        functionDDImpl.setTemplateDescriptor(templateDescriptor, classTemplateSuffix);
        functionDDImpl.setReturnType(AstRenderer.FunctionRenderer.createReturnType(ast, functionDDImpl, file));
        functionDDImpl.setParameters(AstRenderer.FunctionRenderer.createParameters(ast, functionDDImpl, file, fileContent), 
                AstRenderer.FunctionRenderer.isVoidParameter(ast));
        CsmCompoundStatement body = AstRenderer.findCompoundStatement(ast, file, functionDDImpl);
        if (body == null) {
            throw AstRendererException.throwAstRendererException((FileImpl)file, ast, startOffset,
                    "Null body in method definition."); // NOI18N
        }        
        functionDDImpl.setCompoundStatement(body);
        

        postObjectCreateRegistration(global, functionDDImpl);
        nameHolder.addReference(fileContent, functionDDImpl);
        return functionDDImpl;
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
        if (!isValid()) {
            return this;
        }
        if( isCStyleStatic() ) {
            CharSequence name = getName();
            CsmFilter filter = CsmSelect.getFilterBuilder().createNameFilter(
                               name, true, true, false);
            Iterator<CsmFunction> it = CsmSelect.getStaticFunctions(getContainingFile(), filter);
            while(it.hasNext()){
                CsmFunction fun = it.next();
                if( name.equals(fun.getName()) ) {
                    return fun;
                }
            }
            return this;
        }
        String uname = ""+Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION) + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix(); //NOI18N
        CsmProject prj = getContainingFile().getProject();
        CsmDeclaration decl = findDeclaration(prj, uname);
        if (decl != null && decl.getKind() == CsmDeclaration.Kind.FUNCTION) {
            if (!isStatic() || CsmKindUtilities.isClassMember(this)
                    || !CsmKindUtilities.isOffsetableDeclaration(decl)
                    || getContainingFile().equals(((CsmOffsetableDeclaration)decl).getContainingFile())) {
                return (CsmFunction) decl;
            }
        }
        for (CsmProject lib : prj.getLibraries()) {
            CsmFunction def = findDeclaration(lib, uname);
            if (def != null) {
                return def;
            }
        }
        if(decl == null) {
            uname = ""+Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_FRIEND) + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix(); //NOI18N
            decl = findDeclaration(prj, uname);
            if (decl != null && decl.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND) {
                if (!isStatic() || CsmKindUtilities.isClassMember(this)
                        || !CsmKindUtilities.isOffsetableDeclaration(decl)
                        || getContainingFile().equals(((CsmOffsetableDeclaration)decl).getContainingFile())) {
                    return (CsmFunction) decl;
                }
            }
            for (CsmProject lib : prj.getLibraries()) {
                CsmFunction def = findDeclaration(lib, uname);
                if (def != null) {
                    return def;
                }
            }
        }
        return this;
    }

    @Override
    public boolean isPureDefinition() {
        return false;
    }

    private CsmFunction findDeclaration(CsmProject prj, String uname){
        Collection<CsmDeclaration> decls = new ArrayList<>(1);
        for(CsmOffsetableDeclaration candidate : prj.findDeclarations(uname)) {
            if ((candidate.getKind() == CsmDeclaration.Kind.FUNCTION ||
                candidate.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND)) {
                if (FunctionImpl.isObjectVisibleInFile(getContainingFile(), candidate)) {
                    decls.add(candidate);
                }
            }
        }
        CsmDeclaration decl = chooseDeclaration(decls);
        if( decl != null) {
            return (CsmFunction) decl;
        }
        FunctionParameterListImpl parameterList = getParameterList();
        if (parameterList != null && !parameterList.isEmpty()) {
            CsmFile file = getContainingFile();
            if (!Utils.isCppFile(file)){
                uname = uname.substring(0,uname.indexOf('('))+"()"; // NOI18N
                decl = prj.findDeclaration(uname);
                if( (decl instanceof FunctionImpl<?>) &&
                        !((FunctionImpl<?>)decl).isVoidParameterList()) {
                    return (CsmFunction) decl;
                }
            }
        }
        return null;
    }

    @Override
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.FUNCTION_DEFINITION;
    }

    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        Collection<CsmScopeElement> l = super.getScopeElements();
        l.add(getBody());
        return l;
    }

    public static class FunctionDDBuilder extends FunctionBuilder implements StatementBuilderContainer {

        CompoundStatementBuilder bodyBuilder;
        
        public void setBodyBuilder(CompoundStatementBuilder builder) {
            bodyBuilder = builder;
        }
        
        @Override
        public FunctionDDImpl create() {
            final FunctionParameterListBuilder parameters = (FunctionParameterListBuilder)getParametersListBuilder();
            if (parameters == null) {
                return null;
            }
            CsmScope scope = AstRenderer.FunctionRenderer.getScope(getScope(), getFile(), isStatic(), true);

            FunctionDDImpl<?> functionDDImpl = new FunctionDDImpl(getName(), getRawName(), scope, isStatic(), FunctionImpl.CV_RL.isConst(isConst()), getFile(), getStartOffset(), getEndOffset(), true);        
            temporaryRepositoryRegistration(true, functionDDImpl);

//            StringBuilder clsTemplateSuffix = new StringBuilder();
//            TemplateDescriptor templateDescriptor = createTemplateDescriptor(ast, file, functionDDImpl, clsTemplateSuffix, global);
//            CharSequence classTemplateSuffix = NameCache.getManager().getString(clsTemplateSuffix);
//
//            functionDDImpl.setTemplateDescriptor(templateDescriptor, classTemplateSuffix);
            if(getTemplateDescriptorBuilder() != null) {
                functionDDImpl.setTemplateDescriptor(getTemplateDescriptor(), NameCache.getManager().getString(CharSequences.create(""))); // NOI18N
            }
            
            functionDDImpl.setReturnType(getType());
            parameters.setScope(functionDDImpl);
            functionDDImpl.setParameters(parameters.create(), false);
            
            bodyBuilder.setScope(functionDDImpl);
            functionDDImpl.setCompoundStatement(bodyBuilder.create());

            postObjectCreateRegistration(true, functionDDImpl);
            getNameHolder().addReference(getFileContent(), functionDDImpl);
            
            addDeclaration(functionDDImpl);
            
            return functionDDImpl;
        }        

        @Override
        public void addStatementBuilder(StatementBuilder builder) {
            assert builder instanceof CompoundStatementBuilder;
            setBodyBuilder((CompoundStatementBuilder)builder);
        }
        
        protected void setBody(FunctionDDImpl fun) {
            bodyBuilder.setScope(fun);
            fun.setCompoundStatement(bodyBuilder.create());
        }        
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        assert this.body != null: "null body in " + this.getQualifiedName();
        PersistentUtils.writeCompoundStatement(body, output);
    }

    public FunctionDDImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.body = PersistentUtils.readCompoundStatement(input);
        assert this.body != null: "read null body for " + this.getName();
    }
}

