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
package org.netbeans.modules.mylyn.util;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.ITaskListChangeListener;
import org.eclipse.mylyn.internal.tasks.core.ITaskListRunnable;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.internal.tasks.core.RepositoryModel;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.TaskActivityManager;
import org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryDelta;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.data.ITaskDataManagerListener;
import org.eclipse.mylyn.internal.tasks.core.data.SynchronizationManger;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataDiff;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManagerEvent;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataState;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataStore;
import org.eclipse.mylyn.internal.tasks.core.externalization.TaskListExternalizer;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryListener;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.ITaskAttributeDiff;
import org.eclipse.mylyn.tasks.core.data.ITaskDataWorkingCopy;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.mylyn.util.commands.CommandFactory;
import org.netbeans.modules.mylyn.util.internal.CommandsAccessor;
import org.netbeans.modules.mylyn.util.internal.TaskListener;
import org.netbeans.modules.mylyn.util.localtasks.internal.LocalTaskDataHandler;
import org.openide.modules.Places;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Ondrej Vrabec
 */
@NbBundle.Messages({
    "MSG_NewTaskSummary=New Unsubmitted Task"
})
public class MylynSupport {

    private static MylynSupport instance;
    private static final String BACKUP_SUFFIX = ".backup"; //NOI18N
    private final TaskRepositoryManager taskRepositoryManager;
    private final TaskRepository localTaskRepository;
    private final TaskList taskList;
    private final RepositoryModel repositoryModel;
    private final SynchronizationManger synchronizationManager;
    private final TaskDataManager taskDataManager;
    private final TaskActivityManager taskActivityManager;
    private final TaskListExternalizer taskListWriter;
    private final File taskListStorageFile;
    private boolean taskListInitialized;
    private CommandFactory factory;
    private static final Logger LOG = Logger.getLogger(MylynSupport.class.getName());
    private ITaskListChangeListener taskListListener;
    private static final String PROP_REPOSITORY_CREATION_TIME = "repository.creation.time_"; //NOI18N
    private IRepositoryListener taskRepositoryManagerListener;
    private static final String ATTR_TASK_INCOMING_NEW = "NetBeans.task.unseen"; //NOI18N
    private static final RequestProcessor RP = new RequestProcessor("MylynSupport", 1, true); //NOI18N
    private final Task saveTask;
    private boolean dirty;
    private final Map<TaskRepository, UnsubmittedTasksContainer> unsubmittedTaskContainers;
    private ITaskDataManagerListener taskDataManagerListener;
    private final List<TaskDataListener> taskDataListeners;
    private final Map<ITask, List<TaskListener>> taskListeners;
    private final Map<ITask, Reference<NbTask>> tasks = new WeakHashMap<ITask, Reference<NbTask>>();
    private final Map<TaskListener, ITask> taskPerList = new HashMap<TaskListener, ITask>();
    private Lookup.Result<RepositoryConnectorProvider> result;

    public static synchronized MylynSupport getInstance () {
        if (instance == null) {
            instance = new MylynSupport();
        }
        return instance;
    }

    @NbBundle.Messages({
        "LBL_LocalTaskRepository.displayName=Local Tasks"
    })
    private MylynSupport () {
        taskRepositoryManager = new TaskRepositoryManager();
        taskRepositoryManager.addRepositoryConnector(new LocalRepositoryConnector());
        localTaskRepository = new TaskRepository(LocalRepositoryConnector.CONNECTOR_KIND, LocalRepositoryConnector.REPOSITORY_URL);
        localTaskRepository.setRepositoryLabel(Bundle.LBL_LocalTaskRepository_displayName());
        taskRepositoryManager.addRepository(localTaskRepository);

        taskList = new TaskList();
        repositoryModel = new RepositoryModel(taskList, taskRepositoryManager);
        synchronizationManager = new SynchronizationManger(repositoryModel);
        taskActivityManager = new TaskActivityManager(taskRepositoryManager, taskList);
        TaskDataStore taskDataStore = new TaskDataStore(taskRepositoryManager);
        taskDataManager = new TaskDataManager(taskDataStore, taskRepositoryManager, taskList,
                taskActivityManager, synchronizationManager);

        String storagePath = Places.getUserDirectory().getAbsolutePath()
                + "/var/tasks/mylyn".replace("/", File.separator); //NOI18N
        taskListStorageFile = new File(storagePath, ITasksCoreConstants.DEFAULT_TASK_LIST_FILE);
        taskDataManager.setDataPath(storagePath);
        taskListWriter = new TaskListExternalizer(repositoryModel, taskRepositoryManager);
        AccessorImpl.getInstance(); // initializes Accessor
        saveTask = RP.create(new Runnable() {
            @Override
            public void run () {
                try {
                    persist(false);
                } catch (CoreException ex) {
                    LOG.log(Level.WARNING, null, ex);
                }
            }
        });
        unsubmittedTaskContainers = new WeakHashMap<TaskRepository, UnsubmittedTasksContainer>();
        taskDataListeners = new CopyOnWriteArrayList<TaskDataListener>();
        taskListeners = new WeakHashMap<ITask, List<TaskListener>>();
        attachListeners();
    }

