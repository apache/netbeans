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
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import java.util.*;
import org.netbeans.lib.editor.util.CharSequenceUtilities;

import org.netbeans.modules.cnd.antlr.collections.AST;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;
import org.netbeans.modules.cnd.api.model.services.CsmExpressionResolver;
import org.netbeans.modules.cnd.api.model.services.CsmIncludeResolver;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.utils.cache.TextCache;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.csm.*;
import org.netbeans.modules.cnd.modelimpl.csm.AstRendererException;
import org.netbeans.modules.cnd.modelimpl.csm.deep.*;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.FakeAST;
import org.netbeans.modules.cnd.modelimpl.parser.OffsetableAST;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MutableObject;
import org.netbeans.modules.cnd.utils.cache.APTStringManager;
import org.openide.util.CharSequences;
import org.openide.util.Exceptions;

/**
 */
public class AstRenderer {

    private final FileImpl file;
    protected final FileContent fileContent;
    private boolean registeredFakeInclude = false;
    protected final Map<Integer, CsmObject> objects;
    protected final String language;
    protected final String languageFlavor;

    protected static final boolean SKIP_AST_RENDERER_EXCEPTIONS = Boolean.getBoolean("cnd.skip.ast.renderer.exceptions"); //NOI18N

    public AstRenderer(FileImpl fileImpl, FileContent fileContent, String language, String languageFlavor, Map<Integer, CsmObject> objects) {
        if (isBeingParsed(fileImpl)) {
            CndUtils.assertNotNullInConsole(fileContent, "null file content"); // NOI18N
            assert false;
        }
        assert fileContent == null || fileImpl == fileContent.getFile();
        this.file = fileImpl;
        this.fileContent = fileContent;
        this.objects = objects;
        this.language = language;
        this.languageFlavor = languageFlavor;
    }

    public AstRenderer(FileImpl fileImpl) {
        this(fileImpl, null, fileImpl.getFileLanguage(), fileImpl.getFileLanguageFlavor(), null);
    }

    protected CsmFile getContainingFile() {
        return file;
    }

    protected FileContent getFileContent() {
        return fileContent;
    }

    protected boolean hasRegisteredFakeIncludes() {
        return registeredFakeInclude;
    }

    public void render(AST root) {
//        if (file.getAbsolutePath().toString().endsWith("shared.h")) {
//            int i = 10;
//        }
        render(root, (NamespaceImpl) file.getProject().getGlobalNamespace(), fileContent);
    }

    @SuppressWarnings("fallthrough")
    public void render(AST tree, NamespaceImpl currentNamespace, MutableDeclarationsContainer container) {
        if (tree == null) {
            return; // paranoia
        }
        for (AST token = tree.getFirstChild(); token != null; token = token.getNextSibling()) {
            try {
                switch (token.getType()) {
                    case CPPTokenTypes.CSM_LINKAGE_SPECIFICATION:
                        render(token, currentNamespace, container);
                        break;
                    case CPPTokenTypes.CSM_NAMESPACE_DECLARATION:
                        NamespaceDefinitionImpl ns = NamespaceDefinitionImpl.findOrCreateNamespaceDefionition(container, token, currentNamespace, file);
                        render(token, (NamespaceImpl) ns.getNamespace(), ns);
                        checkInnerIncludes(ns, ns.getDeclarations());
                        break;
                    case CPPTokenTypes.CSM_CLASS_DECLARATION:
                    case CPPTokenTypes.CSM_TEMPLATE_CLASS_DECLARATION: {
                        boolean planB = false;
                        if(objects != null) {
                            CsmObject o = objects.get(OffsetableBase.getStartOffset(token));
                            if(o instanceof ClassImpl) {
                                ClassImpl cls = (ClassImpl)o;
                                cls.render(token, getContainingFile(), fileContent, language, languageFlavor, isRenderingLocalContext(), null);
                                //container.addDeclaration(cls);
                                addTypedefs(renderTypedef(token, cls, currentNamespace).typedefs, currentNamespace, container, cls);
                                renderVariableInClassifier(token, cls, currentNamespace, container);
                            } else {
                                planB = true;
                            }
                        } else {
                            planB = true;
                        }
                        if(planB) {
                            ClassImpl cls = createClass(token, currentNamespace, container);
                            addTypedefs(renderTypedef(token, cls, currentNamespace).typedefs, currentNamespace, container, cls);
                            renderVariableInClassifier(token, cls, currentNamespace, container);
                        }
                        break;
                    }
                    case CPPTokenTypes.CSM_ENUM_FWD_DECLARATION:
                    {
                        createForwardEnumDeclaration(token, container, file, currentNamespace);
                        break;
                    }
                    case CPPTokenTypes.CSM_ENUM_DECLARATION:
                    {
                        boolean planB = false;
                        EnumImpl csmEnum = null;
                        if(objects != null) {
                            CsmObject o = objects.get(OffsetableBase.getStartOffset(token));
                            if(o instanceof EnumImpl) {
                                csmEnum = (EnumImpl)o;
                                csmEnum.init(currentNamespace, token, file, !isRenderingLocalContext());
                                container.addDeclaration(csmEnum);
                                addTypedefs(renderTypedef(token, csmEnum, currentNamespace).typedefs, currentNamespace, container, csmEnum);
                                renderVariableInClassifier(token, csmEnum, currentNamespace, container);
                            } else {
                                planB = true;
                            }
                        } else {
                            planB = true;
                        }
                        if(planB) {
                            csmEnum = createEnum(token, currentNamespace, container);
                            addTypedefs(renderTypedef(token, csmEnum, currentNamespace).typedefs, currentNamespace, container, csmEnum);
                            renderVariableInClassifier(token, csmEnum, currentNamespace, container);
                        }
                        if (csmEnum != null) {
                            checkInnerIncludes(csmEnum, Collections.<CsmObject>emptyList());
                        }
                        break;
                    }
                    case CPPTokenTypes.CSM_FUNCTION_LIKE_VARIABLE_TEMPLATE_DECLARATION:
                    case CPPTokenTypes.CSM_FUNCTION_LIKE_VARIABLE_DECLARATION:
                        if (isFuncLikeVariable(token, false)) {
                            if (renderFuncLikeVariable(token, currentNamespace, container, !isFuncLikeVariable(token, true))) {
                                break;
                            }
                        }
                    //nobreak!
                    case CPPTokenTypes.CSM_FUNCTION_DECLARATION:
                    case CPPTokenTypes.CSM_FUNCTION_RET_FUN_DECLARATION:
                    case CPPTokenTypes.CSM_FUNCTION_TEMPLATE_DECLARATION:
                    case CPPTokenTypes.CSM_USER_TYPE_CAST_DECLARATION:
                    case CPPTokenTypes.CSM_USER_TYPE_CAST_TEMPLATE_DECLARATION:
                        FunctionImpl<?> fi = FunctionImpl.create(token, file, fileContent, null, currentNamespace, !isRenderingLocalContext(), objects);
                        container.addDeclaration(fi);
                        if (NamespaceImpl.isNamespaceScope(fi)) {
                            currentNamespace.addDeclaration(fi);
                        }
                        break;
                    case CPPTokenTypes.CSM_TEMPLATE_EXPLICIT_INSTANTIATION:
                        if(isClassExplicitInstantiation(token)) {
                            // TODO
                        } else {
                            CsmFunctionInstantiation cfi = FunctionInstantiationImpl.create(token, file, fileContent, !isRenderingLocalContext());
                            container.addDeclaration(cfi);
                        }
                        break;
                    case CPPTokenTypes.CSM_CTOR_DEFINITION:
                    case CPPTokenTypes.CSM_CTOR_TEMPLATE_DEFINITION:
                        container.addDeclaration(ConstructorDefinitionImpl.create(token, file, fileContent, !isRenderingLocalContext()));
                        break;
                    case CPPTokenTypes.CSM_DTOR_DEFINITION:
                    case CPPTokenTypes.CSM_DTOR_TEMPLATE_DEFINITION:
                        container.addDeclaration(DestructorDefinitionImpl.create(token, file, fileContent, !isRenderingLocalContext()));
                        break;
                    case CPPTokenTypes.CSM_FUNCTION_RET_FUN_DEFINITION:
                    case CPPTokenTypes.CSM_FUNCTION_DEFINITION:
                    case CPPTokenTypes.CSM_FUNCTION_TEMPLATE_DEFINITION:
                    case CPPTokenTypes.CSM_USER_TYPE_CAST_DEFINITION:
                    case CPPTokenTypes.CSM_USER_TYPE_CAST_TEMPLATE_DEFINITION:
                        if (isMemberDefinition(token)) {
                            container.addDeclaration(FunctionDefinitionImpl.create(token, file, fileContent, null, !isRenderingLocalContext(), objects));
                        } else {
                            FunctionDDImpl<?> fddi = FunctionDDImpl.create(token, file, fileContent, currentNamespace, !isRenderingLocalContext());
                            //fddi.setScope(currentNamespace);
                            container.addDeclaration(fddi);
                            if (NamespaceImpl.isNamespaceScope(fddi)) {
                                currentNamespace.addDeclaration(fddi);
                            }
                        }
                        break;
                    case CPPTokenTypes.CSM_FWD_TEMPLATE_EXPLICIT_SPECIALIZATION:
                        if (renderForwardClassDeclaration(token, currentNamespace, container, file, isRenderingLocalContext())) {
                            break;
                        } else {
                            renderForwardMemberDeclaration(token, currentNamespace, container, file);
                        }
                        break;
                    case CPPTokenTypes.CSM_TEMPLATE_EXPLICIT_SPECIALIZATION:
                        if (isClassSpecialization(token)) {
                            ClassImpl spec = ClassImplSpecialization.create(token, currentNamespace, file, language, languageFlavor, fileContent, !isRenderingLocalContext(), container);
                            container.addDeclaration(spec);
                            addTypedefs(renderTypedef(token, spec, currentNamespace).typedefs, currentNamespace, container, spec);
                        } else {
                            if (isMemberDefinition(token)) {
                                if(!isFunctionOnlySpecialization(token)) {
                                    // this is a template method specialization declaration (without a definition)
                                    ClassImplFunctionSpecialization spec = ClassImplFunctionSpecialization.create(token, currentNamespace, file, language, languageFlavor, fileContent, !isRenderingLocalContext(), container);
                                    MethodImplSpecialization explicitSpecializationDeclaration = MethodImplSpecialization.create(token, file, fileContent, spec, CsmVisibility.PUBLIC, !isRenderingLocalContext());
                                    spec.addMember(explicitSpecializationDeclaration, !isRenderingLocalContext());
                                    if (currentNamespace != null && NamespaceImpl.isNamespaceScope(explicitSpecializationDeclaration)) {
                                        currentNamespace.addDeclaration(explicitSpecializationDeclaration);
                                    }
                                    container.addDeclaration(explicitSpecializationDeclaration);
                                } else {
                                    FunctionImplEx<Object> explicitSpecializationDeclaration = FunctionImplEx.create(token, file, fileContent, currentNamespace, !isRenderingLocalContext(), !isRenderingLocalContext(), objects);
                                    if (currentNamespace != null && NamespaceImpl.isNamespaceScope(explicitSpecializationDeclaration)) {
                                        currentNamespace.addDeclaration(explicitSpecializationDeclaration);
                                    }
                                    container.addDeclaration(explicitSpecializationDeclaration);
                                }
                            } else {
                                if (renderForwardMemberDeclaration(token, currentNamespace, container, file)) {
                                    break;
                                }
                                FunctionImpl<?> funct = FunctionImpl.create(token, file, fileContent, null, currentNamespace, !isRenderingLocalContext(),objects);
                                container.addDeclaration(funct);
                                if (NamespaceImpl.isNamespaceScope(funct)) {
                                    currentNamespace.addDeclaration(funct);
                                }
                            }
                        }
                        break;
                    case CPPTokenTypes.CSM_TEMPLATE_CTOR_DEFINITION_EXPLICIT_SPECIALIZATION:
                        container.addDeclaration(ConstructorDefinitionImpl.create(token, file, fileContent, !isRenderingLocalContext()));
                        break;
                    case CPPTokenTypes.CSM_TEMPLATE_DTOR_DEFINITION_EXPLICIT_SPECIALIZATION:
                        container.addDeclaration(DestructorDefinitionImpl.create(token, file, fileContent, !isRenderingLocalContext()));
                        break;
                    case CPPTokenTypes.CSM_USER_TYPE_CAST_DEFINITION_EXPLICIT_SPECIALIZATION:
                    case CPPTokenTypes.CSM_TEMPLATE_FUNCTION_DEFINITION_EXPLICIT_SPECIALIZATION:
                        if (isMemberDefinition(token)) {
                            if(!isFunctionOnlySpecialization(token)) {
                                ClassImpl spec = ClassImplFunctionSpecialization.create(token, currentNamespace, file, language, languageFlavor, fileContent, !isRenderingLocalContext(), container);
                            }
                            FunctionDefinitionImpl<Object> funcDef = FunctionDefinitionImpl.create(token, file, fileContent, currentNamespace, !isRenderingLocalContext(), objects);
                            container.addDeclaration(funcDef);
                            if (currentNamespace != null && NamespaceImpl.isNamespaceScope(funcDef)) {
                                currentNamespace.addDeclaration(funcDef);
                            }
                        } else {
                            FunctionDDImpl<?> fddit = FunctionDDImpl.create(token, file, fileContent, currentNamespace, !isRenderingLocalContext());
                            container.addDeclaration(fddit);
                            if (NamespaceImpl.isNamespaceScope(fddit)) {
                                currentNamespace.addDeclaration(fddit);
                            }
                        }
                        break;
                    case CPPTokenTypes.CSM_NAMESPACE_ALIAS:
                        if(!TraceFlags.CPP_PARSER_ACTION || isRenderingLocalContext()) {
                            NamespaceAliasImpl alias = NamespaceAliasImpl.create(token, file, currentNamespace, !isRenderingLocalContext());
                            container.addDeclaration(alias);
                            currentNamespace.addDeclaration(alias);
                        }
                        break;
                    case CPPTokenTypes.CSM_USING_DIRECTIVE: {
                        if(!TraceFlags.CPP_PARSER_ACTION || isRenderingLocalContext()) {
                            UsingDirectiveImpl using = UsingDirectiveImpl.create(token, file, !isRenderingLocalContext());
                            container.addDeclaration(using);
                            currentNamespace.addDeclaration(using);
                        }
                        break;
                    }
                    case CPPTokenTypes.CSM_USING_DECLARATION: {
                        if(!TraceFlags.CPP_PARSER_ACTION || isRenderingLocalContext()) {
                            UsingDeclarationImpl using = UsingDeclarationImpl.create(token, file, currentNamespace, !isRenderingLocalContext(), CsmVisibility.PUBLIC);
                            container.addDeclaration(using);
                            currentNamespace.addDeclaration(using);
                        }
                        break;
                    }
                    case CPPTokenTypes.CSM_TEMPL_FWD_CL_OR_STAT_MEM:
                        if (renderForwardClassDeclaration(token, currentNamespace, container, file, isRenderingLocalContext())) {
                            break;
                        } else {
                            renderForwardMemberDeclaration(token, currentNamespace, container, file);
                        }
                        break;
                    case CPPTokenTypes.CSM_GENERIC_DECLARATION:
                        if (renderNSP(token, currentNamespace, container, file)) {
                            break;
                        }
                        if (renderVariable(token, currentNamespace, container, currentNamespace, false)) {
                            break;
                        }
                        if (renderForwardClassDeclaration(token, currentNamespace, container, file, isRenderingLocalContext())) {
                            break;
                        }
                        if (renderLinkageSpec(token, file, currentNamespace, container)) {
                            break;
                        }
                        addTypedefs(renderTypedef(token, file, fileContent, currentNamespace, container).typedefs, currentNamespace, container,null);
                        break;
                    default:
                        renderNSP(token, currentNamespace, container, file);
                }
            } catch (AstRendererException e) {
                if (!SKIP_AST_RENDERER_EXCEPTIONS) {
                    // In MySQL related tests we see endless "empty function name" exceptions
                    if (CndUtils.isUnitTestMode() && e.getMessage().contains("Empty function name")) { // NOI18N
                        try {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            PrintStream ps = new PrintStream(baos, true, "UTF8"); // NOI18N
                            ps.println("AST:"); // NOI18N
                            AstUtil.toStream(tree, ps);
                            ps.println();
                            String content = baos.toString("UTF8"); // NOI18N
                            DiagnosticExceptoins.register(new AssertionError(e.getMessage() + " # " + content, e)); // NOI18N
                        } catch (UnsupportedEncodingException inner) {
                            DiagnosticExceptoins.register(new AssertionError(e.getMessage() + " # Failed to dump AST!", e)); // NOI18N
                        }
                    } else {
                        DiagnosticExceptoins.register(e);
                    }
                }
            } catch (Throwable thr) {
              CharSequence fileName = (file != null) ? file.getAbsolutePath() : "<unknown>"; // NOI18N
              throw new RuntimeException("Exception in file " + fileName + ": " + thr.getMessage(), thr); // NOI18N
            }
        }
    }

