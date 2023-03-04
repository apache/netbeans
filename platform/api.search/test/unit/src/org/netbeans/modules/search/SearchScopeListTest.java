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
package org.netbeans.modules.search;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.search.SearchScopeDefinition;
import org.netbeans.spi.search.SearchScopeDefinitionProvider;
import org.openide.util.Mutex;


/**
 *
 * @author jhavlin
 */
public class SearchScopeListTest extends NbTestCase {

    public SearchScopeListTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(CustomSearchScopeDefinitionProvider.class);
    }

    /**
     * Test for bug 204118 - [71cat] AssertionError at
     * org.netbeans.modules.search.SearchScopeRegistry.addChangeListener.
     */
    public void testAddChangeListener() throws InterruptedException,
            InvocationTargetException {

        final CustomChangeListener cl = new CustomChangeListener();
        final CustomChangeListener cl2 = new CustomChangeListener();

        final SearchScopeList ssl = new SearchScopeList();
        ssl.addChangeListener(cl);
        ssl.addChangeListener(cl2);

        Mutex.EVENT.writeAccess((Mutex.Action<Boolean>) () -> {
            for (SearchScopeDefinition ssd
                    : ssl.getSeachScopeDefinitions()) {
                if (ssd instanceof CustomSearchScope) {
                    ((CustomSearchScope) ssd).fireChangeEvent();
                }
            }
            return true;
        });

        assertEquals(3, cl.getCounter());
        assertEquals(3, cl.getCounter());
    }

    public void testSearchScopesNotifiedAboutChangesInEDT()
            throws InterruptedException {
        CustomSearchScope css = new CustomSearchScope(true, 1);
        SearchScopeList ssl = new SearchScopeList(css);
        final Semaphore s = new Semaphore(0);
        final AtomicBoolean notifiedInEDT = new AtomicBoolean(false);
        ssl.addChangeListener((ChangeEvent e) -> {
            notifiedInEDT.set(EventQueue.isDispatchThread());
            s.release();
        });
        css.fireChangeEvent();
        boolean acqrd = s.tryAcquire(10, TimeUnit.SECONDS);
        assertTrue("Should be notified in EDT", acqrd && notifiedInEDT.get());
    }

    /**
     * Change listener implementation for the tests above.
     */
    private class CustomChangeListener implements ChangeListener {

        int counter = 0;

        @Override
        public void stateChanged(ChangeEvent e) {
            counter ++;
        }

        public int getCounter() {
            return counter;
        }
    }

    public void testSorting() {
        SearchScopeList ssl = new SearchScopeList();
        List<SearchScopeDefinition> defs = ssl.getSeachScopeDefinitions();
        assertEquals(1, defs.get(0).getPriority());
        assertEquals(2, defs.get(1).getPriority());
        assertEquals(3, defs.get(2).getPriority());
        ssl.clean();
    }

    /**
     * Search scope implementation for the tests above.
     */
    public static class CustomSearchScope extends SearchScopeDefinition {

        private boolean applicable = true;
        private int priority;
        private Set<ChangeListener> listeners = new HashSet<>();

        public CustomSearchScope(boolean applicable, int priority) {
            this.applicable = applicable;
            this.priority = priority;
        }

        @Override
        public String getTypeId() {
            return "TEST";
        }

        @Override
        public String getDisplayName() {
            return "Test Search Scope";
        }

        @Override
        public synchronized boolean isApplicable() {
            return applicable;
        }

        @Override
        public SearchInfo getSearchInfo() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setApplicable(boolean applicable) {

            Set<ChangeListener> listenersCopy = null;

            synchronized (this) {
                boolean oldVal = this.applicable;
                this.applicable = applicable;
                if (applicable != oldVal) {

                    listenersCopy = new HashSet<>(listeners);
                }
            }
            for (ChangeListener l : listenersCopy) {
                l.stateChanged(new ChangeEvent(this));
            }
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public void clean() {
        }

        public void fireChangeEvent() {
            notifyListeners();
        }
    }

    public static class CustomSearchScopeDefinitionProvider
            extends SearchScopeDefinitionProvider {

        @Override
        public List<SearchScopeDefinition> createSearchScopeDefinitions() {
            List<SearchScopeDefinition> list =
                    new LinkedList<>();
            list.add(new CustomSearchScope(true, 2));
            list.add(new CustomSearchScope(true, 1));
            list.add(new CustomSearchScope(false, 3));
            return list;
        }
    }
}
