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

package org.netbeans.modules.groovy.refactoring;

import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.modules.groovy.refactoring.findusages.FindUsagesPlugin;
import org.netbeans.modules.groovy.refactoring.findusages.model.RefactoringElement;
import org.netbeans.modules.groovy.refactoring.move.MoveFileRefactoringPlugin;
import org.netbeans.modules.groovy.refactoring.rename.RenamePackagePlugin;
import org.netbeans.modules.groovy.refactoring.rename.RenameRefactoringPlugin;
import org.netbeans.modules.groovy.refactoring.utils.GroovyProjectUtil;
import org.netbeans.modules.groovy.refactoring.utils.IdentifiersUtil;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Groovy refactoring plugin factory implementation.
 * This is the place where is decided which plugin should be used in which cases.
 *
 * @author Martin Janicek
 */
@ServiceProvider(service = RefactoringPluginFactory.class)
public class GroovyRefactoringFactory implements RefactoringPluginFactory {

    @Override
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        final Lookup lookup = refactoring.getRefactoringSource();
        final NonRecursiveFolder pkg = lookup.lookup(NonRecursiveFolder.class);
        final RefactoringElement element = lookup.lookup(RefactoringElement.class);
        FileObject sourceFO = lookup.lookup(FileObject.class);

        if (element == null) {
            return null; // Might happened #221580
        }

        if (sourceFO == null) {
            if (pkg != null) {
                sourceFO = pkg.getFolder();
            } else {
                if (element != null) {
                    sourceFO = element.getFileObject();
                }
            }
        }

        if (sourceFO == null || !GroovyProjectUtil.isInGroovyProject(sourceFO)) {
            return null;
        }

        if (refactoring instanceof WhereUsedQuery) {
            return new FindUsagesPlugin(sourceFO, element, refactoring);
        }
        if (refactoring instanceof RenameRefactoring) {
            final RenameRefactoring renameRefactoring = (RenameRefactoring) refactoring;

            if (IdentifiersUtil.isPackageRename(renameRefactoring)) {
//                return new RenamePackagePlugin(sourceFO, renameRefactoring);
            } else {
                return new RenameRefactoringPlugin(sourceFO, element, renameRefactoring);
            }
        }
        if (refactoring instanceof MoveRefactoring) {
            return new MoveFileRefactoringPlugin(sourceFO, element, refactoring);
        }
        return null;
    }
}
