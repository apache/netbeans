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
package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.ClassIndex.ResourceType;
import org.netbeans.api.java.source.ClassIndex.SearchScopeType;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.SourceUtilsEx;
import org.netbeans.modules.refactoring.java.WhereUsedBinaryElement;
import org.netbeans.modules.refactoring.java.WhereUsedElement;
import org.netbeans.modules.refactoring.java.api.WhereUsedQueryConstants;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.java.spi.JavaWhereUsedFilters;
import org.netbeans.modules.refactoring.java.spi.JavaWhereUsedFilters.ReadWrite;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.ui.FiltersDescription;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import static org.netbeans.modules.refactoring.java.plugins.Bundle.*;

/**
 *
 * @author  Jan Becicka
 * @author  Ralph Ruijs
 */
public class JavaWhereUsedQueryPlugin extends JavaRefactoringPlugin implements FiltersDescription.Provider {
    private boolean fromLibrary;
    private final WhereUsedQuery refactoring;
    
    public static final boolean DEPENDENCIES;
    static {
        String prop = System.getProperty("org.netbeans.modules.refactoring.java.plugins.JavaWhereUsedQueryPlugin.dependencies"); //NOI18N
        DEPENDENCIES = prop == null ? true : prop.equalsIgnoreCase("true"); //NOI18N
    }
    private volatile CancellableTask queryTask;

    /** Creates a new instance of WhereUsedQuery */
    public JavaWhereUsedQueryPlugin(WhereUsedQuery refactoring) {
        this.refactoring = refactoring;
    }
    
    @Override
    protected JavaSource getJavaSource(Phase p) {
        switch (p) {
        default: 
            return JavaSource.forFileObject(refactoring.getRefactoringSource().lookup(TreePathHandle.class).getFileObject());
        }
    }
    
    @Override
    public Problem preCheck() {
        cancelRequest = false;
        cancelRequested.set(false);
        TreePathHandle handle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        if (!handle.getFileObject().isValid()) {
            return new Problem(true, NbBundle.getMessage(FindVisitor.class, "DSC_ElNotAvail")); // NOI18N
        }
        if (handle.getKind() == Tree.Kind.ARRAY_TYPE) {
            return new Problem(true, NbBundle.getMessage(FindVisitor.class, "ERR_FindUsagesArrayType"));
        }
        return super.preCheck();
    }

