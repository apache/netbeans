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

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.DateRange;
import org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta;
import org.eclipse.mylyn.tasks.core.ITask;
import org.netbeans.modules.mylyn.util.internal.TaskListener;
import org.openide.util.WeakListeners;

/**
 *
 * @author Ondrej Vrabec
 */
public final class NbTask {
    private final ITask delegate;
    private final TaskListenerImpl list;
    private final List<NbTaskListener> listeners;
    private SynchronizationState syncState;
    
    NbTask (ITask task) {
        this.delegate = task;
        this.listeners = new CopyOnWriteArrayList<NbTaskListener>();
        updateSynchronizationState();
        list = new TaskListenerImpl();
        MylynSupport.getInstance().addTaskListener(task, WeakListeners.create(TaskListener.class,
                list,
                MylynSupport.getInstance()));
    }

    public SynchronizationState getSynchronizationState () {
        return syncState;
    }

    public boolean isOutgoing () {
        SynchronizationState state = getSynchronizationState();
        return state == SynchronizationState.CONFLICT
                || state == SynchronizationState.OUTGOING
                || state == SynchronizationState.OUTGOING_NEW;
    }

    public String getTaskId () {
        return delegate.getTaskId();
    }

    ITask getDelegate () {
        return delegate;
    }

    public void markSeen (boolean seen) {
        MylynSupport.getInstance().markTaskSeen(getDelegate(), seen);
    }

    public void delete () {
        MylynSupport.getInstance().deleteTask(getDelegate());
    }
    
    public void discardLocalEdits () throws CoreException {
        MylynSupport.getInstance().discardLocalEdits(delegate);
    }

    public void setAttribute (String attributeName, String attributeValue) {
        delegate.setAttribute(attributeName, attributeValue);
    }

    public String getAttribute (String attributeName) {
        return delegate.getAttribute(attributeName);
    }

    @Override
    public String toString () {
        return delegate.toString();
    }

    @Override
    public int hashCode () {
        return delegate.hashCode();
    }

    @Override
    public boolean equals (Object obj) {
        if (obj instanceof NbTask) {
            return delegate.equals(((NbTask) obj).delegate);
        } else {
            return false;
        }
    }

    public String getSummary () {
        return delegate.getSummary();
    }

    public Date getModificationDate () {
        return delegate.getModificationDate();
    }

    public Date getCreationDate () {
        return delegate.getCreationDate();
    }

    public boolean isCompleted () {
        return delegate.isCompleted();
    }

    public void setSummary (String summary) {
        delegate.setSummary(summary);
    }

    /**
     * Adds a listener to the tasklist. The listener will be notified on a change
     * in tasks's content (summary, description, etc.).
     *
     * @param listener listener
     */
    public void addNbTaskListener (NbTaskListener listener) {
        listeners.add(listener);
    }

    public void removeNbTaskListener (NbTaskListener listener) {
        listeners.remove(listener);
    }

    public String getRepositoryUrl () {
        return delegate.getRepositoryUrl();
    }

    private void updateSynchronizationState () {
        switch (delegate.getSynchronizationState()) {
            case CONFLICT:
                syncState = SynchronizationState.CONFLICT;
                break;
            case INCOMING:
                syncState = SynchronizationState.INCOMING;
                break;
            case INCOMING_NEW:
                syncState = SynchronizationState.INCOMING_NEW;
                break;
            case OUTGOING:
                syncState = SynchronizationState.OUTGOING;
                break;
            case OUTGOING_NEW:
                syncState = SynchronizationState.OUTGOING_NEW;
                break;
            case SYNCHRONIZED:
            default:
                syncState = SynchronizationState.SYNCHRONIZED;
                break;
        }
    }

    /**
     * Returns task data model for editor page.
     *
     * @return task data model the editor page should access - read and edit -
     * or null when no data for the task found
     */
    public NbTaskDataModel getTaskDataModel () {
        return MylynSupport.getInstance().getTaskDataModel(this);
    }
    
    public NbTaskDataState getTaskDataState () throws CoreException {
        return MylynSupport.getInstance().getTaskDataState(this);
    }

    public String getTaskKey () {
        return delegate.getTaskKey();
    }

    boolean isUnsubmittedRepositoryTask () {
        return syncState == SynchronizationState.OUTGOING_NEW
                && MylynSupport.getInstance().isUnsubmittedRepositoryTask(delegate);
    }

    boolean isLocal () {
        return LocalRepositoryConnector.CONNECTOR_KIND.equals(delegate.getConnectorKind());
    }

    public final String getPrivateNotes () {
        if (delegate instanceof AbstractTask) {
            return ((AbstractTask) delegate).getNotes();
        } else {
            return null;
        }
    }

    public final void setPrivateNotes (String notes) {
        if (delegate instanceof AbstractTask) {
            ((AbstractTask) delegate).setNotes(notes);
        }
    }

    public final Date getDueDate () {
        return delegate.getDueDate();
    }

    public final void setDueDate (Date dueDate) {
        delegate.setDueDate(dueDate);
    }

    public final NbDateRange getScheduleDate () {
        if (delegate instanceof AbstractTask) {
            DateRange date = ((AbstractTask) delegate).getScheduledForDate();
            return date == null ? null : new NbDateRange(date);
        } else {
            return null;
        }
    }

    public final void setScheduleDate (NbDateRange scheduledDate) {
        if (delegate instanceof AbstractTask) {
            ((AbstractTask) delegate).setScheduledForDate(scheduledDate == null ? null : scheduledDate.getDelegate());
        }
    }

    public int getEstimate () {
        if (delegate instanceof AbstractTask) {
            return ((AbstractTask) delegate).getEstimatedTimeHours();
        } else {
            return 0;
        }
    }

    public void setEstimate (int estimatedHours) {
        if (delegate instanceof AbstractTask) {
            ((AbstractTask) delegate).setEstimatedTimeHours(estimatedHours);
        }
    }

    public static enum SynchronizationState {
        INCOMING_NEW,
        INCOMING,
        OUTGOING_NEW,
        OUTGOING,
        SYNCHRONIZED,
        CONFLICT
    }

    private class TaskListenerImpl implements TaskListener {

        public TaskListenerImpl () {
        }

        @Override
        public void taskModified (ITask task, TaskContainerDelta delta) {
            assert task == NbTask.this.delegate;
            SynchronizationState oldState = getSynchronizationState();
            updateSynchronizationState();
            NbTaskListener.TaskEvent ev = new NbTaskListener.TaskEvent(NbTask.this, delta,
                    oldState != getSynchronizationState());
            for (NbTaskListener list : listeners.toArray(new NbTaskListener[0])) {
                list.taskModified(ev);
            }
        }
    }
    
}
