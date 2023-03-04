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

package org.netbeans.modules.subversion.ui.diff;

import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.DiffController;
import org.netbeans.modules.subversion.*;
import org.openide.util.NbBundle;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.text.MessageFormat;
import org.netbeans.modules.versioning.diff.AbstractDiffSetup;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

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
    
    public static final String REVISION_PRISTINE = "PRISTINE"; // NOI18N
    public static final String REVISION_BASE = "BASE"; // NOI18N
    public static final String REVISION_CURRENT = "LOCAL"; // NOI18N
    public static final String REVISION_HEAD    = "HEAD"; // NOI18N
    
    private final File      baseFile;
    
    /**
     * Name of the file's property if the setup represents a property diff setup, null otherwise. 
     */
    private final String    propertyName;
    
    private String    firstRevision;
    private final String    secondRevision;
    private FileInformation info;

    private final StreamSource    firstSource;
    private final StreamSource    secondSource;

    private DiffController      view;
    private DiffNode            node;

    private String    title;

    public Setup(File baseFile, String propertyName, int type) {
        this.baseFile = baseFile;
        this.propertyName = propertyName;
        info = Subversion.getInstance().getStatusCache().getStatus(baseFile);
        int status = info.getStatus();
        
        ResourceBundle loc = NbBundle.getBundle(Setup.class);
        String firstTitle;
        String secondTitle;

        // the first source

        switch (type) {
            case DIFFTYPE_LOCAL:           
            case DIFFTYPE_REMOTE:

                // from-BASE
                if (match(status, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
                    firstRevision = null;
                    firstTitle = loc.getString("MSG_DiffPanel_NoBaseRevision");
                } else if (match (status, FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
                    firstRevision = null;
                    firstTitle = NbBundle.getMessage(Setup.class, "LBL_Diff_NoLocalFile"); // NOI18N
                } else if (match(status, FileInformation.STATUS_VERSIONED_DELETEDLOCALLY
                | FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
                    firstRevision = REVISION_BASE;
                    firstTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_BaseRevision"), new Object [] { firstRevision });
                } else {
                    firstRevision = REVISION_BASE;
                    firstTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_BaseRevision"), new Object [] { firstRevision });
                }

                break;

            case DIFFTYPE_ALL:

                // from-HEAD

                if (match (status, FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
                    firstRevision = REVISION_HEAD;
                    firstTitle = loc.getString("MSG_DiffPanel_RemoteNew");
                } else if (match (status, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY
                                 | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
                    firstRevision = null;
                    firstTitle = loc.getString("MSG_DiffPanel_NoBaseRevision");
                } else if (match(status, FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY)) {
                    firstRevision = null;
                    firstTitle = loc.getString("MSG_DiffPanel_RemoteDeleted");
                } else {
                    firstRevision = REVISION_HEAD;
                    firstTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_RemoteModified"), new Object [] { firstRevision });
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown diff type: " + type); // NOI18N
        }


        // the second source

        switch (type) {
            case DIFFTYPE_LOCAL:
            case DIFFTYPE_ALL:

                // to-LOCAL

                if (match(status, FileInformation.STATUS_VERSIONED_CONFLICT)) {
                    secondRevision = REVISION_CURRENT;
                    secondTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_LocalConflict"), new Object [] { secondRevision });
                } else if (match(status, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY
                | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
                    secondRevision = REVISION_CURRENT;
                    secondTitle = loc.getString("MSG_DiffPanel_LocalNew");
                } else if (match (status, FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
                    secondRevision = null;
                    secondTitle = NbBundle.getMessage(Setup.class, "LBL_Diff_NoLocalFile"); // NOI18N
                } else if (match(status, FileInformation.STATUS_VERSIONED_DELETEDLOCALLY
                | FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
                    secondRevision = null;
                    secondTitle = loc.getString("MSG_DiffPanel_LocalDeleted");
                } else {
                    secondRevision = REVISION_CURRENT;
                    secondTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_LocalModified"), new Object [] { secondRevision });
                }
                break;

            case DIFFTYPE_REMOTE:

                // to-HEAD

                if (match(status, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY
                | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
                    secondRevision = null;
                    secondTitle = loc.getString("MSG_DiffPanel_LocalNew");
                } else if (match(status, FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
                    secondRevision = REVISION_HEAD;
                    secondTitle = loc.getString("MSG_DiffPanel_RemoteNew");
                } else if (match(status, FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY)) {
                    secondRevision = null;
                    secondTitle = loc.getString("MSG_DiffPanel_RemoteDeleted");
                } else {
                    secondRevision = REVISION_HEAD;
                    secondTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_RemoteModified"), new Object [] { secondRevision });
                }            
                break;

            default:
                throw new IllegalArgumentException("Unknown diff type: " + type); // NOI18N
        }
        
        if (propertyName != null){
            if (REVISION_HEAD.equals(firstRevision)) {
                firstRevision = REVISION_BASE;
            }
        }

        firstSource = new DiffStreamSource(baseFile, propertyName, firstRevision, firstTitle);
        secondSource = new DiffStreamSource(baseFile, propertyName, secondRevision, secondTitle);
        title = "<html>" + Subversion.getInstance().getAnnotator().annotateNameHtml(baseFile, info); // NOI18N
    }

    /**
     * Text file setup for arbitrary revisions.
     * @param firstRevision first revision or <code>null</code> for inital.
     * @param secondRevision second revision
     */
    public Setup(File baseFile, String firstRevision, String secondRevision, final boolean forceNonEditable) {
        this.baseFile = baseFile;
        this.propertyName = null;
        this.firstRevision = firstRevision;
        this.secondRevision = secondRevision;
        title = baseFile.getName();
        firstSource = new DiffStreamSource(baseFile, propertyName, firstRevision, firstRevision);
        // XXX delete when UndoAction works correctly
        secondSource = new DiffStreamSource(baseFile, propertyName, secondRevision, secondRevision) {
            @Override
            public boolean isEditable() {
                return !forceNonEditable && super.isEditable();
            }
        };
    }

    /**
     * Text file setup for arbitrary revisions.
     * @param firstRevision first revision
     * @param secondRevision second revision
     */
    public Setup (File baseFile, SVNUrl repoUrl, SVNUrl firstFileUrl, String firstRevision, String firstTitle,
            SVNUrl secondFileUrl, String secondRevision, String secondTitle, FileInformation info) {
        this.baseFile = baseFile;
        this.propertyName = null;
        this.firstRevision = firstRevision;
        this.secondRevision = secondRevision;
        this.info = info;
        title = baseFile.getName();
        firstSource = new org.netbeans.modules.subversion.ui.history.DiffStreamSource(baseFile, 
                repoUrl, firstFileUrl, firstRevision, firstTitle);
        if (Setup.REVISION_CURRENT.equals(secondRevision)) {
            secondSource = new DiffStreamSource(baseFile, propertyName, secondRevision, secondTitle);
        } else {
            secondSource = new org.netbeans.modules.subversion.ui.history.DiffStreamSource(baseFile, 
                    repoUrl, secondFileUrl, secondRevision, secondTitle);
        }
    }

    /**
     * Local file vs HEAD
     * @param baseFile
     * @param status remote status of the file
     */
    public Setup(File baseFile, ISVNStatus status) {
        this.baseFile = baseFile;
        this.propertyName = null;
        this.secondRevision = null;
        title = baseFile.getName();
        String headTitle;
        ResourceBundle loc = NbBundle.getBundle(Setup.class);
        if (status.getRepositoryTextStatus().equals(SVNStatusKind.ADDED)) {
                    firstRevision = REVISION_HEAD;
                    headTitle = loc.getString("MSG_DiffPanel_RemoteNew");
                } else if (status.getRepositoryTextStatus().equals(SVNStatusKind.DELETED)) {
                    firstRevision = null;
                    headTitle = loc.getString("MSG_DiffPanel_RemoteDeleted");
                } else if (status.getRepositoryTextStatus().equals(SVNStatusKind.MODIFIED)) {
                    firstRevision = REVISION_HEAD;
                    headTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_RemoteModified"), new Object [] { firstRevision });
                } else {
                    firstRevision = REVISION_HEAD;
                    headTitle = REVISION_HEAD.toString();
                }
        firstSource = new DiffStreamSource(baseFile, propertyName, REVISION_HEAD, headTitle);
        secondSource = new DiffStreamSource(baseFile, propertyName, REVISION_CURRENT, REVISION_CURRENT);
    }

    public String getPropertyName() {
        return propertyName;
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

    public void setNode(DiffNode node) {
        this.node = node;
    }

    public DiffNode getNode() {
        return node;
    }

    @Override
    public String toString() {
        return title;
    }

    /**
     * Loads data over network
     */
    void initSources() throws IOException {
        if (firstSource instanceof DiffStreamSource) {
            ((DiffStreamSource) firstSource).init();
        } else if (firstSource instanceof org.netbeans.modules.subversion.ui.history.DiffStreamSource) {
            ((org.netbeans.modules.subversion.ui.history.DiffStreamSource) firstSource).init();
        }
        if (secondSource instanceof DiffStreamSource) {
            ((DiffStreamSource) secondSource).init();
        } else if (secondSource instanceof org.netbeans.modules.subversion.ui.history.DiffStreamSource) {
            ((org.netbeans.modules.subversion.ui.history.DiffStreamSource) secondSource).init();
        }
    }

    private static boolean match(int status, int mask) {
        return (status & mask) != 0;
    }
}
