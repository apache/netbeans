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
package org.netbeans.spi.search;

import java.util.List;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Provider of search scopes. It is an object registered in the default lookup.
 * If a combo box with search scopes is created, all search scope definion
 * providers are used to get a list of search scope definitions.
 *
 * @author jhavlin
 */
public abstract class SearchScopeDefinitionProvider {

    public abstract @NonNull List<SearchScopeDefinition> createSearchScopeDefinitions();
}
