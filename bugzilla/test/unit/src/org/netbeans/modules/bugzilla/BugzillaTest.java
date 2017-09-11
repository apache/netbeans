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

package org.netbeans.modules.bugzilla;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaAttribute;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaClient;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaTaskDataHandler;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataStore;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugzilla.util.FileUtils;

/**
 *
 * @author tomas
 */
public class BugzillaTest extends NbTestCase implements TestConstants {

    static NullProgressMonitor nullProgressMonitor = new NullProgressMonitor();
    private TaskRepository repository;
    private BugzillaRepositoryConnector brc;
    private TaskRepositoryManager trm;

    public BugzillaTest(String arg0) {
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
        repository = new TaskRepository("bugzilla", REPO_URL);
        AuthenticationCredentials authenticationCredentials = new AuthenticationCredentials(REPO_USER, REPO_PASSWD);
        repository.setCredentials(AuthenticationType.REPOSITORY, authenticationCredentials, false);
        repository.setCredentials(AuthenticationType.HTTP, authenticationCredentials, false);

        trm = new TaskRepositoryManager();
        brc = new BugzillaRepositoryConnector(new File(getWorkDir().getAbsolutePath(), "bugzillaconfiguration"));

        trm.addRepository(repository);
        trm.addRepositoryConnector(brc);

        WebUtil.init();
    }

    public void testBugzilla() throws Throwable {
//        TaskDataState state;
//        TaskData d;
        try {

            // create issue
            TaskData data = createIssue(brc, repository, "bug pruser", "pruser", "bug");

            TaskDataStore tds = new TaskDataStore(trm);

            // update issue
            updateTaskData(data, brc, repository);

            // hours worked
            timeTracking(data, brc, repository);
            
            // add atachment
            data = addAttachement(data, brc, repository, "Adding attachement", "some file", "crap");
            
            // read attachment
            readAttachement(data, brc, repository, "crap");

            // add comment
            String comment = "this is not a comment " + System.currentTimeMillis();
            RepositoryResponse rr = TestUtil.addComment(repository, data, comment);
            assertEquals(RepositoryResponse.ResponseKind.TASK_UPDATED, rr.getReposonseKind());

            // read comments
            readComment(data, brc, repository, comment);

            changeProduct(data, brc, repository);

            // resolve
            closeIssue(data, brc, repository);


        } catch (Exception e) {
            TestUtil.handleException(e);
        }
    }

    public static TaskData createIssue(BugzillaRepositoryConnector brc, TaskRepository repository, String summary, String desc, String typeName) throws CoreException, MalformedURLException {
        TaskData data = TestUtil.createTaskData(brc, repository, summary, desc, typeName);
        RepositoryResponse rr = TestUtil.postTaskData(brc, repository, data);
        String taskId = rr.getTaskId();
        data = brc.getTaskData(repository, taskId, nullProgressMonitor);
        assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_CREATED);
        assertNotNull(data);    

