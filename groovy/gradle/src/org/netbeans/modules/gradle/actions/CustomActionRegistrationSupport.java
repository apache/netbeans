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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.execute.ActionMapping;
import org.netbeans.modules.gradle.api.execute.ProjectActionMappingProvider;
import org.netbeans.modules.gradle.customizer.CustomActionMapping;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author lkishalmi
 */
public class CustomActionRegistrationSupport {
    private static final String NB_ACTIONS = "nb-actions.xml"; //NOI18N

    final Map<String, CustomActionMapping> customActions = new TreeMap<>();
    final Project project;

    public CustomActionRegistrationSupport(Project project) {
        this.project = project;
        ProjectActionMappingProvider mappingProvider = project.getLookup().lookup(ProjectActionMappingProvider.class);
        Set<String> customizedActions = mappingProvider.customizedActions();
        customizedActions.forEach((action) -> {
            CustomActionMapping mapping = new CustomActionMapping(mappingProvider.findMapping(action));
            customActions.put(action, mapping);
        });
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
        try {
            FileObject fo = project.getProjectDirectory().getFileObject(NB_ACTIONS);
            fo = fo != null ? fo : project.getProjectDirectory().createData(NB_ACTIONS);
            try (PrintWriter out = new PrintWriter(fo.getOutputStream(), true)) {
                out.println("<?xml version=\"1.0\"?>");
                out.println("<!DOCTYPE actions SYSTEM \"action-mapping.dtd\">");
                out.println("<actions>");
                for (CustomActionMapping mapping : customActions.values()) {
                    out.print("    <action name=\"" + mapping.getName() + "\"");
                    if (mapping.getName().startsWith(ActionMapping.CUSTOM_PREFIX)) {
                        out.print(" displayName=\"" + mapping.getDisplayName() + "\"");
                    }
                    if (!mapping.isRepeatable()) {
                        out.print("repeatable=\"false\"");
                    }
                    out.println(">");

                    out.println("        <args>" + mapping.getArgs() + "</args>");
                    if (mapping.getReloadRule() != ActionMapping.ReloadRule.DEFAULT) {
                        out.println("        <reload rule=\"" + mapping.getReloadRule().name() + "\"/>");
                    }
                    out.println("    </action>");
                }
                out.println("</actions>");
            } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                Exceptions.printStackTrace(ex);
            }
        } catch (IOException ex) {

        }
    }
    
}
