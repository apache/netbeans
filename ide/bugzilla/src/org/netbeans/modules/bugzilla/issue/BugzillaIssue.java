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

package org.netbeans.modules.bugzilla.issue;

import java.awt.EventQueue;
import java.io.File;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.swing.JTable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaAttribute;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaOperation;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaTaskDataHandler;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaVersion;
import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskOperation;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugtracking.issuetable.IssueNode;
import org.netbeans.modules.bugtracking.spi.IssueController;
import org.netbeans.modules.bugtracking.issuetable.ColumnDescriptor;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.netbeans.modules.team.spi.OwnerInfo;
import org.netbeans.modules.bugtracking.commons.AttachmentsPanel;
import org.netbeans.modules.bugtracking.commons.NBBugzillaUtils;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.bugzilla.BugzillaConfig;
import org.netbeans.modules.bugzilla.commands.AddAttachmentCommand;
import org.netbeans.modules.bugzilla.repository.BugzillaConfiguration;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.commands.GetAttachmentCommand;
import org.netbeans.modules.bugzilla.repository.IssueField;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.netbeans.modules.bugzilla.util.NbBugzillaConstants;
import org.netbeans.modules.mylyn.util.AbstractNbTaskWrapper;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.netbeans.modules.mylyn.util.NbTask;
import org.netbeans.modules.mylyn.util.NbTask.SynchronizationState;
import org.netbeans.modules.mylyn.util.NbTaskDataModel;
import org.netbeans.modules.mylyn.util.NbTaskDataModel.NbTaskDataModelEvent;
import org.netbeans.modules.mylyn.util.NbTaskDataState;
import org.netbeans.modules.mylyn.util.commands.SubmitTaskCommand;
import org.netbeans.modules.mylyn.util.commands.SynchronizeTasksCommand;
import org.openide.awt.StatusDisplayer;
import org.openide.modules.Places;
import org.openide.util.NbBundle;
import static org.netbeans.modules.bugzilla.issue.Bundle.*;

/**
 *
 * @author Tomas Stupka
 * @author Jan Stola
 */
public class BugzillaIssue extends AbstractNbTaskWrapper {

    public static final String RESOLVE_FIXED = "FIXED";                                                         // NOI18N
    public static final String RESOLVE_DUPLICATE = "DUPLICATE";                                                 // NOI18N
    public static final String VCSHOOK_BUGZILLA_FIELD = "netbeans.vcshook.bugzilla.";                           // NOI18N
    private static final SimpleDateFormat CC_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");            // NOI18N
    static final SimpleDateFormat DUE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");                 // NOI18N

    private final BugzillaRepository repository;

    private BugzillaIssueController controller;
    private WeakReference<BugzillaIssueNode> nodeRef;
    private OwnerInfo info;

    static final String LABEL_NAME_ID           = "bugzilla.issue.id";          // NOI18N
    static final String LABEL_NAME_SEVERITY     = "bugzilla.issue.severity";    // NOI18N
    static final String LABEL_NAME_ISSUE_TYPE   = "bugzilla.issue.issue_type";  // NOI18N
    static final String LABEL_NAME_PRIORITY     = "bugzilla.issue.priority";    // NOI18N
    static final String LABEL_NAME_STATUS       = "bugzilla.issue.status";      // NOI18N
    static final String LABEL_NAME_RESOLUTION   = "bugzilla.issue.resolution";  // NOI18N
    static final String LABEL_NAME_ASSIGNED_TO  = "bugzilla.issue.assigned";    // NOI18N
    static final String LABEL_NAME_PRODUCT      = "bugzilla.issue.product";     // NOI18N
    static final String LABEL_NAME_COMPONENT    = "bugzilla.issue.component";   // NOI18N
    static final String LABEL_NAME_VERSION      = "bugzilla.issue.version";     // NOI18N
    static final String LABEL_NAME_OS           = "bugzilla.issue.os";          // NOI18N
    static final String LABEL_NAME_PLATFORM     = "bugzilla.issue.platform";    // NOI18N
    static final String LABEL_NAME_MILESTONE    = "bugzilla.issue.milestone";   // NOI18N
    static final String LABEL_NAME_REPORTER     = "bugzilla.issue.reporter";    // NOI18N
    static final String LABEL_NAME_MODIFICATION = "bugzilla.issue.modified";    // NOI18N
    static final String LABEL_NAME_QA_CONTACT   = "bugzilla.issue.qa_contact";  // NOI18N
    static final String LABEL_NAME_KEYWORDS     = "bugzilla.issue.keywords";    // NOI18N
    static final String LABEL_NAME_WHITEBOARD   = "bugzilla.issue.whiteboard";  // NOI18N

    /**
     * IssueProvider wasn't seen yet
     */
    static final int FIELD_STATUS_IRELEVANT = -1;

    /**
     * Field wasn't changed since the issue was seen the last time
     */
    static final int FIELD_STATUS_UPTODATE = 1;

    /**
     * Field was changed since the issue was seen the last time
     */
    static final int FIELD_STATUS_MODIFIED = 2;

    /**
     * Field was changed since the issue was seen the last time
     */
    static final int FIELD_STATUS_OUTGOING = 4;

    /**
     * Field was changed both locally and in repository
     */
    static final int FIELD_STATUS_CONFLICT = FIELD_STATUS_MODIFIED | FIELD_STATUS_OUTGOING;
    private String initialProduct = null;

    private Map<String, TaskOperation> availableOperations;

    private String recentChanges = "";
    private String tooltip = "";

    private static final URL ICON_REMOTE_PATH = IssuePanel.class.getClassLoader().getResource("org/netbeans/modules/bugzilla/resources/remote.png"); //NOI18N
    private static final URL ICON_CONFLICT_PATH = IssuePanel.class.getClassLoader().getResource("org/netbeans/modules/bugzilla/resources/conflict.png"); //NOI18N
    private static final URL ICON_UNSUBMITTED_PATH = IssuePanel.class.getClassLoader().getResource("org/netbeans/modules/bugzilla/resources/unsubmitted.png"); //NOI18N
    private boolean loading;
    private boolean wasDuplicated;

    public BugzillaIssue (NbTask task, BugzillaRepository repo) {
        super(task);
        this.repository = repo;
        updateRecentChanges();
        updateTooltip();
    }

    @Override
    protected void taskDeleted (NbTask task) {
        getRepository().taskDeleted(getID(task));
    }

    void markUserChange () {
        if (isMarkedNewUnread()) {
            markNewRead();
        }
    }

    void delete () {
        deleteTask();
    }

    public void opened() {
        if(Bugzilla.LOG.isLoggable(Level.FINE)) {
            Bugzilla.LOG.log(Level.FINE, "issue {0} open start", new Object[] {getID()});
        }
        loading = true;
        Bugzilla.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run () {
                if (editorOpened()) {
                    ensureConfigurationUptodate();
                    loading = false;
                    refreshViewData(true);
                } else {
                    // should close somehow
                }
            }
        });
        String refresh = System.getProperty("org.netbeans.modules.bugzilla.noIssueRefresh"); // NOI18N
        if(refresh != null && refresh.equals("true")) {                                      // NOI18N
            return;
        }
        if(Bugzilla.LOG.isLoggable(Level.FINE)) {
            Bugzilla.LOG.log(Level.FINE, "issue {0} open finish", new Object[] {getID()});
        }
    }

    public void closed () {
        if(Bugzilla.LOG.isLoggable(Level.FINE)) {
            Bugzilla.LOG.log(Level.FINE, "issue {0} close start", new Object[] {getID()});
        }
        Bugzilla.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run () {
                editorClosed();
            }
        });
        if(Bugzilla.LOG.isLoggable(Level.FINE)) {
            Bugzilla.LOG.log(Level.FINE, "issue {0} close finish", new Object[] {getID()});
        }
    }

    public String getDisplayName() {
        return getDisplayName(getNbTask());
    }

    public static String getDisplayName (NbTask task) {
        return task.getSynchronizationState() == SynchronizationState.OUTGOING_NEW ?
                task.getSummary() :
                NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue", new Object[] {getID(task), task.getSummary()}); //NOI18N
    }
    
    // XXX not the same as in Issue.getShortenedDisplayName()

