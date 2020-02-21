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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.modules.cnd.api.model.CsmClassForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.support.CsmClassifierResolver;
import org.netbeans.modules.cnd.api.model.services.CsmCompilationUnit;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmIncludeResolver;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmUsingResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmSortUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.completion.impl.xref.SymTabCache.CacheEntry;
import org.netbeans.modules.cnd.completion.impl.xref.FileReferencesContext;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.spi.model.CsmBaseUtilitiesProvider;

/**
 *
 */
public class CompletionResolverImpl implements CompletionResolver {

    private static final boolean DEBUG_SUMMARY = Boolean.getBoolean("csm.utilities.trace.summary");
    private static final boolean TRACE = Boolean.getBoolean("csm.utilities.trace");
    private static final boolean DEBUG = TRACE | DEBUG_SUMMARY;
    //    public static final int RESOLVE_CLASS_ENUMERATORS       = 1 << 13;
    private int resolveTypes = RESOLVE_NONE;
    private boolean resolveContextMode  = false;
    private int hideTypes = ~RESOLVE_NONE;
    private CsmFile file;
    private CsmContext context;
    Result result = EMPTY_RESULT;
    CsmProjectContentResolver contResolver = null;
    private boolean caseSensitive = false;
    private boolean naturalSort = false;
    private boolean sort = false;
    private static final int NOT_INITIALIZED = -1;
    private int contextOffset = NOT_INITIALIZED;
    private CsmScope contextScope = null;
    private QueryScope queryScope = QueryScope.GLOBAL_QUERY;
    private boolean inIncludeDirective = false;
    private final FileReferencesContext fileReferncesContext;

    public boolean isSortNeeded() {
        return sort;
    }

    public void setSortNeeded(boolean sort) {
        this.sort = sort;
    }

    public void setContextOffset(int offset) {
        this.contextOffset = offset;
    }

    public void setContextScope(CsmScope scope) {
        this.contextScope = scope;
    }

    @Override
    public QueryScope setResolveScope(QueryScope queryScope) {
        QueryScope oldScope = this.queryScope;
        this.queryScope = queryScope;
        return oldScope;
    }

