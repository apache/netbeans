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
import com.sun.source.util.TreePath;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Becicka
 */
public class CopyTransformer extends RefactoringVisitor {
    
    private String newName;
    private boolean insertImport;
    private String oldPackage;
    private String oldName;
    private String newPackage;

    public CopyTransformer(WorkingCopy workingCopy, String oldName, String newName, boolean insertImport, String oldPackage) {
        try {
            setWorkingCopy(workingCopy);
            this.newName = newName;
            this.insertImport = insertImport;
            this.oldPackage = oldPackage;
            this.oldName = oldName;
            this.newPackage = RefactoringUtils.getPackageName(workingCopy.getFileObject().getParent());
        } catch (ToPhaseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Override
    public Tree visitCompilationUnit(CompilationUnitTree tree, Element p) {
        if (!workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            CompilationUnitTree cut = tree;
            if (cut.getPackageName() != null && !"".equals(newPackage) && !newPackage.equals(cut.getPackageName().toString())) { // NOI18N
                rewrite(cut.getPackageName(), make.Identifier(newPackage));
            } else {
                // in order to handle default package, we have to rewrite whole
                // compilation unit:
                cut = make.CompilationUnit(
                        cut.getPackageAnnotations(),
                        "".equals(newPackage) ? null : make.Identifier(newPackage), // NOI18N
                        cut.getImports(),
                        cut.getTypeDecls(),
                        cut.getSourceFile()
                );
                rewrite(tree, cut);
            }
            if (insertImport && !"package-info".equals(newName)) { //NOI18N
                Tree tree2 = make.insertCompUnitImport(cut, 0, make.Import(make.Identifier(oldPackage + ".*"), false)); // NOI18N
                rewrite(tree, tree2);
            }
        }
        return super.visitCompilationUnit(tree, p);
    }         

    @Override
    public Tree visitClass(ClassTree tree, Element p) {
        if (!workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            TypeElement currentClass = (TypeElement) workingCopy.getTrees().getElement(getCurrentPath());
            if (currentClass == null) {
                Logger.getLogger("org.netbeans.modules.refactoring.java").severe("Cannot resolve tree " + tree + "\n file: " + workingCopy.getFileObject().getPath()); // NOI18N
            } else {
                if (!currentClass.getNestingKind().isNested() && 
                        ( tree.getSimpleName().toString().equals(oldName) ||
                          tree.getSimpleName().toString().equals(oldName + "_1")
                        )
                    ) {
                    Tree nju = make.setLabel(tree, newName);
                    rewrite(tree, nju);
                }
            }
        }
        return super.visitClass(tree, p);
    }
    
    @Override
    public Tree visitIdentifier(IdentifierTree node, Element p) {
        renameUsageIfMatch(getCurrentPath(), node,p);
        return super.visitIdentifier(node, p);
    }

    @Override
    public Tree visitMemberSelect(MemberSelectTree node, Element p) {
        renameUsageIfMatch(getCurrentPath(), node,p);
        return super.visitMemberSelect(node, p);
    }
    
    private void renameUsageIfMatch(TreePath path, Tree tree, Element elementToFind) {
        if (JavaPluginUtils.isSyntheticPath(workingCopy, path)) {
            return;
        }
        Element el = workingCopy.getTrees().getElement(path);
        if (el==null) {
            return;
        }
        
        if ((el.getKind().isClass() || el.getKind().isInterface()) && (((TypeElement) el).getQualifiedName().toString().equals(newPackage+"."+oldName)
                                                                   || ((TypeElement) el).getQualifiedName().toString().equals(oldPackage+"."+oldName))){ // NOI18N
            Tree nju = make.setLabel(tree, newName);
            rewrite(tree, nju);
        }
    }
    
}
