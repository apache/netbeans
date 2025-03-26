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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.OCIProfile;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionState;
import org.openide.util.NbBundle;

/**
 * 
 * @author sdedic
 */
@ActionID(
        category = "Tools",
        id = "org.netbeans.modules.cloud.oracle.actions.SetDefaultTenancyAction"
)
@ActionRegistration(
        displayName = "#CTL_SetDefaultProfileAction",
        asynchronous = true,
        enabledOn = @ActionState(type = OCIProfile.class, useActionInstance = true)
)

@ActionReferences(value = {
    @ActionReference(path = "Cloud/Oracle/Tenancy/Actions", position = 200)
})
@NbBundle.Messages({
    "CTL_SetDefaultProfileAction=Set As Default",

})
public class SetDefaultProfileAction extends AbstractAction {
    private final OCIProfile profile;

    public SetDefaultProfileAction(OCIProfile tenancy) {
        this.profile = tenancy;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        OCIManager.getDefault().setActiveProfile(profile);
    }

    @Override
    public boolean isEnabled() {
        return OCIManager.getDefault().getActiveProfile() != profile;
    }
}
