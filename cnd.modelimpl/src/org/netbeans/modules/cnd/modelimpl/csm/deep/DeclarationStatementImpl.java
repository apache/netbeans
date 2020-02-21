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
package org.netbeans.modules.cnd.modelimpl.csm.deep;

import java.util.*;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;

import org.netbeans.modules.cnd.modelimpl.csm.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase.SimpleDeclarationBuilder;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

/**
 * Implementation of CsmDeclarationStatement
 */
public final class DeclarationStatementImpl extends StatementBase implements CsmDeclarationStatement {

    private volatile List<CsmDeclaration> declarators;
    private static final List<CsmDeclaration> EMPTY = Collections.<CsmDeclaration>emptyList();
    
    private DeclarationStatementImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file, scope);
    }

    private DeclarationStatementImpl(CsmScope scope, CsmFile file, int start, int end) {
        super(file, start, end, scope);        
    }
    
    public static DeclarationStatementImpl create(AST ast, CsmFile file, CsmScope scope) {
        DeclarationStatementImpl stmt = new DeclarationStatementImpl(ast, file, scope);
        stmt.init(ast);
        return stmt;
    }

    private void init(AST ast) {
        render(ast);
    }
    
    @Override
    public final CsmStatement.Kind getKind() {
        return CsmStatement.Kind.DECLARATION;
    }

    @Override
    public final List<CsmDeclaration> getDeclarators() {
        return Collections.unmodifiableList(declarators);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (declarators != null) {
            Utils.disposeAll(declarators);
        }
    }

    @Override
    public String toString() {
        return "" + getKind() + ' ' + getOffsetString() + '[' + declarators + ']'; // NOI18N
    }

    private synchronized void render(AST ast) {
        if (this.declarators == null) {
            // assign constant to prevent infinite recusion by calling this method in the same thread
            this.declarators = EMPTY;
            DSRenderer renderer = new DSRenderer();
            renderer.render(ast, null, null);
            // assign should be the latest operation
            // prevent publishing list before it is completely constructed
            this.declarators = renderer.declarators;
        }
    }

    private class DSRenderer extends AstRenderer {
        private List<CsmDeclaration> declarators = new ArrayList<>();

        public DSRenderer() {
            super((FileImpl) DeclarationStatementImpl.this.getContainingFile());
        }

        @Override
        protected VariableImpl<?> createVariable(AST offsetAst, AST templateAst, CsmFile file, CsmType type, NameHolder name, boolean _static, boolean _extern, MutableDeclarationsContainer container1, MutableDeclarationsContainer container2, CsmScope scope) {
            VariableImpl<?> var = super.createVariable(offsetAst, templateAst, file, type, name, _static, _extern, container1, container2, getScope());
            declarators.add(var);
            return var;
        }

        @Override
        protected FunctionImpl<?> createFunction(AST ast, CsmFile file, CsmType type, CsmScope scope) {
            FunctionImpl<?> fun = null;
            try {
                fun = FunctionImpl.create(ast, file, null, type, getScope(), !isRenderingLocalContext(),objects);
                declarators.add(fun);
            } catch (AstRendererException ex) {
                DiagnosticExceptoins.register(ex);
            }
            return fun;
        }

        @Override
        protected boolean isRenderingLocalContext() {
            return true;
        }

        @Override
        public void render(AST tree, NamespaceImpl currentNamespace, MutableDeclarationsContainer container) {
            if (tree != null) {
                try {
                    AST token = tree;
                    switch (token.getType()) {
                        case CPPTokenTypes.CSM_FOR_INIT_STATEMENT:
                        case CPPTokenTypes.CSM_DECLARATION_STATEMENT:
                            if (!renderVariable(token, currentNamespace, container, currentNamespace, false)) {
                                render(token.getFirstChild(), currentNamespace, container);
                            }
                            break;
                        case CPPTokenTypes.CSM_FUNCTION_DEFINITION:
                            try {
                                CsmDeclaration fddi;
                                if (APTLanguageSupport.getInstance().isLanguageC(language)) {
                                    fddi = FunctionDDImpl.create(token, getContainingFile(), null, currentNamespace, !isRenderingLocalContext());
                                } else {
                                    fddi = LambdaFunction.create(token, getContainingFile(), null, getScope(), !isRenderingLocalContext());
                                }
                                declarators.add(fddi);
                            } catch (AstRendererException e) {
                                DiagnosticExceptoins.register(e);
                            }
                            break;
                        case CPPTokenTypes.CSM_NAMESPACE_ALIAS:
                            declarators.add(NamespaceAliasImpl.create(token, getContainingFile(), null, !isRenderingLocalContext()));
                            break;
                        case CPPTokenTypes.CSM_USING_DIRECTIVE:
                            declarators.add(UsingDirectiveImpl.create(token, getContainingFile(), !isRenderingLocalContext()));
                            break;
                        case CPPTokenTypes.CSM_USING_DECLARATION:
                            declarators.add(UsingDeclarationImpl.create(token, getContainingFile(), null, !isRenderingLocalContext(), CsmVisibility.PUBLIC));
                            break;

                        case CPPTokenTypes.CSM_CLASS_DECLARATION:
                        case CPPTokenTypes.CSM_TEMPLATE_CLASS_DECLARATION:
                        {
                            try {
                                ClassImpl cls = createClass(token, null, new StmtDeclsContainer());
                                Pair typedefs = renderTypedef(token, cls, currentNamespace);
                                if (!typedefs.getTypesefs().isEmpty()) {
                                    addTypedefs(typedefs.getTypesefs(), currentNamespace, container, cls);
                                    for (CsmTypedef typedef : typedefs.getTypesefs()) {
                                        declarators.add(typedef);
                                        //FIXME: class do not allow register enclosing typedef that does not in repository
                                        //if (cls != null) {
                                        //   cls.addEnclosingTypedef(typedefs[i]);
                                        //}
                                    }
                                }
                                renderVariableInClassifier(token, cls, currentNamespace, container);
                            } catch (AstRendererException e) {
                                DiagnosticExceptoins.register(e);
                            }
                            break;
                        }
                        case CPPTokenTypes.CSM_ENUM_DECLARATION:
                        case CPPTokenTypes.CSM_ENUM_FWD_DECLARATION:
                        {
                            EnumImpl csmEnum = createEnum(token, currentNamespace, container);
                            Pair typedefs = renderTypedef(token, csmEnum, currentNamespace);
                            if (!typedefs.getTypesefs().isEmpty()) {
                                addTypedefs(typedefs.getTypesefs(), currentNamespace, container, csmEnum);
                                for (CsmTypedef typedef : typedefs.getTypesefs()) {
                                    declarators.add(typedef);
                                    //FIXME: class do not allow register enclosing typedef that does not in repository
                                    //if (cls != null) {
                                    //   cls.addEnclosingTypedef(typedefs[i]);
                                    //}
                                }
                            }                            
                            renderVariableInClassifier(token, csmEnum, currentNamespace, container);
                            break;
                        }                
                        case CPPTokenTypes.CSM_TYPE_ALIAS:
                        case CPPTokenTypes.CSM_GENERIC_DECLARATION:
                        {
                            if (renderForwardClassDeclaration(token, currentNamespace, container, (FileImpl) getContainingFile(), isRenderingLocalContext())) {
                                break;
                            }
                            Pair typedefs = renderTypedef(token, (FileImpl) getContainingFile(), fileContent, getScope(), currentNamespace);
                            if (!typedefs.getTypesefs().isEmpty()) {
                                for (CsmTypedef typedef : typedefs.getTypesefs()) {
                                    declarators.add(typedef);
                                }
                            }
                            break;
                        }
                    }
                } catch (AstRendererException ex) {
                    if (!SKIP_AST_RENDERER_EXCEPTIONS) {
                        // In MySQL related tests we see endless "empty function name" exceptions
                        DiagnosticExceptoins.register(ex);
                    }
                }
            }
        }

        @Override
        protected CsmClassForwardDeclaration createForwardClassDeclaration(AST ast, MutableDeclarationsContainer container, FileImpl file, CsmScope scope) {
            ClassForwardDeclarationImpl cfdi = ClassForwardDeclarationImpl.create(ast, file, !isRenderingLocalContext());
            ForwardClass fc = ForwardClass.createIfNeeded(cfdi.getName(), getContainingFile(), ast, cfdi.getStartOffset(), cfdi.getEndOffset(), scope, !isRenderingLocalContext());
            if(fc != null) {
                declarators.add(fc);
            }
            //declarators.add(cfdi);
            //cfdi.init(ast, scope, !isRenderingLocalContext());
            return cfdi;
        }
                
// Never used 
//	/**
//	 * Creates a variable for declaration like int x(y);
//	 * Returns a token that follows this declaration or null
//	 */
//	private AST createVarWithCtor(AST token) {
//	    assert(token.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN || token.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND);
//	    AST typeToken = token;
//	    AST next = token.getNextSibling();
//	    if( next != null && next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
//		TypeImpl type;
//		if( typeToken.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ) {
//		    type = TypeFactory.createBuiltinType(typeToken.getText(), null, 0, typeToken, getContainingFile());
//		}
//		else {
//		    type = TypeFactory.createType(typeToken, getContainingFile(), null, 0);
//		}
//		String name = next.getText();
//		VariableImpl var = new VariableImpl(next, getContainingFile(), type, name, true);
//		// we ignore both currentNamespace and container
//		declarators.add(var);
//		// eat all tokens up to the comma that separates the next decl
//		next = next.getNextSibling();
//		if( next != null && next.getType() == CPPTokenTypes.CSM_PARMLIST ) {
//		    next = next.getNextSibling();
//		}
//		if( next != null && next.getType() == CPPTokenTypes.COMMA ) {
//		    next = next.getNextSibling();
//		}
//		return next;
//	    }
//	    return null;
//	}

        @Override
        protected EnumImpl createEnum(AST token, CsmScope scope, DeclarationsContainer container) {
            EnumImpl impl = super.createEnum(token, scope, container); 
            if (impl != null) {
                declarators.add(impl);
            }
            return impl;
        }
        
        private final class StmtDeclsContainer implements MutableDeclarationsContainer {

            @Override
            public void addDeclaration(CsmOffsetableDeclaration declaration) {
                declarators.add(declaration);
            }

            @Override
            public void removeDeclaration(CsmOffsetableDeclaration declaration) {
                throw new UnsupportedOperationException("Not supported."); // NOI18N
            }

            @Override
            public Collection<CsmOffsetableDeclaration> getDeclarations() {
                throw new UnsupportedOperationException("Not supported."); // NOI18N
            }

            @Override
            public CsmOffsetableDeclaration findExistingDeclaration(int startOffset, int endOffset, CharSequence name) {
                return null;
            }

            @Override
            public CsmOffsetableDeclaration findExistingDeclaration(int startOffset, CharSequence name, CsmDeclaration.Kind kind) {
                return null;
            }
            
        }
    }
    
    
    public static class DeclarationStatementBuilder extends StatementBuilder {

        private final List<SimpleDeclarationBuilder> declarations = new ArrayList<>();
        
        public void addDeclarationBuilder(SimpleDeclarationBuilder decl) {
            declarations.add(decl);
        }

        @Override
        public DeclarationStatementImpl create() {
            DeclarationStatementImpl stmt = new DeclarationStatementImpl(getScope(), getFile(), getStartOffset(), getEndOffset());
            List<CsmDeclaration> decls = new ArrayList<>();
            for (SimpleDeclarationBuilder declBuilder : declarations) {
                declBuilder.setScope(getScope());
                decls.add(declBuilder.create());
            }
            if(decls.isEmpty()) {
                stmt.declarators = Collections.<CsmDeclaration>emptyList();
            } else {
                stmt.declarators = decls;
            }
            return stmt;
        }

    }       
}
