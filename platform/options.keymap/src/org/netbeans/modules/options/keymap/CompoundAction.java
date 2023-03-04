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

import java.util.Map;
import org.netbeans.core.options.keymap.api.ShortcutAction;

/**
 *
 * @author Jan Jancura, David Strupl
 */
public class CompoundAction implements ShortcutAction {
    private static final String DEFAULT_PROVIDER = "EditorBridge";
    private Map<String, ShortcutAction> actions;

    public CompoundAction(Map<String, ShortcutAction> actions) {
        this.actions = actions;
    }
    
    /**
     * Use with care, invalidates hashcode.
     * @param mgr
     * @param ac 
     */
    void addAction(String mgr, ShortcutAction ac) {
        this.actions.put(mgr, ac);
    }

    public String getDisplayName () {
        ShortcutAction s = actions.get(DEFAULT_PROVIDER);
        if (s != null) {
            return s.getDisplayName();
}
        for (ShortcutAction sa: actions.values()) {
            String dn = sa.getDisplayName();
            if (dn != null) {
                return dn;
            }
        }
        return "";
    }

    public String getId () {
        ShortcutAction s = actions.get(DEFAULT_PROVIDER);
        if (s != null) {
            return s.getId();
        }
        for (ShortcutAction sa: actions.values()) {
            String id = sa.getId();
            if (id != null) {
                return id;
            }
        }
        return "<error>"; // TODO:
    }

    public String getDelegatingActionId () {
        ShortcutAction s = actions.get(DEFAULT_PROVIDER);
        if (s != null) {
            return s.getDelegatingActionId();
        }
        for (ShortcutAction sa: actions.values()) {
            String id = sa.getDelegatingActionId();
            if (id != null) {
                return id;
            }
        }
        return null; // TODO:
    }
    
    public boolean equals (Object o) {
        if (! (o instanceof CompoundAction)) {
            return false;
        }
        if (actions.get(DEFAULT_PROVIDER) != null) {
            return (getKeymapManagerInstance(DEFAULT_PROVIDER).equals(
                ((CompoundAction)o).getKeymapManagerInstance(DEFAULT_PROVIDER)
            ));
        }
        if (actions.keySet().isEmpty()) {
            return false;
        }
        String k = actions.keySet().iterator().next();
        return (getKeymapManagerInstance(k).equals(
                ((CompoundAction)o).getKeymapManagerInstance(k)
            ));
    }
    
    public int hashCode () {
        if (actions.get(DEFAULT_PROVIDER) != null) {
            return getKeymapManagerInstance(DEFAULT_PROVIDER).hashCode() * 2;
        }
        if (actions.keySet().isEmpty()) {
            return 0;
        }
        String k = actions.keySet().iterator().next();
        return actions.get(k).hashCode() * 2;
    }

    public ShortcutAction getKeymapManagerInstance(String keymapManagerName) {
        return actions.get(keymapManagerName);
    }
    
    public String toString() {
        return "CompoundAction[" + actions + "]";
    }
}
