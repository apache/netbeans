/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