    /**
     * Returns all known tasks from the given repository.
     *
     * @param taskRepository repository tasks are stored in
     * @return tasks from the requested repository
     * @throws CoreException when the tasklist is inaccessible.
     */
    public Collection<NbTask> getTasks (TaskRepository taskRepository) throws CoreException {
        ensureTaskListLoaded();
        assert taskRepositoryManager.getRepositoryConnector(taskRepository.getConnectorKind()) != null
                : "Did you forget to implement RepositoryConnectorProvider?";
        return toNbTasks(taskList.getTasks(taskRepository.getUrl()),
                !LocalRepositoryConnector.CONNECTOR_KIND.equals(taskRepository.getConnectorKind()));
    }

    public Collection<NbTask> getTasks (IRepositoryQuery query) throws CoreException {
        assert query instanceof RepositoryQuery;
        if (query instanceof RepositoryQuery) {
            ensureTaskListLoaded();
            assert taskRepositoryManager.getRepositoryConnector(query.getConnectorKind()) != null
                : "Did you forget to implement RepositoryConnectorProvider?";
            return toNbTasks(((RepositoryQuery) query).getChildren());
        } else {
            return Collections.<NbTask>emptyList();
        }
    }

    public NbTask getTask (String repositoryUrl, String taskKeyOrId) throws CoreException {
        ensureTaskListLoaded();
        ITask task = taskList.getTaskByKey(repositoryUrl, taskKeyOrId);
        if (task == null) {
            task = taskList.getTask(repositoryUrl, taskKeyOrId);
        }
        return toNbTask(task);
    }

    public UnsubmittedTasksContainer getUnsubmittedTasksContainer (TaskRepository taskRepository) throws CoreException {
        UnsubmittedTasksContainer cont;
        synchronized (unsubmittedTaskContainers) {
            cont = unsubmittedTaskContainers.get(taskRepository);
            if (cont == null) {
                ensureTaskListLoaded();
                assert taskRepositoryManager.getRepositoryConnector(taskRepository.getConnectorKind()) != null
                    : "Did you forget to implement RepositoryConnectorProvider?";
                cont = new UnsubmittedTasksContainer(taskRepository, taskList);
                unsubmittedTaskContainers.put(taskRepository, cont);
            }
        }
        return cont;
    }

    public TaskRepository getLocalTaskRepository () {
        return localTaskRepository;
    }

    public void save () throws CoreException {
        persist(false);
    }

    /**
     * Adds a listener notified when a task data is updated ad modified.
     *
     * @param listener
     */
    public void addTaskDataListener (TaskDataListener listener) {
        taskDataListeners.add(listener);
    }

    public void removeTaskDataListener (TaskDataListener listener) {
        taskDataListeners.remove(listener);
    }

    public void addRepositoryListener (IRepositoryListener listener) {
        taskRepositoryManager.addListener(listener);
    }

    // for tests only
    static synchronized void reset () {
        instance = null;
    }

    NbTaskDataState getTaskDataState (NbTask task) throws CoreException {
        TaskDataState taskDataState = taskDataManager.getTaskDataState(task.getDelegate());
        if (taskDataState == null) {
            return null;
        } else {
            if (taskDataState.getLastReadData() == null) {
                taskDataState.setLastReadData(taskDataState.getRepositoryData());
            }
            return new NbTaskDataState(taskDataState);
        }
    }

