/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.refactoring.spi.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.refactoring.api.impl.SPIUIAccessor;

/**
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
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
