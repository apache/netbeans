/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
