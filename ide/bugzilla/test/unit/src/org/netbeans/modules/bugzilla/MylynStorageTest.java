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
package org.netbeans.modules.bugzilla;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaOperation;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskOperation;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import static org.netbeans.modules.bugzilla.TestConstants.REPO_PASSWD;
import static org.netbeans.modules.bugzilla.TestConstants.REPO_URL;
import static org.netbeans.modules.bugzilla.TestConstants.REPO_USER;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.mylyn.util.commands.GetRepositoryTasksCommand;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.netbeans.modules.mylyn.util.NbTask;
import org.netbeans.modules.mylyn.util.NbTask.SynchronizationState;
import org.netbeans.modules.mylyn.util.NbTaskDataModel;
import org.netbeans.modules.mylyn.util.NbTaskDataState;
import org.netbeans.modules.mylyn.util.commands.SimpleQueryCommand;
import org.netbeans.modules.mylyn.util.SubmitCommand;
import org.netbeans.modules.mylyn.util.commands.SubmitTaskCommand;
import org.netbeans.modules.mylyn.util.commands.SynchronizeQueryCommand;
import org.netbeans.modules.mylyn.util.commands.SynchronizeTasksCommand;
import org.netbeans.modules.mylyn.util.TaskDataListener;
import org.netbeans.modules.mylyn.util.NbTaskListener;
import org.openide.util.RequestProcessor;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Ondrej Vrabec
 */
public class MylynStorageTest extends NbTestCase {
    
    private static final String QUERY_NAME = "My new query";
    private BugzillaRepository br;
    private TaskRepository btr;
    
    private static final String PRODUCT = "mylyn"; //NOI18N
    private static final String COMPONENT = "default"; //NOI18N
    
    public MylynStorageTest (String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("netbeans.user", new File(getDataDir(), "userdir").getAbsolutePath());
        MockLookup.setLayersAndInstances();
        BugtrackingUtil.getBugtrackingConnectors(); // ensure conector
        
        // reset
        Method m = MylynSupport.class.getDeclaredMethod("reset", new Class[0]);
        m.setAccessible(true);
        m.invoke(MylynSupport.class);
                
        Field f = Bugzilla.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(Bugzilla.class, null);
        
        br = TestUtil.getRepository("testbugzilla", REPO_URL, REPO_USER, REPO_PASSWD);
        btr = br.getTaskRepository();
    }

    @Override
    protected void tearDown () throws Exception {
        // persist for next round and shutdown
        Method m = MylynSupport.class.getDeclaredMethod("finish");
        m.setAccessible(true);
        m.invoke(MylynSupport.getInstance());
        
        super.tearDown();
    }
    
    public static Test suite () {
        return NbModuleSuite.emptyConfiguration()  
                
        .addTest(MylynStorageTest.class,
                // creates an offline temporary task
                "testCreateUnsubmittedTask",
                // submit the temporary task to the server and turn it into a full remote task
                "testSubmitTemporaryTask",
                // repository settings modifications should keep the single instance of TR
                "testEditRepository",
                // edit task
                "testEditTask",
                // submit task
                "testSubmitTask",
                // external changes
                "testIncomingChanges",
                // external changes and refresh in editor page
                "testIncomingChangesInEditorPage",
                // conflicts in incoming and outgoing
                "testConflicts",
                // conflicts in incoming and outgoing in editor page
                "testConflictsInEditorPage",
                // open task editor for unknown task
                "testOpenUnknownTask",
                // open task editor for task with deleted/corrupted task data in storage
                "testOpenTaskWithDeletedData",

                // create and init query
                "testCreateQuery",
                // synchronize and get external changes
                "testSynchronizeQuery",
                // modify query
                "testModifyQuery",
                // remove from query internal - closing a task
                "testTaskRemovedFromQueryInt",
                // remove from query externally - closing a task
                "testTaskRemovedFromQueryExt",
                // test simple search - temporary query not added to the tasklist
                "testSimpleSearch"
                ).gui(false)
                .suite();
    }
    
    public void testCreateUnsubmittedTask () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        ITaskMapping mapping = new TaskMapping() {

            @Override
            public String getProduct () {
                return PRODUCT;
            }

            @Override
            public String getComponent () {
                return COMPONENT;
            }
            
        };
        NbTask task = supp.createTask(btr, mapping);
        Collection<NbTask> allLocalTasks = supp.getTasks(supp.getLocalTaskRepository());
        Collection<NbTask> allUnsubmittedTasks = supp.getUnsubmittedTasksContainer(btr).getTasks();

