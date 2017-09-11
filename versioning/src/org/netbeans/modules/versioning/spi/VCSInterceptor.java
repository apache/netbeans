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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.versioning.spi;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Versioning systems that need to intercept or listen to file system operations implement this class.  
 * 
 * @author Maros Sandor
 */
public abstract class VCSInterceptor {

    /**
     * Protected constructor, does nothing.   
     */
    protected VCSInterceptor() {
    }

    // ==================================================================================================
    // QUERIES
    // ==================================================================================================

    /**
     * Queries the versioning system for file mutability (write, delete, move). Versioning systems that keep files
     * read-only in working copy can override this method to signal that such files are in fact mutable even if
     * they appear read-only on local file system. When IDE eventually tries to delete or write to these files then
     * the Versioning system should intercept these calls and make requested files writable on-demand.
     * 
     * @param file a file to query
     * @return true if the file is mutable (writable, deletable and movable), false otherwise
     * @since 1.7
     */
    public boolean isMutable(File file) {
        return file.canWrite();
    }

    /**
     * Queries the versioning system for a files VCS specific attribute. At the momement the
     * only supported attribute name is <code>ProvidedExtensions.RemoteLocation</code>
     * 
     * @param file a file to get the attribute for
     * @param attrName te attributes name
     * @return the attributes value or null if not available
     * @since 1.10
     */
    public Object getAttribute(File file, String attrName) {
        return null;
    }

    // ==================================================================================================
    // DELETE
    // ==================================================================================================
    
    /**
     * Notifies the interceptor that the file or folder is about to be deleted. The interceptor MUST NOT delete
     * the file here.
     * 
     * @param file a file or an empty folder to be deleted
     * @return true if this interceptor wants to handle this operation (doDelete will be called), false otherwise
     */
    public boolean beforeDelete(File file) {
        return false;
    }

    /**
     * Called if beforeDelete() returns true and delegates the delete operation to this interceptor. The interceptor
     * may decide to either delete the file or leave it intact. In case it does not want to delete the file, it should
     * just return without doing anything.
     * 
     * @param file a file or an empty folder to delete; the interceptor will never be asked to delete a non-empty folder
     * @throws IOException if the delete operation failed
     */
    public void doDelete(File file) throws IOException {
    }

    /**
     * Called after a file or folder is deleted. In case the file was deleted outside IDE, this is the only method called.
     * 
     * @param file deleted file
     */
    public void afterDelete(File file) {
    }
    
    // ==================================================================================================
    // MOVE
    // ==================================================================================================
    
    /**
     * Notifies the interceptor that the file or folder is about to be moved. The interceptor MUST NOT move
     * the file here.
     * 
     * @param from the file or folder to be moved
     * @param to destination of the file being moved
     * @return true if this interceptor wants to handle this operation (doMove will be called), false otherwise
     */
    public boolean beforeMove(File from, File to) {
        return false;
    }

    /**
     * Called if beforeMove() returns true and delegates the move operation to this interceptor.
     * 
     * @param from the file or folder to be moved
     * @param to destination of the file being moved
     * @throws IOException if the move operation failed
     */
    public void doMove(File from, File to) throws IOException {
    }

    /**
     * Called after a file or folder has beed moved. In case the file was moved outside IDE, this method is not called but 
     * a pair or afterDelete() / afterCreate() is called instead.
     * 
     * @param from original location of the file
     * @param to current location of the file
     */
    public void afterMove(File from, File to) {
    }
    
    // ==================================================================================================
    // COPY
    // ==================================================================================================

    /**
     * Notifies the interceptor that the file or folder is about to be copied. The interceptor MUST NOT copy
     * the file here.
     *
     * @param from the file or folder to be copied
     * @param to destination of the file being copied
     * @return true if this interceptor wants to handle this operation (doCopy will be called), false otherwise
     * @since 1.18
     */
    public boolean beforeCopy(File from, File to) {
        return false;
    }

    /**
     * Called if beforeCopy() returns true and delegates the copy operation to this interceptor.
     *
     * @param from the file or folder to be copied
     * @param to destination of the file being copied
     * @throws IOException if the copy operation failed
     * @since 1.18
     */
    public void doCopy(File from, File to) throws IOException {
    }

    /**
     * Called after a file or folder has been copied. In case the file was copied outside IDE, this method is not called
     * and only afterCreate() is called instead.
     *
     * @param from original location of the file
     * @param to current location of the file
     * @since 1.18
     */
    public void afterCopy(File from, File to) {
    }
    
    // ==================================================================================================
    // CREATE
    // ==================================================================================================

    /**
     * Notifies the interceptor that the file or folder is about to be created. The interceptor MUST NOT create
     * the file here.
     * 
     * Beware: It may happen on some filesystems that the file will be ALREADY created.
     * 
     * @param file file or folder to be created
     * @return true if this interceptor wants to handle this operation (doCreate will be called), false otherwise
     */
    public boolean beforeCreate(File file, boolean isDirectory) {
        return false;
    }

    /**
     * Called if beforeCreate() returns true and delegates the create operation to this interceptor.
     * 
     * Beware: It may happen on some filesystems that the file will be ALREADY created.
     * 
     * @param file the file to create
     * @param isDirectory true if the new file should be a directory, false otherwise
     * @throws IOException if the create operation failed
     */
    public void doCreate(File file, boolean isDirectory) throws IOException {
    }

    /**
     * Called after a new file or folder has beed created. In case the file was created outside IDE, this is the only
     * method called.
     * 
     * @param file the new file
     */
    public void afterCreate(File file) {
    }
    
    // ==================================================================================================
    // CHANGE
    // ==================================================================================================

    /**
     * Called after a file changed.
     * 
     * @param file changed file
     */
    public void afterChange(File file) {
    }
    
    /**
     * Called before a file is changed.
     * Each series of beforeChange/afterChange events is preceded by at least one beforeEdit event.
     * 
     * @param file to be changed file
     */
    public void beforeChange(File file) {
    }
    
    /**
     * Called before a file is about to enter Edit mode. In case the versioning system uses file locking 
     * this is the time when to check-out (edit) the file and make it read/write. CVS would execute 'cvs edit' here. If
     * you do not (wish to) support automatic file check-out, do nothing here. 
     * Each series of beforeChange/afterChange events is preceded by at least one beforeEdit event.
     * 
     * @param file file that was just locked and is expected to change
     */
    public void beforeEdit(File file) {
    }

    /** Allows versioning system to exclude some children from recursive
     * listening check. Also notifies the versioning whenever a refresh
     * is required and allows the versiniong to provide special timestamp
     * for a directory.
     * <p>
     * Default implementation of this method returns -1.
     *
     * @param dir the directory to check timestamp for
     * @param lastTimeStamp the previously known timestamp or -1
     * @param children add subfiles that shall be iterated into this array
     * @return the timestamp that shall represent this directory, it will
     *   be compared with timestamps of all children and the newest
     *   one will be kept and next time passed as lastTimeStamp. Return
     *   0 if the directory does not have any special timestamp. Return
     *   -1 if you are not providing any special implementation
     * @since 1.17
     */
    public long refreshRecursively(File dir, long lastTimeStamp, List<? super File> children) {
        return -1;
    }
}
