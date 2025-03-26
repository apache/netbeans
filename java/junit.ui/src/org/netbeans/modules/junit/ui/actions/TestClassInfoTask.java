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
package org.netbeans.modules.junit.ui.actions;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController.TestMethod;
import org.netbeans.modules.java.testrunner.ui.spi.ComputeTestMethods;
import org.netbeans.modules.java.testrunner.ui.spi.ComputeTestMethods.Factory;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.java.hints.unused.UsedDetector;
import org.netbeans.spi.project.SingleMethod;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

public final class TestClassInfoTask implements Task<CompilationController> {

    private final int caretPosition;
    private SingleMethod singleMethod;

    private static final String JUNIT4_ANNOTATION = "org.junit.Test"; //NOI18N
    private static final String JUNIT5_NESTED_ANNOTATION = "org.junit.jupiter.api.Nested"; //NOI18N
    private static final String JUNIT5_ANNOTATION = "org.junit.platform.commons.annotation.Testable"; //NOI18N
    private static final String TESTCASE = "junit.framework.TestCase"; //NOI18N

    TestClassInfoTask(int caretPosition) {
        this.caretPosition = caretPosition;
    }

    @Override
    public void run(CompilationController controller) throws Exception {
        controller.toPhase(Phase.RESOLVED);
        List<TestMethod> methods = computeTestMethods(controller, new AtomicBoolean(), caretPosition);
        this.singleMethod = !methods.isEmpty() ? methods.get(0).method() : null;
    }

    public static List<TestMethod> computeTestMethods(CompilationInfo info, AtomicBoolean cancel, int caretPosIfAny) {
        FileObject fileObject = info.getFileObject();
        if (!isTestSource(fileObject)) {
            return Collections.emptyList();
        }
        return doComputeTestMethods(info, cancel, caretPosIfAny);
    }

    private static List<TestMethod> doComputeTestMethods(CompilationInfo info, AtomicBoolean cancel, int caretPosIfAny) {
        List<TestMethod> result = new ArrayList<>();
        if (caretPosIfAny == (-1)) {
            Optional<? extends Tree> anyClass = info.getCompilationUnit().getTypeDecls().stream().filter(t -> t.getKind() == Kind.CLASS).findAny();
            if (!anyClass.isPresent()) {
                return Collections.emptyList();
            }
            ClassTree clazz = (ClassTree) anyClass.get();
            TreePath pathToClass = new TreePath(new TreePath(info.getCompilationUnit()), clazz);
            List<TreePath> methods = clazz.getMembers().stream().filter(m -> m.getKind() == Kind.METHOD).map(m -> new TreePath(pathToClass, m)).collect(Collectors.toList());
            collect(info, pathToClass, methods, true, cancel, result);
            return result;
        }
        TreePath tp = info.getTreeUtilities().pathFor(caretPosIfAny);
        while (tp != null && tp.getLeaf().getKind() != Kind.METHOD) {
            tp = tp.getParentPath();
        }
        if (tp != null) {
            collect(info, tp.getParentPath(), Collections.singletonList(tp), false, cancel, result);
            return result;
        }
        return Collections.emptyList();
    }

    SingleMethod getSingleMethod() {
        return singleMethod;
    }

