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
package org.netbeans.modules.javascript.nodejs.exec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;

/**
 * Holds information about (maybe) running node.js processes.
 */
@ProjectServiceProvider(
        service = NodeProcesses.class,
        projectType = {
            "org-netbeans-modules-web-clientproject", // NOI18N
            "org-netbeans-modules-php-project", // NOI18N
            "org-netbeans-modules-web-project", // NOI18N
            "org-netbeans-modules-maven", // NOI18N
        }
)
public final class NodeProcesses {

    private final Map<String, RunInfo> npmScripts = new ConcurrentHashMap<>();

    private volatile RunInfo projectRun = RunInfo.none();


    public NodeProcesses() {
    }

    public static NodeProcesses forProject(Project project) {
        NodeProcesses processes = project.getLookup().lookup(NodeProcesses.class);
        assert processes != null : "NodeProcesses should be found in project " + project.getClass().getName() + " (lookup: " + project.getLookup() + ")";
        return processes;
    }

    public void stop() {
        if (projectRun.isRunning()) {
            projectRun.stop();
        }
        for (RunInfo runInfo : npmScripts.values()) {
            if (runInfo.isRunning()) {
                runInfo.stop();
            }
        }
        npmScripts.clear();
    }

    public RunInfo getProjectRun() {
        assert projectRun != null;
        return projectRun;
    }

    public void setProjectRun(RunInfo projectRun) {
        assert projectRun != null;
        this.projectRun = projectRun;
    }

    public RunInfo getNpmScript(String script) {
        assert script != null;
        RunInfo info = npmScripts.get(script);
        if (info != null) {
            return info;
        }
        return RunInfo.none();
    }

    public void setNpmScript(String script, RunInfo runInfo) {
        assert script != null;
        assert runInfo != null;
        npmScripts.put(script, runInfo);
    }

    //~ Inner classes

    public static final class RunInfo {

        private final AtomicReference<Future<Integer>> currentNodeTask;
        private final boolean debug;


        private RunInfo(@NullAllowed AtomicReference<Future<Integer>> currentNodeTask, boolean debug) {
            this.currentNodeTask = currentNodeTask;
            this.debug = debug;
        }

        public static RunInfo run(@NullAllowed AtomicReference<Future<Integer>> currentNodeTask) {
            return new RunInfo(currentNodeTask, false);
        }

        public static RunInfo debug(@NullAllowed AtomicReference<Future<Integer>> currentNodeTask) {
            return new RunInfo(currentNodeTask, true);
        }

        public static RunInfo none() {
            return new RunInfo(null, false);
        }

        public boolean isDebug() {
            return debug;
        }

        public boolean isRunning() {
            Future<Integer> nodeTask = getCurrentNodeTask();
            return nodeTask != null
                    && !nodeTask.isDone();
        }

        public void stop() {
            Future<Integer> nodeTask = getCurrentNodeTask();
            assert nodeTask != null;
            nodeTask.cancel(true);
            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {
                // noop
            }
        }

        @CheckForNull
        private Future<Integer> getCurrentNodeTask() {
            if (currentNodeTask == null) {
                return null;
            }
            return currentNodeTask.get();
        }

    }

    @ProjectServiceProvider(
            service = ProjectOpenedHook.class,
            projectType = {
                "org-netbeans-modules-web-clientproject", // NOI18N
                "org-netbeans-modules-php-project", // NOI18N
                "org-netbeans-modules-web-project", // NOI18N
                "org-netbeans-modules-maven", // NOI18N
            }
    )
    public static class ProjectOpenedHookImpl extends ProjectOpenedHook {

        private final Project project;


        public ProjectOpenedHookImpl(Project project) {
            this.project = project;
        }

        @Override
        protected void projectOpened() {
            // noop
        }

        @Override
        protected void projectClosed() {
            NodeProcesses.forProject(project).stop();
        }

    }

}
