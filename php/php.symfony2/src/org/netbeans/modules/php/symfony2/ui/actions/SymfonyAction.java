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
package org.netbeans.modules.php.symfony2.ui.actions;

import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.spi.framework.actions.BaseAction;
import org.netbeans.modules.php.symfony2.SymfonyPhpFrameworkProvider;
import org.openide.util.NbBundle;

/**
 * Base class for Symfony 2/3 separate commands.
 */
abstract class SymfonyAction extends BaseAction {


    protected abstract void runCommand(PhpModule phpModule);

    @Override
    public void actionPerformed(PhpModule phpModule) {
        if (!SymfonyPhpFrameworkProvider.getInstance().isInPhpModule(phpModule)) {
            return;
        }
        runCommand(phpModule);
    }

    @NbBundle.Messages({
        "# {0} - command name",
        "SymfonyAction.name=Symfony 2/3: {0}",
    })
    @Override
    protected String getFullName() {
        return Bundle.SymfonyAction_name(getPureName());
    }

}