    public Set<TaskAttribute> countDiff (TaskData newTaskData, TaskData oldTaskData) {
        Set<TaskAttribute> attributes = new LinkedHashSet<TaskAttribute>();
        TaskDataDiff diff = new TaskDataDiff(repositoryModel, newTaskData, oldTaskData);
        for (ITaskAttributeDiff diffAttr : diff.getChangedAttributes()) {
            attributes.add(newTaskData.getRoot().getAttribute(diffAttr.getAttributeId()));
        }
        return attributes;
    }

    public Set<IRepositoryQuery> getRepositoryQueries (TaskRepository taskRepository) throws CoreException {
        ensureTaskListLoaded();
        assert taskRepositoryManager.getRepositoryConnector(taskRepository.getConnectorKind()) != null
                : "Did you forget to implement RepositoryConnectorProvider?";
        return new HashSet<IRepositoryQuery>(taskList.getRepositoryQueries(taskRepository.getUrl()));
    }

    public void addQuery (TaskRepository taskRepository, IRepositoryQuery query) throws CoreException {
        if (!(query instanceof RepositoryQuery)) {
            throw new IllegalArgumentException("Query must be instance of RepositoryQuery: " + query);
        }
        ensureTaskListLoaded();
        assert taskRepositoryManager.getRepositoryConnector(taskRepository.getConnectorKind()) != null
                : "Did you forget to implement RepositoryConnectorProvider?";
        taskList.addQuery((RepositoryQuery) query);
    }

    void discardLocalEdits (ITask task) throws CoreException {
        taskDataManager.discardEdits(task);
    }

    public IRepositoryQuery getRepositoryQuery (TaskRepository taskRepository, String queryName) throws CoreException {
        for (IRepositoryQuery q : getRepositoryQueries(taskRepository)) {
            if (queryName.equals(q.getSummary())) {
                return q;
            }
        }
        return null;
    }

    void deleteTask (ITask task) {
        assert taskListInitialized;
        taskList.deleteTask(task);
        tasks.remove(task);
    }

    public void deleteQuery (IRepositoryQuery query) {
        assert taskListInitialized;
        if (query instanceof RepositoryQuery) {
            taskList.deleteQuery((RepositoryQuery) query);
        }
    }

    /**
     * Creates an unsubmitted task that's to be populated and submitted later.
     * The task is local until submitted and kept in the tasklist under
     * "Unsubmitted" category.
     *
     * @param taskRepository repository the task will be submitted to later.
     * @param initializingData default data (such as product/component) to
     * preset in the new task's data
     * @return the newly created task.
     * @throws CoreException tasklist or task data storage is inaccessible
     */
    public NbTask createTask (TaskRepository taskRepository, ITaskMapping initializingData) throws CoreException {
        ensureTaskListLoaded();
        AbstractTask task = createNewTask(taskRepository);
        AbstractRepositoryConnector repositoryConnector = taskRepositoryManager.getRepositoryConnector(taskRepository.getConnectorKind());
        AbstractTaskDataHandler taskDataHandler = taskRepository == localTaskRepository
                ? new LocalTaskDataHandler(taskRepository)
                : repositoryConnector.getTaskDataHandler();
        TaskAttributeMapper attributeMapper = taskDataHandler.getAttributeMapper(taskRepository);
        TaskData taskData = new TaskData(attributeMapper, repositoryConnector.getConnectorKind(), taskRepository.getRepositoryUrl(), "");
        taskDataHandler.initializeTaskData(taskRepository, taskData, initializingData, new NullProgressMonitor());
        initializeTask(repositoryConnector, taskData, task, taskRepository);
        return MylynSupport.getInstance().toNbTask(task);
    }
    
