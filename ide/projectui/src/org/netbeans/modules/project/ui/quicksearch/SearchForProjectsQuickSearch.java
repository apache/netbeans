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
package org.netbeans.modules.project.ui.quicksearch;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.project.ui.ProjectUtilities;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;

/**
 * QuickSearchProvider for project names. Opens and expands the selected project
 * in the project view.
 *
 * @author markiewb
 */
public class SearchForProjectsQuickSearch implements SearchProvider {

    @Override
    public void evaluate(SearchRequest request, SearchResponse response) {
        final String searchText = request.getText().toLowerCase();

        for (final Project project : OpenProjects.getDefault().getOpenProjects()) {
            final ProjectInformation info = ProjectUtils.getInformation(project);
            if (info == null || info.getDisplayName() == null) {
                continue;
            }
            final String projectName = info.getDisplayName();
            final String projectNameLower = projectName.toLowerCase();

            if (projectNameLower.contains(searchText)) {
                final boolean result = response.addResult(new Runnable() {
                    @Override
                    public void run() {
                        //see http://forums.netbeans.org/post-140855.html#140855
                        ProjectUtilities.selectAndExpandProject(project);
                        ProjectUtilities.makeProjectTabVisible();
                    }
                }, projectName);
                if (!result) {
                    break;
                }
            }
        }
    }
}
