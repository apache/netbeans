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

package org.netbeans.modules.maven.graph;

import org.netbeans.modules.java.graph.DependencyGraphScene;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.InvalidArtifactRTException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import static org.netbeans.modules.maven.graph.Bundle.*;
import org.netbeans.modules.maven.indexer.api.ui.ArtifactViewer;
import org.netbeans.modules.maven.indexer.spi.ui.ArtifactViewerFactory;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.spi.IconResources;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/**
 * component showing graph of dependencies for project.
 * @author Milos Kleint 
 */
public class DependencyGraphTopComponent extends TopComponent implements LookupListener, MultiViewElement, MouseWheelListener {

    private static final @StaticResource String ZOOM_IN_ICON = "org/netbeans/modules/maven/graph/zoomin.gif";
    private static final @StaticResource String ZOOM_OUT_ICON = "org/netbeans/modules/maven/graph/zoomout.gif";
    private static final int COMPLEXITY_LIMIT = 30; //number of dependencies that are safe to show immediately
//    public static final String ATTRIBUTE_DEPENDENCIES_LAYOUT = "MavenProjectDependenciesLayout"; //NOI18N
    private static final Logger LOG = Logger.getLogger(DependencyGraphTopComponent.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(DependencyGraphTopComponent.class);
    private boolean everDisplayed;
    private boolean needsRefresh;
    private final RequestProcessor.Task task_reload = RP.create(new Runnable() {
        @Override
        public void run() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Iterator<? extends MavenProject> it2 = result2.allInstances().iterator();
                    final MavenProject prj = it2.hasNext() ? it2.next() : null;
                    if (prj != null && NbMavenProject.isErrorPlaceholder(prj)) {
                        setPaneText(Err_CannotLoad(), false);
                        return;
                    }
                    if (prj != null) {
                        if (isVisible() || prj.getArtifacts().size() < COMPLEXITY_LIMIT) {
                            btnGraphActionPerformed(null);
                            return;
                        } else {
                            needsRefresh = true;
                        }
                    }
                }
            });
        }
    });
    
    private final Map<Artifact, Icon> projectIcons;
    
    @MultiViewElement.Registration(
        displayName="#TAB_Graph",
        iconBase=IconResources.ICON_DEPENDENCY_JAR,
        persistenceType=TopComponent.PERSISTENCE_NEVER,
        preferredID=ArtifactViewer.HINT_GRAPH,
        mimeType=Constants.POM_MIME_TYPE,
        position=100
    )
    @Messages("TAB_Graph=Graph")
    public static MultiViewElement forPOM(final Lookup editor) {
        class L extends ProxyLookup implements PropertyChangeListener {
            Project p;
            L() {
                FileObject pom = editor.lookup(FileObject.class);
                if (pom != null) {
                    p = FileOwnerQuery.getOwner(pom);
                    if (p != null) {
                        NbMavenProject nbmp = p.getLookup().lookup(NbMavenProject.class);
                        if (nbmp != null) {
                            nbmp.addPropertyChangeListener(WeakListeners.propertyChange(this, nbmp));
                            reset();
                        } else {
                            LOG.log(Level.WARNING, "not a Maven project: {0}", p);
                        }
                    } else {
                        LOG.log(Level.WARNING, "no owner of {0}", pom);
                    }
                } else {
                    LOG.log(Level.WARNING, "no FileObject in {0}", editor);
                }
            }
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                    reset();
                }
            }
            private void reset() {
                ArtifactViewerFactory avf = Lookup.getDefault().lookup(ArtifactViewerFactory.class);
                if (avf != null) {
                    Lookup l = null;
                    try {
                        l = avf.createLookup(p);
                    } catch (InvalidArtifactRTException e) {
                        // issue #258898 
                        LOG.log(Level.WARNING, "problems while creating lookup for {"  + p + "} : " + e.getMessage(), e);
                    }
                    if (l != null) {
                        setLookups(l);
                    } else {
                        LOG.log(Level.WARNING, "no artifact lookup for {0}", p);
                    }
                } else {
                    LOG.warning("no ArtifactViewerFactory found");
                }
            }
        }
        return new DependencyGraphTopComponent(new L());
    }

