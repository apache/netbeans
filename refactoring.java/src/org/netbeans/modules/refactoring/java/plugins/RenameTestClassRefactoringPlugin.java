/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.java.ui.JavaRenameProperties;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.spi.gototest.TestLocator;
import org.netbeans.spi.gototest.TestLocator.LocationResult;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Ralph Ruijs
 */
public class RenameTestClassRefactoringPlugin extends JavaRefactoringPlugin {

    public static final EnumSet<ElementKind> SUPPORTED = EnumSet.of(ElementKind.CLASS, ElementKind.ENUM, ElementKind.INTERFACE, ElementKind.ANNOTATION_TYPE, ElementKind.METHOD);
    private RenameRefactoring refactoring;
    private TreePathHandle treePathHandle;
    private RenameRefactoring[] renameDelegates;

    /** Creates a new instance of RenamePropertyRefactoringPlugin */
    public RenameTestClassRefactoringPlugin(RenameRefactoring rename) {
        this.refactoring = rename;
        treePathHandle = rename.getRefactoringSource().lookup(TreePathHandle.class);
    }

    @Override
    protected JavaSource getJavaSource(Phase phase) {
        return JavaSource.forFileObject(treePathHandle.getFileObject());
    }

    @Override
    public Problem checkParameters() {
        if (!isRenameTestClass() && !isRenameTestClassMethod()) {
            return null;
        }

        initDelegates();

        Problem p = null;
        for (RenameRefactoring delegate : renameDelegates) {
            p = JavaPluginUtils.chainProblems(p, delegate.checkParameters());
            if (p != null && p.isFatal()) {
                return p;
            }
        }
        return JavaPluginUtils.chainProblems(p, super.checkParameters());
    }

    @Override
    public Problem fastCheckParameters() {
        if (!isRenameTestClass() && !isRenameTestClassMethod()) {
            return null;
        }
        initDelegates();

        Problem p = null;
        for (RenameRefactoring delegate : renameDelegates) {
            FileObject delegateFile = delegate.getRefactoringSource().lookup(FileObject.class);
	    if(!isRenameTestClassMethod()) {
		delegate.setNewName(newName(treePathHandle.getFileObject(), delegateFile, refactoring.getNewName()));
	    }
            p = JavaPluginUtils.chainProblems(p, delegate.fastCheckParameters());
            if (p != null && p.isFatal()) {
                return p;
            }
        }
        return JavaPluginUtils.chainProblems(p, super.fastCheckParameters());
    }

    @Override
    protected Problem preCheck(CompilationController javac) throws IOException {
        if (!isRenameTestClass() && !isRenameTestClassMethod()) {
            return null;
        }
        initDelegates();
        Problem p = null;
        for (RenameRefactoring delegate : renameDelegates) {
            p = JavaPluginUtils.chainProblems(p, delegate.preCheck());
            if (p != null && p.isFatal()) {
                return p;
            }
        }
        return JavaPluginUtils.chainProblems(p, super.preCheck(javac));
    }

    @Override
    public Problem prepare(RefactoringElementsBag reb) {
        if (!isRenameTestClass() && !isRenameTestClassMethod()) {
            return null;
        }
        initDelegates();
        fireProgressListenerStart(ProgressEvent.START, renameDelegates.length);
        Problem p = null;
        for (RenameRefactoring delegate : renameDelegates) {
            p = JavaPluginUtils.chainProblems(p, delegate.prepare(reb.getSession()));
            if (p != null && p.isFatal()) {
                return p;
            }
            fireProgressListenerStep();
        }
        fireProgressListenerStop();
        return p;
    }

    private boolean isRenameTestClass() {
        JavaRenameProperties renameProps = refactoring.getContext().lookup(JavaRenameProperties.class);
        if (renameProps != null && renameProps.isIsRenameTestClass()) {
            return true;
        }
        return false;
    }

    private boolean isRenameTestClassMethod() {
        JavaRenameProperties renameProps = refactoring.getContext().lookup(JavaRenameProperties.class);
        if (renameProps != null && renameProps.isIsRenameTestClassMethod()) {
            return true;
        }
        return false;
    }

    private boolean inited = false;

