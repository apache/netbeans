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
package org.netbeans.modules.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.search.SearchScopeDefinition;
import org.netbeans.spi.search.SearchScopeDefinitionProvider;
import org.openide.util.Lookup;
import org.openide.util.Mutex;

/**
 * List of search scopes currently (at the time of creation of this object)
 * registered in the default lookup.
 *
 * @author jhavlin
 */
public class SearchScopeList {

    private final List<SearchScopeDefinition> scopes;
    private final List<ChangeListener> changeListeners =
            new ArrayList<>(1);
    private ProxyChangeListener proxyChangeListener = new ProxyChangeListener();

    /**
     * Create list of search scopes. Use providers currently registered int the
     * lookup.
     */
    public SearchScopeList(SearchScopeDefinition... extraSearchScopes) {
        this.scopes = createScopeList(extraSearchScopes);
    }

    /**
     * Clean all resources if search scopes are no longer needed.
     */
    public void clean() {
        synchronized (scopes) {
            for (SearchScopeDefinition ssd : scopes) {
                ssd.removeChangeListener(proxyChangeListener);
                ssd.clean();
            }
            scopes.clear();
        }
        synchronized (changeListeners) {
            changeListeners.clear();
        }
    }

    /**
     * Add listener that is notified when any of search scopes changes.
     */
    public void addChangeListener(ChangeListener changeListener) {
        synchronized (changeListeners) {
            this.changeListeners.add(changeListener);
        }
    }

    /**
     * Remove registered change listener.
     *
     */
    public void removeChangeListener(ChangeListener changeListener) {
        synchronized (changeListeners) {
            this.changeListeners.remove(changeListener);
        }
    }

    private List<SearchScopeDefinition> createScopeList(
            SearchScopeDefinition... extraSearchScopes) {

        List<SearchScopeDefinition> scopeList =
                new ArrayList<>(6);
        Collection<? extends SearchScopeDefinitionProvider> providers;
        providers = Lookup.getDefault().lookupAll(
                SearchScopeDefinitionProvider.class);
        for (SearchScopeDefinitionProvider provider : providers) {
            scopeList.addAll(provider.createSearchScopeDefinitions());
        }
        scopeList.addAll(Arrays.asList(extraSearchScopes));
        scopeList.sort(new ScopePriorityComparator());
        for (SearchScopeDefinition scope : scopeList) {
            scope.addChangeListener(proxyChangeListener);
        }
        return scopeList;
    }

    /**
     * Return list of contained search scope definitions, sorted by priority.
     */
    public List<SearchScopeDefinition> getSeachScopeDefinitions() {
        synchronized (scopes) {
            return new ArrayList<>(scopes); //#220505
        }
    }

    /**
     * Comparator for sorting search scopes by priority.
     */
    private class ScopePriorityComparator
            implements Comparator<SearchScopeDefinition> {

        @Override
        public int compare(SearchScopeDefinition o1, SearchScopeDefinition o2) {
            return o1.getPriority() - o2.getPriority();
        }
    }

    /**
     * Change listener that is added to each scope in the list.
     */
    private class ProxyChangeListener implements ChangeListener {

        @Override
        public void stateChanged(final ChangeEvent e) {
            Mutex.EVENT.writeAccess(() -> notifyDelegates(e));
        }

        private void notifyDelegates(ChangeEvent e) {
            synchronized (changeListeners) {
                for (ChangeListener changeListener : changeListeners) {
                    changeListener.stateChanged(e);
                }
            }
        }
    }
}
