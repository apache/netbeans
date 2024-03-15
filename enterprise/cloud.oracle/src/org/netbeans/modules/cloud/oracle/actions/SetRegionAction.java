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

import com.oracle.bmc.identity.Identity;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.requests.ListRegionsRequest;
import com.oracle.bmc.identity.responses.ListRegionsResponse;
import java.awt.Dialog;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.stream.Collectors;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.OCIProfile;
import org.netbeans.modules.cloud.oracle.OCISessionInitiator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * This action lets user select an Oracle Cloud region. This region will be used for all subsequent requests.
 * 
 * @author Jan Horvath
 */
@ActionID(
        category = "Tools",
        id = "org.netbeans.modules.cloud.oracle.actions.SetDefaultRegionAction"
)
@ActionRegistration(
        displayName = "#SetDefaultRegion",
        asynchronous = true
)
@ActionReferences(value = {
    @ActionReference(path = "Cloud/Oracle/Tenancy/Actions", position = 250)
})
@NbBundle.Messages({
    "SetDefaultRegion=Set OCI Region",
    "SelectRegion=Select a Region"
})
public class SetRegionAction implements ActionListener {

    private final OCIProfile context;

    public SetRegionAction(OCIProfile context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        OCIProfile profile = OCIManager.forProfile(null);
        Identity identityClient = IdentityClient.builder().build(profile.getAuthenticationProvider());
        ListRegionsRequest request = ListRegionsRequest.builder().build();

        ListRegionsResponse response = identityClient.listRegions(request);

        List<String> items = response.getItems().stream()
                .map(region -> region.getName())
                .sorted((r1, r2) -> r1.compareTo(r2))
                .collect(Collectors.toList());

        SetRegionDialog dlgPanel = new SetRegionDialog(items);
        DialogDescriptor descriptor = new DialogDescriptor(dlgPanel, Bundle.SelectRegion()); //NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setMinimumSize(dlgPanel.getPreferredSize());
        dialog.setVisible(true);
        if (DialogDescriptor.OK_OPTION == descriptor.getValue()) {
            String selectedCode = dlgPanel.getSelectedRegion();
            context.setRegionCode(selectedCode);
        }

    }

}
