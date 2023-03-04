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
package org.netbeans.modules.javaee.project.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.netbeans.api.whitelist.index.WhiteListIndex;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.whitelist.WhiteListQueryImplementation;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Support for propagating whitelist changes to subprojects.
 */
public abstract class WhiteListUpdater {

    private static final RequestProcessor rp = new RequestProcessor();
    protected Project project;
    private String lastWhiteList;

    public WhiteListUpdater(Project project) {
        this.project = project;
        // initialize lastWhiteList lazily to avoid 232272
        // lastWhiteList = getServerWhiteList();
        addSettingListener();
    }

    /**
     * Adds listener on project setting changes. 
     * <p>
     * 
     * Implementors need to take care about two event types:<br/>
     * 
     * 1) When server instance ID is changed, {@link #checkWhiteLists()} should be called<br/>
     * 2) When javac classpath is changed, {@link #updateWhitelist(java.lang.String, java.lang.String)} should be called<br/>
     */
    protected abstract void addSettingListener();

    protected void updateWhitelist(final String oldWhiteListId, final String newWhiteListId) {
        rp.post(new Runnable() {
            @Override
            public void run() {
                updateWhitelist(project, oldWhiteListId, newWhiteListId);
            }
        });
    }

    /**
     * When server is changed web project and all its subprojects get enabled
     * whitelist if necessary.
     */
    private void updateWhitelist(Project p, String oldWhiteListId, String newWhiteListId) {
        List<Project> projs = new ArrayList<Project>();
        projs.add(p);
        //mkleint: see subprojectprovider for official contract, maybe classpath should be checked instead? see #210465
        projs.addAll(p.getLookup().lookup(SubprojectProvider.class).getSubprojects());
        for (Project pp : projs) {
            if (oldWhiteListId != null) {
                WhiteListQuery.enableWhiteListInProject(pp, oldWhiteListId, false);
            }
            if (newWhiteListId != null) {
                WhiteListQuery.enableWhiteListInProject(pp, newWhiteListId, true);
            }
        }
    }


    public void checkWhiteLists() {
        String newWhiteList = getServerWhiteList();
        if ((newWhiteList == null && lastWhiteList == null) ||
            (newWhiteList != null && lastWhiteList != null && newWhiteList.equals(lastWhiteList))) {
            return;
        }
        updateWhitelist(lastWhiteList, newWhiteList);
        lastWhiteList = newWhiteList;
    }

    protected String getServerWhiteList() {
        String servInstID = JavaEEProjectSettings.getServerInstanceID(project);
        if (servInstID != null) {
            J2eePlatform platform;
            try {
                platform = Deployment.getDefault().getServerInstance(servInstID).getJ2eePlatform();
                WhiteListQueryImplementation.UserSelectable sw = platform.getLookup().lookup(WhiteListQueryImplementation.UserSelectable.class);
                if (sw != null) {
                    return sw.getId();
                }
            } catch (InstanceRemovedException ex) {
                //Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    /**
     * A helper which which is here only for lack of better place - shows dialog whether
     * deployment should continue when whitelist violations are present in project.
     * @param p
     * @return 
     */
    @NbBundle.Messages({
            "MSG_WhitelistViolations=Whitelist violations were detected in project being deployed. Are you sure you want to continue deployment?",
            "MSG_Dialog_Title=Continue deployment?"
    })
    public static boolean isWhitelistViolated(Project p) {
        SourceGroup[] sgs = ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sgs.length == 0) {
            return false;
        }
        Collection problems = WhiteListIndex.getDefault().
                getWhiteListViolations(sgs[0].getRootFolder(), null, "oracle");
        if (problems.size() > 0) {
            if (DialogDisplayer.getDefault().notify(
                    new Confirmation(Bundle.MSG_WhitelistViolations(), Bundle.MSG_Dialog_Title(), NotifyDescriptor.YES_NO_OPTION)) != NotifyDescriptor.YES_OPTION) {
                return true;
            }
        }
        return false;
    }

}
