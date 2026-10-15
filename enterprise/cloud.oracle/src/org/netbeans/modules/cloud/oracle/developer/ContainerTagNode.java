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
import com.oracle.bmc.artifacts.requests.DeleteContainerImageRequest;
import com.oracle.bmc.artifacts.requests.ListContainerImagesRequest;
import com.oracle.bmc.artifacts.responses.DeleteContainerImageResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.Action;
import org.netbeans.modules.cloud.oracle.ChildrenProvider;
import org.netbeans.modules.cloud.oracle.NodeProvider;
import static org.netbeans.modules.cloud.oracle.NotificationUtils.confirmAction;
import static org.netbeans.modules.cloud.oracle.NotificationUtils.showErrorMessage;
import static org.netbeans.modules.cloud.oracle.NotificationUtils.showMessage;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.OCINode;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.openide.actions.DeleteAction;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "ContainerTagDesc=Pull URL: {0}\nVersion: {1}\nDigest: {2}",
    "# {0} - [OCIItem name]",
    "MSG_ConfirmDeleteAction=Are you sure that you want to delete {0}.",
    "# {0} - [OCIItem name]",
    "MSG_DeleteActionFailed=Failed to delete {0}.",
    "# {0} - [OCIItem name]",
    "MSG_DeleteActionSuccess=Successfully deleted {0}."
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
    
    @Override
    public Action[] getActions(boolean context) {
        Action[] actions = super.getActions(context);
        List<Action> actionList = new ArrayList<>(Arrays.asList(actions));
        actionList.add(SystemAction.get(DeleteAction.class));
        return actionList.toArray(Action[]::new);
    }
    
    @Override
    public boolean canDestroy() {
        return true;
    }
    
    @Override
    public void destroy() throws IOException {
        RequestProcessor.getDefault().post(() -> {
            if (!confirmAction(Bundle.MSG_ConfirmDeleteAction(this.getName()))) {
                return;
            }
            ArtifactsClient client = OCIManager.getDefault().getActiveProfile(getItem()).newClient(ArtifactsClient.class);
            DeleteContainerImageRequest request = DeleteContainerImageRequest.builder()
                    .imageId(this.getItem().getKey().getValue())
                    .build();
            DeleteContainerImageResponse response;
            
            try {
                response = client.deleteContainerImage(request);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                showErrorMessage(Bundle.MSG_DeleteActionFailed(this.getName()));
                return;
            }
            if (response.get__httpStatusCode__() != 204) {
                showErrorMessage(Bundle.MSG_DeleteActionFailed(this.getName()));
                return;
            }
            
            if (this.getParentNode() instanceof OCINode) {
                ((OCINode)this.getParentNode()).refresh();
            }
            showMessage(Bundle.MSG_DeleteActionSuccess(this.getName()));
        });
        
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

            String tenancyId = session.getTenancy().isPresent() ? session.getTenancy().get().getKey().getValue() : null;

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
                            d.getDigest().trim(),
                            tenancyId
                    ))
                    .collect(Collectors.toList());
        };
    }
    
}
