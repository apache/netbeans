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
package org.netbeans.modules.cnd.navigation.macroview;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.Document;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Macro Expansion panel.
 *
 */
public class MacroExpansionPanel extends JPanel implements ExplorerManager.Provider, HelpCtx.Provider {

    public static final String ICON_PATH = "org/netbeans/modules/cnd/navigation/macroview/resources/macroexpansion.png"; // NOI18N
    private final transient ExplorerManager explorerManager = new ExplorerManager();

    /** Creates new form MacroExpansionPanel. */
    public MacroExpansionPanel(boolean isView) {
        initComponents();
        autoRefresh.setSelected(MacroExpansionTopComponent.isSyncCaretAndContext());
        localContext.setSelected(MacroExpansionTopComponent.isLocalContext());
        fileContext.setSelected(!MacroExpansionTopComponent.isLocalContext());
        jCodeExpansionEditorPane.putClientProperty(MacroExpansionViewUtils.CND_EDITOR_COMPONENT, Boolean.TRUE);
        setName(NbBundle.getMessage(getClass(), "CTL_MacroExpansionTopComponent")); // NOI18N
        setToolTipText(NbBundle.getMessage(getClass(), "HINT_MacroExpansionTopComponent")); // NOI18N
    }

    /**
     * Initializes document of expanded context pane.
     *
     * @param doc - document
     */
    public void setContextExpansionDocument(Document doc) {
        String mimeType = DocumentUtilities.getMimeType(doc);
        if (mimeType == null) {
            mimeType = MIMENames.CPLUSPLUS_MIME_TYPE;
        }
        jCodeExpansionEditorPane.setCaretPosition(0);
        jCodeExpansionEditorPane.setContentType(mimeType);
        jCodeExpansionEditorPane.setDocument(doc);
        jCodeExpansionEditorPane.putClientProperty("HelpID","MacroExpansionWindow"); //NOI18N
        doc.putProperty(JEditorPane.class, jCodeExpansionEditorPane);
    }

    /**
     * Sets text in status bar.
     * 
     * @param s - text
     */
    public void setStatusBarText(String s) {
        jStatusBar.setText(s);
    }

    /**
     * Updates cursor position.
     */
    public void updateCaretPosition() {
        jCodeExpansionEditorPane.setCaretPosition(getCursorPositionFromMainDocument());
    }

    private int getCursorPositionFromMainDocument() {
        Document doc = jCodeExpansionEditorPane.getDocument();
        if (doc == null) {
            return 0;
        }
        Document doc2 = (Document) doc.getProperty(Document.class);
        if (doc2 == null) {
            return 0;
        }
        int docCarretPosition = MacroExpansionViewUtils.getDocumentOffset(doc,
                MacroExpansionViewUtils.getFileOffset(doc2, getMainDocumentCursorPosition()));
        if (docCarretPosition >= 0 && docCarretPosition < doc.getLength()) {
            return docCarretPosition;
        }
        return 0;
    }

    private int getMainDocumentCursorPosition() {
        Document doc = jCodeExpansionEditorPane.getDocument();
        if (doc == null) {
            return 0;
        }
        Document doc2 = (Document) doc.getProperty(Document.class);
        if (doc2 != null) {
            FileObject file2 = CsmUtilities.getFileObject(doc2);
            if (file2 != null) {
                JEditorPane ep = MacroExpansionViewUtils.getEditor(doc2);
                if(ep != null) {
                    int doc2CarretPosition = ep.getCaretPosition();
                    return doc2CarretPosition;
                }
            }
        }
        return 0;
    }

    private void update() {
        Document doc = jCodeExpansionEditorPane.getDocument();
        if (doc == null) {
            return;
        }
        Document mainDoc = (Document) doc.getProperty(Document.class);
        if (mainDoc == null) {
            return;
        }
        JEditorPane ep = MacroExpansionViewUtils.getEditor(doc);
        if (ep == null) {
            return;
        }
        int offset = MacroExpansionViewUtils.getDocumentOffset(mainDoc, MacroExpansionViewUtils.getFileOffset(doc, ep.getCaretPosition()));
        CsmMacroExpansion.showMacroExpansionView(mainDoc, offset);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jCodeExpansionPane = new javax.swing.JScrollPane();
        jCodeExpansionEditorPane = new javax.swing.JEditorPane();
        jStatusBar = new javax.swing.JLabel();
        jToolBar1 = new javax.swing.JToolBar();
        autoRefresh = new javax.swing.JToggleButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        localContext = new javax.swing.JToggleButton();
        fileContext = new javax.swing.JToggleButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        prevMacro = new javax.swing.JButton();
        nextMacro = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setMaximumSize(new java.awt.Dimension(100, 100));
        jPanel1.setMinimumSize(new java.awt.Dimension(100, 100));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jCodeExpansionPane.setBorder(null);

        jCodeExpansionEditorPane.setBorder(null);
        jCodeExpansionPane.setViewportView(jCodeExpansionEditorPane);

        jPanel1.add(jCodeExpansionPane, java.awt.BorderLayout.CENTER);

        jStatusBar.setText(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.jStatusBar.text")); // NOI18N
        jPanel1.add(jStatusBar, java.awt.BorderLayout.PAGE_END);

        add(jPanel1, java.awt.BorderLayout.CENTER);

        jToolBar1.setFloatable(false);
        jToolBar1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jToolBar1.setRollover(true);
        jToolBar1.setMaximumSize(new java.awt.Dimension(28, 240));
        jToolBar1.setPreferredSize(new java.awt.Dimension(28, 240));

        autoRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/macroview/resources/sync.png"))); // NOI18N
        autoRefresh.setToolTipText(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.autoRefresh.toolTipText")); // NOI18N
        autoRefresh.setFocusable(false);
        autoRefresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        autoRefresh.setMaximumSize(new java.awt.Dimension(24, 24));
        autoRefresh.setMinimumSize(new java.awt.Dimension(24, 24));
        autoRefresh.setPreferredSize(new java.awt.Dimension(24, 24));
        autoRefresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        autoRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoRefreshActionPerformed(evt);
            }
        });
        jToolBar1.add(autoRefresh);

