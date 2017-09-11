/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
