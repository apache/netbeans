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

package org.netbeans.modules.maven.nodes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.TreeSelectionModel;
import org.apache.lucene.search.BooleanQuery;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.TextValueCompleter;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.customizer.support.DelayedDocumentChangeListener;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.QueryField;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries.Result;
import static org.netbeans.modules.maven.nodes.Bundle.*;
import org.netbeans.modules.maven.spi.nodes.MavenNodeFactory;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * @author  mkleint
 */
public class AddDependencyPanel extends javax.swing.JPanel {
    private static final @StaticResource String EMPTY_ICON = "org/netbeans/modules/maven/resources/empty.png";
    private static final @StaticResource String WAIT_ICON = "org/netbeans/modules/maven/resources/wait.gif";

    /**
     * Shows the Add Dependency dialog.
     * @param prj a project to add a dependency to
     * @param showDepMan true to show the dependency management panel
     * @param selectedScope an initial scope selection (such as {@code compile})
     * @return groupId + artifactId + version + scope + type + classifier, or null if canceled
     */
    @Messages("TIT_Add_Library=Add Dependency")
    public static @CheckForNull String[] show(Project prj, boolean showDepMan, String selectedScope) {
        NbMavenProject nbproj = prj.getLookup().lookup(NbMavenProject.class);
        AddDependencyPanel pnl = new AddDependencyPanel(nbproj.getMavenProject(), showDepMan, prj);
        pnl.getAccessibleContext().setAccessibleDescription(TIT_Add_Library());
        pnl.setSelectedScope(selectedScope);
        DialogDescriptor dd = new DialogDescriptor(pnl, TIT_Add_Library());
        dd.setClosingOptions(new Object[] {
            pnl.getOkButton(),
            DialogDescriptor.CANCEL_OPTION
        });
        dd.setOptions(new Object[] {
            pnl.getOkButton(),
            DialogDescriptor.CANCEL_OPTION
        });
        pnl.attachDialogDisplayer(dd);
        Object ret = DialogDisplayer.getDefault().notify(dd);
        if (pnl.getOkButton() == ret) {
            return new String[] {
                pnl.getGroupId(),
                pnl.getArtifactId(),
                pnl.getVersion(),
                pnl.getScope(),
                pnl.getType(),
                pnl.getClassifier()
            };
        }
        return null;
    }

    private MavenProject project;
    private Project nbProject;

    private final TextValueCompleter groupCompleter;
    private final TextValueCompleter artifactCompleter;
    private final TextValueCompleter versionCompleter;
    private final JButton okButton;
    private final QueryPanel queryPanel;
    private DMListPanel artifactList;

    private Color defaultVersionC;

    private static final RequestProcessor RP = new RequestProcessor(AddDependencyPanel.class.getName(), 5);
    private static final RequestProcessor RPofOpenListPanel = new RequestProcessor(AddDependencyPanel.OpenListPanel.class.getName(), 1);
    private static final RequestProcessor RPofDMListPanel = new RequestProcessor(AddDependencyPanel.DMListPanel.class.getName(), 1);
    private static final RequestProcessor RPofQueryPanel = new RequestProcessor(AddDependencyPanel.QueryPanel.class.getName(), 10);

    private NotificationLineSupport nls;

