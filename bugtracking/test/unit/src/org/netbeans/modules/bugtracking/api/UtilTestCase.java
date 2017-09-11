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

import java.util.Set;
import java.util.logging.Level;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;
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
