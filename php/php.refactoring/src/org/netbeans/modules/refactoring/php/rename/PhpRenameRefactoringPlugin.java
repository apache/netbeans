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
package org.netbeans.modules.refactoring.php.rename;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.spi.support.ModificationResult;
import org.netbeans.modules.csl.spi.support.ModificationResult.Difference;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.php.findusages.PhpWhereUsedQueryPlugin;
import org.netbeans.modules.refactoring.php.findusages.WarningFileElement;
import org.netbeans.modules.refactoring.php.findusages.WhereUsedElement;
import org.netbeans.modules.refactoring.php.findusages.WhereUsedSupport;
import org.netbeans.modules.refactoring.php.findusages.WhereUsedSupport.Results;
import org.netbeans.modules.refactoring.php.rename.PhpRenameRefactoringUI.RenameDeclarationFile;
import org.netbeans.modules.refactoring.spi.RefactoringCommit;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Radek Matous
 */
public class PhpRenameRefactoringPlugin extends PhpWhereUsedQueryPlugin {

    public PhpRenameRefactoringPlugin(RenameRefactoring refactoring) {
        super(refactoring);
    }

    @NbBundle.Messages({
        "MSG_Error_ElementEmpty=The element name cannot be empty.",
        "MSG_Error_SameName=The element has the same name as before.",
        "# {0} - New file name",
        "MSG_Error_FileExists=The file with name \"{0}\" already exists."
    })
    @Override
    public Problem checkParameters() {
        String newName = getRefactoring().getNewName();
        if (newName != null) {
            String trimmedNewName = newName.trim();
            if (trimmedNewName.length() == 0) {
                return new Problem(true, Bundle.MSG_Error_ElementEmpty());
            }
            final WhereUsedSupport usages = getUsages();
            String oldName = PhpRenameRefactoringUI.getElementName(usages.getName(), usages.getElementKind());
            if (trimmedNewName.equals(oldName.trim())) {
                return new Problem(true, Bundle.MSG_Error_SameName());
            }
            RenameDeclarationFile renameDeclarationFile = getRefactoring().getContext().lookup(PhpRenameRefactoringUI.RenameDeclarationFile.class);
            FileObject declarationFileObject = usages.getDeclarationFileObject();
            if (renameDeclarationFile.renameDeclarationFile()) {
                File parentFolder = FileUtil.toFile(declarationFileObject.getParent());
                File possibleNewFile = new File(parentFolder, trimmedNewName + "." + declarationFileObject.getExt()); //NOI18N
                if (possibleNewFile.isFile()) {
                    return new Problem(true, Bundle.MSG_Error_FileExists(trimmedNewName));
                }
            }
        }
        return null;
    }

    public RenameRefactoring getRefactoring() {
        return (RenameRefactoring) refactoring;
    }

    @Override
    public Problem fastCheckParameters() {
        return checkParameters();
    }

    @Override
    protected void refactorResults(Results results, RefactoringElementsBag refactoringElements, FileObject declarationFileObject) {
        final ModificationResult modificationResult = new ModificationResult();
        final Collection<WhereUsedElement> resultElements = results.getResultElements();
        for (WhereUsedElement whereUsedElement : resultElements) {
            refactorElement(modificationResult, whereUsedElement);
        }
        RenameDeclarationFile renameDeclarationFile = refactoring.getContext().lookup(PhpRenameRefactoringUI.RenameDeclarationFile.class);
        refactoringElements.registerTransaction(new RefactoringCommit(Collections.singletonList(modificationResult)));
        for (FileObject fo : modificationResult.getModifiedFileObjects()) {
            for (Difference diff : modificationResult.getDifferences(fo)) {
                FileRenamer fileRenamer = FileRenamer.NONE;
                if (fo.equals(declarationFileObject) && renameDeclarationFile != null) {
                    fileRenamer = new DelcarationFileRenamer(fo, renameDeclarationFile);
                }
                refactoringElements.add(refactoring, RenameDiffElement.create(diff, fo, modificationResult, fileRenamer));
            }
        }

        Collection<WarningFileElement> warningElements = results.getWarningElements();
        for (WarningFileElement warningElement : warningElements) {
            refactoringElements.add(refactoring, warningElement);
        }
    }

    private void refactorElement(ModificationResult modificationResult, WhereUsedElement whereUsedElement) {
        List<Difference> diffs = new ArrayList<>();
        diffs.add(new Difference(Difference.Kind.CHANGE,
                whereUsedElement.getPosition().getBegin(),
                whereUsedElement.getPosition().getEnd(),
                whereUsedElement.getName(),
                getRefactoring().getNewName(),
                whereUsedElement.getDisplayText()));
        if (!diffs.isEmpty()) {
            modificationResult.addDifferences(whereUsedElement.getFile(), diffs);
        }

    }

    public static final class DelcarationFileRenamer implements FileRenamer {

        private final FileObject declarationFileObject;
        private final RenameDeclarationFile renameDeclarationFile;

        private DelcarationFileRenamer(FileObject declarationFileObject, RenameDeclarationFile renameDeclarationFile) {
            assert declarationFileObject != null;
            assert renameDeclarationFile != null;
            this.declarationFileObject = declarationFileObject;
            this.renameDeclarationFile = renameDeclarationFile;
        }

        @Override
        public void rename(String newName) {
            assert newName != null;
            if (!newName.equals(declarationFileObject.getName()) && renameDeclarationFile.renameDeclarationFile()) {
                try {
                    FileLock lock = declarationFileObject.lock();
                    declarationFileObject.rename(lock, renameDeclarationFile.adjustName(newName), declarationFileObject.getExt());
                    lock.releaseLock();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
