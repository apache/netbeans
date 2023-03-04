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
package org.netbeans.modules.php.project.connections.sync;

import java.util.LinkedList;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.php.project.connections.TmpLocalFile;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Sync item holding remote and local files and providing
 * operations with them.
 */
public final class SyncItem {

    @StaticResource
    static final String NOOP_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/noop.png"; // NOI18N
    @StaticResource
    static final String DOWNLOAD_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/download.png"; // NOI18N
    @StaticResource
    static final String DOWNLOAD_MIRRORED_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/download_mirrored.png"; // NOI18N
    @StaticResource
    static final String UPLOAD_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/upload.png"; // NOI18N
    @StaticResource
    static final String UPLOAD_MIRRORED_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/upload_mirrored.png"; // NOI18N
    @StaticResource
    static final String DELETE_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/delete.png"; // NOI18N
    @StaticResource
    static final String SYMLINK_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/upload.png"; // NOI18N
    @StaticResource
    static final String FILE_DIR_COLLISION_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/file-dir-collision.png"; // NOI18N
    @StaticResource
    static final String FILE_CONFLICT_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/file-conflict.png"; // NOI18N


    @NbBundle.Messages({
        "Operation.noop.titleWithMnemonic=No O&peration",
        "Operation.noop.toolTip=Set No Operation",
        "Operation.download.titleWithMnemonic=&Download",
        "Operation.download.toolTip=Set Download",
        "Operation.downloadReview.titleWithMnemonic=Download &with Review",
        "Operation.upload.titleWithMnemonic=&Upload",
        "Operation.upload.toolTip=Set Upload",
        "Operation.uploadReview.titleWithMnemonic=Upload w&ith Review",
        "Operation.delete.titleWithMnemonic=D&elete",
        "Operation.delete.toolTip=Set Delete",
        "Operation.symlink.titleWithMnemonic=S&ymbolic Link",
        "Operation.fileDirCollision.titleWithMnemonic=File &vs. Directory Collision",
        "Operation.fileConflict.titleWithMnemonic=File C&onflict"
    })
    public static enum Operation {

        NOOP(Bundle.Operation_noop_titleWithMnemonic(), Bundle.Operation_noop_toolTip(), NOOP_ICON_PATH, false),
        DOWNLOAD(Bundle.Operation_download_titleWithMnemonic(), Bundle.Operation_download_toolTip(), DOWNLOAD_ICON_PATH, DOWNLOAD_MIRRORED_ICON_PATH, true),
        DOWNLOAD_REVIEW(Bundle.Operation_downloadReview_titleWithMnemonic(), DOWNLOAD_ICON_PATH, DOWNLOAD_MIRRORED_ICON_PATH, true),
        UPLOAD(Bundle.Operation_upload_titleWithMnemonic(), Bundle.Operation_upload_toolTip(), UPLOAD_ICON_PATH, UPLOAD_MIRRORED_ICON_PATH, true),
        UPLOAD_REVIEW(Bundle.Operation_uploadReview_titleWithMnemonic(), UPLOAD_ICON_PATH, UPLOAD_MIRRORED_ICON_PATH, true),
        DELETE(Bundle.Operation_delete_titleWithMnemonic(), Bundle.Operation_delete_toolTip(), DELETE_ICON_PATH, false),
        SYMLINK(Bundle.Operation_symlink_titleWithMnemonic(), SYMLINK_ICON_PATH, false),
        FILE_DIR_COLLISION(Bundle.Operation_fileDirCollision_titleWithMnemonic(), FILE_DIR_COLLISION_ICON_PATH, false),
        FILE_CONFLICT(Bundle.Operation_fileConflict_titleWithMnemonic(), FILE_CONFLICT_ICON_PATH, false);


        private final String titleWithMnemonic;
        private final String toolTip;
        private final String iconPath;
        private final String mirroredIconPath;
        private final boolean progress;


        private Operation(String titleWithMnemonic, String iconPath, boolean progress) {
            this(titleWithMnemonic, null, iconPath, iconPath, progress);
        }

        private Operation(String titleWithMnemonic, String toolTip, String iconPath, boolean progress) {
            this(titleWithMnemonic, toolTip, iconPath, iconPath, progress);
        }

