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

package org.netbeans.modules.groovy.grails.api;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hejl
 */
public final class ExecutionSupport {

    private static ExecutionSupport instance;

    private final GrailsPlatform platform;

    private ExecutionSupport(GrailsPlatform platform) {
        this.platform = platform;
    }

    public static synchronized ExecutionSupport getInstance() {
        if (instance == null) {
            instance = new ExecutionSupport(GrailsPlatform.getDefault());
        }
        return instance;
    }

    private static ExecutionSupport forRuntime(GrailsPlatform runtime) {
        if (runtime == null) {
            throw new NullPointerException("Runtime is null"); // NOI18N
        }
        if (!runtime.isConfigured()) {
            return null;
        }
        return new ExecutionSupport(runtime);
    }

    // only for wizard
    public Callable<Process> createCreateApp(final File directory) {
        return new Callable<Process>() {

            public Process call() throws Exception {
                if (directory.exists()) {
                    throw new IOException("Project directory already exists"); // NOI18N
                }

                File work = directory.getAbsoluteFile().getParentFile();
                FileUtil.createFolder(work);
                String name = directory.getName();

                String[] args;
                if (GrailsPlatform.Version.VERSION_1_1.compareTo(platform.getVersion()) <= 0) {
                    args = new String[] {name, "--non-interactive"}; // NOI18N
                } else {
                    args = new String[] {name};
                }

                GrailsPlatform.CommandDescriptor descriptor = GrailsPlatform.CommandDescriptor.forProject(
                        "create-app", work, null, args, null); // NOI18N

                return platform.createCommand(descriptor).call();
            }
        };
    }

    public Callable<Process> createRunApp(final GrailsProjectConfig config, final boolean debug,
            final String... arguments) {

        return new Callable<Process>() {

            public Process call() throws Exception {
                File directory = FileUtil.toFile(config.getProject().getProjectDirectory());

                GrailsPlatform.CommandDescriptor descriptor = GrailsPlatform.CommandDescriptor.forProject(
                        GrailsPlatform.IDE_RUN_COMMAND, directory, config, arguments, null, debug);

                return platform.createCommand(descriptor).call();
            }
        };
    }

    public Callable<Process> createSimpleCommand(final String command,
            final GrailsProjectConfig config, final String... arguments) {
        return createSimpleCommand(command, false, config, arguments);
    }

    public Callable<Process> createSimpleCommand(final String command, final boolean debug,
            final GrailsProjectConfig config, final String... arguments) {

        return new Callable<Process>() {

            public Process call() throws Exception {
                File directory = FileUtil.toFile(config.getProject().getProjectDirectory());
                GrailsPlatform.CommandDescriptor descriptor = GrailsPlatform.CommandDescriptor.forProject(
                        command, directory, config, arguments, null, debug);
                return platform.createCommand(descriptor).call();
            }
        };
    }

}