    public NbTask createSubtask (NbTask parentTask) throws CoreException {
        ensureTaskListLoaded();
        TaskRepository taskRepository = taskRepositoryManager.getRepository(parentTask.getDelegate().getRepositoryUrl());
        if (taskRepository == null || parentTask.isUnsubmittedRepositoryTask()) {
            throw new IllegalStateException("Task repository: " + parentTask.getDelegate().getRepositoryUrl()
                    + " - parent: " + parentTask.isUnsubmittedRepositoryTask());
        }
        AbstractTask task = createNewTask(taskRepository);
        AbstractRepositoryConnector repositoryConnector = taskRepositoryManager.getRepositoryConnector(taskRepository.getConnectorKind());
        AbstractTaskDataHandler taskDataHandler = repositoryConnector.getTaskDataHandler();
        
        TaskAttributeMapper attributeMapper = repositoryConnector.getTaskDataHandler().getAttributeMapper(taskRepository);
        TaskData taskData = new TaskData(attributeMapper, repositoryConnector.getConnectorKind(), taskRepository.getRepositoryUrl(), "");
        taskDataHandler.initializeSubTaskData(taskRepository, taskData, parentTask.getTaskDataState().getRepositoryData(), new NullProgressMonitor());
        initializeTask(repositoryConnector, taskData, task, taskRepository);        
        return MylynSupport.getInstance().toNbTask(task);
    }

    private void initializeTask (AbstractRepositoryConnector repositoryConnector, TaskData taskData, AbstractTask task, TaskRepository taskRepository) throws CoreException {
        ITaskMapping mapping = repositoryConnector.getTaskMapping(taskData);
        String taskKind = mapping.getTaskKind();
        if (taskKind != null && taskKind.length() > 0) {
            task.setTaskKind(taskKind);
        }
        ITaskDataWorkingCopy workingCopy = taskDataManager.createWorkingCopy(task, taskData);
        workingCopy.save(null, null);
        repositoryConnector.updateNewTaskFromTaskData(taskRepository, task, taskData);
        String summary = mapping.getSummary();
        if (summary != null && summary.length() > 0) {
            task.setSummary(summary);
        }
        if (taskRepository == localTaskRepository) {
            taskList.addTask(task);
        } else {
            taskList.addTask(task, taskList.getUnsubmittedContainer(task.getAttribute(ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_REPOSITORY_URL)));
        }
        task.setAttribute(AbstractNbTaskWrapper.ATTR_NEW_UNREAD, Boolean.TRUE.toString());
    }

    private AbstractTask createNewTask (TaskRepository taskRepository) {
        AbstractTask task = new LocalTask(String.valueOf(taskList.getNextLocalTaskId()), Bundle.MSG_NewTaskSummary());
        if (taskRepository != localTaskRepository) {
            task.setSynchronizationState(ITask.SynchronizationState.OUTGOING_NEW);
            task.setAttribute(ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_CONNECTOR_KIND, taskRepository.getConnectorKind());
            task.setAttribute(ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_REPOSITORY_URL, taskRepository.getUrl());
        }
        return task;
    }

    public IRepositoryQuery createNewQuery (TaskRepository taskRepository, String queryName) throws CoreException {
        ensureTaskListLoaded();
        assert taskRepositoryManager.getRepositoryConnector(taskRepository.getConnectorKind()) != null
                : "Did you forget to implement RepositoryConnectorProvider?";
        IRepositoryQuery query = repositoryModel.createRepositoryQuery(taskRepository);
        assert query instanceof RepositoryQuery;
        query.setSummary(queryName);
        return query;
    }
    
    public CommandFactory getCommandFactory () throws CoreException {
        if (factory == null) {
            ensureTaskListLoaded();
            factory = CommandsAccessor.INSTANCE.getCommandFactory(taskList, taskDataManager,
                    taskRepositoryManager, repositoryModel);
        }
        return factory;
    }

    void markTaskSeen (final ITask task, boolean seen) {
        ITask.SynchronizationState syncState = task.getSynchronizationState();
        taskDataManager.setTaskRead(task, seen);
        if (!seen && syncState == task.getSynchronizationState()
                && syncState == ITask.SynchronizationState.OUTGOING
                && task instanceof AbstractTask) {
            // mylyn does not set to CONFLICT status
            try {
                taskList.run(new ITaskListRunnable() {
                    @Override
                    public void execute (IProgressMonitor monitor) throws CoreException {
                        ((AbstractTask) task).setSynchronizationState(ITask.SynchronizationState.CONFLICT);
                    }
                });
                taskList.notifyElementChanged(task);
            } catch (CoreException ex) {
                LOG.log(Level.INFO, null, ex);
            }
        }
        task.setAttribute(ATTR_TASK_INCOMING_NEW, null);
    }

