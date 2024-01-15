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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.gradle.GradleProject;
import org.netbeans.modules.gradle.NbGradleProjectImpl;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.GradleReport;
import org.netbeans.modules.gradle.api.NbGradleProject;
import static org.netbeans.modules.gradle.api.NbGradleProject.Quality.EVALUATED;
import static org.netbeans.modules.gradle.api.NbGradleProject.Quality.FALLBACK;
import org.netbeans.modules.gradle.tooling.internal.NbProjectInfo.Report;
import org.netbeans.modules.gradle.api.execute.GradleCommandLine;
import org.netbeans.modules.gradle.cache.ProjectInfoDiskCache;
import org.netbeans.modules.gradle.cache.SubProjectDiskCache;
import org.netbeans.modules.gradle.options.GradleExperimentalSettings;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.netbeans.modules.gradle.spi.GradleSettings;
import org.netbeans.modules.gradle.spi.ProjectInfoExtractor;
import org.netbeans.modules.gradle.spi.loaders.GradlePluginProvider.GradleRuntime;
import org.openide.util.Lookup;

/**
 *
 * @author lkishalmi
 */
public abstract class AbstractProjectLoader {

    final ReloadContext ctx;

    AbstractProjectLoader(ReloadContext ctx) {
        this.ctx = ctx;
    }

    abstract GradleProject load();
 
    abstract boolean isEnabled();

    boolean needsTrust() {
        return true;
    }
    
    static final class ReloadContext {

        final NbGradleProjectImpl project;
        final GradleProject previous;
        final NbGradleProject.Quality aim;
        final GradleCommandLine cmd;
        final String description;

        public ReloadContext(NbGradleProjectImpl project, NbGradleProject.Quality aim, GradleCommandLine cmd, String description) {
            this.project = project;
            this.previous = project.isGradleProjectLoaded() ? project.projectWithQuality(null, FALLBACK, false, false) : FallbackProjectLoader.createFallbackProject(project.getGradleFiles());
            this.aim = aim;
            this.cmd = cmd;
            this.description = description;
        }

        public GradleProject getPrevious() {
            return previous;
        }

        public NbGradleProject.Quality getAim() {
            return aim;
        }
    }

    static GradleCommandLine injectNetBeansTooling(GradleCommandLine cmd, GradleRuntime rt) {
        GradleCommandLine ret = new GradleCommandLine(cmd);
        ret.setFlag(GradleCommandLine.Flag.CONFIGURE_ON_DEMAND, GradleSettings.getDefault().isConfigureOnDemand());
        ret.addParameter(GradleCommandLine.Parameter.INIT_SCRIPT, GradleDaemon.initScript(rt));
        ret.setStackTrace(GradleCommandLine.StackTrace.SHORT);
        ret.addProjectProperty("nbSerializeCheck", "true");
        return ret;
    }

    static GradleProject createGradleProject(GradleFiles gf, ProjectInfoDiskCache.QualifiedProjectInfo info) {
        Collection<? extends ProjectInfoExtractor> extractors = Lookup.getDefault().lookupAll(ProjectInfoExtractor.class);
        Map<Class, Object> results = new HashMap<>();
        Set<String> problems = new LinkedHashSet<>(info.getProblems());

        Map<String, Object> projectInfo = new HashMap<>(info.getInfo());
        projectInfo.putAll(info.getExt());

        for (ProjectInfoExtractor extractor : extractors) {
            ProjectInfoExtractor.Result result = extractor.extract(projectInfo, Collections.unmodifiableMap(results));
            problems.addAll(result.getProblems());
            for (Object extract : result.getExtract()) {
                results.put(extract.getClass(), extract);
            }

        }
        Set<GradleReport> reps = new LinkedHashSet<>();
        if (info.getReports() != null) {
            for (Report r : info.getReports()) {
                reps.add(LegacyProjectLoader.copyReport(r));
            }
        }
        for (String s : problems) {
            reps.add(GradleProject.createGradleReport(gf.getBuildScript().toPath(), s));
        }
        return new GradleProject(info.getQuality(), reps, results.values());

    }

    static void updateSubDirectoryCache(GradleProject gp) {
        if (gp.getQuality().atLeast(EVALUATED)) {
            GradleBaseProject baseProject = gp.getBaseProject();
            if (baseProject.isRoot() && !GradleExperimentalSettings.getDefault().isBundledLoading()) {
                // Bundled Gradle Project loading saves better information, do not overwrite that
                SubProjectDiskCache spCache = SubProjectDiskCache.get(baseProject.getRootDir());
                spCache.storeData(new SubProjectDiskCache.SubProjectInfo(baseProject));
            }
        }
    }

    static void saveCachedProjectInfo(ProjectInfoDiskCache.QualifiedProjectInfo data, GradleProject gp) {
        assert gp.getQuality().betterThan(FALLBACK) : "Never attempt to cache FALLBACK projects."; //NOi18N
        GradleFiles gf = new GradleFiles(gp.getBaseProject().getProjectDir(), true);
        ProjectInfoDiskCache.get(gf).storeData(data);
    }
}
