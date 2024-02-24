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
package org.netbeans.modules.mercurial.ui.annotate;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.FileStatus;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Annotate action for mercurial: 
 * hg annotate - show changeset information per file line 
 * 
 * @author John Rice
 */
@NbBundle.Messages({
    "CTL_MenuItem_ShowAnnotations=Show A&nnotations",
    "CTL_MenuItem_HideAnnotations=Hide A&nnotations"
})
public class AnnotateAction extends ContextAction {
    public static final String ICON_RESOURCE = "org/netbeans/modules/mercurial/resources/icons/annotate.png"; //NOI18N
    
    public AnnotateAction () {
        super(ICON_RESOURCE);
    }
    
    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        if(!HgUtils.isFromHgRepository(context)) {
            return false;
        }

        if (context.getRootFiles().size() > 0 && activatedEditorCookie(nodes) != null) {
            FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
            File file = activatedFile(nodes);
            if (file == null) {
                return false;
            }
            FileInformation info  = cache.getCachedStatus(file);
            if(info != null) {
                int status = info.getStatus();
                if (status == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY ||
                    status == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
                    return false;
                } else {
                    return true;
                }
            } else {
                // XXX won't work properly when staus not chached yet. we should at least force a cahce.refresh
                // at this point
                return true;
            }
        } else {
            return false;
        } 
    } 

    @Override
    protected String getBaseName(Node[] nodes) {
        return visible(nodes) ? "CTL_MenuItem_HideAnnotations" : "CTL_MenuItem_ShowAnnotations"; //NOI18N
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }

    @Override
    protected void performContextAction(Node[] nodes) {
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
            showAnnotations(currentPane, file, null, true);
        }
    }

    public static void showAnnotations(JEditorPane currentPane, final File file, final String revision) {
        showAnnotations(currentPane, file, revision, true);
    }
    
    static void showAnnotations(JEditorPane currentPane, final File file, final String revision, boolean requestActive) {
        if (currentPane == null || file == null) {
            return;
        }
        if (requestActive) {
            TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, currentPane);
            tc.requestActive();
        }

        final AnnotationBar ab = AnnotationBarManager.showAnnotationBar(currentPane);
        ab.setAnnotationMessage(NbBundle.getMessage(AnnotateAction.class, "CTL_AnnotationSubstitute")); // NOI18N;

        final File repository = Mercurial.getInstance().getRepositoryRoot(file);
        if (repository == null) {
            return;
        }

        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                File annotatedFile = file;
                FileStatus st = Mercurial.getInstance().getFileStatusCache().getStatus(file).getStatus(null);
                if (st != null && st.isCopied() && st.getOriginalFile() != null) {
                    annotatedFile = st.getOriginalFile();
                    ab.setReferencedFile(annotatedFile);
                }
                if (revision != null) {
                    // showing annotations from past, the referenced file differs from the one being displayed
                    ab.setReferencedFile(annotatedFile);
                }
                OutputLogger logger = getLogger();
                logger.outputInRed(
                        NbBundle.getMessage(AnnotateAction.class,
                        "MSG_ANNOTATE_TITLE")); // NOI18N
                logger.outputInRed(
                        NbBundle.getMessage(AnnotateAction.class,
                        "MSG_ANNOTATE_TITLE_SEP")); // NOI18N
                computeAnnotations(repository, annotatedFile, this, ab, revision);
                logger.output("\t" + file.getAbsolutePath()); // NOI18N
                logger.outputInRed(
                        NbBundle.getMessage(AnnotateAction.class,
                        "MSG_ANNOTATE_DONE")); // NOI18N
            }
        };
        support.start(rp, repository, NbBundle.getMessage(AnnotateAction.class, "MSG_Annotation_Progress")); // NOI18N
    }

    private static void computeAnnotations(File repository, File file, HgProgressSupport progress, AnnotationBar ab, String revision) {
        List<String> list = null;
        try {
             list = HgCommand.doAnnotate(repository, file, revision, progress.getLogger());
        } catch (HgException.HgCommandCanceledException ex) {
            // canceled by user, do nothing
        } catch (HgException ex) {
            HgUtils.notifyException(ex);
        }
        if (progress.isCanceled()) {
            ab.setAnnotationMessage(NbBundle.getMessage(AnnotateAction.class, "CTL_AnnotationFailed")); // NOI18N;
            return;
        }
        if (list == null) {
            ab.setAnnotationMessage(NbBundle.getMessage(AnnotateAction.class, "CTL_AnnotationFailed")); // NOI18N;
            return;
        }
        AnnotateLine [] lines = toAnnotateLines(list);
        List<String> revisions = getRevisionNumbers(lines);
        HgLogMessage initialRevision = null;
        HgLogMessage [] logs = new HgLogMessage[0];
        if (!revisions.isEmpty()) {
            logs = HgCommand.getLogMessages(repository, Collections.singleton(file), 
                    revisions.get(0), "0", true, false, false, 1, Collections.<String>emptyList(), OutputLogger.getLogger(null), true);
            if (logs.length == 1) {
                initialRevision = logs[0];
            }
            logs = HgCommand.getRevisionInfo(repository, revisions, progress.getLogger());
            if (logs.length != revisions.size()) {
                Logger.getLogger(AnnotateAction.class.getName()).log(Level.WARNING, "Missing some of the requested revisions: {0}", revisions);
            }
        }
        if (progress.isCanceled()) {
            return;
        }
        if (logs == null) return;
        fillCommitMessages(lines, logs, initialRevision);
        ab.setAnnotatedRevision(revision);
        ab.annotationLines(file, Arrays.asList(lines));
    }

    private static List<String> getRevisionNumbers (AnnotateLine[] lines) {
        Set<String> revisions = new HashSet<String>(lines.length);
        for (AnnotateLine line : lines) {
            if (!(line instanceof FakeAnnotationLine)) {
                String revision = line.getRevision();
                try {
                    Long.parseLong(revision);
                    revisions.add(revision);
                } catch (NumberFormatException ex) {
                    // probably a fake item or a non-existent revision
                }
            }
        }
        List<String> retval = new ArrayList<String>(revisions);
        Collections.sort(retval);
        return retval;
    }

    private static void fillCommitMessages(AnnotateLine [] annotations, HgLogMessage [] logs, HgLogMessage initialRevision) {
        for (int i = 0; i < annotations.length; i++) {
            AnnotateLine annotation = annotations[i];
            if (annotation == null) {
                Mercurial.LOG.log(Level.WARNING, "AnnotateAction: annotation {0} of {1} is null", new Object[]{i, annotations.length}); //NOI18N
                continue;
            }
            for (int j = 0; j < logs.length; j++) {
                HgLogMessage log = logs[j];
                if (log == null) {
                    Mercurial.LOG.log(Level.WARNING, "AnnotateAction: log {0} of {1} is null", new Object[]{j, logs.length}); //NOI18N
                    continue;
                }
                if (annotation.getRevision().equals(log.getRevisionNumber())) {
                    annotation.setDate(log.getDate());
                    annotation.setId(log.getCSetShortID());
                    annotation.setCommitMessage(log.getMessage());
                    annotation.setAuthor(log.getAuthor());
                }
            }
        }
        String lowestRev = initialRevision == null ? "-1" : initialRevision.getRevisionNumber();
        for (int i = 0; i < annotations.length; i++) {
            AnnotateLine annotation = annotations[i];
            if (annotation == null) {
                Mercurial.LOG.log(Level.WARNING, "AnnotateAction: annotation {0} of {1} is null", new Object[]{i, annotations.length}); //NOI18N
            }else{
                annotation.setCanBeRolledBack(!annotation.getRevision().equals(lowestRev));
            }
        }
    }

    static AnnotateLine [] toAnnotateLines(List<String> annotations)
    {
        final int GROUP_AUTHOR = 1;
        final int GROUP_REVISION = 2;
        final int GROUP_FILENAME = 3;
        final int GROUP_LINE_NUMBER = 4;
        final int GROUP_CONTENT = 5;
        
        List<AnnotateLine> lines = new ArrayList<AnnotateLine>();
        int i = 0;
        /*
        The output pattern of the mercurial blame command seems to be pretty
        stable. In the test 3.1.2 and 4.5.3 were compared and found to yield
        identical outputs.

        Alternatives are currently not viable - while the JSON output sounds
        better, it is not stable as it significantly changed from 4.0 to 4.5.3.

        Oberservations:
        - the output contains 5 elements:
          - username
          - revision number
          - filename
          - line number
          - line content
        - the username does not contains spaces
        - there can be whitespace before the username (the column is right
          aligned with the longest username
        - the revision number is purely nummeric
        - the revision number is right aligned and is padded with spaces
          on the left side
        - after the revision number exactly one space is placed
        - the filename has no limit
        - between the filename and the linenumber is a colon placed directly
          behind the filename
        - between the colon and the linenumber an unlimited number of spaces can
          be present
        - directly behind the linenumber a colon and a space is placed
        - the rest of the line is the line content
        */
        Pattern p = Pattern.compile("^\\s*(\\S+)\\s+(\\d+) (.*?):\\s*(\\d+): (.*)$"); //NOI18N
        for (String line : annotations) {
            i++;
            Matcher m = p.matcher(line);
            AnnotateLine anLine;
            if (!m.matches()){
                Mercurial.LOG.log(Level.WARNING, "AnnotateAction: toAnnotateLines(): Failed when matching: {0}", new Object[] {line}); //NOI18N
                anLine = new FakeAnnotationLine();
            } else {
                anLine = new AnnotateLine();
                anLine.setAuthor(m.group(GROUP_AUTHOR));
                anLine.setRevision(m.group(GROUP_REVISION));
                anLine.setFileName(m.group(GROUP_FILENAME));
                try {
                    anLine.setPrevLineNum(Integer.parseInt(m.group(GROUP_LINE_NUMBER)));
                } catch (NumberFormatException ex) {
                    anLine.setPrevLineNum(-1);
                }
                anLine.setContent(m.group(GROUP_CONTENT));
            }
            anLine.setLineNum(i);
            
            lines.add(anLine);
        }
        return lines.toArray(new AnnotateLine[0]);
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

    private static class FakeAnnotationLine extends AnnotateLine {
        public FakeAnnotationLine() {
            String fakeItem = NbBundle.getMessage(AnnotateAction.class, "MSG_AnnotateAction.lineDetail.unknown");
            setAuthor(fakeItem);
            setContent(fakeItem);
            setRevision(fakeItem);
            setFileName(fakeItem);
        }
    }
}