    /**
     * Returns a repository for the given connector and URL.
     * If such a repository does not yet exist, creates one and registers it in the mylyn infrastructure.
     *
     * @param repositoryConnector connector handling the given repository
     * @param repositoryUrl  task repository URL
     * @return registered repository
     */
    public TaskRepository getTaskRepository (AbstractRepositoryConnector repositoryConnector, String repositoryUrl) {
        taskRepositoryManager.addRepositoryConnector(repositoryConnector);
        TaskRepository repository = taskRepositoryManager.getRepository(repositoryConnector.getConnectorKind(), repositoryUrl);
        if (repository == null) {
            repository = new TaskRepository(repositoryConnector.getConnectorKind(), repositoryUrl);
            addTaskRepository(repositoryConnector, repository);
        }
        return repository;
    }

    public void setRepositoryUrl (TaskRepository repository, String url) throws CoreException {
        String oldUrl = repository.getRepositoryUrl();
        if (!url.equals(oldUrl)) {
            ensureTaskListLoaded();
            assert taskRepositoryManager.getRepositoryConnector(repository.getConnectorKind()) != null
                : "Did you forget to implement RepositoryConnectorProvider?";
            try {
                for (ITask task : taskList.getAllTasks()) {
                    if (url.equals(task.getAttribute(ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_REPOSITORY_URL))) {
                        taskDataManager.refactorRepositoryUrl(task, task.getRepositoryUrl(), url);
                    } else if (oldUrl.equals(task.getRepositoryUrl())) {
                        taskDataManager.refactorRepositoryUrl(task, url, url);
                    }
                }
            } catch (Throwable t) {
                // log, but set the rapository & co
                LOG.log(Level.WARNING, null, t);
            }
            taskList.refactorRepositoryUrl(oldUrl, url);
            repository.setRepositoryUrl(url);
            taskRepositoryManager.notifyRepositoryUrlChanged(repository, oldUrl);
        }
    }

    NbTask getOrCreateTask (TaskRepository taskRepository, String taskId, boolean addToTaskList) throws CoreException {
        ensureTaskListLoaded();
        ITask task = taskList.getTask(taskRepository.getUrl(), taskId);
        if (task == null) {
            task = repositoryModel.createTask(taskRepository, taskId);
            ((AbstractTask) task).setSynchronizationState(ITask.SynchronizationState.INCOMING_NEW);
            if (addToTaskList) {
                // ensure the task is in the tasklist
                taskList.addTask(task);
            }
        }
        return toNbTask(task);
    }

    TaskRepository getTaskRepositoryFor (ITask task) {
        if (isUnsubmittedRepositoryTask(task)) {
            return taskRepositoryManager.getRepository(
                    task.getAttribute(ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_CONNECTOR_KIND),
                    task.getAttribute(ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_REPOSITORY_URL));
        } else {
            return taskRepositoryManager.getRepository(task.getConnectorKind(), task.getRepositoryUrl());
        }
    }

    void finish () throws CoreException {
        taskList.removeChangeListener(taskListListener);
        synchronized (taskList) {
            // make sure we save all changes
            dirty = true;
        }
        persist(true);
    }

    private void addTaskRepository (AbstractRepositoryConnector repositoryConnector, TaskRepository taskRepository) {
        if (!taskRepository.getConnectorKind().equals(repositoryConnector.getConnectorKind())) {
            throw new IllegalArgumentException("The given task repository is not managed by the given repository connector");
        }
        taskRepositoryManager.addRepositoryConnector(repositoryConnector);
        taskRepositoryManager.addRepository(taskRepository);
        // assert, noone should add two repository instances with the same URL
        assert taskRepository == taskRepositoryManager.getRepository(repositoryConnector.getConnectorKind(), taskRepository.getUrl());
    }
// TODO is this required? The task may be left in the storage indefinitely.
//    /**
//     * Removes the repository, its queries and tasks permanently from the mylyn
//     * infrastructure. The action is irreversible, be careful.
//     *
//     * @param taskRepository repository to remove
//     */
//    public void deleteTaskRepository (TaskRepository taskRepository) {
//        // queries to delete
//        Set<RepositoryQuery> queries = taskList.getRepositoryQueries(taskRepository.getUrl());
//        // tasks to delete
//        Set<ITask> tasks = taskList.getTasks(taskRepository.getUrl());
//        // unsubmitted tasks to delete
//        tasks.addAll(taskList.getUnsubmittedContainer(taskRepository.getUrl()).getChildren());
//        for (RepositoryQuery query : queries) {
//            taskList.deleteQuery(query);
//        }
//        for (ITask task : tasks) {
//            taskList.deleteTask(task);
//        }
//        taskRepositoryManager.removeRepository(taskRepository);
//    }

