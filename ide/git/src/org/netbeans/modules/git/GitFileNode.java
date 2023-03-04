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

package org.netbeans.modules.git;

import java.awt.Color;
import java.io.File;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.GitFileNode.FileNodeInformation;
import org.netbeans.modules.git.options.AnnotationColorProvider;
import org.netbeans.modules.versioning.util.OptionsPanelColorProvider;
import org.netbeans.modules.versioning.util.common.VCSCommitOptions;
import org.netbeans.modules.versioning.util.common.VCSFileInformation;
import org.netbeans.modules.versioning.util.common.VCSFileNode;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public abstract class GitFileNode<T extends FileNodeInformation> extends VCSFileNode {
    
    public abstract static class FileNodeInformation extends VCSFileInformation {

        public abstract boolean isCopied ();

        public abstract boolean isRenamed ();

        public abstract File getOldFile ();
        
    }

    protected GitFileNode(File root, File file) {
        super(root, file);
    }
    
    @Override
    public abstract T getInformation ();

    public static class GitLocalFileNode extends GitFileNode<FileInformation> {
        private FileInformation.Mode mode;
        
        public GitLocalFileNode(File root, File file) {
            super(root, file);
        }
        
        public GitLocalFileNode(File root, File file, FileInformation.Mode mode) {
            super(root, file);
            this.mode = mode;
        }
        
        @Override
        public FileInformation getInformation() {
            return Git.getInstance().getFileStatusCache().getStatus(getFile());
        }

        @Override
        public String getStatusText () {
            return mode == null ? getInformation().getStatusText() : getInformation().getStatusText(mode);
        }

        @Override
        public VCSCommitOptions getDefaultCommitOption (boolean withExclusions) {
            if (withExclusions && GitModuleConfig.getDefault().isExcludedFromCommit(getFile().getAbsolutePath())) {
                return VCSCommitOptions.EXCLUDE;
            } else {
                if(getInformation().containsStatus(FileInformation.STATUS_REMOVED)) {
                    return VCSCommitOptions.COMMIT_REMOVE;
                } else if(getInformation().containsStatus(Status.NEW_INDEX_WORKING_TREE)) {
                    return withExclusions && GitModuleConfig.getDefault().getExludeNewFiles() ? 
                                        VCSCommitOptions.EXCLUDE : 
                                        VCSCommitOptions.COMMIT;
                } else {
                    return VCSCommitOptions.COMMIT;
                }
            }
        }
    }

    public static class GitMergeFileNode extends GitLocalFileNode {
        private FileInformation.Mode mode;
        
        public GitMergeFileNode(File root, File file, FileInformation.Mode mode) {
            super(root, file);
            this.mode = mode;
        }
        
        @Override
        public VCSCommitOptions getDefaultCommitOption (boolean withExclusions) {
            if (getInformation().containsStatus(FileInformation.STATUS_REMOVED)) {
                return VCSCommitOptions.COMMIT_REMOVE;
            } else if (getInformation().containsStatus(Status.NEW_INDEX_WORKING_TREE)
                    && !getInformation().containsStatus(Status.REMOVED_HEAD_INDEX)) {
                return VCSCommitOptions.EXCLUDE;
            } else {
                return VCSCommitOptions.COMMIT;
            }
        }
    }
    
    public static class GitHistoryFileNode extends GitFileNode<HistoryFileInformation> {
        private final HistoryFileInformation fi;
        
        public GitHistoryFileNode(File root, File file, HistoryFileInformation info) {
            super(root, file);
            fi = info;
        }
        
        @Override
        public HistoryFileInformation getInformation() {
            return fi;
        }

        @Override
        public VCSCommitOptions getDefaultCommitOption (boolean withExclusions) {
            return VCSCommitOptions.EXCLUDE;
        }
    }
    
    public static class HistoryFileInformation extends FileNodeInformation {
        private final GitRevisionInfo.GitFileInfo info;

        public HistoryFileInformation (GitRevisionInfo.GitFileInfo info) {
            this.info = info;
        }

        @Override
        public boolean isCopied () {
            return info.getStatus() == GitRevisionInfo.GitFileInfo.Status.COPIED;
        }

        @Override
        public boolean isRenamed () {
            return info.getStatus() == GitRevisionInfo.GitFileInfo.Status.RENAMED;
        }

        @Override
        public File getOldFile () {
            return info.getOriginalFile();
        }

        public String getOldPath () {
            return info.getOriginalPath();
        }

        @Override
        @NbBundle.Messages({
            "CTL_HistoryFileInfo_Status_Added=Added",
            "CTL_HistoryFileInfo_Status_Copied=Copied",
            "CTL_HistoryFileInfo_Status_Renamed=Renamed",
            "CTL_HistoryFileInfo_Status_Modified=Modified",
            "CTL_HistoryFileInfo_Status_Removed=Deleted",
            "CTL_HistoryFileInfo_Status_Unknown=Unknown"
        })
        public String getStatusText () {
            switch (info.getStatus()) {
                case ADDED:
                    return Bundle.CTL_HistoryFileInfo_Status_Added();
                case COPIED:
                    return Bundle.CTL_HistoryFileInfo_Status_Copied();
                case MODIFIED:
                    return Bundle.CTL_HistoryFileInfo_Status_Modified();
                case REMOVED:
                    return Bundle.CTL_HistoryFileInfo_Status_Removed();
                case RENAMED:
                    return Bundle.CTL_HistoryFileInfo_Status_Renamed();
                case UNKNOWN:
                default:
                    return Bundle.CTL_HistoryFileInfo_Status_Unknown();
            }
        }

        @Override
        public int getComparableStatus () {
            return GitRevisionInfo.GitFileInfo.Status.values().length - info.getStatus().ordinal();
        }

        @Override
        public String annotateNameHtml (String name) {
            OptionsPanelColorProvider.AnnotationFormat format;
            switch (info.getStatus()) {
                case ADDED:
                    format = AnnotationColorProvider.getInstance().ADDED_FILE;
                    break;
                case COPIED:
                    format = AnnotationColorProvider.getInstance().ADDED_FILE;
                    break;
                case MODIFIED:
                    format = AnnotationColorProvider.getInstance().MODIFIED_FILE;
                    break;
                case REMOVED:
                    format = AnnotationColorProvider.getInstance().REMOVED_FILE;
                    break;
                case RENAMED:
                    format = AnnotationColorProvider.getInstance().ADDED_FILE;
                    break;
                case UNKNOWN:
                default:
                    format = AnnotationColorProvider.getInstance().EXCLUDED_FILE;
                    break;
            }
            return format.getFormat().format(new Object[] { name, "" });
        }

        @Override
        public Color getAnnotatedColor () {
            switch (info.getStatus()) {
                case ADDED:
                    return AnnotationColorProvider.getInstance().ADDED_FILE.getActualColor();
                case COPIED:
                    return AnnotationColorProvider.getInstance().ADDED_FILE.getActualColor();
                case MODIFIED:
                    return AnnotationColorProvider.getInstance().MODIFIED_FILE.getActualColor();
                case REMOVED:
                    return AnnotationColorProvider.getInstance().REMOVED_FILE.getActualColor();
                case RENAMED:
                    return AnnotationColorProvider.getInstance().ADDED_FILE.getActualColor();
                case UNKNOWN:
                default:
                    return AnnotationColorProvider.getInstance().EXCLUDED_FILE.getActualColor();
            }
        }
        
    }
}
