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
package org.netbeans.modules.php.composer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.composer.files.ComposerJson;
import org.netbeans.modules.php.composer.util.ComposerUtils;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.util.RequestProcessor;

public final class ComposerLibrariesUsageLogger implements PropertyChangeListener {

    private static final RequestProcessor RP = new RequestProcessor(ComposerLibrariesUsageLogger.class);

    private final Project project;

    // @GuardedBy("this")
    private ComposerJson composerJson;


    private ComposerLibrariesUsageLogger(Project project) {
        assert project != null;
        this.project = project;
    }

    void startListening() {
        getComposerJson().addPropertyChangeListener(this);
    }

    void stopListening() {
        getComposerJson().removePropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (ComposerJson.PROP_REQUIRE.equals(propertyName)
                || ComposerJson.PROP_REQUIRE_DEV.equals(propertyName)) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    logLibraries();
                }
            });
        }
    }

    void logLibraries() {
        ComposerJson.ComposerDependencies dependencies = getComposerJson().getDependencies();
        logLibraries("REGULAR", dependencies.dependencies); // NOI18N
        logLibraries("DEVELOPMENT", dependencies.devDependencies); // NOI18N
    }

    private void logLibraries(String type, Map<String, String> dependencies) {
        for (Map.Entry<String, String> dependency : dependencies.entrySet()) {
            ComposerUtils.logUsageComposerLibrary(type, dependency.getKey(), dependency.getValue());
        }
    }

    private synchronized ComposerJson getComposerJson() {
        assert Thread.holdsLock(this);
        if (composerJson == null) {
            PhpModule phpModule = PhpModule.Factory.lookupPhpModule(project);
            assert phpModule != null : "PHP module must be found in " + project.getClass().getName();
            composerJson = new ComposerJson(ComposerUtils.getComposerWorkDir(phpModule));
        }
        return composerJson;
    }

    //~ Inner classes

    // we need this class mainly to instantiate usage logger (it is lazy so someone needs to lookup it from project)
    private static final class ComposerProjectOpenedHook extends ProjectOpenedHook {

        private final Project project;


        public ComposerProjectOpenedHook(Project project) {
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

        private ComposerLibrariesUsageLogger getUsageLogger() {
            ComposerLibrariesUsageLogger usageLogger = project.getLookup().lookup(ComposerLibrariesUsageLogger.class);
            assert usageLogger != null : "Usage logger must be found in lookup of: " + project.getClass().getName();
            return usageLogger;
        }

    }

    //~ Factories

    @ProjectServiceProvider(service = ComposerLibrariesUsageLogger.class, projectType = "org-netbeans-modules-php-project")
    public static ComposerLibrariesUsageLogger usageLogger(Project project) {
        return new ComposerLibrariesUsageLogger(project);
    }

    @ProjectServiceProvider(service = ProjectOpenedHook.class, projectType = "org-netbeans-modules-php-project")
    public static ProjectOpenedHook projectOpenedHook(Project project) {
        return new ComposerProjectOpenedHook(project);
    }

}
