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

package org.netbeans.modules.bugzilla.issue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.eclipse.core.runtime.CoreException;
import org.netbeans.modules.bugzilla.*;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.repository.IssueField;
import org.netbeans.modules.bugzilla.repository.RepositoryTest;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.netbeans.modules.mylyn.util.NbTask;
import org.netbeans.modules.mylyn.util.SubmitCommand;
import org.openide.util.test.MockLookup;

/**
 *
 * @author tomas
 */
public class IssueTest extends NbTestCase implements TestConstants {

    private static final String REPO_NAME = "Beautiful";

    public IssueTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        // reset
        Method m = MylynSupport.class.getDeclaredMethod("reset", new Class[0]);
        m.setAccessible(true);
        m.invoke(MylynSupport.class);
                
        Field f = Bugzilla.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(Bugzilla.class, null);
        
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        MockLookup.setLayersAndInstances();
        BugtrackingUtil.getBugtrackingConnectors(); // ensure conector
    }

    public static Test suite () {
        return NbModuleSuite.createConfiguration(IssueTest.class).gui(false).suite();
    }

    public void testIsFinished() throws Throwable {
        BugzillaRepository br = TestUtil.getRepository("test", REPO_URL, REPO_USER, REPO_PASSWD);
        String id = TestUtil.createIssue(br, "testIsFinished ");
        BugzillaIssue issue = (BugzillaIssue) getRepository().getIssue(id);
        assertFalse(issue.isFinished());
        issue.resolve("FIXED");
        issue.submitAndRefresh();
        assertTrue(issue.isFinished());
    }

    public void testCC() throws Throwable {
        // WARNING: the test assumes that there are more than one value
        // for atributes like platform, versions etc.

        long ts = System.currentTimeMillis();
        String summary = "somary" + ts;
        String id = TestUtil.createIssue(getRepository(), summary);
        BugzillaIssue issue = (BugzillaIssue) getRepository().getIssue(id);
        openTask(issue);
        assertEquals(summary, issue.getFieldValue(IssueField.SUMMARY));

        resetStatusValues(issue);

        BugzillaRepository repository = getRepository();
        BugzillaRepositoryConnector brc = new BugzillaRepositoryConnector(new File(getWorkDir().getAbsolutePath(), "bugzillaconfiguration"));
        NbTask task = BugzillaUtil.getTask(repository, id, false);
        TaskData td = task.getTaskDataState().getRepositoryData();

        // add a cc
        assertNotSame(REPO_USER, issue.getFieldValue(IssueField.CC));
        setFieldValue(td, IssueField.NEWCC, REPO_USER);
        getRepository().getExecutor().execute(new SubmitCommand(brc, getRepository().getTaskRepository(), td));
        issue.refresh();
        
        assertEquals(REPO_USER, issue.getFieldValue(IssueField.CC));
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.CC);

        resetStatusValues(issue);

        // add new cc
        td = task.getTaskDataState().getRepositoryData();
        setFieldValue(td, IssueField.NEWCC, REPO_USER2);
        getRepository().getExecutor().execute(new SubmitCommand(brc, getRepository().getTaskRepository(), td));
        issue.refresh();

        List<String> ccs = issue.getFieldValues(IssueField.CC);
        assertEquals(2, ccs.size());
        assertTrue(ccs.contains(REPO_USER));
        assertTrue(ccs.contains(REPO_USER2));
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.CC);

        resetStatusValues(issue);
        
        // add two cc-s at once
        td = task.getTaskDataState().getRepositoryData();
        setFieldValue(td, IssueField.NEWCC, REPO_USER3 + ", " + REPO_USER4);
        getRepository().getExecutor().execute(new SubmitCommand(brc, getRepository().getTaskRepository(), td));
        issue.refresh();

        ccs = issue.getFieldValues(IssueField.CC);
        assertEquals(4, ccs.size());
        assertTrue(ccs.contains(REPO_USER));
        assertTrue(ccs.contains(REPO_USER2));
        assertTrue(ccs.contains(REPO_USER3));
        assertTrue(ccs.contains(REPO_USER4));
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.CC);

        resetStatusValues(issue);

        // remove a cc
        td = task.getTaskDataState().getRepositoryData();
        ccs = new ArrayList<String>();
        ccs.add(REPO_USER4);
        ccs.add(REPO_USER);
        setFieldValues(td, IssueField.REMOVECC, ccs);
        getRepository().getExecutor().execute(new SubmitCommand(brc, getRepository().getTaskRepository(), td));
        issue.refresh();

        ccs = issue.getFieldValues(IssueField.CC);
        assertEquals(2, ccs.size());
        assertTrue(ccs.contains(REPO_USER2));
        assertTrue(ccs.contains(REPO_USER3));
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.CC);

        resetStatusValues(issue);

        // remove all
        td = task.getTaskDataState().getRepositoryData();
        ccs = new ArrayList<String>();
        ccs.add(REPO_USER3);
        ccs.add(REPO_USER2);        
        setFieldValues(td, IssueField.REMOVECC, ccs);
        getRepository().getExecutor().execute(new SubmitCommand(brc, getRepository().getTaskRepository(), td));
        issue.refresh();
        ccs = issue.getFieldValues(IssueField.CC);
        assertEquals(0, ccs.size());
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.CC);

    }

    // XXX test new issue

    private void addHandler(LogHandler lh) {
        BugtrackingManager.LOG.addHandler(lh);
    }

    private void assertStatus(int expectedStatus, BugzillaIssue issue, IssueField f) throws IllegalAccessException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException {
        int status = issue.getFieldStatus(f);
        //System.out.println(issue.getID() + " " + f + " " + getName(expectedStatus) + " " + getName(status) + " " + "[" + issue.getFieldValue(f) + "] vs [" + getSeenValue(issue, f) + "]");
        if(status != expectedStatus) {
            fail("expected [" + getName(expectedStatus) + "], " +
                 "was [" + getName(status)+ "] " +
                 "because of value [" + issue.getFieldValue(f) + "] ");
        }
    }

    private String getName(int s) {
        switch(s) {
            case BugzillaIssue.FIELD_STATUS_IRELEVANT:
                return "Irelevant";
            case BugzillaIssue.FIELD_STATUS_MODIFIED :
                return "Modified";
            case BugzillaIssue.FIELD_STATUS_UPTODATE :
                return "Uptodate";
            case BugzillaIssue.FIELD_STATUS_OUTGOING :
                return "Outgoing";
            case BugzillaIssue.FIELD_STATUS_CONFLICT :
                return "Conflict";
            default :
                throw new IllegalStateException("Wrong status " + s);
        }
    }

    private BugzillaRepository getRepository() {
        return TestUtil.getRepository(REPO_NAME, REPO_URL, REPO_USER, REPO_PASSWD);
    }

    private String getOtherKeyword(BugzillaIssue issue) throws IOException, CoreException {
        List<String> l = getRepository().getConfiguration().getKeywords();
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.KEYWORDS), IssueField.KEYWORDS.getKey());
    }

    private String getOtherMilestone(BugzillaIssue issue, String product) throws IOException, CoreException {
        List<String> l = getRepository().getConfiguration().getTargetMilestones(product);
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.MILESTONE), IssueField.MILESTONE.getKey());
    }

    private String getOtherMilestone(BugzillaIssue issue) throws IOException, CoreException {
        return getOtherMilestone(issue, TEST_PROJECT);
    }

    private String getOtherPlatform(BugzillaIssue issue) throws IOException, CoreException {
        List<String> l = getRepository().getConfiguration().getPlatforms();
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.PLATFORM), IssueField.PLATFORM.getKey());
    }

    private String getOtherProduct(BugzillaIssue issue) throws IOException, CoreException {
        List<String> l = getRepository().getConfiguration().getProducts();
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.PRODUCT), IssueField.PRODUCT.getKey());
    }

    private String getOtherPriority(BugzillaIssue issue) throws IOException, CoreException {
        List<String> l = getRepository().getConfiguration().getPriorities();
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.PRIORITY), IssueField.PRIORITY.getKey());
    }

    private String getOtherVersion(BugzillaIssue issue, String product) throws IOException, CoreException {
        List<String> l = getRepository().getConfiguration().getVersions(product);
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.VERSION), IssueField.VERSION.getKey());
    }

    private String getOtherVersion(BugzillaIssue issue) throws IOException, CoreException {
        return getOtherVersion(issue, TEST_PROJECT);
    }

    private String getOtherSeverity(BugzillaIssue issue) throws IOException, CoreException {
        List<String> l = getRepository().getConfiguration().getSeverities();
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.SEVERITY), IssueField.SEVERITY.getKey());
    }

    private String getOtherResolution(BugzillaIssue issue) throws IOException, CoreException {
        List<String> l = getRepository().getConfiguration().getResolutions();
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.RESOLUTION), IssueField.RESOLUTION.getKey());
    }

    private String getOtherComponent(BugzillaIssue issue, String project) throws IOException, CoreException {
        List<String> l = getRepository().getConfiguration().getComponents(project);
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.COMPONENT), IssueField.COMPONENT.getKey());
    }

    private String getOtherComponent(BugzillaIssue issue) throws IOException, CoreException {
        return getOtherComponent(issue, TEST_PROJECT);
    }

    private String getDifferentServerValue(List<String> l, String v, String field) {
        if(v != null) {
            for (String s : l) {
                if(!s.equals(v)) {
                    return s;
                }
            }
            fail("there is no different value for field " + field + "  then [" + v + "] on the server.");
        }
        return l.get(0);
    }

    private void resetStatusValues(BugzillaIssue issue) throws InterruptedException, SecurityException, IOException, IllegalAccessException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException {
        issue.setUpToDate(true);
        issue.closed();
        openTask(issue);
        for (IssueField f : issue.getRepository().getConfiguration().getFields()) {
            // seen -> everything's uptodate
            assertStatus(BugzillaIssue.FIELD_STATUS_UPTODATE, issue, f);
        }
    }

    private void submit(BugzillaIssue issue) throws Throwable {
        issue.submitAndRefresh();
    }

    private File getAttachmentFile(String content) throws Exception {
        FileWriter fw = null;
        File f = null;
        try {
            f = File.createTempFile("bugzillatest", null);
            f.deleteOnExit();
            try {
                f.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                // ignore
            }
            fw = new FileWriter(f);
            fw.write(content);
            fw.flush();
            return f;
        } finally {
            try { if (fw != null) fw.close(); } catch (IOException iOException) { }
        }
    }

    private void openTask (BugzillaIssue issue) throws InterruptedException {
        // make sure model is loaded
        issue.opened();
        for (int i = 0; i < 10; ++i) {
            if (!issue.getFieldValue(IssueField.SUMMARY).isEmpty()) {
                break;
            }
            Thread.sleep(1000);
        }
    }

    private void setFieldValue (TaskData td, IssueField f, String value) {
        TaskAttribute a = td.getRoot().getMappedAttribute(f.getKey());
        if(a == null) {
            a = new TaskAttribute(td.getRoot(), f.getKey());
        }
        a.setValue(value);
    }

    private void setFieldValues (TaskData td, IssueField f, List<String> values) {
        TaskAttribute a = td.getRoot().getMappedAttribute(f.getKey());
        if(a == null) {
            a = new TaskAttribute(td.getRoot(), f.getKey());
        }
        a.setValues(values);
    }

    private class LogHandler extends Handler {
        private final String msg;
        private boolean done = false;
        public LogHandler(String msg) {
            this.msg = msg;
        }

        @Override
        public void publish(LogRecord record) {
            if(!done) done = record.getMessage().startsWith(msg);
        }
        @Override
        public void flush() { }
        @Override
        public void close() throws SecurityException { }
    }
}
