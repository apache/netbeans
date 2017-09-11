/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.api.customizer.support;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.maven.spi.customizer.SelectedItemsTablePersister;


public final class SelectedItemsTable extends JTable {

    private static final int CHECKBOX_WIDTH = new JCheckBox().getWidth();

    public SelectedItemsTable(SelectedItemsTableModel model) {
        super(model);

        getColumnModel().getColumn(0).setMaxWidth(CHECKBOX_WIDTH + 20);
        setRowHeight(getFontMetrics(getFont()).getHeight() + (2 * getRowMargin()));
        setTableHeader(null);
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setShowGrid(false);

        final Action switchAction = new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int row = getSelectedRow();
                if (row == -1) {
                    // Nothing selected; e.g. user has tabbed into the table but not pressed Down key.
                    return;
                }
                Boolean b = (Boolean) getValueAt(row, 0);
                setValueAt(Boolean.valueOf(!b.booleanValue()), row, 0);
            }
        };

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switchAction.actionPerformed(null);
            }
        });

        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "startEditing"); // NOI18N
        getActionMap().put("startEditing", switchAction); // NOI18N
    }

    public SelectedItemsTable() {
        this(null);
    }


    public static final class SelectedItemsTableModel extends AbstractTableModel {

        private Boolean[] selected;
        private Boolean[] originalSelected;
        private String[] pkgNames;

        private final SelectedItemsTablePersister persister;

        public SelectedItemsTableModel(SelectedItemsTablePersister persister) {
            this.persister = persister;
            reloadData(persister.read());
        }

        void reloadData(SortedMap<String, Boolean> items) {
            selected = new Boolean[items.size()];
            items.values().toArray(selected);
            if (originalSelected == null) {
                originalSelected = new Boolean[items.size()];
                System.arraycopy(selected, 0, originalSelected, 0, selected.length);
            }
            pkgNames = new String[items.size()];
            items.keySet().toArray(pkgNames);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return pkgNames.length;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return selected[rowIndex];
            } else {
                return pkgNames[rowIndex];
            }
        }

        @Override
        public Class getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            assert columnIndex == 0 : "Who is trying to modify second column?"; // NOI18N
            selected[rowIndex] = (Boolean) aValue;
            persister.write(getItemsMap());
            fireTableCellUpdated(rowIndex, 0);
        }

        private SortedMap<String, Boolean> getItemsMap() {
            SortedMap<String, Boolean> itemsMap = new TreeMap<String, Boolean>();
            for (int i = 0; i < pkgNames.length; i++) {
                itemsMap.put(pkgNames[i], selected[i]);
            }
            return itemsMap;
        }

        public boolean isChanged() {
            return !Arrays.asList(selected).equals(Arrays.asList(originalSelected));
        }

    }
}
