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
package org.openide.awt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import org.openide.awt.NotificationDisplayer.Category;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author jpeska
 */
class NotificationCategoryFactory {

    static final String ATTR_CATEGORY_NAME = "categoryName"; //NOI18N
    static final String ATTR_BUNDLE_NAME = "localizingBundle"; //NOI18N
    static final String ATTR_DISPLAY_NAME_KEY = "diplayNameKey"; //NOI18N
    static final String ATTR_DESCRIPTION_KEY = "descriptionKey"; //NOI18N
    private static final String CATEGORY_LIST_PATH = "Notification/Category"; //NOI18N
    private static NotificationCategoryFactory theInstance;
    private Lookup.Result<Category> lookupRes;
    private Map<String, Category> name2category;
    private List<Category> categories;

    private NotificationCategoryFactory() {
    }

    static Category create(Map<String, String> attrs) {
        String categoryName = attrs.get(ATTR_CATEGORY_NAME);
        String bundleName = attrs.get(ATTR_BUNDLE_NAME);
        String displayNameKey = attrs.get(ATTR_DISPLAY_NAME_KEY);
        String descriptionKey = attrs.get(ATTR_DESCRIPTION_KEY);
        return create(categoryName, bundleName, displayNameKey, descriptionKey);
    }

    static Category create(String categoryName, String bundleName, String displayNameKey, String descriptionKey) {
        ResourceBundle bundle = NbBundle.getBundle(bundleName);
        String displayName = bundle.getString(displayNameKey);
        String description = bundle.getString(descriptionKey);
        return new Category(categoryName, displayName, description);
    }

    /**
     * @return The one and only instance of this class.
     */
    public static NotificationCategoryFactory getInstance() {
        if (null == theInstance) {
            theInstance = new NotificationCategoryFactory();
        }
        return theInstance;
    }

    Category getCategory(String categoryName) {
        assert null != categoryName;
        synchronized (this) {
            initCategories();
            return name2category.get(categoryName);
        }
    }

    List<Category> getCategories() {
        synchronized (this) {
            initCategories();
            return categories;
        }
    }

    private void initCategories() {
        synchronized (this) {
            if (null == name2category) {
                if (null == lookupRes) {
                    lookupRes = initLookup();
                    lookupRes.addLookupListener(new LookupListener() {
                        @Override
                        public void resultChanged(LookupEvent ev) {
                            synchronized (NotificationCategoryFactory.this) {
                                name2category = null;
                                categories = null;
                            }
                        }
                    });
                }
                int index = 0;
                categories = new ArrayList<>(Category.getDefaultCategories());
                categories.addAll(lookupRes.allInstances());
                name2category = new HashMap<>(categories.size());
                for (Category c : categories) {
                    name2category.put(c.getName(), c);
                    c.setIndex(index++);
                }
            }
        }
    }

    private Lookup.Result<Category> initLookup() {
        Lookup lkp = Lookups.forPath(CATEGORY_LIST_PATH);
        Lookup.Template<Category> template = new Lookup.Template<>(Category.class);
        Lookup.Result<Category> res = lkp.lookup(template);
        return res;
    }
}
