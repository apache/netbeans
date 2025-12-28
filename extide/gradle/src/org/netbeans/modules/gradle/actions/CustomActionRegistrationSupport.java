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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.gradle.api.execute.ActionMapping;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.modules.gradle.customizer.CustomActionMapping;
import org.netbeans.modules.gradle.execute.ConfigurableActionProvider;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.NotificationDisplayer.Priority;
import org.openide.util.NbBundle;

/**
 * This entire class is single-thread, intended to be run from the GUI. Use Swing EDT
 * to interact with.
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
        final String id;
        Map<String, CustomActionMapping> customActions = new TreeMap<>();

        public ActionsHolder(GradleExecConfiguration config) {
            this.config = config;
            this.id = config == null ? GradleExecConfiguration.DEFAULT : config.getId();
        }
    }
    
    private void loadCustomizedActions(GradleExecConfiguration c) {
        String id;
        if (c != null) {
            id = c.getId();
        } else {
            id = GradleExecConfiguration.DEFAULT;
        }
        ActionsHolder h = configHolders.get(id);
        if (h != null) {
            customActions = h.customActions;
            return;
        } 
        final ActionsHolder nh = new ActionsHolder(c);
        
        ConfigurableActionProvider configP = project.getLookup().lookup(ConfigurableActionProvider.class);
        ProjectActionMappingProvider actionP = null;
        
        if (configP != null) {
            actionP = configP.findActionProvider(id);
        }
        if (actionP == null) {
            actionP = project.getLookup().lookup(ProjectActionMappingProvider.class);
        }
        if (actionP != null) {
            ProjectActionMappingProvider fa = actionP;
            Set<String> customizedActions = actionP.customizedActions();
            customizedActions.forEach((action) -> {
                ActionMapping ca = fa.findMapping(action);
                if (ca == null) {
                    ca = DefaultActionMapping.DISABLED;
                }
                CustomActionMapping mapping = new CustomActionMapping(ca, action);
                nh.customActions.put(action, mapping);
            });
        }
        configHolders.put(id, nh);
        customActions = nh.customActions;
    }
    
    public void setActiveConfiguration(GradleExecConfiguration cfg) {
        if (cfg == null) {
            cfg = ActionToTaskUtils.findProjectConfiguration(project);
        }
        if (Objects.equals(this.config, cfg)) {
            return;
        }
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
    
    @NbBundle.Messages({
        "ERR_ErrorSavingActions=Error saving build actions.",
        "# {0} - error detail message",
        "ERR_ErrorSavingActionsDetails=Errors encountered during save: {0}"
    })
    
    public void saveAndReportErrors() {
        try {
            save();
        } catch (IOException ex) {
            NotificationDisplayer.getDefault().notify(
                    Bundle.ERR_ErrorSavingActions(),
                    null, 
                    Bundle.ERR_ErrorSavingActionsDetails(ex.getLocalizedMessage()),
                    null, 
                    Priority.HIGH
            );
        }
    }
    
    public void save() throws IOException {
        IOException[] thrown = new IOException[1];
        
        ProjectManager.mutex().writeAccess(() -> {
            try {
                project.getProjectDirectory().getFileSystem().runAtomicAction(() -> {
                    for (Map.Entry<String, ActionsHolder> entry : configHolders.entrySet()) {
                        String s = entry.getKey();
                        ActionsHolder h = entry.getValue();
                        List<ActionMapping> actions = new ArrayList<>(h.customActions.values());
                        actions.sort(ActionMapping::compareTo);
                        try {
                            ActionPersistenceUtils.writeActions(project.getProjectDirectory(), s, actions);
                        } catch (IOException ex) {
                            if (thrown[0] == null) {
                                thrown[0] = ex;
                            } else {
                                thrown[0].addSuppressed(ex);
                            }
                        }
                    }
                });
            } catch (IOException ex) {
                thrown[0] = ex;
            }
        });
        if (thrown[0] != null) {
            throw thrown[0];
        }
    }
}