        private Operation(String titleWithMnemonic, String toolTip, String iconPath, String mirroredIconPath, boolean progress) {
            this.titleWithMnemonic = titleWithMnemonic;
            this.toolTip = toolTip;
            this.iconPath = iconPath;
            this.mirroredIconPath = mirroredIconPath;
            this.progress = progress;
        }

        public String getTitle() {
            return titleWithMnemonic.replace("&", ""); // NOI18N
        }

        public String getTitleWithMnemonic() {
            return titleWithMnemonic;
        }

        public String getToolTip() {
            return toolTip;
        }

        public Icon getIcon(boolean mirrored) {
            if (mirrored) {
                return ImageUtilities.loadImageIcon(mirroredIconPath, false);
            }
            return ImageUtilities.loadImageIcon(iconPath, false);
        }

        public boolean hasProgress() {
            return progress;
        }

    }


    private final SyncItems syncItems;
    private final TransferFile remoteTransferFile;
    private final TransferFile localTransferFile;
    private final Operation defaultOperation;
    private final boolean hasLastTimestamp;

    private volatile Operation operation;
    // for merging
    private volatile TmpLocalFile tmpLocalFile = null;


    SyncItem(SyncItems syncItems, TransferFile remoteTransferFile, TransferFile localTransferFile, long lastTimestamp) {
        assert syncItems != null;
        assert remoteTransferFile != null || localTransferFile != null;
        this.syncItems = syncItems;
        this.remoteTransferFile = remoteTransferFile;
        this.localTransferFile = localTransferFile;
        defaultOperation = calculateDefaultOperation(lastTimestamp);
        hasLastTimestamp = lastTimestamp != -1;
    }

    public String getName() {
        if (remoteTransferFile != null) {
            return remoteTransferFile.getName();
        }
        return localTransferFile.getName();
    }

    public String getPath() {
        if (remoteTransferFile != null) {
            return remoteTransferFile.getRemotePath();
        }
        return localTransferFile.getRemotePath();
    }

    public long getSize() {
        if (remoteTransferFile != null) {
            return remoteTransferFile.getSize();
        }
        return localTransferFile.getSize();
    }

    public String getRemotePath() {
        if (remoteTransferFile == null) {
            return null;
        }
        return remoteTransferFile.getRemotePath();
    }

    public String getLocalPath() {
        if (localTransferFile == null) {
            return null;
        }
        return localTransferFile.getLocalPath();
    }

    public TransferFile getRemoteTransferFile() {
        return remoteTransferFile;
    }

    public TransferFile getLocalTransferFile() {
        return localTransferFile;
    }

    public Operation getOperation() {
        if (operation != null) {
            return operation;
        }
        return defaultOperation;
    }

    public void setOperation(Operation operation) {
        assert operation != null;
        this.operation = operation;
    }

    public void resetOperation() {
        cleanupTmpLocalFile();
        tmpLocalFile = null;
        operation = null;
    }

    public boolean hasLastTimestamp() {
        return hasLastTimestamp;
    }

    public void cleanupTmpLocalFile() {
        if (hasTmpLocalFile()) {
            tmpLocalFile.cleanup();
        }
    }

