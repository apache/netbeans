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
package org.netbeans.modules.jumpto.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class CurrentSearch<T> {

    private final Callable<AbstractModelFilter<T>> filterFactory;

    //@GuardedBy("this")
    private String currentText;
    //@GuardedBy("this")
    private SearchType currentType;
    //@GuardedBy("this")
    private String currentScope;
    //@GuardedBy("this")
    private AbstractModelFilter<T> filter;

    private final Map</*@GuardedBy("this")*/Class<?>,Object> attrs;

    public CurrentSearch(@NonNull final Callable<AbstractModelFilter<T>> filterFactory) {
        Parameters.notNull("filterFactory", filterFactory); //NOI18N
        this.filterFactory = filterFactory;
        this.attrs = new HashMap<>();
        resetFilter();
    }

    public synchronized boolean isNarrowing(
            @NonNull final SearchType searchType,
            @NonNull final String searchText,
            @NullAllowed final String searchScope,
            final boolean correctCase) {
        if (currentType == null || currentText == null) {
            return false;
        }
        return Objects.equals(currentScope, searchScope) &&
            (correctCase || Utils.isCaseSensitive(currentType) == Utils.isCaseSensitive(searchType)) &&
            Utils.isNarrowing(
                currentType,
                searchType,
                currentText,
                searchText);
    }

    public synchronized void filter(
            @NonNull final SearchType searchType,
            @NonNull final String searchText,
            @NullAllowed Map<String,Object> options) {
        this.filter.configure(searchType, searchText, options);
    }

    @NonNull
    public synchronized Models.Filter<T> resetFilter() {
        this.currentType = null;
        this.currentText = null;
        try {
            this.filter = filterFactory.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this.filter;
    }

    public synchronized void searchCompleted(
            @NonNull final SearchType searchType,
            @NonNull final String searchText,
            @NullAllowed final String searchScope) {
        this.currentType = searchType;
        this.currentText = searchText;
        this.currentScope = searchScope;
    }

    @CheckForNull
    public synchronized <T> T setAttribute(@NonNull final Class<T> clz, @NullAllowed final T instance) {
        Parameters.notNull("cls", clz); //NOI18N
        if (instance == null) {
            return clz.cast(attrs.remove(clz));
        }
        return clz.cast(attrs.put(clz, instance));
    }

    @CheckForNull
    public synchronized <T> T getAttribute(@NonNull final Class<T> clz) {
        Parameters.notNull("clz", clz); //NOI18N
        return clz.cast(attrs.get(clz));
    }
}