    private synchronized void ensureTaskListLoaded () throws CoreException {
        if (!taskListInitialized) {
            if (result == null) {
                LookupListener lookupListener = new LookupListener() {
                    @Override
                    public void resultChanged (LookupEvent ev) {
                        registerConnectors();
                    }
                };
                result = Lookup.getDefault().lookupResult(RepositoryConnectorProvider.class);
                result.addLookupListener(lookupListener);
            }
            registerConnectors();
            try {
                if (taskListStorageFile.length() > 0) {
                    taskListWriter.readTaskList(taskList, taskListStorageFile);
                }
            } catch (CoreException ex) {
                LOG.log(Level.INFO, null, ex);
                throw new CoreException(new Status(ex.getStatus().getSeverity(), ex.getStatus().getPlugin(), "Cannot deserialize tasklist"));
            } finally {
                taskListInitialized = true;
            }
        }
    }

    void persist (final boolean removeUnseenOrphanedTasks) throws CoreException {
        if (taskListInitialized) {
            taskList.run(new ITaskListRunnable() {
                @Override
                public void execute (IProgressMonitor monitor) throws CoreException {
                    boolean save;
                    synchronized (taskList) {
                        save = dirty;
                        dirty = false;
                    }
                    if (!save) {
                        return;
                    }
                    try {
                        if (removeUnseenOrphanedTasks) {
                            Set<ITask> orphanedUnseenTasks = new LinkedHashSet<ITask>();
                            for (UnmatchedTaskContainer cont : taskList.getUnmatchedContainers()) {
                                for (ITask task : cont.getChildren()) {
                                    if (task.getSynchronizationState() == ITask.SynchronizationState.INCOMING_NEW
                                            || Boolean.TRUE.toString().equals(task.getAttribute(ATTR_TASK_INCOMING_NEW))) {
                                        orphanedUnseenTasks.add(task);
                                    }
                                }
                            }
                            for (ITask taskToDelete : orphanedUnseenTasks) {
                                deleteTask(taskToDelete);
                            }
                        }
                        taskListStorageFile.getParentFile().mkdirs();
                        backupTaskList(taskListStorageFile);
                        taskListWriter.writeTaskList(taskList, taskListStorageFile);
                    } catch (CoreException ex) {
                        LOG.log(Level.INFO, null, ex);
                        throw new CoreException(new Status(ex.getStatus().getSeverity(), ex.getStatus().getPlugin(), "Cannot persist tasklist"));
                    }
                }
            });
        }
    }
    
    private Preferences getPreferences() {
        return NbPreferences.forModule(MylynSupport.class);
    }

