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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.spi.ModificationResult;
import org.netbeans.modules.refactoring.spi.RefactoringCommit;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.Transaction;

/**
 *
 * @author Martin Matula, Jan Becicka
 */
public abstract class SPIAccessor {
    public static SPIAccessor DEFAULT;

    static {
        Class c = RefactoringElementsBag.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public abstract RefactoringElementsBag createBag(RefactoringSession session, List delegate);
    public abstract Collection getReadOnlyFiles(RefactoringElementsBag bag);
    public abstract ArrayList<Transaction> getCommits(RefactoringElementsBag bag);
    public abstract ArrayList<RefactoringElementImplementation> getFileChanges(RefactoringElementsBag bag);
    public abstract String getNewFileContent(SimpleRefactoringElementImplementation impl);
    public abstract boolean hasChangesInGuardedBlocks(RefactoringElementsBag bag);
    public abstract boolean hasChangesInReadOnlyFiles(RefactoringElementsBag bag);
    public abstract void check(Transaction commit, boolean undo);
    public abstract void sum(Transaction commit);
    public abstract Collection<? extends ModificationResult> getTransactions(RefactoringCommit c);
    
}
