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
package org.netbeans.modules.cnd.refactoring.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmVirtualInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmIncludeHierarchyResolver;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceSupport;
import org.netbeans.modules.cnd.api.model.xref.CsmTypeHierarchyResolver;
import org.netbeans.modules.cnd.modelutil.CsmImageName;
import org.netbeans.modules.cnd.refactoring.api.WhereUsedQueryConstants;
import org.netbeans.modules.cnd.refactoring.elements.CsmRefactoringElementImpl;
import org.netbeans.modules.cnd.refactoring.spi.CsmWhereUsedExtraObjectsProvider;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.ui.FiltersDescription;
import org.openide.util.CharSequences;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 * Actual implementation of Find Usages query search for C/C++
 * 
 * @todo Perform index lookups to determine the set of files to be checked!
 * 
 */
public class CsmWhereUsedQueryPlugin extends CsmRefactoringPlugin implements FiltersDescription.Provider {
    private final WhereUsedQuery refactoring;
    private final CsmObject startReferenceObject;
    private FiltersDescription filtersDescription;
    
    /** Creates a new instance of WhereUsedQuery */
    public CsmWhereUsedQueryPlugin(WhereUsedQuery refactoring) {
        this.refactoring = refactoring;
        startReferenceObject = refactoring.getRefactoringSource().lookup(CsmObject.class);
    }
    
    @Override
    public Problem prepare(final RefactoringElementsBag elements) {
        checkSelectDeclarations();
        
        CsmUID referencedObjectUID = refactoring.getRefactoringSource().lookup(CsmUID.class);
        CsmObject referencedObject = referencedObjectUID == null ? null : (CsmObject) referencedObjectUID.getObject();
        if (referencedObject == null) {
            return null;
        }
        Collection<RefactoringElementImplementation> res = doPrepareElements(referencedObject, elements);
        elements.addAll(refactoring, res);
        fireProgressListenerStop();
        return null;
    }
    
    private void checkSelectDeclarations() {
        if(filtersDescription != null) {
            //select/unselect DECLARATIONS filter if needed
            for (int i = 0; i < filtersDescription.getFilterCount(); i++) {
                if (CsmWhereUsedFilters.DECLARATIONS.getKey().equals(filtersDescription.getKey(i))) {
                    filtersDescription.setSelected(i, isFindOverridingMethods() || isFindDirectSubclassesOnly() || isFindSubclasses());
                    break;
                }
            }
        }
    }

    /*package*/ Collection<RefactoringElementImplementation> doPrepareElements(CsmObject referencedObject, RefactoringElementsBag bagToAdd) {
        try {
            CsmCacheManager.enter();
            return doPrepareElementsImpl(referencedObject, bagToAdd);
        } finally {
            CsmCacheManager.leave();
        }
    }
    
    private Collection<RefactoringElementImplementation> doPrepareElementsImpl(CsmObject referencedObject, RefactoringElementsBag bagToAdd) {
        Collection<RefactoringElementImplementation> res = null;
        referencedObject = CsmRefactoringUtils.convertToCsmObjectIfNeeded(referencedObject);
        if (referencedObject == null) {
            return Collections.emptyList();
        }
        long time = System.currentTimeMillis();
        if (isFindUsages()) {
            if (CsmKindUtilities.isFile(referencedObject)) {
                fireProgressListenerStart(ProgressEvent.START, 2);
                res = processIncludeQuery((CsmFile)referencedObject);
            } else if (Boolean.getBoolean("cnd.model.global.index")) {
                Collection<CsmObject> referencedObjects = getObjectsForFindUsages(referencedObject);
                fireProgressListenerStart(ProgressEvent.START, referencedObjects.size() + 2);
                res = processObjectUsagesQuery(referencedObjects);
            } else {
                Collection<CsmObject> referencedObjects = getObjectsForFindUsages(referencedObject);
                CsmFile startFile = CsmRefactoringUtils.getCsmFile(startReferenceObject);
                Set<CsmFile> files = new HashSet<>();
                for (CsmObject csmObject : referencedObjects) {
                    files.addAll(getRelevantFiles(startFile, csmObject, refactoring));
                }
                LOG.log(Level.FINE, "preparing files took {0}ms", System.currentTimeMillis() - time);
                fireProgressListenerStart(ProgressEvent.START, files.size() + 2);
                res = processObjectUsagesQuery(referencedObjects, files, bagToAdd);
            }
        } else if (isFindDirectSubclassesOnly() || isFindSubclasses()) {
            assert CsmKindUtilities.isClass(referencedObject) : "must be class";
            fireProgressListenerStart(ProgressEvent.START, 2);
            res = processSubclassesQuery((CsmClass)referencedObject);
        } else if (isFindOverridingMethods()) {
            assert CsmKindUtilities.isMethod(referencedObject) : "must be method";
            fireProgressListenerStart(ProgressEvent.START, 2);
            res = processOverridenMethodsQuery((CsmMethod)referencedObject);
        }   
        LOG.log(Level.FINE, "preparing FindUsages elements took {0}ms", System.currentTimeMillis() - time);
        fireProgressListenerStep();
        return res;
    }

