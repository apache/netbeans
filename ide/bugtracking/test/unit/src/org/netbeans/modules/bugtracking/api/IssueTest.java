/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.bugtracking.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.APIAccessor;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.ui.issue.IssueTopComponent;
import org.openide.util.test.MockLookup;

/**
 *
 * @author tomas
 */
public class IssueTest extends NbTestCase {

    public IssueTest(String arg0) {
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

    public void testGetAttributes() {
        APITestIssue apiIssue = getAPIIssue();
        Issue issue = getIssue();
        
        assertEquals(apiIssue.getID(), issue.getID());
        assertEquals(apiIssue.getDisplayName(), issue.getDisplayName());
        assertEquals(apiIssue.getTooltip(), issue.getTooltip());
        assertEquals(apiIssue.getSummary(), issue.getSummary());
    }
    
    public void testGetShortenedDisplayName() {
        Issue issue = getIssue();
        
        assertEquals(IssueImpl.SHORT_DISP_NAME_LENGTH + 3, issue.getShortenedDisplayName().length());
        assertTrue(issue.getShortenedDisplayName().endsWith("..."));
    }
    
    public void testGetRepository() {
        assertEquals(getRepo(), getIssue().getRepository());
    }
    
    public void testIsFinished() {
        APITestIssue apiIssue = getAPIIssue();
        Issue issue = getIssue();
        
        assertFalse(issue.isFinished());
        apiIssue.idFinished = true;
        assertTrue(issue.isFinished());
    }
    
    public void testRefresh() {
        APITestIssue apiIssue = getAPIIssue();
        Issue issue = getIssue();
        
        apiIssue.wasRefreshed = false;
        issue.refresh();
        assertTrue(apiIssue.wasRefreshed);
    }
    
    /**
     * invoked from BugtrackingViewsTest
     * @throws InterruptedException 
     */
    public void _testOpen() throws InterruptedException {
        APITestIssue apiIssue = getAPIIssue();
        Issue issue = getIssue();
        
        apiIssue.wasOpened = false;
        issue.open();
        assertOpened(apiIssue);
        
        IssueTopComponent tc = IssueTopComponent.find(APIAccessor.IMPL.getImpl(issue));
        assertNotNull(tc);
        tc.close();
        
        apiIssue.wasRefreshed = false;
        apiIssue.wasOpened = false;
        issue.open();
        assertOpened(apiIssue);
    }
    
    public void testAddedComment() {
        APITestIssue apiIssue = getAPIIssue();
        Issue issue = getIssue();
        
        apiIssue.wasClosedOnComment = false;
        apiIssue.addedComment = null;
        
        String comment = "cmt";
        boolean refresh = true;
        issue.addComment(comment, refresh);
        assertTrue(apiIssue.wasClosedOnComment);
        assertEquals(comment, apiIssue.addedComment);
        
        comment = "cmt2";
        refresh = false;
        issue.addComment(comment, refresh);
        assertFalse(apiIssue.wasClosedOnComment);
        assertEquals(comment, apiIssue.addedComment);
    }
    
    public void testAttachPatch() {
        APITestIssue apiIssue = getAPIIssue();
        Issue issue = getIssue();
        
        apiIssue.attachedFile = null;
        apiIssue.attachedPatchDesc = null;
        
        String desc = "desc";
        File file = new File("somefile");
        issue.attachFile(file, desc, false);
        assertEquals(desc, apiIssue.attachedPatchDesc);
        assertEquals(file, apiIssue.attachedFile);
        
        desc = "desc2";
        file = new File("somefile2");
        issue.attachFile(file, desc, false);
        assertEquals(desc, apiIssue.attachedPatchDesc);
        assertEquals(file, apiIssue.attachedFile);
    }
    
    public void testPCL() {
        APITestIssue apiIssue = getAPIIssue();
        Issue issue = getIssue();
        
        final boolean refreshed[] = new boolean[] {false};
        PropertyChangeListener l = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                refreshed[0] = true;
            }
        };
        
        issue.addPropertyChangeListener(l);
        apiIssue.wasRefreshed = false;
        issue.refresh();
        assertTrue(apiIssue.wasRefreshed);
        assertTrue(refreshed[0]);
        
        refreshed[0] = false;
        issue.removePropertyChangeListener(l);
        assertFalse(refreshed[0]);
    }
    
//    public void testGetSubtasks() {
//        APITestIssue apiIssue = getAPIIssue();
//        Issue issue = getIssue();
//        
//        assertEquals(apiIssue.getSubtasks().length, issue.getSubtasks());
//    }
    
    private APITestRepository getApiRepo() {
        return APITestKit.getAPIRepo(APITestRepository.ID);
    }

    private Repository getRepo() {
        return APITestKit.getRepo(APITestRepository.ID);
    }

    private APITestIssue getAPIIssue() {
        return getApiRepo().getIssues(new String[] {APITestIssue.ID_1}).iterator().next();
    }
    
    private Issue getIssue() {
        return getRepo().getIssues(APITestIssue.ID_1)[0];
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
