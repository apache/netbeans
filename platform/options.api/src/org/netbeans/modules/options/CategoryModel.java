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

package org.netbeans.modules.options;

import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * @author Radek Matous
 */
public final class CategoryModel implements LookupListener {
    private static final RequestProcessor RP = new RequestProcessor("org.netbeans.modules.options.CategoryModel");  //NOI18N
    private static Reference<CategoryModel> INSTANCE = new WeakReference<CategoryModel>(null);
    private static String currentCategoryID = null;
    private String highlitedCategoryID = null;
    private boolean categoriesValid = true;
    private final Map<String, CategoryModel.Category> id2Category =
            Collections.synchronizedMap(new LinkedHashMap<String, CategoryModel.Category>());
    private MasterLookup masterLookup;
    static final String OD_LAYER_FOLDER_NAME = "OptionsDialog"; // NOI18N
    static final String OD_LAYER_KEYWORDS_FOLDER_NAME = OD_LAYER_FOLDER_NAME.concat("/Keywords"); // NOI18N
    private Result<OptionsCategory> result;
    
    Set<Map.Entry<String, CategoryModel.Category>> getCategories() {
        return id2Category.entrySet();
    }

    private final RequestProcessor.Task masterLookupTask = RP.create(new Runnable() {
        public void run() {
            String[] categoryIDs = getCategoryIDs();
            List<Lookup> all = new ArrayList<Lookup>();
            for (int i = 0; i < categoryIDs.length; i++) {
                Category item = getCategory(categoryIDs[i]);
                Lookup lkp = item.getLookup();
                assert lkp != null;
                if (lkp != Lookup.EMPTY) {
                    all.add(lkp);
                }
            }
            getMasterLookup().setLookups(all);
            addLookupListener();
        }
    },true);
    private final RequestProcessor.Task categoryTask = RP.create(new Runnable() {
        public void run() {
            Map<String, OptionsCategory> all = loadOptionsCategories();
            Map<String, CategoryModel.Category> temp = new LinkedHashMap<String, CategoryModel.Category>();
            for (Iterator<Map.Entry<String, OptionsCategory>> it = all.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, OptionsCategory> entry = it.next();
                OptionsCategory oc = entry.getValue();
                String id = entry.getKey();
                Category cat = new Category(id, oc);
                temp.put(cat.getID(), cat);
            }
            id2Category.clear();
            id2Category.putAll(temp);
            masterLookupTask.schedule(0);
        }
    },true);

    private CategoryModel() {
	if (INSTANCE.get() == null && categoryTask.isFinished()) {
	    categoryTask.schedule(0);
	}
    }

    public static synchronized CategoryModel getInstance() {
        CategoryModel retval = INSTANCE.get();
        if (retval == null) {
            retval = new CategoryModel();
            INSTANCE = new WeakReference<CategoryModel>(retval);
        }
        return retval;
    }

    boolean needsReinit() {
        synchronized(CategoryModel.class) {
            return !categoriesValid;
        }
    }

    boolean isInitialized() {
        return categoryTask.isFinished();
    }

    boolean isLookupInitialized() {
        return masterLookupTask.isFinished();
    }


    void waitForInitialization() {
        categoryTask.waitFinished();
    }

    public String getCurrentCategoryID() {
        return verifyCategoryID(currentCategoryID);
    }

    public void setCurrentCategoryID(String categoryID) {
        currentCategoryID = verifyCategoryID(categoryID);
    }

    
    String getHighlitedCategoryID() {
        return verifyCategoryID(highlitedCategoryID);
    }

    private String verifyCategoryID(String categoryID) {
        String retval = findCurrentCategoryID(categoryID) != -1 ? categoryID : null;
        if (retval == null) {
            String[] categoryIDs = getCategoryIDs();
            if (categoryIDs.length > 0) {
                retval = categoryID = categoryIDs[0];
            }
        }
        return retval;
    }

