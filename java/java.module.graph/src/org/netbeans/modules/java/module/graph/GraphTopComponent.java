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
package org.netbeans.modules.java.module.graph;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.java.graph.DependencyGraphScene;
import org.netbeans.modules.java.graph.GraphEdge;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

/**
 *
 * @author Tomas Zezula
 */
final class GraphTopComponent extends TopComponent implements MultiViewElement, Runnable {

    @StaticResource
    private static final String ZOOM_IN_ICON = "org/netbeans/modules/java/module/graph/resources/zoomin.gif";   //NOI18N
    @StaticResource
    private static final String ZOOM_OUT_ICON = "org/netbeans/modules/java/module/graph/resources/zoomout.gif";
    @StaticResource
    private static final String PUBLIC_ICON = "org/netbeans/modules/java/module/graph/resources/public.gif";

    private static final RequestProcessor RP = new RequestProcessor(GraphTopComponent.class);

    private final RequestProcessor.Task refreshTask = RP.create(this);
    private final JScrollPane pane = new JScrollPane();
    private final ChangeSupport changeSupport;
    private boolean alreadyShown;
    private boolean needsRefresh;
    private MultiViewElementCallback callback;
    private EditorToolbar toolbar;
    private DependencyGraphScene<ModuleNode> scene;

    private Collection<? extends DependencyEdge> edges;
    
    private Timer timer = new Timer(500, new ActionListener() {
        @Override public void actionPerformed(ActionEvent arg0) {
            checkFindValue();
        }
    });
        
    /**
     * Creates new form GraphTopComponent
     */
    GraphTopComponent(@NonNull final Lookup lkp) {
        Parameters.notNull("lkp", lkp); //NOI18N
        changeSupport = new ChangeSupport(
                lkp.lookup(FileObject.class),
                () -> needsRefresh = true);
        associateLookup(lkp);
        initComponents();
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
            setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
        }
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
        
        jdkComboBox.setModel(new DefaultComboBoxModel(JDKVisibility.values()));
        jdkComboBox.setRenderer(new JDKRenderer());
        
