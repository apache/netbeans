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

package org.netbeans.modules.maven.actions;

import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import static org.netbeans.modules.maven.actions.Bundle.*;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

@SuppressWarnings(value = "serial")
@ActionID(id = "org.netbeans.modules.maven.refresh", category = "Project")
@ActionRegistration(displayName = "#ACT_Reload_Project", lazy=false)
@ActionReference(position = 1700, path = "Projects/org-netbeans-modules-maven/Actions")
@Messages("ACT_Reload_Project=Reload POM")
public class RefreshAction extends AbstractAction implements ContextAwareAction {

    private final Lookup context;
    public RefreshAction() {
        this(Lookup.EMPTY);
    }

    @Messages({"# {0} - count", "ACT_Reload_Projects=Reload {0} POMs"})
    private RefreshAction(Lookup lkp) {
        context = lkp;
        Collection<? extends NbMavenProjectImpl> col = context.lookupAll(NbMavenProjectImpl.class);
        if (col.size() > 1) {
            putValue(Action.NAME, ACT_Reload_Projects(col.size()));
        } else {
            putValue(Action.NAME, ACT_Reload_Project());
        }
    }

    @Override public void actionPerformed(ActionEvent event) {
        // #166919 - need to run in RP to prevent RPing later in fireProjectReload()
        //since #227101 fireMavenProjectReload() always posts to the RP... 
                //#211217 in 3.x should not be necessary.. 
                //EmbedderFactory.resetCachedEmbedders();
                for (NbMavenProjectImpl prj : context.lookupAll(NbMavenProjectImpl.class)) {
                    NbMavenProject.fireMavenProjectReload(prj);
                }
    }

    @Override public Action createContextAwareInstance(Lookup actionContext) {
        return new RefreshAction(actionContext);
    }

}