    private void attachListeners () {
        taskRepositoryManager.addListener(taskRepositoryManagerListener = new IRepositoryListener() {
            @Override
            public void repositoryAdded (TaskRepository repository) {
                getRepositoryCreationTime(repository.getRepositoryUrl());
            }

            @Override
            public void repositoryRemoved (TaskRepository repository) {
                setRepositoryCreationTime(repository.getRepositoryUrl(), -1);
            }

            @Override
            public void repositorySettingsChanged (TaskRepository repository) {
            }

            @Override
            public void repositoryUrlChanged (TaskRepository repository, String oldUrl) {
                setRepositoryCreationTime(repository.getRepositoryUrl(), getRepositoryCreationTime(oldUrl));
                setRepositoryCreationTime(oldUrl, -1);
            }
        });
        taskList.addChangeListener(taskListListener = new ITaskListChangeListener() {
            @Override
            public void containersChanged (Set<TaskContainerDelta> deltas) {
                for (TaskContainerDelta delta : deltas) {
                    if (taskListInitialized && !delta.isTransient()) {
                        synchronized (taskList) {
                            dirty = true;
                        }
                        scheduleSave();
                    }
                    if (delta.getElement() instanceof ITask) {
                        // task added to the tasklist   
                        // new tasks (incoming_new) created long ago in the past
                        // should be marked as uptodate so when a repository is registener in the IDE
                        // it is not all green. Only fresh new tasks are relevant to the user
                        ITask task = (ITask) delta.getElement();
                        if ( (task.getSynchronizationState() == ITask.SynchronizationState.INCOMING_NEW 
                              || task.getSynchronizationState() == ITask.SynchronizationState.INCOMING )
                             && task.getCreationDate() != null && task.getModificationDate() != null) {
                            TaskRepository repository = taskRepositoryManager.getRepository(task.getConnectorKind(), task.getRepositoryUrl());
                            if (repository != null) {
                                long time = getRepositoryCreationTime(repository.getRepositoryUrl());
                                if (task.getModificationDate().getTime() < time) {
                                    markTaskSeen(task, true);
                                    task.setAttribute(ATTR_TASK_INCOMING_NEW, Boolean.TRUE.toString());
                                }
                            }
                        }
                        notifyListeners(task, delta);
                    }
                }
            }

            private void notifyListeners (ITask task, TaskContainerDelta delta) {
                // notify listeners
                List<TaskListener> lists;
                synchronized (taskListeners) {
                    lists = taskListeners.get(task);
                }
                if (lists != null) {
                    for (TaskListener list : lists.toArray(new TaskListener[0])) {
                        list.taskModified(task, delta);
                    }
                }
            }
        });
        taskDataManager.addListener(taskDataManagerListener = new ITaskDataManagerListener() {

            @Override
            public void taskDataUpdated (TaskDataManagerEvent event) {
                TaskDataListener.TaskDataEvent e = new TaskDataListener.TaskDataEvent(event);
                for (TaskDataListener l : taskDataListeners.toArray(new TaskDataListener[0])) {
                    l.taskDataUpdated(e);
                }
            }

            @Override
            public void editsDiscarded (TaskDataManagerEvent event) {
                taskDataUpdated(event);
            }
        });
    }

    private void setRepositoryCreationTime (String repositoryUrl, long time) {
        if (time == -1) {
            getPreferences().remove(PROP_REPOSITORY_CREATION_TIME + repositoryUrl);
        } else {
            getPreferences().putLong(PROP_REPOSITORY_CREATION_TIME + repositoryUrl, time);
        }
    }

    private long getRepositoryCreationTime (String repositoryUrl) {
        long time = getPreferences().getLong(PROP_REPOSITORY_CREATION_TIME + repositoryUrl, -1);
        if (time == -1) {
            time = System.currentTimeMillis();
            setRepositoryCreationTime(repositoryUrl, time);
        }
        return time;
    }

    private void scheduleSave () {
        saveTask.schedule(5000);
    }

    private void backupTaskList (File taskListStorageFile) {
        if (taskListStorageFile.canWrite()) {
            File backup = new File(taskListStorageFile.getParentFile(), taskListStorageFile.getName() + BACKUP_SUFFIX);
            backup.delete();
            taskListStorageFile.renameTo(backup);
        }
    }

    Collection<NbTask> toNbTasks (Collection<ITask> tasks) {
        return toNbTasks(tasks, true);
    }
    
    Collection<NbTask> toNbTasks (Collection<ITask> tasks, boolean includeUnsubmittedNewTasks) {
        Set<NbTask> nbTasks = new LinkedHashSet<NbTask>(tasks.size());
        for (ITask task : tasks) {
            if (includeUnsubmittedNewTasks || task.getSynchronizationState() != ITask.SynchronizationState.OUTGOING_NEW) {
                // remember that unsubmitted tasks are local tasks and should not be included
                // in tasks for local task repository
                nbTasks.add(toNbTask(task));
            }
        }
        return Collections.unmodifiableSet(nbTasks);
    }

