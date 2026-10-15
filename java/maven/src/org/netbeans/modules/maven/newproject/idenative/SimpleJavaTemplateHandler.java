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
package org.netbeans.modules.maven.newproject.idenative;

import java.io.File;
import java.util.List;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.archetype.ProjectInfo;
import org.netbeans.modules.maven.classpath.AbstractBootPathImpl;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Project;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.maven.options.MavenSettings;
import org.netbeans.modules.maven.spi.newproject.CreateProjectBuilder;

import static org.netbeans.modules.maven.api.Constants.GROUP_APACHE_PLUGINS;
import static org.netbeans.modules.maven.api.Constants.PLUGIN_COMPILER;

/**
 *
 * @author sdedic
 */
public class SimpleJavaTemplateHandler extends IDENativeTemplateHandler {

    @Override
    protected CreateProjectBuilder customizeBuilder(CreateProjectBuilder builder, ProjectInfo pi) {
        return super.customizeBuilder(builder, pi)
            .setAdditionalNonPomWork(
                (CreateProjectBuilder.Context context) -> () -> {
                    File src = new File(context.getProjectDirectory(), "src" + File.separator + "main" + File.separator + "java");
                    src.mkdirs();
                    String packageName = context.getPackageName();
                    if (packageName != null) {
                        String path = packageName.replace(".", File.separator);
                        new File(src, path).mkdirs();
                    }
                }
            )
            .setAdditionalOperations(
                (CreateProjectBuilder.Context context) -> List.of((POMModel model) -> {
                    MavenProject mp = context.getParent();
                    boolean setLevel = true;
                    if (mp != null) {
                        if (   PluginPropertyUtils.getPluginProperty(mp, GROUP_APACHE_PLUGINS, PLUGIN_COMPILER, "release", "compile", "maven.compiler.release") != null
                            || PluginPropertyUtils.getPluginProperty(mp, GROUP_APACHE_PLUGINS, PLUGIN_COMPILER, "source", "compile", "maven.compiler.source") != null
                            || PluginPropertyUtils.getPluginProperty(mp, GROUP_APACHE_PLUGINS, PLUGIN_COMPILER, "target", "compile", "maven.compiler.target") != null) {
                            setLevel = false;
                        }
                    }
                    if (setLevel) {
                        Project root = model.getProject();
                        if (root != null) {
                            Properties props = root.getProperties();
                            if (props == null) {
                                props = model.getFactory().createProperties();
                                root.setProperties(props);
                            }
                            JavaPlatform active = AbstractBootPathImpl.getActivePlatform(MavenSettings.getDefault().getDefaultJdk());
                            if (active == null) {
                                active = JavaPlatformManager.getDefault().getDefaultPlatform();
                            }
                            String version = active.getSpecification().getVersion().toString();
                            if (version != null && version.startsWith("1.")) {
                                props.setProperty("maven.compiler.source", version);
                                props.setProperty("maven.compiler.target", version);
                            } else {
                                // MCOMPILER-582: compiler plugin 3.13.0+ supports the release flag on all JDK versions
                                props.setProperty("maven.compiler.release", version);
                            }
                        }
                    }
                })
            );
    }
}
