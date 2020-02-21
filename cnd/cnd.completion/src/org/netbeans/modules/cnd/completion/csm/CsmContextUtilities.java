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

package org.netbeans.modules.cnd.completion.csm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.Callable;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmFunctionInstantiation;
import org.netbeans.modules.cnd.api.model.CsmFunctionParameterList;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmInitializerListContainer;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVariableDefinition;
import org.netbeans.modules.cnd.api.model.deep.CsmDeclarationStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmCacheMap;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilterBuilder;
import org.netbeans.modules.cnd.api.model.support.CsmTypes;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmSortUtilities;
import org.netbeans.modules.cnd.completion.cplusplus.CsmFinderFactory;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionSupport;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmFinder;
import org.netbeans.modules.cnd.utils.MutableObject;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.CharSequences;
import org.openide.util.Pair;

/**
 *
 */
public class CsmContextUtilities {
    private static final boolean DEBUG = Boolean.getBoolean("csm.utilities.trace");

    /** Creates a new instance of CsmScopeUtilities */
    private CsmContextUtilities() {
    }

    public static List<CsmDeclaration> findLocalDeclarations(CsmContext context, String strPrefix, boolean match, boolean caseSensitive) {
        return findLocalDeclarations(context, strPrefix, match, caseSensitive, true/*include file locals*/, false/*include function locals*/);
    }

    public static List<CsmDeclaration> findFileLocalVariables(CsmContext context) {
        return findFileLocalVariables(context, "", false, false);
    }

    public static List<CsmDeclaration> findFileLocalVariables(CsmContext context, String strPrefix, boolean match, boolean caseSensitive) {
        return findLocalDeclarations(context, strPrefix, match, caseSensitive, true/*include file locals*/, false/*exclude function locals*/);
    }

    public static List<CsmDeclaration> findFunctionLocalVariables(CsmContext context) {
        List<CsmDeclaration> decls = findFunctionLocalDeclarations(context, "", false, false);
        List<CsmDeclaration> out = new ArrayList<CsmDeclaration>(decls.size());
        for (CsmDeclaration elem : decls) {
            if (CsmKindUtilities.isVariable(elem)) {
                out.add(elem);
            }
        }
        return out;
    }

    public static List<CsmDeclaration> findFunctionLocalDeclarations(CsmContext context, String strPrefix, boolean match, boolean caseSensitive) {
        List<CsmDeclaration> decls = findLocalDeclarations(context, strPrefix, match, caseSensitive, false/*do not include file locals*/, true/*include function locals*/);
        return decls;
    }

    private static final int FILE_LOCAL_MACROS = 0;
    private static final int FILE_PROJECT_LOCAL_MACROS = 1;
    private static final int FILE_LIB_LOCAL_MACROS = 2;
    private static final int PROJECT_MACROS = 3;
    private static final int LIB_MACROS = 4;
    public static List<CsmMacro> findFileLocalMacros(CsmContext context, String strPrefix, boolean match, boolean caseSensitive) {
        return findMacros(context, strPrefix, match, caseSensitive, FILE_LOCAL_MACROS);
    }

    public static List<CsmMacro> findFileIncludedProjectMacros(CsmContext context, String strPrefix, boolean match, boolean caseSensitive) {
        return findMacros(context, strPrefix, match, caseSensitive, FILE_PROJECT_LOCAL_MACROS);
    }

    public static List<CsmMacro> findFileIncludedLibMacros(CsmContext context, String strPrefix, boolean match, boolean caseSensitive) {
        return findMacros(context, strPrefix, match, caseSensitive, FILE_LIB_LOCAL_MACROS);
    }

    public static List<CsmMacro> findProjectMacros(CsmContext context, String strPrefix, boolean match, boolean caseSensitive) {
        return findMacros(context, strPrefix, match, caseSensitive, PROJECT_MACROS);
    }

    public static List<CsmMacro> findLibMacros(CsmContext context, String strPrefix, boolean match, boolean caseSensitive) {
        return findMacros(context, strPrefix, match, caseSensitive, LIB_MACROS);
    }


    private static List<CsmMacro> findMacros(CsmContext context, CharSequence strPrefix,
            boolean match, boolean caseSensitive, int kind) {
        strPrefix = CharSequences.create(strPrefix);
        List<CsmMacro> res = new ArrayList<CsmMacro>();
        for (Iterator<CsmContext.CsmContextEntry> itContext = context.iterator(); itContext.hasNext();) {
            CsmContext.CsmContextEntry entry = itContext.next();
            CsmScope scope = entry.getScope();
            if (CsmKindUtilities.isFile(scope)){
                CsmFile file = (CsmFile)scope;
                switch (kind) {
                    case FILE_LOCAL_MACROS:
                        getFileLocalMacros(file, res, new HashSet<CharSequence>(), strPrefix, match, caseSensitive);
                        break;
                    case FILE_PROJECT_LOCAL_MACROS:
                        gatherProjectIncludedMacros(file, res, false, strPrefix, match, caseSensitive);
                        break;
                    case FILE_LIB_LOCAL_MACROS:
                        gatherLibIncludedMacros(file, res, false, strPrefix, match, caseSensitive);
                        break;
                    case PROJECT_MACROS:
                        gatherProjectIncludedMacros(file, res, true, strPrefix, match, caseSensitive);
                        break;
                    case LIB_MACROS:
                        gatherLibIncludedMacros(file, res, true, strPrefix, match, caseSensitive);
                        break;
                }
            }
        }
        return res;
    }

    private static void getFileLocalMacros(CsmFile file, List<CsmMacro> res, Set<CharSequence> alredyInList,
            CharSequence strPrefix, boolean match, boolean caseSensitive){
        CsmFilter filter = CsmSelect.getFilterBuilder().createNameFilter(strPrefix, match, caseSensitive, false);
        for (Iterator<CsmMacro> itFile = CsmSelect.getMacros(file, filter); itFile.hasNext();) {
            CsmMacro macro = itFile.next();
            //if (macro.getStartOffset() > offsetInScope) {
            //    break;
            //}
            CharSequence name = macro.getName();
            if (!alredyInList.contains(name) && CsmSortUtilities.matchName(name, strPrefix, match, caseSensitive)) {
                res.add(macro);
                alredyInList.add(name);
            }
        }
    }