    @NbBundle.Messages({
        "SyncItem.error.fileConflict=File must be merged before synchronization.",
        "SyncItem.error.fileDirCollision=Cannot synchronize file with directory.",
        "SyncItem.error.childNotDeleted=Not all children marked for deleting.",
        "SyncItem.error.cannotDownload=Non-existing file cannot be downloaded.",
        "SyncItem.error.cannotUpload=Non-existing file cannot be uploaded.",
        "SyncItem.warn.downloadReview=File should be reviewed before first download.",
        "SyncItem.warn.uploadReview=File should be reviewed before first upload.",
        "SyncItem.warn.symlink=Symbolic links are not transfered (to avoid future overriding)."
    })
    public ValidationResult validate() {
        Operation op = getOperation();
        switch (op) {
            case NOOP:
                // noop
                break;
            case FILE_CONFLICT:
                return new ValidationResult(false, Bundle.SyncItem_error_fileConflict());
                //break;
            case FILE_DIR_COLLISION:
                return new ValidationResult(false, Bundle.SyncItem_error_fileDirCollision());
                //break;
            case SYMLINK:
                return new ValidationResult(true, Bundle.SyncItem_warn_symlink());
                //break;
            case DELETE:
                if (localTransferFile != null
                        && !verifyChildrenOperation(localTransferFile, true, Operation.DELETE)) {
                    return new ValidationResult(false, Bundle.SyncItem_error_childNotDeleted());
                }
                if (remoteTransferFile != null
                        && !verifyChildrenOperation(remoteTransferFile, false, Operation.DELETE)) {
                    return new ValidationResult(false, Bundle.SyncItem_error_childNotDeleted());
                }
                break;
            case DOWNLOAD:
            case DOWNLOAD_REVIEW:
                if (remoteTransferFile == null) {
                    return new ValidationResult(false, Bundle.SyncItem_error_cannotDownload());
                }
                if (op == Operation.DOWNLOAD_REVIEW) {
                    return new ValidationResult(true, Bundle.SyncItem_warn_downloadReview());
                }
                break;
            case UPLOAD:
            case UPLOAD_REVIEW:
                if (localTransferFile == null) {
                    return new ValidationResult(false, Bundle.SyncItem_error_cannotUpload());
                }
                if (op == Operation.UPLOAD_REVIEW) {
                    return new ValidationResult(true, Bundle.SyncItem_warn_uploadReview());
                }
                break;
            default:
                throw new IllegalStateException("Unhandled operation: " + op);
        }
        return ValidationResult.VALID;
    }

    public boolean isDiffPossible() {
        if (remoteTransferFile != null
                && localTransferFile != null) {
            return remoteTransferFile.isFile() && localTransferFile.isFile();
        } else if (remoteTransferFile != null) {
            return remoteTransferFile.isFile();
        }
        return localTransferFile.isFile();
    }

    public boolean isOperationChangePossible() {
        if (getOperation() == Operation.SYMLINK) {
            return false;
        }
        return true;
    }

    public boolean hasTmpLocalFile() {
        return tmpLocalFile != null;
    }

    public TmpLocalFile getTmpLocalFile() {
        return tmpLocalFile;
    }

    public void setTmpLocalFile(TmpLocalFile tmpLocalFile) {
        assert tmpLocalFile != null;
        cleanupTmpLocalFile();
        this.tmpLocalFile = tmpLocalFile;
    }

    private Operation calculateDefaultOperation(long lastTimestamp) {
        if (remoteTransferFile != null && remoteTransferFile.isLink()) {
            return Operation.SYMLINK;
        }
        if (localTransferFile != null && remoteTransferFile != null) {
            if (localTransferFile.isFile() && !remoteTransferFile.isFile()) {
                return Operation.FILE_DIR_COLLISION;
            }
            if (localTransferFile.isDirectory() && !remoteTransferFile.isDirectory()) {
                return Operation.FILE_DIR_COLLISION;
            }
            if (localTransferFile.isDirectory() && remoteTransferFile.isDirectory()) {
                return Operation.NOOP;
            }
        }
        if (lastTimestamp == -1) {
            // running for the first time
            return calculateFirstDefaultOperation();
        }
        return calculateNewDefaultOperation(lastTimestamp);
    }

    private Operation calculateFirstDefaultOperation() {
        if (localTransferFile == null
                || remoteTransferFile == null) {
            if (localTransferFile == null) {
                return Operation.DOWNLOAD;
            }
            return Operation.UPLOAD;
        }
        long localTimestamp = localTransferFile.getTimestamp();
        RemoteTimestamp remoteTimestamp = new RemoteTimestamp(remoteTransferFile.getTimestamp());
        long localSize = localTransferFile.getSize();
        long remoteSize = remoteTransferFile.getSize();
        if (remoteTimestamp.equalsTo(localTimestamp)
                && localSize == remoteSize) {
            // simply equal files
            return Operation.NOOP;
        }
        if (remoteTimestamp.newerThan(localTimestamp)) {
            return Operation.DOWNLOAD_REVIEW;
        }
        return Operation.UPLOAD_REVIEW;
    }

