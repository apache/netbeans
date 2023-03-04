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
package org.netbeans.modules.javascript.nodejs.misc;

import java.net.URI;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.javascript.nodejs.file.PackageJson;
import org.netbeans.modules.javascript.nodejs.options.NodeJsOptions;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

public final class SharabilityQueryImpl implements SharabilityQueryImplementation2, PreferenceChangeListener {

    private final PackageJson packageJson;

    private volatile URI nodeModulesUri;
    private volatile Boolean versioningIgnored;


    private SharabilityQueryImpl(Project project) {
        assert project != null;
        packageJson = new PackageJson(project.getProjectDirectory());
    }

    private static SharabilityQueryImplementation2 create(Project project) {
        SharabilityQueryImpl sharabilityQuery = new SharabilityQueryImpl(project);
        NodeJsOptions nodeJsOptions = NodeJsOptions.getInstance();
        nodeJsOptions.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, sharabilityQuery, nodeJsOptions));
        return sharabilityQuery;
    }

    @ProjectServiceProvider(service = SharabilityQueryImplementation2.class, projectType = "org-netbeans-modules-web-clientproject") // NOI18N
    public static SharabilityQueryImplementation2 forHtml5Project(Project project) {
        return create(project);
    }

    @ProjectServiceProvider(service = SharabilityQueryImplementation2.class, projectType = "org-netbeans-modules-php-project") // NOI18N
    public static SharabilityQueryImplementation2 forPhpProject(Project project) {
        return create(project);
    }

    @ProjectServiceProvider(service = SharabilityQueryImplementation2.class, projectType = "org-netbeans-modules-web-project") // NOI18N
    public static SharabilityQueryImplementation2 forWebProject(Project project) {
        return create(project);
    }

    @ProjectServiceProvider(service = SharabilityQueryImplementation2.class, projectType = "org-netbeans-modules-maven") // NOI18N
    public static SharabilityQueryImplementation2 forMavenProject(Project project) {
        return create(project);
    }

    @Override
    public SharabilityQuery.Sharability getSharability(URI uri) {
        if (isVersioningIgnored()
                && uri.equals(getNodeModulesUri())) {
            return SharabilityQuery.Sharability.NOT_SHARABLE;
        }
        return SharabilityQuery.Sharability.UNKNOWN;
    }

    public URI getNodeModulesUri() {
        if (nodeModulesUri == null) {
            nodeModulesUri = Utilities.toURI(packageJson.getNodeModulesDir());
        }
        return nodeModulesUri;
    }

    public boolean isVersioningIgnored() {
        if (versioningIgnored == null) {
            versioningIgnored = NodeJsOptions.getInstance().isNpmIgnoreNodeModules();
        }
        return versioningIgnored;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (NodeJsOptions.NPM_IGNORE_NODE_MODULES.equals(evt.getKey())) {
            versioningIgnored = null;
        }
    }

}
