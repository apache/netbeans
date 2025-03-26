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
package org.netbeans.modules.refactoring.java.spi;

import com.sun.source.tree.CompilationUnitTree;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.plugins.FindVisitor;
import org.netbeans.modules.refactoring.java.plugins.JavaPluginUtils;
import org.netbeans.modules.refactoring.java.spi.hooks.JavaModificationResult;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringCommit;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public abstract class JavaRefactoringPlugin extends ProgressProviderAdapter implements RefactoringPlugin {

    protected enum Phase {PRECHECK, FASTCHECKPARAMETERS, CHECKPARAMETERS, PREPARE};
    /**
     * Use cancelRequested
     * @deprecated
     */
    @Deprecated
    protected volatile boolean cancelRequest = false;
    
    /**
     * true if cancel was requested
     * false otherwise
     */
    protected final AtomicBoolean cancelRequested = new AtomicBoolean();
    
    private volatile CancellableTask currentTask;
    private WorkingTask workingTask = new WorkingTask();
    

    /**
     * Create Java specific implementation of {@link Transaction}.
     * 
     * @param modifications collection of {@link ModificationResult}
     * @return Java specific Transaction
     * @see RefactoringElementsBag#registerTransaction(Transaction) 
     * @since 1.24.0
     * 
     * @author Jan Becicka
     */
    public static Transaction createTransaction(@NonNull Collection<ModificationResult> modifications) {
        return new RefactoringCommit(createJavaModifications(modifications));
    }

    private static Collection<org.netbeans.modules.refactoring.spi.ModificationResult> createJavaModifications(Collection<ModificationResult> modifications) {
        LinkedList<org.netbeans.modules.refactoring.spi.ModificationResult> result = new LinkedList<>();
        for (ModificationResult r:modifications) {
            result.add(new JavaModificationResult(r));
        }
        return result;
    }
    
    
    protected Problem preCheck(CompilationController javac) throws IOException {
        return null;
    }
    protected Problem checkParameters(CompilationController javac) throws IOException {
        return null;
    }
    protected Problem fastCheckParameters(CompilationController javac) throws IOException {
        return null;
    }
//    protected abstract Problem prepare(WorkingCopy wc, RefactoringElementsBag bag) throws IOException;

    protected abstract JavaSource getJavaSource(Phase p);

    @Override
    public Problem preCheck() {
        cancelRequest = false;
        cancelRequested.set(false);
        return workingTask.run(Phase.PRECHECK);
    }

    @Override
    public Problem checkParameters() {
        return workingTask.run(Phase.CHECKPARAMETERS);
    }

    @Override
    public Problem fastCheckParameters() {
        return workingTask.run(Phase.FASTCHECKPARAMETERS);
    }

//    public Problem prepare(final RefactoringElementsBag bag) {
//        this.whatRun = Switch.PREPARE;
//        this.problem = null;
//        FileObject fo = getFileObject();
//        JavaSource js = JavaSource.forFileObject(fo);
//        try {
//            js.runModificationTask(new CancellableTask<WorkingCopy>() {
//                public void cancel() {
//                }
//
//                public void run(WorkingCopy wc) throws Exception {
//                    prepare(wc, bag);
//                }
//            }).commit();
//        } catch (IOException ex) {
//            throw new RuntimeException(ex);
//        }
//        return problem;
//    }
    
    @Override
    public void cancelRequest() {
        cancelRequest = true;
        cancelRequested.set(true);
        if (currentTask!=null) {
            currentTask.cancel();
        }
    }

    protected ClasspathInfo getClasspathInfo(AbstractRefactoring refactoring) {
        ClasspathInfo cpInfo = refactoring.getContext().lookup(ClasspathInfo.class);
        if (cpInfo==null) {
            Collection<? extends TreePathHandle> handles = refactoring.getRefactoringSource().lookupAll(TreePathHandle.class);
            if (!handles.isEmpty()) {
                cpInfo = RefactoringUtils.getClasspathInfoFor(handles.toArray(new TreePathHandle[0]));
            } else {
                cpInfo = JavaRefactoringUtils.getClasspathInfoFor((FileObject)null);
            }
            refactoring.getContext().add(cpInfo);
        }
        return cpInfo;
    }
    
    protected static Problem createProblem(Problem result, boolean isFatal, String message) {
        Problem problem = new Problem(isFatal, message);
        return JavaPluginUtils.chainProblems(result, problem);
    }

    /**
     * Checks if the element is still available. Tests if it is still valid.
     * (Was not deleted by matching mechanism.)
     * If element is available, returns null, otherwise it creates problem.
     * (Helper method for refactoring implementation as this problem is
     * general for all refactorings.)
     *
     * @param   e  element to check
     * @param info 
     * @return  problem message or null if the element is valid
     */
    protected static Problem isElementAvail(TreePathHandle e, CompilationInfo info) {
        if (e==null) {
            //element is null or is not valid.
            return new Problem(true, NbBundle.getMessage(FindVisitor.class, "DSC_ElNotAvail")); // NOI18N
        } else {
            Element el = e.resolveElement(info);
            String elName = el != null ? el.getSimpleName().toString() : null;
            if (el == null || el.asType().getKind() == TypeKind.ERROR || "<error>".equals(elName)) { // NOI18N
                return new Problem(true, NbBundle.getMessage(FindVisitor.class, "DSC_ElementNotResolved"));
            }
            
            if ("this".equals(elName) || "super".equals(elName)) { // NOI18N
                return new Problem(true, NbBundle.getMessage(FindVisitor.class, "ERR_CannotRefactorThis", el.getSimpleName()));
            }
            
            // element is still available
            return null;
        }
    }
    
    private Map<FileObject,List<FileObject>> groupByRoot (Set<FileObject> sourceFiles) {
        Map<FileObject,List<FileObject>> result = new LinkedHashMap<> (); // Keep order, sources first!
        for (FileObject file : sourceFiles) {
            if (cancelRequested.get()) {
                return Collections.emptyMap();
            }
            ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);
            if (cp != null) {
                FileObject root = cp.findOwnerRoot(file);
                if (root != null) {
                    List<FileObject> subr = result.get (root);
                    if (subr == null) {
                        subr = new LinkedList<>();
                        result.put (root,subr);
                    }
                    subr.add (file);
                }
            } else {
                FileObject root = FileUtil.getArchiveFile(file);
                if (root != null) {
                    List<FileObject> subr = result.get (root);
                    if (subr == null) {
                        subr = new LinkedList<>();
                        result.put (root,subr);
                    }
                    subr.add(file);
                }
            }
        }
        return result;
    }
    
    protected final Collection<ModificationResult> processFiles(Set<FileObject> files, CancellableTask<WorkingCopy> task) throws IOException {
        return processFiles(files, task, null);
    }

    protected final Collection<ModificationResult> processFiles(Set<FileObject> files, CancellableTask<WorkingCopy> task, ClasspathInfo info) throws java.io.IOException {
        return processFiles(files, task, info, true);
    }
    
    protected final void queryFiles(Set<FileObject> files, CancellableTask<? extends CompilationController> task)  throws java.io.IOException {
        queryFiles(files, task, null);
    }
    
    protected final void queryFiles(Set<FileObject> files, CancellableTask<? extends CompilationController> task, ClasspathInfo info)  throws java.io.IOException {
        processFiles(files, task, info, false);
    }
    
    private static final ClassPath EMPTY_PATH = ClassPathSupport.createClassPath(new URL[0]);

    private Collection<ModificationResult> processFiles(Set<FileObject> sourceFiles, CancellableTask<? extends CompilationController> task, ClasspathInfo info, boolean modification) throws IOException {
        currentTask = task;
        Collection<ModificationResult> results = new LinkedList<>();
        try {
            Map<FileObject, List<FileObject>> work = groupByRoot(sourceFiles);
            processFiles(work, info, modification, results, task);
        } finally {
            currentTask = null;
        }
        return results;
    }

    private void processFiles(Map<FileObject, List<FileObject>> work, ClasspathInfo info, boolean modification, Collection<ModificationResult> results, CancellableTask<? extends CompilationController> task) throws IOException, IllegalArgumentException {
        for (Map.Entry<FileObject, List<FileObject>> entry : work.entrySet()) {
            if (cancelRequested.get()) {
                results.clear();
                return;
            }
            final FileObject root = entry.getKey();
            if (info == null) {
                ClassPath bootPath = ClassPath.getClassPath(root, ClassPath.BOOT);
                if (bootPath == null) {
                    //javac requires at least java.lang
                    bootPath = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
                }
                ClassPath moduleBootPath = ClassPath.getClassPath(root, JavaClassPathConstants.MODULE_BOOT_PATH);
                if (moduleBootPath == null) {
                    moduleBootPath = EMPTY_PATH;
                }
                ClassPath compilePath = ClassPath.getClassPath(root, ClassPath.COMPILE);
                if (compilePath == null) {
                    compilePath = EMPTY_PATH;
                }
                ClassPath moduleClassPath = ClassPath.getClassPath(root, JavaClassPathConstants.MODULE_CLASS_PATH);
                if (moduleClassPath == null) {
                    moduleClassPath = EMPTY_PATH;
                }
                ClassPath moduleCompilePath = ClassPath.getClassPath(root, JavaClassPathConstants.MODULE_COMPILE_PATH);
                if (moduleCompilePath == null) {
                    moduleCompilePath = EMPTY_PATH;
                }
                ClassPath executePath = ClassPath.getClassPath(root, ClassPath.EXECUTE);
                if (executePath == null) {
                    executePath = EMPTY_PATH;
                }
                ClassPath srcPath = ClassPath.getClassPath(root, ClassPath.SOURCE);
                if (srcPath == null) {
                    srcPath = EMPTY_PATH;
                }
                info = new ClasspathInfo.Builder(bootPath)
                        .setModuleBootPath(moduleBootPath)
                        .setClassPath(ClassPathSupport.createProxyClassPath(compilePath,executePath))
                        .setModuleClassPath(moduleClassPath)
                        .setModuleCompilePath(moduleCompilePath)
                        .setSourcePath(srcPath)
                        .build();
            }
            List<FileObject> augmentedFiles = new ArrayList<>(entry.getValue());
            FileObject fake = FileUtil.createMemoryFileSystem().getRoot().createData("Fake.java");
            if (!augmentedFiles.stream().anyMatch(fo -> SourceUtils.isClassFile(fo))) {
                augmentedFiles.add(fake);
            }
            final JavaSource javaSource = JavaSource.create(info, augmentedFiles);
            if (modification) {
                results.add(javaSource.runModificationTask(cc -> {
                    if (cc.getFileObject() == fake) return ;
                    ((CancellableTask<WorkingCopy>) task).run(cc);
                })); // can throw IOException
            } else {
                javaSource.runUserActionTask(cc -> {
                    if (cc.getFileObject() == fake) return ;
                    currentTask.run(cc);
                }, true);
            }
        }
    }
    
    protected final Problem createAndAddElements(Set<FileObject> files, CancellableTask<WorkingCopy> task, RefactoringElementsBag elements, AbstractRefactoring refactoring, ClasspathInfo info) {
        try {
            final Collection<ModificationResult> results = processFiles(files, task, info);
            elements.registerTransaction(createTransaction(results));
            for (ModificationResult result:results) {
                for (FileObject jfo : result.getModifiedFileObjects()) {
                    for (Difference dif: result.getDifferences(jfo)) {
                            elements.add(refactoring,DiffElement.create(dif, jfo, result));
                    }
                }
            }
        } catch (IOException e) {
            return createProblemAndLog(null, e);
        }
        return null;
    }

    protected final Problem createAndAddElements(Set<FileObject> files, CancellableTask<WorkingCopy> task, RefactoringElementsBag elements, AbstractRefactoring refactoring) {
        return createAndAddElements(files, task, elements, refactoring, null);
    }
    
    protected final Problem createProblemAndLog(Problem p, Throwable t) {
        Throwable cause = t.getCause();
        Problem newProblem;
        if (cause != null && (cause.getClass().getName().equals("org.netbeans.api.java.source.JavaSource$InsufficientMemoryException") ||  //NOI18N
            (cause.getCause()!=null ) && cause.getCause().getClass().getName().equals("org.netbeans.api.java.source.JavaSource$InsufficientMemoryException"))) { //NOI18N
            newProblem = new Problem(true, NbBundle.getMessage(JavaRefactoringPlugin.class, "ERR_OutOfMemory"));
        } else {
            String msg = NbBundle.getMessage(JavaRefactoringPlugin.class, "ERR_ExceptionThrown", t.toString());
            newProblem = new Problem(true, msg);
        }
        Exceptions.printStackTrace(t);
        
        Problem problem;
        if (p == null) {
            return newProblem;
        }
        problem = p;
        while(problem.getNext() != null) {
            problem = problem.getNext();
        }
        problem.setNext(newProblem);
        return p;
    }
    
    private class WorkingTask implements Task<CompilationController> {
        
        private Phase whatRun;
        private Problem problem;

        private Problem run(Phase s) {
            this.whatRun = s;
            this.problem = null;
            JavaSource js = getJavaSource(s);
            if (js==null) {
                return null;
            }
            try {
                js.runUserActionTask(this, true);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return problem;
        }

        @Override
        public void run(CompilationController javac) throws Exception {
            switch(whatRun) {
            case PRECHECK:
                this.problem = preCheck(javac);
                break;
            case CHECKPARAMETERS:
                this.problem = checkParameters(javac);
                break;
            case FASTCHECKPARAMETERS:
                this.problem = fastCheckParameters(javac);
                break;
            default:
                throw new IllegalStateException();
            }
        }
        
    }
    
    protected class TransformTask implements CancellableTask<WorkingCopy> {
        
        private RefactoringVisitor visitor;
        private TreePathHandle treePathHandle;
        public TransformTask(RefactoringVisitor visitor, TreePathHandle searchedItem) {
            this.visitor = visitor;
            this.treePathHandle = searchedItem;
        }
        
        @Override
        public void cancel() {
        }
        
        @Override
        public void run(WorkingCopy compiler) throws IOException {
            try {
                try {
                    visitor.setWorkingCopy(compiler);
                } catch (ToPhaseException e) {
                    return;
                }
                CompilationUnitTree cu = compiler.getCompilationUnit();
                if (cu == null) {
                    ErrorManager.getDefault().log(ErrorManager.ERROR, "compiler.getCompilationUnit() is null " + compiler); // NOI18N
                    return;
                }
                Element el = null;
                if (treePathHandle != null) {
                    el = treePathHandle.resolveElement(compiler);
                    if (el == null) {
                        ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot resolve " + treePathHandle + "in " + compiler.getFileObject().getPath());
                        return;
                    }
                }

                visitor.scan(compiler.getCompilationUnit(), el);
            } finally {
                fireProgressListenerStep();
            }
        }
    }
}
