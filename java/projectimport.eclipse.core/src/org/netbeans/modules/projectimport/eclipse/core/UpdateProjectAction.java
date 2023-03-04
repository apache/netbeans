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
package org.netbeans.modules.projectimport.eclipse.core;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.projectimport.eclipse.core.spi.UpgradableProjectLookupProvider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

@ActionID(id = "org.netbeans.modules.projectimport.eclipse.core.UpdateProjectAction", category = "Project")
@ActionRegistration(displayName = "#UpdateProjectAction.Name", lazy=false)
@ActionReference(position = 234, path = "Projects/Actions")
public final class UpdateProjectAction extends AbstractAction implements ContextAwareAction {
    
    private Lookup context;
    
    public UpdateProjectAction() {
        this(null);
    }

    public UpdateProjectAction(Lookup actionContext) {
        super(NbBundle.getMessage(UpdateProjectAction.class, "UpdateProjectAction.Name"));
        this.context = actionContext;
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
    }

    public @Override void actionPerformed(ActionEvent ignore) {
        new UpdateAllProjects().update(false);
    }

    @Override
    public boolean isEnabled() {
        assert context != null;
        Project p = context.lookup(Project.class);
        if (p == null || !UpgradableProjectLookupProvider.isRegistered(p)) {
            return false;
        }
        UpgradableProject upgradable = p.getLookup().lookup(UpgradableProject.class);
        return upgradable != null && upgradable.isUpgradable();
    }
    
    public @Override Action createContextAwareInstance(Lookup actionContext) {
        return new UpdateProjectAction(actionContext);
    }

}
