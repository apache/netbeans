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

package org.netbeans.modules.bugzilla.commands;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.bugzilla.*;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import static org.netbeans.modules.bugzilla.TestConstants.REPO_PASSWD;
import static org.netbeans.modules.bugzilla.TestConstants.REPO_URL;
import static org.netbeans.modules.bugzilla.TestConstants.REPO_USER;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.issue.IssueTestUtils;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.mylyn.util.MylynSupport;

/**
 *
 * @author tomas
 */
public class ExceptionHandlerTest extends NbTestCase implements TestConstants {
    public static final String EXCEPTION_HANDLER_CLASS_NAME = "org.netbeans.modules.bugzilla.commands.BugzillaExecutor$ExceptionHandler";
    private TaskRepositoryManager trm;
    private BugzillaRepositoryConnector brc;
    private ProxySelector defaultPS;
    
    public ExceptionHandlerTest(String arg0) {
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
        
        // reset
        Method m = MylynSupport.class.getDeclaredMethod("reset", new Class[0]);
        m.setAccessible(true);
        m.invoke(MylynSupport.class);
                
        Field f = Bugzilla.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(Bugzilla.class, null);
        
        brc = Bugzilla.getInstance().getRepositoryConnector();
        
        WebUtil.init();
        
        if (defaultPS == null) {
            defaultPS = ProxySelector.getDefault();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        ProxySelector.setDefault(defaultPS);         
    }
    
    @RandomlyFails
    public void testIsLoginHandler() throws Throwable {
        RepositoryInfo info = new RepositoryInfo("bgzll", BugzillaConnector.ID, REPO_URL, "bgzll", "bgzll", "XXX", null , "XXX".toCharArray(), null);
        BugzillaRepository repository = new BugzillaRepository(info);
        repository.ensureCredentials();
        assertHandler(repository, "LoginHandler");

        info = new RepositoryInfo("bgzll", BugzillaConnector.ID, REPO_URL, "bgzll", "bgzll", REPO_USER, null , "XXX".toCharArray(), null);
        repository = new BugzillaRepository(info);
        repository.ensureCredentials();
        assertHandler(repository, "LoginHandler");
        
    }

    public void testIsNotFoundHandler() throws Throwable {
        ProxySelector ps = new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                return Collections.singletonList(Proxy.NO_PROXY);
            }
            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        ProxySelector.setDefault(ps); 
        
        RepositoryInfo info = new RepositoryInfo("bgzll", BugzillaConnector.ID, "http://crap", "bgzll", "bgzll", null, null, null , null);
        BugzillaRepository repository = new BugzillaRepository(info);
        repository.ensureCredentials();
        assertHandler(repository, "NotFoundHandler");
    }

    public void testIsDefaultHandler() throws Throwable {
        ProxySelector ps = new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                return Collections.singletonList(Proxy.NO_PROXY);
            }
            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        ProxySelector.setDefault(ps); 
        
        RepositoryInfo info = new RepositoryInfo("bgzll", BugzillaConnector.ID, "dil://dil.com", "bgzll", "bgzll", null, null, null , null);
        BugzillaRepository repository = new BugzillaRepository(info);
        repository.ensureCredentials();
        assertHandler(repository, "DefaultHandler");

        info = new RepositoryInfo("bgzll", BugzillaConnector.ID, "crap", "bgzll", "bgzll", null, null, null , null);
        repository = new BugzillaRepository(info);
        repository.ensureCredentials();
        assertHandler(repository, "DefaultHandler");

        // XXX need more tests
    }
    
    public void testIsMidAirHandler() throws Throwable {
        BugzillaRepository repository = TestUtil.getRepository("test", REPO_URL, REPO_USER, REPO_PASSWD);
        
        String id = TestUtil.createIssue(repository, "testIsMidairHandler");
        
        TaskData data1 = TestUtil.getTaskData(repository.getTaskRepository(), id);
        TaskAttribute ta1 = data1.getRoot().createMappedAttribute(TaskAttribute.COMMENT_NEW);
        ta1.setValue("comment1");
        
        // add comment bypassing data1 to cause a midair collision
        TaskData data2 = TestUtil.getTaskData(repository.getTaskRepository(), id);
        TaskAttribute ta2 = data2.getRoot().createMappedAttribute(TaskAttribute.COMMENT_NEW);
        ta2.setValue("midairingcomment");
        TestUtil.postTaskData(brc, repository.getTaskRepository(), data2);
        
        try {
            TestUtil.postTaskData(brc, repository.getTaskRepository(), data1);
        } catch (CoreException ex) {
            assertEquals(EXCEPTION_HANDLER_CLASS_NAME + "$" + "MidAirHandler", getHandler(repository, ex).getClass().getName());
        } catch (Exception ex) {
            TestUtil.handleException(ex);
        }
    }
    
    public void testRefreshConfigOnMidAir() throws Throwable {
        BugzillaRepository repository = TestUtil.getRepository("test", REPO_URL, REPO_USER, REPO_PASSWD);
        
        String id = TestUtil.createIssue(repository, "testRefreshConfigOnMidAir");
        
        BugzillaIssue issue = repository.getIssue(id);
        issue.addComment("comment1");
        
        // add comment bypassing the issue to cause a midair collision
        TaskData data2 = TestUtil.getTaskData(repository.getTaskRepository(), id);
        TaskAttribute ta2 = data2.getRoot().createMappedAttribute(TaskAttribute.COMMENT_NEW);
        ta2.setValue("midairingcomment");
        TestUtil.postTaskData(brc, repository.getTaskRepository(), data2);
        
        // try to submit
        LogHandler lhExecute = new LogHandler("execute SubmitTaskCommand [task #"+id, LogHandler.Compare.STARTS_WITH, LogHandler.DEFAULT_TIMEOUT, 2);
        LogHandler lhRefresh = new LogHandler(" Refresh bugzilla configuration", LogHandler.Compare.STARTS_WITH, LogHandler.DEFAULT_TIMEOUT, 1);
        System.setProperty("netbeans.t9y.throwOnClientError", "true");
        
        Throwable st = null;
        try {
            IssueTestUtils.submit(issue);
        } catch (Throwable t) {
            st = t;
        }
        lhExecute.waitUntilDone();
        lhRefresh.waitUntilDone();
        assertEquals(2, lhExecute.getInterceptedCount());
        assertTrue(lhRefresh.isDone());
        assertTrue(st.getMessage().contains("Mid-air collision occurred while submitting"));
    }

    private void assertHandler(BugzillaRepository repository, String name) throws Throwable {
        try {
            brc.getClientManager().getClient(repository.getTaskRepository(), NULL_PROGRESS_MONITOR).validate(NULL_PROGRESS_MONITOR);
        } catch (CoreException ex) {
            assertEquals(EXCEPTION_HANDLER_CLASS_NAME + "$" + name, getHandler(repository, ex).getClass().getName());
        } catch (Exception ex) {
            TestUtil.handleException(ex);
        }
    }

    private Object getHandler(BugzillaRepository repository, CoreException ce) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException {
        BugzillaExecutor executor = repository.getExecutor();
        Class c = Class.forName(EXCEPTION_HANDLER_CLASS_NAME);
        Method m = c.getDeclaredMethod("createHandler", CoreException.class, BugzillaExecutor.class, BugzillaRepository.class, ValidateCommand.class, boolean.class);
        m.setAccessible(true);
        return  m.invoke(executor, new Object[]{ce, executor, repository, null, true});
    }
}
