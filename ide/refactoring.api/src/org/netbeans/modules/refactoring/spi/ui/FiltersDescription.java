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
package org.netbeans.modules.refactoring.spi.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.refactoring.api.impl.SPIUIAccessor;

/**
 * @author Ralph Benjamin Ruijs &lt;ralphbenjamin@netbeans.org&gt;
 * @since 1.29
 */
public final class FiltersDescription {
    
    static {
        SPIUIAccessor.DEFAULT = new AccessorImpl();
    }

    /**
     * List of {@code FilterItem}s describing filters properties
     */
    private List<FilterItem> filters;

    /**
     * Creates a new instance of FiltersDescription
     */
    public FiltersDescription() {
        filters = new ArrayList<FilterItem>();
    }

    /**
     * Add a new filter to the FiltersDescription. Filters are by default
     * disabled. {@code FiltersDescription.Provider}s should enable them when
     * used.
     *
     * @param key identifier for the filter
     * @param tooltip text to display in the tooltip of the filter button
     * @param selected true if the filter should be selected
     * @param icon icon to use for the filter button
     */
    public void addFilter(@NonNull String key, @NonNull String tooltip,
            boolean selected, @NonNull Icon icon) {
        FilterItem newItem = new FilterItem(key, tooltip, selected, icon);
        filters.add(newItem);
    }

    /**
     * Returns the number of filters in this description.
     *
     * @return the number of filters in this description
     */
    public int getFilterCount() {
        return filters.size();
    }

    /**
     * Returns the key of the filter at the supplied index.
     *
     * @param index the index of the filter
     * @return the key
     * @throws IndexOutOfBoundsException if the index is out of range
     * ({@code index < 0 || index >= size()})
     */
    public @NonNull
    String getKey(int index) {
        return filters.get(index).key;
    }

    /**
     * Returns the tooltip of the filter at the supplied index.
     *
     * @param index the index of the filter
     * @return the tooltip
     * @throws IndexOutOfBoundsException if the index is out of range
     * ({@code index < 0 || index >= size()})
     */
    public @NonNull
    String getTooltip(int index) {
        return filters.get(index).tooltip;
    }

    /**
     * Returns the icon of the filter at the supplied index.
     *
     * @param index the index of the filter
     * @return the icon
     * @throws IndexOutOfBoundsException if the index is out of range
     * ({@code index < 0 || index >= size()})
     */
    public @NonNull
    Icon getIcon(int index) {
        return filters.get(index).icon;
    }

    /**
     * Returns true if the filter at the supplied index is selected.
     *
     * @param index the index of the filter
     * @return true if the filter is selected.
     * @throws IndexOutOfBoundsException if the index is out of range
     * ({@code index < 0 || index >= size()})
     */
    public boolean isSelected(int index) {
        return filters.get(index).selected;
    }
    
    /**
     * Change the selected value of the filter at the supplied index.
     *
     * @param index the index of the filter
     * @param selected true if the filter should be selected
     * @throws IndexOutOfBoundsException if the index is out of range
     * ({@code index < 0 || index >= size()})
     * @since 1.36
     */
    public void setSelected(int index, boolean selected) {
        filters.get(index).selected = selected;
    }

    /**
     * Enable the filter at the supplied index.
     *
     * @param index the index of the filter
     * @throws IndexOutOfBoundsException if the index is out of range
     * ({@code index < 0 || index >= size()})
     */
    public void enable(int index) {
        filters.get(index).enabled = true;
    }

    /**
     * Enable the filter with the supplied key. If there is no filter with the
     * supplied key, nothing will change.
     *
     * @param key the key of a filter
     */
    public void enable(@NonNull String key) {
        for (FilterItem filterItem : filters) {
            if (filterItem.key.contentEquals(key)) {
                filterItem.enabled = true;
                break;
            }
        }
    }

    /**
     * Returns true if the filter at the supplied index is enabled.
     *
     * @param index the index of the filter
     * @return true if the filter is enabled.
     * @throws IndexOutOfBoundsException if the index is out of range
     * ({@code index < 0 || index >= size()})
     */
    public boolean isEnabled(int index) {
        return filters.get(index).enabled;
    }
    
    void reset() {
        for (FilterItem filter : filters) {
            filter.enabled = false;
        }
    }

    private static class FilterItem {

        String key;
        String tooltip;
        Icon icon;
        boolean selected;
        boolean enabled;

        FilterItem(String key, String tooltip,
                boolean selected, Icon icon) {
            this.key = key;
            this.tooltip = tooltip;
            this.icon = icon;
            this.selected = selected;
            this.enabled = false;
        }
    }

    /**
     * {@code RefactoringPlugin}s can implement this interface if they want to
     * supply new filters, or enable existing filters.
     */
    public static interface Provider {

        /**
         * Add filters to the supplied {@code FiltersDescription}. This method
         * will be called after the plugin is received from the
         * {@code RefactoringPluginFactory}.
         *
         * @param filtersDescription the {@code FiltersDescription} to add filters to
         * @see FiltersDescription#addFilter
         */
        void addFilters(FiltersDescription filtersDescription);

        /**
         * Enable filters in the supplied {@code FiltersDescription}. This method
         * will be called after the plugins {@code RefactoringPlugin#prepare} method is called.
         *
         * @param filtersDescription the {@code FiltersDescription} to enable filters from
         * @see FiltersDescription#enable
         */
        void enableFilters(FiltersDescription filtersDescription);
    }
}
