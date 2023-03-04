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
package org.netbeans.modules.bugtracking.tasks.filter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jpeska
 */
public class AppliedFilters<T> {

    private List<DashboardFilter<T>> filters;

    public AppliedFilters() {
        filters = new ArrayList<DashboardFilter<T>>();
    }

    public void addFilter(DashboardFilter<T> filter) {
        if (!filters.contains(filter)) {
            filters.add(filter);
        }
    }

    public void removeFilter(DashboardFilter<T> filter) {
        filters.remove(filter);
    }

    public List<DashboardFilter> getFilters() {
        return new ArrayList<DashboardFilter>(filters);
    }

    public boolean isEmpty() {
        return filters.isEmpty();
    }

    public void clear() {
        filters.clear();
    }

    public boolean isInFilter(T entry) {
        for (DashboardFilter<T> filter : filters) {
            if (!filter.isInFilter(entry)) {
                return false;
            }
        }
        return true;
    }

    public boolean expandNodes() {
        for (DashboardFilter filter : filters) {
            if (filter.expandNodes()) {
                return true;
            }
        }
        return false;
    }

    public boolean showHitCount() {
        for (DashboardFilter filter : filters) {
            if (filter.showHitCount()) {
                return true;
            }
        }
        return false;
    }
}
