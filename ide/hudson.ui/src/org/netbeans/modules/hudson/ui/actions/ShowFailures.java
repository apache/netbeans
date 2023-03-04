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
package org.netbeans.modules.hudson.ui.actions;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.netbeans.modules.hudson.ui.impl.HudsonFailureDisplayer;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

/**
 * Action to display test failures.
 */
public class ShowFailures extends AbstractAction implements ContextAwareAction {

    private static ShowFailures INSTANCE = null;
    private final Lookup context;

    /**
     * Get the shared instance from system filesystem.
     */
    public static ShowFailures getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ShowFailures();
        }
        return INSTANCE;
    }

    public ShowFailures() {
        this(Utilities.actionsGlobalContext());
    }

    @Messages("ShowFailures.label=Show Test Failures")
    private ShowFailures(Lookup context) {
        putValue(NAME, Bundle.ShowFailures_label());
        this.context = context;
    }

    /**
     * Check if failures for job build or maven module build can be shown. The
     * build has to be unstable, and a failure displayer has to be available.
     */
    private boolean canShowFailures(HudsonJobBuild build,
            HudsonMavenModuleBuild module) {

        if (module == null && !HudsonJobBuild.Result.UNSTABLE.equals(
                build.getResult())) {
            return false;
        } else if (module != null) {
            boolean failed = false;
            switch (module.getColor()) {
                case yellow:
                case yellow_anime:
                    failed = true;
            }
            if (!failed) {
                return false;
            }
        }
        return build.canShowFailures();
    }

    /**
     * Find maven module builds that are part of a job build but are not already
     * included in the context. It's used for prevention of duplicate opening of
     * test results.
     */
    private List<HudsonMavenModuleBuild> getExtraModuleBuilds(
            HudsonJobBuild build) {
        List<HudsonMavenModuleBuild> result =
                new LinkedList<HudsonMavenModuleBuild>();
        Collection<? extends HudsonMavenModuleBuild> alreadyIncludedBuilds =
                context.lookupAll(HudsonMavenModuleBuild.class);
        Set<String> alreadyIncludedURLs =
                new HashSet<String>(alreadyIncludedBuilds.size());
        for (HudsonMavenModuleBuild b : alreadyIncludedBuilds) {
            alreadyIncludedURLs.add(b.getUrl());
        }
        for (HudsonMavenModuleBuild m : build.getMavenModules()) {
            if (!alreadyIncludedURLs.contains(m.getUrl())) {
                result.add(m);
            }
        }
        return result;
    }

    /**
     * If there is at least one unstable build in the context, this action will
     * be enabled.
     */
    @Override
    public boolean isEnabled() {
        for (HudsonJobBuild job : context.lookupAll(HudsonJobBuild.class)) {
            if (canShowFailures(job, null)) {
                return true;
            }
        }
        for (HudsonMavenModuleBuild module
                : context.lookupAll(HudsonMavenModuleBuild.class)) {
            if (canShowFailures(module.getBuild(), module)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        for (HudsonJobBuild job : context.lookupAll(HudsonJobBuild.class)) {
            showFailures(job, null);
        }
        for (HudsonMavenModuleBuild module
                : context.lookupAll(HudsonMavenModuleBuild.class)) {
            showFailures(module.getBuild(), module);
        }
    }

    /**
     * Show failures in a job build or maven module build. If a maven module
     * build is not specified, but the job build contains some module build,
     * failures from all its module builds will be shown.
     */
    private void showFailures(HudsonJobBuild build,
            HudsonMavenModuleBuild moduleBuild) {

        if (!canShowFailures(build, moduleBuild)) {
            return;
        }
        if (moduleBuild != null) {
            if (moduleBuild.canShowFailures()) {
                moduleBuild.showFailures(new HudsonFailureDisplayer(moduleBuild));
            }
        } else if (build.canShowFailures()) {
            if (build.getMavenModules().isEmpty()) {
                build.showFailures(new HudsonFailureDisplayer(build));
            } else {
                for (HudsonMavenModuleBuild extraModule
                        : getExtraModuleBuilds(build)) {
                    extraModule.showFailures(
                            new HudsonFailureDisplayer(extraModule));
                }
            }
        }
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new ShowFailures(actionContext);
    }
}
