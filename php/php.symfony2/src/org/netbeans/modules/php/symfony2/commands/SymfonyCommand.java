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
package org.netbeans.modules.php.symfony2.commands;

import org.netbeans.modules.php.spi.framework.commands.FrameworkCommand;

/**
 * Symfony 2/3 command.
 */
public final class SymfonyCommand extends FrameworkCommand {

    private final String help;


    public SymfonyCommand(String command, String description, String help) {
        super(command, description, command);
        this.help = help;
    }

    @Override
    protected String getHelpInternal() {
        return help;
    }

    @Override
    public String getPreview() {
        return SymfonyScript.SCRIPT_NAME + " " + super.getPreview(); // NOI18N
    }

}
