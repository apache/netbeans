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

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.openide.filesystems.FileObject;

final class TestClassInfoTask implements CancellableTask<CompilationController> {

    private FileObject fileObject;

    private final int caretPosition;
    private String className;
    private String methodName;

    private static final String JUNIT4_ANNOTATION = "org.junit.Test"; //NOI18N
    private static final String JUNIT5_ANNOTATION = "org.junit.platform.commons.annotation.Testable"; //NOI18N
    private static final String TESTCASE = "junit.framework.TestCase"; //NOI18N

    TestClassInfoTask(int caretPosition) {
        this.caretPosition = caretPosition;
    }

    @Override
    public void cancel() {
    }

    @Override
    public void run(CompilationController controller) throws Exception {
        controller.toPhase(Phase.RESOLVED);
        fileObject = controller.getFileObject();
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
        TypeElement testcase = elements.getTypeElement(TESTCASE);
        boolean junit3 = (testcase != null && typeElement != null) ? controller.getTypes().isSubtype(typeElement.asType(), testcase.asType()) : false;
        TreePath tp = controller.getTreeUtilities().pathFor(caretPosition);
        while (tp != null && tp.getLeaf().getKind() != Kind.METHOD) {
            tp = tp.getParentPath();
        }
        if (tp != null) {
            Element element = controller.getTrees().getElement(tp);
            if (element != null) {
                String mn = element.getSimpleName().toString();
                if (junit3) {
                    methodName = mn.startsWith("test") ? mn : null; //NOI18N
                } else {
                    List<? extends AnnotationMirror> allAnnotationMirrors = elements.getAllAnnotationMirrors(element);
                    if (isJunit4Test(allAnnotationMirrors) || isJunit5Testable(allAnnotationMirrors)) {
                        methodName = mn;
                    }
                }
            }
        }
    }

    public FileObject getFileObject() {
        return fileObject;
    }

    String getClassName() {
        return className;
    }

    String getMethodName() {
        return methodName;
    }

    private boolean isJunit4Test(List<? extends AnnotationMirror> allAnnotationMirrors) {
        for (Iterator<? extends AnnotationMirror> it = allAnnotationMirrors.iterator(); it.hasNext();) {
            AnnotationMirror annotationMirror = it.next();
            TypeElement typeElement = (TypeElement) annotationMirror.getAnnotationType().asElement();
            if (typeElement.getQualifiedName().contentEquals(JUNIT4_ANNOTATION)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isJunit5Testable(List<? extends AnnotationMirror> allAnnotationMirrors) {
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
}
