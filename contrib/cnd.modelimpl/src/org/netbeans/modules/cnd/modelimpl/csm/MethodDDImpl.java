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
import java.util.List;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionParameterListImpl.FunctionParameterListBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.Disposable;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.deep.CompoundStatementImpl.CompoundStatementBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.StatementBase.StatementBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.StatementBase.StatementBuilderContainer;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.util.CharSequences;

/**
 * Method, which contains it's body right at throws POD (point of declaration)
 */
public class MethodDDImpl<T> extends MethodImpl<T> implements CsmFunctionDefinition {

    private CsmCompoundStatement body;

    private final DefinitionKind definitionKind;

    protected MethodDDImpl(CharSequence name, CharSequence rawName, CsmClass cls, CsmVisibility visibility, DefinitionKind defKind, boolean _virtual, boolean _override, boolean _final, boolean _explicit, boolean _static, FunctionImpl.CV_RL _const, CsmFile file, int startOffset, int endOffset, boolean global) {
        super(name, rawName, cls, visibility, _virtual, _override, _final, _explicit, _static, _const, false, file, startOffset, endOffset, global);
        this.definitionKind = defKind;
    }

    public static<T> MethodDDImpl<T> create(AST ast, final CsmFile file, FileContent fileContent, ClassImpl cls, CsmVisibility visibility, boolean global) throws AstRendererException {
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
        boolean _override = false;
        boolean _final = false;
        boolean _explicit = false;
        boolean afterParen = false;
        boolean afterAssignEqual = false;
        DefinitionKind defKind = DefinitionKind.REGULAR;
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.LITERAL_static:
                    _static = true;
                    break;
                case CPPTokenTypes.LITERAL_virtual:
                    _virtual = true;
                    break;
                case CPPTokenTypes.LITERAL_override:
                    _override = true;
                    break;
                case CPPTokenTypes.LITERAL_final:
                    _final = true;
                    break;
                case CPPTokenTypes.LITERAL_explicit:
                    _explicit = true;
                    break;
                case CPPTokenTypes.RPAREN:
                    afterParen = true;
                    break;
                case CPPTokenTypes.ASSIGNEQUAL:
                    if (afterParen) {
                        afterAssignEqual = true;
                    }
                    break;
                case CPPTokenTypes.LITERAL_delete:
                    if (afterAssignEqual) {
                        defKind = DefinitionKind.DELETE;
                    }
                    break;
                case CPPTokenTypes.LITERAL_default:
                    if (afterAssignEqual) {
                        defKind = DefinitionKind.DEFAULT;
                    }
                    break;
            }
        }

        MethodDDImpl<T> methodDDImpl = new MethodDDImpl<>(name, rawName, cls, visibility, defKind, _virtual, _override, _final, _explicit, _static, _const, file, startOffset, endOffset, global);
        temporaryRepositoryRegistration(ast, global, methodDDImpl);

        StringBuilder clsTemplateSuffix = new StringBuilder();
        TemplateDescriptor templateDescriptor = createTemplateDescriptor(ast, file, methodDDImpl, clsTemplateSuffix, global);
        CharSequence classTemplateSuffix = NameCache.getManager().getString(clsTemplateSuffix);

        methodDDImpl.setTemplateDescriptor(templateDescriptor, classTemplateSuffix);
        methodDDImpl.setReturnType(AstRenderer.FunctionRenderer.createReturnType(ast, methodDDImpl, file));
        methodDDImpl.setParameters(AstRenderer.FunctionRenderer.createParameters(ast, methodDDImpl, file, fileContent),
                AstRenderer.FunctionRenderer.isVoidParameter(ast));
        CsmCompoundStatement body = AstRenderer.findCompoundStatement(ast, file, methodDDImpl);
        if (body == null) {
            throw AstRendererException.throwAstRendererException((FileImpl)file, ast, startOffset,
                    "Null body in method definition."); // NOI18N
        }
        methodDDImpl.setCompoundStatement(body);

        postObjectCreateRegistration(global, methodDDImpl);
        nameHolder.addReference(fileContent, methodDDImpl);
        return methodDDImpl;
    }

    protected void setCompoundStatement(CsmCompoundStatement body) {
        this.body = body;
    }

    @Override
    public CsmFunction getDeclaration() {
        return this;
    }

    @Override
    public DefinitionKind getDefinitionKind() {
        return definitionKind;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (body instanceof Disposable) {
            ((Disposable)body).dispose();
        }
    }

    @Override
    public CsmCompoundStatement getBody() {
        return body;
    }

    @Override
    public Kind getKind() {
        return CsmDeclaration.Kind.FUNCTION_DEFINITION;
    }

    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        Collection<CsmScopeElement> l = super.getScopeElements();
        l.add(getBody());
        return l;
    }

    public static class MethodDDBuilder extends MethodBuilder implements StatementBuilderContainer {

        private CompoundStatementBuilder bodyBuilder;

        private final List<Token> bodyTokens = new ArrayList<>();

        public void setBodyBuilder(CompoundStatementBuilder builder) {
            bodyBuilder = builder;
        }

        public CompoundStatementBuilder getBodyBuilder() {
            return bodyBuilder;
        }

        public void addBodyToken(Token token) {
            if (!APTUtils.isEOF(token)) {
                bodyTokens.add(token);
            }
        }

        public TokenStream getBodyTokenStream() {
            if(bodyTokens.isEmpty()) {
                return null;
            } else {
                return new TokenStream() {

                    int index = 0;

                    @Override
                    public Token nextToken() throws TokenStreamException {
                        if(bodyTokens.size() > index) {
                            index++;
                            return bodyTokens.get(index - 1);
                        } else {
                            return null;
                        }
                    }
                };
            }
        }

        @Override
        public MethodDDImpl create(CsmParserProvider.ParserErrorDelegate delegate) {
            final FunctionParameterListBuilder parameters = (FunctionParameterListBuilder)getParametersListBuilder();
            if (parameters == null) {
                return null;
            }
            final CompoundStatementBuilder aBodyBuilder = getBodyBuilder();
            if (aBodyBuilder == null) {
                return null;
            }
            CsmClass cls = (CsmClass) getScope();
            boolean _virtual = false;
            boolean _explicit = false;


            MethodDDImpl method = new MethodDDImpl(getName(), getRawName(), cls, getVisibility(), DefinitionKind.REGULAR, _virtual, false, false, _explicit, isStatic(), FunctionImpl.CV_RL.isConst(isConst()), getFile(), getStartOffset(), getEndOffset(), true);
            temporaryRepositoryRegistration(true, method);

            //StringBuilder clsTemplateSuffix = new StringBuilder();
            //TemplateDescriptor templateDescriptor = createTemplateDescriptor(ast, file, functionImpl, clsTemplateSuffix, global);
            //CharSequence classTemplateSuffix = NameCache.getManager().getString(clsTemplateSuffix);

            //functionImpl.setTemplateDescriptor(templateDescriptor, classTemplateSuffix);
            if(getTemplateDescriptorBuilder() != null) {
                method.setTemplateDescriptor(getTemplateDescriptor(), NameCache.getManager().getString(CharSequences.create(""))); // NOI18N
            }

            method.setReturnType(getType());
            parameters.setScope(method);
            method.setParameters(parameters.create(), true);

            postObjectCreateRegistration(true, method);
            getNameHolder().addReference(getFileContent(), method);

            addDeclaration(method);

            aBodyBuilder.setScope(method);
            method.setCompoundStatement(aBodyBuilder.create());

            postObjectCreateRegistration(true, method);
            getNameHolder().addReference(getFileContent(), method);

//            addMember(method);

            return method;
        }

        @Override
        public void addStatementBuilder(StatementBuilder builder) {
            assert builder instanceof CompoundStatementBuilder;
            setBodyBuilder((CompoundStatementBuilder)builder);
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeCompoundStatement(this.body, output);
        output.writeByte(definitionKind.toByte());
    }

    public MethodDDImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.body = PersistentUtils.readCompoundStatement(input);
        this.definitionKind = DefinitionKind.fromByte(input.readByte());
    }
}
