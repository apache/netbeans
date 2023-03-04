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

package org.netbeans.modules.palette.ui;

import java.awt.Dialog;
import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import java.beans.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.openide.awt.Mnemonics;
import org.openide.util.*;
import org.openide.explorer.*;
import org.openide.explorer.view.BeanTreeView;
import org.openide.*;
import org.openide.nodes.*;
import org.netbeans.modules.palette.*;


/**
 * This class provides the UI for managing palette content (adding components
 * etc). Shown to the user as "Palette Customizer" window.
 *
 * @author Tomas Pavek, S. Aubrecht
 */

public class Customizer extends JPanel implements ExplorerManager.Provider,
                                                      Lookup.Provider
{
    private ExplorerManager explorerManager;
    private Lookup lookup;
    
    private Node root;
    private PaletteController controller;
    private Settings settings;
    
    private JButton[] customButtons;

    // ------------

    /**
     * Opens the manager window.
     *
     * @param paletteRoot Palette root node.
     */
    public static void show( Node paletteRoot, PaletteController controller, Settings settings ) {
        JButton closeButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(
            closeButton, Utils.getBundleString("CTL_Close_Button")); // NOI18N
        closeButton.getAccessibleContext().setAccessibleDescription( Utils.getBundleString("ACSD_Close") );
        DialogDescriptor dd = new DialogDescriptor(
            new Customizer( paletteRoot, controller, settings ),
            Utils.getBundleString("CTL_Customizer_Title"), // NOI18N
            false,
            new Object[] { closeButton },
            closeButton,
            DialogDescriptor.DEFAULT_ALIGN,
            null,
            null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
    }

    /** Creates new Customizer */
    public Customizer( Node paletteRoot, PaletteController controller, Settings settings ) {
        this.root = paletteRoot;
        this.controller = controller;
        this.settings = settings;
        explorerManager = new ExplorerManager();

        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(explorerManager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(explorerManager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(explorerManager));
        map.put("delete", ExplorerUtils.actionDelete(explorerManager, true)); // NOI18N

        lookup = ExplorerUtils.createLookup(explorerManager, map);

        explorerManager.setRootContext(paletteRoot);

        initComponents();
        
        createCustomButtons();

        CheckTreeView treeView = new CheckTreeView( settings );
        treeView.getAccessibleContext().setAccessibleName(
            Utils.getBundleString("ACSN_PaletteContentsTree")); // NOI18N
        treeView.getAccessibleContext().setAccessibleDescription(
            Utils.getBundleString("ACSD_PaletteContentsTree")); // NOI18N
        treePanel.add(treeView, java.awt.BorderLayout.CENTER);
        captionLabel.setLabelFor(treeView);

        explorerManager.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                if (ExplorerManager.PROP_SELECTED_NODES.equals(ev.getPropertyName())) {
                    updateInfoLabel(explorerManager.getSelectedNodes());
                    updateButtons();
                }
            }
        });
        updateButtons();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        ExplorerUtils.activateActions(explorerManager, true);
    }

    @Override
    public void removeNotify() {
        ExplorerUtils.activateActions(explorerManager, false);
        super.removeNotify();
    }

    // ExplorerManager.Provider
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    // Lookup.Provider from TopComponent
    public Lookup getLookup() {
        return lookup;
    }

    private void updateButtons() {
        Node[] selNodes = explorerManager.getSelectedNodes();
        boolean canRemove = null != selNodes && selNodes.length > 0;
        boolean canMoveUp = null != selNodes && selNodes.length == 1;
        boolean canMoveDown = null != selNodes && selNodes.length == 1;
        
        for( int i=0; null != selNodes && i<selNodes.length; i++ ) {
            Node node = selNodes[i];
            if( !node.canDestroy() )
                canRemove = false;
            
            Node parent = node.getParentNode();
            if( null == parent || movePossible( node, parent, true ) < 0 )
                canMoveUp = false;
            if( null == parent || movePossible( node, parent, false ) < 0 )
                canMoveDown = false;
        }
        removeButton.setEnabled( canRemove );
        moveUpButton.setEnabled( canMoveUp );
        moveDownButton.setEnabled( canMoveDown );
        newCategoryButton.setEnabled( new Utils.NewCategoryAction( root ).isEnabled() );
    }
    // -------

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        captionLabel = new javax.swing.JLabel();
        treePanel = new javax.swing.JPanel();
        infoLabel = new javax.swing.JLabel();
        moveUpButton = new javax.swing.JButton();
        moveDownButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        newCategoryButton = new javax.swing.JButton();
        customActionsPanel = new javax.swing.JPanel();
        resetButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(captionLabel, Utils.getBundleString("CTL_Caption")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 10);
        add(captionLabel, gridBagConstraints);

        treePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        treePanel.setPreferredSize(new java.awt.Dimension(288, 336));
        treePanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(treePanel, gridBagConstraints);

        infoLabel.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(infoLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(moveUpButton, Utils.getBundleString("CTL_MoveUp_Button")); // NOI18N
        moveUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(28, 12, 0, 10);
        add(moveUpButton, gridBagConstraints);
        moveUpButton.getAccessibleContext().setAccessibleDescription(Utils.getBundleString("ACSD_MoveUp")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(moveDownButton, Utils.getBundleString("CTL_MoveDown_Button")); // NOI18N
        moveDownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 10);
        add(moveDownButton, gridBagConstraints);
        moveDownButton.getAccessibleContext().setAccessibleDescription(Utils.getBundleString("ACSD_MoveDown")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, Utils.getBundleString("CTL_Remove_Button")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 10);
        add(removeButton, gridBagConstraints);
        removeButton.getAccessibleContext().setAccessibleDescription(Utils.getBundleString("ACSD_Remove")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(newCategoryButton, Utils.getBundleString("CTL_NewCategory_Button")); // NOI18N
        newCategoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newCategoryButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 10);
        add(newCategoryButton, gridBagConstraints);
        newCategoryButton.getAccessibleContext().setAccessibleDescription(Utils.getBundleString("ACSD_NewCategory")); // NOI18N

        customActionsPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(customActionsPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(resetButton, Utils.getBundleString("CTL_ResetPalette")); // NOI18N
        resetButton.setActionCommand("Reset Palette");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(28, 12, 0, 10);
        add(resetButton, gridBagConstraints);
        resetButton.getAccessibleContext().setAccessibleName(Utils.getBundleString("ASCN_ResetPalette")); // NOI18N
        resetButton.getAccessibleContext().setAccessibleDescription(Utils.getBundleString("ASCD_ResetPalette")); // NOI18N

        getAccessibleContext().setAccessibleDescription(Utils.getBundleString("ACSD_PaletteCustomizer")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        Utils.resetPalette( controller, settings );
    }//GEN-LAST:event_resetButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        Node[] selected = explorerManager.getSelectedNodes();
        if (selected.length == 0)
            return;

        if( selected.length == 1 && !selected[0].canDestroy() )
            return;
        
        // first user confirmation...
        NotifyDescriptor desc = new NotifyDescriptor.Confirmation(
            Utils.getBundleString("MSG_ConfirmPaletteDelete"), // NOI18N
            Utils.getBundleString("CTL_ConfirmDeleteTitle"), // NOI18N
            NotifyDescriptor.YES_NO_OPTION);

        if (NotifyDescriptor.YES_OPTION.equals(
                    DialogDisplayer.getDefault().notify(desc)))
        {
            try {
                for (int i=0; i < selected.length; i++) {
                    if( selected[i].canDestroy() )
                        selected[i].destroy();
                }
            }
            catch (java.io.IOException e) {
                Logger.getLogger( getClass().getName() ).log( Level.INFO, null, e );
            }
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void moveDownButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownButtonActionPerformed
        moveNode(false);
    }//GEN-LAST:event_moveDownButtonActionPerformed

    private void moveUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpButtonActionPerformed
        moveNode(true);
    }//GEN-LAST:event_moveUpButtonActionPerformed

    private void newCategoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newCategoryButtonActionPerformed
        new Utils.NewCategoryAction( root ).actionPerformed( evt );
    }//GEN-LAST:event_newCategoryButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel captionLabel;
    private javax.swing.JPanel customActionsPanel;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JButton moveDownButton;
    private javax.swing.JButton moveUpButton;
    private javax.swing.JButton newCategoryButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton resetButton;
    private javax.swing.JPanel treePanel;
    // End of variables declaration//GEN-END:variables

    private void moveNode(boolean up) {
        Node[] selected = explorerManager.getSelectedNodes();
        if (selected.length != 1)
            return;

        Node node = selected[0];
        Node parent = node.getParentNode();
        if (parent == null)
            return;

        Index indexCookie = parent.getCookie(Index.class);
        if (indexCookie == null)
            return;

        int index = movePossible(node, parent, up);
        if (index != -1) {
            if (up) {
                indexCookie.moveUp(index);
            } else {
                indexCookie.moveDown(index);
            }
        }
    }

    private static int movePossible(Node node, Node parentNode, boolean up) {
        if (parentNode == null)
            return -1;

        Node[] nodes = parentNode.getChildren().getNodes();
        for (int i=0; i < nodes.length; i++)
            if (nodes[i].getName().equals(node.getName()))
                return (up && i > 0) || (!up && i+1 < nodes.length) ? i : -1;

        return -1;
    }

    private void updateInfoLabel(org.openide.nodes.Node[] nodes) {
        String text = " "; // NOI18N
        if (nodes.length == 1) {
            Item item = nodes[0].getCookie( Item.class );
            if (item != null)
                text = item.getShortDescription(); //TODO revisit PaletteSupport.getItemComponentDescription(item);
        }
        infoLabel.setText(text);
    }
    
    private void createCustomButtons() {
        PaletteActions customActions = root.getLookup().lookup( PaletteActions.class );
        if( null == customActions )
            return;
        
        Action[] actions = customActions.getImportActions();
        if( null == actions || actions.length == 0 )
            return;
        
        customButtons = new JButton[actions.length];
        for( int i=0; i<actions.length; i++ ) {
            customButtons[i] = new JButton( actions[i] );
            if( null != actions[i].getValue( Action.NAME ) )
                Mnemonics.setLocalizedText( customButtons[i], actions[i].getValue( Action.NAME ).toString() );
            if( null != actions[i].getValue( Action.LONG_DESCRIPTION ) )
                customButtons[i].getAccessibleContext().setAccessibleDescription( actions[i].getValue( Action.LONG_DESCRIPTION ).toString() );
            java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = i;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 10);
            customActionsPanel.add( customButtons[i], gridBagConstraints);
        }
    }
    
    
    private static class CheckTreeView extends BeanTreeView {
        /** Creates a new instance of CheckTreeView */
        public CheckTreeView( Settings settings ) {
            if( settings instanceof DefaultSettings ) {
                CheckListener l = new CheckListener( (DefaultSettings)settings );
                tree.addMouseListener( l );
                tree.addKeyListener( l );

                CheckRenderer check = new CheckRenderer( (DefaultSettings)settings );
                tree.setCellRenderer( check );
            }
            tree.setEditable(false);
        }
    }
}
