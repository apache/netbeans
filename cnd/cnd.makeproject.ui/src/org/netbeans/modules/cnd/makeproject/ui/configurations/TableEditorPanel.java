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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.ui.ListEditorPanel;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

public class TableEditorPanel extends ListEditorPanel<LibraryItem> {

    private static final Image brokenProjectBadge = ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/brokenProjectBadge.gif"); // NOI18N
    private final FSPath baseDir;
    private final MakeConfiguration conf;
    private JTable targetList;
    private final MyTableCellRenderer myTableCellRenderer = new MyTableCellRenderer();

    @Override
    public char getDownButtonMnemonics() {
        return getString("DOWN_OPTION_BUTTON_MN").charAt(0);
    }

    /*
    public TableEditorPanel(Object[] objects) {
    this(objects, null, null);
    }
     */
    public TableEditorPanel(MakeConfiguration conf, List<LibraryItem> objects, JButton[] extraButtons, FSPath baseDir) {
        super(objects, extraButtons);
        this.conf = conf;
        this.baseDir = baseDir;
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

    private int calculateColumnWidth(String txt) {
        Font font = getTargetList().getFont();
        FontMetrics fontMetrics = getTargetList().getFontMetrics(font);
        int width = fontMetrics.stringWidth(txt);
        return width;
    }

    @Override
    protected void setData(List<LibraryItem> data) {
        getTargetList().setModel(new MyTableModel());
        // Set column sizes
        getTargetList().getColumnModel().getColumn(0).setPreferredWidth(1000);
        int w1 = calculateColumnWidth(getString("CONFIGURATION")) + 10;
        getTargetList().getColumnModel().getColumn(1).setPreferredWidth(w1);
        getTargetList().getColumnModel().getColumn(1).setMinWidth(w1);
        int w2 = calculateColumnWidth(getString("BUILD")) + 10;
        getTargetList().getColumnModel().getColumn(2).setPreferredWidth(w2);
        getTargetList().getColumnModel().getColumn(2).setMinWidth(w2);
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
            setData(null);
        }
        return targetList;
    }

    private class MyTable extends JTable {

        public MyTable() {
            //setTableHeader(null); // Hides table headers
//            if (getRowHeight() < 19) {
//                setRowHeight(19);
//            }
            getAccessibleContext().setAccessibleDescription(""); // NOI18N
            getAccessibleContext().setAccessibleName(""); // NOI18N
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
        }