    private static void gatherProjectIncludedMacros(CsmFile file, List<CsmMacro> res,
            boolean all, CharSequence strPrefix,  boolean match, boolean caseSensitive) {
        CsmProject prj = file.getProject();
        if (!all) {
            gatherIncludeMacros(file, prj, true, new HashSet<CsmFile>(), new HashSet<CharSequence>(), res, strPrefix, match, caseSensitive);
        } else {
            Set<CharSequence> alredyInList = new HashSet<CharSequence>();
            for(Iterator<CsmFile> i = prj.getHeaderFiles().iterator(); i.hasNext();){
                getFileLocalMacros(i.next(), res, alredyInList, strPrefix, match, caseSensitive);
            }
        }
    }

    private static void gatherLibIncludedMacros(CsmFile file, List<CsmMacro> res, boolean all,
            CharSequence strPrefix, boolean match, boolean caseSensitive) {
        CsmProject prj = file.getProject();
        if (!all) {
            gatherIncludeMacros(file, prj, false, new HashSet<CsmFile>(), new HashSet<CharSequence>(), res, strPrefix, match, caseSensitive);
        } else {
            Set<CharSequence> alredyInList = new HashSet<CharSequence>();
            for(Iterator<CsmProject> p = prj.getLibraries().iterator(); p.hasNext();){
                CsmProject lib = p.next();
                for(Iterator<CsmFile> i = lib.getHeaderFiles().iterator(); i.hasNext();){
                    getFileLocalMacros(i.next(), res, alredyInList, strPrefix, match, caseSensitive);
                }
            }

        }
    }

    private static void gatherIncludeMacros(CsmFile file, CsmProject prj, boolean own,
            Set<CsmFile> visitedFiles, Set<CharSequence> alredyInList,
            List<CsmMacro> res, CharSequence strPrefix, boolean match, boolean caseSensitive) {
        if( visitedFiles.contains(file) ) {
            return;
        }
        visitedFiles.add(file);
        for (Iterator<CsmInclude> iter = file.getIncludes().iterator(); iter.hasNext();) {
            CsmInclude inc = iter.next();
            CsmFile incFile = inc.getIncludeFile();
            if( incFile != null ) {
                if (own) {
                    if (incFile.getProject() == prj) {
                        getFileLocalMacros(incFile, res, alredyInList, strPrefix, match, caseSensitive);
                        gatherIncludeMacros(incFile, prj, own, visitedFiles, alredyInList, res, strPrefix, match, caseSensitive);
                    }
                } else {
                    if (incFile.getProject() != prj) {
                        getFileLocalMacros(incFile, res, alredyInList, strPrefix, match, caseSensitive);
                    }
                    gatherIncludeMacros(incFile, prj, own, visitedFiles, alredyInList, res, strPrefix, match, caseSensitive);
                }
            }
        }
    }

