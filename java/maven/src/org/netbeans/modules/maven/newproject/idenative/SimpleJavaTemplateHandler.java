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
package org.netbeans.modules.maven.newproject.idenative;

import java.io.File;
import java.util.Collections;
import java.util.List;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.archetype.ProjectInfo;
import org.netbeans.modules.maven.classpath.AbstractBootPathImpl;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Project;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.maven.options.MavenSettings;
import org.netbeans.modules.maven.spi.newproject.CreateProjectBuilder;

/**
 *
 * @author sdedic
 */
public class SimpleJavaTemplateHandler extends IDENativeTemplateHandler {

    @Override
    protected CreateProjectBuilder customizeBuilder(CreateProjectBuilder builder, ProjectInfo pi) {
        return super.customizeBuilder(builder, pi).setAdditionalNonPomWork(new CreateProjectBuilder.AdditionalChangeHandle() {
            @Override
            public Runnable createAdditionalChange(final CreateProjectBuilder.Context context) {
                return new Runnable() {

                    @Override
                    public void run() {
                        File src = new File(context.getProjectDirectory(), "src" + File.separator + "main" + File.separator + "java");
                        src.mkdirs();
                        if (context.getPackageName() != null) {
                            String path = context.getPackageName().replace(".", File.separator);
                            new File(src, path).mkdirs();
                        }
                    }
                };
            }
        }).setAdditionalOperations(new CreateProjectBuilder.PomOperationsHandle() {
            //#230984 use source 1.7 by default, unless parent paroject defines something, in that case, just inherit
            @Override
            public List<ModelOperation<POMModel>> createPomOperations(final CreateProjectBuilder.Context context) {
                return Collections.<ModelOperation<POMModel>>singletonList(new ModelOperation<POMModel>() {

                    @Override
                    public void performOperation(POMModel model) {
                        MavenProject mp = context.getParent();
                        boolean setLevel = true;
                        if (mp != null) {
                            String source = PluginPropertyUtils.getPluginProperty(mp, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, "source", "compile", "maven.compiler.source");
                            String target = PluginPropertyUtils.getPluginProperty(mp, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, "target", "compile", "maven.compiler.target");
                            if (target != null || source != null) {
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
                                props.setProperty("maven.compiler.source", version);
                                props.setProperty("maven.compiler.target", version);
                            }
                        }
                    }
                });
            }
        });
    }
}
