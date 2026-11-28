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
package org.netbeans.modules.acp.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(category = "ACP", id = "org.netbeans.modules.acp.ui.AcpAskAgentOutputAction")
@ActionRegistration(displayName = "#CTL_AcpAskAgentOutputAction")
@ActionReferences({
    @ActionReference(path = "Editors/Popup", position = 5010)
})
@NbBundle.Messages("CTL_AcpAskAgentOutputAction=Ask ACP Agent About Output...")
public final class AcpAskAgentOutputAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent event) {
        AcpEditorContext.send(event, (editor, selected) -> "Build/test output:\n```\n" + selected
                + "\n```\n\nPlease analyze this output and help me fix any errors.");
    }
}
