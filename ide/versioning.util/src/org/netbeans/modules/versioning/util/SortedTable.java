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

package org.netbeans.modules.versioning.util;

import javax.swing.JTable;
import javax.swing.plaf.TableHeaderUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.netbeans.modules.versioning.util.TableSorter.SortableHeaderRenderer;

/**
 * Table whose major goal is to create a custom table header, to allow fix
 * of bug #164425.
 *
 * @author Marian Petras
 */
public class SortedTable extends JTable {

    private final TableSorter sorter;

    /**
     * Creates a table and gives it a sorting header.
     */
    public SortedTable(TableSorter sorter) {
        super(sorter);

        this.sorter = sorter;
        updateSorterTableHeader();
    }

    /**
     * Creates a table header which is correctly updated and keeps working
     * when the Windows theme is switched from Windows Vista to Windows Classic
     * or vice versa.
     */
    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new TableHeader(columnModel);
    }

    @Override
    public void setTableHeader(JTableHeader tableHeader) {
        /*
         * This method could be package-private but at least one module
         * still requires it to be public.
         */
        if (tableHeader != this.tableHeader) {
            super.setTableHeader(tableHeader);
            updateSorterTableHeader();
        }
    }

    private void updateSorterTableHeader() {
        if (sorter != null) {       //is null during construction
            sorter.setTableHeader(tableHeader);
        }
    }

    /**
     * Table header with sorting enabled.
     * It was created with the aim of fixing bug #164425
     * (NullPointerException at
     * com.sun.java.swing.plaf.windows.WindowsTableHeaderUI$XPDefaultRenderer.paint).
     */
    private final class TableHeader extends JTableHeader {

        private TableHeader(TableColumnModel columnModel) {
            super(columnModel);
            setSortingRenderer();
        }

        @Override
        public void setUI(TableHeaderUI ui) {
            /*
             * It fixes the bug by reseting the renderer to the default (non-sorting)
             * renderer before the UI is changed.
             * Thanks to this change (the reset), method
             * WindowsTableHeaderUI.uninstallUI(JComponent) then resets the
             * renderer to the default Windows L&F renderer which is the
             * renderer used when the Windows Classic theme is used.
             */
            if (ui != this.ui) {
                unsetSortingRenderer();
                super.setUI(ui);
                setSortingRenderer();
                repaint();
            }
        }

        /**
         * Resets the renderer to the default L&F renderer.
         */
        private void unsetSortingRenderer() {
            TableCellRenderer defaultRenderer = getDefaultRenderer();
            if (defaultRenderer instanceof TableSorter.SortableHeaderRenderer) {
                setDefaultRenderer(((TableSorter.SortableHeaderRenderer) defaultRenderer).getRendererDelegate());
            }
        }

        /**
         * Sets a custom sorting renderer, which delegates to the default L&F
         * renderer.
         */
        private void setSortingRenderer() {
            if (sorter != null) {       //is null during construction
                TableCellRenderer defaultRenderer = getDefaultRenderer();
                if (!(defaultRenderer instanceof TableSorter.SortableHeaderRenderer)) {
                    setDefaultRenderer(sorter.new SortableHeaderRenderer(defaultRenderer));
                }
            }
        }

    }

}
