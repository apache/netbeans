/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
