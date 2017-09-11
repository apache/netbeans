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

package org.netbeans.modules.git.ui.diff;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.ResourceBundle;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileInformation.Mode;
import org.netbeans.modules.git.GitFileNode;
import org.netbeans.modules.git.ui.repository.Revision;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.diff.AbstractDiffSetup;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public final class Setup extends AbstractDiffSetup {

    private final File      baseFile;

    private final String    firstRevision;
    private final String    secondRevision;

    private DiffStreamSource    firstSource;
    private DiffStreamSource    secondSource;

    private DiffController      view;
    private DiffNode node;

    private String    title;

    public Setup (GitFileNode<FileInformation> node, Mode mode, Revision revision) {
        this.baseFile = node.getFile();

        ResourceBundle loc = NbBundle.getBundle(Setup.class);
        String firstTitle;
        String secondTitle;
        FileInformation info = node.getInformation();
        File originalFile = null;
        if (info != null && (info.isCopied() || info.isRenamed())) {
            originalFile = info.getOldFile();
        }

        // <editor-fold defaultstate="collapsed" desc="left panel">
        switch (mode) {
            case HEAD_VS_WORKING_TREE:
            case HEAD_VS_INDEX:
                firstRevision = originalFile == null && info.containsStatus(EnumSet.of(FileInformation.Status.NEW_HEAD_WORKING_TREE, FileInformation.Status.NEW_HEAD_INDEX)) ? null : revision.getCommitId();
                firstTitle = originalFile == null
                        ? revision.toString()
                        : MessageFormat.format(loc.getString("MSG_DiffPanel_Revision.file"), //NOI18N
                        new Object[] { revision.toString(), originalFile.getName() } );
                break;
            case INDEX_VS_WORKING_TREE:
                firstRevision = GitUtils.INDEX;
                firstTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_IndexRevision"), new Object[]{firstRevision}); // NOI18N
                break;
            default:
                throw new IllegalArgumentException("Unknown diff type: " + mode); // NOI18N
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="right panel">
        switch (mode) {
            case HEAD_VS_WORKING_TREE:
            case INDEX_VS_WORKING_TREE:
                if (info.containsStatus(FileInformation.Status.NEW_HEAD_WORKING_TREE)) {
                    secondRevision = GitUtils.CURRENT;
                    if (originalFile != null) {
                        if (info.isRenamed()) {
                            secondTitle = loc.getString("MSG_DiffPanel_LocalRenamed"); //NOI18N
                        } else {
                            secondTitle = loc.getString("MSG_DiffPanel_LocalCopied"); //NOI18N
                        }
                    } else {
                        secondTitle = loc.getString("MSG_DiffPanel_LocalNew"); //NOI18N
                    }
                } else if (info.containsStatus(FileInformation.Status.REMOVED_HEAD_WORKING_TREE)) {
                    secondRevision = null;
                    secondTitle = loc.getString("MSG_DiffPanel_LocalDeleted"); // NOI18N
                } else {
                    secondRevision = GitUtils.CURRENT;
                    secondTitle = loc.getString("MSG_DiffPanel_LocalModified"); // NOI18N
                }
                break;
            case HEAD_VS_INDEX:
                if (info.containsStatus(FileInformation.Status.NEW_HEAD_INDEX)) {
                    secondRevision = GitUtils.INDEX;
                    if (originalFile != null) {
                        if (info.isRenamed()) {
                            secondTitle = loc.getString("MSG_DiffPanel_IndexRenamed"); //NOI18N
                        } else {
                            secondTitle = loc.getString("MSG_DiffPanel_IndexCopied"); //NOI18N
                        }
                    } else {
                        secondTitle = loc.getString("MSG_DiffPanel_IndexNew"); //NOI18N
                    }
                } else if (info.containsStatus(FileInformation.Status.REMOVED_HEAD_INDEX)) {
                    secondRevision = null;
                    secondTitle = loc.getString("MSG_DiffPanel_IndexDeleted"); // NOI18N
                } else {
                    secondRevision = GitUtils.INDEX;
                    secondTitle = loc.getString("MSG_DiffPanel_IndexModified"); // NOI18N
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown diff type: " + mode); // NOI18N
        }// </editor-fold>

        firstSource = new DiffStreamSource(originalFile == null || mode == Mode.INDEX_VS_WORKING_TREE ? baseFile : originalFile, baseFile, firstRevision, firstTitle);
        secondSource = new DiffStreamSource(baseFile, baseFile, secondRevision, secondTitle);
        title = "<html>" + info.annotateNameHtml(baseFile.getName()); // NOI18N
    }

    Setup (File file, Mode mode, final boolean forceNonEditable) {
        this.baseFile = file;
        switch (mode) {
            case HEAD_VS_INDEX:
                firstRevision = GitUtils.HEAD;
                secondRevision = GitUtils.INDEX;
                break;
            case HEAD_VS_WORKING_TREE:
                firstRevision = GitUtils.HEAD;
                secondRevision = GitUtils.CURRENT;
                break;
            case INDEX_VS_WORKING_TREE:
                firstRevision = GitUtils.INDEX;
                secondRevision = GitUtils.CURRENT;
                break;
            default:
                throw new IllegalStateException();
        }
        firstSource = new DiffStreamSource(baseFile, baseFile, firstRevision, firstRevision);
        // XXX delete when UndoAction works correctly
        secondSource = new DiffStreamSource(baseFile, baseFile, secondRevision, secondRevision) {
            @Override
            public boolean isEditable() {
                return !forceNonEditable && super.isEditable();
            }
        };
    }

    Setup (File file, Revision rev1, Revision rev2, GitFileNode.HistoryFileInformation fileInfo) {
        baseFile = file;
        firstRevision = rev1.getCommitId();
        secondRevision = rev2.getCommitId();
        StringBuilder sb = new StringBuilder(rev1.toString(true));
        File firstSourceBaseFile = baseFile;
        if (fileInfo != null && fileInfo.getOldPath() != null) {
            sb.append(" (").append(fileInfo.getOldPath()).append(")");
            firstSourceBaseFile = fileInfo.getOldFile();
        }
        firstSource = new DiffStreamSource(firstSourceBaseFile, baseFile, firstRevision, sb.toString());
        secondSource = new DiffStreamSource(baseFile, baseFile, secondRevision, rev2.toString(true));
    }

    public File getBaseFile() {
        return baseFile;
    }

    public void setView(DiffController view) {
        this.view = view;
    }

    public DiffController getView() {
        return view;
    }

    @Override
    public StreamSource getFirstSource() {
        return firstSource;
    }

    @Override
    public StreamSource getSecondSource() {
        return secondSource;
    }

    void setNode (DiffNode node) {
        assert this.node == null;
        this.node = node;
    }

    DiffNode getNode() {
        return node;
    }

    @Override
    public String toString() {
        return title;
    }

    /**
     * Loads data
     * @param group that carries shared state. Note that this group must not be executed later on.
     */
    void initSources() throws IOException {
        if (firstSource != null) firstSource.init();
        if (secondSource != null) secondSource.init();
    }

}