    private int findCurrentCategoryID(String categoryID) {
        return categoryID == null ? -1 : Arrays.asList(getCategoryIDs()).indexOf(categoryID);
    }

    public String[] getCategoryIDs() {
        categoryTask.waitFinished();
        Set<String> keys = id2Category.keySet();
        return keys.toArray(new String[0]);
    }

    Category getCurrent() {
        String categoryID =  getCurrentCategoryID();
        return (categoryID == null) ? null : getCategory(categoryID);
    }

    void setCurrent(Category item) {
        item.setCurrent();
    }

    void setHighlited(Category item,boolean highlited) {
        item.setHighlited(highlited);
    }

    HelpCtx getHelpCtx() {
        final CategoryModel.Category category = getCurrent();
        return (category == null) ? null : category.getHelpCtx();
    }

    void update(PropertyChangeListener l, boolean force) {
        String[] categoryIDs = getCategoryIDs();
        for (int i = 0; i < categoryIDs.length; i++) {
            CategoryModel.Category item = getCategory(categoryIDs[i]);
            item.update(l, force);
        }
    }

    void save() {
        String[] categoryIDs = getCategoryIDs();
        for (int i = 0; i < categoryIDs.length; i++) {
            CategoryModel.Category item = getCategory(categoryIDs[i]);
            item.applyChanges();
        }
    }

    void cancel() {
        String[] categoryIDs = getCategoryIDs();
        for (int i = 0; i < categoryIDs.length; i++) {
            CategoryModel.Category item = getCategory(categoryIDs[i]);
            item.cancel();
        }
    }

    boolean dataValid() {
        boolean retval = true;
        String[] categoryIDs = getCategoryIDs();
        for (int i = 0; retval && i < categoryIDs.length; i++) {
            CategoryModel.Category item = getCategory(categoryIDs[i]);
            retval = item.isValid();
        }
        return retval;
    }

    boolean isChanged() {
        boolean retval = false;
        String[] categoryIDs = getCategoryIDs();
        for (int i = 0; !retval && i < categoryIDs.length; i++) {
            CategoryModel.Category item = getCategory(categoryIDs[i]);
            retval = item.isChanged();
        }
        return retval;
    }


    Category getNextCategory() {
        int idx =  findCurrentCategoryID(getCurrentCategoryID());
        String[] categoryIDs = getCategoryIDs();
        String nextId = "";
        if (idx >= 0 && idx < categoryIDs.length && categoryIDs.length > 0) {
            if (idx+1 < categoryIDs.length) {
                nextId = categoryIDs[idx+1];
            }  else {
                nextId = categoryIDs[0];
            }
        } else {
            nextId = null;
        }
        return nextId != null ? getCategory(nextId) : null;
    }

    Category getPreviousCategory() {
        int idx =  findCurrentCategoryID(getCurrentCategoryID());
        String[] categoryIDs = getCategoryIDs();
        String previousId = "";
        if (idx >= 0 && idx < categoryIDs.length && categoryIDs.length > 0) {
            if (idx-1 >= 0) {
                previousId = categoryIDs[idx-1];
            }  else {
                previousId = categoryIDs[categoryIDs.length-1];
            }
        } else {
            previousId = null;
        }
        return previousId != null ? getCategory(previousId) : null;
    }


    Category getCategory(String categoryID) {
        categoryTask.waitFinished();
        return id2Category.get(categoryID);
    }

    private MasterLookup getMasterLookup() {
        if (masterLookup == null) {
            masterLookup = new MasterLookup();
        }
        return masterLookup;
    }

    private Map<String, OptionsCategory> loadOptionsCategories() {
        Lookup lookup = Lookups.forPath(OD_LAYER_FOLDER_NAME);
        if (result != null) {
            result.removeLookupListener(this);
        }
        result = lookup.lookup(new Lookup.Template<OptionsCategory>(OptionsCategory.class));
        Map<String, OptionsCategory> m = new LinkedHashMap<String, OptionsCategory>();
        for (Iterator<? extends Lookup.Item<OptionsCategory>> it = result.allItems().iterator(); it.hasNext();) {
            Lookup.Item<OptionsCategory> item = it.next();
            m.put(item.getId().substring(OD_LAYER_FOLDER_NAME.length() + 1), item.getInstance());
        }
        return Collections.unmodifiableMap(m);
    }
    
