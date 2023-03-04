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

package org.netbeans.modules.php.spi.framework.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.openide.util.HelpCtx;

/**
 * Base action class which operates with a PHP module, suitable for framework classes.
 * It takes care about names (name in menu, name in shortcuts).
 * @author Tomas Mysik
 */
public abstract class BaseAction extends AbstractAction implements HelpCtx.Provider {

    protected BaseAction() {
        putValue("noIconInMenu", true); // NOI18N
        String fullName = getFullName();
        putValue(NAME, fullName);
        putValue(SHORT_DESCRIPTION, fullName);
        putValue("menuText", getPureName()); // NOI18N
    }

    /**
     * Get the name for shortcuts table (Tools > Options > Keymap).
     * @return the name for shortcuts table (Tools > Options > Keymap)
     */
    protected abstract String getFullName();

    /**
     * Get the name for menu (and context menu).
     * @return the name for menu (and context menu)
     */
    protected abstract String getPureName();

    /**
     * Framework implementations should likely check whether they
     * {@link org.netbeans.modules.php.spi.phpmodule.PhpFrameworkProvider#isInPhpModule(PhpModule) extend} the given PHP module.
     * @param phpModule PHP module the action is invoked on, never {@code null}
     */
    protected abstract void actionPerformed(PhpModule phpModule);

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * If a PHP module {@link PhpModule#inferPhpModule() is found} then
     * {@link #actionPerformed(PhpModule) actionPerformed(PhpModule)} is called.
     */
    @Override
    public final void actionPerformed(ActionEvent e) {
        PhpModule phpModule = PhpModule.Factory.inferPhpModule();
        if (phpModule == null) {
            return;
        }
        actionPerformed(phpModule);
    }
}
