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
package org.netbeans.modules.git.ui.blame;

import org.netbeans.modules.versioning.spi.VCSContext;

import javax.swing.*;
import java.io.File;
import java.util.*;
import org.netbeans.libs.git.GitBlameResult;
import org.netbeans.libs.git.GitException;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileStatusCache;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.GitAction;
import org.netbeans.modules.git.utils.GitUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.cookies.EditorCookie;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.windows.TopComponent;
import org.openide.text.NbDocument;
import org.openide.util.Mutex;

@ActionID(id = "org.netbeans.modules.git.ui.blame.AnnotateAction", category = "Git")
@ActionRegistration(displayName = "#CTL_MenuItem_ShowAnnotations")
@NbBundle.Messages({
    "CTL_MenuItem_ShowAnnotations=Show A&nnotations"
})
public class AnnotateAction extends GitAction {
    private static final String ICON_RESOURCE = "org/netbeans/modules/git/resources/icons/annotate.png"; //NOI18N
    
    public AnnotateAction () {
        super(ICON_RESOURCE);
    }
    
    @Override
    protected boolean enableFull (Node[] nodes) {
        VCSContext context = getCurrentContext(nodes);
        if (context.getRootFiles().size() > 0 && activatedEditorCookie(nodes) != null) {
            FileStatusCache cache = Git.getInstance().getFileStatusCache();
            File file = activatedFile(nodes);
            if (file == null) {
                return false;
            }
            FileInformation info = cache.getStatus(file);
            return info != null && !info.containsStatus(EnumSet.of(FileInformation.Status.NOTVERSIONED_EXCLUDED, 
                    FileInformation.Status.NOTVERSIONED_NOTMANAGED, 
                    FileInformation.Status.NEW_HEAD_WORKING_TREE));
        } else {
            return false;
        }
    } 

    @Override
    public String getName () {
        return NbBundle.getMessage(getClass(), visible(TopComponent.getRegistry().getActivatedNodes()) ? "CTL_MenuItem_HideAnnotations" : "CTL_MenuItem_ShowAnnotations"); //NOI18N
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }

    @Override
    protected void performContextAction (Node[] nodes) {
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

    public void showAnnotations (JEditorPane currentPane, final File file, final String revision) {
        if (currentPane == null || file == null) {
            return;
        }
        TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, currentPane);
        tc.requestActive();

        final AnnotationBar ab = AnnotationBarManager.showAnnotationBar(currentPane);
        ab.setAnnotationMessage(NbBundle.getMessage(AnnotateAction.class, "CTL_AnnotationSubstitute")); // NOI18N;

        final File repository = Git.getInstance().getRepositoryRoot(file);
        if (repository == null) {
            return;
        }

        new GitProgressSupport() {
            @Override
            public void perform() {
                GitUtils.logRemoteRepositoryAccess(repository);
                if (revision != null) {
                    // showing annotations from past, the referenced file differs from the one being displayed
                    ab.setReferencedFile(file);
                }
                GitBlameResult result = null;
                try {
                    result = getClient().blame(file, revision == null ? "HEAD" : revision, getProgressMonitor());
                    if (isCanceled()) {
                        ab.setAnnotationMessage(NbBundle.getMessage(AnnotateAction.class, "CTL_AnnotationFailed")); // NOI18N;
                        return;
                    }
                    AnnotateLine [] lines = toAnnotateLines(result);
                    ab.setAnnotatedRevision(revision);
                    ab.annotationLines(file, Arrays.asList(lines));
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                }
            }
        }.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(AnnotateAction.class, "MSG_Annotation_Progress")); // NOI18N
    }

    private static AnnotateLine [] toAnnotateLines (GitBlameResult result) {
        if (result == null) {
            return new AnnotateLine[0];
        }
        List<AnnotateLine> lines = new ArrayList<AnnotateLine>(result.getLineCount());
        for (int i = 0; i < result.getLineCount(); ++i) {
            lines.add(new AnnotateLine(result.getLineDetails(i), i + 1));
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

    private File activatedFile (Node[] nodes) {
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
}
