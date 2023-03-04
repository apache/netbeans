/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.refactoring.java.test;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import junit.framework.Test;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.*;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.api.WhereUsedQueryConstants;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Ralph Ruijs
 */
public class FindUsagesTest extends NbTestCase {
    private FileObject projectDir;
    private FileObject childProjectDir;
    private static final Logger LOG = Logger.getLogger(FindUsagesTest.class.getName());

    public FindUsagesTest(String name) {
        super(name);
    }

    /**
     * Set-up the services and project
     */
    @Override
    protected void setUp() throws IOException, InterruptedException {
        clearWorkDir();
        String work = getWorkDirPath();
        System.setProperty("netbeans.user", work);
        projectDir = Utilities.openProject("SimpleJ2SEApp", getDataDir());
        childProjectDir = Utilities.openProject("SimpleJ2SEAppChild", getDataDir());
        SourceUtils.waitScanFinished();
    }
    
    public void testFindUsages() throws IOException, InterruptedException, ExecutionException {
        FileObject testFile = projectDir.getFileObject("/src/simplej2seapp/Main.java");
        JavaSource src = JavaSource.forFileObject(testFile);
        final WhereUsedQuery[] wuq = new WhereUsedQuery[1];
        src.runWhenScanFinished(new Task<CompilationController>() {

            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement klass = controller.getElements().getTypeElement("simplej2seapp.Main");
                Element field = klass.getEnclosedElements().get(4);
                TreePathHandle element = TreePathHandle.create(field, controller);
                wuq[0] = new WhereUsedQuery(Lookups.singleton(element));
            }
        }, false).get();
        setParameters(wuq, true, false, false, false, false, false);
        
