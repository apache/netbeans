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
import com.oracle.bmc.devops.model.BuildRunSummary;
import com.oracle.bmc.devops.requests.ListBuildRunsRequest;
import com.oracle.bmc.devops.responses.ListBuildRunsResponse;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.netbeans.modules.cloud.oracle.ChildrenProvider;
import org.netbeans.modules.cloud.oracle.NodeProvider;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.OCINode;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "BuildRuns=Builds",})
public class BuildRunNode extends OCINode {

    private static final String GREEN = "org/netbeans/modules/cloud/oracle/resources/green_dot.svg"; // NOI18N
    private static final String RED = "org/netbeans/modules/cloud/oracle/resources/red_dot.svg"; // NOI18N
    private static final String YELLOW = "org/netbeans/modules/cloud/oracle/resources/yellow_dot.svg"; // NOI18N

    public BuildRunNode(BuildRunItem item) {
        super(item, Children.LEAF);
        switch(item.getLifecycleState().toLowerCase()) {
            case "succeeded": 
                setIconBaseWithExtension(GREEN);
                break;
            case "failed": 
                setIconBaseWithExtension(RED);
                break;
            default:
                setIconBaseWithExtension(YELLOW);
        }
    }

    public static NodeProvider<BuildRunItem> createNode() {
        return BuildRunNode::new;
    }

    public static ChildrenProvider.SessionAware<DevopsProjectItem, BuildRunFolderItem> listBuildRuns() {
        return (project, session) -> Collections.singletonList(
                new BuildRunFolderItem(
                        OCID.of(project.getKey().getValue(), "BuildRunFolder"),
                        project.getCompartmentId(),
                        Bundle.BuildRuns(),
                        session.getTenancy().isPresent() ? session.getTenancy().get().getKey().getValue() : null,
                        session.getRegion().getRegionCode())
        );
    }
    
    public static ChildrenProvider.SessionAware<BuildRunFolderItem, BuildRunItem> expandBuildRuns() {
        return (project, session) -> {
            try ( DevopsClient client = session.newClient(DevopsClient.class)) {
                ListBuildRunsRequest request = ListBuildRunsRequest.builder()
                        .projectId(project.getKey().getValue())
                        .sortBy(ListBuildRunsRequest.SortBy.TimeCreated)
                        .limit(6)
                        .build();
                ListBuildRunsResponse response = client.listBuildRuns(request);
                List<BuildRunSummary> projects = response.getBuildRunSummaryCollection().getItems();

                String tenancyId = session.getTenancy().isPresent() ?
                        session.getTenancy().get().getKey().getValue() : null;
                String regionCode = session.getRegion().getRegionCode();

                return projects.stream()
                                        .map(p -> new BuildRunItem(
                                                OCID.of(p.getId(), "BuildRun"), 
                                                project.getCompartmentId(),
                                                p.getDisplayName(),
                                                p.getLifecycleState().getValue(),
                                                tenancyId,
                                                regionCode
                                        ))
                                        .collect(Collectors.toList()
                );
            }
        };
    }
    
    public static class BuildRunFolderNode extends OCINode {

        private static final String ICON = "org/netbeans/modules/cloud/oracle/resources/build_run_folder.svg"; // NOI18N

        public BuildRunFolderNode(BuildRunFolderItem folder) {
            super(folder);
            setIconBaseWithExtension(ICON);
        }
        
        public void refresh() {
            ((OCINode) getParentNode()).refresh();
        }
    }

    public static NodeProvider<BuildRunFolderItem> createFolderNode() {
        return BuildRunFolderNode::new;
    }

}
