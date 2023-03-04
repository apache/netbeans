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

package org.netbeans.modules.bugtracking.ui.query;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.TestQuery;
import org.netbeans.modules.bugtracking.TestRepository;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.spi.*;
import org.netbeans.modules.bugtracking.spi.QueryController.QueryMode;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author tomas
 */
public class QTCTestHidden extends NbTestCase {

    public QTCTestHidden(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

//    XXX failing on hudson SHOULD BE FIXED
//    public void testOpenNewQuery() throws Throwable {
//        MyRepository repo = MyConnector.repo;
//
//        LogHandler openedHandler = new LogHandler("QueryAction.openQuery finnish", LogHandler.Compare.STARTS_WITH);
//        LogHandler savedQueriesHandler = new LogHandler("saved queries.", LogHandler.Compare.ENDS_WITH);
//
//        repo.queries.add(new MyQuery(repo));
//
//        QueryAction.openQuery(null, repo, true);
//        openedHandler.waitUntilDone();
//
//        QueryTopComponent qtc1 = getQueryTC();
//        JComboBox combo = (JComboBox) getField(qtc1, "repositoryComboBox");
//        assertFalse(combo.isEnabled());
//        assertTrue(combo.isVisible());
//        JButton button = (JButton) getField(qtc1, "newButton");
//        assertFalse(button.isEnabled());
//        assertTrue(button.isVisible());
//
//        openedHandler.reset();
//        savedQueriesHandler.reset();
//        QueryAction.openQuery(null, repo, false);
//        openedHandler.waitUntilDone();
//        savedQueriesHandler.waitUntilDone();
//
//        QueryTopComponent qtc2 = getQueryTC(qtc1);
//        combo = (JComboBox) getField(qtc2, "repositoryComboBox");
//        assertTrue(combo.isEnabled());
//        assertTrue(combo.isVisible());
//        button = (JButton) getField(qtc2, "newButton");
//        assertTrue(button.isEnabled());
//        assertTrue(button.isVisible());
//
//        JPanel queriesPanel = (JPanel) getField(qtc2, "queriesPanel");
//        assertTrue(queriesPanel.isVisible());
//        QueryProvider[] savedQueries = (QueryProvider[]) getField(qtc2, "savedQueries");
//        assertEquals(1, savedQueries.length);
//    }

//    XXX failing on hudson SHOULD BE FIXED
//    public void testSaveQuery() throws Throwable {
//        MyRepository repo = MyConnector.repo;
//
//        LogHandler openedHandler = new LogHandler("opened", LogHandler.Compare.ENDS_WITH);
//
//        repo.queries.add(new MyQuery(repo));
//
//        QueryAction.openQuery(null, repo, true);
//        openedHandler.waitUntilDone();
//
//        QueryTopComponent qtc1 = getQueryTC();
//        JPanel repoPanel = (JPanel) getField(qtc1, "repoPanel");
//        assertTrue(repoPanel.isVisible());
//
//        repo.newquery.setSaved(true);
//        repoPanel = (JPanel) getField(qtc1, "repoPanel");
//        assertFalse(repoPanel.isVisible());
//    }

//    XXX failing on hudson SHOULD BE FIXED
//    public void testOpenQuery() throws Throwable {
//        MyRepository repo = MyConnector.repo;
//
//        LogHandler openedHandler = new LogHandler("opened", LogHandler.Compare.ENDS_WITH);
//
//        repo.queries.add(new MyQuery(repo));
//        MyQuery query = new MyQuery(repo);
//        QueryAction.openQuery(query);
//        openedHandler.waitUntilDone();
//
//        QueryTopComponent qtc1 = getQueryTC();
//        JPanel repoPanel = (JPanel) getField(qtc1, "repoPanel");
//        assertFalse(repoPanel.isVisible());
//
//        openedHandler.reset();
//        query = new MyQuery(repo); // new query
//        QueryAction.openQuery(query);
//        openedHandler.waitUntilDone();
//
//        QueryTopComponent qtc2 = getQueryTC(qtc1);
//        assertNotSame(qtc1, qtc2);
//
//        repoPanel = (JPanel) getField(qtc2, "repoPanel");
//        assertFalse(repoPanel.isVisible());
//    }

    private QueryTopComponent getQueryTC(QueryTopComponent... ignore) {
        QueryTopComponent qtc = null;
        Set<TopComponent> tcs = TopComponent.getRegistry().getOpened();
        for (TopComponent tc : tcs) {
            if (tc instanceof QueryTopComponent) {
                boolean found = false;
                for (TopComponent i : ignore) {
                    if(tc == i) {
                        found = true;
                        break;
                    }
                }
                if(found) continue;
                qtc = (QueryTopComponent) tc;
                break;
            }
        }
        assertNotNull(qtc);
        return qtc;
    }

    private QueryProvider[] getSavedQueries(QueryTopComponent tc) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        return (QueryProvider[]) getField(tc, "savedQueries");
    }

    private Object getField(Object o, String name) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = o.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(o);
    }

    private static class MyRepository extends TestRepository {
        List<TestQuery> queries = new ArrayList<TestQuery>();
        MyQuery newquery;
        private static int c = 0;
        private final int i;
        private final RepositoryInfo info;
        public MyRepository() {
            this.newquery = new MyQuery();
            this.i = c++;
            String name = "repoid" + i;
            info = new RepositoryInfo(name, name, "http://repo", name, name, null, null, null, null);
        }

        @Override
        public RepositoryInfo getInfo() {
            return info;
        }

        @Override
        public Image getIcon() {
            return null;
        }

        @Override
        public TestQuery createQuery() {
            return newquery;
        }

        @Override
        public Collection<TestQuery> getQueries() {
            return queries;
        }

    }

    private static class MyQuery extends TestQuery {
        private static int c = 0;
        private final int i;

        private final QueryController controler = new QueryController() {
            private final JPanel panel = new JPanel();
            
            @Override
            public JComponent getComponent(QueryMode mode) {
                return panel;
            }
            @Override
            public HelpCtx getHelpCtx() {
                return null;
            }

            @Override
            public boolean providesMode(QueryMode mode) {
                return true;
            }

            @Override public void opened() { }
            @Override public void closed() { }
            @Override
            public boolean saveChanges(String name) {
                return true;
            }
            @Override
            public boolean discardUnsavedChanges() {
                return true;
            }
            @Override public void addPropertyChangeListener(PropertyChangeListener l) { }
            @Override public void removePropertyChangeListener(PropertyChangeListener l) { }
            @Override
            public boolean isChanged() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }    
        };
        private boolean saved;

        public MyQuery() {
            i = c++;
        }

        @Override
        public String getDisplayName() {
            return "query"+i;
        }
        @Override
        public String getTooltip() {
            return "query"+i;
        }
        @Override
        public QueryController getController() {
            return controler;
        }
        public void setSaved(boolean saved) {
            this.saved = saved;
        }
        @Override
        public boolean isSaved() {
            return saved;
        }
    }
    
    @BugtrackingConnector.Registration (
        id=MyConnector.ID,
        displayName="Dummy bugtracking connector",
        tooltip="bugtracking connector created for testing purposes"
    )
    public static class MyConnector implements BugtrackingConnector {
        static final String ID = "QTCconector";

        public MyConnector() {
        }
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public Repository createRepository(RepositoryInfo info) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Repository createRepository() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

}
