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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.ui.history;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.diff.DiffStreamSource;
import org.netbeans.modules.git.ui.history.RepositoryRevision.Event;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * DiffResultsView not showing differences but rather fixed line numbers.
 * Currently used by bugtracking to display revisions of a file and to fix the view on a given line number.
 * 
 * @author Ondra Vrabec
 */
final class DiffResultsViewForLine extends DiffResultsView {
    private int lineNumber;
    
    public DiffResultsViewForLine(final SearchHistoryPanel parent, final List<RepositoryRevision> results, final int lineNumber) {
        super(parent, results);
        this.lineNumber = Math.max(lineNumber - 1, 0);
        setButtonLabels();
    }

    @Override
    protected void showRevisionDiff(RepositoryRevision.Event rev, boolean showLastDifference) {
        if (rev.getFile() == null) return;
        showDiff(rev.getLogInfoHeader().getRepositoryRoot(), null, rev, showLastDifference);
    }

    @Override
    protected GitProgressSupport createShowDiffTask (Event revision1, Event revision2, boolean showLastDifference) {
        if (revision1 == null) {
            return new ShowDiffTask(revision2.getFile(), revision2.getLogInfoHeader().getLog().getRevision(), showLastDifference);
        } else {
            return super.createShowDiffTask(revision1, revision2, showLastDifference);
        }
    }

    @Override
    void onNextButton() {
        if (++currentIndex >= treeView.getRowCount()) currentIndex = 0;
        setDiffIndex(currentIndex, false);
    }

    @Override
    void onPrevButton() {
        if (--currentIndex < 0) currentIndex = treeView.getRowCount() - 1;
        setDiffIndex(currentIndex, true);
    }

