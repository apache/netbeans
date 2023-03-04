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