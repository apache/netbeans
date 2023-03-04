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

package org.netbeans.modules.mercurial.ui.diff;

import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.DiffController;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.FileInformation;
import org.openide.util.NbBundle;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.text.MessageFormat;
import org.netbeans.modules.mercurial.FileStatus;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage.HgRevision;
import org.netbeans.modules.versioning.diff.AbstractDiffSetup;

/**
 * Represents on DIFF setup.
 *
 * @author Maros Sandor
 */
public final class Setup extends AbstractDiffSetup {

    /**
     * What was locally changed? The right pane contains local file.
     *
     * <p>Local addition, removal or change is displayed in
     * the right pane as addition, removal or change respectively
     * (i.e. not reversed as removal, addition or change).
     *
     * <pre>
     * diff from-BASE to-LOCAL
     * </pre>
     */
    public static final int DIFFTYPE_LOCAL     = 0;

    /**
     * What was remotely changed? The right pane contains remote file.
     *
     * <p>Remote addition, removal or change is displayed in
     * the right pane as addition, removal or change respectively
     * (i.e. not reversed as removal, addition or change).
     *
     * <pre>
     * diff from-BASE to-HEAD
     * </pre>
     */
    public static final int DIFFTYPE_REMOTE    = 1;

    /**
     * What was locally changed comparing to recent head?
     * The Right pane contains local file.
     *
     * <p> Local addition, removal or change is displayed in
     * the right pane as addition, removal or change respectively
     * (i.e. not reversed as removal, addition or change).
     *
     * <pre>
     * diff from-HEAD to-LOCAL
     * </pre>
     */
    public static final int DIFFTYPE_ALL       = 2;
    
    private final File      baseFile;

    private final HgRevision    firstRevision;
    private final HgRevision    secondRevision;
    private final FileInformation info;

    private DiffStreamSource    firstSource;
    private DiffStreamSource    secondSource;

    private DiffController      view;
    private DiffNode            node;

    private String    title;

    public Setup(File baseFile, String propertyName, int type) {
        this.baseFile = baseFile;
        info = Mercurial.getInstance().getFileStatusCache().getStatus(baseFile);
        int status = info.getStatus();
        FileStatus fileStatus = info.getStatus(null);
        
        ResourceBundle loc = NbBundle.getBundle(Setup.class);
        String firstTitle;
        String secondTitle;

        // the first source

        switch (type) {
            case DIFFTYPE_LOCAL:           

                // from-BASE

                if (match(status, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY
                | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
                    firstRevision = HgRevision.BASE;
                    File originalFile = null;
                    if (fileStatus != null && fileStatus.isCopied()) {
                        originalFile = fileStatus.getOriginalFile();
                    }
                    firstTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_BaseRevision"), new Object [] { //NOI18N
                        originalFile == null ? firstRevision.getRevisionNumber() : originalFile.getName() });
                } else if (match (status, FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
                    firstRevision = null;
                    firstTitle = NbBundle.getMessage(Setup.class, "LBL_Diff_NoLocalFile"); // NOI18N
                } else if (match(status, FileInformation.STATUS_VERSIONED_DELETEDLOCALLY
                | FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
                    firstRevision = HgRevision.BASE;
                    firstTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_BaseRevision"), new Object [] { firstRevision.getRevisionNumber() }); // NOI18N
                } else {
                    firstRevision = HgRevision.BASE;
                    firstTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_BaseRevision"), new Object [] { firstRevision.getRevisionNumber() }); // NOI18N
                }

                break;

            default:
                throw new IllegalArgumentException("Unknown diff type: " + type); // NOI18N
        }


        // the second source

        switch (type) {
            case DIFFTYPE_LOCAL:

                // to-LOCAL

                if (match(status, FileInformation.STATUS_VERSIONED_CONFLICT)) {
                    secondRevision = HgRevision.CURRENT;
                    secondTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_LocalConflict"), new Object [] { secondRevision.getRevisionNumber() }); // NOI18N
                } else if (match(status, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY
                | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
                    secondRevision = HgRevision.CURRENT;
                    if (fileStatus != null && fileStatus.isCopied()) {
                        if (fileStatus.getOriginalFile() != null && !fileStatus.getOriginalFile().exists()) {
                            if (fileStatus.getOriginalFile().getParentFile().getAbsolutePath()
                                    .equals(baseFile.getParentFile().getAbsolutePath())) {
                                secondTitle = loc.getString("MSG_DiffPanel_LocalRenamed"); // NOI18N
                            } else {
                                secondTitle = loc.getString("MSG_DiffPanel_LocalMoved"); // NOI18N
                            }
                        } else {
                            secondTitle = loc.getString("MSG_DiffPanel_LocalCopied"); // NOI18N
                        }
                    } else {
                        secondTitle = loc.getString("MSG_DiffPanel_LocalNew"); // NOI18N
                    }
                } else if (match (status, FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
                    secondRevision = null;
                    secondTitle = NbBundle.getMessage(Setup.class, "LBL_Diff_NoLocalFile"); // NOI18N
                } else if (match(status, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
                    secondRevision = null;
                    secondTitle = loc.getString("MSG_DiffPanel_LocalRemoved"); // NOI18N
                } else if (match(status, FileInformation.STATUS_VERSIONED_DELETEDLOCALLY)) {
                    secondRevision = null;
                    secondTitle = loc.getString("MSG_DiffPanel_LocalDeleted"); // NOI18N
                } else {
                    secondRevision = HgRevision.CURRENT;
                    secondTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_LocalModified"), new Object [] { secondRevision.getRevisionNumber() }); // NOI18N
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown diff type: " + type); // NOI18N
        }

        firstSource = new DiffStreamSource(baseFile, baseFile, firstRevision, firstTitle);
        secondSource = new DiffStreamSource(baseFile, baseFile, secondRevision, secondTitle);
        title = "<html>" + Mercurial.getInstance().getMercurialAnnotator().annotateNameHtml(baseFile, info); // NOI18N
    }

    /**
     * Text file setup for arbitrary revisions.
     * @param firstRevision first revision or <code>null</code> for inital.
     * @param secondRevision second revision
     */
    public Setup(File baseFile, HgRevision firstRevision, HgRevision secondRevision, FileInformation info, final boolean forceNonEditable) {
        title = baseFile.getName();
        this.baseFile = baseFile;
        this.firstRevision = firstRevision;
        this.secondRevision = secondRevision;
        this.info = info;
        File firstSourceBaseFile = baseFile;
        if (info != null && info.getStatus(null) != null && info.getStatus(null).getOriginalFile() != null) {
            firstSourceBaseFile = info.getStatus(null).getOriginalFile();
        }
        firstSource = new DiffStreamSource(firstSourceBaseFile, firstSourceBaseFile, firstRevision, 
                firstRevision.toString());
        // XXX delete when UndoAction works correctly
        secondSource = new DiffStreamSource(baseFile, baseFile, secondRevision, secondRevision.toString()) {
            @Override
            public boolean isEditable() {
                return !forceNonEditable && super.isEditable();
            }
        };
    }

    public File getBaseFile() {
        return baseFile;
    }

    public FileInformation getInfo() {
        return info;
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

    void setNode(DiffNode node) {
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

    private static boolean match(int status, int mask) {
        return (status & mask) != 0;
    }
}
