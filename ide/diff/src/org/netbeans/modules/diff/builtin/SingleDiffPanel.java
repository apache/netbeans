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
package org.netbeans.modules.diff.builtin;

import java.awt.Container;
import java.awt.EventQueue;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.LifecycleManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Reader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.modules.diff.Utils;
import org.netbeans.modules.diff.options.DiffOptionsController;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

/**
 *
 * @author Maros Sandor
 */
public class SingleDiffPanel extends javax.swing.JPanel implements PropertyChangeListener {
    
    private FileObject          base;
    private FileObject          modified;
    private final FileObject    type;

    private DiffController      controller;
    private Action              nextAction;
    private Action              prevAction;
    private JComponent innerPanel;
    private FileChangeListener baseFCL, modifiedFCL;
    private PropertyChangeListener locationKeeper;

    /** Creates new form SingleDiffPanel */
    public SingleDiffPanel(FileObject left, FileObject right, FileObject type) throws IOException {
        this.base = left;
        this.modified = right;
        this.type = type;
        setListeners();
        initComponents();
        initMyComponents();
        refreshComponents();
    }

    private void initMyComponents() throws IOException {
        // centers components on the toolbar
        actionsToolbar.add(Box.createHorizontalGlue(), 0);
        actionsToolbar.add(Box.createHorizontalGlue());
        
        nextAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onNext();
            }
        };
        bNext.setAction(nextAction);
        // setAction sets the properties from action, so init tooltip & co. afterwards
        bNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/diff/builtin/visualizer/editable/diff-next.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(bNext, org.openide.util.NbBundle.getMessage(SingleDiffPanel.class, "SingleDiffPanel.bNext.text")); // NOI18N
        bNext.setToolTipText(org.openide.util.NbBundle.getMessage(SingleDiffPanel.class, "SingleDiffPanel.bNext.toolTipText")); // NOI18N        
            
        prevAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onPrev();
            }
        };
        bPrevious.setAction(prevAction);
        // setAction sets the properties from action, so init tooltip & co. afterwards
        bPrevious.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/diff/builtin/visualizer/editable/diff-prev.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(bPrevious, org.openide.util.NbBundle.getMessage(SingleDiffPanel.class, "SingleDiffPanel.bPrevious.text")); // NOI18N
        bPrevious.setToolTipText(org.openide.util.NbBundle.getMessage(SingleDiffPanel.class, "SingleDiffPanel.bPrevious.toolTipText")); // NOI18N
        
        getActionMap().put("jumpNext", nextAction);  // NOI18N
        getActionMap().put("jumpPrev", prevAction); // NOI18N

        refreshController(false);
    }

    private void refreshController(boolean keepLocation) throws IOException {
        int diffIndex = -1;
        if (controller != null) {
            diffIndex = controller.getDifferenceIndex();
            controller.removePropertyChangeListener(this);
            addPropertyChangeListener(this);
            if (locationKeeper != null) {
                controller.removePropertyChangeListener(locationKeeper);
                locationKeeper = null;
            }
        }
        
        // whatever the reason is that the fileobject isn't refreshed (!?),
        // this is an explicit user refresh so
        // refresh the FO explicitly as well
        base.refresh();
        modified.refresh();
        
        StreamSource ss1 = new DiffStreamSource(base, type, false);
        StreamSource ss2 = new DiffStreamSource(modified, type, true);
        controller = DiffController.createEnhanced(ss1, ss2);
        controller.addPropertyChangeListener(this);
        if (keepLocation && diffIndex >= 0) {
            final int fDiffIndex = diffIndex;
            controller.addPropertyChangeListener(locationKeeper = new PropertyChangeListener() {

                @Override
                public void propertyChange (PropertyChangeEvent evt) {
                    if (DiffController.PROP_DIFFERENCES.equals(evt.getPropertyName())) {
                        if (controller.getDifferenceCount() > controller.getDifferenceIndex()) {
                            controller.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, fDiffIndex);
                        }
                        controller.removePropertyChangeListener(this);
                    }
                }
            });
        }
        
        controllerPanel.removeAll();
        innerPanel = controller.getJComponent();
        controllerPanel.add(innerPanel);
        setName(innerPanel.getName());
        Container c = getParent();
        if (c != null) {
            c.setName(getName());
        }
        activateNodes();
        revalidate();
        repaint();
    }

    public void activateNodes () {
        TopComponent tc = (TopComponent) getClientProperty(TopComponent.class);
        if (tc != null) {
            Node node;
            try {
                DataObject dobj = DataObject.find(modified);
                node = dobj.getNodeDelegate();
            } catch (DataObjectNotFoundException e) {
                node = new AbstractNode(Children.LEAF, Lookups.singleton(modified));
            }
            tc.setActivatedNodes(new Node[] {node});
        }
    }

    public UndoRedo getUndoRedo() {
        UndoRedo undoRedo = null;
        if (innerPanel != null) {
            undoRedo = (UndoRedo) innerPanel.getClientProperty(UndoRedo.class);
        }
        if (undoRedo == null) {
            undoRedo = UndoRedo.NONE;
        }
        return undoRedo;
    }

    public void requestActive () {
        if (controllerPanel != null) {
            controllerPanel.requestFocusInWindow();
        }
    }
    
    public void closed() {
        // Traverse children components of controller panel and release the editor panes
        // from editor registry and annotation holder
        releaseChildrenPanes(controllerPanel);
    }
    
    private static void releaseChildrenPanes(JComponent c) {
        for (int i = c.getComponentCount() - 1; i >= 0; i--) {
            java.awt.Component ac = c.getComponent(i);
            if (ac instanceof JComponent) {
                JComponent ch = (JComponent) ac;
                if (Boolean.TRUE.equals(ch.getClientProperty("usedByCloneableEditor"))) {
                    ch.putClientProperty("usedByCloneableEditor", Boolean.FALSE);
                } else {
                    releaseChildrenPanes(ch);
                }
            }
        }
    }
    
    private void onPrev() {
        int idx = controller.getDifferenceIndex();
        if (idx > 0) {
            controller.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, idx - 1);
        }
    }

    private void onNext() {
        int idx = controller.getDifferenceIndex();
        if (idx < controller.getDifferenceCount() - 1) {
            controller.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, idx + 1);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        refreshComponents();
    }

    private void refreshComponents() {
        nextAction.setEnabled(controller.getDifferenceIndex() < controller.getDifferenceCount() - 1);
        prevAction.setEnabled(controller.getDifferenceIndex() > 0);
    }

    private void setListeners () {
        FileObject baseParent = base.getParent();
        FileObject modifiedParent = modified.getParent();
        if (baseParent != null) {
            baseParent.addFileChangeListener(WeakListeners.create(FileChangeListener.class, baseFCL = new DiffFileChangeListener(), baseParent));
        }
        if (baseParent != modifiedParent && modifiedParent != null) {
            modifiedParent.addFileChangeListener(WeakListeners.create(FileChangeListener.class, modifiedFCL = new DiffFileChangeListener(), modifiedParent));
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        actionsToolbar = new javax.swing.JToolBar();
        bNext = new javax.swing.JButton();
        bPrevious = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        bRefresh = new javax.swing.JButton();
        bSwap = new javax.swing.JButton();
        bExport = new javax.swing.JButton();
        bOptions = new javax.swing.JButton();
        controllerPanel = new javax.swing.JPanel();

        actionsToolbar.setFloatable(false);
        actionsToolbar.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(bNext, org.openide.util.NbBundle.getMessage(SingleDiffPanel.class, "SingleDiffPanel.bNext.text")); // NOI18N
        bNext.setFocusable(false);
        bNext.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bNext.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        actionsToolbar.add(bNext);

        org.openide.awt.Mnemonics.setLocalizedText(bPrevious, org.openide.util.NbBundle.getMessage(SingleDiffPanel.class, "SingleDiffPanel.bPrevious.text")); // NOI18N
        bPrevious.setFocusable(false);
        bPrevious.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bPrevious.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        actionsToolbar.add(bPrevious);
        actionsToolbar.add(jSeparator1);

        bRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/diff/builtin/visualizer/editable/diff-refresh.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(bRefresh, org.openide.util.NbBundle.getMessage(SingleDiffPanel.class, "SingleDiffPanel.bRefresh.text")); // NOI18N
        bRefresh.setToolTipText(org.openide.util.NbBundle.getMessage(SingleDiffPanel.class, "SingleDiffPanel.bRefresh.toolTipText")); // NOI18N
        bRefresh.setFocusable(false);
        bRefresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bRefresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bRefreshActionPerformed(evt);
            }
        });
        actionsToolbar.add(bRefresh);

        org.openide.awt.Mnemonics.setLocalizedText(bSwap, org.openide.util.NbBundle.getMessage(SingleDiffPanel.class, "SingleDiffPanel.bSwap.text")); // NOI18N
        bSwap.setFocusable(false);
        bSwap.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bSwap.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bSwap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSwapActionPerformed(evt);
            }
        });
        actionsToolbar.add(bSwap);

        org.openide.awt.Mnemonics.setLocalizedText(bExport, org.openide.util.NbBundle.getMessage(SingleDiffPanel.class, "SingleDiffPanel.bExport.text")); // NOI18N
        bExport.setFocusable(false);
        bExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bExportActionPerformed(evt);
            }
        });
        actionsToolbar.add(bExport);

        org.openide.awt.Mnemonics.setLocalizedText(bOptions, org.openide.util.NbBundle.getMessage(SingleDiffPanel.class, "SingleDiffPanel.bOptions.text")); // NOI18N
        bOptions.setFocusable(false);
        bOptions.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bOptions.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bOptionsActionPerformed(evt);
            }
        });
        actionsToolbar.add(bOptions);

        controllerPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(controllerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
            .addComponent(actionsToolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(actionsToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(controllerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void bRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRefreshActionPerformed
        LifecycleManager.getDefault().saveAll();
        try {
            refreshController(false);
        } catch (IOException e) {
            Logger.getLogger(SingleDiffPanel.class.getName()).log(Level.SEVERE, "", e); // elegant, nice and simple exception logging
        }
    }//GEN-LAST:event_bRefreshActionPerformed
    
    private void bSwapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSwapActionPerformed
        LifecycleManager.getDefault().saveAll();
        FileObject temp = base;
        base = modified;
        modified = temp;
        try {
            refreshController(true);
        } catch (IOException e) {
            Logger.getLogger(SingleDiffPanel.class.getName()).log(Level.SEVERE, "", e); // elegant, nice and simple exception logging
        }
    }//GEN-LAST:event_bSwapActionPerformed

    private void bExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bExportActionPerformed
        StreamSource ss1 = new DiffStreamSource(base, type, false);
        StreamSource ss2 = new DiffStreamSource(modified, type, true);
        ExportPatch.exportPatch(new StreamSource[] { ss1 }, new StreamSource[] { ss2 });
    }//GEN-LAST:event_bExportActionPerformed
    
    private void bOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bOptionsActionPerformed
        OptionsDisplayer.getDefault().open("Advanced/" + DiffOptionsController.OPTIONS_SUBPATH);
    }//GEN-LAST:event_bOptionsActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar actionsToolbar;
    private javax.swing.JButton bExport;
    private javax.swing.JButton bNext;
    private javax.swing.JButton bOptions;
    private javax.swing.JButton bPrevious;
    private javax.swing.JButton bRefresh;
    private javax.swing.JButton bSwap;
    private javax.swing.JPanel controllerPanel;
    private javax.swing.JToolBar.Separator jSeparator1;
    // End of variables declaration//GEN-END:variables

    private class DiffFileChangeListener extends FileChangeAdapter {
        @Override
        public void fileRenamed(FileRenameEvent fe) {
            if (fe.getFile() == base || fe.getFile() == modified) {
                refreshFiles();
            }
        }

        private void refreshFiles() {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        refreshController(true);
                    } catch (IOException ex) {
                        Logger.getLogger(SingleDiffPanel.class.getName()).log(Level.SEVERE, "", ex); //NOI18N
                    }
                }
            });
        }
    }

    private static class DiffStreamSource extends StreamSource {
        
        private final FileObject    fileObject;
        private final FileObject    type;
        private final boolean       isRight;

        public DiffStreamSource(FileObject fileObject, FileObject type, boolean isRight) {
            this.fileObject = fileObject;
            this.type = type;
            this.isRight = isRight;
        }

        @Override
        public boolean isEditable() {
            return isRight && fileObject.canWrite();
        }

        @Override
        public Lookup getLookup() {
            return Lookups.fixed(fileObject);
        }

        @Override
        public String getName() {
            return fileObject.getName();
        }

        @Override
        public String getTitle() {
            return FileUtil.getFileDisplayName(fileObject);
        }

        @Override
        public String getMIMEType() {
            if (type != null) {
                if (Utils.isFileContentBinary(type)) {
                    return null;
                }
                return type.getMIMEType();
            } else {
                if (Utils.isFileContentBinary(fileObject)) {
                    return null;
                }
                return fileObject.getMIMEType();
            }
        }

        @Override
        public Reader createReader() throws IOException {
            if (type != null) {
                return new InputStreamReader(fileObject.getInputStream(), FileEncodingQuery.getEncoding(type));
            } else {
                return new InputStreamReader(fileObject.getInputStream(), FileEncodingQuery.getEncoding(fileObject));
            }
        }

        @Override
        public Writer createWriter(Difference[] conflicts) throws IOException {
            return null;
        }
    }
}
