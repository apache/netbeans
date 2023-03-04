/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cloud.oracle.adm;

import com.oracle.bmc.adm.ApplicationDependencyManagementClient;
import com.oracle.bmc.adm.model.CreateKnowledgeBaseDetails;
import com.oracle.bmc.adm.requests.CreateKnowledgeBaseRequest;
import com.oracle.bmc.adm.responses.CreateKnowledgeBaseResponse;
import com.oracle.bmc.model.BmcException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentItem;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Pisl
 */
@ActionID(
        category = "Tools",
        id = "org.netbeans.modules.cloud.oracle.adm.CreateKnowledgeBaseAction"
)
@ActionRegistration(
        displayName = "#CTL_CreateKnowledgeBaseAction",
        asynchronous = true
)

@ActionReferences(value = {
    @ActionReference(path = "Cloud/Oracle/Compartment/Actions", position = 270)
})
@NbBundle.Messages({
    "CTL_CreateKnowledgeBaseAction=Create Knowledge Base",
    "# {0} - Knowledge Base name",
    "MSG_CreatingKnowledgeBase=Creating Knowledge base {0}...",
    "# {0} - Knowledge Base name",
    "MSG_KBCreated=Knowledge Base {0} was created.",
    "# {0} - Knowledge Base name",
    "MSG_KBNotCreated=Creating Knowledge Base {0} failed.",
    "MSG_KBNameIsNotFilled=You must fill in the displayed name of the knowledge base."

})
public class CreateKnowledgeBaseAction implements ActionListener {

    private final CompartmentItem context;

    public CreateKnowledgeBaseAction(CompartmentItem context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Optional<String> result = CreateKnowledgeBaseDialog.showDialog(context);
        if (!result.isPresent()) {
            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(Bundle.MSG_KBNameIsNotFilled()));
        }
        result.ifPresent((p) -> {
            RequestProcessor.getDefault().execute(() -> {
                ProgressHandle progressHandle = ProgressHandle.createHandle(Bundle.MSG_CreatingKnowledgeBase(p));
                progressHandle.start();
                
                try (ApplicationDependencyManagementClient client
                        = new ApplicationDependencyManagementClient(OCIManager.getDefault().getConfigProvider())) {

                    CreateKnowledgeBaseDetails params = CreateKnowledgeBaseDetails.builder()
                            .compartmentId(context.getKey().getValue())
                            .displayName(result.get()).build();
                    CreateKnowledgeBaseRequest request = CreateKnowledgeBaseRequest.builder()
                            .createKnowledgeBaseDetails(params).build();
                    CreateKnowledgeBaseResponse response = client.createKnowledgeBase(request);
                    int resultCode = response.get__httpStatusCode__();
                    String message;
                    if (resultCode == 202) {
                        context.refresh();
                        message = Bundle.MSG_KBCreated(result.get());
                        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(message));
                    } else {
                        ErrorUtils.processError(response, Bundle.MSG_KBNotCreated(result.get()));
                    }
                } catch (BmcException e) {
                    ErrorUtils.processError(e, Bundle.MSG_KBNotCreated(result.get()));
                } finally {
                    progressHandle.finish();
                }

            });
        });
    }

}
