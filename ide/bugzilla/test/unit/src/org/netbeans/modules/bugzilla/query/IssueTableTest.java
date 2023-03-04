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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.issuetable.IssueTable;
import org.netbeans.modules.bugtracking.issuetable.IssuetableTestFactory;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugzilla.TestConstants;
import org.netbeans.modules.bugzilla.TestUtil;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.openide.util.test.MockLookup;

/**
 *
 * @author tomas
 */
public class IssueTableTest extends IssuetableTestFactory implements QueryConstants, TestConstants {

    private Map<String, BugzillaQuery> queries = new HashMap<>();
    
    public IssueTableTest(Test test) {
        super(test);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(org.netbeans.modules.bugtracking.issuetable.IssueTableTestCase.class);
        return new IssueTableTest(suite);
    }
    
    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", System.getProperty("java.io.tmpdir"));
        System.setProperty("netbeans.t9y.bugzilla.force.refresh.delay", "please!");
        
        MockLookup.setLayersAndInstances();
        BugtrackingUtil.getBugtrackingConnectors(); // ensure conector
    }

    @Override
    protected void tearDown() throws Exception {        
    }

    @Override
    public Query createQuery() {
        final String summary = "summary" + System.currentTimeMillis();

        final BugzillaRepository repo = QueryTestUtil.getRepository();

        String p =  MessageFormat.format(PARAMETERS_FORMAT, summary);
        String queryName = QUERY_NAME + System.currentTimeMillis();
        final BugzillaQuery bugzillaQuery = new BugzillaQuery(queryName, repo, p, false, false, true); // false = not saved
        assertEquals(0,bugzillaQuery.getIssues().size());
        Query query = TestUtil.getQuery(bugzillaQuery);
        queries.put(queryName, bugzillaQuery);
        return query;
    }
    
    @Override
    public void setSaved(Query q) {
        BugzillaQuery bugzillaQuery = queries.get(q.getDisplayName());
        bugzillaQuery.getController().save(q.getDisplayName());
    }
    
    @Override
    public IssueTable getTable(Query q) {
        try {
            BugzillaQuery bugzillaQuery = queries.get(q.getDisplayName());
            QueryController c = bugzillaQuery.getController();
            return c.getIssueTable();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int getColumnsCountBeforeSave() {
        return 7;
    }

    @Override
    public int getColumnsCountAfterSave() {
        return 9;
    }



}
