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

package org.netbeans.modules.subversion.ui.blame;

import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.openide.nodes.Node;
import org.openide.cookies.EditorCookie;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.windows.WindowManager;
import org.tigris.subversion.svnclientadapter.*;
import javax.swing.*;
import java.io.File;
import java.util.*;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.openide.text.NbDocument;
import org.openide.util.Mutex;
import org.openide.windows.TopComponent;

/**
 *
 * @author Maros Sandor
 */
@NbBundle.Messages({
    "CTL_MenuItem_HideAnnotations=Hide A&nnotations",
    "CTL_MenuItem_HideAnnotations_Context=Hide A&nnotations",
    "CTL_MenuItem_HideAnnotations_Context_Multiple=Hide A&nnotations",
    "CTL_MenuItem_HideAnnotations_Projects=Hide A&nnotations",
    "CTL_MenuItem_ShowAnnotations=Show A&nnotations",
    "CTL_MenuItem_ShowAnnotations_Context=Show A&nnotations",
    "CTL_MenuItem_ShowAnnotations_Context_Multiple=Show A&nnotations",
    "CTL_MenuItem_ShowAnnotations_Projects=Show A&nnotations"
})
public class BlameAction extends ContextAction {
    private static final String ICON_RESOURCE = "org/netbeans/modules/subversion/resources/icons/annotate.png"; //NOI18N
    
    public BlameAction () {
        super(ICON_RESOURCE);
    }
    
    @Override
    protected String getBaseName(Node [] activatedNodes) {
        if (visible(activatedNodes)) {
            return "CTL_MenuItem_HideAnnotations";  // NOI18N
        } else {
            return "CTL_MenuItem_ShowAnnotations"; // NOI18N
        }
    }

    @Override
    public boolean enable(Node[] nodes) {
        return super.enable(nodes) && activatedEditorCookie(nodes) != null;
    }

    @Override
    protected String iconResource() {
        return ICON_RESOURCE;
    }

