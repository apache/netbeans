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

package org.netbeans.modules.maven.queries;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.netbeans.api.java.queries.AnnotationProcessingQuery.Result;
import org.netbeans.api.java.queries.AnnotationProcessingQuery.Trigger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.spi.java.queries.AnnotationProcessingQueryImplementation;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

@ProjectServiceProvider(service=AnnotationProcessingQueryImplementation.class, projectType="org-netbeans-modules-maven")
public class MavenAnnotationProcessingQueryImpl implements AnnotationProcessingQueryImplementation {

    private final Project prj;

    public MavenAnnotationProcessingQueryImpl(Project prj) {
        this.prj = prj;
    }

    public @Override Result getAnnotationProcessingOptions(final FileObject file) {
        return new Result() {
            public @Override Set<? extends Trigger> annotationProcessingEnabled() {
                String version = PluginPropertyUtils.getPluginVersion(prj.getLookup().lookup(NbMavenProject.class).getMavenProject(), Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER);
                if (version != null && new ComparableVersion(version).compareTo(new ComparableVersion("2.2")) < 0) {
                    return EnumSet.noneOf(Trigger.class);
                }
                String compilerArgument = PluginPropertyUtils.getPluginProperty(prj, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, "compilerArgument", tests() ? "testCompile" : "compile", null);
                if ("-proc:none".equals(compilerArgument)) {
                    return EnumSet.noneOf(Trigger.class);
                }
                return EnumSet.allOf(Trigger.class);
            }
            public @Override Iterable<? extends String> annotationProcessorsToRun() {
                String[] procs = PluginPropertyUtils.getPluginPropertyList(prj, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, "annotationProcessors", "annotationProcessor", tests() ? "testCompile" : "compile");
                return procs != null ? Arrays.asList(procs) : null;
            }
            public @Override URL sourceOutputDirectory() {
                boolean tests = tests();
                String generatedSourcesDirectory = PluginPropertyUtils.getPluginProperty(prj, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, "generatedSourcesDirectory", tests ? "testCompile" : "compile", null);
                if (generatedSourcesDirectory == null) {
                    generatedSourcesDirectory = tests ? /* XXX MCOMPILER-167 */"${project.build.directory}/generated-sources/test-annotations" : "${project.build.directory}/generated-sources/annotations";
                }
                try {
                    return FileUtil.urlForArchiveOrDir(new File((String) PluginPropertyUtils.createEvaluator(prj).evaluate(generatedSourcesDirectory)));
                } catch (ExpressionEvaluationException ex) {
                    return null;
                }
            }
            public @Override Map<? extends String, ? extends String> processorOptions() {
                Map<String,String> options = new LinkedHashMap<String,String>();
                options.put("eclipselink.canonicalmodel.use_static_factory", "false"); // #192101
                String goal = tests() ? "testCompile" : "compile";
                Properties props = PluginPropertyUtils.getPluginPropertyParameter(prj, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, "compilerArguments", goal);
                if (props != null) {
                    for (Map.Entry<?,?> entry : props.entrySet()) {
                        String k = (String) entry.getKey();
                        if (k.startsWith("A")) {
                            String v = (String) entry.getValue();
                            options.put(k.substring(1), v.isEmpty() ? null : v);
                        }
                    }
                }
                String compilerArgument = PluginPropertyUtils.getPluginProperty(prj, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, "compilerArgument", goal, null);
                if (compilerArgument != null && compilerArgument.startsWith("-A")) {
                    int idx = compilerArgument.indexOf('=');
                    if (idx != -1) {
                        options.put(compilerArgument.substring(2, idx), compilerArgument.substring(idx + 1));
                    } else {
                        options.put(compilerArgument.substring(2), null);
                    }
                }
                return options;
            }
            public @Override void addChangeListener(ChangeListener l) {}
            public @Override void removeChangeListener(ChangeListener l) {}
            private boolean tests() {
                NbMavenProjectImpl project = prj.getLookup().lookup(NbMavenProjectImpl.class);
                String actual = file.toURI().toString();
                for (URI r : project.getSourceRoots(true)) {
                    if (actual.startsWith(r.toString())) {
                        return true;
                    }
                }
                for (URI r : project.getGeneratedSourceRoots(true)) {
                    if (actual.startsWith(r.toString())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

}
