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

package org.netbeans.modules.php.symfony;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpInterpreter;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.spi.framework.PhpModuleExtender;
import org.netbeans.modules.php.symfony.ui.wizards.NewProjectConfigurationPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 * @author Tomas Mysik
 */
public class SymfonyPhpModuleExtender extends PhpModuleExtender {
    //@GuardedBy(this)
    private NewProjectConfigurationPanel panel = null;

    @Override
    public Set<FileObject> extend(PhpModule phpModule) throws ExtendingException {
        // init project
        SymfonyScript symfonyScript = null;
        try {
            symfonyScript = SymfonyScript.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            // should not happen, must be handled in the wizard
            Exceptions.printStackTrace(ex);
            throw new ExtendingException(ex.getLocalizedMessage(), ex);
        }

        if (!symfonyScript.initProject(phpModule, getPanel().getProjectParams())) {
            // can happen if symfony script was not chosen
            Logger.getLogger(SymfonyPhpModuleExtender.class.getName())
                    .log(Level.INFO, "Framework Symfony not found in newly created project {0}", phpModule.getDisplayName());
            throw new ExtendingException(NbBundle.getMessage(SymfonyPhpModuleExtender.class, "MSG_NotExtended"));
        }

        // generate apps
        for (Pair<String, String[]> app : getPanel().getApps()) {
            symfonyScript.initApp(phpModule, app.first(), app.second());
        }

        // prefetch commands
        SymfonyPhpFrameworkProvider.getInstance().getFrameworkCommandSupport(phpModule).refreshFrameworkCommandsLater(null);

        // return files
        Set<FileObject> files = new HashSet<>();
        FileObject databases = SymfonyPhpFrameworkProvider.locate(phpModule, "config/databases.yml", true); // NOI18N
        if (databases != null) {
            // likely --orm=none
            files.add(databases);
        }
        FileObject config = SymfonyPhpFrameworkProvider.locate(phpModule, "config/ProjectConfiguration.class.php", true); // NOI18N
        if (config != null) {
            // #176041
            files.add(config);
        }

        if (files.isEmpty()) {
            // open at least index.php
            FileObject index = SymfonyPhpFrameworkProvider.locate(phpModule, "web/index.php", true); // NOI18N
            if (index != null) {
                files.add(index);
            }
        }

        return files;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        getPanel().addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        getPanel().removeChangeListener(listener);
    }

    @Override
    public JComponent getComponent() {
        return getPanel();
    }

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public boolean isValid() {
        return getErrorMessage() == null;
    }

    @Override
    public String getErrorMessage() {
        try {
            PhpInterpreter.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            return ex.getLocalizedMessage();
        }
        try {
            SymfonyScript.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            return NbBundle.getMessage(SymfonyPhpModuleExtender.class, "MSG_CannotExtend", ex.getMessage());
        }
        return getPanel().getErrorMessage();
    }

    @Override
    public String getWarningMessage() {
        return getPanel().getWarningMessage();
    }

    private synchronized NewProjectConfigurationPanel getPanel() {
        if (panel == null) {
            panel = new NewProjectConfigurationPanel();
        }
        return panel;
    }
}
