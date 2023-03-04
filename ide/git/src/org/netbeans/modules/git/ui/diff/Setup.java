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
