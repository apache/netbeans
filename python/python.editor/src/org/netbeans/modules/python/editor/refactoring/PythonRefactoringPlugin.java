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
package org.netbeans.modules.python.editor.refactoring;

/*
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsfpath.api.classpath.ClassPath;
import org.netbeans.modules.python.editor.PythonUtils;
import org.netbeans.napi.gsfret.source.ClasspathInfo;
import org.netbeans.napi.gsfret.source.CompilationController;
import org.netbeans.napi.gsfret.source.ModificationResult;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.napi.gsfret.source.WorkingCopy;
import org.netbeans.modules.refactoring.spi.*;
import org.netbeans.modules.refactoring.api.*;
import org.openide.filesystems.FileObject;
*/
/**
 * Plugin implementation based on the one for Java refactoring.
 * 
 */
/* Uncomment when it works ;-)
public abstract class PythonRefactoringPlugin extends ProgressProviderAdapter implements RefactoringPlugin, CancellableTask<CompilationController> {
    protected enum Phase {
        PRECHECK, FASTCHECKPARAMETERS, CHECKPARAMETERS, PREPARE, DEFAULT
    };
    private Phase whatRun = Phase.DEFAULT;
    private Problem problem;
    protected volatile boolean cancelRequest = false;
    private volatile CancellableTask currentTask;

    protected abstract Problem preCheck(CompilationController javac) throws IOException;

    protected abstract Problem checkParameters(CompilationController javac) throws IOException;

    protected abstract Problem fastCheckParameters(CompilationController javac) throws IOException;

    protected abstract Source getPythonSource(Phase p);

    public void cancel() {
    }

    public final void run(CompilationController javac) throws Exception {
        switch (whatRun) {
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

    public Problem preCheck() {
        return run(Phase.PRECHECK);
    }

    public Problem checkParameters() {
        return run(Phase.CHECKPARAMETERS);
    }

    public Problem fastCheckParameters() {
        return run(Phase.FASTCHECKPARAMETERS);
    }

    private Problem run(Phase s) {
        this.whatRun = s;
        this.problem = null;
        Source js = getPythonSource(s);
        if (js == null) {
            return null;
        }
        try {
            js.runUserActionTask(this, true);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return problem;
    }

    public void cancelRequest() {
        cancelRequest = true;
        if (currentTask != null) {
            currentTask.cancel();
        }
    }

    protected ClasspathInfo getClasspathInfo(AbstractRefactoring refactoring) {
        ClasspathInfo cpInfo = refactoring.getContext().lookup(ClasspathInfo.class);
        if (cpInfo == null) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Missing scope (ClasspathInfo), using default scope (all open projects)");
            cpInfo = PythonRefUtils.getClasspathInfoFor((FileObject)null);
            if (cpInfo != null) {
                refactoring.getContext().add(cpInfo);
            }
        }
        return cpInfo;
    }

    protected static final Problem createProblem(Problem result, boolean isFatal, String message) {
        Problem problem = new Problem(isFatal, message);
        if (result == null) {
            return problem;
        } else if (isFatal) {
            problem.setNext(result);
            return problem;
        } else {
            //problem.setNext(result.getNext());
            //result.setNext(problem);

            // [TODO] performance
            Problem p = result;
            while (p.getNext() != null) {
                p = p.getNext();
            }
            p.setNext(problem);
            return result;
        }
    }

    private Iterable<? extends List<FileObject>> groupByRoot(Iterable<? extends FileObject> data) {
        Map<FileObject, List<FileObject>> result = new HashMap<FileObject, List<FileObject>>();
        for (FileObject file : data) {
            ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);
            if (cp != null) {
                FileObject root = cp.findOwnerRoot(file);
                if (root != null) {
                    List<FileObject> subr = result.get(root);
                    if (subr == null) {
                        subr = new LinkedList<FileObject>();
                        result.put(root, subr);
                    }
                    subr.add(file);
                }
            }
        }
        return result.values();
    }

    protected final Collection<ModificationResult> processFiles(Set<FileObject> files, CancellableTask<WorkingCopy> task) {
        currentTask = task;
        Collection<ModificationResult> results = new LinkedList<ModificationResult>();
        try {
            // Process Python files and RHTML files separately - and OTHER files separately
            // TODO - now that I don't need separate RHTML models any more, can
            // I just do a single pass?
            Set<FileObject> pythonFiles = new HashSet<FileObject>(2 * files.size());
            Set<FileObject> rhtmlFiles = new HashSet<FileObject>(2 * files.size());
            for (FileObject file : files) {
                if (PythonUtils.isPythonFile(file)) {
                    pythonFiles.add(file);
                //} else if (PythonUtils.isRhtmlOrYamlFile(file)) {
                //    // Avoid opening HUGE Yaml files - they may be containing primarily data
                //    if (file.getSize() > 512*1024) {
                //        continue;
                //    }
                //    rhtmlFiles.add(file);
                }
            }

            Iterable<? extends List<FileObject>> work = groupByRoot(pythonFiles);
            for (List<FileObject> fos : work) {
                final Source source = Source.create(ClasspathInfo.create(fos.get(0)), fos);
                try {
                    results.add(source.runModificationTask(task));
                } catch (IOException ex) {
                    throw (RuntimeException)new RuntimeException().initCause(ex);
                }
            }
            work = groupByRoot(rhtmlFiles);
            for (List<FileObject> fos : work) {
                final Source source = Source.create(ClasspathInfo.create(fos.get(0)), fos);
                try {
                    results.add(source.runModificationTask(task));
                } catch (IOException ex) {
                    throw (RuntimeException)new RuntimeException().initCause(ex);
                }
            }
        } finally {
            currentTask = null;
        }
        return results;
    }

    protected class TransformTask implements CancellableTask<WorkingCopy> {
        private SearchVisitor visitor;
        private PythonElementCtx treePathHandle;

        public TransformTask(SearchVisitor visitor, PythonElementCtx searchedItem) {
            this.visitor = visitor;
            this.treePathHandle = searchedItem;
        }

        public void cancel() {
        }

        public void run(WorkingCopy compiler) throws IOException {
            visitor.setWorkingCopy(compiler);
            visitor.scan();

            //for (PythonElementCtx tree : visitor.getUsages()) {
            //    ElementGripFactory.getDefault().put(compiler.getFileObject(), tree, compiler);
            //}

            fireProgressListenerStep();
        }
    }
}
*/
