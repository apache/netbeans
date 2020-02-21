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
package org.netbeans.modules.cnd.diagnostics.clank.ui.codesnippet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.clang.tools.services.ClankDiagnosticInfo;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.diagnostics.clank.ClankCsmErrorInfo;
import org.netbeans.modules.cnd.diagnostics.clank.impl.ClankCsmErrorInfoAccessor;
import org.netbeans.modules.cnd.diagnostics.clank.ui.views.DiagnosticsAnnotationProvider;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.netbeans.modules.cnd.diagnostics.clank.ui//ClankDiagnosticsDetails//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "ClankDiagnosticsDetailsTopComponent",//NOI18N
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "output", openAtStartup = false, position = 357)
@ActionID(category = "Window", id = "org.netbeans.modules.cnd.diagnostics.clank.ui.ClankDiagnosticsDetailsTopComponent")
@ActionReference(path = "Menu/Window/Tools", position = 337)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ClankDiagnosticsDetailsAction",//NOI18N
        preferredID = "ClankDiagnosticsDetailsTopComponent"//NOI18N
)
@Messages({
    "CTL_ClankDiagnosticsDetailsAction=Clank Diagnostic Details Window",
    "CTL_ClankDiagnosticsDetailsTopComponent=Clank Diagnostic Details Window",
    "HINT_ClankDiagnosticsDetailsTopComponent=This is a Clank Diagnostics Details window"
})
public final class ClankDiagnosticsDetailsTopComponent extends TopComponent implements ExplorerManager.Provider, PropertyChangeListener {

    static final String PREFERRED_ID = "ClankDiagnosticsDetailsTopComponent";//NOI18N
    private final ExplorerManager manager = new ExplorerManager();
    private BeanTreeView btv;
    private final Action copyAction;

