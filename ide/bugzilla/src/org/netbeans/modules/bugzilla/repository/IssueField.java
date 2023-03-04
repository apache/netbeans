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

package org.netbeans.modules.bugzilla.repository;

import org.eclipse.mylyn.internal.bugzilla.core.BugzillaAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.netbeans.modules.mylyn.util.AbstractNbTaskWrapper;
import org.openide.util.NbBundle;

/**
 * Information about an issue field.
 *
 * @author Jan Stola, Tomas Stupka
 */
public class IssueField {
    public static final IssueField SUMMARY = new IssueField(BugzillaAttribute.SHORT_DESC.getKey(), "LBL_SUMMARY"); // NOI18N
    public static final IssueField WHITEBOARD = new IssueField(BugzillaAttribute.STATUS_WHITEBOARD.getKey(), "LBL_WHITEBOARD"); // NOI18N
    public static final IssueField STATUS = new IssueField(TaskAttribute.STATUS, "LBL_STATUS"); // NOI18N
    public static final IssueField PRIORITY = new IssueField(BugzillaAttribute.PRIORITY.getKey(), "LBL_PRIORITY"); // NOI18N
    public static final IssueField RESOLUTION = new IssueField(TaskAttribute.RESOLUTION, "LBL_RESOLUTION"); // NOI18N
    public static final IssueField PRODUCT = new IssueField(BugzillaAttribute.PRODUCT.getKey(), "LBL_PRODUCT"); // NOI18N
    public static final IssueField COMPONENT = new IssueField(BugzillaAttribute.COMPONENT.getKey(), "LBL_COMPONENT"); // NOI18N
    public static final IssueField VERSION = new IssueField(BugzillaAttribute.VERSION.getKey(), "LBL_VERSION"); // NOI18N
    public static final IssueField PLATFORM = new IssueField(BugzillaAttribute.REP_PLATFORM.getKey(), "LBL_PLATFORM"); // NOI18N
    public static final IssueField OS = new IssueField(BugzillaAttribute.OP_SYS.getKey(), "LBL_OS"); // NOI18N
    public static final IssueField MILESTONE = new IssueField(BugzillaAttribute.TARGET_MILESTONE.getKey(), "LBL_MILESTONE"); // NOI18N
    public static final IssueField REPORTER = new IssueField(BugzillaAttribute.REPORTER.getKey(), "LBL_REPORTER"); // NOI18N
    public static final IssueField REPORTER_NAME = new IssueField(BugzillaAttribute.REPORTER_NAME.getKey(), "LBL_REPORTER_NAME"); // NOI18N
    public static final IssueField ASSIGNED_TO = new IssueField(BugzillaAttribute.ASSIGNED_TO.getKey(), "LBL_ASSIGNED_TO"); // NOI18N
    public static final IssueField ASSIGNED_TO_NAME = new IssueField(BugzillaAttribute.ASSIGNED_TO_NAME.getKey(), "LBL_ASSIGNED_TO_NAME"); // NOI18N
    public static final IssueField QA_CONTACT = new IssueField(BugzillaAttribute.QA_CONTACT.getKey(), "LBL_QA_CONTACT"); // NOI18N
    public static final IssueField QA_CONTACT_NAME = new IssueField(BugzillaAttribute.QA_CONTACT_NAME.getKey(), "LBL_QA_CONTACT_NAME"); // NOI18N
    public static final IssueField DEPENDS_ON = new IssueField(BugzillaAttribute.DEPENDSON.getKey(), "LBL_DEPENDS_ON"); // NOI18N
    public static final IssueField BLOCKS = new IssueField(BugzillaAttribute.BLOCKED.getKey(), "LBL_BLOCKS"); // NOI18N
    public static final IssueField URL = new IssueField(BugzillaAttribute.BUG_FILE_LOC.getKey(), "LBL_URL"); // NOI18N
    public static final IssueField KEYWORDS = new IssueField(BugzillaAttribute.KEYWORDS.getKey(), "LBL_KEYWORDS"); // NOI18N
    public static final IssueField SEVERITY = new IssueField(BugzillaAttribute.BUG_SEVERITY.getKey(), "LBL_SEVERITY"); // NOI18N
    public static final IssueField ISSUE_TYPE = new IssueField("cf_bug_type", "LBL_ISSUE_TYPE"); // NOI18N
    public static final IssueField DESCRIPTION = new IssueField(BugzillaAttribute.LONG_DESC.getKey(), "LBL_DESCRIPTION"); // NOI18N
    public static final IssueField CREATION = new IssueField(TaskAttribute.DATE_CREATION, "LBL_CREATION"); // NOI18N
    public static final IssueField CC = new IssueField(BugzillaAttribute.CC.getKey(), "LBL_CC"); // NOI18N
    public static final IssueField MODIFICATION = new IssueField(TaskAttribute.DATE_MODIFICATION, null);
    public static final IssueField NEWCC = new IssueField(BugzillaAttribute.NEWCC.getKey(), null);
    public static final IssueField REMOVECC = new IssueField(BugzillaAttribute.REMOVECC.getKey(), null);
    public static final IssueField COMMENT_COUNT = new IssueField(TaskAttribute.TYPE_COMMENT, null, false);
    public static final IssueField ATTACHEMENT_COUNT = new IssueField(TaskAttribute.TYPE_ATTACHMENT, null, false);
    public static final IssueField ACTUAL_TIME = new IssueField(BugzillaAttribute.ACTUAL_TIME.getKey(), "LBL_ACTUAL_TIME", true);
    public static final IssueField WORK_TIME = new IssueField(BugzillaAttribute.WORK_TIME.getKey(), "LBL_WORK_TIME", true);
    public static final IssueField REMAINING_TIME = new IssueField(BugzillaAttribute.REMAINING_TIME.getKey(), "LBL_REMAINING_TIME", true);
    public static final IssueField ESTIMATED_TIME = new IssueField(BugzillaAttribute.ESTIMATED_TIME.getKey(), "LBL_DEADLINE", true);
    public static final IssueField DEADLINE = new IssueField(BugzillaAttribute.DEADLINE.getKey(), null, true);
    public static final IssueField COMMENT = new IssueField(TaskAttribute.COMMENT_NEW, null, true);
    public static final IssueField DUPLICATE_ID = new IssueField(BugzillaAttribute.DUP_ID.getKey(), "LBL_DUPLICATE_ID", true);
    public static final IssueField REASSIGN_TO_DEFAULT = new IssueField(BugzillaAttribute.SET_DEFAULT_ASSIGNEE.getKey(), null, true);
    public static final IssueField NB_ATTACH_IDE_LOG = new IssueField("nb.attachMessageLog", "LBL_ATTACH_IDE_LOG"); //NOI18N
    public static final IssueField NB_NEW_ATTACHMENTS = new IssueField(AbstractNbTaskWrapper.NEW_ATTACHMENT_ATTRIBUTE_ID, "LBL_NEW_ATTACHMENTS", false); //NOI18N

    private final String key;
    private final String displayNameKey;
    private boolean singleAttribute;

    IssueField(String key, String displayNameKey) {
        this(key, displayNameKey, true);
    }

    IssueField(String key, String displayNameKey, boolean singleAttribute) {
        this.key = key;
        this.singleAttribute = singleAttribute;
        this.displayNameKey = displayNameKey;
    }

    public String getKey() {
        return key;
    }

    public boolean isSingleAttribute() {
        return singleAttribute;
    }

    public boolean isReadOnly() {
        return !singleAttribute;
    }

    public String getDisplayName() {
        assert displayNameKey != null; // shouldn't be called for a field with a null display name
        return NbBundle.getMessage(IssueField.class, displayNameKey);
    }

}
