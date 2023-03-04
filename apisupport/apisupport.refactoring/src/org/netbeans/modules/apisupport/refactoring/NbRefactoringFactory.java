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
