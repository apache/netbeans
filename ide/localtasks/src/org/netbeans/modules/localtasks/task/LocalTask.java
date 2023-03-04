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
package org.netbeans.modules.localtasks.task;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.spi.IssueController;
import org.netbeans.modules.localtasks.LocalRepository;
import org.netbeans.modules.localtasks.util.FileUtils;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.netbeans.modules.bugtracking.commons.AttachmentsPanel;
import org.netbeans.modules.bugtracking.commons.AttachmentsPanel.AttachmentInfo;
import org.netbeans.modules.mylyn.util.NbTask;
import org.netbeans.modules.mylyn.util.NbTaskDataModel;
import org.netbeans.modules.mylyn.util.localtasks.AbstractLocalTask;
import org.netbeans.modules.mylyn.util.localtasks.IssueField;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

/**
 *
 * @author Ondrej Vrabec
 */
public final class LocalTask extends AbstractLocalTask {

    private final NbTask task;
    private final LocalRepository repository;
    private TaskController controller;
    private static final RequestProcessor RP = LocalRepository.getInstance().getRequestProcessor();
    private static final String NB_ATTACHMENT = "nb.attachment."; //NOI18N
    private static final String NB_TASK_REFERENCE = "nb.taskreference."; //NOI18N
    private static final Object ATTACHMENT_STORAGE_LOCK = new Object();

    private List<AttachmentInfo> unsavedAttachments;
    private String tooltip = "";

    public LocalTask (NbTask task) {
        super(task);
        this.task = task;
        this.repository = LocalRepository.getInstance();
        updateTooltip();
    }

    @Override
    protected void taskDeleted (NbTask task) {
        repository.taskDeleted(getID());
    }

    @Override
    protected void attributeChanged (NbTaskDataModel.NbTaskDataModelEvent event, NbTaskDataModel model) {
        modelStateChanged(model.isDirty() || hasUnsavedAttributes());
    }

    @Override
    protected void modelSaved (NbTaskDataModel model) {
        modelStateChanged(model.isDirty() || hasUnsavedAttributes());
    }

    @Override
    protected String getSummary (TaskData taskData) {
        return getFieldValue(taskData, IssueField.SUMMARY);
    }

    @Override
    protected void taskDataUpdated () {
        RP.post(new Runnable() {
            @Override
            public void run () {
                dataChanged();
            }
        });
    }

    private void dataChanged () {
        updateTooltip();
        fireDataChanged();
        if (controller != null) {
            controller.refreshViewData();
        }
    }
    
    @NbBundle.Messages({
        "LBL_Task.tooltip.statusLabel=Status",
        "LBL_Task.tooltip.status.open=Open",
        "LBL_Task.tooltip.status.completed=Completed",
        "CTL_Issue_Scheduled_Title=Scheduled",
        "CTL_Issue_Due_Title=Due",
        "CTL_Issue_Estimate_Title=Estimate"
    })
    private boolean updateTooltip () {
        String displayName = getDisplayName();
        if (displayName.startsWith("#")) { //NOI18N
            displayName = displayName.replaceFirst("#", ""); //NOI18N
        }
        String oldTooltip = tooltip;

        String status = isFinished() ? Bundle.LBL_Task_tooltip_status_completed() : Bundle.LBL_Task_tooltip_status_open();

        String scheduledLabel = Bundle.CTL_Issue_Scheduled_Title();
        String scheduled = getScheduleDisplayString();

        String dueLabel = Bundle.CTL_Issue_Due_Title();
        String due = getDueDisplayString();
        

        String estimateLabel = Bundle.CTL_Issue_Estimate_Title();
        String estimate = getEstimateDisplayString();

        String fieldTable = "<table>" //NOI18N
            + "<tr><td><b>" + Bundle.LBL_Task_tooltip_statusLabel() + ":</b></td><td>" + status + "</td></tr>"; //NOI18N

        if (!scheduled.isEmpty()) {
            fieldTable += "<tr><td><b>" + scheduledLabel + ":</b></td><td>" + scheduled + "</td></tr>"; //NOI18N
        }
        if (!due.isEmpty()) {
            fieldTable += "<tr><td><b>" + dueLabel + ":</b></td><td>" + due + "</td></tr>"; //NOI18N
        }
        if (!estimate.isEmpty()) {
            fieldTable += "<tr><td><b>" + estimateLabel + ":</b></td><td>" + estimate + "</td></tr>"; //NOI18N
        }
        fieldTable += "</table>"; //NOI18N
        
        StringBuilder sb = new StringBuilder("<html>"); //NOI18N
        sb.append("<b>").append(displayName).append("</b>"); //NOI18N
        sb.append("<hr>"); //NOI18N
        sb.append(fieldTable);
        sb.append("</html>"); //NOI18N
        tooltip = sb.toString();
        return !oldTooltip.equals(tooltip);
    }