    public void setInIncludeDirective(boolean inIncludeDirective) {
        this.inIncludeDirective = inIncludeDirective;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public boolean isNaturalSort() {
        return naturalSort;
    }

    /** Creates a new instance of CompletionResolver */
    public CompletionResolverImpl(CsmFile file) {
        this(file, false, false, false, null);
    }

    public CompletionResolverImpl(CsmFile file, boolean caseSensitive, boolean sort, boolean naturalSort, FileReferencesContext fileReferncesContext) {
        this(file, RESOLVE_CONTEXT, caseSensitive, sort, naturalSort, fileReferncesContext);
    }

    private CompletionResolverImpl(CsmFile file, int resolveTypes, boolean caseSensitive, boolean sort, boolean naturalSort, FileReferencesContext fileReferncesContext) {
        this.file = file;
        this.resolveTypes = resolveTypes;
        this.resolveContextMode = ((resolveTypes & RESOLVE_CONTEXT) == RESOLVE_CONTEXT);
        this.caseSensitive = caseSensitive;
        this.naturalSort = naturalSort;
        this.sort = sort;
        this.fileReferncesContext = fileReferncesContext;
    }

    @Override
    public void setResolveTypes(int resolveTypes) {
        this.resolveTypes = resolveTypes;
        this.resolveContextMode = ((resolveTypes & RESOLVE_CONTEXT) == RESOLVE_CONTEXT);
    }

    @Override
    public boolean refresh() {
        result = EMPTY_RESULT;
        // update if file attached to invalid project
        if ((file != null) && (file.getProject() != null) && !file.getProject().isValid()) {
            file = CsmUtilities.getCsmFile(CsmUtilities.getFileObject(file), true, false);
        }
        context = null;
        // should be called last, because uses setting set above
        this.contResolver = null;
        if (file == null) {
            return false;
        }
        this.contResolver = createContentResolver();
        return true;
    }

    public boolean update(boolean caseSensitive, boolean naturalSort) {
        this.caseSensitive = caseSensitive;
        this.naturalSort = naturalSort;
        return refresh();
    }

    @Override
    public boolean resolve(int docOffset, String strPrefix, boolean match) {
        int offset = contextOffset == NOT_INITIALIZED ? docOffset : contextOffset;
        if (file == null) {
            return false;
        }
        if (contextScope != null) {
            context = CsmOffsetResolver.findContextFromScope(file, offset, contextScope);
        } else {
            context = CsmOffsetResolver.findContext(file, offset, fileReferncesContext);
        }
        if (DEBUG) {
            System.out.println("context for offset " + offset + " :\n" + context);//NOI18N
        }
        initResolveMask(context, offset, strPrefix, match);
        this.hideTypes = initHideMask(context, offset, this.resolveTypes, this.queryScope, strPrefix, match, this.inIncludeDirective);
        resolveContext(context, offset, strPrefix, match);
        return file != null;
    }

    @Override
    public Result getResult() {
        return this.result;
    }
    public static final boolean STAT_COMPLETION = Boolean.getBoolean("cnd.completion.stat");
    public static final boolean TIMING_COMPLETION = Boolean.getBoolean("cnd.completion.timing") || STAT_COMPLETION;
    public static final boolean USE_CACHE = true;

    private void resolveContext(CsmContext context, int offset, String strPrefix, boolean match) {
        long time = 0;
        if (TIMING_COMPLETION) {
            time = System.currentTimeMillis();
            System.err.println("Started resolving context");
        }
        CsmProject prj = file != null ? file.getProject() : null;
        if (prj == null) {
            return;
        }
        CacheEntry key = null;
        CsmFunction fun = CsmContextUtilities.getFunction(context, true);
        ResultImpl resImpl = new ResultImpl(context);
        boolean isLocalVariable = resolveLocalContext(prj, resImpl, fun, context, offset, strPrefix, match);
        if (USE_CACHE && isEnough(strPrefix, match)) {
            if (isLocalVariable) {
                result = buildResult(context, resImpl);
                return;
            }
            CsmObject cacheDecl = null;
            if (fun != null) {
                cacheDecl = fun;
            } else if (CsmKindUtilities.isVariable(context.getLastObject())) {
                cacheDecl = (CsmVariable) context.getLastObject();
                if (CsmKindUtilities.isParameter(cacheDecl)) {
                    // do not cache parameters, they are local variables as well
                    cacheDecl = null;
                } else {
                    // Try to detect context of long chain variables separated by comma.
                    // This code is not right but allow to fix performance problems.
                    // TODO: Rework it.
                    CsmScope scope = ((CsmVariable)cacheDecl).getScope();
                    if (CsmKindUtilities.isClass(scope) ||
                        CsmKindUtilities.isEnum(scope)) {
                        cacheDecl = scope;
                    } else if (CsmKindUtilities.isNamespace(scope)) {
                        cacheDecl = scope;
                    }
                }
            }
            if (CsmBaseUtilities.isValid(cacheDecl) && fileReferncesContext != null) {
                CsmUID uid = UIDs.get(cacheDecl);
                key = new CacheEntry(resolveTypes, hideTypes, strPrefix, uid);
                Result res = fileReferncesContext.getSymTabCache().get(key);
                if (res != null) {
                    result = res;
                    return;
                } else {
                    fileReferncesContext.getSymTabCache().setScope(uid);
                }
            }
        }
        //long timeStart = System.nanoTime();
        resolveContext(prj, resImpl, context, offset, strPrefix, match);
        result = buildResult(context, resImpl);
        if (key != null) {
            fileReferncesContext.getSymTabCache().put(key, result);
        }
        //long timeEnd = System.nanoTime();
        //System.out.println("get gesolve list time "+(timeEnd -timeStart)+" objects "+result.size()); //NOI18N
        //System.out.println("get global macro time "+(timeGlobMacroEnd -timeGlobMacroStart)+" objects "+ //NOI18N
        //        (globProjectMacros.size()+globLibMacros.size()));
        if (TIMING_COMPLETION) {
            time = System.currentTimeMillis() - time;
            System.err.println("Resolving context took " + time + "ms");
        }
    }

    private boolean isEnough(String strPrefix, boolean match) {
        return match && strPrefix != null && strPrefix.length() > 0;
    }

    private boolean isEnough(String strPrefix, boolean match, Collection<?> collection) {
        if (collection != null && isEnough(strPrefix, match)) {
            if (collection.size() > 1) {
                return true;
            } else if (!collection.isEmpty()) {
                // We have fast lookup if strPrefix denotes the context class name.
                // Therefore if collection contains only one template class we shouldn't stop
                // searching, because there might be template parameters after strPrefix.
                // We need to look for all specializations.
                Object elem = collection.iterator().next();
                if (CsmKindUtilities.isCsmObject(elem) 
                        && CsmKindUtilities.isClass((CsmObject) elem)
                        && CsmKindUtilities.isTemplate((CsmObject) elem)) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param strPrefix
     * @param match
     * @param collection collection to check and update with visible objects
     * @param toClean
     * @param out collection where results are added after check
     * @return true if found visible objects in collection
     */
    @SuppressWarnings("unchecked")
    private boolean isEnoughAfterFilterVisibileObjects(String strPrefix, boolean match,
            Collection<? extends CsmObject> toCheck, Collection out) {
        boolean enough = false;
        boolean foundVisible = false;
        if (isEnough(strPrefix, match, toCheck)) {
            assert toCheck != null && !toCheck.isEmpty();
            // we have found possible candidates, but we'd prefer to check visibility to
            // select the best one
            Collection<CsmObject> visibleObjs = new ArrayList<CsmObject>();
            Collection<CsmObject> visibleTypedefs = new ArrayList<CsmObject>();
            Collection<CsmObject> visibleFwd = new ArrayList<CsmObject>();
            CsmIncludeResolver resolver = CsmIncludeResolver.getDefault();
            CsmFile startFile = contResolver.getStartFile();
            for (CsmObject obj : toCheck) {
                boolean isVisible = false;

                if (CsmKindUtilities.isOffsetable(obj)) {
                    isVisible = resolver.isObjectVisible(((CsmOffsetable) obj).getContainingFile(), file);
                }

                isVisible |= resolver.isObjectVisible(startFile, obj);

                if (isVisible) {
                    foundVisible = true;
                    if(CsmClassifierResolver.getDefault().isForwardClassifier(obj)) {
                        visibleFwd.add(obj);
                    } else if (CsmKindUtilities.isTypedefOrTypeAlias(obj)) {
                        visibleTypedefs.add(obj);
                        enough = true;
                    } else {
                        visibleObjs.add(obj);
                        enough = true;
                    }
                }
            }
            if (foundVisible) {
                // add visible
                out.addAll(visibleObjs);
                out.addAll(visibleTypedefs);
                out.addAll(visibleFwd);
            }
        }
        return enough;
    }

    private boolean resolveLocalContext(CsmProject prj, ResultImpl resImpl, CsmFunction fun, CsmContext context, int offset, String strPrefix, boolean match) {
        boolean needVars = needLocalVars(context, offset);
        boolean needClasses = needLocalClasses(context, offset);
        if (needVars || needClasses) {
            List<CsmDeclaration> decls = contResolver.findFunctionLocalDeclarations(context, strPrefix, match);
            // separate local classes/structs/enums/unions and variables
            resImpl.localVars = new ArrayList<CsmVariable>(decls.size());
            for (CsmDeclaration elem : decls) {
                if (needVars && CsmKindUtilities.isVariable(elem)) {
                    resImpl.localVars.add((CsmVariable) elem);
                    if (isEnough(strPrefix, match)) {
                        return true;
                    }
                }
                if (needClasses && CsmKindUtilities.isClassifier(elem)) {
                    if (resImpl.classesEnumsTypedefs == null) {
                        resImpl.classesEnumsTypedefs = new ArrayList<CsmClassifier>();
                    }
                    resImpl.classesEnumsTypedefs.add((CsmClassifier) elem);
                    if (isEnough(strPrefix, match)) {
                        return true;
                    }
                }
                if (needVars && CsmKindUtilities.isEnumerator(elem)) {
                    if (resImpl.fileLocalEnumerators == null) {
                        resImpl.fileLocalEnumerators = new ArrayList<CsmEnumerator>();
                    }
                    resImpl.fileLocalEnumerators.add((CsmEnumerator) elem);
                    if (isEnough(strPrefix, match)) {
                        return true;
                    }
                }
                if (needVars && CsmKindUtilities.isFunction(elem)) {
                    if (resImpl.fileLocalFunctions == null) {
                        resImpl.fileLocalFunctions = new ArrayList<CsmFunction>();
                    }
                    resImpl.fileLocalFunctions.add((CsmFunction) elem);
                    if (isEnough(strPrefix, match)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkNamespaceDeclarations() {
        switch (file.getFileType()) {
            case SOURCE_C_FILE:
            case SOURCE_FORTRAN_FILE:
                return false;
        }
        return true;
    }

    private boolean resolveContext(CsmProject prj, ResultImpl resImpl, CsmContext context, int offset, String strPrefix, boolean match) {
        CsmFunction fun = CsmContextUtilities.getFunction(context, true);
        if (needLocalVars(context, offset)) {
            if (resImpl.fileLocalEnumerators == null) {
                resImpl.fileLocalEnumerators = new ArrayList<CsmEnumerator>();
            }
            resImpl.fileLocalEnumerators.addAll(contResolver.getFileLocalEnumerators(context, strPrefix, match));
            if (isEnough(strPrefix, match, resImpl.fileLocalEnumerators)) {
                return true;
            }
            boolean staticContext = fun == null ? true : CsmBaseUtilities.isStaticContext(fun);

            if (needClassElements(context, offset)) {
                boolean inspectOuterClasses = true;
                CsmOffsetableDeclaration contextDeclaration = fun;
                //if (fun == null) System.err.printf("\nFunction is null. Offset: %d Context:\n%s \n", offset, context.toString());
                CsmClass clazz = (fun == null) ? null : CsmBaseUtilities.getFunctionClass(fun);
                clazz = clazz != null ? clazz : CsmContextUtilities.getClass(context, false, true);
                if(clazz == null) {
                    // variable like function definition (IZ#159422)
                    clazz = CsmBaseUtilities.getFunctionClassByQualifiedName(fun);
                    if(clazz != null) {
                        CsmFilter filter = CsmSelect.getFilterBuilder().createNameFilter(fun.getName(), true, caseSensitive, false);
                        Iterator<CsmMember> classMembers = CsmSelect.getClassMembers(clazz, filter);
                        for (Iterator<CsmMember> it = classMembers; it.hasNext();) {
                            CsmMember member = it.next();
                            if(CsmKindUtilities.isVariable(member)) {
                                contextDeclaration = member;
                                staticContext = member.isStatic();
                            }
                        }
                    }
                }

                if (clazz != null) {
                    if (tryInsertCurrentClass(clazz, resImpl, context, offset, strPrefix, match)) {
                        return true;
                    }

                    // get class variables visible in this method
                    if (needClassFields(context, offset)) {
                        resImpl.classFields = contResolver.getFields(clazz, contextDeclaration, strPrefix, staticContext, match, true, inspectOuterClasses, false);
                        if (isEnough(strPrefix, match, resImpl.classFields)) {
                            return true;
                        }
                    }

                    // get class enumerators visible in this method
                    if (needClassEnumerators(context, offset)) {
                        resImpl.classEnumerators = contResolver.getEnumerators(clazz, contextDeclaration, strPrefix, match, true, inspectOuterClasses, false);
                        if (isEnough(strPrefix, match, resImpl.classEnumerators)) {
                            return true;
                        }
                    }

                    // get class methods visible in this method
                    if (needClassMethods(context, offset)) {
                        resImpl.classMethods = contResolver.getMethods(clazz, contextDeclaration, strPrefix, staticContext, match, true, inspectOuterClasses, false);
                        if (isEnough(strPrefix, match, resImpl.classMethods)) {
                            return true;
                        }
                    }

                    if (needNestedClassifiers(context, offset)) {
                        // get class nested classifiers visible in this context
                        resImpl.classesEnumsTypedefs = append(resImpl.classesEnumsTypedefs, contResolver.getNestedClassifiers(clazz, contextDeclaration, strPrefix, match, needClasses(context, offset), inspectOuterClasses, resolveContextMode));
                        if (isEnough(strPrefix, match, resImpl.classesEnumsTypedefs)) {
                            return true;
                        }
                    }
                }
            }
        } else if (needClassElements(context, offset)) {
            CsmClass clazz = fun == null ? null : CsmBaseUtilities.getFunctionClass(fun);
            clazz = clazz != null ? clazz : CsmContextUtilities.getClass(context, false, true);
            if (clazz != null) {
                boolean staticContext = false;

                if (tryInsertCurrentClass(clazz, resImpl, context, offset, strPrefix, match)) {
                    return true;
                }

                // get class methods visible in this method
                CsmOffsetableDeclaration contextDeclaration = fun != null ? fun : clazz;
                // if we in resolving mode => use 2 phases
                // in the first phase we analyze only the current class
                // in the second phase we analyze outer and parent classes
                for (int phase = match ? 0 : 1; phase < 2; phase++) {
                    boolean inspectOuterAndParentClasses = (phase == 1);
                    if (needClassMethods(context, offset)) {
                        resImpl.classMethods = contResolver.getMethods(clazz, contextDeclaration, strPrefix, staticContext, match, inspectOuterAndParentClasses, inspectOuterAndParentClasses, false);
                        if (isEnough(strPrefix, match, resImpl.classMethods)) {
                            return true;
                        }
                    }
                    if (needClassFields(context, offset)) {
                        // get class variables visible in this context
                        resImpl.classFields = contResolver.getFields(clazz, contextDeclaration, strPrefix, staticContext, match, inspectOuterAndParentClasses, inspectOuterAndParentClasses, false);
                        if (isEnough(strPrefix, match, resImpl.classFields)) {
                            return true;
                        }
                    }
                    if (needClassEnumerators(context, offset)) {
                        // get class enumerators visible in this context
                        resImpl.classEnumerators = contResolver.getEnumerators(clazz, contextDeclaration, strPrefix, match, inspectOuterAndParentClasses, inspectOuterAndParentClasses, false);
                        if (isEnough(strPrefix, match, resImpl.classEnumerators)) {
                            return true;
                        }
                    }
                    if (needNestedClassifiers(context, offset)) {
                        // get class nested classifiers visible in this context
                        resImpl.classesEnumsTypedefs = append(resImpl.classesEnumsTypedefs, contResolver.getNestedClassifiers(clazz, contextDeclaration, strPrefix, match, inspectOuterAndParentClasses, inspectOuterAndParentClasses, resolveContextMode));
                        if (isEnough(strPrefix, match, resImpl.classesEnumsTypedefs)) {
                            return true;
                        }
                    }
                }
            }
        }
        if (needTemplateParameters(context, offset)) {
            resImpl.templateParameters = getTemplateParameters(context, strPrefix, match, offset);
            if (isEnough(strPrefix, match, resImpl.templateParameters)) {
                return true;
            }
        }
        if (needClasses(context, offset) || needContextClasses(context, offset)) {
            // list of classesEnumsTypedefs
            Collection<CsmClassifier> classesEnums = getClassesEnums(context, prj, strPrefix, match, offset, !needClasses(context, offset));
            Collection<CsmClassifier> visibleClassesEnums = new ArrayList<CsmClassifier>();
            if (isEnoughAfterFilterVisibileObjects(strPrefix, match, classesEnums, visibleClassesEnums)) {
                resImpl.classesEnumsTypedefs = append(resImpl.classesEnumsTypedefs, visibleClassesEnums);
                return true;
            } else {
                // we need to keep found classes even when they are not visible
                // i.e. we are in completion mode or
                // later on it will be checked against lib classes
                resImpl.classesEnumsTypedefs = append(resImpl.classesEnumsTypedefs, classesEnums);
            }
        }
        if (needFileLocalMacros(context, offset)) {
            resImpl.fileLocalMacros = contResolver.getFileLocalMacros(context, strPrefix, match);
            if (isEnough(strPrefix, match, resImpl.fileLocalMacros)) {
                return true;
            }
        }
        if (needFileLocalFunctions(context, offset)) {
            if (resImpl.fileLocalFunctions == null) {
                resImpl.fileLocalFunctions = new ArrayList<CsmFunction>();
            }
            resImpl.fileLocalFunctions.addAll(getFileLocalFunctions(context, strPrefix, match));
            if (isEnough(strPrefix, match, resImpl.fileLocalFunctions)) {
                return true;
            }
        }
        // file local variables
        if (needFileLocalVars(context, offset)) {
            if (fileReferncesContext != null && !fileReferncesContext.isCleaned()) {
                fileReferncesContext.advance(offset);
            }
            resImpl.fileLocalVars = contResolver.getFileLocalVariables(context, fileReferncesContext, strPrefix, match, queryScope == QueryScope.LOCAL_QUERY || queryScope == QueryScope.GLOBAL_QUERY);
            if (isEnough(strPrefix, match, resImpl.fileLocalVars)) {
                return true;
            }
            if (resImpl.fileLocalEnumerators == null) {
                resImpl.fileLocalEnumerators = contResolver.getFileLocalEnumerators(context, strPrefix, match);
                if (isEnough(strPrefix, match, resImpl.fileLocalEnumerators)) {
                    return true;
                }
            }
        }

        if (needFileIncludedMacros(context, offset)) {
            if (fileReferncesContext != null && !fileReferncesContext.isCleaned()) {
                fileReferncesContext.advance(offset);
                CsmMacro macro = fileReferncesContext.findIncludedMacro(strPrefix);
                if (macro != null) {
                    resImpl.fileProjectMacros = new ArrayList<CsmMacro>(1);
                    resImpl.fileProjectMacros.add(macro);
                }
                if (isEnough(strPrefix, match, resImpl.fileProjectMacros)) {
                    return true;
                }
            } else {
                resImpl.fileProjectMacros = contResolver.getFileIncludedProjectMacros(context, strPrefix, match);
            }
        }
        if (needFileIncludedLibMacros(context, offset)) {
            if (fileReferncesContext != null && !fileReferncesContext.isCleaned()) {
                fileReferncesContext.advance(offset);
                CsmMacro macro = fileReferncesContext.findIncludedMacro(strPrefix);
                if (macro != null) {
                    resImpl.fileLibMacros = new ArrayList<CsmMacro>(1);
                    resImpl.fileLibMacros.add(macro);
                }
            } else {
                resImpl.fileLibMacros = contResolver.getFileIncludeLibMacros(context, strPrefix, match);
            }
            if (isEnough(strPrefix, match, resImpl.fileLibMacros)) {
                return true;
            }
        }
        if (needGlobalMacros(context, offset)) {
            resImpl.globProjectMacros = contResolver.getProjectMacros(context, strPrefix, match);
            if (isEnough(strPrefix, match, resImpl.globProjectMacros)) {
                return true;
            }
        }
        if (needGlobalLibMacros(context, offset)) {
            resImpl.globLibMacros = contResolver.getLibMacros(context, strPrefix, match);
            if (isEnough(strPrefix, match, resImpl.globLibMacros)) {
                return true;
            }
        }

        if (needGlobalVariables(context, offset)) {
            resImpl.globVars = getGlobalVariables(context, prj, strPrefix, match, offset);
            if (isEnough(strPrefix, match, resImpl.globVars)) {
                return true;
            }
        }
        if (needGlobalEnumerators(context, offset)) {
            resImpl.globEnumerators = getGlobalEnumerators(context, prj, strPrefix, match, offset);
            if (isEnough(strPrefix, match, resImpl.globEnumerators)) {
                return true;
            }
        }
        if (needGlobalFunctions(context, offset)) {
            resImpl.globFuns = getGlobalFunctions(context, prj, strPrefix, match, offset);
            if (isEnough(strPrefix, match, resImpl.globFuns)) {
                return true;
            }
        }
        if (needGlobalNamespaces(context, offset)) {
            resImpl.projectNsAliases = getProjectNamespaceAliases(context, prj, strPrefix, match, offset);
            if (isEnough(strPrefix, match, resImpl.projectNsAliases)) {
                return true;
            }
            resImpl.globProjectNSs = getGlobalNamespaces(context, prj, strPrefix, match, offset);
            if (isEnough(strPrefix, match, resImpl.globProjectNSs)) {
                return true;
            }
        }

        if (needLibClasses(context, offset)) {
            if (resImpl.libClasses == null) {
                resImpl.libClasses = new ArrayList<CsmClassifier>();
            }
            Collection<CsmClassifier> libClassesEnums = getLibClassesEnums(prj, strPrefix, match);
            Collection<CsmClassifier> visibleClassesEnums = new ArrayList<CsmClassifier>();
            if (isEnoughAfterFilterVisibileObjects(strPrefix, match, libClassesEnums, visibleClassesEnums)) {
                // we found better classifier in libraries, clear project ones
                if (resImpl.classesEnumsTypedefs != null) {
                    resImpl.classesEnumsTypedefs.clear();
                }
                resImpl.libClasses.addAll(visibleClassesEnums);
                return true;
            } else {
                // we need to keep found classes even when they are not visible
                // i.e. we are in completion mode or
                // may be something is broken with #include resolving...
                resImpl.libClasses.addAll(libClassesEnums);
            }
        }
        if (needLibVariables(context, offset)) {
            resImpl.libVars = getLibVariables(prj, strPrefix, match);
            if (isEnough(strPrefix, match, resImpl.libVars)) {
                return true;
            }
        }
        if (needLibEnumerators(context, offset)) {
            resImpl.libEnumerators = getLibEnumerators(prj, strPrefix, match);
            if (isEnough(strPrefix, match, resImpl.libEnumerators)) {
                return true;
            }
        }
        if (needLibFunctions(context, offset)) {
            resImpl.libFuns = getLibFunctions(prj, strPrefix, match);
            if (isEnough(strPrefix, match, resImpl.libFuns)) {
                return true;
            }
        }
        if (needLibNamespaces(context, offset)) {
            resImpl.libNSs = getLibNamespaces(prj, strPrefix, match);
            if (isEnough(strPrefix, match, resImpl.libNSs)) {
                return true;
            }
//            libNsAliases = getLibNamespaceAliases(prj, strPrefix, match, offset);
        }
        return false;
    }
    
    private static <T> Collection<T> append(Collection<T> current, T elem) {
        LinkedHashSet<T> result;
        if (current == null) {
            result = new LinkedHashSet<>();
        } else {
            result = new LinkedHashSet<>(current);
        }
        result.add(elem);
        return result;
    }
    
    private static <T> Collection<T> append(Collection<T> current, Collection<T> toAdd) {
        LinkedHashSet<T> result;
        if (current == null) {
            result = new LinkedHashSet<>();
        } else {
            result = new LinkedHashSet<>(current);
        }
        result.addAll(toAdd);
        return result;
    }

    /**
     * If we are inside class and class name matches strPrefix,
     * we should add class to the result as elements inside class could
     * not have the same name.
     * The only exception is constructor, but we prefer class in such case
     *
     * @param clazz
     * @param resImpl
     * @param context
     * @param offset
     * @param strPrefix
     * @param match
     *
     * @return true if it is enough resolving context
     */
    private boolean tryInsertCurrentClass(CsmClass clazz, ResultImpl resImpl, CsmContext context, int offset, String strPrefix, boolean match) {
        if (needClasses(context, offset) && CsmOffsetUtilities.isInObject(clazz, offset)) {
            if (CsmSortUtilities.matchName(clazz.getName(), strPrefix, match, caseSensitive)) {
                // if inside type and class name matches given string
                // we should add class to resolve ambiguity class/constructor in favor of class
                resImpl.classesEnumsTypedefs = append(resImpl.classesEnumsTypedefs, clazz);
            }
            
            if (isEnough(strPrefix, match, resImpl.classesEnumsTypedefs)) {
                return true;
            }
        }
        return false;
    }

    private static int initHideMask(final CsmContext context, final int offset, final int resolveTypes,
            final QueryScope queryScope, final String strPrefix, boolean match, boolean inIncludeDirective) {
        int hideTypes = inIncludeDirective ? RESOLVE_MACROS : ~RESOLVE_NONE;
        // do not provide libraries data and global data when just resolve context with empty prefix
        if ((resolveTypes & RESOLVE_CONTEXT) == RESOLVE_CONTEXT && strPrefix.length() == 0) {
            hideTypes &= ~RESOLVE_LIB_ELEMENTS;
            // if not in exact file scope do not provide project globals
            if (!CsmKindUtilities.isFile(context.getLastScope())) {
                hideTypes &= ~RESOLVE_GLOB_MACROS;
            }
        }
        if (queryScope == QueryScope.LOCAL_QUERY || queryScope == QueryScope.SMART_QUERY) {
            // hide all lib context
            hideTypes &= ~RESOLVE_LIB_ELEMENTS;

            // hide some project context
            hideTypes &= ~RESOLVE_GLOB_MACROS;
            hideTypes &= ~RESOLVE_FILE_PRJ_MACROS;
            hideTypes &= ~RESOLVE_GLOB_NAMESPACES;
            hideTypes &= ~RESOLVE_CLASSES;
            hideTypes &= ~RESOLVE_GLOB_VARIABLES;
            hideTypes &= ~RESOLVE_GLOB_FUNCTIONS;
            hideTypes &= ~RESOLVE_GLOB_ENUMERATORS;
        }
        // for local query hide some more elements as well
        if (queryScope == QueryScope.LOCAL_QUERY) {
            hideTypes &= ~RESOLVE_CLASS_FIELDS;
            hideTypes &= ~RESOLVE_CLASS_METHODS;
            hideTypes &= ~RESOLVE_CLASS_NESTED_CLASSIFIERS;
            hideTypes &= ~RESOLVE_CLASS_ENUMERATORS;
        }
        // if exact match, we shouldn't hide classes and lib classes
        if (match) {
            hideTypes |= RESOLVE_CLASSES;
            hideTypes |= RESOLVE_LIB_CLASSES;
        }
        if (CsmContextUtilities.isInSimpleType(context, offset)) {
            hideTypes &= ~RESOLVE_CLASS_FIELDS;
            hideTypes &= ~RESOLVE_CLASS_METHODS;
            hideTypes &= ~RESOLVE_CLASS_ENUMERATORS;
        }
        if (CsmContextUtilities.isInForwardDeclaration(context, offset)) {
            hideTypes &= ~RESOLVE_CLASS_FIELDS;
            hideTypes &= ~RESOLVE_CLASS_METHODS;
        }
        return hideTypes;
    }

    private static Result buildResult(CsmContext context, ResultImpl out) {
        // local vars
        int fullSize = 0;
        if (DEBUG || STAT_COMPLETION) {
            fullSize += trace(out.localVars, "Local variables"); //NOI18N
        }
        // add class fields
        if (DEBUG || STAT_COMPLETION) {
            fullSize += trace(out.classFields, "Class fields"); //NOI18N
        }
        // add class enumerators
        if (DEBUG || STAT_COMPLETION) {
            fullSize += trace(out.classEnumerators, "Class enumerators"); //NOI18N
        }
        // add class methods
        if (DEBUG || STAT_COMPLETION) {
            fullSize += trace(out.classMethods, "Class methods"); //NOI18N
        }
        // add classesEnumsTypedefs
        if (DEBUG || STAT_COMPLETION) {
            fullSize += trace(out.classesEnumsTypedefs, "Classes/Enums/Typedefs"); //NOI18N
        }
        // add file local variables
        if (DEBUG || STAT_COMPLETION) {
            fullSize += trace(out.fileLocalVars, "File Local Variables"); //NOI18N
        }
        if (DEBUG || STAT_COMPLETION) {
            fullSize += trace(out.fileLocalEnumerators, "File Local Enumerators"); //NOI18N
        }
        if (DEBUG || STAT_COMPLETION) {
            fullSize += trace(out.fileLocalMacros, "File Local Macros"); //NOI18N
        }
        if (DEBUG || STAT_COMPLETION) {
            fullSize += trace(out.fileLocalFunctions, "File Local Functions"); //NOI18N
        }
        removeForwards(out.classesEnumsTypedefs);
        // remove local macros from project included macros
        remove(out.fileProjectMacros, out.fileLocalMacros);
        if (DEBUG || STAT_COMPLETION) {
            fullSize += trace(out.fileProjectMacros, "File Included Project Macros"); //NOI18N
        }
        // add global variables
        // remove file local from global list if there are intersections by names
        remove(out.globVars, out.fileLocalVars);
        if (DEBUG || STAT_COMPLETION) {
            fullSize += trace(out.globVars, "Global variables"); //NOI18N
        }
        // add global enumerators, but remove file local ones
        remove(out.globEnumerators, out.fileLocalEnumerators);
        if (DEBUG || STAT_COMPLETION) {
            fullSize += trace(out.globEnumerators, "Global enumerators"); //NOI18N
        }
        // global macros
        // remove project included macros from all other macros
        remove(out.globProjectMacros, out.fileProjectMacros);
        // remove local macros from project macros
        remove(out.globProjectMacros, out.fileLocalMacros);
        if (DEBUG || STAT_COMPLETION) {
            fullSize += trace(out.globProjectMacros, "Global Project Macros"); //NOI18N
        }
        // add global functions
        if (DEBUG || STAT_COMPLETION) {
            fullSize += trace(out.globFuns, "Global Project functions"); //NOI18N
        }
        // add namespaces
        if (DEBUG || STAT_COMPLETION) {
            fullSize += trace(out.globProjectNSs, "Global Project Namespaces"); //NOI18N
        }
        // add namespace aliases
        if (DEBUG || STAT_COMPLETION) {
            fullSize += trace(out.projectNsAliases, "Project Namespace Aliases"); //NOI18N
        }
        // add libraries classesEnumsTypedefs
        if (DEBUG || STAT_COMPLETION) {
            fullSize += trace(out.libClasses, "Library classes"); //NOI18N
        }
        if (DEBUG || STAT_COMPLETION) {
            fullSize += trace(out.fileLibMacros, "File Included Library Macros"); //NOI18N
        }
        // remove file included lib macros from all other lib macros
        remove(out.globLibMacros, out.fileLibMacros);
        if (DEBUG || STAT_COMPLETION) {
            fullSize += trace(out.globLibMacros, "Global Library Macros"); //NOI18N
        }
        // add libraries variables
        if (DEBUG || STAT_COMPLETION) {
            fullSize += trace(out.libVars, "Global Library variables"); //NOI18N
        }
        // add libraries enumerators
        if (DEBUG || STAT_COMPLETION) {
            fullSize += trace(out.libEnumerators, "Global Library enumerators"); //NOI18N
        }
        // add libraries functions
        if (DEBUG || STAT_COMPLETION) {
            fullSize += trace(out.libFuns, "Global Library functions"); //NOI18N
        }
        // add libraries namespaces
        if (DEBUG || STAT_COMPLETION) {
            fullSize += trace(out.libNSs, "Global Library Namespaces"); //NOI18N
        }
        // add libraries namespace aliases
        if (DEBUG || STAT_COMPLETION) {
            fullSize += trace(out.libNsAliases, "Global Library Namespace Aliases"); //NOI18N
        }
        // all elements info
        if (DEBUG || STAT_COMPLETION) {
            trace(null, "There are " + fullSize + " resovled elements"); //NOI18N
        }
        return out;
    }

    private static void removeForwards(Collection<CsmClassifier> classesEnumsTypedefs) {
        if (classesEnumsTypedefs != null) {
            Set<CharSequence> nonForwards = new TreeSet<>();
            boolean hasForwards = false;
            for (CsmClassifier cls : classesEnumsTypedefs) {
                if (CsmBaseUtilitiesProvider.getDefault().isDummyForwardClass(cls)) {
                    hasForwards = true;
                } else {
                    CharSequence qname = cls.getQualifiedName();
                    nonForwards.add(qname);
                }
            }
            if (hasForwards) {
                for(Iterator<CsmClassifier> it = classesEnumsTypedefs.iterator(); it.hasNext(); ) {
                    CsmClassifier cls = it.next();
                    if (CsmBaseUtilitiesProvider.getDefault().isDummyForwardClass(cls)) {
                        CharSequence qname = CsmBaseUtilitiesProvider.getDefault().getDummyForwardSimpleQualifiedName(cls);
                        if (qname != null && nonForwards.contains(qname)) {
                            it.remove();
                        }
                    }   
                }
            }
        }
    }

    private static <T> Collection<T> remove(Collection<T> dest, Collection<T> removeItems) {
        CsmUtilities.<T>removeAll(dest, removeItems);
        return dest;
    }

    protected CsmProjectContentResolver createContentResolver() {
        CsmFile contextFile = this.file;
        CsmProject filePrj = contextFile.getProject();
        CsmProject startProject = filePrj;
        CsmFile startProjectFile = this.file;
        Collection<CsmProject> libs = new ArrayList<CsmProject>();
        boolean replaceProject = startProject.isArtificial();
        for (CsmCompilationUnit cu : CsmFileInfoQuery.getDefault().getCompilationUnits(file, contextOffset)) {
            CsmFile startFile = cu.getStartFile();
            CsmProject prj = startFile == null ? null : startFile.getProject();
            if (prj != null) {
                startProject = replaceProject ? prj : startProject;
                startProjectFile = startFile;
                break;
            }
        }
        // add libararies elements
        libs.addAll(startProject.getLibraries());
        CsmProjectContentResolver resolver = new CsmProjectContentResolver(startProjectFile, filePrj, isCaseSensitive(), isSortNeeded(), isNaturalSort(), libs);
        return resolver;
    }

    @SuppressWarnings("unchecked")
    private static Collection merge(Collection orig, Collection newList) {
        return CsmUtilities.merge(orig, newList);
    }

    private Collection<CsmTemplateParameter> getTemplateParameters(CsmContext context, String strPrefix, boolean match, int offset) {
        Collection<CsmTemplateParameter> templateParameters = null;
        CsmFunction fun = CsmContextUtilities.getFunction(context, false);
        Collection<CsmTemplate> analyzeTemplates = new ArrayList<CsmTemplate>();
        if (fun == null && context.getLastObject() != null) {
            // Fix for IZ#138099: unresolved identifier for functions' template parameter.
            // We might be just before function name, where its template parameters
            // and type reside. Let's try a bit harder to find that function.
            CsmObject obj = context.getLastObject();
            if (CsmKindUtilities.isFunction(obj)) {
                fun = (CsmFunction) obj;
            } else {
                obj = CsmDeclarationResolver.findInnerFileObject(file, offset, context, fileReferncesContext);
                if (CsmKindUtilities.isFunction(obj)) {
                    fun = (CsmFunction) obj;
                } else if (CsmKindUtilities.isClassForwardDeclaration(obj) || CsmKindUtilities.isEnumForwardDeclaration(obj)) {
                    if (CsmKindUtilities.isTemplate(obj)) {
                        analyzeTemplates.add((CsmTemplate) obj);
                    }
                }
            }
            if (CsmKindUtilities.isTemplate(obj) && obj != fun) {
                analyzeTemplates.add((CsmTemplate) obj);
            }
        }
        if (CsmKindUtilities.isTemplate(fun)) {
            analyzeTemplates.add((CsmTemplate) fun);
        }
        CsmClass funClass = fun == null ? null : CsmBaseUtilities.getFunctionClass(fun);
        CsmClass contextClass = CsmContextUtilities.getClass(context, false, false);
        CsmClassifier clazz = funClass != null ? funClass : contextClass;
        if (clazz == null && CsmKindUtilities.isClassForwardDeclaration(context.getLastScope())) {
            // Bug 249752. Wee need to collect template parameters from forward classifiers as well
            clazz = (CsmClassForwardDeclaration) context.getLastScope();
        }
        if (clazz != null) {
            // We add template parameters to function parameters on function init,
            // so we dont need to add them to completion list again.
            if (CsmKindUtilities.isTemplate(clazz) && !analyzeTemplates.contains((CsmTemplate)clazz)) {
                analyzeTemplates.add((CsmTemplate) clazz);
            }
            CsmScope scope = clazz.getScope();
            while (CsmKindUtilities.isClass(scope)) {
                if (CsmKindUtilities.isTemplate(scope)) {
                    analyzeTemplates.add((CsmTemplate) scope);
                }
                scope = ((CsmClass) scope).getScope();
            }
        }
        if (!analyzeTemplates.isEmpty()) {
            templateParameters = new ArrayList<CsmTemplateParameter>();
            for (CsmTemplate csmTemplate : analyzeTemplates) {
                getTemplateParameters(csmTemplate, strPrefix, match, templateParameters);
            }
        }
        return templateParameters;
    }

    private void getTemplateParameters(CsmTemplate template, String strPrefix, boolean match, Collection<CsmTemplateParameter> out) {
        for (CsmTemplateParameter elem : template.getTemplateParameters()) {
            if (CsmSortUtilities.matchName(elem.getName(), strPrefix, match, caseSensitive)) {
                out.add(elem);
            }
            if (CsmKindUtilities.isTemplate(elem)) {
                getTemplateParameters((CsmTemplate) elem, strPrefix, match, out);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<CsmClassifier> getClassesEnums(CsmContext context, CsmProject prj, String strPrefix, boolean match, int offset, boolean contextOnly) {
        if (prj == null) {
            return null;
        }
        // try to get elements from visible namespaces
        Collection<CsmNamespace> namespaces = getNamespacesToSearch(context, this.file, offset, strPrefix.length() == 0, contextOnly);
        LinkedHashSet<CsmClassifier> out = new LinkedHashSet<CsmClassifier>(1024);
        for (CsmNamespace ns : namespaces) {
            List<CsmClassifier> res = contResolver.getNamespaceClassesEnums(ns, strPrefix, match, false);
            out.addAll(res);
        }
        CsmDeclaration.Kind kinds[] = {
            CsmDeclaration.Kind.CLASS,
            CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION,
            CsmDeclaration.Kind.ENUM_FORWARD_DECLARATION,
            CsmDeclaration.Kind.STRUCT,
            CsmDeclaration.Kind.UNION,
            CsmDeclaration.Kind.ENUM,
            CsmDeclaration.Kind.TYPEDEF,
            CsmDeclaration.Kind.TYPEALIAS
        };
        if (!contextOnly && checkNamespaceDeclarations()) {
            Collection usedDecls = getUsedDeclarations(this.file, offset, strPrefix, match, kinds);
            out.addAll(usedDecls);
        }
        if(out.isEmpty() && match) {
            // Special case for nested structs in C
            // See Bug 144535 - wrong error highlighting for inner structure
            out.addAll(prj.findClassifiers(strPrefix));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private Collection<CsmVariable> getGlobalVariables(CsmContext context, CsmProject prj, String strPrefix, boolean match, int offset) {
        Collection<CsmNamespace> namespaces = getNamespacesToSearch(context, this.file, offset, strPrefix.length() == 0, false);
        LinkedHashSet<CsmVariable> out = new LinkedHashSet<CsmVariable>(1024);
        for (CsmNamespace ns : namespaces) {
            List<CsmVariable> res = contResolver.getNamespaceVariables(ns, strPrefix, match, false);
            out.addAll(res);
        }
        CsmDeclaration.Kind kinds[] = {
            CsmDeclaration.Kind.VARIABLE
        };
        if (checkNamespaceDeclarations()) {
            Collection usedDecls = getUsedDeclarations(this.file, offset, strPrefix, match, kinds);
            out.addAll(usedDecls);
        }
        return out;
    }

    private Collection<CsmEnumerator> getGlobalEnumerators(CsmContext context, CsmProject prj, String strPrefix, boolean match, int offset) {
        if (isEnough(strPrefix, match) && fileReferncesContext != null && !fileReferncesContext.isCleaned()) {
            CsmEnumerator hotSpotEnum = fileReferncesContext.getHotSpotEnum(strPrefix);
            if (hotSpotEnum != null) {
                return Collections.singleton(hotSpotEnum);
            }
        }
        Collection<CsmNamespace> namespaces = getNamespacesToSearch(context, this.file, offset, strPrefix.length() == 0, false);
        LinkedHashSet<CsmEnumerator> out = new LinkedHashSet<CsmEnumerator>(1024);
        for (CsmNamespace ns : namespaces) {
            List<CsmEnumerator> res = contResolver.getNamespaceEnumerators(ns, strPrefix, match, false);
            out.addAll(res);
        }
        if (isEnough(strPrefix, match, out)) {
            if (fileReferncesContext != null && !fileReferncesContext.isCleaned()) {
                fileReferncesContext.putHotSpotEnum(out);
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private Collection<CsmFunction> getGlobalFunctions(CsmContext context, CsmProject prj, String strPrefix, boolean match, int offset) {
        Collection<CsmNamespace> namespaces = getNamespacesToSearch(context, this.file, offset, strPrefix.length() == 0, false);
        LinkedHashSet<CsmFunction> out = new LinkedHashSet<CsmFunction>(1024);
        for (CsmNamespace ns : namespaces) {
            List<CsmFunction> res = contResolver.getNamespaceFunctions(ns, strPrefix, match, false);
            out.addAll(res);
        }
        CsmDeclaration.Kind kinds[] = {
            CsmDeclaration.Kind.FUNCTION,
            CsmDeclaration.Kind.FUNCTION_DEFINITION,
            CsmDeclaration.Kind.FUNCTION_FRIEND,
            CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION
        };
        if (checkNamespaceDeclarations()) {
            Collection usedDecls = getUsedDeclarations(this.file, offset, strPrefix, match, kinds);
            out.addAll(usedDecls);
        }
        return out;
    }

    private Collection<CsmFunction> getFileLocalFunctions(CsmContext context, String strPrefix, boolean match) {
        Collection<CsmFunction> res = contResolver.getFileLocalFunctions(context, strPrefix, match, queryScope == QueryScope.LOCAL_QUERY);
        return res;
    }

    private Collection<CsmNamespace> getGlobalNamespaces(CsmContext context, CsmProject prj, String strPrefix, boolean match, int offset) {
        Collection<CsmNamespace> namespaces = getNamespacesToSearch(context, this.file, offset, strPrefix.length() == 0, false);
        LinkedHashSet<CsmNamespace> out = new LinkedHashSet<CsmNamespace>(1024);
        for (CsmNamespace ns : namespaces) {
            List<CsmNamespace> res = contResolver.getNestedNamespaces(ns, strPrefix, match, false);
            out.addAll(res);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private Collection<CsmNamespaceAlias> getProjectNamespaceAliases(CsmContext context, CsmProject prj, String strPrefix, boolean match, int offset) {
        if (!checkNamespaceDeclarations()) {
            return Collections.<CsmNamespaceAlias>emptyList();
        }
        CsmProject inProject = (strPrefix.length() == 0) ? prj : null;
        Collection aliases = CsmUsingResolver.getDefault().findNamespaceAliases(this.file, offset, inProject);
        Collection out = new LinkedHashSet<CsmNamespaceAlias>();
        if (strPrefix.length() > 0) {
            out = filterDeclarations(aliases, strPrefix, match,
                    new CsmDeclaration.Kind[]{CsmDeclaration.Kind.NAMESPACE_ALIAS});
        } else {
            out.addAll(aliases);
        }

        // here we lookup aliases defined in namespaces visible at this point
        Collection<CsmNamespace> namespaces = getNamespacesToSearch(context,this.file, offset, strPrefix.length() == 0,false);
        for (CsmNamespace ns : namespaces) {
            List<CsmNamespaceAlias> aliases2 = contResolver.getNamespaceAliases(ns, strPrefix, match, match);
            for (CsmNamespaceAlias alias : aliases2) {
                if (alias.getContainingFile().equals(this.file) && alias.getEndOffset() < offset) {
                    out.add(alias);
                }
            }
        }
        return out;
    }

    private Collection<CsmClassifier> getLibClassesEnums(CsmProject prj, String strPrefix, boolean match) {
        return contResolver.getLibClassesEnums(strPrefix, match);
    }

    private Collection<CsmVariable> getLibVariables(CsmProject prj, String strPrefix, boolean match) {
        return contResolver.getLibVariables(strPrefix, match);
    }

    private Collection<CsmEnumerator> getLibEnumerators(CsmProject prj, String strPrefix, boolean match) {
        Collection<CsmEnumerator> res = null;
        if (fileReferncesContext != null && match) {
            res = fileReferncesContext.getLibEnumerators(strPrefix);
        }
        if (res == null) {
            res = contResolver.getLibEnumerators(strPrefix, match, true);
            if (fileReferncesContext != null && match) {
                fileReferncesContext.putLibEnumerators(strPrefix, res);
            }
        }
        return res;
    }

    private Collection<CsmFunction> getLibFunctions(CsmProject prj, String strPrefix, boolean match) {
        return contResolver.getLibFunctions(strPrefix, match);
    }

    private Collection<CsmNamespace> getLibNamespaces(CsmProject prj, String strPrefix, boolean match) {
        return contResolver.getLibNamespaces(strPrefix, match);
    }

    private boolean needLocalClasses(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_LOCAL_CLASSES) == RESOLVE_LOCAL_CLASSES) {
            return true;
        }
        return false;
    }

    private boolean needClasses(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_CLASSES) == RESOLVE_CLASSES) {
            return true;
        }
        return false;
    }

    private boolean needContextClasses(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_CONTEXT_CLASSES) == RESOLVE_CONTEXT_CLASSES) {
            return true;
        }
        return false;
    }

    private void updateResolveTypesInFunction(final int offset, final CsmContext context, boolean match) {

        // always resolve local classes, not only when in type
        resolveTypes |= RESOLVE_LOCAL_CLASSES;

        boolean isInType = CsmContextUtilities.isInSimpleType(context, offset);
        if (!isInType) {
            resolveTypes |= RESOLVE_FILE_LOCAL_VARIABLES;
            resolveTypes |= RESOLVE_LOCAL_VARIABLES;
            resolveTypes |= RESOLVE_GLOB_VARIABLES;
            resolveTypes |= RESOLVE_GLOB_ENUMERATORS;
            resolveTypes |= RESOLVE_CLASS_FIELDS;
            resolveTypes |= RESOLVE_CLASS_ENUMERATORS;
            resolveTypes |= RESOLVE_LIB_ENUMERATORS;
        }
        if (CsmContextUtilities.isInFunctionBodyOrInitializerListOrCastOperatorType(context, offset)) {
            if (!isInType || !match) {
                resolveTypes |= RESOLVE_LIB_VARIABLES;
                resolveTypes |= RESOLVE_GLOB_FUNCTIONS;
                resolveTypes |= RESOLVE_FILE_LOCAL_FUNCTIONS;
                resolveTypes |= RESOLVE_LIB_FUNCTIONS;
                resolveTypes |= RESOLVE_CLASS_METHODS;
            }
            if (!match) {
                resolveTypes |= RESOLVE_FILE_LOCAL_VARIABLES;
                resolveTypes |= RESOLVE_LOCAL_VARIABLES;
                resolveTypes |= RESOLVE_GLOB_VARIABLES;
                resolveTypes |= RESOLVE_CLASS_FIELDS;
                resolveTypes |= RESOLVE_CLASS_ENUMERATORS;
            }
        }
    }

    private boolean needFileLocalVars(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_FILE_LOCAL_VARIABLES) == RESOLVE_FILE_LOCAL_VARIABLES) {
            return true;
        }
        return false;
    }

    private boolean needLocalVars(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_LOCAL_VARIABLES) == RESOLVE_LOCAL_VARIABLES) {
            return true;
        }
        return false;
    }

    private boolean needGlobalVariables(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_GLOB_VARIABLES) == RESOLVE_GLOB_VARIABLES) {
            return true;
        }
        return false;
    }

    private boolean needGlobalEnumerators(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_GLOB_ENUMERATORS) == RESOLVE_GLOB_ENUMERATORS) {
            return true;
        }
        return false;
    }

    private boolean needGlobalFunctions(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_GLOB_FUNCTIONS) == RESOLVE_GLOB_FUNCTIONS) {
            return true;
        }
        return false;
    }

    private boolean needGlobalNamespaces(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_GLOB_NAMESPACES) == RESOLVE_GLOB_NAMESPACES) {
            return true;
        }
        return false;
    }

    private boolean needFunctionVars(CsmContext context, int offset) {
        return needLocalVars(context, offset);
    }

    private boolean needLibClasses(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_LIB_CLASSES) == RESOLVE_LIB_CLASSES) {
            return true;
        }
        return false;
    }

    private boolean needLibVariables(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_LIB_VARIABLES) == RESOLVE_LIB_VARIABLES) {
            return true;
        }
        return false;
    }

    private boolean needLibEnumerators(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_LIB_ENUMERATORS) == RESOLVE_LIB_ENUMERATORS) {
            return true;
        }
        return false;
    }

    private boolean needLibFunctions(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_LIB_FUNCTIONS) == RESOLVE_LIB_FUNCTIONS) {
            return true;
        }
        return false;
    }

    private boolean needLibNamespaces(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_LIB_NAMESPACES) == RESOLVE_LIB_NAMESPACES) {
            return true;
        }
        return false;
    }

    private boolean needFileLocalMacros(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_FILE_LOCAL_MACROS) == RESOLVE_FILE_LOCAL_MACROS) {
            return true;
        }
        return false;
    }

    private boolean needFileLocalFunctions(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_FILE_LOCAL_FUNCTIONS) == RESOLVE_FILE_LOCAL_FUNCTIONS) {
            return true;
        }
        return false;
    }

    private boolean needFileIncludedMacros(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_FILE_PRJ_MACROS) == RESOLVE_FILE_PRJ_MACROS) {
            return true;
        }
        return false;
    }

    private boolean needFileIncludedLibMacros(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_FILE_LIB_MACROS) == RESOLVE_FILE_LIB_MACROS) {
            return true;
        }
        return false;
    }

    private boolean needGlobalMacros(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_GLOB_MACROS) == RESOLVE_GLOB_MACROS) {
            return true;
        }
        return false;
    }

    private boolean needGlobalLibMacros(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_LIB_MACROS) == RESOLVE_LIB_MACROS) {
            return true;
        }
        return false;
    }

    private boolean needClassMethods(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_CLASS_METHODS) == RESOLVE_CLASS_METHODS) {
            return true;
        }
        return false;
    }

    private boolean needClassFields(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_CLASS_FIELDS) == RESOLVE_CLASS_FIELDS) {
            return true;
        }
        return false;
    }

    private boolean needClassEnumerators(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_CLASS_ENUMERATORS) == RESOLVE_CLASS_ENUMERATORS) {
            return true;
        }
        return false;
    }

    private boolean needNestedClassifiers(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_CLASS_NESTED_CLASSIFIERS) == RESOLVE_CLASS_NESTED_CLASSIFIERS) {
            return true;
        }
        return false;
    }

    private boolean needClassElements(CsmContext context, int offset) {
        if (((hideTypes & resolveTypes & RESOLVE_CLASS_METHODS) == RESOLVE_CLASS_METHODS) ||
                ((hideTypes & resolveTypes & RESOLVE_CLASS_FIELDS) == RESOLVE_CLASS_FIELDS) ||
                ((hideTypes & resolveTypes & RESOLVE_CLASS_NESTED_CLASSIFIERS) == RESOLVE_CLASS_NESTED_CLASSIFIERS) ||
                ((hideTypes & resolveTypes & RESOLVE_CLASS_ENUMERATORS) == RESOLVE_CLASS_ENUMERATORS)) {
            return true;
        }
        return false;
    }

    private boolean needTemplateParameters(CsmContext context, int offset) {
        if ((hideTypes & resolveTypes & RESOLVE_TEMPLATE_PARAMETERS) == RESOLVE_TEMPLATE_PARAMETERS) {
            return true;
        }
        return false;
    }

    // ====================== Debug support ===================================
    private static int trace(Collection<? extends CsmObject> list, String msg) {
        System.err.println("\t" + msg + " [size - " + (list == null ? "null" : list.size()) + "]"); //NOI18N
        if (list == null) {
            return 0;
        }
        if (TRACE) {
            int i = 0;
            for (CsmObject elem : list) {
                System.err.println("\t\t[" + i + "]" + CsmUtilities.getCsmName(elem)); //NOI18N
                i++;
            }
        }
        return list.size();
    }

    private static final class ResultImpl implements Result {
        
        private CsmContext context;

        private Collection<CsmVariable> localVars;
        private Collection<CsmField> classFields;
        private Collection<CsmEnumerator> classEnumerators;
        private Collection<CsmMethod> classMethods;
        private Collection<CsmClassifier> classesEnumsTypedefs;
        private Collection<CsmVariable> fileLocalVars;
        private Collection<CsmEnumerator> fileLocalEnumerators;
        private Collection<CsmMacro> fileLocalMacros;
        private Collection<CsmFunction> fileLocalFunctions;
        private Collection<CsmMacro> fileProjectMacros;
        private Collection<CsmVariable> globVars;
        private Collection<CsmEnumerator> globEnumerators;
        private Collection<CsmMacro> globProjectMacros;
        private Collection<CsmFunction> globFuns;
        private Collection<CsmNamespace> globProjectNSs;
        private Collection<CsmNamespaceAlias> projectNsAliases;
        private Collection<CsmClassifier> libClasses;
        private Collection<CsmMacro> fileLibMacros;
        private Collection<CsmMacro> globLibMacros;
        private Collection<CsmVariable> libVars;
        private Collection<CsmEnumerator> libEnumerators;
        private Collection<CsmFunction> libFuns;
        private Collection<CsmNamespace> libNSs;
        private Collection<CsmNamespaceAlias> libNsAliases;
        private Collection<CsmTemplateParameter> templateParameters;

        private ResultImpl(CsmContext context) {
            this.context = context;
        }

        @Override
        public Collection<CsmVariable> getLocalVariables() {
            return CompletionResolverImpl.<CsmVariable>maskNull(localVars);
        }

        @Override
        public Collection<CsmField> getClassFields() {
            return CompletionResolverImpl.<CsmField>maskNull(classFields);
        }

        @Override
        public Collection<CsmEnumerator> getClassEnumerators() {
            return CompletionResolverImpl.<CsmEnumerator>maskNull(classEnumerators);
        }

        @Override
        public Collection<CsmMethod> getClassMethods() {
            return CompletionResolverImpl.<CsmMethod>maskNull(classMethods);
        }

        @Override
        public Collection<CsmClassifier> getProjectClassesifiersEnums() {
            return CompletionResolverImpl.<CsmClassifier>maskNull(classesEnumsTypedefs);
        }

        @Override
        public Collection<CsmVariable> getFileLocalVars() {
            return CompletionResolverImpl.<CsmVariable>maskNull(fileLocalVars);
        }

        @Override
        public Collection<CsmEnumerator> getFileLocalEnumerators() {
            return CompletionResolverImpl.<CsmEnumerator>maskNull(fileLocalEnumerators);
        }

        @Override
        public Collection<CsmMacro> getFileLocalMacros() {
            return CompletionResolverImpl.<CsmMacro>maskNull(fileLocalMacros);
        }

        @Override
        public Collection<CsmFunction> getFileLocalFunctions() {
            return CompletionResolverImpl.<CsmFunction>maskNull(fileLocalFunctions);
        }

        @Override
        public Collection<CsmMacro> getInFileIncludedProjectMacros() {
            return CompletionResolverImpl.<CsmMacro>maskNull(fileProjectMacros);
        }

        @Override
        public Collection<CsmVariable> getGlobalVariables() {
            return CompletionResolverImpl.<CsmVariable>maskNull(globVars);
        }

        @Override
        public Collection<CsmEnumerator> getGlobalEnumerators() {
            return CompletionResolverImpl.<CsmEnumerator>maskNull(globEnumerators);
        }

        @Override
        public Collection<CsmMacro> getGlobalProjectMacros() {
            return CompletionResolverImpl.<CsmMacro>maskNull(globProjectMacros);
        }

        @Override
        public Collection<CsmFunction> getGlobalProjectFunctions() {
            return CompletionResolverImpl.<CsmFunction>maskNull(globFuns);
        }

        @Override
        public Collection<CsmNamespace> getGlobalProjectNamespaces() {
            return CompletionResolverImpl.<CsmNamespace>maskNull(globProjectNSs);
        }

        @Override
        public Collection<CsmNamespaceAlias> getProjectNamespaceAliases() {
            return CompletionResolverImpl.<CsmNamespaceAlias>maskNull(projectNsAliases);
        }

        @Override
        public Collection<CsmClassifier> getLibClassifiersEnums() {
            return CompletionResolverImpl.<CsmClassifier>maskNull(libClasses);
        }

        @Override
        public Collection<CsmMacro> getInFileIncludedLibMacros() {
            return CompletionResolverImpl.<CsmMacro>maskNull(fileLibMacros);
        }

        @Override
        public Collection<CsmMacro> getLibMacros() {
            return CompletionResolverImpl.<CsmMacro>maskNull(globLibMacros);
        }

        @Override
        public Collection<CsmVariable> getLibVariables() {
            return CompletionResolverImpl.<CsmVariable>maskNull(libVars);
        }

        @Override
        public Collection<CsmEnumerator> getLibEnumerators() {
            return CompletionResolverImpl.<CsmEnumerator>maskNull(libEnumerators);
        }

        @Override
        public Collection<CsmFunction> getLibFunctions() {
            return CompletionResolverImpl.<CsmFunction>maskNull(libFuns);
        }

        @Override
        public Collection<CsmNamespace> getLibNamespaces() {
            return CompletionResolverImpl.<CsmNamespace>maskNull(libNSs);
        }

        @Override
        public Collection<CsmNamespaceAlias> getLibNamespaceAliases() {
            return CompletionResolverImpl.<CsmNamespaceAlias>maskNull(libNsAliases);
        }

        @Override
        public Collection<CsmTemplateParameter> getTemplateparameters() {
            return CompletionResolverImpl.<CsmTemplateParameter>maskNull(templateParameters);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Collection<? extends CsmObject> addResulItemsToCol(Collection<? extends CsmObject> orig) {
            assert orig != null;
            return appendResult(orig, this);
        }
        int size = -1;

        @Override
        public int size() {
            if (size == -1) {
                size = 0;
                // init size value
                size += getLocalVariables().size();

                size += getClassFields().size();

                size += getClassEnumerators().size();

                size += getClassMethods().size();

                size += getProjectClassesifiersEnums().size();

                size += getFileLocalVars().size();

                size += getFileLocalEnumerators().size();

                size += getFileLocalMacros().size();

                size += getFileLocalFunctions().size();

                size += getInFileIncludedProjectMacros().size();

                size += getGlobalVariables().size();

                size += getGlobalEnumerators().size();

                size += getGlobalProjectMacros().size();

                size += getGlobalProjectFunctions().size();

                size += getGlobalProjectNamespaces().size();

                size += getLibClassifiersEnums().size();

                size += getInFileIncludedLibMacros().size();

                size += getLibMacros().size();

                size += getLibVariables().size();

                size += getLibEnumerators().size();

                size += getLibFunctions().size();

                size += getLibNamespaces().size();

                size += getTemplateparameters().size();
            }
            return size;
        }

        @Override
        public CsmContext getContext() {
            return context;
        }

        @Override
        public String toString() {
            Collection<? extends CsmObject> coll = new ArrayList<CsmObject>();
            addResulItemsToCol(coll);
            return coll.toString();
        }
    }
    private static final Result EMPTY_RESULT = new EmptyResultImpl();

    private static final class EmptyResultImpl implements Result {

        @Override
        public Collection<CsmVariable> getLocalVariables() {
            return Collections.<CsmVariable>emptyList();
        }

        @Override
        public Collection<CsmField> getClassFields() {
            return Collections.<CsmField>emptyList();
        }

        @Override
        public Collection<CsmEnumerator> getClassEnumerators() {
            return Collections.<CsmEnumerator>emptyList();
        }

        @Override
        public Collection<CsmMethod> getClassMethods() {
            return Collections.<CsmMethod>emptyList();
        }

        @Override
        public Collection<CsmClassifier> getProjectClassesifiersEnums() {
            return Collections.<CsmClassifier>emptyList();
        }

        @Override
        public Collection<CsmVariable> getFileLocalVars() {
            return Collections.<CsmVariable>emptyList();
        }

        @Override
        public Collection<CsmEnumerator> getFileLocalEnumerators() {
            return Collections.<CsmEnumerator>emptyList();
        }

        @Override
        public Collection<CsmMacro> getFileLocalMacros() {
            return Collections.<CsmMacro>emptyList();
        }

        @Override
        public Collection<CsmFunction> getFileLocalFunctions() {
            return Collections.<CsmFunction>emptyList();
        }

        @Override
        public Collection<CsmMacro> getInFileIncludedProjectMacros() {
            return Collections.<CsmMacro>emptyList();
        }

        @Override
        public Collection<CsmVariable> getGlobalVariables() {
            return Collections.<CsmVariable>emptyList();
        }

        @Override
        public Collection<CsmEnumerator> getGlobalEnumerators() {
            return Collections.<CsmEnumerator>emptyList();
        }

        @Override
        public Collection<CsmMacro> getGlobalProjectMacros() {
            return Collections.<CsmMacro>emptyList();
        }

        @Override
        public Collection<CsmFunction> getGlobalProjectFunctions() {
            return Collections.<CsmFunction>emptyList();
        }

        @Override
        public Collection<CsmNamespace> getGlobalProjectNamespaces() {
            return Collections.<CsmNamespace>emptyList();
        }

        @Override
        public Collection<CsmClassifier> getLibClassifiersEnums() {
            return Collections.<CsmClassifier>emptyList();
        }

        @Override
        public Collection<CsmMacro> getInFileIncludedLibMacros() {
            return Collections.<CsmMacro>emptyList();
        }

        @Override
        public Collection<CsmMacro> getLibMacros() {
            return Collections.<CsmMacro>emptyList();
        }

        @Override
        public Collection<CsmVariable> getLibVariables() {
            return Collections.<CsmVariable>emptyList();
        }

        @Override
        public Collection<CsmEnumerator> getLibEnumerators() {
            return Collections.<CsmEnumerator>emptyList();
        }

        @Override
        public Collection<CsmFunction> getLibFunctions() {
            return Collections.<CsmFunction>emptyList();
        }

        @Override
        public Collection<CsmNamespace> getLibNamespaces() {
            return Collections.<CsmNamespace>emptyList();
        }

        @Override
        public Collection<? extends CsmObject> addResulItemsToCol(Collection<? extends CsmObject> orig) {
            return orig;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public CsmContext getContext() {
            return null;
        }

        @Override
        public Collection<CsmNamespaceAlias> getProjectNamespaceAliases() {
            return Collections.<CsmNamespaceAlias>emptyList();
        }

        @Override
        public Collection<CsmNamespaceAlias> getLibNamespaceAliases() {
            return Collections.<CsmNamespaceAlias>emptyList();
        }

        @Override
        public Collection<CsmTemplateParameter> getTemplateparameters() {
            return Collections.<CsmTemplateParameter>emptyList();
        }

        @Override
        public String toString() {
            return "<Empty Result>"; // NOI18N
        }
    }

    private static <T> Collection<T> maskNull(Collection<T> list) {
        return list != null ? list : Collections.<T>emptyList();
    }

    private static <T> Collection appendResult(Collection<T> dest, ResultImpl result) {
        assert dest != null;
        // local vars
        merge(dest, result.localVars);
        // add class fields
        merge(dest, result.classFields);
        // add class enumerators
        merge(dest, result.classEnumerators);
        // add class methods
        merge(dest, result.classMethods);
        // add classesEnumsTypedefs
        merge(dest, result.classesEnumsTypedefs);
        // add file local variables
        merge(dest, result.fileLocalVars);
        merge(dest, result.fileLocalEnumerators);
        merge(dest, result.fileLocalMacros);
        merge(dest, result.fileLocalFunctions);
        merge(dest, result.fileProjectMacros);
        // add global variables
        merge(dest, result.globVars);
        // add global enumerators
        merge(dest, result.globEnumerators);
        // global macros
        merge(dest, result.globProjectMacros);
        // add global functions
        merge(dest, result.globFuns);
        // add namespaces
        merge(dest, result.globProjectNSs);
        // add namespace aliases
        merge(dest, result.projectNsAliases);
        // add libraries classesEnumsTypedefs
        merge(dest, result.libClasses);
        merge(dest, result.fileLibMacros);
        merge(dest, result.globLibMacros);
        // add libraries variables
        merge(dest, result.libVars);
        // add libraries enumerators
        merge(dest, result.libEnumerators);
        // add libraries functions
        merge(dest, result.libFuns);
        // add libraries namespaces
        merge(dest, result.libNSs);
        // add libraries namespace aliases
        merge(dest, result.libNsAliases);
        // add template parameters
        merge(dest, result.templateParameters);

        return dest;
    }

    private void initResolveMask(final CsmContext context, int offset, final String strPrefix, boolean match) {
        if ((resolveTypes & RESOLVE_CONTEXT) == RESOLVE_CONTEXT) {
            if (strPrefix.length() == 0) {
                resolveTypes |= RESOLVE_FILE_PRJ_MACROS;
            } else {
                if (fileReferncesContext == null) {
                    resolveTypes |= RESOLVE_GLOB_MACROS | RESOLVE_LIB_MACROS;
                } else {
                    resolveTypes |= RESOLVE_FILE_PRJ_MACROS | RESOLVE_FILE_LIB_MACROS;
                }
            }

            // resolve classes always
            resolveTypes |= RESOLVE_CONTEXT_CLASSES;

            // namespaces and classes could be everywhere, hide should decide what to disable
            resolveTypes |= RESOLVE_CLASSES;
            resolveTypes |= RESOLVE_TEMPLATE_PARAMETERS;
            resolveTypes |= RESOLVE_GLOB_NAMESPACES;
            resolveTypes |= RESOLVE_LIB_CLASSES;
            resolveTypes |= RESOLVE_LIB_NAMESPACES;
            resolveTypes |= RESOLVE_CLASS_NESTED_CLASSIFIERS;
            resolveTypes |= FILE_LOCAL_ELEMENTS;

            // FIXUP: after we made static consts in headers belong to namespace,
            // in constuct below usage of globalVarUsedInArrayIndex became unresolved
            // const int globalVarUsedInArrayIndex;
            // struct UsingGlobalVarInArrayIndex {
            //     int data[globalVarUsedInArrayIndex];
            // };
            // TODO: solve this issue in a more elegant way
            resolveTypes |= RESOLVE_GLOB_VARIABLES;
            resolveTypes |= RESOLVE_GLOB_ENUMERATORS;

            assert (context != null);
            boolean resolveGlobalContext = false;
            if (CsmContextUtilities.isInFunction(context, offset)) {
                // for speed up remember result
                updateResolveTypesInFunction(offset, context, match);
            } else if (isInFunctionPointerType(context, offset)) {
                updateResolveTypesInFunction(offset, context, match);
            } else if (CsmContextUtilities.getClass(context, false, true) != null) {
                // for speed up remember result
                resolveTypes |= RESOLVE_CLASS_FIELDS;
                resolveTypes |= RESOLVE_CLASS_METHODS;
                resolveTypes |= RESOLVE_CLASS_ENUMERATORS;
            } else if (CsmContextUtilities.isInFunctionInstantiation(context, offset)) {
                updateResolveTypesInFunction(offset, context, match);
            } else {
                resolveGlobalContext = true;
            }
            if (resolveGlobalContext || (match && CsmContextUtilities.isInInitializerList(context, offset))) {
                // resolve global context as well
                resolveTypes |= RESOLVE_GLOB_FUNCTIONS;
                resolveTypes |= RESOLVE_GLOB_NAMESPACES;
                resolveTypes |= RESOLVE_LIB_CLASSES;
                resolveTypes |= RESOLVE_LIB_VARIABLES;
                resolveTypes |= RESOLVE_LIB_ENUMERATORS;
                resolveTypes |= RESOLVE_LIB_FUNCTIONS;
                resolveTypes |= RESOLVE_LIB_NAMESPACES;
            }
        }
    }

    private boolean isInFunctionPointerType(CsmContext context, int offset) {
        if (CsmKindUtilities.isFunctionPointerType(context.getLastScope())) {
            return CsmOffsetUtilities.isInObject(context.getLastScope(), offset);
        } else if (CsmKindUtilities.isTypedefOrTypeAlias(context.getLastObject())) {
            CsmTypedef typedef = (CsmTypedef) context.getLastObject();
            CsmType type = typedef.getType();
            if (CsmKindUtilities.isFunctionPointerType(type)) {
                return CsmOffsetUtilities.isInObject(type, offset);
            }
        }
        return false;
    }

    private Collection<CsmDeclaration> getUsedDeclarations(CsmFile file, int offset, String prefix, boolean match, CsmDeclaration.Kind[] kinds) {
        CsmProject prj = file.getProject();
        CsmProject inProject = prefix.length() == 0 ? prj : null;
        Collection<CsmDeclaration> usedDecls = CsmUsingResolver.getDefault().findUsedDeclarations(file, offset, inProject);
        Collection<CsmDeclaration> out = filterDeclarations(usedDecls, prefix, match, kinds);
        return out;
    }

    private Collection<CsmDeclaration> filterDeclarations(Collection<CsmDeclaration> orig, String prefix, boolean match, CsmDeclaration.Kind[] kinds) {
        LinkedHashSet<CsmDeclaration> out = new LinkedHashSet<CsmDeclaration>(orig.size());
        contResolver.filterDeclarations(orig.iterator(), out, kinds, prefix, match, false);
        return out;
    }

    private Collection<CsmNamespace> getNamespacesToSearch(CsmContext context, CsmFile file, int offset, boolean onlyInProject, boolean contextOnly) {
        CsmProject prj = file.getProject();
        CsmProject inProject = (onlyInProject || contextOnly) ? prj : null;
        Collection<CsmNamespace> namespaces = new ArrayList<CsmNamespace>();

        // add all namespaces from context
        Collection<CsmNamespace> contextNSs = getContextNamespaces(context);
        namespaces.addAll(contextNSs);
        namespaces = filterNamespaces(namespaces, inProject);

        if (!contextOnly && checkNamespaceDeclarations()) {
            namespaces.addAll(CsmUsingResolver.getDefault().findVisibleNamespaces(file, offset, inProject));
        }

        if (prj != null) {
            // add global namespace
            CsmNamespace globNS = prj.getGlobalNamespace();
            namespaces.add(globNS);
        }

        return namespaces;
    }

    /** it's a list, not just collection because order matters */
    private List<CsmNamespace> getContextNamespaces(CsmContext context) {
        CsmNamespace ns = CsmContextUtilities.getNamespace(context);
        List<CsmNamespace> out = new ArrayList<CsmNamespace>();
        while (ns != null && !ns.isGlobal()) {
            out.add(ns);
            ns = ns.getParent();
        }
        return out;
    }

    private Collection<CsmNamespace> filterNamespaces(Collection<CsmNamespace> orig, CsmProject prj) {
        LinkedHashSet<CsmNamespace> out = new LinkedHashSet<CsmNamespace>(orig.size());
        for (CsmNamespace ns : orig) {
            if (ns != null && (prj == null || ns.getProject() == prj)) {
                out.add(ns);
            }
        }
        return out;
    }
}
