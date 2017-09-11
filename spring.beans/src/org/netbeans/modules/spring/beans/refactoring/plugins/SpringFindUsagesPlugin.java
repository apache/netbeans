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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
