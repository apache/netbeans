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
package org.netbeans.modules.testng.ui.actions;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController.TestMethod;
import org.netbeans.modules.java.testrunner.ui.spi.ComputeTestMethods;
import org.netbeans.modules.java.testrunner.ui.spi.ComputeTestMethods.Factory;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.project.SingleMethod;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lukas
 */
public final class TestClassInfoTask implements CancellableTask<CompilationController> {

    private final int caretPosition;
    private String packageName;
    private String className;
    private String methodName;
    private FileObject fo;
    
    /**
     * <b>DO NOT USE!</b> Package private due to use in tests
     */
    static String ANNOTATION = "org.testng.annotations.Test"; //NOI18N
    static String TESTNG_ANNOTATION_PACKAGE = "org.testng.annotations"; //NOI18N

    TestClassInfoTask(int caretPosition) {
        this.caretPosition = caretPosition;
    }

    public void cancel() {
    }

    public void run(CompilationController controller) throws Exception {
        controller.toPhase(Phase.RESOLVED);
        fo = controller.getFileObject();
        TypeElement typeElement = null;
        List<? extends TypeElement> topLevelElements = controller.getTopLevelElements();
        for (Iterator<? extends TypeElement> it = topLevelElements.iterator(); it.hasNext();) {
            typeElement = it.next();
            if (typeElement.getKind() == ElementKind.CLASS) {
                className = typeElement.getSimpleName().toString();
                break;
            }
        }
        Elements elements = controller.getElements();
        if (typeElement != null) {
            packageName = elements.getPackageOf(typeElement).getQualifiedName().toString();
        }
        List<TestMethod> testMethods = computeTestMethods(controller, new AtomicBoolean(), caretPosition);
        if (!testMethods.isEmpty()) {
            methodName = testMethods.iterator().next().method().getMethodName();
        }
    }

    String getClassName() {
        return className;
    }

    String getMethodName() {
        return methodName;
    }

    String getPackageName() {
        return packageName;
    }
    
    FileObject getFileObject() {
        return fo;
    }

    public static List<TestMethod> computeTestMethods(CompilationInfo info, AtomicBoolean cancel, int caretPosIfAny) {
        //TODO: first verify if this is a test class/class in a test source group?
        FileObject fileObject = info.getFileObject();
        ClassTree clazz;
        List<TreePath> methods;
        if (caretPosIfAny == (-1)) {
            Optional<? extends Tree> anyClass = info.getCompilationUnit().getTypeDecls().stream().filter(t -> t.getKind() == Kind.CLASS).findAny();
            if (!anyClass.isPresent()) {
                return Collections.emptyList();
            }
            clazz = (ClassTree) anyClass.get();
            TreePath pathToClass = new TreePath(new TreePath(info.getCompilationUnit()), clazz);
            methods = clazz.getMembers().stream().filter(m -> m.getKind() == Kind.METHOD).map(m -> new TreePath(pathToClass, m)).collect(Collectors.toList());
        } else {
            TreePath tp = info.getTreeUtilities().pathFor(caretPosIfAny);
            while (tp != null && tp.getLeaf().getKind() != Kind.METHOD) {
                tp = tp.getParentPath();
            }
            if (tp != null) {
                clazz = (ClassTree) tp.getParentPath().getLeaf();
                methods = Collections.singletonList(tp);
            } else {
                return Collections.emptyList();
            }
        }
        TypeElement typeElement = (TypeElement) info.getTrees().getElement(new TreePath(new TreePath(info.getCompilationUnit()), clazz));
        Elements elements = info.getElements();
        boolean hasClassLevelAnnotation = hasTestNGTestAnnotation(elements, typeElement);
        List<TestMethod> result = new ArrayList<>();
        for (TreePath tp : methods) {
            if (cancel.get()) {
                return null;
            }
            Element element = info.getTrees().getElement(tp);
            if (element != null && element.getKind() == ElementKind.METHOD) {
                if (hasTestNGTestAnnotation(elements, element) ||
                    (hasClassLevelAnnotation && element.getModifiers().contains(Modifier.PUBLIC) &&
                     !hasTestNGAnnotation(elements, element))) {
                    String mn = element.getSimpleName().toString();
                    SourcePositions sp = info.getTrees().getSourcePositions();
                    int start = (int) sp.getStartPosition(tp.getCompilationUnit(), tp.getLeaf());
                    int preferred = info.getTreeUtilities().findNameSpan((MethodTree) tp.getLeaf())[0];
                    int end = (int) sp.getEndPosition(tp.getCompilationUnit(), tp.getLeaf());
                    Document doc = info.getSnapshot().getSource().getDocument(false);
                    try {
                        result.add(new TestMethod(typeElement.getQualifiedName().toString(), new SingleMethod(fileObject, mn),
                                doc != null ? doc.createPosition(start) : new SimplePosition(start),
                                doc != null ? doc.createPosition(preferred) : new SimplePosition(preferred),
                                doc != null ? doc.createPosition(end) : new SimplePosition(end)));
                    } catch (BadLocationException ex) {
                        //ignore
                    }
                }
            }
        }
        return result;
    }

    private static boolean hasTestNGTestAnnotation(Elements elements, Element element) {
        return elements.getAllAnnotationMirrors(element)
                       .stream()
                       .map(am -> (TypeElement) am.getAnnotationType().asElement())
                       .anyMatch(annTypeElement -> annTypeElement.getQualifiedName().contentEquals(ANNOTATION));
    }

    private static boolean hasTestNGAnnotation(Elements elements, Element element) {
        return elements.getAllAnnotationMirrors(element)
                       .stream()
                       .map(am -> (TypeElement) am.getAnnotationType().asElement())
                       .map(te -> (QualifiedNameable) te.getEnclosingElement())
                       .anyMatch(annTypeElement -> annTypeElement.getQualifiedName().contentEquals(TESTNG_ANNOTATION_PACKAGE));
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

    @MimeRegistration(mimeType="text/x-java", service=org.netbeans.modules.gsf.testrunner.ui.spi.ComputeTestMethods.class)
    public static final class TestNGComputeTestMethodsImpl implements org.netbeans.modules.gsf.testrunner.ui.spi.ComputeTestMethods {

        @Override
        public List<TestMethod> computeTestMethods(Parser.Result parserResult, AtomicBoolean cancel) {
            try {
                CompilationController cc = CompilationController.get(parserResult);
                if (isTestSource(cc.getFileObject()) && cc.toPhase(Phase.ELEMENTS_RESOLVED).compareTo(Phase.ELEMENTS_RESOLVED) >= 0) {
                    return TestClassInfoTask.computeTestMethods(cc, cancel, -1);
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            return Collections.emptyList();
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
