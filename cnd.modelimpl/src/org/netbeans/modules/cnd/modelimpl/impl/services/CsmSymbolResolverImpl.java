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

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmOverloadingResolver;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.modelimpl.csm.TypeFactory;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.impl.services.evaluator.VariableProvider;
import org.netbeans.modules.cnd.modelimpl.parser.CPPParserEx;
import org.netbeans.modules.cnd.modelimpl.parser.FakeAST;
import org.netbeans.modules.cnd.modelimpl.parser.OffsetableAST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.spi.model.services.CsmSymbolResolverImplementation;
import org.openide.util.CharSequences;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.spi.model.services.CsmSymbolResolverImplementation.class)
public class CsmSymbolResolverImpl implements CsmSymbolResolverImplementation {

    private static final String LT = "<"; // NOI18N
    
    private static final String GT = ">"; // NOI18N
    
    private static final Logger LOG = Logger.getLogger(VariableProvider.class.getSimpleName());

    @Override
    public Collection<CsmOffsetable> resolveSymbol(NativeProject project, CharSequence declText) {
        CsmProject cndProject = CsmModelAccessor.getModel().getProject(project);
        if (cndProject != null) {
            cndProject.waitParse();
            return resolveSymbol(cndProject, declText);
        } 
        return Collections.emptyList();
    }

