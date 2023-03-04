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

package org.netbeans.modules.bugtracking;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.spi.*;

/**
 *
 * @author tomas
 */
public class RecentIssuesTest extends NbTestCase {

    public RecentIssuesTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();        
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        Field f = BugtrackingManager.class.getDeclaredField("recentIssues");
        f.setAccessible(true);
        List<IssueImpl> ri = (List<IssueImpl>) f.get(BugtrackingManager.getInstance());
        if(ri != null) ri.clear();
    }

    public void testGetRecentIssuesEmptyReturn() throws MalformedURLException, IOException {
        List<IssueImpl> ri = BugtrackingManager.getInstance().getAllRecentIssues();
        assertNotNull(ri);
        assertEquals(0, ri.size());
        
        List<IssueImpl> ri2 = BugtrackingManager.getInstance().getRecentIssues(getRepository(new RITestRepository("test repo")));
        assertNotNull(ri2);
        assertEquals(0, ri.size());        
    }

    public void testAddRecentIssues() throws MalformedURLException, IOException {
        final RITestRepository riTestRepo = new RITestRepository("test repo");
        RepositoryImpl repo = getRepository(riTestRepo);
        IssueImpl issue1 = getIssue(repo, new RITestIssue(riTestRepo, "1"));
        IssueImpl issue2 = getIssue(repo, new RITestIssue(riTestRepo, "2"));

        // add issue1
        BugtrackingManager.getInstance().addRecentIssue(repo, issue1);

        // test for another repo -> nothing is returned
        RepositoryImpl repo2 = getRepository(new RITestRepository("test repo 2"));
        List<IssueImpl>  issues = (List<IssueImpl>) BugtrackingManager.getInstance().getRecentIssues(repo2);
        assertNotNull(issues);
        assertEquals(0, issues.size());

        // getIssues for repo -> issue1 is returned
        issues = (List<IssueImpl>) BugtrackingManager.getInstance().getRecentIssues(repo);
        assertNotNull(issues);
        assertEquals(1, issues.size());
        assertEquals(issue1.getID(), issues.iterator().next().getID());
        
        // getAll -> issue1 is returned
        List<IssueImpl> allIssues = BugtrackingManager.getInstance().getAllRecentIssues();
        assertNotNull(allIssues);
        assertEquals(1, allIssues.size());
        assertTrue(allIssues.get(0).getRepositoryImpl().getId().equals(repo.getId()));
        assertEquals(issue1.getID(), allIssues.get(0).getID());

        // add issue2
        BugtrackingManager.getInstance().addRecentIssue(repo, issue2);

        // getIssues -> issue1 & issue2 are returned
        issues = (List<IssueImpl>) BugtrackingManager.getInstance().getRecentIssues(repo);
        assertNotNull(issues);
        assertEquals(2, issues.size());
        assertEquals(issue2.getID(), issues.get(0).getID());
        assertEquals(issue1.getID(), issues.get(1).getID());

        // getAll -> issue1 & issue2 are returned
        allIssues = BugtrackingManager.getInstance().getAllRecentIssues();
        assertNotNull(allIssues);
        assertEquals(2, allIssues.size());
        assertTrue(allIssues.get(0).getRepositoryImpl().getId().equals(repo.getId()));
        assertTrue(allIssues.get(1).getRepositoryImpl().getId().equals(repo.getId()));
        assertRecentIssues(allIssues, new IssueImpl[] {issue2, issue1});
    }

    public void testAddRecentIssuesMoreThan5() throws MalformedURLException, IOException {
        RITestRepository riTestRepo1 = new RITestRepository("test repo");
        RepositoryImpl repo1 = getRepository(new RITestRepository("test repo"));
        IssueImpl repo1issue1 = getIssue(repo1, new RITestIssue(riTestRepo1, "r1i1"));
        IssueImpl repo1issue2 = getIssue(repo1, new RITestIssue(riTestRepo1, "r1i2"));
        IssueImpl repo1issue3 = getIssue(repo1, new RITestIssue(riTestRepo1, "r1i3"));
        IssueImpl repo1issue4 = getIssue(repo1, new RITestIssue(riTestRepo1, "r1i4"));
        IssueImpl repo1issue5 = getIssue(repo1, new RITestIssue(riTestRepo1, "r1i5"));
        IssueImpl repo1issue6 = getIssue(repo1, new RITestIssue(riTestRepo1, "r1i6"));
        IssueImpl repo1issue7 = getIssue(repo1, new RITestIssue(riTestRepo1, "r1i7"));

        // add repo1 issues 1, 2, 3, 4, 5, 6, 7,
        BugtrackingManager.getInstance().addRecentIssue(repo1, repo1issue1);
        BugtrackingManager.getInstance().addRecentIssue(repo1, repo1issue2);
        BugtrackingManager.getInstance().addRecentIssue(repo1, repo1issue3);
        BugtrackingManager.getInstance().addRecentIssue(repo1, repo1issue4);
        BugtrackingManager.getInstance().addRecentIssue(repo1, repo1issue5);
        BugtrackingManager.getInstance().addRecentIssue(repo1, repo1issue6);
        BugtrackingManager.getInstance().addRecentIssue(repo1, repo1issue7);

        // getIssues for repo1 -> repo1 issues 1..7 are returned
        List<IssueImpl> issues = (List<IssueImpl>) BugtrackingManager.getInstance().getRecentIssues(repo1);
        assertNotNull(issues);
        assertEquals(7, issues.size());
        assertEquals(repo1issue7.getID(), issues.get(0).getID());
        assertEquals(repo1issue6.getID(), issues.get(1).getID());
        assertEquals(repo1issue5.getID(), issues.get(2).getID());
        assertEquals(repo1issue4.getID(), issues.get(3).getID());
        assertEquals(repo1issue3.getID(), issues.get(4).getID());
        assertEquals(repo1issue2.getID(), issues.get(5).getID());
        assertEquals(repo1issue1.getID(), issues.get(6).getID());

        RITestRepository riTestrepo2 = new RITestRepository("test repo2");
        RepositoryImpl repo2 = getRepository(riTestrepo2);
        IssueImpl repo2issue1 = getIssue(repo2, new RITestIssue(riTestrepo2, "r2i1"));
        IssueImpl repo2issue2 = getIssue(repo2, new RITestIssue(riTestrepo2, "r2i2"));
        IssueImpl repo2issue3 = getIssue(repo2, new RITestIssue(riTestrepo2, "r2i3"));
        IssueImpl repo2issue4 = getIssue(repo2, new RITestIssue(riTestrepo2, "r2i4"));
        IssueImpl repo2issue5 = getIssue(repo2, new RITestIssue(riTestrepo2, "r2i5"));
        IssueImpl repo2issue6 = getIssue(repo2, new RITestIssue(riTestrepo2, "r2i6"));
        IssueImpl repo2issue7 = getIssue(repo2, new RITestIssue(riTestrepo2, "r2i7"));

        // add repo2 issues 1, 2, 3, 4, 5, 6, 7,
        BugtrackingManager.getInstance().addRecentIssue(repo2, repo2issue1);
        BugtrackingManager.getInstance().addRecentIssue(repo2, repo2issue2);
        BugtrackingManager.getInstance().addRecentIssue(repo2, repo2issue3);
        BugtrackingManager.getInstance().addRecentIssue(repo2, repo2issue4);
        BugtrackingManager.getInstance().addRecentIssue(repo2, repo2issue5);
        BugtrackingManager.getInstance().addRecentIssue(repo2, repo2issue6);
        BugtrackingManager.getInstance().addRecentIssue(repo2, repo2issue7);

        // getIssues for repo2 -> repo2 issues 1..7 are returned
        issues = BugtrackingManager.getInstance().getRecentIssues(repo2);
        assertNotNull(issues);
        assertEquals(7, issues.size());
        assertEquals(repo2issue7.getID(), issues.get(0).getID());
        assertEquals(repo2issue6.getID(), issues.get(1).getID());
        assertEquals(repo2issue5.getID(), issues.get(2).getID());
        assertEquals(repo2issue4.getID(), issues.get(3).getID());
        assertEquals(repo2issue3.getID(), issues.get(4).getID());
        assertEquals(repo2issue2.getID(), issues.get(5).getID());
        assertEquals(repo2issue1.getID(), issues.get(6).getID());
    }

    public void testRecentIssueDeleted () throws MalformedURLException, IOException {
        final RITestRepository riTestRepo = new RITestRepository("test repo");
        RepositoryImpl repo = getRepository(riTestRepo);
        RITestIssue riIssue = new RITestIssue(riTestRepo, "1");
        IssueImpl issue1 = getIssue(repo, riIssue);
        IssueImpl issue2 = getIssue(repo, new RITestIssue(riTestRepo, "2"));

        // add issue1
        BugtrackingManager.getInstance().addRecentIssue(repo, issue1);

        // test for repo -> issue is returned
        List<IssueImpl> issues = (List<IssueImpl>) BugtrackingManager.getInstance().getRecentIssues(repo);
        assertNotNull(issues);
        assertEquals(1, issues.size());
        assertEquals(issue1.getID(), issues.iterator().next().getID());
        
        // getAll -> issue1 is returned
        List<IssueImpl> allIssues = BugtrackingManager.getInstance().getAllRecentIssues();
        assertNotNull(allIssues);
        assertEquals(1, allIssues.size());
        assertTrue(allIssues.get(0).getRepositoryImpl().getId().equals(repo.getId()));
        assertEquals(issue1.getID(), allIssues.get(0).getID());
        
        // add second issue
        BugtrackingManager.getInstance().addRecentIssue(repo, issue2);
        
        // delete issue
        riIssue.deleted();
        
        // test for repo => only the second issue returned
        issues = (List<IssueImpl>) BugtrackingManager.getInstance().getRecentIssues(repo);
        assertNotNull(issues);
        assertEquals(1, allIssues.size());
        assertTrue(allIssues.get(0).getRepositoryImpl().getId().equals(repo.getId()));
        assertEquals(issue2.getID(), allIssues.get(0).getID());
        
        // getAll -> only the second issue returned
        allIssues = BugtrackingManager.getInstance().getAllRecentIssues();
        assertNotNull(allIssues);
        assertEquals(1, allIssues.size());
        assertTrue(allIssues.get(0).getRepositoryImpl().getId().equals(repo.getId()));
        assertEquals(issue2.getID(), allIssues.get(0).getID());
    }

    private void assertRecentIssues(List<IssueImpl> recent, IssueImpl[] issues) {
        assertEquals(recent.size(), issues.length);
        for (int i = 0; i < issues.length; i++) {
            assertEquals(issues[i].getID(), recent.get(i).getID());
        }
    }

    private class RITestRepository extends TestRepository {
        private final String name;
        private RepositoryInfo info;

        public RITestRepository(String name) {
            this.name = name;
            info = new RepositoryInfo(name, name, null, name, name, null, null, null, null);
        }

        @Override
        public RepositoryInfo getInfo() {
            return info;
        }
    }
        
    private class RITestIssue extends TestIssue {
        private final String name;
        private final RITestRepository repository;
        private final PropertyChangeSupport support;
        public RITestIssue(RITestRepository repository, String name) {
            this.repository = repository;
            this.name = name;
            this.support = new PropertyChangeSupport(this);
        }
        public String getDisplayName() {
            return name;
        }
        public String getTooltip() {
            return name;
        }
        public boolean isNew() {
            return false;
        }
        public boolean isFinished() {
            return false;
        }
        public String getSummary() {
            return "This is" + name;
        }
        public String getID() {
            return name;
        }
        @Override
        public void addPropertyChangeListener (PropertyChangeListener listener) {
            support.addPropertyChangeListener(listener);
        }
        @Override
        public void removePropertyChangeListener (PropertyChangeListener listener) {
            support.removePropertyChangeListener(listener);
        }
        private void deleted () {
            support.firePropertyChange(IssueProvider.EVENT_ISSUE_DELETED, null, null);
        }
    }

    private class RITestConector implements BugtrackingConnector {
        @Override
        public Repository createRepository(RepositoryInfo info) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public Repository createRepository() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    private RepositoryImpl getRepository(RITestRepository repo) {
        return TestKit.getRepository(repo);
    }
    
    private IssueImpl getIssue(RepositoryImpl repo2, RITestIssue riTestIssue) {
        return TestKit.getIssue(repo2, riTestIssue);
    }
    
}
