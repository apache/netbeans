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

package org.netbeans.modules.php.zend;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.spi.framework.PhpModuleExtender;
import org.netbeans.modules.php.zend.ui.wizards.NewProjectConfigurationPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class ZendPhpModuleExtender extends PhpModuleExtender {
    //@GuardedBy(this)
    private NewProjectConfigurationPanel panel = null;

    @Override
    public Set<FileObject> extend(PhpModule phpModule) throws ExtendingException {
        // init project
        ZendScript zendScript = null;
        try {
            zendScript = ZendScript.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            // should not happen, must be handled in the wizard
            Exceptions.printStackTrace(ex);
            throw new ExtendingException(ex.getLocalizedMessage(), ex);
        }

        if (!zendScript.initProject(phpModule)) {
            // can happen if zend script was not chosen
            Logger.getLogger(ZendPhpModuleExtender.class.getName())
                    .log(Level.INFO, "Framework Zend not found in newly created project {0}", phpModule.getDisplayName());
            throw new ExtendingException(NbBundle.getMessage(ZendPhpModuleExtender.class, "MSG_NotExtended"));
        }

        // prefetch commands
        ZendPhpFrameworkProvider.getInstance().getFrameworkCommandSupport(phpModule).refreshFrameworkCommandsLater(null);

        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            // broken project
            assert false : "Module extender for no sources of: " + phpModule.getName();
            return Collections.emptySet();
        }

        // return files
        Set<FileObject> files = new HashSet<>();
        FileObject appConfig = sourceDirectory.getFileObject("application/configs/application.ini"); // NOI18N
        if (appConfig != null) {
            files.add(appConfig);
        }
        FileObject indexController = sourceDirectory.getFileObject("application/controllers/IndexController.php"); // NOI18N
        if (indexController != null) {
            files.add(indexController);
        }
        FileObject bootstrap = sourceDirectory.getFileObject("application/Bootstrap.php"); // NOI18N
        if (bootstrap != null) {
            files.add(bootstrap);
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
            ZendScript.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            return NbBundle.getMessage(ZendPhpModuleExtender.class, "MSG_CannotExtend", ex.getMessage());
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
