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
package org.netbeans.modules.cloud.oracle.steps;

import com.oracle.bmc.devops.DevopsClient;
import com.oracle.bmc.devops.model.ProjectSummary;
import com.oracle.bmc.devops.requests.ListProjectsRequest;
import com.oracle.bmc.devops.responses.ListProjectsResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.assets.AbstractStep;
import org.netbeans.modules.cloud.oracle.assets.Steps;
import org.netbeans.modules.cloud.oracle.assets.Steps.Values;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentItem;
import org.netbeans.modules.cloud.oracle.devops.DevopsProjectItem;
import org.netbeans.modules.cloud.oracle.devops.DevopsProjectService;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "FetchingDevopsProjects=Fetching DevOps projects",
    "NoDevopsProjects=There are no Devops Projects in selected Compartment",
    "SelectDevopsProject=Select Devops Project"
})
public class DevopsStep extends AbstractStep<DevopsProjectItem> {

    private Map<String, DevopsProjectItem> devopsProjects;
    private DevopsProjectItem selected;

    @Override
    public void prepare(ProgressHandle h, Values values) {
        h.progress(Bundle.FetchingDevopsProjects());
        List<String> devops = DevopsProjectService.getDevopsProjectOcid();
        CompartmentItem compartment = values.getValueForStep(CompartmentStep.class);
        Map<String, DevopsProjectItem> allProjectsInCompartment = getDevopsProjects(compartment.getKey().getValue());
        Map<String, DevopsProjectItem> filtered = allProjectsInCompartment.entrySet().stream().filter(e -> devops.contains(e.getValue().getKey().getValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (!filtered.isEmpty()) {
            devopsProjects = filtered;
        } else {
            devopsProjects = allProjectsInCompartment;
        }
        if (devopsProjects.size() == 1) {
            selected = devopsProjects.values().iterator().next();
        }
    }

    @Override
    public NotifyDescriptor createInput() {
        if (devopsProjects.size() > 1) {
            return Steps.createQuickPick(devopsProjects, Bundle.SelectDevopsProject());
        }
        if (devopsProjects.isEmpty()) {
            return new NotifyDescriptor.QuickPick("", Bundle.NoDevopsProjects(), Collections.emptyList(), false);
        }
        throw new IllegalStateException("No data to create input"); // NOI18N
    }

    @Override
    public boolean onlyOneChoice() {
        return devopsProjects.size() == 1;
    }

    @Override
    public void setValue(String projectName) {
        selected = devopsProjects.get(projectName);
    }

    @Override
    public DevopsProjectItem getValue() {
        return selected;
    }

    protected static Map<String, DevopsProjectItem> getDevopsProjects(String compartmentId) {
        try (DevopsClient client = new DevopsClient(OCIManager.getDefault().getConfigProvider())) {
            ListProjectsRequest request = ListProjectsRequest.builder().compartmentId(compartmentId).build();
            ListProjectsResponse response = client.listProjects(request);
            List<ProjectSummary> projects = response.getProjectCollection().getItems();
            for (ProjectSummary project : projects) {
                project.getNotificationConfig().getTopicId();
            }
            return projects.stream().map(p -> new DevopsProjectItem(OCID.of(p.getId(), "DevopsProject"), // NOI18N
                    compartmentId, p.getName())).collect(Collectors.toMap(DevopsProjectItem::getName, Function.identity()));
        }
    }

}
