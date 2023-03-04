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

import org.netbeans.modules.php.spi.framework.commands.FrameworkCommand;

/**
 * Doctrine2 command.
 */
public final class Doctrine2Command extends FrameworkCommand {

    private final String help;


    public Doctrine2Command(String command, String description, String help) {
        super(command, description, command);
        this.help = help;
    }

    @Override
    protected String getHelpInternal() {
        return help;
    }

    @Override
    public String getPreview() {
        return Doctrine2Script.SCRIPT_NAME + " " + super.getPreview(); // NOI18N
    }

}