    @Override
    public Problem preCheck() {
        CsmRefactoringUtils.waitParsedAllProjects();
        CsmUID uid = refactoring.getRefactoringSource().lookup(CsmUID.class);    
        Problem invalidContext = new Problem(true, NbBundle.getMessage(CsmWhereUsedQueryPlugin.class, "MSG_InvalidObjectNothingToFind")); // NOI18N;
        if (uid == null) {
            CsmFile startFile = CsmRefactoringUtils.getCsmFile(startReferenceObject);
            if (startFile == null || !startFile.isValid()) {
                return invalidContext;
            }              
            return super.preCheck();
        }
        CsmObject referencedObject = (CsmObject) uid.getObject();
        if (!CsmBaseUtilities.isValid(referencedObject)) {
            return invalidContext;
        }
        return super.preCheck();
    }

    @Override
    public Problem fastCheckParameters() {
        CsmUID uid = refactoring.getRefactoringSource().lookup(CsmUID.class);    
        if (uid != null && CsmKindUtilities.isMethod((CsmObject)uid.getObject())) {
            return checkParametersForMethod(isFindOverridingMethods(), isFindUsages());
        } else {
            return super.fastCheckParameters();
        }
    }
    
    
    //    //@Override
//    protected Problem fastCheckParameters(CompilationController info) {
//        if (searchHandle.getKind() == ElementKind.METHOD) {
//            return checkParametersForMethod(isFindOverridingMethods(), isFindUsages());
//        } 
//        return null;
//    }
//    
//    //@Override
//    protected Problem checkParameters(CompilationController info) {
//        return null;
//    }
    
    public static Collection<RefactoringElementImplementation> getWhereUsed(CsmReference ref, Map<Object, Boolean> params, FiltersDescription filtersDescription) {
        CsmObject targetObject = ref.getReferencedObject();
        Lookup lkp = Lookups.singleton(ref);
        WhereUsedQuery query = new WhereUsedQuery(lkp);
        Collection<CsmProject> prjs = CsmRefactoringUtils.getRelatedCsmProjects(targetObject, ref.getContainingFile().getProject());
        CsmProject[] ar = prjs.toArray(new CsmProject[prjs.size()]);
        query.getContext().add(ar);
        // set parameters
        for (Map.Entry<Object, Boolean> entry : params.entrySet()) {
            query.putValue(entry.getKey(), entry.getValue());
        }
        CsmWhereUsedQueryPlugin whereUsedPlugin = new CsmWhereUsedQueryPlugin(query);
        if (filtersDescription != null) {
            whereUsedPlugin.addFilters(filtersDescription);
        }
        whereUsedPlugin.checkSelectDeclarations();
        Collection<RefactoringElementImplementation> elements = whereUsedPlugin.doPrepareElements(targetObject, null);
        if (filtersDescription != null) {
            whereUsedPlugin.enableFilters(filtersDescription);
        }
        return elements;
    }
    
    private Problem checkParametersForMethod(boolean overriders, boolean usages) {
        if (!(usages || overriders)) {
            return new Problem(true, NbBundle.getMessage(CsmWhereUsedQueryPlugin.class, "MSG_NothingToFind"));
        } else {
            return null;
        }
    }

