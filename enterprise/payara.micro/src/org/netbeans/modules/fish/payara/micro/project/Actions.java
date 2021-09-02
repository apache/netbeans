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
package org.netbeans.modules.fish.payara.micro.project;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import javax.swing.Action;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.RELOAD_FILE;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.RELOAD_ICON;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author lkishalmi
 */
public class Actions {

    public static final String COMMAND_MICRO_RELOAD = "micro-reload"; //NOI18N

    private Actions() {}
    
    @ActionID(
            id = "org.netbeans.modules.payara.micro.action.reload",
            category = "Build"
    )
    @ActionRegistration(
            displayName = "#CTL_ReloadAppAction",
            lazy = false
    )
    @ActionReferences({
        @ActionReference(path = "Menu/BuildProject", position = 55),
        @ActionReference(path = "Toolbars/Build", position = 325),
        @ActionReference(path = "Projects/org-netbeans-modules-maven/Actions", position = 1020),
        @ActionReference(path = "Shortcuts", name = "DS-A")
    })
    @NbBundle.Messages("CTL_ReloadAppAction=Reload")
    public static Action reloadAction() {
        Action ret = ProjectSensitiveActions.projectCommandAction(COMMAND_MICRO_RELOAD, Bundle.CTL_ReloadAppAction(), null);
        ret.putValue("iconBase", RELOAD_ICON);
        return ret;
    }
    
    static void reloadApplication(MicroApplication application) {
        if (!application.isRunning()) {
            return;
        }
        String buildPath = application.getMavenProject().getBuild().getDirectory()
                + File.separator
                + application.getMavenProject().getBuild().getFinalName();
        reloadApplication(buildPath);
    }
    
    static void reloadApplication(String buildPath) {
        File check = new File(buildPath, RELOAD_FILE);
        if (check.exists()) {
            check.setLastModified(System.currentTimeMillis());
        } else {
            try {
                check.createNewFile();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
