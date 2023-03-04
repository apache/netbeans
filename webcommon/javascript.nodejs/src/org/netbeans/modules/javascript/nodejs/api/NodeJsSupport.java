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
package org.netbeans.modules.javascript.nodejs.api;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.options.NodeJsOptions;
import org.netbeans.modules.javascript.nodejs.preferences.NodeJsPreferences;
import org.netbeans.modules.javascript.nodejs.ui.customizer.NodeJsCustomizerProvider;
import org.netbeans.modules.javascript.nodejs.ui.options.NodeJsOptionsPanelController;
import org.openide.util.Parameters;

/**
 * Node.js support.
 * @since 0.15
 */
public final class NodeJsSupport {

    private static final NodeJsSupport INSTANCE = new NodeJsSupport();


    private NodeJsSupport() {
    }

    /**
     * Gets instance of node.js support.
     * @return instance of node.js support
     */
    public static NodeJsSupport getInstance() {
        return INSTANCE;
    }

    /**
     * Checks whether node.js support is present and enabled in the given project.
     * @param project project to be checked
     * @return {@code true} if node.js support is present and enabled in the given project, {@code false} otherwise
     */
    public boolean isEnabled(@NonNull Project project) {
        Parameters.notNull("project", project); // NOI18N
        org.netbeans.modules.javascript.nodejs.platform.NodeJsSupport projectNodeJsSupport = getProjectNodeJsSupport(project);
        return projectNodeJsSupport != null
                && projectNodeJsSupport.getPreferences().isEnabled();
    }

    /**
     * Gets file path (<b>possibly with parameters!</b>) representing <tt>node</tt> executable of the given project
     * or {@code null} if not set/found. If the project is {@code null}, the default <tt>node</tt>
     * path is returned (path set in IDE Options).
     * @param project project to get <tt>node</tt> for, can be {@code null}
     * @return file path (<b>possibly with parameters!</b>) representing <tt>node</tt> executable, can be {@code null} if not set/found
     */
    @CheckForNull
    public String getNode(@NullAllowed Project project) {
        if (project == null) {
            return getGlobalNode();
        }
        org.netbeans.modules.javascript.nodejs.platform.NodeJsSupport projectNodeJsSupport = getProjectNodeJsSupport(project);
        if (projectNodeJsSupport == null) {
            return getGlobalNode();
        }
        NodeJsPreferences preferences = projectNodeJsSupport.getPreferences();
        if (!preferences.isEnabled()
                || preferences.isDefaultNode()) {
            return getGlobalNode();
        }
        return preferences.getNode();
    }

    /**
     * Opens <tt>node</tt> settings for the given project - if project specific <tt>node</tt> is used
     * (and node.js is enabled in the given project), Project Properties dialog is opened (otherwise
     * IDE Options). If project is {@code null}, opens IDE Options.
     * @param project project to be used, can be {@code null}
     */
    public void openNodeSettings(@NullAllowed Project project) {
        if (project == null) {
            openNodeJsOptions();
            return;
        }
        org.netbeans.modules.javascript.nodejs.platform.NodeJsSupport projectNodeJsSupport = getProjectNodeJsSupport(project);
        if (projectNodeJsSupport == null) {
            openNodeJsOptions();
            return;
        }
        NodeJsPreferences preferences = projectNodeJsSupport.getPreferences();
        if (preferences.isEnabled()
                && !preferences.isDefaultNode()) {
            NodeJsCustomizerProvider.openCustomizer(project, NodeJsCustomizerProvider.CUSTOMIZER_IDENT);
            return;
        }
        openNodeJsOptions();
    }

    @CheckForNull
    private org.netbeans.modules.javascript.nodejs.platform.NodeJsSupport getProjectNodeJsSupport(Project project) {
        assert project != null;
        return project.getLookup().lookup(org.netbeans.modules.javascript.nodejs.platform.NodeJsSupport.class);
    }

    private void openNodeJsOptions() {
        OptionsDisplayer.getDefault().open(NodeJsOptionsPanelController.OPTIONS_PATH);
    }

    private String getGlobalNode() {
        return NodeJsOptions.getInstance().getNode();
    }

}
