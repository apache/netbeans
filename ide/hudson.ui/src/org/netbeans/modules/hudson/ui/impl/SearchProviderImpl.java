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

package org.netbeans.modules.hudson.ui.impl;

import java.util.Locale;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.netbeans.modules.hudson.ui.api.UI;
import org.netbeans.modules.hudson.api.Utilities;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.util.NbBundle.Messages;

@Messages("quicksearch=Jenkins") // QuickSearch/Jenkins#displayName
public class SearchProviderImpl implements SearchProvider {

    @Override public void evaluate(SearchRequest request, SearchResponse response) {
        String text = request.getText();
        if (text == null) {
            return;
        }
        if (!Utilities.isHudsonSupportActive()) {
            return;
        }
        work(text, response);
    }

    @Messages({"# {0} - server label", "# {1} - job name", "search_response=Hudson job {1} on {0}"})
    private void work(String text, SearchResponse response) {
        for (final HudsonInstance instance : HudsonManager.getAllInstances()) {
            for (HudsonJob job : instance.getJobs()) {
                final String name = job.getName();
                // XXX could also search for text in instance name, and/or Maven modules
                if (name.toLowerCase(Locale.ENGLISH).contains(text.toLowerCase(Locale.ENGLISH))) {
                    if (!response.addResult(new Runnable() {
                        @Override public void run() {
                            UI.selectNode(instance.getUrl(), name);
                        }
                    }, Bundle.search_response(instance.getName(), name))) {
                        return;
                    }
                }
            }
        }
    }

}
