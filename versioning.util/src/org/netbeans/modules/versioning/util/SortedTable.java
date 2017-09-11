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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
