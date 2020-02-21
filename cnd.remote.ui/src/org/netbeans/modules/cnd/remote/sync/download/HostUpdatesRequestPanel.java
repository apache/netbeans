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

package org.netbeans.modules.cnd.remote.sync.download;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 */
public class HostUpdatesRequestPanel extends JPanel {

    private final List<RowData> model;

    private final JPanel bottomPanel;
    private final JCheckBox cbRememberChoice;
    private final JTable fileTable;
    private final HostUpdatesPersistence persistence;

    /*package*/ static Set<FileDownloadInfo> request(Collection<FileDownloadInfo> infos, ExecutionEnvironment env, HostUpdatesPersistence persistence) {
        HostUpdatesRequestPanel panel = new HostUpdatesRequestPanel(infos, env, persistence);
        String envString = RemoteUtil.getDisplayName(env);
        String caption = NbBundle.getMessage(HostUpdatesRequestPanel.class, "HostUpdatesRequestPanel.TITLE", envString);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        DialogDescriptor dd = new DialogDescriptor(panel, caption, true,
                new Object[]{DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION},
                DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.pack();
        try {
            dlg.setVisible(true);
        } catch (Throwable th) {
            if (!(th.getCause() instanceof InterruptedException)) {
                throw new RuntimeException(th);
            }
            dd.setValue(DialogDescriptor.CANCEL_OPTION);
        } finally {
            dlg.dispose();
        }
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            panel.apply();
            return panel.getConfirmed();
        } else {
            return null;
        }
    }

    HostUpdatesRequestPanel(Collection<FileDownloadInfo> fileInfos, ExecutionEnvironment env, HostUpdatesPersistence persistence) {
        super(new BorderLayout());
        this.persistence = persistence;
        this.model = new ArrayList<>(fileInfos.size());
        for (FileDownloadInfo info : fileInfos) {
            boolean selected = persistence.getFileSelected(info.getLocalFile(), true);
            model.add(new RowData(info, selected));
        }
        Collections.sort(model);

        fileTable = new JTable(new FileTableModel());
        fileTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        fileTable.getColumnModel().getColumn(0).setPreferredWidth(24);
        //int height = 32;
        for (int c = 1; c < fileTable.getColumnCount(); c++) {
            //height = packColumnAndCalcHeight(fileTable, c, 2);
            setColumnWidth(fileTable, c, 2);
        }
        Dimension pref = fileTable.getPreferredSize();
        pref.width = Math.min(pref.width, fileTable.getPreferredScrollableViewportSize().width);
        pref.height = Math.max(Math.min(pref.height + 3, 480), 48);
        fileTable.setPreferredScrollableViewportSize(pref);
        JScrollPane scroller = new JScrollPane(fileTable);
        add(scroller, BorderLayout.CENTER);

        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        cbRememberChoice = new JCheckBox(NbBundle.getMessage(getClass(), "HostUpdatesRequestPanel.remember.text"), false);
        bottomPanel.add(cbRememberChoice);
        this.add(bottomPanel, BorderLayout.SOUTH);
        setPopup();
    }

