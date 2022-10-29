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
package org.netbeans.lib.profiler.ui.swing;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * TableRowSorter for single-column sorting with default SortOrders for each column. 
 *
 * @author Jiri Sedlacek
 */
class ProfilerRowSorter extends TableRowSorter {
    
    // --- Package-private constructor -----------------------------------------
    
    ProfilerRowSorter(TableModel model) {
        super(model);
    }
    
    // --- Sorting support -----------------------------------------------------
    
    private SortOrder defaultSortOrder = SortOrder.ASCENDING;
    private Map<Integer, SortOrder> defaultSortOrders;
    
    private int secondarySortColumn = -1;
    
    private boolean threeStateColumns;
    
    public void setAllowsThreeStateColumns(boolean threeStateColumns) {
        this.threeStateColumns = threeStateColumns;
    }
    
    public boolean allowsThreeStateColumns() {
        return threeStateColumns;
    }
    
    public void toggleSortOrder(int column) {
        // UNSORTED not allowed for sorting columns (default)
        if (!allowsThreeStateColumns()) {
            super.toggleSortOrder(column);
            return;
        }
        
        // Switching from one column to another
        if (getSortColumn() != column) {
            super.toggleSortOrder(column);
            return;
        }
        
        // Toggling from default sort order
        SortOrder so = getSortOrder();
        if (Objects.equals(getDefaultSortOrder(column), so)) {
            super.toggleSortOrder(column);
            return;
        }
        
        // Resetting UNSORTED, use default sort order
        if (Objects.equals(SortOrder.UNSORTED, so)) {
            setSortColumn(column);
            return;
        }
        
        // Toggling from second sort order, switch to UNSORTED
        setSortColumn(column, SortOrder.UNSORTED);
    }
    
    public void setSortKeys(List newKeys) {
        if (newKeys == null || newKeys.isEmpty()) {
            setSortKeysImpl(newKeys);
            return;
        }
        
        RowSorter.SortKey oldKey = getSortKey();
        RowSorter.SortKey newKey = (RowSorter.SortKey)newKeys.get(0);
        
        if (oldKey == null || oldKey.getColumn() != newKey.getColumn()) {
            // Use defined initial SortOrder for a newly sorted column
            setSortColumn(newKey.getColumn());
        } else {
            setSortKeysImpl(newKeys);
        }
    }
    
    protected void setSortKeysImpl(List newKeys) {
        super.setSortKeys(newKeys);
    }
    
    void setSortColumn(int column) {
        setSortColumn(column, getDefaultSortOrder(column));
    }
    
    void setSortColumn(int column, SortOrder order) {
        setSortKey(new RowSorter.SortKey(column, order));
    }
    
    void setSortKey(RowSorter.SortKey key) {
        RowSorter.SortKey secondaryKey = secondarySortColumn == -1 ||
                          secondarySortColumn == key.getColumn() ? null :
                          new RowSorter.SortKey(secondarySortColumn,
                          getDefaultSortOrder(secondarySortColumn));
        setSortKeysImpl(secondaryKey == null ? Arrays.asList(key) :
                          Arrays.asList(key, secondaryKey));
    }
    
    int getSortColumn() {
        RowSorter.SortKey key = getSortKey();
        return key == null ? -1 : key.getColumn();
    }
    
    SortOrder getSortOrder() {
        RowSorter.SortKey key = getSortKey();
        return key == null ? SortOrder.UNSORTED : key.getSortOrder();
    }
    
    RowSorter.SortKey getSortKey() {
        List<? extends RowSorter.SortKey> keys = getSortKeys();
        return keys == null || keys.isEmpty() ? null : keys.get(0);
    }
    
    
    void setSecondarySortColumn(int column) {
        secondarySortColumn = column;
    }
    
    int getSecondarySortColumn() {
        return secondarySortColumn;
    }
    
    
    void setDefaultSortOrder(SortOrder sortOrder) {
        defaultSortOrder = sortOrder;
    }
    
    void setDefaultSortOrder(int column, SortOrder sortOrder) {
        if (defaultSortOrders == null) defaultSortOrders = new HashMap();
        defaultSortOrders.put(column, sortOrder);
    }
    
    SortOrder getDefaultSortOrder(int column) {
        SortOrder order = defaultSortOrders == null ? null :
                          defaultSortOrders.get(column);
        return order == null ? defaultSortOrder : order;
    }
    
    
    // --- Filtering support ---------------------------------------------------
    
    private boolean filterMode = true; // AND filter by default
    private Collection<RowFilter<Object, Object>> filters;
    
    // false = OR, true = AND
    void setFiltersMode(boolean mode) {
        this.filterMode = mode;
        if (filters != null) refreshRowFilter();
    }
    
    boolean getFiltersMode() {
        return filterMode;
    }
    
    void addRowFilter(RowFilter filter) {
        if (filters == null) filters = new HashSet();
        filters.remove(filter);
        filters.add(filter);
        refreshRowFilter();
    }
    
    void removeRowFilter(RowFilter filter) {
        if (filters == null) return;
        filters.remove(filter);
        refreshRowFilter();
    }
    
    private void refreshRowFilter() {
        if (filters == null || filters.isEmpty()) {
            setRowFilter(null);
        } else if (filters.size() == 1) {
            setRowFilter(filters.iterator().next());
        } else {
            setRowFilter(filterMode ? RowFilter.andFilter(filters) :
                                      RowFilter.orFilter(filters));
        }
    }
    
    // --- Persistence ---------------------------------------------------------
    
    private static final String SORT_COLUMN_KEY = "ProfilerRowSorter.SortColumn"; // NOI18N
    private static final String SORT_ORDER_KEY = "ProfilerRowSorter.SortOrder"; // NOI18N
    
    void loadFromStorage(Properties properties, ProfilerTable table) {
        String columnS = properties.getProperty(SORT_COLUMN_KEY);
        String orderS = properties.getProperty(SORT_ORDER_KEY);
        if (columnS != null) {
            try {
                int column = Integer.parseInt(columnS);
                SortOrder order = getSortOrder(orderS);
//                if (SortOrder.UNSORTED.equals(order)) order = getDefaultSortOrder(column);
                setSortColumn(column, order);
            } catch (NumberFormatException e) {
                // Reset sorting? Set default column?
            }
        } else {
            // Reset sorting? Set default column?
        }
    }
    
    void saveToStorage(Properties properties, ProfilerTable table) {
        RowSorter.SortKey key = getSortKey();
        if (key == null) {
            properties.remove(SORT_COLUMN_KEY);
            properties.remove(SORT_ORDER_KEY);
        } else {
            int column = key.getColumn();
            SortOrder order = key.getSortOrder();
            properties.setProperty(SORT_COLUMN_KEY, Integer.toString(column));
            properties.setProperty(SORT_ORDER_KEY, order.toString());
        }
    }
    
    private SortOrder getSortOrder(String sortOrder) {
        if (SortOrder.ASCENDING.toString().equals(sortOrder)) return SortOrder.ASCENDING;
        else if (SortOrder.DESCENDING.toString().equals(sortOrder)) return SortOrder.DESCENDING;
        else return SortOrder.UNSORTED;
    }
    
}
