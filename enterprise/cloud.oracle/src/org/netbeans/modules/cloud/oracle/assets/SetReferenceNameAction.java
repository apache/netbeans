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
package org.netbeans.modules.cloud.oracle.assets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@ActionID(
        category = "Tools",
        id = "org.netbeans.modules.cloud.oracle.actions.SetReferenceName"
)
@ActionRegistration( 
        displayName = "#ReferenceName", 
        asynchronous = true
)

@NbBundle.Messages({
    "ReferenceName=Set a reference name",
    "ReferenceNameValidationError=Reference Name can contain only letters and numbers",
    "ReferenceNameSame=Reference names must be unique for each resource type."
})
public class SetReferenceNameAction implements ActionListener {
    
    private final OCIItem context;

    public SetReferenceNameAction(OCIItem context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String oldRefName = CloudAssets.getDefault().getReferenceName(context);
        if (oldRefName == null) {
            oldRefName = "";
        }
        NotifyDescriptor.InputLine inp = new NotifyDescriptor.InputLine(oldRefName, Bundle.ReferenceName());
        inp.setInputText(oldRefName);
        Object selected = DialogDisplayer.getDefault().notify(inp);
        if (DialogDescriptor.OK_OPTION != selected) {
            return;
        }
        String refName = inp.getInputText();
        if (refName.matches("[a-zA-Z0-9]+")) { //NOI18N
            if (!CloudAssets.getDefault().setReferenceName(context, refName)) {
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(Bundle.ReferenceNameSame());
                DialogDisplayer.getDefault().notify(msg);
            }
        } else {
            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(Bundle.ReferenceNameValidationError());
            DialogDisplayer.getDefault().notify(msg);
        }
    }
    
}
