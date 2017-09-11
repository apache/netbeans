/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
                categories = new ArrayList<Category>(Category.getDefaultCategories());
                categories.addAll(lookupRes.allInstances());
                name2category = new HashMap<String, Category>(categories.size());
                for (Category c : categories) {
                    name2category.put(c.getName(), c);
                    c.setIndex(index++);
                }
            }
        }
    }

    private Lookup.Result<Category> initLookup() {
        Lookup lkp = Lookups.forPath(CATEGORY_LIST_PATH);
        Lookup.Template<Category> template = new Lookup.Template<Category>(Category.class);
        Lookup.Result<Category> res = lkp.lookup(template);
        return res;
    }
}
