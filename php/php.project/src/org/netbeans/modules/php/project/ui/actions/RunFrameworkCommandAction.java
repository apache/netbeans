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

package org.netbeans.modules.php.project.ui.actions;

import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.project.PhpModuleImpl;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.spi.framework.actions.RunCommandAction;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.framework.PhpModuleActionsExtender;
import org.openide.LifecycleManager;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public final class RunFrameworkCommandAction extends RunCommandAction {
    private static final long serialVersionUID = -22735302227232842L;
    private static final RunFrameworkCommandAction INSTANCE = new RunFrameworkCommandAction();

    private RunFrameworkCommandAction() {
    }

    public static RunFrameworkCommandAction getInstance() {
        return INSTANCE;
    }

    @Override
    public void actionPerformed(final PhpModule phpModule) {
        if (phpModule instanceof PhpModuleImpl) {
            PhpProject project = ((PhpModuleImpl) phpModule).getPhpProject();
            // XXX more precise would be to collect all Run Command actions and if > 1 then show a dialog which framework should be run
            for (PhpFrameworkProvider frameworkProvider : project.getFrameworks()) {
                PhpModuleActionsExtender actionsExtender = frameworkProvider.getActionsExtender(phpModule);
                if (actionsExtender != null) {
                    RunCommandAction runCommandAction = actionsExtender.getRunCommandAction();
                    if (runCommandAction != null) {
                        // #202549
                        LifecycleManager.getDefault().saveAll();
                        runCommandAction.actionPerformed(phpModule);
                        return;
                    }
                }
            }
        }
    }

    @Override
    protected String getFullName() {
        return NbBundle.getMessage(RunFrameworkCommandAction.class, "LBL_RunFrameworkCommand");
    }
}
