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

package org.netbeans.modules.apisupport.refactoring;

import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
//import org.netbeans.modules.refactoring.api.MoveClassRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * netbeans related support for refactoring
 * @author Milos Kleint
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.RefactoringPluginFactory.class)
public class NbRefactoringFactory implements RefactoringPluginFactory {
    

    /**
     * Creates a new instance of NbRefactoringFactory
     */
    public NbRefactoringFactory() { }

    /** Creates and returns a new instance of the refactoring plugin or returns
     * null if the plugin is not suitable for the passed refactoring.
     * @param refactoring Refactoring, the plugin shimport org.openide.ErrorManager;
ould operate on.
     * @return Instance of RefactoringPlugin or null if the plugin is not applicable to
     * the passed refactoring.
     */
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        Lookup look = refactoring.getRefactoringSource();
        FileObject file = look.lookup(FileObject.class);
        NonRecursiveFolder folder = look.lookup(NonRecursiveFolder.class);
        TreePathHandle handle = look.lookup(TreePathHandle.class);
        FileObject prjFile = file;
        //#114235
        if (prjFile == null && folder != null) {
            prjFile = folder.getFolder();
        }
        if (prjFile == null && handle != null) {
            prjFile = handle.getFileObject();
        }
        if (prjFile != null) {
            //#107638
            Project project = FileOwnerQuery.getOwner(prjFile);
            if (project == null || project.getLookup().lookup(NbModuleProvider.class) == null) {
                // take just netbeans module development into account..
                return null;
            }
        }
        
        if (refactoring instanceof WhereUsedQuery) {
            if (handle != null) {
                return new NbWhereUsedRefactoringPlugin(refactoring);
            }
        }
        if (refactoring instanceof RenameRefactoring) {
            if (handle!=null || ((file!=null) && RetoucheUtils.isJavaFile(file))) {
                //rename java file, class, method etc..
                return new NbRenameRefactoringPlugin((RenameRefactoring)refactoring);
            } else if (file!=null && RetoucheUtils.isOnSourceClasspath(file) && file.isFolder()) {
                //rename folder
                return new NbMoveRefactoringPlugin((RenameRefactoring)refactoring);
            } else if (folder!=null && RetoucheUtils.isOnSourceClasspath(folder.getFolder())) {
                //rename package
                return new NbMoveRefactoringPlugin((RenameRefactoring)refactoring);
            } else if (folder!=null && !RetoucheUtils.isOnSourceClasspath(folder.getFolder())) {
                //rename resource
                return new NbMoveRefactoringPlugin((RenameRefactoring)refactoring);
            }
        }    
            
        if (refactoring instanceof MoveRefactoring) {
//TODO            return new NbMoveRefactoringPlugin((MoveRefactoring)refactoring);
        }
        if (refactoring instanceof SafeDeleteRefactoring) {
            if (handle != null) {
                return new NbSafeDeleteRefactoringPlugin(refactoring);
            }
        }
        return null;
    }
    
}