    public ClankDiagnosticsDetailsTopComponent() {
        initComponents();
        setName(Bundle.CTL_ClankDiagnosticsDetailsTopComponent());
        setToolTipText(Bundle.HINT_ClankDiagnosticsDetailsTopComponent());
        btv = new BeanTreeView();        
        jSplitPane2.setLeftComponent(btv);
        btv.setRootVisible(false);
        copyAction = new AbstractAction(NbBundle.getMessage(ClankDiagnosticsDetailsTopComponent.class, "ClankDiagnosticsDetailsTopComponent.Copy")) {//NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                Node[] selectedNodes = getExplorerManager().getSelectedNodes();
                if (selectedNodes == null || selectedNodes.length == 0) {
                    selectedNodes = getExplorerManager().getRootContext().getChildren().getNodes();
                }
                StringBuilder content = new StringBuilder();      
                for (Node n : selectedNodes){
                    if (n instanceof ClankDiagnosticInfoNode){
                        try {
                            ClankDiagnosticInfoNode node = (ClankDiagnosticInfoNode) n;
                            CsmFile csmErrorFile = ClankCsmErrorInfoAccessor.getDefault().getCsmFile(node.error);
                            FileSystem fSystem = csmErrorFile.getFileObject().getFileSystem();
                            FileObject fo = CndFileUtils.toFileObject(fSystem, node.note.getSourceFileName());
                            CsmFile csmNoteFile = CsmUtilities.getCsmFile(fo, false, false);                            
                            content.append(node.note.getMessage()).append(" at ").append(node.note.getSourceFileName()).append(" [");//NOI18N
                            final int[] startOffsets = node.note.getStartOffsets();
                            final int[] endOffsets = node.note.getEndOffsets();
                            for (int i = 0; i < startOffsets.length; i++) {
                                int[] lineColumnByOffset = CsmFileInfoQuery.getDefault().getLineColumnByOffset(csmNoteFile, startOffsets[i]);
                                int[] endlineColumnByOffset = CsmFileInfoQuery.getDefault().getLineColumnByOffset(csmNoteFile, endOffsets[i]);
                                content.append(lineColumnByOffset[0]).append(":").append(lineColumnByOffset[1]).append("-");//NOI18N
                                content.append(endlineColumnByOffset[0]).append(":").append(endlineColumnByOffset[1]);//NOI18N
                                if (i < startOffsets.length -1) {
                                    content.append(";");//NOI18N
                                    
                                }
                            }
                            content.append("]\n");//NOI18N
                            //now add details
                        } catch (FileStateInvalidException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(content.toString()), new ClipboardOwner() {

                    @Override
                    public void lostOwnership(Clipboard clipboard, Transferable contents) {
                        //do nothing
                    }
                });                
            }
        };
        previousError.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ClankDiagnosticChildren children = (ClankDiagnosticChildren) manager.getRootContext().getChildren();
                    CsmFile csmErrorFile = ClankCsmErrorInfoAccessor.getDefault().getCsmFile(children.error);
                    FileSystem fSystem = csmErrorFile.getFileObject().getFileSystem();                
                    DiagnosticsAnnotationProvider.prev(fSystem, ClankDiagnosticsDetailsTopComponent.this);
//                Node[] selectedNodes = manager.getSelectedNodes();
//                if (selectedNodes.length == 1 && selectedNodes[0] instanceof ClankDiagnosticInfoNode) {
//                    ClankDiagnosticInfoNode selectedNode = (ClankDiagnosticInfoNode) selectedNodes[0];                
//                    ClankDiagnosticChildren children = (ClankDiagnosticChildren) manager.getRootContext().getChildren();
//                    final int nodesCount = children.getNodesCount();
//                    for (int i = 0; i < nodesCount; i++) {
//                        ClankDiagnosticInfoNode nodeAt = (ClankDiagnosticInfoNode) children.getNodeAt(i);
//                        if (nodeAt.note == selectedNode.note) { 
//                            final ClankDiagnosticInfo problem = ((ClankDiagnosticInfoNode)children.getNodeAt(i -1 )).note;
//                            setSelectedNode(problem);
//                            return;
//                        }
//                    }
//                    
//                }
                } catch (FileStateInvalidException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        nextError.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {                
                    ClankDiagnosticChildren children = (ClankDiagnosticChildren) manager.getRootContext().getChildren();
                    CsmFile csmErrorFile = ClankCsmErrorInfoAccessor.getDefault().getCsmFile(children.error);
                    FileSystem fSystem = csmErrorFile.getFileObject().getFileSystem();
                    DiagnosticsAnnotationProvider.next(fSystem, ClankDiagnosticsDetailsTopComponent.this);
//                    Node[] selectedNodes = manager.getSelectedNodes();
//                    if (selectedNodes.length == 1 && selectedNodes[0] instanceof ClankDiagnosticInfoNode) {
//                        ClankDiagnosticInfoNode selectedNode = (ClankDiagnosticInfoNode) selectedNodes[0];
//                        ClankDiagnosticChildren children = (ClankDiagnosticChildren) manager.getRootContext().getChildren();
//                        final int nodesCount = children.getNodesCount();
//                        for (int i = 0; i < nodesCount; i++) {
//                            ClankDiagnosticInfoNode nodeAt = (ClankDiagnosticInfoNode) children.getNodeAt(i);
//                            if (nodeAt.note == selectedNode.note) {
//                                final ClankDiagnosticInfo problem = ((ClankDiagnosticInfoNode)children.getNodeAt(i + 1)).note;
//                                setSelectedNode(problem);
//                                return;
//                            }
//                        }
//                        
//                    }
                } catch (FileStateInvalidException ex) {                    
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        manager.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                //follow selected node changes only
                if (!ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())){
                    return;
                }
                Node[] selectedNodes = manager.getSelectedNodes();

                if (selectedNodes.length == 1 && selectedNodes[0] instanceof ClankDiagnosticInfoNode) {
                    nodeChanged((ClankDiagnosticInfoNode) selectedNodes[0]);
                }
            }
        });
    }

    private void nodeChanged(ClankDiagnosticInfoNode node) {
        try {
            DiagnosticsAnnotationProvider.setCurrentDiagnostic(node.note);            
            CsmFile csmErrorFile = ClankCsmErrorInfoAccessor.getDefault().getCsmFile(node.error);
            FileSystem fSystem = csmErrorFile.getFileObject().getFileSystem();
            FileObject fo = CndFileUtils.toFileObject(fSystem, node.note.getSourceFileName());
            CsmFile csmNoteFile = CsmUtilities.getCsmFile(fo, false, false);
            int size = node.note.getStartOffsets().length;
            int[][] startLineColumnByOffset = new int[size][];
            int[][] endlineColumnByOffset = new int[size][];
            for (int i = 0; i < size; i++) {
                startLineColumnByOffset[i] = CsmFileInfoQuery.getDefault().getLineColumnByOffset(csmNoteFile, node.note.getStartOffsets()[i]);
                endlineColumnByOffset[i] = CsmFileInfoQuery.getDefault().getLineColumnByOffset(csmNoteFile, node.note.getEndOffsets()[i]);
            }
//            int[] lineColumnByOffset = CsmFileInfoQuery.getDefault().getLineColumnByOffset(csmNoteFile, node.note.getStartOffset());
//            int[] endlineColumnByOffset = CsmFileInfoQuery.getDefault().getLineColumnByOffset(csmNoteFile, node.note.getEndOffset());
            CodeSnippet codeSnippet = new CodeSnippet(fo, "filePath", startLineColumnByOffset, endlineColumnByOffset);//NOI18N
            descriptionPanel.removeAll();
            descriptionPanel.setLayout(new BorderLayout());
            final CodeSnippetPanel codeSnippetPanel = new CodeSnippetPanel(codeSnippet, true);
            descriptionPanel.add(codeSnippetPanel);

            descriptionPanel.revalidate();
            nextError.setEnabled(DiagnosticsAnnotationProvider.isNextActionEnabled());
            previousError.setEnabled(DiagnosticsAnnotationProvider.isPrevActionEnabled());
            //and go to
            goTo(node);
        } catch (FileStateInvalidException ex) {
            //Exceptions.printStackTrace(ex);
        }
    }

    public void setData(ClankCsmErrorInfo info) {
        final ClankDiagnosticChildren rootChildren = new ClankDiagnosticChildren(info);
        final RootDiagnisticNode rootContext = new RootDiagnisticNode(rootChildren);
        manager.setRootContext(rootContext);
        ClankDiagnosticInfoNode node = (ClankDiagnosticInfoNode) rootChildren.getNodes()[0];
        setSelectedNode(node.note);
        btv.revalidate();
    }
    
    private int indexOf(ClankDiagnosticInfo note) {
        ClankDiagnosticChildren children = (ClankDiagnosticChildren) manager.getRootContext().getChildren();
        final int nodesCount = children.getNodesCount();
        for (int i = 0; i < nodesCount; i++) {
            ClankDiagnosticInfoNode nodeAt = (ClankDiagnosticInfoNode) children.getNodeAt(i);
            if (nodeAt.note == note) {                
                return i;
            }
        }   
        return -1;
    }

    public void setSelectedNode(ClankDiagnosticInfo note) {
        ClankDiagnosticChildren children = (ClankDiagnosticChildren) manager.getRootContext().getChildren();
        final int nodesCount = children.getNodesCount();
        for (int i = 0; i < nodesCount; i++) {
            ClankDiagnosticInfoNode nodeAt = (ClankDiagnosticInfoNode) children.getNodeAt(i);
            if (nodeAt.note == note) {
                try {
                    manager.setSelectedNodes(new Node[]{nodeAt});
                } catch (PropertyVetoException ex) {
                    Exceptions.printStackTrace(ex);
                }

                return;
            }
        };
    }

    public static synchronized ClankDiagnosticsDetailsTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win instanceof ClankDiagnosticsDetailsTopComponent) {
            return (ClankDiagnosticsDetailsTopComponent) win;
        }
        if (win == null) {
            Logger.getLogger(ClankDiagnosticsDetailsTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");//NOI18N
        } else {
            Logger.getLogger(ClankDiagnosticsDetailsTopComponent.class.getName()).warning(
                    "There seem to be multiple components with the '" + PREFERRED_ID//NOI18N
                    + "' ID. That is a potential source of errors and unexpected behavior.");//NOI18N
        }

        ClankDiagnosticsDetailsTopComponent result = new ClankDiagnosticsDetailsTopComponent();
        Mode outputMode = WindowManager.getDefault().findMode("output");//NOI18N

        if (outputMode != null) {
            outputMode.dockInto(result);
        }
        return result;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        previousError = new javax.swing.JButton();
        nextError = new javax.swing.JButton();
        jSplitPane2 = new javax.swing.JSplitPane();
        descriptionPanel = new javax.swing.JPanel();

        jToolBar1.setBorder(new VariableRightBorder());
        jToolBar1.setFloatable(false);
        jToolBar1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jToolBar1.setRollover(true);

        previousError.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/diagnostics/clank/resources/prevmatch.png"))); // NOI18N
        previousError.setToolTipText(org.openide.util.NbBundle.getBundle(ClankDiagnosticsDetailsTopComponent.class).getString("ClankDiagnosticsDetailsTopComponent.previousError.toolTipText")); // NOI18N
        previousError.setBorderPainted(false);
        previousError.setEnabled(false);
        previousError.setFocusable(false);
        previousError.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        previousError.setMaximumSize(new java.awt.Dimension(24, 24));
        previousError.setMinimumSize(new java.awt.Dimension(24, 24));
        previousError.setPreferredSize(new java.awt.Dimension(24, 24));
        previousError.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(previousError);

        nextError.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/diagnostics/clank/resources/nextmatch.png"))); // NOI18N
        nextError.setToolTipText(org.openide.util.NbBundle.getBundle(ClankDiagnosticsDetailsTopComponent.class).getString("ClankDiagnosticsDetailsTopComponent.nextError.toolTipText")); // NOI18N
        nextError.setBorderPainted(false);
        nextError.setEnabled(false);
        nextError.setFocusable(false);
        nextError.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        nextError.setMaximumSize(new java.awt.Dimension(24, 24));
        nextError.setMinimumSize(new java.awt.Dimension(24, 24));
        nextError.setPreferredSize(new java.awt.Dimension(24, 24));
        nextError.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(nextError);

        jSplitPane2.setBorder(null);
        jSplitPane2.setDividerLocation(200);
        jSplitPane2.setResizeWeight(1.0);
        jSplitPane2.setToolTipText(org.openide.util.NbBundle.getMessage(ClankDiagnosticsDetailsTopComponent.class, "ClankDiagnosticsDetailsTopComponent.jSplitPane2.toolTipText")); // NOI18N
        jSplitPane2.setFocusable(false);
        jSplitPane2.setOneTouchExpandable(true);

        javax.swing.GroupLayout descriptionPanelLayout = new javax.swing.GroupLayout(descriptionPanel);
        descriptionPanel.setLayout(descriptionPanelLayout);
        descriptionPanelLayout.setHorizontalGroup(
            descriptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 245, Short.MAX_VALUE)
        );
        descriptionPanelLayout.setVerticalGroup(
            descriptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 410, Short.MAX_VALUE)
        );

        jSplitPane2.setRightComponent(descriptionPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jSplitPane2)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JButton nextError;
    private javax.swing.JButton previousError;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");//NOI18N
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");//NOI18N
        // TODO read your settings according to their version
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    private void goTo(final ClankDiagnosticInfoNode node) {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                {
                    try {
                        CsmFile csmErrorFile = ClankCsmErrorInfoAccessor.getDefault().getCsmFile(node.error);
                        FileSystem fSystem = csmErrorFile.getFileObject().getFileSystem();
                        DiagnosticsAnnotationProvider.goTo(node.note, fSystem, null);
//                        final FileObject fo = CndFileUtils.toFileObject(fSystem, node.note.getSourceFileName());
//                        CsmFile csmNoteFile = CsmUtilities.getCsmFile(fo, false, false);
//                        final int[] lineColumnByOffset = CsmFileInfoQuery.getDefault().getLineColumnByOffset(csmNoteFile, node.note.getStartOffsets()[0]);
//                        SwingUtilities.invokeLater(new Runnable() {
//                            @Override
//                            public void run() {
//                                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(CodeSnippetPanel.class, "OpeningFile"));//NOI18N
//                                RequestProcessor.getDefault().post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        if (fo == null) {
//                                            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(CodeSnippetPanel.class, "CannotOpen", node.note.getSourceFileName()));//NOI18N
//                                        } else {
//                                            Utilities.show(fo, lineColumnByOffset[0]);
//                                        }
//                                    }
//                                });
//                            }
//                        });
                    } catch (FileStateInvalidException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (DiagnosticsAnnotationProvider.DIAGNOSTIC_CHANGED.equals(evt.getPropertyName())) {
            ClankDiagnosticInfo note = (ClankDiagnosticInfo) evt.getNewValue();
            setSelectedNode(note);
        }
    }
    
    private class RootDiagnisticNode extends AbstractNode {

        public RootDiagnisticNode(ClankDiagnosticChildren children) {
            super(children);
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[]{copyAction};
        }
    }

    private class ClankDiagnosticChildren extends Children.Keys<ClankDiagnosticInfo> {

        private final ClankCsmErrorInfo error;

        public ClankDiagnosticChildren(ClankCsmErrorInfo info) {
            this.error = info;
            final ArrayList<ClankDiagnosticInfo> notes = ClankCsmErrorInfoAccessor.getDefault().getDelegate(info).notes();
            ArrayList<ClankDiagnosticInfo> keys = new ArrayList<>();
            keys.add(ClankCsmErrorInfoAccessor.getDefault().getDelegate(info));
            keys.addAll(notes);
            setKeys(keys);
        }
        
        @Override
        protected Node[] createNodes(ClankDiagnosticInfo key) {
            return new Node[]{new ClankDiagnosticInfoNode(error, key)};
        }

    }

    private class ClankDiagnosticInfoNode extends AbstractNode {

        private final ClankDiagnosticInfo note;
        private final ClankCsmErrorInfo error;
        private Action defaultAction = null;

        public ClankDiagnosticInfoNode(ClankCsmErrorInfo error, ClankDiagnosticInfo note) {
            super(Children.LEAF);
            this.error = error;
            this.note = note;
            defaultAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    goTo(ClankDiagnosticInfoNode.this);
                }
            ;
            };
            setName(note.getMessage());
        }

        @Override

        public String getHtmlDisplayName() {
            try {
                CsmFile csmErrorFile = ClankCsmErrorInfoAccessor.getDefault().getCsmFile(error);
                FileSystem fSystem = csmErrorFile.getFileObject().getFileSystem();
                FileObject fo = CndFileUtils.toFileObject(fSystem, note.getSourceFileName());
                CsmFile csmNoteFile = CsmUtilities.getCsmFile(fo, false, false);
                StringBuilder htmlName = new StringBuilder("<html>");//NOI18N
                htmlName.append(note.getMessage()).append(" at <b>").append(CndPathUtilities.getBaseName(note.getSourceFileName())).append(" [");//NOI18N
                final int[] startOffsets = note.getStartOffsets();
                final int[] endOffsets = note.getEndOffsets();
                for (int i = 0; i < startOffsets.length; i++) {
                    int[] lineColumnByOffset = CsmFileInfoQuery.getDefault().getLineColumnByOffset(csmNoteFile, startOffsets[i]);
                    int[] endlineColumnByOffset = CsmFileInfoQuery.getDefault().getLineColumnByOffset(csmNoteFile, endOffsets[i]);                
                    htmlName.append(lineColumnByOffset[0]).append(":").append(lineColumnByOffset[1]).append("-");//NOI18N
                    htmlName.append(endlineColumnByOffset[0]).append(":").append(endlineColumnByOffset[1]);//NOI18N
                    if (i < startOffsets.length -1) {
                        htmlName.append(";");//NOI18N
                                
                    }
                }
                htmlName.append("]</b>");//NOI18N
                htmlName.append("</html>");//NOI18N
                return htmlName.toString();
            } catch (FileStateInvalidException ex) {
                //Exceptions.printStackTrace(ex);
            }
            return getName();
        }

        @Override
        public Action getPreferredAction() {
            return defaultAction;
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[] {copyAction};
        }

        @Override
        public Image getIcon(int type) {
            if (error.getSeverity() == CsmErrorInfo.Severity.ERROR) {
                return ImageUtilities.loadImage("org/netbeans/modules/cnd/diagnostics/clank/resources/bugs-error24.png");//NOI18N
            } 
            return ImageUtilities.loadImage("org/netbeans/modules/cnd/diagnostics/clank/resources/bugs-warning24.png");//NOI18N
        }

    }

    private class VariableRightBorder implements Border {

        public VariableRightBorder() {
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Color old = g.getColor();
            g.setColor(getColor());
            g.drawLine(x + width - 1, y, x + width - 1, y + height);
            g.setColor(old);
        }

        public Color getColor() {
            if (org.openide.util.Utilities.isMac()) {
                Color c1 = UIManager.getColor("controlShadow");//NOI18N
                Color c2 = UIManager.getColor("control");//NOI18N
                return new Color((c1.getRed() + c2.getRed()) / 2,
                        (c1.getGreen() + c2.getGreen()) / 2,
                        (c1.getBlue() + c2.getBlue()) / 2);
            } else {
                return UIManager.getColor("controlShadow");//NOI18N
            }
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 0, 2);
        }

        public boolean isBorderOpaque() {
            return true;
        }
    }
}
