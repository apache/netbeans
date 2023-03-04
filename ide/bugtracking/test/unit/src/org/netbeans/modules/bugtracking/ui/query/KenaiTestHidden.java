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

import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author tomas
 */
public class KenaiTestHidden extends NbTestCase {

    private String username;
    private String password;
//    private Kenai kenai;

    public KenaiTestHidden(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

//    @Override
//    protected void setUp() throws Exception {
//        super.setUp();
//
//        BufferedReader br = new BufferedReader(new FileReader(new File(System.getProperty("user.home"), ".test-kenai")));
//        username = br.readLine();
//        password = br.readLine();
//        br.close();
//
//        kenai = KenaiManager.getDefault().createKenai("testkenai", "https://testkenai.com");
//    }
//
////    public void testQueryTopComponent() throws Throwable {
////        KenaiRepository repo = KenaiConnector.repo;
////
////        LogHandler openedHandler = new LogHandler("QueryAction.openQuery finnish", LogHandler.Compare.STARTS_WITH);
////
////        QueryAction.openQuery(null, repo, true);
////        openedHandler.waitUntilDone();
////
////        QueryTopComponent qtc = getQueryTC();
////        JComboBox combo = (JComboBox) getField(qtc, "repositoryComboBox");
////        assertFalse(combo.isEnabled());
////        JButton button = (JButton) getField(qtc, "newButton");
////        assertFalse(button.isEnabled());
////
////        kenai.login(username, password.toCharArray());
////        kenai.logout();
////        kenai.login(username, password.toCharArray());
////
////        combo = (JComboBox) getField(qtc, "repositoryComboBox");
////        assertFalse(combo.isEnabled());
////        button = (JButton) getField(qtc, "newButton");
////        assertFalse(button.isEnabled());
////    }
//
//    public void testRefreshQueriesInQueryTopComponent() throws Throwable {
//        QueryAccessorImpl qa = new QueryAccessorImpl(); // need the instace to listen on kenai
//
//        kenai.login(username, password.toCharArray());
//
//        KenaiRepository repo = KenaiConnector.repo;
//        repo.queries.add(new KenaiQuery(repo));
//
//        LogHandler openedHandler = new LogHandler("QueryAction.openQuery finnish", LogHandler.Compare.STARTS_WITH);
//        LogHandler savedHandler = new LogHandler("saved queries.", LogHandler.Compare.ENDS_WITH);
//
//        QueryAction.openQuery(null, repo, true);
//        openedHandler.waitUntilDone();
//
//        QueryTopComponent qtc = getQueryTC();
//
//        savedHandler.waitUntilDone();
//        QueryProvider[] savedQueries = getSavedQueries(qtc);
//        assertNotNull(savedQueries);
//
//        assertEquals(repo.queries.size(), savedQueries.length);
//
//        repo.queries.add(new KenaiQuery(repo));
//        savedHandler.reset();
//        kenai.logout();
//        savedHandler.waitUntilDone();
//
//        savedQueries = getSavedQueries(qtc);
//        assertEquals(repo.queries.size(), savedQueries.length);
//
//        repo.queries.clear();
//        LogHandler noQueriesHandler = new LogHandler("No queries.", LogHandler.Compare.ENDS_WITH);
//        kenai.login(username, password.toCharArray());
//        noQueriesHandler.waitUntilDone();
//
//        savedQueries = getSavedQueries(qtc);
//        assertEquals(repo.queries.size(), savedQueries.length);
//    }
//
//    private QueryTopComponent getQueryTC() {
//        QueryTopComponent qtc = null;
//        Set<TopComponent> tcs = TopComponent.getRegistry().getOpened();
//        for (TopComponent tc : tcs) {
//            if (tc instanceof QueryTopComponent) {
//                qtc = (QueryTopComponent) tc;
//            }
//        }
//        assertNotNull(qtc);
//        return qtc;
//    }
//
//    private QueryProvider[] getSavedQueries(QueryTopComponent tc) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
//        return (QueryProvider[]) getField(tc, "savedQueries");
//    }
//
//    private Object getField(Object o, String name) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
//        Field f = o.getClass().getDeclaredField(name);
//        f.setAccessible(true);
//        return f.get(o);
//    }
//
//    private static class KenaiRepository extends RepositoryProvider {
//        List<Query> queries = new ArrayList<Query>();
//        QueryProvider newquery;
//
//        public KenaiRepository() {
//            this.newquery = new KenaiQuery(this);
//        }
//
//        @Override
//        public String getID() {
//            return "repoid";
//        }
//        
//        @Override
//        public Image getIcon() {
//            return null;
//        }
//
//        @Override
//        public String getDisplayName() {
//            return "kenai repo";
//        }
//
//        @Override
//        public String getTooltip() {
//            return "kenai repo";
//        }
//
//        @Override
//        public String getUrl() {
//            return "http://kenai.repo";
//        }
//
//        @Override
//        public IssueProvider getIssue(String id) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public void remove() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public BugtrackingController getController() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public QueryProvider createQuery() {
//            return newquery;
//        }
//
//        @Override
//        public IssueProvider createIssue() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public QueryProvider[] getQueries() {
//            return queries.toArray(new QueryProvider[queries.size()]);
//        }
//
//        @Override
//        public IssueProvider[] simpleSearch(String criteria) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        protected IssueCache getIssueCache() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Lookup getLookup() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public Collection<RepositoryUser> getUsers() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//    }
//
//    private static class KenaiQuery extends QueryProvider {
//        private RepositoryProvider repository;
//        private BugtrackingController controler = new BugtrackingController() {
//            private JPanel panel = new JPanel();
//            @Override
//            public JComponent getComponent() {
//                return panel;
//            }
//            @Override
//            public HelpCtx getHelpCtx() {
//                return null;
//            }
//            @Override
//            public boolean isValid() {
//                return true;
//            }
//            @Override
//            public void applyChanges() throws IOException {}
//        };
//
//        public KenaiQuery(RepositoryProvider repository) {
//            this.repository = repository;
//        }
//
//        @Override
//        public String getDisplayName() {
//            return "kenai query";
//        }
//        @Override
//        public String getTooltip() {
//            return "kenai query";
//        }
//        @Override
//        public BugtrackingController getController() {
//            return controler;
//        }
//        @Override
//        public RepositoryProvider getRepository() {
//            return repository;
//        }
//        @Override
//        public IssueProvider[] getIssues(int includeStatus) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//        @Override
//        public boolean contains(IssueProvider issue) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//        @Override
//        public int getIssueStatus(IssueProvider issue) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//    }
//    
//    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.bugtracking.spi.BugtrackingConnector.class)
//    public static class KenaiConnector extends BugtrackingConnector {
//        static String ID = "KenaiCconector";
//        private static KenaiRepository repo = new KenaiRepository();
//
//        public KenaiConnector() {
//        }
//
//        @Override
//        public String getDisplayName() {
//            return ID;
//        }
//
//        @Override
//        public String getTooltip() {
//            return ID;
//        }
//
//        @Override
//        public RepositoryProvider createRepository() {
//                throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public RepositoryProvider[] getRepositories() {
//            return new RepositoryProvider[] {repo};
//        }
//
//        public Lookup getLookup() {
//            return Lookup.EMPTY;
//        }
//
//        @Override
//        public String getID() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public Image getIcon() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//    }

}