        jSeparator1.setSeparatorSize(new java.awt.Dimension(0, 4));
        jToolBar1.add(jSeparator1);

        localContext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/macroview/resources/declscope.png"))); // NOI18N
        localContext.setToolTipText(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.localContext.toolTipText")); // NOI18N
        localContext.setFocusable(false);
        localContext.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        localContext.setMaximumSize(new java.awt.Dimension(24, 24));
        localContext.setMinimumSize(new java.awt.Dimension(24, 24));
        localContext.setPreferredSize(new java.awt.Dimension(24, 24));
        localContext.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        localContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                localContextActionPerformed(evt);
            }
        });
        jToolBar1.add(localContext);

        fileContext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/macroview/resources/filescope.png"))); // NOI18N
        fileContext.setToolTipText(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.fileContext.toolTipText")); // NOI18N
        fileContext.setFocusable(false);
        fileContext.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        fileContext.setMaximumSize(new java.awt.Dimension(24, 24));
        fileContext.setMinimumSize(new java.awt.Dimension(24, 24));
        fileContext.setPreferredSize(new java.awt.Dimension(24, 24));
        fileContext.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        fileContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileContextActionPerformed(evt);
            }
        });
        jToolBar1.add(fileContext);
        fileContext.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.localContext.AccessibleContext.accessibleDescription")); // NOI18N

        jSeparator4.setSeparatorSize(new java.awt.Dimension(0, 4));
        jToolBar1.add(jSeparator4);

        prevMacro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/macroview/resources/prevmacro.png"))); // NOI18N
        prevMacro.setToolTipText(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.prevMacro.toolTipText")); // NOI18N
        prevMacro.setFocusable(false);
        prevMacro.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        prevMacro.setMaximumSize(new java.awt.Dimension(24, 24));
        prevMacro.setMinimumSize(new java.awt.Dimension(24, 24));
        prevMacro.setPreferredSize(new java.awt.Dimension(24, 24));
        prevMacro.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        prevMacro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevMacroActionPerformed(evt);
            }
        });
        jToolBar1.add(prevMacro);
        prevMacro.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.prevMacro.AccessibleContext.accessibleDescription")); // NOI18N

        nextMacro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/macroview/resources/nextmacro.png"))); // NOI18N
        nextMacro.setToolTipText(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.nextMacro.toolTipText")); // NOI18N
        nextMacro.setFocusable(false);
        nextMacro.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        nextMacro.setMaximumSize(new java.awt.Dimension(24, 24));
        nextMacro.setMinimumSize(new java.awt.Dimension(24, 24));
        nextMacro.setPreferredSize(new java.awt.Dimension(24, 24));
        nextMacro.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        nextMacro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextMacroActionPerformed(evt);
            }
        });
        jToolBar1.add(nextMacro);
        nextMacro.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MacroExpansionPanel.class, "MacroExpansionPanel.nextMacro.AccessibleContext.accessibleDescription")); // NOI18N

        add(jToolBar1, java.awt.BorderLayout.LINE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void nextMacroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextMacroActionPerformed
        Document doc = jCodeExpansionEditorPane.getDocument();
        if (doc == null) {
            return;
        }
        int offset = CsmMacroExpansion.getNextMacroExpansionStartOffset(doc, jCodeExpansionEditorPane.getCaretPosition());
        if (offset >= 0 && offset < doc.getLength()) {
            jCodeExpansionEditorPane.setCaretPosition(offset);
        }
}//GEN-LAST:event_nextMacroActionPerformed

    private void prevMacroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prevMacroActionPerformed
        Document doc = jCodeExpansionEditorPane.getDocument();
        if (doc == null) {
            return;
        }
        int offset = CsmMacroExpansion.getPrevMacroExpansionStartOffset(doc, jCodeExpansionEditorPane.getCaretPosition());
        if (offset >= 0 && offset < doc.getLength()) {
            jCodeExpansionEditorPane.setCaretPosition(offset);
        }
}//GEN-LAST:event_prevMacroActionPerformed

    private void localContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_localContextActionPerformed
        fileContext.setSelected(false);
        localContext.setSelected(true);
        MacroExpansionTopComponent.setLocalContext(true);
        update();
}//GEN-LAST:event_localContextActionPerformed

    private void fileContextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileContextActionPerformed
        fileContext.setSelected(true);
        localContext.setSelected(false);
        MacroExpansionTopComponent.setLocalContext(false);
        update();
}//GEN-LAST:event_fileContextActionPerformed

    private void autoRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoRefreshActionPerformed
        Document doc = jCodeExpansionEditorPane.getDocument();
        if (doc == null) {
            return;
        }
        MacroExpansionTopComponent.setSyncCaretAndContext(autoRefresh.isSelected());
        update();
}//GEN-LAST:event_autoRefreshActionPerformed

    @Override
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return jCodeExpansionPane.requestFocusInWindow();
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton autoRefresh;
    private javax.swing.JToggleButton fileContext;
    private javax.swing.JEditorPane jCodeExpansionEditorPane;
    private javax.swing.JScrollPane jCodeExpansionPane;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JLabel jStatusBar;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToggleButton localContext;
    private javax.swing.JButton nextMacro;
    private javax.swing.JButton prevMacro;
    // End of variables declaration//GEN-END:variables

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("MacroExpansionWindow"); // NOI18N
    }
}
