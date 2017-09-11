/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import static junit.framework.Assert.assertTrue;
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
