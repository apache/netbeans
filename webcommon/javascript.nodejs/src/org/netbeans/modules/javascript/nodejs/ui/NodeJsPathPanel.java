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
package org.netbeans.modules.javascript.nodejs.ui;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.javascript.nodejs.exec.NodeExecutable;
import org.netbeans.modules.javascript.nodejs.util.FileUtils;
import org.netbeans.modules.javascript.nodejs.util.NodeJsUtils;
import org.netbeans.modules.web.clientproject.api.network.NetworkException;
import org.netbeans.modules.web.clientproject.api.util.StringUtilities;
import org.netbeans.modules.web.common.api.Version;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public final class NodeJsPathPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(NodeJsPathPanel.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(NodeJsPathPanel.class);

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final RequestProcessor.Task versionTask;

    volatile File nodeSources = null;


    public NodeJsPathPanel() {
        initComponents();
        init();

        versionTask = RP.create(new Runnable() {
            @Override
            public void run() {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setVersion();
                    }
                });
            }
        });
    }

    @NbBundle.Messages({
        "# {0} - node.js file name",
        "NodeJsPathPanel.node.hint1=Full path of node file (typically {0}).",
        "# {0} - node.js file name",
        "# {1} - node.js alternative file name",
        "NodeJsPathPanel.node.hint2=Full path of node file (typically {0} or {1}).",
    })
    private void init() {
        sourcesTextField.setText(" "); // NOI18N
        String[] nodes = NodeExecutable.NODE_NAMES;
        if (nodes.length > 1) {
            nodeHintLabel.setText(Bundle.NodeJsPathPanel_node_hint2(nodes[0], nodes[1]));
        } else {
            nodeHintLabel.setText(Bundle.NodeJsPathPanel_node_hint1(nodes[0]));
        }
        // listeners
        nodeTextField.getDocument().addDocumentListener(new NodeDocumentListener());
        sourcesTextField.getDocument().addDocumentListener(new DefaultDocumentListener());
    }

    public String getNode() {
        return nodeTextField.getText();
    }

    public void setNode(String node) {
        nodeTextField.setText(node);
    }

    @CheckForNull
    public String getNodeSources() {
        if (nodeSources != null) {
            return nodeSources.getAbsolutePath();
        }
        return null;
    }

    public void setNodeSources(String nodeSources) {
        if (StringUtilities.hasText(nodeSources)) {
            this.nodeSources = new File(nodeSources);
            setNodeSourcesDescription();
        }
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public void enablePanel(boolean enabled) {
        assert EventQueue.isDispatchThread();
        nodeLabel.setEnabled(enabled);
        nodeTextField.setEnabled(enabled);
        nodeBrowseButton.setEnabled(enabled);
        nodeSearchButton.setEnabled(enabled);
        nodeHintLabel.setEnabled(enabled);
        nodeInstallLabel.setVisible(enabled);
        sourcesLabel.setEnabled(enabled);
        sourcesTextField.setEnabled(enabled);
        selectSourcesButton.setEnabled(enabled);
        downloadSourcesButton.setEnabled(false);
        if (enabled) {
            if (nodeSources != null) {
                setNodeSourcesDescription();
            }
            setVersion();
        }
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    void detectVersion() {
        versionTask.schedule(100);
    }

    @NbBundle.Messages("NodeJsPathPanel.version.detecting=Detecting...")
    void setVersion() {
        assert EventQueue.isDispatchThread();
        downloadSourcesButton.setEnabled(false);
        if (nodeSources == null) {
            setNodeSourcesDescription(Bundle.NodeJsPathPanel_version_detecting());
        }
        final String nodePath = getNode();
        RP.post(new Runnable() {
            @Override
            public void run() {
                final Version version;
                final Version realVersion;
                NodeExecutable node = NodeExecutable.forPath(nodePath);
                if (node != null) {
                    version = node.getVersion();
                    realVersion = node.getRealVersion();
                } else {
                    version = null;
                    realVersion = null;
                }
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        assert EventQueue.isDispatchThread();
                        if (version != null) {
                            downloadSourcesButton.setEnabled(true);
                        }
                        if (nodeSources == null) {
                            setNodeSourcesDescription(version, realVersion);
                        }
                    }
                });
            }
        });
    }

    @NbBundle.Messages({
        "# {0} - version",
        "NodeJsPathPanel.sources.exists=Sources for version {0} already exist. Download again?",
        "NodeJsPathPanel.sources.downloading=Downloading...",
        "NodeJsPathPanel.download.success=Node.js sources downloaded successfully.",
        "# {0} - file URL",
        "NodeJsPathPanel.download.failure=File {0} cannot be downloaded.",
        "NodeJsPathPanel.download.error=Error occured during download (see IDE log).",
    })
    private void downloadSources() {
        assert EventQueue.isDispatchThread();
        downloadSourcesButton.setEnabled(false);
        String nodePath = getNode();
        final NodeExecutable node = NodeExecutable.forPath(nodePath);
        assert node != null : nodePath;
        final Version version = node.getVersion();
        assert version != null : nodePath;
        final Version realVersion = node.getRealVersion();
        assert realVersion != null : version;
        if (NodeJsUtils.hasNodeSources(version)) {
            nodeSources = null;
            setNodeSourcesDescription(version, realVersion);
            NotifyDescriptor.Confirmation confirmation = new NotifyDescriptor.Confirmation(
                    Bundle.NodeJsPathPanel_sources_exists(version.toString()),
                    NotifyDescriptor.YES_NO_OPTION);
            if (DialogDisplayer.getDefault().notify(confirmation) == NotifyDescriptor.NO_OPTION) {
                downloadSourcesButton.setEnabled(true);
                return;
            }
        }
        sourcesTextField.setText(Bundle.NodeJsPathPanel_sources_downloading());
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (FileUtils.downloadNodeSources(version, node.isIojs())) {
                        StatusDisplayer.getDefault().setStatusText(Bundle.NodeJsPathPanel_download_success());
                    }
                    nodeSources = null;
                } catch (NetworkException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                    informUser(Bundle.NodeJsPathPanel_download_failure(ex.getFailedRequests().get(0)));
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                    informUser(Bundle.NodeJsPathPanel_download_error());
                }
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setNodeSourcesDescription(version, realVersion);
                        downloadSourcesButton.setEnabled(true);
                    }
                });
            }
        });
    }

    private void informUser(String message) {
        NotifyDescriptor descriptor = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notifyLater(descriptor);
    }

    private void setNodeSourcesDescription() {
        assert EventQueue.isDispatchThread();
        File nodeSourcesRef = nodeSources;
        assert nodeSourcesRef != null;
        setNodeSourcesDescription(nodeSourcesRef.getAbsolutePath());
    }

    @NbBundle.Messages({
        "# {0} - node.js version",
        "NodeJsPathPanel.sources.downloaded=Downloaded (version {0})",
        "# {0} - real node.js version",
        "# {1} - node.js version",
        "NodeJsPathPanel.sources.es5.downloaded=Downloaded (version {0} -> {1})",
        "# {0} - node.js version",
        "NodeJsPathPanel.sources.not.downloaded=Not downloaded (version {0})",
        "# {0} - real node.js version",
        "# {1} - node.js version",
        "NodeJsPathPanel.sources.es5.not.downloaded=Not downloaded (version {0} -> {1})",
        "NodeJsPathPanel.sources.na=Not available",
    })
    private void setNodeSourcesDescription(@NullAllowed Version version, @NullAllowed Version realVersion) {
        assert EventQueue.isDispatchThread();
        String text;
        if (version == null) {
            text = Bundle.NodeJsPathPanel_sources_na();
        } else if (NodeJsUtils.hasNodeSources(version)) {
            if (Objects.equals(version, realVersion)) {
                text = Bundle.NodeJsPathPanel_sources_downloaded(version);
            } else {
                assert realVersion != null : version;
                text = Bundle.NodeJsPathPanel_sources_es5_downloaded(realVersion, version);
            }
        } else {
            if (Objects.equals(version, realVersion)) {
                text = Bundle.NodeJsPathPanel_sources_not_downloaded(version);
            } else {
                assert realVersion != null : version;
                text = Bundle.NodeJsPathPanel_sources_es5_not_downloaded(realVersion, version);
            }
        }
        setNodeSourcesDescription(text);
    }

    private void setNodeSourcesDescription(String text) {
        assert EventQueue.isDispatchThread();
        sourcesTextField.setText(text);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nodeLabel = new JLabel();
        nodeTextField = new JTextField();
        nodeBrowseButton = new JButton();
        nodeSearchButton = new JButton();
        nodeHintLabel = new JLabel();
        nodeInstallLabel = new JLabel();
        sourcesLabel = new JLabel();
        sourcesTextField = new JTextField();
        downloadSourcesButton = new JButton();
        selectSourcesButton = new JButton();

        Mnemonics.setLocalizedText(nodeLabel, NbBundle.getMessage(NodeJsPathPanel.class, "NodeJsPathPanel.nodeLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(nodeBrowseButton, NbBundle.getMessage(NodeJsPathPanel.class, "NodeJsPathPanel.nodeBrowseButton.text")); // NOI18N
        nodeBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                nodeBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(nodeSearchButton, NbBundle.getMessage(NodeJsPathPanel.class, "NodeJsPathPanel.nodeSearchButton.text")); // NOI18N
        nodeSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                nodeSearchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(nodeHintLabel, "HINT"); // NOI18N

        Mnemonics.setLocalizedText(nodeInstallLabel, NbBundle.getMessage(NodeJsPathPanel.class, "NodeJsPathPanel.nodeInstallLabel.text")); // NOI18N
        nodeInstallLabel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                nodeInstallLabelMousePressed(evt);
            }
            public void mouseEntered(MouseEvent evt) {
                nodeInstallLabelMouseEntered(evt);
            }
        });

        Mnemonics.setLocalizedText(sourcesLabel, NbBundle.getMessage(NodeJsPathPanel.class, "NodeJsPathPanel.sourcesLabel.text")); // NOI18N

        sourcesTextField.setEditable(false);
        sourcesTextField.setColumns(30);

        Mnemonics.setLocalizedText(downloadSourcesButton, NbBundle.getMessage(NodeJsPathPanel.class, "NodeJsPathPanel.downloadSourcesButton.text")); // NOI18N
        downloadSourcesButton.setEnabled(false);
        downloadSourcesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                downloadSourcesButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(selectSourcesButton, NbBundle.getMessage(NodeJsPathPanel.class, "NodeJsPathPanel.selectSourcesButton.text")); // NOI18N
        selectSourcesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                selectSourcesButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(nodeLabel)
                    .addComponent(sourcesLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nodeTextField)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nodeBrowseButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nodeSearchButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nodeHintLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(nodeInstallLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sourcesTextField, GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(downloadSourcesButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selectSourcesButton))))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(nodeLabel)
                    .addComponent(nodeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(nodeBrowseButton)
                    .addComponent(nodeSearchButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(nodeHintLabel)
                    .addComponent(nodeInstallLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(sourcesLabel)
                    .addComponent(downloadSourcesButton)
                    .addComponent(selectSourcesButton)
                    .addComponent(sourcesTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("NodeJsPathPanel.node.browse.title=Select node")
    private void nodeBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_nodeBrowseButtonActionPerformed
        assert EventQueue.isDispatchThread();
        File file = new FileChooserBuilder(NodeJsPathPanel.class)
                .setFilesOnly(true)
                .setTitle(Bundle.NodeJsPathPanel_node_browse_title())
                .showOpenDialog();
        if (file != null) {
            nodeTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_nodeBrowseButtonActionPerformed

    @NbBundle.Messages("NodeJsPathPanel.node.none=No node executable was found.")
    private void nodeSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_nodeSearchButtonActionPerformed
        assert EventQueue.isDispatchThread();
        for (String node : FileUtils.findFileOnUsersPath(NodeExecutable.NODE_NAMES)) {
            nodeTextField.setText(new File(node).getAbsolutePath());
            return;
        }
        // no node found
        StatusDisplayer.getDefault().setStatusText(Bundle.NodeJsPathPanel_node_none());
    }//GEN-LAST:event_nodeSearchButtonActionPerformed

    private void downloadSourcesButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_downloadSourcesButtonActionPerformed
        downloadSources();
    }//GEN-LAST:event_downloadSourcesButtonActionPerformed

    private void nodeInstallLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_nodeInstallLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_nodeInstallLabelMouseEntered

    private void nodeInstallLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_nodeInstallLabelMousePressed
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL("https://nodejs.org/")); // NOI18N
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }//GEN-LAST:event_nodeInstallLabelMousePressed

    @NbBundle.Messages("NodeJsPathPanel.sources.browse.title=Select node.js sources")
    private void selectSourcesButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_selectSourcesButtonActionPerformed
        assert EventQueue.isDispatchThread();
        File sources = new FileChooserBuilder(NodeJsPathPanel.class)
                .setDirectoriesOnly(true)
                .setTitle(Bundle.NodeJsPathPanel_sources_browse_title())
                .showOpenDialog();
        if (sources != null) {
            nodeSources = sources;
            setNodeSourcesDescription();
        }
    }//GEN-LAST:event_selectSourcesButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton downloadSourcesButton;
    private JButton nodeBrowseButton;
    private JLabel nodeHintLabel;
    private JLabel nodeInstallLabel;
    private JLabel nodeLabel;
    private JButton nodeSearchButton;
    private JTextField nodeTextField;
    private JButton selectSourcesButton;
    private JLabel sourcesLabel;
    private JTextField sourcesTextField;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class NodeDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            processUpdate();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processUpdate();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processUpdate();
        }

        private void processUpdate() {
            fireChange();
            NodeExecutable node = NodeExecutable.forPath(getNode());
            if (node != null) {
                node.resetVersion();
            }
            detectVersion();
        }

    }

    private final class DefaultDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            processUpdate();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processUpdate();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processUpdate();
        }

        private void processUpdate() {
            fireChange();
        }

    }

}
