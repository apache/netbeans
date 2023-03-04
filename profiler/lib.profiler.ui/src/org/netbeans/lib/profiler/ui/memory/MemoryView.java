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
package org.netbeans.lib.profiler.ui.memory;

import java.util.ResourceBundle;
import org.netbeans.lib.profiler.filters.GenericFilter;
import org.netbeans.lib.profiler.results.memory.MemoryResultsSnapshot;
import org.netbeans.lib.profiler.ui.results.DataView;
import org.netbeans.lib.profiler.ui.swing.ExportUtils;
import org.netbeans.lib.profiler.ui.swing.ProfilerTable;
import org.netbeans.lib.profiler.utils.StringUtils;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class MemoryView extends DataView {
    
    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.memory.Bundle"); // NOI18N
    static final String EXPORT_TOOLTIP = messages.getString("MemoryView_ExportTooltip"); // NOI18N
    static final String EXPORT_OBJECTS = messages.getString("MemoryView_ExportObjects"); // NOI18N
    static final String EXPORT_LIVE = messages.getString("MemoryView_ExportLive"); // NOI18N
    static final String EXPORT_ALLOCATED = messages.getString("MemoryView_ExportAllocated"); // NOI18N
    static final String EXPORT_ALLOCATED_LIVE = messages.getString("MemoryView_ExportAllocatedLive"); // NOI18N
    static final String COLUMN_NAME = messages.getString("MemoryView_ColumnName"); // NOI18N
    static final String COLUMN_ALLOCATED_BYTES = messages.getString("MemoryView_ColumnAllocatedBytes"); // NOI18N
    static final String COLUMN_ALLOCATED_OBJECTS = messages.getString("MemoryView_ColumnAllocatedObjects"); // NOI18N
    static final String COLUMN_LIVE_BYTES = messages.getString("MemoryView_ColumnLiveBytes"); // NOI18N
    static final String COLUMN_LIVE_OBJECTS = messages.getString("MemoryView_ColumnLiveObjects"); // NOI18N
    static final String COLUMN_TOTAL_ALLOCATED_OBJECTS = messages.getString("MemoryView_ColumnTotalAllocatedObjects"); // NOI18N
    static final String COLUMN_AVG_AGE = messages.getString("MemoryView_ColumnAvgAge"); // NOI18N
    static final String COLUMN_GENERATIONS = messages.getString("MemoryView_ColumnGenerations"); // NOI18N
    static final String COLUMN_SELECTED = messages.getString("MemoryView_ColumnSelected"); // NOI18N
    static final String ACTION_GOTOSOURCE = messages.getString("MemoryView_ActionGoToSource"); // NOI18N
    static final String ACTION_PROFILE_METHOD = messages.getString("MemoryView_ActionProfileMethod"); // NOI18N
    static final String ACTION_PROFILE_CLASS = messages.getString("MemoryView_ActionProfileClass"); // NOI18N
    static final String SELECTED_COLUMN_TOOLTIP = messages.getString("MemoryView_SelectedColumnTooltip"); // NOI18N
    static final String NAME_COLUMN_TOOLTIP = messages.getString("MemoryView_NameColumnTooltip"); // NOI18N
    static final String LIVE_SIZE_COLUMN_TOOLTIP = messages.getString("MemoryView_LiveSizeColumnTooltip"); // NOI18N
    static final String LIVE_COUNT_COLUMN_TOOLTIP = messages.getString("MemoryView_LiveCountColumnTooltip"); // NOI18N
    static final String ALLOC_SIZE_COLUMN_TOOLTIP = messages.getString("MemoryView_AllocSizeColumnTooltip"); // NOI18N
    static final String ALLOC_COUNT_COLUMN_TOOLTIP = messages.getString("MemoryView_AllocCountColumnTooltip"); // NOI18N
    static final String TOTAL_ALLOC_COUNT_COLUMN_TOOLTIP = messages.getString("MemoryView_TotalAllocCountColumnTooltip"); // NOI18N
    static final String AVG_AGE_COLUMN_TOOLTIP = messages.getString("MemoryView_AvgAgeColumnTooltip"); // NOI18N
    static final String GENERATIONS_COLUMN_TOOLTIP = messages.getString("MemoryView_GenerationsColumnTooltip"); // NOI18N
    static final String FILTER_CLASSES_SCOPE = messages.getString("MemoryView_FilterClassesScope"); // NOI18N
    static final String FILTER_ALLOCATIONS_SCOPE = messages.getString("MemoryView_FilterAllocationsScope"); // NOI18N
    static final String FILTER_SCOPE_TOOLTIP = messages.getString("MemoryView_FilterScopeTooltip"); // NOI18N
    static final String SEARCH_CLASSES_SCOPE = messages.getString("MemoryView_SearchClassesScope"); // NOI18N
    static final String SEARCH_ALLOCATIONS_SCOPE = messages.getString("MemoryView_SearchAllocationsScope"); // NOI18N
    static final String SEARCH_SCOPE_TOOLTIP = messages.getString("MemoryView_SearchScopeTooltip"); // NOI18N
    static final String EXPAND_MENU = messages.getString("MemoryView_ExpandMenu"); // NOI18N
    static final String EXPAND_PLAIN_ITEM = messages.getString("MemoryView_ExpandPlainItem"); // NOI18N
    static final String EXPAND_TOPMOST_ITEM = messages.getString("MemoryView_ExpandTopmostItem"); // NOI18N
    static final String COLLAPSE_CHILDREN_ITEM = messages.getString("MemoryView_CollapseChildrenItem"); // NOI18N
    static final String COLLAPSE_ALL_ITEM = messages.getString("MemoryView_CollapseAllItem"); // NOI18N
    // -----_GenerationsCo
    
    
    public abstract void setData(MemoryResultsSnapshot snapshot, GenericFilter filter, int aggregation);
    
    public abstract void resetData();
    
    
    public abstract void showSelectionColumn();
    
    public abstract void refreshSelection();
    
    
    public abstract ExportUtils.ExportProvider[] getExportProviders();
    
    
    protected abstract ProfilerTable getResultsComponent();
    
    
    static final void userFormClassNames(MemoryResultsSnapshot snapshot) {
        // class names in VM format
        String[] classNames = snapshot == null ? null : snapshot.getClassNames();
        if (classNames != null) for (int i = 0; i < classNames.length; i++)
            classNames[i] = StringUtils.userFormClassName(classNames[i]);
    }
    
}
