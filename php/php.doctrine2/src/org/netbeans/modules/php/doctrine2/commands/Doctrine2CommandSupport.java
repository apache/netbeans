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
package org.netbeans.modules.php.doctrine2.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.doctrine2.ui.options.Doctrine2OptionsPanelController;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommand;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommandSupport;
import org.openide.util.NbBundle.Messages;

/**
 * Command support for Doctrine2.
 */
public final class Doctrine2CommandSupport extends FrameworkCommandSupport {

    public Doctrine2CommandSupport(PhpModule phpModule) {
        super(phpModule);
    }

    @Messages("LBL_Doctrine2=Doctrine2")
    @Override
    public String getFrameworkName() {
        return Bundle.LBL_Doctrine2();
    }

    @Override
    public void runCommand(CommandDescriptor commandDescriptor, Runnable postExecution) {
        String[] commands = commandDescriptor.getFrameworkCommand().getCommands();
        String[] commandParams = commandDescriptor.getCommandParams();
        List<String> params = new ArrayList<>(commands.length + commandParams.length);
        params.addAll(Arrays.asList(commands));
        params.addAll(Arrays.asList(commandParams));
        try {
            Doctrine2Script.getDefault().runCommand(phpModule, params, postExecution);
        } catch (InvalidPhpExecutableException ex) {
            UiUtils.invalidScriptProvided(ex.getLocalizedMessage(), Doctrine2OptionsPanelController.OPTIONS_SUBPATH);
        }
    }

    @Override
    protected String getOptionsPath() {
        return Doctrine2OptionsPanelController.getOptionsPath();
    }

    @Override
    protected File getPluginsDirectory() {
        return null;
    }

    @Override
    protected List<FrameworkCommand> getFrameworkCommandsInternal() {
        Doctrine2Script doctrine2;
        try {
            doctrine2 = Doctrine2Script.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            UiUtils.invalidScriptProvided(ex.getLocalizedMessage(), Doctrine2OptionsPanelController.OPTIONS_SUBPATH);
            return null;
        }

        List<Doctrine2CommandVO> commandsVO = doctrine2.getCommands(phpModule);
        if (commandsVO == null) {
            // some error
            return null;
        }
        List<FrameworkCommand> commands = new ArrayList<>(commandsVO.size());
        for (Doctrine2CommandVO command : commandsVO) {
            commands.add(new Doctrine2Command(command.getCommand(), command.getDescription(), command.getHelp()));
        }
        return commands;
    }

}
