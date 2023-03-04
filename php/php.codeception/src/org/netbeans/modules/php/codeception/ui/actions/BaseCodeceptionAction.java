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
package org.netbeans.modules.php.codeception.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.codeception.ui.options.CodeceptionOptionsPanelController;
import org.openide.util.NbBundle;

abstract class BaseCodeceptionAction extends AbstractAction {

    protected final PhpModule phpModule;


    protected BaseCodeceptionAction(PhpModule phpModule) {
        assert phpModule != null;
        this.phpModule = phpModule;
        putValue("noIconInMenu", true); // NOI18N
        String name = getName();
        putValue(NAME, name);
        putValue(SHORT_DESCRIPTION, name);
        putValue("menuText", name); // NOI18N
    }

    protected abstract String getName();

    protected abstract void runCommand() throws InvalidPhpExecutableException;

    @NbBundle.Messages("BaseCodeceptionAction.error.codeception.notValid=Codeception is not valid.")
    @Override
    public final void actionPerformed(ActionEvent e) {
        try {
            runCommand();
        } catch (InvalidPhpExecutableException ex) {
            UiUtils.invalidScriptProvided(Bundle.BaseCodeceptionAction_error_codeception_notValid(), CodeceptionOptionsPanelController.OPTIONS_SUB_PATH);
        }
    }

}
