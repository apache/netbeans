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
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.spring.api.beans.SpringScope;
import org.netbeans.modules.spring.beans.refactoring.Occurrences;
import org.netbeans.modules.spring.beans.refactoring.Occurrences.Occurrence;
import org.netbeans.modules.spring.beans.refactoring.SpringRefactoringElement;
import org.netbeans.modules.spring.beans.refactoring.SpringRefactorings;
import org.netbeans.modules.spring.beans.refactoring.SpringRefactorings.RenamedProperty;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * @author John Baker
 */
public class SpringFindUsagesPlugin implements RefactoringPlugin {

    private final WhereUsedQuery refactoring;

    SpringFindUsagesPlugin(WhereUsedQuery query) {
        refactoring = query;
    }

    public Problem fastCheckParameters() {
        return null;
    }

    public Problem checkParameters() {
        return null;
    }

    public void cancelRequest() {
    }

    public Problem preCheck() {
        return null;
    }

    public Problem prepare(RefactoringElementsBag refactoringElementsBag) {
        if (!refactoring.getBooleanValue(WhereUsedQuery.FIND_REFERENCES)) {
            return null;
        }
        final TreePathHandle treePathHandle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        if (treePathHandle != null && treePathHandle.getKind() == Kind.METHOD) {
            return prepareMethodRefactoring(refactoringElementsBag, treePathHandle);
        }
        
        if (treePathHandle != null && TreeUtilities.CLASS_TREE_KINDS.contains(treePathHandle.getKind())) {
            return prepareClassRefactoring(refactoringElementsBag, treePathHandle);
        }
        
        return null;
    }
    
    private Problem prepareClassRefactoring(RefactoringElementsBag refactoringElementsBag, final TreePathHandle treePathHandle) {
        FileObject fo = treePathHandle.getFileObject();
        SpringScope scope = SpringScope.getSpringScope(fo);
        if (scope == null) {
            return null;
        }
        try {
            JavaSource source = JavaSource.forFileObject(fo);
            // #253033
            if (source == null) {
                return null;
            }
            final String[] className = new String[] { null };
            source.runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController compilationController) throws Exception {
                    compilationController.toPhase(JavaSource.Phase.RESOLVED);
                    TypeElement type = (TypeElement) treePathHandle.resolveElement(compilationController);
                    if (type != null) {
                        className[0] = ElementUtilities.getBinaryName(type);
                    }
                }
            }, true);
            if (className[0] != null) {
                for (Occurrences.Occurrence item : Occurrences.getJavaClassOccurrences(className[0], scope)) {
                    refactoringElementsBag.add(refactoring, SpringRefactoringElement.create(item));
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
                prop = SpringRefactorings.getRenamedProperty(treePathHandle, js, null);
            }

            SpringScope scope = SpringScope.getSpringScope(fo);
            if (scope == null) {
                return null;
            }

            if (prop != null) {
                String oldName = prop.getOldName();
                if (oldName != null) {
                    for (Occurrence occurrence : Occurrences.getPropertyOccurrences(prop, js, scope)) {
                        refactoringElements.add(refactoring, SpringRefactoringElement.create(occurrence));
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }
}
