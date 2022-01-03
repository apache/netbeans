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
import java.util.Objects;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl.MemberBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionParameterListImpl.FunctionParameterListBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import static org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities.UID_INTERNAL_DATA_PREFIX;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.util.CharSequences;

/**
 * CsmFunction + CsmMember implementation
 * @param T
 */
public class MethodImpl<T> extends FunctionImpl<T> implements CsmMethod {

    private final CsmVisibility visibility;
    private static final short ABSTRACT = 1 << (FunctionImpl.LAST_USED_FLAG_INDEX+1);
    private static final short VIRTUAL = 1 << (FunctionImpl.LAST_USED_FLAG_INDEX+2);
    private static final short EXPLICIT = 1 << (FunctionImpl.LAST_USED_FLAG_INDEX+3);
    private static final short OVERRIDE = 1 << (FunctionImpl.LAST_USED_FLAG_INDEX+4);
    private static final short FINAL = 1 << (FunctionImpl.LAST_USED_FLAG_INDEX+5);

    protected MethodImpl(CharSequence name, CharSequence rawName, CsmClass cls, CsmVisibility visibility, boolean _virtual, boolean _override, boolean _final, boolean _explicit, boolean _static, FunctionImpl.CV_RL _const, boolean _abstract, CsmFile file, int startOffset, int endOffset, boolean global) {
        super(name, rawName, cls, _static, _const, file, startOffset, endOffset, global);
        this.visibility = visibility;
        setVirtual(_virtual);
        setOverride(_override);
        setFinal(_final);
        setExplicit(_explicit);
        setAbstract(_abstract);
    }

    public static <T> MethodImpl<T> create(AST ast, final CsmFile file, FileContent fileContent, ClassImpl cls, CsmVisibility visibility, boolean global) throws AstRendererException {
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

        MethodImpl<T> methodImpl = new MethodImpl<>(name, rawName, cls, visibility, _virtual, _override, _final, _explicit, _static, _const, _abstract, file, startOffset, endOffset, global);
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

    @Override
    public CsmClass getContainingClass() {
        return (CsmClass) getScope();
    }

    @Override
    public CsmVisibility getVisibility() {
        return visibility;
    }

    @Override
    public boolean isAbstract() {
        return hasFlags(ABSTRACT);
    }

    private void setAbstract(boolean _abstract) {
        setFlags(ABSTRACT, _abstract);
    }

    private void setVirtual(boolean _virtual) {
        setFlags(VIRTUAL, _virtual);
    }

    private void setOverride(boolean _override) {
        setFlags(OVERRIDE, _override);
    }

    private void setFinal(boolean _final) {
        setFlags(FINAL, _final);
    }

    private void setExplicit(boolean _explicit) {
        setFlags(EXPLICIT, _explicit);
    }

    @Override
    public boolean isExplicit() {
        return hasFlags(EXPLICIT);
    }

    @Override
    public boolean isVirtual() {
        //TODO: implement!
        // returns direct "virtual" keyword presence
        return hasFlags(VIRTUAL);
    }

    @Override
    public boolean isOverride() {
        return hasFlags(OVERRIDE);
    }

    @Override
    public boolean isFinal() {
        return hasFlags(FINAL);
    }

    @Override
    public boolean isConst() {
        return super.isConst();
    }

    @Override
    public boolean isVolatile() {
        return super.isVolatile();
    }

    @Override
    public boolean isLValue() {
        return super.isLValue();

    }
    @Override
    public boolean isRValue() {
        return super.isRValue();
    }

    @Override
    protected CharSequence createUIDExtraSuffix(AST ast) {
        CharSequence funSuffix = super.createUIDExtraSuffix(ast);
        if (CsmKindUtilities.isMethodDeclaration(this)) {
            // Check if this is method declaration which is added in class via #include directive:
            // struct AAA {
            //  #include "body.inc"
            // };
            CsmClass cls = getContainingClass();
            CsmFile clsFile = cls != null ? cls.getContainingFile() : null;
            if (cls != null && clsFile != null && !Objects.equals(clsFile, getContainingFile())) {
                StringBuilder sb = new StringBuilder(UID_INTERNAL_DATA_PREFIX);
                sb.append(INCLUDED_DECLARATION);
                sb.append(cls.getName());
                sb.append("_"); // NOI18N
                if (clsFile instanceof FileImpl) {
                    sb.append(((FileImpl) clsFile).getFileId());
                } else {
                    // Absolute path shouldn't be used here
                    sb.append(clsFile.getName());
                }
                if (funSuffix != null) {
                    sb.append(funSuffix);
                }
                String mtdSuffix = sb.toString();
                if (mtdSuffix.length() > UID_EXTRA_SUFFIX_MAX_LENGTH) {
                    mtdSuffix = mtdSuffix.substring(0, UID_EXTRA_SUFFIX_MAX_LENGTH) + UID_EXTRA_SUFFIX_TOO_LONG;
                }
                return mtdSuffix;
            }
        }
        return funSuffix;
    }

    public static class MethodBuilder extends FunctionBuilder implements MemberBuilder {

        private final boolean _virtual = false;
        private final boolean _explicit = false;
        private CsmVisibility visibility = CsmVisibility.PUBLIC;

        protected MethodBuilder() {
        }

        public MethodBuilder(SimpleDeclarationBuilder builder) {
            super(builder);
        }

        public CsmVisibility getVisibility() {
            return visibility;
        }

        @Override
        public void setVisibility(CsmVisibility visibility) {
            this.visibility = visibility;
        }

        public boolean isVirtual() {
            return _virtual;
        }

        public boolean isExplicit() {
            return _explicit;
        }

        @Override
        public MethodImpl create(CsmParserProvider.ParserErrorDelegate delegate) {
            final FunctionParameterListBuilder parameters = (FunctionParameterListBuilder)getParametersListBuilder();
            if (parameters == null) {
                return null;
            }
            CsmClass cls = (CsmClass) getScope();

            MethodImpl method = new MethodImpl(getName(), getRawName(), cls, getVisibility(), isVirtual(), false, false, isExplicit(), isStatic(), FunctionImpl.CV_RL.isConst(isConst()), false, getFile(), getStartOffset(), getEndOffset(), true);
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

//            addMember(method);
            return method;
        }

//        protected void addMember(CsmMember member) {
//            if (getParent() instanceof ClassImpl.ClassBuilder) {
//                ((ClassImpl.ClassBuilder) getParent()).addMember(member);
//            }
//        }

    }


////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeVisibility(this.visibility, output);
    }

    public MethodImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.visibility = PersistentUtils.readVisibility(input);
    }
}

