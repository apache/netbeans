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

package org.netbeans.modules.groovy.refactoring.rename;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.spi.support.ModificationResult;
import org.netbeans.modules.csl.spi.support.ModificationResult.Difference;
import org.netbeans.modules.groovy.refactoring.DiffElement;
import org.netbeans.modules.groovy.refactoring.findusages.FindUsagesElement;
import org.netbeans.modules.groovy.refactoring.findusages.FindUsagesPlugin;
import org.netbeans.modules.groovy.refactoring.findusages.model.RefactoringElement;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringCommit;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;

/**
 * Implementation of the rename refactoring for groovy.
 *
 * @author Martin Janicek
 */
public class RenameRefactoringPlugin extends FindUsagesPlugin {


    public RenameRefactoringPlugin(FileObject fileObject, RefactoringElement element, AbstractRefactoring refactoring) {
        super(fileObject, element, refactoring);
    }


    public RenameRefactoring getRefactoring() {
        refactoring.getContext().add(UI.Constants.REQUEST_PREVIEW);
        return (RenameRefactoring) refactoring;
    }

    @Override
    protected void refactorResults(RefactoringElementsBag elementsBag, List<FindUsagesElement> usages) {
        final ModificationResult modificationResult = collectModifications(usages);

        elementsBag.registerTransaction(new RefactoringCommit(Collections.singletonList(modificationResult)));
        for (FileObject fo : modificationResult.getModifiedFileObjects()) {
            for (Difference diff : modificationResult.getDifferences(fo)) {
                elementsBag.add(refactoring, DiffElement.create(diff, fo, modificationResult));
                fo.refresh();
            }
        }
    }

    private ModificationResult collectModifications(List<FindUsagesElement> usages) {
        final ModificationResult modificationResult = new ModificationResult();
        for (FindUsagesElement whereUsedElement : usages) {
            addModificationToResult(modificationResult, whereUsedElement);
        }
        return modificationResult;
    }

    private void addModificationToResult(ModificationResult modificationResult, FindUsagesElement whereUsedElement) {
        if (isAlreadyInResult(modificationResult, whereUsedElement)) {
            return;
        }

        List<Difference> diffs = new ArrayList<Difference>();
        diffs.add(new Difference(
                Difference.Kind.CHANGE,
                whereUsedElement.getPosition().getBegin(),
                whereUsedElement.getPosition().getEnd(),
                whereUsedElement.getName(),
                getRefactoring().getNewName(),
                whereUsedElement.getDisplayText()));
        if (!diffs.isEmpty()) {
            modificationResult.addDifferences(whereUsedElement.getParentFile(), diffs);
        }
    }

    private boolean isAlreadyInResult(ModificationResult modificationResult, FindUsagesElement whereUsedElement) {
        assert modificationResult != null;
        assert whereUsedElement != null;

        final FileObject file = whereUsedElement.getParentFile();
        final PositionBounds position = whereUsedElement.getPosition();
        if (position == null) {
            return false;
        }

        final int start = position.getBegin().getOffset();
        final int end = position.getEnd().getOffset();
        
        List<? extends Difference> differences = modificationResult.getDifferences(file);
        if (differences != null && !differences.isEmpty()) {
            for (Difference diff : differences) {
                int startOffset = diff.getStartPosition().getOffset();
                int endOffset = diff.getEndPosition().getOffset();

                if (startOffset == start && endOffset == end) {
                    return true;
                }
            }
        }
        return false;
    }
}
