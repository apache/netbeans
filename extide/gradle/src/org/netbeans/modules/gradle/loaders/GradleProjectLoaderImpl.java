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
package org.netbeans.modules.gradle.loaders;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.GradleProject;
import org.netbeans.modules.gradle.GradleProjectLoader;
import org.netbeans.modules.gradle.NbGradleProjectImpl;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.execute.GradleCommandLine;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.modules.gradle.options.GradleExperimentalSettings;
import org.openide.util.NbBundle;

/**
 *
 * @author lkishalmi
 */
public class GradleProjectLoaderImpl implements GradleProjectLoader {

    final Project project;
    private static final Logger LOGGER = Logger.getLogger(GradleProjectLoaderImpl.class.getName());

    public GradleProjectLoaderImpl(Project project) {
        this.project = project;
    }

    @Override
    @NbBundle.Messages({
        "ERR_ProjectNotTrusted=Gradle execution is not trusted on this project."
    })
    public GradleProject loadProject(NbGradleProject.Quality aim, String descriptionOpt, boolean ignoreCache, boolean interactive, String... args) {
        LOGGER.info("Load aiming " +aim + " for "+ project);
        GradleCommandLine cmd = new GradleCommandLine(args);
        AbstractProjectLoader.ReloadContext ctx = new AbstractProjectLoader.ReloadContext((NbGradleProjectImpl) project, aim, cmd, descriptionOpt);
        LOGGER.log(Level.FINER, "Load context: project = {0}, prev = {1}, aim = {2}, args = {3}", new Object[] { 
            project, ctx.previous, aim, cmd});
        List<AbstractProjectLoader> loaders = new LinkedList<>();

        if (!ignoreCache) loaders.add(new DiskCacheProjectLoader(ctx));
        if (GradleExperimentalSettings.getDefault().isBundledLoading()) {
            loaders.add(new BundleProjectLoader(ctx));
            loaders.add(new DiskCacheProjectLoader(ctx));
        }
        loaders.add(new LegacyProjectLoader(ctx));
        loaders.add(new FallbackProjectLoader(ctx));

        Boolean trust = null;

        GradleProject best = null;
        GradleProject ret = null;
        for (AbstractProjectLoader loader : loaders) {
            if (loader.isEnabled()) {
                if (loader.needsTrust()) {
                    if (trust == null) {
                        trust = RunUtils.isProjectTrusted(ctx.project, interactive);
                    }
                    if (trust) {
                        ret = loader.load();
                        LOGGER.log(Level.FINER, "Loaded with trusted loader {0} -> {1}", new Object[] { loader, ret });
                    } else {
                        ret = ctx.getPrevious();
                        if (ret != null) {
                            ret = ret.invalidate(Bundle.ERR_ProjectNotTrusted());
                        }
                        LOGGER.log(Level.FINER, "Execution not allowed, invalidated {0}", ret);
                    }
                } else {
                    ret = loader.load();
                    LOGGER.log(Level.FINER, "Loaded with loader {0} -> {1}", new Object[] { loader, ret });
                }
                if (ret != null) {
                    if (best == null || best.getQuality().notBetterThan(ret.getQuality())) {
                        best = ret;
                    }
                    if (ret.getQuality().atLeast(aim)) {
                        // We have the quality we are looking for, let's be happy with that
                        break;
                    }
                }
            } else {
                LOGGER.log(Level.FINER, "Loaded disabled: {0}", loader);
            }
        }
        return best;
    }
}
