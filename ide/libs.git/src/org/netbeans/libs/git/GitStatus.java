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

package org.netbeans.libs.git;

import java.io.File;
import org.eclipse.jgit.diff.DiffEntry;

/**
 * Provides overall information about git status of a certain resource 
 * in a git repository.
 * 
 * @author Ondra Vrabec
 */
public final class GitStatus {

    private final File file;
    private final String relativePath;
    private final boolean tracked;
    private GitConflictDescriptor conflictDescriptor;
    private Status statusHeadIndex;
    private Status statusIndexWC;
    private Status statusHeadWC;
    private boolean isFolder;
    private DiffEntry diffEntry;
    private final String workTreePath;
    private long indexEntryModificationDate;

    /**
     * File's status, respectively the state of a file between two trees
     * (can be HEAD vs. index, HEAD vs. working tree or index vs. working tree).
     */
    public enum Status {
        STATUS_ADDED, STATUS_REMOVED, STATUS_NORMAL, STATUS_MODIFIED, STATUS_IGNORED
    }

    GitStatus (String workTreePath, File file, String relativePath, boolean tracked) {
        this.workTreePath = workTreePath;
        this.file = file;
        this.relativePath = relativePath;
        this.tracked = tracked;
    }
    
    /**
     * @return file the status is associated with
     */
    public File getFile () {
        return file;
    }

    /**
     * @return relative path of the file in the repository
     */
    public String getRelativePath () {
        return relativePath;
    }

    /**
     * @return file's state/difference between the HEAD and Index
     */
    public Status getStatusHeadIndex () {
        return statusHeadIndex;
    }

    /**
     * @return file's state/difference between the Index and Working tree
     */
    public Status getStatusIndexWC () {
        return statusIndexWC;
    }

    /**
     * @return file's state/difference between the HEAD and Working tree
     */
    public Status getStatusHeadWC () {
        return statusHeadWC;
    }

    /**
     * @return <code>true</code> if the file is tracked by Git, 
     * meaning it has been already committed or added to the Index
     */
    public boolean isTracked () {
        return tracked;
    }

    /**
     * States if the file is currently in conflict and needs to be resolved.
     * If the file is in conflict then:
     * <ul>
     * <li>more information can be acquired with the <code>getConflictDescriptor</code> method</li>
     * <li>contents of the file in conflict (base, mine and others) can be acquired via 
     * {@link GitClient#catIndexEntry(java.io.File, int, java.io.OutputStream, org.netbeans.libs.git.progress.ProgressMonitor) }. </li>
     * </ul>
     * @return <code>true</code> if the file is currently in conflict.
     */
    public boolean isConflict () {
        return conflictDescriptor != null;
    }

    /**
     * @return <code>true</code> if the file references a folder.
     */
    public boolean isFolder () {
        return isFolder;
    }

    /**
     * @return <code>true</code> if the file is tracked in the Index as copied.
     */
    public boolean isCopied () {
        return diffEntry != null && diffEntry.getChangeType().equals(DiffEntry.ChangeType.COPY);
    }

    /**
     * @return <code>true</code> if the file is tracked in the Index as renamed.
     */
    public boolean isRenamed () {
        return diffEntry != null && diffEntry.getChangeType().equals(DiffEntry.ChangeType.RENAME);
    }

    /**
     * @return <code>null</code> if the file is neither copied or renamed, the original file this 
     * file has been copied or renamed from otherwise.
     */
    public File getOldPath () {
        if (isRenamed() || isCopied()) {
            return new File(workTreePath + File.separator + diffEntry.getOldPath());
        } else {
            return null;
        }
    }

    /**
     * @return more information about the conflict or <code>null</code> if the file is not in conflict.
     */
    public GitConflictDescriptor getConflictDescriptor () {
        return conflictDescriptor;
    }
    
    /**
     * Returns the time in milliseconds of the last modification timestamp of
     * the index entry. Useful when you need to know when the file was last
     * updated in the index.
     *
     * @return modification timestamp of the index in milliseconds. When there
     * is no such entry in the index (file was removed or not yet added) this
     * returns <code>-1</code>.
     * @since 1.19
     */
    public long getIndexEntryModificationDate () {
        return indexEntryModificationDate;
    }

    void setDiffEntry (DiffEntry diffEntry) {
        this.diffEntry = diffEntry;
    }

    void setConflictDescriptor (GitConflictDescriptor conflictDescriptor) {
        this.conflictDescriptor = conflictDescriptor;
    }

    void setFolder (boolean isFolder) {
        this.isFolder = isFolder;
    }

    void setStatusHeadIndex (Status statusHeadIndex) {
        this.statusHeadIndex = statusHeadIndex;
    }

    void setStatusHeadWC (Status statusHeadWC) {
        this.statusHeadWC = statusHeadWC;
    }

    void setStatusIndexWC (Status statusIndexWC) {
        this.statusIndexWC = statusIndexWC;
    }

    void setIndexEntryModificationDate (long ts) {
        this.indexEntryModificationDate = ts;
    }
}
