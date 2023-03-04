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

package org.netbeans.modules.options.keymap;

import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.openide.util.NbBundle;

/**
 * Class holding an action instance and providing displayname
 * Stored in keymap table
 * @author Max Sauer
 */
public class ActionHolder {
    private ShortcutAction action;
    private boolean isAlternative;

    public ActionHolder(ShortcutAction action, boolean isAlternative) {
        this.action = action;
        this.isAlternative = isAlternative;
    }

    public ShortcutAction getAction() {
        return action;
    }

    public boolean isAlternative() {
        return isAlternative;
    }

    @Override
    public String toString() {
        String displayName = action.getDisplayName();
        return isAlternative ? displayName + " " + NbBundle.getMessage(ActionHolder.class, "Alternative_Shortcut") : displayName; //NOI18N
    }

}
