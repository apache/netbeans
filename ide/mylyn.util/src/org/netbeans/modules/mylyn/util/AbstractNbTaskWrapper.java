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

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.bugtracking.commons.AttachmentsPanel;
import org.netbeans.modules.bugtracking.spi.IssueController;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.netbeans.modules.bugtracking.spi.IssueScheduleProvider;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Ondrej Vrabec
 */
public abstract class AbstractNbTaskWrapper {
    
    static final String ATTR_NEW_UNREAD = "NetBeans.task.markedNewUnread"; //NOI18N
    private static final Object MODEL_LOCK = new Object();
    private static final Logger LOG = Logger.getLogger(AbstractNbTaskWrapper.class.getName());
    private static final RequestProcessor RP = new RequestProcessor("NBTasks"); //NOI18N
    public static final String NEW_ATTACHMENT_ATTRIBUTE_ID = "nb.attachments.new"; //NOI18N
    private static final String NB_NEW_ATTACHMENT_PATCH_ATTR_ID = "nb.newattachment.patch"; //NOI18N
    private static final String NB_NEW_ATTACHMENT_FILE_ATTR_ID = "nb.newattachment.file"; //NOI18N
    private static final String NB_NEW_ATTACHMENT_CONTENT_TYPE_ATTR_ID = "nb.newattachment.contentType"; //NOI18N
    private static final String NB_NEW_ATTACHMENT_DESC_ATTR_ID = "nb.newattachment.desc"; //NOI18N
    private static final String NB_NEW_ATTACHMENT_ATTR_ID = "nb.newattachment."; //NOI18N

    private NbTask task;
    private NbTaskDataModel model;
    private NbTaskDataModel.NbTaskDataModelListener list;
    private boolean readPending;
    private final TaskDataListenerImpl taskDataListener;
    private final TaskListenerImpl taskListener;
    private Reference<TaskData> repositoryDataRef;
    private final RequestProcessor.Task repositoryTaskDataLoaderTask;
    private final PropertyChangeSupport support;
    
    /** PRIVATE TASK ATTRIBUTES **/
    private String privateNotes;
    private Date dueDate;
    private boolean dueDateModified;
    private NbDateRange scheduleDate;
    private boolean scheduleDateModified;
    private Integer estimate;
    /** PRIVATE TASK ATTRIBUTES **/

    public AbstractNbTaskWrapper (NbTask task) {
        this.task = task;
        this.repositoryDataRef = new SoftReference<TaskData>(null);
        support = new PropertyChangeSupport(this);
        repositoryTaskDataLoaderTask = RP.create(new Runnable() {
            @Override
            public void run () {
                loadRepositoryTaskData();
            }
        });
        MylynSupport mylynSupp = MylynSupport.getInstance();
        taskDataListener = new TaskDataListenerImpl();
        mylynSupp.addTaskDataListener(WeakListeners.create(TaskDataListener.class, taskDataListener, mylynSupp));
        taskListener = new TaskListenerImpl();
        task.addNbTaskListener(WeakListeners.create(NbTaskListener.class, taskListener, mylynSupp));
    }

    /**
     * Returns the id of the given task or null if task is new
     * @param task
     * @return id or null
     */
    public static String getID (NbTask task) {
        if (task.isUnsubmittedRepositoryTask()) {
            return "-" + task.getTaskId();
        }
        return task.getTaskId();
    }

    protected static boolean attachmentAttributesDiffer (TaskAttribute ta1, TaskAttribute ta2) {
        if (ta2 == null) {
            return true;
        }
        String value1 = ta1.getValue();
        String value2 = ta2.getValue();
        boolean changes = !value1.equals(value2);
        if (!changes) {
            // is a child attribue changed??
            for (TaskAttribute childTA : ta1.getAttributes().values()) {
                if (attachmentAttributesDiffer(childTA, ta2.getAttribute(childTA.getId()))) {
                    changes = true;
                    break;
                }
            }
        }
        return changes;
    }

