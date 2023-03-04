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

package org.netbeans.modules.subversion;

import org.openide.util.NbBundle;

import java.util.*;
import java.io.Serializable;
import java.io.File;
import java.util.logging.Level;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNStatusUnversioned;

/**
 * Immutable class encapsulating status of a file.
 *
 * @author Maros Sandor
 */
public class FileInformation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * There is nothing known about the file, it may not even exist.
     */ 
    public static final int STATUS_UNKNOWN                      = 0;

    /**
     * The file is not managed by the module, i.e. the user does not wish it to be under control of this
     * versioning system module. All files except files under versioned roots have this status.
     */ 
    public static final int STATUS_NOTVERSIONED_NOTMANAGED      = 1;
    
    /**
     * The file exists locally but is NOT under version control because it should not be (i.e. is has
     * the Ignore property set or resides under an excluded folder). The file itself IS under a versioned root.
     */ 
    public static final int STATUS_NOTVERSIONED_EXCLUDED        = 2;

    /**
     * The file exists locally but is NOT under version control, mostly because it has not been added
     * to the repository yet.
     */ 
    public static final int STATUS_NOTVERSIONED_NEWLOCALLY      = 4;
        
    /**
     * The file is under version control and is in sync with repository.
     */ 
    public static final int STATUS_VERSIONED_UPTODATE           = 8;
    
    /**
     * The file is modified locally and was not yet modified in repository.
     */ 
    public static final int STATUS_VERSIONED_MODIFIEDLOCALLY_CONTENT    = 16;
    
    /**
     * The file was not modified locally but an updated version exists in repository.
     */ 
    public static final int STATUS_VERSIONED_MODIFIEDINREPOSITORY = 32;
    
    /**
     * Merging during update resulted in merge conflict. Conflicts in the local copy must be resolved before
     * the file can be commited.  
     */ 
    public static final int STATUS_VERSIONED_CONFLICT_CONTENT   = 64;

    /**
     * The file was modified both locally and remotely and these changes may or may not result in
     * merge conflict. 
     */ 
    public static final int STATUS_VERSIONED_MERGE              = 128;
    
    /**
     * The file does NOT exist locally and exists in repository, it has beed removed locally, waits
     * for commit.
     */ 
    public static final int STATUS_VERSIONED_REMOVEDLOCALLY     = 256;
    
    /**
     * The file does NOT exist locally but exists in repository and has not yet been downloaded. 
     */ 
    public static final int STATUS_VERSIONED_NEWINREPOSITORY    = 512;

    /**
     * The file has been removed from repository. 
     */ 
    public static final int STATUS_VERSIONED_REMOVEDINREPOSITORY = 1024;

    /**
     * The file does NOT exist locally and exists in repository, it has beed removed locally.
     */ 
    public static final int STATUS_VERSIONED_DELETEDLOCALLY     = 2048;
    
    /**
     * The file exists locally and has beed scheduled for addition to repository. This status represents
     * state after the 'add' command.
     */ 
    public static final int STATUS_VERSIONED_ADDEDLOCALLY       = 4096;

    /**
     * The file has an active lock against it.
     */
    public static final int STATUS_LOCKED = 8192;

    /**
     * Local tree conflict
     */
    public static final int STATUS_VERSIONED_CONFLICT_TREE = 16384;
    public static final int STATUS_VERSIONED_CONFLICT = STATUS_VERSIONED_CONFLICT_CONTENT | STATUS_VERSIONED_CONFLICT_TREE;

    /**
     * The file is locked elsewhere
     */
    public static final int STATUS_LOCKED_REMOTELY = 32768;
    
    /**
     * The file has a modified property list
     */
    public static final int STATUS_VERSIONED_MODIFIEDLOCALLY_PROPERTY = STATUS_LOCKED_REMOTELY * 2;
    public static final int STATUS_VERSIONED_MODIFIEDLOCALLY = STATUS_VERSIONED_MODIFIEDLOCALLY_CONTENT | STATUS_VERSIONED_MODIFIEDLOCALLY_PROPERTY;
    
    public static final int STATUS_ALL = ~0;

    /**
     * All statuses except <tt>STATUS_NOTVERSIONED_NOTMANAGED</tt>
     *
     * <p>Note: it covers ignored files.
     */
    public static final int STATUS_MANAGED = STATUS_ALL & ~STATUS_NOTVERSIONED_NOTMANAGED;


    /** Has local metadata under .svn/ */
    public static final int STATUS_VERSIONED = STATUS_VERSIONED_UPTODATE |
            STATUS_VERSIONED_MODIFIEDLOCALLY |
            STATUS_VERSIONED_MODIFIEDINREPOSITORY |
            STATUS_VERSIONED_CONFLICT |
            STATUS_VERSIONED_MERGE |
            STATUS_VERSIONED_REMOVEDLOCALLY |
            STATUS_VERSIONED_REMOVEDINREPOSITORY |
            STATUS_VERSIONED_DELETEDLOCALLY |
            STATUS_VERSIONED_ADDEDLOCALLY;

    public static final int STATUS_IN_REPOSITORY = STATUS_VERSIONED_UPTODATE |
            STATUS_VERSIONED_MODIFIEDLOCALLY |
            STATUS_VERSIONED_MODIFIEDINREPOSITORY |
            STATUS_VERSIONED_CONFLICT |
            STATUS_VERSIONED_MERGE |
            STATUS_VERSIONED_REMOVEDLOCALLY |
            STATUS_VERSIONED_NEWINREPOSITORY |
            STATUS_VERSIONED_REMOVEDINREPOSITORY |
            STATUS_VERSIONED_DELETEDLOCALLY;

    public static final int STATUS_LOCAL_CHANGE =
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | 
            FileInformation.STATUS_VERSIONED_ADDEDLOCALLY | 
            FileInformation.STATUS_VERSIONED_CONFLICT | 
            FileInformation.STATUS_VERSIONED_DELETEDLOCALLY |
            FileInformation.STATUS_VERSIONED_MERGE |
            FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY;

    /**
     * Modified, in conflict, scheduled for removal or addition;
     * or deleted but with existing entry record.
     */
    public static final int STATUS_REVERTIBLE_CHANGE =
            FileInformation.STATUS_VERSIONED_ADDEDLOCALLY | 
            FileInformation.STATUS_VERSIONED_CONFLICT | 
            FileInformation.STATUS_VERSIONED_MERGE |
            FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY |
            FileInformation.STATUS_VERSIONED_DELETEDLOCALLY |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY;


    public static final int STATUS_REMOTE_CHANGE = 
            FileInformation.STATUS_VERSIONED_MERGE | 
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_NEWINREPOSITORY |
            FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY;
    
    
    /**
     * Status constant.
     */ 
    private final int   status;

    /**
     * Status constant for properties.
     */ 
    private final int   propStatus;
    
    /**
     * Entry from the .svn directory, if it exists and has been read.
     */
    private transient ISVNStatus entry;

    /**
     * Directory indicator.
     */ 
    private final boolean   isDirectory;
    
    private static final String STATUS_UNKNOWN_EXT = "W";  // NOI18N
    private static final String STATUS_NOTVERSIONED_NOTMANAGED_EXT = "Z"; // NOI18N
    private static final String STATUS_NOTVERSIONED_EXCLUDED_EXT = "I"; // NOI18N
    private static final String STATUS_NOTVERSIONED_NEWLOCALLY_EXT = "?"; // NOI18N
    private static final String STATUS_VERSIONED_UPTODATE_EXT = "S"; // NOI18N
    private static final String STATUS_VERSIONED_MODIFIEDLOCALLY_EXT = "M"; // NOI18N
    private static final String STATUS_VERSIONED_MODIFIEDINREPOSITORY_EXT = "G"; // NOI18N
    private static final String STATUS_VERSIONED_CONFLICT_EXT = "C"; // NOI18N
    private static final String STATUS_VERSIONED_MERGE_EXT = "P"; // NOI18N
    private static final String STATUS_VERSIONED_REMOVEDLOCALLY_EXT = "R"; // NOI18N
    private static final String STATUS_VERSIONED_NEWINREPOSITORY_EXT = "N"; // NOI18N
    private static final String STATUS_VERSIONED_REMOVEDINREPOSITORY_EXT = "D"; // NOI18N
    private static final String STATUS_VERSIONED_DELETEDLOCALLY_EXT = "E"; // NOI18N
    private static final String STATUS_VERSIONED_ADDEDLOCALLY_EXT = "A"; // NOI18N

    /**
     * For deserialization purposes only.
     */ 
    public FileInformation() {
        status = 0;
        propStatus = 0;
        isDirectory = false;
    }

    private FileInformation(int status, int propStatus, ISVNStatus entry, boolean isDirectory) {
        this.status = status;
        this.propStatus = propStatus;
        this.entry = entry;
        this.isDirectory = isDirectory;
    }

    FileInformation(int status, ISVNStatus entry) {
        this(status, 0, entry, entry.getNodeKind() == SVNNodeKind.DIR);
    }

    FileInformation(int status, boolean isDirectory) {
        this(status, 0, null, isDirectory);
    }
    
    public FileInformation(int status, int propStatus, boolean isDirectory) {
        this(status, propStatus, null, isDirectory);
    }
    
    /**
     * Retrieves the status constant representing status of the file.
     * 
     * @return one of status constants
     */ 
    public int getStatus() {
        return status;
    }

    public boolean isDirectory() {
        return isDirectory;
    }
    
    /**
     * Retrieves file's Entry.
     *
     * @param file file this information belongs to or null if you do not want the entry to be read from disk 
     * in case it is not loaded yet
     * @return Status parsed entry form the .svn/entries file or null if the file does not exist,
     * is not versioned or its entry is invalid
     */
    public ISVNStatus getEntry(File file) {
        if (file != null && (entry == null || !entry.getFile().equals(file))) {
            readEntry(file);
        }
        return entry;
    }
    
    private void readEntry(File file) {
        try {
            entry = SvnUtils.getSingleStatus(Subversion.getInstance().getClient(false), file);
        } catch (SVNClientException e) {
            // at least log the exception
            if (!WorkingCopyAttributesCache.getInstance().isSuppressed(e)) {
                Subversion.LOG.log(Level.INFO, null, e);
            }
        }
    }    

    /**
     * Returns localized text representation of status.
     * 
     * @return status name, for multistatuses prefers local
     * status name.
     */ 
    public String getStatusText() {
        return getStatusText(~0);
    }    

    /**
     * Returns localized text representation of status.
     *
     * @param displayStatuses statuses bitmask
     *
     * @return status name, for multistatuses prefers local
     * status name, for masked <tt>""</tt>.
     */
    public String getStatusText(int displayStatuses) {
        int status = this.status & displayStatuses;
        ResourceBundle loc = NbBundle.getBundle(FileInformation.class);
        String retval;
        if (status == FileInformation.STATUS_UNKNOWN) {
            retval = loc.getString("CTL_FileInfoStatus_Unknown");            
        } else if (match(status, FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) {
            retval = loc.getString("CTL_FileInfoStatus_Excluded");
        } else if (match(status, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            retval = loc.getString("CTL_FileInfoStatus_NewLocally");
        } else if (match(status, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            if (entry != null && entry.isCopied()) {
                retval = loc.getString("CTL_FileInfoStatus_AddedLocallyCopied");
            } else {
                retval = loc.getString("CTL_FileInfoStatus_AddedLocally");
            }
        } else if (match(status, FileInformation.STATUS_VERSIONED_UPTODATE)) {
            retval = loc.getString("CTL_FileInfoStatus_UpToDate");
        } else if (match(status, FileInformation.STATUS_VERSIONED_CONFLICT_TREE)) {
            retval = loc.getString("CTL_FileInfoStatus_TreeConflict");
        } else if (match(status, FileInformation.STATUS_VERSIONED_CONFLICT)) {
            retval = loc.getString("CTL_FileInfoStatus_Conflict");
        } else if (match(status, FileInformation.STATUS_VERSIONED_MERGE)) {
            retval = loc.getString("CTL_FileInfoStatus_Merge");            
        } else if (match(status, FileInformation.STATUS_VERSIONED_DELETEDLOCALLY)) {
            retval = loc.getString("CTL_FileInfoStatus_DeletedLocally");
        } else if (match(status, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
            retval = loc.getString("CTL_FileInfoStatus_RemovedLocally");
        } else if (match(status, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY)) {
            retval = loc.getString("CTL_FileInfoStatus_ModifiedLocally");
        } else if (match(status, FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
            retval = loc.getString("CTL_FileInfoStatus_NewInRepository");
        } else if (match(status, FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY)) {
            retval = loc.getString("CTL_FileInfoStatus_ModifiedInRepository");
        } else if (match(status, FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY)) {
            retval = loc.getString("CTL_FileInfoStatus_RemovedInRepository");
        } else {
            retval = "";   // NOI18N                     
        }
        if ((this.status & STATUS_LOCKED) != 0) {
            if (!retval.isEmpty()) {
                retval += "/"; //NOI18N
            }
            retval += loc.getString("CTL_FileInfoStatus_LockedLocally");
        } else if ((this.status & STATUS_LOCKED_REMOTELY) != 0) {
            if (!retval.isEmpty()) {
                retval += "/"; //NOI18N
            }
            retval += loc.getString("CTL_FileInfoStatus_LockedRemotely");
        }
        return retval;
    }    

    /**
     * @return short status name for local changes, for remote
     * changes returns <tt>""</tt>
     */
    public String getShortStatusText() {
        ResourceBundle loc = NbBundle.getBundle(FileInformation.class);
        if (match(status, FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) {
            return loc.getString("CTL_FileInfoStatus_Excluded_Short");
        } else if (match(status, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            return loc.getString("CTL_FileInfoStatus_NewLocally_Short");
        } else if (match(status, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            if (entry != null && entry.isCopied()) {
                return loc.getString("CTL_FileInfoStatus_AddedLocallyCopied_Short");
            }
            return loc.getString("CTL_FileInfoStatus_AddedLocally_Short");
        } else if (status == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) {
            return loc.getString("CTL_FileInfoStatus_RemovedLocally_Short");
        } else if (status == FileInformation.STATUS_VERSIONED_DELETEDLOCALLY) {
            return loc.getString("CTL_FileInfoStatus_DeletedLocally_Short");
        } else if (match(status, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY)) {
            return loc.getString("CTL_FileInfoStatus_ModifiedLocally_Short");
        } else if (match(status, FileInformation.STATUS_VERSIONED_CONFLICT_TREE)) {
            return loc.getString("CTL_FileInfoStatus_TreeConflict_Short");
        } else if (match(status, FileInformation.STATUS_VERSIONED_CONFLICT)) {
            return loc.getString("CTL_FileInfoStatus_Conflict_Short");
        } else {
            return "";  // NOI18N                  
        }
    }

    private static boolean match(int status, int mask) {
        return (status & mask) != 0;
    }

    @Override
    public String toString() {
        return "Text: " + status + " " + getStatusText(status) + "\nProp: " + propStatus + " " + getStatusText(propStatus); // NOI18N
    }
}

