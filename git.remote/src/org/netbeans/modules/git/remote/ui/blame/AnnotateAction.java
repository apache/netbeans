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
package org.netbeans.modules.git.remote.ui.blame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import org.netbeans.modules.git.remote.cli.GitBlameResult;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.FileInformation;
import org.netbeans.modules.git.remote.FileStatusCache;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.client.GitClientExceptionHandler;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.git.remote.ui.actions.GitAction;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(id = "org.netbeans.modules.git.remote.ui.blame.AnnotateAction", category = "GitRemote")
@ActionRegistration(displayName = "#CTL_MenuItem_ShowAnnotations")
@NbBundle.Messages({
    "CTL_MenuItem_ShowAnnotations=Show A&nnotations"
})
public class AnnotateAction extends GitAction {
    private static final String ICON_RESOURCE = "org/netbeans/modules/git/remote/resources/icons/annotate.png"; //NOI18N
    
    public AnnotateAction () {
        super(ICON_RESOURCE);
    }
    
    @Override
    protected boolean enable (Node[] nodes) {
        VCSContext context = getCurrentContext(nodes);
        if (context.getRootFiles().size() > 0 && activatedEditorCookie(nodes) != null) {
            FileStatusCache cache = Git.getInstance().getFileStatusCache();
            VCSFileProxy file = activatedFile(nodes);
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
            if (ec == null) {
                return;
            }
            final VCSFileProxy file = activatedFile(nodes);
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

    public void showAnnotations (JEditorPane currentPane, final VCSFileProxy file, final String revision) {
        if (currentPane == null || file == null) {
            return;
        }
        TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, currentPane);
        tc.requestActive();

        final AnnotationBar ab = AnnotationBarManager.showAnnotationBar(currentPane);
        ab.setAnnotationMessage(NbBundle.getMessage(AnnotateAction.class, "CTL_AnnotationSubstitute")); // NOI18N;

        final VCSFileProxy repository = Git.getInstance().getRepositoryRoot(file);
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
        List<AnnotateLine> lines = new ArrayList<>(result.getLineCount());
        for (int i = 0; i < result.getLineCount(); ++i) {
            lines.add(new AnnotateLine(result.getLineDetails(i), i + 1));
        }
        return lines.toArray(new AnnotateLine[lines.size()]);
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

    private VCSFileProxy activatedFile (Node[] nodes) {
        if (nodes.length == 1) {
            Node node = nodes[0];
            DataObject dobj = (DataObject) node.getCookie(DataObject.class);
            if (dobj != null) {
                FileObject fo = dobj.getPrimaryFile();
                return VCSFileProxy.createFileProxy(fo);
            }
        }
        return null;
    }
}
