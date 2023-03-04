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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.awt.NotificationDisplayer.Category;

/**
 *
 * @author S. Aubrecht
 * @author jpeska
 */
class CategoryFilter {

    private final Set<String> enabledCategories = new HashSet<>();

    public CategoryFilter() {
        addDefaultTypes();
    }

    private CategoryFilter(CategoryFilter src) {
        this.enabledCategories.addAll(src.enabledCategories);
    }

    public boolean isEnabled(String category) {
        return enabledCategories.contains(category);
    }

    public void setEnabled(String category, boolean enabled) {
        if (enabled) {
            enabledCategories.add(category);
        } else {
            enabledCategories.remove(category);
        }
    }

    @Override
    public CategoryFilter clone() {
        return new CategoryFilter(this);
    }

    void clear() {
        enabledCategories.clear();
    }

    void load(Preferences prefs, String prefix) throws BackingStoreException {
        enabledCategories.clear();
        String enabled = prefs.get(prefix + "_enabled", ""); //NOI18N //NOI18N
        if (enabled.trim().length() > 0) {
            StringTokenizer tokenizer = new StringTokenizer(enabled, "\n"); //NOI18N
            while (tokenizer.hasMoreTokens()) {
                enabledCategories.add(tokenizer.nextToken());
            }
        } else {
            addDefaultTypes();
        }
    }

    void save(Preferences prefs, String prefix) throws BackingStoreException {
        StringBuilder buffer = new StringBuilder();
        for (Iterator<String> type = enabledCategories.iterator(); type.hasNext();) {
            buffer.append(type.next());
            if (type.hasNext()) {
                buffer.append("\n"); //NOI18N
            }
        }
        prefs.put(prefix + "_enabled", buffer.toString()); //NOI18N
    }

    private void addDefaultTypes() {
        for (Category category : Category.getCategories()) {
            enabledCategories.add(category.getName());
        }
    }
}
