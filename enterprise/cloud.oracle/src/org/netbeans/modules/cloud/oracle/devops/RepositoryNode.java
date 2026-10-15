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
import com.oracle.bmc.devops.model.RepositorySummary;
import com.oracle.bmc.devops.requests.ListRepositoriesRequest;
import com.oracle.bmc.devops.responses.ListRepositoriesResponse;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.netbeans.modules.cloud.oracle.ChildrenProvider;
import org.netbeans.modules.cloud.oracle.NodeProvider;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.OCINode;
import org.netbeans.modules.cloud.oracle.devops.RepositoryItem.RepositoryFolder;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "Repositories=Code Repositories"})
public class RepositoryNode extends OCINode {

    private static final String ICON = "org/netbeans/modules/cloud/oracle/resources/repository.svg"; // NOI18N

    public RepositoryNode(OCIItem item) {
        super(item, Children.LEAF);
        setIconBaseWithExtension(ICON);
    }

    public static NodeProvider<RepositoryItem> createNode() {
        return RepositoryNode::new;
    }

    public static ChildrenProvider.SessionAware<DevopsProjectItem, RepositoryFolder> listRepositories() {
        return (project, session) -> {
            try ( DevopsClient client = session.newClient(DevopsClient.class)) {
                ListRepositoriesRequest listRepositoriesRequest = ListRepositoriesRequest.builder()
                        .projectId(project.getKey().getValue()).build();
                ListRepositoriesResponse response = client.listRepositories(listRepositoriesRequest);
                List<RepositorySummary> projects = response.getRepositoryCollection().getItems();

                String tenancyId = session.getTenancy().isPresent() ?
                        session.getTenancy().get().getKey().getValue() : null;
                String regionCode = session.getRegion().getRegionCode();

                return Collections.singletonList(
                        new RepositoryFolder(OCID.of(project.getKey().getValue(), "RepositoryFolder"),
                                project.getCompartmentId(),
                                Bundle.Repositories(),
                                projects.stream()
                                        .map(p -> new RepositoryItem(
                                                OCID.of(p.getId(), "Repository"),
                                                project.getCompartmentId(),
                                                p.getName(),
                                                tenancyId,
                                                regionCode))
                                        .collect(Collectors.toList()),
                                tenancyId,
                                regionCode)
                );
            }
        };
    }

    public static class RepositoryFolderNode extends OCINode {

        private static final String ICON = "org/netbeans/modules/cloud/oracle/resources/repository_folder.svg"; // NOI18N

        public RepositoryFolderNode(RepositoryItem.RepositoryFolder item) {
            super(item);
            setIconBaseWithExtension(ICON);
        }
    }

    public static NodeProvider<RepositoryFolder> createFolderNode() {
        return RepositoryFolderNode::new;
    }

    public static ChildrenProvider<RepositoryFolder, RepositoryItem> expandRepositories() {
        return repositories -> repositories.getRepositories();
    }
}
