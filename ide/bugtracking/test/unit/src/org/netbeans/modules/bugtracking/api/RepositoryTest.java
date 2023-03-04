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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.TestIssue;
import org.openide.util.test.MockLookup;

/**
 *
 * @author tomas
 */
public class RepositoryTest extends NbTestCase {

    public RepositoryTest(String arg0) {
        super(arg0);
    }

    public static junit.framework.Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new RepositoryTest("testAttributes"));
        suite.addTest(new RepositoryTest("testCreateIssueSumDesc"));
        suite.addTest(new RepositoryTest("testUrlChanged"));
        suite.addTest(new RepositoryTest("testCanAttachFiles"));
        suite.addTest(new RepositoryTest("testQueryListChanged"));
        suite.addTest(new RepositoryTest("testDisplayNameChanged"));
        suite.addTest(new RepositoryTest("testGetQueries"));
        suite.addTest(new RepositoryTest("testGetIssues"));
        suite.addTest(new RepositoryTest("testIsMutable"));    
        return suite;
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
    protected void tearDown() throws Exception {   
    }

    public void testAttributes() {
        Repository repo = getRepo();        
        assertEquals(APITestRepository.DISPLAY_NAME, repo.getDisplayName());
        assertEquals(APITestRepository.TOOLTIP, repo.getTooltip());
        assertEquals(APITestRepository.URL, repo.getUrl());
        assertEquals(APITestRepository.ID, repo.getId());
        assertEquals(APITestRepository.ICON, repo.getIcon());
    }
    
    public void testCreateIssueSumDesc() {
        APITestRepository apiRepo = getApiRepo();
        final String summary = "testsum";
        final String desc = "testdesc";
        TestIssue issue = apiRepo.createIssue(summary, desc);
        assertEquals(summary, issue.getSummary());
        assertEquals(desc, issue.getDescription());
    }
    
    public void testGetQueries() {
        APITestRepository apiRepo = getApiRepo();
        Repository repo = getRepo();
        assertEquals(apiRepo.getQueries().size(), repo.getQueries().size());
    }
    
    public void testGetIssues() {
        APITestRepository apiRepo = getApiRepo();
        Repository repo = getRepo();
        String[] ids = new String[] {APITestIssue.ID_1, APITestIssue.ID_2};
        assertEquals(apiRepo.getIssues(ids).size(), repo.getIssues(ids).length);
    }
    
//    public void testSimpleSearch() {
//        APITestRepository apiRepo = getApiRepo();
//        Repository repo = getRepo();
//        assertEquals(apiRepo.simpleSearch(APITestIssue.ID_1).size(), repo.(APITestIssue.ID_1).);
//    }
    
    public void testIsMutable() {
        Repository repo = getRepo();
        String[] ids = new String[] {APITestIssue.ID_1, APITestIssue.ID_2};
        assertEquals(true, repo.isMutable());
    }
    
    public void testCanAttachFiles() {
        Repository repo = getRepo();
        
        getApiRepo().canAttachFiles = false;
        assertFalse(repo.canAttachFiles());
        getApiRepo().canAttachFiles = true;
        assertTrue(repo.canAttachFiles());
    }
    
    public void testQueryListChanged() {
        Repository repo = getRepo();
        APITestRepository apiTestRepo = getApiRepo();
        
        final boolean[] received = new boolean[] {false};
        final PropertyChangeListener l = new PropertyChangeListener() {
              @Override
              public void propertyChange(PropertyChangeEvent pce) {
                  if(Repository.EVENT_QUERY_LIST_CHANGED.equals(pce.getPropertyName())) {
                      received[0] = true;
                  }
              }
          };
        repo.addPropertyChangeListener(l);
        apiTestRepo.fireQueryChangeEvent();
        assertTrue(received[0]);
        
        repo.removePropertyChangeListener(l);
        received[0] = false;
        apiTestRepo.fireQueryChangeEvent();
        assertFalse(received[0]);
    }

    public void testDisplayNameChanged() throws IOException {
        final String newDisplayName = "newDisplayName";
        APITestRepository apiTestRepo = getApiRepo();
        apiTestRepo.getController().setDisplayName(newDisplayName);
        testAttributeChange(RepositoryImpl.ATTRIBUTE_DISPLAY_NAME, APITestRepository.DISPLAY_NAME, newDisplayName);
    }
    
    public void testUrlChanged() throws IOException {
        final String newURL = "http://test/newUrl/";
        APITestRepository apiTestRepo = getApiRepo();
        apiTestRepo.getController().setURL(newURL);
        testAttributeChange(RepositoryImpl.ATTRIBUTE_URL, APITestRepository.URL, newURL);
    }
    
    private void testAttributeChange(final String key, final String expectedOldValue, final String expectedNewValue) throws IOException {
        final Repository repo = getRepo();
        
        final boolean[] received = new boolean[] {false};
        final PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if(Repository.EVENT_ATTRIBUTES_CHANGED.equals(pce.getPropertyName())) {
                    try {
                        received[0] = true;

                        Map<String, String> oldM = (Map<String, String>) pce.getOldValue();
                        Map<String, String> newM = (Map<String, String>) pce.getNewValue();
                        String oldValue = oldM.get(key);
                        String newValue = newM.get(key);

                        assertEquals(expectedOldValue, oldValue);
                        assertEquals(expectedNewValue, newValue);
                    } catch (Exception e) {
                        repo.removePropertyChangeListener(this);
                    }
                }
            }
        };
        repo.addPropertyChangeListener(propertyChangeListener);
        try {
            APIAccessorImpl.IMPL.getImpl(repo).applyChanges();
        } finally {
            repo.removePropertyChangeListener(propertyChangeListener);
        }
        assertTrue(received[0]);
    }

    private APITestRepository getApiRepo() {
        return APITestKit.getAPIRepo(APITestRepository.ID);
    }

    private Repository getRepo() {
        return APITestKit.getRepo(APITestRepository.ID);
    }
    
}