        @Override
        public TableCellEditor getCellEditor(int row, int col) {
            //TableColumn col = getTargetList().getColumnModel().getColumn(1);
            if (col == 0) {
                return super.getCellEditor(row, col);
            } else if (col == 1) {
                LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem) getElementAt(row);
                Project project = projectItem.getProject(baseDir);
                if (project == null) {
                    return super.getCellEditor(row, col);
                } else {
                    MakeArtifact[] artifacts = MakeArtifact.getMakeArtifacts(project);
                    JComboBox comboBox = new JComboBox();
                    for (int i = 0; i < artifacts.length; i++) {
                        comboBox.addItem(new MakeArtifactWrapper(artifacts[i]));
                    }
                    return new DefaultCellEditor(comboBox);
                }
            } else {
                // col 2
                LibraryItem libraryItem = getElementAt(row);
                if (libraryItem instanceof LibraryItem.ProjectItem) {
                    LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem) getElementAt(row);
                    JCheckBox checkBox = new JCheckBox();
                    checkBox.setSelected(((LibraryItem.ProjectItem) libraryItem).getMakeArtifact().getBuild());
                    return new DefaultCellEditor(checkBox);
                } else {
                    return super.getCellEditor(row, col);
                }
            }
        }
    }

    private static class MakeArtifactWrapper {

        private final MakeArtifact makeArtifact;

        public MakeArtifactWrapper(MakeArtifact makeArtifact) {
            this.makeArtifact = makeArtifact;
        }

        public MakeArtifact getMakeArtifact() {
            return makeArtifact;
        }

        @Override
        public String toString() {
            return getMakeArtifact().getConfigurationName();
        }
    }

    private class MyTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, color, isSelected, hasFocus, row, col);
            Object element = getElementAt(row);
            if (!(element instanceof LibraryItem)) {
                // FIXUP ERROR!
                label.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/resources/blank.gif", false)); // NOI18N
                label.setToolTipText("unknown"); // NOI18N
                return label;
            }
            LibraryItem libraryItem = (LibraryItem) element;
            if (col == 0) {
                Image iconImage = ImageUtilities.loadImage(StdLibPanel.getLibraryIconResource(libraryItem));
                label.setToolTipText(libraryItem.getDescription());
                if (libraryItem instanceof LibraryItem.ProjectItem && ((LibraryItem.ProjectItem) libraryItem).getProject(baseDir) == null) {
                    iconImage = ImageUtilities.mergeImages(iconImage, brokenProjectBadge, 8, 0);
                    label.setToolTipText(getString("BROKEN") + label.getToolTipText());
                }
                label.setIcon(new ImageIcon(iconImage));
            } else if (col == 1) {
                label.setText(""); // NOI18N
                label.setIcon(null);
                label.setToolTipText(null);
                if (libraryItem instanceof LibraryItem.ProjectItem) {
                    label.setText(((LibraryItem.ProjectItem) libraryItem).getMakeArtifact().getConfigurationName());
                    label.setToolTipText(getString("CLICK_TO_CHANGE"));
                    if (((LibraryItem.ProjectItem) libraryItem).getProject(baseDir) == null) {
                        label.setToolTipText(""); // NOI18N
                    }
                }
            } else {
                // col 2
                if (libraryItem instanceof LibraryItem.ProjectItem) {
                    JCheckBox checkBox = new JCheckBox();
                    checkBox.setSelected(((LibraryItem.ProjectItem) libraryItem).getMakeArtifact().getBuild());
                    checkBox.setBackground(label.getBackground());
                    if (conf.isMakefileConfiguration()) {
                        checkBox.setEnabled(false);
                        checkBox.setSelected(false);
                    }
                    return checkBox;
                } else {
                    label.setText(""); // NOI18N
                    label.setIcon(null);
                    label.setToolTipText(null);
                }
            }
            return label;
        }
    }

    private class MyTableModel extends DefaultTableModel {

        private final String[] columnNames = {getString("ITEM"), getString("CONFIGURATION"), getString("BUILD")}; // NOI18N

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public int getRowCount() {
            return getListDataSize();
        }

        @Override
        public Object getValueAt(int row, int col) {
            return getElementAt(row);
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            Object element = getElementAt(row);
            LibraryItem libraryItem = (LibraryItem) element;
            if (col == 0) {
                return libraryItem.canEdit();
            } else if (col == 1) {
                if (libraryItem instanceof LibraryItem.ProjectItem) {
                    Project libProject = ((LibraryItem.ProjectItem) libraryItem).getProject(baseDir);
                    if (libProject == null) {
                        return false;
                    }
                    ConfigurationDescriptorProvider configurationDescriptorProvider = libProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
                    if (configurationDescriptorProvider == null || !configurationDescriptorProvider.gotDescriptor()) {
                        //return false if there is no description yet, means we are not ready
                        return false;
                    }
                    MakeConfigurationDescriptor makeConfigurationDescriptor = configurationDescriptorProvider.getConfigurationDescriptor();
                    if (makeConfigurationDescriptor.getState() == ConfigurationDescriptor.State.BROKEN) { // See IZ 193075
                        return false;
                    }
                    return true;
                } else {
                    return false;
                }
            } else {
                // col 2
                if (!conf.isMakefileConfiguration() && libraryItem instanceof LibraryItem.ProjectItem) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (value == null) {
                return; // See IZ 193075
            }
            LibraryItem libraryItem = getElementAt(row);
            if (col == 0) {
                libraryItem.setValue((String) value);
                fireTableCellUpdated(row, col);
            } else if (col == 1) {
                // FIXUP: should do a deep clone of the list
                MakeArtifact oldMakeArtifact = ((LibraryItem.ProjectItem) libraryItem).getMakeArtifact();
                boolean abs = CndPathUtilities.isPathAbsolute(oldMakeArtifact.getProjectLocation());
                MakeArtifact makeArtifact = ((MakeArtifactWrapper) value).getMakeArtifact();
                String projectLocation = makeArtifact.getProjectLocation();
                String workingDirectory = makeArtifact.getWorkingDirectory();
                if (!abs) {
                    // retain abs/rel paths...
                    projectLocation = CndPathUtilities.toRelativePath(baseDir.getFileObject(), projectLocation);
                    workingDirectory = CndPathUtilities.toRelativePath(baseDir.getFileObject(), workingDirectory);
                }
                makeArtifact.setProjectLocation(CndPathUtilities.normalizeSlashes(projectLocation));
                makeArtifact.setWorkingDirectory(CndPathUtilities.normalizeSlashes(workingDirectory));
                replaceElement(libraryItem, new LibraryItem.ProjectItem(makeArtifact), row);
                // FIXUP
                fireTableCellUpdated(row, 0);
                fireTableCellUpdated(row, 1);
                fireTableCellUpdated(row, 2);
            } else {
                // FIXUP: should do a deep clone of the list
                // col 2
                if (libraryItem instanceof LibraryItem.ProjectItem) {
                    MakeArtifact newMakeArtifact = ((LibraryItem.ProjectItem) libraryItem).getMakeArtifact().clone();
                    newMakeArtifact.setBuild(!newMakeArtifact.getBuild());
                    replaceElement(libraryItem, new LibraryItem.ProjectItem(newMakeArtifact), row);
                }
                fireTableCellUpdated(row, 0);
                fireTableCellUpdated(row, 1);
                fireTableCellUpdated(row, 2);
            }
        }
    }

    private static String getString(String s) {
        return NbBundle.getBundle(TableEditorPanel.class).getString(s);
    }
}
