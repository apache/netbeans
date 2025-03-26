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
package org.netbeans.modules.cloud.oracle.policy;

import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.model.CreatePolicyDetails;
import com.oracle.bmc.identity.model.Policy;
import com.oracle.bmc.identity.model.UpdatePolicyDetails;
import com.oracle.bmc.identity.requests.CreatePolicyRequest;
import com.oracle.bmc.identity.requests.ListPoliciesRequest;
import com.oracle.bmc.identity.requests.UpdatePolicyRequest;
import com.oracle.bmc.identity.responses.CreatePolicyResponse;
import com.oracle.bmc.identity.responses.ListPoliciesResponse;
import com.oracle.bmc.identity.responses.UpdatePolicyResponse;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.netbeans.modules.cloud.oracle.NotificationUtils.showErrorMessage;
import static org.netbeans.modules.cloud.oracle.NotificationUtils.showMessage;
import static org.netbeans.modules.cloud.oracle.NotificationUtils.showWarningMessage;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.assets.Steps;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentItem;
import org.netbeans.modules.cloud.oracle.steps.CompartmentStep;
import org.netbeans.modules.cloud.oracle.steps.TenancyStep;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Uploads OCI policies or modifies existing.
 *
 * @author Dusan Petrovic
 */
@NbBundle.Messages({
    "MSG_PolicyUploaded=Policy: {0} successfully uploaded",
    "MSG_StatementsExist=Generated policy statements already exist inisde the compartment",
    "MSG_PolicyUploadError=Error while uploading policy: {0}",
    "MSG_PolicyUpdated=Policy: {0} successfully updated",
    "MSG_PolicyUpdateError=Error while updating policy: {0}",
    "MSG_PotentialOverpermissivePolicies=Potential overly permissive policy found in compartment: {0}"
})
public class PolicyUploader {
    
    private static final String DEFAULT_POLICY_NAME = "cloud-assets-auto-generated-policy";
    private static final String DEFAULT_POLICY_DESCRIPTION = "OCI Policy that allows access to added Cloud Assets";

    private final IdentityClient client;
    
    public PolicyUploader() {
        this.client = OCIManager.getDefault().getActiveSession().newClient(IdentityClient.class);
    }
    
    public void uploadPolicies(List<String> statements) {   
        Steps.NextStepProvider nsProvider = Steps.NextStepProvider.builder()
            .stepForClass(TenancyStep.class, (s) -> new CompartmentStep())
            .build();
        
        Lookup lookup = Lookups.fixed(nsProvider);
        Steps.getDefault().executeMultistep(new TenancyStep(true), lookup)
                .thenAccept(values -> {
                    CompartmentItem compartment = values.getValueForStep(CompartmentStep.class);
                    String compartmentId = compartment.getKey().getValue();
                    try {
                        List<String> statementsToAdd = filterExistingStatements(statements, compartmentId);
                        if (statementsToAdd.isEmpty()) {
                            showMessage(Bundle.MSG_StatementsExist());
                            return;
                        }
                        
                        Optional<Policy> policy = findPolicyByNameIfExist(DEFAULT_POLICY_NAME, compartmentId);
                        
                        if (policy.isPresent()) {
                            updateExistingPolicy(policy.get(), statementsToAdd);
                        } else {
                            createNewPolicy(statementsToAdd, compartmentId);
                        }
                    } catch (Exception ex) {
                        showErrorMessage(Bundle.MSG_PolicyUploadError(ex.getMessage()));
                    }
                    
                });        
    }
    
    private List<String> filterExistingStatements(List<String> statements, String compartmentId) {
        ListPoliciesRequest request = ListPoliciesRequest.builder()
                    .compartmentId(compartmentId)
                    .build();
        
        ListPoliciesResponse response = this.client.listPolicies(request);
        List<String> extractedStatements = PolicyParser.getAllStatementsFrom(response.getItems());        
        List<String> noWhitespaceStatements = PolicyParser.removeWhitespacesFromStatements(extractedStatements);
        
        detectOverpermissivePolicies(extractedStatements, compartmentId);
      
        return statements
                    .stream()
                    .filter(i -> !noWhitespaceStatements.contains(PolicyParser.prepareForComparing(i)))
                    .collect(Collectors.toList());
    }

    private void detectOverpermissivePolicies(List<String> extractedStatements, String compartmentId) {
        List<String> overpermissive = PolicyParser.filterOverpermissiveStatements(extractedStatements);
        if (!overpermissive.isEmpty()) {
            showWarningMessage(Bundle.MSG_PotentialOverpermissivePolicies(compartmentId));
        }
    }

    private Optional<Policy> findPolicyByNameIfExist(String policyName, String compartmentId) {
        ListPoliciesRequest request = ListPoliciesRequest.builder()
                .name(policyName)
                .compartmentId(compartmentId)
                .limit(1)
                .build();
        
        ListPoliciesResponse response = this.client.listPolicies(request);
        return !response.getItems().isEmpty() ? 
                Optional.of(response.getItems().get(0)) : Optional.empty();
    }

    private void createNewPolicy(List<String> statementsToAdd, String compartmentId) {
        CreatePolicyDetails createPolicyDetails = CreatePolicyDetails.builder()
                .name(DEFAULT_POLICY_NAME)
                .description(DEFAULT_POLICY_DESCRIPTION)
                .compartmentId(compartmentId)
                .statements(statementsToAdd)
                .build();
        CreatePolicyRequest request = CreatePolicyRequest.builder()
                .createPolicyDetails(createPolicyDetails)
                .build();

        CreatePolicyResponse response = this.client.createPolicy(request);
        if (response.get__httpStatusCode__() != 200) {
            showErrorMessage(Bundle.MSG_PolicyUploadError(DEFAULT_POLICY_NAME));
        }
        showMessage(Bundle.MSG_PolicyUploaded(DEFAULT_POLICY_NAME));
    }

    private void updateExistingPolicy(Policy policy, List<String> statementsToAdd) {
        List<String> resultStatements = Stream.concat(policy.getStatements().stream(), statementsToAdd.stream())
                                          .collect(Collectors.toList());
        UpdatePolicyDetails updatePolicyDetails = UpdatePolicyDetails.builder()
                .statements(resultStatements)
                .build();
        UpdatePolicyRequest request = UpdatePolicyRequest.builder()
                .policyId(policy.getId())
                .updatePolicyDetails(updatePolicyDetails)
                .build();

        UpdatePolicyResponse response = this.client.updatePolicy(request);
        if (response.get__httpStatusCode__() != 200) {
            showErrorMessage(Bundle.MSG_PolicyUpdateError(DEFAULT_POLICY_NAME));
        }
        showMessage(Bundle.MSG_PolicyUpdated(DEFAULT_POLICY_NAME));
    }
}
