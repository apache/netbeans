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
package org.netbeans.modules.jumpto.common;

import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.spi.jumpto.support.NameMatcher;
import org.netbeans.spi.jumpto.support.NameMatcherFactory;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Tomas Zezula
 */
public abstract class AbstractModelFilter<T> implements Models.Filter<T> {

    public static final String OPTION_CLEAR = "_clear_content"; //NOI18N

    private final ChangeSupport changeSupport;
    //GuardedBy("this")
    private NameMatcher matcher;
    //GuardedBy("this")
    private String searchText;

    protected AbstractModelFilter() {
        this.changeSupport = new ChangeSupport(this);
    }

    @Override
    public final boolean accept(@NonNull final T item) {
        boolean res = true;
        final NameMatcher m = getMatcher();
        if (m != null) {
            final String itemValue = getItemValue(item);
            res = m.accept(itemValue);
            if (res) {
                update(item);
            }
        }
        return res;
    }

    public final void configure(
            @NullAllowed final SearchType searchType,
            @NullAllowed final String searchText,
            @NullAllowed final Map<String, Object> options) {
        synchronized (this) {
            this.searchText = searchText;
            if (options != null && options.get(OPTION_CLEAR) == Boolean.TRUE) {
                this.matcher = NameMatcher.NONE;
            } else {
                this.matcher = createNameMatcher(searchType, searchText, options);
            }
        }
        changeSupport.fireChange();
    }

    @Override
    public final void addChangeListener(ChangeListener listener) {
        this.changeSupport.addChangeListener(listener);
    }

    @Override
    public final void remmoveChangeListener(ChangeListener listener) {
        this.changeSupport.removeChangeListener(listener);
    }

    @NonNull
    protected abstract String getItemValue(@NonNull final T item);

    protected void update(@NonNull final T item) {
    }

    @CheckForNull
    protected final synchronized String getSearchText() {
        return searchText;
    }

    @CheckForNull
    private synchronized NameMatcher getMatcher() {
        return matcher;
    }

    @CheckForNull
    private static NameMatcher createNameMatcher (
            @NullAllowed final SearchType searchType,
            @NullAllowed final String searchText,
            @NullAllowed final Map<String,Object> options) {
        return (searchText != null && searchType != null) ?
            NameMatcherFactory.createNameMatcher(
                    searchText,
                    searchType,
                    options == null ? Map.of() : options) :
            null;
    }
}
