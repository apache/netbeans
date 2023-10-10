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

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Item;
import org.openide.util.Lookup.Result;
import org.openide.util.lookup.Lookups;

/**
 * QuickSearchprovider for Options Dialog
 * @author Jan Becicka
 * @author Max Sauer
 */
public class QuickSearchProvider implements SearchProvider {

    private HashMap<String, Set<String>> path2keywords = new HashMap<String, Set<String>>();

    @Override
    public void evaluate(SearchRequest request, SearchResponse response) {
	readKeywordsFromNewAnnotation();
        for (Lookup.Item<OptionsCategory> entry : getODCategories()) {
            for (Map.Entry<String, Set<String>> kw : getKeywords(entry).entrySet()) {
                for (String keywords : kw.getValue()) {
		    for (String keyword : Arrays.asList(keywords.split(","))) {
			if (keyword.toLowerCase().indexOf(request.getText().toLowerCase()) > -1) {
			    String path = entry.getId().substring(entry.getId().indexOf("/") + 1);  // NOI18N
			    if (kw.getKey().contains("/")) {
				path = path + kw.getKey().substring(kw.getKey().indexOf("/")); //NOI18N
			    }
			    if (!response.addResult(new OpenOption(path), keyword)) {
				return;
			    }
			}
		    }
                }
            }
        }
    }

    private Map<String, Set<String>> getKeywords(Lookup.Item<OptionsCategory> it) {
	OptionsCategory category = it.getInstance();
	String categoryID = it.getId();

	Map<String, Set<String>> kws = new HashMap<String, Set<String>>();
	if (category instanceof OptionsCategoryImpl) {
	    Set<String> categoryKeywords = ((OptionsCategoryImpl) category).getKeywordsByCategory();
	    String categoryPath = categoryID.substring(categoryID.indexOf('/') + 1);
	    Map<String, Set<String>> mergedMap = new HashMap<String, Set<String>>();
	    mergedMap.put(categoryPath, mergeKeywords(categoryPath, categoryKeywords));
	    kws.putAll(mergedMap);
	}

	//sub-panels keywords
	Lookup lkp = Lookups.forPath(categoryID);
	Result<AdvancedOption> lkpResult = lkp.lookupResult(AdvancedOption.class);
	for (Item<AdvancedOption> item : lkpResult.allItems()) {
	    // don't lookup in subfolders
	    if (item.getId().substring(0, item.getId().lastIndexOf('/')).equals(categoryID)) {  // NOI18N
		AdvancedOption option = item.getInstance();
		if (option instanceof AdvancedOptionImpl) {
		    String subCategoryID = item.getId();
		    Set<String> subCategoryKeywords = ((AdvancedOptionImpl) option).getKeywordsByCategory();
		    String subCategoryPath = subCategoryID.substring(subCategoryID.indexOf('/') + 1);
		    Map<String, Set<String>> mergedMap = new HashMap<String, Set<String>>();
		    String optionPath = subCategoryPath.substring(0, subCategoryPath.indexOf("/") + 1).concat(((AdvancedOptionImpl) option).getDisplayName());
		    mergedMap.put(subCategoryPath, mergeKeywords(optionPath, subCategoryKeywords));
		    kws.putAll(mergedMap);
		}
	    }
	}
	return kws;
    }

    private void readKeywordsFromNewAnnotation() {
	FileObject keywordsFOs = FileUtil.getConfigRoot().getFileObject(CategoryModel.OD_LAYER_KEYWORDS_FOLDER_NAME);

        for(FileObject keywordsFO : keywordsFOs.getChildren()) {
	    String location = keywordsFO.getAttribute("location").toString(); //NOI18N
	    String tabTitle = keywordsFO.getAttribute("tabTitle").toString(); //NOI18N
	    Set<String> keywords = new HashSet<String>();
	    Enumeration<String> attributes = keywordsFO.getAttributes();
	    while (attributes.hasMoreElements()) {
		String attribute = attributes.nextElement();
		if (attribute.startsWith("keywords")) {
		    String word = keywordsFO.getAttribute(attribute).toString();
		    for (String s : word.split(",")) {
			keywords.add(s.trim());
		    }
		}
	    }
	    String path = location.concat("/").concat(tabTitle);
	    Set<String> keywordsFromPath = path2keywords.get(path);
	    if(keywordsFromPath != null) {
		for (String keyword : keywordsFromPath) {
		    if (!keywords.contains(keyword)) {
			keywords.add(keyword);
		    }
		}
	    }
	    path2keywords.put(path, keywords);
        }
    }

    private Set<String> mergeKeywords(String path, Set<String> initialKeywords) {
	Set<String> mergedKeywords = path2keywords.get(path);
	if (mergedKeywords == null) {
	    return initialKeywords;
	} else {
	    Set<String> toAdd = new HashSet<String>();
	    for (String keyword : initialKeywords) {
		for(String s : keyword.split(",")) {
		    if (!mergedKeywords.contains(s)) {
			toAdd.add(s);
		    }
		}
	    }
	    mergedKeywords.addAll(toAdd);
	    path2keywords.put(path, mergedKeywords);
	}
	return mergedKeywords;
    }

    private Iterable<? extends Lookup.Item<OptionsCategory>> getODCategories() {
        Lookup lookup = Lookups.forPath(CategoryModel.OD_LAYER_FOLDER_NAME);
        Lookup.Result<OptionsCategory> result = lookup.lookupResult(OptionsCategory.class);
        return result.allItems();
    }
    
    private class OpenOption implements Runnable {
        
        private String path;
        
        OpenOption(String path) {
            this.path = path;
        }

        public void run() {
            if(!OptionsDisplayer.getDefault().open(path)) {
                // If Options dialog already opened, select category. When
                // the dialog is not opened, it is selected automatically.
                OptionsDisplayerImpl.selectCategory(path);
            }
        }
    }
}
