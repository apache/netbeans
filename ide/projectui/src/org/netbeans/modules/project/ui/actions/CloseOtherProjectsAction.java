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
package org.netbeans.modules.project.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

@ActionID(
        category = "File",
        id = "org.netbeans.modules.project.ui.actions.CloseOtherProjectsAction"
        )
@ActionRegistration(
        displayName = "#CTL_CloseOtherProjectsAction"
        )
@ActionReference(path = "Menu/File", position = 750)
@Messages("CTL_CloseOtherProjectsAction=C&lose Other Projects")
public final class CloseOtherProjectsAction implements ActionListener {

    private static final RequestProcessor RP = new RequestProcessor(CloseAllProjectsAction.class);
    private final List<Project> selectedProjects;

    public CloseOtherProjectsAction(List<Project> selectedProjects) {
        this.selectedProjects = selectedProjects;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        RP.post(new Runnable() { //#249720
            @Override
            public void run() { 
                OpenProjects manager = OpenProjects.getDefault();
                List<Project> openProjects = new ArrayList<Project>(Arrays.asList(manager.getOpenProjects()));
                openProjects.removeAll(selectedProjects);
                if (!openProjects.isEmpty()) {
                    Project[] otherProjectsToBeClosed = openProjects.toArray(new Project[0]);
                    manager.close(otherProjectsToBeClosed);
                }
            }
        });    
    }
}
