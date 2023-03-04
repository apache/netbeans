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
package org.netbeans.modules.refactoring.php.findusages;

import java.util.Collection;
import java.util.Set;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.php.findusages.WhereUsedSupport.Results;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.filesystems.FileObject;

/**
 * @todo index all identifiers inside project not to crawl the whole project for usages
 * @todo Scan comments!
 *
 * @author  Radek Matous
 */
public class PhpWhereUsedQueryPlugin extends ProgressProviderAdapter implements RefactoringPlugin {

    protected AbstractRefactoring refactoring;
    private WhereUsedSupport usages;
    private volatile boolean cancelled;

    public PhpWhereUsedQueryPlugin(AbstractRefactoring refactoring) {
        this.refactoring = refactoring;
        this.usages = refactoring.getRefactoringSource().lookup(WhereUsedSupport.class);
    }

    @Override
    public Problem prepare(final RefactoringElementsBag elementsBag) {
        this.usages.clearResults();
        if (isFindOverridingMethods()) {
            usages.overridingMethods();
        }
        if (isFindSubclasses()) {
            usages.collectSubclasses();
        } else if (isFindDirectSubclassesOnly()) {
            usages.collectDirectSubclasses();
        } else if (isFindUsages()) {
            Set<FileObject> relevantFiles = usages.getRelevantFiles();
            fireProgressListenerStart(ProgressEvent.START, relevantFiles.size());
            for (FileObject fileObject : relevantFiles) {
                if (fileObject == null) {
                    continue;
                }
                if (cancelled) {
                    // Reset cancelled state for repeated search to work.
                    cancelled = false;
                    break;
                }
                usages.collectUsages(fileObject);
                fireProgressListenerStep();
            }
        }
        Results results = usages.getResults();
        refactorResults(results, elementsBag, usages.getDeclarationFileObject());
        fireProgressListenerStop();
        return null;
    }

    protected void refactorResults(Results results, final RefactoringElementsBag elementsBag, FileObject declarationFileObject) {
        Collection<WhereUsedElement> resultElements = results.getResultElements();
        for (WhereUsedElement whereUsedElement : resultElements) {
            elementsBag.add(refactoring, whereUsedElement);
        }

        Collection<WarningFileElement> warningElements = results.getWarningElements();
        for (WarningFileElement warningElement : warningElements) {
            elementsBag.add(refactoring, warningElement);
        }
    }

    private boolean isFindSubclasses() {
        return (refactoring instanceof WhereUsedQuery) ? ((WhereUsedQuery) refactoring).getBooleanValue(WhereUsedQueryConstants.FIND_SUBCLASSES)
                : false;
    }

    private boolean isFindUsages() {
        return (refactoring instanceof WhereUsedQuery) ? ((WhereUsedQuery) refactoring).getBooleanValue(WhereUsedQuery.FIND_REFERENCES)
                : true;
    }

    private boolean isFindDirectSubclassesOnly() {
        return (refactoring instanceof WhereUsedQuery) ? ((WhereUsedQuery) refactoring).getBooleanValue(WhereUsedQueryConstants.FIND_DIRECT_SUBCLASSES)
                : false;
    }

    private boolean isFindOverridingMethods() {
        return (refactoring instanceof WhereUsedQuery) ? ((WhereUsedQuery) refactoring).getBooleanValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS)
                : false;
    }

    protected boolean isSearchInComments() {
        return (refactoring instanceof WhereUsedQuery) ? ((WhereUsedQuery) refactoring).getBooleanValue(WhereUsedQuery.SEARCH_IN_COMMENTS)
                : false;
    }

    @Override
    public Problem preCheck() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    @Override
    public void cancelRequest() {
        cancelled = true;
    }

    /**
     * @return the usages
     */
    public WhereUsedSupport getUsages() {
        return usages;
    }
}