    @Override
    protected Problem preCheck(CompilationController javac) throws IOException {
        javac.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
        TreePathHandle handle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        if (handle.resolveElement(javac) == null) {
            return new Problem(true, NbBundle.getMessage(FindVisitor.class, "DSC_ElNotAvail")); // NOI18N
        }
        return null;
    }
    
    
    public static Set<FileObject> getRelevantFiles(
            final TreePathHandle tph, final ClasspathInfo cpInfo,
            final boolean isFindSubclasses, final boolean isFindDirectSubclassesOnly,
            final boolean isFindOverridingMethods, final boolean isSearchOverloadedMethods,
            final boolean isFindUsages, final boolean isIncludeDependencies, final boolean isSearchInComments, final Set<NonRecursiveFolder> folders,
            final AtomicBoolean cancel) {
        final ClassIndex idx = cpInfo.getClassIndex();
        final Set<FileObject> sourceSet = new TreeSet<>(new FileComparator());
        final Set<NonRecursiveFolder> packages = (folders == null)? Collections.<NonRecursiveFolder>emptySet() : folders;
        final Set<ResourceType> resourceType = isIncludeDependencies? EnumSet.of(ResourceType.SOURCE, ResourceType.BINARY) : EnumSet.of(ResourceType.SOURCE);
        
        final FileObject file = tph.getFileObject();
        JavaSource source;
        source = JavaPluginUtils.createSource(file, cpInfo, tph);
        if(cancel != null && cancel.get()) {
            return Collections.<FileObject>emptySet();
        }
        CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
            @Override
            public void cancel() {
            }
            
            @Override
            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                final Element el = tph.resolveElement(info);
                if (el == null) {
                    sourceSet.clear();
                    LOG.log(Level.INFO, "#250160 #145291: Cannot resolve handle: %s\n%s", new Object[] {tph, info.getClasspathInfo()});
                    return;
                }
                Set<SearchScopeType> searchScopeType = new HashSet<>(1);
                final Set<String> packageSet = new HashSet<>(packages.size());
                for (NonRecursiveFolder nonRecursiveFolder : packages) {
                    String resourceName = cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE).getResourceName(nonRecursiveFolder.getFolder());
                    if (resourceName != null) {
                        packageSet.add(resourceName.replace('/', '.'));
                    }
                }
                searchScopeType.add(new SearchScopeType() {
                    @Override
                    public Set<? extends String> getPackages() {
                        return packageSet.isEmpty() ? null : packageSet;
                    }

                    @Override
                    public boolean isSources() {
                        return true;
                    }

                    @Override
                    public boolean isDependencies() {
                        return isIncludeDependencies;
                    }
                });
                if (cancel != null && cancel.get()) {
                    sourceSet.clear();
                    return;
                }
                if (el.getKind().isField()) {
                    //get field references from index
                    final ElementHandle<TypeElement> handle = ElementHandle.create((TypeElement) el.getEnclosingElement());
                    sourceSet.addAll(idx.getResources(handle, EnumSet.of(ClassIndex.SearchKind.FIELD_REFERENCES), searchScopeType, resourceType));
                    if(isSearchInComments && cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE).contains(file)) {
                        sourceSet.add(file);
                    }
                } else if (el.getKind().isClass() || el.getKind().isInterface()) {
                    if (isFindSubclasses || isFindDirectSubclassesOnly) {
                        if (isFindDirectSubclassesOnly) {
                            EnumSet searchKind = EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS);
                            if (el.getKind() == ElementKind.INTERFACE) {
                                searchKind.add(ClassIndex.SearchKind.FUNCTIONAL_IMPLEMENTORS);
                            }
                            //get direct implementors from index
                            sourceSet.addAll(idx.getResources(ElementHandle.create((TypeElement) el), searchKind, searchScopeType, resourceType));
                        } else {
                            Set<ElementHandle<TypeElement>> implementorsAsHandles = RefactoringUtils.getImplementorsAsHandles(idx, cpInfo, (TypeElement)el, cancel);
                            if (cancel != null && cancel.get()) {
                                sourceSet.clear();
                                return;
                            }
                            sourceSet.addAll(SourceUtilsEx.getFiles((Collection<ElementHandle<? extends Element>>)(Collection<?>)implementorsAsHandles, cpInfo, cancel));
                            if (el.getKind() == ElementKind.INTERFACE) {
                                sourceSet.addAll(getFunctionalSubtypes(
                                    ElementHandle.create((TypeElement)el),
                                    implementorsAsHandles,
                                    cpInfo,
                                    searchScopeType));
                            }
                        }
                    } else {
                        //get type references from index
                        sourceSet.addAll(idx.getResources(ElementHandle.create((TypeElement) el), EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS), searchScopeType, resourceType));
                    }
                } else if (el.getKind() == ElementKind.METHOD) {
                    ExecutableElement method = (ExecutableElement) el;
                    List<ExecutableElement> methods = new LinkedList<>();
                    methods.add(method);
                    TypeElement enclosingTypeElement = info.getElementUtilities().enclosingTypeElement(method);
                    if(isSearchOverloadedMethods) {
                        for (Element overloaded : enclosingTypeElement.getEnclosedElements()) {
                            if(method != overloaded &&
                                    method.getKind() == overloaded.getKind() &&
                                    ((ExecutableElement)overloaded).getSimpleName().contentEquals(method.getSimpleName())) {
                                methods.add((ExecutableElement)overloaded);
                            }
                        }
                    }
                    if (isFindOverridingMethods) {
                        //Find overriding methods
                        sourceSet.addAll(getImplementorsRecursive(idx, cpInfo, enclosingTypeElement, cancel));
                    }
                    if (isFindUsages) {
                        //get method references for method and for all it's overriders
                        Set<ElementHandle<TypeElement>> s = RefactoringUtils.getImplementorsAsHandles(idx, cpInfo, (TypeElement) method.getEnclosingElement(), cancel);
                        for (ElementHandle<TypeElement> eh : s) {
                            if (cancel != null && cancel.get()) {
                                sourceSet.clear();
                                return;
                            }
                            TypeElement te = eh.resolve(info);
                            if (te == null) {
                                continue;
                            }
                            for (Element e : te.getEnclosedElements()) {
                                if (RefactoringUtils.isExecutableElement(e)) {
                                    for (ExecutableElement executableElement : methods) {
                                        if (info.getElements().overrides((ExecutableElement) e, executableElement, te)) {
                                            sourceSet.addAll(idx.getResources(ElementHandle.create(te), EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES), searchScopeType, resourceType));
                                        }
                                    }
                                }
                            }
                        }
                        sourceSet.addAll(idx.getResources(ElementHandle.create((TypeElement) el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES), searchScopeType, resourceType)); //?????
                    }
                } else if (el.getKind() == ElementKind.CONSTRUCTOR) {
                    sourceSet.addAll(idx.getResources(ElementHandle.create((TypeElement) el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS), searchScopeType, resourceType));
                } else if((el.getKind() == ElementKind.LOCAL_VARIABLE || el.getKind() == ElementKind.PARAMETER)
                        || el.getModifiers().contains(Modifier.PRIVATE)) {
                    sourceSet.add(file);
                }
            }
        };
        try {
            source.runUserActionTask(task, true);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        Set<FileObject> result = sourceSet;
        // filter out files that are not on source path
        if(!isIncludeDependencies) {
            Set<FileObject> filteredSources = new HashSet<>(sourceSet.size());
            ClassPath cp = cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
            for (FileObject fo : sourceSet) {
                if (cp.contains(fo)) {
                    filteredSources.add(fo);
                } else {
                    LOG.log(Level.FINE, "Filtered out: {0}", fo.getNameExt()); //NOI18N
                }
                if (cancel != null && cancel.get()) {
                    return Collections.<FileObject>emptySet();
                }
            }
            result = filteredSources;
        }
        return result;
    }
    private static final Logger LOG = Logger.getLogger(JavaWhereUsedQueryPlugin.class.getName());

    private static Collection<FileObject> getImplementorsRecursive(ClassIndex idx, ClasspathInfo cpInfo, TypeElement el, AtomicBoolean cancel) {
        Set<?> implementorsAsHandles = RefactoringUtils.getImplementorsAsHandles(idx, cpInfo, el, cancel);

        if(cancel != null && cancel.get()) {
            return Collections.<FileObject>emptySet();
        }
        @SuppressWarnings("unchecked")
        Collection<FileObject> set = SourceUtilsEx.getFiles((Collection<ElementHandle<? extends Element>>) implementorsAsHandles, cpInfo, cancel);

        return set;
    }

    @Override
    public Problem prepare(final RefactoringElementsBag elements) {
        fireProgressListenerStart(ProgressEvent.START, -1);
        usedAccessFilters.clear();
        usedFilters.clear();
        
        final FindTask findTask = new FindTask(elements);
        Problem problem = null;
        
        ClasspathInfo cp = getClasspathInfo(refactoring);
        fromLibrary = refactoring.getRefactoringSource().lookup(TreePathHandle.class).getFileObject() == null || refactoring.getRefactoringSource().lookup(TreePathHandle.class).getFileObject().getNameExt().endsWith("class"); // NOI18N
        if (isSearchFromBaseClass()) {
            TreePathHandle sourceHandle = refactoring.getContext().lookup(TreePathHandle.class);
            if (fromLibrary && sourceHandle != null) {
                cp = RefactoringUtils.getClasspathInfoFor(sourceHandle, refactoring.getRefactoringSource().lookup(TreePathHandle.class));
            } else {
                cp = RefactoringUtils.getClasspathInfoFor(refactoring.getRefactoringSource().lookup(TreePathHandle.class));
            }
        }
        Scope customScope = refactoring.getContext().lookup(Scope.class);
        if (customScope != null) {

            // ********* 1 *********
            if (!customScope.getFiles().isEmpty()) {
                Set<FileObject> a = new TreeSet<>(new FileComparator());
                a.addAll(customScope.getFiles());
                fireProgressListenerStep(a.size());
                try {
                    queryFiles(a, findTask,  RefactoringUtils.getClasspathInfoFor(a.toArray(new FileObject[0])));
                } catch (IOException e) {
                    problem = JavaPluginUtils.chainProblems(problem, createProblemAndLog(null, e));
                }
            }

            // ********* 2 *********            
            FileObject fo = null;
            if (fromLibrary) {
                fo = RefactoringUtils.getFileObject(refactoring.getRefactoringSource().lookup(TreePathHandle.class));
                if (fo == null) {
                    fo = refactoring.getRefactoringSource().lookup(TreePathHandle.class).getFileObject();
                }
            }
            if (!customScope.getSourceRoots().isEmpty()) {
                ClasspathInfo cpath;
                if(isSearchFromBaseClass() && fo != null) {
                    HashSet<FileObject> fileobjects = new HashSet<>(customScope.getSourceRoots());
                    fileobjects.add(fo);
                    cpath = RefactoringUtils.getClasspathInfoFor(customScope.isDependencies(), fileobjects.toArray(new FileObject[0]));
                } else {
                    cpath = RefactoringUtils.getClasspathInfoFor(customScope.isDependencies(), customScope.getSourceRoots().toArray(new FileObject[0]));
                }
                Set<FileObject> a = getRelevantFiles(refactoring.getRefactoringSource().lookup(TreePathHandle.class), cpath, isFindSubclasses(), isFindDirectSubclassesOnly(), isFindOverridingMethods(), isSearchOverloadedMethods(), isFindUsages(), customScope.isDependencies(), isSearchInComments(), null, cancelRequested);
                
                fireProgressListenerStep(a.size());
                try {
                    queryFiles(a, findTask, cpath);
                } catch (IOException e) {
                    problem = JavaPluginUtils.chainProblems(problem, createProblemAndLog(null, e));
                }
            }
            

            // ********* 3n *********
            Map<FileObject, Set<NonRecursiveFolder>> folders = new HashMap<>();
            for(NonRecursiveFolder nonRecursiveFolder : customScope.getFolders()) {
                FileObject folder = nonRecursiveFolder.getFolder();
                ClassPath classPath = ClassPath.getClassPath(folder, ClassPath.SOURCE);
                final FileObject sourceRoot = classPath.findOwnerRoot(folder);
                Set<NonRecursiveFolder> packages = folders.get(sourceRoot);
                if(packages == null) {
                    packages = new HashSet<>();
                    folders.put(sourceRoot, packages);
                }
                packages.add(nonRecursiveFolder);
            }
            for (Map.Entry<FileObject, Set<NonRecursiveFolder>> entry : folders.entrySet()) {
                FileObject sourceRoot1 = entry.getKey();
                Set<NonRecursiveFolder> packages1 = entry.getValue();
                if (packages1 != null && !packages1.isEmpty()) {
                    ClasspathInfo cpath;
                    if (isSearchFromBaseClass() && fo != null) {
                        cpath = RefactoringUtils.getClasspathInfoFor(customScope.isDependencies(), sourceRoot1, fo);
                    } else {
                        cpath = RefactoringUtils.getClasspathInfoFor(customScope.isDependencies(), sourceRoot1);
                    }
                    Set<FileObject> a = getRelevantFiles(refactoring.getRefactoringSource().lookup(TreePathHandle.class), cpath, isFindSubclasses(), isFindDirectSubclassesOnly(), isFindOverridingMethods(), isSearchOverloadedMethods(), isFindUsages(), customScope.isDependencies(), isSearchInComments(), packages1, cancelRequested);
                 
                    fireProgressListenerStep(a.size());
                    try {
                        queryFiles(a, findTask, cpath);
                    } catch (IOException e) {
                        problem = JavaPluginUtils.chainProblems(problem, createProblemAndLog(null, e));
                    }
                }
            }
        } else {
            Set<FileObject> a = getRelevantFiles(refactoring.getRefactoringSource().lookup(TreePathHandle.class), cp, isFindSubclasses(), isFindDirectSubclassesOnly(), isFindOverridingMethods(), isSearchOverloadedMethods(), isFindUsages(), false, isSearchInComments(), null, cancelRequested);
            fireProgressListenerStep(a.size());
            try {
                queryFiles(a, findTask, cp);
            } catch (IOException e) {
                problem = JavaPluginUtils.chainProblems(problem, createProblemAndLog(null, e));
            }
        }
        
        fireProgressListenerStop();
        return problem;
    }
    
    @Override
    public void cancelRequest() {
        super.cancelRequest();
        CancellableTask t = queryTask;
        if (t != null) {
            t.cancel();
        }
    }
    
    @Override
    public Problem fastCheckParameters() {
        if (refactoring.getRefactoringSource().lookup(TreePathHandle.class).getKind() == Tree.Kind.METHOD) {
            return checkParametersForMethod(isFindOverridingMethods(), isFindUsages());
        } 
        return null;
    }

    @Override
    protected Problem checkParameters(CompilationController javac) throws IOException {
        // Rerun precheck, in-case the source has changed
        return preCheck(javac);
    }
    
    private Problem checkParametersForMethod(boolean overriders, boolean usages) {
        if (!(usages || overriders)) {
            return new Problem(true, NbBundle.getMessage(JavaWhereUsedQueryPlugin.class, "MSG_NothingToFind"));
        } else {
            return null;
        }
    }
    
    public static CloneableEditorSupport findCloneableEditorSupport(DataObject dob) {
        Object obj = dob.getLookup().lookup(org.openide.cookies.OpenCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        obj = dob.getLookup().lookup(org.openide.cookies.EditorCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        return null;
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
    private boolean isSearchOverloadedMethods() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.SEARCH_OVERLOADED);
    }
    private boolean isSearchInComments() {
        return refactoring.getBooleanValue(WhereUsedQuery.SEARCH_IN_COMMENTS);
    }
    private boolean isSearchFromBaseClass() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.SEARCH_FROM_BASECLASS);
    }

    @Override
    @NbBundle.Messages({"READ_FILTER=Read filter", "WRITE_FILTER=Write filter",
                        "IMPORT_FILTER=Import filter",
                        "READ_WRITE_FILTER=Read/Write filter",
                        "COMMENT_FILTER=Comment filter",
                        "SOURCE_FILTER=Source filter",
                        "BINARY_FILTER=Binary filter", "TEST_FILTER=Test filter",
                        "DEPENDENCY_FILTER=Dependency filter",
                        "PLATFORM_FILTER=Platform filter"})
    public void addFilters(FiltersDescription filtersDescription) {
        filtersDescription.addFilter(JavaWhereUsedFilters.ReadWrite.READ.getKey(), READ_FILTER(), true,
                ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/found_item_read.png", false)); //NOI18N
        filtersDescription.addFilter(JavaWhereUsedFilters.ReadWrite.WRITE.getKey(), WRITE_FILTER(), true,
                ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/found_item_write.png", false)); //NOI18N
        filtersDescription.addFilter(JavaWhereUsedFilters.ReadWrite.READ_WRITE.getKey(), READ_WRITE_FILTER(), true,
                ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/found_item_readwrite.png", false)); //NOI18N
        filtersDescription.addFilter(JavaWhereUsedFilters.IMPORT.getKey(), IMPORT_FILTER(), true,
                ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/found_item_import.png", false)); //NOI18N
        filtersDescription.addFilter(JavaWhereUsedFilters.COMMENT.getKey(), COMMENT_FILTER(), true,
                ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/found_item_comment.png", false)); //NOI18N
        filtersDescription.addFilter(JavaWhereUsedFilters.SOURCEFILE.getKey(), SOURCE_FILTER(), true,
                ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/found_item_source.png", false)); //NOI18N
        if(DEPENDENCIES) {
            filtersDescription.addFilter(JavaWhereUsedFilters.BINARYFILE.getKey(), BINARY_FILTER(), true,
                    ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/clazz.png", false)); //NOI18N
        }
        filtersDescription.addFilter(JavaWhereUsedFilters.TESTFILE.getKey(), TEST_FILTER(), true,
                ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/found_item_test.png", false)); //NOI18N
        if(DEPENDENCIES) {
            filtersDescription.addFilter(JavaWhereUsedFilters.DEPENDENCY.getKey(), DEPENDENCY_FILTER(), true,
                    ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/found_item_binary.gif", false)); //NOI18N
            filtersDescription.addFilter(JavaWhereUsedFilters.PLATFORM.getKey(), PLATFORM_FILTER(), true,
                    ImageUtilities.loadImageIcon("org/netbeans/modules/java/platform/resources/platform.gif", false)); //NOI18N
        }
    }

    private final EnumSet<JavaWhereUsedFilters.ReadWrite> usedAccessFilters = EnumSet.noneOf(JavaWhereUsedFilters.ReadWrite.class);
    private final Set<String> usedFilters = new HashSet<>();
    @Override
    public void enableFilters(FiltersDescription filtersDescription) {
        for (JavaWhereUsedFilters.ReadWrite filter : usedAccessFilters) {
            filtersDescription.enable(filter.getKey());
        }
        for (String string : usedFilters) {
            filtersDescription.enable(string);
        }
    }

    @NonNull
    private static Collection<? extends FileObject> getFunctionalSubtypes(
            @NonNull final ElementHandle<TypeElement> base,
            @NonNull final Collection<ElementHandle<TypeElement>> subtypes,
            @NonNull final ClasspathInfo cpInfo,
            @NonNull final Set<ClassIndex.SearchScopeType> scope) {
        assert base.getKind() == ElementKind.INTERFACE;
        final ClassIndex index = cpInfo.getClassIndex();
        final Set<ClassIndex.SearchKind> fncKind = EnumSet.of(ClassIndex.SearchKind.FUNCTIONAL_IMPLEMENTORS);
        final Set<FileObject> result = new HashSet<>();
        result.addAll(index.getResources(base, fncKind, scope));
        for (ElementHandle<TypeElement> e : subtypes) {
            if (e.getKind() == ElementKind.INTERFACE) {
                result.addAll(index.getResources(e, fncKind, scope));
            }
        }
        return result;
    }

    private class FindTask implements CancellableTask<CompilationController> {

        private final RefactoringElementsBag elements;
        private volatile AtomicBoolean cancelled;
        private final Set<FileObject> cachedPlatformRoots;

        public FindTask(RefactoringElementsBag elements) {
            this.elements = elements;
            this.cancelled = new AtomicBoolean(false);
            this.cachedPlatformRoots = new HashSet<>();
        }
        
        @Override
        public void cancel() {
            cancelled.set(true);
        }

        @Override
        public void run(CompilationController compiler) throws IOException {
            if (cancelled.get()) {
                return;
            }
            if (compiler.toPhase(JavaSource.Phase.RESOLVED)!=JavaSource.Phase.RESOLVED) {
                return;
            }
            TreePathHandle handle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
            Element element = handle.resolveElement(compiler);
            if (element==null) {
                ErrorManager.getDefault().log(ErrorManager.ERROR, "element is null for handle " + handle); // NOI18N
                return;
            }
            final boolean fromPlatform;
            final boolean fromTest;
            CompilationUnitTree cu = compiler.getCompilationUnit();
            FileObject fo = compiler.getFileObject();
            fromPlatform = fromPlatform(fo);
            if (cu == null) {
                if(DEPENDENCIES) {
//                if(!fromPlatform) {
//                    fromTest |= UnitTestForSourceQuery.
                    fromTest = false;
//                }

                    elements.add(refactoring, WhereUsedBinaryElement.create(fo, fromTest, fromPlatform));

                    usedFilters.add(JavaWhereUsedFilters.BINARYFILE.getKey());
                }
                return;
            }
            final boolean fromDependency;
            if(!fromPlatform) {
                ClassPath srcPath = compiler.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
                FileObject ownerRoot = srcPath.findOwnerRoot(fo);
                fromDependency = ownerRoot == null;
                fromTest = !fromDependency && RefactoringUtils.isFromTestRoot(fo, srcPath);
            } else {
                fromTest = false;
                fromDependency = false;
            }
            AtomicBoolean inImport = new AtomicBoolean();
            if (isFindUsages()) {
                Collection<WhereUsedElement> foundElements;
                FindUsagesVisitor findVisitor = new FindUsagesVisitor(compiler, cancelled, isSearchInComments(), isSearchOverloadedMethods(), fromTest, fromPlatform, fromDependency, inImport);
                findVisitor.scan(cu, element);
                foundElements = findVisitor.getElements();
                boolean usagesInComments = findVisitor.usagesInComments();
                for (WhereUsedElement el : foundElements) {
                    final ReadWrite access = el.getAccess();
                    if(access != null) {
                        usedAccessFilters.add(access);
                    }
                    elements.add(refactoring, el);
                }
                if(!foundElements.isEmpty()) {
                    if(fromTest) {
                        usedFilters.add(JavaWhereUsedFilters.TESTFILE.getKey());
                    }
                    if(!fromTest) {
                        usedFilters.add(JavaWhereUsedFilters.SOURCEFILE.getKey());
                    }
                    if(usagesInComments) {
                        usedFilters.add(JavaWhereUsedFilters.COMMENT.getKey());
                    }
                    if(inImport.get()) {
                        usedFilters.add(JavaWhereUsedFilters.IMPORT.getKey());
                    }
                    if(DEPENDENCIES) {
                        if(fromDependency) {
                            usedFilters.add(JavaWhereUsedFilters.DEPENDENCY.getKey());
                        }
                        if(fromPlatform) {
                            usedFilters.add(JavaWhereUsedFilters.PLATFORM.getKey());
                        }
                    }
                }
            }
            Collection<TreePath> result = new ArrayList<>();
            if (element.getKind() == ElementKind.METHOD && isFindOverridingMethods()) {
                FindOverridingVisitor override = new FindOverridingVisitor(compiler);
                override.scan(compiler.getCompilationUnit(), element);
                result.addAll(override.getUsages());
            } else if ((element.getKind().isClass() || element.getKind().isInterface()) &&
                    (isFindSubclasses()||isFindDirectSubclassesOnly())) {
                FindSubtypesVisitor subtypes = new FindSubtypesVisitor(!isFindDirectSubclassesOnly(), compiler);
                subtypes.scan(compiler.getCompilationUnit(), element);
                result.addAll(subtypes.getUsages());
            }
            for (TreePath tree : result) {
                elements.add(refactoring, WhereUsedElement.create(compiler, tree, fromTest, fromPlatform, fromDependency, inImport));
            }
            if(!result.isEmpty()) {
                if(DEPENDENCIES) {
                    if(fromPlatform) {
                        usedFilters.add(JavaWhereUsedFilters.PLATFORM.getKey());
                    }
                    if(fromDependency) {
                        usedFilters.add(JavaWhereUsedFilters.DEPENDENCY.getKey());
                    }
                }
                if(fromTest) {
                    usedFilters.add(JavaWhereUsedFilters.TESTFILE.getKey());
                }
                if(!fromTest) {
                    usedFilters.add(JavaWhereUsedFilters.SOURCEFILE.getKey());
                }
                if(inImport.get()) {
                    usedFilters.add(JavaWhereUsedFilters.IMPORT.getKey());
                }
                
            }
            fireProgressListenerStep();
        }

        private boolean fromPlatform(FileObject fo) {
            boolean fromPlatform = false;
            FileObject archive = FileUtil.getArchiveFile(fo);
            if(archive != null) {
                FileObject root = FileUtil.getArchiveRoot(archive);
                if(root != null) {
                    if(cachedPlatformRoots.contains(root)) {
                        fromPlatform = true;
                    }
                    JavaPlatformManager manager = JavaPlatformManager.getDefault();
                    for (JavaPlatform javaPlatform : manager.getInstalledPlatforms()) {
                        if(javaPlatform.getSourceFolders().contains(root) ||
                                javaPlatform.getStandardLibraries().contains(root) ||
                                javaPlatform.getBootstrapLibraries().contains(root)) {
                            fromPlatform = true;
                            if(DEPENDENCIES) {
                                usedFilters.add(JavaWhereUsedFilters.PLATFORM.getKey());
                            }
                            cachedPlatformRoots.add(root);
                            break;
                        }
                    }
                }
            }
            return fromPlatform;
        }
    }
    
    private static class FileComparator implements Comparator<FileObject> {

        @Override
        public int compare(FileObject o1, FileObject o2) {
            return o1.getPath().compareTo(o2.getPath());
        }
    }

}
