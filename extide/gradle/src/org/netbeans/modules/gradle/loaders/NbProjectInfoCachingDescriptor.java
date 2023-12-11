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

import java.util.Set;
import org.netbeans.modules.gradle.GradleProjectStructure;
import org.netbeans.modules.gradle.api.NbGradleProject.Quality;
import org.netbeans.modules.gradle.tooling.internal.NbProjectInfo;
import org.netbeans.modules.gradle.api.execute.GradleCommandLine;
import org.netbeans.modules.gradle.cache.ProjectInfoDiskCache;
import org.netbeans.modules.gradle.spi.GradleFiles;

import static org.netbeans.modules.gradle.api.NbGradleProject.Quality.*;
import org.netbeans.modules.gradle.spi.loaders.GradlePluginProvider.GradleRuntime;

/**
 *
 * @author lkishalmi
 */
public final class NbProjectInfoCachingDescriptor implements ModelCachingDescriptor<NbProjectInfo> {

    final GradleProjectStructure structure;

    public NbProjectInfoCachingDescriptor(GradleProjectStructure structure) {
        this.structure = structure;
    }


    @Override
    public Class<NbProjectInfo> getModelClass() {
        return NbProjectInfo.class;
    }

    @Override
    public Set<String> getTargets() {
        return structure.getProjectPaths();
    }

    @Override
    public GradleCommandLine gradleCommandLine(GradleRuntime rt) {
        return AbstractProjectLoader.injectNetBeansTooling(new GradleCommandLine(), rt);
    }

    @Override
    public void onLoad(String target, NbProjectInfo model) {
        Quality quality = model.hasException() ? SIMPLE : FULL_ONLINE;
        ProjectInfoDiskCache.QualifiedProjectInfo qinfo = new ProjectInfoDiskCache.QualifiedProjectInfo(quality, model);
        GradleFiles gf = new GradleFiles(structure.getProjectDir(target), true);
        ProjectInfoDiskCache.get(gf).storeData(qinfo);
    }

    @Override
    public void onError(String target, Exception ex) {
    }

    @Override
    public boolean needsRefresh(String target) {
        GradleFiles gf = new GradleFiles(structure.getProjectDir(target), true);
        return ! ProjectInfoDiskCache.get(gf).isValid();
    }

}