    @Messages("BTN_OK=Add")
    private AddDependencyPanel(MavenProject mavenProject, boolean showDepMan, Project prj) {
        this.project = mavenProject;
        this.nbProject = prj;
        initComponents();
        groupCompleter = new TextValueCompleter(Collections.<String>emptyList(), txtGroupId);
        artifactCompleter = new TextValueCompleter(Collections.<String>emptyList(), txtArtifactId);
        versionCompleter = new TextValueCompleter(Collections.<String>emptyList(), txtVersion);
        txtGroupId.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                final String groupId = txtGroupId.getText().trim();
                if (groupId.length() > 0) {
                    artifactCompleter.setLoading(true);
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            populateArtifact(groupId);
                        }
                    });
                }
            }
        });

        txtArtifactId.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                final String groupId = txtGroupId.getText().trim();
                final String artifactId = txtArtifactId.getText().trim();
                if (groupId.length() > 0 && artifactId.length() > 0) {
                    versionCompleter.setLoading(true);
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            populateVersion(groupId, artifactId);
                        }
                    });
                }
            }
        });

        okButton = new JButton(BTN_OK());

        DocumentListener docList = new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkValidState();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                checkValidState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkValidState();
            }
        };
        txtGroupId.getDocument().addDocumentListener(docList);
        txtVersion.getDocument().addDocumentListener(docList);
        txtArtifactId.getDocument().addDocumentListener(docList);
        checkValidState();
        groupCompleter.setLoading(true);
        RP.post(new Runnable() {
            @Override
            public void run() {
                Result<String> res = populateGroupId();
                if (res.isPartial()) {
                    //we will ignore any rare occurances of repository being added after the groupId result is 
                    // processed.. this is the only way of ensuring that the completion gets refreshed.
                    res.waitForSkipped();
                    populateGroupId();
                    final String[] vals = new String[2];
                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {
                            @Override
                            public void run() {
                                vals[0] = txtGroupId.getText().trim();
                                vals[1] = txtArtifactId.getText().trim();
                            }
                        });
                        if (vals[0] != null && vals[0].length() > 0) {
                            populateArtifact(vals[0]);
                            if (vals[1] != null && vals[1].length() > 0) {
                                populateVersion(vals[0], vals[1]);
                            }
                        }
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (InvocationTargetException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });

        queryPanel = new QueryPanel();
        resultsPanel.add(queryPanel, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(
                DelayedDocumentChangeListener.create(
                searchField.getDocument(), queryPanel, 500));

        /*searchField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                queryPanel.maybeFind(searchField.getText());
            }

            public void removeUpdate(DocumentEvent e) {
                queryPanel.maybeFind(searchField.getText());
            }

            public void changedUpdate(DocumentEvent e) {
                queryPanel.maybeFind(searchField.getText());
            }
        });*/

        defaultVersionC = txtVersion.getForeground();
        if (showDepMan) {
            artifactList = new DMListPanel(project);
            artifactPanel.add(artifactList, BorderLayout.CENTER);
        } else {
            tabPane.setEnabledAt(2, false);
        }
        chkNbOnly.setVisible(false);
        String packaging = prj.getLookup().lookup(NbMavenProject.class).getPackagingType();
        if (NbMavenProject.TYPE_NBM.equals(packaging) || NbMavenProject.TYPE_NBM_APPLICATION.equals(packaging)) {
            chkNbOnly.setVisible(true);
            chkNbOnly.setSelected(true);
        }

        pnlOpenProjects.add(new OpenListPanel(prj), BorderLayout.CENTER);

    }

    private JButton getOkButton() {
        return okButton;
    }

    private String getGroupId() {
        return txtGroupId.getText().trim();
    }

    private String getArtifactId() {
        return txtArtifactId.getText().trim();
    }

    private String getVersion() {
        String v = txtVersion.getText().trim();
        return v.isEmpty() ? null : v;
    }

    private String getScope() {
        String scope = comScope.getSelectedItem().toString();
        if ("compile".equals(scope)) { //NOI18N
            //compile is the default scope, no need to explicitly define.
            scope = null;
        }
        return scope;
    }

    private String getType() {
        String t = txtType.getText().trim();
        return t.isEmpty() ? null : t;
    }

    private String getClassifier() {
        String c = txtClassifier.getText().trim();
        return c.isEmpty() ? null : c;
    }

    /** For gaining access to DialogDisplayer instance to manage
     * warning messages
     */
    private void attachDialogDisplayer(DialogDescriptor dd) {
        nls = dd.getNotificationLineSupport();
        if (nls == null) {
            nls = dd.createNotificationLineSupport();
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        assert nls != null : " The notificationLineSupport was not attached to the panel."; //NOI18N
    }

    private void setSelectedScope(String type) {
        comScope.setSelectedItem(type);
    }

    @Messages ({"MSG_Defined=Dependency with given groupId and artifactId is already defined in project."})
    private void checkValidState() {
        String gId = txtGroupId.getText().trim();
        if (gId.length() <= 0) {
            gId = null;
        }
        String aId = txtArtifactId.getText().trim();
        if (aId.length() <= 0) {
            aId = null;
        }
        String version = txtVersion.getText().trim();
        if (version.length() <= 0) {
            version = null;
        }

        String warn = null;
        boolean dmDefined = tabPane.isEnabledAt(2);
        if (artifactList != null) {
            Color c = defaultVersionC;
            if (dmDefined) {
                if (findConflict(artifactList.getDMDeps(), gId, aId, version, null) == 1) {
                    c = Color.RED;
                    warn = NbBundle.getMessage(AddDependencyPanel.class, "MSG_VersionConflict");
                }
            }
            txtVersion.setForeground(c);
        }
        
        if (project.getDependencies() != null && gId != null && aId != null) {
            //poor mans expression evaluator, it's unlikely that some other expressions would be frequent
            String resolvedGroupId = gId.contains("${project.groupId}") ? gId.replace("${project.groupId}", project.getGroupId()) : gId;
            String resolvedArtifactId = aId.contains("${project.artifactId}") ? aId.replace("${project.artifactId}", project.getArtifactId()) : aId;
            
            for (Dependency dep : project.getDependencies()) {
                if (resolvedGroupId.equals(dep.getGroupId()) && resolvedArtifactId.equals(dep.getArtifactId())) {
                    warn = Bundle.MSG_Defined();
                }
                    
            }
        }
        
        if (nls != null) {
            if (warn != null) {
                nls.setWarningMessage(warn);
            } else {
                nls.clearMessages();
            }
        }

        if (gId == null) {
            okButton.setEnabled(false);
            return;
        }
        if (aId == null) {
            okButton.setEnabled(false);
            return;
        }
        if (version == null && !dmDefined) {
            okButton.setEnabled(false);
            return;
        }

        okButton.setEnabled(true);
    }

    private static Border getNbScrollPaneBorder () {
        Border b = UIManager.getBorder("Nb.ScrollPane.border");
        if (b == null) {
            Color c = UIManager.getColor("controlShadow");
            b = new LineBorder(c != null ? c : Color.GRAY);
        }
        return b;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblGroupId = new javax.swing.JLabel();
        txtGroupId = new javax.swing.JTextField();
        lblArtifactId = new javax.swing.JLabel();
        txtArtifactId = new javax.swing.JTextField();
        lblVersion = new javax.swing.JLabel();
        txtVersion = new javax.swing.JTextField();
        lblScope = new javax.swing.JLabel();
        comScope = new javax.swing.JComboBox();
        lblType = new javax.swing.JLabel();
        txtType = new javax.swing.JTextField();
        lblClassifier = new javax.swing.JLabel();
        txtClassifier = new javax.swing.JTextField();
        tabPane = new javax.swing.JTabbedPane();
        searchPanel = new javax.swing.JPanel();
        searchLabel = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        resultsLabel = new javax.swing.JLabel();
        resultsPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        chkNbOnly = new javax.swing.JCheckBox();
        pnlOpen = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        pnlOpenProjects = new javax.swing.JPanel();
        pnlDepMan = new javax.swing.JPanel();
        artifactsLabel = new javax.swing.JLabel();
        artifactPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();

        lblGroupId.setLabelFor(txtGroupId);
        org.openide.awt.Mnemonics.setLocalizedText(lblGroupId, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "LBL_GroupId")); // NOI18N

        lblArtifactId.setLabelFor(txtArtifactId);
        org.openide.awt.Mnemonics.setLocalizedText(lblArtifactId, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "LBL_ArtifactId")); // NOI18N

        lblVersion.setLabelFor(txtVersion);
        org.openide.awt.Mnemonics.setLocalizedText(lblVersion, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "LBL_Version")); // NOI18N

        lblScope.setLabelFor(comScope);
        org.openide.awt.Mnemonics.setLocalizedText(lblScope, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "LBL_Scope")); // NOI18N

        comScope.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "compile", "runtime", "test", "provided" }));

        lblType.setLabelFor(txtType);
        org.openide.awt.Mnemonics.setLocalizedText(lblType, NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.lblType.text")); // NOI18N

        lblClassifier.setLabelFor(txtClassifier);
        org.openide.awt.Mnemonics.setLocalizedText(lblClassifier, NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.lblClassifier.text")); // NOI18N

        searchPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                searchPanelComponentShown(evt);
            }
        });

        searchLabel.setLabelFor(searchField);
        org.openide.awt.Mnemonics.setLocalizedText(searchLabel, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.searchLabel.text", new Object[] {})); // NOI18N

        searchField.setText(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.searchField.text", new Object[] {})); // NOI18N

        jLabel1.setForeground(javax.swing.UIManager.getDefaults().getColor("textInactiveText"));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.jLabel1.text", new Object[] {})); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(resultsLabel, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.resultsLabel.text", new Object[] {})); // NOI18N

        resultsPanel.setBorder(getNbScrollPaneBorder());
        resultsPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 76, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 18, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(chkNbOnly, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.chkNbOnly.text")); // NOI18N
        chkNbOnly.setToolTipText(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.chkNbOnly.toolTipText")); // NOI18N
        chkNbOnly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkNbOnlyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchPanelLayout.createSequentialGroup()
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(searchLabel)
                        .addGap(4, 4, 4)
                        .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchPanelLayout.createSequentialGroup()
                                .addComponent(searchField, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(chkNbOnly))))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, searchPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(resultsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 554, Short.MAX_VALUE)
                            .addGroup(searchPanelLayout.createSequentialGroup()
                                .addComponent(resultsLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 261, Short.MAX_VALUE)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(106, 106, 106)))))
                .addContainerGap())
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchLabel)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkNbOnly))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resultsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resultsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
                .addContainerGap())
        );

        searchField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.searchField.AccessibleContext.accessibleDescription")); // NOI18N
        resultsLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.resultsLabel.AccessibleContext.accessibleDescription")); // NOI18N

        tabPane.addTab(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.searchPanel.TabConstraints.tabTitle", new Object[] {}), null, searchPanel, NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.searchPanel.TabConstraints.tabToolTip")); // NOI18N

        jLabel3.setLabelFor(pnlOpenProjects);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.jLabel3.text")); // NOI18N

        pnlOpenProjects.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout pnlOpenLayout = new javax.swing.GroupLayout(pnlOpen);
        pnlOpen.setLayout(pnlOpenLayout);
        pnlOpenLayout.setHorizontalGroup(
            pnlOpenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOpenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlOpenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlOpenProjects, javax.swing.GroupLayout.DEFAULT_SIZE, 554, Short.MAX_VALUE)
                    .addComponent(jLabel3))
                .addContainerGap())
        );
        pnlOpenLayout.setVerticalGroup(
            pnlOpenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOpenLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlOpenProjects, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabPane.addTab(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.pnlOpen.TabConstraints.tabTitle"), null, pnlOpen, NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.pnlOpen.TabConstraints.tabToolTip")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(artifactsLabel, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.artifactsLabel.text", new Object[] {})); // NOI18N

        artifactPanel.setBorder(getNbScrollPaneBorder());
        artifactPanel.setLayout(new java.awt.BorderLayout());

        jLabel2.setForeground(javax.swing.UIManager.getDefaults().getColor("textInactiveText"));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.jLabel2.text", new Object[] {})); // NOI18N
        artifactPanel.add(jLabel2, java.awt.BorderLayout.PAGE_END);

        javax.swing.GroupLayout pnlDepManLayout = new javax.swing.GroupLayout(pnlDepMan);
        pnlDepMan.setLayout(pnlDepManLayout);
        pnlDepManLayout.setHorizontalGroup(
            pnlDepManLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDepManLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDepManLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(artifactPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 554, Short.MAX_VALUE)
                    .addComponent(artifactsLabel))
                .addContainerGap())
        );
        pnlDepManLayout.setVerticalGroup(
            pnlDepManLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDepManLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(artifactsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(artifactPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabPane.addTab(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.pnlDepMan.TabConstraints.tabTitle", new Object[] {}), null, pnlDepMan, NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.pnlDepMan.TabConstraints.tabToolTip")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblGroupId)
                    .addComponent(lblArtifactId)
                    .addComponent(lblVersion)
                    .addComponent(lblType))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtArtifactId, javax.swing.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
                    .addComponent(txtGroupId, javax.swing.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(txtVersion, javax.swing.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblScope)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comScope, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtType, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblClassifier)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtClassifier, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(tabPane, javax.swing.GroupLayout.DEFAULT_SIZE, 583, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblGroupId)
                    .addComponent(txtGroupId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblArtifactId)
                    .addComponent(txtArtifactId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblVersion)
                    .addComponent(txtVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comScope, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblScope))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblType)
                    .addComponent(txtType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblClassifier)
                    .addComponent(txtClassifier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tabPane, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE))
        );

        txtGroupId.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.txtGroupId.AccessibleContext.accessibleDescription")); // NOI18N
        txtArtifactId.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.txtArtifactId.AccessibleContext.accessibleDescription")); // NOI18N
        txtVersion.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.txtVersion.AccessibleContext.accessibleDescription")); // NOI18N
        comScope.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.comScope.AccessibleContext.accessibleDescription")); // NOI18N
        tabPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.tabPane.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void searchPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_searchPanelComponentShown
        searchField.requestFocus();
    }//GEN-LAST:event_searchPanelComponentShown

    private void chkNbOnlyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkNbOnlyActionPerformed
        queryPanel.stateChanged(new ChangeEvent(searchField.getDocument()));
    }//GEN-LAST:event_chkNbOnlyActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel artifactPanel;
    private javax.swing.JLabel artifactsLabel;
    private javax.swing.JCheckBox chkNbOnly;
    private javax.swing.JComboBox comScope;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblArtifactId;
    private javax.swing.JLabel lblClassifier;
    private javax.swing.JLabel lblGroupId;
    private javax.swing.JLabel lblScope;
    private javax.swing.JLabel lblType;
    private javax.swing.JLabel lblVersion;
    private javax.swing.JPanel pnlDepMan;
    private javax.swing.JPanel pnlOpen;
    private javax.swing.JPanel pnlOpenProjects;
    private javax.swing.JLabel resultsLabel;
    private javax.swing.JPanel resultsPanel;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JTabbedPane tabPane;
    private javax.swing.JTextField txtArtifactId;
    private javax.swing.JTextField txtClassifier;
    private javax.swing.JTextField txtGroupId;
    private javax.swing.JTextField txtType;
    private javax.swing.JTextField txtVersion;
    // End of variables declaration//GEN-END:variables
    // End of variables declaration

    private Result<String> populateGroupId() {
        assert !SwingUtilities.isEventDispatchThread();
        final Result<String> result = RepositoryQueries.getGroupsResult(RepositoryPreferences.getInstance().getRepositoryInfos());
        final List<String> lst = new ArrayList<String>(result.getResults());
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                groupCompleter.setValueList(lst, result.isPartial());
            }
        });
        return result;
    }

    private Result<String> populateArtifact(String groupId) {
        assert !SwingUtilities.isEventDispatchThread();
        final Result<String> result = RepositoryQueries.getArtifactsResult(groupId, RepositoryPreferences.getInstance().getRepositoryInfos());
        final List<String> lst = new ArrayList<String>(result.getResults());
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                artifactCompleter.setValueList(lst, result.isPartial());
            }
        });
        return result;
    }

    private Result<NBVersionInfo> populateVersion(String groupId, String artifactId) {
        assert !SwingUtilities.isEventDispatchThread();
        final Result<NBVersionInfo> result = RepositoryQueries.getVersionsResult(groupId, artifactId, RepositoryPreferences.getInstance().getRepositoryInfos());
        List<NBVersionInfo> lst = result.getResults();
        final List<String> vers = new ArrayList<String>();
        for (NBVersionInfo rec : lst) {
            if (!vers.contains(rec.getVersion())) {
                vers.add(rec.getVersion());
            }
        }
        // also include properties/expressions that could be related to version
        // management
        List<String> propList = new ArrayList<String>();
        for (Object propKey : project.getProperties().keySet()) {
            String key = (String)propKey;
            if (key.endsWith(".version")) { //NOI18N
                // is this the correct heuristics?
                propList.add("${" + key + "}");
            }
        }
        Collections.sort(propList);
        vers.addAll(propList);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                versionCompleter.setValueList(vers, result.isPartial());
            }
        });
        return result;
    }

    private static List<Dependency> getDependenciesFromDM(MavenProject project, Project nbprj) {
        NbMavenProjectImpl p = nbprj.getLookup().lookup(NbMavenProjectImpl.class);
        MavenProject localProj = project;
        DependencyManagement curDM;
        List<Dependency> result = new ArrayList<Dependency>();
        //mkleint: without the managementKey checks I got some entries multiple times.
        // do we actually need to traverse the parent poms, are they completely resolved anyway?
        //XXX
        Set<String> knownKeys = new HashSet<String>();

        while (localProj != null) {
            curDM = localProj.getDependencyManagement();
            if (curDM != null) {
                @SuppressWarnings("unchecked")
                List<Dependency> ds = curDM.getDependencies();
                for (Dependency d : ds) {
                    if (knownKeys.contains(d.getManagementKey())) {
                        continue;
                    }
                    result.add(d);
                    knownKeys.add(d.getManagementKey());
                }
            }
            try {
                localProj = p.loadParentOf(EmbedderFactory.getProjectEmbedder(), localProj);
                if (localProj == null || NbMavenProject.isErrorPlaceholder(localProj)) {
                    break;
                }
            } catch (ProjectBuildingException x) {
                break;
            }
        }
        result.sort(new Comparator<Dependency>() {

            @Override
            public int compare(Dependency o1, Dependency o2) {
                return o1.getManagementKey().compareTo(o2.getManagementKey());
            }
        });
        return result;
    }

    /**
     * @return 0 -> no conflicts, 1 -> conflict in version, 2 -> conflict in scope
     */
    private static int findConflict (List<Dependency> deps, String groupId, String artifactId, String version, String scope) {
        if (deps == null) {
            return 0;
        }
        for (Dependency dep : deps) {
            if (artifactId != null && artifactId.equals(dep.getArtifactId()) &&
                    groupId != null && groupId.equals(dep.getGroupId())) {
                if (version != null && !version.equals(dep.getVersion())) {
                    return 1;
                }
                if (scope != null) {
                    if (!scope.equals(dep.getScope())) {
                        return 2;
                    }
                } else if (dep.getScope() != null) {
                    return 2;
                }

            }
        }

        return 0;
    }

    private void setFields(String groupId, String artifactId, String version, String type, String classifier) {
        boolean sameGrId = false;
        if (groupId != null && groupId.equals(project.getGroupId())) {
            groupId = "${project.groupId}"; //NOI18N
            sameGrId = true;
        }
        txtGroupId.setText(groupId);
        txtArtifactId.setText(artifactId);
        if (sameGrId && version != null && version.equals(project.getVersion())) {
            version = "${project.version}"; //NOI18N
        }
        txtVersion.setText(version);
        if (type != null) {
            if (type.equals("jar") || 
                    (("nbm".equals(project.getPackaging()) || "nbm-application".equals(project.getPackaging())) && type.equals("nbm"))) {//NOI18N
                type = null;
            }
        }
        txtType.setText(type);
        txtClassifier.setText(classifier);
    }

    private static Node noResultsNode, searchingNode, tooGeneralNode;

    private static Node getNoResultsNode() {
        if (noResultsNode == null) {
            AbstractNode nd = new AbstractNode(Children.LEAF) {

                @Override
                public Image getIcon(int arg0) {
                    return ImageUtilities.loadImage(EMPTY_ICON); //NOI18N
                    }

                @Override
                public Image getOpenedIcon(int arg0) {
                    return getIcon(arg0);
                }
            };
            nd.setName("Empty"); //NOI18N

            nd.setDisplayName(NbBundle.getMessage(AddDependencyPanel.class, "LBL_Node_Empty")); //NOI18N

            noResultsNode = nd;
        }

        return new FilterNode (noResultsNode, Children.LEAF);
    }

    private static Node getSearchingNode() {
        if (searchingNode == null) {
            AbstractNode nd = new AbstractNode(Children.LEAF) {

                @Override
                public Image getIcon(int arg0) {
                    return ImageUtilities.loadImage(WAIT_ICON); //NOI18N
                }

                @Override
                public Image getOpenedIcon(int arg0) {
                    return getIcon(arg0);
                }
            };
            nd.setName("Searching"); //NOI18N

            nd.setDisplayName(NbBundle.getMessage(AddDependencyPanel.class, "LBL_Node_Searching")); //NOI18N

            searchingNode = nd;
        }

        return new FilterNode (searchingNode, Children.LEAF);
    }
    
    private static Node getTooGeneralNode() {
        if (tooGeneralNode == null) {
            AbstractNode nd = new AbstractNode(Children.LEAF) {

                @Override
                public Image getIcon(int arg0) {
                    return ImageUtilities.loadImage(EMPTY_ICON); //NOI18N
                    }

                @Override
                public Image getOpenedIcon(int arg0) {
                    return getIcon(arg0);
                }
            };
            nd.setName("Too General"); //NOI18N

            nd.setDisplayName(NbBundle.getMessage(AddDependencyPanel.class, "LBL_Node_TooGeneral")); //NOI18N

            tooGeneralNode = nd;
        }

        return new FilterNode (tooGeneralNode, Children.LEAF);
    }
    
    private class ResultsRootNode extends AbstractNode {

        private ResultsRootChildren resultsChildren;

        public ResultsRootNode() {
            this(new InstanceContent());
        }

        private ResultsRootNode(InstanceContent content) {
            super (new ResultsRootChildren(), new AbstractLookup(content));
            content.add(this);
            this.resultsChildren = (ResultsRootChildren) getChildren();
        }

        public void setOneChild(Node n) {
            List<Node> ch = new ArrayList<Node>(1);
            ch.add(n);
            setNewChildren(ch);
        }
        
        public void setNewChildren(List<Node> ch) {
            resultsChildren.setNewChildren (ch);
        }
    }
    
    private class ResultsRootChildren extends Children.Keys<Node> {
        
        List<Node> myNodes;

        public ResultsRootChildren() {
            myNodes = Collections.EMPTY_LIST;
        }

        private void setNewChildren(List<Node> ch) {
            myNodes = ch;
            refreshList();
        }

        @Override
        protected void addNotify() {
            refreshList();
        }

        private void refreshList() {
            List<Node> keys = new ArrayList<>();
            for (Node node : myNodes) {
                keys.add(node);
            }
            setKeys(keys);
        }

        @Override
        protected Node[] createNodes(Node key) {
            return new Node[] { key };
        }

    }

    private static final Object LOCK = new Object();

    private class QueryPanel extends JPanel implements ExplorerManager.Provider,
            Comparator<String>, PropertyChangeListener, ChangeListener {
        

        private final BeanTreeView btv;
        private final ExplorerManager manager;
        private final ResultsRootNode resultsRootNode;

        private String inProgressText, lastQueryText, curTypedText;

        private final Color defSearchC;

        private QueryPanel() {
            btv = new BeanTreeView();
            btv.setRootVisible(false);
            btv.setDefaultActionAllowed(true);
            btv.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            manager = new ExplorerManager();
            setLayout(new BorderLayout());
            add(btv, BorderLayout.CENTER);
            defSearchC = AddDependencyPanel.this.searchField.getForeground();
            manager.addPropertyChangeListener(this);
            AddDependencyPanel.this.resultsLabel.setLabelFor(btv);
            btv.getAccessibleContext().setAccessibleDescription(AddDependencyPanel.this.resultsLabel.getAccessibleContext().getAccessibleDescription());
            resultsRootNode = new ResultsRootNode();
            manager.setRootContext(resultsRootNode);
        }

        /** delayed change of query text */
        @Override
        public void stateChanged (ChangeEvent e) {
            Document doc = (Document)e.getSource();
            try {
                curTypedText = doc.getText(0, doc.getLength()).trim();
            } catch (BadLocationException ex) {
                // should never happen, nothing we can do probably
                return;
            }

            AddDependencyPanel.this.searchField.setForeground(defSearchC);

            if (curTypedText.length() > 0) {
                find(curTypedText);
            }
        }
        
                private boolean cancel() {
                    synchronized (LOCK) {
                        if (lastQueryText != null && !lastQueryText.equals(inProgressText)) {
                            return true; //we no longer care
                        }
                    }
                    return false;
                }
        

        @Messages({"MSG_ClassesExcluded=Too general query. Class names excluded from the search.",
                   "# {0} - number",
                   "# {1} - total number",
                   "MSG_Narrow=Only {0} of {1} results shown. Consider narrowing your search."
                  })
        void find(String queryText) {
            synchronized (LOCK) {
                if (inProgressText != null) {
                    lastQueryText = queryText;
                    // stop waiting for results of the previous search
                    //TODO we want to have the current task cancelled and new one started.
                    //currently we wait for the first one to finish, which takes forever in some cases.
                    
                    return;
                }
                inProgressText = queryText;
                lastQueryText = null;
            }

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    resultsRootNode.setOneChild(getSearchingNode());
                    AddDependencyPanel.this.searchField.setForeground(defSearchC);
                    AddDependencyPanel.this.nls.clearMessages();
                }
            });

            final List<QueryField> fields = new ArrayList<QueryField>();
            final List<QueryField> fieldsNonClasses = new ArrayList<QueryField>();
            String q = queryText.trim();
            String[] splits = q.split(" "); //NOI118N

            List<String> fStrings = new ArrayList<String>();
            fStrings.add(QueryField.FIELD_GROUPID);
            fStrings.add(QueryField.FIELD_ARTIFACTID);
            fStrings.add(QueryField.FIELD_VERSION);
            fStrings.add(QueryField.FIELD_NAME);
            fStrings.add(QueryField.FIELD_DESCRIPTION);
            fStrings.add(QueryField.FIELD_CLASSES);

            for (String curText : splits) {
                for (String fld : fStrings) {
                    QueryField f = new QueryField();
                    f.setField(fld);
                    f.setValue(curText);
                    fields.add(f);
                    if (!QueryField.FIELD_CLASSES.equals(fld)) {
                        fieldsNonClasses.add(f);
                    }
                }
            }
            

            Task t = RPofQueryPanel.post(new Runnable() {
                
                @Override
                public void run() {
                    if (cancel()) return;//we no longer care
                    //first try with classes search included,
                    try {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                AddDependencyPanel.this.nls.setInformationMessage(null); //NOI18N
                            }
                        });
                        final Result<NBVersionInfo> result = RepositoryQueries.findResult(fields, RepositoryPreferences.getInstance().getRepositoryInfos());
                        if (cancel()) return;//we no longer care
                        updateResults(result.getResults(), result.isPartial());
                        if (result.isPartial()) {
                            if (cancel()) return;//we no longer care
                            result.waitForSkipped();
                            if (cancel()) return;//we no longer care
                            updateResults(result.getResults(), false);
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (result.getReturnedResultCount() < result.getTotalResultCount()) {
                                    AddDependencyPanel.this.nls.setInformationMessage(MSG_Narrow(result.getReturnedResultCount(), result.getTotalResultCount()));
                                }
                            }
                        });
                        
                    } catch (BooleanQuery.TooManyClauses exc) {
                        if (cancel()) return;//we no longer care
                        // if failing, then exclude classes from search..
                        try {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    AddDependencyPanel.this.nls.setInformationMessage(MSG_ClassesExcluded());
                                }
                            });
                            Result<NBVersionInfo> result = RepositoryQueries.findResult(fieldsNonClasses, RepositoryPreferences.getInstance().getRepositoryInfos());
                            if (cancel()) return;//we no longer care
                            updateResults(result.getResults(), result.isPartial());
                            if (result.isPartial()) {
                                result.waitForSkipped();
                                if (cancel()) return;//we no longer care
                                updateResults(result.getResults(), false);
                            }
                        } catch (BooleanQuery.TooManyClauses exc2) {
                            // if still failing, report to the user
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    AddDependencyPanel.this.searchField.setForeground(Color.RED);
                                    AddDependencyPanel.this.nls.setWarningMessage(NbBundle.getMessage(AddDependencyPanel.class, "MSG_TooGeneral")); //NOI18N
                                    resultsRootNode.setOneChild(getTooGeneralNode());
                                }
                            });
                        }
                    } catch (OutOfMemoryError oome) {
                        // running into OOME may still happen in Lucene despite the fact that
                        // we are trying hard to prevent it in NexusRepositoryIndexerImpl
                        // (see #190265)
                        // in the bad circumstances theoretically any thread may encounter OOME
                        // but most probably this thread will be it
                        // trying to indicate the condition to the user here
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                AddDependencyPanel.this.searchField.setForeground(Color.RED);
                                AddDependencyPanel.this.nls.setWarningMessage(NbBundle.getMessage(AddDependencyPanel.class, "MSG_TooGeneral")); //NOI18N
                                resultsRootNode.setOneChild(getTooGeneralNode());
                            }
                        });
                    }
                }
            });

            t.addTaskListener(new TaskListener() {

                @Override
                public void taskFinished(Task task) {
                    synchronized (LOCK) {
                        String localText = inProgressText;
                        inProgressText = null;
                        if (lastQueryText != null && !lastQueryText.equals(localText)) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    if (lastQueryText != null) {
                                        find(lastQueryText);
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }

        @Override
        public ExplorerManager getExplorerManager() {
            return manager;
        }

        void updateResults(List<NBVersionInfo> infos, final boolean partial) {
            final Map<String, List<NBVersionInfo>> map = new HashMap<String, List<NBVersionInfo>>();

            if (infos != null) {
                if (chkNbOnly.isSelected()) { // #181656: show only NB modules
                    List<NBVersionInfo> refined = new ArrayList<NBVersionInfo>();
                    Map<String, NBVersionInfo> check = new HashMap<String, NBVersionInfo>(); // class index works only on JAR artifacts
                    Set<String> found = new HashSet<String>(); // but search string might also be found in other fields
                    for (NBVersionInfo nbvi : infos) {
                        String key = key(nbvi);
                        if (NbMavenProject.TYPE_NBM.equals(nbvi.getPackaging())) {
                            refined.add(nbvi);
                            found.add(key);
                        } else {
                            check.put(key, nbvi);
                        }
                    }
                    final Result<String> findResult = RepositoryQueries.getGAVsForPackaging(NbMavenProject.TYPE_NBM, RepositoryPreferences.getInstance().getRepositoryInfos());
                    for (String alt : findResult.getResults()) {
                        if (check.containsKey(alt) && !found.contains(alt)) {
                            refined.add(check.get(alt));
                        }
                    }
                    Collections.sort(refined);
                    infos = refined;
                }
                for (NBVersionInfo nbvi : infos) {
                    String key = nbvi.getGroupId() + " : " + nbvi.getArtifactId(); //NOI18n
                    List<NBVersionInfo> get = map.get(key);
                    if (get == null) {
                        get = new ArrayList<NBVersionInfo>();
                        map.put(key, get);
                    }
                    get.add(nbvi);
                }
            }

            final List<String> keyList = new ArrayList<String>(map.keySet());
            // sort specially using our comparator, see compare method
            keyList.sort(QueryPanel.this);

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateResultNodes(keyList, map, partial);
                }
            });
        }


        private void updateResultNodes(List<String> keyList, Map<String, List<NBVersionInfo>> map, boolean partial) {

            if (keyList.size() > 0) { // some results available
                
                Map<String, Node> currentNodes = new HashMap<String, Node>();
                for (Node nd : resultsRootNode.getChildren().getNodes()) {
                    currentNodes.put(nd.getName(), nd);
                }
                List<Node> newNodes = new ArrayList<Node>(keyList.size());

                    // still searching?
                if (partial)
                    newNodes.add(getSearchingNode());
                
                for (String key : keyList) {
                    Node nd;
                    nd = currentNodes.get(key);
                    if (null != nd) {
                        ((MavenNodeFactory.ArtifactNode)((FilterNodeWithDefAction)nd).getOriginal()).setVersionInfos(map.get(key));
                    } else {
                        nd = createFilterWithDefaultAction(MavenNodeFactory.createArtifactNode(key, map.get(key)), false);
                    }
                    newNodes.add(nd);
                }
                
                resultsRootNode.setNewChildren(newNodes);
            } else if (partial) { // still searching, no results yet
                resultsRootNode.setOneChild(getSearchingNode());
            } else { // finished searching with no results
                resultsRootNode.setOneChild(getNoResultsNode());
            }
        }

        /** Impl of comparator, sorts artifacts asfabetically with exception
         * of items that contain current query string, which take precedence.
         */
        @Override
        public int compare(String s1, String s2) {

            int index1 = s1.indexOf(inProgressText);
            int index2 = s2.indexOf(inProgressText);

            if (index1 >= 0 || index2 >=0) {
                if (index1 < 0) {
                    return 1;
                } else if (index2 < 0) {
                    return -1;
                }
                return s1.compareTo(s2);
            } else {
                return s1.compareTo(s2);
            }
        }

        /** PropertyChangeListener impl, stores maven coordinates of selected artifact */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                Node[] selNodes = manager.getSelectedNodes();
                changeSelection(selNodes.length == 1 ? selNodes[0].getLookup() : Lookup.EMPTY);
            }
        }

        private String key(NBVersionInfo nbvi) {
            return nbvi.getGroupId() + ':' + nbvi.getArtifactId() + ':' + nbvi.getVersion();
        }

    } // QueryPanel

    private static final Object DM_DEPS_LOCK = new Object();
    private class DMListPanel extends JPanel implements ExplorerManager.Provider,
            AncestorListener, ActionListener, PropertyChangeListener, Runnable {

        private final BeanTreeView btv;
        private final ExplorerManager manager;
        private final MavenProject project;
        private Node noDMRoot;

        private List<Dependency> dmDeps;

        public DMListPanel(MavenProject project) {
            this.project = project;
            btv = new BeanTreeView();
            btv.setRootVisible(false);
            btv.setDefaultActionAllowed(true);
            //lv.setDefaultProcessor(this);
            manager = new ExplorerManager();
            manager.addPropertyChangeListener(this);
            setLayout(new BorderLayout());
            add(btv, BorderLayout.CENTER);
            addAncestorListener(this);
            AddDependencyPanel.this.artifactsLabel.setLabelFor(btv);

            // disable tab if DM section not defined
            RPofDMListPanel.post(this);
        }

        @Override
        public ExplorerManager getExplorerManager() {
            return manager;
        }

        private NBVersionInfo convert2VInfo(Dependency dep) {
            return new NBVersionInfo(null, dep.getGroupId(), dep.getArtifactId(),
                    dep.getVersion(), dep.getType(), null, null, null, dep.getClassifier());
        }

        private List<Dependency> getDMDeps() {
            synchronized (DM_DEPS_LOCK) {
                return dmDeps;
            }
        }

        private void loadArtifacts() {
            List<Dependency> deps = getDMDeps();
            if (deps == null || deps.isEmpty()) {
                if (noDMRoot == null) {
                    AbstractNode nd = new AbstractNode(Children.LEAF) {
                        @Override
                        public Image getIcon(int arg0) {
                            return ImageUtilities.loadImage(EMPTY_ICON); //NOI18N
                        }
                        @Override
                        public Image getOpenedIcon(int arg0) {
                            return getIcon(arg0);
                        }
                    };
                    nd.setName("Empty"); //NOI18N
                    nd.setDisplayName(NbBundle.getMessage(AddDependencyPanel.class, "LBL_DM_Empty"));
                    Children.Array array = new Children.Array();
                    array.add(new Node[]{nd});
                    noDMRoot = new AbstractNode(array);
                }
                manager.setRootContext(noDMRoot);
            } else {
                Children.Array array = new Children.Array();
                Node root = new AbstractNode(array);
                for (Dependency dep : deps) {
                    array.add(new Node[]{ createFilterWithDefaultAction(MavenNodeFactory.createVersionNode(convert2VInfo(dep), true), true) });
                }
                manager.setRootContext(root);
            }
        }

        @Override
        public void ancestorAdded(AncestorEvent event) {
            loadArtifacts();
        }

        @Override
        public void ancestorRemoved(AncestorEvent event) {
        }

        @Override
        public void ancestorMoved(AncestorEvent event) {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Node[] selNodes = manager.getSelectedNodes();
            changeSelection(selNodes.length == 1 ? selNodes[0].getLookup() : Lookup.EMPTY);
        }

        /** Loads dependencies outside EQ thread, updates tab state in EQ */
        @Override
        public void run() {
            synchronized (DM_DEPS_LOCK) {
                dmDeps = getDependenciesFromDM(project, AddDependencyPanel.this.nbProject);
            }
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    boolean dmEmpty = dmDeps.isEmpty();
                    tabPane.setEnabledAt(2, !dmEmpty);
                }
            });
        }

    }


    private class OpenListPanel extends JPanel implements ExplorerManager.Provider,
            PropertyChangeListener, Runnable {

        private final BeanTreeView btv;
        private final ExplorerManager manager;
        private final Project project;

        public OpenListPanel(Project project) {
            this.project = project;
            btv = new BeanTreeView();
            btv.setRootVisible(false);
            btv.setDefaultActionAllowed(true);
            manager = new ExplorerManager();
            manager.addPropertyChangeListener(this);
            setLayout(new BorderLayout());
            add(btv, BorderLayout.CENTER);

            RPofOpenListPanel.post(this);
        }

        @Override
        public ExplorerManager getExplorerManager() {
            return manager;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Node[] selNodes = manager.getSelectedNodes();
            changeSelection(selNodes.length == 1 ? selNodes[0].getLookup() : Lookup.EMPTY);
        }

        /** Loads dependencies outside EQ thread, updates tab state in EQ */
        @Override
        public void run() {
            Project[] prjs = OpenProjects.getDefault().getOpenProjects();
            final List<Node> toRet = new ArrayList<Node>();
            for (Project p : prjs) {
                if (p == project) {
                    continue;
                }
                NbMavenProject mav = p.getLookup().lookup(NbMavenProject.class);
                if (mav != null) {
                    boolean continueProjectIteration = false;
                    MavenProject mavenProject = mav.getMavenProject();
                    Iterator<Dependency> iterator = project.getLookup().lookup(NbMavenProject.class).getMavenProject().getDependencies().iterator();
                    while (iterator.hasNext()) {
                        Dependency dependency = iterator.next();
                        if (mavenProject.getGroupId().equals(dependency.getGroupId())
                                && mavenProject.getArtifactId().equals(dependency.getArtifactId())) {
                            continueProjectIteration = true;
                            break;
                        }
                    }
                    if ( continueProjectIteration ) {
                        continue;
                    }
                    LogicalViewProvider lvp = p.getLookup().lookup(LogicalViewProvider.class);
                    toRet.add(createFilterWithDefaultAction(lvp.createLogicalView(), true));
                }
            }
            Children.Array ch = new Children.Array();
            ch.add(toRet.toArray(new Node[0]));
            Node root = new AbstractNode(ch);
            getExplorerManager().setRootContext(root);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    boolean opEmpty = toRet.isEmpty();
                    tabPane.setEnabledAt(1, !opEmpty);
                }
            });
        }

    }

    private class DefAction extends AbstractAction implements ContextAwareAction {
        private final boolean close;
        private final Lookup lookup;

        public DefAction(boolean closeNow, Lookup look) {
            this.close = closeNow;
            lookup = look;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Project prj = lookup.lookup(Project.class);
            boolean set = false;
            if (prj != null) {
                NbMavenProject mav = prj.getLookup().lookup(NbMavenProject.class);
                MavenProject m = mav.getMavenProject();
                AddDependencyPanel.this.setFields(m.getGroupId(), m.getArtifactId(), m.getVersion(), null, null);
                set = true;
            }
            if (!set) {
                NBVersionInfo vi = lookup.lookup(NBVersionInfo.class);
                if (vi != null) {
                    //in dm panel we want to pass empty version
                    boolean isDM = lookup.lookup(DependencyManagement.class) != null;
                    String ver =  isDM ?  "" : vi.getVersion();
                    String type = isDM ? "" : vi.getType();
                    String classifier = isDM ? "" : vi.getClassifier();
                    AddDependencyPanel.this.setFields(vi.getGroupId(), vi.getArtifactId(), ver, type, classifier);
                    set = true;
                }
            }
            if (set) {
                if (close) {
                    AddDependencyPanel.this.getOkButton().doClick();
                } else {
                    //reset completion.
                    AddDependencyPanel.this.artifactCompleter.setLoading(true);
                    AddDependencyPanel.this.versionCompleter.setLoading(true);
                    final String groupId = txtGroupId.getText().trim();
                    final String artifactId = txtArtifactId.getText().trim();
                    RP.post(new Runnable() {
                        @Override public void run() {
                            populateArtifact(groupId);
                            populateVersion(groupId, artifactId);
                        }
                    });
                }
            } else {
                AddDependencyPanel.this.setFields("", "", "", "", ""); //NOI18N
                //reset completion.
                AddDependencyPanel.this.artifactCompleter.setValueList(Collections.<String>emptyList(), false);
                AddDependencyPanel.this.versionCompleter.setValueList(Collections.<String>emptyList(), false);
            }
        }

        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            return new DefAction(close, actionContext);
        }

    }

    private void changeSelection(Lookup context) {
        new DefAction(false, context).actionPerformed(null);
    }

    private Node createFilterWithDefaultAction(final Node nd, boolean leaf) {
        return new FilterNodeWithDefAction (nd, leaf);
    }

    class FilterNodeWithDefAction extends FilterNode {

        public FilterNodeWithDefAction(Node nd, boolean leaf) {
            super(nd, leaf ? Children.LEAF : new FilterNode.Children(nd) {
                @Override
                protected Node[] createNodes(Node key) {
                    return new Node[]{createFilterWithDefaultAction(key, true)};
                }
            });
        }

        @Override
        public Action getPreferredAction() {
            return super.getPreferredAction();
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[0];
        }
        
        @Override
        public Node getOriginal() {
            return super.getOriginal();
        }
    }
}
