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

package org.netbeans.modules.bugtracking.api;

import java.util.Set;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.APIAccessor;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.ui.issue.IssueTopComponent;
import org.netbeans.modules.bugtracking.ui.query.QueryTopComponent;
import org.openide.util.test.MockLookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author tomas
 */
public class UtilTestCase extends NbTestCase {

    public UtilTestCase(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }   
    
    @Override
    protected void setUp() throws Exception {    
        MockLookup.setLayersAndInstances();
        APITestConnector.init();
    }

    @Override
    protected void tearDown() throws Exception { }

    public void testOpenIssue() throws InterruptedException {
        APITestIssue apiIssue = getAPIIssue(APITestIssue.ID_1);
        Issue issue = getIssue(APITestIssue.ID_1);
        
        apiIssue.wasOpened = false;
        Util.openIssue(getRepo(), APITestIssue.ID_1);
        assertOpened(apiIssue);
        
        IssueTopComponent tc = IssueTopComponent.find(APIAccessor.IMPL.getImpl(issue));
        assertNotNull(tc);
        tc.close();
    }

    public void testCreateNewIssue() {
        Repository repo = getRepo();
        APITestRepository apiRepo = getApiRepo();
        
        apiRepo.newIssue = null;
        Util.createNewIssue(repo);
        
        long t = System.currentTimeMillis();
        TopComponent openedTC = null;
        while(openedTC == null) {
            Set<TopComponent> openedTCs = WindowManager.getDefault().getRegistry().getOpened();
            for (TopComponent tc : openedTCs) {
                if(tc instanceof IssueTopComponent) {
                    IssueTopComponent itc = (IssueTopComponent)tc;
                    IssueImpl issueImpl = itc.getIssue();
                    if(issueImpl != null && issueImpl.isData(apiRepo.newIssue)) {
                        openedTC = tc;
                        break;
                    }
                }
            }
            if(System.currentTimeMillis() - t > 5000) {
                break;
            }
        }
        
        assertNotNull(apiRepo.newIssue);
        if(openedTC == null) {
            fail("TopComponent with new issue wasn't opened");
        }
        openedTC.close();
    }
    
    public void testCreateIssue() {
        Repository repo = getRepo();
        APITestRepository apiRepo = getApiRepo();
        
        apiRepo.newIssue = null;
        String summary = "summary";
        String desc = "desc";
        Util.createIssue(repo, summary, desc);
        
        long t = System.currentTimeMillis();
        TopComponent openedTC = null;
        while(openedTC == null) {
            Set<TopComponent> openedTCs = WindowManager.getDefault().getRegistry().getOpened();
            for (TopComponent tc : openedTCs) {
                if(tc instanceof IssueTopComponent) {
                    IssueTopComponent itc = (IssueTopComponent)tc;
                    IssueImpl issueImpl = itc.getIssue();
                    if(issueImpl != null && issueImpl.isData(apiRepo.newIssue)) {
                        openedTC = tc;
                        break;
                    }
                }
            }
            if(System.currentTimeMillis() - t > 5000) {
                break;
            }
        }
        
        assertNotNull(apiRepo.newIssue);
        if(openedTC == null) {
            fail("TopComponent with new issue wasn't opened");
        }
        assertEquals(summary, apiRepo.newIssue.getSummary());
        assertEquals(desc, apiRepo.newIssue.getDescription());
        
        openedTC.close();
    }
    
    public void testCreateNewQuery() {
        Repository repo = getRepo();
        APITestRepository apiRepo = getApiRepo();
        
        assertNull(apiRepo.newQuery);
        Util.createNewQuery(repo);
        
        long t = System.currentTimeMillis();
        TopComponent openedTC = null;
        while(openedTC == null) {
            Set<TopComponent> openedTCs = WindowManager.getDefault().getRegistry().getOpened();
            for (TopComponent tc : openedTCs) {
                if(tc instanceof QueryTopComponent) {
                    QueryTopComponent itc = (QueryTopComponent)tc;
                    QueryImpl queryImpl = itc.getQuery();
                    if(queryImpl != null && queryImpl.isData(apiRepo.newQuery)) {
                        openedTC = tc;
                        break;
                    }
                }
            }
            if(System.currentTimeMillis() - t > 50000) {
                break;
            }
        }
        
        assertNotNull(apiRepo.newQuery);
        if(openedTC == null) {
            fail("TopComponent with new query wasn't opened");
        }
        openedTC.close();
    }
    
    private APITestRepository getApiRepo() {
        return APITestKit.getAPIRepo(APITestRepository.ID);
    }

    private Repository getRepo() {
        return APITestKit.getRepo(APITestRepository.ID);
    }
    
    private APITestIssue getAPIIssue(String id) {
        return getApiRepo().getIssues(new String[] {id}).iterator().next();
    }

    private Issue getIssue(String id) {
        return getRepo().getIssues(id)[0];
    }
    
    private void assertOpened(APITestIssue apiIssue) throws InterruptedException {
        long t = System.currentTimeMillis();
        while(!apiIssue.wasOpened) {
            Thread.currentThread().sleep(200);
            if(System.currentTimeMillis() - t > 5000) {
                // timeout
                fail("issue wasn't opened");
            }
        }
    }    
}
