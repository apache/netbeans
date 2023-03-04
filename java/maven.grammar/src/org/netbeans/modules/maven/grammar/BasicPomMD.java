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
