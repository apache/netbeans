/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.modelimpl.csm;

import java.io.IOException;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionParameterListImpl.FunctionParameterListBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.deep.CompoundStatementImpl;
import org.netbeans.modules.cnd.modelimpl.csm.deep.StatementBase.StatementBuilderContainer;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.openide.util.CharSequences;

/**
 */
public final class DestructorDDImpl extends MethodDDImpl<CsmMethod> {

    protected DestructorDDImpl(CharSequence name, CharSequence rawName, CsmClass cls, CsmVisibility visibility, DefinitionKind defKind,  boolean _virtual, boolean _explicit, boolean _static, FunctionImpl.CV_RL _const, CsmFile file, int startOffset, int endOffset, boolean global) {
        super(name, rawName, cls, visibility, defKind, _virtual, false, false, _explicit, _static, _const, file, startOffset, endOffset, global);
    }

    public static DestructorDDImpl createDestructor(AST ast, final CsmFile file, FileContent fileContent, ClassImpl cls, CsmVisibility visibility, boolean global) throws AstRendererException {
        int startOffset = getStartOffset(ast);
        int endOffset = getEndOffset(ast);

        NameHolder nameHolder = NameHolder.createDestructorName(ast);
        CharSequence name = QualifiedNameCache.getManager().getString(nameHolder.getName());
        if (name.length() == 0) {
            AstRendererException.throwAstRendererException((FileImpl) file, ast, startOffset, "Empty function name."); // NOI18N
        }
        CharSequence rawName = initRawName(ast);

        boolean _static = AstRenderer.FunctionRenderer.isStatic(ast, file, fileContent, name);
        FunctionImpl.CV_RL _const = AstRenderer.FunctionRenderer.isConst(ast);
        boolean _virtual = false;
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

        DestructorDDImpl destructorDDImpl = new DestructorDDImpl(name, rawName, cls, visibility, defKind, _virtual, _explicit, _static, _const, file, startOffset, endOffset, global);
        temporaryRepositoryRegistration(ast, global, destructorDDImpl);

        StringBuilder clsTemplateSuffix = new StringBuilder();
        TemplateDescriptor templateDescriptor = createTemplateDescriptor(ast, file, destructorDDImpl, clsTemplateSuffix, global);
        CharSequence classTemplateSuffix = NameCache.getManager().getString(clsTemplateSuffix);

        destructorDDImpl.setTemplateDescriptor(templateDescriptor, classTemplateSuffix);
        destructorDDImpl.setReturnType(AstRenderer.FunctionRenderer.createReturnType(ast, destructorDDImpl, file));
        destructorDDImpl.setParameters(AstRenderer.FunctionRenderer.createParameters(ast, destructorDDImpl, file, fileContent),
                AstRenderer.FunctionRenderer.isVoidParameter(ast));
        CsmCompoundStatement body = AstRenderer.findCompoundStatement(ast, file, destructorDDImpl);
        if (body == null) {
            throw AstRendererException.throwAstRendererException((FileImpl)file, ast, startOffset,
                    "Null body in method definition."); // NOI18N
        }
        destructorDDImpl.setCompoundStatement(body);
        postObjectCreateRegistration(global, destructorDDImpl);
        nameHolder.addReference(fileContent, destructorDDImpl);
        return destructorDDImpl;
    }

    @Override
    public CsmType getReturnType() {
        return NoType.instance();
    }


    public static class DestructorDDBuilder extends MethodDDBuilder implements StatementBuilderContainer {
        @Override
        public DestructorDDImpl create(CsmParserProvider.ParserErrorDelegate delegate) {
            final FunctionParameterListBuilder parameters = (FunctionParameterListBuilder)getParametersListBuilder();
            if (parameters == null) {
                return null;
            }
            final CompoundStatementImpl.CompoundStatementBuilder bodyBuilder = getBodyBuilder();
            if (bodyBuilder == null) {
                return null;
            }
            CsmClass cls = (CsmClass) getScope();
            boolean _virtual = false;
            boolean _explicit = false;


            DestructorDDImpl method = new DestructorDDImpl(getName(), getRawName(), cls, getVisibility(), DefinitionKind.REGULAR, _virtual, _explicit, isStatic(), FunctionImpl.CV_RL.isConst(isConst()), getFile(), getStartOffset(), getEndOffset(), true);
            temporaryRepositoryRegistration(true, method);

            //StringBuilder clsTemplateSuffix = new StringBuilder();
            //TemplateDescriptor templateDescriptor = createTemplateDescriptor(ast, file, functionImpl, clsTemplateSuffix, global);
            //CharSequence classTemplateSuffix = NameCache.getManager().getString(clsTemplateSuffix);

            //functionImpl.setTemplateDescriptor(templateDescriptor, classTemplateSuffix);
            if(getTemplateDescriptorBuilder() != null) {
                method.setTemplateDescriptor(getTemplateDescriptor(), NameCache.getManager().getString(CharSequences.create(""))); // NOI18N
            }

            //method.setReturnType(getType());
            parameters.setScope(method);
            method.setParameters(parameters.create(), true);

            postObjectCreateRegistration(true, method);
            getNameHolder().addReference(getFileContent(), method);

            addDeclaration(method);

            bodyBuilder.setScope(method);
            method.setCompoundStatement(bodyBuilder.create());

            postObjectCreateRegistration(true, method);
            getNameHolder().addReference(getFileContent(), method);

//            addMember(method);

            return method;
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent

    public DestructorDDImpl(RepositoryDataInput input) throws IOException {
        super(input);
    }

}