    @Override
    public Collection<CsmOffsetable> resolveSymbol(CsmProject project, CharSequence declText) {
        try {
            CsmCacheManager.enter();
            AST ast = tryParseQualifiedId(declText);
            if (ast != null) {
                // Simple case - declText is just a qualified id
                return resolveQualifiedId(project, ast);
            } else {
                ast = tryParseFunctionSignature(declText);                
                if (ast != null) {
                    // More complex case - declText is non-template function signature
                    return resolveFunction(project, ast, false);
                } else {
                    ast = tryParseDeclaration(declText);
                    if (ast != null) {
                        // The most complex case - declText should be template function signature.
                        switch (ast.getType()) {
                            case CPPTokenTypes.CSM_FUNCTION_LIKE_VARIABLE_TEMPLATE_DECLARATION:
                            case CPPTokenTypes.CSM_FUNCTION_LIKE_VARIABLE_DECLARATION:
                            case CPPTokenTypes.CSM_FUNCTION_RET_FUN_DECLARATION:
                            case CPPTokenTypes.CSM_FUNCTION_DECLARATION:
                                return resolveFunction(project, ast, true);

                            case CPPTokenTypes.CSM_FUNCTION_TEMPLATE_DECLARATION:
                                // Not allowed in signature (declText contains template literal)
                                return resolveFunction(project, ast, true);
                                
                            case CPPTokenTypes.CSM_TEMPLATE_EXPLICIT_SPECIALIZATION:
                                // Not allowed in signature (declText contains template literal)
                                if (!AstRenderer.isClassSpecialization(ast) && !AstRenderer.isClassExplicitInstantiation(ast)) {
                                    return resolveFunction(project, ast, true);
                                }
                                return Collections.emptyList();
                        }               
                    }
                }
            }
        } catch (Exception ex) {
            LOG.warning(ex.getMessage());
        } finally {
            CsmCacheManager.leave();
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<CsmOffsetable> resolveGlobalFunction(NativeProject project, CharSequence functionName) {
        CsmProject cndProject = CsmModelAccessor.getModel().getProject(project);
        if (cndProject != null) {
            try {
                CsmCacheManager.enter();
                List<CsmOffsetable> candidates = new ArrayList<>();
                CsmSelect.CsmFilter filter = CsmSelect.getFilterBuilder().createCompoundFilter(
                         CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.FUNCTION_DEFINITION),
                         CsmSelect.getFilterBuilder().createNameFilter(functionName, true, true, false)
                );               
                Iterator<CsmOffsetableDeclaration> iter = CsmSelect.getDeclarations(cndProject.getGlobalNamespace(), filter);
                fillFromDecls(candidates, iter, FunctionsAcceptor.INSTANCE);
                return candidates;
            } catch (Exception ex) {
                LOG.warning(ex.getMessage());
            } finally {
                CsmCacheManager.leave();
            }
        }
        return Collections.emptyList();
    }
    
    private Collection<CsmOffsetable> resolveFunction(CsmProject project, AST ast, boolean template) {
        AST funNameAst = AstUtil.findMethodName(ast);
        if (funNameAst != null) {
//            CharSequence qualifiedName = funNameAst.getText();
            CharSequence qualifiedName[] = AstRenderer.renderQualifiedId(funNameAst, null, true);
            
            List<CsmObject> resolvedContext = new ArrayList<>();
            resolveContext(project, qualifiedName, resolvedContext);

            List<CsmFunction> candidates = new ArrayList<>();
            CsmSelect.CsmFilter filter = CsmSelect.getFilterBuilder().createCompoundFilter(
                     CsmSelect.getFilterBuilder().createKindFilter(
                         CsmDeclaration.Kind.FUNCTION,
                         CsmDeclaration.Kind.FUNCTION_DEFINITION,
                         CsmDeclaration.Kind.FUNCTION_INSTANTIATION
                     ),
                     CsmSelect.getFilterBuilder().createNameFilter(
                         hasTemplateSuffix(qualifiedName[qualifiedName.length - 1]) ? trimTemplateSuffix(qualifiedName[qualifiedName.length - 1]) : qualifiedName[qualifiedName.length - 1], 
                         true, 
                         true, 
                         false
                     )
            );               
            
            // DeclarationAcceptor always set to all functions acceptor because on Solaris,
            // nm returns signatures with return type for usual functions
            DeclarationAcceptor acceptor = FunctionsAcceptor.INSTANCE;/*template ? TemplateFunctionsAcceptor.INSTANCE : NonTemplateFunctionsAcceptor.INSTANCE;*/
            
            for (CsmObject context : resolvedContext) {
                if (CsmKindUtilities.isNamespace(context)) {
                    CsmNamespace ns = (CsmNamespace) context;
                    Iterator<CsmOffsetableDeclaration> iter = CsmSelect.getDeclarations(ns, filter);
                    fillFromDecls(candidates, iter, acceptor);
                } else if (CsmKindUtilities.isClass(context)) {
                    CsmClass cls = (CsmClass) context;
                    fillFromDecls(candidates, CsmSelect.getClassMembers(cls, filter), acceptor);
                }
            }
            //Iterator<CsmFunction> funIter = CsmSelect.getFunctions(project, concat(qualifiedName, APTUtils.SCOPE));
            return fillFromDecls(filterFunctions(ast, funNameAst, candidates.iterator()), null);
        }
        return Collections.emptyList();
    }
    
    private Collection<CsmOffsetable> resolveQualifiedId(CsmProject project, AST qualNameNode) {        
        CharSequence qualifiedId[] = AstRenderer.renderQualifiedId(qualNameNode, null, true);
        
        List<CsmObject> resolvedContext = new ArrayList<>();
        resolveContext(project, qualifiedId, resolvedContext);
        
        List<CsmOffsetable> candidates = new ArrayList<>();
        CsmSelect.CsmFilter filter = CsmSelect.getFilterBuilder().createCompoundFilter(
                 CsmSelect.getFilterBuilder().createKindFilter(
                     CsmDeclaration.Kind.VARIABLE,
                     CsmDeclaration.Kind.FUNCTION,
                     CsmDeclaration.Kind.FUNCTION_DEFINITION,
                     CsmDeclaration.Kind.FUNCTION_INSTANTIATION,
                     CsmDeclaration.Kind.CLASS,
                     CsmDeclaration.Kind.STRUCT,
                     CsmDeclaration.Kind.TYPEDEF,
                     CsmDeclaration.Kind.TYPEALIAS                                             
                 ),
                 CsmSelect.getFilterBuilder().createNameFilter(qualifiedId[qualifiedId.length - 1], true, true, false)
        );                                
        for (CsmObject context : resolvedContext) {
            if (CsmKindUtilities.isNamespace(context)) {
                CsmNamespace ns = (CsmNamespace) context;
                fillFromDecls(candidates, CsmSelect.getDeclarations(ns, filter), null);
            } else if (CsmKindUtilities.isClass(context)) {
                CsmClass cls = (CsmClass) context;
                fillFromDecls(candidates, CsmSelect.getClassMembers(cls, filter), null);
            }
        }
        return candidates;
    }
    
    private Collection<CsmFunction> filterFunctions(AST funAst, AST funNameAst, Iterator<CsmFunction> candidates) {
        if (candidates.hasNext()) {
            Collection<AST> paramsAsts = getFunctionParamsAsts(funAst, funNameAst);    
            List<CsmFunction> filteredByParamNumber = filterFunctionsByParamNumber(paramsAsts, candidates);
            return filterFunctionsByParamTypes(paramsAsts, filteredByParamNumber);
        }        
        return Collections.emptyList();
    }
    
    private List<CsmFunction> filterFunctionsByParamNumber(Collection<AST> paramsAsts, Iterator<CsmFunction> candidates) {
        List<CsmFunction> filteredByParamNumber = new ArrayList<>();
        while (candidates.hasNext()) {
            CsmFunction candidate = candidates.next();
            if (candidate.getParameters().size() == paramsAsts.size()) {
                filteredByParamNumber.add(candidate);
            }
        }        
        return filteredByParamNumber;
    }
    
    private Collection<CsmFunction> filterFunctionsByParamTypes(Collection<AST> paramsAsts, Collection<CsmFunction> candidates) {       
        Map<CsmFunction, List<CsmType>> paramsPerFunction = new IdentityHashMap<>();
        for (CsmFunction candidate : candidates) {
            List<CsmType> parameters = createFunctionParams(paramsAsts, candidate);
            paramsPerFunction.put(candidate, parameters);
        }            
        return CsmOverloadingResolver.resolveOverloading(candidates, null, paramsPerFunction);
    }
    
    private Collection<AST> getFunctionParamsAsts(AST targetFunAst, AST targetFunNameAst) {
        AST lparen = AstUtil.findSiblingOfType(targetFunNameAst, CPPTokenTypes.LPAREN);
        AST rparen = AstUtil.findSiblingOfType(lparen, CPPTokenTypes.RPAREN);
        AST params = lparen;
        while (params != rparen && !(CPPTokenTypes.CSM_PARMLIST == params.getType())) {
            params = params.getNextSibling();
        }
        if (CPPTokenTypes.CSM_PARMLIST == params.getType()) {
            List<AST> parameters = new ArrayList<>();
            AST paramAst = params.getFirstChild();
            while (paramAst != null) {
                if (CPPTokenTypes.CSM_PARAMETER_DECLARATION == paramAst.getType()) {
                    parameters.add(paramAst);
                }
                paramAst = paramAst.getNextSibling();
            }
            return parameters;
        }        
        return Collections.emptyList();
    }
    
    private List<CsmType> createFunctionParams(Collection<AST> targetFunParamsAsts, CsmFunction context) {
        List<CsmType> params = new ArrayList<>();
        for (AST paramAst : targetFunParamsAsts) {
            if (paramAst != null) {
                AST paramTypeStart = paramAst.getFirstChild();
                while (paramTypeStart != null && !AstUtil.isTypeNode(paramTypeStart) && !AstRenderer.isQualifier(paramTypeStart.getType())) {
                    paramTypeStart = paramTypeStart.getNextSibling();
                }
                if (paramTypeStart != null && AstRenderer.isQualifier(paramTypeStart.getType())) {
                    AST typeAst = AstRenderer.getFirstSiblingSkipQualifiers(paramTypeStart);
                    if (!AstUtil.isTypeNode(typeAst)) {
                        paramTypeStart = null;
                    }
                }
                if (paramTypeStart != null) {
                    AST ptrOperator = AstUtil.findSiblingOfType(paramTypeStart, CPPTokenTypes.CSM_PTR_OPERATOR);
                    // TODO: AST has wrong offsets here!
                    CsmType type = TypeFactory.createType(paramTypeStart, context.getContainingFile(), ptrOperator, 0, context.getScope());
                    params.add(type);
                }
            }
        }
        return params;
    }
    
    private void resolveContext(CsmProject project, CharSequence qualifiedName[], Collection<CsmObject> result) {   
        CharSequence[] cnn = qualifiedName;
        if (cnn != null) {
            if (cnn.length > 1) {
                resolveContext(project.getGlobalNamespace(), qualifiedName, 0, result);
            } else if (cnn.length == 1) {
                result.add(project.getGlobalNamespace());
            }
        }
    }
    
    private void resolveContext(CsmNamespace context, CharSequence qualifiedName[], int current, Collection<CsmObject> result) {
        CharSequence[] cnn = qualifiedName;
        if (current >= cnn.length - 1) {
            result.add(context);
            return;
        }        
        CsmSelect.CsmFilter filter = createNamespaceFilter(qualifiedName[current]);        
        Iterator<CsmOffsetableDeclaration> decls = CsmSelect.getDeclarations(context, filter);
        if (!decls.hasNext() && hasTemplateSuffix(qualifiedName[current])) {
            filter = createNamespaceFilter(trimTemplateSuffix(qualifiedName[current]));
            decls = CsmSelect.getDeclarations(context, filter);
        }
        
        handleNamespaceDecls(decls, cnn, current, result);
        
        if (!hasTemplateSuffix(qualifiedName[current])) {            
            Set<CsmNamespace> handledNamespaces = new HashSet<>();
            for (CsmNamespace nested : context.getNestedNamespaces()) {
                if (!handledNamespaces.contains(nested)) {
                    handledNamespaces.add(nested);
                    if (qualifiedName[current].toString().equals(nested.getName().toString())) {
                        resolveContext(nested, qualifiedName, current + 1, result);
                    }
                }
            }
        }
    }
    
    private void resolveContext(CsmClass context, CharSequence qualifiedName[], int current, Collection<CsmObject> result) {
        CharSequence[] cnn = qualifiedName;
        if (current >= cnn.length - 1) {
            result.add(context);
            return;
        }
        CsmSelect.CsmFilter filter = createClassFilter(qualifiedName[current]);
        Iterator<CsmMember> decls = CsmSelect.getClassMembers(context, filter);
        if (!decls.hasNext() && hasTemplateSuffix(qualifiedName[current])) {
            filter = createClassFilter(trimTemplateSuffix(qualifiedName[current]));
            decls = CsmSelect.getClassMembers(context, filter);
        }
        handleClassDecls(decls, cnn, current, result);    
    }    
    
    private void handleNamespaceDecls(Iterator<CsmOffsetableDeclaration> decls, CharSequence qualifiedName[], int current, Collection<CsmObject> result) {
        Set<CsmNamespace> handledNamespaces = new HashSet<>();
        while (decls.hasNext()) {
            CsmOffsetableDeclaration decl = decls.next();
            if (CsmKindUtilities.isNamespaceAlias(decl)) {
                CsmNamespace ns = ((CsmNamespaceAlias) decl).getReferencedNamespace();
                if (!handledNamespaces.contains(ns)) {
                    handledNamespaces.add(ns);
                    resolveContext(ns, qualifiedName, current + 1, result);
                }                
            } else if (CsmKindUtilities.isClass(decl)) {
                resolveContext((CsmClass) decl, qualifiedName, current + 1, result);
            } else if (CsmKindUtilities.isTypedefOrTypeAlias(decl)) {
                CsmTypedef typedef = (CsmTypedef) decl;
                CsmClassifier cls = CsmBaseUtilities.getOriginalClassifier(typedef, typedef.getContainingFile());
                if (CsmKindUtilities.isClass(cls)) {
                    resolveContext((CsmClass) cls, qualifiedName, current + 1, result);
                }
            }
        }
    }
    
    private void handleClassDecls(Iterator<CsmMember> decls, CharSequence qualifiedName[], int current, Collection<CsmObject> result) {
        while (decls.hasNext()) {
            CsmMember member = decls.next();
            if (CsmKindUtilities.isClass(member)) {
                resolveContext((CsmClass) member, qualifiedName, current + 1, result);
            } else if (CsmKindUtilities.isTypedefOrTypeAlias(member)) {
                CsmTypedef typedef = (CsmTypedef) member;
                CsmClassifier cls = CsmBaseUtilities.getOriginalClassifier(typedef, typedef.getContainingFile());
                if (CsmKindUtilities.isClass(cls)) {
                    resolveContext((CsmClass) cls, qualifiedName, current + 1, result);
                }
            }
        }        
    }
    
    private static <T extends CsmOffsetable> List<T> fillFromDecls(Iterable<? extends CsmObject> decls, DeclarationAcceptor acceptor) {
        return fillFromDecls(decls.iterator(), acceptor);
    }
    
    private static <T extends CsmOffsetable> List<T> fillFromDecls(Iterator<? extends CsmObject> decls, DeclarationAcceptor acceptor) {
        List<T> result = new ArrayList<>();
        while (decls.hasNext()) {
            CsmObject decl = decls.next();
            if (acceptor == null || acceptor.accept(decl)) {
                result.add((T) decl);
            }
        }
        return result;
    }
    
    private static <T extends CsmOffsetable> void fillFromDecls(List<T> list, Iterable<? extends CsmObject> decls, DeclarationAcceptor acceptor) {
        fillFromDecls(list, decls.iterator(), acceptor);
    }    
    
    private static <T extends CsmOffsetable> void fillFromDecls(List<T> list, Iterator<? extends CsmObject> decls, DeclarationAcceptor acceptor) {
        while (decls.hasNext()) {
            CsmObject decl = decls.next();
            if (acceptor == null || acceptor.accept(decl)) {
                list.add((T) decl);
            }
        }
    }
    
    private static AST tryParseQualifiedId(CharSequence sequence) {
        String trimmedSequence = sequence.toString().trim();
        CPPParserEx parser = createParser(trimmedSequence);
        if (parser != null) {
            parser.qualified_id();
            if (!parser.matchError && parser.getAST() != null) {
                AST lastChild = AstUtil.getLastNonEOFChildRecursively(parser.getAST());
                if (lastChild instanceof OffsetableAST) {
                    OffsetableAST offsetableAst = (OffsetableAST) lastChild;
                    if (offsetableAst.getEndOffset() == trimmedSequence.length()) {
                        return parser.getAST();
                    }
                }
            }
        }
        return null;
    }
    
    private static AST tryParseFunctionSignature(CharSequence sequence) {
        String trimmedSequence = sequence.toString().trim();
        CPPParserEx parser = createParser(trimmedSequence);
        if (parser != null) {
            parser.function_declarator(false, false, false);
            if (!parser.matchError && parser.getAST() != null) {
                AST signatureAst = new FakeAST();
                signatureAst.setType(CPPTokenTypes.CSM_FUNCTION_DECLARATION);
                signatureAst.addChild(parser.getAST());
                AST lastChild = AstUtil.getLastNonEOFChildRecursively(signatureAst);
                if (lastChild instanceof OffsetableAST) {
                    OffsetableAST offsetableAst = (OffsetableAST) lastChild;
                    if (offsetableAst.getEndOffset() == trimmedSequence.length()) {
                        return signatureAst;
                    }
                }
            }
        }
        return null;
    }    
    
    private static AST tryParseDeclaration(CharSequence sequence) {
        CPPParserEx parser = createParser(sequence);
        if (parser != null) {
            parser.external_declaration();
            if (!parser.matchError) {
                return parser.getAST();
            }
        }
        return null;
    }    
    
    private static CPPParserEx createParser(CharSequence sequence) {
        TokenStream ts = APTTokenStreamBuilder.buildTokenStream(sequence.toString(), APTFile.Kind.C_CPP);
        if (ts != null) {
            int flags = CPPParserEx.CPP_CPLUSPLUS;
            flags |= CPPParserEx.CPP_SUPPRESS_ERRORS;            
            APTLanguageFilter langFilter = APTLanguageSupport.getInstance().getFilter(APTLanguageSupport.GNU_CPP, APTLanguageSupport.FLAVOR_UNKNOWN);
            return CPPParserEx.getInstance("In_memory_parse", langFilter.getFilteredStream(ts), flags); // NOI18N
        }
        return null;
    }
    
    private static String concat(CharSequence charSequences[], CharSequence separator) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (CharSequence cs : charSequences) {
            if (first) {
                first = false;
            } else {
                sb.append(separator);
            }
            sb.append(cs);
        }
        return sb.toString();
    }
    
