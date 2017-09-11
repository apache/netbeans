/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
            new ArrayList<ChangeListener>(1);
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
                new ArrayList<SearchScopeDefinition>(6);
        Collection<? extends SearchScopeDefinitionProvider> providers;
        providers = Lookup.getDefault().lookupAll(
                SearchScopeDefinitionProvider.class);
        for (SearchScopeDefinitionProvider provider : providers) {
            scopeList.addAll(provider.createSearchScopeDefinitions());
        }
        scopeList.addAll(Arrays.asList(extraSearchScopes));
        Collections.sort(scopeList, new ScopePriorityComparator());
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
            return new ArrayList<SearchScopeDefinition>(scopes); //#220505
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
            Mutex.EVENT.writeAccess(new Runnable() {
                @Override
                public void run() {
                    notifyDelegates(e);
                }
            });
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
