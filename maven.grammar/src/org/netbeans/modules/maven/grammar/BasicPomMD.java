/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.grammar;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import org.apache.maven.artifact.Artifact;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import static org.netbeans.modules.maven.grammar.Bundle.*;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryUtil;
import org.netbeans.modules.maven.indexer.spi.ui.ArtifactViewerPanelProvider;
import org.openide.awt.Actions;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

public class BasicPomMD implements MultiViewDescription, Serializable {

    private static final RequestProcessor RP = new RequestProcessor(BasicPomMD.class);

    private final Lookup lookup;

    private BasicPomMD(Lookup lookup) {
        this.lookup = lookup;
    }

    @Override public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Messages("TAB_Pom=POM")
    @Override public String getDisplayName() {
        return TAB_Pom();
    }

    @Override public Image getIcon() {
        return ImageUtilities.loadImage(POMDataObject.POM_ICON, true);
    }

    @Override public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override public String preferredID() {
        return "pom"; // XXX could be ArtifactViewer.HINT_* constant
    }

    @Override public MultiViewElement createElement() {
        return new POMView(lookup);
    }

    @ServiceProvider(service=ArtifactViewerPanelProvider.class, position=500)
    public static class Factory implements ArtifactViewerPanelProvider {

        @Override public MultiViewDescription createPanel(Lookup lookup) {
            return new BasicPomMD(lookup);
        }

    }

    private static class POMView implements MultiViewElement, Runnable {

        private final Lookup lookup;
        private final RequestProcessor.Task task = RP.create(this);
        private JToolBar toolbar;
        private JPanel panel;

        POMView(Lookup lookup) {
            this.lookup = lookup;
        }

        @Override public JComponent getVisualRepresentation() {
            if (panel == null) {
                panel = new JPanel(new BorderLayout());
            }
            return panel;
        }

        @Override public JComponent getToolbarRepresentation() {
            // XXX copied from org.netbeans.modules.maven.repository.ui, should be made into shared API
            if (toolbar == null) {
                toolbar = new JToolBar();
                toolbar.setFloatable(false);
                if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
                    toolbar.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
                }
                
                Action[] a = new Action[1];
                Action[] actions = lookup.lookup(a.getClass());
                Dimension space = new Dimension(3, 0);
                toolbar.addSeparator(space);
                for (Action act : actions) {
                    JButton btn = new JButton();
                    Actions.connect(btn, act);
                    toolbar.add(btn);
                    toolbar.addSeparator(space);
                }
            }
            return toolbar;
        }

        @Override public void setMultiViewCallback(MultiViewElementCallback callback) {}

        @Override public CloseOperationState canCloseElement() {
            return CloseOperationState.STATE_OK;
        }

        @Override public Action[] getActions() {
            return new Action[0];
        }

        @Override public Lookup getLookup() {
            return lookup;
        }

        @Override public void componentOpened() {}

        @Override public void componentClosed() {}

        @Messages("LBL_loading=Loading POM...")
        @Override public void componentShowing() {
            panel.add(new JLabel(LBL_loading(), SwingConstants.CENTER), BorderLayout.CENTER);
            task.schedule(0);
        }

        @Override public void componentHidden() {}

        @Override public void componentActivated() {}

        @Override public void componentDeactivated() {}

        @Override public UndoRedo getUndoRedo() {
            return UndoRedo.NONE;
        }

        // XXX prefer to use CloneableEditor, and try to make it work with POM navigator panels
        @Messages({"# {0} - message", "LBL_failed_to_load=Failed to load POM: {0}"})
        @Override public void run() {
            Artifact artifact = lookup.lookup(Artifact.class);
            assert artifact != null;
            NBVersionInfo originfo = lookup.lookup(NBVersionInfo.class);
            //in some cases the artifact.getRepository() will be null..
            // eg. NbVersionInfo -> artifact -> NBVersionInfo looses the repository id information.
            String repoId = null;
            if (originfo != null) {
                repoId = originfo.getRepoId();
            }
            if (repoId == null) {
                repoId = artifact.getRepository() != null ? artifact.getRepository().getId() : null;
            }

            NBVersionInfo info = new NBVersionInfo(repoId, artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), "pom", null, null, null, null);
            try {
                File pom = RepositoryUtil.downloadArtifact(info);
                FileObject pomFO = FileUtil.toFileObject(pom);
                if (pomFO == null) {
                    throw new FileNotFoundException(pom.getAbsolutePath());
                }
                final String text = FileEncodingQuery.getEncoding(pomFO).decode(ByteBuffer.wrap(pomFO.asBytes())).toString();
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        JEditorPane pane = new JEditorPane("text/xml", text);
                        pane.setEditable(false);
                        pane.setCaretPosition(0);
                        panel.removeAll();
                        panel.add(new JScrollPane(pane), BorderLayout.CENTER);
                        panel.revalidate();
                    }
                });
            } catch (final Exception x) {
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        panel.removeAll();
                        panel.add(new JLabel(LBL_failed_to_load(x.getLocalizedMessage()), SwingConstants.CENTER), BorderLayout.CENTER);
                        panel.revalidate();
                    }
                });
            }
        }

    }

}
