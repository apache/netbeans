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
package org.netbeans.modules.notifications.filter;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Set of filters
 */
public final class FilterRepository {

    private static FilterRepository theInstance;
    /**
     * the set of filters of this repository *
     */
    private final LinkedList<NotificationFilter> filters = new LinkedList<>();
    
    private int active = -1;   // index of the active filter

    /**
     * Constructor, default.
     */
    FilterRepository() {
    }

    public static FilterRepository getInstance() {
        if (null == theInstance) {
            theInstance = new FilterRepository();
        }
        return theInstance;
    }

    public void assign(final FilterRepository fr) {
        if (fr != this) {
            filters.clear();
            Iterator<NotificationFilter> it = fr.filters.iterator();
            while (it.hasNext()) {
                filters.add((NotificationFilter) it.next().clone());
            }

            active = fr.active;
        }
    }

    @Override
    public Object clone() {
        FilterRepository ret = new FilterRepository();
        ret.assign(this);
        return ret;
    }

    public List<NotificationFilter> getAllFilters() {
        return new ArrayList<>(filters);
    }

    // Implementation of java.util.Set
    /**
     * Adds a new filter to the collection, if it was not present already.
     *
     * @param f the Filter to be added
     * @return true iff it was not member before and was added
     */
    void add(NotificationFilter f) {
        filters.add(f);
    }

    /**
     * Remove the filter specified by parameter from the collection.
     *
     * @param filter the Filter to remove
     * @return true iff the filter was found and removed
     */
    void remove(NotificationFilter f) {
        if (f == getActive()) {
            setActive(null);
        }
        filters.remove(f);
    }

    Iterator<NotificationFilter> iterator() {
        return filters.iterator();
    }

    int size() {
        return filters.size();
    }

    /**
     * Returns a filter with the given name or null if not found.
     *
     * @param name name of the filter to look up
     * @return Filter with name or null
     */
    NotificationFilter getFilterByName(String name) {
        Iterator<NotificationFilter> it = filters.iterator();
        while (it.hasNext()) {
            NotificationFilter f = it.next();
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }

    public NotificationFilter getActive() {
        return (active == -1 || filters.isEmpty()) ? NotificationFilter.EMPTY : filters.get(active);
    }

    public void setActive(NotificationFilter newactive) {
        if (newactive == null || newactive.equals(NotificationFilter.EMPTY)) {
            this.active = -1;
        } else {
            int i = filters.indexOf(newactive);
            if (i != -1) {
                this.active = i;
            }
        }
    }

    public void load() throws IOException {
        filters.clear();
        active = -1;
        Preferences prefs = NbPreferences.forModule(FilterRepository.class);
        prefs = prefs.node("Filters"); //NOI18N
        active = prefs.getInt("active", -1);

        int count = prefs.getInt("count", 0); //NOI18N
        for (int i = 0; i < count; i++) {
            NotificationFilter filter = new NotificationFilter();
            try {
                filter.load(prefs, "Filter_" + i); //NOI18N
            } catch (BackingStoreException bsE) {
                throw new IOException("Cannot load filter repository", bsE);
            }
            filters.add(filter);
        }
    }

    public void save() throws IOException {
        try {
            Preferences prefs = NbPreferences.forModule(FilterRepository.class);
            prefs = prefs.node("Filters"); //NOI18N
            prefs.clear();
            prefs.putBoolean("firstTimeStart", false); //NOI18N
            prefs.putBoolean("firstTimeStartWithIssue", false); //NOI18N

            prefs.putInt("count", filters.size()); //NOI18N
            prefs.putInt("active", active); //NOI18N
            for (int i = 0; i < filters.size(); i++) {
                NotificationFilter filter = filters.get(i);
                filter.save(prefs, "Filter_" + i); //NOI18N
            }
        } catch (BackingStoreException bsE) {
            throw new IOException("Cannot save filter repository", bsE);
        }
    }

    NotificationFilter createNewFilter() {
        return new NotificationFilter(NbBundle.getMessage(FilterRepository.class, "LBL_NewFilter")); //NOI18N
    }

    void clear() {
        filters.clear();
        setActive(null);
    }

    List<NotificationFilter> getFilters() {
        return new ArrayList<>(filters);
    }
}
