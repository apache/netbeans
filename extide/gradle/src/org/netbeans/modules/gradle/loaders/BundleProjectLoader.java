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

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.gradle.tooling.ProjectConnection;
import org.netbeans.modules.gradle.GradleProject;
import org.netbeans.modules.gradle.api.ModelFetcher;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.NbProjectInfo;
import org.netbeans.modules.gradle.cache.AbstractDiskCache.CacheEntry;
import org.netbeans.modules.gradle.cache.SubProjectDiskCache;
import org.netbeans.modules.gradle.cache.SubProjectDiskCache.SubProjectInfo;
import org.netbeans.modules.gradle.options.GradleExperimentalSettings;
import org.openide.util.Exceptions;

/**
 *
 * @author lkishalmi
 */
public class BundleProjectLoader extends AbstractProjectLoader {


    private CacheEntry<SubProjectInfo> structureCache;
    BundleProjectLoader(ReloadContext ctx) {
        super(ctx);
    }


    @Override
    GradleProject load() {
        File rootDir = ctx.project.getGradleFiles().getRootDir();
        if (structureCache == null) {
            SubProjectDiskCache spCache = SubProjectDiskCache.get(rootDir);
            structureCache = spCache.loadEntry();
        }
        if (structureCache == null || !structureCache.isValid()) {
            ModelCache modelCache = ModelCacheManager.getModelCache(rootDir, org.gradle.tooling.model.GradleProject.class, () -> new ModelCache(ctx.project, new ProjectStructureCachingDescriptor(rootDir)));
            try {
                modelCache.refreshAndWait();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            SubProjectDiskCache spCache = SubProjectDiskCache.get(rootDir);
            structureCache = spCache.loadEntry();
        }
        if (structureCache != null) {
            ModelCache modelCache = ModelCacheManager.getModelCache(rootDir, NbProjectInfo.class, () -> new ModelCache(ctx.project, new NbProjectInfoCachingDescriptor(structureCache.getData())));
            try {
                modelCache.refreshAndWait();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    @Override
    boolean isEnabled() {
        return ctx.getAim().betterThan(NbGradleProject.Quality.FALLBACK) && GradleExperimentalSettings.getDefault().isBundledLoading();
    }

}
