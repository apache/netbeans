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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.gradle.GradleProject;
import static org.netbeans.modules.gradle.api.NbGradleProject.Quality.FALLBACK;
import org.netbeans.modules.gradle.cache.ProjectInfoDiskCache;
import org.netbeans.modules.gradle.options.GradleExperimentalSettings;

/**
 *
 * @author lkishalmi
 */
public class DiskCacheProjectLoader extends AbstractProjectLoader {
    private static final Logger LOG = Logger.getLogger(DiskCacheProjectLoader.class.getName());
    
    DiskCacheProjectLoader(ReloadContext ctx) {
        super(ctx);
    }

    @Override
    public GradleProject load() {
        ProjectInfoDiskCache cache = ProjectInfoDiskCache.get(ctx.project.getGradleFiles());
        LOG.log(Level.FINER, "Loaded from cache: {0}, valid: {1}", new Object[] { ctx.previous, cache.isValid() });
        if (cache.isCompatible()) {
            GradleProject prev = createGradleProject(ctx.project.getGradleFiles(), cache.loadData());
            LOG.log(Level.FINER, "Loaded from cache: {0}, valid: {1}", new Object[] { prev, cache.isValid() });
            if (GradleArtifactStore.getDefault().sanityCheckCachedProject(prev)) {
                if (cache.isValid()) {
                    updateSubDirectoryCache(prev);
                    return prev;
                } else {
                    return prev.invalidate("Disk cache data is invalid.");
                }
            }
        }
        return null;
    }

    @Override
    boolean isEnabled() {
        return ctx.aim.betterThan(FALLBACK) && !GradleExperimentalSettings.getDefault().isCacheDisabled();
    }

    @Override
    boolean needsTrust() {
        return false;
    }
}
