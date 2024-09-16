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
import com.oracle.bmc.devops.model.DeployArtifactSummary;
import com.oracle.bmc.devops.requests.ListDeployArtifactsRequest;
import com.oracle.bmc.devops.responses.ListDeployArtifactsResponse;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.netbeans.modules.cloud.oracle.ChildrenProvider;
import org.netbeans.modules.cloud.oracle.NodeProvider;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.OCINode;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "DeployArtifacts=Artifacts",})
public class DeployArtifactNode extends OCINode {

    private static final String ICON = "org/netbeans/modules/cloud/oracle/resources/deploy_artifact.svg"; // NOI18N

    public DeployArtifactNode(OCIItem item) {
        super(item, Children.LEAF);
        setIconBaseWithExtension(ICON);
    }

    public static NodeProvider<DeployArtifactItem> createNode() {
        return DeployArtifactNode::new;
    }

    public static ChildrenProvider.SessionAware<DevopsProjectItem, DeployArtifactItem.DeployArtifactFolder> listDeployArtifacts() {
        return (project, session) -> {
            try ( DevopsClient client = new DevopsClient(OCIManager.getDefault().getConfigProvider())) {
                ListDeployArtifactsRequest request = ListDeployArtifactsRequest.builder()
                        .projectId(project.getKey().getValue()).build();
                ListDeployArtifactsResponse response = client.listDeployArtifacts(request);
                List<DeployArtifactSummary> projects = response.getDeployArtifactCollection().getItems();

                String tenancyId = session.getTenancy().isPresent() ?
                        session.getTenancy().get().getKey().getValue() : null;
                String regionCode = session.getRegion().getRegionCode();

                return Collections.singletonList(
                        new DeployArtifactItem.DeployArtifactFolder(OCID.of(project.getKey().getValue(), "DeployArtifactFolder"),
                                project.getCompartmentId(),
                                Bundle.DeployArtifacts(),
                                projects.stream()
                                        .map(p -> new DeployArtifactItem(
                                                OCID.of(p.getId(), "DeployArtifact"),
                                                project.getCompartmentId(),
                                                p.getDisplayName(),
                                                tenancyId,
                                                regionCode))
                                        .collect(Collectors.toList()),
                                tenancyId,
                                regionCode)
                );
            }
        };
    }

    public static class DeployArtifactFolderNode extends OCINode {

        private static final String ICON = "org/netbeans/modules/cloud/oracle/resources/deploy_artifact_folder.svg"; // NOI18N

        public DeployArtifactFolderNode(DeployArtifactItem.DeployArtifactFolder folder) {
            super(folder);
            setIconBaseWithExtension(ICON);
        }
    }

    public static NodeProvider<DeployArtifactItem.DeployArtifactFolder> createFolderNode() {
        return DeployArtifactFolderNode::new;
    }

    public static ChildrenProvider<DeployArtifactItem.DeployArtifactFolder, DeployArtifactItem> expandRepositories() {
        return repositories -> repositories.getArtidfacts();
    }

}
