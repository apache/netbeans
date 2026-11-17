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

package org.netbeans.modules.git.ui.rebase;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DropMode;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.netbeans.libs.git.GitRebaseTodoLine.RebaseAction;
import org.netbeans.libs.git.GitRevisionInfo;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * The table of the rebase steps to be performed, like the todo file of
 * <code>git rebase -i</code>: the number of each commit (1 is the newest
 * commit, at the head), the action (pick, squash, fixup, drop), the
 * revision id and the subject, oldest first. A squash or fixup row folds
 * the commit into the commit of the row above. The buttons above the table
 * move the selected step towards the top or the bottom of the list.
 *
 * <p>The rows are colored after the colors of the diff view: a dropped row
 * like a deleted diff line, a squashed or fixup-ed row like a changed diff
 * line, and the revision of a commit that the rebase rebuilds (with a new
 * hash) is painted like a changed diff line.
 *
 * @author mbien
 */
@SuppressWarnings("serial")
public class InteractiveRebasePanel extends JPanel {

    private final TodoTableModel tableModel;

    @NbBundle.Messages({
        "TTT_InteractiveRebasePanel.btnUp=Move the selected commit up in the list",
        "TTT_InteractiveRebasePanel.btnDown=Move the selected commit down in the list"
    })
    /** Creates new form InteractiveRebasePanel */
    public InteractiveRebasePanel() {
        initComponents();
        tableModel = new TodoTableModel();
        tblRebase.setModel(tableModel);
        initializeColumns(tblRebase);
        pnlInfo.setLayout(new BorderLayout());
        btnUp.setToolTipText(Bundle.TTT_InteractiveRebasePanel_btnUp());
        btnDown.setToolTipText(Bundle.TTT_InteractiveRebasePanel_btnDown());
        btnUp.addActionListener(e -> moveSelection(-1));
        btnDown.addActionListener(e -> moveSelection(1));
        tblRebase.getSelectionModel().addListSelectionListener(e -> updateMoveButtons());
        tblRebase.setDragEnabled(true);
        tblRebase.setDropMode(DropMode.INSERT_ROWS);
        tblRebase.setTransferHandler(new RowTransferHandler());
    }

    /**
     * Moves the selected row one position towards the top or the bottom of
     * the list, keeping the selection on the moved row.
     *
     * @param delta -1 to move the row up, 1 to move it down
     */
    private void moveSelection(int delta) {
        int row = tblRebase.getSelectedRow();
        int target = row + delta;
        if (row < 0 || target < 0 || target >= tableModel.getRowCount()) {
            return;
        }
        tableModel.moveRow(row, target);
        tblRebase.setRowSelectionInterval(target, target);
    }

    /**
     * The move buttons are only enabled for a row that has a neighbor in the
     * direction it is moved to.
     */
    private void updateMoveButtons() {
        int row = tblRebase.getSelectedRow();
        btnUp.setEnabled(row > 0);
        btnDown.setEnabled(row >= 0 && row < tableModel.getRowCount() - 1);
    }

    /** The info panel the controller fills with the details of the commit
     * selected in the todo table. */
    public JPanel getInfoPanel() {
        return pnlInfo;
    }

    /** The todo table. */
    public JTable getTable() {
        return tblRebase;
    }