    private Operation calculateNewDefaultOperation(long lastTimestamp) {
        if (localTransferFile == null
                || remoteTransferFile == null) {
            if (localTransferFile == null) {
                return new RemoteTimestamp(remoteTransferFile.getTimestamp()).newerThan(lastTimestamp) ? Operation.DOWNLOAD : Operation.DELETE;
            }
            return localTransferFile.getTimestamp() > lastTimestamp ? Operation.UPLOAD : Operation.DELETE;
        }
        long localTimestamp = localTransferFile.getTimestamp();
        RemoteTimestamp remoteTimestamp = new RemoteTimestamp(remoteTransferFile.getTimestamp());
        long localSize = localTransferFile.getSize();
        long remoteSize = remoteTransferFile.getSize();
        if (remoteTimestamp.equalsTo(localTimestamp)
                && localSize == remoteSize) {
            // simply equal files
            return Operation.NOOP;
        }
        if (localTimestamp <= lastTimestamp
                && remoteTimestamp.equalsOrOlderThan(lastTimestamp)
                && localSize == remoteSize) {
            // already synchronized
            return Operation.NOOP;
        }
        if (localTimestamp > lastTimestamp
                && remoteTimestamp.newerThan(lastTimestamp)) {
            // both files are newer
            return Operation.FILE_CONFLICT;
        }
        // only one file is newer
        if (remoteTimestamp.newerThan(localTimestamp)) {
            return Operation.DOWNLOAD;
        }
        return Operation.UPLOAD;
    }

    private boolean verifyChildrenOperation(TransferFile transferFile, boolean localChildren, Operation operation) {
        LinkedList<TransferFile> children = new LinkedList<>();
        children.addAll(localChildren ? transferFile.getLocalChildren() : transferFile.getRemoteChildren());
        while (!children.isEmpty()) {
            TransferFile child = children.pop();
            SyncItem syncItem = syncItems.getByRemotePath(child.getRemotePath());
            if (syncItem.getOperation() != operation) {
                return false;
            }
            children.addAll(localChildren ? child.getLocalChildren() : child.getRemoteChildren());
        }
        return true;
    }

    @Override
    public String toString() {
        return "SyncItem{" // NOI18N
                + "path: " + (localTransferFile != null ? localTransferFile.getRemotePath() : remoteTransferFile.getRemotePath()) // NOI18N
                + ", localFile: " + (localTransferFile != null) // NOI18N
                + ", remoteFile: " + (remoteTransferFile != null) // NOI18N
                + ", operation: " + getOperation() // NOI18N
                + ", valid: " + validate() // NOI18N
                + ", tmpLocalFile: " + (hasTmpLocalFile()) // NOI18N
                + "}"; // NOI18N
    }

    //~ Inner classes

    private static final class RemoteTimestamp {

        private static final long TIMEDIFF_TOLERANCE = 30L; // in seconds

        private final long remoteTimestamp;


        public RemoteTimestamp(long remoteTimestamp) {
            this.remoteTimestamp = remoteTimestamp;
        }

        public boolean equalsTo(long timestamp) {
            // similarly as ant
            return Math.abs(timestamp - remoteTimestamp) < TIMEDIFF_TOLERANCE;
        }

        public boolean equalsOrOlderThan(long timestamp) {
            if (equalsTo(timestamp)) {
                return true;
            }
            return remoteTimestamp <= timestamp;
        }

        public boolean newerThan(long timestamp) {
            if (equalsTo(timestamp)) {
                return false;
            }
            return remoteTimestamp > timestamp;
        }

        @Override
        public String toString() {
            return String.valueOf(remoteTimestamp);
        }

    }

    //~ Inner classes

    public static final class ValidationResult {

        static final ValidationResult VALID = new ValidationResult(true, null);

        private final boolean valid;
        private final String message;


        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean hasError() {
            return !valid;
        }

        public boolean hasWarning() {
            return valid && message != null;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "ValidationResult{valid=" + valid + ", message=" + message + '}'; // NOI18N
        }

    }

}
