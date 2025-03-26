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
package org.netbeans.modules.cloud.oracle.actions;

import com.oracle.bmc.artifacts.ArtifactsClient;
import com.oracle.bmc.artifacts.model.ContainerRepository;
import com.oracle.bmc.artifacts.requests.CreateContainerRepositoryRequest;
import com.oracle.bmc.artifacts.responses.CreateContainerRepositoryResponse;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.developer.ContainerRepositoryItem;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.requests.OCIItemCreationDetails;

/**
 *
 * @author Dusan Petrovic
 */
public class CreateContainerRepositoryCommand extends CreateResourceCommand<ContainerRepositoryItem> {
    
    @Override
    ContainerRepositoryItem callCreate(OCIItemCreationDetails itemCreator) {
        ArtifactsClient client = OCIManager.getDefault().getActiveProfile(itemCreator.getCompartment()).newClient(ArtifactsClient.class);
        
        CreateContainerRepositoryRequest request = (CreateContainerRepositoryRequest) itemCreator.getRequest();
        CreateContainerRepositoryResponse response = client.createContainerRepository(request);
        ContainerRepository res = response.getContainerRepository();

        return new ContainerRepositoryItem(
                OCID.of(res.getId(), "ContainerRepository"), //NOI18N
                res.getCompartmentId(),
                res.getDisplayName(),
                itemCreator.getCompartment().getRegionCode(),
                res.getNamespace(),
                res.getIsPublic(), 
                res.getImageCount(),
                itemCreator.getCompartment().getTenancyId()
        );
    }
    
}