//    public String getShortenedDisplayName() {
//        if (data.isNew()) {
//            return getDisplayName();
//        }
//
//        String shortSummary = TextUtils.shortenText(getSummary(),
//                                                    2,    //try at least 2 words
//                                                    SHORTENED_SUMMARY_LENGTH);
//        return NbBundle.getMessage(BugzillaIssue.class,
//                                   "CTL_Issue",                         //NOI18N
//                                   new Object[] {getID(), shortSummary});
//    }

    public String getTooltip() {
        return tooltip;
    }

    public static ColumnDescriptor[] getColumnDescriptors(BugzillaRepository repository) {
        ResourceBundle loc = NbBundle.getBundle(BugzillaIssue.class);
        JTable t = new JTable();
        List<ColumnDescriptor<String>> ret = new LinkedList<>();
        
            ret.add(new ColumnDescriptor<>(LABEL_NAME_ID, String.class,
                                              loc.getString("CTL_Issue_ID_Title"),                // NOI18N
                                              loc.getString("CTL_Issue_ID_Desc"),                 // NOI18N
                                              UIUtils.getColumnWidthInPixels(6, t)));
            ret.add(new ColumnDescriptor<>(IssueNode.LABEL_NAME_SUMMARY, String.class,
                                              loc.getString("CTL_Issue_Summary_Title"),           // NOI18N
                                              loc.getString("CTL_Issue_Summary_Desc")));          // NOI18N
            ret.add(NBBugzillaUtils.isNbRepository(repository.getUrl())
                                        ?
                                              new ColumnDescriptor<>(LABEL_NAME_ISSUE_TYPE, String.class,
                                              loc.getString("CTL_Issue_Issue_Type_Title"),        // NOI18N
                                              loc.getString("CTL_Issue_Issue_Type_Desc"),         // NOI18N
                                              0)
                                        :
                                              new ColumnDescriptor<>(LABEL_NAME_SEVERITY, String.class,
                                              loc.getString("CTL_Issue_Severity_Title"),          // NOI18N
                                              loc.getString("CTL_Issue_Severity_Desc"),           // NOI18N
                                              0));
            ret.add(new ColumnDescriptor<>(LABEL_NAME_PRIORITY, String.class,
                                              loc.getString("CTL_Issue_Priority_Title"),          // NOI18N
                                              loc.getString("CTL_Issue_Priority_Desc"),           // NOI18N
                                              0));
            ret.add(new ColumnDescriptor<>(LABEL_NAME_STATUS, String.class,
                                              loc.getString("CTL_Issue_Status_Title"),            // NOI18N
                                              loc.getString("CTL_Issue_Status_Desc"),             // NOI18N
                                              0));
            ret.add(new ColumnDescriptor<>(LABEL_NAME_RESOLUTION, String.class,
                                              loc.getString("CTL_Issue_Resolution_Title"),        // NOI18N
                                              loc.getString("CTL_Issue_Resolution_Desc"),         // NOI18N
                                              0));
            ret.add(new ColumnDescriptor<>(LABEL_NAME_ASSIGNED_TO, String.class,
                                              loc.getString("CTL_Issue_Assigned_Title"),          // NOI18N
                                              loc.getString("CTL_Issue_Assigned_Desc"),           // NOI18N
                                              0));
            ret.add(new ColumnDescriptor<>(LABEL_NAME_PRODUCT, String.class,
                                              loc.getString("CTL_Issue_Product_Title"),          // NOI18N
                                              loc.getString("CTL_Issue_Product_Desc"),           // NOI18N
                                              0, false));
            ret.add(new ColumnDescriptor<>(LABEL_NAME_COMPONENT, String.class,
                                              loc.getString("CTL_Issue_Component_Title"),          // NOI18N
                                              loc.getString("CTL_Issue_Component_Desc"),           // NOI18N
                                              0, false));
            ret.add(new ColumnDescriptor<>(LABEL_NAME_VERSION, String.class,
                                              loc.getString("CTL_Issue_Version_Title"),          // NOI18N
                                              loc.getString("CTL_Issue_Version_Desc"),           // NOI18N
                                              0, false));
            ret.add(new ColumnDescriptor<>(LABEL_NAME_OS, String.class,
                                              loc.getString("CTL_Issue_OS_Title"),          // NOI18N
                                              loc.getString("CTL_Issue_OS_Desc"),           // NOI18N
                                              0, false));
            ret.add(new ColumnDescriptor<>(LABEL_NAME_PLATFORM, String.class,
                                              loc.getString("CTL_Issue_Platform_Title"),          // NOI18N
                                              loc.getString("CTL_Issue_Platform_Desc"),           // NOI18N
                                              0, false));
            ret.add(new ColumnDescriptor<>(LABEL_NAME_MILESTONE, String.class,
                                              loc.getString("CTL_Issue_Milestone_Title"),          // NOI18N
                                              loc.getString("CTL_Issue_Milestone_Desc"),           // NOI18N
                                              0, false));
            ret.add(new ColumnDescriptor<>(LABEL_NAME_REPORTER, String.class,
                                              loc.getString("CTL_Issue_Reporter_Title"),          // NOI18N
                                              loc.getString("CTL_Issue_Reporter_Desc"),           // NOI18N
                                              0, false));
            ret.add(new ColumnDescriptor<>(LABEL_NAME_MODIFICATION, String.class,
                                              loc.getString("CTL_Issue_Modification_Title"),          // NOI18N
                                              loc.getString("CTL_Issue_Modification_Desc"),           // NOI18N
                                              0, false));
            if(BugzillaUtil.showQAContact(repository)) {
                ret.add(new ColumnDescriptor<>(LABEL_NAME_QA_CONTACT, String.class,
                                              loc.getString("CTL_Issue_QA_Contact_Title"),          // NOI18N
                                              loc.getString("CTL_Issue_QA_Contact_Desc"),           // NOI18N
                                              0, false));
            }
            ret.add(new ColumnDescriptor<>(LABEL_NAME_KEYWORDS, String.class,
                                              loc.getString("CTL_Issue_Keywords_Title"),          // NOI18N
                                              loc.getString("CTL_Issue_Keywords_Desc"),           // NOI18N
                                              0, false));
            if(BugzillaUtil.showStatusWhiteboard(repository)) {
                ret.add(new ColumnDescriptor<>(LABEL_NAME_WHITEBOARD, String.class,
                                              loc.getString("CTL_Issue_Whiteboard_Title"),          // NOI18N
                                              loc.getString("CTL_Issue_Whiteboard_Desc"),           // NOI18N
                                              0, false));
            }
        return ret.toArray(new ColumnDescriptor[0]);
    }


    public IssueController getController() {
        if (controller == null) {
            controller = new BugzillaIssueController(this);
        }
        return controller;
    }

    @Override
    public String toString() {
        String str = getID() + " : "  + getSummary(); // NOI18N
        return str;
    }

    public IssueNode getNode() {
        BugzillaIssueNode n = nodeRef != null ? nodeRef.get() : null;
        if(n == null) {
            n = createNode();
            nodeRef = new WeakReference<>(n);
        }
        if (!EventQueue.isDispatchThread()) {
            // loads repository task data from disk
            getRepositoryTaskData();
        }
        return n;
    }

    public void setOwnerInfo(OwnerInfo info) {
        this.info = info;
    }
    
    public OwnerInfo getOwnerInfo() {
        return info;
    }

    public String getRecentChanges() {
        return recentChanges;
    }

    public BugzillaRepository getRepository() {
        return repository;
    }

    private void ensureConfigurationUptodate () {
        BugzillaConfiguration conf = getRepository().getConfiguration();
        NbTaskDataState taskDataState = null;
        try {
            taskDataState = getNbTask().getTaskDataState();
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.INFO, null, ex);
        }
        boolean needsRefresh = false;
        if (taskDataState != null && !isNew()) {
            for (TaskData taskData : new TaskData[] { 
                taskDataState.getLastReadData(),
                taskDataState.getLocalData(),
                taskDataState.getRepositoryData()
            }) {
                String product = getFieldValue(taskData, IssueField.PRODUCT);
                String resolution = getFieldValue(taskData, IssueField.RESOLUTION);
                String severity = getFieldValue(taskData, IssueField.SEVERITY);
                String milestone = getFieldValue(taskData, IssueField.MILESTONE);
                String version = getFieldValue(taskData, IssueField.VERSION);
                String priority = getFieldValue(taskData, IssueField.PRIORITY);
                String platform = getFieldValue(taskData, IssueField.PLATFORM);
                String status = getFieldValue(taskData, IssueField.STATUS);
                String os = getFieldValue(taskData, IssueField.OS);
                String component = getFieldValue(taskData, IssueField.COMPONENT);

                if(!component.isEmpty() && !conf.getComponents(product).contains(component) ||
                   !os.isEmpty() && !conf.getOSs().contains(os) ||
                   !status.isEmpty() && !conf.getStatusValues().contains(status) ||
                   !platform.isEmpty() && !conf.getPlatforms().contains(platform) ||
                   !priority.isEmpty() && !conf.getPriorities().contains(priority) ||
                   !product.isEmpty() && !conf.getProducts().contains(product) ||
                   !resolution.isEmpty() && !conf.getResolutions().contains(resolution) ||
                   !severity.isEmpty() && !conf.getSeverities().contains(severity) ||
                   !milestone.isEmpty() && !conf.getTargetMilestones(product).contains(milestone) ||
                   !version.isEmpty() && !conf.getVersions(product).contains(version))
                {
                    needsRefresh = true;
                    break;
                }
            }            
        }
        if (needsRefresh) {
            getRepository().refreshConfiguration();
            
        }
    }

    @Override
    protected void repositoryTaskDataLoaded (TaskData repositoryTaskData) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run () {
                if (updateTooltip()) {
                    fireDataChanged();
                }
            }
        });
    }    

    public String getRepositoryFieldValue (IssueField f) {
        NbTaskDataModel m = getModel();
        TaskData td;
        if (m == null) {
            td = getRepositoryTaskData();
            if (td == null) {
                return "..."; //NOI18N
            }
        } else {
            td = m.getRepositoryTaskData();
        }
        return getFieldValue(td, f);
    }
    
    public String getFieldValue(IssueField f) {
        NbTaskDataModel m = getModel();
        return getFieldValue(m == null ? null : m.getLocalTaskData(), f);
    }

    String getLastSeenFieldValue (IssueField f) {
        NbTaskDataModel m = getModel();
        return getFieldValue(m == null ? null : m.getLastReadTaskData(), f);
    }

    private static String getFieldValue (TaskData taskData, IssueField f) {
        if (taskData == null) {
            return "";
        }
        if(f.isSingleAttribute()) {
            TaskAttribute a = taskData.getRoot().getMappedAttribute(f.getKey());
            if(a != null && a.getValues().size() > 1) {
                return listValues(a);
            }
            return a != null ? a.getValue() : ""; // NOI18N
        } else {
            List<TaskAttribute> attrs = taskData.getAttributeMapper().getAttributesByType(taskData, f.getKey());
            // returning 0 would set status MODIFIED instead of NEW
            return "" + ( attrs != null && attrs.size() > 0 ?  attrs.size() : ""); // NOI18N
        }
    }

    /**
     * Returns a comma separated list created
     * from the values returned by TaskAttribute.getValues()
     *
     * @param a
     * @return
     */
    private static String listValues(TaskAttribute a) {
        if(a == null) {
            return "";                                                          // NOI18N
        }
        StringBuilder sb = new StringBuilder();
        List<String> l = a.getValues();
        for (int i = 0; i < l.size(); i++) {
            String s = l.get(i);
            sb.append(s);
            if(i < l.size() -1) {
                sb.append(",");                                                 // NOI18N
            }
        }
        return sb.toString();
    }

    void setFieldValue(IssueField f, String value) {
        if(f.isReadOnly()) {
            assert false : "can't set value into IssueField " + f.getKey();       // NOI18N
            return;
        }
        NbTaskDataModel m = getModel();
        // should not happen, setFieldValue either runs with model lock
        // or it is called from issue editor in AWT - the editor could not be closed by user in the meantime
        // (or it could. see issue #254373 -> reload attributes called setFV)
        boolean isshowing = getController().getComponent().isShowing();
        assert m != null || !isshowing : "isShowing=" + isshowing + ", model=" + m;
        if(m != null) {
            TaskData taskData = m.getLocalTaskData();
            TaskAttribute a = taskData.getRoot().getMappedAttribute(f.getKey());
            if (a == null) {
                if (f == IssueField.REASSIGN_TO_DEFAULT) {
                    setOperation(BugzillaOperation.reassignbycomponent);
                    return;
                }
                a = new TaskAttribute(taskData.getRoot(), f.getKey());
            }
            if(f == IssueField.PRODUCT) {
                handleProductChange(a);
            }
            Bugzilla.LOG.log(Level.FINER, "setting value [{0}] on field [{1}]", new Object[]{value, f.getKey()}) ;
            if (!value.equals(a.getValue())) {
                setValue(m, a, value);
            }
        } 
    }

    void setFieldValues(IssueField f, List<String> ccs) {
        NbTaskDataModel m = getModel();
        // should not happen, setFieldValue either runs with model lock
        // or it is called from issue editor in AWT - the editor could not be closed by user in the meantime
        // (or it could. see issue #254373 -> reload attributes called setFV)
        assert m != null || !getController().getComponent().isShowing();
        if(m != null) {
            TaskData taskData = m.getLocalTaskData();
            TaskAttribute a = taskData.getRoot().getMappedAttribute(f.getKey());
            if(a == null) {
                a = new TaskAttribute(taskData.getRoot(), f.getKey());
            }
            a.setValues(ccs);
            m.attributeChanged(a);
        } 
    }

    public List<String> getRepositoryFieldValues (IssueField f) {
        NbTaskDataModel m = getModel();
        return getFieldValues(m == null ? getRepositoryTaskData() : m.getRepositoryTaskData(), f);
    }

    public List<String> getFieldValues(IssueField f) {
        NbTaskDataModel m = getModel();
        return getFieldValues(m == null ? null : m.getLocalTaskData(), f);
    }

    List<String> getLastSeenFieldValues (IssueField f) {
        NbTaskDataModel m = getModel();
        return getFieldValues(m == null ? null : m.getLastReadTaskData(), f);
    }
    
    private static List<String> getFieldValues(TaskData taskData, IssueField f) {
        if (taskData == null) {
            return Collections.<String>emptyList();
        }
        if(f.isSingleAttribute()) {
            TaskAttribute a = taskData.getRoot().getMappedAttribute(f.getKey());
            if(a != null) {
                return a.getValues();
            } else {
                return Collections.emptyList();
            }
        } else {
            List<String> ret = new ArrayList<>();
            ret.add(getFieldValue(taskData, f));
            return ret;
        }
    }

    /**
     * Returns a status value for the given field<br>
     * <ul>
     *  <li>{@link #FIELD_STATUS_IRELEVANT} - issue wasn't seen yet
     *  <li>{@link #FIELD_STATUS_UPTODATE} - field value wasn't changed
     *  <li>{@link #FIELD_STATUS_MODIFIED} - field value was changed in repository
     *  <li>{@link #FIELD_STATUS_OUTGOING} - field value was changed locally
     *  <li>{@link #FIELD_STATUS_CONFLICT} - field value was changed both locally and remotely
     * </ul>
     * @param f IssueField
     * @return a status value
     */
    public int getFieldStatus(IssueField f) {
        NbTaskDataModel m = getModel();
        if (m == null) {
            return FIELD_STATUS_UPTODATE;
        }
        TaskAttribute ta = m.getLocalTaskData().getRoot().getMappedAttribute(f.getKey());
        boolean incoming = ta != null && m.hasIncomingChanges(ta, true);
        boolean outgoing = ta != null && m.hasOutgoingChanges(ta);
        if (ta == null) {
            return FIELD_STATUS_UPTODATE;
        } else if (incoming & outgoing) {
            return FIELD_STATUS_CONFLICT;
        } else if (incoming) {
            return FIELD_STATUS_MODIFIED;
        } else if (outgoing) {
            return FIELD_STATUS_OUTGOING;
        }
        return FIELD_STATUS_UPTODATE;
    }

    private BugzillaIssueNode createNode() {
        return new BugzillaIssueNode(this);
    }

    private void handleProductChange(TaskAttribute a) {
        if(!isNew() && initialProduct == null) {
            initialProduct = a.getValue();
        }
    }

    public void resolve (final String resolution) {
        assert !isNew();

        runWithModelLoaded(new Runnable() {
            @Override
            public void run () {
                String value = getFieldValue(IssueField.STATUS);
                if(!(value.equals("RESOLVED") && resolution.equals(getFieldValue(IssueField.RESOLUTION)))) { // NOI18N
                    setOperation(BugzillaOperation.resolve);
                    TaskAttribute rta = getModel().getLocalTaskData().getRoot();
                    TaskAttribute ta = rta.getMappedAttribute(BugzillaOperation.resolve.getInputId());
                    if(ta != null) { // ta can be null when changing status from CLOSED to RESOLVED
                        setValue(getModel(), ta, resolution);
                    }
                }
            }
        });
    }

    void accept() {
        setOperation(BugzillaOperation.accept);
    }

    void duplicate(String id) {
        NbTaskDataModel m = getModel();
        setOperation(BugzillaOperation.duplicate);
        TaskAttribute rta = m.getLocalTaskData().getRoot();
        TaskAttribute ta = rta.getMappedAttribute(BugzillaOperation.duplicate.getInputId());
        setValue(m, ta, id);
        wasDuplicated = true;
    }

    boolean canReassign() {
        NbTaskDataModel m = getModel();
        if (m == null) {
            return false;
        }
        BugzillaConfiguration rc = getRepository().getConfiguration();
        final BugzillaVersion installedVersion = rc != null ? rc.getInstalledVersion() : null;
        boolean oldRepository = installedVersion != null ? installedVersion.compareMajorMinorOnly(BugzillaVersion.BUGZILLA_3_2) < 0 : false;
        if (oldRepository) {
            TaskAttribute rta = m.getLocalTaskData().getRoot();
            TaskAttribute ta = rta.getMappedAttribute(BugzillaOperation.reassign.getInputId());
            return (ta != null);
        } else {
            return true;
        }
    }
    
    boolean canAssignToDefault() {
        NbTaskDataModel m = getModel();
        if (m == null) {
            return false;
        }
        BugzillaConfiguration rc = getRepository().getConfiguration();
        final BugzillaVersion installedVersion = rc != null ? rc.getInstalledVersion() : null;
        boolean pre4 = installedVersion != null ? installedVersion.compareMajorMinorOnly(BugzillaVersion.BUGZILLA_3_0) <= 0 : false;
        TaskData taskData = m.getLocalTaskData();
        if(pre4) {
            return BugzillaOperation.reassignbycomponent.getInputId() != null ? 
                        taskData.getRoot().getMappedAttribute(BugzillaOperation.reassignbycomponent.getInputId()) != null :
                        false;
        } else {
            TaskAttribute ta = taskData.getRoot().getAttribute(BugzillaAttribute.SET_DEFAULT_ASSIGNEE.getKey()); 
            return ta != null;
        }
    }
    
    boolean hasTimeTracking() {
        NbTaskDataModel m = getModel();
        return m != null && m.getLocalTaskData().getRoot()
                .getMappedAttribute(BugzillaAttribute.ACTUAL_TIME.getKey()) != null; // XXX dummy
    }

    void reassign(String user) {
        NbTaskDataModel m = getModel();
        setOperation(BugzillaOperation.reassign);
        TaskAttribute rta = m.getLocalTaskData().getRoot();
        TaskAttribute ta = rta.getMappedAttribute(BugzillaOperation.reassign.getInputId());
        if(ta != null) {
            setValue(m, ta, user);
        }
        ta = rta.getMappedAttribute(BugzillaAttribute.ASSIGNED_TO.getKey());
        if(ta != null) {
            setValue(m, ta, user);
        }
    }

    void verify() {
        setOperation(BugzillaOperation.verify);
    }

    void close() {
        setOperation(BugzillaOperation.close);
    }

    void reopen() {
        setOperation(BugzillaOperation.reopen);
    }

    private void setOperation (BugzillaOperation operation) {
        NbTaskDataModel m = getModel();
        TaskAttributeMapper mapper = m.getLocalTaskData().getAttributeMapper();
        for (TaskOperation op : mapper.getTaskOperations(m.getLocalTaskData().getRoot())) {
            if (op.getOperationId().equals(operation.toString())) {
                setOperation(op);
                return;
            }
        }
        // no operation found, leave to NO_OP ~ Leave as...
        setOperation(BugzillaOperation.none);
    }
    
    private void setOperation (TaskOperation operation) {
        NbTaskDataModel m = getModel();
        TaskAttribute rta = m.getLocalTaskData().getRoot();
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.OPERATION);
        m.getLocalTaskData().getAttributeMapper().setTaskOperation(ta, operation);
        m.attributeChanged(ta);
    }

    List<Attachment> getAttachments() {
        NbTaskDataModel m = getModel();
        List<TaskAttribute> attrs = m == null ? null : m.getLocalTaskData()
                .getAttributeMapper().getAttributesByType(m.getLocalTaskData(), TaskAttribute.TYPE_ATTACHMENT);
        if (attrs == null) {
            return Collections.emptyList();
        }
        List<Attachment> attachments = new ArrayList<Attachment>(attrs.size());
        for (TaskAttribute taskAttribute : attrs) {
            attachments.add(new Attachment(taskAttribute));
        }
        return attachments;
    }

    public void addAttachment(File file, final String comment, final String desc, String contentType, final boolean patch) {
        assert !EventQueue.isDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        final FileTaskAttachmentSource attachmentSource = new FileTaskAttachmentSource(file);
        if (contentType == null) {
            file = FileUtil.normalizeFile(file);
            String ct = FileUtil.getMIMEType(FileUtil.toFileObject(file));
            if ((ct != null) && (!"content/unknown".equals(ct))) { // NOI18N
                contentType = ct;
            } else {
                contentType = FileTaskAttachmentSource.getContentTypeFromFilename(file.getName());
            }
        }
        attachmentSource.setContentType(contentType);
        TaskData repositoryTaskData = getRepositoryTaskData();
        if (repositoryTaskData == null && (!synchronizeTask()
                || (repositoryTaskData = getRepositoryTaskData()) == null)) {
            // not fully initialized task, sync failed
            return;            
        }
        final TaskAttribute attAttribute = new TaskAttribute(repositoryTaskData.getRoot(),  TaskAttribute.TYPE_ATTACHMENT);
        TaskAttributeMapper mapper = attAttribute.getTaskData().getAttributeMapper();
        TaskAttribute a = attAttribute.createMappedAttribute(TaskAttribute.ATTACHMENT_DESCRIPTION);
        a.setValue(desc);
        a = attAttribute.createMappedAttribute(TaskAttribute.ATTACHMENT_IS_PATCH);
        mapper.setBooleanValue(a, patch);
        a = attAttribute.createMappedAttribute(TaskAttribute.ATTACHMENT_CONTENT_TYPE);
        a.setValue(contentType);

        AddAttachmentCommand cmd = new AddAttachmentCommand(getID(), repository, comment, attachmentSource, file, attAttribute);
        repository.getExecutor().execute(cmd);
        if(!cmd.hasFailed()) {
            refresh(true); // XXX to much refresh - is there no other way?
        }
    }

    Comment[] getComments() {
        final List<Comment> comments = new ArrayList<>();
        runWithModelLoaded(new Runnable() {
            @Override
            public void run() {
                NbTaskDataModel m = getModel();
                List<TaskAttribute> attrs = m == null ? null : m.getLocalTaskData()
                        .getAttributeMapper().getAttributesByType(m.getLocalTaskData(), TaskAttribute.TYPE_COMMENT);
                if (attrs != null) {
                    for (TaskAttribute taskAttribute : attrs) {
                        comments.add(new Comment(taskAttribute));
                    }
                }
            }
        });
        return comments.toArray(new Comment[0]);
    }

    // XXX carefull - implicit refresh
    public void addComment (final String comment, final boolean close) {
        assert !EventQueue.isDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        if(comment == null && !close) {
            return;
        }
        refresh();

        runWithModelLoaded(new Runnable() {
            @Override
            public void run () {
                // resolved attrs
                if (close) {
                    Bugzilla.LOG.log(Level.FINER, "resolving issue #{0} as fixed", new Object[]{getID()});
                    resolve(RESOLVE_FIXED); // XXX constant?

                    if(BugzillaUtil.isNbRepository(repository)) {
                        // check for other preselections
                        Properties p = System.getProperties();
                        Enumeration<Object> keys = p.keys();
                        List<String> keyList = new LinkedList<>();
                        while(keys.hasMoreElements()) {
                            Object key = keys.nextElement();
                            if(key.toString().startsWith(VCSHOOK_BUGZILLA_FIELD)) {
                                keyList.add(key.toString());
                            }
                        }
                        for (String key : keyList) {
                            String fieldName = key.substring(VCSHOOK_BUGZILLA_FIELD.length());
                            String value = p.getProperty(key);
                            IssueField issueField = repository.getConfiguration().getField(fieldName);
                            if(issueField != null) {
                                if(issueField.isReadOnly()) {
                                    Bugzilla.LOG.log(Level.WARNING, "field [{0}] is read-only.", new Object[]{repository.getUrl(), fieldName});
                                } else {
                                    setFieldValue(issueField, value);
                                }
                            } else {
                                Bugzilla.LOG.log(Level.WARNING, "Repsitory [{0}] has no field [{1}]", new Object[]{repository.getUrl(), fieldName});
                            }
                        }
                    }

                }
                if(comment != null) {
                    addComment(comment);
                }        

                submitAndRefresh();
            }
        });
    }

    public void addComment (final String comment) {
        if(comment != null && !comment.isEmpty()) {
            runWithModelLoaded(new Runnable() {
                @Override
                public void run () {
                    Bugzilla.LOG.log(Level.FINER, "adding comment [{0}] to issue #{1}", new Object[]{comment, getID()});
                    TaskAttribute ta = getModel().getLocalTaskData().getRoot().getMappedAttribute(IssueField.COMMENT.getKey());
                    String value = ta.getValue();
                    if (value == null || value.trim().isEmpty()) {
                        value = comment;
                    } else {
                        value += "\n\n" + comment; //NOI18N
                    }
                    setValue(getModel(), ta, value);
                }
            });
        }
    }
    
    private void setDueDateAndSubmit (final Date date) {
        refresh();
        runWithModelLoaded(new Runnable() {
            @Override
            public void run () {
                if (date == null) {
                    setFieldValue(IssueField.DEADLINE, "");
                } else {
                    setFieldValue(IssueField.DEADLINE, DUE_DATE_FORMAT.format(date));
                }
                submitAndRefresh();
            }
        });
    }

    public void attachPatch(File file, String description, boolean isPatch) {
        // HACK for attaching hg bundles - they are NOT patches
        isPatch = !file.getName().endsWith(".hg"); // NOI18N
        addAttachment(file, null, description, null, isPatch);
    }

    private void prepareSubmit() {
        if (initialProduct != null) {
            // product change
            TaskAttribute ta = getModel().getLocalTaskData().getRoot().getMappedAttribute(BugzillaAttribute.CONFIRM_PRODUCT_CHANGE.getKey());
            if (ta == null) {
                ta = BugzillaTaskDataHandler.createAttribute(getModel().getLocalTaskData().getRoot(), BugzillaAttribute.CONFIRM_PRODUCT_CHANGE);
            }
            setValue(getModel(), ta, "1"); //NOI18N
        }
    }

    @NbBundle.Messages({
        "# {0} - task id and summary",
        "MSG_BugzillaIssue.statusBar.submitted=Task {0} submitted.",
        "MSG_LOG_FILE_DESC=IDE log",
        "BugzillaIssue.attachment.noDescription=<no description>"
    })
    public boolean submitAndRefresh() {
        final boolean[] result = new boolean[1];
        runWithModelLoaded(new Runnable() {

            @Override
            public void run () {
                assert !EventQueue.isDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

                prepareSubmit();
                boolean addIDELog = fixAttachLog();
                List<AttachmentsPanel.AttachmentInfo> newAttachments = getNewAttachments();
                if (!newAttachments.isEmpty()) {
                    // clear before submit, we do not know how connectors deal with internal attributes
                    setNewAttachments(Collections.<AttachmentsPanel.AttachmentInfo>emptyList());
                }
                final boolean wasNew = isNew();

                SubmitTaskCommand submitCmd;
                try {
                    if (saveChanges()) {
                        fireChanged();
                        submitCmd = MylynSupport.getInstance().getCommandFactory().createSubmitTaskCommand(getModel());
                    } else {
                        result[0] = false;
                        return;
                    }
                } catch (CoreException ex) {
                    Bugzilla.LOG.log(Level.WARNING, null, ex);
                    result[0] = false;
                    return;
                }
                repository.getExecutor().execute(submitCmd);
                if (!submitCmd.hasFailed()) {
                    taskSubmitted(submitCmd.getSubmittedTask());
                }

                if (!wasNew) {
                    refresh();
                    if (wasDuplicated && !submitCmd.hasFailed()) {
                        try {
                            BugzillaIssue dupe = repository.getIssueCache().getIssue(getFieldValue(IssueField.DUPLICATE_ID));
                            if(dupe != null) {
                                // if duplicate known on client then refresh to 
                                // avoid potential mid-air ...
                                dupe.refresh();
                            }
                        } finally {
                            wasDuplicated = false;        
                        }
                    }
                } else {
                    RepositoryResponse rr = submitCmd.getRepositoryResponse();
                    if(!submitCmd.hasFailed()) {
                        updateRecentChanges();
                        updateTooltip();
                        fireDataChanged();
                        String id = getID();
                        repository.getIssueCache().setIssue(id, BugzillaIssue.this);
                        Bugzilla.LOG.log(Level.FINE, "created issue #{0}", id);
                        // a new issue was created -> refresh all queries
                        repository.refreshAllQueries();
                    } else {
                        Bugzilla.LOG.log(Level.FINE, "submiting failed");
                        if(rr != null) {
                            Bugzilla.LOG.log(Level.FINE, "repository response {0}", rr.getReposonseKind());
                        } else {
                            Bugzilla.LOG.log(Level.FINE, "no repository response available");
                        }
                    }
                }

                if(submitCmd.hasFailed()) {
                    result[0] = false;
                    boolean needSave = false;
                    if (addIDELog) {
                        setFieldValue(IssueField.NB_ATTACH_IDE_LOG, "1"); //NOI18N
                        needSave = true;
                    }
                    if (!newAttachments.isEmpty()) {
                        setNewAttachments(newAttachments);
                        needSave = true;
                    }
                    if (needSave) {
                        saveChanges();
                    }
                    return;
                } else {
                    if (addIDELog) {
                        File f = new File(Places.getUserDirectory(), NbBugzillaConstants.NB_LOG_FILE_PATH);
                        if (f.isFile()) {
                            addAttachment(f, "", MSG_LOG_FILE_DESC(), NbBugzillaConstants.NB_LOG_FILE_ATT_CONT_TYPE, false);
                        }
                    }
                    if (!newAttachments.isEmpty()) {
                        for (AttachmentsPanel.AttachmentInfo attachment : newAttachments) {
                            if (attachment.getFile().isFile()) {
                                if (attachment.getDescription().trim().length() == 0) {
                                    attachment.setDescription(Bundle.BugzillaIssue_attachment_noDescription());
                                }
                                addAttachment(attachment.getFile(), null, attachment.getDescription(), attachment.getContentType(), attachment.isPatch()); // NOI18N
                            } else {
                                // PENDING notify user
                            }
                        }
                    }
                }
                StatusDisplayer.getDefault().setStatusText(Bundle.MSG_BugzillaIssue_statusBar_submitted(
                        getDisplayName()));

                setUpToDate(true, false);
                result[0] = true;
            }

            private boolean fixAttachLog () {
                String val = getFieldValue(IssueField.NB_ATTACH_IDE_LOG);
                getModel().getLocalTaskData().getRoot().removeAttribute(IssueField.NB_ATTACH_IDE_LOG.getKey());
                return "1".equals(val);
            }
            
        });
        return result[0];
    }

    void updateModelAndRefresh () {
        runWithModelLoaded(new Runnable() {
            @Override
            public void run() {
                updateModel();
                refresh();
            }
        });
    }
    
    public boolean refresh() {
        assert !EventQueue.isDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        return refresh(false);
    }

    private boolean refresh (boolean afterSubmitRefresh) { // XXX cacheThisIssue - we probalby don't need this, just always set the issue into the cache
        assert !EventQueue.isDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        boolean synced = synchronizeTask();
        if (!loading) {
            // refresh only when model is not currently being loaded
            // otherwise it most likely ends up in editor not fully initialized
            refreshViewData(afterSubmitRefresh);
        }
        return synced;
    }

    private void refreshViewData(boolean force) {
        if (controller != null) {
            // view might not exist yet and we won't unnecessarily create it
            controller.refreshViewData(force);
        }
    }

    /**
     * Returns available operations for this issue
     * @return
     */
    Map<String, TaskOperation> getAvailableOperations () {
        if (availableOperations == null) {
            HashMap<String, TaskOperation> operations = new HashMap<String, TaskOperation>(5);
            NbTaskDataModel model = getModel();
            List<TaskAttribute> allOperations = model.getLocalTaskData().getAttributeMapper().getAttributesByType(model.getLocalTaskData(), TaskAttribute.TYPE_OPERATION);
            for (TaskAttribute operation : allOperations) {
                // the test must be here, 'operation' (applying writable action) is also among allOperations
                if (operation.getId().startsWith(TaskAttribute.PREFIX_OPERATION)) {
                    operations.put(operation.getId().substring(TaskAttribute.PREFIX_OPERATION.length()), TaskOperation.createFrom(operation));
                }
            }
            availableOperations = operations;
        }

        return availableOperations;
    }

    boolean isResolveAvailable () {
        Map<String, TaskOperation> operations = getAvailableOperations();
        return operations.containsKey(BugzillaOperation.resolve.toString());
    }
    
    private String getMappedValue(TaskAttribute a, String key) {
        TaskAttribute ma = a.getMappedAttribute(key);
        if(ma != null) {
            return ma.getValue();
        }
        return null;
    }

    public void setUpToDate (boolean seen) {
        setUpToDate(seen, true);
    }

    private boolean updateTooltip () {
        String displayName = getDisplayName();
        if (displayName.startsWith("#")) { //NOI18N
            displayName = displayName.replaceFirst("#", ""); //NOI18N
        }
        String oldTooltip = tooltip;

        SynchronizationState state = getSynchronizationState();
        URL iconPath = getStateIcon(state);
        String iconCode = "";
        if (iconPath != null) {
            iconCode = "<img src=\"" + iconPath + "\">&nbsp;"; //NOI18N
        }
        String stateName = getStateDisplayName(state);

        String priorityLabel = NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Priority_Title"); //NOI18N
        String priority = getRepositoryFieldValue(IssueField.PRIORITY);
        URL priorityIcon = BugzillaConfig.getInstance().getPriorityIconURL(priority);

        boolean showIssueType = BugzillaUtil.showIssueType(repository);
        String typeLabel = NbBundle.getMessage(BugzillaIssue.class, showIssueType ? "LBL_Type" : "CTL_Issue_Severity_Title"); //NOI18N
        String type = showIssueType ? getRepositoryFieldValue(IssueField.ISSUE_TYPE) : getRepositoryFieldValue(IssueField.SEVERITY);

        String productLabel = NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Product_Title"); //NOI18N
        String product = getRepositoryFieldValue(IssueField.PRODUCT);

        String componentLabel = NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Component_Title"); //NOI18N
        String component = getRepositoryFieldValue(IssueField.COMPONENT);

        String assigneeLabel = NbBundle.getMessage(BugzillaIssue.class, "LBL_Assigned"); //NOI18N
        String assignee = getRepositoryFieldValue(IssueField.ASSIGNED_TO);

        String statusLabel = NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Status_Title"); //NOI18N
        String status = getRepositoryFieldValue(IssueField.STATUS);
        String resolution = getRepositoryFieldValue(IssueField.RESOLUTION);

        if (resolution != null && !resolution.trim().isEmpty()) {
            status += "/" + resolution; //NOI18N
        }
        String scheduledLabel = NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Scheduled_Title"); //NOI18N
        String scheduled = getScheduleDisplayString();

        String dueLabel = NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Due_Title"); //NOI18N
        String due = getDueDisplayString();
        

        String estimateLabel = NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Estimate_Title"); //NOI18N
        String estimate = getEstimateDisplayString();

        String fieldTable = "<table>" //NOI18N
            + "<tr><td><b>" + priorityLabel + ":</b></td><td><img src=\"" + priorityIcon + "\">&nbsp;" + priority + "</td><td style=\"padding-left:25px;\"><b>" + typeLabel + ":</b></td><td>" + type + "</td></tr>" //NOI18N
            + "<tr><td><b>" + productLabel + ":</b></td><td>" + product + "</td><td style=\"padding-left:25px;\"><b>" + componentLabel + ":</b></td><td>" + component + "</td></tr>" //NOI18N
            + "<tr><td><b>" + assigneeLabel + ":</b></td><td colspan=\"3\">" + assignee + "</td></tr>"
            + "<tr><td><b>" + statusLabel + ":</b></td><td colspan=\"3\">" + status + "</td></tr>"; //NOI18N

        if (!scheduled.isEmpty()) {
            fieldTable += "<tr><td><b>" + scheduledLabel + ":</b></td><td colspan=\"3\">" + scheduled + "</td></tr>"; //NOI18N
        }
        boolean addNewLine = !due.isEmpty() || !estimate.isEmpty();
        if (addNewLine) {
            fieldTable += "<tr>"; //NOI18N
        }
        if (!due.isEmpty()) {
            fieldTable += "<tr><td><b>" + dueLabel + ":</b></td><td>" + due + "</td>"; //NOI18N
        }
        if (!estimate.isEmpty()) {
            fieldTable += "<td style=\"padding-left:25px;\"><b>" + estimateLabel + ":</b></td><td>" + estimate + "</td>"; //NOI18N
        }
        if (addNewLine) {
            fieldTable += "</tr>"; //NOI18N
        }
        fieldTable += "</table>"; //NOI18N
        
        StringBuilder sb = new StringBuilder("<html>"); //NOI18N
        sb.append("<b>").append(displayName).append("</b><br>"); //NOI18N
        if (stateName != null && !stateName.isEmpty()) {
            sb.append("<p style=\"padding:5px;\">").append(iconCode).append(stateName).append("</p>"); //NOI18N
        }
        sb.append("<hr>"); //NOI18N
        sb.append(fieldTable);
        sb.append("</html>"); //NOI18N
        tooltip = sb.toString();
        return !oldTooltip.equals(tooltip);
    }

    private URL getStateIcon(SynchronizationState state) {
        URL iconPath = null;
        if (state.equals(SynchronizationState.CONFLICT)) {
            iconPath = ICON_CONFLICT_PATH;
        } else if (state.equals(SynchronizationState.INCOMING) || state.equals(SynchronizationState.INCOMING_NEW)) {
            iconPath = ICON_REMOTE_PATH;
        } else if (state.equals(SynchronizationState.OUTGOING) || state.equals(SynchronizationState.OUTGOING_NEW)) {
            iconPath = ICON_UNSUBMITTED_PATH;
        }
        return iconPath;
    }

    private String getStateDisplayName(SynchronizationState state) {
        String displayName = "";
        if (state.equals(SynchronizationState.CONFLICT)) {
            displayName = NbBundle.getMessage(BugzillaIssue.class, "LBL_ConflictShort"); //NOI18N;
        } else if (state.equals(SynchronizationState.INCOMING)) {
            displayName = NbBundle.getMessage(BugzillaIssue.class, "LBL_RemoteShort"); //NOI18N;
        } else if (state.equals(SynchronizationState.INCOMING_NEW)) {
            displayName = NbBundle.getMessage(BugzillaIssue.class, "LBL_RemoteNewShort"); //NOI18N;
        } else if (state.equals(SynchronizationState.OUTGOING)) {
            displayName = NbBundle.getMessage(BugzillaIssue.class, "LBL_UnsubmittedShort"); //NOI18N;
        } else if (state.equals(SynchronizationState.OUTGOING_NEW)) {
            displayName = NbBundle.getMessage(BugzillaIssue.class, "LBL_UnsubmittedNewShort"); //NOI18N;
        }
        return displayName;
    }
    
    private boolean updateRecentChanges () {
        String oldChanges = recentChanges;
        recentChanges = "";
        SynchronizationState syncState = getSynchronizationState();
        if (syncState == SynchronizationState.INCOMING_NEW) {
            recentChanges = NbBundle.getMessage(BugzillaIssue.class, "LBL_NEW_STATUS"); //NOI18N
        } else if (syncState == SynchronizationState.INCOMING
                || syncState == SynchronizationState.CONFLICT) {
            try {
                NbTaskDataState taskDataState = getNbTask().getTaskDataState();
                if (taskDataState != null) {
                    TaskData repositoryData = taskDataState.getRepositoryData();
                    TaskData lastReadData = taskDataState.getLastReadData();
                    List<IssueField> changedFields = new ArrayList<>();
                    for (IssueField f : getRepository().getConfiguration().getFields()) {
                        if (f==IssueField.MODIFICATION
                                || f==IssueField.REPORTER_NAME
                                || f==IssueField.QA_CONTACT_NAME
                                || f==IssueField.ASSIGNED_TO_NAME) {
                            continue;
                        }
                        String value = getFieldValue(repositoryData, f);
                        String seenValue = getFieldValue(lastReadData, f);
                        if(!value.trim().equals(seenValue.trim())) {
                            changedFields.add(f);
                        }
                    }
                    int changedCount = changedFields.size();
                    if(changedCount == 1) {
                        String ret = null;
                        for (IssueField changedField : changedFields) {
                            if (changedField == IssueField.SUMMARY) {
                                ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_SUMMARY_CHANGED_STATUS"); // NOI18N
                            } else if (changedField == IssueField.CC) {
                                ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_CC_FIELD_CHANGED_STATUS"); // NOI18N
                            } else if (changedField == IssueField.KEYWORDS) {
                                ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_KEYWORDS_CHANGED_STATUS"); // NOI18N
                            } else if (changedField == IssueField.DEPENDS_ON || changedField == IssueField.BLOCKS) {
                                ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_DEPENDENCE_CHANGED_STATUS"); // NOI18N
                            } else if (changedField == IssueField.COMMENT_COUNT) {
                                String value = getFieldValue(repositoryData, changedField);
                                String seenValue = getFieldValue(lastReadData, changedField);
                                if(seenValue.equals("")) { // NOI18N
                                    seenValue = "0"; // NOI18N
                                }
                                int count = 0;
                                try {
                                    count = Integer.parseInt(value) - Integer.parseInt(seenValue);
                                } catch(NumberFormatException ex) {
                                    Bugzilla.LOG.log(Level.WARNING, ret, ex);
                                }
                                ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_COMMENTS_CHANGED", new Object[] {count}); // NOI18N
                            } else if (changedField == IssueField.ATTACHEMENT_COUNT) {
                                ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_ATTACHMENTS_CHANGED"); // NOI18N
                            } else {
                                ret = NbBundle.getMessage(BugzillaIssue.class, "LBL_CHANGED_TO", new Object[] {changedField.getDisplayName(), getFieldValue(repositoryData, changedField)}); // NOI18N
                            }
                        }
                        recentChanges = ret;
                    } else {
                        for (IssueField changedField : changedFields) {
                            String key;
                            if (changedField == IssueField.SUMMARY) {
                                key = "LBL_CHANGES_INCL_SUMMARY"; // NOI18N
                            } else if (changedField == IssueField.PRIORITY) {
                                key = "LBL_CHANGES_INCL_PRIORITY"; // NOI18N
                            } else if (changedField == IssueField.SEVERITY) {
                                key = "LBL_CHANGES_INCL_SEVERITY"; // NOI18N
                            } else if (changedField == IssueField.ISSUE_TYPE) {
                                key = "LBL_CHANGES_INCL_ISSUE_TYPE"; // NOI18N
                            } else if (changedField == IssueField.PRODUCT) {
                                key = "LBL_CHANGES_INCL_PRODUCT"; // NOI18N
                            } else if (changedField == IssueField.COMPONENT) {
                                key = "LBL_CHANGES_INCL_COMPONENT"; // NOI18N
                            } else if (changedField == IssueField.PLATFORM) {
                                key = "LBL_CHANGES_INCL_PLATFORM"; // NOI18N
                            } else if (changedField == IssueField.VERSION) {
                                key = "LBL_CHANGES_INCL_VERSION"; // NOI18N
                            } else if (changedField == IssueField.MILESTONE) {
                                key = "LBL_CHANGES_INCL_MILESTONE"; // NOI18N
                            } else if (changedField == IssueField.KEYWORDS) {
                                key = "LBL_CHANGES_INCL_KEYWORDS"; // NOI18N
                            } else if (changedField == IssueField.URL) {
                                key = "LBL_CHANGES_INCL_URL"; // NOI18N
                            } else if (changedField == IssueField.ASSIGNED_TO) {
                                key = "LBL_CHANGES_INCL_ASSIGNEE"; // NOI18N
                            } else if (changedField == IssueField.QA_CONTACT) {
                                key = "LBL_CHANGES_INCL_QA_CONTACT"; // NOI18N
                            } else if (changedField == IssueField.DEPENDS_ON || changedField == IssueField.BLOCKS) {
                                key = "LBL_CHANGES_INCLUSIVE_DEPENDENCE"; // NOI18N
                            } else {
                                key = "LBL_CHANGES"; // NOI18N
                            }
                            recentChanges = NbBundle.getMessage(BugzillaIssue.class, key, new Object[] {changedCount});
                        }
                    }
                }
            } catch (CoreException ex) {
                Bugzilla.LOG.log(Level.WARNING, null, ex);
            }
        }
        return !oldChanges.equals(recentChanges);
    }
    
    @Override
    protected void modelSaved (NbTaskDataModel model) {
        if (controller != null) {
            controller.modelStateChanged(model.isDirty(), model.hasOutgoingChanged());
        }
    }
    
    @Override
    protected String getSummary (TaskData taskData) {
        return getFieldValue(taskData, IssueField.SUMMARY);
    }

    @Override
    protected void attributeChanged (NbTaskDataModelEvent event, NbTaskDataModel model) {
        if (controller != null) {
            // view might not exist yet and we won't unnecessarily create it
            controller.modelStateChanged(model.isDirty(), model.isDirty() || !model.getChangedAttributes().isEmpty());
        }
    }
    
    @Override
    protected boolean synchronizeTask () {
        try {
            NbTask task = getNbTask();
            synchronized (task) {
                Bugzilla.LOG.log(Level.FINE, "refreshing issue #{0}", task.getTaskId());
                SynchronizeTasksCommand cmd = MylynSupport.getInstance().getCommandFactory().createSynchronizeTasksCommand(
                        getRepository().getTaskRepository(), Collections.<NbTask>singleton(task));
                getRepository().getExecutor().execute(cmd);
                return !cmd.hasFailed();
            }
        } catch (CoreException ex) {
            // should not happen
            Bugzilla.LOG.log(Level.WARNING, null, ex);
            return false;
        }
    }

    boolean save () {
        return saveChanges();
    }

    private void setValue (NbTaskDataModel model, TaskAttribute ta, String value) {
        TaskData repositoryTaskData = model.getRepositoryTaskData();
        if (value.isEmpty() && repositoryTaskData != null) {
            // should be empty or set to ""???
            TaskAttribute a = repositoryTaskData.getRoot().getAttribute(ta.getId());
            if (a == null || a.getValues().isEmpty()) {
                // repository value is also empty list, so let's set to the same
                ta.clearValues();
            } else {
                ta.setValue(value);
            }
        } else {
            ta.setValue(value);
        }
        model.attributeChanged(ta);
    }

    boolean setUnsubmittedAttachments (List<AttachmentsPanel.AttachmentInfo> newAttachments) {
        return super.setNewAttachments(newAttachments);
    }

    List<AttachmentsPanel.AttachmentInfo> getUnsubmittedAttachments () {
        return getNewAttachments();
    }

    void setTaskPrivateNotes (String notes) {
        super.setPrivateNotes(notes);
        if (controller != null) {
            controller.modelStateChanged(true, hasLocalEdits());
        }
    }
    
    public void setTaskDueDate (final Date date, final boolean persistChange) {
        runWithModelLoaded(new Runnable() {

            @Override
            public void run () {
                if (hasTimeTracking()) {
                    setDueDateAndSubmit(date);
                } else {
                    setDueDate(date, persistChange);
                    if (controller != null) {
                        controller.modelStateChanged(hasUnsavedChanges(), hasLocalEdits());
                    }
                    if (persistChange) {
                        dataChanged();
                    }
                }
            }
        });
    }
    
    public void setTaskScheduleDate (IssueScheduleInfo date, boolean persistChange) {
        super.setScheduleDate(date, persistChange);
        if (controller != null) {
            controller.modelStateChanged(hasUnsavedChanges(), hasLocalEdits());
        }
        if (persistChange) {
            dataChanged();
        }
    }

    public void setTaskEstimate (int estimate, boolean persistChange) {
        super.setEstimate(estimate, persistChange);
        if (controller != null) {
            controller.modelStateChanged(hasUnsavedChanges(), hasLocalEdits());
        }
        if (persistChange) {
            dataChanged();
        }
    }

    public boolean discardLocalEdits () {
        final boolean retval[] = new boolean[1];
        runWithModelLoaded(new Runnable() {
            @Override
            public void run () {
                clearUnsavedChanges();
                retval[0] = cancelChanges();
                if (controller != null) {
                    controller.modelStateChanged(hasUnsavedChanges(), hasLocalEdits());
                    controller.refreshViewData(false);
                }
            }
        });
        return retval[0];
    }

    public String getPriority() {
        return getRepositoryFieldValue(IssueField.PRIORITY);
    }

    class Comment {
        private final Date when;
        private final String author;
        private final String authorName;
        private final Long number;
        private final String text;
        private final Double worked;

        public Comment(TaskAttribute a) {
            Date d = null;
            String s = "";
            try {
                s = getMappedValue(a, TaskAttribute.COMMENT_DATE);
                if(s != null && !s.trim().equals("")) {                         // NOI18N
                    d = CC_DATE_FORMAT.parse(s);
                }
            } catch (ParseException ex) {
                Bugzilla.LOG.log(Level.SEVERE, s, ex);
            }
            when = d;
            TaskAttribute authorAttr = a.getMappedAttribute(TaskAttribute.COMMENT_AUTHOR);
            if (authorAttr != null) {
                author = authorAttr.getValue();
                TaskAttribute nameAttr = authorAttr.getMappedAttribute(TaskAttribute.PERSON_NAME);
                authorName = nameAttr != null ? nameAttr.getValue() : null;
            } else {
                author = authorName = null;
            }
            String n = getMappedValue(a, TaskAttribute.COMMENT_NUMBER);
            number = n != null ? Long.parseLong(n) : null;
            text = getMappedValue(a, TaskAttribute.COMMENT_TEXT);
            String workedString = getMappedValue(a, BugzillaAttribute.WORK_TIME.getKey());
            
            double dbWorked = 0;
            if(workedString == null || workedString.isEmpty()) {
                dbWorked = 0.0;
            } else {
                try {
                    dbWorked = Double.parseDouble(workedString);
                } catch (NumberFormatException e) {
                    Bugzilla.LOG.log(Level.WARNING, "WORK_TIME time for comment " + number + " is " + workedString , e);
                    dbWorked = 0;
                }
            }
            worked = dbWorked;
        }

        public Long getNumber() {
            return number;
        }

        public String getText() {
            return text;
        }

        public Date getWhen() {
            return when;
        }

        public String getAuthor() {
            return author;
        }

        public String getAuthorName() {
            return authorName;
        }

        public Double getWorked() {
            return worked;
        }
    }

    class Attachment extends AttachmentsPanel.AbstractAttachment {
        private final String desc;
        private final String filename;
        private final String author;
        private final String authorName;
        private final Date date;
        private final String id;
        private final String contentType;
        private final String isDeprected;
        private final String size;
        private final String isPatch;
        private final String url;

        public Attachment(TaskAttribute ta) {
            id = ta.getValue();
            Date d = null;
            String s = "";
            try {
                s = getMappedValue(ta, TaskAttribute.ATTACHMENT_DATE);
                if(s != null && !s.trim().equals("")) {                         // NOI18N
                    d = CC_DATE_FORMAT.parse(s);
                }
            } catch (ParseException ex) {
                Bugzilla.LOG.log(Level.SEVERE, s, ex);
            }
            date = d;
            filename = getMappedValue(ta, TaskAttribute.ATTACHMENT_FILENAME);
            desc = getMappedValue(ta, TaskAttribute.ATTACHMENT_DESCRIPTION);

            TaskAttribute authorAttr = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_AUTHOR);
            if(authorAttr != null) {
                author = authorAttr.getValue();
                TaskAttribute nameAttr = authorAttr.getMappedAttribute(TaskAttribute.PERSON_NAME);
                authorName = nameAttr != null ? nameAttr.getValue() : null;
            } else {
                authorAttr = ta.getTaskData().getRoot().getMappedAttribute(IssueField.REPORTER.getKey()); 
                if(authorAttr != null) {
                    author = authorAttr.getValue();
                    TaskAttribute nameAttr = authorAttr.getMappedAttribute(TaskAttribute.PERSON_NAME);
                    authorName = nameAttr != null ? nameAttr.getValue() : null;
                } else {
                    author = authorName = null;
                }
            }
            contentType = getMappedValue(ta, TaskAttribute.ATTACHMENT_CONTENT_TYPE);
            isDeprected = getMappedValue(ta, TaskAttribute.ATTACHMENT_IS_DEPRECATED);
            isPatch = getMappedValue(ta, TaskAttribute.ATTACHMENT_IS_PATCH);
            size = getMappedValue(ta, TaskAttribute.ATTACHMENT_SIZE);
            url = getMappedValue(ta, TaskAttribute.ATTACHMENT_URL);
        }

        @Override
        public boolean isPatch() {
            return "1".equals(isPatch);
        }
        
        @Override
        public String getAuthorName() {
            return authorName;
        }

        @Override
        public String getAuthor() {
            return author;
        }

        @Override
        public Date getDate() {
            return date;
        }

        @Override
        public String getDesc() {
            return desc;
        }

        @Override
        public String getFilename() {
            return filename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        public String getId() {
            return id;
        }

        @Override
        public boolean isDeprecated() {
            return "1".equals(isDeprected);
        }

        public String getSize() {
            return size;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public void getAttachementData(final OutputStream os) {
            assert !EventQueue.isDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N            
            repository.getExecutor().execute(new GetAttachmentCommand(repository, id, os));
        }

        @Override
        public void open() {
            super.open(); //To change body of generated methods, choose Tools | Templates.
        }
        
    }

    @Override
    protected void taskDataUpdated () {
        availableOperations = null;
        ensureConfigurationUptodate();
        Bugzilla.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                dataChanged();
            }
        });
    }

    private void dataChanged () {
        updateTooltip();
        fireDataChanged();
        refreshViewData(false);
    }

    @Override
    protected void taskModified (boolean syncStateChanged) {
        if (updateRecentChanges() | updateTooltip()) {
            fireDataChanged();
        }
        if (syncStateChanged) {
            fireStatusChanged();
        }
    }

    void fireChangeEvent () {
        fireChanged();
    }
    
}
