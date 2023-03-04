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
package org.netbeans.modules.search.project;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.spi.search.SearchScopeDefinition;
import org.netbeans.spi.search.SearchScopeDefinitionProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author jhavlin
 */
@ServiceProvider(service=SearchScopeDefinitionProvider.class)
public class ProjectSearchScopeDefinitionProvider
        extends SearchScopeDefinitionProvider {

    @Override
    public List<SearchScopeDefinition> createSearchScopeDefinitions() {
        List<SearchScopeDefinition> list =
                new ArrayList<SearchScopeDefinition>(2);
        list.add(new SearchScopeCurrentProject());
        list.add(new SearchScopeMainProject());
        list.add(new SearchScopeOpenProjects());
        list.add(new SearchScopeNodeSelection());
        return list;
    }
}
