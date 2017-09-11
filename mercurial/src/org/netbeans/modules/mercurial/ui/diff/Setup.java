/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
                throw new IllegalArgumentException("Unknow diff type: " + type); // NOI18N
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
                throw new IllegalArgumentException("Unknow diff type: " + type); // NOI18N
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
