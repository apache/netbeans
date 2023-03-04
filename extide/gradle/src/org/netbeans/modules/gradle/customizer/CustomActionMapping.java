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

package org.netbeans.modules.gradle.customizer;

import org.netbeans.modules.gradle.api.execute.ActionMapping;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Laszlo Kishalmi
 */
public class CustomActionMapping implements ActionMapping {

    private final ActionMapping original;
    private final String name;
    private String displayName;
    private String args;
    private ReloadRule reloadRule = ReloadRule.DEFAULT;
    private boolean repeatable = true;

    public CustomActionMapping(String name) {
        original = null;
        this.name = name;
    }


    public CustomActionMapping(ActionMapping original, String name) {
        this.original = original;
        this.name = original.getName() == null ? name : original.getName();
        displayName = original.getDisplayName();
        args = original.getArgs();
        reloadRule = original.getReloadRule();
        repeatable = original.isRepeatable();
    }

    public boolean isChanged() {
        if (original == null) return true;
        boolean ret = getDisplayName().equals(original.getDisplayName());
        ret &= getArgs().equals(original.getArgs());
        ret &= getReloadRule().equals(original.getReloadRule());
        ret &= isRepeatable() == original.isRepeatable();

        return ! ret;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName != null ? displayName : name;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getArgs() {
        return args != null ? args : "";
    }

    public void setArgs(String args) {
        this.args = args;
    }

    @Override
    public ReloadRule getReloadRule() {
        return reloadRule;
    }

    public void setReloadRule(ReloadRule reloadRule) {
        this.reloadRule = reloadRule;
    }

    @Override
    public boolean isRepeatable() {
        return repeatable;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    @Override
    public String getReloadArgs() {
        return "";
    }

    @Override
    public boolean isApplicable(Set<String> plugins) {
        return true;
    }

    @Override
    public int compareTo(ActionMapping o) {
        return name.compareTo(o.getName());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CustomActionMapping other = (CustomActionMapping) obj;
        return Objects.equals(this.name, other.name);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
