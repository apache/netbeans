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

package org.netbeans.modules.spring.beans.refactoring.plugins;

import com.sun.source.tree.Tree.Kind;
import java.io.IOException;
import java.util.Collections;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringCommit;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.spring.api.beans.SpringScope;
import org.netbeans.modules.spring.beans.refactoring.*;
import org.netbeans.modules.spring.beans.refactoring.Occurrences.Occurrence;
import org.netbeans.modules.spring.beans.refactoring.SpringRefactorings.RenamedClassName;
import org.netbeans.modules.spring.beans.refactoring.SpringRefactorings.RenamedProperty;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Andrei Badea
 */
public class SpringRenamePlugin implements RefactoringPlugin {

    private final RenameRefactoring refactoring;

    public SpringRenamePlugin(RenameRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    public Problem preCheck() {
        return null;
    }

    public Problem checkParameters() {
        return null;
    }

    public Problem fastCheckParameters() {
        return null;
    }

    public void cancelRequest() {
    }

    public Problem prepare(RefactoringElementsBag refactoringElements) {
        TreePathHandle treePathHandle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);

        if (treePathHandle != null && treePathHandle.getKind() == Kind.METHOD) {
            return prepareMethodRefactoring(refactoringElements, treePathHandle);
        }

        return prepareClassRefactoring(refactoringElements, treePathHandle);
    }

    private Problem prepareClassRefactoring(RefactoringElementsBag refactoringElements, TreePathHandle treePathHandle) {
        FileObject fo = null;
        if (treePathHandle != null && TreeUtilities.CLASS_TREE_KINDS.contains(treePathHandle.getKind())) {
            fo = treePathHandle.getFileObject();
        }
        if (fo == null) {
            fo = refactoring.getRefactoringSource().lookup(FileObject.class);
        }
        boolean recursive = true;
        if (fo == null) {
            NonRecursiveFolder folder = refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class);
            if (folder != null) {
                recursive = false;
                fo = folder.getFolder();
            }
        }
        if (fo == null) {
            return null;
        }
        SpringScope scope = SpringScope.getSpringScope(fo);
        if (scope == null) {
            return null;
        }

        try {
            if (treePathHandle != null) {
                RenamedClassName clazz = null;
                JavaSource js = JavaSource.forFileObject(fo);
                if (js != null) {
                    clazz = SpringRefactorings.getRenamedClassName(treePathHandle, js, refactoring.getNewName());
                }
                if (clazz != null) {
                    String oldBinaryName = clazz.getOldBinaryName();
                    String newBinaryName = clazz.getNewBinaryName();
                    if (oldBinaryName != null && newBinaryName != null) {
                        Modifications mods = new Modifications();
                        for (Occurrence occurrence : Occurrences.getJavaClassOccurrences(oldBinaryName, scope)) {
                            refactoringElements.add(refactoring, SpringRefactoringElement.createJavaElementRefModification(occurrence, mods, clazz.getOldSimpleName(), newBinaryName));
                        }
                        refactoringElements.registerTransaction(new RefactoringCommit(Collections.singleton(mods)));
                    }
                }
            } else if (fo.isFolder()) {
                String oldPackageName = SpringRefactorings.getPackageName(fo);
                // If the rename is not recursive (e.g, "a.b.c" -> "x.b.c"), the new name is the whole package name.
                String newPackageName = recursive ? SpringRefactorings.getRenamedPackageName(fo, refactoring.getNewName()) : refactoring.getNewName();
                if (oldPackageName != null && newPackageName != null) {
                    Modifications mods = new Modifications();
                    for (Occurrence occurrence : Occurrences.getJavaPackageOccurrences(oldPackageName, recursive, scope)) {
                        refactoringElements.add(refactoring, SpringRefactoringElement.createJavaElementRefModification(occurrence, mods, null, newPackageName));
                    }
                    refactoringElements.registerTransaction(new RefactoringCommit(Collections.singleton(mods)));
                }
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }

    private Problem prepareMethodRefactoring(RefactoringElementsBag refactoringElements, final TreePathHandle treePathHandle) {
        FileObject fo = treePathHandle.getFileObject();

        try {
            RenamedProperty prop = null;
            JavaSource js = JavaSource.forFileObject(fo);
            if (js != null) {
                prop = SpringRefactorings.getRenamedProperty(treePathHandle, js, refactoring.getNewName());
            }

            SpringScope scope = SpringScope.getSpringScope(fo);
            if (scope == null) {
                return null;
            }

            if (prop != null) {
                String newName = prop.getNewName();
                String oldName = prop.getOldName();
                if (newName != null && oldName != null) {
                    Modifications mods = new Modifications();
                    for (Occurrence occurrence : Occurrences.getPropertyOccurrences(prop, js, scope)) {
                        refactoringElements.add(refactoring,
                                SpringRefactoringElement.createPropertyRefModification(occurrence, mods, prop.getOldName(), prop.getNewName()));
                    }
                    refactoringElements.registerTransaction(new RefactoringCommit(Collections.singleton(mods)));
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }
}
