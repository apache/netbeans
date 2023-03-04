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
package org.netbeans.modules.php.doctrine2.ui.actions;

import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.doctrine2.Doctrine2PhpFrameworkProvider;
import org.netbeans.modules.php.spi.framework.actions.RunCommandAction;
import org.openide.util.NbBundle.Messages;

/**
 * Doctrine2 run command action.
 */
public final class Doctrine2RunCommandAction extends RunCommandAction {

    private static final long serialVersionUID = -876546546767L;
    private static final Doctrine2RunCommandAction INSTANCE = new Doctrine2RunCommandAction();


    private Doctrine2RunCommandAction() {
    }

    public static Doctrine2RunCommandAction getInstance() {
        return INSTANCE;
    }

    @Override
    public void actionPerformed(PhpModule phpModule) {
        if (!Doctrine2PhpFrameworkProvider.getInstance().isInPhpModule(phpModule)) {
            return;
        }

        Doctrine2PhpFrameworkProvider.getInstance().getFrameworkCommandSupport(phpModule).openPanel();
    }

    @Messages({
        "# {0} - command name",
        "LBL_Doctrine2Action=Doctrine2: {0}"
    })
    @Override
    protected String getFullName() {
        return Bundle.LBL_Doctrine2Action(getPureName());
    }

}
