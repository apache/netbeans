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
package org.netbeans.modules.cloud.oracle.compute;

import com.oracle.bmc.core.ComputeClient;
import com.oracle.bmc.core.VirtualNetworkClient;
import com.oracle.bmc.core.model.Instance;
import com.oracle.bmc.core.model.Vnic;
import com.oracle.bmc.core.model.VnicAttachment;
import com.oracle.bmc.core.requests.GetImageRequest;
import com.oracle.bmc.core.requests.GetVnicRequest;
import com.oracle.bmc.core.requests.ListInstancesRequest;
import com.oracle.bmc.core.requests.ListVnicAttachmentsRequest;
import com.oracle.bmc.core.responses.GetImageResponse;
import com.oracle.bmc.core.responses.GetVnicResponse;
import com.oracle.bmc.core.responses.ListVnicAttachmentsResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.netbeans.modules.cloud.oracle.ChildrenProvider;
import org.netbeans.modules.cloud.oracle.NodeProvider;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.OCINode;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentItem;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "CoputeInstanceDesc=Compute Instance: {0}\nPublic IP: {1}\nUsername: {2}\nProcessor: {3}"
})
public class ComputeInstanceNode extends OCINode {
    private static final String COMPUTE_INSTANCE_ICON = "org/netbeans/modules/cloud/oracle/resources/computeinstance.svg"; // NOI18N

    public ComputeInstanceNode(ComputeInstanceItem instance) {
        super(instance, Children.LEAF);
        setName(instance.getName());
        setDisplayName(instance.getName());
        setIconBaseWithExtension(COMPUTE_INSTANCE_ICON);
        setShortDescription(Bundle.CoputeInstanceDesc(
                instance.getName(), 
                instance.getPublicIp(),
                instance.getUsername(),
                instance.getProcessorDescription()
        ));
    }

    public static NodeProvider<ComputeInstanceItem> createNode() {
        return (instance) -> {
            update(instance);
            return new ComputeInstanceNode(instance);
        };
    }
    
    
    static public void update(ComputeInstanceItem instance) {
        ComputeClient computeClient = ComputeClient.builder()
                .build(OCIManager.getDefault().getActiveProfile(instance).getAuthenticationProvider());
        if (instance.getImageId() != null) {
            GetImageRequest request = GetImageRequest.builder()
                    .imageId(instance.getImageId()).build();
            GetImageResponse response = computeClient.getImage(request);
            String os = response.getImage().getOperatingSystem();
             if (os.contains("Oracle")) { //NOI18N
                instance.setUsername("opc"); //NOI18N
            } else if (os.contains("Ubuntu")) { //NOI18N
                instance.setUsername("ubuntu"); //NOI18N
            } else if (os.contains("CentOS")) { //NOI18N
                instance.setUsername("centos"); //NOI18N
            } else if (os.contains("Debian")) { //NOI18N
                instance.setUsername("debian"); //NOI18N
            } else if (os.contains("Windows")) { //NOI18N
                instance.setUsername("Administrator"); //NOI18N
            } else {
                instance.setUsername("opc"); //NOI18N
            }
        }
         
        VirtualNetworkClient virtualNetworkClient = VirtualNetworkClient.builder()
                .build(OCIManager.getDefault().getActiveProfile(instance).getAuthenticationProvider());

        ListVnicAttachmentsRequest listVnicAttachmentsRequest = ListVnicAttachmentsRequest.builder()
                .compartmentId(instance.getCompartmentId())
                .instanceId(instance.getKey().getValue())
                .build();
        
        ListVnicAttachmentsResponse listVnicAttachmentsResponse = computeClient.listVnicAttachments(listVnicAttachmentsRequest);
        List<VnicAttachment> vnicAttachments = listVnicAttachmentsResponse.getItems();

        for (VnicAttachment vnicAttachment : vnicAttachments) {
            GetVnicRequest getVnicRequest = GetVnicRequest.builder()
                    .vnicId(vnicAttachment.getVnicId())
                    .build();
            GetVnicResponse getVnicResponse = virtualNetworkClient.getVnic(getVnicRequest);
            Vnic vnic = getVnicResponse.getVnic();

            if (vnic.getPublicIp() != null) {
                instance.setPublicId(vnic.getPublicIp());
            }
        }
    }
    
    /**
     * Retrieves list of Vaults belonging to a given Compartment.
     *
     * @return Returns {@code ChildrenProvider} which fetches List of
     * {@code BucketItem} for given {@code CompartmentItem}
     */
    public static ChildrenProvider.SessionAware<CompartmentItem, ComputeInstanceItem> getComputeInstances() {
        return (compartmentId, session) -> {
            final ComputeClient client = session.newClient(ComputeClient.class);

            ListInstancesRequest listInstancesRequest = ListInstancesRequest.builder()
                    .compartmentId(compartmentId.getKey().getValue())
                    .limit(88)
                    .build();

            String tenancyId = session.getTenancy().isPresent() ? session.getTenancy().get().getKey().getValue() : null;
            String regionCode = session.getRegion().getRegionCode();

            return client.listInstances(listInstancesRequest)
                    .getItems()
                    .stream()
                    .filter(c -> !c.getLifecycleState().equals(Instance.LifecycleState.Terminated))
                    .filter(c -> !c.getFreeformTags().containsKey("OKEnodePoolName"))
                    .map(d -> new ComputeInstanceItem(
                        OCID.of(d.getId(), "ComputeInstance"), //NOI18N
                            compartmentId.getKey().getValue(),
                            d.getDisplayName(), 
                            d.getShapeConfig().getProcessorDescription(),
                            d.getImageId(),
                            null,
                            null,
                            tenancyId,
                            regionCode
                    ))
                    .collect(Collectors.toList());
        };
    }
    
}
