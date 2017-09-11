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