    private static boolean hasTemplateSuffix(CharSequence qualNamePart) {
        return CharSequences.indexOf(qualNamePart, GT) > CharSequences.indexOf(qualNamePart, LT);
    }
    
    private static CharSequence trimTemplateSuffix(CharSequence qualNamePart) {
        return qualNamePart.subSequence(0, CharSequences.indexOf(qualNamePart, LT));
    }
    
    private static CsmSelect.CsmFilter createNamespaceFilter(CharSequence qualNamePart) {
        return CsmSelect.getFilterBuilder().createCompoundFilter(
                 CsmSelect.getFilterBuilder().createKindFilter(
                     CsmDeclaration.Kind.CLASS, 
                     CsmDeclaration.Kind.STRUCT
//                     CsmDeclaration.Kind.TYPEDEF,
//                     CsmDeclaration.Kind.TYPEALIAS,
//                     CsmDeclaration.Kind.NAMESPACE_ALIAS
                 ),
                 CsmSelect.getFilterBuilder().createNameFilter(qualNamePart, true, true, false)
        );
    }
    
    private static CsmSelect.CsmFilter createClassFilter(CharSequence qualNamePart) {
        return CsmSelect.getFilterBuilder().createCompoundFilter(
                 CsmSelect.getFilterBuilder().createKindFilter(
                     CsmDeclaration.Kind.CLASS,
                     CsmDeclaration.Kind.STRUCT
//                     CsmDeclaration.Kind.TYPEDEF,
//                     CsmDeclaration.Kind.TYPEALIAS                    
                 ),
                 CsmSelect.getFilterBuilder().createNameFilter(qualNamePart, true, true, false)
        );
    }            
    
