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
    
    public static abstract class FileNodeInformation extends VCSFileInformation {

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
