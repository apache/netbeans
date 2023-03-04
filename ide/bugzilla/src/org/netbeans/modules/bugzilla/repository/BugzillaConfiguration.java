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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCustomField;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaVersion;
import org.eclipse.mylyn.internal.bugzilla.core.RepositoryConfiguration;
import org.netbeans.modules.bugzilla.commands.GetConfigurationCommand;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaConfiguration {

    // XXX simplify this. no need to hold this.
    // its cached in bugzillacoreplugin - get it from there.
    private RepositoryConfiguration rc;

    public synchronized void initialize(BugzillaRepository repository, boolean forceRefresh) {
        this.rc = getRepositoryConfiguration(repository, forceRefresh);
    }

    protected RepositoryConfiguration getRepositoryConfiguration(final BugzillaRepository repository, final boolean forceRefresh) {
        GetConfigurationCommand cmd = new GetConfigurationCommand(forceRefresh, repository);
        repository.getExecutor().execute(cmd, true, false);
        return cmd.getConf();
    }

    public boolean isValid() {
        return rc != null;
    }

    /**
     * Returns all products defined in the given repository
     *
     * @param repository
     * @return
     */
    public List<String> getProducts() {
        if(rc == null) {
            return Collections.emptyList();
        }
        return rc.getProducts();
    }

    /**
     * Returns the componets for the given product or all known components if product is null
     *
     * @param repository
     * @param product
     * @return list of components
     */
    public List<String> getComponents(String product) {
        if(rc == null) {
            return Collections.emptyList();
        }
        if(product == null) {
            return rc.getComponents();
        } else {
            return rc. getComponents(product);
        }
    }

    /**
     * Returns all resolutions defined in the given repository
     *
     * @param repository
     * @return
     */
    public List<String> getResolutions() {
        if(rc == null) {
            return Collections.emptyList();
        }
        return rc.getResolutions();
    }

    /**
     * Returns versiones defined for the given product or all available versions if product is null
     *
     * @param repository
     * @param product
     * @return
     */
    public List<String> getVersions(String product) {
        if(rc == null) {
            return Collections.emptyList();
        }
        if(product == null) {
            return rc.getVersions();
        } else {
            return rc.getVersions(product);
        }
    }

    /**
     * Returns all status defined in the given repository
     * @param repository
     * @return
     */
    public List<String> getStatusValues()  {
        if(rc == null) {
            return Collections.emptyList();
        }
        List<String> ret = new LinkedList<String>(rc.getOpenStatusValues());
        ret.addAll(rc.getClosedStatusValues());
        return ret;
    }

    /**
     * Returns all open statuses defined in the given repository.
     * @param repository
     * @return all open statuses defined in the given repository.
     */
    public List<String> getOpenStatusValues()  {
        if(rc == null) {
            return Collections.emptyList();
        }
        return rc.getOpenStatusValues();
    }

    /**
     * Returns all priorities defined in the given repository
     * @param repository
     * @return
     */
    public List<String> getPriorities()  {
        if(rc == null) {
            return Collections.emptyList();
        }
        return rc.getPriorities();
    }

    /**
     * Returns all keywords defined in the given repository
     * @param repository
     * @return
     */
    public List<String> getKeywords() {
        if(rc == null) {
            return Collections.emptyList();
        }
        return rc.getKeywords();
    }

    /**
     * Returns all platforms defined in the given repository
     * @param repository
     * @return
     */
    public List<String> getPlatforms() {
        if(rc == null) {
            return Collections.emptyList();
        }
        return rc.getPlatforms();
    }

    /**
     * Returns all operating systems defined in the given repository
     * @param repository
     * @return
     */
    public List<String> getOSs() {
        if(rc == null) {
            return Collections.emptyList();
        }
        return rc.getOSs();
    }

    /**
     * Returns all severities defined in the given repository
     * @param repository
     * @return
     */
    public List<String> getSeverities() {
        if(rc == null) {
            return Collections.emptyList();
        }
        return rc.getSeverities();
    }

    /**
     * Returns all issue types defined in the given repository.
     * WARNING - works only for the bugzilla hosting netbeans
     * 
     * @param repository
     * @return
     */
    public List<String> getIssueTypes() {
        if(rc == null) {
            return Collections.emptyList();
        }
        List<BugzillaCustomField> fields = rc.getCustomFields();
        for (BugzillaCustomField field : fields) {
            if(field.getName().equals("cf_bug_type")) {                          // NOI18N
                return field.getOptions();
            }
        }
        return Collections.emptyList();
    }

    /**
     * Returns all custom fields defined in the given repository
     * @param repository
     * @return
     */
    public List<BugzillaCustomField> getCustomFields() {
        if(rc == null) {
            return Collections.emptyList();
        }
        return rc.getCustomFields();
    }

    /**
     * Returns target milestones defined for the given product or all available
     * milestones if product is null
     *
     * @param repository
     * @param product
     * @return
     */
    public List<String> getTargetMilestones(String product) {
        if(rc == null) {
            return Collections.emptyList();
        }
        if(product == null) {
            return rc.getTargetMilestones();
        } else {
            return rc.getTargetMilestones(product);
        }
    }

    /**
     * Returns the bugzilla repositories version 
     * @return
     */
    public BugzillaVersion getInstalledVersion() {
        if(rc == null) {
            return null;
        }
        return rc.getInstallVersion();
    }

    private List<IssueField> issueFields;
    public List<IssueField> getFields() {
        if (issueFields == null) {
            List<IssueField> fields = new ArrayList<IssueField>(40);
            fields.add(IssueField.SUMMARY);
            fields.add(IssueField.WHITEBOARD);
            fields.add(IssueField.STATUS);
            fields.add(IssueField.PRIORITY);
            fields.add(IssueField.RESOLUTION);
            fields.add(IssueField.PRODUCT);
            fields.add(IssueField.COMPONENT);
            fields.add(IssueField.VERSION);
            fields.add(IssueField.PLATFORM);
            fields.add(IssueField.OS);
            fields.add(IssueField.MILESTONE);
            fields.add(IssueField.REPORTER);
            fields.add(IssueField.REPORTER_NAME);
            fields.add(IssueField.ASSIGNED_TO);
            fields.add(IssueField.ASSIGNED_TO_NAME);
            fields.add(IssueField.QA_CONTACT);
            fields.add(IssueField.QA_CONTACT_NAME);
            fields.add(IssueField.DEPENDS_ON);
            fields.add(IssueField.BLOCKS);
            fields.add(IssueField.URL);
            fields.add(IssueField.KEYWORDS);
            fields.add(IssueField.SEVERITY);
            fields.add(IssueField.ISSUE_TYPE);
            fields.add(IssueField.DESCRIPTION);
            fields.add(IssueField.CREATION);
            fields.add(IssueField.CC);
            fields.add(IssueField.MODIFICATION);
            fields.add(IssueField.NEWCC);
            fields.add(IssueField.REMOVECC);
            fields.add(IssueField.COMMENT_COUNT);
            fields.add(IssueField.ATTACHEMENT_COUNT);
            fields.add(IssueField.ESTIMATED_TIME);
            fields.add(IssueField.WORK_TIME);
            fields.add(IssueField.REMAINING_TIME);
            fields.add(IssueField.DEADLINE);
            
            if(rc != null) {
                // Custom fields
                for (BugzillaCustomField field : rc.getCustomFields()) {
                    fields.add(new CustomIssueField(field));
                }
            }
            issueFields = fields;
        }
        return issueFields;
    }

    public IssueField getField(String key) {
        for (IssueField issueField : getFields()) {
            if(issueField.getKey().equals(key)) {
                return issueField;
            }
        }
        return null;
    }
}
