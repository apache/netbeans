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
package org.netbeans.modules.cnd.makeproject.ui.configurations;

import java.awt.Component;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.cnd.makeproject.api.configurations.PackagingConfiguration;
import org.netbeans.modules.cnd.utils.ui.ListEditorPanel;
import org.netbeans.modules.cnd.makeproject.api.PackagerInfoElement;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

public class PackagingInfoPanel extends ListEditorPanel<PackagerInfoElement> {

    private final PackagingConfiguration packagingConfiguration;
    private JTable targetList;
    private final MyTableCellRenderer myTableCellRenderer = new MyTableCellRenderer();
    private final JButton addButton;
    private final JButton addEntryButton;
    private JTextArea docArea;

    public PackagingInfoPanel(List<PackagerInfoElement> infoList, PackagingConfiguration packagingConfiguration) {
        super(infoList, new JButton[]{new JButton(), new JButton()});
        getAddButton().setVisible(false);
        this.packagingConfiguration = packagingConfiguration;

        this.addButton = extraButtons[0];
        addButton.setText(getString("PackagingFilesPanel.addButton.text"));
        addButton.setMnemonic(getString("PackagingFilesPanel.addButton.mn").charAt(0));
        addButton.getAccessibleContext().setAccessibleDescription(getString("PackagingFilesPanel.addButton.ad"));
        addButton.addActionListener(new AddButtonAction());

        this.addEntryButton = extraButtons[1];
        addEntryButton.setText(getString("PackagingFilesPanel.addParameterButton.text"));
        addEntryButton.setMnemonic(getString("PackagingFilesPanel.addParameterButton.mn").charAt(0));
        addEntryButton.getAccessibleContext().setAccessibleDescription(getString("PackagingFilesPanel.addParameterButton.ad"));
        addEntryButton.addActionListener(new AddEntryButtonAction());

        getEditButton().setVisible(false);
        getDefaultButton().setVisible(false);

        if (infoList.size() > 0 && infoList.get(0).isMandatory()) {
            getRemoveButton().setEnabled(false);
        }
    }

    public void setDocArea(JTextArea docArea) {
        this.docArea = docArea;
    }

    private class AddButtonAction implements java.awt.event.ActionListener {

        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            addObjectAction(new PackagerInfoElement(packagingConfiguration.getType().getValue(), "", "")); // NOI18N
        }
    }

    private class AddEntryButtonAction implements java.awt.event.ActionListener {

        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            PackagingNewEntryPanel packagingNewEntryPanel = new PackagingNewEntryPanel(packagingConfiguration);
            DialogDescriptor dialogDescriptor = new DialogDescriptor(packagingNewEntryPanel, getString("AddNewParameterDialogTitle"));
            DialogDisplayer.getDefault().notify(dialogDescriptor);
            if (dialogDescriptor.getValue() != DialogDescriptor.OK_OPTION) {
                return;
            }
            addObjectAction(packagingNewEntryPanel.getInfoElement());
        }
    }

    @Override
    public PackagerInfoElement copyAction(PackagerInfoElement o) {
        PackagerInfoElement elem = o;
        return new PackagerInfoElement(elem.getPackager(), new String(elem.getName()), new String(elem.getValue()));
    }

    @Override
    public String getCopyButtonText() {
        return getString("PackagingFilesPanel.duplicateButton.text");
    }

    @Override
    public char getCopyButtonMnemonics() {
        return getString("PackagingFilesPanel.duplicateButton.mn").charAt(0);
    }

    @Override
    public String getCopyButtonAD() {
        return getString("PackagingFilesPanel.duplicateButton.ad");
    }

    @Override
    public String getListLabelText() {
        return getString("PackagingInfoPanel.listlabel.text");
    }

    @Override
    public char getListLabelMnemonic() {
        return getString("PackagingInfoPanel.listlabel.mn").toCharArray()[0];
    }

    // Overrides ListEditorPanel
    @Override
    public int getSelectedIndex() {
        int index = getTargetList().getSelectedRow();
        if (index >= 0 && index < getListDataSize()) {
            return index;
        } else {
            return 0;
        }
    }

    @Override
    protected void setSelectedIndex(int i) {
        getTargetList().getSelectionModel().setSelectionInterval(i, i);
    }

    @Override
    protected void setData(List<PackagerInfoElement> data) {
        getTargetList().setModel(new MyTableModel());
        // Set column sizes
        getTargetList().getColumnModel().getColumn(0).setPreferredWidth(100);
        getTargetList().getColumnModel().getColumn(0).setMaxWidth(400);
        //
        getTargetList().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        getTargetList().getSelectionModel().addListSelectionListener(new TargetSelectionListener());
        // Left align table header
        ((DefaultTableCellRenderer) getTargetList().getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
    }

    private class TargetSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }
            checkSelection();
            // Disable Remove button for mandatory entries
            int i = getSelectedIndex();
            if (getListDataSize() >= 0 && i >= 0 && i < getListDataSize()) {
                PackagerInfoElement infoElement = getElementAt(i);
                if (infoElement.isMandatory()) {
                    getRemoveButton().setEnabled(false);
                }
            }
        }
    }

    @Override
    protected void ensureIndexIsVisible(int selectedIndex) {
        // FIXUP...
        //targetList.ensureIndexIsVisible(selectedIndex);
        //java.awt.Rectangle rect = targetList.getCellRect(selectedIndex, 0, true);
        //targetList.scrollRectToVisible(rect);
    }

    @Override
    protected Component getViewComponent() {
        return getTargetList();
    }

    private JTable getTargetList() {
        if (targetList == null) {
            targetList = new MyTable();
            getListLabel().setLabelFor(targetList);
            setData(null);
        }
        return targetList;
    }

    private class MyTable extends JTable {

        public MyTable() {
//	    //setTableHeader(null); // Hides table headers
//	    if (getRowHeight() < 19)
//		setRowHeight(19);
            getAccessibleContext().setAccessibleDescription(""); // NOI18N
            getAccessibleContext().setAccessibleName(""); // NOI18N

            getSelectionModel().addListSelectionListener(new MyListSelectionListener());

            putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // NOI18N
        }

        @Override
        public boolean getShowHorizontalLines() {
            return false;
        }

        @Override
        public boolean getShowVerticalLines() {
            return false;
        }

        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            return myTableCellRenderer;
        }        //        @Override