    protected void addTypedefs(Collection<CsmTypedef> typedefs, NamespaceImpl currentNamespace, MutableDeclarationsContainer container, ClassEnumBase enclosingClassifier) {
        if (typedefs != null) {
            for (CsmTypedef typedef : typedefs) {
                // It could be important to register in project before add as member...
                if (!isRenderingLocalContext()) {
                    file.getProjectImpl(true).registerDeclaration(typedef);
                }
                if (container != null) {
                    container.addDeclaration(typedef);
                }
                if (currentNamespace != null) {
                    // Note: DeclarationStatementImpl.DSRenderer can call with null namespace
                    currentNamespace.addDeclaration(typedef);
                }
                if (enclosingClassifier != null) {
                   enclosingClassifier.addEnclosingTypedef(typedef);
                }
            }
        }
    }

    public boolean isFuncLikeVariable(AST ast, boolean findRefsForParams) {
        return isFuncLikeVariable(ast, findRefsForParams, false);
    }

    /**
     * Parser don't use a symbol table, so constructs like
     * int a(b)
     * are parsed as if they were functions.
     * At the moment of rendering, we check whether this is a variable of a function
     * @return true if it's a variable, otherwise false (it's a function)
     */
    public boolean isFuncLikeVariable(AST ast, boolean findRefsForParams, boolean findWithFullResolve) {
        AST astParmList = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_PARMLIST);
        AST qualId = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID, astParmList);
        if (qualId != null && AstUtil.findChildOfType(qualId, CPPTokenTypes.LITERAL_OPERATOR) != null) {
            return false; // operator cannot be a variable.
        }
        if (astParmList != null) {
            for (AST node = astParmList.getFirstChild(); node != null; node = node.getNextSibling()) {
                if (!isRefToVariableOrFunction(node, findRefsForParams, findWithFullResolve)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Parser don't use a symbol table, so local constructs like
     * int a(b)
     * are parsed as if they were variables.
     * At the moment of rendering, we check whether this is a variable of a function
     * @return true if it's a function, otherwise false (it's a variable)
     */
    protected boolean isVariableLikeFunc(AST ast) {
        AST astParmList = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_PARMLIST);
        if (astParmList != null) {
            for (AST node = astParmList.getFirstChild(); node != null; node = node.getNextSibling()) {
                if (node.getType() != CPPTokenTypes.CSM_PARAMETER_DECLARATION) {
                    return false;
                }
                AST child = node.getFirstChild();
                if (child != null) {
                    if (child.getType() == CPPTokenTypes.LITERAL_const) {
                        return true;
                    } else if (child.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN) {
                        return true;
                    } else if (child.getType() == CPPTokenTypes.CSM_TYPE_ATOMIC) {
                        return true;
                    } else if (child.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND) {
                        if (!isAbstractDeclarator(child.getNextSibling())) {
                            CsmType type = TypeFactory.createType(child, file, null, 0);
                            if (type != null) {
                                CsmClassifier cls = type.getClassifier();
                                if (CsmBaseUtilities.isValid(cls)) {
                                    return true;
                                }
                            }
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Determines whether the given parameter can actually be a reference to a variable,
     * not a parameter
     * @param node an AST node that corresponds to parameter
     * @param findVariable indicates that we should find param variable or just check that it looks like id
     * @return true if might be just a reference to a variable, otherwise false
     */
    private boolean isRefToVariableOrFunction(AST node, boolean findVariableOrFunction, boolean findWithFullResolve) {

        if (node.getType() != CPPTokenTypes.CSM_PARAMETER_DECLARATION) { // paranoja
            return false;
        }

        AST child = node.getFirstChild();

        AST name = null;

        // AST structure is different for int f1(A) and int f2(*A)
        if (child != null && child.getType() == CPPTokenTypes.CSM_PTR_OPERATOR) {
            // we know it's variable initialization => no need to look for variable
            // TODO: why we need to go deeper after * or & ? I'd prefer to return 'true'
            if (true) {
                return true;
            }
            while (child != null && child.getType() == CPPTokenTypes.CSM_PTR_OPERATOR) {
                child = child.getNextSibling();
            }
            // now it's CSM_VARIABLE_DECLARATION
            if (child != null) {
                name = child.getFirstChild();
            }
        } else if (child != null && child.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND) {
            if (!isAbstractDeclarator(child.getNextSibling())) {
                return false;
            }
            name = child.getFirstChild();
        } else if(child != null && child.getType() == CPPTokenTypes.CSM_VARIABLE_DECLARATION) {
            return true;
        }

        if (name == null) {
            return false;
        }
        if (name.getType() != CPPTokenTypes.CSM_QUALIFIED_ID &&
                name.getType() != CPPTokenTypes.IDENT) {
            return false;
        }

        return isVariableOrFunctionName(name, findVariableOrFunction, findWithFullResolve);
    }

    private boolean isVariableOrFunctionName(AST name, boolean findVariableOrFunction, boolean findWithFullResolve) {
        OffsetableAST csmAST = AstUtil.getFirstOffsetableAST(name);

        StringBuilder varName = new StringBuilder(AstUtil.getText(name));
        AST next = name.getNextSibling();
        while (next != null) {
            next = skipTemplateParameters(next);
            if(next == null) {
                break;
            }
            name = next;
            varName.append(AstUtil.getText(name));
            next = next.getNextSibling();
        }

        if (findVariableOrFunction) {
            if (findVariable(varName, csmAST.getOffset())) {
                return true;
            }
            if (findFunction(varName, csmAST.getOffset())) {
                return true;
            }
            if (findWithFullResolve) {
                return findGlobalHard(
                    varName,
                    csmAST.getOffset(),
                    CsmDeclaration.Kind.FUNCTION,
                    CsmDeclaration.Kind.FUNCTION_FRIEND,
                    CsmDeclaration.Kind.VARIABLE,
                    CsmDeclaration.Kind.VARIABLE_DEFINITION,
                    CsmDeclaration.Kind.ENUMERATOR
                );
            }
            return false;
        } else {
            next = name.getNextSibling();
            next = skipTemplateParameters(next);
            if (next != null) {
                return isScopedId(name);
            }
            return true;
        }
    }

    public static AST skipTemplateParameters(AST node) {
        int depth = 0;
        while (node != null) {
            switch (node.getType()) {
                case CPPTokenTypes.LESSTHAN:
                    depth++;
                    break;
                case CPPTokenTypes.GREATERTHAN:
                    depth--;
                    if (depth == 0) {
                        return node.getNextSibling();
                    }
                    break;
                default:
                    if(depth == 0) {
                        return node;
                    }
            }
            node = node.getNextSibling();
        }
        return null;
    }

    private boolean isAbstractDeclarator(AST node) {
        if(node == null) {
            return true;
        }
        if(node.getType() != CPPTokenTypes.LPAREN) {
            return false;
        }
        node = node.getNextSibling();
        if(node != null && node.getType() == CPPTokenTypes.CSM_PARMLIST) {
            node = node.getNextSibling();
        }
        if(node == null || node.getType() != CPPTokenTypes.RPAREN) {
            return false;
        }
        if(node.getNextSibling() !=  null) {
            return false;
        }
        return true;
    }


    /**
     * Finds variable in globals and in the current file
     */
    private boolean findVariable(CharSequence name, int offset) {
        String uname = ""+Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.VARIABLE) +//NOI18N
                OffsetableDeclarationBase.UNIQUE_NAME_SEPARATOR + /*"::" +*/ name; // NOI18N
        if (findGlobal(file.getProject(), uname, name, false, new ArrayList<CsmProject>())) {
            return true;
        }
        CsmFilter filter = CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.NAMESPACE_DEFINITION, CsmDeclaration.Kind.VARIABLE, CsmDeclaration.Kind.VARIABLE_DEFINITION);
        Iterator<CsmOffsetableDeclaration> declarations = CsmSelect.getDeclarations(file, filter);
        return findVariable(name, declarations, offset, filter);
    }

    /**
     * Finds function in globals and in the current file
     */
    private boolean findFunction(CharSequence name, int offset) {
        String uname = ""+Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION) +//NOI18N
                OffsetableDeclarationBase.UNIQUE_NAME_SEPARATOR + /*"::" +*/ name; // NOI18N
        if (findGlobal(file.getProject(), uname, name, true, new ArrayList<CsmProject>())) {
            return true;
        }
        uname = ""+Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_FRIEND) +//NOI18N
                OffsetableDeclarationBase.UNIQUE_NAME_SEPARATOR + /*"::" +*/ name; // NOI18N
        if (findGlobal(file.getProject(), uname, name, true, new ArrayList<CsmProject>())) {
            return true;
        }
        CsmFilter filter = CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.NAMESPACE_DEFINITION, CsmDeclaration.Kind.FUNCTION, CsmDeclaration.Kind.FUNCTION_DEFINITION,
                CsmDeclaration.Kind.FUNCTION_FRIEND, CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION);
        Iterator<CsmOffsetableDeclaration> declarations = CsmSelect.getDeclarations(file, filter);
        return findFunction(name, declarations, offset, filter);
    }

    private boolean findGlobal(CsmProject project, String uname, CharSequence qualName, boolean findByPrefix, Collection<CsmProject> processedProjects) {
        if (processedProjects.contains(project)) {
            return false;
        }
        processedProjects.add(project);
        CsmDeclaration decl = null;
        if (findByPrefix && project instanceof ProjectBase) {
            ProjectBase projectBase = (ProjectBase) project;
            Collection<CsmOffsetableDeclaration> candidates = projectBase.findDeclarationsByPrefix(uname);
            for (CsmOffsetableDeclaration candidate : candidates) {
                if (CharSequenceUtilities.equals(qualName, candidate.getQualifiedName())) {
                    decl = candidate;
                    break;
                }
            }
        } else {
            decl = project.findDeclaration(uname);
        }
        if (decl != null && CsmIncludeResolver.getDefault().isObjectVisible(file, decl)) {
            return true;
        }
        for (CsmProject lib : project.getLibraries()) {
            if (findGlobal(lib, uname, qualName, findByPrefix, processedProjects)) {
                return true;
            }
        }
        return false;
    }

    private boolean findGlobalHard(CharSequence name, int offset, CsmDeclaration.Kind ... kinds) {
        Collection<CsmObject> resolvedObjects = CsmExpressionResolver.resolveObjects(name, file, offset, null);
        if (resolvedObjects != null) {
            for (CsmObject obj : resolvedObjects) {
                if (CsmKindUtilities.isDeclaration(obj)) {
                    CsmDeclaration decl = (CsmDeclaration) obj;
                    for (CsmDeclaration.Kind kind : kinds) {
                        if (kind.equals(decl.getKind())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean findVariable(CharSequence name, Iterator<CsmOffsetableDeclaration> it, int offset, CsmFilter filter) {
        while(it.hasNext()) {
            CsmOffsetableDeclaration decl = it.next();
            if (decl.getStartOffset() >= offset) {
                break;
            }
            switch (decl.getKind()) {
                case VARIABLE:
                    if (CharSequences.comparator().compare(name, ((CsmVariable) decl).getName()) == 0) {
                        return true;
                    }
                    break;
                case VARIABLE_DEFINITION:
                    if (CharSequences.comparator().compare(name, ((CsmVariable) decl).getQualifiedName()) == 0) {
                        return true;
                    }
                    break;
                case NAMESPACE_DEFINITION:
                    CsmNamespaceDefinition nd = (CsmNamespaceDefinition) decl;
                    if (nd.getStartOffset() <= offset && nd.getEndOffset() >= offset) {
                        if (findVariable(name, CsmSelect.getDeclarations(nd, filter), offset, filter)) {
                            return true;
                        }
                    }
                    break;
            }
        }
        return false;
    }

    private boolean findFunction(CharSequence name, Iterator<CsmOffsetableDeclaration> it, int offset, CsmFilter filter) {
        while(it.hasNext()) {
            CsmOffsetableDeclaration decl = it.next();
            if (decl.getStartOffset() >= offset) {
                break;
            }
            switch (decl.getKind()) {
                case FUNCTION:
                case FUNCTION_FRIEND:
                    if (CharSequences.comparator().compare(name, ((CsmFunction) decl).getName()) == 0) {
                        return true;
                    }
                    break;
                case FUNCTION_DEFINITION:
                case FUNCTION_FRIEND_DEFINITION:
                    if (CharSequences.comparator().compare(name, ((CsmFunctionDefinition) decl).getQualifiedName()) == 0) {
                        return true;
                    }
                    break;
                case NAMESPACE_DEFINITION:
                    CsmNamespaceDefinition nd = (CsmNamespaceDefinition) decl;
                    if (nd.getStartOffset() <= offset && nd.getEndOffset() >= offset) {
                        if (findFunction(name, CsmSelect.getDeclarations(nd, filter), offset, filter)) {
                            return true;
                        }
                    }
                    break;
            }
        }
        return false;
    }

    protected boolean isRenderingLocalContext() {
        return false;
    }

    protected void checkInnerIncludes(CsmOffsetableDeclaration inclContainer, Collection<? extends CsmObject> containerInnerObjects) {
        // Check for include directives in class
        if (fileContent != null && !isRenderingLocalContext()) {
            CsmFile curFile = getContainingFile();
            boolean alreadyInInclude = !curFile.equals(inclContainer.getContainingFile());
            if (alreadyInInclude) {
                // we already in phase of fixing fake includes
                // TODO: we somehow should check includes in curFile which are valid in context of parsing from inclContainer's file
                return;
            }
            Outer:
            for (CsmInclude include : fileContent.getIncludes()) {
                if (include instanceof IncludeImpl) {
                    if (include.getStartOffset() > inclContainer.getStartOffset() && include.getEndOffset() < inclContainer.getEndOffset()) {
                        // check that not inside body of container's elemens
                        for (CsmObject inner : containerInnerObjects) {
                            if (CsmKindUtilities.isOffsetable(inner)) {
                                CsmOffsetable offs = (CsmOffsetable) inner;
                                if (curFile.equals(offs.getContainingFile())) {
                                    // inner is in the same file as #include directive
                                    if (include.getStartOffset() > offs.getStartOffset()
                                            && include.getEndOffset() < offs.getEndOffset()) {
                                        // #include inside one of inner object's body => not fake for inclContainer
                                        continue Outer;
                                    }
                                }
                            }
                        }
                        registeredFakeInclude |= fileContent.onFakeIncludeRegistration((IncludeImpl) include, inclContainer);
                    }
                }
            }
        }
    }

    /**
     * In the case of the "function-like variable" - construct like
     * int a(b)
     * renders the AST to create the variable
     */
    private boolean renderFuncLikeVariable(AST token, NamespaceImpl currentNamespace, MutableDeclarationsContainer container, boolean fakeRegistration) {
        if (token != null) {
            AST ast = token;
            token = token.getFirstChild();
            if (token != null) {
                boolean _static = false;
                boolean _extern = false;
                AST templateAst = null;
                if (token.getType() == CPPTokenTypes.LITERAL_template) {
                    templateAst = token;
                    token = token.getNextSibling();
                }
                if (token != null && isQualifier(token.getType())) {
                    _static = AstUtil.hasChildOfType(token, CPPTokenTypes.LITERAL_static);
                    _extern = AstUtil.hasChildOfType(token, CPPTokenTypes.LITERAL_extern);
                    token = getFirstSiblingSkipQualifiers(token);
                }
                if (token != null && (token.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ||
                        token.getType() == CPPTokenTypes.CSM_TYPE_ATOMIC ||
                        token.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND ||
                        token.getType() == CPPTokenTypes.LITERAL_struct ||
                        token.getType() == CPPTokenTypes.LITERAL_class ||
                        token.getType() == CPPTokenTypes.LITERAL_union)) {
                    AST typeToken = token;
                    AST next = token.getNextSibling();
                    if (token.getType() == CPPTokenTypes.LITERAL_struct ||
                            token.getType() == CPPTokenTypes.LITERAL_class ||
                            token.getType() == CPPTokenTypes.LITERAL_union) {
                        if (next != null && next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
                            typeToken = next;
                            next = next.getNextSibling();
                        }
                    }
                    while (next != null && next.getType() == CPPTokenTypes.CSM_PTR_OPERATOR) {
                        next = next.getNextSibling();
                    }
                    if (next != null && next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
                        TypeImpl type;
                        if (typeToken.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN) {
                            type = TypeFactory.createBuiltinType(AstUtil.getText(typeToken), null, 0, typeToken, file);
                        } else {
                            type = TypeFactory.createType(typeToken, file, null, 0);
                        }

                        if (!fakeRegistration) {
                            // Ensure that offset node contains template node if it present (in case of template node
                            // there could be only one declaration in the current statement, as per 
                            // [C++14 standard, 14.3]:
                            // In a template-declaration, explicit specialization, or explicit instantiation 
                            // the init-declarator-list in the declaration shall contain at most one declarator
                            AST varAstNode = (templateAst != null) ? ast : next;
                            NameHolder nameHolder = NameHolder.createSimpleName(next);
                            VariableImpl<?> var = createVariable(
                                    varAstNode,
                                    templateAst, 
                                    file, 
                                    type, 
                                    nameHolder, 
                                    _static, 
                                    _extern, 
                                    currentNamespace, 
                                    container, 
                                    null
                            );
                            if (currentNamespace != null) {
                                currentNamespace.addDeclaration(var);
                            }
                            if (container != null) {
                                container.addDeclaration(var);
                            }
                            return true;
                        } else {
                            try {
                                FunctionImplEx<?> fi = FunctionImplEx.create(ast, file, fileContent, currentNamespace, false, !isRenderingLocalContext(), objects);
                                fileContent.onFakeRegisration(fi, org.openide.util.Pair.of(ast, container));
                            } catch (AstRendererException e) {
                                DiagnosticExceptoins.register(e);
                            }
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean renderLinkageSpec(AST ast, FileImpl file, NamespaceImpl currentNamespace, MutableDeclarationsContainer container) {
        if (ast != null) {
            AST token = ast.getFirstChild();
            if (token != null) {
                if (token.getType() == CPPTokenTypes.CSM_LINKAGE_SPECIFICATION) {
                    render(token, currentNamespace, container);
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("fallthrough")
    protected void renderVariableInClassifier(AST ast, ClassEnumBase<?> classifier,
            MutableDeclarationsContainer container1, MutableDeclarationsContainer container2) {
        if(AstUtil.hasChildOfType(ast, CPPTokenTypes.LITERAL_typedef)) {
            return;
        }
        AST token = ast.getFirstChild();
        boolean unnamedStaticUnion = false;
        boolean _static = AstUtil.hasChildOfType(ast, CPPTokenTypes.LITERAL_static);
        boolean _extern = AstUtil.hasChildOfType(ast, CPPTokenTypes.LITERAL_extern);
        boolean typedef = false;
        int typeStartOffset = 0;
        if (token != null) {
            typedef = token.getType() == CPPTokenTypes.LITERAL_typedef;
            typeStartOffset = AstUtil.getFirstOffsetableAST(token).getOffset();
            if (token.getType() == CPPTokenTypes.LITERAL_static) {
                token = token.getNextSibling();
                if (token != null) {
                    if (token.getType() == CPPTokenTypes.LITERAL_union) {
                        token = token.getNextSibling();
                        if (token != null) {
                            if (token.getType() == CPPTokenTypes.LCURLY) {
                                unnamedStaticUnion = true;
                            }
                        }
                    }
                }
            }
        }
        for (; token != null; token = token.getNextSibling()) {
            if (token.getType() == CPPTokenTypes.RCURLY) {
                break;
            }
        }
        if (token != null) {
            int rcurlyOffset = AstUtil.getFirstOffsetableAST(token).getEndOffset();
            int startOffset = typeStartOffset;
            int endOffset = rcurlyOffset;
            token = token.getNextSibling();
            boolean nothingBeforSemicolon = true;
            AST ptrOperator = null;
            Outer:
            for (; token != null; token = token.getNextSibling()) {
                switch (token.getType()) {
                    case CPPTokenTypes.CSM_PTR_OPERATOR:
                        nothingBeforSemicolon = false;
                        if (ptrOperator == null) {
                            ptrOperator = token;
                        }
                        break;
                    case CPPTokenTypes.CSM_QUALIFIED_ID:
                        if(typedef) {
                            break;
                        }
                    case CPPTokenTypes.CSM_VARIABLE_DECLARATION:
                    case CPPTokenTypes.CSM_ARRAY_DECLARATION: {
                        nothingBeforSemicolon = false;
                        int arrayDepth = 0;
                        NameHolder nameHolder = null;
                        for (AST varNode = token.getFirstChild(); varNode != null; varNode = varNode.getNextSibling()) {
                            switch (varNode.getType()) {
                                case CPPTokenTypes.LSQUARE:
                                    arrayDepth++;
                                    break;
                                case CPPTokenTypes.CSM_QUALIFIED_ID:
                                    nameHolder = NameHolder.createSimpleName(AstUtil.getLastChild(varNode));
                                    break;
                                case CPPTokenTypes.IDENT:
                                    nameHolder = NameHolder.createSimpleName(varNode);
                                    break;
                            }
                        }
                        if (nameHolder != null) {
                            CsmType type = TypeFactory.createType(classifier, ptrOperator, arrayDepth, token, file, startOffset, endOffset);
                            // TODO: null for templateAst?
                            VariableImpl<?> var = createVariable(token, null, file, type, nameHolder, _static, _extern, container1, container2, null);
                            if (container2 != null) {
                                container2.addDeclaration(var);
                            }
                            // TODO! don't add to namespace if....
                            if (container1 != null) {
                                container1.addDeclaration(var);
                            }
                            classifier.addEnclosingVariable(var);
                            ptrOperator = null;
                        }
                        break;
                    }
                    case CPPTokenTypes.IDENT:
                        {
                            nothingBeforSemicolon = false;
                            if (token.getNextSibling() != null && token.getNextSibling().getType() == CPPTokenTypes.COLON) {
                                // it could be bit field
                                // common type for all bit fields
                                CsmType type = TypeFactory.createType(classifier, null, 0, null, file, startOffset, endOffset);
                                if (renderBitFieldImpl(token, token, type, classifier)) {
                                    break Outer;
                                }
                            }
                            break;
                        }
                    case CPPTokenTypes.SEMICOLON:
                    {
                        if (unnamedStaticUnion && nothingBeforSemicolon) {
                            nothingBeforSemicolon = false;
                            CsmType type = TypeFactory.createType(classifier, null, 0, null, file, startOffset, endOffset);
                            VariableImpl<?> var = VariableImpl.create(file, rcurlyOffset, rcurlyOffset,
                                    type, null, "", null, true, false, !isRenderingLocalContext()); // NOI18N
                            if (container2 != null) {
                                container2.addDeclaration(var);
                            }
                            // TODO! don't add to namespace if....
                            if (container1 != null) {
                                container1.addDeclaration(var);
                            }
                            classifier.addEnclosingVariable(var);
                        }
                        break;
                    }
                    default:
                        nothingBeforSemicolon = false;
                }
            }
        }
    }

    protected boolean renderBitFieldImpl(AST startOffsetAST, AST afterTypeAST, CsmType type, ClassEnumBase<?> classifier) {
        return false;
    }

    @SuppressWarnings("fallthrough")
    protected Pair renderTypedef(AST ast, ClassEnumBase cls, CsmObject container) {

        Pair results = new Pair();

        AST node = ast.getFirstChild();
        if (node != null && AstUtil.hasChildOfType(ast, CPPTokenTypes.LITERAL_typedef)) {
            if(node.getType() == CPPTokenTypes.LITERAL_typedef) {
                node = node.getNextSibling();
            }
            AST classNode = node;
            while (classNode != null && (isVolatileQualifier(classNode.getType()) 
                    || isConstQualifier(classNode.getType()) || isAtomicQualifier(classNode.getType()))) {
                classNode = classNode.getNextSibling();
            }
            if (classNode == null) {
                return results;
            }
            switch (classNode.getType()) {

                case CPPTokenTypes.LITERAL_class:
                case CPPTokenTypes.LITERAL_union:
                case CPPTokenTypes.LITERAL_struct:
                case CPPTokenTypes.LITERAL_enum:

                    AST curr = AstUtil.findSiblingOfType(classNode, CPPTokenTypes.RCURLY);
                    if (curr == null) {
                        return results;
                    }

                    int arrayDepth = 0;
                    AST nameToken = null;
                    AST ptrOperator = null;
                    CharSequence name = "";
                    for (curr = curr.getNextSibling(); curr != null; curr = curr.getNextSibling()) {
                        switch (curr.getType()) {
                            case CPPTokenTypes.CSM_PTR_OPERATOR:
                                // store only 1-st one - the others (if any) follows,
                                // so it's TypeImpl.createType() responsibility to process them all
                                if (ptrOperator == null) {
                                    ptrOperator = ast;
                                }
                                break;
                            case CPPTokenTypes.CSM_QUALIFIED_ID:
                                nameToken = curr;
                                //token t = nameToken.
                                name = AstUtil.findId(nameToken);
                                //name = token.getText();
                                break;
                            case CPPTokenTypes.LSQUARE:
                                arrayDepth++;
                                break;
                            case CPPTokenTypes.COMMA:
                            case CPPTokenTypes.SEMICOLON:
                                TypeImpl typeImpl = TypeFactory.createType(cls, ptrOperator, arrayDepth, ast, file);
                                typeImpl.setTypeOfTypedef();
                                CsmTypedef typedef = createTypedef((nameToken == null) ? ast : nameToken, file, container, typeImpl, name);
                                if (cls != null && cls.getName().length() == 0) {
                                    ((TypedefImpl) typedef).setTypeUnnamed();
                                }
                                if (typedef != null) {
                                    if (cls != null) {
                                        results.enclosing = cls;
                                    }
                                    results.typedefs.add(typedef);
                                }
                                ptrOperator = null;
                                name = "";
                                nameToken = null;
                                arrayDepth = 0;
                                break;
                        }

                    }
                    break;
                default:
                // error message??
            }
        }
        return results;
    }

    protected static class Pair {
        protected List<CsmTypedef> typedefs = new ArrayList<>();
        protected ClassEnumBase<?> enclosing;
        private Pair(){
        }
        public List<CsmTypedef> getTypesefs(){
            return typedefs;
        }
        public ClassEnumBase<?> getEnclosingClassifier(){
            return enclosing;
        }
    }

    @SuppressWarnings("fallthrough")
    protected Pair renderTypedef(AST ast, FileImpl file, FileContent fileContent, CsmScope scope, MutableDeclarationsContainer container) throws AstRendererException {
        Pair results = new Pair();
        if (ast != null) {
            AST firstChild = ast.getFirstChild();
            if (firstChild != null) {
                if (firstChild.getType() == CPPTokenTypes.LITERAL_typedef) {
                    AST typeBeginning = null;
                    AST classifier = null;
                    int arrayDepth = 0;
                    AST nameToken = null;
                    AST ptrOperator = null;
                    AST typeQuals = null;
                    CharSequence name = "";
                    boolean cpp11StyleFunction = false;

                    CsmClassForwardDeclaration cfdi = null;

                    boolean typeof = false;
                    for (AST curr = firstChild; curr != null; curr = curr.getNextSibling()) {
                        switch (curr.getType()) {
                            case CPPTokenTypes.LITERAL_typeof:
                            case CPPTokenTypes.LITERAL___typeof:
                            case CPPTokenTypes.LITERAL___typeof__:
                                typeof = true;
                                break;
                            case CPPTokenTypes.CSM_EXPRESSION:
                                if (typeof) {
                                    classifier = curr.getFirstChild();
                                    typeBeginning = classifier;
                                }
                                break;
                            case CPPTokenTypes.CSM_TYPE_COMPOUND:
                            case CPPTokenTypes.CSM_TYPE_BUILTIN:
                            case CPPTokenTypes.CSM_TYPE_ATOMIC:
                                if (!cpp11StyleFunction) {
                                    classifier = typeQuals != null ? typeQuals : curr;
                                    typeBeginning = classifier;
                                }
                                break;
                            case CPPTokenTypes.LITERAL_enum:
                                if (AstUtil.findSiblingOfType(curr, CPPTokenTypes.RCURLY) != null) {
                                    results.enclosing = EnumImpl.create(curr, scope, file, fileContent, !isRenderingLocalContext());
                                    if (results.getEnclosingClassifier() != null && scope instanceof MutableDeclarationsContainer) {
                                        ((MutableDeclarationsContainer) scope).addDeclaration(results.getEnclosingClassifier());
                                    }
                                    if (container != null && results.getEnclosingClassifier() != null && !ForwardClass.isForwardClass(results.getEnclosingClassifier())) {
                                        container.addDeclaration(results.getEnclosingClassifier());
                                    }
                                    break;
                                }
                            // else fall through!
                            case CPPTokenTypes.LITERAL_struct:
                            case CPPTokenTypes.LITERAL_union:
                            case CPPTokenTypes.LITERAL_class:
                                AST next = curr.getNextSibling();
                                if (next != null && next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
                                    classifier = typeQuals != null ? typeQuals : curr;
                                    typeBeginning = classifier;
                                    cfdi = createForwardClassDeclaration(ast, container, file, scope);
                                }
                                break;
                            case CPPTokenTypes.CSM_PTR_OPERATOR:
                                // store only 1-st one - the others (if any) follows,
                                // so it's TypeImpl.createType() responsibility to process them all
                                if (ptrOperator == null) {
                                    ptrOperator = curr;
                                }
                                break;
                            case CPPTokenTypes.CSM_QUALIFIED_ID:
                                // now token corresponds the name, since the case "struct S" is processed before
                                nameToken = curr;
                                name = AstUtil.findId(nameToken);
                                break;
                            case CPPTokenTypes.LSQUARE:
                                arrayDepth++;
                                break;
                            case CPPTokenTypes.COMMA:
                            case CPPTokenTypes.SEMICOLON:
                                TypeImpl typeImpl = null;
                                boolean planB = false;
                                if(objects != null) {
                                    AST token = firstChild.getNextSibling();
                                    CsmObject o = objects.get(OffsetableBase.getStartOffset(token));
                                    if(o instanceof TypeImpl) {
                                        typeImpl = (TypeImpl) o;
                                    } else {
                                        planB = true;
                                    }
                                } else {
                                    planB = true;
                                }
                                if(planB) {
                                    if (cfdi != null) {
                                        typeImpl = TypeFactory.createType(new AST[]{classifier, typeBeginning}, cfdi, file, ptrOperator, arrayDepth, null, scope, false, true);
                                    } else if (classifier != null) {
                                        typeImpl = TypeFactory.createType(new AST[]{classifier, typeBeginning}, file, ptrOperator, arrayDepth, null, scope, false, true);
                                    } else if (results.getEnclosingClassifier() != null) {
                                        typeImpl = TypeFactory.createType(results.getEnclosingClassifier(), ptrOperator, arrayDepth, ast, file);
                                    }
                                }
                                if (typeImpl != null) {
                                    typeImpl.setTypeOfTypedef();
                                    CsmTypedef typedef = createTypedef(ast/*nameToken*/, file, scope, typeImpl, name);
                                    if (typedef != null) {
                                        if (results.getEnclosingClassifier() != null && results.getEnclosingClassifier().getName().length() == 0) {
                                            ((TypedefImpl) typedef).setTypeUnnamed();
                                        }
                                        results.typedefs.add(typedef);
                                    }
                                }
                                ptrOperator = null;
                                name = "";
                                nameToken = null;
                                arrayDepth = 0;
                                typeBeginning = curr.getNextSibling();
                                break;

                            case CPPTokenTypes.POINTERTO:
                                if (classifier != null && AstUtil.findChildOfType(classifier, CPPTokenTypes.LITERAL_auto) != null) {
                                    cpp11StyleFunction = true;
                                }
                                break;
                        }
                        
                        // Handle const/volatile quals
                        if (isCVQualifier(curr.getType())) {
                            if (typeQuals == null) {
                                typeQuals = curr;
                            }
                        } else {
                            typeQuals = null; // Maybe too cautious.
                        }
                    }
                } else if (firstChild.getType() == CPPTokenTypes.CSM_TYPE_ALIAS ||
                            firstChild.getType() == CPPTokenTypes.LITERAL_using ||
                            firstChild.getType() == CPPTokenTypes.LITERAL_template) {
                    CsmOffsetableDeclaration declaration = null;
                    
                    int typeToken = -1;
                    AST classifier = null;
                    int arrayDepth = 0;
                    AST nameToken = null;
                    AST ptrOperator = null;
                    AST typeQuals = null;
                    AST templateParams = null;
                    AST typeAliasAst = ast;
                    CharSequence name = "";

                    boolean typeof = false;
                    for (AST curr = firstChild; curr != null; curr = curr.getNextSibling()) {
                        switch (curr.getType()) {
                            case CPPTokenTypes.CSM_TYPE_ALIAS:
                                typeAliasAst = curr;
                                curr = curr.getFirstChild(); // go deeper
                                break;
                            case CPPTokenTypes.IDENT:
                                // now token corresponds the name, since the case "struct S" is processed before
                                nameToken = curr;
                                name = AstUtil.getText(nameToken);
                                break;
                            case CPPTokenTypes.LITERAL_template:
                                templateParams = curr;
                                break;
                            case CPPTokenTypes.CSM_TYPE_COMPOUND:
                            case CPPTokenTypes.CSM_TYPE_BUILTIN: 
                            case CPPTokenTypes.CSM_TYPE_ATOMIC: {
                                classifier = typeQuals != null ? typeQuals : curr;
                                typeToken = curr.getType();
                                break;
                            }
                            case CPPTokenTypes.LITERAL_class:
                            case CPPTokenTypes.LITERAL_struct:
                            case CPPTokenTypes.LITERAL_union: {
                                AST next = curr.getNextSibling();
                                if (next != null && next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
                                    classifier = typeQuals != null ? typeQuals : curr;
                                    typeToken = curr.getType();
                                }
                                break;
                            }
                            case CPPTokenTypes.CSM_ENUM_DECLARATION:
                            case CPPTokenTypes.CSM_CLASS_DECLARATION: {
                                // This is type alias to class definition

                                if (templateParams != null) {
                                    // [dcl.type], point 3:
                                    // A type-specifier-seq shall not define a class or enumeration
                                    // unless it appears in the type-id of an alias-declaration (7.1.3) that
                                    // is not the declaration of a template-declaration.
                                    break;
                                }
                                
                                if (ptrOperator == null) {
                                    ptrOperator = AstUtil.findLastSiblingOfType(curr.getFirstChild(), CPPTokenTypes.CSM_PTR_OPERATOR);
                                }

                                // Process class definition
                                if (curr.getType() == CPPTokenTypes.CSM_CLASS_DECLARATION) {
                                    declaration = createClass(curr, scope, container);
                                } else {
                                    declaration = createEnum(curr, scope, container);
                                }
                                
                                typeToken = curr.getType();
                                break;
                            }
                            case CPPTokenTypes.CSM_PTR_OPERATOR:
                                // store only 1-st one - the others (if any) follows,
                                // so it's TypeImpl.createType() responsibility to process them all
                                if (ptrOperator == null) {
                                    ptrOperator = curr;
                                }
                                break;
                            case CPPTokenTypes.SEMICOLON: {
                                TypeImpl typeImpl;
                                List<CsmTemplateParameter> csmTemplateParams;
                                switch (typeToken) {
                                    case CPPTokenTypes.CSM_TYPE_COMPOUND:
                                    case CPPTokenTypes.CSM_TYPE_BUILTIN:
                                        csmTemplateParams = renderTemplateParams(templateParams, scope);
                                        // Type of type alias could have similar syntax as declarations in function parameters
                                        // (unnamed types are allowed as alias has name outside its type declaration)
                                        typeImpl = TypeFactory.createType(classifier, null, file, null, ptrOperator, arrayDepth, null, scope, csmTemplateParams, true, true);
                                        if (typeImpl != null) {
                                            typeImpl.setTypeOfTypedef();
                                            createTypeAlias(results, ast, csmTemplateParams, file, scope, typeImpl, name);
                                        }
                                        break;
                                    case CPPTokenTypes.LITERAL_class:
                                    case CPPTokenTypes.LITERAL_struct:
                                    case CPPTokenTypes.LITERAL_union:
                                        CsmClassForwardDeclaration cfdi = createForwardClassDeclaration(typeAliasAst, container, file, scope);
                                        typeImpl = TypeFactory.createType(classifier, cfdi, file, ptrOperator, arrayDepth, null, scope, false, true);
                                        if (typeImpl != null) {
                                            typeImpl.setTypeOfTypedef();
                                            createTypeAlias(results, ast, null, file, scope, typeImpl, name);
                                        }
                                        break;
                                    case CPPTokenTypes.CSM_ENUM_DECLARATION:
                                    case CPPTokenTypes.CSM_CLASS_DECLARATION:
                                        if (CsmKindUtilities.isClassifier(declaration)) {
                                            typeImpl = TypeFactory.createType((CsmClassifier)declaration, ptrOperator, arrayDepth, curr.getFirstChild(), file, declaration.getStartOffset(), declaration.getEndOffset());
                                            typeImpl.setTypeOfTypedef();
                                            csmTemplateParams = renderTemplateParams(templateParams, scope);
                                            createTypeAlias(results, ast, csmTemplateParams, file, scope, typeImpl, name);
                                        }
                                        break;
                                }
                                typeToken = -1;
                                classifier = null;
                                ptrOperator = null;
                                name = "";
                                nameToken = null;
                                arrayDepth = 0;
                                break;
                            }
                        }
                        
                        // Handle const/volatile quals
                        if (isCVQualifier(curr.getType())) {
                            if (typeQuals == null) {
                                typeQuals = curr;
                            }
                        } else {
                            typeQuals = null; // Maybe too cautious.
                        }
                    }
                }
            }
        }
        return results;
    }

    private List<CsmTemplateParameter> renderTemplateParams(AST templateParams, CsmScope scope) {
        List<CsmTemplateParameter> csmTemplateParams = null;
        if (templateParams != null) {
            csmTemplateParams = TemplateUtils.getTemplateParameters(templateParams, getContainingFile(), scope, !isRenderingLocalContext());
        }
        return csmTemplateParams;
    }

    protected CsmClassForwardDeclaration createForwardClassDeclaration(AST ast, MutableDeclarationsContainer container, FileImpl file, CsmScope scope) {
        return ClassForwardDeclarationImpl.create(ast, file, scope, container, !isRenderingLocalContext());
    }
    
    protected CsmEnumForwardDeclaration createForwardEnumDeclaration(AST ast, MutableDeclarationsContainer container, FileImpl file, CsmScope scope) {
        return EnumForwardDeclarationImpl.create(ast, file, scope, container, !isRenderingLocalContext());
    }

    private CsmTypedef createTypeAlias(Pair results, AST nameAST, List<CsmTemplateParameter> csmTemplateParams, FileImpl file, CsmScope scope, CsmType typeImpl, CharSequence name) {
        if (csmTemplateParams != null) {
            typeImpl = TemplateUtils.checkTemplateType(typeImpl, scope, csmTemplateParams);
        }

        CsmTypeAlias typeAlias = createTypeAlias(nameAST/*nameToken*/, file, scope, typeImpl, name);

        if (typeAlias != null) {
            if (results.getEnclosingClassifier() != null && results.getEnclosingClassifier().getName().length() == 0) {
                ((TypedefImpl) typeAlias).setTypeUnnamed();
            }
            if (csmTemplateParams != null) {
                TemplateDescriptor templateDescriptor = new TemplateDescriptor(csmTemplateParams, name, false, !isRenderingLocalContext());
                ((TypedefImpl) typeAlias).setTemplateDescriptor(templateDescriptor);
            }
            results.typedefs.add(typeAlias);
        }
        return typeAlias;
    }

    protected ClassImpl createClass(AST token, CsmScope scope, DeclarationsContainer container) throws AstRendererException {
        ClassImpl cls = TemplateUtils.isPartialClassSpecialization(token) ?
                            ClassImplSpecialization.create(token, scope, file, language, languageFlavor, fileContent, !isRenderingLocalContext(), container) :
                            ClassImpl.create(token, scope, file, language, languageFlavor, fileContent, !isRenderingLocalContext(), container);
        if (container instanceof MutableDeclarationsContainer) {
            ((MutableDeclarationsContainer)container).addDeclaration(cls);
        }
        return cls;
    }

    protected EnumImpl createEnum(AST token, CsmScope scope, DeclarationsContainer container) {
        EnumImpl csmEnum = EnumImpl.create(token, scope, file, fileContent, !isRenderingLocalContext());
        if (container instanceof MutableDeclarationsContainer) {
            ((MutableDeclarationsContainer)container).addDeclaration(csmEnum);
        }
        return csmEnum;
    }

    protected CsmTypedef createTypedef(AST ast, FileImpl file, CsmObject container, CsmType type, CharSequence name) {
        return TypedefImpl.create(ast, file, container, type, name, !isRenderingLocalContext());
    }

    protected CsmTypeAlias createTypeAlias(AST ast, FileImpl file, CsmObject container, CsmType type, CharSequence name) {
        return TypeAliasImpl.create(ast, file, container, type, name, !isRenderingLocalContext());
    }

    public boolean renderForwardClassDeclaration(
            AST ast,
            NamespaceImpl currentNamespace, MutableDeclarationsContainer container,
            FileImpl file,
            boolean isRenderingLocalContext) {

        AST child = ast.getFirstChild();
        if (child == null) {
            return false;
        }
        if (child.getType() == CPPTokenTypes.LITERAL_template) {
            child = skipTemplateSibling(child);
            if (child == null) {
                return false;
            }
        }

        switch (child.getType()) {
            case CPPTokenTypes.LITERAL_class:
            case CPPTokenTypes.LITERAL_struct:
            case CPPTokenTypes.LITERAL_union:
                AST next = child.getNextSibling();
                if (next != null && next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
                    createForwardClassDeclaration(ast, container, file, currentNamespace);
                    return true;
                }
        }

        return false;
    }

    public boolean renderForwardMemberDeclaration(
            AST ast,
            NamespaceImpl currentNamespace, MutableDeclarationsContainer container,
            FileImpl file) {

        AST child = ast.getFirstChild();
        while (child != null) {
            switch (child.getType()) {
                case CPPTokenTypes.LITERAL_template:
                    child = skipTemplateSibling(child);
                    continue;
                case CPPTokenTypes.LITERAL_inline:
                case CPPTokenTypes.LITERAL__inline:
                case CPPTokenTypes.LITERAL___inline:
                case CPPTokenTypes.LITERAL___inline__:
                    child = child.getNextSibling();
                    continue;
            }
            break;
        }
        if (child == null) {
            return false;
        }
        child = getFirstSiblingSkipQualifiers(child);
        if (child == null) {
            return false;
        }

        switch (child.getType()) {
            case CPPTokenTypes.CSM_TYPE_COMPOUND:
            case CPPTokenTypes.CSM_TYPE_BUILTIN:
                child = getFirstSiblingSkipQualifiers(child.getNextSibling());
                while (child != null && child.getType() == CPPTokenTypes.CSM_PTR_OPERATOR) {
                    child = child.getNextSibling();
                }
                if (child != null) {
                    if (child.getType() == CPPTokenTypes.CSM_VARIABLE_DECLARATION ||
                            child.getType() == CPPTokenTypes.CSM_ARRAY_DECLARATION) {
                        //static variable definition
                        return renderVariable(ast, currentNamespace, container, currentNamespace, false);
                    } else {
                        //method forward declaratin
                        try {
                            FunctionImpl<?> ftdecl = FunctionImpl.create(ast, file, fileContent, null, currentNamespace, !isRenderingLocalContext(),objects);
                            if (container != null) {
                                container.addDeclaration(ftdecl);
                            }
                            if (NamespaceImpl.isNamespaceScope(ftdecl)) {
                                currentNamespace.addDeclaration(ftdecl);
                            }
                        } catch (AstRendererException e) {
                            DiagnosticExceptoins.register(e);
                        }
                        return true;
                    }
                }
                break;
        }

        return false;
    }

    public static CharSequence getQualifiedName(AST qid) {
        if (qid != null && (qid.getType() == CPPTokenTypes.CSM_QUALIFIED_ID || qid.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND)) {
            if (qid.getFirstChild() != null) {
                StringBuilder sb = new StringBuilder();
                for (AST namePart = qid.getFirstChild(); namePart != null; namePart = namePart.getNextSibling()) {
                    // TODO: update this assert it should accept names like: allocator<char, typename A>
//                    if( ! ( namePart.getType() == CPPTokenTypes.ID || namePart.getType() == CPPTokenTypes.SCOPE ||
//                            namePart.getType() == CPPTokenTypes.LESSTHAN || namePart.getType() == CPPTokenTypes.GREATERTHAN ||
//                            namePart.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN || namePart.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND ||
//                            namePart.getType() == CPPTokenTypes.COMMA) ) {
//			new Exception("Unexpected token type " + namePart).printStackTrace(System.err);
//		    }
                    if (namePart.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ||
                            namePart.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND) {
                        AST builtInType = namePart.getFirstChild();
                        if (builtInType != null) {
                            sb.append(AstUtil.getText(builtInType));
                        }
                    } else {
                        sb.append(AstUtil.getText(namePart));
                    }
                }
                return TextCache.getManager().getString(sb);
            }
        }
        return "";
    }

    public static CharSequence[] getNameTokens(AST qid) {
        if (qid != null && (qid.getType() == CPPTokenTypes.CSM_QUALIFIED_ID || qid.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND)) {
            int templateDepth = 0;
            List<CharSequence> l = new ArrayList<>();
            for (AST namePart = qid.getFirstChild(); namePart != null; namePart = namePart.getNextSibling()) {
                if (templateDepth == 0 && namePart.getType() == CPPTokenTypes.IDENT) {
                    l.add(NameCache.getManager().getString(AstUtil.getText(namePart)));
                } else if (namePart.getType() == CPPTokenTypes.LESSTHAN) {
                    // the beginning of template parameters
                    templateDepth++;
                } else if (namePart.getType() == CPPTokenTypes.GREATERTHAN) {
                    // the beginning of template parameters
                    templateDepth--;
                } else {
                    //assert namePart.getType() == CPPTokenTypes.SCOPE;
                    if (templateDepth == 0 && namePart.getType() != CPPTokenTypes.SCOPE) {
                        StringBuilder tokenText = new StringBuilder();
                        tokenText.append('[').append(AstUtil.getText(namePart));
                        if (namePart.getNumberOfChildren() == 0) {
                            tokenText.append(", line=").append(namePart.getLine()); // NOI18N
                            tokenText.append(", column=").append(namePart.getColumn()); // NOI18N
                        }
                        tokenText.append(']');
                        System.err.println("Incorect token: expected '::', found " + tokenText.toString());
                    }
                }
            }
            return l.toArray(new CharSequence[l.size()]);
        }
        return new CharSequence[0];
    }

    public static TypeImpl renderType(AST tokType, CsmFile file, CsmScope scope, boolean global) {
        return renderType(tokType, file, false, scope, global);
    }

    public static TypeImpl renderType(AST tokType, CsmFile file, boolean inSpecOrFunParams, CsmScope scope, boolean global) {
        return renderType(tokType, file, null, inSpecOrFunParams, scope, global);
    }

    /**
     * Creates type from AST
     * @param tokType - ast for type
     * @param file - containing file
     * @param fileContent - content of the file
     * @param inSpecOrFunParams - if it is a type inside spec or function params
     * @param scope - scope of type usage/declaration
     * @param global - if we are in global context
     * @return type
     */
    public static TypeImpl renderType(AST tokType, CsmFile file, FileContent fileContent, boolean inSpecOrFunParams, CsmScope scope, boolean global) {
        AST typeAST = tokType;
        tokType = getFirstSiblingSkipQualifiers(tokType);

        if (tokType != null) {
            if (tokType.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ||
                    tokType.getType() == CPPTokenTypes.CSM_TYPE_ATOMIC ||
                    tokType.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND) {
                AST next = getFirstSiblingSkipQualifiers(
                    getFirstSiblingSkipFunctionSpecifiers(
                        AstUtil.skipTokens(tokType.getNextSibling(), CPPTokenTypes.LITERAL_friend)
                    )
                );
                AST ptrOperator = (next != null && next.getType() == CPPTokenTypes.CSM_PTR_OPERATOR) ? next : null;
                if (inSpecOrFunParams) {
                    return TypeFactory.createType(typeAST, file, ptrOperator, 0, null, null, true, false);
                } else {
                    return TypeFactory.createType(typeAST, file, ptrOperator, 0);
                }
            }
            if (AstUtil.isElaboratedKeyword(tokType)) {
                boolean createForwardDecl = (tokType.getType() != CPPTokenTypes.LITERAL_enum);

                AST next = tokType.getNextSibling();
                if (next != null && next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
                    AST tokenTypeStart = tokType;

                    tokType = next;
                    next = tokType.getNextSibling();

                    AST ptrOperator = (next != null && next.getType() == CPPTokenTypes.CSM_PTR_OPERATOR) ? next : null;

                    if (createForwardDecl && scope != null) {
                        // Find first namespace scope to add elaborated forwards in it
                        MutableObject<CsmNamespace> targetScope = new MutableObject<>();
                        MutableObject<MutableDeclarationsContainer> targetDefinitionContainer = new MutableObject<>();
                        getClosestNamespaceInfo(scope, file, fileContent, OffsetableBase.getStartOffset(tokenTypeStart), targetScope, targetDefinitionContainer);

                        FakeAST fakeParent = new FakeAST();
                        fakeParent.addChild(tokenTypeStart);
                        ClassForwardDeclarationImpl.create(fakeParent, file, targetScope.value, targetDefinitionContainer.value, global, true);
                    }

                    return TypeFactory.createType(typeAST, file, ptrOperator, 0);
                }
            }
        }

        /**
        CsmClassifier classifier = null;
        if( tokType.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ) {
        classifier = BuiltinTypes.getBuiltIn(tokType);
        }
        else { // tokType.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND
        try {
        Resolver resolver = new Resolver(file, ((CsmAST) tokType.getFirstChild()).getOffset());
        // gather name components into string array
        // for example, for std::vector new CharSequence[] { "std", "vector" }
        List l = new ArrayList();
        for( AST namePart = tokType.getFirstChild(); namePart != null; namePart = namePart.getNextSibling() ) {
        if( namePart.getType() == CPPTokenTypes.ID ) {
        l.add(namePart.getText());
        }
        else {
        assert namePart.getType() == CPPTokenTypes.SCOPE;
        }
        }
        CsmObject o = resolver.resolve((String[]) l.toArray(new CharSequence[l.size()]));
        if( o instanceof CsmClassifier ) {
        classifier = (CsmClassifier) o;
        }
        }
        catch( Exception e ) {
        e.printStackTrace(System.err);
        }
        }

        if( classifier != null ) {
        AST next = tokType.getNextSibling();
        AST ptrOperator =  (next != null && next.getType() == CPPTokenTypes.CSM_PTR_OPERATOR) ? next : null;
        return TypeImpl.createType(classifier, ptrOperator, 0);
        }

        return null;
         */
        return null;
    }
    
    /**
     * 
     * @param ast
     * @return first sibling, skips function specifiers (inline, virtual, explicit)
     */
    public static AST getFirstSiblingSkipFunctionSpecifiers(AST ast) {
        return AstUtil.skipTokens(
            ast, 
            CPPTokenTypes.LITERAL_inline, 
            CPPTokenTypes.LITERAL_virtual,
            CPPTokenTypes.LITERAL_explicit
        );
    }
    
    

    /**
     * Returns first sibling (or just passed ast), skips cv-qualifiers and storage class specifiers
     */
    public static AST getFirstSiblingSkipQualifiers(AST ast) {
        while (ast != null && isQualifier(ast.getType())) {
            ast = ast.getNextSibling();
        }
        return ast;
    }

    /**
     * Returns first sibling (or just passed ast), skips inline
     */
    public static AST getFirstSiblingSkipInline(AST ast) {
        while (ast != null && isInline(ast.getType())) {
            ast = ast.getNextSibling();
        }
        return ast;
    }

    /**
     * Returns first child, skips cv-qualifiers and storage class specifiers
     */
    public static AST getFirstChildSkipQualifiers(AST ast) {
        return getFirstSiblingSkipQualifiers(ast.getFirstChild());
    }

    public static boolean isQualifier(int tokenType) {
        return isCVQualifier(tokenType) || isStorageClassSpecifier(tokenType) || isAtomicQualifier(tokenType)
                || (tokenType == CPPTokenTypes.LITERAL_typename);
    }

    public static boolean isCVQualifier(int tokenType) {
        return isConstQualifier(tokenType) || isVolatileQualifier(tokenType);
    }

    public static boolean isAtomicQualifier(int tokenType) {
        switch (tokenType) {
            case CPPTokenTypes.LITERAL__Atomic:
                return true;
            default:
                return false;
        }
    }

    public static boolean isConstQualifier(int tokenType) {
        switch (tokenType) {
            case CPPTokenTypes.LITERAL_const:
                return true;
            case CPPTokenTypes.LITERAL___const:
                return true;
            case CPPTokenTypes.LITERAL___const__:
                return true;
            case CPPTokenTypes.LITERAL_constexpr:
                return true;
            default:
                return false;
        }
    }

    public static boolean isVolatileQualifier(int tokenType) {
        switch (tokenType) {
            case CPPTokenTypes.LITERAL_volatile:
                return true;
            case CPPTokenTypes.LITERAL___volatile__:
                return true;
            case CPPTokenTypes.LITERAL___volatile:
                return true;
            default:
                return false;
        }
    }

    public static boolean isStorageClassSpecifier(int tokenType) {
        switch (tokenType) {
            case CPPTokenTypes.LITERAL_auto:
                return true;
            case CPPTokenTypes.LITERAL_register:
                return true;
            case CPPTokenTypes.LITERAL_static:
                return true;
            case CPPTokenTypes.LITERAL_extern:
                return true;
            case CPPTokenTypes.LITERAL_mutable:
                return true;
            case CPPTokenTypes.LITERAL___thread:
            case CPPTokenTypes.LITERAL___symbolic:
            case CPPTokenTypes.LITERAL___global:
            case CPPTokenTypes.LITERAL___hidden:
                return true;
            case CPPTokenTypes.LITERAL_thread_local:
            case CPPTokenTypes.LITERAL__Thread_local:
                return true;
            default:
                return false;
        }
    }

    public static boolean isInline(int tokenType) {
        switch (tokenType) {
            case CPPTokenTypes.LITERAL_inline:
            case CPPTokenTypes.LITERAL__inline:
            case CPPTokenTypes.LITERAL___inline:
            case CPPTokenTypes.LITERAL___inline__:
                return true;
            default:
                return false;
        }
    }

    /**
     * Checks whether the given AST is a variable declaration(s),
     * if yes, creates variable(s), adds to container(s), returns true,
     * otherwise returns false;
     *
     * There might be two containers, in which the given variable should be added.
     * For example, global variables should be added both to file and to global namespace;
     * variables, declared in some namespace definition, should be added to both this definition and correspondent namespace as well.
     *
     * On the other hand, local variables are added only to it's containing scope, so either container1 or container2 might be null.
     *
     * @param ast AST to process
     * @param container1 container to add created variable into (may be null)
     * @param container2 container to add created variable into (may be null)
     */
    public boolean renderVariable(AST ast, MutableDeclarationsContainer namespaceContainer, MutableDeclarationsContainer container2, CsmScope scope, boolean functionParameter) {
        boolean _static = AstUtil.hasChildOfType(ast, CPPTokenTypes.LITERAL_static);
        boolean _extern = AstUtil.hasChildOfType(ast, CPPTokenTypes.LITERAL_extern);
        AST templateAst = null;
        AST typeAST = ast.getFirstChild();
        AST tokType = typeAST;
        while (tokType != null && tokType.getType() == CPPTokenTypes.LITERAL_template) {
            if (templateAst == null) {
                templateAst = tokType;
            }
            typeAST = tokType = skipTemplateSibling(tokType);
        }
        tokType = getFirstSiblingSkipQualifiers(tokType);
        if (tokType == null) {
            return false;
        }
        boolean isThisReference = false;
        CsmClassForwardDeclaration cfdi = null;
        boolean createForwardClass = false;
        if (tokType.getType() == CPPTokenTypes.LITERAL_struct ||
            tokType.getType() == CPPTokenTypes.LITERAL_union ||
            tokType.getType() == CPPTokenTypes.LITERAL_enum ||
            tokType.getType() == CPPTokenTypes.LITERAL_class) {
            // This is struct/class word for reference on containing struct/class
            AST keyword = tokType;
            typeAST = tokType;
            tokType = tokType.getNextSibling();
            if (tokType == null) {
                return false;
            }
            if (keyword.getType() != CPPTokenTypes.LITERAL_enum && tokType.getType() == CPPTokenTypes.CSM_QUALIFIED_ID && !isRenderingLocalContext()) {
//                if(namespaceContainer == null && container2 == null && !functionParameter) {
                    createForwardClass = !isRenderingLocalContext();
//                }
                }
/*
            if (keyword.getType() != CPPTokenTypes.LITERAL_enum && tokType.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
                createForwardClass = true;
            }
 */
            isThisReference = true;
        }
        if (isConstQualifier(tokType.getType())) {
            assert (false) : "must be skipped above";
            tokType = tokType.getNextSibling();
        }

        boolean typeof = false;
        if (tokType.getType() == CPPTokenTypes.LITERAL_typeof ||
                tokType.getType() == CPPTokenTypes.LITERAL___typeof ||
                tokType.getType() == CPPTokenTypes.LITERAL___typeof__
                ) {
            typeof = true;
            AST next = tokType.getNextSibling();
            if (next.getType() == CPPTokenTypes.LPAREN) {
                next = next.getNextSibling();
                typeAST = next;
            }
            tokType = next.getNextSibling();
        }
        if (typeof || tokType.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ||
                tokType.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND ||
                tokType.getType() == CPPTokenTypes.CSM_TYPE_ATOMIC ||
                tokType.getType() == CPPTokenTypes.CSM_QUALIFIED_ID && isThisReference ||
                tokType.getType() == CPPTokenTypes.IDENT && isThisReference||
                tokType.getType() == CPPTokenTypes.CSM_VARIABLE_DECLARATION) {
            AST nextToken;
            if(tokType.getType() == CPPTokenTypes.CSM_VARIABLE_DECLARATION) {
                nextToken = tokType;
            } else {
                nextToken = tokType.getNextSibling();
            }
            while (nextToken != null &&
                    (nextToken.getType() == CPPTokenTypes.CSM_PTR_OPERATOR ||
                    isQualifier(nextToken.getType()) ||
                    nextToken.getType() == CPPTokenTypes.LPAREN)) {
                nextToken = nextToken.getNextSibling();
            }

            if (nextToken == null ||
                    nextToken.getType() == CPPTokenTypes.LSQUARE ||
                    nextToken.getType() == CPPTokenTypes.CSM_VARIABLE_DECLARATION ||
                    nextToken.getType() == CPPTokenTypes.CSM_VARIABLE_LIKE_FUNCTION_DECLARATION ||
                    nextToken.getType() == CPPTokenTypes.CSM_ARRAY_DECLARATION ||
                    nextToken.getType() == CPPTokenTypes.ASSIGNEQUAL) {

                AST ptrOperator = null;
                boolean theOnly = true;
                boolean hasVariables = false;
                int inParamsLevel = 0;

                for (AST token = ast.getFirstChild(); token != null; token = token.getNextSibling()) {
                    switch (token.getType()) {
                        case CPPTokenTypes.LPAREN:
                            inParamsLevel++;
                            break;
                        case CPPTokenTypes.RPAREN:
                            inParamsLevel--;
                            break;
                        case CPPTokenTypes.CSM_PTR_OPERATOR:
                            // store only 1-st one - the others (if any) follows,
                            // so it's TypeImpl.createType() responsibility to process them all
                            if (ptrOperator == null && inParamsLevel == 0) {
                                ptrOperator = token;
                            }
                            break;
                        case CPPTokenTypes.CSM_VARIABLE_DECLARATION:
                        case CPPTokenTypes.CSM_ARRAY_DECLARATION:
                            hasVariables = true;
                            if (theOnly) {
                                for (AST next = token.getNextSibling(); next != null; next = next.getNextSibling()) {
                                    int type = next.getType();
                                    if (type == CPPTokenTypes.CSM_VARIABLE_DECLARATION || type == CPPTokenTypes.CSM_ARRAY_DECLARATION) {
                                        theOnly = false;
                                    }
                                }
                            }
                            processVariable(token, ptrOperator, (theOnly ? ast : token), typeAST/*tokType*/, templateAst, namespaceContainer, container2, file, _static, _extern, functionParameter, cfdi);
                            ptrOperator = null;
                            break;
                        case CPPTokenTypes.CSM_VARIABLE_LIKE_FUNCTION_DECLARATION:
                            AST inner = token.getFirstChild();
                            if (inner != null) {
                                theOnly = false;
                                TypeImpl type = null;
                                if (tokType.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN) {
                                    AST typeNameToken = tokType.getFirstChild();
                                    if (typeNameToken != null) {
                                        type = TypeFactory.createBuiltinType(AstUtil.getText(typeNameToken), ptrOperator, 0, tokType, file);
                                    }
                                } else {
                                    type = TypeFactory.createType(typeAST, file, ptrOperator, 0);
                                }
                                if (isVariableLikeFunc(token)) {
//                                    CsmScope scope = (namespaceContainer instanceof CsmNamespace) ? (CsmNamespace) namespaceContainer : null;
                                    processFunction(token, file, type, namespaceContainer, container2, scope);
                                } else {
                                    processVariable(token, ptrOperator, (theOnly ? ast : token), typeAST/*tokType*/, templateAst, namespaceContainer, container2, file, _static, _extern, false, cfdi);
                                    ptrOperator = null;
                                }
                            }
                    }
                }
                if (!hasVariables && functionParameter) {
                    // unnamed parameter
                    processVariable(ast, ptrOperator, ast, typeAST/*tokType*/, templateAst, namespaceContainer, container2, file, _static, _extern, functionParameter, cfdi);
                }
                if (createForwardClass) {
                    MutableObject<CsmNamespace> targetScope = new MutableObject<>();
                    MutableObject<MutableDeclarationsContainer> targetDefinitionContainer = new MutableObject<>();
                    getClosestNamespaceInfo(scope, file, fileContent, OffsetableBase.getStartOffset(ast), targetScope, targetDefinitionContainer);

                    ClassForwardDeclarationImpl.create(ast, file, targetScope.value, (MutableDeclarationsContainer) targetDefinitionContainer.value, !isRenderingLocalContext(), true);
                }
                return true;
            }

            if (functionParameter && nextToken != null) {
                if (nextToken.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
                    processVariable(nextToken, null, ast, typeAST/*tokType*/, templateAst, namespaceContainer, container2, file, _static, _extern, true, cfdi);
                } else if (nextToken.getType() == CPPTokenTypes.CSM_PARMLIST) {
                    processVariable(null, null, ast, typeAST/*tokType*/, templateAst, namespaceContainer, container2, file, _static, _extern, true, cfdi);
                } else if (nextToken.getType() == CPPTokenTypes.RPAREN) {
                    processVariable(ast, null, ast, typeAST/*tokType*/, templateAst, namespaceContainer, container2, file, _static, _extern, true, cfdi);
                }
            }
        }
        return false;
    }

    @SuppressWarnings("fallthrough")
    protected void processVariable(AST varAst, AST ptrOperator, AST offsetAst, AST classifier, AST templateAst,
            MutableDeclarationsContainer container1, MutableDeclarationsContainer container2,
            FileImpl file, boolean _static, boolean _extern, boolean inFunctionParameters, CsmClassForwardDeclaration cfdi) {
        int arrayDepth = 0;
        NameHolder name = NameHolder.createName(CharSequences.empty());
        AST qn = null;
        if (varAst != null) {
            int inParamsLevel = 0;
            for (AST token = varAst.getFirstChild(); token != null; token = token.getNextSibling()) {
                switch (token.getType()) {
                    case CPPTokenTypes.LPAREN:
                        inParamsLevel++;
                        break;
                    case CPPTokenTypes.RPAREN:
                        inParamsLevel--;
                        break;
                    case CPPTokenTypes.LSQUARE:
                        if (inParamsLevel == 0) {
                            arrayDepth++;
                        }
                        break;
                    case CPPTokenTypes.CSM_EXPRESSION:
                        // TODO: TypeImpl should store expression for array definitions
                        break;
                    case CPPTokenTypes.LITERAL_struct:
                    case CPPTokenTypes.LITERAL_union:
                    case CPPTokenTypes.LITERAL_enum:
                    case CPPTokenTypes.LITERAL_class:
                        // skip both this and next
                        token = token.getNextSibling();
                        continue;
                    case CPPTokenTypes.CSM_QUALIFIED_ID:
                        if (inParamsLevel == 0) {
                            qn = token;
                            name = NameHolder.createSimpleName(getVarNameNode(token));
                        }
                        break;
                    case CPPTokenTypes.IDENT:
                        if (inParamsLevel == 0) {
                            name = NameHolder.createSimpleName(token);
                        }
                        break;
                }
            }
        }
        CsmType type;
        if (cfdi != null) {
            type = TypeFactory.createType(classifier, cfdi, file, ptrOperator, arrayDepth, null, null, inFunctionParameters, false);
        } else {
            type = TypeFactory.createType(classifier, file, ptrOperator, arrayDepth, null, null, inFunctionParameters);
        }
        if (isScopedId(qn)) {
            if (isRenderingLocalContext()) {
                System.err.println("error in rendering " + file + " offset:" + offsetAst); // NOI18N
            }
            // This is definition of global namespace variable or definition of static class variable
            // TODO What about global variable definitions:
            // extern int i; - declaration
            // int i; - definition
            // TODO _extern = false?
            VariableDefinitionImpl var = VariableDefinitionImpl.create(offsetAst, file, type, name, _static, _extern);
            if (container2 != null) {
                container2.addDeclaration(var);
            }
        } else {
            VariableImpl<?> var = createVariable(offsetAst, templateAst, file, type, name, _static, _extern, container1, container2, null);
            if (container2 != null) {
                container2.addDeclaration(var);
            }
            // TODO! don't add to namespace if....
            if (container1 != null) {
                container1.addDeclaration(var);
            }
        }
    }

    protected VariableImpl<?> createVariable(AST offsetAst, AST templateAst, CsmFile file, CsmType type, NameHolder name, boolean _static,  boolean _extern,
            MutableDeclarationsContainer container1, MutableDeclarationsContainer container2, CsmScope scope) {
        VariableImpl<?> var = VariableImpl.create(offsetAst, file, type, name, scope, _static, _extern, !isRenderingLocalContext());
        return var;
    }

    protected void processFunction(AST token, CsmFile file, CsmType type,
             MutableDeclarationsContainer container1,
             MutableDeclarationsContainer container2, CsmScope scope) {
        FunctionImpl<?> fun = createFunction(token, file, type, scope);
        if (fun != null) {
            if (container2 != null) {
                container2.addDeclaration(fun);
            }
            if (container2 != null && NamespaceImpl.isNamespaceScope(fun)) {
                container1.addDeclaration(fun);
            }
        }
    }

    protected FunctionImpl<?> createFunction(AST ast, CsmFile file, CsmType type, CsmScope scope) {
        FunctionImpl<?> fun = null;
        try {
            fun = FunctionImpl.create(ast, file, fileContent, type, scope, !isRenderingLocalContext(),objects);
        } catch (AstRendererException ex) {
            Exceptions.printStackTrace(ex);
        }
        return fun;
    }

    public static List<CsmParameter> renderParameters(AST ast, final CsmFile file, FileContent fileContent, CsmScope scope) {
        ArrayList<CsmParameter> parameters = new ArrayList<>();
        if (ast != null && (ast.getType() == CPPTokenTypes.CSM_PARMLIST ||
                ast.getType() == CPPTokenTypes.CSM_KR_PARMLIST)) {
            for (AST token = ast.getFirstChild(); token != null; token = token.getNextSibling()) {
                if (token.getType() == CPPTokenTypes.CSM_PARAMETER_DECLARATION) {
                    List<ParameterImpl> params = renderParameter(token, file, fileContent, scope);
                    if (params != null) {
                        parameters.addAll(params);
                    }
                }
            }
        }
        parameters.trimToSize();
        return parameters;
    }

    public static boolean isVoidParameter(AST ast) {
        if (ast != null && (ast.getType() == CPPTokenTypes.CSM_PARMLIST ||
                ast.getType() == CPPTokenTypes.CSM_KR_PARMLIST)) {
            AST token = ast.getFirstChild();
            if (token != null && token.getType() == CPPTokenTypes.CSM_PARAMETER_DECLARATION) {
                AST firstChild = token.getFirstChild();
                if (firstChild != null) {
                    if (firstChild.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN && firstChild.getNextSibling() == null) {
                        AST grandChild = firstChild.getFirstChild();
                        if (grandChild != null && grandChild.getType() == CPPTokenTypes.LITERAL_void) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static List<ParameterImpl> renderParameter(AST ast, final CsmFile file, FileContent fileContent, final CsmScope scope1) {

        // The only reason there might be several declarations is the K&R C style
        // we can split this function into two (for K&R and "normal" parameters)
        // if we found this ineffective; but now I vote for more clear and readable - i.e. single for both cases - code

        final List<ParameterImpl> result = new ArrayList<>();
        AST firstChild = ast.getFirstChild();
        if (firstChild != null) {
            if (firstChild.getType() == CPPTokenTypes.ELLIPSIS) {
                ParameterEllipsisImpl parameter = ParameterEllipsisImpl.create(ast.getFirstChild(), file, null, scope1); // NOI18N
                result.add(parameter);
                return result;
            }
            if (firstChild.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN && firstChild.getNextSibling() == null) {
                AST grandChild = firstChild.getFirstChild();
                if (grandChild != null && grandChild.getType() == CPPTokenTypes.LITERAL_void) {
                    return Collections.emptyList();
                }
            }
        }
        class AstRendererEx extends AstRenderer {

            public AstRendererEx(FileContent fileContent) {
                super((FileImpl) file, fileContent, ((FileImpl) file).getFileLanguage(), ((FileImpl) file).getFileLanguageFlavor(), null);
            }

            @Override
            protected VariableImpl<?> createVariable(AST offsetAst, AST templateAst, CsmFile file, CsmType type, NameHolder name, boolean _static, boolean _extern, MutableDeclarationsContainer container1, MutableDeclarationsContainer container2, CsmScope scope2) {
                type = TemplateUtils.checkTemplateType(type, scope1);
                ParameterImpl parameter;
                if (offsetAst.getType() == CPPTokenTypes.ELLIPSIS) {
                    parameter = ParameterEllipsisImpl.create(offsetAst, file, type, scope1);
                } else {
                    parameter = ParameterImpl.create(offsetAst, file, type, name, scope1);
                }
                result.add(parameter);
                return parameter;
            }
        }
        AstRendererEx renderer = new AstRendererEx(fileContent);
        renderer.renderVariable(ast, null, null, scope1, true);
        return result;
    }

//    public static boolean isCsmType(AST token) {
//        if( token != null ) {
//            int type = token.getType();
//            return type == CPPTokenTypes.CSM_TYPE_BUILTIN || type == CPPTokenTypes.CSM_TYPE_COMPOUND;
//        }
//        return false;
//    }
    public static int getType(AST token) {
        return (token == null) ? -1 : token.getType();
    }

    public static int getFirstChildType(AST token) {
        AST child = token.getFirstChild();
        return (child == null) ? -1 : child.getType();
    }

//    public static int getNextSiblingType(AST token) {
//        AST sibling = token.getNextSibling();
//        return (sibling == null) ? -1 : sibling.getType();
//    }
    public boolean renderNSP(AST token, NamespaceImpl currentNamespace, MutableDeclarationsContainer container, FileImpl file) {
        token = token.getFirstChild();
        if (token == null) {
            return false;
        }
        switch (token.getType()) {
            case CPPTokenTypes.CSM_NAMESPACE_ALIAS:
                if(!TraceFlags.CPP_PARSER_ACTION || isRenderingLocalContext()) {
                    NamespaceAliasImpl alias = NamespaceAliasImpl.create(token, file, currentNamespace, !isRenderingLocalContext());
                    container.addDeclaration(alias);
                    currentNamespace.addDeclaration(alias);
                }
                return true;
            case CPPTokenTypes.CSM_USING_DIRECTIVE: {
                if(!TraceFlags.CPP_PARSER_ACTION || isRenderingLocalContext()) {
                    UsingDirectiveImpl using = UsingDirectiveImpl.create(token, file, !isRenderingLocalContext());
                    container.addDeclaration(using);
                    currentNamespace.addDeclaration(using);
                }
                return true;
            }
            case CPPTokenTypes.CSM_USING_DECLARATION: {
                if(!TraceFlags.CPP_PARSER_ACTION || isRenderingLocalContext()) {
                    UsingDeclarationImpl using = UsingDeclarationImpl.create(token, file, currentNamespace, !isRenderingLocalContext(), CsmVisibility.PUBLIC);
                    container.addDeclaration(using);
                    currentNamespace.addDeclaration(using);
                }
                return true;
            }
        }
        return false;
    }

    public static boolean isClassSpecialization(AST ast) {
        AST type = ast.getFirstChild(); // type
        if (type != null) {
            AST child = type;
            while ((child = child.getNextSibling()) != null) {
                if (child.getType() == CPPTokenTypes.GREATERTHAN) {
                    child = child.getNextSibling();
                    if (child != null && (child.getType() == CPPTokenTypes.LITERAL_class ||
                            child.getType() == CPPTokenTypes.LITERAL_struct)) {
                        return true;
                    }
                    if (child == null || child.getType() != CPPTokenTypes.LITERAL_template) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean isClassExplicitInstantiation(AST ast) {
        AST type = ast.getFirstChild(); // type
        if (type != null) {
            AST child = type;
            while ((child = child.getNextSibling()) != null) {
                if (child.getType() == CPPTokenTypes.GREATERTHAN) {
                    child = child.getNextSibling();
                    if (child != null && (child.getType() == CPPTokenTypes.SEMICOLON)) {
                        return true;
                    }
                    return false;
                }
            }
        }
        return false;
    }

    protected boolean isMemberDefinition(AST ast) {
        if (CastUtils.isCast(ast)) {
            return CastUtils.isMemberDefinition(ast);
        }
        AST id = AstUtil.findMethodName(ast);
        return isScopedId(id);
    }

    protected boolean isFunctionOnlySpecialization(AST ast) {
        if (CastUtils.isCast(ast)) {
            return CastUtils.isCastOperatorOnlySpecialization(ast);
        }
        AST mtdName = AstUtil.findMethodName(ast);
        if (mtdName != null && mtdName.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
            AST lessThan = AstUtil.findChildOfType(mtdName, CPPTokenTypes.LESSTHAN);
            if (lessThan == null) {
                return true;
            }
            AST scope = AstUtil.findSiblingOfType(lessThan, CPPTokenTypes.SCOPE);
            if (scope == null) {
                return true;
            }
            return false;
        }
        return mtdName != null && mtdName.getType() == CPPTokenTypes.LESSTHAN;
    }

    public static boolean isScopedId(AST id) {
        if (id == null) {
            return false;
        }
        if (id.getType() == CPPTokenTypes.IDENT) {
            AST scope = id.getNextSibling();
            scope = skipTemplateParameters(scope);
            if (scope != null && scope.getType() == CPPTokenTypes.SCOPE) {
                return true;
            }
        } else if (id.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
            int i = 0;
            AST q = id.getFirstChild();
            while (q != null) {
                if (q.getType() == CPPTokenTypes.SCOPE) {
                    return true;
                }
                q = q.getNextSibling();
            }
        }
        return false;
    }

    public static CsmCompoundStatement findCompoundStatement(AST ast, CsmFile file, CsmFunction owner) {
        for (AST token = ast.getFirstChild(); token != null; token = token.getNextSibling()) {
            switch (token.getType()) {
                case CPPTokenTypes.CSM_COMPOUND_STATEMENT:
                    return CompoundStatementImpl.create(token, file, owner);
                case CPPTokenTypes.CSM_COMPOUND_STATEMENT_LAZY:
                    return LazyCompoundStatementImpl.create(token, file, owner);
                case CPPTokenTypes.CSM_TRY_CATCH_STATEMENT_LAZY:
                    return LazyTryCatchStatementImpl.create(token, file, owner);
            }
        }
        // prevent null bodies
        return EmptyCompoundStatementImpl.create(ast, file, owner);
    }

    public static StatementBase renderStatement(AST ast, CsmFile file, CsmScope scope) {
        return renderStatement(ast, file, scope, null);
    }

    public static StatementBase renderStatement(AST ast, CsmFile file, CsmScope scope, Map<Integer, CsmObject> objects) {
        switch (ast.getType()) {
            case CPPTokenTypes.CSM_LABELED_STATEMENT:
                return LabelImpl.create(ast, file, scope);
            case CPPTokenTypes.CSM_CASE_STATEMENT:
                return CaseStatementImpl.create(ast, file, scope);
            case CPPTokenTypes.CSM_DEFAULT_STATEMENT:
                return UniversalStatement.create(ast, file, CsmStatement.Kind.DEFAULT, scope);
            case CPPTokenTypes.CSM_EXPRESSION_STATEMENT:
                return ExpressionStatementImpl.create(ast, file, scope);
            case CPPTokenTypes.CSM_CLASS_DECLARATION:
            case CPPTokenTypes.CSM_ENUM_DECLARATION:
            case CPPTokenTypes.CSM_ENUM_FWD_DECLARATION:
            case CPPTokenTypes.CSM_DECLARATION_STATEMENT:
            case CPPTokenTypes.CSM_GENERIC_DECLARATION:
                if(new AstRenderer((FileImpl) file, null, ((FileImpl) file).getFileLanguage(), ((FileImpl) file).getFileLanguageFlavor(), objects).isExpressionLikeDeclaration(ast, scope)) {
                    return ExpressionStatementImpl.create(ast, file, scope);
                } else {
                    return DeclarationStatementImpl.create(ast, file, scope);
                }
            case CPPTokenTypes.CSM_COMPOUND_STATEMENT:
                return CompoundStatementImpl.create(ast, file, scope);
            case CPPTokenTypes.CSM_IF_STATEMENT:
                return IfStatementImpl.create(ast, file, scope);
            case CPPTokenTypes.CSM_SWITCH_STATEMENT:
                return SwitchStatementImpl.create(ast, file, scope);
            case CPPTokenTypes.CSM_WHILE_STATEMENT:
                return LoopStatementImpl.create(ast, file, false, scope);
            case CPPTokenTypes.CSM_DO_WHILE_STATEMENT:
                return LoopStatementImpl.create(ast, file, true, scope);
            case CPPTokenTypes.CSM_FOR_STATEMENT:
                return ForStatementImpl.create(ast, file, scope);
            case CPPTokenTypes.CSM_GOTO_STATEMENT:
                return GotoStatementImpl.create(ast, file, scope);
            case CPPTokenTypes.CSM_CONTINUE_STATEMENT:
                return UniversalStatement.create(ast, file, CsmStatement.Kind.CONTINUE, scope);
            case CPPTokenTypes.CSM_BREAK_STATEMENT:
                return UniversalStatement.create(ast, file, CsmStatement.Kind.BREAK, scope);
            case CPPTokenTypes.CSM_RETURN_STATEMENT:
                return ReturnStatementImpl.create(ast, file, scope);
            case CPPTokenTypes.CSM_TRY_STATEMENT:
                return TryCatchStatementImpl.create(ast, file, scope, false);
            case CPPTokenTypes.CSM_CATCH_CLAUSE:
                // TODO: isn't it in TryCatch ??
                return UniversalStatement.create(ast, file, CsmStatement.Kind.CATCH, scope);
            case CPPTokenTypes.CSM_THROW_STATEMENT:
                // TODO: throw
                return UniversalStatement.create(ast, file, CsmStatement.Kind.THROW, scope);
            case CPPTokenTypes.CSM_ASM_BLOCK:
                // just ignore
                break;
//            case CPPTokenTypes.SEMICOLON:
//            case CPPTokenTypes.LCURLY:
//            case CPPTokenTypes.RCURLY:
//                break;
//            default:
//                System.out.println("unexpected statement kind="+ast.getType());
//                break;
        }
        return null;
    }

    /**
     * Parser don't use a symbol table, so constructs like
     * a & b;
     * are parsed as if they were declarations.
     * At the moment of rendering, we check whether this is a expression or a declaration
     * @return true if it's a expression, otherwise false (it's a declaration)
     */
    private boolean isExpressionLikeDeclaration(AST ast, CsmScope scope) {
        AST type = ast.getFirstChild();
        if (type != null && type.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND) {
            AST nextToType = type.getNextSibling();
            if(nextToType != null && nextToType.getType() != CPPTokenTypes.CSM_VARIABLE_DECLARATION) {
                AST name = type.getFirstChild();
                if (name != null) {
                    if (isVariableOrFunctionName(name, false, false)) {
                        if (isVariableOrFunctionName(name, true, false)) {
                            return true;
                        }
                        if (isLocalVariableOrFunction(name.getText(), scope)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks local statements for function or variable declaration with name
     */
    private boolean isLocalVariableOrFunction(CharSequence name, CsmScope scope) {
        while(CsmKindUtilities.isStatement(scope)) {
            scope = ((CsmStatement) scope).getScope();
        }
        if(CsmKindUtilities.isFunction(scope)) {
            CsmFunction fun = (CsmFunction) scope;
            for (CsmParameter param : fun.getParameters()) {
                if (param.getQualifiedName().toString().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public ExpressionBase renderExpression(AST ast, CsmScope scope) {
        return isExpression(ast) ? ExpressionsFactory.create(ast, file,/* null,*/ scope) : null;
    }

    public CsmCondition renderCondition(AST ast, CsmScope scope) {
        if (ast != null && ast.getType() == CPPTokenTypes.CSM_CONDITION) {
            AST first = getFirstChildSkipQualifiers(ast);
            if (first != null) {
                int type = first.getType();
                if (isExpression(type)) {
                    return ConditionExpressionImpl.create(first, file, scope);
                } else if (type == CPPTokenTypes.CSM_TYPE_BUILTIN ||
                        type == CPPTokenTypes.CSM_TYPE_COMPOUND ||
                        type == CPPTokenTypes.LITERAL_struct ||
                        type == CPPTokenTypes.LITERAL_class ||
                        type == CPPTokenTypes.LITERAL_union ||
                        type == CPPTokenTypes.LITERAL_enum) {
                    return ConditionDeclarationImpl.create(ast, file, scope);
                }
            }
        }
        return null;
    }

    public static List<CsmExpression> renderConstructorInitializersList(AST ast, CsmScope scope, CsmFile file) {
        ArrayList<CsmExpression> initializers = null;
        for (AST token = ast.getFirstChild(); token != null; token = token.getNextSibling()) {
            if (token.getType() == CPPTokenTypes.CSM_CTOR_INITIALIZER_LIST) {
                for (AST initializerToken = token.getFirstChild(); initializerToken != null; initializerToken = initializerToken.getNextSibling()) {
                    if (initializerToken.getType() == CPPTokenTypes.CSM_CTOR_INITIALIZER) {
                        CsmExpression initializer = ExpressionsFactory.create(initializerToken, file,/* null,*/ scope);
                        if (initializers == null) {
                            initializers = new ArrayList<>();
                        }
                        initializers.add(initializer);
                    }
                }
            }
        }
        if (initializers != null) {
            initializers.trimToSize();
        }
        return initializers;
    }

    public static boolean isExpression(AST ast) {
        return ast != null && isExpression(ast.getType());
    }

    public static boolean isExpression(int tokenType) {
        return CPPTokenTypes.CSM_EXPRESSIONS_START < tokenType &&
                tokenType < CPPTokenTypes.CSM_EXPRESSIONS_END;
    }

    public static boolean isStatement(AST ast) {
        return ast != null && isStatement(ast.getType());
    }

    public static boolean isStatement(int tokenType) {
        return CPPTokenTypes.CSM_STATEMENTS_START < tokenType &&
                tokenType < CPPTokenTypes.CSM_STATEMENTS_END;
    }

    public static AST skipTemplateSibling(AST template) {
        assert template.getType() == CPPTokenTypes.LITERAL_template;
        AST next = template.getNextSibling();
        if (template.getFirstChild() != null) {
            // this is template node
            return next;
        } else {
            // this is plain template literal
            int balance = 0;
            while (next != null) {
                switch (next.getType()) {
                    case CPPTokenTypes.LESSTHAN:
                        balance++;
                        break;
                    case CPPTokenTypes.GREATERTHAN:
                        --balance;
                        if (balance == 0) {
                            return next.getNextSibling();
                        } else if (balance < 0) {
                            return null;
                        }
                        break;
                }
                next = next.getNextSibling();
            }
        }
        return null;
    }
//    public ExpressionBase renderExpression(ExpressionBase parent) {
//
//    }

    public static boolean getClosestNamespaceInfo(CsmScope scope, CsmFile file, FileContent fileContent, int offset,
                                                  MutableObject<CsmNamespace> closestNamespace,
                                                  MutableObject<MutableDeclarationsContainer> closestDefinitionContainer)
    {
        // Find first namespace scope to add elaborated forwards in it
        NamespaceImpl targetScope = findClosestNamespace(scope);

        closestNamespace.value = null;
        closestDefinitionContainer.value = null;

        if (targetScope != null) {
            MutableDeclarationsContainer targetDefinitionContainer;

            if (targetScope.isGlobal()) {
                targetDefinitionContainer = fileContent;
//                targetDefinitionContainer = null;
            } else {
                targetDefinitionContainer = (MutableDeclarationsContainer) getContainingNamespaceDefinition(
                        targetScope.getDefinitions(),
                        file,
                        offset
                );
            }

            closestNamespace.value = targetScope;
            closestDefinitionContainer.value = targetDefinitionContainer;

            return true;
        }

        return false;
    }

    public static CharSequence[] renderQualifiedId(AST qid, List<CsmTemplateParameter> parameters) {
        return renderQualifiedId(qid, parameters, false);
    }

    public static CharSequence[] renderQualifiedId(AST qid, List<CsmTemplateParameter> parameters, boolean includeLastIdent) {
        int cnt = qid.getNumberOfChildren();
        if( cnt >= 1 ) {
            List<CharSequence> l = new ArrayList<>();
            APTStringManager manager = NameCache.getManager();
            StringBuilder id = new StringBuilder(""); // NOI18N
            int level = 0;
            for( AST token = qid.getFirstChild(); token != null; token = token.getNextSibling() ) {
                int type2 = token.getType();
                switch (type2) {
                    case CPPTokenTypes.IDENT:
                        id = new StringBuilder(AstUtil.getText(token));
                        break;
                    case CPPTokenTypes.GREATERTHAN:
                        level--;
                        break;
                    case CPPTokenTypes.LESSTHAN:
                        if (id != null) {
                            TemplateUtils.addSpecializationSuffix(token, id, parameters, true);
                        }
                        level++;
                        break;
                    case CPPTokenTypes.SCOPE:
                        if (id != null && level == 0 && id.length()>0) {
                            l.add(manager.getString(id));
                            id = null;
                        }
                        break;
                    default:
                }
            }
            if (includeLastIdent && id != null && level == 0 && id.length() > 0) {
                l.add(manager.getString(id));
            }
            return l.toArray(new CharSequence[l.size()]);
        }
        return null;
    }
    
    private static AST getVarNameNode(AST token) {
        if (token == null) {
            return null;
        }
        int templateLevel = 0;
        AST id = null, last = null;
        AST child = token.getFirstChild();
        while (child != null) {
            switch (child.getType()) {
                case CPPTokenTypes.IDENT:
                    if (templateLevel == 0) {
                        id = child;
                    }   
                    break;
                case CPPTokenTypes.LESSTHAN:
                    ++templateLevel;
                    break;
                case CPPTokenTypes.GREATERTHAN:
                    --templateLevel;
                    break;
            }
            last = child;
            child = child.getNextSibling();
        }
        return id != null ? id : last;
    }

    private static NamespaceImpl findClosestNamespace(CsmScope scope) {
        while (scope != null && !(scope instanceof NamespaceImpl)) {
            if (scope instanceof CsmScopeElement) {
                scope = ((CsmScopeElement) scope).getScope();
            } else {
                scope = null;
            }
        }
        return (scope instanceof NamespaceImpl) ? (NamespaceImpl) scope : null;
    }

    private static CsmNamespaceDefinition getContainingNamespaceDefinition(Collection<CsmNamespaceDefinition> collection, CsmFile file, int offset) {
        Iterator<CsmNamespaceDefinition> iter = collection.iterator();
        while (iter.hasNext()) {
            CsmNamespaceDefinition ns = iter.next();
            if (file.getAbsolutePath().equals(ns.getContainingFile().getAbsolutePath())) {
                if (ns.getStartOffset() <= offset && ns.getEndOffset() > offset) {
                    return ns;
                }
            }
        }
        return null;
    }

    private static boolean isBeingParsed(CsmFile file) {
        if (true) return false;
        if (file instanceof FileImpl) {
            return ((FileImpl)file).isParsed();
        } else {
            return false;
        }
    }

    public static class FunctionRenderer {

        public static boolean isStatic(AST ast, CsmFile file, FileContent fileContent, CharSequence name) {
            boolean _static = false;
            AST child = ast.getFirstChild();
            if (child != null) {
                _static = child.getType() == CPPTokenTypes.LITERAL_static;
            } else {
                System.err.println("function ast " + ast.getText() + " without childs in file " + file.getAbsolutePath());
            }
            if (!_static) {
                CsmFilter filter = CsmSelect.getFilterBuilder().createNameFilter(name, true, true, false);
                Iterator<CsmFunction> it;
                if (fileContent != null) {
                    it = fileContent.getFileDeclarations().getStaticFunctionDeclarations(filter);
                } else {
                    assert !isBeingParsed(file) : "no passed file content";
                    it = CsmSelect.getStaticFunctions(file, filter);
                }
                while (it.hasNext()) {
                    CsmFunction fun = it.next();
                    if (name.equals(fun.getName())) {
                        // we don't check signature here since file-level statics
                        // is C-style construct
                        _static = true;
                        break;
                    }
                }
            }
            return _static;
        }

        public static FunctionImpl.CV_RL isConst(AST node) {
            FunctionImpl.CV_RL res = new FunctionImpl.CV_RL();
            AST token = node.getFirstChild();
            while( token != null &&  token.getType() != CPPTokenTypes.CSM_QUALIFIED_ID) {
                token = token.getNextSibling();
            }
            while( token != null ) {
                if (AstRenderer.isConstQualifier(token.getType())) {
                    res._const = true;
                } else if (AstRenderer.isVolatileQualifier(token.getType())) {
                    res._volatile = true;
                } else if (token.getType() == CPPTokenTypes.AMPERSAND) { //the & ref-qualifier
                    res._lvalue = true;
                } else if (token.getType() == CPPTokenTypes.AND) { //the && ref-qualifier
                    res._rvalue = true;
                }
                token = token.getNextSibling();
            }
            return res;
        }

        public static CsmScope getScope(CsmScope scope, CsmFile file, boolean _static, boolean definition) {
            // change scope to file for static methods, but only to prevent
            // registration in global namespace
//            if(scope instanceof CsmNamespace) {
            if(CsmBaseUtilities.isGlobalNamespace(scope)) {
                if( !NamespaceImpl.isNamespaceScope(file, definition, _static) ) {
                        scope = file;
                }
            }
            return scope;
        }

        public static CsmType createReturnType(AST node, CsmScope scope, CsmFile file) {
            return createReturnType(node, scope, file, null);
        }
        public static CsmType createReturnType(AST node, CsmScope scope, CsmFile file, Map<Integer, CsmObject> objects) {
            CsmType ret = null;
            boolean planB = false;
            if(objects != null) {
                CsmObject o = objects.get(OffsetableBase.getStartOffset(node));
                if(o instanceof TypeImpl) {
                    ret = (TypeImpl) o;
                } else {
                    planB = true;
                }
            } else {
                planB = true;
            }
            if(planB) {
                AST token = getTypeToken(node.getFirstChild());
                if( token != null ) {
                    AST autoToken = null;
                    if (token.getFirstChild() != null && token.getFirstChild().getType() == CPPTokenTypes.LITERAL_auto) {
                        autoToken = token;
                        token = AstUtil.findSiblingOfType(token.getNextSibling(), CPPTokenTypes.POINTERTO);
                        token = getTypeToken(token);
                    }
                    ret = AstRenderer.renderType(token, file, null, false); // last two params just dummy ones
                    if (ret == null && autoToken != null) {
                        ret = TypeFactory.createSimpleType(
                                BuiltinTypes.getBuiltIn("auto"), // NOI18N
                                file, 
                                OffsetableBase.getStartOffset(autoToken), 
                                OffsetableBase.getEndOffset(autoToken)
                        );
                    }
                }
                if( ret == null ) {
                    ret = TypeFactory.createBuiltinType("int", (AST) null, 0,  null/*getAst().getFirstChild()*/, file); // NOI18N
                }
            }
            return TemplateUtils.checkTemplateType(ret, scope);
        }

        public static FunctionParameterListImpl createParameters(AST ast, CsmScope scope, CsmFile file, FileContent fileContent) {
            FunctionParameterListImpl parameterList = FunctionParameterListImpl.create(file, fileContent, ast, scope);
            return parameterList;
        }

        public static boolean isVoidParameter(AST node) {
            AST ast = findParameterNode(node);
            return AstRenderer.isVoidParameter(ast);
        }

        private static AST findParameterNode(AST node) {
            AST ast = AstUtil.findChildOfType(node, CPPTokenTypes.CSM_PARMLIST);
            if (ast != null) {
                // for K&R-style
                AST ast2 = AstUtil.findSiblingOfType(ast.getNextSibling(), CPPTokenTypes.CSM_KR_PARMLIST);
                if (ast2 != null) {
                    ast = ast2;
                }
            }
            return ast;
        }

        private static AST getTypeToken(AST node) {
            for( AST token = node; token != null; token = token.getNextSibling() ) {
                int type = token.getType();
                switch( type ) {
                    case CPPTokenTypes.CSM_TYPE_BUILTIN:
                    case CPPTokenTypes.CSM_TYPE_ATOMIC:
                    case CPPTokenTypes.CSM_TYPE_COMPOUND:
                    case CPPTokenTypes.LITERAL_typename:
                    case CPPTokenTypes.LITERAL_struct:
                    case CPPTokenTypes.LITERAL_class:
                    case CPPTokenTypes.LITERAL_union:
                    case CPPTokenTypes.LITERAL_enum:
                        return token;
                    default:
                        if( AstRenderer.isCVQualifier(type) ) {
                            return token;
                        }
                }
            }
            return null;
        }
    }
}

