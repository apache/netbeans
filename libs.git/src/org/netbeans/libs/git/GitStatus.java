/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
