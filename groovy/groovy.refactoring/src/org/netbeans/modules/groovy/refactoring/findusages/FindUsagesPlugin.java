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

package org.netbeans.modules.groovy.refactoring.findusages;

import java.util.List;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.groovy.refactoring.GroovyRefactoringPlugin;
import org.netbeans.modules.groovy.refactoring.findusages.impl.AbstractFindUsages;
import org.netbeans.modules.groovy.refactoring.findusages.impl.FindAllSubtypes;
import org.netbeans.modules.groovy.refactoring.findusages.impl.FindAllUsages;
import org.netbeans.modules.groovy.refactoring.findusages.impl.FindDirectSubtypesOnly;
import org.netbeans.modules.groovy.refactoring.findusages.impl.FindMethodUsages;
import org.netbeans.modules.groovy.refactoring.findusages.impl.FindOverridingMethods;
import org.netbeans.modules.groovy.refactoring.findusages.impl.FindTypeUsages;
import org.netbeans.modules.groovy.refactoring.findusages.impl.FindVariableUsages;
import org.netbeans.modules.groovy.refactoring.findusages.model.RefactoringElement;
import org.netbeans.modules.groovy.refactoring.utils.GroovyProjectUtil;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.java.api.WhereUsedQueryConstants;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;

/**
 * Groovy plugin for Find usages. It check whether the operation could be proceed,
 * collects all relevant occurrences and so on.
 *
 * @author Martin Janicek
 */
public class FindUsagesPlugin extends GroovyRefactoringPlugin {

    public FindUsagesPlugin(FileObject fileObject, RefactoringElement element, AbstractRefactoring whereUsedQuery) {
        super(element, fileObject, whereUsedQuery);
    }


    @Override
    public Problem prepare(final RefactoringElementsBag elementsBag) {
        if (element == null) {
            throw new IllegalStateException("There is no element in refactoring plugin !");
        }

        refactorResults(elementsBag, collectUsages());
        fireProgressListenerStop();
        return null;
    }


    protected void refactorResults(final RefactoringElementsBag elementsBag, final List<FindUsagesElement> usages) {
        for (FindUsagesElement usage : usages) {
            elementsBag.add(refactoring, usage);
        }
    }

    private List<FindUsagesElement> collectUsages() {
        final AbstractFindUsages strategy;
        if (isMethodUsage()) {
            strategy = getMethodStrategy();
        } else if (isClassTypeUsage()) {
            strategy = getClassStrategy();
        } else if (isVariableUsage()) {
            strategy = getVariableStrategy();
        } else {
            throw new IllegalStateException("Not implemented yet for kind: " + element.getKind());
        }

        List<FileObject> relevantFiles = getRelevantFiles();
        fireProgressListenerStart(ProgressEvent.START, relevantFiles.size());
        for (FileObject relevantFile : relevantFiles) {
            strategy.findUsages(relevantFile);
            fireProgressListenerStep();
        }
        return strategy.getResults();
    }

    private boolean isClassTypeUsage() {
        ASTNode node = element.getNode();
        switch (element.getKind()) {
            case CLASS:
            case INTERFACE:
                return true;
            case PROPERTY:
            case FIELD:
                if (node instanceof ClassNode) {
                    return true;
                }
        }
        return false;
    }

    private boolean isMethodUsage() {
        switch (element.getKind()) {
            case METHOD:
            case CONSTRUCTOR:
                return true;
        }
        return false;
    }

    private boolean isVariableUsage() {
        ASTNode node = element.getNode();
        switch (element.getKind()) {
            case VARIABLE:
                return true;
            case PROPERTY:
            case FIELD:
                if (!(node instanceof ClassNode)) {
                    return true;
                }
        }
        return false;
    }

    private AbstractFindUsages getMethodStrategy() {
        if (isFindOverridingMethods()) {
            return new FindOverridingMethods(element);
        } else if (isFindUsages()) {
            return new FindMethodUsages(element);
        }

        // This also happen in all other refactorings (rename, move etc.)
        if (element.getKind() == ElementKind.CONSTRUCTOR) {
            return new FindAllUsages(element);
        } else {
            return new FindMethodUsages(element);
        }
    }

    private AbstractFindUsages getClassStrategy() {
        if (isFindAllSubtypes()) {
            return new FindAllSubtypes(element);
        } else  if (isFindDirectSubtypes()) {
            return new FindDirectSubtypesOnly(element);
        } else if (isFindUsages()) {
            return new FindTypeUsages(element);
        }

        // This also happen in all other refactorings (rename, move etc.)
        return new FindAllUsages(element);
    }

    private AbstractFindUsages getVariableStrategy() {
        return new FindVariableUsages(element);
    }

    private List<FileObject> getRelevantFiles() {
        // FIXME: Filter this with respect to selected scope
        return GroovyProjectUtil.getGroovyFilesInProject(fileObject);
    }

    private boolean isFindOverridingMethods() {
        return isSet(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS);
    }

    private boolean isFindAllSubtypes() {
        return isSet(WhereUsedQueryConstants.FIND_SUBCLASSES);
    }

    private boolean isFindDirectSubtypes() {
        return isSet(WhereUsedQueryConstants.FIND_DIRECT_SUBCLASSES);
    }

    private boolean isFindUsages() {
        if (refactoring instanceof WhereUsedQuery) {
            return ((WhereUsedQuery) refactoring).getBooleanValue(WhereUsedQuery.FIND_REFERENCES);
        }
        return false;
    }

    private boolean isSet(final WhereUsedQueryConstants constant) {
        if (refactoring instanceof WhereUsedQuery) {
            return ((WhereUsedQuery) refactoring).getBooleanValue(constant);
        }
        return false;
    }
}