    private void setPopup() {

        Action checkSelected = new AbstractAction(NbBundle.getMessage(getClass(), "ACTION_CheckSelected")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] rows = fileTable.getSelectedRows();
                if (rows.length > 0) {
                    for (int i = 0; i < rows.length; i++) {
                        model.get(rows[i]).selected = true;
                    }
                }
                fileTable.repaint();
            }
        };

        Action uncheckSelected = new AbstractAction(NbBundle.getMessage(getClass(), "ACTION_UncheckSelected")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] rows = fileTable.getSelectedRows();
                if (rows.length > 0) {
                    for (int i = 0; i < rows.length; i++) {
                        model.get(rows[i]).selected = false;
                    }
                }
                fileTable.repaint();
            }            
        };

        final JPopupMenu menu = new JPopupMenu();
        menu.add(new JMenuItem(checkSelected));
        menu.add(new JMenuItem(uncheckSelected));

        final MenuListener menuListener = new MenuListener(fileTable, menu);
        fileTable.addMouseListener(menuListener);
    }

    private static class MenuListener extends MouseAdapter {

        private final JTable table;
        private final JPopupMenu menu;

        public MenuListener(JTable tblPathMappings, JPopupMenu menu) {
            this.table = tblPathMappings;
            this.menu = menu;
        }

        private void showMenu(MouseEvent evt) {
            if (evt != null) {
                int row = table.rowAtPoint(evt.getPoint());
                if (row >= 0 && table.getSelectionModel().isSelectionEmpty()) {
                    table.getSelectionModel().setSelectionInterval(row, row);
                }
                menu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            if (evt.isPopupTrigger()) {
                showMenu(evt);
            }
        }
        @Override
        public void mouseReleased(MouseEvent evt) {
            if (evt.isPopupTrigger()) {
                showMenu(evt);
            }
        }
    }

    private void apply() {
        for (RowData data : model) {
            persistence.setFileSelected(data.fileInfo.getLocalFile(), data.selected, cbRememberChoice.isSelected());
        }
        persistence.store();
    }

    private static void setColumnWidth(JTable table, int vColIndex, int margin) {
        //int height = 0;
        DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
        TableColumn col = colModel.getColumn(vColIndex);
        int width = 0;
        TableCellRenderer renderer = col.getHeaderRenderer();
        if (renderer == null) {
            renderer = table.getTableHeader().getDefaultRenderer();
        }
        Component comp = renderer.getTableCellRendererComponent(
                table, col.getHeaderValue(), false, false, 0, 0);
        Dimension pref = comp.getPreferredSize();
        width = pref.width;
        //height += pref.height;
        for (int r = 0; r < table.getRowCount(); r++) {
            renderer = table.getCellRenderer(r, vColIndex);
            comp = renderer.getTableCellRendererComponent(
                    table, table.getValueAt(r, vColIndex), false, false, r, vColIndex);
            pref = comp.getPreferredSize();
            //height += pref.height;
            width = Math.max(width, pref.width);
        }
        width += 2 * margin;
        col.setPreferredWidth(width);
        //height += 2 * margin;
        //return height;
    }

    private Set<FileDownloadInfo> getConfirmed() {
        Set<FileDownloadInfo> result = new HashSet<>();
        for (RowData data : model) {
            if (data.selected) {
                result.add(data.fileInfo);
            }
        }
        return result;
    }


    private static class RowData implements Comparable<RowData> {
        public final FileDownloadInfo fileInfo;
        public boolean selected;
        public RowData(FileDownloadInfo fileInfo, boolean selected) {
            this.fileInfo = fileInfo;
            this.selected = selected;
        }

        @Override
        public int compareTo(RowData o) {
            if (o == null) {
                return -1;
            } else {
                return fileInfo.getLocalFile().getAbsolutePath().compareTo(o.fileInfo.getLocalFile().getAbsolutePath());
            }
        }
    }

    private class FileTableModel extends AbstractTableModel {

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public int getRowCount() {
            return model.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            RowData data = (rowIndex < model.size()) ? model.get(rowIndex) : null;
            switch (columnIndex) {
                case 0: return (data == null) ? false : data.selected;
                case 1: return data.fileInfo.getLocalFile().getName();
                case 2: return data.fileInfo.getLocalFile().getParent();
                default: throw new IllegalArgumentException("Illegal column index: " + columnIndex); //NOI18N
            }
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            RowData data = (rowIndex < model.size()) ? model.get(rowIndex) : null;
            if (columnIndex != 0) {
                throw new IllegalArgumentException("Illegal column index: " + columnIndex); //NOI18N
            }
            if (value instanceof Boolean) {
                if (data != null) {
                    data.selected = ((Boolean) value).booleanValue();
                }
            } else {
                throw new IllegalArgumentException("Illegal value to set: " + value); //NOI18N
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0: return Boolean.class;
                case 1: return String.class;
                case 2: return String.class;
                default: throw new IllegalArgumentException("Illegal column index: " + columnIndex); //NOI18N
            }
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0: return NbBundle.getMessage(HostUpdatesRequestPanel.class, "HostUpdatesRequestPanel.column_check");
                case 1: return NbBundle.getMessage(HostUpdatesRequestPanel.class, "HostUpdatesRequestPanel.column_name");
                case 2: return NbBundle.getMessage(HostUpdatesRequestPanel.class, "HostUpdatesRequestPanel.column_dir");
                default: throw new IllegalArgumentException("Illegal column index: " + columnIndex); //NOI18N
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }
    }
}