    private static void collect(CompilationInfo info, TreePath clazz, List<TreePath> methods, boolean searchNested, AtomicBoolean cancel, List<TestMethod> result) {
        Trees trees = info.getTrees();
        Elements elements = info.getElements();
        TreeUtilities treeUtilities = info.getTreeUtilities();
        int clazzPreferred = treeUtilities.findNameSpan((ClassTree) clazz.getLeaf())[0];
        TypeElement typeElement = (TypeElement) trees.getElement(clazz);
        TypeElement testcase = elements.getTypeElement(TESTCASE);
        boolean junit3 = (testcase != null && typeElement != null) ? info.getTypes().isSubtype(typeElement.asType(), testcase.asType()) : false;
        for (TreePath tp : methods) {
            if (cancel.get()) {
                return;
            }
            Element element = trees.getElement(tp);
            if (element != null) {
                String mn = element.getSimpleName().toString();
                boolean testMethod = false;
                if (junit3) {
                    testMethod = mn.startsWith("test"); //NOI18N
                } else {
                    List<? extends AnnotationMirror> allAnnotationMirrors = elements.getAllAnnotationMirrors(element);
                    if (isJunit4Test(allAnnotationMirrors) || isJunit5Testable(allAnnotationMirrors)) {
                        testMethod = true;
                    }
                }
                if (testMethod) {
                    SourcePositions sp = trees.getSourcePositions();
                    int start = (int) sp.getStartPosition(tp.getCompilationUnit(), tp.getLeaf());
                    int preferred = treeUtilities.findNameSpan((MethodTree) tp.getLeaf())[0];
                    int end = (int) sp.getEndPosition(tp.getCompilationUnit(), tp.getLeaf());
                    Document doc = info.getSnapshot().getSource().getDocument(false);
                    try {
                        result.add(new TestMethod(elements.getBinaryName(typeElement).toString(),
                                doc != null ? doc.createPosition(clazzPreferred) : new SimplePosition(clazzPreferred),
                                new SingleMethod(info.getFileObject(), mn),
                                doc != null ? doc.createPosition(start) : new SimplePosition(start),
                                doc != null ? doc.createPosition(preferred) : new SimplePosition(preferred),
                                doc != null ? doc.createPosition(end) : new SimplePosition(end)));
                    } catch (BadLocationException ex) {
                        //ignore
                    }
                }
            }
        }
        if (searchNested && !cancel.get()) {
            ((ClassTree)clazz.getLeaf()).getMembers().stream().filter(m -> m.getKind() == Kind.CLASS).map(n -> new TreePath(clazz, n)).forEach(nested -> {
                if (!cancel.get()) {
                    Element nestedElement = trees.getElement(nested);
                    if (nestedElement != null && !junit3) {
                        List<? extends AnnotationMirror> allAnnotationMirrors = elements.getAllAnnotationMirrors(nestedElement);
                        if (isJunit5Nested(allAnnotationMirrors)) {
                            List<TreePath> nestedMethods = ((ClassTree) nested.getLeaf()).getMembers().stream().filter(m -> m.getKind() == Kind.METHOD).map(m -> new TreePath(nested, m)).collect(Collectors.toList());
                            collect(info, nested, nestedMethods, true, cancel, result);
                        }
                    }
                }
            });
        }
    }

    private static boolean isTestSource(FileObject fo) {
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        if (cp != null) {
            FileObject root = cp.findOwnerRoot(fo);
            if (root != null) {
                return UnitTestForSourceQuery.findSources(root).length > 0;
            }
        }
        return false;
    }

