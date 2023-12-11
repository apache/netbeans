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

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.Model;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.execute.GradleCommandLine;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.modules.gradle.tooling.internal.ModelFetcher;
import static org.netbeans.modules.gradle.loaders.ModelCache.State.*;
import org.netbeans.modules.gradle.spi.GradleSettings;
import org.netbeans.modules.gradle.spi.loaders.GradlePluginProvider;
import org.openide.util.RequestProcessor;

/**
 *
 * @author lkishalmi
 */
public class ModelCache <T extends Model> {

    enum State { FREE, BUSY }

    static final RequestProcessor RP = new RequestProcessor(ModelCache.class);
    
    private static final Logger LOG = Logger.getLogger(ModelCache.class.getName());
    
    private State state = FREE;
    final ModelCachingDescriptor<T> descriptor;
    final Project project;

    private CountDownLatch barrier;

    public ModelCache(Project project, ModelCachingDescriptor<T> descriptor) {
        this.project = project;
        this.descriptor = descriptor;
    }

    public void refreshAndWait() throws InterruptedException {
        synchronized (this) {
            if (state == FREE) {
                barrier = new CountDownLatch(1);
                RP.submit(() -> load());
            }
        }
        barrier.await();
    }

    private void load() {
        synchronized (this) {
            if (state == State.BUSY) {
                throw new IllegalStateException("Chache is BUSY");
            } else {
                state = BUSY;
            }
        }
        try {
            List<String> filteredTargets = descriptor.getTargets().stream().filter((String target) -> descriptor.needsRefresh(target)).collect(Collectors.toList());
            if (!filteredTargets.isEmpty()) {
                final GradleCommandLine cmd = new GradleCommandLine(RunUtils.getCompatibleGradleDistribution(project), descriptor.gradleCommandLine(GradlePluginProvider.GradleRuntime.fromProject(project)));

                cmd.setFlag(GradleCommandLine.Flag.CONFIGURE_ON_DEMAND, GradleSettings.getDefault().isConfigureOnDemand());
                cmd.setFlag(GradleCommandLine.Flag.CONFIGURATION_CACHE, GradleSettings.getDefault().getUseConfigCache());

                ProjectConnection pconn = project.getLookup().lookup(ProjectConnection.class);

                ModelFetcher fetcher = new ModelFetcher();
                for (String target : filteredTargets) {
                     fetcher.modelAction(target, descriptor.getModelClass(), (T model) -> descriptor.onLoad(target, model));
                }
                long startTime = System.currentTimeMillis();
                fetcher.fetchModels(pconn, (launcher) -> {
                    cmd.configure(launcher);
                });
                fetcher.awaitTermination(10, TimeUnit.MINUTES);
                long endTime = System.currentTimeMillis();
                LOG.info("Loaded " + filteredTargets.size() + " targets for " + project + " in " + (endTime - startTime));
            }
        } catch (Exception ex) {
        } finally {
            synchronized (this) {
                state = FREE;
            }
            barrier.countDown();
        }
    }

    public synchronized State getState() {
        return state;
    }

}