    protected static List<CsmDeclaration> findLocalDeclarations(CsmContext context, String strPrefix, boolean match, boolean caseSensitive, boolean includeFileLocal, boolean includeFunctionVars) {
        List<CsmDeclaration> res = new ArrayList<CsmDeclaration>();
        boolean incAny = includeFileLocal || includeFunctionVars;
        assert (incAny) : "at least one must be true";
        boolean incAll = includeFileLocal && includeFunctionVars;
        for (Iterator<CsmContext.CsmContextEntry> it = context.iterator(); it.hasNext() && incAny;) {
            CsmContext.CsmContextEntry entry = it.next();
            boolean include = incAll;
            if (!include) {
                // check if something changed
                if (!includeFileLocal) {
                    assert (includeFunctionVars);
                    // if it wasn't necessary to include all file local variables, but now
                    // we jump in function => mark that from now include all
                    if (CsmKindUtilities.isFunction(entry.getScope())) {
                        incAll = include = true;
                    }
                    if (CsmKindUtilities.isFunctionExplicitInstantiation(entry.getScope())) {
                        incAll = include = true;
                    }
                    if (CsmKindUtilities.isFunctionPointerType(entry.getScope())) {
                        incAll = include = true;
                    }
                } else if (!includeFunctionVars) {
                    assert (includeFileLocal);
                    // we have sorted context entries => if we reached function or class =>
                    // skip function and all others
                    if (CsmKindUtilities.isFunction(entry.getScope()) ||
                            CsmKindUtilities.isClassifier(entry.getScope())) {
                        incAll = incAny = include = false;
                    } else {
                        include = true;
                    }
                }
            }
            if (include) {
                res = addEntryDeclarations(entry, res, context, strPrefix, match, caseSensitive);
            }
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    private static List<CsmDeclaration> addEntryDeclarations(CsmContext.CsmContextEntry entry, List<CsmDeclaration> decls, CsmContext fullContext,
                                                                String strPrefix, boolean match, boolean caseSensitive) {
        List<CsmDeclaration> newList = findEntryDeclarations(entry, fullContext, strPrefix, match, caseSensitive);
        return mergeDeclarations(decls, newList);
    }

    private static List<CsmDeclaration> findEntryDeclarations(CsmContext.CsmContextEntry entry, CsmContext fullContext,
                                                                String strPrefix, boolean match, boolean caseSensitive) {
        assert (entry != null) : "can't work on null entries";
        CsmScope scope = entry.getScope();
        int offsetInScope = entry.getOffset();
        List<CsmDeclaration> resList = new ArrayList<CsmDeclaration>();
        boolean stoppedBeforeFirst = true;
        for (Iterator<CsmScopeElement> it = scope.getScopeElements().iterator(); it.hasNext();) {
            CsmScopeElement scpElem = it.next();
            if (canBreak(offsetInScope, scpElem, fullContext)) {
                break;
            }
            stoppedBeforeFirst = false;
            List<CsmDeclaration> declList = extractDeclarations(fullContext, scpElem, strPrefix, match, caseSensitive);
            resList.addAll(declList);
        }
        if (stoppedBeforeFirst && CsmKindUtilities.isFunction(scope)) {
            // check if in K&R list
            CsmFunctionParameterList paramList = ((CsmFunction)scope).getParameterList();
            if (CsmOffsetUtilities.isInObject(paramList, offsetInScope)) {
                // add all parameters
                for (CsmParameter csmParameter : paramList.getParameters()) {
                    List<CsmDeclaration> declList = extractDeclarations(fullContext, csmParameter, strPrefix, match, caseSensitive);
                    resList.addAll(declList);
                }
            }
        }
        return resList;
    }

    public static CsmFilter createFilter(final CsmDeclaration.Kind[] kinds, final String strPrefix,
            final boolean match, boolean caseSensitive, final boolean returnUnnamedMembers){
        CsmFilter filter = null;
        CsmFilterBuilder builder = CsmSelect.getFilterBuilder();
        if (kinds != null && strPrefix != null){
            filter = builder.createCompoundFilter(
                     builder.createKindFilter(kinds),
                     builder.createNameFilter(strPrefix, match, caseSensitive, returnUnnamedMembers));
        } else if (kinds != null){
            filter = builder.createKindFilter(kinds);
        } else if (strPrefix != null){
            filter = builder.createNameFilter(strPrefix, match, caseSensitive, returnUnnamedMembers);
        }
        return filter;
    }

    public static List<CsmEnumerator> findFileLocalEnumerators(CsmContext context, String strPrefix, boolean match, boolean caseSensitive) {
        List<CsmEnumerator> res = new ArrayList<CsmEnumerator>();
        for (Iterator<CsmContext.CsmContextEntry> itContext = context.iterator(); itContext.hasNext();) {
            CsmContext.CsmContextEntry entry = itContext.next();
            CsmScope scope = entry.getScope();
            int offsetInScope = entry.getOffset();
            if (CsmKindUtilities.isFile(scope)){
                CsmFile file = (CsmFile)scope;
                CsmFilter fileFilter = createFilter(new CsmDeclaration.Kind[] {CsmDeclaration.Kind.ENUM, CsmDeclaration.Kind.NAMESPACE_DEFINITION,
                    CsmDeclaration.Kind.CLASS, CsmDeclaration.Kind.STRUCT},
                                   null, match, caseSensitive, true);
                for (Iterator<CsmOffsetableDeclaration> itFile = CsmSelect.getDeclarations(file, fileFilter); itFile.hasNext();) {
                    CsmDeclaration decl = itFile.next();
                    if (canBreak(offsetInScope, decl, context)) {
                        break;
                    }
                    if (CsmKindUtilities.isEnum(decl)) {
                        CsmEnum en = (CsmEnum)decl;
                        if (!context.isCpp() || !en.isStronglyTyped() && en.getName().length()==0){
                            addEnumerators(res, en, strPrefix, match, caseSensitive);
                        }
                    } else if (CsmKindUtilities.isNamespaceDefinition(decl) && decl.getName().length()==0){
                        CsmNamespaceDefinition ns = (CsmNamespaceDefinition)decl;
                        CsmFilter filter = createFilter(new CsmDeclaration.Kind[] {CsmDeclaration.Kind.ENUM},
                                null, match, caseSensitive, true);
                        for(Iterator<CsmOffsetableDeclaration> i = CsmSelect.getDeclarations(ns, filter); i.hasNext();){
                            CsmDeclaration nsDecl = i.next();
                            if (canBreak(offsetInScope, nsDecl, context)) {
                                break;
                            }
                            if (CsmKindUtilities.isEnum(nsDecl)) {
                                CsmEnum en = (CsmEnum)nsDecl;
                                if (!en.isStronglyTyped() && en.getName().length()==0){
                                    addEnumerators(res, en, strPrefix, match, caseSensitive);
                                }
                            }
                        }
                    } else if (CsmKindUtilities.isClass(decl) &&
                               (!context.isCpp() || decl.getName().length()==0)){
                        CsmClass cls = (CsmClass) decl;
                        for (CsmMember member : cls.getMembers()) {
                            if (canBreak(offsetInScope, member, context)) {
                                break;
                            }
                            if (CsmKindUtilities.isEnum(member)) {
                                CsmEnum en = (CsmEnum)member;
                                if (!context.isCpp() || !en.isStronglyTyped() && en.getName().length()==0){
                                    addEnumerators(res, en, strPrefix, match, caseSensitive);
                                }
                            }
                        }
                    }
                }
            }
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    private static void addEnumerators(List resList, CsmEnum en, String strPrefix, boolean match, boolean caseSensitive){
        for(Iterator<CsmEnumerator> i = en.getEnumerators().iterator(); i.hasNext();){
            CsmNamedElement scpElem = i.next();
            if (CsmSortUtilities.matchName(scpElem.getName(), strPrefix, match, caseSensitive)) {
                resList.add(scpElem);
            }
        }
    }

    private static boolean canBreak(int offsetInScope, CsmScopeElement elem, CsmContext fullContext) {
        // break if element already is in context
        // or element is after offset
        if (elem == null) {
            return false;
        } else if (offsetInScope == CsmContext.CsmContextEntry.WHOLE_SCOPE) {
            return isInContext(fullContext, elem);
        } else if (CsmKindUtilities.isOffsetable(elem)) {
            return ((CsmOffsetable)elem).getStartOffset() >= offsetInScope || isInContext(fullContext, elem);
        }
        return isInContext(fullContext, elem);
    }

    private static List<CsmDeclaration> mergeDeclarations(List<CsmDeclaration> prevScopeDecls, List<CsmDeclaration> newScopeDecls) {
        // new scope elements have priority
        List<CsmDeclaration> res = new ArrayList<CsmDeclaration>();
        if (newScopeDecls != null && newScopeDecls.size() > 0) {
            res.addAll(newScopeDecls);
        }
        if (prevScopeDecls != null && prevScopeDecls.size() > 0) {
            res.addAll(prevScopeDecls);
        }
        return res;
    }

//    public static void updateContextObject(CsmObject obj, CsmContext context) {
//        if (context != null && obj != null) {
//            context.setLastObject(obj);
//        }
//    }

    public static void updateContextObject(CsmObject obj, int offset, CsmContext context) {
        if (context != null && obj != null) {
            context.setLastObject(obj);
        }
    }

//    public static void updateContext(CsmObject obj, CsmContext context) {
//        if (context != null && CsmKindUtilities.isScope(obj)) {
//            context.add((CsmScope)obj);
//        } else if (CsmKindUtilities.isOffsetable(obj)) {
//            updateContextObject(obj, context);
//        }
//    }

    public static void updateContext(CsmObject obj, int offset, CsmContext context) {
        if (context != null) {
            if (CsmKindUtilities.isScope(obj)) {
                context.add((CsmScope)obj, offset);
            } else if (CsmKindUtilities.isOffsetable(obj)) {
                updateContextObject(obj, offset, context);
            }
        }
    }

    private static boolean isInContext(CsmContext context, CsmObject obj) {
        for (ListIterator<CsmContext.CsmContextEntry> it = context.reverseIterator(); it.hasPrevious();) {
            CsmContext.CsmContextEntry elem = it.previous();
            if (obj.equals(elem.getScope())) {
                return true;
            }
        }
        return false;
    }

    private static CsmClassifier getTypeClassifier(CsmContext fullContext, CsmType type) {
        return CsmBaseUtilities.getClassifier(type, fullContext.getFile(), fullContext.getOffset(), true);
    }

    @SuppressWarnings("unchecked")
    private static List<CsmDeclaration> extractDeclarations(CsmContext fullContext, CsmScopeElement scpElem,
                                                        String strPrefix, boolean match, boolean caseSensitive) {
        List list = new ArrayList();
        if (CsmKindUtilities.isDeclaration(scpElem)) {
            if (CsmSortUtilities.matchName((( CsmNamedElement)scpElem).getName(), strPrefix, match, caseSensitive)) {
                boolean add = true;
                // special check for "var args" parameters
                if (CsmKindUtilities.isParamVariable(scpElem)) {
                    add = !((CsmParameter)scpElem).isVarArgs();
                }
                if (add) {
                    list.add(scpElem);
                }
            }
        } else if (CsmKindUtilities.isStatement(scpElem)) {
            CsmStatement.Kind kind = ((CsmStatement)scpElem).getKind();
            if (kind == CsmStatement.Kind.DECLARATION) {
                List<CsmDeclaration> decls = ((CsmDeclarationStatement)scpElem).getDeclarators();
                List<CsmNamedElement> listByName = CsmSortUtilities.filterList(decls, strPrefix, match, caseSensitive);
                list.addAll(listByName);
                for (CsmDeclaration elem : decls) {
                    if (CsmKindUtilities.isTypedef(elem) || CsmKindUtilities.isTypeAlias(elem)) {
                        final CsmType type = ((CsmTypedef)elem).getType();
                        CsmClassifier classifier = getTypeClassifier(fullContext, type);
                        if (CsmOffsetUtilities.isInObject(elem, classifier) && !CsmOffsetUtilities.sameOffsets(elem, classifier)) {
                            elem = classifier;
                        }
                    }
                    if (CsmKindUtilities.isEnum(elem)) {
                        listByName = CsmSortUtilities.filterList(((CsmEnum)elem).getEnumerators(), strPrefix, match, caseSensitive);
                        list.addAll(listByName);
                    } else if (CsmKindUtilities.isUnion(elem) && ((CsmClass)elem).getName().length() == 0) {
                        listByName = CsmSortUtilities.filterList(((CsmClass)elem).getMembers(), strPrefix, match, caseSensitive);
                        list.addAll(listByName);
                    }
                }
            }
        }
        return list;
    }

    private static final Callable<CsmCacheMap> CACHE_INITIALIZER = new Callable<CsmCacheMap>() {

        @Override
        public CsmCacheMap call() {
            return new CsmCacheMap("CsmContextUtilities Cache", 0, 16); // NOI18N
        }
    };
    private static final class ObjectKey {
        private final Object obj;

        public ObjectKey(Object obj) {
            this.obj = obj;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(obj);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ObjectKey other = (ObjectKey) obj;
            return this.obj == other.obj;
        }
    }

    public static CsmClass getClass(CsmContext context, boolean checkFunDefition, boolean inScope) {
        CsmClass clazz = null;

        // Support of common initializers and GCC extension - designated initializers (bug 240016)
        if (CsmKindUtilities.isVariable(context.getLastObject()) && CsmContextUtilities.isInInitializerList(context, context.getOffset())) {
            CsmVariable var = (CsmVariable) context.getLastObject();

            CsmClassifier classifier = CsmBaseUtilities.getOriginalClassifier(var.getType().getClassifier(), var.getContainingFile());

            if (classifier != null) {
                FileObject fObj = var.getContainingFile().getFileObject();
                CsmFinder finder = (fObj != null) ? CsmFinderFactory.getDefault().getFinder(fObj) : null;
                clazz = getContextClassInInitializer(var, classifier, context.getOffset(), finder);
            }
        }

        if (clazz == null) {
            CsmScope enumScope = null;
            for (int i = context.size() - 1; 0 <= i; --i) {
                CsmScope scope = context.get(i).getScope();
                if (CsmKindUtilities.isEnum(scope)) {
                    enumScope = ((CsmEnum)scope).getScope();
                }
                if (CsmKindUtilities.isClass(scope)
                        && (!inScope || CsmOffsetUtilities.isInClassScope((CsmClass)scope, context.getOffset()))) {
                    clazz = (CsmClass)scope;
                    break;
                }
            }
            if (CsmKindUtilities.isClass(enumScope)) {
                clazz = (CsmClass) enumScope;
            }
        }
        if (clazz == null && checkFunDefition) {
            // check if we in one of class's method
            CsmFunction fun = getFunction(context, false);
            clazz = fun == null ? null : CsmBaseUtilities.getFunctionClass(fun);
        }
        if (clazz == null) {
            // IZ #141107 References to parent class field in static field initializers are unresolved
            // for static field definition, take into account (static) class context
            CsmObject last = context.getLastObject();
            if(CsmKindUtilities.isVariableDefinition(last)) {
                CsmVariable decl = ((CsmVariableDefinition) last).getDeclaration();
                if (CsmKindUtilities.isClassMember(decl)) {
                    clazz = ((CsmMember) decl).getContainingClass();
                }
            }
        }
        return clazz;
    }
    
    public static CsmClass getContextClassInInitializer(CsmVariable var, CsmClassifier varCls, int offset, CsmFinder finder) {
        CsmExpression expression = var.getInitialValue();
        if (expression != null) {
            CsmClass result = null;
            CsmCacheMap cache = CsmCacheManager.getClientCache(CsmContextUtilities.class, CACHE_INITIALIZER);
            boolean[] found = new boolean[] { false };
            ObjectKey tsKey = new ObjectKey(var);
            ContextClassInitializerData data = (ContextClassInitializerData)CsmCacheMap.getFromCache(cache, tsKey, found);
            if (data != null || !found[0]) {
                TokenSequence<TokenId> cppts = (data != null) ? data.cppts : null;
                if (cppts == null) {
                    // expression.getText() here is used because we have offset - 
                    // it is bound to the text on screen, not expanded one.
                    CharSequence expressionText = expression.getText();
                    if (!fastCheckCanBeInnerContext(expressionText)) {
                        return null;
                    } else if (!fastCheckCanBeCompoundLiteral(expressionText) && !CsmKindUtilities.isClass(varCls)) {
                        // Variable is not a class and no compund literal in initializer. Context is global.
                        return null;
                    }
                    TokenHierarchy<CharSequence> hi = TokenHierarchy.create(expressionText, CppTokenId.languageCpp());
                    List<TokenSequence<?>> tsList = hi.embeddedTokenSequences(expression.getEndOffset() - expression.getStartOffset(), true);
                    // Go from inner to outer TSes
                    for (int i = tsList.size() - 1; i >= 0; i--) {
                        TokenSequence<?> ts = tsList.get(i);
                        final Language<?> lang = ts.languagePath().innerLanguage();
                        if (CndLexerUtilities.isCppLanguage(lang, false)) {
                            @SuppressWarnings("unchecked") // NOI18N
                            TokenSequence<TokenId> uts = (TokenSequence<TokenId>) ts;
                            cppts = uts;
                        }
                    }
                }
                if (cppts != null) {
                    List<InitPathItem> cachedPathSequence = (data != null) ?  data.lastPathSequence : Collections.<InitPathItem>emptyList();
                    ListIterator<InitPathItem> cachedPathIter = (!cachedPathSequence.isEmpty()) ? 
                        cachedPathSequence.listIterator(cachedPathSequence.size()) 
                        : cachedPathSequence.listIterator();
                    
                    CsmClass contextClass = CsmKindUtilities.isClass(varCls) ? (CsmClass) varCls : null;
                    int contextArrayDepth = var.getType().getArrayDepth();
                    List<InitPathItem> pathSequence = new ArrayList<InitPathItem>(); // for example: { a : { .b= { c : ...

                    cppts.move(offset - expression.getStartOffset());
                    cppts.movePrevious();
                    if (checkValidInitializerIdent(cppts) || (cppts.token() != null && !CppTokenId.IDENTIFIER.equals(cppts.token().id()))) {
                        MutableObject<Boolean> shouldPathsBeMerged = new MutableObject<Boolean>(false);
                        MutableObject<Integer> skipped = new MutableObject<Integer>(0);
                        boolean foundPosition = true;
                        boolean foundCompoundLiteral = false;
                        while (foundPosition && !shouldPathsBeMerged.value && !foundCompoundLiteral) {
                            foundPosition = findInitializerStart(cppts);
                            int startOffset = cppts.offset();
                            Token<TokenId> startIdent = getInitializerIdentToken(cppts);
                            foundPosition |= findUpperInitializer(cppts, cachedPathIter, skipped, shouldPathsBeMerged);
                            if (shouldPathsBeMerged.value) {
                                mergePaths(pathSequence, cachedPathIter, startIdent, skipped.value, startOffset);
                            } else if (foundPosition) {
                                // Check for compound literal:
                                // { .ptr = & (MyClass) {.field = 0} }
                                //             ^
                                Pair<String, Integer> compoundLiteral = getInitializerCompoundLiteral(cppts); 
                                pathSequence.add(0, new InitPathItem(startIdent, compoundLiteral, skipped.value, startOffset));
                                foundCompoundLiteral = (compoundLiteral != null);
                            }
                        }
                        final boolean finishedSuccessfully = shouldPathsBeMerged.value || !cppts.movePrevious() || foundCompoundLiteral;
                        if (finishedSuccessfully && !pathSequence.isEmpty()) {
                            cachedPathSequence = pathSequence;
                            //pathSequence.remove(pathSequence.size() - 1); // remove last because we are providing context for it!
                            result = resolveInitializerContext(pathSequence, contextClass, contextArrayDepth, var, finder);
                        }
                    }
                    
                    if (cache != null) {
                        cache.put(tsKey, CsmCacheMap.toValue(
                            new ContextClassInitializerData(cppts, cachedPathSequence), 
                            Integer.MAX_VALUE
                        ));
                    }
                }
            }
            return result;
        }
        return null;
    }
    
    // = {..., {...}}
    private static boolean fastCheckCanBeInnerContext(CharSequence expression) {
        return expression != null && CharSequenceUtils.indexOf(expression, '{') != -1; // NOI18N
    }
    
    // = {..., (Type) {...}}
    private static boolean fastCheckCanBeCompoundLiteral(CharSequence expression) {
        return expression != null && CharSequenceUtils.indexOf(expression, '(') != -1; // NOI18N
    }
    
    private static boolean canMergePaths(ListIterator<InitPathItem> lastPathIter, int currentOffset) {
        if (lastPathIter.hasPrevious()) {
            InitPathItem lastPathItem = lastPathIter.previous();
            while (lastPathItem != null && lastPathItem.offset >= currentOffset) {
                if (lastPathItem.offset == currentOffset) {
                    // they are the same, move forward to start merging from current item
                    lastPathIter.next();
                    return true;
                } else {
                    // lastPathItem.offset > currentOffset
                    if (lastPathIter.hasPrevious()) {
                        lastPathItem = lastPathIter.previous();
                    } else {
                        lastPathItem = null;
                    }
                }
            }
            if (lastPathItem != null) {
                // just move forward to start from current item next time
                lastPathIter.next();
            }
        }
        return false;
    }
    
    private static void mergePaths(List<InitPathItem> pathSequence, ListIterator<InitPathItem> lastPathIter, Token<TokenId> ident, int skipped, int offset) {
        InitPathItem lastPathItem = lastPathIter.previous();
        pathSequence.add(0, new InitPathItem(ident, lastPathItem.compoundLiteral, lastPathItem.position + skipped, offset));
        while (lastPathIter.hasPrevious()) {
            lastPathItem = lastPathIter.previous();
            pathSequence.add(0, lastPathItem);
        }
    }
    
    private static boolean findInitializerStart(TokenSequence<TokenId> cppts) {
        int level = 0;
        boolean moved = false;
        do {
            if (CppTokenId.LBRACE.equals(cppts.token().id())) {
                --level;
            } else if (CppTokenId.RBRACE.equals(cppts.token().id())) {
                ++level;
            } else if (CppTokenId.LPAREN.equals(cppts.token().id())) {
                --level;
            } else if (CppTokenId.RPAREN.equals(cppts.token().id())) {
                ++level;
            } else if (CppTokenId.LBRACKET.equals(cppts.token().id())) {
                --level;
            } else if (CppTokenId.RBRACKET.equals(cppts.token().id())) {
                ++level;
            } else if (CppTokenId.COMMA.equals(cppts.token().id())) {
                if (level == 0) {
                    break;
                }
            }
        } while (level >= 0 && (moved = cppts.movePrevious()));
        return moved && cppts.token() != null 
            && (CppTokenId.LBRACE.equals(cppts.token().id()) || CppTokenId.COMMA.equals(cppts.token().id()));
    }
    
    private static boolean findUpperInitializer(
        TokenSequence<TokenId> cppts, 
        ListIterator<InitPathItem> lastPathIter, 
        MutableObject<Integer> skippedHolder, 
        MutableObject<Boolean> shouldPathsBeMerged
    ) {
        int level = 0;
        int skipped = 0;
        do {
            if (level == 0) {
                if (canMergePaths(lastPathIter, cppts.offset())) {
                    skippedHolder.value = skipped;
                    shouldPathsBeMerged.value = true;
                    return false;
                }
            }
            if (CppTokenId.LBRACE.equals(cppts.token().id())) {
                --level;
            } else if (CppTokenId.RBRACE.equals(cppts.token().id())) {
                ++level;
            } else if (CppTokenId.LPAREN.equals(cppts.token().id())) {
                --level;
            } else if (CppTokenId.RPAREN.equals(cppts.token().id())) {
                ++level;
            } else if (CppTokenId.LBRACKET.equals(cppts.token().id())) {
                --level;
            } else if (CppTokenId.RBRACKET.equals(cppts.token().id())) {
                ++level;
            } else if (CppTokenId.COMMA.equals(cppts.token().id())) {
                if (level == 0) {
                    ++skipped;
                }
            }
        } while (level >= 0 && cppts.movePrevious());
        skippedHolder.value = skipped;
        if (level < 0) {
            return cppts.token() != null 
                && CppTokenId.LBRACE.equals(cppts.token().id()) 
                && cppts.movePrevious();
        }
        return false;
    }
    
    private static Token<TokenId> getInitializerIdentToken(TokenSequence<TokenId> cppts) {
        int index = cppts.index();
        Token<TokenId> ident = null;
        findToken(
            cppts,
            false,
            false,
            Arrays.asList(CppTokenId.IDENTIFIER),
            CppTokenId.WHITESPACE, CppTokenId.NEW_LINE, CppTokenId.LINE_COMMENT, CppTokenId.BLOCK_COMMENT, CppTokenId.DOT
        );
        if (checkValidInitializerIdent(cppts)) {
            ident = cppts.token();
        }
        cppts.moveIndex(index);
        cppts.moveNext();
        return ident;
    }
    
    private static Pair<String, Integer> getInitializerCompoundLiteral(TokenSequence<TokenId> cppts) {
        int index = cppts.index();
        if (cppts.token() != null && cppts.token().id() == CppTokenId.COMMA) {
            // Compound literal in initializer starts with LBRACE, not with COMMA
            return null;
        }
        Token<TokenId> rParen;
        if (cppts.token() != null && cppts.token().id() == CppTokenId.RPAREN) {
            rParen = cppts.token();
        } else {
            rParen = findToken(
                cppts,
                true,
                false,
                Arrays.asList(CppTokenId.RPAREN),
                CppTokenId.WHITESPACE, CppTokenId.NEW_LINE, CppTokenId.LINE_COMMENT, CppTokenId.BLOCK_COMMENT
            );
        }
        if (rParen != null) {
            int arrayDepth = 0;
            List<Token<TokenId>> nameTokens = new LinkedList<Token<TokenId>>();
            int level = 1;
            while (level > 0 && cppts.movePrevious() && cppts.token() != null) {
                if (CppTokenId.LBRACE.equals(cppts.token().id())) {
                    --level;
                } else if (CppTokenId.RBRACE.equals(cppts.token().id())) {
                    ++level;
                } else if (CppTokenId.LPAREN.equals(cppts.token().id())) {
                    --level;
                } else if (CppTokenId.RPAREN.equals(cppts.token().id())) {
                    ++level;
                } else if (CppTokenId.LBRACKET.equals(cppts.token().id())) {
                    --level;
                    if (level == 1) { // RPAREN is 1
                        ++arrayDepth;
                    }
                } else if (CppTokenId.RBRACKET.equals(cppts.token().id())) {
                    ++level;
                }
                if (level > 0) {
                    if (isTokenOneOf(cppts.token(), CppTokenId.IDENTIFIER, CppTokenId.SCOPE)) {
                        nameTokens.add(0, cppts.token());
                    }
                }
            }
            if (!nameTokens.isEmpty() && isTokenOneOf(cppts.token(), CppTokenId.LPAREN)) {
                StringBuilder sb = new StringBuilder();
                for (Token<TokenId> nameToken : nameTokens) {
                    sb.append(nameToken.text());
                }
                return Pair.of(sb.toString(), arrayDepth);
            }
        }
        cppts.moveIndex(index);
        cppts.moveNext();
        return null;
    }

    private static boolean checkValidInitializerIdent(TokenSequence<TokenId> cppts) {
        // checks if we are at identifier in initializers like:
        // '..., .ident = { ...' or '..., ident : {...'
        if (cppts.token() != null && CppTokenId.IDENTIFIER.equals(cppts.token().id())) {
            boolean leftSeparatorFound = findToken(
                    cppts,
                    true,
                    true,
                    Arrays.asList(CppTokenId.LBRACE, CppTokenId.COMMA),
                    CppTokenId.WHITESPACE, CppTokenId.NEW_LINE, CppTokenId.LINE_COMMENT, CppTokenId.BLOCK_COMMENT, CppTokenId.DOT
            ) != null;

            boolean rightSeparatorFound = findToken(
                    cppts,
                    false,
                    true,
                    Arrays.asList(CppTokenId.COLON, CppTokenId.EQ),
                    CppTokenId.WHITESPACE, CppTokenId.NEW_LINE, CppTokenId.LINE_COMMENT, CppTokenId.BLOCK_COMMENT
            ) != null;

            return leftSeparatorFound && rightSeparatorFound;
        }
        return false;
    }

    private static CsmClass resolveInitializerContext(List<InitPathItem> pathSequence, CsmClass initialContext, int initialArrayDepth, CsmVariable var, CsmFinder finder) {
        CsmClass context = initialContext;
        int contextArrayDepth = initialArrayDepth;
        int index = getStartIndexToResolveInitializerContext(pathSequence);
        for (Iterator<InitPathItem> itemIter = pathSequence.listIterator(index); index < pathSequence.size(); index++) {
            InitPathItem item = itemIter.next();
            // Adjust context if there was compound literal
            if (item.compoundLiteral != null) {
                if (finder != null) {
                    CsmClassifier castedCls = CompletionSupport.getClassFromName(finder, item.compoundLiteral.first(), true);
                    castedCls = CsmBaseUtilities.getOriginalClassifier(castedCls, var.getContainingFile());
                    if (CsmKindUtilities.isClass(castedCls)) {
                        context = (CsmClass) castedCls;
                        contextArrayDepth = item.compoundLiteral.second();
                    } else {
                        context = null; // no class found!
                    }
                } else {
                    context = null; // finder is not provided!
                }
            }
            if (context == null) {
                break; // Error happened! Cannot proceed resolving without context
            }
            // Dig deeper only if this is not the last element
            if (index < pathSequence.size() - 1) {
                CsmClassifier classifier = null;
                int arrayDepth = 0;
                if (item.isIdentBased()) {
                    String fieldName = item.ident.text().toString();
                    for (CsmMember csmMember : context.getMembers()) {
                        if (CsmKindUtilities.isField(csmMember) && fieldName.equals(csmMember.getName().toString())) {
                            CsmType fieldType = ((CsmField)csmMember).getType();
                            if (fieldType != null) {
                                classifier = CsmBaseUtilities.getOriginalClassifier(fieldType.getClassifier(), fieldType.getContainingFile());
                                arrayDepth = fieldType.getArrayDepth(); // TODO: do something like CsmBaseUtilities.isPointer
                                arrayDepth += fieldType.isPointer() ? 1 : 0;
                            }
                            break;
                        }
                    }
                } else {
                    if (contextArrayDepth > 0) {
                        --contextArrayDepth;
                        continue;
                    } else {
                        int counter = 0;
                        Iterator<CsmMember> memberIter = context.getMembers().iterator();
                        while (counter < item.position && memberIter.hasNext()) {
                            memberIter.next();
                            ++counter;
                        }
                        if (memberIter.hasNext()) {
                            CsmMember member = memberIter.next();
                            if (CsmKindUtilities.isField(member)) {
                                CsmType fieldType = ((CsmField) member).getType();
                                if (fieldType != null) {
                                    classifier = CsmBaseUtilities.getOriginalClassifier(fieldType.getClassifier(), fieldType.getContainingFile());
                                    arrayDepth = fieldType.getArrayDepth(); // TODO: do something like CsmBaseUtilities.isPointer
                                    arrayDepth += fieldType.isPointer() ? 1 : 0;
                                }
                            }
                        }
                    }
                }
                if (CsmKindUtilities.isClass(classifier)) {
                    context = (CsmClass) classifier;
                    contextArrayDepth = arrayDepth;
                }
                if (classifier == null) {
                    context = null; // error happened
                    break;
                }
            }
        }
        return context;
    }
    
    private static int getStartIndexToResolveInitializerContext(List<InitPathItem> pathSequence) {
        // Look for the most inner compound literal
        int index = 0;
        int lastCompoundLiteralIndex = 0;
        for (Iterator<InitPathItem> itemIter = pathSequence.iterator(); index < pathSequence.size() - 1; index++) {
            InitPathItem item = itemIter.next();
            if (item.compoundLiteral != null) {
                lastCompoundLiteralIndex = index;
            }
        }
        return lastCompoundLiteralIndex;
    }

    private static Token<TokenId> findToken(TokenSequence<TokenId> ts,
                                            boolean backward,
                                            boolean restoreTs,
                                            List<? extends TokenId> targetTokens,
                                            TokenId ... skipTokens
    ) {
        int offset = ts.offset();

        Token<TokenId> result = null;

        while (backward ? ts.movePrevious() : ts.moveNext())  {
            Token<TokenId> token = ts.token();

            for (TokenId tId : targetTokens) {
                if (tId.equals(token.id())) {
                    result = token;
                    break;
                }
            }

            if (result != null) {
                break;
            }

            boolean skip = false;

            for (TokenId tId : skipTokens) {
                if (tId.equals(token.id())) {
                    skip = true;
                    break;
                }
            }

            if (!skip) {
                break;
            }
        }

        if (restoreTs) {
            ts.move(offset);
            ts.moveNext();
        }

        return result;
    }
    
    private static boolean isTokenOneOf(Token<TokenId> token, TokenId ... tokens) {
        if (token != null) {
            for (TokenId tokId : tokens) {
                if (tokId.equals(token.id())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final class InitPathItem  {
        
        public final Token<TokenId> ident;
        
        public final Pair<String, Integer> compoundLiteral; 
        
        public final int position;
        
        public final int offset;

        public InitPathItem(Token<TokenId> ident, Pair<String, Integer> compoundLiteral, int position, int offset) {
            this.ident = ident;
            this.compoundLiteral = compoundLiteral;
            this.position = position;
            this.offset = offset;
        }
        
        public boolean isIdentBased() {
            return ident != null && ident.text() != null;
        }

        @Override
        public String toString() {
            String posDescriptor = isIdentBased() ? ("[" + ident.text() + ", " + position + "]") // NOI18N
                : "[" + position + "]"; // NOI18N
            if (compoundLiteral != null) {
                return "(" + compoundLiteral.first() + "[" + compoundLiteral.second() + "])" + posDescriptor; // NOI18N
            }
            return posDescriptor;
        }
    }
    
    private static final class ContextClassInitializerData {
        
        public final TokenSequence<TokenId> cppts;
        
        public final List<InitPathItem> lastPathSequence;

        public ContextClassInitializerData(TokenSequence<TokenId> cppts, List<InitPathItem> lastPathSequence) {
            this.cppts = cppts;
            this.lastPathSequence = lastPathSequence;
        }
    }
    
    public static CsmFunction getFunction(CsmContext context, boolean inScope) {
        CsmFunction result = null;
        for (int i = context.size() - 1; 0 <= i; --i) {
            CsmScope scope = context.get(i).getScope();
            int offset = context.getOffset();
//            if (CsmKindUtilities.isClass(scope) && CsmOffsetUtilities.isInClassScope((CsmClass)scope, offset)) {
//                break;
//            } else
            if (CsmKindUtilities.isFunction(scope)) {
                if (!inScope || CsmOffsetUtilities.isInFunctionScope((CsmFunction)scope, offset)) {
                    result = (CsmFunction)scope;
                    if (!CsmKindUtilities.isLambda(scope)) {
                        return result;
                    }
                }
            }
        }
        return result;
    }

    public static CsmFunctionInstantiation getFunctionInstantiation(CsmContext context, boolean inScope) {
        for (int i = context.size() - 1; 0 <= i; --i) {
            CsmScope scope = context.get(i).getScope();
            int offset = context.getOffset();
            if (CsmKindUtilities.isFunctionExplicitInstantiation(scope)
                    && (!inScope || CsmOffsetUtilities.isInObject(scope, offset))) {
                return (CsmFunctionInstantiation)scope;
            }
        }
        return null;
    }

    public static CsmFunctionDefinition getFunctionDefinition(CsmContext context) {
        CsmFunctionDefinition fun = null;
        for (Iterator<CsmContext.CsmContextEntry> it = context.iterator(); it.hasNext();) {
            CsmContext.CsmContextEntry elem = it.next();
            if (CsmKindUtilities.isFunctionDefinition(elem.getScope())) {
                fun = (CsmFunctionDefinition)elem.getScope();
                break;
            }
        }
        return fun;
    }

    public static CsmNamespace getNamespace(CsmContext context) {
        CsmFunction fun = getFunction(context, false);
        CsmNamespace ns;
        if (fun != null) {
            ns = getFunctionNamespace(fun);
        } else {
            CsmClass cls = CsmContextUtilities.getClass(context, false, false);
            ns = cls == null ? null : getClassNamespace(cls);
        }
        if (ns == null) {
            // look for namespace definition in context
            for (ListIterator<CsmContext.CsmContextEntry> it = context.reverseIterator(); it.hasPrevious();) {
                CsmContext.CsmContextEntry elem = it.previous();
                if (CsmKindUtilities.isNamespaceDefinition(elem.getScope())) {
                    ns = ((CsmNamespaceDefinition)elem.getScope()).getNamespace();
                    break;
                }
            }
        }
        return ns;
    }

    private static CsmNamespace getFunctionNamespace(CsmFunction fun) {
        return CsmBaseUtilities.getFunctionNamespace(fun);
    }

    private static CsmNamespace getClassNamespace(CsmClass cls) {
        return CsmBaseUtilities.getClassNamespace(cls);
    }

    public static boolean isInFunctionBodyOrInitializerListOrCastOperatorType(CsmContext context, int offset) {
        return isInFunctionBody(context, offset) ||
               isInInitializerList(context, offset) ||
               isInCastOperatorType(context, offset);
    }

    public static boolean isInCastOperatorType(CsmContext context, int offset) {
        CsmFunctionDefinition funDef = getFunctionDefinition(context);
        if (funDef != null && CsmKindUtilities.isCastOperator(funDef)) {
            return CsmOffsetUtilities.isInObject(funDef.getReturnType(), offset);
        }
        return false;
    }

    public static boolean isInFunctionBody(CsmContext context, int offset) {
        CsmFunctionDefinition funDef = getFunctionDefinition(context);
        return (funDef == null) ? false : CsmOffsetUtilities.isInObject(funDef.getBody(), offset);
    }

    public static boolean isInInitializerList(CsmContext context, int offset) {
        CsmFunction f = getFunction(context, false);
        if (CsmKindUtilities.isConstructor(f)) {
            for (CsmExpression izer : ((CsmInitializerListContainer) f).getInitializerList()) {
                if (CsmOffsetUtilities.isInObject(izer, offset)) {
                    return true;
                }
            }
        }
        if (CsmKindUtilities.isVariable(context.getLastObject())) {
            CsmVariable var = (CsmVariable) context.getLastObject();
            CsmExpression initialValue = var.getInitialValue();
            if (initialValue != null && CsmOffsetUtilities.isInObject(initialValue, offset)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInFunction(CsmContext context, int offset) {
        CsmFunction fun = getFunction(context, true);
        return fun != null;
    }

    public static boolean isInFunctionInstantiation(CsmContext context, int offset) {
        CsmFunctionInstantiation fi = getFunctionInstantiation(context, true);
        return fi != null;
    }

    public static boolean isInSimpleType(CsmContext context, int offset) {
        CsmType type = extractLastType(context, offset);
        // in instantianiton and decltype everything is possible
        return (type != null) &&
                !type.isInstantiation() &&
                !checkDecltype(type) &&
                !CsmKindUtilities.isFunctionPointerType(type) &&
                CsmOffsetUtilities.isInObject(type, offset);
    }

    public static boolean isInForwardDeclaration(CsmContext context, int offset) {
        CsmObject last = context.getLastObject();
        return CsmKindUtilities.isClassForwardDeclaration(last);
    }

    public static boolean isInType(CsmContext context, int offset) {
        CsmType type = extractLastType(context, offset);
        return (type != null) && CsmOffsetUtilities.isInObject(type, offset);
    }


    private static CsmType extractLastType(CsmContext context, int offset) {
        CsmObject last = context.getLastObject();
        CsmType type = null;
        if (CsmKindUtilities.isTypedef(last) || CsmKindUtilities.isTypeAlias(last)) {
            type = ((CsmTypedef)last).getType();
        } else if (CsmKindUtilities.isType(last)) {
            type = (CsmType) last;
        }
        return type;
    }

    /**
     * @param type
     * @return true if type is based on decltype
     */
    static boolean checkDecltype(CsmType type) {
        final CharSequence classifierText = type.getClassifierText();
        CharSequence aliases[] = CsmTypes.getDecltypeAliases();
        boolean checlFullName = false;
        for (CharSequence alias : aliases) {
            if (CharSequenceUtils.indexOf(classifierText, alias) >= 0) {
                checlFullName = true;
                break;
            }
        }
        if (checlFullName) { // NOI18N
            String fullName = classifierText.toString();
            String nameParts[] = fullName.split("::"); // NOI18N
            for (String part : nameParts) {
                if (CsmTypes.isDecltype(part)) {
                    return true;
                }
            }
        }
        return false;
    }
}
