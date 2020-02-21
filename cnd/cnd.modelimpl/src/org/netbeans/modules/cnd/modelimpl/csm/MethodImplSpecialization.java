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
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.util.CharSequences;

/**
 * Template function explicit specialization declaration.
 *
 */
public class MethodImplSpecialization<T> extends MethodImpl<T> {

    protected MethodImplSpecialization(CharSequence name, CharSequence rawName, CsmClass cls, CsmVisibility visibility, boolean _virtual, boolean _override, boolean _final, boolean _explicit, boolean _static, FunctionImpl.CV_RL _const, boolean _abstract, CsmFile file, int startOffset, int endOffset, boolean global) {
        super(name, rawName, cls, visibility, _virtual, _override, _final, _explicit, _static, _const, _abstract, file, startOffset, endOffset, global);
    }

    public static<T> MethodImplSpecialization<T> create(AST ast, final CsmFile file, FileContent fileContent, ClassImpl cls, CsmVisibility visibility, boolean global) throws AstRendererException {
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
        boolean _abstract = false;
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
                        _abstract = true;
                    }
                    break;
            }
        }

        MethodImplSpecialization<T> methodImpl = new MethodImplSpecialization<>(name, rawName, cls, visibility, _virtual, _override, _final, _explicit, _static, _const, _abstract, file, startOffset, endOffset, global);
        temporaryRepositoryRegistration(ast, global, methodImpl);

        StringBuilder clsTemplateSuffix = new StringBuilder();
        TemplateDescriptor templateDescriptor = createTemplateDescriptor(ast, file, methodImpl, clsTemplateSuffix, global);
        CharSequence classTemplateSuffix = NameCache.getManager().getString(clsTemplateSuffix);

        methodImpl.setTemplateDescriptor(templateDescriptor, classTemplateSuffix);
        methodImpl.setReturnType(AstRenderer.FunctionRenderer.createReturnType(ast, methodImpl, file));
        methodImpl.setParameters(AstRenderer.FunctionRenderer.createParameters(ast, methodImpl, file, fileContent),
                AstRenderer.FunctionRenderer.isVoidParameter(ast));

        postObjectCreateRegistration(global, methodImpl);
        nameHolder.addReference(fileContent, methodImpl);
        return methodImpl;
    }

    private static CharSequence getFunctionName(AST ast) {
        CharSequence funName = CharSequences.create(AstUtil.findId(ast, CPPTokenTypes.RCURLY, true));
        return getFunctionNameFromFunctionSpecialicationName(funName);
    }

    private static CharSequence getFunctionNameFromFunctionSpecialicationName(CharSequence functionName) {
        CharSequence[] nameParts = Utils.splitQualifiedName(functionName.toString());
        StringBuilder className = new StringBuilder("");
        if(nameParts.length > 0) {
            className.append(nameParts[nameParts.length - 1]);
        }
        return className;
    }


    public static class MethodSpecializationBuilder extends MethodBuilder {

        @Override
        public MethodImplSpecialization create(CsmParserProvider.ParserErrorDelegate delegate) {
            MethodImplSpecialization fun = new MethodImplSpecialization(getName(), getRawName(), (CsmClass)getScope(), getVisibility(), isVirtual(), false, false, isExplicit(),  isStatic(), FunctionImpl.CV_RL.isConst(isConst()), false, getFile(), getStartOffset(), getEndOffset(), isGlobal());
            init(fun);
            return fun;
        }

        protected void init(FunctionImplEx fun) {
            temporaryRepositoryRegistration(isGlobal(), fun);

            setTemplateDescriptor(fun);
            setReturnType(fun);
            setParameters(fun);

            postObjectCreateRegistration(isGlobal(), fun);
            addReference(fun);
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
    }

    public MethodImplSpecialization(RepositoryDataInput input) throws IOException {
        super(input);
    }
}
