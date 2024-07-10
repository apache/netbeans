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
import com.oracle.bmc.core.model.Vnic;
import com.oracle.bmc.core.model.VnicAttachment;
import com.oracle.bmc.core.requests.GetVnicRequest;
import com.oracle.bmc.core.requests.ListVnicAttachmentsRequest;
import com.oracle.bmc.core.responses.GetVnicResponse;
import com.oracle.bmc.core.responses.ListVnicAttachmentsResponse;
import java.util.List;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Horvath
 */
public final class ComputeInstanceItem extends OCIItem {
    private String publicIp = null;
    private final RequestProcessor RP = new RequestProcessor();
    
    public ComputeInstanceItem(OCID id, String compartmentId, String name) {
        super(id, compartmentId, name);
    }

    public ComputeInstanceItem() {
        super();
    }

    public String getPublicIp() {
        if (publicIp == null) {
            RP.post(() -> {
                String oldPublicIp = publicIp;
                loadDetails();
                firePropertyChange("publicIp", oldPublicIp, publicIp); //NOI18N
            });
            return "---"; //NOI18N
        }
        return publicIp;
    } 
    
    private void loadDetails() {
        ComputeClient computeClient = ComputeClient.builder()
                .build(OCIManager.getDefault().getActiveProfile().getAuthenticationProvider());
        
        VirtualNetworkClient virtualNetworkClient = VirtualNetworkClient.builder()
                .build(OCIManager.getDefault().getActiveProfile().getAuthenticationProvider());

        
        ListVnicAttachmentsRequest listVnicAttachmentsRequest = ListVnicAttachmentsRequest.builder()
                .compartmentId(getCompartmentId())
                .instanceId(getKey().getValue())
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
                publicIp = vnic.getPublicIp();
                break; 
            }
        }
    }
    
}
