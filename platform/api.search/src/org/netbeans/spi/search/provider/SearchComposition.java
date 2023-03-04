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
package org.netbeans.spi.search.provider;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.search.provider.SearchListener;

/**
 * Object that encapsulates search state, settings and provides access to its
 * results displayer.
 *
 * @author jhavlin
 */
public abstract class SearchComposition<R> {

    /** Constructor for subclasses. */
    protected SearchComposition() {}

    /**
     * Start searching.
     */
    public abstract void start(@NonNull SearchListener listener);

    /**
     * Terminate searching.
     */
    public abstract void terminate();

    /**
     * Tells whether the search has been terminated.
     *
     * @return False if search has not been started yet, is still in progress,
     * or has finished normally. True if the search has been terminated.
     */
    public abstract boolean isTerminated();

    /**
     * Get results displayer. All invocations of this method on a this object
     * should return identical object.
     */
    public abstract @NonNull SearchResultsDisplayer<R> getSearchResultsDisplayer();
}
