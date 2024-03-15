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
package org.netbeans.modules.cloud.oracle.devops;


import com.oracle.bmc.devops.DevopsClient;
import com.oracle.bmc.devops.model.ProjectSummary;
import com.oracle.bmc.devops.requests.ListProjectsRequest;
import com.oracle.bmc.devops.responses.ListProjectsResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.netbeans.modules.cloud.oracle.ChildrenProvider;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.netbeans.modules.cloud.oracle.NodeProvider;
import org.netbeans.modules.cloud.oracle.OCINode;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentItem;

/**
 *
 * @author Jan Horvath
 */

public class DevopsProjectNode extends OCINode {
    
    private static final String DB_ICON = "org/netbeans/modules/cloud/oracle/resources/devops_project.svg"; // NOI18N
    
    public DevopsProjectNode(OCIItem item) {
        super(item);
        setName(item.getName()); 
        setDisplayName(item.getName());
        setIconBaseWithExtension(DB_ICON);
        setShortDescription(item.getDescription());
    }
    
    public static NodeProvider<OCIItem> createNode() {
        return DevopsProjectNode::new;
    }
   
    public static ChildrenProvider.SessionAware<CompartmentItem, DevopsProjectItem> listDevopsProjects() {
        return (compartmentId, session) -> {
            try (
                DevopsClient client = session.newClient(DevopsClient.class)) {
                ListProjectsRequest request = ListProjectsRequest.builder().compartmentId(compartmentId.getKey().getValue()).build();
                ListProjectsResponse response = client.listProjects(request);

                List<ProjectSummary> projects = response.getProjectCollection().getItems();
                for (ProjectSummary project : projects) {
                    project.getNotificationConfig().getTopicId();
                    
                }
                return projects.stream().map(p -> new DevopsProjectItem(OCID.of(p.getId(), "DevopsProject"), 
                        p.getName())).collect(Collectors.toList());
            }
        };
    }
    
}