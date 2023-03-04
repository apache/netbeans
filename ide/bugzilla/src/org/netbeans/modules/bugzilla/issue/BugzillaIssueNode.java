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

package org.netbeans.modules.bugzilla.issue;

import java.util.List;
import org.netbeans.modules.bugtracking.issuetable.IssueNode;
import org.netbeans.modules.bugtracking.commons.NBBugzillaUtils;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.BugzillaConnector;
import org.netbeans.modules.bugzilla.repository.BugzillaConfiguration;
import org.netbeans.modules.bugzilla.repository.IssueField;
import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaIssueNode extends IssueNode<BugzillaIssue> {
    public BugzillaIssueNode(BugzillaIssue issue) {
        super(BugzillaConnector.ID,
              issue.getRepository().getID(),
              issue, 
              Bugzilla.getInstance().getIssueProvider(), 
              Bugzilla.getInstance().getStatusProvider(), 
              Bugzilla.getInstance().getChangesProvider());
    }

    BugzillaIssue getBugzillaIssue() {
        return getIssueData();
    }

    @Override
    protected Property<?>[] getProperties() {
        return new Property<?>[] {
            new IDProperty(),
            NBBugzillaUtils.isNbRepository(getBugzillaIssue().getRepository().getUrl()) 
                ? new IssueTypeProperty()
                : new SeverityProperty(),
            new PriorityProperty(),
            new BugzillaFieldProperty(BugzillaIssue.LABEL_NAME_STATUS, IssueField.STATUS, "CTL_Issue_Status_Title", "CTL_Issue_Status_Desc"), // NOI18N
            new BugzillaFieldProperty(BugzillaIssue.LABEL_NAME_ASSIGNED_TO, IssueField.ASSIGNED_TO, "CTL_Issue_Assigned_Title", "CTL_Issue_Status_Desc"), // NOI18N
            new ResolutionProperty(),
            new SummaryProperty(), 
            new ModificationProperty(),
            new BugzillaFieldProperty(BugzillaIssue.LABEL_NAME_PRODUCT, IssueField.PRODUCT, "CTL_Issue_Product_Title", "CTL_Issue_Product_Desc"), // NOI18N
            new BugzillaFieldProperty(BugzillaIssue.LABEL_NAME_COMPONENT, IssueField.COMPONENT, "CTL_Issue_Component_Title", "CTL_Issue_Component_Desc"), // NOI18N
            new BugzillaFieldProperty(BugzillaIssue.LABEL_NAME_VERSION, IssueField.VERSION, "CTL_Issue_Version_Title", "CTL_Issue_Version_Desc"), // NOI18N
            new BugzillaFieldProperty(BugzillaIssue.LABEL_NAME_OS, IssueField.OS, "CTL_Issue_OS_Title", "CTL_Issue_OS_Desc"), // NOI18N
            new BugzillaFieldProperty(BugzillaIssue.LABEL_NAME_PLATFORM, IssueField.PLATFORM, "CTL_Issue_Platform_Title", "CTL_Issue_Platform_Desc"), // NOI18N
            new BugzillaFieldProperty(BugzillaIssue.LABEL_NAME_MILESTONE, IssueField.MILESTONE, "CTL_Issue_Milestone_Title", "CTL_Issue_Milestone_Desc"), // NOI18N
            new BugzillaFieldProperty(BugzillaIssue.LABEL_NAME_REPORTER, IssueField.REPORTER_NAME, "CTL_Issue_Reporter_Title", "CTL_Issue_Reporter_Desc"), // NOI18N
            new BugzillaFieldProperty(BugzillaIssue.LABEL_NAME_QA_CONTACT, IssueField.QA_CONTACT_NAME, "CTL_Issue_QA_Contact_Title", "CTL_Issue_QA_Contact_Desc"), // NOI18N
            new BugzillaFieldProperty(BugzillaIssue.LABEL_NAME_KEYWORDS, IssueField.KEYWORDS, "CTL_Issue_Keywords_Title", "CTL_Issue_Keywords_Desc"), // NOI18N
            new BugzillaFieldProperty(BugzillaIssue.LABEL_NAME_WHITEBOARD, IssueField.WHITEBOARD, "CTL_Issue_Whiteboard_Title", "CTL_Issue_Whiteboard_Desc"), // NOI18N
        };
    };

    @Override
    public void fireDataChanged() {
        super.fireDataChanged();
    }

    private Integer getIssueTypeSortKey(String issueType) {
        BugzillaConfiguration bc = getBugzillaIssue().getRepository().getConfiguration();
        if(bc == null || !bc.isValid()) {
            return null;
        }
        List<String> s = bc.getIssueTypes();
        if(s == null) {
            return null;
        }
        return s.indexOf(issueType);
    }

    private Integer getSeveritySortKey(String severity) {
        BugzillaConfiguration bc = getBugzillaIssue().getRepository().getConfiguration();
        if(bc == null || !bc.isValid()) {
            return null;
        }
        List<String> s = bc.getSeverities();
        if(s == null) {
            return null;
        }
        return s.indexOf(severity);
    }

    private Integer getPrioritySortKey(String priority) {
        BugzillaConfiguration bc = getBugzillaIssue().getRepository().getConfiguration();
        if(bc == null || !bc.isValid()) {
            return null;
        }
        List<String> p = bc.getPriorities();
        if(p == null) {
            return null;
        }
        return p.indexOf(priority);
    }

    private Integer getResolutionSortKey(String resolution) {
        BugzillaConfiguration bc = getBugzillaIssue().getRepository().getConfiguration();
        if(bc == null || !bc.isValid()) {
            return null;
        }
        List<String> r = bc.getResolutions();
        if(r == null) {
            return null;
        }
        return r.indexOf(resolution);
    }

    private class IDProperty extends IssueNode<BugzillaIssue>.IssueProperty<String> {
        public IDProperty() {
            super(BugzillaIssue.LABEL_NAME_ID,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_ID_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_ID_Desc")); // NOI18N
        }
        @Override
        public String getValue() {
            return getBugzillaIssue().getID();
        }
        
        @Override
        public int compareTo(IssueNode<BugzillaIssue>.IssueProperty<String> p) {
            if(p == null) return 1;
            Integer i1 = Integer.parseInt(getIssueData().getID());
            Integer i2 = Integer.parseInt(p.getIssueData().getID());
            return i1.compareTo(i2);
        }

    }

    private class SeverityProperty extends IssueNode<BugzillaIssue>.IssueProperty<String> {
        public SeverityProperty() {
            super(BugzillaIssue.LABEL_NAME_SEVERITY,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Severity_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Severity_Desc")); // NOI18N
        }
        @Override
        public String getValue() {
            return getBugzillaIssue().getRepositoryFieldValue(IssueField.SEVERITY);
        }
        @Override
        public Object getValue(String attributeName) {
            if("sortkey".equals(attributeName)) {                               // NOI18N
                return getSeveritySortKey(getBugzillaIssue().getRepositoryFieldValue(IssueField.SEVERITY));
            } else {
                return super.getValue(attributeName);
            }
        }
    }

    private class IssueTypeProperty extends IssueNode<BugzillaIssue>.IssueProperty<String> {
        public IssueTypeProperty() {
            super(BugzillaIssue.LABEL_NAME_ISSUE_TYPE,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Issue_Type_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Issue_Type_Desc")); // NOI18N
        }
        @Override
        public String getValue() {
            return getBugzillaIssue().getRepositoryFieldValue(IssueField.ISSUE_TYPE);
        }
        @Override
        public Object getValue(String attributeName) {
            if("sortkey".equals(attributeName)) {                               // NOI18N
                return getIssueTypeSortKey(getBugzillaIssue().getRepositoryFieldValue(IssueField.ISSUE_TYPE));
            } else {
                return super.getValue(attributeName);
            }
        }
    }

    public class PriorityProperty extends IssueNode<BugzillaIssue>.IssueProperty<String> {
        public PriorityProperty() {
            super(BugzillaIssue.LABEL_NAME_PRIORITY,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Priority_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Priority_Desc")); // NOI18N
        }
        @Override
        public String getValue() {
            return getBugzillaIssue().getRepositoryFieldValue(IssueField.PRIORITY);
        }
        @Override
        public Object getValue(String attributeName) {
            if("sortkey".equals(attributeName)) {                               // NOI18N
                return getPrioritySortKey(getBugzillaIssue().getRepositoryFieldValue(IssueField.PRIORITY));
            } else {
                return super.getValue(attributeName);
            }
        }
    }

    private class ResolutionProperty extends IssueNode<BugzillaIssue>.IssueProperty<String> {
        public ResolutionProperty() {
            super(BugzillaIssue.LABEL_NAME_RESOLUTION,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Resolution_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_ID_Desc")); // NOI18N
        }
        @Override
        public String getValue() {
            return getBugzillaIssue().getRepositoryFieldValue(IssueField.RESOLUTION);
        }
        @Override
        public Object getValue(String attributeName) {
            if("sortkey".equals(attributeName)) {                               // NOI18N
                return getResolutionSortKey(getBugzillaIssue().getRepositoryFieldValue(IssueField.RESOLUTION));
            } else {
                return super.getValue(attributeName);
            }
        }
    }

    private class ModificationProperty extends IssueNode<BugzillaIssue>.IssueProperty<String> {
        public ModificationProperty() {
            super(BugzillaIssue.LABEL_NAME_MODIFICATION,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Modification_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Modification_Desc")); // NOI18N
        }
        @Override
        public String getValue() {
            return getBugzillaIssue().getRepositoryFieldValue(IssueField.MODIFICATION);
        }
        @Override
        public int compareTo(IssueNode<BugzillaIssue>.IssueProperty<String> p) {
            if(p == null) return 1;
            // XXX sort as date
            String s1 = getBugzillaIssue().getRepositoryFieldValue(IssueField.MODIFICATION);
            String s2 = p.getIssueData().getRepositoryFieldValue(IssueField.MODIFICATION);
            return s1.compareTo(s2);
        }
    }

    private class BugzillaFieldProperty extends IssueProperty<String> {
        private final IssueField field;
        public BugzillaFieldProperty(String fieldLabel, IssueField f, String titleProp, String descProp) {
            super(fieldLabel,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, titleProp), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, descProp)); // NOI18N
            this.field = f;
        }
        @Override
        public String getValue() {
            return getBugzillaIssue().getRepositoryFieldValue(field);
        }
        @Override
        public int compareTo(IssueNode<BugzillaIssue>.IssueProperty<String> p) {
            if(p == null) return 1;
            String s1 = getBugzillaIssue().getRepositoryFieldValue(field);
            String s2 = p.getIssueData().getRepositoryFieldValue(field);
            return s1.compareTo(s2);
        }
    }
}
