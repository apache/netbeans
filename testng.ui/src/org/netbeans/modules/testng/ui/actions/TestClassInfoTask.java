/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2008-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
