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
package org.netbeans.modules.refactoring.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.refactoring.api.impl.SPIAccessor;
import org.netbeans.modules.refactoring.api.impl.APIAccessor;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;

/**
 *
 * @author Martin Matula, Jan Becicka
 */
final class AccessorImpl extends SPIAccessor {
    @Override
    public RefactoringElementsBag createBag(RefactoringSession session, List delegate) {
        assert session != null && delegate != null;
        return new RefactoringElementsBag(session, delegate);
    }
    
    @Override
    public Collection getReadOnlyFiles(RefactoringElementsBag bag) {
        return bag.getReadOnlyFiles();
    }
    
    @Override
    public ArrayList<Transaction> getCommits(RefactoringElementsBag bag) {
        return bag.commits;
    }

    @Override
    public ArrayList<RefactoringElementImplementation> getFileChanges(RefactoringElementsBag bag) {
        return bag.fileChanges;
    }
    
    @Override
    public String getNewFileContent(SimpleRefactoringElementImplementation impl) {
        return impl.getNewFileContent();
    }

    @Override
    public boolean hasChangesInGuardedBlocks(RefactoringElementsBag bag) {
        return bag.hasGuarded;
    }

    @Override
    public boolean hasChangesInReadOnlyFiles(RefactoringElementsBag bag) {
        return bag.hasReadOnly;
    }

    @Override
    public void check(Transaction commit, boolean undo) {
        if (commit instanceof RefactoringCommit) {
            ((RefactoringCommit) commit).check(undo);
        }
    }

    @Override
    public void sum(Transaction commit) {
        if (commit instanceof RefactoringCommit) {
            ((RefactoringCommit) commit).sum();
        }
    }

    @Override
    public Collection<? extends ModificationResult> getTransactions(RefactoringCommit c) {
        return c.results;
    }
    
}
