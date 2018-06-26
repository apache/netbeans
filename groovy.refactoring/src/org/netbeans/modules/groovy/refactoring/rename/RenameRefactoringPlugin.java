/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
