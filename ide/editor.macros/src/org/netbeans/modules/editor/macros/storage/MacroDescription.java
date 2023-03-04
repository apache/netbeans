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

package org.netbeans.modules.editor.macros.storage;

import java.util.List;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.openide.util.Utilities;

/**
 *
 * @author Vita Stejskal
 */
public final class MacroDescription {
    
    public MacroDescription(String name, String code, String description, List<? extends MultiKeyBinding> shortcuts) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.shortcuts = shortcuts;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public List<? extends MultiKeyBinding> getShortcuts() {
        return shortcuts;
    }

    public @Override String toString() {
        return "EditorMacro[name='" + name + "', shortcuts=[" + shortcuts + "]"; //NOI18N
    }

    public @Override boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MacroDescription other = (MacroDescription) obj;
        if (!Utilities.compareObjects(this.name, other.name)) {
            return false;
        }
        if (!Utilities.compareObjects(this.code, other.code)) {
            return false;
        }
        if (!Utilities.compareObjects(this.description, other.description)) {
            return false;
        }
        if (!Utilities.compareObjects(this.shortcuts, other.shortcuts)) {
            return false;
        }
        return true;
    }

    public @Override int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 37 * hash + (this.code != null ? this.code.hashCode() : 0);
        hash = 37 * hash + (this.description != null ? this.description.hashCode() : 0);
        hash = 37 * hash + (this.shortcuts != null ? this.shortcuts.hashCode() : 0);
        return hash;
    }
    
    // ------------------------------------------
    // private implementation
    // ------------------------------------------

    private final String name;
    private final String code;
    private final String description;
    private final List<? extends MultiKeyBinding> shortcuts;
    
}
