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
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.Action;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.api.WhereUsedQueryConstants;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.java.ui.WhereUsedQueryUI;
import org.netbeans.modules.refactoring.java.ui.tree.ElementGrip;
import org.netbeans.modules.refactoring.spi.ProblemDetailsFactory;
import org.netbeans.modules.refactoring.spi.ProblemDetailsImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import static org.netbeans.modules.refactoring.java.plugins.Bundle.*;


/**
 * The plugin that carries out Safe Delete refactoring.
 * @author Bharath Ravikumar
 * @author Jan Becicka
 */
public class SafeDeleteRefactoringPlugin extends JavaRefactoringPlugin {
    
    private static final String DOT = "."; //NOI18N
    private static final String JAVA_EXTENSION = "java";
    private final ArrayList<TreePathHandle> grips;
    private final SafeDeleteRefactoring refactoring;
    private WhereUsedQuery[] whereUsedQueries;
    
    /**
     * Creates the a new instance of the Safe Delete refactoring
     * plugin.
     * @param refactoring The refactoring to be used by this plugin
     */
    public SafeDeleteRefactoringPlugin(SafeDeleteRefactoring refactoring) {
        this.refactoring = refactoring;
        this.grips = new ArrayList<>();
    }

    /**
     * Invokes the checkParameters of each of the underlying
     * WhereUsed refactorings and returns a Problem (if any)
     * returned by any of these queries.
     */
    @Override
    public Problem checkParameters() {
        //This class expects too many details from SafeDeleteRefactoring
        //But there's no other go I guess.
        grips.clear();
        for (final FileObject f: lookupJavaFileObjects()) {
            JavaSource source = JavaSource.forFileObject(f);
            if (source == null) {
                continue;
            }
            try {
                source.runUserActionTask(new CancellableTask<CompilationController>() {
                    @Override
                    public void cancel() {
                        
                    }
                    @Override
                    public void run(CompilationController co) throws Exception {
                        co.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        CompilationUnitTree cut = co.getCompilationUnit();
                        for (Tree t: cut.getTypeDecls()) {
                            if (t.getKind() == Tree.Kind.EMPTY_STATEMENT) {
                                // syntax errors
                                continue;
                            }
                            TreePathHandle handle = TreePathHandle.create(TreePath.getPath(cut, t), co);
                            if (!containsHandle(handle, co)) {
                                grips.add(handle);
                            }
                        }
                    }
                }, true);
            } catch (IllegalArgumentException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        grips.addAll(refactoring.getRefactoringSource().lookupAll(TreePathHandle.class));
        
        whereUsedQueries = new WhereUsedQuery[grips.size()];
        for(int i = 0;i <  whereUsedQueries.length; ++i) {
            final TreePathHandle handle = grips.get(i);
            final WhereUsedQuery q = new WhereUsedQuery(Lookups.singleton(handle));
            for (Object o:refactoring.getContext().lookupAll(Object.class)) {
                q.getContext().add(o);
            }
            q.getContext().add(refactoring);
            q.getContext().add(this);
            
            if(Tree.Kind.METHOD == handle.getKind()) {
                JavaSource source;
                source = JavaSource.forFileObject(handle.getFileObject());
                try {
                    final int index = i;
                    source.runUserActionTask(new Task<CompilationController>() {

                        @Override
                        public void run(CompilationController info) throws Exception {
                            info.toPhase(JavaSource.Phase.RESOLVED);
                            final Element element = handle.resolveElement(info);
                            if (element == null) {
                                throw new NullPointerException(String.format("#145291: Cannot resolve handle: %s\n%s", handle, info.getClasspathInfo())); // NOI18N
                            }
                            ElementKind kind = element.getKind();
                            if (kind == ElementKind.METHOD) {
                                Collection<ExecutableElement> overridens = JavaRefactoringUtils.getOverriddenMethods((ExecutableElement)element, info);
                                if(!overridens.isEmpty()) {
                                    ExecutableElement el = (ExecutableElement) overridens.iterator().next();
                                    assert el!=null;
                                    TreePathHandle basem = TreePathHandle.create(el, info);
                                    q.setRefactoringSource(Lookups.fixed(basem));
                                    grips.remove(index);
                                    grips.add(index, basem);
                                }
                            }
                        }
                    }, true);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                q.putValue(WhereUsedQueryConstants.SEARCH_FROM_BASECLASS, true);
                q.putValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS, false);
            }
            
            q.putValue(WhereUsedQuery.SEARCH_IN_COMMENTS, refactoring.isCheckInComments());
            whereUsedQueries[i] = q;
        }
        
        Problem problemFromUsage;
        for(int i = 0;i < whereUsedQueries.length; ++i) {
//          Fix for issue 63050. Doesn't make sense to check usages of a Resource.Ignore it.
//            if(whereUsedQueries[i].getRefactoredObject() instanceof Resource)
//                continue;
            problemFromUsage = whereUsedQueries[i].checkParameters();
            if(problemFromUsage != null) {
                return problemFromUsage;
            }
        }
        return null;
    }

    /**
     * A No-op for this particular refactoring.
     */
    @Override
    public Problem fastCheckParameters() {
        //Nothing to be done for Safe Delete
        return null;
    }

    /**
     * Checks whether the element being refactored is a valid Method/Field/Class
     * @return Problem returns a generic problem message if the check fails
     */
    @Override
    @NbBundle.Messages({"# {0} - VariableName", "ERR_VarNotInBlockOrMethod=Variable \"{0}\" is not inside a block or method declaration."})
    public Problem preCheck() {
        cancelRequested.set(false);
        final Problem[] problem = new Problem[1];
        Collection<? extends TreePathHandle> handles = refactoring.getRefactoringSource().lookupAll(TreePathHandle.class);
        for (final TreePathHandle tph : handles) {
            final FileObject fileObject = tph.getFileObject();
            if (fileObject == null || !fileObject.isValid()) {
                return new Problem(true, NbBundle.getMessage(FindVisitor.class, "DSC_ElNotAvail")); // NOI18N
            }
            JavaSource js = JavaSource.forFileObject(fileObject);
            if (js==null) {
                continue;
            }
            try {
                js.runUserActionTask(new Task<CompilationController>() {
                    
                    @Override
                    public void run(CompilationController javac) throws Exception {
                        javac.toPhase(JavaSource.Phase.RESOLVED);
                        TreePath selectedTree = tph.resolve(javac);
                        if (selectedTree != null && selectedTree.getParentPath() != null
                                && !TreeUtilities.CLASS_TREE_KINDS.contains(selectedTree.getParentPath().getLeaf().getKind())
                                && selectedTree.getParentPath().getLeaf().getKind() != Tree.Kind.COMPILATION_UNIT
                                && selectedTree.getLeaf().getKind() == Tree.Kind.VARIABLE) {
                            switch (selectedTree.getParentPath().getLeaf().getKind()) {
                                case BLOCK:
                                case METHOD:
                                    break;
                                default:
                                    problem[0] = new Problem(true, ERR_VarNotInBlockOrMethod(selectedTree.getLeaf().toString()));
                            }
                        }
                    }
                }, true);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return problem[0];
    }

    
    /**
     * For each element to be refactored, the corresponding
     * prepare method of the underlying WhereUsed query is
     * invoked to check for usages. If none is present, a
     * <CODE>SafeDeleteRefactoringElement</CODE> is created
     * with the corresponding element.
     * @param refactoringElements
     * @return
     */
    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        RefactoringSession usages = RefactoringSession.create("delete"); // NOI18N
        Set<TreePathHandle> refactoredObjects = new HashSet<>();
        Collection<? extends FileObject> files = lookupJavaFileObjects();
        Problem problem = findUsagesAndDelete(refactoredObjects, usages, files, refactoringElements);
        if(problem != null && problem.isFatal()) {
            return problem;
        }
        
        for (RefactoringElement refacElem : usages.getRefactoringElements()) {
            final ElementGrip elem = refacElem.getLookup().lookup(ElementGrip.class);
            if (files.contains(refacElem.getParentFile())) {
                continue;
            }
            if (!isPendingDelete(elem, refactoredObjects)) {
                problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(SafeDeleteRefactoringPlugin.class, "ERR_ReferencesFound"),
                        ProblemDetailsFactory.createProblemDetails(
                                new ProblemDetailsImplemen(new WhereUsedQueryUI(
                                        elem!=null?elem.getHandle():null, getWhereUsedItemNames(), refactoring),
                                        usages))));
                break;
            }
        }
        fireProgressListenerStop();
        return problem;
    }

    private boolean containsHandle(TreePathHandle handle, CompilationInfo info) {
        Element wanted = handle.resolveElement(info);
        for (TreePathHandle current : refactoring.getRefactoringSource().lookupAll(TreePathHandle.class)) {
            if (wanted == current.resolveElement(info)) {
                return true;
            }
        }
        return false;
    }
    
    private Problem findUsagesAndDelete(Set<TreePathHandle> refactoredObjects, RefactoringSession usages, Collection<? extends FileObject> files, RefactoringElementsBag refactoringElements) throws IllegalArgumentException {
        fireProgressListenerStart(AbstractRefactoring.PARAMETERS_CHECK, whereUsedQueries.length + 1);
        Problem problem = null;
        try {
            for (int i = 0; i < whereUsedQueries.length; ++i) {
                TreePathHandle refactoredObject = whereUsedQueries[i].getRefactoringSource().lookup(TreePathHandle.class);
                refactoredObjects.add(refactoredObject);
                whereUsedQueries[i].prepare(usages);
                TreePathHandle treePathHandle = grips.get(i);
                Set<FileObject> relevant = getRelevantFiles(treePathHandle);
                if(Tree.Kind.METHOD == treePathHandle.getKind()) {
                    OverriddenAbsMethodFinder finder = new OverriddenAbsMethodFinder(allMethods);
                    JavaSource javaSrc = JavaSource.forFileObject(treePathHandle.getFileObject());
                    try {
                        javaSrc.runUserActionTask(finder, true);
                    } catch (IOException ioException) {
                        ErrorManager.getDefault().notify(cancelRequested.get()?ErrorManager.INFORMATIONAL:ErrorManager.UNKNOWN,ioException);
                    }
                    problem = finder.getProblem();
                }
                TransformTask task = new TransformTask(new DeleteTransformer(allMethods, files), treePathHandle);
                problem = JavaPluginUtils.chainProblems(createAndAddElements(relevant, task, refactoringElements, refactoring), problem);
                fireProgressListenerStep();
            }
        } finally {
            usages.finished();
        }
        
        return problem;
    }

    private HashSet<ElementHandle<ExecutableElement>> allMethods;
    private Set<FileObject> getRelevantFiles(final TreePathHandle tph) {
        if (tph.getKind() == Kind.METHOD) {
            ClasspathInfo cpInfo = RefactoringUtils.getClasspathInfoFor(tph);
            final Set<FileObject> set = new LinkedHashSet<>();
            set.add(tph.getFileObject());
            JavaSource source = JavaSource.create(cpInfo, tph.getFileObject());

            try {
                source.runUserActionTask(new CancellableTask<CompilationController>() {

                    @Override
                    public void cancel() {
                        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                    }

                    @Override
                    public void run(CompilationController info) throws Exception {
                        final ClassIndex idx = info.getClasspathInfo().getClassIndex();
                        info.toPhase(JavaSource.Phase.RESOLVED);
                        final ElementUtilities elmUtils = info.getElementUtilities();

                        //add all references of overriding methods
                        ExecutableElement el = (ExecutableElement) tph.resolveElement(info);
                        ElementHandle<ExecutableElement> methodHandle = ElementHandle.create(el);
                        ElementHandle<TypeElement> enclosingType = ElementHandle.create(elmUtils.enclosingTypeElement(el));
                        allMethods = new HashSet<>();
                        allMethods.add(methodHandle);
                        for (ExecutableElement e : JavaRefactoringUtils.getOverridingMethods(el, info, cancelRequested)) {
                            ElementHandle<ExecutableElement> handle = ElementHandle.create(e);
                            set.add(SourceUtils.getFile(handle, info.getClasspathInfo()));
                            ElementHandle<TypeElement> encl = ElementHandle.create(elmUtils.enclosingTypeElement(e));
                            set.addAll(idx.getResources(encl, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                            allMethods.add(ElementHandle.create(e));
                        }
                        //add all references of overriden methods
                        for (ExecutableElement e : JavaRefactoringUtils.getOverriddenMethods(el, info)) {
                            ElementHandle<ExecutableElement> handle = ElementHandle.create(e);
                            set.add(SourceUtils.getFile(handle, info.getClasspathInfo()));
                            ElementHandle<TypeElement> encl = ElementHandle.create(elmUtils.enclosingTypeElement(e));
                            set.addAll(idx.getResources(encl, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                            allMethods.add(ElementHandle.create(e));
                        }
                        set.addAll(idx.getResources(enclosingType, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                        set.add(SourceUtils.getFile(methodHandle, info.getClasspathInfo()));
                    }
                }, true);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
            return set;
        } else {
            return Collections.singleton(tph.getFileObject());
        }
    }
    

    private String getWhereUsedItemNames() {
        final StringBuilder b = new StringBuilder();
        for (final TreePathHandle handle:grips) {
            try {
                JavaSource.forFileObject(handle.getFileObject()).runUserActionTask(new Task<CompilationController>() {
                    @Override
                    public void run(CompilationController parameter) throws Exception {
                        parameter.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        if (b.length() > 0) {
                            b.append(", ");
                        }
                        b.append(handle.resolveElement(parameter).getSimpleName());
                    }
                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return b.toString();
    }
    
    private Collection<? extends FileObject> lookupJavaFileObjects() {
        Lookup lkp = refactoring.getRefactoringSource();
        Collection<? extends FileObject> javaFiles;
        NonRecursiveFolder folder = lkp.lookup(NonRecursiveFolder.class);
        if (folder != null) {
            javaFiles = getJavaFileObjects(folder.getFolder(), false);
        } else {
            Collection<FileObject> javaFileObjects =  new ArrayList<>();
            for (FileObject fileObject : lkp.lookupAll(FileObject.class)) {
                if (fileObject.isFolder()) {
                    javaFileObjects.addAll(getJavaFileObjects(fileObject, true));
                }else if (JavaRefactoringUtils.isRefactorable(fileObject)) {
                    javaFileObjects.add(fileObject);
                }
            }
            javaFiles = javaFileObjects;
        }
        return javaFiles;
    }
    
    @Override
    protected JavaSource getJavaSource(Phase p) {
        return null;
    }
    
    private static void addSourcesInDir(FileObject dirFileObject, boolean isRecursive, Collection<FileObject> javaSrcFiles){
        for (FileObject childFileObject : dirFileObject.getChildren()) {
            if (childFileObject.isData() && JAVA_EXTENSION.equalsIgnoreCase(childFileObject.getExt())) {
                javaSrcFiles.add(childFileObject);
            }
            else if (childFileObject.isFolder() && isRecursive) {
                addSourcesInDir(childFileObject, true, javaSrcFiles);
            }
        }
    }
    
    private static Collection<FileObject> getJavaFileObjects(FileObject dirFileObject, boolean isRecursive){
        Collection<FileObject> javaSrcFiles = new ArrayList<>();
        addSourcesInDir(dirFileObject, isRecursive, javaSrcFiles);
        return javaSrcFiles;
    }
    
    private static String getMethodName(final TreePathHandle methodHandle){
        JavaSource javaSrc = JavaSource.forFileObject(methodHandle.getFileObject());
        final String[] methodNameString = new String[1];
        //Ugly hack to return the method name from the anonymous inner class
        try {
            javaSrc.runUserActionTask(new CancellableTask<CompilationController>(){

                @Override
                public void cancel() {
                    //No op
                }

                @Override
                public void run(CompilationController compilationController) throws Exception {
                    ExecutableElement execElem = (ExecutableElement) methodHandle.resolveElement(compilationController);
                    TypeElement type = (TypeElement) execElem.getEnclosingElement();
                    methodNameString[0] = type.getQualifiedName() + DOT + execElem.toString();
                }
                
            }, true);

        } catch (IOException ioException) {
            ErrorManager.getDefault().notify(ioException);
        }

        return methodNameString[0];
    }
    
    private static boolean isPendingDelete(ElementGrip elementGrip, Set<TreePathHandle> refactoredObjects) {
        ElementGrip parent = elementGrip;
        if (parent!=null) {
            do {
                if (refactoredObjects.contains(parent.getHandle())) {
                    return true;
                }
                parent = parent.getParent();
            } while (parent!=null);
        }
        return false;
    }
    
    private class ProblemDetailsImplemen implements ProblemDetailsImplementation {

        private RefactoringUI ui;
        private RefactoringSession rs;
        
        public ProblemDetailsImplemen(RefactoringUI ui, RefactoringSession rs) {
            this.ui = ui;
            this.rs = rs;
        }
        
        @Override
        public void showDetails(Action callback, Cancellable parent) {
            parent.cancel();
            UI.openRefactoringUI(ui, rs, callback);
        }
        
        @Override
        public String getDetailsHint() {
            return NbBundle.getMessage(SafeDeleteRefactoringPlugin.class, "LBL_ShowUsages");
        }
    }
}
