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

package org.netbeans.modules.git.ui.diff;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JScrollPane;
import javax.swing.SortOrder;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.versioning.util.status.VCSStatusNode.NameProperty;
import org.netbeans.modules.versioning.util.status.VCSStatusNode.PathProperty;
import org.netbeans.modules.versioning.util.status.VCSStatusTableModel;
import org.netbeans.swing.etable.ETable;
import org.netbeans.swing.etable.ETableColumn;
import org.netbeans.swing.etable.ETableColumnModel;

/**
 * Tests that the diff file table persists and restores its sort order.
 */
public class DiffFileTableTest extends NbTestCase {

    private static final String SORTING_PANEL = "diffView";
    private static final int COLUMN_INDEX_NAME = 0;
    private static final int COLUMN_INDEX_PATH = 2;

    public DiffFileTableTest (String name) {
        super(name);
    }

    @Override
    protected boolean runInEQ () {
        return true;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        GitModuleConfig.getDefault().setSortingStatus(SORTING_PANEL, Map.of());
    }

    public void testNoSortingRestoredByDefault () {
        DiffFileTable t = createTable();

        assertNull(findSortedColumn(t, 1));
    }

    public void testRestoreSorting () {
        GitModuleConfig.getDefault().setSortingStatus(SORTING_PANEL,
                Map.of(PathProperty.NAME, SortOrder.DESCENDING));

        DiffFileTable t = createTable();

        ETableColumn column = findSortedColumn(t, 1);
        assertNotNull(column);
        assertEquals(COLUMN_INDEX_PATH, column.getModelIndex());
        assertFalse(column.isAscending());
    }

    public void testRestoreMultiColumnSorting () {
        LinkedHashMap<String, SortOrder> sortingStatus = new LinkedHashMap<>();
        sortingStatus.put(PathProperty.NAME, SortOrder.ASCENDING);
        sortingStatus.put(NameProperty.NAME, SortOrder.DESCENDING);
        GitModuleConfig.getDefault().setSortingStatus(SORTING_PANEL, sortingStatus);

        DiffFileTable t = createTable();

        ETableColumn primary = findSortedColumn(t, 1);
        assertNotNull(primary);
        assertEquals(COLUMN_INDEX_PATH, primary.getModelIndex());
        assertTrue(primary.isAscending());
        ETableColumn secondary = findSortedColumn(t, 2);
        assertNotNull(secondary);
        assertEquals(COLUMN_INDEX_NAME, secondary.getModelIndex());
        assertFalse(secondary.isAscending());
    }

    public void testPersistSorting () {
        DiffFileTable t = createTable();
        getETable(t).setColumnSorted(COLUMN_INDEX_NAME, true, 1);

        t.storeSortingStatus();

        Map<String, SortOrder> sortingStatus = GitModuleConfig.getDefault().getSortingStatus(SORTING_PANEL);
        assertEquals(Map.of(NameProperty.NAME, SortOrder.ASCENDING), sortingStatus);
    }

    public void testPersistUnsorted () {
        GitModuleConfig.getDefault().setSortingStatus(SORTING_PANEL,
                Map.of(PathProperty.NAME, SortOrder.ASCENDING));
        DiffFileTable t = createTable();
        ((ETableColumnModel) getETable(t).getColumnModel()).clearSortedColumns();

        t.storeSortingStatus();

        assertTrue(GitModuleConfig.getDefault().getSortingStatus(SORTING_PANEL).isEmpty());
    }

    private static DiffFileTable createTable () {
        return new DiffFileTable(new VCSStatusTableModel<DiffNode>(new DiffNode[0]), null);
    }

    private static ETable getETable (DiffFileTable table) {
        return (ETable) ((JScrollPane) table.getComponent()).getViewport().getView();
    }

    private static ETableColumn findSortedColumn (DiffFileTable table, int sortRank) {
        ETable eTable = getETable(table);
        for (int i = 0; i < eTable.getColumnModel().getColumnCount(); ++i) {
            ETableColumn column = (ETableColumn) eTable.getColumnModel().getColumn(i);
            if (column.isSorted() && column.getSortRank() == sortRank) {
                return column;
            }
        }
        return null;
    }
}
