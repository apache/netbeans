/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.notifications.center;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.modules.notifications.NotificationImpl;
import org.netbeans.modules.notifications.Utils;
import org.netbeans.swing.etable.ETable;
import org.netbeans.swing.etable.ETableColumn;
import org.netbeans.swing.etable.ETableColumnModel;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author jpeska
 */
public class NotificationTable extends ETable {

    private final List<ProcessKeyEventListener> keyListeners = new ArrayList<ProcessKeyEventListener>();

    NotificationTable() {
        super(new NotificationTableModel());
        init();
    }

    private void init() {
        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(false);
        getTableHeader().setResizingAllowed(true);
        setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setFillsViewportHeight(true);

        ETableColumnModel colModel = (ETableColumnModel) getColumnModel();
        ETableColumn ecol;
        ecol = (ETableColumn) colModel.getColumn(NotificationTableModel.PRIORITY_COLUMN);
        ecol.setHeaderValue(NbBundle.getMessage(NotificationTable.class, "LBL_NotificationPriority"));
        ecol.setCellRenderer(new NotificationPriorityRenderer());

        ecol = (ETableColumn) colModel.getColumn(NotificationTableModel.TIMESTAMP_COLUMN);
        ecol.setHeaderValue(NbBundle.getMessage(NotificationTable.class, "LBL_NotificationTimestamp"));
        ecol.setCellRenderer(new NotificationDateRenderer());
        colModel.setColumnSorted(ecol, false, 1);

        ecol = (ETableColumn) colModel.getColumn(NotificationTableModel.CATEGORY_COLUMN);
        ecol.setHeaderValue(NbBundle.getMessage(NotificationTable.class, "LBL_NotificationCategory"));
        ecol.setCellRenderer(new NotificationCategoryRenderer());

        ecol = (ETableColumn) colModel.getColumn(NotificationTableModel.MESSAGE_COLUMN);
        ecol.setHeaderValue(NbBundle.getMessage(NotificationTable.class, "LBL_NotificationMessage"));
        ecol.setCellRenderer(new NotificationRenderer());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    NotificationTable table = NotificationTable.this;
                    int r = table.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < table.getRowCount()) {
                        table.setRowSelectionInterval(r, r);
                    } else {
                        table.clearSelection();
                    }
                    int modelIndex = table.convertRowIndexToModel(table.getSelectedRow());
                    JPopupMenu popup;
                    if (modelIndex < 0) {
                        popup = Utilities.actionsToPopup(Utils.getGlobalNotificationActions(), (NotificationTable) e.getSource());
                    } else {
                        NotificationTableModel model = (NotificationTableModel) getModel();
                        final NotificationImpl notification = model.getEntry(modelIndex);
                        popup = Utilities.actionsToPopup(Utils.getNotificationActions(notification), (NotificationTable) e.getSource());
                    }
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete"); // NOI18N
    }

    @Override
    protected void processKeyEvent(KeyEvent e) {
        fireProcessKeyEvent(e);
        if (!e.isConsumed()) {
            super.processKeyEvent(e);
        }
    }

    void addProcessKeyEventListener(ProcessKeyEventListener listener) {
        keyListeners.add(listener);
    }

    void removeProcessKeyEventListener(ProcessKeyEventListener listener) {
        keyListeners.remove(listener);
    }

    private void fireProcessKeyEvent(KeyEvent e) {
        for (ProcessKeyEventListener l : keyListeners) {
            l.processKeyEvent(e);
        }
    }
    
    void showNextSelection(boolean forward) {
        int selectedRow = this.getSelectedRow();
        int lastRowIndex = this.getRowCount() - 1;
        ListSelectionModel selection = this.getSelectionModel();
        int toSelect;
        if (forward) {
            toSelect = selectedRow + 1;
        } else {
            toSelect = selectedRow - 1;
        }
        if (toSelect < 0) {
            toSelect = lastRowIndex;
        } else if (toSelect > lastRowIndex) {
            toSelect = 0;
        }
        selection.setSelectionInterval(toSelect, toSelect);
    }

    void showDefaultSelection() {
        int selectedRow = this.getSelectedRow();
        if (selectedRow == -1) {
            this.getSelectionModel().setSelectionInterval(0, 0);
        }
    }

    private class NotificationPriorityRenderer extends NotificationRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value != null) {
                setIcon(((NotificationDisplayer.Priority) value).getIcon());
                setText("");
                setToolTipText(((NotificationDisplayer.Priority) value).name());
            }
            return this;
        }
    }

    private class NotificationDateRenderer extends NotificationRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setText(Utils.getFormatedDate((Calendar) value));
            setToolTipText(Utils.getFullFormatedDate((Calendar) value));
            return this;
        }
    }

    private class NotificationCategoryRenderer extends NotificationRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setToolTipText(getNotification(table, row).getCategory().getDescription());
            return this;
        }
    }

    private class NotificationRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            NotificationImpl notification = getNotification(table, row);
            if (!notification.isRead()) {
                setFont(getFont().deriveFont(Font.BOLD));
            }
            return this;
        }

        NotificationImpl getNotification(JTable table, int row) {
            NotificationTableModel model = (NotificationTableModel) table.getModel();
            NotificationImpl notification = model.getEntry(table.convertRowIndexToModel(row));
            return notification;
        }
    }

    interface ProcessKeyEventListener {
        public void processKeyEvent(KeyEvent e);
    }
}
