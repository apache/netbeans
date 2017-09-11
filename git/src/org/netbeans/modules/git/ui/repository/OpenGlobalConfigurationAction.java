/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
