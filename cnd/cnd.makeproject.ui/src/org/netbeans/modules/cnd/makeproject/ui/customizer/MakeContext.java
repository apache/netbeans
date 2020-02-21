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

package org.netbeans.modules.cnd.makeproject.ui.customizer;

import java.util.Set;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.WeakSet;

/**
 *
 */
public class MakeContext {

    public static enum Kind {
        Project,
        Folder,
        Item
    }

    public static interface Savable {
        void save();
    }

    private final Kind kind;
    private final Project project;
    private final ExecutionEnvironment env;
    private final Configuration[] selectedConfigurations;
    private SharedItemConfiguration[] items;
    private Folder[] folders;
    private JPanel container;
    private ConfigurationDescriptor configurationDescriptor;
    private final Set<Savable> listeners = new WeakSet<>();

    public MakeContext(Kind kind, Project project, ExecutionEnvironment env, Configuration[] selectedConfigurations){
        this.project = project;
        this.kind = kind;
        this.env = env;
        this.selectedConfigurations = selectedConfigurations;
    }

    public MakeContext setPanel(JPanel container) {
        this.container = container;
        return this;
    }

    public MakeContext setConfigurationDescriptor(ConfigurationDescriptor configurationDescriptor) {
        this.configurationDescriptor = configurationDescriptor;
        return this;
    }

    public MakeContext setFolders(Folder[] folders) {
        this.folders = folders;
        return this;
    }

    /*package*/MakeContext setSharedItem(SharedItemConfiguration[] items) {
        // 1 -> many
        this.items = items;
        return this;
    }
    /**
     * @return the kind
     */
    public Kind getKind() {
        return kind;
    }

    /**
     * @return the project
     */
    public Project getProject() {
        return project;
    }

    /**
     * @return the env
     */
    public ExecutionEnvironment getEnv() {
        return env;
    }

    /**
     * @return the selectedConfigurations
     */
    public Configuration[] getSelectedConfigurations() {
        return selectedConfigurations;
    }

    /**
     * @return items or NULL if items were not set
     */
    /*package*/ SharedItemConfiguration[] getItems() {
        return items;
    }

    /**
     * @return the folder
     */
    /*package*/ Folder[] getFolders() {
        return folders;
    }

    /**
     * @return the container
     */
    public JPanel getContainer() {
        return container;
    }

    /**
     * @return the configurationDescriptor
     */
    public ConfigurationDescriptor getConfigurationDescriptor() {
        return configurationDescriptor;
    }

    public void registerSavable(Savable listener){
        listeners.add(listener);
    }

    public void save() {
        listeners.forEach((listener) -> {
            listener.save();
        });
    }

    public boolean isCompilerConfiguration(){
        return ((MakeConfiguration) selectedConfigurations[0]).isCompileConfiguration();
    }

    public boolean isProc() {
        PredefinedToolKind itemTool = getItemsTool();
        if (itemTool == PredefinedToolKind.CCCompiler
                || itemTool == PredefinedToolKind.CCompiler) {
            for (SharedItemConfiguration item : items) {
                if (!ItemConfiguration.isProCFile(item.getItem(), itemTool)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    public PredefinedToolKind getItemsTool(){
        PredefinedToolKind result = null;
        for (SharedItemConfiguration item : items) {
            PredefinedToolKind current = getItemTool(item);
            if (result != null && current != result) {
                result = PredefinedToolKind.UnknownTool;
            } else {
                // it's ok to set null too
                result = current;
            }
        }
        return result;
    }
    
    private PredefinedToolKind getItemTool(SharedItemConfiguration item) {
        PredefinedToolKind tool = PredefinedToolKind.UnknownTool;
        CompilerSet compilerSet = null;

        // IG one item context -> many items
        for (int i = 0; i < selectedConfigurations.length; i++) {
            MakeConfiguration makeConfiguration = (MakeConfiguration) selectedConfigurations[i];
            CompilerSet compilerSet2 = makeConfiguration.getCompilerSet().getCompilerSet();
            ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration);
            if (itemConfiguration == null) {
                continue;
            }
            PredefinedToolKind tool2 = itemConfiguration.getTool();
            if (tool == PredefinedToolKind.UnknownTool && compilerSet == null) {
                // this is the first iteration
                // initialize goldens to compare with
                tool = tool2;
                compilerSet = compilerSet2;
            } else if (tool != tool2 || compilerSet != compilerSet2) {
                // if we found differences in tool kind of used 
                // compiler set, then tools are different; break
                tool = PredefinedToolKind.UnknownTool;
                break;
            }

            if ((isCompilerConfiguration() && !makeConfiguration.isCompileConfiguration()) ||
                (!isCompilerConfiguration() && makeConfiguration.isCompileConfiguration())) {
                tool = PredefinedToolKind.UnknownTool;
                break;
            }
        }
        return tool;
    }

    public boolean isQtMode() {
        boolean isQtMode = false;
        for (int i = 0; i < selectedConfigurations.length; i++) {
            MakeConfiguration makeConfiguration = (MakeConfiguration) selectedConfigurations[i];
            isQtMode |= makeConfiguration.isQmakeConfiguration();
        }
        return isQtMode;
    }

}
