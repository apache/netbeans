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

import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmInheritanceUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmSortUtilities;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmEnumForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmUsingDeclaration;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmFriendResolver;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilterBuilder;
import org.netbeans.modules.cnd.api.model.services.CsmUsingResolver;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import static org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities.getInlinedNamespaces;
import static org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities.isInlinedNamespace;
import org.netbeans.modules.cnd.completion.impl.xref.FileReferencesContext;
import org.netbeans.modules.cnd.modelutil.ClassifiersAntiLoop;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.util.CharSequences;

/**
 * help class to resolve content of the project
 * if file was passed => additionally collect file specific info
 */
public final class CsmProjectContentResolver {
    private static final String SCOPE = "::";  // NOI18N

    private boolean caseSensitive = false;
    private boolean naturalSort = false;
    private boolean sort = false;
    private final CsmProject project;
    private final CsmFile startFile;
    private final Collection<CsmProject> libs;

    public CsmProjectContentResolver() {
        this(false);
    }

    public CsmProjectContentResolver(boolean caseSensitive) {
        this(caseSensitive, false, false);
    }

    public CsmProjectContentResolver(boolean caseSensitive, boolean naturalSort) {
        this(caseSensitive, true, naturalSort);
    }

    public CsmProjectContentResolver(boolean caseSensitive, boolean needSort, boolean naturalSort) {
        this.caseSensitive = caseSensitive;
        this.naturalSort = naturalSort;
        this.sort = needSort;
        this.libs = null;
        this.project = null;
        this.startFile = null;
    }

    /**
     * Creates a new instance of CsmProjectContentResolver
     * could be used for getting info only from project
     */
    public CsmProjectContentResolver(CsmFile startFile, CsmProject project, boolean caseSensitive, boolean needSort, boolean naturalSort, Collection<CsmProject> libs) {
        this.caseSensitive = caseSensitive;
        this.naturalSort = naturalSort;
        this.sort = needSort;
        this.project = project;
        this.libs = libs;
        this.startFile = startFile;
    }

    public CsmFile getStartFile() {
        return startFile;
    }