    private void setButtonLabels() {
        parent.bNext.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DiffResultsViewForLine.class, "ACSN_NextRevision")); // NOI18N
        parent.bNext.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DiffResultsViewForLine.class, "ACSD_NextRevision")); // NOI18N
        parent.bNext.setToolTipText(NbBundle.getMessage(DiffResultsViewForLine.class, "ACSD_NextRevision")); // NOI18N
        parent.bPrev.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DiffResultsViewForLine.class, "ACSN_PrevRevision")); // NOI18N
        parent.bPrev.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DiffResultsViewForLine.class, "ACSD_PrevRevision")); // NOI18N
        parent.bPrev.setToolTipText(NbBundle.getMessage(DiffResultsViewForLine.class, "ACSD_PrevRevision")); // NOI18N
    }

    private class ShowDiffTask extends GitProgressSupport {
        private final File file;
        private final String revision;

        public ShowDiffTask (File file, String revision, boolean showLastDifference) {
            this.file = file;
            this.revision = revision;
        }

        @Override
        public void perform () {
            showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_LoadingDiff")); //NOI18N
            final DiffStreamSource leftSource = new DiffStreamSource(file, file, revision, revision);
            final LocalFileDiffStreamSource rightSource = new LocalFileDiffStreamSource(file, true);

            // it's enqueued at ClientRuntime queue and does not return until previous request handled
            leftSource.getMIMEType();  // triggers s1.init()
            if (isCanceled()) {
                showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_NoRevisions")); // NOI18N
                return;
            }

            rightSource.getMIMEType();
            if (isCanceled()) {
                showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_NoRevisions")); // NOI18N
                return;
            }

            if (currentTask != this) return;

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (isCanceled()) {
                            showDiffError(NbBundle.getMessage(DiffResultsView.class, "MSG_DiffPanel_NoRevisions")); // NOI18N
                            return;
                        }
                        final DiffController view = DiffController.createEnhanced(leftSource, rightSource);
                        int leftMaxLineNumber = getLastLineIndex(leftSource);
                        int rightMaxLineNumber = getLastLineIndex(rightSource);
                        if (currentTask == ShowDiffTask.this) {
                            currentDiff = view;
                            setBottomComponent(currentDiff.getJComponent());
                            if (leftMaxLineNumber != -1) {
                                setLocation(Math.min(leftMaxLineNumber, lineNumber), false);
                            }
                            if (rightMaxLineNumber != -1) {
                                setLocation(Math.min(rightMaxLineNumber, lineNumber), true);
                            }
                            parent.refreshComponents(false);
                        }
                    } catch (IOException e) {
                        LOG.log(Level.INFO, null, e);
                    }
                }
            });
        }
    }

    private int getLastLineIndex (final StreamSource ss) {
        String mimeType = ss.getMIMEType();
        if (mimeType == null || !mimeType.startsWith("text/")) {
            LOG.log(Level.INFO, "Wrong mime type");
            return 0;
        }
        EditorKit kit = CloneableEditorSupport.getEditorKit(mimeType);
        if (kit == null) {
            LOG.log(Level.WARNING, "No editor kit available");
            return 0;
        }
        Document sdoc = getSourceDocument(ss);
        Document doc = sdoc != null ? sdoc : kit.createDefaultDocument();
        StyledDocument styledDoc;
        if ((doc instanceof StyledDocument)) {
            styledDoc = (StyledDocument) doc;
        } else {
            styledDoc = new DefaultStyledDocument(new StyleContext());
            kit = new StyledEditorKit();
        }
        if (sdoc == null) {
            Reader r = null;
            try {
                r = ss.createReader();
                if (r != null) {
                    try {
                        kit.read(r, styledDoc, 0);
                    } catch (javax.swing.text.BadLocationException e) {
                        throw new IOException("Can not locate the beginning of the document."); // NOI18N
                    }
                }
            } catch (IOException ex) {
                LOG.log(Level.INFO, null, ex);
            } finally {
                try {
                    if (r != null) {
                        r.close();
                    }
                } catch (IOException ex) {
                    LOG.log(Level.INFO, null, ex);
                }
            }
        }
        return org.openide.text.NbDocument.findLineNumber(styledDoc, styledDoc.getEndPosition().getOffset());
    }

    private Document getSourceDocument(StreamSource ss) {
        Document sdoc = null;
        FileObject fo = ss.getLookup().lookup(FileObject.class);
        if (fo != null) {
            try {
                DataObject dao = DataObject.find(fo);
                if (dao.getPrimaryFile() == fo) {
                    EditorCookie ec = dao.getCookie(EditorCookie.class);
                    if (ec != null) {
                        sdoc = ec.openDocument();
                    }
                }
            } catch (Exception e) {
                // fallback to other means of obtaining the source
            }
        } else {
            sdoc = ss.getLookup().lookup(Document.class);
        }
        return sdoc;
    }

    private void setLocation (final int lineNumber, final boolean showLineInLocal) {
        if (currentDiff == null) {
            return;
        }
        if (showLineInLocal) {
            currentDiff.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.LineNumber, lineNumber);
        } else {
            currentDiff.getJComponent().putClientProperty("diff.smartScrollDisabled", Boolean.TRUE);
            currentDiff.setLocation(DiffController.DiffPane.Base, DiffController.LocationType.LineNumber, lineNumber);
        }
    }

    private static class LocalFileDiffStreamSource extends StreamSource {

        private final FileObject    fileObject;
        private final boolean       isRight;
        private File file;
        private String mimeType;

        public LocalFileDiffStreamSource (File file, boolean isRight) {
            this.file = FileUtil.normalizeFile(file);
            this.fileObject = FileUtil.toFileObject(this.file);
            this.isRight = isRight;
        }

        @Override
        public boolean isEditable() {
            return isRight && fileObject != null && fileObject.canWrite();
        }

        @Override
        public Lookup getLookup() {
            if (fileObject != null) {
                return Lookups.fixed(fileObject);
            } else {
                return Lookups.fixed();
            }
        }

        @Override
        public String getName() {
            return file.getName();
        }

        @Override
        public String getTitle() {
            return fileObject != null ? FileUtil.getFileDisplayName(fileObject) : file.getAbsolutePath();
        }

        @Override
        public String getMIMEType() {
            return mimeType = fileObject != null && fileObject.isValid() ? GitUtils.getMimeType(file) : null;
        }

        @Override
        public Reader createReader() throws IOException {
            if (mimeType == null || !mimeType.startsWith("text/")) {
                return null;
            } else {
                return Utils.createReader(file);
            }
        }

        @Override
        public Writer createWriter(Difference[] conflicts) throws IOException {
            return null;
        }
    }
}
