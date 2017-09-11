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

import com.sun.source.tree.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 */
public class DeleteTransformer extends RefactoringVisitor {
    
    private final HashSet<ElementHandle<ExecutableElement>> allMethods;
    private final Collection<? extends FileObject> files;

    DeleteTransformer(HashSet<ElementHandle<ExecutableElement>> allMethods, Collection<? extends FileObject> files) {
        this.allMethods = allMethods;
        this.files = files;
    }

    @Override
    public Tree visitCompilationUnit(CompilationUnitTree node, Element p) {
        if(files.contains(workingCopy.getFileObject())) {
            return null;
        }
        return super.visitCompilationUnit(node, p);
    }

    @Override
    public Tree visitMethod(MethodTree tree, Element p) {
        deleteDeclIfMatch(tree, p);
        return super.visitMethod(tree, p);
    }

    @Override
    public Tree visitClass(ClassTree tree, Element p) {
        deleteDeclIfMatch(tree, p);
        return super.visitClass(tree, p);
    }

    @Override
    public Tree visitVariable(VariableTree tree, Element p) {
        deleteDeclIfMatch(tree, p);
        return super.visitVariable(tree, p);
    }
    
    private void deleteDeclIfMatch(Tree tree, Element elementToFind) {
        if (JavaPluginUtils.isSyntheticPath(workingCopy, getCurrentPath())) {
            return ;
        }
        
        Element el = workingCopy.getTrees().getElement(getCurrentPath());
        if (isMatch(el, elementToFind)) {
            Tree parent = getCurrentPath().getParentPath().getLeaf();
            Tree newOne = null;
            if (TreeUtilities.CLASS_TREE_KINDS.contains(parent.getKind())) {
                newOne = make.removeClassMember((ClassTree) parent, tree);
            } else if (parent.getKind() == Tree.Kind.COMPILATION_UNIT) {
                newOne = make.removeCompUnitTypeDecl((CompilationUnitTree) parent, tree);
            } else if (tree.getKind() == Tree.Kind.VARIABLE) {
                if (parent.getKind() == Tree.Kind.METHOD) {
                    newOne = make.removeMethodParameter((MethodTree)parent, (VariableTree) tree);
                } else {
                    newOne = make.removeBlockStatement((BlockTree)parent, (VariableTree) tree);
                }
            }
            if (newOne!=null) {
                rewrite(parent,newOne);
            }
        }
    }
    
    private boolean isMatch(Element element, Element elementToFind) {
        if(element == null) {
            return false;
        }
        if(allMethods == null || element.getKind() != ElementKind.METHOD) {
            return element.equals(elementToFind);
        } else {
            for (ElementHandle<ExecutableElement> mh: allMethods) {
                ExecutableElement baseMethod =  mh.resolve(workingCopy);
                if (baseMethod==null) {
                    Logger.getLogger("org.netbeans.modules.refactoring.java").log(Level.INFO, "DeleteTransformer cannot resolve {0}", mh);
                    continue;
                }
                if (baseMethod.equals(element) || workingCopy.getElements().overrides((ExecutableElement)element, baseMethod, workingCopy.getElementUtilities().enclosingTypeElement(baseMethod))) {
                    return true;
                }
            }
        }
        return false;
    }
}