        return data;
    }

    private void assertChanged(Task task, TaskData data, boolean changed) {
        boolean hasChanged = brc.hasTaskChanged(repository, task, data);
        assertEquals(changed, hasChanged);
    }


    private void listIssues(BugzillaRepositoryConnector brc, TaskRepository repository, TaskData data) throws UnsupportedEncodingException {
        String dateString = repository.getSynchronizationTimeStamp();
        if (dateString == null) {
            dateString = "";
        }
//
//        IRepositoryQuery query = new RepositoryQuery(repository.getConnectorKind(), "");
//        String urlQueryBase = repository.getRepositoryUrl() + CHANGED_BUGS_CGI_QUERY
//					+ URLEncoder.encode(dateString, repository.getCharacterEncoding()) + CHANGED_BUGS_CGI_ENDDATE;

		String url = "/buglist.cgi?query_format=advanced&short_desc_type=allwordssubstr";
        IRepositoryQuery query = new RepositoryQuery(repository.getConnectorKind(), "");
		query.setUrl(url);
        final List<TaskData> collectedData = new ArrayList<TaskData>();
        TaskDataCollector collector = new TaskDataCollector() {
			public void accept(TaskData taskData) {
				collectedData.add(taskData);
			}
		};
		brc.performQuery(repository, query, collector, null, nullProgressMonitor);
        assertTrue(collectedData.size() > 0);
        String taskId = data.getTaskId();
        data = null;
        for (TaskData taskData : collectedData) {
            if(taskData.getTaskId().equals(taskId)) {
                data = taskData;
                break;
            }
        }
        assertNotNull(data);
    }

    private void readComment(TaskData data, BugzillaRepositoryConnector brc, TaskRepository repository, String comment) throws CoreException {
        // refresh
        data = brc.getTaskData(repository, data.getTaskId(), nullProgressMonitor);

        List<TaskAttribute> attributes = data.getAttributeMapper().getAttributesByType(data, TaskAttribute.TYPE_COMMENT);
        assertNotNull(attributes);
        
        boolean fail = true;
        for (TaskAttribute ta : attributes) {
            if(ta.getMappedAttribute(TaskAttribute.COMMENT_TEXT).getValue().equals(comment)) {
                fail = false;
                break;
            }
        }
        if(fail) {
            fail("Couldn't find comment text [" + comment + "] for taskdata [" + data.getTaskId() + "]");
        }
    }

    private void updateTaskData(TaskData data, BugzillaRepositoryConnector brc, TaskRepository repository) throws CoreException {
        data = brc.getTaskData(repository, data.getTaskId(), nullProgressMonitor);
		TaskAttribute attrModification1 = data.getRoot().getMappedAttribute(TaskAttribute.DATE_MODIFICATION);

        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.USER_ASSIGNED);
        ta = rta.getMappedAttribute(TaskAttribute.SUMMARY);
        String val = ta.getValue();
        ta.setValue(val + " updated");
        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>();
        attrs.add(ta);
        RepositoryResponse rr = brc.getTaskDataHandler().postTaskData(repository, data, attrs, new NullProgressMonitor());
        assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_UPDATED);

        data = brc.getTaskData(repository, data.getTaskId(), nullProgressMonitor);
        rta = data.getRoot();
        ta = rta.getMappedAttribute(TaskAttribute.SUMMARY);
        assertEquals(val + " updated", ta.getValue());

        TaskAttribute attrModification2 = data.getRoot().getMappedAttribute(TaskAttribute.DATE_MODIFICATION);
        assertNotSame(attrModification1, attrModification2);

    }
    
    private void timeTracking(TaskData data, BugzillaRepositoryConnector brc, TaskRepository repository) throws CoreException {
        data = brc.getTaskData(repository, data.getTaskId(), nullProgressMonitor);
		TaskAttribute attrModification1 = data.getRoot().getMappedAttribute(TaskAttribute.DATE_MODIFICATION);

        // Orig. Est.
        TaskAttribute taEstimatedTime = data.getRoot().getMappedAttribute(BugzillaAttribute.ESTIMATED_TIME.getKey());
        
        // Hours left
        TaskAttribute taRemainingTime = data.getRoot().getMappedAttribute(BugzillaAttribute.REMAINING_TIME.getKey());
        
        // Current Est.
        TaskAttribute taActualTime = data.getRoot().getMappedAttribute(BugzillaAttribute.ACTUAL_TIME.getKey());
        
        // Deadline
        TaskAttribute taDeadline = data.getRoot().getMappedAttribute(BugzillaAttribute.DEADLINE.getKey());
        
        // need a comment when changing Hours Worked
        TaskAttribute taComment = data.getRoot().createMappedAttribute(TaskAttribute.COMMENT_NEW);
        
        // Hours Worked
        TaskAttribute taWorkTime = data.getRoot().getMappedAttribute(BugzillaAttribute.WORK_TIME.getKey());
        assertNull(taWorkTime);
        taWorkTime = data.getRoot().createMappedAttribute(BugzillaAttribute.WORK_TIME.getKey());
        assertNotNull(taWorkTime);

        // estimate 10, work 3 => actuall == 3, 
        taEstimatedTime.setValue("10");
        taWorkTime.setValue("3");
        taRemainingTime.setValue("3");
        taActualTime.setValue("7"); // will have no effect on the returned value
        taComment.setValue("worked 3");
        taDeadline.setValue(new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis() + (24*60*60*1000))));
        
        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>();
        attrs.add(taEstimatedTime);
        attrs.add(taWorkTime);
        attrs.add(taComment);
        attrs.add(taActualTime);
        attrs.add(taRemainingTime);
        RepositoryResponse rr = brc.getTaskDataHandler().postTaskData(repository, data, attrs, new NullProgressMonitor());
        assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_UPDATED);

        data = brc.getTaskData(repository, data.getTaskId(), nullProgressMonitor);
        
        List<TaskAttribute> attributes = data.getAttributeMapper().getAttributesByType(data, TaskAttribute.TYPE_COMMENT);
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        taComment = attributes.get(0);
        TaskAttribute ta = taComment.getMappedAttribute(BugzillaAttribute.WORK_TIME.getKey());
        assertEquals("3.0", ta.getValue());
                
        ta = data.getRoot().getMappedAttribute(BugzillaAttribute.ESTIMATED_TIME.getKey());
        assertEquals("10.00", ta.getValue());
        ta = data.getRoot().getMappedAttribute(BugzillaAttribute.REMAINING_TIME.getKey());
        assertEquals("3.00", ta.getValue());
        ta = data.getRoot().getMappedAttribute(BugzillaAttribute.ACTUAL_TIME.getKey());
        assertEquals("3.00", ta.getValue());
        ta = data.getRoot().getMappedAttribute(BugzillaAttribute.DEADLINE.getKey());
        assertEquals(new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis() + (24*60*60*1000))), ta.getValue());

        
        // orig est == Hours left + Hours worked
        // Hours left 2 => actuall == 2 + 3; 
        
        // Hours left
        taRemainingTime = data.getRoot().getMappedAttribute(BugzillaAttribute.REMAINING_TIME.getKey());
        taWorkTime = data.getRoot().createMappedAttribute(BugzillaAttribute.WORK_TIME.getKey());
        taComment = data.getRoot().createMappedAttribute(TaskAttribute.COMMENT_NEW);;
        
        taRemainingTime.setValue("2"); 
        taWorkTime.setValue("2"); 
        taComment.setValue("worked 2");
        attrs = new HashSet<TaskAttribute>();
        attrs.add(taRemainingTime);
        attrs.add(taWorkTime);
        attrs.add(taComment);
        rr = brc.getTaskDataHandler().postTaskData(repository, data, attrs, new NullProgressMonitor());
        assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_UPDATED);

        data = brc.getTaskData(repository, data.getTaskId(), nullProgressMonitor);
        ta = data.getRoot().getMappedAttribute(BugzillaAttribute.REMAINING_TIME.getKey());
        assertEquals("2.00", ta.getValue());
        ta = data.getRoot().getMappedAttribute(BugzillaAttribute.ACTUAL_TIME.getKey());
        assertEquals("5.00", ta.getValue());
        ta = data.getRoot().getMappedAttribute(BugzillaAttribute.ESTIMATED_TIME.getKey());
        assertEquals("10.00", ta.getValue());
        
        TaskAttribute attrModification2 = data.getRoot().getMappedAttribute(TaskAttribute.DATE_MODIFICATION);
        assertNotSame(attrModification1, attrModification2);

    }

    private void changeProduct(TaskData data, BugzillaRepositoryConnector brc, TaskRepository repository) throws CoreException, IOException {
        data = brc.getTaskData(repository, data.getTaskId(), nullProgressMonitor);

        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.PRODUCT);

        BugzillaClient client = brc.getClientManager().getClient(repository, NULL_PROGRESS_MONITOR);
        
        List<String> products = client.getRepositoryConfiguration().getProducts();
        String newProject = TEST_PROJECT2;
        assertNotNull(newProject);
        ta.setValue(newProject);

        String version = client.getRepositoryConfiguration(NULL_PROGRESS_MONITOR).getVersions(newProject).get(0);
        ta = rta.getMappedAttribute(BugzillaAttribute.VERSION.getKey());
        ta.setValue(version);

        String component = client.getRepositoryConfiguration(NULL_PROGRESS_MONITOR).getComponents(newProject).get(0);
        ta = rta.getMappedAttribute(BugzillaAttribute.COMPONENT.getKey());
        ta.setValue(component);

        String milestone = client.getRepositoryConfiguration(NULL_PROGRESS_MONITOR).getTargetMilestones(newProject).get(0);
        ta = rta.getMappedAttribute(BugzillaAttribute.TARGET_MILESTONE.getKey());
        ta.setValue(milestone);

        ta = rta.getMappedAttribute(BugzillaAttribute.SET_DEFAULT_ASSIGNEE.getKey());
        if (ta != null) {
            ta.setValue("1"); 
        }

        ta = rta.getMappedAttribute(BugzillaAttribute.CONFIRM_PRODUCT_CHANGE.getKey());
        if (ta == null) {
            ta = BugzillaTaskDataHandler.createAttribute(rta, BugzillaAttribute.CONFIRM_PRODUCT_CHANGE);
        }

        if (ta != null) {
            ta.setValue("1");
        }

        RepositoryResponse rr = brc.getTaskDataHandler().postTaskData(repository, data, null, new NullProgressMonitor());
        assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_UPDATED);


    }

    private TaskData addAttachement(TaskData data,  BugzillaRepositoryConnector brc, TaskRepository repository, String comment, String desc, String content) throws Exception {
//        Task task = new Task(getRepository().getRepositoryUrl(), getRepository().getConnectorKind(), key, taskId, "");
        File f = getAttachmentFile(content);

        FileTaskAttachmentSource attachmentSource = new FileTaskAttachmentSource(f);
        attachmentSource.setContentType("text/plain");

//        List<TaskAttribute> attributes = data.getAttributeMapper().getAttributesByType(data, TaskAttribute.TYPE_ATTACHMENT);
        TaskAttribute attAttribute = new TaskAttribute(data.getRoot(),  TaskAttribute.TYPE_ATTACHMENT);
        TaskAttribute a = attAttribute.createMappedAttribute(TaskAttribute.ATTACHMENT_DESCRIPTION);
        a.setValue(desc);
        String bugId = data.getTaskId();
        brc.getClientManager().getClient(repository, nullProgressMonitor)
                .postAttachment(bugId, comment, attachmentSource, attAttribute, nullProgressMonitor);

        data = brc.getTaskData(repository, data.getTaskId(), nullProgressMonitor);
        List<TaskAttribute> attributes = data.getAttributeMapper().getAttributesByType(data, TaskAttribute.TYPE_ATTACHMENT);
        assertTrue(attributes.size() > 0);
        return data;
    }

    private void readAttachement(TaskData data,  BugzillaRepositoryConnector brc, TaskRepository repository, String content) throws Exception {
        // refresh
        data = brc.getTaskData(repository, data.getTaskId(), nullProgressMonitor);

        List<TaskAttribute> attributes = data.getAttributeMapper().getAttributesByType(data, TaskAttribute.TYPE_ATTACHMENT);
        TaskAttribute attribute = attributes.get(0);
        TaskAttachmentMapper attachment = TaskAttachmentMapper.createFrom(attribute);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        InputStream is = brc.getClientManager().getClient(repository, nullProgressMonitor).getAttachmentData(attachment.getAttachmentId(), nullProgressMonitor);
        FileUtils.copyStream(is, os);

        try {
//          byte[] d = new byte[4];
//          os.read(d);
			assertEquals(content, os.toString());
        } finally {
			if(os != null) os.close();
		}
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

    private void closeIssue(TaskData data, BugzillaRepositoryConnector brc, TaskRepository repository) throws Exception {
        //RepositoryConfiguration rc = brc.getClientManager().getClient(repository, nullProgressMonitor).getRepositoryConfiguration();

        // refresh
        data = brc.getTaskData(repository, data.getTaskId(), nullProgressMonitor);

        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>();
        TaskAttribute rta = data.getRoot();

        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.OPERATION);
        ta.setValue("resolve");
        attrs.add(ta);

        ta = rta.getMappedAttribute(TaskAttribute.STATUS);
        ta.setValue("FIXED");
        attrs.add(ta);

        RepositoryResponse rr = brc.getTaskDataHandler().postTaskData(repository, data, attrs, nullProgressMonitor);
        assertEquals(rr.getReposonseKind(), RepositoryResponse.ResponseKind.TASK_UPDATED);

        data = brc.getTaskData(repository, rr.getTaskId(), nullProgressMonitor);
        rta = data.getRoot();
        ta = rta.getMappedAttribute(TaskAttribute.STATUS);
        assertEquals("RESOLVED", ta.getValue());
    }


    // XXX how to get task!!!
    // XXX TaskTask isn't working - returns taskId instead of taskKey
    private class Task extends AbstractTask {
        private String key;

        public Task(String repositoryUrl, String key, String taskId, String summary) {
            super(repositoryUrl, taskId, summary);
            this.key = key;
        }

        @Override
        public boolean isLocal() {
            return true;
        }

        @Override
        public String getConnectorKind() {
            return repository.getConnectorKind();
        }

        @Override
        public String getTaskKey() {
            return key;
        }
    }
}
