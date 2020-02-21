/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.mercurial.remote.ui.queues;

import org.netbeans.modules.mercurial.remote.FileInformation;
import org.netbeans.modules.mercurial.remote.HgModuleConfig;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.remotefs.versioning.util.common.VCSFileNode;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.util.common.VCSCommitOptions;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public class QFileNode extends VCSFileNode<FileInformation> {
    public static final VCSCommitOptions INCLUDE = new VCSCommitOptions.Commit(NbBundle.getMessage(QFileNode.class, "IncludeOption.name")); //NOI18N
    public static final VCSCommitOptions EXCLUDE = new VCSCommitOptions.Commit(NbBundle.getMessage(QFileNode.class, "ExcludeOption.name")); //NOI18N
    private final FileInformation fi;

    public QFileNode(VCSFileProxy root, VCSFileProxy file) {
        this(root, file, null);
    }

    QFileNode (VCSFileProxy repository, VCSFileProxy file, FileInformation fi) {
        super(repository, file);
        this.fi = fi;
    }

    @Override
    public FileInformation getInformation() {
        return fi == null ? Mercurial.getInstance().getFileStatusCache().getStatus(getFile()) : fi;
    }

    @Override
    public VCSCommitOptions getDefaultCommitOption (boolean withExclusions) {
        if (withExclusions && HgModuleConfig.getDefault(getRoot()).isExcludedFromCommit(getFile().getPath())) {
            return EXCLUDE;
        } else {
            if ((getInformation().getStatus() & (FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY | FileInformation.STATUS_VERSIONED_DELETEDLOCALLY)) != 0) {
                return VCSCommitOptions.COMMIT_REMOVE;
            } else if ((getInformation().getStatus() & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) != 0) {
                return HgModuleConfig.getDefault(getRoot()).getExludeNewFiles() ? EXCLUDE : INCLUDE;
            } else {
                return INCLUDE;
            }
        }
    }

}
