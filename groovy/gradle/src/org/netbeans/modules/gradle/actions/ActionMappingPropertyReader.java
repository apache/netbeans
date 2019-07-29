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


    private static final String PARAM_ARGS        = ".args";        //NOI18N
    private static final String PARAM_PLUGINS     = ".plugins";     //NOI18N
    private static final String PARAM_PRIORITY    = ".priority";    //NOI18N
    private static final String PARAM_RELOAD_ARGS = ".reload.args"; //NOI18N
    private static final String PARAM_RELOAD_RULE = ".reload.rule"; //NOI18N
    private static final String PARAM_REPEATABLE  = ".repeatable";  //NOI18N

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
                // args and/or reload.args shall be specified for defining an action.
                if (key.endsWith(PARAM_RELOAD_ARGS)) {
                    ret.add(key.substring(ACTION_PROP_PREFIX.length(), key.length() - PARAM_RELOAD_ARGS.length()));
                } else if (key.endsWith(PARAM_ARGS)) {
                    ret.add(key.substring(ACTION_PROP_PREFIX.length(), key.length() - PARAM_ARGS.length()));
                }
            }
        }
        return ret;
    }

    private ActionMapping createMapping(String name) {
        DefaultActionMapping ret = new DefaultActionMapping(name);
        String prefix = ACTION_PROP_PREFIX + name;
        ret.displayName = props.getProperty(ACTION_PROP_PREFIX + name);
        ret.args = props.getProperty(prefix + PARAM_ARGS);
        ret.reloadArgs = props.getProperty(prefix + PARAM_RELOAD_ARGS);
        String rule = props.getProperty(prefix + PARAM_RELOAD_RULE, ActionMapping.ReloadRule.DEFAULT.name());
        try {
            ret.reloadRule = ActionMapping.ReloadRule.valueOf(rule.trim());
        } catch (IllegalArgumentException ex) {

        }
        String repeatable = props.getProperty(prefix + PARAM_REPEATABLE);
        if (repeatable != null) {
            ret.repeatableAction = Boolean.valueOf(repeatable);
        }
        if (props.containsKey(prefix + PARAM_PLUGINS)) {
            String[] plugins = props.getProperty(prefix + PARAM_PLUGINS).split(",\\s"); //NOI18N
            ret.withPlugins = new LinkedHashSet<>();
            ret.withPlugins.addAll(Arrays.asList(plugins));
        }
        if (props.containsKey(prefix + PARAM_PRIORITY)) {
            try {
                ret.priority = Integer.parseInt(props.getProperty(prefix + PARAM_PRIORITY));
            } catch(NumberFormatException ex) {

            }
        }
        return ret;
    }
}
