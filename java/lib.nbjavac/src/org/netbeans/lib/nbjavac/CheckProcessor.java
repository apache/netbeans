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
package org.netbeans.lib.nbjavac;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("*")
public class CheckProcessor extends AbstractProcessor implements TaskListener {

    private boolean listenerInstalled;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!listenerInstalled) {
            JavacTask.instance(processingEnv).addTaskListener(this);
            listenerInstalled = true;
        }

        return false;
    }

    @Override
    public void finished(TaskEvent e) {
        if (e.getKind() != TaskEvent.Kind.ANALYZE) {
            return ;
        }
        String path = e.getCompilationUnit().getSourceFile().toUri().toString();
        if (path.contains("test") || !path.contains("org/netbeans")) {
            //only applies to NetBeans code, and tests are excepted
            return ;
        }
        new TreePathScanner<Void, Void>() {
            @Override
            public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
                TreePath selectPath = new TreePath(getCurrentPath(), node.getMethodSelect());
                Trees trees = Trees.instance(processingEnv);
                Element el = trees.getElement(selectPath);

                if (el != null &&
                    el.getKind() == ElementKind.METHOD &&
                    ((QualifiedNameable) el.getEnclosingElement()).getQualifiedName().contentEquals("org.netbeans.api.java.source.TreeMaker") &&
                    (el.getSimpleName().contentEquals("Class") || el.getSimpleName().contentEquals("Interface")) &&
                    processingEnv.getElementUtils().isDeprecated(el)) {
                    trees.printMessage(Diagnostic.Kind.ERROR, "Use of the deprecated TreeMaker.Class/Interface method is not permitted inside the NetBeans sources.", node, selectPath.getCompilationUnit());
                }

                return super.visitMethodInvocation(node, p);
            }
        }.scan(e.getCompilationUnit(), null);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
