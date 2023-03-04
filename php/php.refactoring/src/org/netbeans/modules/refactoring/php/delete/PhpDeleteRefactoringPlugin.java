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
package org.netbeans.modules.refactoring.php.delete;

import java.util.Collections;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProblemDetails;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.php.findusages.WhereUsedQueryUI;
import org.netbeans.modules.refactoring.php.findusages.WhereUsedSupport;
import org.netbeans.modules.refactoring.spi.ProblemDetailsFactory;
import org.netbeans.modules.refactoring.spi.ProblemDetailsImplementation;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Radek Matous
 */
public class PhpDeleteRefactoringPlugin extends ProgressProviderAdapter implements RefactoringPlugin {

    private final SafeDeleteRefactoring refactoring;
    private final SafeDeleteSupport safeDeleteSupport;
    private WhereUsedQuery[] whereUsedQueries;

    public PhpDeleteRefactoringPlugin(SafeDeleteRefactoring refactoring) {
        this.refactoring = refactoring;
        safeDeleteSupport = this.refactoring.getRefactoringSource().lookup(SafeDeleteSupport.class);
    }

    public SafeDeleteRefactoring getRefactoring() {
        return refactoring;
    }

    @Override
    public Problem preCheck() {
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    @Override
    public void cancelRequest() {
    }

    @Override
    public Problem checkParameters() {
        Set<ModelElement> visibleElements = safeDeleteSupport.getVisibleElements();
        whereUsedQueries = new WhereUsedQuery[visibleElements.size()];
        Index idx = safeDeleteSupport.getIdx();
        int position = 0;
        for (ModelElement modelElement : visibleElements) {
            WhereUsedSupport support = WhereUsedSupport.getInstance(Collections.singleton(modelElement), idx, modelElement.getFileObject(), modelElement.getOffset());
            whereUsedQueries[position++] = new WhereUsedQuery(Lookups.singleton(support));
        }

        return null;
    }

    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        RefactoringSession inner = RefactoringSession.create("delete"); // NOI18N
        FileObject file = safeDeleteSupport.getFile();
        fireProgressListenerStart(AbstractRefactoring.PARAMETERS_CHECK, whereUsedQueries.length + 1);
        for (int i = 0; i < whereUsedQueries.length; ++i) {
            final WhereUsedQuery whereUsedQuery = whereUsedQueries[i];
            whereUsedQuery.prepare(inner);
            fireProgressListenerStep();
            for (RefactoringElement refacElem : inner.getRefactoringElements()) {
                if (file != refacElem.getParentFile()) {
                    WhereUsedSupport support = whereUsedQuery.getRefactoringSource().lookup(WhereUsedSupport.class);
                    final ProblemDetailsImplemen problemDetailsImplemen = new ProblemDetailsImplemen(new WhereUsedQueryUI(support), inner);
                    final ProblemDetails problemDetails = ProblemDetailsFactory.createProblemDetails(problemDetailsImplemen);
                    Problem problem = new Problem(false, NbBundle.getMessage(PhpDeleteRefactoringPlugin.class, "ERR_ReferencesFound"), problemDetails);
                    fireProgressListenerStop();
                    return problem;
                }
            }
        }
        fireProgressListenerStep();
        return null;
    }


    private static class ProblemDetailsImplemen implements ProblemDetailsImplementation {

        private RefactoringUI ui;
        private RefactoringSession rs;

        public ProblemDetailsImplemen(RefactoringUI ui, RefactoringSession rs) {
            this.ui = ui;
            this.rs = rs;
        }

        @Override
        public void showDetails(Action callback, Cancellable parent) {
            parent.cancel();
            UI.openRefactoringUI(ui, rs, callback);
        }

        @Override
        public String getDetailsHint() {
            return NbBundle.getMessage(PhpDeleteRefactoringPlugin.class, "LBL_ShowUsages");//NOI18N
        }
    }
}
