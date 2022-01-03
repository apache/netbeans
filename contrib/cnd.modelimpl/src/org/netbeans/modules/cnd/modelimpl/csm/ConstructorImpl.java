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
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmConstructor;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionParameterListImpl.FunctionParameterListBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.openide.util.CharSequences;

/**
 * CsmConstructor implementation
 */
public final class ConstructorImpl extends MethodImpl<CsmConstructor> implements CsmConstructor {

    protected ConstructorImpl(CharSequence name, CharSequence rawName, CsmClass cls, CsmVisibility visibility,  boolean _virtual, boolean _explicit, boolean _static, FunctionImpl.CV_RL _const, CsmFile file, int startOffset, int endOffset, boolean global) {
        super(name, rawName, cls, visibility, _virtual, false, false, _explicit, _static, _const, false, file, startOffset, endOffset, global);
    }

    public static ConstructorImpl createConstructor(AST ast, final CsmFile file, FileContent fileContent, ClassImpl cls, CsmVisibility visibility, boolean global) throws AstRendererException {
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
        boolean _virtual = false;
        boolean _explicit = false;
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.LITERAL_static:
                    _static = true;
                    break;
                case CPPTokenTypes.LITERAL_virtual:
                    _virtual = true;
                    break;
                case CPPTokenTypes.LITERAL_explicit:
                    _explicit = true;
                    break;
            }
        }

        ConstructorImpl constructorImpl = new ConstructorImpl(name, rawName, cls, visibility, _virtual, _explicit, _static, _const, file, startOffset, endOffset, global);
        temporaryRepositoryRegistration(ast, global, constructorImpl);

        StringBuilder clsTemplateSuffix = new StringBuilder();
        TemplateDescriptor templateDescriptor = createTemplateDescriptor(ast, file, constructorImpl, clsTemplateSuffix, global);
        CharSequence classTemplateSuffix = NameCache.getManager().getString(clsTemplateSuffix);

        constructorImpl.setTemplateDescriptor(templateDescriptor, classTemplateSuffix);
        constructorImpl.setReturnType(AstRenderer.FunctionRenderer.createReturnType(ast, constructorImpl, file));
        constructorImpl.setParameters(AstRenderer.FunctionRenderer.createParameters(ast, constructorImpl, file, fileContent),
                AstRenderer.FunctionRenderer.isVoidParameter(ast));
        postObjectCreateRegistration(global, constructorImpl);
        nameHolder.addReference(fileContent, constructorImpl);
        return constructorImpl;
    }

    @Override
    public Collection<CsmExpression> getInitializerList() {
        return Collections.<CsmExpression>emptyList();
    }

    @Override
    public CsmType getReturnType() {
        return NoType.instance();
    }

    public static class ConstructorBuilder extends MethodBuilder {

        public ConstructorBuilder(SimpleDeclarationBuilder builder) {
            super(builder);
        }

        @Override
        public ConstructorImpl create(CsmParserProvider.ParserErrorDelegate delegate) {
            CsmClass cls = (CsmClass) getScope();
            boolean _virtual = false;
            boolean _explicit = false;


            ConstructorImpl method = new ConstructorImpl(getName(), getRawName(), cls, getVisibility(), _virtual, _explicit, isStatic(), FunctionImpl.CV_RL.isConst(isConst()), getFile(), getStartOffset(), getEndOffset(), true);
            temporaryRepositoryRegistration(true, method);

            //StringBuilder clsTemplateSuffix = new StringBuilder();
            //TemplateDescriptor templateDescriptor = createTemplateDescriptor(ast, file, functionImpl, clsTemplateSuffix, global);
            //CharSequence classTemplateSuffix = NameCache.getManager().getString(clsTemplateSuffix);

            //functionImpl.setTemplateDescriptor(templateDescriptor, classTemplateSuffix);
            if(getTemplateDescriptorBuilder() != null) {
                method.setTemplateDescriptor(getTemplateDescriptor(), NameCache.getManager().getString(CharSequences.create(""))); // NOI18N
            }

            //method.setReturnType(getType());
            ((FunctionParameterListBuilder)getParametersListBuilder()).setScope(method);
            method.setParameters(((FunctionParameterListBuilder)getParametersListBuilder()).create(),
                    true);

            postObjectCreateRegistration(true, method);
            getNameHolder().addReference(getFileContent(), method);

//            addMember(method);
            return method;
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent

    public ConstructorImpl(RepositoryDataInput input) throws IOException {
        super(input);
    }
}
