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

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.Utilities;


/**
 *
 * @author Max Sauer
 */
@OptionsPanelController.Keywords(keywords={"#KW_KeymapOptions"}, location=OptionsDisplayer.KEYMAPS)
public class KeymapPanel extends javax.swing.JPanel implements ActionListener, Popupable, ChangeListener {

    // Delay times for incremental search [ms]
    private static final int SEARCH_DELAY_TIME_LONG = 300; // < 3 chars
    private static final int SEARCH_DELAY_TIME_SHORT = 20; // >= 3 chars

    private volatile KeymapViewModel keymapModel;
    private TableSorter sorter;

    private JPopupMenu popup = new JPopupMenu();

    private boolean ignoreActionEvents;
    
    //search fields
    private Popup searchPopup;
    private SpecialkeyPanel specialkeyList;


    /** Creates new form KeymapPanel */
    public KeymapPanel() {
        sorter = new TableSorter(getModel());
        initComponents();
        specialkeyList = new SpecialkeyPanel(this, searchSCField);
        
        // close the popup when user clicks elsewhere
        moreButton.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                hidePopup();
            }
        });

        sorter.setTableHeader(actionsTable.getTableHeader());
        sorter.getTableHeader().setReorderingAllowed(false);
        actionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        actionsTable.setAutoscrolls(true);
        
        ActionListener al = (ActionEvent e) -> {
            getModel().setSearchText(searchField.getText());
            getModel().update();
        };

        final Timer searchDelayTimer = new Timer(SEARCH_DELAY_TIME_LONG, al);
        searchDelayTimer.setRepeats(false);

        searchField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                searchSCField.setText("");
                ((ShortcutListener)searchSCField.getKeyListeners()[0]).clear();
                
                if (searchField.getText().length() > 3)
                    searchDelayTimer.setInitialDelay(SEARCH_DELAY_TIME_SHORT);
                searchDelayTimer.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (searchField.getText().length() > 3)
                    searchDelayTimer.setInitialDelay(SEARCH_DELAY_TIME_LONG);
                searchDelayTimer.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchSCField.setText("");
                getModel().setSearchText(searchField.getText());
                getModel().update();
            }
        });

        searchSCField.addKeyListener(new ShortcutListener(false));

        ActionListener al2 = (ActionEvent e) -> narrowByShortcut();

        final Timer searchDelayTimer2 = new Timer(SEARCH_DELAY_TIME_SHORT, al2);
        searchDelayTimer2.setRepeats(false);
        searchSCField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                searchField.setText("");
                searchDelayTimer2.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchDelayTimer2.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchDelayTimer2.restart();
            }
        });

        actionsTable.addMouseListener(new ButtonCellMouseListener(actionsTable));
        actionsTable.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_CONTEXT_MENU &&
                    e.getKeyCode() != KeyEvent.VK_F2) {
                    return;
                }
		int leadRow = actionsTable.getSelectionModel().getLeadSelectionIndex();
		int leadColumn = actionsTable.getColumnModel().getSelectionModel().
		                   getLeadSelectionIndex();
		if (leadRow != -1 && leadColumn != -1 && !actionsTable.isEditing()) {
                    showPopupMenu(leadRow, leadColumn, -1, -1);
                    e.consume();
		}
            }
            
        });
        TableColumn column = actionsTable.getColumnModel().getColumn(1);
        column.setCellEditor(new ButtonCellEditor(getModel()));
        column.setCellRenderer(new ButtonCellRenderer(actionsTable.getDefaultRenderer(ButtonCellRenderer.class)));
        setColumnWidths();
        popupPanel = new ShortcutPopupPanel(actionsTable, popup);
        popup.add(popupPanel);
        cbProfile.addActionListener(this);
        manageButton.addActionListener(this);
        btnPrintAsHTML.setVisible(ExportShortcutsAction.getExportShortcutsToHTMLAction().isEnabled());
    }
    
    private ShortcutPopupPanel popupPanel;

    private class KeymapTable extends JTable {
        int lastRow;
        int lastColumn;

        @Override
        public boolean editCellAt(int row, int column) {
            lastRow = row;
            lastColumn = column;

            boolean editCellAt = super.editCellAt(row, column);
            ((DefaultCellEditor) getCellEditor(lastRow, lastColumn)).getComponent().requestFocus();
           return editCellAt;
        }

        @Override
        protected void processKeyEvent(KeyEvent e) {

            if (!isEditing())
                super.processKeyEvent(e);
            else {
                Component component = ((DefaultCellEditor) getCellEditor(lastRow, lastColumn)).getComponent();
                component.requestFocus();
                component.dispatchEvent(new KeyEvent(component, e.getID(), e.getWhen(), e.getModifiers(), e.getKeyCode(), e.getKeyChar()));
            }
        }
        
        private String selectedActionId;

        @Override
        public void valueChanged(ListSelectionEvent e) {
            super.valueChanged(e);
            if (!e.getValueIsAdjusting()) {
                int index = getSelectedRow();
                selectedActionId = getActionId(index);
            }
        }
        
        

        @Override
        public void sorterChanged(RowSorterEvent e) {
            String aid = selectedActionId;
            int colIndex = getSelectedColumn();
            super.sorterChanged(e);
            restoreSelection(aid, colIndex);
        }
        
        private void restoreSelection(String id, int colIndex) {
            if (id == null) {
                clearSelection();
                return;
            }
            TableModel tm = getModel();
            for (int i = 0; i < tm.getRowCount(); i++) {
                ActionHolder ah = (ActionHolder)tm.getValueAt(i, 0);
                if (ah != null && id.equals(ah.getAction().getId())) {
                    changeSelection(i, colIndex, false, false);
                    break;
                }
            }
        }
        
        private String getActionId(int modelIndex) {
            if (modelIndex >= 0 && modelIndex < getModel().getRowCount()) {
                ActionHolder h = (ActionHolder)getModel().getValueAt(modelIndex, 0);
                if (h != null) {
                    ShortcutAction sa = h.getAction();
                    return sa.getId();
                }
            }
            return null;
        }
        
        @Override
        public void tableChanged(TableModelEvent e) {
            String aid = selectedActionId;
            // preserve also table column selection:
            int colIndex = getSelectedColumn();
            super.tableChanged(e);
            restoreSelection(aid, colIndex);
        }

    }

    //todo: merge with update
    private void narrowByShortcut() {
        if (searchSCField.getText().length() != 0) {
            final String searchText = searchSCField.getText();
            getModel().runWithoutEvents(() -> {
                getModel().getDataVector().removeAllElements();
                
                for(List<String> categories : getModel().getCategories().values()) {
                    for(String category : categories) {
                        for (Object o : getMutableModel().getItems(category, false)) {
                            ShortcutAction sca = (ShortcutAction) o;
                            String[] shortcuts = getMutableModel().getShortcuts(sca);
                            for (int i = 0; i < shortcuts.length; i++) {
                                String shortcut = shortcuts[i];
                                if (searched(shortcut, searchText))
                                    getModel().addRow(new Object[]{new ActionHolder(sca, false), shortcut, category, ""});
                            }
                        }
                    }
                }
            });
            getModel().fireTableDataChanged();
        } else
            getModel().update();
    }

    KeymapViewModel getModel() {
        if (keymapModel == null) {
            KeymapViewModel tmpModel = new KeymapViewModel();
            synchronized (this) {
                if (keymapModel == null) {
                    keymapModel = tmpModel;
                    tmpModel.getMutableModel().addChangeListener(this);
                }
            }
        }
        return keymapModel;
    }
    
    MutableShortcutsModel getMutableModel() {
        return getModel().getMutableModel();
    }

    //controller methods
    void applyChanges() {
        stopCurrentCellEditing();
        getMutableModel().apply();
    }

    void cancel() {
        stopCurrentCellEditing();
        if (keymapModel == null)
            return;
        getMutableModel().cancel();
    }

    boolean dataValid() {
        return true;
    }

    boolean isChanged() {
        return getMutableModel().isChanged();
    }

    void update() {
        //do not remember search state
        getModel().setSearchText(""); //NOI18N
        searchSCField.setText("");
        ((ShortcutListener)searchSCField.getKeyListeners()[0]).clear();
        searchField.setText(""); //NOI18N

        //setup profiles
        refreshProfileCombo ();
        
        class I implements Runnable, TaskListener {
            int stage;
            
            @Override
            public void run() {
                if (stage > 0) {
                    ((CardLayout)actionsView.getLayout()).show(actionsView, "actions"); // NOI18N
                } else {
                    getMutableModel().refreshActions();
                    Task t = getModel().postUpdate();
                    t.addTaskListener(this);
                }
            }
            
            @Override
            public void taskFinished(Task t) {
                stage++;
                SwingUtilities.invokeLater(this);
            } 
        }

        //update model
        KeymapModel.RP.post(new I());
    }

    //controller method end


    private void refreshProfileCombo() {
        ignoreActionEvents = true;
        String currentProfile = getMutableModel().getCurrentProfile();
        List keymaps = getMutableModel().getProfiles();
        ComboBoxModel model = new DefaultComboBoxModel(keymaps.toArray());
        currentProfile = getMutableModel().getProfileDisplayName(currentProfile);
        cbProfile.setModel(model);
        cbProfile.setSelectedItem(currentProfile);
        ignoreActionEvents = false;
    }

    private void stopCurrentCellEditing() {
        int row = actionsTable.getEditingRow();
        int col = actionsTable.getEditingColumn();
        if (row != -1)
            actionsTable.getCellEditor(row,col).stopCellEditing();
    }

    /**
     * @param shortcut shortcut compared with searched text
     * @return true if search text is empty || shortcut starts with or contains
     * searchtext
     */
    private boolean searched(String shortcut, String searchText) {
        //shortcut.equals(searchSCField.getText())
        if (searchText.length() == 0 || shortcut.startsWith(searchText) ||
                shortcut.contains(searchText))
            return true;
        else
            return false;
    }


    /**
     * Adjust column widths
     */
    private void setColumnWidths() {
        TableColumn column = null;
        for (int i = 0; i < actionsTable.getColumnCount(); i++) {
            column = actionsTable.getColumnModel().getColumn(i);
            switch (i) {
                case 0:
                    column.setPreferredWidth(250);
                    break;
                case 1:
                    column.setPreferredWidth(175);
                    break;
                case 2:
                    column.setPreferredWidth(60);
                    break;
                case 3:
                    column.setPreferredWidth(60);
                    break;
            }
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

        lProfile = new javax.swing.JLabel();
        cbProfile = new javax.swing.JComboBox();
        manageButton = new javax.swing.JButton();
        searchField = new javax.swing.JTextField();
        searchLabel = new javax.swing.JLabel();
        searchSCLabel = new javax.swing.JLabel();
        searchSCField = new javax.swing.JTextField();
        moreButton = new javax.swing.JButton();
        actionsView = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        actionProgress = new javax.swing.JProgressBar();
        waitLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        actionsTable = new KeymapTable();
        btnPrintAsHTML = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();

        lProfile.setLabelFor(cbProfile);
        org.openide.awt.Mnemonics.setLocalizedText(lProfile, org.openide.util.NbBundle.getMessage(KeymapPanel.class, "CTL_Keymap_Name")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(manageButton, org.openide.util.NbBundle.getMessage(KeymapPanel.class, "CTL_Duplicate")); // NOI18N

        searchField.setText(org.openide.util.NbBundle.getMessage(KeymapPanel.class, "KeymapPanel.searchField.text")); // NOI18N

        searchLabel.setLabelFor(searchField);
        org.openide.awt.Mnemonics.setLocalizedText(searchLabel, org.openide.util.NbBundle.getMessage(KeymapPanel.class, "KeymapPanel.searchLabel.text")); // NOI18N

        searchSCLabel.setLabelFor(searchSCField);
        org.openide.awt.Mnemonics.setLocalizedText(searchSCLabel, org.openide.util.NbBundle.getMessage(KeymapPanel.class, "KeymapPanel.searchSCLabel.text")); // NOI18N

        searchSCField.setText(org.openide.util.NbBundle.getMessage(KeymapPanel.class, "KeymapPanel.searchSCField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(moreButton, org.openide.util.NbBundle.getMessage(KeymapPanel.class, "KeymapPanel.moreButton.text")); // NOI18N
        moreButton.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        moreButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moreButtonActionPerformed(evt);
            }
        });

        actionsView.setLayout(new java.awt.CardLayout());

        actionProgress.setIndeterminate(true);
        actionProgress.setString(org.openide.util.NbBundle.getMessage(KeymapPanel.class, "KeymapPanel.actionProgress.string")); // NOI18N

        waitLabel.setFont(new java.awt.Font("Dialog", 2, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(waitLabel, org.openide.util.NbBundle.getMessage(KeymapPanel.class, "KeymapPanel.waitLabel.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(waitLabel)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(222, Short.MAX_VALUE)
                .addComponent(actionProgress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(221, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 65, Short.MAX_VALUE)
                .addComponent(waitLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(actionProgress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 98, Short.MAX_VALUE))
        );

        actionsView.add(jPanel1, "wait");

        jScrollPane1.setPreferredSize(new java.awt.Dimension(453, 100));

        actionsTable.setModel(sorter);
        jScrollPane1.setViewportView(actionsTable);

        actionsView.add(jScrollPane1, "actions");

        org.openide.awt.Mnemonics.setLocalizedText(btnPrintAsHTML, org.openide.util.NbBundle.getMessage(KeymapPanel.class, "KeymapPanel.btnPrintAsHTML.text")); // NOI18N
        btnPrintAsHTML.setToolTipText(org.openide.util.NbBundle.getMessage(KeymapPanel.class, "KeymapPanel.btnPrintAsHTML.toolTipText")); // NOI18N
        btnPrintAsHTML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintAsHTMLActionPerformed(evt);
            }
        });

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(searchLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchSCLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchSCField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(moreButton))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(lProfile)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbProfile, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnPrintAsHTML)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(manageButton))
                            .addComponent(actionsView, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(6, 6, 6))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {searchField, searchSCField});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(manageButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lProfile)
                        .addComponent(cbProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnPrintAsHTML)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(moreButton)
                    .addComponent(searchSCField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchSCLabel)
                    .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(actionsView, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                .addContainerGap())
        );

        searchField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(KeymapPanel.class, "KeymapPanel.searchField.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void hidePopup() {
        if (searchPopup != null) {
            searchPopup.hide();
            searchPopup = null;
        }
    }
    /**
     * Shows popup with ESC and TAB keys
     */
    private void moreButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moreButtonActionPerformed
        if (searchPopup != null) {
            return;
        }
        JComponent tf = (JComponent) evt.getSource();
        Point p = new Point(tf.getX(), tf.getY());
        SwingUtilities.convertPointToScreen(p, this);
        Rectangle usableScreenBounds = Utilities.getUsableScreenBounds();
        if (p.x + specialkeyList.getWidth() > usableScreenBounds.width) {
            p.x = usableScreenBounds.width - specialkeyList.getWidth();
        }
        if (p.y + specialkeyList.getHeight() > usableScreenBounds.height) {
            p.y = usableScreenBounds.height - specialkeyList.getHeight();
        }
        //show special key popup
        searchPopup = PopupFactory.getSharedInstance().getPopup(this, specialkeyList, p.x, p.y);
        searchPopup.show();
}//GEN-LAST:event_moreButtonActionPerformed

    private void btnPrintAsHTMLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintAsHTMLActionPerformed
	if (getMutableModel().getCurrentProfile()!=null){
	    ExportShortcutsAction.exportShortcutsOfProfileToHTML(getMutableModel().getCurrentProfile());
	}        
    }//GEN-LAST:event_btnPrintAsHTMLActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar actionProgress;
    private javax.swing.JTable actionsTable;
    private javax.swing.JPanel actionsView;
    private javax.swing.JButton btnPrintAsHTML;
    private javax.swing.JComboBox cbProfile;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lProfile;
    private javax.swing.JButton manageButton;
    private javax.swing.JButton moreButton;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JTextField searchSCField;
    private javax.swing.JLabel searchSCLabel;
    private javax.swing.JLabel waitLabel;
    // End of variables declaration//GEN-END:variables


    @Override
    public Popup getPopup() {
        return searchPopup;
    }

    class ButtonCellMouseListener implements MouseListener {

        private JTable table;

        public ButtonCellMouseListener(JTable table) {
            this.table = table;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            forwardEvent(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        private void forwardEvent(MouseEvent e) {
            Point p = new Point(e.getX(), e.getY());
            int row = table.rowAtPoint(p);
            int col = table.columnAtPoint(p);
            
            if (showPopupMenu(row, col, e.getX(), e.getY())) {
                e.consume();
            }
        }
    }
    
    private boolean showPopupMenu(int row, int col, int x, int y) {
        JTable table = actionsTable;
        
        if (col != 1) {
            return false;
        }
        
        Object valueAt = table.getValueAt(row, col);
        ShortcutCellPanel scCell = (ShortcutCellPanel) table.getCellRenderer(row, col).getTableCellRendererComponent(table, valueAt, true, true, row, col);
        Rectangle cellRect = table.getCellRect(row, col, false);
        JButton button = scCell.getButton();
        if (x < 0  || x > (cellRect.x + cellRect.width - button.getWidth())) { //inside changeButton
            boolean isShortcutSet = scCell.getTextField().getText().length() != 0;
            final ShortcutPopupPanel panel = (ShortcutPopupPanel) popup.getComponents()[0];
            panel.setDisplayAddAlternative(isShortcutSet);
            panel.setRow(row);

            if (x == -1 || y == -1) {
                x = button.getX() + 1;
                y = button.getY() + 1;
            }
            panel.setCustomProfile(keymapModel.getMutableModel().isCustomProfile(keymapModel.getMutableModel().getCurrentProfile()));
            popup.show(table, x, y);
            SwingUtilities.invokeLater(panel::requestFocus);
            popup.requestFocus();
            return true;
        }
        return false;
    }

    static String loc (String key) {
        return NbBundle.getMessage (KeymapPanel.class, key);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (ignoreActionEvents) {
            return;
        }
        Object source = e.getSource();

        if (source == cbProfile) {
            String profile = (String) cbProfile.getSelectedItem();
            if (profile != null)
                getMutableModel().setCurrentProfile(profile);
            getModel().update();
        } else if (source == manageButton) {
            //remember previous profile state, in case user will cancel dialog
            Map<String, Map<ShortcutAction, Set<String>>> modifiedProfiles = getMutableModel().getModifiedProfiles();
            Set<String> deletedProfiles = getMutableModel().getDeletedProfiles();

            //show manage profiles dialog
            final ProfilesPanel profilesPanel = new ProfilesPanel(this);
            DialogDescriptor dd = new DialogDescriptor(
                    profilesPanel, 
                    NbBundle.getMessage(KeymapPanel.class, "CTL_Manage_Keymap_Profiles"),
                    true, new Object[] { DialogDescriptor.CLOSED_OPTION }, DialogDescriptor.CLOSED_OPTION, 
                    DialogDescriptor.BOTTOM_ALIGN,
                    new HelpCtx("org.netbeans.modules.options.keymap.ProfilesPanel"), 
                    null
            );
            DialogDisplayer.getDefault().notify(dd);

            final String selectedProfile = profilesPanel.getSelectedProfile();
            getMutableModel().setCurrentProfile(selectedProfile);
            refreshProfileCombo();
            keymapModel.update();
        }
        return;
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == getMutableModel()) {
            firePropertyChange(OptionsPanelController.PROP_CHANGED, Boolean.FALSE, Boolean.TRUE);
        }
    }
}