    @Override
    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_IN_REPOSITORY;
    }

    @Override
    protected int getDirectoryEnabledStatus() {
        return 0;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected void performContextAction(Node[] nodes) {        
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }        
        if (visible(nodes)) {
            JEditorPane pane = activatedEditorPane(nodes);
            AnnotationBarManager.hideAnnotationBar(pane);
        } else {
            EditorCookie ec = activatedEditorCookie(nodes);
            if (ec == null) return;
            
            final File file = activatedFile(nodes);

            JEditorPane[] panes = ec.getOpenedPanes();
            if (panes == null) {
                ec.open();
                panes = ec.getOpenedPanes();
            }
            if (panes == null) {
                return;
            }
            final JEditorPane currentPane = panes[0];
            showAnnotations(currentPane, file, null);
        }
    }

    public static void showAnnotations(JEditorPane currentPane, File file, SVNRevision revision) {
        final AnnotationBar ab = AnnotationBarManager.showAnnotationBar(currentPane);
        ab.setAnnotationMessage(NbBundle.getMessage(BlameAction.class, "CTL_AnnotationSubstitute")); // NOI18N;

        SVNUrl repository;
        try {
            repository = SvnUtils.getRepositoryRootUrl(file);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }

        if (revision == null) {
            ISVNStatus status = Subversion.getInstance().getStatusCache().getStatus(file).getEntry(file);
            if (status == null || status.getRevision() == null) {
                // status could not be loaded or we have a symlink, do not continnue
                return;
            }
            ab.setSVNClienListener(new SVNClientListener(status.getRevision().getNumber(), repository, file, ab));
        }

        TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, currentPane);
        tc.requestActive();
        computeAnnotations(repository, file, ab, revision);
    }

    private static void computeAnnotations(SVNUrl repository, final File file, final AnnotationBar ab, final SVNRevision revision) {
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repository);
        SvnProgressSupport support = new SvnProgressSupport() {
            @Override
            public void perform() {                    
                computeAnnotations(file, this, ab, revision);
            }
        };
        support.start(rp, repository, NbBundle.getMessage(BlameAction.class, "MSG_Annotation_Progress")); // NOI18N        
    }
            
    
    private static void computeAnnotations(File file, SvnProgressSupport progress, AnnotationBar ab, SVNRevision revision) {
        SvnClient client;
        try {
            client = Subversion.getInstance().getClient(file, progress);
        } catch (SVNClientException ex) {
            ab.setAnnotationMessage(NbBundle.getMessage(BlameAction.class, "CTL_AnnotationFailed")); // NOI18N;
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }
        if (revision == null) {
            revision = SVNRevision.BASE;
        } else {
            // showing annotations from past, the referenced file differs from the one being displayed
            ab.setReferencedFile(file);
        }

        ISVNAnnotations annotations;
        try {
            annotations = client.annotate(file, new SVNRevision.Number(1), revision);
        } catch (SVNClientException e) {
            ab.setAnnotationMessage(NbBundle.getMessage(BlameAction.class, "CTL_AnnotationFailed")); // NOI18N;
            SvnClientExceptionHandler.notifyException(e, true, true);
            return;
        }
        if (progress.isCanceled()) {
            ab.setAnnotationMessage(NbBundle.getMessage(BlameAction.class, "CTL_AnnotationFailed")); // NOI18N;
            return;
        }
        AnnotateLine [] lines = toAnnotateLines(annotations);
        ab.annotationLines(file, Arrays.asList(lines));
        
        // fetch log messages
        ISVNLogMessage [] logs;
        try {
            logs = client.getLogMessages(file, revision, new SVNRevision.Number(1), revision, false, false, 0, false);
        } catch (SVNClientException e) {
            progress.annotate(e);
            return;
        }
        if (progress.isCanceled()) {
            return;
        }
        fillCommitMessages(lines, logs);
    }

    private static void fillCommitMessages(AnnotateLine [] annotations, ISVNLogMessage[] logs) {
        long lowestRevisionNumber = Long.MAX_VALUE;
        for (int i = 0; i < annotations.length; i++) {
            AnnotateLine annotation = annotations[i];
            for (int j = 0; j < logs.length; j++) {
                ISVNLogMessage log = logs[j];
                if (log.getRevision().getNumber() < lowestRevisionNumber) {
                    lowestRevisionNumber = log.getRevision().getNumber(); 
                }
                if (annotation.getRevision().equals(log.getRevision().toString())) {
                    annotation.setDate(log.getDate());
                    annotation.setCommitMessage(log.getMessage());
                }
            }
        }
        String lowestRev = Long.toString(lowestRevisionNumber);
        for (int i = 0; i < annotations.length; i++) {
            AnnotateLine annotation = annotations[i];
            annotation.setCanBeRolledBack(!annotation.getRevision().equals(lowestRev));
        }
    }

    private static AnnotateLine [] toAnnotateLines(ISVNAnnotations annotations) {
        AnnotateLine [] lines = new AnnotateLine[annotations.numberOfLines()];
        int n = annotations.numberOfLines();
        for (int i = 0; i < n; i++) {
            lines[i] = new AnnotateLine();
            lines[i].setAuthor(annotations.getAuthor(i));
            lines[i].setContent(annotations.getLine(i));
            lines[i].setLineNum(i + 1);
            lines[i].setRevision(Long.toString(annotations.getRevision(i)));
            lines[i].setDate(annotations.getChanged(i));
        }
        return lines;
    }

    /**
     * @param nodes or null (then taken from windowsystem, it may be wrong on editor tabs #66700).
     */
    public boolean visible(Node[] nodes) {
        JEditorPane currentPane = activatedEditorPane(nodes);
        return AnnotationBarManager.annotationBarVisible(currentPane);
    }

    /**
     * @return active editor pane or null if selected node
     * does not have any or more nodes selected.
     */
    private JEditorPane activatedEditorPane(Node[] nodes) {
        final EditorCookie ec = activatedEditorCookie(nodes);
        if (ec != null) {
            return Mutex.EVENT.readAccess(new Mutex.Action<JEditorPane>() {
                @Override
                public JEditorPane run () {
                    return NbDocument.findRecentEditorPane(ec);
                }
            });
        }
        return null;
    }

    private EditorCookie activatedEditorCookie(Node[] nodes) {
        if (nodes == null) {
            nodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
        }
        if (nodes.length == 1) {
            Node node = nodes[0];
            return (EditorCookie) node.getCookie(EditorCookie.class);
        }
        return null;
    }

    private File activatedFile(Node[] nodes) {
        if (nodes.length == 1) {
            Node node = nodes[0];
            DataObject dobj = (DataObject) node.getCookie(DataObject.class);
            if (dobj != null) {
                FileObject fo = dobj.getPrimaryFile();
                return FileUtil.toFile(fo);
            }
        }
        return null;
    }

    private static class SVNClientListener implements ISVNNotifyListener {
        
        private final SVNUrl repository;
        private final File file;
        private final AnnotationBar ab;
        
        private long revision = -1;
        
        private File notifiedFile = null;
        
        public SVNClientListener(long revision, SVNUrl repository, final File file, final AnnotationBar ab) {
            this.revision = revision;
            this.repository = repository;
            this.ab = ab;
            this.file = file;
        }                
        
        @Override
        public void setCommand(int arg0) {            
            // do nothing
        }

        @Override
        public void logCommandLine(String arg0) {
            // do nothing
        }

        @Override
        public void logMessage(String arg0) {
            // do nothing
        }

        @Override
        public void logError(String arg0) {
            // do nothing
        }

        @Override
        public void logRevision(long newRevision, String path) {
            if(notifiedFile == null) {
                return;
            }
            if(notifiedFile.getAbsolutePath().equals(file.getAbsolutePath()) && revision != newRevision) {
                computeAnnotations(repository, file, ab, SVNRevision.BASE);
                revision = newRevision;
                notifiedFile = null;
            }
        }

        @Override
        public void logCompleted(String arg0) {
            // do nothing
        }

        @Override
        public void onNotify(File file, SVNNodeKind nodeKind) {
            notifiedFile = file;
        }        
    }
    
}