    public final void addPropertyChangeListener (PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public final void removePropertyChangeListener (PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    protected final TaskData getRepositoryTaskData () {
        TaskData taskData = repositoryDataRef.get();
        if (taskData == null) {
            if (EventQueue.isDispatchThread()) {
                repositoryTaskDataLoaderTask.schedule(100);
            } else {
                return loadRepositoryTaskData();
            }
        }
        return taskData;
    }

    private TaskData loadRepositoryTaskData () {
        // this method is time consuming
        assert !EventQueue.isDispatchThread();
        TaskData td = repositoryDataRef.get();
        if (td != null) {
            return td;
        }
        try {
            NbTaskDataState taskDataState = task.getTaskDataState();
            if (taskDataState != null) {
                td = taskDataState.getRepositoryData();
                repositoryDataRef = new SoftReference<TaskData>(td);
                repositoryTaskDataLoaded(td);
            }
        } catch (CoreException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        return td;
    }

    protected final void deleteTask () {
        assert task.isLocal() : "Only local tasks can be deleted: " + task.getDelegate().getConnectorKind();
        synchronized (MODEL_LOCK) {
            if (list != null) {
                model.removeNbTaskDataModelListener(list);
                list = null;
            }
            model = null;
        }
        MylynSupport mylynSupp = MylynSupport.getInstance();
        mylynSupp.removeTaskDataListener(taskDataListener);
        task.removeNbTaskListener(taskListener);
        if (task.isLocal()) {
            task.delete();
            taskDeleted(task);
            fireDeleted();
        }
    }

    protected abstract void taskDeleted (NbTask task);

    public final boolean isMarkedNewUnread () {
        return task.isLocal() && Boolean.TRUE.toString().equals(task.getAttribute(ATTR_NEW_UNREAD));
    }

    public final boolean isNew () {
        return task.isUnsubmittedRepositoryTask();
    }

    protected final void markNewRead () {
        task.setAttribute(ATTR_NEW_UNREAD, null);
    }

    public final void setUpToDate (boolean seen, boolean markReadPending) {
        synchronized (MODEL_LOCK) {
            if (markReadPending) {
                // this is a workaround to keep incoming changes visible in editor
                NbTask.SynchronizationState syncState = task.getSynchronizationState();
                readPending |= syncState == NbTask.SynchronizationState.INCOMING
                        || syncState == NbTask.SynchronizationState.CONFLICT;
            } else {
                readPending = false;
            }
            task.markSeen(seen);
        }
    }

    protected final boolean editorOpened () {
        list = new NbTaskDataModel.NbTaskDataModelListener() {
            @Override
            public void attributeChanged (NbTaskDataModel.NbTaskDataModelEvent event) {
                NbTaskDataModel m = model;
                if (event.getModel() == m) {
                    AbstractNbTaskWrapper.this.attributeChanged(event, m);
                }
            }
        };
        if (task.getSynchronizationState() == NbTask.SynchronizationState.INCOMING_NEW) {
            // mark as seen so no fields are highlighted
            setUpToDate(true, false);
        }
        synchronized (MODEL_LOCK) {
            synchronized (getNbTask()) {
                // clear upon close
                if (readPending) {
                    // make sure remote changes are not lost and still highlighted in the editor
                    setUpToDate(false, false);
                    if (task.getDelegate() instanceof AbstractTask) {
                        ((AbstractTask) task.getDelegate()).setMarkReadPending(false);
                    }
                }
                model = task.getTaskDataModel();
                if (model == null) {
                    if (!synchronizeTask()) {
                        return false;
                    }
                    model = task.getTaskDataModel();
                }
                if(model == null) {
                    // issue #247877 
                    // something went wrong, not much we can do about it at this place. 
                    // lets avoid at least the NPE
                    LOG.log(Level.WARNING, "no model even after synchronizing {0}", task.getTaskId());
                    return false;
                }
                model.addNbTaskDataModelListener(list);
            }
            try {
                MylynSupport.getInstance().editorOpened(task.getDelegate());
            } catch (CoreException ex) {
                LOG.log(Level.WARNING, null, ex);
            }
        }
        return true;
    }

    protected final void editorClosed () {
        NbTaskDataModel m = model;
        boolean markedAsNewUnread = isMarkedNewUnread();
        if (m != null) {
            if (list != null) {
                m.removeNbTaskDataModelListener(list);
                list = null;
            }
            readPending = false;
            if (markedAsNewUnread) {
                // was not modified by user and not yet saved
                deleteTask();
            } else {
                if (m.isDirty()) {
                    try {
                        save();
                    } catch (CoreException ex) {
                        LOG.log(Level.WARNING, null, ex);
                    }
                }
                try {
                    MylynSupport.getInstance().editorClosing(task.getDelegate(), m.getRepositoryTaskData());
                } catch (CoreException ex) {
                    LOG.log(Level.WARNING, null, ex);
                }
                synchronized (MODEL_LOCK) {
                    if (model == m) {
                        model = null;
                    }
                }
            }
        }
    }

    protected final void runWithModelLoaded (Runnable runnable) {
        synchronized (MODEL_LOCK) {
            boolean closeModel = false;
            try {
                if (model == null) {
                    closeModel = true;
                    model = task.getTaskDataModel();
                }
                runnable.run();
            } finally {
                if (closeModel) {
                    if (model != null && model.isDirty()) {
                        try {
                            // let's not loose edits
                            model.save();
                        } catch (CoreException ex) {
                            LOG.log(Level.INFO, null, ex);
                        }
                    }
                    model = null;
                }
            }
        }
    }

    protected abstract void attributeChanged (NbTaskDataModel.NbTaskDataModelEvent event, NbTaskDataModel model);

    private void save () throws CoreException {
        NbTaskDataModel m = this.model;
        if(m == null) {
            // haven't been opened yet so there is no model?
            // might happen when e.g. closing a local task on commit (see issue #252222)
            m = task.getTaskDataModel();
        }
        markNewRead();
        boolean modified = persistPrivateTaskAttributes();
        if (m.isDirty()) {
            if (isNew()) {
                String summary = task.getSummary();
                String newSummary = getSummary(m.getLocalTaskData());
                if (newSummary != null && !(newSummary.isEmpty() || newSummary.equals(summary))) {
                    task.setSummary(newSummary);
                    taskModified(false);
                }
            }
            m.save();
            modelSaved(m);
        }
        if (modified) {
            taskModified(false);
        }
    }
    
    protected final void taskSubmitted (NbTask task) {
        if (task != null && task != this.task) {
            this.task.removeNbTaskListener(taskListener);
            this.task = task;
            task.addNbTaskListener(taskListener);
            synchronized (MODEL_LOCK) {
                if (list != null) {
                    model.removeNbTaskDataModelListener(list);
                }
                model = task.getTaskDataModel();
                repositoryDataRef.clear();
                if (list != null) {
                    model.addNbTaskDataModelListener(list);
                }
            }
        }
    }

    protected final boolean saveChanges () {
        try {
            save();
            return true;
        } catch (CoreException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        return false;
    }

    public final boolean cancelChanges () {
        try {
            if (saveChanges()) {
                task.discardLocalEdits();
                model.refresh();
                return true;
            }
        } catch (CoreException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        return false;
    }

    public final void clearUnsavedChanges () {
        clearPrivateTaskAttributeChanges();
        model.clearUnsavedChanges();
    }

    public final boolean hasLocalEdits () {
        NbTaskDataModel m = model;
        return !(m == null || m.getChangedAttributes().isEmpty());
    }
    
    public final boolean hasUnsavedChanges () {
        NbTaskDataModel m = model;
        return m != null && m.isDirty()
                || hasUnsavedPrivateTaskAttributes();
    }

    protected final boolean updateModel () {
        try {
            model.refresh();
            MylynSupport.getInstance().editorOpened(task.getDelegate());
            return true;
        } catch (CoreException ex) {
            LOG.log(Level.INFO, null, ex);
        }
        return false;
    }

    protected abstract void modelSaved (NbTaskDataModel model);

    protected abstract String getSummary (TaskData taskData);

    protected abstract void taskDataUpdated ();

    protected final boolean isSeen () {
        NbTask.SynchronizationState syncState = task.getSynchronizationState();
        return syncState == NbTask.SynchronizationState.OUTGOING
                || syncState == NbTask.SynchronizationState.OUTGOING_NEW
                || syncState == NbTask.SynchronizationState.SYNCHRONIZED;
    }

    protected abstract void taskModified (boolean syncStateChanged);

    protected final NbTaskDataModel getModel () {
        return model;
    }

    protected abstract void repositoryTaskDataLoaded (TaskData repositoryTaskData);

    protected final NbTask getNbTask () {
        return task;
    }

    public final long getCreated () {
        Date createdDate = getCreatedDate();
        if (createdDate != null) {
            return createdDate.getTime();
        } else {
            return -1;
        }
    }

    public final Date getCreatedDate () {
        return task.getCreationDate();
    }

    public final long getLastModify () {
        Date lastModifyDate = getLastModifyDate();
        if (lastModifyDate != null) {
            return lastModifyDate.getTime();
        } else {
            return -1;
        }
    }

    public final Date getLastModifyDate () {
        return task.getModificationDate();
    }

    public final String getSummary () {
        return task.getSummary();
    }

    public final String getID () {
        return getID(task);
    }

    public final boolean isFinished () {
        return task.isCompleted();
    }

    protected final NbTask.SynchronizationState getSynchronizationState () {
        return task.getSynchronizationState();
    }

    public final IssueStatusProvider.Status getStatus () {
        switch (getSynchronizationState()) {
            case CONFLICT:
                return IssueStatusProvider.Status.CONFLICT;
            case INCOMING:
                return IssueStatusProvider.Status.INCOMING_MODIFIED;
            case INCOMING_NEW:
                return IssueStatusProvider.Status.INCOMING_NEW;
            case OUTGOING:
                return IssueStatusProvider.Status.OUTGOING_MODIFIED;
            case OUTGOING_NEW:
                return IssueStatusProvider.Status.OUTGOING_NEW;
            case SYNCHRONIZED:
                return IssueStatusProvider.Status.SEEN;
        }
        return null;
    }

    /**
     * Called when the task does not yet contain all needed data. Needs to re-sync to get full taskdata.
     * @return false when the sync fails for some reason
     */
    protected abstract boolean synchronizeTask ();

    protected final boolean setNewAttachments (List<AttachmentsPanel.AttachmentInfo> newAttachments) {
        NbTaskDataModel m = getModel();
        TaskData td = m.getLocalTaskData();
        TaskAttribute ta = td.getRoot().getAttribute(AbstractNbTaskWrapper.NEW_ATTACHMENT_ATTRIBUTE_ID);
        Map<String, TaskAttribute> previousAttachments = Collections.<String, TaskAttribute>emptyMap();
        if (ta != null) {
            previousAttachments = ta.getAttributes();
            ta.clearAttributes();
        }
        boolean retval = false;
        if (newAttachments.isEmpty() && !previousAttachments.isEmpty()) {
            m.attributeChanged(ta);
            retval = true;
        } else if (!newAttachments.isEmpty()) {
            if (ta == null) {
                ta = td.getRoot().createAttribute(AbstractNbTaskWrapper.NEW_ATTACHMENT_ATTRIBUTE_ID);
            }
            boolean present = true;
            for (int i = 0; i < newAttachments.size(); ++i) {
                AttachmentsPanel.AttachmentInfo att = newAttachments.get(i);
                TaskAttribute attAttr = ta.createAttribute(NB_NEW_ATTACHMENT_ATTR_ID + i);
                if (att.getDescription() != null) {
                    attAttr.createAttribute(NB_NEW_ATTACHMENT_DESC_ATTR_ID).setValue(att.getDescription());
                }
                if (att.getContentType() != null) {
                    attAttr.createAttribute(NB_NEW_ATTACHMENT_CONTENT_TYPE_ATTR_ID).setValue(att.getContentType());
                }
                if (att.getFile() != null) {
                    attAttr.createAttribute(NB_NEW_ATTACHMENT_FILE_ATTR_ID).setValue(att.getFile().getAbsolutePath());
                }
                attAttr.createAttribute(NB_NEW_ATTACHMENT_PATCH_ATTR_ID).setValue(att.isPatch() ? "1" : "0");
                if (present) {
                    present = false;
                    for (TaskAttribute previousAttachment : previousAttachments.values()) {
                        if (!attachmentAttributesDiffer(previousAttachment, ta)) {
                            present = true;
                            break;
                        }
                    }
                }
            }
            if (!present || previousAttachments.size() != newAttachments.size()) {
                m.attributeChanged(ta);
                retval = true;
            }
        }
        return retval;
    }

    protected final List<AttachmentsPanel.AttachmentInfo> getNewAttachments () {
        NbTaskDataModel m = getModel();
        TaskData td = m == null ? null : m.getLocalTaskData();
        List<AttachmentsPanel.AttachmentInfo> attachments = Collections.<AttachmentsPanel.AttachmentInfo>emptyList();
        if (td != null) {
            TaskAttribute ta = td.getRoot().getAttribute(AbstractNbTaskWrapper.NEW_ATTACHMENT_ATTRIBUTE_ID);
            if (ta != null) {
                Map<String, TaskAttribute> attributes = ta.getAttributes();
                attachments = new ArrayList<AttachmentsPanel.AttachmentInfo>(attributes.size());
                for (TaskAttribute attAttr : attributes.values()) {
                    AttachmentsPanel.AttachmentInfo attInfo = new AttachmentsPanel.AttachmentInfo();
                    attInfo.setDescription("");
                    attInfo.setFile(null);
                    attInfo.setIsPatch(false);
                    for (Map.Entry<String, TaskAttribute> attSubAttr : attAttr.getAttributes().entrySet()) {
                        if (NB_NEW_ATTACHMENT_DESC_ATTR_ID.equals(attSubAttr.getKey())) {
                            attInfo.setDescription(attSubAttr.getValue().getValue());
                        } else if (NB_NEW_ATTACHMENT_CONTENT_TYPE_ATTR_ID.equals(attSubAttr.getKey())) {
                            attInfo.setContentType(attSubAttr.getValue().getValue());
                        } else if (NB_NEW_ATTACHMENT_FILE_ATTR_ID.equals(attSubAttr.getKey())) {
                            attInfo.setFile(new File(attSubAttr.getValue().getValue()));
                        } else if (NB_NEW_ATTACHMENT_PATCH_ATTR_ID.equals(attSubAttr.getKey())) {
                            attInfo.setIsPatch("1".equals(attSubAttr.getValue().getValue()));
                        }
                    }
                    attachments.add(attInfo);
                }
            }
        }
        return attachments;
    }

    protected final void setPrivateNotes (String text) {
        privateNotes = text;
    }

    public final String getPrivateNotes () {
        if (privateNotes == null) {
            return getNbTask().getPrivateNotes();
        } else {
            return privateNotes;
        }
    }

    public final Date getDueDate () {
        return dueDateModified ? dueDate : getNbTask().getDueDate();
    }

    protected final void setDueDate (Date date, boolean persistentChange) {
        dueDate = date;
        dueDateModified = true;
        if (persistentChange) {
            persistDueDate();
        }
    }

    public final NbDateRange getScheduleDate () {
        return scheduleDateModified ? scheduleDate : getNbTask().getScheduleDate();
    }

    protected final void setScheduleDate (IssueScheduleInfo info, boolean persistentChange) {
        scheduleDate = info == null ? null : new NbDateRange(info);
        scheduleDateModified = true;
        if (persistentChange) {
            persistScheduleDate();
        }
    }

    public final int getEstimate () {
        return estimate == null ? getNbTask().getEstimate() : estimate;
    }

    protected final void setEstimate (int estimate, boolean persistentChange) {
        this.estimate = estimate;
        if (persistentChange) {
            persistEstimate();
        }
    }

    /**
     * Returns only persistent value of due date, never the unsaved modification from the task editor
     * @return current task's persistent due date value
     */
    public final Date getPersistentDueDate () {
        return getNbTask().getDueDate();
    }

    /**
     * Returns only persistent value of estimate, never the unsaved modification from the task editor
     * @return current task's persistent estimate value
     */
    public final int getPersistentEstimate () {
        return getNbTask().getEstimate();
    }

    /**
     * Returns only persistent value of schedule date, never the unsaved modification from the task editor
     * @return current task's persistent scheduled date value
     */
    public final IssueScheduleInfo getPersistentScheduleInfo () {
        NbDateRange scheduled = getNbTask().getScheduleDate();
        return scheduled == null ? null : scheduled.toSchedulingInfo();
    }

    protected final boolean hasUnsavedPrivateTaskAttributes () {
        return privateNotes != null || dueDateModified || scheduleDateModified || estimate != null;
    }

    private boolean persistPrivateTaskAttributes () {
        boolean modified = false;
        if (privateNotes != null) {
            getNbTask().setPrivateNotes(privateNotes.isEmpty() ? null : privateNotes);
            privateNotes = null;
            modified = true;
        }
        boolean fireScheduleEvent = false;
        if (persistEstimate()) {
            modified = true;
            fireScheduleEvent = true;
        }
        if (persistDueDate()) {
            modified = true;
            fireScheduleEvent = true;
        }
        if (persistScheduleDate()) {
            modified = true;
            fireScheduleEvent = true;
        }
        if (fireScheduleEvent) {
            fireScheduleChanged();
        }
        return modified;
    }

    private boolean persistDueDate () {
        boolean modified = false;
        if (dueDateModified) {
            getNbTask().setDueDate(dueDate);
            dueDate = null;
            dueDateModified = false;
            modified = true;
        }
        return modified;
    }

    private boolean persistEstimate () {
        boolean modified = false;
        if (estimate != null) {
            getNbTask().setEstimate(estimate);
            estimate = null;
            modified = true;
        }
        return modified;
    }

    private boolean persistScheduleDate () {
        boolean modified = false;
        if (scheduleDateModified) {
            getNbTask().setScheduleDate(scheduleDate);
            scheduleDate = null;
            scheduleDateModified = false;
            modified = true;
        }
        return modified;
    }

    private void clearPrivateTaskAttributeChanges () {
        privateNotes = null;
        dueDate = null;
        dueDateModified = false;
        scheduleDate = null;
        scheduleDateModified = false;
        estimate = null;
    }

    protected final String getDueDisplayString () {
        Calendar cal = Calendar.getInstance();
        Date date = getPersistentDueDate();
        if (date == null) {
            return "";
        }
        cal.setTime(date);
        return formatDate(cal);
    }

    protected final String getEstimateDisplayString () {
        int est = getPersistentEstimate();
        if (est == 0) {
            return "";
        }
        return "" + est;
    }

    protected final String getScheduleDisplayString () {
        NbDateRange schedule = getNbTask().getScheduleDate();
        if (schedule == null) {
            return "";
        }
        int interval = schedule.getEndDate().get(Calendar.DAY_OF_YEAR) - schedule.getStartDate().get(Calendar.DAY_OF_YEAR);
        if (interval == 1) {
            return formatDate(schedule.getStartDate());
        }
        return formateDate(schedule.getStartDate(), schedule.getEndDate());
    }

    protected final void fireChanged () {
        support.firePropertyChange(IssueController.PROP_CHANGED, null, null);
    }

    protected final void fireDataChanged () {
        support.firePropertyChange(IssueProvider.EVENT_ISSUE_DATA_CHANGED, null, null);
    }

    protected final void fireScheduleChanged () {
        support.firePropertyChange(IssueScheduleProvider.EVENT_ISSUE_SCHEDULE_CHANGED, null, null);
    }

    protected final void fireStatusChanged () {
        support.firePropertyChange(IssueStatusProvider.EVENT_STATUS_CHANGED, null, null);
    }

    private void fireDeleted () {
        support.firePropertyChange(IssueProvider.EVENT_ISSUE_DELETED, null, null);
    }

    private String formatDate (Calendar date) {
        return DateFormat.getDateInstance(DateFormat.DEFAULT).format(date.getTime());
    }

    private String formateDate (Calendar start, Calendar end) {
        DateFormat format = DateFormat.getDateInstance(DateFormat.DEFAULT);
        return format.format(start.getTime()) + " - " + format.format(end.getTime()); //NOI18N
    }

    private class TaskDataListenerImpl implements TaskDataListener {

        @Override
        public void taskDataUpdated (TaskDataListener.TaskDataEvent event) {
            if (event.getTask() == task) {
                if (event.getTaskData() != null && !event.getTaskData().isPartial()) {
                    repositoryDataRef = new SoftReference<TaskData>(event.getTaskData());
                }
                if (event.getTaskDataUpdated()) {
                    NbTaskDataModel m = model;
                    if (m != null) {
                        try {
                            m.refresh();
                        } catch (CoreException ex) {
                            LOG.log(Level.INFO, null, ex);
                        }
                    }
                    AbstractNbTaskWrapper.this.taskDataUpdated();
                }
            }
        }
    }

    private class TaskListenerImpl implements NbTaskListener {

        @Override
        public void taskModified (NbTaskListener.TaskEvent event) {
            if (event.getTask() == task && event.getKind() == NbTaskListener.TaskEvent.Kind.MODIFIED) {
                boolean syncStateChanged = event.taskStateChanged();
                AbstractNbTaskWrapper.this.taskModified(syncStateChanged);
            }
        }

    }
}
