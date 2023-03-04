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

package org.netbeans.modules.quicksearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Dafe Simonek
 */
public final class ProviderModel {

    /** folder in layer file system where provider of fast access content are searched for */
    private static final String SEARCH_PROVIDERS_FOLDER = "/QuickSearch"; //NOI18N
    private static final String COMMAND_PREFIX = "command"; //NOI18N

    private static ProviderModel instance;

    private List<Category> categories;
    
    private LinkedHashSet<String> knownCommands;

    private ProviderModel () {
    }

    public static ProviderModel getInstance () {
        if (instance == null) {
            instance = new ProviderModel();
        }
        return instance;
    }

    /**
     * Get the value of categories
     *
     * @return the value of categories
     */
    public List<Category> getCategories() {
        if (categories == null) {
            categories = loadCategories();
        }
        return this.categories;
    }
    
    public boolean isKnownCommand(String command) {
        if (knownCommands == null) {
            knownCommands = new LinkedHashSet<String>();
            for (Category cat : getCategories()) {
                knownCommands.add(cat.getCommandPrefix());
            }
        }
        return knownCommands.contains(command);
    }
    
    public static class Category {

        private FileObject fo;

        private String displayName, commandPrefix;
        
        private List<SearchProvider> providers;

        public Category(FileObject fo, String displayName, String commandPrefix) {
            this.fo = fo;
            this.displayName = displayName;
            this.commandPrefix = commandPrefix;
        }
        
        public List<SearchProvider> getProviders () {
            if (providers == null) {
                Collection<? extends SearchProvider> catProviders =
                        Lookups.forPath(fo.getPath()).lookupAll(SearchProvider.class);
                providers = new ArrayList<SearchProvider>(catProviders);
            }

            return providers;
        }
        
        public String getName() {
            return fo.getNameExt();
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getCommandPrefix() {
            return commandPrefix;
        }
        
    } // end of Category

    private static List<Category> loadCategories () {
        FileObject[] categoryFOs = FileUtil.getConfigFile(SEARCH_PROVIDERS_FOLDER).getChildren();

        // respect ordering defined in layers
        List<FileObject> sortedCats = FileUtil.getOrder(Arrays.asList(categoryFOs), false);

        List<ProviderModel.Category> categories = new ArrayList<ProviderModel.Category>(sortedCats.size());

        for (FileObject curFO : sortedCats) {
            String displayName = null;
            try {
                displayName = curFO.getFileSystem().getDecorator().annotateName(
                        curFO.getNameExt(), Collections.singleton(curFO));
            } catch (FileStateInvalidException ex) {
                Logger.getLogger(ProviderModel.class.getName()).log(Level.WARNING,
                        "Obtaining display name for " + curFO + " failed.", ex);
            }

            String commandPrefix = null;
            Object cpAttr = curFO.getAttribute(COMMAND_PREFIX);
            if (cpAttr instanceof String) {
                commandPrefix = (String)cpAttr;
            }

            categories.add(new Category(curFO, displayName, commandPrefix));
        }

        return categories;
    }

}
