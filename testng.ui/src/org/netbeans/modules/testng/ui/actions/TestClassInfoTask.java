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

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lukas
 */
final class TestClassInfoTask implements CancellableTask<CompilationController> {

    private final int caretPosition;
    private String packageName;
    private String className;
    private String methodName;
    private FileObject fo;
    
    /**
     * <b>DO NOT USE!</b> Package private due to use in tests
     */
    static String ANNOTATION = "org.testng.annotations.Test"; //NOI18N

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
        TreePath tp = controller.getTreeUtilities().pathFor(caretPosition);
        while (tp != null && tp.getLeaf().getKind() != Kind.METHOD) {
            tp = tp.getParentPath();
        }
        if (tp != null) {
            Element element = controller.getTrees().getElement(tp);
            List<? extends AnnotationMirror> allAnnotationMirrors = elements.getAllAnnotationMirrors(element);
            for (Iterator<? extends AnnotationMirror> it = allAnnotationMirrors.iterator(); it.hasNext();) {
                AnnotationMirror annotationMirror = it.next();
                typeElement = (TypeElement) annotationMirror.getAnnotationType().asElement();
                if (typeElement.getQualifiedName().contentEquals(ANNOTATION)) {
                    methodName = element.getSimpleName().toString();
                    break;
                }
            }
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
}
