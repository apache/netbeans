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
package org.netbeans.modules.php.blade.editor.directives;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PluginDirectives {

    private Map<String, List<PluginDirective>> pluginDirectives = new HashMap<>();

    public PluginDirectives() {
        initLivewirePlugins();
    }

    private void initLivewirePlugins() {
        //hardcoded livewire
        List<PluginDirective> livewirePlugins = new ArrayList<>();

        livewirePlugins.add(new PluginDirective("@livewireStyles")); // NOI18N
        livewirePlugins.add(new PluginDirective("@bukStyles")); // NOI18N
        livewirePlugins.add(new PluginDirective("@livewireScripts")); // NOI18N
        livewirePlugins.add(new PluginDirective("@bukScripts")); // NOI18N
        livewirePlugins.add(new PluginDirective("@island", true)); // NOI18N
        livewirePlugins.add(new PluginDirective("@endisland", true)); // NOI18N
        livewirePlugins.add(new PluginDirective("@placeholder", true)); // NOI18N
        livewirePlugins.add(new PluginDirective("@endplaceholder", true)); // NOI18N
        livewirePlugins.add(new PluginDirective("@persist", true)); // NOI18N
        livewirePlugins.add(new PluginDirective("@endpersist", true)); // NOI18N
        livewirePlugins.add(new PluginDirective("@teleport", true)); // NOI18N
        livewirePlugins.add(new PluginDirective("@endteleport", true)); // NOI18N
        pluginDirectives.put("inertia", livewirePlugins); // NOI18N
    }

    public Map<String, List<PluginDirective>> getPluginDirectives() {
        return pluginDirectives;
    }

    public boolean isPluginDirective(String directiveName) {
        for (Map.Entry<String, List<PluginDirective>> entry : pluginDirectives.entrySet()) {
            for (PluginDirective directive : entry.getValue()) {
                if (directive.getName().equals(directiveName)) {
                    return true;
                }
            }
        }

        return false;
    }
    
    public static class PluginDirective {

        private final String name;
        private final boolean isBlockDirective;

        public PluginDirective(String name) {
            this(name, false);
        }

        public PluginDirective(String name, boolean isBlock) {
            this.name = name;
            this.isBlockDirective = isBlock;
        }

        public String getName() {
            return name;
        }

        public boolean isBlockDirective() {
            return isBlockDirective;
        }
    }
}