        /*************** TEST *******************/
        // is it really in the tasklist
        assertEquals(0, allLocalTasks.size());
//        assertTrue(allLocalTasks.contains(task));
        assertEquals(1, allUnsubmittedTasks.size());
        assertTrue(allUnsubmittedTasks.contains(task));
        assertEquals(0, supp.getTasks(btr).size()); // not yet in the repository
        assertEquals("1", task.getTaskId());
    }
    
    public void testSubmitTemporaryTask () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        NbTask task = supp.getUnsubmittedTasksContainer(btr).getTasks().iterator().next();
        // edit the task
        NbTaskDataModel model = task.getTaskDataModel();
        
        // model.getTaskData returns our local data
        String defaultSummary = task.getSummary();
        TaskAttribute rta = model.getLocalTaskData().getRoot();
        assertFalse(model.isDirty());
        // now edit summary, product and component
        String newSummary = "Task summary testSubmitTemporaryTask";
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.SUMMARY);
        ta.setValue(newSummary);
        model.attributeChanged(ta);
        
        // now we have unsaved changes, the task is dirty
        assertTrue(model.isDirty());
        // not yet saved
        assertEquals(defaultSummary, task.getSummary());
        // save
        model.save(new NullProgressMonitor());
        // all saved?
        assertFalse(model.isDirty());
        // well, not exactly, for new unsubmitted task we need to manually refresh task's attributes
        assertEquals(defaultSummary, task.getSummary());
        if (task.getSynchronizationState() == SynchronizationState.OUTGOING_NEW) {
            task.setSummary(newSummary);
        }
        assertEquals(newSummary, task.getSummary());
        
        // let's submit finally
        NbTask submittedTask = submitTask(task, model);
        
        assertNotSame(task, submittedTask); // they difer, the new task is a persistent, not local one
        assertEquals(0, supp.getUnsubmittedTasksContainer(btr).getTasks().size());
        assertSame(submittedTask, supp.getTask(btr.getUrl(), submittedTask.getTaskId()));
        
        assertEquals(newSummary, task.getSummary());
        model = submittedTask.getTaskDataModel();
        assertSame(btr, model.getTaskRepository());
        assertSame(submittedTask, model.getTask());
        assertFalse(model.isDirty());
        assertTrue(model.hasBeenRead());
        assertTrue(model.getChangedAttributes().isEmpty());
        assertTrue(model.getChangedOldAttributes().isEmpty());
        assertEquals(SynchronizationState.SYNCHRONIZED, submittedTask.getSynchronizationState());
    }
    
    public void testEditRepository () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        Collection<NbTask> tasks = supp.getTasks(btr);
        assertEquals(1, tasks.size());
        NbTask task = tasks.iterator().next();
        assertNotNull(task);
        
        // TR should be singleton
        BugzillaRepository otherRepository = TestUtil.getRepository("testbugzilla", REPO_URL, REPO_USER, REPO_PASSWD);
        TaskRepository otherTaskRepository = otherRepository.getTaskRepository();
        assertSame(btr, otherTaskRepository);
        assertNotNull(task.getTaskDataState());
        
        // now lets change URL, it should propagate to the tasklist and rewrite all tasks
        Method m = BugzillaRepository.class.getDeclaredMethod("setupTaskRepository",
                String.class, String.class, String.class, String.class,
                char[].class, String.class, char[].class,
                Boolean.TYPE);
        m.setAccessible(true);
        m.invoke(otherRepository, "testbugzilla", REPO_URL, REPO_URL + "/OTHER",
                REPO_USER, REPO_PASSWD.toCharArray(), null, null, false);
        assertSame(btr, otherTaskRepository);
        assertEquals(REPO_URL + "/OTHER", otherRepository.getUrl());
        assertEquals(otherRepository.getUrl(), task.getRepositoryUrl());
        assertNotNull(task.getTaskDataState());
        NbTask task2 = supp.getTasks(otherTaskRepository).iterator().next();
        assertSame(task, task2);
        assertEquals(otherRepository.getUrl(), task2.getRepositoryUrl());
        assertNotNull(task2.getTaskDataState());
        
        // and back to clean
        m.invoke(otherRepository, "testbugzilla", REPO_URL + "/OTHER", REPO_URL,
                REPO_USER, REPO_PASSWD.toCharArray(), null, null, false);
        assertEquals(REPO_URL, otherRepository.getUrl());
        assertEquals(otherRepository.getUrl(), task.getRepositoryUrl());
    }
    
    public void testEditTask () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        Collection<NbTask> tasks = supp.getTasks(btr);
        assertEquals(1, tasks.size());
        NbTask task = tasks.iterator().next();
        assertNotNull(task);
        
        // the task should be clean, synchronized and without any modifications
        NbTaskDataModel model = task.getTaskDataModel();
        assertFalse(model.isDirty());
        assertEquals(SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
        // edit
        TaskAttribute rta = model.getLocalTaskData().getRoot();
        String oldSummary = task.getSummary();
        String newSummary = getName() + "_" + task.getTaskId();
        // change the task summary
        TaskAttribute summaryAttr = rta.getMappedAttribute(TaskAttribute.SUMMARY);
        summaryAttr.setValue(newSummary);
        model.attributeChanged(summaryAttr);
        // task is dirty - not saved and has modifications
        assertTrue(model.isDirty());
        assertEquals(1, model.getChangedAttributes().size());
        for (TaskAttribute attr : model.getChangedAttributes()) {
            // unsaved modifications
            assertEquals(newSummary, attr.getValue());
            // no outgoing until save
            assertFalse(model.hasOutgoingChanges(attr));
        }
        
        // unsubmitted tasks should be empty
        assertEquals(0, br.getUnsubmittedIssues().size());
        final CountDownLatch l = new CountDownLatch(1);
        PropertyChangeListener list = new PropertyChangeListener() {
            @Override
            public void propertyChange (PropertyChangeEvent evt) {
                l.countDown();
            }
        };
        br.addPropertyChangeListener(list);
        
        // save
        model.save(new NullProgressMonitor());
        l.await();
        assertEquals(1, br.getUnsubmittedIssues().size());
        br.removePropertyChangeListener(list);
        
        // task is clean (saved) - and has modifications
        assertFalse(model.isDirty());
        assertEquals(1, model.getChangedAttributes().size());
        for (TaskAttribute attr : model.getChangedAttributes()) {
            // unsaved modifications
            assertEquals(newSummary, attr.getValue());
            // no outgoing until save
            assertTrue(model.hasOutgoingChanges(attr));
        }
        assertEquals(SynchronizationState.OUTGOING, task.getSynchronizationState());
    }
    
    public void testSubmitTask () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        Collection<NbTask> tasks = supp.getTasks(btr);
        assertEquals(1, tasks.size());
        NbTask task = tasks.iterator().next();
        assertNotNull(task);
        
        // outgoing unsubmitted changes
        assertEquals(SynchronizationState.OUTGOING, task.getSynchronizationState());
        NbTaskDataModel model = task.getTaskDataModel();
        String oldSummary = task.getSummary();
        TaskAttribute summaryAttr = model.getLocalTaskData().getRoot().getMappedAttribute(TaskAttribute.SUMMARY);
        String newSummary = summaryAttr.getValue();
        assertTrue(model.hasOutgoingChanges(summaryAttr));
        assertFalse(oldSummary.equals(newSummary));
        
        // unsubmitted tasks should contain the task
        assertEquals(1, br.getUnsubmittedIssues().size());
        final CountDownLatch l = new CountDownLatch(1);
        PropertyChangeListener list = new PropertyChangeListener() {
            @Override
            public void propertyChange (PropertyChangeEvent evt) {
                l.countDown();
            }
        };
        br.addPropertyChangeListener(list);
        
        // submit
        task = submitTask(task, model);
        l.await();
        assertEquals(0, br.getUnsubmittedIssues().size());
        br.removePropertyChangeListener(list);
        
        // test
        assertFalse(model.isDirty());
        assertTrue(model.getChangedAttributes().isEmpty());
        assertTrue(model.getChangedOldAttributes().isEmpty());
        assertEquals(SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
        assertEquals(newSummary, task.getSummary());
    }
    
    public void testIncomingChanges () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        Collection<NbTask> tasks = supp.getTasks(btr);
        assertEquals(1, tasks.size());
        NbTask task = tasks.iterator().next();
        assertNotNull(task);
        
        DummyTaskWrapper wrapper = new DummyTaskWrapper(task);
        String oldSummary = task.getSummary();
        String newSummary = getName() + "_" + task.getTaskId();
        assertEquals(SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
        makeExternalChange(task, newSummary);
        
        // still no change, need to do a sync job
        assertEquals(oldSummary, wrapper.getSummary());
        assertEquals(SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
        assertEquals(SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
        
        
        // sync with server
        SynchronizeTasksCommand cmd = supp.getCommandFactory().createSynchronizeTasksCommand(btr, Collections.<NbTask>singleton(task));
        br.getExecutor().execute(cmd);
        assertEquals(SynchronizationState.INCOMING, task.getSynchronizationState());
        assertEquals(SynchronizationState.INCOMING, wrapper.getSynchronizationState());
        assertEquals(newSummary, wrapper.getSummary());
        assertEquals("Summary from " + oldSummary + " to " + newSummary, wrapper.getIncomingChangesText());
        wrapper.forget();
    }
    
    public void testIncomingChangesInEditorPage () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        Collection<NbTask> tasks = supp.getTasks(btr);
        assertEquals(1, tasks.size());
        NbTask task = tasks.iterator().next();
        assertNotNull(task);
        
        String oldSummary = task.getSummary();
        String newSummary = getName() + "_" + task.getTaskId();
        DummyTaskWrapper wrapper = new DummyTaskWrapper(task);
        assertEquals(SynchronizationState.INCOMING, wrapper.getSynchronizationState());
        DummyEditorPage page = new DummyEditorPage(task);
        page.open();
        page.assertOpened();
        assertEquals(oldSummary, page.taskDataSummary);
        assertTrue(page.summaryChanged);
        page.clear();
        assertEquals(SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
        assertEquals(SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
        makeExternalChange(task, newSummary);
        
        // still no change, need to do a sync job
        assertEquals(oldSummary, wrapper.getSummary());
        assertEquals(SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
        
        
        // sync with server
        SynchronizeTasksCommand cmd = supp.getCommandFactory().createSynchronizeTasksCommand(btr, Collections.<NbTask>singleton(task));
        br.getExecutor().execute(cmd);
        // synchronized because it's refreshed in the editor page automatically
        assertTrue(page.summaryChanged);
        assertEquals(newSummary, page.taskDataSummary);
        assertEquals(SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
        assertEquals(newSummary, wrapper.getSummary());
        assertEquals("", wrapper.getIncomingChangesText());
        
        page.close();
        wrapper.forget();
    }
    
    public void testConflicts () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        Collection<NbTask> tasks = supp.getTasks(btr);
        assertEquals(1, tasks.size());
        NbTask task = tasks.iterator().next();
        assertNotNull(task);
        
        DummyTaskWrapper wrapper = new DummyTaskWrapper(task);
        String oldSummary = task.getSummary();
        String newSummary = getName() + "_" + task.getTaskId();
        assertEquals(SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
        makeExternalChange(task, newSummary);
        
        // still no change, need to do a sync job
        assertEquals(oldSummary, wrapper.getSummary());
        assertEquals(SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
        assertEquals(SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
        
        // make local changes
        DummyEditorPage page = new DummyEditorPage(task);
        page.open();
        page.assertOpened();
        page.changeSummary(newSummary + "_local");
        // save
        page.close();
        
        // still no change, need to do a sync job
        assertEquals(SynchronizationState.OUTGOING, wrapper.getSynchronizationState());
        
        // sync with server
        SynchronizeTasksCommand cmd = supp.getCommandFactory().createSynchronizeTasksCommand(btr, Collections.<NbTask>singleton(task));
        br.getExecutor().execute(cmd);
        assertEquals(SynchronizationState.CONFLICT, wrapper.getSynchronizationState());
        assertEquals(newSummary, wrapper.getSummary());
        assertEquals("Summary from " + oldSummary + " to " + newSummary, wrapper.getIncomingChangesText());
        
        // open editor and clear 
        page.open();
        assertEquals(SynchronizationState.OUTGOING, wrapper.getSynchronizationState());
        assertEquals("", wrapper.getIncomingChangesText());
        assertTrue(page.summaryChangedLocally);
        
        // revert to synchronized
        page.revert();
        assertEquals(SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
        assertEquals(SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
        
        wrapper.forget();
    }
    
    public void testConflictsInEditorPage () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        Collection<NbTask> tasks = supp.getTasks(btr);
        assertEquals(1, tasks.size());
        NbTask task = tasks.iterator().next();
        assertNotNull(task);
        
        DummyTaskWrapper wrapper = new DummyTaskWrapper(task);
        String oldSummary = task.getSummary();
        String newSummary = getName() + "_" + task.getTaskId();
        assertEquals(SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
        makeExternalChange(task, newSummary);
        
        // still no change, need to do a sync job
        assertEquals(oldSummary, wrapper.getSummary());
        assertEquals(SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
        assertEquals(SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
        
        // make local changes
        DummyEditorPage page = new DummyEditorPage(task);
        page.open();
        page.assertOpened();
        page.changeSummary(newSummary + "_local");
        // save
        page.save();
        
        // still no change, need to do a sync job
        assertEquals(SynchronizationState.OUTGOING, wrapper.getSynchronizationState());
        
        // sync with server
        SynchronizeTasksCommand cmd = supp.getCommandFactory().createSynchronizeTasksCommand(btr, Collections.<NbTask>singleton(task));
        br.getExecutor().execute(cmd);
        // not in conflict because it's refreshed in the editor page automatically
        assertTrue(page.summaryChangedLocally);
        assertEquals(newSummary + "_local", page.taskDataSummary);
        assertEquals(SynchronizationState.OUTGOING, wrapper.getSynchronizationState());
        assertEquals(newSummary, wrapper.getSummary());
        assertEquals("", wrapper.getIncomingChangesText());
        
        // revert to synchronized
        page.revert();
        assertEquals(SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
        assertEquals(SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
        assertEquals(newSummary, page.taskDataSummary);
        assertEquals(newSummary, wrapper.getSummary());
        assertEquals("", wrapper.getIncomingChangesText());
        
        page.close();
        wrapper.forget();
    }
    
    public void testOpenUnknownTask () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        
        String taskId = "1";
        Collection<NbTask> tasks = supp.getTasks(btr);
        for (NbTask t : tasks) {
            assertFalse(taskId.equals(t.getTaskId()));
        }
        
        DummyEditorPage page = new DummyEditorPage(taskId);
        page.open();
        page.waitUntilOpened();
        page.assertOpened();
        
        page.close();
        
        assertTrue(supp.getTasks(btr).contains(page.task));
        page.task.delete();
        assertFalse(supp.getTasks(btr).contains(page.task));
    }
    
    public void testOpenTaskWithDeletedData () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        
        Collection<NbTask> tasks = supp.getTasks(btr);
        assertEquals(1, tasks.size());
        NbTask task = tasks.iterator().next();
        assertNotNull(task);
        // delete task data
        assertNotNull(task.getTaskDataState());
        deleteTaskData(task);
        assertNull(task.getTaskDataState());
        
        DummyEditorPage page = new DummyEditorPage(task);
        page.open();
        page.waitUntilOpened();
        page.assertOpened();
        assertEquals(task, page.task);
        
        page.close();
    }
    
    public void testCreateQuery () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        
        Collection<NbTask> tasks = supp.getTasks(btr);
        assertEquals(1, tasks.size());
        // task already known, will be up to date
        NbTask task = tasks.iterator().next();
        assertNotNull(task);
                
        // query list is empty
        assertEquals(0, supp.getRepositoryQueries(btr).size());
        // create new query
        final IRepositoryQuery query = supp.createNewQuery(btr, QUERY_NAME);
        query.setUrl("/buglist.cgi?query_format=advanced&product=" + PRODUCT + "&component=" + COMPONENT);
        supp.addQuery(btr, query);
        // was it added?
        assertSame(query, supp.getRepositoryQueries(btr).iterator().next());
        
        // it's still empty, need to sync first
        assertEquals(0, supp.getTasks(query).size());
        
        DummyQueryController controller = new DummyQueryController(query);
        // synchronize
        SynchronizeQueryCommand cmd = supp.getCommandFactory().createSynchronizeQueriesCommand(btr, query);
        cmd.addCommandProgressListener(controller);
        br.getExecutor().execute(cmd);
        
        // all pages should be opened
        controller.closeAllPages();
        // get all tasks for the query
        tasks = controller.getTasks();
        assertEquals(new HashSet<NbTask>(supp.getTasks(query)), new HashSet<NbTask>(tasks));
        assertTrue(tasks.contains(task));
        
        // all tasks are NEW - except for the known old task
        for (NbTask t : tasks) {
            if (t == task || controller.getOpenedTasks().contains(t)) {
                assertEquals(SynchronizationState.SYNCHRONIZED, t.getSynchronizationState());
            } else {
                if (t.getSynchronizationState() == SynchronizationState.SYNCHRONIZED) {
                    // prehistoric tasks are marked as synchronized
                    // but have a specific flag
                    assertEquals("true", t.getAttribute("NetBeans.task.unseen"));
                } else {
                    assertEquals(SynchronizationState.INCOMING_NEW, t.getSynchronizationState());
                }
                DummyEditorPage p = new DummyEditorPage(t);
                p.open();
                p.assertOpened();
                p.close();
                assertEquals(SynchronizationState.SYNCHRONIZED, t.getSynchronizationState());
                assertNull(t.getAttribute("NetBeans.task.unseen"));
            }
        }
    }
    
    public void testSynchronizeQuery () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        IRepositoryQuery q = supp.getRepositoryQuery(btr, QUERY_NAME);
        Collection<NbTask> tasks = supp.getTasks(q);
        Set<DummyTaskWrapper> wrappers = new HashSet<DummyTaskWrapper>(tasks.size());
        
        // get tasks from the query
        assertFalse(tasks.isEmpty());
        
        // make external changes in summaries
        Map<NbTask, String> oldSummaries = new HashMap<NbTask, String>(tasks.size());
        Map<NbTask, String> newSummaries = new HashMap<NbTask, String>(tasks.size());
        int i = 0;
        for (NbTask task : tasks) {
            // make at most 10 changes so this ends sometimes
            if (++i > 10) {
                break;
            }
            wrappers.add(new DummyTaskWrapper(task));
            assertEquals(SynchronizationState.SYNCHRONIZED, task.getSynchronizationState());
            String newSummary = getName() + "_" + task.getTaskId() + "_" + System.currentTimeMillis();
            oldSummaries.put(task, task.getSummary());
            newSummaries.put(task, newSummary);
            makeExternalChange(task, newSummary);
        }
        
        // no change yet
        for (DummyTaskWrapper wrapper : wrappers) {
            assertEquals(SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
            assertEquals(SynchronizationState.SYNCHRONIZED, wrapper.task.getSynchronizationState());
        }
        SynchronizeQueryCommand cmd = supp.getCommandFactory().createSynchronizeQueriesCommand(btr, q);
        br.getExecutor().execute(cmd);
          
        // all tasks have incoming changes
        for (DummyTaskWrapper wrapper : wrappers) {
            String newSummary = newSummaries.get(wrapper.task);
            assertEquals(SynchronizationState.INCOMING, wrapper.getSynchronizationState());
            assertEquals(SynchronizationState.INCOMING, wrapper.task.getSynchronizationState());
            assertEquals(newSummary, wrapper.getSummary());
            assertEquals("Summary from " + oldSummaries.get(wrapper.task) + " to " + newSummary, wrapper.getIncomingChangesText());
            
            // open and see changes
            DummyEditorPage p = new DummyEditorPage(wrapper.task);
            p.open();
            assertTrue(p.summaryChanged);
            assertEquals(newSummary, p.taskDataSummary);
            assertEquals(SynchronizationState.SYNCHRONIZED, wrapper.getSynchronizationState());
            assertEquals(SynchronizationState.SYNCHRONIZED, wrapper.task.getSynchronizationState());
            p.close();
        }
        for (DummyTaskWrapper wrapper : wrappers) {
            wrapper.forget();
        }
    }
    
    public void testModifyQuery () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        IRepositoryQuery query = supp.getRepositoryQuery(btr, QUERY_NAME);
        Collection<NbTask> tasks = supp.getTasks(query);
        Collection<NbTask> toRemove = new HashSet<NbTask>();
        for (NbTask task : tasks) {
            if (task.isCompleted()) {
                toRemove.add(task);
            }
        }
        assertFalse(toRemove.isEmpty());
        
        // make another new task so the query is not empty
        NbTask createdTask = createNewTask("New task testModifyQuery");
        
        // modify query to make it more precise - will not list closed tasks
        assertFalse(tasks.isEmpty());
        DummyQueryController controller = new DummyQueryController(query);
        query.setUrl(query.getUrl() + "&bug_status=NEW" + "&bug_status=REOPENED"); //NOI18N
        // synchronize
        SynchronizeQueryCommand cmd = supp.getCommandFactory().createSynchronizeQueriesCommand(btr, query);
        cmd.addCommandProgressListener(controller);
        br.getExecutor().execute(cmd);
        
        tasks = controller.tasks;
        assertFalse(tasks.isEmpty());
        assertEquals(new HashSet<NbTask>(supp.getTasks(query)), new HashSet<NbTask>(tasks));
        for (NbTask removedTask : toRemove) {
            assertFalse(tasks.contains(removedTask));
        }
        assertTrue(tasks.contains(createdTask));
    }
    
    public void testTaskRemovedFromQueryInt () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        IRepositoryQuery q = supp.getRepositoryQuery(btr, QUERY_NAME);
        Collection<NbTask> tasks = supp.getTasks(q);
        
        // get tasks from the query
        assertFalse(tasks.isEmpty());
        DummyQueryController controller = new DummyQueryController(q);
        // get a task to close
        NbTask task = tasks.iterator().next();
        assertTrue(controller.tasks.contains(task));
        // close the task in editor
        DummyEditorPage page = new DummyEditorPage(task);
        page.open();
        page.closeTask("WONTFIX");
        page.save();
        
        // submit
        page.submit();
        assertTrue(task.isCompleted());
        
        // refresh query
        SynchronizeQueryCommand cmd = supp.getCommandFactory().createSynchronizeQueriesCommand(btr, q);
        cmd.addCommandProgressListener(controller);
        br.getExecutor().execute(cmd);
        
        // task should be removed from the list
        assertFalse(controller.getTasks().contains(task));
        
        page.close();
    }
    
    public void testTaskRemovedFromQueryExt () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        IRepositoryQuery q = supp.getRepositoryQuery(btr, QUERY_NAME);
        Collection<NbTask> tasks = supp.getTasks(q);
        
        // get tasks from the query
        assertFalse(tasks.isEmpty());
        DummyQueryController controller = new DummyQueryController(q);
        // get a task to close
        NbTask task = tasks.iterator().next();
        assertTrue(controller.tasks.contains(task));
        
        // close the task externally
        assertFalse(task.isCompleted());
        TaskData external = task.getTaskDataState().getRepositoryData();
        TaskAttribute opAttr = external.getRoot().getMappedAttribute(TaskAttribute.OPERATION);
        TaskOperation taskOperation = null;
        for (TaskOperation op : external.getAttributeMapper().getTaskOperations(opAttr)) {
            if (BugzillaOperation.resolve.getLabel().equals(op.getLabel())) {
                taskOperation = op;
                break;
            }
        }
        assertNotNull(taskOperation);
        external.getAttributeMapper().setTaskOperation(opAttr, taskOperation);
        TaskAttribute resolutionAttr = external.getRoot().getMappedAttribute(BugzillaOperation.resolve.getInputId());
        resolutionAttr.setValue("WONTFIX");
        SubmitCommand submitCmd = new SubmitCommand(Bugzilla.getInstance().getRepositoryConnector(), btr, external);
        br.getExecutor().execute(submitCmd);
        
        // refresh query
        SynchronizeQueryCommand cmd = supp.getCommandFactory().createSynchronizeQueriesCommand(btr, q);
        cmd.addCommandProgressListener(controller);
        br.getExecutor().execute(cmd);
        
        // task should be removed from the list
        assertFalse(controller.getTasks().contains(task));
    }
    
    public void testSimpleSearch () throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        String queryName = "Temporary query";
        
        IRepositoryQuery q = supp.createNewQuery(btr, queryName);
        q.setUrl("/buglist.cgi?query_format=advanced&bug_id=1%2C2%2C3"); // three tasks
        // query list is empty
        assertFalse(supp.getRepositoryQueries(btr).contains(q));
        // it's still empty, need to sync first
        
        // synchronize
        SimpleQueryCommand cmd = supp.getCommandFactory().createSimpleQueryCommand(btr, q);
        br.getExecutor().execute(cmd);
                
        // get all tasks for the query
        Collection<NbTask> tasks = cmd.getTasks();
        assertEquals(3, tasks.size());
        
        Collection<NbTask> tasklistTasks = supp.getTasks(btr);
        // all tasks are in the tasklist
        for (NbTask t : tasks) {
            assertTrue(tasklistTasks.contains(t));
        }
        assertFalse(supp.getRepositoryQueries(btr).contains(q));
        
        // open tasks
        for (NbTask t : tasks) {
            DummyEditorPage p = new DummyEditorPage(t);
            p.open();
            p.waitUntilOpened();
            p.assertOpened();
            p.close();
        }
    }

    /**
     * This should be done in the editor page upon click on Submit
     */
    private NbTask submitTask (NbTask task, NbTaskDataModel model) throws CoreException {
        SubmitTaskCommand cmd = MylynSupport.getInstance().getCommandFactory().createSubmitTaskCommand(model);
        br.getExecutor().execute(cmd);
        NbTask newTask = cmd.getSubmittedTask();
        if (task == newTask) {
            // refresh model and whole editor page if opened
            model.refresh();
        }
        
        return newTask;
    }

    private void makeExternalChange (NbTask task, String newSummary) throws CoreException {
        MylynSupport supp = MylynSupport.getInstance();
        TaskData taskData = task.getTaskDataState().getRepositoryData();
        
        // edit the task externally
        TaskAttribute rta = taskData.getRoot();
        // now make an external change in summary
        TaskData external = new TaskData(taskData.getAttributeMapper(),
                taskData.getConnectorKind(),
                taskData.getRepositoryUrl(),
                taskData.getTaskId());
        external.setVersion(taskData.getVersion());
        for (TaskAttribute child : rta.getAttributes().values()) {
            external.getRoot().deepAddCopy(child);
        }
        external.getRoot().getMappedAttribute(TaskAttribute.SUMMARY).setValue(newSummary);
        SubmitCommand submitCmd = new SubmitCommand(Bugzilla.getInstance().getRepositoryConnector(), btr, external);
        br.getExecutor().execute(submitCmd);
    }

    private NbTask createNewTask (String summary) throws CoreException {
        MylynSupport supp = MylynSupport.getInstance();
        ITaskMapping mapping = new TaskMapping() {

            @Override
            public String getProduct () {
                return PRODUCT;
            }

            @Override
            public String getComponent () {
                return COMPONENT;
            }
            
        };
        NbTask task = supp.createTask(btr, mapping);
        NbTaskDataModel model = task.getTaskDataModel();
        
        // model.getTaskData returns our local data
        TaskAttribute rta = model.getLocalTaskData().getRoot();
        assertFalse(model.isDirty());
        // now edit summary, product and component
        String newSummary = summary;
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.SUMMARY);
        ta.setValue(newSummary);
        model.attributeChanged(ta);
        
        // save
        model.save(new NullProgressMonitor());
        if (task.getSynchronizationState() == SynchronizationState.OUTGOING_NEW) {
            task.setSummary(newSummary);
        }
        return submitTask(task, model);
    }

    private void deleteTaskData (NbTask task) throws Exception {
        MylynSupport supp = MylynSupport.getInstance();
        Field f = MylynSupport.class.getDeclaredField("taskDataManager");
        f.setAccessible(true);
        TaskDataManager mgr = (TaskDataManager) f.get(supp);
        f = NbTask.class.getDeclaredField("delegate");
        f.setAccessible(true);
        mgr.deleteTaskData((ITask) f.get(task));
    }

    // something like BugzillaIssue
    private class DummyTaskWrapper implements NbTaskListener {
        private final NbTask task;
        private SynchronizationState syncState;
        private String summary;
        private String incomingChanges;

        public DummyTaskWrapper (NbTask task) {
            this.task = task;
            syncState = task.getSynchronizationState();
            summary = task.getSummary();
            task.addNbTaskListener(this);
        }

        // should be moved to a central place
        // maybe a BugzillaIssueManager
        @Override
        public void taskModified (TaskEvent ev) {
            if (ev.getTask() == task && ev.getKind() == TaskEvent.Kind.MODIFIED) {
                syncState = task.getSynchronizationState();
                summary = task.getSummary();
                incomingChanges = "";
                if (syncState == SynchronizationState.INCOMING
                        || syncState == SynchronizationState.CONFLICT) {
                    try {
                        NbTaskDataState taskDataState = task.getTaskDataState();
                        Set<TaskAttribute> changedAttributes = MylynSupport.getInstance().countDiff(
                                taskDataState.getRepositoryData(),
                                taskDataState.getLastReadData());
                        for (TaskAttribute changedAttr : changedAttributes) {
                            if (changedAttr.getId().equals(taskDataState.getRepositoryData().getRoot()
                                    .getMappedAttribute(TaskAttribute.SUMMARY).getId())) {
                                incomingChanges = "Summary from "
                                        + taskDataState.getLastReadData().getRoot().getMappedAttribute(TaskAttribute.SUMMARY).getValue()
                                        + " to "
                                        + taskDataState.getRepositoryData().getRoot().getMappedAttribute(TaskAttribute.SUMMARY).getValue();
                            }
                        }
                    } catch (CoreException ex) {
                        log(ex.toString());
                    }
                }
            }
        }
        
        void forget () {
            task.removeNbTaskListener(this);
        }

        private SynchronizationState getSynchronizationState () {
            return syncState;
        }

        private String getSummary () {
            return summary;
        }

        private String getIncomingChangesText () {
            return incomingChanges;
        }
    }

    private class DummyEditorPage implements TaskDataListener {
        private NbTask task;
        private String taskId;
        private NbTaskDataModel model;
        private String taskDataSummary;
        private boolean summaryChanged;
        private boolean summaryChangedLocally;
        private volatile boolean waitingToOpen;
        private MylynSupport supp;

        public DummyEditorPage (NbTask task) {
            this.task = task;
        }
        
        public DummyEditorPage (String taskId) {
            this.taskId = taskId;
        }
        
        void open () throws CoreException {
            supp = MylynSupport.getInstance();
            supp.addTaskDataListener(this);
            if (task == null || task.getTaskDataState() == null) {
                waitingToOpen = true;
                RequestProcessor.getDefault().schedule(new Runnable() {
                    @Override
                    public void run () {
                        if (waitingToOpen) {
                            try {
                                GetRepositoryTasksCommand cmd = supp.getCommandFactory().createGetRepositoryTasksCommand(
                                        btr, Collections.<String>singleton(task == null ? taskId : task.getTaskId()));
                                br.getExecutor().execute(cmd);
                                if (!cmd.getTasks().isEmpty()) {
                                    task = cmd.getTasks().iterator().next();
                                    if (task != null) {
                                        finishOpen();
                                    }
                                }
                            } catch (CoreException ex) {
                                log(ex.toString());
                            }
                        }
                    }
                }, 2, TimeUnit.SECONDS);
            } else {
                model = task.getTaskDataModel();
                if (model.getLocalTaskData().isPartial()) {
                    waitingToOpen = true;
                } else {
                    finishOpen();
                }
            }
        }
        
        void close () throws CoreException {
            save();
            MylynSupport.getInstance().removeTaskDataListener(this);
        }

        @Override
        public void taskDataUpdated (TaskDataEvent event) {
            if (event.getTask() == task) {
                if (event.getTaskData() != null && !event.getTaskData().isPartial() && waitingToOpen) {
                    try {
                        finishOpen();
                    } catch (CoreException ex) {
                        log(ex.getMessage());
                    }
                }
                if (event.getTaskDataUpdated()) {
                    refresh();
                }
            }
        }

        private void clear () {
            summaryChanged = summaryChangedLocally = false;
        }

        private void changeSummary (String newSummary) {
            TaskAttribute summaryAttr = model.getLocalTaskData().getRoot().getMappedAttribute(TaskAttribute.SUMMARY);
            summaryAttr.setValue(newSummary);
            model.attributeChanged(summaryAttr);
            assertTrue(model.isDirty());
        }

        private void save () throws CoreException {
            if (model.isDirty()) {
                model.save(null);
            }
        }

        private void revert () throws CoreException {
            task.discardLocalEdits();
            refresh();
        }

        private void refresh () {
            if (model == null) {
                return;
            }
            // maybe show a warning before overwriting the state
            try {
                model.refresh();
                TaskAttribute ta = model.getLocalTaskData().getRoot().getMappedAttribute(TaskAttribute.SUMMARY);
                summaryChangedLocally = model.hasOutgoingChanges(ta);
                summaryChanged = model.hasIncomingChanges(ta, true);
                taskDataSummary = ta.getValue();
            } catch (CoreException ex) {
                log(ex.toString());
            }
        }

        private void finishOpen () throws CoreException {
            if (model == null) {
                model = task.getTaskDataModel();
            }
            taskDataSummary = model.getLocalTaskData().getRoot().getMappedAttribute(TaskAttribute.SUMMARY).getValue();
            TaskAttribute ta = model.getLocalTaskData().getRoot().getMappedAttribute(TaskAttribute.SUMMARY);
            summaryChangedLocally = model.hasOutgoingChanges(ta);
            summaryChanged = model.hasIncomingChanges(ta, true);
            waitingToOpen = false;
        }
        
        private void assertOpened () {
            assertFalse(waitingToOpen);
        }

        private void closeTask (String resolution) {
            TaskOperation taskOperation = null;
            TaskAttribute opAttr = model.getLocalTaskData().getRoot().getMappedAttribute(TaskAttribute.OPERATION);
            for (TaskOperation op : model.getLocalTaskData().getAttributeMapper().getTaskOperations(opAttr)) {
                if (BugzillaOperation.resolve.getLabel().equals(op.getLabel())) {
                    taskOperation = op;
                    break;
                }
            }
            assertNotNull(taskOperation);
            assertFalse(task.isCompleted());
            model.getLocalTaskData().getAttributeMapper().setTaskOperation(opAttr, taskOperation);
            model.attributeChanged(opAttr);
            TaskAttribute resolutionAttr = model.getLocalTaskData().getRoot().getMappedAttribute(BugzillaOperation.resolve.getInputId());
            resolutionAttr.setValue(resolution);
            model.attributeChanged(resolutionAttr);
        }

        private void submit () throws CoreException {
            SubmitTaskCommand cmd = supp.getCommandFactory().createSubmitTaskCommand(model);
            br.getExecutor().execute(cmd);
        }

        private void waitUntilOpened () throws Exception {
            for (int i = 0; i < 50; ++i) {
                if (!waitingToOpen) {
                    break;
                }
                Thread.sleep(1000);
            }
            assertOpened();
        }
    }

    private class DummyQueryController implements SynchronizeQueryCommand.CommandProgressListener {
        private final IRepositoryQuery query;
        private final MylynSupport supp;
        private final Set<NbTask> tasks;
        private final List<DummyEditorPage> pages;
        private boolean flag = true;

        public DummyQueryController (IRepositoryQuery query) throws CoreException {
            this.query = query;
            this.supp = MylynSupport.getInstance();
            this.tasks = new HashSet<NbTask>(supp.getTasks(query));
            this.pages = new ArrayList<DummyEditorPage>();
        }

        @Override
        public void queryRefreshStarted (Collection<NbTask> tasks) {
            for (NbTask task : tasks) {
                taskAdded(task);
            }
        }

        @Override
        public void tasksRefreshStarted (Collection<NbTask> tasks) {
        }

        @Override
        public void taskAdded (NbTask task) {
            tasks.add(task);
            // open every other task to simulate fast clicking on task in query
            // when the task is not yet ready
            if (flag = !flag) {
                // open page
                DummyEditorPage page = new DummyEditorPage(task);
                pages.add(page);
                try {
                    page.open();
                } catch (CoreException ex) {
                    log(ex.getMessage());
                }
            }
        }

        @Override
        public void taskRemoved (NbTask task) {
            tasks.remove(task);
        }

        @Override
        public void taskSynchronized (NbTask task) {
        }

        void closeAllPages () throws CoreException {
            for (DummyEditorPage page : pages) {
                page.assertOpened();
                page.close();
            }
        }
        
        private Collection<NbTask> getTasks () {
            return tasks;
        }

        private Set<NbTask> getOpenedTasks () {
            Set<NbTask> retval = new HashSet<NbTask>(pages.size());
            for (DummyEditorPage p : pages) {
                retval.add(p.task);
            }
            return retval;
        }
    }
    
}
