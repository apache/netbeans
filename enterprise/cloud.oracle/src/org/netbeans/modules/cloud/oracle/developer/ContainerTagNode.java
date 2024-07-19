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
import com.oracle.bmc.artifacts.requests.ListContainerImagesRequest;
import java.util.stream.Collectors;
import org.netbeans.modules.cloud.oracle.ChildrenProvider;
import org.netbeans.modules.cloud.oracle.NodeProvider;
import org.netbeans.modules.cloud.oracle.OCINode;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "ContainerTagDesc=Pull URL: {0}\nVersion: {1}\nDigest: {2}"
})
public class ContainerTagNode extends OCINode {
    private static final String CONTAINER_TAG_ICON = "org/netbeans/modules/cloud/oracle/resources/containertag.svg"; // NOI18N

    public ContainerTagNode(ContainerTagItem tag) {
        super(tag, Children.LEAF);
        setName(tag.getName());
        setDisplayName(tag.getName());
        setIconBaseWithExtension(CONTAINER_TAG_ICON);
        setShortDescription(Bundle.ContainerTagDesc(tag.getUrl(), tag.getVersion(), tag.getDigest()));
    }

    public static NodeProvider<ContainerTagItem> createNode() {
        return ContainerTagNode::new;
    }

    /**
     * Retrieves list of Vaults belonging to a given Compartment.
     *
     * @return Returns {@code ChildrenProvider} which fetches List of
     * {@code BucketItem} for given {@code CompartmentItem}
     */
    public static ChildrenProvider.SessionAware<ContainerRepositoryItem, ContainerTagItem> getContainerTags() {
        return (containerRepository, session) -> {
            ArtifactsClient client = session.newClient(ArtifactsClient.class);

            ListContainerImagesRequest listContainerImagesRequest  = ListContainerImagesRequest.builder()
                    .compartmentId(containerRepository.getCompartmentId())
                    .repositoryId(containerRepository.getKey().getValue())
                    .build();

            return client.listContainerImages(listContainerImagesRequest)
                    .getContainerImageCollection()
                    .getItems()
                    .stream()
                    .map(d -> new ContainerTagItem(
                            OCID.of(d.getId(), "ContainerTag"), //NOI18N
                            containerRepository.getCompartmentId(),
                            containerRepository.getName(),
                            containerRepository.getRegionCode(),
                            containerRepository.getNamespace(),
                            d.getVersion(),
                            d.getDigest().trim()
                    ))
                    .collect(Collectors.toList());
        };
    }
    
}
