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


package org.netbeans.modules.quicksearch.recent;

import org.netbeans.modules.quicksearch.ResultsModel.ItemResult;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.util.NbBundle;

    
/**
 * Recent searches
 * @author  Jan Becicka
 */
public class RecentProvider implements SearchProvider {

    @Override
    public void evaluate(SearchRequest request, SearchResponse response) {
        boolean atLeastOne = false;
        boolean limitReached = false;
        for (ItemResult itemR : RecentSearches.getDefault().getSearches()) {
            if (itemR.getDisplayName().toLowerCase().indexOf(request.getText().toLowerCase()) != -1) {
                if (!response.addResult(itemR.getAction(), itemR.getDisplayName(),
                        itemR.getDisplayHint(), itemR.getShortcut())) {
                    limitReached = true;
                    break;
                } else {
                    atLeastOne = true;
                }
            }
        }
        if (atLeastOne && !limitReached && request.getText().isEmpty()) {
            addClearAction(response);
        }
    }

    @NbBundle.Messages("RecentSearches.clear=(Clear recent searches)")
    private void addClearAction(SearchResponse response) {
        boolean add = response.addResult(new Runnable() {
            @Override
            public void run() {
                RecentSearches.getDefault().clear();
            }
        }, "<html><i>" + Bundle.RecentSearches_clear() + "</i></html>");//NOI18N
    }
}
