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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CompilationController;

/**
 *
 * @author Jan Becicka
 */
public class FindSubtypesVisitor extends FindVisitor {

    private boolean recursive;
    public FindSubtypesVisitor(boolean recursive, CompilationController workingCopy) {
        super(workingCopy);
        this.recursive = recursive;
    }

    @Override
    public Tree visitClass(ClassTree node, Element elementToFind) {
        if (workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            return super.visitClass(node, elementToFind);
        }
        Trees trees = workingCopy.getTrees();
        Types types = workingCopy.getTypes();
        TypeMirror type2 = elementToFind.asType();
        type2 = types.erasure(type2);
        
        if (recursive) {
            TypeMirror type1 = trees.getTypeMirror(getCurrentPath());
            if (type1 != null) {
                type1 = types.erasure(type1);
                if (isSubtype(type1, type2)) {
                    addUsage(getCurrentPath());
                }
            }
        } else {
            TypeElement el = (TypeElement) workingCopy.getTrees().getElement(getCurrentPath());
            if (el != null && el.getSuperclass()!=null && types.isSameType(types.erasure(el.getSuperclass()), type2) || containsType(el.getInterfaces(), type2)) {
                addUsage(getCurrentPath());
            }
        }
        return super.visitClass(node, elementToFind);
    }
    
    @Override
    public Tree visitLambdaExpression(LambdaExpressionTree node, Element elementToFind) {
        if (workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            return super.visitLambdaExpression(node, elementToFind);
        }
        Trees trees = workingCopy.getTrees();
        Types types = workingCopy.getTypes();
        TypeMirror type1 = trees.getTypeMirror(getCurrentPath());
        if(type1 == null) {
            return super.visitLambdaExpression(node, elementToFind);
        }
        type1 = types.erasure(type1);
        TypeMirror type2 = elementToFind.asType();
        type2 = types.erasure(type2);
        
        if (types.isSameType(type1, type2) || (recursive && isSubtype(type1, type2))) {
            addUsage(getCurrentPath());
        }
        return super.visitLambdaExpression(node, elementToFind);
    }
    
    private boolean containsType(List<? extends TypeMirror> list, TypeMirror t) {
        Types types = workingCopy.getTypes();
        for (TypeMirror m:list) {
            if (types.isSameType(t, types.erasure(m))) {
                return true;
            }
        }
        return false;
    }
    
    protected boolean isSubtype(TypeMirror type1, TypeMirror type2) {
        Types types = workingCopy.getTypes();
        TypeMirror tm1 = type1;
        TypeMirror tm2 = type2;

        return types.isSubtype(tm1, tm2) && !types.isSameType(tm1, tm2);
    }

}
