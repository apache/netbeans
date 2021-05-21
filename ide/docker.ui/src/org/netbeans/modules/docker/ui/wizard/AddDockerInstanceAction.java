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
package org.netbeans.modules.docker.ui.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

/**
 *
 * @author Petr Hejl
 */
@ActionID(id = "org.netbeans.modules.docker.ui.wizard.AddDockerInstanceAction", category = "System")
@ActionRegistration(displayName = "#LBL_AddDockerInstanceAction")
@ActionReferences(
    @ActionReference(path = "Docker/Wizard", position = 100)
)
public class AddDockerInstanceAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        AddDockerInstanceWizard wizard = new AddDockerInstanceWizard();
        wizard.show();
    }
}
