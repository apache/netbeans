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
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Optional;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentNode;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentItem;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Horvath
 */
@ActionID(
        category = "Tools",
        id = "org.netbeans.modules.cloud.oracle.actions.CreateAutonomousDBAction"
)
@ActionRegistration(
        displayName = "#CTL_CreateAutonomousDBAction",
        asynchronous = true
)

@ActionReferences(value = {
    @ActionReference(path = "Cloud/Oracle/Compartment/Actions", position = 250)
})
@NbBundle.Messages({
    "CTL_CreateAutonomousDBAction=Create Autonomous Database",
    "MSG_DBCreated=Autonomous Database {0} was created.",
    "MSG_DBNotCreated=Autonomous Database {0} failed to create: {1}"

})
public class CreateAutonomousDBAction implements ActionListener {

    private final CompartmentItem context;

    public CreateAutonomousDBAction(CompartmentItem context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Optional<Pair<String, char[]>> result = CreateAutonomousDBDialog.showDialog(context);
        result.ifPresent((p) -> {
            RequestProcessor.getDefault().execute(() -> {
                Optional<String> message = OCIManager.getDefault().createAutonomousDatabase(context.getKey().getValue(), p.first(), p.second());
                if (!message.isPresent()) {
                    context.refresh();
                    DialogDisplayer.getDefault().notifyLater(
                            new NotifyDescriptor.Message(
                                    Bundle.MSG_DBCreated(p.first())));
                } else {
                    DialogDisplayer.getDefault().notifyLater(
                            new NotifyDescriptor.Message(
                                    Bundle.MSG_DBNotCreated(p.first(), message.get())));
                }
            });
        });
    }

}
