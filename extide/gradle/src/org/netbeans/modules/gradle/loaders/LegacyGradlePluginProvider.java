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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.loaders.GradlePluginProvider;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lkishalmi
 */
@ServiceProvider(service = GradlePluginProvider.class)
public final class LegacyGradlePluginProvider implements GradlePluginProvider {

    static final String TOOLING_JAR_NAME = "modules/gradle/netbeans-gradle-tooling.jar"; //NOI18N

    private static final File TOOLING_JAR = InstalledFileLocator.getDefault().locate(TOOLING_JAR_NAME, NbGradleProject.CODENAME_BASE, false);

    @Override
    public Collection<File> classpath(GradleRuntime runtime) {
        return Collections.singleton(TOOLING_JAR);
    }

    @Override
    public Collection<String> plugins(GradleRuntime runtime) {
        return List.of(
            "org.netbeans.modules.gradle.tooling.NetBeansToolingPlugin",
            "org.netbeans.modules.gradle.tooling.NetBeansRunSinglePlugin",
            "org.netbeans.modules.gradle.tooling.NetBeansExplodedWarPlugin"
        );
    }
    
}