        timer.setDelay(500);
        timer.setRepeats(false);
    }

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        if (toolbar == null) {
            toolbar = new EditorToolbar();
            toolbar.setFloatable(false);
            toolbar.setRollover(true);

            toolbar.addSeparator();
            Dimension space = new Dimension(3, 0);
            toolbar.add(zoomIn);
            toolbar.addSeparator(space);
            toolbar.add(zoomOut);
            toolbar.addSeparator(space);
            toolbar.add(lblFind);
            toolbar.add(txtFind);
            toolbar.addSeparator(space);
            toolbar.add(lblPath);
            toolbar.add(maxPathSpinner);
            toolbar.addSeparator();
            toolbar.add(jdkLabel);
            toolbar.add(jdkComboBox);
            toolbar.addSeparator(space);
            toolbar.add(transitiveCheckBox);
//            toolbar.addSeparator(space);
//            toolbar.add(lblScopes);
//            toolbar.add(comScopes);
        }
        return toolbar;
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
        pane.setWheelScrollingEnabled(true);
        add(pane, BorderLayout.CENTER);
        alreadyShown = false;
        needsRefresh = true;
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
        if (needsRefresh) {
            refreshModel();
            needsRefresh = false;
            alreadyShown = true;
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

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    public void run() {
        final FileObject moduleInfo = getLookup().lookup(FileObject.class);
        assert moduleInfo != null;
        final DependencyCalculator calc = new DependencyCalculator(moduleInfo);
        final Collection<? extends ModuleNode> nodes = calc.getNodes();
        final Collection<? extends DependencyEdge> edges = calc.getEdges();
        SwingUtilities.invokeLater(()->displayScene(nodes, edges));
    }

    @NonNull
    JScrollPane getScrollPane () {
        return pane;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jComboBox1 = new javax.swing.JComboBox<>();
        jPanel1 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        zoomIn = new javax.swing.JButton();
        zoomOut = new javax.swing.JButton();
        lblFind = new javax.swing.JLabel();
        txtFind = new javax.swing.JTextField();
        lblPath = new javax.swing.JLabel();
        maxPathSpinner = new javax.swing.JSpinner();
        jdkLabel = new javax.swing.JLabel();
        jdkComboBox = new javax.swing.JComboBox<>();
        transitiveCheckBox = new javax.swing.JCheckBox();

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        zoomIn.setIcon(ImageUtilities.loadImageIcon(ZOOM_IN_ICON, true));
        org.openide.awt.Mnemonics.setLocalizedText(zoomIn, org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.zoomIn.text")); // NOI18N
        zoomIn.setFocusable(false);
        zoomIn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        zoomIn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        zoomIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomIn(evt);
            }
        });
        jToolBar1.add(zoomIn);

        zoomOut.setIcon(ImageUtilities.loadImageIcon(ZOOM_OUT_ICON, true));
        org.openide.awt.Mnemonics.setLocalizedText(zoomOut, org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.zoomOut.text")); // NOI18N
        zoomOut.setFocusable(false);
        zoomOut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        zoomOut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        zoomOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOut(evt);
            }
        });
        jToolBar1.add(zoomOut);

        jPanel1.add(jToolBar1);

        org.openide.awt.Mnemonics.setLocalizedText(lblFind, org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.lblFind.text")); // NOI18N
        jPanel1.add(lblFind);

        txtFind.setMaximumSize(new java.awt.Dimension(200, 22));
        txtFind.setMinimumSize(new java.awt.Dimension(50, 19));
        txtFind.setPreferredSize(new java.awt.Dimension(150, 22));
        txtFind.setFont(new Font("Arial", java.awt.Font.PLAIN, 11));
        jPanel1.add(txtFind);

        org.openide.awt.Mnemonics.setLocalizedText(lblPath, org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.lblPath.text")); // NOI18N
        lblPath.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.lblPath.toolTipText")); // NOI18N
        jPanel1.add(lblPath);

        maxPathSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, 5, 1));
        maxPathSpinner.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.maxPathSpinner.toolTipText")); // NOI18N
        maxPathSpinner.setMaximumSize(new java.awt.Dimension(60, 32767));
        maxPathSpinner.setRequestFocusEnabled(false);
        maxPathSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxPathSpinnerStateChanged(evt);
            }
        });
        jPanel1.add(maxPathSpinner);

        org.openide.awt.Mnemonics.setLocalizedText(jdkLabel, org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.jdkLabel.text")); // NOI18N
        jdkLabel.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.jdkLabel.toolTipText")); // NOI18N
        jPanel1.add(jdkLabel);

        jdkComboBox.setMaximumSize(new java.awt.Dimension(300, 32767));
        jdkComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jdkComboBoxItemStateChanged(evt);
            }
        });
        jPanel1.add(jdkComboBox);

        org.openide.awt.Mnemonics.setLocalizedText(transitiveCheckBox, org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.transitiveCheckBox.text")); // NOI18N
        transitiveCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.transitiveCheckBox.toolTipText")); // NOI18N
        transitiveCheckBox.setIcon(ImageUtilities.loadImageIcon(PUBLIC_ICON, true));
        transitiveCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transitiveCheckBoxActionPerformed(evt);
            }
        });
        jPanel1.add(transitiveCheckBox);

        add(jPanel1, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void zoomIn(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomIn
        scene.setMyZoomFactor(scene.getZoomFactor() * 1.2);
        scene.validate();
        scene.repaint();
        if (pane.getHorizontalScrollBar().isVisible() ||
            pane.getVerticalScrollBar().isVisible()) {
            revalidate();
            repaint();
        }
    }//GEN-LAST:event_zoomIn

    private void zoomOut(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOut
        scene.setMyZoomFactor(scene.getZoomFactor() * 0.8);
        scene.validate();
        scene.repaint();
        if (!pane.getHorizontalScrollBar().isVisible() &&
            !pane.getVerticalScrollBar().isVisible()) {
            revalidate();
            repaint();
        }
    }//GEN-LAST:event_zoomOut

    private void maxPathSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxPathSpinnerStateChanged
        scene.highlightDepth(getSelectedDepth());
    }//GEN-LAST:event_maxPathSpinnerStateChanged

    private void jdkComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jdkComboBoxItemStateChanged
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED && scene != null) {                        
            scene.updateVisibility();
        }
    }//GEN-LAST:event_jdkComboBoxItemStateChanged

    private void transitiveCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transitiveCheckBoxActionPerformed
        if (scene != null) {
            scene.updateVisibility();
        }     
    }//GEN-LAST:event_transitiveCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JComboBox<String> jdkComboBox;
    private javax.swing.JLabel jdkLabel;
    private javax.swing.JLabel lblFind;
    private javax.swing.JLabel lblPath;
    private javax.swing.JSpinner maxPathSpinner;
    private javax.swing.JCheckBox transitiveCheckBox;
    private javax.swing.JTextField txtFind;
    private javax.swing.JButton zoomIn;
    private javax.swing.JButton zoomOut;
    // End of variables declaration//GEN-END:variables

    private void checkFindValue() {
        String val = txtFind.getText().trim();
        if ("".equals(val)) { //NOI18N
            val = null;
        }
        scene.setSearchString(val);
    }
    
    @NbBundle.Messages({
        "TXT_ComputingDependencies=Computing Dependencies...",
        "TXT_RefreshingDependencies=Refreshing Dependencies..."
    })
    private void refreshModel() {
        setPaneText(alreadyShown?
                Bundle.TXT_RefreshingDependencies() :
                Bundle.TXT_ComputingDependencies());
        enableControls(false);
        refreshTask.schedule(0);
    }

    private void enableControls(final boolean enable) {
        zoomIn.setEnabled(enable);
        zoomOut.setEnabled(enable);
    }

    private void setPaneText(@NonNull final String text)  {
        final JLabel lbl = new JLabel(text);
        lbl.setHorizontalAlignment(JLabel.CENTER);
        lbl.setVerticalAlignment(JLabel.CENTER);
        pane.setViewportView(lbl);
    }

    private static final BasicStroke TRANSITIVE_STROKE = new BasicStroke (1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, BasicStroke.JOIN_MITER, new float[] {5, 5}, 0);
    
    private void displayScene(
            @NonNull final Collection<? extends ModuleNode> nodes,
            @NonNull final Collection<? extends DependencyEdge> edges) 
    {
        
        this.edges = edges;
        
        DependencyGraphScene.PaintingProvider<ModuleNode> paintingProvider = new DependencyGraphScene.PaintingProvider<ModuleNode>() {
            @Override
            public boolean isVisible(ModuleNode node) {
                switch((JDKVisibility)jdkComboBox.getSelectedItem()) {
                    case ALL:
                        return true;
                    case DIRECT:
                        if(!node.isJdk()) {
                            return true;
                        } else {
                            Collection<GraphEdge<ModuleNode>> edges = scene.findNodeEdges(scene.getGraphNodeRepresentant(node), true, true);
                            return edges.stream().anyMatch(e -> !e.getSource().isJdk() && !getEdge(e.getSource(), e.getTarget()).isTrasitive());                            
                        }
                    case NONE:
                        return !node.isJdk();
                    default:
                        assert false;
                        return true;
                }
            }

            @Override
            public boolean isVisible(ModuleNode source, ModuleNode target) {
                DependencyEdge edge = getEdge(source, target);
                assert edge != null;
                if(edge.isTrasitive() && !transitiveCheckBox.isSelected()) {
                    return false;
                }
                switch((JDKVisibility)jdkComboBox.getSelectedItem()) {
                    case ALL:
                        return true;
                    case DIRECT:
                    case NONE:
                        return isVisible(source) && isVisible(target);
                    default:
                        assert false;
                        return true;
                }
            }
            
            @Override
            public Stroke getStroke(ModuleNode source, ModuleNode target) {
                if(!transitiveCheckBox.isSelected()) {
                    return null;
                }
                DependencyEdge edge = getEdge(source, target);
                assert edge != null;
                Stroke s = null;
                if(edge.isTrasitive()) { 
                    s = TRANSITIVE_STROKE;
                }
                return s;
            }            

            @Override
            public Icon getIcon(ModuleNode node) {
                return null;
            }

            @Override
            public Color getColor(ModuleNode node) {
                return null;
            }
        };                          
        
        scene = new DependencyGraphScene<>(null, GraphTopComponent.this::getSelectedDepth, null, paintingProvider);
        nodes.stream().forEach((n)-> {
            scene.addGraphNodeImpl(n);
        });
        edges.stream().forEach((e)-> scene.addEdge(e.getSource(), e.getTarget()));
        scene.calculatePrimaryPathsAndLevels();       
        JComponent sceneView = scene.getView();
        if (sceneView == null) {
            sceneView = scene.createView();
            // vlv: print
            sceneView.putClientProperty("print.printable", true); // NOI18N
        }
        pane.setViewportView(sceneView);
        scene.setSurroundingScrollPane(pane);
        scene.initialLayout();
        scene.setSelectedObjects(Collections.singleton(scene.getRootGraphNode()));
        if (scene.getMaxNodeDepth() > 1) {
            lblPath.setVisible(true);
            ((SpinnerNumberModel)maxPathSpinner.getModel()).
                    setMaximum(Integer.valueOf(scene.getMaxNodeDepth()));
            maxPathSpinner.setEnabled(true);
            maxPathSpinner.setVisible(true);
        }
        scene.highlightDepth(getSelectedDepth());
        enableControls(true);
    }

    private DependencyEdge getEdge(ModuleNode source, ModuleNode target) {
        // XXX suboptimal, though still extremely cheap                
        for (DependencyEdge edge : edges) {
            if(edge.getSource().equals(source) && edge.getTarget().equals(target)) {
                return edge;
            }
        }
        return null;
    }
                
    private static class EditorToolbar extends org.openide.awt.Toolbar {
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

    private int getSelectedDepth() {
        return ((SpinnerNumberModel)maxPathSpinner.getModel()).getNumber().intValue();
    }
    
    private static class ChangeSupport extends FileChangeAdapter implements PropertyChangeListener, DocumentListener {

        private final Runnable resetAction;
        private final FileObject file;
        private final EditorCookie.Observable ec;
        private Document currentDoc;

        ChangeSupport(
                @NonNull final FileObject file,
                @NonNull final Runnable resetAction) {
            Parameters.notNull("file", file);   //NOI18N
            Parameters.notNull("resetAction", resetAction); //NOI18N
            this.resetAction = resetAction;
            this.file = file;
            this.file.addFileChangeListener(WeakListeners.create(FileChangeListener.class, this, this.file));
            EditorCookie.Observable cookie = null;
            try {
                final DataObject dobj = DataObject.find(file);
                cookie = dobj.getLookup().lookup(EditorCookie.Observable.class);
            } catch (DataObjectNotFoundException e) {
                //pass
            }
            this.ec = cookie;
            if (this.ec != null) {
                this.ec.addPropertyChangeListener(WeakListeners.propertyChange(this, this.ec));
                assignDocListener(this.ec);
            }
        }


        @Override
        public void fileDataCreated(FileEvent fe) {
            reset();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            reset();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            reset();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            reset();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (EditorCookie.Observable.PROP_DOCUMENT.equals(evt.getPropertyName())) {
                assignDocListener(this.ec);
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            reset();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            reset();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
        }

        private void reset() {
            resetAction.run();
        }

        private void assignDocListener(@NonNull final EditorCookie ec) {
            if (currentDoc != null) {
                currentDoc.removeDocumentListener(this);
            }
            currentDoc = ec.getDocument();
            if (currentDoc != null) {
                currentDoc.addDocumentListener(this);
            }
        }
    }
    
    @NbBundle.Messages({
        "LBL_All=Show all JDK modules",
        "LBL_Direct=Show only direct JDK dependencies",
        "LBL_None=Hide all JDK modules"
    })
    private enum JDKVisibility {
        ALL(Bundle.LBL_All()),
        DIRECT(Bundle.LBL_Direct()),
        NONE(Bundle.LBL_None());        
        final String displayName;
        private JDKVisibility(String displayName) {
            this.displayName = displayName;
        }
    }
    
    private static class JDKRenderer extends DefaultListCellRenderer {
        @Override
        @SuppressWarnings({"AssignmentToMethodParameter"})
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if(value instanceof JDKVisibility) {
                value = ((JDKVisibility)value).displayName;
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
}
