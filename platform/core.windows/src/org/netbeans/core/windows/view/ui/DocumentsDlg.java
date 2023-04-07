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

package org.netbeans.core.windows.view.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.Switches;
import org.netbeans.core.windows.TopComponentTracker;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.cookies.SaveCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.ListView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

/**
 * Panel to display list of TopComponents in editor area.
 *
 * @author  Marek Slama
 */
public class DocumentsDlg extends JPanel implements PropertyChangeListener, ExplorerManager.Provider, HelpCtx.Provider {
    
    private static DocumentsDlg defaultInstance;
    
    private final ExplorerManager explorer = new ExplorerManager();
    private Dimension previousDialogSize;

    /** Creates new form DocumentsDlg */
    private DocumentsDlg () {
        initComponents();
        
        // Internationalize.
	Mnemonics.setLocalizedText(jButtonActivate, NbBundle.getMessage(DocumentsDlg.class, "LBL_Activate"));
        Mnemonics.setLocalizedText(jButtonClose, NbBundle.getMessage(DocumentsDlg.class, "LBL_CloseDocuments"));
        Mnemonics.setLocalizedText(jButtonSave, NbBundle.getMessage(DocumentsDlg.class, "LBL_SaveDocuments"));
        Mnemonics.setLocalizedText(explorerLabel, NbBundle.getMessage(DocumentsDlg.class, "LBL_Documents"));
        Mnemonics.setLocalizedText(descriptionLabel, NbBundle.getMessage(DocumentsDlg.class, "LBL_Description"));
        
        Mnemonics.setLocalizedText(closeButton, NbBundle.getMessage(DocumentsDlg.class, "LBL_Close"));
            
        // Set labels for.
        explorerLabel.setLabelFor(listView);
        descriptionLabel.setLabelFor(descriptionArea);
        
        // Accessible context.
        jButtonActivate.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DocumentsDlg.class, "ACSD_Activate"));
        jButtonClose.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DocumentsDlg.class, "ACSD_CloseDocuments"));
        jButtonSave.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DocumentsDlg.class, "ACSD_SaveDocuments"));
        closeButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DocumentsDlg.class, "ACSD_Close"));
        descriptionArea.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DocumentsDlg.class, "ACSD_DescriptionArea"));

        ItemListener orderingListener = new ItemListener() {

            @Override
            public void itemStateChanged( ItemEvent e ) {
                updateNodes();
            }
        };
        radioOrderByName.addItemListener( orderingListener );
        radioOrderByUsage.addItemListener( orderingListener );
    }
    

    private static DocumentsDlg getDefault() {
        if(defaultInstance == null) {
            defaultInstance = new DocumentsDlg();
        }
        return defaultInstance;
    }
    
    /** Gets <code>HelpCtx</code>. Implements <code>HelpCtx.Provider</code>. */
    @Override
    public HelpCtx getHelpCtx() {
        // PENDING replace by id string.
        return new HelpCtx(DocumentsDlg.class);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        explorerLabel = new javax.swing.JLabel();
        descriptionLabel = new javax.swing.JLabel();
        explorerPanel = createListView();
        jScrollPane1 = new javax.swing.JScrollPane();
        descriptionArea = new javax.swing.JTextArea();
        jButtonActivate = new javax.swing.JButton();
        jButtonClose = new javax.swing.JButton();
        jButtonSave = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        lblOrderBy = new javax.swing.JLabel();
        radioOrderByName = new javax.swing.JRadioButton();
        radioOrderByUsage = new javax.swing.JRadioButton();

        FormListener formListener = new FormListener();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 0, 11));
        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(explorerLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(descriptionLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(explorerPanel, gridBagConstraints);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(3, 60));

        descriptionArea.setEditable(false);
        jScrollPane1.setViewportView(descriptionArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jScrollPane1, gridBagConstraints);

        jButtonActivate.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jButtonActivate, gridBagConstraints);

        jButtonClose.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jButtonClose, gridBagConstraints);

        jButtonSave.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        add(jButtonSave, gridBagConstraints);

        closeButton.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(closeButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(lblOrderBy, NbBundle.getMessage(DocumentsDlg.class, "Lbl_OrderBy")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(lblOrderBy, gridBagConstraints);

        buttonGroup1.add(radioOrderByName);
        radioOrderByName.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(radioOrderByName, NbBundle.getMessage(DocumentsDlg.class, "Lbl_OrderByName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 20, 0, 0);
        add(radioOrderByName, gridBagConstraints);

        buttonGroup1.add(radioOrderByUsage);
        org.openide.awt.Mnemonics.setLocalizedText(radioOrderByUsage, NbBundle.getMessage(DocumentsDlg.class, "Lbl_OrderByUsage")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 20, 10, 0);
        add(radioOrderByUsage, gridBagConstraints);
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == jButtonActivate) {
                DocumentsDlg.this.activate(evt);
            }
            else if (evt.getSource() == jButtonClose) {
                DocumentsDlg.this.closeDocuments(evt);
            }
            else if (evt.getSource() == jButtonSave) {
                DocumentsDlg.this.saveDocuments(evt);
            }
            else if (evt.getSource() == closeButton) {
                DocumentsDlg.this.closeButtonActionPerformed(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        closeDialog();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void saveDocuments(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveDocuments
        // Add your handling code here:
        Node[] selNodes = explorer.getSelectedNodes();
        if (selNodes.length == 0) {
            return;
        }
        for (int i = 0; i < selNodes.length; i++) {
            TopComponent tc = ((TopComponentNode) selNodes[i]).getTopComponent();
            Lookup l = tc.getLookup();
            SaveCookie sc = (SaveCookie) l.lookup(SaveCookie.class);
            if (sc != null) {
                try {
                    sc.save();
                } catch (IOException exc) {
                    Logger.getAnonymousLogger().log(Level.WARNING,
                    "[WinSys.DocumentsDlg.saveDocuments]" // NOI18N
                    + " Warning: Cannot save content of TopComponent: [" // NOI18N
                    + WindowManagerImpl.getInstance().getTopComponentDisplayName(tc) + "]" // NOI18N
                    + " [" + tc.getClass().getName() + "]", exc); // NOI18N
                }
                //Refresh name of node because TopComponent name is probably changed too
                //('*' is removed)
                ((TopComponentNode) selNodes[i]).refresh();
            }
        }
        jButtonSave.setEnabled(false);
    }//GEN-LAST:event_saveDocuments

    private void closeDocuments(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeDocuments
        // Add your handling code here:
        Node[] selNodes = explorer.getSelectedNodes();
        if (selNodes.length == 0) {
            return;
        }
        //#70965 begin - select document previous to first closed document
        int positionToSelectAfter = 0;
        Node[] children = explorer.getRootContext().getChildren().getNodes();
        for (int i = 0; i < children.length; i++) {
            if (children[i] == selNodes[0]) {
                positionToSelectAfter = Math.max(0, i - 1);
                break;
            }
        }
        //#70965 end
        for (int i = 0; i < selNodes.length; i++) {
            TopComponent tc = ((TopComponentNode) selNodes[i]).getTopComponent();
            if( Switches.isEditorTopComponentClosingEnabled() && Switches.isClosingEnabled( tc ) )
                tc.close();
        }
        
        List<TopComponent> tcList = getOpenedDocuments();
        List<TopComponentNode> tcNodes = new ArrayList<TopComponentNode> (tcList.size());
        for (TopComponent tc : tcList) {
            tcNodes.add(new TopComponentNode(tc));
        }
        
        if(tcList.isEmpty()) {
            // No opened documents left, close the dialog.
            closeDialog();
        } else {
            // Update list view.
            if( radioOrderByName.isSelected() ) {
                java.util.Collections.sort(tcNodes);
            }
            Children.Array nodeArray = new Children.Array();
            nodeArray.add((TopComponentNode[])tcNodes.toArray(new TopComponentNode[0]));
            Node root = new AbstractNode(nodeArray);
            explorer.setRootContext(root);
            //#54656 begin
            try {
                explorer.setSelectedNodes(new Node[]{root.getChildren().getNodes()[positionToSelectAfter]});
            } catch (PropertyVetoException exc) {
                //mkleint - well, what can we do, I've never seen the selection being vetoed anyway.
            }
            listView.requestFocusInWindow();
            //#54656 end
        }
    }//GEN-LAST:event_closeDocuments

    private void activate(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activate
        // Add your handling code here:
        Node[] selNodes = explorer.getSelectedNodes();
        if (selNodes.length == 0) {
            return;
        }
        
        closeDialog();
        
        final TopComponent tc = ((TopComponentNode) selNodes[0]).getTopComponent();
        //Call using invokeLater to make sure it is performed after dialog
        //is closed.
        SwingUtilities.invokeLater(new Runnable () {
            public void run() {
                // #37226-41075 Unmaximized the other mode if needed.
                WindowManagerImpl wm = WindowManagerImpl.getInstance();
                ModeImpl mode = (ModeImpl)wm.findMode(tc);
                if(mode != null && mode != wm.getCurrentMaximizedMode()) {
                    wm.switchMaximizedMode(null);
                }
                tc.requestActive();
            }
        });
    }//GEN-LAST:event_activate

    private void closeDialog() {
        //save dialog size on closing via dialog buttons
        getDefault().previousDialogSize=this.getSize();

        Window w = SwingUtilities.getWindowAncestor(this);
        w.setVisible(false);
        w.dispose();
    }

    @Override
    public void addNotify () {
        explorer.addPropertyChangeListener(this);
        jButtonActivate.setEnabled(false);
        jButtonClose.setEnabled(false);
        jButtonSave.setEnabled(false);
        super.addNotify();
    }

    @Override
    public void removeNotify () {
        // save dialog size on click at [x] in titlebar
        Window dlg = SwingUtilities.getWindowAncestor(this);
        if( null != dlg )
            getDefault().previousDialogSize = dlg.getSize();
        super.removeNotify();
        explorer.removePropertyChangeListener(this);
    }


    public static void showDocumentsDialog() {
        DocumentsDlg documentsPanel = getDefault();
        DialogDescriptor dlgDesc = new DialogDescriptor(
            documentsPanel,
            NbBundle.getMessage(DocumentsDlg.class, "CTL_DocumentsTitle"),
            true, // is modal!!
            new Object[0],
            // make "switcch to document" button default
            getDefault().jButtonActivate,
            DialogDescriptor.DEFAULT_ALIGN,
            null,
            null
        );
        dlgDesc.setHelpCtx( null ); //hide the default Help button
        final Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);
        dlg.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DocumentsDlg.class, "ACSD_DocumentsDialog"));
        if( dlg instanceof JDialog ) {
            HelpCtx.setHelpIDString(((JDialog)dlg).getRootPane(), documentsPanel.getHelpCtx().getHelpID());
        }
        getDefault().updateNodes();
        
        if (getDefault().previousDialogSize != null) {
            dlg.setSize(getDefault().previousDialogSize);
            dlg.setLocationRelativeTo(null);
        }

        dlg.setVisible(true);
        getDefault().clearNodes();
    }
    
    /**
     * Tells whether documents list is empty or not.
     * 
     * @return true when document list contains no documents, false otherwise.
     */ 
    public static boolean isEmpty () {
        for(Object elem : WindowManagerImpl.getInstance().getModes()) {
            ModeImpl mode = (ModeImpl)elem;
            if(mode.getKind() == Constants.MODE_KIND_EDITOR && 
               !mode.getOpenedTopComponents().isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    private JPanel createListView () {
        JPanel panel = new JPanel();
        // Defined size in #36907 - surrounding controls will add to this size
        // and result is desired 540x400. Note that we can't hardcode size of
        // whole dialog to work correctly with different font size
        panel.setPreferredSize(new Dimension(375, 232));
        panel.setLayout(new BorderLayout());
        listView = new ListView();
        // proper border for the view
        listView.setBorder((Border)UIManager.get("Nb.ScrollPane.border")); // NOI18N
        listView.setPopupAllowed(false);
        listView.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DocumentsDlg.class, "ACSD_ListView"));
        //view.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listView.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "closeSelected") ;//NOI18N
        listView.getActionMap().put( "closeSelected", new AbstractAction() {//NOI18N
            @Override
            public void actionPerformed( ActionEvent e ) {
                closeDocuments(e );
            }

        });
        panel.add(listView, BorderLayout.CENTER);
        return panel;
    }
    
    private void updateNodes() {
        //Create nodes for TopComponents, sort them using their own comparator
        List<TopComponent> tcList = getOpenedDocuments();
        TopComponent activeTC = TopComponent.getRegistry().getActivated();
        TopComponentNode[] tcNodes = new TopComponentNode[tcList.size()];
        TopComponentNode toSelect = null;
        for (int i = 0; i < tcNodes.length; i++) {
            TopComponent tc = tcList.get(i);
            tcNodes[i] = new TopComponentNode(tc);
            if( tc == activeTC ) {
                toSelect = tcNodes[i];
            }
        }
        if( radioOrderByName.isSelected() ) {
            Arrays.sort(tcNodes);
        }
        
        Children.Array nodeArray = new Children.Array();
        nodeArray.add(tcNodes);
        Node root = new AbstractNode(nodeArray);
        explorer.setRootContext(root);
        // set focus to documents list
        listView.requestFocus();
        // select the active editor tab or the first item if possible
        if (tcNodes.length > 0) {
            try {
                if( null == toSelect ) 
                    toSelect = tcNodes[0];
                explorer.setSelectedNodes(new Node[] {toSelect} );
            } catch (PropertyVetoException exc) {
                // do nothing, what should I do?
            }
        }
    }
    
    private void clearNodes() {
        explorer.setRootContext(Node.EMPTY);
    }

    private static List<TopComponent> getOpenedDocuments() {
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        TopComponent[] recentDocuments = wm.getRecentViewList();
        List<TopComponent> documents = new ArrayList<TopComponent> (recentDocuments.length);
        TopComponentTracker tcTracker = TopComponentTracker.getDefault();
        for( TopComponent tc : recentDocuments ) {
            if( tcTracker.isEditorTopComponent( tc ) ) {
                documents.add( tc );
            }
        }
        
        return documents;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            Node [] selNodes = (Node []) evt.getNewValue();
            //Enable button Activate only if one node is selected
            if (selNodes.length == 1) {
                jButtonActivate.setEnabled(true);
            } else {
                jButtonActivate.setEnabled(false);
            }
            //Enable button Close only if at least one node is selected
            if (selNodes.length > 0) {
                if( selNodes.length == 1 ) {
                    TopComponent tc = ((TopComponentNode) selNodes[0]).getTopComponent();
                    jButtonClose.setEnabled(Switches.isEditorTopComponentClosingEnabled()
                            && Switches.isClosingEnabled( tc ));
                } else {
                    jButtonClose.setEnabled(Switches.isEditorTopComponentClosingEnabled());
                }
            } else {
                jButtonClose.setEnabled(false);
            }
            //Check if any TopComponent in selection is modified.
            //If TopComponent lookup returns SaveCookie
            boolean enableSave = false;
            for (int i = 0; i < selNodes.length; i++) {
                TopComponent tc = ((TopComponentNode) selNodes[i]).getTopComponent();
                Lookup l = tc.getLookup();
                SaveCookie sc = (SaveCookie) l.lookup(SaveCookie.class);
                if (sc != null) {
                    enableSave = true;
                    break;
                }
            }
            jButtonSave.setEnabled(enableSave);
            
            // Set description.
            if(selNodes != null && selNodes.length == 1) {
                descriptionArea.setText(((TopComponentNode)selNodes[0]).getDescription());
            } else {
                descriptionArea.setText(null);
            }
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton closeButton;
    private javax.swing.JTextArea descriptionArea;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JLabel explorerLabel;
    private javax.swing.JPanel explorerPanel;
    private javax.swing.JButton jButtonActivate;
    private javax.swing.JButton jButtonClose;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblOrderBy;
    private javax.swing.JRadioButton radioOrderByName;
    private javax.swing.JRadioButton radioOrderByUsage;
    // End of variables declaration//GEN-END:variables
    private ListView listView;
    
    public ExplorerManager getExplorerManager() {
        return explorer;
    }
    
    private static final Collator COLLATOR = Collator.getInstance();

    /** Used to display list of TopComponent in ListView. */
    private class TopComponentNode extends AbstractNode
                                   implements Comparable<TopComponentNode>, Action, PropertyChangeListener {
        
        private TopComponent tc;
        
        public TopComponentNode (TopComponent tc) {
            super(Children.LEAF);
            this.tc = tc;
            tc.addPropertyChangeListener(WeakListeners.propertyChange(this, tc));
        }
        
        public String getName() {
            // #60263: apparently used by functional tests.
            String result = tc.getName();
            if( null == result )
                result = tc.getDisplayName();
            if( null == result )
                result = tc.toString();
            return result;
        }
        public String getDisplayName() {
            // Also #60263. Forms do not have a tc.name??
            String result = tc.getDisplayName();
            return result != null ? result : tc.getName();
        }

        public String getHtmlDisplayName() {
            return tc.getHtmlDisplayName();
        }
        
        public Image getIcon (int type) {
            Image image = tc.getIcon();
            return image == null ? ImageUtilities.loadImage("org/openide/resources/actions/empty.gif") : image; // NOI18N
        }
        
        public String getDescription() {
            return tc.getToolTipText();
        }

        @Override
        public String getShortDescription() {
            return tc.getToolTipText();
        }
        
        public TopComponent getTopComponent () {
            return tc;
        }
        
        /** Force refresh of node name in ListView. */
        void refresh () {
            fireNameChange(null, null);
        }
        
        public int compareTo(TopComponentNode tcn) {
            String displayName1 = getDisplayName();
            String displayName2 = tcn.getDisplayName();
            
            if(displayName1 == null) {
                return displayName2 == null ? 0 : -1;
            } else {
                return displayName2 == null ? 1 : COLLATOR.compare(displayName1, displayName2);
            }
        }
        
        /** Invokes itself ac action when double click or Enter pressed on node
         */
        public Action getPreferredAction() {
            return this;
        }

        /** Implementation of Action interface, activates TopComponent
         * currently selected in the list view (should be the same component
         * that is asociated with this Node) 
         */
        public void actionPerformed(ActionEvent evt) {
            activate(evt);
        }
        
        public boolean isEnabled() {
            return true;
        }
        
        public void putValue(String key, Object value) {
            // no operation
        }
        
        public void setEnabled(boolean b) {
            // no operation
        }

        public void propertyChange(PropertyChangeEvent evt) {
            fireDisplayNameChange(null, null);
        }
        
    }
}
