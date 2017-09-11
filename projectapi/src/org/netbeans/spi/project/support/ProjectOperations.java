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

package org.netbeans.spi.project.support;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DataFilesProviderImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.netbeans.spi.project.MoveOrRenameOperationImplementation;
import org.openide.filesystems.FileObject;

/**
 * Allows gathering information for various project operations.
 *
 * @author Jan Lahoda
 * @since 1.7
 */
public final class ProjectOperations {
    
    private ProjectOperations() {
    }
    
    /**Return list of files that are considered metadata files and folders for the given project.
     * Returns meaningful values only if some of the <code>is*Supported</code> methods
     * return <code>true</code>.
     *
     * @param prj project to test
     * @return list of metadata files/folders
     * @see DataFilesProviderImplementation#getMetadataFiles
     */
    public static List<FileObject> getMetadataFiles(Project prj) {
        List<FileObject> result = new ArrayList<FileObject>();
        
        for (DataFilesProviderImplementation i : prj.getLookup().lookupAll(DataFilesProviderImplementation.class)) {
            result.addAll(i.getMetadataFiles());
            assert !result.contains(null) : "Nulls in " + result + " from " + i;
        }
        
        return result;
    }
            
    /**Return list of files that are considered source files and folders for the given project.
     * Returns meaningful values only if some of the <code>is*Supported</code> methods
     * return <code>true</code>.
     *
     * @param prj project to test
     * @return list of data files/folders
     * @see DataFilesProviderImplementation#getDataFiles
     */
    public static List<FileObject> getDataFiles(Project prj) {
        List<FileObject> result = new ArrayList<FileObject>();
        
        for (DataFilesProviderImplementation i : prj.getLookup().lookupAll(DataFilesProviderImplementation.class)) {
            result.addAll(i.getDataFiles());
            assert !result.contains(null) : "Nulls in " + result + " from " + i;
        }
        
        return result;
    }
    
    /**Test whether the delete operation is supported on the given project.
     * 
     * @param prj project to test
     * @return <code>true</code> if the project has a {@link DeleteOperationImplementation},
     *         <code>false</code> otherwise
     */
    public static boolean isDeleteOperationSupported(Project prj) {
        return prj.getLookup().lookup(DeleteOperationImplementation.class) != null;
    }
    
    /**Notification that the project is about to be deleted.
     * Should be called immediately before the project is deleted.
     *
     * The project is supposed to do all required cleanup to allow the project to be deleted.
     *
     * @param prj project to notify
     * @throws IOException is some error occurs
     * @see DeleteOperationImplementation#notifyDeleting
     */
    public static void notifyDeleting(Project prj) throws IOException {
        for (DeleteOperationImplementation i : prj.getLookup().lookupAll(DeleteOperationImplementation.class)) {
            i.notifyDeleting();
        }
    }
    
    /**Notification that the project has been deleted.
     * Should be called immediately after the project is deleted.
     *
     * @param prj project to notify
     * @throws IOException is some error occurs
     * @see DeleteOperationImplementation#notifyDeleted
     */
    public static void notifyDeleted(Project prj) throws IOException {
        for (DeleteOperationImplementation i : prj.getLookup().lookupAll(DeleteOperationImplementation.class)) {
            i.notifyDeleted();
        }
    }
    
    /**Test whether the copy operation is supported on the given project.
     * 
     * @param prj project to test
     * @return <code>true</code> if the project has a {@link CopyOperationImplementation},
     *         <code>false</code> otherwise
     */
    public static boolean isCopyOperationSupported(Project prj) {
        return prj.getLookup().lookup(CopyOperationImplementation.class) != null;
    }
    
    /**Notification that the project is about to be copyied.
     * Should be called immediatelly before the project is copied.
     *
     * The project is supposed to do all required cleanup to allow the project to be copied.
     *
     * @param prj project to notify
     * @throws IOException is some error occurs
     * @see CopyOperationImplementation#notifyCopying
     */
    public static void notifyCopying(Project prj) throws IOException {
        for (CopyOperationImplementation i : prj.getLookup().lookupAll(CopyOperationImplementation.class)) {
            i.notifyCopying();
        }
    }
    
    /**Notification that the project has been copied.
     * Should be called immediatelly after the project is copied.
     *
     * The project is supposed to do all necessary fixes to the project's structure to
     * form a valid project.
     *
     * Both original and newly created project (copy) are notified, in this order.
     *
     * @param original original project
     * @param nue      new project (copy)
     * @param originalPath the project folder of the original project (for consistency with notifyMoved)
     * @param name     new name of the project
     * @throws IOException is some error occurs
     * @see CopyOperationImplementation#notifyCopied
     */
    public static void notifyCopied(Project original, Project nue, File originalPath, String name) throws IOException {
        for (CopyOperationImplementation i : original.getLookup().lookupAll(CopyOperationImplementation.class)) {
            i.notifyCopied(null, originalPath, name);
        }
        for (CopyOperationImplementation i : nue.getLookup().lookupAll(CopyOperationImplementation.class)) {
            i.notifyCopied(original, originalPath, name);
        }
    }
    
    /**Notification that the project is about to be moved.
     * Should be called immediately before the project is moved.
     * {@link MoveOrRenameOperationImplementation#notifyRenaming} may be called instead.
     * The project is supposed to do all required cleanup to allow the project to be moved.
     *
     * @param prj project to notify
     * @throws IOException is some error occurs
     * @see MoveOperationImplementation#notifyMoving
     */
    public static void notifyMoving(Project prj) throws IOException {
        for (MoveOperationImplementation i : prj.getLookup().lookupAll(MoveOperationImplementation.class)) {
            i.notifyMoving();
        }
    }
    
    /**Notification that the project has been moved.
     * Should be called immediatelly after the project is moved.
     * {@link MoveOrRenameOperationImplementation#notifyRenamed} may be called instead.
     *
     * The project is supposed to do all necessary fixes to the project's structure to
     * form a valid project.
     *
     * Both original and moved project are notified, in this order.
     *
     * @param original original project
     * @param nue      moved project
     * @param originalPath the project folder of the original project
     * @param name     new name of the project
     * @throws IOException is some error occurs
     * @see MoveOperationImplementation#notifyMoved
     */
    public static void notifyMoved(Project original, Project nue, File originalPath, String name) throws IOException {
        for (MoveOperationImplementation i : original.getLookup().lookupAll(MoveOperationImplementation.class)) {
            i.notifyMoved(null, originalPath, name);
        }
        for (MoveOperationImplementation i : nue.getLookup().lookupAll(MoveOperationImplementation.class)) {
            i.notifyMoved(original, originalPath, name);
        }
    }
    
    /**
     * Tests whether the move or rename operations are supported on the given project.
     * 
     * @param prj project to test
     * @return <code>true</code> if the project has a {@link MoveOperationImplementation},
     *         <code>false</code> otherwise
     */
    public static boolean isMoveOperationSupported(Project prj) {
        return prj.getLookup().lookup(MoveOperationImplementation.class) != null;
    }
    
}
