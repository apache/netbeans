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
package org.netbeans.api.search.provider.impl;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.search.provider.SearchFilter;
import org.netbeans.spi.search.SearchFilterDefinition;

/**
 *
 * @author jhavlin
 */
public final class DefinitionUtils {

    private DefinitionUtils() {
        // hide constructor
    }

    /**
     * Create a list of SearchFilters from a list of SearchFilterDefinitions.
     */
    public static List<SearchFilter> createSearchFilterList(
            SearchFilterDefinition[] definitions) {

        List<SearchFilter> list = new ArrayList<>(
                definitions.length);

        for (SearchFilterDefinition def : definitions) {
            list.add(new DelegatingSearchFilter(def));
        }
        return list;
    }
}
