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
package org.netbeans.modules.refactoring.api;

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.refactoring.api.impl.APIAccessor;
import org.netbeans.modules.refactoring.api.impl.SPIAccessor;
import org.netbeans.modules.refactoring.spi.GuardedBlockHandler;
import org.netbeans.modules.refactoring.spi.ProblemDetailsImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.netbeans.modules.refactoring.spi.ui.FiltersDescription;

/**
 *
 * @author Martin Matula, Jan Becicka
 */
final class AccessorImpl extends APIAccessor {
    @Override
    public Collection<GuardedBlockHandler> getGBHandlers(AbstractRefactoring refactoring) {
        assert refactoring != null;
        return refactoring.getGBHandlers();
    }
    
    @Override
    public boolean hasPluginsWithProgress(AbstractRefactoring refactoring) {
        return refactoring.pluginsWithProgress!=null && !refactoring.pluginsWithProgress.isEmpty();
    }

    @Override
    public Problem chainProblems(Problem p, Problem p1) {
        return AbstractRefactoring.chainProblems(p, p1);
    }
    
    @Override
    public ProblemDetails createProblemDetails(ProblemDetailsImplementation pdi) {
        assert pdi != null;
        return new ProblemDetails(pdi);
    }

    @Override
    public boolean isCommit(RefactoringSession session) {
        return session.realcommit;
    }

    @Override
    public RefactoringElementImplementation getRefactoringElementImplementation(RefactoringElement el) {
        return el.impl;
    }

    @Override
    public boolean hasChangesInGuardedBlocks(RefactoringSession session) {
        return SPIAccessor.DEFAULT.hasChangesInGuardedBlocks(session.getElementsBag());
    }

    @Override
    public boolean hasChangesInReadOnlyFiles(RefactoringSession session) {
        return SPIAccessor.DEFAULT.hasChangesInReadOnlyFiles(session.getElementsBag());
    }
    
    @Override
    public FiltersDescription getFiltersDescription(AbstractRefactoring refactoring) {
        return refactoring.getFiltersDescription();
    }

    @Override
    public void resetFiltersDescription(AbstractRefactoring refactoring) {
        refactoring.resetFiltersDescription();
    }

    @Override
    public boolean isFinished(RefactoringSession session) {
        return session.isFinished();
    }

    @Override
    public List<Transaction> getCommits(RefactoringSession session) {
        return session.getCommits();
    }

    @Override
    public List<RefactoringElementImplementation> getFileChanges(RefactoringSession session) {
        return session.getFileChanges();
    }
    
}