    @Override
    protected void taskModified (boolean syncStateChanged) {
        updateTooltip();
        fireDataChanged();
    }

    @Override
    protected void repositoryTaskDataLoaded (TaskData repositoryTaskData) {
        // NO OP
    }

    @Override
    public boolean synchronizeTask () {
        return true;
    }

    @NbBundle.Messages({
        "# {0} - task id", "# {1} - task summary", "LBL_LocalTask.displayName=#{0} - {1}"
    })
    public String getDisplayName () {
        return Bundle.LBL_LocalTask_displayName(getID(task), task.getSummary());
    }

    public String getTooltip () {
        return tooltip;
    }

    public boolean searchFor (String[] keywords) {
        String summary = getSummary().toLowerCase();
        for (String kw : keywords) {
            kw = kw.toLowerCase();
            if (summary.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    public IssueController getController () {
        return getTaskController();
    }
    
    TaskController getTaskController () {
        if (controller == null) {
            controller = new TaskController(this);
        }
        return controller;
    }

    void opened () {
        LocalRepository.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run () {
                if (editorOpened()) {
                    getTaskController().refreshViewData();
                } else {
                    // should close somehow
                }
            }
        });
    }

    void closed () {
        LocalRepository.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run () {
                editorClosed();
            }
        });
    }

    public void delete () {
        runWithModelLoaded(new Runnable() {
            @Override
            public void run () {
                for (Attachment att : getAttachments()) {
                    if (att.isInCentral()) {
                        String uri = att.getUri();
                        try {
                            File f = Utilities.toFile(new URI(uri));
                            f.delete();
                        } catch (URISyntaxException ex) {
                            // not interested
                        }
                    }
                }
                clearUnsavedChanges();
            }
        });
        fireChanged();
        if (controller != null) {
            controller.taskDeleted();
        }
        deleteTask();
    }

    void markUserChange () {
        markNewRead();
    }

    String getFieldValue (IssueField field) {
        NbTaskDataModel model = getModel();
        return getFieldValue(model == null ? null : model.getLocalTaskData(), field);
    }

    List<String> getFieldValues (IssueField field) {
        NbTaskDataModel model = getModel();
        return getFieldValues(model == null ? null : model.getLocalTaskData(), field);
    }

    void setFieldValue (IssueField field, String value) {
        NbTaskDataModel m = getModel();
        // should not happen, setFieldValue either runs with model lock
        // or it is called from issue editor in AWT - the editor could not be closed by user in the meantime
        assert m != null;
        TaskData taskData = m.getLocalTaskData();
        TaskAttribute a = taskData.getRoot().getMappedAttribute(field.getKey());
        assert a != null : field.getKey();
        if (!value.equals(a.getValue())) {
            setValue(m, a, value);
        }
    }

    private static String getFieldValue (TaskData taskData, IssueField field) {
        if (taskData == null) {
            return "";
        }
        TaskAttribute a = taskData.getRoot().getAttribute(field.getKey());
        if (a == null) {
            return ""; //NOI18N
        } else if (a.getValues().size() > 1) {
            return listValues(a);
        } else {
            return a.getValue();
        }
    }

    private static List<String> getFieldValues (TaskData taskData, IssueField field) {
        if (taskData == null) {
            return Collections.<String>emptyList();
        }
        TaskAttribute a = taskData.getRoot().getAttribute(field.getKey());
        if (a == null) {
            return Collections.<String>emptyList();
        } else {
            return a.getValues();
        }
    }

    private static String listValues (TaskAttribute a) {
        if (a == null) {
            return ""; //NOI18N
        }
        StringBuilder sb = new StringBuilder();
        for (String s : a.getValues()) {
            sb.append(s);
            sb.append(","); //NOI18N
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
        return sb.toString();
    }

    private void setValue (NbTaskDataModel model, TaskAttribute ta, String value) {
        ta.setValue(value);
        model.attributeChanged(ta);
    }
 
    private boolean hasUnsavedAttributes () {
        return hasUnsavedAttachments() || hasUnsavedPrivateTaskAttributes();
    }
 
    boolean hasUnsavedAttachments () {
        return unsavedAttachments != null;
    }
    
    public boolean save () {
        NbTaskDataModel model = getModel();
        TaskData taskData = model == null ? null : model.getLocalTaskData();
        if (taskData != null) {
            getNbTask().setSummary(taskData.getRoot().getAttribute(IssueField.SUMMARY.getKey()).getValue());
            persistAttachments(model, taskData);
            fireDataChanged();
        }
        return saveChanges();
    }

    void clearModifications () {
        unsavedAttachments = null;
        clearUnsavedChanges();
    }

    boolean hasSubtasks () {
        return !getTaskReferences().isEmpty();
    }

    List<Attachment> getAttachments () {
        NbTaskDataModel model = getModel();
        TaskData taskData = model == null ? null : model.getLocalTaskData();
        List<Attachment> attachments = new ArrayList<>();
        TaskAttribute parentTA = taskData == null ? null : taskData.getRoot().getAttribute(IssueField.ATTACHMENTS.getKey());
        if (parentTA != null) {
            for (TaskAttribute ta : parentTA.getAttributes().values()) {
                if (ta.getId().startsWith(NB_ATTACHMENT)) {
                    attachments.add(new Attachment(ta));
                }
            }
        }
        return attachments;
    }

    List<AttachmentInfo> getUnsubmittedAttachments () {
        return unsavedAttachments == null
                ? Collections.<AttachmentInfo>emptyList()
                : Collections.unmodifiableList(unsavedAttachments);
    }

    void setUnsubmittedAttachments (List<AttachmentInfo> attachments) {
        unsavedAttachments = new ArrayList<>(attachments);
        modelStateChanged(true);
    }

    private void persistAttachments (NbTaskDataModel model, TaskData td) {
        if (unsavedAttachments != null) {
            TaskAttribute parentTA = td.getRoot().getAttribute(IssueField.ATTACHMENTS.getKey());
            if (parentTA == null) {
                parentTA = td.getRoot().createAttribute(IssueField.ATTACHMENTS.getKey());
            }
            if (!unsavedAttachments.isEmpty()) {
                boolean copyToCentral = askCopyToCentralStorage(unsavedAttachments.size());
                for (AttachmentInfo att : unsavedAttachments) {
                    File file = att.getFile();
                    if (file != null) {
                        String desc = att.getDescription();
                        String contentType = att.getContentType();
                        boolean isPatch = att.isPatch();
                        addAttachment(model, parentTA, file, desc, contentType, isPatch, copyToCentral);
                    }
                }
                unsavedAttachments.clear();
            }
        }
    }

    private void addAttachment (NbTaskDataModel model, TaskAttribute parentTA,
            File file, String desc, String contentType, boolean isPatch,
            boolean copyToCentralStorage) {
        if (desc == null) {
            desc = "";
        }
        if (contentType == null) {
            file = FileUtil.normalizeFile(file);
            String ct = FileUtil.getMIMEType(FileUtil.toFileObject(file));
            if ((ct != null) && (!"content/unknown".equals(ct))) { // NOI18N
                contentType = ct;
            } else {
                contentType = FileTaskAttachmentSource.getContentTypeFromFilename(file.getName());
            }
        }
        int attachmentIndex = parentTA.getAttributes().size();
        TaskAttribute attachment = parentTA.createAttribute(NB_ATTACHMENT + attachmentIndex);
        TaskAttachmentMapper mapper = new TaskAttachmentMapper();
        mapper.setAttachmentId(String.valueOf(attachmentIndex));
        mapper.setDescription(desc);
        mapper.setFileName(file.getName());
        mapper.setPatch(isPatch);
        mapper.setCreationDate(new Date());
        mapper.setContentType(contentType);
        File realFile = copyToCentralStorage ? copyFileToCentral(file) : file;
        mapper.setUrl(Utilities.toURI(realFile).toString());
        // abuse this attribute and mark the patch residing in the central storage
        mapper.setReplaceExisting(copyToCentralStorage);
        mapper.applyTo(attachment);
        model.attributeChanged(parentTA);
    }
    
    private void deleteAttachment (TaskAttribute ta) {
        NbTaskDataModel model = getModel();
        TaskData taskData = model == null ? null : model.getLocalTaskData();
        TaskAttribute parentTA = taskData == null ? null : taskData.getRoot().getAttribute(IssueField.ATTACHMENTS.getKey());
        if (parentTA != null) {
            if (parentTA.getAttribute(ta.getId()) != null) {
                parentTA.removeAttribute(ta.getId());
                model.attributeChanged(parentTA);
                getTaskController().attachmentDeleted();
            }
        }
    }

    void addTaskReference (Issue task) {
        NbTaskDataModel model = getModel();
        TaskData taskData = model == null ? null : model.getLocalTaskData();
        if (taskData != null) {
            TaskAttribute parentTA = taskData.getRoot().getAttribute(IssueField.REFERENCES.getKey());
            if (parentTA == null) {
                parentTA = taskData.getRoot().createAttribute(IssueField.REFERENCES.getKey());
            }
            int index = parentTA.getAttributes().size();
            TaskAttribute attr = parentTA.createAttribute(NB_TASK_REFERENCE + index);
            TaskReference taskRef = new TaskReference(task);
            taskRef.applyTo(attr);
            model.attributeChanged(parentTA);
        }
    }

    List<TaskReference> getTaskReferences () {
        NbTaskDataModel model = getModel();
        TaskData taskData = model == null ? null : model.getLocalTaskData();
        List<TaskReference> references = new ArrayList<>();
        TaskAttribute parentTA = taskData == null ? null : taskData.getRoot().getAttribute(IssueField.REFERENCES.getKey());
        if (parentTA != null) {
            for (TaskAttribute ta : parentTA.getAttributes().values()) {
                if (ta.getId().startsWith(NB_TASK_REFERENCE)) {
                    references.add(TaskReference.createFrom(ta));
                }
            }
        }
        return references;
    }

    void removeTaskReference (String repositoryId, String taskId) {
        NbTaskDataModel model = getModel();
        TaskData taskData = model == null ? null : model.getLocalTaskData();
        if (taskData != null) {
            TaskAttribute parentTA = taskData.getRoot().getAttribute(IssueField.REFERENCES.getKey());
            if (parentTA != null) {
                for (TaskAttribute ta : parentTA.getAttributes().values()) {
                    if (ta.getId().startsWith(NB_TASK_REFERENCE)) {
                        TaskReference ref = TaskReference.createFrom(ta);
                        if (repositoryId.equals(ref.getRepositoryId()) && taskId.equals(ref.getTaskId())) {
                            parentTA.removeAttribute(ta.getId());
                            break;
                        }
                    }
                }
            }
            model.attributeChanged(parentTA);
        }
    }
    
    void setTaskPrivateNotes (String notes) {
        super.setPrivateNotes(notes);
        modelStateChanged(true);
    }
    
    public void setTaskDueDate (Date date, boolean persistChange) {
        super.setDueDate(date, persistChange);
        modelStateChanged(hasUnsavedChanges());
        if (persistChange) {
            dataChanged();
        }
    }
    
    public void setTaskScheduleDate (IssueScheduleInfo date, boolean persistChange) {
        super.setScheduleDate(date, persistChange);
        modelStateChanged(hasUnsavedChanges());
        if (persistChange) {
            dataChanged();
        }
    }
    
    public void setTaskEstimate (int estimate, boolean persistChange) {
        super.setEstimate(estimate, persistChange);
        modelStateChanged(hasUnsavedChanges());
        if (persistChange) {
            dataChanged();
        }
    }

    public void addComment (String comment, boolean closeAsFixed) {
        if (comment != null && !comment.isEmpty()) {
            String notes = getPrivateNotes();
            notes += "\n\n" + comment;
            setPrivateNotes(notes);
        }
        if (closeAsFixed) {
            finish();
        }
        save();
        modelStateChanged(false);
        if (controller != null) {
            controller.refreshViewData();
        }
    }

    public void attachPatch (final File file, final String description) {
        if (file != null) {
            runWithModelLoaded(new Runnable() {

                @Override
                public void run () {
                    NbTaskDataModel model = getModel();
                    TaskData td = model == null ? null : model.getLocalTaskData();
                    if (td != null) {
                        TaskAttribute parentTA = td.getRoot().getAttribute(IssueField.ATTACHMENTS.getKey());
                        if (parentTA == null) {
                            parentTA = td.getRoot().createAttribute(IssueField.ATTACHMENTS.getKey());
                        }
                        addAttachment(model, parentTA, file, description, null, true, true);
                        save();
                        modelStateChanged(false);
                        if (controller != null) {
                            controller.refreshViewData();
                        }
                    }
                }
            });
        }
    }

    void fireChangeEvent () {
        fireChanged();
    }

    private void modelStateChanged (boolean dirty) {
        if (controller != null) {
            controller.modelStateChanged(dirty);
        }
    }

    @NbBundle.Messages({
        "LBL_LocalTask.copyAttToCentralStorage.title=Copy Attachment",
        "# {0} - number of attachments",
        "MSG_LocalTask.copyAttToCentralStorage.text=You are trying to add {0} attachments to the local task.\n"
                + "The attachments will be kept in their original locations and linked from the task\n\n"
                + "Do you want to copy the files to a central storage to make sure they will be accessible "
                + "even after their original location is deleted?"
    })
    private boolean askCopyToCentralStorage (int attachmentCount) {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                Bundle.MSG_LocalTask_copyAttToCentralStorage_text(attachmentCount),
                Bundle.LBL_LocalTask_copyAttToCentralStorage_title(),
                NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
        return DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION;
    }

    private File copyFileToCentral (File file) {
        File destFolder = new File(Places.getUserDirectory().getAbsolutePath()
                + ("/var/tasks/mylyn/tasks/" + repository.getTaskRepository().getConnectorKind() //NOI18N
                + "-" + repository.getTaskRepository().getUrl()+ "/offline/attachments").replace("/", File.separator)); //NOI18N
        destFolder.mkdirs();
        synchronized (ATTACHMENT_STORAGE_LOCK) {
            File destFile = new File(destFolder, file.getName());
            if (destFile.exists()) {
                int i = 1;
                while (destFile.exists()) {
                    destFile = new File(destFolder, i + "_" + file.getName());
                    i = i + 1;
                }
            }
            try {
                FileUtils.copyFile(file, destFile);
            } catch (IOException ex) {
                Logger.getLogger(LocalTask.class.getName()).log(Level.INFO, "Cannot copy " + file //NOI18N
                        + " to " + destFile + ", will stick with the original", ex); //NOI18N
                destFile = file;
            }
            return destFile;
        }
    }
    
    static final class TaskReference {

        private final String repositoryId;
        private final String taskId;

        public TaskReference (String repositoryId, String taskId) {
            this.repositoryId = repositoryId;
            this.taskId = taskId;
        }

        private TaskReference (Issue task) {
            this.repositoryId = task.getRepository().getId();
            this.taskId = task.getID();
        }

        public String getTaskId () {
            return taskId;
        }

        public String getRepositoryId () {
            return repositoryId;
        }

        private void applyTo (TaskAttribute attr) {
            attr.createAttribute("task.repository").setValue(repositoryId);
            attr.createAttribute("task.id").setValue(taskId);
        }
        
        private static TaskReference createFrom (TaskAttribute ta) {
            String repositoryId = "";
            String taskId = "";
            TaskAttribute attr = ta.getAttribute("task.repository");
            if (attr != null) {
                repositoryId = attr.getValue();
            }
            attr = ta.getAttribute("task.id");
            if (attr != null) {
                taskId = attr.getValue();
            }
            return new TaskReference(repositoryId, taskId);
        }
    }

    class Attachment extends AttachmentsPanel.AbstractAttachment {

        private final TaskAttribute ta;
        private final String desc;
        private final String filename;
        private final Date date;
        private final String contentType;
        private final boolean isPatch;
        private final String uri;
        private Action deleteAction;
        private final boolean residesInCentral;

        public Attachment (TaskAttribute ta) {
            TaskAttachmentMapper taskAttachment = TaskAttachmentMapper.createFrom(ta);
            this.ta = ta;
            this.desc = taskAttachment.getDescription();
            this.filename = taskAttachment.getFileName();
            this.date = taskAttachment.getCreationDate();
            this.contentType = taskAttachment.getContentType();
            this.isPatch = taskAttachment.isPatch();
            this.uri = taskAttachment.getUrl();
            Boolean inCentral = taskAttachment.getReplaceExisting();
            this.residesInCentral = Boolean.TRUE.equals(inCentral);
        }

        @Override
        public String getAuthorName () {
            return "";
        }

        @Override
        public String getAuthor () {
            return "";
        }

        @Override
        public Date getDate () {
            return date;
        }

        @Override
        public String getDesc () {
            return desc;
        }

        @Override
        public String getFilename () {
            return filename;
        }

        @Override
        public String getContentType () {
            return contentType;
        }

        @Override
        public boolean isPatch () {
            return isPatch;
        }

        @Override
        public void getAttachementData (final OutputStream os) {
            try {
                File f = Utilities.toFile(new URI(uri));
                FileUtils.copyStreamsCloseAll(os, FileUtils.createInputStream(f));
            } catch (URISyntaxException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public boolean canBeDeleted () {
            return true;
        }

        @Override
        public Action getDeleteAction () {
            if (deleteAction == null) {
                deleteAction = new DeleteAttachmentAction();
            }
            return deleteAction;
        }

        public String getUri () {
            return uri;
        }
        
        public boolean isInCentral () {
            return residesInCentral;
        }

        @NbBundle.Messages({
            "CTL_AttachmentAction.Remove=Remove",
            "MSG_AttachmentAction.Remove.confirm.text=Do you want to permanently remove the attachment?",
            "LBL_AttachmentAction.Remove.confirm.title=Remove Attachment"
        })
        private class DeleteAttachmentAction extends AbstractAction {

            public DeleteAttachmentAction() {
                putValue(NAME, Bundle.CTL_AttachmentAction_Remove());
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                        Bundle.MSG_AttachmentAction_Remove_confirm_text(),
                        Bundle.LBL_AttachmentAction_Remove_confirm_title(),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                    deleteAttachment(ta);
                }
            }
        }

    }

}
