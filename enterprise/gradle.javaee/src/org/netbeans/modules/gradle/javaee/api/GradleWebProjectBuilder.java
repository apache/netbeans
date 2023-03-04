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

package org.netbeans.modules.gradle.javaee.api;

import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.spi.ProjectInfoExtractor;
import java.io.File;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Laszlo Kishalmi
 */
final class GradleWebProjectBuilder implements ProjectInfoExtractor.Result {

    public static final String WEB_PLUGIN = "war";

    final Map<String, Object> info;
    final GradleWebProject prj = new GradleWebProject();

    GradleWebProjectBuilder(Map<String, Object> info) {
        this.info = info;
    }

    GradleWebProjectBuilder build() {
        prj.webAppDir = (File) info.get("webapp_dir"); //NOI18N
        prj.webXml = (File) info.get("webxml"); //NOI18N
        Set<File> classpath = (Set<File>) info.get("web_classpath"); //NOI18N
        prj.mainWar = (File) info.get("main_war"); //NOI18N
        prj.classpath = classpath != null
                ? Collections.unmodifiableSet(new LinkedHashSet<>(classpath))
                : Collections.<File>emptySet();
        prj.explodedWarDir = (File) info.get("exploded_war_dir");
        return this;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Set getExtract() {
        return Collections.singleton(prj);
    }

    @Override
    public Set<String> getProblems() {
        return Collections.emptySet();
    }

    @ServiceProvider(service = ProjectInfoExtractor.class, position = 100)
    public static final class Extractor implements ProjectInfoExtractor {

        @Override
        public Result fallback(GradleFiles files) {
            File srcDir = new File(files.getProjectDir(), "src/main/webapp");
            String prjName = files.getProjectDir().getName();
            if (srcDir.exists()) {
                GradleWebProject prj = new GradleWebProject();
                prj.webAppDir = srcDir;
                prj.webXml = new File(srcDir, "WEB-INF/web.xml");
                prj.mainWar = new File(files.getProjectDir(), "build/libs/" + prjName + ".war");
                prj.classpath = Collections.<File>emptySet();
                prj.explodedWarDir = new File(files.getProjectDir(), "build/exploded/" + prjName + ".war");
                return new DefaultResult(prj);
            }
            return Result.NONE;
        }

        @Override
        @SuppressWarnings("rawtypes")
        public Result extract(Map<String, Object> props, Map<Class, Object> otherInfo) {
            GradleBaseProject gp = (GradleBaseProject) otherInfo.get(GradleBaseProject.class);
            assert gp != null : "GradleProject should have been evaluated first, check the position of this extractor!";
            if (gp.getPlugins().contains(WEB_PLUGIN)) {
                return new GradleWebProjectBuilder(props).build();
            }
            return Result.NONE;
        }

    }
}
