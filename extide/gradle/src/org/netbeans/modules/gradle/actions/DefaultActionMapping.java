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

package org.netbeans.modules.gradle.actions;

import org.netbeans.modules.gradle.api.execute.ActionMapping;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Laszlo Kishalmi
 */
public class DefaultActionMapping implements ActionMapping {

    String name;
    String displayName;
    String args;
    String reloadArgs;
    ReloadRule reloadRule = ReloadRule.DEFAULT;
    
    Set<String> withPlugins = Collections.<String>emptySet();
    
    boolean repeatableAction = true;
    
    int priority;

    protected DefaultActionMapping() {
    }

    protected DefaultActionMapping(String name) {
        this.name = name;
    }

    protected DefaultActionMapping(String name, String args) {
        this.name = name;
        this.args = args;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName != null ? displayName : name;
    }

    @Override
    public String getArgs() {
        return args != null ? args : "";
    }

    @Override
    public ReloadRule getReloadRule() {
        return reloadRule;
    }

    @Override
    public String getReloadArgs() {
        return reloadArgs != null ? reloadArgs : "";
    }

    @Override
    public boolean isApplicable(Set<String> plugins) {
        return plugins.containsAll(withPlugins);
    }

    @Override
    public boolean isRepeatable() {
        return repeatableAction;
    }

    private int strength() {
        return withPlugins.size();
    }
    
    @Override
    public int compareTo(ActionMapping o) {
        int ret = getName().compareTo(o.getName());
        if (o instanceof DefaultActionMapping) {
            DefaultActionMapping d = (DefaultActionMapping) o;
            ret = ret != 0 ? ret : priority - d.priority;
            ret = ret != 0 ? ret : strength() - d.strength();
        }
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.name);
        hash = 41 * hash + Objects.hashCode(this.withPlugins);
        hash = 41 * hash + this.priority;
        return hash;
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
        final DefaultActionMapping other = (DefaultActionMapping) obj;
        if (this.repeatableAction != other.repeatableAction) {
            return false;
        }
        if (this.priority != other.priority) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.args, other.args)) {
            return false;
        }
        if (!Objects.equals(this.reloadArgs, other.reloadArgs)) {
            return false;
        }
        if (this.reloadRule != other.reloadRule) {
            return false;
        }
        return Objects.equals(this.withPlugins, other.withPlugins);
    }

    public static DefaultActionMapping DISABLED = new DefaultActionMapping();
    
    static {
        DISABLED.reloadRule =  ReloadRule.NEVER;
    }
}
