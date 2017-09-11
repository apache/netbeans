/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
