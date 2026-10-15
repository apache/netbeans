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

package org.netbeans.modules.options.keymap;

import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * Popup panel for changing shortcuts, invoked by mouseclick over [...] button
 * inside keymap options panel
 * @author Max Sauer
 */
public class ShortcutPopupPanel extends javax.swing.JPanel {

    private static final AbstractListModel modelWithAddAlternative = new Model(true);
    private static final AbstractListModel modelWithoutAddAltenrnative = new Model(false);
    private static AbstractListModel model = new DefaultListModel();

    private int row;
    private JTable table;
    private JPopupMenu pm;
    /** whether 'add alternative' should be displayed */
    private boolean displayAlternative;

    /** Creates new form ShortcutPopup */
    ShortcutPopupPanel(JTable table, JPopupMenu pm) {
        initComponents();
        this.table = table;
        this.pm = pm;
        
        // forward focus to the list & select 1st item in it
        addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                list.requestFocus();
                list.setSelectedIndex(0);
            }
            
        });
        
        // close on ESCape
        list.addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
                    ShortcutPopupPanel.this.pm.setVisible(false);
                }
            }
            
        });
    }

    public void setRow(int row) {
        this.row = row;
    }

    /**
     * Set whether 'Add Alternative' menu item should be displayed
     */
    void setDisplayAddAlternative(boolean shortcutSet) {
        model = shortcutSet ? modelWithAddAlternative : modelWithoutAddAltenrnative;
        list.setModel(model);
        this.displayAlternative = shortcutSet;
        this.setPreferredSize(list.getPreferredSize());
    }

    private void addAlternative() {
        String category = (String) table.getValueAt(row, 2);
        ShortcutAction action = ((ActionHolder) table.getValueAt(row, 0)).getAction();
        Object[] newRow = new Object[]{new ActionHolder(action, true), "", category, ""};
        ((DefaultTableModel) ((TableSorter) table.getModel()).getTableModel()).insertRow(row + 1, newRow);
        pm.setVisible(false);
        table.editCellAt(row + 1, 1);
    }

    private void clear() {
        pm.setVisible(false);
        String scText = (String)table.getValueAt(row, 1);
        ShortcutAction action = ((ActionHolder) table.getValueAt(row, 0)).getAction();
        KeymapViewModel keymapViewModel = (KeymapViewModel) ((TableSorter) table.getModel()).getTableModel();
        if (scText.length() != 0)
            keymapViewModel.getMutableModel().removeShortcut(action, scText);
        if (((ActionHolder) table.getValueAt(row, 0)).isAlternative())
            //alternative SC, remove row
            keymapViewModel.removeRow(row);
        else {
            table.setValueAt("",row, 1); // NOI18N
            keymapViewModel.update();
        }
        return;
    }

    private void resetToDefault() {
        pm.setVisible(false);
        ShortcutAction action = ((ActionHolder) table.getValueAt(row, 0)).getAction();
        KeymapViewModel mod = (KeymapViewModel) ((TableSorter) table.getModel()).getTableModel();
        Collection<ShortcutAction> conflicts = mod.getMutableModel().revertShortcutsToDefault(action, false);
        if (conflicts != null) {
            if (!overrideAll(conflicts)) {
                return;
            }
            mod.getMutableModel().revertShortcutsToDefault(action, true);
        }
        mod.update();
        mod.fireTableDataChanged();
    }

    private boolean overrideAll(Collection<ShortcutAction> actions) {
        JPanel innerPane = new JPanel();
        StringBuffer display = new StringBuffer();
        for(ShortcutAction sc : actions) {
            display.append(" '" + sc.getDisplayName() + "'<br>"); //NOI18N
        }

        innerPane.add(new JLabel(NbBundle.getMessage(KeymapViewModel.class, "Override_All", display))); //NOI18N
        DialogDescriptor descriptor = new DialogDescriptor(
                innerPane,
                NbBundle.getMessage(KeymapViewModel.class, "Conflicting_Shortcut_Dialog"), //NOI18N
                true,
                DialogDescriptor.YES_NO_OPTION,
                null,
                null);
        DialogDisplayer.getDefault().notify(descriptor);

        if (descriptor.getValue().equals(DialogDescriptor.YES_OPTION))
            return true;
        else return false;
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
        list = new javax.swing.JList();

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        list.setModel(model);
        list.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                listMouseMoved(evt);
            }
        });
        list.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                listKeyPressed(evt);
            }
        });
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(list);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void listMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listMouseMoved
        list.setSelectedIndex(list.locationToIndex(new Point(evt.getX(), evt.getY())));
    }//GEN-LAST:event_listMouseMoved

    private void listMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listMouseClicked
        int index = list.locationToIndex(new Point(evt.getX(), evt.getY()));
        itemSelected(index);
    }//GEN-LAST:event_listMouseClicked

    private boolean customProfile;
    
    void setCustomProfile(boolean customProfile) {
        this.customProfile = customProfile;
    }
    
    private void itemSelected(int index) {
        if (displayAlternative) {
        switch (index) {
            case 0: //edit
                pm.setVisible(false);
                table.editCellAt(row, 1);
                break;
            case 1: {//add alternative
                addAlternative();
                break;
            }
            case 2: {//reset to default
                resetToDefault();
                break;
            }
            case 3: {//clear
                clear();
                break;
            }
            default:
                throw new UnsupportedOperationException("Invalid popup selection item"); // NOI18N
            }
        } else {
            switch (index) {
            case 0: //edit
                pm.setVisible(false);
                table.editCellAt(row, 1);
                break;
            case 1: {
                resetToDefault();
                break;
            }
            case 2: {
                clear();
                break;
            }
            default:
                throw new UnsupportedOperationException("Invalid popup selection item"); // NOI18N
            }

        }
    }
    private void listKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_listKeyPressed
        // UP / DOWN just wraps around the list
        // TODO: convert to action & bind to action map
        int index = list.getSelectedIndex();
        if (evt.getKeyCode() == KeyEvent.VK_UP) {
            list.setSelectedIndex(index == 0 ? model.getSize() - 1 : index - 1);
        } 
        if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            list.setSelectedIndex(index == (model.getSize() - 1) ? 0 : index + 1);
        }
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!list.isSelectionEmpty()) {
                itemSelected(list.getSelectedIndex());
            }
        }
        evt.consume();
    }//GEN-LAST:event_listKeyPressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList list;
    // End of variables declaration//GEN-END:variables


    private static class Model extends AbstractListModel {
        private boolean displayAlternative;

        public Model(boolean displayAlternative) {
            this.displayAlternative = displayAlternative;
        }

        String[] elms = {
            NbBundle.getMessage(ShortcutPopupPanel.class, "Edit"), //NOI18N
            NbBundle.getMessage(ShortcutPopupPanel.class, "Add_Alternative"), //NOI18N
            NbBundle.getMessage(ShortcutPopupPanel.class, "Reset_to_Default"), //NOI18N
            NbBundle.getMessage(ShortcutPopupPanel.class, "Clear") //NOI18N
        };

        String[] elms0 = {
            elms[0], elms[2], elms[3]
        };

        @Override
        public int getSize() {
            return displayAlternative == true ? elms.length : elms0.length;
        }

        @Override
        public Object getElementAt(int index) {
            return displayAlternative == true ? elms[index] : elms0[index];
        }

    }

}
