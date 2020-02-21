/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.refactoring.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.refactoring.spi.CheckModificationHook;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;

/**
 *
 */
public abstract class CsmModificationRefactoringPlugin extends CsmRefactoringPlugin {
    // the context object where refactoring starts

    private final CsmObject startReferenceObject;
    private final CsmContext editorContext;
    private final AbstractRefactoring refactoring;
    private final Collection<CheckModificationHook> modificationHooks;

    protected CsmModificationRefactoringPlugin(AbstractRefactoring refactoring) {
        this.refactoring = refactoring;
        this.startReferenceObject = refactoring.getRefactoringSource().lookup(CsmObject.class);
        this.editorContext = refactoring.getRefactoringSource().lookup(CsmContext.class);
        assert startReferenceObject != null || editorContext != null: "no start reference or editor context";
        modificationHooks = new ArrayList<CheckModificationHook>(refactoring.getContext().lookupAll(CheckModificationHook.class));
        if (modificationHooks.isEmpty()) {
            modificationHooks.add(new DefaultHookImpl());
        }
    }

    protected final CsmObject getStartReferenceObject() {
        return startReferenceObject;
    }
    
    protected final CsmContext getEditorContext() {
        return editorContext;
    }

    @Override
    public final Problem prepare(RefactoringElementsBag elements) {
        Problem out = null;
        try {
            Collection<CsmFile> files = getRefactoredFiles();
            fireProgressListenerStart(ProgressEvent.START, files.size());
            out = createAndAddElements(files, elements, refactoring);
        } finally {
            fireProgressListenerStop();
        }
        return out;
    }

    protected abstract Collection<CsmFile> getRefactoredFiles();

    protected Problem checkIfModificationPossible(Problem problem, CsmObject referencedObject) {
        fireProgressListenerStep();
        try {
            for (CheckModificationHook hook : modificationHooks) {
                problem = hook.appendProblem(refactoring, problem, referencedObject);
                if (problem != null && problem.isFatal()) {
                    return problem;
                }
            }
        } finally {
            fireProgressListenerStep();
        }
        return problem;
    }

    private static final class DefaultHookImpl extends CheckModificationHook {

        @Override
        public Problem appendProblem(AbstractRefactoring refactoring, Problem problem, CsmObject referencedObject) {
            return super.defaultCheckIfModificationPossible(problem, referencedObject);
        }
    }

    @Override
    protected final ModificationResult processFiles(Collection<CsmFile> files, AtomicReference<Problem> outProblem) {
        ModificationResult out = null;
        for (CsmFile csmFile : files) {
            if (isCancelled()) {
                // may be return what we already have?
                return null;
            }
            if (out == null) {
                out = new ModificationResult(csmFile.getProject());
            }
            processFile(csmFile, out, outProblem);
            fireProgressListenerStep();
        }
        return out;
    }

    protected abstract void processFile(CsmFile csmFile, ModificationResult mr, AtomicReference<Problem> outProblem);
}
