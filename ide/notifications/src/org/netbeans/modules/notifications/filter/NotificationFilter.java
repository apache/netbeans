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

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.modules.notifications.NotificationImpl;

/**
 *
 * @author S. Aubrecht
 * @author jpeska
 */
public class NotificationFilter {

    public static final NotificationFilter EMPTY = new EmptyNotificationFilter();
    private String name;
    private CategoryFilter categoryFilter = new CategoryFilter();

    NotificationFilter(String name) {
        this.name = name;
    }

    NotificationFilter() {
    }

    private NotificationFilter(NotificationFilter src) {
        this.name = src.getName();
        categoryFilter = null == src.getCategoryFilter() ? null : src.getCategoryFilter().clone();
    }

    public boolean isEnabled(NotificationImpl notification) {
        return categoryFilter == null ? true : categoryFilter.isEnabled(notification.getCategory().getName());
    }

    public String getName() {
        return name;
    }

    void setName(String newName) {
        this.name = newName;
    }

    CategoryFilter getCategoryFilter() {
        return categoryFilter;
    }

    void setCategoryFilter(CategoryFilter categoryFilter) {
        this.categoryFilter = categoryFilter;
    }

    @Override
    public Object clone() {
        return new NotificationFilter(this);
    }

    @Override
    public String toString() {
        return name;
    }

    void load(Preferences prefs, String prefix) throws BackingStoreException {
        name = prefs.get(prefix + "_name", "Filter"); //NOI18N //NOI18N
        if (prefs.getBoolean(prefix + "_types", false)) { //NOI18N
            categoryFilter = new CategoryFilter();
            categoryFilter.load(prefs, prefix + "_types"); //NOI18N
        } else {
            categoryFilter = null;
        }
    }

    void save(Preferences prefs, String prefix) throws BackingStoreException {
        prefs.put(prefix + "_name", name); //NOI18N

        if (null != categoryFilter) {
            prefs.putBoolean(prefix + "_types", true); //NOI18N
            categoryFilter.save(prefs, prefix + "_types"); //NOI18N
        } else {
            prefs.putBoolean(prefix + "_types", false); //NOI18N
        }
    }

    private static class EmptyNotificationFilter extends NotificationFilter {

        public EmptyNotificationFilter() {
            super(Util.getString("no-filter")); //NOI18N
        }

        @Override
        public boolean isEnabled(NotificationImpl notification) {
            return true;
        }
    }
}
