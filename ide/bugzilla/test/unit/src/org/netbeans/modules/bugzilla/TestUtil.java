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

package org.netbeans.modules.bugzilla;

import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaAttribute;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaClient;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
import org.eclipse.mylyn.internal.bugzilla.core.RepositoryConfiguration;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.bugtracking.APIAccessor;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;

/**
 *
 * @author tomas
 */
public class TestUtil implements TestConstants {
    public static void handleException(Exception exception) throws Throwable {
        if (exception instanceof CoreException) {
            CoreException e = (CoreException) exception;
            IStatus status = e.getStatus();
            if (status instanceof RepositoryStatus) {
                RepositoryStatus rs = (RepositoryStatus) status;
                String html = rs.getHtmlMessage();
                if(html != null && !html.trim().equals("")) {
//                    HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault ();
//                    if (displayer != null) {
//                        displayer.showURL (url);
//                    } else {
//                        //LOG.info("No URLDisplayer found.");
//                    }

                    final HtmlPanel p = new HtmlPanel();
                    p.setHtml(html);
                    BugzillaUtil.show(p, "html", "ok");
                }
                throw new Exception(rs.getHtmlMessage());
            }
            if (e.getStatus().getException() != null) {
                throw e.getStatus().getException();
            }
            if (e.getCause() != null) {
                throw e.getCause();
            }
            throw e;
        }
        exception.printStackTrace();
        throw exception;
    }

    public static TaskData createTaskData(BugzillaRepositoryConnector brc, TaskRepository repository, String summary, String desc, String typeName) throws MalformedURLException, CoreException {
        TaskAttributeMapper attributeMapper = brc.getTaskDataHandler().getAttributeMapper(repository);
        TaskData data = new TaskData(attributeMapper, repository.getConnectorKind(), repository.getRepositoryUrl(), "");
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.USER_ASSIGNED);
        ta = rta.createMappedAttribute(TaskAttribute.SUMMARY);
        ta.setValue(summary);
        ta = rta.createMappedAttribute(TaskAttribute.DESCRIPTION);
        ta.setValue(desc);

        BugzillaClient client = brc.getClientManager().getClient(repository, NULL_PROGRESS_MONITOR);
        RepositoryConfiguration rc = brc.getRepositoryConfiguration(repository, false, new NullProgressMonitor());
        String os = client.getRepositoryConfiguration().getOSs().get(0);
        ta = rta.createMappedAttribute(BugzillaAttribute.OP_SYS.getKey());
        ta.setValue(os);

        ta = rta.createMappedAttribute(BugzillaAttribute.PRODUCT.getKey());
        ta.setValue(TEST_PROJECT);

        String platform = client.getRepositoryConfiguration().getPlatforms().get(0);
        ta = rta.createMappedAttribute(BugzillaAttribute.REP_PLATFORM.getKey());
        ta.setValue(platform);

        String version = client.getRepositoryConfiguration().getVersions(TEST_PROJECT).get(0);
        ta = rta.createMappedAttribute(BugzillaAttribute.VERSION.getKey());
        ta.setValue(version);

        String component = client.getRepositoryConfiguration().getComponents(TEST_PROJECT).get(0);
        ta = rta.createMappedAttribute(BugzillaAttribute.COMPONENT.getKey());
        ta.setValue(component);

        return data;
    }

    public static RepositoryResponse postTaskData(BugzillaRepositoryConnector brc, TaskRepository repository, TaskData data) throws CoreException {
        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>(); // XXX what is this for
        return  brc.getTaskDataHandler().postTaskData(repository, data, attrs, NULL_PROGRESS_MONITOR);
    }

    public static TaskData getTaskData(TaskRepository taskRepository, String id) throws CoreException {
        BugzillaRepositoryConnector brc = Bugzilla.getInstance().getRepositoryConnector();
        return brc.getTaskData(taskRepository, id, new NullProgressMonitor());
    }

    public static String createIssue(BugzillaRepository repo, String summary) throws MalformedURLException, CoreException {
        BugzillaRepositoryConnector brc = Bugzilla.getInstance().getRepositoryConnector();
        TaskRepository tr = repo.getTaskRepository();
        TaskData data = TestUtil.createTaskData(brc, tr, summary, ISSUE_DESCRIPTION, ISSUE_SEVERITY);
        RepositoryResponse rr = TestUtil.postTaskData(brc, tr, data);
        return rr.getTaskId();
    }
//
//    public static RepositoryResponse addComment(BugzillaRepository repository, TaskData data, String comment) throws CoreException {
//        return addComment(repository.getTaskRepository(), id, comment);
//    }

    public static RepositoryResponse addComment(TaskRepository taskRepository, String id, String comment) throws CoreException {
        TaskData data = getTaskData(taskRepository, id);
        return addComment(taskRepository, data, comment);
    }

    public static RepositoryResponse addComment(TaskRepository taskRepository, TaskData data, String comment) throws CoreException {
        TaskAttribute ta = data.getRoot().createMappedAttribute(TaskAttribute.COMMENT_NEW);
        ta.setValue(comment);

        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>();
        attrs.add(ta);
        return Bugzilla.getInstance().getRepositoryConnector().getTaskDataHandler().postTaskData(taskRepository, data, attrs, new NullProgressMonitor());
    }

    public static BugzillaRepository getRepository(String name, String url, String user, String psswd) {
        RepositoryInfo info = new RepositoryInfo(name, BugzillaConnector.ID, url, name, name, user, null, psswd.toCharArray(), null);
        BugzillaRepository repo = new BugzillaRepository(info);
        repo.ensureCredentials();
        return repo;
    }

    public static void validate(BugzillaRepositoryConnector brc, TaskRepository repository) throws Throwable {
        try {
            brc.getClientManager().getClient(repository, NULL_PROGRESS_MONITOR).validate(NULL_PROGRESS_MONITOR);
        } catch (Exception ex) {
            handleException(ex);
        }
    }
    
    public static Query getQuery(BugzillaQuery bugzillaQuery) {
        Repository repository = BugzillaUtil.getRepository(bugzillaQuery.getRepository());
        return getQuery(repository, bugzillaQuery);
    }    
    
    public static Issue getIssue(BugzillaIssue bugzillaIssue) {
        Repository repository = BugzillaUtil.getRepository(bugzillaIssue.getRepository());
        return getIssue(repository, bugzillaIssue);
    }    
    
    private static Query getQuery(Repository repository, BugzillaQuery q) {
        RepositoryImpl repositoryImpl = APIAccessor.IMPL.getImpl(repository);
        QueryImpl impl = repositoryImpl.getQuery(q);
        if(impl == null) {
            return null;
        }
        return impl.getQuery();
    }    
    
    private static Issue getIssue(Repository repository, BugzillaIssue i) {
        RepositoryImpl repositoryImpl = APIAccessor.IMPL.getImpl(repository);
        IssueImpl impl = repositoryImpl.getIssue(i);
        return impl != null ? impl.getIssue() : null;
    } 
        
}