        doRefactoring("FindUsagesTest", wuq, 9);
    }
    
    public void testFindCurrentPackage() throws IOException, InterruptedException, ExecutionException {
        FileObject testFile = projectDir.getFileObject("/src/simplej2seapp/Main.java");
        JavaSource src = JavaSource.forFileObject(testFile);
        final WhereUsedQuery[] wuq = new WhereUsedQuery[1];
        src.runWhenScanFinished(new Task<CompilationController>() {

            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement klass = controller.getElements().getTypeElement("simplej2seapp.Main");
                Element field = klass.getEnclosedElements().get(4);
                TreePathHandle element = TreePathHandle.create(field, controller);
                wuq[0] = new WhereUsedQuery(Lookups.singleton(element));
            }
        }, false).get();
        setParameters(wuq, true, false, false, false, false, false);
        final NonRecursiveFolder package1 = new NonRecursiveFolder() {
            @Override
            public FileObject getFolder() {
                return projectDir.getFileObject("/src/package1");
            }
        };
        Collection<NonRecursiveFolder> folders = Arrays.asList(package1);
        Scope scope = Scope.create(null, folders, null);
        wuq[0].getContext().add(scope);

        doRefactoring("FindCurrentPackageTest", wuq, 1);
    }
    
    public void testFindSingleSourceRoot() throws IOException, InterruptedException, ExecutionException {
        FileObject testFile = projectDir.getFileObject("/src/simplej2seapp/Main.java");
        JavaSource src = JavaSource.forFileObject(testFile);
        final WhereUsedQuery[] wuq = new WhereUsedQuery[1];
        src.runWhenScanFinished(new Task<CompilationController>() {

            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement klass = controller.getElements().getTypeElement("simplej2seapp.Main");
                Element field = klass.getEnclosedElements().get(4);
                TreePathHandle element = TreePathHandle.create(field, controller);
                wuq[0] = new WhereUsedQuery(Lookups.singleton(element));
            }
        }, false).get();

        setParameters(wuq, true, false, false, false, false, false);
        
        Scope customScope = Scope.create(Arrays.asList(projectDir.getFileObject("/test")), null, null);
        wuq[0].getContext().add(customScope);

        doRefactoring("FindSingleSourceRootTest", wuq, 1);
    }
    
    public void testFindComplexScope() throws IOException, InterruptedException, ExecutionException {
        FileObject testFile = projectDir.getFileObject("/src/simplej2seapp/Main.java");
        JavaSource src = JavaSource.forFileObject(testFile);
        final WhereUsedQuery[] wuq = new WhereUsedQuery[1];
        src.runWhenScanFinished(new Task<CompilationController>() {

            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement klass = controller.getElements().getTypeElement("simplej2seapp.Main");
                Element field = klass.getEnclosedElements().get(4);
                TreePathHandle element = TreePathHandle.create(field, controller);
                wuq[0] = new WhereUsedQuery(Lookups.singleton(element));
            }
        }, false).get();

        setParameters(wuq, true, false, false, false, false, false);
        final NonRecursiveFolder package1 = new NonRecursiveFolder() {
            @Override
            public FileObject getFolder() {
                return projectDir.getFileObject("/src/package1");
            }
        };
        Scope customScope = Scope.create(Arrays.asList(projectDir.getFileObject("/test")),
                                         Arrays.asList(package1),
                                         Arrays.asList(projectDir.getFileObject("/src/simplej2seapp/B.java")));
        wuq[0].getContext().add(customScope);

        doRefactoring("FindComplexScopeTest", wuq, 3);
    }
    
    public void testFindSubclasses() throws IOException, InterruptedException, ExecutionException {
        FileObject testFile = projectDir.getFileObject("/src/simplej2seapp/Main.java");
        JavaSource src = JavaSource.forFileObject(testFile);
        final WhereUsedQuery[] wuq = new WhereUsedQuery[1];
        final CharSequence[] symbolName = new CharSequence[1];
        src.runWhenScanFinished(new Task<CompilationController>() {

            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement klass = controller.getElements().getTypeElement("simplej2seapp.Main");
                symbolName[0] = klass.getSimpleName();
                wuq[0] = new WhereUsedQuery(Lookups.singleton(TreePathHandle.create(klass, controller)));
            }
        }, false).get();
        
        setParameters(wuq, false, false, true, false, false, false);

        doRefactoring("FindSubClassesTest", wuq, 3);
    }
    
    public void test200230() throws IOException, InterruptedException, ExecutionException {
        FileObject testFile = projectDir.getFileObject("/src/simplej2seapp/Main.java");
        JavaSource src = JavaSource.forFileObject(testFile);
        final WhereUsedQuery[] wuq = new WhereUsedQuery[1];
        src.runWhenScanFinished(new Task<CompilationController>() {

            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement klass = controller.getElements().getTypeElement("simplej2seapp.Main");
                Element field = klass.getEnclosedElements().get(4);
                TreePathHandle element = TreePathHandle.create(field, controller);
                wuq[0] = new WhereUsedQuery(Lookups.singleton(element));
                wuq[0].getContext().add(RefactoringUtils.getClasspathInfoFor(element));
            }
        }, false).get();
        setParameters(wuq, true, false, false, false, false, false);

        doRefactoring("test200230", wuq, 9);
    }
    
    public void test200843() throws IOException, InterruptedException, ExecutionException {
        FileObject testFile = projectDir.getFileObject("/src/simplej2seapp/D.java");
        JavaSource src = JavaSource.forFileObject(testFile);
        final WhereUsedQuery[] wuq = new WhereUsedQuery[1];
        src.runWhenScanFinished(new Task<CompilationController>() {

            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                ClassTree klass = (ClassTree) controller.getCompilationUnit().getTypeDecls().get(0);
                MethodTree runTree = (MethodTree) klass.getMembers().get(1);
                TreePath path = controller.getTrees().getPath(controller.getCompilationUnit(), runTree);
                TreePathHandle element = TreePathHandle.create(path, controller);
                Element method = controller.getTrees().getElement(path);
                Collection<ExecutableElement> overridens = JavaRefactoringUtils.getOverriddenMethods((ExecutableElement)method, controller);
                wuq[0] = new WhereUsedQuery(Lookups.singleton(TreePathHandle.create(overridens.iterator().next(), controller)));
                wuq[0].getContext().add(element);
            }
        }, false).get();
        setParameters(wuq, true, false, false, false, false, true);

        doRefactoring("test200843", wuq, 1);
    }
    
    public void test204305() throws Exception { // #204305 - Find Usages: Search from Base doesn´t work 
        FileObject testFile = projectDir.getFileObject("/src/simplej2seapp/D.java");
        JavaSource src = JavaSource.forFileObject(testFile);
        final WhereUsedQuery[] wuq = new WhereUsedQuery[1];
        src.runWhenScanFinished(new Task<CompilationController>() {

            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                ClassTree klass = (ClassTree) controller.getCompilationUnit().getTypeDecls().get(0);
                MethodTree runTree = (MethodTree) klass.getMembers().get(1);
                TreePath path = controller.getTrees().getPath(controller.getCompilationUnit(), runTree);
                TreePathHandle element = TreePathHandle.create(path, controller);
                Element method = controller.getTrees().getElement(path);
                Collection<ExecutableElement> overridens = JavaRefactoringUtils.getOverriddenMethods((ExecutableElement)method, controller);
                wuq[0] = new WhereUsedQuery(Lookups.singleton(TreePathHandle.create(overridens.iterator().next(), controller)));
                wuq[0].getContext().add(element);
            }
        }, false).get();
        setParameters(wuq, true, false, false, false, false, true);
        
        final NonRecursiveFolder simplej2seapp = new NonRecursiveFolder() {
            @Override
            public FileObject getFolder() {
                return projectDir.getFileObject("/src/simplej2seapp");
            }
        };
        Scope customScope = Scope.create(Arrays.asList(projectDir.getFileObject("/src")), null, null);
        wuq[0].getContext().add(customScope);
        doRefactoring("test204305", wuq, 1);
        
        customScope = Scope.create(null, Arrays.asList(simplej2seapp), null);
        wuq[0].getContext().add(customScope);
        doRefactoring("test204305", wuq, 1);
        
        customScope = Scope.create(null, null, Arrays.asList(projectDir.getFileObject("/src/simplej2seapp/Main.java")));
        wuq[0].getContext().add(customScope);
        doRefactoring("test204305", wuq, 1);
        
        customScope = Scope.create(null, null, null);
        wuq[0].getContext().add(customScope);
        doRefactoring("test204305", wuq, 0);
    }
    
        
    public void test204519() throws Exception {
        FileObject testFile = projectDir.getFileObject("/src/simplej2seapp/D.java");
        JavaSource src = JavaSource.forFileObject(testFile);
        final WhereUsedQuery[] wuq = new WhereUsedQuery[1];
        src.runWhenScanFinished(new Task<CompilationController>() {

            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                ClassTree klass = (ClassTree) controller.getCompilationUnit().getTypeDecls().get(0);
                MethodTree runTree = (MethodTree) klass.getMembers().get(2);
                TreePath path = controller.getTrees().getPath(controller.getCompilationUnit(), runTree);
                TreePathHandle element = TreePathHandle.create(path, controller);
                Element method = controller.getTrees().getElement(path);
                Collection<ExecutableElement> overridens = JavaRefactoringUtils.getOverriddenMethods((ExecutableElement)method, controller);
                wuq[0] = new WhereUsedQuery(Lookups.singleton(TreePathHandle.create(overridens.iterator().next(), controller)));
                wuq[0].getContext().add(element);
            }
        }, false).get();
        setParameters(wuq, false, false, false, false, true, true);
        
        doRefactoring("test204305", wuq, 1);
        
        final NonRecursiveFolder simplej2seapp = new NonRecursiveFolder() {
            @Override
            public FileObject getFolder() {
                return projectDir.getFileObject("/src/simplej2seapp");
            }
        };
        Scope customScope = Scope.create(Arrays.asList(projectDir.getFileObject("/src")), null, null);
        wuq[0].getContext().add(customScope);
        doRefactoring("test204519", wuq, 1);
        
        customScope = Scope.create(null, Arrays.asList(simplej2seapp), null);
        wuq[0].getContext().add(customScope);
        doRefactoring("test204519", wuq, 1);
        
        customScope = Scope.create(null, null, Arrays.asList(projectDir.getFileObject("/src/simplej2seapp/D.java")));
        wuq[0].getContext().add(customScope);
        doRefactoring("test204519", wuq, 1);
        
        customScope = Scope.create(null, null, null);
        wuq[0].getContext().add(customScope);
        doRefactoring("test204519", wuq, 0);
    }

    public void test202412() throws IOException, InterruptedException, ExecutionException { // #202412 NullPointerException at org.netbeans.modules.refactoring.java.RefactoringUtils.getFileObject
        FileObject testFile = projectDir.getFileObject("/src/simplej2seapp/I.java");
        JavaSource src = JavaSource.forFileObject(testFile);
        final WhereUsedQuery[] wuq = new WhereUsedQuery[1];
        src.runWhenScanFinished(new Task<CompilationController>() {

            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                ClassTree klass = (ClassTree) controller.getCompilationUnit().getTypeDecls().get(0);
                MethodTree runTree = (MethodTree) klass.getMembers().get(0);
                TreePath path = controller.getTrees().getPath(controller.getCompilationUnit(), runTree);
                TreePathHandle element = TreePathHandle.create(path, controller);
                wuq[0] = new WhereUsedQuery(Lookups.singleton(element));
            }
        }, false).get();
        setParameters(wuq, true, false, false, false, false, false);

        doRefactoring("test202412", wuq, 1);
    }
    
    private void doRefactoring(final String name, final WhereUsedQuery[] wuq, final int amount) throws InterruptedException {
        RefactoringSession rs = RefactoringSession.create("Session");

        wuq[0].preCheck();
        wuq[0].fastCheckParameters();
        wuq[0].checkParameters();
        wuq[0].prepare(rs);
        rs.finished();
        rs.doRefactoring(true);
        
        Collection<RefactoringElement> elems = rs.getRefactoringElements();

        LOG.fine(name);
        for (RefactoringElement refactoringElement : elems) {
            LOG.fine(refactoringElement.getParentFile().getNameExt());
        }
        
        assertEquals("Number of usages", amount, elems.size());
    }
    
    private void setParameters(final org.netbeans.modules.refactoring.api.WhereUsedQuery[] wuq,
            boolean references, boolean comments, boolean subclasses,
            boolean directSubclasses, boolean overriding, boolean fromBaseclass) {
        wuq[0].putValue(WhereUsedQuery.FIND_REFERENCES, references);
        wuq[0].putValue(WhereUsedQuery.SEARCH_IN_COMMENTS, comments);
        wuq[0].putValue(WhereUsedQueryConstants.FIND_DIRECT_SUBCLASSES, directSubclasses);
        wuq[0].putValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS, overriding);
        wuq[0].putValue(WhereUsedQueryConstants.FIND_SUBCLASSES, subclasses);
        wuq[0].putValue(WhereUsedQueryConstants.SEARCH_FROM_BASECLASS, fromBaseclass);
    }
    
    public static Test suite() throws InterruptedException {
//        return NbModuleSuite.createConfiguration(FindUsagesTest.class)
//                .clusters(".*").enableModules(".*")
//                .gui(false).suite();
        return NbTestSuite.createTest(Noop.class, "noop");
    }
}
