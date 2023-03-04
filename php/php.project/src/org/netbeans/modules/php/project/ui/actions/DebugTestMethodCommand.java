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

import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ui.actions.support.ConfigAction;
import org.netbeans.modules.php.project.ui.actions.support.Displayable;
import org.netbeans.spi.project.SingleMethod;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Debugs one test method.
 */
public class DebugTestMethodCommand extends Command implements Displayable {

    public static final String ID = SingleMethod.COMMAND_DEBUG_SINGLE_METHOD;


    public DebugTestMethodCommand(PhpProject project) {
        super(project);
    }

    @Override
    public String getCommandId() {
        return ID;
    }

    @NbBundle.Messages("DebugTestMethodCommand.name=Debug Test Method")
    @Override
    public String getDisplayName() {
        return Bundle.DebugTestMethodCommand_name();
    }

    @Override
    public boolean isActionEnabledInternal(Lookup context) {
        return getConfigAction().isDebugMethodEnabled(context);
    }

    @Override
    public void invokeActionInternal(Lookup context) {
        getConfigAction().debugMethod(context);
    }

    @Override
    protected ConfigAction getConfigAction() {
        return ConfigAction.get(ConfigAction.Type.TEST, getProject());
    }

}
