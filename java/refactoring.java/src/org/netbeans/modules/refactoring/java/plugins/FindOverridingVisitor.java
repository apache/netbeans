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

package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;

/**
 *
 * @author Jan Becicka
 */
public class FindOverridingVisitor extends FindVisitor {

    public FindOverridingVisitor(CompilationController workingCopy) {
        super(workingCopy);
    }

    @Override
    public Tree visitMethod(MethodTree node, Element elementToFind) {
        if (!workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            ExecutableElement el = (ExecutableElement) workingCopy.getTrees().getElement(getCurrentPath());
            
            if (el != null && workingCopy.getElements().overrides(el, (ExecutableElement) elementToFind, (TypeElement) el.getEnclosingElement())) {
                addUsage(getCurrentPath());
            }
        }
        return super.visitMethod(node, elementToFind);
    }

    @Override
    public Tree visitLambdaExpression(LambdaExpressionTree node, Element elementToFind) {
        Element type = elementToFind.getEnclosingElement();
        if (type.getKind() == ElementKind.INTERFACE &&
                workingCopy.getElements().isFunctionalInterface((TypeElement) type) &&
                !workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            
            TypeMirror typeMirror = workingCopy.getTrees().getTypeMirror(getCurrentPath());
            
            if (typeMirror != null && workingCopy.getTypes().isSameType(typeMirror, type.asType())) {
                addUsage(getCurrentPath());
            }
        }
        return super.visitLambdaExpression(node, elementToFind);
    }
}
