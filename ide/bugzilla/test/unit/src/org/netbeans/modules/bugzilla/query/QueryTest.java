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

package org.netbeans.modules.bugzilla.query;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.netbeans.modules.bugzilla.*;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.bugtracking.spi.QueryController.QueryMode;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.openide.util.test.MockLookup;

/**
 *
 * @author tomas
 */
public class QueryTest extends NbTestCase implements TestConstants, QueryConstants {

    private static final EnumSet<IssueStatusProvider.Status> STATUS_ALL = EnumSet.allOf(IssueStatusProvider.Status.class);
    
    public QueryTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        MockLookup.setLayersAndInstances();
        BugtrackingUtil.getBugtrackingConnectors(); // ensure connector
        
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
    }

    public void testRefresh() throws MalformedURLException, CoreException, InterruptedException {
        long ts = System.currentTimeMillis();
        String summary = "somary" + ts;
        String id1 = TestUtil.createIssue(QueryTestUtil.getRepository(), summary);

        LogHandler h = new LogHandler("Finnished populate", LogHandler.Compare.STARTS_WITH);

        String p =  MessageFormat.format(PARAMETERS_FORMAT, summary);
        BugzillaQuery q = new BugzillaQuery(QUERY_NAME + ts, QueryTestUtil.getRepository(), p, true, false, true);
        ts = System.currentTimeMillis();
        h.waitUntilDone();

        TestQueryNotifyListener nl = new TestQueryNotifyListener(q);

        nl.reset();
        q.refreshIntern(false);
        Collection<BugzillaIssue> is = q.getIssues();
        assertEquals(1, is.size());
        assertTrue(nl.started);
        assertTrue(nl.finished);
        List<BugzillaIssue> il = nl.getIssues(STATUS_ALL);
        assertEquals(1, il.size());
        BugzillaIssue i = il.get(0);
        assertEquals(summary, i.getSummary());
        assertEquals(id1, i.getID());

        nl.reset();
        q.refresh(p, false);
        assertTrue(nl.started);
        assertTrue(nl.finished);
        il = nl.getIssues(EnumSet.allOf(IssueStatusProvider.Status.class));
        assertEquals(1, il.size());
        i = il.get(0);
        assertEquals(summary, i.getSummary());
        assertEquals(id1, i.getID());
        is = q.getIssues();
        assertEquals(1, is.size());
    }

    public void testGetIssues() throws MalformedURLException, CoreException {
        long ts = System.currentTimeMillis();
        String summary1 = "somary1" + ts;
        String id1 = TestUtil.createIssue(QueryTestUtil.getRepository(), summary1);
        String summary2 = "somary2" + ts;
        String id2 = TestUtil.createIssue(QueryTestUtil.getRepository(), summary2);

        // query for issue1
        String p =  MessageFormat.format(PARAMETERS_FORMAT, summary1);
        BugzillaQuery q = new BugzillaQuery(QUERY_NAME + ts, QueryTestUtil.getRepository(), p, true, false, true);
        TestQueryNotifyListener nl = new TestQueryNotifyListener(q);
        
        Collection<BugzillaIssue> bugzillaIssues = q.getIssues();
        assertEquals(0, nl.issues.size());

        nl.reset();
        q.refreshIntern(false);
        assertTrue(nl.started);
        assertTrue(nl.finished);
        assertEquals(1, nl.getIssues(EnumSet.allOf(IssueStatusProvider.Status.class)).size());
        assertEquals(1, q.getIssues().size());
        BugzillaIssue i = q.getIssues().iterator().next();
        assertEquals(summary1, i.getSummary());
        assertEquals(id1, i.getID());

        nl.reset();
        q.refresh(p, false);
        assertTrue(nl.started);
        assertTrue(nl.finished);
        assertEquals(1, nl.getIssues(STATUS_ALL).size());
        assertEquals(1, q.getIssues().size());
        i = q.getIssues().iterator().next();
        assertEquals(summary1, i.getSummary());
        assertEquals(id1, i.getID());

        // query for issue1 & issue2
        p =  MessageFormat.format(PARAMETERS_FORMAT, Long.toString(ts));
        nl.reset();
        q.refresh(p, false);
        bugzillaIssues = q.getIssues();
        assertTrue(nl.started);
        assertTrue(nl.finished);
        assertEquals(2, nl.getIssues(STATUS_ALL).size());
        assertEquals(2, bugzillaIssues.size());
        List<String> summaries = new ArrayList<String>();
        List<String> ids = new ArrayList<String>();
        for(BugzillaIssue issue : bugzillaIssues) {
            summaries.add(issue.getSummary());
            ids.add(issue.getID());
        }
        assertTrue(summaries.contains(summary1));
        assertTrue(summaries.contains(summary2));
        assertTrue(ids.contains(id1));
        assertTrue(ids.contains(id2));

//        Collection<Issue> is = BugtrackingUtil.getByIdOrSummary(BugzillaUtil.getQuery(q).getIssues(), "" + ts); // shoud return both issues
//        assertEquals(2, is.size());
//        summaries = new ArrayList<String>();
//        ids = new ArrayList<String>();
//        for(Issue issue : is) {
//            summaries.add(issue.getSummary());
//            ids.add(issue.getID());
//        }
//        assertTrue(summaries.contains(summary1));
//        assertTrue(summaries.contains(summary2));
//        assertTrue(ids.contains(id1));
//        assertTrue(ids.contains(id2));
//        
//        is = BugtrackingUtil.getByIdOrSummary(BugzillaUtil.getQuery(q).getIssues(), summary1); // shoud return 1st issue
//        assertEquals(1, is.size());
//        assertEquals(id1, is.iterator().next().getID());
//        assertEquals(summary1, is.iterator().next().getSummary());
    }


    // XXX test obsolete status

    // XXX shoud be on the spi
    public void testLastRefresh() {
        String parameters = "query_format=advanced&" +
          "short_desc_type=allwordssubstr&" +
          "short_desc=whatever112233445566778899&" +
          "product=TestProduct";
        String qname = "q" + System.currentTimeMillis();
        BugzillaQuery q = new BugzillaQuery(qname, QueryTestUtil.getRepository(), parameters, true, true, false);
        long lastRefresh = q.getLastRefresh();
        assertEquals(-1, lastRefresh);
        long ts = System.currentTimeMillis();

        ts = System.currentTimeMillis();
        q.refresh(parameters, false);
        assertTrue(q.getLastRefresh() >= ts);

        ts = System.currentTimeMillis();
        q.refreshIntern(false);
        lastRefresh = q.getLastRefresh();
        assertTrue(lastRefresh >= ts);

        // emulate restart
        q = new BugzillaQuery(qname, QueryTestUtil.getRepository(), parameters, true, true, false);
        assertEquals((int)(lastRefresh/1000), (int)(q.getLastRefresh()/1000));

    }

    @RandomlyFails
    public void testSaveAterSearch() throws MalformedURLException, CoreException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InterruptedException, NoSuchFieldException {
        long ts = System.currentTimeMillis();
        String summary = "somary" + ts;
        BugzillaRepository repository = QueryTestUtil.getRepository();
        String id1 = TestUtil.createIssue( repository, summary);

        LogHandler h = new LogHandler("Finnished populate", LogHandler.Compare.STARTS_WITH);

        // create query
        BugzillaQuery q = new BugzillaQuery(repository);

        // get controler and wait until populated with default values
        QueryController c = q.getController();
        ts = System.currentTimeMillis();
        h.waitUntilDone();

        // populate with parameters - summary
        populate(c, summary);

        TestQueryNotifyListener nl = new TestQueryNotifyListener(q);

        // search
        nl.reset();
        h = new LogHandler("refresh finish", LogHandler.Compare.STARTS_WITH);
        search(c); // search button and wait until done
        ts = System.currentTimeMillis();
        h.waitUntilDone();

        assertTrue(nl.started);
        assertTrue(nl.finished);
        List<BugzillaIssue> il = nl.getIssues(STATUS_ALL);
        assertEquals(1, il.size());
        BugzillaIssue i = il.get(0);
        assertEquals(summary, i.getSummary());
        assertEquals(id1, i.getID());

        // save
        nl.reset();
        String name = QUERY_NAME + ts;
        h = new LogHandler(" saved", LogHandler.Compare.ENDS_WITH);
        q.setName(name);
        q.getController().save(name);
        save(c); // save button
        h.waitUntilDone();
        assertTrue(q.isSaved());
        // create a new repo instance and check if our query is between them
        repository = QueryTestUtil.getRepository();
        Collection<BugzillaQuery> queries = repository.getQueries();
        boolean bl = false;
        for (BugzillaQuery query : queries) {
            bl = query.getDisplayName().equals(name);
            if(bl) break;
        }
        assertTrue(bl);
    }

    public void testSaveBeforeSearch() throws MalformedURLException, CoreException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InterruptedException, NoSuchFieldException {
        long ts = System.currentTimeMillis();
        String summary = "somary" + ts;
        String id1 = TestUtil.createIssue(QueryTestUtil.getRepository(), summary);

        LogHandler h = new LogHandler("Finnished populate ", LogHandler.Compare.STARTS_WITH);

        // create query
        BugzillaQuery q = new BugzillaQuery(QueryTestUtil.getRepository());

        // get controler and wait until populated with default values
        QueryController c = q.getController();
        h.waitUntilDone();

        // populate with parameters - summary
        populate(c, summary);

        TestQueryNotifyListener nl = new TestQueryNotifyListener(q);
        nl.reset();

        QueryListener ql = new QueryListener(q.getRepository(), q);
        q.getController().addPropertyChangeListener(ql);
        
        String name = QUERY_NAME + ts;
        q.setName(name);
        q.setSaved(true); 

        h = new LogHandler("refresh finish", LogHandler.Compare.STARTS_WITH); // we wan't to check
                                                                              // if the refresh is made after save  
        save(c); // save button
        h.waitUntilDone();
        assertEquals(1, ql.saved);

        assertTrue(nl.started);
        assertTrue(nl.finished);
        List<BugzillaIssue> il = nl.getIssues(STATUS_ALL);
        assertEquals(1, il.size());
        BugzillaIssue i = il.get(0);
        assertEquals(summary, i.getSummary());
        assertEquals(id1, i.getID());
    }

    public void testSaveRemove() throws MalformedURLException, CoreException, InterruptedException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        long ts = System.currentTimeMillis();

        // create query
        LogHandler h = new LogHandler("Finnished populate", LogHandler.Compare.STARTS_WITH);
        BugzillaQuery q = new BugzillaQuery(QueryTestUtil.getRepository());

        // get controler and wait until populated with default values
        QueryController c = q.getController();
        h.waitUntilDone();
        Collection<BugzillaQuery> qs = QueryTestUtil.getRepository().getQueries();
        int queriesCount = qs.size();

        QueryListener ql = new QueryListener(q.getRepository(), q);
        q.getController().addPropertyChangeListener(ql);
        
        String name = QUERY_NAME + ts;
        q.setName(name);
        q.setSaved(true); 
        
        // save
        h = new LogHandler(" saved", LogHandler.Compare.ENDS_WITH);
        save(c);
        h.waitUntilDone();
        assertEquals(1, ql.saved);

        qs = QueryTestUtil.getRepository().getQueries();
        assertEquals(queriesCount + 1, qs.size());

        // remove
        remove(c);
        assertEquals(1, ql.removed);
        qs = QueryTestUtil.getRepository().getQueries();
        assertEquals(queriesCount, qs.size());
    }

    private void save(QueryController c) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method m = c.getClass().getDeclaredMethod("onSave");
        m.setAccessible(true);
        m.invoke(c);
    }

    private void remove(QueryController c) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method m = c.getClass().getDeclaredMethod("remove");
        m.setAccessible(true);
        m.invoke(c);
    }

    private void search(QueryController c) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method m = c.getClass().getDeclaredMethod("onRefresh");
        m.setAccessible(true);
        m.invoke(c);
    }

    private void populate(QueryController c, String summary) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        QueryPanel p = (QueryPanel) c.getComponent(QueryMode.EDIT);
        p.summaryTextField.setText(summary);
        p.productList.getSelectionModel().clearSelection(); // no product
        Field f = c.getClass().getDeclaredField("populated");
        f.setAccessible(true);
        f.set(c, true);
    }

    private class QueryListener implements PropertyChangeListener {
        int saved = 0;
        int removed = 0;
        private final BugzillaRepository repo;
        private final BugzillaQuery query;

        public QueryListener(BugzillaRepository repo, BugzillaQuery query) {
            this.repo = repo;
            this.query = query;
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(RepositoryProvider.EVENT_QUERY_LIST_CHANGED)) {
                Collection<BugzillaQuery> queries = repo.getQueries();
                for (BugzillaQuery q : queries) {
                    if(q.getDisplayName().equals(query.getDisplayName())) {
                        // this query wasn't removed
                        return; 
                    }
                }
                removed++;
            }
            if(evt.getPropertyName().equals(QueryController.PROP_CHANGED)) {
                saved++;
            }
        }
        void reset() {
            saved = 0;
            removed = 0;
        }

    }
}
