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


package org.netbeans.modules.maven.repository.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries.Result;
import org.netbeans.modules.maven.indexer.api.RepositoryUtil;
import org.netbeans.modules.maven.indexer.api.ui.ArtifactViewer;
import static org.netbeans.modules.maven.repository.ui.Bundle.*;
import org.openide.awt.Actions;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 *
 * @author mkleint
 */
public class BasicArtifactPanel extends TopComponent implements MultiViewElement {

    private static final RequestProcessor RP = new RequestProcessor(BasicArtifactPanel.class);
    
    private boolean renderType = false;
    
    private JToolBar toolbar;

    /** Creates new form BasicArtifactPanel */
    public BasicArtifactPanel(Lookup lookup) {
        super(lookup);
        initComponents();
        lstVersions.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() > 1 && e.getButton() == MouseEvent.BUTTON1) {
                    Object obj = lstVersions.getSelectedValue();
                    if (obj instanceof String) {
                       //Loading.. text #160353
                        return;
                    }
                    NBVersionInfo info = (NBVersionInfo) obj;
                    if (info != null) {
                        ArtifactViewer.showArtifactViewer(info);
                    }
                }
            }
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {}
            @Override
            public void mouseDragged(MouseEvent e) {}
            @Override
            public void mouseMoved(MouseEvent e) {}
        });
        lstVersions.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof NBVersionInfo) {
                    NBVersionInfo info = (NBVersionInfo)value;
                    if (renderType) {
                    //often there are 2 or more types associated with a given version, list it, instead of just rendering 2 or more same versions
                        ((JLabel)c).setText(info.getVersion() + "  [" + info.getType() + "]");
                    } else {
                        ((JLabel)c).setText(info.getVersion());
                    }
                }
                return c;
            }
        });
        lstVersions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
            this.jPanel1.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
            this.jPanel2.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
            this.jPanel3.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
            this.jPanel4.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
            this.jPanel5.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
        }
    }

    @Messages({
        "# {0} - number of bytes",
        "TXT_Bytes={0} bytes",
        "# {0} - number of kilo bytes",
        "TXT_kb={0} kb",
        "# {0} - number of mega bytes",
        "TXT_Mb={0} Mb"
    })
    private String computeSize(long size) {
        long kbytes = size / 1024;
        if (kbytes == 0) {
            return TXT_Bytes(size);
        }
        long mbytes = kbytes / 1024;
        if (mbytes == 0) {
            return TXT_kb(kbytes);
        }
        return TXT_Mb(mbytes);
    }

    public @Override int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        lblGroupId = new javax.swing.JLabel();
        txtGroupId = new javax.swing.JTextField();
        lblArtifactId = new javax.swing.JLabel();
        txtArtifactId = new javax.swing.JTextField();
        lblVersion = new javax.swing.JLabel();
        txtVersion = new javax.swing.JTextField();
        lblPackaging = new javax.swing.JLabel();
        txtPackaging = new javax.swing.JTextField();
        lblClassifier = new javax.swing.JLabel();
        txtClassifier = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtSize = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtLastModified = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtSHA = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstVersions = new javax.swing.JList();
        jLabel4 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        lstClassifiers = new javax.swing.JList();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "BasicArtifactPanel.jPanel1.border.title"))); // NOI18N

        lblGroupId.setLabelFor(txtGroupId);
        lblGroupId.setText(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "BasicArtifactPanel.lblGroupId.text")); // NOI18N

        txtGroupId.setEditable(false);

        lblArtifactId.setLabelFor(txtArtifactId);
        lblArtifactId.setText(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "BasicArtifactPanel.lblArtifactId.text")); // NOI18N

        txtArtifactId.setEditable(false);

        lblVersion.setLabelFor(txtVersion);
        lblVersion.setText(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "BasicArtifactPanel.lblVersion.text")); // NOI18N

        txtVersion.setEditable(false);

        lblPackaging.setText(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "BasicArtifactPanel.lblPackaging.text")); // NOI18N

        txtPackaging.setEditable(false);

        lblClassifier.setText(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "BasicArtifactPanel.lblClassifier.text")); // NOI18N

        txtClassifier.setEditable(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblGroupId)
                    .addComponent(lblArtifactId)
                    .addComponent(lblVersion)
                    .addComponent(lblPackaging)
                    .addComponent(lblClassifier))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtClassifier)
                    .addComponent(txtArtifactId, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                    .addComponent(txtGroupId, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                    .addComponent(txtVersion, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                    .addComponent(txtPackaging, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblGroupId)
                    .addComponent(txtGroupId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblArtifactId)
                    .addComponent(txtArtifactId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblVersion)
                    .addComponent(txtVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPackaging)
                    .addComponent(txtPackaging, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblClassifier)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(txtClassifier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)))
                .addContainerGap(71, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "TIT_PrimaryArtifact"))); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "BasicArtifactPanel.jLabel1.text")); // NOI18N

        txtSize.setEditable(false);

        jLabel2.setText(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "BasicArtifactPanel.jLabel2.text")); // NOI18N

        txtLastModified.setEditable(false);

        jLabel3.setText(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "BasicArtifactPanel.jLabel3.text")); // NOI18N

        txtSHA.setEditable(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtLastModified, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                    .addComponent(txtSize, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                    .addComponent(txtSHA, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtLastModified, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtSHA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(119, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "TIT_Versions"))); // NOI18N

        jScrollPane2.setViewportView(lstVersions);

        jLabel4.setText(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "BasicArtifactPanel.jLabel4.text")); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(BasicArtifactPanel.class, "TIT_SecondaryArtifacts"))); // NOI18N

        jScrollPane3.setViewportView(lstClassifiers);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(15, 15, 15))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(123, 123, 123))
        );

        jScrollPane1.setViewportView(jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 699, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblArtifactId;
    private javax.swing.JLabel lblClassifier;
    private javax.swing.JLabel lblGroupId;
    private javax.swing.JLabel lblPackaging;
    private javax.swing.JLabel lblVersion;
    private javax.swing.JList lstClassifiers;
    private javax.swing.JList lstVersions;
    private javax.swing.JTextField txtArtifactId;
    private javax.swing.JTextField txtClassifier;
    private javax.swing.JTextField txtGroupId;
    private javax.swing.JTextField txtLastModified;
    private javax.swing.JTextField txtPackaging;
    private javax.swing.JTextField txtSHA;
    private javax.swing.JTextField txtSize;
    private javax.swing.JTextField txtVersion;
    // End of variables declaration//GEN-END:variables

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        if (toolbar == null) {
            toolbar = new JToolBar();
            if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
                toolbar.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
            }
            toolbar.setFloatable(false);
            Action[] a = new Action[1];
            Action[] actions = getLookup().lookup(a.getClass());
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

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
    }

    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
    }

    @Messages({
        "TXT_Loading=Loading...",
        "MSG_FailedSHA1=<Failed to calculate SHA1>",
        "MSG_NOSHA=<Cannot calculate SHA1, the artifact is not present locally>",
        "TXT_INCOMPLETE=<Incomplete result, processing indices...>"
    })
    @Override
    public void componentOpened() {
        final Artifact artifact = getLookup().lookup(Artifact.class);
        assert artifact != null;
        
        final NBVersionInfo info = getLookup().lookup(NBVersionInfo.class);
        if (info != null) {
            txtGroupId.setText(info.getGroupId());
            txtArtifactId.setText(info.getArtifactId());
            txtVersion.setText(info.getVersion());
            txtPackaging.setText(info.getType());
            txtClassifier.setText(info.getClassifier());
            txtSize.setText(computeSize(info.getSize()));
            txtLastModified.setText("" + new Date(info.getLastModified()));
        } else {
            txtGroupId.setText(artifact.getGroupId());
            txtArtifactId.setText(artifact.getArtifactId());
            txtVersion.setText(artifact.getVersion());
            txtPackaging.setText(artifact.getType());
            txtClassifier.setText(artifact.getClassifier());
        }


        final DefaultListModel dlm = new DefaultListModel();
        dlm.addElement(TXT_Loading());
        lstVersions.setModel(dlm);
        RP.post(new Runnable() {
            @Override
            public void run() {
                final Result<NBVersionInfo> result = RepositoryQueries.getVersionsResult(artifact.getGroupId(), artifact.getArtifactId(), RepositoryPreferences.getInstance().getRepositoryInfos());
                final List<NBVersionInfo> infos = result.getResults();
                final ArtifactVersion av = new DefaultArtifactVersion(artifact.getVersion());
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dlm.removeAllElements();
                        for (NBVersionInfo ver : infos) {
                            if (!av.equals(new DefaultArtifactVersion(ver.getVersion()))) {
                                dlm.addElement(ver);
                            }
                            if (!artifact.getType().equals(ver.getType())) {
                                renderType = true;
                            }
                        }
                        if (result.isPartial()) {
                            dlm.addElement(TXT_INCOMPLETE());
                        }
                    }
                });
            }
        });
        final DefaultListModel mdl = new DefaultListModel();
        mdl.addElement(TXT_Loading());
        lstClassifiers.setModel(mdl);
        RP.post(new Runnable() {
            @Override
            public void run() {
                final Result<NBVersionInfo> result = RepositoryQueries.getRecordsResult(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), RepositoryPreferences.getInstance().getRepositoryInfos());
                List<NBVersionInfo> infos = result.getResults();
                final Set<String> classifiers = new TreeSet<String>();
                boolean hasJavadoc = false;
                boolean hasSource = false;
                for (NBVersionInfo inf : infos) {
                    if (inf.getClassifier() != null) {
                        classifiers.add(inf.getClassifier());
                    }
                    if (inf.isJavadocExists()) {
                        hasJavadoc = true;
                    }
                    if (inf.isSourcesExists()) {
                        hasSource = true;
                    }
                }
                if (hasSource) {
                    classifiers.add("source");
                }
                if (hasJavadoc) {
                    classifiers.add("javadoc");
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        mdl.removeAllElements();
                        for (String ver : classifiers) {
                            mdl.addElement(ver);
                        }
                        if (result.isPartial()) {
                            mdl.addElement(TXT_INCOMPLETE());
                        }
                    }
                });
            }
        });


        File artFile = FileUtilities.convertArtifactToLocalRepositoryFile(artifact);
        if (artFile.exists()) {
            try {
                String sha = RepositoryUtil.calculateSHA1Checksum(artFile);
                txtSHA.setText(sha);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                txtSHA.setText(MSG_FailedSHA1());
            }
        } else {
            txtSHA.setText(MSG_NOSHA());
        }
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
    }


}
