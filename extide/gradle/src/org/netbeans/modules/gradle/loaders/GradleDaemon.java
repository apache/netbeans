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
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import org.netbeans.modules.gradle.spi.loaders.GradlePluginProvider;
import org.netbeans.modules.gradle.spi.loaders.GradlePluginProvider.GradleRuntime;

import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
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

    public static String initScript(GradleRuntime rt) {
        Path initScript = rt.rootDir.toPath().resolve(".gradle/nb-tooling.gradle"); //NOI18N
        String onDisk = "";
        try {
            onDisk = Files.readString(initScript);
        } catch (IOException ex) {}
        String init = generateInitScript(rt);
        if (!onDisk.equals(init)) {
            try {
                Files.createDirectories(initScript.getParent());
                Files.writeString(initScript, init);
            } catch (IOException ex) {}
        }
        return initScript.toString();
    }
    
    private static String generateInitScript(GradleRuntime rt) {
        var providers = Lookup.getDefault().lookupAll(GradlePluginProvider.class);
        try (var wr = new StringWriter()) {
            wr.append("initscript {\n");
            wr.append("    dependencies {\n");
            for (GradlePluginProvider pvd : providers) {
                for (File f : pvd.classpath(rt)) {
                    String fname = f.getAbsolutePath();
                    wr.append("        classpath files('").append(fname).append("')\n");
                }
            }
            wr.append("    }\n}\n\n");
            
            wr.append("allprojects {\n");
            for (GradlePluginProvider pvd : providers) {
                for (var plugin : pvd.plugins(rt)) {
                    wr.append("    apply plugin: ").append(plugin).append("\n");
                }
            }
            wr.append("}\n");
            
            return wr.toString();
        } catch (IOException ex) {}
        return null;
    }
}
