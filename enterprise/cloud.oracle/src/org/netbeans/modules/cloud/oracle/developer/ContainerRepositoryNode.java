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
package org.netbeans.modules.cloud.oracle.developer;

import com.oracle.bmc.artifacts.ArtifactsClient;
import com.oracle.bmc.artifacts.model.ContainerRepository;
import com.oracle.bmc.artifacts.requests.GetContainerRepositoryRequest;
import com.oracle.bmc.artifacts.requests.ListContainerRepositoriesRequest;
import com.oracle.bmc.artifacts.responses.GetContainerRepositoryResponse;
import java.util.stream.Collectors;
import org.netbeans.modules.cloud.oracle.ChildrenProvider;
import org.netbeans.modules.cloud.oracle.NodeProvider;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.OCINode;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentItem;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "ContainerRepositoryDesc=Container Repository: {0}\nURL: {1}"
})
public class ContainerRepositoryNode extends OCINode {
    private static final String CONTAINER_REPOSITORY_ICON = "org/netbeans/modules/cloud/oracle/resources/containerrepository.svg"; // NOI18N

    public ContainerRepositoryNode(ContainerRepositoryItem instance) {
        super(instance);
        setName(instance.getName());
        setDisplayName(instance.getName());
        setIconBaseWithExtension(CONTAINER_REPOSITORY_ICON);
        setShortDescription(Bundle.ContainerRepositoryDesc(instance.getName(), instance.getUrl()));
    }

    public static NodeProvider<ContainerRepositoryItem> createNode() {
        return ContainerRepositoryNode::new;
    }
    
    @Override
    public void update(OCIItem item) {
        ContainerRepositoryItem orig = (ContainerRepositoryItem) item;
        ArtifactsClient client = OCIManager.getDefault().getActiveProfile().newClient(ArtifactsClient.class);
        GetContainerRepositoryRequest listContainerRepositoriesRequest = GetContainerRepositoryRequest.builder()
                .repositoryId(item.getKey().getValue())
                .build();
        
        GetContainerRepositoryResponse response = client.getContainerRepository(listContainerRepositoriesRequest);
        ContainerRepository cr = response.getContainerRepository();
        orig.setImageCount(cr.getImageCount());
                
    }

    /**
     * Retrieves list of Vaults belonging to a given Compartment.
     *
     * @return Returns {@code ChildrenProvider} which fetches List of
     * {@code BucketItem} for given {@code CompartmentItem}
     */
    public static ChildrenProvider.SessionAware<CompartmentItem, ContainerRepositoryItem> getContainerRepositories() {
        return (compartmentId, session) -> {
            ArtifactsClient client = session.newClient(ArtifactsClient.class);

            ListContainerRepositoriesRequest listContainerRepositoriesRequest = ListContainerRepositoriesRequest.builder()
                    .compartmentId(compartmentId.getKey().getValue())
                    .build();

            String tenancyId = session.getTenancy().isPresent() ? session.getTenancy().get().getKey().getValue() : null;

            return client.listContainerRepositories(listContainerRepositoriesRequest)
                    .getContainerRepositoryCollection()
                    .getItems()
                    .stream()
                    .map(d -> new ContainerRepositoryItem(
                        OCID.of(d.getId(), "ContainerRepository"), //NOI18N
                            compartmentId.getKey().getValue(),
                            d.getDisplayName(),
                            session.getRegion().getRegionCode(),
                            d.getNamespace(),
                            d.getIsPublic(), 
                            d.getImageCount(),
                            tenancyId
                    ))
                    .collect(Collectors.toList());
        };
    }
    
}
