/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.project.ui.actions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import org.openide.util.WeakSet;

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
                commandActions = new WeakSet<Action>();
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
