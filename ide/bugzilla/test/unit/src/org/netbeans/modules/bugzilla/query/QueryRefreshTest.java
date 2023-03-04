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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.TestKit;
import org.netbeans.modules.bugtracking.dummies.DummyBugtrackingOwnerSupport;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.LogHandler;
import org.netbeans.modules.bugzilla.TestConstants;
import static org.netbeans.modules.bugzilla.TestConstants.REPO_PASSWD;
import static org.netbeans.modules.bugzilla.TestConstants.REPO_URL;
import static org.netbeans.modules.bugzilla.TestConstants.REPO_USER;
import org.netbeans.modules.bugzilla.TestUtil;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.test.MockLookup;

/**
 *
 * @author tomas
 */
public class QueryRefreshTest extends NbTestCase implements TestConstants, QueryConstants {

    public QueryRefreshTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }   
    
    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        // bypass bugtracking owner logic
        System.setProperty("org.openide.util.Lookup", TestLookup.class.getName());
        
        // refresh faster
        System.setProperty("netbeans.t9y.bugzilla.force.refresh.schedule", "60000");
        
        MockLookup.setLayersAndInstances();
        BugtrackingUtil.getBugtrackingConnectors(); // ensure conector        
    }

    @Override
    protected void tearDown() throws Exception {        
    }

    public void testQueryOpenNoRefresh() throws Throwable {
        long ts = System.currentTimeMillis();
        final String summary = "summary" + System.currentTimeMillis();

        final BugzillaRepository repo = QueryTestUtil.getRepository();        
        String id = TestUtil.createIssue(repo, summary);
        assertNotNull(id);

        LogHandler h = new LogHandler("Finnished populate", LogHandler.Compare.STARTS_WITH);

        String p =  MessageFormat.format(PARAMETERS_FORMAT, summary);
        final BugzillaQuery q = new BugzillaQuery(QUERY_NAME, repo, p, false, false, true);
        ts = System.currentTimeMillis();
        h.waitUntilDone();


        LogHandler lh = new LogHandler("scheduling query", LogHandler.Compare.STARTS_WITH);
        Bugzilla.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                // init columndescriptors before opening query to prevent some "do not call in awt asserts"
                BugzillaIssue.getColumnDescriptors(repo);
                TestKit.openQuery(TestUtil.getQuery(q));
            }
        }).waitFinished();
        assertFalse(lh.isDone());    // but this one wasn't yet
    }

    public static final class TestLookup extends AbstractLookup {
        public TestLookup() {
            this(new InstanceContent());
        }
        private TestLookup(InstanceContent ic) {
            super(ic);
            ic.add(new DummyBugtrackingOwnerSupport());
        }
    }

}
