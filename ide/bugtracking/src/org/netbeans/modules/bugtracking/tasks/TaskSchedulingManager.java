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
package org.netbeans.modules.bugtracking.tasks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.RepositoryRegistry;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * Handles issue scheduling info and provides tasks with set scheduling.
 *
 * @author Ondrej Vrabec
 */
public final class TaskSchedulingManager {

    public static final String PROPERTY_SCHEDULED_TASKS_CHANGED = "TaskSchedulingManager.scheduledTasksChanged"; //NOI18N

    private static TaskSchedulingManager instance;
    private final Map<IssueImpl, IssueScheduleInfo> scheduledTasks;
    // this is needed because not all repositories are available immediately, see team repos
    private final Map<String, Set<String>> persistedTasks;
    private final Set<String> initializedRepositories;
    private final PropertyChangeSupport support;
    private static final String SEP = "###"; //NOI18N
    private static final String PREF_SCHEDULED = "TaskSchedulingManager.scheduledTasks."; //NOI18N
    private boolean initializing;
    private static final RequestProcessor RP = new RequestProcessor("TaskSchedulingManager"); //NOI18N
    private final Set<IssueImpl> issuesToHandle;
    private final Set<IssueImpl> deletedIssues;
    private final Task handleTask;

    private TaskSchedulingManager() {
        support = new PropertyChangeSupport(this);
        initializedRepositories = Collections.synchronizedSet(new HashSet<String>());
        scheduledTasks = Collections.synchronizedMap(new WeakHashMap<IssueImpl, IssueScheduleInfo>());
        persistedTasks = new HashMap<String, Set<String>>();
        issuesToHandle = new LinkedHashSet<IssueImpl>();
        deletedIssues = new LinkedHashSet<IssueImpl>();
        handleTask = RP.create(new HandleTask());
        loadTasks();
        RepositoryRegistry.getInstance().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (RepositoryRegistry.EVENT_REPOSITORIES_CHANGED.equals(evt.getPropertyName())) {
                    repositoriesChanged(evt);
                }
            }
        });
    }

    /**
     * Returns the only instance of this class.
     *
     * @return instance
     */
    public static synchronized TaskSchedulingManager getInstance() {
        if (instance == null) {
            instance = new TaskSchedulingManager();
        }
        return instance;
    }

    /**
     * Handles a task, compares its scheduling info with the cached one and in case there is a change an event is fired.
     *
     * @param task task to handle.
     */
    public void handleTask(IssueImpl task) {
        boolean schedule;
        synchronized (issuesToHandle) {
            schedule = issuesToHandle.add(task);
        }
        if (schedule) {
            handleTask.schedule(500);
        }
    }

    /**
     * TODO
     *
     * @param task task to handle.
     */
    public void taskDeleted (IssueImpl task) {
        boolean schedule;
        synchronized (deletedIssues) {
            schedule = deletedIssues.add(task);
        }
        if (schedule) {
            handleTask.schedule(500);
        }
    }

    /**
     * Adds a listener notified when the list of scheduled tasks changes.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Removes a previously added listener. Will no longer be notified.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    /**
     * Returns all scheduled tasks for given repositories. Note that it may take some time when the tasks have not yet been initialized. in that case it may end up in network access.
     *
     * @param repositories
     * @return array of tasks with scheduling info set.
     */
    public IssueImpl[] getScheduledTasks(RepositoryImpl... repositories) {
        return getScheduledTasks(null, repositories);
    }

    /**
     * Returns all scheduled tasks for given repositories. Note that it may take some time when the tasks have not yet been initialized. in that case it may end up in network access.
     *
     * @param repositories
     * @param restrictionInterval - returned tasks are within this interval
     * @return array of tasks with scheduling info set.
     */
    public IssueImpl[] getScheduledTasks(IssueScheduleInfo restrictionInterval, RepositoryImpl... repositories) {
        Set<String> repositoryIds = new HashSet<String>(repositories.length);
        for (RepositoryImpl repo : repositories) {
            repositoryIds.add(repo.getId());
        }
        initializeTasks(repositoryIds);
        // flush changes        
        handleIssues();
        
        Set<IssueImpl> allTasks = new HashSet<IssueImpl>(Arrays.asList(scheduledTasks.keySet().toArray(new IssueImpl[0])));
        for (Iterator<IssueImpl> it = allTasks.iterator(); it.hasNext();) {
            IssueImpl issue = it.next();
            if (!repositoryIds.contains(issue.getRepositoryImpl().getId()) || !isInInterval(issue.getSchedule(), restrictionInterval)) {
                it.remove();
            }
        }
        return allTasks.toArray(new IssueImpl[0]);
    }

    public boolean isInInterval(IssueScheduleInfo schedule, IssueScheduleInfo restrictionInterval) {
        if (restrictionInterval == null) {
            return true;
        }

        Calendar scheduleStart = Calendar.getInstance();
        scheduleStart.setTime(schedule.getDate());
        Calendar scheduleEnd = Calendar.getInstance();
        scheduleEnd.setTime(schedule.getDate());
        scheduleEnd.add(Calendar.DATE, schedule.getInterval());

        Calendar intervalStart = Calendar.getInstance();
        intervalStart.setTime(restrictionInterval.getDate());
        Calendar intervaEnd = Calendar.getInstance();
        intervaEnd.setTime(restrictionInterval.getDate());
        intervaEnd.add(Calendar.DATE, restrictionInterval.getInterval());

        boolean yearIsLower = intervalStart.get(Calendar.YEAR) < scheduleStart.get(Calendar.YEAR);
        boolean start = yearIsLower ? yearIsLower
                : intervalStart.get(Calendar.YEAR) == scheduleStart.get(Calendar.YEAR)
                && intervalStart.get(Calendar.DAY_OF_YEAR) <= scheduleStart.get(Calendar.DAY_OF_YEAR);

        boolean yearIsHigher = intervaEnd.get(Calendar.YEAR) > scheduleEnd.get(Calendar.YEAR);
        boolean end = yearIsHigher ? yearIsHigher
                : intervaEnd.get(Calendar.YEAR) == scheduleEnd.get(Calendar.YEAR)
                && intervaEnd.get(Calendar.DAY_OF_YEAR) >= scheduleEnd.get(Calendar.DAY_OF_YEAR);

        return start && end;
    }

    private void fireChange() {
        support.firePropertyChange(PROPERTY_SCHEDULED_TASKS_CHANGED, null, null);
    }

    private void persist() {
        Map<String, String> toPersist = new HashMap<String, String>();
        synchronized (persistedTasks) {
            for (Map.Entry<String, Set<String>> e : persistedTasks.entrySet()) {
                StringBuilder sb = new StringBuilder();
                for (String taskId : e.getValue()) {
                    sb.append(taskId);
                    sb.append(SEP);
                }
                toPersist.put(e.getKey(), sb.toString());
            }
        }
        for (Map.Entry<String, String> e : toPersist.entrySet()) {
            if (e.getValue().isEmpty()) {
                NbPreferences.forModule(TaskSchedulingManager.class).remove(PREF_SCHEDULED + e.getKey());
            } else {
                NbPreferences.forModule(TaskSchedulingManager.class).put(PREF_SCHEDULED + e.getKey(), e.getValue());
            }
        }
    }

    private void repositoriesChanged(PropertyChangeEvent evt) {
        List<RepositoryImpl> oldRepositories = (List) (evt.getOldValue() == null ? Collections.emptyList() : evt.getOldValue());
        List<RepositoryImpl> newRepositories = (List) (evt.getNewValue() == null ? Collections.emptyList() : evt.getNewValue());
        Set<RepositoryImpl> removed = new HashSet<RepositoryImpl>(oldRepositories);
        removed.removeAll(newRepositories);

        if (!removed.isEmpty()) {

            // do we want to delete the data permanently???
            // what if it's a team repo or user recreates the repository???
//            synchronized (persistedTasks) {
//                remove from persisting data
//            }
            boolean changed = false;
            for (IssueImpl impl : getScheduledTasks()) {
                if (removed.contains(impl.getRepositoryImpl())) {
                    if (null != scheduledTasks.remove(impl)) {
                        changed = true;
                    }
                }
            }
            if (changed) {
                fireChange();
            }
        }
    }

    private void loadTasks() {
        Preferences pref = NbPreferences.forModule(TaskSchedulingManager.class);
        try {
            for (String key : pref.keys()) {
                if (key.startsWith(PREF_SCHEDULED)) {
                    String repositoryId = key.substring(PREF_SCHEDULED.length());
                    String tasks = pref.get(key, "");
                    for (String taskId : tasks.split(SEP)) {
                        if (!taskId.isEmpty()) {
                            getRepositoryTasks(repositoryId).add(taskId);
                        }
                    }
                }
            }
        } catch (BackingStoreException ex) {
            Logger.getLogger(TaskSchedulingManager.class.getName()).log(Level.INFO, null, ex);
        }
    }

    private Set<String> getRepositoryTasks(String repositoryId) {
        synchronized (persistedTasks) {
            Set<String> tasks = persistedTasks.get(repositoryId);
            if (tasks == null) {
                tasks = Collections.synchronizedSet(new HashSet<String>());
                persistedTasks.put(repositoryId, tasks);
            }
            return tasks;
        }
    }

    private void initializeTasks(Set<String> repositories) {
        boolean fireChange = false;
        try {
            initializing = true;
            String[] repositoryIds;
            synchronized (persistedTasks) {
                repositoryIds = persistedTasks.keySet().toArray(new String[persistedTasks.size()]);
            }
            for (String repositoryId : repositoryIds) {
                synchronized (initializedRepositories) {
                    // refresh only not yet initialized repositories and the requested ones
                    if (!initializedRepositories.contains(repositoryId) && repositories.contains(repositoryId)) {
                        if (initializeTasks(repositoryId)) {
                            fireChange = true;
                        }
                    }
                }
            }
        } finally {
            initializing = false;
            if (fireChange) {
                fireChange();
            }
        }
    }

    private boolean initializeTasks(String repositoryId) {
        RepositoryImpl repository = null;
        for (RepositoryImpl repo : RepositoryRegistry.getInstance().getKnownRepositories(false, true)) {
            if (repositoryId.equals(repo.getId())) {
                repository = repo;
                break;
            }
        }
        if (repository == null) {
            return false;
        } else {
            initializeTasks(repository);
            return true;
        }
    }

    private void initializeTasks(RepositoryImpl repository) {
        initializedRepositories.add(repository.getId());
        String[] taskIds = getRepositoryTasks(repository.getId()).toArray(new String[0]);
        if (taskIds.length > 0) {
            Collection<IssueImpl> issues = repository.getIssueImpls(taskIds);
            for (IssueImpl impl : issues) {
                handleSingleIssue(impl);
            }
        }
    }

    private boolean handleSingleIssue (IssueImpl issue) {
        IssueScheduleInfo info = issue.getSchedule();
        boolean changed = false;
        
        synchronized (initializedRepositories) {
            if (info == null) {
                if (scheduledTasks.remove(issue) != null) {
                    changed = true;
                }
                if (getRepositoryTasks(issue.getRepositoryImpl().getId()).remove(issue.getID())) {
                    persist();
                }
            } else {
                IssueScheduleInfo oldInfo = scheduledTasks.put(issue, info);
                if (!info.equals(oldInfo)) {
                    changed = true;
                }
                if (getRepositoryTasks(issue.getRepositoryImpl().getId()).add(issue.getID())) {
                    persist();
                }
            }
        }
        return changed;
    }

    private boolean handleDeletedIssue (IssueImpl issue) {
        boolean changed = false;
        synchronized (initializedRepositories) {
            if (scheduledTasks.remove(issue) != null) {
                changed = true;
            }
            if (getRepositoryTasks(issue.getRepositoryImpl().getId()).remove(issue.getID())) {
                persist();
            }
        }
        return changed;
    }

    private class HandleTask implements Runnable {
        @Override
        public void run() {
            handleIssues();
        }
    }
    
    private void handleIssues() {
        IssueImpl[] issues;
        synchronized (issuesToHandle) {
            issues = issuesToHandle.toArray(new IssueImpl[0]);
            issuesToHandle.clear();
        }
        boolean changed = false;
        for (IssueImpl issue : issues) {
            changed |= handleSingleIssue(issue);
        }
        synchronized (deletedIssues) {
            issues = deletedIssues.toArray(new IssueImpl[0]);
            deletedIssues.clear();
        }
        for (IssueImpl issue : issues) {
            changed |= handleDeletedIssue(issue);
        }
        if (changed && !initializing) {
            fireChange();
        }
    }

}