    private static void printAST(AST ast) {
        StringBuilder sb = new StringBuilder();
        printAST(sb, ast, 0);
        System.out.println(sb.toString());
    }
    
    private static void printAST(StringBuilder sb, AST ast, int level) {
        if (ast != null) {
            repeat(sb, ' ', level * 2); // NOI18N
            sb.append(ast.getText()).append('\n'); // NOI18N
            printAST(sb, ast.getFirstChild(), level + 1);
            printAST(sb, ast.getNextSibling(), level);
        }
    }
    
    private static void repeat(StringBuilder sb, char character, int times) {
        while (--times >= 0) {
            sb.append(character);
        }
    }

    private static interface DeclarationAcceptor {
        
        boolean accept(CsmObject decl);        
    }
     
    private static class FunctionsAcceptor implements DeclarationAcceptor {
        
        public static final FunctionsAcceptor INSTANCE = new FunctionsAcceptor();

        @Override
        public boolean accept(CsmObject decl) {
            return (decl instanceof CsmFunction);
        }               
    }
    
    private static class TemplateFunctionsAcceptor extends FunctionsAcceptor {
        
        public static final TemplateFunctionsAcceptor INSTANCE = new TemplateFunctionsAcceptor();

        @Override
        public boolean accept(CsmObject decl) {
            return super.accept(decl) && 
                   (decl instanceof CsmTemplate) &&
                   ((CsmTemplate) decl).isTemplate();
        }
    }
    
    private static class NonTemplateFunctionsAcceptor extends FunctionsAcceptor {
        
        public static final NonTemplateFunctionsAcceptor INSTANCE = new NonTemplateFunctionsAcceptor();
        
        @Override
        public boolean accept(CsmObject decl) {
            return super.accept(decl) &&
                   !TemplateFunctionsAcceptor.INSTANCE.accept(decl);
        }        
    }
}
