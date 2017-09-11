/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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

        Mutex.EVENT.writeAccess(new Mutex.Action<Boolean>() {
            @Override
            public Boolean run() {
                for (SearchScopeDefinition ssd
                        : ssl.getSeachScopeDefinitions()) {
                    if (ssd instanceof CustomSearchScope) {
                        ((CustomSearchScope) ssd).fireChangeEvent();
                    }
                }
                return true;
            }
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
        ssl.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                notifiedInEDT.set(EventQueue.isDispatchThread());
                s.release();
            }
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
        private Set<ChangeListener> listeners = new HashSet<ChangeListener>();

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

                    listenersCopy = new HashSet<ChangeListener>(listeners);
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
                    new LinkedList<SearchScopeDefinition>();
            list.add(new CustomSearchScope(true, 2));
            list.add(new CustomSearchScope(true, 1));
            list.add(new CustomSearchScope(false, 3));
            return list;
        }
    }
}