    private static boolean isJunit4Test(List<? extends AnnotationMirror> allAnnotationMirrors) {
        for (Iterator<? extends AnnotationMirror> it = allAnnotationMirrors.iterator(); it.hasNext();) {
            AnnotationMirror annotationMirror = it.next();
            TypeElement typeElement = (TypeElement) annotationMirror.getAnnotationType().asElement();
            if (typeElement.getQualifiedName().contentEquals(JUNIT4_ANNOTATION)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean isJunit5Nested(List<? extends AnnotationMirror> allAnnotationMirrors) {
        for (Iterator<? extends AnnotationMirror> it = allAnnotationMirrors.iterator(); it.hasNext();) {
            AnnotationMirror annotationMirror = it.next();
            TypeElement typeElement = (TypeElement) annotationMirror.getAnnotationType().asElement();
            if (typeElement.getQualifiedName().contentEquals(JUNIT5_NESTED_ANNOTATION)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isJunit5Testable(List<? extends AnnotationMirror> allAnnotationMirrors) {
        Queue<AnnotationMirror> pendingMirrorsToCheck = new ArrayDeque<>(allAnnotationMirrors);
        Set<AnnotationMirror> alreadyAddedMirrorsToCheck = new HashSet<>(allAnnotationMirrors);
        
        while (pendingMirrorsToCheck.peek()!= null) {
            AnnotationMirror annotationMirror = pendingMirrorsToCheck.poll();
            TypeElement annotationElement = (TypeElement) annotationMirror.getAnnotationType().asElement();
            if (annotationElement.getQualifiedName().contentEquals(JUNIT5_ANNOTATION)) {
                return true;
            }
            List<? extends AnnotationMirror> parentAnnotationMirrors = annotationElement.getAnnotationMirrors();
            Set<? extends AnnotationMirror> newlySeenParentAnnotationMirrors = parentAnnotationMirrors.stream()
                .filter(parentAnnotationMirror -> !alreadyAddedMirrorsToCheck.contains(parentAnnotationMirror))
                .collect(Collectors.toSet());
            pendingMirrorsToCheck.addAll(newlySeenParentAnnotationMirrors);
            alreadyAddedMirrorsToCheck.addAll(newlySeenParentAnnotationMirrors);
        }
        return false;
    }

    @ServiceProvider(service=Factory.class)
    public static final class ComputeTestMethodsImpl implements Factory {

        @Override
        public ComputeTestMethods create() {
            return new TaskImpl();
        }

        private static class TaskImpl implements ComputeTestMethods {

            private final AtomicBoolean cancel = new AtomicBoolean();

            @Override
            public void cancel() {
                cancel.set(true);
            }

            @Override
            public List<TestMethod> computeTestMethods(CompilationInfo info) {
                return TestClassInfoTask.computeTestMethods(info, cancel, -1);
            }
        }
    }

    @ServiceProvider(service=UsedDetector.Factory.class)
    public static final class UsedDetectorImpl implements UsedDetector.Factory {

        @Override
        public UsedDetector create(CompilationInfo info) {
            if (isTestSource(info.getFileObject())) {
                List<TestMethod> testMethods = TestClassInfoTask.doComputeTestMethods(info, new AtomicBoolean(), -1);
                SourcePositions sp = info.getTrees().getSourcePositions();
                return (el, path) -> {
                    if (el.getKind() == ElementKind.METHOD) {
                        for (TestMethod tm : testMethods) {
                            if (tm.method().getMethodName().contentEquals(el.getSimpleName())
                                    && tm.start().getOffset() == sp.getStartPosition(path.getCompilationUnit(), path.getLeaf())
                                    && tm.end().getOffset() == sp.getEndPosition(path.getCompilationUnit(), path.getLeaf())) {
                                return true;
                            }
                        }
                    }
                    return false;
                };
            }
            return null;
        }
    }

    @MimeRegistration(mimeType="text/x-java", service=org.netbeans.modules.gsf.testrunner.ui.spi.ComputeTestMethods.class)
    public static final class JUnitComputeTestMethodsImpl implements org.netbeans.modules.gsf.testrunner.ui.spi.ComputeTestMethods {

        @Override
        public List<TestMethod> computeTestMethods(Parser.Result parserResult, AtomicBoolean cancel) {
            try {
                CompilationController cc = CompilationController.get(parserResult);
                if (isTestSource(cc.getFileObject()) && cc.toPhase(Phase.ELEMENTS_RESOLVED).compareTo(Phase.ELEMENTS_RESOLVED) >= 0) {
                    return TestClassInfoTask.doComputeTestMethods(cc, cancel, -1);
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            return Collections.emptyList();
        }
    }

    private static class SimplePosition implements Position {

        private final int offset;

        private SimplePosition(int offset) {
            this.offset = offset;
        }

        @Override
        public int getOffset() {
            return offset;
        }
    }
}
