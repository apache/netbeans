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