    private List<CsmEnumerator> getEnumeratorsFromEnumsEnumeratorsAndTypedefs(List enumsEnumeratorsAndTypedefs, boolean match, String strPrefix, boolean sort) {
        CsmFilter nameFilter = null;
        List<CsmEnumerator> res = new ArrayList<CsmEnumerator>();
        if (enumsEnumeratorsAndTypedefs != null) {
            for (Iterator it = enumsEnumeratorsAndTypedefs.iterator(); it.hasNext();) {
                CsmObject ob = (CsmObject) it.next();
                CsmEnum elemEnum = null;
                if (CsmKindUtilities.isEnumerator(ob)) {
                    CsmEnumerator elem = (CsmEnumerator) ob;
                    if (matchName(elem.getName(), strPrefix, match)) {
                        res.add((CsmEnumerator) ob);
                    }
                } else if (CsmKindUtilities.isEnum(ob)) {
                    elemEnum = (CsmEnum) ob;
                } else if (CsmKindUtilities.isEnumForwardDeclaration(ob)) {
                    elemEnum = ((CsmEnumForwardDeclaration)ob).getCsmEnum();
                } else {
                    // for typedef check whether it defines unnamed enum
                    assert CsmKindUtilities.isTypedef(ob) || CsmKindUtilities.isTypeAlias(ob);
                    CsmTypedef td = (CsmTypedef) ob;
                    CsmType type = td.getType();
                    if (type != null && (!isCpp() || td.isTypeUnnamed())) {
                        CsmClassifier classifier = type.getClassifier();
                        if (CsmKindUtilities.isClass(classifier)) {
                            if (!isCpp()) {
                                // This works only for C
                                for (CsmMember member : ((CsmClass) classifier).getMembers()) {
                                    if (CsmKindUtilities.isEnum(member)) {
                                        elemEnum = (CsmEnum) member;
                                        break;
                                    }    
                                }
                            }
                        } else if (CsmKindUtilities.isEnum(classifier)) {
                            elemEnum = (CsmEnum) classifier;
                        } else if (CsmKindUtilities.isEnumForwardDeclaration(classifier)) {
                            elemEnum = ((CsmEnumForwardDeclaration)classifier).getCsmEnum();
                        }
                    }
                }
                if (elemEnum != null && !elemEnum.isStronglyTyped()) {
                    if (nameFilter == null) {
                        nameFilter = CsmSelect.getFilterBuilder().createNameFilter(strPrefix, match, caseSensitive, false);
                    }
                    for (Iterator<CsmEnumerator> enmtrIter = CsmSelect.getEnumerators(elemEnum, nameFilter); enmtrIter.hasNext();) {
                        CsmEnumerator elem = enmtrIter.next();
                        if (matchName(elem.getName(), strPrefix, match)) {
                            res.add(elem);
                        }
                    }
                }
            }
            if (sort) {
                CsmSortUtilities.sortMembers(res, isCaseSensitive());
            }
        }
        return res;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isNaturalSort() {
        return naturalSort;
    }

    public void setNaturalSort(boolean naturalSort) {
        this.naturalSort = naturalSort;
    }

    public boolean isSortNeeded() {
        return sort;
    }

    public void setSortNeeded(boolean sort) {
        this.sort = sort;
    }

    /** ================= help methods =======================================*/
    public List<CsmVariable> getGlobalVariables(String strPrefix, boolean match) {
        if (project == null) {
            return Collections.emptyList();
        }
        CsmNamespace globNS = project.getGlobalNamespace();
        // add global variables
        List<CsmVariable> res = getNamespaceVariables(globNS, strPrefix, match, false, false);
        if (sort) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }

    public List<CsmFunction> getGlobalFunctions(String strPrefix, boolean match) {
        if (project == null) {
            return Collections.emptyList();
        }
        CsmNamespace globNS = project.getGlobalNamespace();
        List<CsmFunction> res = getNamespaceFunctions(globNS, strPrefix, match, false, false);
        if (sort) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }

    public List<CsmNamespace> getGlobalNamespaces(String strPrefix, boolean match) {
        if (project == null) {
            return Collections.emptyList();
        }
        CsmNamespace globNS = project.getGlobalNamespace();
        List<CsmNamespace> res = getNestedNamespaces(globNS, strPrefix, match, false);
        if (sort) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // help methods to resolve macros
    public List<CsmMacro> getFileLocalMacros(CsmContext context, String strPrefix, boolean match) {
        List<CsmMacro> res = CsmContextUtilities.findFileLocalMacros(context, strPrefix, match, isCaseSensitive());
        if (isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }

    public List<CsmMacro> getFileIncludedProjectMacros(CsmContext context, String strPrefix, boolean match) {
        List<CsmMacro> res = CsmContextUtilities.findFileIncludedProjectMacros(context, strPrefix, match, isCaseSensitive());
        if (isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }

    public List<CsmMacro> getFileIncludeLibMacros(CsmContext context, String strPrefix, boolean match) {
        List<CsmMacro> res = CsmContextUtilities.findFileIncludedLibMacros(context, strPrefix, match, isCaseSensitive());
        if (isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }

    public List<CsmMacro> getProjectMacros(CsmContext context, String strPrefix, boolean match) {
        List<CsmMacro> res = CsmContextUtilities.findProjectMacros(context, strPrefix, match, isCaseSensitive());
        if (isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }

    public List<CsmMacro> getLibMacros(CsmContext context, String strPrefix, boolean match) {
        List<CsmMacro> res = CsmContextUtilities.findLibMacros(context, strPrefix, match, isCaseSensitive());
        if (isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }

    ///////////////////////////////////////////////////////////////////////////////
    // help methods to resolve current project libraries content
    @SuppressWarnings("unchecked")
    public Collection<CsmVariable> getLibVariables(String strPrefix, boolean match) {
        return getLibElements(NS_VARIABLE_FILTER, strPrefix, match, this.isSortNeeded(), false);
    }

    @SuppressWarnings("unchecked")
    public Collection<CsmFunction> getLibFunctions(String strPrefix, boolean match) {
        return getLibElements(NS_FUNCTION_FILTER, strPrefix, match, this.isSortNeeded(), false);
    }

    @SuppressWarnings("unchecked")
    public Collection<CsmClassifier> getLibClassesEnums(String strPrefix, boolean match) {
        return getLibElements(NS_CLASS_ENUM_FILTER, strPrefix, match, this.isSortNeeded(), false);
    }

    @SuppressWarnings("unchecked")
    public Collection<CsmEnumerator> getLibEnumerators(String strPrefix, boolean match, boolean sort) {
        return getLibElements(NS_ENUMERATOR_FILTER, strPrefix, match, this.isSortNeeded(), false);
    }

    @SuppressWarnings("unchecked")
    public Collection<CsmNamespace> getLibNamespaces(String strPrefix, boolean match) {
        return getLibElements(NS_NAMESPACES_FILTER, strPrefix, match, this.isSortNeeded(), false);
    }

    private Collection getLibElements(NsContentResultsFilter filter, String strPrefix, boolean match, boolean sort, boolean searchNested) {
        if (project == null) {
            return Collections.EMPTY_LIST;
        }
        Set<CsmProject> handledLibs = new HashSet<CsmProject>();
        handledLibs.add(project);
        Map<String, CsmObject> res = new HashMap<String, CsmObject>();
        Collection<CsmObject> libElements = new LinkedHashSet<CsmObject>();
        // add libararies elements
        for (CsmProject lib : libs) {
            if (!handledLibs.contains(lib)) {
                handledLibs.add(lib);
                CsmProjectContentResolver libResolver = new CsmProjectContentResolver(this.startFile, lib, isCaseSensitive(), isSortNeeded(), isNaturalSort(), Collections.<CsmProject>emptyList());
                // TODO: now only direct lib is handled and not libraries of libraries of ...
                Collection<? extends CsmObject> results = filter.getResults(libResolver, lib.getGlobalNamespace(), strPrefix, match, searchNested);
                if (match) {
                    libElements.addAll(results);
                } else {
                    res = mergeByFQN(res, results);
                }
            }
        }
        Collection<CsmObject> out;
        if (match) {
            out = libElements;
        } else {
            out = res.values();
        }
        if (sort) {
            List<CsmObject> sorted = new ArrayList<CsmObject>(out);
            CsmSortUtilities.sortMembers(sorted, isNaturalSort(), isCaseSensitive());
            out = sorted;
        }
        return out;
    }

    /**
     * namespace's content filter
     */
    private interface NsContentResultsFilter {

        /**
         * @param resolver resolver
         * @param ns namespace to analyze using resolver
         * @param strPrefix prefix to search elements starting from
         * @param match flag indicating that results must match prefix
         * @param sort flag indicating that results must be sorted
         * @searchNested flag indicating that results must be searched in nested namespaces as well
         * @returns specific results of analyzed namespace
         */
        public Collection<? extends CsmObject> getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match, boolean searchNested);
    }
    private static final NsContentResultsFilter NS_VARIABLE_FILTER = new NsContentResultsFilter() {

        @Override
        public Collection<? extends CsmObject> getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match, boolean searchNested) {
            return resolver.getNamespaceVariables(ns, strPrefix, match, searchNested);
        }
    };
    private static final NsContentResultsFilter NS_FUNCTION_FILTER = new NsContentResultsFilter() {

        @Override
        public Collection<? extends CsmObject> getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match, boolean searchNested) {
            return resolver.getNamespaceFunctions(ns, strPrefix, match, searchNested);
        }
    };
    private static final NsContentResultsFilter NS_CLASS_ENUM_FILTER = new NsContentResultsFilter() {

        @Override
        public Collection<? extends CsmObject> getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match, boolean searchNested) {
            return resolver.getNamespaceClassesEnums(ns, strPrefix, match, searchNested);
        }
    };
    private static final NsContentResultsFilter NS_ENUMERATOR_FILTER = new NsContentResultsFilter() {

        @Override
        public Collection<? extends CsmObject> getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match, boolean searchNested) {
            return resolver.getNamespaceEnumerators(ns, strPrefix, match, searchNested);
        }
    };
    private static final NsContentResultsFilter NS_NAMESPACES_FILTER = new NsContentResultsFilter() {

        @Override
        public Collection<? extends CsmObject> getResults(CsmProjectContentResolver resolver, CsmNamespace ns, String strPrefix, boolean match, boolean searchNested) {
            return resolver.getGlobalNamespaces(strPrefix, match);
        }
    };

    /////////////////////////////////////////////////////////////////////////////////////////
    public List<CsmDeclaration> findFunctionLocalDeclarations(CsmContext context, String strPrefix, boolean match) {
        List<CsmDeclaration> res = CsmContextUtilities.findFunctionLocalDeclarations(context, strPrefix, match, isCaseSensitive());
        if (isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }

    public List<CsmEnumerator> getFileLocalEnumerators(CsmContext context, String strPrefix, boolean match) {
        List<CsmEnumerator> res = CsmContextUtilities.findFileLocalEnumerators(context, strPrefix, match, isCaseSensitive());
        if (isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }

    public List<CsmVariable> getFileLocalVariables(CsmContext context, FileReferencesContext fileReferncesContext, String strPrefix, boolean match, boolean needFileLocalOrDeclFromUnnamedNS) {
        List<CsmVariable> out = new ArrayList<CsmVariable>();
        if (!context.isEmpty()) {
            for (Iterator<CsmContext.CsmContextEntry> it = context.iterator(); it.hasNext();) {
                CsmContext.CsmContextEntry elem = it.next();
                if (CsmKindUtilities.isFile(elem.getScope())) {
                    CsmFile currentFile = (CsmFile) elem.getScope();
                    fillFileLocalVariables(strPrefix, match, currentFile, needFileLocalOrDeclFromUnnamedNS, false, out);
                    List<CsmVariable> cached = null;
                    if (fileReferncesContext != null && !fileReferncesContext.isCleaned() && match) {
                        cached = fileReferncesContext.getFileLocalIncludeVariables(strPrefix);
                    }
                    if (cached != null) {
                        out.addAll(cached);
                    } else {
                        fillFileLocalIncludeVariables(strPrefix, match, currentFile, out);
                    }
                    for (Iterator<CsmContext.CsmContextEntry> it2 = context.iterator(); it2.hasNext();) {
                        CsmContext.CsmContextEntry elem2 = it2.next();
                        if (CsmKindUtilities.isNamespaceDefinition(elem2.getScope())) {
                            CsmNamespaceDefinition nsd = (CsmNamespaceDefinition) elem2.getScope();
                            fillFileLocalIncludeNamespaceVariables(nsd.getNamespace(), strPrefix, match, currentFile, out, needFileLocalOrDeclFromUnnamedNS);
                        }
                    }
                    break;
                }
            }
        }
        return out;
    }

    public List<CsmFunction> getFileLocalFunctions(CsmContext context, String strPrefix, boolean match, boolean needDeclFromUnnamedNS) {
        Map<CharSequence, CsmFunction> out = new HashMap<CharSequence, CsmFunction>();
        if (!context.isEmpty()) {
            for (Iterator<CsmContext.CsmContextEntry> it = context.iterator(); it.hasNext();) {
                CsmContext.CsmContextEntry elem = it.next();
                if (CsmKindUtilities.isFile(elem.getScope())) {
                    CsmFile currentFile = (CsmFile) elem.getScope();
                    fillFileLocalFunctions(strPrefix, match, currentFile, needDeclFromUnnamedNS, false, out);
                    fillFileLocalIncludeFunctions(strPrefix, match, currentFile, out);
                    for (Iterator<CsmContext.CsmContextEntry> it2 = context.iterator(); it2.hasNext();) {
                        CsmContext.CsmContextEntry elem2 = it2.next();
                        if (CsmKindUtilities.isNamespaceDefinition(elem2.getScope())) {
                            List<CsmFunction> funs = new ArrayList<CsmFunction>();
                            CsmNamespaceDefinition nsd = (CsmNamespaceDefinition) elem2.getScope();
                            CsmNamespace namespace = nsd.getNamespace();
                            // namespace can be null, for example in fortran - since ProgramImpl implements CsmNamespaceDefinition
                            // and moreover it returns kind CsmDeclaration.Kind.NAMESPACE_DEFINITION
                            // The latter looks strange, but now is 2 days to 8.1 code freeze, so changing this kind is too risky,
                            // while checking for null is not :)
                            if (namespace != null) {
                                fillFileLocalIncludeNamespaceFunctions(namespace, strPrefix, match, currentFile, funs, needDeclFromUnnamedNS);
                                for (CsmFunction fun : funs) {
                                    out.put(fun.getSignature(), fun);
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
        return new ArrayList<CsmFunction>(out.values());
    }

    private void fillFileLocalFunctions(String strPrefix, boolean match,
            CsmFile file, boolean needDeclFromUnnamedNS, boolean fromUnnamedNamespace,
            Map<CharSequence, CsmFunction> out) {
        CsmDeclaration.Kind[] kinds;
        if (needDeclFromUnnamedNS || fromUnnamedNamespace) {
            kinds = new CsmDeclaration.Kind[]{
                        CsmDeclaration.Kind.FUNCTION,
                        CsmDeclaration.Kind.FUNCTION_DEFINITION,
                        CsmDeclaration.Kind.FUNCTION_LAMBDA,
                        CsmDeclaration.Kind.FUNCTION_FRIEND,
                        CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION,
                        CsmDeclaration.Kind.NAMESPACE_DEFINITION};
        } else {
            kinds = new CsmDeclaration.Kind[]{
                        CsmDeclaration.Kind.FUNCTION,
                        CsmDeclaration.Kind.FUNCTION_DEFINITION,
                        CsmDeclaration.Kind.FUNCTION_LAMBDA,
                        CsmDeclaration.Kind.FUNCTION_FRIEND,
                        CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION};
        }
        CsmFilter filter = CsmContextUtilities.createFilter(kinds,
                strPrefix, match, caseSensitive, fromUnnamedNamespace || needDeclFromUnnamedNS);
        Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDeclarations(file, filter);
        while (it.hasNext()) {
            CsmOffsetableDeclaration decl = it.next();
            if (CsmKindUtilities.isFunction(decl)) {
                CsmFunction fun = (CsmFunction) decl;
                if (fromUnnamedNamespace || CsmBaseUtilities.isFileLocalFunction(fun)) {
                    if (decl.getName().length() != 0 && matchName(decl.getName(), strPrefix, match)) {
                        out.put(fun.getSignature(), fun);
                    }
                }
            } else if (CsmKindUtilities.isNamespaceDefinition(decl)) {
                CsmNamespaceDefinition nsd = (CsmNamespaceDefinition) decl;
                boolean fillFromNamespace = (needDeclFromUnnamedNS && (nsd.getName().length() == 0))
                                            || nsd.getNamespace().isInline();
                if (fillFromNamespace) {
                    // add all declarations from unnamed namespace as well
                    fillFileLocalFunctions(strPrefix, match, (CsmNamespaceDefinition) decl, needDeclFromUnnamedNS, true, out);
                }
            }
        }
    }

    private void fillFileLocalFunctions(String strPrefix, boolean match,
            CsmNamespaceDefinition ns, boolean needDeclFromUnnamedNS, boolean fromUnnamedNamespace,
            Map<CharSequence, CsmFunction> out) {
        CsmDeclaration.Kind[] kinds;
        if (fromUnnamedNamespace || needDeclFromUnnamedNS) {
            kinds = new CsmDeclaration.Kind[]{
                        CsmDeclaration.Kind.FUNCTION,
                        CsmDeclaration.Kind.FUNCTION_DEFINITION,
                        CsmDeclaration.Kind.FUNCTION_LAMBDA,
                        CsmDeclaration.Kind.FUNCTION_FRIEND,
                        CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION,

                        CsmDeclaration.Kind.NAMESPACE_DEFINITION};
        } else {
            kinds = new CsmDeclaration.Kind[]{
                        CsmDeclaration.Kind.FUNCTION,
                        CsmDeclaration.Kind.FUNCTION_DEFINITION,
                        CsmDeclaration.Kind.FUNCTION_LAMBDA,
                        CsmDeclaration.Kind.FUNCTION_FRIEND,
                        CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION};
        }
        CsmFilter filter = CsmContextUtilities.createFilter(kinds,
                strPrefix, match, caseSensitive, fromUnnamedNamespace || needDeclFromUnnamedNS);
        Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDeclarations(ns, filter);
        while (it.hasNext()) {
            CsmOffsetableDeclaration decl = it.next();
            if (CsmKindUtilities.isFunction(decl)) {
                CsmFunction fun = (CsmFunction) decl;
                if (fromUnnamedNamespace || CsmBaseUtilities.isFileLocalFunction(fun)) {
                    if (decl.getName().length() != 0 && matchName(decl.getName(), strPrefix, match)) {
                        out.put(fun.getSignature(), fun);
                    }
                }
            } else if (CsmKindUtilities.isNamespaceDefinition(decl)) {
                CsmNamespaceDefinition nsd = (CsmNamespaceDefinition) decl;
                boolean fillFromNamespace = (needDeclFromUnnamedNS && (nsd.getName().length() == 0))
                                            || nsd.getNamespace().isInline();
                if (fillFromNamespace) {
                    // add all declarations from unnamed namespace as well
                    fillFileLocalFunctions(strPrefix, match, (CsmNamespaceDefinition) decl, needDeclFromUnnamedNS, true, out);
                }
            }
        }
    }

    private void fillFileLocalVariables(String strPrefix, boolean match,
            CsmFile file, boolean needDeclFromUnnamedNS, boolean fromUnnamedNamespace,
            Collection<CsmVariable> out) {
        CsmDeclaration.Kind[] kinds;
        if (fromUnnamedNamespace || needDeclFromUnnamedNS) {
            kinds = new CsmDeclaration.Kind[]{
                        CsmDeclaration.Kind.VARIABLE,
                        CsmDeclaration.Kind.VARIABLE_DEFINITION,
                        CsmDeclaration.Kind.NAMESPACE_DEFINITION};
        } else {
            kinds = new CsmDeclaration.Kind[]{
                        CsmDeclaration.Kind.VARIABLE,
                        CsmDeclaration.Kind.VARIABLE_DEFINITION};
        }
        CsmFilter filter = CsmContextUtilities.createFilter(kinds,
                strPrefix, match, caseSensitive, true);
        Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDeclarations(file, filter);
        fillFileLocalVariables(strPrefix, match, it, needDeclFromUnnamedNS, fromUnnamedNamespace, true, out);
    }

    @SuppressWarnings("unchecked")
    private void fillUnionVariables(String strPrefix, boolean match, CsmClass union, Collection<CsmVariable> out) {
        Iterator<CsmMember> i = CsmSelect.getClassMembers(union,
                CsmSelect.getFilterBuilder().createNameFilter(strPrefix,
                match, caseSensitive, true));
        Collection filtered = CsmSortUtilities.filterList(i, strPrefix, match, caseSensitive);
        out.addAll(filtered);
    }

    private void fillNamespaceVariables(String strPrefix, boolean match,
            CsmNamespaceDefinition ns, boolean needDeclFromUnnamedNS, boolean fromUnnamedNamespace,
            Collection<CsmVariable> out) {
        CsmDeclaration.Kind[] kinds;
        if (fromUnnamedNamespace || needDeclFromUnnamedNS) {
            kinds = new CsmDeclaration.Kind[]{
                        CsmDeclaration.Kind.VARIABLE,
                        CsmDeclaration.Kind.VARIABLE_DEFINITION,
                        CsmDeclaration.Kind.NAMESPACE_DEFINITION};
        } else {
            kinds = new CsmDeclaration.Kind[]{
                        CsmDeclaration.Kind.VARIABLE,
                        CsmDeclaration.Kind.VARIABLE_DEFINITION};
        }
        CsmFilter filter = CsmContextUtilities.createFilter(kinds,
                strPrefix, match, caseSensitive, true);
        Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDeclarations(ns, filter);
        fillFileLocalVariables(strPrefix, match, it, needDeclFromUnnamedNS, fromUnnamedNamespace, false, out);
    }

    @SuppressWarnings("unchecked")
    private void fillFileLocalVariables(String strPrefix, boolean match,
            Iterator<CsmOffsetableDeclaration> it, boolean needDeclFromUnnamedNS, boolean fromUnnamedNamespace,
            boolean anyVariable,
            Collection<CsmVariable> out) {
        while (it.hasNext()) {
            CsmOffsetableDeclaration decl = it.next();
            if (CsmKindUtilities.isVariable(decl)) {
                CharSequence varName = decl.getName();
                if (fromUnnamedNamespace || (anyVariable || CsmKindUtilities.isFileLocalVariable(decl))) {
                    if (varName.length() != 0) {
                        if (matchName(varName, strPrefix, match)) {
                            out.add((CsmVariable) decl);
                        }
                    } else {
                        CsmVariable var = (CsmVariable) decl;
                        CsmType type = var.getType();
                        if (type != null) {
                            CsmClassifier clsfr = type.getClassifier();
                            if (clsfr != null && CsmKindUtilities.isUnion(clsfr)) {
                                fillUnionVariables(strPrefix, match, (CsmClass) clsfr, out);
                            }
                        }
                    }
                }
            } else if (CsmKindUtilities.isNamespaceDefinition(decl)) {
                CsmNamespaceDefinition nsd = (CsmNamespaceDefinition) decl;
                boolean fillFromNamespace = (needDeclFromUnnamedNS && (nsd.getName().length() == 0))
                                            || nsd.getNamespace().isInline();
                if (fillFromNamespace) {
                    // add all declarations from unnamed namespace as well
                    fillNamespaceVariables(strPrefix, match, (CsmNamespaceDefinition) decl, needDeclFromUnnamedNS, true, out);
                }
            }
        }
    }

    private void fillFileLocalIncludeVariables(String strPrefix, boolean match,
            CsmFile file, Collection<CsmVariable> out) {
        CsmDeclaration.Kind[] kinds = new CsmDeclaration.Kind[]{
            CsmDeclaration.Kind.VARIABLE,
            CsmDeclaration.Kind.VARIABLE_DEFINITION};
        CsmFilter filter = CsmContextUtilities.createFilter(kinds,
                strPrefix, match, caseSensitive, false);
        fillFileLocalVariablesByFilter(filter, file, out);
    }

    public static void fillFileLocalVariablesByFilter(CsmFilter filter, CsmFile file, Collection<CsmVariable> out) {
        fillFileLocalIncludeSymbols(new LocalVariablesCollector(out, filter), file, new HashSet<CsmFile>(), true);
    }

    private void fillFileLocalIncludeNamespaceVariables(CsmNamespace ns, String strPrefix, boolean match,
            CsmFile file, Collection<CsmVariable> out, boolean needDeclFromUnnamedNS) {
        if (!ns.isGlobal()) {
            CsmDeclaration.Kind[] kinds = new CsmDeclaration.Kind[]{
                CsmDeclaration.Kind.VARIABLE,
                CsmDeclaration.Kind.VARIABLE_DEFINITION
            };
            Collection<CsmScopeElement> se = new ArrayList<CsmScopeElement>();
            CsmFilter filter = CsmContextUtilities.createFilter(kinds, strPrefix, match, caseSensitive, false);
            getFileLocalIncludeNamespaceMembers(ns, file, filter, se, needDeclFromUnnamedNS);
            List<CsmVariable> vars = new ArrayList<CsmVariable>();
            filterDeclarations(se.iterator(), vars, kinds, strPrefix, match, false);
            vars = filterVariables(vars);
            out.addAll(vars);
        } else {
            // Global ns doesn't have definitions, so search in file instead
            List<CsmVariable> allLocalDecls = new ArrayList<CsmVariable>();
            fillFileLocalVariables(strPrefix, match, file, needDeclFromUnnamedNS, false, allLocalDecls);
            // Unlike getFileLocalIncludeNamespaceMembers (used in first branch),
            // fillFileLocalVariables for some reason returns not only variables without
            // external linkage, so need to filter results before adding to out.
            out.addAll(filterFileLocalStaticVariables(allLocalDecls));
        }
    }
    
    private void fillFileLocalIncludeFunctions(String strPrefix, boolean match,
            CsmFile file, Map<CharSequence, CsmFunction> out) {
        CsmDeclaration.Kind[] kinds = new CsmDeclaration.Kind[]{
            CsmDeclaration.Kind.FUNCTION,
            CsmDeclaration.Kind.FUNCTION_DEFINITION};
        CsmFilter filter = CsmContextUtilities.createFilter(kinds,
                strPrefix, match, caseSensitive, false);
        fillFileLocalFunctionsByFilter(filter, file, out);
    }

    public static void fillFileLocalFunctionsByFilter(CsmFilter filter, CsmFile file, Map<CharSequence, CsmFunction> out) {
        fillFileLocalIncludeSymbols(new LocalFunctionsCollector(out, filter), file, new HashSet<CsmFile>(), true);
    }

    private void fillFileLocalIncludeNamespaceFunctions(CsmNamespace ns, String strPrefix, boolean match,
            CsmFile file, List<CsmFunction> out, boolean needDeclFromUnnamedNS) {
        if (!ns.isGlobal()) {
            CsmDeclaration.Kind kinds[] = {
                CsmDeclaration.Kind.FUNCTION,
                CsmDeclaration.Kind.FUNCTION_DEFINITION,
                CsmDeclaration.Kind.FUNCTION_LAMBDA,
                CsmDeclaration.Kind.FUNCTION_FRIEND,
                CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION
            };
            Collection<CsmScopeElement> se = new ArrayList<CsmScopeElement>();
            CsmFilter filter = CsmContextUtilities.createFilter(kinds, strPrefix, match, caseSensitive, false);
            getFileLocalIncludeNamespaceMembers(ns, file, filter, se, needDeclFromUnnamedNS);

            List<CsmFunction> funs = new ArrayList<CsmFunction>();
            filterDeclarations(se.iterator(), funs, kinds, strPrefix, match, false);
            funs = filterFunctionDefinitions(funs);
            out.addAll(funs);
        } else {
            // Global ns doesn't have definitions, so search in file instead
            Map<CharSequence, CsmFunction> allLocalDecls = new HashMap<CharSequence, CsmFunction>();
            fillFileLocalFunctions(strPrefix, match, file, needDeclFromUnnamedNS, false, allLocalDecls);
            out.addAll(allLocalDecls.values());
        }
    }

    private void getFileLocalIncludeNamespaceMembers(CsmNamespace ns, CsmFile file, CsmFilter scopeFilter, Collection<CsmScopeElement> out, boolean needDeclFromUnnamedNS) {
        CsmFilterBuilder builder = CsmSelect.getFilterBuilder();
        CsmFilter filter = builder.createKindFilter(CsmDeclaration.Kind.NAMESPACE_DEFINITION);
        for (Iterator<CsmOffsetableDeclaration> itFile = CsmSelect.getDeclarations(file, filter); itFile.hasNext();) {
            CsmOffsetableDeclaration decl = itFile.next();
            if (CsmKindUtilities.isNamespaceDefinition(decl)) {
                CsmNamespaceDefinition nsd = (CsmNamespaceDefinition) decl;
                if (nsd.getQualifiedName().equals(ns.getQualifiedName())) {
                    for (Iterator<CsmScopeElement> itNs = CsmSelect.getScopeDeclarations(nsd, scopeFilter); itNs.hasNext();) {
                        out.add(itNs.next());
                    }
                } else if (CharSequenceUtilities.startsWith(ns.getQualifiedName(), nsd.getQualifiedName())) {
                    getFileLocalIncludeNamespaceMembersFromNested(filter, ns.getQualifiedName(), nsd, scopeFilter, out, needDeclFromUnnamedNS);
                }
                if (needDeclFromUnnamedNS && CharSequenceUtilities.startsWith(ns.getQualifiedName(), nsd.getQualifiedName())) {
                    getFileLocalIncludeNamespaceMembersFromNested(filter, "<unnamed>", nsd, scopeFilter, out, needDeclFromUnnamedNS); // NOI18N
                }
            }
        }
    }

    private void getFileLocalIncludeNamespaceMembersFromNested(CsmFilter nsDefinitionFilter, CharSequence nsName, CsmNamespaceDefinition ns, CsmFilter scopeFilter, Collection<CsmScopeElement> out, boolean needDeclFromUnnamedNS) {
        for (Iterator<CsmOffsetableDeclaration> itFile = CsmSelect.getDeclarations(ns, nsDefinitionFilter); itFile.hasNext();) {
            CsmOffsetableDeclaration decl = itFile.next();
            if (CsmKindUtilities.isNamespaceDefinition(decl)) {
                CsmNamespaceDefinition nsd = (CsmNamespaceDefinition) decl;
                if (nsd.getQualifiedName().equals(nsName)) {
                    for (Iterator<CsmScopeElement> itNs = CsmSelect.getScopeDeclarations(nsd, scopeFilter); itNs.hasNext();) {
                        out.add(itNs.next());
                    }
                } else if (nsName.toString().startsWith(nsd.getQualifiedName().toString())) {
                    getFileLocalIncludeNamespaceMembersFromNested(nsDefinitionFilter, nsName, nsd, scopeFilter, out, needDeclFromUnnamedNS);
                }
                if (needDeclFromUnnamedNS && nsName.toString().startsWith(nsd.getQualifiedName().toString())) {
                    getFileLocalIncludeNamespaceMembersFromNested(nsDefinitionFilter, "<unnamed>", nsd, scopeFilter, out, needDeclFromUnnamedNS); // NOI18N
                }
                if ((needDeclFromUnnamedNS && nsd.getName().length() == 0) || nsd.isInline()) {
                    for (Iterator<CsmScopeElement> itNs = CsmSelect.getScopeDeclarations(nsd, scopeFilter); itNs.hasNext();) {
                        out.add(itNs.next());
                    }
                    getFileLocalIncludeNamespaceMembersFromNested(nsDefinitionFilter, nsName, nsd, scopeFilter, out, needDeclFromUnnamedNS);
                }
            }
        }
    }

//    private void getFileLocalIncludeNamespaceMembers(CsmNamespace ns, CsmFile file, int offset,
//            Collection out) {
//        for(CsmNamespaceDefinition nsd : CsmUsingResolver.getDefault().findDirectVisibleNamespaceDefinitions(file, offset, project)) {
//            if (nsd.getQualifiedName().equals(ns.getQualifiedName())) {
//                out.addAll(nsd.getScopeElements());
//            } else if (ns.getQualifiedName().toString().startsWith(nsd.getQualifiedName().toString())) {
//                getFileLocalIncludeNamespaceMembersFromNested(ns.getQualifiedName(), nsd, out);
//            }
//        }
//    }
//
//    private void getFileLocalIncludeNamespaceMembersFromNested(CharSequence nsName, CsmNamespaceDefinition ns, Collection out) {
//        for (CsmOffsetableDeclaration decl : ns.getDeclarations()) {
//            if(CsmKindUtilities.isNamespaceDefinition(decl)) {
//                CsmNamespaceDefinition nsd = (CsmNamespaceDefinition) decl;
//                if (nsd.getQualifiedName().equals(nsName)) {
//                    out.addAll(nsd.getScopeElements());
//                } else if (nsName.toString().startsWith(nsd.getQualifiedName().toString())) {
//                    getFileLocalIncludeNamespaceMembersFromNested(nsName, nsd, out);
//                }
//            }
//        }
//    }
//    public List getLocalDeclarations(CsmContext context, String strPrefix, boolean match) {
//        List res = CsmContextUtilities.findLocalDeclarations(context, strPrefix, match, isCaseSensitive());
//        if (res != null) {
//            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
//        }
//        return res;
//    }
    public List<CsmVariable> getNamespaceVariables(CsmNamespace ns, String strPrefix, boolean match, boolean searchNestedUnnamedNamespaces) {
        return getNamespaceVariables(ns, strPrefix, match, isSortNeeded(), searchNestedUnnamedNamespaces);
    }

    @SuppressWarnings("unchecked")
    private List<CsmVariable> getNamespaceVariables(CsmNamespace ns, String strPrefix, boolean match, boolean sort, boolean searchNestedUnnamedNamespaces) {
        List res = getNamespaceMembers(ns, CsmDeclaration.Kind.VARIABLE, strPrefix, match, searchNestedUnnamedNamespaces, false);
        Collection used;
        if (match) {
            used = CsmUsingResolver.getDefault().findUsedDeclarations(ns,strPrefix);
        } else {
            used = CsmUsingResolver.getDefault().findUsedDeclarations(ns);
        }
        filterDeclarations(used.iterator(), res, new CsmDeclaration.Kind[]{CsmDeclaration.Kind.VARIABLE}, strPrefix, match, false);
        res = filterVariables(res);
        if (sort) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }

    public List<CsmVariable> getFileLocalNamespaceVariables(CsmNamespace ns, CsmFile file, String strPrefix, boolean match) {
        List<CsmVariable> out = new ArrayList<CsmVariable>();
        fillFileLocalIncludeNamespaceVariables(ns, strPrefix, match, file, out, true);
        if (isSortNeeded()) {
            CsmSortUtilities.sortMembers(out, isNaturalSort(), isCaseSensitive());
        }
        return out;
    }

    public List<CsmFunction> getFileLocalNamespaceFunctions(CsmNamespace ns, CsmFile file, String strPrefix, boolean match) {
        List<CsmFunction> out = new ArrayList<CsmFunction>();
        fillFileLocalIncludeNamespaceFunctions(ns, strPrefix, match, file, out, true);
        if (isSortNeeded()) {
            CsmSortUtilities.sortMembers(out, isNaturalSort(), isCaseSensitive());
        }
        return out;
    }

    public List<CsmFunction> getNamespaceFunctions(CsmNamespace ns, String strPrefix, boolean match, boolean searchNestedUnnamedNamespaces) {
        return getNamespaceFunctions(ns, strPrefix, match, isSortNeeded(), searchNestedUnnamedNamespaces);
    }

    @SuppressWarnings("unchecked")
    private List<CsmFunction> getNamespaceFunctions(CsmNamespace ns, String strPrefix, boolean match, boolean sort, boolean searchNestedUnnamedNamespaces) {
        CsmDeclaration.Kind memberKinds[] = {
            CsmDeclaration.Kind.FUNCTION,
            CsmDeclaration.Kind.FUNCTION_DEFINITION,
            CsmDeclaration.Kind.FUNCTION_LAMBDA,
            CsmDeclaration.Kind.FUNCTION_FRIEND,
            CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION
        };
        List res = getNamespaceMembers(ns, memberKinds, strPrefix, match, searchNestedUnnamedNamespaces, false);
        Collection used;
        if (match) {
            used = CsmUsingResolver.getDefault().findUsedDeclarations(ns,strPrefix);
        } else {
            used = CsmUsingResolver.getDefault().findUsedDeclarations(ns);
        }
        filterDeclarations(used.iterator(), res, memberKinds, strPrefix, match, false);
        res = filterFunctionDefinitions(res);
        if (sort) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }

    public List<CsmNamespaceAlias> getNamespaceAliases(CsmNamespace ns, String strPrefix, boolean match, boolean searchNestedUnnamedNamespaces) {
        return getNamespaceAliases(ns, strPrefix, match, isSortNeeded(), searchNestedUnnamedNamespaces);
    }

    @SuppressWarnings("unchecked")
    private List<CsmNamespaceAlias> getNamespaceAliases(CsmNamespace ns, String strPrefix, boolean match, boolean sort, boolean searchNestedUnnamedNamespaces) {
        List res = getNamespaceMembers(ns, CsmDeclaration.Kind.NAMESPACE_ALIAS, strPrefix, match, searchNestedUnnamedNamespaces, false);
        Collection used;
        if (match) {
            used = CsmUsingResolver.getDefault().findUsedDeclarations(ns,strPrefix);
        } else {
            used = CsmUsingResolver.getDefault().findUsedDeclarations(ns);
        }
        filterDeclarations(used.iterator(), res, new CsmDeclaration.Kind[]{CsmDeclaration.Kind.NAMESPACE_ALIAS}, strPrefix, match, false);
        if (sort && res != null) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }

    public List<CsmNamespace> getNestedNamespaces(CsmNamespace ns, String strPrefix, boolean match, boolean searchNestedNamespaces) {
        Map<CharSequence, CsmNamespace> set = getNestedNamespaces(ns, strPrefix, match, searchNestedNamespaces, new HashSet<CsmNamespace>());
        List<CsmNamespace> res;
        if (!set.isEmpty()) {
            res = new ArrayList<CsmNamespace>(set.values());
        } else {
            res = new ArrayList<CsmNamespace>();
        }
        if (sort) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }

    private Map<CharSequence, CsmNamespace> getNestedNamespaces(CsmNamespace ns, String strPrefix, boolean match, boolean searchNestedUnnamedNamespaces, Set<CsmNamespace> handledNS) {
        Map<CharSequence, CsmNamespace> res = new LinkedHashMap<CharSequence, CsmNamespace>(); // order is important
        if(ns != null) {
            handledNS.add(ns);
            CsmProject nsProject = ns.getProject();
            if(nsProject != null) {
                // handle all nested namespaces
                for (CsmProject lib : nsProject.getLibraries()) {
                    CsmNamespace n = lib.findNamespace(ns.getQualifiedName());
                    if (n != null && !handledNS.contains(n)) {
                        res.putAll(getNestedNamespaces(n, strPrefix, match, searchNestedUnnamedNamespaces, handledNS));
                    }
                }
                for (Iterator<CsmNamespace> it = ns.getNestedNamespaces().iterator(); it.hasNext();) {
                    CsmNamespace nestedNs = it.next();
                    // TODO: consider when we add nested namespaces
                    if ((nestedNs.getName().length() != 0) && matchName(nestedNs.getName(), strPrefix, match)) {
                        res.put(nestedNs.getQualifiedName(), nestedNs);
                    }

                    if ((searchNestedUnnamedNamespaces && nestedNs.getName().length() == 0) || nestedNs.isInline()) {
                        res.putAll(getNestedNamespaces(nestedNs, strPrefix, match, searchNestedUnnamedNamespaces, handledNS));
                    }
                }
            }
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    public List<CsmClassifier> getNamespaceClassesEnums(CsmNamespace ns, String strPrefix, boolean match, boolean searchNestedUnnamedNamespaces) {
        CsmDeclaration.Kind classKinds[] = {
            CsmDeclaration.Kind.CLASS,
            CsmDeclaration.Kind.STRUCT,
            CsmDeclaration.Kind.UNION,
            CsmDeclaration.Kind.ENUM,
            CsmDeclaration.Kind.TYPEDEF,
            CsmDeclaration.Kind.TYPEALIAS
        };
        List res = getNamespaceMembers(ns, classKinds, strPrefix, match, searchNestedUnnamedNamespaces, false);
        Collection used;
        if (match) {
            used = CsmUsingResolver.getDefault().findUsedDeclarations(ns,strPrefix);
        } else {
            used = CsmUsingResolver.getDefault().findUsedDeclarations(ns);
        }
        filterDeclarations(used.iterator(), res, classKinds, strPrefix, match, false);
        if (isSortNeeded() && res != null) {
            CsmSortUtilities.sortClasses(res, isCaseSensitive());
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    public List<CsmEnumerator> getNamespaceEnumerators(CsmNamespace ns, String strPrefix, boolean match, boolean searchNestedUnnamedNamespaces) {
        // get all enums and check theirs enumerators
        // also get all typedefs and check whether they define
        // unnamed enum
        CsmDeclaration.Kind classKinds[] = {
            CsmDeclaration.Kind.ENUM,
            CsmDeclaration.Kind.ENUM_FORWARD_DECLARATION,
            CsmDeclaration.Kind.TYPEDEF,
            CsmDeclaration.Kind.TYPEALIAS
        };

        List enumsAndTypedefs = getNamespaceMembers(
                ns,
                classKinds,
                "",
                false,
                new HashSet<CsmNamespace>(),
                searchNestedUnnamedNamespaces,
                true,
                searchNestedUnnamedNamespaces,
                searchNestedUnnamedNamespaces
        );
        Collection used;
        if (match) {
            used = CsmUsingResolver.getDefault().findUsedDeclarations(ns, strPrefix);
        } else {
            used = CsmUsingResolver.getDefault().findUsedDeclarations(ns);
        }
        CsmDeclaration.Kind classAndEnumeratorKinds[] = {
            CsmDeclaration.Kind.ENUM,
            CsmDeclaration.Kind.ENUM_FORWARD_DECLARATION,
            CsmDeclaration.Kind.TYPEDEF,
            CsmDeclaration.Kind.TYPEALIAS,
            CsmDeclaration.Kind.ENUMERATOR
        };
        filterDeclarations(used.iterator(), enumsAndTypedefs, classAndEnumeratorKinds, "", false, true);
        List res = getEnumeratorsFromEnumsEnumeratorsAndTypedefs(enumsAndTypedefs, match, strPrefix, sort);
        return res;
    }

    @SuppressWarnings("unchecked")
    public List<CsmClassifier> getNestedClassifiers(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, String strPrefix, boolean match,
            boolean inspectParentClasses, boolean inspectOuterClasses, boolean returnBaseClasses) {
        CsmDeclaration.Kind memberKinds[] = {
            CsmDeclaration.Kind.TYPEDEF,
            CsmDeclaration.Kind.TYPEALIAS,
            CsmDeclaration.Kind.UNION,
            CsmDeclaration.Kind.STRUCT,
            CsmDeclaration.Kind.CLASS,
            CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION,
            CsmDeclaration.Kind.ENUM_FORWARD_DECLARATION,
            CsmDeclaration.Kind.ENUM,
        };
        List res = getClassMembers(clazz, contextDeclaration, memberKinds, strPrefix, false, match, inspectParentClasses, inspectOuterClasses, true, returnBaseClasses, false);
        if (isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    public List<CsmMethod> getMethods(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, String strPrefix, boolean staticOnly, boolean match, boolean inspectParentClasses, boolean inspectOuterClasses, boolean scopeAccessedClassifier) {
        CsmDeclaration.Kind memberKinds[] = {
            CsmDeclaration.Kind.FUNCTION,
            CsmDeclaration.Kind.FUNCTION_DEFINITION,
            CsmDeclaration.Kind.FUNCTION_LAMBDA,
            CsmDeclaration.Kind.FUNCTION_FRIEND,
            CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION
        };
        List res = getClassMembers(clazz, contextDeclaration, memberKinds, strPrefix, staticOnly, match, inspectParentClasses, inspectOuterClasses, scopeAccessedClassifier, false, false);
        if (isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    public List<CsmClass> getBaseClasses(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, String strPrefix, boolean match) {
        assert (clazz != null);
        CsmVisibility minVisibility;
        if (contextDeclaration == null) {
            // we are in global context and are interested in all base classes
            minVisibility = CsmInheritanceUtilities.MAX_VISIBILITY;
        } else {
            minVisibility = CsmInheritanceUtilities.getContextVisibility(clazz, contextDeclaration, CsmVisibility.PUBLIC, true);
        }

        Map<CharSequence, CsmClass> set = getBaseClasses(clazz, contextDeclaration, strPrefix, match, new ClassifiersAntiLoop(), minVisibility, INIT_INHERITANCE_LEVEL);
        List<CsmClass> res;
        if (!set.isEmpty()) {
            res = new ArrayList<CsmClass>(set.values());
        } else {
            res = new ArrayList<CsmClass>();
        }
        if (CsmSortUtilities.matchName(clazz.getName(), strPrefix, true, true)) {
            // add current class as well
            res.add(0, clazz);
        }
        return res;
    }

    public List<CsmField> getFields(CsmClass clazz, boolean staticOnly) {
        return getFields(clazz, clazz, "", staticOnly, false, true, true, false);
    }

    @SuppressWarnings("unchecked")
    public List<CsmField> getFields(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, String strPrefix, boolean staticOnly, boolean match, boolean inspectParentClasses, boolean inspectOuterClasses, boolean scopeAccessedClassifier) {
        List/*<CsmMember>*/ res = getClassMembers(clazz, contextDeclaration, new CsmDeclaration.Kind[]{CsmDeclaration.Kind.VARIABLE}, strPrefix, staticOnly, match, inspectParentClasses, inspectOuterClasses, scopeAccessedClassifier, false, false);
        if (isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }

    public List<CsmEnumerator> getEnumerators(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, String strPrefix, boolean match, boolean inspectParentClasses, boolean inspectOuterClasses, boolean scopeAccessedClassifier) {
        // get all enums and check theirs enumerators
        // also get all typedefs and check whether they define
        // unnamed enum
        CsmDeclaration.Kind classKinds[] = {
            CsmDeclaration.Kind.ENUM,
            CsmDeclaration.Kind.ENUM_FORWARD_DECLARATION,
            CsmDeclaration.Kind.TYPEDEF,
            CsmDeclaration.Kind.TYPEALIAS
        };
        List enumsAndTypedefs = getClassMembers(clazz, contextDeclaration, classKinds, "", false, false, inspectParentClasses, inspectOuterClasses, scopeAccessedClassifier, false, true);
        List<CsmEnumerator> res = getEnumeratorsFromEnumsEnumeratorsAndTypedefs(enumsAndTypedefs, match, strPrefix, sort);
        return res;
    }

    public List<CsmMember> getFieldsAndMethods(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, String strPrefix, boolean staticOnly, boolean match, boolean inspectParentClasses, boolean inspectOuterClasses, boolean scopeAccessedClassifier) {
        CsmDeclaration.Kind memberKinds[] = {
            CsmDeclaration.Kind.VARIABLE,
            CsmDeclaration.Kind.FUNCTION,
            CsmDeclaration.Kind.FUNCTION_DEFINITION,
            CsmDeclaration.Kind.FUNCTION_LAMBDA,
            CsmDeclaration.Kind.FUNCTION_FRIEND,
            CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION
        };
        List<CsmMember> res = getClassMembers(clazz, contextDeclaration, memberKinds, strPrefix, staticOnly, match, inspectParentClasses, inspectOuterClasses, scopeAccessedClassifier, false, false);
        if (isSortNeeded()) {
            CsmSortUtilities.sortMembers(res, isNaturalSort(), isCaseSensitive());
        }
        return res;
    }

    // =============== help methods to get/check content of containers =========
    private static final int MAX_INHERITANCE_DEPTH = 15;

    private static final int INIT_INHERITANCE_LEVEL = 0;
    private static final int NO_INHERITANCE = 1;
    private static final int EXACT_CLASS = 2;
    private static final int CHILD_INHERITANCE = 3;

    private List<CsmMember> getClassMembers(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration,
            CsmDeclaration.Kind kinds[], String strPrefix, boolean staticOnly, boolean match,
            boolean inspectParentClasses, boolean inspectOuterClasses, boolean scopeAccessedClassifier, boolean returnBaseClasses, boolean returnUnnamedMembers) {
        assert (clazz != null);
        CsmVisibility minVisibility;
        if (contextDeclaration == null) {
            // we are in global context and are interested in all static members
            minVisibility = CsmInheritanceUtilities.MAX_VISIBILITY;
        } else if (scopeAccessedClassifier) {
            minVisibility = CsmInheritanceUtilities.getContextVisibility(clazz, contextDeclaration, CsmVisibility.PUBLIC, inspectParentClasses);
            if (minVisibility == CsmVisibility.NONE) {
                staticOnly = true;
                minVisibility = CsmInheritanceUtilities.getContextVisibility(clazz, contextDeclaration);
            }
        } else {
            minVisibility = CsmInheritanceUtilities.getContextVisibility(clazz, contextDeclaration);
        }

        Map<CharSequence, CsmMember> set = getClassMembers(clazz, contextDeclaration, kinds, strPrefix, staticOnly, match,
                new ClassifiersAntiLoop(), minVisibility, INIT_INHERITANCE_LEVEL, inspectParentClasses, inspectOuterClasses, returnBaseClasses, returnUnnamedMembers);
        List<CsmMember> res;
        if (set != null && set.size() > 0) {
            res = new ArrayList<CsmMember>(set.values());
        } else {
            res = new ArrayList<CsmMember>();
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    private Map<CharSequence, CsmMember> getClassMembers(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, CsmDeclaration.Kind kinds[],
            String strPrefix, boolean staticOnly, boolean match,
            ClassifiersAntiLoop handledClasses, CsmVisibility minVisibility, int inheritanceLevel, boolean inspectParentClasses, boolean inspectOuterClasses,
            boolean returnBaseClasses,
            boolean returnUnnamedMembers) {
        assert (clazz != null);

        if (handledClasses.contains(clazz)) {
            return Collections.<CharSequence, CsmMember>emptyMap();
        }

        if (minVisibility == CsmVisibility.NONE) {
            return Collections.<CharSequence, CsmMember>emptyMap();
        }

        VisibilityInfo visibilityInfo = getContextVisibility(clazz, contextDeclaration, minVisibility, inheritanceLevel);
        minVisibility = visibilityInfo.visibility;
        inheritanceLevel = visibilityInfo.inheritanceLevel;
        boolean friend = visibilityInfo.friend;
        Map<CharSequence, CsmMember> res = new LinkedHashMap<CharSequence, CsmMember>(); // order is important
        CsmFilter memberFilter = CsmContextUtilities.createFilter(kinds,
                strPrefix, match, caseSensitive, returnUnnamedMembers);
        Collection<CsmClass> classesAskedForMembers = new ArrayList(1);
        classesAskedForMembers.add(clazz);
        if (inspectOuterClasses) {
            CsmScope outerScope = clazz.getScope();
            while (CsmKindUtilities.isClass(outerScope)) {
                if (!handledClasses.contains((CsmClass) outerScope)) {
                    classesAskedForMembers.add((CsmClass) outerScope);
                }
                outerScope = ((CsmClass) outerScope).getScope();
            }
            CsmProject classProject = clazz.getContainingFile().getProject();
            if (classProject != null) {
                CharSequence[] nameParts = splitQualifiedName(clazz.getQualifiedName().toString());
                if (nameParts.length > 1) {
                    StringBuilder className = new StringBuilder(""); // NOI18N
                    for (CharSequence charSequence : nameParts) {
                        className.append(charSequence);
                        for (CsmClassifier cls : classProject.findClassifiers(className)) {
                            if (CsmKindUtilities.isClass(cls)) {
                                classesAskedForMembers.add((CsmClass) cls);
                            }
                        }
                        className.append(SCOPE);
                    }
                }
            }
        }
        for (CsmClass csmClass : classesAskedForMembers) {
            handledClasses.add(csmClass);
            Iterator<CsmMember> it = CsmSelect.getClassMembers(csmClass, memberFilter);
            int unnamedEnumCount = 0;
            while (it.hasNext()) {
                CsmMember member = it.next();
                if (isKindOf(member.getKind(), kinds) &&
                        (!staticOnly || member.isStatic()) &&
                        matchVisibility(member, minVisibility)) {
                    CharSequence memberName = member.getName();
                    if ((matchName(memberName, strPrefix, match)) ||
                            (memberName.length() == 0 && returnUnnamedMembers)) {
                        CharSequence qname;
                        if (CsmKindUtilities.isFunction(member)) {
                            qname = ((CsmFunction) member).getSignature();
                        } else {
                            qname = member.getQualifiedName();
                            if (member.getName().length() == 0 && CsmKindUtilities.isEnum(member)) {
                                // Fix for IZ#139784: last unnamed enum overrides previous ones
                                qname = CharSequences.create(new StringBuilder(qname).append('$').append(++unnamedEnumCount));
                            }
                        }
                        // do not replace inner objects by outer ones
                        if (!res.containsKey(qname)) {
                            res.put(qname, member);
                        }
                    }
                }
            }

            // inspect unnamed unions, structs and classes
            CsmDeclaration.Kind memberKinds[] = {
                CsmDeclaration.Kind.UNION,
                CsmDeclaration.Kind.STRUCT,
                CsmDeclaration.Kind.CLASS,};
            CsmFilter nestedClassifierFilter = CsmContextUtilities.createFilter(memberKinds, "*", true, false, true); // NOI18N
            it = CsmSelect.getClassMembers(csmClass, nestedClassifierFilter);
            while (it.hasNext()) {
                CsmMember member = it.next();
                CharSequence memberName = member.getName();
                if (memberName.length() == 0) {
                    if (isKindOf(member.getKind(), memberKinds) &&
                            matchVisibility(member, minVisibility)) {
                        CsmClass innerClass = (CsmClass) member;
                        // if inner class doesn't have enclosing variables => iterate it
                        if (innerClass.getEnclosingVariables().isEmpty()) {
                            Map<CharSequence, CsmMember> set = getClassMembers(innerClass, contextDeclaration, kinds, strPrefix, staticOnly, match,
                                    handledClasses, CsmVisibility.PUBLIC, INIT_INHERITANCE_LEVEL, inspectParentClasses, inspectOuterClasses, returnBaseClasses, returnUnnamedMembers);
                            // replace by own elements in nested set
                            if (set != null && set.size() > 0) {
                                set.putAll(res);
                                res = set;
                            }
                        }
                    }
                }
            }

            // inspect usings
            CsmDeclaration.Kind usingKind[] = {
                CsmDeclaration.Kind.USING_DECLARATION,
                };
            CsmFilter usingFilter = CsmSelect.getFilterBuilder().createKindFilter(usingKind); // NOI18N
            it = CsmSelect.getClassMembers(csmClass, usingFilter);
            while (it.hasNext()) {
                CsmMember member = it.next();
                if (isKindOf(member.getKind(), usingKind) &&
                        matchVisibility(member, minVisibility)) {
                    CsmUsingDeclaration using = (CsmUsingDeclaration) member;
                    CsmDeclaration decl = using.getReferencedDeclaration();
                    if (CsmKindUtilities.isClassMember(decl)) {
                        CsmClass containingClass = ((CsmMember)decl).getContainingClass();
                        if (containingClass != null) {
                            VisibilityInfo vInfo = getContextVisibility(containingClass, member, CsmVisibility.PROTECTED, INIT_INHERITANCE_LEVEL);
                            if (matchVisibility((CsmMember) decl, vInfo.visibility)) {
                                if (isKindOf(decl.getKind(), kinds)) {
                                    CharSequence memberName = decl.getName();
                                    if ((matchName(memberName, strPrefix, match)) ||
                                            (memberName.length() == 0 && returnUnnamedMembers)) {
                                        CharSequence qname;
                                        if (CsmKindUtilities.isFunction(decl)) {
                                            qname = ((CsmFunction) decl).getSignature();
                                        } else {
                                            qname = decl.getQualifiedName();
                                        }
                                        // do not replace inner objects by outer ones
                                        if (!res.containsKey(qname)) {
                                            res.put(qname, (CsmMember) decl);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (inspectParentClasses) {
                // handle base classes in context of original class/function
                for (Iterator<CsmInheritance> it2 = csmClass.getBaseClasses().iterator(); it2.hasNext();) {
                    CsmInheritance inherit = it2.next();
                    CsmClass baseClass = CsmInheritanceUtilities.getCsmClass(inherit);
                    if (baseClass != null) {
                        if (returnBaseClasses) {
                            CharSequence baseClsName = baseClass.getName();
                            if (isKindOf(baseClass.getKind(), kinds) && (matchName(baseClsName, strPrefix, match))) {
                                // do not replace inner objects by outer ones
                                if (!res.containsKey(baseClsName)) {
                                    res.put(baseClsName, (CsmMember) baseClass);
                                }
                            }
                        }
                        VisibilityInfo nextInfo = getNextInheritanceInfo(contextDeclaration, minVisibility, inherit, inheritanceLevel, friend);
                        CsmVisibility nextMinVisibility = nextInfo.visibility;
                        int nextInheritanceLevel = nextInfo.inheritanceLevel;

                        Map<CharSequence, CsmMember> baseRes = getClassMembers(baseClass, contextDeclaration, kinds, strPrefix, staticOnly, match,
                                handledClasses, nextMinVisibility, nextInheritanceLevel, inspectParentClasses, inspectOuterClasses, returnBaseClasses, returnUnnamedMembers);
                        if (baseRes != null && !baseRes.isEmpty()) {
                            // add parent members at the end
                            for (Map.Entry<CharSequence, CsmMember> entry : baseRes.entrySet()) {
                                if (!res.containsKey(entry.getKey())) {
                                    res.put(entry.getKey(), entry.getValue());
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
    private Map<CharSequence, CsmClass> getBaseClasses(CsmClass csmClass, CsmOffsetableDeclaration contextDeclaration, String strPrefix, boolean match,
            ClassifiersAntiLoop handledClasses, CsmVisibility minVisibility, int inheritanceLevel) {
        assert (csmClass != null);

        if (handledClasses.contains(csmClass)) {
            return new HashMap<CharSequence, CsmClass>();
        }
        handledClasses.add(csmClass);

        if (minVisibility == CsmVisibility.NONE) {
            return new HashMap<CharSequence, CsmClass>();
        }

        VisibilityInfo visibilityInfo = getContextVisibility(csmClass, contextDeclaration, minVisibility, inheritanceLevel);
        minVisibility = visibilityInfo.visibility;
        inheritanceLevel = visibilityInfo.inheritanceLevel;
        boolean friend = visibilityInfo.friend;

        Map<CharSequence, CsmClass> res = new HashMap<CharSequence, CsmClass>();
        // handle base classes in context of original class/function
        for (Iterator<CsmInheritance> it2 = csmClass.getBaseClasses().iterator(); it2.hasNext();) {
            CsmInheritance inherit = it2.next();
            CsmClass baseClass = CsmInheritanceUtilities.getCsmClass(inherit);
            if (baseClass != null) {
                if (!baseClass.equals(csmClass)) {
                    VisibilityInfo nextInfo = getNextInheritanceInfo(contextDeclaration, minVisibility, inherit, inheritanceLevel, friend);
                    CsmVisibility nextMinVisibility = nextInfo.visibility;
                    int nextInheritanceLevel = nextInfo.inheritanceLevel;
                    if (nextMinVisibility != CsmVisibility.NONE) {
                        Map<CharSequence, CsmClass> baseRes = getBaseClasses(baseClass, contextDeclaration, strPrefix, match,
                                handledClasses, nextMinVisibility, nextInheritanceLevel);
                        if (matchName(baseClass.getName(), strPrefix, match)) {
                            baseRes.put(baseClass.getQualifiedName(), baseClass);
                        }
                        // replace by own elements in inherited set
                        baseRes.putAll(res);
                        res = baseRes;
                    }
                } else {
                   // Commented because it is absolutely ok in case of variadic templates inheritance,
                   // or just in case of unfinished code, like that:
                   //
                   //   template <typename T>
                   //   struct AAA : AAA<typename T::next> {};
                   //
                   //CndUtils.assertTrueInConsole(false, "Infinite recursion in file " + csmClass.getContainingFile() + " class " + csmClass); //NOI18N
                }
            }
        }
        return res;
    }

    private List/*<CsmDeclaration>*/ getNamespaceMembers(CsmNamespace ns, CsmDeclaration.Kind kind, String strPrefix, boolean match, boolean searchNestedUnnamedNamespaces, boolean returnUnnamedMembers) {
        return getNamespaceMembers(ns, new CsmDeclaration.Kind[]{kind}, strPrefix, match, searchNestedUnnamedNamespaces, returnUnnamedMembers);
    }

    private List<CsmDeclaration> getNamespaceMembers(CsmNamespace ns, CsmDeclaration.Kind kinds[], String strPrefix, boolean match, boolean searchNestedUnnamedNamespaces, boolean returnUnnamedMembers) {
        List<CsmDeclaration> res = getNamespaceMembers(ns, kinds, strPrefix, match, new HashSet<CsmNamespace>(), searchNestedUnnamedNamespaces, returnUnnamedMembers);
        return res;
    }

    private List<CsmDeclaration> getNamespaceMembers(CsmNamespace ns, CsmDeclaration.Kind kinds[], String strPrefix, boolean match, Set<CsmNamespace> handledNS, boolean searchNestedUnnamedNamespaces, boolean returnUnnamedMembers) {
        return getNamespaceMembers(ns, kinds, strPrefix, match, handledNS, searchNestedUnnamedNamespaces, returnUnnamedMembers, returnUnnamedMembers, returnUnnamedMembers);
    }

    private List<CsmDeclaration> getNamespaceMembers(CsmNamespace ns,
                                                     CsmDeclaration.Kind kinds[],
                                                     String strPrefix,
                                                     boolean match,
                                                     Set<CsmNamespace> handledNS,
                                                     boolean searchNestedUnnamedNamespaces,
                                                     boolean returnUnnamedInCurrentNs,
                                                     boolean returnUnnamedInNestedNs,
                                                     boolean returnUnnamedInParentNs)
    {
        if (handledNS.contains(ns)) {
            return Collections.<CsmDeclaration>emptyList();
        }

        handledNS.add(ns);
        List<CsmDeclaration> res = new ArrayList<CsmDeclaration>();
        Iterator it;
        //it = ns.getDeclarations().iterator();
        //filterDeclarations(it, res, kinds, strPrefix, match, returnUnnamedMembers);
        filterDeclarations(ns, res, kinds, strPrefix, match, returnUnnamedInCurrentNs);
        if (!ns.isGlobal()) {
            for (CsmProject lib : ns.getProject().getLibraries()) {
                CsmNamespace n = lib.findNamespace(ns.getQualifiedName());
                if (n != null && !handledNS.contains(n)) {
                    filterDeclarations(n, res, kinds, strPrefix, match, returnUnnamedInCurrentNs);
                    handledNS.add(n);
                }
            }
        }
        // handle all nested and inlined namespaces

        // Note that nested namespaces contain inlined namespaces
        Collection<CsmNamespace> namespacesToSearchIn;
        if (searchNestedUnnamedNamespaces) {
            namespacesToSearchIn = ns.getNestedNamespaces();
        } else {
            namespacesToSearchIn = getInlinedNamespaces(ns, libs);
        }

        if (!namespacesToSearchIn.isEmpty()) {
          for (it = namespacesToSearchIn.iterator(); it.hasNext();) {
              CsmNamespace nestedNs = (CsmNamespace) it.next();

              boolean goDeeper;
              if (searchNestedUnnamedNamespaces) {
                  // namespacesToSearchIn contains all nested namespaces
                  goDeeper = nestedNs.getName().length() == 0 || isInlinedNamespace(nestedNs, libs);
              } else {
                  // namespacesToSearchIn contains only inlined namespaces
                  goDeeper = true;
              }

              // we need nested namespaces only if they do not modify qualified path (they names are empty) or they are inlined
              if (goDeeper) {
              // TODO: consider when we add nested namespaces
  //        if (nestedNs.getName().length() != 0) {
  //            if (need namespaces &&
  //                    matchName(nestedNs.getName(), strPrefix, match)) {
  //                res.add(nestedNs);
  //            }
  //        }
                  res.addAll(getNamespaceMembers(nestedNs, kinds, strPrefix, match, handledNS, true, returnUnnamedInNestedNs));
              }
          }
        }

        // handle all parent namespaces
        CsmNamespace parentNS = ns.getParent();
        while(parentNS != null && !handledNS.contains(parentNS) && !ns.isGlobal()) {
            handledNS.add(parentNS);

            strPrefix = ns.getName() + SCOPE + strPrefix; // NOI18N
            filterDeclarations(parentNS, res, kinds, strPrefix, match, returnUnnamedInParentNs);

            ns  = parentNS;
            parentNS = parentNS.getParent();
        }

        return res;
    }

    @SuppressWarnings("unchecked")
    /*package*/ void filterDeclarations(final CsmNamespace ns, final Collection out, final CsmDeclaration.Kind[] kinds, final String strPrefix, final boolean match, final boolean returnUnnamedMembers) {
        CsmFilter filter = CsmContextUtilities.createFilter(kinds, strPrefix, match, caseSensitive, returnUnnamedMembers);
        Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDeclarations(ns, filter);
        while (it.hasNext()) {
            CsmDeclaration decl = it.next();
            if (isKindOf(decl.getKind(), kinds)) {
                CharSequence name = decl.getName();
                if (matchName(name, strPrefix, match) || (name.length() == 0 && returnUnnamedMembers)) {
                    out.add(decl);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    /*package*/ void filterDeclarations(final Iterator in, final Collection out, final CsmDeclaration.Kind kinds[], final String strPrefix, final boolean match, final boolean returnUnnamedMembers) {
        while (in.hasNext()) {
            CsmDeclaration decl = (CsmDeclaration) in.next();
            if (isKindOf(decl.getKind(), kinds)) {
                CharSequence name = decl.getName();
                if (matchName(name, strPrefix, match) || (name.length() == 0 && returnUnnamedMembers)) {
                    out.add(decl);
                }
            }
        }
    }

    private static boolean isKindOf(CsmDeclaration.Kind kind, CsmDeclaration.Kind kinds[]) {
        for (int i = 0; i < kinds.length; i++) {
            if (kind == kinds[i]) {
                return true;
            }
        }
        return false;
    }

    private boolean matchName(CharSequence name, String strPrefix, boolean match) {
        return CsmSortUtilities.startsWith(name.toString(), strPrefix, match, caseSensitive);
    }

    public boolean matchVisibility(CsmMember member, CsmVisibility minVisibility) {
        return CsmInheritanceUtilities.matchVisibility(member, minVisibility);
    }

    private Map<String, CsmObject> mergeByFQN(Map<String, CsmObject> orig, Collection<? extends CsmObject> newList) {
        assert orig != null;
        if (newList != null && newList.size() > 0) {
            for (CsmObject object : newList) {
                assert CsmKindUtilities.isQualified(object);
                String fqn = ((CsmQualifiedNamedElement) object).getQualifiedName().toString();
                orig.put(fqn, object);
            }
        }
        return orig;
    }

    private <T> Collection<T> merge(Collection<T> orig, Collection<T> newList) {
        return CsmUtilities.merge(orig, newList);
    }

    private List<CsmFunction> filterFunctionDefinitions(List funs) {
        List<CsmFunction> out = new ArrayList<CsmFunction>();
        if (funs != null && funs.size() > 0) {
            for (Iterator it = funs.iterator(); it.hasNext();) {
                CsmObject fun = (CsmObject) it.next();
                if (!CsmKindUtilities.isFunctionDefinition(fun) ||
                        ((CsmFunctionDefinition) fun).getDeclaration() == fun) {
                    out.add((CsmFunction) fun);
                }
            }
        }
        return out;
    }

    private List<CsmVariable> filterVariables(List<CsmVariable> res) {
        Map<String, CsmVariable> out = new HashMap<String, CsmVariable>(res.size());
        for (CsmVariable var : res) {
            String fqn = var.getQualifiedName().toString();
            CsmVariable old = out.get(fqn);
            // replace extern variable by normal one if needed
            if (old == null || !CsmKindUtilities.isExternVariable(var)) {
                out.put(fqn, var);
            }
        }
        return new ArrayList<CsmVariable>(out.values());
    }

    private List<CsmVariable> filterFileLocalStaticVariables(List<CsmVariable> vars) {
        List<CsmVariable> res = new ArrayList<CsmVariable>();
        for (CsmVariable var : vars) {
            if (!CsmBaseUtilities.isGlobalVariable(var)) {
                res.add(var);
            }
        }
        return res;
    }

    ////////////////////////////////////////////////////////////////////////////
    // staff to help with visibility handling
    private static final class VisibilityInfo {

        private final int inheritanceLevel;
        private final CsmVisibility visibility;
        private final boolean friend;

        public VisibilityInfo(int inheritanceLevel, CsmVisibility visibility, boolean friend) {
            this.inheritanceLevel = inheritanceLevel;
            this.visibility = visibility;
            this.friend = friend;
        }

        @Override
        public String toString() {
            return "inheritanceLevel=" + inheritanceLevel + ", visibility=" + visibility + ", friend=" + friend; // NOI18N
        }
    }

    private VisibilityInfo getContextVisibility(CsmClass clazz, CsmOffsetableDeclaration contextDeclaration, CsmVisibility minVisibility, int inheritanceLevel) {
        boolean friend = false;
        if (inheritanceLevel == INIT_INHERITANCE_LEVEL) {
            inheritanceLevel = NO_INHERITANCE;
            CsmClass contextClass = CsmBaseUtilities.getContextClass(contextDeclaration);
            if (clazz.equals(contextClass)) {
                inheritanceLevel = EXACT_CLASS;
            } else if (contextClass != null) {
                // check how clazz is visible in context class
                while (true) {
                    if (CsmInheritanceUtilities.isAssignableFrom(contextClass, clazz)) {
                        inheritanceLevel = CHILD_INHERITANCE;
                    }
                    CsmScope nextScope = contextClass.getScope();
                    if(nextScope != null && CsmKindUtilities.isClass(nextScope) && !contextClass.equals((CsmClass)nextScope)) {
                        contextClass = (CsmClass) nextScope;
                    } else {
                        break;
                    }
                }
            // TODO: think about opposite usage C extends B extends A; C is used in A context
            // what is by spec? Are there additional visibility for A about C?
            }
            if (inheritanceLevel == NO_INHERITANCE && contextDeclaration != null) {
                friend = CsmFriendResolver.getDefault().isFriend(contextDeclaration, clazz);
            }
        } else if (contextDeclaration != null) {
            // min visibility can be changed by context declaration properties
            CsmInheritanceUtilities.ContextVisibilityInfo cvi = CsmInheritanceUtilities.getContextVisibilityInfo(clazz, contextDeclaration, minVisibility, inheritanceLevel == CHILD_INHERITANCE);
            minVisibility = cvi.visibility;
            friend = cvi.friend;
        }
        return new VisibilityInfo(inheritanceLevel, minVisibility, friend);
    }

    private VisibilityInfo getNextInheritanceInfo(CsmOffsetableDeclaration contextDeclaration, CsmVisibility curMinVisibility, CsmInheritance inherit, int curInheritanceLevel, boolean friend) {
        CsmVisibility nextMinVisibility;
        int nextInheritanceLevel = curInheritanceLevel;
        if (curInheritanceLevel == NO_INHERITANCE) {
            if(friend) {
                nextMinVisibility = CsmInheritanceUtilities.mergeInheritedVisibility(curMinVisibility, inherit.getVisibility());
                nextInheritanceLevel = CHILD_INHERITANCE;
            } else if (contextDeclaration == null && curMinVisibility == CsmInheritanceUtilities.MAX_VISIBILITY && inherit.getVisibility() == CsmVisibility.PUBLIC) {
                // We are in global context => it may be a definition of inner member (which must be visible)
                // so keep the MAX_VISIBILITY. Note, that this is a hack and different compilers have different behavior on that matter
                nextMinVisibility = CsmInheritanceUtilities.MAX_VISIBILITY;
                nextInheritanceLevel = NO_INHERITANCE;
            } else {
                nextMinVisibility = CsmInheritanceUtilities.mergeExtInheritedVisibility(curMinVisibility, inherit.getVisibility());
                nextInheritanceLevel = NO_INHERITANCE;
            }
        } else if (curInheritanceLevel == EXACT_CLASS) {
            // create merged visibility based on direct inheritance
            nextMinVisibility = CsmInheritanceUtilities.mergeInheritedVisibility(curMinVisibility, inherit.getVisibility());
            nextInheritanceLevel = CHILD_INHERITANCE;
        } else {
            assert (curInheritanceLevel == CHILD_INHERITANCE);
            // create merged visibility based on child inheritance
            nextMinVisibility = CsmInheritanceUtilities.mergeChildInheritanceVisibility(curMinVisibility, inherit.getVisibility());
            nextInheritanceLevel = CHILD_INHERITANCE;
        }
        return new VisibilityInfo(nextInheritanceLevel, nextMinVisibility, false);
    }
    
    private boolean isCpp() {
        // Or use CsmFileInfoQuery here?
        if (startFile != null) {
            switch (startFile.getFileType()) {
                case SOURCE_C_FILE:
                case SOURCE_FORTRAN_FILE:
                    return false;
            }
        }
        return true;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // staff to help with local symbols visible via #include directives
    
    private static abstract class LocalSymbolsCollector<S extends CsmOffsetableDeclaration> {
        
        protected final CsmFilter filter;

        public LocalSymbolsCollector(CsmFilter filter) {
            this.filter = filter;
        }

        public abstract Iterator<S> getStaticSymbols(CsmFile file);
        
        public abstract void handle(S symbol);
        
    }
    
    private final static class LocalVariablesCollector extends LocalSymbolsCollector<CsmVariable> {
        
        private final Collection<CsmVariable> out;

        public LocalVariablesCollector(Collection<CsmVariable> out, CsmFilter filter) {
            super(filter);
            this.out = out;
        }

        @Override
        public Iterator<CsmVariable> getStaticSymbols(CsmFile file) {
            return CsmSelect.getStaticVariables(file, filter);
        }

        @Override
        public void handle(CsmVariable symbol) {
            if (CsmKindUtilities.isFileLocalVariable(symbol)) {
                out.add(symbol);
            }
        }
    }
    
    private final static class LocalFunctionsCollector extends LocalSymbolsCollector<CsmFunction> {
        
        private final Map<CharSequence, CsmFunction> out;

        public LocalFunctionsCollector(Map<CharSequence, CsmFunction> out, CsmFilter filter) {
            super(filter);
            this.out = out;
        }

        @Override
        public Iterator<CsmFunction> getStaticSymbols(CsmFile file) {
            return CsmSelect.getStaticFunctions(file, filter);
        }

        @Override
        public void handle(CsmFunction symbol) {
            if (CsmKindUtilities.isFileLocalFunction(symbol)) {
                out.put(symbol.getSignature(), symbol);
            }
        }
    }
    
    private static <S extends CsmOffsetableDeclaration> void fillFileLocalIncludeSymbols(LocalSymbolsCollector<S> collector, 
        CsmFile file, Set<CsmFile> antiLoop, boolean first) {
        if (antiLoop.contains(file)) {
            return;
        }
        if (first) {
            // check #inlcude stack at first
            List<CsmInclude> includeStack = CsmFileInfoQuery.getDefault().getIncludeStack(file);
            for (CsmInclude include : includeStack) {
                CsmFile srcFile = include.getContainingFile();
                int endOffset = include.getStartOffset();
                Iterator<S> it = collector.getStaticSymbols(srcFile);
                while (it.hasNext()) {
                    S decl = it.next();
                    if (CsmOffsetUtilities.isAfterObject(decl, endOffset)) {
                        collector.handle(decl);
                    } else {
                        break;
                    }
                }
            }
        }
        antiLoop.add(file);
        for (CsmInclude incl : file.getIncludes()) {
            CsmFile f = incl.getIncludeFile();
            if (f != null) {
                fillFileLocalIncludeSymbols(collector, f, antiLoop, false);
            }
        }
        if (!first) {
            Iterator<S> it = collector.getStaticSymbols(file);
            while (it.hasNext()) {
                S decl = it.next();
                collector.handle(decl);
            }
        }
    }

    public static CharSequence[] splitQualifiedName(String qualified) {
        List<CharSequence> v = new ArrayList<CharSequence>();
        for (StringTokenizer t = new StringTokenizer(qualified, ": \t\n\r\f", false); t.hasMoreTokens(); ) {// NOI18N
            v.add(t.nextToken());
        }
        return v.toArray(new CharSequence[v.size()]);
    }
}
