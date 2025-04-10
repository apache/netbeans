/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.project.ui.actions;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.Action;

/**
 * Manages shortcuts based on the action's command.
 * Useful for file and project actions.
 */
class ShortcutManager {
    
    public static final ShortcutManager INSTANCE = new ShortcutManager();

    private ShortcutManager() {}

    // command -> shortcut
    Map<String, Object> shorcuts = new HashMap<String, Object>();

    // command -> WeakSet of actions
    HashMap<String, Set<Action>> actions = new HashMap<String, Set<Action>>();
    public void registerAction(String command, Action action) {
        synchronized (this) {
            Set<Action> commandActions = actions.get(command);
            if (commandActions == null) {
                commandActions = Collections.newSetFromMap(new WeakHashMap<>());
                actions.put(command, commandActions);
            }
            commandActions.add(action);
        }
        Object shorcut = getShortcut(command);
        if (shorcut != null) {
            action.putValue(Action.ACCELERATOR_KEY, shorcut);
        }
    }
    public void registerShortcut(String command, Object shortcut) {
        Set<Action> actionsToChange = null;
        synchronized (this) {
            Object exShorcut = getShortcut(command);
            if ((exShorcut != null && exShorcut.equals(shortcut)) || (exShorcut == null && shortcut == null)) {
                // or both are null
                return; // No action needed
            }
            shorcuts.put(command, shortcut);
            Set<Action> commandActions = actions.get(command);
            if (commandActions != null && !commandActions.isEmpty()) {
                actionsToChange = new HashSet<Action>();
                actionsToChange.addAll(commandActions);
            }
        }
        if (actionsToChange != null) {
            // Need to change actions in existing actions
            for (Action a : actionsToChange) {
                if (a != null) {
                    a.putValue(Action.ACCELERATOR_KEY, shortcut);
                }
            }
        }
    }
    public synchronized Object getShortcut(String command) {
        return shorcuts.get(command);
    }

}