//	public TableCellEditor getCellEditor(int row, int col) {
//	    //TableColumn col = getTargetList().getColumnModel().getColumn(1);
//	    if (col == 0) {
//		return super.getCellEditor(row, col);
//	    }
//	    else if (col == 1) {
//		LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem)listData.elementAt(row);
//		Project project = projectItem.getProject(baseDir);
//		if (project == null) {
//		    return super.getCellEditor(row, col);
//		}
//		else {
//		    MakeArtifact[] artifacts = MakeArtifact.getMakeArtifacts(project);
//		    JComboBox comboBox = new JComboBox();
//		    for (int i = 0; i < artifacts.length; i++)
//			comboBox.addItem(new MakeArtifactWrapper(artifacts[i]));
//		    return new DefaultCellEditor(comboBox);
//		}
//	    }
//	    else {
//		// col 2
//		LibraryItem libraryItem = (LibraryItem)listData.elementAt(row);
//		if (libraryItem instanceof LibraryItem.ProjectItem) {
//		    LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem)listData.elementAt(row);
//		    JCheckBox checkBox = new JCheckBox();
//		    checkBox.setSelected(((LibraryItem.ProjectItem)libraryItem).getMakeArtifact().getBuild());
//		    return new DefaultCellEditor(checkBox);
//		}
//		else {
//		    return super.getCellEditor(row, col);
//		}
//	    }
//	}
    }

    public void refresh() {
        updateDoc();
    }

    private class MyListSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent arg0) {
            updateDoc();
        }
    }

    public void updateDoc() {
        if (docArea == null) {
            return;
        }
        docArea.setText("");

        int i = targetList.getSelectedRow();
        if (getListDataSize() == 0 || i < 0 || i >= getListDataSize()) {
            return;
        }

        PackagerInfoElement elem = getElementAt(i);
        if (elem.getName().equals("ARCH")) { //NOI18N
            docArea.setText(getString("PACKAGING_ARCH_DOC"));
        } else if (elem.getName().equals("CATEGORY")) { //NOI18N
            docArea.setText(getString("PACKAGING_CATEGORY_DOC"));
        } else if (elem.getName().equals("NAME")) { //NOI18N
            docArea.setText(getString("PACKAGING_NAME_DOC"));
        } else if (elem.getName().equals("PKG")) { //NOI18N
            docArea.setText(getString("PACKAGING_PKG_DOC"));
        } else if (elem.getName().equals("VERSION")) { // NOI18N
            docArea.setText(getString("PACKAGING_VERSION_DOC"));
        } else if (elem.getName().equals("BASEDIR")) { // NOI18N
            docArea.setText(getString("PACKAGING_BASEDIR_DOC"));
        } else if (elem.getName().equals("CLASSES")) { // NOI18N
            docArea.setText(getString("PACKAGING_CLASSES_DOC"));
        } else if (elem.getName().equals("DESC")) { // NOI18N
            docArea.setText(getString("PACKAGING_DESC_DOC"));
        } else if (elem.getName().equals("EMAIL")) { // NOI18N
            docArea.setText(getString("PACKAGING_EMAIL_DOC"));
        } else if (elem.getName().equals("HOTLINE")) { // NOI18N
            docArea.setText(getString("PACKAGING_HOTLINE_DOC"));
        } else if (elem.getName().equals("INTONLY")) { //NOI18N
            docArea.setText(getString("PACKAGING_INTONLY_DOC"));
        } else if (elem.getName().equals("ISTATES")) { //NOI18N
            docArea.setText(getString("PACKAGING_ISTATES_DOC"));
        } else if (elem.getName().equals("MAXINST")) { //NOI18N
            docArea.setText(getString("PACKAGING_MAXINST_DOC"));
        } else if (elem.getName().equals("ORDER")) { //NOI18N
            docArea.setText(getString("PACKAGING_ORDER_DOC"));
        } else if (elem.getName().equals("PSTAMP")) { //NOI18N
            docArea.setText(getString("PACKAGING_PSTAMP_DOC"));
        } else if (elem.getName().equals("RSTATES")) { //NOI18N
            docArea.setText(getString("PACKAGING_RSTATES_DOC"));
        } else if (elem.getName().equals("SUNW_ISA")) { //NOI18N
            docArea.setText(getString("PACKAGING_SUNW_ISA_DOC"));
        } else if (elem.getName().equals("SUNW_LOC")) { //NOI18N
            docArea.setText(getString("PACKAGING_SUNW_LOC_DOC"));
        } else if (elem.getName().equals("SUNW_PKG_DIR")) { //NOI18N
            docArea.setText(getString("PACKAGING_SUNW_PKG_DIR_DOC"));
        } else if (elem.getName().equals("SUNW_PKG_ALLZONES")) { //NOI18N
            docArea.setText(getString("PACKAGING_SUNW_PKG_ALLZONES_DOC"));
        } else if (elem.getName().equals("SUNW_PKG_HOLLOW")) { //NOI18N
            docArea.setText(getString("PACKAGING_SUNW_PKG_HOLLOW_DOC"));
        } else if (elem.getName().equals("SUNW_PKG_THISZONE")) { //NOI18N
            docArea.setText(getString("PACKAGING_SUNW_PKG_THISZONE_DOC"));
        } else if (elem.getName().equals("SUNW_PKGLIST")) { //NOI18N
            docArea.setText(getString("PACKAGING_SUNW_PKGLIST_DOC"));
        } else if (elem.getName().equals("SUNW_PKGTYPE")) { //NOI18N
            docArea.setText(getString("PACKAGING_SUNW_PKGTYPE_DOC"));
        } else if (elem.getName().equals("SUNW_PKGVERS")) { //NOI18N
            docArea.setText(getString("PACKAGING_SUNW_PKGVERS_DOC"));
        } else if (elem.getName().equals("SUNW_PRODNAME")) { //NOI18N
            docArea.setText(getString("PACKAGING_SUNW_PRODNAME_DOC"));
        } else if (elem.getName().equals("SUNW_PRODVERS")) { //NOI18N
            docArea.setText(getString("PACKAGING_SUNW_PRODVERS_DOC"));
        } else if (elem.getName().equals("ULIMIT")) { //NOI18N
            docArea.setText(getString("PACKAGING_ULIMIT_DOC"));
        } else if (elem.getName().equals("VENDOR")) { //NOI18N
            docArea.setText(getString("PACKAGING_VENDOR_DOC"));
        } else if (elem.getName().equals("VSTOCK")) { //NOI18N
            docArea.setText(getString("PACKAGING_VSTOCK_DOC"));
        }
    }

    private class MyTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, color, isSelected, hasFocus, row, col);
            PackagerInfoElement elem = getElementAt(row);
            if (col == 0) {
            } else if (col == 1) {
                String val = elem.getValue();
                if (val.contains("${")) { // NOI18N
                    String expandedVal = packagingConfiguration.expandMacros(val);
                    label.setText(expandedVal); // NOI18N
                }
            }
            return label;
        }
    }

    private class MyTableModel extends DefaultTableModel {

        private final String[] columnNames = {getString("PackagingInfoPanel.column.0.text"), getString("PackagingInfoPanel.column.1.text")};

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return getListDataSize();
        }

        @Override
        public Object getValueAt(int row, int col) {
//            return listData.elementAt(row);
            PackagerInfoElement elem = getElementAt(row);
            if (col == 0) {
                return elem.getName();
            }
            if (col == 1) {
                return elem.getValue();
            }
            assert false;
            return null;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            PackagerInfoElement elem = getElementAt(row);
            if (col == 0) {
                elem.setName((String) value);
            } else if (col == 1) {
                elem.setValue((String) value);
            } else {
                assert false;
            }
            elem.setDefaultValue(false);
        }
    }

    private static String getString(String s) {
        return NbBundle.getBundle(PackagingInfoPanel.class).getString(s);
    }
}