    private Collection<CsmObject> getObjectsForFindUsages(CsmObject startObject) {
        Collection<CsmObject> out = new LinkedHashSet<>();
        if (isFindUsages()) {
            Collection<CsmObject> allObjects = collectAllObjects(startObject);
            for (CsmObject referencedObject : allObjects) {
                if (CsmKindUtilities.isMethod(referencedObject)) {
                    CsmMethod method = (CsmMethod) CsmBaseUtilities.getFunctionDeclaration((CsmFunction) referencedObject);
                    if (CsmVirtualInfoQuery.getDefault().isVirtual(method)) {
                        out.addAll(CsmVirtualInfoQuery.getDefault().getOverriddenMethods(method, isSearchFromBaseClass()));
                    }
                } else if (CsmKindUtilities.isClass(referencedObject)) {
                    // add all constructors
                    out.addAll(CsmRefactoringUtils.getConstructors((CsmClass)referencedObject));
                }
                out.add(referencedObject);
            }
        }
        return out;
    }
        
    private boolean isFindSubclasses() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.FIND_SUBCLASSES);
    }
    
    private boolean isFindUsages() {
        return refactoring.getBooleanValue(WhereUsedQuery.FIND_REFERENCES);
    }
    
    private boolean isFindDirectSubclassesOnly() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.FIND_DIRECT_SUBCLASSES);
    }
    
    private boolean isFindOverridingMethods() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS);
    }

    private boolean isSearchFromBaseClass() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.SEARCH_FROM_BASECLASS);
    }

    private boolean isReadWriteAccess() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.READ_WRITE);
    }

    private boolean isSearchInComments() {
        return refactoring.getBooleanValue(WhereUsedQuery.SEARCH_IN_COMMENTS);
    }
    
    private Collection<RefactoringElementImplementation> processObjectUsagesQuery(final Collection<CsmObject> csmObjects) {
        assert isFindUsages() : "must be find usages mode";
        final boolean onlyUsages = !isFindOverridingMethods();
        final CsmReferenceRepository xRef = CsmReferenceRepository.getDefault();
        final Collection<RefactoringElementImplementation> elements = new ConcurrentLinkedQueue<>();
        //Set<CsmReferenceKind> kinds = isFindOverridingMethods() ? CsmReferenceKind.ALL : CsmReferenceKind.ANY_USAGE;
        final Set<CsmReferenceKind> kinds = CsmReferenceKind.ALL;
        final Interrupter interrupter = new Interrupter() {

            @Override
            public boolean cancelled() {
                return isCancelled();
            }
        };      
        RequestProcessor rp = new RequestProcessor("FindUsagesQuery", CndUtils.getNumberCndWorkerThreads() + 1); // NOI18N
        final CountDownLatch waitFinished = new CountDownLatch(csmObjects.size());
        for (final CsmObject curObj : csmObjects) {
            Runnable task = new Runnable() {

                @Override
                public void run() {
                    try {
                        if (!isCancelled()) {
                            String oldName = Thread.currentThread().getName();
                            try {
                                Thread.currentThread().setName("FindUsagesQuery: Analyzing " + curObj); //NOI18N
                                CsmCacheManager.enter();
                                Collection<CsmReference> refs = xRef.getReferences(curObj, (CsmProject)null, kinds, interrupter);
                                for (CsmReference csmReference : refs) {
                                    boolean accept = true;
                                    if (onlyUsages) {
                                        accept = !CsmReferenceResolver.getDefault().isKindOf(csmReference, EnumSet.of(CsmReferenceKind.DECLARATION, CsmReferenceKind.DEFINITION));
                                    }
                                    if (accept) {
                                        elements.add(CsmRefactoringElementImpl.create(csmReference, true, curObj));
                                    }
                                }
                            } finally {
                                CsmCacheManager.leave();
                                Thread.currentThread().setName(oldName);
                            }
                            synchronized (CsmWhereUsedQueryPlugin.this) {
                                fireProgressListenerStep();
                            }
                        }
                    } finally {
                        waitFinished.countDown();
                    }
                }
            };
            rp.post(task);
        }
        try {
            waitFinished.await();
        } catch (InterruptedException ex) {
        }
        return elements;
    }
    
    private Collection<RefactoringElementImplementation> processObjectUsagesQuery(
                                                            final Collection<CsmObject> csmObjects,            
                                                            final Collection<CsmFile> files,
                                                            final RefactoringElementsBag bagToAdd) {
        assert isFindUsages() : "must be find usages mode";
        final boolean onlyUsages = false;
        final CsmReferenceRepository xRef = CsmReferenceRepository.getDefault();
        //Set<CsmReferenceKind> kinds = isFindOverridingMethods() ? CsmReferenceKind.ALL : CsmReferenceKind.ANY_USAGE;
        final Set<CsmReferenceKind> kinds = CsmReferenceKind.ALL;
        final CsmObject[] objs = csmObjects.toArray(new CsmObject[csmObjects.size()]);
        final Interrupter interrupter = new Interrupter(){
            @Override
            public boolean cancelled() {
                return isCancelled();
            }
        };
        RequestProcessor rp = new RequestProcessor("FindUsagesQuery", CndUtils.getNumberCndWorkerThreads() + 1); // NOI18N
        
        final long time = System.currentTimeMillis();
        List<CsmFile> sortedFiles = new ArrayList<>(files);
        Collections.sort(sortedFiles, new Comparator<CsmFile>() {
            @Override
            public int compare(CsmFile o1, CsmFile o2) {
                CsmProject prj1 = o1.getProject();
                CsmProject prj2 = o2.getProject();
                if (prj1 == null || prj2 == null || prj1.equals(prj2)) {
                    return CharSequences.comparator().compare(o1.getName(), o2.getName());
                } else {
                    return CharSequences.comparator().compare(prj1.getName(), prj2.getName());
                }
            }
        });
        LOG.log(Level.FINE, "creation of sorted {0} files took {1}ms", new Object[] {sortedFiles.size(), System.currentTimeMillis()-time});
        final List<OneFileWorker> work = new ArrayList<>(sortedFiles.size());
        for (final CsmFile file : sortedFiles) {
            OneFileWorker task = new OneFileWorker(interrupter, file, onlyUsages, xRef, kinds, objs);
            work.add(task);
            rp.post(task);
        }

        final Collection<RefactoringElementImplementation> elements = new ArrayList<>(work.size()*2);
        int indexNonEmpty = 0;
        int total = 0;
        boolean firstResults = true;
        // wait files one by one
        // we need sorted output to support Background mode without insertions between tree nodes
        for (OneFileWorker workUnit : work) {
            if (isCancelled()) {
                break;
            }
            Collection<RefactoringElementImplementation> exposedElements = workUnit.exposedElements;
            while (exposedElements == null) {
                if (isCancelled()) {
                    break;
                }
                synchronized (workUnit) {
                    exposedElements = workUnit.exposedElements;
                    if (exposedElements == null) {
                        try {
                            workUnit.wait();
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
            assert exposedElements != null || isCancelled();
            if (exposedElements != null && !exposedElements.isEmpty()) {
                if (firstResults) {
                    firstResults = false;
                    LOG.log(Level.FINE, "creation of the first {0} elements took {1}ms", new Object[] {exposedElements.size(), System.currentTimeMillis()-time});
                }
                if (bagToAdd == null) {
                    elements.addAll(exposedElements);
                } else {
                    synchronized (bagToAdd) {
                        bagToAdd.addAll(refactoring, exposedElements);
                    }
                }
                total += exposedElements.size();
                LOG.log(Level.FINEST, "[{0}/{1}] {2}", new Object[] {++indexNonEmpty, total, workUnit.file.getAbsolutePath()});
            }
        }
        return elements;
    }

    @Override
    public void addFilters(FiltersDescription filtersDescription) {
        this.filtersDescription = filtersDescription;
        filtersDescription.addFilter(CsmWhereUsedFilters.COMMENTS.getKey(), 
                NbBundle.getMessage(this.getClass(), "TXT_Filter_Comments"), true,
                ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/refactoring/resources/found_item_comment.png", false)); //NOI18N
        filtersDescription.addFilter(CsmWhereUsedFilters.DEAD_CODE.getKey(),
                NbBundle.getMessage(this.getClass(), "TXT_Filter_DeadCode"), true,
                ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/refactoring/resources/found_item_dead.png", false)); //NOI18N

        filtersDescription.addFilter(CsmWhereUsedFilters.READ.getKey(), 
                NbBundle.getMessage(this.getClass(), "TXT_Filter_Read"), true,
                ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/refactoring/resources/found_item_read.png", false)); //NOI18N
        filtersDescription.addFilter(CsmWhereUsedFilters.WRITE.getKey(),
                NbBundle.getMessage(this.getClass(), "TXT_Filter_Write"), true,
                ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/refactoring/resources/found_item_write.png", false)); //NOI18N
        filtersDescription.addFilter(CsmWhereUsedFilters.READ_WRITE.getKey(),
                NbBundle.getMessage(this.getClass(), "TXT_Filter_ReadWrite"), true,
                ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/refactoring/resources/found_item_readwrite.png", false)); //NOI18N

        filtersDescription.addFilter(CsmWhereUsedFilters.DECLARATIONS.getKey(),
                NbBundle.getMessage(this.getClass(), "TXT_Filter_Declarations"), false,
                ImageUtilities.loadImageIcon(CsmImageName.DECLARATION_FILTER, false));
        filtersDescription.addFilter(CsmWhereUsedFilters.SCOPE.getKey(),
                NbBundle.getMessage(this.getClass(), "TXT_Filter_Scope"), false,
                ImageUtilities.loadImageIcon(CsmImageName.SCOPE_FILTER, false));
        filtersDescription.addFilter(CsmWhereUsedFilters.MACROS.getKey(),
                NbBundle.getMessage(this.getClass(), "TXT_Filter_Macros"), true,
                ImageUtilities.loadImageIcon(CsmImageName.MACRO, false));
    }

    @Override
    public void enableFilters(FiltersDescription filtersDescription) {
        if (isSearchInComments()) {
            filtersDescription.enable(CsmWhereUsedFilters.COMMENTS.getKey());
        }
        filtersDescription.enable(CsmWhereUsedFilters.DEAD_CODE.getKey());
        if (isReadWriteAccess()) {
            filtersDescription.enable(CsmWhereUsedFilters.READ.getKey());
            filtersDescription.enable(CsmWhereUsedFilters.WRITE.getKey());
            filtersDescription.enable(CsmWhereUsedFilters.READ_WRITE.getKey());
        }
        if (!isFindDirectSubclassesOnly() && !isFindSubclasses()) {
            filtersDescription.enable(CsmWhereUsedFilters.DECLARATIONS.getKey());
            filtersDescription.enable(CsmWhereUsedFilters.SCOPE.getKey());
        }
        filtersDescription.enable(CsmWhereUsedFilters.MACROS.getKey());
    }
    
    private final class OneFileWorker implements Runnable {
        final Interrupter interrupter;
        private final CsmFile file;
        private volatile Collection<RefactoringElementImplementation> exposedElements = null;
        private final boolean onlyUsages;
        private final CsmReferenceRepository xRef;
        private final Set<CsmReferenceKind> kinds;
        private final CsmObject[] objs;

        public OneFileWorker(Interrupter interrupter, CsmFile file, boolean onlyUsages, CsmReferenceRepository xRef, Set<CsmReferenceKind> kinds, CsmObject[] objs) {
            this.interrupter = interrupter;
            this.file = file;
            this.onlyUsages = onlyUsages;
            this.xRef = xRef;
            this.kinds = kinds;
            this.objs = objs;
        }

        @Override
        public void run() {
            Collection<RefactoringElementImplementation> fileElems = Collections.emptyList();
            try {
                if (!isCancelled()) {
                    String oldName = Thread.currentThread().getName();
                    try {
                        Thread.currentThread().setName("FindUsagesQuery: Analyzing " + file.getAbsolutePath()); //NOI18N
                        CsmCacheManager.enter();
                        // get find usages
                        Collection<CsmReference> refs = xRef.getReferences(objs, file, kinds, interrupter);
                        
                        // get usages in comments if needed
                        if (isSearchInComments() && objs.length > 0 && !refs.isEmpty()) {
                            Collection<CsmReference> comments = CsmRefactoringUtils.getComments(file, CsmRefactoringUtils.getSimpleText(objs[0]));
                            refs.addAll(comments);
                        }
                        
                        fileElems = new ArrayList<>(refs.size());
                        for (CsmReference csmReference : refs) {
                            boolean accept = true;
                            if (onlyUsages) {
                                accept = !CsmReferenceResolver.getDefault().isKindOf(csmReference, EnumSet.of(CsmReferenceKind.DECLARATION, CsmReferenceKind.DEFINITION));
                            }
                            if (accept) {
                                fileElems.add(CsmRefactoringElementImpl.create(csmReference, true, objs.length > 0 ? objs[0] : null));
                            }
                        }
                    } finally {
                        CsmCacheManager.leave();
                        Thread.currentThread().setName(oldName);
                    }
                    synchronized (CsmWhereUsedQueryPlugin.this) {
                        fireProgressListenerStep();
                    }
                }
            } finally {
                synchronized (this) {
                    this.exposedElements = fileElems;
                    notifyAll();
                }
            }
        }
    }
    
    private Collection<RefactoringElementImplementation> processOverridenMethodsQuery(final CsmMethod startMethod) {
        assert isFindOverridingMethods() : "must be search for overriden methods";
        Collection<RefactoringElementImplementation> elements = new LinkedHashSet<>(1024);
        Collection<CsmObject> allObjects = collectAllObjects(startMethod);
        Collection<CsmMethod> allMethods = new HashSet<>();
        for (CsmObject obj : allObjects) {
            if (CsmKindUtilities.isMethod(obj)) {
                CsmMethod method = (CsmMethod) CsmBaseUtilities.getFunctionDeclaration((CsmFunction) obj);
                if (method != null) {
                    allMethods.add(method);
                }
            }
        }
        for (CsmMethod csmMethod : allMethods) {
            Collection<CsmMethod> overrides = CsmVirtualInfoQuery.getDefault().getOverriddenMethods(csmMethod, isSearchFromBaseClass());
            overrides.add(csmMethod);
            for (CsmMethod method : overrides) {
                CsmReference declRef = CsmReferenceSupport.createObjectReference(method);
                elements.add(CsmRefactoringElementImpl.create(declRef, false, method));
                // find defintion of method if needed
                if (!CsmKindUtilities.isFunctionDefinition(method)) {
                    CsmFunctionDefinition def = method.getDefinition();
                    if (def != null) {
                        CsmReference defRef = CsmReferenceSupport.createObjectReference(def);
                        elements.add(CsmRefactoringElementImpl.create(defRef, false, method));
                    }
                }
            }
        }
        return elements;
    }

    @Override
    protected final ModificationResult processFiles(Collection<CsmFile> files, AtomicReference<Problem> outProblem) {
        // where used query does not modify files
        return null;
    }

    private Collection<RefactoringElementImplementation> processIncludeQuery(final CsmFile startFile) {
        assert isFindUsages() : "must be find usages";
        Collection<RefactoringElementImplementation> elements = new LinkedHashSet<>(1024);
        Collection<CsmObject> allObjects = collectAllObjects(startFile);
        Collection<CsmFile> allFiles = new HashSet<>();
        for (CsmObject obj : allObjects) {
            if (CsmKindUtilities.isFile(obj)) {
                allFiles.add((CsmFile)obj);
            }
        }        
        for (CsmFile csmFile : allFiles) {
            Collection<CsmReference> refs = CsmIncludeHierarchyResolver.getDefault().getIncludes(csmFile);
            CsmProject[] prjs = refactoring.getContext().lookup(CsmProject[].class);
            if (prjs != null && prjs.length != 0) {
                for (CsmReference csmReference : refs) {
                    for (CsmProject prj : prjs) {
                        if (csmReference.getContainingFile().getProject().equals(prj)) {
                            elements.add(CsmRefactoringElementImpl.create(csmReference, false, csmFile));
                            break;
                        }
                    }
                }
            } else {
                for (CsmReference csmReference : refs) {
                    elements.add(CsmRefactoringElementImpl.create(csmReference, false, csmFile));
                }
            }
        }
        return elements;
    }
    
    private Collection<RefactoringElementImplementation> processSubclassesQuery(final CsmClass startClass) {
        assert isFindDirectSubclassesOnly() || isFindSubclasses() : "must be search of subclasses";
        Collection<RefactoringElementImplementation> elements = new LinkedHashSet<>(1024);
        boolean directSubtypesOnly = isFindDirectSubclassesOnly();
        Collection<CsmObject> allObjects = collectAllObjects(startClass);
        for (CsmObject obj : allObjects) {
            if (CsmKindUtilities.isClass(obj)) {
                CsmClass referencedClass = (CsmClass) obj;
                Collection<CsmReference> refs = CsmTypeHierarchyResolver.getDefault().getSubTypes(referencedClass, directSubtypesOnly);
                for (CsmReference csmReference : refs) {
                    elements.add(CsmRefactoringElementImpl.create(csmReference, false, startClass));
                }
            }
        }
        return elements;
    }     
    
    private Collection<CsmObject> collectAllObjects(CsmObject primaryObject) {
        Collection<CsmObject> allObjects = new HashSet<>();
        if (primaryObject != null) {
            allObjects.add(primaryObject);
            for (CsmWhereUsedExtraObjectsProvider provider : Lookup.getDefault().lookupAll(CsmWhereUsedExtraObjectsProvider.class)) {
                allObjects.addAll(provider.getExtraObjects(primaryObject));
            }
            allObjects.addAll(getEqualObjects(primaryObject));
        }
        return allObjects;
    }    
}
