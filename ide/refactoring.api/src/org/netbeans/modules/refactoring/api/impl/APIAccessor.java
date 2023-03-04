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
package org.netbeans.modules.refactoring.api.impl;

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProblemDetails;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.spi.GuardedBlockHandler;
import org.netbeans.modules.refactoring.spi.ProblemDetailsImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringCommit;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.netbeans.modules.refactoring.spi.ui.FiltersDescription;

/**
 *
 * @author Martin Matula, Jan Becicka
 */
public abstract class APIAccessor {
    public static APIAccessor DEFAULT;

    static {
        Class c = AbstractRefactoring.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public abstract Collection<GuardedBlockHandler> getGBHandlers(AbstractRefactoring refactoring);
    public abstract Problem chainProblems(Problem p, Problem p1);
    public abstract ProblemDetails createProblemDetails(ProblemDetailsImplementation pdi);
    public abstract boolean isCommit(RefactoringSession session);
    public abstract RefactoringElementImplementation getRefactoringElementImplementation(RefactoringElement el) ;
    public abstract boolean hasPluginsWithProgress(AbstractRefactoring refactoring);
    public abstract boolean hasChangesInGuardedBlocks(RefactoringSession session);
    public abstract boolean hasChangesInReadOnlyFiles(RefactoringSession session);
    public abstract FiltersDescription getFiltersDescription(AbstractRefactoring refactoring);
    public abstract void resetFiltersDescription(AbstractRefactoring refactoring);
    public abstract boolean isFinished(RefactoringSession session);
    public abstract List<Transaction> getCommits(RefactoringSession session);
    public abstract List<RefactoringElementImplementation> getFileChanges(RefactoringSession session);

}
