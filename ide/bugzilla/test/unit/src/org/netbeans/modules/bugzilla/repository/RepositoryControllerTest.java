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

package org.netbeans.modules.bugzilla.repository;

import java.io.IOException;
import org.netbeans.modules.bugzilla.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.spi.*;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.openide.util.test.MockLookup;

/**
 *
 * @author tomas
 */
public class RepositoryControllerTest extends NbTestCase implements TestConstants {

    private static String REPO_NAME; 

    public RepositoryControllerTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        REPO_NAME = "Beautiful-" + System.currentTimeMillis();
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        MockLookup.setLayersAndInstances();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testController() throws Throwable {
        BugzillaConnector bc = getConnector();
        BugzillaRepository repo = new BugzillaRepository();      
        BugzillaRepositoryController c = getController(repo);
        
        // populate
        // only name
        populate(c, REPO_NAME, "", "", "");
        assertFalse(c.isValid());

        // only url
        populate(c, "", REPO_URL, "", "");
        assertFalse(c.isValid());

        // only user
        populate(c, "", "", REPO_USER, "");
        assertFalse(c.isValid());

        // only passwd
        populate(c, "", "", "", REPO_PASSWD);
        assertFalse(c.isValid());

        // only user & passwd
        populate(c, "", "", REPO_USER, REPO_PASSWD);
        assertFalse(c.isValid());

        // name & url
        populate(c, REPO_NAME, REPO_URL, "", "");
        assertTrue(c.isValid());

        // full house
        populate(c, REPO_NAME, REPO_URL, REPO_USER, REPO_PASSWD);
        assertTrue(c.isValid());

        // no crap, its valid!
        c.applyChanges();
        try {
            Bugzilla.getInstance().getRepositoryConnector().getClientManager().getClient(repo.getTaskRepository(), NULL_PROGRESS_MONITOR).validate(NULL_PROGRESS_MONITOR);
        } catch (CoreException ex) {
            TestUtil.handleException(ex);
        } catch (IOException ex) {
            TestUtil.handleException(ex);
        }
    }

    public void testControllerOnValidate() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, Throwable {
        BugzillaRepository repo = new BugzillaRepository();
        BugzillaRepositoryController c = getController(repo);

        checkOnValidate(c, REPO_NAME, REPO_URL, null, REPO_PASSWD, true);

        checkOnValidate(c, REPO_NAME, REPO_URL, "", REPO_PASSWD, true);

        checkOnValidate(c, REPO_NAME, REPO_URL, "xxx", REPO_PASSWD, false);

        checkOnValidate(c, REPO_NAME, REPO_URL, REPO_USER, null, true);

        checkOnValidate(c, REPO_NAME, REPO_URL, REPO_USER, "", true);

        checkOnValidate(c, REPO_NAME, REPO_URL, REPO_USER, "xxx", true);

        checkOnValidate(c, REPO_NAME, REPO_URL, REPO_USER, REPO_PASSWD, true);
    }

    private void checkOnValidate(BugzillaRepositoryController c, String repoName, String repoUrl, String user, String psswd, boolean assertWorked) throws Throwable {

        populate(c, repoName, repoUrl, user, psswd); //
        assertTrue(c.isValid());

        LogHandler lh = new LogHandler("validate for", LogHandler.Compare.STARTS_WITH);
        LogHandler lhAutoupdate = new LogHandler("AutoupdateSupport.checkAndNotify start", LogHandler.Compare.ENDS_WITH);
        onValidate(c);
        lh.waitUntilDone();
        assertFalse(lhAutoupdate.isDone());
        lhAutoupdate.reset();
        String msg = lh.getInterceptedMessage();
        boolean worked = msg.indexOf("ok.") > -1;
        if(assertWorked) {
            assertTrue(worked);
        } else {
            assertFalse(worked);
        }
    }

    private BugzillaConnector getConnector() {
        BugtrackingConnector[] c = BugtrackingUtil.getBugtrackingConnectors();
        BugzillaConnector bc = null;
        for (BugtrackingConnector bugtrackingConnector : c) {
            if(bugtrackingConnector instanceof BugzillaConnector) {
                bc = (BugzillaConnector) bugtrackingConnector;
                break;
            }
        }
        assertNotNull(bc);
        return bc;
    }

    private BugzillaRepositoryController getController(BugzillaRepository repo) {
        assertNotNull(repo);
        RepositoryController c = repo.getController();
        assertNotNull(c);
        assertFalse(c.isValid());
        return (BugzillaRepositoryController) c;
    }

    private RepositoryPanel getRepositoryPanel(RepositoryController c) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = c.getClass().getDeclaredField("panel");
        f.setAccessible(true);
        return (RepositoryPanel) f.get(c);
    }

    private void populate(BugzillaRepositoryController c, String name, String url, String user, String psswd) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        RepositoryPanel panel = getRepositoryPanel(c);
        resetPanel(panel);        
        panel.nameField.setText(name);
        panel.urlField.setText(url);
        panel.userField.setText(user);
        panel.psswdField.setText(psswd);
        setPopulated(c);
    }

    private void resetPanel(RepositoryPanel panel) {
        panel.nameField.setText("");
        panel.urlField.setText("");
        panel.userField.setText("");
        panel.psswdField.setText("");
    }

    private void onValidate(BugzillaRepositoryController c) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method m = c.getClass().getDeclaredMethod("onValidate");
        m.setAccessible(true);
        m.invoke(c);
    }
    
    private void setPopulated(BugzillaRepositoryController c) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
        Field f = c.getClass().getDeclaredField("populated");
        f.setAccessible(true);
        f.set(c, true);
    }
}