    NbTask toNbTask (ITask task) {
        NbTask nbTask = null;
        if (task != null) {
            synchronized (tasks) {
                Reference<NbTask> nbTaskRef = tasks.get(task);
                if (nbTaskRef != null) {
                    nbTask = nbTaskRef.get();
                }
                if (nbTask == null) {
                    nbTask = new NbTask(task);
                    tasks.put(task, new SoftReference<NbTask>(nbTask));
                }
            }
        }
        return nbTask;
    }

    static Set<ITask> toMylynTasks (Set<NbTask> tasks) {
        Set<ITask> mylynTasks = new LinkedHashSet<ITask>(tasks.size());
        for (NbTask task : tasks) {
            mylynTasks.add(task.getDelegate());
        }
        return mylynTasks;
    }

    void addTaskListener (ITask task, TaskListener listener) {
        List<TaskListener> list;
        synchronized (taskListeners) {
            list = taskListeners.get(task);
            if (list == null) {
                list = new CopyOnWriteArrayList<TaskListener>();
                taskListeners.put(task, list);
            }
        }
        list.add(listener);
        assert !taskPerList.containsKey(listener) : "One task per one listener";
        taskPerList.put(listener, task);
    }

    void removeTaskListener (TaskListener listener) {
        ITask task = taskPerList.get(listener);
        if (task != null) {
            
        }
    }

    void removeTaskListener (ITask task, TaskListener listener) {
        synchronized (taskListeners) {
            List<TaskListener> list = taskListeners.get(task);
            if (list != null) {
                list.remove(listener);
            }
        }
        taskPerList.remove(listener);
    }

    NbTaskDataModel getTaskDataModel (NbTask task) {
        assert taskListInitialized;
        ITask mylynTask = task.getDelegate();
        mylynTask.setAttribute(MylynSupport.ATTR_TASK_INCOMING_NEW, null);
        TaskRepository taskRepository = getTaskRepositoryFor(mylynTask);
        try {
            ITaskDataWorkingCopy workingCopy = taskDataManager.getWorkingCopy(mylynTask);
            if (workingCopy instanceof TaskDataState && workingCopy.getLastReadData() == null) {
                ((TaskDataState) workingCopy).setLastReadData(workingCopy.getRepositoryData());
            }
            return new NbTaskDataModel(taskRepository, task, workingCopy);
        } catch (CoreException ex) {
            MylynSupport.LOG.log(Level.INFO, null, ex);
            return null;
        }
    }

    private void registerConnectors () {
        for (RepositoryConnectorProvider provider : result.allInstances()) {
            AbstractRepositoryConnector connector = provider.getConnector();
            if (connector != null) {
                taskRepositoryManager.addRepositoryConnector(connector);
            }
        }
    }

    void editorOpened (final ITask task) throws CoreException {
        // mark the task as not read pending to block incoming refreshes
        // from rewriting last seen task data while the editor is open
        // maybe we should follow the mylyn's way of handling incoming updates
        if (task instanceof AbstractTask) {
            taskList.run(new ITaskListRunnable() {
                @Override
                public void execute (IProgressMonitor monitor) throws CoreException {
                    ((AbstractTask) task).setMarkReadPending(false);
                }
            }, null, true);
        }
    }

    void editorClosing (final ITask task, final TaskData td) throws CoreException {
        // copy repository task data into last seen data
        if (task instanceof AbstractTask) {
            taskList.run(new ITaskListRunnable() {
                @Override
                public void execute (IProgressMonitor monitor) throws CoreException {
                    ((AbstractTask) task).setMarkReadPending(true);
                    taskDataManager.putUpdatedTaskData(task, td, true);
                }
            }, null, true);
        }
    }

    boolean isUnsubmittedRepositoryTask (ITask task) {
        return task.getSynchronizationState() == ITask.SynchronizationState.OUTGOING_NEW
                && task.getAttribute(ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_CONNECTOR_KIND) != null;
    }

    void taskModified (ITask task) {
        taskList.notifyElementChanged(task);
    }

    public void notifyCredentialsChanged(TaskRepository repository) {
        taskRepositoryManager.notifyRepositorySettingsChanged(repository, new TaskRepositoryDelta(TaskRepositoryDelta.Type.CREDENTIALS));
    }
}
