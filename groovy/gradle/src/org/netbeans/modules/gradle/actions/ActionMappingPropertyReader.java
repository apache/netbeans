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
package org.netbeans.modules.gradle.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import static org.netbeans.modules.gradle.actions.CustomActionRegistrationSupport.ACTION_PROP_PREFIX;
import org.netbeans.modules.gradle.api.execute.ActionMapping;

/**
 *
 * @author lkishalmi
 */
public final class ActionMappingPropertyReader {

    final Properties props;
    ActionMappingPropertyReader(Properties props) {
        this.props = props;
    }

    public static Set<ActionMapping> loadMappings(Properties props) {
        ActionMappingPropertyReader reader = new ActionMappingPropertyReader(props);
        return Collections.unmodifiableSet(reader.buildMappings());
    }

    private Set<ActionMapping> buildMappings() {
        Set<ActionMapping> mappings = new HashSet<>();
        for (String actionName : getActionNames()) {
            mappings.add(createMapping(actionName));
        }
        return mappings;
    }

    private Set<String> getActionNames() {
        Set<String> ret = new HashSet<>();
        for (String key : props.stringPropertyNames()) {
            if (key.startsWith(ACTION_PROP_PREFIX)) {
                int dot = key.indexOf('.', ACTION_PROP_PREFIX.length() + 1);
                if (dot > 0) {
                    String name = key.substring(ACTION_PROP_PREFIX.length(), dot);
                    ret.add(name);
                }
            }
        }
        return ret;
    }

    private ActionMapping createMapping(String name) {
        DefaultActionMapping ret = new DefaultActionMapping(name);
        String prefix = ACTION_PROP_PREFIX + name + '.';
        ret.displayName = props.getProperty(ACTION_PROP_PREFIX + name);
        ret.args = props.getProperty(prefix + "args");
        ret.reloadArgs = props.getProperty(prefix + "reload.args");
        String rule = props.getProperty(prefix + "reload.rule", ActionMapping.ReloadRule.DEFAULT.name());
        try {
            ret.reloadRule = ActionMapping.ReloadRule.valueOf(rule.trim());
        } catch (IllegalArgumentException ex) {

        }
        String repeatable = props.getProperty(prefix + "repeatable");
        if (repeatable != null) {
            ret.repeatableAction = Boolean.valueOf(repeatable);
        }
        if (props.containsKey(prefix + "plugins")) {
            String[] plugins = props.getProperty(prefix + "plugins").split(",\\s");
            ret.withPlugins = new LinkedHashSet<>();
            ret.withPlugins.addAll(Arrays.asList(plugins));
        }
        if (props.containsKey(prefix + "priority")) {
            try {
                ret.priority = Integer.parseInt(props.getProperty(prefix + "priority"));
            } catch(NumberFormatException ex) {

            }
        }
        return ret;
    }
}
