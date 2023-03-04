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
package org.netbeans.modules.javascript.bower.misc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.bower.file.BowerJson;
import org.netbeans.modules.javascript.bower.util.BowerUtils;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.util.RequestProcessor;

public final class BowerLibrariesUsageLogger implements PropertyChangeListener {

    private static final RequestProcessor RP = new RequestProcessor(BowerLibrariesUsageLogger.class);

    private final BowerJson bowerJson;


    private BowerLibrariesUsageLogger(Project project) {
        assert project != null;
        bowerJson = new BowerJson(project.getProjectDirectory());
    }

    void startListening() {
        bowerJson.addPropertyChangeListener(this);
    }

    void stopListening() {
        bowerJson.removePropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (BowerJson.PROP_DEPENDENCIES.equals(propertyName)
                || BowerJson.PROP_DEV_DEPENDENCIES.equals(propertyName)) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    logLibraries();
                }
            });
        }
    }

    void logLibraries() {
        BowerJson.BowerDependencies dependencies = bowerJson.getDependencies();
        logLibraries("REGULAR", dependencies.dependencies); // NOI18N
        logLibraries("DEVELOPMENT", dependencies.devDependencies); // NOI18N
    }

    private void logLibraries(String type, Map<String, String> dependencies) {
        for (Map.Entry<String, String> dependency : dependencies.entrySet()) {
            BowerUtils.logUsageBowerLibrary(type, dependency.getKey(), dependency.getValue());
        }
    }

    //~ Inner classes

    // we need this class mainly to instantiate usage logger (it is lazy so someone needs to lookup it from project)
    private static final class BowerProjectOpenedHook extends ProjectOpenedHook {

        private final Project project;


        public BowerProjectOpenedHook(Project project) {
            assert project != null;
            this.project = project;
        }

        @Override
        protected void projectOpened() {
            getUsageLogger().startListening();
        }

        @Override
        protected void projectClosed() {
            getUsageLogger().stopListening();
        }

        private BowerLibrariesUsageLogger getUsageLogger() {
            BowerLibrariesUsageLogger usageLogger = project.getLookup().lookup(BowerLibrariesUsageLogger.class);
            assert usageLogger != null : "Usage logger must be found in lookup of: " + project.getClass().getName();
            return usageLogger;
        }

    }

    //~ Factories

    @ProjectServiceProvider(service = BowerLibrariesUsageLogger.class, projectTypes = {
        @LookupProvider.Registration.ProjectType(id = "org-netbeans-modules-web-clientproject"), // NOI18N
        @LookupProvider.Registration.ProjectType(id = "org-netbeans-modules-php-project"), // NOI18N
        @LookupProvider.Registration.ProjectType(id = "org-netbeans-modules-web-project"), // NOI18N
        @LookupProvider.Registration.ProjectType(id = "org-netbeans-modules-maven"), // NOI18N
    })
    public static BowerLibrariesUsageLogger usageLogger(Project project) {
        return new BowerLibrariesUsageLogger(project);
    }

    @ProjectServiceProvider(service = ProjectOpenedHook.class, projectTypes = {
        @LookupProvider.Registration.ProjectType(id = "org-netbeans-modules-web-clientproject"), // NOI18N
        @LookupProvider.Registration.ProjectType(id = "org-netbeans-modules-php-project"), // NOI18N
        @LookupProvider.Registration.ProjectType(id = "org-netbeans-modules-web-project"), // NOI18N
        @LookupProvider.Registration.ProjectType(id = "org-netbeans-modules-maven"), // NOI18N
    })
    public static ProjectOpenedHook projectOpenedHook(Project project) {
        return new BowerProjectOpenedHook(project);
    }

}
