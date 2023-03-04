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

package org.netbeans.modules.mercurial.ui.queues;

import java.io.File;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.versioning.util.common.VCSCommitOptions;
import org.netbeans.modules.versioning.util.common.VCSFileNode;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class QFileNode extends VCSFileNode<FileInformation> {
    public static final VCSCommitOptions INCLUDE = new VCSCommitOptions.Commit(NbBundle.getMessage(QFileNode.class, "IncludeOption.name")); //NOI18N
    public static final VCSCommitOptions EXCLUDE = new VCSCommitOptions.Commit(NbBundle.getMessage(QFileNode.class, "ExcludeOption.name")); //NOI18N
    private final FileInformation fi;

    public QFileNode(File root, File file) {
        this(root, file, null);
    }

    QFileNode (File repository, File file, FileInformation fi) {
        super(repository, file);
        this.fi = fi;
    }

    @Override
    public FileInformation getInformation() {
        return fi == null ? Mercurial.getInstance().getFileStatusCache().getStatus(getFile()) : fi;
    }

    @Override
    public VCSCommitOptions getDefaultCommitOption (boolean withExclusions) {
        if (withExclusions && HgModuleConfig.getDefault().isExcludedFromCommit(getFile().getAbsolutePath())) {
            return EXCLUDE;
        } else {
            if ((getInformation().getStatus() & (FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY | FileInformation.STATUS_VERSIONED_DELETEDLOCALLY)) != 0) {
                return VCSCommitOptions.COMMIT_REMOVE;
            } else if ((getInformation().getStatus() & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) != 0) {
                return HgModuleConfig.getDefault().getExludeNewFiles() ? EXCLUDE : INCLUDE;
            } else {
                return INCLUDE;
            }
        }
    }

}
