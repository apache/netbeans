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

package org.netbeans.modules.php.zend.ui.actions;

import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.spi.framework.actions.RunCommandAction;
import org.netbeans.modules.php.zend.ZendPhpFrameworkProvider;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public final class ZendRunCommandAction extends RunCommandAction {
    private static final long serialVersionUID = -2278946423132142L;
    private static final ZendRunCommandAction INSTANCE = new ZendRunCommandAction();

    private ZendRunCommandAction() {
    }

    public static ZendRunCommandAction getInstance() {
        return INSTANCE;
    }

    @Override
    public void actionPerformed(PhpModule phpModule) {
        if (!ZendPhpFrameworkProvider.getInstance().isInPhpModule(phpModule)) {
            return;
        }
        ZendPhpFrameworkProvider.getInstance().getFrameworkCommandSupport(phpModule).openPanel();
    }

    @Override
    protected String getFullName() {
        return NbBundle.getMessage(ZendRunCommandAction.class, "LBL_ZendAction", getPureName());
    }
}