    private void initDelegates() {
        if (inited) {
            return;
        }
        
        final List<RenameRefactoring> renameRefactoringsList = Collections.synchronizedList(new LinkedList<RenameRefactoring>());
        final ElementKind elementKind = treePathHandle.getElementHandle().getKind();

        if(SUPPORTED.contains(elementKind)) {
            final FileObject fileObject = treePathHandle.getFileObject();
            Collection<? extends TestLocator> testLocators = Lookup.getDefault().lookupAll(TestLocator.class);
            for (final TestLocator testLocator : testLocators) {
                if(testLocator.appliesTo(fileObject)) {
                    if(testLocator.asynchronous()) {
                        CountDownLatch latch = new CountDownLatch(1);
                        testLocator.findOpposite(fileObject, -1, new TestLocator.LocationListener() {
                            @Override
                            public void foundLocation(FileObject fo, LocationResult location) {
                                try {
                                    if(elementKind == ElementKind.CLASS) {
                                        addIfMatch(location, testLocator, fo, renameRefactoringsList);
                                    } else if(elementKind == ElementKind.METHOD) {
                                        addIfMatchMethod(location, testLocator, renameRefactoringsList);
                                    }
                                } finally {
                                    latch.countDown();
                                }
                            }
                        });
                        try {
                            latch.await(10000000000L, TimeUnit.NANOSECONDS);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(RenamePropertyRefactoringPlugin.class.getName())
                                    .fine("Finding test class took too long, or it was interupted"); //NOI18N
                        }
                    } else {
                        LocationResult location = testLocator.findOpposite(fileObject, -1);
                        if (elementKind == ElementKind.METHOD) {
                            addIfMatchMethod(location, testLocator, renameRefactoringsList);
                        } else {
                            addIfMatch(location, testLocator, fileObject, renameRefactoringsList);
                        }
                    }
                }
            }
        }
        
        renameDelegates = renameRefactoringsList.toArray(new RenameRefactoring[0]);
        inited = true;
    }
    
    private static String newName(FileObject testedFile, FileObject testFile, String newName) {
        String testedName = testedFile.getName();
        String testName = testFile.getName();
        
        return testName.replace(testedName, newName);
    }
    
    private void addIfMatch(LocationResult location, final TestLocator testLocator, final FileObject fileObject, final List<RenameRefactoring> renameRefactoringsList) {
        if(location.getFileObject() != null && testLocator.getFileType(location.getFileObject()).equals(TestLocator.FileType.TEST)) {
            RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(location.getFileObject()));
            renameRefactoring.setNewName(newName(fileObject, location.getFileObject(), refactoring.getNewName()));
            renameRefactoring.setSearchInComments(true);
            renameRefactoringsList.add(renameRefactoring);
        }
    }

    private void addIfMatchMethod(final LocationResult location, final TestLocator testLocator, final List<RenameRefactoring> renameRefactoringsList) {
        if(location.getFileObject() != null && testLocator.getFileType(location.getFileObject()).equals(TestLocator.FileType.TEST)) {
	    try {
		JavaSource.forFileObject(location.getFileObject()).runUserActionTask(new Task<CompilationController>() {
		    @Override
		    public void run(CompilationController javac) throws Exception {
			final Element methodElement = treePathHandle.resolveElement(javac);
			String methodName = methodElement.getSimpleName().toString();
			String testMethodName = RefactoringUtils.getTestMethodName(methodName);
			javac.toPhase(JavaSource.Phase.RESOLVED);
			CompilationUnitTree cut = javac.getCompilationUnit();
			Tree classTree = cut.getTypeDecls().get(0);
			List<? extends Tree> members = ((ClassTree) classTree).getMembers();
			for (int i = 0; i < members.size(); i++) {
                            Tree member = members.get(i);
                            if(member.getKind() != Tree.Kind.METHOD) {
                                continue;
                            }
                            MethodTree methodTree = (MethodTree) member;
			    if (methodTree.getName().contentEquals(testMethodName)
                                    && methodTree.getReturnType().getKind() == Tree.Kind.PRIMITIVE_TYPE
                                    && ((PrimitiveTypeTree) methodTree.getReturnType()).getPrimitiveTypeKind() == TypeKind.VOID) {
                                 // test method should at least be void
                                classTree = ((ClassTree) classTree).getMembers().get(i);
                                TreePath tp = TreePath.getPath(cut, classTree);
                                RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(TreePathHandle.create(tp, javac)));
                                renameRefactoring.setNewName(RefactoringUtils.getTestMethodName(refactoring.getNewName()));
                                renameRefactoring.setSearchInComments(true);
                                renameRefactoringsList.add(renameRefactoring);
                                break;
                            }
			}
		    }
		}, true);
	    } catch (IOException ex) {
		Exceptions.printStackTrace(ex);
	    }
	}
    }
}
