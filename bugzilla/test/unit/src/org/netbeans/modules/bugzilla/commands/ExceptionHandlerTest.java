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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