//    private Project project;
    private Lookup.Result<org.apache.maven.shared.dependency.tree.DependencyNode> result;
    private Lookup.Result<MavenProject> result2;
    private Lookup.Result<POMModel> result3;

    private DependencyGraphScene<MavenDependencyNode> scene;
    private MultiViewElementCallback callback;
    final JScrollPane pane = new JScrollPane();
    
    private Timer timer = new Timer(500, new ActionListener() {
        @Override public void actionPerformed(ActionEvent arg0) {
            checkFindValue();
        }
    });
    private JToolBar toolbar;
    
    @Messages({
        "LBL_Scope_All=All",
        "LBL_Scope_Compile=Compile",
        "LBL_Scope_Runtime=Runtime",
        "LBL_Scope_Test=Test"
    })
    public DependencyGraphTopComponent(Lookup lookup) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, hashCode() + " created: " + lookup, new Exception());
        }
        projectIcons = getIconsForOpenProjects();
        associateLookup(lookup);
        initComponents();
//        project = proj;
        //sldDepth.getLabelTable().put(0, new JLabel(LBL_All())); LBL_All=All
        timer.setDelay(500);
        timer.setRepeats(false);
        txtFind.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent arg0) {
                timer.restart();
            }

            @Override public void removeUpdate(DocumentEvent arg0) {
                timer.restart();
            }

            @Override public void changedUpdate(DocumentEvent arg0) {
                timer.restart();
            }
        });
        comScopes.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                int scopesSize = ((List<?>) value).size();
                String msg;
                if (scopesSize == 0) {
                    msg = LBL_Scope_All();
                } else if (scopesSize == 2) {
                    msg = LBL_Scope_Compile();
                } else if (scopesSize == 3) {
                    msg = LBL_Scope_Runtime();
                } else {
                    msg = LBL_Scope_Test();
                }
                return super.getListCellRendererComponent(list, msg, index, isSelected, cellHasFocus);
            }
        });
        DefaultComboBoxModel<List<String>> mdl = new DefaultComboBoxModel<>();
        mdl.addElement(Arrays.asList(new String[0]));
        mdl.addElement(Arrays.asList(new String[] {
            Artifact.SCOPE_PROVIDED,
            Artifact.SCOPE_COMPILE
        }));
        mdl.addElement(Arrays.asList(new String[] {
            Artifact.SCOPE_PROVIDED,
            Artifact.SCOPE_COMPILE,
            Artifact.SCOPE_RUNTIME
        }));
        mdl.addElement(Arrays.asList(new String[] {
            Artifact.SCOPE_PROVIDED,
            Artifact.SCOPE_COMPILE,
            Artifact.SCOPE_RUNTIME,
            Artifact.SCOPE_TEST
        }));
        comScopes.setModel(mdl);
        comScopes.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (scene != null) {
                    @SuppressWarnings("unchecked")
                    List<String> selected = (List<String>) comScopes.getSelectedItem();
                    ScopesVisitor vis = new ScopesVisitor(scene, selected);
                    vis.accept(scene.getRootGraphNode().getImpl());
                    scene.validate();
                    scene.repaint();
                    revalidate();
                    repaint();
                }
            }
        });
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
            setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
        }
    }
    
    private void checkFindValue() {
        String val = txtFind.getText().trim();
        if ("".equals(val)) { //NOI18N
            val = null;
        }
        scene.setSearchString(val);
    }
    
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    @Override public void componentOpened() {
        super.componentOpened();
        pane.setWheelScrollingEnabled(true);
        add(pane, BorderLayout.CENTER);
        result = getLookup().lookupResult(org.apache.maven.shared.dependency.tree.DependencyNode.class);
        result.addLookupListener(this);
        result2 = getLookup().lookupResult(MavenProject.class);
        result2.addLookupListener(this);
        result3 = getLookup().lookupResult(POMModel.class);
        result3.addLookupListener(this);
        waitForApproval();
    }
    
    @Override
    public void componentActivated() {
        super.componentActivated();
        if(needsRefresh) {
            needsRefresh = false;
            btnGraphActionPerformed(null);
        }
    }

    /**
     * Adds key bindings to move the graph with the cursor-keys around.
     * Zoom-in/-out with CTRL + + and CTRL + - or the mouse wheel.
     */
    public void addKeyboardBindings() {

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK), "zoomIn");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, KeyEvent.CTRL_DOWN_MASK), "zoomIn");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK), "zoomOut");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, KeyEvent.CTRL_DOWN_MASK), "zoomOut");

        getActionMap().put("left", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pane.getHorizontalScrollBar().setValue(pane.getHorizontalScrollBar().getValue() - 10);
            }
        });

        getActionMap().put("right", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pane.getHorizontalScrollBar().setValue(pane.getHorizontalScrollBar().getValue() + 10);
            }
        });

        getActionMap().put("up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pane.getVerticalScrollBar().setValue(pane.getVerticalScrollBar().getValue() - 10);
            }
        });

        getActionMap().put("down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pane.getVerticalScrollBar().setValue(pane.getVerticalScrollBar().getValue() + 10);
            }
        });

        getActionMap().put("zoomIn", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnBiggerActionPerformed(e);
            }
        });

        getActionMap().put("zoomOut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnSmallerActionPerformed(e);                
            }
        });
    
        if (scene != null) {
            pane.setWheelScrollingEnabled(false);
            JComponent sceneView = scene.getView();
            if (sceneView == null) {
                sceneView = scene.createView();
            }
            pane.setViewportView(sceneView);
            sceneView.addMouseWheelListener(this);

            sceneView.setFocusable(true);
            sceneView.requestFocusInWindow();

            sceneView.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK), "left");
            sceneView.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK), "right");
            sceneView.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
            sceneView.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");

        }
    }

    
    @Override
    public void mouseWheelMoved(MouseWheelEvent evt) {
        
        final int notches = evt.getWheelRotation();
        if (notches < 0) {
            // mouse wheel moved up, zoom in
            btnBiggerActionPerformed(null);
        } else {
            // mouse wheel moved down, zoom out
            btnSmallerActionPerformed(null);
        }
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

    @Override
    public void componentShowing() {
        super.componentShowing();
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        btnGraph = new javax.swing.JButton();
        btnBigger = new javax.swing.JButton();
        btnSmaller = new javax.swing.JButton();
        lblFind = new javax.swing.JLabel();
        txtFind = new javax.swing.JTextField();
        lblPath = new javax.swing.JLabel();
        maxPathSpinner = new javax.swing.JSpinner();
        lblScopes = new javax.swing.JLabel();
        comScopes = new javax.swing.JComboBox();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jToolBar1.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(btnGraph, org.openide.util.NbBundle.getMessage(DependencyGraphTopComponent.class, "DependencyGraphTopComponent.btnGraph.text")); // NOI18N
        btnGraph.setFocusable(false);
        btnGraph.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnGraph.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnGraph.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGraphActionPerformed(evt);
            }
        });
        jToolBar1.add(btnGraph);

        btnBigger.setIcon(ImageUtilities.loadImageIcon(ZOOM_IN_ICON, true));
        btnBigger.setToolTipText(org.openide.util.NbBundle.getMessage(DependencyGraphTopComponent.class, "DependencyGraphTopComponent.btnBigger.toolTipText")); // NOI18N
        btnBigger.setFocusable(false);
        btnBigger.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnBigger.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnBigger.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBiggerActionPerformed(evt);
            }
        });
        jToolBar1.add(btnBigger);

        btnSmaller.setIcon(ImageUtilities.loadImageIcon(ZOOM_OUT_ICON, true));
        btnSmaller.setToolTipText(org.openide.util.NbBundle.getMessage(DependencyGraphTopComponent.class, "DependencyGraphTopComponent.btnSmaller.toolTipText")); // NOI18N
        btnSmaller.setFocusable(false);
        btnSmaller.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSmaller.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSmaller.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSmallerActionPerformed(evt);
            }
        });
        jToolBar1.add(btnSmaller);

        org.openide.awt.Mnemonics.setLocalizedText(lblFind, org.openide.util.NbBundle.getMessage(DependencyGraphTopComponent.class, "DependencyGraphTopComponent.lblFind.text")); // NOI18N
        jToolBar1.add(lblFind);

        txtFind.setMaximumSize(new java.awt.Dimension(200, 22));
        txtFind.setMinimumSize(new java.awt.Dimension(50, 19));
        txtFind.setPreferredSize(new java.awt.Dimension(150, 22));
        txtFind.setFont(new Font("Arial", java.awt.Font.PLAIN, 11));
        jToolBar1.add(txtFind);

        jPanel1.add(jToolBar1);

        lblPath.setLabelFor(maxPathSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(lblPath, org.openide.util.NbBundle.getMessage(DependencyGraphTopComponent.class, "DependencyGraphTopComponent.lblPath.text")); // NOI18N
        lblPath.setToolTipText(org.openide.util.NbBundle.getMessage(DependencyGraphTopComponent.class, "DependencyGraphTopComponent.maxPathSpinner.toolTipText")); // NOI18N
        jPanel1.add(lblPath);

        maxPathSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, 5, 1));
        maxPathSpinner.setToolTipText(org.openide.util.NbBundle.getMessage(DependencyGraphTopComponent.class, "DependencyGraphTopComponent.maxPathSpinner.toolTipText")); // NOI18N
        maxPathSpinner.setRequestFocusEnabled(false);
        maxPathSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxPathSpinnerStateChanged(evt);
            }
        });
        jPanel1.add(maxPathSpinner);

        org.openide.awt.Mnemonics.setLocalizedText(lblScopes, org.openide.util.NbBundle.getMessage(DependencyGraphTopComponent.class, "DependencyGraphTopComponent.lblScopes.text")); // NOI18N
        jPanel1.add(lblScopes);
        jPanel1.add(comScopes);

        add(jPanel1, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents
    
    private void btnSmallerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSmallerActionPerformed
        scene.setMyZoomFactor(scene.getZoomFactor() * 0.8);
        scene.validate();
        scene.repaint();
        if (!pane.getHorizontalScrollBar().isVisible() && 
            !pane.getVerticalScrollBar().isVisible()) {
            revalidate();
            repaint();
        }
        
    }//GEN-LAST:event_btnSmallerActionPerformed
    
    private void btnBiggerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBiggerActionPerformed
        scene.setMyZoomFactor(scene.getZoomFactor() * 1.2);
        scene.validate();
        scene.repaint();
        if (pane.getHorizontalScrollBar().isVisible() || 
            pane.getVerticalScrollBar().isVisible()) {
            revalidate();
            repaint();
        }
        
    }//GEN-LAST:event_btnBiggerActionPerformed

    private void maxPathSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxPathSpinnerStateChanged
        scene.highlightDepth(getSelectedDepth());
    }//GEN-LAST:event_maxPathSpinnerStateChanged

    private void btnGraphActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGraphActionPerformed
        btnGraph.setEnabled(false);
        createScene();
    }//GEN-LAST:event_btnGraphActionPerformed

    private int getSelectedDepth() {
        return ((SpinnerNumberModel)maxPathSpinner.getModel()).getNumber().intValue();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBigger;
    private javax.swing.JButton btnGraph;
    private javax.swing.JButton btnSmaller;
    private javax.swing.JComboBox comScopes;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lblFind;
    private javax.swing.JLabel lblPath;
    private javax.swing.JLabel lblScopes;
    private javax.swing.JSpinner maxPathSpinner;
    private javax.swing.JTextField txtFind;
    // End of variables declaration//GEN-END:variables

    private boolean expectingChanges;
    void saveChanges(POMModel model) throws IOException {
        LOG.log(Level.FINE, "{0} saveChanges...", hashCode());
        assert !expectingChanges;
        expectingChanges = true;
        try {
            Utilities.saveChanges(model);
        } finally {
            expectingChanges = false;
            LOG.log(Level.FINE, "{0} saveChanges...done", hashCode());
        }
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        if (expectingChanges) {
            LOG.log(Level.FINE, "{0} expecting change", hashCode());
            return;
        }
        LOG.log(Level.FINE, hashCode() + " not expecting change", new Exception());
        task_reload.schedule(200); // aggregate the events, multiple will be often coming close one by another..
    }

    JScrollPane getScrollPane () {
        return pane;
    }

    @Messages({
        "LBL_waiting_for_approval_first_time=Click Show Graph to compute and display dependencies.",
        "LBL_waiting_for_approval_dirty=Model has changed. Click Show Graph to refresh."
    })
    private void waitForApproval() {
        setPaneText(everDisplayed ? LBL_waiting_for_approval_dirty() : LBL_waiting_for_approval_first_time(), false);
        btnGraph.setEnabled(true);
        maxPathSpinner.setEnabled(false);
        maxPathSpinner.setVisible(false);
        lblPath.setVisible(false);
        txtFind.setEnabled(false);
        btnBigger.setEnabled(false);
        btnSmaller.setEnabled(false);
        comScopes.setEnabled(false);
    }

    @Messages({
        "Err_CannotLoad=Cannot display Artifact's dependency tree.",
        "LBL_Loading=Loading and constructing graph (this may take a while)."        
    })

    private void createScene() {
        Iterator<? extends org.apache.maven.shared.dependency.tree.DependencyNode> it1 = result.allInstances().iterator();
        Iterator<? extends MavenProject> it2 = result2.allInstances().iterator();
        Iterator<? extends POMModel> it3 = result3.allInstances().iterator();
        final MavenProject prj = it2.hasNext() ? it2.next() : null;
        if (prj != null && NbMavenProject.isErrorPlaceholder(prj)) {
            setPaneText(Err_CannotLoad(), false);
            return;
        }
        Optional.ofNullable(scene)
                .ifPresent((s) -> s.resetHighlight());
        everDisplayed = true;
        setPaneText(LBL_Loading(), true);
        final Project nbProj = getLookup().lookup(Project.class);
        if (prj != null && it1.hasNext()) {
            final MavenDependencyNode root = new MavenDependencyNode(it1.next());
            final POMModel model = it3.hasNext() ? it3.next() : null;
            RP.post(new Runnable() {
                @Override public void run() {
                    DependencyGraphScene.VersionProvider<MavenDependencyNode> versionProvider = new DependencyGraphScene.VersionProvider<MavenDependencyNode>() {
                        @Override
                        public String getVersion(MavenDependencyNode dependencyNode) {
                            return dependencyNode.getVersion();
                        }
                        @Override
                        public int compareVersions(MavenDependencyNode dependencyNode1, MavenDependencyNode dependencyNode2) {
                            return dependencyNode1.compareVersions(dependencyNode2);
                        }
                        @Override
                        public boolean isOmmitedForConflict(MavenDependencyNode dependencyNode) {
                             return dependencyNode.getState() == DependencyNode.OMITTED_FOR_CONFLICT;
                        }
                        @Override
                        public boolean isIncluded(MavenDependencyNode dependencyNode) {
                             return dependencyNode.getState() == DependencyNode.INCLUDED;
                        }
                    };
                    
                    DependencyGraphScene.PaintingProvider<MavenDependencyNode> pp = new DependencyGraphScene.PaintingProvider<MavenDependencyNode>() {
                        @Override
                        public Icon getIcon(MavenDependencyNode node) {
                            return DependencyGraphTopComponent.this.getIcon(node);
                        }

                        @Override
                        public boolean isVisible(MavenDependencyNode node) {
                            return true;
                        }

                        @Override
                        public boolean isVisible(MavenDependencyNode source, MavenDependencyNode target) {
                            return true;
                        }

                        @Override
                        public Color getColor(MavenDependencyNode node) {
                            return node.getScopeColor();
                        }

                        @Override
                        public Stroke getStroke(MavenDependencyNode source, MavenDependencyNode target) {
                            return null;
                        }
                        
                    };
                    
                    GraphConstructor constr = new GraphConstructor(prj);
                    constr.accept(root);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override public void run() {
                            final DependencyGraphScene<MavenDependencyNode> scene2 = new DependencyGraphScene<>(
                                new MavenActionsProvider(DependencyGraphTopComponent.this, nbProj, model), 
                                DependencyGraphTopComponent.this::getSelectedDepth, 
                                versionProvider, 
                                pp);
                            constr.updateScene(scene2);
                            scene = scene2;
                            JComponent sceneView = scene.getView();
                            if (sceneView == null) {
                                sceneView = scene.createView();
                                // vlv: print
                                sceneView.putClientProperty("print.printable", true); // NOI18N
                                addKeyboardBindings();
                            }
                            pane.setViewportView(sceneView);
                            scene.setSurroundingScrollPane(pane);
                            scene.initialLayout();
                            scene.setSelectedObjects(Collections.singleton(scene.getRootGraphNode()));
                            txtFind.setEnabled(true);
                            btnBigger.setEnabled(true);
                            btnSmaller.setEnabled(true);
                            comScopes.setEnabled(true);
                            if (scene.getMaxNodeDepth() > 1) {
                                lblPath.setVisible(true);
                                ((SpinnerNumberModel)maxPathSpinner.getModel()).
                                        setMaximum(Integer.valueOf(scene.getMaxNodeDepth()));
                                maxPathSpinner.setEnabled(true);
                                maxPathSpinner.setVisible(true);
                            }
                            scene.highlightDepth(getSelectedDepth());
                        }
                    });
                }
            });
        } else {
            LOG.log(Level.WARNING, "{0} missing DependencyNode and/or Project", hashCode());
        }
    }

    @Override
    public JComponent getVisualRepresentation() {
        jPanel1.removeAll();
        jToolBar1.removeAll();
        return this;
    }

    public static class EditorToolbar extends org.openide.awt.Toolbar {
        public EditorToolbar() {
            Border b = UIManager.getBorder("Nb.Editor.Toolbar.border"); //NOI18N
            setBorder(b);
            if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
                setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
            }
        }

        @Override
        public String getUIClassID() {
            if( UIManager.get("Nb.Toolbar.ui") != null ) { //NOI18N
                return "Nb.Toolbar.ui"; //NOI18N
            }
            return super.getUIClassID();
        }

        @Override
        public String getName() {
            return "editorToolbar"; //NOI18N
        }
    }

    @Override
    public JComponent getToolbarRepresentation() {
        if (toolbar == null) {
            toolbar = new EditorToolbar();
            toolbar.setFloatable(false);
            toolbar.setRollover(true);
//            Action[] a = new Action[1];
//            Action[] actions = getLookup().lookup(a.getClass());
//            for (Action act : actions) {
//                JButton btn = new JButton();
//                Actions.connect(btn, act);
//                toolbar.add(btn);
//            }
            toolbar.addSeparator();
            Dimension space = new Dimension(3, 0);
            toolbar.add(btnGraph);
            toolbar.addSeparator(space);
            toolbar.add(btnBigger);
            toolbar.addSeparator(space);
            toolbar.add(btnSmaller);
            toolbar.addSeparator(space);
            toolbar.add(lblFind);
            toolbar.add(txtFind);
            toolbar.addSeparator(space);
            toolbar.add(lblPath);
            toolbar.add(maxPathSpinner);
            toolbar.addSeparator(space);
            toolbar.add(lblScopes);
            toolbar.add(comScopes);
        }
        return toolbar;
    }

    @Override public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
    }

    @Override public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    private void setPaneText(String text, boolean progress)  {
        JComponent vView;
        if (progress) {
            JPanel panel = new JPanel();
            JProgressBar pb = new JProgressBar();
            JLabel lbl = new JLabel();

            panel.setLayout(new java.awt.GridBagLayout());
            panel.setOpaque(false);

            pb.setIndeterminate(true);
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            panel.add(pb, gridBagConstraints);

            Mnemonics.setLocalizedText(lbl, text);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
            panel.add(lbl, gridBagConstraints);
            vView = panel;
        } else {
            JLabel lbl = new JLabel(text);
            lbl.setHorizontalAlignment(JLabel.CENTER);
            lbl.setVerticalAlignment(JLabel.CENTER);
            vView = lbl;
        }

        pane.setViewportView(vView);
    }
    
    public Icon getIcon(MavenDependencyNode n) {
        return projectIcons.get(n.getArtifact());
    }
    
    /**
     * @return map of maven artifact mapped to project icon
     */
    private Map<Artifact, Icon> getIconsForOpenProjects() {
        Map<Artifact, Icon> result = new HashMap<Artifact, Icon>();
        //NOTE: surely not the best way to get the project icon
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        for (Project project : openProjects) {
            NbMavenProject mavenProject = project.getLookup().lookup(NbMavenProject.class);
            if (null != mavenProject) {
                Artifact artifact = mavenProject.getMavenProject().getArtifact();
                //get icon from opened project
                Icon icon = ProjectUtils.getInformation(project).getIcon();
                if (null != icon) {
                    result.put(artifact, icon);
                }
            }
        }
        return result;
    }
}