    private void addLookupListener() {
        result.addLookupListener(this);
    }

    public void resultChanged(LookupEvent ev) {
        synchronized(CategoryModel.class) {
            categoriesValid = false;
            OptionsDisplayerImpl.lookupListener.resultChanged(ev);
            INSTANCE = new WeakReference<CategoryModel>(null);
        }
    }

    final class Category  {
        private OptionsCategory category;
        private OptionsPanelController controller;
        private final Set<PropertyChangeListener> controllerListeners = new HashSet<PropertyChangeListener>(1);
        private boolean isUpdated;
        private JComponent component;
        private Lookup lookup;
        private final String id;

        private Category(final String id, final OptionsCategory category) {
            this.category = category;
            this.id = id;
        }

        boolean isCurrent() {
            return getID().equals(getCurrentCategoryID());
        }

        boolean isHighlited() {
            return getID().equals(getHighlitedCategoryID());
        }

        private void setCurrent() {
            setCurrentCategoryID(getID());
        }

        public void setCurrentSubcategory(String subpath) {
            OptionsPanelControllerAccessor.getDefault().setCurrentSubcategory(create(), subpath);
        }

        private void setHighlited(boolean highlited) {
            if (highlited) {
                highlitedCategoryID = getID();
            } else {
                highlitedCategoryID = currentCategoryID;
            }
        }

        public Icon getIcon() {
            return category.getIcon();
        }

        //whatever ID representing category (dataObject name,category name, just mnemonic, ...)
        //for impl. #74855: Add an API for opening the Options dialog
        public  String getID() {
            return id;
        }

        public String getCategoryName() {
            return category.getCategoryName();
        }

        public void handleSuccessfulSearchInController(String searchText, List<String> matchedKeywords) {
	    create().handleSuccessfulSearch(searchText, matchedKeywords);
        }

        private synchronized OptionsPanelController create() {
            if (controller == null) {
                controller = category.create();
            }
            return controller;
        }

        final void update(PropertyChangeListener l, boolean forceUpdate) {
            if ((!isUpdated && !forceUpdate) || (isUpdated && forceUpdate)) {
                isUpdated = true;
                getComponent();
                create().update();
                if (l != null && !controllerListeners.contains(l)) {
                    create().addPropertyChangeListener(l);
                    controllerListeners.add(l);
                }
            }
        }

        private void applyChanges() {
            if (isUpdated) {
                create().applyChanges();
            }
            isUpdated = false;
        }

        private void cancel() {
            if (isUpdated) {
                create().cancel();
            }
            isUpdated = false;
        }

        private boolean isValid() {
            boolean retval = true;
            if (isUpdated) {
                retval = create().isValid();
            }
            return retval;
        }

        private boolean isChanged() {
            boolean retval = false;
            if (isUpdated) {
                retval = create().isChanged();
            }
            return retval;
        }

        public JComponent getComponent() {
            if (component == null) {
                component = create().getComponent(getMasterLookup());
            }
            return component;
        }

        private HelpCtx getHelpCtx() {
            return create().getHelpCtx();
        }


        private Lookup getLookup() {
            if (lookup == null) {
                lookup = create().getLookup();
            }
            return lookup;
        }
    }

    private class MasterLookup extends ProxyLookup {
        private void setLookups(List<Lookup> lookups) {
            setLookups(lookups.toArray(new Lookup[0]));
        }
        @Override
        protected void beforeLookup(Lookup.Template template) {
            super.beforeLookup(template);
            masterLookupTask.waitFinished();
        }
    }
}
