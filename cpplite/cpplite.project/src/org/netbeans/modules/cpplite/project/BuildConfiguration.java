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
package org.netbeans.modules.cpplite.project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import org.netbeans.spi.project.ActionProvider;

/**
 *
 * @author lahvac
 */
public class BuildConfiguration {
    private static final String KEY_NAME = "name";

    private final String name;
    private final Map<String, List<List<String>>> commands;

    public BuildConfiguration(String name, Map<String, List<List<String>>> commands) {
        this.name = name;
        this.commands = commands;
    }

    public String getName() {
        return name;
    }

    public List<List<String>> executablesFor(String command) {
        return commands.get(command);
    }

    public void save(Preferences prefs) {
        prefs.put(KEY_NAME, name);
        for (String command : new String[] {ActionProvider.COMMAND_BUILD, ActionProvider.COMMAND_CLEAN, ActionProvider.COMMAND_RUN}) {
            List<List<String>> executablesFor = commands.get(command);
            if (executablesFor == null) {
                prefs.remove(command);
            } else {
                prefs.put(command, Utils.encode(executablesFor));
            }
        }
    }

    public static BuildConfiguration read(Preferences prefs) {
        Map<String, List<List<String>>> commands = new HashMap<>();
        for (String command : new String[] {ActionProvider.COMMAND_BUILD, ActionProvider.COMMAND_CLEAN, ActionProvider.COMMAND_RUN}) {
            String cmd = prefs.get(command, null);
            if (cmd == null || cmd.isEmpty()) {
                continue;
            }
            commands.put(command, Utils.decode(cmd));
        }
        return new BuildConfiguration(prefs.get(KEY_NAME, ""), commands);
    }
}
