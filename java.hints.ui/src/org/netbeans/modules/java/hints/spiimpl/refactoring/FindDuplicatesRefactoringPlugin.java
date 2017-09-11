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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.spiimpl.refactoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata.Options;
import org.netbeans.modules.java.hints.spiimpl.MessageImpl;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.BatchResult;
import org.netbeans.modules.java.hints.spiimpl.batch.ProgressHandleWrapper;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.spi.java.hints.Hint.Kind;
import org.netbeans.spi.java.hints.HintContext.MessageKind;
import org.openide.util.NbBundle.Messages;

public class FindDuplicatesRefactoringPlugin extends AbstractApplyHintsRefactoringPlugin {

    private final FindDuplicatesRefactoring refactoring;

    public FindDuplicatesRefactoringPlugin(FindDuplicatesRefactoring refactoring) {
        super(refactoring);
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

     public Problem prepare(RefactoringElementsBag refactoringElements) {
        cancel.set(false);

        //Collection<MessageImpl> problems = refactoring.isQuery() ? performSearchForPattern(refactoringElements) : performApplyPattern(refactoringElements);
        
        Collection<MessageImpl> problems = performSearchForPattern(refactoringElements);
        problems.addAll(performApplyPattern(refactoringElements));
        
        Problem current = null;

        for (MessageImpl problem : problems) {
            Problem p = new Problem(problem.kind == MessageKind.ERROR, problem.text);

            if (current != null)
                p.setNext(current);
            current = p;
        }

        return current;
    }

    @Messages("WARN_HasQueries=The selected configuration contains inspections that do not provide any transformations. " +
              "No diff will be provided for code detected by such inspections. Use Source/Inspect... to perform code analysis.")
    private List<MessageImpl> performSearchForPattern(final RefactoringElementsBag refactoringElements) {
        ProgressHandleWrapper w = new ProgressHandleWrapper(this, 10, 90);
        Iterable<? extends HintDescription> queries = filterQueries(refactoring.getPattern(), true);
        BatchResult candidates = BatchSearch.findOccurrences(queries, refactoring.getScope(), w, /*XXX:*/HintsSettings.getGlobalSettings());
        List<MessageImpl> problems = new LinkedList<MessageImpl>(candidates.problems);

        if (queries.iterator().hasNext()) {
            problems.add(new MessageImpl(MessageKind.WARNING, Bundle.WARN_HasQueries()));
        }
        
        prepareElements(candidates, w, refactoringElements, refactoring.isVerify(), problems);

        w.finish();

        return problems;
     }

    private Collection<MessageImpl> performApplyPattern(RefactoringElementsBag refactoringElements) {
        return performApplyPattern(filterQueries(refactoring.getPattern(), false), refactoring.getScope(), refactoringElements);
    }

    private Iterable<? extends HintDescription> filterQueries(Iterable<? extends HintDescription> hints, boolean positive) {
        ArrayList<HintDescription> result = new ArrayList<HintDescription>();
        for (HintDescription hint: hints) {
            if (hint.getMetadata().options.contains(Options.NO_BATCH)) continue;
            if (hint.getMetadata().kind != Kind.INSPECTION) continue;
            if (positive ^ !(hint.getMetadata().options.contains(Options.QUERY) || hint.getOptions().contains(Options.QUERY))) {
                result.add(hint);
            }
        }
        return result;
    }
}