    /** The table model holding the rebase steps. */
    public TodoTableModel getTableModel() {
        return tableModel;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnUp = new JButton();
        btnDown = new JButton();
        splitPane = new JSplitPane();
        scrollRebase = new JScrollPane();
        tblRebase = new JTable();
        pnlInfo = new JPanel();

        btnUp.setIcon(ImageUtilities.loadImageIcon("/org/netbeans/modules/git/resources/icons/rebase-up.png", false));
        btnUp.setFocusPainted(false);

        btnDown.setIcon(ImageUtilities.loadImageIcon("/org/netbeans/modules/git/resources/icons/rebase-down.png", false));
        btnDown.setFocusPainted(false);

        splitPane.setDividerLocation(350);

        tblRebase.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollRebase.setViewportView(tblRebase);

        splitPane.setLeftComponent(scrollRebase);
        splitPane.setRightComponent(pnlInfo);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(splitPane, GroupLayout.DEFAULT_SIZE, 709, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnUp)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDown)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnUp, GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                    .addComponent(btnDown, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(7, 7, 7)
                .addComponent(splitPane)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    JButton btnDown;
    JButton btnUp;
    JPanel pnlInfo;
    JScrollPane scrollRebase;
    JSplitPane splitPane;
    JTable tblRebase;
    // End of variables declaration//GEN-END:variables

    /**
     * The model of the todo table.
     */
    @NbBundle.Messages({
        "LBL_InteractiveRebasePanel.column.number=#",
        "LBL_InteractiveRebasePanel.column.action=Action",
        "LBL_InteractiveRebasePanel.column.revision=Revision",
        "LBL_InteractiveRebasePanel.column.message=Message"
    })
    public static final class TodoTableModel extends AbstractTableModel {

        private final String[] columns = new String[] {
            Bundle.LBL_InteractiveRebasePanel_column_number(),
            Bundle.LBL_InteractiveRebasePanel_column_action(),
            Bundle.LBL_InteractiveRebasePanel_column_revision(),
            Bundle.LBL_InteractiveRebasePanel_column_message()
        };
        private final List<GitRevisionInfo> revisions = new ArrayList<>();
        private final List<RebaseAction> actions = new ArrayList<>();
        private final List<Integer> numbers = new ArrayList<>();
        // the message the user settled for the commit of the row (a reword),
        // or <code>null</code> if the commit keeps its original message
        private final List<String> messages = new ArrayList<>();
        // the message the user settled for the run of squashed or fixup-ed
        // rows the row absorbs, or <code>null</code> if the run keeps its
        // auto-combined message
        private final List<String> runMessages = new ArrayList<>();
        // the order and the position of each commit when the plan was
        // loaded: the chain the branch has now, the plan is compared to it
        private final List<String> loadOrder = new ArrayList<>();
        private final Map<String, Integer> loadIndices = new HashMap<>();
        private final List<RowState> rowStates = new ArrayList<>();

        /**
         * Fills the table with the commits to be rebased, all initially
         * picked; the commits must be ordered oldest first, like the todo
         * file of git rebase -i. Each commit is numbered by its distance
         * from the head: the newest commit (the last row) is 1, the oldest
         * (the first row) is the size of the range.
         */
        public void setRevisions(List<GitRevisionInfo> revisions) {
            this.revisions.clear();
            this.actions.clear();
            this.numbers.clear();
            this.messages.clear();
            this.runMessages.clear();
            this.loadOrder.clear();
            this.loadIndices.clear();
            this.rowStates.clear();
            for (GitRevisionInfo revision : revisions) {
                this.revisions.add(revision);
                this.actions.add(RebaseAction.PICK);
                loadIndices.put(revision.getRevision(), this.revisions.size() - 1);
                loadOrder.add(revision.getRevision());
            }
            int n = this.revisions.size();
            for (int i = 0; i < n; i++) {
                this.numbers.add(n - i);
                this.messages.add(null);
                this.runMessages.add(null);
                this.rowStates.add(RowState.UNCHANGED);
            }
            computeRowStates();
            fireTableDataChanged();
        }

        /** The message the user settled for the commit of a row, or
         * <code>null</code> if the commit keeps its original message. */
        public String getMessage(int rowIndex) {
            return messages.get(rowIndex);
        }

        /** Stores the message the user settled for the commit of a row. */
        public void setMessage(int rowIndex, String message) {
            messages.set(rowIndex, message);
        }

        /** The message the user settled for the run of squashed or fixup-ed
         * rows a row absorbs, or <code>null</code> if the run keeps its
         * auto-combined message. */
        public String getRunMessage(int rowIndex) {
            return runMessages.get(rowIndex);
        }

        /** Stores the message the user settled for the run of squashed or
         * fixup-ed rows a row absorbs. */
        public void setRunMessage(int rowIndex, String message) {
            runMessages.set(rowIndex, message);
        }

        @Override
        public int getRowCount() {
            return revisions.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return switch (columnIndex) {
                case 0 -> numbers.get(rowIndex);
                case 1 -> actions.get(rowIndex);
                case 2 -> {
                    String id = revisions.get(rowIndex).getRevision();
                    yield id.length() > 10 ? id.substring(0, 10) : id;
                }
                default -> revisions.get(rowIndex).getShortMessage();
            };
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            // the oldest commit (the first row, the table is oldest first)
            // cannot be squashed or fixup-ed: there is no commit above it to
            // fold into; after reordering this is always the current first
            // row
            return columnIndex == 1 && rowIndex > 0;
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            if (columnIndex == 1 && value instanceof RebaseAction ra) {
                actions.set(rowIndex, ra);
                clearRunMessages();
                computeRowStates();
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }

        /**
         * Forgets the messages the user settled for the squashed runs: the
         * composition of the runs changed, the settled messages apply to a
         * different set of commits and are settled again when the rebase is
         * started. The reword messages are kept, they belong to the commits
         * themselves.
         */
        private void clearRunMessages() {
            for (int i = 0; i < runMessages.size(); i++) {
                runMessages.set(i, null);
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 0 -> Integer.class;
                case 1 -> JComboBox.class;
                default -> Object.class;
            };
        }

        /** The commit of a row of the table. */
        public GitRevisionInfo getRevision(int rowIndex) {
            return revisions.get(rowIndex);
        }

        /** The action of a row of the table. */
        public RebaseAction getAction(int rowIndex) {
            return actions.get(rowIndex);
        }

        /**
         * Moves a row of the table to another position, keeping the actions
         * and the number with the commit. A squash or fixup row moved to the
         * first row (the oldest commit, which cannot be squashed or
         * fixup-ed) is switched to pick.
         *
         * @param from the row to move
         * @param to the position to move it to
         */
        public void moveRow(int from, int to) {
            GitRevisionInfo revision = revisions.remove(from);
            RebaseAction moved = actions.remove(from);
            int number = numbers.remove(from);
            String message = messages.remove(from);
            String runMessage = runMessages.remove(from);
            revisions.add(to, revision);
            actions.add(to, moved);
            numbers.add(to, number);
            messages.add(to, message);
            runMessages.add(to, runMessage);
            if (to == 0 && (moved == RebaseAction.SQUASH || moved == RebaseAction.FIXUP)) {
                actions.set(to, RebaseAction.PICK);
            }
            clearRunMessages();
            computeRowStates();
            fireTableDataChanged();
        }

        /**
         * What the rebase does to the commit of a row, relative to the
         * branch it is on now.
         */
        public enum RowState {
            /** The commit stays on the branch unchanged, with the same hash. */
            UNCHANGED,
            /** The commit stays on the branch but the rebase rebuilds it
             * (with a new hash): it absorbs a squashed or fixup-ed row, or
             * it is built on something other than the commit that originally
             * preceded it. */
            MODIFIED,
            /** The commit is folded into the row above (squash, fixup): its
             * changes stay on the branch, the commit itself does not. */
            FOLDED,
            /** The commit and its changes are removed from the branch (drop). */
            REMOVED
        }

        /** What the rebase does to the commit of a row. */
        public RowState getRowState(int rowIndex) {
            return rowStates.get(rowIndex);
        }

        /**
         * Recomputes what the rebase does to each commit. A dropped row is
         * REMOVED; a squashed or fixup-ed row is FOLDED into the nearest
         * surviving row above, which is MODIFIED (its hash changes) as are
         * all surviving rows below it. A surviving row keeps its hash only
         * if the range is still linear and it is still built on the commit
         * that originally preceded it (or on the branch, for the oldest
         * commit); any other base rebuilds the row and every row built on it.
         */
        private void computeRowStates() {
            int survivor = -1; // the nearest surviving row above the current row
            for (int i = 0; i < revisions.size(); i++) {
                RebaseAction action = actions.get(i);
                switch (action) {
                    case DROP -> rowStates.set(i, RowState.REMOVED);
                    case SQUASH, FIXUP -> {
                        rowStates.set(i, RowState.FOLDED);
                        if (survivor >= 0) {
                            rowStates.set(survivor, RowState.MODIFIED);
                        }
                    }
                    default -> {
                        // the original parent of the commit (the range is
                        // linear): the commit that preceded it when the plan
                        // was loaded, or the branch for the oldest commit
                        Integer loaded = loadIndices.get(revisions.get(i).getRevision());
                        String expectedParent = (loaded == null || loaded == 0) ? null : loadOrder.get(loaded - 1);
                        String actualParent = survivor < 0 ? null : revisions.get(survivor).getRevision();
                        boolean modified = action != RebaseAction.PICK
                                || !Objects.equals(actualParent, expectedParent)
                                || (survivor >= 0 && rowStates.get(survivor) == RowState.MODIFIED);
                        rowStates.set(i, modified ? RowState.MODIFIED : RowState.UNCHANGED);
                        survivor = i;
                    }
                }
            }
        }
    }

    /**
     * Allows reordering of commits via drag and drop.
     * 
     * Insertion goes through TodoTableModel.moveRow, which also switches a squash
     * or fixup dropped onto the first row back to pick.
     */
    private final class RowTransferHandler extends TransferHandler {

        /** The flavor of the dragged row, no other component accepts it. */
        private static final DataFlavor ROW_FLAVOR;
        static {
            DataFlavor flavor;
            try {
                flavor = new DataFlavor("application/x-netbeans-git-rebase-todo-row");
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
            ROW_FLAVOR = flavor;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            int row = tblRebase.getSelectedRow();
            return row >= 0 ? new RowTransferable(row) : null;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY;
        }

        @Override
        public boolean canImport(JComponent c, DataFlavor[] flavors) {
            for (DataFlavor flavor : flavors) {
                if (ROW_FLAVOR.equals(flavor)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean importData(JComponent c, Transferable t) {
            if (!(c instanceof JTable table) || !t.isDataFlavorSupported(ROW_FLAVOR)) {
                return false;
            }
            // the table's drop location carries the exact insertion index,
            // the insertion line is drawn at the same position
            JTable.DropLocation drop = table.getDropLocation();
            if (drop == null || drop.getRow() < 0) {
                return false;
            }
            Object data;
            try {
                data = t.getTransferData(ROW_FLAVOR);
            } catch (UnsupportedFlavorException | IOException ex) {
                return false;
            }
            if (!(data instanceof Integer from)) {
                return false;
            }
            // moveRow takes the index of the row in the table without the
            // moved row
            int to = from < drop.getRow() ? drop.getRow() - 1 : drop.getRow();
            if (to == from) {
                return true; // the row was dropped where it already is
            }
            tableModel.moveRow(from, to);
            tblRebase.setRowSelectionInterval(to, to);
            return true;
        }

        /** The dragged row of the todo table. */
        private record RowTransferable(int row) implements Transferable {

            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[] { ROW_FLAVOR };
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return ROW_FLAVOR.equals(flavor);
            }

            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                if (!ROW_FLAVOR.equals(flavor)) {
                    throw new UnsupportedFlavorException(flavor);
                }
                return row;
            }
        }
    }

    /**
     * Sets up the action column with the pick/squash/fixup/drop choices, an
     * arrow marking the squash and fixup rows (the commit folds into the row
     * above, like in the todo file of git rebase -i) and the revision
     * column with a monospace font. All the cell renderers tint the cells
     * after the state the rebase gives the row (see cellBackground).
     */
    private void initializeColumns(JTable table) {
        // the default renderers of the number and the message column tint
        // the cells after the state of the row like the other renderers
        table.setDefaultRenderer(Integer.class, new StateLabel(JLabel.RIGHT));
        table.setDefaultRenderer(Object.class, new StateLabel(JLabel.LEFT));

        JComboBox<RebaseAction> actions = new JComboBox<>(new RebaseAction[] {
            RebaseAction.PICK, RebaseAction.REWORD, RebaseAction.SQUASH, RebaseAction.FIXUP, RebaseAction.DROP
        });
        // the combo box that edits the action column centers its values like
        // the table does; a dedicated renderer is used (not the table's
        // ActionCellRenderer) so the two containers don't share a stateful
        // label
        actions.setRenderer(new CenteredComboRenderer());
        table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(actions));
        table.getColumnModel().getColumn(1).setCellRenderer(new ActionCellRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new MonospaceCellRenderer());

        int column = 0;
        TableCellRenderer renderer = table.getDefaultRenderer(table.getColumnClass(column));
        Component comp = renderer.getTableCellRendererComponent(
            table, "99", false, false, 0, column
        );
        int width = comp.getPreferredSize().width; // padding
        table.getColumnModel().getColumn(column).setPreferredWidth(width);
        table.getColumnModel().getColumn(column).setMaxWidth(width);
    }

    /**
     * The renderer of the action column: the value is centered and a squash
     * or fixup row (the commit folds into the row above) is marked with an
     * up arrow.
     */
    private final class ActionCellRenderer extends JLabel implements TableCellRenderer {

        ActionCellRenderer() {
            setOpaque(true);
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            RebaseAction action = value instanceof RebaseAction ra ? ra : null;
            boolean foldsUp = action == RebaseAction.SQUASH || action == RebaseAction.FIXUP;
            setText((foldsUp ? "↑ " : "") + Objects.toString(value, ""));
            setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            setBackground(isSelected ? table.getSelectionBackground() : cellBackground(row, column, table));
            return this;
        }
    }

    /**
     * The renderer of the combo box that edits the action column: the values
     * are centered, like in the table. The selection and the colors are
     * handled by the inherited DefaultListCellRenderer.
     */
    private static final class CenteredComboRenderer extends DefaultListCellRenderer {

        CenteredComboRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }
    }

    private final class MonospaceCellRenderer extends JLabel implements TableCellRenderer {

        MonospaceCellRenderer() {
            setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            setOpaque(true);
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(Objects.toString(value, ""));
            setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            setBackground(isSelected ? table.getSelectionBackground() : cellBackground(row, column, table));
            return this;
        }
    }

    /**
     * The background of a cell of a row the rebase changes, after the
     * colors of the diff view: a dropped row like a deleted diff line, a
     * squashed or fixup-ed row like a changed diff line, and the revision of
     * a row that the rebase rebuilds (the commit gets a new hash) like a
     * changed diff line; the other cells of such a row stay uncolored.
     */
    private Color cellBackground(int row, int column, JTable table) {
        if (row < 0 || row >= tableModel.getRowCount()) {
            return table.getBackground();
        }
        return switch (tableModel.getRowState(row)) {
            case REMOVED -> deletedColor();
            case FOLDED -> changedColor();
            case MODIFIED -> column == 2 ? changedColor() : table.getBackground(); // the revision
            default -> table.getBackground();
        };
    }

    /** The color the diff view uses for deleted lines, or the default the
     * diff view falls back to if the user has not configured it. */
    private static Color deletedColor() {
        Color c = UIManager.getColor("nb.diff.deleted.color"); //NOI18N
        return c != null ? c : new Color(255, 160, 180);
    }

    /** The color the diff view uses for changed lines, or the default the
     * diff view falls back to if the user has not configured it. */
    private static Color changedColor() {
        Color c = UIManager.getColor("nb.diff.changed.color"); //NOI18N
        return c != null ? c : new Color(160, 200, 255);
    }

    /**
     * A default cell renderer that tints the cell after the state the
     * rebase gives its row (see cellBackground).
     */
    private final class StateLabel extends DefaultTableCellRenderer {

        StateLabel(int alignment) {
            setHorizontalAlignment(alignment);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                setBackground(cellBackground(row, column, table));
            }
            return this;
        }
    }

}
