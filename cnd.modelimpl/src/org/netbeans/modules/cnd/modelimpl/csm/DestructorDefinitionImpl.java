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
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionParameterListImpl.FunctionParameterListBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.deep.StatementBase.StatementBuilderContainer;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.openide.util.CharSequences;

/**
 */
public final class DestructorDefinitionImpl extends FunctionDefinitionImpl<CsmFunctionDefinition> {

    protected DestructorDefinitionImpl(CharSequence name, CharSequence rawName, CsmScope scope, boolean _static, FunctionImpl.CV_RL _const, CsmFile file, int startOffset, int endOffset, boolean global) {
        super(name, rawName, scope, _static, _const, file, startOffset, endOffset, global);
    }

    public static DestructorDefinitionImpl create(AST ast, CsmFile file, FileContent fileContent, boolean global) throws AstRendererException{
        CsmScope scope = null;
        
        int startOffset = getStartOffset(ast);
        int endOffset = getEndOffset(ast);
        
        NameHolder nameHolder = NameHolder.createDestructorDefinitionName(ast);
        CharSequence name = QualifiedNameCache.getManager().getString(nameHolder.getName());
        if (name.length() == 0) {
            AstRendererException.throwAstRendererException((FileImpl) file, ast, startOffset, "Empty function name."); // NOI18N
        }
        CharSequence rawName = initRawName(ast);
        
        boolean _static = AstRenderer.FunctionRenderer.isStatic(ast, file, fileContent, name);
        FunctionImpl.CV_RL _const = AstRenderer.FunctionRenderer.isConst(ast);

        DestructorDefinitionImpl res = new DestructorDefinitionImpl(name, rawName, scope, _static, _const, file, startOffset, endOffset, global);        
        
        temporaryRepositoryRegistration(ast, global, res);
        
        StringBuilder clsTemplateSuffix = new StringBuilder();
        TemplateDescriptor templateDescriptor = createTemplateDescriptor(ast, file, res, clsTemplateSuffix, global);
        CharSequence classTemplateSuffix = NameCache.getManager().getString(clsTemplateSuffix);
        
        res.setTemplateDescriptor(templateDescriptor, classTemplateSuffix);
        res.setReturnType(AstRenderer.FunctionRenderer.createReturnType(ast, res, file));
        res.setParameters(AstRenderer.FunctionRenderer.createParameters(ast, res, file, fileContent), 
                AstRenderer.FunctionRenderer.isVoidParameter(ast));        
        
        CharSequence[] classOrNspNames = CastUtils.isCast(ast) ?
            getClassOrNspNames(ast) :
            res.initClassOrNspNames(ast);
        res.setClassOrNspNames(classOrNspNames);        

        CsmCompoundStatement body = AstRenderer.findCompoundStatement(ast, file, res);
        if (body == null) {
            throw AstRendererException.throwAstRendererException((FileImpl)file, ast, startOffset,
                    "Null body in method definition."); // NOI18N
        }        
        res.setCompoundStatement(body);
        
        postObjectCreateRegistration(global, res);
        postFunctionImpExCreateRegistration(fileContent, global, res);
        nameHolder.addReference(fileContent, res);
        return res;
    }

    @Override
    public CsmType getReturnType() {
        return NoType.instance();
    }
    
    public static class DestructorDefinitionBuilder extends FunctionDefinitionBuilder implements StatementBuilderContainer {
        @Override
        public DestructorDefinitionImpl create() {
            CsmScope scope = AstRenderer.FunctionRenderer.getScope(getScope(), getFile(), isStatic(), true);

            DestructorDefinitionImpl impl = new DestructorDefinitionImpl(getName(), getRawName(), scope, isStatic(), FunctionImpl.CV_RL.isConst(isConst()), getFile(), getStartOffset(), getEndOffset(), true);
            temporaryRepositoryRegistration(true, impl);

            if(getTemplateDescriptorBuilder() != null) {
                impl.setTemplateDescriptor(getTemplateDescriptor(), NameCache.getManager().getString(CharSequences.create(""))); // NOI18N
            }
            
            ((FunctionParameterListBuilder)getParametersListBuilder()).setScope(impl);
            impl.setParameters(((FunctionParameterListBuilder)getParametersListBuilder()).create(), false);
            
            impl.setClassOrNspNames(getScopeNames());        
            
            getBodyBuilder().setScope(impl);
            impl.setCompoundStatement(getBodyBuilder().create());

            postObjectCreateRegistration(true, impl);
            postFunctionImpExCreateRegistration(getFileContent(), isGlobal(), impl);
            getNameHolder().addReference(getFileContent(), impl);
            
            addDeclaration(impl);
            
            return impl;
        }
    }    

    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent

    public DestructorDefinitionImpl(RepositoryDataInput input) throws IOException {
        super(input);
    }

}
