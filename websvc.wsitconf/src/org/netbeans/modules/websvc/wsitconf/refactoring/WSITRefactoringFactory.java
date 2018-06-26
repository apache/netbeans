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
