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

import org.netbeans.modules.gradle.api.NbGradleProject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openide.modules.InstalledFileLocator;
import org.openide.modules.Places;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class GradleDaemon {
    static final AtomicBoolean initScriptReady = new AtomicBoolean(false);

    static final RequestProcessor GRADLE_LOADER_RP = new RequestProcessor("gradle-project-loader", 1); //NOI18N
    static final String INIT_SCRIPT_NAME = "modules/gradle/nb-tooling.gradle"; //NOI18N
    static final String TOOLING_JAR_NAME = "modules/gradle/netbeans-gradle-tooling.jar"; //NOI18N

    private static final String PROP_TOOLING_JAR = "NETBEANS_TOOLING_JAR";
    private static final String TOOLING_JAR = InstalledFileLocator.getDefault().locate(TOOLING_JAR_NAME, NbGradleProject.CODENAME_BASE, false).getAbsolutePath().replace("\\", "\\\\");

    private static final Logger LOG = Logger.getLogger(GradleDaemon.class.getName());

    private GradleDaemon() {}

    public static String initScript() {
        File initScript = Places.getCacheSubfile("gradle/nb-tooling.gradle"); //NOI18N
        synchronized (initScriptReady) {
            if (!initScriptReady.get()) {
                File initTemplate = InstalledFileLocator.getDefault().locate(INIT_SCRIPT_NAME, NbGradleProject.CODENAME_BASE, false);
                try (Stream<String> lines = Files.lines(initTemplate.toPath())) {
                    List<String> script = lines.map(line -> line.replace(PROP_TOOLING_JAR, TOOLING_JAR)).collect(Collectors.toList());
                    Files.write(initScript.toPath(), script);
                    initScriptReady.set(true);
                } catch(IOException ex) {
                    // This one is unlikely
                    LOG.log(Level.WARNING, "Can't create NetBeans Gradle init script", ex); //NOI18N
                    // Let it pass trough. Gradle call will display some errors as well
                }
            }
        }
        return initScript.getAbsolutePath();
    }
}
