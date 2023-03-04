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
package org.netbeans.modules.git.ui.repository;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.SystemReader;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.ui.actions.GitAction;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Vrabec
 */
@ActionID(id = "org.netbeans.modules.git.ui.repository.OpenGlobalConfigurationAction", category = "Git")
@ActionRegistration(lazy = false, displayName = "#LBL_OpenGlobalConfigurationAction_Name")
@NbBundle.Messages({
    "LBL_OpenGlobalConfigurationAction_Name=Open &Global Configuration",
    "LBL_OpenGlobalConfigurationAction_PopupName=Open &Global Configuration",
    "LBL_OpenGlobalConfigurationAction_CreateConfigFile=Create User Config File",
    "# {0} - path to configuration file",
    "MSG_OpenGlobalConfigurationAction_CreateConfigFile=Global user configuration does not yet exist.\n"
            + "Do you want to create it?\n\n"
            + "It will be created as {0}"
})
public class OpenGlobalConfigurationAction extends GitAction {

    private static final Logger LOG = Logger.getLogger(OpenGlobalConfigurationAction.class.getName());
    
    @Override
    protected boolean enableFull (Node[] activatedNodes) {
        return true;
    }
    
    @Override
    protected void performContextAction (final Node[] nodes) {
        Utils.postParallel(new Runnable () {
            @Override
            public void run() {
                File config = SystemReader.getInstance().openUserConfig(null, FS.DETECTED).getFile();
                if (!config.exists()) {
                    if (shallCreate(config)) {
                        try {
                            if (!config.createNewFile()) {
                                LOG.log(Level.WARNING, "Git config not created: {0}", new Object[] { config.getPath() });
                            }
                        } catch (IOException ex) {
                            GitClientExceptionHandler.notifyException(ex, true);
                        }
                    } else {
                        return;
                    }
                }
                if (config.canRead()) {
                    Utils.openFile(config);
                } else {
                    LOG.log(Level.WARNING, "Cannot read Git config: {0}", new Object[] { config.getPath() });
                }
            }

        }, 0);
    }
    
    private boolean shallCreate (File configFile) {
        return NotifyDescriptor.YES_OPTION == DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                Bundle.MSG_OpenGlobalConfigurationAction_CreateConfigFile(configFile.getAbsolutePath()),
                Bundle.LBL_OpenGlobalConfigurationAction_CreateConfigFile(),
                NotifyDescriptor.YES_NO_OPTION));
    }
}
