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
