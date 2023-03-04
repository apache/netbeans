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

package org.netbeans.modules.websvc.wsitconf.refactoring;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.netbeans.modules.websvc.wsitconf.refactoring.WSITRenameRefactoringPlugin;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Grebac
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.RefactoringPluginFactory.class)
public class WSITRefactoringFactory implements RefactoringPluginFactory {
    
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.websvc.wsitconf.refactoring");

    /**
     * Creates a new instance of WSITRefactoringFactory
     */
    public WSITRefactoringFactory() { }

    /** Creates and returns a new instance of the refactoring plugin or returns
     * null if the plugin is not suitable for the passed refactoring.
     * @param refactoring Refactoring, the plugin should operate on.
     * @return Instance of RefactoringPlugin or null if the plugin is not applicable to
     * the passed refactoring.
     */
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {

        LOGGER.log(Level.FINE, "Create instance called: " + refactoring);   // NOI18N

        
        NonRecursiveFolder pkg = refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class);
        boolean javaPackage = pkg != null && isOnSourceClasspath(pkg.getFolder());
        
        if ( javaPackage && (refactoring instanceof RenameRefactoring) ){
            LOGGER.log(Level.FINE, "Rename package refactoring");           // NOI18N
            return new WSITRenamePackagePlugin((RenameRefactoring) refactoring);
        }
        
        if ( refactoring instanceof MoveRefactoring ){
            return new WSITMoveRefactoringPlugin((MoveRefactoring)refactoring);
        }
        
        TreePathHandle tph = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        if (tph == null) {
            return null;
        }

        if (refactoring instanceof RenameRefactoring) {
            LOGGER.log(Level.FINE, "Rename refactoring");                   // NOI18N
            return new WSITRenameRefactoringPlugin((RenameRefactoring) refactoring);
        }
        
        if (refactoring instanceof SafeDeleteRefactoring) {
            LOGGER.log(Level.FINE, "Safe delete refactoring");              // NOI18N
            return new WSITSafeDeleteRefactoringPlugin((SafeDeleteRefactoring) refactoring);
        }

        return null;
    }
    
    private static boolean isOnSourceClasspath(FileObject fileObject) {
        Project project = FileOwnerQuery.getOwner(fileObject);
        if (project==null) return false;
        if (OpenProjects.getDefault().isProjectOpen(project)) {
            SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                    JavaProjectConstants.SOURCES_TYPE_JAVA);
            for (int j = 0; j < sourceGroups.length; j++) {
                if (fileObject==sourceGroups[j].getRootFolder()) {
                    return true;
                }
                if (FileUtil.isParentOf(sourceGroups[j].getRootFolder(), fileObject))
                    return true;
            }
            return false;
        }
        return false;
    }
    
}
