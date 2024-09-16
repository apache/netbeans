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
package org.netbeans.modules.cloud.oracle.vault;

import com.oracle.bmc.vault.VaultsClient;
import com.oracle.bmc.vault.model.ScheduleSecretDeletionDetails;
import com.oracle.bmc.vault.model.Secret;
import com.oracle.bmc.vault.model.SecretSummary.LifecycleState;
import com.oracle.bmc.vault.requests.GetSecretRequest;
import com.oracle.bmc.vault.requests.ListSecretsRequest;
import com.oracle.bmc.vault.requests.ScheduleSecretDeletionRequest;
import com.oracle.bmc.vault.responses.GetSecretResponse;
import com.oracle.bmc.vault.responses.ScheduleSecretDeletionResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import org.netbeans.modules.cloud.oracle.items.OCIItem;
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
    "SecretNodeDesc=Valut Secret: {0}\nLifecycle State: {1}",
    "SecretNodeDeletingDesc=Valut Secret: {0}\nLifecycle State: {1}\nDeletion time: {2}",
    "# {0} - [OCIItem name]",
    "MSG_ConfirmDeleteAction=Are you sure that you want to schedule deletion of {0}",
    "# {0} - [OCIItem name]",
    "MSG_DeleteActionFailed=Failed to schedule deletion of {0}.",
    "# {0} - [OCIItem name]", "# {1} - [Scheduled deletion time]",
    "MSG_DeleteActionSuccess=Successfully scheduled deletion of {0} at {1}."
})
public class SecretNode extends OCINode {
    private static final String SECRET_ICON = "org/netbeans/modules/cloud/oracle/resources/secret.svg"; // NOI18N
    private static final SimpleDateFormat DELETION_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public SecretNode(SecretItem vault) {
        super(vault, Children.LEAF);
        setName(vault.getName());
        setDisplayName(vault.getName());
        setIconBaseWithExtension(SECRET_ICON);
        setShortDescription(
                createShortDescription(
                        vault.getLifecycleState(),
                        vault.getDeletionTime()));
    }
    
    private String createShortDescription(String state, Date deletionTime) {
        if (deletionTime != null) {
            return Bundle.SecretNodeDeletingDesc(this.getItem().getName(), state, formatDateTime(deletionTime));
        }
        return Bundle.SecretNodeDesc(this.getItem().getName(), state);
    }
    
    private String formatDateTime(Date deletionTime) {
        return DELETION_TIME_FORMAT.format(deletionTime);
    }

    public static NodeProvider<SecretItem> createNode() {
        return SecretNode::new;
    }
    
    @Override
    public void update(OCIItem item) {
        SecretItem orig = (SecretItem) item;
        VaultsClient client = OCIManager.getDefault().getActiveProfile().newClient(VaultsClient.class);
        GetSecretRequest request = GetSecretRequest.builder()
                .secretId(orig.getKey().getValue())
                .build();

        GetSecretResponse response = client.getSecret(request);
        Secret secret = response.getSecret();
        orig.setLifecycleState(secret.getLifecycleState().getValue());
        orig.setDeletionTime(secret.getTimeOfDeletion());
        setShortDescription(
                createShortDescription(
                        orig.getLifecycleState(),
                        orig.getDeletionTime()));
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
        return ((SecretItem)this.getItem()).getLifecycleState().equals(LifecycleState.Active.getValue());
    }

    @Override
    public void destroy() throws IOException {
        RequestProcessor.getDefault().post(() -> {
            if (!confirmAction(Bundle.MSG_ConfirmDeleteAction(this.getName()))) {
                return;
            }
            
            VaultsClient client = OCIManager.getDefault().getActiveSession().newClient(VaultsClient.class);
            Date deletionTime = getDeletionTime();
            ScheduleSecretDeletionRequest request = buildScheduleDeletionRequest(deletionTime);
            ScheduleSecretDeletionResponse response;
            
            try {
                response = client.scheduleSecretDeletion(request);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                showErrorMessage(Bundle.MSG_DeleteActionFailed(this.getName()));
                return;
            }
            
            if (response.get__httpStatusCode__() != 200) {
                showErrorMessage(Bundle.MSG_DeleteActionFailed(this.getName()));
                return;
            }
            
            updateToPendingState(deletionTime);      
            showMessage(Bundle.MSG_DeleteActionSuccess(
                                this.getName(),
                                this.formatDateTime(deletionTime)));
        });
    }
    
    private ScheduleSecretDeletionRequest buildScheduleDeletionRequest(Date deletionTime) {
        ScheduleSecretDeletionDetails scheduleSecretDeletionDetails = ScheduleSecretDeletionDetails.builder()
                    .timeOfDeletion(deletionTime)
                    .build();

        return ScheduleSecretDeletionRequest.builder()
                    .secretId(this.getItem().getKey().getValue())
                    .scheduleSecretDeletionDetails(scheduleSecretDeletionDetails)
                    .build();
    }

    private void updateToPendingState(Date deletionTime) {
        ((SecretItem) this.getItem()).setLifecycleState(LifecycleState.PendingDeletion.getValue());
        setShortDescription(
                createShortDescription(
                        LifecycleState.PendingDeletion.getValue(),
                        deletionTime));
    }
    
    private Date getDeletionTime() {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).plusHours(1);
        return Date.from(tomorrow.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * Retrieves list of Secrets belonging to a given Vault.
     *
     * @@return Returns {@code ChildrenProvider} which fetches List of {@code SecretItem} for given {@code VaultItem}
     */
    public static ChildrenProvider.SessionAware<VaultItem, SecretItem> getSecrets() {
        return (vault, session) -> {
            VaultsClient client = session.newClient(VaultsClient.class);
            
            ListSecretsRequest listSecretsRequest = ListSecretsRequest.builder()
                    .compartmentId(vault.getCompartmentId())
                    .vaultId(vault.getKey().getValue())
                    .limit(88)
                    .build();

            String tenancyId = session.getTenancy().isPresent() ? session.getTenancy().get().getKey().getValue() : null;
            String regionCode = session.getRegion().getRegionCode();

            return client.listSecrets(listSecretsRequest)
                    .getItems()
                    .stream()
                    .map(d -> new SecretItem(
                                OCID.of(d.getId(), "Vault/Secret"), //NOI18N
                                d.getCompartmentId(),
                                d.getSecretName(),
                                d.getLifecycleState().getValue(),
                                d.getTimeOfDeletion(),
                                tenancyId,
                                regionCode)
                    )
                    .collect(Collectors.toList());
        };
    }

}
