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

import org.netbeans.modules.gradle.spi.actions.ProjectActionMappingProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.gradle.api.execute.ActionMapping;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.modules.gradle.customizer.CustomActionMapping;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.openide.filesystems.FileObject;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.netbeans.modules.gradle.execute.ConfigurableActionProvider;

/**
 *
 * @author lkishalmi
 */
public class CustomActionRegistrationSupport {

    public static final String ACTION_PROP_PREFIX = "action."; //NOI18N

    final Project project;
    final Map<String, ActionsHolder>  configHolders = new HashMap<>();
    GradleExecConfiguration config;
    Map<String, CustomActionMapping> customActions = new TreeMap<>();
    

    public CustomActionRegistrationSupport(Project project) {
        this.project = project;
        config = ActionToTaskUtils.findProjectConfiguration(project);
        loadCustomizedActions(config);
    }
    
    class ActionsHolder {
        final GradleExecConfiguration config;
        Map<String, CustomActionMapping> customActions = new TreeMap<>();

        public ActionsHolder(GradleExecConfiguration config) {
            this.config = config;
        }
    }
    
    private void loadCustomizedActions(GradleExecConfiguration c) {
        ActionsHolder h = configHolders.get(c.getId());
        if (h != null) {
            customActions = h.customActions;
            return;
        } 
        final ActionsHolder nh = new ActionsHolder(c);
        
        ConfigurableActionProvider configP = project.getLookup().lookup(ConfigurableActionProvider.class);
        ProjectActionMappingProvider actionP = null;
        
        if (configP != null) {
            actionP = configP.findActionProvider(c.getId());
        }
        if (actionP == null) {
            actionP = project.getLookup().lookup(ProjectActionMappingProvider.class);
        }
        if (actionP != null) {
            ProjectActionMappingProvider fa = actionP;
            Set<String> customizedActions = actionP.customizedActions();
            customizedActions.forEach((action) -> {
                CustomActionMapping mapping = new CustomActionMapping(fa.findMapping(action));
                nh.customActions.put(action, mapping);
            });
        }
        configHolders.put(c.getId(), nh);
        customActions = h.customActions;
    }
    
    public void setActiveConfiguration(GradleExecConfiguration cfg) {
        this.config = cfg;
        loadCustomizedActions(cfg);
    }

    public String findNewCustonActionId() {
        int i = 1;
        String ret;
        do {
            ret = ActionMapping.CUSTOM_PREFIX + i++;
        } while (customActions.containsKey(ret));
        return ret;
    }

    public CustomActionMapping registerCustomAction(String name, String displayName, String args, ActionMapping.ReloadRule rule, boolean repeatable) {
        CustomActionMapping mapping = new CustomActionMapping(name);
        mapping.setDisplayName(displayName);
        mapping.setArgs(args);
        mapping.setReloadRule(rule);
        mapping.setRepeatable(repeatable);

        return registerCustomAction(mapping);
    }

    public CustomActionMapping registerCustomAction(CustomActionMapping mapping) {
        customActions.put(mapping.getName(), mapping);
        return mapping;
    }

    public CustomActionMapping registerCustomAction(String displayName, String args) {
        String name = getByDisplayName(displayName);
        if (name == null) {
            name = findNewCustonActionId();
        }
        return registerCustomAction(name, displayName, args, ActionMapping.ReloadRule.DEFAULT, true);
    }

    public CustomActionMapping unregisterCustomAction(String name) {
        return customActions.remove(name);
    }

    public CustomActionMapping getCustomAction(String name) {
        return customActions.get(name);
    }

    public Collection<CustomActionMapping> getCustomActions() {
        return Collections.unmodifiableCollection(customActions.values());
    }

    private String getByDisplayName(String displayName) {
        String ret = null;
        for (CustomActionMapping value : customActions.values()) {
            if (value.getDisplayName().equals(displayName)) {
                ret = value.getName();
                break;
            }
        }
        return ret;
    }

    public void save() {
        EditableProperties props = new EditableProperties(false);
        ProjectManager.mutex().writeAccess(() -> {
            try {
                FileObject fo = project.getProjectDirectory().getFileObject(GradleFiles.GRADLE_PROPERTIES_NAME);
                if (fo != null) {
                    try (InputStream is = fo.getInputStream()) {
                        props.load(is);
                    }
                }
                // Remove previously defined acltion, if any
                Iterator<String> it = props.keySet().iterator();
                while (it.hasNext()) {
                    if (it.next().startsWith(ACTION_PROP_PREFIX)) {
                        it.remove();
                    }
                }
                // Add new actions, if any
                for (CustomActionMapping mapping : customActions.values()) {
                    String prefix = ACTION_PROP_PREFIX + mapping.getName() + '.';
                    if (mapping.getName().startsWith(ActionMapping.CUSTOM_PREFIX)) {
                        props.setProperty(ACTION_PROP_PREFIX + mapping.getName(), mapping.getDisplayName());
                    }
                    if (!mapping.getArgs().isEmpty()) {
                        props.setProperty(prefix + "args", mapping.getArgs()); //NOI18N
                    }
                    if (!mapping.getReloadArgs().isEmpty()) {
                        props.setProperty(prefix + "reload.args", mapping.getReloadArgs()); //NOI18N
                    }
                    if (mapping.getReloadRule() != ActionMapping.ReloadRule.DEFAULT) {
                        props.setProperty(prefix + "reload.rule", mapping.getReloadRule().name()); //NOI18N
                    }
                    if (!mapping.isRepeatable()) {
                        props.setProperty(prefix + "repeatable", "false"); //NOI18N
                    }
                }

                if ((fo != null) && props.isEmpty()) {
                    fo.delete();
                }
                if ((fo == null) && !props.isEmpty()) {
                    fo = project.getProjectDirectory().createData(GradleFiles.GRADLE_PROPERTIES_NAME);
                }
                if ((fo != null) && !props.isEmpty()) {
                    try (OutputStream os = fo.getOutputStream()) {
                        props.store(os);
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }

}
